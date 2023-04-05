package com.paymentgateway.pg.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;

@Service
public class HealthCheckUpPG_WS {

	private static Logger logger = LoggerFactory.getLogger(HealthCheckUpPG_WS.class.getName());
	
	@Autowired
	UserDao userDao;
	
	public String FindPayId(String payId) {
		JSONObject response = new JSONObject();
		response.put("data", "400");
		try {
			User user=userDao.findPayId(payId);
			if( user!=null && user.getPayId()!=null ) {
				response.put("data", "200");
				
			}
		} catch (Exception e) {
			logger.info("Health Checkup Pg-Ws Service in Excepation :----------", e);
			response.put("data", "400");
		}
		return response.toString();
	}
}
