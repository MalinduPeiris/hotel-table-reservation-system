package com.tablebooknow.controller.payment;

import com.tablebooknow.dao.PaymentDAO;
import com.tablebooknow.dao.ReservationDAO;
import com.tablebooknow.dao.UserDAO;
import com.tablebooknow.dao.PaymentCardDAO;
import com.tablebooknow.model.payment.PaymentCard;
import com.tablebooknow.model.payment.Payment;
import com.tablebooknow.model.reservation.Reservation;
import com.tablebooknow.model.user.User;
import com.tablebooknow.service.PaymentGateway;
import com.tablebooknow.util.ReservationQueue;
import java.util.Enumeration;
import com.tablebooknow.util.QRCodeGenerator;
import com.tablebooknow.service.EmailService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Servlet for handling payment processing for table reservations
 */
@WebServlet("/payment/*")
public class PaymentServlet extends HttpServlet {
    private PaymentDAO paymentDAO;
    private ReservationDAO reservationDAO;
    private UserDAO userDAO;
    private PaymentGateway paymentGateway;
    private ReservationQueue reservationQueue;

    @Override
    public void init() throws ServletException {
        System.out.println("Initializing PaymentServlet");
        paymentDAO = new PaymentDAO();
        reservationDAO = new ReservationDAO();
        userDAO = new UserDAO();
        paymentGateway = new PaymentGateway();
        reservationQueue = new ReservationQueue();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        System.out.println("GET request to payment: " + pathInfo);

        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            System.out.println("User not logged in, redirecting to login page");
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        if (pathInfo == null || pathInfo.equals("/")) {
            // Default payment page - redirect to payment dashboard
            response.sendRedirect(request.getContextPath() + "/paymentcard/dashboard");
            return;
        }

        switch (pathInfo) {
            case "/initiate":
                initiatePayment(request, response);
                break;
            case "/success":
                handlePaymentSuccess(request, response);
                break;
            case "/cancel":
                handlePaymentCancel(request, response);
                break;
            case "/notify":
                handlePaymentNotification(request, response);
                break;
            default:
                System.out.println("Unknown path: " + pathInfo);
                response.sendRedirect(request.getContextPath() + "/paymentcard/dashboard");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        System.out.println("POST request to payment: " + pathInfo);

        // Debug: Log all request parameters
        System.out.println("Request parameters:");
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            for (String value : paramValues) {
                System.out.println("  " + paramName + " = " + value);
            }
        }

        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            System.out.println("User not logged in, redirecting to login page");
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        if (pathInfo == null || pathInfo.equals("/")) {
            // Default path for processing payment form
            processPaymentForm(request, response);
            return;
        }

        switch (pathInfo) {
            case "/process":
                processPaymentForm(request, response);
                break;
            case "/notify":
                // PayHere will send POST notifications to this endpoint
                handlePaymentNotification(request, response);
                break;
            default:
                System.out.println("Unknown path: " + pathInfo);
                response.sendRedirect(request.getContextPath() + "/paymentcard/dashboard");
                break;
        }
    }

    /**
     * Process the submitted payment form and redirect to PayHere
     */
    private void processPaymentForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Processing payment form");

        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");

        // Get payment card ID from session
        String paymentCardId = (String) session.getAttribute("paymentCardId");

        // Get reservation from session
        String reservationId = (String) session.getAttribute("reservationId");
        if (reservationId == null) {
            reservationId = request.getParameter("reservationId");
        }

        if (reservationId == null) {
            System.out.println("No reservation ID found, redirecting to date selection");
            response.sendRedirect(request.getContextPath() + "/reservation/dateSelection");
            return;
        }

