package com.transactions.utility;
import java.util.HashMap;
import java.util.Map;
public class CardValidatorUtils {
    private static final Map<String, String> testCardEnrollmentMap = new HashMap<>();
    static {
        testCardEnrollmentMap.put("5111111111111111", "3DS");
        testCardEnrollmentMap.put("5123450000000008", "25");
        testCardEnrollmentMap.put("5555229999997722", "26");
        testCardEnrollmentMap.put("5555229999999975", "9");
        // Visa
        testCardEnrollmentMap.put("4111111111111111", "3DS");
        testCardEnrollmentMap.put("4012000033330026", "25");
        testCardEnrollmentMap.put("4043409999991437", "26");
        testCardEnrollmentMap.put("4029939999997636", "9");
        // Diners Club
        testCardEnrollmentMap.put("30000000000000", "3DS");
        testCardEnrollmentMap.put("30123400000000", "25");
        testCardEnrollmentMap.put("36259600000012", "26");
        // JCB
        testCardEnrollmentMap.put("35000000000000", "3DS");
        testCardEnrollmentMap.put("3528000000000007", "25");
        testCardEnrollmentMap.put("3528111100000001", "26");
    }

    public static Map<String, String> getCardDetails(String cardNumber) {
        Map<String, String> result = new HashMap<>();

        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            result.put("cardType", "Unknown");
            result.put("enrollmentType", "Unknown");
            return result;
        }

