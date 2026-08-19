package my_app.infra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ProcessKiller {

    private static final Logger log = LoggerFactory.getLogger(ProcessKiller.class);

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
            log.info("Task agendada criada (auto-delete): {}", taskName);

            var run = new ProcessBuilder("schtasks", "/run", "/tn", taskName)
                    .redirectErrorStream(true).start();
            logProcessOutput(run, "schtasks /run");
            log.info("Task executada via schtasks /run para PID {}", pid);
        } catch (IOException e) {
            log.error("Erro ao agendar kill do PID {}", pid, e);
        }
    }

    public static void killCurrentProcessAsync() {
        killPidAsync(ProcessHandle.current().pid());
    }

    private static void logProcessOutput(Process p, String label) {
        try {
            String output = new String(p.getInputStream().readAllBytes());
            p.waitFor();
            log.info("{} exitCode={} output={}", label, p.exitValue(), output.trim());
        } catch (Exception e) {
            log.warn("{} erro ao ler output", label, e);
        }
    }
}