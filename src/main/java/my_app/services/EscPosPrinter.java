package my_app.services;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.output.PrinterOutputStream;
import com.github.anastaciocintra.output.TcpIpOutputStream;
import my_app.db.models.ClienteModel;
import my_app.db.models.EmpresaModel;
import my_app.db.models.PedidoItemModel;
import my_app.db.models.PedidoModel;
import my_app.db.models.VendaModel;
import my_app.db.services.EmpresaService;
import my_app.utils.DateUtils;
import my_app.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.PrintService;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.fazecast.jSerialComm.SerialPort;
public class EscPosPrinter implements ComprovanteBuilder {
    private static final Logger log = LoggerFactory.getLogger(EscPosPrinter.class);
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String SEP = "--------------------------------";

    private final EmpresaService empresaService;
    private final OutputStream outputStream;
    private final String portaImpressora;

    public EscPosPrinter() {
        this.empresaService = createEmpresaService();
        this.outputStream = null;
        this.portaImpressora = null;
    }

    public EscPosPrinter(EmpresaService empresaService) {
        this.empresaService = empresaService;
        this.outputStream = null;
        this.portaImpressora = null;
    }

    public EscPosPrinter(OutputStream outputStream) {
        this.empresaService = createEmpresaService();
        this.outputStream = outputStream;
        this.portaImpressora = null;
    }

    public EscPosPrinter(EmpresaService empresaService, OutputStream outputStream) {
        this.empresaService = empresaService;
        this.outputStream = outputStream;
        this.portaImpressora = null;
    }

    // novo construtor para impressora via porta serial (Bluetooth RFCOMM / COM)
    public EscPosPrinter(EmpresaService empresaService, String portaImpressora) {
        this.empresaService = empresaService;
        this.outputStream = null;
        this.portaImpressora = portaImpressora;
    }

    public static EscPosPrinter viaTcp(String host, int port) {
        try {
            return new EscPosPrinter(new TcpIpOutputStream(host, port));
        } catch (IOException e) {
            throw new RuntimeException("Erro ao conectar à impressora TCP: " + e.getMessage(), e);
        }
    }

    public static EscPosPrinter viaTcp(String host) {
        return viaTcp(host, 9205);
    }

