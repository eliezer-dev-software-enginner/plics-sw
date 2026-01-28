package my_app.db.repositories;

import my_app.db.dto.FornecedorDto;
import my_app.db.models.FornecedorModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FornecedorRepository extends BaseRepository<FornecedorDto, FornecedorModel> {

    public FornecedorModel salvar(FornecedorDto dto) throws SQLException {
        String sql = """
    INSERT INTO fornecedores
    (nome, cpf_cnpj, celular, email, inscricao_estadual, uf_selected,
     cidade, bairro, rua, numero, observacao, data_criacao)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dto.nome());
            ps.setString(2, dto.cpfCnpj()); // Corrigido de dto.cnpj() para dto.cpfCnpj()
            ps.setString(3, dto.celular());
            ps.setString(4, dto.email());
            ps.setString(5, dto.inscricaoEstadual());
            ps.setString(6, dto.ufSelected());
            ps.setString(7, dto.cidade());
            ps.setString(8, dto.bairro());
            ps.setString(9, dto.rua());
            ps.setString(10, dto.numero());
            ps.setString(11, dto.observacao());
            ps.setLong(12, System.currentTimeMillis());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long idGerado = generatedKeys.getLong(1);
                    return new FornecedorModel().fromIdAndDto(idGerado, dto);
                }
            }
        }
        throw new SQLException("Falha ao salvar fornecedor e recuperar ID gerado");
    }

    @Override
    public void excluirById(Long id) throws SQLException {
        try (PreparedStatement ps =
                     conn().prepareStatement("DELETE FROM fornecedores WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public FornecedorModel buscarById(Long id) throws SQLException {
        String sql = "SELECT * FROM fornecedores WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? new FornecedorModel().fromResultSet(rs) : null;
        }
    }


    public List<FornecedorModel> listar() throws SQLException {
        List<FornecedorModel> lista = new ArrayList<>();
        try (Statement st = conn().createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM fornecedores");
            while (rs.next()) lista.add(new FornecedorModel().fromResultSet(rs));
        }
        return lista;
    }

    // UPDATE
    @Override
    public void atualizar(FornecedorModel model) throws SQLException {
        String sql = """
        UPDATE fornecedores SET 
            nome = ?, 
            cpf_cnpj = ?, 
            celular = ?, 
            email = ?, 
            inscricao_estadual = ?, 
            uf_selected = ?,
            cidade = ?, 
            bairro = ?, 
            rua = ?, 
            numero = ?, 
            observacao = ?
        WHERE id = ?
        """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, model.nome);
            ps.setString(2, model.cpfCnpj);
            ps.setString(3, model.celular);
            ps.setString(4, model.email);
            ps.setString(5, model.inscricaoEstadual);
            ps.setString(6, model.ufSelected);
            ps.setString(7, model.cidade);
            ps.setString(8, model.bairro);
            ps.setString(9, model.rua);
            ps.setString(10, model.numero);
            ps.setString(11, model.observacao);
            ps.setLong(12, model.id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Falha ao atualizar: Fornecedor com ID " + model.id + " não encontrado.");
            }
        }
    }
}