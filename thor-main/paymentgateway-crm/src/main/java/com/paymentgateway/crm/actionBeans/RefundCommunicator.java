package com.paymentgateway.crm.actionBeans;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;

@Service("refundCommunicator")
public class RefundCommunicator {

private static final Logger logger = LoggerFactory.getLogger(RefundCommunicator.class);
	
	/*@Autowired
	private ConfigurationProvider configProvider;*/

    @Autowired
    private Fields field;
	public String communicator(JSONObject refundValue) throws SystemException {		

			 String hostUrl = PropertiesManager.propertiesMap.get("RefundURL");
		
		try {
			// String authString = userName + ":" + pass;
			// byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			// String authStringEnc = new String(authEncBytes);

			URL url = new URL(hostUrl);
			int timeout = 20000;
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(timeout);
			// conn.setRequestProperty("Authorization", "Basic " +
			// authStringEnc);
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");

			OutputStream os = conn.getOutputStream();
			os.write(refundValue.toString().getBytes());
			os.flush();
			
			logger.info("Refund Communicator Request : " + refundValue.toString());
			
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
				
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			conn.disconnect();
			String serverResponse = sb.toString();
			
			// JSONObject response = new JSONObject(serverResponse);
			logger.info("Refund Communicator Response : " + maskRefundFields(serverResponse));
			return serverResponse;
		} catch (Exception exception) {
			logger.info("Exception in Refund WS call : " + exception.getMessage());
		}
		return null;
	}
	
	public String maskRefundFields(String request) {
  		JSONObject jsonReq = new JSONObject(request);
   	   for(String key : jsonReq.keySet()) {
   		   if(key.equalsIgnoreCase("CUST_EMAIL")) {
   			  String reqValue = field.maskEmail(jsonReq.getString(key));
   			 jsonReq.put("CUST_EMAIL", reqValue);
   		   } 		   
   		   else if(key.equalsIgnoreCase("CUST_PHONE")) {
   			  String reqValue = field.fieldMask(jsonReq.getString(key));
   			 jsonReq.put("CUST_PHONE", reqValue);
   		   }		  
   	   }
   	  return jsonReq.toString();
  	}
	
	public String communicatorFromFileData(List<JSONObject> valiJsonObjectList, List<JSONObject> invalidJsonObjectList, Integer totalFileEntry, Integer totalInvalid, String refundType, User sessionUser) throws SystemException {

		String hostUrl = PropertiesManager.propertiesMap.get(Constants.REFUND_URL_FOR_FILEDATA.getValue());

		try {
			JSONObject refundValue = new JSONObject();
			refundValue.put("validData", valiJsonObjectList);
			refundValue.put("inValidData", invalidJsonObjectList);
			refundValue.put("totalFileEntry", totalFileEntry);
			refundValue.put("totalInvalid", totalInvalid);
			refundValue.put("refundType", refundType);
			refundValue.put("sessionUser", sessionUser.getUserType());

			URL url = new URL(hostUrl);
			int timeout = 20000;
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(timeout);
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");

			OutputStream os = conn.getOutputStream();
			os.write(refundValue.toString().getBytes());
			os.flush();

			logger.info("Refund Communicator Request : " + refundValue.toString());

			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			conn.disconnect();
			String serverResponse = sb.toString();

			// JSONObject response = new JSONObject(serverResponse);
			logger.info("Refund Communicator Response : " + maskRefundFields(serverResponse));
			return serverResponse;
		} catch (Exception exception) {
			logger.info("Exception in Refund WS call : " + exception.getMessage());
		}
		return null;
	}
}
