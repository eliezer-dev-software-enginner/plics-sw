package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import my_app.domain.components.Components;
import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

import java.time.LocalDateTime;

@Setter
@Getter
@Table("clientes")
public class ClienteModel {

    @Column(primary = true)
    private Integer id;

    private LocalDateTime dataCriacao;

    private String nome;
    private String cpfCnpj;
    private String celular;
    private String email;
    @Column(name = "isPessoaFisica")
    private Boolean pessoaFisica;

    @Column(name="data_nascimento")
    private Long dataNascimento;
    private String observacao;

    @Column(name = "is_gestante")
    private Boolean gestante;

    @Column(name="data_nascimento_bebe")
    private Long dataNascimentoBebe;

    private String cep;
    private String uf;
    private String cidade;
    private String bairro;
    private String rua;
    private String numero;

    public Components.Endereco getEndereco(){
        return new Components.Endereco(
                getUf(),getCep(),getCidade(),getBairro(),getRua(),getNumero()
        );
    }

    public String getGestanteText(){
        if(gestante!=null && gestante)return "Sim";
        return "Não";
    }
}