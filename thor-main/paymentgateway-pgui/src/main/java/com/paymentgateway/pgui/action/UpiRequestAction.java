package com.paymentgateway.pgui.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.UserStatusType;
import com.paymentgateway.oneclick.TokenManager;
import com.paymentgateway.pg.core.util.ResponseCreator;
import com.paymentgateway.pg.core.util.TransactionResponser;

@Service
public class UpiRequestAction {

	private static Logger logger = LoggerFactory.getLogger(UpiRequestAction.class.getName());

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private TransactionResponser transactionResponser;

	@Autowired
	private ResponseCreator responseCreator;

	@Autowired
	private TokenManager tokenManager;

	@Autowired
	private UserDao userDao;

	@Autowired
	private FieldsDao fieldsDao;

	private Map<String, String> responseFields;

	public Map<String, String> upiRequestHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			Map<String, String> reqMap) throws IOException {
		responseFields = new HashMap<String, String>();

		try {
			Fields fields = null;
			if (reqMap.containsKey(FieldType.CHECKOUT_JS_FLAG.getName())
					&& reqMap.get(FieldType.CHECKOUT_JS_FLAG.getName()).equalsIgnoreCase("true")) {
				httpRequest.getSession().invalidate();
			} else {
				fields = (Fields) httpRequest.getSession().getAttribute(Constants.FIELDS.getValue());
			}
			if (null != fields) {
			} else {
				if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("ADMIN_PAYID"))
						&& StringUtils.isNotBlank(reqMap.get("encSessionData"))) {
					Map<String, String> fieldsMap = new HashMap<String, String>();
					Map<String, String> responseMap = transactionControllerServiceProvider.hostedDecrypt(
							PropertiesManager.propertiesMap.get("ADMIN_PAYID"), reqMap.get("encSessionData"));
					if (!responseMap.isEmpty()) {
						String decryptedString = responseMap.get(FieldType.ENCDATA.getName());
						String[] fieldArray = decryptedString.split("~");

						for (String key : fieldArray) {
							String[] namValuePair = key.split("=", 2);
							httpRequest.getSession().setAttribute(namValuePair[0], namValuePair[1]);
						}
					}
					String sessionFields = (String) httpRequest.getSession().getAttribute(Constants.FIELDS.getValue());
					sessionFields = sessionFields.substring(1, sessionFields.length() - 1);
					String[] fieldArray = sessionFields.split(",");
					for (String key : fieldArray) {
						if (key.charAt(0) == ' ') {
							key = key.replaceFirst("^\\s*", "");
						}
						String[] namValuePair = key.split("=", 2);
						fieldsMap.put(namValuePair[0], namValuePair[1]);
					}
					fields = new Fields(fieldsMap);
					logger.info(fieldsMap.toString());

				} else {
					logger.info("session fields lost");
					String path = httpRequest.getContextPath();
					logger.info(path);
					if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
						String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host")
								+ "/pgui/jsp/error";
						httpResponse.sendRedirect(resultPath);
					}
					httpResponse.sendRedirect("error");
				}
			}

			String subMerchantId = null;
			String superMerchantId = null;
			boolean isSubMerchant = false;

			if (StringUtils.isNotBlank(
					(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_HEADER_ACEEPT.getName()))) {
				fields.put((FieldType.INTERNAL_HEADER_ACEEPT.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_HEADER_ACEEPT.getName()));
			}
			if (StringUtils.isNotBlank(
					(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_HEADER_USER_AGENT.getName()))) {
				fields.put((FieldType.INTERNAL_HEADER_USER_AGENT.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_HEADER_USER_AGENT.getName()));
			}
			if (StringUtils
					.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_CUST_IP.getName()))) {
				fields.put((FieldType.INTERNAL_CUST_IP.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_CUST_IP.getName()));
			}
			if (StringUtils.isNotBlank(
					(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()))) {
				fields.put((FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()));
			}

			// Check For Sub Merchant Transactions via invoice:
			if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
				subMerchantId = fields.get(FieldType.SUB_MERCHANT_ID.getName());
				User subMerchant = userDao.findPayId(subMerchantId);

				if (subMerchant.getUserStatus() != UserStatusType.ACTIVE) {
					fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.USER_INACTIVE.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.USER_INACTIVE.getCode());
					fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
					fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
					responseCreator.ResponsePost(fields, httpResponse);
//					return Action.NONE;
				}
				// Remove for hash Validation
				fields.remove(FieldType.SUB_MERCHANT_ID.getName());
			} else {

				// Check For Sub Merchant Transactions via website:

				User subMerchant = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));

				if (subMerchant.isSuperMerchant() && StringUtils.isNotBlank(subMerchant.getSuperMerchantId())) {

					fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REJECTED_BY_PG.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED_BY_PG.getCode());
					fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
					fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
					responseCreator.ResponsePost(fields, httpResponse);
