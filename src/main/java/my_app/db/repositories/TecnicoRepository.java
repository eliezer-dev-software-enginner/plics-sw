package my_app.db.repositories;

import my_app.db.dto.TecnicoDto;
import my_app.db.models.TecnicoModel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TecnicoRepository extends BaseRepository<TecnicoDto, TecnicoModel> {
    public TecnicoModel salvar(TecnicoDto dto) throws SQLException {
        String sql = """
        INSERT INTO tecnicos
        (nome, data_criacao) VALUES (?,?)
        """;

        long dataCriacao =  System.currentTimeMillis();

        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dto.nome());
            ps.setLong(2, dataCriacao);
            ps.executeUpdate();
            
            // Recupera o ID gerado e cria nova instância
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long idGerado = generatedKeys.getLong(1);
                    return new TecnicoModel().fromIdAndDtoAndMillis(idGerado, dto, dataCriacao);
                }
            }
        }
        throw new SQLException("Falha ao recuperar ID gerado");
    }

    public List<TecnicoModel> listar() throws SQLException {
        List<TecnicoModel> lista = new ArrayList<>();
        try (Statement st = conn().createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM tecnicos");
            while (rs.next()) lista.add((TecnicoModel) new TecnicoModel().fromResultSet(rs));
        }
        return lista;
    }

    // UPDATE
    public void atualizar(TecnicoModel model) throws SQLException {
        String sql = """
        UPDATE tecnicos SET nome = ? WHERE id = ?
        """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, model.nome);
            ps.setLong(2, model.id);
            ps.executeUpdate();
        }
    }

    public void excluirById(Long id) throws SQLException {
        try (PreparedStatement ps =
                     conn().prepareStatement("DELETE FROM tecnicos WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    protected TecnicoModel buscarById(Long id) throws SQLException {
        String sql = "SELECT * FROM tecnicos WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? (TecnicoModel) new TecnicoModel().fromResultSet(rs) : null;
        }
    }
}

