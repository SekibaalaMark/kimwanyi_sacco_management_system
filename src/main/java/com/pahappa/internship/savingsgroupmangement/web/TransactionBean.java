package com.pahappa.internship.savingsgroupmangement.web;

import com.pahappa.internship.savingsgroupmangement.model.Transaction;
import com.pahappa.internship.savingsgroupmangement.model.TransactionType;
import com.pahappa.internship.savingsgroupmangement.model.User;
import com.pahappa.internship.savingsgroupmangement.service.TransactionService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class TransactionBean implements Serializable {

    private final TransactionService transactionService = new TransactionService();

    @Inject
    private AuthBean authBean; // Grabs the active logged-in user session

    private Double amount;
    private Double currentBalance;
    private List<Transaction> transactionHistory;

    @PostConstruct
    public void init() {
        refreshLedger();
    }

    public void refreshLedger() {
        User user = authBean.getCurrentUser();
        if (user != null) {
            this.currentBalance = transactionService.getUserBalance(user.getId());
            this.transactionHistory = transactionService.getUserTransactionHistory(user.getId());
        }
    }

    public void deposit() {
        handleTransaction(TransactionType.DEPOSIT, "Deposit of UGX" + amount + " successful!");
    }

    public void withdraw() {
        handleTransaction(TransactionType.WITHDRAWAL, "Withdrawal of UGX" + amount + " successful!");
    }

    private void handleTransaction(TransactionType type, String successMessage) {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            User user = authBean.getCurrentUser();
            transactionService.executeTransaction(user, amount, type);

            // Success routine
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", successMessage));
            amount = null; // Clear input field
            refreshLedger(); // Update calculations on screen
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Transaction Error", e.getMessage()));
        }
    }

    // --- Getters and Setters ---
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Double getCurrentBalance() { return currentBalance; }
    public List<Transaction> getTransactionHistory() { return transactionHistory; }
}