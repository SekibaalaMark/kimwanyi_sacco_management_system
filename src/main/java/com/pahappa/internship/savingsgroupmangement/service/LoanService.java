package com.pahappa.internship.savingsgroupmangement.service;

import com.pahappa.internship.savingsgroupmangement.dao.LoanDAO;
import com.pahappa.internship.savingsgroupmangement.dao.TransactionDAO;
import com.pahappa.internship.savingsgroupmangement.dao.UserDAO;
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
    public static final BigDecimal MINIMUM_ACCOUNT_BALANCE = new BigDecimal("20000"); // UGX 20,000 minimum balance floor

    @Inject
    private LoanDAO loanDAO;

    @Inject
    private TransactionDAO transactionDAO;

    @Inject
    private UserDAO userDAO;


    public void applyForLoan(Long memberId, BigDecimal requestedAmount) {
        if (requestedAmount == null || requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Requested loan amount must be greater than zero.");
        }


        if (loanDAO.hasActiveOrPendingLoan(memberId)) {
            throw new IllegalStateException("You already have an active or pending loan application. Please settle it first.");
        }


        BigDecimal savingsBalance = transactionDAO.getMemberBalance(memberId);
        BigDecimal maxLoanAllowed = savingsBalance.multiply(new BigDecimal("3")).setScale(2, RoundingMode.HALF_UP);

        if (requestedAmount.compareTo(maxLoanAllowed) > 0) {
            throw new IllegalStateException(
                    String.format("Loan denied. Maximum allowed loan is 3x your savings balance (UGX %,.2f). Max eligible: UGX %,.2f.",
                            savingsBalance, maxLoanAllowed)
            );
        }


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

    public void approveLoan(Long loanId) {
        Loan loan = loanDAO.findById(loanId);
        if (loan == null) throw new IllegalArgumentException("Loan record not found.");
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException("Only PENDING loans can be approved.");
        }


        User borrower = userDAO.findById(loan.getMemberId());
        if (borrower == null) {
            throw new IllegalStateException("Borrower user record not found.");
        }


        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovedAt(LocalDateTime.now());
        loanDAO.update(loan);


        Transaction loanDisbursement = new Transaction();
        loanDisbursement.setUser(borrower);
        loanDisbursement.setType(TransactionType.DEPOSIT);
        loanDisbursement.setAmount(loan.getPrincipalAmount().doubleValue());

        transactionDAO.saveTransaction(loanDisbursement);
    }


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


    public void repayLoan(Long memberId, Long loanId, BigDecimal paymentAmount) {
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Repayment amount must be greater than zero.");
        }

        Loan loan = loanDAO.findById(loanId);
        if (loan == null) throw new IllegalArgumentException("Loan record not found.");
        if (!loan.getMemberId().equals(memberId)) {
            throw new IllegalStateException("You can only repay your own loan.");
        }
        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException("Only active APPROVED loans can be repaid.");
        }


        BigDecimal remainingLoanBalance = loan.getRemainingBalance();
        if (paymentAmount.compareTo(remainingLoanBalance) > 0) {
            throw new IllegalArgumentException(
                    String.format("Payment exceeds outstanding loan balance. Current balance is UGX %,.2f.", remainingLoanBalance)
            );
        }


        BigDecimal currentAccountBalance = transactionDAO.getMemberBalance(loan.getMemberId());
        BigDecimal projectedAccountBalance = currentAccountBalance.subtract(paymentAmount);

        if (projectedAccountBalance.compareTo(MINIMUM_ACCOUNT_BALANCE) < 0) {
            BigDecimal maxPermissibleRepayment = currentAccountBalance.subtract(MINIMUM_ACCOUNT_BALANCE);

            if (maxPermissibleRepayment.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException(
                        String.format("Repayment failed. Your account balance (UGX %,.2f) is already at or below the minimum required balance of UGX %,.2f.",
                                currentAccountBalance, MINIMUM_ACCOUNT_BALANCE)
                );
            } else {
                throw new IllegalStateException(
                        String.format("Repayment failed. Your account balance cannot fall below UGX %,.2f. The maximum you can pay right now is UGX %,.2f.",
                                MINIMUM_ACCOUNT_BALANCE, maxPermissibleRepayment)
                );
            }
        }


        User borrower = userDAO.findById(loan.getMemberId());
        if (borrower == null) {
            throw new IllegalStateException("Borrower user record not found.");
        }


        Transaction repaymentTransaction = new Transaction();
        repaymentTransaction.setUser(borrower);
        repaymentTransaction.setType(TransactionType.WITHDRAWAL);
        repaymentTransaction.setAmount(paymentAmount.doubleValue());
        repaymentTransaction.setDescription("Loan repayment for loan #" + loan.getId());

        transactionDAO.saveTransaction(repaymentTransaction);

        BigDecimal newAmountPaid = loan.getAmountPaid().add(paymentAmount);
        loan.setAmountPaid(newAmountPaid);


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
