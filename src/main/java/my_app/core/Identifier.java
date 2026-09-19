package my_app.core;

import lombok.Getter;
import lombok.Setter;
import net.sf.persism.annotations.Column;

@Getter
@Setter
public abstract class Identifier {
    @Column(primary = true)
    private Integer id;
}