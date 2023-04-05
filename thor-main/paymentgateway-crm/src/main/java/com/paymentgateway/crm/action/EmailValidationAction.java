package com.paymentgateway.crm.action;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.MerchantGridViewService;
import com.paymentgateway.commons.dao.TransactionDetailsService;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.CustTransactionAuthentication;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.UserStatusType;

/**
 * @author Sunil
 *
 */

public class EmailValidationAction extends AbstractSecureAction {

	private static Logger logger = LoggerFactory.getLogger(EmailValidationAction.class.getName());
	private static final long serialVersionUID = 5995449017764989418L;

	private String id;
	private UserDao userDao = new UserDao();
	private User user = new User();
	@Autowired
	private MerchantGridViewService merchantGridViewService;

	public String execute() {
		try {
			user = userDao.findByAccountValidationKey(getId());

			if (getId() == null) {
				return ERROR;
			} else if (user == null) {
				addActionMessage(ErrorType.ALREADY_VALIDATE_EMAIL.getResponseMessage());
				return "validate";
			} else if (user.isEmailValidationFlag()) {
				addActionMessage(ErrorType.ALREADY_VALIDATE_EMAIL.getResponseMessage());
				return "validate";
			}

			Date expiryTime = user.getEmailExpiryTime();
			Date currentTime = new Date();
			int result = currentTime.compareTo(expiryTime);

			if (result < 0 || result == 0) {
				if (user.getUserType().equals(UserType.ADMIN)) {

					userDao.updateEmailValidation(getId(), UserStatusType.ACTIVE, true);

				} else {
					userDao.updateEmailValidation(getId(), UserStatusType.SUSPENDED, true);
					user.setUserStatus(UserStatusType.SUSPENDED);
					user.setEmailValidationFlag(true);
					merchantGridViewService.addUserInMap(user);
				}

			} else {
				logger.info("Activation link has been expired");
				return "linkExpired";
			}
			return SUCCESS;

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	public String transactionAuthentication() {
		try {
			if (getId() == null) {
				return ERROR;
			}
			TransactionDetailsService transactionDetailsService = new TransactionDetailsService();
			String txnAuthentication = transactionDetailsService.getTransactionAuthentication(getId());

			if (txnAuthentication.equals(CustTransactionAuthentication.SUCCESS.getAuthenticationName())) {
				return "alreadyAuthenticate";
			}
			transactionDetailsService.updateTransactionAuthentication(
					CustTransactionAuthentication.SUCCESS.getAuthenticationName(), getId());
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
