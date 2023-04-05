package com.paymentgateway.commons.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.util.UserStatusType;

@Service
public class LoginOtpDao extends HibernateAbstractDao {

	private static Logger logger = LoggerFactory.getLogger(LoginOtpDao.class.getName());

	public void create(LoginOtp login) throws DataAccessLayerException {
		super.save(login);
	}

	public void createPin(ForgetPin pin) throws DataAccessLayerException {
		super.save(pin);
	}

	public void updatePin(ForgetPin pin) throws DataAccessLayerException {
		super.saveOrUpdate(pin);
	}

	public void update(LoginOtp otp) throws DataAccessLayerException {
		super.saveOrUpdate(otp);
	}

	/*
	 * public User getUserData(String emailId) {
	 * 
	 * User user = null; Session session = HibernateSessionProvider.getSession();
	 * Transaction tx = session.beginTransaction(); try {
	 * 
	 * user = (User)session.createQuery("from User U where U.emailId = :emailId")
	 * .setParameter("emailId", emailId).getSingleResult();
	 * 
	 * tx.commit(); } catch (ObjectNotFoundException objectNotFound) {
	 * handleException(objectNotFound,tx); } catch (HibernateException
	 * hibernateException) { handleException(hibernateException,tx); } finally {
	 * autoClose(session); } return user; }
	 */

	public User getUserData(String phoneNumber) {

		User user = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {

			user = (User) session.createQuery("from User U where U.mobile = :mobile")
					.setParameter("mobile", phoneNumber).getSingleResult();

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return user;
	}

	public void checkOtp(String mobile, String currentDate) {

		
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			session.createQuery("UPDATE LoginOtp U set U.status = 'InActive', U.updateDate = :updateDate "
					+ "where U.mobileNo = :mobileNo and U.status = :status")
					.setParameter("mobileNo", mobile).setParameter("status", "Active")
					.setParameter("updateDate", currentDate).executeUpdate();

			tx.commit();
			// return otp;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} catch (NullPointerException ex) {
			logger.error("Null pointer exception in LoginOtpDao , checkOtp", ex);
		} finally {
			autoClose(session);
		}
	}

	public ForgetPin checkForgetOtp(String mobileNo) {

		ForgetPin otp = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			otp = (ForgetPin) session
					.createQuery("from ForgetPin U where U.mobileNo = :mobileNo and U.status = :status")
					.setParameter("mobileNo", mobileNo).setParameter("status", "Active").getSingleResult();

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} catch (NullPointerException ex) {
			logger.error("Exception in LoginOtpDao , checkForgetOtp ", ex);
		} finally {
			autoClose(session);

		}
		return otp;
	}

	public LoginOtp checkExpireOtp(String userOtp, String mobile) {

		LoginOtp otp = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			otp = (LoginOtp) session
					.createQuery("from LoginOtp U where U.otp = :otp and U.mobileNo = :mobileNo and U.status = :status")
					.setParameter("otp", userOtp).setParameter("mobileNo", mobile).setParameter("status", "Active")
					.getSingleResult();

			tx.commit();
			return otp;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} catch (NullPointerException ex) {
			logger.error("Null pointer exception in LoginOtpDao , checkExpireOtp", ex);
		} finally {
			autoClose(session);

		}
		return otp;
	}

	public ForgetPin checkExpirePasswordOtp(String otp, String mobileNo) {
		ForgetPin forgetPin = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			forgetPin = (ForgetPin) session.createQuery("from ForgetPin FP where FP.otp = '" + otp
					+ "' and FP.mobileNo = '" + mobileNo + "' and FP.status = 'Active'").getSingleResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} catch (NullPointerException ex) {
			logger.error("Null pointer exception in LoginOtpDao , checkExpireOtp", ex);
		} finally {
			autoClose(session);
		}
		return forgetPin;
	}

	public LoginOtp checkEmail(String emailId) {
		LoginOtp otp = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			otp = (LoginOtp) session.createQuery("from LoginOtp U where U.emailId = :emailId and U.status = :status")
					.setParameter("emailId", emailId).setParameter("status", "Active").getSingleResult();
			tx.commit();
			return otp;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} catch (NullPointerException ex) {
			logger.error("Null pointer exception in LoginOtpDao , checkEmail", ex);
		} finally {
			autoClose(session);
		}
		return otp;
	}
}
