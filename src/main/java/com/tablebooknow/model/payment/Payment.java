package com.tablebooknow.model.payment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Model class for storing payment information
 */
public class Payment implements Serializable {
    private String id;
    private String reservationId;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private String status; // PENDING, COMPLETED, FAILED, REFUNDED
    private String paymentMethod;
    private String transactionId; // ID from payment gateway
    private String paymentGateway; // PayHere, etc.
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    /**
     * Default constructor that generates a unique ID for a new payment.
     */
    public Payment() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
        this.currency = "LKR"; // Default currency for Sri Lanka
    }

    /**
     * Constructor with all fields.
     */
    public Payment(String id, String reservationId, String userId, BigDecimal amount,
                   String currency, String status, String paymentMethod,
                   String transactionId, String paymentGateway,
                   LocalDateTime createdAt, LocalDateTime completedAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.paymentGateway = paymentGateway;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPaymentGateway() {
        return paymentGateway;
    }

    public void setPaymentGateway(String paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    /**
     * Converts the Payment object to a CSV format string for file storage.
     * Format: id,reservationId,userId,amount,currency,status,paymentMethod,transactionId,paymentGateway,createdAt,completedAt
     */
    public String toCsvString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                id,
                reservationId != null ? reservationId : "",
                userId != null ? userId : "",
                amount != null ? amount.toString() : "",
                currency != null ? currency : "",
                status != null ? status : "",
                paymentMethod != null ? paymentMethod : "",
                transactionId != null ? transactionId : "",
                paymentGateway != null ? paymentGateway : "",
                createdAt != null ? createdAt.toString() : "",
                completedAt != null ? completedAt.toString() : "");
    }

    /**
     * Creates a Payment object from a CSV format string.
     */
    public static Payment fromCsvString(String csvLine) {
        try {
            String[] parts = csvLine.split(",");
            if (parts.length < 10) {
                throw new IllegalArgumentException("Invalid CSV format for Payment: expected at least 10 fields, got " + parts.length);
            }

            // Create a new payment with default values
            Payment payment = new Payment();

            // Set mandatory fields - careful with array indexing
            payment.setId(parts[0].trim());

            // Handle optional fields
            if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                payment.setReservationId(parts[1].trim());
            }

            if (parts.length > 2 && !parts[2].trim().isEmpty()) {
                payment.setUserId(parts[2].trim());
            }

            // Handle amount
            if (parts.length > 3 && !parts[3].trim().isEmpty()) {
                try {
                    payment.setAmount(new BigDecimal(parts[3].trim()));
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing amount: " + parts[3]);
                    // Use default value or set to zero
                    payment.setAmount(BigDecimal.ZERO);
                }
            }

            // Handle currency
            if (parts.length > 4 && !parts[4].trim().isEmpty()) {
                payment.setCurrency(parts[4].trim());
            }

            // Handle status
            if (parts.length > 5 && !parts[5].trim().isEmpty()) {
                payment.setStatus(parts[5].trim());
            }

            // Handle paymentMethod
            if (parts.length > 6 && !parts[6].trim().isEmpty()) {
                payment.setPaymentMethod(parts[6].trim());
            }

            // Handle transactionId
            if (parts.length > 7 && !parts[7].trim().isEmpty()) {
                payment.setTransactionId(parts[7].trim());
            }

            // Handle paymentGateway
            if (parts.length > 8 && !parts[8].trim().isEmpty()) {
                payment.setPaymentGateway(parts[8].trim());
            }

            // Handle createdAt
            if (parts.length > 9 && !parts[9].trim().isEmpty()) {
                try {
                    payment.setCreatedAt(LocalDateTime.parse(parts[9].trim()));
                } catch (Exception e) {
                    System.err.println("Error parsing createdAt date: " + parts[9]);
                    // Use current time as fallback
                    payment.setCreatedAt(LocalDateTime.now());
                }
            }

            // Handle completedAt (if available)
            if (parts.length > 10 && !parts[10].trim().isEmpty()) {
                try {
                    payment.setCompletedAt(LocalDateTime.parse(parts[10].trim()));
                } catch (Exception e) {
                    System.err.println("Error parsing completedAt date: " + parts[10]);
                    // Leave as null if can't parse
                }
            }

            return payment;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing payment CSV: " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id='" + id + '\'' +
                ", reservationId='" + reservationId + '\'' +
                ", userId='" + userId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status='" + status + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", paymentGateway='" + paymentGateway + '\'' +
                ", createdAt=" + createdAt +
                ", completedAt=" + completedAt +
                '}';
    }
}