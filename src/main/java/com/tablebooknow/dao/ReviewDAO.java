package com.tablebooknow.dao;

import com.tablebooknow.model.review.Review;
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
import java.util.logging.Logger;

/**
 * Data Access Object for Review entities to handle file-based storage operations.
 */
public class ReviewDAO {
    private static final Logger logger = Logger.getLogger(ReviewDAO.class.getName());
    private static final String FILE_PATH = getDataFilePath("reviews.txt");

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
     * Creates a new review by appending to the reviews file.
     * @param review The review to create
     * @return The created review with assigned ID
     */
    public Review create(Review review) throws IOException {
        // Make sure the file exists
        FileHandler.ensureFileExists(FILE_PATH);

        logger.info("Creating review in file: " + FILE_PATH);

        // Set timestamps
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        // Append review to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(review.toCsvString());
            writer.newLine();
        } catch (IOException e) {
            logger.severe("Error writing to file: " + e.getMessage());
            throw e;
        }

        return review;
    }

    /**
     * Finds a review by its ID.
     * @param id The ID to search for
     * @return The review or null if not found
     */
    public Review findById(String id) throws IOException {
        if (!FileHandler.fileExists(FILE_PATH)) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        Review review = Review.fromCsvString(line);
                        if (review.getId().equals(id)) {
                            return review;
                        }
                    } catch (Exception e) {
                        logger.warning("Error parsing review line: " + line);
                        // Continue to next line on error
                    }
                }
            }
        }

        return null;
    }

    /**
     * Find reviews by user ID.
     * @param userId The user ID to search for
     * @return List of reviews for the specified user
     */
    public List<Review> findByUserId(String userId) throws IOException {
        List<Review> userReviews = new ArrayList<>();

        if (!FileHandler.fileExists(FILE_PATH)) {
            return userReviews;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        Review review = Review.fromCsvString(line);
                        if (review.getUserId().equals(userId)) {
                            userReviews.add(review);
                        }
                    } catch (Exception e) {
                        logger.warning("Error parsing review line: " + line);
                    }
                }
            }
        }

        return userReviews;
    }

    /**
     * Find reviews by reservation ID.
     * @param reservationId The reservation ID to search for
     * @return List of reviews for the specified reservation
     */
    public List<Review> findByReservationId(String reservationId) throws IOException {
        List<Review> reservationReviews = new ArrayList<>();

        if (!FileHandler.fileExists(FILE_PATH)) {
            return reservationReviews;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        Review review = Review.fromCsvString(line);
                        if (reservationId.equals(review.getReservationId())) {
                            reservationReviews.add(review);
                        }
                    } catch (Exception e) {
                        logger.warning("Error parsing review line: " + line);
                    }
                }
            }
        }

        return reservationReviews;
    }

    /**
     * Check if a review exists for a specific reservation
     * @param reservationId The reservation ID to check
     * @param userId The user ID who owns the reservation
     * @return true if a review exists, false otherwise
     */
    public boolean hasReview(String reservationId, String userId) throws IOException {
        if (!FileHandler.fileExists(FILE_PATH)) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        Review review = Review.fromCsvString(line);
                        if (review.getReservationId().equals(reservationId) &&
                                review.getUserId().equals(userId)) {
                            return true;
                        }
                    } catch (Exception e) {
                        logger.warning("Error parsing review line: " + line);
                    }
                }
            }
        }

        return false;
    }

    /**
     * Updates a review's information.
     * @param review The review to update
     * @return true if successful, false otherwise
     */
    public boolean update(Review review) throws IOException {
        if (!FileHandler.fileExists(FILE_PATH)) {
            return false;
        }

        List<Review> reviews = findAll();
        boolean found = false;

        // Set update timestamp
        review.setUpdatedAt(LocalDateTime.now());

        // Replace the review in the list
        for (int i = 0; i < reviews.size(); i++) {
            if (reviews.get(i).getId().equals(review.getId())) {
                reviews.set(i, review);
                found = true;
                break;
            }
        }

        if (!found) {
            return false;
        }

        // Write all reviews back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Review r : reviews) {
                writer.write(r.toCsvString());
                writer.newLine();
            }
        }

        return true;
    }

    /**
     * Deletes a review by its ID.
     * @param id The ID of the review to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(String id) throws IOException {
        if (!FileHandler.fileExists(FILE_PATH)) {
            return false;
        }

        List<Review> reviews = findAll();
        boolean found = false;

        // Remove the review from the list
        for (int i = 0; i < reviews.size(); i++) {
            if (reviews.get(i).getId().equals(id)) {
                reviews.remove(i);
                found = true;
                break;
            }
        }

        if (!found) {
            return false;
        }

        // Write all reviews back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Review r : reviews) {
                writer.write(r.toCsvString());
                writer.newLine();
            }
        }

        return true;
    }

    /**
     * Gets all reviews from the file.
     * @return List of all reviews
     */
    public List<Review> findAll() throws IOException {
        List<Review> reviews = new ArrayList<>();

        if (!FileHandler.fileExists(FILE_PATH)) {
            return reviews;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        reviews.add(Review.fromCsvString(line));
                    } catch (Exception e) {
                        logger.warning("Error parsing review line: " + line);
                        // Continue to next line on error
                    }
                }
            }
        }

        return reviews;
    }

    /**
     * Gets the average rating for all reviews.
     * @return Average rating (1-5) or 0 if no reviews
     */
    public double getAverageRating() throws IOException {
        List<Review> reviews = findAll();

        if (reviews.isEmpty()) {
            return 0;
        }

        int sum = 0;
        for (Review review : reviews) {
            sum += review.getRating();
        }

        return (double) sum / reviews.size();
    }
}