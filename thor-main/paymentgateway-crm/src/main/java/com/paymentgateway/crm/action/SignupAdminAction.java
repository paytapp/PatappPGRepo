package com.paymentgateway.crm.action;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


import com.paymentgateway.commons.email.EmailServiceProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.crm.actionBeans.CreateNewAdmin;

/**
 * @ Neeraj
 */

public class SignupAdminAction extends AbstractSecureAction {

	@Autowired
	private EmailServiceProvider emailServiceProvider;
	
	@Autowired
	private CrmValidator validator;
	
	@Autowired
	CreateNewAdmin createUser;
	
	private static final long serialVersionUID = 908867511652353218L;
	private static Logger logger = LoggerFactory.getLogger(SignupAdminAction.class.getName());
	private String emailId;
	private String password;
	private String businessName;
	private String mobile;
	private String confirmPassword;

	public String execute() {
		
		ResponseObject responseObject = new ResponseObject();
		try {
			responseObject = createUser.createUser(getUserInstance(), UserType.ADMIN);
			if (!ErrorType.SUCCESS.getResponseCode().equals(responseObject.getResponseCode())) {
				addActionMessage(responseObject.getResponseMessage());

			}
			if (ErrorType.SUCCESS.getResponseCode().equals(responseObject.getResponseCode())) {
				addActionMessage("User successfully registered.");
			}
			// Sending Email for Email Validation
			emailServiceProvider.emailValidator(responseObject);
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}
	private User getUserInstance() {
		
		try{
			
			User user = new User();
			user.setEmailId(getEmailId());
			user.setPassword(getPassword());
			user.setMobile(getMobile());
			user.setBusinessName(getBusinessName());
			return user;
		}
		
		catch(Exception e){
			logger.error("Signup admin failed " , e);
			return null;
		}
	
	}

	public void validate() {

		// Validate blank and invalid fields
		if (validator.validateBlankField(getPassword())) {
			addFieldError(CrmFieldType.PASSWORD.getName(), validator.getResonseObject().getResponseMessage());
			if (validator.validateBlankField(getConfirmPassword())) {
				addFieldError(CrmFieldConstants.CONFIRM_PASSWORD.getValue(),
						validator.getResonseObject().getResponseMessage());
			}
		} else if (validator.validateBlankField(getConfirmPassword())) {
			addFieldError(CrmFieldConstants.CONFIRM_PASSWORD.getValue(),
					validator.getResonseObject().getResponseMessage());
		} else if (!(getPassword().equals(getConfirmPassword()))) {
			addFieldError(CrmFieldType.PASSWORD.getName(), ErrorType.PASSWORD_MISMATCH.getResponseMessage());
		} else if (!(validator.isValidPasword(getPassword()))) {
			addFieldError(CrmFieldType.PASSWORD.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}

		if ((validator.validateBlankField(getBusinessName()))) {
			addFieldError(CrmFieldType.BUSINESS_NAME.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.BUSINESS_NAME, getBusinessName()))) {
			addFieldError(CrmFieldType.BUSINESS_NAME.getName(), validator.getResonseObject().getResponseMessage());
		}

		if (validator.validateBlankField(getMobile())) {
			addFieldError(CrmFieldType.MOBILE.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.MOBILE, getMobile()))) {
			addFieldError(CrmFieldType.MOBILE.getName(), validator.getResonseObject().getResponseMessage());
		}

		if (validator.validateBlankField(getEmailId())) {
			addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.isValidEmailId(getEmailId()))) {
			addFieldError(CrmFieldType.EMAILID.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}
}
