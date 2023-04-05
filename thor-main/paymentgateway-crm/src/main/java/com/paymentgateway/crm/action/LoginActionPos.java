package com.paymentgateway.crm.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.dispatcher.SessionMap;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.util.TokenHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.LoginHistory;
import com.paymentgateway.commons.user.LoginHistoryDao;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.crm.actionBeans.LoginAuthenticator;
import com.paymentgateway.crm.actionBeans.SessionCleaner;;

/**
 * @author Shaiwal
 * 
 */

public class LoginActionPos extends AbstractSecureAction implements ServletRequestAware {

	@Autowired
	LoginHistoryDao loginHistoryDao;

	@Autowired
	private CrmValidator validator;

	@Autowired
	LoginAuthenticator loginAuthenticator;

	private static Logger logger = LoggerFactory.getLogger(LoginActionPos.class.getName());
	private static final long serialVersionUID = -5127683348802926510L;

	private String redirectUrl;
	private String emailId;
	private String password;
	private String otp;
	private HttpServletRequest request;
	private String loginType;
	private String permissionString = "";

	@Override
	public String execute() {

		logger.info("Login for Action POS , emailId = "+emailId);

		logger.info("IP ADDRESS X-Forwarded-For   " + request.getHeader("X-Forwarded-For") + "     port    "
				+ request.getHeader("x-forwarded-port"));

		logger.info("IP ADDRESS TEST remote addr    " + request.getRemoteAddr());
		logger.info("CloudFront-Is-Mobile-Viewer    " + request.getHeader("CloudFront-Is-Mobile-Viewer"));
		logger.info("CloudFront-Is-Tablet-Viewer    " + request.getHeader("CloudFront-Is-Tablet-Viewer"));
		logger.info("CloudFront-Is-SmartTV-Viewer    " + request.getHeader("CloudFront-Is-SmartTV-Viewer"));
		logger.info("CloudFront-Is-Desktop-Viewer    " + request.getHeader("CloudFront-Is-Desktop-Viewer"));
		logger.info("Header value " + request.getHeader("User-Agent"));
		ResponseObject responseObject = new ResponseObject();
		LoginHistory loginHistory = new LoginHistory();
		User user;
		try {

			if (loginType.equals("userOtp")) {
				responseObject = loginAuthenticator.authenticateOtp(getOtp(), getEmailId(),
						request.getHeader(CrmFieldConstants.USER_AGENT.getValue()), request.getRemoteAddr());
			} else {
				responseObject = loginAuthenticator.authenticate(getEmailId(), getPassword(),
						request.getHeader(CrmFieldConstants.USER_AGENT.getValue()), request.getRemoteAddr());
			}

			if (!ErrorType.SUCCESS.getResponseCode().equals(responseObject.getResponseCode())) {
				addFieldError(CrmFieldType.EMAILID.getName(), responseObject.getResponseMessage());
				return INPUT;
			}

			MDC.put(Constants.CRM_LOG_USER_PREFIX.getValue(), getEmailId());
			logger.info("logged in");

			loginHistory = loginHistoryDao.findLastLoginByUser(getEmailId());

			SessionCleaner.cleanSession(sessionMap);
			sessionMap = (SessionMap<String, Object>) ActionContext.getContext().getSession();

			user = loginAuthenticator.getUser();
			sessionMap.put(Constants.USER.getValue(), user);
			sessionMap.put(Constants.TERMINAL_ID.getValue(), user.getTerminalId());
			sessionMap.put(Constants.PAY_ID.getValue(), user.getPayId());
			sessionMap.put(Constants.LAST_LOGIN.getValue(), loginHistory);
			sessionMap.put(Constants.CUSTOM_TOKEN.getValue(), TokenHelper.generateGUID());
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	public void validate() {

		if ((validator.validateBlankFields(getEmailId()))) {
			addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
			return;
		}

		if (loginType.equals("pwd")) {
			if (validator.validateBlankFields(getPassword())) {
				addFieldError(CrmFieldType.PASSWORD.getName(), validator.getResonseObject().getResponseMessage());
				return;
			}
		} else if (loginType.equals("userOtp")) {
			if (validator.validateBlankFields(getOtp())) {
				addFieldError(CrmFieldType.OTP.getName(), validator.getResonseObject().getResponseMessage());
				return;
			}
		}

	}

	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	public String getEmailId() {
		return emailId.trim();
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

}
