package com.paymentgateway.crm.action;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.SmsSender;
import com.paymentgateway.commons.user.LoginOtp;
import com.paymentgateway.commons.user.LoginOtpDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.PropertiesManager;

public class LoginOtpAction extends AbstractSecureAction {

	@Autowired
	private CrmValidator validator;

	@Autowired
	private SmsSender smsSender;

	@Autowired
	private LoginOtpDao otpDao;

	private static Logger logger = LoggerFactory.getLogger(LoginOtpAction.class.getName());
	private static final long serialVersionUID = -1271576830462438772L;

//	private String emailId;
	private String phoneNumber;
	private String response;
	private String otpSuccessStatus;

	@Override
	public String execute() {
		logger.info("inside LoginOtpAction");
		try {
			LoginOtp otp = new LoginOtp();
			DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
			Calendar calobj = Calendar.getInstance();
			Calendar calobj1 = Calendar.getInstance();
			String currentDate = df.format(calobj1.getTime());
			calobj.add(Calendar.MINUTE, 15);
			String expDate = df.format(calobj.getTime());
			User userModel = otpDao.getUserData(phoneNumber);
			int num = (int) ((Math.random() * 99999) + 100000);
			String val = Integer.toString(num);
			try {
				if (StringUtils.isBlank(userModel.getMobile()) || (userModel.getMobile().length() > 10)) {
					setResponse("Invalid Mobile Number");
					return INPUT;
				}
				otpDao.checkOtp(userModel.getMobile(), currentDate);
					otp.setPayId(userModel.getPayId());
					otp.setMobileNo(userModel.getMobile());
					otp.setOtp(val);
					otp.setMessage("Payment Gateway team");
					otp.setExpiryDate(expDate);
					otp.setStatus("Active");
					// otp.setEmailId(emailId);
					otp.setMobileNo(phoneNumber);
					otp.setCreateDate(currentDate);
					// otp.setRequestedBy(emailId);
					otp.setRequestedBy(phoneNumber);
					otpDao.create(otp);
					String otpMessage = maskString(userModel.getMobile(), 2, 6, '*');
					String message = "Your OTP for Daddytech is "+ val;
					message += ". \nPlease do not share your OTP with anyone. \n Thanks, \nTeam Payment Gateway";
					
					String smsInnuvisolutions = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
					String responseMsg;
					if(StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {
						 responseMsg = smsSender.sendSMSByInnvisSolution(userModel.getMobile(), message);
					}else {
					     responseMsg = smsSender.sendSMS(userModel.getMobile(), message);
					}
					if (responseMsg == null) {
						setResponse("Unable to send OTP");
						setOtpSuccessStatus("error");
						return INPUT;

					}
					setResponse("OTP has been sent to " + otpMessage);
					setOtpSuccessStatus("success");
					return SUCCESS;
				//}
			} catch (Exception ex) {
				otp.setPayId(userModel.getPayId());
				otp.setMobileNo(userModel.getMobile());
				otp.setOtp(val);
				otp.setMessage("Payment Gateway team");
				otp.setExpiryDate(expDate);
				otp.setStatus("Active");
				// otp.setEmailId(emailId);
				otp.setMobileNo(phoneNumber);
				otp.setCreateDate(currentDate);
				otp.setUpdateDate(currentDate);
				otp.setRequestedBy(phoneNumber);
				otpDao.create(otp);
				String otpMessage = maskString(userModel.getMobile(), 2, 6, '*');
				String message = "Your OTP for Daddytech is " + val;
				message += ". \nPlease do not share your OTP with anyone. \n Thanks, \nTeam Payment Gateway";
				
				String smsInnuvisolutions = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
				String responseMsg;
				if(StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {
					 responseMsg = smsSender.sendSMSByInnvisSolution(userModel.getMobile(), message);
				}else {
				     responseMsg = smsSender.sendSMS(userModel.getMobile(), message);
				}
				
				//String response = smsSender.sendSMS(userModel.getMobile(), message);
				if (responseMsg == null)
				{
					setResponse("Unable to send OTP");
					setOtpSuccessStatus("error");
					return INPUT;
				}
				setResponse("OTP has been sent to " + otpMessage);
				setOtpSuccessStatus("success");
				return SUCCESS;
			}

		} catch (Exception ex) {
			setResponse("Invalid User");
			logger.info("inside login otp action get error message", ex);
			return INPUT;
		}

		//return SUCCESS;
	}

	public void validate() {
		if ((validator.validateBlankFields(getPhoneNumber()))) {
			addFieldError(CrmFieldType.MOBILE.getName(), validator.getResonseObject().getResponseMessage());
			return;
		}
	}

	private static String maskString(String strText, int start, int end, char maskChar) throws Exception {
		if (strText == null || strText.equals(""))
			return "";

		if (start < 0)
			start = 0;

		if (end > strText.length())
			end = strText.length();

		if (start > end)
			throw new Exception("End index cannot be greater than start index");

		int maskLength = end - start;

		if (maskLength == 0)
			return strText;

		StringBuilder sbMaskString = new StringBuilder(maskLength);

		for (int i = 0; i < maskLength; i++) {
			sbMaskString.append(maskChar);
		}
		return strText.substring(0, start) + sbMaskString.toString() + strText.substring(start + maskLength);
	}

	/*
	 * public String getEmailId() { return emailId; }
	 * 
	 * public void setEmailId(String emailId) { this.emailId = emailId; }
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getOtpSuccessStatus() {
		return otpSuccessStatus;
	}

	public void setOtpSuccessStatus(String otpSuccessStatus) {
		this.otpSuccessStatus = otpSuccessStatus;
	}

}
