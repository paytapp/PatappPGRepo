package com.paymentgateway.qaicash;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

/**
 * @author Shaiwal
 *
 */
@Service("qaicashTransactionCommunicator")
public class TransactionCommunicator {

	
	@Autowired
	private PropertiesManager propertiesManager;
	
	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());

	public void updateSaleResponse(Fields fields, String paymentUrl) {

		fields.put(FieldType.QAICASH_FINAL_REQUEST.getName(), paymentUrl);
		fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());

	}


	public String refundPostRequest(String request, String hostUrl) throws SystemException {
		
		// Refund is not supported by Qaicash
		String response = "";
		return response;
	}

	public String statusEnqPostRequest(String hostUrl) throws SystemException {
		String stringResponse = "";
		HttpsURLConnection connection = null;
		try {
			
			logger.info("Status Enquiry Request URL for Qaicash : " + hostUrl);
			StringBuilder serverResponse = new StringBuilder();
			URL url = new URL(hostUrl);

			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Language", "en-US");
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			int status = connection.getResponseCode();

			// No deposit found with this order id
			if (status == 404) {
				serverResponse.append("No Such Transaction Found");
				connection.disconnect();
				return serverResponse.toString();
			}
			
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
				serverResponse.append('\r');
			}
			rd.close();
			String str = serverResponse.toString();
			
			logger.info("Status Enquiry Response for Qaicash :{} ",str);
			return str;

		} 
		catch (FileNotFoundException fnf) {
			logger.error("Exception in getting Status Enquiry respose for Qaicash", fnf);
			logger.error("Exception in getting Status Enquiry respose for Qaicash", fnf.getMessage());
			logger.error("Exception in getting Status Enquiry respose for Qaicash", fnf.getStackTrace());
			
		}
		catch (Exception e) {
			logger.error("Exception in getting Status Enquiry respose for Qaicash", e);
		}
		return stringResponse;
	}
	

	public String getPaymentResponse(String request,Fields fields) throws SystemException {
		String stringResponse = "";

		try {
			
			StringBuilder saleUrl = new StringBuilder();
			
			saleUrl.append(propertiesManager.propertiesMap.get("QAICASHSaleUrl"));
			saleUrl.append(fields.get(FieldType.MERCHANT_ID.getName()));
			saleUrl.append("/deposit/");
			
			URL obj = new URL(saleUrl.toString());
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			StringBuilder sb = new StringBuilder();
			JSONObject object = new JSONObject(request);
			Iterator<String> keys = object.keys();

			while (keys.hasNext()) {
				String key = keys.next();
				String value = object.getString(key);
				sb.append(key + "=" + value + "&");
			}

			String urlParams = sb.toString();
			urlParams = urlParams.substring(0, urlParams.length() - 1);

			con.setDoOutput(true);
			con.setDoInput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParams);
			wr.flush();
			wr.close();
			logger.info("Sending Qaicash POST request for getting payment URL: " + saleUrl);
			int responseCode = con.getResponseCode();
			logger.info("Qaicash POST request responseCode {} for Pg Ref Num {}",responseCode ,fields.get(FieldType.PG_REF_NUM.getName()));
			logger.info("Post parameters : " + urlParams);
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// print result
			logger.info(response.toString());
			return response.toString();

		} catch (Exception e) {
			logger.error("Exception in getting payment URL response for Qaicash", e);
		}
		return stringResponse;
	}
	

}
