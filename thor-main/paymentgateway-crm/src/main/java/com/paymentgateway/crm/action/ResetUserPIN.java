package com.paymentgateway.crm.action;

import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.SaltFileManager;

public class ResetUserPIN extends AbstractSecureAction {

	private static final long serialVersionUID = -5956533558995482980L;
	private static Logger logger = LoggerFactory.getLogger(ResetUserPIN.class.getName());

	@Autowired
	private SaltFileManager saltFileManager;

	@Autowired
	private CrmValidator validator;

	@Autowired
	private UserDao userDao;

	private String mobile;
	private String payId;
	private String response;
	private String responseMessage;
	private List<User> aaData;

	private User sessionUser = new User();

	public String execute() {
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.ADMIN)) {
				logger.info("fetching user to reset pin where mobile number is " + getMobile());
				setAaData(userDao.getUserProfileByMobile(getMobile()));
			}
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	public String resetUserPIN() {
		logger.info("inside resetUserPIN()");
		try {
			String hashedPin;
			String pin;
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.ADMIN)) {

				String salt = saltFileManager.getSalt(payId);
				Random random = new Random();
				pin = String.format("%06d", random.nextInt(999999));
				if (StringUtils.isNotBlank(payId) && StringUtils.isNotBlank(pin)) {
					if (StringUtils.isNotBlank(salt) && pin.length() == 6) {
						hashedPin = Hasher.getHash(pin.concat(salt));
						String failedLoginCount = "0";
						userDao.resetUserPIN(hashedPin, failedLoginCount, sessionUser.getBusinessName(), getMobile(),
								getPayId());
						logger.info("user pin updated for PayId/Mobile: [" +getPayId()+"/"+getMobile()+"] hashedPIN: " + hashedPin );
						setResponse("success");
						setResponseMessage("User pin updated successfully. PIN: " + pin);
					}
				} else {
					logger.info("Failed to reset PIN, Error: Blank payId or pin");
					setResponse("fail");
					setResponseMessage("Failed to reset PIN.");
				}
			}

			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			setResponse("fail");
			setResponseMessage("Failed to reset PIN.");
			return ERROR;
		}
	}

	public void validate() {

		if (validator.validateBlankField(getMobile())) {
		} else if (!validator.validateField(CrmFieldType.MOBILE, getMobile())) {
			addFieldError(CrmFieldType.MOBILE.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public List<User> getAaData() {
		return aaData;
	}

	public void setAaData(List<User> aaData) {
		this.aaData = aaData;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

}
