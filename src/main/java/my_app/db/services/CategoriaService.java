package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.CategoriaModel;
import my_app.db.repositories.CategoriaRepository;
import net.sf.persism.Session;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class CategoriaService extends BaseService<CategoriaModel> {

    // produção
    public CategoriaService() throws SQLException {
        this(new Session(DB.production().connection()));
    }

    // testes
    public CategoriaService(Session session) {
        super(new CategoriaRepository(session));
    }

    @Override
    public CategoriaModel salvar(CategoriaModel model) throws SQLException {
        validarCampos(model);
        validarNome(model.getNome(), -1);
        model.setDataCriacao(LocalDateTime.now());
        return repository.salvar(model);
    }

    @Override
    public void atualizar(CategoriaModel model) throws SQLException {
        validarCampos(model);
        validarNome(model.getNome(), model.getId());
        repository.atualizar(model);
    }

    private void validarNome(String nome, long idAtual) throws SQLException {
        boolean duplicado = repository.listar().stream()
                .filter(c -> c.getId() != idAtual)
                .anyMatch(c -> c.getNome().equalsIgnoreCase(nome.trim()));

        if (duplicado) throw new IllegalArgumentException("Já existe uma categoria com esse nome");
    }

    private void validarCampos(CategoriaModel model) {
        if (model.getNome() == null || model.getNome().isBlank())
            throw new IllegalArgumentException("Nome é obrigatório");
    }
}