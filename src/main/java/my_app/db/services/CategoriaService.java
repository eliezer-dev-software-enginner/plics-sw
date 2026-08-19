package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.CategoriaModel;
import my_app.db.repositories.CategoriaRepository;
import net.sf.persism.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class CategoriaService extends BaseService<CategoriaModel> {

    private static final Logger log = LoggerFactory.getLogger(CategoriaService.class);

    // produção
    public CategoriaService() throws SQLException {
        this(DB.getPersismSession());
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
        var salvo = repository.salvar(model);
        log.info("Categoria salva: id={} nome={}", salvo.getId(), salvo.getNome());
        return salvo;
    }

    @Override
    public void atualizar(CategoriaModel model) throws SQLException {
        validarCampos(model);
        validarNome(model.getNome(), model.getId());
        repository.atualizar(model);
        log.info("Categoria atualizada: id={} nome={}", model.getId(), model.getNome());
    }

    private void validarNome(String nome, Integer idAtual) throws SQLException {
        boolean duplicado = repository.listar().stream()
                .filter(c -> !c.getId().equals(idAtual))
                .anyMatch(c -> c.getNome().equalsIgnoreCase(nome.trim()));

        if (duplicado) throw new IllegalArgumentException("Já existe uma categoria com esse nome");
    }

    private void validarCampos(CategoriaModel model) {
        if (model.getNome() == null || model.getNome().isBlank())
            throw new IllegalArgumentException("Nome é obrigatório");
    }
}