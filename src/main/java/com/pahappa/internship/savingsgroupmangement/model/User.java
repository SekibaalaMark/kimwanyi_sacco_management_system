package com.pahappa.internship.savingsgroupmangement.model;



import jakarta.persistence.*;
import org.mindrot.jbcrypt.BCrypt;


import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RULE: Each member must have a unique National ID / membership number
    @Column(name = "national_id", unique = true, nullable = false, length = 30)
    private String nationalId;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.MEMBER;

    // RULE: Accounts can be deactivated (Active vs Inactive) instead of deleted
    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public User() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.password != null && !this.password.startsWith("$2a$")) {
            this.password = BCrypt.hashpw(this.password, BCrypt.gensalt());
        }
    }

    public boolean checkPassword(String plainTextPassword) {
        return BCrypt.checkpw(plainTextPassword, this.password);
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
