package com.paymentgateway.commons.user;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.util.PaymentType;

@Component
public class SurchargeDetailsDao extends HibernateAbstractDao {

	private static Logger logger = LoggerFactory.getLogger(SurchargeDetailsDao.class.getName());

	public void create(SurchargeDetails surchargeDetails) throws DataAccessLayerException {
		super.save(surchargeDetails);
	}

	public SurchargeDetails findDetails(String payId, String paymentTypeCode) {

		String paymentType = PaymentType.getpaymentName(paymentTypeCode);
		SurchargeDetails responseUser = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			responseUser = (SurchargeDetails) session.createQuery(
					"from SurchargeDetails SD where SD.payId = :payId and SD.paymentType = :paymentType and SD.status = 'ACTIVE'")
					.setParameter("payId", payId).setParameter("paymentType", paymentTypeCode).setCacheable(true)
					.uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
			// return responseUser;
		}
		return responseUser;

	}

	
	public SurchargeDetails findDetailsByRegion(String payId, String paymentTypeCode, String paymentsRegion) {

		String paymentType = PaymentType.getpaymentName(paymentTypeCode);
		
		AccountCurrencyRegion acr ;
		
		if (paymentsRegion.equals(AccountCurrencyRegion.DOMESTIC.toString())) {
			acr = AccountCurrencyRegion.DOMESTIC;
		}
		else {
			acr = AccountCurrencyRegion.INTERNATIONAL;
		}
		
		SurchargeDetails responseUser = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			responseUser = (SurchargeDetails) session.createQuery(
					"from SurchargeDetails SD where SD.payId = :payId and SD.paymentType = :paymentType and SD.paymentsRegion = :acr and SD.status = 'ACTIVE'")
					.setParameter("payId", payId).setParameter("paymentType", paymentTypeCode)
					.setParameter("acr", acr).setCacheable(true)
					.uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
			// return responseUser;
		}
		return responseUser;

	}

	@SuppressWarnings("unchecked")
	public List<SurchargeDetails> findSurchargeDetails(String payId, String paymentTypeCode, String date) {
		List<SurchargeDetails> surchargeDetailsList = new ArrayList<SurchargeDetails>();
		surchargeDetailsList = null;
		String paymentType = PaymentType.getpaymentName(paymentTypeCode);
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			surchargeDetailsList = session.createNativeQuery(
					"select * from SurchargeDetails SD  where  (case when SD.updatedDate=SD.createdDate then :date between SD.createdDate and current_timestamp() else  SD.createdDate <= :date and SD.updatedDate > :date end) and SD.payId = :payId and SD.paymentType = :paymentType")
					.setParameter("payId", payId).setParameter("paymentType", paymentType).setParameter("date", date)
					.getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
			// return responseUser;
		}
		return surchargeDetailsList;

	}
	
	@SuppressWarnings("unchecked")
	public List<SurchargeDetails> findSurchargeDetailsAll(String payId, String paymentTypeCode) {
		List<SurchargeDetails> surchargeDetailsList = new ArrayList<SurchargeDetails>();
		surchargeDetailsList = null;
		String paymentType = PaymentType.getpaymentName(paymentTypeCode);
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			surchargeDetailsList = session.createNativeQuery(
					"select * from SurchargeDetails SD  where   SD.payId = :payId and SD.paymentType = :paymentType")
					.setParameter("payId", payId).setParameter("paymentType", paymentTypeCode)
					.getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
			// return responseUser;
		}
		return surchargeDetailsList;

	}

	public SurchargeDetails findPendingDetails(String payId, String paymentType) {
		SurchargeDetails responseUser = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			responseUser = (SurchargeDetails) session.createQuery(
					"from SurchargeDetails SD where SD.payId = :payId and SD.paymentType = :paymentType and SD.status = 'PENDING'")
					.setParameter("payId", payId).setParameter("paymentType", paymentType).setCacheable(true)
					.uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
			// return responseUser;
		}
		return responseUser;

	}

	public SurchargeDetails findPendingDetailsByRegion(String payId, String paymentType, String paymentsRegion) {
		SurchargeDetails responseUser = null;
		AccountCurrencyRegion acr ;
		
		if (paymentsRegion.equals(AccountCurrencyRegion.DOMESTIC.toString())) {
			acr = AccountCurrencyRegion.DOMESTIC;
		}
		else {
			acr = AccountCurrencyRegion.INTERNATIONAL;
		}
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			responseUser = (SurchargeDetails) session.createQuery(
					"from SurchargeDetails SD where SD.payId = :payId and SD.paymentType = :paymentType and SD.paymentsRegion = :acr and SD.status = 'PENDING'")
					.setParameter("payId", payId).setParameter("paymentType", paymentType)
					.setParameter("acr", acr).setCacheable(true).uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
			// return responseUser;
		}
		return responseUser;

	}


	@SuppressWarnings("unchecked")
	public List<SurchargeDetails> findPendingDetails() {
		List<SurchargeDetails> pendingSurchargeDetailsList = new ArrayList<SurchargeDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			pendingSurchargeDetailsList = session.createQuery("from SurchargeDetails SD where SD.status='PENDING'")
					.setCacheable(true).getResultList();
			tx.commit();
			return pendingSurchargeDetailsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return pendingSurchargeDetailsList;
	}

	@SuppressWarnings("unchecked")
	public List<SurchargeDetails> getActiveSurchargeDetailsByPayId(String payId) {
		List<SurchargeDetails> activeSurchargeDetailsList = new ArrayList<SurchargeDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			activeSurchargeDetailsList = session
					.createQuery("from SurchargeDetails SD where SD.payId = :payId and SD.status='ACTIVE'")
					.setParameter("payId", payId).getResultList();
			tx.commit();
			return activeSurchargeDetailsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} catch (Exception exception) {
			logger.error("Exception occured while getting surcharge Details for pay id = " + payId , exception);
		} finally {
			autoClose(session);
		}
		return activeSurchargeDetailsList;
	}

	public boolean isSurchargeDetailsSet(String payId) {
		BigInteger countAmount = new BigInteger("0");
		BigInteger countPercentage = new BigInteger("0");
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			String sqlQueryAmount = "select count(*) from SurchargeDetails where payId=:payId and status='ACTIVE' and surchargeAmount > 0 ";
			String sqlQueryPercentage = "select count(*) from SurchargeDetails where payId=:payId and status='ACTIVE' and surchargePercentage > 0 ";

			countAmount = (BigInteger) session.createNativeQuery(sqlQueryAmount).setParameter("payId", payId)
					.getSingleResult();

			countPercentage = (BigInteger) session.createNativeQuery(sqlQueryPercentage)
					.setParameter("payId", payId).getSingleResult();
			tx.commit();

			if (countAmount.intValue() > 0 || countPercentage.intValue() > 0) {
				return true;
			} else {
				return false;
			}
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return false;
	}

}
