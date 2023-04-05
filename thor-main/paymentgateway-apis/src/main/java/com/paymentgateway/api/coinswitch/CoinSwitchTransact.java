package com.paymentgateway.api.coinswitch;

import java.util.HashMap;
import java.util.List;
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

import com.google.gson.Gson;
import com.paymentgateway.commons.dao.VirtualAccountNumberGeneratorDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.CoinSwitchTransactionObject;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.VirtualAccountNumberGenerator;

/*
*@auther Sandeep Sharma
*/

@RestController
public class CoinSwitchTransact {

	@Autowired
	private CoinSwitchService coinSwitchService;

	@Autowired
	private CoinSwitchUserRegistration coinSwitchUserRegistration;

	@Autowired
	private CoinSwitchUserTransaction coinSwitchUserTransaction;

	@Autowired
	private VirtualAccountNumberGenerator virtualAccountNumberGenerator;

	@Autowired
	private VirtualAccountNumberGeneratorDao virtualAccountNumberGeneratorDao;

	private static Logger logger = LoggerFactory.getLogger(CoinSwitchTransact.class.getName());

	@RequestMapping(method = RequestMethod.POST, value = "/userReg", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> userReg(@RequestBody Map<String, String> reqmap) {
		Map<String, String> responseMap = new HashMap<String, String>();
		try {
			Fields fields = new Fields(reqmap);
			fields.clean();
			// Check Hash
			Boolean isHash = coinSwitchService.validateHash(fields);
			if (!isHash) {
				responseMap.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.HASH_INVALID.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.HASH_INVALID.getResponseCode());
				return responseMap;
			}
			// Validate fields
			Map<String, String> validationResponse = coinSwitchService.validateFields(fields);
			if (!validationResponse.get(FieldType.RESPONSE_MESSAGE.getName()).equalsIgnoreCase("SUCCESS")
					&& !validationResponse.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase("000")) {
				responseMap = validationResponse;
				return responseMap;
			}
			// Checking for duplicate user
			Boolean isExist = coinSwitchService.checkUserExist(fields);
			if (isExist) {
				responseMap.put(FieldType.STATUS.getName(), StatusType.DUPLICATE.getName());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DUPLICATE_USER.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE_USER.getResponseCode());
				return responseMap;
			}
			// Request for generating VA in cashfree and getting response
			// From response we are getting virtual account no and virtual account ifsc save
			// those params in DB
			responseMap.clear();
			String virtualAccountNo = virtualAccountNumberGenerator.getNewVirtualAccountNo();
			fields.put(FieldType.CUST_ID.getName(), virtualAccountNo);
			long accountNo = Long.valueOf(fields.get(FieldType.CUST_ID.getName())) + 1;
			String newVirtualAccountNo = String.valueOf(String.format("%08d", accountNo));
			virtualAccountNumberGeneratorDao.updateLatestVirtualAccountNo(fields.get(FieldType.CUST_ID.getName()),
					newVirtualAccountNo);

			responseMap = coinSwitchService.cashfreeAccountGeneration(fields);
			if (responseMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(ErrorType.SUCCESS.getResponseMessage())
					&& responseMap.get(FieldType.RESPONSE_MESSAGE.getName())
							.equalsIgnoreCase(ErrorType.SUCCESS.getResponseMessage())) {
				fields.put(FieldType.VIRTUAL_ACC_NUM.getName(),
						responseMap.get(FieldType.CUSTOMER_ACCOUNT_NO.getName()));
				fields.put(FieldType.VIRTUAL_ACC_IFSC.getName(), responseMap.get(FieldType.IFSC_CODE.getName()));
				responseMap.clear();
				responseMap = coinSwitchUserRegistration.saveUserDetail(fields);
				return responseMap;

			} else {
				return responseMap;
			}

		} catch (Exception exception) {
			logger.error("Exception", exception);
			responseMap.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
			responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseCode());
			return responseMap;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/userTransactions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String userTransactions(@RequestBody Map<String, String> reqmap) {
		Map<String, String> responseMap = new HashMap<String, String>();
		String response = "";
		Gson gson = new Gson();
		try {
			Fields fields = new Fields(reqmap);
			fields.clean();
			Boolean isHash = coinSwitchService.validateHash(fields);
			if (!isHash) {
				responseMap.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.HASH_INVALID.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.HASH_INVALID.getResponseCode());
				response = gson.toJson(responseMap);
				return response;
			}
			Map<String, String> validationResponse = coinSwitchService.validateUserTransactionFields(fields);
			if (!validationResponse.get(FieldType.RESPONSE_MESSAGE.getName()).equalsIgnoreCase("SUCCESS")
					&& !validationResponse.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase("000")) {
				response = gson.toJson(validationResponse);
				return response;
			}
			Boolean isExist = coinSwitchService.checkUserExist(fields);
			if (!isExist) {
				responseMap.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_USER.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_USER.getResponseCode());
				response = gson.toJson(responseMap);
				return response;
			}
			List<CoinSwitchTransactionObject> getResponse = coinSwitchUserTransaction.fetchCustomerTransaction(fields);
			response = gson.toJson(getResponse);

			return response;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			responseMap.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
			responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseCode());
			response = gson.toJson(responseMap);
			return response;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/updateUserStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> updateUserStatus(@RequestBody Map<String, String> reqmap) {
		Map<String, String> responseMap = new HashMap<String, String>();
		try {
			Fields fields = new Fields(reqmap);
			fields.clean();
			Boolean isHash = coinSwitchService.validateHash(fields);
			if (!isHash) {
				responseMap.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.HASH_INVALID.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.HASH_INVALID.getResponseCode());
				return responseMap;
			}
			// Validate fields
			Map<String, String> validationResponse = coinSwitchService.validateUpdateStatusFields(fields);
			if (!validationResponse.get(FieldType.RESPONSE_MESSAGE.getName()).equalsIgnoreCase("SUCCESS")
					&& !validationResponse.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase("000")) {
				responseMap = validationResponse;
				return responseMap;
			}
			Boolean isExist = coinSwitchService.checkUserExist(fields);
			if (!isExist) {
				responseMap.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_USER.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_USER.getResponseCode());
				return responseMap;
			}
			responseMap = coinSwitchUserRegistration.updateUserStatus(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			responseMap.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
			responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseCode());
			return responseMap;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/updateUserDetail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> updateUserDetail(@RequestBody Map<String, String> reqmap) {
		Map<String, String> responseMap = new HashMap<String, String>();
		try {
			Fields fields = new Fields(reqmap);
			fields.clean();
			Boolean isHash = coinSwitchService.validateHash(fields);
			if (!isHash) {
				responseMap.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.HASH_INVALID.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.HASH_INVALID.getResponseCode());
				return responseMap;
			}
			Map<String, String> validationResponse = coinSwitchService.validateUpdateUserFields(fields);
			if (!validationResponse.get(FieldType.RESPONSE_MESSAGE.getName()).equalsIgnoreCase("SUCCESS")
					&& !validationResponse.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase("000")) {
				responseMap = validationResponse;
				return responseMap;
			}
			Boolean isExist = coinSwitchService.checkUserExist(fields);
			if (!isExist) {
				responseMap.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_USER.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_USER.getResponseCode());
				return responseMap;
			}
			responseMap = coinSwitchUserRegistration.updateUserBankDetails(fields);
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			responseMap.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
			responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseCode());
			return responseMap;
		}
	}

}
