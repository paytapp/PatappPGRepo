package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


import com.paymentgateway.commons.api.SmsSender;
import com.paymentgateway.commons.dao.CheckerMakerDao;
import com.paymentgateway.commons.email.EmailServiceProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.CheckerMaker;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.actionBeans.CheckExistingUser;
import com.paymentgateway.crm.actionBeans.CreateNewUser;

/**
 * @author Puneet,Neeraj
 *
 */
public class SignupAction extends AbstractSecureAction implements ServletRequestAware {

	@Autowired
	private CreateNewUser createUser;

	@Autowired
	private SmsSender smsSender;

	@Autowired
	private CheckerMakerDao checkerMakerDao;

	@Autowired
	private CrmValidator validator;

	@Autowired
	private CheckExistingUser checkExistingUser;

	@Autowired
	private UserDao userDao;

	@Autowired
	private EmailServiceProvider emailServiceProvider;

	private static Logger logger = LoggerFactory.getLogger(SignupAction.class.getName());
	private static final long serialVersionUID = 5995449017764989418L;

	private String emailId;
	private String otp;
	private String pin;
	private String businessName;
	private String phoneNumber;
	private String confirmPin;
	private String userRoleType;
	private String response;
	private String phoneSuccessStatus;
	private String otpSuccessStatus;
	private String emailSuccessStatus;
	private String mpaOnlineOffLineFlag;
	// private String isPartner;
	private String partnerFlag;

	ResponseObject responseObject = new ResponseObject();

	public String execute() {

		List<CheckerMaker> checkerMakerList = new ArrayList<CheckerMaker>();
		CheckerMaker checkerMaker = new CheckerMaker();
		try {

			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			checkerMakerList = checkerMakerDao.findAllChekerMaker();

			if (userRoleType.equals(CrmFieldConstants.USER_RESELLER_TYPE.getValue())) {
				responseObject = createUser.createUser(getUserInstance(checkerMaker), UserType.RESELLER, "",
						sessionUser, getPartnerFlag());
			} else {
				responseObject = createUser.createUser(getUserInstance(checkerMaker), UserType.MERCHANT, "",
						sessionUser, "");
			}
			if (!ErrorType.SUCCESS.getResponseCode().equals(responseObject.getResponseCode())) {
				addActionMessage(responseObject.getResponseMessage());
				return INPUT;
			}
			emailServiceProvider.emailValidator(responseObject);
			addActionMessage("Please verify your sign up by clicking on the link sent on your Email ID");
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			addActionMessage("Something went wrong! Please try again later");
			return INPUT;
		}
	}

	private User getUserInstance(CheckerMaker checkerMaker) {
		try {
			User user = new User();
			user.setEmailId(getEmailId().toLowerCase());
			user.setPin(getPin());
			user.setMobile(getPhoneNumber());
			user.setBusinessName(getBusinessName());
		
			if (!StringUtils.isEmpty(mpaOnlineOffLineFlag) && mpaOnlineOffLineFlag.equalsIgnoreCase("online"))
				user.setMpaOnlineFlag(true);
			else
				user.setMpaOnlineFlag(false);

			if (userRoleType.equals(CrmFieldConstants.USER_RESELLER_TYPE.getValue())) {
			} else {
				if (checkerMaker != null) {
					user.setCheckerPayId(checkerMaker.getCheckerPayId());
					user.setMakerPayId(checkerMaker.getMakerPayId());
					user.setCheckerName(checkerMaker.getCheckerName());
					user.setMakerName(checkerMaker.getMakerName());
				}
			}
			return user;
		} catch (Exception e) {
			logger.error("Exception in getUserInstance() , exception = " , e);
			return null;
		}
	}

