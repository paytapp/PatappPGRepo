package com.paymentgateway.vepay;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.PayGateCryptoUtils;

@Service("vepayTransactionConverter")
public class TransactionConverter {

	private static final Logger logger = LoggerFactory.getLogger(TransactionConverter.class.getName());
	private static final String SEPARATOR = "|";

	@Autowired
	private PayGateCryptoUtils payGateCryptoUtils;
	
	@SuppressWarnings("incomplete-switch")
	public String perpareRequest(Fields fields, Transaction transaction) throws SystemException {

		String request = null;

		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case AUTHORISE:
		case ENROLL:
			break;
		case REFUND:
			request = refundRequest(fields, transaction);
			break;
		case SALE:
			request = saleRequest(fields, transaction);
			break;
		}
		return request.toString();

	}

	public String saleRequest(Fields fields, Transaction transaction) throws SystemException {

		try {

			return null;
			/*
			 * String intentRequest =
			 * PropertiesManager.propertiesMap.get(Constants.VEPAY_INTENT_REQUEST_URL);
			 * 
			 * 
			 * if
			 * (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.
			 * UPI.getCode())) {
			 * 
			 * upi_details.append(fields.get(FieldType.PAYER_ADDRESS.getName()));
			 * fields.put(FieldType.CARD_MASK.getName(),fields.get(FieldType.PAYER_ADDRESS.
			 * getName())); }
			 * 
			 * 
			 * 
			 * return outputHtml.toString();
			 */
		}

		catch (Exception exception) {
			logger.error("Exception in generating Payu sale request ", exception);
		}

		return null;
	}

	public String refundRequest(Fields fields, Transaction transaction) throws SystemException {

		try {

			JsonObject refRequest = new JsonObject();

			refRequest.addProperty("ag_id", fields.get(FieldType.ADF1.getName()).toString());
			refRequest.addProperty("me_id", fields.get(FieldType.MERCHANT_ID.getName()).toString());
			refRequest.addProperty("ag_ref", fields.get(FieldType.ACQ_ID.getName()).toString());
			refRequest.addProperty("refund_amount", transaction.getAmount());
			refRequest.addProperty("refund_reason", "CustomerRequestForRefund");
			
			logger.info("VePay refund Request before encryption for Order Id = " + fields.get(FieldType.ORDER_ID.getName()) +"  >>  " +refRequest.toString());
			refRequest.addProperty("ag_ref",
					payGateCryptoUtils.encrypt(fields.get(FieldType.ACQ_ID.getName()).toString(),
							fields.get(FieldType.TXN_KEY.getName()).toString()));

			refRequest.addProperty("refund_amount", payGateCryptoUtils.encrypt(transaction.getAmount(),
					fields.get(FieldType.TXN_KEY.getName()).toString()));
			refRequest.addProperty("refund_reason", payGateCryptoUtils.encrypt("CustomerRequestForRefund",
					fields.get(FieldType.TXN_KEY.getName()).toString()));
			
			logger.info("VePay refund Request after encryption for Order Id = " + fields.get(FieldType.ORDER_ID.getName()) +"  >>  " +refRequest.toString());
			return refRequest.toString();
			
		}

		catch (Exception e) {
			logger.error("Exception in generating Vepay refund request", e);
			return null;
		}

	}

	public Transaction toTransaction(String jsonResponse, Fields fields) {

		Transaction transaction = new Transaction();

		try {

			/*
			 * JSONObject respObj = new JSONObject(jsonResponse);
			 * 
			 * transaction.setAg_ref(respObj.get("ag_ref").toString());
			 * transaction.setPg_ref(respObj.get("pg_ref").toString());
			 * transaction.setRes_code(respObj.get("res_code").toString());
			 * transaction.setRes_message(respObj.get("res_message").toString());
			 * transaction.setStatus(respObj.get("status").toString());
			 * transaction.setRefund_ref(respObj.get("refund_ref").toString());
			 * 
			 * 
			 */
			
			return null;
			
		} catch (Exception e) {
			logger.error("Exception in parsing status enquiry response for Vepay" , e);
		}

		return transaction;

	}

	public TransactionConverter() {

	}

}
