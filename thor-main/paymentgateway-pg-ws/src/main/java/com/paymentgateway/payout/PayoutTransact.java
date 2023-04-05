package com.paymentgateway.payout;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

@RestController
public class PayoutTransact {

	private static Logger logger = LoggerFactory.getLogger(PayoutTransact.class.getName());

	@Autowired
	private MerchantPayoutHandler merchantPayoutHandler;

	@Autowired
	private VendorPayoutHandler vendorPayoutHandler;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private VendorPayoutDao vendorPayoutDao;



	@RequestMapping(method = RequestMethod.POST, value = "/beneVerification", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> beneVerification(@RequestBody Map<String, String> reqmap)
			throws SystemException {
		Map<String, String> responseMap = new HashMap<String, String>();

		Fields fields = new Fields(reqmap);
		fields.logAllFields("Raw Request:");
		fields.clean();

		try {

			if (!vendorPayoutHandler.validateHashForApi(fields)) {

				logger.info("Invalid Hash found for Order Id is " + fields.get(FieldType.ORDER_ID.getName()));

				fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getResponseCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_HASH.getResponseMessage());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Auththentication Failed " + fields.getFields());
				return fields.getFields();
			}

			if (vendorPayoutHandler.isMendatoryFieldEmptyForBeneVerification(fields)) {

				logger.info("Invalid Fields or Empty Mendatory Fields found for Order Id is "
						+ fields.get(FieldType.ORDER_ID.getName()));

				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Fields Are " + fields.getFields());

				return fields.getFields();
			}
			
			//Flag for verify offline account details of MPA.
			if(StringUtils.isBlank(fields.get(FieldType.MPA_FLAG.getName()))){
				if (!vendorPayoutHandler.isMerchantAllowed(fields)) {
	
					logger.info("Merchant is Not Allowed for Access this API found for PAY ID is "
							+ fields.get(FieldType.PAY_ID.getName()));
	
					fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACCESS_DENIED.getResponseCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACCESS_DENIED.getResponseMessage());
					fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
					logger.info("Merchant Does not Have Permission " + fields.getFields());
					return fields.getFields();
				}
			}else{
				fields.remove(FieldType.MPA_FLAG.getName());
			}

			logger.info("Checking Duplicate Order Id");

			vendorPayoutHandler.checkDuplicateOrderId(fields);

			if (fields.get(FieldType.DUPLICATE_YN.getName()).equalsIgnoreCase("Y")) {
				fields.remove(FieldType.DUPLICATE_YN.getName());
				logger.info("Duplicate order Id found Order Id is " + fields.get(FieldType.ORDER_ID.getName()));

				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE_ORDER_ID.getResponseCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DUPLICATE_ORDER_ID.getResponseMessage());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Duplicate Order_ID " + fields.getFields());
				return fields.getFields();
			}

			fields.remove(FieldType.DUPLICATE_YN.getName());
			
			fields.put(FieldType.BENE_NAME_REQUEST.getName(), fields.get(FieldType.BENE_NAME.getName()));

