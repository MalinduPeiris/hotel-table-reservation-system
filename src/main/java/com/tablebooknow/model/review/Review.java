package com.tablebooknow.model.review;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a review/feedback for a completed reservation.
 */
public class Review implements Serializable {
    private String id;
    private String userId;
    private String reservationId;
    private int rating;  // 1-5 stars
    private String title;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Default constructor that generates a unique ID for a new review.
     */
    public Review() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructor with all fields.
     */
    public Review(String id, String userId, String reservationId, int rating, String title,
                  String comment, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.reservationId = reservationId;
        this.rating = rating;
        this.title = title;
        this.comment = comment;
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

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < 1) {
            this.rating = 1;
        } else if (rating > 5) {
            this.rating = 5;
        } else {
            this.rating = rating;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
     * Converts the Review object to a CSV format string for file storage.
     * Format: id,userId,reservationId,rating,title,comment,createdAt,updatedAt
     */
    public String toCsvString() {
        return String.format("%s,%s,%s,%d,%s,%s,%s,%s",
                id,
                userId != null ? userId : "",
                reservationId != null ? reservationId : "",
                rating,
                title != null ? escapeCommas(title) : "",
                comment != null ? escapeCommas(comment) : "",
                createdAt != null ? createdAt.toString() : "",
                updatedAt != null ? updatedAt.toString() : "");
    }

    /**
     * Escape commas in strings to prevent CSV format issues.
     */
    private String escapeCommas(String str) {
        return str.replace(",", ";;");
    }

    /**
     * Unescape commas in strings from CSV format.
     */
    private static String unescapeCommas(String str) {
        return str.replace(";;", ",");
    }

    /**
     * Creates a Review object from a CSV format string.
     */
    public static Review fromCsvString(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length < 8) {
            throw new IllegalArgumentException("Invalid CSV format for Review: " + csvLine);
        }

        // Parse rating
        int rating = 5; // Default to 5 stars if parsing fails
        try {
            rating = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            // Use default if parsing fails
        }

        // Parse timestamps
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;
        try {
            if (!parts[6].isEmpty()) {
                createdAt = LocalDateTime.parse(parts[6]);
            }
            if (!parts[7].isEmpty()) {
                updatedAt = LocalDateTime.parse(parts[7]);
            }
        } catch (Exception e) {
            // Use current time as fallback
            LocalDateTime now = LocalDateTime.now();
            createdAt = now;
            updatedAt = now;
        }

        return new Review(
                parts[0],                     // id
                parts[1],                     // userId
                parts[2],                     // reservationId
                rating,                       // rating
                unescapeCommas(parts[4]),     // title
                unescapeCommas(parts[5]),     // comment
                createdAt,                    // createdAt
                updatedAt                     // updatedAt
        );
    }

    @Override
    public String toString() {
        return "Review{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", reservationId='" + reservationId + '\'' +
                ", rating=" + rating +
                ", title='" + title + '\'' +
                ", comment='" + comment + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}