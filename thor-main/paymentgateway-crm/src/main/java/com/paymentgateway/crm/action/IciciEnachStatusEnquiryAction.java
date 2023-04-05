package com.paymentgateway.crm.action;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;

/**
 * 
 */
public class IciciEnachStatusEnquiryAction extends AbstractSecureAction {

	
	/*@Autowired
	private IciciEnachMandateEnquiryProcessor iciciEnachMandateEnquiryProcessor;*/
	
	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;
	
	private static final long serialVersionUID = 6371292597285420796L;
	private static Logger logger = LoggerFactory.getLogger(IciciEnachStatusEnquiryAction.class.getName());
	
	private String orderId;
	private String payId;
	private String subMerchantPayId;
	private String amount;
	private String hash;

	public String execute() {
		//ICICI_ENACH_STATUS_ENQUIRY
	
		logger.info("inside IciciEnachStatusEnquiryAction for registration status enqiry ");
		try {
		Fields fields = new Fields();
		fields.put(FieldType.PAY_ID.getName(), payId);
		fields.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId);
		fields.put(FieldType.ORDER_ID.getName(), orderId);
		fields.put(FieldType.AMOUNT.getName(), amount);
		fields.put(FieldType.HASH.getName(), hash);
		
		
		Map<String, String> response = transactionControllerServiceProvider.transact(fields, Constants.ICICI_ENACH_STATUS_ENQUIRY.getValue());
		
		logger.info("registration status enquiry API  response received from pg ws " + response);
		if(response.isEmpty()) {
			logger.info("registration status enquiry not initiated !!");
		} else {
			//logger.info("registration status enquiry Successfully !!, Response Status is :" + response.get(FieldType.STATUS.getName()));
		}
		 /*String hostUrl = propertiesManager.propertiesMap.get(Constants.ICICI_ENACH_STATUS_ENQUIRY.getValue());
		 	String responseBody = "";
			Map<String, String> resMap = new HashMap<String, String>();
		 
		 JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(fields.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, fields.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(hostUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);*/
		 
		/* URL url = new URL(hostUrl);
				int timeout = 20000;
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(timeout);
				conn.setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setRequestProperty("Content-Type", "application/json");
				conn.setRequestProperty("Accept", "application/json");

				OutputStream os = conn.getOutputStream();
				os.write(fields.toString().getBytes());
				os.flush();
				
				logger.info("eNach Registration status enquiry Request : " + fields.toString());
				
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
				
				 JSONObject response = new JSONObject(serverResponse);
				logger.info("Refund Communicator Response : " + serverResponse);
				return serverResponse;*/
		
		
		
		
		} catch (Exception ex) {
			logger.info("exception caught while registration status "+ex);
			
		}
		
		return SUCCESS;
	}
	
	
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}
	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}
}
