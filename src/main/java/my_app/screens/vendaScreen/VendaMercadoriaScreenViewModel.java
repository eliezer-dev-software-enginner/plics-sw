package my_app.screens.vendaScreen;

import megalodonte.ComputedState;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.base.state.State;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.v2.ListState;
import my_app.core.AppRoutes;
import my_app.core.db.models.*;
import my_app.core.db.services.*;
import my_app.core.events.DadosFinanceirosAtualizadosEvent;
import my_app.core.events.EntityEvent;
import my_app.core.events.EventBus;
import my_app.core.Data;
import my_app.core.Parcela;
import my_app.core.ViewModelScreenContract;
import my_app.core.components.Components;
import my_app.core.states.TotaisState;
import my_app.services.EscPosPrinter;
import my_app.services.WinRawPrinter;
import my_app.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pack.utilities.CurrencyPack;
import pack.utilities.DatePack;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

public class VendaMercadoriaScreenViewModel extends ViewModelScreenContract<VendaModel> {
    private static final Logger log = LoggerFactory.getLogger(VendaMercadoriaScreenViewModel.class);
    private final VendaService vendaService;
    private final ProdutoService produtoService;
    private final ClienteService clienteService;
    private final ContaAreceberService contaService;
    private final Consumer<Object> eventListener = this::onEntityEvent;
    private final EscPosPrinter escPosPrinter;

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
    final State<String> frete = State.of("0");

    final State<LocalDate> dataValidade = State.of(null);

    final ListState<ClienteModel> clientes = ListState.ofEmpty();
    final State<ClienteModel> clienteSelected = State.of(null);

    final List<String> opcoesEstoque = List.of("Sim", "Não");
    final State<String> opcaoEstoqueSelected = State.of(opcoesEstoque.getFirst());
    final State<String> estoqueAnterior = State.of("0");
    final State<String> estoqueAtual = State.of("0");

    // Snapshot da venda original ao entrar em modo de edição (ver onInit): usado pra
    // calcular só a DIFERENÇA de quantidade no preview de estoque, em vez da quantidade
    // inteira — a quantidade atual já foi descontada do estoque quando a venda foi criada
    // (só se afetavaEstoqueOriginal for true; ver VendaModel.afetaEstoque).
    private BigDecimal quantidadeOriginal = BigDecimal.ZERO;
    private String produtoCodOriginal = null;
    private boolean afetavaEstoqueOriginal = false;

    private final ListState<ProdutoModel> produtoModelListState = ListState.ofEmpty();
    final ListState<ProdutoModel> sugestoesProduto = ListState.ofEmpty();
    final State<ProdutoModel> produtoEncontrado = State.of(null);
    final State<String> codigo = State.of("");

    final ComputedState<Boolean> sugestoesProdutoVisible = ComputedState.of(
            () -> !sugestoesProduto.get().isEmpty(),
            sugestoesProduto
    );

    final TotaisState totais = new TotaisState(pcVenda, qtd, descontoEmDinheiro);

    // Frete é cobrado do cliente (soma no total que ele paga), diferente do frete de
    // Produtos (custo de adquirir do fornecedor) — conceitos distintos, não relacionados.
    // Fica fora de TotaisState (compartilhada com ComprasScreen, que não tem esse conceito)
    // pra não vazar um campo "Frete" indevido lá.
    final ComputedState<String> totalComFrete = ComputedState.of(() -> {
        var liquido = new BigDecimal(totais.totalLiquido.get());
        var freteValue = CurrencyPack.deCentavosParaReal(frete.get());
        return liquido.add(freteValue).toString();
    }, totais.totalLiquido, frete);

    public VendaMercadoriaScreenViewModel(ScreenContextInterface ctx) {
        super(ctx);
        this.vendaService = createOrReport(VendaService::new);
        this.produtoService = createOrReport(ProdutoService::new);
        this.clienteService = createOrReport(ClienteService::new);
        this.contaService = createOrReport(ContaAreceberService::new);
        EmpresaService empresaService = createOrReport(EmpresaService::new);
        this.escPosPrinter = new EscPosPrinter(empresaService, carregarPortaImpressora());

        screenNameSpawn = AppRoutes.Screens.ADD_OR_EDIT_VENDA_MERCADORIA.name();

        this.onInit();
    }

