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

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
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
            if (args.length < 3) {
                log("ERRO: args insuficientes (" + args.length + ")");
                UI.runOnUi(() -> updateStatus.set("Erro: args insuficientes"));
                return;
            }

            long pid = Long.parseLong(args[0]);
            String pkgPath = args[1];
            String exePath = args[2];
            log("Iniciando. PID=" + pid + " PKG=" + pkgPath + " EXE=" + exePath);

            UI.runOnUi(() -> updateStatus.set("Aguardando aplicação fechar..."));

            ProcessHandle.of(pid).ifPresentOrElse(
                    ph -> { log("Aguardando PID " + pid); ph.onExit().join(); log("PID " + pid + " fechou"); },
                    () -> log("PID " + pid + " nao encontrado, prosseguindo")
            );

            UI.runOnUi(() -> updateStatus.set("Instalando atualização..."));

            try {
                if (IS_WINDOWS) {
                    runWindowsUpdate(pkgPath, exePath);
                } else {
                    runLinuxUpdate(pkgPath);
                }
            } catch (IOException e) {
                log("ERRO: " + e.getMessage());
                UI.runOnUi(() -> updateStatus.set("Erro: " + e.getMessage()));
            }
        }).start();
    }

    private void runWindowsUpdate(String msiPath, String exePath) throws IOException {
        var tempDir = Files.createTempDirectory("plics-update-");
        var logFile = tempDir.resolve("msi-result.txt");
        var batchFile = tempDir.resolve("run-update.bat");
        var vbsFile = tempDir.resolve("run-update.vbs");

        var msiLogFile = tempDir.resolve("msi-install.log");
        String msiLog = msiLogFile.toAbsolutePath().toString().replace("\"", "\"\"");
        String resultLog = logFile.toAbsolutePath().toString().replace("\"", "\"\"");
        String msi = msiPath.replace("\"", "\"\"");
        String exe = exePath.replace("\"", "\"\"");

        String taskName = "PlicsUpdate_" + System.currentTimeMillis();

        String script = "@echo off\r\n" +
                "setlocal enabledelayedexpansion\r\n" +
                "set MSI=" + msi + "\r\n" +
                "set LOG=" + msiLog + "\r\n" +
                "set RESULT=" + resultLog + "\r\n" +
                "set TASKNAME=" + taskName + "\r\n" +
                "set EXE=" + exe + "\r\n" +
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
                "  start \"\" \"!EXE!\"\r\n" +
                ")\r\n" +
                "schtasks /delete /tn \"!TASKNAME!\" /f >nul 2>&1\r\n";

        Files.writeString(batchFile, script);

        String batEscaped = batchFile.toAbsolutePath().toString().replace("\"", "\"\"");
        String vbsScript =
                "Set WshShell = CreateObject(\"WScript.Shell\")\r\n" +
                        "WshShell.Run \"\"\"" + batEscaped + "\"\"\", 0, False\r\n";
        Files.writeString(vbsFile, vbsScript);

        log("Script criado: " + batchFile);
        log("VBS wrapper criado: " + vbsFile);

        String startTime = java.time.LocalTime.now().plusMinutes(10)
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

        var create = new ProcessBuilder("schtasks", "/create", "/f", "/sc", "once",
                "/tn", taskName,
                "/tr", "wscript.exe \"" + vbsFile.toAbsolutePath() + "\"",
                "/st", startTime)
                .redirectErrorStream(true).start();
        logProcessOutput(create, "schtasks /create");

        log("Task agendada criada: " + taskName);

        var run = new ProcessBuilder("schtasks", "/run", "/tn", taskName)
                .redirectErrorStream(true).start();
        logProcessOutput(run, "schtasks /run");

        log("Task executada via schtasks /run");
        System.exit(0);
    }

    private void logProcessOutput(Process p, String label) {
        try {
            String output = new String(p.getInputStream().readAllBytes());
            p.waitFor();
            log(label + " exitCode=" + p.exitValue() + " output=" + output.trim());
        } catch (Exception e) {
            log(label + " erro ao ler output: " + e.getMessage());
        }
    }

