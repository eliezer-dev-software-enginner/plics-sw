package plics_sw_new.screens.home;

import javafx.scene.paint.Color;
import megalodonte.base.components.IconInterface;
import megalodonte.components.Image;
import megalodonte.props.ImageProps;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

public class Components {
    public static IconInterface icon(Ikon ikon, double size, String color) {
        return IconInterface.of(FontIcon.of(ikon, (int) size, Color.web(color)));
    }

    public static IconInterface icon(String source, double size) {
        return IconInterface.of(new Image(source,new ImageProps().size(size)).getJavaFxNode());
    }
}
