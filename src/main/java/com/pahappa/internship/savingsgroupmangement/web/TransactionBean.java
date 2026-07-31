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

    @Inject
    private TransactionService transactionService;

    @Inject
    private AuthBean authBean;

    private Double  amount;
    private Double  transferAmount;
    private String  selectedRecipientUsername;
    private List<User>        activeRecipients;
    private Double            currentBalance;
    private List<Transaction> transactionHistory;
    private String            activeSection = "overview";

    @PostConstruct
    public void init() {
        refreshLedger();
    }

    public void refreshLedger() {
        User user = authBean.getCurrentUser();
        if (user != null) {
            this.currentBalance     = transactionService.getUserBalance(user.getId());
            this.transactionHistory = transactionService.getUserTransactionHistory(user.getId());
            this.activeRecipients   = transactionService.getActiveTransferRecipients(user.getId());
        }
    }

    public void showSection(String section) {
        this.activeSection = section;
        refreshLedger();
    }

    public void deposit() {
        handleTransaction(TransactionType.DEPOSIT, "Deposit of UGX " + amount + " successful!");
    }

    public void withdraw() {
        handleTransaction(TransactionType.WITHDRAWAL, "Withdrawal of UGX " + amount + " successful!");
    }

    public void transfer() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            if (selectedRecipientUsername == null || selectedRecipientUsername.trim().isEmpty()) {
                throw new Exception("Please select a recipient member from the list.");
            }
            if (transferAmount == null || transferAmount <= 0) {
                throw new Exception("Please enter a valid transfer amount.");
            }
            User recipient = transactionService.transferByUsername(authBean.getCurrentUser(), selectedRecipientUsername, transferAmount);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Transfer Successful",
                    "UGX " + transferAmount + " sent to " + recipient.getUsername() + " (" + recipient.getFullName() + ")."));
            transferAmount = null;
            selectedRecipientUsername = null;
            refreshLedger();
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Transfer Failed", e.getMessage()));
        }
    }

    public User getSelectedRecipientUser() {
        if (selectedRecipientUsername == null || activeRecipients == null) return null;
        return activeRecipients.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(selectedRecipientUsername))
                .findFirst()
                .orElse(null);
    }

    private void handleTransaction(TransactionType type, String successMessage) {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            User user = authBean.getCurrentUser();
            transactionService.executeTransaction(user, amount, type);

            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", successMessage));
            amount = null;
            refreshLedger();
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Transaction Error", e.getMessage()));
        }
    }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Double getTransferAmount() { return transferAmount; }
    public void setTransferAmount(Double transferAmount) { this.transferAmount = transferAmount; }

    public String getSelectedRecipientUsername() { return selectedRecipientUsername; }
    public void setSelectedRecipientUsername(String selectedRecipientUsername) { this.selectedRecipientUsername = selectedRecipientUsername; }

    public List<User>        getActiveRecipients()   { return activeRecipients;   }
    public Double            getCurrentBalance()      { return currentBalance;     }
    public List<Transaction> getTransactionHistory()  { return transactionHistory; }
    public String            getActiveSection()       { return activeSection;      }
    public void              setActiveSection(String s){ this.activeSection = s;   }
}
