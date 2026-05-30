package my_app.db.models_old;

import my_app.db.dto.PreferenciasDto;
import my_app.domain.ModelBase;
import my_app.domain.SqlField;

public class PreferenciasModel extends ModelBase<PreferenciasDto> {
    @SqlField(name = "tema", type = "string")
    public String tema;
    @SqlField(name = "login", type = "string")
    public String login;
    @SqlField(name = "senha", type = "string")
    public String senha;
    @SqlField(name = "credenciais_habilitadas", type = "int")
    public Integer credenciaisHabilitadas;
    @SqlField(name = "primeiro_acesso", type = "int")
    public Integer primeiroAcesso;

    @Override
    public PreferenciasModel fromIdAndDtoAndMillis(Long id, PreferenciasDto dto, long millis) {
        this.id = id;
        this.dataCriacao = millis;
        this.tema = dto.tema();
        this.login = dto.login();
        this.senha = dto.senha();
        this.credenciaisHabilitadas = dto.credentiaisHabilitadas();
        this.primeiroAcesso = dto.primeiroAcesso();
        return this;
    }

    public boolean isFirstAccess(){
        return primeiroAcesso != null && primeiroAcesso == 1;
    }
}