package my_app.lifecycle.viewmodel.component;

import megalodonte.ComputedState;
import megalodonte.State;
import megalodonte.router.v4.ScreenContext;

public abstract class ViewModelv2 {
    protected final ScreenContext ctx;
    protected final State<Boolean> modoEdicao = State.of(false);

    public final State<Boolean> focusState = new State<>(false);

    public ComputedState<String> btnText = ComputedState.of(() -> modoEdicao.get() ? "Atualizar" : "+ Adicionar", modoEdicao);

    public ViewModelv2(ScreenContext ctx){
        this.ctx = ctx;
    }

    protected void onInit() {
        // ciclo de vida futuro
    }

    protected void onDispose() {
        // limpeza futura
    }

    public abstract void populateFromModel();
    public abstract void clearForm();
    public abstract void handleAddOrUpdate();
    public abstract void handleClickMenuDelete();

    public State<Boolean> modoEdicaoState(){
        return modoEdicao;
    }
}
/**
 * Isso te permite evoluir depois para:
 *
 * lifecycle
 *
 * cache
 *
 * hot reload
 *
 * scoping por screen
 */