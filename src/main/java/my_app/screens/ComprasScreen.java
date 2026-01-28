package my_app.screens;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import megalodonte.ComputedState;
import megalodonte.ForEachState;
import megalodonte.Show;
import megalodonte.State;
import megalodonte.async.Async;
import megalodonte.base.UI;
import megalodonte.components.*;
import megalodonte.props.ColumnProps;
import megalodonte.props.RowProps;
import megalodonte.router.Router;
import megalodonte.styles.ColumnStyler;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import my_app.db.dto.CompraDto;
import my_app.db.dto.FornecedorDto;
import my_app.db.models.CategoriaModel;
import my_app.db.models.CompraModel;
import my_app.db.models.FornecedorModel;
import my_app.db.models.ProdutoModel;
import my_app.db.repositories.CategoriaRepository;
import my_app.db.repositories.ComprasRepository;
import my_app.db.repositories.FornecedorRepository;
import my_app.db.repositories.ProdutoRepository;
import my_app.screens.components.Components;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

//TODO: finalizar implementações
//TODO: lista de compras para exibir na tabela
public class ComprasScreen implements ScreenComponent, ContratoTelaCrud {
    private final Router router;
    private final Theme theme = ThemeManager.theme();
    State<LocalDate> dataCompra = State.of(LocalDate.now());
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


    ComputedState<Double> totalLiquido = ComputedState.of(() -> {
        int qtdValue = Integer.parseInt(qtd.get().trim().isEmpty() ? "0" : qtd.get());
        double precoCompraValue = Double.parseDouble(pcCompra.get()) / 100.0;

        double precoDescontoValue = Double.parseDouble(descontoEmDinheiro.get()) / 100.0;

        return (qtdValue * precoCompraValue - precoDescontoValue);
    }, descontoEmDinheiro, qtd, pcCompra);

    State<LocalDate> dataValidade = State.of(null);

    ObservableList<FornecedorModel> fornecedores = FXCollections.observableArrayList();
    State<FornecedorModel> fornecedorSelected = State.of(null);

    State<CompraModel> compraSelected = State.of(null);
    private ComprasRepository comprasRepository = new ComprasRepository();
    private final ObservableList<CompraModel> compras = FXCollections.observableArrayList();


    public ComprasScreen(Router router) {
        this.router = router;
    }

    @Override
    public void onMount() {
        fetchData();
    }

    private void fetchData() {
        Async.Run(() -> {
            try {
                var fornecedorModelList = new FornecedorRepository().listar();
                fornecedores.addAll(fornecedorModelList);//meu select fica preenchido
                var listCompras = new ComprasRepository().listar();

                UI.runOnUi(() -> {
                    if (!fornecedorModelList.isEmpty()) {
                        fornecedorModelList.stream().filter(f -> f.id == 1L)
                                .findFirst()
                                .ifPresent(fornecedorSelected::set);
                    }
                    compras.addAll(listCompras);
                });

            } catch (SQLException e) {
                e.printStackTrace();
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar compras: " + e.getMessage()));
            }

        });

        IO.println(dataCompra.get());
    }

    @Override
    public Component render() {
        return mainView();
    }

    @Override
    public Component form() {
        Runnable searchProductOnFocusChange = this::buscarProduto;

        final var top = new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(Components.DatePickerColumn(dataCompra, "Data de compra 2", "dd/mm/yyyy"))
                .r_child(Components.SelectColumn("Fornecedor", fornecedores, fornecedorSelected, f -> f.nome))
                .r_child(Components.InputColumn("N NF/Pedido compra", numeroNota, "Ex: 12345678920"))
                .r_child(Components.InputColumnComFocusHandler("Código", codigo, "xxxxxxxx", searchProductOnFocusChange));

        final var valoresRow = new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(Components.TextWithValue("Valor total(bruto): ", totalBruto))
                .r_child(Components.TextWithValue("Desconto: ", descontoEmDinheiro))
                .r_child(Components.TextWithValue("Total geral(líquido): ", totalLiquido.map(it -> Utils.toBRLCurrency(BigDecimal.valueOf(it))))
                );

        return new Card(new Scroll(
                new Column(new ColumnProps().minWidth(800))
                        .c_child(Components.FormTitle("Cadastrar Nova Compra"))
                        .c_child(new SpacerVertical(20))
                        .c_child(top)
                        .c_child(new SpacerVertical(10))
                        .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                                .r_child(Components.InputColumn("Descrição do produto", produtoEncontrado.map(p -> p != null ? p.descricao : ""), "Ex: Paraiso"))
                                //.r_child(Components.InputColumn("Pc. de compra", produtoEncontrado.map(p-> p != null? p.descricao: ""),"Ex: R$ 10,00"))
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
                        .c_child(aPrazoForm())
                        .c_child(new SpacerVertical(10))
                        .c_child(valoresRow)
                        .c_child(Components.actionButtons(btnText, this::handleAddOrUpdate, this::clearForm))
        ));
    }

