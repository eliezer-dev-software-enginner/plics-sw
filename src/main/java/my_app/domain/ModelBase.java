package my_app.domain;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class ModelBase<Dto> {
    public Long id;
    public Long dataCriacao;//TODO: mudar para createdMillis

    private Map<String,Map<String,String>> mapSqlFieldDtoField;

    public Map<String, Map<String,String>> correlacionarFieldSqlComFielEmModel(){
       throw new RuntimeException("Not implemented");
    }

   // abstract public ModelBase<?> fromResultSet(ResultSet queryResultSet) throws SQLException;
    @Deprecated
    //TODO: marcar como protected
     public ModelBase<?> fromIdAndDto(Long id, Dto dto){
         this.id = id;
         return this;
    }

    public ModelBase<?> fromIdAndDtoAndMillis(Long id, Dto dto, long millis){
        this.id = id;
        this.dataCriacao = millis;
        return this;
    }

    public Map<String, Map<String,String>> getMapSqlFieldModelFieldBase() {
        if(mapSqlFieldDtoField == null){
            mapSqlFieldDtoField = new HashMap<>();
            mapSqlFieldDtoField.put("id", Map.of("id", "long"));
            mapSqlFieldDtoField.put("data_criacao",Map.of("dataCriacao","long"));
        }

        return mapSqlFieldDtoField;
    }

    protected void setField(String fieldName, Object value) {
        try {
            var field = this.getClass().getField(fieldName);
            field.set(this, value);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao setar campo: " + fieldName, e);
        }
    }

    protected Object getValueFromResultSet(ResultSet rs, String sqlField, String type) throws SQLException {
        return switch (type) {
            case "long" -> rs.getLong(sqlField);
            case "string" -> rs.getString(sqlField);
            case "int" -> rs.getInt(sqlField);
            case "double" -> rs.getDouble(sqlField);
            case "boolean" -> rs.getBoolean(sqlField);
            default -> throw new RuntimeException("Tipo não suportado: " + type);
        };
    }

    public ModelBase<?> fromResultSet(ResultSet rs) throws SQLException {
        var map = correlacionarFieldSqlComFielEmModel();

        for (var entry : map.entrySet()) {
            String sqlField = entry.getKey();
            var modelInfo = entry.getValue();

            for (var modelEntry : modelInfo.entrySet()) {
                String modelField = modelEntry.getKey();
                String type = modelEntry.getValue();

                Object value = getValueFromResultSet(rs, sqlField, type);
                setField(modelField, value);
            }
        }

        return this;
    }
}