//					return Action.NONE;
				}

				if (!subMerchant.isSuperMerchant() && StringUtils.isNotBlank(subMerchant.getSuperMerchantId())) {
					superMerchantId = subMerchant.getSuperMerchantId();
					subMerchantId = subMerchant.getPayId();
					isSubMerchant = true;
				}

				if (isSubMerchant && subMerchant.getUserStatus() != UserStatusType.ACTIVE) {
					fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REJECTED_BY_PG.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED_BY_PG.getCode());
					fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
					fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
					responseCreator.ResponsePost(fields, httpResponse);
//					return Action.NONE;
				}
			}

			String fieldsAsString = fields.getFieldsAsString();

			httpRequest.getSession().setAttribute(Constants.FIELDS.getValue(), fields);

			fields.put(FieldType.INTERNAL_REQUEST_FIELDS.getName(), fieldsAsString);
			logger.info("Raw Request Fields:  " + fields.getFieldsAsString());

			if (StringUtils.isBlank(fields.get(FieldType.HASH.getName()))) {

				throw new SystemException(ErrorType.VALIDATION_FAILED, "Invalid " + FieldType.HASH.getName());
			}

			if (StringUtils.isBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
				fields.remove(FieldType.SUB_MERCHANT_ID.getName());
			}

			// Add Sub Merchant Id Again for keeping transaction records for sub merchant
			if (StringUtils.isNotBlank(subMerchantId)) {
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
			}

			// Update PayId to super merchant id and add sub merchant payid as
			// SUB_MERCHANT_ID for record
			if (isSubMerchant) {
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
				fields.put(FieldType.PAY_ID.getName(), superMerchantId);
			}

			fields.put(FieldType.PAYMENT_TYPE.getName(), reqMap.get("paymentType"));
			fields.put(FieldType.MOP_TYPE.getName(), reqMap.get("mopType"));

			httpRequest.getSession().setAttribute((FieldType.INTERNAL_ORIG_TXN_TYPE.getName()),
					TransactionType.SALE.getName());

			fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.SALE.getName());
			fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
			fields.put(FieldType.PAYER_ADDRESS.getName(), reqMap.get("vpa"));
//			fields.put(FieldType.PAYER_PHONE.getName(), getVpaPhone());
//			fields.put(FieldType.PAYER_NAME.getName(), getUpiCustName());
			fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
			String sessionOID = (String) httpRequest.getSession().getAttribute(FieldType.OID.getName());
			fields.put(FieldType.INTERNAL_ORIG_TXN_ID.getName(), sessionOID);

//			if (getCardHolderType() != null && !getCardHolderType().equals("")) {
//				fields.put(FieldType.CARD_HOLDER_TYPE.getName(), getCardHolderType());
//			} else {
			fields.put(FieldType.CARD_HOLDER_TYPE.getName(), CardHolderType.CONSUMER.toString());
			responseFields.put(FieldType.CARD_HOLDER_TYPE.getName(), CardHolderType.CONSUMER.toString());
//			}

			// If PAYMENTS_REGION not found from bin , set defaults to DOMESTIC
//			if (getPaymentsRegion() != null && !getPaymentsRegion().equals("")) {
//				fields.put(FieldType.PAYMENTS_REGION.getName(), getPaymentsRegion());
//			} else {
			fields.put(FieldType.PAYMENTS_REGION.getName(), AccountCurrencyRegion.DOMESTIC.toString());
			responseFields.put(FieldType.PAYMENTS_REGION.getName(), AccountCurrencyRegion.DOMESTIC.toString());
