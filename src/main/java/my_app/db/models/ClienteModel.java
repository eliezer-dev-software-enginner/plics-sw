package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import my_app.db.dto.ClienteDto;
import my_app.domain.ModelBase;
import my_app.domain.SqlField;
import net.sf.persism.annotations.Table;
@Setter
@Getter
@Table("clientes")
public class ClienteModel {
    private long id;
    private long data_criacao_millis;
    private String nome;
    private String cpfCnpj;
    private String celular;
    private String email;
}