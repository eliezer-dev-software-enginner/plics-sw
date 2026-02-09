package my_app.db.models;

import my_app.db.dto.TecnicoDto;
import my_app.domain.ModelBase;
import my_app.domain.SqlField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TecnicoModel extends ModelBase<TecnicoDto> {
    //TODO: adicionar mais pra frente id de quem criar esse tecnico: userId

    @SqlField(name = "nome", type = "string")
    public String nome;

    @Override
    public TecnicoModel fromIdAndDtoAndMillis(Long id, TecnicoDto tecnicoDto, long millis) {
        var model = (TecnicoModel) super.fromIdAndDtoAndMillis(id, tecnicoDto, millis);
        model.nome = tecnicoDto.nome();
        return model;
    }
}



