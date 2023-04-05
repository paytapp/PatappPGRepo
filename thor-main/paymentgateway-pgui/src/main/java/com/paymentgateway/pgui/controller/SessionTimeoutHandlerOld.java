package com.paymentgateway.pgui.controller;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

/**
 * @author Sandeep
 *
 */

@Service
public class SessionTimeoutHandlerOld extends HttpServlet {

	private static final long serialVersionUID = -4972019992248063557L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {

		HttpSession session = request.getSession();

		// Invalidate the session and removes any attribute related to it
		session.invalidate();

		// Get an HttpSession related to this request, if no session exist don't
		// create a new one. This is just a check to see after invalidation the
		// session will be null.
		session = request.getSession(false);

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		doPost(request, response);
	}

}