    protected void onInit() {
        qtd.subscribe(v -> atualizarEstoqueVisual());
        opcaoEstoqueSelected.subscribe(v -> atualizarEstoqueVisual());
        produtoEncontrado.subscribe(v -> atualizarEstoqueVisual());
        codigo.subscribe(this::filtrarProdutos);

        produtoEncontrado.subscribe(this::selecionarProduto);

        if(isEditing){
            var data = selected.get();
            quantidadeOriginal = data != null && data.getQuantidade() != null ? data.getQuantidade() : BigDecimal.ZERO;
            produtoCodOriginal = data != null ? data.getProdutoCod() : null;
            afetavaEstoqueOriginal = data != null && Boolean.TRUE.equals(data.getAfetaEstoque());
            atualizarEstoqueVisual();
        }

        EventBus.getInstance().subscribe(eventListener);
    }

    private String carregarPortaImpressora() {
        List<PreferenciasModel> prefs;
        try (var prefsService = createOrReport(PreferenciasService::new)) {
            prefs = prefsService.listar();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (!prefs.isEmpty()) {
            var port = prefs.getFirst().getPortaImpressora();
            if (port != null && !port.isBlank()) return port;
        }
        return null;
    }

    @Override
    protected boolean matchesSearch(VendaModel model, String query) {
        return (model.getProduto() != null && contains(model.getProduto().getDescricao(), query))
                || (model.getCliente() != null && contains(model.getCliente().getNome(), query))
                || contains(model.getNumeroNota(), query);
    }

    private boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    private void onEntityEvent(Object event) {
        if (event instanceof EntityEvent<?> ee && ee.entity() instanceof VendaModel) {
            fetchListData();
        }

        if (event instanceof EntityEvent<?> ee && ee.entity() instanceof ClienteModel) {
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
    }

    void filtrarProdutos(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            sugestoesProduto.clear();
            return;
        }

        var filtrados = produtoModelListState.get().stream()
                .filter(p -> p.getCodigoBarras().contains(termo.trim())
                        || p.getDescricao().toLowerCase().contains(termo.trim().toLowerCase()))
                .limit(8)
                .toList();

        log.info("produtoModelListState, size: {}",filtrados.size());
        log.info("produtos no filtro: {}, size: {}", termo,filtrados.size());

        sugestoesProduto.set(filtrados);
    }

    void selecionarProduto(ProdutoModel produto) {
        if (produto != null) {
            codigo.set(produto.getDescricao());
            pcVenda.set(Utils.deRealParaCentavos(produto.getPrecoVenda()));
            var estoque = produto.getEstoque() != null ? produto.getEstoque() : BigDecimal.ZERO;
            estoqueAnterior.set(estoque.toString());
            sugestoesProduto.clear();
        }
    }

    //estamos focando em edição/clonagem
    @Override
    public void populateFieldsFromModel(VendaModel model) {
        String prodCod = model.getProdutoCod();
        Long clienteId = model.getClienteId();

        Async.Run(() -> {
                ProdutoModel produto = produtoService.buscarPorCodigoBarras(prodCod);
                ClienteModel cliente = clienteService.buscarById(clienteId);

            UI.runOnUi(() -> {
                produtoEncontrado.set(produto);
                clienteSelected.set(cliente);

                dataVenda.set(DatePack.millisParaLocalDate(model.getDataVenda()));
                numeroNota.set(model.getNumeroNota());
                qtd.set(Utils.quantidadeTratada(model.getQuantidade()));
                observacao.set(model.getObservacao());
                tipoPagamentoSelecionado.set(model.getTipoPagamento());
                pcVenda.set(Utils.deRealParaCentavos(model.getPrecoUnitario()));
                descontoEmDinheiro.set(Utils.deRealParaCentavos(model.getDesconto()));
                frete.set(Utils.deRealParaCentavos(model.getFrete()));
                dataValidade.set(model.getDataValidade() != null
                        ? DatePack.millisParaLocalDate(model.getDataValidade())
                        : null);
            });
        });
    }

    //só roda pra carregar na tabela

    public void fetchListData(boolean isClientSelectedDefault) {
        Async.Run(() -> {
            try {
                var clienteList = clienteService.listar();
                var vendaList = vendaService.listar();
                var produtoList = produtoService.listar();

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

                UI.runOnUi(() -> {
                    produtoModelListState.set(produtoList);
                    clientes.set(clienteList);
                    if(isClientSelectedDefault) {
                        clienteList.stream()
                                .filter(c -> c.getId() == 1L)
                                .findFirst()
                                .ifPresent(clienteSelected::set);
                    }
                    allDataList.set(vendaList);
                });

            } catch (Exception e) {
                log.error("Erro ao buscar vendas", e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar vendas: " + e.getMessage()));
            }
        });
    }

    @Override
    public void fetchListData() {
        fetchListData(true);
    }


    @Override
    public void handleAddOrUpdate() {
        final var produtoAtual = produtoEncontrado.get();

        BigDecimal qtdValue;
        try {
            qtdValue = new BigDecimal(qtd.get().trim());
        } catch (NumberFormatException e) {
            Components.ShowAlertError("Quantidade inválida!");
            return;
        }

        if (!isEditing && "Sim".equalsIgnoreCase(opcaoEstoqueSelected.get())) {
            var alerta = calcularAlertaEstoque(produtoAtual, qtdValue);
            if (alerta != null) {
                Components.ShowAlertAdvice(alerta, () -> Async.Run(this::salvarVenda));
                return;
            }
        }

        final VendaModel atualizado = isEditing ? populateModelFromFields() : null;

        Async.Run(() -> {
            if (isEditing) {
                atualizarVenda(atualizado);
            } else {
                salvarVenda();
            }
        });
    }


    /** Retorna a mensagem de alerta se a venda deixar o estoque abaixo do mínimo, ou null se estiver ok. */
    private String calcularAlertaEstoque(ProdutoModel produto, BigDecimal qtdValue) {
        var estoqueBase = produto.getEstoque() != null ? produto.getEstoque() : BigDecimal.ZERO;
        var estoqueMinimo = produto.getEstoqueMinimo() != null ? produto.getEstoqueMinimo() : BigDecimal.ZERO;
        var estoquePostVenda = estoqueBase.subtract(qtdValue);

        if (estoquePostVenda.compareTo(estoqueMinimo) >= 0) {
            return null;
        }
        var nome = produto.getDescricao() != null ? produto.getDescricao() : produto.getCodigoBarras();
        return "O estoque de \"" + nome + "\" ficará abaixo do mínimo (" + estoquePostVenda + " / mínimo: " + estoqueMinimo + "). Deseja continuar?";
    }

    private void atualizarVenda(VendaModel atualizado) {
        final var original = selected.get();
        if (original == null) return;


        boolean atualizarEstoque = "Sim".equalsIgnoreCase(opcaoEstoqueSelected.get());
        try {
            vendaService.atualizar(atualizado, atualizarEstoque);
        } catch (Exception e) {
            UI.runOnUi(() -> Components.ShowAlertError("Erro ao atualizar: " + e.getMessage()));
            return;
        }

        recarregarProdutoNaVenda(atualizado);

        UI.runOnUi(() -> {
            Components.ShowPopup(ctx, "Venda atualizada com sucesso!");
            EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
            EventBus.getInstance().publish(EntityEvent.editado(atualizado));
            clearForm();
        });
    }

    private void salvarVenda() {
        var model = populateModelFromFields();
        boolean atualizarEstoque = "Sim".equalsIgnoreCase(opcaoEstoqueSelected.get());

        VendaModel salvo;
        try {
            salvo = vendaService.salvar(model, atualizarEstoque);
        } catch (Exception e) {
            UI.runOnUi(() -> Components.ShowAlertError("Erro ao salvar venda: " + e.getMessage()));
            return;
        }

        recarregarProdutoNaVenda(salvo);

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
            clearForm();
            EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
            EventBus.getInstance().publish(EntityEvent.criado(finalVenda));
            Components.ShowPopupWithButton(ctx, "Salvo com sucesso", "Imprimir", () -> imprimirNotaDeVenda(finalVenda));
        });
    }

