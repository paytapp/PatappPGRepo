package com.paymentgateway.commons.user;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.dao.DataAccessObject;
import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.exception.DataAccessLayerException;

/**
 * @author Rahul
 *
 */

@Component
public class PendingResellerMappingDao extends HibernateAbstractDao{
	
	public PendingResellerMappingDao() {
	}

	@SuppressWarnings("unused")
	private Connection getConnection() throws SQLException {
		return DataAccessObject.getBasicConnection();
	}
	
	public void create(PendingResellerMappingApproval pendingResellerMappingApproval) throws DataAccessLayerException {
		super.save(pendingResellerMappingApproval);
	}

	public void update(PendingResellerMappingApproval pendingResellerMappingApproval) throws DataAccessLayerException {
		super.saveOrUpdate(pendingResellerMappingApproval);
	}
	
	public PendingResellerMappingApproval find(String emailId) {
		PendingResellerMappingApproval responseUser = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			responseUser = (PendingResellerMappingApproval) session
					.createQuery("from PendingResellerMappingApproval PR where PR.merchantEmailId = :emailId and PR.requestStatus = 'PENDING'")
					.setParameter("emailId", emailId).setCacheable(true).uniqueResult();	
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return responseUser;

	}
	
	public boolean checkExistingMappingRequest(String emailId, String reseller) {
		PendingResellerMappingApproval pendingRequest = null;
		boolean exists = false ;
		
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		
		try {
			
			
			pendingRequest = (PendingResellerMappingApproval) session
					.createQuery("from PendingResellerMappingApproval PR where PR.merchantEmailId = :emailId and PR.resellerId = :reseller and PR.requestStatus = 'PENDING'")
					.setParameter("emailId", emailId)
					.setParameter("reseller", reseller)
					.setCacheable(true).uniqueResult();	
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		if (pendingRequest != null){
			exists = true;
		}
		return exists;

	}
	
	
	public PendingResellerMappingApproval findExistingMappingRequest(String emailId, String reseller) {
		PendingResellerMappingApproval pendingRequest = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			pendingRequest = (PendingResellerMappingApproval) session
					.createQuery("from PendingResellerMappingApproval PR where PR.merchantEmailId = :emailId and PR.resellerId = :reseller and PR.requestStatus = 'PENDING'")
					.setParameter("emailId", emailId)
					.setParameter("reseller", reseller)
					.setCacheable(true).uniqueResult();	
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return pendingRequest;

	}
	
	
	@SuppressWarnings("unchecked")
	public List<PendingResellerMappingApproval> getPendingResellerMappingList() {
		List<PendingResellerMappingApproval> userProfileList = new ArrayList<PendingResellerMappingApproval>();
		userProfileList = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			userProfileList = session.createQuery("from PendingResellerMappingApproval PRMA where PRMA.requestStatus = 'PENDING'").setCacheable(true).getResultList();
			tx.commit();
			return userProfileList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return userProfileList;
	}

}
