package com.paymentgateway.crm.action;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.ForgetPin;
import com.paymentgateway.commons.user.LoginOtpDao;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;

public class ValidateOtpAction extends AbstractSecureAction {

	private static Logger logger = LoggerFactory.getLogger(ValidateOtpAction.class.getName());
	private static final long serialVersionUID = 622840002482852289L;

	@Autowired
	private CrmValidator validator;

	@Autowired
	private LoginOtpDao otpDao;

	private String response;
	private String phoneNumber;

	private String otp;

	@Override
	public String execute() {
		ResponseObject responseObject = new ResponseObject();
		try {
			if (sessionMap.containsKey("MOBILE_NUMBER") && sessionMap.containsKey("STATUS_CODE")
					&& sessionMap.containsKey("payId")) {
				if (((String) sessionMap.get("MOBILE_NUMBER")).equalsIgnoreCase(phoneNumber)
						&& ((String) sessionMap.get("STATUS_CODE")).equalsIgnoreCase("000")) {
					sessionMap.remove("STATUS_CODE");
					sessionMap.remove("MOBILE_NUMBER");
					return SUCCESS;
				}
			}
			logger.info("Inside validate otp action !!");
			User userModel = otpDao.getUserData(phoneNumber);
			try {
				ForgetPin loginOtp = otpDao.checkExpirePasswordOtp(otp, phoneNumber);
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
						e.printStackTrace();
					}
					long diff = d2.getTime() - d1.getTime();
					long diffMinutes = diff / (60 * 1000) % 60;
					long diffHours = diff / (60 * 60 * 1000);
					if (diffHours >= 0 && diffMinutes >= 0) {
						addFieldError(CrmFieldType.MOBILE.getName(), responseObject.getResponseMessage());
						setResponse("success");
						loginOtp.setStatus("InActive");
						otpDao.updatePin(loginOtp);
						sessionMap.put("payId", userModel.getPayId());
						sessionMap.put("STATUS_CODE", "000");
						sessionMap.put("MOBILE_NUMBER", phoneNumber);
						return SUCCESS;
					} else {
						loginOtp.setStatus("InActive");
						otpDao.updatePin(loginOtp);
						setResponse("OTP has been expired !");
						addFieldError(CrmFieldType.MOBILE.getName(), "OTP has been expired !");
						return INPUT;
					}
				} else {
					addFieldError(CrmFieldType.MOBILE.getName(), "Invalid OTP");
					setResponse("Invalid OTP");
					return INPUT;
				}
			} catch (Exception ex) {
				addFieldError(CrmFieldType.MOBILE.getName(), "Invalid OTP");
				setResponse("Invalid OTP");
				return INPUT;
			}
		} catch (Exception e) {
			setResponse("Invalid User");
			logger.info("Exception : "+e);
			return INPUT;
		}
	}

	public void validate() {

		if ((validator.validateBlankFields(getPhoneNumber()))) {
			addFieldError(CrmFieldType.MOBILE.getName(), validator.getResonseObject().getResponseMessage());
			return;
		}

		if (validator.validateBlankFields(getOtp())) {
			addFieldError(CrmFieldType.OTP.getName(), validator.getResonseObject().getResponseMessage());
			return;
		}

	}

	public LoginOtpDao getOtpDao() {
		return otpDao;
	}

	public void setOtpDao(LoginOtpDao otpDao) {
		this.otpDao = otpDao;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

}
