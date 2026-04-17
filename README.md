# 🏨 Hotel Table Reservation System - Backend Analysis

This document provides a comprehensive analysis of the backend components of the "Hotel Table Reservation System," highlighting the Object-Oriented Programming (OOP) concepts implemented throughout the codebase.

## 📋 Table of Contents

1. [System Overview](#system-overview)
2. [Core Components](#core-components)
    - [User Management](#1-user-management)
    - [Table Management](#2-table-management)
    - [Review and Feedback System](#3-review-and-feedback-system)
    - [Reservation System](#4-reservation-system)
    - [Payment Method System](#5-payment-method-system)
    - [Menu Item System](#6-menu-item-system)
3. [Class Relationships](#class-relationships)
4. [OOP Concepts Implementation](#oop-concepts-implementation)
5. [Data Flow Architecture](#data-flow-architecture)

## 🌐 System Overview

The Hotel Table Reservation System is a web-based application built using Java web technologies. It allows users to create accounts, make table reservations, select menu items, process payments, and submit reviews. The system uses file storage (txt files) instead of a database for data persistence.

The architecture follows the Model-View-Controller (MVC) pattern:
- **Model**: Java classes representing entities like users, tables, reservations, etc.
- **View**: JSP pages (not fully covered in this analysis)
- **Controller**: Servlet classes handling HTTP requests and responses

## 🧩 Core Components

### 1. User Management

#### 🔹 Description
The User Management component handles user-related operations, including registration, authentication, profile updates, and account deletion.

#### 🔹 Classes Involved

```
com.tablebooknow.model.user
└── User.java

com.tablebooknow.model.admin
└── Admin.java

com.tablebooknow.dao
├── UserDAO.java
└── AdminDAO.java

com.tablebooknow.controller.user
├── UserServlet.java
└── UserProfileServlet.java

com.tablebooknow.controller.admin
├── AdminLoginServlet.java
└── AdminUserServlet.java

com.tablebooknow.util
└── PasswordHasher.java
```

#### 🔹 CRUD Operations

##### ✅ Create (Register a new user)
```java
// UserServlet.java - register method
User newUser = new User();
newUser.setUsername(username);
newUser.setEmail(email);
newUser.setPassword(PasswordHasher.hashPassword(password));
if (phone != null && !phone.trim().isEmpty()) {
    newUser.setPhone(phone);
}
userDAO.create(newUser);
```

##### 📖 Read (Find user by ID, username, or email)
```java
// UserDAO.java - findById method
public User findById(String id) throws IOException {
    if (!FileHandler.fileExists(FILE_PATH)) {
        return null;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
        String line;
        while ((line = reader.readLine()) != null) {
            User user = User.fromCsvString(line);
            if (user.getId().equals(id)) {
                return user;
            }
        }
    }
    return null;
}
```

##### 🔄 Update (User profile update)
```java
// UserProfileServlet.java - updateUserProfile method
User user = userDAO.findById(userId);
// Update email and phone
if (email != null && !email.trim().isEmpty()) {
    user.setEmail(email);
}
if (phone != null && !phone.trim().isEmpty()) {
    user.setPhone(phone);
}
// Update password if provided
if (newPassword != null && !newPassword.isEmpty()) {
    user.setPassword(PasswordHasher.hashPassword(newPassword));
}
boolean updated = userDAO.update(user);
```

##### ❌ Delete (Remove user account)
```java
// AdminUserServlet.java - deleteUser method
List<Reservation> userReservations = reservationDAO.findByUserId(userId);
if (userReservations != null && !userReservations.isEmpty()) {
    request.setAttribute("errorMessage", 
        "Cannot delete user with active reservations. Please cancel or delete all user reservations first.");
    response.sendRedirect(request.getContextPath() + "/admin/users/view?id=" + userId);
    return;
}
boolean success = userDAO.delete(userId);
```

#### 🔹 OOP Concepts Applied

##### Encapsulation
The `User` class encapsulates user data and provides getter/setter methods to control access:

```java
// User.java
public class User implements Serializable {
    private String id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private boolean isAdmin;
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    // Other getters and setters...
}
```

##### Inheritance
The system implements inheritance through the relationship between regular users and admin users. While there isn't a direct inheritance in the classes, conceptually admins have extended capabilities:

```java
// In User.java
private boolean isAdmin; // Flag to indicate if user has admin privileges

// In AdminLoginServlet.java - checks both Admin and User tables for authentication
Admin admin = adminDAO.findByUsername(username);
if (admin != null && PasswordHasher.checkPassword(password, admin.getPassword())) {
    // Admin login logic
} else {
    // Check if regular user with admin privileges
    User user = userDAO.findByUsername(username);
    if (user != null && user.isAdmin() && PasswordHasher.checkPassword(password, user.getPassword())) {
        // User-admin login logic
    }
}
```

##### Polymorphism
The system demonstrates polymorphism through different authentication mechanisms based on user type:

```java
// Different login paths in AdminLoginServlet.java
// For admin users:
session.setAttribute("adminId", admin.getId());
session.setAttribute("adminRole", admin.getRole());

// For regular users with admin privileges:
session.setAttribute("adminId", user.getId());
session.setAttribute("adminRole", "admin"); // Default role for user-admins
```

##### Data Access Layer Pattern
The system uses the DAO (Data Access Object) pattern to separate data persistence logic from business logic:

```java
// UserDAO.java handles all data persistence operations
public User create(User user) throws IOException {...}
public User findById(String id) throws IOException {...}
public User findByUsername(String username) throws IOException {...}
public boolean update(User user) throws IOException {...}
public boolean delete(String id) throws IOException {...}
```

#### 🔹 Class Diagram

```
+-------------------+     +-------------------+
|       User        |     |      Admin        |
+-------------------+     +-------------------+
| -id: String       |     | -id: String       |
| -username: String |     | -username: String |
| -password: String |     | -password: String |
| -email: String    |     | -email: String    |
| -phone: String    |     | -fullName: String |
| -isAdmin: boolean |     | -role: String     |
+-------------------+     +-------------------+
| +getId(): String  |     | +getId(): String  |
| +setId(String)    |     | +setId(String)    |
| ...               |     | ...               |
+-------------------+     +-------------------+
         ^                          ^
         |                          |
         |                          |
+-------------------+     +-------------------+
|     UserDAO       |     |     AdminDAO      |
+-------------------+     +-------------------+
| +create()         |     | +create()         |
| +findById()       |     | +findById()       |
| +findByUsername() |     | +findByUsername() |
| +findByEmail()    |     | +findByEmail()    |
| +update()         |     | +update()         |
| +delete()         |     | +delete()         |
| +findAll()        |     | +findAll()        |
+-------------------+     +-------------------+
         ^                          ^
         |                          |
         |                          |
+-------------------+     +-------------------+
|   UserServlet     |     |  AdminLoginServlet|
+-------------------+     +-------------------+
| +doGet()          |     | +doGet()          |
| +doPost()         |     | +doPost()         |
| -login()          |     | -logout()         |
| -register()       |     |                   |
| -logout()         |     |                   |
+-------------------+     +-------------------+
```

#### 🔹 Flow of Operation

1. **Registration Flow**:
   - User submits registration form with username, email, password
   - `UserServlet.doPost()` processes the form data
   - Password is hashed using `PasswordHasher.hashPassword()`
   - `UserDAO.create()` stores the user data in users.txt
   - User is auto-logged in and redirected to the home page

2. **Login Flow**:
   - User submits login form with username and password
   - `UserServlet.login()` validates credentials
   - `UserDAO.findByUsername()` retrieves the user record
   - `PasswordHasher.checkPassword()` verifies the password
   - Session is created with user information
   - User is redirected to appropriate page based on role

3. **Profile Update Flow**:
   - User submits profile update form
   - `UserProfileServlet.updateUserProfile()` processes the updates
   - `UserDAO.update()` updates the user record in users.txt

4. **Delete User Flow**:
   - Admin initiates user deletion
   - `AdminUserServlet.deleteUser()` checks for active reservations
   - If no reservations, `UserDAO.delete()` removes the user record

### 2. Table Management

#### 🔹 Description
The Table Management component handles restaurant tables, including adding new tables, assigning table types, and managing table availability.

#### 🔹 Classes Involved

```
com.tablebooknow.model.table
└── Table.java

com.tablebooknow.dao
└── TableDAO.java

com.tablebooknow.controller.admin
└── AdminTableServlet.java

com.tablebooknow.config
└── TableInitializer.java
```

#### 🔹 CRUD Operations

##### ✅ Create (Add a new table)
```java
// AdminTableServlet.java - createTable method
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

// Save to database
Table createdTable = tableDAO.create(newTable);
```

##### 📖 Read (Retrieve table details)
```java
// TableDAO.java - findById method
public Table findById(String id) throws IOException {
    if (!FileHandler.fileExists(FILE_PATH)) {
        return null;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                try {
                    Table table = Table.fromCsvString(line);
                    if (table.getId().equals(id)) {
                        return table;
                    }
                } catch (Exception e) {
                    logger.warning("Error parsing table line: " + line + ", error: " + e.getMessage());
                    // Continue to next line on error
                }
            }
        }
    }

    return null;
}
```

##### 🔄 Update (Modify table details)
```java
// AdminTableServlet.java - updateTable method
Table table = tableDAO.findById(tableId);
// Update table data
table.setTableNumber(tableNumber);
table.setTableType(tableType);
table.setFloor(floor);
table.setCapacity(capacity);
table.setLocationDescription(locationDescription);
table.setActive(isActive);

// Save to database
boolean success = tableDAO.update(table);
```

##### ❌ Delete (Remove table)
```java
// AdminTableServlet.java - deleteTable method
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
```

#### 🔹 OOP Concepts Applied

##### Encapsulation
The `Table` class encapsulates table data with private fields and public getter/setter methods:

```java
// Table.java
public class Table implements Serializable {
    private String id;
    private String tableNumber;
    private String tableType;
    private int capacity;
    private int floor;
    private String locationDescription;
    private boolean active;
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    // Other getters and setters...
}
```

##### Abstraction
The `Table` class provides abstract methods that hide implementation details:

```java
// Table.java - Abstracts the concept of a table
public String getDisplayName() {
    if (tableType == null || tableNumber == null) {
        return "Unknown Table";
    }

    String typeLabel;
    switch (tableType.toLowerCase()) {
        case "family":
            typeLabel = "Family";
            break;
        case "luxury":
            typeLabel = "Luxury";
            break;
        // Other cases...
    }
    return typeLabel + " Table " + tableNumber;
}

public String generateSystemId() {
    // Implementation details hidden from other components
}
```

##### Data Access Object Pattern
The `TableDAO` class implements the DAO pattern to provide an abstraction layer for table data persistence:

```java
// TableDAO.java
public Table create(Table table) throws IOException {...}
public Table findById(String id) throws IOException {...}
public List<Table> findByFloor(int floor) throws IOException {...}
public boolean update(Table table) throws IOException {...}
public boolean delete(String id) throws IOException {...}
```

##### Factory Pattern
The system uses a factory-like approach in `TableInitializer` to create default tables:

```java
// TableInitializer.java
public void initializeDefaultTables() throws IOException {
    // Create default tables
    logger.info("Initializing default tables...");

    // First floor tables
    for (int i = 1; i <= 4; i++) {
        Table familyTable = new Table();
        familyTable.setTableNumber(String.valueOf(i));
        familyTable.setTableType("family");
        familyTable.setCapacity(6);
        familyTable.setFloor(1);
        familyTable.setLocationDescription("First floor family section");
        familyTable.setId("f1-" + i);
        familyTable.setActive(true);
        create(familyTable);
    }
    
    // More table creation...
}
```

#### 🔹 Class Diagram

```
+---------------------+
|        Table        |
+---------------------+
| -id: String         |
| -tableNumber: String|
| -tableType: String  |
| -capacity: int      |
| -floor: int         |
| -locationDesc: String|
| -active: boolean    |
+---------------------+
| +getId(): String    |
| +setId(String)      |
| +getDisplayName()   |
| +generateSystemId() |
| ...                 |
+---------------------+
          ^
          |
          |
+---------------------+
|      TableDAO       |
+---------------------+
| +create()           |
| +findById()         |
| +findByFloor()      |
| +findByType()       |
| +findAllActive()    |
| +update()           |
| +delete()           |
| +findAll()          |
+---------------------+
          ^
          |
          |
+---------------------+     +---------------------+
| AdminTableServlet   |     | TableInitializer    |
+---------------------+     +---------------------+
| +doGet()            |     | +contextInitialized()|
| +doPost()           |     | +initializeDefaultTables()|
| ...                 |     |                     |
+---------------------+     +---------------------+
```

#### 🔹 Flow of Operation

1. **Application Startup**:
   - `TableInitializer.contextInitialized()` is called when the application starts
   - `TableInitializer.initializeDefaultTables()` creates default tables if none exist
   - Tables are stored in tables.txt through `TableDAO.create()`

2. **Admin Table Management**:
   - Admin accesses table management through `/admin/tables/*` URLs
   - `AdminTableServlet.doGet()` handles GET requests to view tables, show forms
   - `AdminTableServlet.doPost()` handles POST requests to create/update/delete tables
   - Table operations are processed through appropriate methods
   - `TableDAO` methods are used to persist changes to tables.txt

3. **Table Creation Flow**:
   - Admin fills table creation form
   - `AdminTableServlet.createTable()` processes the form
   - Table ID is generated based on type, floor, and number
   - `TableDAO.create()` saves the new table to tables.txt

4. **Table Update Flow**:
   - Admin edits table details
   - `AdminTableServlet.updateTable()` processes the updates
   - `TableDAO.update()` updates the table record

5. **Table Deletion Flow**:
   - Admin initiates table deletion
   - `AdminTableServlet.deleteTable()` checks for active reservations
   - If no reservations, `TableDAO.delete()` removes the table

### 3. Review and Feedback System

#### 🔹 Description
The Review and Feedback system allows users to submit reviews and ratings for their dining experiences. It manages the collection, display, and moderation of user reviews.

#### 🔹 Classes Involved

```
com.tablebooknow.model.review
└── Review.java

com.tablebooknow.dao
└── ReviewDAO.java

com.tablebooknow.controller.review
└── ReviewServlet.java

com.tablebooknow.controller.admin
└── AdminReviewServlet.java
```

#### 🔹 CRUD Operations

##### ✅ Create (Submit a new review)
```java
// ReviewServlet.java - createReview method
Review review = new Review();
review.setUserId(userId);
review.setReservationId(reservationId);
review.setRating(rating);
review.setTitle(title);
review.setComment(comment);
review.setCreatedAt(LocalDateTime.now());
review.setUpdatedAt(LocalDateTime.now());

// Save the review
reviewDAO.create(review);
```

##### 📖 Read (View reviews)
```java
// ReviewServlet.java - showUserReviews method
List<Review> userReviews = reviewDAO.findByUserId(userId);
request.setAttribute("reviews", userReviews);

// Get completed reservations that don't have reviews yet
List<Reservation> userReservations = reservationDAO.findByUserId(userId);

List<Reservation> completedReservationsWithoutReviews = userReservations.stream()
        .filter(reservation -> "completed".equals(reservation.getStatus()) || 
                "confirmed".equals(reservation.getStatus()))
        .filter(reservation -> {
            try {
                return !reviewDAO.hasReview(reservation.getId(), userId);
            } catch (IOException e) {
                return false;
            }
        })
        .collect(java.util.stream.Collectors.toList());

request.setAttribute("completedReservations", completedReservationsWithoutReviews);
```

##### 🔄 Update (Edit a review)
```java
// ReviewServlet.java - updateReview method
Review review = reviewDAO.findById(reviewId);

// Update the review
review.setRating(rating);
review.setTitle(title);
review.setComment(comment);
review.setUpdatedAt(LocalDateTime.now());

// Save the updated review
reviewDAO.update(review);
```

##### ❌ Delete (Remove a review)
```java
// ReviewServlet.java - deleteReview method
Review review = reviewDAO.findById(reviewId);

// Check if the review belongs to the user
if (!review.getUserId().equals(userId)) {
    request.setAttribute("errorMessage", "You can only delete your own reviews");
    response.sendRedirect(request.getContextPath() + "/reviews/list");
    return;
}

// Delete the review
reviewDAO.delete(reviewId);
```

#### 🔹 OOP Concepts Applied

##### Encapsulation
The `Review` class encapsulates review data with private fields and getter/setter methods:

```java
// Review.java
public class Review implements Serializable {
    private String id;
    private String userId;
    private String reservationId;
    private int rating;
    private String title;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Getters and setters with validation
    public void setRating(int rating) {
        if (rating < 1) {
            this.rating = 1;
        } else if (rating > 5) {
            this.rating = 5;
        } else {
            this.rating = rating;
        }
    }
    
    // Other getters and setters...
}
```

##### Composition
The Review system demonstrates composition by combining multiple objects to form a complete review system:

```java
// ReviewServlet.java uses multiple DAOs and models
private ReviewDAO reviewDAO;
private ReservationDAO reservationDAO;
private UserDAO userDAO;

@Override
public void init() throws ServletException {
    reviewDAO = new ReviewDAO();
    reservationDAO = new ReservationDAO();
    userDAO = new UserDAO();
}
```

##### Single Responsibility Principle
Each class has a single responsibility:
- `Review.java`: Represents the review data structure
- `ReviewDAO.java`: Handles data persistence
- `ReviewServlet.java`: Processes user review operations
- `AdminReviewServlet.java`: Manages admin review moderation

```java
// ReviewDAO.java focuses solely on review data operations
public Review create(Review review) throws IOException {...}
public Review findById(String id) throws IOException {...}
public List<Review> findByUserId(String userId) throws IOException {...}
public boolean hasReview(String reservationId, String userId) throws IOException {...}
public boolean update(Review review) throws IOException {...}
public boolean delete(String id) throws IOException {...}
```

##### Data Access Object Pattern
The `ReviewDAO` implements the DAO pattern to separate data access from business logic:

```java
// ReviewDAO.java handles all review data access
public class ReviewDAO {
    private static final String FILE_PATH = getDataFilePath("reviews.txt");
    
    // Data access methods...
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
```

#### 🔹 Class Diagram

```
+---------------------+
|       Review        |
+---------------------+
| -id: String         |
| -userId: String     |
| -reservationId: String|
| -rating: int        |
| -title: String      |
| -comment: String    |
| -createdAt: LocalDateTime|
| -updatedAt: LocalDateTime|
+---------------------+
| +getId(): String    |
| +setId(String)      |
| +setRating(int)     |
| ...                 |
+---------------------+
          ^
          |
          |
+---------------------+
|     ReviewDAO       |
+---------------------+
| +create()           |
| +findById()         |
| +findByUserId()     |
| +findByReservationId()|
| +hasReview()        |
| +update()           |
| +delete()           |
| +findAll()          |
| +getAverageRating() |
+---------------------+
          ^
          |
          |
+---------------------+     +---------------------+
|   ReviewServlet     |     | AdminReviewServlet  |
+---------------------+     +---------------------+
| +doGet()            |     | +doGet()            |
| +doPost()           |     | +doPost()           |
| -showUserReviews()  |     | -deleteReview()     |
| -createReview()     |     |                     |
| -updateReview()     |     |                     |
| -deleteReview()     |     |                     |
+---------------------+     +---------------------+
```

#### 🔹 Flow of Operation

1. **View User Reviews**:
   - User accesses `/reviews/list`
   - `ReviewServlet.doGet()` handles the request
   - `ReviewServlet.showUserReviews()` retrieves user's reviews and reservations without reviews
   - Data is forwarded to JSP for display

2. **Submit Review Flow**:
   - User selects a completed reservation and submits review form
   - `ReviewServlet.doPost()` with `action=create` is triggered
   - `ReviewServlet.createReview()` processes the form data
   - `ReviewDAO.create()` saves the review to reviews.txt

3. **Edit Review Flow**:
   - User edits an existing review
   - `ReviewServlet.doPost()` with `action=update` is triggered
   - `ReviewServlet.updateReview()` processes the updates
   - `ReviewDAO.update()` updates the review record

4. **Delete Review Flow**:
   - User initiates review deletion
   - `ReviewServlet.doPost()` with `action=delete` is triggered
   - `ReviewServlet.deleteReview()` verifies ownership and deletes the review
   - `ReviewDAO.delete()` removes the review from reviews.txt

5. **Admin Review Moderation**:
   - Admin accesses `/admin/reviews/*`
   - `AdminReviewServlet.doGet()` displays all reviews for moderation
   - Admin can delete inappropriate reviews using `/admin/reviews/delete`
   - `AdminReviewServlet.deleteReview()` processes deletion

### 4. Reservation System

#### 🔹 Description
The Reservation System manages the booking of tables, tracking reservation details, and handling the reservation process from date selection to confirmation.

#### 🔹 Classes Involved

```
com.tablebooknow.model.reservation
├── Reservation.java
└── ReservationQueue.java

com.tablebooknow.dao
└── ReservationDAO.java

com.tablebooknow.controller.reservation
├── ReservationServlet.java
└── UserReservationsServlet.java

com.tablebooknow.controller.admin
└── AdminReservationServlet.java
```

#### 🔹 CRUD Operations

##### ✅ Create (Make a new reservation)
```java
// ReservationServlet.java - confirmReservation method
Reservation reservation = new Reservation();
reservation.setUserId(userId);
reservation.setReservationDate(reservationDate);
reservation.setReservationTime(reservationTime);
reservation.setDuration(Integer.parseInt(reservationDuration));
reservation.setTableId(tableId);
reservation.setBookingType(bookingType);
reservation.setSpecialRequests(specialRequests);
reservation.setStatus("pending"); // Change status to pending until payment

// Save the reservation
Reservation createdReservation = reservationDAO.create(reservation);
```

##### 📖 Read (View reservations)
```java
// UserReservationsServlet.java - doGet method
String userId = (String) session.getAttribute("userId");

try {
    // Get all reservations for this user
    List<Reservation> userReservations = reservationDAO.findByUserId(userId);

    // Sort reservations using merge sort - by date and time
    userReservations = mergeSortReservations(userReservations);

    // Set as request attribute
    request.setAttribute("userReservations", userReservations);

    // Forward to the JSP
    request.getRequestDispatcher("/user-reservations.jsp").forward(request, response);
} catch (Exception e) {
    request.setAttribute("errorMessage", "Error loading reservations: " + e.getMessage());
    request.getRequestDispatcher("/user-reservations.jsp").forward(request, response);
}
```

##### 🔄 Update (Modify a reservation)
```java
// AdminReservationServlet.java - updateReservation method
Reservation reservation = reservationDAO.findById(reservationId);

// Update fields from the form
String status = request.getParameter("status");
String tableId = request.getParameter("tableId");
String date = request.getParameter("reservationDate");
String time = request.getParameter("reservationTime");
String specialRequests = request.getParameter("specialRequests");
String bookingType = request.getParameter("bookingType");
String durationStr = request.getParameter("duration");

if (status != null && !status.isEmpty()) {
    reservation.setStatus(status);
}
// Update other fields...

boolean success = reservationDAO.update(reservation);
```

##### ❌ Delete/Cancel (Cancel a reservation)
```java
// UserReservationsServlet.java - doPost method (for cancel)
String action = request.getParameter("action");
String reservationId = request.getParameter("reservationId");

if ("cancel".equals(action) && reservationId != null) {
    try {
        // Get the reservation
        Reservation reservation = reservationDAO.findById(reservationId);

        // Verify the reservation belongs to this user
        if (reservation != null && reservation.getUserId().equals(userId)) {
            // Cancel the reservation
            boolean success = reservationDAO.cancelReservation(reservationId);

            if (success) {
                request.setAttribute("successMessage", "Reservation successfully cancelled.");
            } else {
                request.setAttribute("errorMessage", "Failed to cancel reservation.");
            }
        } else {
            request.setAttribute("errorMessage", "Invalid reservation or permission denied.");
        }
    } catch (Exception e) {
        request.setAttribute("errorMessage", "Error cancelling reservation: " + e.getMessage());
    }
}
```

#### 🔹 OOP Concepts Applied

##### Encapsulation
The `Reservation` class encapsulates reservation data:

```java
// Reservation.java
public class Reservation implements Serializable {
    private String id;
    private String userId;
    private String tableId;
    private String reservationDate;
    private String reservationTime;
    private int duration;
    private String bookingType;
    private String specialRequests;
    private String status;
    private String createdAt;
    
    // Getters and setters...
}
