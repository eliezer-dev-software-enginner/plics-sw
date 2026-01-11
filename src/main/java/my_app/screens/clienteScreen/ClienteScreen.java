package my_app.screens.clienteScreen;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import megalodonte.State;
import megalodonte.components.*;
import megalodonte.components.inputs.Input;
import megalodonte.props.*;
import megalodonte.router.Router;
import megalodonte.styles.ColumnStyler;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import megalodonte.utils.related.TextVariant;
import my_app.db.dto.FornecedorDto;
import my_app.db.models.FornecedorModel;
import my_app.db.repositories.FornecedorRepository;
import my_app.screens.components.Components;

public class ClienteScreen {
    private final Router router;
    private final Theme theme = ThemeManager.theme();
    private final FornecedorRepository fornecedorRepository = new FornecedorRepository();

    private final ObservableList<FornecedorModel> fornecedores = FXCollections.observableArrayList();

    public ClienteScreen(Router router) {
        this.router = router;
        loadFornecedores();
    }

    private void loadFornecedores() {
        try {
            fornecedores.clear();
            fornecedores.addAll(fornecedorRepository.listar());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar fornecedores", e);
        }
    }

    

    public Component render() {
        return new Column(new ColumnProps().paddingAll(25), new ColumnStyler().bgColor(theme.colors().background()))
                .c_child(Components.commonCustomMenus(this::handleClickNew,
                       this::handleClickEdit,
                        () -> {
                            // delete logic
                        }
                ))
                .c_child(new SpacerVertical(30))
                .c_child(form())
                .c_child(new SpacerVertical(30))
                .c_child(table());
    }

    private void handleClickNew() {
            btnText.set("+ Adicionar");
            nome.set("");
            cnpj.set("");
    }

    private void handleClickEdit() {
        btnText.set("+ Adicionar");
        nome.set("");
        cnpj.set("");
    }


    private final State<String> nome = new State<>("");
    private final State<String> cnpj = new State<>("");
    private final State<String> celular = new State<>("");
    private final State<String> btnText = new State<>("+ Adicionar");

    Component form() {
        return new Card(
                new Column(new ColumnProps().paddingAll(20))
                        .c_child(new Row(new RowProps().centerHorizontally())
                                .r_child(new Text("Cadastro de Cliente", new TextProps().bold().variant(TextVariant.SUBTITLE))))
                        .c_child(new SpacerVertical(20))
                        .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                                .r_child(InputColumn("Nome", nome))
                                .r_child(InputColumn("CPF/CNPJ", cnpj))
                                .r_child(InputColumn("Celular", celular))
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
            fornecedores.add(model);
            
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
        TableColumn<FornecedorModel, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                    data.getValue().id != null ? String.valueOf(data.getValue().id) : ""
                )
        );
        idCol.setMaxWidth(80);

        // Coluna Nome
        TableColumn<FornecedorModel, String> nomeCol = new TableColumn<>("Nome");
        nomeCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().nome)
        );
        nomeCol.setPrefWidth(300);

        // Coluna CNPJ
        TableColumn<FornecedorModel, String> cnpjCol = new TableColumn<>("CNPJ");
        cnpjCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().cpfCnpj)
        );

        // Coluna Data Criação
        TableColumn<FornecedorModel, String> dataCol = new TableColumn<>("Data Criação");
        dataCol.setCellValueFactory(data -> {
            if (data.getValue().dataCriacao != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    new java.util.Date(data.getValue().dataCriacao).toString()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        table.getColumns().addAll(idCol, nomeCol, cnpjCol, dataCol);
        table.setItems(fornecedores);

        return Component.CreateFromJavaFxNode(table);
    }

    private static Component InputColumn(String label, State<String> inputState) {
        return new Column()
                .c_child(new Text(label, new TextProps().variant(TextVariant.SMALL).bold()))
                .c_child(new Input(inputState, new InputProps().variant(TextVariant.SMALL).height(40)));
    }
}