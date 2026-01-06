package my_app.db.models;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CategoriaModel {
    public Long id;
    public String nome;
    public Long dataCriacao;

    public CategoriaModel(){}

    public CategoriaModel(Long id, String nome, Long dataCriacao){
        this.id = id;
        this(nome,dataCriacao);
    }

    public CategoriaModel(String nome, Long dataCriacao){
        this.nome = nome;
        this.dataCriacao = dataCriacao;
    }

    public static CategoriaModel fromResultSet(ResultSet rs) throws SQLException {
        var model  = new CategoriaModel();
        model.id = rs.getLong("id");
        model.nome = rs.getString("nome");
       return model;
    }
}