//			}

			// Clear symbols from PG response
			if (fields.get(FieldType.PG_TXN_MESSAGE.getName()) != null) {
				String PG_TXN_MESSAGE = fields.get(FieldType.PG_TXN_MESSAGE.getName());
				if (PG_TXN_MESSAGE.contains(":")) {

					PG_TXN_MESSAGE.replace(":", " ");
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), PG_TXN_MESSAGE);
				}
			}

			String surchargeFlag = httpRequest.getSession().getAttribute("SURCHARGE_FLAG").toString();
			if (surchargeFlag.equals("Y")) {
				fields.put((FieldType.SURCHARGE_FLAG.getName()), surchargeFlag);
			}

			String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());
			String paymentType = fields.get(FieldType.PAYMENT_TYPE.getName());
			if (paymentType.equals(PaymentType.UPI.getCode())) {
				String upTotalAmount = httpRequest.getSession().getAttribute(FieldType.UP_TOTAL_AMOUNT.getName())
						.toString();
				upTotalAmount = Amount.formatAmount(upTotalAmount, currencyCode);
				fields.put(FieldType.TOTAL_AMOUNT.getName(), upTotalAmount);
				httpRequest.getSession().setAttribute("UPI_TOTAL_AMOUNT", upTotalAmount);
			} else {
				throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
			}

			// If we get any sent to bank present in db with this order Id , reject the
			// transaction

			if (StringUtils.isNotBlank(fields.get(FieldType.ORDER_ID.getName()))) {
				boolean isPresent = fieldsDao.checkSentToBankUpi(fields.get(FieldType.ORDER_ID.getName()));

				if (isPresent) {

					String responseCode = ErrorType.DUPLICATE.getCode();
					Map<String, String> fieldsMap = new HashMap<String, String>();

					for (String key : fields.keySet()) {
						fieldsMap.put(key, fields.get(key));
					}
					fieldsMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE.getResponseCode());
					fieldsMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DUPLICATE.getResponseMessage());
					fieldsMap.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.DUPLICATE.getResponseMessage());
					fieldsMap.put(FieldType.STATUS.getName(), StatusType.DUPLICATE.getName());
					fieldsMap.remove(FieldType.ACS_URL.getName());
					fieldsMap.remove(FieldType.CARD_MASK.getName());
					fieldsMap.remove(FieldType.PRODUCT_DESC.getName());
					fieldsMap.remove(FieldType.MD.getName());
					fieldsMap.remove(FieldType.CUST_EMAIL.getName());
					fieldsMap.remove(FieldType.CARD_HOLDER_TYPE.getName());
					fieldsMap.remove(FieldType.PAYER_NAME.getName());
					fieldsMap.remove(FieldType.PAYMENTS_REGION.getName());
					fieldsMap.remove(FieldType.PAYER_ADDRESS.getName());
					fieldsMap.remove(FieldType.PAYER_PHONE.getName());
					fieldsMap.remove(FieldType.MERCHANT_ID.getName());
					fieldsMap.remove(FieldType.ACQUIRER_TYPE.getName());
					fieldsMap.remove(FieldType.ACQ_ID.getName());
					Fields responseMap = new Fields(fieldsMap);

					logger.info("Response Code === " + responseCode + " Redirecting to merchant ");
					logger.info("Response fields from pgws:  " + responseMap.getFieldsAsString());
					httpRequest.getSession().setAttribute(Constants.FIELDS.getValue(), responseMap);
					httpRequest.getSession().setAttribute(FieldType.INTERNAL_ORIG_TXN_ID.getName(),
							responseMap.get(FieldType.TXN_ID.getName()));
					responseMap.put(FieldType.RETURN_URL.getName(),
							httpRequest.getSession().getAttribute(FieldType.RETURN_URL.getName()).toString());
//					if (responseMap.get(FieldType.PG_TXN_MESSAGE.getName()) != null) {
//						setResponseMessage(responseMap.get(FieldType.PG_TXN_MESSAGE.getName()));
//					} else {
//						setResponseMessage(responseMap.get(FieldType.RESPONSE_MESSAGE.getName()));
//					}

//					setPgRefNum(responseMap.get(FieldType.PG_REF_NUM.getName()));
//					setResponseCode(responseMap.get(FieldType.RESPONSE_CODE.getName()));

//					setTransactionStatus(responseMap.get(FieldType.STATUS.getName()));
					responseMap.put(FieldType.TXNTYPE.getName(), responseMap.get(FieldType.PAYMENT_TYPE.getName()));
//					setTxnType(responseMap.get(FieldType.PAYMENT_TYPE.getName()));
					responseMap.removeInternalFields();
					responseMap.removeSecureFields();
					responseMap.remove(FieldType.HASH.getName());
					// E-ticketing
					String pgFlag = (String) httpRequest.getSession()
							.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
					if (StringUtils.isNotBlank(pgFlag) && pgFlag.equals(Constants.Y.getValue())) {
						responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
						String encData = responseCreator.createCrisUpiResponse(responseMap);
						logger.info("encrypted response to Payment GateWay for UPI in UPIRequestAction " + encData);
						responseFields.put(Constants.ENC_DATA.getValue(), encData);
						responseFields.put(FieldType.RETURN_URL.getName(),
								httpRequest.getSession().getAttribute(FieldType.RETURN_URL.getName()).toString());
					} else {
						responseMap.put(FieldType.HASH.getName(), Hasher.getHash(responseMap));
						for (String key : responseMap.keySet()) {
							responseFields.put(key, responseMap.get(key));
						}
//						setResponseFields(responseMap.getFields());
					}
					return responseFields;
				}
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.ACQUIRER_TYPE.getName()))) {
				String responseCode = ErrorType.DUPLICATE.getCode();
				Map<String, String> fieldsMap = new HashMap<String, String>();

				for (String key : fields.keySet()) {
					fieldsMap.put(key, fields.get(key));
				}
				fieldsMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE.getResponseCode());
				fieldsMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DUPLICATE.getResponseMessage());
				fieldsMap.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.DUPLICATE.getResponseMessage());
				fieldsMap.put(FieldType.STATUS.getName(), StatusType.DUPLICATE.getName());
				fieldsMap.remove(FieldType.ACS_URL.getName());
				fieldsMap.remove(FieldType.CARD_MASK.getName());
				fieldsMap.remove(FieldType.PRODUCT_DESC.getName());
				fieldsMap.remove(FieldType.MD.getName());
				fieldsMap.remove(FieldType.CUST_EMAIL.getName());
				fieldsMap.remove(FieldType.CARD_HOLDER_TYPE.getName());
				fieldsMap.remove(FieldType.PAYER_NAME.getName());
				fieldsMap.remove(FieldType.PAYMENTS_REGION.getName());
				fieldsMap.remove(FieldType.PAYER_ADDRESS.getName());
				fieldsMap.remove(FieldType.PAYER_PHONE.getName());
				fieldsMap.remove(FieldType.MERCHANT_ID.getName());
				fieldsMap.remove(FieldType.ACQUIRER_TYPE.getName());
				fieldsMap.remove(FieldType.ACQ_ID.getName());
				fieldsMap.remove(FieldType.ACQUIRER_TYPE.getName());
				Fields responseMap = new Fields(fieldsMap);

				logger.info("Response Code === " + responseCode + " Redirecting to merchant ");
				logger.info("Response fields from pgws:  " + responseMap.getFieldsAsString());
				httpRequest.getSession().setAttribute(Constants.FIELDS.getValue(), responseMap);
				httpRequest.getSession().setAttribute(FieldType.INTERNAL_ORIG_TXN_ID.getName(),
						responseMap.get(FieldType.TXN_ID.getName()));
				responseMap.put(FieldType.RETURN_URL.getName(),
						httpRequest.getSession().getAttribute(FieldType.RETURN_URL.getName()).toString());
