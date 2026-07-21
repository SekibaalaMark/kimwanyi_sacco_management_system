package com.pahappa.internship.savingsgroupmangement.web;

import com.pahappa.internship.savingsgroupmangement.model.Loan;
import com.pahappa.internship.savingsgroupmangement.service.LoanService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Named
@ViewScoped
public class AdminLoanBean implements Serializable {

    @Inject
    private LoanService loanService;

    private List<Loan> pendingLoans = Collections.emptyList();
    private List<Loan> allLoans = Collections.emptyList();
    private String rejectionReason;

    @PostConstruct
    public void init() {
        loadData();
    }

    public void loadData() {
        this.pendingLoans = loanService.getPendingLoans();
        this.allLoans = loanService.getAllLoans();
    }

    public void handleApprove(Long loanId) {
        try {
            loanService.approveLoan(loanId);
            addMessage(FacesMessage.SEVERITY_INFO, "Approved", "Loan ID " + loanId + " has been approved.");
            loadData();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Approval Failed", e.getMessage());
        }
    }

    public void handleReject(Long loanId) {
        try {
            loanService.rejectLoan(loanId, rejectionReason);
            addMessage(FacesMessage.SEVERITY_INFO, "Rejected", "Loan ID " + loanId + " rejected.");
            rejectionReason = null;
            loadData();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Rejection Failed", e.getMessage());
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters and Setters
    public List<Loan> getPendingLoans() { return pendingLoans; }
    public List<Loan> getAllLoans() { return allLoans; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}