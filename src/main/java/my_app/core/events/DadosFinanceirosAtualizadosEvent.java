package my_app.core.events;

public class DadosFinanceirosAtualizadosEvent {
    private static final DadosFinanceirosAtualizadosEvent INSTANCE = new DadosFinanceirosAtualizadosEvent();

    private DadosFinanceirosAtualizadosEvent() {}

    public static DadosFinanceirosAtualizadosEvent getInstance() {
        return INSTANCE;
    }
}
