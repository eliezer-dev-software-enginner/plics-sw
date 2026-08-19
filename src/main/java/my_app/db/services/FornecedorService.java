package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.FornecedorModel;
import my_app.db.repositories.FornecedorRepository;
import net.sf.persism.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static my_app.utils.Utils.*;

public class FornecedorService extends BaseService<FornecedorModel> {

    private static final Logger log = LoggerFactory.getLogger(FornecedorService.class);

    private final FornecedorRepository fornecedorRepository;

    public FornecedorService() throws SQLException {
        this(DB.getPersismSession());
    }

    public FornecedorService(Session session) {
        super(new FornecedorRepository(session));
        this.fornecedorRepository = (FornecedorRepository) repository;
    }

    @Override
    public FornecedorModel salvar(FornecedorModel model) throws SQLException {
        validar(model, null);
        model.setDataCriacao(LocalDateTime.now());
        // Nunca loga CPF/CNPJ, e-mail ou celular — mesmo critério de ClienteService.
        var salvo = repository.salvar(model);
        log.info("Fornecedor salvo: id={} nome={}", salvo.getId(), salvo.getNome());
        return salvo;
    }

    @Override
    public void atualizar(FornecedorModel model) throws SQLException {
        validar(model, model.getId());
        repository.atualizar(model);
        log.info("Fornecedor atualizado: id={} nome={}", model.getId(), model.getNome());
    }

    private void validar(FornecedorModel model, Integer idAtual) throws SQLException {
        String nome = model.getNome();
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome é obrigatório");

        String cpfCnpj = model.getCpfCnpj();
        if (cpfCnpj != null && !cpfCnpj.isBlank()) {
            Boolean pessoaFisica = model.getPessoaFisica();
            if (pessoaFisica == null) {
                if (!isValidCpf(cpfCnpj) && !isValidCnpj(cpfCnpj))
                    throw new IllegalArgumentException("CPF/CNPJ inválido (deve conter 11 ou 14 dígitos)");
            } else if (pessoaFisica) {
                if (!isValidCpf(cpfCnpj))
                    throw new IllegalArgumentException("CPF inválido");
            } else {
                if (!isValidCnpj(cpfCnpj))
                    throw new IllegalArgumentException("CNPJ inválido");
            }
        }

        if (cpfCnpj != null && !cpfCnpj.isBlank()) {
            var existente = fornecedorRepository.buscarPorCpfCnpj(cpfCnpj);
            if (existente != null && !existente.getId().equals(idAtual))
                throw new IllegalArgumentException("Já existe um fornecedor com este CNPJ/CPF");
        }

        String email = model.getEmail();
        if (email != null && !email.isBlank() && isNotValidEmail(email))
            throw new IllegalArgumentException("Formato de e-mail inválido");

        String celular = model.getCelular();
        if (celular != null && !celular.isBlank() && !isValidPhone(celular))
            throw new IllegalArgumentException("Telefone inválido (informe DDD + Número)");
    }

    public List<FornecedorModel> listarNovosPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        return fornecedorRepository.listarNovosPorPeriodo(dataInicio, dataFim);
    }
}
