package my_app.screens.fornecedorScreen;

import megalodonte.ComputedState;
import megalodonte.v2.ListState;
import megalodonte.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.db.dto.FornecedorDto;
import my_app.db.models.FornecedorModel;
import my_app.db.repositories.FornecedorRepository;
import my_app.lifecycle.viewmodel.component.ViewModelv2;
import my_app.screens.components.Components;

import java.time.LocalDate;
import java.util.List;

import static my_app.utils.Utils.*;

public class FornecedorScreenViewModel extends ViewModelv2 {
    private final ScreenContext ctx;
    private final FornecedorRepository fornecedorRepository;

    public final ListState<FornecedorModel> fornecedores = ListState.ofEmpty();
    public final State<FornecedorModel> fornecedorSelected = new State<>(null);

    State<String> nome = State.of("");
    State<String> cnpj = State.of("");
    State<String> celular = State.of("");
    State<String> inscricaoEstadual = State.of("");
    State<String> email = State.of("");

    //endereço
    List<String> ufList = List.of(
            "AC-Acre", "AL-Alagoas", "AP-Amapá", "AM-Amazonas", "BA-Bahia", "CE-Ceará", "DF-Distrito Federal", "ES-Espírito Santo",
            "GO-Goiás", "MA-Maranhão", "MT-Mato Grosso", "MS-Mato Grosso do Sul", "MG-Minas Gerais", "PA-Pará", "PB-Paraíba", "PR-Paraná",
            "PE-Pernambuco", "PI-Piauí", "RJ-Rio de Janeiro", "RN-Rio Grande do Norte", "RS-Rio Grande do Sul", "RO-Rondônia", "RR-Roraima",
            "SC-Santa Catarina", "SP-São Paulo", "SE-Sergipe", "TO-Tocantins"
    );

    State<String> ufSelected = State.of(ufList.getFirst());
    State<String> cidade = State.of("");
    State<String> bairro = State.of("");
    State<String> rua = State.of("");
    State<String> numero = State.of("");

    State<String> observacao = State.of("");

    public final State<Boolean> modoEdicao = State.of(false);
    public final ComputedState<String> btnText = ComputedState.of(() -> modoEdicao.get() ? "Atualizar" : "+ Adicionar", modoEdicao);



    public FornecedorScreenViewModel(ScreenContext ctx) {
        this.ctx = ctx;
        this.fornecedorRepository = new FornecedorRepository();

        this.onInit();
    }

    @Override
    protected void onInit() {

    }


    public void loadFornecedores() {
        Async.Run(()->{
            try {
                fornecedores.clear();
                final var list = fornecedorRepository.listar();
                UI.runOnUi(()->  fornecedores.addAll(list));
            } catch (Exception e) {
                UI.runOnUi(()-> Components.ShowAlertError("Erro ao carregar fornecedores: " + e.getMessage()));
            }
        });
    }

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

        if (nomeValue.isEmpty()) {
            Components.ShowAlertError("Nome é obrigatório");
            return;
        }

        //TODO: DEVE TER O MESMO PROBLEMA QUE TINHA LÁ EM CLIENTE
        if (!cnpjValue.isEmpty() && !isValidCnpj(cnpjValue)) {
            Components.ShowAlertError("CNPJ inválido (deve conter 14 dígitos)");
            return;
        }

        // 3. Validação de E-mail (se preenchido)
        if (!emailValue.isEmpty() && !isValidEmail(emailValue)) {
            Components.ShowAlertError("Formato de e-mail inválido");
            return;
        }

        // 4. Validação de Telefone/Celular
        if (!celularValue.isEmpty() && !isValidPhone(celularValue)) {
            Components.ShowAlertError("Telefone inválido (informe DDD + Número)");
            return;
        }

        if(modoEdicao.get() && fornecedorSelected.get() == null) return;

