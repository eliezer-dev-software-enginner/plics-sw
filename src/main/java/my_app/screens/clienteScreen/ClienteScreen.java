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
import my_app.db.dto.ClienteDto;
import my_app.db.dto.FornecedorDto;
import my_app.db.models.CategoriaModel;
import my_app.db.models.ClienteModel;
import my_app.db.models.FornecedorModel;
import my_app.db.repositories.ClienteRepository;
import my_app.db.repositories.FornecedorRepository;
import my_app.screens.components.Components;

public class ClienteScreen {
    private final Router router;
    private final Theme theme = ThemeManager.theme();
    private final ClienteRepository clienteRepository = new ClienteRepository();

    private final ObservableList<ClienteModel> clientes = FXCollections.observableArrayList();
    State<ClienteModel> clienteSelecionado = State.of(null);

    public ClienteScreen(Router router) {
        this.router = router;
        loadClientes();
    }

    private void loadClientes() {
        try {
            clientes.clear();
            clientes.addAll(clienteRepository.listar());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar clientes", e);
        }
    }

    public Component render() {
        return new Column(new ColumnProps().paddingAll(7), new ColumnStyler().bgColor(theme.colors().background()))
                .c_child(Components.commonCustomMenus(this::handleClickMenuNew,
                       this::handleClickMenuEdit,
                        () -> {
                            // delete logic
                            //TODO: implementar lógica de deletar
                        }
                ))
                .c_child(new SpacerVertical(10))
                .c_child(form())
                .c_child(new SpacerVertical(30))
                .c_child(table());
    }

    private void handleClickMenuNew() {
            btnText.set("+ Adicionar");
            nome.set("");
            cnpj.set("");
    }

    private void handleClickMenuEdit() {
        nome.set(clienteSelecionado.get().nome);
        cnpj.set(clienteSelecionado.get().cpfCnpj);
        btnText.set("+ Atualizar");
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
                                .r_child(Components.InputColumn("Nome", nome))
                                .r_child(Components.InputColumn("CPF/CNPJ", cnpj))
                                .r_child(Components.InputColumn("Celular", celular))
                                .r_child(Components.ButtonCadastro(btnText,this::handleAdd))
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
            //TODO: preencher mais dados
            var dto = new ClienteDto(nomeValue, cnpjValue, "");
            var model = clienteRepository.salvar(dto);
            clientes.add(model);
            
            IO.println("cliente '" + model.nome + "' cadastrado com ID: " + model.id);
            
            // Limpa formulário
            nome.set("");
            cnpj.set("");
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Component table() {
        TableView<ClienteModel> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Coluna ID
        TableColumn<ClienteModel, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                    data.getValue().id != null ? String.valueOf(data.getValue().id) : ""
                )
        );
        idCol.setMinWidth(60);
        idCol.setMaxWidth(60);

        // Coluna Nome
        TableColumn<ClienteModel, String> nomeCol = new TableColumn<>("Nome");
        nomeCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().nome)
        );
        //nomeCol.setPrefWidth(100);

        // Coluna CNPJ
        TableColumn<ClienteModel, String> cnpjCol = new TableColumn<>("CNPJ");
        cnpjCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().cpfCnpj)
        );

        // Coluna Data Criação
        TableColumn<ClienteModel, String> dataCol = new TableColumn<>("Data Criação");
        dataCol.setCellValueFactory(data -> {
            if (data.getValue().dataCriacao != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    new java.util.Date(data.getValue().dataCriacao).toString()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        table.getColumns().addAll(idCol, nomeCol, cnpjCol, dataCol);
        table.setItems(clientes);

        table.getSelectionModel().selectedItemProperty().addListener((_, _, newSelection) -> {
            if (newSelection != null) {
                IO.println("ID selecionado: " + newSelection.id);
                clienteSelecionado.set(newSelection);
            }
        });


        return Component.CreateFromJavaFxNode(table);
    }
}