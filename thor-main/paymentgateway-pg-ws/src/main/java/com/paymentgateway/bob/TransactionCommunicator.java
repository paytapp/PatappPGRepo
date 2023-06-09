package com.paymentgateway.bob;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Rahul
 *
 */
@Service("bobTransactionCommunicator")
public class TransactionCommunicator {

	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private UserSettingDao userSettingDao;

	
	public void updateSaleResponse(Fields fields, String request) {
		
		String requestUrl = ConfigurationConstants.BOB_SALE_REQUEST_URL.getValue();
		String responseUrl = null;
		
		UserSettingData userSetting = userSettingDao.fetchDataUsingPayId(fields.get(FieldType.PAY_ID.getName()));
		if (userSetting.isAllowCustomHostedUrl()) {
			responseUrl = (userSetting.getCustomHostedUrl() + "/pgui/jsp/bobResponse");
		} else {
			responseUrl = PropertiesManager.propertiesMap.get(Constants.RESPONSE_URL);
		}
			
		StringBuilder req = new StringBuilder();
		req.append(requestUrl);
		req.append(request);
		req.append(Constants.BOB_ERROR_URL_TAG);
		req.append(responseUrl);
		req.append(Constants.BOB_RESPONSE_URL_TAG);
		req.append(responseUrl);
		req.append(Constants.BOB_TRANPORTAL_ID_TAG);
		req.append(fields.get(FieldType.MERCHANT_ID.getName()));
		
		fields.put(FieldType.BOB_FINAL_REQUEST.getName(), req.toString());
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
				hostUrl = PropertiesManager.propertiesMap.get(Constants.REFUND_URL);
				break;
			case STATUS:
				hostUrl = PropertiesManager.propertiesMap.get(Constants.STATUS_ENQ_URL);
				break;
			}

			URL url = new URL(hostUrl);
			logger.info("Request message to bob: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + request);
			 //logRequest(request, hostUrl,fields);
			URLConnection connection = null;
			if (ConfigurationConstants.IS_DEBUG.getValue().equals("1")) {
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestProperty("Content-Type", "application/xml");
			} else {
				connection = (HttpsURLConnection) url.openConnection();
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			}
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
			logger.info("Response mesage from bob: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + response);
			// logResponse(response, hostUrl,fields);
			return response;
		} catch (IOException ioException) {
			MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			logger.error("Network Exception with BOB", ioException);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, ioException,
					"Network Exception with BOB " + hostUrl.toString());
		}
	}

}
