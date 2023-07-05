//package com.paymentgateway.phonePe;
//
//
//import org.springframework.context.annotation.Configuration;
//
//
//
//import org.hibernate.SessionFactory;
//
//import java.io.File;
//
//import org.hibernate.Session;
//import org.hibernate.boot.MetadataSources;
//import org.hibernate.boot.registry.StandardServiceRegistry;
//import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//@Configuration
//public class HibernateConfig {
////    
////	private static Logger logger = LoggerFactory.getLogger(AppConfig.class.getName());
////	private SessionFactory factory;
////
////	private static final String hbmddlAutoSettingName = "hibernate.hbm2ddl.auto";
////	private static final String hbmddlAutoSetting = "update";
////
////	private static class SessionHelper {
////		private static final AppConfig provider = new AppConfig();
////	}
////
////	private AppConfig() {
////
////		// Configures settings from hibernate.cfg.xml
////		final File hibernateFile = new File(System.getenv("PG_PROPS") + Constants.HIBERNATE_FILE_NAME.getValue());
////		final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
////				.configure(hibernateFile)
////				.applySetting(hbmddlAutoSettingName, hbmddlAutoSetting)
////				 // Configure your Hibernate dialect
////				.applySetting("hibernate.dialect", "org.hibernate.dialect.MySQLDialect")
////				.build();
////
////		try {
////			factory = new MetadataSources(registry)
////					.addAnnotatedClass(Transaction.class)
////					.addAnnotatedClass(PaymentResponse.class)
////					.addAnnotatedClass(StatusCheckResponse.class)
////					.buildMetadata()
////					.buildSessionFactory();
////
////		} catch (Exception exception) {
////			logger.error("Error creating hibernate session: ", exception);
////			StandardServiceRegistryBuilder.destroy(registry);
////			throw exception;
////		}
////	}
////
////	private SessionFactory getFactory() {
////		return factory;
////	}
////
////	public static SessionFactory getSessionFactory() {
////		return SessionHelper.provider.getFactory();
////	}
////
////	public static Session getSession() {
////		return getSessionFactory().openSession();
////	}
////
////	public static void closeSession(Session session) {
////		if (null != session && session.isOpen()) {
////			session.close();
////			session = null;
////		}
////	}
//	
//	
//
//	private static Logger logger = LoggerFactory.getLogger(HibernateConfig.class.getName());
//	private SessionFactory factory;
//
//	private static final String hbmddlAutoSettingName = "hibernate.hbm2ddl.auto";
//	private static final String hbmddlAutoSetting = "update";
//
////	private static class SessionHelper {
////		private static final HibernateConfig provider = new HibernateConfig();
////	}
//
//	public HibernateConfig() {
//
//		// configures settings from hibernate.cfg.xml
//	final File hibernateFile = new File(System.getenv("PG_PROPS")+Constants.HIBERNATE_FILE_NAME.getValue());
//	final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
//																.configure(hibernateFile).applySetting(hbmddlAutoSettingName, hbmddlAutoSetting)
//																.build();
//
//	try {
//			factory = new MetadataSources(registry).addAnnotatedClass(Transaction.class)
//													.addAnnotatedClass(PaymentResponse.class)
//													.addAnnotatedClass(StatusCheckResponse.class)
//													.buildMetadata().buildSessionFactory();
//
//		} catch (Exception exception) {
//			logger.error("Error creating hibernate session : " , exception);
//			StandardServiceRegistryBuilder.destroy(registry);
//			throw exception;
//		}
//	}
//
//	private SessionFactory getFactory() {
//		return factory;
//	}
//
////	public static SessionFactory getSessionFactory() {
////		return SessionHelper.provider.getFactory();
////	}
////
////	public static Session getSession() {
////		return getSessionFactory().openSession();
////	}
//
//	public static void closeSession(Session session) {
//		if(null != session && session.isOpen()) {
//			session.close();
//			session = null;
//		}
//	}
//
//	
//}
//




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

