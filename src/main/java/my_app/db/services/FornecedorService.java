package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.FornecedorModel;
import my_app.db.repositories.FornecedorRepository;
import net.sf.persism.Session;

import java.sql.SQLException;
import java.time.LocalDateTime;

import static my_app.utils.Utils.*;

public class FornecedorService extends BaseService<FornecedorModel> {

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
        return repository.salvar(model);
    }

    @Override
    public void atualizar(FornecedorModel model) throws SQLException {
        validar(model, model.getId());
        repository.atualizar(model);
    }

    private void validar(FornecedorModel model, Integer idAtual) throws SQLException {
        String nome = model.getNome();
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome é obrigatório");

        String cnpj = model.getCpfCnpj();
        if (cnpj != null && !cnpj.isBlank() && !isValidCnpj(cnpj))
            throw new IllegalArgumentException("CNPJ inválido (deve conter 14 dígitos)");

        if (cnpj != null && !cnpj.isBlank()) {
            var existente = fornecedorRepository.buscarPorCpfCnpj(cnpj);
            if (existente != null && !existente.getId().equals(idAtual))
                throw new IllegalArgumentException("Já existe um fornecedor com este CNPJ/CPF");
        }

        String email = model.getEmail();
        if (email != null && !email.isBlank() && !isValidEmail(email))
            throw new IllegalArgumentException("Formato de e-mail inválido");

        String celular = model.getCelular();
        if (celular != null && !celular.isBlank() && !isValidPhone(celular))
            throw new IllegalArgumentException("Telefone inválido (informe DDD + Número)");
    }
}
