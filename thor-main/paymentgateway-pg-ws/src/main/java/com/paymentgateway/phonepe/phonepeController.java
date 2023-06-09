package com.paymentgateway.phonepe;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.commons.util.Fields;

/*
 * Author Arvind Chaturvedi
 * 
 * For Phonepe payment 
 */
@RestController
public class phonepeController {
	private static Logger logger = LoggerFactory.getLogger(phonepeController.class.getName());

	@Autowired
	phonepeService phonepeSeerviceObj;
	
	
	@RequestMapping(method = RequestMethod.POST, value = "phonepe/payment", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> paymentApi(@RequestBody Map<String, String> reqmap) {
		Map<String, String> responseMap = new HashMap<String, String>();
		
		try {
			responseMap = phonepeSeerviceObj.getResponse(reqmap);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}

	}
}
