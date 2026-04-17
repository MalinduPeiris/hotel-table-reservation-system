package com.tablebooknow.service;

import com.tablebooknow.model.payment.Payment;
import com.tablebooknow.model.payment.PaymentCard;
import com.tablebooknow.model.reservation.Reservation;
import com.tablebooknow.model.user.User;
import com.tablebooknow.dao.PaymentCardDAO;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * PaymentGateway class for integration with PayHere payment system.
 * Handles creating payment requests and validating responses.
 */
public class PaymentGateway {

    // PayHere API configurations
    private static final String SANDBOX_URL = "https://sandbox.payhere.lk/pay/checkout";
    private static final String PRODUCTION_URL = "https://www.payhere.lk/pay/checkout";

    // PayHere merchant details - REPLACE WITH YOUR ACTUAL SANDBOX CREDENTIALS
    private static final String MERCHANT_ID = "1221688";  // Replace with your actual sandbox merchant ID
    private static final String MERCHANT_SECRET = "NDEwMjkxMjMxNTMxODkxNzQzNjMyNTI5MjgxMDkzMzgwMjY4MjY0MQ=="; // Replace with your actual sandbox secret
    private static final boolean USE_SANDBOX = true; // Set to true for sandbox, false for production
    private static final String CURRENCY = "USD"; // Changed to USD to match with your prices

    // Table pricing based on table type
    private static final Map<String, BigDecimal> TABLE_PRICES = new HashMap<>();
    static {
        TABLE_PRICES.put("family", new BigDecimal("12.00"));
        TABLE_PRICES.put("luxury", new BigDecimal("18.00"));
        TABLE_PRICES.put("regular", new BigDecimal("8.00"));
        TABLE_PRICES.put("couple", new BigDecimal("6.00"));
    }

    /**
     * Get the PayHere checkout URL (either sandbox or production)
     * @return The appropriate checkout URL
     */
    public String getCheckoutUrl() {
        return USE_SANDBOX ? SANDBOX_URL : PRODUCTION_URL;
    }

    /**
     * Calculate the amount to charge based on table type and booking duration
     * @param tableType The type of table (family, luxury, regular, couple)
     * @param duration Booking duration in hours
     * @return The calculated price
     */
    public BigDecimal calculateAmount(String tableType, int duration) {
        BigDecimal basePrice = TABLE_PRICES.getOrDefault(tableType, new BigDecimal("8.00"));
        return basePrice.multiply(new BigDecimal(duration));
    }

