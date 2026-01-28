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

    ComputedState<String> descontoComputed = ComputedState.of(() -> Utils.toBRLCurrency(Utils.deCentavosParaReal(descontoEmDinheiro.get())),
            descontoEmDinheiro);


    State<LocalDate> dataValidade = State.of(null);

    // Estados para controle visual do estoque
    State<String> estoqueAnterior = State.of("0");
    State<String> estoqueAtual = State.of("0");

    ObservableList<FornecedorModel> fornecedores = FXCollections.observableArrayList();
    State<FornecedorModel> fornecedorSelected = State.of(null);

    State<CompraModel> compraSelected = State.of(null);
    private ComprasRepository comprasRepository = new ComprasRepository();
    private ProdutoRepository produtoRepository = new ProdutoRepository();
    
    // Setter para injeção de dependência em testes
    void setProdutoRepository(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }
    private final ObservableList<CompraModel> compras = FXCollections.observableArrayList();

    State<List<String>> opcoesDeControleDeEstoque = State.of(List.of("Sim", "Não"));
    State<String> opcaoDeControleDeEstoqueSelected = State.of(opcoesDeControleDeEstoque.get().getFirst());


    public ComprasScreen(Router router) {
        this.router = router;
        
        // Configura listeners para atualizar estoque visual
        qtd.subscribe(novaQtd -> atualizarEstoqueVisual());
        opcaoDeControleDeEstoqueSelected.subscribe(novaOpcao -> atualizarEstoqueVisual());
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
                    
                    // Associar fornecedores às compras
                    for (CompraModel compra : listCompras) {
                        FornecedorModel fornecedor = fornecedorModelList.stream()
                                .filter(f -> f.id.equals(compra.fornecedorId))
                                .findFirst()
                                .orElse(null);
                        compra.fornecedor = fornecedor;
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
                .r_child(Components.SelectColumn("Fornecedor", fornecedores, fornecedorSelected, f -> f.nome, true))
                .r_child(Components.InputColumn("N NF/Pedido compra", numeroNota, "Ex: 12345678920"))
                .r_child(Components.InputColumnComFocusHandler("Código", codigo, "xxxxxxxx", searchProductOnFocusChange));

        final var valoresRow = new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(Components.TextWithValue("Valor total(bruto): ", totalBruto))
                .r_child(Components.TextWithValue("Desconto: ", descontoComputed))
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
                                .r_child(Components.InputColumnCurrency("Pc. de compra", pcCompra))
                                .r_child(Components.InputColumn("Quantidade", qtd, "Ex: 2"))
                                .r_child(Components.InputColumnCurrency("Desconto em R$", descontoEmDinheiro))
                        )
.c_child(new SpacerVertical(10))
                        .c_child(
                                new Row(new RowProps().spacingOf(10)).r_child(Components.SelectColumn("Tipo de pagamento", tiposPagamento, tipoPagamentoSeleced, it -> it))
                                        .r_child(Components.SelectColumn("Refletir no estoque?", opcoesDeControleDeEstoque, opcaoDeControleDeEstoqueSelected, it -> it))
                                        .r_child(Components.TextAreaColumn("Observação", observacao, ""))
                        )
                        .c_child(new SpacerVertical(10))
                        .c_child(
                                new Row(new RowProps().spacingOf(15))
                                        .r_child(Components.TextWithValue("Estoque anterior:", estoqueAnterior))
                                        .r_child(Components.TextWithValue("Estoque após compra:", estoqueAtual))
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
                    pcCompra.set(Utils.deRealParaCentavos(produto.precoCompra));
                    
                    // Atualiza os campos de estoque
                    BigDecimal estoqueAtualValue = produto.estoque != null ? produto.estoque : BigDecimal.ZERO;
                    estoqueAnterior.set(estoqueAtualValue.toString());
                    
                    // Calcula o estoque após a compra (se controle estiver ativo)
                    if ("Sim".equals(opcaoDeControleDeEstoqueSelected.get())) {
                        try {
                            int qtdValue = Integer.parseInt(qtd.get().trim().isEmpty() ? "0" : qtd.get());
                            BigDecimal estoqueAposCompra = estoqueAtualValue.add(BigDecimal.valueOf(qtdValue));
                            estoqueAtual.set(estoqueAposCompra.toString());
                        } catch (NumberFormatException e) {
                            estoqueAtual.set(estoqueAtualValue.toString());
                        }
                    } else {
                        estoqueAtual.set(estoqueAtualValue.toString());
                    }
//                    var valor = produtoEncontrado.get().precoCompra;
//                    BigDecimal semPonto = valor.setScale(0, RoundingMode.DOWN).multiply(BigDecimal.valueOf(100));
//                    pcCompra.set(String.valueOf(semPonto));
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
        TableColumn<CompraModel, String> numNotaCol = new TableColumn<>("N Nota");
        numNotaCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().numeroNota)
        );

        TableColumn<CompraModel, String> qtdCol = new TableColumn<>("Qtd");
        qtdCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().quantidade.toPlainString())
        );

        TableColumn<CompraModel, String> fornecedorCol = new TableColumn<>("Fornecedor");
        fornecedorCol.setCellValueFactory(data -> {
            var fornecedor = data.getValue().fornecedor;
            if (fornecedor != null) {
                return new javafx.beans.property.SimpleStringProperty(fornecedor.nome);
            } else {
                return new javafx.beans.property.SimpleStringProperty("");
            }
        });

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

        table.getColumns().addAll(idCol, numNotaCol,qtdCol, fornecedorCol, dataCol);
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

        final var data = compraSelected.get();
        if (data != null) {
            dataCompra.set(DateUtils.millisParaLocalDate(data.dataCompra));
            numeroNota.set(data.numeroNota);

            final var codProduto = data.produtoCod;
            codigo.set(codProduto);

            // Ao clonar, não precisamos buscar o produto async, já temos todos os dados
            produtoEncontrado.set(null); // Limpa estado anterior

            // Buscar fornecedor pelo ID para clonagem de forma assíncrona
            // Garantir que a lista de fornecedores está carregada antes de selecionar
            Async.Run(() -> {
                try {
                    // Se a lista estiver vazia, carregá-la primeiro
                    if (fornecedores.isEmpty()) {
                        var fornecedorModelList = new FornecedorRepository().listar();
                        UI.runOnUi(() -> fornecedores.addAll(fornecedorModelList));
                    }

                    // Buscar o fornecedor específico
                    var fornecedor = new FornecedorRepository().buscarById(data.fornecedorId);
                    UI.runOnUi(() -> {
                        fornecedorSelected.set(fornecedor);
                        // Atualizar também o fornecedor no modelo da lista para refresh da tabela
                        data.fornecedor = fornecedor;
                    });
                } catch (SQLException e) {
                    IO.println("Erro ao buscar fornecedor: " + e.getMessage());
                }
            });

            qtd.set(data.quantidade.stripTrailingZeros().toPlainString());
            observacao.set(data.observacao);
            tipoPagamentoSeleced.set(data.tipoPagamento);
            pcCompra.set(Utils.deRealParaCentavos(data.precoDeCompra));
            if (data.dataValidade != null) {
                dataValidade.set(DateUtils.millisParaLocalDate(data.dataValidade));
            } else {
                dataValidade.set(null);
            }
        }
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
                    
                    // Remove do estoque a quantidade correspondente a esta compra
                    removerEstoqueProduto(data.produtoCod, data.quantidade);
                    
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

            // Ao clonar, não precisamos buscar o produto async, já temos todos os dados
            produtoEncontrado.set(null); // Limpa estado anterior

            // Buscar fornecedor pelo ID para clonagem de forma assíncrona
            // Garantir que a lista de fornecedores está carregada antes de selecionar
            Async.Run(() -> {
                try {
                    // Se a lista estiver vazia, carregá-la primeiro
                    if (fornecedores.isEmpty()) {
                        var fornecedorModelList = new FornecedorRepository().listar();
                        UI.runOnUi(() -> fornecedores.addAll(fornecedorModelList));
                    }
                    
                    // Buscar o fornecedor específico
                    var fornecedor = new FornecedorRepository().buscarById(data.fornecedorId);
                    UI.runOnUi(() -> {
                        fornecedorSelected.set(fornecedor);
                        // Atualizar também o fornecedor no modelo da lista para refresh da tabela
                        data.fornecedor = fornecedor;
                    });
                } catch (SQLException e) {
                    IO.println("Erro ao buscar fornecedor: " + e.getMessage());
                }
            });

            qtd.set(data.quantidade.stripTrailingZeros().toPlainString());
            observacao.set(data.observacao);
            tipoPagamentoSeleced.set(data.tipoPagamento);
            pcCompra.set(Utils.deRealParaCentavos(data.precoDeCompra));
            if (data.dataValidade != null) {
                dataValidade.set(DateUtils.millisParaLocalDate(data.dataValidade));
            } else {
                dataValidade.set(null);
            }
        }
    }

    @Override
    public void handleAddOrUpdate() {
        Async.Run(() -> {
            if (modoEdicao.get()) {
                final var selecionado = compraSelected.get();
                if(selecionado != null){
                    // Calcular estoque anterior e posterior para edição
                    BigDecimal estoqueAntesEdicao = BigDecimal.ZERO;
                    BigDecimal estoqueAposEdicao = BigDecimal.ZERO;
                    String refletirEstoqueEdicao = opcaoDeControleDeEstoqueSelected.get();
                    
                    try {
                        ProdutoModel produto = produtoRepository.buscarPorCodigoBarras(codigo.get());
                        if (produto != null) {
                            estoqueAntesEdicao = produto.estoque != null ? produto.estoque : BigDecimal.ZERO;
                            if ("Sim".equals(refletirEstoqueEdicao)) {
                                BigDecimal novaQtd = new BigDecimal(qtd.get());
                                BigDecimal qtdAnterior = selecionado.quantidade;
                                BigDecimal diferenca = novaQtd.subtract(qtdAnterior);
                                estoqueAposEdicao = estoqueAntesEdicao.add(diferenca);
                            } else {
                                estoqueAposEdicao = estoqueAntesEdicao;
                            }
                        }
                    } catch (SQLException e) {
                        IO.println("Erro ao buscar produto para controle de estoque na edição: " + e.getMessage());
                    }

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
                                    dataValidade.get()!= null? DateUtils.localDateParaMillis(dataValidade.get()): null,
                                    estoqueAntesEdicao,
                                    estoqueAposEdicao,
                                    refletirEstoqueEdicao
                            ));
                    try {
                        comprasRepository.atualizar(modelAtualizada);
                        Utils.updateItemOnObservableList(compras, selecionado, modelAtualizada);
                        
                        // Atualiza o estoque com base na diferença entre as quantidades
                        BigDecimal novaQuantidade = modelAtualizada.quantidade;
                        BigDecimal quantidadeAnterior = selecionado.quantidade;
                        atualizarEstoqueProduto(modelAtualizada.produtoCod, novaQuantidade, true, quantidadeAnterior);
                        
                        Components.ShowPopup(router, "Sua compra de mercadoria foi atualizada com sucesso!");
                    } catch (SQLException e) {
                        UI.runOnUi(()->Components.ShowAlertError("Erro ao atualizar compra: " + e.getMessage()));
                    }
                }
            }else{
                try {
                    final var dtValidade = dataValidade.get() != null?
                            DateUtils.localDateParaMillis(dataValidade.get()) : null;

                    // Calcular valores de estoque para salvar
                    BigDecimal estoqueAntes = BigDecimal.ZERO;
                    BigDecimal estoqueApos = BigDecimal.ZERO;
                    String refletirEstoqueSalv = opcaoDeControleDeEstoqueSelected.get();
                    
                    try {
                        ProdutoModel produto = produtoRepository.buscarPorCodigoBarras(codigo.get());
                        if (produto != null) {
                            estoqueAntes = produto.estoque != null ? produto.estoque : BigDecimal.ZERO;
                            if ("Sim".equals(refletirEstoqueSalv)) {
                                estoqueApos = estoqueAntes.add(new BigDecimal(qtd.get()));
                            } else {
                                estoqueApos = estoqueAntes;
                            }
                        }
                    } catch (SQLException e) {
                        IO.println("Erro ao buscar produto para controle de estoque: " + e.getMessage());
                    }

var dto = new CompraDto(codigo.get(),
                            Utils.deCentavosParaReal(pcCompra.get()),
                            fornecedorSelected.get().id,
                            new BigDecimal(qtd.get()),
                            Utils.deCentavosParaReal(descontoEmDinheiro.get()),
                            tipoPagamentoSeleced.get(), observacao.get(),
                            DateUtils.localDateParaMillis(dataCompra.get()),
                            numeroNota.get(),
                            dtValidade,
                            BigDecimal.ZERO, // quantidade_anterior (será calculado depois)
                            BigDecimal.ZERO, // estoque_apos_compra (será calculado depois)
                            opcaoDeControleDeEstoqueSelected.get()
                    );

                    var compraSalva = comprasRepository.salvar(dto);

                    // Atualiza o estoque do produto
                    atualizarEstoqueProduto(dto.produtoCod(), dto.quantidade(), false, null);

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
        opcaoDeControleDeEstoqueSelected.set("Não"); // Reset para padrão seguro
        estoqueAnterior.set("0");
        estoqueAtual.set("0");
    }

    /**
     * Atualiza o estoque do produto baseado na operação de compra
     * @param codigoBarras Código de barras do produto
     * @param quantidade Quantidade da compra
     * @param isEdicao Se true, precisa calcular a diferença (nova quantidade - quantidade anterior)
     * @param quantidadeAnterior Quantidade anterior da compra (usada apenas em edição)
     */
    void atualizarEstoqueProduto(String codigoBarras, BigDecimal quantidade, boolean isEdicao, BigDecimal quantidadeAnterior) {
        if (!"Sim".equals(opcaoDeControleDeEstoqueSelected.get())) {
            IO.println("Controle de estoque desativado para esta operação");
            return;
        }

        Async.Run(() -> {
            try {
                BigDecimal quantidadeParaAtualizar;
                
                if (isEdicao) {
                    // Em edição, calcula a diferença: novo valor - valor anterior
                    quantidadeParaAtualizar = quantidade.subtract(quantidadeAnterior);
                    IO.println("Atualizando estoque (edição): " + codigoBarras + 
                              " | Qtd anterior: " + quantidadeAnterior + 
                              " | Nova qtd: " + quantidade + 
                              " | Diferença: " + quantidadeParaAtualizar);
                } else {
                    // Em adição, usa a quantidade diretamente
                    quantidadeParaAtualizar = quantidade;
                    IO.println("Adicionando ao estoque: " + codigoBarras + " | Quantidade: " + quantidade);
                }

                if (quantidadeParaAtualizar.compareTo(BigDecimal.ZERO) != 0) {
                    produtoRepository.atualizarEstoque(codigoBarras, quantidadeParaAtualizar);
                    IO.println("Estoque atualizado com sucesso para o produto: " + codigoBarras);
                } else {
                    IO.println("Sem alteração de estoque necessária para o produto: " + codigoBarras);
                }
            } catch (SQLException e) {
                IO.println("Erro ao atualizar estoque do produto " + codigoBarras + ": " + e.getMessage());
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao atualizar estoque: " + e.getMessage()));
            }
        });
    }

    /**
     * Atualiza os campos visuais de estoque (anterior e atual)
     */
    void atualizarEstoqueVisual() {
        if (produtoEncontrado.get() == null) {
            estoqueAnterior.set("0");
            estoqueAtual.set("0");
            return;
        }

        BigDecimal estoqueBase = produtoEncontrado.get().estoque != null ? 
                                produtoEncontrado.get().estoque : BigDecimal.ZERO;
        estoqueAnterior.set(estoqueBase.toString());

        if ("Sim".equals(opcaoDeControleDeEstoqueSelected.get())) {
            try {
                int qtdValue = Integer.parseInt(qtd.get().trim().isEmpty() ? "0" : qtd.get());
                BigDecimal estoqueAposCompra = estoqueBase.add(BigDecimal.valueOf(qtdValue));
                estoqueAtual.set(estoqueAposCompra.toString());
            } catch (NumberFormatException e) {
                estoqueAtual.set(estoqueBase.toString());
            }
        } else {
            estoqueAtual.set(estoqueBase.toString());
        }
    }

    /**
     * Remove do estoque a quantidade correspondente a uma compra excluída
     * @param codigoBarras Código de barras do produto
     * @param quantidade Quantidade da compra que está sendo excluída
     */
    void removerEstoqueProduto(String codigoBarras, BigDecimal quantidade) {
        if (!"Sim".equals(opcaoDeControleDeEstoqueSelected.get())) {
            IO.println("Controle de estoque desativado para esta operação");
            return;
        }

        Async.Run(() -> {
            try {
                // Remove a quantidade do estoque (valor negativo)
                BigDecimal quantidadeParaRemover = quantidade.negate();
                produtoRepository.atualizarEstoque(codigoBarras, quantidadeParaRemover);
                IO.println("Estoque removido com sucesso para o produto: " + codigoBarras + " | Quantidade: " + quantidade);
            } catch (SQLException e) {
                IO.println("Erro ao remover estoque do produto " + codigoBarras + ": " + e.getMessage());
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao remover estoque: " + e.getMessage()));
            }
        });
    }

    record Parcela(int numero, LocalDate dataVencimento, double valor) {
    }
}