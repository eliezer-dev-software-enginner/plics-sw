package my_app.screens.ler_planilha_ia;

import javafx.stage.FileChooser;
import megalodonte.base.UI;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.state.State;
import megalodonte.components.Button;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.props.ColumnProps;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.CategoriaModel;
import my_app.db.models.FornecedorModel;
import my_app.db.models.ProdutoModel;
import my_app.db.services.CategoriaService;
import my_app.db.services.FornecedorService;
import my_app.db.services.ProdutoService;
import my_app.domain.Data;
import my_app.domain.components.Components;
import my_app.services.PlanilhaFornecedorReader;
import my_app.utils.Utils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class LerPlanilhaScreen implements ScreenComponent {
    private final ScreenContext screenContext;
    private final FornecedorService fornecedorService;
    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;
    final State<String> status = new State<>("");
    final State<CategoriaModel> categoria = new State<>(null);

    public LerPlanilhaScreen(ScreenContext screenContext) {
        this.screenContext = screenContext;
        this.fornecedorService = createFornecedorService();
        this.produtoService = createProdutoService();
        categoriaService = createCategoriaService();
    }

    @Override
    public void onMount() {
        try {
            var categoriasList = categoriaService.listar();
            UI.runOnUi(()->   categoria.set(categoriasList.getFirst()) );
        } catch (SQLException e) {
            e.printStackTrace();
            UI.runOnUi(()-> Components.ShowAlertError("Erro ao carregar categoria GERAL"+e.getMessage()));
        }
    }

    @Override
    public Component render() {
        var stage = screenContext.selfStage();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolha a planilha");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("planilha",
                "*.xlsx", "*.excel"));

        Runnable handleClick = ()->{
            var file = fileChooser.showOpenDialog(stage);

            if (file != null) {
                IO.print("caminho: " + file.toPath().toUri());

                Components.ShowPopup(screenContext, "Planilha selecionada");

                try{
                    var items = new PlanilhaFornecedorReader().ler(file.toPath());
                    //salvar fornecedores
                    for (PlanilhaFornecedorReader.ItemFornecedor row : items) {
                        String fornecedorNome = row.fornecedor();
                        FornecedorModel fornecedorModel = new FornecedorModel();
                        fornecedorModel.setNome(fornecedorNome);
                        fornecedorModel.setDataCriacao(LocalDateTime.now());

                        try{
                          fornecedorModel = fornecedorService.salvar(fornecedorModel);
                        }catch (Exception e){
                            throw new RuntimeException("Erro ao salvar fornecedor: " + e.getMessage());
                        }
                        //salvar produtos referenciando o fornecedor
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

                        try {
                            produtoService.salvar(produtoModel);
                        } catch (Exception e) {
                            throw new RuntimeException("Erro ao salvar produto: " + e.getMessage());
                        }
                    }
                    UI.runOnUi(()->Components.ShowPopupWithButton(screenContext,"Dados copiados com sucesso","Fechar tela", stage::close));

                }catch (Exception e){
                    e.printStackTrace();
                    UI.runOnUi(()-> Components.ShowAlertError(e.getMessage()));
                }
            }
        };

        return new Container()
                .children(
                      new Column(new ColumnProps().fillHeight().centerVertically().centerHorizontally().paddingTop(90))
                              .children(
                                      Components.FormTitle("Carregar planilha automaticamente para o sistema"),
                                      new Button("Carregar planilha").onClick(handleClick),
                                      new Text(status)
                              )
                );
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
