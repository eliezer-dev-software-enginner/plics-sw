package my_app.screens.pdvScreen;

import megalodonte.ComputedState;
import megalodonte.State;
import megalodonte.v2.ListState;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.ProdutoModel;
import my_app.db.repositories.ProdutoRepository;
import my_app.lifecycle.viewmodel.component.ViewModelv2;
import my_app.screens.components.Components;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class PDVScreenViewModel extends ViewModelv2 {

    private final ScreenContext ctx;
    private final ProdutoRepository produtoRepository;

    // Cache de todos os produtos (lookup por código)
    private final Map<String, ProdutoModel> produtosCache = new HashMap<>();

    // Estado reativo: itens no carrinho
    final ListState<ItemVenda> itensCarrinho = ListState.ofEmpty();


    // Estado do campo de busca
    final State<String> codigoBarrasInput = State.of("");

    //item atual buscado
    final State<String> quantidadeInput = State.of("0");

    // Subtotal derivado — recalculado sempre que itensCarrinho mudar
    final State<String> subtotal = State.of("0");

    final State<String> totalRecebido = State.of("0");
    final State<String> troco         = State.of("0");



    public PDVScreenViewModel(ScreenContext ctx) {
        this.ctx = ctx;
        this.produtoRepository = new ProdutoRepository();
        this.onInit();
    }

    @Override
    protected void onInit() {
        // Sempre que o carrinho mudar, recalcula o subtotal
        itensCarrinho.onChange(itens -> {
            BigDecimal total = itens.stream()
                    .map(ItemVenda::totalItem)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            subtotal.set(total.toPlainString());
        });

        totalRecebido.subscribe(recebido -> {
            try {
                BigDecimal recebidoBD = new BigDecimal(recebido);
                BigDecimal subtotalBD = new BigDecimal(subtotal.get());
                BigDecimal t = recebidoBD.subtract(subtotalBD);
                troco.set(t.compareTo(BigDecimal.ZERO) < 0 ? "0" : t.toPlainString());
            } catch (NumberFormatException e) {
                troco.set("0");
            }
        });
    }

    void loadProdutos() {
        Async.Run(() -> {
            try {
                var list = produtoRepository.listar();

                System.out.println("=== loadProdutos ===");
                System.out.println("Lista retornada: " + list.size());
                if (!list.isEmpty()) {
                    System.out.println("Primeiro produto: " + list.get(0).descricao);
                }

                UI.runOnUi(() -> {
                    produtosCache.clear();
                    list.forEach(p -> produtosCache.put(p.codigoBarras, p));
                    System.out.println("Cache populado com: " + produtosCache.size() + " produtos");
                });
            } catch (Exception e) {
                e.printStackTrace(); // <- isso é importante, pode estar engolindo exceção
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar produtos: " + e.getMessage()));
            }
        });
    }

    void adicionarPorCodigo(String codigo) {
        var produto = produtosCache.get(codigo.trim());
        if (produto == null) {
            Components.ShowAlertError("Produto não encontrado: " + codigo);
            return;
        }

        // Se já existe no carrinho, incrementa a qtd
        var existente = itensCarrinho.get().stream()
                .filter(i -> i.produto.codigoBarras.equals(codigo))
                .findFirst();

        if (existente.isPresent()) {
            existente.get().quantidade = existente.get().quantidade.add(BigDecimal.ONE);
            itensCarrinho.refresh(); // notifica observers sem substituir a lista
        } else {
            var item = new ItemVenda(produto);
            item.quantidade = new BigDecimal(quantidadeInput.get());
            itensCarrinho.add(item);
        }

        codigoBarrasInput.set(""); // limpa o campo após adicionar
        quantidadeInput.set("0");
    }

    void atualizarQuantidade(ItemVenda item, BigDecimal novaQtd) {
        if (novaQtd.compareTo(BigDecimal.ZERO) <= 0) {
            itensCarrinho.remove(item);
        } else {
            item.quantidade = novaQtd;
            itensCarrinho.refresh();
        }
    }

    void removerItem(ItemVenda item) {
        itensCarrinho.remove(item);
    }
}