<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gourmet Reserve - Loading</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/preloader.css" rel="stylesheet">
</head>
<body>
    <%
        // Check if user is already logged in
        boolean isLoggedIn = session.getAttribute("userId") != null;
        // Determine where to redirect after preloader
        String redirectUrl = isLoggedIn ? "/" : "/login.jsp";
    %>

    <div class="preloader">
        <div class="loading-text">
            PREPARING YOUR DINING EXPERIENCE
        </div>

        <!-- Restaurant Icon -->
        <svg class="restaurant-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7V22H22V7L12 2ZM12 7H16M12 7V12M12 7H8M12 12L8 14M12 12L16 14M12 12V17"/>
        </svg>

        <!-- Walking Path -->
        <div class="walking-path"></div>

        <!-- Animated Walking Man -->
        <div class="walking-man">
            <svg class="human-svg" viewBox="0 0 64 64">
                <path fill="var(--primary-gold)" d="M32 12c-4.418 0-8 3.582-8 8s3.582 8 8 8 8-3.582 8-8-3.582-8-8-8zm-8 24c-2.209 0-4 1.791-4 4v12h24V40c0-2.209-1.791-4-4-4H24zm20 16H20v-8h24v8z"/>
                <path fill="none" stroke="var(--primary-gold)" stroke-width="3"
                      d="M24 40l-8-8m24 8l8-8m-16-4v4m0 12v8"
                      stroke-linecap="round">
                    <animate attributeName="stroke-dashoffset"
                             values="0;20;0"
                             dur="0.8s"
                             repeatCount="indefinite"/>
                    <animate attributeName="opacity"
                             values="1;0.5;1"
                             dur="0.8s"
                             repeatCount="indefinite"/>
                </path>
            </svg>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        window.addEventListener('load', function() {
            setTimeout(function() {
                document.querySelector('.preloader').style.display = 'none';
                window.location.href = '${pageContext.request.contextPath}<%= redirectUrl %>';
            }, 4000);
        });
    </script>
</body>
</html>