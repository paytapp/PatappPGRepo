package com.paymentgateway.crm.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.crm.actionBeans.ChangeUserPassword;
import com.paymentgateway.crm.actionBeans.PasswordHasher;

/**
 * @author ISHA
 *
 */
public class ChangePasswordAction extends AbstractSecureAction {

	@Autowired
	private CrmValidator validator;

	@Autowired
	private ChangeUserPassword changeUserPassword;

	@Autowired
	private UserDao userDao;

	private static Logger logger = LoggerFactory.getLogger(ChangePasswordAction.class.getName());
	private static final long serialVersionUID = -6122140077608015851L;
	private String oldPin;
	private String newPin;
	private String confirmnewPin;
	private String response;

	public String execute() {
		ResponseObject responseObject = new ResponseObject();
		try {
			User sessionUser = (User) (sessionMap.get(Constants.USER.getValue()));
			responseObject = changeUserPassword.changePin(sessionUser.getMobile(), oldPin, newPin);
			if (responseObject.getResponseCode().equals(ErrorType.PIN_MISMATCH.getResponseCode())) {
				addFieldError(CrmFieldConstants.OLD_PIN.getValue(), ErrorType.PIN_MISMATCH.getResponseMessage());
				setResponse(ErrorType.PIN_MISMATCH.getResponseMessage());
				return SUCCESS;
			} else if (responseObject.getResponseCode().equals(ErrorType.OLD_NEW_PIN_MATCH.getResponseCode())) {
				addFieldError(CrmFieldConstants.OLD_PIN.getValue(), ErrorType.OLD_NEW_PIN_MATCH.getResponseMessage());
				setResponse(ErrorType.OLD_NEW_PIN_MATCH.getResponseMessage());
				return SUCCESS;
			}
			setResponse(ErrorType.PIN_RESET.getResponseMessage());
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			setResponse("error");
			return ERROR;
		}
	}
	
	public String validateOldPin() {
		try {
			User sessionUser = (User) (sessionMap.get(Constants.USER.getValue()));
			logger.info("Validating old PIN of " + sessionUser.getUserType() + " with email ID "
					+ sessionUser.getEmailId());
			User user = userDao.findUserByPhone(sessionUser.getMobile());
			oldPin = (PasswordHasher.hashPassword(oldPin, user.getPayId()));
			if (!(oldPin.equals(user.getPin()))) {
				setResponse(ErrorType.PIN_MISMATCH.getResponseMessage());
				return SUCCESS;
			} else {
				setResponse(ErrorType.SUCCESS.getResponseMessage());
				return SUCCESS;
			}
		} catch (Exception e) {
			logger.error("Exception", e);
			return SUCCESS;
		}
	}

	public void validate() {
		if (!(validator.validateBlankField(getOldPin()))) {
			if (!(validator.isValidPin(getOldPin()))) {
				setResponse(ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
				addFieldError(CrmFieldConstants.OLD_PIN.getValue(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
			}
		}
		if (!(validator.validateBlankField(getNewPin()))) {
			if (!(validator.isValidPin(getNewPin()))) {
				setResponse(ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
				addFieldError(CrmFieldConstants.NEW_PIN.getValue(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
			}
		}
		if (!(validator.validateBlankField(getConfirmnewPin()))) {
			if (!(getNewPin().equals(getConfirmnewPin()))) {
				setResponse(ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
				addFieldError(CrmFieldConstants.CONFIRM_NEW_PIN.getValue(),
						ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
			}
		}
	}

	public String getOldPin() {
		return oldPin;
	}

	public void setOldPin(String oldPin) {
		this.oldPin = oldPin;
	}

	public String getNewPin() {
		return newPin;
	}

	public void setNewPin(String newPin) {
		this.newPin = newPin;
	}

	public String getConfirmnewPin() {
		return confirmnewPin;
	}

	public void setConfirmnewPin(String confirmnewPin) {
		this.confirmnewPin = confirmnewPin;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

}
