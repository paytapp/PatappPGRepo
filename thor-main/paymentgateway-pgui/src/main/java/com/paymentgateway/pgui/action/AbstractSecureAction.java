package com.paymentgateway.pgui.action;

import java.util.Map;

import org.apache.struts2.dispatcher.SessionMap;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.interceptor.ParameterNameAware;
import com.paymentgateway.commons.util.BlackListedParameterNames;

/**
 * @author Puneet
 *
 */
public class AbstractSecureAction extends AbstractAction implements	SessionAware,ParameterNameAware {

	private static final long serialVersionUID = -5723367184317587221L;
	
	protected SessionMap<String, Object> sessionMap;

	public void setSession(Map<String, Object> session){
		this.sessionMap = (SessionMap<String, Object>) session;
	}
	
	protected Map<String, Object> getSession(){
		return sessionMap;
	}
	
	protected void sessionPut(String key, Object value){
		sessionMap.put(key, value);
	}

	public boolean acceptableParameterName(String parameterName) {

		boolean allowedParameterName = true;

		for (BlackListedParameterNames parameter : BlackListedParameterNames.values()) {
			if (parameterName.contains(parameter.getName())) {
				allowedParameterName = false;
			}
		}
		return allowedParameterName;
	}
}
