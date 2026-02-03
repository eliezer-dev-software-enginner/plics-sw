package my_app.services;

import my_app.db.dto.VendaDto;
import my_app.db.models.VendaModel;
import my_app.db.repositories.ProdutoRepository;
import my_app.db.repositories.VendaRepository;

import java.sql.SQLException;
import java.util.function.Consumer;

public final class VendaMercadoriaService {
    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;
    public boolean deveAtualizarEstoque;

    public VendaMercadoriaService(VendaRepository vendaRepository, ProdutoRepository produtoRepository){
        this.vendaRepository = vendaRepository;
        this.produtoRepository = produtoRepository;
    }

    public VendaModel salvar(VendaDto vendaDto) throws SQLException {
            var venda = vendaRepository.salvar(vendaDto);
            produtoRepository.decrementarEstoque(vendaDto.produtoCod(), vendaDto.quantidade());
            return venda;
    }


    public VendaModel salvarOrThrow(VendaDto vendaDto, Consumer<String> handleErrorMessage) throws RuntimeException{
        try {
            var venda = vendaRepository.salvar(vendaDto);
            produtoRepository.decrementarEstoque(vendaDto.produtoCod(), vendaDto.quantidade());
            return venda;
        } catch (SQLException e) {
            handleErrorMessage.accept(e.getMessage());
            return null;
        }
    }

    public void atualizarOrThrow(VendaModel model, Consumer<String> handleErrorMessage) throws RuntimeException{
        try {
             vendaRepository.atualizar(model);
        } catch (SQLException e) {
            handleErrorMessage.accept(e.getMessage());
        }
    }
}
