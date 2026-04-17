package com.tablebooknow.model.menu;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a menu item in the system.
 */
public class MenuItem implements Serializable {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category; // appetizer, main, dessert, drink
    private boolean isAvailable;
    private String imageUrl; // Optional: for future image support

    /**
     * Default constructor that generates a unique ID for a new menu item.
     */
    public MenuItem() {
        this.id = UUID.randomUUID().toString();
        this.isAvailable = true; // By default, items are available
    }

    /**
     * Constructor with all fields.
     */
    public MenuItem(String id, String name, String description, BigDecimal price,
                    String category, boolean isAvailable, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.isAvailable = isAvailable;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Converts the MenuItem object to a CSV format string for file storage.
     * Format: id,name,description,price,category,isAvailable,imageUrl
     */
    public String toCsvString() {
        return String.format("%s,%s,%s,%s,%s,%b,%s",
                id,
                escapeCommas(name),
                escapeCommas(description),
                price != null ? price.toString() : "",
                category != null ? category : "",
                isAvailable,
                imageUrl != null ? imageUrl : "");
    }

    /**
     * Creates a MenuItem object from a CSV format string.
     */
    public static MenuItem fromCsvString(String csvLine) {
        // We need to handle commas within fields that are escaped with semicolons (;;)
        String[] parts = splitCsvLine(csvLine);

        if (parts.length < 7) {
            throw new IllegalArgumentException("Invalid CSV format for MenuItem");
        }

        // Parse price
        BigDecimal price = null;
        if (parts[3] != null && !parts[3].isEmpty()) {
            try {
                price = new BigDecimal(parts[3]);
            } catch (NumberFormatException e) {
                System.err.println("Error parsing price: " + parts[3]);
                price = BigDecimal.ZERO;
            }
        }

        // Parse isAvailable
        boolean isAvailable = true; // Default is available
        if (parts[5] != null && !parts[5].isEmpty()) {
            isAvailable = Boolean.parseBoolean(parts[5]);
        }

        return new MenuItem(
                parts[0],
                unescapeCommas(parts[1]),
                unescapeCommas(parts[2]),
                price,
                parts[4],
                isAvailable,
                parts[6]
        );
    }

    /**
     * Splits CSV line respecting escaped commas
     */
    private static String[] splitCsvLine(String line) {
        String[] result = new String[7];
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        int fieldIndex = 0;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == ',' && !inQuotes) {
                result[fieldIndex++] = field.toString();
                field = new StringBuilder();

                if (fieldIndex >= result.length) {
                    break;
                }
            } else if (c == '"') {
                inQuotes = !inQuotes;
                field.append(c);
            } else {
                field.append(c);
            }
        }

        if (fieldIndex < result.length) {
            result[fieldIndex] = field.toString();
        }

        return result;
    }

    /**
     * Escapes commas in fields with semicolons
     */
    private static String escapeCommas(String input) {
        if (input == null) {
            return "";
        }
        return input.replace(",", ";;");
    }

    /**
     * Unescapes commas in fields
     */
    private static String unescapeCommas(String input) {
        if (input == null) {
            return "";
        }
        return input.replace(";;", ",");
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", isAvailable=" + isAvailable +
                '}';
    }
}