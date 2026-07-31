package com.pahappa.internship.savingsgroupmangement.service;

import com.pahappa.internship.savingsgroupmangement.dao.LoanDAO;
import com.pahappa.internship.savingsgroupmangement.dao.TransactionDAO;
import com.pahappa.internship.savingsgroupmangement.dao.UserDAO;
import com.pahappa.internship.savingsgroupmangement.dto.ActivityLogEntry;
import com.pahappa.internship.savingsgroupmangement.dto.AdminSummaryDTO;
import com.pahappa.internship.savingsgroupmangement.model.Loan;
import com.pahappa.internship.savingsgroupmangement.model.LoanStatus;
import com.pahappa.internship.savingsgroupmangement.model.Transaction;
import com.pahappa.internship.savingsgroupmangement.model.TransactionType;
import com.pahappa.internship.savingsgroupmangement.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    public List<User> getAllMembers() {
        return userDAO.findAllMembers();
    }

    public void toggleUserActiveStatus(Long userId, boolean newStatus) {
        userDAO.updateStatus(userId, newStatus);
    }

    /**
     * Builds a unified, time-sorted activity log from all transactions and all loan events.
     */
    public List<ActivityLogEntry> buildActivityLog() {
        List<ActivityLogEntry> log = new ArrayList<>();
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
        fmt.setMaximumFractionDigits(0);

        // ── build memberId → username lookup from loan events ──
        Map<Long, String> memberNames = new HashMap<>();
        for (User u : userDAO.findAllMembers()) {
            memberNames.put(u.getId(), u.getUsername());
        }

        // ── Transaction events ──
        for (Transaction tx : transactionDAO.findAll()) {
            String username = tx.getUser() != null ? tx.getUser().getUsername() : "Unknown";
            String desc     = tx.getDescription();
            Double amt      = tx.getAmount();
            String fmtAmt   = "UGX " + fmt.format(amt);

            ActivityLogEntry.Category cat;
            String detail;

            if (desc != null && desc.startsWith("Transfer to ")) {
                cat    = ActivityLogEntry.Category.TRANSFER_OUT;
                detail = username + " transferred " + fmtAmt + " → " + desc.substring("Transfer to ".length());
            } else if (desc != null && desc.startsWith("Transfer from ")) {
                cat    = ActivityLogEntry.Category.TRANSFER_IN;
                detail = username + " received " + fmtAmt + " ← " + desc.substring("Transfer from ".length());
            } else if (desc != null && desc.startsWith("Loan repayment")) {
                cat    = ActivityLogEntry.Category.LOAN_REPAID;
                detail = username + " repaid " + fmtAmt + " (" + desc + ")";
            } else if (tx.getType() == TransactionType.DEPOSIT) {
                cat    = ActivityLogEntry.Category.DEPOSIT;
                detail = username + " deposited " + fmtAmt;
            } else {
                cat    = ActivityLogEntry.Category.WITHDRAWAL;
                detail = username + " withdrew " + fmtAmt;
            }

            log.add(new ActivityLogEntry(tx.getCreatedAt(), cat, username, detail, amt));
        }

        // ── Loan events ──
        for (Loan loan : loanDAO.findAll()) {
            String username = memberNames.getOrDefault(loan.getMemberId(), "Member #" + loan.getMemberId());
            String fmtAmt   = "UGX " + fmt.format(loan.getPrincipalAmount());

            // Applied
            log.add(new ActivityLogEntry(
                loan.getAppliedAt(),
                ActivityLogEntry.Category.LOAN_APPLIED,
                username,
                username + " applied for loan of " + fmtAmt,
                loan.getPrincipalAmount().doubleValue()
            ));

            // Approved
            if (loan.getApprovedAt() != null &&
                (loan.getStatus() == LoanStatus.APPROVED || loan.getStatus() == LoanStatus.PAID)) {
                log.add(new ActivityLogEntry(
                    loan.getApprovedAt(),
                    ActivityLogEntry.Category.LOAN_APPROVED,
                    "Admin",
                    "Loan #" + loan.getId() + " for " + username + " approved — " + fmtAmt + " disbursed",
                    loan.getPrincipalAmount().doubleValue()
                ));
            }

            // Rejected
            if (loan.getStatus() == LoanStatus.REJECTED && loan.getApprovedAt() != null) {
                String reason = loan.getRejectionReason() != null ? ": " + loan.getRejectionReason() : "";
                log.add(new ActivityLogEntry(
                    loan.getApprovedAt(),
                    ActivityLogEntry.Category.LOAN_REJECTED,
                    "Admin",
                    "Loan #" + loan.getId() + " for " + username + " rejected" + reason,
                    null
                ));
            }
        }

        // Sort newest first
        log.sort(Comparator.comparing(ActivityLogEntry::getTimestamp).reversed());
        return log;
    }
}