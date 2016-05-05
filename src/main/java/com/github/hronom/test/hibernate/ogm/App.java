package com.github.hronom.test.hibernate.ogm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform;
import org.hibernate.jpa.HibernateEntityManagerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.TransactionManager;

public class App {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws Exception {
        // Build the EntityManagerFactory as you would build in in Hibernate ORM.
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ogm-jpa-tutorial");

        // Accessing JBoss's Transaction can be done differently but this one works nicely.
        TransactionManager tm = extractJBossTransactionManager(emf);

        // Persist entities the way you are used to in plain JPA.
        tm.begin();
        logger.info("About to store dog and breed");
        EntityManager em = emf.createEntityManager();
        Breed collie = new Breed();
        collie.setName("Collie");
        em.persist(collie);
        Dog dina = new Dog();
        dina.setName("Dina");
        dina.setBreed(collie);
        em.persist(dina);
        Long dinaId = dina.getId();
        em.flush();
        em.close();
        tm.commit();

        // Retrieve your entities the way you are used to in plain JPA.
        tm.begin();
        logger.info("About to retrieve dog and breed");
        em = emf.createEntityManager();
        dina = em.find(Dog.class, dinaId);
        logger.info(String.format("Found dog %s of breed %s", dina.getName(), dina.getBreed().getName()));
        em.flush();
        em.close();
        tm.commit();

        emf.close();
    }

    private static TransactionManager extractJBossTransactionManager(EntityManagerFactory factory) {
        SessionFactoryImplementor sessionFactory =
            (SessionFactoryImplementor) ((HibernateEntityManagerFactory) factory)
                .getSessionFactory();
        return sessionFactory
            .getServiceRegistry()
            .getService(JtaPlatform.class)
            .retrieveTransactionManager();
    }
}
