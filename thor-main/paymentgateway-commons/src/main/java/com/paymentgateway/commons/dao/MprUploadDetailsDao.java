package com.paymentgateway.commons.dao;

import javax.persistence.NoResultException;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.user.MprUploadDetails;

@Service
public class MprUploadDetailsDao extends HibernateAbstractDao {
	
	public void create(MprUploadDetails mpruploadDetails) throws DataAccessLayerException {
		super.save(mpruploadDetails);
	}
	
	
	@SuppressWarnings("unchecked")
	public MprUploadDetails existMprFile(String acquirer,String paymentType,String mprDate) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		MprUploadDetails responseMprDetails = null;
		
	
			String getNodalAmount="from MprUploadDetails n where n.acquirerName = :acquirerName and n.paymentType = :paymentType and n.mprDate = :mprDate";
			try {
				responseMprDetails =(MprUploadDetails) session.createQuery(getNodalAmount).setParameter("acquirerName", acquirer).setParameter("paymentType", paymentType).
						setParameter("mprDate", mprDate)
						.setCacheable(true).getSingleResult();
				tx.commit();
				return responseMprDetails;
			
			} catch (ObjectNotFoundException objectNotFound) {
				handleException(objectNotFound,tx);
			} catch (HibernateException hibernateException) {
				handleException(hibernateException,tx);
			} 
			catch(NullPointerException ex)
			{
				ex.printStackTrace();
			}catch (NoResultException ex) {
			
			}
			finally {
				autoClose(session);
			}
			if(responseMprDetails==null) {
				return responseMprDetails;
			}
			return responseMprDetails;		
		
	    
	}

}
