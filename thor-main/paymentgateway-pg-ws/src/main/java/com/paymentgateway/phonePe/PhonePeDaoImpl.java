package com.paymentgateway.phonePe;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


import com.paymentgateway.commons.user.MPAMerchant;

@Repository
//@Transactional
public class PhonePeDaoImpl implements PhonePeDao {
	
//	@Autowired
//	SessionFactory sessionFactory;
	
	@Override
	public void savePhonePeTransaction(Transaction entity) {
//		try {
//			Session session = sessionFactory.getCurrentSession();
//			session.saveOrUpdate(entity);
//		}catch(Exception e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public String payRequestForUPIOpenIntentFlow() {
		return null;
	}

	@Override
	public String payRequestForUPICollect() {
		return null;
	}

	@Override
	public String payRequestForUPIQr() {
		return null;
	}

	@Override
	public String payRequestForNewCardFlow() {
		return null;
	}

	@Override
	public String payRequestWithCardId() {
		return null;
	}

	@Override
	public String payRequestWithToken() {
		return null;
	}

	@Override
	public String payRequestForNetBanking() {
		return null;
	}
	
	
//	public MPAMerchant getMerchantById(String payId){
//		try {
//			Session session = sessionFactory.getCurrentSession();
//		Query query = session.createQuery("FROM mpamerchant where payId=:payId");
//		query.setParameter("payId" , payId);
//		return (MPAMerchant)query.getSingleResult();
//		}catch (NoResultException e){
//			return null;
//		}
//	}
//	
//	public List<Transaction> findAllTransaction(){
//		try {
//			Session session = sessionFactory.getCurrentSession();
//		Query<Transaction> query = session.createQuery("FROM transaction");
//		return query.getResultList();
//		}catch (NoResultException e){
//			return null;
//		}
//	}


}