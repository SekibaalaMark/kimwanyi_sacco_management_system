package com.pahappa.internship.savingsgroupmangement.dao;

import com.pahappa.internship.savingsgroupmangement.config.HibernateUtil;
import com.pahappa.internship.savingsgroupmangement.model.Role;
import com.pahappa.internship.savingsgroupmangement.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class UserDAO {

    public void saveUser(User user) throws Exception {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new Exception("Failed to save user: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }


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


    public long countAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(u) FROM User u WHERE u.role = :role";
            Long count = session.createQuery(hql, Long.class)
                    .setParameter("role", Role.MEMBER)
                    .uniqueResult();
            return count != null ? count : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }


    public List<User> findAllMembers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM User u WHERE u.role = :role ORDER BY u.createdAt DESC";
            return session.createQuery(hql, User.class)
                    .setParameter("role", Role.MEMBER)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    public void updateStatus(Long userId, boolean active) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, userId);
            if (user != null) {
                user.setActive(active);
                session.merge(user);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }
}