package com.paymentgateway.commons.user;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.exception.DataAccessLayerException;

@Service
public class NodalAmoutDao extends HibernateAbstractDao {

	

	
	private static Logger logger = LoggerFactory.getLogger(NodalAmoutDao.class.getName());
	public void create(NodalAmount nodal) throws DataAccessLayerException {
		super.save(nodal);
	}
	
	

	@SuppressWarnings("unchecked")
	public List<NodalAmount> getNodalAmount(String acquirer,String paymentType,String fromDate) {
		List<NodalAmount> responseNodalList = new ArrayList<NodalAmount>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		
			String getNodalAmount="from NodalAmount n where n.acquirer = :acquirer and n.paymentType = :paymentType and reconDate = :reconDate";
			try {
				responseNodalList = session.createQuery(getNodalAmount).setParameter("acquirer", acquirer).setParameter("paymentType", paymentType).
						setParameter("reconDate", fromDate)
						.setCacheable(true).getResultList();
				tx.commit();
				
			} catch (ObjectNotFoundException objectNotFound) {
				handleException(objectNotFound,tx);
			} catch (HibernateException hibernateException) {
				handleException(hibernateException,tx);
			} 
			finally {
				autoClose(session);
			}
		
		return responseNodalList;
	}
	
	@SuppressWarnings("unchecked")
	public List<NodalAmount> getNodalAmountCC(String acquirer,String fromDate) {
		List<NodalAmount> responseNodalList = new ArrayList<NodalAmount>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
	
			String getNodalAmount="from NodalAmount n where n.acquirer = :acquirer and n.paymentType = :paymentType and reconDate = :reconDate";
			try {
				responseNodalList = session.createQuery(getNodalAmount).setParameter("acquirer", acquirer).setParameter("paymentType", "CC-DC").
						setParameter("reconDate", fromDate)
						.setCacheable(true).getResultList();
				tx.commit();
				
			} catch (ObjectNotFoundException objectNotFound) {
				handleException(objectNotFound,tx);
			} catch (HibernateException hibernateException) {
				handleException(hibernateException,tx);
			} 
			finally {
				autoClose(session);
			}
		
		return responseNodalList;
	
}
	
	
	@SuppressWarnings("unchecked")
	public List<NodalAmount> getNodalAmountUPI(String acquirer,String fromDate) {
		List<NodalAmount> responseNodalList = new ArrayList<NodalAmount>();
		
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
			String getNodalAmount="from NodalAmount n where n.acquirer = :acquirer and n.paymentType = :paymentType and reconDate = :reconDate";
			try {
				responseNodalList = session.createQuery(getNodalAmount).setParameter("acquirer", acquirer).setParameter("paymentType", "UPI").
						setParameter("reconDate", fromDate)
						.setCacheable(true).getResultList();
				tx.commit();
				
			} catch (ObjectNotFoundException objectNotFound) {
				handleException(objectNotFound,tx);
			} catch (HibernateException hibernateException) {
				handleException(hibernateException,tx);
			}
			
			finally {
				autoClose(session);
			}
		
		return responseNodalList;
	
}
	
	@SuppressWarnings("unchecked")
	public List<NodalAmount> getNodalAmountList() {
		List<NodalAmount> responseNodalList = null;
	     String queryString="from NodalAmount";
	     Session session = HibernateSessionProvider.getSession();
		 Transaction tx = session.beginTransaction();
		
		try {
			responseNodalList = session.createQuery(queryString)
			   .getResultList();
			tx.commit();
			
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} 
		finally {
			autoClose(session);
		}
		return responseNodalList;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public NodalAmount existNodalData(String acquirer,String paymentType,String fromDate) {
	        NodalAmount responseNodalAmount = null;
	        Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
	
			String getNodalAmount="from NodalAmount n where n.acquirer = :acquirer and n.paymentType = :paymentType and reconDate = :reconDate";
			try {
				responseNodalAmount =(NodalAmount) session.createQuery(getNodalAmount).setParameter("acquirer", acquirer).setParameter("paymentType", paymentType).
						setParameter("reconDate", fromDate)
						.setCacheable(true).getSingleResult();
				tx.commit();
				return responseNodalAmount;
			
			} catch (ObjectNotFoundException objectNotFound) {
				handleException(objectNotFound,tx);
			} catch (HibernateException hibernateException) {
				handleException(hibernateException,tx);
			} 
			catch(NullPointerException ex)
			{
				logger.error("Exception in exist nodal account", ex);
			}
			finally {
				autoClose(session);
			}
		
			return responseNodalAmount;		
	    
	}
	
	@SuppressWarnings("unchecked")
	public List<NodalAmount> getSingleResult(String acquirer,String paymentType,String fromDate) {
	       List<NodalAmount> responseNodalAmount = null;
	       Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
	
			String getNodalAmount="from NodalAmount n where n.acquirer = :acquirer and n.paymentType = :paymentType and reconDate = :reconDate";
			try {
				responseNodalAmount =session.createQuery(getNodalAmount).setParameter("acquirer", acquirer).setParameter("paymentType", paymentType).
						setParameter("reconDate", fromDate)
						.setCacheable(true).getResultList();
						
				tx.commit();
				
				return responseNodalAmount;
			} catch (ObjectNotFoundException objectNotFound) {
				handleException(objectNotFound,tx);
			} catch (HibernateException hibernateException) {
				handleException(hibernateException,tx);
			} 
			catch(NullPointerException ex)
			{
				ex.printStackTrace();
			}
			finally {
				autoClose(session);
				
			}
			return responseNodalAmount;
	    
	}
	
}
