<%@ include file="admin-header.jsp" %>

<%
    List<User> users = (List<User>) request.getAttribute("users");
    Integer userCount = (Integer) request.getAttribute("userCount");
    String searchTerm = (String) request.getAttribute("searchTerm");
    String filter = (String) request.getAttribute("filter");

    if (userCount == null) userCount = 0;
%>

<h1 style="color: var(--gold); margin-bottom: 2rem;">User Management</h1>

<div style="display: flex; gap: 1rem; margin-bottom: 2rem;">
    <a href="${pageContext.request.contextPath}/admin/users/add" class="action-btn edit-btn" style="display: flex; align-items: center; padding: 0.75rem 1.5rem; background-color: #4CAF50;">
        <span style="margin-right: 0.5rem;">➕</span> Add New User
    </a>
</div>

<div class="card">
    <div style="margin-bottom: 1.5rem;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
            <h2>All Users (<%= userCount %>)</h2>
            <div>
                <a href="${pageContext.request.contextPath}/admin/users" class="action-btn edit-btn" style="padding: 0.5rem 1rem; <%= (filter == null) ? "background-color: #4CAF50;" : "" %>">All</a>
                <a href="${pageContext.request.contextPath}/admin/users?filter=admin" class="action-btn edit-btn" style="padding: 0.5rem 1rem; <%= "admin".equals(filter) ? "background-color: #2196F3;" : "" %>">Admins</a>
                <a href="${pageContext.request.contextPath}/admin/users?filter=regular" class="action-btn edit-btn" style="padding: 0.5rem 1rem; <%= "regular".equals(filter) ? "background-color: #FF9800;" : "" %>">Regular Users</a>
            </div>
        </div>
        <form method="get" action="${pageContext.request.contextPath}/admin/users" style="display: flex; gap: 1rem; margin-top: 1rem;">
            <input type="text" name="search" placeholder="Search by username, email or phone..." value="<%= searchTerm != null ? searchTerm : "" %>"
                style="flex: 1; padding: 0.5rem; border-radius: 5px; background: rgba(255, 255, 255, 0.1); color: var(--text); border: 1px solid rgba(212, 175, 55, 0.3);">
            <% if (filter != null && !filter.isEmpty()) { %>
                <input type="hidden" name="filter" value="<%= filter %>">
            <% } %>
            <button type="submit" class="action-btn edit-btn">Search</button>
            <% if (searchTerm != null && !searchTerm.isEmpty()) { %>
                <a href="${pageContext.request.contextPath}/admin/users<%= filter != null ? "?filter=" + filter : "" %>" class="action-btn delete-btn" style="padding: 0.5rem 1rem;">Clear</a>
            <% } %>
        </form>
    </div>

    <table class="data-table">
        <thead>
            <tr>
                <th>User ID</th>
                <th>Username</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Admin</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
            <%
            if (users != null && !users.isEmpty()) {
                for (User user : users) {
            %>
            <tr>
                <td><%= user.getId() %></td>
                <td><%= user.getUsername() %></td>
                <td><%= user.getEmail() != null ? user.getEmail() : "N/A" %></td>
                <td><%= user.getPhone() != null ? user.getPhone() : "N/A" %></td>
                <td>
                    <form id="adminForm_<%= user.getId() %>" method="post" action="${pageContext.request.contextPath}/admin/users/updateAdmin">
                        <input type="hidden" name="userId" value="<%= user.getId() %>">
                        <input type="checkbox" name="isAdmin" <%= user.isAdmin() ? "checked" : "" %>
                               onchange="document.getElementById('adminForm_<%= user.getId() %>').submit()"
                               style="width: 18px; height: 18px; accent-color: var(--gold);">
                    </form>
                </td>
                <td>
                    <div style="display: flex; gap: 0.5rem;">
                        <a href="${pageContext.request.contextPath}/admin/users/view?id=<%= user.getId() %>" class="action-btn edit-btn">View</a>
                        <a href="${pageContext.request.contextPath}/admin/users/edit?id=<%= user.getId() %>" class="action-btn edit-btn" style="background-color: #2196F3;">Edit</a>
                        <form method="post" action="${pageContext.request.contextPath}/admin/users/delete" onsubmit="return confirmDelete('<%= user.getId() %>');">
                            <input type="hidden" name="userId" value="<%= user.getId() %>">
                            <button type="submit" class="action-btn delete-btn">Delete</button>
                        </form>
                    </div>
                </td>
            </tr>
            <%
                }
            } else {
            %>
            <tr>
                <td colspan="6" style="text-align: center;">
                    <% if (searchTerm != null && !searchTerm.isEmpty()) { %>
                        No users found matching "<%= searchTerm %>".
                    <% } else if ("admin".equals(filter)) { %>
                        No admin users found.
                    <% } else if ("regular".equals(filter)) { %>
                        No regular users found.
                    <% } else { %>
                        No users found.
                    <% } %>
                </td>
            </tr>
            <% } %>
        </tbody>
    </table>
</div>

<script>
    function confirmDelete(userId) {
        // First make an AJAX call to check if the user has reservations
        const xhr = new XMLHttpRequest();
        xhr.open('GET', '${pageContext.request.contextPath}/admin/users/view?id=' + userId, false); // Synchronous request
        xhr.send();

        // If the user has reservations, show a specific message
        if (xhr.responseText.indexOf('This user has active reservations') !== -1) {
            alert('Cannot delete user with active reservations. Please cancel or delete all reservations first.');
            return false;
        }

        // Otherwise ask for normal confirmation
        return confirm('Are you sure you want to delete this user? This action cannot be undone.');
    }
</script>

<%@ include file="admin-footer.jsp" %>