
/*
package com.pahappa.internship.savingsgroupmangement.filter;


import com.pahappa.internship.savingsgroupmangement.model.Role;
import com.pahappa.internship.savingsgroupmangement.web.AuthBean;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(urlPatterns = {"/member/*", "/admin/*"})
public class SecurityFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No special initialization configuration needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        // Try to safely extract our session-scoped AuthBean managed by JSF
        AuthBean authBean = (session != null) ? (AuthBean) session.getAttribute("authBean") : null;

        String contextPath = httpRequest.getContextPath();
        String requestURI = httpRequest.getRequestURI();

        // 1. RULE: Guard against unauthenticated access
        if (authBean == null || authBean.getCurrentUser() == null) {
            httpResponse.sendRedirect(contextPath + "/index.xhtml?error=unauthorized");
            return;
        }

        // 2. RULE: Enforce Authorization boundaries based on Roles
        if (requestURI.contains("/admin/") && authBean.getCurrentUser().getRole() != Role.ADMIN) {
            // A regular member is trying to access admin pages -> block them!
            httpResponse.sendRedirect(contextPath + "/member/dashboard.xhtml?error=forbidden");
            return;
        }

        if (requestURI.contains("/member/") && authBean.getCurrentUser().getRole() == Role.ADMIN) {
            // Rule: Admin account cannot be a savings member; roles are separate.
            // Block an admin from sneaking into member interfaces.
            httpResponse.sendRedirect(contextPath + "/admin/dashboard.xhtml?error=forbidden");
            return;
        }

        // Everything looks safe! Let the request proceed to the page.
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup resources if necessary
    }
}


 */