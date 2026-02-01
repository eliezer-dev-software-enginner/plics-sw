package my_app.screens.produtoScreen;

import megalodonte.*;
import megalodonte.components.*;
import megalodonte.props.*;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import megalodonte.utils.related.TextVariant;
import my_app.db.models.ProdutoModel;
import my_app.db.models.VendaModel;
import my_app.screens.components.Components;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.text.NumberFormat;
import java.util.Locale;

public class ProdutoComponents {
    private static Theme theme = ThemeManager.theme();

    public static Component ContainerLeft(ProdutoScreenViewModel vm) {
        var rowProps = new RowProps().spacingOf(10);

        Runnable handleGerarCodigoBarras = ()->{
           final var codigo = Utils.gerarCodigoBarrasEAN13();
           vm.codigoBarras.set(codigo);
        };

        return new Column(new ColumnProps().spacingOf(20))
                .c_child(
                        new Row(rowProps)
                                .r_child(new Row(new RowProps().bottomVertically())
                                        .r_child(Components.InputColumn("SKU(Código de barras)", vm.codigoBarras, ""))
                                        .r_child(new Button("Gerar", new ButtonProps().height(37)
                                                .textColor("#FFF")
                                                .onClick(handleGerarCodigoBarras)))
                                )
                                .r_child(Components.InputColumn("Descrição curta", vm.descricao, ""))
                                .r_child(Components.SelectColumn("Unidade", vm.unidades, vm.unidadeSelected, it -> it))
                                .r_child(Components.InputColumn("Marca", vm.marca, ""))
                ).c_child(new Row(rowProps)
                                .r_child(Components.InputColumnCurrency("Preço de compra", vm.precoCompra))
                                .r_child(Components.InputColumn("Margem %", vm.margem, ""))
//                        .r_child(Components.InputColumn("Lucro", vm.lucro,Entypo.CREDIT))
                                .r_child(Components.InputColumnCurrency("Preço de venda", vm.precoVenda))
                ).c_child(new Row(rowProps)
                        .r_child(Components.SelectColumn("Categoria", vm.categorias, vm.categoriaSelected, it -> it.nome))
                        .r_child(Components.SelectColumn("Fornecedor", vm.fornecedores, vm.fornecedorSelected, it -> it.nome))
                        .r_child(Components.InputColumn("Garantia", vm.garantia, ""))
                        .r_child(Components.InputColumn("Validade", vm.validade, ""))
                        .r_child(Components.InputColumn("Comissão", vm.comissao, ""))
                )
                .c_child(new Row(rowProps)
                        .r_child(Components.TextAreaColumn("Observações", vm.observacoes, ""))
                        .r_child(Components.InputColumn("Estoque", vm.estoque, ""))//fornecedor padrão
                );
    }
}
