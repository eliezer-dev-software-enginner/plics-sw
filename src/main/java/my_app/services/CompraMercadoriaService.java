package my_app.services;

import my_app.db.dto.CompraDto;
import my_app.db.models_old.CompraModel;
import my_app.db.repositories_old.ComprasRepository;
import my_app.db.repositories_old.ProdutoRepository;

import java.sql.SQLException;
import java.util.function.Consumer;

public final class CompraMercadoriaService {
    private final ComprasRepository compraRepository;
    private final ProdutoRepository produtoRepository;

    public boolean deveAtualizarEstoque = true;

    public CompraMercadoriaService(ComprasRepository compraRepository, ProdutoRepository produtoRepository){
        this.compraRepository = compraRepository;
        this.produtoRepository = produtoRepository;
    }

    public CompraModel salvarOrThrow(CompraDto dto, Consumer<String> handleErrorMessage) throws RuntimeException{
        try {
            var compraSalva = compraRepository.salvar(dto);
            if(deveAtualizarEstoque){
                produtoRepository.incrementarEstoque(dto.produtoCod(), dto.quantidade());
            }
            return compraSalva;
        } catch (SQLException e) {
            handleErrorMessage.accept(e.getMessage());
            return null;
        }
    }

    public void atualizarOrThrow(CompraModel model, Consumer<String> handleErrorMessage) throws RuntimeException{
        try {
             compraRepository.atualizar(model);
        } catch (SQLException e) {
            handleErrorMessage.accept(e.getMessage());
        }
    }
}
