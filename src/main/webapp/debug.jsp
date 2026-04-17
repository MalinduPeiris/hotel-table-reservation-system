<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<!DOCTYPE html>
<html>
<head>
    <title>System Debug Information</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
            line-height: 1.6;
        }
        h1, h2 {
            color: #333;
        }
        .section {
            margin-bottom: 30px;
            padding: 15px;
            background-color: #f9f9f9;
            border-radius: 5px;
            border: 1px solid #ddd;
        }
        .property {
            margin-bottom: 5px;
        }
        .key {
            font-weight: bold;
            color: #555;
        }
        .value {
            margin-left: 10px;
            word-break: break-all;
        }
        .error {
            color: red;
            font-weight: bold;
        }
        .success {
            color: green;
            font-weight: bold;
        }
    </style>
</head>
<body>
    <h1>System Debug Information</h1>

    <div class="section">
        <h2>Session Information</h2>
        <%
        HttpSession userSession = request.getSession(false);
        if (userSession != null) {
            out.println("<p class='success'>Session exists with ID: " + userSession.getId() + "</p>");
            out.println("<h3>Session Attributes:</h3>");
            Enumeration<String> attributeNames = userSession.getAttributeNames();
            if (!attributeNames.hasMoreElements()) {
                out.println("<p>No session attributes found.</p>");
            } else {
                while (attributeNames.hasMoreElements()) {
                    String name = attributeNames.nextElement();
                    Object value = userSession.getAttribute(name);
                    out.println("<div class='property'>");
                    out.println("<span class='key'>" + name + ":</span>");
                    out.println("<span class='value'>" + value + "</span>");
                    out.println("</div>");
                }
            }
        } else {
            out.println("<p class='error'>No session exists!</p>");
        }
        %>
    </div>

    <div class="section">
        <h2>Request Parameters</h2>
        <%
        Enumeration<String> paramNames = request.getParameterNames();
        if (!paramNames.hasMoreElements()) {
            out.println("<p>No request parameters found.</p>");
        } else {
            while (paramNames.hasMoreElements()) {
                String name = paramNames.nextElement();
                String[] values = request.getParameterValues(name);
                out.println("<div class='property'>");
                out.println("<span class='key'>" + name + ":</span>");
                if (values.length == 1) {
                    out.println("<span class='value'>" + values[0] + "</span>");
                } else {
                    out.println("<span class='value'>");
                    for (String value : values) {
                        out.println(value + "<br>");
                    }
                    out.println("</span>");
                }
                out.println("</div>");
            }
        }
        %>
    </div>

    <div class="section">
        <h2>Application Initialization</h2>
        <%
        String dataPath = System.getProperty("app.datapath");
        if (dataPath != null) {
            out.println("<p class='success'>app.datapath is set to: " + dataPath + "</p>");

            // Check if directory exists
            File dataDir = new File(dataPath);
            if (dataDir.exists() && dataDir.isDirectory()) {
                out.println("<p class='success'>Data directory exists</p>");

                // List files in directory
                out.println("<h3>Files in data directory:</h3>");
                File[] files = dataDir.listFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        out.println("<div class='property'>");
                        out.println("<span class='key'>" + file.getName() + ":</span>");
                        out.println("<span class='value'>" + file.length() + " bytes, last modified: " +
                                    new Date(file.lastModified()) + "</span>");
                        out.println("</div>");
                    }
                } else {
                    out.println("<p class='error'>No files found in data directory</p>");
                }
            } else {
                out.println("<p class='error'>Data directory does not exist or is not a directory!</p>");
            }
        } else {
            out.println("<p class='error'>app.datapath is not set! Application initialization may have failed.</p>");
        }
        %>
    </div>

    <div class="section">
        <h2>System Properties</h2>
        <%
        Properties props = System.getProperties();
        Enumeration<?> propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String name = (String) propNames.nextElement();
            String value = props.getProperty(name);
            out.println("<div class='property'>");
            out.println("<span class='key'>" + name + ":</span>");
            out.println("<span class='value'>" + value + "</span>");
            out.println("</div>");
        }
        %>
    </div>

    <div class="section">
        <h2>Servlet and JSP Paths</h2>
        <div class='property'>
            <span class='key'>Context Path:</span>
            <span class='value'><%= request.getContextPath() %></span>
        </div>
        <div class='property'>
            <span class='key'>Servlet Path:</span>
            <span class='value'><%= request.getServletPath() %></span>
        </div>
        <div class='property'>
            <span class='key'>Real Path:</span>
            <span class='value'><%= application.getRealPath("/") %></span>
        </div>
    </div>

    <div class="section">
        <h2>Test Servlet Mapping</h2>
        <form action="${pageContext.request.contextPath}/reservation/createReservation" method="post">
            <input type="date" name="reservationDate" value="<%= java.time.LocalDate.now() %>">
            <input type="time" name="reservationTime" value="12:00">
            <input type="hidden" name="bookingType" value="normal">
            <input type="submit" value="Test Reservation Form Submit">
        </form>
        <p>Click the button above to test the form submission to the /reservation/createReservation endpoint.</p>
    </div>
</body>
</html>