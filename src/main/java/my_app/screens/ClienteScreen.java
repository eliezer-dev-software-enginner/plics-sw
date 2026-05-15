package my_app.screens;

import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Card;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Column;
import megalodonte.props.ColumnProps;
import megalodonte.router.v4.ScreenContext;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import megalodonte.v2.Show;
import my_app.db.dto.ClienteDto;
import my_app.db.models.ClienteModel;
import my_app.db.repositories.ClienteRepository;
import my_app.domain.ContratoTelaCrud;
import my_app.screens.components.Components;
//import javafx.scene.control.*;
import javafx.scene.control.*;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.util.List;

import static my_app.utils.Utils.*;

public class ClienteScreen implements ScreenComponent, ContratoTelaCrud {
    private final ScreenContext ctx;
    private final Theme theme = ThemeManager.theme();
    private final ClienteRepository clienteRepository = new ClienteRepository();
    
    ListState<ClienteModel> clientes = ListState.of(List.of());
    State<ClienteModel> clienteSelecionado = State.of(null);

    private final State<String> nome = new State<>("");
    //TODO: CONSIDERAR TER 2 CAMPOS NA UI, UM PARA CPF E OUTRO PARA CNPJ
    private final State<String> cnpjCpf = new State<>("");
    private final State<String> celular = new State<>("");
    private final State<String> email = new State<>("");

    final List<String> tipoPessoaList = List.of("Física", "Jurídica");
    State<String> tipoPessoaSelected = new State<>(tipoPessoaList.getFirst());

    ComputedState<Boolean> tipoPessoaEhFisica = ComputedState.of(()-> tipoPessoaSelected.get().equals(tipoPessoaList.getFirst()), tipoPessoaSelected);

    State<Boolean> editMode = State.of(false);

    ComputedState<String> btnText = ComputedState.of( ()-> editMode.get()? "Atualizar": "+ Adicionar", editMode);

    public ClienteScreen(ScreenContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onMount() {
        loadClientes();
        tipoPessoaSelected.subscribe(_->{
            cnpjCpf.set("");
        });
    }

    private void loadClientes() {
        Async.Run(()->{
            try {
                var list = clienteRepository.listar();
                UI.runOnUi(()-> {
                    clientes.addAll(list);
                });
            } catch (Exception e) {
                UI.runOnUi(()->  Components.ShowAlertError("Erro ao buscar clientes"));
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
                        .c_child(Components.FormTitle("Cadastrar cliente"))
                        .c_child(new SpacerVertical(20))
                        .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                                .r_child(Components.InputColumn("Nome", nome, "Ex: João"))
                                .r_child(Components.SelectColumn("Tipo de pessoa", tipoPessoaList, tipoPessoaSelected, it-> it))
                                .r_child(Show.when(tipoPessoaEhFisica,
                                        ()-> Components.InputColumnCpf("CPF", cnpjCpf),
                                        ()-> Components.InputColumnCnpj("CNPJ", cnpjCpf)
                                        ))
                                .r_child(Components.InputColumnPhone("Celular", celular))
                                .r_child(Components.InputColumn("Email", email,""))
                        )
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
        editMode.set(true);
        populateFromCliente();
    }

    private void populateFromCliente() {
        final var data = clienteSelecionado.get();
        if(data != null){
            nome.set(data.nome);
            cnpjCpf.set(data.cpfCnpj);
            celular.set(data.celular);
            email.set(data.email);

            if(Utils.isValidCpf(data.cpfCnpj)){
                tipoPessoaSelected.set(tipoPessoaList.getFirst());
            }else{
                tipoPessoaSelected.set(tipoPessoaList.getLast());
            }
        }
    }

    @Override
    public void handleClickMenuDelete() {
        final var forn = clienteSelecionado.get();
        if(forn != null){
            editMode.set(false);

            Components.ShowAlertAdvice("Deseja excluir cliente  " + forn.nome, ()->{
                Async.Run(()->{
                    try{
                        clienteRepository.excluirById(forn.id);
                        UI.runOnUi(()->{
                            clientes.removeIf(it-> it.id.equals(forn.id));
                            Components.ShowPopup(ctx, "Cliente excluido com sucesso");
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
        populateFromCliente();
    }

    @Override
    public void handleAddOrUpdate() {
        String nomeValue = nome.get().trim();
        String cnpjCpfValue = cnpjCpf.get().trim();
        String celularValue = celular.get().trim();
        String emailValue = email.get().trim();

        if (nomeValue.isEmpty()) {
            Components.ShowAlertError("Nome é obrigatório");
            return;
        }

        if(!cnpjCpfValue.isEmpty()){
            if(tipoPessoaEhFisica.get() && !isValidCpf(cnpjCpfValue)){
                Components.ShowAlertError("CPF inválido (deve conter 11 dígitos) e tem: " + cnpjCpfValue.length() + " dígitos");
                return;
            } else if (!tipoPessoaEhFisica.get() && !isValidCnpj(cnpjCpfValue)) {
                Components.ShowAlertError("CNPJ inválido (deve conter 14 dígitos) e tem: " + cnpjCpfValue.length() + " dígitos");
                return;
            }
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

        if (editMode.get()) {
            if (clienteSelecionado.get() == null) return;
            asyncUpdate(clienteSelecionado.get().id, nomeValue, cnpjCpfValue, celularValue, emailValue);
        } else {
            asyncSalvar(nomeValue, cnpjCpfValue, celularValue, emailValue);
        }
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

    @Override
    public void clearForm() {
        nome.set("");
        cnpjCpf.set("");
        celular.set("");
        email.set("");
    }

    @Override
    public Component  table() {
        var simpleTable = new SimpleTable<ClienteModel>();
        simpleTable.fromData(clientes)
                .header()
                .columns()
                    .column("ID", it-> it.id)
                    .column("Nome", it-> it.nome)
                    .column("CPF/CNPJ", it-> it.cpfCnpj.length() == 11? Utils.formatCpf(it.cpfCnpj): Utils.formatCnpj(it.cpfCnpj))
                    .column("Data de criação", it-> DateUtils.millisToBrazilianDateTime(it.dataCriacao))
                .build()
                .onItemSelectChange(it->   clienteSelecionado.set(it));

        return simpleTable;
    }
}