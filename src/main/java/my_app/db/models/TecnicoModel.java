package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import my_app.db.dto.TecnicoDto;
import my_app.domain.ModelBase;
import my_app.domain.SqlField;
import net.sf.persism.annotations.Table;

@Setter
@Getter
@Table("tecnicos")
public class TecnicoModel {
    private long id;
    private long data_criacao_millis;
    private String nome;
}



