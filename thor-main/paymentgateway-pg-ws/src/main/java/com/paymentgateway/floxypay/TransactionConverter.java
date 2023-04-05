package com.paymentgateway.floxypay;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;

/**
 * @author Shaiwal
 *
 */
@Service("floxypayTransactionConverter")
public class TransactionConverter {

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

		logger.info("Preparing Floxypay payment request");

		JSONObject jsonRequest = new JSONObject();

		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		transaction.setAmount(amount);
		
		jsonRequest.put(Constants.amount, amount);
		jsonRequest.put(Constants.orderid, transaction.getOrderid());
		jsonRequest.put(Constants.customerName, transaction.getCustomerName());
		jsonRequest.put(Constants.customerMobile, transaction.getCustomerMobile());
		jsonRequest.put(Constants.CustomerEmail, transaction.getCustomerEmail());

		logger.info("Prepared Floxypay payment request : {}", jsonRequest.toString());

		return jsonRequest.toString();

	}

	public String refundRequest(Fields fields, Transaction transaction) throws SystemException {
		// No Refund API for Floxypay

		JSONArray requestArray = new JSONArray();
		return requestArray.toString();
	}

	public String statusEnquiryRequest(Fields fields, Transaction transaction) throws SystemException {

		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put("order_id", transaction.getOrderid());
		return jsonRequest.toString();
	}

	public Transaction toTransaction(String response) {

		Transaction transaction = new Transaction();
		try {

			JSONObject resJson = new JSONObject(response);

			
			if (response.contains(Constants.orderid)) {
				transaction.setOrderid(resJson.get(Constants.orderid).toString());
			}
			if (response.contains(Constants.systemid)) {
				transaction.setSystemid(resJson.get(Constants.systemid).toString());
			}
			if (response.contains(Constants.amount)) {
				transaction.setAmount(resJson.get(Constants.amount).toString());
			}
			if (response.contains(Constants.status)) {
				transaction.setStatus(resJson.get(Constants.status).toString());
			}
		}

		catch (Exception e) {
			logger.error("Exception", e);
		}
		logger.info("Response Status" + transaction.getStatus());
		return transaction;
	}// toTransaction()

	public Transaction toTransactionRefund(String response) {

		Transaction transaction = new Transaction();
		return transaction;
	}

	public Transaction toStatusTransaction(String response) {

		Transaction transaction = new Transaction();
		JSONObject resJson = new JSONObject(response);
		
		if (response.contains(Constants.systemid)) {
			transaction.setSystemid(resJson.get(Constants.systemid).toString());
		}
		if (response.contains(Constants.amount)) {
			transaction.setAmount(resJson.get(Constants.amount).toString());
		}
		if (response.contains(Constants.status)) {
			transaction.setStatus(resJson.get(Constants.status).toString());
		}
		
		return transaction;
	}// toTransaction()

	public String getPaymentUrl(String response,Fields fields) throws SystemException{
		String paymentUrl = null;
		try {

			JSONObject resJson = new JSONObject(response);
			
			if (response.contains("url")) {
				paymentUrl = resJson.get("url").toString();
			}
			else {
				logger.info("Payment Response does not contain payment URL");
			}
			
			if (response.contains("orderid")) {
				String acqId = resJson.get("orderid").toString();
				fields.put(FieldType.ACQ_ID.getName(), acqId);
				fields.put(FieldType.RRN.getName(), acqId);
			}

			return paymentUrl;
		} catch (Exception e) {
			logger.error("Exception in parsing payment response to get payment URL ", e);
		}
		return paymentUrl;

	}

}
