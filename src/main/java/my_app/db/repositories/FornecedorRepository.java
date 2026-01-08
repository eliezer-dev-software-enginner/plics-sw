package my_app.db.repositories;

import my_app.db.DB;
import my_app.db.dto.FornecedorDto;
import my_app.db.models.FornecedorModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FornecedorRepository {

    private Connection conn() throws SQLException {
        return DB.getInstance().connection();
    }

    // CREATE
    public FornecedorModel salvar(FornecedorDto dto) throws SQLException {
        String sql = """
        INSERT INTO fornecedor 
        (nome, cpfCnpj, data_criacao) VALUES (?,?,?)
        """;

        long dataCriacao = dto.dataCriacao() != null ? dto.dataCriacao() : System.currentTimeMillis();

        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dto.nome());
            ps.setString(2, dto.cnpj());
            ps.setLong(3, dataCriacao);
            ps.executeUpdate();
            
            // Recupera o ID gerado e cria nova instância
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long idGerado = generatedKeys.getLong(1);
                    return new FornecedorModel(idGerado, dto.nome(), dto.cnpj(), dataCriacao);
                }
            }
        }
        throw new SQLException("Falha ao recuperar ID gerado");
    }

    public List<FornecedorModel> listar() throws SQLException {
        List<FornecedorModel> lista = new ArrayList<>();
        try (Statement st = conn().createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM fornecedor");
            while (rs.next()) lista.add(FornecedorModel.fromResultSet(rs));
        }
        return lista;
    }

    // UPDATE
    public void atualizar(FornecedorModel model) throws SQLException {
        String sql = """
        UPDATE fornecedor SET nome = ?, cpfCnpj = ? WHERE id = ?
        """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, model.nome);
            ps.setString(2, model.cpfCnpj);
            ps.setLong(3, model.id);
            ps.executeUpdate();
        }
    }

    public void excluir(Long id) throws SQLException {
        try (PreparedStatement ps =
                     conn().prepareStatement("DELETE FROM fornecedor WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public FornecedorModel buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM fornecedor WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? FornecedorModel.fromResultSet(rs) : null;
        }
    }
}