package com.paymentgateway.crm.actionBeans;

import javax.servlet.http.HttpServlet;

import org.apache.struts2.dispatcher.SessionMap;

public class SessionCleaner extends HttpServlet {

	private static final long serialVersionUID = -4977656370661415299L;

	public static void cleanSession(SessionMap<String, Object> sessionMap) {
		sessionMap.clear();
		sessionMap.invalidate();
	}
}
