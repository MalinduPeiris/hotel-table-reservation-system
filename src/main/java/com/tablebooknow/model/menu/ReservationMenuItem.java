package com.tablebooknow.model.menu;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents the many-to-many relationship between Reservations and MenuItems.
 * This class keeps track of which menu items are ordered for a specific reservation.
 */
public class ReservationMenuItem implements Serializable {
    private String id;
    private String reservationId;
    private String menuItemId;
    private int quantity;
    private String specialInstructions; // Optional: For special requests related to this specific item

    /**
     * Default constructor that generates a unique ID.
     */
    public ReservationMenuItem() {
        this.id = UUID.randomUUID().toString();
        this.quantity = 1; // Default quantity is 1
    }

    /**
     * Constructor with all fields.
     */
    public ReservationMenuItem(String id, String reservationId, String menuItemId,
                               int quantity, String specialInstructions) {
        this.id = id;
        this.reservationId = reservationId;
        this.menuItemId = menuItemId;
        this.quantity = quantity;
        this.specialInstructions = specialInstructions;
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

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    /**
     * Converts the ReservationMenuItem object to a CSV format string for file storage.
     * Format: id,reservationId,menuItemId,quantity,specialInstructions
     */
    public String toCsvString() {
        return String.format("%s,%s,%s,%d,%s",
                id,
                reservationId != null ? reservationId : "",
                menuItemId != null ? menuItemId : "",
                quantity,
                specialInstructions != null ? specialInstructions.replace(",", ";;") : "");
    }

    /**
     * Creates a ReservationMenuItem object from a CSV format string.
     */
    public static ReservationMenuItem fromCsvString(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length < 5) {
            throw new IllegalArgumentException("Invalid CSV format for ReservationMenuItem");
        }

        // Parse quantity
        int quantity = 1; // Default quantity is 1
        try {
            quantity = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing quantity: " + parts[3]);
        }

        // Handle special instructions with escaped commas
        String specialInstructions = parts[4];
        if (parts.length > 5) {
            // In case there were commas in the special instructions
            StringBuilder sb = new StringBuilder(specialInstructions);
            for (int i = 5; i < parts.length; i++) {
                sb.append(",").append(parts[i]);
            }
            specialInstructions = sb.toString();
        }

        // Unescape commas
        specialInstructions = specialInstructions.replace(";;", ",");

        return new ReservationMenuItem(
                parts[0],
                parts[1],
                parts[2],
                quantity,
                specialInstructions
        );
    }

    @Override
    public String toString() {
        return "ReservationMenuItem{" +
                "id='" + id + '\'' +
                ", reservationId='" + reservationId + '\'' +
                ", menuItemId='" + menuItemId + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}