package com.paymentgateway.crm.action;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.dispatcher.SessionMap;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.email.EmailServiceProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserRecordsDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.UserStatusType;
import com.paymentgateway.crm.actionBeans.CheckOldPassword;

/**
 * @author Neeraj
 */
public class ForgetPasswordAction extends AbstractSecureAction implements ServletRequestAware {

	private static final int emailExpiredInMinute = ConfigurationConstants.EMAIL_EXPIRED_MINUTE.getValues();

	@Autowired
	private UserDao userDao;

	@Autowired
	private CrmValidator validator;

	@Autowired
	private UserRecordsDao userRecordsDao;

	@Autowired
	private CheckOldPassword checkOldPassword;

	private static Logger logger = LoggerFactory.getLogger(ForgetPasswordAction.class.getName());
	private static final long serialVersionUID = 4184065113906121002L;
	@Autowired
	private EmailServiceProvider emailServiceProvider;

	private String phoneNumber;
	private String id;
	private String payId;
	private String newPin;
	private String confirmNewPin;
	private String response;
	private String errorMessage;
	private String errorCode;

	private String emailId;

	private User user = new User();

	@Override
	public void setServletRequest(HttpServletRequest request) {
		// TODO Auto-generated method stub

	}

	@SkipValidation
	public String execute() {
		try {
			if (!validatePhone()) {
				String accountValidationKey = TransactionManager.getNewTransactionId();
				user = userDao.findPayIdByPhoneNumber(getPhoneNumber());
				if (user != null) {
					if ((user.getUserStatus().toString()).equals(UserStatusType.ACTIVE.toString())) {
						String payId = user.getPayId();
						if (!StringUtils.isEmpty(payId)) {
							Date currnetDate = new Date();
							Calendar c = Calendar.getInstance();
							c.setTime(currnetDate);
							c.add(Calendar.MINUTE, emailExpiredInMinute);
							currnetDate = c.getTime();
							// Sending Email for Email Validation
							emailServiceProvider.pinReset(accountValidationKey, getPhoneNumber());
							userDao.updateAccountValidationKey(accountValidationKey, payId);
							// userDao.enterEmailExpiryTime(currnetDate, payId);
							setResponse(ErrorType.RESET_LINK_SENT.getResponseMessage());
						} else {
							setResponse(ErrorType.INVALID_PHONE_NUMBER.getResponseMessage());
							setErrorMessage(ErrorType.INVALID_PHONE_NUMBER.getResponseMessage());
						}
					} else {
						setResponse(ErrorType.VERIFY_PHONE_NUMBER.getResponseMessage());
						setErrorMessage(ErrorType.VERIFY_PHONE_NUMBER.getResponseMessage());
					}
				} else {
					setResponse(ErrorType.INVALID_PHONE_NUMBER.getResponseMessage());
					setErrorMessage(ErrorType.INVALID_PHONE_NUMBER.getResponseMessage());
				}
			} else {
				setResponse(getErrorMessage());
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return SUCCESS;
		}
		return SUCCESS;
	}

	@SkipValidation
	public String resetPin() {
		try {
			user = userDao.findByAccountValidationKey(getId());
			if (user == null) {
				return ERROR;
			}
			if (user.isPhoneValidationFlag()) {
				addActionMessage(ErrorType.ALREADY_PIN_RESET.getResponseMessage());
				return "reset";
			}
			if (StringUtils.isNotBlank(getId())) {
				sessionMap.put("payId", user.getPayId());
			}
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	public String resetUserPin() {
		user = userDao.findPayId(payId);
		if (user == null) {
			setResponse("User doesn't exist");
			setErrorCode(ErrorType.USER_NOT_FOUND.getCode());
			return SUCCESS;
		}
		try {
			if (!validateFields(user)) {
				logger.info("validation success");
				userRecordsDao.createDetails(user.getMobile(), user.getPin(), user.getPayId());
				String salt = (new PropertiesManager()).getSalt(user.getPayId());
				if (null == salt) {
					throw new SystemException(ErrorType.AUTHENTICATION_UNAVAILABLE,
							ErrorType.AUTHENTICATION_UNAVAILABLE.getResponseCode());
				}
				String hashedPin = Hasher.getHash(getNewPin().concat(salt));
				user.setPin(hashedPin);
				user.setPhoneValidationFlag(true);
				if (user.getUserType().equals(UserType.SUBUSER) || user.getUserType().equals(UserType.SUBACQUIRER)
						|| user.getUserType().equals(UserType.SUBADMIN)) {
					user.setUserStatus(UserStatusType.ACTIVE);
				}
				userDao.update(user);
				addActionMessage(ErrorType.PIN_RESET.getResponseMessage());
				setResponse(ErrorType.PIN_RESET.getResponseMessage());
				setErrorCode(ErrorType.PIN_RESET.getCode());
			} else {
				setResponse(getErrorMessage());
			}

			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return SUCCESS;
		}
	}

	public boolean validatePhone() {
		if (validator.validateBlankField(getPhoneNumber())) {
			setErrorMessage(validator.getResonseObject().getResponseMessage());
			return true;
		} else if (!(validator.isValidPhoneNumber(getPhoneNumber()))) {
			setErrorMessage(ErrorType.INVALID_PHONE_NUMBER.getResponseMessage());
			return true;
		}
		return false;
	}

	public boolean validateFields(User user) throws SystemException {
		if (validator.validateBlankField(getNewPin())) {
			setErrorMessage(ErrorType.NEW_PIN.getResponseMessage());
			return true;
		} else if (validator.validateBlankField(getConfirmNewPin())) {
			setErrorMessage(ErrorType.CONFIRM_NEW_PIN.getResponseMessage());
			return true;
		} else if (!(getNewPin().equals(getConfirmNewPin()))) {
			setErrorMessage(ErrorType.PIN_MISMATCH.getResponseMessage());
			return true;
		} else if (!(validator.isValidPin(getNewPin()))) {
			setErrorMessage(ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
			return true;
		}
		if (StringUtils.isNotBlank(user.getPin())) {
			if (newPin.equals(user.getPin())) {
				setErrorMessage(ErrorType.OLD_NEW_PIN_MATCH.getResponseMessage());
				return true;
			}
		}

		if (checkOldPassword.isUsedPin(newPin, user.getMobile())) {
			setErrorMessage(ErrorType.OLD_NEW_PIN_MATCH.getResponseMessage());
			return true;
		}
		return false;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getNewPin() {
		return newPin;
	}

	public void setNewPin(String newPin) {
		this.newPin = newPin;
	}

	public String getConfirmNewPin() {
		return confirmNewPin;
	}

	public void setConfirmNewPin(String confirmNewPin) {
		this.confirmNewPin = confirmNewPin;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
}
