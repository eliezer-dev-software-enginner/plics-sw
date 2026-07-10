package my_app.hotreload;

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
    private String implementationClassName;
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

    public HotReload implementationClassName(String implementationClassName) {
        this.implementationClassName = implementationClassName;
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

            System.out.println(
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
                    System.out.println("[HotReload] Resource Change detected: " + res);
                    Path targetCss = classesPath.resolve(res.getFileName());
                    try {
                        Files.copy(res, targetCss, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("[HotReload] Resource copied to target/classes.");
                        needsReload = true;
                    } catch (IOException e) {
                        System.err.println("[HotReload] Failed to copy Resource: " + e.getMessage());
                    }
                }

                // Process Java
                if (!javaCandidates.isEmpty()) {
                    System.out.println("[HotReload] Java Changes detected (" + javaCandidates.size() + " files).");
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
                                System.out.println(
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
                System.out.println("[HotReload] Watching directory: " + dir.getFileName());
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
                    System.out.println("[HotReload] Compiling file: " + fqcn); // NOVO LOG
                    files.add(p.toString());
                });

        System.out.println("[HotReload] Compiling " + files.size() + " files...");

        // argumentos do javac DEVEM ser separados
        List<String> args = new ArrayList<>();
        args.add("-d");
        args.add(classesPath.toString());

        String modulePath = getModulePath();
        if (modulePath != null) {
            args.add("--module-path");
            args.add(modulePath);
            args.add("--add-modules");
            args.add("javafx.controls,javafx.graphics,javafx.base");
        }

        args.addAll(files);

        int result = compiler.run(null, null, null,
                args.toArray(new String[0]));

        System.out.println("[HotReload] Compile status: " + (result == 0));
        return result == 0;
    }

    private void callReloadEntry() throws Exception {
        URL[] urls = new URL[] { classesPath.toUri().toURL() };

        // Passa as classes a serem excluídas para o ClassLoader
        ClassLoader cl = new HotReloadClassLoader(urls, ClassLoader.getSystemClassLoader(), classesToExclude);

        // Carrega a classe de recarga NO NOVO ClassLoader, usando o nome da classe
        // injetada
        Class<?> reloaderClass = cl.loadClass(implementationClassName);

        // Cria uma nova instância da classe de recarga
        var reloader = (Reloader) reloaderClass.getDeclaredConstructor().newInstance();

        System.out.println("[HotReload] Invoking new Reloader implementation: " + implementationClassName);

        // Usamos reflection para chamar o Platform.runLater do JavaFX para não depender
        // diretamente do módulo javafx.controls.
        Class<?> platformClass = Class.forName("javafx.application.Platform");
        Method runLaterMethod = platformClass.getMethod("runLater", Runnable.class);

        runLaterMethod.invoke(null, (Runnable) () -> {
            try {
                // Passa o contexto, o nome da screen class e o classesPath
                Method reloadWithScreen = reloaderClass.getMethod("reload", Context.class, String.class, String.class);
                reloadWithScreen.invoke(reloader, reloadContext, screenClassName, classesPath.toString());
                log.info("Reload finished.");
            } catch (Exception e) {
                log.error("Error during reload execution", e);
            }
        });
    }

    public void stop() {
        System.out.println("[HotReload Debug] HotReload was stopped");
        running = false;
    }

    private String getModulePath() {
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        List<String> paths = new ArrayList<>();

        System.out.println("[HotReload Debug] Runtime Arguments: " + arguments);

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
            System.out.println("[HotReload Debug] Module path NOT found in arguments.");
            return null;
        }

        String combinedPath = String.join(File.pathSeparator, paths);
        System.out.println("[HotReload Debug] Combined module path: " + combinedPath);
        return combinedPath;
    }
}