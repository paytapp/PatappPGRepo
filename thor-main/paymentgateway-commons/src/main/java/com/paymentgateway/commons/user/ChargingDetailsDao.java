package com.paymentgateway.commons.user;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.onUsOffUs;

@Component
public class ChargingDetailsDao extends HibernateAbstractDao {

	public ChargingDetailsDao() {
		super();
	}

	public void create(ChargingDetails chargingDetails) throws DataAccessLayerException {
		super.save(chargingDetails);
	}

	public void delete(ChargingDetails chargingDetails) throws DataAccessLayerException {
		super.delete(chargingDetails);
	}

	public void update(ChargingDetails chargingDetails) throws DataAccessLayerException {
		super.saveOrUpdate(chargingDetails);
	}

	@SuppressWarnings("rawtypes")
	public List findAll() throws DataAccessLayerException {
		return super.findAll(ChargingDetails.class);
	}

	public ChargingDetails find(Long id) throws DataAccessLayerException {
		return (ChargingDetails) super.find(ChargingDetails.class, id);
	}

	public List<ChargingDetails> findDetail(String date, String payId, String acquirerName, String paymentType,
			String mopType, String currency) {
		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			String sqlQuery = "select * from ChargingDetails where (case when updatedDate is null then :date between createdDate and current_timestamp() else :date between createdDate and updatedDate end) and payId=:payId and acquirerName=:acquirerName and paymentType=:paymentType and mopType=:mopType and currency=:currency";
			chargingDetailsList = session.createNativeQuery(sqlQuery, ChargingDetails.class).setParameter("date", date)
					.setParameter("payId", payId).setParameter("acquirerName", acquirerName)
					.setParameter("mopType", mopType).setParameter("paymentType", paymentType)
					.setParameter("currency", currency).getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return chargingDetailsList;
	}

	public List<ChargingDetails> findDetail(String date, String payId, String acquirerName, String paymentType,
			String mopType, String currency, String transactionType) {
		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			String sqlQuery = "select * from ChargingDetails where (case when updatedDate is null then :date between createdDate and current_timestamp() else"
					+ " :date between createdDate and updatedDate end) and payId=:payId and acquirerName=:acquirerName and paymentType=:paymentType and mopType=:mopType "
					+ " and currency=:currency and transactionType=:transactionType ";
			chargingDetailsList = session.createNativeQuery(sqlQuery, ChargingDetails.class)
					.setParameter("payId", payId).setParameter("acquirerName", acquirerName)
					.setParameter("mopType", mopType).setParameter("paymentType", paymentType)
					.setParameter("currency", currency).setParameter("transactionType", transactionType)
					.setParameter("date", date).getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return chargingDetailsList;

	}

