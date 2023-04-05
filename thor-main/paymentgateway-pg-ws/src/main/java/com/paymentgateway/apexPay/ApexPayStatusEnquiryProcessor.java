package com.paymentgateway.apexPay;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class ApexPayStatusEnquiryProcessor {

	@Autowired
	@Qualifier("apexPayTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	private Hasher hasher;

	private static Logger logger = LoggerFactory.getLogger(ApexPayStatusEnquiryProcessor.class.getName());

	public void enquiryProcessor(Fields fields) {
		String request = statusEnquiryRequest(fields);
		String response = "";
		try {

			response = getResponse(request);
			updateFields(fields, response);

		} catch (SystemException exception) {
			logger.error("Exception", exception);
		} catch (Exception e) {
			logger.error("Exception in decrypting status enquiry response for Asiancheckout ", e);
		}

	}

	public String statusEnquiryRequest(Fields fields) {
		try {
			String merchantId = fields.get(FieldType.MERCHANT_ID.getName());
			String transactionId = fields.get(FieldType.PG_REF_NUM.getName());
			String amount = fields.get(FieldType.TOTAL_AMOUNT.getName());
			amount = Amount.formatAmount(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
			String currency = fields.get(FieldType.CURRENCY_CODE.getName());

			Fields statusFields = new Fields();
			statusFields.put(FieldType.PAY_ID.getName(), merchantId);
			statusFields.put(FieldType.ORDER_ID.getName(), transactionId);
			statusFields.put(FieldType.AMOUNT.getName(), amount);
			statusFields.put(FieldType.TXNTYPE.getName(), "STATUS");
			statusFields.put(FieldType.CURRENCY_CODE.getName(), currency);

			String hash = Hasher.getHashWithSalt(statusFields, fields.get(FieldType.PASSWORD.getName()));

			statusFields.put(FieldType.HASH.getName(), hash);

			String jsonString = fields.toJSONString(statusFields);

			return jsonString;
		} catch (Exception e) {
			logger.error("Exception in preparing ApexPay Status Enquiry Request", e);
			return null;
		}
	}

	public static String getResponse(String jsonRrequest) throws SystemException {
		if (StringUtils.isBlank(jsonRrequest)) {
			logger.info("Request is empty for status enquiry");
			return null;
		}
		String hostUrl = "";
		try {
			
			logger.info("Apexpay Status Enquiry Request : " + jsonRrequest);
			
			hostUrl = PropertiesManager.propertiesMap.get(Constants.APEXPAY_STATUS_ENQ_URL);
			OkHttpClient client = new OkHttpClient();
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType, jsonRrequest);
			Request request = new Request.Builder().url(hostUrl).post(body)
					.addHeader("content-type", "application/json").addHeader("cache-control", "no-cache").build();
			Response response = client.newCall(request).execute();
			String res = response.body().string();
			logger.info("Apexpay Status Enquiry Response : " + res);
			
			return res;
		} catch (Exception e) {
			logger.error("Exception in Status Enquiry ", e);
			return null;
		}
	}

	public void updateFields(Fields fields, String jsonResponse) {

		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;

		JSONObject jsonObj = new JSONObject(jsonResponse);
		if (jsonObj.has("RESPONSE_CODE") && jsonObj.has("STATUS")
				&& "000".equalsIgnoreCase(jsonObj.getString("RESPONSE_CODE"))
				&& "Captured".equalsIgnoreCase(jsonObj.getString("STATUS"))) {

			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;

			if (jsonObj.has("RESPONSE_MESSAGE")) {
				pgTxnMsg = jsonObj.getString("RESPONSE_MESSAGE");
			}
		} else {
			status = StatusType.FAILED_AT_ACQUIRER.getName();
			errorType = ErrorType.FAILED;
			if (jsonObj.has("RESPONSE_MESSAGE")) {
				pgTxnMsg = jsonObj.getString("RESPONSE_MESSAGE");
			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
		
		if (jsonObj.toString().contains("ACQ_ID")) {
			fields.put(FieldType.ACQ_ID.getName(), jsonObj.getString("ACQ_ID"));
		}
		else {
			fields.put(FieldType.ACQ_ID.getName(), "NA");
		}
		
		if (jsonObj.toString().contains("RRN")) {
			fields.put(FieldType.RRN.getName(), jsonObj.getString("RRN"));
		}
		else {
			fields.put(FieldType.RRN.getName(), "NA");
		}
		
		fields.put(FieldType.PG_RESP_CODE.getName(), jsonObj.getString("RESPONSE_CODE"));
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
	}

}
