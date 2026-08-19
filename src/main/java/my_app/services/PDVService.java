package my_app.services;

import my_app.db.DB;
import my_app.db.models.PedidoModel;
import my_app.db.models.PedidoItemModel;
import my_app.db.repositories.PedidoItemRepository;
import my_app.db.services.ContaAreceberService;
import my_app.db.services.PedidoService;
import my_app.db.services.ProdutoService;
import my_app.domain.Parcela;
import my_app.screens.pdvScreen.ItemVenda;
import net.sf.persism.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class PDVService {

    private static final Logger log = LoggerFactory.getLogger(PDVService.class);

    private final Session session;

    public PDVService() {
        this.session = null;
    }

    public PDVService(Session session) {
        this.session = session;
    }

    public PedidoModel finalizarVenda(
            List<ItemVenda> itens,
            String formaPagamento,
            Integer clienteId,
            boolean isFiado,
            int numeroParcelas
    ) throws SQLException {
        return finalizarVenda(itens, formaPagamento, clienteId, isFiado, numeroParcelas, BigDecimal.ZERO);
    }

    public PedidoModel finalizarVenda(
            List<ItemVenda> itens,
            String formaPagamento,
            Integer clienteId,
            boolean isFiado,
            int numeroParcelas,
            BigDecimal desconto
    ) throws SQLException {
        return finalizarVenda(itens, formaPagamento, clienteId, isFiado, numeroParcelas, desconto, BigDecimal.ZERO);
    }

    public PedidoModel finalizarVenda(
            List<ItemVenda> itens,
            String formaPagamento,
            Integer clienteId,
            boolean isFiado,
            int numeroParcelas,
            BigDecimal desconto,
            BigDecimal frete
    ) throws SQLException {

        var sess = session != null ? session : DB.getPersismSession();
        var result = new PedidoModel[1];
        var thrown = new SQLException[1];

        sess.withTransaction(() -> {
            try {
                BigDecimal totalBruto = itens.stream()
                        .map(ItemVenda::totalItem)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal descontoValue = desconto != null ? desconto : BigDecimal.ZERO;
                BigDecimal freteValue = frete != null ? frete : BigDecimal.ZERO;
                BigDecimal totalLiquido = totalBruto.subtract(descontoValue).add(freteValue);
                if (totalLiquido.compareTo(BigDecimal.ZERO) < 0) totalLiquido = BigDecimal.ZERO;

                var pedidoService = new PedidoService(sess);
                var pedidoModel = new PedidoModel();
                pedidoModel.setClienteId(clienteId);
                pedidoModel.setFormaPagamento(formaPagamento);
                pedidoModel.setTotalLiquido(totalLiquido);
                pedidoModel.setDesconto(descontoValue);
                pedidoModel.setFrete(freteValue);
                pedidoModel.setFiado(isFiado ? 1 : 0);
                var pedido = pedidoService.salvar(pedidoModel);

                var itemRepo = new PedidoItemRepository(sess);
                var produtoService = new ProdutoService(sess);

                for (ItemVenda item : itens) {
                    var itemModel = new PedidoItemModel();
                    itemModel.setPedidoId(pedido.getId());
                    itemModel.setProdutoCod(item.produto.getCodigoBarras());
                    itemModel.setQuantidade(item.quantidade);
                    itemModel.setPrecoUnitario(item.produto.getPrecoVenda());
                    itemModel.setDesconto(BigDecimal.ZERO);
                    itemModel.setTotalItem(item.totalItem());
                    itemModel.setDataCriacao(LocalDateTime.now());
                    itemRepo.salvar(itemModel);

                    produtoService.decrementarEstoque(item.produto.getCodigoBarras(), item.quantidade);
                }

                if (isFiado && clienteId != null) {
                    var contaService = new ContaAreceberService(sess);
                    var parcelas = Parcela.gerarParcelas(LocalDate.now(), Math.max(1, numeroParcelas), totalLiquido.doubleValue());
                    contaService.gerarContasDeVenda(pedido.getId(), clienteId, parcelas);
                }

                result[0] = pedido;
            } catch (SQLException e) {
                thrown[0] = e;
                throw new RuntimeException(e);
            }
        });

        if (thrown[0] != null) {
            throw thrown[0];
        }

        log.info("Venda PDV finalizada: pedidoId={} itens={} totalLiquido={}",
                result[0].getId(), itens.size(), result[0].getTotalLiquido());
        return result[0];
    }

    // Reverso de finalizarVenda(): devolve o estoque de cada item, apaga os itens e
    // as contas a receber vinculadas (se a venda era fiada) e por fim o pedido.
    public void excluirVenda(int pedidoId) throws SQLException {
        var sess = session != null ? session : DB.getPersismSession();
        var thrown = new SQLException[1];

        sess.withTransaction(() -> {
            try {
                var pedidoService = new PedidoService(sess);
                var pedido = pedidoService.buscarById(pedidoId);
                if (pedido == null) throw new IllegalArgumentException("Venda não encontrada");

                var itemRepo = new PedidoItemRepository(sess);
                var produtoService = new ProdutoService(sess);
                var itens = itemRepo.listarPorPedido(pedidoId);

                for (PedidoItemModel item : itens) {
                    produtoService.incrementarEstoque(item.getProdutoCod(), item.getQuantidade());
                }
                itemRepo.excluirPorPedidoId(pedidoId);

                if (Integer.valueOf(1).equals(pedido.getFiado())) {
                    var contaService = new ContaAreceberService(sess);
                    contaService.excluirPorVendaId(pedidoId);
                }

                pedidoService.excluirById(pedidoId);
            } catch (SQLException e) {
                thrown[0] = e;
                throw new RuntimeException(e);
            }
        });

        if (thrown[0] != null) {
            throw thrown[0];
        }
        log.info("Venda PDV excluída: pedidoId={}", pedidoId);
    }

    // Igual a excluirVenda() na devolução de estoque e contas, mas preserva o pedido
    // (marca devolvida=true em vez de apagar), mantendo o histórico do caixa.
    public void devolverVenda(int pedidoId) throws SQLException {
        var sess = session != null ? session : DB.getPersismSession();
        var thrown = new SQLException[1];

        sess.withTransaction(() -> {
            try {
                var pedidoService = new PedidoService(sess);
                var pedido = pedidoService.buscarById(pedidoId);
                if (pedido == null) throw new IllegalArgumentException("Venda não encontrada");
                if (Boolean.TRUE.equals(pedido.getDevolvida()))
                    throw new IllegalArgumentException("Esta venda já foi devolvida");

                var itemRepo = new PedidoItemRepository(sess);
                var produtoService = new ProdutoService(sess);
                var itens = itemRepo.listarPorPedido(pedidoId);

                for (PedidoItemModel item : itens) {
                    produtoService.incrementarEstoque(item.getProdutoCod(), item.getQuantidade());
                }

                if (Integer.valueOf(1).equals(pedido.getFiado())) {
                    var contaService = new ContaAreceberService(sess);
                    for (var conta : contaService.buscarPorVenda(pedidoId)) {
                        if ("PAGO".equals(conta.getStatus()) || "PARCIAL".equals(conta.getStatus())) {
                            contaService.cancelarRecebimento(conta.getId());
                        }
                    }
                    contaService.excluirPorVendaId(pedidoId);
                }

                pedido.setDevolvida(true);
                pedido.setDataDevolucao(System.currentTimeMillis());
                pedidoService.atualizar(pedido);
            } catch (SQLException e) {
                thrown[0] = e;
                throw new RuntimeException(e);
            }
        });

        if (thrown[0] != null) {
            throw thrown[0];
        }
        log.info("Venda PDV devolvida: pedidoId={}", pedidoId);
    }
}
