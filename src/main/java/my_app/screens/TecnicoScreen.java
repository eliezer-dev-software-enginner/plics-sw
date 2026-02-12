package my_app.screens;

import megalodonte.ComputedState;
import megalodonte.ListState;
import megalodonte.State;
import megalodonte.async.Async;
import megalodonte.base.UI;
import megalodonte.components.*;
import megalodonte.props.RowProps;
import megalodonte.router.Router;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import my_app.db.dto.TecnicoDto;
import my_app.db.models.TecnicoModel;
import my_app.db.repositories.TecnicoRepository;
import my_app.domain.ContratoTelaCrud;
import my_app.events.EventBus;
import my_app.events.TecnicoCriadoEvent;
import my_app.screens.components.Components;
import my_app.utils.DateUtils;

import java.sql.SQLException;
import java.util.List;

public class TecnicoScreen implements ScreenComponent, ContratoTelaCrud {
    private final Router router;
    private final TecnicoRepository tecnicoRepository;

    State<String> nome = State.of("");
    State<Boolean> modoEdicao = State.of(false);

    ComputedState<String> btnText = ComputedState.of(() -> modoEdicao.get() ? "Atualizar" : "+ Cadastrar", modoEdicao);
    State<TecnicoModel> tecnicoSelecionada = State.of(null);
    ListState<TecnicoModel> tecnicos = ListState.of(List.of());

    public TecnicoScreen(Router router) {
        this.router = router;
        tecnicoRepository = new TecnicoRepository();
    }

    public void onMount() {
        loadTecnicos();
    }

    private void loadTecnicos() {
        Async.Run(() -> {
            try {
               tecnicos.addAll(tecnicoRepository.listar());
            } catch (Exception e) {
                Components.ShowAlertError("Erro ao carregar tecnicos: " + e.getMessage());
            }
        });

    }

    public Component render() {
        return mainView();
    }

    @Override
    public Component form() {
        return new Card(new Column()
                .c_child(Components.FormTitle("Cadastrar Novo Técnico"))
                .c_child(new SpacerVertical(20))
                .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                        .r_child(
                                Components.InputColumn("Nome", nome, "Ex: Matias"))
//                        .r_child(
//                                Components.SelectColumn("Status", status, statusSelecionado, it-> it))
                )
                .c_child(new SpacerVertical(20))
                .c_child(Components.actionButtons(btnText, this::handleAddOrUpdate, this::clearForm))
        );
    }

    @Override
    public Component table() {
        return new SimpleTable<TecnicoModel>()
                .fromData(tecnicos)
                .onItemSelectChange(tecnicoSelecionada::set)
                .header()
                .columns()
                .column("ID", it-> it.id, 90.0)
                .column("Nome", it-> it.nome)
                .column("Data criação", it-> DateUtils.millisToBrazilianDateTime(it.dataCriacao))
                .build();
    }


    @Override
    public void handleClickNew() {
        modoEdicao.set(false);
        clearForm();
    }

    @Override
    public void handleClickMenuEdit() {
        modoEdicao.set(true);
        if (tecnicoSelecionada.get() != null)
            nome.set(tecnicoSelecionada.get().nome);
    }

    @Override
    public void handleClickMenuDelete() {
        if (tecnicoSelecionada != null) {
            modoEdicao.set(false);

            Async.Run(() -> {
                try {
                    Long id = tecnicoSelecionada.get().id;
                    tecnicoRepository.excluirById(id);
                    UI.runOnUi(() -> {
                        Components.ShowPopup(router, "técnico excluido com sucesso");
                        tecnicos.removeIf(tecnicoModel -> tecnicoModel.id.equals(id));
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

        final var data = tecnicoSelecionada.get();
        if (data != null) {
            nome.set(data.nome);
        }
    }

    @Override
    public void handleAddOrUpdate() {
        String value = nome.get().trim();

        if (value.isEmpty()) {
            Components.ShowAlertError("Preencha o nome do técnico");
            return;
        }

        if (modoEdicao.get() && tecnicoSelecionada.get() == null) return;

        var dto = new TecnicoDto(value.trim());

        if (modoEdicao.get()) {
            Async.Run(() -> {
                try {
                    final var model = tecnicoSelecionada.get();
                    model.nome = value;
                    tecnicoRepository.atualizar(model);
                   //tecnicos.updateIf(it -> it.id.equals(tecnicoSelecionada.get().id), model);
                    loadTecnicos();
                    UI.runOnUi(() -> {
                        Components.ShowPopup(router, "Técnico atualizada com sucesso");
                        clearForm();
                    });
                } catch (Exception e) {
                    UI.runOnUi(()-> Components.ShowAlertError(e.getMessage()));
                }
            });
        } else {
            Async.Run(() -> {
                try {
                    var model = tecnicoRepository.salvar(dto);
                    UI.runOnUi(() -> {
                        tecnicos.add(model);
                        Components.ShowPopup(router, "Técnico '" + model.nome + "' cadastrado com sucesso");
                        clearForm();
                        // Publicar evento de técnico criado
                        EventBus.getInstance().publish(new TecnicoCriadoEvent(model));
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

}