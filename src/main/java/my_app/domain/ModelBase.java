package my_app.domain;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class ModelBase<Dto> {
    public Long id;
    public Long dataCriacao;

    abstract public ModelBase<?> fromResultSet(ResultSet queryResultSet) throws SQLException;
    abstract public ModelBase<?> fromIdAndDto(Long id, Dto dto);
}
