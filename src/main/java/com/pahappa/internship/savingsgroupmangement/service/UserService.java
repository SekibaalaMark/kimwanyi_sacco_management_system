package com.pahappa.internship.savingsgroupmangement.service;


import com.pahappa.internship.savingsgroupmangement.dao.UserDAO;
import com.pahappa.internship.savingsgroupmangement.model.User;

public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public void registerMember(User user) throws Exception {
        // Enforce RULE: Unique National ID check
        if (userDAO.findByNationalId(user.getNationalId()) != null) {
            throw new Exception("Registration failed: A member with this National ID already exists.");
        }

        // Validate unique username
        if (userDAO.findByUsername(user.getUsername()) != null) {
            throw new Exception("Registration failed: This username is already taken.");
        }

        // RULE: Ensure registration here explicitly sets MEMBER role
        // (Admin creation must bypass this or happen through a separate secure process)
        user.setRole(com.pahappa.internship.savingsgroupmangement.model.Role.MEMBER);
        user.setActive(true);

        userDAO.saveUser(user);
    }
}
