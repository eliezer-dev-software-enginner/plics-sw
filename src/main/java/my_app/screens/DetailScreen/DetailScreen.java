package my_app.screens.DetailScreen;

import megalodonte.*;
import megalodonte.components.Column;
import megalodonte.components.Component;
import megalodonte.components.Text;

public class DetailScreen {
    Router router;

    public DetailScreen(Router router) {
    }

    public Component render (){
        return new Column()
                .child(new Text("Detail screen", new TextProps().fontSize(30))
                );
    }
}
