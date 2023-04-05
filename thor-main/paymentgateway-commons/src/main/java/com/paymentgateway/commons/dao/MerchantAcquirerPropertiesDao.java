package com.paymentgateway.commons.dao;

import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.user.MerchantAcquirerProperties;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.TDRStatus;

@Service
public class MerchantAcquirerPropertiesDao extends HibernateAbstractDao {

	public void create(MerchantAcquirerProperties merchantAcquirerProperties) throws DataAccessLayerException {
		super.save(merchantAcquirerProperties);
	}

	public MerchantAcquirerProperties getMerchantAcquirerProperties(String merchantPayId, String acquirerCode) {
		MerchantAcquirerProperties merchantAcquirerProperties = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			merchantAcquirerProperties = (MerchantAcquirerProperties) session.createQuery(
					"from MerchantAcquirerProperties MP where MP.merchantPayId = :merchantPayId and MP.acquirerCode = :acquirerCode and MP.status = 'ACTIVE' ")
					.setParameter("merchantPayId", merchantPayId)
					.setParameter("acquirerCode", acquirerCode).setCacheable(true).uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return merchantAcquirerProperties;

	}
	
	public MerchantAcquirerProperties getMerchantAcquirerPropertiesByName(String merchantPayId, String acquirerName) {
		MerchantAcquirerProperties merchantAcquirerProperties = null;
		AcquirerType acquirer = AcquirerType.getInstancefromName(acquirerName);
		String acquirerCode = acquirer.getCode();
		
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			merchantAcquirerProperties = (MerchantAcquirerProperties) session.createQuery(
					"from MerchantAcquirerProperties MP where MP.merchantPayId = :merchantPayId and MP.acquirerCode = :acquirerCode and MP.status = 'ACTIVE' ")
					.setParameter("merchantPayId", merchantPayId)
					.setParameter("acquirerCode", acquirerCode).setCacheable(true).uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return merchantAcquirerProperties;

	}

	@SuppressWarnings("unchecked")
	public void addOrUpdateMerchantAcquirerProperties(MerchantAcquirerProperties merchantAcquirerProperties) {

		MerchantAcquirerProperties merchantAcquirerPropertiesFromDb = new MerchantAcquirerProperties();
		merchantAcquirerPropertiesFromDb = getMerchantAcquirerProperties(merchantAcquirerProperties.getMerchantPayId(),
				merchantAcquirerProperties.getAcquirerCode());
		Session session = null;
		Date currentDate = new Date();

		if (merchantAcquirerPropertiesFromDb != null) {

			try {
				session = HibernateSessionProvider.getSession();
				Transaction tx = session.beginTransaction();
				Long id = merchantAcquirerPropertiesFromDb.getId();
				session.load(merchantAcquirerPropertiesFromDb, merchantAcquirerPropertiesFromDb.getId());
				MerchantAcquirerProperties merchantAcquirerPropertiesUpdate = (MerchantAcquirerProperties) session
						.get(MerchantAcquirerProperties.class, id);
				merchantAcquirerPropertiesUpdate.setStatus(TDRStatus.INACTIVE);
				merchantAcquirerPropertiesUpdate.setUpdateDate(currentDate);
				session.update(merchantAcquirerPropertiesUpdate);
				tx.commit();
				session.close();

			} catch (HibernateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				autoClose(session);
			}
		}

		create(merchantAcquirerProperties);

	}

}
