package com.paymentgateway.commons.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.user.PendingMappingRequest;

@Component
public class PendingMappingRequestDao extends HibernateAbstractDao {
	

	public void create(PendingMappingRequest pendingMappingRequest) throws DataAccessLayerException {
		super.save(pendingMappingRequest);
		
	}


	@SuppressWarnings("unchecked")
	public List<PendingMappingRequest> getPendingMappingRequest() {
		List<PendingMappingRequest> pendingMappingRequestList = new ArrayList<PendingMappingRequest>();
		pendingMappingRequestList = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		
		try {
			pendingMappingRequestList = session.createQuery("from PendingMappingRequest PMR where PMR.status='PENDING'")
					.setCacheable(true)
					.getResultList();
			tx.commit();
			return pendingMappingRequestList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return pendingMappingRequestList;
	}
	
	@SuppressWarnings("unchecked")
	public List<PendingMappingRequest> getPendingMappingRequest(String status) {
		List<PendingMappingRequest> pendingMappingRequestList = new ArrayList<PendingMappingRequest>();
		pendingMappingRequestList = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		
		try {
			StringBuilder query=new StringBuilder("from PendingMappingRequest PMR where PMR.status != 'PENDING' and PMR.requestBySubAdmin = true");
			if(StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL"))
			{
				query.append(" and PMR.status = '"+status+"'");
			}else{
				query.append(" and (PMR.status = 'ACTIVE' or PMR.status = 'REJECTED')");
			}
			pendingMappingRequestList = session.createQuery(query.toString())
					.setCacheable(true)
					.getResultList();
			tx.commit();
			return pendingMappingRequestList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return pendingMappingRequestList;
	}
	
	
	public PendingMappingRequest findPendingMappingRequest(String merchantEmailId , String acquirer, String mapString) {
		PendingMappingRequest pendingMappingRequest = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			pendingMappingRequest = (PendingMappingRequest) session
					.createQuery(
							"from PendingMappingRequest PMR where PMR.merchantEmailId = :merchantEmailId and PMR.acquirer = :acquirer and PMR.mapString = :mapString and PMR.status = 'PENDING' ")
					.setParameter("merchantEmailId", merchantEmailId)
					.setParameter("acquirer", acquirer)
					.setParameter("mapString", mapString)
					.setCacheable(true).getSingleResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
			// return responseUser;
		}
		return pendingMappingRequest;

	}
	
	public PendingMappingRequest duplicateFindPendingMappingRequest(String merchantEmailId , String acquirer, String mapString) {
		PendingMappingRequest pendingMappingRequest = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			pendingMappingRequest = (PendingMappingRequest) session
					.createQuery(
							"from PendingMappingRequest PMR where PMR.merchantEmailId = :merchantEmailId and PMR.acquirer = :acquirer and"
							+ " PMR.mapString = :mapString  and PMR.status = 'PENDING' ")
					.setParameter("merchantEmailId", merchantEmailId)
					.setParameter("acquirer", acquirer)
					.setParameter("mapString", mapString)
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
		return pendingMappingRequest;

	}
	
	public PendingMappingRequest findActiveMappingRequest(String merchantEmailId , String acquirer) {
		PendingMappingRequest pendingMappingRequest = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			pendingMappingRequest = (PendingMappingRequest) session
					.createQuery(
							"from PendingMappingRequest PMR where PMR.merchantEmailId = :merchantEmailId and PMR.acquirer = :acquirer and PMR.status = 'ACTIVE' ")
					.setParameter("merchantEmailId", merchantEmailId)
					.setParameter("acquirer", acquirer)
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
		return pendingMappingRequest;

	}
	
	
	@SuppressWarnings("unchecked")
	public String findActiveMappingByEmailId(String merchantEmailId) {
		List<PendingMappingRequest> activeMappingList = new ArrayList<PendingMappingRequest>();
		activeMappingList = null;
		StringBuilder currencySet = new StringBuilder();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			activeMappingList = session.createQuery("from PendingMappingRequest PMR where PMR.merchantEmailId = :merchantEmailId and PMR.status = 'ACTIVE' ")
					.setParameter("merchantEmailId", merchantEmailId)
					.setCacheable(true)
					.getResultList();
			tx.commit();
			
			for(PendingMappingRequest pendingMappingRequest: activeMappingList){
				currencySet.append(pendingMappingRequest.getAccountCurrencySet());
			}
				
			return currencySet.toString();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return null;
	}
	
	public boolean findActiveMappingByEmailIdForActiveStatus(String merchantEmailId) {
		BigInteger count = new BigInteger("0");
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			String sqlQuery = "select count(*) from PendingMappingRequest where merchantEmailId=:merchantEmailId and status='ACTIVE' and mapString != ''";
			count = (BigInteger) session.createNativeQuery(sqlQuery).setParameter("merchantEmailId", merchantEmailId).getSingleResult();
			tx.commit();
			if (count.intValue() > 0) {
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
	
	public boolean findSuperMerchantActiveMappingByEmailIdForActiveStatus(String merchantEmailId) {
		BigInteger count = new BigInteger("0");
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			String sqlQuery = "select count(*) from PendingMappingRequest where merchantEmailId=:merchantEmailId and status='ACTIVE' and mapString != ''";
			count = (BigInteger) session.createNativeQuery(sqlQuery).setParameter("merchantEmailId", merchantEmailId).getSingleResult();
			tx.commit();
			if (count.intValue() > 0) {
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

}
