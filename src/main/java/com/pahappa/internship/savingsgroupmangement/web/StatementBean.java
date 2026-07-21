package com.pahappa.internship.savingsgroupmangement.web;

import com.pahappa.internship.savingsgroupmangement.dto.StatementDTO;
import com.pahappa.internship.savingsgroupmangement.service.StatementService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

@Named
@RequestScoped
public class StatementBean implements Serializable {

    @Inject
    private StatementService statementService;

    @Inject
    private AuthBean authBean;

    private StatementDTO currentStatement;

    public StatementDTO getMemberStatement() {
        if (currentStatement == null && authBean.getCurrentUser() != null) {
            currentStatement = statementService.generateStatementForMember(authBean.getCurrentUser().getId());
        }
        return currentStatement;
    }
}