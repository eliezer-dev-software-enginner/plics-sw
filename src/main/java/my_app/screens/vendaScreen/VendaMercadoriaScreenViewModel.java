package my_app.screens.vendaScreen;

import megalodonte.ComputedState;
import megalodonte.v2.ListState;
import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.*;
import my_app.db.services.*;
import my_app.domain.Data;
import my_app.domain.Parcela;
import my_app.domain.states.TotaisState;
import my_app.core.events.DadosFinanceirosAtualizadosEvent;
import my_app.core.events.EntityEvent;
import my_app.core.events.EventBus;
import my_app.domain.ViewModelScreenContract;
import my_app.domain.components.Components;
import my_app.services.EscPosPrinter;
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
    private final EscPosPrinter escPosPrinter;

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
    final State<String> opcaoEstoqueSelected = State.of(opcoesEstoque.getFirst());
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

    public final Components.InputRef quantidadeRef = new Components.InputRef();

    public VendaMercadoriaScreenViewModel(ScreenContext ctx) {
        this(ctx, createVendaService(), createProdutoService(), createClienteService(), createContaAreceberService());
    }

    private static EmpresaService createEmpresaService() {
        try {
            return new EmpresaService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public VendaMercadoriaScreenViewModel(ScreenContext ctx, VendaService vendaService, ProdutoService produtoService, ClienteService clienteService, ContaAreceberService contaService) {
        super(ctx);
        this.vendaService = vendaService;
        this.produtoService = produtoService;
        this.clienteService = clienteService;
        this.contaService = contaService;
        EmpresaService empresaService = createEmpresaService();
        this.escPosPrinter = new EscPosPrinter(empresaService, carregarPortaImpressora());
        this.onInit();
    }

    private static VendaService createVendaService() {
        try {
            return new VendaService();
        } catch (Exception e) {
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    private static ProdutoService createProdutoService() {
        try {
            return new ProdutoService();
        } catch (SQLException e) {
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    private static ClienteService createClienteService() {
        try {
            return new ClienteService();
        } catch (Exception e) {
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    private String carregarPortaImpressora() {
        try {
            var prefsService = new PreferenciasService();
            var prefs = prefsService.listar();
            if (!prefs.isEmpty()) {
                var port = prefs.getFirst().getPortaImpressora();
                if (port != null && !port.isBlank()) return port;
            }
        } catch (SQLException e) {
            log.warn("Não foi possível carregar porta da impressora", e);
        }
        return null;
    }

    private static ContaAreceberService createContaAreceberService() {
        try {
            return new ContaAreceberService();
        } catch (Exception e) {
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onInit() {
        qtd.subscribe(v -> atualizarEstoqueVisual());
        opcaoEstoqueSelected.subscribe(v -> atualizarEstoqueVisual());
        produtoEncontrado.subscribe(v -> atualizarEstoqueVisual());
        codigo.subscribe(this::filtrarProdutos);

        produtoEncontrado.subscribe(this::selecionarProduto);

        EventBus.getInstance().subscribe(event -> {
            if (event instanceof EntityEvent<?> ee && ee.entity() instanceof ClienteModel) {
                refreshClientes();
            }
        });
    }

    void filtrarProdutos(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            sugestoesProduto.clear();
            return;
        }

        var selected = produtoEncontrado.get();
        if (selected == null || !selected.getCodigoBarras().equals(termo.trim())) {
            produtoEncontrado.set(null);
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
            pcVenda.set(Utils.deRealParaCentavos(produto.getPrecoVenda()));
            estoqueAnterior.set(produto.getEstoque().toString());
            sugestoesProduto.clear();
            quantidadeRef.requestFocus();
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void refreshClientes() {
        try {
            var clienteList = clienteService.listar();
            UI.runOnUi(() -> {
                clientes.clear();
                clientes.addAll(clienteList);
            });
        } catch (Exception e) {
            log.error("Erro ao recarregar clientes", e);
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
                        venda.setProduto(produtoList.stream()
                                .filter(it -> it.getCodigoBarras().equals(venda.getProdutoCod()))
                                .findFirst()
                                .orElse(null));
                    }
                    vendas.addAll(vendaList);
                });

            } catch (Exception e) {
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

        var qtdStr = qtd.get().trim();
        if (qtdStr.isEmpty()) {
            Components.ShowAlertError("Quantidade é obrigatória!");
            return;
        }
        try {
            new BigDecimal(qtdStr);
        } catch (NumberFormatException e) {
            Components.ShowAlertError("Quantidade inválida!");
            return;
        }

        Async.Run(() -> {
            if (modoEdicao.get()) {
                final var selecionado = vendaSelected.get();
                if (selecionado == null) return;

                fillModelFromForm(selecionado, false);
                try {
                    vendaService.atualizar(selecionado);
                } catch (Exception e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao atualizar: " + e.getMessage()));
                    return;
                }

                UI.runOnUi(() -> {
                    vendas.updateIf(it -> it.getId().equals(selecionado.getId()), it -> selecionado);
                    Components.ShowPopup(ctx, "Venda atualizada com sucesso!");
                    EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
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
                } catch (Exception e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao salvar venda: " + e.getMessage()));
                    return;
                }

                if ("A PRAZO".equals(tipoPagamentoSelecionado.get()) && !parcelas.get().isEmpty()) {
                    try {
                        contaService.gerarContasDeVenda(salvo.getId(), salvo.getClienteId(), parcelas.get());
                    } catch (Exception e) {
                        UI.runOnUi(() -> Components.ShowAlertError("Erro ao gerar contas: " + e.getMessage()));
                        return;
                    }
                }

                VendaModel finalVenda = salvo;
                UI.runOnUi(() -> {
                    vendas.add(finalVenda);
                    clearForm();
                    EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                    reloadProdutos();
                    clearForm();
                    Components.ShowPopupWithButton(ctx,"Salvo com sucesso","Imprimir", ()-> imprimirNotaDeVenda(finalVenda));
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
                    EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                });
            } catch (Exception e) {
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
        descontoEmDinheiro.set("0");
        if (!clientes.get().isEmpty()) {
            clienteSelected.set(clientes.get().getFirst());
        }
        estoqueAnterior.set("0");
        estoqueAtual.set("0");
    }

    private void fillModelFromForm(VendaModel model, boolean isNew) {
        String cod = produtoEncontrado.get().getCodigoBarras();
        Integer clienteId = clienteSelected.get().getId();

        model.setProduto(produtoModelListState.get().stream().filter(prod-> prod.getCodigoBarras().equals(cod)).findFirst().get());
        model.setProdutoCod(cod);

        model.setCliente(clientes.get().stream().filter(c-> c.getId().equals(clienteId)).findFirst().get());
        model.setClienteId(clienteId);

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

    void imprimirNotaDeVenda(VendaModel vendaModel){
        Async.Run(() -> {
            try {
                escPosPrinter.imprimir(vendaModel);
            } catch (Exception e) {
                e.printStackTrace();
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao imprimir: " + e.getMessage()));
            }
        });
    }
}
