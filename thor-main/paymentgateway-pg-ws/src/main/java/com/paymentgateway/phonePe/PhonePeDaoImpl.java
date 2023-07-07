package com.paymentgateway.phonePe;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.paymentgateway.commons.user.MPAMerchant;

@Repository
@Transactional
public class PhonePeDaoImpl implements PhonePeDao {
	
	@Autowired
	SessionFactory sessionFactory;
	
	@Override
	public void savePhonePeTransaction(Transaction entity) {
		try {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Transaction transaction = session.getTransaction();
			transaction.begin();
			session.saveOrUpdate(entity);
			transaction.commit();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
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
	
	
	@Override
	public PaymentResponse savePaymentResponse(PaymentResponse response) {
		try {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Transaction transaction = session.getTransaction();
			transaction.begin();
			session.save(response);
			transaction.commit();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return response;
		
	}
	
	@Override
	public StatusCheckResponse saveTransactionStatusResponse(StatusCheckResponse response) {
		try {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Transaction transaction = session.getTransaction();
			transaction.begin();
			session.save(response);
			transaction.commit();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return response;
		
	}
	
	@Override
	public void saveTransactionDetails(TransactionDetailsEntity entity) {
		try {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Transaction transaction = session.getTransaction();
			transaction.begin();
			session.save(entity);
			transaction.commit();
		}catch(Exception e) {
			e.printStackTrace();
		}
				
	}
	
	public void updateCallbackInTransactionDetail(TransactionDetailsEntity entity) {
		try {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Transaction transaction = session.getTransaction();
			transaction.begin();
			session.update(entity);
			transaction.commit();
		}catch(Exception e) {
			e.printStackTrace();
		}
				
	}
	
	public TransactionDetailsEntity findTransactionFromTxnId(String txnId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("FROM transaction_details where empId=:empId");
		query.setParameter("empId" , empId);
		return (TransactionDetailsEntity)query.getSingleResult();
		}catch (NoResultException e){
			return null;
		}
	}


}
