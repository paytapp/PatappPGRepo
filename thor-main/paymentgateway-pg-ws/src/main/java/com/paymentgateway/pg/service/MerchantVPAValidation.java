package com.paymentgateway.pg.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.UserStatusType;

@RestController
@CrossOrigin
public class MerchantVPAValidation {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private UserDao userDao;

	private static Logger logger = LoggerFactory.getLogger(MerchantVPAValidation.class.getName());

	public static final String XML_OPEN_TAG = "<XML>";
	public static final String XML_CLOSE_TAG = "</XML>";
	public static final String CustName_OPEN_TAG = "<CustName>";
	public static final String CustName_CLOSE_TAG = "</CustName>";
	public static final String ActCode_OPEN_TAG = "<ActCode>";
	public static final String ActCode_CLOSE_TAG = "</ActCode>";
	public static final String Message_OPEN_TAG = "<Message>";
	public static final String Message_CLOSE_TAG = "</Message>";
	public static final String TxnId_OPEN_TAG = "<TxnId>";
	public static final String TxnId_CLOSE_TAG = "</TxnId>";

	public static final String Source_OPEN_TAG = "<Source>";
	public static final String Source_CLOSE_TAG = "</Source>";
	public static final String SubscriberId_OPEN_TAG = "<SubscriberId>";
	public static final String SubscriberId_CLOSE_TAG = "</SubscriberId>";
	public static final String MerchantKey_OPEN_TAG = "<MerchantKey>";
	public static final String MerchantKey_CLOSE_TAG = "</MerchantKey>";

