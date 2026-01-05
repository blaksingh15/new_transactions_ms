package com.transactions.utility;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class AES256Api {

    // PHP-style base64url encode
    public static String base64UrlEncode(byte[] input) {
        return Base64.getEncoder().encodeToString(input)
                .replace('+', '-')
                .replace('/', '_')
                .replaceAll("=+$", "");  // remove padding
    }

    // PHP-style base64url decode
    public static byte[] base64UrlDecode(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input string cannot be null or empty");
        }
        
        // Replace URL-safe characters back to standard Base64
        String padded = input.replace('-', '+').replace('_', '/');
        
        // Add padding if needed
        int padding = (4 - padded.length() % 4) % 4;
        padded += "=".repeat(padding);
        
        try {
            return Base64.getDecoder().decode(padded);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64URL string: " + e.getMessage());
        }
    }

    // Derive IV from SHA-256 of public key
    private static byte[] getIV(String publicKey) throws Exception {
        if (publicKey == null || publicKey.isEmpty()) {
            throw new IllegalArgumentException("Public key cannot be null or empty");
        }
        
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(publicKey.getBytes(StandardCharsets.UTF_8));
        byte[] iv = new byte[16];
        System.arraycopy(hash, 0, iv, 0, 16);
        return iv;
    }

    // Encryption method
    public static String encrypt(String plainText, String privateKey, String publicKey) throws Exception {
        if (plainText == null || privateKey == null || publicKey == null) {
            throw new IllegalArgumentException("Input parameters cannot be null");
        }
        
        byte[] iv = getIV(publicKey);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Pad/truncate private key to 32 bytes
        byte[] keyBytes = privateKey.getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[32];
        System.arraycopy(keyBytes, 0, key, 0, Math.min(keyBytes.length, 32));
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        return base64UrlEncode(encrypted);
    }

    // Decryption method
    public static String decrypt(String encodedText, String privateKey, String publicKey) throws Exception {
        if (encodedText == null || privateKey == null || publicKey == null) {
            throw new IllegalArgumentException("Input parameters cannot be null");
        }
        try {
            System.out.println("encryptedPayloadEnd length: " + encodedText.length());
            System.out.println("public_key length: " + publicKey.length());
            System.out.println("private_key length: " + privateKey.length());
            byte[] encryptedBytes = AES256Api.base64UrlDecode(encodedText);
            System.out.println("Decoded encryptedPayloadEnd length: " + encryptedBytes.length);

            byte[] iv = getIV(publicKey);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            byte[] keyBytes = privateKey.getBytes(StandardCharsets.UTF_8);
            byte[] key = new byte[32];
            System.arraycopy(keyBytes, 0, key, 0, Math.min(keyBytes.length, 32));
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] decrypted = cipher.doFinal(encryptedBytes);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new Exception("Decryption failed: " + e.getMessage(), e);
        }
    }
}
