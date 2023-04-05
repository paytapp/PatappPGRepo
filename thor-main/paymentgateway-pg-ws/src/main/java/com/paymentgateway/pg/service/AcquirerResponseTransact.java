package com.paymentgateway.pg.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;



@RestController
@CrossOrigin
public class AcquirerResponseTransact  {
	
	private static Logger logger = LoggerFactory.getLogger(AcquirerResponseTransact.class.getName());
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/payu/callback")
	public @ResponseBody void payuCallbackHandler(@RequestBody Map<String, String> reqmap) {
		try {
			logger.info("Payu Callbak response ");
			
		} catch (Exception exception) {
			logger.error("Exception", exception);
			
		}

	}
	
}
