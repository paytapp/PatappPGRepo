package com.paymentgateway.payout.merchantApi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.dao.PayoutAcquirerMappingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.PayoutAcquireMapping;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PayoutPupose;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.payout.qaicash.QaicashResponseHandler;

@RestController
public class PayoutTransact {

	private static Logger logger = LoggerFactory.getLogger(PayoutTransact.class.getName());

	@Autowired
	private VendorPayoutHandler vendorPayoutHandler;

	@Autowired
	private PayoutAcquirerMappingDao payoutAcquirerMappingDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private QaicashResponseHandler qaicashResponseHandler;

	@RequestMapping(method = RequestMethod.POST, value = "/beneVerification", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> beneVerification(@RequestBody Map<String, String> reqmap)
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

			// Flag for verify offline account details of MPA.
			if (StringUtils.isBlank(fields.get(FieldType.MPA_FLAG.getName()))) {
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
			} else {
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

			// fetching acq mapping from DB
			PayoutAcquireMapping payoutAcquireMapping = payoutAcquirerMappingDao
					.fetchSavedMappingByPayId(fields.get(FieldType.PAY_ID.getName()));

			if (payoutAcquireMapping != null && StringUtils.isNotBlank(payoutAcquireMapping.getBankName())) {

				fields.put(FieldType.ACQUIRER_NAME.getName(), payoutAcquireMapping.getBankName());
				fields.put(FieldType.BENE_NAME_REQUEST.getName(), fields.get(FieldType.BENE_NAME.getName()));
				fields.put(FieldType.TXNTYPE.getName(), "IMPS");
				fields.put(FieldType.PURPOSE.getName(), PayoutPupose.OTHERS.getName());

				vendorPayoutHandler.communicatePayoutTransactionController(fields, payoutAcquireMapping, null);

				if (StringUtils.isNotBlank(fields.get(FieldType.BENE_NAME_REQUEST.getName()))) {
					if (fields.get(FieldType.BENE_NAME_REQUEST.getName())
							.equalsIgnoreCase(fields.get(FieldType.BENE_NAME.getName()))) {
						fields.remove(FieldType.BENE_NAME_REQUEST.getName());
					}
				}

				if (StringUtils.isNoneBlank(fields.get(FieldType.PG_TXN_MESSAGE.getName()))) {
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), fields.get(FieldType.PG_TXN_MESSAGE.getName()));
				}

				if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {
					fields.put(FieldType.STATUS.getName(), StatusType.VERIFIED.getName());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
				} else if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.TIMEOUT.getName())
						|| fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.PROCESSING.getName())) {
					fields.put(FieldType.STATUS.getName(), StatusType.PENDING.getName());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PENDING.getCode());
				} else {
					fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.USER_NOT_FOUND.getCode());
				}

				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

				logger.info("Response Fields to Merchant " + fields.getFields());

			} else {
				logger.info("No Acquirer for Payout, payId " + fields.get(FieldType.PAY_ID.getName()));
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getCode());
			}

			vendorPayoutHandler.clearFieldsForBeneVerification(fields);

			return fields.getFields();
		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception in API", exception);

			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.COMMON_ERROR.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.COMMON_ERROR.getResponseCode());
			logger.info("Response Fields to Merchant " + fields.getFields());
		} finally {
			vendorPayoutHandler.clearFieldsForBeneVerification(fields);
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

				if (StringUtils.isBlank(fields.get(FieldType.RESPONSE_CODE.getName()))) {
					logger.info("Invalid Hash found for Order Id is " + fields.get(FieldType.ORDER_ID.getName()));

					fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
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

			if (!vendorPayoutHandler.isDirectTransctionAllowed(fields)) {

				logger.info("Merchant is Not Allowed for Access this API found for PAY ID is "
						+ fields.get(FieldType.PAY_ID.getName()));

				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACCESS_DENIED.getResponseCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACCESS_DENIED.getResponseMessage());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				return fields.getFields();
			}

			User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));

			if (!vendorPayoutHandler.useStatusActive(user)) {

				logger.info("Merchant is Not Active for Access this API found for PAY ID is "
						+ fields.get(FieldType.PAY_ID.getName()));

				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.USER_INACTIVE.getResponseCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.USER_INACTIVE.getResponseMessage());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				return fields.getFields();
			}

			logger.info("Checking Duplicate Order Id");

			// fetching acq mapping from DB
			PayoutAcquireMapping payoutAcquireMapping = payoutAcquirerMappingDao
					.fetchSavedMappingByPayId(fields.get(FieldType.PAY_ID.getName()), user);
			if (payoutAcquireMapping != null && StringUtils.isNotBlank(payoutAcquireMapping.getBankName())) {

				fields.put(FieldType.ACQUIRER_NAME.getName(), payoutAcquireMapping.getBankName());

				vendorPayoutHandler.checkDuplicateOrderIdPayout(fields);

				if (fields.get(FieldType.DUPLICATE_YN.getName()).equalsIgnoreCase("Y")) {
					fields.remove(FieldType.DUPLICATE_YN.getName());
					logger.info("Duplicate order Id found Order Id is " + fields.get(FieldType.ORDER_ID.getName()));

					fields.put(FieldType.STATUS.getName(), StatusType.DUPLICATE.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.DUPLICATE_ORDER_ID.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE_ORDER_ID.getResponseCode());

					vendorPayoutHandler.updateBulkTxnStatus(fields);
					vendorPayoutHandler.clearFieldsForMerchantDirect(fields);

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

					vendorPayoutHandler.updateBulkTxnStatus(fields);
					vendorPayoutHandler.clearFieldsForMerchantDirect(fields);

					fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
					return fields.getFields();
				}

				fields.put(FieldType.USER_TYPE.getName(), "Merchant Initiated Direct");

				// check with acquirer
				if (vendorPayoutHandler.purposeCheck(fields)) {
					logger.info("Purpose chack for Pay Id " + fields.get(FieldType.PAY_ID.getName()));

					fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.INVALID_PAYOUT_PURPOSE.getResponseMessage());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(),
							ErrorType.INVALID_PAYOUT_PURPOSE.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYOUT_PURPOSE.getCode());

					vendorPayoutHandler.clearFieldsForMerchantDirect(fields);

					fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

					return fields.getFields();
				}

				// check the daily Limit Amount
				if (vendorPayoutHandler.isDailyLimitExceed(fields)) {
					logger.info("Daily Limit Exceed for Pay Id " + fields.get(FieldType.PAY_ID.getName()));

					fields.put(FieldType.STATUS.getName(), StatusType.DECLINED.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.DECLINED_BY_INSUFFICIENT_BALANCE.getResponseMessage());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(),
							ErrorType.DECLINED_BY_INSUFFICIENT_BALANCE.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DECLINED_BY_INSUFFICIENT_BALANCE.getCode());

					vendorPayoutHandler.clearFieldsForMerchantDirect(fields);

					fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
					return fields.getFields();
				}

				if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
					User user1 = userDao.findPayId(user.getSuperMerchantId());
					if (StringUtils.isNotBlank(user1.getResellerId())) {
						fields.put(FieldType.RESELLER_ID.getName(), user1.getResellerId());
					}
				} else {
					if (StringUtils.isNotBlank(user.getResellerId())) {
						fields.put(FieldType.RESELLER_ID.getName(), user.getResellerId());
					}
				}

				// check the daily Limit Amount
				if (!vendorPayoutHandler.verifyAmountandTxnType(fields)) {
					logger.info("Amount is Invalid for TXNType " + fields.get(FieldType.TXNTYPE.getName()));
					vendorPayoutHandler.saveFailedTxnInDb(fields, user);
					vendorPayoutHandler.clearFieldsForMerchantDirect(fields);
					fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
					return fields.getFields();
				}

				logger.info("Requested Order Id : " + fields.get(FieldType.ORDER_ID.getName()) + ", Requested PayId : "
						+ fields.get(FieldType.PAY_ID.getName()));

				// Check the payout Acquirer and communicate
				vendorPayoutHandler.communicatePayoutTransactionController(fields, payoutAcquireMapping, user);

				// For showing msg to UI
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), fields.get(FieldType.RESPONSE_MESSAGE.getName()));

			} else {
				logger.info("No Acquirer for Payout, payId " + fields.get(FieldType.PAY_ID.getName()));
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getCode());
			}

			vendorPayoutHandler.clearFieldsForMerchantDirect(fields);
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			return fields.getFields();

		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception in API", exception);
			vendorPayoutHandler.clearFieldsForMerchantDirect(fields);
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.COMMON_ERROR.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.COMMON_ERROR.getResponseCode());

		} finally {
			vendorPayoutHandler.clearFieldsForMerchantDirect(fields);
		}
		return responseMap;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/payoutStatusEnquiry", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> payoutStatusEnquiry(@RequestBody Map<String, String> reqmap) {
		Fields fields = new Fields(reqmap);
		try {

			fields.logAllFields("Raw Request payoutStatusEnquiry : ");
			fields.clean();

			if (!vendorPayoutHandler.validateHashForApi(fields)) {

				if (StringUtils.isBlank(fields.get(FieldType.RESPONSE_CODE.getName()))) {
					logger.info("Invalid Hash found for Order Id is " + fields.get(FieldType.ORDER_ID.getName()));

					fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());

					fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

					logger.info("Payout Bank Status Enquiry Response > " + fields.getFields());
					return fields.getFields();
				}

			}

			String payId = fields.get(FieldType.PAY_ID.getName());
			String acquirerName = fields.get(FieldType.ACQUIRER_NAME.getName());
			String virtualAcNo = fields.get(FieldType.VIRTUAL_AC_CODE.getName());

			if (StringUtils.isNotBlank(payId) && StringUtils.isNotBlank(acquirerName)
					&& StringUtils.isNotBlank(virtualAcNo)) {
				// fetching acq mapping from DB
				PayoutAcquireMapping payoutAcquireMapping = payoutAcquirerMappingDao
						.findMappingwithAcquirerNameAndVirtualAcNo(payId, acquirerName, virtualAcNo);

				if (payoutAcquireMapping != null && StringUtils.isNotBlank(payoutAcquireMapping.getBankName())) {
					fields.put(FieldType.ACQUIRER_NAME.getName(), payoutAcquireMapping.getBankName());

					vendorPayoutHandler.communicatePayoutTransactionStatusEnquiryController(fields,
							payoutAcquireMapping);

				} else {
					logger.info("No Acquirer for Payout, payId " + fields.get(FieldType.PAY_ID.getName()));
					fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getCode());
					logger.info("No Acquirer Found for Payout");
				}
			} else {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.EMPTY_FIELD.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.EMPTY_FIELD.getCode());
				logger.info(
						"payoutStatusEnquiry(), Missing required field (payId, Acquirer name and virtual Account No) in request for fetch mapping verify request from schduler");
			}

		} catch (Exception e) {
			logger.info("exception in payout Enq ", e);
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.COMMON_ERROR.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.COMMON_ERROR.getCode());
		}
		logger.info("Payout Bank Status Enquiry Response > " + fields.getFields());
		return fields.getFields();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/merchantInitiatedDirectEnquiry", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> vendorPayoutStatusEnquiry(@RequestBody Map<String, String> reqmap)
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
			logger.info("merchantInitiatedDirectEnquiry() final response to merchant " + fields.getFields());
			return fields.getFields();
		} catch (Exception e) {
			// Ideally this should be a non-reachable code
			logger.error("Exception in API", e);
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.COMMON_ERROR.getResponseCode());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.COMMON_ERROR.getResponseCode());
		}
		logger.info("merchantInitiatedDirectEnquiry() final response to merchant " + fields.getFields());
		return fields.getFields();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/updateOldVirtualDetails", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> updateOldVirtualDetails(@RequestBody Map<String, String> reqmap)
			throws SystemException {

		logger.info("Inside updating old virtual details");
		Fields fields = new Fields(reqmap);
		fields.logAllFields("Raw Request:");
		fields.clean();

		try {

			vendorPayoutHandler.updateOldVirtualDetails();

			logger.info("inserted all old virtual details");
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Updated Successfully");

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

	@RequestMapping(method = RequestMethod.POST, value = "/checkbalance", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> checkPayoutbalance(@RequestBody Map<String, String> reqmap)
			throws SystemException {

		Map<String, String> responseMap = new HashMap<String, String>();
		try {

			responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getInternalMessage());
			logger.info("Enquiry for Balance check");
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Enquiry for Balance check fields");

			vendorPayoutHandler.fetchPayoutBalance(fields);
			responseMap = fields.getFields();
		} catch (Exception e) {
			logger.error("Exception in Fetch Balance", e);
		}
		return responseMap;

	}

	@RequestMapping(value = "/cashfree/callback", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public @ResponseBody String cashfreeCallbackResponse(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		try {
			logger.info("inside Cashfree Callback Handle API");
			Fields fields = new Fields();
			vendorPayoutHandler.addRequestInFields(request, response, fields);

			if (StringUtils.isNotBlank(fields.get("event"))) {

				if (fields.get("event").equalsIgnoreCase("CREDIT_CONFIRMATION")) {
					fields.put(FieldType.PAY_ID.getName(), request.getParameter("payId"));
					vendorPayoutHandler.cashfreePayoutAddBalance(fields);
				} else {
					vendorPayoutHandler.cashfreeCallbackResponseHandler(fields);
				}
			}

			return fields.get(FieldType.RESPONSE_MESSAGE.getName());

		} catch (Exception e) {
			logger.info("exception in cashfree payout Callback ", e);
		}
		return null;
	}

	@RequestMapping(value = "/payoutEncDecrypt", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String decryptPayout(@RequestBody Map<String, String> reqmap) throws IOException {

		try {
			JSONObject json = new JSONObject();

			for (Map.Entry<String, String> entry : reqmap.entrySet()) {
				json.put(entry.getKey(), entry.getValue());
			}

			String response = vendorPayoutHandler.decryptPayoutEncData(json.toString());

			return response;
		} catch (Exception e) {
			logger.info("exception in cashfree payout Callback ", e);
		}

		return null;
	}

	@SuppressWarnings("static-access")
	@RequestMapping(method = RequestMethod.POST, value = "/apexPayoutCallback", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void apexPayoutCallback(@RequestBody Map<String, String> reqmap, HttpServletRequest request) {

		Fields fields = new Fields(reqmap);
		try {

			fields.logAllFields("Apexpay Payout Callback Received : ");

			String whitelistedIP = propertiesManager.propertiesMap.get("ApexPayoutWhiteListedIP");
			String responseIp = request.getHeader("X-Forwarded-For");
			logger.info("Apexpay payout callback received from Ip address (X-Forwarded-For) " + responseIp);

			if (StringUtils.isBlank(responseIp)) {
				responseIp = request.getRemoteAddr();
			}

			logger.info("Apexpay payout callback received from Ip address (RemoteAddr) " + responseIp);
			if (!whitelistedIP.contains(responseIp)) {
				logger.info("Response received from an unknown IP address = " + responseIp + " Client Id = "
						+ fields.get("CLIENT_ID"));
				return;
			}

			fields.clean();

			String STATUS = fields.get("STATUS");
			String CLIENT_ID = fields.get("CLIENT_ID");

			if (StringUtils.isNotBlank(STATUS) && StringUtils.isNotBlank(CLIENT_ID)) {
				vendorPayoutHandler.handleApexCallback(fields);

			} else {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.EMPTY_FIELD.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.EMPTY_FIELD.getCode());
				logger.info("Apexpay Callback for payout, Missing required field (STATUS, CLIENT_ID) in response");
			}

		} catch (Exception e) {
			logger.info("exception in payout callback ", e);
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.COMMON_ERROR.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.COMMON_ERROR.getCode());
		}
		logger.info("Apexpay payout callabck response = " + fields.getFields());
	}

	@SuppressWarnings("static-access")
	@RequestMapping(method = RequestMethod.POST, value = "/fonePaisaPayoutCallback", consumes = MediaType.APPLICATION_JSON_VALUE)
	public String fonePaisaPayoutCallback(@RequestBody String reqJson, HttpServletRequest request) {

		Fields fields = new Fields();
		try {

			fields.logAllFields("fonePaisa Payout Callback Received : " + reqJson);

			String whitelistedIP = propertiesManager.propertiesMap.get("FonePaisaWhiteListedIP");
			String responseIp = request.getHeader("X-Forwarded-For");
			logger.info("fonePaisa payout callback received from Ip address (X-Forwarded-For) " + responseIp);

			if (StringUtils.isBlank(responseIp)) {
				responseIp = request.getRemoteAddr();
			}

			logger.info("fonePaisa payout callback received from Ip address (RemoteAddr) " + responseIp);
			if (!whitelistedIP.contains(responseIp)) {
				logger.info("Response received from an unknown IP address = " + responseIp);
				return "Whitelist IP Mismatch";
			}

			vendorPayoutHandler.handleFonePaisaCallback(reqJson, fields);

			logger.info("fonePaisa payout callabck response = " + fields.getFields());

			return "Success";

		} catch (Exception e) {
			logger.info("exception in payout callback ", e);
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.COMMON_ERROR.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.COMMON_ERROR.getCode());
			return e.getMessage();
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/qaicashPayoutCallback", consumes = MediaType.APPLICATION_JSON_VALUE)
	public String qaicashPayoutCallback(@RequestBody String reqJson, HttpServletRequest request) {

		Fields fields = new Fields();
		try {

			String responseIp = request.getHeader("X-Forwarded-For");
			logger.info("Qaicash payout callback received from Ip address (RemoteAddr) " + responseIp);
			logger.info("Qaicash Payout Callback Received : {} ", reqJson);

			JSONObject responseJson = new JSONObject(reqJson);

			if (!responseJson.has("orderId") || !responseJson.has("transactionId")) {
				logger.info(
						"Qaicash Callabck Response does not have OrderId , callabck {} has been rejected for IP address {}",
						reqJson, responseIp);
				return "NO DATA FOUND";
			}

			qaicashResponseHandler.handleQaicashCallback(reqJson, fields);
			logger.info("Qaicash payout callabck response = " + fields.getFields());

			return "Success";

		} catch (Exception e) {
			logger.info("Exception in Qaicash payout callback ", e);
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.COMMON_ERROR.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.COMMON_ERROR.getCode());
			return e.getMessage();
		}

	}

}
