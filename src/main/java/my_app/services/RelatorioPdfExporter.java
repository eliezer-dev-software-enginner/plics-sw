package my_app.services;

import my_app.db.models.EmpresaModel;
import my_app.utils.Utils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Gera o PDF do RelatoriosScreen usando Apache PDFBox (mesma família de libs do
// Apache POI já usado no projeto pra ler planilhas). Layout simples de texto —
// o relatório é uma lista de valores, não precisa de tabela/gráfico.
public class RelatorioPdfExporter {

    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final float MARGEM = 50;
    private static final float LEADING = 20;

    public void gerar(File destino, EmpresaModel empresa, LocalDate inicio, LocalDate fim, RelatorioDados dados) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            var page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            var fonteTitulo = PDType1Font.HELVETICA_BOLD;
            var fonteSecao = PDType1Font.HELVETICA_BOLD;
            var fonteTexto = PDType1Font.HELVETICA;

            try (var cs = new PDPageContentStream(doc, page)) {
                float y = page.getMediaBox().getHeight() - MARGEM;

                y = escreverLinha(cs, fonteTitulo, 18, MARGEM, y, empresa != null && empresa.getNome() != null ? empresa.getNome() : "Plics SW");
                if (empresa != null && empresa.getCpfCnpj() != null && !empresa.getCpfCnpj().isBlank()) {
                    String doc2 = empresa.getCpfCnpj().length() == 14
                            ? Utils.formatCnpj(empresa.getCpfCnpj())
                            : Utils.formatCpf(empresa.getCpfCnpj());
                    y = escreverLinha(cs, fonteTexto, 10, MARGEM, y, "CNPJ/CPF: " + doc2);
                }
                y -= LEADING / 2;

                y = escreverLinha(cs, fonteTitulo, 14, MARGEM, y, "Relatório Financeiro");
                y = escreverLinha(cs, fonteTexto, 11, MARGEM, y,
                        "Período: " + inicio.format(DATA_FMT) + " a " + fim.format(DATA_FMT));
                y -= LEADING;

                y = escreverLinha(cs, fonteSecao, 12, MARGEM, y, "RECEITAS");
                y = escreverLinha(cs, fonteTexto, 11, MARGEM, y, "Vendas de mercadoria: " + Utils.toBRLCurrency(dados.receitasVendas()));
                y = escreverLinha(cs, fonteTexto, 11, MARGEM, y, "Vendas no PDV: " + Utils.toBRLCurrency(dados.receitasPedidosPdv()));
                y = escreverLinha(cs, fonteTexto, 11, MARGEM, y, "Contas a receber recebidas: " + Utils.toBRLCurrency(dados.receitasContasRecebidas()));
                y = escreverLinha(cs, fonteSecao, 11, MARGEM, y, "Total de receitas: " + Utils.toBRLCurrency(dados.totalReceitas()));
                y -= LEADING;

                y = escreverLinha(cs, fonteSecao, 12, MARGEM, y, "DESPESAS");
                y = escreverLinha(cs, fonteTexto, 11, MARGEM, y, "Compras de mercadoria: " + Utils.toBRLCurrency(dados.despesasCompras()));
                y = escreverLinha(cs, fonteTexto, 11, MARGEM, y, "Contas a pagar pagas: " + Utils.toBRLCurrency(dados.despesasContasPagas()));
                y = escreverLinha(cs, fonteSecao, 11, MARGEM, y, "Total de despesas: " + Utils.toBRLCurrency(dados.totalDespesas()));
                y -= LEADING;

                y = escreverLinha(cs, fonteTitulo, 13, MARGEM, y, "Lucro líquido do período: " + Utils.toBRLCurrency(dados.lucroLiquido()));
                y -= LEADING;

                y = escreverLinha(cs, fonteSecao, 12, MARGEM, y, "PRODUTOS MAIS VENDIDOS DO PERÍODO");
                if (dados.produtosMaisVendidos() == null || dados.produtosMaisVendidos().isEmpty()) {
                    y = escreverLinha(cs, fonteTexto, 11, MARGEM, y, "Nenhum produto vendido no período");
                } else {
                    int posicao = 1;
                    for (var produto : dados.produtosMaisVendidos()) {
                        String unidade = produto.unidade() != null && !produto.unidade().isBlank() ? produto.unidade() : "un";
                        y = escreverLinha(cs, fonteTexto, 11, MARGEM, y, posicao + ". " + produto.descricao() + " — " +
                                Utils.quantidadeTratada(produto.quantidade()) + " " + unidade);
                        posicao++;
                    }
                }
                y -= LEADING;

                y = escreverLinha(cs, fonteSecao, 12, MARGEM, y, "SITUAÇÃO ATUAL (não vinculada ao período)");
                y = escreverLinha(cs, fonteTexto, 11, MARGEM, y, "Contas a receber em aberto: " + Utils.toBRLCurrency(dados.contasReceberEmAberto()));
                y = escreverLinha(cs, fonteTexto, 11, MARGEM, y, "Contas a pagar em aberto: " + Utils.toBRLCurrency(dados.contasPagarEmAberto()));
                y -= LEADING * 2;

                escreverLinha(cs, fonteTexto, 8, MARGEM, y,
                        "Gerado em " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " pelo Plics SW");
            }

            doc.save(destino);
        }
    }

    private float escreverLinha(PDPageContentStream cs, PDFont fonte, float tamanho, float x, float y, String texto) throws IOException {
        cs.beginText();
        cs.setFont(fonte, tamanho);
        cs.newLineAtOffset(x, y);
        cs.showText(texto != null ? texto : "");
        cs.endText();
        return y - LEADING;
    }
}
