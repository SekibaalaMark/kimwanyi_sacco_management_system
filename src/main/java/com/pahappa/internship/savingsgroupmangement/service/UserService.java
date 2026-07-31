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

        // Validate and sanitize National ID
        requireText(user.getNationalId(), "National ID");
        String nationalId = user.getNationalId().trim();
        if (!nationalId.matches("^[A-Za-z0-9\\-\\/]{4,30}$")) {
            throw new IllegalArgumentException("National ID must be 4-30 alphanumeric characters.");
        }
        user.setNationalId(nationalId);

        // Validate and sanitize Username
        requireText(user.getUsername(), "Username");
        String username = user.getUsername().trim();
        if (!username.matches("^[a-zA-Z0-9_.-]{3,50}$")) {
            throw new IllegalArgumentException("Username must be 3-50 characters (letters, numbers, '.', '_', '-').");
        }
        user.setUsername(username);

        // Validate and sanitize Full Name
        requireText(user.getFullName(), "Full name");
        String fullName = user.getFullName().trim();
        if (!fullName.matches("^[a-zA-Z\\s'\\-]{2,100}$")) {
            throw new IllegalArgumentException("Full name must contain only letters, spaces, hyphens, or apostrophes (2-100 chars).");
        }
        user.setFullName(fullName);

        // Validate, sanitize, and format Phone Number (12 digits + optional leading '+')
        requireText(user.getPhoneNumber(), "Phone number");
        String phone = user.getPhoneNumber().trim().replaceAll("\\s+", "");
        if (!phone.startsWith("+")) {
            phone = "+" + phone;
        }
        if (!phone.matches("^\\+[0-9]{12}$")) {
            throw new IllegalArgumentException("Phone number must contain exactly 12 digits (e.g. 256700000000 or +256700000000).");
        }
        user.setPhoneNumber(phone);

        // Validate and sanitize Email
        requireText(user.getEmail(), "Email address");
        String email = user.getEmail().trim().toLowerCase();
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        user.setEmail(email);

        // Validate Password strength
        requireText(user.getPassword(), "Password");
        if (user.getPassword().length() < 8 || !user.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain both letters and numbers.");
        }

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
            } else {
                throw new Exception("Registration failed due to duplicate entry.");
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
