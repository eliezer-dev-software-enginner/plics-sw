package my_app.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_FILE = "app.properties";
    private static final String CONFIG_PATH = System.getProperty("user.home") + "/.erp-local/" + CONFIG_FILE;
    private static ConfigManager instance;
    private Properties properties;

    private ConfigManager() {
        loadConfiguration();
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadConfiguration() {
        properties = new Properties();
        
        if (loadFromFile()) {
            System.out.println("Configuração carregada de: " + CONFIG_PATH);
        } else {
            System.out.println("AVISO: Arquivo .erp-local/app.properties não encontrado. Criando template...");
            createConfigTemplate();
        }
    }

    private boolean loadFromFile() {
        try (FileInputStream input = new FileInputStream(CONFIG_PATH)) {
            properties.load(input);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public String getProperty(String key) {
        String value = properties.getProperty(key);
        return value != null ? value.trim() : null;
    }

    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }

    public void createConfigTemplate() {
        String template = """
                # Configurações da Aplicação ERP Local v2
                # Arquivo local seguro - NÃO COMPARTILHAR
                
                # Configurações do Telegram
                telegram.bot.token=SEU_BOT_TOKEN_AQUI
                telegram.chat.id=SEU_CHAT_ID_AQUI
                """;
        
        try {
            Path configDir = Paths.get(System.getProperty("user.home"), ".erp-local");
            Files.createDirectories(configDir);
            
            Path configFile = configDir.resolve(CONFIG_FILE);
            if (!Files.exists(configFile)) {
                Files.write(configFile, template.getBytes());
                System.out.println("Arquivo de configuração criado em: " + configFile);
                System.out.println("Por favor, edite o arquivo com seus dados do Telegram.");
            }
        } catch (IOException e) {
            System.err.println("Erro ao criar arquivo de configuração: " + e.getMessage());
        }
    }
}