//				if (responseMap.get(FieldType.PG_TXN_MESSAGE.getName()) != null) {
//					setResponseMessage(responseMap.get(FieldType.PG_TXN_MESSAGE.getName()));
//				} else {
//					setResponseMessage(responseMap.get(FieldType.RESPONSE_MESSAGE.getName()));
//				}

//				setPgRefNum(responseMap.get(FieldType.PG_REF_NUM.getName()));
//				setResponseCode(responseMap.get(FieldType.RESPONSE_CODE.getName()));

//				setTransactionStatus(responseMap.get(FieldType.STATUS.getName()));
				responseMap.put(FieldType.TXNTYPE.getName(), responseMap.get(FieldType.PAYMENT_TYPE.getName()));
//				setTxnType(responseMap.get(FieldType.PAYMENT_TYPE.getName()));
				responseMap.removeInternalFields();
				responseMap.removeSecureFields();
				responseMap.remove(FieldType.HASH.getName());
				// E-ticketing
				String pgFlag = (String) httpRequest.getSession()
						.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
				if (StringUtils.isNotBlank(pgFlag) && pgFlag.equals(Constants.Y.getValue())) {
					responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
					String encData = responseCreator.createCrisUpiResponse(responseMap);
					logger.info("encrypted response to Payment GateWay for UPI in UPIRequestAction " + encData);
					responseFields.put(Constants.ENC_DATA.getValue(), encData);
					responseFields.put(FieldType.RETURN_URL.getName(),
							httpRequest.getSession().getAttribute(FieldType.RETURN_URL.getName()).toString());
				} else {
					responseMap.put(FieldType.HASH.getName(), Hasher.getHash(responseMap));
					for (String key : responseMap.keySet()) {
						responseFields.put(key, responseMap.get(key));
					}
//					setResponseFields(responseMap.getFields());
				}

				return responseFields;
			}

			if (StringUtils.isBlank(fields.get(FieldType.AMOUNT.getName()))) {
				throw new SystemException(ErrorType.BANK_SURCHARGE_REJECTED, "Bank Surcharge request rejected");
			}

			if (surchargeFlag.equals("Y")) {
				logger.info("IN UPI REq action orderId = " + fields.get(FieldType.ORDER_ID.getName())
						+ " total amount = " + fields.get(FieldType.TOTAL_AMOUNT.getName()) + "  amount  = "
						+ fields.get(FieldType.AMOUNT.getName()));
				if (Double.valueOf(fields.get(FieldType.TOTAL_AMOUNT.getName()))
						.compareTo(Double.valueOf(fields.get(FieldType.AMOUNT.getName()))) < 0) {
					throw new SystemException(ErrorType.BANK_SURCHARGE_REJECTED, "Bank Surcharge request rejected");
				}
			}

			logger.info("UPI Request action , order ID = " + fields.get(FieldType.ORDER_ID.getName())
					+ "UPI Request action , pay ID = " + fields.get(FieldType.PAY_ID.getName()));
			Map<String, String> response = transactionControllerServiceProvider.transact(fields,
					Constants.TXN_WS_INTERNAL.getValue());

			String pgFlag = (String) httpRequest.getSession()
					.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
			if (response.get(FieldType.RESPONSE_CODE.getName())
					.equalsIgnoreCase(ErrorType.INVALID_VPA.getResponseCode())) {
				response.remove(FieldType.ACQUIRER_TYPE.getName());
			}
			Fields responseMap = new Fields(response);

			if (responseMap.get(FieldType.STATUS.getName()).equals(StatusType.DENIED_BY_FRAUD.getName())) {

				responseMap.put(FieldType.TXNTYPE.getName(),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DENIED_BY_FRAUD.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DENIED_BY_FRAUD.getCode());
				transactionResponser.removeInvalidResponseFields(responseMap);
				transactionResponser.addResponseDateTime(responseMap);

				if (StringUtils.isNotBlank(pgFlag)) {
					responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
				}
				responseMap.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
				responseCreator.create(responseMap);
//				responseCreator.ResponsePost(responseMap, httpResponse);
				responseMap.put(FieldType.MERCHANT_NAME.getName(),
						userDao.getBusinessNameByPayId(responseMap.get(FieldType.PAY_ID.getName())));
				responseMap.put(FieldType.HASH.getName(), Hasher.getHash(responseMap));
				for (String key : responseMap.keySet()) {
					responseFields.put(key, responseMap.get(key));
				}
				return responseFields;
//				return Action.NONE;
			}
			
			if (responseMap.get(FieldType.STATUS.getName()).equals(StatusType.INVALID.getName())) {

				responseMap.put(FieldType.TXNTYPE.getName(),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.MIN_AMOUNT_ERROR.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.MIN_AMOUNT_ERROR.getCode());
				transactionResponser.removeInvalidResponseFields(responseMap);
				transactionResponser.addResponseDateTime(responseMap);

				if (StringUtils.isNotBlank(pgFlag)) {
					responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
				}
				responseMap.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
				responseCreator.create(responseMap);
//				responseCreator.ResponsePost(responseMap, httpResponse);
				responseMap.put(FieldType.MERCHANT_NAME.getName(),
						userDao.getBusinessNameByPayId(responseMap.get(FieldType.PAY_ID.getName())));
				responseMap.put(FieldType.HASH.getName(), Hasher.getHash(responseMap));
				for (String key : responseMap.keySet()) {
					responseFields.put(key, responseMap.get(key));
				}
				return responseFields;
//				return Action.NONE;
			}
			/*
			 * if
			 * (StringUtils.isNotBlank(responseMap.get(FieldType.ACQUIRER_TYPE.getName()))
			 * && responseMap.get(FieldType.ACQUIRER_TYPE.getName()).equalsIgnoreCase(
			 * AcquirerType.BOB.getCode())) { sessionMap.put(FieldType.TXN_KEY.getName(),
			 * responseMap.get(FieldType.TXN_KEY.getName()));
			 * responseMap.put(FieldType.RESPONSE_CODE .getName(),
			 * ErrorType.BOB_UPI_CODE.getCode()); }
			 */

			// SAVE VPA
			if (StringUtils.isNotBlank(responseMap.get(FieldType.RESPONSE_CODE.getName())) && responseMap
					.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase(ErrorType.SUCCESS.getCode())) {
				if (reqMap.get("vpaSaveflag").equalsIgnoreCase("true")) {
					tokenManager.addVPAToken(fields, userDao.findPayId(responseMap.get(FieldType.PAY_ID.getName())));
				}
			}

			logger.info("Response received from Transact in UPI Request Action "
					+ responseMap.get(FieldType.RESPONSE_CODE.getName()));
			if (responseMap.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase(ErrorType.UNKNOWN.getCode())) {

				logger.info("Response received === " + responseMap.get(FieldType.RESPONSE_CODE.getName())
						+ " Redirecting to merchant ");
				responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), (String) httpRequest.getSession()
						.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName()));
				responseCreator.ResponsePost(responseMap, httpResponse);