	public boolean isChargingDetailsSet(String payId) {
		BigInteger count = new BigInteger("0");
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			String sqlQuery = "select count(*) from ChargingDetails where payId=:payId and status='ACTIVE' and merchantTDR >= 0";
			count = (BigInteger) session.createNativeQuery(sqlQuery).setParameter("payId", payId).getSingleResult();
			tx.commit();
			if (count.intValue() >= 0) {
				return true;
			} else {
				return false;
			}
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public List<ChargingDetails> getAllActiveChargingDetails(String payId) {
		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			chargingDetailsList = session
					.createQuery(
							"from ChargingDetails C where C.payId= :payId and C.status='ACTIVE' and C.merchantTDR >= 0")
					.setParameter("payId", payId).setCacheable(true).getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return chargingDetailsList;
	}

	@SuppressWarnings("unchecked")
	public List<ChargingDetails> getAllActiveSaleChargingDetails(String payId, PaymentType paymentType) {
		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			if (paymentType.getName().equalsIgnoreCase(PaymentType.NET_BANKING.getName())) {
				chargingDetailsList = session
						.createQuery(
								"from ChargingDetails C where C.payId= :payId and C.paymentType=:paymentType and C.status='ACTIVE'")
						.setParameter("payId", payId).setParameter("paymentType", paymentType).setCacheable(true)
						.getResultList();
			} else {
				chargingDetailsList = session
						.createQuery(
								"from ChargingDetails C where C.payId= :payId and C.paymentType=:paymentType and C.status='ACTIVE' and C.transactionType='SALE'")
						.setParameter("payId", payId).setParameter("paymentType", paymentType).setCacheable(true)
						.getResultList();
			}
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return chargingDetailsList;
	}

	@SuppressWarnings("unchecked")
	public List<ChargingDetails> getAllActiveSaleSurchargeDetails(String payId, String acquirer) {
		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			chargingDetailsList = session
					.createQuery(
							"from ChargingDetails C where C.payId= :payId and C.acquirerName=:acquirerName and C.status='ACTIVE' and C.transactionType='SALE'")
					.setParameter("payId", payId).setParameter("acquirerName", acquirer).setCacheable(true)
					.getResultList();

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return chargingDetailsList;
	}

	@SuppressWarnings("unchecked")
	public List<ChargingDetails> getAllActiveSaleChargingDetails(String payId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		try {

			chargingDetailsList = session
					.createQuery(
							"from ChargingDetails C where C.payId=:payId and C.status='ACTIVE' and C.transactionType = '"
									+ TransactionType.SALE + "' and C.merchantTDR >= 0")
					.setParameter("payId", payId).setCacheable(true).getResultList();

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return chargingDetailsList;
	}

	@SuppressWarnings("unchecked")
	public List<ChargingDetails> getAllActiveChargingDetails() {
		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			chargingDetailsList = session
					.createQuery("from ChargingDetails C where C.status='ACTIVE' and C.merchantTDR >= 0")
					.getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return chargingDetailsList;
	}

	public List<ChargingDetails> findBankList(String payId) {
		List<ChargingDetails> bankList = new ArrayList<ChargingDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {

			String sqlQuery = "select * from ChargingDetails where payId=:payId and paymentType='NET_BANKING' and status='ACTIVE'";
			bankList = session.createNativeQuery(sqlQuery, ChargingDetails.class).setParameter("payId", payId)
					.getResultList();

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return bankList;
	}

	public List<ChargingDetails> getPendingChargingDetailList() {
		List<ChargingDetails> pendingChargingDetailsList = new ArrayList<ChargingDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			String sqlQuery = "select * from ChargingDetails where status='PENDING'";
			pendingChargingDetailsList = session.createNativeQuery(sqlQuery, ChargingDetails.class).getResultList();

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return pendingChargingDetailsList;
	}
	
	@SuppressWarnings("unchecked")
	public List<ChargingDetails> getChargingDetailListForPendingReport(String status) {
		List<ChargingDetails> pendingChargingDetailsList = new ArrayList<ChargingDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			StringBuilder query = new StringBuilder("from ChargingDetails where requestBySubAdmin = true");
			
			if(StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL"))
					query.append(" and status = '"+status+"'");
			else
				query.append(" and (status = 'ACTIVE' or status = 'REJECTED')");
			pendingChargingDetailsList = session.createQuery(query.toString()).getResultList();

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return pendingChargingDetailsList;
	}

	@SuppressWarnings("unchecked")
	public List<ChargingDetails> getAllSlabGroupForPendingChargingDetailList(String acqName, onUsOffUs acqMode,
			CardHolderType cardHolderType, String currency, MopType mopType, String payId, PaymentType paymentType,
			AccountCurrencyRegion paymentRegion, TDRStatus status) {
		List<ChargingDetails> pendingChargingDetailsList = new ArrayList<ChargingDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			pendingChargingDetailsList = session
					.createQuery(
							"from ChargingDetails where status=:status and acquirerName = :acqName and acquiringMode = :acqMode and"
									+ " cardHolderType = :cardHolderType and currency = :currency and mopType = :mopType and payId = :payId and paymentType = :paymentType"
									+ " and paymentsRegion = :paymentsRegion and (slabId = '01' or  slabId = '02' or slabId = '03')")
					.setParameter("status", status).setParameter("acqName", acqName).setParameter("acqMode", acqMode)
					.setParameter("cardHolderType", cardHolderType).setParameter("currency", currency)
					.setParameter("mopType", mopType).setParameter("payId", payId)
					.setParameter("paymentType", paymentType).setParameter("paymentsRegion", paymentRegion)
					.getResultList();

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return pendingChargingDetailsList;
	}
	
	@SuppressWarnings("unchecked")
	public List<ChargingDetails> getAllSlabGroupForPendingChargingDetailReportList(String acqName, onUsOffUs acqMode,
			CardHolderType cardHolderType, String currency, MopType mopType, String payId, PaymentType paymentType,
			AccountCurrencyRegion paymentRegion, String status) {
		List<ChargingDetails> pendingChargingDetailsList = new ArrayList<ChargingDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			StringBuilder query =new StringBuilder("from ChargingDetails where acquirerName = :acqName and acquiringMode = :acqMode and ");
					query.append("cardHolderType = :cardHolderType and currency = :currency and mopType = :mopType and payId = :payId and paymentType = :paymentType");
					query.append(" and paymentsRegion = :paymentsRegion and requestBySubAdmin = true and (slabId = '01' or  slabId = '02' or slabId = '03')");
					
					if(StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")){
						query.append(" and status = '"+status+"'");
					}else{
						query.append(" and (status = 'ACTIVE' or status = 'REJECTED')");
					}

			pendingChargingDetailsList = session
					.createQuery(query.toString())
					.setParameter("acqName", acqName).setParameter("acqMode", acqMode)
					.setParameter("cardHolderType", cardHolderType).setParameter("currency", currency)
					.setParameter("mopType", mopType).setParameter("payId", payId)
					.setParameter("paymentType", paymentType).setParameter("paymentsRegion", paymentRegion)
	
					.getResultList();

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return pendingChargingDetailsList;
	}

	@SuppressWarnings("unchecked")
	public List<PendingBulkCharges> getPendingBulkChargingDetailList() {
		List<PendingBulkCharges> pendingChargingDetailsList = new ArrayList<PendingBulkCharges>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			pendingChargingDetailsList = session.createQuery("from PendingBulkCharges where status='PENDING'")
					.getResultList();

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return pendingChargingDetailsList;
	}

	public List<ChargingDetails> findCardList(String payId) {
		List<ChargingDetails> bankList = new ArrayList<ChargingDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			String sqlQuery = "select * from ChargingDetails where payId=:payId and (paymentType='CREDIT_CARD' or paymentType='DEBIT_CARD') and status='ACTIVE'";

			bankList = session.createNativeQuery(sqlQuery, ChargingDetails.class).setParameter("payId", payId)
					.getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return bankList;
	}

	public ChargingDetails findPendingChargingDetail(MopType mopType, PaymentType paymentType,
			TransactionType transactionType, String acquirerName, String currency, String payId, String slabId,
			CardHolderType cardHolderType) {

		ChargingDetails chargingDetails = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			chargingDetails = (ChargingDetails) session
					.createQuery(
							"from ChargingDetails CD where CD.mopType = :mopType and CD.transactionType =:transactionType and CD.acquirerName = :acquirerName and "
									+ "CD.payId = :payId and CD.slabId = :slabId and CD.cardHolderType = :cardHolderType and CD.paymentType = :paymentType and CD.currency = :currency and CD.status = 'PENDING'")
					.setParameter("payId", payId).setParameter("paymentType", paymentType)
					.setParameter("mopType", mopType).setParameter("transactionType", transactionType)
					.setParameter("acquirerName", acquirerName).setParameter("currency", currency)
					.setParameter("paymentType", paymentType).setParameter("slabId", slabId)
					.setParameter("cardHolderType", cardHolderType).setCacheable(true).uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
			// return responseUser;
		}
		return chargingDetails;

	}

	public ChargingDetails findActiveChargingDetail(MopType mopType, PaymentType paymentType,
			TransactionType transactionType, String acquirerName, String currency, String payId,
			AccountCurrencyRegion paymentsRegion, CardHolderType cardHolderType, onUsOffUs acquiringMode,
			String slabId) {

		ChargingDetails chargingDetails = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			chargingDetails = (ChargingDetails) session
					.createQuery(
							"from ChargingDetails CD where CD.mopType = :mopType and CD.transactionType =:transactionType and CD.acquirerName = :acquirerName and "
									+ "CD.payId = :payId and CD.paymentType = :paymentType and CD.currency = :currency and CD.paymentsRegion = :paymentsRegion and CD.acquiringMode = :acquiringMode and CD.cardHolderType = :cardHolderType and CD.slabId = :slabId and CD.status = 'ACTIVE'")
					.setParameter("payId", payId).setParameter("paymentType", paymentType)
					.setParameter("mopType", mopType).setParameter("transactionType", transactionType)
					.setParameter("acquirerName", acquirerName).setParameter("currency", currency)
					.setParameter("paymentsRegion", paymentsRegion).setParameter("acquiringMode", acquiringMode)
					.setParameter("cardHolderType", cardHolderType).setParameter("slabId", slabId).setCacheable(true)
					.uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
			// return responseUser;
		}
		return chargingDetails;
	}

	public ChargingDetails findChargingDetail(MopType mopType, PaymentType paymentType, TransactionType transactionType,
			String acquirerName, String currency, String payId, onUsOffUs acquiringMode,
			AccountCurrencyRegion paymentsRegion, CardHolderType cardHolderType, String slabId) {

		ChargingDetails chargingDetails = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			chargingDetails = (ChargingDetails) session
					.createQuery(
							"from ChargingDetails CD where CD.mopType = :mopType and CD.transactionType =:transactionType and CD.acquirerName = :acquirerName and "
									+ "CD.payId = :payId and CD.paymentType = :paymentType and CD.currency = :currency and CD.cardHolderType = :cardHolderType and CD.paymentsRegion = :paymentsRegion and CD.acquiringMode = :acquiringMode and CD.slabId = :slabId and CD.status = 'ACTIVE'")
					.setParameter("payId", payId).setParameter("paymentType", paymentType)
					.setParameter("mopType", mopType).setParameter("transactionType", transactionType)
					.setParameter("acquirerName", acquirerName).setParameter("currency", currency)
					.setParameter("paymentsRegion", paymentsRegion).setParameter("cardHolderType", cardHolderType)
					.setParameter("acquiringMode", acquiringMode).setParameter("slabId", slabId)
					.setParameter("paymentType", paymentType).setCacheable(true).uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
			// return responseUser;
		}
		return chargingDetails;
	}

	public ChargingDetails findSurchargeDetail(PaymentType paymentType, String payId,
			AccountCurrencyRegion paymentsRegion, String slabId) {

		ChargingDetails chargingDetails = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			chargingDetails = (ChargingDetails) session
					.createQuery(
							"from ChargingDetails CD where CD.payId = :payId and CD.paymentType = :paymentType and CD.paymentsRegion = :paymentsRegion and CD.slabId = :slabId and CD.status = 'ACTIVE'")
					.setParameter("payId", payId).setParameter("paymentType", paymentType)
					.setParameter("paymentsRegion", paymentsRegion).setParameter("slabId", slabId).setCacheable(true)
					.uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
			// return responseUser;
		}
		return chargingDetails;
	}

