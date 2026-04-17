package com.tablebooknow.test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

/**
 * Test class to verify that the JavaMail and ZXing (QR code) libraries are properly configured.
 * This class has two test methods:
 * 1. testQRCodeGeneration() - Tests if QR codes can be generated and saved
 * 2. testEmailSending() - Tests if emails can be sent
 */
public class DependencyTest {

    /**
     * Main method to run the tests
     */
    public static void main(String[] args) {
        System.out.println("Starting dependency tests...");

        // Test QR code generation
        boolean qrSuccess = testQRCodeGeneration();
        System.out.println("QR Code Test: " + (qrSuccess ? "PASSED" : "FAILED"));

        // Test email sending (commented out to avoid sending actual emails during tests)
        // Uncomment to test with your actual email credentials
        // boolean emailSuccess = testEmailSending("your-email@gmail.com", "your-password");
        // System.out.println("Email Test: " + (emailSuccess ? "PASSED" : "FAILED"));

        // Just test if JavaMail classes are available
        boolean emailClassesAvailable = checkEmailClasses();
        System.out.println("JavaMail Classes Available: " + (emailClassesAvailable ? "YES" : "NO"));
    }

    /**
     * Tests QR code generation functionality
     * @return true if successful, false otherwise
     */
    public static boolean testQRCodeGeneration() {
        try {
            // Create data for QR Code
            String qrCodeData = "Test Reservation: ID-12345, User-Test, Date-2025-04-01, Time-19:00";

            // Set QR Code properties
            String filePath = "test-qr-code.png";
            String charset = "UTF-8";
            int width = 200;
            int height = 200;
            BarcodeFormat format = BarcodeFormat.QR_CODE;

            // Generate QR Code
            BitMatrix matrix = new MultiFormatWriter().encode(
                    new String(qrCodeData.getBytes(charset), charset),
                    format, width, height);

            // Write to file
            try (FileOutputStream outputStream = new FileOutputStream(new File(filePath))) {
                MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);
            }

            System.out.println("QR code generated successfully at: " + new File(filePath).getAbsolutePath());
            return true;

        } catch (Exception e) {
            System.err.println("Error generating QR code: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tests email sending functionality
     * NOTE: This requires actual SMTP credentials and will send a real email
     *
     * @param username SMTP username (typically email address)
     * @param password SMTP password or app password
     * @return true if successful, false otherwise
     */
    public static boolean testEmailSending(String username, String password) {
        try {
            // Mail server properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com"); // Using Gmail as an example
            props.put("mail.smtp.port", "587");

            // Create session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(username));
            message.setSubject("Table Reservation System - Email Test");
            message.setText("This is a test email to verify JavaMail functionality in the Table Reservation System.");

            // Send the message
            Transport.send(message);

            System.out.println("Test email sent successfully to: " + username);
            return true;

        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if JavaMail classes are available without actually sending an email
     * @return true if JavaMail classes are available, false otherwise
     */
    public static boolean checkEmailClasses() {
        try {
            // Just check if essential classes are available
            Class.forName("javax.mail.Message");
            Class.forName("javax.mail.Transport");
            Class.forName("javax.mail.internet.MimeMessage");
            return true;
        } catch (ClassNotFoundException e) {
            System.err.println("JavaMail classes not found: " + e.getMessage());
            return false;
        }
    }
}