//				return Action.NONE;
			}

			if (responseMap.get(FieldType.RESPONSE_CODE.getName())
					.equalsIgnoreCase(ErrorType.ACQUIRER_NOT_FOUND.getCode())) {

				logger.info("Response Code === " + responseMap.get(FieldType.RESPONSE_CODE.getName())
						+ " Redirecting to merchant ");
				if (StringUtils.isNotBlank(pgFlag)) {
					responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
				}
				responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), (String) httpRequest.getSession()
						.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName()));
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.INTERNAL_SYSTEM_ERROR.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INTERNAL_SYSTEM_ERROR.getCode());
				responseMap.put(FieldType.TXNTYPE.getName(),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
				transactionResponser.removeInvalidResponseFields(responseMap);
				transactionResponser.addResponseDateTime(responseMap);
				responseCreator.ResponsePost(responseMap, httpResponse);
//				return Action.NONE;
			}
			logger.info("Response fields from pgws:  " + responseMap.getFieldsAsString());
			httpRequest.getSession().setAttribute(Constants.FIELDS.getValue(), responseMap);
			httpRequest.getSession().setAttribute(FieldType.INTERNAL_ORIG_TXN_ID.getName(),
					responseMap.get(FieldType.TXN_ID.getName()));
			httpRequest.getSession().setAttribute(FieldType.ACQUIRER_TYPE.getName(),
					responseMap.get(FieldType.ACQUIRER_TYPE.getName()));
			responseMap.put(FieldType.RETURN_URL.getName(),
					httpRequest.getSession().getAttribute(FieldType.RETURN_URL.getName()).toString());
