package my_app.infra;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ProcessKiller {

    private static final Path LOG_FILE = Path.of(System.getProperty("java.io.tmpdir"), "plics-killer.log");

    private static void log(String msg) {
        try {
            Files.writeString(LOG_FILE,
                    java.time.Instant.now() + " " + msg + "\n",
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {}
    }

    /**
     * Mata o processo do PID informado (e sua árvore de filhos), de forma
     * assíncrona e FORA da Job Object da aplicação atual, via Agendador de
     * Tarefas do Windows. A task é criada e disparada imediatamente via
     * schtasks /run, sem depender de horário agendado.
     */
    public static void killPidAsync(long pid) {
        try {
            var tempDir = Files.createTempDirectory("plics-kill-");
            var batchFile = tempDir.resolve("kill.bat");
            var vbsFile = tempDir.resolve("kill.vbs");

            String taskName = "PlicsKill_" + System.currentTimeMillis();

            String script = "@echo off\r\n" +
                    "set TASKNAME=" + taskName + "\r\n" +
                    "taskkill /F /T /PID " + pid + " 2>nul\r\n" +
                    "schtasks /delete /tn \"%TASKNAME%\" /f >nul 2>&1\r\n";
            Files.writeString(batchFile, script);

            String batEscaped = batchFile.toAbsolutePath().toString().replace("\"", "\"\"");
            String vbsScript =
                    "Set WshShell = CreateObject(\"WScript.Shell\")\r\n" +
                            "WshShell.Run \"\"\"" + batEscaped + "\"\"\", 0, False\r\n";
            Files.writeString(vbsFile, vbsScript);

            log("Script criado: " + batchFile);
            log("VBS wrapper criado: " + vbsFile);

            String startTime = LocalTime.now().plusMinutes(10)
                    .format(DateTimeFormatter.ofPattern("HH:mm"));

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

            log("Task executada via schtasks /run para PID " + pid);
        } catch (IOException e) {
            log("ERRO ao agendar kill do PID " + pid + ": " + e.getMessage());
        }
    }

    /** Mata o processo atual (self-kill), usando o mesmo mecanismo. */
    public static void killCurrentProcessAsync() {
        killPidAsync(ProcessHandle.current().pid());
    }

    private static void logProcessOutput(Process p, String label) {
        try {
            String output = new String(p.getInputStream().readAllBytes());
            p.waitFor();
            log(label + " exitCode=" + p.exitValue() + " output=" + output.trim());
        } catch (Exception e) {
            log(label + " erro ao ler output: " + e.getMessage());
        }
    }
}