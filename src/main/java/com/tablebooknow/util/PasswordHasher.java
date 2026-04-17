package com.tablebooknow.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for hashing and verifying passwords.
 * This implementation uses SHA-256 with salting for secure password storage.
 */
public class PasswordHasher {
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16; // 16 bytes = 128 bits
    private static final String DELIMITER = ":";

    /**
     * Hashes a password using SHA-256 with a random salt.
     * @param password The plain text password to hash
     * @return A string in the format "salt:hash"
     */
    public static String hashPassword(String password) {
        try {
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // Create hash
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());

            // Convert to Base64 strings
            String saltString = Base64.getEncoder().encodeToString(salt);
            String hashString = Base64.getEncoder().encodeToString(hashedPassword);

            // Return "salt:hash"
            return saltString + DELIMITER + hashString;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verifies a password against a stored hash.
     * @param password The plain text password to verify
     * @param storedHash The stored hash in the format "salt:hash"
     * @return true if the password matches, false otherwise
     */
    public static boolean checkPassword(String password, String storedHash) {
        try {
            // Split the stored hash into salt and hash
            String[] parts = storedHash.split(DELIMITER);
            if (parts.length != 2) {
                return false; // Invalid stored hash format
            }

            // Decode the salt and hash
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] hash = Base64.getDecoder().decode(parts[1]);

            // Hash the provided password with the same salt
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());

            // Compare the hashes
            if (hash.length != hashedPassword.length) {
                return false;
            }

            // Time-constant comparison to prevent timing attacks
            int diff = 0;
            for (int i = 0; i < hash.length; i++) {
                diff |= hash[i] ^ hashedPassword[i];
            }

            return diff == 0;
        } catch (NoSuchAlgorithmException | IllegalArgumentException e) {
            return false; // Any error means verification failed
        }
    }
}