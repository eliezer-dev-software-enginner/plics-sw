package my_app.infra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class ProcessKiller {

    private static final Logger log = LoggerFactory.getLogger(ProcessKiller.class);

    /**
     * Mata o processo do PID informado (e sua árvore de filhos), de forma
     * assíncrona e FORA da Job Object da aplicação atual.
     * No Windows usa o Agendador de Tarefas (schtasks + taskkill.exe);
     * no Linux/Unix envia SIGKILL (kill -9).
     */
    public static void killPidAsync(long pid) {
        String os = System.getProperty("os.name", "").toLowerCase();

        if (os.contains("win")) {
            killPidWindows(pid);
        } else {
            killPidUnix(pid);
        }
    }

    private static void killPidWindows(long pid) {
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
            log.error("Erro ao agendar kill do PID {} via schtasks", pid, e);
        }
    }

    private static void killPidUnix(long pid) {
        try {
            var p = new ProcessBuilder("kill", "-9", String.valueOf(pid))
                    .redirectErrorStream(true).start();
            logProcessOutput(p, "kill -9");
            log.info("Sinal SIGKILL enviado para PID {}", pid);
        } catch (IOException e) {
            log.error("Erro ao enviar kill para PID {}", pid, e);
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

    /**
     * Remove diretórios temporários deixados pelo fluxo antigo de auto-atualização
     * (baixava o pacote em "plics-update-*" e lançava um updater externo, removido —
     * ver docs/DECISIONS.md) e por "plics-kill-*" (scripts de {@link #killPidAsync}).
     * Instalações antigas do app podem ter deixado esses diretórios pra trás, e nenhum
     * dos dois se autolimpava. Chamado no startup da aplicação principal: nesse
     * momento, qualquer diretório desses já é de uma sessão anterior, então é sempre
     * seguro apagar.
     */
    public static void cleanTempDirs() {
        cleanTempDirsWithPrefix("plics-update-");
        cleanTempDirsWithPrefix("plics-kill-");
    }

    private static void cleanTempDirsWithPrefix(String prefix) {
        try {
            var temp = Path.of(System.getProperty("java.io.tmpdir"));
            try (var dirs = Files.list(temp)) {
                dirs.filter(p -> p.getFileName().toString().startsWith(prefix))
                    .forEach(p -> {
                        log.info("Limpando diretório temporário de sessão anterior: {}", p);
                        try (var files = Files.walk(p)) {
                            files.sorted(Comparator.reverseOrder())
                                .forEach(f -> {
                                    try { Files.deleteIfExists(f); } catch (IOException e) {
                                        log.warn("Erro ao remover {}", f, e);
                                    }
                                });
                        } catch (IOException e) {
                            log.warn("Erro ao percorrer {}", p, e);
                        }
                    });
            }
        } catch (IOException e) {
            log.warn("Erro ao listar diretórios temporários com prefixo {}", prefix, e);
        }
    }
}