package my_app.core.db.models;

import lombok.Getter;
import lombok.Setter;
import my_app.core.Identifier;
import net.sf.persism.annotations.Table;

import java.time.LocalDateTime;

@Setter
@Getter
@Table("categorias")
public class CategoriaModel extends Identifier {
  private LocalDateTime dataCriacao;
  private String nome;
}



