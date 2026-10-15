package my_app.screens.relatoriosScreen;

import com.google.gson.Gson;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.base.state.State;
import megalodonte.base.state.Subscription;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import my_app.core.components.Components;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** Financial report layout, filters and chart placement are declared in JSONC. */
public final class RelatoriosScreen implements ScreenComponent {
    private static final UiDefinition VIEW = UiDefinition.fromResource(RelatoriosScreen.class, "/views/relatorios.view.jsonc");
    private static final Gson JSON = new Gson();
    private final RelatoriosScreenViewModel vm;
    private final UiSession session;
    private final List<Subscription> subscriptions = new ArrayList<>();

    public RelatoriosScreen(ScreenContextInterface ctx) {
        vm = new RelatoriosScreenViewModel(ctx);
        session = VIEW.open(new Actions()
                .register("generate", ignored -> generate())
                .register("downloadPdf", ignored -> vm.baixarPdf()));
        session.states().string("dataInicio").set(vm.dataInicio.get().toString());
        session.states().string("dataFim").set(vm.dataFim.get().toString());
        session.display("receitasVendas", vm.receitasVendas, String.class);
        session.display("receitasPedidosPdv", vm.receitasPedidosPdv, String.class);
        session.display("receitasContasRecebidas", vm.receitasContasRecebidas, String.class);
        session.display("despesasCompras", vm.despesasCompras, String.class);
        session.display("despesasContasPagas", vm.despesasContasPagas, String.class);
        session.display("totalReceitas", vm.totalReceitas, String.class);
        session.display("totalDespesas", vm.totalDespesas, String.class);
        session.display("lucroLiquido", vm.lucroLiquido, String.class);
        session.display("contasReceberEmAberto", vm.contasReceberEmAberto, String.class);
        session.display("contasPagarEmAberto", vm.contasPagarEmAberto, String.class);
        session.display("produtosMaisVendidos", vm.produtosMaisVendidos, String.class);
        session.display("produtosSemVenda", vm.produtosSemVenda, String.class);
        session.display("produtosDevolvidos", vm.produtosDevolvidos, String.class);
        session.display("novosClientes", vm.novosClientes, String.class);
        session.display("novosFornecedores", vm.novosFornecedores, String.class);
        bindChart("comparativo", vm.comparativoSeries);
        bindChart("receitasPie", vm.receitasPieData);
        bindChart("despesasPie", vm.despesasPieData);
        bindChart("formasPagamentoPie", vm.formasPagamentoPieData);
    }

    private <T> void bindChart(String name, State<List<T>> values) {
        subscriptions.add(values.observe(items -> session.states().string(name).set(JSON.toJson(items))));
    }

    private void generate() {
        try {
            vm.dataInicio.set(LocalDate.parse(session.states().string("dataInicio").get()));
            vm.dataFim.set(LocalDate.parse(session.states().string("dataFim").get()));
            vm.gerarRelatorio();
        } catch (DateTimeParseException error) {
            Components.ShowAlertError("Selecione datas válidas para o relatório.");
        }
    }

    @Override public Component render() { return session.render(); }
    @Override public void onMount() { vm.gerarRelatorio(); }
    @Override public void onDestroy() {
        subscriptions.forEach(Subscription::close);
        session.close();
        try { vm.onDestroy(); }
        catch (Exception error) { throw new IllegalStateException("Could not close report services", error); }
    }
}
