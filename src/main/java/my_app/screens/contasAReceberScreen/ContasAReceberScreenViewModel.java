package my_app.screens.contasAReceberScreen;

import megalodonte.ComputedState;
import megalodonte.ListState;
import megalodonte.State;
import megalodonte.base.async.Async;
import megalodonte.base.UI;
import megalodonte.router.Router;
import my_app.db.dto.ContaAreceberDto;
import my_app.db.models.ClienteModel;
import my_app.db.models.ContaAreceberModel;
import my_app.db.repositories.ContasAReceberRepository;
import my_app.db.repositories.ClienteRepository;
import my_app.db.repositories.VendaRepository;
import my_app.events.DadosFinanceirosAtualizadosEvent;
import my_app.events.EventBus;
import my_app.lifecycle.viewmodel.component.ViewModel;
import my_app.screens.components.Components;
import my_app.services.ContasAReceberService;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ContasAReceberScreenViewModel extends ViewModel {
    private final ContasAReceberRepository repository = new ContasAReceberRepository();
    private final ClienteRepository clienteRepository = new ClienteRepository();
    private final ContasAReceberService service = new ContasAReceberService(new VendaRepository(), clienteRepository);

    // Lists for dropdowns
    public final List<String> statusOptions = List.of("TODOS", "PENDENTE", "PAGO", "PARCIAL", "ATRASADO", "CANCELADO");
    public final List<String> tipoDocumentoOptions = List.of("DUPLICATA", "BOLETO", "NOTA FISCAL", "CHEQUE", "OUTRO");
    
    // Observable list for table
    public final ListState<ContaAreceberModel> contas = ListState.of(List.of());
    
    // Form states
    public final State<String> descricao = State.of("");
    public final State<String> valorOriginal = State.of("0"); // in cents
    public final State<BigDecimal> valorRecebido = State.of(BigDecimal.ZERO);
    public final State<LocalDate> dataVencimento = State.of(LocalDate.now());
    public final State<LocalDate> dataRecebimento = State.of(null);
    public final State<String> status = State.of("PENDENTE");
    public final State<String> tipoDocumento = State.of("DUPLICATA");
    public final State<String> numeroDocumento = State.of("");
    public final State<String> observacao = State.of("");
    
    // Related data
    public final State<List<ClienteModel>> clientes = State.of(List.of());
    public final State<ClienteModel> clienteSelected = State.of(null);
    public final State<ContaAreceberModel> contaSelected = State.of(null);
    public final State<String> statusOptionSelected = State.of(statusOptions.getFirst());
    
    // UI states
    public final State<Boolean> modoEdicao = State.of(false);
    public final State<Boolean> modoRecebimento = State.of(false);
    public final State<String> valorRecebimento = State.of("0");
    
    // Computed states
    public final ComputedState<String> btnText = ComputedState.of(() -> 
        modoEdicao.get() ? "Atualizar" : "+ Adicionar", modoEdicao);
        
    public final ComputedState<String> btnRecebimentoText = ComputedState.of(() -> 
        modoRecebimento.get() ? "Registrar Recebimento" : "Pagar", modoRecebimento);
        
    public final ComputedState<Boolean> formValido = ComputedState.of(() -> {
        return !descricao.get().trim().isEmpty() && 
               !valorOriginal.get().equals("0") && 
               clienteSelected.get() != null &&
               dataVencimento.get() != null;
    }, descricao, valorOriginal, clienteSelected, dataVencimento);
    
    public final ComputedState<Boolean> recebimentoValido = ComputedState.of(() -> {
        // Para o botão "Pagar", só verifica se há conta selecionada
        // A validação do valor acontece no momento do registro do recebimento
        return contaSelected.get() != null;
    }, contaSelected);

    
    // Business logic methods
    public void loadInicial() {
        Async.Run(() -> {
            try {
                var contasList = repository.listar();
                var clienteList = clienteRepository.listar();
                
                // Load related clientes for each conta
                for (var conta : contasList) {
                    if (conta.clienteId != null) {
                        var cliente = clienteList.stream()
                            .filter(f -> f.id.equals(conta.clienteId))
                            .findFirst()
                            .orElse(null);
                        conta.cliente = cliente;
                    }
                }

                UI.runOnUi(() -> {
                    contas.addAll(contasList);
                    clientes.set(clienteList);
                    if (!clienteList.isEmpty()) {
                        clienteSelected.set(clienteList.getFirst());
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
                    contasFiltradas = repository.listar();
                } else {
                    contasFiltradas = repository.buscarPorStatus(statusFiltro);
                }
                
                // Load related clientes
                var clientesList = clienteRepository.listar();
                for (var conta : contasFiltradas) {
                    if (conta.clienteId != null) {
                        var cliente = clientesList.stream()
                            .filter(f -> f.id.equals(conta.clienteId))
                            .findFirst()
                            .orElse(null);
                        conta.cliente = cliente;
                    }
                }
                
                UI.runOnUi(() -> {
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
                var contasVencidas = repository.buscarVencidas();
                
                // Load related clientes
                var clientesList = clienteRepository.listar();
                for (var conta : contasVencidas) {
                    if (conta.clienteId != null) {
                        ClienteModel cliente = clientesList.stream()
                            .filter(f -> f.id.equals(conta.clienteId))
                            .findFirst()
                            .orElse(null);
                        conta.cliente = cliente;
                    }
                }
                
                UI.runOnUi(() -> {
                    contas.addAll(contasVencidas);
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }
    
    public ContaAreceberDto toDto() {
        return new ContaAreceberDto(
            descricao.get(),
            Utils.deCentavosParaReal(valorOriginal.get()),
            valorRecebido.get(),
            Utils.deCentavosParaReal(valorOriginal.get()).subtract(valorRecebido.get()),
            DateUtils.localDateParaMillis(dataVencimento.get()),
            dataRecebimento.get() != null ? DateUtils.localDateParaMillis(dataRecebimento.get()) : null,
            status.get(),
            clienteSelected.get() != null ? clienteSelected.get().id : null,
            null, // vendaId - can be set later if linked to a purchase
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
                    final var modelAtualizada = new ContaAreceberModel().fromIdAndDto(contaSelected.get().id, dto);
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
                    EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                });
                } catch (Exception e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao salvar: " + e.getMessage()));
                }
            });
        }
    }
    
    public void registrarRecebimento(Router router) {
        if (contaSelected.get() == null) {
            UI.runOnUi(() -> Components.ShowAlertError("Selecione uma conta para registrar recebimento"));
            return;
        }
        
        BigDecimal valorRecebimentoBig = Utils.deCentavosParaReal(valorRecebimento.get());
        
        // Validar valor do recebimento
        if (valorRecebimentoBig.compareTo(BigDecimal.ZERO) <= 0) {
            UI.runOnUi(() -> Components.ShowAlertError("Informe um valor de recebimento maior que zero"));
            return;
        }
        
        if (valorRecebimentoBig.compareTo(contaSelected.get().valorRestante) > 0) {
            UI.runOnUi(() -> Components.ShowAlertError("Valor do recebimento não pode ser maior que o valor restante"));
            return;
        }
        
        Async.Run(() -> {
            try {
                service.registrarRecebimento(contaSelected.get().id, valorRecebimentoBig);
                UI.runOnUi(() -> {
                    // Update the item in the list
                    try {
                        var contaAtualizada = repository.buscarById(contaSelected.get().id);
                        int index = contas.indexOf(contaSelected.get());
                        if (index >= 0) {
                            contas.set(index, contaAtualizada);
                        }
                        Components.ShowPopup(router, "Recebimento registrado com sucesso!");
                        valorRecebimento.set("0");
                        modoRecebimento.set(false);
                        EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                    } catch (Exception ex) {
                        Components.ShowAlertError("Erro ao atualizar lista: " + ex.getMessage());
                    }
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao registrar recebimento: " + e.getMessage()));
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
                service.registrarRecebimento(contaSelected.get().id, contaSelected.get().valorRestante);
                UI.runOnUi(() -> {
                    try {
                        // Update the item in the list
                        var contaAtualizada = repository.buscarById(contaSelected.get().id);
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
        valorRecebido.set(conta.valorRecebido);
        dataVencimento.set(DateUtils.millisParaLocalDate(conta.dataVencimento));
        dataRecebimento.set(conta.dataRecebimento != null ? DateUtils.millisParaLocalDate(conta.dataRecebimento) : null);
        status.set(conta.status);
        tipoDocumento.set(conta.tipoDocumento);
        numeroDocumento.set(conta.numeroDocumento);
        observacao.set(conta.observacao);
        
        // Find and set cliente by ID
        if (conta.clienteId != null) {
            Async.Run(() -> {
                try {
                    ClienteModel cliente = clienteRepository.buscarById(conta.clienteId);
                    UI.runOnUi(() -> clienteSelected.set(cliente));
                } catch (Exception e) {
                    System.err.println("Erro ao buscar cliente: " + e.getMessage());
                }
            });
        }
        
        modoEdicao.set(true);
    }
    
    public void limparFormulario() {
        descricao.set("");
        valorOriginal.set("0");
        valorRecebido.set(BigDecimal.ZERO);
        dataVencimento.set(LocalDate.now());
        dataRecebimento.set(null);
        status.set("PENDENTE");
        tipoDocumento.set("DUPLICATA");
        numeroDocumento.set("");
        observacao.set("");
        modoEdicao.set(false);
        valorRecebimento.set("0");
        modoRecebimento.set(false);
        contaSelected.set(null);
        
        if (!clientes.get().isEmpty()) {
            clienteSelected.set(clientes.get().getFirst());
        }
    }
    
    public void carregarParaEdicao(ContaAreceberModel conta) {
        if (conta == null) return;
        
        descricao.set(conta.descricao);
        valorOriginal.set(Utils.deRealParaCentavos(conta.valorOriginal));
        valorRecebido.set(conta.valorRecebido);
        dataVencimento.set(DateUtils.millisParaLocalDate(conta.dataVencimento));
        dataRecebimento.set(conta.dataRecebimento != null ? DateUtils.millisParaLocalDate(conta.dataRecebimento) : null);
        status.set(conta.status);
        tipoDocumento.set(conta.tipoDocumento);
        numeroDocumento.set(conta.numeroDocumento);
        observacao.set(conta.observacao);
        
        // Find and set cliente by ID
        if (conta.clienteId != null) {
            Async.Run(() -> {
                try {
                    ClienteModel cliente = clienteRepository.buscarById(conta.clienteId);
                    UI.runOnUi(() -> clienteSelected.set(cliente));
                } catch (Exception e) {
                    System.err.println("Erro ao buscar cliente: " + e.getMessage());
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