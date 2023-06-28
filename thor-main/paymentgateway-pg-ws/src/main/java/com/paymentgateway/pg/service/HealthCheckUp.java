package com.paymentgateway.pg.service;

import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.commons.util.FieldType;

@RestController
@CrossOrigin
public class HealthCheckUp {
	private static Logger logger = LoggerFactory.getLogger(HealthCheckUp.class.getName());



	@Autowired
	private HealthCheckUpPG_WS healthCheckUpPG_WS;

	
	@RequestMapping(method = RequestMethod.POST, value = "/healthCheckUpPGWS", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String healthCheckUpPgws(@RequestBody Map<String, String> reqmap) {
		JSONObject response = new JSONObject();
		try {
			String payId=reqmap.get(FieldType.PAY_ID.getName());
			response = new JSONObject(healthCheckUpPG_WS.FindPayId(payId));
		} catch (Exception exception) {
			logger.error("Exception HealthCheckUpPG_WS  >>>> ", exception);
			response.put("data", "400");
		}
		return response.toString();
	}
}