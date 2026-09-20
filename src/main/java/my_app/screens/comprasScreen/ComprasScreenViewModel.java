package my_app.screens.comprasScreen;

import megalodonte.ComputedState;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.base.state.State;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.v2.ListState;
import my_app.core.AppRoutes;
import my_app.core.events.DadosFinanceirosAtualizadosEvent;
import my_app.core.events.EntityEvent;
import my_app.core.events.EventBus;
import my_app.core.db.dto.CompraDto;
import my_app.core.db.models.CompraModel;
import my_app.core.db.models.FornecedorModel;
import my_app.core.db.models.ProdutoModel;
import my_app.core.db.services.CompraService;
import my_app.core.db.services.FornecedorService;
import my_app.core.db.services.ProdutoService;
import my_app.core.Data;
import my_app.core.Parcela;
import my_app.core.ViewModelScreenContract;
import my_app.core.components.Components;
import my_app.core.states.TotaisState;
import my_app.services.ContasPagarService;
import my_app.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pack.utilities.CurrencyPack;
import pack.utilities.DatePack;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

public class ComprasScreenViewModel extends ViewModelScreenContract<CompraModel> {

    private static final Logger log = LoggerFactory.getLogger(ComprasScreenViewModel.class);
    private final CompraService compraService;
    private final FornecedorService fornecedorService;
    private final ProdutoService produtoService;
    private final ContasPagarService contasPagarService;

    private final Consumer<Object> eventListener = this::onEntityEvent;

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

    // --- fornecedores (para o select do form) ---
    final ListState<FornecedorModel> fornecedores = ListState.ofEmpty();
    final State<FornecedorModel> fornecedorSelected = State.of(null);

    // --- Controle de estoque ---
    final State<String> opcaoEstoqueSelected = State.of(Data.simNaoList.getFirst());
    final State<String> estoqueAnterior = State.of("0");
    final State<String> estoqueAtual = State.of("0");

    private final ListState<ProdutoModel> produtoModelListState = ListState.ofEmpty();
    final ListState<ProdutoModel> sugestoesProduto = ListState.ofEmpty();
    final State<ProdutoModel> produtoEncontrado = State.of(null);

    final ComputedState<Boolean> sugestoesProdutoVisible = ComputedState.of(
            () -> !sugestoesProduto.get().isEmpty(),
            sugestoesProduto
    );

    public final Components.InputRef quantidadeRef = new Components.InputRef();

    public ComprasScreenViewModel(ScreenContextInterface ctx) {
        super(ctx);
        screenNameSpawn = AppRoutes.Screens.ADD_OR_EDIT_COMPRAS.name();
        this.compraService = createOrReport(CompraService::new);
        this.fornecedorService = createOrReport(FornecedorService::new);
        this.produtoService = createOrReport(ProdutoService::new);
        this.contasPagarService = createOrReport(ContasPagarService::new);
        this.onInit();
    }

    @Override
    protected boolean matchesSearch(CompraModel model, String query) {
        return (model.getProdutoModel() != null && contains(model.getProdutoModel().getDescricao(), query))
                || (model.getFornecedor() != null && contains(model.getFornecedor().getNome(), query))
                || contains(model.getNumeroNota(), query);
    }

    private boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    protected void onInit() {
        qtd.subscribe(v -> atualizarEstoqueVisual());
        opcaoEstoqueSelected.subscribe(v -> atualizarEstoqueVisual());

        produtoEncontrado.subscribe(v -> atualizarEstoqueVisual());
        codigo.subscribe(this::filtrarProdutos);

        produtoEncontrado.subscribe(this::selecionarProduto);

        EventBus.getInstance().subscribe(eventListener);
    }

