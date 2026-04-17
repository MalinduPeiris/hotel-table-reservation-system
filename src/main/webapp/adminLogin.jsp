<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Login | Gourmet Reserve</title>
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@500&family=Roboto:wght@300;400;500&display=swap" rel="stylesheet">
    <style>
        :root {
            --gold: #D4AF37;
            --burgundy: #800020;
            --dark: #1a1a1a;
            --text: #e0e0e0;
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            min-height: 100vh;
            background: var(--dark);
            font-family: 'Roboto', sans-serif;
            display: flex;
            justify-content: center;
            align-items: center;
            background-image:
                radial-gradient(circle at 10% 20%, rgba(196, 30, 58, 0.1) 0%, transparent 50%),
                radial-gradient(circle at 90% 80%, rgba(255, 215, 0, 0.1) 0%, transparent 50%),
                linear-gradient(rgba(0,0,0,0.8), rgba(0,0,0,0.8)),
                url('${pageContext.request.contextPath}/assets/img/restaurant-bg.jpg');
            background-size: cover;
            background-position: center;
        }

        .login-container {
            background: rgba(30, 30, 30, 0.95);
            padding: 3rem;
            border-radius: 20px;
            border: 1px solid var(--gold);
            width: 90%;
            max-width: 400px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.3);
        }

        .login-header {
            text-align: center;
            margin-bottom: 2rem;
        }

        .login-header h1 {
            font-family: 'Playfair Display', serif;
            color: var(--gold);
            font-size: 2rem;
            margin-bottom: 0.5rem;
        }

        .form-group {
            margin-bottom: 1.5rem;
        }

        .form-label {
            display: block;
            margin-bottom: 0.5rem;
            color: var(--gold);
        }

        .form-input {
            width: 100%;
            padding: 0.8rem;
            background: rgba(255, 255, 255, 0.1);
            border: 1px solid var(--gold);
            border-radius: 6px;
            color: var(--text);
            font-size: 1rem;
        }

        .submit-btn {
            width: 100%;
            padding: 1rem;
            background: var(--gold);
            border: none;
            border-radius: 6px;
            color: var(--dark);
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.3s ease;
        }

        .submit-btn:hover {
            transform: translateY(-2px);
        }

        .error-message {
            color: #ff4444;
            text-align: center;
            margin-top: 1rem;
        }

        .back-to-main {
            display: block;
            text-align: center;
            margin-top: 1.5rem;
            color: var(--text);
            text-decoration: none;
            font-size: 0.9rem;
            opacity: 0.8;
            transition: opacity 0.3s;
        }

        .back-to-main:hover {
            opacity: 1;
            color: var(--gold);
        }
    </style>
</head>
<body>
    <div class="login-container">
        <div class="login-header">
            <h1>Admin Portal</h1>
            <p style="color: #ccc;">Restricted Access</p>
        </div>

        <form id="loginForm" action="${pageContext.request.contextPath}/admin/login" method="post">
            <div class="form-group">
                <label class="form-label">Username</label>
                <input type="text" class="form-input" id="username" name="username" required>
            </div>

            <div class="form-group">
                <label class="form-label">Password</label>
                <input type="password" class="form-input" id="password" name="password" required>
            </div>

            <button type="submit" class="submit-btn">Sign In</button>

            <% if (request.getAttribute("errorMessage") != null) { %>
                <div class="error-message">
                    <%= request.getAttribute("errorMessage") %>
                </div>
            <% } %>
        </form>

        <a href="${pageContext.request.contextPath}/" class="back-to-main">Return to Main Page</a>
    </div>

    <script>
        document.getElementById('loginForm').addEventListener('submit', function(e) {
            // Basic client-side validation
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            if (!username || !password) {
                e.preventDefault();
                alert('Please enter both username and password');
            }
        });
    </script>
</body>
</html>