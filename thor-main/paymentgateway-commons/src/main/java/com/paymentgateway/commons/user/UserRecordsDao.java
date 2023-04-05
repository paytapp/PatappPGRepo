package com.paymentgateway.commons.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.exception.DataAccessLayerException;

/**
 * @author Puneet
 * 
 */

@Component
public class UserRecordsDao extends HibernateAbstractDao {

	private Date date = new Date();
	private UserRecords userRecords = new UserRecords();
	
	public UserRecordsDao() {
		super();
	}

	public void createDetails(String phoneNumber, String password, String payId){
		userRecords.setEmailId(phoneNumber);
		userRecords.setPassword(password);
		userRecords.setPayId(payId);
		userRecords.setPhoneNumber(phoneNumber);
		userRecords.setCreateDate(date);	
		create(userRecords);
	}
	
	public void create(UserRecords userRecords) throws DataAccessLayerException {
		super.save(userRecords);
	}

	public void delete(UserRecords userRecords) throws DataAccessLayerException {
		super.delete(userRecords);
	}

	public UserRecords find(Long id) throws DataAccessLayerException {
		return (UserRecords) super.find(UserRecords.class, id);
	}

	public UserRecords find(String name) throws DataAccessLayerException {
		return (UserRecords) super.find(UserRecords.class, name);
	}

	@SuppressWarnings("rawtypes")
	public List findAll() throws DataAccessLayerException {
		return super.findAll(UserRecords.class);
	}

	public void update(UserRecords userRecords) throws DataAccessLayerException {
		super.saveOrUpdate(userRecords);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getOldPin(String phoneNumber) {
		List<String> userOldPin = new ArrayList<String>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			userOldPin = session.createQuery("Select password from UserRecords UR where UR.phoneNumber = :phoneNumber  order by UR.id desc")
														.setParameter("phoneNumber", phoneNumber)
														.setMaxResults(4).getResultList();
			tx.commit();
	      }
		catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		}
		catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		}
		finally {
			autoClose(session);
		}
		return userOldPin;
	}
}
