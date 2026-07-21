package com.pahappa.internship.savingsgroupmangement.dto;

import java.math.BigDecimal;

public class AdminSummaryDTO {
    private long totalMembers;
    private long activeLoansCount;
    private long pendingLoansCount;
    private BigDecimal totalSystemDeposits;
    private BigDecimal totalSystemWithdrawals;
    private BigDecimal totalDisbursedLoans;
    private BigDecimal netGroupLiquidity;

    public AdminSummaryDTO(long totalMembers, long activeLoansCount, long pendingLoansCount,
                           BigDecimal totalSystemDeposits, BigDecimal totalSystemWithdrawals,
                           BigDecimal totalDisbursedLoans, BigDecimal netGroupLiquidity) {
        this.totalMembers = totalMembers;
        this.activeLoansCount = activeLoansCount;
        this.pendingLoansCount = pendingLoansCount;
        this.totalSystemDeposits = totalSystemDeposits;
        this.totalSystemWithdrawals = totalSystemWithdrawals;
        this.totalDisbursedLoans = totalDisbursedLoans;
        this.netGroupLiquidity = netGroupLiquidity;
    }

    // Getters
    public long getTotalMembers() { return totalMembers; }
    public long getActiveLoansCount() { return activeLoansCount; }
    public long getPendingLoansCount() { return pendingLoansCount; }
    public BigDecimal getTotalSystemDeposits() { return totalSystemDeposits; }
    public BigDecimal getTotalSystemWithdrawals() { return totalSystemWithdrawals; }
    public BigDecimal getTotalDisbursedLoans() { return totalDisbursedLoans; }
    public BigDecimal getNetGroupLiquidity() { return netGroupLiquidity; }
}