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

    private Double amount;
    private Double transferAmount;
    private String recipientNationalId;
    private User verifiedRecipient;
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

    public void transfer() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            if (verifiedRecipient == null || !verifiedRecipient.getNationalId().equals(recipientNationalId == null ? null : recipientNationalId.trim())) {
                throw new Exception("Please verify the recipient before confirming this transfer.");
            }
            User recipient = transactionService.transfer(authBean.getCurrentUser(), recipientNationalId, transferAmount);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Transfer successful",
                    "UGX " + transferAmount + " sent to " + recipient.getUsername() + "."));
            transferAmount = null;
            recipientNationalId = null;
            verifiedRecipient = null;
            refreshLedger();
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Transfer Error", e.getMessage()));
        }
    }

    public void verifyRecipient() {
        FacesContext context = FacesContext.getCurrentInstance();
        verifiedRecipient = null;
        try {
            User recipient = transactionService.findTransferRecipient(recipientNationalId);
            if (authBean.getCurrentUser().getId().equals(recipient.getId())) {
                throw new Exception("You cannot send money to your own account.");
            }
            verifiedRecipient = recipient;
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Recipient confirmed",
                    "You are sending money to " + recipient.getUsername() + "."));
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Recipient Error", e.getMessage()));
        }
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

    public String getRecipientNationalId() { return recipientNationalId; }
    public void setRecipientNationalId(String recipientNationalId) { this.recipientNationalId = recipientNationalId; }

    public User getVerifiedRecipient() { return verifiedRecipient; }

    public Double getCurrentBalance() { return currentBalance; }
    public List<Transaction> getTransactionHistory() { return transactionHistory; }
}
