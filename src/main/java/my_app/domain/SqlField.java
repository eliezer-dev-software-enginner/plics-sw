package my_app.domain;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SqlField {
    String name();   // nome no banco
    String type();   // tipo (string, long, etc)
}