        try {
            Reservation reservation = reservationDAO.findById(reservationId);
            if (reservation == null) {
                request.setAttribute("errorMessage", "Reservation not found");
                request.getRequestDispatcher("/paymentcard/dashboard").forward(request, response);
                return;
            }

            User user = userDAO.findById(userId);
            if (user == null) {
                request.setAttribute("errorMessage", "User not found");
                request.getRequestDispatcher("/paymentcard/dashboard").forward(request, response);
                return;
            }

            // Extract table type from table ID
            String tableId = reservation.getTableId();
            String tableType = "regular"; // Default
            if (tableId != null && !tableId.isEmpty()) {
                char typeChar = tableId.charAt(0);
                if (typeChar == 'f') tableType = "family";
                else if (typeChar == 'l') tableType = "luxury";
                else if (typeChar == 'c') tableType = "couple";
                else if (typeChar == 'r') tableType = "regular";
            }

            // Calculate payment amount based on table type and duration
            int duration = reservation.getDuration();
            BigDecimal amount = paymentGateway.calculateAmount(tableType, duration);

            // Create payment record
            Payment payment = new Payment();
            payment.setUserId(userId);
            payment.setReservationId(reservationId);
            payment.setAmount(amount);
            payment.setCurrency("USD"); // Use USD currency to match pricing
            payment.setStatus("PENDING");
            payment.setPaymentGateway("PayHere");

            // Set the payment method if we have a card
            if (paymentCardId != null) {
                try {
                    PaymentCardDAO paymentCardDAO = new PaymentCardDAO();
                    PaymentCard card = paymentCardDAO.findById(paymentCardId);
                    if (card != null) {
                        payment.setPaymentMethod("Card - " + card.getCardType().toUpperCase());
                    }
                } catch (Exception e) {
                    System.err.println("Error getting payment card details: " + e.getMessage());
                }
            }

            // Save payment to get an ID
            paymentDAO.create(payment);

            // Store the payment ID in session for reference
            session.setAttribute("paymentId", payment.getId());

            // Generate URLs for PayHere
            String baseUrl = request.getRequestURL().toString();
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/payment/"));

            String returnUrl = baseUrl + "/payment/success";
            String cancelUrl = baseUrl + "/payment/cancel";
            String notifyUrl = baseUrl + "/payment/notify";

            // Generate form parameters for PayHere
            Map<String, String> params = paymentGateway.generateFormParameters(
                    payment, reservation, user, returnUrl, cancelUrl, notifyUrl, paymentCardId
            );

            // Store the parameters for the JSP to create the form
            request.setAttribute("paymentParams", params);
            request.setAttribute("checkoutUrl", paymentGateway.getCheckoutUrl());

            // Check if this is a simulation request
            String simulateParam = request.getParameter("simulatePayment");
            if (simulateParam != null) {
                request.setAttribute("simulatePayment", simulateParam);
            }

            // Forward to payment processing JSP
            request.getRequestDispatcher("/paymentProcess.jsp").forward(request, response);

        } catch (Exception e) {
            System.err.println("Error processing payment: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error processing payment: " + e.getMessage());
            request.getRequestDispatcher("/paymentcard/dashboard").forward(request, response);
        }
    }

    /**
     * Initiate a payment for a reservation
     */
    private void initiatePayment(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Initiating payment");

        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");

        // Debug info
        System.out.println("All session attributes:");
        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            System.out.println("  " + name + ": " + session.getAttribute(name));
        }

        String reservationId = request.getParameter("reservationId");
        if (reservationId == null) {
            reservationId = (String) session.getAttribute("reservationId");
            System.out.println("Retrieved reservationId from session: " + reservationId);
        } else {
            System.out.println("Retrieved reservationId from request parameters: " + reservationId);
        }

        if (reservationId == null) {
            System.out.println("No reservation ID found, redirecting to date selection");
            response.sendRedirect(request.getContextPath() + "/reservation/dateSelection");
            return;
        }

        // Store reservation ID in session (in case it came from request parameters)
        session.setAttribute("reservationId", reservationId);
        System.out.println("Stored reservationId in session: " + reservationId);

        // Include reservation details for the payment page
        try {
            Reservation reservation = reservationDAO.findById(reservationId);
            if (reservation != null) {
                System.out.println("Found reservation: " + reservation);
                request.setAttribute("reservation", reservation);

                // Extract table information for display
                if (reservation.getTableId() != null) {
                    String tableId = reservation.getTableId();
                    char typeChar = tableId.charAt(0);
                    String tableType = "Regular";
                    if (typeChar == 'f') tableType = "Family";
                    else if (typeChar == 'l') tableType = "Luxury";
                    else if (typeChar == 'c') tableType = "Couple";
                    request.setAttribute("tableType", tableType);
                }
            } else {
                System.out.println("Reservation not found for ID: " + reservationId);
            }
        } catch (Exception e) {
            System.err.println("Error loading reservation: " + e.getMessage());
            e.printStackTrace();
            // Continue to payment page anyway
        }

        // Redirect to payment dashboard
        System.out.println("Redirecting to payment dashboard");
        response.sendRedirect(request.getContextPath() + "/paymentcard/dashboard");
    }

    // Handle successful payments
    private void handlePaymentSuccess(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Payment success callback received");

        HttpSession session = request.getSession();
        String paymentId = (String) session.getAttribute("paymentId");
        String reservationId = (String) session.getAttribute("reservationId");
        String userId = (String) session.getAttribute("userId");

        String status = request.getParameter("status_code");
        String paymentGatewayId = request.getParameter("payment_id");
        String orderId = request.getParameter("order_id");
        String simulatePayment = request.getParameter("simulatePayment");

        System.out.println("Payment status: " + status);
        System.out.println("PayHere payment ID: " + paymentGatewayId);
        System.out.println("Order ID: " + orderId);
        System.out.println("Simulation mode: " + simulatePayment);

        // Log all parameters for debugging
        System.out.println("All payment success parameters:");
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String[] values = request.getParameterValues(paramName);
            for (String value : values) {
                System.out.println("  " + paramName + ": " + value);
            }
        }

        // Check for simulation mode
        boolean isSimulation = "true".equals(simulatePayment);

        // If we're missing orderId from the request, use the one from the session
        if (orderId == null && paymentId != null) {
            orderId = paymentId;
        }

        // Validate payment
        boolean isValid = false;
        String confirmationMessage = "";
        Payment payment = null;
        Reservation reservation = null;
        User user = null;
        String qrCodeBase64 = null;

        try {
            if (isSimulation) {
                // For simulation, create a simulated payment record
                if (reservationId != null) {
                    // Extract table type from reservation
                    reservation = reservationDAO.findById(reservationId);
                    if (reservation != null) {
                        userId = (String) session.getAttribute("userId");
                        user = userDAO.findById(userId);

                        if (user != null) {
                            // Create a simulated payment
                            String tableId = reservation.getTableId();
                            String tableType = "regular";
                            if (tableId != null && !tableId.isEmpty()) {
                                char typeChar = tableId.charAt(0);
                                if (typeChar == 'f') tableType = "family";
                                else if (typeChar == 'l') tableType = "luxury";
                                else if (typeChar == 'c') tableType = "couple";
                                else if (typeChar == 'r') tableType = "regular";
                            }

                            BigDecimal amount = paymentGateway.calculateAmount(tableType, reservation.getDuration());

                            payment = new Payment();
                            payment.setUserId(userId);
                            payment.setReservationId(reservationId);
                            payment.setAmount(amount);
                            payment.setCurrency("USD");
                            payment.setStatus("COMPLETED");
                            payment.setTransactionId("SIM-" + System.currentTimeMillis());
                            payment.setPaymentGateway("Development Simulation");
                            payment.setCompletedAt(LocalDateTime.now());

                            // Check if a payment method was selected
                            String cardType = (String) session.getAttribute("cardType");
                            if (cardType != null) {
                                payment.setPaymentMethod("Card - " + cardType.toUpperCase());
                            } else {
                                payment.setPaymentMethod("Simulated Payment");
                            }

                            paymentDAO.create(payment);
                            paymentId = payment.getId();
                            session.setAttribute("paymentId", paymentId);

                            // Update reservation status
                            reservation.setStatus("confirmed");
                            reservationDAO.update(reservation);

                            // Add to reservation queue
                            reservationQueue.enqueue(reservation);

                            isValid = true;
                            confirmationMessage = "Payment successful! Your table reservation is now confirmed.";
                        }
                    }
                }
            } else if (orderId != null) {
                // For real PayHere payment
                payment = null;

                // Try to find payment by orderId (which should match our payment ID)
                try {
                    payment = paymentDAO.findById(orderId);
                } catch (Exception e) {
                    System.err.println("Error finding payment by ID: " + e.getMessage());
                }

                // If not found, try using the reservationId to find associated payments
                if (payment == null && reservationId != null) {
                    try {
                        List<Payment> payments = paymentDAO.findByReservationId(reservationId);
                        if (!payments.isEmpty()) {
                            payment = payments.get(0); // Use the first payment found
                        }
                    } catch (Exception e) {
                        System.err.println("Error finding payment by reservation: " + e.getMessage());
                    }
                }

                if (payment != null) {
                    // Update payment status and store in session
                    payment.setStatus("COMPLETED");
                    if (paymentGatewayId != null) {
                        payment.setTransactionId(paymentGatewayId);
                    }
                    payment.setCompletedAt(LocalDateTime.now());

                    paymentDAO.update(payment);

                    // Store payment ID in session
                    session.setAttribute("paymentId", payment.getId());

                    // Get the user information
                    userId = payment.getUserId();
                    if (userId != null) {
                        user = userDAO.findById(userId);
                    }

                    // Find and update the reservation
                    reservation = reservationDAO.findById(payment.getReservationId());
                    if (reservation != null) {
                        // Update reservation status
                        reservation.setStatus("confirmed");
                        reservationDAO.update(reservation);

                        // Update reservation ID in session if missing
                        if (session.getAttribute("reservationId") == null) {
                            session.setAttribute("reservationId", reservation.getId());
                        }

                        // Add to reservation queue
                        reservationQueue.enqueue(reservation);

                        isValid = true;
                        confirmationMessage = "Payment successful! Your table reservation is now confirmed.";
                    }
                }
            }

            // Generate QR code if payment was successful
            if (isValid && payment != null && reservation != null && user != null) {
                try {
                    System.out.println("Generating QR code for reservation: " + reservationId);

                    // Generate QR code content
                    String qrContent = QRCodeGenerator.createQRCodeContent(
                            reservation.getId(),
                            payment.getId(),
                            user.getId());

                    // Generate QR code as Base64 string for displaying in browser
                    qrCodeBase64 = QRCodeGenerator.createQRCodeBase64(qrContent, 250, 250);

                    // Store QR code in session for display on confirmation page
                    session.setAttribute("qrCodeBase64", qrCodeBase64);

                    confirmationMessage += " Please keep your reservation QR code for check-in.";

                    // Send confirmation email with QR code
                    try {
                        System.out.println("Attempting to send confirmation email to user: " + user.getEmail());
                        boolean emailSent = EmailService.sendConfirmationEmail(user, reservation, payment);
                        System.out.println("Email sending result: " + (emailSent ? "SUCCESS" : "FAILED"));

                        if (emailSent) {
                            confirmationMessage += " A confirmation email has been sent to your email address.";
                        } else {
                            System.err.println("Failed to send confirmation email to: " + user.getEmail());
                            confirmationMessage += " We were unable to send a confirmation email. Please contact support if needed.";
                        }
                    } catch (Exception emailEx) {
                        System.err.println("Exception sending confirmation email: " + emailEx.getMessage());
                        emailEx.printStackTrace();
                        confirmationMessage += " We encountered an issue sending your confirmation email.";
                    }
                } catch (Exception e) {
                    System.err.println("Error generating QR code: " + e.getMessage());
                    e.printStackTrace();
                    confirmationMessage += " Please keep your reservation ID for check-in.";
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing payment success: " + e.getMessage());
            e.printStackTrace();
            confirmationMessage = "There was an issue processing your payment. Please contact support.";
        }

        if (isValid) {
            session.setAttribute("confirmationMessage", confirmationMessage);

            // Store additional details for the confirmation page
            if (reservation != null) {
                session.setAttribute("tableId", reservation.getTableId());
                // If these were already stored earlier, no need to set them again
                session.setAttribute("reservationDate", reservation.getReservationDate());
                session.setAttribute("reservationTime", reservation.getReservationTime());
            }

            if (qrCodeBase64 != null) {
                session.setAttribute("qrCodeBase64", qrCodeBase64);
            }

            request.setAttribute("paymentSuccessful", true);

            // Redirect to confirmation page
            response.sendRedirect(request.getContextPath() + "/reservationConfirmation.jsp");
        } else {
            request.setAttribute("errorMessage", "We couldn't verify your payment. Please contact support.");
            request.setAttribute("paymentSuccessful", false);
            request.getRequestDispatcher("/paymentSuccess.jsp").forward(request, response);
        }
    }

    /**
     * Handle cancelled payment from PayHere
     */
    private void handlePaymentCancel(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Payment cancel callback received");

        HttpSession session = request.getSession();
        String paymentId = (String) session.getAttribute("paymentId");

        try {
            if (paymentId != null) {
                Payment payment = paymentDAO.findById(paymentId);
                if (payment != null) {
                    // Update payment status
                    payment.setStatus("CANCELLED");
                    paymentDAO.update(payment);
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing payment cancellation: " + e.getMessage());
        }

        request.setAttribute("errorMessage", "Payment was cancelled. Please try again.");
        request.getRequestDispatcher("/paymentcard/dashboard").forward(request, response);
    }

    /**
     * Handle payment notification from PayHere (server to server)
     */
    private void handlePaymentNotification(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Payment notification received");

        String merchantId = request.getParameter("merchant_id");
        String orderId = request.getParameter("order_id");
        String paymentId = request.getParameter("payment_id");
        String payhere_amount = request.getParameter("payhere_amount");
        String payhere_currency = request.getParameter("payhere_currency");
        String status_code = request.getParameter("status_code");
        String md5sig = request.getParameter("md5sig");

        System.out.println("Notification details - Order: " + orderId + ", Status: " + status_code);

        // Log all parameters for debugging
        System.out.println("All notification parameters:");
        request.getParameterMap().forEach((key, values) -> {
            for (String value : values) {
                System.out.println("  " + key + ": " + value);
            }
        });

        // Validate notification
        boolean isValid = paymentGateway.validateNotification(
                merchantId, orderId, paymentId, payhere_amount, payhere_currency, status_code
        );

        if (isValid && "2".equals(status_code)) { // 2 is success status in PayHere
            try {
                Payment payment = paymentDAO.findById(orderId);
                if (payment != null) {
                    // Update payment status
                    payment.setStatus("COMPLETED");
                    payment.setTransactionId(paymentId);
                    payment.setCompletedAt(LocalDateTime.now());

                    paymentDAO.update(payment);

                    // Find and update the reservation
                    Reservation reservation = reservationDAO.findById(payment.getReservationId());
                    if (reservation != null) {
                        reservation.setStatus("confirmed");
                        reservationDAO.update(reservation);

                        // Add to reservation queue
                        reservationQueue.enqueue(reservation);
                    }

                    // Send success response
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("Payment processed successfully");
                    return;
                }
            } catch (Exception e) {
                System.err.println("Error processing payment notification: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Send error response
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("Invalid payment notification");
    }
}