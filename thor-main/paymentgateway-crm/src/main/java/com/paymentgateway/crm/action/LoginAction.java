package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.dispatcher.SessionMap;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.apache.struts2.util.TokenHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.LoginHistory;
import com.paymentgateway.commons.user.LoginHistoryDao;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.Permissions;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.Roles;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmActions;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.actionBeans.CheckExistingUser;
import com.paymentgateway.crm.actionBeans.LoginAuthenticator;
import com.paymentgateway.crm.actionBeans.SessionCleaner;;

/**
 * @author Puneet
 * 
 */
public class LoginAction extends AbstractSecureAction implements ServletRequestAware {

	@Autowired
	private LoginHistoryDao loginHistoryDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private CrmValidator validator;

	@Autowired
	private LoginAuthenticator loginAuthenticator;

	@Autowired
	private CheckExistingUser checkExistingUser;

	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
    private UserSettingDao userSettingDao;

	private static Logger logger = LoggerFactory.getLogger(LoginAction.class.getName());
	private static final long serialVersionUID = -5127683348802926510L;

	private String redirectUrl;
	private String emailId;
	private String phoneNumber;
	private String pin;
	private String otp;
	private HttpServletRequest request;
	private String loginType;
	private String permissionString = "";
	private String phoneSuccessStatus;
	private String otpSuccessStatus;
	private String responseMsg;
	private String captcha;
	private String captchaCode;
	private String phoneUserType;
	private String password;
	ResponseObject responseObject = new ResponseObject();

