package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import my_app.db.dto.FornecedorDto;
import my_app.domain.ModelBase;
import my_app.domain.SqlField;
import net.sf.persism.annotations.Table;
@Setter
@Getter
@Table("fornecedores")
public class FornecedorModel {
    private long id;
    private long data_criacao_millis;
    private String nome;
    private String cpfCnpj;
    private String celular;
    private String inscricaoEstadual;
    private String email;
    private String ufSelected;
    private String cidade;
    private String bairro;
    private String rua;
    private String numero;
    private String observacao;
}