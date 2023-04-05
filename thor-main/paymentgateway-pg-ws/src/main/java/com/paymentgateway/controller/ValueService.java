package com.paymentgateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
//@Scope(value ="prototype")
public class ValueService {

	@Autowired
	UserDao userDao;
	
	public String getVal(String payId) {
		//UserDao userDao = new UserDao();
		User user = new User();
		user = userDao.findPayId(payId);
		
		String emailId = user.getEmailId();
		return emailId;
	}
}
