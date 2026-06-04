package my_app.screens.vendaScreen;

import megalodonte.ComputedState;
import megalodonte.v2.ListState;
import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.ClienteModel;
import my_app.db.models.ProdutoModel;
import my_app.db.models.VendaModel;
import my_app.db.services.ClienteService;
import my_app.db.services.ContaAreceberService;
import my_app.db.services.ProdutoService;
import my_app.db.services.VendaService;
import my_app.domain.Data;
import my_app.domain.Parcela;
import my_app.domain.states.TotaisState;
import my_app.core.events.DadosFinanceirosAtualizadosEvent;
import my_app.core.events.EventBus;
import my_app.domain.ViewModelScreenContract;
import my_app.domain.components.Components;
import my_app.utils.DateUtils;
import my_app.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class VendaMercadoriaScreenViewModel extends ViewModelScreenContract {

    private static final Logger log = LoggerFactory.getLogger(VendaMercadoriaScreenViewModel.class);
    private final VendaService vendaService;
    private final ProdutoService produtoService;
    private final ClienteService clienteService;
    private final ContaAreceberService contaService;

    final ListState<VendaModel> vendas = ListState.ofEmpty();

    final State<LocalDate> dataVenda = State.of(LocalDate.now());
    final State<String> numeroNota = State.of("");

    final State<String> qtd = State.of("0");
    final State<String> observacao = State.of("");

    final State<String> tipoPagamentoSelecionado = State.of(Data.tiposPagamentoList.get(1));
    final ComputedState<Boolean> tipoPagamentoIsAPrazo = ComputedState.of(
            () -> tipoPagamentoSelecionado.get().equals("A PRAZO"),
            tipoPagamentoSelecionado);

    final State<List<Parcela>> parcelas = State.of(List.of());
    final State<String> descontoEmDinheiro = State.of("0");
    final State<String> pcVenda = State.of("0");

    final State<LocalDate> dataValidade = State.of(null);

    final ListState<ClienteModel> clientes = ListState.ofEmpty();
    final State<ClienteModel> clienteSelected = State.of(null);

    final State<VendaModel> vendaSelected = State.of(null);

    final List<String> opcoesEstoque = List.of("Sim", "Não");
    final State<String> opcaoEstoqueSelected = State.of(opcoesEstoque.get(0));
    final State<String> estoqueAnterior = State.of("0");
    final State<String> estoqueAtual = State.of("0");

    private final ListState<ProdutoModel> produtoModelListState = ListState.ofEmpty();
    final ListState<ProdutoModel> sugestoesProduto = ListState.ofEmpty();
    final State<ProdutoModel> produtoEncontrado = State.of(null);
    final State<String> codigo = State.of("");

    final ComputedState<Boolean> sugestoesProdutoVisible = ComputedState.of(
            () -> !sugestoesProduto.get().isEmpty(),
            sugestoesProduto
    );

    final TotaisState totais = new TotaisState(pcVenda, qtd, descontoEmDinheiro);

    public VendaMercadoriaScreenViewModel(ScreenContext ctx) {
        super(ctx);
        try {
            this.vendaService = new VendaService();
            this.produtoService = new ProdutoService();
            this.clienteService = new ClienteService();
            this.contaService = new ContaAreceberService();
        } catch (SQLException e) {
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            throw new RuntimeException(e);
        }
        this.onInit();
    }

    @Override
    protected void onInit() {
        qtd.subscribe(v -> atualizarEstoqueVisual());
        opcaoEstoqueSelected.subscribe(v -> atualizarEstoqueVisual());
        codigo.subscribe(termo -> filtrarProdutos(termo));

        produtoEncontrado.subscribe(this::selecionarProduto);
    }

    void filtrarProdutos(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            sugestoesProduto.clear();
            return;
        }

        produtoEncontrado.set(null);

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
            pcVenda.set(Utils.deRealParaCentavos(produto.getPrecoVenda()));
            estoqueAnterior.set(produto.getEstoque().toString());
            sugestoesProduto.clear();
        }
    }

    @Override
    public void populateFromModel() {
        final var data = vendaSelected.get();
        if (data == null) return;

        modoEdicao.set(false);
        dataVenda.set(DateUtils.millisParaLocalDate(data.getDataVenda()));
        numeroNota.set(data.getNumeroNota());
        codigo.set(data.getProdutoCod());
        produtoEncontrado.set(null);
        qtd.set(Utils.quantidadeTratada(data.getQuantidade()));
        observacao.set(data.getObservacao());
        tipoPagamentoSelecionado.set(data.getTipoPagamento());
        pcVenda.set(Utils.deRealParaCentavos(data.getPrecoUnitario()));
        dataValidade.set(data.getDataValidade() != null
                ? DateUtils.millisParaLocalDate(data.getDataValidade())
                : null);
    }

    void reloadProdutos() {
        try {
            var produtoList = produtoService.listar();
            UI.runOnUi(() -> produtoModelListState.set(produtoList));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    void fetchData() {
        Async.Run(() -> {
            try {
                var clienteList = clienteService.listar();
                var vendaList = vendaService.listar();
                var produtoList = produtoService.listar();

                UI.runOnUi(() -> {
                    produtoModelListState.set(produtoList);
                    clientes.addAll(clienteList);
                    clienteList.stream()
                            .filter(f -> f.getId() == 1)
                            .findFirst()
                            .ifPresent(clienteSelected::set);

                    for (var venda : vendaList) {
                        venda.setCliente(clienteList.stream()
                                .filter(it -> it.getId().equals(venda.getClienteId()))
                                .findFirst()
                                .orElse(null));
                    }
                    vendas.addAll(vendaList);
                });

            } catch (SQLException e) {
                log.error("Erro ao buscar vendas", e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar vendas: " + e.getMessage()));
            }
        });
    }

    @Override
    public void handleAddOrUpdate() {
        if (produtoEncontrado.get() == null) {
            Components.ShowAlertError("Produto não encontrado!");
            return;
        }

        Async.Run(() -> {
            if (modoEdicao.get()) {
                final var selecionado = vendaSelected.get();
                if (selecionado == null) return;

                fillModelFromForm(selecionado, false);
                try {
                    vendaService.atualizar(selecionado);
                } catch (SQLException e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao atualizar: " + e.getMessage()));
                    return;
                }

                UI.runOnUi(() -> {
                    vendas.updateIf(it -> it.getId().equals(selecionado.getId()), it -> selecionado);
                    Components.ShowPopup(ctx, "Venda atualizada com sucesso!");
                    reloadProdutos();
                    clearForm();
                });
            } else {
                var model = new VendaModel();
                fillModelFromForm(model, true);
                boolean atualizarEstoque = opcaoEstoqueSelected.get().equalsIgnoreCase("Sim");

                VendaModel salvo;
                try {
                    salvo = vendaService.salvar(model, atualizarEstoque);
                } catch (SQLException e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao salvar venda: " + e.getMessage()));
                    return;
                }

                if ("A PRAZO".equals(tipoPagamentoSelecionado.get()) && !parcelas.get().isEmpty()) {
                    try {
                        contaService.gerarContasDeVenda(salvo.getId(), salvo.getClienteId(), parcelas.get());
                    } catch (SQLException e) {
                        UI.runOnUi(() -> Components.ShowAlertError("Erro ao gerar contas: " + e.getMessage()));
                        return;
                    }
                }

                VendaModel finalVenda = salvo;
                UI.runOnUi(() -> {
                    vendas.add(finalVenda);
                    Components.ShowPopup(ctx, "Venda salva com sucesso!");
                    clearForm();
                    EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                    reloadProdutos();
                    clearForm();
                });
            }
        });
    }

    @Override
    public void handleClickMenuDelete() {
        final var data = vendaSelected.get();
        if (data == null) return;

        Async.Run(() -> {
            try {
                Integer vendaId = data.getId();
                contaService.excluirPorVendaId(vendaId);
                vendaService.excluir(vendaId, opcaoEstoqueSelected.get().equals("Sim"));
                reloadProdutos();

                UI.runOnUi(() -> {
                    vendas.removeIf(it -> it.getId().equals(vendaId));
                    Components.ShowPopup(ctx, "Venda e contas vinculadas excluídas!");
                });
            } catch (SQLException e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao excluir: " + e.getMessage()));
            }
        });
    }

    @Override
    public void clearForm() {
        dataVenda.set(LocalDate.now());
        numeroNota.set("");
        modoEdicao.set(false);
        codigo.set("");
        produtoEncontrado.set(null);
        qtd.set("");
        observacao.set("");
        tipoPagamentoSelecionado.set(Data.tiposPagamentoList.get(1));
        pcVenda.set("0");
        dataValidade.set(null);
        if (!clientes.get().isEmpty()) {
            clienteSelected.set(clientes.get().getFirst());
        }
        opcaoEstoqueSelected.set("Não");
        estoqueAnterior.set("0");
        estoqueAtual.set("0");
    }

    private void fillModelFromForm(VendaModel model, boolean isNew) {
        model.setProdutoCod(produtoEncontrado.get().getCodigoBarras());
        model.setClienteId(clienteSelected.get() != null ? clienteSelected.get().getId() : null);
        model.setQuantidade(new BigDecimal(qtd.get()));
        model.setPrecoUnitario(Utils.deCentavosParaReal(pcVenda.get()));
        model.setDesconto(Utils.deCentavosParaReal(descontoEmDinheiro.get()));
        model.setTipoPagamento(tipoPagamentoSelecionado.get());
        model.setObservacao(observacao.get());
        model.setTotalLiquido(new BigDecimal(totais.totalLiquido.get()));
        model.setDataValidade(dataValidade.get() != null ? DateUtils.localDateParaMillis(dataValidade.get()) : null);
        model.setDataVenda(DateUtils.localDateParaMillis(dataVenda.get()));
        model.setNumeroNota(numeroNota.get());
        if (isNew) {
            model.setTotalLiquido(new BigDecimal(totais.totalLiquido.get()));
        }
    }

    private void atualizarEstoqueVisual() {
        if (produtoEncontrado.get() == null) {
            estoqueAnterior.set("0");
            estoqueAtual.set("0");
            return;
        }

        var estoqueBase = produtoEncontrado.get().getEstoque() != null
                ? produtoEncontrado.get().getEstoque()
                : BigDecimal.ZERO;

        estoqueAnterior.set(estoqueBase.toString());

        if ("Sim".equals(opcaoEstoqueSelected.get())) {
            try {
                int qtdValue = Integer.parseInt(qtd.get().trim().isEmpty() ? "0" : qtd.get());
                estoqueAtual.set(estoqueBase.subtract(BigDecimal.valueOf(qtdValue)).toString());
            } catch (NumberFormatException e) {
                estoqueAtual.set(estoqueBase.toString());
            }
        } else {
            estoqueAtual.set(estoqueBase.toString());
        }
    }
}
