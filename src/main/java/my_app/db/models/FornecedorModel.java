package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import my_app.domain.components.Components;
import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

import java.time.LocalDateTime;

@Setter
@Getter
@Table("fornecedores")
public class FornecedorModel {

    @Column(primary = true)
    private Integer id;

    private LocalDateTime dataCriacao;

    private String nome;
    private String cpfCnpj;
    private String celular;

    @Column(name = "inscricao_estadual")
    private String inscricaoEstadual;

    private String email;

    @Column(name = "uf_selected")
    private String ufSelected;

    private String cidade;
    private String bairro;
    private String rua;
    private String numero;
    private String cep;
    private String observacao;

    public Components.Endereco getEndereco(){
        return new Components.Endereco(
                getUfSelected(),getCep(),getCidade(),getBairro(),getRua(),getNumero()
        );
    }
}