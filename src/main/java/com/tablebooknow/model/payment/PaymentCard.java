package com.tablebooknow.model.payment;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Model class for storing payment card information
 */
public class PaymentCard implements Serializable {
    private String id;
    private String userId;
    private String cardholderName;
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private String cardType; // visa, mastercard, amex, discover
    private boolean defaultCard;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Default constructor that generates a unique ID for a new payment card.
     */
    public PaymentCard() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.defaultCard = false;
    }

    /**
     * Constructor with all fields.
     */
    public PaymentCard(String id, String userId, String cardholderName, String cardNumber,
                       String expiryDate, String cvv, String cardType, boolean defaultCard,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.cardholderName = cardholderName;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.cardType = cardType;
        this.defaultCard = defaultCard;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public void setCardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public boolean isDefaultCard() {
        return defaultCard;
    }

    public void setDefaultCard(boolean defaultCard) {
        this.defaultCard = defaultCard;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the last 4 digits of the card number.
     */
    public String getLast4Digits() {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }

    /**
     * Returns a masked version of the card number.
     */
    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "****************";
        }

        // Only show the last 4 digits
        String last4 = getLast4Digits();
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < cardNumber.length() - 4; i++) {
            masked.append("*");
        }
        masked.append(last4);

        // Add spaces for readability
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < masked.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(masked.charAt(i));
        }

        return formatted.toString();
    }

    /**
     * Converts the PaymentCard object to a CSV format string for file storage.
     * Format: id,userId,cardholderName,cardNumber,expiryDate,cvv,cardType,defaultCard,createdAt,updatedAt
     */
    public String toCsvString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%b,%s,%s",
                id,
                userId != null ? userId : "",
                cardholderName != null ? cardholderName.replace(",", ";;") : "",
                cardNumber != null ? cardNumber : "",
                expiryDate != null ? expiryDate : "",
                cvv != null ? cvv : "",
                cardType != null ? cardType : "",
                defaultCard,
                createdAt != null ? createdAt.toString() : "",
                updatedAt != null ? updatedAt.toString() : "");
    }

    /**
     * Creates a PaymentCard object from a CSV format string.
     */
    public static PaymentCard fromCsvString(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length < 10) {
            throw new IllegalArgumentException("Invalid CSV format for PaymentCard");
        }

        // Parse timestamps
        LocalDateTime createdDateTime = null;
        LocalDateTime updatedDateTime = null;
        try {
            if (!parts[8].isEmpty()) {
                createdDateTime = LocalDateTime.parse(parts[8]);
            }
            if (!parts[9].isEmpty()) {
                updatedDateTime = LocalDateTime.parse(parts[9]);
            }
        } catch (Exception e) {
            // Use current time as fallback
            LocalDateTime now = LocalDateTime.now();
            createdDateTime = now;
            updatedDateTime = now;
        }

        // Parse cardholderName with special handling for commas
        String cardholderName = parts[2].replace(";;", ",");

        // Parse boolean value
        boolean isDefault = Boolean.parseBoolean(parts[7]);

        return new PaymentCard(
                parts[0],                  // id
                parts[1],                  // userId
                cardholderName,            // cardholderName
                parts[3],                  // cardNumber
                parts[4],                  // expiryDate
                parts[5],                  // cvv
                parts[6],                  // cardType
                isDefault,                 // defaultCard
                createdDateTime,           // createdAt
                updatedDateTime            // updatedAt
        );
    }

    @Override
    public String toString() {
        return "PaymentCard{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", cardholderName='" + cardholderName + '\'' +
                ", cardNumber='" + getMaskedCardNumber() + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", cardType='" + cardType + '\'' +
                ", defaultCard=" + defaultCard +
                ", createdAt=" + createdAt +
                '}';
    }
}