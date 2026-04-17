package com.tablebooknow.dao;

import com.tablebooknow.util.FileHandler;
import com.tablebooknow.model.admin.Admin;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Admin entities to handle file-based storage operations.
 */
public class AdminDAO {

    private static final String FILE_PATH = getDataFilePath("admins.txt");

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
     * Creates a new admin by appending to the admins file.
     * @param admin The admin to create
     * @return The created admin with assigned ID
     */
    public Admin create(Admin admin) throws IOException {
        // Make sure the file exists
        FileHandler.ensureFileExists(FILE_PATH);

        System.out.println("Creating admin in file: " + FILE_PATH);

        // Append admin to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(admin.toCsvString());
            writer.newLine();
        }

        return admin;
    }

    /**
     * Finds an admin by their ID.
     * @param id The ID to search for
     * @return The admin or null if not found
     */
    public Admin findById(String id) throws IOException {
        if (!FileHandler.fileExists(FILE_PATH)) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        Admin admin = Admin.fromCsvString(line);
                        if (admin.getId().equals(id)) {
                            return admin;
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing admin line: " + line);
                        // Continue to next line on error
                    }
                }
            }
        }

        return null;
    }

    /**
     * Finds an admin by their username.
     * @param username The username to search for
     * @return The admin or null if not found
     */
    public Admin findByUsername(String username) throws IOException {
        if (!FileHandler.fileExists(FILE_PATH)) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        Admin admin = Admin.fromCsvString(line);
                        if (admin.getUsername().equalsIgnoreCase(username)) {
                            return admin;
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing admin line: " + line);
                        // Continue to next line on error
                    }
                }
            }
        }

        return null;
    }

    /**
     * Finds an admin by their email.
     * @param email The email to search for
     * @return The admin or null if not found
     */
    public Admin findByEmail(String email) throws IOException {
        if (!FileHandler.fileExists(FILE_PATH)) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        Admin admin = Admin.fromCsvString(line);
                        if (admin.getEmail() != null && admin.getEmail().equalsIgnoreCase(email)) {
                            return admin;
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing admin line: " + line);
                        // Continue to next line on error
                    }
                }
            }
        }

        return null;
    }

    /**
     * Updates an admin's information.
     * @param admin The admin to update
     * @return true if successful, false otherwise
     */
    public boolean update(Admin admin) throws IOException {
        if (!FileHandler.fileExists(FILE_PATH)) {
            return false;
        }

        List<Admin> admins = findAll();
        boolean found = false;

        // Replace the admin in the list
        for (int i = 0; i < admins.size(); i++) {
            if (admins.get(i).getId().equals(admin.getId())) {
                admins.set(i, admin);
                found = true;
                break;
            }
        }

        if (!found) {
            return false;
        }

        // Write all admins back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Admin a : admins) {
                writer.write(a.toCsvString());
                writer.newLine();
            }
        }

        return true;
    }

    /**
     * Deletes an admin by their ID.
     * @param id The ID of the admin to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(String id) throws IOException {
        if (!FileHandler.fileExists(FILE_PATH)) {
            return false;
        }

        List<Admin> admins = findAll();
        boolean found = false;

        // Remove the admin from the list
        for (int i = 0; i < admins.size(); i++) {
            if (admins.get(i).getId().equals(id)) {
                admins.remove(i);
                found = true;
                break;
            }
        }

        if (!found) {
            return false;
        }

        // Write all admins back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Admin a : admins) {
                writer.write(a.toCsvString());
                writer.newLine();
            }
        }

        return true;
    }

    /**
     * Gets all admins from the file.
     * @return List of all admins
     */
    public List<Admin> findAll() throws IOException {
        List<Admin> admins = new ArrayList<>();

        if (!FileHandler.fileExists(FILE_PATH)) {
            return admins;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        admins.add(Admin.fromCsvString(line));
                    } catch (Exception e) {
                        System.err.println("Error parsing admin line: " + line);
                        // Continue to next line on error
                    }
                }
            }
        }

        return admins;
    }

    /**
     * Initializes admin accounts if none exist.
     * This is useful for creating a default admin account on first run.
     */
    public void initializeDefaultAdmin() throws IOException {
        // Make sure the file exists
        FileHandler.ensureFileExists(FILE_PATH);

        // Check if we already have any admins
        List<Admin> existingAdmins = findAll();
        if (!existingAdmins.isEmpty()) {
            return; // Admins already exist, no need to create default
        }

        // Create default admin
        Admin defaultAdmin = new Admin();
        defaultAdmin.setUsername("admin");
        defaultAdmin.setPassword(com.tablebooknow.util.PasswordHasher.hashPassword("admin123"));
        defaultAdmin.setEmail("admin@gourmetreserve.com");
        defaultAdmin.setFullName("System Administrator");
        defaultAdmin.setRole("superadmin");

        create(defaultAdmin);

        System.out.println("Created default admin account. Username: admin, Password: admin123");
    }
}