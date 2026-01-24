package my_app.screens;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ScrollPane;
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
import my_app.db.models.CompraModel;
import my_app.db.models.FornecedorModel;
import my_app.db.models.ProdutoModel;
import my_app.db.repositories.ComprasRepository;
import my_app.db.repositories.FornecedorRepository;
import my_app.db.repositories.ProdutoRepository;
import my_app.screens.components.Components;
import my_app.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

//TODO: finalizar implementações
public class ComprasScreen implements ScreenComponent, ContratoTelaCrud {
    private final Router router;
    private ComprasRepository comprasRepository = new ComprasRepository();

    State<LocalDate> dataCompra = State.of(LocalDate.now());
    State<String> numeroNota = State.of("");

    State<Boolean> modoEdicao = State.of(false);

    ComputedState<String> btnText = ComputedState.of(()-> modoEdicao.get()? "Atualizar" : "+ Adicionar", modoEdicao);

    State<String> codigo = State.of("");
    State<ProdutoModel> produtoEncontrado = State.of(null);
    State<String> qtd = State.of("2");
    State<String> observacao = State.of("");

    List<String> tiposPagamento = List.of("A VISTA","CRÉDITO", "DÉBITO", "PIX", "A PRAZO");
    State<String> tipoPagamentoSeleced = State.of(tiposPagamento.get(1));

    ComputedState<Boolean> tipoPagamentoSelectedIsAPrazo = ComputedState.of(
            ()-> tipoPagamentoSeleced.get().equals("A PRAZO"),
            tipoPagamentoSeleced);

    State<List<Parcela>> parcelas = State.of(List.of());

    State<String> descontoEmDinheiro = State.of("0");

    // Preço de compra (armazena em centavos, ex: 123 = R$ 1,23)
    State<String> pcCompra = State.of("0");

    ComputedState<String> totalBruto = ComputedState.of(()-> {
        int qtdValue = Integer.parseInt(qtd.get().trim().isEmpty()? "0": qtd.get());
        double precoCompraValue = Double.parseDouble(pcCompra.get()) / 100.0;

        return Utils.toBRLCurrency(BigDecimal.valueOf(qtdValue * precoCompraValue));
    }, descontoEmDinheiro, qtd, pcCompra);


    ComputedState<Double> totalLiquido = ComputedState.of(()-> {
        int qtdValue = Integer.parseInt(qtd.get().trim().isEmpty()? "0": qtd.get());
        double precoCompraValue = Double.parseDouble(pcCompra.get()) / 100.0;

        double precoDescontoValue = Double.parseDouble(descontoEmDinheiro.get()) / 100.0;

        return (qtdValue * precoCompraValue - precoDescontoValue);
    }, descontoEmDinheiro, qtd, pcCompra);

    State<String> dataValidade = State.of("0");

    ObservableList<FornecedorModel> fornecedores = FXCollections.observableArrayList();
    State<FornecedorModel> fornecedorSelected = State.of(null);

    State<CompraModel> compraSelected = State.of(null);


    public ComprasScreen(Router router) {
        this.router = router;
    }

    @Override
    public void onMount() {
        fetchData();
    }

    private void fetchData() {
        Async.Run(()->{
            try{
               var list = new FornecedorRepository().listar();
              fornecedores.addAll(list);//meu select fica preenchido

               UI.runOnUi(()->{
                   if(!list.isEmpty()){
                       list.stream().filter(f-> f.id == 1L)
                               .findFirst()
                               .ifPresent(fornecedorSelected::set);
                   }
               });

            }catch (SQLException e){
              UI.runOnUi(()->  Components.ShowAlertError("Erro ao buscar compras"));
            }

        });

        IO.println(dataCompra.get());
    }

    private final Theme theme = ThemeManager.theme();

    public Component render() {
        return mainView();
    }

