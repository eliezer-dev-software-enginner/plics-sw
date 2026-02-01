package my_app.screens;

import megalodonte.*;
import megalodonte.async.Async;
import megalodonte.base.UI;
import megalodonte.components.*;
import megalodonte.props.ColumnProps;
import megalodonte.props.RowProps;
import megalodonte.router.Router;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import my_app.db.dto.VendaDto;
import my_app.db.models.*;
import my_app.db.repositories.*;
import my_app.domain.Parcela;
import my_app.screens.components.Components;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static my_app.domain.Parcela.gerarParcelas;

public class VendaMercadoriaScreen implements ScreenComponent, ContratoTelaCrud {
    public final ListState<VendaModel> vendas = ListState.of(List.of());
    private final Router router;
    private final Theme theme = ThemeManager.theme();

    State<LocalDate> dataVenda = State.of(LocalDate.now());
    State<String> numeroNota = State.of("");
    State<Boolean> modoEdicao = State.of(false);
    ComputedState<String> btnText = ComputedState.of(() -> modoEdicao.get() ? "Atualizar" : "+ Adicionar", modoEdicao);
    State<String> codigo = State.of("");
    State<ProdutoModel> produtoEncontrado = State.of(null);
    State<String> qtd = State.of("0");
    State<String> observacao = State.of("");
    List<String> tiposPagamento = List.of("A VISTA", "CRÉDITO", "DÉBITO", "PIX", "A PRAZO");
    State<String> tipoPagamentoSeleced = State.of(tiposPagamento.get(1));

    ComputedState<Boolean> tipoPagamentoSelectedIsAPrazo = ComputedState.of(
            () -> tipoPagamentoSeleced.get().equals("A PRAZO"),
            tipoPagamentoSeleced);
    State<List<Parcela>> parcelas = State.of(List.of());
    State<String> descontoEmDinheiro = State.of("0");
    // Preço de compra (armazena em centavos, ex: 123 = R$ 1,23)
    State<String> pcCompra = State.of("0");

    ComputedState<String> totalBruto = ComputedState.of(() -> {
        int qtdValue = Integer.parseInt(qtd.get().trim().isEmpty() ? "0" : qtd.get());
        double precoCompraValue = Double.parseDouble(pcCompra.get()) / 100.0;
        return Utils.toBRLCurrency(BigDecimal.valueOf(qtdValue * precoCompraValue));
    }, descontoEmDinheiro, qtd, pcCompra);

    ComputedState<String> totalLiquido = ComputedState.of(() -> {
        int qtdValue = Integer.parseInt(qtd.get().trim().isEmpty() ? "0" : qtd.get());
        double precoCompraValue = Double.parseDouble(pcCompra.get()) / 100.0;

        double precoDescontoValue = Double.parseDouble(descontoEmDinheiro.get()) / 100.0;
        return String.valueOf(qtdValue * precoCompraValue - precoDescontoValue);
    }, descontoEmDinheiro, qtd, pcCompra);

    ComputedState<String> descontoComputed = ComputedState.of(() -> Utils.toBRLCurrency(Utils.deCentavosParaReal(descontoEmDinheiro.get())),
            descontoEmDinheiro);
    State<LocalDate> dataValidade = State.of(null);

    ListState<ClienteModel> clientes = ListState.of(List.of());
    State<ClienteModel> clienteSelected = State.of(null);
    State<VendaModel> vendaSelected = State.of(null);
    private VendaRepository vendaRepository = new VendaRepository();
    private ProdutoRepository produtoRepository = new ProdutoRepository();

    public VendaMercadoriaScreen(Router router) {
        this.router = router;
        // Configura listeners para atualizar estoque visual
        qtd.subscribe(novaQtd -> atualizarEstoqueVisual());
    }

    @Override
    public void onMount() {
        fetchData();
    }

