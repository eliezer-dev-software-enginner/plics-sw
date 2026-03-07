package my_app.db.models;

import my_app.db.dto.FornecedorDto;
import my_app.domain.ModelBase;
import my_app.domain.SqlField;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FornecedorModel extends ModelBase<FornecedorDto> {
    @SqlField(name = "nome", type = "string")
    public String nome;

    @SqlField(name = "cpf_cnpj", type = "string")
    public String cpfCnpj;

    @SqlField(name = "celular", type = "string")
    public String celular;

    @SqlField(name = "inscricao_estadual", type = "string")
    public String inscricaoEstadual;

    @SqlField(name = "email", type = "string")
    public String email;

    @SqlField(name = "uf_selected", type = "string")
    public String ufSelected;

    @SqlField(name = "cidade", type = "string")
    public String cidade;

    @SqlField(name = "bairro", type = "string")
    public String bairro;

    @SqlField(name = "rua", type = "string")
    public String rua;

    @SqlField(name = "numero", type = "string")
    public String numero;

    @SqlField(name = "observacao", type = "string")
    public String observacao;


    @Override
    public FornecedorModel fromIdAndDtoAndMillis(Long id, FornecedorDto dto, long millis) {
        var model = (FornecedorModel) super.fromIdAndDtoAndMillis(id, dto, millis);
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