        if(modoEdicao.get()){
            asyncAtualizar(nomeValue, cnpjValue, celularValue, emailValue, inscricaoValue, ufValue, cidadeValue, bairroValue, ruaValue, numeroValue, observacaoValue);
        }else{
            asyncSalvar(nomeValue, cnpjValue, celularValue, emailValue, inscricaoValue, ufValue, cidadeValue, bairroValue, ruaValue, numeroValue, observacaoValue);
        }
    }

    private void asyncAtualizar(String nomeValue, String cnpjValue, String celularValue, String emailValue, String inscricaoValue, String ufValue, String cidadeValue, String bairroValue, String ruaValue, String numeroValue, String observacaoValue) {
        Async.Run(()->{
            try {
                // 1. Criamos a Model com os novos dados mantendo o ID e Data de Criação originais
                FornecedorModel selecionado = fornecedorSelected.get();
                FornecedorModel modelAtualizada = (FornecedorModel) new FornecedorModel().fromIdAndDtoAndMillis(selecionado.id, new FornecedorDto(
                        nomeValue, cnpjValue, celularValue, emailValue,
                        inscricaoValue, ufValue, cidadeValue, bairroValue,
                        ruaValue, numeroValue, observacaoValue
                ), selecionado.dataCriacao);

                // 2. Atualiza no Banco de Dados
                fornecedorRepository.atualizar(modelAtualizada);

                UI.runOnUi(() -> {
                    fornecedores.updateIf(fornecedorModel -> fornecedorModel.id.equals(modelAtualizada.id),
                            fornecedorModel -> modelAtualizada);
                    Components.ShowPopup(ctx, "Fornecedor atualizado com sucesso");
                    clearForm();
                });

            } catch (Exception e) {
                UI.runOnUi(()-> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    private void asyncSalvar(String nomeValue, String cnpjValue, String celularValue, String emailValue, String inscricaoValue, String ufValue, String cidadeValue, String bairroValue, String ruaValue, String numeroValue, String observacaoValue) {
        Async.Run(()->{
            try {
                var dto = new FornecedorDto(
                        nomeValue,
                        cnpjValue,
                        celularValue,
                        emailValue,
                        inscricaoValue,
                        ufValue,
                        cidadeValue,
                        bairroValue,
                        ruaValue,
                        numeroValue,
                        observacaoValue
                );

                var model = fornecedorRepository.salvar(dto);

                UI.runOnUi(()-> {
                    fornecedores.add(model);
                    Components.ShowPopup(ctx, "Fornecedor cadastrado com sucesso");
                    clearForm();
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void handleClickMenuEdit() {
        final var forn = fornecedorSelected.get();
        if(forn != null){
            modoEdicao.set(true);
            nome.set(forn.nome);
            cnpj.set(forn.cpfCnpj);
            celular.set(forn.celular);
            inscricaoEstadual.set(forn.inscricaoEstadual);
            email.set(forn.email);
            ufSelected.set(forn.ufSelected);
            cidade.set(forn.cidade);
            bairro.set(forn.bairro);
            rua.set(forn.rua);
            numero.set(forn.numero);
            observacao.set(forn.observacao);
        }
    }

    public void handleClickMenuDelete() {
        modoEdicao.set(false);

        final var fornecedorModel = fornecedorSelected.get();
        if(fornecedorModel ==null) return;

        Components.ShowAlertAdvice("Deseja excluir fornecedor  " + fornecedorModel.nome, ()->{
                Async.Run(()->{
                    try{
                        fornecedorRepository.excluirById(fornecedorModel.id);
                        UI.runOnUi(()->{
                            fornecedores.removeIf(it-> it.id.equals(fornecedorModel.id));
                            Components.ShowPopup(ctx, "Fornecedor excluido com sucesso");
                        });
                    }catch (Exception e){
                        UI.runOnUi(()->Components.ShowAlertError("Erro ao tentar excluir: " + e.getMessage()));
                    }
                });
            });
    }

    public void handleClickMenuClone() {
        modoEdicao.set(false);

        final var data = fornecedorSelected.get();
        if(data != null){
            nome.set(data.nome);
            cnpj.set(data.cpfCnpj);
            celular.set(data.celular);
            inscricaoEstadual.set(data.inscricaoEstadual);
            email.set(data.email);
            ufSelected.set(data.ufSelected);
            cidade.set(data.cidade);
            bairro.set(data.bairro);
            rua.set(data.rua);
            numero.set(data.numero);
            observacao.set(data.observacao);
        }
    }

    public void handleClickNew() {
    }

    public void clearForm(){
        modoEdicao.set(false);
        nome.set("");
        cnpj.set("");
        celular.set("");
        inscricaoEstadual.set("");
        email.set("");
        ufSelected.set(ufList.getFirst());
        cidade.set("");
        bairro.set("");
        rua.set("");
        numero.set("");
        observacao.set("");
    }
}

