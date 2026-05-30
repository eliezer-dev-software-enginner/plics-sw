package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import net.sf.persism.annotations.Table;

import java.sql.ResultSet;
import java.sql.SQLException;
@Setter
@Getter
@Table("licensas")
public class LicensaModel {
    private long id;
    private long data_criacao_millis;
        private String valor;
        private Long dataCriacao;
}
