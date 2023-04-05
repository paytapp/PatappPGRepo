package com.paymentgateway.payout.cashfreePayout;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;

@Service
public class CashfreeCommunicator {

	private static final Logger logger = LoggerFactory.getLogger(CashfreeCommunicator.class);

	@Autowired
	private MongoInstance mongoInstance;

	private static final String prefix = "MONGO_DB_";

	public String communicateForAuthToken(Fields fields, String url) {

		try {

			String xClientId = fields.get(FieldType.ADF4.getName());
			String xClientSecret = fields.get(FieldType.ADF5.getName());

			HttpURLConnection con = null;
			StringBuffer response = new StringBuffer();
			try {

				URL requestUrl = new URL(url);
				con = (HttpURLConnection) requestUrl.openConnection();
				con.setRequestMethod("POST");
				con.setRequestProperty("Content-Type", "application/json");
				con.setRequestProperty("X-Client-Id", xClientId);
				con.setRequestProperty("X-Client-Secret", xClientSecret);

				// For POST only - START
				con.setDoOutput(true);
				con.setUseCaches(false);
				OutputStream os = con.getOutputStream();
				os.flush();
				os.close();

				int responseCode = con.getResponseCode();

				logger.info("Cashfree Auth Req Code == " + responseCode);

				if (responseCode == HttpURLConnection.HTTP_OK) {
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String inputLine;

					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();

					logger.info("Cashfree Auth Token Response " + response);

					return response.toString();

				} else {
					logger.info("Error in Cashfree Auth Token " + con.getResponseCode());
					return null;

				}
			} catch (Exception e) {
				logger.error("Exception in Cashfree Auth Token API == ", e);

				return response.toString();
			} finally {
				if (con != null) {
					con.disconnect();
				}
			}

		} catch (Exception e) {
			logger.error("Exception in getting auth token from Cashfree", e);
		}
		return null;
	}

	public String vpaValidationCashfree(Fields fields, String authTokenNow, String url) {

		HttpsURLConnection connection = null;
		try {

			StringBuilder reqUrl = new StringBuilder();

			reqUrl.append(url);
			reqUrl.append("?vpa=");
			reqUrl.append(fields.get(FieldType.PAYER_ADDRESS.getName()));
			reqUrl.append("&name=");
			reqUrl.append(fields.get(FieldType.PAYER_NAME.getName()).replace(" ", ""));

			StringBuilder serverResponse = new StringBuilder();

			logger.info("Cashfree Payout VPA Validation Request URL " + reqUrl.toString());
			connection = (HttpsURLConnection) new URL(reqUrl.toString()).openConnection();
			connection.setRequestProperty("Authorization", "Bearer " + authTokenNow);
			connection.setRequestMethod("GET");
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
				serverResponse.append('\r');
			}
			rd.close();
			String str = serverResponse.toString();
			logger.info("Cashfree VPA Validation Response " + str);
			return str;

		} catch (Exception e) {
			logger.error("Exception in Cashfree VPA Validation ", e);

			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

	}

