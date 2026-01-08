package my_app.db.repositories;

import my_app.db.DB;
import my_app.db.models.CategoriaModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioRepository {

    private Connection conn() throws SQLException {
        return DB.getInstance().connection();
    }

    // CREATE
    public CategoriaModel salvar(CategoriaModel model) throws SQLException {
        String sql = """
        INSERT INTO categoria 
        (nome, data_criacao) VALUES (?,?)
        """;

        long dataCriacao = model.dataCriacao != null ? model.dataCriacao : System.currentTimeMillis();

        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, model.nome);
            ps.setLong(2, dataCriacao);
            ps.executeUpdate();
            
            // Recupera o ID gerado
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    model.id = generatedKeys.getLong(1);
                    model.dataCriacao = dataCriacao;
                }
            }
        }
        return model;
    }

    public List<CategoriaModel> listar() throws SQLException {
        List<CategoriaModel> lista = new ArrayList<>();
        try (Statement st = conn().createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM categoria");
            while (rs.next()) lista.add(CategoriaModel.fromResultSet(rs));
        }
        return lista;
    }

    // UPDATE
    public void atualizar(CategoriaModel model) throws SQLException {
        String sql = """
        UPDATE categoria SET nome = ? WHERE id = ?
        """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, model.nome);
            ps.setLong(2, model.id);
            ps.executeUpdate();
        }
    }

    public void excluir(Long id) throws SQLException {
        try (PreparedStatement ps =
                     conn().prepareStatement("DELETE FROM categoria WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public CategoriaModel buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM categoria WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? CategoriaModel.fromResultSet(rs) : null;
        }
    }
}

