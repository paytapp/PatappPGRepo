package com.paymentgateway.grezpay;

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

/**
 * @author Shaiwal
 *
 */
@Service("grezpayTransactionConverter")
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
		case SALE:
			request = saleRequest(fields, transaction);
			break;
		}
		return request;

	}

	public String saleRequest(Fields fields, Transaction transaction) throws SystemException {

		logger.info("Preparing Grezpay payment request");

		JSONObject jsonRequest = new JSONObject();

		String returnUrl = null;
		UserSettingData userSetting = userSettingDao.fetchDataUsingPayId((fields.get(FieldType.PAY_ID.getName())));
		if (userSetting.isAllowCustomHostedUrl()) {
			returnUrl = (userSetting.getCustomHostedUrl() + "/pgui/jsp/grepayResponse?pgRefNo=");
		} else {
			returnUrl = PropertiesManager.propertiesMap.get(Constants.GREZPAY_RETURN_URL);
		}

		returnUrl = returnUrl.concat(fields.get(FieldType.PG_REF_NUM.getName()));
		transaction.setSuccess_url(returnUrl);
		transaction.setFail_url(returnUrl);
		transaction.setPg_cancel_url(returnUrl);
		
		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		transaction.setAmount(amount);

		
		jsonRequest.put(Constants.curr_code, transaction.getCurr_code());
		jsonRequest.put(Constants.amount, transaction.getAmount());
		jsonRequest.put(Constants.merchant_order_token, transaction.getMerchant_order_token());
		jsonRequest.put(Constants.customer_email, transaction.getCustomer_email());
		jsonRequest.put(Constants.customer_mobile, transaction.getCustomer_mobile());
		jsonRequest.put(Constants.customer_first_name, transaction.getCustomer_first_name());
		jsonRequest.put(Constants.customer_last_name, transaction.getCustomer_last_name());
		jsonRequest.put(Constants.success_url, transaction.getSuccess_url());
		jsonRequest.put(Constants.fail_url, transaction.getFail_url());
		jsonRequest.put(Constants.pg_cancel_url, transaction.getPg_cancel_url());
		jsonRequest.put(Constants.api_key, transaction.getApi_key());

		logger.info("Prepared Grezpay payment request : {}", jsonRequest.toString());

		return jsonRequest.toString();

	}

	public String refundRequest(Fields fields, Transaction transaction) throws SystemException {
		// No Refund API for grezpay

		JSONArray requestArray = new JSONArray();
		return requestArray.toString();
	}

	public String statusEnquiryRequest(Fields fields, Transaction transaction) throws SystemException {

		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put(Constants.identifier, transaction.getIdentifier());
		return jsonRequest.toString();
	}

	public Transaction toTransaction(String response, Fields fields) {

		Transaction transaction = new Transaction();
		try {

			JSONObject resJson = new JSONObject(response);
			transaction.setMerchant_order_token(resJson.get(FieldType.PG_REF_NUM.getName()).toString());
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
			JSONObject resJson = new JSONObject(response);
			

		} catch (Exception e) {
			logger.error("Exception ", e);
		}

		return transaction;
	}// toTransaction()


	public String getPaymentUrl(String response, Fields fields) {
		String paymentUrl = null;
		try {

			JSONObject resJson = new JSONObject(response);

			String status = null;
			if (response.contains("status")) {
				status = resJson.get("status").toString();
			}

			if (status.equalsIgnoreCase("true")) {

				if (response.contains("transaction_id")) {
					fields.put(FieldType.ACQ_ID.getName(), resJson.get("transaction_id").toString());
				}
				if (response.contains("data")) {
					fields.put(FieldType.AUTH_CODE.getName(), resJson.get("data").toString());
				}
				if (response.contains("message")) {
					fields.put(FieldType.PG_RESPONSE_MSG.getName(), resJson.get("message").toString());
				}
				if (response.contains("status_code")) {
					fields.put(FieldType.PG_RESP_CODE.getName(), resJson.get("status_code").toString());
				}
				
				paymentUrl = resJson.get("ref_link").toString();
				
			} else {
				logger.info("Grezpay payment Response does not contain payment URL , {} ", response);
				
				if (response.contains("message")) {
					fields.put(FieldType.PG_RESPONSE_MSG.getName(), resJson.get("message").toString());
				}
				
			}

			return paymentUrl;
		} catch (Exception e) {
			logger.error("Exception in parsing Grezpay payment response to get payment URL ", e);
		}
		return paymentUrl;

	}

}
