package my_app.screens.comprasAPagarScreen;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import megalodonte.ComputedState;
import megalodonte.State;
import megalodonte.base.async.Async;
import megalodonte.base.UI;
import megalodonte.router.Router;
import my_app.db.dto.ContasPagarDto;
import my_app.db.models.ContasPagarModel;
import my_app.db.models.FornecedorModel;
import my_app.db.repositories.ContasPagarRepository;
import my_app.db.repositories.FornecedorRepository;
import my_app.events.DadosFinanceirosAtualizadosEvent;
import my_app.events.EventBus;
import my_app.lifecycle.viewmodel.component.ViewModel;
import my_app.screens.components.Components;
import my_app.services.ContasPagarService;
import my_app.utils.Utils;
import my_app.utils.DateUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ComprasAPagarScreenViewModel extends ViewModel {
    private final ContasPagarRepository repository = new ContasPagarRepository();
    private final FornecedorRepository fornecedorRepository = new FornecedorRepository();
    private final ContasPagarService service = new ContasPagarService();

    // Lists for dropdowns
    public final List<String> statusOptions = List.of("TODOS", "PENDENTE", "PAGO", "PARCIAL", "ATRASADO", "CANCELADO");
    public final List<String> tipoDocumentoOptions = List.of("DUPLICATA", "BOLETO", "NOTA FISCAL", "CHEQUE", "OUTRO");
    
    // Observable list for table
    public final ObservableList<ContasPagarModel> contas = FXCollections.observableArrayList();
    
    // Form states
    public final State<String> descricao = State.of("");
    public final State<String> valorOriginal = State.of("0"); // in cents
    public final State<BigDecimal> valorPago = State.of(BigDecimal.ZERO);
    public final State<LocalDate> dataVencimento = State.of(LocalDate.now());
    public final State<LocalDate> dataPagamento = State.of(null);
    public final State<String> status = State.of("PENDENTE");
    public final State<String> tipoDocumento = State.of("DUPLICATA");
    public final State<String> numeroDocumento = State.of("");
    public final State<String> observacao = State.of("");
    
    // Related data
    public final State<List<FornecedorModel>> fornecedores = State.of(List.of());
    public final State<FornecedorModel> fornecedorSelected = State.of(null);
    public final State<ContasPagarModel> contaSelected = State.of(null);
    public final State<String> statusOptionSelected = State.of(statusOptions.getFirst());
    
    // UI states
    public final State<Boolean> modoEdicao = State.of(false);
    public final State<Boolean> modoPagamento = State.of(false);
    public final State<String> valorPagamento = State.of("0");
    
    // Computed states
    public final ComputedState<String> btnText = ComputedState.of(() -> 
        modoEdicao.get() ? "Atualizar" : "+ Adicionar", modoEdicao);
        
    public final ComputedState<String> btnPagamentoText = ComputedState.of(() -> 
        modoPagamento.get() ? "Registrar Pagamento" : "Pagar", modoPagamento);
        
    public final ComputedState<Boolean> formValido = ComputedState.of(() -> {
        return !descricao.get().trim().isEmpty() && 
               !valorOriginal.get().equals("0") && 
               fornecedorSelected.get() != null &&
               dataVencimento.get() != null;
    }, descricao, valorOriginal, fornecedorSelected, dataVencimento);
    
    public final ComputedState<Boolean> pagamentoValido = ComputedState.of(() -> {
        // Para o botão "Pagar", só verifica se há conta selecionada
        // A validação do valor acontece no momento do registro do pagamento
        return contaSelected.get() != null;
    }, contaSelected);

    
    // Business logic methods
    public void loadInicial() {
        Async.Run(() -> {
            try {
                var contasList = repository.listar();
                var fornecedoresList = fornecedorRepository.listar();
                
                // Load related fornecedores for each conta
                for (ContasPagarModel conta : contasList) {
                    if (conta.fornecedorId != null) {
                        FornecedorModel fornecedor = fornecedoresList.stream()
                            .filter(f -> f.id.equals(conta.fornecedorId))
                            .findFirst()
                            .orElse(null);
                        conta.fornecedor = fornecedor;
                    }
                }

                contas.setAll(contasList);
                fornecedores.set(fornecedoresList);
                if (!fornecedoresList.isEmpty()) {
                    fornecedorSelected.set(fornecedoresList.getFirst());
                }

//                UI.runOnUi(() -> {
//                    contas.setAll(contasList);
//                    fornecedores.set(fornecedoresList);
//                    if (!fornecedoresList.isEmpty()) {
//                        fornecedorSelected.set(fornecedoresList.getFirst());
//                    }
//                });
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
                    contasFiltradas = repository.listar();
                } else {
                    contasFiltradas = repository.buscarPorStatus(statusFiltro);
                }
                
                // Load related fornecedores
                var fornecedoresList = fornecedorRepository.listar();
                for (ContasPagarModel conta : contasFiltradas) {
                    if (conta.fornecedorId != null) {
                        FornecedorModel fornecedor = fornecedoresList.stream()
                            .filter(f -> f.id.equals(conta.fornecedorId))
                            .findFirst()
                            .orElse(null);
                        conta.fornecedor = fornecedor;
                    }
                }
                
                UI.runOnUi(() -> {
                    contas.setAll(contasFiltradas);
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }
    
    public void loadVencidas() {
        Async.Run(() -> {
            try {
                var contasVencidas = repository.buscarVencidas();
                
                // Load related fornecedores
                var fornecedoresList = fornecedorRepository.listar();
                for (ContasPagarModel conta : contasVencidas) {
                    if (conta.fornecedorId != null) {
                        FornecedorModel fornecedor = fornecedoresList.stream()
                            .filter(f -> f.id.equals(conta.fornecedorId))
                            .findFirst()
                            .orElse(null);
                        conta.fornecedor = fornecedor;
                    }
                }
                
                UI.runOnUi(() -> {
                    contas.setAll(contasVencidas);
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }
    
    public ContasPagarDto toDto() {
        return new ContasPagarDto(
            descricao.get(),
            Utils.deCentavosParaReal(valorOriginal.get()),
            valorPago.get(),
            Utils.deCentavosParaReal(valorOriginal.get()).subtract(valorPago.get()),
            DateUtils.localDateParaMillis(dataVencimento.get()),
            dataPagamento.get() != null ? DateUtils.localDateParaMillis(dataPagamento.get()) : null,
            status.get(),
            fornecedorSelected.get() != null ? fornecedorSelected.get().id : null,
            null, // compraId - can be set later if linked to a purchase
            numeroDocumento.get(),
            tipoDocumento.get(),
            observacao.get()
        );
    }
    
    public void salvarOuAtualizar(Router router) {
        if (!formValido.get()) {
            UI.runOnUi(() -> Components.ShowAlertError("Preencha todos os campos obrigatórios"));
            return;
        }
        
        var dto = toDto();
        
        if (modoEdicao.get()) {
            // Update logic
            Async.Run(() -> {
                try {
                    ContasPagarModel modelAtualizada = new ContasPagarModel().fromIdAndDto(contaSelected.get().id, dto);
                    repository.atualizar(modelAtualizada);
                    UI.runOnUi(() -> {
                        // Update item in observable list
                        int index = contas.indexOf(contaSelected.get());
                        if (index >= 0) {
                            contas.set(index, modelAtualizada);
                        }
                        Components.ShowPopup(router, "Conta atualizada com sucesso!");
                        limparFormulario();
                    });
                } catch (Exception e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao atualizar: " + e.getMessage()));
                }
            });
        } else {
            // Save logic
            Async.Run(() -> {
                try {
                    var contaSalva = repository.salvar(dto);
                    UI.runOnUi(() -> {
                        contas.add(contaSalva);
                        Components.ShowPopup(router, "Conta cadastrada com sucesso!");
                        limparFormulario();
                    });
                } catch (Exception e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao salvar: " + e.getMessage()));
                }
            });
        }
    }
    
    public void registrarPagamento(Router router) {
        if (contaSelected.get() == null) {
            UI.runOnUi(() -> Components.ShowAlertError("Selecione uma conta para registrar pagamento"));
            return;
        }
        
        BigDecimal valorPagamentoBig = Utils.deCentavosParaReal(valorPagamento.get());
        
        // Validar valor do pagamento
        if (valorPagamentoBig.compareTo(BigDecimal.ZERO) <= 0) {
            UI.runOnUi(() -> Components.ShowAlertError("Informe um valor de pagamento maior que zero"));
            return;
        }
        
        if (valorPagamentoBig.compareTo(contaSelected.get().valorRestante) > 0) {
            UI.runOnUi(() -> Components.ShowAlertError("Valor do pagamento não pode ser maior que o valor restante"));
            return;
        }
        
        Async.Run(() -> {
            try {
                service.registrarPagamento(contaSelected.get().id, valorPagamentoBig);
                UI.runOnUi(() -> {
                    // Update the item in the list
                    try {
                        ContasPagarModel contaAtualizada = repository.buscarById(contaSelected.get().id);
                        int index = contas.indexOf(contaSelected.get());
                        if (index >= 0) {
                            contas.set(index, contaAtualizada);
                        }
                        Components.ShowPopup(router, "Pagamento registrado com sucesso!");
                        valorPagamento.set("0");
                        modoPagamento.set(false);
                        EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                    } catch (Exception ex) {
                        Components.ShowAlertError("Erro ao atualizar lista: " + ex.getMessage());
                    }
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao registrar pagamento: " + e.getMessage()));
            }
        });
    }
    
    public void quitarConta(Router router) {
        if (contaSelected.get() == null) {
            UI.runOnUi(() -> Components.ShowAlertError("Selecione uma conta para quitar"));
            return;
        }
        
        Async.Run(() -> {
            try {
                service.registrarPagamento(contaSelected.get().id, contaSelected.get().valorRestante);
                UI.runOnUi(() -> {
                    try {
                        // Update the item in the list
                        ContasPagarModel contaAtualizada = repository.buscarById(contaSelected.get().id);
                        int index = contas.indexOf(contaSelected.get());
                        if (index >= 0) {
                            contas.set(index, contaAtualizada);
                        }
                        Components.ShowPopup(router, "Conta quitada com sucesso!");
                        EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                    } catch (Exception ex) {
                        Components.ShowAlertError("Erro ao atualizar lista: " + ex.getMessage());
                    }
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao quitar conta: " + e.getMessage()));
            }
        });
    }
    
    public void excluir(Router router) {
        if (contaSelected.get() == null) {
            UI.runOnUi(() -> Components.ShowAlertError("Selecione uma conta para excluir"));
            return;
        }
        
        Async.Run(() -> {
            try {
                service.excluir(contaSelected.get().id);
                UI.runOnUi(() -> {
                    contas.remove(contaSelected.get());
                    Components.ShowPopup(router, "Conta excluída com sucesso!");
                    limparFormulario();
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao excluir: " + e.getMessage()));
            }
        });
    }
    
    public void editar() {
        if (contaSelected.get() == null) {
            UI.runOnUi(() -> Components.ShowAlertError("Selecione uma conta para editar"));
            return;
        }
        
        var conta = contaSelected.get();
        descricao.set(conta.descricao);
        valorOriginal.set(Utils.deRealParaCentavos(conta.valorOriginal));
        valorPago.set(conta.valorPago);
        dataVencimento.set(DateUtils.millisParaLocalDate(conta.dataVencimento));
        dataPagamento.set(conta.dataPagamento != null ? DateUtils.millisParaLocalDate(conta.dataPagamento) : null);
        status.set(conta.status);
        tipoDocumento.set(conta.tipoDocumento);
        numeroDocumento.set(conta.numeroDocumento);
        observacao.set(conta.observacao);
        
        // Find and set fornecedor by ID
        if (conta.fornecedorId != null) {
            Async.Run(() -> {
                try {
                    FornecedorModel fornecedor = fornecedorRepository.buscarById(conta.fornecedorId);
                    UI.runOnUi(() -> fornecedorSelected.set(fornecedor));
                } catch (Exception e) {
                    System.err.println("Erro ao buscar fornecedor: " + e.getMessage());
                }
            });
        }
        
        modoEdicao.set(true);
    }
    
    public void limparFormulario() {
        descricao.set("");
        valorOriginal.set("0");
        valorPago.set(BigDecimal.ZERO);
        dataVencimento.set(LocalDate.now());
        dataPagamento.set(null);
        status.set("PENDENTE");
        tipoDocumento.set("DUPLICATA");
        numeroDocumento.set("");
        observacao.set("");
        modoEdicao.set(false);
        valorPagamento.set("0");
        modoPagamento.set(false);
        contaSelected.set(null);
        
        if (!fornecedores.get().isEmpty()) {
            fornecedorSelected.set(fornecedores.get().getFirst());
        }
    }
    
    public void carregarParaEdicao(ContasPagarModel conta) {
        if (conta == null) return;
        
        descricao.set(conta.descricao);
        valorOriginal.set(Utils.deRealParaCentavos(conta.valorOriginal));
        valorPago.set(conta.valorPago);
        dataVencimento.set(DateUtils.millisParaLocalDate(conta.dataVencimento));
        dataPagamento.set(conta.dataPagamento != null ? DateUtils.millisParaLocalDate(conta.dataPagamento) : null);
        status.set(conta.status);
        tipoDocumento.set(conta.tipoDocumento);
        numeroDocumento.set(conta.numeroDocumento);
        observacao.set(conta.observacao);
        
        // Find and set fornecedor by ID
        if (conta.fornecedorId != null) {
            Async.Run(() -> {
                try {
                    FornecedorModel fornecedor = fornecedorRepository.buscarById(conta.fornecedorId);
                    UI.runOnUi(() -> fornecedorSelected.set(fornecedor));
                } catch (Exception e) {
                    System.err.println("Erro ao buscar fornecedor: " + e.getMessage());
                }
            });
        }
        
        modoEdicao.set(true);
    }
    
    public BigDecimal getTotalEmAberto() {
        try {
            return service.getTotalEmAberto();
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
    
    public BigDecimal getTotalVencidas() {
        try {
            return service.getTotalVencidas();
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}