    private void buscarProduto() {
        Async.Run(() -> {
            try {
                var produto = new ProdutoRepository().buscarPorCodigoBarras(codigo.get());
                UI.runOnUi(() -> {
                    if (!codigo.get().trim().isEmpty() && produto == null) {
                        IO.println("Produto não encontrado para o codigo: " + codigo.get());
                         Components.ShowAlertError("Produto não encontrado para o codigo: " + codigo.get());
                        return;
                    }
                    IO.println("Produto encontrado");
                    produtoEncontrado.set(produto);
                    var valor = produtoEncontrado.get().precoCompra;
                    BigDecimal semPonto = valor.setScale(0, RoundingMode.DOWN).multiply(BigDecimal.valueOf(100));
                    pcCompra.set(String.valueOf(semPonto));
                    // pcCompra.set(valor);
                });

            } catch (SQLException e) {
                UI.runOnUi(()->Components.ShowAlertError("Erro ao buscar produto por código: " + e.getMessage()));
            }
        });
    }

    Component aPrazoForm() {
        var dtPrimeiraParcela = State.of(LocalDate.now().plusMonths(1).minusDays(1));
        var qtdParcelas = State.of("1");

        Runnable handleGerarParcelas = () -> {
            gerarParcelas(dtPrimeiraParcela.get(), Integer.parseInt(qtdParcelas.get()), totalLiquido.get());
        };

        ForEachState<Parcela, Component> parcelaComponentForEachState = ForEachState.of(parcelas, this::parcelaItem);

        return Show.when(tipoPagamentoSelectedIsAPrazo,
                () -> new Column(new ColumnProps())
                        .c_child(
                                new Row(new RowProps().spacingOf(10).bottomVertically())
                                        .r_child(Components.DatePickerColumn(dtPrimeiraParcela, "Data primeira parcela", ""))
                                        .r_child(Components.InputColumn("Quantidade de parcelas", qtdParcelas, "Ex: 1"))
                                        .r_child(Components.ButtonCadastro("Gerar parcelas", handleGerarParcelas)))
                        .items(parcelaComponentForEachState)
        );
    }

