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
public class PendingUserApprovalDao extends HibernateAbstractDao {

	public PendingUserApprovalDao() {
	}

	@SuppressWarnings("unused")
	private Connection getConnection() throws SQLException {
		return DataAccessObject.getBasicConnection();
	}

	public void create(PendingUserApproval pendingUserApproval) throws DataAccessLayerException {
		super.save(pendingUserApproval);
	}

	public void update(PendingUserApproval pendingUserApproval) throws DataAccessLayerException {
		super.saveOrUpdate(pendingUserApproval);
	}

	public PendingUserApproval find(String payId) {
		PendingUserApproval responseUser = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			responseUser = (PendingUserApproval) session
					.createQuery("from PendingUserApproval P where P.payId = :payId and P.requestStatus = 'PENDING'")
					.setParameter("payId", payId).setCacheable(true).uniqueResult();	
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
	public List<PendingUserApproval> getPendingUserProfileList() {
		List<PendingUserApproval> userProfileList = new ArrayList<PendingUserApproval>();
		userProfileList = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			userProfileList = session.createQuery("from PendingUserApproval where requestStatus ='PENDING' ").setCacheable(true).getResultList();
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
