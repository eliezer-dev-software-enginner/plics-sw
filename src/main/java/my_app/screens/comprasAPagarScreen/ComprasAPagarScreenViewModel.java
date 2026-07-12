package my_app.screens.comprasAPagarScreen;

import megalodonte.ComputedState;
import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.ContasPagarModel;
import my_app.db.models.FornecedorModel;
import my_app.db.services.ContasPagarService;
import my_app.db.services.FornecedorService;
import my_app.domain.components.Components;
import my_app.domain.ViewModelScreenContract;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ComprasAPagarScreenViewModel extends ViewModelScreenContract<ContasPagarModel> {

    private final ContasPagarService contaService;
    private final FornecedorService fornecedorService;

    public final State<String> descricao = State.of("");
    public final State<String> valorOriginal = State.of("0");
    public final State<String> valorPagamento = State.of("0");

    public final State<LocalDate> dataVencimento = State.of(LocalDate.now());
    public final State<LocalDate> dataPagamento = State.of(null);

    public final State<String> status = State.of("PENDENTE");
    public final State<String> tipoDocumento = State.of("DUPLICATA");
    public final State<String> numeroDocumento = State.of("");
    public final State<String> observacao = State.of("");

    public final State<List<FornecedorModel>> fornecedores = State.of(List.of());
    public final State<FornecedorModel> fornecedorSelected = State.of(null);
    public final State<ContasPagarModel> contaSelected = State.of(null);

    public final State<Boolean> modoPagamento = State.of(false);

    public final List<String> statusOptions = List.of("TODOS", "PENDENTE", "PAGO", "PARCIAL", "ATRASADO", "CANCELADO");
    public final List<String> tipoDocumentoOptions = List.of("DUPLICATA", "BOLETO", "NOTA FISCAL", "CHEQUE", "OUTRO");
    public final State<String> statusOptionSelected = State.of(statusOptions.getFirst());

    public final ComputedState<String> btnPagamentoText = ComputedState.of(() ->
            modoPagamento.get() ? "Registrar Pagamento" : "Pagar", modoPagamento);

    public final ComputedState<Boolean> formValido = ComputedState.of(() ->
            !descricao.get().trim().isEmpty() &&
                    !valorOriginal.get().equals("0") &&
                    fornecedorSelected.get() != null &&
                    dataVencimento.get() != null, descricao, valorOriginal, fornecedorSelected, dataVencimento);

    public final ComputedState<Boolean> pagamentoValido = ComputedState.of(() ->
            contaSelected.get() != null, contaSelected);

    public ComprasAPagarScreenViewModel(ScreenContext ctx) {
        super(ctx);
        this.contaService = createOrReport(ContasPagarService::new);
        this.fornecedorService = createOrReport(FornecedorService::new);
    }

    @Override
    protected boolean matchesSearch(ContasPagarModel model, String query) {
        return contains(model.getDescricao(), query)
                || (model.getFornecedor() != null && contains(model.getFornecedor().getNome(), query));
    }

    private boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    @Override
    public void fetchListData() {
        Async.Run(() -> {
            try {
                var contasList = contaService.listar();
                var fornecedoresList = fornecedorService.listar();

                for (var conta : contasList) {
                    if (conta.getFornecedorId() != null) {
                        fornecedoresList.stream()
                                .filter(f -> f.getId().equals(conta.getFornecedorId()))
                                .findFirst()
                                .ifPresent(conta::setFornecedor);
                    }
                }

                final var fornecedoresCopy = List.copyOf(fornecedoresList);

                UI.runOnUi(() -> {
                    allDataList.set(contasList);
                    fornecedores.set(fornecedoresCopy);
                    if (!fornecedoresCopy.isEmpty()) {
                        fornecedorSelected.set(fornecedoresCopy.getFirst());
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
                List<ContasPagarModel> contasFiltradas;
                if ("TODOS".equals(statusFiltro)) {
                    contasFiltradas = contaService.listar();
                } else {
                    contasFiltradas = contaService.buscarPorStatus(statusFiltro);
                }

                attachFornecedores(contasFiltradas);

                UI.runOnUi(() -> {
                    allDataList.set(contasFiltradas);
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
                attachFornecedores(contasVencidas);

                UI.runOnUi(() -> {
                    allDataList.set(contasVencidas);
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
        dataPagamento.set(conta.getDataPagamento() != null ? DateUtils.millisParaLocalDate(conta.getDataPagamento()) : null);
        status.set(conta.getStatus());
        tipoDocumento.set(conta.getTipoDocumento());
        numeroDocumento.set(conta.getNumeroDocumento());
        observacao.set(conta.getObservacao());

        if (conta.getFornecedorId() != null) {
            fornecedores.get().stream()
                    .filter(f -> f.getId().equals(conta.getFornecedorId()))
                    .findFirst()
                    .ifPresent(fornecedorSelected::set);
        }
    }

    @Override
    public void clearForm() {
        descricao.set("");
        valorOriginal.set("0");
        dataVencimento.set(LocalDate.now());
        dataPagamento.set(null);
        status.set("PENDENTE");
        tipoDocumento.set("DUPLICATA");
        numeroDocumento.set("");
        observacao.set("");
        modoPagamento.set(false);
        valorPagamento.set("0");
        contaSelected.set(null);
        if (!fornecedores.get().isEmpty()) {
            fornecedorSelected.set(fornecedores.get().getFirst());
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

        Components.ShowAlertAdvice("Deseja excluir \"" + selected.getDescricao() + "\"?", () -> Async.Run(() -> {
            try {
                contaService.excluir(selected.getId());
                UI.runOnUi(() -> {
                    allDataList.removeIf(c -> c.getId().equals(selected.getId()));
                    Components.ShowPopup(ctx, "Conta excluída com sucesso!");
                    clearForm();
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao excluir: " + e.getMessage()));
            }
        }));
    }

    public void registrarPagamento(ScreenContext ctx) {
        var selected = contaSelected.get();
        if (selected == null) {
            UI.runOnUi(() -> Components.ShowAlertError("Selecione uma conta para registrar pagamento"));
            return;
        }

        var valorPagamentoBig = Utils.deCentavosParaReal(valorPagamento.get());

        if (valorPagamentoBig.compareTo(BigDecimal.ZERO) <= 0) {
            UI.runOnUi(() -> Components.ShowAlertError("Informe um valor de pagamento maior que zero"));
            return;
        }

        if (valorPagamentoBig.compareTo(selected.getValorRestante()) > 0) {
            UI.runOnUi(() -> Components.ShowAlertError("Valor do pagamento não pode ser maior que o valor restante"));
            return;
        }

        Async.Run(() -> {
            try {
                contaService.registrarPagamento(selected.getId(), valorPagamentoBig);
                var updated = contaService.buscarById(selected.getId());

                if (updated.getFornecedorId() != null) {
                    fornecedores.get().stream()
                            .filter(f -> f.getId().equals(updated.getFornecedorId()))
                            .findFirst()
                            .ifPresent(updated::setFornecedor);
                }

                UI.runOnUi(() -> {
                    allDataList.updateIf(c -> c.getId().equals(selected.getId()), c -> updated);
                    Components.ShowPopup(ctx, "Pagamento registrado com sucesso!");
                    valorPagamento.set("0");
                    modoPagamento.set(false);
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao registrar pagamento: " + e.getMessage()));
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
                contaService.registrarPagamento(selected.getId(), selected.getValorRestante());
                var updated = contaService.buscarById(selected.getId());

                if (updated.getFornecedorId() != null) {
                    fornecedores.get().stream()
                            .filter(f -> f.getId().equals(updated.getFornecedorId()))
                            .findFirst()
                            .ifPresent(updated::setFornecedor);
                }

                UI.runOnUi(() -> {
                    allDataList.updateIf(c -> c.getId().equals(selected.getId()), c -> updated);
                    Components.ShowPopup(ctx, "Conta quitada com sucesso!");
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao quitar conta: " + e.getMessage()));
            }
        });
    }

    private void asyncSalvar() {
        Async.Run(() -> {
            try {
                var model = new ContasPagarModel();
                fillModelFromForm(model, true);
                var salvo = contaService.salvar(model);
                salvo.setFornecedor(fornecedorSelected.get());

                UI.runOnUi(() -> {
                    allDataList.add(salvo);
                    Components.ShowPopup(ctx, "Conta cadastrada com sucesso!");
                    clearForm();
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
                selected.setFornecedor(fornecedorSelected.get());

                UI.runOnUi(() -> {
                    allDataList.updateIf(c -> c.getId().equals(selected.getId()), c -> selected);
                    Components.ShowPopup(ctx, "Conta atualizada com sucesso!");
                    clearForm();
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao atualizar: " + e.getMessage()));
            }
        });
    }

    private void fillModelFromForm(ContasPagarModel model, boolean isNew) {
        model.setDescricao(descricao.get());
        model.setValorOriginal(Utils.deCentavosParaReal(valorOriginal.get()));
        model.setDataVencimento(DateUtils.localDateParaMillis(dataVencimento.get()));
        model.setDataPagamento(dataPagamento.get() != null ? DateUtils.localDateParaMillis(dataPagamento.get()) : null);
        model.setStatus(status.get());
        model.setFornecedorId(fornecedorSelected.get() != null ? fornecedorSelected.get().getId() : null);
        model.setCompraId(null);
        model.setNumeroDocumento(numeroDocumento.get());
        model.setTipoDocumento(tipoDocumento.get());
        model.setObservacao(observacao.get());

        if (isNew) {
            model.setValorPago(BigDecimal.ZERO);
            if (model.getValorOriginal() != null) {
                model.setValorRestante(model.getValorOriginal());
            }
        } else {
            if (model.getValorOriginal() != null && model.getValorPago() != null) {
                var restante = model.getValorOriginal().subtract(model.getValorPago());
                model.setValorRestante(restante.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : restante);
            }
        }
    }

    private void attachFornecedores(List<ContasPagarModel> contasList) {
        for (var conta : contasList) {
            if (conta.getFornecedorId() != null) {
                fornecedores.get().stream()
                        .filter(f -> f.getId().equals(conta.getFornecedorId()))
                        .findFirst()
                        .ifPresent(conta::setFornecedor);
            }
        }
    }

    @Override
    public void onDestroy() throws Exception {
        this.contaService.close();
        this.fornecedorService.close();
    }
}
