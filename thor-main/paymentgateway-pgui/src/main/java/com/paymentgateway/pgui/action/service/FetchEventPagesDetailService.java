package com.paymentgateway.pgui.action.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.EventPagesDao;
import com.paymentgateway.commons.user.EventPages;

/**
 * @author Rahul
 *
 */

@Service
public class FetchEventPagesDetailService {
	
	private static Logger logger = LoggerFactory.getLogger(FetchEventPagesDetailService.class.getName());
	
	@Autowired
	private EventPagesDao EventPagesDao;
	
	public JSONObject prepareEventDetails(String uniqueNo, String payId) {
		EventPages userInfo = EventPagesDao.fetchuserDetails(uniqueNo, payId);
		JSONObject requestParameterJson = new JSONObject();
		requestParameterJson.put("name", userInfo.getName());
		requestParameterJson.put("emailId", userInfo.getEmailId());
		requestParameterJson.put("mobileNo", userInfo.getMobileNo());
		requestParameterJson.put("amout", userInfo.getAmount());
		requestParameterJson.put("address", userInfo.getAddress());
		requestParameterJson.put("remarks", userInfo.getRemarks());
		requestParameterJson.put("uniqueNo", userInfo.getUniqueNo());
		logger.info("Event pages user info JSON : " + requestParameterJson);
		return requestParameterJson;
		
	}

}
