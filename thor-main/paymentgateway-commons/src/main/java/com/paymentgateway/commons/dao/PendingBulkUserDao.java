package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.user.PendingBulkUserRequest;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.TDRStatus;


/**
 * @author Rajit
 *
 */
@Service
public class PendingBulkUserDao extends HibernateAbstractDao {

	
	public PendingBulkUserDao() {
	}
	
	
	public void create(PendingBulkUserRequest pendingBulkUserRequest) throws DataAccessLayerException {
		super.save(pendingBulkUserRequest);
	}
	
	
	public PendingBulkUserRequest find(String mobileNumber) {
		PendingBulkUserRequest responseUser = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			responseUser = (PendingBulkUserRequest) session
					.createQuery("from PendingBulkUserRequest PBUR where PBUR.mobileNumber = :mobileNumber and PBUR.status = 'PENDING'")
					.setParameter("mobileNumber", mobileNumber).setCacheable(true).uniqueResult();	
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
	
	@SuppressWarnings("unchecked")
	public List<PendingBulkUserRequest> getPendingBulkUserList() {
		List<PendingBulkUserRequest> userProfileList = new ArrayList<PendingBulkUserRequest>();
		userProfileList = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			userProfileList = session.createQuery("from PendingBulkUserRequest PBUR where PBUR.status = 'PENDING'").setCacheable(true).getResultList();
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

	public void updatePendingBulkUser(PendingBulkUserRequest bulkUser, TDRStatus status , String requestedBy , String updatedBy) {

		Date date = new Date();
		String currentDate = DateCreater.formatDateForDb(date);
		try {

			Session session = null;
			session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			Long id = bulkUser.getId();
			session.load(bulkUser, bulkUser.getId());
			PendingBulkUserRequest pendingUserRequest = (PendingBulkUserRequest) session.get(PendingBulkUserRequest.class, id);
			pendingUserRequest.setStatus(status);
			pendingUserRequest.setUpdatedDate(currentDate);
			if(!requestedBy.equalsIgnoreCase("")){
				pendingUserRequest.setRequestedBy(requestedBy);
			}
			if (!updatedBy.equalsIgnoreCase("")){
				pendingUserRequest.setUpdatedBy(updatedBy);
			}
			
			session.update(pendingUserRequest);
			tx.commit();
			session.close();

		} catch (HibernateException e) {
			e.printStackTrace();
		} finally {

		}

	}
	
}
