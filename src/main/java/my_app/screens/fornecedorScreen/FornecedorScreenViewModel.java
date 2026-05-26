package my_app.screens.fornecedorScreen;

import megalodonte.v2.ListState;
import megalodonte.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.db.dto.FornecedorDto;
import my_app.db.models.FornecedorModel;
import my_app.db.repositories.FornecedorRepository;
import my_app.domain.Data;
import my_app.lifecycle.viewmodel.component.ViewModelScreenContract;
import my_app.domain.components.Components;

import static my_app.utils.Utils.*;

public class FornecedorScreenViewModel extends ViewModelScreenContract {
    private final FornecedorRepository fornecedorRepository;

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
        this.fornecedorRepository = new FornecedorRepository();

        this.onInit();
    }

    @Override
    protected void onInit() {

    }

    @Override
    public void populateFromModel() {
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

        //Pode cadastrar com cnpj/cpf vazio
        for (FornecedorModel fornecedorModel : fornecedores.get()) {
            if(!cnpjValue.isEmpty()){
                if(cnpjValue.equals(fornecedorModel.cpfCnpj.trim()))throw new RuntimeException("Já existe um fornecedor com este CNPJ/CPF");
            }
        }

        if (nomeValue.isEmpty()) throw new RuntimeException("Nome é obrigatório");

        //TODO: DEVE TER O MESMO PROBLEMA QUE TINHA LÁ EM CLIENTE
        if (!cnpjValue.isEmpty() && !isValidCnpj(cnpjValue)) throw new RuntimeException("CNPJ inválido (deve conter 14 dígitos)");

        // 3. Validação de E-mail (se preenchido)
        if (!emailValue.isEmpty() && !isValidEmail(emailValue)) throw new RuntimeException("Formato de e-mail inválido");

        if (!celularValue.isEmpty() && !isValidPhone(celularValue)) throw new RuntimeException("Telefone inválido (informe DDD + Número)");

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
                throw new RuntimeException(e);
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

    public void handleClickMenuDelete() {
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

    @Override
    public void clearForm(){
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

