package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.user.IssuerDetails;

/**
 * @author Rahul
 *
 */
@Service
public class IssuerDetailsDao extends HibernateAbstractDao {

	public void create(IssuerDetails issuerDetails) throws DataAccessLayerException {
		super.save(issuerDetails);
	}

	public void update(IssuerDetails issuerDetails) throws DataAccessLayerException {
		super.saveOrUpdate(issuerDetails);
	}

	@SuppressWarnings("unchecked")
	public List<IssuerDetails> fetchEmiSlab(String issuerName, String payId, String paymentType) {
		List<IssuerDetails> emiSlabList = new ArrayList<IssuerDetails>();

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		emiSlabList = null;

		try {
			emiSlabList = session
					.createNativeQuery(
							"select * from IssuerDetails ID  where  ID.issuerName =:issuerName and ID.payId =:payId and ID.paymentType = :paymentType and ID.status = 'ACTIVE'",
							IssuerDetails.class)
					.setParameter("issuerName", issuerName).setParameter("payId", payId)
					.setParameter("paymentType", paymentType).getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
			// return responseUser;
		}
		return emiSlabList;

	}

	@SuppressWarnings("unchecked")
	public IssuerDetails fetchEmiSlabByTenure(String issuerName, String payId, String tenure, String paymentType) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		IssuerDetails emiSlab = null;

		try {
			emiSlab = (IssuerDetails) session
					.createQuery(
							"from IssuerDetails ID where ID.issuerName =:issuerName and ID.payId =:payId and ID.paymentType = :paymentType and ID.tenure = :tenure and ID.status = 'ACTIVE' ")
					.setParameter("issuerName", issuerName).setParameter("payId", payId)
					.setParameter("paymentType", paymentType).setParameter("tenure", tenure).setCacheable(true)
					.uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return emiSlab;

	}

	public IssuerDetails getIssuerDetailabyslabId(Long id) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		IssuerDetails slab = null;
		try {
			slab = (IssuerDetails) session
					.createQuery("from IssuerDetails ID where ID.id = :id and ID.status = 'ACTIVE' ")
					.setParameter("id", id).setCacheable(true).uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return slab;
	}

	@SuppressWarnings("unchecked")
	public List<IssuerDetails> getIssuerDetailsbypayId(String payId) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		List<IssuerDetails> slabList = new ArrayList<IssuerDetails>();
		try {
			slabList = session.createQuery("from IssuerDetails ID where ID.payId = :payId and ID.status = 'ACTIVE' ")
					.setParameter("payId", payId).setCacheable(true).getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return slabList;

	}

	@SuppressWarnings("unchecked")
	public List<IssuerDetails> getIssuerDetailsForTxn(String payId, String paymentType) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		List<IssuerDetails> slabList = new ArrayList<IssuerDetails>();
		try {
			slabList = session
					.createQuery(
							"from IssuerDetails ID where ID.payId = :payId and ID.paymentType = :paymentType and ID.alwaysOn = '1' and ID.status = 'ACTIVE' ")
					.setParameter("payId", payId).setParameter("paymentType", paymentType).setCacheable(true)
					.getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return slabList;
	}

	public void editEmiSlab(String rateOfInterest, Long slabId, Boolean alwaysOn) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			Long id=slabId;
			IssuerDetails issuerDetails = session.load(IssuerDetails.class, id);
			issuerDetails.setRateOfInterest(rateOfInterest);
			issuerDetails.setAlwaysOn(alwaysOn);
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
	public List<IssuerDetails> getAllEmiSlabByPayIdAndIssuerName(String payId, String issuerName) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<IssuerDetails> emiSlabList = new ArrayList<IssuerDetails>();
		try {
			
			if(payId.equals("ALL") && issuerName.equals("ALL")) {
				
				emiSlabList = session
						.createQuery(
								"from IssuerDetails ID where ID.status = 'ACTIVE' order by merchantName asc").setCacheable(true)
						.getResultList();
				
			} else  if(payId.equals("ALL") && !issuerName.equals("ALL")) {
				
				emiSlabList = session
						.createQuery(
								"from IssuerDetails ID where ID.issuerName = :issuerName and ID.status = 'ACTIVE' order by merchantName asc")
						.setParameter("issuerName", issuerName).setCacheable(true)
						.getResultList();
				
			} else if(!payId.equals("ALL") && issuerName.equals("ALL")) {
				
				emiSlabList = session
						.createQuery(
								"from IssuerDetails ID where ID.payId = :payId and ID.status = 'ACTIVE' order by merchantName asc")
						.setParameter("payId", payId).setCacheable(true)
						.getResultList();
				
			} else {
			emiSlabList = session
					.createQuery(
							"from IssuerDetails ID where ID.payId = :payId and ID.issuerName = :issuerName and ID.status = 'ACTIVE' order by merchantName asc")
					.setParameter("payId", payId).setParameter("issuerName", issuerName).setCacheable(true)
					.getResultList();
			}
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return emiSlabList;
	}

	@SuppressWarnings("unchecked")
	public List<IssuerDetails> getActiveAllEmiSlab() {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<IssuerDetails> emiSlabList = new ArrayList<IssuerDetails>();
		try {
			emiSlabList = session.createQuery("from IssuerDetails ID where ID.status = 'ACTIVE' order by merchantName asc").getResultList();

		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return emiSlabList;

	}
}
