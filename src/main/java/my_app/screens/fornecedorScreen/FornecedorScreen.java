package my_app.screens.fornecedorScreen;

import javafx.scene.Node;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.inputs.Input;
import megalodonte.props.TextProps;
import megalodonte.props.TextVariant;
import megalodonte.router.Router;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import my_app.db.dto.FornecedorDto;
import my_app.db.models.FornecedorModel;
import my_app.db.repositories.FornecedorRepository;
import my_app.screens.components.Components;

import java.util.List;
import java.util.stream.Collectors;

public class FornecedorScreen {
    private final Router router;
    private final Theme theme = ThemeManager.theme();
    private final FornecedorRepository fornecedorRepository = new FornecedorRepository();
    private final List<FornecedorModel> fornecedores;

    public FornecedorScreen(Router router) {
        this.router = router;
        this.fornecedores = loadFornecedores();
    }

    private List<FornecedorModel> loadFornecedores() {
        try {
            return fornecedorRepository.listar();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar fornecedores", e);
        }
    }

    public Component render() {
        return new Column(new ColumnProps().paddingAll(25), new ColumnStyler().bgColor(theme.colors().background()))
                .c_child(Components.commonCustomMenus(
                        () -> {
                            btnText.set("+ Adicionar");
                            nome.set("");
                            cnpj.set("");
                        },
                        () -> {
                            btnText.set("Salvar");
                        },
                        () -> {
                            // delete logic
                        }
                ))
                .c_child(new SpacerVertical(30))
                .c_child(form())
                .c_child(new SpacerVertical(30))
                .c_child(table());
    }

    private final State<String> nome = new State<>("");
    private final State<String> cnpj = new State<>("");
    private final State<String> btnText = new State<>("+ Adicionar");

    Component form() {
        return new Card(
                new Column(new ColumnProps().paddingAll(20))
                        .c_child(new Row(new RowProps().centerHorizontally())
                                .r_child(new Text("Cadastro de Fornecedor", new TextProps().bold().variant(TextVariant.SUBTITLE))))
                        .c_child(new SpacerVertical(20))
                        .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                                .r_child(InputColumn("Nome Fantasia", nome))
                                .r_child(InputColumn("CNPJ", cnpj))
                                .r_child(new Button(btnText, new ButtonProps().fillWidth().height(45).bgColor("#2563eb").fontSize(20).textColor("white")
                                        .onClick(this::handleAdd)))
                        ));
    }

    private void handleAdd() {
        String nomeValue = nome.get().trim();
        String cnpjValue = cnpj.get().trim();

        if (nomeValue.isEmpty()) {
            IO.println("Nome é obrigatório");
            return;
        }

        try {
            var dto = new FornecedorDto(nomeValue, cnpjValue, "", "", "", System.currentTimeMillis());
            var model = fornecedorRepository.salvar(dto);
            
            // Atualiza lista de fornecedores
            fornecedores.clear();
            fornecedores.addAll(loadFornecedores());
            
            IO.println("Fornecedor '" + model.nome + "' cadastrado com ID: " + model.id);
            
            // Limpa formulário
            nome.set("");
            cnpj.set("");
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Component table() {
        TableView<FornecedorModel> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Coluna ID
        TableColumn<FornecedorModel, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(80);

        // Coluna Nome
        TableColumn<FornecedorModel, String> nomeCol = new TableColumn<>("Nome");
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome"));

        // Coluna CNPJ
        TableColumn<FornecedorModel, String> cnpjCol = new TableColumn<>("CNPJ");
        cnpjCol.setCellValueFactory(new PropertyValueFactory<>("cpfCnpj"));

        // Coluna Data Criação
        TableColumn<FornecedorModel, String> dataCol = new TableColumn<>("Data Criação");
        dataCol.setCellValueFactory(param -> {
            if (param.getValue().dataCriacao != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    new java.util.Date(param.getValue().dataCriacao).toString()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        table.getColumns().addAll(idCol, nomeCol, cnpjCol, dataCol);
        table.getItems().addAll(fornecedores);

        return Component.FromJavaFxNode(table);
    }

    private static Component InputColumn(String label, State<String> inputState) {
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(22)))
                .c_child(new Input(inputState, new InputProps().fontSize(20).height(40)));
    }
}