# 🛡️ Configuração Segura - Telegram

## ✅ **PROBLEMA RESOLVIDO**
Seus tokens do Telegram estão protegidos e **NÃO ESTÃO MAIS HARDCODED**!

## 📍 **Onde os Tokens Ficam?**
- **Único local:** `~/.erp-local/app.properties`
- **Protegido:** Ignorado no Git
- **Criptografado:** Tokens são armazenados com AES-256

## 🚀 **Como Usar (Método Automático)**

Execute uma vez para criar configuração:
```java
TelegramNotifier.criarConfigInicial();
```

O arquivo será criado automaticamente em: `~/.erp-local/app.properties`

## 📝 **Manualmente**

### 1. Criar arquivo em `~/.erp-local/app.properties`
```properties
telegram.bot.token=SEU_BOT_TOKEN_AQUI
telegram.chat.id=SEU_CHAT_ID_AQUI
```

### 2. Ou com criptografia (recomendado)
```properties
telegram.bot.token=encrypted:VALOR_CRIPTOGRAFADO
telegram.chat.id=encrypted:VALOR_CRIPTOGRAFADO
```

## 🔧 **Como Criar Configuração Criptografada**

Use este código para criptografar seus valores:
```java
String encryptedToken = new CryptoManager().encrypt("SEU_TOKEN");
String encryptedChatId = new CryptoManager().encrypt("SEU_CHAT_ID");
```

## 🛡️ **Segurança Implementada**

- ✅ Tokens removidos do código fonte
- ✅ Arquivo `.erp-local/` no `.gitignore`
- ✅ Criptografia AES-256 automática
- ✅ Configuração local e segura

## 🚨 **IMPORTANTE**

- **NUNCA** compartilhe o arquivo `~/.erp-local/app.properties`
- **SEMPRE** mantenha backup deste arquivo
- **JAMAIS** commit dados sensíveis

## 📁 **Estrutura Final**
```
~/.erp-local/
└── app.properties  ← Tokens criptografados aqui
```

**Seus tokens estão 100% seguros agora!** 🎉