package com.paymentgateway.crm.action;

import java.util.Iterator;
import java.util.Set;

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
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.Permissions;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.Roles;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmActions;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.crm.actionBeans.LoginAuthenticator;
import com.paymentgateway.crm.actionBeans.SessionCleaner;

public class LoginPosAction extends AbstractSecureAction implements ServletRequestAware {
	
	
	@Autowired
	LoginHistoryDao loginHistoryDao;

	@Autowired
	private CrmValidator validator;

	@Autowired
	LoginAuthenticator loginAuthenticator;

	private static Logger logger = LoggerFactory.getLogger(LoginAction.class.getName());
	private static final long serialVersionUID = -5127683348802926510L;

	private String redirectUrl;
	private String emailId;
	private String password;
	private String captcha;
	private String captchaCode;
	private String otp;
	private HttpServletRequest request;
	private String loginType;
	private String permissionString = "";

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
		logger.info("Header value "+request.getHeader("User-Agent"));
		ResponseObject responseObject = new ResponseObject();
		LoginHistory loginHistory = new LoginHistory();
		User user;
		try {
			

		   if(loginType.equals("userOtp"))
		   {
              responseObject = loginAuthenticator.authenticateOtp(getOtp(),getEmailId(),
            		  request.getHeader(CrmFieldConstants.USER_AGENT.getValue()), request.getRemoteAddr());
	 	   }			
		   else
		   {
			responseObject = loginAuthenticator.authenticate(getEmailId(), getPassword(),
					request.getHeader(CrmFieldConstants.USER_AGENT.getValue()), request.getRemoteAddr());
		   }
		   
		   
		   
			if (!ErrorType.SUCCESS.getResponseCode().equals(responseObject.getResponseCode())) {
				addFieldError(CrmFieldType.EMAILID.getName(), responseObject.getResponseMessage());
				return INPUT;
			}

			// To add custom field to each log for all activities of this user
			MDC.put(Constants.CRM_LOG_USER_PREFIX.getValue(), getEmailId());
			logger.info("logged in");

			loginHistory = loginHistoryDao.findLastLoginByUser(getEmailId());

			SessionCleaner.cleanSession(sessionMap);
			sessionMap = (SessionMap<String, Object>) ActionContext.getContext().getSession();

			user = loginAuthenticator.getUser();
			sessionMap.put(Constants.USER.getValue(), user);
			sessionMap.put(Constants.LAST_LOGIN.getValue(), loginHistory);
			sessionMap.put(Constants.CUSTOM_TOKEN.getValue(), TokenHelper.generateGUID());
			if (user.getUserType().equals(UserType.SUBUSER) || user.getUserType().equals(UserType.SUBACQUIRER)
					|| user.getUserType().equals(UserType.SUBADMIN)) {
				Set<Roles> roles = user.getRoles();
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
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}


	public void validate() {
		if (validator.validateBlankField(getCaptcha())) {
			addFieldError(CrmFieldType.CAPTCHA.getName(), validator.getResonseObject().getResponseMessage());
		} else {
			String sessionCaptcha = (String) sessionMap.get(CaptchaServlet.CAPTCHA_KEY);
			if (!captcha.equalsIgnoreCase(sessionCaptcha)) {
				setCaptcha("");
				addFieldError(CrmFieldType.CAPTCHA.getName(), CrmFieldConstants.INVALID_CAPTCHA.getValue());

			}
		}

		if ((validator.validateBlankFields(getEmailId()))) {
			addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
			return;
		} 
		
		if(loginType.equals("pwd"))
		{
			if (validator.validateBlankFields(getPassword())) {			
				addFieldError(CrmFieldType.PASSWORD.getName(), validator.getResonseObject().getResponseMessage());
				return;
			}
		}
		else if(loginType.equals("userOtp"))
		{
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
