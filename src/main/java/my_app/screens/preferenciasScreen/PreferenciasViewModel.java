package my_app.screens.preferenciasScreen;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.ListState;
import my_app.Main;
import my_app.core.events.DadosFinanceirosAtualizadosEvent;
import my_app.core.events.EventBus;
import my_app.db.DB;
import my_app.db.models.PreferenciasModel;
import my_app.db.services.PreferenciasService;
import my_app.domain.components.Components;
import my_app.domain.ViewModelScreenContract;
import net.sf.persism.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class PreferenciasViewModel extends ViewModelScreenContract {
    private static final Logger log = LoggerFactory.getLogger(PreferenciasViewModel.class);

    private PreferenciasService preferenciasService;

    final State<String> habilitarCredenciaisSelected = State.of("Não");
    final State<String> loginState = State.of("");
    final State<String> passwordState = State.of("");
    final ListState<String> comportsState = new ListState<>(List.of("N/D"));
    final State<String> comportsStateSelected = State.of("N/D");

    private PreferenciasModel prefLoaded;

    public PreferenciasViewModel(ScreenContext ctx) {
        this(ctx, createPreferenciasService());
    }

    public PreferenciasViewModel(ScreenContext ctx, PreferenciasService preferenciasService) {
        super(ctx);
        this.preferenciasService = preferenciasService;
        this.onInit();
    }

    private static PreferenciasService createPreferenciasService() {
        try {
            return new PreferenciasService();
        } catch (SQLException e) {
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    void load() {
        Async.Run(() -> {
            try {
                SerialPort[] portas = SerialPort.getCommPorts();
                for (SerialPort p : portas) {
                    String name = p.getSystemPortName() + " - " + p.getDescriptivePortName();
                    UI.runOnUi(()->{
                        System.out.println(name);
                        comportsState.add(name);
                    });
                }

                var prefs = preferenciasService.listar();
                if (!prefs.isEmpty()) {
                    var pref = prefs.getFirst();
                    prefLoaded = pref;
                UI.runOnUi(() -> {
                        habilitarCredenciaisSelected.set(pref.getCredenciaisHabilitadas() == 1 ? "Sim" : "Não");
                        loginState.set(pref.getLogin());
                        passwordState.set(pref.getSenha());
                        var savedPort = pref.getPortaImpressora();
                        if (savedPort != null && !savedPort.isBlank()) {
                            comportsState.get().stream()
                                    .filter(name -> name.startsWith(savedPort))
                                    .findFirst()
                                    .ifPresent(name -> comportsStateSelected.set(name));
                        }
                    });
                }
            } catch (SQLException e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    void salvar() {
        var erro = validar();
        if (erro != null) {
            UI.runOnUi(() -> Components.ShowAlertError(erro));
            return;
        }

        var habilitar = habilitarCredenciaisSelected.get().equals("Sim");
        var login = loginState.get();
        var senha = passwordState.get();

        Async.Run(() -> {
            try {
                prefLoaded.setCredenciaisHabilitadas(habilitar ? 1 : 0);
                prefLoaded.setLogin(login);
                prefLoaded.setSenha(senha);
                savePrinterPort();
                preferenciasService.atualizar(prefLoaded);
                UI.runOnUi(() -> Components.ShowPopup(ctx, "Preferências salvas com sucesso!"));
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    private void savePrinterPort() {
        var selected = comportsStateSelected.get();
        if (selected != null && !selected.equals("N/D")) {
            var systemPortName = selected.split(" - ")[0];
            prefLoaded.setPortaImpressora(systemPortName);
        } else {
            prefLoaded.setPortaImpressora(null);
        }
    }

    String validar() {
        var habilitar = habilitarCredenciaisSelected.get().equals("Sim");
        if (!habilitar) return null;
        if (loginState.get().isBlank()) return "Login é obrigatório";
        if (passwordState.get().isBlank()) return "Senha é obrigatória";
        return null;
    }

    void deletarTodosDados() {
        Async.Run(() -> {
            try {
                preferenciasService.close();
                DB.limparBanco();

                try (var stmt = DB.production().connection().createStatement()) {
                    stmt.execute("INSERT INTO preferencias (tema, credenciais_habilitadas, primeiro_acesso, dataCriacao, login, senha) SELECT 'Claro', 0, 1, strftime('%s', 'now') * 1000, 'admin', '1234' WHERE NOT EXISTS (SELECT 1 FROM preferencias WHERE id = 1)");
                    stmt.execute("INSERT INTO categorias (nome, data_criacao) SELECT 'Geral', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE nome = 'Geral')");
                    stmt.execute("INSERT INTO fornecedores (nome, dataCriacao) SELECT 'Fornecedor Padrão', strftime('%s', 'now') * 1000 WHERE NOT EXISTS (SELECT 1 FROM fornecedores WHERE nome = 'Fornecedor Padrão')");
                    stmt.execute("INSERT INTO usuarios (nome, senha, cargo, dataCriacao) SELECT 'admin', '1234', 'admin', strftime('%s', 'now') * 1000 WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE nome = 'admin')");
                    stmt.execute("INSERT INTO empresas (texto_responsabilidade, dataCriacao) SELECT 'APÓS O VENCIMENTO COBRAR MULTA DE ATRASO 2,00\nNÃO RECEBER ATRASADO\nJUROS DE 0,01 AO DIA.', strftime('%s', 'now') * 1000 WHERE NOT EXISTS (SELECT 1 FROM empresas WHERE id = 1)");
                    stmt.execute("INSERT INTO clientes (nome, cpfCnpj, celular, email, dataCriacao,isPessoaFisica) SELECT 'CLIENTE PADRÃO', '', '', '', strftime('%s', 'now') * 1000, 1 WHERE NOT EXISTS (SELECT 1 FROM clientes WHERE id = 1)");
                }

                UI.runOnUi(() -> {
                    Components.ShowPopupForced(ctx,
                            "Todos os dados foram excluídos com sucesso!\n\nFeche o aplicativo e abra de novo para aplicar as mudanças.",
                            "Fechar aplicativo",
                            Main::handleClose
                    );
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao excluir dados: " + e.getMessage()));
            }
        });
    }

    @Override
    public void populateFromModel() {}

    @Override
    public void clearForm() {}

    @Override
    public void handleAddOrUpdate() {
        salvar();
    }

    @Override
    public void handleClickMenuDelete() {}
}
