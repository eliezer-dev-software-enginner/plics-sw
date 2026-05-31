package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

@Setter
@Getter
@Table("categorias")
public class CategoriaModel {

  @Column(primary = true)
  private Integer id;

  private LocalDateTime dataCriacao;
  private String nome;
}



