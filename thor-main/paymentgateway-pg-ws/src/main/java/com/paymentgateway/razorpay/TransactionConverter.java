package com.paymentgateway.razorpay;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;

@Service("razorpayTransactionConverter")
final class TransactionConverter {

	@Autowired
	@Qualifier("razorpayTransactionCommunicator")
	private TransactionCommunicator communicator;

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

		default:

			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Unknown TxnType for Razorpay request creation");
		}

		return request;

	}

	public String saleRequest(Fields fields, Transaction transaction) throws SystemException {

		try {

			// Step 1 is to get a new id from Razorpay to start transaction
			JsonObject orderIdReq = new JsonObject();

			orderIdReq.addProperty("amount", transaction.getAmount());
			orderIdReq.addProperty("currency", Constants.INR);

			String saleOrdIdRes = communicator.getSaleOrderId(orderIdReq.toString(), fields);

			JSONObject orderIdRes = new JSONObject(saleOrdIdRes);

			if (orderIdRes != null && orderIdRes.toString().contains("id") && orderIdRes.get("id") != null) {
				transaction.setId(orderIdRes.get("id").toString());
			} else {
				throw new SystemException(ErrorType.BANK_EXCEPTION, "Missing id in Order Id Gen Response for Razorpay");
			}

			fields.put(FieldType.AUTH_CODE.getName(), orderIdRes.get("id").toString());
			
			// Step 2 is to get request HTML for the transaction
			JsonObject saleHtmlReq = new JsonObject();

			saleHtmlReq.addProperty("amount", transaction.getAmount());
			saleHtmlReq.addProperty("currency", Constants.INR);
			saleHtmlReq.addProperty("contact", transaction.getContact());
			saleHtmlReq.addProperty("email", transaction.getEmail());
			saleHtmlReq.addProperty("order_id", transaction.getId());
			saleHtmlReq.addProperty("ip", transaction.getIp());
			saleHtmlReq.addProperty("referer", transaction.getReferrer());
			saleHtmlReq.addProperty("user_agent", transaction.getUser_agent());
			saleHtmlReq.addProperty("method", transaction.getMethod());
			saleHtmlReq.addProperty("callback_url", transaction.getCallback_url());

			if (transaction.getMethod().equalsIgnoreCase(Constants.CARD)) {

				JsonObject cardReq = new JsonObject();
				cardReq.addProperty("number", transaction.getNumber());
				cardReq.addProperty("name", transaction.getName());
				cardReq.addProperty("expiry_month", transaction.getExpiry_month());
				cardReq.addProperty("expiry_year", transaction.getExpiry_year());
				cardReq.addProperty("cvv", transaction.getCvv());

				saleHtmlReq.add("card", cardReq);
			} else if (transaction.getMethod().equalsIgnoreCase(Constants.UPI)) {
				saleHtmlReq.addProperty("vpa", transaction.getVpa());
			} else if (transaction.getMethod().equalsIgnoreCase(Constants.NET_BANKING)) {
				saleHtmlReq.addProperty("bank", transaction.getBank());
			} else if (transaction.getMethod().equalsIgnoreCase(Constants.WALLET)) {
				saleHtmlReq.addProperty("wallet", transaction.getWallet());
			}

			return communicator.getSaleHtml(saleHtmlReq.toString(), fields);

		}

		catch (Exception exception) {
			logger.error("Exception in generating Razorpay sale request ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error in generating Sale Request for Razorpay");
		}

	}

	public String refundRequest(Fields fields, Transaction transaction) throws SystemException {

		try {

			JsonObject refRequest = new JsonObject();
			refRequest.addProperty("amount", fields.get(FieldType.AMOUNT.getName()));
			refRequest.addProperty("speed", "optimum");
			return refRequest.toString();

		}

		catch (Exception e) {

			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e, "Error in preparing Razorpay refund request");

		}

	}

	public Transaction toTransaction(String jsonResponse, Fields fields) throws SystemException {

		Transaction transaction = new Transaction();

		try {

			JSONObject respObj = new JSONObject(jsonResponse);

			// Success Case
			if (respObj.get("id") != null && respObj.get("payment_id") != null && respObj.get("status") != null ) {
				transaction.setOrder_id(respObj.get("id").toString());
				transaction.setPayment_id(respObj.get("payment_id").toString());
				transaction.setStatus(respObj.get("status").toString());
			}
			else if (respObj.get("error") != null) {
				
				JSONObject errorObj = respObj.getJSONObject("error");
				transaction.setCode(errorObj.get("code").toString());
				transaction.setDescription(errorObj.get("description").toString());
			}
			

		} catch (Exception e) {
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"Error in converting Razorpay refund response toTransaction");
		}

		return transaction;

	}

}
