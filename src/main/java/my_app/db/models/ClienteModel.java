package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import my_app.db.dto.ClienteDto;
import my_app.domain.ModelBase;
import my_app.domain.SqlField;
import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

import java.time.LocalDateTime;

@Setter
@Getter
@Table("clientes")
public class ClienteModel {

    @Column(primary = true)
    private Integer id;

    private LocalDateTime dataCriacao;

    private String nome;
    private String cpfCnpj;
    private String celular;
    private String email;
    @Column(name = "isPessoaFisica")
    private Boolean pessoaFisica;
}