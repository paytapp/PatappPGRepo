package com.paymentgateway.globalpay;

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
@Service("globalpayTransactionConverter")
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
		case STATUS:
			request = statusEnquiryRequest(fields, transaction);
			break;
		}
		return request;

	}

	public String saleRequest(Fields fields, Transaction transaction) throws SystemException {

		logger.info("Preparing Globalpay payment request");

		JSONObject jsonRequest = new JSONObject();

		String returnUrl = null;
		UserSettingData userSetting = userSettingDao.fetchDataUsingPayId((fields.get(FieldType.PAY_ID.getName())));
		if (userSetting.isAllowCustomHostedUrl()) {
			returnUrl = (userSetting.getCustomHostedUrl() + "/pgui/jsp/globalpayResponse?pgRefNo=");
		} else {
			returnUrl = PropertiesManager.propertiesMap.get(Constants.GLOBALPAY_RETURN_URL);
		}

		returnUrl = returnUrl.concat(fields.get(FieldType.PG_REF_NUM.getName()));

		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		transaction.setPayment_amount(amount);

		jsonRequest.put(Constants.merchant_order_id, transaction.getMerchant_order_id());
		jsonRequest.put(Constants.payment_amount, transaction.getPayment_amount());
		jsonRequest.put(Constants.return_url, returnUrl);
		jsonRequest.put(Constants.customer_id, transaction.getCustomer_id());
		jsonRequest.put(Constants.customer_name, transaction.getCustomer_name());
		jsonRequest.put(Constants.customer_email, transaction.getCustomer_email());
		jsonRequest.put(Constants.customer_mobile, transaction.getCustomer_mobile());
		jsonRequest.put(Constants.player_register_date, transaction.getPlayer_register_date());
		jsonRequest.put(Constants.player_deposit_amount, transaction.getPlayer_deposit_amount());
		jsonRequest.put(Constants.player_deposit_count, transaction.getPlayer_deposit_count());

		logger.info("Prepared Globalpay payment request : {}", jsonRequest.toString());

		return jsonRequest.toString();

	}

	public String refundRequest(Fields fields, Transaction transaction) throws SystemException {
		// No Refund API for Globalpay

		JSONArray requestArray = new JSONArray();
		return requestArray.toString();
	}

	public String statusEnquiryRequest(Fields fields, Transaction transaction) throws SystemException {

		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put(Constants.merchant_order_id, transaction.getMerchant_order_id());
		return jsonRequest.toString();
	}

	public Transaction toTransaction(String response, Fields fields) {

		Transaction transaction = new Transaction();
		try {

			
			  JSONObject resJson = new JSONObject(response);
			  
			  transaction.setMerchant_order_id(resJson.get("pgRefNo").toString());
			  transaction.setTransaction_id(resJson.get(Constants.transaction_id).toString());
			  
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
			
			if (response.contains("status")) {
				if (resJson.get("status").toString().equalsIgnoreCase("true")) {
					logger.info("Status is true for Status Enquiry");
					
					transaction.setMessage(resJson.get("message").toString());
					JSONObject dataJson = resJson.getJSONObject("data");
					
					transaction.setTransaction_id(dataJson.get("transaction_id").toString());
					transaction.setMerchant_order_id(dataJson.get("merchant_order_id").toString());
					transaction.setPayment_amount(dataJson.get("payment_amount").toString());
					transaction.setPayment_status(dataJson.get("payment_status").toString());
					transaction.setBank_rrn(dataJson.get("bank_rrn").toString());
				}
				else {
					
				}
			}
			return transaction;
		} catch (Exception e) {
			logger.error("Exception ", e);
		}

		return transaction;
	}// toTransaction()


	public String getPaymentUrl(String response, Fields fields) {
		String paymentUrl = null;
		try {

			JSONObject resJson = new JSONObject(response);

			String status = "";
			if (response.contains("status")) {
				status = resJson.get("status").toString();
			}

			if (status.equalsIgnoreCase("true")) {
				fields.put(FieldType.ACQ_ID.getName(), resJson.get("transaction_id").toString());
				fields.put(FieldType.PG_RESPONSE_MSG.getName(), resJson.get("message").toString());

				paymentUrl = resJson.get("checkout_url").toString();
			} else {
				fields.put(FieldType.PG_RESPONSE_MSG.getName(), resJson.get("message").toString());
				logger.info("Payment Response does not contain payment URL , {} ", response);
			}

			return paymentUrl;
		} catch (Exception e) {
			logger.error("Exception in parsing Global payment response to get payment URL ", e);
		}
		return paymentUrl;

	}

}