	public String verifyAuthToken(String authToken, JSONObject adfFields) {
		String authVerifyUrl = adfFields.getString(Constants.ADF_11);

		HttpURLConnection con = null;
		StringBuffer response = new StringBuffer();
		try {

			URL requestUrl = new URL(authVerifyUrl);
			con = (HttpURLConnection) requestUrl.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Authorization", "Bearer " + authToken);

			// For POST only - START
			con.setDoOutput(true);
			con.setUseCaches(false);
			OutputStream os = con.getOutputStream();
			os.flush();
			os.close();

			int responseCode = con.getResponseCode();

			logger.info("Cashfree verify auth token response code == " + responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				logger.info("Cashfree verify auth token Response " + response);

				return response.toString();

			} else {
				logger.info("Error in Cashfree verify auth token " + con.getResponseCode());
				return null;

			}
		} catch (Exception e) {
			logger.error("Exception in Cashfree verify auth token ", e);

			return response.toString();
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
	}

	public String fetchBene(Fields fields, String authToken, JSONObject adfFields) {

		String beneId = null;
		try {

			String accNo = fields.get(FieldType.BENE_ACCOUNT_NO.getName());
			String ifscCode = fields.get(FieldType.IFSC_CODE.getName());
			String vpa = fields.get(FieldType.PAYER_ADDRESS.getName());
			String payId = fields.get(FieldType.PAY_ID.getName());

			List<BasicDBObject> conditionList = new ArrayList<BasicDBObject>();
			if(StringUtils.isNotBlank(vpa)){
				conditionList.add(new BasicDBObject(FieldType.PAYER_ADDRESS.getName(), vpa));
			}else{
				conditionList.add(new BasicDBObject(FieldType.BENE_ACCOUNT_NO.getName(), accNo));
				conditionList.add(new BasicDBObject(FieldType.IFSC_CODE.getName(), ifscCode));
			}
			conditionList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			conditionList.add(new BasicDBObject(FieldType.USER_TYPE.getName(), "CASHFREE_PAYOUT"));

			BasicDBObject query = new BasicDBObject("$and", conditionList);
			logger.info("Query to fetch Beneficiary : " + query);

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> collection = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + com.paymentgateway.commons.util.Constants.BENE_VERIFICATION_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();

			// Get Bene from DB
			if (cursor.hasNext()) {

				Document dbobj = cursor.next();

				if (dbobj.get(FieldType.BENEFICIARY_CD.getName()) != null) {
					return dbobj.get(FieldType.BENEFICIARY_CD.getName()).toString();
				}

			}

			// Add new Bene
			else {

				beneId = TransactionManager.getNewTransactionId();
				JSONObject beneAddReq = new JSONObject();

				beneAddReq.put("beneId", beneId);
				
				beneAddReq.put("email", adfFields.getString(Constants.ADF_4));
				beneAddReq.put("phone", fields.get(FieldType.PHONE_NO.getName()));
				beneAddReq.put("address1", "Vaishali");
				beneAddReq.put("city", "Ghaziabad");
				beneAddReq.put("state", "Uttar Pradesh");
				beneAddReq.put("pincode", "201014");
				
				if(StringUtils.isNotBlank(vpa)){
					beneAddReq.put("vpa", vpa);
					beneAddReq.put("name", fields.get(FieldType.PAYER_NAME.getName()));
				}else{
					beneAddReq.put("name", fields.get(FieldType.BENE_NAME.getName()));
					beneAddReq.put("bankAccount", accNo);
					beneAddReq.put("ifsc", ifscCode);
				}

				String addBeneUrl = adfFields.getString(Constants.ADF_7);

				HttpURLConnection con = null;
				StringBuffer response = new StringBuffer();

				try {

					URL requestUrl = new URL(addBeneUrl);
					con = (HttpURLConnection) requestUrl.openConnection();
					con.setRequestMethod("POST");
					con.setRequestProperty("Content-Type", "application/json");
					con.setRequestProperty("Authorization", "Bearer " + authToken);

					// For POST only - START
					con.setDoOutput(true);
					con.setUseCaches(false);
					OutputStream os = con.getOutputStream();
					os.write(beneAddReq.toString().getBytes());
					os.flush();
					os.close();

					int responseCode = con.getResponseCode();

					logger.info("Cashfree add bene response code == " + responseCode);

					if (responseCode == HttpURLConnection.HTTP_OK) {
						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String inputLine;

						while ((inputLine = in.readLine()) != null) {
							response.append(inputLine);
						}
						in.close();

						logger.info("Cashfree add bene Response " + response);

						if (StringUtils.isNotBlank(response)) {

							JSONObject resObj = new JSONObject(response.toString());
							if (resObj != null && resObj.get("status") != null && resObj.get("subCode") != null
									&& resObj.get("status").toString().equalsIgnoreCase("SUCCESS")
									&& resObj.get("subCode").toString().equalsIgnoreCase("200")) {

								addNewBeneToDb(beneAddReq, fields);
								return beneId;

							}

						} else {

							fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
							fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
							fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Beneficiary");
							return null;
						}

					} else {
						fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
						fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Beneficiary");
						logger.info("Error in Cashfree add bene " + con.getResponseCode());
						return null;

					}
				} catch (Exception e) {
					logger.error("Exception in Cashfree add bene ", e);

				} finally {
					if (con != null) {
						con.disconnect();
					}
				}
			}

		} catch (Exception e) {
			logger.error("Exception", e);
		}
		return null;
	}

	private void addNewBeneToDb(JSONObject beneAddReq, Fields fields) {
		try {

			Document doc = new Document();

			doc.put(FieldType.BENEFICIARY_CD.getName(), beneAddReq.get("beneId").toString());
			doc.put(FieldType.BENE_NAME.getName(), beneAddReq.get("name").toString());
			doc.put(FieldType.CUST_EMAIL.getName(), beneAddReq.get("email").toString());
			doc.put(FieldType.CUST_PHONE.getName(), beneAddReq.get("phone").toString());
			
			if(beneAddReq.has("vpa") && StringUtils.isNotBlank(beneAddReq.getString("vpa"))){
				doc.put(FieldType.PAYER_ADDRESS.getName(), beneAddReq.get("vpa").toString());
			}else{
				doc.put(FieldType.BENE_ACCOUNT_NO.getName(), beneAddReq.get("bankAccount").toString());
				doc.put(FieldType.IFSC_CODE.getName(), beneAddReq.get("ifsc").toString());
			}
			
			doc.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			doc.put(FieldType.USER_TYPE.getName(), "CASHFREE_PAYOUT");

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + com.paymentgateway.commons.util.Constants.BENE_VERIFICATION_COLLECTION.getValue()));
			collection.insertOne(doc);

		} catch (Exception e) {
			logger.error("Exception ", e);
		}

	}

	public String sendPayout(Fields fields, String authToken, String beneId, JSONObject adfFields) {
		String payoutUrl = adfFields.getString(Constants.ADF_8);

		JSONObject payoutReq = new JSONObject();

		payoutReq.put("beneId", beneId);
		payoutReq.put("amount", fields.get(FieldType.AMOUNT.getName()));
		payoutReq.put("transferId", fields.get(FieldType.TXN_ID.getName()));
		
		if(StringUtils.isNotBlank(fields.get(FieldType.PAYER_ADDRESS.getName()))){
			payoutReq.put("transferMode", "upi");
		}

		logger.info("Cashfree Payout Request is = " + payoutReq.toString());

		HttpURLConnection con = null;
		StringBuffer response = new StringBuffer();
		try {

			URL requestUrl = new URL(payoutUrl);
			con = (HttpURLConnection) requestUrl.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Authorization", "Bearer " + authToken);

			// For POST only - START
			con.setDoOutput(true);
			con.setUseCaches(false);
			OutputStream os = con.getOutputStream();
			os.write(payoutReq.toString().getBytes());
			os.flush();
			os.close();

			int responseCode = con.getResponseCode();

			logger.info("Cashfree Payout response code == " + responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				logger.info("Cashfree Payout Response " + response);

				return response.toString();

			} else {
				logger.info("Error in Cashfree Payout " + con.getResponseCode());
				return null;

			}
		} catch (Exception e) {
			logger.error("Exception in Cashfree Payout ", e);

			return response.toString();
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
	}

	public String getStatus(Fields fields, String authToken, JSONObject adfFields) {
		
		HttpsURLConnection connection = null;
		try {

			String payoutStatusUrl = adfFields.getString(Constants.ADF_9);

			StringBuilder reqUrl = new StringBuilder();

			reqUrl.append(payoutStatusUrl);
			reqUrl.append("?referenceId=");
			reqUrl.append(fields.get(FieldType.ACQ_ID.getName()));
			reqUrl.append("&");
			reqUrl.append("transferId=");
			reqUrl.append(fields.get(FieldType.TXN_ID.getName()));

			StringBuilder serverResponse = new StringBuilder();
			URL url = new URL(reqUrl.toString());

			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestProperty("Authorization", "Bearer " + authToken);
			connection.setRequestMethod("GET");
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
				serverResponse.append('\r');
			}
			rd.close();
			String str = serverResponse.toString();
			logger.info("Cashfree Payout Status Response " + str);
			return str;

		} catch (Exception e) {
			logger.error("Exception in Cashfree Payout Status ", e);

			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

}
