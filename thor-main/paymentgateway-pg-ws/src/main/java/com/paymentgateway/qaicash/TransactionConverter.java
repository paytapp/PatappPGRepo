package com.paymentgateway.qaicash;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;
import com.paymentgateway.pg.core.util.QaicashUtil;

/**
 * @author Shaiwal
 *
 */
@Service("qaicashTransactionConverter")
public class TransactionConverter {

	@Autowired
	private QaicashUtil qaicashUtil;

	@Autowired
	private UserSettingDao userSettingDao;

	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;

	private static final Logger logger = LoggerFactory.getLogger(TransactionConverter.class.getName());

	@SuppressWarnings("incomplete-switch")
	public String perpareRequest(Fields fields, Transaction transaction) throws SystemException {

		String request = null;

		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case REFUND:
			request = refundRequest(fields, transaction);
			break;
		case SALE:
			request = saleRequest(fields, transaction);
			break;
		case STATUS:
			request = statusEnquiryRequest(fields, transaction);
			break;
		}
		return request;

	}

	public String saleRequest(Fields fields, Transaction transaction) throws SystemException {

		logger.info("Preparing qaicash payment request");

		JSONObject jsonRequest = new JSONObject();
		StringBuilder hmacString = new StringBuilder();

		String returnUrl = null;
		UserSettingData userSetting = userSettingDao.fetchDataUsingPayId((fields.get(FieldType.PAY_ID.getName())));
		if (userSetting.isAllowCustomHostedUrl()) {
			returnUrl = (userSetting.getCustomHostedUrl() + "/pgui/jsp/qaicashResponse");
		} else {
			returnUrl = PropertiesManager.propertiesMap.get(Constants.QAICASH_RETURN_URL);
		}

		returnUrl = returnUrl.concat(fields.get(FieldType.PG_REF_NUM.getName()));

		String callbackUrl = PropertiesManager.propertiesMap.get(Constants.QAICASH_CALLBACK_URL);

		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		transaction.setAmount(amount);

		jsonRequest.put(Constants.merchantId, transaction.getMerchantId());
		jsonRequest.put(Constants.currency, transaction.getCurrency());
		jsonRequest.put(Constants.orderId, transaction.getOrderId());
		jsonRequest.put(Constants.amount, amount);
		jsonRequest.put(Constants.dateTime, transaction.getDateTime());
		jsonRequest.put(Constants.language, transaction.getLanguage());
		jsonRequest.put(Constants.depositorUserId, transaction.getDepositorUserId());
		jsonRequest.put(Constants.depositMethod, transaction.getDepositMethod());
		jsonRequest.put(Constants.redirectUrl, returnUrl);
		jsonRequest.put(Constants.callbackUrl, callbackUrl);

		if (StringUtils.isNotBlank(transaction.getDepositorName())) {
			jsonRequest.put(Constants.depositorName, transaction.getDepositorName());
		}

		hmacString.append(transaction.getMerchantId());
		hmacString.append("|");
		hmacString.append(transaction.getOrderId());
		hmacString.append("|");
		hmacString.append(transaction.getAmount());
		hmacString.append("|");
		hmacString.append(transaction.getCurrency());
		hmacString.append("|");
		hmacString.append(transaction.getDateTime());
		hmacString.append("|");
		hmacString.append(transaction.getDepositorUserId());
		hmacString.append("|");
		hmacString.append(transaction.getDepositMethod());

		String hmac = qaicashUtil.HMAC_SHA256(transaction.getMerchantApiKey(), hmacString.toString());
		jsonRequest.put(Constants.messageAuthenticationCode, hmac);

		logger.info("Prepared qaicash payment request : {}", jsonRequest.toString());

		return jsonRequest.toString();

	}

	public String refundRequest(Fields fields, Transaction transaction) throws SystemException {
		// No Refund API for Qaicash

		JSONArray requestArray = new JSONArray();
		return requestArray.toString();
	}

	public String statusEnquiryRequest(Fields fields, Transaction transaction) throws SystemException {

		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put(Constants.merchantId, transaction.getMerchantId());
		jsonRequest.put(Constants.orderId, transaction.getOrderId());

		StringBuilder hmacString = new StringBuilder();
		hmacString.append(transaction.getMerchantId());
		hmacString.append("|");
		hmacString.append(transaction.getOrderId());

		String hmac = qaicashUtil.HMAC_SHA256(transaction.getMerchantApiKey(), hmacString.toString());

		jsonRequest.put(Constants.messageAuthenticationCode, hmac);

		return jsonRequest.toString();
	}

	public Transaction toTransaction(String response, Fields fields) {

		Transaction transaction = new Transaction();
		try {

			JSONObject resJson = new JSONObject(response);

			transaction.setMerchantId(fields.get(FieldType.MERCHANT_ID.getName()));
			transaction.setMerchantApiKey(fields.get(FieldType.TXN_KEY.getName()));

			if (response.contains(FieldType.PG_REF_NUM.getName())) {
				transaction.setOrderId(resJson.get(FieldType.PG_REF_NUM.getName()).toString());
			}
		}

		catch (Exception e) {
			logger.error("Exception", e);
		}

		return transaction;
	}// toTransaction()

	public Transaction toTransactionRefund(String response) {

		Transaction transaction = new Transaction();
		return transaction;
	}

	public Transaction toStatusTransaction(String response) {

		Transaction transaction = new Transaction();
		try {

			if (response.equalsIgnoreCase("No Such Transaction Found")) {
				transaction.setStatus("Failed");
				return transaction;
			}
			JSONObject resJson = new JSONObject(response);

			transaction.setStatus(resJson.get("status").toString());
			transaction.setOrderId(resJson.get("orderId").toString());
			transaction.setTransactionId(resJson.get("transactionId").toString());
			transaction.setAmount(resJson.get("amount").toString());
		} catch (Exception e) {
			logger.error("Exception ", e);
		}

		return transaction;
	}// toTransaction()

	public String getTextBetweenTags(String text, String tag1, String tag2) {

		int leftIndex = text.indexOf(tag1);
		if (leftIndex == -1) {
			return null;
		}

		int rightIndex = text.indexOf(tag2);
		if (rightIndex != -1) {
			leftIndex = leftIndex + tag1.length();
			return text.substring(leftIndex + 9, rightIndex - 3);
		}

		return null;
	}// getTextBetweenTags()

	public String getPaymentUrl(String response, Fields fields) {
		String paymentUrl = null;
		try {

			JSONObject resJson = new JSONObject(response);

			String success = "";
			if (response.contains("success")) {
				success = resJson.get("success").toString();
			}

			if (success.equalsIgnoreCase("true")) {

				JSONObject depositTxnJson = resJson.getJSONObject("depositTransaction");
				fields.put(FieldType.ACQ_ID.getName(), depositTxnJson.get("transactionId").toString());
				fields.put(FieldType.PG_RESPONSE_MSG.getName(), depositTxnJson.get("notes").toString());

				JSONObject paymentPageSessionJson = resJson.getJSONObject("paymentPageSession");
				paymentUrl = paymentPageSessionJson.get("paymentPageUrl").toString();
				paymentUrl = paymentUrl.concat("/redirect");
			} else {
				logger.info("Payment Response does not contain payment URL , {} ", response);
			}

			return paymentUrl;
		} catch (Exception e) {
			logger.error("Exception in parsing payment response to get payment URL ", e);
		}
		return paymentUrl;

	}

}
