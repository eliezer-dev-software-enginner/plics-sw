package my_app.domain;

import javafx.animation.Animation;
import javafx.util.Duration;
import megalodonte.ComputedState;
import megalodonte.application.ErrorReporter;
import megalodonte.base.Animations;
import megalodonte.base.UI;
import megalodonte.base.components.IconInterface;
import megalodonte.base.components.Ref;
import megalodonte.base.state.State;
import megalodonte.components.Icon;
import megalodonte.router.v4.ScreenContext;
import megalodonte.utils.ThrowingSupplier;
import megalodonte.v2.ListState;
import my_app.domain.components.Components;
import org.kordamp.ikonli.entypo.Entypo;

public abstract class ViewModelScreenContract<Model> {
    protected final ScreenContext ctx;
    protected final State<Boolean> modoEdicao = State.of(false);

    public final State<Boolean> focusState = new State<>(false);
    public final State<Boolean> formIsVisible = new State<>(true);
    public final ComputedState<String> formIsVisibleTextComputed = ComputedState.
            of(()-> formIsVisible.get()? "Minimizar formulário":"Expandir formulário",formIsVisible);

    public final ComputedState<IconInterface> iconComputed = ComputedState.
            of(()-> formIsVisible.get()? Components.ikon(Entypo.CHEVRON_SMALL_DOWN, 18, "#fff")
                    :Components.ikon(Entypo.CHEVRON_SMALL_UP, 18, "#fff"),formIsVisible);

    // Ícone fixo (chevron-down); a rotação comunica o estado, não a troca de ícone
    public final Ref<IconInterface> toggleIconRef = new Ref<>();
    private Animation toggleIconAnim;

    public final ComputedState<String> btnText = ComputedState.of(() -> modoEdicao.get() ? "Atualizar" : "+ Adicionar", modoEdicao);

    public final State<String> searchState = new State<>("");
    public final ListState<Model> allDataList = ListState.ofEmpty();
    public final ListState<Model> filteredList = ListState.ofEmpty();

    public ViewModelScreenContract(ScreenContext ctx) {
        this.ctx = ctx;
        searchState.subscribe(_ -> applyFilter());
        allDataList.subscribe(_ -> applyFilter());

        formIsVisible.subscribe(this::animateToggleIcon);
    }

    private void animateToggleIcon(boolean visible) {
        var icon = toggleIconRef.current();
        if (icon == null) return;

        UI.runOnUi(() -> {
            if (toggleIconAnim != null && toggleIconAnim.getStatus() == Animation.Status.RUNNING) {
                toggleIconAnim.stop();
            }
            double currentAngle = icon.getNode().getRotate(); // parte de onde o ícone está de verdade
            double targetAngle = visible ? 0 : 180;
            toggleIconAnim = Animations.rotate(icon.getNode(), currentAngle, targetAngle, Duration.millis(200));
            toggleIconAnim.play();
        });
    }

    public IconInterface createToggleIcon() {
        return Components.ikon(Entypo.CHEVRON_SMALL_UP, 18, "#fff").ref(toggleIconRef);
    }

    private void applyFilter() {
        var query = searchState.get();
        if (query == null || query.isBlank()) {
            filteredList.set(allDataList.get());
            return;
        }
        filteredList.set(allDataList.get().stream()
                .filter(it -> matchesSearch(it, query.trim().toLowerCase()))
                .toList());
    }

    protected abstract boolean matchesSearch(Model model, String query);

    protected void onInit() {}
    public void onDestroy() throws Exception {
        // no-op por padrão, subclasses sobrescrevem se precisar
    }

    public abstract void populateFromModel();
    public abstract void clearForm();
    public abstract void handleAddOrUpdate();
    public abstract void handleClickMenuDelete();

    //deve popular allDataList e filteredList
    //filteredList é o que vai preencher a tabela
    public abstract void fetchListData();

    public State<Boolean> modoEdicaoState(){
        return modoEdicao;
    }

    public void handleFocusChange(boolean focus) {
        focusState.set(focus);
    }

    protected <T> T createOrReport(ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            ErrorReporter.handle(e);
            throw new IllegalStateException(e); // interrompe a construção da tela de forma previsível
        }
    }

    public void handleToggleFormVisible(){
        formIsVisible.set(!formIsVisible.get());
    }
}
