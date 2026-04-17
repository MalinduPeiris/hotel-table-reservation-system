package com.tablebooknow.test;

import com.tablebooknow.dao.PaymentDAO;
import com.tablebooknow.dao.ReservationDAO;
import com.tablebooknow.dao.UserDAO;
import com.tablebooknow.model.payment.Payment;
import com.tablebooknow.model.reservation.Reservation;
import com.tablebooknow.model.user.User;
import com.tablebooknow.service.EmailService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Test servlet for email functionality.
 * Only accessible to admin users and in development environments.
 */
@WebServlet("/admin/test/email/*")
public class EmailTestServlet extends HttpServlet {

    // Set this to true for development/testing, false for production
    private static final boolean DEVELOPMENT_MODE = true;

    private UserDAO userDAO;
    private ReservationDAO reservationDAO;
    private PaymentDAO paymentDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
        reservationDAO = new ReservationDAO();
        paymentDAO = new PaymentDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Security check - only admin users should be able to access this
        HttpSession session = request.getSession(false);
        boolean isAdmin = session != null && Boolean.TRUE.equals(session.getAttribute("isAdmin"));

        // Development mode bypass for testing
        if (DEVELOPMENT_MODE) {
            // Create session if it doesn't exist and set admin flag
            if (session == null) {
                session = request.getSession(true);
                session.setAttribute("isAdmin", Boolean.TRUE);
            } else if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
                session.setAttribute("isAdmin", Boolean.TRUE);
            }
            isAdmin = true;

