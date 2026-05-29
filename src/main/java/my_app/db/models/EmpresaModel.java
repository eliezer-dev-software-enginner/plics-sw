package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import net.sf.persism.annotations.Table;

import java.sql.ResultSet;
import java.sql.SQLException;
@Setter
@Getter
@Table("empresas")
public class EmpresaModel {
    private long id;
    private long data_criacao_millis;
    private String nome;
    private String cpfCnpj;
    private String telefone;
    private String cep;
    private String cidade;
    private String rua;
    private String bairro;
    private String localPagamento;
    private String textoResponsabilidade;
    private String termoServico;
    private String logoMarca;
    private Long dataCriacao;
}