//			if (responseMap.get(FieldType.PG_TXN_MESSAGE.getName()) != null) {
//				setResponseMessage(responseMap.get(FieldType.PG_TXN_MESSAGE.getName()));
//			} else {
//				setResponseMessage(responseMap.get(FieldType.RESPONSE_MESSAGE.getName()));
//			}

//			setPgRefNum(responseMap.get(FieldType.PG_REF_NUM.getName()));
//			setResponseCode(responseMap.get(FieldType.RESPONSE_CODE.getName()));

			// Incase of UPI via BOB, redirect user to bob url
			if (StringUtils.isNotBlank(responseMap.get(FieldType.ACQUIRER_TYPE.getName())) && responseMap
					.get(FieldType.ACQUIRER_TYPE.getName()).equalsIgnoreCase(AcquirerType.BOB.getCode())) {
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REDIRECT_UPI.getCode());
//				setResponseCode(ErrorType.REDIRECT_UPI.getCode());
				responseMap.put("REDIRECT_URL", PropertiesManager.propertiesMap.get("UPIRedirectURL"));
//				setRedirectURL(PropertiesManager.propertiesMap.get("UPIRedirectURL"));
				httpRequest.getSession().setAttribute(FieldType.TXN_KEY.getName(),
						responseMap.get(FieldType.TXN_KEY.getName()));
				httpRequest.getSession().setAttribute(FieldType.BOB_FINAL_REQUEST.getName(),
						responseMap.get(FieldType.BOB_FINAL_REQUEST.getName()));
			}

			// Incase of UPI via FSSPAY, redirect user to FSSPAY URL
			if (StringUtils.isNotBlank(responseMap.get(FieldType.ACQUIRER_TYPE.getName())) && responseMap
					.get(FieldType.ACQUIRER_TYPE.getName()).equalsIgnoreCase(AcquirerType.FSSPAY.getCode())) {
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REDIRECT_UPI.getCode());
				// setResponseCode(ErrorType.REDIRECT_UPI.getCode());
				responseMap.put("REDIRECT_URL", PropertiesManager.propertiesMap.get("UPIRedirectURL"));
				// setRedirectURL(PropertiesManager.propertiesMap.get("UPIRedirectURL"));
				httpRequest.getSession().setAttribute(FieldType.TXN_KEY.getName(),
						responseMap.get(FieldType.TXN_KEY.getName()));
				httpRequest.getSession().setAttribute(FieldType.FSS_PAY_FINAL_REQUEST.getName(),
						responseMap.get(FieldType.FSS_PAY_FINAL_REQUEST.getName()));
			}

			// Incase of UPI via Safexpay, redirect user to Safexpay URL
			if (StringUtils.isNotBlank(responseMap.get(FieldType.ACQUIRER_TYPE.getName())) && responseMap
					.get(FieldType.ACQUIRER_TYPE.getName()).equalsIgnoreCase(AcquirerType.SAFEXPAY.getCode())) {
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REDIRECT_UPI.getCode());
				// setResponseCode(ErrorType.REDIRECT_UPI.getCode());
				responseMap.put("REDIRECT_URL", PropertiesManager.propertiesMap.get("UPIRedirectURL"));
				// setRedirectURL(PropertiesManager.propertiesMap.get("UPIRedirectURL"));
				httpRequest.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
						responseMap.get(FieldType.MERCHANT_ID.getName()));
				httpRequest.getSession().setAttribute(FieldType.TXN_KEY.getName(),
						responseMap.get(FieldType.TXN_KEY.getName()));
				httpRequest.getSession().setAttribute(FieldType.PG_REF_NUM.getName(),
						responseMap.get(FieldType.PG_REF_NUM.getName()));
				httpRequest.getSession().setAttribute(FieldType.ADF1.getName(),
						responseMap.get(FieldType.ADF1.getName()));
			}

			// Incase of UPI via PAYPHI, redirect user to PAYPHI url
			if (StringUtils.isNotBlank(responseMap.get(FieldType.ACQUIRER_TYPE.getName())) && responseMap
					.get(FieldType.ACQUIRER_TYPE.getName()).equalsIgnoreCase(AcquirerType.PAYPHI.getCode())) {
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REDIRECT_UPI.getCode());
//				setResponseCode(ErrorType.REDIRECT_UPI.getCode());
				responseMap.put("REDIRECT_URL", PropertiesManager.propertiesMap.get("UPIRedirectURL"));