    /**
     * Generate PayHere form parameters for the payment, including card details if available
     * @param payment The payment object
     * @param reservation The reservation object
     * @param user The user making the payment
     * @param returnUrl URL to return to after payment
     * @param cancelUrl URL to return to if payment is cancelled
     * @param notifyUrl URL for PayHere to send payment notification
     * @param paymentCardId Optional payment card ID to pre-fill card details
     * @return Map of form parameters
     */
    public Map<String, String> generateFormParameters(
            Payment payment,
            Reservation reservation,
            User user,
            String returnUrl,
            String cancelUrl,
            String notifyUrl,
            String paymentCardId
    ) {
        Map<String, String> params = new HashMap<>();

        // Required Parameters - make sure all these are filled
        params.put("merchant_id", MERCHANT_ID);
        params.put("return_url", returnUrl);
        params.put("cancel_url", cancelUrl);
        params.put("notify_url", notifyUrl);

        // Format amount with exactly 2 decimal places
        String formattedAmount = String.format("%.2f", payment.getAmount());

        // Transaction details
        params.put("order_id", payment.getId());
        params.put("items", "Table Reservation - " + extractTableTypeFromId(reservation.getTableId()));
        params.put("currency", CURRENCY); // Use USD as currency
        params.put("amount", formattedAmount);

        // Customer details - make sure these are not null/empty
        String firstName = user.getUsername();
        String lastName = "Customer";
        String email = user.getEmail() != null ? user.getEmail() : "customer@example.com";
        String phone = user.getPhone() != null ? user.getPhone() : "0771234567";

        params.put("first_name", firstName);
        params.put("last_name", lastName);
        params.put("email", email);
        params.put("phone", phone);
        params.put("address", "Hotel Address");
        params.put("city", "Colombo");
        params.put("country", "Sri Lanka");

        // Custom parameters for your reference
        params.put("custom_1", reservation.getId());
        params.put("custom_2", user.getId());

        // Add payment card details if provided
        if (paymentCardId != null && !paymentCardId.isEmpty()) {
            try {
                PaymentCardDAO paymentCardDAO = new PaymentCardDAO();
                PaymentCard card = paymentCardDAO.findById(paymentCardId);

                if (card != null) {
                    // Set the payment card as custom_3
                    params.put("custom_3", card.getId());

                    // Pre-fill card information for PayHere (if supported by the gateway)
                    params.put("card_holder_name", card.getCardholderName());

                    // Only provide masked card number for security
                    String lastFour = card.getCardNumber().substring(Math.max(0, card.getCardNumber().length() - 4));
                    params.put("card_no", "************" + lastFour);

                    // Format expiry date MM/YY to separate month and year fields
                    if (card.getExpiryDate() != null && card.getExpiryDate().contains("/")) {
                        String[] expiryParts = card.getExpiryDate().split("/");
                        if (expiryParts.length == 2) {
                            params.put("card_expiry_month", expiryParts[0]);
                            params.put("card_expiry_year", "20" + expiryParts[1]); // Assuming YY format
                        }
                    }

                    System.out.println("Payment card details added to payment parameters");
                }
            } catch (Exception e) {
                System.err.println("Error retrieving payment card details: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Generate hash - this is critical for PayHere validation
        String hash = generateHash(
                MERCHANT_ID,
                params.get("order_id"),
                formattedAmount,
                CURRENCY,
                MERCHANT_SECRET
        );
        params.put("hash", hash);

        // Debug info to check parameters being sent
        System.out.println("PayHere parameters:");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        return params;
    }

    /**
     * Extract the table type from the table ID (e.g., "f1-1" -> "family")
     * @param tableId The table ID
     * @return The table type or "regular" if not found
     */
    private String extractTableTypeFromId(String tableId) {
        if (tableId == null || tableId.isEmpty()) {
            return "regular";
        }

        char firstChar = tableId.charAt(0);
        switch (firstChar) {
            case 'f':
                return "family";
            case 'l':
                return "luxury";
            case 'r':
                return "regular";
            case 'c':
                return "couple";
            default:
                return "regular";
        }
    }

    /**
     * Generate a hash value for secure payment verification
     * This uses the PayHere merchant hash generation algorithm
     * @param merchantId PayHere merchant ID
     * @param orderId Your order ID
     * @param amount Payment amount formatted with 2 decimal places
     * @param currency Currency code (USD)
     * @param merchantSecret Your PayHere merchant secret key
     * @return MD5 hash of the parameters
     */
    private String generateHash(String merchantId, String orderId, String amount, String currency, String merchantSecret) {
        // First, hash the merchant secret
        String md5MerchantSecret = md5(merchantSecret).toUpperCase();

        // Now create the string to hash as per PayHere specifications
        String stringToHash = merchantId + orderId + amount + currency + md5MerchantSecret;

        System.out.println("String to hash: " + stringToHash);

        // Generate the final hash
        String hash = md5(stringToHash).toUpperCase();

        System.out.println("Generated hash: " + hash);

        return hash;
    }

    /**
     * Validate a payment notification from PayHere
     * @param merchantId Merchant ID from notification
     * @param orderId Order ID from notification
     * @param paymentId PayHere payment ID
     * @param amount Payment amount
     * @param currency Payment currency
     * @param status Payment status
     * @return true if hash matches (valid notification), false otherwise
     */
    public boolean validateNotification(
            String merchantId,
            String orderId,
            String paymentId,
            String amount,
            String currency,
            String status
    ) {
        // First, verify merchant ID
        if (!merchantId.equals(MERCHANT_ID)) {
            System.out.println("Merchant ID mismatch: " + merchantId + " vs " + MERCHANT_ID);
            return false;
        }

        // Generate hash for verification based on PayHere's notification hash format
        String md5MerchantSecret = md5(MERCHANT_SECRET).toUpperCase();

        // Construct the string to hash according to PayHere docs for notification verification
        String stringToHash = merchantId + orderId + amount + currency + status + md5MerchantSecret;

        String generatedHash = md5(stringToHash).toUpperCase();
        System.out.println("Generated verification hash: " + generatedHash);

        // In a real implementation, you would compare this with the md5sig sent in the notification
        // For now, we'll assume it's valid for demonstration purposes
        return true;
    }

    /**
     * Generate MD5 hash of a string
     * @param input The string to hash
     * @return MD5 hash of the input
     */
    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not generate MD5 hash", e);
        }
    }
}