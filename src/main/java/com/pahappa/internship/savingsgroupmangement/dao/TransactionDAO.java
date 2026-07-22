package com.pahappa.internship.savingsgroupmangement.dao;

import com.pahappa.internship.savingsgroupmangement.config.HibernateUtil;
import com.pahappa.internship.savingsgroupmangement.model.Transaction;
import com.pahappa.internship.savingsgroupmangement.model.User;
import com.pahappa.internship.savingsgroupmangement.model.Role;
import jakarta.enterprise.context.ApplicationScoped;
import org.hibernate.Session;
import org.hibernate.LockMode;
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

    /**
     * Moves funds between two member ledgers as one database transaction.  The debit and
     * credit are both persisted, or neither is persisted if validation or saving fails.
     */
    public User transfer(Long senderId, String recipientNationalId, Double amount) throws Exception {
        org.hibernate.Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            User sender = session.get(User.class, senderId, LockMode.PESSIMISTIC_WRITE);
            if (sender == null || !sender.isActive() || sender.getRole() != Role.MEMBER) {
                throw new Exception("Transfer failed: sender account is unavailable.");
            }

            User recipient = session.createQuery(
                            "FROM User WHERE nationalId = :nationalId", User.class)
                    .setParameter("nationalId", recipientNationalId)
                    .uniqueResult();
            if (recipient == null || !recipient.isActive() || recipient.getRole() != Role.MEMBER) {
                throw new Exception("Transfer failed: no active member was found with that National ID.");
            }
            if (sender.getId().equals(recipient.getId())) {
                throw new Exception("Transfer failed: you cannot send money to your own account.");
            }

            Double balance = session.createQuery(
                            "SELECT SUM(CASE WHEN t.type = com.pahappa.internship.savingsgroupmangement.model.TransactionType.DEPOSIT THEN t.amount ELSE -t.amount END) " +
                                    "FROM Transaction t WHERE t.user.id = :userId", Double.class)
                    .setParameter("userId", sender.getId())
                    .uniqueResult();
            double availableBalance = balance != null ? balance : 0.0;
            double minimumBalance = 20000.0;
            if (amount > availableBalance - minimumBalance) {
                throw new Exception("Transfer failed: insufficient available funds. A minimum balance of UGX 20,000 must remain.");
            }

            Transaction debit = new Transaction();
            debit.setUser(sender);
            debit.setType(com.pahappa.internship.savingsgroupmangement.model.TransactionType.WITHDRAWAL);
            debit.setAmount(amount);
            debit.setDescription("Transfer to " + recipient.getNationalId());

            Transaction credit = new Transaction();
            credit.setUser(recipient);
            credit.setType(com.pahappa.internship.savingsgroupmangement.model.TransactionType.DEPOSIT);
            credit.setAmount(amount);
            credit.setDescription("Transfer from " + sender.getNationalId());

            session.persist(debit);
            session.persist(credit);
            tx.commit();
            return recipient;
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



    /**
     * Retrieves all recorded transactions across all users for system audit logs.
     */
    public List<Transaction> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Transaction t ORDER BY t.createdAt DESC", Transaction.class)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Computes total aggregate amount for a specific TransactionType (DEPOSIT or WITHDRAWAL).
     */
    public BigDecimal getTotalByTransactionType(com.pahappa.internship.savingsgroupmangement.model.TransactionType type) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT SUM(t.amount) FROM Transaction t WHERE t.type = :type";
            Double total = session.createQuery(hql, Double.class)
                    .setParameter("type", type)
                    .uniqueResult();

            return total != null ? BigDecimal.valueOf(total) : BigDecimal.ZERO;
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }
}
