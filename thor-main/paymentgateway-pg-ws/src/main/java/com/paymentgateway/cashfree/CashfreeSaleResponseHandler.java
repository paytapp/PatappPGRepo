package com.paymentgateway.cashfree;

import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.Validator;
import com.paymentgateway.pg.core.util.CashfreeChecksumUtil;
import com.paymentgateway.pg.core.util.ProcessManager;
import com.paymentgateway.pg.core.util.Processor;

@Service
public class CashfreeSaleResponseHandler {

	private static Logger logger = LoggerFactory.getLogger(CashfreeSaleResponseHandler.class.getName());

	@Autowired
	private Validator generalValidator;

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	@Qualifier("cashfreeTransactionConverter")
	private TransactionConverter transactionConverter;

	@Autowired
	private CashfreeTransformer cashfreeTransformer;

	@Autowired
	private CashfreeChecksumUtil cashFreeChecksumUtil;

	@Autowired
	@Qualifier("cashfreeTransactionCommunicator")
	private TransactionCommunicator transactionCommunicator;

	public Map<String, String> process(Fields fields) throws SystemException {

		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getCode());
		Transaction transactionResponse = new Transaction();
		String response = fields.get(FieldType.CASHFREE_RESPONSE_FIELD.getName());

		/*
		 * boolean res = isHashMatching(response, fields); boolean doubleVer =
		 * doubleVerification(response, fields);
		 */

		fields.remove(FieldType.CASHFREE_RESPONSE_FIELD.getName());
		generalValidator.validate(fields);

		transactionResponse = toTransaction(response, transactionResponse);
		cashfreeTransformer = new CashfreeTransformer(transactionResponse);
		cashfreeTransformer.updateResponse(fields);
		
		/*
		 * if (res == true && doubleVer == true) { transactionResponse =
		 * toTransaction(response, transactionResponse); cashfreeTransformer = new
		 * CashfreeTransformer(transactionResponse);
		 * cashfreeTransformer.updateResponse(fields); } else {
		 * fields.put(FieldType.STATUS.getName(), StatusType.DENIED.getName());
		 * fields.put(FieldType.RESPONSE_CODE.getName(),
		 * ErrorType.SIGNATURE_MISMATCH.getCode());
		 * fields.put(FieldType.RESPONSE_MESSAGE.getName(),
		 * ErrorType.SIGNATURE_MISMATCH.getResponseMessage()); }
		 */
		fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
		ProcessManager.flow(updateProcessor, fields, true);
		return fields.getFields();

	}

	public Transaction toTransaction(String response, Transaction transactionResponse) {
		Transaction transaction = new Transaction();
		transaction = transactionConverter.toTransaction(response);
		return transaction;
	}

	/*
	 * private boolean isHashMatching(String transactionResponse, Fields fields)
	 * throws SystemException {
	 * 
	 * JSONObject resJson = new JSONObject(transactionResponse.toString());
	 * 
	 * String responseSignature = (String) resJson.get(Constants.signature);
	 * 
	 * resJson.remove(Constants.signature); String resSignatureCalculated =
	 * cashFreeChecksumUtil.checkSaleResponseHash(resJson,
	 * fields.get(FieldType.TXN_KEY.getName()));
	 * 
	 * logger.info("Order Id " + fields.get(FieldType.ORDER_ID.getName()) +
	 * "  bank response signature == " + responseSignature); logger.info("Order Id "
	 * + fields.get(FieldType.ORDER_ID.getName()) + "  calculated signature == " +
	 * resSignatureCalculated);
	 * 
	 * if (responseSignature.contentEquals(resSignatureCalculated)) { return true; }
	 * else {
	 * logger.info("Signature from Bank did not match with generated Signature");
	 * logger.info("Bank Hash = " + responseSignature);
	 * logger.info("Calculated Hash = " + resSignatureCalculated); return false; }
	 * 
	 * }
	 * 
	 * private boolean doubleVerification(String transactionResponse, Fields fields)
	 * throws SystemException {
	 * 
	 * try {
	 * 
	 * JSONObject resJson = new JSONObject(transactionResponse);
	 * 
	 * // Skip if txStatus is not present in response if
	 * (!transactionResponse.contains(Constants.txStatus)){ return true; }
	 * 
	 * // Skip for unsuccessful transactions if if
	 * (transactionResponse.contains(Constants.txStatus) &&
	 * !resJson.get(Constants.txStatus).toString().equalsIgnoreCase("SUCCESS")) {
	 * return true; }
	 * 
	 * String request = transactionConverter.statusEnquiryRequest(fields, null);
	 * String hostUrl =
	 * PropertiesManager.propertiesMap.get(Constants.STATUS_ENQ_REQUEST_URL); String
	 * response = transactionCommunicator.statusEnqPostRequest(request, hostUrl);
	 * 
	 * logger.info("Bank Response = " + transactionResponse);
	 * logger.info("Double Verification Response = " + response);
	 * 
	 * 
	 * JSONObject resJsonBank = new JSONObject(transactionResponse);
	 * 
	 * if
	 * ((resJson.get(Constants.txStatus).toString().equals(resJsonBank.get(Constants
	 * .txStatus).toString())) &&
	 * (resJson.get(Constants.orderAmount).toString().equals(resJsonBank.get(
	 * Constants.orderAmount).toString()))) { return true; } else {
	 * logger.info("Double Verification Response donot match for Cashfree"); return
	 * false; }
	 * 
	 * }
	 * 
	 * catch (Exception e) { logger.error("Exceptionn ", e); return false; }
	 * 
	 * }
	 */
}
