package com.paymentgateway.pgui.action.beans;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

public class SessionCleaner extends HttpServlet {

	private static final long serialVersionUID = -4977656370661415299L;

	public static void cleanSession(HttpSession sessionMap) {
		sessionMap.invalidate();		
	}
}
