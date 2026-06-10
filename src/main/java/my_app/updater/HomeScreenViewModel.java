package my_app.updater;

import megalodonte.application.MegalodonteApp;
import megalodonte.base.UI;
import megalodonte.base.state.State;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class HomeScreenViewModel {
    State<String> updateStatus = new State("Iniciando...");

    private static final Path LOG_FILE = Path.of(
        System.getProperty("java.io.tmpdir"), "plics-updater.log"
    );

    private void log(String msg) {
        try {
            Files.writeString(LOG_FILE,
                java.time.Instant.now() + " " + msg + "\n",
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {}
    }

    public void update() {
        new Thread(() -> {
            String[] args = MegalodonteApp.getArgs();
            if (args.length < 2) {
                log("ERRO: args insuficientes (" + args.length + ")");
                UI.runOnUi(() -> updateStatus.set("Erro: args insuficientes"));
                return;
            }

            String pidStr = args[0];
            String msiPath = args[1];
            log("Iniciando. PID=" + pidStr + " MSI=" + msiPath);

            long pid = Long.parseLong(pidStr);
            UI.runOnUi(() -> updateStatus.set("Aguardando aplicação fechar..."));

            ProcessHandle.of(pid).ifPresentOrElse(
                ph -> { log("Aguardando PID " + pid); ph.onExit().join(); log("PID " + pid + " fechou"); },
                () -> log("PID " + pid + " nao encontrado, prosseguindo")
            );

            UI.runOnUi(() -> updateStatus.set("Instalando atualização..."));

            try {
                var tempDir = Files.createTempDirectory("plics-update-");
                var logFile = tempDir.resolve("msi-result.txt");
                var batchFile = tempDir.resolve("run-update.bat");

                var msiLogFile = tempDir.resolve("msi-install.log");
                String msiLog = msiLogFile.toAbsolutePath().toString().replace("\"", "\"\"");
                String resultLog = logFile.toAbsolutePath().toString().replace("\"", "\"\"");
                String msi = msiPath.replace("\"", "\"\"");
                String script = "@echo off\r\n" +
                    "setlocal enabledelayedexpansion\r\n" +
                    "set MSI=" + msi + "\r\n" +
                    "set LOG=" + msiLog + "\r\n" +
                    "set RESULT=" + resultLog + "\r\n" +
                    "set TRY=0\r\n" +
                    "taskkill /f /im java.exe /im javaw.exe /im \"Plics SW.exe\" 2>nul\r\n" +
                    "timeout /t 10 /nobreak >nul\r\n" +
                    ":retry\r\n" +
                    "msiexec /i \"!MSI!\" /quiet /log \"!LOG!\"\r\n" +
                    "set EC=!ERRORLEVEL!\r\n" +
                    "if !EC!==1603 (\r\n" +
                    "  set /a TRY=TRY+1\r\n" +
                    "  if !TRY! lss 3 (\r\n" +
                    "    timeout /t 10 /nobreak >nul\r\n" +
                    "    goto retry\r\n" +
                    "  )\r\n" +
                    ")\r\n" +
                    "echo ExitCode=!EC! > \"!RESULT!\"\r\n" +
                    "if !EC!==0 (\r\n" +
                    "  msg \"%USERNAME%\" \"Plics SW atualizado! Voce ja pode abri-lo.\"\r\n" +
                    ") else (\r\n" +
                    "  msg \"%USERNAME%\" \"Erro na instalacao (code !EC!). Verifique os logs.\"\r\n" +
                    ")\r\n";

                Files.writeString(batchFile, script);
                log("Script criado: " + batchFile);

                new ProcessBuilder("cmd", "/c", batchFile.toAbsolutePath().toString())
                    .start();
                log("msiexec iniciado via script batch");
                System.exit(0);
            } catch (IOException e) {
                log("ERRO ao iniciar msiexec: " + e.getMessage());
                UI.runOnUi(() -> updateStatus.set("Erro: " + e.getMessage()));
            }
        }).start();
    }
}