	@RequestMapping(method = RequestMethod.POST, value = "/vpaValidate", consumes = MediaType.TEXT_XML_VALUE, produces = MediaType.TEXT_XML_VALUE)
	public @ResponseBody String vpaValidate(@RequestBody String reqmap) {

		StringBuilder xmlResponse = new StringBuilder();
		try {

			logger.info("Request Received for Merchant VPA Validation " + reqmap);

			String source = null;
			String subscriberId = null;
			String txnId = null;
			String merchantKey = null;

			if (reqmap.contains("Source")) {
				source = getTextBetweenTags(reqmap, Source_OPEN_TAG, Source_CLOSE_TAG);
			}
			if (reqmap.contains("SubscriberId")) {
				subscriberId = getTextBetweenTags(reqmap, SubscriberId_OPEN_TAG, SubscriberId_CLOSE_TAG);
			}
			if (reqmap.contains("TxnId")) {
				txnId = getTextBetweenTags(reqmap, TxnId_OPEN_TAG, TxnId_CLOSE_TAG);
			}
			if (reqmap.contains("MerchantKey")) {
				merchantKey = getTextBetweenTags(reqmap, MerchantKey_OPEN_TAG, MerchantKey_CLOSE_TAG);
			}

			MongoDatabase dbIns = null;
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get("MONGO_DB_" + Constants.CUST_QR_COLLECTION.getValue()));

			List<BasicDBObject> vpaQuery = new ArrayList<BasicDBObject>();

			vpaQuery.add(new BasicDBObject("CUSTOMER_ID", subscriberId));
			vpaQuery.add(new BasicDBObject("STATUS", "Active"));

			BasicDBObject finalQuery = new BasicDBObject("$and", vpaQuery);
			String businessName = null;
			String payId = null;
			String accountId = null;

			long count = collection.countDocuments(finalQuery);

			if (count > 0) {
				MongoCursor<Document> cursor = collection.find(finalQuery).iterator();
				while (cursor.hasNext()) {
					Document doc = cursor.next();
					businessName = doc.get("COMPANY_NAME").toString();
					payId = doc.get("PAY_ID").toString();
					accountId = doc.get("CUSTOMER_ACCOUNT_NO").toString();
					break;
				}
			}
			
			
			// Validate if Merchant is Active
			
			if (StringUtils.isNotBlank(payId)) {
				
				User user = userDao.findPayId(payId);
				
				if (user == null || !user.getUserStatus().equals(UserStatusType.ACTIVE)) {
					
					logger.info("User Status is not active, sending failed response to bank , Pay Id = " + payId);
					
					xmlResponse.append(XML_OPEN_TAG);
					xmlResponse.append(Message_OPEN_TAG);
					xmlResponse.append("INVALID");
					xmlResponse.append(Message_CLOSE_TAG);
					xmlResponse.append(ActCode_OPEN_TAG);
					xmlResponse.append("1");
					xmlResponse.append(ActCode_CLOSE_TAG);
					xmlResponse.append(XML_CLOSE_TAG);
					
					logger.info("Sending response for VPA validation to Bank " + xmlResponse.toString());
				}
				
			}
			

			// Validation For Satin Load Id
			
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("SATIN_USE_ACC_VALIDATION")) && 
					propertiesManager.propertiesMap.get("SATIN_USE_ACC_VALIDATION").equalsIgnoreCase("Y")) {
				
				String satinPayId = propertiesManager.propertiesMap.get("SATIN_CREDITCARE_PAY_ID");
				if (StringUtils.isNotBlank(payId) && StringUtils.isNotBlank(accountId) && StringUtils.isNotBlank(satinPayId)
						&& payId.equalsIgnoreCase(satinPayId)) {

					String response = "false";
					String loadId = accountId.split("/")[0];
					logger.info("Validating load account id for Satin account number >> " + loadId);
					response = checkSatinLoanAcc(loadId);

					if (!response.equalsIgnoreCase("true")) {
						logger.info(
								"Satin loan Account validation not allowing transaction ,sending failure for Static QR validation to bank , load Id = "+ loadId);

						xmlResponse.append(XML_OPEN_TAG);
						xmlResponse.append(Message_OPEN_TAG);
						xmlResponse.append("INVALID");
						xmlResponse.append(Message_CLOSE_TAG);
						xmlResponse.append(ActCode_OPEN_TAG);
						xmlResponse.append("1");
						xmlResponse.append(ActCode_CLOSE_TAG);
						xmlResponse.append(XML_CLOSE_TAG);
						
						logger.info("Sending response for VPA validation to Bank " + xmlResponse.toString());
						
						return xmlResponse.toString();
					}

				}
				
			}
			

			if (count > 0 && StringUtils.isNotBlank(businessName)) {
				xmlResponse.append(XML_OPEN_TAG);
				xmlResponse.append(CustName_OPEN_TAG);
				xmlResponse.append(businessName);
				xmlResponse.append(CustName_CLOSE_TAG);
				xmlResponse.append(ActCode_OPEN_TAG);
				xmlResponse.append("0");
				xmlResponse.append(ActCode_CLOSE_TAG);
				xmlResponse.append(Message_OPEN_TAG);
				xmlResponse.append("VALID");
				xmlResponse.append(Message_CLOSE_TAG);
				xmlResponse.append(TxnId_OPEN_TAG);
				xmlResponse.append(txnId);
				xmlResponse.append(TxnId_CLOSE_TAG);
				xmlResponse.append(XML_CLOSE_TAG);

				logger.info("Sending response for VPA validation to Bank " + xmlResponse.toString());
				
				return xmlResponse.toString();
			} else {

				xmlResponse.append(XML_OPEN_TAG);
				xmlResponse.append(Message_OPEN_TAG);
				xmlResponse.append("INVALID");
				xmlResponse.append(Message_CLOSE_TAG);
				xmlResponse.append(ActCode_OPEN_TAG);
				xmlResponse.append("1");
				xmlResponse.append(ActCode_CLOSE_TAG);
				xmlResponse.append(XML_CLOSE_TAG);
				
				logger.info("Sending response for VPA validation to Bank " + xmlResponse.toString());
				
				return xmlResponse.toString();
			}

		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception in merchant VPA validation API", exception);

			xmlResponse.append(XML_OPEN_TAG);
			xmlResponse.append(Message_OPEN_TAG);
			xmlResponse.append("INVALID");
			xmlResponse.append(Message_CLOSE_TAG);
			xmlResponse.append(ActCode_OPEN_TAG);
			xmlResponse.append("1");
			xmlResponse.append(ActCode_CLOSE_TAG);
			xmlResponse.append(XML_CLOSE_TAG);
			
			logger.info("Sending response for VPA validation to Bank " + xmlResponse.toString());
			return xmlResponse.toString();
		}

	}

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

	public String checkSatinLoanAcc(String loadId) {

		String result = "false";
		try {

			String accValUrlSatin = propertiesManager.propertiesMap.get("SATIN_CREDITCARE_ACC_VAL_URL");
			if (StringUtils.isBlank(accValUrlSatin)) {
				return "false";
			}

			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet request = new HttpGet(accValUrlSatin + loadId);

			// add request headers
			request.addHeader("x-lms-access-key-id",
					propertiesManager.propertiesMap.get("SATIN_CREDITCARE_HEADER_VALUE_1"));
			request.addHeader("x-lms-secret-access-key",
					propertiesManager.propertiesMap.get("SATIN_CREDITCARE_HEADER_VALUE_2"));
			request.addHeader("vendorName", propertiesManager.propertiesMap.get("SATIN_CREDITCARE_HEADER_VALUE_3"));

			try (CloseableHttpResponse response = httpClient.execute(request)) {

				HttpEntity entity = response.getEntity();

				if (entity != null) {
					result = EntityUtils.toString(entity);
					logger.info("Response received from Satin for load id validation >>> " + result);
				}

				return result;
			}

		}

		catch (Exception e) {
			logger.error("Exception in Satin Load Account Id Validation for load Id "+loadId+" >>  ", e);
			return "false";
		}

	}

}
