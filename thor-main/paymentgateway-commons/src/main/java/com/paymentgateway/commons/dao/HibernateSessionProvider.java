package com.paymentgateway.commons.dao;


import java.io.File;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.BeneficiaryAccounts;
import com.paymentgateway.commons.user.Chargeback;
import com.paymentgateway.commons.user.ChargebackComment;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.CheckerMaker;
import com.paymentgateway.commons.user.DynamicPaymentPage;
import com.paymentgateway.commons.user.ForgetPin;
import com.paymentgateway.commons.user.FraudPrevention;
import com.paymentgateway.commons.user.GstRSaleReport;
import com.paymentgateway.commons.user.IssuerDetails;
import com.paymentgateway.commons.user.LoginHistory;
import com.paymentgateway.commons.user.LoginOtp;
import com.paymentgateway.commons.user.MPAMerchant;
import com.paymentgateway.commons.user.MerchantAcquirerProperties;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.user.MerchantSurchargeText;
import com.paymentgateway.commons.user.Mop;
import com.paymentgateway.commons.user.MopTransaction;
import com.paymentgateway.commons.user.MprUploadDetails;
import com.paymentgateway.commons.user.NodalAmount;
import com.paymentgateway.commons.user.NotificationEmailer;
import com.paymentgateway.commons.user.Payment;
import com.paymentgateway.commons.user.PaymentOptions;
import com.paymentgateway.commons.user.PendingBulkCharges;
import com.paymentgateway.commons.user.PendingBulkUserRequest;
import com.paymentgateway.commons.user.PendingMappingRequest;
import com.paymentgateway.commons.user.PendingResellerMappingApproval;
import com.paymentgateway.commons.user.PendingUserApproval;
import com.paymentgateway.commons.user.Permissions;
import com.paymentgateway.commons.user.RatesDefault;
import com.paymentgateway.commons.user.RefundLimitObject;
import com.paymentgateway.commons.user.RefundValidationDetails;
import com.paymentgateway.commons.user.ResellerCharges;
import com.paymentgateway.commons.user.Roles;
import com.paymentgateway.commons.user.SUFDetail;
import com.paymentgateway.commons.user.SchedulerJobs;
import com.paymentgateway.commons.user.SettlementDataUpload;
import com.paymentgateway.commons.user.Surcharge;
import com.paymentgateway.commons.user.SurchargeDetails;
import com.paymentgateway.commons.user.Token;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserAudit;
import com.paymentgateway.commons.user.UserRecords;
import com.paymentgateway.commons.util.BinRange;
import com.paymentgateway.commons.util.Constants;

/**
 * @author Puneet
 *
 */
public class HibernateSessionProvider {
	private static Logger logger = LoggerFactory.getLogger(HibernateSessionProvider.class.getName());
	private SessionFactory factory;

	private static final String hbmddlAutoSettingName = "hibernate.hbm2ddl.auto";
	private static final String hbmddlAutoSetting = "update";

	private static class SessionHelper {
		private static final HibernateSessionProvider provider = new HibernateSessionProvider();
	}

	private HibernateSessionProvider() {

		// configures settings from hibernate.cfg.xml
	final File hibernateFile = new File(System.getenv("PG_PROPS")+Constants.HIBERNATE_FILE_NAME.getValue());
	final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
																.configure(hibernateFile).applySetting(hbmddlAutoSettingName, hbmddlAutoSetting)
																.build();

	try {
			factory = new MetadataSources(registry).addAnnotatedClass(User.class)
												   .addAnnotatedClass(UserRecords.class)
												   .addAnnotatedClass(Roles.class)
												   .addAnnotatedClass(Permissions.class)
												   .addAnnotatedClass(LoginHistory.class)
												   .addAnnotatedClass(Account.class)
												   .addAnnotatedClass(Payment.class)
												   .addAnnotatedClass(Mop.class)
												   .addAnnotatedClass(MopTransaction.class)
												   .addAnnotatedClass(ChargingDetails.class)
											//	   .addAnnotatedClass(Invoice.class)
												   .addAnnotatedClass(DynamicPaymentPage.class)
												   .addAnnotatedClass(Token.class)
												   .addAnnotatedClass(AccountCurrency.class)
												   .addAnnotatedClass(FraudPrevention.class)
												   .addAnnotatedClass(Chargeback.class)
												   .addAnnotatedClass(ChargebackComment.class)
												   .addAnnotatedClass(SurchargeDetails.class)
												   .addAnnotatedClass(Surcharge.class)
											//	   .addAnnotatedClass(ServiceTax.class)
												   .addAnnotatedClass(PendingUserApproval.class)
											//	   .addAnnotatedClass(NotificationDetail.class)
												   .addAnnotatedClass(BinRange.class)
												   .addAnnotatedClass(PendingResellerMappingApproval.class)
												   .addAnnotatedClass(NotificationEmailer.class)
												   .addAnnotatedClass(PendingMappingRequest.class)
											//	   .addAnnotatedClass(RouterRule.class)
												   .addAnnotatedClass(GstRSaleReport.class)
											//	   .addAnnotatedClass(RouterConfiguration.class)
												   .addAnnotatedClass(MerchantAcquirerProperties.class)
												   .addAnnotatedClass(BeneficiaryAccounts.class)
												   .addAnnotatedClass(NodalAmount.class)
												   .addAnnotatedClass(RefundValidationDetails.class)
												   .addAnnotatedClass(SettlementDataUpload.class)
												   .addAnnotatedClass(MprUploadDetails.class)
												   .addAnnotatedClass(LoginOtp.class)
												   .addAnnotatedClass(ForgetPin.class)
												  // .addAnnotatedClass(PaymentCombinationDetails.class)	
												   .addAnnotatedClass(PaymentOptions.class)
												   .addAnnotatedClass(MerchantProcessingApplication.class)
												   .addAnnotatedClass(SUFDetail.class)
												   .addAnnotatedClass(IssuerDetails.class)
												   .addAnnotatedClass(RatesDefault.class)
												   .addAnnotatedClass(MerchantSurchargeText.class)
												   .addAnnotatedClass(SchedulerJobs.class)
												   .addAnnotatedClass(CheckerMaker.class)
												   .addAnnotatedClass(MPAMerchant.class)
												   .addAnnotatedClass(PendingBulkUserRequest.class)
												   .addAnnotatedClass(UserAudit.class)
												   .addAnnotatedClass(ResellerCharges.class)
												   .addAnnotatedClass(RefundLimitObject.class)
												   .addAnnotatedClass(PendingBulkCharges.class)
												   .buildMetadata().buildSessionFactory();

		} catch (Exception exception) {
			logger.error("Error creating hibernate session : " , exception);
			StandardServiceRegistryBuilder.destroy(registry);
			throw exception;
		}
	}

	private SessionFactory getFactory() {
		return factory;
	}

	public static SessionFactory getSessionFactory() {
		return SessionHelper.provider.getFactory();
	}

	public static Session getSession() {
		return getSessionFactory().openSession();
	}

	public static void closeSession(Session session) {
		if(null != session && session.isOpen()) {
			session.close();
			session = null;
		}
	}
}
