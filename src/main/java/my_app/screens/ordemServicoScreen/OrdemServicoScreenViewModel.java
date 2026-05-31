package my_app.screens.ordemServicoScreen;

import megalodonte.ComputedState;
import megalodonte.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.ListState;
import my_app.db.models.ClienteModel;
import my_app.db.models.OrdemServicoModel;
import my_app.db.models.TecnicoModel;
import my_app.db.services.ClienteService;
import my_app.db.services.OrdemServicoService;
import my_app.db.services.TecnicoService;
import my_app.domain.components.Components;
import my_app.core.events.EntityEvent;
import my_app.core.events.EventBus;
import my_app.lifecycle.viewmodel.component.ViewModelScreenContract;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class OrdemServicoScreenViewModel extends ViewModelScreenContract {

    private final OrdemServicoService service;
    private final ClienteService clienteService;
    private final TecnicoService tecnicoService;

    public final ListState<OrdemServicoModel> ordensDeServico = ListState.of(List.of());
    public final ListState<ClienteModel> clientes = ListState.of(List.of());
    public final ListState<TecnicoModel> tecnicos = ListState.of(List.of());

    public final State<ClienteModel> clienteSelected = State.of(null);
    public final State<TecnicoModel> tecnicoSelected = State.of(null);
    public final State<OrdemServicoModel> osSelected = State.of(null);

    public final State<String> equipamento = State.of("");
    public final State<String> maoDeObra = State.of("0");
    public final State<String> pecasValor = State.of("0");

    public final List<String> tiposPagamento = List.of("A VISTA", "CRÉDITO", "DÉBITO", "PIX");
    public final State<String> tipoPagamentoSelected = State.of(tiposPagamento.get(1));

    public final List<String> statusOptions = List.of("Aberto", "Aguardando peça", "Autorizado", "Cancelado", "Em andamento", "Faturado", "Finalizado", "Orçamento");
    public final State<String> statusSelected = State.of(statusOptions.getFirst());

    public final State<LocalDate> dataVisita = State.of(LocalDate.now());
    public final State<String> checklistRelatorio = State.of("");

    public final ComputedState<String> totalLiquido = ComputedState.of(() -> {
        double maoObraValue = Double.parseDouble(maoDeObra.get()) / 100.0;
        double pecasValue = Double.parseDouble(pecasValor.get()) / 100.0;
        return String.valueOf(maoObraValue + pecasValue);
    }, maoDeObra, pecasValor);

    public OrdemServicoScreenViewModel(ScreenContext ctx) {
        super(ctx);
        try {
            service = new OrdemServicoService();
            clienteService = new ClienteService();
            tecnicoService = new TecnicoService();
        } catch (SQLException e) {
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            throw new RuntimeException(e);
        }

        EventBus.getInstance().subscribe(event -> {
            if (event instanceof EntityEvent<?> ee && ee.entity() instanceof TecnicoModel) {
                refreshTecnicos();
            }
        });
    }

    public void loadInicial() {
        Async.Run(() -> {
            try {
                var oss = service.listar();
                var clientesList = clienteService.listar();
                var tecnicosList = tecnicoService.listar();

                attachClientesTecnicos(oss, clientesList, tecnicosList);

                final var clientesCopy = List.copyOf(clientesList);

                UI.runOnUi(() -> {
                    ordensDeServico.clear();
                    ordensDeServico.addAll(oss);
                    clientes.addAll(clientesCopy);
                    if (!clientesCopy.isEmpty()) {
                        clienteSelected.set(clientesCopy.getFirst());
                    }
                    tecnicos.addAll(tecnicosList);
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    private void refreshTecnicos() {
        Async.Run(() -> {
            try {
                var tecnicosList = tecnicoService.listar();
                UI.runOnUi(() -> {
                    tecnicos.clear();
                    tecnicos.addAll(tecnicosList);
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    @Override
    public void populateFromModel() {
        if (osSelected.get() == null) return;
        var os = osSelected.get();

        clienteSelected.set(os.getCliente());
        tecnicoSelected.set(os.getTecnico());
        equipamento.set(os.getEquipamento());
        tipoPagamentoSelected.set(os.getTipoPagamento());
        statusSelected.set(os.getStatus());
        dataVisita.set(os.getDataEscolhida() != null ? DateUtils.millisParaLocalDate(os.getDataEscolhida()) : LocalDate.now());
        maoDeObra.set(Utils.deRealParaCentavos(os.getMaoDeObraValor()));
        pecasValor.set(Utils.deRealParaCentavos(os.getPecasValor()));
        checklistRelatorio.set(os.getChecklistRelatorio());
    }

    @Override
    public void clearForm() {
        clienteSelected.set(!clientes.isEmpty() ? clientes.get(0) : null);
        tecnicoSelected.set(null);
        equipamento.set("");
        tipoPagamentoSelected.set(tiposPagamento.get(1));
        statusSelected.set(statusOptions.getFirst());
        dataVisita.set(LocalDate.now());
        maoDeObra.set("0");
        pecasValor.set("0");
        checklistRelatorio.set("");
        osSelected.set(null);
    }

    @Override
    public void handleAddOrUpdate() {
        if (tecnicoSelected.get() == null) {
            UI.runOnUi(() -> Components.ShowAlertError("Técnico não foi selecionado"));
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
        var selected = osSelected.get();
        if (selected == null) return;

        Components.ShowAlertAdvice("Deseja excluir a O.S #" + selected.getNumeroOs() + "?", () -> {
            Async.Run(() -> {
                try {
                    service.excluir(selected.getId());
                    UI.runOnUi(() -> {
                        ordensDeServico.removeIf(os -> os.getId().equals(selected.getId()));
                        Components.ShowPopup(ctx, "Ordem de serviço excluída com sucesso!");
                        clearForm();
                    });
                } catch (Exception e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao excluir: " + e.getMessage()));
                }
            });
        });
    }

    public void openTecnicoWindow() {
        ctx.router().spawnWindow("tecnicos", e -> {});
    }

    private void asyncSalvar() {
        Async.Run(() -> {
            try {
                var model = new OrdemServicoModel();
                fillModelFromForm(model);
                var salvo = service.salvar(model);
                salvo.setCliente(clienteSelected.get());
                salvo.setTecnico(tecnicoSelected.get());

                UI.runOnUi(() -> {
                    ordensDeServico.add(salvo);
                    Components.ShowPopup(ctx, "Ordem de serviço salva com sucesso!");
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
                var selected = osSelected.get();
                if (selected == null) return;
                fillModelFromForm(selected);
                service.atualizar(selected);
                selected.setCliente(clienteSelected.get());
                selected.setTecnico(tecnicoSelected.get());

                UI.runOnUi(() -> {
                    ordensDeServico.updateIf(os -> os.getId().equals(selected.getId()), os -> selected);
                    Components.ShowPopup(ctx, "Ordem de serviço atualizada com sucesso!");
                    clearForm();
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao atualizar: " + e.getMessage()));
            }
        });
    }

    private void fillModelFromForm(OrdemServicoModel model) {
        model.setClienteId(clienteSelected.get() != null ? clienteSelected.get().getId() : null);
        model.setTecnicoId(tecnicoSelected.get() != null ? tecnicoSelected.get().getId() : null);
        model.setEquipamento(equipamento.get());
        model.setMaoDeObraValor(Utils.deCentavosParaReal(maoDeObra.get()));
        model.setPecasValor(Utils.deCentavosParaReal(pecasValor.get()));
        model.setTipoPagamento(tipoPagamentoSelected.get());
        model.setStatus(statusSelected.get());
        model.setChecklistRelatorio(checklistRelatorio.get());
        model.setDataEscolhida(DateUtils.localDateParaMillis(dataVisita.get()));
        model.setTotalLiquido(new BigDecimal(totalLiquido.get()));
    }

    private void attachClientesTecnicos(List<OrdemServicoModel> oss, List<ClienteModel> clientesList, List<TecnicoModel> tecnicosList) {
        for (var os : oss) {
            if (os.getClienteId() != null) {
                clientesList.stream()
                        .filter(c -> c.getId().equals(os.getClienteId()))
                        .findFirst()
                        .ifPresent(os::setCliente);
            }
            if (os.getTecnicoId() != null) {
                tecnicosList.stream()
                        .filter(t -> t.getId().equals(os.getTecnicoId()))
                        .findFirst()
                        .ifPresent(os::setTecnico);
            }
        }
    }
}
