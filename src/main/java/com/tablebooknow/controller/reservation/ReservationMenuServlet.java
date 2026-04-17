package com.tablebooknow.controller.reservation;

import com.tablebooknow.dao.MenuItemDAO;
import com.tablebooknow.dao.ReservationDAO;
import com.tablebooknow.dao.ReservationMenuItemDAO;
import com.tablebooknow.model.menu.MenuItem;
import com.tablebooknow.model.menu.ReservationMenuItem;
import com.tablebooknow.model.reservation.Reservation;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Servlet for handling menu selection for reservations
 */
@WebServlet("/reservationMenu/*")
public class ReservationMenuServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(ReservationMenuServlet.class.getName());

    private MenuItemDAO menuItemDAO;
    private ReservationDAO reservationDAO;
    private ReservationMenuItemDAO reservationMenuItemDAO;

    @Override
    public void init() throws ServletException {
        menuItemDAO = new MenuItemDAO();
        reservationDAO = new ReservationDAO();
        reservationMenuItemDAO = new ReservationMenuItemDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "/";
        }

        switch (pathInfo) {
            case "/":
            case "/select":
                showMenuSelectionPage(request, response);
                break;
            case "/getAvailableItems":
                getAvailableMenuItems(request, response);
                break;
            case "/getSelectedItems":
                getSelectedMenuItems(request, response);
                break;
            default:
                showMenuSelectionPage(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "/";
        }

        switch (pathInfo) {
            case "/addItem":
                addMenuItem(request, response);
                break;
            case "/updateQuantity":
                updateMenuItemQuantity(request, response);
                break;
            case "/removeItem":
                removeMenuItem(request, response);
                break;
            case "/saveSelections":
                saveMenuSelections(request, response);
                break;
            case "/skipSelection":
                skipMenuSelection(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/reservationMenu/select");
                break;
        }
    }

    /**
     * Show the menu selection page for a specific reservation
     */
    private void showMenuSelectionPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get reservation ID from request parameter or session
        String reservationId = request.getParameter("reservationId");

        if (reservationId == null) {
            // Check if it's stored in session (e.g., after payment)
            reservationId = (String) request.getSession().getAttribute("reservationId");
        }

        if (reservationId == null) {
            // No reservation ID provided, redirect to reservations list
            response.sendRedirect(request.getContextPath() + "/user/reservations");
            return;
        }

        try {
            // Get reservation details
            Reservation reservation = reservationDAO.findById(reservationId);
            if (reservation == null) {
                request.setAttribute("errorMessage", "Reservation not found");
                response.sendRedirect(request.getContextPath() + "/user/reservations");
                return;
            }

            // Verify the reservation belongs to the logged-in user
            String userId = (String) request.getSession().getAttribute("userId");
            if (!reservation.getUserId().equals(userId)) {
                request.setAttribute("errorMessage", "Access denied: This reservation does not belong to your account");
                response.sendRedirect(request.getContextPath() + "/user/reservations");
                return;
            }

            // Get available menu items
            List<MenuItem> availableItems = menuItemDAO.findAllAvailable();

            // Group by category for easier display
            Map<String, List<MenuItem>> itemsByCategory = new HashMap<>();
            for (MenuItem item : availableItems) {
                String category = item.getCategory();
                if (!itemsByCategory.containsKey(category)) {
                    itemsByCategory.put(category, new ArrayList<>());
                }
                itemsByCategory.get(category).add(item);
            }

            // Get already selected items for this reservation
            Map<MenuItem, Integer> selectedItems = reservationMenuItemDAO.findMenuItemsForReservation(reservationId);

            // Set as request attributes
            request.setAttribute("reservation", reservation);
            request.setAttribute("itemsByCategory", itemsByCategory);
            request.setAttribute("selectedItems", selectedItems);

            // Forward to the JSP
            request.getRequestDispatcher("/menu-selection.jsp").forward(request, response);

        } catch (Exception e) {
            logger.severe("Error showing menu selection page: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error loading menu selection page: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/user/reservations");
        }
    }

    /**
     * Ajax endpoint to get available menu items as JSON
     */
    private void getAvailableMenuItems(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Get available menu items
            List<MenuItem> availableItems = menuItemDAO.findAllAvailable();

            // Convert to JSON using Gson
            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(availableItems);

            // Send JSON response
            PrintWriter out = response.getWriter();
            out.print(json);
            out.flush();

        } catch (Exception e) {
            logger.severe("Error getting available menu items: " + e.getMessage());
            e.printStackTrace();

            // Return error JSON
            PrintWriter out = response.getWriter();
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
            out.flush();
        }
    }

    /**
     * Ajax endpoint to get selected menu items for a reservation as JSON
     */
    private void getSelectedMenuItems(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Get reservation ID
        String reservationId = request.getParameter("reservationId");
        if (reservationId == null || reservationId.isEmpty()) {
            PrintWriter out = response.getWriter();
            out.print("{\"error\": \"Reservation ID is required\"}");
            out.flush();
            return;
        }

        try {
            // Get selected items for this reservation
            List<ReservationMenuItem> selectedItems = reservationMenuItemDAO.findByReservationId(reservationId);

            // Convert to JSON using Gson
            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(selectedItems);

            // Send JSON response
            PrintWriter out = response.getWriter();
            out.print(json);
            out.flush();

        } catch (Exception e) {
            logger.severe("Error getting selected menu items: " + e.getMessage());
            e.printStackTrace();

            // Return error JSON
            PrintWriter out = response.getWriter();
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
            out.flush();
        }
    }

    /**
     * Add a menu item to a reservation
     */
    private void addMenuItem(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Get parameters
        String reservationId = request.getParameter("reservationId");
        String menuItemId = request.getParameter("menuItemId");
        String quantityStr = request.getParameter("quantity");
        String specialInstructions = request.getParameter("specialInstructions");

        // Validate input
        if (reservationId == null || menuItemId == null || quantityStr == null) {
            PrintWriter out = response.getWriter();
            out.print("{\"success\": false, \"message\": \"Missing required parameters\"}");
            out.flush();
            return;
        }

        try {
            // Parse quantity
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                throw new NumberFormatException("Quantity must be greater than zero");
            }

            // Create new reservation menu item
            ReservationMenuItem item = new ReservationMenuItem();
            item.setReservationId(reservationId);
            item.setMenuItemId(menuItemId);
            item.setQuantity(quantity);
            item.setSpecialInstructions(specialInstructions);

            // Save to database
            reservationMenuItemDAO.create(item);

            // Return success JSON
            PrintWriter out = response.getWriter();
            out.print("{\"success\": true, \"message\": \"Menu item added successfully\", \"itemId\": \"" + item.getId() + "\"}");
            out.flush();

        } catch (NumberFormatException e) {
            logger.warning("Invalid quantity format: " + e.getMessage());

            // Return error JSON
            PrintWriter out = response.getWriter();
            out.print("{\"success\": false, \"message\": \"Invalid quantity: " + e.getMessage() + "\"}");
            out.flush();

        } catch (Exception e) {
            logger.severe("Error adding menu item: " + e.getMessage());
            e.printStackTrace();

            // Return error JSON
            PrintWriter out = response.getWriter();
            out.print("{\"success\": false, \"message\": \"Error adding menu item: " + e.getMessage() + "\"}");
            out.flush();
        }
    }

    /**
     * Update the quantity of a menu item in a reservation
     */
    private void updateMenuItemQuantity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Get parameters
        String reservationMenuItemId = request.getParameter("reservationMenuItemId");
        String quantityStr = request.getParameter("quantity");

        // Validate input
        if (reservationMenuItemId == null || quantityStr == null) {
            PrintWriter out = response.getWriter();
            out.print("{\"success\": false, \"message\": \"Missing required parameters\"}");
            out.flush();
            return;
        }

        try {
            // Parse quantity
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                throw new NumberFormatException("Quantity must be greater than zero");
            }

            // Get the reservation menu item
            ReservationMenuItem item = reservationMenuItemDAO.findById(reservationMenuItemId);
            if (item == null) {
                PrintWriter out = response.getWriter();
                out.print("{\"success\": false, \"message\": \"Item not found\"}");
                out.flush();
                return;
            }

            // Update quantity
            item.setQuantity(quantity);

            // Save to database
            boolean success = reservationMenuItemDAO.update(item);

            // Return JSON response
            PrintWriter out = response.getWriter();
            if (success) {
                out.print("{\"success\": true, \"message\": \"Quantity updated successfully\"}");
            } else {
                out.print("{\"success\": false, \"message\": \"Failed to update quantity\"}");
            }
            out.flush();

        } catch (NumberFormatException e) {
            logger.warning("Invalid quantity format: " + e.getMessage());

            // Return error JSON
            PrintWriter out = response.getWriter();
            out.print("{\"success\": false, \"message\": \"Invalid quantity: " + e.getMessage() + "\"}");
            out.flush();

        } catch (Exception e) {
            logger.severe("Error updating menu item quantity: " + e.getMessage());
            e.printStackTrace();

            // Return error JSON
            PrintWriter out = response.getWriter();
            out.print("{\"success\": false, \"message\": \"Error updating quantity: " + e.getMessage() + "\"}");
            out.flush();
        }
    }

    /**
     * Remove a menu item from a reservation
     */
    private void removeMenuItem(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Get parameters
        String reservationMenuItemId = request.getParameter("reservationMenuItemId");

        // Validate input
        if (reservationMenuItemId == null) {
            PrintWriter out = response.getWriter();
            out.print("{\"success\": false, \"message\": \"Missing item ID parameter\"}");
            out.flush();
            return;
        }

        try {
            // Delete from database
            boolean success = reservationMenuItemDAO.delete(reservationMenuItemId);

            // Return JSON response
            PrintWriter out = response.getWriter();
            if (success) {
                out.print("{\"success\": true, \"message\": \"Menu item removed successfully\"}");
            } else {
                out.print("{\"success\": false, \"message\": \"Failed to remove menu item\"}");
            }
            out.flush();

        } catch (Exception e) {
            logger.severe("Error removing menu item: " + e.getMessage());
            e.printStackTrace();

            // Return error JSON
            PrintWriter out = response.getWriter();
            out.print("{\"success\": false, \"message\": \"Error removing menu item: " + e.getMessage() + "\"}");
            out.flush();
        }
    }

    /**
     * Save menu selections and proceed to next step
     */
    private void saveMenuSelections(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get parameters
        String reservationId = request.getParameter("reservationId");

        // Validate input
        if (reservationId == null) {
            request.getSession().setAttribute("errorMessage", "Missing reservation ID");
            response.sendRedirect(request.getContextPath() + "/user/reservations");
            return;
        }

        try {
            // Verify the user has at least one menu item selected
            List<ReservationMenuItem> selectedItems = reservationMenuItemDAO.findByReservationId(reservationId);
            if (selectedItems.isEmpty()) {
                request.setAttribute("errorMessage", "Please select at least one menu item or click 'Skip Menu Selection'");
                request.getRequestDispatcher("/reservationMenu/select?reservationId=" + reservationId).forward(request, response);
                return;
            }

            // Set success message
            request.getSession().setAttribute("successMessage", "Menu selections saved successfully!");

            // Redirect to reservation details or confirmation page
            response.sendRedirect(request.getContextPath() + "/user/reservations");

        } catch (Exception e) {
            logger.severe("Error saving menu selections: " + e.getMessage());
            e.printStackTrace();

            request.setAttribute("errorMessage", "Error saving menu selections: " + e.getMessage());
            request.getRequestDispatcher("/reservationMenu/select?reservationId=" + reservationId).forward(request, response);
        }
    }

    /**
     * Skip menu selection and proceed to next step
     */
    private void skipMenuSelection(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get parameters
        String reservationId = request.getParameter("reservationId");

        // Validate input
        if (reservationId == null) {
            request.getSession().setAttribute("errorMessage", "Missing reservation ID");
            response.sendRedirect(request.getContextPath() + "/user/reservations");
            return;
        }

        try {
            // Remove any existing menu selections for this reservation
            reservationMenuItemDAO.deleteByReservationId(reservationId);

            // Set success message
            request.getSession().setAttribute("successMessage", "Menu selection skipped. You can add menu items later if you wish.");

            // Redirect to reservation details or confirmation page
            response.sendRedirect(request.getContextPath() + "/user/reservations");

        } catch (Exception e) {
            logger.severe("Error skipping menu selection: " + e.getMessage());
            e.printStackTrace();

            request.setAttribute("errorMessage", "Error skipping menu selection: " + e.getMessage());
            request.getRequestDispatcher("/reservationMenu/select?reservationId=" + reservationId).forward(request, response);
        }
    }
}