/**
 * 
 */
package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.Payment;
import com.paymentgateway.commons.user.ResellerCharges;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Amitosh Aanand
 *
 */
@Component
@SuppressWarnings("unchecked")
public class ResellerChargesDao extends HibernateAbstractDao {
	
	private static Logger logger = LoggerFactory.getLogger(ResellerChargesDao.class.getName());
	
	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	PropertiesManager propertiesManager;

	private static final String prefix = "MONGO_DB_";
	
	public ResellerChargesDao() {
		super();
	}

	public void create(ResellerCharges resellerCharges) throws DataAccessLayerException {
		super.save(resellerCharges);
	}

	public void delete(ResellerCharges resellerCharges) throws DataAccessLayerException {
		super.delete(resellerCharges);
	}

	public void update(ResellerCharges resellerCharges) throws DataAccessLayerException {
		super.saveOrUpdate(resellerCharges);
	}

	@SuppressWarnings("rawtypes")
	public List findAll() throws DataAccessLayerException {
		return super.findAll(ResellerCharges.class);
	}

	public ResellerCharges find(Long id) throws DataAccessLayerException {
		return (ResellerCharges) super.find(ResellerCharges.class, id);
	}

	public ResellerCharges find(String name) throws DataAccessLayerException {
		return (ResellerCharges) super.find(ResellerCharges.class, name);
	}

	@SuppressWarnings("deprecation")
	public ResellerCharges findDetailForUpdate(String merchantPayId, String resellerId, String slabId,
			PaymentType paymentType, MopType mopType, TransactionType transactionType,
			AccountCurrencyRegion paymentsRegion, CardHolderType cardHolderType, String currency) {
		ResellerCharges resellerCharges = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			resellerCharges = (ResellerCharges) session.createQuery(
					"from ResellerCharges RC where RC.merchantPayId = :merchantPayId and RC.resellerId = :resellerId and RC.slabId =:slabId "
							+ "and RC.paymentType = :paymentType and RC.mopType = :mopType and RC.transactionType = :transactionType and "
							+ "RC.paymentsRegion = :paymentsRegion and RC.cardHolderType = :cardHolderType and "
							+ "RC.currency = :currency and RC.status = 'ACTIVE'")
					.setParameter("merchantPayId", merchantPayId).setParameter("resellerId", resellerId)
					.setParameter("slabId", slabId).setParameter("paymentType", paymentType)
					.setParameter("mopType", mopType).setParameter("transactionType", transactionType)
					.setParameter("paymentsRegion", paymentsRegion).setParameter("cardHolderType", cardHolderType)
					.setParameter("currency", currency).setCacheable(true).uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return resellerCharges;
	}

	public void editChargingDetail(String emailId, ResellerCharges resellerCharges, ResellerCharges rcFromDB, boolean b,
			String userType, String loginUserEmailId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		Date updatedDate = new Date();
		try {
			ResellerCharges resellerChargesFromDb = session.load(ResellerCharges.class, rcFromDB.getId());
			resellerChargesFromDb.setStatus("INACTIVE");
			resellerChargesFromDb.setUpdatedDate(updatedDate);
			resellerChargesFromDb.setUpdatedBy(loginUserEmailId);
			create(resellerCharges);
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
	}

	public List<ResellerCharges> fetchChargesByResellerAndMerchant(String merchantPayId, String resellerId) {
		List<ResellerCharges> resellerChargesList = new ArrayList<ResellerCharges>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			if(merchantPayId.equalsIgnoreCase("ALL")) {
				resellerChargesList = session.createQuery("from ResellerCharges where status='ACTIVE'"
						+ " and resellerId ='" + resellerId + "' order by createdDate").getResultList();
			} else {
				resellerChargesList = session.createQuery("from ResellerCharges where status='ACTIVE' and merchantPayId ='"
					+ merchantPayId + "' and resellerId ='" + resellerId + "' order by createdDate").getResultList();
			}
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return resellerChargesList;
	}

	public ResellerCharges fetchMostSuitableResellerCharge(String merchantPayId, String resellerId, String slabId,
			PaymentType paymentType, AccountCurrencyRegion paymentsRegion, CardHolderType cardHolderType,
			String currency) {
		List<ResellerCharges> resellerChargesList = new ArrayList<ResellerCharges>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			resellerChargesList = session
					.createQuery("FROM ResellerCharges WHERE status = 'ACTIVE' AND merchantPayId = '" + merchantPayId
							+ "' AND resellerId = '" + resellerId + "'" + " AND slabId = '" + slabId
							+ "'  AND paymentType = '" + paymentType + "' AND paymentsRegion = '" + paymentsRegion + "'"
							+ " AND cardHolderType = '" + cardHolderType + "' AND currency = '" + currency
							+ "' ORDER BY resellerPercentage DESC")
					.getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		ResellerCharges rc = null;
		if(!resellerChargesList.isEmpty()) {
			rc = resellerChargesList.get(0);
		}
		return rc;
	}
	
	public boolean getChargingDetailsMap(String merchantPayId, String paymentType, String paymentsRegion) {

		Session session = null;
		boolean chargingMapFlag = false;

		try {
			User merchant = userDao.findPayId(merchantPayId);
			Set<Account> accounts = merchant.getAccounts();
			if (accounts == null || accounts.size() == 0) {
				logger.info("No account found for Pay ID = " + merchant.getPayId());
			} else {
				for (Account accountThis : accounts) {
					Set<Payment> payments = accountThis.getPayments();
					for(Payment payment : payments) {
						PaymentType paymentName = PaymentType.getInstanceIgnoreCase(paymentType);
						if(payment.getPaymentType().toString().equals(paymentName.toString())) {
							chargingMapFlag = true;
							break;
						}
					}
				}
			}
			
		} finally {
			HibernateSessionProvider.closeSession(session);
		}
		return chargingMapFlag;
	}
}
