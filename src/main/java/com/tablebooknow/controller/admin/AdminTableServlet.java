package com.tablebooknow.controller.admin;

import com.tablebooknow.dao.TableDAO;
import com.tablebooknow.dao.ReservationDAO;
import com.tablebooknow.model.table.Table;
import com.tablebooknow.model.reservation.Reservation;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet for admin table management with full CRUD functionality
 */
@WebServlet("/admin/tables/*")
public class AdminTableServlet extends HttpServlet {
    private TableDAO tableDAO;
    private ReservationDAO reservationDAO;

    @Override
    public void init() throws ServletException {
        tableDAO = new TableDAO();
        reservationDAO = new ReservationDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Check if admin is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }

        String pathInfo = request.getPathInfo();

        // Default path handling
        if (pathInfo == null || pathInfo.equals("/")) {
            listAllTables(request, response);
            return;
        }

        // Handle specific paths
        switch (pathInfo) {
            case "/view":
                viewTable(request, response);
                break;
            case "/edit":
                showEditForm(request, response);
                break;
            case "/add":
                showAddForm(request, response);
                break;
            case "/floor":
                listTablesByFloor(request, response);
                break;
            default:
                listAllTables(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Check if admin is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }

        String pathInfo = request.getPathInfo();

        // Default path handling
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendRedirect(request.getContextPath() + "/admin/tables");
            return;
        }

        // Handle specific paths
        switch (pathInfo) {
            case "/create":
                createTable(request, response);
                break;
            case "/update":
                updateTable(request, response);
                break;
            case "/delete":
                deleteTable(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/admin/tables");
                break;
        }
    }

    private void listAllTables(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Get search parameters
            String searchTerm = request.getParameter("search");
            String floorFilter = request.getParameter("floor");
            String typeFilter = request.getParameter("type");

            List<Table> allTables = tableDAO.findAll();
            List<Table> filteredTables = new ArrayList<>(allTables);

            // Apply type filter if provided
            if (typeFilter != null && !typeFilter.isEmpty()) {
                filteredTables = filteredTables.stream()
                        .filter(table -> table.getTableType() != null &&
                                table.getTableType().equalsIgnoreCase(typeFilter))
                        .collect(java.util.stream.Collectors.toList());
                request.setAttribute("typeFilter", typeFilter);
            }

            // Apply floor filter if provided
            if (floorFilter != null && !floorFilter.isEmpty()) {
                try {
                    int floor = Integer.parseInt(floorFilter);
                    filteredTables = filteredTables.stream()
                            .filter(table -> table.getFloor() == floor)
                            .collect(java.util.stream.Collectors.toList());
                    request.setAttribute("floorFilter", floorFilter);
                } catch (NumberFormatException e) {
                    // Invalid floor filter, ignore
                }
            }

            // Apply search if provided
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String search = searchTerm.toLowerCase();
                filteredTables = filteredTables.stream()
                        .filter(table ->
                                (table.getTableNumber() != null && table.getTableNumber().toLowerCase().contains(search)) ||
                                        (table.getTableType() != null && table.getTableType().toLowerCase().contains(search)) ||
                                        (table.getLocationDescription() != null && table.getLocationDescription().toLowerCase().contains(search)) ||
                                        (table.getId() != null && table.getId().toLowerCase().contains(search)))
                        .collect(java.util.stream.Collectors.toList());
                request.setAttribute("searchTerm", searchTerm);
            }

            request.setAttribute("tables", filteredTables);
            request.setAttribute("tableCount", filteredTables.size());
            request.setAttribute("totalTables", allTables.size());

