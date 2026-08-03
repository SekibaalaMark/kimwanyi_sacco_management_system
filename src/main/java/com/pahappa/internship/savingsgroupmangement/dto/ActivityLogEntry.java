package com.pahappa.internship.savingsgroupmangement.dto;

import java.time.LocalDateTime;

/**
 * A single row in the admin activity log, unified across transactions and loan events.
 */
public class ActivityLogEntry {

    public enum Category { DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT,
                           LOAN_APPLIED, LOAN_APPROVED, LOAN_REJECTED, LOAN_REPAID,
                           MEMBER_ACTIVATED, MEMBER_DEACTIVATED }

    private final LocalDateTime timestamp;
    private final Category      category;
    private final String        actor;        // username or "System"
    private final String        detail;       // human-readable description
    private final Double        amount;       // null for non-monetary events

    public ActivityLogEntry(LocalDateTime timestamp, Category category,
                            String actor, String detail, Double amount) {
        this.timestamp = timestamp;
        this.category  = category;
        this.actor     = actor;
        this.detail    = detail;
        this.amount    = amount;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public Category      getCategory()  { return category;  }
    public String        getActor()     { return actor;     }
    public String        getDetail()    { return detail;    }
    public Double        getAmount()    { return amount;    }

    /** CSS class for the category badge */
    public String getBadgeStyle() {
        return switch (category) {
            case DEPOSIT, TRANSFER_IN, LOAN_APPROVED, MEMBER_ACTIVATED ->
                    "background:#1f7a4d;";
            case WITHDRAWAL, TRANSFER_OUT, LOAN_REPAID ->
                    "background:#b86912;";
            case LOAN_APPLIED ->
                    "background:#3b6eb5;";
            case LOAN_REJECTED, MEMBER_DEACTIVATED ->
                    "background:#b84040;";
        };
    }

    /** Short label shown in the badge */
    public String getBadgeLabel() {
        return switch (category) {
            case DEPOSIT          -> "Deposit";
            case WITHDRAWAL       -> "Withdrawal";
            case TRANSFER_IN      -> "Transfer In";
            case TRANSFER_OUT     -> "Transfer Out";
            case LOAN_APPLIED     -> "Loan Applied";
            case LOAN_APPROVED    -> "Loan Approved";
            case LOAN_REJECTED    -> "Loan Rejected";
            case LOAN_REPAID      -> "Loan Repaid";
            case MEMBER_ACTIVATED -> "Activated";
            case MEMBER_DEACTIVATED -> "Deactivated";
        };
    }
}
