package my_app.screens.ordemServicoScreen;

import megalodonte.ComputedState;
import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.v2.ListState;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.ClienteModel;
import my_app.db.models.OrdemServicoModel;
import my_app.db.models.TecnicoModel;
import my_app.db.services.ClienteService;
import my_app.db.services.OrdemServicoService;
import my_app.db.services.TecnicoService;
import my_app.domain.components.Components;
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

public class OrdemServicoScreenViewModel extends ViewModelScreenContract<OrdemServicoModel> {
    private static final Logger log = LoggerFactory.getLogger(OrdemServicoScreenViewModel.class);

    private final OrdemServicoService service;
    private final ClienteService clienteService;
    private final TecnicoService tecnicoService;

    public final ListState<ClienteModel> clientes = ListState.ofEmpty();
    public final ListState<TecnicoModel> tecnicos = ListState.ofEmpty();

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
        this.service = createOrReport(OrdemServicoService::new);
        this.clienteService = createOrReport(ClienteService::new);
        this.tecnicoService = createOrReport(TecnicoService::new);

        EventBus.getInstance().subscribe(event -> {
            if (event instanceof EntityEvent<?> ee && ee.entity() instanceof TecnicoModel) {
                refreshTecnicos();
            }
        });
    }

    @Override
    protected boolean matchesSearch(OrdemServicoModel model, String query) {
        return contains(model.getEquipamento(), query)
                || contains(model.getStatus(), query)
                || (model.getCliente() != null && contains(model.getCliente().getNome(), query))
                || (model.getTecnico() != null && contains(model.getTecnico().getNome(), query));
    }

    private boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    @Override
    public void fetchListData() {
        Async.Run(() -> {
            try {
                var oss = service.listar();
                var clientesList = clienteService.listar();
                var tecnicosList = tecnicoService.listar();

                attachClientesTecnicos(oss, clientesList, tecnicosList);

                final var clientesCopy = List.copyOf(clientesList);

                UI.runOnUi(() -> {
                    allDataList.set(oss);
                    clientes.addAll(clientesCopy);
                    if (!clientesCopy.isEmpty()) {
                        clienteSelected.set(clientesCopy.getFirst());
                    }
                    tecnicos.addAll(tecnicosList);
                });
            } catch (Exception e) {
                log.error("Erro ao buscar ordens de serviço", e);
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
                log.error("Erro ao atualizar lista de técnicos", e);
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    @Override
    public void populateFieldsFromModel() {
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
        if (modoEdicao.get() && osSelected.get() == null) return;

        // model montado aqui, síncrono (thread da UI) — não dentro do Async.Run de
        // asyncSalvar/asyncAtualizar. populateModelFromFields() lê modoEdicao.get()
        // internamente pra decidir se reaproveita osSelected ou cria um OrdemServicoModel
        // novo; chamado de dentro do Async.Run isso quase sempre lia modoEdicao já
        // resetado por ContratoTelaCrudV3.handleAddOrUpdate() (que reseta logo depois
        // de disparar essa chamada), fazendo toda edição tentar dar update num model
        // novo sem id (mesmo bug corrigido em outras telas).
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
        var selected = osSelected.get();
        if (selected == null) return;

        Components.ShowAlertAdvice("Deseja excluir a O.S #" + selected.getNumeroOs() + "?", () -> Async.Run(() -> {
            try {
                service.excluir(selected.getId());
                UI.runOnUi(() -> {
                    allDataList.removeIf(os -> os.getId().equals(selected.getId()));
                    Components.ShowPopup(ctx, "Ordem de serviço excluída com sucesso!");
                    clearForm();
                });
            } catch (Exception e) {
                log.error("Erro ao excluir ordem de serviço id={}", selected.getId(), e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao excluir: " + e.getMessage()));
            }
        }));
    }

    public void openTecnicoWindow() {
        ctx.router().spawnWindow("tecnicos", e -> {});
    }

    private void asyncSalvar(OrdemServicoModel model) {
        Async.Run(() -> {
            try {
                var salvo = service.salvar(model);
                salvo.setCliente(clienteSelected.get());
                salvo.setTecnico(tecnicoSelected.get());

                UI.runOnUi(() -> {
                    allDataList.add(salvo);
                    Components.ShowPopup(ctx, "Ordem de serviço salva com sucesso!");
                    clearForm();
                });
            } catch (Exception e) {
                log.error("Erro ao salvar ordem de serviço", e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao salvar: " + e.getMessage()));
            }
        });
    }

    private void asyncAtualizar(OrdemServicoModel model) {
        Async.Run(() -> {
            try {
                service.atualizar(model);
                model.setCliente(clienteSelected.get());
                model.setTecnico(tecnicoSelected.get());

                UI.runOnUi(() -> {
                    allDataList.updateIf(os -> os.getId().equals(model.getId()), os -> model);
                    Components.ShowPopup(ctx, "Ordem de serviço atualizada com sucesso!");
                    clearForm();
                });
            } catch (Exception e) {
                log.error("Erro ao atualizar ordem de serviço id={}", model.getId(), e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao atualizar: " + e.getMessage()));
            }
        });
    }

    @Override
    public OrdemServicoModel populateModelFromFields() {
        var model = modoEdicao.get() && osSelected.get() != null
                ? osSelected.get()
                : new OrdemServicoModel();

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

        return model;
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

    @Override
    public void onDestroy() throws Exception {
        this.service.close();
        this.clienteService.close();
        this.tecnicoService.close();
    }
}
