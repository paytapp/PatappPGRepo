package com.paymentgateway.phonePe;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.File;

@Configuration
@EnableTransactionManagement
public class HibernateConfig {

    private static final Logger logger = LoggerFactory.getLogger(HibernateConfig.class);

    @Bean
    public SessionFactory sessionFactory() {
        final File hibernateFile = new File(System.getenv("PG_PROPS") + Constants.HIBERNATE_FILE_NAME.getValue());
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure(hibernateFile)
                .build();

        SessionFactory sessionFactory;
        try {
            sessionFactory = new MetadataSources(registry)
                    .addAnnotatedClass(Transaction.class)
                    .addAnnotatedClass(PaymentResponse.class)
                    .addAnnotatedClass(StatusCheckResponse.class)
                    .addAnnotatedClass(TransactionDetailsEntity.class)
                    .buildMetadata()
                    .buildSessionFactory();
        } catch (Exception exception) {
            logger.error("Error creating Hibernate session: ", exception);
            StandardServiceRegistryBuilder.destroy(registry);
            throw exception;
        }

        return sessionFactory;
    }
}

