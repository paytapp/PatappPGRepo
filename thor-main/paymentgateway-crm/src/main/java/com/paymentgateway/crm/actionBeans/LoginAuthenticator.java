package com.paymentgateway.crm.actionBeans;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.LoginHistoryDao;
import com.paymentgateway.commons.user.LoginOtp;
import com.paymentgateway.commons.user.LoginOtpDao;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.UserStatusType;
import com.paymentgateway.crm.action.LoginAction;

/**
 * @author Puneet
 *
 */
@Service
public class LoginAuthenticator {

	@Autowired
	@Qualifier("userDao")
	private UserDao userDao;

	@Autowired
	private LoginOtpDao otpDao;

	@Autowired
	private LoginHistoryDao loginHistoryDao;

	private User user = null;
	private static Logger logger = LoggerFactory.getLogger(LoginAuthenticator.class.getName());

	public ResponseObject authenticate(String phoneNumber, String pin, String agent, String ip) throws SystemException {

		ResponseObject responseObject = new ResponseObject();
		boolean status;
		String failureReason = null;

		user = userDao.findUserByPhone(phoneNumber);
		
		if (null == user) {
			// If user is not found, userid is invalid
			responseObject.setResponseMessage(ErrorType.USER_NOT_FOUND.getResponseMessage());
			responseObject.setResponseCode(ErrorType.USER_NOT_FOUND.getResponseCode());
			return responseObject;
		}
		
		// Userid is valid
		if (!(user.getUserStatus().equals(UserStatusType.ACTIVE)
				|| user.getUserStatus().equals(UserStatusType.TRANSACTION_BLOCKED)
				|| user.getUserStatus().equals(UserStatusType.SUSPENDED))
				|| user.getUserStatus().equals(UserStatusType.PENDING)) {
			responseObject.setResponseMessage(ErrorType.USER_INACTIVE.getResponseMessage());
			responseObject.setResponseCode(ErrorType.USER_INACTIVE.getResponseCode());

			status = false;
			failureReason = ErrorType.USER_INACTIVE.getInternalMessage();
			loginHistoryDao.saveLoginDetails(agent, status, user, ip, failureReason);
			return responseObject;
		}
		
		
		
		if(user.getUserType().equals(UserType.RESELLER) && user.getUserStatus().equals(UserStatusType.SUSPENDED)) {
			
			responseObject.setResponseMessage(ErrorType.RESELLER_INACTIVE.getResponseMessage());
			responseObject.setResponseCode(ErrorType.RESELLER_INACTIVE.getResponseCode());
			status = false;
			failureReason = ErrorType.RESELLER_INACTIVE.getInternalMessage();
			loginHistoryDao.saveLoginDetails(agent, status, user, ip, failureReason);
			return responseObject;
		}
		
		Integer failCount = 0;
		if (StringUtils.isNotBlank(user.getFailLoginCount())) {
			failCount = Integer.valueOf(user.getFailLoginCount());
			failCount = failCount + 1;
			
			if (failCount > 5) {
				responseObject.setResponseMessage(ErrorType.USER_ACCOUNT_LOCKED.getResponseMessage());
				responseObject.setResponseCode(ErrorType.USER_ACCOUNT_LOCKED.getResponseCode());
				return responseObject;
			}
		}
		
		pin = PasswordHasher.hashPin(pin, user.getPayId());
		String userDBPin = user.getPin();
		if (StringUtils.isEmpty(userDBPin)) {
			status = false;
			failureReason = ErrorType.USER_PIN_NOT_SET.getInternalMessage();
			responseObject.setResponseMessage(ErrorType.USER_PIN_NOT_SET.getResponseMessage());
			responseObject.setResponseCode(ErrorType.USER_PIN_NOT_SET.getResponseCode());
		} else if (userDBPin.equals(pin)) {
			status = true;
			responseObject.setResponseCode(ErrorType.SUCCESS.getResponseCode());
			user.setFailLoginCount("0");
			userDao.update(user);
		} else {
			status = false;
			failureReason = ErrorType.USER_PIN_INCORRECT.getInternalMessage();
			responseObject.setResponseMessage(ErrorType.USER_PIN_INCORRECT.getResponseMessage());
			responseObject.setResponseCode(ErrorType.USER_PIN_INCORRECT.getResponseCode());
			
			Integer failCountNow = 0;
			
			if (StringUtils.isNotBlank(user.getFailLoginCount())) {
				failCountNow = Integer.valueOf(user.getFailLoginCount());
				failCountNow = failCountNow + 1;
			}	
				user.setFailLoginCount(String.valueOf(failCountNow));
				userDao.update(user);
			
			
		}
		loginHistoryDao.saveLoginDetails(agent, status, user, ip, failureReason);
		return responseObject;
	}

	

