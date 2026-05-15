package my_app.hotreload;

import javafx.application.Platform;
import megalodonte.application.Context;
import megalodonte.base.components.ComponentInterface;
import megalodonte.base.components.ScreenComponent;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class Reloader {
    public void reload(Context context, String screenClassName, String classesPath) {
        Platform.runLater(() -> doReload(context, screenClassName, classesPath));
    }

    private void doReload(Context context, String screenClassName, String classesPath) {
        try {
            if (screenClassName == null) {
                System.err.println("[UIReloader] Screen class name is null.");
                return;
            }

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
                            // Fall through to parent
                        }
                    }
                    return super.loadClass(name);
                }
            };

            Class<?> screenClass = freshLoader.loadClass(screenClassName);
            ScreenComponent screenInstance = (ScreenComponent) screenClass.getDeclaredConstructor().newInstance();

            context.useView(screenInstance);

            System.out.println("[UIReloader] UI reloaded successfully.");
            freshLoader.close();

        } catch (Exception e) {
            System.err.println("[UIReloader] Error during UI reload process.");
            e.printStackTrace();
        }
    }
}
