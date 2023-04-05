package com.paymentgateway.billDesk;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pg.core.util.BillDeskUtil;

/**
 * @author Rahul
 *
 */
@Service
public class BillDeskStatusEnquiryProcessor {

	private static Logger logger = LoggerFactory.getLogger(BillDeskStatusEnquiryProcessor.class.getName());

	@Autowired
	private BillDeskUtil billDeskUtil;

	private BillDeskTransformer billDeskTransformer = null;

	@Autowired
	@Qualifier("billDeskTransactionConverter")
	private TransactionConverter converter;

	public void enquiryProcessor(Fields fields) {
		String request = statusEnquiryRequest(fields);
		StringBuilder req = new StringBuilder();
		req.append(Constants.REQUEST_MSG);
		req.append(request);
		request = req.toString();
		String response = "";
		try {
			response = getResponse(request, fields);
		} catch (SystemException exception) {
			logger.error("Exception", exception);
		}

		updateFields(fields, response);

	}

	public String statusEnquiryRequest(Fields fields) {
		logger.info("Status enquiry initiated for PG_REF_NO: " + fields.get(FieldType.PG_REF_NUM.getName())
				+ " with Billdesk");
		DateFormat currentDate = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar calobj = Calendar.getInstance();
		String timeStamp = currentDate.format(calobj.getTime());
		StringBuilder request = new StringBuilder();
		request.append(Constants.STATUS_ENQ_REQUEST_TYPE);
		request.append(Constants.PIPE);
		request.append(fields.get(FieldType.MERCHANT_ID.getName()));
		request.append(Constants.PIPE);
		request.append(fields.get(FieldType.PG_REF_NUM.getName()));
		request.append(Constants.PIPE);
		request.append(timeStamp);

		String checkSum = billDeskUtil.generateChecksum(request.toString(), fields.get(FieldType.TXN_KEY.getName()));
		request.append(Constants.PIPE);
		request.append(checkSum);

		return request.toString();

	}

	public static String getResponse(String request, Fields fields) throws SystemException {

		String response = "";
		String hostUrl = "";
		
		hostUrl = PropertiesManager.propertiesMap.get(Constants.ENQUIRY_REQUEST_URL);

		try {
			response = executePost(request, hostUrl, fields);
		} catch (Exception exception) {
			logger.error("Exception : " , exception.getMessage());
		}

		return response;
	}

	public static String executePost(String urlParameters, String hostUrl, Fields fields) throws Exception {
		logger.info("Status Enq Request for BillDesk: Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " "
				+ urlParameters);
		String response = "";

		HttpURLConnection connection = null;
		URL url;
		try {
			// Create connection
			url = new URL(hostUrl);

			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
			connection.setRequestProperty("Content-Language", "en-US");

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setConnectTimeout(60000);
			connection.setReadTimeout(60000);

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
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
			logger.info("Response Received from BillDesk: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " "
					+ "Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + response);
			return response;
		}

		catch (Exception exception) {
			logger.error("Exception : ", exception);
			throw new SystemException(ErrorType.ACQUIRER_ERROR, "Unable to get respose from BillDesk");
		} finally {

			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	public void updateFields(Fields fields, String response) {

		Transaction transactionResponse = new Transaction();
		transactionResponse = converter.statusToTransaction(response, fields);
		billDeskTransformer = new BillDeskTransformer(transactionResponse);
		billDeskTransformer.updateResponse(fields);

	}// toTransaction()

}
