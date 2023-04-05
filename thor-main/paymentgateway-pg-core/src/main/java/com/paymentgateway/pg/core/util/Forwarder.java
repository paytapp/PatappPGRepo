package com.paymentgateway.pg.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionSupport;
import com.paymentgateway.commons.util.Fields;

/**
 * @author Sunil
 * 
 */
public class Forwarder extends ActionSupport implements ServletRequestAware, SessionAware {

	private static Logger logger = LoggerFactory.getLogger(Forwarder.class.getName());
	private static final long serialVersionUID = 3512911775058957379L;

	HttpServletRequest httpRequest;
	private Map<String, Object> sessionMap;

	public Fields fieldMapProvider() {
		@SuppressWarnings("unchecked")
		Map<String, String[]> fieldMapObj = httpRequest.getParameterMap();
		Map<String, String> requestMap = new HashMap<String, String>();
		for (Entry<String, String[]> entry : fieldMapObj.entrySet()) {
			try {
				requestMap.put(entry.getKey(), entry.getValue()[0]);

			} catch (ClassCastException classCastException) {
				logger.error("Exception", classCastException);
			}
		}

		// Creating flag for new order transaction
		Fields fields = new Fields(requestMap);
		fields.removeInternalFields();
		return fields;
	}

	public HttpServletRequest getHttpRequest() {
		return httpRequest;
	}

	public void setHttpRequest(HttpServletRequest httpRequest) {
		this.httpRequest = httpRequest;
	}

	public void setSession(Map<String, Object> sessionMap) {
		this.setSessionMap(sessionMap);

	}

	public void setServletRequest(HttpServletRequest hReq) {
		this.httpRequest = hReq;
	}

	public Map<String, Object> getSessionMap() {
		return sessionMap;
	}

	public void setSessionMap(Map<String, Object> sessionMap) {
		this.sessionMap = sessionMap;
	}

}
