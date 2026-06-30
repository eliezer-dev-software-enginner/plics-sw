package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.EmpresaModel;
import my_app.db.repositories.EmpresaRepository;
import net.sf.persism.Session;



import java.sql.SQLException;
import java.time.LocalDateTime;

public class EmpresaService extends BaseService<EmpresaModel> {

    public EmpresaService() throws SQLException {
        this(DB.getPersismSession());
    }

    public EmpresaService(Session session) {
        super(new EmpresaRepository(session));
    }

    @Override
    public void atualizar(EmpresaModel model) throws SQLException {
        validarCampos(model);
        model.setDataCriacao(LocalDateTime.now());
        repository.atualizar(model);
    }

    public EmpresaModel buscarUnico() throws SQLException {
        EmpresaRepository repo = (EmpresaRepository) repository;
        return repo.buscarUnico();
    }

    public EmpresaModel salvarOuAtualizar(EmpresaModel model) throws SQLException {
        validarCampos(model);
        model.setDataCriacao(LocalDateTime.now());
        var existente = buscarUnico();
        if (existente == null) {
            return repository.salvar(model);
        }
        model.setId(existente.getId());
        repository.atualizar(model);
        return model;
    }

    private void validarCampos(EmpresaModel model) {
        if (model.getNome() == null || model.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
    }
}