	@SuppressWarnings("unchecked")
	public List<ChargingDetails> getChargingDetailsList(PaymentType paymentType, String payId,
			AccountCurrencyRegion paymentsRegion, String slabId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		try {
			chargingDetailsList = session.createQuery("from ChargingDetails where payId='" + payId
					+ "' and paymentsRegion='" + paymentsRegion + "' and paymentType='" + paymentType
					+ "' and  status='ACTIVE' and slabId='" + slabId + "'").getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return chargingDetailsList;
	}

	@SuppressWarnings("unchecked")
	public List<ChargingDetails> getCreditChargingDetailsList(PaymentType paymentType, String payId,
			AccountCurrencyRegion paymentsRegion, String slabId, CardHolderType cardHolderType) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		try {
			chargingDetailsList = session
					.createQuery(
							"from ChargingDetails where payId='" + payId + "' and paymentsRegion='" + paymentsRegion
									+ "' and paymentType='" + paymentType + "' and cardHolderType='" + cardHolderType
									+ "' and  status='ACTIVE' and slabId='" + slabId + "' order by merchantTDR desc")
					.getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return chargingDetailsList;
	}
	
	@SuppressWarnings("unchecked")
	public List<ChargingDetails> getCreditChargingDetailsList(PaymentType paymentType, String payId,
			AccountCurrencyRegion paymentsRegion, String slabId, CardHolderType cardHolderType, MopType mopType) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		try {
			chargingDetailsList = session.createQuery("from ChargingDetails where payId='" + payId
					+ "' and paymentsRegion='" + paymentsRegion + "' and paymentType='" + paymentType
					+ "' and cardHolderType='" + cardHolderType + "' and mopType='" + mopType
					+ "' and  status='ACTIVE' and slabId='" + slabId + "' order by merchantTDR desc").getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return chargingDetailsList;
	}

	@SuppressWarnings("unchecked")
	public List<ChargingDetails> getDebitChargingDetailsList(PaymentType paymentType, String payId,
			AccountCurrencyRegion paymentsRegion, String slabId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		MopType mop = MopType.VISA;
		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		try {
			chargingDetailsList = session.createQuery("from ChargingDetails where payId='" + payId
					+ "' and paymentsRegion='" + paymentsRegion + "' and paymentType='" + paymentType
					+ "' and mopType='" + mop + "' and  status='ACTIVE' and slabId='" + slabId + "'").getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return chargingDetailsList;
	}

	@SuppressWarnings("unchecked")
	public List<ChargingDetails> getDebitMasterChargingDetailsList(PaymentType paymentType, String payId,
			AccountCurrencyRegion paymentsRegion, String slabId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		MopType mop = MopType.MASTERCARD;
		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		try {
			chargingDetailsList = session.createQuery("from ChargingDetails where payId='" + payId
					+ "' and paymentsRegion='" + paymentsRegion + "' and paymentType='" + paymentType
					+ "' and mopType='" + mop + "' and  status='ACTIVE' and slabId='" + slabId + "'").getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return chargingDetailsList;
	}

	@SuppressWarnings("unchecked")
	public List<ChargingDetails> getDebitRupayChargingDetailsList(PaymentType paymentType, String payId,
			AccountCurrencyRegion paymentsRegion, String slabId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		MopType mop = MopType.RUPAY;
		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		try {
			chargingDetailsList = session.createQuery("from ChargingDetails where payId='" + payId
					+ "' and paymentsRegion='" + paymentsRegion + "' and paymentType='" + paymentType
					+ "' and mopType='" + mop + "' and  status='ACTIVE' and slabId='" + slabId + "'").getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return chargingDetailsList;
	}

	public Double getBankTDR(String acquirerName, String mopType, String paymentType, String transactionType)
			throws HibernateException, Exception {
		Double result = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		String sqlQuery = "select bankTDR from ChargingDetails where acquirerName=:acquirerName and mopType=:mopType and paymentType=:paymentType and transactionType=:transactionType";
		result = (Double) session.createNativeQuery(sqlQuery).setParameter("acquirerName", acquirerName)
				.setParameter("mopType", mopType).setParameter("paymentType", paymentType)
				.setParameter("transactionType", transactionType).getSingleResult();
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<ChargingDetails> getAllChargingSlabs(String payId, String paymentsRegion, String paymentType,
			String currency, String mopType, String transactionType, String acquiringMode, String acquirerName,
			String cardHolderType) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		try {
			chargingDetailsList = session.createQuery("from ChargingDetails where payId='" + payId
					+ "' and paymentsRegion='" + paymentsRegion + "' and paymentType='" + paymentType
					+ "' and currency='" + currency + "' and mopType='" + mopType + "' and acquiringMode='"
					+ acquiringMode + "' and status='ACTIVE' and acquirerName='" + acquirerName
					+ "' and transactionType='" + transactionType + "' and cardHolderType='" + cardHolderType + "'")
					.getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return chargingDetailsList;
	}

	@SuppressWarnings("unchecked")
	public List<ChargingDetails> getChargingDetailsListPerSlabs(String payId, String paymentsRegion, String paymentType,
			String currency, String mopType, String transactionType, String acquiringMode, String acquirerName,
			String cardHolderType, String slabId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		try {
			chargingDetailsList = session.createQuery("from ChargingDetails where payId='" + payId
					+ "' and paymentsRegion='" + paymentsRegion + "' and paymentType='" + paymentType
					+ "' and currency='" + currency + "' and mopType='" + mopType + "' and acquiringMode='"
					+ acquiringMode + "' and status='ACTIVE' and acquirerName='" + acquirerName
					+ "' and transactionType='" + transactionType + "' and cardHolderType='" + cardHolderType
					+ "' and slabId='" + slabId + '"').getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return chargingDetailsList;
	}

	@SuppressWarnings("unchecked")
	public ChargingDetails getChargingDetailsPerSlabs(String payId, String paymentType, String paymentsRegion,
			String currency, String mopType, String transactionType, String acquiringMode, String acquirerName,
			String slabId, String cardHolderType) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		ChargingDetails chargingDetail = new ChargingDetails();
		try {
			chargingDetail = (ChargingDetails) session
					.createQuery("from ChargingDetails where payId='" + payId + "' and paymentsRegion='"
							+ paymentsRegion + "' and paymentType='" + paymentType + "' and currency='" + currency
							+ "' and mopType='" + mopType + "' and acquiringMode='" + acquiringMode
							+ "' and status='ACTIVE' and acquirerName='" + acquirerName + "' and transactionType='"
							+ transactionType + "' and cardHolderType='" + cardHolderType + "' and slabId='" + slabId
							+ "'")
					.setCacheable(true).uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return chargingDetail;
	}

	public ChargingDetails getChargingDetailsWithoutSlabs(String payId, String paymentType, String currency,
			String mopType, String transactionType, String acquirerName, String cardHolderType) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		ChargingDetails chargingDetail = new ChargingDetails();
		try {
			chargingDetail = (ChargingDetails) session.createQuery("from ChargingDetails where payId='" + payId
					+ "' and paymentType='" + paymentType + "' and currency='" + currency + "' and mopType='" + mopType
					+ "' and acquirerName='" + acquirerName + "' and transactionType='" + transactionType
					+ "' and cardHolderType is null").setCacheable(true).uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return chargingDetail;
	}

	public void insertDummyChargingDetail(ChargingDetails chargingDetails) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			session.save(chargingDetails);
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}

	}

	public ChargingDetails getChargingDetailsWithoutSlabsForUpdateAll(String acquirerName, String currency,
			MopType mopType, String payId, PaymentType paymentType, TransactionType transactionType, String slabId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		ChargingDetails chargingDetail = new ChargingDetails();
		try {
			if (StringUtils.isBlank(slabId)) {
				chargingDetail = (ChargingDetails) session
						.createQuery("from ChargingDetails where payId='" + payId + "' and paymentType='" + paymentType
								+ "' and mopType='" + mopType + "' and currency = '" + currency + "' and acquirerName='"
								+ acquirerName + "' and transactionType='" + transactionType + "' and status='ACTIVE'")
						.setCacheable(true).uniqueResult();
			} else {
				chargingDetail = (ChargingDetails) session.createQuery("from ChargingDetails where payId='" + payId
						+ "' and paymentType='" + paymentType + "' and mopType='" + mopType + "' and acquirerName='"
						+ acquirerName + "' and transactionType='" + transactionType + "' and slabId='" + slabId
						+ "' and status='ACTIVE'").setCacheable(true).uniqueResult();
			}
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return chargingDetail;
	}

	@SuppressWarnings("unchecked")
	public List<ChargingDetails> findAllChargingDetailsByDate(Date createdDate, Date updatedDate) {
		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		chargingDetailsList = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			chargingDetailsList = session
					.createQuery(
							"from ChargingDetails CD where CD.createdDate >= :createdDate or CD.createdDate <= :updatedDate")
					.setParameter("createdDate", createdDate).setParameter("updatedDate", updatedDate)
					.setCacheable(true).getResultList();
			tx.commit();
			return chargingDetailsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return chargingDetailsList;
	}

	public ChargingDetails findDetailForUpdate(String payId, String slabId, PaymentType paymentType, MopType mopType,
			TransactionType transactionType, String acquirerName, AccountCurrencyRegion paymentsRegion,
			onUsOffUs acquiringMode, CardHolderType cardHolderType, String currency) {

		ChargingDetails chargingDetails = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			chargingDetails = (ChargingDetails) session
					.createQuery(
							"from ChargingDetails CD where CD.payId = :payId and CD.slabId =:slabId and CD.paymentType = :paymentType and "
									+ "CD.mopType = :mopType and CD.transactionType = :transactionType and CD.acquirerName = :acquirerName and "
									+ "CD.paymentsRegion = :paymentsRegion and CD.acquiringMode = :acquiringMode and CD.cardHolderType = :cardHolderType and "
									+ " CD.currency = :currency and CD.status = 'ACTIVE'")
					.setParameter("payId", payId).setParameter("slabId", slabId)
					.setParameter("paymentType", paymentType).setParameter("mopType", mopType)
					.setParameter("transactionType", transactionType).setParameter("acquirerName", acquirerName)
					.setParameter("paymentsRegion", paymentsRegion).setParameter("acquiringMode", acquiringMode)
					.setParameter("cardHolderType", cardHolderType).setParameter("currency", currency)
					.setCacheable(true).uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
			// return responseUser;
		}
		return chargingDetails;

	}

	public PendingBulkCharges findPendingBulkCharges(String payId, String slab, PaymentType paymentType,
			TransactionType transactionType, String acquirerName, AccountCurrencyRegion paymentsRegion,
			onUsOffUs acquiringMode, String currency) {

		PendingBulkCharges pendingBulkCharges = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			pendingBulkCharges = (PendingBulkCharges) session
					.createQuery(
							"from PendingBulkCharges PBC where PBC.payId = :payId and PBC.slab =:slab and PBC.paymentType = :paymentType and "
									+ "PBC.transactionType = :transactionType and PBC.acquirerName = :acquirerName and "
									+ "PBC.paymentsRegion = :paymentsRegion and PBC.acquiringMode = :acquiringMode and "
									+ " PBC.currency = :currency and PBC.status = 'PENDING'")
					.setParameter("payId", payId).setParameter("slab", slab).setParameter("paymentType", paymentType)
					.setParameter("transactionType", transactionType).setParameter("acquirerName", acquirerName)
					.setParameter("paymentsRegion", paymentsRegion).setParameter("acquiringMode", acquiringMode)
					.setParameter("currency", currency).setCacheable(true).uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
			// return responseUser;
		}
		return pendingBulkCharges;

	}

	public void deleteBulkCharges(PendingBulkCharges pendingBulkCharges) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			session.saveOrUpdate(pendingBulkCharges);
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
			// return responseUser;
		}

	}

	public void insertBulkCharges(PendingBulkCharges pendingBulkCharges) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			session.save(pendingBulkCharges);
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
			// return responseUser;
		}

	}

	// Method to get Charging Details using Router Configuration
	public ChargingDetails getCdForRc(RouterConfiguration rc) {

		String payId = rc.getMerchant();
		String slabId = rc.getSlabId();
		PaymentType paymentType = PaymentType.getInstanceUsingCode(rc.getPaymentType());
		MopType mopType = MopType.getmop(rc.getMopType());
		TransactionType transactionType = TransactionType.getInstanceFromCode(rc.getTransactionType());
		String acquirerName = AcquirerType.getInstancefromCode(rc.getAcquirer()).getName();

		AccountCurrencyRegion paymentsRegion = rc.getPaymentsRegion();
		onUsOffUs acquiringMode = null;
		if (rc.getOnUsoffUsName().equalsIgnoreCase(onUsOffUs.ON_US.toString())) {
			acquiringMode = onUsOffUs.ON_US;
		} else {
			acquiringMode = onUsOffUs.OFF_US;
		}
		CardHolderType cardHolderType = rc.getCardHolderType();
		String currency = rc.getCurrency();

		ChargingDetails chargingDetails = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			chargingDetails = (ChargingDetails) session
					.createQuery(
							"from ChargingDetails CD where CD.payId = :payId and CD.slabId =:slabId and CD.paymentType = :paymentType and "
									+ "CD.mopType = :mopType and CD.transactionType = :transactionType and CD.acquirerName = :acquirerName and "
									+ "CD.paymentsRegion = :paymentsRegion and CD.acquiringMode = :acquiringMode and CD.cardHolderType = :cardHolderType and "
									+ " CD.currency = :currency and CD.status = 'ACTIVE'")
					.setParameter("payId", payId).setParameter("slabId", slabId)
					.setParameter("paymentType", paymentType).setParameter("mopType", mopType)
					.setParameter("transactionType", transactionType).setParameter("acquirerName", acquirerName)
					.setParameter("paymentsRegion", paymentsRegion).setParameter("acquiringMode", acquiringMode)
					.setParameter("cardHolderType", cardHolderType).setParameter("currency", currency)
					.setCacheable(true).uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
			// return responseUser;
		}
		return chargingDetails;

	}

	@SuppressWarnings("unchecked")
	public List<ChargingDetails> getSimilarCdList(ChargingDetails cd) {

		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		chargingDetailsList = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			chargingDetailsList = session
					.createQuery(
							"from ChargingDetails CD where CD.mopType = :mopType and CD.transactionType =:transactionType "
									+ " and CD.payId = :payId and CD.paymentType = :paymentType and CD.currency = :currency and CD.paymentsRegion = :paymentsRegion"
									+ " and CD.acquiringMode = :acquiringMode and CD.cardHolderType = :cardHolderType and CD.slabId = :slabId and CD.status = 'ACTIVE'")
					.setParameter("mopType", cd.getMopType()).setParameter("transactionType", cd.getTransactionType())
					.setParameter("payId", cd.getPayId()).setParameter("paymentType", cd.getPaymentType())
					.setParameter("currency", cd.getCurrency()).setParameter("paymentsRegion", cd.getPaymentsRegion())
					.setParameter("acquiringMode", cd.getAcquiringMode())
					.setParameter("cardHolderType", cd.getCardHolderType()).setParameter("slabId", cd.getSlabId())
					.setCacheable(true).getResultList();
			tx.commit();
			return chargingDetailsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}

		return chargingDetailsList;

	}

	@SuppressWarnings("unchecked")
	public List<ChargingDetails> getMerchantActiveChargingDetails(String payId) {
		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			chargingDetailsList = session
					.createQuery(
							"from ChargingDetails C where C.status='ACTIVE' and C.merchantTDR > 0 and payId = :payId order by createdDate")
					.setParameter("payId", payId).getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return chargingDetailsList;
	}

	public PendingBulkCharges findPendingBulkChargesById(long id) {
		PendingBulkCharges pendingBulkCharges = new PendingBulkCharges();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			pendingBulkCharges = (PendingBulkCharges) session.createQuery("from PendingBulkCharges where id=:id")
					.setParameter("id", id).getSingleResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return pendingBulkCharges;
	}

	public void updateBulkPendingStatus(PendingBulkCharges fetchPendingBulkDetails, TDRStatus status,
			String loginUserMerchantEmailId) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			fetchPendingBulkDetails.setStatus(status);
			fetchPendingBulkDetails.setUpdateBy(loginUserMerchantEmailId);
			fetchPendingBulkDetails.setUpdatedDate(new Date());
			session.saveOrUpdate(fetchPendingBulkDetails);
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
	}

	@SuppressWarnings("unchecked")
	public List<PendingBulkCharges> getPendingBulkChargingDetailList(String status) {
		List<PendingBulkCharges> pendingChargingDetailsList = new ArrayList<PendingBulkCharges>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			StringBuilder query=new StringBuilder("from PendingBulkCharges where ");
			if(StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")){
				query.append(" status = '"+status+"'");
			}else{
				query.append(" (status = 'ACTIVE' or status = 'REJECTED')");
			}
			
			pendingChargingDetailsList = session.createQuery(query.toString()).getResultList();

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return pendingChargingDetailsList;
	}

	public PendingBulkCharges findDetailForUpdate(String payId, String slab, PaymentType paymentType,
			onUsOffUs acquiringMode, TransactionType transactionType, String acquirerName,
			AccountCurrencyRegion paymentsRegion, String currency) {
		
		PendingBulkCharges pendingBulkCharges = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			pendingBulkCharges = (PendingBulkCharges) session
					.createQuery(
							"from PendingBulkCharges PBC where PBC.payId = :payId and PBC.slab =:slab and PBC.paymentType = :paymentType and "
									+ "PBC.transactionType = :transactionType and PBC.acquirerName = :acquirerName and "
									+ "PBC.paymentsRegion = :paymentsRegion and PBC.acquiringMode = :acquiringMode and "
									+ " PBC.currency = :currency and PBC.status = 'ACTIVE'")
					.setParameter("payId", payId).setParameter("slab", slab).setParameter("paymentType", paymentType)
					.setParameter("transactionType", transactionType).setParameter("acquirerName", acquirerName)
					.setParameter("paymentsRegion", paymentsRegion).setParameter("acquiringMode", acquiringMode)
					.setParameter("currency", currency).setCacheable(true).uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
			// return responseUser;
		}
		return pendingBulkCharges;
	}


}
