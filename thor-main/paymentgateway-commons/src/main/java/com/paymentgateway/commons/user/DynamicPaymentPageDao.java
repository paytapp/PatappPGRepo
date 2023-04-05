package com.paymentgateway.commons.user;

import javax.persistence.NoResultException;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.exception.DataAccessLayerException;

@Component
public class DynamicPaymentPageDao extends HibernateAbstractDao {
	
	private static Logger logger = LoggerFactory.getLogger(DynamicPaymentPageDao.class.getName());
	private static final String getCompleteDetail = "from DynamicPaymentPage D where D.payId = :payId";
	public DynamicPaymentPageDao(){
		 super();
	}
	public void create(DynamicPaymentPage dynamicPaymentPage) throws DataAccessLayerException {
        super.save(dynamicPaymentPage);
    }

	public void delete(DynamicPaymentPage dynamicPaymentPage) throws DataAccessLayerException {
        super.delete(dynamicPaymentPage);
    }

	public void update(DynamicPaymentPage dynamicPaymentPage) throws DataAccessLayerException {
        super.saveOrUpdate(dynamicPaymentPage);
    }

	public DynamicPaymentPage findPayId(String payId1) {
		return (DynamicPaymentPage) findByPayId(payId1);
	}

	protected DynamicPaymentPage findByPayId(String payId1) {

		DynamicPaymentPage responseUser = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			responseUser = (DynamicPaymentPage) session.createQuery(getCompleteDetail)
			.setParameter("payId", payId1)
			.setCacheable(true).getSingleResult();
			tx.commit();

			return responseUser;
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
		return responseUser;
	}
}
