package com.paymentgateway.airPay;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;
import com.paymentgateway.pg.core.util.AirPayUtil;

/**
 * @author Sandeep
 *
 */
@Service("airPayTransactionConverter")
public class TransactionConverter {

	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;

	@Autowired
	private AirPayUtil airPayUtil;

	private static final Logger logger = LoggerFactory.getLogger(TransactionConverter.class.getName());

	public static final String RESPONSE_OPEN_TAG = "<RESPONSE>";
	public static final String RESPONSE_CLOSE_TAG = "</RESPONSE>";
	public static final String TRANSACTION_OPEN_TAG = "<TRANSACTION>";
	public static final String TRANSACTION_CLOSE_TAG = "</TRANSACTION>";
	public static final String TRANSACTIONSTATUS_OPEN_TAG = "<TRANSACTIONSTATUS>";
	public static final String TRANSACTIONSTATUS_CLOSE_TAG = "</TRANSACTIONSTATUS>";
	public static final String MESSAGE_OPEN_TAG = "<MESSAGE>";
	public static final String MESSAGE_CLOSE_TAG = "</MESSAGE>";
	public static final String APTRANSACTIONID_OPEN_TAG = "<APTRANSACTIONID>";
	public static final String APTRANSACTIONID_CLOSE_TAG = "</APTRANSACTIONID>";
	public static final String TRANSACTIONID_OPEN_TAG = "<TRANSACTIONID>";
	public static final String TRANSACTIONID_CLOSE_TAG = "</TRANSACTIONID>";
	public static final String AMOUNT_OPEN_TAG = "<AMOUNT>";
	public static final String AMOUNT_CLOSE_TAG = "</AMOUNT>";
	public static final String ap_SecureHash_OPEN_TAG = "<ap_SecureHash>";
	public static final String ap_SecureHash_CLOSE_TAG = "</ap_SecureHash>";
	public static final String CUSTOMVAR_OPEN_TAG = "<CUSTOMVAR>";
	public static final String CUSTOMVAR_CLOSE_TAG = "</CUSTOMVAR>";
	public static final String CARDCOUNTRY_OPEN_TAG = "<CARDCOUNTRY>";
	public static final String CARDCOUNTRY_CLOSE_TAG = "</CARDCOUNTRY>";
	public static final String CUSTOMER_OPEN_TAG = "<CUSTOMER>";
	public static final String CUSTOMER_CLOSE_TAG = "</CUSTOMER>";
	public static final String CUSTOMEREMAIL_OPEN_TAG = "<CUSTOMEREMAIL>";
	public static final String CUSTOMEREMAIL_CLOSE_TAG = "</CUSTOMEREMAIL>";
	public static final String CUSTOMERPHONE_OPEN_TAG = "<CUSTOMERPHONE>";
	public static final String CUSTOMERPHONE_CLOSE_TAG = "</CUSTOMERPHONE>";
	public static final String CURRENCYCODE_OPEN_TAG = "<CURRENCYCODE>";
	public static final String CURRENCYCODE_CLOSE_TAG = "</CURRENCYCODE>";
	public static final String TRANSACTIONPAYMENTSTATUS_OPEN_TAG = "<TRANSACTIONPAYMENTSTATUS>";
	public static final String TRANSACTIONPAYMENTSTATUS_CLOSE_TAG = "</TRANSACTIONPAYMENTSTATUS>";
	public static final String CUSTOMERVPA_OPEN_TAG = "<CUSTOMERVPA>";
	public static final String CUSTOMERVPA_CLOSE_TAG = "</CUSTOMERVPA>";

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
		case CAPTURE:
			break;
		case STATUS:
			request = statusEnquiryRequest(fields, transaction);
			break;
		}
		return request;

	}

	public String saleRequest(Fields fields, Transaction transaction) throws SystemException {
		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		JSONObject jsonRequest = new JSONObject();
		prepareSaleRequest(jsonRequest, fields, amount);

		String privateKey = airPayUtil.generatingPvtKey(fields);
		jsonRequest.put(Constants.PRIVATEKEY, privateKey);
		String signature = airPayUtil.generatingCheckSum(jsonRequest, fields);
		jsonRequest.put(Constants.CHECKSUM, signature);
		logger.info("AirPay Request for UPI " + jsonRequest.toString());
		return jsonRequest.toString();
	}

	public String refundRequest(Fields fields, Transaction transaction) throws SystemException {
		JSONObject json = new JSONObject();
		JSONArray requestArray = new JSONArray();
		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		json.put(Constants.MERCHANT_ID, fields.get(FieldType.MERCHANT_ID.getName()));
		String privateKey = airPayUtil.generatingPvtKey(fields);
		json.put(Constants.PRIVATEKEY, privateKey);
		json.put(Constants.MODE, "refund");
		JSONArray jsonArray = new JSONArray();
		JSONObject internalJson = new JSONObject();
		internalJson.put(Constants.AIRPAY_TXN_ID, fields.get(FieldType.ACQ_ID.getName()));
		internalJson.put(Constants.AMOUNT, amount);
		jsonArray.put(internalJson);
		json.put(Constants.TRANSACTION, jsonArray);
		String signature = airPayUtil.generatingRefundCheckSum(json, fields);
		json.put(Constants.CHECKSUM, signature);
		requestArray.put(json);

		logger.info("Refund Request To AIRPAY >> " + requestArray.toString());
		return requestArray.toString();
	}

	public String statusEnquiryRequest(Fields fields, Transaction transaction) {
		String privateKey = airPayUtil.generatingPvtKey(fields);
		String checkSum = airPayUtil.generatingStatusCheckSum(fields);
		StringBuilder sb = new StringBuilder();
		sb.append(Constants.MERCHANT_ID);
		sb.append("=");
		sb.append(fields.get(FieldType.MERCHANT_ID.getName()));
		sb.append("&");
		sb.append(Constants.MERCHANT_TXN_ID);
		sb.append("=");
		sb.append(fields.get(FieldType.PG_REF_NUM.getName()));
		sb.append("&");
		sb.append(Constants.PRIVATEKEY);
		sb.append("=");
		sb.append(privateKey);
		sb.append("&");
		sb.append(Constants.CHECKSUM);
		sb.append("=");
		sb.append(checkSum);

		logger.info("status Enquiry Request TO AIRPAY >> " + sb.toString());
		return sb.toString();
	}

	public Transaction toTransaction(String response) {

		Transaction transaction = new Transaction();
		try {

			JSONObject resJson = new JSONObject(response);

			if (response.contains(Constants.TRANSACTIONSTATUS)) {
				transaction.setTRANSACTIONSTATUS(resJson.get(Constants.TRANSACTIONSTATUS).toString());
			}
			if (response.contains(Constants.RESP_AMOUNT)) {
				transaction.setAMOUNT(resJson.get(Constants.RESP_AMOUNT).toString());
			}
			if (response.contains(Constants.TRANSACTIONTIME)) {
				transaction.setTRANSACTIONTIME(resJson.get(Constants.TRANSACTIONTIME).toString());
			}
			if (response.contains(Constants.TRANSACTIONPAYMENTSTATUS)) {
				transaction.setTRANSACTIONPAYMENTSTATUS(resJson.get(Constants.TRANSACTIONPAYMENTSTATUS).toString());
			}
			if (response.contains(Constants.ap_SecureHash)) {
				transaction.setAp_SecureHash(resJson.get(Constants.ap_SecureHash).toString());
			}
			if (response.contains(Constants.BANKRESPONSEMSG)) {
				transaction.setBANKRESPONSEMSG(resJson.get(Constants.BANKRESPONSEMSG).toString());
			}
			if (response.contains(Constants.TRANSACTIONID)) {
				transaction.setTRANSACTIONID(resJson.get(Constants.TRANSACTIONID).toString());
			}
			if (response.contains(Constants.CHMOD)) {
				transaction.setCHMOD(resJson.get(Constants.CHMOD).toString());
			}
			if (response.contains(Constants.CURRENCYCODE)) {
				transaction.setCURRENCYCODE(resJson.get(Constants.CURRENCYCODE).toString());
			}
			if (response.contains(Constants.TRANSACTIONPAYMENTSTATUS)) {
				transaction.setTRANSACTIONPAYMENTSTATUS(resJson.get(Constants.TRANSACTIONPAYMENTSTATUS).toString());
			}
			if (response.contains(Constants.APTRANSACTIONID)) {
				transaction.setAPTRANSACTIONID(resJson.get(Constants.APTRANSACTIONID).toString());
			}
			if (response.contains(Constants.CUSTOMVAR)) {
				transaction.setCUSTOMVAR(resJson.get(Constants.CUSTOMVAR).toString());
			}
			if (response.contains(Constants.TXN_RETRY)) {
				transaction.setTXN_RETRY(resJson.get(Constants.TXN_RETRY).toString());
			}
			if (response.contains(Constants.RESP_MESSAGE)) {
				transaction.setMESSAGE(resJson.get(Constants.RESP_MESSAGE).toString());
			}
			if (response.contains(Constants.CUSTOMERPHONE)) {
				transaction.setCUSTOMERPHONE(resJson.get(Constants.CUSTOMERPHONE).toString());
			}
			if (response.contains(Constants.CUSTOMEREMAIL)) {
				transaction.setCUSTOMEREMAIL(resJson.get(Constants.CUSTOMEREMAIL).toString());
			}
			if (response.contains(Constants.TRANSACTIONTYPE)) {
				transaction.setTRANSACTIONTYPE(resJson.get(Constants.TRANSACTIONTYPE).toString());
			}
			if (response.contains(Constants.BILLEDAMOUNT)) {
				transaction.setBILLEDAMOUNT(resJson.get(Constants.BILLEDAMOUNT).toString());
			}
			if (response.contains(Constants.SURCHARGE)) {
				transaction.setSURCHARGE(resJson.get(Constants.SURCHARGE).toString());
			}
			if (response.contains(Constants.CUSTOMER)) {
				transaction.setCUSTOMER(resJson.get(Constants.CUSTOMER).toString());
			}
			if (response.contains(Constants.CUSTOMERVPA)) {
				transaction.setCUSTOMERVPA(resJson.get(Constants.CUSTOMERVPA).toString());
			}
		}

		catch (Exception e) {
			logger.error("Exception", e);
		}

		return transaction;
	}// toTransaction()

	private void prepareSaleRequest(JSONObject jsonRequest, Fields fields, String amount) {

		jsonRequest.put(Constants.MERCHANT_ID, fields.get(FieldType.MERCHANT_ID.getName()));
		jsonRequest.put(Constants.ORDER_ID, fields.get(FieldType.PG_REF_NUM.getName()));
		jsonRequest.put(Constants.AMOUNT, amount);
		jsonRequest.put(Constants.CURRENCY_ISO,
				Currency.getAlphabaticCode(fields.get(FieldType.CURRENCY_CODE.getName())));
		jsonRequest.put(Constants.CURRENCY, fields.get(FieldType.CURRENCY_CODE.getName()));
		// jsonRequest.put(Constants.UID,
		// "0c167df73976d43a8f965e8e68c530a4236d12cd72dcd69c90a255495d3d254c");
		jsonRequest.put("chmod", "");
		jsonRequest.put("arpyVer", "3");
		jsonRequest.put("payvia", "directindex");
		jsonRequest.put(Constants.CHANNEL, "upi");
		jsonRequest.put(Constants.CHANNEL_MODE, "upi");

		jsonRequest.put(Constants.SUBMODE, "vpa");
		jsonRequest.put(Constants.ACTION, "upi_vpa");
		jsonRequest.put(Constants.APINAME, "collectVPA");

		jsonRequest.put(Constants.VPA, fields.get(FieldType.PAYER_ADDRESS.getName()));

		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
			jsonRequest.put(Constants.BUYER_EMAIL, fields.get(FieldType.CUST_EMAIL.getName()));
		} else {
			jsonRequest.put(Constants.BUYER_EMAIL, PropertiesManager.propertiesMap.get(Constants.AIRPAY_EMAIL_ID));
		}

		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_PHONE.getName()))) {
			jsonRequest.put(Constants.BUYER_PHONE, fields.get(FieldType.CUST_PHONE.getName()));
		} else {
			jsonRequest.put(Constants.BUYER_PHONE, PropertiesManager.propertiesMap.get(Constants.AIRPAY_MOBILE));
		}
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_NAME.getName()))) {
			String[] arrCustName = fields.get(FieldType.CUST_NAME.getName()).split(" ", 2);
			if (arrCustName.length <= 1) {
				jsonRequest.put(Constants.BUYER_FIRST_NAME, arrCustName[0]);
				jsonRequest.put(Constants.BUYER_LAST_NAME, arrCustName[0]);
			} else {
				jsonRequest.put(Constants.BUYER_FIRST_NAME, arrCustName[0]);
				jsonRequest.put(Constants.BUYER_LAST_NAME, arrCustName[1]);
			}
		} else if (StringUtils.isNotBlank(fields.get(FieldType.PAYER_NAME.getName()))) {
			String[] arrCustName = fields.get(FieldType.PAYER_NAME.getName()).split(" ", 2);
			if (arrCustName.length <= 1) {
				jsonRequest.put(Constants.BUYER_FIRST_NAME, arrCustName[0]);
				jsonRequest.put(Constants.BUYER_LAST_NAME, arrCustName[0]);
			} else {
				jsonRequest.put(Constants.BUYER_FIRST_NAME, arrCustName[0]);
				jsonRequest.put(Constants.BUYER_LAST_NAME, arrCustName[1]);
			}
		} else {
			jsonRequest.put(Constants.BUYER_FIRST_NAME, PropertiesManager.propertiesMap.get(Constants.AIRPAY_FIRSTNAME));
			jsonRequest.put(Constants.BUYER_LAST_NAME, PropertiesManager.propertiesMap.get(Constants.AIRPAY_LASTNAME));
		}
		if (StringUtils.isNotBlank(fields.get(FieldType.ADDRESS.getName()))) {
			jsonRequest.put(Constants.BUYER_ADDRESS, fields.get(FieldType.ADDRESS.getName()));
		} else {
			jsonRequest.put(Constants.BUYER_ADDRESS, "");
		}
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_CITY.getName()))) {
			jsonRequest.put(Constants.BUYER_CITY, fields.get(FieldType.CUST_CITY.getName()));
		} else {
			jsonRequest.put(Constants.BUYER_CITY, "");
		}
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_STATE.getName()))) {
			jsonRequest.put(Constants.BUYER_STATE, fields.get(FieldType.CUST_STATE.getName()));
		} else {
			jsonRequest.put(Constants.BUYER_STATE, "");
		}
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_COUNTRY.getName()))) {
			jsonRequest.put(Constants.BUYER_COUNTRY, fields.get(FieldType.CUST_COUNTRY.getName()));
		} else {
			jsonRequest.put(Constants.BUYER_COUNTRY, "");
		}

		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_ZIP.getName()))) {
			jsonRequest.put(Constants.BUYER_PIN_CODE, fields.get(FieldType.CUST_ZIP.getName()));
		} else {
			jsonRequest.put(Constants.BUYER_PIN_CODE, "");
		}
		jsonRequest.put("fullname", jsonRequest.get(Constants.BUYER_FIRST_NAME).toString() + " "
				+ jsonRequest.get(Constants.BUYER_LAST_NAME).toString());
		jsonRequest.put(Constants.CUST_FIELD, "");
		jsonRequest.put(Constants.SUB_TYPE, "");

	}

	public Transaction toTransactionRefund(String response) {

		Transaction transaction = new Transaction();
		try {
			JSONArray responseArray = new JSONArray(response);
			JSONObject resJson = new JSONObject();

			for (int i = 0; i < responseArray.length(); i++) {
				resJson = responseArray.getJSONObject(i);
			}

			if (response.contains(Constants.MERCHANT_ID)) {
				transaction.setMercId(resJson.get(Constants.MERCHANT_ID).toString());
			}

			if (response.contains(Constants.MODE)) {
				transaction.setMode(resJson.get(Constants.MODE).toString());
			}

			if (response.contains(Constants.SUCCESS)) {
				transaction.setSuccess(resJson.get(Constants.SUCCESS).toString());
			}
			if (response.contains(Constants.MESSAGE)) {
				transaction.setMessage(resJson.get(Constants.MESSAGE).toString());
			}

			if (resJson.has(Constants.TRANSACTION)) {
				String array = resJson.get(Constants.TRANSACTION).toString();

				JSONArray transactionArray = new JSONArray(array);
				JSONObject transactionJson = new JSONObject();

				for (int i = 0; i < transactionArray.length(); i++) {
					transactionJson = transactionArray.getJSONObject(i);
				}
				if (transactionJson.has(Constants.AMOUNT)) {
					transaction.setAmount(transactionJson.get(Constants.AMOUNT).toString());
				}

				if (transactionJson.has(Constants.SUCCESS)) {
					transaction.setSuccess(transactionJson.get(Constants.SUCCESS).toString());
				}

				if (transactionJson.has(Constants.MESSAGE)) {
					transaction.setMessage(transactionJson.get(Constants.MESSAGE).toString());
				}

				if (transactionJson.has(Constants.REFUND_AIRPAY_ID)) {
					transaction.setRefundairpayid(transactionJson.get(Constants.REFUND_AIRPAY_ID).toString());
				}
			}

		}

		catch (Exception e) {
			logger.error("Exception", e);
		}

		return transaction;
	}

	public Transaction toStatusTransaction(String xml) {

		Transaction transaction = new Transaction();
		transaction
				.setTRANSACTIONSTATUS(getTextBetweenTags(xml, TRANSACTIONSTATUS_OPEN_TAG, TRANSACTIONSTATUS_CLOSE_TAG));
		transaction.setMESSAGE(getTextBetweenTags(xml, MESSAGE_OPEN_TAG, MESSAGE_CLOSE_TAG));
		transaction.setTRANSACTIONID(getTextBetweenTags(xml, TRANSACTIONID_OPEN_TAG, TRANSACTIONID_CLOSE_TAG));
		transaction.setTRANSACTIONPAYMENTSTATUS(
				getTextBetweenTags(xml, TRANSACTIONPAYMENTSTATUS_OPEN_TAG, TRANSACTIONPAYMENTSTATUS_CLOSE_TAG));
		transaction.setAPTRANSACTIONID(getTextBetweenTags(xml, APTRANSACTIONID_OPEN_TAG, APTRANSACTIONID_CLOSE_TAG));
		transaction.setAMOUNT(getTextBetweenTags(xml, AMOUNT_OPEN_TAG, AMOUNT_CLOSE_TAG));
		transaction.setCUSTOMERVPA(getTextBetweenTags(xml, CUSTOMERVPA_OPEN_TAG, CUSTOMERVPA_CLOSE_TAG));
		return transaction;
	}// toTransaction()

	public String getTextBetweenTags(String text, String tag1, String tag2) {

		int leftIndex = text.indexOf(tag1);
		if (leftIndex == -1) {
			return null;
		}

		int rightIndex = text.indexOf(tag2);
		if (rightIndex != -1) {
			leftIndex = leftIndex + tag1.length();
			return text.substring(leftIndex + 9, rightIndex - 3);
		}

		return null;
	}// getTextBetweenTags()

}