//				setRedirectURL(PropertiesManager.propertiesMap.get("UPIRedirectURL"));
				httpRequest.getSession().setAttribute(FieldType.TXN_KEY.getName(),
						responseMap.get(FieldType.TXN_KEY.getName()));
				httpRequest.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
						responseMap.get(FieldType.MERCHANT_ID.getName()));
				httpRequest.getSession().setAttribute(FieldType.PASSWORD.getName(),
						responseMap.get(FieldType.PASSWORD.getName()));
				httpRequest.getSession().setAttribute(FieldType.PAYPHI_FINAL_REQUEST.getName(),
						responseMap.get(FieldType.PAYPHI_FINAL_REQUEST.getName()));
			}

			// Incase of UPI via PAYU, redirect user to PAYU URL
			if (StringUtils.isNotBlank(responseMap.get(FieldType.ACQUIRER_TYPE.getName())) && responseMap
					.get(FieldType.ACQUIRER_TYPE.getName()).equalsIgnoreCase(AcquirerType.PAYU.getCode())) {
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REDIRECT_UPI.getCode());
//				setResponseCode(ErrorType.REDIRECT_UPI.getCode());
				responseMap.put("REDIRECT_URL", PropertiesManager.propertiesMap.get("UPIRedirectURL"));
//				setRedirectURL(PropertiesManager.propertiesMap.get("UPIRedirectURL"));
				httpRequest.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
						responseMap.get(FieldType.MERCHANT_ID.getName()));
				httpRequest.getSession().setAttribute(FieldType.PASSWORD.getName(),
						responseMap.get(FieldType.PASSWORD.getName()));
				httpRequest.getSession().setAttribute(FieldType.PG_REF_NUM.getName(),
						responseMap.get(FieldType.PG_REF_NUM.getName()));
				httpRequest.getSession().setAttribute(FieldType.PAYU_FINAL_REQUEST.getName(),
						responseMap.get(FieldType.PAYU_FINAL_REQUEST.getName()));
			}

			if (responseMap.get(FieldType.ACQUIRER_TYPE.getName()).equalsIgnoreCase(AcquirerType.APEXPAY.getCode())) {
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REDIRECT_UPI.getCode());
//				setResponseCode(ErrorType.REDIRECT_UPI.getCode());
				responseMap.put("REDIRECT_URL", PropertiesManager.propertiesMap.get("UPIRedirectURL"));
				httpRequest.getSession().setAttribute(FieldType.TXN_KEY.getName(),
						responseMap.get(FieldType.TXN_KEY.getName()));
				httpRequest.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
						responseMap.get(FieldType.MERCHANT_ID.getName()));
				httpRequest.getSession().setAttribute(FieldType.PASSWORD.getName(),
						responseMap.get(FieldType.PASSWORD.getName()));
				httpRequest.getSession().setAttribute(FieldType.APEXPAY_FINAL_REQUEST.getName(),
						responseMap.get(FieldType.APEXPAY_FINAL_REQUEST.getName()));
			}
			
			// Incase of UPI via AIRPAY, redirect user to airpay url
			if (StringUtils.isNotBlank(responseMap.get(FieldType.ACQUIRER_TYPE.getName())) && responseMap
					.get(FieldType.ACQUIRER_TYPE.getName()).equalsIgnoreCase(AcquirerType.AIRPAY.getCode())) {
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REDIRECT_UPI.getCode());
				responseMap.put("REDIRECT_URL", PropertiesManager.propertiesMap.get("UPIRedirectURL"));
				httpRequest.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
						responseMap.get(FieldType.MERCHANT_ID.getName()));
				httpRequest.getSession().setAttribute(FieldType.TXN_KEY.getName(),
						responseMap.get(FieldType.TXN_KEY.getName()));
				httpRequest.getSession().setAttribute(FieldType.ADF1.getName(),
						responseMap.get(FieldType.ADF1.getName()));
				httpRequest.getSession().setAttribute(FieldType.ADF2.getName(),
						responseMap.get(FieldType.ADF2.getName()));
			}

			// Incase of UPI via Qaicash, redirect user to Qaicash url
			if (StringUtils.isNotBlank(responseMap.get(FieldType.ACQUIRER_TYPE.getName())) && responseMap
					.get(FieldType.ACQUIRER_TYPE.getName()).equalsIgnoreCase(AcquirerType.QAICASH.getCode())) {
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REDIRECT_UPI.getCode());
				responseMap.put("REDIRECT_URL", PropertiesManager.propertiesMap.get("UPIRedirectURL"));
				httpRequest.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
						responseMap.get(FieldType.MERCHANT_ID.getName()));
				httpRequest.getSession().setAttribute(FieldType.TXN_KEY.getName(),
						responseMap.get(FieldType.TXN_KEY.getName()));
			}
			
			// Incase of UPI via Globalpay, redirect user to Qaicash url
				if (StringUtils.isNotBlank(responseMap.get(FieldType.ACQUIRER_TYPE.getName())) && responseMap
						.get(FieldType.ACQUIRER_TYPE.getName()).equalsIgnoreCase(AcquirerType.GLOBALPAY.getCode())) {
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REDIRECT_UPI.getCode());
					responseMap.put("REDIRECT_URL", PropertiesManager.propertiesMap.get("UPIRedirectURL"));
					httpRequest.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					httpRequest.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
				}
			
			// Incase of UPI via Floxypay, redirect user to Floxypay url
			if (StringUtils.isNotBlank(responseMap.get(FieldType.ACQUIRER_TYPE.getName())) && responseMap
					.get(FieldType.ACQUIRER_TYPE.getName()).equalsIgnoreCase(AcquirerType.FLOXYPAY.getCode())) {
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REDIRECT_UPI.getCode());
				responseMap.put("REDIRECT_URL", PropertiesManager.propertiesMap.get("UPIRedirectURL"));
				httpRequest.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
						responseMap.get(FieldType.MERCHANT_ID.getName()));
				httpRequest.getSession().setAttribute(FieldType.TXN_KEY.getName(),
						responseMap.get(FieldType.TXN_KEY.getName()));
			}
						
			// Incase of UPI via DigitalSolution, redirect user to DigitalSolution url
			if (StringUtils.isNotBlank(responseMap.get(FieldType.ACQUIRER_TYPE.getName())) && responseMap
					.get(FieldType.ACQUIRER_TYPE.getName()).equalsIgnoreCase(AcquirerType.DIGITALSOLUTIONS.getCode())) {
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REDIRECT_UPI.getCode());
				responseMap.put("REDIRECT_URL", PropertiesManager.propertiesMap.get("UPIRedirectURL"));
				httpRequest.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
						responseMap.get(FieldType.MERCHANT_ID.getName()));
				httpRequest.getSession().setAttribute(FieldType.TXN_KEY.getName(),
						responseMap.get(FieldType.TXN_KEY.getName()));
			}
			
			// Incase of UPI via Grezpay, redirect user to Grezpay url
			if (StringUtils.isNotBlank(responseMap.get(FieldType.ACQUIRER_TYPE.getName())) && responseMap
					.get(FieldType.ACQUIRER_TYPE.getName()).equalsIgnoreCase(AcquirerType.GREZPAY.getCode())) {
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REDIRECT_UPI.getCode());
				responseMap.put("REDIRECT_URL", PropertiesManager.propertiesMap.get("UPIRedirectURL"));
				httpRequest.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
						responseMap.get(FieldType.MERCHANT_ID.getName()));
				httpRequest.getSession().setAttribute(FieldType.TXN_KEY.getName(),
						responseMap.get(FieldType.TXN_KEY.getName()));
			}

			// Incase of UPI via UPIGateway, redirect user to UPIGateway url
			if (StringUtils.isNotBlank(responseMap.get(FieldType.ACQUIRER_TYPE.getName())) && responseMap
					.get(FieldType.ACQUIRER_TYPE.getName()).equalsIgnoreCase(AcquirerType.UPIGATEWAY.getCode())) {
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REDIRECT_UPI.getCode());
				responseMap.put("REDIRECT_URL", PropertiesManager.propertiesMap.get("UPIRedirectURL"));
				httpRequest.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
						responseMap.get(FieldType.MERCHANT_ID.getName()));
			}
			
			responseMap.put(FieldType.TXNTYPE.getName(), responseMap.get(FieldType.PAYMENT_TYPE.getName()));