            request.getRequestDispatcher("/admin-tables.jsp").forward(request, response);

        } catch (Exception e) {
            System.err.println("Error in listAllTables: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error loading tables: " + e.getMessage());
            request.getRequestDispatcher("/admin-tables.jsp").forward(request, response);
        }
    }

    private void listTablesByFloor(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String floorParam = request.getParameter("floorNumber");

        if (floorParam == null || floorParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/tables");
            return;
        }

        try {
            int floor = Integer.parseInt(floorParam);
            List<Table> floorTables = tableDAO.findByFloor(floor);

            request.setAttribute("tables", floorTables);
            request.setAttribute("tableCount", floorTables.size());
            request.setAttribute("floorFilter", floorParam);
            request.setAttribute("floorTitle", "Floor " + floor + " Tables");

            request.getRequestDispatcher("/admin-tables.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "Invalid floor number");
            response.sendRedirect(request.getContextPath() + "/admin/tables");
        }
    }

    private void viewTable(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String tableId = request.getParameter("id");
        if (tableId == null || tableId.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/tables");
            return;
        }

        try {
            Table table = tableDAO.findById(tableId);
            if (table == null) {
                request.setAttribute("errorMessage", "Table not found");
                response.sendRedirect(request.getContextPath() + "/admin/tables");
                return;
            }

            // Get all reservations for this table
            List<Reservation> tableReservations = new ArrayList<>();
            List<Reservation> allReservations = reservationDAO.findAll();

            for (Reservation reservation : allReservations) {
                if (tableId.equals(reservation.getTableId())) {
                    tableReservations.add(reservation);
                }
            }

            request.setAttribute("table", table);
            request.setAttribute("tableReservations", tableReservations);
            request.getRequestDispatcher("/admin-table-details.jsp").forward(request, response);

        } catch (Exception e) {
            request.setAttribute("errorMessage", "Error loading table: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/tables");
        }
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String tableId = request.getParameter("id");
        if (tableId == null || tableId.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/tables");
            return;
        }

        try {
            Table table = tableDAO.findById(tableId);
            if (table == null) {
                request.setAttribute("errorMessage", "Table not found");
                response.sendRedirect(request.getContextPath() + "/admin/tables");
                return;
            }

            request.setAttribute("table", table);
            request.setAttribute("editMode", true);
            request.getRequestDispatcher("/admin-table-form.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Error loading table for editing: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/tables");
        }
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Just forward to the form page
        request.setAttribute("editMode", false);
        request.getRequestDispatcher("/admin-table-form.jsp").forward(request, response);
    }

    private void createTable(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Get form data
            String tableNumber = request.getParameter("tableNumber");
            String tableType = request.getParameter("tableType");
            String floorStr = request.getParameter("floor");
            String capacityStr = request.getParameter("capacity");
            String locationDescription = request.getParameter("locationDescription");
            String isActiveStr = request.getParameter("isActive");

            // Validate required fields
            if (tableNumber == null || tableNumber.trim().isEmpty() ||
                    tableType == null || tableType.trim().isEmpty() ||
                    floorStr == null || floorStr.trim().isEmpty() ||
                    capacityStr == null || capacityStr.trim().isEmpty()) {

                request.setAttribute("errorMessage", "Table number, type, floor and capacity are required");
                request.setAttribute("editMode", false);
                request.getRequestDispatcher("/admin-table-form.jsp").forward(request, response);
                return;
            }

            // Parse numeric values
            int floor = Integer.parseInt(floorStr);
            int capacity = Integer.parseInt(capacityStr);
            boolean isActive = "on".equals(isActiveStr) || "true".equals(isActiveStr);

            // Create new table
            Table newTable = new Table();
            newTable.setTableNumber(tableNumber);
            newTable.setTableType(tableType);
            newTable.setFloor(floor);
            newTable.setCapacity(capacity);
            newTable.setLocationDescription(locationDescription);
            newTable.setActive(isActive);

            // Generate system ID based on type, floor and number (e.g., f1-3)
            String systemId = generateSystemTableId(tableType, floor, tableNumber);
            newTable.setId(systemId);

            // Check if table with this ID already exists
            if (tableDAO.findById(systemId) != null) {
                request.setAttribute("errorMessage", "A table with this number and floor already exists");
                request.setAttribute("editMode", false);
                request.getRequestDispatcher("/admin-table-form.jsp").forward(request, response);
                return;
            }

            // Save to database
            Table createdTable = tableDAO.create(newTable);

            if (createdTable != null && createdTable.getId() != null) {
                request.setAttribute("successMessage", "Table created successfully");
                response.sendRedirect(request.getContextPath() + "/admin/tables/view?id=" + createdTable.getId());
            } else {
                request.setAttribute("errorMessage", "Failed to create table");
                request.setAttribute("editMode", false);
                request.getRequestDispatcher("/admin-table-form.jsp").forward(request, response);
            }
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "Invalid number format for floor or capacity");
            request.setAttribute("editMode", false);
            request.getRequestDispatcher("/admin-table-form.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Error creating table: " + e.getMessage());
            request.setAttribute("editMode", false);
            request.getRequestDispatcher("/admin-table-form.jsp").forward(request, response);
        }
    }

    /**
     * Generate a system table ID based on type, floor and number (e.g., f1-3)
     */
    private String generateSystemTableId(String tableType, int floor, String tableNumber) {
        String prefix;

        if (tableType != null && !tableType.isEmpty()) {
            // Extract first letter of table type
            prefix = tableType.substring(0, 1).toLowerCase();
        } else {
            // Default to 't' for unknown table type
            prefix = "t";
        }

        return prefix + floor + "-" + tableNumber;
    }

    private void updateTable(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String tableId = request.getParameter("tableId");
        if (tableId == null || tableId.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/tables");
            return;
        }

        try {
            Table table = tableDAO.findById(tableId);
            if (table == null) {
                request.setAttribute("errorMessage", "Table not found");
                response.sendRedirect(request.getContextPath() + "/admin/tables");
                return;
            }

            // Get form data
            String tableNumber = request.getParameter("tableNumber");
            String tableType = request.getParameter("tableType");
            String floorStr = request.getParameter("floor");
            String capacityStr = request.getParameter("capacity");
            String locationDescription = request.getParameter("locationDescription");
            String isActiveStr = request.getParameter("isActive");

            // Validate required fields
            if (tableNumber == null || tableNumber.trim().isEmpty() ||
                    tableType == null || tableType.trim().isEmpty() ||
                    floorStr == null || floorStr.trim().isEmpty() ||
                    capacityStr == null || capacityStr.trim().isEmpty()) {

                request.setAttribute("errorMessage", "Table number, type, floor and capacity are required");
                request.setAttribute("table", table);
                request.setAttribute("editMode", true);
                request.getRequestDispatcher("/admin-table-form.jsp").forward(request, response);
                return;
            }

            // Parse numeric values
            int floor = Integer.parseInt(floorStr);
            int capacity = Integer.parseInt(capacityStr);
            boolean isActive = "on".equals(isActiveStr) || "true".equals(isActiveStr);

            // Update table data
            table.setTableNumber(tableNumber);
            table.setTableType(tableType);
            table.setFloor(floor);
            table.setCapacity(capacity);
            table.setLocationDescription(locationDescription);
            table.setActive(isActive);

            // Save to database
            boolean success = tableDAO.update(table);

            if (success) {
                request.setAttribute("successMessage", "Table updated successfully");
                response.sendRedirect(request.getContextPath() + "/admin/tables/view?id=" + tableId);
            } else {
                request.setAttribute("errorMessage", "Failed to update table");
                request.setAttribute("table", table);
                request.setAttribute("editMode", true);
                request.getRequestDispatcher("/admin-table-form.jsp").forward(request, response);
            }
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "Invalid number format for floor or capacity");
            response.sendRedirect(request.getContextPath() + "/admin/tables/edit?id=" + tableId);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Error updating table: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/tables");
        }
    }

    private void deleteTable(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String tableId = request.getParameter("tableId");
        if (tableId == null || tableId.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/tables");
            return;
        }

        try {
            // Check if there are any reservations for this table
            List<Reservation> allReservations = reservationDAO.findAll();
            List<Reservation> tableReservations = new ArrayList<>();

            for (Reservation reservation : allReservations) {
                if (tableId.equals(reservation.getTableId())) {
                    tableReservations.add(reservation);
                }
            }

            if (!tableReservations.isEmpty()) {
                request.setAttribute("errorMessage",
                        "Cannot delete table with active reservations. Please cancel or delete all reservations for this table first.");
                response.sendRedirect(request.getContextPath() + "/admin/tables/view?id=" + tableId);
                return;
            }

            boolean success = tableDAO.delete(tableId);

            if (success) {
                request.setAttribute("successMessage", "Table deleted successfully");
                response.sendRedirect(request.getContextPath() + "/admin/tables");
            } else {
                request.setAttribute("errorMessage", "Failed to delete table");
                response.sendRedirect(request.getContextPath() + "/admin/tables/view?id=" + tableId);
            }
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Error deleting table: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/tables");
        }
    }
}