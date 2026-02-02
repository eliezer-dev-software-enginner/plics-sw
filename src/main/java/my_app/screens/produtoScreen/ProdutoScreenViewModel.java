package my_app.screens.produtoScreen;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import megalodonte.ComputedState;
import megalodonte.ListState;
import megalodonte.State;
import megalodonte.async.Async;
import megalodonte.base.UI;
import megalodonte.router.Router;
import my_app.db.dto.ProdutoDto;
import my_app.db.models.CategoriaModel;
import my_app.db.models.FornecedorModel;
import my_app.db.models.ProdutoModel;
import my_app.db.repositories.CategoriaRepository;
import my_app.db.repositories.FornecedorRepository;
import my_app.db.repositories.ProdutoRepository;
import my_app.lifecycle.viewmodel.component.ViewModel;
import my_app.screens.components.Components;
import my_app.services.ProdutoService;
import my_app.utils.Utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

public class ProdutoScreenViewModel extends ViewModel {

    private final ProdutoService service = new ProdutoService();
    private final ProdutoRepository produtoRepository = new ProdutoRepository();
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

    public final List<String> unidades = List.of("UN", "KG", "ml");
    public final State<String> unidadeSelected = new State<>("UN");

    public final State<List<CategoriaModel>> categorias = new State<>(List.of());
    public final State<CategoriaModel> categoriaSelected = new State<>(null);

    //TODO: BUSCAR FORNECEDORES DO BANCO
    public final State<List<FornecedorModel>> fornecedores = State.of(List.of());
    public final State<FornecedorModel> fornecedorSelected = new State<>(null);

    public final State<String> observacoes = new State<>("");
    public final State<String> estoque = new State<>("0");
    public final State<String> validade = new State<>("");
    public final State<LocalDate> dtCriacao = State.of(null);

    public final State<String> imagem = new State<>("/assets/produto-generico.png");

    public final State<Boolean> modoEdicao = State.of(false);
    public final ComputedState<String> btnText = ComputedState.of(() -> modoEdicao.get() ? "Atualizar" : "+ Adicionar", modoEdicao);
    public final State<ProdutoModel> produtoSelected = State.of(null);

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
        return p;
    }

    public void salvarOuAtualizar(Router router) {
        var dto = toProduto();

        if (modoEdicao.get()) {
            Async.Run(() -> {
                try {
                    service.atualizar(new ProdutoModel().fromIdAndDto(produtoSelected.get().id, dto));
                    var produtosList = produtoRepository.listar();
                    UI.runOnUi(() -> {
                        this.produtos.addAll(produtosList);
                        Components.ShowPopup(router, "Produto atualizado com sucesso!");
                    });
                } catch (Exception e) {
                    UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
                }
            });
        }else{
            Async.Run(() -> {
                try {
                    var produtoModel = service.salvar(dto);
                    produtoModel.dataCriacao = System.currentTimeMillis();
                    produtoModel.categoria = categoriaSelected.get();

                    UI.runOnUi(() -> {
                        produtos.add(produtoModel);
                        Components.ShowPopup(router, "Produto cadastrado com sucesso");
                        limparFormulario();
                    });
                } catch (Exception e) {
                    UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
                }
            });
        }
    }


    public void limparFormulario() {
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
        validade.set("");
        observacoes.set("");
        imagem.set("/assets/produto-generico.png");
    }

    public void excluir() throws Exception {
        Long id = produtoSelected.get().id;
        service.excluir(id);
        produtos.removeIf(it -> it.id.equals(id));
    }

    public void loadInicial() {
        Async.Run(() -> {
            try {
                var produtosList = produtoRepository.listar();
                var fornecedores = new FornecedorRepository().listar();
                var categorias = new CategoriaRepository().listar();

                UI.runOnUi(() -> {
                    this.produtos.addAll(produtosList);
                    this.categorias.set(categorias);
                    this.categoriaSelected.set(categorias.isEmpty() ? null : categorias.getFirst());

                    this.fornecedores.set(fornecedores);
                    this.fornecedorSelected.set(fornecedores.isEmpty() ? null : fornecedores.getFirst());

                    for(var p :produtosList){
                        var categoria = categorias.stream()
                                .filter(it-> it.id.equals(p.categoriaId))
                                .findFirst()
                                .orElse(null);

                        p.categoria = categoria;
                    }
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }
}

