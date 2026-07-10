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
import my_app.domain.states.EnderecoState;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.sql.SQLException;
import java.time.LocalDate;

public class ClienteViewModel extends ViewModelScreenContract {
    private final ClienteService clienteService;

    final ListState<ClienteModel> clientes = ListState.ofEmpty();
    final State<ClienteModel> clienteSelecionado = State.of(null);

    final State<String> nome = new State<>("");
    final State<String> cnpjCpf = new State<>("");
    final State<String> celular = new State<>("");
    final State<String> email = new State<>("");
    final State<String> observacao = new State<>("");
    public final State<LocalDate> dataNascimento = State.of(null);

    final State<String> tipoPessoaSelected = new State<>(Data.tiposPessoaList.getFirst());
    final State<String> isGestante = new State<>(Data.simNaoList.getLast());
    public final State<LocalDate> dataNascimentoBebe = State.of(null);

    final ComputedState<Boolean> tipoPessoaEhFisica = ComputedState.of(
            () -> tipoPessoaSelected.get().equals(Data.tiposPessoaList.getFirst()),
            tipoPessoaSelected
    );

    final ComputedState<Boolean> isGestanteComputed = ComputedState.of(
            () -> isGestante.get().equals(Data.simNaoList.getFirst()),
            isGestante
    );

    final State<EnderecoState> enderecoState = new State<>(new EnderecoState());

    public ClienteViewModel(ScreenContext ctx) {
        this(ctx, createClienteService());
    }

    public ClienteViewModel(ScreenContext ctx, ClienteService clienteService) {
        super(ctx);
        this.clienteService = clienteService;
        this.onInit();
    }

    private static ClienteService createClienteService() {
        try {
            return new ClienteService();
        } catch (SQLException e) {
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onInit() {
        tipoPessoaSelected.subscribe(_ -> cnpjCpf.set(""));
    }

    @Override
    public void populateFromModel() {
        final var data = clienteSelecionado.get();
        if (data == null) return;
        tipoPessoaSelected.set(
                Utils.isValidCpf(data.getCpfCnpj())
                        ? Data.tiposPessoaList.getFirst()
                        : Data.tiposPessoaList.getLast()
        );
        nome.set(data.getNome());
        cnpjCpf.set(data.getCpfCnpj());
        celular.set(data.getCelular());
        email.set(data.getEmail());
        observacao.set(data.getObservacao() == null? "": data.getObservacao());
        dataNascimento.set(data.getDataNascimento() != null?
                DateUtils.millisParaLocalDate(data.getDataNascimento()): null
                );

        final Boolean gestante = data.getGestante();

        if(gestante){
            isGestante.set(Data.simNaoList.getFirst());
        }else{
            isGestante.set(Data.simNaoList.getLast());
        }

        dataNascimentoBebe.set(data.getDataNascimentoBebe() != null?
                DateUtils.millisParaLocalDate(data.getDataNascimentoBebe()): null
        );

        enderecoState.get().populateFromClienteModel(data);
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
        model.setObservacao(observacao.get());

        boolean isGestanteValue = isGestante.get()!=null && isGestante.get().equals(Data.simNaoList.getFirst());
        model.setGestante(isGestanteValue);

        model.setDataNascimento(dataNascimento.get() != null ?
                DateUtils.localDateParaMillis(dataNascimento.get()) : null);

        model.setDataNascimentoBebe(dataNascimentoBebe.get() != null ?
                DateUtils.localDateParaMillis(dataNascimentoBebe.get()) : null);


        EnderecoState enderecoStateValue = enderecoState.get();
        model.setCep(enderecoStateValue.cep.get());
        model.setUf(enderecoStateValue.ufSelected.get());
        model.setCidade(enderecoStateValue.cidade.get());
        model.setBairro(enderecoStateValue.bairro.get());
        model.setRua(enderecoStateValue.rua.get());
        model.setNumero(enderecoStateValue.numero.get());

        return model;
    }

    void loadClientes() {
        Async.Run(() -> {
            try {
                var list = clienteService.listar();
                UI.runOnUi(() -> clientes.set(list));
            } catch (Exception e) {
                e.printStackTrace();
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar clientes: " + e.getMessage()));
            }
        });
    }

    @Override
    public void handleClickMenuDelete() {
        final var model = clienteSelecionado.get();
        if (model == null) return;

        Components.ShowAlertAdvice("Deseja excluir cliente " + model.getNome(), () -> Async.Run(() -> {
            try {
                clienteService.excluirById(model.getId());
                UI.runOnUi(() -> {
                    clientes.removeIf(it -> it.getId().equals(model.getId()));
                    Components.ShowPopup(ctx, "Cliente excluído com sucesso");
                    EventBus.getInstance().publish(EntityEvent.excluido(model.getId()));
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao tentar excluir: " + e.getMessage()));
            }
        }));
    }

    @Override
    public void handleAddOrUpdate() {
        Async.Run(() -> {
            try {
                if (modoEdicao.get()) {
                    if(clienteSelecionado.get() == null)return;
                    var model = getModelFromFields(clienteSelecionado.get());
                    clienteService.atualizar(model);
                    ClienteModel finalModel = new ClienteModel();
                    finalModel.setId(model.getId());
                    finalModel.setNome(model.getNome());
                    finalModel.setCpfCnpj(model.getCpfCnpj());
                    finalModel.setCelular(model.getCelular());
                    finalModel.setEmail(model.getEmail());
                    finalModel.setPessoaFisica(model.getPessoaFisica());
                    finalModel.setDataCriacao(model.getDataCriacao());
                    UI.runOnUi(() -> {
                        clientes.updateIf(it -> it.getId().equals(finalModel.getId()), it -> finalModel);
                        Components.ShowPopup(ctx, "Cliente atualizado com sucesso");
                        clearForm();
                        EventBus.getInstance().publish(EntityEvent.editado(finalModel));
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

    @Override
    public void onDestroy() throws Exception {
        this.clienteService.close();
    }
}