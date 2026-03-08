package my_app.screens;

import megalodonte.*;
import megalodonte.base.async.Async;
import megalodonte.base.UI;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.Router;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import megalodonte.utils.related.TextVariant;
import my_app.db.dto.FornecedorDto;
import my_app.db.models.FornecedorModel;
import my_app.db.repositories.FornecedorRepository;
import my_app.domain.ContratoTelaCrud;
import my_app.screens.components.Components;
import my_app.utils.DateUtils;

import java.util.List;

import static my_app.utils.Utils.*;

public class FornecedorScreen implements ScreenComponent, ContratoTelaCrud {
    private final Router router;
    private final FornecedorRepository fornecedorRepository;

    private final ListState<FornecedorModel> fornecedores = ListState.of(List.of());

    State<Boolean> editMode = State.of(false);
    ComputedState<String> btnText = ComputedState.of(()-> editMode.get()? "Atualizar" : "Adicionar", editMode);

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

    State<FornecedorModel> fornecedorSelected = State.of(null);

    public FornecedorScreen(Router router) {
        this.router = router;
        fornecedorRepository = new FornecedorRepository();
    }

    public void onMount(){
        loadFornecedores();
    }


    private void loadFornecedores() {
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

    public Component render() {
        return mainView();
    }


    @Override
    public Component form() {
        return new Card(
                new Column(new ColumnProps().paddingAll(20))
                        .c_child(new Row(new RowProps().centerHorizontally())
                                .r_child(new Text("Cadastro de Fornecedor", (TextProps) new TextProps().variant(TextVariant.SUBTITLE).bold())))
                        .c_child(new SpacerVertical(20))
                        .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                                .r_child(Components.InputColumn("Nome Fantasia", nome,"Ex: Empresa 123"))
                                .r_child(Components.InputColumnNumeric("CNPJ", cnpj, ""))
                                .r_child(Components.InputColumnPhone("Celular", celular))
                                .r_child(Components.InputColumn("Inscrição estadual", inscricaoEstadual, ""))
                        )
                        .c_child(Components.InputColumn("Email", email, "Ex: email@teste.com"))
                        .c_child(new SpacerVertical(20))
                        .c_child(new Column().c_child(Components.FormTitle("Endereço")))
                        .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                                .r_child(Components.InputColumn("Cidade", cidade,""))
                                .r_child(Components.InputColumn("Bairro", bairro, ""))
                                .r_child(Components.InputColumn("Rua", rua, ""))
                                .r_child(Components.InputColumnNumeric("Número", numero, ""))
                        )
                        .c_child(Components.SelectColumn("UF", ufList, ufSelected, it->it))
                        .c_child(new SpacerVertical(20))
                        .c_child(new LineHorizontal())
                        .c_child(Components.TextAreaColumn("Observação", observacao,""))
                        .c_child(new SpacerVertical(20))
                        .c_child(Components.actionButtons(btnText, this::handleAddOrUpdate, this::clearForm)));
    }

    @Override
    public void handleClickNew() {
        editMode.set(false);
        clearForm();
    }

    @Override
    public void handleClickMenuEdit() {
        final var forn = fornecedorSelected.get();
        if(forn != null){
            editMode.set(true);
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

    @Override
    public void handleClickMenuDelete() {
        final var forn = fornecedorSelected.get();
        if(forn != null){
            editMode.set(false);

            Components.ShowAlertAdvice("Deseja excluir fornecedor  " + forn.nome, ()->{
                Async.Run(()->{
                    try{
                        fornecedorRepository.excluirById(forn.id);
                        UI.runOnUi(()->{
                            fornecedores.removeIf(it-> it.id.equals(forn.id));
                            Components.ShowPopup(router, "Fornecedor excluido com sucesso");
                        });
                    }catch (Exception e){
                        UI.runOnUi(()->Components.ShowAlertError("Erro ao tentar excluir: " + e.getMessage()));
                    }
                });
            });
        }
    }

    @Override
    public void handleClickMenuClone() {
        editMode.set(false);

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

    @Override
    public void clearForm(){
        editMode.set(false);
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

        if (nomeValue.isEmpty()) {
            Components.ShowAlertError("Nome é obrigatório");
            return;
        }

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

        if(editMode.get() && fornecedorSelected.get() == null) return;

        if(editMode.get()){
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
                        Components.ShowPopup(router, "Fornecedor atualizado com sucesso");
                        clearForm();
                    });

                } catch (Exception e) {
                    UI.runOnUi(()-> Components.ShowAlertError(e.getMessage()));
                }
            });
        }else{
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
                        Components.ShowPopup(router, "Fornecedor cadastrado com sucesso");
                        clearForm();
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Override
    public Component table() {
        return new SimpleTable<FornecedorModel>()
                .fromData(fornecedores)
                .header().columns()
                    .column("ID", it-> it.id)
                    .column("Nome", it->it.nome)
                    .column("Telefone", it->it.celular)
                    .column("CNPJ", it->it.cpfCnpj)
                    .column("Email", it->it.email)
                    .column("Data de Criação", it->DateUtils.millisToBrazilianDateTime(it.dataCriacao))
                    .end()
                .build()
                .onItemSelectChange(fornecedorSelected::set)
                .onItemDoubleClick(it-> {
                    Components.ShowModal( ItemDetails(it), router, 550);
                });
    }

    Component ItemDetails(FornecedorModel model){
        return new Column(new ColumnProps().paddingAll(20))
                .c_child(new Text("Detalhes do fornecedor", new TextProps().variant(TextVariant.SUBTITLE)))
                .c_child(new SpacerVertical(20))
                .c_child(Components.TextWithDetails("ID: ", model.id))
                .c_child(Components.TextWithDetails("Nome: ", model.nome))
                .c_child(Components.TextWithDetails("CNPJ: ", model.cpfCnpj))
                .c_child(Components.TextWithDetails("Telefone: ", model.celular))
                .c_child(Components.TextWithDetails("Inscrição estadual: ", model.inscricaoEstadual))
                .c_child(Components.TextWithDetails("Email: ", model.email))
                .c_child(Components.TextWithDetails("UF: ", model.ufSelected))
                .c_child(Components.TextWithDetails("Cidade: ", model.cidade))
                .c_child(Components.TextWithDetails("Bairro: ", model.bairro))
                .c_child(Components.TextWithDetails("Rua: ", model.rua))
                .c_child(Components.TextWithDetails("Número: ", model.numero))
                .c_child(Components.TextWithDetails("Data de criação: ", DateUtils.millisToBrazilianDateTime(model.dataCriacao)))
                .c_child(Components.TextWithDetails("Observação: ", model.observacao,true));
    }
}