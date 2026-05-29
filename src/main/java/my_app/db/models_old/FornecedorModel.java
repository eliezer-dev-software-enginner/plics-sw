package my_app.db.models_old;

import my_app.db.dto.FornecedorDto;
import my_app.domain.ModelBase;
import my_app.domain.SqlField;

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
        this.id = id;
        this.dataCriacao = millis;
        this.nome = dto.nome();
        this.cpfCnpj = dto.cpfCnpj();
        this.celular = dto.celular();
        this.email = dto.email();
        this.inscricaoEstadual = dto.inscricaoEstadual();
        this.ufSelected = dto.ufSelected();
        this.cidade = dto.cidade();
        this.bairro = dto.bairro();
        this.rua = dto.rua();
        this.numero = dto.numero();
        this.observacao = dto.observacao();
        return this;
    }
}