package com.paymentgateway.digitalsolution;

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
@Service("digitalsolutionTransactionConverter")
public class TransactionConverter {

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

		String returnUrl = null;
		UserSettingData userSetting = userSettingDao.fetchDataUsingPayId((fields.get(FieldType.PAY_ID.getName())));
		if (userSetting.isAllowCustomHostedUrl()) {
			returnUrl = (userSetting.getCustomHostedUrl() + "/pgui/jsp/digSolResponse?pgRefNo=");
		} else {
			returnUrl = PropertiesManager.propertiesMap.get(Constants.DIGITALSOLUTION_RETURN_URL);
		}

		returnUrl = returnUrl.concat(fields.get(FieldType.PG_REF_NUM.getName()));

		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		transaction.setAmount(amount);

		jsonRequest.put(Constants.token, transaction.getToken());
		jsonRequest.put(Constants.clint_ref_id, transaction.getClint_ref_id());
		jsonRequest.put(Constants.amount, amount);
		jsonRequest.put(Constants.remark, transaction.getRemark());
		jsonRequest.put(Constants.surl, returnUrl);
		jsonRequest.put(Constants.furl, returnUrl);

		logger.info("Prepared Digital Solutions payment request : {}", jsonRequest.toString());
		return jsonRequest.toString();

	}

	public String refundRequest(Fields fields, Transaction transaction) throws SystemException {
		// No Refund API for Qaicash

		JSONArray requestArray = new JSONArray();
		return requestArray.toString();
	}

	public String statusEnquiryRequest(Fields fields, Transaction transaction) throws SystemException {

		JSONObject jsonRequest = new JSONObject();
		
		jsonRequest.put(Constants.token, transaction.getToken());
		jsonRequest.put(Constants.clint_ref_id, transaction.getClint_ref_id());
		
		return jsonRequest.toString();
	}

	public Transaction toTransaction(String response, Fields fields) {

		Transaction transaction = new Transaction();
		try {

			JSONObject resJson = new JSONObject(response);

			transaction.setToken(fields.get(FieldType.MERCHANT_ID.getName()));

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
