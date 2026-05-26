package my_app.db.models;

import my_app.db.dto.CategoriaDto;
import my_app.db.dto.FornecedorDto;
import my_app.domain.ModelBase;
import my_app.domain.SqlField;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CategoriaModel extends ModelBase<CategoriaDto> {
    @SqlField(name = "nome", type = "string")
    public String nome;

    @Override
    public CategoriaModel fromIdAndDtoAndMillis(Long id, CategoriaDto dto, long millis) {
        this.id = id;
        this.dataCriacao = millis;
        this.nome = dto.nome();
        return this;
    }
}



