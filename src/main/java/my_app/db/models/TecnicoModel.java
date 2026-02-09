package my_app.db.models;

import my_app.db.dto.TecnicoDto;
import my_app.domain.ModelBase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TecnicoModel extends ModelBase<TecnicoDto> {
    //TODO: adicionar mais pra frente id de quem criar esse tecnico: userId
    public String nome;

    @Override
    public Map<String,Map<String,String>> correlacionarFieldSqlComFielEmModel() {
        var map = getMapSqlFieldModelFieldBase();
        map.put("nome", Map.of("nome","string"));
        return map;
    }

    @Override
    public TecnicoModel fromIdAndDtoAndMillis(Long id, TecnicoDto tecnicoDto, long millis) {
        var model = (TecnicoModel) super.fromIdAndDtoAndMillis(id, tecnicoDto, millis);
        model.nome = tecnicoDto.nome();
        return model;
    }
}



