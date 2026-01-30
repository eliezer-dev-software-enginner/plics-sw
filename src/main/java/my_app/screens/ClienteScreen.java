package my_app.screens;

import megalodonte.ComputedState;
import megalodonte.ListState;
import megalodonte.State;
import megalodonte.async.Async;
import megalodonte.base.UI;
import megalodonte.components.*;
import megalodonte.props.*;
import megalodonte.router.Router;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import my_app.db.dto.ClienteDto;
import my_app.db.models.ClienteModel;
import my_app.db.repositories.ClienteRepository;
import my_app.screens.components.Components;
import my_app.utils.Utils;

import java.util.List;

import static my_app.utils.Utils.*;

public class ClienteScreen implements ScreenComponent, ContratoTelaCrud {
    private final Router router;
    private final Theme theme = ThemeManager.theme();
    private final ClienteRepository clienteRepository = new ClienteRepository();
    
    ListState<ClienteModel> clientes = ListState.of(List.of());
    State<ClienteModel> clienteSelecionado = State.of(null);

    private final State<String> nome = new State<>("");
    private final State<String> cnpj = new State<>("");
    private final State<String> celular = new State<>("");
    private final State<String> email = new State<>("");

    State<Boolean> editMode = State.of(false);

    ComputedState<String> btnText = ComputedState.of( ()-> editMode.get()? "Atualizar": "+ Adicionar", editMode);

    public ClienteScreen(Router router) {
        this.router = router;
    }

    @Override
    public void onMount() {
        loadClientes();
    }

    private void loadClientes() {
        Async.Run(()->{
            try {
                var list = clienteRepository.listar();
                UI.runOnUi(()-> {
                    clientes.addAll(list);
                });
            } catch (Exception e) {
                UI.runOnUi(()->  Components.ShowAlertError("Erro ao buscar clientes"));
            }
        });

    }

    public Component render() {
        return mainView();
    }

