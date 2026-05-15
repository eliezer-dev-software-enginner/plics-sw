package my_app.hotreload;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Set;

public class HotReloadClassLoader extends URLClassLoader {

    private final Set<String> classesToExclude;

    public HotReloadClassLoader(URL[] urls, ClassLoader parent, Set<String> classesToExclude) {
        super(urls, parent);
        this.classesToExclude = classesToExclude != null ? classesToExclude : Collections.emptySet();
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        System.out.println("[HotReloadClassLoader] loadClass called for: " + name + " (parent: " + getParent() + ")");
        
        // 1. Tenta encontrar a classe JÁ CARREGADA por ESTE ClassLoader
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            System.out.println("[HotReloadClassLoader] Found loaded class: " + name + " -> " + loadedClass);
            if (resolve) resolveClass(loadedClass);
            return loadedClass;
        }

        // 2. Regras de exclusão (Java, JavaFX, classes da lib)
        if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("javafx.") ||
                name.startsWith("megalodonte.") || classesToExclude.contains(name)) {
            System.out.println("[HotReloadClassLoader] Delegating to parent (excluded): " + name);
            return super.loadClass(name, resolve);
        }

        // 3. Se não está excluída, tentamos carregar a versão nova
        try {
            System.out.println("[HotReloadClassLoader] Loading NEW class from classesPath: " + name);
            Class<?> c = findClass(name);
            if (resolve) resolveClass(c);
            return c;
        } catch (ClassNotFoundException e) {
            System.out.println("[HotReloadClassLoader] Not found in classesPath, delegating to parent: " + name);
            return super.loadClass(name, resolve);
        }
    }
}