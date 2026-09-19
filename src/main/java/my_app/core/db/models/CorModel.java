package my_app.core.db.models;

import lombok.Getter;
import lombok.Setter;
import my_app.core.Identifier;
import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

import java.time.LocalDateTime;

@Setter
@Getter
@Table("cores")
public class CorModel extends Identifier {

    private String nome;

    @Column(name = "dataCriacao")
    private LocalDateTime dataCriacao;
}
