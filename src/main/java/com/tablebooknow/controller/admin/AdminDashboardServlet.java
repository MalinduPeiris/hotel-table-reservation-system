package com.tablebooknow.controller.admin;

import com.tablebooknow.dao.AdminDAO;
import com.tablebooknow.dao.ReservationDAO;
import com.tablebooknow.dao.UserDAO;
import com.tablebooknow.model.reservation.Reservation;
import com.tablebooknow.model.user.User;
import com.tablebooknow.dao.AdminDAO;
import com.tablebooknow.dao.ReservationDAO;
import com.tablebooknow.dao.UserDAO;
import com.tablebooknow.dao.TableDAO;
import com.tablebooknow.model.reservation.Reservation;
import com.tablebooknow.model.user.User;
import com.tablebooknow.model.table.Table;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Servlet for the admin dashboard
 */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {
    private UserDAO userDAO;
    private ReservationDAO reservationDAO;
    private TableDAO tableDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
        reservationDAO = new ReservationDAO();
        tableDAO = new TableDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Check if admin is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }

        try {
            // Get statistics for the dashboard
            int totalUsers = userDAO.findAll().size();

            List<Reservation> allReservations = reservationDAO.findAll();
            int totalReservations = allReservations.size();

            // Get table statistics
            List<Table> allTables = tableDAO.findAll();
            int totalTables = allTables.size();

            // Count tables by type
            int familyTables = 0;
            int luxuryTables = 0;
            int regularTables = 0;
            int coupleTables = 0;

            for (Table table : allTables) {
                String type = table.getTableType();
                if (type != null) {
                    if (type.equalsIgnoreCase("family")) {
                        familyTables++;
                    } else if (type.equalsIgnoreCase("luxury")) {
                        luxuryTables++;
                    } else if (type.equalsIgnoreCase("regular")) {
                        regularTables++;
                    } else if (type.equalsIgnoreCase("couple")) {
                        coupleTables++;
                    }
                }
            }

            // Count reservations by status
            int pendingReservations = 0;
            int confirmedReservations = 0;
            int cancelledReservations = 0;

            for (Reservation reservation : allReservations) {
                String status = reservation.getStatus();
                if ("pending".equals(status)) {
                    pendingReservations++;
                } else if ("confirmed".equals(status)) {
                    confirmedReservations++;
                } else if ("cancelled".equals(status)) {
                    cancelledReservations++;
                }
            }

            // Get upcoming reservations (for today and future)
            List<Reservation> upcomingReservations =
                    reservationDAO.findUpcomingReservations(
                            LocalDate.now().toString(),
                            LocalTime.now().toString()
                    );

            // Set attributes for the dashboard
            request.setAttribute("totalUsers", totalUsers);
            request.setAttribute("totalReservations", totalReservations);
            request.setAttribute("pendingReservations", pendingReservations);
            request.setAttribute("confirmedReservations", confirmedReservations);
            request.setAttribute("cancelledReservations", cancelledReservations);
            request.setAttribute("upcomingReservations", upcomingReservations);

            // Set table statistics
            request.setAttribute("totalTables", totalTables);
            request.setAttribute("familyTables", familyTables);
            request.setAttribute("luxuryTables", luxuryTables);
            request.setAttribute("regularTables", regularTables);
            request.setAttribute("coupleTables", coupleTables);

            // Forward to dashboard JSP
            request.getRequestDispatcher("/admin-dashboard.jsp").forward(request, response);

        } catch (Exception e) {
            request.setAttribute("errorMessage", "Error loading dashboard: " + e.getMessage());
            request.getRequestDispatcher("/admin-dashboard.jsp").forward(request, response);
        }
    }
}