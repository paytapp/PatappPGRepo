package com.paymentgateway.commons.dao;

import java.util.Date;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.user.Advertisement;

@Service
public class AdvertisementDao extends HibernateAbstractDao {

	private static Logger logger = LoggerFactory.getLogger(AdvertisementDao.class.getName());
	
	public void create(Advertisement advertisement) throws DataAccessLayerException {
		super.save(advertisement);
	}

	public Advertisement findAdvertisement() {
		
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		
		Advertisement advertisement = null;
		try {
			advertisement = (Advertisement) session
					.createQuery("from Advertisement A where A.status = 'ACTIVE' ")
					.setCacheable(true).uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
			// return responseUser;
		}
		return advertisement;

	}


	@SuppressWarnings("unchecked")
	public void addOrUpdateAdvertisement(Advertisement advertisement) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		
		Advertisement advertisementFromDb = new Advertisement();
		advertisementFromDb = findAdvertisement();
		Date currentDate = new Date();

		if (advertisementFromDb != null) {

			try {
				Long id = advertisementFromDb.getId();
				session.load(advertisementFromDb, advertisementFromDb.getId());
				Advertisement advertisementDetails = (Advertisement) session.get(Advertisement.class, id);
				advertisementDetails.setStatus("INACTIVE");
				advertisementDetails.setUpdatedDate(currentDate);
				session.update(advertisementDetails);
				tx.commit();
				session.close();

			} catch (HibernateException e) {
				logger.error("HibernateException " , e);
				handleException(e, tx);
			} catch (Exception e) {
				logger.error("Exception " , e);
			} finally {
				autoClose(session);
			}
		}

		create(advertisement);

	}


}
