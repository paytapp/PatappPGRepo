package com.paymentgateway.crm.interceptor;

import java.util.Map;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;

/**
 * @author Harpreet
 *
 */
public class ResellerAuthorizationInterceptor extends AbstractInterceptor {

	private static final long serialVersionUID = -2389829581420808437L;
	private static Logger logger = LoggerFactory.getLogger(ResellerAuthorizationInterceptor.class.getName());
	
	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		try {
			Map<String, Object> sessionMap = invocation
					.getInvocationContext().getSession();
			Object userObject = sessionMap.get(Constants.USER.getValue());

			if (null == userObject) {
				return Action.LOGIN;
			}

			User user = (User) userObject;
		if(!user.getUserType().equals(UserType.RESELLER)){
			return Action.LOGIN;
		}

			return invocation.invoke();
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return Action.ERROR;
		}
	}
	

}