    @Override
    public Component form() {
        return new Card(
                new Column(new ColumnProps().paddingAll(20))
                        .c_child(Components.FormTitle("Cadastrar cliente"))
                        .c_child(new SpacerVertical(20))
                        .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                                .r_child(Components.InputColumn("Nome", nome, "Ex: João"))
                                .r_child(Components.InputColumnNumeric("CPF/CNPJ", cnpj,"xx..."))
                                .r_child(Components.InputColumnPhone("Celular", celular))
                                .r_child(Components.InputColumn("Email", email,""))
                        )
                        .c_child(new SpacerVertical(20))
                        .c_child(Components.actionButtons(btnText, this::handleAddOrUpdate, this::clearForm)));
    }

    @Override
    public void handleClickNew() {
        editMode.set(false);
        clearForm();
    }

    @Override
    public void handleClickMenuEdit() {
        final var data = clienteSelecionado.get();
        if(data != null){
            editMode.set(true);
            nome.set(data.nome);
            cnpj.set(data.cpfCnpj);
            celular.set(data.celular);
            email.set(data.email);
        }
    }

    @Override
    public void handleClickMenuDelete() {
        final var forn = clienteSelecionado.get();
        if(forn != null){
            editMode.set(false);

            Components.ShowAlertAdvice("Deseja excluir cliente  " + forn.nome, ()->{
                Async.Run(()->{
                    try{
                        clienteRepository.excluirById(forn.id);
                        UI.runOnUi(()->{
                            clientes.removeIf(it-> it.id.equals(forn.id));
                            Components.ShowPopup(router, "Cliente excluido com sucesso");
                        });
                    }catch (Exception e){
                        UI.runOnUi(()->Components.ShowAlertError("Erro ao tentar excluir: " + e.getMessage()));
                    }
                });
            });
        }
    }

    @Override
    public void handleClickMenuClone() {
        editMode.set(false);

        final var data = clienteSelecionado.get();
        if(data != null){
            nome.set(data.nome);
            cnpj.set(data.cpfCnpj);
            celular.set(data.celular);
            email.set(data.email);
        }
    }

    @Override
    public void handleAddOrUpdate() {
        String nomeValue = nome.get().trim();
        String cnpjValue = cnpj.get().trim();
        String celularValue = celular.get().trim();
        String emailValue = email.get().trim();

        if (nomeValue.isEmpty()) {
            Components.ShowAlertError("Nome é obrigatório");
            return;
        }

        if (!cnpjValue.isEmpty() && !isValidCnpj(cnpjValue)) {
            Components.ShowAlertError("CNPJ inválido (deve conter 14 dígitos) e tem: " + cnpjValue.length() + " dígitos");
            return;
        }

        // 3. Validação de E-mail (se preenchido)
        if (!emailValue.isEmpty() && !isValidEmail(emailValue)) {
            Components.ShowAlertError("Formato de e-mail inválido");
            return;
        }

        // 4. Validação de Telefone/Celular
        if (!celularValue.isEmpty() && !isValidPhone(celularValue)) {
            Components.ShowAlertError("Telefone inválido (informe DDD + Número)");
            return;
        }

        if(editMode.get() && clienteSelecionado.get() == null) return;
        var id = clienteSelecionado.get().id;

        if(editMode.get()){
            Async.Run(()->{
                try {
                    // 1. Criamos a Model com os novos dados mantendo o ID e Data de Criação originais
                    var selecionado = clienteSelecionado.get();
                    var modelAtualizada = new ClienteModel().fromIdAndDto(selecionado.id, new ClienteDto(
                            nomeValue, cnpjValue, celularValue, emailValue
                    ));

                    // 2. Atualiza no Banco de Dados
                    clienteRepository.atualizar(modelAtualizada);

                    UI.runOnUi(() -> {
                        clientes.updateIf(it-> it.id.equals(id), it-> modelAtualizada);

                        Components.ShowPopup(router, "Cliente atualizado com sucesso");
                        clearForm();
                    });
                } catch (Exception e) {
                    UI.runOnUi(()-> Components.ShowAlertError(e.getMessage()));
                }
            });
        }else{
            Async.Run(()->{
                try {
                    var dto = new ClienteDto(
                            nomeValue,
                            cnpjValue,
                            celularValue,
                            emailValue
                    );

                    var model = clienteRepository.salvar(dto);

                    UI.runOnUi(()-> {
                        clientes.add(model);
                        Components.ShowPopup(router, "Cliente cadastrado com sucesso");
                        clearForm();
                    });

                } catch (Exception e) {
                    UI.runOnUi(()-> Components.ShowAlertError(e.getMessage()));
                }
            });
        }
    }

    @Override
    public void clearForm() {
        nome.set("");
        cnpj.set("");
        celular.set("");
        email.set("");
    }

    @Override
    public Component  table() {
        var simpleTable = new SimpleTable<ClienteModel>();
        simpleTable.fromData(clientes)
                .header()
                .columns()
                    .column("ID", it-> it.id)
                    .column("Nome", it-> it.nome)
                    .column("CPF/CNPJ", it-> it.cpfCnpj)
                    .column("Data de criação", it-> Utils.formatDateTime(it.dataCriacao))
                .build()
                .onItemSelectChange(it->   clienteSelecionado.set(it));

        return simpleTable;

//        TableView<ClienteModel> table = new TableView<>();
//        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
//
//        // Coluna ID
//        TableColumn<ClienteModel, String> idCol = new TableColumn<>("ID");
//        idCol.setCellValueFactory(data ->
//                new javafx.beans.property.SimpleStringProperty(
//                    data.getValue().id != null ? String.valueOf(data.getValue().id) : ""
//                )
//        );
//        idCol.setMinWidth(60);
//        idCol.setMaxWidth(60);
//
//        // Coluna Nome
//        TableColumn<ClienteModel, String> nomeCol = new TableColumn<>("Nome");
//        nomeCol.setCellValueFactory(data ->
//                new javafx.beans.property.SimpleStringProperty(data.getValue().nome)
//        );
//        //nomeCol.setPrefWidth(100);
//
//        // Coluna CNPJ
//        TableColumn<ClienteModel, String> cnpjCol = new TableColumn<>("CNPJ");
//        cnpjCol.setCellValueFactory(data ->
//                new javafx.beans.property.SimpleStringProperty(data.getValue().cpfCnpj)
//        );
//
//        // Coluna Data Criação
//        TableColumn<ClienteModel, String> dataCol = new TableColumn<>("Data Criação");
//        dataCol.setCellValueFactory(data -> {
//            if (data.getValue().dataCriacao != null) {
//                return new javafx.beans.property.SimpleStringProperty(
//                    Utils.formatDateTime(data.getValue().dataCriacao));
//            }
//            return new javafx.beans.property.SimpleStringProperty("");
//        });
//
//        table.getColumns().addAll(idCol, nomeCol, cnpjCol, dataCol);
//        table.setItems(clientes);
//
//        table.getSelectionModel().selectedItemProperty().addListener((_, _, newSelection) -> {
//            if (newSelection != null) {
//                IO.println("ID selecionado: " + newSelection.id);
//                clienteSelecionado.set(newSelection);
//            }
//        });


       // return Component.CreateFromJavaFxNode(table);
    }
}