    @Override
    public Component form(){
        Runnable searchProductOnFocusChange = ()->{
            Async.Run(()->{
                try {
                    var produto =  new ProdutoRepository().buscarPorCodigoBarras(codigo.get());
                    UI.runOnUi(()->{
                        if(produto == null){
                            IO.println("Produto não encontrado para o codigo: " + codigo.get());
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
                    throw new RuntimeException(e);
                }
            });

        };

        final var top = new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(Components.DatePickerColumn(dataCompra,"Data de compra 2", "dd/mm/yyyy"))
                .r_child(Components.SelectColumn("Fornecedor", fornecedores, fornecedorSelected, f-> f.nome))
                .r_child(Components.InputColumn("N NF/Pedido compra", numeroNota,"Ex: 12345678920"))
                .r_child(Components.InputColumnComFocusHandler("Código", codigo,"xxxxxxxx", searchProductOnFocusChange));

        final var valoresRow = new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(Components.TextWithValue("Valor total(bruto): ", totalBruto))
                .r_child(Components.TextWithValue("Desconto: ", descontoEmDinheiro))
                .r_child(Components.TextWithValue("Total geral(líquido): ", totalLiquido.map(it-> Utils.toBRLCurrency(BigDecimal.valueOf(it))))
                );

        var scroll = new ScrollPane();
        scroll.setPrefHeight(600);
        //scroll.setStyle("-fx-background-color:red;");

        scroll.setContent(new Column(new ColumnProps().minWidth(800))
                .c_child(Components.FormTitle("Cadastrar Nova Compra"))
                .c_child(new SpacerVertical(20))
                .c_child(top)
                .c_child(new SpacerVertical(10))
                .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                        .r_child(Components.InputColumn("Descrição do produto",produtoEncontrado.map(p-> p != null? p.descricao: ""),"Ex: Paraiso"))
                        //.r_child(Components.InputColumn("Pc. de compra", produtoEncontrado.map(p-> p != null? p.descricao: ""),"Ex: R$ 10,00"))
                        .r_child(Components.InputColumnCurrency("Pc. de compra", pcCompra))
                        .r_child(Components.InputColumn("Quantidade", qtd,"Ex: 2"))
                        .r_child(Components.InputColumnCurrency("Desconto em R$", descontoEmDinheiro))
                )
                .c_child(new SpacerVertical(10))
                .c_child(
                        new Row(new RowProps().spacingOf(10)).r_child(Components.SelectColumn("Tipo de pagamento",tiposPagamento, tipoPagamentoSeleced,it->it))
                                .r_child(Components.TextAreaColumn("Observação",observacao,""))
                )
                .c_child(new SpacerVertical(10))
                .c_child(aPrazoForm())
                .c_child(new SpacerVertical(10))
                .c_child(valoresRow)
                .c_child(Components.ButtonCadastro(btnText, this::handleSaveOrUpdate))
                .getJavaFxNode()
        );

        return new Card(Component.CreateFromJavaFxNode(scroll));
    }

    Component aPrazoForm() {
        var dtPrimeiraParcela = State.of(LocalDate.now().plusMonths(1).minusDays(1));
        var qtdParcelas = State.of("1");

        Runnable handleGerarParcelas = ()->{
           gerarParcelas(dtPrimeiraParcela.get(), Integer.parseInt(qtdParcelas.get()), totalLiquido.get());
        };

        ForEachState<Parcela, Component> parcelaComponentForEachState = ForEachState.of(parcelas, this::parcelaItem);

        return Show.when(tipoPagamentoSelectedIsAPrazo,
                ()-> new Column(new ColumnProps())
                        .c_child(
                                new Row(new RowProps().spacingOf(10).bottomVertically())
                                .r_child(Components.DatePickerColumn(dtPrimeiraParcela, "Data primeira parcela", ""))
                                .r_child(Components.InputColumn("Quantidade de parcelas",qtdParcelas, "Ex: 1"))
                                .r_child(Components.ButtonCadastro("Gerar parcelas", handleGerarParcelas)))
                        .items(parcelaComponentForEachState)
                );
    }

    Component parcelaItem(Parcela parcela){
        return new Row(new RowProps())
                .r_child(Components.TextColumn("PARCELA",String.valueOf(parcela.numero())))
                .r_child(Components.TextColumn("VENCIMENTO",parcela.dataVencimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))))
                .r_child(Components.TextColumn("VALOR",String.format("R$ %.2f", parcela.valor())));
    }

    record Parcela(int numero, LocalDate dataVencimento, double valor) {
    }

    void handleSaveOrUpdate(){
        Async.Run(()->{
            if(modoEdicao.get()){
                //TODO: implementar
                return;
            }
            try {
                var dto = new CompraDto(codigo.get(), pcCompra.get(),fornecedorSelected.get().id,
                        new BigDecimal(qtd.get()), descontoEmDinheiro.get(),
                        tipoPagamentoSeleced.get(), observacao.get());
                var compraSalva = comprasRepository.salvar(dto);
                UI.runOnUi(()->{
                    IO.println("compra foi salva!");
                });
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
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



    private void handleAdd(LocalDate localDate){
        //TODO: implementar

        IO.println(dataCompra.get());

        String dataBR = localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        // p.precoCompra = new BigDecimal(precoCompraRaw.get()).movePointLeft(2);

    }

    private void handleClickMenuNew() {
       modoEdicao.set(false);
        //nome.set("");
    }

    @Override
    public void handleClickNew() {
        modoEdicao.set(false);
        clearForm();
    }

    @Override
    public void handleClickMenuEdit() {
        //  nome.set(categoriaSelecionada.get().nome);
       modoEdicao.set(true);
    }

    @Override
    public void handleClickMenuDelete() {
        //TODO: implementar
    }

    @Override
    public void handleClickMenuClone() {

    }

    @Override
    public void handleAddOrUpdate() {

    }

    @Override
    public void clearForm() {

    }

    @Override
    public Component table() {
        return null;
    }

    private void handleClickMenuCopy(){
        modoEdicao.set(false);

        final var compra = compraSelected.get();
        if(compra!=null){
            dataCompra.set(compra.dataCompra);
            numeroNota.set(compra.numeroNota);
            codigo.set(compra.produtoCod);
            //TODO: produtoEncontrado
            qtd.set(String.valueOf(compra.quantidade));
            observacao.set(compra.observacao);
            tipoPagamentoSeleced.set(compra.tipoPagamento);
            //TODO: parcelas
            pcCompra.set(compra.precoDeCompra);
            //TODO: totalBruto
            dataValidade.set(compra.dataValidade);
            fornecedorSelected.set(compra.fornecedor);
        }



    }



}