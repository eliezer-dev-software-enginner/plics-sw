package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

import java.time.LocalDateTime;

@Setter
@Getter
@Table("cores")
public class CorModel {

    @Column(primary = true)
    private Integer id;

    private String nome;

    @Column(name = "dataCriacao")
    private LocalDateTime dataCriacao;
}
