package com.paymentgateway.controller;

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

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Token;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.oneclick.TokenManager;
import com.paymentgateway.pg.core.pageintegrator.GeneralValidator;
import com.paymentgateway.pg.security.SecurityProcessor;
import com.paymentgateway.requestrouter.RequestRouter;

@RestController
public class TransactionController {
	private static Logger logger = LoggerFactory.getLogger(TransactionController.class.getName());
	@Autowired
	private RequestRouter router;

	@Autowired
	private TokenManager tokenManager;

	@Autowired
	private GeneralValidator generalValidator;

	@Autowired
	private SecurityProcessor securityProcessor;

	@RequestMapping(method = RequestMethod.POST, value = "/tokenManagerGetAll", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, Token> tokenManagerGetAll(@RequestBody Map<String, String> fields) {
		return tokenManager.getAll(fields);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/validatePaymentType", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String validatePaymentType(@RequestBody Fields fields) {
		try {
			generalValidator.validatePaymentType(fields);
			return "SUCCESS";
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			logger.error("Exception in validatePaymentType , exception = ", e);
			return "FAILURE";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Exception in validatePaymentType , exception = ", e);
			return "FAILURE";
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/removeSavedCard", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String removeSavedCard(@RequestBody Fields fields) {
		tokenManager.removeSavedCard(fields);
		return "SUCCESS";

	}

	@RequestMapping(method = RequestMethod.POST, value = "/addToken", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Token addToken(@RequestBody Map<String, String> fields) {
		try {

			Fields field = new Fields(fields);
			return tokenManager.addToken(field);

		} catch (SystemException e) {
			// TODO Auto-generated catch block
			logger.error("Exception in addToken , exception = ", e);
		}
		return null;

	}

	@RequestMapping(method = RequestMethod.POST, value = "/authenticate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public void transact(@RequestBody Map<String, String> reqMap) {

		try {
			Fields fields = new Fields(reqMap);
			securityProcessor.authenticate(fields);

		} catch (Exception exception) {
			logger.error("Exception in addToken , transact = ", exception);
			// Ideally this should be a non-reachable code
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/addAcquirerFields", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public void addAcquirerFields(@RequestBody Map<String, String> reqMap) {

		try {
			Fields fields = new Fields(reqMap);
			securityProcessor.addAcquirerFields(fields);

		} catch (Exception exception) {
			logger.error("Exception in addToken , addAcquirerFields = " + exception);
		}

	}
}