    /**
     * O snapshot de produto embutido na venda ainda reflete o estoque de ANTES do
     * ajuste feito por vendaService.salvar()/atualizar(). Sem recarregar aqui, a venda
     * fica com estoque desatualizado em allDataList até o app reiniciar — reloadProdutos()
     * só atualiza a lista de busca/catálogo, não o que já está embutido nas vendas.
     */
    private void recarregarProdutoNaVenda(VendaModel venda) {
        try {
            venda.setProduto(produtoService.buscarPorCodigoBarras(venda.getProdutoCod()));
        } catch (Exception e) {
            log.error("Erro ao recarregar produto da venda {}", venda.getId(), e);
        }
    }

    @Override
    public void handleClickMenuDelete() {
        final var vendas = selectedItemsOrCurrent();
        if (vendas.isEmpty()) return;

        Components.ShowAlertAdvice("Deseja excluir " + vendas.size() + " venda(s) e suas contas vinculadas?", () -> Async.Run(() -> {
            try {
                for (var venda : vendas) {
                    vendaService.excluir(venda.getId());
                    contaService.excluirPorVendaId(venda.getId());
                }

                UI.runOnUi(() -> {
                    Components.ShowPopup(ctx, vendas.size() + " venda(s) e contas vinculadas excluídas!");
                    EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                    EventBus.getInstance().publish(EntityEvent.excluido(vendas.getLast()));
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao excluir: " + e.getMessage()));
            }
        }));
    }

    public void handleClickMenuDevolucao(VendaModel data) {
        if (data == null || Boolean.TRUE.equals(data.getDevolvida())) return;

        Components.ShowAlertAdvice(
                "Confirma a devolução desta venda? O estoque será restituído e o pagamento vinculado será estornado.",
                () -> Async.Run(() -> {
                    try {
                        Long vendaId = data.getId();
                        vendaService.devolver(vendaId);

                        // Busca de novo em vez de mutar "data": allDataList.updateIf() precisa de
                        // uma referência DIFERENTE da que já está na lista pra ListState notificar
                        // a tabela (mesmo motivo documentado em handleAddOrUpdate, acima).
                        var atualizado = vendaService.buscarById(vendaId);
                        atualizado.setProduto(data.getProduto());
                        atualizado.setCliente(data.getCliente());

                        UI.runOnUi(() -> {
                            allDataList.updateIf(it -> it.getId().equals(vendaId), it -> atualizado);
                            Components.ShowPopup(ctx, "Venda devolvida! Estoque e pagamento foram estornados.");
                            EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                            EventBus.getInstance().publish(EntityEvent.excluido(atualizado.getId()));
                        });
                    } catch (Exception e) {
                        UI.runOnUi(() -> Components.ShowAlertError("Erro ao devolver venda: " + e.getMessage()));
                    }
                })
        );
    }

    @Override
    public void clearForm() {
        dataVenda.set(LocalDate.now());
        numeroNota.set("");
        codigo.set("");
        produtoEncontrado.set(null);
        qtd.set("");
        observacao.set("");
        tipoPagamentoSelecionado.set(Data.tiposPagamentoList.get(1));
        pcVenda.set("0");
        dataValidade.set(null);
        descontoEmDinheiro.set("0");
        frete.set("0");
        if (!clientes.get().isEmpty()) {
            clienteSelected.set(clientes.get().getFirst());
        }
        estoqueAnterior.set("0");
        estoqueAtual.set("0");
    }

    // Sempre retorna um VendaModel NOVO (nunca reaproveita selected), mesmo editando:
    // allDataList.updateIf() compara por referência, então mutar e devolver o mesmo objeto
    // que já está na lista faz o ListState achar que nada mudou e não redesenha a linha.
    @Override
    public VendaModel populateModelFromFields() {
        var model = new VendaModel();

        if (isEditing && selected.get() != null) {
            var original = selected.get();
            model.setId(original.getId());
            model.setDataCriacao(original.getDataCriacao());
        }

        model.setProduto(produtoEncontrado.get());
        model.setProdutoCod(produtoEncontrado.get() != null ? produtoEncontrado.get().getCodigoBarras() : null);

        var cliente = clienteSelected.get();
        Long clienteId = cliente != null ? cliente.getId() : null;
        model.setClienteId(clienteId);
        model.setCliente(clienteId != null
                ? clientes.get().stream().filter(c -> c.getId().equals(clienteId)).findFirst().orElse(cliente)
                : null);

        model.setQuantidade(new BigDecimal(qtd.get()));
        model.setPrecoUnitario(CurrencyPack.deCentavosParaReal(pcVenda.get()));
        model.setDesconto(CurrencyPack.deCentavosParaReal(descontoEmDinheiro.get()));
        model.setFrete(CurrencyPack.deCentavosParaReal(frete.get()));
        model.setTipoPagamento(tipoPagamentoSelecionado.get());
        model.setObservacao(observacao.get());
        model.setTotalLiquido(new BigDecimal(totalComFrete.get()));
        model.setDataValidade(dataValidade.get() != null ? DatePack.localDateParaMillis(dataValidade.get()) : null);
        model.setDataVenda(DatePack.localDateParaMillis(dataVenda.get()));
        model.setNumeroNota(numeroNota.get());

        return model;
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
                var qtdValue = new BigDecimal(qtd.get().trim().isEmpty() ? "0" : qtd.get());

                // Editando a mesma venda/produto que JÁ afetava o estoque: a quantidade
                // original já foi descontada na criação, então o impacto adicional é só a
                // diferença pra quantidadeOriginal. Se a venda original não afetava estoque
                // (afetavaEstoqueOriginal=false), nada foi descontado ainda — o impacto é a
                // quantidade cheia, igual a uma venda nova.
                boolean editandoMesmoProdutoQueJaAfetava = produtoCodOriginal != null
                        && produtoCodOriginal.equals(produtoEncontrado.get().getCodigoBarras())
                        && afetavaEstoqueOriginal;
                var impacto = editandoMesmoProdutoQueJaAfetava ? qtdValue.subtract(quantidadeOriginal) : qtdValue;

                estoqueAtual.set(estoqueBase.subtract(impacto).toString());
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
                List<ContaAreceberModel> parcelas = null;
                if ("A PRAZO".equals(vendaModel.getTipoPagamento()) && vendaModel.getId() != null) {
                    parcelas = contaService.buscarPorVenda(vendaModel.getId());
                }
                final var finalParcelas = parcelas;
                escPosPrinter.imprimir(vendaModel, finalParcelas);
            } catch (Exception e) {
                log.error("Erro ao imprimir nota de venda id={}", vendaModel.getId(), e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao imprimir: " + e.getMessage()));
            }
        });
    }

