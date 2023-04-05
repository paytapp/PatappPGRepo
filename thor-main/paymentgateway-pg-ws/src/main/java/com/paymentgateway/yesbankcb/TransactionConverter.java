package com.paymentgateway.yesbankcb;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StaticDataProvider;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;
import com.paymentgateway.pg.core.util.YesBankUpiUtil;

@Service("yesBankCbTransactionConverter")
public class TransactionConverter {
	private static Logger logger = LoggerFactory.getLogger(TransactionConverter.class.getName());

	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;
	
	@Autowired
	private StaticDataProvider staticDataProvider;

	@Autowired
	private YesBankUpiUtil yesBankUpiUtil;

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

	public JSONObject vpaValidatorRequest(Fields fields) throws SystemException {

		StringBuilder request = new StringBuilder();
		request.append(fields.get(FieldType.ADF5.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.PG_REF_NUM.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.PAYER_ADDRESS.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.REQ_TYPE_VALUE);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.APP_VALUE);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.GEOCODE_VALUE);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.LOCATION_VALUE);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.IP_VALUE);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.TYPE_VALUE);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.CAPABILITY_VALUE);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.OS_VALUE);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.DEVICE_ID_VALUE);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.SIM_ID);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.SYSTEM_UNIQUE_ID);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.BLUETOOTH_MAC);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.WIFI_MAC);
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

		logger.info("Yes Bank UPI VPA Validation request " + request);

		String encryptedString = encrypt(request.toString(), fields);

		JSONObject json = new JSONObject();
		json.put(Constants.REQUEST_MESSAGE, encryptedString);
		json.put(Constants.PG_MERCHANT_ID, fields.get(FieldType.ADF5.getName()));

		fields.put(FieldType.UDF3.getName(), fields.get(FieldType.PAYER_ADDRESS.getName()));
		String payerName = fields.get(FieldType.PAYER_NAME.getName());
		fields.put(FieldType.UDF4.getName(), payerName);
		return json;

	}

	public JSONObject collectRequest(Fields fields) throws SystemException {

		String payerAddress = fields.get(FieldType.PAYER_ADDRESS.getName());
		String amount = acquirerTxnAmountProvider.amountProvider(fields);

		String expiryTime = PropertiesManager.propertiesMap.get(Constants.YES_BANK_UPI_COLLECT_EXPIRY_TIME);
		if(StringUtils.isBlank(expiryTime)) {
			expiryTime = Constants.EXPIRY_VALUE;
		}

		StringBuilder request = new StringBuilder();
		request.append(fields.get(FieldType.ADF5.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.PG_REF_NUM.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(payerAddress);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(amount);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.PRODUCT_DESC);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.EXPIRY_TYPE);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(expiryTime);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.ADF6.getName()));
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

		logger.info("Yes Bank UPI collect request =  " + request.toString() + " for Order Id "
				+ fields.get(FieldType.ORDER_ID.getName()));

		String encryptedString = encrypt(request.toString(), fields);

		logger.info("Yes Bank Encrypted UPI collect request =  " + encryptedString + " for Order Id "
				+ fields.get(FieldType.ORDER_ID.getName()));

		JSONObject json = new JSONObject();
		json.put(Constants.REQUEST_MESSAGE, encryptedString);
		json.put(Constants.PG_MERCHANT_ID, fields.get(FieldType.ADF5.getName()));

		fields.put(FieldType.UDF3.getName(), payerAddress);
		String payerName = fields.get(FieldType.PAYER_NAME.getName());
		fields.put(FieldType.UDF4.getName(), payerName);
		return json;

	}

	public JSONObject payRequest(Fields fields) throws SystemException {

		String refundedAmount = acquirerTxnAmountProvider.amountProvider(fields);

		String currency = Currency.getAlphabaticCode(fields.get(FieldType.CURRENCY_CODE.getName()));
		String merchantId = fields.get(FieldType.ADF5.getName());

		StringBuilder request = new StringBuilder();
		request.append(merchantId);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.PG_REF_NUM.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.ORIG_TXN_ID.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.ACQ_ID.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.REFUND_FLAG.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(refundedAmount);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(currency);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.PAYMENT_TYPE);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.YES_BANK_UPI_TXN_TYPE);
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

		logger.info("Yes Bank UPI refund request =  " + request.toString() + " for Order Id "
				+ fields.get(FieldType.ORDER_ID.getName()));

		String encryptedString = encrypt(request.toString(), fields);

		JSONObject json = new JSONObject();
		json.put(Constants.REQUEST_MESSAGE, encryptedString);
		json.put(Constants.PG_MERCHANT_ID, merchantId);
		
		logger.info("Yes Bank UPI refund request encrypted =  " + request.toString() + " for Order Id "
				+ fields.get(FieldType.ORDER_ID.getName()));
		return json;
	}

	public JSONObject statusEnquiryRequest(Fields fields) throws SystemException {

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
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.LAST_VALUE);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.LAST_VALUE);

		logger.info("Yes Bank UPI status request =  " + request.toString() + " for Order Id "
				+ fields.get(FieldType.ORDER_ID.getName()));
		String encryptedString = encrypt(request.toString(), fields);

		JSONObject json = new JSONObject();
		json.put(Constants.REQUEST_MESSAGE, encryptedString);
		json.put(Constants.PG_MERCHANT_ID, merchantId);
		return json;
	}

	public Transaction toTransactionStatusEnquiry(String encryptedResponse, Fields fields) throws SystemException {

		Transaction transaction = new Transaction();
		String decryptedString = decrypt(encryptedResponse, fields);

		logger.info("Yes Bank UPI Status decrypted response for Order Id " + fields.get(FieldType.ORDER_ID.getName())
		+ " reponse = " + decryptedString);
		
		
		String[] value_split = decryptedString.split("\\|");

		String status = value_split[4];
		String responseCode = value_split[6];
		String responseMsg = value_split[5];
		String dateTime = value_split[3];
		String customerReference = value_split[9];
		String rrn = value_split[11];
//		String acqId = value_split[0];
		String authCode = value_split[13];
		transaction.setResponse(responseCode);
		transaction.setResponseMessage(responseMsg);
		transaction.setStatus(status);
		transaction.setDateTime(dateTime);
		transaction.setArn(customerReference);
		transaction.setRrn(rrn);
		transaction.setAcq_id(rrn);
		transaction.setAuth_code(authCode);
		
		return transaction;

	}

	public String encrypt(String encryptedRequest, Fields fields) throws SystemException {

		String key = fields.get(FieldType.ADF7.getName());
		if (StringUtils.isBlank(key)) {
			key = PropertiesManager.propertiesMap.get(Constants.YES_BANKCB_UPI_KEY);
		}

		String encryptedString = "";
		try {
			encryptedString = yesBankUpiUtil.encrypt(encryptedRequest.toString(), key);
		} catch (Exception e) {
			logger.error("Exception in encrypting Yes Bank UPI Request: ", e);
		}
		return encryptedString;

	}

	public String toVpaTransaction(String encryptedResponse, Fields fields) throws SystemException {
		String vpaValidationStatus = "";
		String decryptedString = "";
		try {
			if (!encryptedResponse.contains(Constants.YES_RES_CONTAINS_NA)) {
				decryptedString = decrypt(encryptedResponse, fields);
				logger.info("Yes Bank VPA Validation Response in Plain Text " + decryptedString);
			} else {
				decryptedString = encryptedResponse;
				logger.info("Yes Bank VPA Validation Response in Plain Text " + decryptedString);
			}
			String[] value_split = decryptedString.split("\\|");
			vpaValidationStatus = value_split[3];
		} catch (Exception e) {
			logger.error("Exception in VPA Response Parsing", e);
		}

		return vpaValidationStatus;

	}

	public Transaction toTransaction(String encryptedResponse, Fields fields) throws SystemException {

		Transaction transaction = new Transaction();
		String decryptedString = "";
		try {
			if (!encryptedResponse.contains(Constants.YES_RES_CONTAINS_NA)) {
				decryptedString = decrypt(encryptedResponse, fields);
				logger.info("Yes Bank Collect API Response decrypted  String  " + decryptedString + " for Order Id "
						+ fields.get(FieldType.ORDER_ID.getName()));
			} else {
				decryptedString = encryptedResponse;
				logger.info("Yes Bank Collect API Response String  " + decryptedString + " for Order Id "
						+ fields.get(FieldType.ORDER_ID.getName()));
			}

			String txnType = fields.get(FieldType.TXNTYPE.getName());
			String[] value_split = decryptedString.split("\\|");

			if (txnType.equalsIgnoreCase(TransactionType.SALE.name())) {
				String collectStatus = value_split[3];
				String collectMsg = value_split[4];
				transaction.setStatus(collectStatus);
				transaction.setResponseMessage(collectMsg);
			} else if (txnType.equalsIgnoreCase(TransactionType.REFUND.name())) {
				String refundStatus = value_split[4];
				String refundAcq = value_split[10];
				String refundRRN = value_split[10];
				String refundResponseCode = value_split[6];
				String refundResponseMsg = value_split[5];

				String dateTime = value_split[3];
				String merchantVPA = value_split[8];

				if (StringUtils.isNotBlank(refundStatus)
						&& refundStatus.equalsIgnoreCase(Constants.YES_UPI_REFUND_PENDING)) {
					transaction.setResponse(refundStatus);
				} else {
					transaction.setResponse(refundResponseCode);
				}
				transaction.setAcq_id(refundAcq);
				transaction.setRrn(refundRRN);
				transaction.setStatus(refundStatus);
				transaction.setResponseMessage(refundResponseMsg);
				transaction.setDateTime(dateTime);
				transaction.setMerchantVpa(merchantVPA);

			}
		} catch (Exception e) {
			logger.error("Exception in converting Yes Bank Collect response ", e);
		}
		return transaction;

	}

	public Transaction toTransactionRefundFail(String encryptedResponse, Fields fields) throws SystemException {

		Transaction transaction = new Transaction();
		try {
			String[] value_split = encryptedResponse.split("\\|");
			String refundStatus = value_split[4];
			String refundAcq = value_split[10];
			String refundRRN = value_split[10];
			String refundResponseCode = value_split[6];
			String refundResponseMsg = value_split[5];

			String dateTime = value_split[3];
			String merchantVPA = value_split[8];

			if (StringUtils.isNotBlank(refundStatus)
					&& refundStatus.equalsIgnoreCase(Constants.YES_UPI_REFUND_PENDING)) {
				transaction.setResponse(refundStatus);
			} else {
				transaction.setResponse(refundResponseCode);
			}
			transaction.setAcq_id(refundAcq);
			transaction.setRrn(refundRRN);
			transaction.setStatus(refundStatus);
			transaction.setResponseMessage(refundResponseMsg);
			transaction.setDateTime(dateTime);
			transaction.setMerchantVpa(merchantVPA);

		} catch (Exception e) {
			logger.error("Exception", e);
		}
		return transaction;

	}

	public Transaction toTransactionFailureRes(String encryptedResponse, Fields fields) throws SystemException {
		Transaction transaction = new Transaction();
		try {
			String[] value_split = encryptedResponse.split("\\|");

			String failureCode = value_split[4];
			String failureMsg = value_split[5];
			String dateTime = value_split[3];
			transaction.setResponse(failureCode);
			transaction.setStatus(failureMsg);
			transaction.setDateTime(dateTime);
		} catch (Exception e) {
			logger.error("Exception", e);
		}
		return transaction;

	}

	public Transaction toTransactionCollectFailureRes(String encryptedResponse, Fields fields) throws SystemException {
		Transaction transaction = new Transaction();
		try {
			String[] value_split = encryptedResponse.split("\\|");

			String failureRes = value_split[3];
			String failureMsg = value_split[4];

			transaction.setResponse(failureRes);
			transaction.setStatus(failureMsg);

		} catch (Exception e) {
			logger.error("Exception  ", e);
		}

		return transaction;

	}

	public String decrypt(String encryptedRequest, Fields fields) throws SystemException {
		String decryptedString = "";
		String key = fields.get(FieldType.ADF7.getName());

		if (StringUtils.isBlank(key)) {
			logger.info("Key not found in ADF 7");
			key = PropertiesManager.propertiesMap.get(Constants.YES_BANKCB_UPI_KEY);
		}
		try {
			decryptedString = yesBankUpiUtil.decrypt(encryptedRequest, key);
		} catch (Exception e) {

			logger.error("Exception", e);
		}
		return decryptedString;

	}

}
