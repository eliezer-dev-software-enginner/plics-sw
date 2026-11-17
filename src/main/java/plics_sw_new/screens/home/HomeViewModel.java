package plics_sw_new.screens.home;

import javafx.application.Platform;
import javafx.scene.image.Image;
import megalodonte.ComputedState;
import megalodonte.base.state.State;
import megalodonte.v2.ListState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HomeViewModel {
    private static final Logger log= LoggerFactory.getLogger(HomeViewModel.class);
    private record LoadedAd(AdsScraper.Ad ad, Image image) { }

    public final State<String> receitas = new State<>("R$ 0,00");
    public final State<String> despesas = new State<>("R$ 0,00");
    public final State<String> lucroLiquido = new State<>("R$ 0,00");
    public final State<String> mesAtual = new State<>("");

    public final State<String> vendasHoje = new State<>("R$ 0,00");

    public ListState<AdsScraper.Ad> ads = ListState.ofEmpty();
    public ComputedState<Boolean> adsLoaded = ComputedState.of(()-> !ads.isEmpty() ,ads);

    public void init(){
        CompletableFuture.supplyAsync(() -> {
            try { return new AdsScraper().fetch(); }
            catch(Exception error) { log.debug("Anúncios indisponíveis",error);return List.<AdsScraper.Ad>of(); }
        }).thenAccept(fetchedAds -> Platform.runLater(() -> { ads.addAll(fetchedAds); }));
    }

}
