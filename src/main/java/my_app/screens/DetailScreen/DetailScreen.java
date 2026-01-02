package my_app.screens.DetailScreen;

import megalodonte.*;
import megalodonte.components.Column;
import megalodonte.components.Component;
import megalodonte.components.Text;
import megalodonte.router.Router;

public class DetailScreen {
    Router router;

    public DetailScreen(Router router) {
    }

    public Component render (){
        return new Column()
                .c_child(new Text("Detail screen", new TextProps().fontSize(30))
                );
    }
}
