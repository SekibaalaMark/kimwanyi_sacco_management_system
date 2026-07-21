package com.pahappa.internship.savingsgroupmangement.service;

import com.pahappa.internship.savingsgroupmangement.dao.LoanDAO;
import com.pahappa.internship.savingsgroupmangement.dao.TransactionDAO;
import com.pahappa.internship.savingsgroupmangement.dao.UserDAO; // Added to fetch User entity
import com.pahappa.internship.savingsgroupmangement.model.Loan;
import com.pahappa.internship.savingsgroupmangement.model.LoanStatus;
import com.pahappa.internship.savingsgroupmangement.model.Transaction;
import com.pahappa.internship.savingsgroupmangement.model.TransactionType;
import com.pahappa.internship.savingsgroupmangement.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class LoanService {

    public static final BigDecimal INTEREST_RATE = new BigDecimal("0.10"); // 10% flat interest rate

    @Inject
    private LoanDAO loanDAO;

    @Inject
    private TransactionDAO transactionDAO;

    @Inject
    private UserDAO userDAO; // Injected to retrieve the borrower user entity

    /**
     * Apply for a new loan
     */
    public void applyForLoan(Long memberId, BigDecimal requestedAmount) {
        if (requestedAmount == null || requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Requested loan amount must be greater than zero.");
        }

        // Rule 1: A member may only hold one active or pending loan at a time
        if (loanDAO.hasActiveOrPendingLoan(memberId)) {
            throw new IllegalStateException("You already have an active or pending loan application. Please settle it first.");
        }

        // Rule 2: Maximum loan amount is 3x current savings balance
        BigDecimal savingsBalance = transactionDAO.getMemberBalance(memberId);
        BigDecimal maxLoanAllowed = savingsBalance.multiply(new BigDecimal("3")).setScale(2, RoundingMode.HALF_UP);

        if (requestedAmount.compareTo(maxLoanAllowed) > 0) {
            throw new IllegalStateException(
                    String.format("Loan denied. Maximum allowed loan is 3x your savings balance (UGX %,.2f). Max eligible: UGX %,.2f.",
                            savingsBalance, maxLoanAllowed)
            );
        }

        // Calculate 10% flat rate interest
        BigDecimal interestAmount = requestedAmount.multiply(INTEREST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = requestedAmount.add(interestAmount);

        Loan loan = new Loan();
        loan.setMemberId(memberId);
        loan.setPrincipalAmount(requestedAmount);
        loan.setInterestAmount(interestAmount);
        loan.setTotalAmount(totalAmount);
        loan.setAmountPaid(BigDecimal.ZERO);
        loan.setStatus(LoanStatus.PENDING);
        loan.setAppliedAt(LocalDateTime.now());

        loanDAO.save(loan);
    }

    /**
     * Approve a pending loan (Admin action)
     * Automatically credits (DEPOSIT) the principal amount to the member's account balance.
     */
    public void approveLoan(Long loanId) {
        Loan loan = loanDAO.findById(loanId);
        if (loan == null) throw new IllegalArgumentException("Loan record not found.");
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException("Only PENDING loans can be approved.");
        }

        // 1. Fetch the member/user entity
        User borrower = userDAO.findById(loan.getMemberId());
        if (borrower == null) {
            throw new IllegalStateException("Borrower user record not found.");
        }

        // 2. Mark the loan as APPROVED
        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovedAt(LocalDateTime.now());
        loanDAO.update(loan);

        // 3. Automatically top up member's account balance by creating a DEPOSIT transaction
        Transaction loanDisbursement = new Transaction();
        loanDisbursement.setUser(borrower);
        loanDisbursement.setType(TransactionType.DEPOSIT);
        loanDisbursement.setAmount(loan.getPrincipalAmount().doubleValue());

        transactionDAO.saveTransaction(loanDisbursement);
    }

    /**
     * Reject a pending loan (Admin action)
     */
    public void rejectLoan(Long loanId, String reason) {
        Loan loan = loanDAO.findById(loanId);
        if (loan == null) throw new IllegalArgumentException("Loan record not found.");
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException("Only PENDING loans can be rejected.");
        }

        loan.setStatus(LoanStatus.REJECTED);
        loan.setRejectionReason(reason);
        loanDAO.update(loan);
    }

    /**
     * Repay an active loan
     */
    public void repayLoan(Long loanId, BigDecimal paymentAmount) {
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Repayment amount must be greater than zero.");
        }

        Loan loan = loanDAO.findById(loanId);
        if (loan == null) throw new IllegalArgumentException("Loan record not found.");
        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException("Only active APPROVED loans can be repaid.");
        }

        BigDecimal remainingBalance = loan.getRemainingBalance();
        if (paymentAmount.compareTo(remainingBalance) > 0) {
            throw new IllegalArgumentException(
                    String.format("Payment exceeds loan balance. Outstanding balance is UGX %,.2f.", remainingBalance)
            );
        }

        BigDecimal newAmountPaid = loan.getAmountPaid().add(paymentAmount);
        loan.setAmountPaid(newAmountPaid);

        // Mark fully paid if remaining balance hits zero
        if (newAmountPaid.compareTo(loan.getTotalAmount()) >= 0) {
            loan.setStatus(LoanStatus.PAID);
        }

        loanDAO.update(loan);
    }

    public List<Loan> getMemberLoans(Long memberId) {
        return loanDAO.findByMemberId(memberId);
    }

    public List<Loan> getPendingLoans() {
        return loanDAO.findAllPending();
    }

    public List<Loan> getAllLoans() {
        return loanDAO.findAll();
    }
}