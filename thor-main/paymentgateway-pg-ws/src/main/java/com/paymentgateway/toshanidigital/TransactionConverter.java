
package com.paymentgateway.toshanidigital;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;

/**
 * @author Shaiwal
 *
 */
@Service("toshanidigitalTransactionConverter")
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

		logger.info("Preparing Toshani payment request");

		JSONObject jsonRequest = new JSONObject();

		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		transaction.setPayment_amount(amount);

		jsonRequest.put(Constants.payment_amount, transaction.getPayment_amount());
		jsonRequest.put(Constants.access_token, transaction.getAccess_token());
		jsonRequest.put(Constants.name, transaction.getName());
		jsonRequest.put(Constants.number, transaction.getNumber());
		jsonRequest.put(Constants.vpa, fields.get(FieldType.PAYER_ADDRESS.getName()));

		logger.info("Prepared Toshani payment request : {}", jsonRequest.toString());

		return jsonRequest.toString();

	}

	public String refundRequest(Fields fields, Transaction transaction) throws SystemException { // No Refund API for
																									// Toshani

		JSONArray requestArray = new JSONArray();
		return requestArray.toString();
	}

	public String statusEnquiryRequest(Fields fields, Transaction transaction) throws SystemException {

		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put(Constants.orderid, transaction.getOrderid());
		jsonRequest.put(Constants.access_token, transaction.getAccess_token());
		return jsonRequest.toString();
	}

	public Transaction toTransaction(String response, Fields fields) {

		Transaction transaction = new Transaction();
		try {

			JSONObject resJson = new JSONObject(response);
			
			if (resJson.has("order_id")) {
				transaction.setOrder_id(resJson.get("order_id").toString());
			}
			
			if (resJson.has("payments_status")) {
				transaction.setPayments_status(resJson.get("payments_status").toString());
			}
			
			if (resJson.has("qr_id")) {
				transaction.setQr_id(resJson.get("qr_id").toString());
			}
			
			if (resJson.has("payment_amount")) {
				transaction.setPayment_amount(resJson.get("payment_amount").toString());
			}
			
			if (resJson.has("rrn")) {
				transaction.setRrn(resJson.get("rrn").toString());
			}
			
			if (resJson.has("vpa")) {
				transaction.setVpa(resJson.get("vpa").toString());
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

	
	public Transaction toTransactionCollect(JSONObject response) {

		Transaction transaction = new Transaction();
		
		if (response.has("result")) {
			if (response.get("result").toString().equalsIgnoreCase("1")) {
				transaction.setResult(response.get("result").toString());
				transaction.setMessage(response.get("message").toString());
				
				if (response.has("data")) {
					JSONObject dataJson = response.getJSONArray("data").getJSONObject(0);
					
					if (dataJson.has("order_id")) {
						transaction.setOrder_id(dataJson.get("order_id").toString());
					}
					
					if (dataJson.has("payments_status")) {
						transaction.setPayments_status(dataJson.get("payments_status").toString());
					}
					
					if (dataJson.has("qr_id")) {
						transaction.setQr_id(dataJson.get("qr_id").toString());
					}
					
					if (dataJson.has("status")) {
						transaction.setStatus(dataJson.get("status").toString());
					}

					if (dataJson.has("vpa")) {
						transaction.setVpa(dataJson.get("vpa").toString());
					}
					
					if (dataJson.has("rrn")) {
						transaction.setRrn(dataJson.get("rrn").toString());
					}
					
				}
			}
			else {
				transaction.setResult(response.get("result").toString());
				transaction.setMessage(response.get("message").toString());
			}
		}
		
		return transaction;
	}
	
	
	public Transaction toStatusTransaction(String responseStr) {

		Transaction transaction = new Transaction();
		try {
			JSONObject response = new JSONObject(responseStr);

			if (response.has("result")) {
					transaction.setResult(response.get("result").toString());
					transaction.setMessage(response.get("message").toString());
					
					if (response.has("data")) {
						JSONObject dataJson = response.getJSONArray("data").getJSONObject(0);
						
						if (dataJson.has("order_id")) {
							transaction.setOrder_id(dataJson.get("order_id").toString());
						}
						
						if (dataJson.has("payments_status")) {
							transaction.setPayments_status(dataJson.get("payments_status").toString());
						}
						
						if (dataJson.has("qr_id")) {
							transaction.setQr_id(dataJson.get("qr_id").toString());
						}
						
						if (dataJson.has("status")) {
							transaction.setStatus(dataJson.get("status").toString());
						}

						if (dataJson.has("vpa")) {
							transaction.setVpa(dataJson.get("vpa").toString());
						}
						
						if (dataJson.has("rrn")) {
							transaction.setRrn(dataJson.get("rrn").toString());
						}
						
						if (dataJson.has("payment_amount")) {
							transaction.setPayment_amount(dataJson.get("payment_amount").toString());
						}
						
				}
				
				else {
					transaction.setResult(response.get("result").toString());
					transaction.setMessage(response.get("message").toString());
				}
			}
			

		} catch (Exception e) {
			logger.error("Exception ", e);
		}

		return transaction;
	}// toTransaction()


}
