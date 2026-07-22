package com.pahappa.internship.savingsgroupmangement.service;




import com.pahappa.internship.savingsgroupmangement.dao.TransactionDAO;
import com.pahappa.internship.savingsgroupmangement.dao.UserDAO;
import com.pahappa.internship.savingsgroupmangement.model.Role;
import com.pahappa.internship.savingsgroupmangement.model.Transaction;
import com.pahappa.internship.savingsgroupmangement.model.TransactionType;
import com.pahappa.internship.savingsgroupmangement.model.User;

import java.util.List;

public class TransactionService {

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final UserDAO userDAO = new UserDAO();

    public void executeTransaction(User user, Double amount, TransactionType type) throws Exception {
        // Enforce boundary parameters: No negative or empty entries
        if (amount == null || amount <= 0) {
            throw new Exception("Transaction failed: The amount must be greater than zero.");
        }

        // Rule check for Withdrawals
        if (type == TransactionType.WITHDRAWAL) {
            Double MINIMUM_WITHDRAWABLE = 20000.0;
            Double currentBalance = transactionDAO.calculateTotalBalance(user.getId());
            if (amount > (currentBalance-MINIMUM_WITHDRAWABLE)) {
                throw new Exception("Transaction failed: Insufficient account funds. Current balance: " + currentBalance);
            }
        }

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(amount);
        transaction.setType(type);

        transactionDAO.saveTransaction(transaction);
    }

    public User transfer(User sender, String recipientNationalId, Double amount) throws Exception {
        if (sender == null || sender.getId() == null) {
            throw new Exception("Transfer failed: please log in again.");
        }
        if (recipientNationalId == null || recipientNationalId.trim().isEmpty()) {
            throw new Exception("Transfer failed: recipient National ID is required.");
        }
        if (amount == null || amount <= 0) {
            throw new Exception("Transfer failed: the amount must be greater than zero.");
        }

        return transactionDAO.transfer(sender.getId(), recipientNationalId.trim(), amount);
    }

    public User findTransferRecipient(String nationalId) throws Exception {
        if (nationalId == null || nationalId.trim().isEmpty()) {
            throw new Exception("Recipient National ID is required.");
        }

        User recipient = userDAO.findByNationalId(nationalId.trim());
        if (recipient == null || !recipient.isActive() || recipient.getRole() != Role.MEMBER) {
            throw new Exception("No active member was found with that National ID.");
        }
        return recipient;
    }

    public List<Transaction> getUserTransactionHistory(Long userId) {
        return transactionDAO.findTransactionsByUser(userId);
    }

    public Double getUserBalance(Long userId) {
        return transactionDAO.calculateTotalBalance(userId);
    }
}
