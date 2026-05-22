package my_app.screens.clienteScreen;

import megalodonte.ComputedState;
import megalodonte.v2.ListState;
import megalodonte.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.db.dto.ClienteDto;
import my_app.db.models.ClienteModel;
import my_app.db.models.FornecedorModel;
import my_app.db.repositories.ClienteRepository;
import my_app.domain.Data;
import my_app.events.ClienteEvents;
import my_app.events.EventBus;
import my_app.lifecycle.viewmodel.component.ViewModelScreenContract;
import my_app.screens.components.Components;
import my_app.utils.Utils;

import java.util.List;

import static my_app.utils.Utils.*;

public class ClienteViewModel extends ViewModelScreenContract {
    private final ClienteRepository clienteRepository = new ClienteRepository();

    final ListState<ClienteModel> clientes = ListState.of(List.of());
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
        nome.set(data.nome);
        cnpjCpf.set(data.cpfCnpj);
        celular.set(data.celular);
        email.set(data.email);
        tipoPessoaSelected.set(
                Utils.isValidCpf(data.cpfCnpj)
                        ? Data.tiposPessoaList.getFirst()
                        : Data.tiposPessoaList.getLast()
        );
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

    @Override
    public void handleClickMenuDelete() {
        final var cliente = clienteSelecionado.get();
        if (cliente == null) return;

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

    @Override
    public void handleAddOrUpdate() {
        String nomeValue    = nome.get().trim();
        String cnpjCpfValue = cnpjCpf.get().trim();
        String celularValue = celular.get().trim();
        String emailValue   = email.get().trim();

        if (nomeValue.isEmpty()) {
            throw new RuntimeException("Nome é obrigatório");
        }

        if (!cnpjCpfValue.isEmpty()) {
            if (tipoPessoaEhFisica.get() && !isValidCpf(cnpjCpfValue)) {
                throw new RuntimeException("CPF inválido (deve conter 11 dígitos)");
            } else if (!tipoPessoaEhFisica.get() && !isValidCnpj(cnpjCpfValue)) {
                throw new RuntimeException("CNPJ inválido (deve conter 14 dígitos)");
            }
        }

        if (!emailValue.isEmpty() && !isValidEmail(emailValue)) {
            throw new RuntimeException("Formato de e-mail inválido");
        }
        if (!celularValue.isEmpty() && !isValidPhone(celularValue)) {
            throw new RuntimeException("Telefone inválido (informe DDD + Número)");
        }

        if (modoEdicao.get()) {
            if (clienteSelecionado.get() == null) return;
            asyncUpdate(clienteSelecionado.get().id, nomeValue, cnpjCpfValue, celularValue, emailValue);
        } else {
            asyncSalvar(nomeValue, cnpjCpfValue, celularValue, emailValue);
        }
    }

    @Override
    public void clearForm() {
        nome.set("");
        cnpjCpf.set("");
        celular.set("");
        email.set("");
    }

    private void asyncUpdate(long id, String nome, String cnpjCpf, String celular, String email) {
        Async.Run(() -> {
            try {
                var model = new ClienteModel().fromIdAndDto(id, new ClienteDto(nome, cnpjCpf, celular, email));
                clienteRepository.atualizar((ClienteModel) model);
                UI.runOnUi(() -> {
                    clientes.updateIf(it -> it.id.equals(id), it -> (ClienteModel) model);
                    Components.ShowPopup(ctx, "Cliente atualizado com sucesso");
                    clearForm();
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void asyncSalvar(String nome, String cnpjCpf, String celular, String email) {
        //Pode cadastrar com cnpj/cpf vazio
        for (var model : clientes.get()) {
            if(!cnpjCpf.isEmpty()){
                if(cnpjCpf.equals(model.cpfCnpj.trim()))throw new RuntimeException("Já existe um cliente com este CNPJ/CPF");
            }
        }

        Async.Run(() -> {
            try {
                var model = clienteRepository.salvar(new ClienteDto(nome, cnpjCpf, celular, email));
                UI.runOnUi(() -> {
                    clientes.add(model);
                    Components.ShowPopup(ctx, "Cliente cadastrado com sucesso");
                    clearForm();
                    EventBus.getInstance().publish(new ClienteEvents.Criado(model));
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}