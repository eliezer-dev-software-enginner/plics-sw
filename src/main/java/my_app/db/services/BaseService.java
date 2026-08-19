package my_app.db.services;

import java.sql.SQLException;
import java.util.List;
import my_app.db.repositories.BaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseService<M> implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(BaseService.class);

    protected final BaseRepository<M> repository;

    public BaseService(BaseRepository<M> repository) {
        this.repository = repository;
    }

    // Nunca loga o model inteiro (toString()) aqui: não há garantia nenhuma, nesta classe
    // genérica, de que um Model futuro não vá ganhar campo sensível (senha, token) exposto
    // via um @ToString do Lombok — cada Service concreto que já sobrescreve salvar()/
    // atualizar() (a maioria) loga os próprios campos, escolhidos a dedo, com segurança.
    public M salvar(M model) throws SQLException {
        var salvo = repository.salvar(model);
        log.info("{} salvo", modelName());
        return salvo;
    }

    public List<M> listar() throws SQLException {
        return repository.listar();
    }

    public void atualizar(M model) throws SQLException {
        repository.atualizar(model);
        log.info("{} atualizado", modelName());
    }

    public void excluirById(long id) throws SQLException {
        repository.excluirById(id);
        log.info("{} excluído: id={}", modelName(), id);
    }

    private String modelName() {
        return getClass().getSimpleName().replace("Service", "");
    }

    public M buscarById(long id) throws SQLException {
        return repository.buscarById(id);
    }

    @Override
    public void close() throws Exception {
        repository.close();
    }
}