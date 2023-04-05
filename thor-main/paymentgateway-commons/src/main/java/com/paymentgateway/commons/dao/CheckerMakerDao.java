package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.user.CheckerMaker;

@Component
public class CheckerMakerDao extends HibernateAbstractDao{

	private static Logger logger = LoggerFactory.getLogger(CheckerMakerDao.class.getName());
	
	public CheckerMakerDao() {
		super();
	}
	public void create(CheckerMaker checkerMaker) throws DataAccessLayerException {
		super.save(checkerMaker);
	}
	
	public void update(CheckerMaker checkerMaker) throws DataAccessLayerException {
		super.saveOrUpdate(checkerMaker);
	}
	
	public int deleteByCategory(String category) throws DataAccessLayerException {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		Query query = null;
		int result = 0;
		try {
			query = session.createQuery("delete from CheckerMaker where industryCategory =:category");
			query.setParameter("category", category);
			result = query.executeUpdate();

			tx.commit();
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			autoClose(session);
		}
		return result;
	}

	public List<CheckerMaker> findAllChekerMaker() {
		
		List<CheckerMaker> CheckerMakerList = new ArrayList<CheckerMaker>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
	
		try{
		
			CheckerMakerList = session.createQuery("from CheckerMaker", CheckerMaker.class).getResultList();

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return CheckerMakerList;
	}
	
	public CheckerMaker findByCategory(String category) {
		
		CheckerMaker checkerMaker = new CheckerMaker();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
	
		try{
		
			checkerMaker = (CheckerMaker) session.createQuery("from CheckerMaker where industryCategory =:category").setParameter("category", category).setMaxResults(1).uniqueResult();

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return checkerMaker;
	}

}