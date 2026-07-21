package com.pahappa.internship.savingsgroupmangement.config;

import com.pahappa.internship.savingsgroupmangement.dao.UserDAO;
import com.pahappa.internship.savingsgroupmangement.model.Role;
import com.pahappa.internship.savingsgroupmangement.model.User;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class DataInitializer implements ServletContextListener {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            // Check if default admin exists
            User existingAdmin = userDAO.findByUsername("admin");

            if (existingAdmin == null) {
                User admin = new User();
                admin.setNationalId("SYS-ADMIN-001"); // Satisfies nullable = false & unique constraint
                admin.setUsername("admin");
                admin.setPassword("admin123"); // Set plain text; @PrePersist in User hashes it with BCrypt
                admin.setRole(Role.ADMIN);
                admin.setActive(true);

                userDAO.saveUser(admin);

                System.out.println("=========================================");
                System.out.println(" DEFAULT ADMIN CREATED ");
                System.out.println(" Username: admin ");
                System.out.println(" Password: admin123 ");
                System.out.println(" Role: ADMIN ");
                System.out.println("=========================================");
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize default admin user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup if needed
    }
}