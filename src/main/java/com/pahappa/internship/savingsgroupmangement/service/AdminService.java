package com.pahappa.internship.savingsgroupmangement.service;

import com.pahappa.internship.savingsgroupmangement.dao.LoanDAO;
import com.pahappa.internship.savingsgroupmangement.dao.TransactionDAO;
import com.pahappa.internship.savingsgroupmangement.dao.UserDAO;
import com.pahappa.internship.savingsgroupmangement.dto.AdminSummaryDTO;
import com.pahappa.internship.savingsgroupmangement.model.Loan;
import com.pahappa.internship.savingsgroupmangement.model.LoanStatus;
import com.pahappa.internship.savingsgroupmangement.model.TransactionType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class AdminService {

    @Inject
    private UserDAO userDAO;

    @Inject
    private LoanDAO loanDAO;

    @Inject
    private TransactionDAO transactionDAO;

    public AdminSummaryDTO getDashboardSummary() {
        long totalMembers = userDAO.countAll();

        List<Loan> loans = loanDAO.findAll();

        long activeLoans = loans.stream()
                .filter(l -> l.getStatus() == LoanStatus.APPROVED)
                .count();

        long pendingLoans = loans.stream()
                .filter(l -> l.getStatus() == LoanStatus.PENDING)
                .count();

        // Calculate direct totals from TransactionDAO
        BigDecimal totalDeposits = transactionDAO.getTotalByTransactionType(TransactionType.DEPOSIT);
        BigDecimal totalWithdrawals = transactionDAO.getTotalByTransactionType(TransactionType.WITHDRAWAL);

        BigDecimal totalDisbursed = loans.stream()
                .filter(l -> l.getStatus() == LoanStatus.APPROVED || l.getStatus() == LoanStatus.PAID)
                .map(Loan::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netLiquidity = totalDeposits.subtract(totalWithdrawals);

        return new AdminSummaryDTO(
                totalMembers,
                activeLoans,
                pendingLoans,
                totalDeposits,
                totalWithdrawals,
                totalDisbursed,
                netLiquidity
        );
    }
}