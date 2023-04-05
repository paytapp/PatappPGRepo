package com.paymentgateway.crm.action;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.UserStatusType;

public class ChangeAdminSetUpAction extends AbstractSecureAction {

	private static final long serialVersionUID = 3702199787764289745L;
	private static Logger logger = LoggerFactory.getLogger(ChangeAdminSetUpAction.class.getName());

	private String emailId;
	private String adminEmailId;
	private String response;

	public String changeAdmin() {
		UserDao userDao = new UserDao();
		User user = userDao.find(adminEmailId);
		if(user == null){
			setResponse("User doesn't exist!");
			logger.error(response + "with" + adminEmailId);
			return SUCCESS;
		}
		if (user.getUserType().equals(UserType.ADMIN)) {
			UserStatusType userStatus = user.getUserStatus();
			if ((userStatus.equals(UserStatusType.ACTIVE))) {
				String payId = user.getPayId();
				User subAdmin = userDao.find(emailId);
				subAdmin.setParentPayId(payId);
				userDao.update(subAdmin);
				setResponse("Admin Changed Successfully");
				logger.error(response + "for " + adminEmailId);
			} else {
				setResponse("User Inactive");
				logger.error(adminEmailId + response);
			}
		} else {
			setResponse("User doesn't exist!");
			logger.error(response + "with" + adminEmailId);

		}
		return SUCCESS;

	}

	public void validate() {
		CrmValidator validator = new CrmValidator();
		if (validator.validateBlankField(getEmailId())) {
			addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.isValidEmailId(getEmailId()))) {
			addFieldError(CrmFieldType.EMAILID.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getAdminEmailId())) {
			addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.isValidEmailId(getAdminEmailId()))) {
			addFieldError(CrmFieldType.EMAILID.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}

	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getAdminEmailId() {
		return adminEmailId;
	}

	public void setAdminEmailId(String adminEmailId) {
		this.adminEmailId = adminEmailId;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

}
