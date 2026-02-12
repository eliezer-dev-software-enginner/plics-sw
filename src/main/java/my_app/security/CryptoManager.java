package my_app.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoManager {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private SecretKey secretKey;

    public CryptoManager() {
        this.secretKey = generateKey();
    }

    private SecretKey generateKey() {
        try {
            // Gera chave persistente baseada na máquina
            String machineId = System.getProperty("user.name") + 
                             System.getProperty("os.name") + 
                             System.getProperty("os.arch");
            
            SecureRandom random = new SecureRandom(machineId.getBytes(StandardCharsets.UTF_8));
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256, random);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar chave de criptografia", e);
        }
    }

    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar texto", e);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar texto", e);
        }
    }
}