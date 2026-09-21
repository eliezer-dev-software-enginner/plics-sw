package my_app.screens.clienteScreen;

import megalodonte.ComputedState;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.base.state.State;
import my_app.core.AppRoutes;
import my_app.core.Data;
import my_app.core.ViewModelScreenContract;
import my_app.core.components.Components;
import my_app.core.db.models.ClienteModel;
import my_app.core.db.services.ClienteService;
import my_app.core.events.EntityEvent;
import my_app.core.events.EventBus;
import my_app.core.states.EnderecoState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pack.utilities.DatePack;
import pack.utilities.ValidatorPack;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.function.Consumer;

public class ClienteViewModel extends ViewModelScreenContract<ClienteModel> {
    private static final Logger log = LoggerFactory.getLogger(ClienteViewModel.class);

    private final ClienteService clienteService;
    private final Consumer<Object> eventListener = this::onEntityEvent;

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

    public ClienteViewModel(ScreenContextInterface ctx) {
        super(ctx);
        screenNameSpawn = AppRoutes.Screens.ADD_OR_EDIT_CLIENTE.name();
        this.clienteService = createOrReport(ClienteService::new);
        tipoPessoaSelected.subscribe(_ -> cnpjCpf.set(""));
        EventBus.getInstance().subscribe(eventListener);
    }

    private void onEntityEvent(Object event) {
        if (event instanceof EntityEvent<?> ee && ee.entity() instanceof ClienteModel) {
            fetchListData();
        }
    }


    @Override
    protected boolean matchesSearch(ClienteModel model, String query) {
        return contains(model.getNome(), query)
                || contains(model.getCpfCnpj(), query)
                || contains(model.getEmail(), query)
                || contains(model.getCelular(), query);
    }

    private boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    @Override
    public void populateFieldsFromModel(ClienteModel model) {
        tipoPessoaSelected.set(
                ValidatorPack.isValidCpf(model.getCpfCnpj())
                        ? Data.tiposPessoaList.getFirst()
                        : Data.tiposPessoaList.getLast()
        );
        nome.set(model.getNome());
        cnpjCpf.set(model.getCpfCnpj());
        celular.set(model.getCelular());
        email.set(model.getEmail());
        observacao.set(model.getObservacao() == null? "": model.getObservacao());
        dataNascimento.set(model.getDataNascimento() != null?
                DatePack.millisParaLocalDate(model.getDataNascimento()): null
                );

        final Boolean gestante = model.getGestante();

        if(gestante){
            isGestante.set(Data.simNaoList.getFirst());
        }else{
            isGestante.set(Data.simNaoList.getLast());
        }

        dataNascimentoBebe.set(model.getDataNascimentoBebe() != null?
                DatePack.millisParaLocalDate(model.getDataNascimentoBebe()): null
        );

        enderecoState.get().populateFromClienteModel(model);
    }

    @Override
    public ClienteModel populateModelFromFields() {
        var model = isEditing && selected.get() != null
                ? selected.get()
                : new ClienteModel();

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
                DatePack.localDateParaMillis(dataNascimento.get()) : null);

        model.setDataNascimentoBebe(dataNascimentoBebe.get() != null ?
                DatePack.localDateParaMillis(dataNascimentoBebe.get()) : null);


        EnderecoState enderecoStateValue = enderecoState.get();
        model.setCep(enderecoStateValue.cep.get());
        model.setUf(enderecoStateValue.ufSelected.get());
        model.setCidade(enderecoStateValue.cidade.get());
        model.setBairro(enderecoStateValue.bairro.get());
        model.setRua(enderecoStateValue.rua.get());
        model.setNumero(enderecoStateValue.numero.get());

        return model;
    }

    @Override
    public void fetchListData(){
        Async.Run(() -> {
            try {
                var list = clienteService.listar();
                UI.runOnUi(() -> allDataList.set(list));
            } catch (Exception e) {
                log.error("Erro ao buscar clientes", e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar clientes: " + e.getMessage()));
            }
        });
    }

    @Override
    public void handleClickMenuDelete() {
        final var model = selected.get();
        if (model == null) return;

        Components.ShowAlertAdvice("Deseja excluir cliente " + model.getNome(), () -> Async.Run(() -> {
            try {
                clienteService.excluirById(model.getId());
                UI.runOnUi(() -> {
                    allDataList.removeIf(it -> it.getId().equals(model.getId()));
                    Components.ShowPopup(ctx, "Cliente excluído com sucesso");
                    EventBus.getInstance().publish(EntityEvent.excluido(model.getId()));
                });
            } catch (Exception e) {
                log.error("Erro ao excluir cliente id={}", model.getId(), e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao tentar excluir: " + e.getMessage()));
            }
        }));
    }

    @Override
    public void handleAddOrUpdate() {
        var model = populateModelFromFields();
        Async.Run(() -> {
            try {
                if (isEditing) {
                    clienteService.atualizar(model);
                    ClienteModel finalModel = new ClienteModel();
                    finalModel.setId(model.getId());
                    finalModel.setNome(model.getNome());
                    finalModel.setCpfCnpj(model.getCpfCnpj());
                    finalModel.setCelular(model.getCelular());
                    finalModel.setEmail(model.getEmail());
                    finalModel.setPessoaFisica(model.getPessoaFisica());
                    finalModel.setObservacao(model.getObservacao());
                    finalModel.setDataNascimento(model.getDataNascimento());
                    finalModel.setGestante(model.getGestante());
                    finalModel.setDataNascimentoBebe(model.getDataNascimentoBebe());
                    finalModel.setCep(model.getCep());
                    finalModel.setUf(model.getUf());
                    finalModel.setCidade(model.getCidade());
                    finalModel.setBairro(model.getBairro());
                    finalModel.setRua(model.getRua());
                    finalModel.setNumero(model.getNumero());
                    finalModel.setDataCriacao(model.getDataCriacao());
                    UI.runOnUi(() -> {
                        allDataList.updateIf(it -> it.getId().equals(finalModel.getId()), it -> finalModel);
                        Components.ShowPopup(ctx, "Cliente atualizado com sucesso");
                        clearForm();
                        EventBus.getInstance().publish(EntityEvent.editado(finalModel));
                        finishEditing();
                    });
                } else {
                    clienteService.salvar(model);
                    UI.runOnUi(() -> {
                        allDataList.add(model);
                        Components.ShowPopup(ctx, "Cliente cadastrado com sucesso");
                        clearForm();
                        EventBus.getInstance().publish(EntityEvent.criado(model));
                    });
                }
            } catch (IllegalArgumentException e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            } catch (Exception e) {
                log.error("Erro inesperado ao salvar cliente", e);
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
        observacao.set("");
        tipoPessoaSelected.set(Data.tiposPessoaList.getFirst());
        isGestante.set(Data.simNaoList.getLast());
        dataNascimento.set(null);
        dataNascimentoBebe.set(null);
        enderecoState.get().clear();
    }

    @Override
    public void onDestroy() throws Exception {
        this.clienteService.close();
        EventBus.getInstance().unsubscribe(eventListener);
    }

    @Override
    public ClienteModel findById(Long id) throws SQLException {
        return clienteService.buscarById(id);
    }
}