	@SkipValidation
	private Long getRandomNumber() {

		boolean isDuplicateVirtualAccountNo = false;

		Long randomNum;

		do {
			logger.info("generating random number");
			randomNum = (long) Math.round(Math.round(Math.random() * (99999999 - 10000000) + 10000000));
			isDuplicateVirtualAccountNo = userDao.checkDuplicateVirtualAccountNo("LETZ" + randomNum);
			logger.info(
					"virtual Account is " + "LETZ" + randomNum + " found duplicate? " + isDuplicateVirtualAccountNo);
		} while (isDuplicateVirtualAccountNo);

		return randomNum;
	}

	@SkipValidation
	public String emailIdValidate() {
		try {
			responseObject = checkExistingUser.checkuser(getEmailId());
			if (responseObject.getResponseCode().equalsIgnoreCase("307")) {
				setResponse("you can signup");
				setEmailSuccessStatus("success");
				return SUCCESS;
			} else {
				setResponse("Email ID already exist !");
				setEmailSuccessStatus("error");
				return SUCCESS;
			}
		} catch (Exception e) {
			logger.error("Something went wrong : " , e);
		}
		return SUCCESS;

	}

	@SkipValidation
	public String phoneNumberValidate() {
		try {
			if (StringUtils.isBlank(getPhoneNumber()) || (getPhoneNumber().length() > 10)) {
				setResponse("Invalid Mobile Number");
				return INPUT;
			}
			responseObject = checkExistingUser.checkPhoneUser(getPhoneNumber());
			if (responseObject == null) {
				int num = (int) ((Math.random() * 99999) + 100000);
				String otp = Integer.toString(num);
				String otpMessage = maskString(getPhoneNumber(), 2, 6, '*');
				String message = "Your OTP for Daddytech is " + otp
						+ ". Please do not share your OTP with anyone.\n Thanks, \n Team Payment Gateway";
				logger.info("Message is "+message);
				String smsInnuvisolutions = PropertiesManager.propertiesMap
						.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
				String responseMsg;
				/*
				 * if (StringUtils.isNotBlank(smsInnuvisolutions) &&
				 * smsInnuvisolutions.equalsIgnoreCase("Y")) {
				 * logger.info("SMS send by InnvisSolution"); responseMsg =
				 * smsSender.sendSMSByInnvisSolution(getPhoneNumber(), message);
				 * 
				 * } else { logger.info("Normal sms send"); responseMsg =
				 * smsSender.sendSMS(getPhoneNumber(), message); }
				 */
				// String response = smsSender.sendSMS(getPhoneNumber(),
				// message);
				responseMsg = "success";
				if (responseMsg.equalsIgnoreCase("success") || responseMsg.equalsIgnoreCase("000")) {
					sessionMap.put(getPhoneNumber(), otp);
					setResponse("OTP has been sent to " + otpMessage);
					setOtpSuccessStatus("success");
					return SUCCESS;
				} else {
					setResponse("Please try again later");
					setOtpSuccessStatus("error");
					return SUCCESS;
				}
			} else {
				setResponse("Mobile number already exist !");
				setOtpSuccessStatus("error");
				return SUCCESS;
			}
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
			setResponse("Something went wrong, please try again later");
		}
		return SUCCESS;
	}

	@SkipValidation
	public String mobileNumberValidate() {
		try {
			
			if (validator.validateBlankField(getPhoneNumber())) {
				setPhoneSuccessStatus("error");
				setResponse("Invalid Mobile Number");
				return ERROR;
			} else if (!(validator.validateField(CrmFieldType.MOBILE, getPhoneNumber()))) {
				setPhoneSuccessStatus("error");
				setResponse("Invalid Mobile Number");
				return ERROR;
			}
			
			if (StringUtils.isBlank(getPhoneNumber()) || (getPhoneNumber().length() > 10)) {
				setPhoneSuccessStatus("error");
				setResponse("Mobile number already exists");
				return SUCCESS;
			}
			if (checkExistingUser.checkExistingMobileUser(getPhoneNumber())) {
				setPhoneSuccessStatus("success");
				return SUCCESS;
			}
		} catch (Exception e) {
			logger.info("Exception caught, " , e);
			setResponse("Mobile number already exists");
			setPhoneSuccessStatus("error");
			return SUCCESS;
		}
		setPhoneSuccessStatus("error");
		setResponse("Mobile number already exists");
		return SUCCESS;
	}

