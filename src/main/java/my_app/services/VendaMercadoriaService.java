package my_app.services;

import my_app.db.dto.VendaDto;
import my_app.db.models.VendaModel;
import my_app.db.services.ProdutoService;
import my_app.db.services.VendaService;

import java.sql.SQLException;
import java.util.function.Consumer;

public final class VendaMercadoriaService {
    private final VendaService vendaService;
    private final ProdutoService produtoService;
    public boolean deveAtualizarEstoque;

    public VendaMercadoriaService(VendaService vendaService, ProdutoService produtoService){
        this.vendaService = vendaService;
        this.produtoService = produtoService;
    }

    public VendaModel salvar(VendaDto vendaDto) throws SQLException {
        var model = toModel(vendaDto);
        return vendaService.salvar(model, deveAtualizarEstoque);
    }


    public VendaModel salvarOrThrow(VendaDto vendaDto, Consumer<String> handleErrorMessage) throws RuntimeException{
        try {
            var model = toModel(vendaDto);
            return vendaService.salvar(model, deveAtualizarEstoque);
        } catch (SQLException e) {
            handleErrorMessage.accept(e.getMessage());
            return null;
        }
    }

    public void atualizarOrThrow(VendaModel model, Consumer<String> handleErrorMessage) throws RuntimeException{
        try {
            vendaService.atualizar(model);
        } catch (SQLException e) {
            handleErrorMessage.accept(e.getMessage());
        }
    }

    private VendaModel toModel(VendaDto dto) {
        var model = new VendaModel();
        model.setProdutoCod(dto.produtoCod());
        model.setClienteId(dto.clienteId() != null ? dto.clienteId().intValue() : null);
        model.setQuantidade(dto.quantidade());
        model.setPrecoUnitario(dto.precoUnitario());
        model.setDesconto(dto.desconto());
        model.setTipoPagamento(dto.formaPagamento());
        model.setObservacao(dto.observacao());
        model.setTotalLiquido(dto.totalLiquido());
        model.setDataValidade(dto.dataValidade());
        return model;
    }
}
