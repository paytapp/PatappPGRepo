package com.paymentgateway.payu;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.xmlbeans.impl.common.IdentityConstraint.FieldState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.SystemConstants;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Rahul
 *
 */
@Service("payuTransactionCommunicator")
public class TransactionCommunicator {

	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());
	public static final String ALGO = "AES";
	private static String key = "9A8984DDAE18356B802EB5653066B7DF";
	private static Key keyObj = null;
	public void updateSaleResponse(Fields fields, String request) {

		if(fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode()) || fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())) {
			try {
				TransactionCommunicator tc = new TransactionCommunicator();
				tc.CryptoTest(key);
				fields.put(FieldType.PAYU_FINAL_REQUEST.getName(), hostedEncrypt(request));
			} catch (Exception e) {
				logger.error("Exception in Payu updateSaleRespone", e);
			}	
		}else {
			fields.put(FieldType.PAYU_FINAL_REQUEST.getName(), request.toString());	
		}
		
		
		fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());

	}

	@SuppressWarnings("incomplete-switch")
	public String getResponse(String request, Fields fields) throws SystemException {

		String hostUrl = "";

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
				hostUrl = PropertiesManager.propertiesMap.get(Constants.PAYU_REFUND_URL);
				break;
			case STATUS:
				hostUrl = PropertiesManager.propertiesMap.get(Constants.PAYU_STATUS_ENQ_URL);
				break;
			}

			logger.info("Request message to Payu : TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " "
					+ "Order Id = " + fields.get(FieldType.ORDER_ID.getName()) + "  Request = " + request);

			URL url = new URL(hostUrl);

			String[] arrOfStr = request.split("&", 6);
			Map<String, String> reqMap = new HashMap<String, String>();
			for (String reqst : arrOfStr) {
				String[] reqStr = reqst.split("=");
				reqMap.put(reqStr[0], reqStr[1]);
			}

			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String, String> param : reqMap.entrySet()) {
				if (postData.length() != 0)
					postData.append('&');
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}
			byte[] postDataBytes = postData.toString().getBytes("UTF-8");

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			connection.setDoOutput(true);
			connection.getOutputStream().write(postDataBytes);

			DataOutputStream requestWriter = new DataOutputStream(connection.getOutputStream());
			requestWriter.writeBytes(request);
			requestWriter.close();
			String responseData = "";
			InputStream is = connection.getInputStream();
			BufferedReader responseReader = new BufferedReader(new InputStreamReader(is));
			if ((responseData = responseReader.readLine()) != null) {
				//logger.info("Payu Refund Response >>>" + responseData);				
				log("Payu Refund Response >>>" + responseData ,fields);
			}
			
			responseReader.close();

			return responseData;

		} catch (Exception e) {
			logger.error("Exception in Payu txn Communicator", e);
		}
		return null;

	}

	public static String getTextBetweenTags(String text, String tag1, String tag2) {

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
	}

	
	private static void log(String message, Fields fields){
		message = Pattern.compile("(<card>)([\\s\\S]*?)(</card>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<pan>)([\\s\\S]*?)(</pan>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<expmonth>)([\\s\\S]*?)(</expmonth>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<expyear>)([\\s\\S]*?)(</expyear>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<cvv2>)([\\s\\S]*?)(</cvv2>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<password>)([\\s\\S]*?)(</password>)").matcher(message).replaceAll("$1$3");
	//	MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
		logger.info(message);
	}
	
	
	
	public void CryptoTest(String key) {
		this.key = key;
		this.keyObj = new SecretKeySpec(key.getBytes(), ALGO);
	}
	
	
	public static String hostedEncrypt(String data) throws SystemException {
		try {

			String ivString = key.substring(0, 16);
			IvParameterSpec iv = new IvParameterSpec(ivString.getBytes(SystemConstants.DEFAULT_ENCODING_UTF_8));

			Cipher cipher = Cipher.getInstance(ALGO + "/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, keyObj, iv);

			byte[] encValue = cipher.doFinal(data.getBytes(SystemConstants.DEFAULT_ENCODING_UTF_8));

			Base64.Encoder base64Encoder = Base64.getEncoder().withoutPadding();
			String base64EncodedData = base64Encoder.encodeToString(encValue);
			return base64EncodedData;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException
				| IllegalBlockSizeException | BadPaddingException
				| InvalidAlgorithmParameterException scramblerExceptionException) {
			throw new SystemException(ErrorType.CRYPTO_ERROR, scramblerExceptionException,
					"Error during encryption process");
		}
	}

}
