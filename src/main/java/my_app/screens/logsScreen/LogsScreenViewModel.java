package my_app.screens.logsScreen;

import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.base.state.State;
import my_app.domain.components.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Lê o log configurado em logback.xml (mesmo arquivo pra qualquer plataforma —
 * {@code ~/.plics-sw/logs/plics-sw.log}) pra exibir na LogsScreen. Só leitura: nunca escreve
 * nesse arquivo, quem faz isso é o próprio Logback em background.
 */
public class LogsScreenViewModel {
    private static final Logger log = LoggerFactory.getLogger(LogsScreenViewModel.class);

    // plics-sw.log gira em até 5MB (ver logback.xml) — com bastante uso vira dezenas de
    // milhares de linhas. Um javafx.scene.control.TextArea não é virtualizado (ao contrário
    // de TableView): carregar o arquivo inteiro nele deixa a tela extremamente pesada bem
    // antes de chegar no limite de 5MB. Mostra só a cauda mais recente; o arquivo completo
    // continua disponível via "Abrir pasta de logs".
    private static final int MAX_LINHAS = 500;

    final State<String> conteudoLogs = State.of("Carregando...");

    public LogsScreenViewModel() {
        carregarLogs();
    }

    private Path pastaDeLogs() {
        return Paths.get(System.getProperty("user.home"), ".plics-sw", "logs");
    }

    private Path arquivoLogPrincipal() {
        return pastaDeLogs().resolve("plics-sw.log");
    }

    public void carregarLogs() {
        conteudoLogs.set("Carregando...");
        Async.Run(() -> {
            try {
                var arquivo = arquivoLogPrincipal();
                if (!Files.exists(arquivo)) {
                    UI.runOnUi(() -> conteudoLogs.set("Nenhum log encontrado ainda em " + arquivo));
                    return;
                }

                var linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
                int total = linhas.size();
                var ultimas = total > MAX_LINHAS ? linhas.subList(total - MAX_LINHAS, total) : linhas;

                var texto = new StringBuilder();
                if (total > MAX_LINHAS) {
                    texto.append("(mostrando as últimas ").append(MAX_LINHAS).append(" de ").append(total)
                            .append(" linhas — arquivo completo em ").append(arquivo).append(")\n\n");
                }
                texto.append(String.join("\n", ultimas));

                var textoFinal = texto.toString();
                UI.runOnUi(() -> conteudoLogs.set(textoFinal.isBlank() ? "(log vazio)" : textoFinal));
            } catch (IOException e) {
                log.error("Erro ao ler arquivo de log", e);
                UI.runOnUi(() -> conteudoLogs.set("Erro ao ler o log: " + e.getMessage()));
            }
        });
    }

    /**
     * Desktop.open() é chamada bloqueante (espera o SO terminar de abrir o gerenciador de
     * arquivos) — chamada direto no clique do botão, ela trava a FX Application Thread até lá,
     * e a janela toda para de responder enquanto isso. Precisa rodar fora da FX thread.
     */
    public void abrirPastaDeLogs() {
        var pasta = pastaDeLogs();
        if (!Files.exists(pasta)) {
            Components.ShowAlertError("A pasta de logs ainda não existe: " + pasta);
            return;
        }

        Async.Run(() -> {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(pasta.toFile());
                } else {
                    UI.runOnUi(() -> Components.ShowAlertError("Não foi possível abrir a pasta automaticamente. Caminho: " + pasta));
                }
            } catch (Exception e) {
                log.error("Erro ao abrir pasta de logs", e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao abrir a pasta de logs: " + e.getMessage()));
            }
        });
    }
}
