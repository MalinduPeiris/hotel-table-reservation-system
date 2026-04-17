package com.tablebooknow.config;

import com.tablebooknow.dao.TableDAO;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;

/**
 * Initializes the default tables on application startup.
 */
@WebListener
public class TableInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Initializing default tables");

        try {
            // Initialize default tables
            TableDAO tableDAO = new TableDAO();
            tableDAO.initializeDefaultTables();

            System.out.println("Tables initialization completed successfully");
        } catch (IOException e) {
            System.err.println("Failed to initialize tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup if needed
    }
}