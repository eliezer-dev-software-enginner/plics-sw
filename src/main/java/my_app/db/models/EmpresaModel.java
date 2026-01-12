package my_app.db.models;

import my_app.db.dto.EmpresaDto;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EmpresaModel {
    public Long id;
    public String nome;
    public String cpfCnpj;
    public String telefone;
    public String cep;
    public String cidade;
    public String rua;
    public String bairro;
    public String localPagamento;
    public String textoResponsabilidade;
    public String termoServico;
    public Long dataCriacao;

    public EmpresaModel() {}


    public static EmpresaModel fromResultSet(ResultSet rs) throws SQLException {
        var model = new EmpresaModel();

        model.id = rs.getLong("id");
        model.nome = rs.getString("nome");
        model.cpfCnpj = rs.getString("cpfCnpj");
        model.telefone = rs.getString("celular");
        model.cep = rs.getString("endereco_cep");
        model.cidade = rs.getString("endereco_cidade");
        model.rua = rs.getString("endereco_rua");
        model.bairro = rs.getString("endereco_bairro");
        model.localPagamento = rs.getString("local_pagamento");
        model.textoResponsabilidade = rs.getString("texto_responsabilidade");
        model.termoServico = rs.getString("texto_termo_de_servico");

        model.dataCriacao = rs.getLong("data_criacao");
        return model;
    }
}