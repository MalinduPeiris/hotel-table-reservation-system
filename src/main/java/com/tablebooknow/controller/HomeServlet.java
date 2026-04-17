package com.tablebooknow.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet that handles requests to the root URL of the application.
 * Redirects to the home page or login page based on login status.
 */
@WebServlet("/")
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        boolean isLoggedIn = session != null && session.getAttribute("userId") != null;

        // Get referer to check where the request is coming from
        String referer = request.getHeader("Referer");
        boolean isFromLogin = referer != null && (referer.contains("/login.jsp") || referer.contains("/user/login"));

        // Get any showPreloader parameter
        String showPreloader = request.getParameter("showPreloader");

        // Check if we should show preloader
        // 1. User has just logged in (coming from login page)
        // 2. Explicitly requested via parameter
        // 3. No referer (direct access to root URL)
        if ((isFromLogin && isLoggedIn) || "true".equals(showPreloader) || referer == null) {
            request.getRequestDispatcher("/preloader.jsp").forward(request, response);
        } else {
            // Otherwise, just show the home page
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }
}