    void imprimirNotaDeVendaAlternativo(VendaModel vendaModel) {
        Async.Run(() -> {
            try {
                String nomeImpressora = resolverNomeImpressoraTermica();
                if (nomeImpressora == null) {
                    UI.runOnUi(() -> Components.ShowAlertError("Impressora térmica não encontrada"));
                    return;
                }
                byte[] bytes = escPosPrinter.gerarBytesEscPos(vendaModel);
                boolean ok = WinRawPrinter.imprimirRaw(nomeImpressora, bytes);
                if (!ok) {
                    UI.runOnUi(() -> Components.ShowAlertError("Falha na impressão alternativa (RAW)"));
                }
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro: " + e.getMessage()));
            }
        });
    }

    private String resolverNomeImpressoraTermica() {
        String configurada = carregarPortaImpressora();
        if (configurada != null && !EscPosPrinter.isSerialPortName(configurada)) {
            return configurada;
        }
        var services = javax.print.PrintServiceLookup.lookupPrintServices(null, null);
        for (var ps : services) {
            String nome = ps.getName();
            if (nome != null && nome.toUpperCase().contains("TM-T20X")) {
                return nome;
            }
        }
        var def = javax.print.PrintServiceLookup.lookupDefaultPrintService();
        return def != null ? def.getName() : null;
    }

    @Override
    public void onDestroy() throws Exception {
        EventBus.getInstance().unsubscribe(eventListener);
        this.vendaService.close();
        this.clienteService.close();
        this.produtoService.close();
        this.contaService.close();
    }

    @Override
    public VendaModel findById(Long id) throws SQLException {
        return vendaService.buscarById(id);
    }
}
