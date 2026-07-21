package com.pahappa.internship.savingsgroupmangement.dao;

import com.pahappa.internship.savingsgroupmangement.config.HibernateUtil;
import com.pahappa.internship.savingsgroupmangement.model.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class TransactionDAO {

    public void saveTransaction(Transaction transaction) {
        org.hibernate.Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(transaction);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public List<Transaction> findTransactionsByUser(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Transaction> query = session.createQuery(
                    "FROM Transaction t WHERE t.user.id = :userId ORDER BY t.createdAt DESC", Transaction.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Calculates the user's total savings balance directly from deposit/withdrawal history.
     */
    public Double calculateTotalBalance(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT SUM(CASE WHEN t.type = com.pahappa.internship.savingsgroupmangement.model.TransactionType.DEPOSIT THEN t.amount ELSE -t.amount END) " +
                    "FROM Transaction t WHERE t.user.id = :userId";

            Double balance = session.createQuery(hql, Double.class)
                    .setParameter("userId", userId)
                    .uniqueResult();

            return balance != null ? balance : 0.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * Helper method to return balance as a BigDecimal for accurate calculations
     * in business logic (e.g., loan 3x multiplier check).
     */
    public BigDecimal getMemberBalance(Long userId) {
        Double balance = calculateTotalBalance(userId);
        return BigDecimal.valueOf(balance);
    }


    /**
     * Alias for findTransactionsByUser to support statement generation.
     */
    public List<Transaction> getTransactionsByMemberId(Long memberId) {
        return findTransactionsByUser(memberId);
    }
}