            // Log the bypass
            System.out.println("WARNING: Development mode is enabled. Admin access check bypassed.");
        }

        if (!isAdmin) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "/";
        }

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Email Test Utility</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; line-height: 1.6; }");
        out.println("h1, h2 { color: #333; }");
        out.println(".section { margin-bottom: 30px; padding: 15px; background-color: #f9f9f9; border-radius: 5px; border: 1px solid #ddd; }");
        out.println(".success { color: green; }");
        out.println(".error { color: red; }");
        out.println(".code { background: #f0f0f0; padding: 10px; border-radius: 5px; font-family: monospace; overflow-x: auto; }");
        out.println(".development-warning { background-color: #fff3cd; color: #856404; padding: 10px; border-radius: 5px; margin-bottom: 20px; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");

        // Show development mode warning
        if (DEVELOPMENT_MODE) {
            out.println("<div class='development-warning'>");
            out.println("<strong>⚠️ Development Mode Enabled</strong>");
            out.println("<p>Admin access check has been bypassed. Disable this before deploying to production.</p>");
            out.println("</div>");
        }

        out.println("<h1>Email Test Utility</h1>");

        try {
            switch (pathInfo) {
                case "/":
                    showMainPage(out);
                    break;
                case "/test-simple":
                    testSimpleEmail(request, response, out);
                    break;
                case "/test-reservation":
                    testReservationEmail(request, response, out);
                    break;
                case "/diagnose":
                    diagnoseEmailSetup(out);
                    break;
                default:
                    out.println("<p class='error'>Unknown action: " + pathInfo + "</p>");
                    showMainPage(out);
            }
        } catch (Exception e) {
            out.println("<div class='section'>");
            out.println("<h2 class='error'>Error</h2>");
            out.println("<p>" + e.getMessage() + "</p>");
            out.println("<pre class='code'>");
            e.printStackTrace(out);
            out.println("</pre>");
            out.println("</div>");
        }

        out.println("</body>");
        out.println("</html>");
    }

    private void showMainPage(PrintWriter out) {
        out.println("<div class='section'>");
        out.println("<h2>Email Testing Options</h2>");
        out.println("<ul>");
        out.println("<li><a href='email/diagnose'>Diagnose Email Setup</a> - Check configuration and connection</li>");
        out.println("<li><a href='email/test-simple?email=your-email@example.com'>Send Test Email</a> - Send a simple test email</li>");
        out.println("<li><a href='email/test-reservation'>Test Reservation Confirmation Email</a> - Send a test reservation confirmation</li>");
        out.println("</ul>");
        out.println("</div>");
    }

    private void testSimpleEmail(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        String email = request.getParameter("email");
        if (email == null || email.isEmpty()) {
            out.println("<div class='section'>");
            out.println("<h2>Send Test Email</h2>");
            out.println("<form action='test-simple' method='get'>");
            out.println("<p>Enter email address to send test to:</p>");
            out.println("<input type='email' name='email' required style='width: 300px;'>");
            out.println("<button type='submit'>Send Test Email</button>");
            out.println("</form>");
            out.println("</div>");
            return;
        }

        out.println("<div class='section'>");
        out.println("<h2>Sending Test Email</h2>");
        out.println("<p>Attempting to send test email to: " + email + "</p>");

        boolean success = EmailService.sendTestEmail(email);

        if (success) {
            out.println("<p class='success'>Test email sent successfully! Please check your inbox and spam folder.</p>");
        } else {
            out.println("<p class='error'>Failed to send test email. Check server logs for details.</p>");
        }

        out.println("<p><a href='email'>Back to main menu</a></p>");
        out.println("</div>");
    }

    private void testReservationEmail(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        String userId = request.getParameter("userId");
        String reservationId = request.getParameter("reservationId");
        String paymentId = request.getParameter("paymentId");

        if (userId == null || reservationId == null || paymentId == null) {
            // Show selection form
            out.println("<div class='section'>");
            out.println("<h2>Test Reservation Email</h2>");

            try {
                // Get list of users
                out.println("<form action='test-reservation' method='get'>");
                out.println("<p>Select a user:</p>");
                out.println("<select name='userId' style='width: 300px;'>");

                for (User user : userDAO.findAll()) {
                    if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                        out.println("<option value='" + user.getId() + "'>" + user.getUsername() + " (" + user.getEmail() + ")</option>");
                    }
                }

                out.println("</select>");

                // Get list of reservations
                out.println("<p>Select a reservation:</p>");
                out.println("<select name='reservationId' style='width: 300px;'>");

                for (Reservation reservation : reservationDAO.findAll()) {
                    out.println("<option value='" + reservation.getId() + "'>" +
                            reservation.getReservationDate() + " at " + reservation.getReservationTime() +
                            " (Table: " + reservation.getTableId() + ")</option>");
                }

                out.println("</select>");

                // Get list of payments
                out.println("<p>Select a payment:</p>");
                out.println("<select name='paymentId' style='width: 300px;'>");

                for (Payment payment : paymentDAO.findAll()) {
                    out.println("<option value='" + payment.getId() + "'>" + payment.getId() +
                            (payment.getAmount() != null ? " ($" + payment.getAmount() + ")" : "") + "</option>");
                }

                out.println("</select>");

                out.println("<p><button type='submit'>Send Test Email</button></p>");
                out.println("</form>");

            } catch (Exception e) {
                out.println("<p class='error'>Error loading test data: " + e.getMessage() + "</p>");
                e.printStackTrace();
            }

            out.println("</div>");
            return;
        }

        // Send test reservation email
        try {
            User user = userDAO.findById(userId);
            Reservation reservation = reservationDAO.findById(reservationId);
            Payment payment = paymentDAO.findById(paymentId);

            if (user == null || reservation == null || payment == null) {
                out.println("<div class='section'>");
                out.println("<h2>Error</h2>");
                out.println("<p class='error'>Could not find user, reservation, or payment with the provided IDs.</p>");
                out.println("</div>");
                return;
            }

            out.println("<div class='section'>");
            out.println("<h2>Sending Reservation Confirmation Email</h2>");
            out.println("<p>Attempting to send reservation confirmation email to: " + user.getEmail() + "</p>");

            boolean success = EmailService.sendConfirmationEmail(user, reservation, payment);

            if (success) {
                out.println("<p class='success'>Reservation confirmation email sent successfully! Please check your inbox and spam folder.</p>");
            } else {
                out.println("<p class='error'>Failed to send confirmation email. Check server logs for details.</p>");
            }

            out.println("<p><a href='email'>Back to main menu</a></p>");
            out.println("</div>");

        } catch (Exception e) {
            out.println("<div class='section'>");
            out.println("<h2>Error</h2>");
            out.println("<p class='error'>Error sending test reservation email: " + e.getMessage() + "</p>");
            out.println("<pre class='code'>");
            e.printStackTrace(out);
            out.println("</pre>");
            out.println("</div>");
        }
    }

    private void diagnoseEmailSetup(PrintWriter out) {
        out.println("<div class='section'>");
        out.println("<h2>Email Configuration Diagnostics</h2>");

        out.println("<p>Running email setup diagnosis...</p>");
        out.println("<pre class='code'>");

        // Save the original System.out and System.err
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        try {
            // Create custom PrintStream objects that redirect to the HttpServletResponse's PrintWriter
            PrintStream customOut = new PrintStream(new java.io.OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    out.write(b);
                    out.flush();
                }
            });

            PrintStream customErr = new PrintStream(new java.io.OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    out.write(b);
                    out.flush();
                }
            });

            // Set the custom PrintStream objects as System.out and System.err
            System.setOut(customOut);
            System.setErr(customErr);

            // Run the diagnostics
            EmailService.diagnoseEmailSetup();

        } finally {
            // Restore original System.out and System.err
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        out.println("</pre>");
        out.println("<p><a href='email'>Back to main menu</a></p>");
        out.println("</div>");
    }
}