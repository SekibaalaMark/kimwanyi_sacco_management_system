package com.pahappa.internship.savingsgroupmangement.web;

import com.pahappa.internship.savingsgroupmangement.model.Loan;
import com.pahappa.internship.savingsgroupmangement.model.User;
import com.pahappa.internship.savingsgroupmangement.service.LoanService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Named
@ViewScoped
public class LoanBean implements Serializable {

    @Inject
    private LoanService loanService;

    @Inject
    private AuthBean authBean;

    private BigDecimal applyAmount;
    private BigDecimal repayAmount;
    private Long selectedLoanId;
    private List<Loan> memberLoans = Collections.emptyList();

    @PostConstruct
    public void init() {
        loadData();
    }

    public void loadData() {
        User currentUser = authBean.getCurrentUser();
        if (currentUser != null) {
            this.memberLoans = loanService.getMemberLoans(currentUser.getId());
        }
    }

    public void handleApply() {
        User currentUser = authBean.getCurrentUser();
        if (currentUser == null) return;

        try {
            loanService.applyForLoan(currentUser.getId(), applyAmount);
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Loan application submitted for admin review.");
            applyAmount = null;
            loadData();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Application Failed", e.getMessage());
        }
    }

    public void handleRepay() {
        try {
            if (selectedLoanId == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No loan selected for repayment.");
                return;
            }
            loanService.repayLoan(selectedLoanId, repayAmount);
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Loan repayment recorded successfully.");
            repayAmount = null;
            loadData();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Repayment Failed", e.getMessage());
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters and Setters
    public BigDecimal getApplyAmount() { return applyAmount; }
    public void setApplyAmount(BigDecimal applyAmount) { this.applyAmount = applyAmount; }

    public BigDecimal getRepayAmount() { return repayAmount; }
    public void setRepayAmount(BigDecimal repayAmount) { this.repayAmount = repayAmount; }

    public Long getSelectedLoanId() { return selectedLoanId; }
    public void setSelectedLoanId(Long selectedLoanId) { this.selectedLoanId = selectedLoanId; }

    public List<Loan> getMemberLoans() { return memberLoans; }
}