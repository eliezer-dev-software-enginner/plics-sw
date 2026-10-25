package my_app.screens.homeScreen;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import megalodonte.base.Redirect;
import megalodonte.base.components.Component;
import megalodonte.components.Clickable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Optional home promotion area; unavailable feeds never block the dashboard. */
final class HomeAdsPane implements AutoCloseable {
    private static final Logger log=LoggerFactory.getLogger(HomeAdsPane.class);
    private final VBox regularRoot=new VBox(18);
    private final VBox urgentRoot=new VBox(18);
    private volatile boolean closed;

    HomeAdsPane() {
        for(VBox root:List.of(regularRoot,urgentRoot)) {
            root.setManaged(false);
            root.setVisible(false);
            root.setMaxWidth(Double.MAX_VALUE);
        }
        CompletableFuture.supplyAsync(() -> {
            try { return new AdsScraper().fetch(); }
            catch(Exception error) { log.debug("Anúncios indisponíveis",error);return List.<AdsScraper.Ad>of(); }
        }).thenAccept(ads -> Platform.runLater(() -> { if(!closed) show(ads); }));
    }

    Component regularComponent() { return Component.CreateFromJavaFxNode(regularRoot); }
    Component urgentComponent() { return Component.CreateFromJavaFxNode(urgentRoot); }

    private void show(List<AdsScraper.Ad> ads) {
        if(ads.isEmpty()) return;
        regularRoot.getChildren().clear();
        urgentRoot.getChildren().clear();
        var regular=ads.stream().filter(ad -> !ad.urgent()).toList();
        var urgent=ads.stream().filter(AdsScraper.Ad::urgent).toList();
        if(!regular.isEmpty()) {
            VBox section=new VBox(12);
            section.getChildren().add(heading("Para você", "Novidades e serviços selecionados"));
            FlowPane cards=new FlowPane(12,12);
            cards.setPrefWrapLength(1000);
            regular.forEach(ad -> cards.getChildren().add(card(ad,false).getJavaFxNode()));
            section.getChildren().add(cards);
            regularRoot.getChildren().add(section);
            regularRoot.setManaged(true);
            regularRoot.setVisible(true);
        }
        if(!urgent.isEmpty()) {
            VBox section=new VBox(12);
            section.getChildren().add(heading("Em destaque", "Confira esta oportunidade"));
            if(urgent.size()==1) section.getChildren().add(featuredCard(urgent.getFirst()).getJavaFxNode());
            else urgent.forEach(ad -> section.getChildren().add(card(ad,true).getJavaFxNode()));
            urgentRoot.getChildren().add(section);
            urgentRoot.setManaged(true);
            urgentRoot.setVisible(true);
        }
    }

    private static VBox heading(String label,String subtitle) {
        Label title=new Label(label);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #18324d;");
        Label detail=new Label(subtitle);
        detail.setStyle("-fx-font-size: 13px; -fx-text-fill: #53657e;");
        return new VBox(3,title,detail);
    }

    private static Component card(AdsScraper.Ad ad,boolean urgent) {
        ImageView image=new ImageView(new Image(ad.image().toString(),true));
        image.setFitWidth(urgent ? 360 : 260);
        image.setFitHeight(urgent ? 140 : 125);
        image.setPreserveRatio(true);
        Label title=new Label(ad.title());
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #18324d;");
        VBox content=new VBox(8,image,title);
        HBox box=new HBox(content);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(14));
        box.setStyle(urgent
                ? "-fx-background-color: #fff7ed; -fx-background-radius: 12; -fx-border-color: #f0b66b; -fx-border-radius: 12;"
                : "-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #dce6ef; -fx-border-radius: 12;");
        return clickable(ad,box,false);
    }

    private static Component featuredCard(AdsScraper.Ad ad) {
        ImageView image=new ImageView(new Image(ad.image().toString(),true));
        image.setPreserveRatio(true);
        image.setFitHeight(300);
        Label title=new Label(ad.title());
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #18324d;");
        VBox copy=new VBox(12,title);
        copy.setAlignment(Pos.CENTER_LEFT);
        copy.setMinWidth(0);
        copy.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(copy,Priority.ALWAYS);
        HBox box=new HBox(28,image,copy);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(18));
        box.setMaxWidth(Double.MAX_VALUE);
        box.setStyle("-fx-background-color: #fff7ed; -fx-background-radius: 12; -fx-border-color: #f0b66b; -fx-border-radius: 12;");
        image.fitWidthProperty().bind(box.widthProperty().multiply(.55));
        return clickable(ad,box,true);
    }

    private static Component clickable(AdsScraper.Ad ad,HBox box,boolean fillWidth) {
        var clickable=new Clickable(Component.CreateFromJavaFxNode(box))
                .onClick(() -> Redirect.to(ad.redirect().toString()));
        if(fillWidth && clickable.getJavaFxNode() instanceof Region region) region.setMaxWidth(Double.MAX_VALUE);
        return clickable;
    }

    @Override public void close() {
        closed=true;
        regularRoot.getChildren().clear();
        urgentRoot.getChildren().clear();
    }
}
