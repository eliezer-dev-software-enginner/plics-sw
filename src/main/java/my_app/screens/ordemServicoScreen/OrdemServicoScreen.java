package my_app.screens.ordemServicoScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.theme.ThemeInterface;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.v4.ScreenContext;
import megalodonte.utils.related.TextVariant;
import my_app.db.models.OrdemServicoModel;
import my_app.domain.ContratoTelaCrudV3;
import my_app.domain.components.Components;
import my_app.domain.ViewModelScreenContract;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

public class OrdemServicoScreen implements ScreenComponent, ContratoTelaCrudV3 {
    private final OrdemServicoScreenViewModel vm;
    private final ThemeInterface theme = ThemeManager.theme();
    private final ScreenContext ctx;

    public OrdemServicoScreen(ScreenContext ctx) {
        this.ctx = ctx;
        this.vm = new OrdemServicoScreenViewModel(ctx);
    }

    @Override
    public void onMount() {
        vm.fetchListData();
    }

    @Override
    public void onDestroy() {
        ContratoTelaCrudV3.super.onDestroy();
    }

    @Override
    public Component render() {
        var mainContent = new Container(new ContainerProps().bgColor(theme.colors().background()))
                .children(
                        Components.searchInput(viewModel().searchState, ""),
                        form(),
                        new SpacerVertical(30),
                        table()
                );

        return new Container(new ContainerProps().paddingAll(10).bgColor(theme.colors().background()))
                .children(
                        commonCustomMenus(vm.focusState),
                        new SpacerVertical(10),
                        Components.ScrollPaneDefault(mainContent)
                );
    }

    @Override
    public ViewModelScreenContract viewModel() {
        return vm;
    }

    @Override
    public Component form() {
        return new Card(new Scroll(
                new Column(new ColumnProps().minWidth(800))
                        .c_child(Components.FormTitle(vm.btnText.get()))
                        .c_child(new SpacerVertical(20))
                        .c_child(
                                new Row(new RowProps().bottomVertically().spacingOf(10))
                                        .r_child(Components.DatePickerColumn(vm.dataVisita, "Data de visita"))
                                        .r_child(Components.SelectColumn("Cliente", vm.clientes,
                                                vm.clienteSelected, f -> f != null ? f.getNome() : "", true))
                                        .r_child(Components.SelectColumnWithButton("Técnico", vm.tecnicos,
                                                vm.tecnicoSelected, it -> it != null ? it.getNome() : "", true,
                                                "+", vm::openTecnicoWindow))
                                        .r_child(Components.InputColumn("Equipamento", vm.equipamento, "Marca, Modelo ou Serial"))
                                        .r_child(Components.InputColumnCurrency("Mão de obra (R$)", vm.maoDeObra))
                        )
                        .c_child(new SpacerVertical(10))
                        .c_child(
                                new Row(new RowProps().spacingOf(10))
                                        .r_child(Components.InputColumnCurrency("Peças (R$)", vm.pecasValor))
                                        .r_child(Components.SelectColumn("Tipo de pagamento",
                                                vm.tiposPagamento, vm.tipoPagamentoSelected, it -> it))
                                        .r_child(Components.TextAreaColumn("Checklist / Relatório do Serviço",
                                                vm.checklistRelatorio, "Descreva o que foi feito..."))
                        )
                        .c_child(new SpacerVertical(10))
                        .c_child(Components.TextWithValue("Total geral(líquido): ",
                                vm.totalLiquido.map(Utils::toBRLCurrency)))
                        .c_child(Components.actionButtons(vm.btnText, this::handleAddOrUpdate, this::clearForm))
        ));
    }

    @Override
    public Component table() {
        return new SimpleTable<OrdemServicoModel>()
                .fromData(vm.filteredList)
                .header()
                .columns()
                .column("ID", it -> it.getId() != null ? "#" + it.getId() : "", (double) 90)
                .column("N. OS", it -> it.getNumeroOs() != null ? String.valueOf(it.getNumeroOs()) : "", (double) 90)
                .column("Cliente", it -> it.getCliente() != null ? it.getCliente().getNome() : "")
                .column("Status", OrdemServicoModel::getStatus)
                .column("Equipamento", OrdemServicoModel::getEquipamento)
                .column("Mão de obra", it -> Utils.toBRLCurrency(it.getMaoDeObraValor()))
                .column("Total liq.", it -> Utils.toBRLCurrency(it.getTotalLiquido()))
                .column("Data de visita", it -> DateUtils.millisToBrazilianDate(it.getDataEscolhida()))
                .column("Data de criação", it -> DateUtils.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .build()
                .onItemSelectChange(vm.osSelected::set)
                .onItemDoubleClick(it -> Components.ShowModal(ItemDetails(it), ctx))
                .onChangeFocus(vm::handleFocusChange)
                .onClickOutside(() -> vm.osSelected.set(null));
    }

    private Component ItemDetails(OrdemServicoModel model) {
        return new Column(new ColumnProps().paddingAll(20))
                .c_child(new Text("Detalhes da ordem de serviço",
                        new TextProps().variant(TextVariant.SUBTITLE)))
                .c_child(new SpacerVertical(20))
                .c_child(Components.TextWithDetails("ID: ", model.getId()))
                .c_child(Components.TextWithDetails("Número da Ordem de serviço: ", model.getNumeroOs()))
                .c_child(Components.TextWithDetails("Checklist/Relatório: ", model.getChecklistRelatorio()))
                .c_child(Components.TextWithDetails("Cliente: ",
                        model.getCliente() != null ? model.getCliente().getNome() : ""))
                .c_child(Components.TextWithDetails("Técnico visitante: ",
                        model.getTecnico() != null ? model.getTecnico().getNome() : ""))
                .c_child(Components.TextWithDetails("Data de visita: ",
                        DateUtils.millisToBrazilianDate(model.getDataEscolhida())))
                .c_child(Components.TextWithDetails("Equipamento: ", model.getEquipamento()))
                .c_child(Components.TextWithDetails("Mão de obra (R$): ",
                        Utils.toBRLCurrency(model.getMaoDeObraValor())))
                .c_child(Components.TextWithDetails("Peças (R$): ",
                        Utils.toBRLCurrency(model.getPecasValor())))
                .c_child(Components.TextWithDetails("Total líquido (R$): ",
                        Utils.toBRLCurrency(model.getTotalLiquido())))
                .c_child(Components.TextWithDetails("Tipo de pagamento: ", model.getTipoPagamento()))
                .c_child(Components.TextWithDetails("Status: ", model.getStatus()))
                .c_child(Components.TextWithDetails("Data de criação: ",
                        DateUtils.localDateTimeToBrazilianDateTime(model.getDataCriacao())));
    }
}
