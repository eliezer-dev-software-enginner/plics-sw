package my_app.screens;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import megalodonte.ComputedState;
import megalodonte.ListState;
import megalodonte.State;
import megalodonte.async.Async;
import megalodonte.base.UI;
import megalodonte.components.*;
import megalodonte.props.ColumnProps;
import megalodonte.props.RowProps;
import megalodonte.props.TextProps;
import megalodonte.router.Router;
import megalodonte.utils.related.TextVariant;
import my_app.db.dto.OrdemServicoDto;
import my_app.db.models.*;
import my_app.db.repositories.*;
import my_app.domain.ContratoTelaCrud;
import my_app.events.EventBus;
import my_app.events.TecnicoCriadoEvent;
import my_app.screens.components.Components;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class OrdemServicoScreen implements ScreenComponent, ContratoTelaCrud {
    private final Router router;
    private final ListState<ClienteModel> clientes = ListState.of(List.of());
    private final ListState<TecnicoModel> tecnicos = ListState.of(List.of());

    State<ClienteModel> clienteSelected = State.of(null);
    State<TecnicoModel> tecnicoSelected = State.of(null);
    State<OrdemServicoModel> osSelected = State.of(null);

    private final ListState<OrdemServicoModel> ordensDeServicoList = ListState.of(List.of());

    State<String> equipamento = State.of("");

    List<String> tiposPagamento = List.of("A VISTA", "CRÉDITO", "DÉBITO", "PIX");
    State<String> tipoPagamentoSeleced = State.of(tiposPagamento.get(1));

    List<String> status = List.of("Aberto", "Aguardando peça", "Autorizado", "Cancelado", "Em andamento", "Faturado", "Finalizado", "Orçamento");
    State<String> statusSelecionado = State.of(status.getFirst());

    State<LocalDate> dataVisita = State.of(LocalDate.now());
    State<Boolean> modoEdicao = State.of(false);

    ComputedState<String> btnText = ComputedState.of(() -> modoEdicao.get() ? "Atualizar" : "+ Cadastrar", modoEdicao);

    State<String> checklistRelatorio = State.of("");

    State<String> maoDeObra = State.of("0");
    State<String> pecasValor = State.of("0");

    //TODO: no futuro deve ser tratado como String
    ComputedState<String> totalLiquido = ComputedState.of(() -> {
        double maoObraValue = Double.parseDouble(maoDeObra.get()) / 100.0;
        double pecasValorValue = Double.parseDouble(pecasValor.get()) / 100.0;

        return String.valueOf (maoObraValue + pecasValorValue);
    }, maoDeObra, pecasValor);


    private final OrdemServicoRepository ordemServicoRepository;
    private final ClienteRepository clienteRepository;
    private final TecnicoRepository tecnicoRepository;

    public OrdemServicoScreen(Router router) {
        this.router = router;

        clienteRepository = new ClienteRepository();
        tecnicoRepository = new TecnicoRepository();
        ordemServicoRepository = new OrdemServicoRepository();
        
        // Inscrever para receber eventos de criação de técnicos
        EventBus.getInstance().subscribe(event -> {
            if (event instanceof TecnicoCriadoEvent) {
                TecnicoModel novoTecnico = ((TecnicoCriadoEvent) event).getTecnico();
                UI.runOnUi(() -> {
                    tecnicos.add(novoTecnico);
                    // Se for o primeiro técnico, selecioná-lo automaticamente
                    if (tecnicos.size() == 1) {
                        tecnicoSelected.set(novoTecnico);
                    }
                });
            }
        });
    }

    @Override
    public void onMount() {fetchData();}

    private void fetchData() {
        Async.Run(() -> {
            try {
                var listOSs = new OrdemServicoRepository().listar();
                var listClientes = clienteRepository.listar();
                var listTecnicos = tecnicoRepository.listar();

                // Associar clientes e tecnicos às ordem de servicos
                for (var os : listOSs) {
                    var cliente = listClientes.stream()
                            .filter(f -> f.id.equals(os.clienteId))
                            .findFirst()
                            .orElse(null);

                    var tecnico = listTecnicos.stream()
                            .filter(f -> f.id.equals(os.tecnicoId))
                            .findFirst()
                            .orElse(null);
                    os.cliente = cliente;
                    os.tecnico = tecnico;
                }

                UI.runOnUi(() -> {
                    clientes.addAll(listClientes);
                    if(!clientes.isEmpty()){
                        clienteSelected.set(clientes.get(0));
                    }

                    ordensDeServicoList.addAll(listOSs);
                    tecnicos.addAll(listTecnicos);
                });

            } catch (SQLException e) {
                e.printStackTrace();
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar compras: " + e.getMessage()));
            }
        });
    }

    private void openTecnicoWindow() {
        router.spawnWindow("tecnicos");
    }

    @Override
    public Component render() {
        return mainView();
    }

    @Override
    public Component form() {
        final var top = new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(Components.DatePickerColumn(dataVisita, "Data de visita"))
                .r_child(Components.SelectColumn("Cliente", clientes, clienteSelected, f -> f.nome, true))
                .r_child(Components.SelectColumnWithButton("Técnico", tecnicos, tecnicoSelected, it -> it.nome, true,
                        "Criar", this::openTecnicoWindow))
                .r_child(Components.InputColumn("Equipamento", equipamento, "Marca, Modelo ou Serial"))
                .r_child(Components.InputColumnCurrency("Mão de obra (R$)", maoDeObra));

        return new Card(new Scroll(
                new Column(new ColumnProps().minWidth(800))
                        .c_child(Components.FormTitle("Cadastrar Nova Ordem de Serviço (O.S)"))
                        .c_child(new SpacerVertical(20))
                        .c_child(top)
                        .c_child(new SpacerVertical(10))
                        .c_child(
                                new Row(new RowProps().spacingOf(10))
                                        .r_child(Components.InputColumnCurrency("Peças (R$)", pecasValor))
                                        .r_child(Components.SelectColumn("Tipo de pagamento", tiposPagamento, tipoPagamentoSeleced, it -> it))
                                        .r_child(Components.TextAreaColumn("Checklist / Relatório do Serviço", checklistRelatorio, "Descreva o que foi feito..."))

                        )
                        .c_child(new SpacerVertical(10))
                        .c_child((Components.TextWithValue("Total geral(líquido): ", totalLiquido.map(Utils::toBRLCurrency))))
                        .c_child(Components.actionButtons(btnText, this::handleAddOrUpdate, this::clearForm))
        ));
    }


    @Override
    public Component table() {
        return new SimpleTable<OrdemServicoModel>()
                .fromData(ordensDeServicoList)
                .header()
                .columns()
                .column("ID", it -> it.id, (double) 90)
                .column("N. OS", it -> it.numeroOs, (double) 90)
                .column("Cliente", it -> it.cliente.nome)
                .column("Status", it -> it.status)
                .column("Equipamento", it -> it.equipamento)
                .column("Mão de obra", it -> Utils.toBRLCurrency(it.maoDeObraValor))
                .column("Total liq.", it -> Utils.toBRLCurrency(it.totalLiquido))
                .column("Data de visita", it -> DateUtils.millisToBrazilianDateTime(it.dataEscolhida))
                .column("Data de criação da OS", it -> DateUtils.millisToBrazilianDateTime(it.dataCriacao))
                .build()
                .onItemSelectChange(it -> osSelected.set(it))
                .onItemDoubleClick(it-> Components.ShowModal( ItemDetails(it), router));
    }

    Component ItemDetails(OrdemServicoModel model){
        return new Column(new ColumnProps().paddingAll(20))
                .c_child(new Text("Detalhes da ordem de serviço", new TextProps().variant(TextVariant.SUBTITLE)))
                .c_child(new SpacerVertical(20))
                .c_child(Components.TextWithDetails("ID: ", model.id))
                .c_child(Components.TextWithDetails("Número da Ordem de serviço: ", model.numeroOs))
                .c_child(Components.TextWithDetails("Checklist/Relatório: ", model.checklistRelatorio))
                .c_child(Components.TextWithDetails("Cliente: ", model.cliente.nome))
                .c_child(Components.TextWithDetails("Técnico visitante: ", model.tecnico.nome))
                .c_child(Components.TextWithDetails("Data de visita: ", DateUtils.millisToBrazilianDateTime(model.dataEscolhida)))
                .c_child(Components.TextWithDetails("Equipamento: ", model.equipamento))
                .c_child(Components.TextWithDetails("Mão de obra (R$): ", Utils.toBRLCurrency(model.maoDeObraValor)))
                .c_child(Components.TextWithDetails("Peças (R$): ", Utils.toBRLCurrency(model.pecas_valor)))
                .c_child(Components.TextWithDetails("Total líquido (R$): ", Utils.toBRLCurrency(model.totalLiquido)))
                .c_child(Components.TextWithDetails("Tipo de pagamento: ", model.tipoPagamento))
                .c_child(Components.TextWithDetails("Status: ", model.status))
                .c_child(Components.TextWithDetails("Data de criação: ", DateUtils.millisToBrazilianDateTime(model.dataCriacao)));
    }

    @Override
    public void handleClickNew() {
        modoEdicao.set(false);
        clearForm();
    }

    @Override
    public void handleClickMenuEdit() {
        handleClickMenuClone();
        modoEdicao.set(true);
    }

    @Override
    public void handleClickMenuDelete() {
        modoEdicao.set(false);

        final var data = osSelected.get();
        if (data != null) {
            Async.Run(() -> {
                try {
                    Long osId = data.id;
                    ordemServicoRepository.excluirById(osId);
                    UI.runOnUi(() -> {
                        ordensDeServicoList.removeIf(it -> it.id.equals(osId));
                        Components.ShowPopup(router, "Ordem de serviço excluída com sucesso!");
                    });
                } catch (SQLException e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao excluir ordem de serviço: " + e.getMessage()));
                }
            });
        }
    }

    @Override
    public void handleClickMenuClone() {
        modoEdicao.set(false);

        final var data = osSelected.get();
        if (data != null) {
            clienteSelected.set(data.cliente);
            tecnicoSelected.set(data.tecnico);

            equipamento.set(data.equipamento);
            tipoPagamentoSeleced.set(data.tipoPagamento);
            statusSelecionado.set(data.status);
            dataVisita.set(DateUtils.millisParaLocalDate(data.dataEscolhida));

            maoDeObra.set(Utils.deRealParaCentavos(data.maoDeObraValor));
            pecasValor.set(Utils.deRealParaCentavos(data.pecas_valor));

            checklistRelatorio.set(data.checklistRelatorio);
        }
    }

    @Override
    public void handleAddOrUpdate() {
        if(tecnicoSelected.isNull()) Components.ShowAlertError("Técnico não foi selecionado");

        final var dto = new OrdemServicoDto(
                clienteSelected.get().id,
                tecnicoSelected.get().id,
                equipamento.get(),
                Utils.deCentavosParaReal(maoDeObra.get()),
                Utils.deCentavosParaReal(pecasValor.get()),
                checklistRelatorio.get(),
                DateUtils.localDateParaMillis(dataVisita.get()),
                tipoPagamentoSeleced.get(),
                statusSelecionado.get(),
                new BigDecimal(totalLiquido.get()),
                null
        );

        Async.Run(() -> {
            if (modoEdicao.get()) {
                final var selecionado = osSelected.get();
                if(selecionado == null) return;

                var modelAtualizada = (OrdemServicoModel) new OrdemServicoModel().fromIdAndDto(selecionado.id, dto);
                try {
                    ordemServicoRepository.atualizar(modelAtualizada);

                    UI.runOnUi(()-> {
                        Components.ShowPopup(router, "Sua ordem de serviço foi atualizada com sucesso!");
                        ordensDeServicoList.updateIf(it -> it.id.equals(selecionado.id), it -> modelAtualizada);
                    });
                } catch (SQLException e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao atualizar ordem de serviço: " + e.getMessage()));
                }
            } else {
                try {
                    var model = ordemServicoRepository.salvar(dto);
                    model.tecnico = tecnicoSelected.get();
                    model.cliente = clienteSelected.get();

                    UI.runOnUi(()-> {
                        ordensDeServicoList.add(model);
                        Components.ShowPopup(router, "Sua ordem de serviço foi salva com sucesso!");
                    });
                } catch (SQLException e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao salvar ordem de serviço: " + e.getMessage()));
                }
            }
        });
    }

    @Override
    public void clearForm() {
        clienteSelected.set(null);
        tecnicoSelected.set(null);

        equipamento.set("");
        tipoPagamentoSeleced.set(tiposPagamento.getFirst());
        statusSelecionado.set(status.getFirst());
        dataVisita.set(LocalDate.now());

        maoDeObra.set("0");
        pecasValor.set("0");

        checklistRelatorio.set("");
    }

}