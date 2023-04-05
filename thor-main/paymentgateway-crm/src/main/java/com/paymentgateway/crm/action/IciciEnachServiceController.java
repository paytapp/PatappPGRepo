package com.paymentgateway.crm.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.mpa.MPAFileEncoder;

public class IciciEnachServiceController {

	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	MPAFileEncoder encoder;
	
	@Autowired
	FieldsDao fieldsDao;
	
	private String txnId;

	private static Logger logger = LoggerFactory.getLogger(IciciEnachServiceController.class.getName());
	
	public String ENachRegistrationStausEnquiry() {
		try {
			
			/*HashMap<String, String> registrationDetailMap = fieldsDao.getENachRegistrationByTxnId(txnId);
			
			Map<String, Map<String, String>> merchantMap = new HashMap<String, Map<String, String>>();
			Map<String, String> merchantIdentifierMap = new HashMap<String, String>();
			
			//Map<String, Map<String,Map<String, String>>> paymentMap = new HashMap<String, Map<String, Map<String, String>>>();
			Map<String, Map<String, String>> paymentInstructionMap = new HashMap<String, Map<String, String>>();
			Map<String, String> paymentInstruction = new HashMap<String, String>();
			
			Map<String, Map<String, String>> transactionMap = new HashMap<String, Map<String, String>>();
			Map<String, String> transactionIdentifierMap = new HashMap<String, String>();
			
			Map<String, Map<String, String>> consumerMap = new HashMap<String, Map<String, String>>();
			Map<String, String> consumerIdentifierMap = new HashMap<String, String>();
			
			Map<String, Map<String, Map<String, String>>> finalMap = new HashMap<String, Map<String, Map<String, String>>>();
			
			
			String merchantId = PropertiesManager.propertiesMap.get(Constants.ICICI_ENACH_MERCHANT_ID);
			merchantIdentifierMap.put("identifier", merchantId);
			merchantMap.put("merchant", merchantIdentifierMap);
			
			paymentInstructionMap.put("instruction", paymentInstruction);
			//paymentMap.put("payment", paymentInstructionMap);
			
			transactionIdentifierMap.put("deviceIdentifier", "S");
			transactionIdentifierMap.put("type", "002");
			transactionIdentifierMap.put("currency", PropertiesManager.propertiesMap.get("CURRENCY_356"));
			transactionIdentifierMap.put("identifier", registrationDetailMap.get("TXN_ID"));
			transactionIdentifierMap.put("dateTime", registrationDetailMap.get("START_DATE"));
			transactionIdentifierMap.put("subType", "002");
			transactionIdentifierMap.put("requestType", "TSI");
			
			transactionMap.put("transaction", transactionIdentifierMap);
			
			consumerIdentifierMap.put("identifier", registrationDetailMap.get("CONSUMER_ID"));
			consumerMap.put("consumer", consumerIdentifierMap);
			
			finalMap.put("", merchantMap);
			finalMap.put("payment", paymentInstructionMap);
			finalMap.put("", transactionMap);
			finalMap.put("", consumerMap);
			
			{
				  "merchant": {
				    "identifier": "T3239"
				  },
				  "payment": {
				    "instruction": {}
				  },
				  "transaction": {
				    "deviceIdentifier": "S",
				    "type": "002",
				    "currency": "INR",
				    "identifier": "1385401123084035",
				    "dateTime": "23-11-2020",
				    "subType": "002",
				    "requestType": "TSI"
				  },
				  "consumer": {
				    "identifier": "c964634"
				  }
				}
			
			
			//HttpsURLConnection connection = null;
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.ICICI_ENACH_STATUS_ENQUIRY_URL);
			
			URL url = new URL(serviceUrl);
			int timeout = 20000;
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setConnectTimeout(timeout);
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept", "application/json");

			OutputStream os = connection.getOutputStream();
			os.write(finalMap.toString().getBytes());
			os.flush();
			
			logger.info("ICICI ENach Communicator Request : " + finalMap.toString());
			
			if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
				throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			connection.disconnect();
			String serverResponse = sb.toString();
			
			// JSONObject response = new JSONObject(serverResponse);
			logger.info("ICICI ENach Communicator Response : " + serverResponse);
			
			
			//fieldsDao.updateENachRegistrationDetailByTxnId(serverResponse, txnId);
			return serverResponse;*/
			
			
			
			

			/*URL url = new URL(serviceUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty(Constants.CONTENT_TYPE, "application/json");
			//connection.setRequestProperty(Constants.Authorization, authenticationResponseMap.getString("id"));

			JSONObject request = new JSONObject();
			request.put(Constants.TYPE, serviceType);
			request.put(Constants.EMAIL, emailId);
			request.put(Constants.CALLBACK_URL, PropertiesManager.propertiesMap.get(Constants.MPA_CALLBACK_URL));

			String imageUrls[] = imageUrl.split(",");
			if (imageUrls.length > 0 && StringUtils.isNotBlank(imageUrls[0])) {
				JSONArray urlArray = new JSONArray();
				for (int i = 0; i < imageUrls.length; i++) {
					urlArray.put(imageUrls[i]);
				}
				request.put(Constants.IMAGES, urlArray);
			}

			connection.setRequestProperty("Content-Length", request.toString());
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}

			logger.info("Response received from Signzy : " + serverResponse);
			rd.close();
			connection.disconnect();
			return new JSONObject(serverResponse.toString());*/
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
		
		
	}
	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}
}
