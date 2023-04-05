package com.paymentgateway.isgpay;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.ISGPayEncryption;

/**
 * @author Rahul
 *
 */
@Service
public class ISGPayStatusEnquiryProcessor {

	@Autowired
	@Qualifier("iSGPayTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	private ISGPayEncryption iSGPayEncryption;

	@Autowired
	private PropertiesManager propertiesManager;

	private static Logger logger = LoggerFactory.getLogger(ISGPayStatusEnquiryProcessor.class.getName());

	public void enquiryProcessor(Fields fields) {
		String request = statusEnquiryRequest(fields);
		String response = "";
		try {
			response = getResponse(request);
		} catch (SystemException exception) {
			logger.error("Exception", exception);
		}

		updateFields(fields, response);

	}

	public String statusEnquiryRequest(Fields fields) {

		String MerchantId = fields.get(FieldType.MERCHANT_ID.getName());
		String BankId = fields.get(FieldType.ADF1.getName());
		String TerminalId = fields.get(FieldType.ADF2.getName());
		String TxnRefNo = fields.get(FieldType.PG_REF_NUM.getName());
		String PassCode = fields.get(FieldType.ADF4.getName());
		String TxnType = "Status";

		String password = fields.get(FieldType.PASSWORD.getName());

		StringBuilder requestString = new StringBuilder();

		try {
			fields.put(FieldType.TERMINAL_ID.getName(), TerminalId);
			fields.put(FieldType.BANK_ID.getName(), BankId);

			LinkedHashMap<String, String> hmReqFields = new LinkedHashMap<String, String>();

			hmReqFields.put(Constants.PASS_CODE, PassCode);
			hmReqFields.put(Constants.MERCHANT_ID, MerchantId);
			hmReqFields.put(Constants.TXN_REF_NO, TxnRefNo);
			hmReqFields.put(Constants.TERMINAL_ID, TerminalId);
			hmReqFields.put(Constants.TXN_TYPE, TxnType);
			hmReqFields.put(Constants.BANK_ID, BankId);

			String hashCode = iSGPayEncryption.generateHash(hmReqFields, password);
			hmReqFields.put(Constants.HASH, hashCode);

			requestString.append(createPostDataFromMap(hmReqFields));
			logger.info("ISGPAY Status Enquiry resuest " + hmReqFields);
			return requestString.toString();
		}

		catch (Exception e) {
			logger.error("Exception in preparing ISGPay refund request " , e);
		}

		return requestString.toString();

	}

	public String getResponse(String request) throws SystemException {

		String hostUrl = "";

		try {

			hostUrl = propertiesManager.propertiesMap.get(Constants.ISGPAY_STATUS_ENQ_URL);

			String url = hostUrl + "?" + request;

			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			logger.info("Response mesage from isgpay statues Enquiry >>> " + response.toString());
			return response.toString();

		} catch (Exception e) {
			logger.error("Exception in ISGPAY status enquiry", e);
		}
		return null;
	}

	public void updateFields(Fields fields, String response) {

		Transaction transactionResponse = new Transaction();
		transactionResponse = toTransaction(response);
		updateResponse(fields, transactionResponse);

	}// toTransaction()

	public String getTextBetweenTags(String text, String tag1, String tag2) {

		int leftIndex = text.indexOf(tag1);
		if (leftIndex == -1) {
			return null;
		}

		int rightIndex = text.indexOf(tag2);
		if (rightIndex != -1) {
			leftIndex = leftIndex + tag1.length();
			return text.substring(leftIndex, rightIndex);
		}

		return null;
	}// getTextBetweenTags()

	private String createPostDataFromMap(final Map<String, String> fields) {
		final StringBuffer buf = new StringBuffer();
		String ampersand = "";
		for (final String key : fields.keySet()) {
			final String value = fields.get(key);
			if (value != null && value.length() > 0) {
				buf.append(ampersand);
				buf.append(URLEncoder.encode(key));
				buf.append('=');
				buf.append(URLEncoder.encode(value));
			}
			ampersand = "&";
		}
		return buf.toString();
	}

	public void updateResponse(Fields fields, Transaction transaction) {

		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;

		if ((StringUtils.isNotBlank(transaction.getResponseCode()))
				&& ((transaction.getResponseCode()).equalsIgnoreCase(ISGPayResultType.ISGPAY001.getBankCode())))

		{
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

		}

		else {
			if ((StringUtils.isNotBlank(transaction.getResponseCode()))) {

				ISGPayResultType resultInstance = ISGPayResultType.getInstanceFromName(transaction.getResponseCode());

				// Get processor response using parameter ccAuthReply_processorResponse
				if (resultInstance == null) {
					resultInstance = ISGPayResultType
							.getInstanceFromName(transaction.getCcAuthReply_processorResponse());
				}

				if (resultInstance != null) {
					status = resultInstance.getStatusCode();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getiPayCode());
					pgTxnMsg = resultInstance.getMessage();
				} else {
					status = StatusType.FAILED_AT_ACQUIRER.getName();
					errorType = ErrorType.getInstanceFromCode("022");
					if (StringUtils.isNotBlank(transaction.getMessage())) {
						pgTxnMsg = transaction.getMessage();
					}
					else {
						pgTxnMsg = "Transaction failed at acquirer";
					}
					
				}

			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;
				if (StringUtils.isNotBlank(transaction.getMessage())) {
					pgTxnMsg = transaction.getMessage();
				}
				else {
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
				}
				

			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		if (StringUtils.isNotBlank(transaction.getAuthCode())) {
			fields.put(FieldType.AUTH_CODE.getName(), transaction.getAuthCode());
		}

		else {
			fields.put(FieldType.AUTH_CODE.getName(), "0");
		}

		// Added by shaiwal for different fields in VISA and MasterCard
		if (StringUtils.isNotBlank(fields.get(FieldType.MOP_TYPE.getName()))
				&& fields.get(FieldType.MOP_TYPE.getName()).equalsIgnoreCase(MopType.MASTERCARD.getCode())) {

			if (StringUtils.isNotBlank(transaction.getRetRefNo())) {
				fields.put(FieldType.ACQ_ID.getName(), transaction.getPgTxnId());
			}

			if (StringUtils.isNotBlank(transaction.getPgTxnId())) {
				fields.put(FieldType.RRN.getName(), transaction.getRetRefNo());
			}
		}

		else if (StringUtils.isNotBlank(fields.get(FieldType.MOP_TYPE.getName()))
				&& fields.get(FieldType.MOP_TYPE.getName()).equalsIgnoreCase(MopType.VISA.getCode())) {

			if (StringUtils.isNotBlank(transaction.getRetRefNo())) {
				fields.put(FieldType.ACQ_ID.getName(), transaction.getRetRefNo());
			}

			if (StringUtils.isNotBlank(transaction.getPgTxnId())) {
				fields.put(FieldType.RRN.getName(), transaction.getPgTxnId());
			}
		}

		else {
			if (StringUtils.isNotBlank(transaction.getRetRefNo())) {
				fields.put(FieldType.ACQ_ID.getName(), transaction.getRetRefNo());
			}

			if (StringUtils.isNotBlank(transaction.getPgTxnId())) {
				fields.put(FieldType.RRN.getName(), transaction.getPgTxnId());
			}
		}

		fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getResponseCode());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);

	}

