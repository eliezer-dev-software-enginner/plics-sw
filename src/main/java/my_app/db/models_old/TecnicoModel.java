package my_app.db.models_old;

import my_app.db.dto.TecnicoDto;
import my_app.domain.ModelBase;
import my_app.domain.SqlField;

public class TecnicoModel extends ModelBase<TecnicoDto> {
    //TODO: adicionar mais pra frente id de quem criar esse tecnico: userId

    @SqlField(name = "nome", type = "string")
    public String nome;

    @Override
    public TecnicoModel fromIdAndDtoAndMillis(Long id, TecnicoDto tecnicoDto, long millis) {
        this.id = id;
        this.dataCriacao = millis;
        this.nome = tecnicoDto.nome();
        this.dataCriacao = millis;
        return this;
    }
}



