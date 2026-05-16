package my_app.lifecycle.viewmodel.component;

public abstract class ViewModelv2 {
    public ViewModelv2(){
    }

    protected void onInit() {
        // ciclo de vida futuro
    }

    protected void onDispose() {
        // limpeza futura
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