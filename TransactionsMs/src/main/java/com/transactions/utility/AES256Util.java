package com.transactions.utility;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AES256Util {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    //8TQ1WYuSRfiuRjGWnjFQVNcsXA4UV+MDgQRi93xiZV4= fZaYtAkplFPx+K5tb7voG1bwZM6TqhuJ9rp0IuwiiMw=
    //private static final String SECRET_KEY = "cejXt5sR1ZUSck6AjxRwPvhhZdXkwXcv"; // 16, 24, or 32-byte key
    private static final String SECRET_KEY = "8TQ1WYuSRfiuRjGWnjFQVNcsXA4UV+MDgQRi93xiZV4="; // 16, 24, or 32-byte key

    // Generate AES-256 Key (Run this once and store securely)
    public static String generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(256, new SecureRandom()); 
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    // Encrypt data
    public static String encrypt(String data) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(SECRET_KEY);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));
        String encrypted = Base64.getUrlEncoder().encodeToString(encryptedBytes);
        
        // Remove trailing characters: =, -, _, +
        if (encrypted != null && encrypted.length() > 0) {
            while (encrypted.length() > 0) {
                char lastChar = encrypted.charAt(encrypted.length() - 1);
                if (lastChar == '=' || lastChar == '-' || lastChar == '_' || lastChar == '+') {
                    encrypted = encrypted.substring(0, encrypted.length() - 1);
                } else {
                    break;
                }
            }
        }
        
        return encrypted;
    }

    // Decrypt data
    public static String decrypt(String encryptedData) throws Exception {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return null;
        }

        try {
            byte[] keyBytes = Base64.getDecoder().decode(SECRET_KEY);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decryptedBytes = cipher.doFinal(Base64.getUrlDecoder().decode(encryptedData));
            String decrypted = new String(decryptedBytes, "UTF-8");
            
            // Remove trailing characters: =, -, _, +
            if (decrypted != null && decrypted.length() > 0) {
                while (decrypted.length() > 0) {
                    char lastChar = decrypted.charAt(decrypted.length() - 1);
                    if (lastChar == '=' || lastChar == '-' || lastChar == '_' || lastChar == '+') {
                        decrypted = decrypted.substring(0, decrypted.length() - 1);
                    } else {
                        break;
                    }
                }
            }
            
            return decrypted;
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid Base64 string: " + e.getMessage());
        }
    }
}
