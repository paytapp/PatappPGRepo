package com.paymentgateway.hdfc.upi;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;
import com.paymentgateway.pg.core.util.HdfcUpiUtil;

/**
 * @author Rahul
 *
 */
@Service("hdfcUpiTransactionConverter")
public class TransactionConverter {

	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;
	
	@Autowired
	@Qualifier("hdfcUpiUtil")
	private HdfcUpiUtil hdfcUpiUtil;

	private String decryptedString;

	private static Logger logger = LoggerFactory.getLogger(TransactionConverter.class.getName());

	@SuppressWarnings("incomplete-switch")
	public JSONObject perpareRequest(Fields fields) throws SystemException {

		JSONObject request = null;

		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case REFUND:
			request = payRequest(fields);
			break;
		case SALE:
			request = collectRequest(fields);
			break;
		case ENQUIRY:
			request = statusEnquiryRequest(fields);
			break;
		}
		return request;

	}

	public JSONObject vpaValidatorRequest(Fields fields) {

		String status = PropertiesManager.propertiesMap.get("HdfcUpiStatusValue");
		String lastValue = PropertiesManager.propertiesMap.get("hdfcUpiLastValue");
		// String payerAddress =
		// PropertiesManager.propertiesMap.get("hdfcUpiMerchantVPA");
		String payerAddress = fields.get(FieldType.PAYER_ADDRESS.getName());
		String merchantId = fields.get(FieldType.ADF5.getName());

		StringBuilder request = new StringBuilder();
		request.append(merchantId);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.TXN_ID.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(payerAddress);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(status);
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append(lastValue);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(lastValue);

		String key = fields.get(FieldType.ADF7.getName());
		String encryptedString = null;
		try {
			encryptedString = hdfcUpiUtil.encrypt(request.toString(), key);
		} catch (Exception e) {
			logger.error("Exception : " , e);
		}

		JSONObject json = new JSONObject();
		json.put(Constants.REQUEST_MESSAGE, encryptedString);
		json.put(Constants.PG_MERCHANT_ID, merchantId);

		String payerVpa = fields.get(FieldType.PAYER_ADDRESS.getName());
		String payerName = fields.get(FieldType.PAYER_NAME.getName());
		fields.put(FieldType.UDF3.getName(), payerVpa);
		fields.put(FieldType.UDF4.getName(), payerName);
		return json;

	}

	public JSONObject collectRequest(Fields fields) throws SystemException {

		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		String expiryTime = PropertiesManager.propertiesMap.get("HdfcUpiExpiryTime");
		String mccCode = fields.get(FieldType.ADF6.getName());
		String lastValue = PropertiesManager.propertiesMap.get("hdfcUpiLastValue");
		// String payerAddress =
		// PropertiesManager.propertiesMap.get("hdfcUpiMerchantVPA");
		String payerAddress = fields.get(FieldType.PAYER_ADDRESS.getName());
		String merchantId = fields.get(FieldType.ADF5.getName());

		StringBuilder request = new StringBuilder();
		request.append(merchantId);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.TXN_ID.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(payerAddress);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(amount);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.PRODUCT_DESC.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(expiryTime);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(mccCode);
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append(lastValue);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(lastValue);

		String key = fields.get(FieldType.ADF7.getName());
		String encryptedString = null;
		try {
			encryptedString = hdfcUpiUtil.encrypt(request.toString(), key);
		} catch (Exception e) {
			logger.error("Exception : " , e);
		}

		JSONObject json = new JSONObject();
		json.put(Constants.REQUEST_MESSAGE, encryptedString);
		json.put(Constants.PG_MERCHANT_ID, merchantId);

		fields.put(FieldType.UDF3.getName(), payerAddress);
		String customerVpa = fields.get(FieldType.PAYER_NAME.getName());
		fields.put(FieldType.UDF4.getName(), customerVpa);
		return json;
	}

	public JSONObject payRequest(Fields fields) {

		String refundedAmount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
				fields.get(FieldType.CURRENCY_CODE.getName()));
		String currency = Currency.getAlphabaticCode(fields.get(FieldType.CURRENCY_CODE.getName()));
		//String lastValue = PropertiesManager.propertiesMap.get("hdfcUpiLastValue");
		//String txnType = PropertiesManager.propertiesMap.get("hdfcUpiTransactionType");
		//String paymentType = PropertiesManager.propertiesMap.get("hdfcUpiPaymentType");
		String merchantId = fields.get(FieldType.ADF5.getName());
		StringBuilder request = new StringBuilder();
		request.append(merchantId);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.TXN_ID.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.ORIG_TXN_ID.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.ACQ_ID.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.RRN.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.REFUND_DESCRIPTION_MESSAGE);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(refundedAmount);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(currency);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.HDFC_UPI_TRANS_TYPE);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.HDFC_UPI_PAYMENT_TYPE);
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.LAST_VALUE);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.LAST_VALUE);

		String key = fields.get(FieldType.ADF7.getName());
		String encryptedString = null;
		try {
			encryptedString = hdfcUpiUtil.encrypt(request.toString(), key);
		} catch (Exception e) {
			logger.error("Exception : " , e);
		}

		JSONObject json = new JSONObject();
		json.put(Constants.REQUEST_MESSAGE, encryptedString);
		json.put(Constants.PG_MERCHANT_ID, merchantId);

		/*
		 * String payerVpa = fields.get(FieldType.UDF3.getName()); String
		 * payerName = fields.get(FieldType.UDF4.getName()); String merchantVpa
		 * = fields.get(FieldType.UDF1.getName());
		 */
		return json;
	}

	public JSONObject statusEnquiryRequest(Fields fields) {

		String merchantId = fields.get(FieldType.ADF5.getName());

		StringBuilder request = new StringBuilder();
		request.append(merchantId);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.PG_REF_NUM.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.LAST_VALUE);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.LAST_VALUE);

		String key = fields.get(FieldType.ADF7.getName());
		String encryptedString = null;
		try {
			encryptedString = hdfcUpiUtil.encrypt(request.toString(), key);
		} catch (Exception e) {
			logger.error("Exception : " , e);
		}

		JSONObject json = new JSONObject();
		json.put(Constants.REQUEST_MESSAGE, encryptedString);
		json.put(Constants.PG_MERCHANT_ID, merchantId);
		return json;
	}

	public Transaction toTransaction(String encryptedResponse, Fields fields) {

		Transaction transaction = new Transaction();
		String key = fields.get(FieldType.ADF7.getName());

		try {
			decryptedString = hdfcUpiUtil.decrypt(encryptedResponse, key);
			logger.info("decryptedString : " + decryptedString);
		} catch (Exception e) {
			logger.error("Exception : " , e);
		}

		String[] value_split = decryptedString.split("\\|");
		/*
		 * for (String string : value_split) { }
		 */

		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (txnType.equals(TransactionType.SALE.name())) {
			String vpaStatus = value_split[3];
			transaction.setStatus(vpaStatus);
		} else {
			String refundStatus = value_split[4];
			String refundAcq = value_split[0];
			String refundRRN = value_split[9];
			String refundResponseCode = value_split[6];
			String refundResponseMsg = value_split[5];

			String dateTime = value_split[3];
			String merchantVPA = value_split[8];

			transaction.setResponse(refundResponseCode);
			transaction.setAcq_id(refundAcq);
			transaction.setRrn(refundRRN);
			transaction.setStatus(refundStatus);
			transaction.setResponseMessage(refundResponseMsg);
			transaction.setDateTime(dateTime);
			transaction.setMerchantVpa(merchantVPA);

		}

		return transaction;
	}

	public Transaction toTransactionStatusEnquiry(String encryptedResponse, Fields fields) {

		Transaction transaction = new Transaction();
		String key = fields.get(FieldType.ADF7.getName());

		try {
			decryptedString = hdfcUpiUtil.decrypt(encryptedResponse, key);
			logger.info("decryptedString : " + decryptedString);
		} catch (Exception e) {
			logger.error("Exception : " , e);
		}

		String[] value_split = decryptedString.split("\\|");
		logger.info("HDFC UPI StatusEnquiry RESPONSE  " + decryptedString);

		String receivedResponseCode = value_split[6];

		String status = getStatusType(receivedResponseCode);
		ErrorType errorMsg = getErrorType(receivedResponseCode);

		String responseCode = value_split[6];
		String responseMsg = value_split[4];
		String txnMsg = value_split[5];
		String payeeApprovalNum = value_split[0];
		String ReferenceId = value_split[10];
		String dateTime = value_split[3];
		String customerReference = value_split[9];
		String txnId = value_split[1];
		String address = value_split[18];
		String[] merchantDetails = address.split("\\!");
		String merchantVPA = merchantDetails[0];

		logger.info("Merchant Status enquiry" + merchantVPA);

		transaction.setResponse(responseCode);
		transaction.setResponseMessage(responseMsg);
		transaction.setTransactionMessage(txnMsg);
		transaction.setPayeeApprovalNum(payeeApprovalNum);
		transaction.setReferenceId(ReferenceId);
		transaction.setDateTime(dateTime);
		transaction.setCustomerReference(customerReference);
		transaction.setTransactionId(txnId);
		transaction.setAddress(address);
		transaction.setMerchantVPA(merchantVPA);
		return transaction;

	}

	public String getStatusType(String receivedResponse) {
		String status = null;
		if (receivedResponse.equals(Constants.SUCCESS_REPONSE_CODE)) {
			status = StatusType.CAPTURED.getName();
		} else if (receivedResponse.equals("U19")) {
			status = StatusType.FAILED.getName();
		} else {
			status = StatusType.REJECTED.getName();
		}

		return status;
	}

	public ErrorType getErrorType(String receivedResponse) {
		ErrorType error = null;

		if (receivedResponse.equals(Constants.SUCCESS_REPONSE_CODE)) {
			error = ErrorType.SUCCESS;
		} else if (receivedResponse.equals("U19")) {
			error = ErrorType.INVALID_REQUEST_FIELD;
		} else if (receivedResponse.equals("002")) {
			error = ErrorType.CANCELLED;
		} else if (receivedResponse.equals("003")) {
			error = ErrorType.INTERNAL_SYSTEM_ERROR;
		} else if (receivedResponse.equals("004")) {
			error = ErrorType.CANCELLED;
		} else {
			error = ErrorType.DECLINED;
		}

		return error;
	}

}