package my_app.db.models_old;

import lombok.Getter;
import lombok.Setter;
import net.sf.persism.annotations.Table;

@Setter
@Getter
@Table("categorias")
public class CategoriaModelV2 {
  private int id;
  private String nome;
  private long data_criacao;
}



