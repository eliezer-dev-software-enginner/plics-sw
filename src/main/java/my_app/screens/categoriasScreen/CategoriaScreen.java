package my_app.screens.categoriasScreen;

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
import my_app.db.dto.CategoriaDto;
import my_app.db.models.CategoriaModel;
import my_app.db.repositories.CategoriaRepository;
import my_app.screens.components.Components;

public class CategoriaScreen {
    private final Router router;

    State<String> nome = State.of("");
    State<String> btnText = State.of("+ Adicionar");
    State<CategoriaModel> categoriaSelecionada = State.of(null);

    ObservableList<CategoriaModel> categoriasObservable = FXCollections.observableArrayList();

    private CategoriaRepository categoriaRepository = new CategoriaRepository();
    public CategoriaScreen(Router router) {
        this.router = router;
        loadCategorias();
    }

    private void loadCategorias() {
        try {
            categoriasObservable.clear();
            categoriasObservable.addAll(categoriaRepository.listar());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar categorias", e);
        }
    }

    private Theme theme = ThemeManager.theme();



    public Component render() {
        return new Column(new ColumnProps().paddingAll(25), new ColumnStyler().bgColor(theme.colors().background()))
                .c_child(Components.commonCustomMenus(
                        ()->{
                            //new
                            btnText.set("+ Adicionar");
                            nome.set("");
                        }
                        ,()->{
                    //edit
                    btnText.set("Atualizar");
                    nome.set(categoriaSelecionada.get().nome);
                }, ()->{
                    //delete
                    Long id = categoriaSelecionada.get().id;
                    CategoriaModel selecionada = categoriasObservable.stream()
                            .filter(cat -> cat.id.equals(id))
                            .findFirst()
                            .orElse(null);
                    if (selecionada != null) {
                        categoriasObservable.remove(selecionada);
                    }
                }))
                .c_child(new SpacerVertical(30))
                .c_child(header())
                .c_child(new SpacerVertical(20))
                .c_child(form())
                .c_child(new SpacerVertical(20))
                .c_child(table());
    }

    Component header(){
       return new Column()
               .c_child(new Text("Gerenciamento de Categorias de Estoque", new TextProps().variant(TextVariant.SUBTITLE)));
    }

    Component form(){

        return new Card(new Column()
                .c_child(
                        new Row()
                                .r_child(new Text("Cadastrar Nova Categoria", new TextProps().bold().variant(TextVariant.SUBTITLE))))
                .c_child(new SpacerVertical(20))
                .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                        .r_child(new Input(nome, new InputProps().height(45).fontSize(18).placeHolder("Ex: Eletrônicos")))
                        .r_child(new Button(btnText, new ButtonProps().fillWidth().height(45).bgColor("#2563eb").fontSize(20).textColor("white")
                                .onClick(this::handleAdd)))
                ));
    }

    private void handleAdd(){
        String value = nome.get();
        IO.println("Nome: " + value);

        var dto = new CategoriaDto(value.trim(), System.currentTimeMillis());

        try{
            var model = categoriaRepository.salvar(dto);
            categoriasObservable.add(model);
            IO.println("Categoria '" + model.nome + "' cadastrada com ID: " + model.id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    Component table() {
        TableView<CategoriaModel> table = new TableView<>();

        // ===== COLUNA: NOME =====
        TableColumn<CategoriaModel, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().nome)
        );
        colNome.setPrefWidth(300);

        // Aumenta a fonte de toda a tabela (células)
        //table.setStyle("-fx-font-size: %spx;".formatted(theme.typography().body()));
        table.setStyle(
                "-fx-font-size: %spx; ".formatted(theme.typography().body()) +
                        "-fx-background-color: %s; ".formatted(theme.colors().background()) + // Fundo da tabela
                        "-fx-control-inner-background: %s; ".formatted(theme.colors().surface()) + // Fundo das células
                        "-fx-text-background-color: %s;".formatted("black") +// Cor do texto
                "-fx-selection-bar: %s; ".formatted(theme.colors().primary()) + // Cor da barra de seleção (Azul igual ao seu botão)
                        "-fx-selection-bar-non-focused: ;".formatted(theme.colors().primary())  // Cor quando a tabela perde o foco
        );

        //individual
       // colNome.setStyle("-fx-font-size: %spx; -fx-alignment: CENTER-LEFT;".formatted(theme.typography().body()));

        // ===== COLUNA: DATA =====
        TableColumn<CategoriaModel, String> colData = new TableColumn<>("Data criação");
        colData.setCellValueFactory(data -> {
            var millis = data.getValue().dataCriacao;
            var dataFormatada = java.time.Instant.ofEpochMilli(millis)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                    .toString();
            return new javafx.beans.property.SimpleStringProperty(dataFormatada);
        });
        colData.setPrefWidth(200);

        // CategoriaModel item = getTableView().getItems().get(getIndex());


        // Dentro do seu método table()
        table.getSelectionModel().selectedItemProperty().addListener((_, _, newSelection) -> {
            if (newSelection != null) {
                IO.println("ID selecionado: " + newSelection.id);
               categoriaSelecionada.set(newSelection);
            }
        });


        // ===== ADD COLUNAS =====
        table.getColumns().addAll(colNome, colData);

        // ===== DADOS =====
        table.setItems(categoriasObservable);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        return new Card(Component.CreateFromJavaFxNode(table));
    }


}