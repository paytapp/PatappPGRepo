package com.paymentgateway.api.cashFree;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.dao.VirtualAccountNumberGeneratorDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.UserStatusType;
import com.paymentgateway.commons.util.VirtualAccountNumberGenerator;

/*
*@auther Vishal Yadav
*/

@Service
public class CashFreeResponseHandler {

	private static Logger logger = LoggerFactory.getLogger(CashFreeResponseHandler.class.getName());
	private static final String prefix = "MONGO_DB_";

		@SuppressWarnings("static-access")
		public  Fields genrateNewTokenResponse(String response , Fields fields) {
			Map<String, String> saveResponse = new HashMap<String, String>();
			try {
				if (response == null) {
					fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
					return fields;
				}
				JSONObject respObj = new JSONObject(response);
				if (respObj.getString("status").equalsIgnoreCase("SUCCESS")
						&& respObj.getString("subCode").equalsIgnoreCase("200")) {
					if (respObj.has("data")) {
						JSONObject respObJ = respObj.getJSONObject("data");
						fields.put(FieldType.TOKEN.getName(),(String) respObJ.getString("token"));
						fields.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
						fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
						fields.put(FieldType.PG_RESP_CODE.getName(),respObj.getString("subCode"));
						fields.put(FieldType.PG_RESPONSE_STATUS.getName(),(String) respObj.getString("status"));
						fields.put(FieldType.PG_RESPONSE_MSG.getName(),(String) respObj.getString("message"));
					
					}
				}else {
					fields.put(FieldType.STATUS.getName(),StatusType.ERROR.getName());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), (String) respObj.getString("message"));
					fields.put(FieldType.RESPONSE_CODE.getName(),(String) respObj.getString("subCode"));
				}
	
			} catch (Exception e) {
				logger.error("Exception in CashFree genrateNewTokenResponse ", e);
				fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			}
	
			return fields;
		}

	
		
		public Map<String, String> genrateVirtualAccountResponse(String response,Fields fields) {
			Map<String, String> saveResponse = new HashMap<String, String>();
			try {
	
				if (response == null) {
					fields.clear();
					saveResponse.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
					saveResponse.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
					saveResponse.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
					return saveResponse;
				}
				// JSONObject responseJSON = new JSONObject(response);
				JSONObject respObj = new JSONObject(response);
				if (respObj.getString("status").equalsIgnoreCase("SUCCESS")&& respObj.getString("subCode").equalsIgnoreCase("200")) {
					if (respObj.has("data")) {
						
						JSONObject respOBJ = respObj.getJSONObject("data");
						fields.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(),(String) respOBJ.getString("accountNumber"));
						fields.put(FieldType.IFSC_CODE.getName(),(String) respOBJ.getString("ifsc"));
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
						fields.put(FieldType.PG_RESP_CODE.getName(),respObj.getString("subCode"));
						fields.put(FieldType.PG_RESPONSE_STATUS.getName(),(String) respObj.getString("status"));
						fields.put(FieldType.PG_RESPONSE_MSG.getName(),(String) respObj.getString("message"));
					
						saveResponse.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(),(String) respOBJ.getString("accountNumber"));
						saveResponse.put(FieldType.IFSC_CODE.getName(),(String) respOBJ.getString("ifsc"));
						saveResponse.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
						saveResponse.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
						saveResponse.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
					}
				}else {
					fields.clear();
					saveResponse.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
					saveResponse.put(FieldType.PG_RESPONSE_STATUS.getName(),(String) respObj.getString("status"));
					saveResponse.put(FieldType.PG_RESPONSE_MSG.getName(),(String) respObj.getString("message"));
					saveResponse.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
				}
	
			} catch (Exception e) {
				fields.clear();
				logger.error("Exception in CashFree genrateNewTokenResponse ", e);
				saveResponse.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
				saveResponse.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
				saveResponse.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			}
	
			return saveResponse;
		}
		
		public Map<String, String>  genrateVpaResponse(String response,Fields fields) {
			Map<String, String> saveResponse = new HashMap<String, String>();
			try {
				if (response == null) {
					fields.clear();
					saveResponse.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
					saveResponse.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
					saveResponse.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
					return saveResponse;
				}
				JSONObject respObj = new JSONObject(response);
				if (respObj.getString("status").equalsIgnoreCase("SUCCESS")&& respObj.getString("subCode").equalsIgnoreCase("200")) {
					if (respObj.has("data")) {
						JSONObject respOBJ = respObj.getJSONObject("data");
						fields.put(FieldType.VPA.getName(), (String) respOBJ.getString("vpa"));
						fields.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
						fields.put(FieldType.PG_RESP_CODE.getName(),respObj.getString("subCode"));
						fields.put(FieldType.PG_RESPONSE_STATUS.getName(),(String) respObj.getString("status"));
						fields.put(FieldType.PG_RESPONSE_MSG.getName(),(String) respObj.getString("message"));
						saveResponse.put(FieldType.VPA.getName(), (String) respOBJ.getString("vpa"));
						saveResponse.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
						saveResponse.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
						saveResponse.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
						
					}
				}else {
					fields.clear();
					saveResponse.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
					saveResponse.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
					saveResponse.put(FieldType.PG_RESPONSE_MSG.getName(),(String) respObj.getString("message"));
				}
	
			} catch (Exception e) {
				fields.clear();
				logger.error("Exception in CashFree genrateNewTokenResponse ", e);
				saveResponse.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
				saveResponse.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
				saveResponse.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			}
	
			return saveResponse;
		}

		
		public Map<String, String> qrCodeForExistingResponse(String response,Fields fields) {
			
			Map<String, String> saveResponse = new HashMap<String, String>();
			try {
				if (response == null) {
					fields.clear();
					saveResponse.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
					saveResponse.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
					saveResponse.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
					return saveResponse;
				}
				JSONObject respObj = new JSONObject(response);
				if (respObj.getString("status").equalsIgnoreCase("SUCCESS")
						&& respObj.getString("subCode").equalsIgnoreCase("200")) {
					fields.put(FieldType.UPI_QR_CODE.getName(),(String) respObj.get("qrCode"));
					fields.put(FieldType.PG_RESP_CODE.getName(),respObj.getString("subCode"));
					fields.put(FieldType.PG_RESPONSE_STATUS.getName(),(String) respObj.getString("status"));
					fields.put(FieldType.PG_RESPONSE_MSG.getName(),(String) respObj.getString("message"));
					saveResponse.put(FieldType.VIRTUAL_ACC_NUM.getName(),fields.get(FieldType.VIRTUAL_ACC_NUM.getName()));
					saveResponse.put(FieldType.VIRTUAL_VPA_NUM.getName(),fields.get(FieldType.VIRTUAL_VPA_NUM.getName()));
					saveResponse.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(),fields.get(FieldType.CUSTOMER_ACCOUNT_NO.getName()));
					saveResponse.put(FieldType.IFSC_CODE.getName(),fields.get(FieldType.IFSC_CODE.getName()));
					saveResponse.put(FieldType.VPA.getName(), fields.get(FieldType.VPA.getName()) );
					saveResponse.put(FieldType.UPI_QR_CODE.getName(),(String) respObj.get("qrCode"));
					saveResponse.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
					saveResponse.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
					saveResponse.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
					
				}
			} catch (Exception e) {
				fields.clear();
				logger.error("Exception in CashFree genrateNewTokenResponse ", e);
				saveResponse.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
				saveResponse.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
				saveResponse.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			}
	
			return saveResponse;
		}
		
		public void updateVAStatusResponseHandler(String response,Fields fields) {
			
			try {
	
				if (response == null) {
					fields.clear();
					fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
					return ;
				}
				// JSONObject responseJSON = new JSONObject(response);
				JSONObject respObj = new JSONObject(response);
				if (respObj.getString("status").equalsIgnoreCase("SUCCESS")&& respObj.getString("subCode").equalsIgnoreCase("200")) {
					
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
						fields.put(FieldType.PG_RESP_CODE.getName(),respObj.getString("subCode"));
						fields.put(FieldType.PG_RESPONSE_STATUS.getName(),(String) respObj.getString("status"));
						fields.put(FieldType.PG_RESPONSE_MSG.getName(),(String) respObj.getString("message"));
					
						fields.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
						fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
				}else {
					fields.clear();
					fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
					fields.put(FieldType.PG_RESPONSE_STATUS.getName(),(String) respObj.getString("status"));
					fields.put(FieldType.PG_RESPONSE_MSG.getName(),(String) respObj.getString("message"));
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
				}
	
			} catch (Exception e) {
				fields.clear();
				logger.error("Exception in CashFree genrateNewTokenResponse ", e);
				fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			}
		}
		
			
}
