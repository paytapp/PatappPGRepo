package com.paymentgateway.crm.actionBeans;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;

/**
 * @author Puneet
 * 
 */

@Service
public class CheckExistingUser {

	private User checkedUser = new User();
	ResponseObject responseObject;

	public ResponseObject checkuser(String emailId) {
		responseObject = new ResponseObject();
		checkedUser = new UserDao().find(emailId);
		if (null != checkedUser) {
			responseObject.setResponseCode(ErrorType.USER_UNAVAILABLE.getResponseCode());
			responseObject.setResponseMessage(ErrorType.USER_UNAVAILABLE.getResponseMessage());
		} else {

			responseObject.setResponseCode(ErrorType.USER_AVAILABLE.getResponseCode());
			responseObject.setResponseMessage(ErrorType.USER_AVAILABLE.getResponseMessage());
		}
		return responseObject;
	}

	public ResponseObject checkPhoneUser(String phoneNumber) {
		responseObject = new ResponseObject();
		checkedUser = new UserDao().findUserByPhone(phoneNumber);
		if (null != checkedUser) {
			responseObject.setResponseCode(ErrorType.USER_PHONE_UNAVAILABLE.getResponseCode());
			responseObject.setResponseMessage(ErrorType.USER_PHONE_UNAVAILABLE.getResponseMessage());

		} else {
			responseObject = null;
		}
		return responseObject;
	}

	public boolean checkExistingMobileUser(String phoneNumber) {
		try {
			checkedUser = new UserDao().findUserByPhone(phoneNumber);
			if (null == checkedUser) {
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public ResponseObject checkPhoneUserLogin(String phoneNumber) {
		responseObject = new ResponseObject();
		checkedUser = new UserDao().findUserByPhone(phoneNumber);
		if (null != checkedUser) {
			responseObject.setResponseCode(ErrorType.USER_PHONE_UNAVAILABLE.getResponseCode());
			responseObject.setResponseMessage(ErrorType.USER_PHONE_UNAVAILABLE.getResponseMessage());
			responseObject.setUserType(checkedUser.getUserType().toString());

		} else {
			responseObject.setResponseCode(ErrorType.USER_PHONE_NUMBER_UNAVAILABLE.getResponseCode());
			responseObject.setResponseMessage(ErrorType.USER_PHONE_NUMBER_UNAVAILABLE.getResponseMessage());
		}
		return responseObject;
	}
	
	public ResponseObject checkuserByFirstNameAndLastNameAndBusinessName(String firstName ,String lastName ,String businessName) {
		responseObject = new ResponseObject();
		boolean resultQuery = new UserDao().checkAcquirer(firstName,lastName,businessName);
		if (resultQuery) {
			responseObject.setResponseCode(ErrorType.USER_ALREADY_EXISTS.getResponseCode());
			responseObject.setResponseMessage(ErrorType.USER_ALREADY_EXISTS.getResponseMessage());
		} else {
			responseObject.setResponseCode(ErrorType.USER_AVAILABLE.getResponseCode());
			responseObject.setResponseMessage(ErrorType.USER_AVAILABLE.getResponseMessage());
		}
		return responseObject;
	}	
}
