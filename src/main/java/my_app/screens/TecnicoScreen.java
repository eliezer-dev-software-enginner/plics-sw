package my_app.screens;

import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Card;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Column;
import megalodonte.router.v4.ScreenContext;
import my_app.db.dto.TecnicoDto;
import my_app.db.models.*;
import my_app.db.repositories.*;
import my_app.domain.ContratoTelaCrud;
import my_app.domain.ContratoTelaCrudV2;
import my_app.events.EventBus;
import my_app.events.TecnicoEvents;
import my_app.screens.components.Components;
import javafx.scene.control.*;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import my_app.utils.DateUtils;

import java.sql.SQLException;
import java.util.List;

public class TecnicoScreen implements ScreenComponent, ContratoTelaCrudV2 {
    private final ScreenContext ctx;
    private final TecnicoRepository tecnicoRepository;

    State<String> nome = State.of("");
    State<Boolean> modoEdicao = State.of(false);

    ComputedState<String> btnText = ComputedState.of(() -> modoEdicao.get() ? "Atualizar" : "+ Cadastrar", modoEdicao);
    State<TecnicoModel> tecnicoSelecionada = State.of(null);
    ListState<TecnicoModel> tecnicos = ListState.of(List.of());

    public TecnicoScreen(ScreenContext ctx) {
        this.ctx = ctx;
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
                UI.runOnUi(()-> Components.ShowAlertError("Erro ao carregar tecnicos: " + e.getMessage()));
            }
        });
    }

    public Component render() {
        return mainView(tecnicoSelecionada);
    }

    @Override
    public Component form() {
        return new Card(new Column().children(
                        Components.FormTitle("Cadastrar Novo Técnico"),
                        new SpacerVertical(20),
                        new Row(new RowProps().bottomVertically().spacingOf(10))
                                .r_child(
                                        Components.InputColumn("Nome", nome, "Ex: Matias")),
                        new SpacerVertical(20),
                        Components.actionButtons(btnText, this::handleAddOrUpdate, this::clearForm)
                )
        );
    }

    @Override
    public Component table() {
        return new SimpleTable<TecnicoModel>()
                .fromData(tecnicos)
                .onItemSelectChange(tecnicoSelecionada::set)
                .onClickOutside(()-> {
                    tecnicoSelecionada.set(null);
                    modoEdicao.set(false);
                })
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
        if (tecnicoSelecionada.get() != null){
            modoEdicao.set(true);
            nome.set(tecnicoSelecionada.get().nome);
        }
    }

    @Override
    public void handleClickMenuDelete() {
        if (tecnicoSelecionada != null) {
            modoEdicao.set(false);

            Async.Run(() -> {
                try {
                    Long id = tecnicoSelecionada.get().id;
                    tecnicoRepository.excluirById(id);
                    EventBus.getInstance().publish(new TecnicoEvents.Excluido(id));
                    UI.runOnUi(() -> {
                        Components.ShowPopup(ctx, "técnico excluido com sucesso");
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
            asyncUpdate(value);
        } else {
            asyncCreate(dto);
        }
    }

    private void asyncCreate(TecnicoDto dto) {
        Async.Run(() -> {
            try {
                var model = tecnicoRepository.salvar(dto);
                UI.runOnUi(() -> {
                    tecnicos.add(model);
                    Components.ShowPopup(ctx, "Técnico '" + model.nome + "' cadastrado com sucesso");
                    clearForm();
                    // Publicar evento de técnico criado
                    EventBus.getInstance().publish(new TecnicoEvents.Criado(model));
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void asyncUpdate(String value) {
        Async.Run(() -> {
            try {
                final var original = tecnicoSelecionada.get();
                // não muta o original — cria novo com os dados atualizados
                final var model = new TecnicoModel();
                model.id = original.id;
                model.nome = value;
                model.dataCriacao = original.dataCriacao;

                tecnicoRepository.atualizar(model);
                EventBus.getInstance().publish(new TecnicoEvents.Editado(model));

                UI.runOnUi(() -> {
                    tecnicos.updateIf(it -> it.id.equals(model.id), it -> model);
                    Components.ShowPopup(ctx, "Técnico atualizada com sucesso");
                    clearForm();
                });
            } catch (Exception e) {
                UI.runOnUi(()-> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    @Override
    public void clearForm() {
        nome.set("");
    }

}