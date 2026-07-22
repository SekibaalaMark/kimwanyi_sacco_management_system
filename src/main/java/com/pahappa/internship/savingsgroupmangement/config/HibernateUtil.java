package com.pahappa.internship.savingsgroupmangement.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration().configure();
            configuration.setProperty("hibernate.connection.url", requiredEnvironmentVariable("DB_URL"));
            configuration.setProperty("hibernate.connection.username", requiredEnvironmentVariable("DB_USERNAME"));
            configuration.setProperty("hibernate.connection.password", requiredEnvironmentVariable("DB_PASSWORD"));
            return configuration.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Database Initial SessionFactory configuration failed: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Required environment variable " + name + " is not set.");
        }
        return value;
    }


    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}
