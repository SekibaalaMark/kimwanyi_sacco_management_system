package com.pahappa.internship.savingsgroupmangement.web;

import com.pahappa.internship.savingsgroupmangement.model.User;
import com.pahappa.internship.savingsgroupmangement.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("registerBean")
@RequestScoped
public class RegisterBean {

    private User user;
    private final UserService userService = new UserService();

    @PostConstruct
    public void init() {
        user = new User();
    }

    public String register() {
        try {
            userService.registerMember(user);

            // Show global success message to user
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Registration complete! You can now log in."));

            // Clear out form by creating a fresh object instance
            user = new User();
            return null; // Stay on the same page to view message
        } catch (Exception e) {
            // Intercept validation exceptions (e.g., non-unique National ID) and show cleanly
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registration Error", e.getMessage()));
            return null;
        }
    }

    // --- Getters and Setters ---
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}