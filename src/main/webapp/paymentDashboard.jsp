<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.tablebooknow.model.payment.PaymentCard" %>
<%@ page import="com.tablebooknow.util.GsonFactory" %>
<%@ page import="com.google.gson.Gson" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Payment Dashboard | Gourmet Reserve</title>
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@500&family=Roboto:wght@300;400;500&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/payment-dashboard.css">
</head>
<body>
    <%
        // Check if user is logged in
        if (session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String username = (String) session.getAttribute("username");
        String reservationId = (String) session.getAttribute("reservationId");

        // If no reservation ID is in session, check if it's in request parameters
        if (reservationId == null) {
            reservationId = request.getParameter("reservationId");
            if (reservationId != null) {
                session.setAttribute("reservationId", reservationId);
            }
        }

        // Get reservation details if available
        com.tablebooknow.model.reservation.Reservation reservation = null;
        String tableType = request.getAttribute("tableType") != null ?
                          (String) request.getAttribute("tableType") : "Regular";

        if (request.getAttribute("reservation") != null) {
            reservation = (com.tablebooknow.model.reservation.Reservation) request.getAttribute("reservation");
        }

        // Get error message if any
        String errorMessage = (String) request.getAttribute("errorMessage");
        String successMessage = (String) request.getAttribute("successMessage");

        // Calculate amount based on table type and duration
        double basePrice = 0;
        int duration = 2; // Default

        if (reservation != null) {
            duration = reservation.getDuration();

            if (tableType.equalsIgnoreCase("Family")) {
                basePrice = 12.00;
            } else if (tableType.equalsIgnoreCase("Luxury")) {
                basePrice = 18.00;
            } else if (tableType.equalsIgnoreCase("Regular")) {
                basePrice = 8.00;
            } else if (tableType.equalsIgnoreCase("Couple")) {
                basePrice = 6.00;
            }
        }

        double totalAmount = basePrice * duration;

        // Get payment cards for this user
        List<PaymentCard> paymentCards = (List<PaymentCard>) request.getAttribute("paymentCards");

        // Convert payment cards to JSON for JavaScript
        Gson gson = GsonFactory.createGson();
        String paymentCardsJson = "[]";
        if (paymentCards != null && !paymentCards.isEmpty()) {
            paymentCardsJson = gson.toJson(paymentCards);
        }
    %>

    <div class="payment-dashboard animated">
        <div class="dashboard-header">
            <h1 class="dashboard-title">Payment Dashboard</h1>
            <p class="dashboard-subtitle">Manage your payment methods and complete your reservation</p>
        </div>

        <div class="dashboard-content">
            <!-- Success/Error Messages -->
            <% if (successMessage != null) { %>
                <div class="message success-message">
                    <%= successMessage %>
                </div>
            <% } %>

            <% if (errorMessage != null) { %>
                <div class="message error-message">
                    <%= errorMessage %>
                </div>
            <% } %>

            <!-- Reservation Summary -->
            <div>
                <h2 class="section-title">Reservation Summary</h2>
                <div class="reservation-summary">
                    <div class="summary-item">
                        <span class="summary-label">Reservation ID</span>
                        <span class="summary-value"><%= reservationId %></span>
                    </div>
                    <% if (reservation != null) { %>
                        <div class="summary-item">
                            <span class="summary-label">Date</span>
                            <span class="summary-value"><%= reservation.getReservationDate() %></span>
                        </div>
                        <div class="summary-item">
                            <span class="summary-label">Time</span>
                            <span class="summary-value"><%= reservation.getReservationTime() %></span>
                        </div>
                        <div class="summary-item">
                            <span class="summary-label">Duration</span>
                            <span class="summary-value"><%= duration %> hours</span>
                        </div>
                        <div class="summary-item">
                            <span class="summary-label">Table Type</span>
                            <span class="summary-value"><%= tableType %></span>
                        </div>
                        <div class="summary-item">
                            <span class="summary-label">Price per Hour</span>
                            <span class="summary-value">$<%= String.format("%.2f", basePrice) %></span>
                        </div>
                        <div class="summary-item">
                            <span class="summary-label">Total Amount</span>
                            <span class="summary-value highlight">$<%= String.format("%.2f", totalAmount) %></span>
                        </div>
                    <% } %>
                </div>
            </div>

            <!-- Payment Methods Section -->
            <div>
                <h2 class="section-title">Your Payment Methods</h2>
                <button id="toggleFormBtn" class="toggle-form-btn">
                    <i class="fas fa-plus btn-icon"></i> Add New Payment Method
                </button>

                <div id="newCardForm" class="new-card-form">
                    <form id="cardForm">
                        <input type="hidden" name="action" value="add" id="formAction">
                        <input type="hidden" name="cardId" id="editCardId">

                        <div class="form-group">
                            <label class="form-label">Card Holder Name</label>
                            <input type="text" class="form-input" id="cardholderName" name="cardholderName" placeholder="Enter cardholder name" required>
                        </div>

                        <div class="form-group">
                            <label class="form-label">Card Number</label>
                            <input type="text" class="form-input" id="cardNumber" name="cardNumber" placeholder="1234 5678 9012 3456" maxlength="19" required>
                        </div>

                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Expiry Date</label>
                                <input type="text" class="form-input" id="expiryDate" name="expiryDate" placeholder="MM/YY" maxlength="5" required>
                            </div>
                            <div class="form-group">
                                <label class="form-label">CVV</label>
                                <input type="text" class="form-input" id="cvv" name="cvv" placeholder="123" maxlength="3" required>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="form-label">Card Type</label>
                            <select class="form-input" id="cardType" name="cardType" required>
                                <option value="">Select card type</option>
                                <option value="visa">Visa</option>
                                <option value="mastercard">Mastercard</option>
                                <option value="amex">American Express</option>
                                <option value="discover">Discover</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label class="form-label">
                                <input type="checkbox" name="makeDefault" id="makeDefault" value="true"> Make this my default payment method
                            </label>
                        </div>

                        <button type="submit" class="save-card-btn" id="saveCardBtn">Save Card</button>
                    </form>
                </div>

                <div class="payment-methods" id="paymentCardsContainer">
                    <!-- Cards will be loaded dynamically by JavaScript -->
                    <div id="noCardsMessage" style="grid-column: 1/-1; text-align: center; padding: 2rem; background: rgba(0,0,0,0.2); border-radius: 12px;">
                        <p>No payment methods added yet.</p>
                    </div>
                </div>
            </div>

            <!-- Proceed to Payment Button -->
            <form action="${pageContext.request.contextPath}/payment/process" method="post" id="paymentForm">
                <input type="hidden" name="reservationId" value="<%= reservationId %>">
                <input type="hidden" name="cardId" id="selectedCardId" value="">
                <button type="submit" class="proceed-btn" id="proceedBtn" disabled>Proceed to Payment</button>
            </form>

            <a href="${pageContext.request.contextPath}/reservation/tableSelection" class="back-link">Back to Table Selection</a>
        </div>
    </div>

    <!-- Delete Card Confirmation Modal -->
    <div class="modal-overlay" id="deleteModal">
        <div class="modal">
            <div class="modal-header">
                <h2 class="modal-title">Delete Payment Method</h2>
                <button class="close-btn" onclick="hideDeleteModal()">&times;</button>
            </div>
            <p>Are you sure you want to delete this payment method? This action cannot be undone.</p>
            <div class="modal-footer">
                <button class="modal-btn btn-secondary" onclick="hideDeleteModal()">Cancel</button>
                <button class="modal-btn btn-danger" id="confirmDeleteBtn">Delete</button>
            </div>
        </div>
    </div>

    <script>
        // Store this data from JSP for use in JavaScript
        const appContextPath = "${pageContext.request.contextPath}";
        let paymentCards = <%= paymentCardsJson %>;

        // Debug
        console.log("Context path:", appContextPath);
        console.log("Initial payment cards:", paymentCards);

        // Card editing state
        let isEditingCard = false;
        let cardToDelete = null;

        // DOM Elements
        document.addEventListener('DOMContentLoaded', function() {
            // Get DOM elements
            const toggleFormBtn = document.getElementById('toggleFormBtn');
            const newCardForm = document.getElementById('newCardForm');
            const cardForm = document.getElementById('cardForm');
            const paymentCardsContainer = document.getElementById('paymentCardsContainer');
            const deleteModal = document.getElementById('deleteModal');
            const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
            const proceedBtn = document.getElementById('proceedBtn');
            const selectedCardIdInput = document.getElementById('selectedCardId');
            const noCardsMessage = document.getElementById('noCardsMessage');
            const editCardIdInput = document.getElementById('editCardId');
            const formActionInput = document.getElementById('formAction');
            const saveCardBtn = document.getElementById('saveCardBtn');

            console.log("Elements initialized");

            // Form toggle functionality
            toggleFormBtn.addEventListener('click', function() {
                console.log("Toggle form button clicked");
                newCardForm.classList.toggle('visible');

                if (newCardForm.classList.contains('visible')) {
                    toggleFormBtn.innerHTML = '<i class="fas fa-minus btn-icon"></i> Close Form';

                    // If we were editing, reset the form
                    if (isEditingCard) {
                        resetCardForm();
                    }
                } else {
                    toggleFormBtn.innerHTML = '<i class="fas fa-plus btn-icon"></i> Add New Payment Method';
                    // Reset form
                    resetCardForm();
                }
            });

            // Card form submission
            cardForm.addEventListener('submit', function(e) {
                e.preventDefault();
                console.log("Card form submitted");

                // Validate form
                if (!validateCardForm()) {
                    return;
                }

                // Get form data
                const formData = new FormData(cardForm);

                // Show loading state
                saveCardBtn.textContent = isEditingCard ? 'Updating...' : 'Saving...';
                saveCardBtn.disabled = true;

                // Use the absolute URL with context path
                let url = '';
                if (isEditingCard) {
                    url = appContextPath + "/paymentcard/update";
                } else {
                    url = appContextPath + "/paymentcard";
                }

                // Convert FormData to URL-encoded string for easier debugging
                const urlEncodedData = new URLSearchParams(formData).toString();
                console.log("Sending data:", urlEncodedData);

                // Send AJAX request using fetch API
                fetch(url, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: urlEncodedData
                })
                .then(response => {
                    console.log("Response status:", response.status);
                    if (!response.ok) {
                        throw new Error(`Server returned ${response.status} ${response.statusText}`);
                    }
                    return response.text();
                })
                .then(data => {
                    console.log("Card saved successfully:", data);

                    // Show success message before reload
                    const messageDiv = document.createElement('div');
                    messageDiv.className = 'message success-message';
                    messageDiv.textContent = isEditingCard ?
                        'Payment method updated successfully' :
                        'Payment method added successfully';

                    // Insert at the beginning of content
                    const dashboardContent = document.querySelector('.dashboard-content');
                    dashboardContent.insertBefore(messageDiv, dashboardContent.firstChild);

                    // Reload the page after a brief delay to show the message
                    setTimeout(() => {
                        window.location.reload();
                    }, 1000);
                })
                .catch(error => {
                    console.error('Error:', error);

                    // Show error message
                    const messageDiv = document.createElement('div');
                    messageDiv.className = 'message error-message';
                    messageDiv.textContent = 'Failed to save card: ' + error.message;

                    // Insert at the beginning of content
                    const dashboardContent = document.querySelector('.dashboard-content');
                    dashboardContent.insertBefore(messageDiv, dashboardContent.firstChild);

                    // Reset button state
                    saveCardBtn.textContent = isEditingCard ? 'Update Card' : 'Save Card';
                    saveCardBtn.disabled = false;
                });
            });

            // Format card number with spaces
            document.getElementById('cardNumber').addEventListener('input', function(e) {
                let value = e.target.value.replace(/\s/g, '');

                // Limit to 16 digits for most cards
                if (value.length > 16) {
                    value = value.substr(0, 16);
                }

                // Format with spaces every 4 digits
                let formattedValue = '';
                for (let i = 0; i < value.length; i++) {
                    if (i > 0 && i % 4 === 0) {
                        formattedValue += ' ';
                    }
                    formattedValue += value[i];
                }

                e.target.value = formattedValue;
            });

            // Format expiry date with slash
            document.getElementById('expiryDate').addEventListener('input', function(e) {
                let value = e.target.value.replace(/\D/g, '');

                // Limit to 4 digits (MM/YY)
                if (value.length > 4) {
                    value = value.substr(0, 4);
                }

                // Format with slash
                if (value.length > 2) {
                    e.target.value = value.substr(0, 2) + '/' + value.substr(2);
                } else {
                    e.target.value = value;
                }
            });

            // Only allow digits for CVV
            document.getElementById('cvv').addEventListener('input', function(e) {
                e.target.value = e.target.value.replace(/\D/g, '').substr(0, 3);
            });

            // Delete card confirmation
            // Delete card confirmation with form submission
            confirmDeleteBtn.addEventListener('click', function() {
                if (cardToDelete) {
                    console.log("Deleting card:", cardToDelete);

                    // Create a form and submit it
                    const form = document.createElement('form');
                    form.method = 'POST';
                    form.action = appContextPath + '/paymentcard/delete';

                    const input = document.createElement('input');
                    input.type = 'hidden';
                    input.name = 'cardId';
                    input.value = cardToDelete;

                    form.appendChild(input);
                    document.body.appendChild(form);

                    console.log("Submitting delete form with cardId:", cardToDelete);
                    form.submit();
                } else {
                    console.error("No card selected for deletion");
                    alert("No card selected for deletion");
                }
            });

            // Set default card functionality
            document.addEventListener('click', function(e) {
                if (e.target && e.target.classList.contains('set-default-btn')) {
                    const cardId = e.target.getAttribute('data-card-id');
                    if (!cardId) return;

                    console.log("Setting card as default:", cardId);

                    // Send request to set as default with proper context path
                    fetch(`${appContextPath}/paymentcard/setdefault`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded',
                        },
                        body: `cardId=${cardId}`
                    })
                    .then(response => {
                        console.log("Set default response status:", response.status);
                        if (!response.ok) {
                            throw new Error(`Server returned ${response.status} ${response.statusText}`);
                        }
                        return response.text();
                    })
                    .then(data => {
                        console.log("Set default response data:", data);

                        // Show success message before reload
                        const messageDiv = document.createElement('div');
                        messageDiv.className = 'message success-message';
                        messageDiv.textContent = 'Default payment method updated';

                        // Insert at the beginning of content
                        const dashboardContent = document.querySelector('.dashboard-content');
                        dashboardContent.insertBefore(messageDiv, dashboardContent.firstChild);

                        // Reload page after a brief delay
                        setTimeout(() => {
                            window.location.reload();
                        }, 1000);
                    })
                    .catch(error => {
                        console.error('Error setting default card:', error);

                        // Show error message
                        const messageDiv = document.createElement('div');
                        messageDiv.className = 'message error-message';
                        messageDiv.textContent = 'Failed to set default card: ' + error.message;

                        // Insert at the beginning of content
                        const dashboardContent = document.querySelector('.dashboard-content');
                        dashboardContent.insertBefore(messageDiv, dashboardContent.firstChild);
                    });
                }
            });

            // Payment form submission
            document.getElementById('paymentForm').addEventListener('submit', function(e) {
                // Check if a card is selected
                if (!selectedCardIdInput.value) {
                    e.preventDefault();
                    alert('Please select a payment method');
                    return false;
                }

                // Proceed with submission - disable button to prevent double-submission
                proceedBtn.textContent = 'Processing...';
                proceedBtn.disabled = true;
                return true;
            });

            // Initialize the card display
            renderCards();
        });

        // Functions

        // Render all cards
        function renderCards() {
            const paymentCardsContainer = document.getElementById('paymentCardsContainer');
            const noCardsMessage = document.getElementById('noCardsMessage');
            const proceedBtn = document.getElementById('proceedBtn');

            // Clear container
            paymentCardsContainer.innerHTML = '';

            if (!paymentCards || paymentCards.length === 0) {
                // Show no cards message
                paymentCardsContainer.appendChild(noCardsMessage);
                proceedBtn.disabled = true;
                return;
            }

            // Hide no cards message
            noCardsMessage.style.display = 'none';

            // Enable proceed button
            proceedBtn.disabled = false;

            // Add cards to container
            paymentCards.forEach(card => {
                const cardElement = createCardElement(card);
                paymentCardsContainer.appendChild(cardElement);
            });

            // Select default card
            const defaultCard = paymentCards.find(card => card.defaultCard === true);

            if (defaultCard) {
                selectCard(defaultCard.id);
            } else if (paymentCards.length > 0) {
                // Select first card if no default
                selectCard(paymentCards[0].id);
            }
        }

        // Create HTML card element
        function createCardElement(card) {
            const cardElement = document.createElement('div');
            cardElement.className = 'payment-card';
            cardElement.dataset.cardId = card.id;

            // Determine card icon class
            let cardIconClass = 'fa-credit-card';
            if (card.cardType === 'visa') {
                cardIconClass = 'fa-cc-visa';
            } else if (card.cardType === 'mastercard') {
                cardIconClass = 'fa-cc-mastercard';
            } else if (card.cardType === 'amex') {
                cardIconClass = 'fa-cc-amex';
            } else if (card.cardType === 'discover') {
                cardIconClass = 'fa-cc-discover';
            }

            // Format card type display
            const cardTypeName = card.cardType.charAt(0).toUpperCase() + card.cardType.slice(1);

            // Get last 4 digits of card number
            const last4 = card.cardNumber ? card.cardNumber.replace(/\s/g, '').slice(-4) : '****';

            // Create card inner HTML without using template literals (backticks)
            let innerHtml = '';

            // Add default badge if needed
            if (card.defaultCard) {
                innerHtml += '<div class="card-badge">Default</div>';
            }

            // Add card type section
            innerHtml += '<div class="card-type">' +
                '<i class="fab ' + cardIconClass + ' card-icon"></i>' +
                '<span class="card-name">' + cardTypeName + '</span>' +
                '</div>';

            // Add card number section
            innerHtml += '<div class="card-number">**** **** **** ' + last4 + '</div>';

            // Add expiry date
            innerHtml += '<div class="card-expiry">Expires: ' + card.expiryDate + '</div>';

            // Add action buttons
            innerHtml += '<div class="card-actions">' +
                '<button class="card-btn btn-edit" onclick="editCard(\'' + card.id + '\')">' +
                '<i class="fas fa-edit"></i> Edit' +
                '</button>' +
                '<button class="card-btn btn-delete" onclick="showDeleteModal(\'' + card.id + '\')">' +
                '<i class="fas fa-trash"></i> Delete' +
                '</button>';

            // Add "Set Default" button if not already default
            if (!card.defaultCard) {
                innerHtml += '<button class="card-btn set-default-btn" data-card-id="' + card.id + '">' +
                    '<i class="fas fa-star"></i> Set Default' +
                    '</button>';
            }

            // Close action buttons div
            innerHtml += '</div>';

            // Set the HTML
            cardElement.innerHTML = innerHtml;

            // Add click event for card selection
            cardElement.addEventListener('click', function(e) {
                // Don't select if clicking on buttons
                if (e.target.closest('.card-actions')) {
                return;
                                }

                                selectCard(card.id);
                            });

                            return cardElement;
                        }

                        // Select a card
                        function selectCard(cardId) {
                            // Remove selected class from all cards
                            document.querySelectorAll('.payment-card').forEach(cardElem => {
                                cardElem.classList.remove('selected');
                            });

                            // Add selected class to this card
                            const cardElement = document.querySelector('.payment-card[data-card-id="' + cardId + '"]');
                            if (cardElement) {
                                cardElement.classList.add('selected');

                                // Update hidden input value
                                document.getElementById('selectedCardId').value = cardId;

                                // Enable proceed button
                                document.getElementById('proceedBtn').disabled = false;
                            }
                        }

                        // Show delete confirmation modal
                        function showDeleteModal(id) {
                            cardToDelete = id;
                            document.getElementById('deleteModal').style.display = 'flex';
                        }

                        // Hide delete confirmation modal
                        function hideDeleteModal() {
                            document.getElementById('deleteModal').style.display = 'none';
                            cardToDelete = null;
                        }

                        // Edit a card
                        function editCard(cardId) {
                            // Find the card in the array
                            const card = paymentCards.find(c => c.id === cardId);
                            if (!card) {
                                console.error("Card not found:", cardId);
                                return;
                            }

                            console.log("Editing card:", card);

                            // Show the form
                            const newCardForm = document.getElementById('newCardForm');
                            const toggleFormBtn = document.getElementById('toggleFormBtn');

                            newCardForm.classList.add('visible');
                            toggleFormBtn.innerHTML = '<i class="fas fa-minus btn-icon"></i> Close Form';

                            // Update form action and button text
                            document.getElementById('formAction').value = 'update';
                            document.getElementById('editCardId').value = cardId;
                            document.getElementById('saveCardBtn').textContent = 'Update Card';

                            // Fill form with card data
                            document.getElementById('cardholderName').value = card.cardholderName || '';
                            document.getElementById('cardNumber').value = ''; // Don't populate full card number for security reasons
                            document.getElementById('expiryDate').value = card.expiryDate || '';
                            document.getElementById('cvv').value = ''; // Don't populate CVV for security reasons
                            document.getElementById('cardType').value = card.cardType || '';
                            document.getElementById('makeDefault').checked = card.defaultCard || false;

                            // Set editing state
                            isEditingCard = true;

                            // Scroll to the form
                            newCardForm.scrollIntoView({ behavior: 'smooth' });
                        }

                        // Reset card form to add new mode
                        function resetCardForm() {
                            const cardForm = document.getElementById('cardForm');
                            cardForm.reset();

                            document.getElementById('formAction').value = 'add';
                            document.getElementById('editCardId').value = '';
                            document.getElementById('saveCardBtn').textContent = 'Save Card';

                            isEditingCard = false;
                        }

                        // Validate card form
                        function validateCardForm() {
                            const cardholderName = document.getElementById('cardholderName').value;
                            const cardNumber = document.getElementById('cardNumber').value.replace(/\s/g, '');
                            const expiryDate = document.getElementById('expiryDate').value;
                            const cvv = document.getElementById('cvv').value;
                            const cardType = document.getElementById('cardType').value;

                            // When editing, we don't require card number and CVV
                            if (isEditingCard) {
                                if (!cardholderName || !expiryDate || !cardType) {
                                    alert('Please fill in all required fields');
                                    return false;
                                }

                                // Validate expiry date
                                if (!/^\d{2}\/\d{2}$/.test(expiryDate)) {
                                    alert('Please enter a valid expiry date (MM/YY)');
                                    return false;
                                }

                                // If CVV is provided, validate it
                                if (cvv && !/^\d{3,4}$/.test(cvv)) {
                                    alert('Please enter a valid CVV (3-4 digits)');
                                    return false;
                                }

                                return true;
                            }

                            // Basic validation for new card
                            if (!cardholderName || !cardNumber || !expiryDate || !cvv || !cardType) {
                                alert('Please fill in all fields');
                                return false;
                            }

                            // Validate card number format (13-19 digits)
                            if (!/^\d{13,19}$/.test(cardNumber)) {
                                alert('Please enter a valid card number (13-19 digits)');
                                return false;
                            }

                            // Validate expiry date
                            if (!/^\d{2}\/\d{2}$/.test(expiryDate)) {
                                alert('Please enter a valid expiry date (MM/YY)');
                                return false;
                            }

                            // Validate expiry date is not in the past
                            const [month, year] = expiryDate.split('/').map(part => parseInt(part, 10));
                            const currentDate = new Date();
                            const currentYear = currentDate.getFullYear() % 100; // Get last 2 digits
                            const currentMonth = currentDate.getMonth() + 1; // Months are 0-indexed

                            if (year < currentYear || (year === currentYear && month < currentMonth)) {
                                alert('Card has expired. Please enter a valid expiry date.');
                                return false;
                            }

                            return true;
                        }
                    </script>
                </body>
                </html>