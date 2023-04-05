package com.paymentgateway.fssPay;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class FssPayStatusEnquiryProcessor {

	@Autowired
	@Qualifier("fssPayTransactionConverter")
	private TransactionConverter converter;

	private FssPayTransformer fssPayTransformer = null;

	private static Logger logger = LoggerFactory.getLogger(FssPayStatusEnquiryProcessor.class.getName());

	public static final String REQUEST_OPEN_TAG = "<request>";
	public static final String REQUEST_CLOSE_TAG = "</request>";
	public static final String RESULT_OPEN_TAG = "<result>";
	public static final String RESULT_CLOSE_TAG = "</result>";
	public static final String ERROR_TEXT_OPEN_TAG = "<error_text>";
	public static final String ERROR_TEXT_CLOSE_TAG = "</error_text>";
	public static final String PAYMENT_ID_OPEN_TAG = "<paymentid>";
	public static final String PAYMENT_ID_CLOSE_TAG = "</paymentid>";
	public static final String AUTH_OPEN_TAG = "<auth>";
	public static final String AUTH_CLOSE_TAG = "</auth>";
	public static final String REF_OPEN_TAG = "<ref>";
	public static final String REF_CLOSE_TAG = "</ref>";
	public static final String AVR_OPEN_TAG = "<avr>";
	public static final String AVR_CLOSE_TAG = "</avr>";
	public static final String TRANID_OPEN_TAG = "<tranid>";
	public static final String TRANID_CLOSE_TAG = "</tranid>";
	public static final String ERROR_CODE_OPEN_TAG = "<error_code_tag>";
	public static final String ERROR_CODE_CLOSE_TAG = "</error_code_tag>";
	public static final String ERROR_SERVICE_OPEN_TAG = "<error_service_tag>";
	public static final String ERROR_SERVICE_CLOSE_TAG = "</error_service_tag>";
	public static final String AMOUNT_OPEN_TAG = "<amt>";
	public static final String AMOUNT_CLOSE_TAG = "</amt>";
	public static final String TRACKID_OPEN_TAG = "<trackid>";
	public static final String TRACKID_CLOSE_TAG = "</trackid>";
	public static final String PAY_ID_OPEN_TAG = "<payid>";
	public static final String PAY_ID_CLOSE_TAG = "</payid>";
	public static final String AUTH_RESC_OPEN_TAG = "<authrescode>";
	public static final String AUTH_RESC_CLOSE_TAG = "</authrescode>";

	public static final String REQUEST = "request";
	public static final String ID = "id";
	public static final String PASSWORD = "password";
	public static final String ACTION = "action";
	public static final String AMT = "amt";
	public static final String CURRENCYCODE = "currencycode";
	public static final String TRACKID = "trackId";
	public static final String CARD = "card";
	public static final String EXPMONTH = "expmonth";
	public static final String EXPYEAR = "expyear";
	public static final String CVV2 = "cvv2";
	public static final String MEMBER = "member";
	public static final String TYPE = "type";
	public static final String ERRORURL = "errorURL";
	public static final String RESPONSEURL = "responseURL";
	public static final String LANGUAGE = "langid";
	public static final String TRANSID = "transid";
	public static final String CURRENCY = "currency";
	public static final String UDF5 = "udf5";

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

		StringBuilder xml = new StringBuilder();
		String language = PropertiesManager.propertiesMap.get(Constants.LANGUAGE);
		String udf5 = PropertiesManager.propertiesMap.get(Constants.UDF5_VALUE);
		String amount = fields.get(FieldType.TOTAL_AMOUNT.getName());
		String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());

		if (!amount.contains(".")) {
			amount = Amount.toDecimal(amount, currencyCode);
		}
		String paymentType = fields.get(FieldType.PAYMENT_TYPE.getName());
		String type = null;
		if (paymentType.equals(PaymentType.CREDIT_CARD.getCode())) {
			type = Constants.CREDIT_CARD;
		} else {
			type = Constants.DEBIT_CARD;
		}
		String txnId = TransactionManager.getNewTransactionId();
		xml.append(REQUEST_OPEN_TAG);
		getElement(CURRENCYCODE, fields.get(FieldType.CURRENCY_CODE.getName()), xml);
		getElement(TYPE, type, xml);
		getElement(LANGUAGE, language, xml);
		getElement(ID, fields.get(FieldType.MERCHANT_ID.getName()), xml);
		getElement(TRANSID, fields.get(FieldType.PG_REF_NUM.getName()), xml);
		getElement(ACTION, Constants.STATUS_ENQ_ACTION, xml);
		getElement(TRACKID, txnId, xml);
		getElement(AMT, amount, xml);
		getElement(PASSWORD, fields.get(FieldType.PASSWORD.getName()), xml);
		getElement(UDF5, udf5, xml);
		xml.append(REQUEST_CLOSE_TAG);
		
		//logger.info("Status enquiry request to FSSPAY : " + xml.toString());
		log("Status enquiry request to FSSPAY : " + xml.toString(), fields);
		return xml.toString();

	}

	public static void getElement(String name, String value, StringBuilder xml) {
		if (null == value) {
			return;
		}

		xml.append("<");
		xml.append(name);
		xml.append(">");
		xml.append(value);
		xml.append("</");
		xml.append(name);
		xml.append(">");
	}

	public static String getResponse(String request) throws SystemException {

		String hostUrl = "";

		Fields fields = new Fields();
		fields.put(FieldType.TXNTYPE.getName(), "STATUS");
		try {

			TransactionType transactionType = TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()));
			switch (transactionType) {
			case SALE:
			case AUTHORISE:
				break;
			case ENROLL:
				break;
			case CAPTURE:
				break;
			case REFUND:
				hostUrl = "";
				;
				break;
			case STATUS:
				hostUrl = PropertiesManager.propertiesMap.get(Constants.STATUS_ENQ_URL);
				;
			}

			URL url = new URL(hostUrl);
			URLConnection connection = null;
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Content-Type", "application/xml");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			DataOutputStream dataoutputstream = new DataOutputStream(connection.getOutputStream());
			dataoutputstream.writeBytes(request);
			dataoutputstream.flush();
			dataoutputstream.close();
			BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String decodedString;
			String response = "";
			while ((decodedString = bufferedreader.readLine()) != null) {
				response = response + decodedString;
			}
			//logger.info("Response received from FSSPAY : " + response);
			log("Status enquiry request to FSSPAY : " +response, fields);
			return response;
		} catch (IOException ioException) {
			MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			// logger.error("Network Exception with FSS", ioException);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, ioException,
					"Network Exception with FSS " + hostUrl.toString());
		}
	}

	public void updateFields(Fields fields, String xml) {

		Transaction transactionResponse = new Transaction();
		transactionResponse = converter.toTransaction(xml);
		fssPayTransformer = new FssPayTransformer(transactionResponse);
		fssPayTransformer.updateResponse(fields);

		/*
		 * //Transaction transaction = new Transaction(); String result =
		 * getTextBetweenTags(xml, RESULT_OPEN_TAG, RESULT_CLOSE_TAG);
		 * fields.put(FieldType.PG_TXN_MESSAGE.getName(), result);
		 * fields.put((FieldType.PG_RESP_CODE.getName()),(getTextBetweenTags(xml,
		 * ERROR_CODE_OPEN_TAG, ERROR_CODE_CLOSE_TAG)));
		 * fields.put((FieldType.ACQ_ID.getName()),(getTextBetweenTags(xml,
		 * TRANID_OPEN_TAG, TRANID_CLOSE_TAG)));
		 * fields.put((FieldType.AVR.getName()),(getTextBetweenTags(xml, AVR_OPEN_TAG,
		 * AVR_CLOSE_TAG)));
		 * fields.put((FieldType.RRN.getName()),(getTextBetweenTags(xml, REF_OPEN_TAG,
		 * REF_CLOSE_TAG)));
		 * fields.put((FieldType.AUTH_CODE.getName()),(getTextBetweenTags(xml,
		 * AUTH_OPEN_TAG, AUTH_CLOSE_TAG)));
		 */

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

	private static void log(String message, Fields fields) {
		message = Pattern.compile("(<card>)([\\s\\S]*?)(</card>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<pan>)([\\s\\S]*?)(</pan>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<expmonth>)([\\s\\S]*?)(</expmonth>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<expyear>)([\\s\\S]*?)(</expyear>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<cvv2>)([\\s\\S]*?)(</cvv2>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<password>)([\\s\\S]*?)(</password>)").matcher(message).replaceAll("$1$3");
		MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
		logger.info(message);
	}
}
