package my_app.db.repositories_old;

import my_app.db.dto.ClienteDto;
import my_app.db.models_old.ClienteModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteRepository extends BaseRepository<ClienteDto,ClienteModel> {

    @Override
    public ClienteModel salvar(ClienteDto dto) throws SQLException {
        String sql = """
        INSERT INTO clientes
        (nome, cpf_cnpj, celular, data_criacao, email) VALUES (?,?,?,?,?)
    """;
        long dataMillis = System.currentTimeMillis();
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dto.nome());
            ps.setString(2, dto.cnpj());
            ps.setString(3, dto.telefone());
            ps.setLong(4, dataMillis);
            ps.setString(5, dto.email());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long idGerado = generatedKeys.getLong(1);
                    return new ClienteModel().fromIdAndDtoAndMillis(idGerado, dto, dataMillis);
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed: clientes.cpf_cnpj")) {
                throw new SQLException("Já existe um cliente cadastrado com este CPF/CNPJ");
            }
            throw e;
        }
        throw new SQLException("Falha ao recuperar ID gerado");
    }

    @Override
    public List<ClienteModel> listar() throws SQLException {
        List<ClienteModel> lista = new ArrayList<>();
        try (Statement st = conn().createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM clientes");
            while (rs.next()) lista.add((ClienteModel) new ClienteModel().fromResultSet(rs));
        }
        return lista;
    }


   //TODO: atualizar demais campos
    @Override
    public void atualizar(ClienteModel model) throws SQLException {
        String sql = """
        UPDATE clientes SET nome = ?, cpf_cnpj = ?, email = ?, celular = ? WHERE id = ?
        """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, model.nome);
            ps.setString(2, model.cpfCnpj);
            ps.setString(3, model.email);
            ps.setString(4, model.celular);
            ps.setLong(5, model.id);
            ps.executeUpdate();
        }
    }

    @Override
    public void excluirById(Long id) throws SQLException {
        try (PreparedStatement ps =
                     conn().prepareStatement("DELETE FROM clientes WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public ClienteModel buscarById(Long id) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? (ClienteModel) new ClienteModel().fromResultSet(rs) : null;
        }
    }
}