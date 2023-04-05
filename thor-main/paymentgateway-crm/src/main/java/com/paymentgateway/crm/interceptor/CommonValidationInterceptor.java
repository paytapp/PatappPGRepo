package com.paymentgateway.crm.interceptor;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.paymentgateway.commons.util.CrmValidator;

/**
 * @author Puneet
 * 
 */

public class CommonValidationInterceptor extends AbstractInterceptor {
	
	@Autowired
	private CrmValidator validator;

	private static Logger logger = LoggerFactory.getLogger(CommonValidationInterceptor.class.getName());
	private static final long serialVersionUID = -8412279724011707370L;
	
	@SuppressWarnings("unchecked")
	@Override
	public String intercept(ActionInvocation actionInvocation) {
		
		HttpServletRequest	request = ServletActionContext.getRequest();
		Map<String,String[]> requestMap = request.getParameterMap();
		try {
			for (Entry<String, String[]> httpRequestentry : requestMap.entrySet()) {				
				for(String parameter:httpRequestentry.getValue()){
					parameter = validator.validateRequestParameter(parameter);
				}			
			}
			return actionInvocation.invoke();			
		}catch (ClassCastException classCastException) {
			logger.error("Class cast exception", classCastException);
			return Action.ERROR;
		}catch (Exception exception) {
			logger.error("Exception", exception);
			return Action.ERROR;
		}	
	}	
}
