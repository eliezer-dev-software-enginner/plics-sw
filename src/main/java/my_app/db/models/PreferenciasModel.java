package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import my_app.db.dto.PreferenciasDto;
import my_app.domain.ModelBase;
import my_app.domain.SqlField;
import net.sf.persism.annotations.Table;
@Setter
@Getter
@Table("preferencias")
public class PreferenciasModel {
    private long id;
    private long data_criacao_millis;
    private String tema;
    private String login;
    private String senha;
    private Integer credenciaisHabilitadas;
    private Integer primeiroAcesso;


    public boolean isFirstAccess(){
        return primeiroAcesso != null && primeiroAcesso == 1;
    }
}