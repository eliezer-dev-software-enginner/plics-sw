package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.ClienteModel;
import my_app.db.repositories.ClienteRepository;
import net.sf.persism.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static my_app.utils.Utils.*;

public class ClienteService extends BaseService<ClienteModel> {

    private static final Logger log = LoggerFactory.getLogger(ClienteService.class);

    private final ClienteRepository clienteRepository;

    // produção
    public ClienteService() throws SQLException {
        this(DB.getPersismSession());
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
        // Nunca loga CPF/CNPJ, e-mail ou celular — só o suficiente pra identificar o
        // registro no log (id/nome), dado pessoal do cliente não precisa estar em disco.
        var salvo = repository.salvar(model);
        log.info("Cliente salvo: id={} nome={}", salvo.getId(), salvo.getNome());
        return salvo;
    }

    @Override
    public void atualizar(ClienteModel model) throws SQLException {
        validarCampos(model);
        repository.atualizar(model);
        log.info("Cliente atualizado: id={} nome={}", model.getId(), model.getNome());
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

        if (!model.getEmail().isEmpty() && isNotValidEmail(model.getEmail())) {
            throw new IllegalArgumentException("Formato de e-mail inválido");
        }
        if (!model.getCelular().isEmpty() && !isValidPhone(model.getCelular())) {
            throw new IllegalArgumentException("Telefone inválido (informe DDD + Número)");
        }
    }

    public List<ClienteModel> listarNovosPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        return clienteRepository.listarNovosPorPeriodo(dataInicio, dataFim);
    }
}