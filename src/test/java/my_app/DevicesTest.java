package my_app;

public class DevicesTest {
    static void main(String[] args) throws Exception {
        System.out.println("=== DIAGNÓSTICO JSSC ===");
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("Arch: " + System.getProperty("os.arch"));
        System.out.println("Java: " + System.getProperty("java.version"));
        System.out.println();

        try {
            System.out.println("JSSC ClassLoader: " + jssc.SerialPortList.class.getClassLoader());
            String[] ports = jssc.SerialPortList.getPortNames();
            System.out.println("Portas encontradas: " + java.util.Arrays.toString(ports));
            System.out.println("Total: " + ports.length);
        } catch (Throwable e) {
            System.out.println("ERRO: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }
}
