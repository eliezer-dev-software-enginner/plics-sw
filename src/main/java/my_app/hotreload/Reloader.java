package my_app.hotreload;

import javafx.application.Platform;
import megalodonte.application.Context;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class Reloader {
    public void reload(Context context, String screenClassName, String classesPath) {
        Platform.runLater(() -> doReload(context, screenClassName, classesPath));
    }

    private void doReload(Context context, String screenClassName, String classesPath) {
        try {
            if (classesPath == null) {
                classesPath = "build/classes/java/main";
            }

            URL classesUrl = new File(classesPath).toURI().toURL();
            ClassLoader parent = this.getClass().getClassLoader();

            URLClassLoader freshLoader = new URLClassLoader(new URL[]{classesUrl}, parent) {
                @Override
                public Class<?> loadClass(String name) throws ClassNotFoundException {
                    if (name.startsWith("my_app.")) {
                        try {
                            return findClass(name);
                        } catch (ClassNotFoundException e) {
                            // fall through to parent
                        }
                    }
                    return super.loadClass(name);
                }
            };

            // Recarrega Main e chama initialize() — isso reconstrói AppRoutes, Router e tudo
            Class<?> mainClass = freshLoader.loadClass("my_app.Main");
            Method initMethod = mainClass.getMethod("initialize", Context.class);
            initMethod.invoke(null, context);

            System.out.println("[UIReloader] UI reloaded via Main.initialize().");
            freshLoader.close();

        } catch (Exception e) {
            System.err.println("[UIReloader] Error during UI reload process.");
            e.printStackTrace();
        }
    }
}