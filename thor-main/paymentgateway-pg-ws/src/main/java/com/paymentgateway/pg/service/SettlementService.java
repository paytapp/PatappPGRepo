package com.paymentgateway.pg.service;

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
import com.paymentgateway.nodal.payout.NodalRequestHandler;

@RestController
public class SettlementService {

	private static Logger logger = LoggerFactory.getLogger(SettlementService.class.getName());

	@Autowired
	private NodalRequestHandler nodalRequestHandler;

	@RequestMapping(method = RequestMethod.POST, value = "/nodalPayout", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> nodalPayment(@RequestBody Map<String, String> reqmap) {
		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			Map<String, String> responseMap = nodalRequestHandler.process(fields);

			return responseMap;
		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception", exception);
			return null;
		}

	}

}
