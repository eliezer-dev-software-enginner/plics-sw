package my_app.screens.produtoScreen;

import megalodonte.v2.ListState;
import megalodonte.base.state.State;
import megalodonte.base.async.Async;
import megalodonte.base.UI;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.CategoriaModel;
import my_app.db.models.FornecedorModel;
import my_app.db.models.ProdutoModel;
import my_app.db.services.CategoriaService;
import my_app.db.services.FornecedorService;
import my_app.db.services.ProdutoService;
import my_app.domain.ViewModelScreenContract;
import my_app.domain.components.Components;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ProdutoScreenViewModel extends ViewModelScreenContract {

    private final ProdutoService produtoService;

    public final ListState<ProdutoModel> produtos = ListState.of(List.of());
    public final State<String> codigoBarras = new State<>("");
    public final State<String> descricao = new State<>("");
    public final State<String> precoCompra = new State<>("0");
    public final State<String> precoVenda = new State<>("0");

    public final State<String> margem = new State<>("0");
    public final State<String> lucro = new State<>("0");

    public final State<String> comissao = new State<>("");
    public final State<String> garantia = new State<>("");
    public final State<String> marca = new State<>("");

    public final State<String> unidadeSelected = new State<>("UN");

    public final State<List<CategoriaModel>> categorias = new State<>(List.of());
    public final State<CategoriaModel> categoriaSelected = new State<>(null);

    public final State<List<FornecedorModel>> fornecedores = State.of(List.of());
    public final State<FornecedorModel> fornecedorSelected = new State<>(null);

    public final State<String> observacoes = new State<>("");
    public final State<String> estoque = new State<>("0");
    public final State<LocalDate> validade = State.of(null);

    public final State<String> imagem = new State<>("/assets/produto-generico.png");
    public final State<ProdutoModel> produtoSelected = new State<>(null);
    public final State<String> perecivelSelected = new State<>("Não");

    public ProdutoScreenViewModel(ScreenContext ctx) {
        super(ctx);
        try {
            produtoService = new ProdutoService();
        } catch (SQLException e) {
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    public void loadInicial() {
        Async.Run(() -> {
            try {
                var produtosList = produtoService.listar();
                var fornecedorService = new FornecedorService();
                var fornecedorModelList = fornecedorService.listar();
                var categoriaService = new CategoriaService();
                var categoriasList = categoriaService.listar();

                UI.runOnUi(() -> {
                    this.produtos.addAll(produtosList);
                    this.categorias.set(categoriasList);
                    this.categoriaSelected.set(categoriasList.isEmpty() ? null : categoriasList.getFirst());

                    this.fornecedores.set(fornecedorModelList);
                    this.fornecedorSelected.set(fornecedorModelList.isEmpty() ? null : fornecedorModelList.getFirst());

                    for (var p : produtosList) {
                        var cat = categoriasList.stream()
                                .filter(it -> it.getId().equals(p.getCategoriaId()))
                                .findFirst()
                                .orElse(null);
                        var forn = fornecedorModelList.stream()
                                .filter(it -> it.getId().equals(p.getFornecedorId()))
                                .findFirst()
                                .orElse(null);
                        p.setCategoria(cat);
                        p.setFornecedor(forn);
                    }
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    @Override
    public void handleClickMenuDelete() {
        ProdutoModel produtoModel = produtoSelected.get();
        if (produtoModel == null) return;

        var bodyMessage = "Tem certeza que deseja excluir o produto: %s com código: %s?"
                .formatted(produtoModel.getDescricao(), produtoModel.getCodigoBarras());
        Components.ShowAlertAdvice(bodyMessage, () -> {
            Async.Run(() -> {
                try {
                    produtoService.excluirById(produtoModel.getId());
                    UI.runOnUi(() -> {
                        clearForm();
                        Components.ShowPopup(ctx, "Produto excluído com sucesso");
                    });
                } catch (Exception e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao excluir produto: " + e.getMessage()));
                }
            });
        });
    }

    @Override
    public void handleAddOrUpdate() {
        if (perecivelSelected.get().equals("Sim") && validade.isNull()) {
            Components.ShowAlertError("Escolha a data de validade");
            return;
        }

        if (modoEdicao.get()) {
            asyncAtualizar();
        } else {
            asyncSalvar();
        }
    }

    private void asyncAtualizar() {
        Async.Run(() -> {
            try {
                var selecionado = produtoSelected.get();
                fillModelFromForm(selecionado);
                produtoService.atualizar(selecionado);

                var produtosList = produtoService.listar();
                UI.runOnUi(() -> {
                    this.produtos.clear();
                    this.produtos.addAll(produtosList);
                    Components.ShowPopup(ctx, "Produto atualizado com sucesso!");
                    clearForm();
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    private void asyncSalvar() {
        Async.Run(() -> {
            try {
                var model = new ProdutoModel();
                fillModelFromForm(model);
                model.setTotalLiquido(model.getPrecoVenda().subtract(model.getPrecoCompra()));

                var salvo = produtoService.salvar(model);
                salvo.setCategoria(categoriaSelected.get());
                salvo.setFornecedor(fornecedorSelected.get());

                UI.runOnUi(() -> {
                    produtos.add(salvo);
                    Components.ShowPopup(ctx, "Produto cadastrado com sucesso");
                    clearForm();
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    private void fillModelFromForm(ProdutoModel model) {
        model.setCodigoBarras(codigoBarras.get());
        model.setDescricao(descricao.get());
        model.setPrecoCompra(Utils.deCentavosParaReal(precoCompra.get()));
        model.setPrecoVenda(Utils.deCentavosParaReal(precoVenda.get()));
        model.setUnidade(unidadeSelected.get());
        model.setCategoriaId(categoriaSelected.get() == null ? 1 : categoriaSelected.get().getId());
        model.setFornecedorId(fornecedorSelected.get() == null ? 1 : fornecedorSelected.get().getId());
        var estoqueField = estoque.get();
        model.setEstoque(estoqueField == null || estoqueField.trim().isEmpty() ? BigDecimal.ZERO : new BigDecimal(estoqueField));
        model.setObservacoes(observacoes.get());
        model.setImagem(imagem.get());
        model.setMarca(marca.get());
        model.setValidade(validade.isNull() ? null : DateUtils.localDateParaMillis(validade.get()));
        model.setGarantia(garantia.get());
        model.setComissao(comissao.get());
        model.setTotalLiquido(model.getPrecoVenda().subtract(model.getPrecoCompra()));
    }

    @Override
    public void clearForm() {
        codigoBarras.set("");
        descricao.set("");
        precoCompra.set("0");
        precoVenda.set("0");
        margem.set("0");
        lucro.set("0");
        comissao.set("");
        garantia.set("");
        marca.set("");
        unidadeSelected.set("UN");
        estoque.set("0");
        validade.set(null);
        observacoes.set("");
        imagem.set("/assets/produto-generico.png");
    }

    @Override
    public void populateFromModel() {
        if (produtoSelected.get() == null) return;
        final var model = produtoSelected.get();

        codigoBarras.set(model.getCodigoBarras());
        descricao.set(model.getDescricao());
        precoCompra.set(Utils.deRealParaCentavos(model.getPrecoCompra()));
        precoVenda.set(Utils.deRealParaCentavos(model.getPrecoVenda()));
        comissao.set(model.getComissao());
        garantia.set(model.getGarantia());
        marca.set(model.getMarca());
        unidadeSelected.set(model.getUnidade());
        estoque.set(Utils.quantidadeTratada(model.getEstoque()));

        validade.set(model.getValidade() != null ? DateUtils.millisParaLocalDate(model.getValidade()) : null);
        perecivelSelected.set(model.getValidade() != null && model.getValidade() > 0 ? "Sim" : "Não");
        observacoes.set(model.getObservacoes());
        imagem.set(model.getImagem());
    }

    public ScreenContext getCtx() {
        return ctx;
    }
}
