package my_app.db.models;

import my_app.db.dto.FornecedorDto;
import my_app.domain.ModelBase;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FornecedorModel extends ModelBase<FornecedorDto> {
    public Long id;
    public String nome;
    public String cpfCnpj;
    public Long dataCriacao;
    public String celular;
    public String inscricaoEstadual;
    public String email;
    public String ufSelected;
    public String cidade;
    public String bairro;
    public String rua;
    public String numero;
    public String observacao;

    public FornecedorModel() {}

    public FornecedorModel fromResultSet(ResultSet rs) throws SQLException {
        var model = new FornecedorModel();
        model.id = rs.getLong("id");
        model.nome = rs.getString("nome");
        model.cpfCnpj = rs.getString("cpf_cnpj");
        model.dataCriacao = rs.getLong("data_criacao");
        model.celular = rs.getString("celular");
        model.inscricaoEstadual = rs.getString("inscricao_estadual");
        model.email = rs.getString("email");
        model.ufSelected = rs.getString("uf_selected");
        model.cidade = rs.getString("cidade");
        model.bairro = rs.getString("bairro");
        model.rua = rs.getString("rua");
        model.numero = rs.getString("numero");
        model.observacao = rs.getString("observacao");
        return model;
    }

    @Override
    public FornecedorModel fromIdAndDto(Long id, FornecedorDto dto) {
        var model = new FornecedorModel();
        model.id = id;
        model.nome = dto.nome();
        model.cpfCnpj = dto.cpfCnpj();
        model.celular = dto.celular();
        model.email = dto.email();
        model.inscricaoEstadual = dto.inscricaoEstadual();
        model.ufSelected = dto.ufSelected();
        model.cidade = dto.cidade();
        model.bairro = dto.bairro();
        model.rua = dto.rua();
        model.numero = dto.numero();
        model.observacao = dto.observacao();
        return model;
    }
}