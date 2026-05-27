package my_app.db.models_old;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LicensaModel {
        public Long id;
        public String valor;

        public Long dataCriacao;

        public static LicensaModel fromResultSet(ResultSet rs) throws SQLException {
            var p = new LicensaModel();
            p.id = rs.getLong("id");
            p.valor = rs.getString("valor");
            return p;
        }
}
