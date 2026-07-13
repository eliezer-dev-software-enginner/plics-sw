package my_app.screens.produtoScreen;

import megalodonte.v2.ListState;
import megalodonte.base.state.State;
import megalodonte.base.async.Async;
import megalodonte.base.UI;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.CategoriaModel;
import my_app.db.models.CorModel;
import my_app.db.models.ProdutoModel;
import my_app.db.services.CategoriaService;
import my_app.db.services.CorService;
import my_app.db.services.FornecedorService;
import my_app.db.services.ProdutoService;
import my_app.core.events.EntityEvent;
import my_app.core.events.EventBus;
import my_app.db.models.FornecedorModel;
import my_app.domain.ViewModelScreenContract;
import my_app.domain.components.Components;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ProdutoScreenViewModel extends ViewModelScreenContract<ProdutoModel> {

    private final ProdutoService produtoService;
    private final FornecedorService fornecedorService;
    private final CategoriaService categoriaService;
    private final CorService corService;

    public final ListState<CorModel> cores = ListState.ofEmpty();

    public final State<String> codigoBarras = new State<>("");
    public final State<String> descricao = new State<>("");
    public final State<String> precoCompra = new State<>("0");
    public final State<String> precoVenda = new State<>("0");

    public final State<String> margem = new State<>("0");
    public final State<String> lucro = new State<>("0");

    public final State<String> comissao = new State<>("");
    public final State<String> garantia = new State<>("");
    public final State<String> marca = new State<>("");

    public final ListState<String> coresSelecionadas = ListState.ofEmpty();
    public final State<String> tamanhoSelected = new State<>("");
    public final State<String> modelo = new State<>("");

    public final State<String> unidadeSelected = new State<>("UN");

    public final State<List<CategoriaModel>> categorias = new State<>(List.of());
    public final State<CategoriaModel> categoriaSelected = new State<>(null);

    public final State<List<FornecedorModel>> fornecedores = State.of(List.of());
    public final State<FornecedorModel> fornecedorSelected = new State<>(null);

    public final State<String> observacoes = new State<>("");
    public final State<String> estoque = new State<>("0");
    public final State<String> estoqueMinimo = new State<>("0");
    public final State<LocalDate> validade = State.of(null);

    public final State<String> imagem = new State<>("/assets/produto-generico.png");
    public final State<ProdutoModel> produtoSelected = new State<>(null);
    public final State<String> perecivelSelected = new State<>("Não");

    public ProdutoScreenViewModel(ScreenContext ctx) {
        super(ctx);
        this.produtoService = createOrReport(ProdutoService::new);
        this.fornecedorService = createOrReport(FornecedorService::new);
        this.categoriaService = createOrReport(CategoriaService::new);
        this.corService = createOrReport(CorService::new);
        EventBus.getInstance().subscribe(event -> {
            if (event instanceof EntityEvent<?> ee && ee.entity() instanceof FornecedorModel) {
                refreshFornecedores();
            }
        });
    }

    @Override
    protected boolean matchesSearch(ProdutoModel model, String query) {
        return contains(model.getDescricao(), query)
                || contains(model.getCodigoBarras(), query);
    }

    private boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    private void refreshFornecedores() {
        Async.Run(() -> {
            try {
                var fornecedorModelList = fornecedorService.listar();
                UI.runOnUi(() -> {
                    this.fornecedores.set(fornecedorModelList);
                    this.fornecedorSelected.set(fornecedorModelList.isEmpty() ? null : fornecedorModelList.getFirst());
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    @Override
    public void fetchListData() {
        Async.Run(() -> {
            try {
                var produtosList = produtoService.listar();
                var fornecedorModelList = fornecedorService.listar();
                var categoriasList = categoriaService.listar();
                var coresList = corService.listar();

                UI.runOnUi(() -> {
                    this.allDataList.set(produtosList);
                    this.cores.set(coresList);
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
        Components.ShowAlertAdvice(bodyMessage, () -> Async.Run(() -> {
            try {
                produtoService.excluirById(produtoModel.getId());
                UI.runOnUi(() -> {
                    allDataList.removeIf(it -> it.getId().equals(produtoModel.getId()));
                    clearForm();
                    Components.ShowPopup(ctx, "Produto excluído com sucesso");
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao excluir produto: " + e.getMessage()));
            }
        }));
    }

    public String validar() {
        if (perecivelSelected.get().equals("Sim") && validade.isNull()) {
            return "Escolha a data de validade";
        }
        if (perecivelSelected.get().equals("Sim") && !validade.isNull() && validade.get().isBefore(LocalDate.now())) {
            return "A data de validade deve ser maior ou igual à data atual";
        }
        return null;
    }

    @Override
    public void handleAddOrUpdate() {
        var erro = validar();
        if (erro != null) {
            Components.ShowAlertError(erro);
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

                var atualizado = new ProdutoModel();
                atualizado.setId(selecionado.getId());
                atualizado.setCodigoBarras(selecionado.getCodigoBarras());
                atualizado.setDescricao(selecionado.getDescricao());
                atualizado.setPrecoCompra(selecionado.getPrecoCompra());
                atualizado.setPrecoVenda(selecionado.getPrecoVenda());
                atualizado.setUnidade(selecionado.getUnidade());
                atualizado.setCategoriaId(selecionado.getCategoriaId());
                atualizado.setFornecedorId(selecionado.getFornecedorId());
                atualizado.setEstoque(selecionado.getEstoque());
                atualizado.setEstoqueMinimo(selecionado.getEstoqueMinimo());
                atualizado.setObservacoes(selecionado.getObservacoes());
                atualizado.setImagem(selecionado.getImagem());
                atualizado.setMarca(selecionado.getMarca());
                atualizado.setCor(selecionado.getCor());
                atualizado.setTamanho(selecionado.getTamanho());
                atualizado.setModelo(selecionado.getModelo());
                atualizado.setValidade(selecionado.getValidade());
                atualizado.setGarantia(selecionado.getGarantia());
                atualizado.setComissao(selecionado.getComissao());
                atualizado.setTotalLiquido(selecionado.getTotalLiquido());
                atualizado.setDataCriacao(selecionado.getDataCriacao());
                atualizado.setCategoria(selecionado.getCategoria());
                atualizado.setFornecedor(selecionado.getFornecedor());

                UI.runOnUi(() -> {
                    this.allDataList.updateIf(p -> p.getId().equals(atualizado.getId()), p -> atualizado);
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

                //TODO: produto serviçe deveria cuidar disso
                model.setTotalLiquido(model.getPrecoVenda().subtract(model.getPrecoCompra()));

                var salvo = produtoService.salvar(model);

                salvo.setCategoria(categoriaSelected.get());
                salvo.setFornecedor(fornecedorSelected.get());

                UI.runOnUi(() -> {
                    allDataList.add(salvo);
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
        model.setCor(String.join(", ", coresSelecionadas.get()));
        model.setTamanho(tamanhoSelected.get());
        model.setModelo(modelo.get());
        model.setValidade("Sim".equals(perecivelSelected.get()) && !validade.isNull() ? DateUtils.localDateParaMillis(validade.get()) : null);
        model.setGarantia(garantia.get());
        model.setComissao(comissao.get());
        model.setTotalLiquido(model.getPrecoVenda().subtract(model.getPrecoCompra()));
        var estoqueMinimoField = estoqueMinimo.get();
        model.setEstoqueMinimo(estoqueMinimoField == null || estoqueMinimoField.trim().isEmpty() ? BigDecimal.ZERO : new BigDecimal(estoqueMinimoField));
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
        coresSelecionadas.set(List.of());
        tamanhoSelected.set("");
        modelo.set("");
        unidadeSelected.set("UN");
        estoque.set("0");
        estoqueMinimo.set("0");
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
        var corStr = model.getCor();
        coresSelecionadas.set(corStr != null && !corStr.isBlank()
                ? List.of(corStr.split(",\s*"))
                : List.of());

        tamanhoSelected.set(model.getTamanho());
        modelo.set(model.getModelo());
        unidadeSelected.set(model.getUnidade());
        estoque.set(Utils.quantidadeTratada(model.getEstoque()));
        estoqueMinimo.set(Utils.quantidadeTratada(model.getEstoqueMinimo()));

        validade.set(model.getValidade() != null ? DateUtils.millisParaLocalDate(model.getValidade()) : null);
        perecivelSelected.set(model.getValidade() != null && model.getValidade() > 0 ? "Sim" : "Não");
        observacoes.set(model.getObservacoes());
        imagem.set(model.getImagem());
    }

    public ScreenContext getCtx() {
        return ctx;
    }

    @Override
    public void onDestroy() throws Exception {
        this.produtoService.close();
        this.categoriaService.close();
        this.corService.close();
        this.fornecedorService.close();
    }
}
