package my_app.screens.contasAReceberScreen;

import megalodonte.ComputedState;
import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.ListState;
import my_app.db.models.ClienteModel;
import my_app.db.models.ContaAreceberModel;
import my_app.db.services.ClienteService;
import my_app.db.services.ContaAreceberService;
import my_app.domain.components.Components;
import my_app.core.events.DadosFinanceirosAtualizadosEvent;
import my_app.core.events.EventBus;
import my_app.domain.ViewModelScreenContract;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ContasAReceberScreenViewModel extends ViewModelScreenContract {

    private final ContaAreceberService contaService;
    private final ClienteService clienteService;

    public final ListState<ContaAreceberModel> contas = ListState.of(List.of());

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

    public final State<Boolean> modoRecebimento = State.of(false);

    public final List<String> statusOptions = List.of("TODOS", "PENDENTE", "PAGO", "PARCIAL", "ATRASADO", "CANCELADO");
    public final List<String> tipoDocumentoOptions = List.of("DUPLICATA", "BOLETO", "NOTA FISCAL", "CHEQUE", "OUTRO");
    public final State<String> statusOptionSelected = State.of(statusOptions.getFirst());

    public final ComputedState<String> btnRecebimentoText = ComputedState.of(() ->
            modoRecebimento.get() ? "Registrar Recebimento" : "Receber", modoRecebimento);

    public final ComputedState<Boolean> formValido = ComputedState.of(() ->
            !descricao.get().trim().isEmpty() &&
                    !valorOriginal.get().equals("0") &&
                    clienteSelected.get() != null &&
                    dataVencimento.get() != null, descricao, valorOriginal, clienteSelected, dataVencimento);

    public ContasAReceberScreenViewModel(ScreenContext ctx) {
        super(ctx);
        try {
            contaService = new ContaAreceberService();
            clienteService = new ClienteService();
        } catch (SQLException e) {
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    public void loadInicial() {
        Async.Run(() -> {
            try {
                var contasList = contaService.listar();
                var clientesList = clienteService.listar();

                for (var conta : contasList) {
                    if (conta.getClienteId() != null) {
                        clientesList.stream()
                                .filter(c -> c.getId().equals(conta.getClienteId()))
                                .findFirst()
                                .ifPresent(conta::setCliente);
                    }
                }

                final var clientesCopy = List.copyOf(clientesList);

                UI.runOnUi(() -> {
                    contas.addAll(contasList);
                    clientes.set(clientesCopy);
                    if (!clientesCopy.isEmpty()) {
                        clienteSelected.set(clientesCopy.getFirst());
                    }
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    public void loadPorStatus(String statusFiltro) {
        Async.Run(() -> {
            try {
                List<ContaAreceberModel> contasFiltradas;
                if ("TODOS".equals(statusFiltro)) {
                    contasFiltradas = contaService.listar();
                } else {
                    contasFiltradas = contaService.buscarPorStatus(statusFiltro);
                }

                attachClientes(contasFiltradas);

                UI.runOnUi(() -> {
                    contas.clear();
                    contas.addAll(contasFiltradas);
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    public void loadVencidas() {
        Async.Run(() -> {
            try {
                var contasVencidas = contaService.buscarVencidas();
                attachClientes(contasVencidas);

                UI.runOnUi(() -> {
                    contas.clear();
                    contas.addAll(contasVencidas);
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    @Override
    public void populateFromModel() {
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
        if (!formValido.get()) {
            UI.runOnUi(() -> Components.ShowAlertError("Preencha todos os campos obrigatórios"));
            return;
        }

        if (modoEdicao.get()) {
            asyncAtualizar();
        } else {
            asyncSalvar();
        }
    }

    @Override
    public void handleClickMenuDelete() {
        var selected = contaSelected.get();
        if (selected == null) return;

        Components.ShowAlertAdvice("Deseja excluir \"" + selected.getDescricao() + "\"?", () -> {
            Async.Run(() -> {
                try {
                    contaService.excluir(selected.getId());
                    UI.runOnUi(() -> {
                        contas.removeIf(c -> c.getId().equals(selected.getId()));
                        Components.ShowPopup(ctx, "Conta excluída com sucesso!");
                        clearForm();
                        EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                    });
                } catch (Exception e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao excluir: " + e.getMessage()));
                }
            });
        });
    }

    public void registrarRecebimento(ScreenContext ctx) {
        var selected = contaSelected.get();
        if (selected == null) {
            UI.runOnUi(() -> Components.ShowAlertError("Selecione uma conta para registrar recebimento"));
            return;
        }

        var valorRecebimentoBig = Utils.deCentavosParaReal(valorRecebimento.get());

        if (valorRecebimentoBig.compareTo(BigDecimal.ZERO) <= 0) {
            UI.runOnUi(() -> Components.ShowAlertError("Informe um valor de recebimento maior que zero"));
            return;
        }

        if (valorRecebimentoBig.compareTo(selected.getValorRestante()) > 0) {
            UI.runOnUi(() -> Components.ShowAlertError("Valor do recebimento não pode ser maior que o valor restante"));
            return;
        }

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
                    contas.updateIf(c -> c.getId().equals(selected.getId()), c -> updated);
                    Components.ShowPopup(ctx, "Recebimento registrado com sucesso!");
                    valorRecebimento.set("0");
                    modoRecebimento.set(false);
                    EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                });
            } catch (Exception e) {
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
                    contas.updateIf(c -> c.getId().equals(selected.getId()), c -> updated);
                    Components.ShowPopup(ctx, "Conta quitada com sucesso!");
                    EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao quitar conta: " + e.getMessage()));
            }
        });
    }

    private void asyncSalvar() {
        Async.Run(() -> {
            try {
                var model = new ContaAreceberModel();
                fillModelFromForm(model, true);
                var salvo = contaService.salvar(model);
                salvo.setCliente(clienteSelected.get());

                UI.runOnUi(() -> {
                    contas.add(salvo);
                    Components.ShowPopup(ctx, "Conta cadastrada com sucesso!");
                    clearForm();
                    EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao salvar: " + e.getMessage()));
            }
        });
    }

    private void asyncAtualizar() {
        Async.Run(() -> {
            try {
                var selected = contaSelected.get();
                if (selected == null) return;
                fillModelFromForm(selected, false);
                contaService.atualizar(selected);
                selected.setCliente(clienteSelected.get());

                UI.runOnUi(() -> {
                    contas.updateIf(c -> c.getId().equals(selected.getId()), c -> selected);
                    Components.ShowPopup(ctx, "Conta atualizada com sucesso!");
                    clearForm();
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao atualizar: " + e.getMessage()));
            }
        });
    }

    private void fillModelFromForm(ContaAreceberModel model, boolean isNew) {
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
    }

    public BigDecimal getTotalEmAberto() {
        try {
            return contaService.getTotalEmAberto();
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getTotalVencidas() {
        try {
            return contaService.getTotalVencidas();
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private void attachClientes(List<ContaAreceberModel> contasList) {
        for (var conta : contasList) {
            if (conta.getClienteId() != null) {
                clientes.get().stream()
                        .filter(c -> c.getId().equals(conta.getClienteId()))
                        .findFirst()
                        .ifPresent(conta::setCliente);
            }
        }
    }
}
