package my_app.hotreload;

import javafx.application.Platform;
import megalodonte.application.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HotReload {

    private static final Logger log = LoggerFactory.getLogger(HotReload.class);

    private Path sourcePath;
    private Path classesPath;
    private String screenClassName;
    private Context reloadContext;
    private Set<String> classesToExclude = new HashSet<>();

    private volatile boolean running = true;

    private Path resourcesPath;

    private static final long WATCHER_TIMEOUT_MS = 500;

    public HotReload() {}

    public HotReload sourcePath(String sourcePath) {
        this.sourcePath = Paths.get(sourcePath);
        return this;
    }

    public HotReload classesPath(String classesPath) {
        this.classesPath = Paths.get(classesPath);
        return this;
    }

    public HotReload resourcesPath(String resourcesPath) {
        this.resourcesPath = Paths.get(resourcesPath);
        return this;
    }

    public HotReload screenClassName(String screenClassName) {
        this.screenClassName = screenClassName;
        return this;
    }

    public HotReload reloadContext(Context reloadContext) {
        this.reloadContext = reloadContext;
        return this;
    }

    public HotReload classesToExclude(Set<String> classesToExclude) {
        this.classesToExclude = new HashSet<>(classesToExclude);
        this.classesToExclude.add(Reloader.class.getName());
        return this;
    }

    public void start() {
        if (classesToExclude.isEmpty()) {
            classesToExclude.add(Reloader.class.getName());
        }
        
        Thread t = new Thread(this::watchLoop, "HotReload-Watcher");
        t.setDaemon(true);
        t.start();
    }

    private void watchLoop() {
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {

            // 1. Registra o Source Path (código Java) RECURSIVAMENTE
            this.registerAll(ws, this.sourcePath);

            // 2. Registra o Resources Path (recursos)
            resourcesPath.register(ws, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);

            log.info(
                    "[HotReload] started, watching Java source: " + sourcePath + " and Resources: " + resourcesPath);

            while (running) {
                // Wait for the first event
                WatchKey firstKey = ws.take();

                // Debounce: allow burst of events to settle
                Thread.sleep(WATCHER_TIMEOUT_MS);

                Set<Path> javaCandidates = new HashSet<>();
                Set<Path> resourceCandidates = new HashSet<>();

                // Drain all keys that are potentially ready
                List<WatchKey> keysToReset = new ArrayList<>();
                keysToReset.add(firstKey);

                WatchKey otherKey;
                while ((otherKey = ws.poll()) != null) {
                    keysToReset.add(otherKey);
                }

                for (WatchKey key : keysToReset) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.OVERFLOW)
                            continue;

                        Path parent = (Path) key.watchable();
                        Path changedFile = parent.resolve((Path) event.context());

                        if (parent.startsWith(sourcePath) && changedFile.toString().endsWith(".java")) {
                            javaCandidates.add(changedFile);
                        } else if (parent.equals(resourcesPath)) {
                            // Ignora temporários
                            if (!changedFile.getFileName().toString().endsWith("~")) {
                                resourceCandidates.add(changedFile);
                            }
                        }
                    }
                    key.reset();
                }

                boolean needsCompile = false;
                boolean needsReload = false;

                // Process Resources
                for (Path res : resourceCandidates) {
                    log.info("[HotReload] Resource Change detected: " + res);
                    Path targetCss = classesPath.resolve(res.getFileName());
                    try {
                        Files.copy(res, targetCss, StandardCopyOption.REPLACE_EXISTING);
                        log.info("[HotReload] Resource copied to target/classes.");
                        needsReload = true;
                    } catch (IOException e) {
                        System.err.println("[HotReload] Failed to copy Resource: " + e.getMessage());
                    }
                }

                // Process Java
                if (!javaCandidates.isEmpty()) {
                    log.info("[HotReload] Java Changes detected (" + javaCandidates.size() + " files).");
                    needsCompile = true;
                }

                if (needsCompile) {
                    boolean compiledOk = compile();
                    if (compiledOk) {
                        // Check if we need to reload.
                        // Reload is needed unless ALL changed files are excluded.
                        boolean hasReloadableChanges = false;
                        for (Path p : javaCandidates) {
                            String fqcn = getFullyQualifiedClassName(p);
                            if (!this.classesToExclude.contains(fqcn)) {
                                hasReloadableChanges = true;
                                break;
                            } else {
                                log.info(
                                        "[HotReload] Change in excluded class (skipping reload trigger): " + fqcn);
                            }
                        }

                        if (hasReloadableChanges) {
                            needsReload = true;
                        }
                    }
                }

                if (needsReload) {
                    callReloadEntry();
                }
            }

        } catch (Exception e) {
            log.error("Erro no watch service", e);
        }
    }

    /**
     * Registra recursivamente todos os diretórios e subdiretórios sob o caminho
     * 'start' no WatchService.
     */
    private void registerAll(final WatchService ws, final Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                dir.register(ws, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
                log.info("[HotReload] Watching directory: {}", dir.getFileName());
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Converte o Path de um arquivo .java para seu nome de classe FQCN (ex:
     * src/main/java/my_app/App.java -> my_app.App).
     */
    private String getFullyQualifiedClassName(Path javaFilePath) {
        String relativePath = this.sourcePath.relativize(javaFilePath).toString();
        // Remove a extensão .java e substitui barras por pontos
        String className = relativePath.replace(".java", "").replace(this.sourcePath.getFileSystem().getSeparator(),
                ".");
        return className;
    }

    private boolean compile() throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.err.println("[HotReload] No Java compiler available.");
            return false;
        }

        // listar todos os arquivos .java
        List<String> files = new ArrayList<>();
        Files.walk(sourcePath)
                .filter(p -> p.toString().endsWith(".java"))
                .forEach(p -> {
                    String fqcn = this.getFullyQualifiedClassName(p);
                    log.info("[HotReload] Compiling file: {}", fqcn);
                    files.add(p.toString());
                });

        log.info("[HotReload] Compiling {} files...", files.size());

        // argumentos do javac DEVEM ser separados
        List<String> args = new ArrayList<>();
        args.add("-d");
        args.add(classesPath.toString());

        String runtimeClasspath = System.getProperty("java.class.path");
        if (runtimeClasspath != null && !runtimeClasspath.isBlank()) {
            args.add("-cp");
            args.add(runtimeClasspath);
            log.info("[HotReload] Classpath from runtime ({} entries)", runtimeClasspath.split(File.pathSeparator).length);
        }

        String lombokPath = findLombokJar();
        if (lombokPath != null) {
            args.add("-processorpath");
            args.add(lombokPath);
            log.info("[HotReload] Lombok annotation processor: {}", lombokPath);
        } else {
            log.warn("[HotReload] Lombok JAR not found — annotation processors will not run");
        }

        args.addAll(files);

        int result = compiler.run(null, null, null,
                args.toArray(new String[0]));

        log.info("[HotReload] Compile status: {}", result == 0);
        return result == 0;
    }

    private String findLombokJar() {
        // 1. Procura no classpath (quando roda via gradle run)
        String cp = System.getProperty("java.class.path");
        if (cp != null) {
            for (String entry : cp.split(File.pathSeparator)) {
                if (entry.toLowerCase().contains("lombok") && entry.endsWith(".jar")) {
                    log.info("[HotReload] Lombok found on classpath: {}", entry);
                    return entry;
                }
            }
        }

        // 2. Procura no module path
        String mp = getModulePath();
        if (mp != null) {
            for (String entry : mp.split(File.pathSeparator)) {
                if (entry.toLowerCase().contains("lombok") && entry.endsWith(".jar")) {
                    log.info("[HotReload] Lombok found on module path: {}", entry);
                    return entry;
                }
            }
        }

        // 3. Procura no Gradle cache (~/.gradle/caches/modules-2/files-2.1/org.projectlombok/lombok/)
        Path gradleCache = Path.of(System.getProperty("user.home"), ".gradle", "caches", "modules-2", "files-2.1");
        if (Files.isDirectory(gradleCache)) {
            try {
                Path lombokDir = gradleCache.resolve("org.projectlombok/lombok");
                log.info("[HotReload] Searching Lombok in Gradle cache: {}", lombokDir);
                if (Files.isDirectory(lombokDir)) {
                    try (var walk = Files.walk(lombokDir, 5)) {
                        var found = walk.filter(p -> p.toString().endsWith(".jar"))
                                .findFirst()
                                .map(Path::toString);
                        if (found.isPresent()) {
                            log.info("[HotReload] Lombok found in Gradle cache: {}", found.get());
                            return found.get();
                        }
                    }
                } else {
                    log.warn("[HotReload] Lombok dir not found: {}", lombokDir);
                }
            } catch (IOException e) {
                log.debug("[HotReload] Erro ao buscar Lombok no Gradle cache: {}", e.getMessage());
            }
        } else {
            log.warn("[HotReload] Gradle cache not found: {}", gradleCache);
        }

        log.warn("[HotReload] Lombok JAR not found anywhere");
        return null;
    }

    private void callReloadEntry() throws Exception {
        URL[] urls = new URL[] { classesPath.toUri().toURL() };

        // Passa as classes a serem excluídas para o ClassLoader
        ClassLoader cl = new HotReloadClassLoader(urls, ClassLoader.getSystemClassLoader(), classesToExclude);

        // Carrega a classe de recarga NO NOVO ClassLoader, usando o nome da classe
        // injetada
        Class<?> reloaderClass = cl.loadClass("my_app.hotreload.Reloader");

        // Cria uma nova instância da classe de recarga
        var reloader = (Reloader) reloaderClass.getDeclaredConstructor().newInstance();

        log.info("[HotReload] Invoking new Reloader implementation: {}", reloader);

        Platform.runLater(()->{
            try {
                // Passa o contexto, o nome da screen class e o classesPath
                reloader.reload(reloadContext, screenClassName, classesPath.toString());
                log.info("Reload finished.");
            } catch (Exception e) {
                log.error("Error during reload execution", e);
            }
        });
    }

    public void stop() {
        log.info("[HotReload Debug] HotReload was stopped");
        running = false;
    }

    private String getModulePath() {
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        List<String> paths = new ArrayList<>();

        log.info("[HotReload Debug] Runtime Arguments: {}", arguments);

        for (int i = 0; i < arguments.size(); i++) {
            String arg = arguments.get(i);
            if (arg.equals("--module-path") || arg.equals("-p")) {
                if (i + 1 < arguments.size()) {
                    paths.add(arguments.get(i + 1));
                    i++;
                }
            } else if (arg.startsWith("--module-path=")) {
                paths.add(arg.substring("--module-path=".length()));
            } else if (arg.startsWith("-p=")) {
                paths.add(arg.substring("-p=".length()));
            }
        }

        if (paths.isEmpty()) {
            log.info("[HotReload Debug] Module path NOT found in arguments.");
            return null;
        }

        String combinedPath = String.join(File.pathSeparator, paths);
        log.info("[HotReload Debug] Combined module path: " + combinedPath);
        return combinedPath;
    }
}