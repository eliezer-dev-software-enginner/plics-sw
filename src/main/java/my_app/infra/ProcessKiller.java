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
     * Tarefas do Windows. Sem scripts intermediários: a task chama
     * taskkill.exe diretamente e se autodeleta com /z após rodar.
     */
    public static void killPidAsync(long pid) {
        String taskName = "PlicsKill_" + System.currentTimeMillis();
        String startTime = LocalTime.now().plusMinutes(1)
                .format(DateTimeFormatter.ofPattern("HH:mm"));

        try {
            var create = new ProcessBuilder(
                    "schtasks", "/create", "/f", "/z",
                    "/sc", "once",
                    "/tn", taskName,
                    "/tr", "taskkill.exe /F /T /PID " + pid,
                    "/st", startTime)
                    .redirectErrorStream(true).start();
            logProcessOutput(create, "schtasks /create");
            log("Task agendada criada (auto-delete): " + taskName);

            var run = new ProcessBuilder("schtasks", "/run", "/tn", taskName)
                    .redirectErrorStream(true).start();
            logProcessOutput(run, "schtasks /run");
            log("Task executada via schtasks /run para PID " + pid);
        } catch (IOException e) {
            log("ERRO ao agendar kill do PID " + pid + ": " + e.getMessage());
        }
    }

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