package com.paymentgateway.crm.interceptor;

import java.util.Map;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;

public class SubAdminPermissionInterceptor extends AbstractInterceptor {

	/**
	 * @ Neeraj
	 * 
	 */
	private static final long serialVersionUID = 4146811630295786099L;
	private static Logger logger = LoggerFactory.getLogger(SubAdminPermissionInterceptor.class.getName());

	@Override
	public String intercept(ActionInvocation actionInvocation) throws Exception {
		try {
			PropertiesManager prop = new PropertiesManager();
			Map<String, Object> sessionMap = actionInvocation.getInvocationContext().getSession();
			Object userObject = sessionMap.get(Constants.USER.getValue());
			if (null == userObject) {
				return Action.LOGIN;
			}

			//User user = (User) userObject;
		//	if (user.getUserType().equals(UserType.SUBADMIN)) {
		//		String permissionString = sessionMap.get("USER_PERMISSION").toString();
		//		String actionName = actionInvocation.getProxy().getActionName();

				/*if (prop.getSuAdminPermissionProperty(CrmFieldConstants.CREATE_INVOICE.name())
						.contains(actionName)) {
					if (!permissionString.contains(CrmFieldConstants.CREATE_INVOICE.getValue())) {
						return Action.LOGIN;
					}
				} else if (prop.getSuAdminPermissionProperty(CrmFieldConstants.VIEW_INVOICE.name())
						.contains(actionName)) {
					if (!permissionString.contains(CrmFieldConstants.VIEW_INVOICE.getValue())) {
						return Action.LOGIN;
					}
				} else if (prop.getSuAdminPermissionProperty(CrmFieldConstants.VIEW_MERCHANT_SETUP.name())
						.contains(actionName)) {
					if (!permissionString.contains(CrmFieldConstants.VIEW_MERCHANT_SETUP.getValue())) {
						return Action.LOGIN;
					}
				} else if (prop.getSuAdminPermissionProperty(CrmFieldConstants.CREATE_MAPPING.name())
						.contains(actionName)) {
					if (!permissionString.contains(CrmFieldConstants.CREATE_MAPPING.getValue())) {
						return Action.LOGIN;
					}
				} else if (prop.getSuAdminPermissionProperty(CrmFieldConstants.VIEW_SEARCH_PAYMENT.name())
						.contains(actionName)) {
					if (!permissionString.contains(CrmFieldConstants.VIEW_SEARCH_PAYMENT.getValue())) {
						return Action.LOGIN;
					}
				} else if (prop.getSuAdminPermissionProperty(CrmFieldConstants.CREATE_BATCH_OPERATION.name())
						.contains(actionName)) {
					if (!permissionString.contains(CrmFieldConstants.CREATE_BATCH_OPERATION.getValue())) {
						return Action.LOGIN;
					}
				} else if (prop.getSuAdminPermissionProperty(CrmFieldConstants.FRAUD_PREVENTION.name())
						.contains(actionName)) {
					if (!permissionString.contains(CrmFieldConstants.FRAUD_PREVENTION.getValue())) {
						return Action.LOGIN;
					}
				} else if (prop.getSuAdminPermissionProperty(CrmFieldConstants.CREATE_BULK_EMAIL.name())
						.contains(actionName)) {
					if (!permissionString.contains(CrmFieldConstants.CREATE_BULK_EMAIL.getValue())) {
						return Action.LOGIN;
					}
				} else if (prop.getSuAdminPermissionProperty(CrmFieldConstants.VIEW_CASHBACK.name())
						.contains(actionName)) {
					if (!permissionString.contains(CrmFieldConstants.VIEW_CASHBACK.getValue())) {
						return Action.LOGIN;
					}
				} else if (prop.getSuAdminPermissionProperty(CrmFieldConstants.CREATE_HELPTIKECT.name())
						.contains(actionName)) {
					if (!permissionString.contains(CrmFieldConstants.CREATE_HELPTIKECT.getValue())) {
						return Action.LOGIN;
					}
				}*/
			//}
			return actionInvocation.invoke();
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return Action.ERROR;
		}
	}

}
