package my_app.db.models;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FornecedorModel {
    public Long id;
    public String nome;
    public String cpfCnpj;
    public Long dataCriacao;

    public FornecedorModel() {}

    public FornecedorModel(Long id, String nome, String cpfCnpj, Long dataCriacao) {
        this.id = id;
        this.nome = nome;
        this.cpfCnpj = cpfCnpj;
        this.dataCriacao = dataCriacao;
    }

    public static FornecedorModel fromResultSet(ResultSet rs) throws SQLException {
        var model = new FornecedorModel();
        model.id = rs.getLong("id");
        model.nome = rs.getString("nome");
        model.cpfCnpj = rs.getString("cpfCnpj");
        model.dataCriacao = rs.getLong("data_criacao");
        return model;
    }
}