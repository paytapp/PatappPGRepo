package com.paymentgateway.idbi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;




@Service("idbiTransactionCommunicator")
public class TransactionCommunicator {
	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());
	
	public void updateSaleResponse(Fields fields, String request) {
        logger.info("Request Parameter for IDBI: " + request);
		fields.put(FieldType.IDBI_FINAL_REQUEST.getName(), request);
		fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
	}
	
	@SuppressWarnings("incomplete-switch")
	public String getResponse(String request, Fields fields) throws SystemException {
		String response = "";
		String hostUrl = "";

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
				hostUrl = PropertiesManager.propertiesMap.get(Constant.REFUND_REQUEST_URL);
				break;
			case STATUS:
				hostUrl = PropertiesManager.propertiesMap.get(Constant.STATUS_ENQ_REQUEST_URL);
				break;
			}
				try {
					response = executePost(request, hostUrl, fields);
				} catch (Exception exception) {
					logger.error("Exception : " , exception);
				}
			
				return response;
			}
	            
			public String executePost(String urlParameters, String hostUrl, Fields fields) throws Exception {
		    logger.info("Refund Request for IDBI: Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + urlParameters);
				String response = "";

			HttpURLConnection connection = null;
			URL url;
			try {
				// Create connection
				url = new URL(hostUrl);

				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type",
						"application/x-www-form-urlencoded");

				connection.setRequestProperty("Content-Length",
						"" + Integer.toString(urlParameters.getBytes().length));
				connection.setRequestProperty("Content-Language", "en-US");

				connection.setUseCaches(false);
				connection.setDoInput(true);
				connection.setDoOutput(true);
			    connection.setConnectTimeout(60000);
			    connection.setReadTimeout(60000);

				// Send request
				DataOutputStream wr = new DataOutputStream(
						connection.getOutputStream());
				wr.writeBytes(urlParameters);
				wr.flush();
				wr.close();

				// Get Response
				InputStream is = connection.getInputStream();
				BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(is));
				String decodedString;
			
				while ((decodedString = bufferedreader.readLine()) != null) {
					response = response + decodedString;
				}
              	bufferedreader.close();
    			logger.info("Response Received from IDBI: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + response);
				return response;
	     }
	
			catch (Exception exception) {
				logger.error("Exception : " , exception);
				throw new SystemException(ErrorType.ACQUIRER_ERROR, "Unable to get respose from idbi");
			} finally {

				if (connection != null) {
					connection.disconnect();
				}
			}
	}
	



}
