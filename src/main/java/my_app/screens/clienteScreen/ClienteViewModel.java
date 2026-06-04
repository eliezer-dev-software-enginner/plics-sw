package my_app.screens.clienteScreen;

import megalodonte.ComputedState;
import megalodonte.v2.ListState;
import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.ClienteModel;
import my_app.db.services.ClienteService;
import my_app.domain.Data;
import my_app.core.events.EntityEvent;
import my_app.core.events.EventBus;
import my_app.domain.ViewModelScreenContract;
import my_app.domain.components.Components;
import my_app.utils.Utils;

import java.sql.SQLException;

public class ClienteViewModel extends ViewModelScreenContract {
    private final ClienteService clienteService;

    final ListState<ClienteModel> clientes = ListState.ofEmpty();
    final State<ClienteModel> clienteSelecionado = State.of(null);

    final State<String> nome = new State<>("");
    final State<String> cnpjCpf = new State<>("");
    final State<String> celular = new State<>("");
    final State<String> email = new State<>("");

    final State<String> tipoPessoaSelected = new State<>(Data.tiposPessoaList.getFirst());
    final ComputedState<Boolean> tipoPessoaEhFisica = ComputedState.of(
            () -> tipoPessoaSelected.get().equals(Data.tiposPessoaList.getFirst()),
            tipoPessoaSelected
    );

    public ClienteViewModel(ScreenContext ctx) {
        super(ctx);
        try {
            clienteService = new ClienteService();
        } catch (SQLException e) {
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            throw new RuntimeException(e);
        }
        this.onInit();
    }

    @Override
    protected void onInit() {
        tipoPessoaSelected.subscribe(_ -> cnpjCpf.set(""));
    }

    @Override
    public void populateFromModel() {
        final var data = clienteSelecionado.get();
        if (data == null) return;
        nome.set(data.getNome());
        cnpjCpf.set(data.getCpfCnpj());
        celular.set(data.getCelular());
        email.set(data.getEmail());
        tipoPessoaSelected.set(
                Utils.isValidCpf(data.getCpfCnpj())
                        ? Data.tiposPessoaList.getFirst()
                        : Data.tiposPessoaList.getLast()
        );
    }

    private ClienteModel getModelFromFields(ClienteModel model){
        String nomeValue    = nome.get().trim();
        String cnpjCpfValue = cnpjCpf.get().trim();
        String celularValue = celular.get().trim();
        String emailValue   = email.get().trim();

        model.setNome(nomeValue);
        model.setCpfCnpj(cnpjCpfValue);
        model.setCelular(celularValue);
        model.setEmail(emailValue);
        model.setPessoaFisica(tipoPessoaEhFisica.get());

        return model;
    }

    void loadClientes() {
        Async.Run(() -> {
            try {
                var list = clienteService.listar();
                UI.runOnUi(() -> clientes.set(list));
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar clientes"));
            }
        });
    }

    @Override
    public void handleClickMenuDelete() {
        final var model = clienteSelecionado.get();
        if (model == null) return;

        Components.ShowAlertAdvice("Deseja excluir cliente " + model.getNome(), () -> {
            Async.Run(() -> {
                try {
                    clienteService.excluirById(model.getId());
                    UI.runOnUi(() -> {
                        clientes.removeIf(it -> it.getId().equals(model.getId()));
                        Components.ShowPopup(ctx, "Cliente excluído com sucesso");
                    });
                } catch (Exception e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao tentar excluir: " + e.getMessage()));
                }
            });
        });
    }

    @Override
    public void handleAddOrUpdate() {
        Async.Run(() -> {
            try {
                if (modoEdicao.get()) {
                    if(clienteSelecionado.get() == null)return;
                    var model = getModelFromFields(clienteSelecionado.get());
                    clienteService.atualizar(model);
                    UI.runOnUi(() -> {
                        clientes.updateIf(it -> it.getId().equals(model.getId()), it -> model);
                        Components.ShowPopup(ctx, "Ciente atualizado com sucesso");
                        clearForm();
                    });
                } else {
                    var model = getModelFromFields(new ClienteModel());
                    clienteService.salvar(model);
                    UI.runOnUi(() -> {
                        clientes.add(model);
                        Components.ShowPopup(ctx, "Cliente cadastrado com sucesso");
                        clearForm();
                        EventBus.getInstance().publish(EntityEvent.criado(model));
                    });
                }
            } catch (IllegalArgumentException e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro inesperado: " + e.getMessage()));
            }
        });
    }

    @Override
    public void clearForm() {
        nome.set("");
        cnpjCpf.set("");
        celular.set("");
        email.set("");
    }
}