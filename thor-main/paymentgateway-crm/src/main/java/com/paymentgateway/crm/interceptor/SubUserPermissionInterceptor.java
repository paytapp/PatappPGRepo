package com.paymentgateway.crm.interceptor;

import java.util.Map;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.PropertiesManager;

public class SubUserPermissionInterceptor extends AbstractInterceptor {
	
	private static Logger logger = LoggerFactory.getLogger(SubUserPermissionInterceptor.class.getName());
	private static final long serialVersionUID = 4851907201423757904L;

	@Override
	public String intercept(ActionInvocation actionInvocation) {
		try {	
			PropertiesManager prop = new PropertiesManager();
			Map<String, Object> sessionMap = actionInvocation
					.getInvocationContext().getSession();
			Object userObject = sessionMap.get(Constants.USER.getValue());			
			if (null == userObject) {
				return Action.LOGIN;
			}		
			User user = (User) userObject;
			
		if(user.getUserType().equals(UserType.SUBUSER) ||user.getUserType().equals(UserType.SUBACQUIRER)){
			String permissionString = sessionMap.get("USER_PERMISSION").toString();
			String actionName = actionInvocation.getProxy().getActionName();
			if(prop.getSubUserPermissionProperty(CrmFieldConstants.VIEW_TRANSACTIONS.name()).contains(actionName)) {
				if(!permissionString.contains(CrmFieldConstants.VIEW_TRANSACTIONS.getValue())) {
					return Action.LOGIN;
				}
			}
			else if(prop.getSubUserPermissionProperty(CrmFieldConstants.VIEW_REPORTS.name()).contains(actionName)) {
				if(!permissionString.contains(CrmFieldConstants.VIEW_REPORTS.getValue())) {
					return Action.LOGIN;
				}
			}
			else if(prop.getSubUserPermissionProperty(CrmFieldConstants.CREATE_INVOICE.name()).contains(actionName)) {
				if(!permissionString.contains(CrmFieldConstants.CREATE_INVOICE.getValue())) {
					return Action.LOGIN;
				}
			}
			else if(prop.getSubUserPermissionProperty(CrmFieldConstants.VIEW_INVOICE.name()).contains(actionName)) {
				if(!permissionString.contains(CrmFieldConstants.VIEW_INVOICE.getValue())) {
					return Action.LOGIN;
				}
			}
			else if(prop.getSubUserPermissionProperty(CrmFieldConstants.VIEW_REMITTANCE.name()).contains(actionName)) {
				if(!permissionString.contains(CrmFieldConstants.VIEW_REMITTANCE.getValue())) {
					return Action.LOGIN;
				}
			}else{
				return Action.LOGIN;
			}
		}
			return actionInvocation.invoke();
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return Action.ERROR;
		}
	}

}