//			setTxnType(responseMap.get(FieldType.PAYMENT_TYPE.getName()));
			responseMap.removeInternalFields();
			responseMap.removeSecureFields();
			responseMap.remove(FieldType.HASH.getName());
			// E-ticketing
			if (StringUtils.isNotBlank(pgFlag) && pgFlag.equals(Constants.Y.getValue())) {
				responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
				String encData = responseCreator.createCrisUpiResponse(responseMap);
				responseFields.put(Constants.ENC_DATA.getValue(), encData);
				responseFields.put(FieldType.RETURN_URL.getName(),
						httpRequest.getSession().getAttribute(FieldType.RETURN_URL.getName()).toString());
			} else {
				responseMap.put(FieldType.HASH.getName(), Hasher.getHash(responseMap));
				for (String key : responseMap.keySet()) {
					responseFields.put(key, responseMap.get(key));
				}
//				setResponseFields(responseMap.getFields());
			}

			return responseFields;

		} catch (SystemException systemException) {
			logger.error("systemException", systemException);

		} catch (Exception exception) {
			logger.error("Error handling of transaction", exception);
//			return ERROR;
			String path = httpRequest.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host") + "/pgui/jsp/error";
				httpResponse.sendRedirect(resultPath);
			}
			httpResponse.sendRedirect("error");
		}
		return responseFields;
	}

}
