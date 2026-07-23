package com.pahappa.internship.savingsgroupmangement.service;


import com.pahappa.internship.savingsgroupmangement.dao.UserDAO;
import com.pahappa.internship.savingsgroupmangement.model.Role;
import com.pahappa.internship.savingsgroupmangement.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.exception.ConstraintViolationException;

@ApplicationScoped
public class UserService {

    @Inject
    private UserDAO userDAO;

    public void registerMember(User user) throws Exception {
        if (user == null) {
            throw new IllegalArgumentException("Registration details are required.");
        }
        requireText(user.getFullName(), "Full name");
        requireText(user.getPhoneNumber(), "Phone number");
        requireText(user.getEmail(), "Email address");
        if (!user.getEmail().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }

        user.setFullName(user.getFullName().trim());
        user.setPhoneNumber(user.getPhoneNumber().trim());
        user.setEmail(user.getEmail().trim().toLowerCase());
        user.setRole(Role.MEMBER);
        // New members require administrator approval before they can log in.
        user.setActive(false);

        try {
            userDAO.saveUser(user);
        } catch (ConstraintViolationException e) {
            if (e.getMessage().contains("national_id")) {
                throw new Exception("National ID already exists");
            } else if (e.getMessage().contains("username")) {
                throw new Exception("Username already exists");
            }
        }
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }


    public User authenticate(String username, String plainTextPassword) throws Exception {
        User user = userDAO.findByUsername(username);

        if (user == null) {
            throw new Exception("Invalid username or password.");
        }

        if (!user.isActive()) {
            throw new Exception("This account is awaiting administrator activation. Please contact the administrator.");
        }

        if (!user.checkPassword(plainTextPassword)) {
            throw new Exception("Invalid username or password.");
        }

        return user;
    }
}
