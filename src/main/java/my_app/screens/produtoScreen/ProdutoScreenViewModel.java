package my_app.screens.produtoScreen;

import megalodonte.State;
import my_app.db.models.Models;
import my_app.db.models.Models.Produto;
import my_app.lifecycle.viewmodel.component.ViewModel;
import my_app.services.ProdutoService;

import java.math.BigDecimal;
import java.util.List;

public class ProdutoScreenViewModel extends ViewModel {

    private final ProdutoService service = new ProdutoService();
    public final State<String> codigoBarras = new State<>("123456789");
    public final State<String> descricao = new State<>("");
    public final State<String> precoCompra = new State<>("0");

    // depois vira ComputedState
    public final State<String> margem = new State<>("0");
    public final State<String> lucro = new State<>("0");

    public final State<String> precoVenda = new State<>("0");

    public final State<String> comissao = new State<>("");
    public final State<String> garantia = new State<>("");
    public final State<String> marca = new State<>("");

    public final List<String> unidades = List.of("UN","KG","ml");
    public final State<String> unidadeSelected = new State<>("UN");

    public final List<String> categorias = List.of("Padrão");
    public final State<String> categoriaSelected = new State<>("Padrão");

    public final List<String> fornecedores = List.of("Fornecedor Padrão");
    public final State<String> fornecedorSelected = new State<>("Fornecedor Padrão");

    public final State<String> observacoes = new State<>("");
    public final State<String> estoque = new State<>("0");
    public final State<String> validade = new State<>("");

    public final State<String> imagem = new State<>("/assets/produto-generico.png");

    public Produto toProduto() {
        Produto p = new Produto();
        p.codigoBarras = codigoBarras.get();
        p.descricao = descricao.get();
        p.precoCompra = new BigDecimal(precoCompra.get());
        p.precoVenda = new BigDecimal(precoVenda.get());
        p.unidade = unidadeSelected.get();
        p.categoria = categoriaSelected.get();
        p.fornecedor = fornecedorSelected.get();
        p.estoque = Integer.parseInt(estoque.get());
        p.observacoes = observacoes.get();
        p.imagem = imagem.get();
        return p;
    }

    public void carregar(Produto p) {
        descricao.set(p.descricao);
        precoCompra.set(p.precoCompra.toString());
        precoVenda.set(p.precoVenda.toString());
        unidadeSelected.set(p.unidade);
        categoriaSelected.set(p.categoria);
        fornecedorSelected.set(p.fornecedor);
        estoque.set(String.valueOf(p.estoque));
        observacoes.set(p.observacoes);
        imagem.set(p.imagem);
    }

    public void salvar() throws Exception {
        service.salvar(toProduto());
    }

    public void atualizar() throws Exception {
        service.atualizar(toProduto());
    }

    public void excluir() throws Exception {
        service.excluir(codigoBarras.get());
    }

    public void buscar() throws Exception {
        Produto p = service.buscar(codigoBarras.get());
        if (p != null) carregar(p);
    }

    public ProdutoScreenViewModel() {
        onInit();
    }
}