	@SkipValidation
	public String verifyOtp() {
		try {
			String otp1 = (String) sessionMap.get(getPhoneNumber());
			String otp = getOtp();
			if (!StringUtils.isBlank(otp)) {
				if (otp1.equals(otp)) {
					setResponse("OTP successfully verified");
					setOtpSuccessStatus("success");
					return SUCCESS;
				} else {
					setResponse("Invalid OTP");
					setOtpSuccessStatus("error");
					return SUCCESS;
				}
			}
		} catch (Exception e) {
			logger.info("Exception caught, " , e);
		}
		return SUCCESS;
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

	public void validate() {
		if (validator.validateBlankField(getPin())) {
			addFieldError(CrmFieldType.PIN.getName(), validator.getResonseObject().getResponseMessage());
			if (validator.validateBlankField(getConfirmPin())) {
				addFieldError(CrmFieldConstants.CONFIRM_PIN.getValue(),
						validator.getResonseObject().getResponseMessage());
			}
		} else if (validator.validateBlankField(getConfirmPin())) {
			addFieldError(CrmFieldConstants.CONFIRM_PIN.getValue(), validator.getResonseObject().getResponseMessage());
		} else if (!(getPin().equals(getConfirmPin()))) {
			addFieldError(CrmFieldType.PIN.getName(), ErrorType.PASSWORD_MISMATCH.getResponseMessage());
		} else if (!(validator.isValidPin(getPin()))) {
			addFieldError(CrmFieldType.PIN.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}

		if ((validator.validateBlankField(getBusinessName()))) {
			addFieldError(CrmFieldType.BUSINESS_NAME.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.BUSINESS_NAME, getBusinessName()))) {
			addFieldError(CrmFieldType.BUSINESS_NAME.getName(), validator.getResonseObject().getResponseMessage());
		}

		if (validator.validateBlankField(getPhoneNumber())) {
			addFieldError(CrmFieldType.MOBILE.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.MOBILE, getPhoneNumber()))) {
			addFieldError(CrmFieldType.MOBILE.getName(), validator.getResonseObject().getResponseMessage());
		}

		if (validator.validateBlankField(getEmailId())) {
			addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.isValidEmailId(getEmailId()))) {
			addFieldError(CrmFieldType.EMAILID.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
	}

	public String getMpaOnlineOffLineFlag() {
		return mpaOnlineOffLineFlag;
	}

	public void setMpaOnlineOffLineFlag(String mpaOnlineOffLineFlag) {
		this.mpaOnlineOffLineFlag = mpaOnlineOffLineFlag;
	}

	public String getConfirmPin() {
		return confirmPin;
	}

	public void setConfirmPin(String confirmPin) {
		this.confirmPin = confirmPin;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getUserRoleType() {
		return userRoleType;
	}

	public void setUserRoleType(String userRoleType) {
		this.userRoleType = userRoleType;
	}

	@Override
	public void setServletRequest(HttpServletRequest request) {
		// TODO Auto-generated method stub

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

	public void setEmailSuccessStatus(String emailSuccessStatus) {
		this.emailSuccessStatus = emailSuccessStatus;
	}

	public String getEmailSuccessStatus() {
		return emailSuccessStatus;
	}

	public String getPhoneSuccessStatus() {
		return phoneSuccessStatus;
	}

	public void setPhoneSuccessStatus(String phoneSuccessStatus) {
		this.phoneSuccessStatus = phoneSuccessStatus;
	}

	public String getPartnerFlag() {
		return partnerFlag;
	}

	public void setPartnerFlag(String partnerFlag) {
		this.partnerFlag = partnerFlag;
	}

}
