package com.paymentgateway.commons.user;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

@Component
public class SurchargeDao extends HibernateAbstractDao {

	public void create(Surcharge surcharge) throws DataAccessLayerException {
		super.save(surcharge);
	}

	public Surcharge findDetails(String payId, String paymentType) {
		Surcharge responseUser = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {

			responseUser = (Surcharge) session
					.createQuery("from Surcharge S where S.payId = :payId and S.paymentType = :paymentType ")
					.setParameter("payId", payId).setParameter("paymentType", paymentType).setCacheable(true)
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
		return responseUser;

	}

	public String findDetailsByRouterConfiguration(RouterConfiguration routerConfiguration) {

		String acquirerName = AcquirerType.getAcquirerName(routerConfiguration.getAcquirer());
		MopType mopType = MopType.getmop(routerConfiguration.getMopType());
		PaymentType paymentType = PaymentType.getInstanceUsingCode(routerConfiguration.getPaymentType());
		AccountCurrencyRegion acr = routerConfiguration.getPaymentsRegion();

		String payId = routerConfiguration.getMerchant();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		Surcharge responseSurcharge = null;
		try {

			responseSurcharge = (Surcharge) session.createQuery(
					"from Surcharge S where S.payId = :payId and S.paymentType = :paymentType and S.acquirerName = :acquirerName and S.mopType = :mopType "
							+ " and S.paymentsRegion = :acr and S.status = 'ACTIVE' ")
					.setParameter("payId", payId).setParameter("paymentType", paymentType)
					.setParameter("acquirerName", acquirerName).setParameter("mopType", mopType)
					.setParameter("acr", acr).setCacheable(true).uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
			// return responseUser;
		}

		if (responseSurcharge != null) {

			if (routerConfiguration.getCardHolderType().equals(CardHolderType.COMMERCIAL)) {

				return String.valueOf(responseSurcharge.getBankSurchargePercentageCommercial());
			} else {
				return String.valueOf(responseSurcharge.getBankSurchargePercentageCustomer());
			}

		} else {
			return "NA";
		}

	}

	@SuppressWarnings("unchecked")
	public List<Surcharge> findSurchargeListByPayid(String payId, String paymentTypeName) {
		PaymentType paymentType = PaymentType.getInstance(paymentTypeName);
		List<Surcharge> userSurchargeList = new ArrayList<Surcharge>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			userSurchargeList = session.createQuery(
					"from Surcharge C where C.payId= :payId and C.paymentType=:paymentType and C.status='ACTIVE'")
					.setParameter("payId", payId).setParameter("paymentType", paymentType).setCacheable(true)
					.getResultList();
			tx.commit();
			return userSurchargeList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return userSurchargeList;
	}

	@SuppressWarnings("unchecked")
	public List<Surcharge> findSurchargeListByPayIdAcquirerName(String payId, String paymentTypeName,
			String acquirerName, String mopTypeName) {
		PaymentType paymentType = PaymentType.getInstanceUsingCode(paymentTypeName);
		if (paymentType == null) {
			paymentType = PaymentType.getInstance(paymentTypeName);
		}
		MopType mopType = MopType.getInstance(mopTypeName);
		List<Surcharge> userSurchargeList = new ArrayList<Surcharge>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			userSurchargeList = session.createQuery(
					"from Surcharge C where C.payId= :payId and C.paymentType=:paymentType and C.acquirerName =:acquirerName and mopType =:mopType and C.status='ACTIVE'")
					.setParameter("payId", payId).setParameter("paymentType", paymentType)
					.setParameter("acquirerName", acquirerName).setParameter("mopType", mopType).setCacheable(true)
					.getResultList();
			tx.commit();
			return userSurchargeList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return userSurchargeList;
	}

	@SuppressWarnings("unchecked")
	public List<Surcharge> findSurchargeListByPayIdAcquirerNameRegion(String payId, String paymentTypeName,
			String acquirerName, String mopTypeName, String paymentsRegion) {
		PaymentType paymentType = PaymentType.getInstanceUsingCode(paymentTypeName);
		if (paymentType == null) {
			paymentType = PaymentType.getInstance(paymentTypeName);
		}

		AccountCurrencyRegion acr;

		if (paymentsRegion.equals(AccountCurrencyRegion.DOMESTIC.toString())) {
			acr = AccountCurrencyRegion.DOMESTIC;
		} else {
			acr = AccountCurrencyRegion.INTERNATIONAL;
		}

		MopType mopType = MopType.getInstance(mopTypeName);
		List<Surcharge> userSurchargeList = new ArrayList<Surcharge>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			userSurchargeList = session
					.createQuery("from Surcharge C where C.payId= :payId and C.paymentType=:paymentType and"
							+ " C.paymentsRegion = :acr and  C.acquirerName =:acquirerName and mopType =:mopType and C.status='ACTIVE'")
					.setParameter("payId", payId).setParameter("paymentType", paymentType)
					.setParameter("acquirerName", acquirerName).setParameter("mopType", mopType)
					.setParameter("acr", acr).setCacheable(true).getResultList();
			tx.commit();
			return userSurchargeList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return userSurchargeList;
	}

	@SuppressWarnings("unchecked")
	public List<Surcharge> findSurchargeListByPayIdAcquirerNameOffUs(String payId, String paymentTypeName,
			String acquirerName, String mopTypeName) {
		PaymentType paymentType = PaymentType.getInstanceUsingCode(paymentTypeName);
		if (paymentType == null) {
			paymentType = PaymentType.getInstance(paymentTypeName);
		}
		MopType mopType = MopType.getInstance(mopTypeName);
		List<Surcharge> userSurchargeList = new ArrayList<Surcharge>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			userSurchargeList = session.createQuery(
					"from Surcharge C where C.payId= :payId and C.paymentType=:paymentType and C.acquirerName =:acquirerName and mopType =:mopType and C.allowOnOff = '0' and C.status='ACTIVE'")
					.setParameter("payId", payId).setParameter("paymentType", paymentType)
					.setParameter("acquirerName", acquirerName).setParameter("mopType", mopType).setCacheable(true)
					.getResultList();
			tx.commit();
			return userSurchargeList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return userSurchargeList;
	}

	@SuppressWarnings("unchecked")
	public List<Surcharge> findPendingSurchargeListByPayIdAcquirerName(String payId, String paymentTypeName,
			String acquirerName, String mopTypeName) {
		PaymentType paymentType = PaymentType.getInstanceUsingCode(paymentTypeName);
		if (paymentType == null) {
			paymentType = PaymentType.getInstance(paymentTypeName);
		}
		MopType mopType = MopType.getInstance(mopTypeName);
		List<Surcharge> userSurchargeList = new ArrayList<Surcharge>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			userSurchargeList = session.createQuery(
					"from Surcharge C where C.payId= :payId and C.paymentType=:paymentType and C.acquirerName =:acquirerName and mopType =:mopType and C.status='PENDING'")
					.setParameter("payId", payId).setParameter("paymentType", paymentType)
					.setParameter("acquirerName", acquirerName).setParameter("mopType", mopType).setCacheable(true)
					.getResultList();
			tx.commit();
			return userSurchargeList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return userSurchargeList;
	}

	@SuppressWarnings("unchecked")
	public List<Surcharge> findPendingSurchargeList() {
		List<Surcharge> pendingSurchargeList = new ArrayList<Surcharge>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			pendingSurchargeList = session.createQuery("from Surcharge S where S.status='PENDING'").setCacheable(true)
					.getResultList();
			tx.commit();
			return pendingSurchargeList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return pendingSurchargeList;
	}

	@SuppressWarnings("unchecked")
	public List<Surcharge> findActiveSurchargeListByPayIdAcquirer(String payId, String acquirerName) {
		List<Surcharge> activeSurchargeList = new ArrayList<Surcharge>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			activeSurchargeList = session.createQuery(
					"from Surcharge S where S.payId = :payId and S.acquirerName = :acquirerName and S.status='ACTIVE'")
					.setParameter("payId", payId).setParameter("acquirerName", acquirerName).setCacheable(true)
					.getResultList();
			tx.commit();
			return activeSurchargeList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return activeSurchargeList;
	}

	@SuppressWarnings("unchecked")
	public List<Surcharge> findDetailsOnUsOffUs(String payId, String acquirerName, MopType mopType,
			PaymentType paymentType) {

		List<Surcharge> userSurchargeList = new ArrayList<Surcharge>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			userSurchargeList = session.createQuery(
					"from Surcharge C where C.payId= :payId and C.paymentType=:paymentType and C.status='ACTIVE' and C.mopType=:mopType and C.acquirerName=:acquirerName")
					.setParameter("payId", payId).setParameter("paymentType", paymentType)
					.setParameter("mopType", mopType).setParameter("acquirerName", acquirerName).setCacheable(true)
					.getResultList();
			tx.commit();
			return userSurchargeList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return userSurchargeList;
	}

	@SuppressWarnings("unchecked")
	public List<Surcharge> findSurchargeDetails(String payId, String acquirerName) {

		List<Surcharge> userSurchargeList = new ArrayList<Surcharge>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			userSurchargeList = session.createQuery(
					"from Surcharge C where C.payId= :payId and C.status='ACTIVE' and C.acquirerName=:acquirerName")
					.setParameter("payId", payId).setParameter("acquirerName", acquirerName).setCacheable(true)
					.getResultList();
			tx.commit();
			return userSurchargeList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return userSurchargeList;
	}

	@SuppressWarnings("unchecked")
	public List<MopType> findMopType(String payId, String acquirerName) {

		List<MopType> mopTypeList = new ArrayList<MopType>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			mopTypeList = session.createQuery(
					"select distinct(mopType) from Surcharge C where C.payId= :payId and C.status='ACTIVE' and C.acquirerName=:acquirerName")
					.setParameter("payId", payId).setParameter("acquirerName", acquirerName).setCacheable(true)
					.getResultList();
			tx.commit();
			return mopTypeList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return mopTypeList;
	}

	@SuppressWarnings("unchecked")
	public List<PaymentType> findPaymentType(String payId, String acquirerName) {

		List<PaymentType> userPaymentList = new ArrayList<PaymentType>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			userPaymentList = session.createQuery(
					"select distinct(paymentType) from Surcharge C where C.payId= :payId and C.status='ACTIVE' and C.acquirerName=:acquirerName")
					.setParameter("payId", payId).setParameter("acquirerName", acquirerName).setCacheable(true)
					.getResultList();
			tx.commit();
			return userPaymentList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return userPaymentList;
	}

	@SuppressWarnings("unchecked")
	public List<AccountCurrencyRegion> findCurrencyRegion(String payId, String acquirerName) {

		List<AccountCurrencyRegion> userSurchargeList = new ArrayList<AccountCurrencyRegion>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			userSurchargeList = session.createQuery(
					"select distinct(paymentsRegion) from Surcharge C where C.payId= :payId and C.status='ACTIVE' and C.acquirerName=:acquirerName")
					.setParameter("payId", payId).setParameter("acquirerName", acquirerName).setCacheable(true)
					.getResultList();
			tx.commit();
			return userSurchargeList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return userSurchargeList;
	}

	@SuppressWarnings("unchecked")
	public boolean isChargingDetailsMappedWithSurcharge(String payId, String acquirerName, MopType mopType,
			PaymentType paymentType) {
		List<Surcharge> surchargeDetailsList = new ArrayList<Surcharge>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			surchargeDetailsList = session.createQuery(
					"from Surcharge C where C.payId= :payId and C.paymentType=:paymentType and C.status='ACTIVE' and C.mopType=:mopType and C.acquirerName=:acquirerName")
					.setParameter("payId", payId).setParameter("paymentType", paymentType)
					.setParameter("mopType", mopType).setParameter("acquirerName", acquirerName).setCacheable(true)
					.getResultList();
			tx.commit();
			if (surchargeDetailsList.size() > 0) {
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

	public List<Surcharge> findSurcharge(String date, String payId, String acquirerName, PaymentType paymentType,
			MopType mopType, String paymentsRegionName) {
		List<Surcharge> surchargeList = new ArrayList<Surcharge>();

		AccountCurrencyRegion paymentsRegion;

		if (paymentsRegionName.equals(AccountCurrencyRegion.DOMESTIC.toString())) {
			paymentsRegion = AccountCurrencyRegion.DOMESTIC;
		} else {
			paymentsRegion = AccountCurrencyRegion.INTERNATIONAL;
		}
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			String sqlQuery = "from Surcharge where (case when updatedDate=createdDate then :date between createdDate and current_timestamp() else  createdDate <= :date and updatedDate > :date end) and payId=:payId and acquirerName=:acquirerName and paymentType=:paymentType and mopType=:mopType and paymentsRegion = :paymentsRegion";

			surchargeList = session.createNativeQuery(sqlQuery, Surcharge.class).setParameter("date", date)
					.setParameter("payId", payId).setParameter("acquirerName", acquirerName)
					.setParameter("mopType", mopType).setParameter("paymentType", paymentType)
					.setParameter("paymentsRegion", paymentsRegion).getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return surchargeList;
	}

	@SuppressWarnings("unchecked")
	public List<Surcharge> findSurchargeList(String date, String payId, String acquirerName, PaymentType paymentType,
			MopType mopType, String paymentsRegion) {

		AccountCurrencyRegion acr;

		if (paymentsRegion.equals(AccountCurrencyRegion.DOMESTIC.toString())) {
			acr = AccountCurrencyRegion.DOMESTIC;
		} else {
			acr = AccountCurrencyRegion.INTERNATIONAL;
		}

		List<Surcharge> userSurchargeList = new ArrayList<Surcharge>();
		List<Surcharge> surchargeList = new ArrayList<Surcharge>();

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			userSurchargeList = session
					.createQuery("from Surcharge C where C.payId= :payId and C.paymentType=:paymentType and"
							+ " C.paymentsRegion = :acr and  C.acquirerName =:acquirerName and mopType =:mopType")
					.setParameter("payId", payId).setParameter("paymentType", paymentType)
					.setParameter("acquirerName", acquirerName).setParameter("mopType", mopType)
					.setParameter("acr", acr).setCacheable(true).getResultList();
			tx.commit();

			String dateTransaction = date;

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date dateTransactionDate;
			dateTransactionDate = sdf.parse(dateTransaction);

			for (Surcharge surcharge : userSurchargeList) {

				String dateUpdated = surcharge.getUpdatedDate().toString();
				String dateCreated = surcharge.getCreatedDate().toString();

				Date dateCreatedDate = sdf.parse(dateCreated);
				Date dateUpdatedDate = sdf.parse(dateUpdated);

				if (surcharge.getStatus().toString().equalsIgnoreCase(TDRStatus.ACTIVE.getName())
						&& dateTransactionDate.compareTo(dateCreatedDate) >= 0) {
					surchargeList.add(surcharge);
					break;
				} else if (dateTransactionDate.compareTo(dateCreatedDate) >= 0
						&& dateTransactionDate.compareTo(dateUpdatedDate) <= 0) {
					surchargeList.add(surcharge);
					break;
				}
			}

			return surchargeList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} catch (ParseException e) {
		} finally {
			autoClose(session);
		}
		return surchargeList;
	}

	@SuppressWarnings("unchecked")
	public List<Surcharge> findSurchargeListActive(String date, String payId, String acquirerName,
			PaymentType paymentType, MopType mopType, String paymentsRegion) {

		AccountCurrencyRegion acr;

		if (paymentsRegion.equals(AccountCurrencyRegion.DOMESTIC.toString())) {
			acr = AccountCurrencyRegion.DOMESTIC;
		} else {
			acr = AccountCurrencyRegion.INTERNATIONAL;
		}

		List<Surcharge> userSurchargeList = new ArrayList<Surcharge>();
		List<Surcharge> surchargeList = new ArrayList<Surcharge>();

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			userSurchargeList = session
					.createQuery("from Surcharge C where C.payId= :payId and C.paymentType=:paymentType and"
							+ " C.paymentsRegion = :acr and  C.acquirerName =:acquirerName and mopType =:mopType and C.status = 'ACTIVE'")
					.setParameter("payId", payId).setParameter("paymentType", paymentType)
					.setParameter("acquirerName", acquirerName).setParameter("mopType", mopType)
					.setParameter("acr", acr).setCacheable(true).getResultList();
			tx.commit();

			String dateTransaction = date;

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			for (Surcharge surcharge : userSurchargeList) {

				surchargeList.add(surcharge);
			}

			return surchargeList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return surchargeList;
	}

	@SuppressWarnings("unchecked")
	public List<Surcharge> findAllSurchargeByDate(Date createdDate, Date updatedDate) {

		List<Surcharge> surchargeList = new ArrayList<Surcharge>();
		surchargeList = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			surchargeList = session
					.createQuery(
							"from Surcharge S where S.createdDate >= :createdDate or S.createdDate <= :updatedDate")
					.setParameter("createdDate", createdDate).setParameter("updatedDate", updatedDate)
					.setCacheable(true).getResultList();
			tx.commit();
			return surchargeList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return surchargeList;
	}

	@SuppressWarnings("unchecked")
	public List<Surcharge> findSurchargeForTxn(String payId, String paymentTypeCode, String acquirer,
			String mopTypeCode, boolean allowOnOff, AccountCurrencyRegion paymentsRegion) {
		PaymentType paymentType = PaymentType.getInstanceUsingCode(paymentTypeCode);
		if (paymentType == null) {
			paymentType = PaymentType.getInstance(paymentTypeCode);
		}
		MopType mopType = MopType.getmop(mopTypeCode);
		String acquirerName = AcquirerType.getAcquirerName(acquirer);

		List<Surcharge> userSurchargeList = new ArrayList<Surcharge>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			userSurchargeList = session.createQuery(
					"from Surcharge C where C.payId= :payId and C.paymentType=:paymentType and C.acquirerName =:acquirerName and C.paymentsRegion = :paymentsRegion and mopType =:mopType and C.allowOnOff = :allowOnOff and C.status='ACTIVE'")
					.setParameter("payId", payId).setParameter("paymentType", paymentType)
					.setParameter("acquirerName", acquirerName).setParameter("mopType", mopType)
					.setParameter("allowOnOff", allowOnOff).setParameter("paymentsRegion", paymentsRegion)
					.setCacheable(true).getResultList();
			tx.commit();
			return userSurchargeList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return userSurchargeList;
	}

}
