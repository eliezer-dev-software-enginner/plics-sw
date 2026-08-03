package my_app.screens.pdvScreen;

import megalodonte.ComputedState;
import megalodonte.base.components.Ref;
import megalodonte.base.state.State;
import megalodonte.components.v2.Input;
import megalodonte.v2.ListState;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.Main;
import my_app.core.AppRoutes;
import my_app.db.models.ClienteModel;
import my_app.db.models.ProdutoModel;
import my_app.db.services.ClienteService;
import my_app.db.services.EmpresaService;
import my_app.db.services.PedidoItemService;
import my_app.db.services.PreferenciasService;
import my_app.db.services.ProdutoService;
import my_app.core.events.EntityEvent;
import my_app.core.events.DadosFinanceirosAtualizadosEvent;
import my_app.core.events.EventBus;
import my_app.domain.Data;
import my_app.domain.components.Components;
import my_app.services.EscPosPrinter;
import my_app.services.PDVService;
import my_app.services.WinRawPrinter;
import my_app.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PDVScreenViewModel {

    private static final Logger log = LoggerFactory.getLogger(PDVScreenViewModel.class);
    private final ScreenContext ctx;
    private final ProdutoService produtoService;
    private final ClienteService clienteService;
    private final PDVService pdvService;

    // Cache de todos os produtos (lookup por código)
    private final Map<String, ProdutoModel> produtosCache = new HashMap<>();
    final ListState<ProdutoModel> sugestoesProduto = ListState.ofEmpty();

    final ComputedState<Boolean> sugestoesProdutoVisible = ComputedState.of(
            () -> !sugestoesProduto.get().isEmpty(),
            sugestoesProduto
    );

    final State<ProdutoModel> produtoEncontrado = State.of(null);
    final ListState<ClienteModel> clientes = ListState.ofEmpty();

    final State<ClienteModel> clienteSelected = State.of(null);

    // Estado reativo: itens no carrinho
    final ListState<ItemVenda> itensCarrinho = ListState.ofEmpty();

    final State<String> formaPagamentoSelecionado = State.of(Data.tiposPagamentoList.getFirst());
    final ComputedState<Boolean> tipoPagamentoIsAPrazo = ComputedState.of(
            () -> formaPagamentoSelecionado.get().equals("A PRAZO"),
            formaPagamentoSelecionado);
    final State<String> numeroParcelas = State.of("1");

    // Estado do campo de busca
    final State<String> codigoBarrasInput = State.of("");

    //item atual buscado
    final State<String> quantidadeInput = State.of("1");
    final Ref<Input> qtdRef = new Ref<>();

    // Subtotal derivado — recalculado sempre que itensCarrinho mudar
    final State<String> subtotal = State.of("0");

    final State<String> desconto = State.of("0");
    // subtotal - desconto (nunca negativo) — recalculado junto com troco
    final State<String> totalAPagar = State.of("0");

    final State<String> totalRecebido = State.of("0");
    final State<String> troco         = State.of("0");

    final State<Boolean> isPrintNotaVendaVisible = State.of(false);

    private my_app.db.models.PedidoModel lastPedido;
    private final PedidoItemService pedidoItemService;
    private final EmpresaService empresaService;
    private final EscPosPrinter escPosPrinter;

    public PDVScreenViewModel(ScreenContext ctx) {
        this.ctx = ctx;
        this.produtoService = createOrReport(ProdutoService::new);
        this.clienteService = createOrReport(ClienteService::new);
        this.pdvService = createOrReport(PDVService::new);
        this.pedidoItemService = createOrReport(PedidoItemService::new);
        this.empresaService = createOrReport(EmpresaService::new);
        var porta = carregarPortaImpressora();
        this.escPosPrinter = porta != null ? new EscPosPrinter(empresaService, porta) :
                new EscPosPrinter(empresaService);
        this.onInit();
    }

    private String carregarPortaImpressora() {
        try (var prefsService = createOrReport(PreferenciasService::new)) {
            var prefs = prefsService.listar();
            if (!prefs.isEmpty()) {
                var port = prefs.getFirst().getPortaImpressora();
                if (port != null && !port.isBlank()) return port;
            }
        } catch (Exception e) {
            log.warn("Não foi possível carregar porta da impressora", e);
        }
        return null;
    }

    private static <T> T createOrReport(megalodonte.utils.ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            megalodonte.application.ErrorReporter.handle(e);
            throw new IllegalStateException(e);
        }
    }

    protected void onInit() {
        itensCarrinho.onChange(itens -> {
            BigDecimal total = itens.stream()
                    .map(ItemVenda::totalItem)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            subtotal.set(Utils.deRealParaCentavos(total));
            recalcularTotais();
        });

        totalRecebido.subscribe(recebido -> recalcularTotais());
        desconto.subscribe(d -> recalcularTotais());

        EventBus.getInstance().subscribe(event -> {
            if(event instanceof EntityEvent<?> ee && ee.is(EntityEvent.EventType.CRIADO) && ee.entity() instanceof ClienteModel cm){
                clientes.add(cm);
            }
        });

        produtoEncontrado.subscribe(this::selecionarProduto);
        codigoBarrasInput.subscribe(this::filtrarProdutos);

    }

    private BigDecimal calcularTotalLiquido() {
        BigDecimal subtotalBD = Utils.deCentavosParaReal(subtotal.get());
        BigDecimal descontoBD;
        try {
            descontoBD = Utils.deCentavosParaReal(desconto.get());
        } catch (NumberFormatException e) {
            descontoBD = BigDecimal.ZERO;
        }
        BigDecimal liquido = subtotalBD.subtract(descontoBD);
        return liquido.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : liquido;
    }

    private void recalcularTotais() {
        BigDecimal liquido = calcularTotalLiquido();
        totalAPagar.set(Utils.deRealParaCentavos(liquido));

        try {
            BigDecimal recebidoBD = Utils.deCentavosParaReal(totalRecebido.get());
            BigDecimal t = recebidoBD.subtract(liquido);
            troco.set(t.compareTo(BigDecimal.ZERO) < 0 ? "0" : Utils.deRealParaCentavos(t));
        } catch (NumberFormatException e) {
            troco.set("0");
        }
    }

    void selecionarProduto(ProdutoModel produto) {
        if (produto != null) {
            codigoBarrasInput.set(produto.getCodigoBarras());
            // codigoBarrasInput.set() acima já disparou filtrarProdutos(), que repopulou
            // sugestoesProduto (o próprio produto selecionado bate no filtro) — precisa
            // limpar de novo aqui, depois, pra fechar o dropdown de fato.
            sugestoesProduto.clear();
            if (qtdRef.current() != null) {
                qtdRef.current().requestFocus();
            }
        }
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

        if(selected != null) {
            IO.println(selected.getDescricao());
        }


        var filtrados = produtosCache.values().stream()
                .filter(p -> p.getCodigoBarras().contains(termo.trim())
                        || p.getDescricao().toLowerCase().contains(termo.trim().toLowerCase()))
                .limit(8)
                .toList();

        sugestoesProduto.set(filtrados);
    }

    void loadClientes() {
        Async.Run(() -> {
            try {
                var list = clienteService.listar();
                UI.runOnUi(() -> {
                    clientes.clear();
                    clientes.addAll(list);
                    list.stream().filter(c -> c.getId() == 1).findFirst().ifPresent(clienteSelected::set);
                });
            } catch (Exception e) {
                log.error("Erro ao buscar clientes", e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar clientes: " + e.getMessage()));
            }
        });
    }

    void loadProdutos() {
        Async.Run(() -> {
            try {
                var list = produtoService.listar();

                System.out.println("=== loadProdutos ===");
                System.out.println("Lista retornada: " + list.size());
                if (!list.isEmpty()) {
                    System.out.println("Primeiro produto: " + list.getFirst().getDescricao());
                }

                UI.runOnUi(() -> {
                    produtosCache.clear();
                    list.forEach(p -> produtosCache.put(p.getCodigoBarras(), p));
                    System.out.println("Cache populado com: " + produtosCache.size() + " produtos");
                });
            } catch (Exception e) {
                log.error("Erro ao buscar produtos", e);
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

        BigDecimal quantidadeDesejada;
        try {
            quantidadeDesejada = new BigDecimal(quantidadeInput.get().trim());
        } catch (NumberFormatException e) {
            Components.ShowAlertError("Quantidade inválida.");
            return;
        }
        if (quantidadeDesejada.compareTo(BigDecimal.ZERO) <= 0) {
            Components.ShowAlertError("Quantidade deve ser maior que zero.");
            return;
        }

        var estoqueDisponivel = produto.getEstoque() != null ? produto.getEstoque() : BigDecimal.ZERO;
        if (estoqueDisponivel.compareTo(BigDecimal.ZERO) <= 0) {
            Components.ShowAlertError("\"" + produto.getDescricao() + "\" está sem estoque disponível.");
            return;
        }

        // Se já existe no carrinho, incrementa a qtd
        var existente = itensCarrinho.get().stream()
                .filter(i -> i.produto.getCodigoBarras().equals(codigo))
                .findFirst();

        var quantidadeAtualCarrinho = existente.map(i -> i.quantidade).orElse(BigDecimal.ZERO);
        var quantidadeAdicional = existente.isPresent() ? BigDecimal.ONE : quantidadeDesejada;
        var quantidadeFinal = quantidadeAtualCarrinho.add(quantidadeAdicional);

        if (quantidadeFinal.compareTo(estoqueDisponivel) > 0) {
            Components.ShowAlertError("Estoque insuficiente para \"" + produto.getDescricao() + "\". Disponível: "
                    + Utils.quantidadeTratada(estoqueDisponivel) + ", já no carrinho: " + Utils.quantidadeTratada(quantidadeAtualCarrinho));
            return;
        }

        if (existente.isPresent()) {
            existente.get().quantidade = quantidadeFinal;
            itensCarrinho.refresh(); // notifica observers sem substituir a lista
        } else {
            var item = new ItemVenda(produto);
            item.quantidade = quantidadeDesejada;
            itensCarrinho.add(item);
        }

        codigoBarrasInput.set(""); // limpa o campo após adicionar
        quantidadeInput.set("1");
    }

    void atualizarQuantidade(ItemVenda item, BigDecimal novaQtd) {
        if (novaQtd.compareTo(BigDecimal.ZERO) <= 0) {
            itensCarrinho.remove(item);
            return;
        }

        // Sem isso, dava pra contornar a checagem de estoque de adicionarPorCodigo()
        // simplesmente editando a quantidade direto na célula da tabela.
        var estoqueDisponivel = item.produto.getEstoque() != null ? item.produto.getEstoque() : BigDecimal.ZERO;
        if (novaQtd.compareTo(estoqueDisponivel) > 0) {
            Components.ShowAlertError("Estoque insuficiente para \"" + item.produto.getDescricao() + "\". Disponível: "
                    + Utils.quantidadeTratada(estoqueDisponivel));
            itensCarrinho.refresh(); // reverte a célula pro valor anterior
            return;
        }

        item.quantidade = novaQtd;
        itensCarrinho.refresh();
    }

    void removerItem(ItemVenda item) {
        itensCarrinho.remove(item);
    }

    void finalizarVenda() {
        if (itensCarrinho.isEmpty()) {
            Components.ShowAlertError("Carrinho vazio.");
            return;
        }

        boolean fiado = tipoPagamentoIsAPrazo.get();
        Integer clienteId = clienteSelected.get() != null ? clienteSelected.get().getId() : null;

        if (fiado && clienteId == null) {
            Components.ShowAlertError("Selecione um cliente para venda a prazo.");
            return;
        }

        if (clienteId == null) {
            clienteId = 1; // CLIENTE PADRÃO (inserido pela migration V16)
        }

        BigDecimal totalLiquido = calcularTotalLiquido();

        // Vendas a prazo não recebem em dinheiro no ato — só validar recebido/troco
        // quando o pagamento é esperado na hora (a vista, crédito, débito, pix).
        if (!fiado) {
            try {
                BigDecimal recebidoBD = Utils.deCentavosParaReal(totalRecebido.get());
                if (recebidoBD.compareTo(totalLiquido) < 0) {
                    Components.ShowAlertError("Valor recebido insuficiente. Informe o total recebido do cliente.");
                    return;
                }
            } catch (NumberFormatException e) {
                Components.ShowAlertError("Valor recebido inválido.");
                return;
            }
        }

        final Integer finalClienteId = clienteId;
        final int qtdParcelas = fiado ? Math.max(1, Integer.parseInt(numeroParcelas.get())) : 1;
        final String formaPagamento = formaPagamentoSelecionado.get();
        final BigDecimal descontoValue;
        try {
            descontoValue = Utils.deCentavosParaReal(desconto.get());
        } catch (NumberFormatException e) {
            Components.ShowAlertError("Desconto inválido.");
            return;
        }
        Async.Run(() -> {
            try {
                lastPedido = pdvService.finalizarVenda(
                        itensCarrinho.get(),
                        formaPagamento,
                        finalClienteId,
                        fiado,
                        qtdParcelas,
                        descontoValue
                );
                UI.runOnUi(() -> {
                    itensCarrinho.clear();
                    codigoBarrasInput.set("");
                    quantidadeInput.set("1");
                    subtotal.set("0");
                    desconto.set("0");
                    totalAPagar.set("0");
                    totalRecebido.set("0");
                    troco.set("0");
                    clientes.get().stream().filter(c -> c.getId() == 1).findFirst().ifPresent(clienteSelected::set);
                    formaPagamentoSelecionado.set(Data.tiposPagamentoList.getFirst());
                    numeroParcelas.set("1");
                    Components.ShowPopup(ctx, "Venda finalizada com sucesso!");
                    EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                    isPrintNotaVendaVisible.set(true);
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao finalizar venda: " + e.getMessage()));
            }
        });
    }

    void imprimirNota(){
        if (lastPedido == null) {
            Components.ShowAlertError("Nenhuma venda para imprimir.");
            return;
        }

        Async.Run(() -> {
            try {
                var dados = carregarDadosNotaPedido(lastPedido);
                final var pedido = lastPedido;

                try {
                    escPosPrinter.imprimirNotaVenda(pedido, dados.itens(), dados.cliente(), dados.empresa(), dados.parcelas());
                    } catch (Exception e) {
                        UI.runOnUi(()->Components.ShowAlertError("Erro ao imprimir: " + e.getMessage()));
                    }

            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar dados para impressao: " + e.getMessage()));
            }
        });
    }

    // Fallback pra impressoras térmicas que não são reconhecidas como impressora de
    // texto comum pelo Java Print Service — envia os bytes ESC/POS crus via spooler
    // do Windows (mesmo padrão de VendaMercadoriaScreenViewModel.imprimirNotaDeVendaAlternativo).
    void imprimirNotaAlternativa() {
        if (lastPedido == null) {
            Components.ShowAlertError("Nenhuma venda para imprimir.");
            return;
        }

        Async.Run(() -> {
            try {
                String nomeImpressora = resolverNomeImpressoraTermica();
                if (nomeImpressora == null) {
                    UI.runOnUi(() -> Components.ShowAlertError("Impressora térmica não encontrada"));
                    return;
                }
                var dados = carregarDadosNotaPedido(lastPedido);
                byte[] bytes = escPosPrinter.gerarBytesEscPos(lastPedido, dados.itens(), dados.cliente(), dados.empresa(), dados.parcelas());
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

    private record DadosNotaPedido(
            java.util.List<my_app.db.models.PedidoItemModel> itens,
            ClienteModel cliente,
            my_app.db.models.EmpresaModel empresa,
            java.util.List<my_app.db.models.ContaAreceberModel> parcelas
    ) {}

    private DadosNotaPedido carregarDadosNotaPedido(my_app.db.models.PedidoModel pedido) throws Exception {
        var itens = pedidoItemService.listarPorPedido(pedido.getId());
        var empresa = empresaService.buscarUnico();
        var clienteId = pedido.getClienteId();
        final ClienteModel cliente;
        if (clienteId != null) {
            cliente = clienteService.listar().stream()
                    .filter(c -> c.getId().equals(clienteId))
                    .findFirst()
                    .orElse(null);
        } else {
            cliente = null;
        }

        java.util.List<my_app.db.models.ContaAreceberModel> parcelas = null;
        if (pedido.getFiado() != null && pedido.getFiado() == 1) {
            try (var contaService = new my_app.db.services.ContaAreceberService()) {
                parcelas = contaService.buscarPorVenda(pedido.getId());
            }
        }

        return new DadosNotaPedido(itens, cliente, empresa, parcelas);
    }

    void handleCriarCliente(){
        ctx.router().spawnWindow(AppRoutes.Screens.CLIENTES.name(), e->{});
    }

    public void onDestroy() throws Exception {
        this.clienteService.close();
        this.empresaService.close();
        this.produtoService.close();
        this.pedidoItemService.close();
        //TODO: CHAMAR CLOSE
        //this.pdvService.close();
    }
}