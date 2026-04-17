package com.tablebooknow.dao;

import com.tablebooknow.model.payment.PaymentCard;
import com.tablebooknow.util.FileHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for PaymentCard entities to handle file-based storage operations.
 */
public class PaymentCardDAO {

    private static final String FILE_PATH = getDataFilePath("payment_cards.txt");

    /**
     * Gets the path to a data file, using the application's data directory.
     */
    private static String getDataFilePath(String fileName) {
        String dataPath = System.getProperty("app.datapath");

        // Fallback to user.dir/data if app.datapath is not set
        if (dataPath == null) {
            dataPath = System.getProperty("user.dir") + File.separator + "data";
            // Ensure the directory exists
            File dir = new File(dataPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }

        return dataPath + File.separator + fileName;
    }

    /**
     * Creates a new payment card by appending to the payment_cards file.
     * @param card The payment card to create
     * @return The created payment card with assigned ID
     */
    public PaymentCard create(PaymentCard card) throws IOException {
        // Make sure the file exists
        FileHandler.ensureFileExists(FILE_PATH);

        System.out.println("Creating payment card in file: " + FILE_PATH);

        // Set timestamps
        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());

        // Append card to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(card.toCsvString());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            throw e;
        }

        return card;
    }

    /**
     * Finds a payment card by its ID.
     * @param id The ID to search for
     * @return The payment card or null if not found
     */
    public PaymentCard findById(String id) throws IOException {
        if (!FileHandler.fileExists(FILE_PATH)) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        PaymentCard card = PaymentCard.fromCsvString(line);
                        if (card.getId().equals(id)) {
                            return card;
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing payment card line: " + line);
                        // Continue to next line on error
                    }
                }
            }
        }

        return null;
    }

    /**
     * Finds all payment cards for a specific user.
     * @param userId The user ID
     * @return List of payment cards for the user
     */
    public List<PaymentCard> findByUserId(String userId) throws IOException {
        List<PaymentCard> userCards = new ArrayList<>();

        if (!FileHandler.fileExists(FILE_PATH)) {
            return userCards;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        PaymentCard card = PaymentCard.fromCsvString(line);
                        if (card.getUserId().equals(userId)) {
                            userCards.add(card);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing payment card line: " + line);
                        // Continue to next line on error
                    }
                }
            }
        }

        return userCards;
    }

    /**
     * Updates a payment card's information.
     * @param card The payment card to update
     * @return true if successful, false otherwise
     */
    public boolean update(PaymentCard card) throws IOException {
        if (!FileHandler.fileExists(FILE_PATH)) {
            return false;
        }

        List<PaymentCard> cards = findAll();
        boolean found = false;

        // Set update timestamp
        card.setUpdatedAt(LocalDateTime.now());

        // Replace the card in the list
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getId().equals(card.getId())) {
                cards.set(i, card);
                found = true;
                break;
            }
        }

        if (!found) {
            return false;
        }

        // Write all cards back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (PaymentCard c : cards) {
                writer.write(c.toCsvString());
                writer.newLine();
            }
        }

        return true;
    }

    /**
     * Deletes a payment card by its ID.
     * @param id The ID of the payment card to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(String id) throws IOException {
        if (!FileHandler.fileExists(FILE_PATH)) {
            return false;
        }

        List<PaymentCard> cards = findAll();
        boolean found = false;

        // Remove the card from the list
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getId().equals(id)) {
                cards.remove(i);
                found = true;
                break;
            }
        }

        if (!found) {
            return false;
        }

        // Write all cards back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (PaymentCard c : cards) {
                writer.write(c.toCsvString());
                writer.newLine();
            }
        }

        return true;
    }

    /**
     * Gets all payment cards from the file.
     * @return List of all payment cards
     */
    public List<PaymentCard> findAll() throws IOException {
        List<PaymentCard> cards = new ArrayList<>();

        if (!FileHandler.fileExists(FILE_PATH)) {
            return cards;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        cards.add(PaymentCard.fromCsvString(line));
                    } catch (Exception e) {
                        System.err.println("Error parsing payment card line: " + line);
                        // Continue to next line on error
                    }
                }
            }
        }

        return cards;
    }

    /**
     * Gets the default payment card for a user.
     * @param userId The user ID
     * @return The default payment card or null if none is set
     */
    public PaymentCard findDefaultCard(String userId) throws IOException {
        List<PaymentCard> userCards = findByUserId(userId);

        for (PaymentCard card : userCards) {
            if (card.isDefaultCard()) {
                return card;
            }
        }

        // If no default is set but user has cards, return the first one
        if (!userCards.isEmpty()) {
            return userCards.get(0);
        }

        return null;
    }
}