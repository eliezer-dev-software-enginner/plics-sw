package my_app.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_FILE = "app.properties";
    private static final String USER_HOME_CONFIG = System.getProperty("user.home") + "/.erp-local/" + CONFIG_FILE;
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
        
        // 1. Tentar carregar do diretório do usuário (prioridade máxima)
        if (loadFromFile(USER_HOME_CONFIG)) {
            System.out.println("Configuração carregada de: " + USER_HOME_CONFIG);
            return;
        }
        
        // 2. Tentar carregar do diretório da aplicação
        if (loadFromFile(CONFIG_FILE)) {
            System.out.println("Configuração carregada de: " + CONFIG_FILE);
            return;
        }
        
        // 3. Carregar do classpath (fallback)
        if (loadFromClasspath()) {
            System.out.println("Configuração carregada do classpath");
            return;
        }
        
        System.out.println("AVISO: Arquivo de configuração não encontrado. Usando variáveis de ambiente ou valores padrão.");
    }

    private boolean loadFromFile(String filePath) {
        try (InputStream input = new FileInputStream(filePath)) {
            properties.load(input);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean loadFromClasspath() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
                return true;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public String getProperty(String key) {
        // 1. Verificar variável de ambiente (prioridade máxima)
        String envValue = System.getenv(key.replace('.', '_').toUpperCase());
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue;
        }
        
        // 2. Verificar propriedade do sistema (JVM args)
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.trim().isEmpty()) {
            return systemValue;
        }
        
        // 3. Retornar do arquivo de configuração
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
                # Este arquivo contém configurações sensíveis e não deve ser commitado no versionamento
                
                # Configurações do Telegram
                telegram.bot.token=SEU_BOT_TOKEN_AQUI
                telegram.chat.id=SEU_CHAT_ID_AQUI
                
                # Configurações de Database (se necessário)
                db.url=jdbc:sqlite:erp.db
                db.username=
                db.password=
                
                # Outras configurações
                app.name=ERP Local v2
                app.version=2.0.0
                """;
        
        try {
            // Criar diretório se não existir
            Path configDir = Paths.get(System.getProperty("user.home"), ".erp-local");
            Files.createDirectories(configDir);
            
            // Criar arquivo template
            Path configFile = configDir.resolve(CONFIG_FILE);
            if (!Files.exists(configFile)) {
                Files.write(configFile, template.getBytes());
                System.out.println("Arquivo de configuração template criado em: " + configFile);
                System.out.println("Por favor, edite o arquivo com suas configurações.");
            }
        } catch (IOException e) {
            System.err.println("Erro ao criar arquivo de configuração: " + e.getMessage());
        }
    }
}