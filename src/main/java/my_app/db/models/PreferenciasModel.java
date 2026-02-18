package my_app.db.models;

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
        var model = (PreferenciasModel) super.fromIdAndDtoAndMillis(id, dto, millis);
        model.tema = dto.tema();
        model.login = dto.login();
        model.senha = dto.senha();
        model.credenciaisHabilitadas = dto.credentiaisHabilitadas();
        model.primeiroAcesso = dto.primeiroAcesso();
        return model;
    }
}