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
import my_app.services.WinRawPrinter;
import my_app.utils.DateUtils;
import my_app.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class VendaMercadoriaScreenViewModel extends ViewModelScreenContract<VendaModel> {
    private static final Logger log = LoggerFactory.getLogger(VendaMercadoriaScreenViewModel.class);
    private final VendaService vendaService;
    private final ProdutoService produtoService;
    private final ClienteService clienteService;
    private final ContaAreceberService contaService;
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

    final State<LocalDate> dataValidade = State.of(null);

    final ListState<ClienteModel> clientes = ListState.ofEmpty();
    final State<ClienteModel> clienteSelected = State.of(null);

    final State<VendaModel> vendaSelected = State.of(null);

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

    public final Components.InputRef quantidadeRef = new Components.InputRef();

    public VendaMercadoriaScreenViewModel(ScreenContext ctx) {
        super(ctx);
        this.vendaService = createOrReport(VendaService::new);
        this.produtoService = createOrReport(ProdutoService::new);
        this.clienteService = createOrReport(ClienteService::new);
        this.contaService = createOrReport(ContaAreceberService::new);
        EmpresaService empresaService = createOrReport(EmpresaService::new);
        this.escPosPrinter = new EscPosPrinter(empresaService, carregarPortaImpressora());

        this.onInit();
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

    @Override
    protected void onInit() {
        qtd.subscribe(v -> atualizarEstoqueVisual());
        opcaoEstoqueSelected.subscribe(v -> atualizarEstoqueVisual());
        produtoEncontrado.subscribe(v -> atualizarEstoqueVisual());
        codigo.subscribe(this::filtrarProdutos);

        produtoEncontrado.subscribe(this::selecionarProduto);

        modoEdicao.subscribe(editando -> {
            var data = editando ? vendaSelected.get() : null;
            quantidadeOriginal = data != null && data.getQuantidade() != null ? data.getQuantidade() : BigDecimal.ZERO;
            produtoCodOriginal = data != null ? data.getProdutoCod() : null;
            afetavaEstoqueOriginal = data != null && Boolean.TRUE.equals(data.getAfetaEstoque());
            atualizarEstoqueVisual();
        });

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
        if (selected == null || (!selected.getCodigoBarras().equals(termo.trim()) &&  !selected.getDescricao().equals(termo.trim()))) {
            log.info("Produto encontrado foi resetado para null!");
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
            codigo.set(produto.getDescricao());
            pcVenda.set(Utils.deRealParaCentavos(produto.getPrecoVenda()));
            var estoque = produto.getEstoque() != null ? produto.getEstoque() : BigDecimal.ZERO;
            estoqueAnterior.set(estoque.toString());
            sugestoesProduto.clear();
            quantidadeRef.requestFocus();
        }
    }

    @Override
    public void populateFieldsFromModel() {
        final var data = vendaSelected.get();
        if (data == null) return;

        modoEdicao.set(false);

        // Precisa vir antes dos campos abaixo: produtoEncontrado.set() dispara
        // selecionarProduto(), que também mexe em codigo/pcVenda/estoqueAnterior —
        // mas com valores do CATÁLOGO atual. Os sets explícitos logo depois
        // sobrescrevem isso com os valores de fato salvos nesta venda.
        //
        // Antes disso não existia: o codigo.set(data.getProdutoCod()) mais embaixo
        // disparava filtrarProdutos(), que via produtoEncontrado ainda null/stale e
        // resetava ele sozinho pra null de novo — então "Atualizar" caía direto no
        // "Produto não encontrado!", mesmo com o produto certo aparecendo no campo.
        produtoEncontrado.set(data.getProduto());
        if (data.getProduto() == null) {
            // Produto original não existe mais no catálogo (foi excluído depois da
            // venda) — mostra o código registrado mesmo assim; o usuário vai precisar
            // escolher um produto válido pra conseguir salvar.
            codigo.set(data.getProdutoCod());
        }

        dataVenda.set(DateUtils.millisParaLocalDate(data.getDataVenda()));
        numeroNota.set(data.getNumeroNota());
        qtd.set(Utils.quantidadeTratada(data.getQuantidade()));
        observacao.set(data.getObservacao());
        tipoPagamentoSelecionado.set(data.getTipoPagamento());
        pcVenda.set(Utils.deRealParaCentavos(data.getPrecoUnitario()));
        descontoEmDinheiro.set(Utils.deRealParaCentavos(data.getDesconto()));
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

    @Override
    public void fetchListData() {
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
                    clientes.clear();
                    clientes.addAll(clienteList);
                    clienteList.stream()
                            .filter(f -> f.getId() == 1)
                            .findFirst()
                            .ifPresent(clienteSelected::set);
                    allDataList.set(vendaList);
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

        if (clienteSelected.get() == null) {
            Components.ShowAlertError("Selecione um cliente!");
            return;
        }

        BigDecimal qtdValue;
        try {
            qtdValue = new BigDecimal(qtd.get().trim());
        } catch (NumberFormatException e) {
            Components.ShowAlertError("Quantidade inválida!");
            return;
        }

        if (!modoEdicao.get() && "Sim".equalsIgnoreCase(opcaoEstoqueSelected.get())) {
            var produto = produtoEncontrado.get();
            var estoqueBase = produto.getEstoque() != null ? produto.getEstoque() : BigDecimal.ZERO;
            var estoqueMinimo = produto.getEstoqueMinimo() != null ? produto.getEstoqueMinimo() : BigDecimal.ZERO;
            var estoquePostVenda = estoqueBase.subtract(qtdValue);

            if (estoquePostVenda.compareTo(estoqueMinimo) < 0) {
                var nome = produto.getDescricao() != null ? produto.getDescricao() : produto.getCodigoBarras();
                Components.ShowAlertAdvice(
                        "O estoque de \"" + nome + "\" ficará abaixo do mínimo (" + estoquePostVenda + " / mínimo: " + estoqueMinimo + "). Deseja continuar?",
                        () -> Async.Run(this::salvarVenda)
                );
                return;
            }
        }

        // Captura modoEdicao ANTES de entrar no Async.Run: ContratoTelaCrudV3.handleAddOrUpdate()
        // chama viewModel().modoEdicaoState().set(false) logo em seguida, de forma síncrona,
        // e essa chamada corre em paralelo com a task assíncrona abaixo. Sem essa captura,
        // o modoEdicao.get() lá dentro quase sempre já lê false, e a edição vira um cadastro novo
        // (mesmo bug documentado e corrigido em CategoriaScreenViewModel, ver docs/DECISIONS.md).
        //
        // populateModelFromFields() TAMBÉM lê modoEdicao.get() internamente (pra decidir se
        // copia id/dataCriacao de vendaSelected) — chamá-la de dentro do Async.Run sofria do
        // MESMO race mesmo com "editando" já capturado aqui, então o model também precisa ser
        // montado aqui, síncrono, quando estamos editando.
        final boolean editando = modoEdicao.get();
        final VendaModel modeloEditado = editando ? populateModelFromFields() : null;

        Async.Run(() -> {
            if (editando) {
                final var original = vendaSelected.get();
                if (original == null) return;

                // Não reaproveita/muta "original": ele é a MESMA referência que já está
                // dentro de allDataList (veio da seleção da linha na tabela). Mutar e
                // devolver essa mesma referência faz o allDataList.updateIf() "substituir"
                // o item por ele mesmo — ListState.set() vê as duas listas como iguais
                // (mesmas referências, mesma posição) e não notifica ninguém, então a
                // tabela nunca redesenha essa linha sozinha.
                var atualizado = modeloEditado;

                boolean atualizarEstoque = "Sim".equalsIgnoreCase(opcaoEstoqueSelected.get());
                try {
                    vendaService.atualizar(atualizado, atualizarEstoque);
                } catch (Exception e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao atualizar: " + e.getMessage()));
                    return;
                }

                // atualizado.getProduto() ainda é o snapshot de ANTES do ajuste de estoque
                // (populateModelFromFields setou com produtoEncontrado.get(), capturado antes do
                // vendaService.atualizar() acima). Sem recarregar aqui, essa venda fica com
                // estoque desatualizado em allDataList até o app reiniciar — reloadProdutos()
                // só atualiza a lista de busca/catálogo, não o que já está embutido nas vendas.
                try {
                    atualizado.setProduto(produtoService.buscarPorCodigoBarras(atualizado.getProdutoCod()));
                } catch (Exception e) {
                    log.error("Erro ao recarregar produto após atualizar venda", e);
                }

                UI.runOnUi(() -> {
                    allDataList.updateIf(it -> it.getId().equals(atualizado.getId()), it -> atualizado);
                    Components.ShowPopup(ctx, "Venda atualizada com sucesso!");
                    EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                    reloadProdutos();
                    clearForm();
                });
            } else {
                salvarVenda();
            }
        });
    }

    private void salvarVenda() {
        var model = populateModelFromFields();
        boolean atualizarEstoque = opcaoEstoqueSelected.get().equalsIgnoreCase("Sim");

        VendaModel salvo;
        try {
            salvo = vendaService.salvar(model, atualizarEstoque);
        } catch (Exception e) {
            UI.runOnUi(() -> Components.ShowAlertError("Erro ao salvar venda: " + e.getMessage()));
            return;
        }

        // Mesmo motivo do atualizar(): salvo.getProduto() ainda reflete o estoque de
        // ANTES do decremento feito dentro de vendaService.salvar() acima.
        try {
            salvo.setProduto(produtoService.buscarPorCodigoBarras(salvo.getProdutoCod()));
        } catch (Exception e) {
            log.error("Erro ao recarregar produto após salvar venda", e);
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
            allDataList.add(finalVenda);
            clearForm();
            EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
            reloadProdutos();
            clearForm();
            Components.ShowPopupWithButton(ctx,"Salvo com sucesso","Imprimir", ()-> imprimirNotaDeVenda(finalVenda));
        });
    }

    @Override
    public void handleClickMenuDelete() {
        final var data = vendaSelected.get();
        if (data == null) return;

        Async.Run(() -> {
            try {
                Integer vendaId = data.getId();
                // excluir() primeiro: ele pode falhar (ex: produto da venda não existe mais
                // no catálogo e não dá pra devolver estoque). Se excluirPorVendaId rodasse
                // antes e excluir() falhasse depois, as contas a receber já teriam sido
                // apagadas mas a venda continuaria lá — corrompendo o vínculo silenciosamente.
                // Devolução de estoque agora é decidida pelo afetaEstoque PERSISTIDO na
                // própria venda (setado na criação/edição), não pelo dropdown do formulário
                // — que não tem nenhuma relação com a venda sendo excluída aqui.
                vendaService.excluir(vendaId);
                contaService.excluirPorVendaId(vendaId);
                reloadProdutos();

                UI.runOnUi(() -> {
                    allDataList.removeIf(it -> it.getId().equals(vendaId));

                    String mensagem = "Venda e contas vinculadas excluídas!";
                    if (Boolean.TRUE.equals(data.getAfetaEstoque())) {
                        String nomeProduto = data.getProduto() != null
                                ? data.getProduto().getDescricao()
                                : data.getProdutoCod();
                        mensagem += " " + Utils.quantidadeTratada(data.getQuantidade())
                                + " unidade(s) de \"" + nomeProduto + "\" devolvida(s) ao estoque.";
                    }
                    Components.ShowPopup(ctx, mensagem);
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

    // Sempre retorna um VendaModel NOVO (nunca reaproveita vendaSelected), mesmo editando:
    // allDataList.updateIf() compara por referência, então mutar e devolver o mesmo objeto
    // que já está na lista faz o ListState achar que nada mudou e não redesenha a linha.
    @Override
    public VendaModel populateModelFromFields() {
        var model = new VendaModel();

        if (modoEdicao.get() && vendaSelected.get() != null) {
            var original = vendaSelected.get();
            model.setId(original.getId());
            model.setDataCriacao(original.getDataCriacao());
        }

        model.setProduto(produtoEncontrado.get());
        model.setProdutoCod(produtoEncontrado.get() != null ? produtoEncontrado.get().getCodigoBarras() : null);

        var cliente = clienteSelected.get();
        Integer clienteId = cliente != null ? cliente.getId() : null;
        model.setClienteId(clienteId);
        model.setCliente(clienteId != null
                ? clientes.get().stream().filter(c -> c.getId().equals(clienteId)).findFirst().orElse(cliente)
                : null);

        model.setQuantidade(new BigDecimal(qtd.get()));
        model.setPrecoUnitario(Utils.deCentavosParaReal(pcVenda.get()));
        model.setDesconto(Utils.deCentavosParaReal(descontoEmDinheiro.get()));
        model.setTipoPagamento(tipoPagamentoSelecionado.get());
        model.setObservacao(observacao.get());
        model.setTotalLiquido(new BigDecimal(totais.totalLiquido.get()));
        model.setDataValidade(dataValidade.get() != null ? DateUtils.localDateParaMillis(dataValidade.get()) : null);
        model.setDataVenda(DateUtils.localDateParaMillis(dataVenda.get()));
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
                e.printStackTrace();
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
        this.vendaService.close();
        this.clienteService.close();
        this.produtoService.close();
        this.contaService.close();
    }
}
