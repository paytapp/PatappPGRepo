package com.paymentgateway.commons.dao;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.RatesDefault;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.BusinessType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.onUsOffUs;

@Service
public class RatesDefaultDao extends HibernateAbstractDao {

	@Autowired
	private UserDao userDao;

	public void create(RatesDefault ratesDefault) throws DataAccessLayerException {
		super.save(ratesDefault);
	}

	public void saveOrUpdate(RatesDefault ratesDefault) throws DataAccessLayerException {
		super.save(ratesDefault);
	}

	@SuppressWarnings({ "unchecked", "finally" })
	public RatesDefault findDefaultRates(String acquirer, PaymentType paymentType, MopType mopType,
			AccountCurrencyRegion paymentsRegion, onUsOffUs acquiringMode, String slabId, String industryCategory,
			UserType userType, CardHolderType cardHolderType) {

		RatesDefault ratesDef = null;

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			ratesDef = (RatesDefault) session.createQuery(
					"from RatesDefault RD  where  RD.acquirer =:acquirer and RD.paymentType =:paymentType and RD.mopType =:mopType and"
							+ " RD.paymentsRegion =:paymentsRegion and RD.acquiringMode =:acquiringMode and RD.slab =:slabId and RD.industryCategory =:industryCategory "
							+ "and RD.userType =:userType and RD.cardHolderType =:cardHolderType and RD.status ='ACTIVE'")
					.setParameter("acquirer", acquirer).setParameter("paymentType", paymentType)
					.setParameter("paymentsRegion", paymentsRegion).setParameter("acquiringMode", acquiringMode)
					.setParameter("slabId", slabId).setParameter("mopType", mopType)
					.setParameter("industryCategory", industryCategory).setParameter("userType", userType)
					.setParameter("cardHolderType", cardHolderType).setCacheable(true).getSingleResult();
			tx.commit();
			return ratesDef;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
			return ratesDef;
		}

	}

	@SuppressWarnings({ "unchecked", "finally" })
	public RatesDefault findMerchantDefaultRates(PaymentType paymentType, MopType mopType,
			AccountCurrencyRegion paymentsRegion, onUsOffUs acquiringMode, String slabId, String industryCategory,
			UserType userType, CardHolderType cardHolderType) {

		RatesDefault ratesDef = null;

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			ratesDef = (RatesDefault) session.createQuery(
					"from RatesDefault RD  where  RD.paymentType =:paymentType and RD.mopType =:mopType and"
							+ " RD.paymentsRegion =:paymentsRegion and RD.acquiringMode =:acquiringMode and RD.slab =:slabId and RD.industryCategory =:industryCategory "
							+ "and RD.userType =:userType and RD.cardHolderType =:cardHolderType and RD.status ='ACTIVE'")
					.setParameter("paymentType", paymentType).setParameter("paymentsRegion", paymentsRegion)
					.setParameter("acquiringMode", acquiringMode).setParameter("slabId", slabId)
					.setParameter("mopType", mopType).setParameter("industryCategory", industryCategory)
					.setParameter("userType", userType).setParameter("cardHolderType", cardHolderType)
					.setCacheable(true).getSingleResult();
			tx.commit();
			return ratesDef;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
			return ratesDef;
		}

	}

	@SuppressWarnings({ "unchecked", "finally" })
	public RatesDefault findAcqDefRatesByChargingDetails(ChargingDetails cd, UserType userType) {

		Map<String, String> industryCategoryLinkedMap = BusinessType.getIndustryCategoryList();

		String industryCategory = industryCategoryLinkedMap.get(userDao.getIndustryCategoryByPayId(cd.getPayId()));
		if (StringUtils.isBlank(industryCategory)) {
			return null;
		}
		RatesDefault ratesDef = null;

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		String acquirerCode = AcquirerType.getInstancefromName(cd.getAcquirerName()).getCode();
		
		try {
			ratesDef = (RatesDefault) session.createQuery(
					"from RatesDefault RD  where RD.acquirer =:acquirer and RD.paymentType =:paymentType and RD.mopType =:mopType and"
							+ " RD.paymentsRegion =:paymentsRegion and RD.acquiringMode =:acquiringMode and RD.slab =:slabId and RD.industryCategory =:industryCategory "
							+ "and RD.userType =:userType and RD.cardHolderType =:cardHolderType and RD.status ='ACTIVE'")
					.setParameter("acquirer", acquirerCode).setParameter("paymentType", cd.getPaymentType())
					.setParameter("paymentsRegion", cd.getPaymentsRegion())
					.setParameter("acquiringMode", cd.getAcquiringMode()).setParameter("slabId", cd.getSlabId())
					.setParameter("mopType", cd.getMopType()).setParameter("industryCategory", industryCategory)
					.setParameter("userType", userType).setParameter("cardHolderType", cd.getCardHolderType())
					.setCacheable(true).getSingleResult();
			tx.commit();
			return ratesDef;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
			return ratesDef;
		}

	}

	@SuppressWarnings({ "unchecked", "finally" })
	public RatesDefault findMerchantDefRatesByChargingDetails(ChargingDetails cd, UserType userType) {

		Map<String, String> industryCategoryLinkedMap = BusinessType.getIndustryCategoryList();
		String industryCategory = industryCategoryLinkedMap.get(userDao.getIndustryCategoryByPayId(cd.getPayId()));

		if (StringUtils.isBlank(industryCategory)) {
			return null;
		}
		RatesDefault ratesDef = null;

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			ratesDef = (RatesDefault) session
					.createQuery("from RatesDefault RD  where RD.paymentType =:paymentType and RD.mopType =:mopType and"
							+ " RD.paymentsRegion =:paymentsRegion and RD.acquiringMode =:acquiringMode and RD.slab =:slabId and RD.industryCategory =:industryCategory "
							+ "and RD.userType =:userType and RD.cardHolderType =:cardHolderType and RD.status ='ACTIVE'")
					.setParameter("paymentType", cd.getPaymentType())
					.setParameter("paymentsRegion", cd.getPaymentsRegion())
					.setParameter("acquiringMode", cd.getAcquiringMode()).setParameter("slabId", cd.getSlabId())
					.setParameter("mopType", cd.getMopType()).setParameter("industryCategory", industryCategory)
					.setParameter("userType", userType).setParameter("cardHolderType", cd.getCardHolderType())
					.setCacheable(true).getSingleResult();
			tx.commit();
			return ratesDef;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
			return ratesDef;
		}

	}

}
