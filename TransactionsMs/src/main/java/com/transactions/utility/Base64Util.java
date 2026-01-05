package com.transactions.utility;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;
public class Base64Util {
    private static final Pattern BASE64_PATTERN = Pattern.compile("^[A-Za-z0-9+/=]+$");

    public static String encodeBase64(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return Base64.getEncoder().encodeToString(input.getBytes());
    }


    public static String decodeBase64(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return "";
        }

        if (!BASE64_PATTERN.matcher(base64).matches()) {
            System.err.println("Invalid Base64 input: It looks like plain text or JSON, not Base64!");
            return base64; // Return as-is
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid Base64 input: " + e.getMessage());
            return "";
        }
    }
    /*
    public static String decodeBase64(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return "";
        }
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64);
            return new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid Base64 input: " + e.getMessage());
            return "";
        }
    }
    */
    /**
     * Checks if a given string is already Base64 encoded.
     * @param input The string to check.
     * @return True if the string is Base64 encoded, false otherwise.
     */
    public static boolean isBase64Encoded(String input) {
        if (input == null || input.length() % 4 != 0) {
            return false;
        }
        return BASE64_PATTERN.matcher(input).matches();
    }
}
