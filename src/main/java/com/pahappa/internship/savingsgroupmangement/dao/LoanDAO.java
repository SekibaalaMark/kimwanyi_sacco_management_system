package com.pahappa.internship.savingsgroupmangement.dao;

import com.pahappa.internship.savingsgroupmangement.config.HibernateUtil;
import com.pahappa.internship.savingsgroupmangement.model.Loan;
import com.pahappa.internship.savingsgroupmangement.model.LoanStatus;
import jakarta.enterprise.context.ApplicationScoped;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class LoanDAO {

    public void save(Loan loan) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(loan);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to save loan: " + e.getMessage(), e);
        }
    }

    public void update(Loan loan) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(loan);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to update loan: " + e.getMessage(), e);
        }
    }

    public Loan findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Loan.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean hasActiveOrPendingLoan(Long memberId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                            "SELECT COUNT(l) FROM Loan l WHERE l.memberId = :memberId AND l.status IN (:s1, :s2)", Long.class)
                    .setParameter("memberId", memberId)
                    .setParameter("s1", LoanStatus.PENDING)
                    .setParameter("s2", LoanStatus.APPROVED)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

    public List<Loan> findByMemberId(Long memberId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Loan l WHERE l.memberId = :memberId ORDER BY l.appliedAt DESC", Loan.class)
                    .setParameter("memberId", memberId)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Loan> findAllPending() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Loan l WHERE l.status = :status ORDER BY l.appliedAt ASC", Loan.class)
                    .setParameter("status", LoanStatus.PENDING)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Loan> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Loan l ORDER BY l.appliedAt DESC", Loan.class)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}