package my_app.screens.clienteScreen;

import megalodonte.*;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.db.dto.ClienteDto;
import my_app.db.models.ClienteModel;
import my_app.db.repositories.ClienteRepository;
import my_app.lifecycle.viewmodel.component.ViewModel;
import my_app.lifecycle.viewmodel.component.ViewModelv2;
import my_app.screens.components.Components;
import my_app.utils.Utils;

import java.util.List;

import static my_app.utils.Utils.*;

public class ClienteViewModel extends ViewModelv2 {

    private final ScreenContext ctx;
    private final ClienteRepository clienteRepository = new ClienteRepository();

    final ListState<ClienteModel> clientes = ListState.of(List.of());
    final State<ClienteModel> clienteSelecionado = State.of(null);

    final State<String> nome = new State<>("");
    final State<String> cnpjCpf = new State<>("");
    final State<String> celular = new State<>("");
    final State<String> email = new State<>("");

    final List<String> tipoPessoaList = List.of("Física", "Jurídica");
    final State<String> tipoPessoaSelected = new State<>(tipoPessoaList.getFirst());
    final ComputedState<Boolean> tipoPessoaEhFisica = ComputedState.of(
            () -> tipoPessoaSelected.get().equals(tipoPessoaList.getFirst()),
            tipoPessoaSelected
    );

    final State<Boolean> editMode = State.of(false);
    final ComputedState<String> btnText = ComputedState.of(
            () -> editMode.get() ? "Atualizar" : "+ Adicionar",
            editMode
    );

    public ClienteViewModel(ScreenContext ctx) {
        this.ctx = ctx;
        this.onInit();
    }

    @Override
    protected void onInit() {
        tipoPessoaSelected.subscribe(_ -> cnpjCpf.set(""));
    }

    void loadClientes() {
        Async.Run(() -> {
            try {
                var list = clienteRepository.listar();
                UI.runOnUi(() -> clientes.addAll(list));
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar clientes"));
            }
        });
    }

    void handleClickNew() {
        editMode.set(false);
        clearForm();
    }

    void handleClickMenuEdit() {
        editMode.set(true);
        populateFromCliente();
    }

    void handleClickMenuClone() {
        editMode.set(false);
        populateFromCliente();
    }

    void handleClickMenuDelete() {
        final var cliente = clienteSelecionado.get();
        if (cliente == null) return;

        editMode.set(false);
        Components.ShowAlertAdvice("Deseja excluir cliente " + cliente.nome, () -> {
            Async.Run(() -> {
                try {
                    clienteRepository.excluirById(cliente.id);
                    UI.runOnUi(() -> {
                        clientes.removeIf(it -> it.id.equals(cliente.id));
                        Components.ShowPopup(ctx, "Cliente excluído com sucesso");
                    });
                } catch (Exception e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao tentar excluir: " + e.getMessage()));
                }
            });
        });
    }

    void handleAddOrUpdate() {
        String nomeValue    = nome.get().trim();
        String cnpjCpfValue = cnpjCpf.get().trim();
        String celularValue = celular.get().trim();
        String emailValue   = email.get().trim();

        if (nomeValue.isEmpty()) {
            Components.ShowAlertError("Nome é obrigatório");
            return;
        }
        if (!cnpjCpfValue.isEmpty()) {
            if (tipoPessoaEhFisica.get() && !isValidCpf(cnpjCpfValue)) {
                Components.ShowAlertError("CPF inválido (deve conter 11 dígitos)");
                return;
            } else if (!tipoPessoaEhFisica.get() && !isValidCnpj(cnpjCpfValue)) {
                Components.ShowAlertError("CNPJ inválido (deve conter 14 dígitos)");
                return;
            }
        }
        if (!emailValue.isEmpty() && !isValidEmail(emailValue)) {
            Components.ShowAlertError("Formato de e-mail inválido");
            return;
        }
        if (!celularValue.isEmpty() && !isValidPhone(celularValue)) {
            Components.ShowAlertError("Telefone inválido (informe DDD + Número)");
            return;
        }

        if (editMode.get()) {
            if (clienteSelecionado.get() == null) return;
            asyncUpdate(clienteSelecionado.get().id, nomeValue, cnpjCpfValue, celularValue, emailValue);
        } else {
            asyncSalvar(nomeValue, cnpjCpfValue, celularValue, emailValue);
        }
    }

    void clearForm() {
        nome.set("");
        cnpjCpf.set("");
        celular.set("");
        email.set("");
    }

    private void populateFromCliente() {
        final var data = clienteSelecionado.get();
        if (data == null) return;
        nome.set(data.nome);
        cnpjCpf.set(data.cpfCnpj);
        celular.set(data.celular);
        email.set(data.email);
        tipoPessoaSelected.set(
                Utils.isValidCpf(data.cpfCnpj)
                        ? tipoPessoaList.getFirst()
                        : tipoPessoaList.getLast()
        );
    }

    private void asyncUpdate(long id, String nome, String cnpjCpf, String celular, String email) {
        Async.Run(() -> {
            try {
                var model = new ClienteModel().fromIdAndDto(id, new ClienteDto(nome, cnpjCpf, celular, email));
                clienteRepository.atualizar(model);
                UI.runOnUi(() -> {
                    clientes.updateIf(it -> it.id.equals(id), it -> model);
                    Components.ShowPopup(ctx, "Cliente atualizado com sucesso");
                    clearForm();
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    private void asyncSalvar(String nome, String cnpjCpf, String celular, String email) {
        Async.Run(() -> {
            try {
                var model = clienteRepository.salvar(new ClienteDto(nome, cnpjCpf, celular, email));
                UI.runOnUi(() -> {
                    clientes.add(model);
                    Components.ShowPopup(ctx, "Cliente cadastrado com sucesso");
                    clearForm();
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }
}