	public ResponseObject authenticatePasswordOtp(String phoneNumber, String password,String otp ,String agent, String ip) throws SystemException {

		ResponseObject responseObject = new ResponseObject();
		boolean status;
		String failureReason = null;

		user = userDao.findUserByPhone(phoneNumber);

		if (null == user) {
			// If user is not found, userid is invalid
			responseObject.setResponseMessage(ErrorType.USER_NOT_FOUND.getResponseMessage());
			responseObject.setResponseCode(ErrorType.USER_NOT_FOUND.getResponseCode());
			return responseObject;
		}

		// Userid is valid
		if (!(user.getUserStatus().equals(UserStatusType.ACTIVE)
				|| user.getUserStatus().equals(UserStatusType.TRANSACTION_BLOCKED)
				|| user.getUserStatus().equals(UserStatusType.SUSPENDED))
				|| user.getUserStatus().equals(UserStatusType.PENDING)) {
			responseObject.setResponseMessage(ErrorType.USER_INACTIVE.getResponseMessage());
			responseObject.setResponseCode(ErrorType.USER_INACTIVE.getResponseCode());

			status = false;
			failureReason = ErrorType.USER_INACTIVE.getInternalMessage();
			loginHistoryDao.saveLoginDetails(agent, status, user, ip, failureReason);
			return responseObject;
		}
		
		
		
		
		Integer failCount = 0;
		if (StringUtils.isNotBlank(user.getFailLoginCount())) {
			failCount = Integer.valueOf(user.getFailLoginCount());
			failCount = failCount + 1;
			
			if (failCount > 5) {
				responseObject.setResponseMessage(ErrorType.USER_ACCOUNT_LOCKED.getResponseMessage());
				responseObject.setResponseCode(ErrorType.USER_ACCOUNT_LOCKED.getResponseCode());
				return responseObject;
			}
		}
		
		password = PasswordHasher.hashPin(password, user.getPayId());
		String userPassword = user.getPassword();
		if (StringUtils.isEmpty(userPassword)) {
			status = false;
			failureReason = ErrorType.USER_PASSWORD_NOT_SET.getInternalMessage();
			responseObject.setResponseMessage(ErrorType.USER_PASSWORD_NOT_SET.getResponseMessage());
			responseObject.setResponseCode(ErrorType.USER_PASSWORD_NOT_SET.getResponseCode());
		} else if (userPassword.equals(password)) {
			status = true;
			responseObject.setResponseCode(ErrorType.SUCCESS.getResponseCode());
			user.setFailLoginCount("0");
			userDao.update(user);
		} else {
			status = false;
			failureReason = ErrorType.USER_PIN_INCORRECT.getInternalMessage();
			responseObject.setResponseMessage(ErrorType.USER_PIN_INCORRECT.getResponseMessage());
			responseObject.setResponseCode(ErrorType.USER_PIN_INCORRECT.getResponseCode());
			
			Integer failCountNow = 0;
			
			if (StringUtils.isNotBlank(user.getFailLoginCount())) {
				failCountNow = Integer.valueOf(user.getFailLoginCount());
				failCountNow = failCountNow + 1;
			}	
				user.setFailLoginCount(String.valueOf(failCountNow));
				userDao.update(user);
			
				loginHistoryDao.saveLoginDetails(agent, status, user, ip, failureReason);
				return responseObject;
		}
		
		try {
			LoginOtp loginOtp = otpDao.checkExpireOtp(otp, phoneNumber);
			if (loginOtp != null) {
				DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
				Calendar calobj = Calendar.getInstance();
				String startTime = df.format(calobj.getTime());
				String expiryTime = loginOtp.getExpiryDate();
				Date d1 = null;
				Date d2 = null;
				try {
					d1 = df.parse(startTime);
					d2 = df.parse(expiryTime);
				} catch (Exception e) {
					logger.error("Exception" , e);
				}
				long diff = d2.getTime() - d1.getTime();
				long diffMinutes = diff / (60 * 1000) % 60;
				long diffHours = diff / (60 * 60 * 1000);
				if (diffHours >= 0 && diffMinutes >= 0) {
					status = true;
					responseObject.setResponseCode(ErrorType.SUCCESS.getResponseCode());
					responseObject.setResponseMessage(ErrorType.SUCCESS.getResponseMessage());
					loginOtp.setStatus("InActive");
					otpDao.update(loginOtp);
					user.setFailLoginCount("0");
					userDao.update(user);
				} else {
					loginOtp.setStatus("InActive");
					otpDao.update(loginOtp);
					status = false;
					responseObject.setResponseMessage(ErrorType.EXPIRED_OTP.getResponseMessage());
					responseObject.setResponseCode(ErrorType.EXPIRED_OTP.getResponseCode());
				}
			} else {
				status = false;
				failureReason = ErrorType.OTP_NOT_SET.getInternalMessage();

				responseObject.setResponseMessage(ErrorType.INACTIVE_OTP.getResponseMessage());
				responseObject.setResponseCode(ErrorType.INACTIVE_OTP.getResponseCode());
			}
		} catch (Exception ex) {
			status = false;
			failureReason = ErrorType.OTP_NOT_SET.getInternalMessage();
			responseObject.setResponseMessage(ErrorType.INACTIVE_OTP.getResponseMessage());
			responseObject.setResponseCode(ErrorType.INACTIVE_OTP.getResponseCode());
		}
		
		loginHistoryDao.saveLoginDetails(agent, status, user, ip, failureReason);
		return responseObject;
	}
	
	
	public ResponseObject authenticateOtp(String otp, String phoneNumber, String agent, String ip) {
		ResponseObject responseObject = new ResponseObject();
		boolean status;
		String failureReason = null;
		user = userDao.findUserByPhone(phoneNumber);
		
		if (null == user) {
			// If user is not found, userid is invalid
			responseObject.setResponseMessage(ErrorType.USER_NOT_FOUND.getResponseMessage());
			responseObject.setResponseCode(ErrorType.USER_NOT_FOUND.getResponseCode());
			return responseObject;
		}
		// Userid is valid
		if (!(user.getUserStatus().equals(UserStatusType.ACTIVE)
				|| user.getUserStatus().equals(UserStatusType.TRANSACTION_BLOCKED)
				|| user.getUserStatus().equals(UserStatusType.SUSPENDED))
				|| user.getUserStatus().equals(UserStatusType.PENDING)) {
			responseObject.setResponseMessage(ErrorType.USER_INACTIVE.getResponseMessage());
			responseObject.setResponseCode(ErrorType.USER_INACTIVE.getResponseCode());

			status = false;
			failureReason = ErrorType.USER_INACTIVE.getInternalMessage();
			loginHistoryDao.saveLoginDetails(agent, status, user, ip, failureReason);
			return responseObject;
		}
		
		try {
			LoginOtp loginOtp = otpDao.checkExpireOtp(otp, phoneNumber);
			if (loginOtp != null) {
				DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
				Calendar calobj = Calendar.getInstance();
				String startTime = df.format(calobj.getTime());
				String expiryTime = loginOtp.getExpiryDate();
				Date d1 = null;
				Date d2 = null;
				try {
					d1 = df.parse(startTime);
					d2 = df.parse(expiryTime);
				} catch (Exception e) {
					logger.error("Exception" , e);
				}
				long diff = d2.getTime() - d1.getTime();
				long diffMinutes = diff / (60 * 1000) % 60;
				long diffHours = diff / (60 * 60 * 1000);
				if (diffHours >= 0 && diffMinutes >= 0) {
					status = true;
					responseObject.setResponseCode(ErrorType.SUCCESS.getResponseCode());
					responseObject.setResponseMessage(ErrorType.SUCCESS.getResponseMessage());
					loginOtp.setStatus("InActive");
					otpDao.update(loginOtp);
					user.setFailLoginCount("0");
					userDao.update(user);
				} else {
					loginOtp.setStatus("InActive");
					otpDao.update(loginOtp);
					status = false;
					responseObject.setResponseMessage(ErrorType.EXPIRED_OTP.getResponseMessage());
					responseObject.setResponseCode(ErrorType.EXPIRED_OTP.getResponseCode());
				}
			} else {
				status = false;
				failureReason = ErrorType.OTP_NOT_SET.getInternalMessage();

				responseObject.setResponseMessage(ErrorType.INACTIVE_OTP.getResponseMessage());
				responseObject.setResponseCode(ErrorType.INACTIVE_OTP.getResponseCode());
			}
		} catch (Exception ex) {
			status = false;
			failureReason = ErrorType.OTP_NOT_SET.getInternalMessage();
			responseObject.setResponseMessage(ErrorType.INACTIVE_OTP.getResponseMessage());
			responseObject.setResponseCode(ErrorType.INACTIVE_OTP.getResponseCode());
		}
		loginHistoryDao.saveLoginDetails(agent, status, user, ip, failureReason);
		return responseObject;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
