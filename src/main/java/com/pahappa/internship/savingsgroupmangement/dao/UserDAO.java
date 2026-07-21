package com.pahappa.internship.savingsgroupmangement.dao;

import com.pahappa.internship.savingsgroupmangement.config.HibernateUtil;
import com.pahappa.internship.savingsgroupmangement.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

@ApplicationScoped
public class UserDAO {

    public void saveUser(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    /**
     * Finds a User by primary key ID.
     * Required by LoanService when disbursing approved loan funds.
     */
    public User findById(Long id) {
        if (id == null) return null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public User findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE username = :username", User.class);
            query.setParameter("username", username);
            return query.uniqueResult();
        }
    }

    public User findByNationalId(String nationalId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE nationalId = :nationalId", User.class);
            query.setParameter("nationalId", nationalId);
            return query.uniqueResult();
        }
    }
}