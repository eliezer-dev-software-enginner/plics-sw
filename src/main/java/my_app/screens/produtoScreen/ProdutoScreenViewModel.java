package my_app.screens.produtoScreen;

import megalodonte.v2.ListState;
import megalodonte.State;
import megalodonte.base.async.Async;
import megalodonte.base.UI;
import megalodonte.router.v4.ScreenContext;
import my_app.db.dto.ProdutoDto;
import my_app.db.models_old.CategoriaModel;
import my_app.db.models_old.FornecedorModel;
import my_app.db.models_old.ProdutoModel;
import my_app.db.repositories_old.CategoriaRepository;
import my_app.db.repositories_old.FornecedorRepository;
import my_app.db.repositories_old.ProdutoRepository;
import my_app.lifecycle.viewmodel.component.ViewModelScreenContract;
import my_app.domain.components.Components;
import my_app.services.ProdutoService;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ProdutoScreenViewModel extends ViewModelScreenContract {
    private final ProdutoService service;
    private final ProdutoRepository produtoRepository;

    public final ListState<ProdutoModel> produtos = ListState.of(List.of());
    public final State<String> codigoBarras = new State<>("");
    public final State<String> descricao = new State<>("");
    public final State<String> precoCompra = new State<>("0");
    public final State<String> precoVenda = new State<>("0");

    // depois vira ComputedState
    public final State<String> margem = new State<>("0");
    public final State<String> lucro = new State<>("0");

    public final State<String> comissao = new State<>("");
    public final State<String> garantia = new State<>("");
    public final State<String> marca = new State<>("");

    public final State<String> unidadeSelected = new State<>("UN");

    public final State<List<CategoriaModel>> categorias = new State<>(List.of());
    public final State<CategoriaModel> categoriaSelected = new State<>(null);

    //TODO: BUSCAR FORNECEDORES DO BANCO
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
        service = new ProdutoService();
        produtoRepository = new ProdutoRepository();
    }

    public void loadInicial() {
        Async.Run(() -> {
            try {
                var produtosList = produtoRepository.listar();
                var fornecedorModelList = new FornecedorRepository().listar();
                var categorias = new CategoriaRepository().listar();

                UI.runOnUi(() -> {
                    this.produtos.addAll(produtosList);
                    this.categorias.set(categorias);
                    this.categoriaSelected.set(categorias.isEmpty() ? null : categorias.getFirst());

                    this.fornecedores.set(fornecedorModelList);
                    this.fornecedorSelected.set(fornecedorModelList.isEmpty() ? null : fornecedorModelList.getFirst());

                    for(var p :produtosList){
                        var categoria = categorias.stream()
                                .filter(it-> it.id.equals(p.categoriaId))
                                .findFirst()
                                .orElse(null);

                        var fornecedor = fornecedorModelList.stream()
                                .filter(it-> it.id.equals(p.fornecedorId))
                                .findFirst()
                                .orElse(null);

                        p.categoria = categoria;
                        p.fornecedor = fornecedor;
                    }
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    public ProdutoDto toProduto() {
        var p = new ProdutoDto();
        p.codigoBarras = codigoBarras.get();
        p.descricao = descricao.get();

        p.precoCompra = Utils.deCentavosParaReal(precoCompra.get());
        p.precoVenda = Utils.deCentavosParaReal(precoVenda.get());
        p.unidade = unidadeSelected.get();
        p.categoriaId = categoriaSelected.get() == null ? 1L : categoriaSelected.get().id;
        p.fornecedorId = fornecedorSelected.get() == null ? 1L : fornecedorSelected.get().id;
        var estoqueField = estoque.get();
        p.estoque = estoqueField == null || estoqueField.trim().isEmpty()? BigDecimal.ZERO: new BigDecimal(estoqueField);
        p.observacoes = observacoes.get();
        p.imagem = imagem.get();
        p.marca = marca.get();

        p.validade = validade.isNull()? null: DateUtils.localDateParaMillis(validade.get());
        p.garantia = garantia.get();
        p.totalLiquido = p.precoVenda.subtract(p.precoCompra);
        return p;
    }


    @Override
    public void handleClickMenuDelete() {
        ProdutoModel produtoModel = produtoSelected.get();
        if (produtoModel == null) return;

        var bodyMessage = "Tem certeza que deseja excluir o produto: %s com código: %s?".formatted(produtoModel.descricao, produtoModel.codigoBarras);
        Components.ShowAlertAdvice(bodyMessage, () -> {
            Async.Run(() -> {
                try {
                    excluir();
                    //vm.refreshProdutos();
                    UI.runOnUi(() -> {
                        clearForm();
                        Components.ShowPopup(ctx, "Produto excluído com sucesso");
                    });
                } catch (Exception e) {
                    UI.runOnUi(()-> Components.ShowAlertError("Erro ao excluir produto: " + e.getMessage()));
                }
            });
        });
    }

    public void handleAddOrUpdate() {
        var dto = toProduto();

        if(perecivelSelected.get().equals("Sim") && validade.isNull()) {
            Components.ShowAlertError("Escolha a data de validade");
            return;
        }

        if (modoEdicao.get()) {
            asyncAtualizar(ctx, dto);
        }else{
            asyncSalvar(ctx, dto);
        }
    }

    private void asyncAtualizar(ScreenContext ctx, ProdutoDto dto) {
        Async.Run(() -> {
            try {
                service.atualizar((ProdutoModel) new ProdutoModel().fromIdAndDto(produtoSelected.get().id, dto));
                var produtosList = produtoRepository.listar();
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

    private void asyncSalvar(ScreenContext ctx, ProdutoDto dto) {
        Async.Run(() -> {
            try {
                var produtoModel = service.salvar(dto);
                produtoModel.dataCriacao = System.currentTimeMillis();
                produtoModel.categoria = categoriaSelected.get();
                produtoModel.fornecedor = fornecedorSelected.get();

                UI.runOnUi(() -> {
                    produtos.add(produtoModel);
                    Components.ShowPopup(ctx, "Produto cadastrado com sucesso");
                    clearForm();
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

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

    //TODO: usar a exclusão da FornecedorScreenViewModel, pois esta não mostra aviso
    public void excluir() throws Exception {
        Long id = produtoSelected.get().id;
        service.excluir(id);
        produtos.removeIf(it -> it.id.equals(id));
    }

    public ScreenContext getCtx() {
        return ctx;
    }

    public void populateFromModel() {
        if(produtoSelected.get() == null) return;
        final var model = produtoSelected.get();

        codigoBarras.set(model.codigoBarras);
        descricao.set(model.descricao);
        precoCompra.set(Utils.deRealParaCentavos( model.precoCompra));
        precoVenda.set(Utils.deRealParaCentavos( model.precoVenda));
        //margem.set(model.);
        //lucro.set("0");
        comissao.set(model.comissao);
        garantia.set(model.garantia);
        marca.set(model.marca);
        unidadeSelected.set(model.unidade);
        estoque.set(Utils.quantidadeTratada(model.estoque));

        validade.set(model.validade != null ? DateUtils.millisParaLocalDate(model.validade) : null);
        perecivelSelected.set(model.validade != null && model.validade > 0? "Sim": "Não");
        observacoes.set(model.observacoes);
        imagem.set(model.imagem);
    }

    @Override
    public State<Boolean> modoEdicaoState() {
        return modoEdicao;
    }

}

