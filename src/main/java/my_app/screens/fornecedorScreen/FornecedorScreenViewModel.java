package my_app.screens.fornecedorScreen;

import megalodonte.v2.ListState;
import megalodonte.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.FornecedorModel;
import my_app.db.services.FornecedorService;
import my_app.domain.Data;
import my_app.lifecycle.viewmodel.component.ViewModelScreenContract;
import my_app.domain.components.Components;

import java.sql.SQLException;

public class FornecedorScreenViewModel extends ViewModelScreenContract {
    private final FornecedorService fornecedorService;

    public final ListState<FornecedorModel> fornecedores = ListState.ofEmpty();
    public final State<FornecedorModel> fornecedorSelected = new State<>(null);

    State<String> nome = State.of("");
    State<String> cnpj = State.of("");
    State<String> celular = State.of("");
    State<String> inscricaoEstadual = State.of("");
    State<String> email = State.of("");

    State<String> ufSelected = State.of(Data.ufList.getFirst());
    State<String> cidade = State.of("");
    State<String> bairro = State.of("");
    State<String> rua = State.of("");
    State<String> numero = State.of("");

    State<String> observacao = State.of("");

    public FornecedorScreenViewModel(ScreenContext ctx) {
        super(ctx);
        try {
            fornecedorService = new FornecedorService();
        } catch (SQLException e) {
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            throw new RuntimeException(e);
        }
        this.onInit();
    }

    @Override
    protected void onInit() {
    }

    @Override
    public void populateFromModel() {
        final var data = fornecedorSelected.get();
        if (data != null) {
            nome.set(data.getNome());
            cnpj.set(data.getCpfCnpj());
            celular.set(data.getCelular());
            inscricaoEstadual.set(data.getInscricaoEstadual());
            email.set(data.getEmail());
            ufSelected.set(data.getUfSelected());
            cidade.set(data.getCidade());
            bairro.set(data.getBairro());
            rua.set(data.getRua());
            numero.set(data.getNumero());
            observacao.set(data.getObservacao());
        }
    }

    public void loadFornecedores() {
        Async.Run(() -> {
            try {
                fornecedores.clear();
                final var list = fornecedorService.listar();
                UI.runOnUi(() -> fornecedores.addAll(list));
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao carregar fornecedores: " + e.getMessage()));
            }
        });
    }

    @Override
    public void handleAddOrUpdate() {
        String nomeValue = nome.getOrDefault("").trim();
        String cnpjValue = cnpj.getOrDefault("").trim();
        String celularValue = celular.getOrDefault("").trim();
        String emailValue = email.getOrDefault("").trim();
        String inscricaoValue = inscricaoEstadual.getOrDefault("").trim();
        String ufValue = ufSelected.getOrDefault("").trim();
        String cidadeValue = cidade.getOrDefault("").trim();
        String bairroValue = bairro.getOrDefault("").trim();
        String ruaValue = rua.getOrDefault("").trim();
        String numeroValue = numero.getOrDefault("").trim();
        String observacaoValue = observacao.getOrDefault("").trim();

        if (nomeValue.isEmpty()) throw new RuntimeException("Nome é obrigatório");

        if (modoEdicao.get() && fornecedorSelected.get() == null) return;

        if (modoEdicao.get()) {
            asyncAtualizar(nomeValue, cnpjValue, celularValue, emailValue, inscricaoValue, ufValue, cidadeValue, bairroValue, ruaValue, numeroValue, observacaoValue);
        } else {
            asyncSalvar(nomeValue, cnpjValue, celularValue, emailValue, inscricaoValue, ufValue, cidadeValue, bairroValue, ruaValue, numeroValue, observacaoValue);
        }
    }

    private void asyncAtualizar(String nomeValue, String cnpjValue, String celularValue, String emailValue, String inscricaoValue, String ufValue, String cidadeValue, String bairroValue, String ruaValue, String numeroValue, String observacaoValue) {
        Async.Run(() -> {
            try {
                FornecedorModel selecionado = fornecedorSelected.get();
                selecionado.setNome(nomeValue);
                selecionado.setCpfCnpj(cnpjValue);
                selecionado.setCelular(celularValue);
                selecionado.setEmail(emailValue);
                selecionado.setInscricaoEstadual(inscricaoValue);
                selecionado.setUfSelected(ufValue);
                selecionado.setCidade(cidadeValue);
                selecionado.setBairro(bairroValue);
                selecionado.setRua(ruaValue);
                selecionado.setNumero(numeroValue);
                selecionado.setObservacao(observacaoValue);

                fornecedorService.atualizar(selecionado);

                UI.runOnUi(() -> {
                    fornecedores.updateIf(f -> f.getId().equals(selecionado.getId()), f -> selecionado);
                    Components.ShowPopup(ctx, "Fornecedor atualizado com sucesso");
                    clearForm();
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    private void asyncSalvar(String nomeValue, String cnpjValue, String celularValue, String emailValue, String inscricaoValue, String ufValue, String cidadeValue, String bairroValue, String ruaValue, String numeroValue, String observacaoValue) {
        Async.Run(() -> {
            try {
                var model = new FornecedorModel();
                model.setNome(nomeValue);
                model.setCpfCnpj(cnpjValue);
                model.setCelular(celularValue);
                model.setEmail(emailValue);
                model.setInscricaoEstadual(inscricaoValue);
                model.setUfSelected(ufValue);
                model.setCidade(cidadeValue);
                model.setBairro(bairroValue);
                model.setRua(ruaValue);
                model.setNumero(numeroValue);
                model.setObservacao(observacaoValue);

                var salvo = fornecedorService.salvar(model);

                UI.runOnUi(() -> {
                    fornecedores.add(salvo);
                    Components.ShowPopup(ctx, "Fornecedor cadastrado com sucesso");
                    clearForm();
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    public void handleClickMenuDelete() {
        final var fornecedorModel = fornecedorSelected.get();
        if (fornecedorModel == null) return;

        Components.ShowAlertAdvice("Deseja excluir fornecedor  " + fornecedorModel.getNome(), () -> {
            Async.Run(() -> {
                try {
                    fornecedorService.excluirById(fornecedorModel.getId());
                    UI.runOnUi(() -> {
                        fornecedores.removeIf(it -> it.getId().equals(fornecedorModel.getId()));
                        Components.ShowPopup(ctx, "Fornecedor excluido com sucesso");
                    });
                } catch (Exception e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao tentar excluir: " + e.getMessage()));
                }
            });
        });
    }

    @Override
    public void clearForm() {
        modoEdicao.set(false);
        nome.set("");
        cnpj.set("");
        celular.set("");
        inscricaoEstadual.set("");
        email.set("");
        ufSelected.set(Data.ufList.getFirst());
        cidade.set("");
        bairro.set("");
        rua.set("");
        numero.set("");
        observacao.set("");
    }
}
