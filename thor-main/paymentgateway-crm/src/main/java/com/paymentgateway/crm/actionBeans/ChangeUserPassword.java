package com.paymentgateway.crm.actionBeans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.email.EmailServiceProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserRecordsDao;

/**
 * @author ISHA
 *
 */
@Service
public class ChangeUserPassword {
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private UserRecordsDao userRecordsDao;
	
	@Autowired
	private EmailServiceProvider emailServiceProvider;
	
	@Autowired
	private CheckOldPassword checkOldPassword;
		
	/*public ResponseObject changePassword(String emailId, String oldPassword, String newPassword) throws Exception{
		User user = new User();
		ResponseObject responseObject = new ResponseObject();
		
		user = userDao.findUserByPhone(emailId);
		
		oldPassword = (PasswordHasher.hashPassword(oldPassword, user.getPayId()));
		newPassword = (PasswordHasher.hashPassword(newPassword,user.getPayId()));
		if (!(oldPassword.equals(user.getPassword()))){
			responseObject.setResponseCode(ErrorType.PASSWORD_MISMATCH.getResponseCode());
			return responseObject;
		}else if(newPassword.equals(oldPassword)){                                           // Match if new and old password is same and password is correct
			responseObject.setResponseCode(ErrorType.OLD_PASSWORD_MATCH.getResponseCode());
			return responseObject;
		}
		
		
		if(checkOldPassword.isUsedPassword(newPassword, user.getEmailId())) {
			responseObject.setResponseCode(ErrorType.OLD_PASSWORD_MATCH.getResponseCode());
			return responseObject;
		}				
				
		userRecordsDao.createDetails(emailId, oldPassword, user.getPayId());
		user.setPassword(newPassword);
		userDao.update(user);
		responseObject.setResponseCode(ErrorType.PASSWORD_CHANGED.getResponseCode());
		// Sending Email for CRM password change notification
		emailControllerServiceProvider.passwordChange(responseObject,emailId);
		
		return responseObject;
	}*/
	
	public ResponseObject changePin(String phoneNumber, String oldPin, String newPin) throws Exception{
		User user = new User();
		ResponseObject responseObject = new ResponseObject();
		
		user = userDao.findUserByPhone(phoneNumber);
		
		oldPin = (PasswordHasher.hashPassword(oldPin, user.getPayId()));
		newPin = (PasswordHasher.hashPassword(newPin,user.getPayId()));
		if (!(oldPin.equals(user.getPin()))){
			responseObject.setResponseCode(ErrorType.PIN_MISMATCH.getResponseCode());
			return responseObject;
		}else if(newPin.equals(oldPin)){                                           // Match if new and old pin is same and pin is correct
			responseObject.setResponseCode(ErrorType.OLD_NEW_PIN_MATCH.getResponseCode());
			return responseObject;
		}		
		if(checkOldPassword.isUsedPin(newPin, user.getMobile())) {
			responseObject.setResponseCode(ErrorType.OLD_NEW_PIN_MATCH.getResponseCode());
			return responseObject;
		}				
		userRecordsDao.createDetails(phoneNumber, oldPin, user.getPayId());
		user.setPin(newPin);
		userDao.update(user);
		responseObject.setResponseCode(ErrorType.PIN_CHANGED.getResponseCode());
		// Sending Email for CRM password change notification
		emailServiceProvider.pinChange(responseObject,phoneNumber);
		return responseObject;
	}
}