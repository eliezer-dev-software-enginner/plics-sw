package my_app.screens.comprasScreen;

import megalodonte.ComputedState;
import megalodonte.v2.ListState;
import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.db.dto.CompraDto;
import my_app.db.models.CompraModel;
import my_app.db.models.FornecedorModel;
import my_app.db.models.ProdutoModel;
import my_app.db.services.CompraService;
import my_app.db.services.FornecedorService;
import my_app.db.services.ProdutoService;
import my_app.domain.Data;
import my_app.domain.Parcela;
import my_app.domain.states.TotaisState;
import my_app.core.events.DadosFinanceirosAtualizadosEvent;
import my_app.core.events.EventBus;
import my_app.domain.ViewModelScreenContract;
import my_app.domain.components.Components;
import my_app.services.ContasPagarService;
import my_app.utils.DateUtils;
import my_app.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ComprasScreenViewModel extends ViewModelScreenContract {

    private static final Logger log = LoggerFactory.getLogger(ComprasScreenViewModel.class);
    private final CompraService compraService;
    private final FornecedorService fornecedorService;
    private final ProdutoService produtoService;
    private final ContasPagarService contasPagarService;

    // --- Lista principal ---
    final ListState<CompraModel> compras = ListState.ofEmpty();

    // --- Form states ---
    final State<String> numeroNota = State.of("");
    final State<LocalDate> dataCompra = State.of(LocalDate.now());
    final State<String> codigo = State.of("");
    final State<String> qtd = State.of("0");
    final State<String> observacao = State.of("");

    final State<String> tipoPagamentoSelected = State.of(Data.tiposPagamentoList.get(1));
    final ComputedState<Boolean> tipoPagamentoSelectedIsAPrazo = ComputedState.of(
            () -> tipoPagamentoSelected.get().equals("A PRAZO"),
            tipoPagamentoSelected);

    final State<List<Parcela>> parcelas = State.of(List.of());
    final State<String> descontoEmDinheiro = State.of("0");
    final State<String> pcCompra = State.of("0");

    final TotaisState totais = new TotaisState(pcCompra, qtd, descontoEmDinheiro);

    final State<LocalDate> dataValidade = State.of(null);

    // --- fornecedores ---
    final ListState<FornecedorModel> fornecedores = ListState.ofEmpty();
    final State<FornecedorModel> fornecedorSelected = State.of(null);

    // --- Seleção na tabela ---
    final State<CompraModel> compraSelected = State.of(null);

    // --- Controle de estoque ---
    final State<String> opcaoEstoqueSelected = State.of(Data.simNaoList.get(0));
    final State<String> estoqueAnterior = State.of("0");
    final State<String> estoqueAtual = State.of("0");

    private final ListState<ProdutoModel> produtoModelListState = ListState.ofEmpty();
    final ListState<ProdutoModel> sugestoesProduto = ListState.ofEmpty();
    final State<ProdutoModel> produtoEncontrado = State.of(null);

    final ComputedState<Boolean> sugestoesProdutoVisible = ComputedState.of(
            () -> !sugestoesProduto.get().isEmpty(),
            sugestoesProduto
    );

    public ComprasScreenViewModel(ScreenContext ctx) {
        this(ctx, createCompraService(), createFornecedorService(), createProdutoService(), createContasPagarService());
    }

    public ComprasScreenViewModel(ScreenContext ctx, CompraService compraService, FornecedorService fornecedorService, ProdutoService produtoService, ContasPagarService contasPagarService) {
        super(ctx);
        this.compraService = compraService;
        this.fornecedorService = fornecedorService;
        this.produtoService = produtoService;
        this.contasPagarService = contasPagarService;
        this.onInit();
    }

    private static CompraService createCompraService() {
        try {
            return new CompraService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static FornecedorService createFornecedorService() {
        try {
            return new FornecedorService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static ProdutoService createProdutoService() {
        try {
            return new ProdutoService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static ContasPagarService createContasPagarService() {
        try {
            return new ContasPagarService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onInit() {
        qtd.subscribe(v -> atualizarEstoqueVisual());
        opcaoEstoqueSelected.subscribe(v -> atualizarEstoqueVisual());

        produtoEncontrado.subscribe(v -> atualizarEstoqueVisual());
        codigo.subscribe(termo -> filtrarProdutos(termo));

        produtoEncontrado.subscribe(this::selecionarProduto);
    }

    private void atualizarEstoqueVisual() {
        if (produtoEncontrado.get() == null) {
            estoqueAnterior.set("0");
            estoqueAtual.set("0");
            return;
        }

        BigDecimal estoqueBase = produtoEncontrado.get().getEstoque() != null ?
                produtoEncontrado.get().getEstoque() : BigDecimal.ZERO;

        IO.println("Estoque Base (anterior) " + estoqueBase);
        estoqueAnterior.set(estoqueBase.toString());

        if ("Sim".equals(opcaoEstoqueSelected.get())) {
            String qtdStr = qtd.get().trim();
            IO.println("Estoque Atual " + qtdStr);
            BigDecimal qtdValue = qtdStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(qtdStr);
            estoqueAtual.set(estoqueBase.add(qtdValue).toString());
        } else {
            estoqueAtual.set(estoqueBase.toString());
        }
    }

    private void filtrarProdutos(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            sugestoesProduto.clear();
            return;
        }

        var filtrados = produtoModelListState.get().stream()
                .filter(p -> p.getCodigoBarras().contains(termo.trim())
                        || p.getDescricao().toLowerCase().contains(termo.trim().toLowerCase()))
                .limit(8)
                .toList();

        sugestoesProduto.set(filtrados);
    }

    void selecionarProduto(ProdutoModel produto) {
        if (produto != null) {
            codigo.set(produto.getCodigoBarras());
            pcCompra.set(Utils.deRealParaCentavos(produto.getPrecoCompra()));
            estoqueAnterior.set(produto.getEstoque().toString());
            sugestoesProduto.clear();
            atualizarEstoqueVisual();
        }
    }

    void reloadProdutos() {
        try {
            var produtoList = new ProdutoService().listar();
            UI.runOnUi(() -> produtoModelListState.set(produtoList));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void populateFromModel() {
        final var data = compraSelected.get();
        if (data == null) return;

        modoEdicao.set(false);
        dataCompra.set(DateUtils.millisParaLocalDate(data.getDataCompra()));
        numeroNota.set(data.getNumeroNota());
        codigo.set(data.getProdutoCod());
        produtoEncontrado.set(null);
        qtd.set(Utils.quantidadeTratada(data.getQuantidade()));
        observacao.set(data.getObservacao());
        tipoPagamentoSelected.set(data.getTipoPagamento());
        pcCompra.set(Utils.deRealParaCentavos(data.getPrecoDeCompra()));
        dataValidade.set(data.getDataValidade() != null
                ? DateUtils.millisParaLocalDate(data.getDataValidade())
                : null);
    }

    public void fetchData() {
        Async.Run(() -> {
            try {
                var fornecedorModelList = fornecedorService.listar();
                fornecedores.addAll(fornecedorModelList);
                var listCompras = compraService.listar();
                var produtoList = produtoService.listar();

                UI.runOnUi(() -> {
                    produtoModelListState.set(produtoList);
                    if (!fornecedorModelList.isEmpty()) {
                        fornecedorModelList.stream().filter(f -> f.getId() == 1)
                                .findFirst()
                                .ifPresent(fornecedorSelected::set);
                    }

                    for (CompraModel compra : listCompras) {
                        FornecedorModel fornecedor = fornecedorModelList.stream()
                                .filter(f -> f.getId().equals(compra.getFornecedorId()))
                                .findFirst()
                                .orElse(null);
                        compra.setFornecedor(fornecedor);
                    }

                    compras.addAll(listCompras);
                });

            } catch (SQLException e) {
                log.error("Erro ao buscar compras", e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar compras: " + e.getMessage()));
            }
        });
    }

    @Override
    public void handleAddOrUpdate() {
        final var dtValidade = dataValidade.get() != null ?
                DateUtils.localDateParaMillis(dataValidade.get()) : null;

        final var dto = new CompraDto(
                codigo.get(),
                Utils.deCentavosParaReal(pcCompra.get()),
                fornecedorSelected.get().getId(),
                new BigDecimal(qtd.get()),
                Utils.deCentavosParaReal(descontoEmDinheiro.get()),
                tipoPagamentoSelected.get(), observacao.get(),
                DateUtils.localDateParaMillis(dataCompra.get()),
                numeroNota.get(),
                dtValidade,
                opcaoEstoqueSelected.get(),
                new BigDecimal(totais.totalLiquido.get())
        );

        Async.Run(() -> {
            if (modoEdicao.get()) {
                final var selecionado = compraSelected.get();
                if (selecionado == null) return;

                try {
                    var modelAtualizada = compraService.toModel(dto, selecionado.getId(), selecionado.getDataCriacaoMillis());
                    compraService.atualizar(modelAtualizada);

                    UI.runOnUi(() -> {
                        Components.ShowPopup(ctx, "Sua compra de mercadoria foi atualizada com sucesso!");
                        compras.updateIf(it -> it.getId() == selecionado.getId(), it -> modelAtualizada);
                        atualizarEstoqueAposOperacao(dto.produtoCod(), dto.quantidade());
                        reloadProdutos();
                        clearForm();
                    });
                } catch (SQLException e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao atualizar compra: " + e.getMessage()));
                }
            } else {
                try {
                    var compraSalva = compraService.salvar(dto);

                    if ("Sim".equalsIgnoreCase(opcaoEstoqueSelected.get())) {
                        produtoService.atualizarEstoque(dto.produtoCod(), dto.quantidade());
                    }

                    if ("A PRAZO".equals(tipoPagamentoSelected.get()) && !parcelas.get().isEmpty()) {
                        try {
                            List<Parcela> parcelasParaService = parcelas.get().stream()
                                    .map(p -> new Parcela(
                                            p.numero(),
                                            p.dataVencimento(),
                                            p.valor()
                                    ))
                                    .toList();
                            this.contasPagarService.gerarContasDeCompra(compraSalva, parcelasParaService);
                        } catch (SQLException e) {
                            throw new RuntimeException("Erro ao gerar contas a pagar: " + e.getMessage());
                        }
                    }

                    UI.runOnUi(() -> {
                        IO.println("compra foi salva!");
                        compras.add(compraSalva);
                        Components.ShowPopup(ctx, "Sua compra de mercadoria foi salva com sucesso!");
                        EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                        reloadProdutos();
                        clearForm();
                    });
                } catch (SQLException e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao salvar compra: " + e.getMessage()));
                }
            }
        });
    }

    private void atualizarEstoqueAposOperacao(String codigoBarras, BigDecimal quantidade) {
        if (!"Sim".equals(opcaoEstoqueSelected.get())) return;
        Async.Run(() -> {
            try {
                produtoService.atualizarEstoque(codigoBarras, quantidade);
            } catch (SQLException e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao atualizar estoque: " + e.getMessage()));
            }
        });
    }

    @Override
    public void handleClickMenuDelete() {
        modoEdicao.set(false);

        final var data = compraSelected.get();
        if (data != null) {
            Async.Run(() -> {
                try {
                    Long compraId = data.getId();

                    contasPagarService.excluirPorCompraId(compraId);

                    compraService.excluirById(compraId);

                    removerEstoqueProduto(data.getProdutoCod(), data.getQuantidade());

                    UI.runOnUi(() -> {
                        compras.removeIf(it -> it.getId() == compraId);
                        Components.ShowPopup(ctx, "Compra e contas vinculadas excluídas com sucesso!");
                        EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                    });

                } catch (SQLException e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao excluir compra: " + e.getMessage()));
                }
            });
        }
    }

    @Override
    public void clearForm() {
        dataCompra.set(LocalDate.now());
        numeroNota.set("");
        modoEdicao.set(false);
        codigo.set("");
        produtoEncontrado.set(null);
        qtd.set("0");
        observacao.set("");
        tipoPagamentoSelected.set(Data.tiposPagamentoList.get(1));
        pcCompra.set("0");
        dataValidade.set(null);
        fornecedorSelected.set(fornecedores.get(0));
        opcaoEstoqueSelected.set(Data.simNaoList.get(0));
        estoqueAnterior.set("0");
        estoqueAtual.set("0");
    }

    void removerEstoqueProduto(String codigoBarras, BigDecimal quantidade) {
        if (!"Sim".equals(opcaoEstoqueSelected.get())) {
            IO.println("Controle de estoque desativado para esta operação");
            return;
        }

        Async.Run(() -> {
            try {
                BigDecimal quantidadeParaRemover = quantidade.negate();
                produtoService.atualizarEstoque(codigoBarras, quantidadeParaRemover);
                IO.println("Estoque removido com sucesso para o produto: " + codigoBarras + " | Quantidade: " + quantidade);
                reloadProdutos();
            } catch (SQLException e) {
                IO.println("Erro ao remover estoque do produto " + codigoBarras + ": " + e.getMessage());
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao remover estoque: " + e.getMessage()));
            }
        });
    }

}
