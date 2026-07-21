package com.pahappa.internship.savingsgroupmangement.service;




import com.pahappa.internship.savingsgroupmangement.dao.TransactionDAO;
import com.pahappa.internship.savingsgroupmangement.model.Transaction;
import com.pahappa.internship.savingsgroupmangement.model.TransactionType;
import com.pahappa.internship.savingsgroupmangement.model.User;

import java.util.List;

public class TransactionService {

    private final TransactionDAO transactionDAO = new TransactionDAO();

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

    public List<Transaction> getUserTransactionHistory(Long userId) {
        return transactionDAO.findTransactionsByUser(userId);
    }

    public Double getUserBalance(Long userId) {
        return transactionDAO.calculateTotalBalance(userId);
    }
}
