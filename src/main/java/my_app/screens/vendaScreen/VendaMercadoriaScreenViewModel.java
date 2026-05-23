package my_app.screens.vendaScreen;

import megalodonte.ComputedState;
import megalodonte.v2.ListState;
import megalodonte.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.db.dto.VendaDto;
import my_app.db.models.ClienteModel;
import my_app.db.models.ProdutoModel;
import my_app.db.models.VendaModel;
import my_app.db.repositories.ClienteRepository;
import my_app.db.repositories.ContasAReceberRepository;
import my_app.db.repositories.ProdutoRepository;
import my_app.db.repositories.VendaRepository;
import my_app.domain.Data;
import my_app.domain.Parcela;
import my_app.events.DadosFinanceirosAtualizadosEvent;
import my_app.events.EventBus;
import my_app.lifecycle.viewmodel.component.ViewModelScreenContract;
import my_app.screens.components.Components;
import my_app.services.ContasAReceberService;
import my_app.services.VendaMercadoriaService;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class VendaMercadoriaScreenViewModel extends ViewModelScreenContract {
    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;
    private final VendaMercadoriaService vendaService;

    // --- Lista principal ---
    final ListState<VendaModel> vendas = ListState.ofEmpty();

    // --- Form states ---
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

    final ComputedState<String> totalBruto = ComputedState.of(() -> {
        int qtdValue = Integer.parseInt(qtd.get().trim().isEmpty() ? "0" : qtd.get());
        double preco = Double.parseDouble(pcVenda.get()) / 100.0;
        return Utils.toBRLCurrency(BigDecimal.valueOf(qtdValue * preco));
    }, descontoEmDinheiro, qtd, pcVenda);

    final ComputedState<String> totalLiquido = ComputedState.of(() -> {
        int qtdValue = Integer.parseInt(qtd.get().trim().isEmpty() ? "0" : qtd.get());
        double preco = Double.parseDouble(pcVenda.get()) / 100.0;
        double desconto = Double.parseDouble(descontoEmDinheiro.get()) / 100.0;
        return String.valueOf(qtdValue * preco - desconto);
    }, descontoEmDinheiro, qtd, pcVenda);

    final ComputedState<String> descontoFormatado = ComputedState.of(
            () -> Utils.toBRLCurrency(Utils.deCentavosParaReal(descontoEmDinheiro.get())),
            descontoEmDinheiro);

    final State<LocalDate> dataValidade = State.of(null);

    // --- Clientes ---
    final ListState<ClienteModel> clientes = ListState.ofEmpty();
    final State<ClienteModel> clienteSelected = State.of(null);

    // --- Seleção na tabela ---
    final State<VendaModel> vendaSelected = State.of(null);

    // --- Controle de estoque ---
    final List<String> opcoesEstoque = List.of("Sim", "Não");
    final State<String> opcaoEstoqueSelected = State.of(opcoesEstoque.get(0));
    final State<String> estoqueAnterior = State.of("0");
    final State<String> estoqueAtual = State.of("0");


    // --- Produtos ---

    private final megalodonte.v2.ListState<ProdutoModel> produtoModelListState = megalodonte.v2.ListState.ofEmpty();

    final megalodonte.v2.ListState sugestoesProduto = megalodonte.v2.ListState.ofEmpty();
    final State<ProdutoModel> produtoEncontrado = State.of(null);
    final State<String> codigo = State.of("");

    final ComputedState<Boolean> sugestoesProdutoVisible = ComputedState.of(
            () -> !sugestoesProduto.get().isEmpty(),
            sugestoesProduto
    );

    public VendaMercadoriaScreenViewModel(ScreenContext ctx) {
        super(ctx);
        this.produtoRepository = new ProdutoRepository();
        this.vendaRepository = new VendaRepository();
        this.clienteRepository = new ClienteRepository();
        this.vendaService = new VendaMercadoriaService(vendaRepository, produtoRepository);
        this.onInit();
    }

    @Override
    protected void onInit() {
        qtd.subscribe(v -> atualizarEstoqueVisual());
        opcaoEstoqueSelected.subscribe(v -> atualizarEstoqueVisual());
        codigo.subscribe(termo -> filtrarProdutos(termo)); // filtra ao digitar

        produtoEncontrado.subscribe(this::selecionarProduto);

        //TODO: SUBSCREVER A EVENTOS DE PRODUTO
        // EventBus.getInstance().subscribe();
    }

    void filtrarProdutos(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            sugestoesProduto.clear();
            return;
        }

        // Limpa seleção antes de trocar a lista
        produtoEncontrado.set(null);

        var filtrados = produtoModelListState.get().stream()
                .filter(p -> p.codigoBarras.contains(termo.trim())
                        || p.descricao.toLowerCase().contains(termo.trim().toLowerCase()))
                .limit(8) // evita lista enorme
                .toList();

        sugestoesProduto.set(filtrados);
    }

    void selecionarProduto(ProdutoModel produto) {
        if(produto!=null){
            codigo.set(produto.codigoBarras);
            pcVenda.set(Utils.deRealParaCentavos(produto.precoVenda));
            estoqueAnterior.set(produto.estoque.toString());
            sugestoesProduto.clear(); // fecha a lista após seleção
        }
    }

    @Override
    public void populateFromModel() {
        final var data = vendaSelected.get();
        if (data == null) return;

        modoEdicao.set(false);
        dataVenda.set(DateUtils.millisParaLocalDate(data.dataVenda));
        numeroNota.set(data.numeroNota);
        codigo.set(data.produtoCod);
        produtoEncontrado.set(null);
        //estoqueAtual.set(data.q)
        qtd.set(Utils.quantidadeTratada(data.quantidade));
        observacao.set(data.observacao);
        tipoPagamentoSelecionado.set(data.tipoPagamento);
        pcVenda.set(Utils.deRealParaCentavos(data.precoUnitario));
        dataValidade.set(data.dataValidade != null
                ? DateUtils.millisParaLocalDate(data.dataValidade)
                : null);
    }

    void reloadProdutos(){
        try {
            var produtoList = produtoRepository.listar();
            UI.runOnUi(()->  produtoModelListState.set(produtoList));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    void fetchData() {
        Async.Run(() -> {
            try {
                var clienteList = clienteRepository.listar();
                var vendaList = vendaRepository.listar();
                var produtoList = produtoRepository.listar();

                UI.runOnUi(() -> {
                    produtoModelListState.set(produtoList);
                    clientes.addAll(clienteList);
                    clienteList.stream()
                            .filter(f -> f.id == 1L)
                            .findFirst()
                            .ifPresent(clienteSelected::set);

                    for (var venda : vendaList) {
                        venda.cliente = clienteList.stream()
                                .filter(it -> it.id.equals(venda.clienteId))
                                .findFirst()
                                .orElse(null);
                    }
                    vendas.addAll(vendaList);
                });

            } catch (SQLException e) {
                e.printStackTrace();
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

        var dto = new VendaDto(
                produtoEncontrado.get().codigoBarras,
                clienteSelected.get().id,
                new BigDecimal(qtd.get()),
                Utils.deCentavosParaReal(pcVenda.get()),
                Utils.deCentavosParaReal(descontoEmDinheiro.get()),
                tipoPagamentoSelecionado.get(),
                observacao.get(),
                new BigDecimal(totalLiquido.get()),
                dataValidade.isNull() ? null : DateUtils.localDateParaMillis(dataValidade.get())
        );

        vendaService.deveAtualizarEstoque = opcaoEstoqueSelected.get().equalsIgnoreCase("Sim");

        Async.Run(() -> {
            if (modoEdicao.get()) {
                final var selecionado = vendaSelected.get();
                if (selecionado == null) return;

                var modelAtualizada = new VendaModel().fromIdAndDto(selecionado.id, dto);
                vendaService.atualizarOrThrow((VendaModel) modelAtualizada,
                        msg -> UI.runOnUi(() -> Components.ShowAlertError("Erro ao atualizar: " + msg)));
                vendas.updateIf(it -> it.id.equals(selecionado.id), it -> (VendaModel) modelAtualizada);
                Components.ShowPopup(ctx, "Venda atualizada com sucesso!");
                reloadProdutos();
            } else {
                VendaModel venda = null;
                try {
                    venda = vendaService.salvar(dto);
                } catch (SQLException e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao salvar venda: " + e.getMessage()));
                    return;
                }

                if ("A PRAZO".equals(tipoPagamentoSelecionado.get()) && !parcelas.get().isEmpty()) {
                    try {
                        var contasService = new ContasAReceberService(vendaRepository, clienteRepository);
                        contasService.gerarContasDeVenda(venda, parcelas.get());
                    } catch (SQLException e) {
                        UI.runOnUi(() -> Components.ShowAlertError("Erro ao gerar contas: " + e.getMessage()));
                        return;
                    }
                }

                VendaModel finalVenda = venda;
                UI.runOnUi(() -> {
                    vendas.add(finalVenda);
                    Components.ShowPopup(ctx, "Venda salva com sucesso!");
                    clearForm();
                    EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                    reloadProdutos();
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
                Long vendaId = data.id;
                new ContasAReceberRepository().excluirPorVendaId(vendaId);
                vendaRepository.excluirById(vendaId);
                devolverEstoque(data.produtoCod, data.quantidade);

                UI.runOnUi(() -> {
                    vendas.removeIf(it -> it.id.equals(vendaId));
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
        if (!clientes.isEmpty()) clienteSelected.set(clientes.get(0));
        opcaoEstoqueSelected.set("Não");
        estoqueAnterior.set("0");
        estoqueAtual.set("0");
    }

    private void devolverEstoque(String codigoBarras, BigDecimal quantidade) {
        if (!"Sim".equals(opcaoEstoqueSelected.get())) return;

        Async.Run(() -> {
            try {
                produtoRepository.atualizarEstoque(codigoBarras, quantidade);
                reloadProdutos();
            } catch (SQLException e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao devolver estoque: " + e.getMessage()));
            }
        });
    }

    private void atualizarEstoqueVisual() {
        if (produtoEncontrado.get() == null) {
            estoqueAnterior.set("0");
            estoqueAtual.set("0");
            return;
        }

        BigDecimal estoqueBase = produtoEncontrado.get().estoque != null
                ? produtoEncontrado.get().estoque
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
