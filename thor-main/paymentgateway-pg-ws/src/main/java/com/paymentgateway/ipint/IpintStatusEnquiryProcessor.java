package com.paymentgateway.ipint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.pg.core.util.ProcessManager;
import com.paymentgateway.pg.core.util.Processor;

@Service
public class IpintStatusEnquiryProcessor {

	@Autowired
	@Qualifier("ipintTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	private IpintTransformer ipintTransformer;

	@Autowired
	private IpintSaleResponseHandler ipintSaleResponseHandler;

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private FieldsDao fieldsDao;

	private static Logger logger = LoggerFactory.getLogger(IpintStatusEnquiryProcessor.class.getName());

	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA384";

	public void enquiryProcessor(Fields fields) throws Exception {
		String request = statusEnquiry(fields);

		try {
			logger.info("Ipint Status Enquiry PR_PEF_NUM : " + fields.get(FieldType.PG_REF_NUM.getName()) + request);
			Transaction transaction = ipintSaleResponseHandler.toTransactionStatusEnquiry(request);
			ipintTransformer = new IpintTransformer(transaction);

			if (transaction.getTransactionStatus() != null && (!transaction.getTransactionStatus().equalsIgnoreCase("CHECKING"))) {
				if (transaction.getTransactionStatus() != null && (transaction.getTransactionStatus().equalsIgnoreCase("COMPLETED")
						|| transaction.getTransactionStatus().equalsIgnoreCase("FAILED"))) {
					String newTxnId = TransactionManager.getNewTransactionId();
					ipintTransformer.updateResponse(fields);
					fields = fieldsDao.getPreviousForPgRefNumForCyrpto(fields.get(FieldType.PG_REF_NUM.getName()));
					if (transaction.getTransactionStatus().equalsIgnoreCase("COMPLETED")) {
						fields.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
					} else {
						fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
					}
					fields.put(FieldType.TXN_ID.getName(), newTxnId);
					fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
					ProcessManager.flow(updateProcessor, fields, true);
					Fields newFields = new Fields(fields);
					try {
						Runnable runnable = new Runnable() {
							@Override
							public void run() {
								logger.info("Callback After Status Enquiry for orderID = "
										+ newFields.get(FieldType.ORDER_ID.getName()) + " And pgRef = "
										+ newFields.get(FieldType.PG_REF_NUM.getName()));
								fieldsDao.sendCallbackAfterTxnEquiry(newFields);
							}
						};
						propertiesManager.executorImpl(runnable);
					} catch (Exception e) {
						logger.error("Callback of Txn Status failed after status enquiry for Order Id "
								+ fields.get(FieldType.ORDER_ID.getName()));
						logger.error("Exception in Callback ", e);

					}

				}

			}	
			
		} catch (Exception e) {
			logger.error("Excepation  ipint ", e);
		}
	}

	public String statusEnquiry(Fields fields) throws Exception {
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("id", fields.get(FieldType.ACQ_ID.getName()));
		String response = sendSignedRequest(parameters, "/invoice", "GET", fields);
		return response;

	}

	public String sendSignedRequest(HashMap<String, String> parameters, String urlPath, String httpMethod,
			Fields fields) throws Exception {
		StringBuffer response = new StringBuffer();
		try {
			String queryPath = "";
			String signature = "";
			String signatureMessage = ""; // to be signed
			long nonce = System.currentTimeMillis();
			String baseUrl = PropertiesManager.propertiesMap.get(Constants.STATUS_IPINT_ENQ_URL);
			if (!parameters.isEmpty()) {
				queryPath += joinQueryParameters(parameters);
			}
			try {
				signatureMessage = "/api/" + nonce + urlPath + "?" + queryPath;
				// System.out.println("signature message input:" +
				// signatureMessage);
				signature = getSignature(signatureMessage, fields.get(FieldType.PASSWORD.getName()));
				// System.out.println("Signature: " + signature);
			} catch (Exception e) {
				logger.error("Please Ensure Your Secret Key Is Set Up Correctly! ", e);
				System.exit(0);
			}

			URL obj = new URL(baseUrl + urlPath + "?" + queryPath);
			System.out.println("url:" + obj.toString());

			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			if (httpMethod != null) {
				con.setRequestMethod(httpMethod);
			}
			// add API_KEY to header content
			con.setRequestProperty("apikey", fields.get(FieldType.TXN_KEY.getName()));
			con.setRequestProperty("content-type", "application/json");
			con.setRequestProperty("nonce", String.valueOf(nonce));
			con.setRequestProperty("signature", signature);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			logger.info("Ipint Status enquiry response for PgRefNum= " + fields.get(FieldType.PG_REF_NUM.getName())
					+ ":" + response.toString());

		} catch (Exception exception) {
			logger.error("Ipint Status enquir  >>>> ", exception);
		}
		return response.toString();
	}

	private static String joinQueryParameters(HashMap<String, String> parameters) {
		String urlPath = "";
		boolean isFirst = true;

		for (Map.Entry mapElement : parameters.entrySet()) {
			if (isFirst) {
				isFirst = false;
				urlPath += (String) mapElement.getKey() + "=" + (String) mapElement.getValue();
			} else {
				urlPath += "&" + (String) mapElement.getKey() + "=" + (String) mapElement.getValue();
			}
		}
		return urlPath;
	}

	// convert byte array to hex string
	private String bytesToHex(byte[] bytes) {
		final char[] hexArray = "0123456789abcdef".toCharArray();
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0, v; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public String getSignature(String data, String key) {
		byte[] hmacSha384 = null;
		try {
			SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(secretKeySpec);
			hmacSha384 = mac.doFinal(data.getBytes());
		} catch (Exception e) {
			throw new RuntimeException("Failed to calculate hmac-sha384", e);
		}
		return bytesToHex(hmacSha384);
	}

}
