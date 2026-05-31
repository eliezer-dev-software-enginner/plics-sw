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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public final class PDVService {

    public PDVService() {}

    public PedidoModel finalizarVenda(
            List<ItemVenda> itens,
            String formaPagamento,
            Long clienteId,
            boolean isFiado
    ) throws SQLException {

        Session session = DB.getPersismSession();
        var result = new PedidoModel[1];
        var thrown = new SQLException[1];

        session.withTransaction(() -> {
            try {
                BigDecimal total = itens.stream()
                        .map(ItemVenda::totalItem)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                var pedidoService = new PedidoService(session);
                var pedidoModel = new PedidoModel();
                pedidoModel.setClienteId(clienteId);
                pedidoModel.setFormaPagamento(formaPagamento);
                pedidoModel.setTotalLiquido(total);
                pedidoModel.setDesconto(BigDecimal.ZERO);
                pedidoModel.setFiado(isFiado ? 1 : 0);
                var pedido = pedidoService.salvar(pedidoModel);

                var itemRepo = new PedidoItemRepository(session);
                var produtoService = new ProdutoService(session);

                for (ItemVenda item : itens) {
                    var itemModel = new PedidoItemModel();
                    itemModel.setPedidoId(pedido.getId());
                    itemModel.setProdutoCod(item.produto.getCodigoBarras());
                    itemModel.setQuantidade(item.quantidade);
                    itemModel.setPrecoUnitario(item.produto.getPrecoVenda());
                    itemModel.setDesconto(BigDecimal.ZERO);
                    itemModel.setTotalItem(item.totalItem());
                    itemRepo.salvar(itemModel);

                    produtoService.decrementarEstoque(item.produto.getCodigoBarras(), item.quantidade);
                }

                if (isFiado && clienteId != null) {
                    var contaService = new ContaAreceberService(session);
                    long vencimento = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000);
                    var parcela = new Parcela(1, vencimento, total);
                    contaService.gerarContasDeVenda(pedido.getId(), clienteId.intValue(), List.of(parcela));
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

        return result[0];
    }
}
