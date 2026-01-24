package my_app.screens.categoriasScreen;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import megalodonte.ComputedState;
import megalodonte.State;
import megalodonte.async.Async;
import megalodonte.base.UI;
import megalodonte.components.*;
import megalodonte.props.*;
import megalodonte.router.Router;
import megalodonte.styles.ColumnStyler;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import my_app.db.dto.CategoriaDto;
import my_app.db.models.CategoriaModel;
import my_app.db.repositories.CategoriaRepository;
import my_app.screens.ContratoTelaCrud;
import my_app.screens.components.Components;
import my_app.utils.Utils;

import java.sql.SQLException;

public class CategoriaScreen implements ScreenComponent, ContratoTelaCrud {
    private final Router router;
    private final Theme theme = ThemeManager.theme();
    State<String> nome = State.of("");
    State<Boolean> modoEdicao = State.of(false);

    ComputedState<String> btnText = ComputedState.of(() -> modoEdicao.get() ? "Atualizar" : "+ Adicionar", modoEdicao);
    State<CategoriaModel> categoriaSelecionada = State.of(null);
    ObservableList<CategoriaModel> categoriasObservable = FXCollections.observableArrayList();
    private CategoriaRepository categoriaRepository = new CategoriaRepository();

    public CategoriaScreen(Router router) {
        this.router = router;
    }

    public void onMount() {
        loadCategorias();
    }

    private void loadCategorias() {
        Async.Run(() -> {
            try {
                categoriasObservable.clear();
                categoriasObservable.addAll(categoriaRepository.listar());
            } catch (Exception e) {
                Components.ShowAlertError("Erro ao carregar categorias: " + e.getMessage());
            }
        });

    }

    public Component render() {
        return mainView();
    }

    @Override
    public Component form() {
        return new Card(new Column()
                .c_child(Components.FormTitle("Cadastrar Nova Categoria"))
                .c_child(new SpacerVertical(20))
                .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                        .r_child(
                                Components.InputColumn("Nome", nome, "Ex: Eletrômicos"))
                        .r_child(Components.ButtonCadastro(btnText, this::handleAddOrUpdate))
                )
                .c_child(new SpacerVertical(20))
                .c_child(Components.actionButtons(btnText, this::handleAddOrUpdate, this::clearForm))
        );

    }


    @Override
    public void handleClickNew() {
        modoEdicao.set(false);
        clearForm();
    }

    @Override
    public void handleClickMenuEdit() {
        modoEdicao.set(true);
        if (categoriaSelecionada.get() != null)
            nome.set(categoriaSelecionada.get().nome);
    }

    @Override
    public void handleClickMenuDelete() {
        if (categoriaSelecionada != null) {
            modoEdicao.set(false);

            Async.Run(() -> {
                try {
                    Long id = categoriaSelecionada.get().id;
                    categoriaRepository.excluirById(id);
                    UI.runOnUi(() -> {
                        Components.ShowPopup(router, "categoria excluida com sucesso");
                        categoriasObservable.removeIf(categoriaModel -> categoriaModel.id.equals(id));
                    });
                } catch (SQLException e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao tentar excluir: " + e.getMessage()));
                }
            });

        }
    }

    @Override
    public void handleClickMenuClone() {
        modoEdicao.set(false);

        final var data = categoriaSelecionada.get();
        if (data != null) {
            nome.set(data.nome);
        }
    }

    @Override
    public void handleAddOrUpdate() {
        String value = nome.get().trim();

        if (value.isEmpty()) {
            Components.ShowAlertError("Preencha o nome da categoria");
            return;
        }

        if (modoEdicao.get() && categoriaSelecionada.get() == null) return;

        if (modoEdicao.get()) {
            Async.Run(() -> {
                try {
                    final var model = categoriaSelecionada.get();
                    model.nome = value;
                    categoriaRepository.atualizar(model);
                    loadCategorias();
                    UI.runOnUi(() -> {
                        Components.ShowPopup(router, "Categoria atualizada com sucesso");
                        clearForm();
                    });
                } catch (Exception e) {
                    UI.runOnUi(()-> Components.ShowAlertError(e.getMessage()));
                }
            });
        } else {
            var dto = new CategoriaDto(value.trim());

            Async.Run(() -> {
                try {
                    var model = categoriaRepository.salvar(dto);
                    UI.runOnUi(() -> {
                        categoriasObservable.add(model);
                        Components.ShowPopup(router, "Categoria '" + model.nome + "' cadastrada com sucesso");
                        nome.set("");
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }


    }

    @Override
    public void clearForm() {
        nome.set("");
    }

    @Override
    public Component table() {
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
                        "-fx-selection-bar-non-focused: %s;".formatted(theme.colors().primary())  // Cor quando a tabela perde o foco
        );

        //individual
        // colNome.setStyle("-fx-font-size: %spx; -fx-alignment: CENTER-LEFT;".formatted(theme.typography().body()));

        // ===== COLUNA: DATA =====
        TableColumn<CategoriaModel, String> colData = new TableColumn<>("Data criação");
        colData.setCellValueFactory(data -> {
            var millis = data.getValue().dataCriacao;
            return new javafx.beans.property.SimpleStringProperty(Utils.formatDateTime(millis));
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