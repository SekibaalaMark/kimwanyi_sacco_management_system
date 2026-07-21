package com.pahappa.internship.savingsgroupmangement.dao;




import com.pahappa.internship.savingsgroupmangement.config.HibernateUtil;
import com.pahappa.internship.savingsgroupmangement.model.Transaction;
import org.hibernate.Session;
//import org.hibernate.Transaction as DBTransaction;
import org.hibernate.query.Query;
import java.util.List;

public class TransactionDAO {

    public void saveTransaction(Transaction transaction) {
        org.hibernate.Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(transaction);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public List<Transaction> findTransactionsByUser(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Transaction> query = session.createQuery(
                    "FROM Transaction WHERE user.id = :userId ORDER BY createdAt DESC", Transaction.class);
            query.setParameter("userId", userId);
            return query.list();
        }
    }

    public Double calculateTotalBalance(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // HQL sum query: Add up deposits, subtract withdrawals
            String hql = "SELECT COALESCE(SUM(CASE WHEN t.type = 'DEPOSIT' THEN t.amount ELSE -t.amount END), 0.0) " +
                    "FROM Transaction t WHERE t.user.id = :userId";
            Query<Double> query = session.createQuery(hql, Double.class);
            query.setParameter("userId", userId);
            return query.uniqueResult();
        }
    }
}