        cardNumber = cardNumber.replaceAll("[\\s-]", "").trim();
        String enrollmentType = testCardEnrollmentMap.getOrDefault(cardNumber, "Live");
        String cardType = detectCardType(cardNumber);
        result.put("cardType", cardType);
        result.put("enrollmentType", enrollmentType);
        return result;
    }

    public static Map<String, Object> validateAllCard(String cardNumber) {
        Map<String, Object> result = new HashMap<>();
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            result.put("valid", false);
            result.put("message", "Card number is required");
            return result;
        }
    
        cardNumber = cardNumber.replaceAll("\\s+", "").trim();
        String cardType = detectCardType(cardNumber);
        String enrollmentType = testCardEnrollmentMap.get(cardNumber); 
        result.put("cardNumber", cardNumber);
        result.put("cardType", cardType);
    
        if (enrollmentType != null) {
            String message = "";
            if (enrollmentType.equals("3DS")) {
                message = "3DS Authentication";
            } else if (enrollmentType.equals("25")) {
                message = "Approved";
            } else if (enrollmentType.equals("26")) {
                message = "Declined";
            } else if (enrollmentType.equals("9")) {
                message = "Unknown";
            }

            result.put("enrollmentType", enrollmentType);
            result.put("valid", true);
            result.put("validLuhn", true); // assume all test cards pass
            result.put("message", message);
        } else {
            // ✅ Not a test card → use Luhn for live cards
            boolean luhnValid = isValidLuhn(cardNumber);
            result.put("enrollmentType", "LIVE");
            result.put("validLuhn", luhnValid);
            result.put("valid", luhnValid);
            result.put("message", luhnValid ? "Live card is valid (Luhn passed)" : "Live card is invalid (Luhn failed)");
        }
    
        return result;
    }
    /**
     * Validates a card number using the Luhn algorithm.
     *
     * @param cardNumber The card number to validate.
     * @return true if the card number is valid, false otherwise.
     */
    public static boolean isValidLuhn(String cardNumber) {
        // Remove spaces and dashes
        cardNumber = cardNumber.replaceAll("[\\s-]", "").trim();

        // Check if the card number is numeric and has a valid length
        if (!cardNumber.matches("\\d{13,19}")) {
            return false;
        }

        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));

            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }

            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    /**
    Visa: starts with 4, 13 or 16 digits.
    Mastercard: starts with 51–55, total 16 digits.
    Amex: starts with 34 or 37, 15 digits.
    RuPay: starts with 6, 13–16 digits.
    Discover: starts with 6011 or 65 + 12 digits.
    JCB: starts with 35 or 2131/1800, 15–16 digits.
    Diners: starts with 300–305, 36 or 38 + 11 digits.
    */
    private static String detectCardType(String cardNumber) {
         // Remove spaces and dashes
         cardNumber = cardNumber.replaceAll("[\\s-]", "").trim();

        if (cardNumber.matches("^4[0-9]{12}(?:[0-9]{3})?$")) {
            return "Visa";
        } else if (cardNumber.matches("^5[1-5][0-9]{14}$")) {
            return "Mastercard";
        } else if (cardNumber.matches("^3[47][0-9]{13}$")) {
            return "American Express";
        } else if (cardNumber.matches("^6[0-9]{12}(?:[0-9]{3})?$")) {
            return "RuPay";
        } else if (cardNumber.matches("^6(?:011|5[0-9]{2})[0-9]{12}$")) {
            return "Discover";
        } else if (cardNumber.matches("^(3[0-9]{4}|2131|1800)[0-9]{11}$")) {
            return "JCB";
        } else if (cardNumber.matches("^3(0[0-5]|[68][0-9])[0-9]{11}$")) {
            return "Diners Club";
        }
        return "Unknown";
    }

    // Example usage
    // Demo usage (can be called from controller)
    public static String demoValidateCard(String ccno) {

        if(ccno == null || ccno.trim().isEmpty()) {
            ccno =  "4111111111111111"; // Default test card number
        }

        ccno = ccno.replaceAll("[\\s-]", "").trim();

        Map<String, String> result = getCardDetails(ccno);
        System.out.println("Card Type: " + result.get("cardType"));
        System.out.println("Enrollment Type: " + result.get("enrollmentType"));

        Map<String, Object> info = validateAllCard(ccno);

        System.out.println("Card Number: " + info.get("cardNumber"));
        System.out.println("Card Type: " + info.get("cardType"));
        System.out.println("3DS Status: " + info.get("enrollmentType"));
        System.out.println("Luhn Valid: " + info.get("validLuhn"));
        System.out.println("Final Validation: " + info.get("valid"));
        System.out.println("Message: " + info.get("message"));

        /*
         * Output:
            Card Number: 4111111111111111
            Card Type: Visa
            3DS Status: 3DS
            Luhn Valid: true
            Final Validation: true
            Message: Card is valid
         */

        return "Card Type: " + result.get("cardType") + "\n" +
                "Enrollment Type: " + result.get("enrollmentType") + "\n" +
                "Card Number: " + info.get("cardNumber") + "\n" +
                "3DS Status: " + info.get("enrollmentType") + "\n" +
                "Luhn Valid: " + info.get("validLuhn") + "\n" +
                "Final Validation: " + info.get("valid") + "\n" +
                "Message: " + info.get("message");
    }

    /**
     * Extracts the last 4 digits from a card number.
     * 
     * @param cardNumber The card number to extract from
     * @return The last 4 digits as a string, or null if invalid
     */
    public static String getLast4Digits(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return null;
        }
        
        // Remove spaces and dashes
        String cleanCardNumber = cardNumber.replaceAll("[\\s-]", "").trim();
        
        // Check if card number has at least 4 digits
        if (cleanCardNumber.length() < 4 || !cleanCardNumber.matches("\\d+")) {
            return null;
        }
        
        return cleanCardNumber.substring(cleanCardNumber.length() - 4);
    }

    /**
     * Masks a card number showing only the last 4 digits.
     * 
     * @param cardNumber The card number to mask
     * @return Masked card number (e.g., "**** **** **** 1234") or null if invalid
     */
    public static String maskCardNumber(String cardNumber) {
        String last4 = getLast4Digits(cardNumber);
        if (last4 == null) {
            return null;
        }
        
        // Create masked version with asterisks
        String masked = "**** **** **** " + last4;
        return masked;
    }

    /**
     * Extracts the BIN (Bank Identification Number) from a card number.
     * 
     * @param cardNumber The card number to extract BIN from
     * @return The BIN as an integer, or null if invalid
     */
    public static Integer getBinNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return null;
        }
        
        // Remove spaces and dashes
        String cleanCardNumber = cardNumber.replaceAll("[\\s-]", "").trim();
        
        // Check if card number has at least 4 digits
        if (cleanCardNumber.length() < 4 || !cleanCardNumber.matches("\\d+")) {
            return null;
        }
        
        try {
            // Extract first 4-6 digits as BIN (typically 4-6 digits)
            String binStr = cleanCardNumber.substring(0, Math.min(6, cleanCardNumber.length()));
            return Integer.parseInt(binStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Creates a formatted card number with BIN + **** + last 4 digits.
     * 
     * @param cardNumber The card number to format
     * @return Formatted card number (e.g., "4111****1111") or null if invalid
     */
    public static String formatCardNumberWithBin(String cardNumber) {
        String last4 = getLast4Digits(cardNumber);
        Integer bin = getBinNumber(cardNumber);
        
        if (last4 != null && bin != null) {
            // Convert BIN to string and ensure it's at least 4 digits
            String binStr = String.valueOf(bin);
            while (binStr.length() < 4) {
                binStr = "0" + binStr;
            }
            // Take only first 4 digits of BIN for display
            String binDisplay = binStr.substring(0, Math.min(4, binStr.length()));
            return binDisplay + "****" + last4;
        } else if (last4 != null) {
            // Fallback to standard masking if BIN extraction fails
            return "****" + last4;
        }
        
        return null;
    }
}
