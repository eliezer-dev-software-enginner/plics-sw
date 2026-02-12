# 🛡️ Sistema de Configuração Segura

Este documento explica como configurar e usar o sistema de configuração segura para proteger tokens e dados sensíveis.

## 🚨 **PROBLEMA RESOLVIDO**
Tokens NÃO ESTÃO MAIS HARDCODED no código! Eles agora são carregados de forma segura de múltiplas fontes.

## 📋 **Opções de Configuração (em ordem de prioridade)**

### 1️⃣ **Variáveis de Ambiente** (Máxima Prioridade)
```bash
# Windows
set TELEGRAM_BOT_TOKEN=seu_bot_token_aqui
set TELEGRAM_CHAT_ID=seu_chat_id_aqui

# Linux/Mac
export TELEGRAM_BOT_TOKEN=seu_bot_token_aqui
export TELEGRAM_CHAT_ID=seu_chat_id_aqui
```

### 2️⃣ **Propriedades do Sistema** (JVM Args)
```bash
java -Dtelegram.bot.token=seu_token -Dtelegram.chat.id=seu_chat_id -jar app.jar
```

### 3️⃣ **Arquivo de Configuração** (Local Seguro)
- **Localização:** `~/.erp-local/app.properties`
- **Prioridade:** Média
- **Segurança:** Pode conter valores criptografados

### 4️⃣ **Classpath** (Fallback)
- **Localização:** `src/main/resources/app.properties`
- **Uso:** Apenas para desenvolvimento

## 🔐 **Como Usar Criptografia**

### Gerar Configuração Segura Automaticamente
```java
// Execute este método uma vez para gerar configuração criptografada
TelegramNotifier.generateSecureConfig();
```

### Criptografar Valores Manualmente
```java
String encryptedToken = TelegramNotifier.encryptValue("seu_token_aqui");
String encryptedChatId = TelegramNotifier.encryptValue("seu_chat_id_aqui");
```

### Formato do Arquivo de Configuração
```properties
# Valores criptografados (recomendado)
telegram.bot.token=encrypted:AES_ENCRYPTED_VALUE
telegram.chat.id=encrypted:AES_ENCRYPTED_VALUE

# Ou valores em texto claro (menos seguro)
telegram.bot.token=seu_bot_token_aqui
telegram.chat.id=seu_chat_id_aqui

# Chave de criptografia opcional (gerada automaticamente se não informada)
app.encryption.key=BASE64_ENCODED_AES_KEY
```

## 🚀 **Modo de Uso Rápido**

### Para Desenvolvimento
```bash
# Execute o utilitário de migração
java -cp build/classes/java/main my_app.utils.SecurityMigrationUtil

# Ou use variáveis de ambiente
set TELEGRAM_BOT_TOKEN=8214368967:AAFN-Hq8bNU1pue0o4ysK_FsxQ5jde8mTXs
set TELEGRAM_CHAT_ID=-1002907413630
./gradlew run
```

### Para Produção
```bash
# Opção 1: Variáveis de ambiente (mais seguro)
export TELEGRAM_BOT_TOKEN=seu_token_producao
export TELEGRAM_CHAT_ID=seu_chat_id_producao
java -jar build/libs/erp-local-v2.jar

# Opção 2: Arquivo de configuração
# Crie ~/.erp-local/app.properties com valores criptografados
java -jar build/libs/erp-local-v2.jar

# Opção 3: Propriedades do sistema
java -Dtelegram.bot.token=seu_token -Dtelegram.chat.id=seu_chat_id -jar app.jar
```

## 🧪 **Testar Configuração**
```java
// Para testar se tudo está funcionando
my_app.utils.SecurityMigrationUtil.testConfiguration();
```

## 🔄 **Hierarquia de Carregamento**
1. **Variáveis de Ambiente** (ex: `TELEGRAM_BOT_TOKEN`)
2. **Propriedades do Sistema** (ex: `-Dtelegram.bot.token`)
3. **Arquivo em `~/.erp-local/app.properties`**
4. **Arquivo no diretório da aplicação `app.properties`**
5. **Arquivo no classpath `app.properties`**

## 🛡️ **Medidas de Segurança Implementadas**

### ✅ **Proteção contra Exposição**
- Tokens removidos do código fonte
- Arquivos sensíveis no `.gitignore`
- Suporte a criptografia AES-256

### ✅ **Flexibilidade de Deploy**
- Múltiplas fontes de configuração
- Suporte a ambientes diferentes (dev/staging/prod)
- Sem rebuild necessário para mudar configurações

### ✅ **Segurança em Produção**
- Chaves persistente baseada na máquina
- Criptografia automática de valores
- Fallback para variáveis de ambiente

## 📁 **Arquivos Ignorados no Git**
```
.erp-local/          # Diretório de configuração local
app.properties       # Arquivo de configuração
*.properties         # Todos os arquivos properties (exceto gradle.properties)
*.key                # Arquivos de chave
*.pem                # Certificados
```

## 🚨 **RECOMENDAÇÕES DE SEGURANÇA**

1. **NUNCA** commit arquivos de configuração com tokens
2. **SEMPRE** use variáveis de ambiente em produção
3. **CONSIDERE** criptografar tokens para máxima segurança
4. **FAÇA** backup dos arquivos de configuração
5. **NÃO** compartilhe chaves de criptografia

## 🔧 **Troubleshooting**

### Token não encontrado
```
Configurações do Telegram não encontradas. Configure 'telegram.bot.token' e 'telegram.chat.id'
```
**Solução:** Configure uma das fontes de configuração

### Erro de descriptografia
```
Erro ao descriptografar token: InvalidKeyException
```
**Solução:** Verifique se a chave de criptografia está correta ou gere nova configuração

### Permissão negada
```
Permission denied ao criar ~/.erp-local/
```
**Solução:** Verifique permissões do diretório home ou use outro local