    private static EmpresaService createEmpresaService() {
        try {
            return new EmpresaService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void imprimir(VendaModel venda) {
        EmpresaModel empresa = buscarEmpresa();
        if (!tentarEscPos(escpos -> {
            if (empresa != null) cabecalho(escpos, empresa);
            separador(escpos);
            titulo(escpos, "NOTA DE VENDA");
            linha(escpos, "N. " + (venda.getNumeroNota() != null ? venda.getNumeroNota() : String.valueOf(venda.getId())));
            linha(escpos, "Data: " + (venda.getDataCriacao() != null
                    ? venda.getDataCriacao().format(DT_FMT)
                    : DateUtils.millisToBrazilianDateTime(venda.getDataVenda())));
            ClienteModel cliente = venda.getCliente();
            if (cliente != null) linha(escpos, "Cliente: " + cliente.getNome());
            separador(escpos);
            titulo(escpos, "ITEM");
            if (venda.getProduto() != null) linha(escpos, "Produto: " + venda.getProduto().getDescricao());
            linha(escpos, "Cod: " + venda.getProdutoCod());
            linha(escpos, "Qtd: " + venda.getQuantidade().stripTrailingZeros().toPlainString());
            linha(escpos, "Vl. Unit.: " + Utils.toBRLCurrency(venda.getPrecoUnitario()));
            if (venda.getDesconto() != null && venda.getDesconto().compareTo(java.math.BigDecimal.ZERO) > 0) {
                linha(escpos, "Desconto: " + Utils.toBRLCurrency(venda.getDesconto()));
            }
            linha(escpos, "Total: " + Utils.toBRLCurrency(venda.getTotalLiquido()));
            separador(escpos);
            linha(escpos, "Pagamento: " + venda.getTipoPagamento());
            if (venda.getObservacao() != null && !venda.getObservacao().isBlank()) {
                linha(escpos, "Obs: " + venda.getObservacao());
            }
            rodape(escpos);
        })) {
            salvarPreviewTxt(gerarPreviewNotaVenda(venda, empresa));
        }
    }

    public void imprimirNotaVenda(PedidoModel pedido, List<PedidoItemModel> itens, ClienteModel cliente, EmpresaModel empresa) {
        if (!tentarEscPos(escpos -> {
            if (empresa != null) cabecalho(escpos, empresa);
            separador(escpos);
            titulo(escpos, "NOTA DE VENDA");
            linha(escpos, "N. " + pedido.getId());
            linha(escpos, "Data: " + (pedido.getDataCriacao() != null
                    ? pedido.getDataCriacao().format(DT_FMT)
                    : LocalDateTime.now().format(DT_FMT)));
            if (cliente != null) linha(escpos, "Cliente: " + cliente.getNome());
            separador(escpos);
            titulo(escpos, "ITENS");
            for (var item : itens) {
                linha(escpos, item.getProdutoCod() + "  x" + item.getQuantidade().stripTrailingZeros().toPlainString()
                        + "  " + Utils.toBRLCurrency(item.getPrecoUnitario())
                        + "  = " + Utils.toBRLCurrency(item.getTotalItem()));
            }
            separador(escpos);
            linha(escpos, "Total: " + Utils.toBRLCurrency(pedido.getTotalLiquido()));
            if (pedido.getDesconto() != null && pedido.getDesconto().compareTo(java.math.BigDecimal.ZERO) > 0) {
                linha(escpos, "Desconto: " + Utils.toBRLCurrency(pedido.getDesconto()));
            }
            linha(escpos, "Pagamento: " + (pedido.getFormaPagamento() != null ? pedido.getFormaPagamento() : "A VISTA"));
            if (pedido.getObservacao() != null && !pedido.getObservacao().isBlank()) {
                linha(escpos, "Obs: " + pedido.getObservacao());
            }
            rodape(escpos);
        })) {
            salvarPreviewTxt(gerarPreviewNotaVendaPedido(pedido, itens, cliente, empresa));
        }
    }

    @FunctionalInterface
    private interface EscPosConsumer {
        void accept(EscPos escpos) throws Exception;
    }

    private boolean tentarEscPos(EscPosConsumer consumer) {
        try {
            OutputStream out = outputStream != null ? outputStream : resolverOutputStream();
            if (out == null) return false;
            try (EscPos escpos = new EscPos(out)) {
                escpos.setCharacterCodeTable(EscPos.CharacterCodeTable.CP860_Portuguese);
                escpos.initializePrinter();
                consumer.accept(escpos);
                escpos.flush();
            }
            return true;
        } catch (Exception e) {
            log.warn("Falha ao imprimir via ESC/POS, gerando preview .txt: {}", e.getMessage());
            return false;
        }
    }

    private OutputStream resolverOutputStream() throws Exception {
        // Tenta primeiro a porta serial configurada (Bluetooth RFCOMM / COM)
        if (portaImpressora != null && !portaImpressora.isBlank()) {
            SerialPort porta = SerialPort.getCommPort(portaImpressora);
            porta.setBaudRate(9600);
            porta.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 2000);
            if (porta.openPort()) {
                return porta.getOutputStream();
            }
            log.warn("Não foi possível abrir a porta serial {}", portaImpressora);
        }

        // Fallback: impressora registrada no sistema (spooler)
        PrintService ps = PrinterOutputStream.getDefaultPrintService();
        if (ps != null) return new PrinterOutputStream(ps);

        log.warn("Nenhuma impressora encontrada");
        return null;
    }

    private EmpresaModel buscarEmpresa() {
        try {
            return empresaService.buscarUnico();
        } catch (SQLException e) {
            log.warn("Empresa nao cadastrada, imprimindo sem cabecalho");
            return null;
        }
    }

    private void salvarPreviewTxt(String conteudo) {
        String filename = "nota_venda_" + System.currentTimeMillis() + ".txt";
        try (PrintWriter pw = new PrintWriter(filename, StandardCharsets.UTF_8)) {
            pw.print(conteudo);
            log.info("Preview salvo em: {}", filename);
        } catch (IOException e) {
            log.error("Erro ao salvar preview .txt", e);
        }
    }

    private String gerarPreviewNotaVenda(VendaModel venda, EmpresaModel empresa) {
        var sb = new StringBuilder();
        if (empresa != null) {
            sb.append(centrado(empresa.getNome())).append("\n");
            if (empresa.getCpfCnpj() != null && !empresa.getCpfCnpj().isBlank()) {
                String doc = empresa.getCpfCnpj().length() == 14
                        ? Utils.formatCnpj(empresa.getCpfCnpj())
                        : Utils.formatCpf(empresa.getCpfCnpj());
                sb.append(centrado("CNPJ/CPF: " + doc)).append("\n");
            }
            if (empresa.getTelefone() != null && !empresa.getTelefone().isBlank()) {
                sb.append(centrado("Tel: " + Utils.formatPhone(empresa.getTelefone()))).append("\n");
            }
        }
        sb.append(SEP).append("\n");
        sb.append(centrado("NOTA DE VENDA")).append("\n");
        sb.append("N. ").append(venda.getNumeroNota() != null ? venda.getNumeroNota() : String.valueOf(venda.getId())).append("\n");
        sb.append("Data: ").append(venda.getDataCriacao() != null
                ? venda.getDataCriacao().format(DT_FMT)
                : DateUtils.millisToBrazilianDateTime(venda.getDataVenda())).append("\n");
        ClienteModel cliente = venda.getCliente();
        if (cliente != null) sb.append("Cliente: ").append(cliente.getNome()).append("\n");
        sb.append(SEP).append("\n");
        sb.append(centrado("ITEM")).append("\n");
        if (venda.getProduto() != null) sb.append("Produto: ").append(venda.getProduto().getDescricao()).append("\n");
        sb.append("Cod: ").append(venda.getProdutoCod()).append("\n");
        sb.append("Qtd: ").append(venda.getQuantidade().stripTrailingZeros().toPlainString()).append("\n");
        sb.append("Vl. Unit.: ").append(Utils.toBRLCurrency(venda.getPrecoUnitario())).append("\n");
        if (venda.getDesconto() != null && venda.getDesconto().compareTo(java.math.BigDecimal.ZERO) > 0) {
            sb.append("Desconto: ").append(Utils.toBRLCurrency(venda.getDesconto())).append("\n");
        }
        sb.append("Total: ").append(Utils.toBRLCurrency(venda.getTotalLiquido())).append("\n");
        sb.append(SEP).append("\n");
        sb.append("Pagamento: ").append(venda.getTipoPagamento()).append("\n");
        if (venda.getObservacao() != null && !venda.getObservacao().isBlank()) {
            sb.append("Obs: ").append(venda.getObservacao()).append("\n");
        }
        sb.append("\n\n");
        sb.append(centrado("Obrigado pela preferencia!")).append("\n");
        return sb.toString();
    }

    private String gerarPreviewNotaVendaPedido(PedidoModel pedido, List<PedidoItemModel> itens, ClienteModel cliente, EmpresaModel empresa) {
        var sb = new StringBuilder();
        if (empresa != null) {
            sb.append(centrado(empresa.getNome())).append("\n");
            if (empresa.getCpfCnpj() != null && !empresa.getCpfCnpj().isBlank()) {
                String doc = empresa.getCpfCnpj().length() == 14
                        ? Utils.formatCnpj(empresa.getCpfCnpj())
                        : Utils.formatCpf(empresa.getCpfCnpj());
                sb.append(centrado("CNPJ/CPF: " + doc)).append("\n");
            }
            if (empresa.getTelefone() != null && !empresa.getTelefone().isBlank()) {
                sb.append(centrado("Tel: " + Utils.formatPhone(empresa.getTelefone()))).append("\n");
            }
        }
        sb.append(SEP).append("\n");
        sb.append(centrado("NOTA DE VENDA")).append("\n");
        sb.append("N. ").append(pedido.getId()).append("\n");
        sb.append("Data: ").append(pedido.getDataCriacao() != null
                ? pedido.getDataCriacao().format(DT_FMT)
                : LocalDateTime.now().format(DT_FMT)).append("\n");
        if (cliente != null) sb.append("Cliente: ").append(cliente.getNome()).append("\n");
        sb.append(SEP).append("\n");
        sb.append(centrado("ITENS")).append("\n");
        for (var item : itens) {
            sb.append(item.getProdutoCod())
                    .append("  x").append(item.getQuantidade().stripTrailingZeros().toPlainString())
                    .append("  ").append(Utils.toBRLCurrency(item.getPrecoUnitario()))
                    .append("  = ").append(Utils.toBRLCurrency(item.getTotalItem()))
                    .append("\n");
        }
        sb.append(SEP).append("\n");
        sb.append("Total: ").append(Utils.toBRLCurrency(pedido.getTotalLiquido())).append("\n");
        if (pedido.getDesconto() != null && pedido.getDesconto().compareTo(java.math.BigDecimal.ZERO) > 0) {
            sb.append("Desconto: ").append(Utils.toBRLCurrency(pedido.getDesconto())).append("\n");
        }
        sb.append("Pagamento: ").append(pedido.getFormaPagamento() != null ? pedido.getFormaPagamento() : "A VISTA").append("\n");
        if (pedido.getObservacao() != null && !pedido.getObservacao().isBlank()) {
            sb.append("Obs: ").append(pedido.getObservacao()).append("\n");
        }
        sb.append("\n\n");
        sb.append(centrado("Obrigado pela preferencia!")).append("\n");
        return sb.toString();
    }

    private static String centrado(String texto) {
        int espacos = (SEP.length() - (texto != null ? texto.length() : 0)) / 2;
        if (espacos <= 0) return texto != null ? texto : "";
        return " ".repeat(espacos) + (texto != null ? texto : "");
    }

    private void cabecalho(EscPos escpos, EmpresaModel empresa) throws Exception {
        centralizado(escpos, empresa.getNome(), true, Style.FontSize._2);
        if (empresa.getCpfCnpj() != null && !empresa.getCpfCnpj().isBlank()) {
            String doc = empresa.getCpfCnpj().length() == 14
                    ? Utils.formatCnpj(empresa.getCpfCnpj())
                    : Utils.formatCpf(empresa.getCpfCnpj());
            centralizado(escpos, "CNPJ/CPF: " + doc, false, Style.FontSize._1);
        }
        if (empresa.getTelefone() != null && !empresa.getTelefone().isBlank()) {
            centralizado(escpos, "Tel: " + Utils.formatPhone(empresa.getTelefone()), false, Style.FontSize._1);
        }
    }

    private void separador(EscPos escpos) throws Exception {
        escpos.writeLF(SEP);
    }

    private void titulo(EscPos escpos, String texto) throws Exception {
        centralizado(escpos, texto, true, Style.FontSize._1);
    }

    private void linha(EscPos escpos, String texto) throws Exception {
        escpos.writeLF(texto != null ? texto : "");
    }

    private void centralizado(EscPos escpos, String texto, boolean negrito, Style.FontSize tamanho) throws Exception {
        Style style = new Style()
                .setJustification(EscPosConst.Justification.Center)
                .setBold(negrito)
                .setFontSize(tamanho, tamanho);
        escpos.writeLF(style, texto != null ? texto : "");
    }

    private void rodape(EscPos escpos) throws Exception {
        escpos.feed(2);
        centralizado(escpos, "Obrigado pela preferencia!", true, Style.FontSize._1);
        escpos.feed(4);
        escpos.cut(EscPos.CutMode.FULL);
        escpos.flush();
    }
}