//    private void runWindowsUpdate(String msiPath) throws IOException {
//        var tempDir = Files.createTempDirectory("plics-update-");
//        var logFile = tempDir.resolve("msi-result.txt");
//        var batchFile = tempDir.resolve("run-update.bat");
//
//        var msiLogFile = tempDir.resolve("msi-install.log");
//        String msiLog = msiLogFile.toAbsolutePath().toString().replace("\"", "\"\"");
//        String resultLog = logFile.toAbsolutePath().toString().replace("\"", "\"\"");
//        String msi = msiPath.replace("\"", "\"\"");
//
//        String taskName = "PlicsUpdate_" + System.currentTimeMillis();
//
//        String script = "@echo off\r\n" +
//                "setlocal enabledelayedexpansion\r\n" +
//                "set MSI=" + msi + "\r\n" +
//                "set LOG=" + msiLog + "\r\n" +
//                "set RESULT=" + resultLog + "\r\n" +
//                "set TASKNAME=" + taskName + "\r\n" +
//                "set TRY=0\r\n" +
//                "taskkill /f /im java.exe /im javaw.exe /im \"Plics SW.exe\" 2>nul\r\n" +
//                "timeout /t 10 /nobreak >nul\r\n" +
//                ":retry\r\n" +
//                "msiexec /i \"!MSI!\" /quiet /log \"!LOG!\"\r\n" +
//                "set EC=!ERRORLEVEL!\r\n" +
//                "if !EC!==1603 (\r\n" +
//                "  set /a TRY=TRY+1\r\n" +
//                "  if !TRY! lss 3 (\r\n" +
//                "    timeout /t 10 /nobreak >nul\r\n" +
//                "    goto retry\r\n" +
//                "  )\r\n" +
//                ")\r\n" +
//                "echo ExitCode=!EC! > \"!RESULT!\"\r\n" +
//                "if !EC!==0 (\r\n" +
//                "  msg \"%USERNAME%\" \"Plics SW atualizado! Voce ja pode abri-lo.\"\r\n" +
//                ") else (\r\n" +
//                "  msg \"%USERNAME%\" \"Erro na instalacao (code !EC!). Verifique os logs.\"\r\n" +
//                ")\r\n" +
//                "schtasks /delete /tn \"!TASKNAME!\" /f >nul 2>&1\r\n";
//
//        Files.writeString(batchFile, script);
//        log("Script criado: " + batchFile);
//
//        // /st é só formalidade (obrigatório pro /sc once), horário distante o suficiente pra nunca coincidir
//        String startTime = java.time.LocalTime.now().plusMinutes(10)
//                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
//
//        var create = new ProcessBuilder("schtasks", "/create", "/f", "/sc", "once",
//                "/tn", taskName,
//                "/tr", batchFile.toAbsolutePath().toString(),
//                "/st", startTime)
//                .redirectErrorStream(true).start();
//        logProcessOutput(create, "schtasks /create");
//
//        log("Task agendada criada: " + taskName);
//
//        // Dispara a task AGORA, sem depender do horário
//        var run = new ProcessBuilder("schtasks", "/run", "/tn", taskName)
//                .redirectErrorStream(true).start();
//        logProcessOutput(run, "schtasks /run");
//
//        log("Task executada via schtasks /run");
//        System.exit(0);
//    }

    private void runLinuxUpdate(String debPath) throws IOException {
        var tempDir = Files.createTempDirectory("plics-update-");
        var resultFile = tempDir.resolve("result.txt");
        var installLog = tempDir.resolve("install.log");
        var scriptFile = tempDir.resolve("run-update.sh");

        String script = "#!/bin/sh\n" +
            "pkill -f \"Plics SW\" 2>/dev/null\n" +
            "sleep 10\n" +
            "PKG=\"" + debPath.replace("\"", "\\\"") + "\"\n" +
            "LOG=\"" + installLog.toAbsolutePath().toString() + "\"\n" +
            "RESULT=\"" + resultFile.toAbsolutePath().toString() + "\"\n" +
            "for i in 1 2 3; do\n" +
            "  pkexec env DISPLAY=\"$DISPLAY\" DBUS_SESSION_BUS_ADDRESS=\"$DBUS_SESSION_BUS_ADDRESS\" \\\n" +
            "    dpkg -i \"$PKG\" >\"$LOG\" 2>&1\n" +
            "  EC=$?\n" +
            "  if [ \"$EC\" -eq 0 ]; then break; fi\n" +
            "  sleep 10\n" +
            "done\n" +
            "echo \"ExitCode=$EC\" > \"$RESULT\"\n" +
            "if [ \"$EC\" -eq 0 ]; then\n" +
            "  notify-send \"Plics SW\" \"Atualizacao concluida!\"\n" +
            "else\n" +
            "  notify-send \"Plics SW\" \"Erro na instalacao (codigo $EC). Verifique os logs.\"\n" +
            "fi\n";

        Files.writeString(scriptFile, script);
        scriptFile.toFile().setExecutable(true);
        log("Script criado: " + scriptFile);
        new ProcessBuilder("bash", scriptFile.toAbsolutePath().toString()).start();
        log("dpkg iniciado via script shell");
        System.exit(0);
    }
}
