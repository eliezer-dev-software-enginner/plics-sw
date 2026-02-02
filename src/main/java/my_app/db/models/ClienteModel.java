package my_app.db.models;

import my_app.db.dto.ClienteDto;
import my_app.domain.ModelBase;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ClienteModel extends ModelBase<ClienteDto> {
    public Long id;
    public String nome;
    public String cpfCnpj;
    public String celular;
    public String email;
    public Long dataCriacao;

    public ClienteModel fromResultSet(ResultSet rs) throws SQLException {
        var model = new ClienteModel();
        model.id = rs.getLong("id");
        model.nome = rs.getString("nome");
        model.cpfCnpj = rs.getString("cpf_cnpj");
        model.email = rs.getString("email");
        model.celular = rs.getString("celular");
        model.dataCriacao = rs.getLong("data_criacao");
        return model;
    }

    @Override
    public ClienteModel fromIdAndDto(Long id, ClienteDto clienteDto) {
        var model = new ClienteModel();
        model.id = id;
        model.nome = clienteDto.nome();
        model.cpfCnpj = clienteDto.cnpj();
        model.email = clienteDto.email();
        model.celular = clienteDto.telefone();
        return model;
    }
}