package my_app.screens.ler_planilha_ia;

import megalodonte.base.UI;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.state.State;
import megalodonte.components.FilePicker;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import megalodonte.base.route.v2.ScreenContextInterface;
import my_app.core.db.models.CategoriaModel;
import my_app.core.db.models.FornecedorModel;
import my_app.core.db.models.ProdutoModel;
import my_app.core.db.services.CategoriaService;
import my_app.core.db.services.FornecedorService;
import my_app.core.db.services.ProdutoService;
import my_app.core.Data;
import my_app.core.components.Components;
import my_app.services.PlanilhaFornecedorReader;
import my_app.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class LerPlanilhaScreen implements ScreenComponent {
    private static final UiDefinition VIEW = UiDefinition.fromResource(LerPlanilhaScreen.class, "/views/ler-planilha.view.jsonc");
    private static final Logger log = LoggerFactory.getLogger(LerPlanilhaScreen.class);

    private final ScreenContextInterface screenContext;
    private final UiSession session;
    private final FornecedorService fornecedorService;
    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;
    final State<String> status = new State<>("");
    final State<CategoriaModel> categoria = new State<>(null);

    public LerPlanilhaScreen(ScreenContextInterface screenContext) {
        this.screenContext = screenContext;
        this.fornecedorService = createFornecedorService();
        this.produtoService = createProdutoService();
        categoriaService = createCategoriaService();
        session = VIEW.open(new Actions().register("import", ignored -> importarPlanilha()));
        session.display("status", status, String.class);
    }

    @Override
    public void onMount() {
        try {
            var categoriasList = categoriaService.listar();
            UI.runOnUi(()->   categoria.set(categoriasList.getFirst()) );
        } catch (SQLException e) {
            log.error("Erro ao carregar categoria GERAL", e);
            UI.runOnUi(()-> Components.ShowAlertError("Erro ao carregar categoria GERAL"+e.getMessage()));
        }
    }

    @Override public Component render() { return session.render(); }

    @Override public void onDestroy() { session.close(); }

    private void importarPlanilha() {
        var selected = FilePicker.chooseFile(screenContext.selfStage(), "Escolha a planilha", "Planilhas", java.util.List.of("*.xlsx", "*.xls"));
        if (selected.isEmpty()) return;
        var file = java.nio.file.Path.of(selected.get());
        Components.ShowPopup(screenContext, "Planilha selecionada");
        try {
            var items = new PlanilhaFornecedorReader().ler(file);
            for (PlanilhaFornecedorReader.ItemFornecedor row : items) {
                FornecedorModel fornecedorModel = new FornecedorModel();
                fornecedorModel.setNome(row.fornecedor());
                fornecedorModel.setDataCriacao(LocalDateTime.now());
                fornecedorModel = fornecedorService.salvar(fornecedorModel);

                ProdutoModel produtoModel = new ProdutoModel();
                produtoModel.setCodigoBarras(Utils.gerarCodigoBarrasEAN13());
                produtoModel.setUnidade(Data.unidadesDeMedidaList.getFirst());
                produtoModel.setTotalLiquido(row.venda().subtract(row.custo()));
                produtoModel.setCategoriaId(categoria.get().getId());
                produtoModel.setDescricao(row.produto());
                produtoModel.setEstoque(new BigDecimal(row.quantidade()));
                produtoModel.setFornecedorId(fornecedorModel.getId());
                produtoModel.setPrecoCompra(row.custo());
                produtoModel.setPrecoVenda(row.venda());
                produtoModel.setDataCriacao(LocalDateTime.now());
                produtoService.salvar(produtoModel);
            }
            UI.runOnUi(() -> Components.ShowPopupWithButton(screenContext, "Dados copiados com sucesso", "Fechar tela", screenContext.selfStage()::close));
        } catch (Exception error) {
            log.error("Erro ao importar planilha de fornecedor", error);
            UI.runOnUi(() -> Components.ShowAlertError(error.getMessage()));
        }
    }

    private static FornecedorService createFornecedorService() {
        try {
            return new FornecedorService();
        } catch (SQLException e) {
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            throw new RuntimeException(e);
        }
    }
    private static ProdutoService createProdutoService() {
        try {
            return new ProdutoService();
        } catch (SQLException e) {
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    private static CategoriaService createCategoriaService() {
        try {
            return new CategoriaService();
        } catch (SQLException e) {
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            throw new RuntimeException(e);
        }
    }
}