    private void onEntityEvent(Object event) {
        if (event instanceof EntityEvent<?> ee && ee.entity() instanceof FornecedorModel) {
            refreshFornecedores();
        }
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
            quantidadeRef.requestFocus();
        }
    }

    void reloadProdutos() {
        try {
            var produtoList = produtoService.listar();
            UI.runOnUi(() -> produtoModelListState.set(produtoList));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void refreshFornecedores() {
        try {
            var fornecedorList = fornecedorService.listar();
            UI.runOnUi(() -> {
                fornecedores.clear();
                fornecedores.addAll(fornecedorList);
            });
        } catch (Exception e) {
            log.error("Erro ao recarregar fornecedores", e);
        }
    }

    @Override
    public void populateFieldsFromModel() {
        final var data = selected.get();
        if (data == null) return;

        // A edição/clone carrega a compra por ID (ScreenAddOrEdit.buscarById), e esse
        // repository não hidrata as relações: fornecedor/produto vêm null (só os IDs).
        // Resolve os dois no banco fora da FX thread — senão produtoEncontrado fica null
        // (o codigo.set mais embaixo dispara filtrarProdutos() que não teria produto pra
        // setar) e fornecedorSelected fica null, apagando o fornecedor no salvar de uma
        // edição. É a mesma hidratação que fetchListData() já faz na listagem.
        Async.Run(() -> {
            FornecedorModel fornecedor = data.getFornecedor();
            ProdutoModel produto = data.getProdutoModel();
            try {
                if (produto == null && data.getProdutoCod() != null) {
                    produto = produtoService.buscarPorCodigoBarras(data.getProdutoCod());
                }
                if (fornecedor == null && data.getFornecedorId() != null) {
                    fornecedor = fornecedorService.buscarById(data.getFornecedorId());
                }
            } catch (Exception e) {
                log.error("Erro ao hidratar produto/fornecedor da compra id={}", data.getId(), e);
            }
            final var fornecedorResolvido = fornecedor;
            final var produtoResolvido = produto;

            UI.runOnUi(() -> {
                // produtoEncontrado.set() dispara selecionarProduto(), que já seta o
                // codigo/pcCompra — os sets explícitos logo abaixo sobrescrevem com os
                // valores de fato salvos nesta compra.
                produtoEncontrado.set(produtoResolvido);
                fornecedorSelected.set(fornecedorResolvido);
                codigo.set(produtoResolvido != null ? produtoResolvido.getCodigoBarras() : data.getProdutoCod());

                dataCompra.set(DatePack.millisParaLocalDate(data.getDataCompra()));
                numeroNota.set(data.getNumeroNota());
                qtd.set(Utils.quantidadeTratada(data.getQuantidade()));
                observacao.set(data.getObservacao());
                tipoPagamentoSelected.set(data.getTipoPagamento());
                pcCompra.set(Utils.deRealParaCentavos(data.getPrecoDeCompra()));
                descontoEmDinheiro.set(Utils.deRealParaCentavos(data.getDescontoEmReais()));
                dataValidade.set(data.getDataValidade() != null
                        ? DatePack.millisParaLocalDate(data.getDataValidade())
                        : null);
            });
        });
    }

    @Override
    public void fetchListData() {
        Async.Run(() -> {
            try {
                var fornecedorModelList = fornecedorService.listar();
                var listCompras = compraService.listar();
                var produtoList = produtoService.listar();

                for (CompraModel compra : listCompras) {
                    FornecedorModel fornecedor = fornecedorModelList.stream()
                            .filter(f -> f.getId().equals(compra.getFornecedorId()))
                            .findFirst()
                            .orElse(null);
                    ProdutoModel produtoModel = produtoList.stream()
                            .filter(f -> f.getCodigoBarras().equals(compra.getProdutoCod()))
                            .findFirst()
                            .orElse(null);

                    compra.setFornecedor(fornecedor);
                    compra.setProdutoModel(produtoModel);
                }

                UI.runOnUi(() -> {
                    produtoModelListState.set(produtoList);
                    fornecedores.clear();
                    fornecedores.addAll(fornecedorModelList);
                    if (!fornecedorModelList.isEmpty()) {
                        fornecedorModelList.stream().filter(f -> f.getId() == 1)
                                .findFirst()
                                .ifPresent(fornecedorSelected::set);
                    }
                    allDataList.set(listCompras);
                });

            } catch (Exception e) {
                log.error("Erro ao buscar compras", e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar compras: " + e.getMessage()));
            }
        });
    }

    @Override
    public void handleAddOrUpdate() {
        var model = populateModelFromFields();
        // Capturado aqui, síncrono (thread da UI) — ScreenContract.handleAddOrUpdate()
        // reseta modoEdicao logo depois de disparar essa chamada, então ler
        // isEditing só depois de já estar rodando na thread virtual do
        // Async.Run abaixo quase sempre lia o valor já resetado, transformando
        // toda edição em cadastro novo (mesmo bug corrigido em outras telas).
        boolean editando = isEditing;

        Async.Run(() -> {
            if (editando) {
                final var selecionado = selected.get();
                if (selecionado == null) return;

                try {
                    compraService.atualizar(model);

                    UI.runOnUi(() -> {
                        Components.ShowPopup(ctx, "Sua compra de mercadoria foi atualizada com sucesso!");
                        model.setFornecedor(fornecedorSelected.get());
                        allDataList.updateIf(it -> it.getId() == selecionado.getId(), it -> model);
                        EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                        atualizarEstoqueAposOperacao(model.getProdutoCod(), model.getQuantidade());
                        reloadProdutos();
                        clearForm();
                    });
                } catch (Exception e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao atualizar compra: " + e.getMessage()));
                }
            } else {
                try {
                    var compraSalva = compraService.salvar(model);

                    if ("Sim".equalsIgnoreCase(opcaoEstoqueSelected.get())) {
                        produtoService.atualizarEstoque(compraSalva.getProdutoCod(), compraSalva.getQuantidade());
                    }

                    if ("A PRAZO".equals(tipoPagamentoSelected.get())) {
                        try {
                            List<Parcela> parcelasParaService = parcelas.get().stream()
                                    .map(p -> new Parcela(
                                            p.numero(),
                                            p.dataVencimento(),
                                            p.valor()
                                    ))
                                    .toList();
                            this.contasPagarService.gerarContasDeCompra(compraSalva, parcelasParaService);
                        } catch (Exception e) {
                            throw new RuntimeException("Erro ao gerar contas a pagar: " + e.getMessage());
                        }
                    }

                    UI.runOnUi(() -> {
                        IO.println("compra foi salva!");
                        compraSalva.setFornecedor(fornecedorSelected.get());
                        allDataList.add(compraSalva);
                        Components.ShowPopup(ctx, "Sua compra de mercadoria foi salva com sucesso!");
                        EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                        reloadProdutos();
                        clearForm();
                    });
                } catch (Exception e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao salvar compra: " + e.getMessage()));
                }
            }
        });
    }

    @Override
    public CompraModel populateModelFromFields() {
        final var dtValidade = dataValidade.get() != null ?
                DatePack.localDateParaMillis(dataValidade.get()) : null;

        var dto = new CompraDto(
                codigo.get(),
                CurrencyPack.deCentavosParaReal(pcCompra.get()),
                fornecedorSelected.get() != null ? fornecedorSelected.get().getId() : null,
                new BigDecimal(qtd.get()),
                CurrencyPack.deCentavosParaReal(descontoEmDinheiro.get()),
                tipoPagamentoSelected.get(), observacao.get(),
                DatePack.localDateParaMillis(dataCompra.get()),
                numeroNota.get(),
                dtValidade,
                opcaoEstoqueSelected.get(),
                new BigDecimal(totais.totalLiquido.get())
        );

        if (isEditing && selected.get() != null) {
            var selecionado = selected.get();
            return compraService.toModel(dto, selecionado.getId(), selecionado.getDataCriacaoMillis());
        }
        return compraService.toModel(dto);
    }

    private void atualizarEstoqueAposOperacao(String codigoBarras, BigDecimal quantidade) {
        if (!"Sim".equals(opcaoEstoqueSelected.get())) return;
        Async.Run(() -> {
            try {
                produtoService.atualizarEstoque(codigoBarras, quantidade);
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao atualizar estoque: " + e.getMessage()));
            }
        });
    }

    @Override
    public void handleClickMenuDelete() {
        final var data = selected.get();
        if (data != null) {
            Async.Run(() -> {
                try {
                    long compraId = data.getId();

                    contasPagarService.excluirPorCompraId(compraId);

                    compraService.excluirById(compraId);

                    removerEstoqueProduto(data.getProdutoCod(), data.getQuantidade());

                    UI.runOnUi(() -> {
                        allDataList.removeIf(it -> it.getId() == compraId);
                        Components.ShowPopup(ctx, "Compra e contas vinculadas excluídas com sucesso!");
                        EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                    });

                } catch (Exception e ) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao excluir compra: " + e.getMessage()));
                }
            });
        }
    }

    @Override
    public void clearForm() {
        dataCompra.set(LocalDate.now());
        numeroNota.set("");
        codigo.set("");
        produtoEncontrado.set(null);
        qtd.set("0");
        observacao.set("");
        tipoPagamentoSelected.set(Data.tiposPagamentoList.get(1));
        pcCompra.set("0");
        dataValidade.set(null);
        fornecedorSelected.set(fornecedores.get(0));
        opcaoEstoqueSelected.set(Data.simNaoList.getFirst());
        estoqueAnterior.set("0");
        estoqueAtual.set("0");
        descontoEmDinheiro.set("0");
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
            } catch (Exception e) {
                IO.println("Erro ao remover estoque do produto " + codigoBarras + ": " + e.getMessage());
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao remover estoque: " + e.getMessage()));
            }
        });
    }


    @Override
    public void onDestroy() throws Exception {
        EventBus.getInstance().unsubscribe(eventListener);
        this.compraService.close();
        this.produtoService.close();
        this.fornecedorService.close();
        //TODO: chamar close()
       // this.contasPagarService.close();
    }
}
