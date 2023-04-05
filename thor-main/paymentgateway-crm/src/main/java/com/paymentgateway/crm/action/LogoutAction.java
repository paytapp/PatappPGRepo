package com.paymentgateway.crm.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmActions;
import com.paymentgateway.crm.actionBeans.SessionCleaner;

public class LogoutAction extends AbstractSecureAction {

	@Autowired
	private UserDao userDao;

	private static Logger logger = LoggerFactory.getLogger(LogoutAction.class.getName());
	private static final long serialVersionUID = 9203388873112676406L;

	public String logout() {
		try {
			User user = (User) sessionMap.get(Constants.USER.getValue());
			if (user != null) {
				// updating lastActionName as home
				user = userDao.findPayId(user.getPayId());
				String lastActionName = CrmActions.HOME.getValue();
				user.setLastActionName(lastActionName);
				userDao.update(user);
				MDC.put(Constants.CRM_LOG_USER_PREFIX.getValue(), user.getEmailId());
				logger.info("logged out");
			}
			SessionCleaner.cleanSession(sessionMap);
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			SessionCleaner.cleanSession(sessionMap);
			return SUCCESS;
		}
	}
}
