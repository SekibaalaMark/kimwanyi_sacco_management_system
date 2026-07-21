package com.pahappa.internship.savingsgroupmangement.dto;

import com.pahappa.internship.savingsgroupmangement.model.Transaction;
import com.pahappa.internship.savingsgroupmangement.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class StatementDTO {
    private User member;
    private LocalDateTime generatedAt;
    private BigDecimal currentBalance;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private List<Transaction> transactions;

    public StatementDTO(User member, BigDecimal currentBalance, BigDecimal totalDeposits,
                        BigDecimal totalWithdrawals, List<Transaction> transactions) {
        this.member = member;
        this.generatedAt = LocalDateTime.now();
        this.currentBalance = currentBalance;
        this.totalDeposits = totalDeposits;
        this.totalWithdrawals = totalWithdrawals;
        this.transactions = transactions;
    }

    // Getters and Setters
    public User getMember() { return member; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public BigDecimal getCurrentBalance() { return currentBalance; }
    public BigDecimal getTotalDeposits() { return totalDeposits; }
    public BigDecimal getTotalWithdrawals() { return totalWithdrawals; }
    public List<Transaction> getTransactions() { return transactions; }
}