package my_app.db.repositories;

import my_app.db.dto.PreferenciasDto;
import my_app.db.models.PreferenciasModel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PreferenciasRepository extends BaseRepository<PreferenciasDto, PreferenciasModel> {

    public PreferenciasModel salvar(PreferenciasDto dto) throws SQLException {
        throw new RuntimeException("Método não é necessário!");
    }

    public List<PreferenciasModel> listar() throws SQLException {
        var lista = new ArrayList<PreferenciasModel>();
        try (Statement st = conn().createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM preferencias");
            while (rs.next()) lista.add((PreferenciasModel) new PreferenciasModel().fromResultSet(rs));
        }
        return lista;
    }

    public void atualizar(PreferenciasModel model) throws SQLException {
        String sql = "UPDATE preferencias SET tema = ?, login = ?, senha = ?, credenciais_habilitadas = ?, primeiro_acesso = ? WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, model.tema);
            ps.setString(2, model.login);
            ps.setString(3, model.senha);
            ps.setInt(4, model.credenciaisHabilitadas);
            ps.setInt(5, model.primeiroAcesso);
            ps.setInt(6, 1);
            ps.executeUpdate();
        }
    }

    public void excluirById(Long id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM preferencias WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    protected PreferenciasModel buscarById(Long id) throws SQLException {
        String sql = "SELECT * FROM preferencias WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? (PreferenciasModel) new PreferenciasModel().fromResultSet(rs) : null;
        }
    }
}