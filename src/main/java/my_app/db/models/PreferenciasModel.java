package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

@Setter
@Getter
@Table("preferencias")
public class PreferenciasModel {

    @Column(primary = true)
    private Integer id;

    @Column(name = "dataCriacao")
    private long dataCriacaoMillis;

    private String tema;
    private String login;
    private String senha;

    @Column(name = "credenciais_habilitadas")
    private Integer credenciaisHabilitadas;

    @Column(name = "primeiro_acesso")
    private Integer primeiroAcesso;

    public boolean isFirstAccess() {
        return primeiroAcesso != null && primeiroAcesso == 1;
    }
}