package com.tablebooknow.service;

import com.tablebooknow.model.payment.Payment;
import com.tablebooknow.model.reservation.Reservation;
import com.tablebooknow.model.user.User;
import com.tablebooknow.util.QRCodeGenerator;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.FileDataSource;
import java.io.File;
import java.util.Properties;
import java.util.Date;

/**
 * Enhanced service class for sending confirmation emails to users with QR codes.
 */
public class EmailService {

    // Email configuration - replace with your actual SMTP details
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_USERNAME = "hoteltablereservation.sliit@gmail.com"; // Replace with your email
    private static final String EMAIL_PASSWORD = "zhuc luhf adtx bxas"; // Replace with your app password
    private static final boolean EMAIL_DEBUG = true; // Enable debug mode to see SMTP communication

    // QR code configuration
    private static final int QR_CODE_SIZE = 250;
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    /**
     * Sends a confirmation email to the user with QR code.
     *
     * @param user The user who made the reservation
     * @param reservation The reservation details
     * @param payment The payment details
     * @return true if email was sent successfully, false otherwise
     */
    public static boolean sendConfirmationEmail(User user, Reservation reservation, Payment payment) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            System.out.println("Cannot send email: User email is missing");
            return false;
        }

        try {
            // Set up mail properties with enhanced settings for Gmail
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", SMTP_HOST);
            properties.put("mail.smtp.port", SMTP_PORT);
            properties.put("mail.smtp.ssl.trust", SMTP_HOST);
            properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
            properties.put("mail.debug", EMAIL_DEBUG);

            // Create session with authentication
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
                }
            });

            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_USERNAME, "Gourmet Reserve"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user.getEmail()));
            message.setSubject("Reservation Confirmation - Gourmet Reserve");
            message.setSentDate(new Date());

            // Create QR code content
            String qrContent = QRCodeGenerator.createQRCodeContent(
                    reservation.getId(),
                    payment.getId(),
                    user.getId());

            // Generate QR code and save to temp file
            String qrCodeFilePath = TEMP_DIR + File.separator + "qrcode_" + reservation.getId() + ".png";
            boolean qrGenerated = QRCodeGenerator.saveQRCodeToFile(qrContent, qrCodeFilePath, QR_CODE_SIZE, QR_CODE_SIZE);

            if (!qrGenerated) {
                System.err.println("Failed to generate QR code for reservation: " + reservation.getId());
                // Continue without QR code
            }

            // Create multipart message
            Multipart multipart = new MimeMultipart();

            // Create HTML content part
            String htmlContent = createEmailContent(user, reservation, payment, qrContent);
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // Add QR code as attachment if it was generated
            File qrCodeFile = new File(qrCodeFilePath);
            if (qrGenerated && qrCodeFile.exists()) {
                MimeBodyPart qrCodePart = new MimeBodyPart();
                DataSource source = new FileDataSource(qrCodeFile);
                qrCodePart.setDataHandler(new DataHandler(source));
                qrCodePart.setFileName("reservation_qr_code.png");
                qrCodePart.setDisposition(MimeBodyPart.INLINE);
                qrCodePart.setHeader("Content-ID", "<qrcode>");
                multipart.addBodyPart(qrCodePart);
            }

            // Set the multipart content to the message
            message.setContent(multipart);

            // Send the message
            Transport.send(message);

            System.out.println("Confirmation email sent successfully to: " + user.getEmail());

            // Clean up temp file
            if (qrCodeFile.exists()) {
                qrCodeFile.delete();
            }

            return true;
        } catch (Exception e) {
            System.err.println("Error sending confirmation email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Creates the HTML content for the confirmation email with QR code.
     *
     * @param user The user who made the reservation
     * @param reservation The reservation details
     * @param payment The payment details
     * @param qrCodeContent Content for the QR code
     * @return HTML content as a string
     */
    private static String createEmailContent(User user, Reservation reservation, Payment payment, String qrCodeContent) {
        StringBuilder content = new StringBuilder();
        content.append("<html><head><style>");
        content.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        content.append("h1 { color: #D4AF37; }");
        content.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        content.append(".header { text-align: center; padding-bottom: 20px; border-bottom: 2px solid #D4AF37; }");
        content.append(".details { margin: 30px 0; padding: 20px; background-color: #f9f9f9; border-radius: 5px; }");
        content.append(".qr-container { text-align: center; margin: 30px 0; }");
        content.append(".footer { text-align: center; font-size: 12px; margin-top: 40px; color: #777; }");
        content.append(".detail-row { margin-bottom: 10px; }");
        content.append(".detail-label { font-weight: bold; display: inline-block; width: 150px; }");
        content.append("</style></head><body>");

        content.append("<div class='container'>");
        content.append("<div class='header'>");
        content.append("<h1>Gourmet Reserve</h1>");
        content.append("<p>Your Table Reservation is Confirmed!</p>");
        content.append("</div>");

        content.append("<p>Dear ").append(user.getUsername()).append(",</p>");
        content.append("<p>Thank you for choosing Gourmet Reserve. Your table reservation has been confirmed and payment has been processed successfully.</p>");

        // Reservation Details
        content.append("<div class='details'>");
        content.append("<h3>Reservation Details</h3>");

        content.append("<div class='detail-row'>");
        content.append("<span class='detail-label'>Reservation ID:</span> ").append(reservation.getId());
        content.append("</div>");

        content.append("<div class='detail-row'>");
        content.append("<span class='detail-label'>Date:</span> ").append(reservation.getReservationDate());
        content.append("</div>");

        content.append("<div class='detail-row'>");
        content.append("<span class='detail-label'>Time:</span> ").append(reservation.getReservationTime());
        content.append("</div>");

        content.append("<div class='detail-row'>");
        content.append("<span class='detail-label'>Duration:</span> ").append(reservation.getDuration()).append(" hours");
        content.append("</div>");

        // Extract table information
        String tableType = "Standard";
        if (reservation.getTableId() != null) {
            char tableTypeChar = reservation.getTableId().charAt(0);
            if (tableTypeChar == 'f') tableType = "Family";
            else if (tableTypeChar == 'l') tableType = "Luxury";
            else if (tableTypeChar == 'c') tableType = "Couple";
            else if (tableTypeChar == 'r') tableType = "Regular";

            content.append("<div class='detail-row'>");
            content.append("<span class='detail-label'>Table Type:</span> ").append(tableType);
            content.append("</div>");

            content.append("<div class='detail-row'>");
            content.append("<span class='detail-label'>Table ID:</span> ").append(reservation.getTableId());
            content.append("</div>");
        }

        if (payment.getAmount() != null) {
            content.append("<div class='detail-row'>");
            content.append("<span class='detail-label'>Amount Paid:</span> ").append(payment.getAmount()).append(" ").append(payment.getCurrency());
            content.append("</div>");
        }

        content.append("</div>"); // End of details div

        // QR Code section
        content.append("<div class='qr-container'>");
        content.append("<p>Please show this QR code when you arrive at the restaurant for check-in:</p>");
        content.append("<img src='cid:qrcode' alt='Reservation QR Code' style='width: 200px; height: 200px;'/>");
        content.append("<p>Alternatively, you can provide your Reservation ID: <strong>").append(reservation.getId()).append("</strong></p>");
        content.append("</div>");

        // Instructions
        content.append("<p>We recommend arriving 15 minutes before your reservation time. If you need to make any changes to your reservation, please contact us at least 24 hours in advance.</p>");

        content.append("<p>We look forward to serving you at Gourmet Reserve!</p>");

        // Footer
        content.append("<div class='footer'>");
        content.append("<p>This is an automated message, please do not reply to this email.</p>");
        content.append("<p>If you need assistance, please contact us at support@gourmetreserve.com or call (123) 456-7890.</p>");
        content.append("<p>&copy; 2025 Gourmet Reserve. All rights reserved.</p>");
        content.append("</div>");

        content.append("</div></body></html>");

        return content.toString();
    }

    /**
     * Sends a test email to verify the email service is working properly.
     *
     * @param toEmail Email address to send the test to
     * @return true if email was sent successfully, false otherwise
     */
    public static boolean sendTestEmail(String toEmail) {
        try {
            System.out.println("Attempting to send test email to: " + toEmail);

            // Set up mail properties with enhanced settings for Gmail
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", SMTP_HOST);
            properties.put("mail.smtp.port", SMTP_PORT);
            properties.put("mail.smtp.ssl.trust", SMTP_HOST);
            properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
            properties.put("mail.debug", "true"); // Always enable debug for test emails

            System.out.println("Email properties configured");
            System.out.println("Using username: " + EMAIL_USERNAME);
            System.out.println("SMTP host: " + SMTP_HOST + ", Port: " + SMTP_PORT);

            // Create session with authentication
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
                }
            });

            System.out.println("Session created with authentication");

            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_USERNAME, "Gourmet Reserve"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Test Email - Gourmet Reserve");
            message.setText("This is a test email to verify that the email service is working properly. Time: " + new Date());
            message.setSentDate(new Date());

            System.out.println("Message prepared, attempting to send...");

            // Send the message
            Transport.send(message);

            System.out.println("Test email sent successfully to: " + toEmail);
            return true;
        } catch (Exception e) {
            System.err.println("Error sending test email: " + e.getMessage());
            e.printStackTrace();

            // Provide more detailed error information
            if (e instanceof javax.mail.MessagingException) {
                System.err.println("This is a JavaMail messaging exception, which typically indicates:");
                System.err.println("1. Authentication issues (incorrect username/password)");
                System.err.println("2. Connection issues (incorrect server/port)");
                System.err.println("3. SSL/TLS configuration problems");

                if (e.getMessage().contains("535")) {
                    System.err.println("Error 535 indicates authentication failure. Check your username and app password.");
                    System.err.println("Remember that for Gmail, you need to:");
                    System.err.println("1. Enable 2-Step Verification on your Google account");
                    System.err.println("2. Generate an App Password for this application");
                    System.err.println("3. Use that App Password instead of your regular Google password");
                }
            }

            return false;
        }
    }

    /**
     * Utility method to diagnose email configuration and test the connection
     * This can be called directly from other parts of the application for debugging
     */
    public static void diagnoseEmailSetup() {
        System.out.println("========== EMAIL DIAGNOSTICS ==========");
        System.out.println("Email Username: " + EMAIL_USERNAME);
        System.out.println("Email Password: " + (EMAIL_PASSWORD != null ? "[PROVIDED]" : "[NOT PROVIDED]"));
        System.out.println("SMTP Host: " + SMTP_HOST);
        System.out.println("SMTP Port: " + SMTP_PORT);

        // Test the connection without sending an email
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.connectiontimeout", "5000");
            props.put("mail.smtp.timeout", "5000");

            System.out.println("Attempting to connect to SMTP server...");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
                }
            });

            Transport transport = session.getTransport("smtp");
            transport.connect(SMTP_HOST, Integer.parseInt(SMTP_PORT), EMAIL_USERNAME, EMAIL_PASSWORD);
            transport.close();

            System.out.println("SMTP Connection Successful! Authentication passed.");
        } catch (Exception e) {
            System.err.println("SMTP Connection Failed: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("======================================");
    }
}