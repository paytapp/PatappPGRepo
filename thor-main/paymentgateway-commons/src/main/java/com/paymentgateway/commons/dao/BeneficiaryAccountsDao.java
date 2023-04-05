package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.user.BeneficiaryAccounts;

@Service
public class BeneficiaryAccountsDao extends HibernateAbstractDao {

	public void create(BeneficiaryAccounts beneficiaryAccounts) throws DataAccessLayerException {
		super.save(beneficiaryAccounts);
	}

	@SuppressWarnings("unchecked")
	public List<BeneficiaryAccounts> getAllBeneficiaryAccountsList() {
		List<BeneficiaryAccounts> beneficiaryAccountsList = new ArrayList<BeneficiaryAccounts>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			beneficiaryAccountsList = session.createQuery("from BeneficiaryAccounts BA order by createdDate desc")
					.setCacheable(true).getResultList();
			tx.commit();
			return beneficiaryAccountsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return beneficiaryAccountsList;
	}

	@SuppressWarnings("unchecked")
	public List<BeneficiaryAccounts> getBeneficiaryAccountsListByAcquirer(String acquirer) {
		List<BeneficiaryAccounts> beneficiaryAccountsList = new ArrayList<BeneficiaryAccounts>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			beneficiaryAccountsList = session
					.createQuery("from BeneficiaryAccounts BA where BA.acquirer = :acquirer order by createdDate desc")
					.setParameter("acquirer", acquirer).setCacheable(true).getResultList();
			tx.commit();
			return beneficiaryAccountsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return beneficiaryAccountsList;
	}
	@SuppressWarnings("unchecked")
	public List<BeneficiaryAccounts> getAllActiveBeneficiaryAccountsList() {
		List<BeneficiaryAccounts> beneficiaryAccountsList = new ArrayList<BeneficiaryAccounts>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			beneficiaryAccountsList = session.createQuery("from BeneficiaryAccounts BA where BA.status = 'ACTIVE' order by createdDate desc")
					.setCacheable(true).getResultList();
			tx.commit();
			return beneficiaryAccountsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return beneficiaryAccountsList;
	}

	@SuppressWarnings("unchecked")
	public List<BeneficiaryAccounts> getActiveBeneficiaryAccountsListByAcquirer(String acquirer) {
		List<BeneficiaryAccounts> beneficiaryAccountsList = new ArrayList<BeneficiaryAccounts>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			beneficiaryAccountsList = session
					.createQuery("from BeneficiaryAccounts BA where BA.acquirer = :acquirer and BA.status = 'ACTIVE'  order by createdDate desc")
					.setParameter("acquirer", acquirer).setCacheable(true).getResultList();
			tx.commit();
			return beneficiaryAccountsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return beneficiaryAccountsList;
	}
	
	@SuppressWarnings("unchecked")
	public BeneficiaryAccounts findById(Long id) {
		BeneficiaryAccounts beneficiaryAccounts = new BeneficiaryAccounts();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			beneficiaryAccounts = (BeneficiaryAccounts) session
					.createQuery("from BeneficiaryAccounts BA where BA.id = :id")
					.setParameter("id", id).setCacheable(true).uniqueResult();
			tx.commit();
			return beneficiaryAccounts;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return beneficiaryAccounts;
	}
	
	
	@SuppressWarnings("unchecked")
	public BeneficiaryAccounts findByBeneficiaryCd(String  beneficiaryCd) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		BeneficiaryAccounts beneficiaryAccounts = new BeneficiaryAccounts();
		try {

			beneficiaryAccounts = (BeneficiaryAccounts) session
					.createQuery("from BeneficiaryAccounts BA where BA.beneficiaryCd = :beneficiaryCd and BA.status = 'ACTIVE'")
					.setParameter("beneficiaryCd", beneficiaryCd).setCacheable(true).uniqueResult();
			tx.commit();
			return beneficiaryAccounts;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return beneficiaryAccounts;
	}
}
