package my_app.screens;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import megalodonte.*;
import megalodonte.async.Async;
import megalodonte.base.UI;
import megalodonte.components.*;
import megalodonte.props.ColumnProps;
import megalodonte.props.RowProps;
import megalodonte.router.Router;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import my_app.db.dto.CompraDto;
import my_app.db.models.*;
import my_app.db.repositories.ComprasRepository;
import my_app.db.repositories.ContasPagarRepository;
import my_app.db.repositories.FornecedorRepository;
import my_app.db.repositories.ProdutoRepository;
import my_app.domain.ContratoTelaCrud;
import my_app.domain.Parcela;
import my_app.screens.components.Components;
import my_app.services.CompraMercadoriaService;
import my_app.services.ContasPagarService;

import java.math.BigDecimal;
import java.util.List;

import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.sql.SQLException;
import java.time.LocalDate;

//TODO: finalizar implementações
//TODO: lista de compras para exibir na tabela
public class ComprasScreen implements ScreenComponent, ContratoTelaCrud {
    private final Router router;
    private final Theme theme = ThemeManager.theme();
    private final ListState<CompraModel> compras = ListState.of(List.of());
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

    //TODO: no futuro deve ser tratado como String
    ComputedState<String> totalLiquido = ComputedState.of(() -> {
        int qtdValue = Integer.parseInt(qtd.get().trim().isEmpty() ? "0" : qtd.get());
        double precoCompraValue = Double.parseDouble(pcCompra.get()) / 100.0;

        double precoDescontoValue = Double.parseDouble(descontoEmDinheiro.get()) / 100.0;

        return String.valueOf (qtdValue * precoCompraValue - precoDescontoValue);
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
    State<List<String>> opcoesDeControleDeEstoque = State.of(List.of("Sim", "Não"));
    State<String> opcaoDeControleDeEstoqueSelected = State.of(opcoesDeControleDeEstoque.get().getFirst());
    private final ComprasRepository comprasRepository;
    private final ProdutoRepository produtoRepository;
    private CompraMercadoriaService compraMercadoriaService;

    public ComprasScreen(Router router) {
        this.router = router;

        // Configura listeners para atualizar estoque visual
        qtd.subscribe(novaQtd -> atualizarEstoqueVisual());
        opcaoDeControleDeEstoqueSelected.subscribe(novaOpcao -> atualizarEstoqueVisual());

        comprasRepository = new ComprasRepository();
        produtoRepository = new ProdutoRepository();
        compraMercadoriaService = new CompraMercadoriaService(comprasRepository, produtoRepository);
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
                .r_child(Components.DatePickerColumn(dataCompra, "Data de compra", "dd/mm/yyyy"))
                .r_child(Components.SelectColumn("Fornecedor", fornecedores, fornecedorSelected, f -> f.nome, true))
                .r_child(Components.InputColumn("N NF/Pedido compra", numeroNota, "Ex: 12345678920"))
                .r_child(Components.InputColumnComFocusHandler("Código", codigo, "xxxxxxxx", searchProductOnFocusChange));

        final var valoresRow = new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(Components.TextWithValue("Valor total(bruto): ", totalBruto))
                .r_child(Components.TextWithValue("Desconto: ", descontoComputed))
                .r_child(Components.TextWithValue("Total geral(líquido): ", totalLiquido.map(Utils::toBRLCurrency))
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
                        .c_child(Components.aPrazoForm(parcelas, tipoPagamentoSelectedIsAPrazo, totalLiquido))
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
                });
            } catch (SQLException e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar produto por código: " + e.getMessage()));
            }
        });
    }

    @Override
    public Component table() {
        return new SimpleTable<CompraModel>()
                .fromData(compras)
                .header()
                .columns()
                .column("ID", it -> it.id, (double) 90)
                .column("Quantidade", it -> it.quantidade)
                .column("N. Nota", it -> it.numeroNota)
                .column("Fornecedor", it -> it.fornecedor == null ? "" : it.fornecedor.nome)
                .column("Total liq. de compra", it -> Utils.toBRLCurrency(it.totalLiquido))
                .column("Data de criação", it -> DateUtils.millisToBrazilianDateTime(it.dataCriacao))
                .build()
                .onItemSelectChange(it -> compraSelected.set(it));
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

                    // Primeiro exclui todas as contas a pagar vinculadas a esta compra
                    new ContasPagarRepository().excluirPorCompra(id);

                    // Depois exclui a compra
                    comprasRepository.excluirById(id);

                    // Remove do estoque a quantidade correspondente a esta compra
                    removerEstoqueProduto(data.produtoCod, data.quantidade);

                    UI.runOnUi(() -> {
                        compras.removeIf(it -> it.id.equals(id));
                        Components.ShowPopup(router, "Compra e contas vinculadas excluídas com sucesso!");
                    });

                } catch (SQLException e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao excluir compra: " + e.getMessage()));
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
        final var dtValidade = dataValidade.get() != null ?
                DateUtils.localDateParaMillis(dataValidade.get()) : null;

        final var dto = new CompraDto(
                codigo.get(),
                Utils.deCentavosParaReal(pcCompra.get()),
                fornecedorSelected.get().id,
                new BigDecimal(qtd.get()),
                Utils.deCentavosParaReal(descontoEmDinheiro.get()),
                tipoPagamentoSeleced.get(), observacao.get(),
                DateUtils.localDateParaMillis(dataCompra.get()),
                numeroNota.get(),
                dtValidade,
                opcaoDeControleDeEstoqueSelected.get(),
                new BigDecimal(totalLiquido.get())
        );

        compraMercadoriaService.deveAtualizarEstoque = opcaoDeControleDeEstoqueSelected.get().equalsIgnoreCase("Sim");

        Async.Run(() -> {
            if (modoEdicao.get()) {
                final var selecionado = compraSelected.get();
                if(selecionado == null) return;

                CompraModel modelAtualizada = new CompraModel().fromIdAndDto(selecionado.id, dto);
                compraMercadoriaService.atualizarOrThrow(modelAtualizada, message->   UI.runOnUi(() -> Components.ShowAlertError("Erro ao atualizar compra: " + message)));

                UI.runOnUi(()-> {
                    Components.ShowPopup(router, "Sua compra de mercadoria foi atualizada com sucesso!");
                    compras.updateIf(it -> it.id.equals(selecionado.id), it -> modelAtualizada);
                });
            } else {
                final var compraSalva = compraMercadoriaService.salvarOrThrow(dto, message ->  UI.runOnUi(() -> Components.ShowAlertError("Erro ao salvar compra de mercadoria: " + message)));
                // Gerar contas a pagar se for a prazo
                if ("A PRAZO".equals(tipoPagamentoSeleced.get()) && !parcelas.get().isEmpty()) {
                    try {
                        ContasPagarService contasPagarService = new ContasPagarService();
                        List<Parcela> parcelasParaService = parcelas.get().stream()
                                .map(p -> new Parcela(
                                        p.numero(),
                                        p.dataVencimento(),
                                        p.valor()
                                ))
                                .toList();
                        contasPagarService.gerarContasDeCompra(compraSalva, parcelasParaService);
                    } catch (SQLException e) {
                        UI.runOnUi(() -> Components.ShowAlertError("Erro ao gerar contas a pagar: " + e.getMessage()));
                        return;
                    }
                }

                UI.runOnUi(() -> {
                    IO.println("compra foi salva!");
                    compras.add(compraSalva);
                    Components.ShowPopup(router, "Sua compra de mercadoria foi salva com sucesso!");
                });
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
     *
     * @param codigoBarras Código de barras do produto
     * @param quantidade   Quantidade da compra que está sendo excluída
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


}