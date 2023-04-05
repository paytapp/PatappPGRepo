package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.user.RefundLimitObject;
import com.paymentgateway.commons.user.User;

import software.amazon.awssdk.utils.StringUtils;

@Component
public class RefundLimitDao extends HibernateAbstractDao {

	private static Logger logger = LoggerFactory.getLogger(RefundLimitDao.class.getName());

	public void create(RefundLimitObject refundLimitObject) throws DataAccessLayerException {
		super.save(refundLimitObject);
	}

	public void update(RefundLimitObject refundLimitObject) throws DataAccessLayerException {
		super.saveOrUpdate(refundLimitObject);
	}

	@SuppressWarnings("unchecked")
	public List<RefundLimitObject> getLimitReportByPayId(String payId, String subMerchantId,String dateFrom, String dateTo) {
		logger.info("inside getByPayId : ");
		List<RefundLimitObject> refundLimiTObjList = new ArrayList<RefundLimitObject>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			if(StringUtils.isNotBlank(subMerchantId)) {
				if(!subMerchantId.equalsIgnoreCase("ALL")) {
					refundLimiTObjList = session.createQuery("from RefundLimitObject where subMerchantId = :subMerchantId and date between '" + dateFrom + "' and '" + dateTo + "' order by id desc")
							.setParameter("subMerchantId", subMerchantId).setCacheable(true).getResultList();
				} else {
					refundLimiTObjList = session.createQuery("from RefundLimitObject where payId = :payId and date between '" + dateFrom + "' and '" + dateTo + "' and status = 'ACTIVE' order by id desc")
							.setParameter("payId", payId).setCacheable(true).getResultList();
				}
				
			} else {
				refundLimiTObjList = session.createQuery("from RefundLimitObject where payId = :payId and date between '" + dateFrom + "' and '" + dateTo + "' order by id desc")
					.setParameter("payId", payId).setCacheable(true).getResultList();
			}
			tx.commit();
			for(RefundLimitObject refundLimit : refundLimiTObjList) {
				if(StringUtils.isBlank(refundLimit.getSubMerchantName())) {
					refundLimit.setSubMerchantName("NA");
				}
			}
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return refundLimiTObjList;
	}

	public List<RefundLimitObject> findAllMerchantlimitReport(String dateFrom, String dateTo) {
		logger.info("inside findAllMerchantlimitReport : ");
		List<RefundLimitObject> refundLimiTObjList = new ArrayList<RefundLimitObject>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {

			refundLimiTObjList = session.createQuery("from RefundLimitObject where date between '" + dateFrom + "' and '" + dateTo + "' and status = 'ACTIVE' order by id desc", RefundLimitObject.class).setCacheable(true).getResultList();
			tx.commit();
			for(RefundLimitObject refundLimit : refundLimiTObjList) {
				if(StringUtils.isBlank(refundLimit.getSubMerchantName())) {
					refundLimit.setSubMerchantName("NA");
				}
			}
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return refundLimiTObjList;
	}

	@SuppressWarnings("unchecked")
	public List<RefundLimitObject> findAllMerchantlimitReportByResellerId(String resellerId, String payId, String subMerchantId, String dateFrom, String dateTo) {
		logger.info("inside findAllMerchantlimitReportByResellerId : ");
		List<RefundLimitObject> refundLimiTObjList = new ArrayList<RefundLimitObject>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			if(StringUtils.isBlank(payId) && StringUtils.isBlank(subMerchantId)) {
				refundLimiTObjList = session.createQuery("from RefundLimitObject where resellerId = :resellerId and date between '" + dateFrom + "' and '" + dateTo + "' and status = 'ACTIVE' order by id desc")
						.setParameter("resellerId", resellerId).setCacheable(true).getResultList();
			} else if (StringUtils.isNotBlank(subMerchantId)) {
				if(!subMerchantId.equalsIgnoreCase("ALL")) {
					refundLimiTObjList = session.createQuery("from RefundLimitObject where resellerId = :resellerId and subMerchantId = :subMerchantId and date between '" + dateFrom + "' and '" + dateTo + "' order by id desc")
							.setParameter("resellerId", resellerId).setParameter("subMerchantId", subMerchantId).setCacheable(true).getResultList();
				} else {
					refundLimiTObjList = session.createQuery("from RefundLimitObject where resellerId = :resellerId and payId = :payId and date between '" + dateFrom + "' and '" + dateTo + "' and status = 'ACTIVE' order by id desc")
							.setParameter("resellerId", resellerId).setParameter("payId", payId).setCacheable(true).getResultList();
				}
				
			} else {
				refundLimiTObjList = session.createQuery("from RefundLimitObject where resellerId = :resellerId and payId = :payId and date between '" + dateFrom + "' and '" + dateTo + "' order by id desc")
						.setParameter("resellerId", resellerId).setParameter("payId", payId).setCacheable(true).getResultList();
			}
			tx.commit();
			for(RefundLimitObject refundLimit : refundLimiTObjList) {
				if(StringUtils.isBlank(refundLimit.getSubMerchantName())) {
					refundLimit.setSubMerchantName("NA");
				}
			}
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return refundLimiTObjList;
	}

	public RefundLimitObject getActiveByPayId(String payId, String subMerchantId) {
		logger.info("inside getActiveByPayId : ");
		RefundLimitObject refundLimiTObj = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			if (StringUtils.isNotBlank(subMerchantId)) {
				refundLimiTObj = (RefundLimitObject) session.createQuery(
						"from RefundLimitObject rl where rl.payId = '" + payId + "' and rl.subMerchantId = '" + subMerchantId + "' and status = 'ACTIVE'").setCacheable(true).uniqueResult();
			}else {
				refundLimiTObj = (RefundLimitObject) session
						.createQuery("from RefundLimitObject rl where rl.payId = '" + payId + "' and status = 'ACTIVE'").setCacheable(true).uniqueResult();
			}
			tx.commit();
			if(refundLimiTObj != null && StringUtils.isBlank(refundLimiTObj.getSubMerchantName())) {
				refundLimiTObj.setSubMerchantName("NA");
			}
			
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return refundLimiTObj;
	}

	public void deActivate(String date, User sessionUser, String payId, String subMerchantId) {
		logger.info("inside deActivate : ");
		RefundLimitObject refundLimiTObjFromDb = getActiveByPayId(payId, subMerchantId);
		
		if (refundLimiTObjFromDb != null) {
			Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			try {
				RefundLimitObject refundLimiTObj = session.load(RefundLimitObject.class, refundLimiTObjFromDb.getId());
				refundLimiTObj.setStatus("INACTIVE");
				tx.commit();
			} catch (ObjectNotFoundException objectNotFound) {
				handleException(objectNotFound, tx);
			} catch (HibernateException hibernateException) {
				handleException(hibernateException, tx);
			} finally {
				autoClose(session);
			}
		}

	}

	public RefundLimitObject activateLimit(String date, RefundLimitObject refundLimitObj) {
		logger.info("inside Activate : ");

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			refundLimitObj.setStatus("ACTIVE");
			refundLimitObj.setDate(date);
			session.save(refundLimitObj);
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return refundLimitObj;
	}
}
