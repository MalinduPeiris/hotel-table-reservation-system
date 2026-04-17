package com.tablebooknow.dao;

import com.tablebooknow.model.payment.Payment;
import com.tablebooknow.util.FileHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Payment entities to handle file-based storage operations.
 */
public class PaymentDAO {

    private static final String FILE_PATH = getDataFilePath("payments.txt");

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
     * Creates a new payment record by appending to the payments file.
     * @param payment The payment to create
     * @return The created payment with assigned ID
     */
    public Payment create(Payment payment) throws IOException {
        // Make sure the file exists
        FileHandler.ensureFileExists(FILE_PATH);

        System.out.println("Creating payment record in file: " + FILE_PATH);

        // Append payment to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(payment.toCsvString());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            throw e;
        }

        return payment;
    }

    /**
     * Finds a payment by its ID.
     * @param id The ID to search for
     * @return The payment or null if not found
     */
    public Payment findById(String id) throws IOException {
        if (!FileHandler.fileExists(FILE_PATH)) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        Payment payment = Payment.fromCsvString(line);
                        if (payment.getId().equals(id)) {
                            return payment;
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing payment line: " + line);
                        // Continue to next line on error
                    }
                }
            }
        }

        return null;
    }

    /**
     * Finds all payments for a specific reservation.
     * @param reservationId The reservation ID
     * @return List of payments for the reservation
     */
    public List<Payment> findByReservationId(String reservationId) throws IOException {
        List<Payment> reservationPayments = new ArrayList<>();

        if (!FileHandler.fileExists(FILE_PATH)) {
            return reservationPayments;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        Payment payment = Payment.fromCsvString(line);
                        if (payment.getReservationId().equals(reservationId)) {
                            reservationPayments.add(payment);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing payment line: " + line);
                        // Continue to next line on error
                    }
                }
            }
        }

        return reservationPayments;
    }

    /**
     * Finds all payments for a specific user.
     * @param userId The user ID
     * @return List of payments for the user
     */
    public List<Payment> findByUserId(String userId) throws IOException {
        List<Payment> userPayments = new ArrayList<>();

        if (!FileHandler.fileExists(FILE_PATH)) {
            return userPayments;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        Payment payment = Payment.fromCsvString(line);
                        if (payment.getUserId().equals(userId)) {
                            userPayments.add(payment);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing payment line: " + line);
                        // Continue to next line on error
                    }
                }
            }
        }

        return userPayments;
    }

    /**
     * Updates a payment's information.
     * @param payment The payment to update
     * @return true if successful, false otherwise
     */
    public boolean update(Payment payment) throws IOException {
        if (!FileHandler.fileExists(FILE_PATH)) {
            return false;
        }

        List<Payment> payments = findAll();
        boolean found = false;

        // Replace the payment in the list
        for (int i = 0; i < payments.size(); i++) {
            if (payments.get(i).getId().equals(payment.getId())) {
                payments.set(i, payment);
                found = true;
                break;
            }
        }

        if (!found) {
            return false;
        }

        // Write all payments back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Payment p : payments) {
                writer.write(p.toCsvString());
                writer.newLine();
            }
        }

        return true;
    }

    /**
     * Updates a payment's status.
     * @param id The ID of the payment to update
     * @param status The new status (PENDING, COMPLETED, FAILED, REFUNDED)
     * @return true if successful, false otherwise
     */
    public boolean updateStatus(String id, String status) throws IOException {
        Payment payment = findById(id);
        if (payment == null) {
            return false;
        }

        payment.setStatus(status);
        if (status.equals("COMPLETED")) {
            payment.setCompletedAt(java.time.LocalDateTime.now());
        }

        return update(payment);
    }

    /**
     * Gets all payments from the file.
     * @return List of all payments
     */
    public List<Payment> findAll() throws IOException {
        List<Payment> payments = new ArrayList<>();

        if (!FileHandler.fileExists(FILE_PATH)) {
            return payments;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        payments.add(Payment.fromCsvString(line));
                    } catch (Exception e) {
                        System.err.println("Error parsing payment line: " + line);
                        // Continue to next line on error
                    }
                }
            }
        }

        return payments;
    }
}