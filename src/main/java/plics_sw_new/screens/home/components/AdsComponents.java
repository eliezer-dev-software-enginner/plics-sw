package plics_sw_new.screens.home.components;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import megalodonte.ComputedState;
import megalodonte.ForEachState;
import megalodonte.base.Redirect;
import megalodonte.base.components.Component;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Row;
import megalodonte.props.CardProps;
import megalodonte.props.ImageProps;
import megalodonte.props.RowProps;
import megalodonte.v2.ListState;
import megalodonte.v2.Show;
import plics_sw_new.screens.home.AdsScraper;

public class AdsComponents {
    private final ListState<AdsScraper.Ad> adListState;
    private final ComputedState<Boolean> adsLoaded;

    private record LoadedAd(AdsScraper.Ad ad, Image image) { }

    public AdsComponents(ListState<AdsScraper.Ad> adListState, ComputedState<Boolean> adsLoaded){
        this.adListState = adListState;
        this.adsLoaded = adsLoaded;
    }

    public Component DestaqueRow(){
        return Show.when(adsLoaded, () -> new Row(new RowProps().spacingOf(10).fillWidth()).children(
                new Column().children(
                        Component.CreateFromJavaFxNode(heading("Em destaque", "Confira esta oportunidade")),
                        new Row().items(
                                ForEachState.of(adListState.filter(AdsScraper.Ad::urgent), AdsComponents::card),
                                10
                        )
                )
        ));
    }

    private static VBox heading(String label, String subtitle) {
        Label title=new Label(label);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #18324d;");
        Label detail=new Label(subtitle);
        detail.setStyle("-fx-font-size: 13px; -fx-text-fill: #53657e;");
        return new VBox(3,title,detail);
    }

    private static Component card(AdsScraper.Ad ad) {
        boolean isSquare = ad.bannerType().equals("square");
        double width = 260, height = 125;

        if(ad.urgent()){
            width = 360;
            height = 140;
        }

        if(isSquare){
            width = 300;
            height = 300;
        }

        return new Clickable(new Card(
                new Column().children(
                        new Image(ad.image().toString(), new ImageProps().width(width).height(height)),
                        new SpacerVertical(8),
                        new Text(ad.title())
                ),
                new CardProps().paddingAll(14)
        ), ()-> Redirect.to(ad.redirect().toString()));
    }
}