    Component parcelaItem(Parcela parcela) {
        return new Row(new RowProps())
                .r_child(Components.TextColumn("PARCELA", String.valueOf(parcela.numero())))
                .r_child(Components.TextColumn("VENCIMENTO", parcela.dataVencimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))))
                .r_child(Components.TextColumn("VALOR", String.format("R$ %.2f", parcela.valor())));
    }

    //TODO: incluir mais campos
    @Override
    public Component table() {
        TableView<CompraModel> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Coluna ID
        TableColumn<CompraModel, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().id != null ? String.valueOf(data.getValue().id) : ""
                )
        );
        idCol.setMaxWidth(90);

        // Coluna Nome
        TableColumn<CompraModel, String> nomeCol = new TableColumn<>("N Nota");
        nomeCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().numeroNota)
        );

        TableColumn<CompraModel, String> qtdCol = new TableColumn<>("Qtd");
        nomeCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().quantidade.toString())
        );

        // Coluna Data Criação
        TableColumn<CompraModel, String> dataCol = new TableColumn<>("Data de Criação");
        dataCol.setCellValueFactory(data -> {
            if (data.getValue().dataCriacao != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        Utils.formatDateTime(data.getValue().dataCriacao)
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        table.getColumns().addAll(idCol, nomeCol,qtdCol, dataCol);
        table.setItems(compras);

        Utils.onItemTableSelectedChange(table, data-> compraSelected.set(data));

        return Component.CreateFromJavaFxNode(table);
    }


    private void gerarParcelas(LocalDate dataPrimeiraParcela, int quantidadeParcelas, double valorTotalLiquido) {
        List<Parcela> novasParcelas = new ArrayList<>();
        double valorParcela = valorTotalLiquido / quantidadeParcelas;
        IO.println("=== GERANDO PARCELAS ===");
        IO.println("Valor total para parcelar: R$ " + valorTotalLiquido);

        for (int i = 0; i < quantidadeParcelas; i++) {
            LocalDate dataVencimento = dataPrimeiraParcela.plusMonths(i);
            Parcela parcela = new Parcela(i + 1, dataVencimento, valorParcela);
            novasParcelas.add(parcela);
        }

        // Atualizar o state com as parcelas geradas
        parcelas.set(novasParcelas);

        IO.println("=== PARCELAS GERADAS ===");
        for (Parcela parcela : novasParcelas) {
            IO.println("Parcela " + parcela.numero() + ": " +
                    parcela.dataVencimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                    " - Valor: R$ " + String.format("%.2f", parcela.valor()));
        }
        IO.println("========================");
    }

    @Override
    public void handleClickNew() {
        modoEdicao.set(false);
        clearForm();
    }

    @Override
    public void handleClickMenuEdit() {
        modoEdicao.set(true);
    }

    @Override
    public void handleClickMenuDelete() {
        modoEdicao.set(false);

        final var data = compraSelected.get();
        if (data != null) {
            Async.Run(() -> {
                try {
                    Long id = data.id;
                    comprasRepository.excluirById(id);
                    UI.runOnUi(() -> {
                        compras.removeIf(it-> it.id.equals(id));
                    });

                } catch (SQLException e) {
                    UI.runOnUi(()->Components.ShowAlertError("Erro ao excluir compra: " + e.getMessage()));
                }
            });
        }
    }

    @Override
    public void handleClickMenuClone() {
        modoEdicao.set(false);

        final var data = compraSelected.get();
        if (data != null) {
            dataCompra.set(DateUtils.millisParaLocalDate(data.dataCompra));
            numeroNota.set(data.numeroNota);

            final var codProduto = data.produtoCod;
            codigo.set(codProduto);

            buscarProduto();
            qtd.set(data.quantidade.toString());
            observacao.set(data.observacao);
            tipoPagamentoSeleced.set(data.tipoPagamento);
            pcCompra.set(Utils.deRealParaCentavos(data.precoDeCompra));
            dataValidade.set(DateUtils.millisParaLocalDate(data.dataValidade));
            fornecedorSelected.set(data.fornecedor);
        }
    }

    @Override
    public void handleAddOrUpdate() {
        Async.Run(() -> {
            if (modoEdicao.get()) {
                final var selecionado = compraSelected.get();
                if(selecionado != null){
                    CompraModel modelAtualizada = new CompraModel().fromIdAndDto(
                            selecionado.id, new CompraDto(
                                    codigo.get(),
                                    Utils.deCentavosParaReal(pcCompra.get()),
                                    fornecedorSelected.get().id,
                                    new BigDecimal(qtd.get()),
                                    Utils.deCentavosParaReal(descontoEmDinheiro.get()),
                                    tipoPagamentoSeleced.get(),
                                    observacao.get(),
                                    DateUtils.localDateParaMillis(dataCompra.get()),
                                    numeroNota.get(),
                                    DateUtils.localDateParaMillis(dataValidade.get())
                            ));
                    try {
                        comprasRepository.atualizar(modelAtualizada);
                        Utils.updateItemOnObservableList(compras, selecionado, modelAtualizada);
                    } catch (SQLException e) {
                        UI.runOnUi(()->Components.ShowAlertError("Erro ao atualizar compra: " + e.getMessage()));
                    }
                }
            }else{
                try {
                    final var dtValidade = dataValidade.get() != null?
                            DateUtils.localDateParaMillis(dataValidade.get()) : null;

                    var dto = new CompraDto(codigo.get(),
                            Utils.deCentavosParaReal(pcCompra.get()),
                            fornecedorSelected.get().id,
                            new BigDecimal(qtd.get()),
                             Utils.deCentavosParaReal(descontoEmDinheiro.get()),
                            tipoPagamentoSeleced.get(), observacao.get(),
                            DateUtils.localDateParaMillis(dataCompra.get()),
                            numeroNota.get(),
                            dtValidade
                            );

                    var compraSalva = comprasRepository.salvar(dto);

                    UI.runOnUi(() -> {
                        IO.println("compra foi salva!");
                        compras.add(compraSalva);
                        Components.ShowPopup(router, "Sua compra de mercadoria foi salva com sucesso!");
                    });
                } catch (SQLException e) {
                    UI.runOnUi(()->Components.ShowAlertError("Erro ao salvar compra: " + e.getMessage()));
                }
            }
        });
    }

    @Override
    public void clearForm() {
        dataCompra.set(LocalDate.now());
        numeroNota.set("");
        modoEdicao.set(false);
        codigo.set("");
        produtoEncontrado.set(null);
        qtd.set("");
        observacao.set("");
        tipoPagamentoSeleced.set(tiposPagamento.get(1));
        pcCompra.set("0");
        dataValidade.set(null);
        fornecedorSelected.set(null);
    }

    record Parcela(int numero, LocalDate dataVencimento, double valor) {
    }
}