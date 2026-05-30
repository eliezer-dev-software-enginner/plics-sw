package my_app.db.repositories_old;

import my_app.db.dto.CategoriaDto;
import my_app.db.models_old.CategoriaModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaRepository extends BaseRepository<CategoriaDto, CategoriaModel> {
    public CategoriaModel salvar(CategoriaDto dto) throws SQLException {
        String sql = """
        INSERT INTO categoria
        (nome, data_criacao) VALUES (?,?)
        """;

        long millis =  System.currentTimeMillis();

        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dto.nome());
            ps.setLong(2, millis);
            ps.executeUpdate();
            
            // Recupera o ID gerado e cria nova instância
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long idGerado = generatedKeys.getLong(1);
                    return new CategoriaModel().fromIdAndDtoAndMillis(idGerado,dto, millis);
                }
            }
        }
        throw new SQLException("Falha ao salvar Categoria e recuperar ID gerado");
    }

    public List<CategoriaModel> listar() throws SQLException {
        List<CategoriaModel> lista = new ArrayList<>();
        try (Statement st = conn().createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM categoria");
            while (rs.next()) lista.add((CategoriaModel) new CategoriaModel().fromResultSet(rs));
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

    public void excluirById(Long id) throws SQLException {
        try (PreparedStatement ps =
                     conn().prepareStatement("DELETE FROM categoria WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    protected CategoriaModel buscarById(Long id) throws SQLException {
        String sql = "SELECT * FROM categoria WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? (CategoriaModel) new CategoriaModel().fromResultSet(rs) : null;
        }
    }
}

