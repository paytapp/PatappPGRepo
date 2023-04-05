package com.paymentgateway.cashfree;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.CoinSwitchTransactionObject;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

/*
*@auther Vishal Yadav
*/

@RestController
public class CashFreeVPATransact {

	@Autowired
	private CashFreeVPAService cashFreeService;

	@Autowired
	private CashFreeVPARequestHandler cashFreeRequestHandler;

	
	private static Logger logger = LoggerFactory.getLogger(CashFreeVPATransact.class.getName());

		@RequestMapping(method = RequestMethod.POST, value = "/genrateToken", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
		public @ResponseBody Map<String, String> genrateToken(@RequestBody Map<String, String> reqmap) {
			Map<String, String> responseMap = new HashMap<String, String>();
			try {
				Fields fields = new Fields(reqmap);
				fields.clean();
				// Check Hash
				Boolean isHash = cashFreeService.validateHash(fields);
				if (!isHash) {
					responseMap.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.HASH_INVALID.getResponseMessage());
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.HASH_INVALID.getResponseCode());
					return responseMap;
				}
				// Validate fields
				Map<String, String> validationResponse = cashFreeService.validateFieldsToken(fields);
				if (!validationResponse.get(FieldType.RESPONSE_MESSAGE.getName()).equalsIgnoreCase("SUCCESS")
						&& !validationResponse.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase("000")) {
					responseMap = validationResponse;
					return responseMap;
				}
				fields = cashFreeRequestHandler.genrateToken(fields);
				return responseMap;
			} catch (Exception exception) {
				logger.error("Exception", exception);
				responseMap.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseCode());
				return responseMap;
			}
		}
	

		@RequestMapping(method = RequestMethod.POST, value = "/genrateVirtualAccount", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
		public @ResponseBody Map<String, String> genrateVitaulAccount(@RequestBody Map<String, String> reqmap) {
			Map<String, String> responseMap = new HashMap<String, String>();
			try {
				Fields fields = new Fields(reqmap);
				//fields.clean();
				 //Check Hash
//				Boolean isHash = cashFreeService.validateHash(fields);
//				if (!isHash) {
//					responseMap.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
//					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.HASH_INVALID.getResponseMessage());
//					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.HASH_INVALID.getResponseCode());
//					return responseMap;
//				}
				// Validate fields
				
				Map<String, String> validationResponse = cashFreeService.validateFieldsVirtualAccount(fields);
				if (!validationResponse.get(FieldType.RESPONSE_MESSAGE.getName()).equalsIgnoreCase("SUCCESS")
						&& !validationResponse.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase("000")) {
					responseMap = validationResponse;
					return responseMap;
				}

				fields = cashFreeRequestHandler.genrateToken(fields);
				responseMap = cashFreeRequestHandler.genrateVirtualAccountRequest(fields);
				return responseMap;
			} catch (Exception exception) {
				logger.error("Exception", exception);
				responseMap.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseCode());
				return responseMap;
			}
		}

	
	
		@RequestMapping(method = RequestMethod.POST, value = "/genrateNewVpa", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
		public @ResponseBody Map<String, String> genrateNewVpa(@RequestBody Map<String, String> reqmap) {
			Map<String, String> responseMap = new HashMap<String, String>();
			try {
				Fields fields = new Fields(reqmap);
				
				// Check Hash
				Boolean isHash = cashFreeService.validateHash(fields);
				if (!isHash) {
					responseMap.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.HASH_INVALID.getResponseMessage());
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.HASH_INVALID.getResponseCode());
					return responseMap;
				}
				// Validate fields
				Map<String, String> validationResponse = cashFreeService.validateFieldsVPA(fields);
				if (!validationResponse.get(FieldType.RESPONSE_MESSAGE.getName()).equalsIgnoreCase("SUCCESS")
						&& !validationResponse.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase("000")) {
					responseMap = validationResponse;
					return responseMap;
				}
				fields = cashFreeRequestHandler.genrateToken(fields);
				responseMap = cashFreeRequestHandler.genrateVPARequst(fields);
	
				return responseMap;
			} catch (Exception exception) {
				logger.error("Exception", exception);
				responseMap.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseCode());
				return responseMap;
			}
		}
	
	
		
		@RequestMapping(method = RequestMethod.POST, value = "/getQrCodeExistingVPA", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
		public @ResponseBody Map<String, String> getQrCodeExistingVPA(@RequestBody Map<String, String> reqmap) {
			Map<String, String> responseMap = new HashMap<String, String>();
			try {
				Fields fields = new Fields(reqmap);
				fields.clean();
				// Check Hash
     			Boolean isHash = cashFreeService.validateHash(fields);
				if (!isHash) {
					responseMap.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.HASH_INVALID.getResponseMessage());
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.HASH_INVALID.getResponseCode());
					return responseMap;
				}
				// Validate fields
				Map<String, String> validationResponse = cashFreeService.validateFieldsQRCode(fields);
				if (!validationResponse.get(FieldType.RESPONSE_MESSAGE.getName()).equalsIgnoreCase("SUCCESS")
						&& !validationResponse.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase("000")) {
					responseMap = validationResponse;
					return responseMap;
				}
				fields = cashFreeRequestHandler.genrateToken(fields);
				responseMap = cashFreeRequestHandler.genrateCreateQRCodeRequst(fields);
	
				return responseMap;
			} catch (Exception exception) {
				logger.error("Exception", exception);
				responseMap.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseCode());
				return responseMap;
			}
		}
	
	
		
		
		@RequestMapping(method = RequestMethod.POST, value = "/getCashFreeVirtualAccount", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
		public @ResponseBody Map<String, String> genrateCashFreeQRCode(@RequestBody Map<String, String> reqmap) {
			Map<String, String> responseMap = new HashMap<String, String>();
			try {
				Fields fields = new Fields(reqmap);
				
				// Check Hash
				Boolean isHash = cashFreeService.validateHash(fields);
				if (!isHash) {
					responseMap.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.HASH_INVALID.getResponseMessage());
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.HASH_INVALID.getResponseCode());
					return responseMap;
				}
				// Validate fields
				Map<String, String> validationResponse = cashFreeService.validateGenrateCashFreeQRCode(fields);
				if (!validationResponse.get(FieldType.RESPONSE_MESSAGE.getName()).equalsIgnoreCase("SUCCESS")
						&& !validationResponse.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase("000")) {
					responseMap = validationResponse;
					return responseMap;
				}
				
				fields = cashFreeRequestHandler.genrateToken(fields);
				if(fields.get(FieldType.STATUS.getName()).equalsIgnoreCase("ERROR")) {
					responseMap.put(FieldType.STATUS.getName(), fields.get(FieldType.STATUS.getName()));
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), fields.get(FieldType.RESPONSE_MESSAGE.getName()));
					responseMap.put(FieldType.RESPONSE_CODE.getName(), fields.get(FieldType.RESPONSE_CODE.getName()));
					return responseMap;
				}
				
				responseMap = cashFreeRequestHandler.genrateCashFreeQRCode(fields);
				
	
				return responseMap;
			} catch (Exception exception) {
				logger.error("Exception", exception);
				responseMap.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseCode());
				return responseMap;
			}
		}
	
	
		
		@RequestMapping(value = "/cashFreeQRTxnCallBank", method = RequestMethod.POST ,consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE ,produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
		public void icici3dsHandler(HttpServletRequest request, HttpServletResponse response) throws IOException  {
			//iciciResponseAction.iciciResponseHandler(request, response);
			cashFreeRequestHandler.federalResponseHandler(request, response);
		}
		
		@RequestMapping(method = RequestMethod.POST, value = "/cashfreeUpdateStatusVirtualAccount", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
		public @ResponseBody Map<String, String> statusUpdateVitaulAccount(@RequestBody Map<String, String> reqmap) {
			Map<String, String> responseMap = new HashMap<String, String>();
			try {
				Fields fields = new Fields(reqmap);

				// Validate fields
				
				Map<String, String> validationResponse = cashFreeService.validateFieldsVirtualAccount(fields);
				if (!validationResponse.get(FieldType.RESPONSE_MESSAGE.getName()).equalsIgnoreCase("SUCCESS")
						&& !validationResponse.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase("000")) {
					responseMap = validationResponse;
					return responseMap;
				}

				fields = cashFreeRequestHandler.genrateToken(fields);
				responseMap = cashFreeRequestHandler.vaUpdateStatusRequest(fields);
				logger.info("final response for Cashfree Update Status === "+responseMap);
				return responseMap;
			} catch (Exception exception) {
				logger.error("Exception", exception);
				responseMap.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseCode());
				logger.info("final response for Cashfree Update Status === "+responseMap);
				return responseMap;
			}
		}
	

}
