package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.ClienteModel;
import my_app.db.repositories.ClienteRepository;
import net.sf.persism.Session;

import java.sql.SQLException;
import java.time.LocalDateTime;

import static my_app.utils.Utils.*;
import static my_app.utils.Utils.isValidPhone;

public class ClienteService extends BaseService<ClienteModel> {

    private final ClienteRepository clienteRepository;

    // produção
    public ClienteService() throws SQLException {
        this(new Session(DB.production().connection()));
    }

    // testes
    public ClienteService(Session session) {
        super(new ClienteRepository(session));
        this.clienteRepository = (ClienteRepository) repository;
    }

    @Override
    public ClienteModel salvar(ClienteModel model) throws SQLException {
        validarCampos(model);
        model.setDataCriacao(LocalDateTime.now());
        return repository.salvar(model);
    }

    @Override
    public void atualizar(ClienteModel model) throws SQLException {
        validarCampos(model);
        repository.atualizar(model);
    }

    private void validarCampos(ClienteModel model) throws SQLException {
        if (model.getNome().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }

        if (model.getCpfCnpj() != null && !model.getCpfCnpj().isBlank()) {
            var existente = clienteRepository.buscarPorCpfCnpj(model.getCpfCnpj());
            if (existente != null && !existente.getId().equals(model.getId())) {
                throw new IllegalArgumentException("CPF/CNPJ já cadastrado para outro cliente");
            }
        }

        if (!model.getEmail().isEmpty() && !isValidEmail(model.getEmail())) {
            throw new IllegalArgumentException("Formato de e-mail inválido");
        }
        if (!model.getCelular().isEmpty() && !isValidPhone(model.getCelular())) {
            throw new IllegalArgumentException("Telefone inválido (informe DDD + Número)");
        }
    }
}