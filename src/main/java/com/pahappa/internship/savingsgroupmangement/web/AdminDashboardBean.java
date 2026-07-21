package com.pahappa.internship.savingsgroupmangement.web;

import com.pahappa.internship.savingsgroupmangement.dto.AdminSummaryDTO;
import com.pahappa.internship.savingsgroupmangement.model.Loan;
import com.pahappa.internship.savingsgroupmangement.service.AdminService;
import com.pahappa.internship.savingsgroupmangement.service.LoanService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class AdminDashboardBean implements Serializable {

    @Inject
    private AdminService adminService;

    @Inject
    private LoanService loanService;

    private AdminSummaryDTO summary;
    private List<Loan> pendingLoans;
    private Long selectedLoanId;
    private String rejectionReason;

    @PostConstruct
    public void init() {
        refreshDashboard();
    }

    public void refreshDashboard() {
        this.summary = adminService.getDashboardSummary();
        this.pendingLoans = loanService.getPendingLoans();
    }

    public void approveLoan(Long loanId) {
        try {
            loanService.approveLoan(loanId);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Loan #" + loanId + " approved and disbursed successfully."));
            refreshDashboard();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Approval Failed", e.getMessage()));
        }
    }

    public void rejectLoan() {
        try {
            loanService.rejectLoan(selectedLoanId, rejectionReason);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Loan #" + selectedLoanId + " rejected."));
            this.rejectionReason = "";
            refreshDashboard();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Rejection Failed", e.getMessage()));
        }
    }

    // Getters and Setters
    public AdminSummaryDTO getSummary() { return summary; }
    public List<Loan> getPendingLoans() { return pendingLoans; }
    public Long getSelectedLoanId() { return selectedLoanId; }
    public void setSelectedLoanId(Long selectedLoanId) { this.selectedLoanId = selectedLoanId; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}