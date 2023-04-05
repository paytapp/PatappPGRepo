package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.user.SettlementDataUpload;

@Service
public class SettlementDataUploadDao extends HibernateAbstractDao {

	public void create(SettlementDataUpload settlementDataUpload) throws DataAccessLayerException {
		super.save(settlementDataUpload);
	}

	public SettlementDataUpload findLastUpload() {
		SettlementDataUpload settlementDataUpload = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			settlementDataUpload = (SettlementDataUpload) session
					.createQuery("from SettlementDataUpload SDU where SDU.id = (select max(id) from SettlementDataUpload )")
					.setCacheable(true).uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return settlementDataUpload;

	}
	
	
	@SuppressWarnings("unchecked")
	public List<SettlementDataUpload> findAll() {
		List<SettlementDataUpload> settlementDataUploadList = new ArrayList<SettlementDataUpload>();
		settlementDataUploadList = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			settlementDataUploadList = session.createQuery("from SettlementDataUpload").setCacheable(true)
					.getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		
		if (settlementDataUploadList.size()<2) {
			return settlementDataUploadList;
		}
		
		Comparator<SettlementDataUpload> comp = (SettlementDataUpload a, SettlementDataUpload b) -> {

			if (a.getCreatedDate().compareTo(b.getCreatedDate()) > 0) {
				return -1;
			} else if (a.getCreatedDate().compareTo(b.getCreatedDate()) < 0) {
				return 1;
			} else {
				return 0;
			}
		};

		Collections.sort(settlementDataUploadList, comp);
		return settlementDataUploadList;
	}
	
	

}
