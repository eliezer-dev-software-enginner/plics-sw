package my_app.screens.produtoScreen;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import megalodonte.ComputedState;
import megalodonte.State;
import my_app.db.dto.ProdutoDto;
import my_app.db.models.CategoriaModel;
import my_app.db.models.ProdutoModel;
import my_app.db.repositories.CategoriaRepository;
import my_app.db.repositories.ProdutoRepository;
import my_app.lifecycle.viewmodel.component.ViewModel;
import my_app.services.ProdutoService;
import my_app.utils.Utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProdutoScreenViewModel extends ViewModel {

    private final ProdutoService service = new ProdutoService();
    private final ProdutoRepository produtoRepository = new ProdutoRepository();
    public final ObservableList<ProdutoModel> produtos = FXCollections.observableArrayList();
    public final State<String> codigoBarras = new State<>("123456789");
    public final State<String> descricao = new State<>("teste");
    public final State<String> precoCompra = new State<>("0");
    public final State<String> precoVenda = new State<>("0");

    // depois vira ComputedState
    public final State<String> margem = new State<>("0");
    public final State<String> lucro = new State<>("0");

    public final State<String> comissao = new State<>("");
    public final State<String> garantia = new State<>("2 meses");
    public final State<String> marca = new State<>("nike");

    public final List<String> unidades = List.of("UN","KG","ml");
    public final State<String> unidadeSelected = new State<>("UN");

    public final State<List<String>> categorias = new State<>(List.of("Padrão"));
    public final State<String> categoriaSelected = new State<>("Padrão");

    //TODO: BUSCAR FORNECEDORES DO BANCO
    public final List<String> fornecedores = List.of("Fornecedor Padrão");
    public final State<String> fornecedorSelected = new State<>("Fornecedor Padrão");

    public final State<String> observacoes = new State<>("nada ...");
    public final State<String> estoque = new State<>("10");
    public final State<String> validade = new State<>("");

    public final State<String> imagem = new State<>("/assets/produto-generico.png");

    public final State<Boolean> modoEdicao = State.of(false);
    public final ComputedState<String> btnText = ComputedState.of(()-> modoEdicao.get()? "Atualizar": "+ Adicionar", modoEdicao);
    public final State<ProdutoModel> produtoSelected = State.of(null);

    public ProdutoDto toProduto() {
        var p = new ProdutoDto();
        p.codigoBarras = codigoBarras.get();
        p.descricao = descricao.get();

        // Converte de centavos para reais
        p.precoCompra = Utils.deCentavosParaReal(precoCompra.get());
        p.precoVenda = Utils.deCentavosParaReal(precoVenda.get());
        p.unidade = unidadeSelected.get();
        p.categoriaId = 1L;   // temporário
        p.fornecedorId = 1L;  // temporário
        p.estoque = Utils.deCentavosParaReal(estoque.get());
        p.observacoes = observacoes.get();
        p.imagem = imagem.get();
        p.marca = marca.get();
        return p;
    }

    public void carregar(ProdutoModel p) {
        codigoBarras.set(p.codigoBarras);
        descricao.set(p.descricao);
        //convertando pra centavos
        precoCompra.set(Utils.deRealParaCentavos(p.precoCompra));
        precoVenda.set(Utils.deRealParaCentavos(p.precoVenda));
        
        // Converte reais para centavos para o raw state
        unidadeSelected.set(p.unidade);
        //TODO: rever carregamento da categoria
        //categoriaSelected.set(p.categoria.id.toString());
        fornecedorSelected.set(String.valueOf(p.fornecedorId));
        estoque.set(String.valueOf(p.estoque));
        observacoes.set(p.observacoes);
        imagem.set(p.imagem);
        marca.set(p.marca);
    }

    public void salvar() throws Exception {
        if(modoEdicao.get()){
            //TODO: implementar
            return;
        }

        service.salvar(toProduto());
    }

    public void atualizar() throws Exception {
        var dto = toProduto();
        service.atualizar(new ProdutoModel().fromIdAndDto(produtoSelected.get().id, dto));
    }

    public void excluir() throws Exception {
        service.excluir(produtoSelected.get().id);
    }

    public void buscar() throws Exception {
        var p = service.buscar(codigoBarras.get());
        if (p != null) carregar(p);
    }

    public ProdutoScreenViewModel() {
        onInit();
    }

    protected void onInit() {
        loadCategorias();
        loadProdutos();
    }

    private void loadCategorias() {
        try {
            CategoriaRepository repo = new CategoriaRepository();
            List<CategoriaModel> categoriasModel = repo.listar();
            List<String> nomesCategorias = categoriasModel.stream()
                    .map(c -> c.nome)
                    .toList();
            
            if (!nomesCategorias.isEmpty()) {
                categorias.set(nomesCategorias);
                categoriaSelected.set(nomesCategorias.get(0)); // Seleciona primeira
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar categorias: " + e.getMessage());
            // Mantém lista padrão em caso de erro
        }
    }

    private void loadProdutos() {
        try {
            produtos.clear();
            produtos.addAll(produtoRepository.listar());
        } catch (Exception e) {
            System.err.println("Erro ao carregar produtos: " + e.getMessage());
        }
    }

    public void refreshProdutos() {
        loadProdutos();
    }
}

