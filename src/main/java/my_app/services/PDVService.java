package my_app.services;

import my_app.db.DB;
import my_app.db.dto.ContaAreceberDto;
import my_app.db.dto.PedidoDto;
import my_app.db.dto.PedidoItemDto;
import my_app.db.models.PedidoModel;
import my_app.db.repositories.ContasAReceberRepository;
import my_app.db.repositories.PedidoItemRepository;
import my_app.db.repositories.PedidoRepository;
import my_app.db.repositories.ProdutoRepository;
import my_app.screens.pdvScreen.ItemVenda;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class PDVService {

    private final PedidoRepository pedidoRepository;
    private final PedidoItemRepository pedidoItemRepository;
    private final ProdutoRepository produtoRepository;
    private final ContasAReceberRepository contasAReceberRepository;

    public PDVService() {
        this.pedidoRepository = new PedidoRepository();
        this.pedidoItemRepository = new PedidoItemRepository();
        this.produtoRepository = new ProdutoRepository();
        this.contasAReceberRepository = new ContasAReceberRepository();
    }

    public PedidoModel finalizarVenda(
            List<ItemVenda> itens,
            String formaPagamento,
            Long clienteId,
            boolean isFiado
    ) throws SQLException {

        Connection conn = DB.getInstance().connection();
        conn.setAutoCommit(false);

        try {
            // 1. Salva o cabeçalho do pedido
            BigDecimal total = itens.stream()
                    .map(ItemVenda::totalItem)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            var pedidoDto = new PedidoDto(clienteId, formaPagamento, total, BigDecimal.ZERO, null, isFiado);
            var pedido = pedidoRepository.salvar(pedidoDto);

            // 2. Salva cada item e decrementa estoque
            for (ItemVenda item : itens) {
                var itemDto = new PedidoItemDto(
                        pedido.id,
                        item.produto.codigoBarras,
                        item.quantidade,
                        item.produto.precoVenda,
                        BigDecimal.ZERO,
                        item.totalItem()
                );
                pedidoItemRepository.salvar(itemDto);
                produtoRepository.decrementarEstoque(item.produto.codigoBarras, item.quantidade);
            }

            // 3. Se for fiado, gera conta a receber
            if (isFiado && clienteId != null) {
                long vencimento = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000); // 30 dias
                var contaDto = new ContaAreceberDto(
                        "Venda PDV #" + pedido.id,
                        total,
                        BigDecimal.ZERO,
                        total,
                        vencimento,
                        null,
                        "PENDENTE",
                        clienteId,
                        null, // vendaId — pedido é a nova entidade
                        null, null, null
                );
                contasAReceberRepository.salvar(contaDto);
            }

            conn.commit();
            return pedido;

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
