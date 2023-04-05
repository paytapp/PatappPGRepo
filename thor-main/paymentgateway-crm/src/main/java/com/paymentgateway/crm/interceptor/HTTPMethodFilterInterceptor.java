package com.paymentgateway.crm.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.paymentgateway.commons.util.CrmFieldConstants;

public class HTTPMethodFilterInterceptor  extends AbstractInterceptor {

	private static final long serialVersionUID = 8474751470406698684L;

	@Override
	public String intercept(ActionInvocation actionInvocation) throws Exception {
		HttpServletRequest	request = ServletActionContext.getRequest();
		if(request.getMethod().equals(CrmFieldConstants.HTTP_POST_METHOD.getValue())){
			return actionInvocation.invoke();
		}
		HttpServletResponse response = ServletActionContext.getResponse();
        response.setStatus(400);
		return "login";	
	}
}
