package com.pahappa.internship.savingsgroupmangement.service;

import com.pahappa.internship.savingsgroupmangement.dao.TransactionDAO;
import com.pahappa.internship.savingsgroupmangement.dao.UserDAO;
import com.pahappa.internship.savingsgroupmangement.dto.StatementDTO;
import com.pahappa.internship.savingsgroupmangement.model.Transaction;
import com.pahappa.internship.savingsgroupmangement.model.TransactionType;
import com.pahappa.internship.savingsgroupmangement.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class StatementService {

    @Inject
    private TransactionDAO transactionDAO;

    @Inject
    private UserDAO userDAO;

    public StatementDTO generateStatementForMember(Long memberId) {


        User member = userDAO.findById(memberId);

        if (member == null) {
            throw new IllegalArgumentException("Member not found.");
        }

        List<Transaction> history = transactionDAO.getTransactionsByMemberId(memberId);


        BigDecimal currentBalance = transactionDAO.getMemberBalance(memberId);


        BigDecimal totalDeposits = BigDecimal.ZERO;
        BigDecimal totalWithdrawals = BigDecimal.ZERO;


        for (Transaction transaction : history) {

            BigDecimal amount = BigDecimal.valueOf(transaction.getAmount());

            if (transaction.getType() == TransactionType.DEPOSIT) {
                totalDeposits = totalDeposits.add(amount);
            }

            if (transaction.getType() == TransactionType.WITHDRAWAL) {
                totalWithdrawals = totalWithdrawals.add(amount);
            }
        }


        return new StatementDTO(
                member,
                currentBalance,
                totalDeposits,
                totalWithdrawals,
                history
        );
    }
}