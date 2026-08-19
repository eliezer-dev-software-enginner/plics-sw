package my_app.screens.contasAReceberScreen;

import megalodonte.ComputedState;
import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.ContaAreceberModel;
import my_app.db.models.VendaModel;
import my_app.db.services.ClienteService;
import my_app.db.services.ContaAreceberService;
import my_app.db.services.ProdutoService;
import my_app.db.services.VendaService;
import my_app.db.models.ClienteModel;
import my_app.domain.components.Components;
import my_app.core.events.DadosFinanceirosAtualizadosEvent;
import my_app.core.events.EntityEvent;
import my_app.core.events.EventBus;
import my_app.domain.ViewModelScreenContract;
import my_app.utils.DateUtils;
import my_app.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ContasAReceberScreenViewModel extends ViewModelScreenContract<ContaAreceberModel> {
    private static final Logger log = LoggerFactory.getLogger(ContasAReceberScreenViewModel.class);

    private final ContaAreceberService contaService;
    private final ClienteService clienteService;
    private final VendaService vendaService;
    private final ProdutoService produtoService;

    public final State<String> descricao = State.of("");
    public final State<String> valorOriginal = State.of("0");
    public final State<String> valorRecebimento = State.of("0");

    public final State<LocalDate> dataVencimento = State.of(LocalDate.now());
    public final State<LocalDate> dataRecebimento = State.of(null);

    public final State<String> status = State.of("PENDENTE");
    public final State<String> tipoDocumento = State.of("DUPLICATA");
    public final State<String> numeroDocumento = State.of("");
    public final State<String> observacao = State.of("");

    public final State<List<ClienteModel>> clientes = State.of(List.of());
    public final State<ClienteModel> clienteSelected = State.of(null);
    public final State<ContaAreceberModel> contaSelected = State.of(null);

    // Cache das vendas (com produto já resolvido) pra anexar em cada conta sem
    // precisar consultar o banco de novo a cada filtro de status/vencidas.
    private final State<List<VendaModel>> vendas = State.of(List.of());

    public final State<Boolean> modoRecebimento = State.of(false);

    public final List<String> statusOptions = List.of("TODOS", "PENDENTE", "PAGO", "PARCIAL", "ATRASADO", "CANCELADO");
    public final List<String> tipoDocumentoOptions = List.of("DUPLICATA", "BOLETO", "NOTA FISCAL", "CHEQUE", "OUTRO");
    public final State<String> statusOptionSelected = State.of(statusOptions.getFirst());

    public final ComputedState<String> btnRecebimentoText = ComputedState.of(() ->
            modoRecebimento.get() ? "Registrar Recebimento" : "Receber", modoRecebimento);

    public ContasAReceberScreenViewModel(ScreenContext ctx) {
        super(ctx);
        this.contaService = createOrReport(ContaAreceberService::new);
        this.clienteService = createOrReport(ClienteService::new);
        this.vendaService = createOrReport(VendaService::new);
        this.produtoService = createOrReport(ProdutoService::new);
        EventBus.getInstance().subscribe(event -> {
            if (event instanceof EntityEvent<?> ee && ee.entity() instanceof ClienteModel) {
                loadClientes();
            }
        });
    }

    @Override
    protected boolean matchesSearch(ContaAreceberModel model, String query) {
        return contains(model.getDescricao(), query)
                || (model.getCliente() != null && contains(model.getCliente().getNome(), query));
    }

    private boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    void loadClientes() {
        try {
            var clientesList = clienteService.listar();
            UI.runOnUi(() -> clientes.set(clientesList));
        } catch (Exception e) {
            log.error("Erro ao buscar clientes", e);
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
        }
    }

    @Override
    public void fetchListData() {
        Async.Run(() -> {
            try {
                var contasList = contaService.listar();
                var clientesList = clienteService.listar();
                var vendasList = vendaService.listar();
                var produtosList = produtoService.listar();

                for (var venda : vendasList) {
                    produtosList.stream()
                            .filter(p -> p.getCodigoBarras().equals(venda.getProdutoCod()))
                            .findFirst()
                            .ifPresent(venda::setProduto);
                }

                attachClientesEVendas(contasList, clientesList, vendasList);

                final var clientesCopy = List.copyOf(clientesList);
                final var vendasCopy = List.copyOf(vendasList);

                UI.runOnUi(() -> {
                    allDataList.set(contasList);
                    clientes.set(clientesCopy);
                    vendas.set(vendasCopy);
                    if (!clientesCopy.isEmpty()) {
                        clienteSelected.set(clientesCopy.getFirst());
                    }
                });
            } catch (Exception e) {
                log.error("Erro ao buscar contas a receber", e);
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    @Override
    public void populateFieldsFromModel() {
        if (contaSelected.get() == null) return;
        var conta = contaSelected.get();

        descricao.set(conta.getDescricao());
        valorOriginal.set(Utils.deRealParaCentavos(conta.getValorOriginal()));
        dataVencimento.set(DateUtils.millisParaLocalDate(conta.getDataVencimento()));
        dataRecebimento.set(conta.getDataRecebimento() != null ? DateUtils.millisParaLocalDate(conta.getDataRecebimento()) : null);
        status.set(conta.getStatus());
        tipoDocumento.set(conta.getTipoDocumento());
        numeroDocumento.set(conta.getNumeroDocumento());
        observacao.set(conta.getObservacao());

        if (conta.getClienteId() != null) {
            clientes.get().stream()
                    .filter(c -> c.getId().equals(conta.getClienteId()))
                    .findFirst()
                    .ifPresent(clienteSelected::set);
        }
    }

    @Override
    public void clearForm() {
        descricao.set("");
        valorOriginal.set("0");
        dataVencimento.set(LocalDate.now());
        dataRecebimento.set(null);
        status.set("PENDENTE");
        tipoDocumento.set("DUPLICATA");
        numeroDocumento.set("");
        observacao.set("");
        modoRecebimento.set(false);
        valorRecebimento.set("0");
        contaSelected.set(null);
        if (!clientes.get().isEmpty()) {
            clienteSelected.set(clientes.get().getFirst());
        }
    }

    @Override
    public void handleAddOrUpdate() {
        if (modoEdicao.get() && contaSelected.get() == null) return;

        // model montado aqui, síncrono (thread da UI) — populateModelFromFields() lê
        // modoEdicao.get() internamente pra decidir se reaproveita contaSelected ou
        // cria um model novo; chamado de dentro do Async.Run de asyncSalvar/
        // asyncAtualizar isso quase sempre lia modoEdicao já resetado por
        // ContratoTelaCrudV3.handleAddOrUpdate() (que reseta logo depois de disparar
        // essa chamada), fazendo toda edição tentar dar update num model novo sem id
        // (mesmo bug corrigido em outras telas).
        boolean editando = modoEdicao.get();
        var model = populateModelFromFields();

        if (editando) {
            asyncAtualizar(model);
        } else {
            asyncSalvar(model);
        }
    }

    @Override
    public void handleClickMenuDelete() {
        var selected = contaSelected.get();
        if (selected == null) return;

        Components.ShowAlertAdvice("Deseja excluir \"" + selected.getDescricao() + "\"?", () -> Async.Run(() -> {
            try {
                contaService.excluir(selected.getId());
                UI.runOnUi(() -> {
                    allDataList.removeIf(c -> c.getId().equals(selected.getId()));
                    Components.ShowPopup(ctx, "Conta excluída com sucesso!");
                    clearForm();
                    EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                });
            } catch (Exception e) {
                log.error("Erro ao excluir conta a receber id={}", selected.getId(), e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao excluir: " + e.getMessage()));
            }
        }));
    }

    public void registrarRecebimento(ScreenContext ctx) {
        var selected = contaSelected.get();
        if (selected == null) {
            UI.runOnUi(() -> Components.ShowAlertError("Selecione uma conta para registrar recebimento"));
            return;
        }

        var valorRecebimentoBig = Utils.deCentavosParaReal(valorRecebimento.get());

        Async.Run(() -> {
            try {
                contaService.registrarRecebimento(selected.getId(), valorRecebimentoBig);
                var updated = contaService.buscarById(selected.getId());

                if (updated.getClienteId() != null) {
                    clientes.get().stream()
                            .filter(c -> c.getId().equals(updated.getClienteId()))
                            .findFirst()
                            .ifPresent(updated::setCliente);
                }

                UI.runOnUi(() -> {
                    allDataList.updateIf(c -> c.getId().equals(selected.getId()), c -> updated);
                    Components.ShowPopup(ctx, "Recebimento registrado com sucesso!");
                    valorRecebimento.set("0");
                    modoRecebimento.set(false);
                    EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                });
            } catch (Exception e) {
                log.error("Erro ao registrar recebimento id={}", selected.getId(), e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao registrar recebimento: " + e.getMessage()));
            }
        });
    }

    public void quitarConta(ScreenContext ctx) {
        var selected = contaSelected.get();
        if (selected == null) {
            UI.runOnUi(() -> Components.ShowAlertError("Selecione uma conta para quitar"));
            return;
        }

        Async.Run(() -> {
            try {
                contaService.registrarRecebimento(selected.getId(), selected.getValorRestante());
                var updated = contaService.buscarById(selected.getId());

                if (updated.getClienteId() != null) {
                    clientes.get().stream()
                            .filter(c -> c.getId().equals(updated.getClienteId()))
                            .findFirst()
                            .ifPresent(updated::setCliente);
                }

                UI.runOnUi(() -> {
                    allDataList.updateIf(c -> c.getId().equals(selected.getId()), c -> updated);
                    Components.ShowPopup(ctx, "Conta quitada com sucesso!");
                    EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                });
            } catch (Exception e) {
                log.error("Erro ao quitar conta a receber id={}", selected.getId(), e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao quitar conta: " + e.getMessage()));
            }
        });
    }

    private void asyncSalvar(ContaAreceberModel model) {
        Async.Run(() -> {
            try {
                var salvo = contaService.salvar(model);
                salvo.setCliente(clienteSelected.get());

                UI.runOnUi(() -> {
                    allDataList.add(salvo);
                    Components.ShowPopup(ctx, "Conta cadastrada com sucesso!");
                    clearForm();
                    EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                });
            } catch (Exception e) {
                log.error("Erro ao salvar conta a receber", e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao salvar: " + e.getMessage()));
            }
        });
    }

    private void asyncAtualizar(ContaAreceberModel model) {
        Async.Run(() -> {
            try {
                contaService.atualizar(model);
                model.setCliente(clienteSelected.get());

                UI.runOnUi(() -> {
                    allDataList.updateIf(c -> c.getId().equals(model.getId()), c -> model);
                    Components.ShowPopup(ctx, "Conta atualizada com sucesso!");
                    clearForm();
                });
            } catch (Exception e) {
                log.error("Erro ao atualizar conta a receber id={}", model.getId(), e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao atualizar: " + e.getMessage()));
            }
        });
    }

    @Override
    public ContaAreceberModel populateModelFromFields() {
        boolean isNew = !(modoEdicao.get() && contaSelected.get() != null);
        var model = isNew ? new ContaAreceberModel() : contaSelected.get();

        model.setDescricao(descricao.get());
        model.setValorOriginal(Utils.deCentavosParaReal(valorOriginal.get()));
        model.setDataVencimento(DateUtils.localDateParaMillis(dataVencimento.get()));
        model.setDataRecebimento(dataRecebimento.get() != null ? DateUtils.localDateParaMillis(dataRecebimento.get()) : null);
        model.setStatus(status.get());
        model.setClienteId(clienteSelected.get() != null ? clienteSelected.get().getId() : null);
        model.setVendaId(null);
        model.setNumeroDocumento(numeroDocumento.get());
        model.setTipoDocumento(tipoDocumento.get());
        model.setObservacao(observacao.get());

        if (isNew) {
            model.setValorRecebido(BigDecimal.ZERO);
            if (model.getValorOriginal() != null) {
                model.setValorRestante(model.getValorOriginal());
            }
        } else {
            if (model.getValorOriginal() != null && model.getValorRecebido() != null) {
                var restante = model.getValorOriginal().subtract(model.getValorRecebido());
                model.setValorRestante(restante.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : restante);
            }
        }

        return model;
    }

    public BigDecimal getTotalEmAberto() {
        try {
            return contaService.getTotalEmAberto();
        } catch (Exception e) {
            log.warn("Erro ao calcular total em aberto (retornando zero)", e);
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getTotalVencidas() {
        try {
            return contaService.getTotalVencidas();
        } catch (Exception e) {
            log.warn("Erro ao calcular total vencidas (retornando zero)", e);
            return BigDecimal.ZERO;
        }
    }

    // Sem isso, abrir os detalhes de uma conta gerada de uma venda fiada (double-click
    // na tabela, ver ContasAReceberScreen.itemDetails) dava NullPointerException — o
    // model.getVenda() é transient, só existe em memória se alguém anexar depois de
    // carregar do banco, e nada fazia isso além do cliente.
    private void attachClientesEVendas(List<ContaAreceberModel> contasList, List<ClienteModel> clientesList, List<VendaModel> vendasList) {
        for (var conta : contasList) {
            if (conta.getClienteId() != null) {
                clientesList.stream()
                        .filter(c -> c.getId().equals(conta.getClienteId()))
                        .findFirst()
                        .ifPresent(conta::setCliente);
            }
            if (conta.getVendaId() != null) {
                vendasList.stream()
                        .filter(v -> v.getId().equals(conta.getVendaId()))
                        .findFirst()
                        .ifPresent(conta::setVenda);
            }
        }
    }

    @Override
    public void onDestroy() throws Exception {
        this.clienteService.close();
        this.contaService.close();
        this.vendaService.close();
        this.produtoService.close();
    }
}
