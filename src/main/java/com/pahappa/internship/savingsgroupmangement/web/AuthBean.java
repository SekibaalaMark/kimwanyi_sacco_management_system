package com.pahappa.internship.savingsgroupmangement.web;

import com.pahappa.internship.savingsgroupmangement.model.Role;
import com.pahappa.internship.savingsgroupmangement.model.User;
import com.pahappa.internship.savingsgroupmangement.service.UserService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

@Named("authBean")
@SessionScoped
public class AuthBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private User currentUser;

    @Inject
    private UserService userService;

    public String login() {
        try {
            // Authenticate user against DB
            User authenticatedUser = userService.authenticate(username, password);

            // Rule Check: Block login for deactivated accounts
            if (!authenticatedUser.isActive()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Access Denied",
                                "Your account has been deactivated by an administrator. Please contact management."));
                return null;
            }

            this.currentUser = authenticatedUser;

            // Clean up credentials from bean memory immediately after auth
            this.password = null;

            // Route user based on their specific role profile
            if (currentUser.getRole() == Role.ADMIN) {
                return "/admin/dashboard.xhtml?faces-redirect=true";
            } else {
                return "/member/dashboard.xhtml?faces-redirect=true";
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login Failed", e.getMessage()));
            return null;
        }
    }

    public String logout() {
        // Explicitly invalidate the container HTTP Session
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/index.xhtml?faces-redirect=true";
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    // --- Getters and Setters ---
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User currentUser) { this.currentUser = currentUser; }
}