	@Override
	public String execute() {
		logger.info("Login Action , above ip address ");
		logger.info("IP ADDRESS X-Forwarded-For   " + request.getHeader("X-Forwarded-For") + "     port    "
				+ request.getHeader("x-forwarded-port"));
		logger.info("IP ADDRESS TEST remote addr    " + request.getRemoteAddr());
		logger.info("CloudFront-Is-Mobile-Viewer    " + request.getHeader("CloudFront-Is-Mobile-Viewer"));
		logger.info("CloudFront-Is-Tablet-Viewer    " + request.getHeader("CloudFront-Is-Tablet-Viewer"));
		logger.info("CloudFront-Is-SmartTV-Viewer    " + request.getHeader("CloudFront-Is-SmartTV-Viewer"));
		logger.info("CloudFront-Is-Desktop-Viewer    " + request.getHeader("CloudFront-Is-Desktop-Viewer"));
		logger.info("Header value " + request.getHeader("User-Agent"));
		try {
			ResponseObject responseObject = new ResponseObject();
			LoginHistory loginHistory = new LoginHistory();
			User user;
			UserSettingData userSettings;
			try {
				if (loginType.equals("userOtp")) {
					if (sessionMap.containsKey("OTP_RESPONSE_CODE")) {
						responseObject.setResponseCode((String) sessionMap.get("OTP_RESPONSE_CODE"));
						sessionMap.remove("OTP_RESPONSE_CODE");
					} else {
						responseObject = loginAuthenticator.authenticateOtp(getOtp(), getPhoneNumber(),
								request.getHeader(CrmFieldConstants.USER_AGENT.getValue()), request.getRemoteAddr());
						if (responseObject.getResponseCode().equals(ErrorType.INACTIVE_OTP.getResponseCode())) {
							addFieldError(CrmFieldType.OTP.getName(), ErrorType.INACTIVE_OTP.getResponseMessage());
						}
					}
				}

				else if (loginType.equals("pwd")) {
					responseObject = loginAuthenticator.authenticatePasswordOtp(getPhoneNumber(), getPassword(),
							getOtp(), request.getHeader(CrmFieldConstants.USER_AGENT.getValue()),
							request.getRemoteAddr());
				}

				else {
					responseObject = loginAuthenticator.authenticate(getPhoneNumber(), getPin(),
							request.getHeader(CrmFieldConstants.USER_AGENT.getValue()), request.getRemoteAddr());
				}
				if (!ErrorType.SUCCESS.getResponseCode().equals(responseObject.getResponseCode())) {
					addFieldError("error", responseObject.getResponseMessage());
					return INPUT;
				}
				// To add custom field to each log for all activities of this user
				MDC.put(Constants.CRM_LOG_USER_PREFIX.getValue(), getPhoneNumber());
				logger.info("logged in");
				SessionCleaner.cleanSession(sessionMap);
				sessionMap = (SessionMap<String, Object>) ActionContext.getContext().getSession();
				user = loginAuthenticator.getUser();
				emailId = user.getEmailId();
				loginHistory = loginHistoryDao.findLastLoginByUser(emailId);
				
				if(user.getUserType().equals(UserType.MERCHANT) || user.getUserType().equals(UserType.PARENTMERCHANT) || user.getUserType().equals(UserType.SUBUSER)){
					userSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());
					sessionMap.put(Constants.USER_SETTINGS.getValue(), userSettings);
				}
				
				sessionMap.put(Constants.USER.getValue(), user);
				sessionMap.put(Constants.LAST_LOGIN.getValue(), loginHistory);
				sessionMap.put(Constants.CUSTOM_TOKEN.getValue(), TokenHelper.generateGUID());
				if (user.getUserType().equals(UserType.SUBUSER) || user.getUserType().equals(UserType.SUBACQUIRER)
						|| user.getUserType().equals(UserType.SUBADMIN)
						|| user.getUserType().equals(UserType.RESELLER)) {
					Set<Roles> roles = user.getRoles();
					if (user.getUserType().equals(UserType.RESELLER) && roles.isEmpty()) {
					} else {
						Set<Permissions> permissions = roles.iterator().next().getPermissions();
						if (!permissions.isEmpty()) {
							StringBuilder perms = new StringBuilder();
							Iterator<Permissions> itr = permissions.iterator();
							while (itr.hasNext()) {
								PermissionType permissionType = itr.next().getPermissionType();
								perms.append(permissionType.getPermission());
								perms.append("-");
							}
							perms.deleteCharAt(perms.length() - 1);
							setPermissionString(perms.toString());
							sessionMap.put(Constants.USER_PERMISSION.getValue(), perms.toString());

						}
						sessionMap.put("SUBUSERTYPE", user.getSubUserType());
					}
				}

				else if (user.getUserType().equals(UserType.SUBADMIN)) {
					redirectUrl = user.getLastActionName();
				}

				// redirecting to lastActionName
				redirectUrl = user.getLastActionName();
				if (redirectUrl != null) {

					// Quick fix for "resellerLists" action (New name is "adminResellers"
					// that was refactored --Harpreet
					if (redirectUrl.equalsIgnoreCase("resellerLists")) {
						redirectUrl = CrmActions.ADMIN_RESELLER_LISTS.getValue();
						return "redirect";
					}
					return "redirect";
				}
				return SUCCESS;
			} catch (NullPointerException exception) {
				logger.error("NullPointerException has been caught forwarding back to index action ", exception);
				return INPUT;
			}
		} catch (Exception e) {
			logger.error("Exception", e);
			return ERROR;
		}
	}

	public String validateLoginOTP() {
		ResponseObject responseObject = new ResponseObject();
		try {
			responseObject = loginAuthenticator.authenticateOtp(getOtp(), getPhoneNumber(),
					request.getHeader(CrmFieldConstants.USER_AGENT.getValue()), request.getRemoteAddr());
			if (!ErrorType.SUCCESS.getResponseCode().equals(responseObject.getResponseCode())) {
				setOtpSuccessStatus("error");
				setResponseMsg(responseObject.getResponseMessage());
				addFieldError("error", responseObject.getResponseMessage());
				return SUCCESS;
			}
			setOtpSuccessStatus("success");
			sessionMap.put("OTP_RESPONSE_CODE", "000");
		} catch (Exception e) {
			setOtpSuccessStatus("error");
			setResponseMsg(responseObject.getResponseMessage());
			logger.error("Exception caught while validating Login OTP, ", e);
			return SUCCESS;
		}
		return SUCCESS;
	}

	@SkipValidation
	public String phoneNumberValidate() {
		try {
			if (StringUtils.isBlank(getPhoneNumber()) || (getPhoneNumber().length() > 10)) {
				addFieldError(CrmFieldType.MOBILE.getName(), "Invalid Mobile Number");
				return INPUT;
			}
			responseObject = checkExistingUser.checkPhoneUserLogin(getPhoneNumber());
			if (responseObject.getResponseCode().equalsIgnoreCase("380")) {
				setPhoneSuccessStatus("success");
				setPhoneUserType(responseObject.getUserType());
				return SUCCESS;
			} else {
				addFieldError(CrmFieldType.MOBILE.getName(), "User account doesn't exist with this phone number");
				setPhoneSuccessStatus("error");
				return SUCCESS;
			}
		} catch (Exception e) {
			addFieldError(CrmFieldType.MOBILE.getName(), "User account doesn't exist with this phone number");
			logger.info("User account doesn't exist with this phone number");
		}
		return SUCCESS;
	}

	public String sessionTimeOut() {
		addFieldError("error", "Your session has been expired!");
		return INPUT;
	}

	public void validate() {

		String whiteListedMobile = propertiesManager.propertiesMap.get("whiteListedMobile");

		if (StringUtils.isNotBlank(whiteListedMobile) && StringUtils.isNotBlank(getPhoneNumber())
				&& whiteListedMobile.contains(getPhoneNumber())) {
			logger.info("Captcha skipped for this user with mobile = " + getPhoneNumber());
		} else {
			if (!validator.validateBlankFieldForCaptcha(getCaptcha())) {
				String sessionCaptcha = (String) sessionMap.get(CaptchaServlet.CAPTCHA_KEY);

				if (sessionMap.get(CaptchaServlet.CAPTCHA_KEY) != null) {

					if (!captcha.equalsIgnoreCase(sessionCaptcha)) {
						setCaptcha("");
						addFieldError(CrmFieldType.CAPTCHA.getName(), CrmFieldConstants.INVALID_CAPTCHA.getValue());
					}
				}
			}
		}

		if (!(validator.validateBlankField(getPhoneNumber()))) {
			if (!(validator.validateField(CrmFieldType.MOBILE, getPhoneNumber()))) {
				addFieldError(CrmFieldType.MOBILE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (StringUtils.isNotBlank(loginType))
			if (loginType.equals("pin")) {
				if (validator.validateBlankFields(getPin())) {
					addFieldError(CrmFieldType.PIN.getName(), validator.getResonseObject().getResponseMessage());
					return;
				}
			} else if (loginType.equals("userOtp")) {
				if (validator.validateBlankFields(getOtp())) {
					addFieldError(CrmFieldType.OTP.getName(), validator.getResonseObject().getResponseMessage());
					return;
				}
			} else if (loginType.equals("pwd")) {
				if (validator.validateBlankFields(getPassword())) {
					addFieldError(CrmFieldType.PASSWORD.getName(), validator.getResonseObject().getResponseMessage());
				}
			}
	}

	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	public String getPermissionString() {
		return permissionString;
	}

	public void setPermissionString(String permissionString) {
		this.permissionString = permissionString;
	}

	public String getLoginType() {
		return loginType;
	}

	public void setLoginType(String loginType) {
		this.loginType = loginType;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public String getPhoneSuccessStatus() {
		return phoneSuccessStatus;
	}

	public void setPhoneSuccessStatus(String phoneSuccessStatus) {
		this.phoneSuccessStatus = phoneSuccessStatus;
	}

	public String getOtpSuccessStatus() {
		return otpSuccessStatus;
	}

	public void setOtpSuccessStatus(String otpSuccessStatus) {
		this.otpSuccessStatus = otpSuccessStatus;
	}

	public String getResponseMsg() {
		return responseMsg;
	}

	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	public String getCaptcha() {
		return captcha;
	}

	public void setCaptcha(String captcha) {
		this.captcha = captcha;
	}

	public String getCaptchaCode() {
		return captchaCode;
	}

	public void setCaptchaCode(String captchaCode) {
		this.captchaCode = captchaCode;
	}

	public String getPhoneUserType() {
		return phoneUserType;
	}

	public void setPhoneUserType(String phoneUserType) {
		this.phoneUserType = phoneUserType;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	
}