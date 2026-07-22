package com.pahappa.internship.savingsgroupmangement.web;

import com.pahappa.internship.savingsgroupmangement.model.User;
import com.pahappa.internship.savingsgroupmangement.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("registerBean")
@RequestScoped
public class RegisterBean {

    private User user;

    @Inject
    private  UserService userService;

    @PostConstruct
    public void init() {
        user = new User();
    }

    public String register() {
        try {
            userService.registerMember(user);


            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Registration submitted",
                            "Your account is awaiting administrator activation. You will be able to log in once it is activated."));

            user = new User();
            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registration Error", e.getMessage()));
            return null;
        }
    }


    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