    private void fetchData() {
        Async.Run(() -> {
            try {
                var clienteList = new ClienteRepository().listar();
                var vendaList = vendaRepository.listar();

                UI.runOnUi(() -> {
                    clientes.addAll(clienteList);//meu select fica preenchido
                    if (!clienteList.isEmpty()) {
                        clienteList.stream().filter(f -> f.id == 1L)
                                .findFirst()
                                .ifPresent(clienteSelected::set);
                    }

                    for (var venda : vendaList) {
                        var cliente = clienteList.stream()
                                .filter(it -> it.id.equals(venda.clienteId))
                                .findFirst()
                                .orElse(null);
                        venda.cliente = cliente;
                    }
                    vendas.addAll(vendaList);
                });

            } catch (SQLException e) {
                e.printStackTrace();
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar vendas: " + e.getMessage()));
            }
        });
    }

    @Override
    public Component render() {
        return mainView();
    }

    @Override
    public Component form() {
        Runnable searchProductOnFocusChange = this::buscarProduto;

        final var top = new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(Components.DatePickerColumn(dataVenda, "Data de venda"))
                .r_child(Components.SelectColumn("Cliente", clientes, clienteSelected, f -> f.nome, true))
                .r_child(Components.InputColumn("N NF/Pedido compra", numeroNota, "Ex: 12345678920"))
                .r_child(Components.InputColumnComFocusHandler("Código", codigo, "xxxxxxxx", searchProductOnFocusChange));

        final var valoresRow = new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(Components.TextWithValue("Valor total(bruto): ", totalBruto))
                .r_child(Components.TextWithValue("Desconto: ", descontoComputed))
                .r_child(Components.TextWithValue("Total geral(líquido): ", totalLiquido.map(Utils::toBRLCurrency))
                );

        return new Card(new Scroll(
                new Column(new ColumnProps().minWidth(800))
                        .c_child(Components.FormTitle("Cadastrar Nova Venda"))
                        .c_child(new SpacerVertical(20))
                        .c_child(top)
                        .c_child(new SpacerVertical(10))
                        .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                                .r_child(Components.InputColumn("Descrição do produto", produtoEncontrado.map(p -> p != null ? p.descricao : ""),
                                        "Ex: Paraiso", true))
                                .r_child(Components.InputColumnCurrency("Pc. de compra", pcCompra))
                                .r_child(Components.InputColumn("Quantidade", qtd, "Ex: 2"))
                                .r_child(Components.InputColumnCurrency("Desconto em R$", descontoEmDinheiro))
                        )
                        .c_child(new SpacerVertical(10))
                        .c_child(
                                new Row(new RowProps().spacingOf(10)).r_child(Components.SelectColumn("Tipo de pagamento", tiposPagamento, tipoPagamentoSeleced, it -> it))
                                        .r_child(Components.TextAreaColumn("Observação", observacao, ""))
                        )
                        .c_child(new SpacerVertical(10))
                        .c_child(Components.aPrazoForm(parcelas, tipoPagamentoSelectedIsAPrazo, totalLiquido))
                        .c_child(new SpacerVertical(10))
                        .c_child(valoresRow)
                        .c_child(Components.actionButtons(btnText, this::handleAddOrUpdate, this::clearForm))
        ));
    }

    private void buscarProduto() {
        Async.Run(() -> {
            try {
                var produto = produtoRepository.buscarPorCodigoBarras(codigo.get());
                UI.runOnUi(() -> {
                    if (!codigo.get().trim().isEmpty() && produto == null) {
                        IO.println("Produto não encontrado para o codigo: " + codigo.get());
                        Components.ShowAlertError("Produto não encontrado para o codigo: " + codigo.get());
                        return;
                    }
                    IO.println("Produto encontrado");
                    produtoEncontrado.set(produto);
                    pcCompra.set(Utils.deRealParaCentavos(produto.precoCompra));
                });

            } catch (SQLException e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar produto por código: " + e.getMessage()));
            }
        });
    }

    @Override
    public Component table() {
        var simpleTable = new SimpleTable<VendaModel>();
        simpleTable.fromData(vendas)
                .header()
                .columns()
                .column("ID", it-> it.id)
                .column("Quantidade", it-> it.quantidade)
                .column("Preço", it-> it.precoUnitario)
                .column("Data de criação", it-> DateUtils.millisToBrazilianDateTime(it.dataCriacao))
                .build()
                .onItemSelectChange(it->   vendaSelected.set(it));

        return simpleTable;
    }

    @Override
    public void handleClickNew() {
        modoEdicao.set(false);
        clearForm();
    }

    @Override
    public void handleClickMenuEdit() {
        modoEdicao.set(true);

        final var data = vendaSelected.get();
        if (data != null) {
            //TODO: implementar
        }
    }

    @Override
    public void handleClickMenuDelete() {
        modoEdicao.set(false);

        final var data = vendaSelected.get();
        if (data != null) {
//            Async.Run(() -> {
//                try {
//                    Long id = data.id;
//
//                    // Primeiro exclui todas as contas a pagar vinculadas a esta compra
//                    new ContasPagarRepository().excluirPorCompra(id);
//
//                    // Depois exclui a compra
//                    VendaRepository.excluirById(id);
//
//                    // Remove do estoque a quantidade correspondente a esta compra
//                    removerEstoqueProduto(data.produtoCod, data.quantidade);
//
//                    UI.runOnUi(() -> {
//                        compras.removeIf(it -> it.id.equals(id));
//                        Components.ShowPopup(router, "Compra e contas vinculadas excluídas com sucesso!");
//                    });
//
//                } catch (SQLException e) {
//                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao excluir compra: " + e.getMessage()));
//                }
//            });
        }
    }

    @Override
    public void handleClickMenuClone() {
        modoEdicao.set(false);

        final var data = vendaSelected.get();
        if (data != null) {
//            dataVenda.set(DateUtils.millisParaLocalDate(data.dataCriacao));
//            numeroNota.set(data.numeroNota);
//
//            final var codProduto = data.produtoCod;
//            codigo.set(codProduto);
//
//            // Ao clonar, não precisamos buscar o produto async, já temos todos os dados
//            produtoEncontrado.set(null); // Limpa estado anterior
        }
    }

    @Override
    public void handleAddOrUpdate() {
        if(produtoEncontrado.get() == null) {
            Components.ShowAlertError("Produto não encontrado!");
            return;
        }

        final var selecionado = vendaSelected.get();
        if (selecionado != null) {
            return;
        }

        var dto = new VendaDto(
                produtoEncontrado.get().id,
                clienteSelected.get().id,
                new BigDecimal(qtd.get()),
                Utils.deCentavosParaReal(pcCompra.get()),
                Utils.deCentavosParaReal(descontoEmDinheiro.get()),
                Utils.deCentavosParaReal(String.valueOf(totalLiquido.get())),
                tipoPagamentoSeleced.get(),
                observacao.get()
        );
        
        Async.Run(() -> {
            if (modoEdicao.get()) {
                    var modelAtualizada = new VendaModel().fromIdAndDto(selecionado.id, dto);
                    try {
                        vendaRepository.atualizar(modelAtualizada);
                        vendas.updateIf(it-> it.id.equals(selecionado.id), it-> modelAtualizada);
                        Components.ShowPopup(router, "Sua venda de mercadoria foi atualizada com sucesso!");
                    } catch (SQLException e) {
                        UI.runOnUi(() -> Components.ShowAlertError("Erro ao atualizar venda: " + e.getMessage()));
                    }
            } else {
                try {
                    var compraSalva = vendaRepository.salvar(dto);
                    UI.runOnUi(() -> {
                        vendas.add(compraSalva);
                        Components.ShowPopup(router, "Sua venda de mercadoria foi salva com sucesso!");
                    });
                } catch (SQLException e) {
                    IO.println("Erro ao salvar venda: " + e.getMessage());
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao salvar venda: " + e.getMessage()));
                }
            }
        });
    }

    @Override
    public void clearForm() {
        dataVenda.set(LocalDate.now());
        numeroNota.set("");
        modoEdicao.set(false);
        codigo.set("");
        produtoEncontrado.set(null);
        qtd.set("");
        observacao.set("");
        tipoPagamentoSeleced.set(tiposPagamento.get(1));
        pcCompra.set("0");
        dataValidade.set(null);
        clienteSelected.set(null);
    }

    /**
     * Atualiza os campos visuais de estoque (anterior e atual)
     */
    void atualizarEstoqueVisual() {

    }
}