/*
 * package com.paymentgateway.commons.dao;
 * 
 * import java.util.ArrayList; import java.util.List;
 * 
 * import org.hibernate.HibernateException; import
 * org.hibernate.ObjectNotFoundException; import org.hibernate.Session; import
 * org.hibernate.Transaction; import org.slf4j.Logger; import
 * org.slf4j.LoggerFactory; import org.springframework.stereotype.Component;
 * 
 * import com.paymentgateway.commons.user.ChargingDetails; import
 * com.paymentgateway.commons.user.PaymentCombinationDetails; import
 * com.paymentgateway.commons.util.MopType; import
 * com.paymentgateway.commons.util.PaymentType; import
 * com.paymentgateway.commons.util.TransactionType;
 * 
 *//**
	 * @author Amitosh
	 *
	 *//*
		 * @Component public class PaymentCombinationDetailsDao extends
		 * HibernateAbstractDao {
		 * 
		 * private static Logger logger =
		 * LoggerFactory.getLogger(PaymentCombinationDetailsDao.class.getName());
		 * 
		 * PaymentCombinationDetailsDao() { super(); }
		 * 
		 * public void insertPaymentCombination(PaymentCombinationDetails
		 * paymentCombinationDetails) {
		 * logger.info("Inserting new payment Combination"); Session s =
		 * HibernateSessionProvider.getSession(); Transaction t = s.beginTransaction();
		 * try { s.save(paymentCombinationDetails); t.commit(); } catch
		 * (ObjectNotFoundException objectNotFound) { handleException(objectNotFound,
		 * t); } catch (HibernateException hibernateException) {
		 * handleException(hibernateException, t); } finally { autoClose(s); } }
		 * 
		 * @SuppressWarnings("unchecked") public List<PaymentCombinationDetails>
		 * getCombinationData(String acquirerCode, String payId, String paymentType) {
		 * logger.info("Fetching payment combinations");
		 * 
		 * Session s = HibernateSessionProvider.getSession(); Transaction t =
		 * s.beginTransaction(); List<PaymentCombinationDetails>
		 * paymentCombinationDetailsList = new ArrayList<PaymentCombinationDetails>();
		 * try { paymentCombinationDetailsList =
		 * s.createQuery("from PaymentCombinationDetails where payId='" + payId +
		 * "' and acquirerName='" + acquirerCode + "' and paymentType='" +paymentType+
		 * "' and status='ACTIVE'") .getResultList(); t.commit(); } catch
		 * (ObjectNotFoundException objectNotFound) { handleException(objectNotFound,
		 * t); } catch (HibernateException hibernateException) {
		 * handleException(hibernateException, t); } finally { autoClose(s); } return
		 * paymentCombinationDetailsList; } }
		 */