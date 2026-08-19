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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;

public class ClienteViewModel extends ViewModelScreenContract<ClienteModel> {
    private static final Logger log = LoggerFactory.getLogger(ClienteViewModel.class);

    private final ClienteService clienteService;

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
        super(ctx);
        this.clienteService = createOrReport(ClienteService::new);
        tipoPessoaSelected.subscribe(_ -> cnpjCpf.set(""));
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
    public void populateFieldsFromModel() {
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

    @Override
    public ClienteModel populateModelFromFields() {
        var model = modoEdicao.get() && clienteSelecionado.get() != null
                ? clienteSelecionado.get()
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
        final var model = clienteSelecionado.get();
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
        if (modoEdicao.get() && clienteSelecionado.get() == null) return;

        // editando/model capturados aqui, síncronos (thread da UI) — não dentro do
        // Async.Run abaixo. ContratoTelaCrudV3.handleAddOrUpdate() chama
        // modoEdicaoState().set(false) logo depois de disparar essa chamada, então
        // ler modoEdicao.get() só depois de já estar rodando na thread virtual do
        // Async.Run quase sempre lê o valor já resetado — toda edição virava
        // cadastro novo. Mesmo bug já corrigido antes em CategoriaScreenViewModel/
        // VendaMercadoriaScreenViewModel; aqui ainda não tinha sido.
        boolean editando = modoEdicao.get();
        var model = populateModelFromFields();

        Async.Run(() -> {
            try {
                if (editando) {
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
    }
}