			if (vendorPayoutHandler.isDuplicateAccountNumber(fields)) {

				logger.info("Duplicate Account Number Found for Account No. is "
						+ fields.get(FieldType.BENE_ACCOUNT_NO.getName()));
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), fields.get(FieldType.PG_TXN_MESSAGE.getName()));
				vendorPayoutHandler.clearFieldsForBeneVerification(fields);
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

				logger.info("Duplicate Account number " + fields.getFields());
				return fields.getFields();
			}

			fields.put(FieldType.USER_TYPE.getName(), "Verification");

			vendorPayoutHandler.getBeneVerificationAmount(fields);

			

			// communication with bank
			vendorPayoutHandler.communicateCompositeTransaction(fields);

			if (StringUtils.isNotBlank(fields.get(FieldType.BENE_NAME_REQUEST.getName()))) {
				if (fields.get(FieldType.BENE_NAME_REQUEST.getName())
						.equalsIgnoreCase(fields.get(FieldType.BENE_NAME.getName()))) {
					fields.remove(FieldType.BENE_NAME_REQUEST.getName());
				}
			}

			// String request =
			// iciciTransactionConverter.createIMPSRequest(fields);
			// String response = iciciCommunicator.getIMPSResponse(request);
			// responseMap = iciciResponseHandler.impsProcess(fields, response);

			if (StringUtils.isNoneBlank(fields.get(FieldType.PG_TXN_MESSAGE.getName()))) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), fields.get(FieldType.PG_TXN_MESSAGE.getName()));
			}
			vendorPayoutHandler.clearFieldsForBeneVerification(fields);

			if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase("Captured")) {
				fields.put(FieldType.STATUS.getName(), StatusType.VERIFIED.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
			} else if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase("Timeout")) {
				fields.put(FieldType.STATUS.getName(), StatusType.PENDING.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PENDING.getCode());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.USER_NOT_FOUND.getCode());
			}

			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

			logger.info("Response Fields to Merchant " + fields.getFields());
			return fields.getFields();
		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception in API", exception);
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.COMMON_ERROR.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.COMMON_ERROR.getResponseCode());
			logger.info("Response Fields to Merchant " + fields.getFields());
		}finally {
			vendorPayoutHandler.clearFieldsForBeneVerification(fields);
		}

		return fields.getFields();

	}

	@RequestMapping(method = RequestMethod.POST, value = "/beneUpiVerification", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> beneUpiVerification(@RequestBody Map<String, String> reqmap)
			throws SystemException {
		Map<String, String> responseMap = new HashMap<String, String>();

		Fields fields = new Fields(reqmap);
		fields.logAllFields("Raw Request:");
		fields.clean();

		try {

			if (!vendorPayoutHandler.validateHashForApi(fields)) {

				logger.info("Invalid Hash found for Order Id is " + fields.get(FieldType.ORDER_ID.getName()));

				fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getResponseCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_HASH.getResponseMessage());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				return fields.getFields();
			}

			if (vendorPayoutHandler.isUpiMendatoryFieldEmptyForBeneVerification(fields)) {

				logger.info("Invalid Fields or Empty Mendatory Fields found for Order Id is "
						+ fields.get(FieldType.ORDER_ID.getName()));
				logger.info("Fields Are " + fields.getFields());

				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				return fields.getFields();
			}

			if (!vendorPayoutHandler.isVpaVerificationMerchantAllowed(fields)) {

				logger.info("Merchant is Not Allowed for Access this API found for PAY ID is "
						+ fields.get(FieldType.PAY_ID.getName()));

				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACCESS_DENIED.getResponseCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACCESS_DENIED.getResponseMessage());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				return fields.getFields();
			}

			logger.info("Checking Duplicate Order Id");

			vendorPayoutHandler.checkDuplicateVpaOrderId(fields);

			if (fields.get(FieldType.DUPLICATE_YN.getName()).equalsIgnoreCase("Y")) {
				fields.remove(FieldType.DUPLICATE_YN.getName());
				logger.info("Duplicate order Id found Order Id is " + fields.get(FieldType.ORDER_ID.getName()));

				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE_ORDER_ID.getResponseCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DUPLICATE_ORDER_ID.getResponseMessage());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				return fields.getFields();
			}

			fields.remove(FieldType.DUPLICATE_YN.getName());

			if (vendorPayoutHandler.isDuplicateVPA(fields)) {

				logger.info("Duplicate VPA Found for VPA is " + fields.get(FieldType.PAYER_ADDRESS.getName()));
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), fields.get(FieldType.PG_TXN_MESSAGE.getName()));
				vendorPayoutHandler.clearFieldsForBeneVerification(fields);

				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Duplicate VPA Found for Fields are " + fields.getFields());
				return fields.getFields();
			}

			// communication with bank
			vendorPayoutHandler.communicateVpaVerification(fields);

			if (StringUtils.isNoneBlank(fields.get(FieldType.PG_TXN_MESSAGE.getName()))) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), fields.get(FieldType.PG_TXN_MESSAGE.getName()));
			}
			vendorPayoutHandler.clearFieldsForVPAVerification(fields);

			if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase("Captured")) {
				fields.put(FieldType.STATUS.getName(), StatusType.VERIFIED.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
			} else if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase("Timeout")) {
				fields.put(FieldType.STATUS.getName(), StatusType.PENDING.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PENDING.getCode());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.USER_NOT_FOUND.getCode());
			}

			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

			logger.info("Response Fields to Merchant " + fields.getFields());
			return fields.getFields();
		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception in API", exception);
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.COMMON_ERROR.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.COMMON_ERROR.getResponseCode());
			logger.info("Response Fields to Merchant " + fields.getFields());
		}finally {
			vendorPayoutHandler.clearFieldsForVPAVerification(fields);
		}

		return fields.getFields();

	}

	@RequestMapping(method = RequestMethod.POST, value = "/beneStatusEnquiry", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> beneVerificationStatusEnquiry(@RequestBody Map<String, String> reqmap)
			throws SystemException {

		Fields fields = new Fields(reqmap);
		fields.logAllFields("Raw Request:");
		fields.clean();

		try {

			if (!vendorPayoutHandler.validateHashForApi(fields)) {

				logger.info("Invalid Hash found for Order Id is " + fields.get(FieldType.ORDER_ID.getName()));

				fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getResponseCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_HASH.getResponseMessage());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				return fields.getFields();
			}

			vendorPayoutHandler.checkBeneVerificationStatus(fields);
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			return fields.getFields();
		} catch (Exception e) {
			// Ideally this should be a non-reachable code
			logger.error("Exception in API", e);
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.COMMON_ERROR.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.COMMON_ERROR.getResponseCode());
		}

		return fields.getFields();

	}

	@RequestMapping(method = RequestMethod.POST, value = "/merchantInitiatedDirect", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> merchantInitiatedDirectTransaction(
			@RequestBody Map<String, String> reqmap) {
		Map<String, String> responseMap = new HashMap<String, String>();
		Fields fields = new Fields(reqmap);
		try {
			
			fields.logAllFields("Raw Request:");
			fields.clean();

			if (!vendorPayoutHandler.validateHashForApi(fields)) {
				
                if(StringUtils.isBlank(fields.get(FieldType.RESPONSE_CODE.getName()))){
                    logger.info("Invalid Hash found for Order Id is " + fields.get(FieldType.ORDER_ID.getName()));
    
                    fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
                    fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
                    fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
    
                    fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
                }

				
				return fields.getFields();
			}

			if (StringUtils.isBlank(fields.get(FieldType.BENE_ACCOUNT_NO.getName()))) {
				if (vendorPayoutHandler.isMandatoryFieldEmptyForUpi(fields)) {
					logger.info("Invalid Fields or Empty Mandatory Fields found for Order Id is "
							+ fields.get(FieldType.ORDER_ID.getName()));
					fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
					return fields.getFields();
				}

			} else {
				if (vendorPayoutHandler.isMendatoryFieldEmpty(fields)) {
					logger.info("Invalid Fields or Empty Mendatory Fields found for Order Id is "
							+ fields.get(FieldType.ORDER_ID.getName()));
					fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
					return fields.getFields();
				}
			}

//			if (!vendorPayoutHandler.isPayIdValid(fields)) {
//
//				logger.info("Invalid PayId " + fields.get(FieldType.PAY_ID.getName()));
//
//				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
//				fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
//				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseCode());
//
//				return fields.getFields();
//			}

			if (!vendorPayoutHandler.isDirectTransctionAllowed(fields)) {

				logger.info("Merchant is Not Allowed for Access this API found for PAY ID is "
						+ fields.get(FieldType.PAY_ID.getName()));

				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACCESS_DENIED.getResponseCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACCESS_DENIED.getResponseMessage());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				return fields.getFields();
			}

			logger.info("Checking Duplicate Order Id");

			vendorPayoutHandler.checkDuplicateOrderIdPayout(fields);

			if (fields.get(FieldType.DUPLICATE_YN.getName()).equalsIgnoreCase("Y")) {
				fields.remove(FieldType.DUPLICATE_YN.getName());
				logger.info("Duplicate order Id found Order Id is " + fields.get(FieldType.ORDER_ID.getName()));

				fields.put(FieldType.STATUS.getName(), StatusType.DUPLICATE.getName());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.DUPLICATE_ORDER_ID.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE_ORDER_ID.getResponseCode());

				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				return fields.getFields();
			}

			fields.remove(FieldType.DUPLICATE_YN.getName());

			// amount validation
			String amt = fields.get(FieldType.AMOUNT.getName());
			if ((amt.length() >= 3) && StringUtils.isNotBlank(amt) && !amt.contains(".")
					&& StringUtils.isNumeric(amt)) {
			} else {

				logger.info("Invalid Amount by Order Id : " + fields.get(FieldType.ORDER_ID.getName())
						+ ", and Pay Id : " + fields.get(FieldType.ORDER_ID.getName()));
				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.INVALID_AMOUNT.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_AMOUNT.getResponseCode());

				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				return fields.getFields();
			}

			if(vendorPayoutHandler.purposeCheck(fields)) {
				logger.info("Purpose chack for Pay Id " + fields.get(FieldType.PAY_ID.getName()));

				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(),ErrorType.INVALID_PAYOUT_PURPOSE.getResponseMessage());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(),ErrorType.INVALID_PAYOUT_PURPOSE.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYOUT_PURPOSE.getCode());

				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				return fields.getFields();
			}
			// check the daily Limit Amount
			if (vendorPayoutHandler.isDailyLimitExceed(fields)) {
				logger.info("Daily Limit Exceed for Pay Id " + fields.get(FieldType.PAY_ID.getName()));

				fields.put(FieldType.STATUS.getName(), StatusType.DECLINED.getName());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(),ErrorType.DECLINED_BY_INSUFFICIENT_BALANCE.getResponseMessage());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(),ErrorType.DECLINED_BY_INSUFFICIENT_BALANCE.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DECLINED_BY_INSUFFICIENT_BALANCE.getCode());

				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				return fields.getFields();
			}

			fields.put(FieldType.USER_TYPE.getName(), "Merchant Initiated Direct");
			
			User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				User user1 = userDao.findPayId(user.getSuperMerchantId());
				if(StringUtils.isNotBlank(user1.getResellerId())) {
					fields.put(FieldType.RESELLER_ID.getName(), user1.getResellerId());
				}
			} else {
				if(StringUtils.isNotBlank(user.getResellerId())) {
					fields.put(FieldType.RESELLER_ID.getName(), user.getResellerId());
				}
			}


			logger.info("Requested Order Id : " + fields.get(FieldType.ORDER_ID.getName()) + ", Requested PayId : "
					+ fields.get(FieldType.PAY_ID.getName()));

			if (StringUtils.isBlank(fields.get(FieldType.BENE_ACCOUNT_NO.getName()))) {
				vendorPayoutHandler.communicateCompositeUPITransaction(fields);
			} else {
				vendorPayoutHandler.communicateCompositeTransaction(fields);
			}
			
			/*
			 * String request =
			 * iciciTransactionConverter.createIMPSRequest(fields); String
			 * response = iciciCommunicator.getIMPSResponse(request);
			 * responseMap = iciciResponseHandler.impsProcess(fields, response);
			 */

			fields.remove(FieldType.USER_TYPE.getName());
			fields.remove(FieldType.VIRTUAL_AC_CODE.getName());
			fields.remove(FieldType.RESELLER_ID.getName());
			/*
			 * fields.put(FieldType.AMOUNT.getName(),
			 * Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
			 * fields.get(FieldType.CURRENCY_CODE.getName())));
			 */
			vendorPayoutHandler.clearFieldsForMerchantDirect(fields);
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			return fields.getFields();
		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception in API", exception);
			responseMap.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.COMMON_ERROR.getResponseMessage());
			responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.COMMON_ERROR.getResponseCode());

		}
		return responseMap;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/merchantInitiatedDirectEnquiry", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> vendorPayooutStatusEnquiry(@RequestBody Map<String, String> reqmap)
			throws SystemException {

		logger.info("Inside Vendor Payout Status Enquiry(Merchant Initiated Direct)");
		Fields fields = new Fields(reqmap);
		fields.logAllFields("Raw Request:");
		fields.clean();

		try {

			if (!vendorPayoutHandler.validateHashForApi(fields)) {

				logger.info("Invalid Hash found for Order Id is " + fields.get(FieldType.ORDER_ID.getName()));

				fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Authentication Failed Fields : " + fields.getFields());
				return fields.getFields();
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()), "356"));

			vendorPayoutHandler.checkVendorPayoutStatus(fields);

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()), "356"));
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			return fields.getFields();
		} catch (Exception e) {
			// Ideally this should be a non-reachable code
			logger.error("Exception in API", e);
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.COMMON_ERROR.getResponseCode());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.COMMON_ERROR.getResponseCode());
		}

		return fields.getFields();

	}

}
