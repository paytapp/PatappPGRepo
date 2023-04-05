package com.paymentgateway.crm.interceptor;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * @author Puneet
 * 
 */

public class NoCacheInterceptor extends AbstractInterceptor {

	private static Logger logger = LoggerFactory.getLogger(NoCacheInterceptor.class.getName());
	private static final long serialVersionUID = -4346936717217355696L;

	@Override
	public String intercept(ActionInvocation actionInvocation) {
		try {
		
			HttpServletResponse response = ServletActionContext.getResponse();
			response.addHeader("X-FRAME-OPTIONS", "DENY" );
			response.setHeader("Cache-Control",
						"no-store, no-cache, must-revalidate,pre-check=0, post-check=0, max-age=0, s-maxage=0");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Strict-Transport-Security", "max-age=31536000;includeSubDomains");
			response.setHeader("X-XSS-Protection","1;mode=block");
			response.setDateHeader("Expires", 0);

			return actionInvocation.invoke();
		}

		catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}	
	}
}
