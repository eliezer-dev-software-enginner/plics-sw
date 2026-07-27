package my_app.screens.fornecedorScreen;

import megalodonte.ComputedState;
import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.FornecedorModel;
import my_app.db.services.FornecedorService;
import my_app.domain.Data;
import my_app.core.events.EntityEvent;
import my_app.core.events.EventBus;
import my_app.domain.ViewModelScreenContract;
import my_app.domain.components.Components;
import my_app.domain.states.EnderecoState;

import java.sql.SQLException;

public class FornecedorScreenViewModel extends ViewModelScreenContract<FornecedorModel> {
    private final FornecedorService fornecedorService;

    public final State<FornecedorModel> fornecedorSelected = new State<>(null);

    final State<String> nome = State.of("");
    final State<String> cnpjCpf = State.of("");
    final State<String> celular = State.of("");
    final State<String> inscricaoEstadual = State.of("");
    final State<String> email = State.of("");
    final State<String> observacao = State.of("");

    final State<EnderecoState> enderecoState = new State<>(new EnderecoState());

    final State<String> tipoPessoaSelected = new State<>(Data.tiposPessoaList.getFirst());
    final ComputedState<Boolean> tipoPessoaEhFisica = ComputedState.of(
            () -> tipoPessoaSelected.get().equals(Data.tiposPessoaList.getFirst()),
            tipoPessoaSelected
    );

    public FornecedorScreenViewModel(ScreenContext ctx) {
        super(ctx);
        this.fornecedorService = createOrReport(FornecedorService::new);
        this.onInit();
    }

    @Override
    protected boolean matchesSearch(FornecedorModel model, String query) {
        return contains(model.getNome(), query)
                || contains(model.getCpfCnpj(), query)
                || contains(model.getEmail(), query)
                || contains(model.getCelular(), query);
    }

    private boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    @Override
    public void fetchListData() {
        Async.Run(() -> {
            try {
                final var list = fornecedorService.listar();
                UI.runOnUi(() -> allDataList.set(list));
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao carregar fornecedores: " + e.getMessage()));
            }
        });
    }

    @Override
    public void populateFieldsFromModel() {
        final var data = fornecedorSelected.get();
        if (data != null) {
            nome.set(data.getNome());
            cnpjCpf.set(data.getCpfCnpj());
            celular.set(data.getCelular());
            inscricaoEstadual.set(data.getInscricaoEstadual());
            email.set(data.getEmail());
            tipoPessoaSelected.set(
                    Boolean.FALSE.equals(data.getPessoaFisica())
                            ? Data.tiposPessoaList.getLast()
                            : Data.tiposPessoaList.getFirst()
            );
            //
            enderecoState.get().populateFromFornecedorModel(data);
            observacao.set(data.getObservacao());
        }
    }

    @Override
    public FornecedorModel populateModelFromFields() {
        var model = modoEdicao.get() && fornecedorSelected.get() != null
                ? fornecedorSelected.get()
                : new FornecedorModel();

        model.setNome(nome.getOrDefault("").trim());
        model.setCpfCnpj(cnpjCpf.getOrDefault("").trim());
        model.setCelular(celular.getOrDefault("").trim());
        model.setEmail(email.getOrDefault("").trim());
        model.setInscricaoEstadual(inscricaoEstadual.getOrDefault("").trim());
        model.setPessoaFisica(tipoPessoaEhFisica.get());
        model.setUfSelected(enderecoState.get().ufSelected.getOrDefault("").trim());
        model.setCidade(enderecoState.get().cidade.getOrDefault("").trim());
        model.setBairro(enderecoState.get().bairro.getOrDefault("").trim());
        model.setRua(enderecoState.get().rua.getOrDefault("").trim());
        model.setNumero(enderecoState.get().numero.getOrDefault("").trim());
        model.setCep(enderecoState.get().cep.getOrDefault("").trim());
        model.setObservacao(observacao.getOrDefault("").trim());

        return model;
    }

    @Override
    public void handleAddOrUpdate() {
        if (modoEdicao.get() && fornecedorSelected.get() == null) return;

        var model = populateModelFromFields();

        if (modoEdicao.get()) {
            asyncAtualizar(model);
        } else {
            asyncSalvar(model);
        }
    }

    private void asyncAtualizar(FornecedorModel model) {
        Async.Run(() -> {
            try {
                fornecedorService.atualizar(model);

                FornecedorModel atualizado = new FornecedorModel();
                atualizado.setId(model.getId());
                atualizado.setNome(model.getNome());
                atualizado.setCpfCnpj(model.getCpfCnpj());
                atualizado.setCelular(model.getCelular());
                atualizado.setEmail(model.getEmail());
                atualizado.setInscricaoEstadual(model.getInscricaoEstadual());
                atualizado.setPessoaFisica(model.getPessoaFisica());
                atualizado.setUfSelected(model.getUfSelected());
                atualizado.setCidade(model.getCidade());
                atualizado.setBairro(model.getBairro());
                atualizado.setRua(model.getRua());
                atualizado.setNumero(model.getNumero());
                atualizado.setObservacao(model.getObservacao());
                atualizado.setDataCriacao(model.getDataCriacao());

                UI.runOnUi(() -> {
                    allDataList.updateIf(f -> f.getId().equals(atualizado.getId()), f -> atualizado);
                    Components.ShowPopup(ctx, "Fornecedor atualizado com sucesso");
                    clearForm();
                    EventBus.getInstance().publish(EntityEvent.editado(atualizado));
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    private void asyncSalvar(FornecedorModel model) {
        Async.Run(() -> {
            try {
                var salvo = fornecedorService.salvar(model);

                UI.runOnUi(() -> {
                    allDataList.add(salvo);
                    Components.ShowPopup(ctx, "Fornecedor cadastrado com sucesso");
                    clearForm();
                    EventBus.getInstance().publish(EntityEvent.criado(salvo));
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    public void handleClickMenuDelete() {
        final var fornecedorModel = fornecedorSelected.get();
        if (fornecedorModel == null) return;

        Components.ShowAlertAdvice("Deseja excluir fornecedor  " + fornecedorModel.getNome(), () -> Async.Run(() -> {
            try {
                fornecedorService.excluirById(fornecedorModel.getId());
                UI.runOnUi(() -> {
                    allDataList.removeIf(it -> it.getId().equals(fornecedorModel.getId()));
                    Components.ShowPopup(ctx, "Fornecedor excluido com sucesso");
                    EventBus.getInstance().publish(EntityEvent.excluido(fornecedorModel.getId()));
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao tentar excluir: " + e.getMessage()));
            }
        }));
    }

    @Override
    public void clearForm() {
        nome.set("");
        cnpjCpf.set("");
        celular.set("");
        inscricaoEstadual.set("");
        email.set("");
        enderecoState.get().clear();
        observacao.set("");
    }

    @Override
    public void onDestroy() throws Exception {
        this.fornecedorService.close();
    }
}
