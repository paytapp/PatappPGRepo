package com.paymentgateway.commons;

import javax.persistence.NoResultException;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.user.GstRSaleReport;

@Component("GstRSaleReportDao")
//@Scope(value = "prototype")
public class GstRSaleReportDao extends HibernateAbstractDao{
private static Logger logger = LoggerFactory.getLogger(GstRSaleReportDao.class.getName());
	
	public GstRSaleReportDao() {
		super();
	}
	
	public void create(GstRSaleReport gstRSaleReport) throws DataAccessLayerException {
		super.save(gstRSaleReport);
	}
	
public GstRSaleReport findBymonthByPayId(String payId,String month,String year) {
		
	Session session = HibernateSessionProvider.getSession();
	Transaction tx = session.beginTransaction();
	GstRSaleReport gstRSaleReport = null;
		try {
			gstRSaleReport = (GstRSaleReport) session.createQuery("from GstRSaleReport U where U.payId = :payId and U.month = :month and U.year = :year")
					  .setParameter("payId", payId)
					  .setParameter("month", month)
					  .setParameter("year", year).setCacheable(true)
                      .getSingleResult();
			tx.commit();
			getCache().evictAll();
			return gstRSaleReport;
		}catch (NoResultException noResultException){
					return null;
		}catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound,tx);
		} catch (HibernateException hibernateException) {
			logger.error("error : " , hibernateException);
			handleException(hibernateException,tx);
		} finally {
			autoClose(session);
		}
		return gstRSaleReport;
	}
}