	public Transaction toTransaction(String isgPayResponse) {

		Transaction transaction = new Transaction();

		String responseparamsArray[] = isgPayResponse.split(Pattern.quote("&"));

		Map<String, String> responseMap = new HashMap<String, String>();

		for (String item : responseparamsArray) {

			String itemsArray[] = item.split("=");
			String key = itemsArray[0];
			String value = itemsArray[1];
			responseMap.put(key, value);

		}

		logger.info("Final Bank Response for ISGPAY : " + isgPayResponse);

		if (StringUtils.isNotBlank(responseMap.get("BankId")) && !responseMap.get("BankId").equalsIgnoreCase("N/A")) {
			transaction.setBankId(responseMap.get("BankId"));
		}

		if (StringUtils.isNotBlank(responseMap.get("ResponseCode"))) {
			transaction.setResponseCode(responseMap.get("ResponseCode"));
		}

		if (StringUtils.isNotBlank(responseMap.get("Message"))) {
			transaction.setMessage(responseMap.get("Message"));
		}

		if (StringUtils.isNotBlank(responseMap.get("pgTxnId")) && !responseMap.get("pgTxnId").equalsIgnoreCase("N/A")) {
			transaction.setPgTxnId(responseMap.get("pgTxnId"));
		}

		if (StringUtils.isNotBlank(responseMap.get("TxnRefNo"))
				&& !responseMap.get("TxnRefNo").equalsIgnoreCase("N/A")) {
			transaction.setTxnRefNo(responseMap.get("TxnRefNo"));
		}

		if (StringUtils.isNotBlank(responseMap.get("TxnId")) && !responseMap.get("TxnId").equalsIgnoreCase("N/A")) {
			transaction.setTxnId(responseMap.get("TxnId"));
		}

		if (StringUtils.isNotBlank(responseMap.get("Message"))) {
			transaction.setMessage(responseMap.get("Message"));
		}

		if (StringUtils.isNotBlank(responseMap.get("AuthCode"))
				&& !responseMap.get("AuthCode").equalsIgnoreCase("N/A")) {
			transaction.setAuthCode(responseMap.get("AuthCode"));
		}

		if (StringUtils.isNotBlank(responseMap.get("RetRefNo"))
				&& !responseMap.get("RetRefNo").equalsIgnoreCase("N/A")) {
			transaction.setRetRefNo(responseMap.get("RetRefNo"));
		}

		return transaction;
	}

}
