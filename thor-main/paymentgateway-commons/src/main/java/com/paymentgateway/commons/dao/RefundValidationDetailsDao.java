package com.paymentgateway.commons.dao;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.user.RefundValidationDetails;

@Service
public class RefundValidationDetailsDao extends HibernateAbstractDao {

	public void create(RefundValidationDetails refundValidationDetails) throws DataAccessLayerException {
		super.save(refundValidationDetails);
	}
	
	public void delete(RefundValidationDetails refundValidationDetails) throws DataAccessLayerException {
		super.delete(refundValidationDetails);
	}
	
	public void update(RefundValidationDetails refundValidationDetails) throws DataAccessLayerException {
		super.saveOrUpdate(refundValidationDetails);
	}
	
	@SuppressWarnings("unchecked")
	public List<RefundValidationDetails> getAllRefundValidationDetailsList() {
		List<RefundValidationDetails> refundValidationDetailsList = new ArrayList<RefundValidationDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			refundValidationDetailsList = session.createQuery("from RefundValidationDetails RVD order by createdDate asc")
					.setCacheable(true).getResultList();
			tx.commit();
			return refundValidationDetailsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return refundValidationDetailsList;
	}
	
	@SuppressWarnings("unchecked")
	public List<RefundValidationDetails> getRefundValidationDetailsListByPayId(String payId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<RefundValidationDetails> refundValidationDetailsList = new ArrayList<RefundValidationDetails>();
		try {

			refundValidationDetailsList = session
					.createQuery("from RefundValidationDetails DVA where DVA.payId = :payId order by createdDate desc")
					.setParameter("payId", payId).setCacheable(true).getResultList();
			tx.commit();
			return refundValidationDetailsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return refundValidationDetailsList;
	}
	
	@SuppressWarnings("unchecked")
	public List<RefundValidationDetails> getVersionListByRequestDateVersion(String payId, Date refundRequestDate, String version) {
		List<RefundValidationDetails> refundValidationDetailsList = new ArrayList<RefundValidationDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			refundValidationDetailsList = session
					.createQuery("from RefundValidationDetails RVD where RVD.refundRequestDate = :refundRequestDate and fileVersion = :fileVersion and payId = :payId order by createdDate desc")
					.setParameter("refundRequestDate", refundRequestDate)
					.setParameter("fileVersion", version)
					.setParameter("payId", payId).setCacheable(true).getResultList();
			tx.commit();
			return refundValidationDetailsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return refundValidationDetailsList;
	}
	
	@SuppressWarnings("unchecked")
	public List<RefundValidationDetails> getVersionListByRequestDate(Date refundRequestDate) {
		List<RefundValidationDetails> refundValidationDetailsList = new ArrayList<RefundValidationDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			refundValidationDetailsList = session
					.createQuery("from RefundValidationDetails RVD where RVD.refundRequestDate = :refundRequestDate order by createdDate desc")
					.setParameter("refundRequestDate", refundRequestDate).setCacheable(true).getResultList();
					tx.commit();
			
			
			return refundValidationDetailsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return refundValidationDetailsList;
	}
	
	@SuppressWarnings("unchecked")
	public List<RefundValidationDetails> getAllRefundValidationDetailsList(Date refundRequestDate, String payId) {
		List<RefundValidationDetails> refundValidationDetailsList = new ArrayList<RefundValidationDetails>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			refundValidationDetailsList = session
					.createQuery("from RefundValidationDetails DVA where DVA.refundRequestDate = :refundRequestDate  and payId = :payId order by createdDate asc")
					.setParameter("refundRequestDate", refundRequestDate)
					.setParameter("payId", payId).setCacheable(true).getResultList();
			tx.commit();
			return refundValidationDetailsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return refundValidationDetailsList;
	}
	
	/*@SuppressWarnings("unchecked")
	public List<RefundValidationDetails> getVersionTypeListByRequestDateVersion(String payId, Date refundRequestDate, String version) {
		List<RefundValidationDetails> refundValidationDetailsList = new ArrayList<RefundValidationDetails>();
		try {

			startOperation();

			refundValidationDetailsList = session
					.createQuery("from RefundValidationDetails RVD where RVD.refundRequestDate = :refundRequestDate and fileVersion = :fileVersion and payId = :payId order by createdDate desc")
					.setParameter("refundRequestDate", refundRequestDate)
					.setParameter("fileVersion", version)
					.setParameter("payId", payId).setCacheable(true).getResultList();
			tx.commit();
			return refundValidationDetailsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return refundValidationDetailsList;
	}*/
}
