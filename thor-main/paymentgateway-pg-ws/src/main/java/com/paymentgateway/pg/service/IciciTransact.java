package com.paymentgateway.pg.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TxnType;
import com.paymentgateway.icici.composite.api.Constants;
import com.paymentgateway.icici.composite.api.IciciCommunicator;
import com.paymentgateway.icici.composite.api.IciciCompositeDao;
import com.paymentgateway.icici.composite.api.IciciResponseHandler;
import com.paymentgateway.icici.composite.api.IciciTransactionConverter;
import com.paymentgateway.pg.core.util.IciciUtil;

@RestController
public class IciciTransact {

	private static Logger logger = LoggerFactory.getLogger(IciciTransact.class.getName());

	@Autowired
	private IciciTransactionConverter iciciTransactionConverter;

	@Autowired
	private IciciCommunicator iciciCommunicator;

	@Autowired
	private IciciUtil iciciUtils;

	@Autowired
	private IciciResponseHandler iciciResponseHandler;
	
	@Autowired
	private IciciCompositeDao iciciCompositeDao;
	
	@Autowired
	private UserDao userDao;

	@RequestMapping(method = RequestMethod.POST, value = "/iciciIMPSProcessor", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> impsProcessPayment(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("IMPS Raw Request:");
			fields.clean();
			Map<String, String> responseMap = new HashMap<String, String>();
			boolean hashResult = iciciTransactionConverter.validateHash(fields);
			if (!hashResult) {
				fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
				return fields.getFields();
			}
			String request = iciciTransactionConverter.createIMPSRequest(fields);
			String response = iciciCommunicator.getIMPSResponse(request);
			responseMap = iciciResponseHandler.impsProcess(fields, response);
			return responseMap;
		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception in IMPS ProcessPayment", exception);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/iciciIMPSStatusEnq", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> impsStatusEnq(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("IMPS status enq Raw Request:");
			fields.clean();
			String request = iciciTransactionConverter.createIMPSStatusEnqRequest(fields);
			String response = iciciCommunicator.getIMPSStatusEnqResponse(request);
			Map<String, String> responseMap = iciciResponseHandler.impsStatusEnqProcess(fields, response);
			return responseMap;
		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception", exception);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/iciciCibProcessorNodal", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> cibProcess(@RequestBody Map<String, String> reqmap) {
		logger.info("Inside ICICI CIB Nodal Account Processor");
		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("ICICI CIB Raw Request: " + fields.getFields());
			fields.clean();

			String request = null;
			String response = null;
			String url = null;
			String apiKey = null;
			Map<String, String> responseMap = null;

			String requestType = fields.get(FieldType.REQUEST_TYPE.getName());

			switch (requestType) {

			
			case Constants.REQ_TRANSACTION:
				logger.info("inside cibProcess() " + Constants.REQ_TRANSACTION);
				if (!iciciTransactionConverter.cibTransactionValidateHash(fields)) {
					fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
					return fields.getFields();
				}
				// logger.info("Hash Validated");
				iciciTransactionConverter.getNodalDetails(fields);
				
				url = PropertiesManager.propertiesMap.get("CIB_TRANSACTION");
				apiKey = PropertiesManager.propertiesMap.get("NODAL_TXN_API_KEY");
				String priority = iciciTransactionConverter.getPriorityforCIBComposite(fields.get(FieldType.TXNTYPE.getName()));
				
				if(StringUtils.isNotBlank(priority)){
					request = iciciTransactionConverter.createRequestForTransaction(fields);
					response = iciciCommunicator.getCibCompositeResponse(request, url, fields, apiKey, priority);
					responseMap = iciciResponseHandler.cibTransactionResponseHandler(fields, response);
				}else{
					logger.info("Invalid txn type passed in request for CIB Transaction >>> "+fields.get(FieldType.TXNTYPE.getName()));
					
					fields.put(FieldType.RESPONSE_MESSAGE.getName(),ErrorType.INVALID_TXN_TYPE.getResponseMessage());
				}
				
				break;

			case Constants.REQ_TRANSACTION_INQUIRY:
				logger.info("inside " + Constants.REQ_TRANSACTION_INQUIRY);
				if (!iciciTransactionConverter.cibTransactionInqValidateHash(fields)) {
					fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
					return fields.getFields();
				}
				// logger.info("Hash Validated");
				iciciTransactionConverter.getNodalDetails(fields);
				
				iciciResponseHandler.findPrevFields(fields);

				if(fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase("IMPS")){
					url = PropertiesManager.propertiesMap.get("CIB_TRANSACTION_INQUIRY");
				}else{
					url = PropertiesManager.propertiesMap.get("CIB_TRANSACTION_INQUIRY_NEFT_RTGS");
				}
				
				apiKey = PropertiesManager.propertiesMap.get("NODAL_TXN_API_KEY");
				request = iciciTransactionConverter.createRequestForTransactionInquiry(fields);
				response = iciciCommunicator.getCibCompositeResponse(request, url, fields, apiKey, null);
				responseMap = iciciResponseHandler.cibTransactionStatusResponseHandler(fields, response);
				break;

			case Constants.REQ_ADD_BENE:
				logger.info("inside " + Constants.REQ_ADD_BENE);

				if (!iciciTransactionConverter.cibAddBenValidateHash(fields)) {
					fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
					return fields.getFields();
				}
				// logger.info("Hash Validated");
				

				if (!iciciResponseHandler.isDuplicateAccountNumberNodalBene(fields)) {

					iciciTransactionConverter.getNodalDetails(fields);

					
					fields.put(FieldType.REQUEST_TYPE.getName(),Constants.REQ_ADD_BENE);
					
					url = PropertiesManager.propertiesMap.get("CIB_BENE_ADDITION");
					apiKey = PropertiesManager.propertiesMap.get("Icici_Api_Key");
					request = iciciTransactionConverter.createRequestForAddBene(fields);
					response = iciciCommunicator.getCibCompositeResponse(request, url, fields, apiKey, null);
					responseMap = iciciResponseHandler.cibBeneResponseHandler(fields, response);

				} else {
					logger.info("return fields are " + fields.getFields());
					return fields.getFields();
				}

				break;

			case Constants.REQ_VALIDATE_BENE:
				logger.info("inside " + Constants.REQ_VALIDATE_BENE);

				if (!iciciTransactionConverter.cibBeneStatusValidateHash(fields)) {
					fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
					return fields.getFields();
				}
				// logger.info("Hash Validated");
				iciciTransactionConverter.getNodalDetails(fields);

				url = PropertiesManager.propertiesMap.get("CIB_BENE_VALIDATE");
				apiKey = PropertiesManager.propertiesMap.get("CIB_BENE_ADDITION");
				request = iciciTransactionConverter.createRequestForValidateBene(fields);
				response = iciciCommunicator.getCibCompositeResponse(request, url, fields, apiKey, null);
				responseMap = iciciResponseHandler.cibBeneStatusResponseHandler(fields, response);
				break;

			case Constants.REQ_ACCOUNT_STATEMENT:
				logger.info("inside " + Constants.REQ_ACCOUNT_STATEMENT);
				if (!iciciTransactionConverter.cibAccountStatementValidateHash(fields)) {
					fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
					return fields.getFields();
				}
				// logger.info("Hash Validated");
				iciciTransactionConverter.getNodalDetails(fields);

				url = PropertiesManager.propertiesMap.get("CIB_ACCOUNT_STATEMENT");
				request = iciciTransactionConverter.createRequestForAccountStatement(fields);
				response = iciciCommunicator.getCibResponse(request, url, fields);
				responseMap = iciciResponseHandler.cibAccountStatementResponseHandler(fields, response);
				break;

			case Constants.REQ_BALANCE_INQUIRY:
				logger.info("inside " + Constants.REQ_BALANCE_INQUIRY);
				if (!iciciTransactionConverter.cibBalInqValidateHash(fields)) {
					fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
					return fields.getFields();
				}
				// logger.info("Hash Validated");
				iciciTransactionConverter.getNodalDetails(fields);

				url = PropertiesManager.propertiesMap.get("CIB_BALANCE_INQUIRY");
				apiKey = PropertiesManager.propertiesMap.get("Icici_Api_Key");
				request = iciciTransactionConverter.createRequestForBalanceInquiry(fields);
				response = iciciCommunicator.getCibCompositeResponse(request, url, fields, apiKey, null);
				responseMap = iciciResponseHandler.cibBeneBalanceInqHandler(fields, response);
				break;
			}

			logger.info("Response of ICICI CIB " + responseMap);
			return responseMap;
		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception In CIB", exception);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/iciciCibProcessorCurrent", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> cibProcessCurrent(@RequestBody Map<String, String> reqmap) {
		logger.info("Inside ICICI CIB Current Account Processor");
		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("ICICI CIB Raw Request: " + fields.getFields());
			fields.clean();

			String request = null;
			String response = null;
			String url = null;
			Map<String, String> responseMap = null;

			String requestType = fields.get(FieldType.REQUEST_TYPE.getName());

			switch (requestType) {

			case Constants.REQ_REGISTER:
				logger.info("inside " + Constants.REQ_REGISTER);
				if (!iciciTransactionConverter.cibRegistrationValidateHash(fields)) {
					fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
					return fields.getFields();
				}
				// logger.info("Hash Validated");
				url = PropertiesManager.propertiesMap.get("CIB_REGISTRATION");

				request = iciciTransactionConverter.createRequestForRegistration(fields);
				response = iciciCommunicator.getCibResponse(request, url, fields);
				responseMap = iciciResponseHandler.cibRegResponseHandler(fields, response);
				break;

			case Constants.REQ_REGISTER_STATUS:
				logger.info("inside " + Constants.REQ_REGISTER_STATUS);
				if (!iciciTransactionConverter.cibRegistrationStatusValidateHash(fields)) {
					fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
					return fields.getFields();
				}
				// logger.info("Hash Validated");
				url = PropertiesManager.propertiesMap.get("CIB_REGISTRATION_STATUS");
				request = iciciTransactionConverter.createRequestForRegistrationStatus(fields);
				response = iciciCommunicator.getCibResponse(request, url, fields);
				responseMap = iciciResponseHandler.cibRegStatusResponseHandler(fields, response);
				break;

			case Constants.REQ_TRANSACTION:
				logger.info("inside " + Constants.REQ_TRANSACTION);

				if (!iciciTransactionConverter.cibTransactionValidateHash(fields)) {
					fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
					return fields.getFields();
				}
				// logger.info("Hash Validated");
				iciciTransactionConverter.getCurrentDetails(fields);

				url = PropertiesManager.propertiesMap.get("CIB_TRANSACTION");
				request = iciciTransactionConverter.createRequestForTransaction(fields);
				response = iciciCommunicator.getCibResponse(request, url, fields);
				responseMap = iciciResponseHandler.cibTransactionResponseHandler(fields, response);
				break;

			case Constants.REQ_TRANSACTION_INQUIRY:
				logger.info("inside " + Constants.REQ_TRANSACTION_INQUIRY);
				if (!iciciTransactionConverter.cibTransactionInqValidateHash(fields)) {
					fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
					return fields.getFields();
				}
				// logger.info("Hash Validated");
				iciciTransactionConverter.getCurrentDetails(fields);

				url = PropertiesManager.propertiesMap.get("CIB_TRANSACTION_INQUIRY");
				request = iciciTransactionConverter.createRequestForTransactionInquiry(fields);
				response = iciciCommunicator.getCibResponse(request, url, fields);
				responseMap = iciciResponseHandler.cibTransactionStatusResponseHandler(fields, response);
				break;

			case Constants.REQ_ADD_BENE:
				logger.info("inside " + Constants.REQ_ADD_BENE);

				if (!iciciTransactionConverter.cibAddBenValidateHash(fields)) {
					fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
					return fields.getFields();
				}
				// logger.info("Hash Validated");
				iciciTransactionConverter.getCurrentDetails(fields);

				url = PropertiesManager.propertiesMap.get("CIB_BENE_ADDITION");
				request = iciciTransactionConverter.createRequestForAddBene(fields);
				response = iciciCommunicator.getCibResponse(request, url, fields);
				responseMap = iciciResponseHandler.cibBeneResponseHandler(fields, response);
				break;

			case Constants.REQ_VALIDATE_BENE:
				logger.info("inside " + Constants.REQ_VALIDATE_BENE);
				if (!iciciTransactionConverter.cibBeneStatusValidateHash(fields)) {
					fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
					return fields.getFields();
				}
				// logger.info("Hash Validated");
				iciciTransactionConverter.getCurrentDetails(fields);

				url = PropertiesManager.propertiesMap.get("CIB_BENE_VALIDATE");
				request = iciciTransactionConverter.createRequestForValidateBene(fields);
				response = iciciCommunicator.getCibResponse(request, url, fields);
				responseMap = iciciResponseHandler.cibBeneStatusResponseHandler(fields, response);
				break;

			case Constants.REQ_ACCOUNT_STATEMENT:
				logger.info("inside " + Constants.REQ_ACCOUNT_STATEMENT);
				if (!iciciTransactionConverter.cibAccountStatementValidateHash(fields)) {
					fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
					return fields.getFields();
				}
				// logger.info("Hash Validated");
				iciciTransactionConverter.getCurrentDetails(fields);

				url = PropertiesManager.propertiesMap.get("CIB_ACCOUNT_STATEMENT");
				request = iciciTransactionConverter.createRequestForAccountStatement(fields);
				response = iciciCommunicator.getCibResponse(request, url, fields);
				responseMap = iciciResponseHandler.cibAccountStatementResponseHandler(fields, response);
				break;

			case Constants.REQ_BALANCE_INQUIRY:
				logger.info("inside " + Constants.REQ_BALANCE_INQUIRY);
				if (!iciciTransactionConverter.cibBalInqValidateHash(fields)) {
					fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
					return fields.getFields();
				}
				// logger.info("Hash Validated");
				iciciTransactionConverter.getCurrentDetails(fields);

				url = PropertiesManager.propertiesMap.get("CIB_BALANCE_INQUIRY");
				request = iciciTransactionConverter.createRequestForBalanceInquiry(fields);
				response = iciciCommunicator.getCibResponse(request, url, fields);
				responseMap = iciciResponseHandler.cibBeneBalanceInqHandler(fields, response);
				break;
			}

			logger.info("Response of ICICI CIB " + responseMap);
			return responseMap;
		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception In CIB", exception);
			return null;
		}

	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/iciciCibTransactionNodalTopup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> cibTopupTransactionProcess(@RequestBody Map<String, String> reqmap) {
		logger.info("Inside ICICI CIB Nodal Account Topup Process");
		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("ICICI CIB Raw Request: " + fields.getFields());
			fields.clean();

			String request = null;
			String response = null;
			String url = null;
			Map<String, String> responseMap = null;

			String requestType = fields.get(FieldType.REQUEST_TYPE.getName());

			switch (requestType) {

			case Constants.REQ_TRANSACTION:
				logger.info("inside " + Constants.REQ_TRANSACTION);
				if (!iciciTransactionConverter.cibTransactionValidateHash(fields)) {
					fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
					return fields.getFields();
				}
				// logger.info("Hash Validated");
				iciciTransactionConverter.getNodalDetails(fields);
				
				User user=userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
				
				if(user.isAllowNodalPayoutFlag() ){
					if(iciciCompositeDao.isBalanceAvailable(fields)){
						iciciCompositeDao.lockBalanceAmount(fields);
					}else{
						fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
						fields.put(FieldType.PG_TXN_MESSAGE.getName(),
								ErrorType.DECLINED_BY_INSUFFICIENT_BALANCE.getResponseMessage());
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DECLINED_BY_INSUFFICIENT_BALANCE.getResponseCode());
						return fields.getFields();
					}
				}else{
					fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.ACCESS_DENIED.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACCESS_DENIED.getResponseCode());
					return fields.getFields();
				}

				url = PropertiesManager.propertiesMap.get("CIB_TRANSACTION");
				request = iciciTransactionConverter.createRequestForTransaction(fields);
				response = iciciCommunicator.getCibResponse(request, url, fields);
				responseMap = iciciResponseHandler.cibTransactionResponseHandler(fields, response, user);
				break;

			case Constants.REQ_TRANSACTION_INQUIRY:
				logger.info("inside " + Constants.REQ_TRANSACTION_INQUIRY);
				if (!iciciTransactionConverter.cibTransactionInqValidateHash(fields)) {
					fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
					return fields.getFields();
				}
				// logger.info("Hash Validated");
				iciciTransactionConverter.getNodalDetails(fields);

				User userMerchant=userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
				
				url = PropertiesManager.propertiesMap.get("CIB_TRANSACTION_INQUIRY");
				request = iciciTransactionConverter.createRequestForTransactionInquiry(fields);
				response = iciciCommunicator.getCibResponse(request, url, fields);
				responseMap = iciciResponseHandler.cibTransactionStatusResponseHandler(fields, response, userMerchant);
				break;

			}

			logger.info("Response of ICICI CIB " + responseMap);
			return responseMap;
		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception In CIB", exception);
			return null;
		}

	}

	/*@RequestMapping(method = RequestMethod.POST, value = "/iciciECollectionProcessor", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public @ResponseBody String oldProcessECollection(@RequestBody String reqmap) {
		logger.info(" inside processECollection(), encrypted Request recieved : " + reqmap);
		String encryptedResponse = null;
		try {
			long virtualAccountNo = 0L;
			String decryptedString = iciciUtils.decrypt(reqmap);
			JSONObject decryptedJsonResponse = new JSONObject(decryptedString);
			logger.info("response receive from bank >>> " + decryptedJsonResponse.toString());
			if (decryptedString.contains("VirtualACCode")
					&& StringUtils.isNotBlank(decryptedJsonResponse.get("VirtualACCode").toString())) {
				virtualAccountNo = Long
						.valueOf(decryptedJsonResponse.get("VirtualACCode").toString().substring(3, 11));
			}
			if (virtualAccountNo >= 50000000 && virtualAccountNo <= 69999999) {
				logger.info("response receive from bank for coinSwitch user with virtual Account no. >>> "
						+ decryptedJsonResponse.get("VirtualACCode").toString());
				encryptedResponse = iciciResponseHandler.coinSwitchCustomersResponseHandler(decryptedJsonResponse);

			} else {
				encryptedResponse = iciciResponseHandler.eCollectionResponseHandler(decryptedJsonResponse);
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
		return encryptedResponse;
	}*/

	@RequestMapping(method = RequestMethod.POST, value = "/processECollCustom", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String processECollCustom(@RequestBody String reqmap) {
		logger.info("Inside processECollCustom(), encrypted Request recieved : " + reqmap);
		logger.info("This request is for Bebuoy merchant, using the bebuoy certificates for decryption");
		String encryptedResponse = null;
		try {

			String decryptedString = iciciUtils.decryptCustom(reqmap);
			JSONObject decryptedJsonResponse = new JSONObject(decryptedString);

			encryptedResponse = iciciResponseHandler.eCollectionResponseHandlerCustom(decryptedJsonResponse);

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
		return encryptedResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/iciciECollectionProcessor", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public @ResponseBody String eCollection(@RequestBody String reqmap) {
		logger.info(" insode processECollection(), Ecryptrd Request recieved : " + reqmap);
		String encryptString = null;
		try {

			encryptString = iciciUtils.eCollectionEncrypt(reqmap);
			logger.info("Response of ECollection EncryptString" + encryptString);

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
		return encryptString;
	}

	/*@RequestMapping(method = RequestMethod.POST, value = "/iciciECollectionProcessorHybrid", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String processECollection(@RequestBody String reqmap) {
		logger.info(" inside processECollection(), encrypted Request recieved : " + reqmap);
		String encryptedResponse = null;
		try {
			long virtualAccountNo = 0L;
			String decryptedString = iciciUtils.compositeApiDecryption(reqmap, false);
			JSONObject decryptedJsonResponse = new JSONObject(decryptedString);
			logger.info("response receive from bank >>> " + decryptedJsonResponse.toString());
			if (decryptedString.contains("VirtualAccountNumber")
					&& StringUtils.isNotBlank(decryptedJsonResponse.get("VirtualAccountNumber").toString())) {
				virtualAccountNo = Long
						.valueOf(decryptedJsonResponse.get("VirtualAccountNumber").toString().substring(3, 11));
			}
			if (virtualAccountNo >= 50000000 && virtualAccountNo <= 69999999) {
				logger.info("response receive from bank for coinSwitch user with virtual Account no. >>> "
						+ decryptedJsonResponse.get("VirtualAccountNumber").toString());
				encryptedResponse = iciciResponseHandler.coinSwitchCustomersResponseHandlerHybrid(decryptedJsonResponse);

			} else {
				encryptedResponse = iciciResponseHandler.eCollectionResponseHandlerComposite(decryptedJsonResponse);
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
		return encryptedResponse;
	}*/

}
