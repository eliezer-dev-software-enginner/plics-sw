package my_app.domain;

import megalodonte.ComputedState;
import megalodonte.application.ErrorReporter;
import megalodonte.base.state.State;
import megalodonte.router.v4.ScreenContext;
import megalodonte.utils.ThrowingSupplier;

public abstract class ViewModelScreenContract {
    protected final ScreenContext ctx;
    protected final State<Boolean> modoEdicao = State.of(false);

    public final State<Boolean> focusState = new State<>(false);

    public final ComputedState<String> btnText = ComputedState.of(() -> modoEdicao.get() ? "Atualizar" : "+ Adicionar", modoEdicao);

    public ViewModelScreenContract(ScreenContext ctx){
        this.ctx = ctx;
    }

    protected void onInit() {}
    public void onDestroy() throws Exception {
        // no-op por padrão, subclasses sobrescrevem se precisar
    }

    public abstract void populateFromModel();
    public abstract void clearForm();
    public abstract void handleAddOrUpdate();
    public abstract void handleClickMenuDelete();

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
}
