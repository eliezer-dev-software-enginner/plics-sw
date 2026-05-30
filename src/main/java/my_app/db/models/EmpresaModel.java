package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

import java.time.LocalDateTime;

@Setter
@Getter
@Table("empresas")
public class EmpresaModel {

    @Column(primary = true)
    private Integer id;

    private String nome;
    private String cpfCnpj;

    @Column(name = "celular")
    private String telefone;

    @Column(name = "endereco_cep")
    private String cep;

    @Column(name = "endereco_cidade")
    private String cidade;

    @Column(name = "endereco_rua")
    private String rua;

    @Column(name = "endereco_bairro")
    private String bairro;

    @Column(name = "local_pagamento")
    private String localPagamento;

    @Column(name = "texto_responsabilidade")
    private String textoResponsabilidade;

    @Column(name = "texto_termo_de_servico")
    private String termoServico;

    @Column(name = "logomarca")
    private String logoMarca;

    private LocalDateTime dataCriacao;
}