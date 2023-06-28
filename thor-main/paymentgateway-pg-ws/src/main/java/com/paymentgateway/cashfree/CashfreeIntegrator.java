package com.paymentgateway.cashfree;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class CashfreeIntegrator {

	@Autowired
	@Qualifier("cashfreeTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("cashfreeTransactionCommunicator")
	private TransactionCommunicator communicator;

	@Autowired
	private TransactionFactory TransactionFactory;

	private CashfreeTransformer cashfreeTransformer = null;

	public void process(Fields fields) throws SystemException {
		String transactionType = fields.get(FieldType.TXNTYPE.getName());
		String paymentType = fields.get(FieldType.PAYMENT_TYPE.getName());

		if (paymentType.equalsIgnoreCase(PaymentType.UPI.getCode())
				&& transactionType.equals(TransactionType.SALE.getName())) {

			TransactionFactory.getInstance(fields);
			boolean vpaValidationResp = verifyUpiId(fields);
			if (vpaValidationResp) {
				String request = converter.createOrderIdRequest(fields);
				String response = communicator.orderIdResponse(request, fields);
				JSONObject responseJson = new JSONObject(response);
				if (responseJson.has("order_token") && StringUtils.isNotBlank(responseJson.getString("order_token"))) {
					String payRequest = converter.payOrderRequest(responseJson.getString("order_token"),
							fields.get(FieldType.PAYER_ADDRESS.getName()));
					communicator.updateSaleResponse(fields, payRequest);
					communicator.payOrderResponse(payRequest);

				} else {
					communicator.updateFailedResponse(fields);
				}
			} else {
				communicator.updateVpaFailedResponse(fields);
			}
		} else {
			send(fields);
		}

	}// process

	public boolean verifyUpiId(Fields fields) {

		// GET Request with data UPI ID in URL Sample -->
		// https://api.cashfree.com/api/v2/upi/validate/{UPI_ID}
		// HEADER -->> "x-client-id","<CLIENT_ID>" , "x-client-secret","<CLIENT_ID>"
		// RESPONSE -->>
		// {"vpa":"9891233268@ybl","status":"OK","valid":true,"name":"SANDEEP SHARMA"}
		// for Success
		// RESPONSE -->>
		// {"vpa":"1122334455@ybl","status":"OK","valid":false,"name":"NA"} for Failure

		String response = communicator.vpaValidationResponse(fields);

		if (StringUtils.isNotBlank(response)) {
			JSONObject responseJson = new JSONObject(response);
			if (responseJson.has("valid")) {
				return responseJson.getBoolean("valid");
			}
		}

		return false;

	}

	public void send(Fields fields) throws SystemException {

		Transaction transactionRequest = new Transaction();
		Transaction transactionResponse = new Transaction();

		transactionRequest = TransactionFactory.getInstance(fields);

		String request = converter.perpareRequest(fields, transactionRequest);

		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (txnType.equals(TransactionType.SALE.getName())) {
			communicator.updateSaleResponse(fields, request);
		} else {
			String response = communicator.getResponse(request, fields);

			transactionResponse = converter.toTransactionRefund(response);
			cashfreeTransformer = new CashfreeTransformer(transactionResponse);

			if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.REFUND.getName())) {
				cashfreeTransformer.updateRefundResponse(fields);
			}

		}

	}

	public TransactionConverter getConverter() {
		return converter;
	}

	public void setConverter(TransactionConverter converter) {
		this.converter = converter;
	}

	public TransactionCommunicator getCommunicator() {
		return communicator;
	}

	public void setCommunicator(TransactionCommunicator communicator) {
		this.communicator = communicator;
	}

	public CashfreeTransformer getCashfreeTransformer() {
		return cashfreeTransformer;
	}

	public void setCashfreeTransformer(CashfreeTransformer cashfreeTransformer) {
		this.cashfreeTransformer = cashfreeTransformer;
	}

}
