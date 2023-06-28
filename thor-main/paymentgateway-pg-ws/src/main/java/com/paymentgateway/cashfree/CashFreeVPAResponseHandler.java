package com.paymentgateway.cashfree;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

/*
*@auther Vishal Yadav
*/

@Service
public class CashFreeVPAResponseHandler {

	private static Logger logger = LoggerFactory.getLogger(CashFreeVPAResponseHandler.class.getName());
	private static final String prefix = "MONGO_DB_";

	@SuppressWarnings("static-access")
	public Fields genrateNewTokenResponse(String response, Fields fields) {
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
					fields.put(FieldType.TOKEN.getName(), (String) respObJ.getString("token"));
					// fields.put(FieldType.EXPIRY_TIME.getName(),(String)
					// respObJ.getInt("expiry"));
					fields.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
					fields.put(FieldType.PG_RESP_CODE.getName(), respObj.getString("subCode"));
					fields.put(FieldType.PG_RESPONSE_STATUS.getName(), (String) respObj.getString("status"));
					fields.put(FieldType.PG_RESPONSE_MSG.getName(), (String) respObj.getString("message"));

				}
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), (String) respObj.getString("message"));
				fields.put(FieldType.RESPONSE_CODE.getName(), (String) respObj.getString("subCode"));
			}

		} catch (Exception e) {
			logger.error("Exception in CashFree genrateNewTokenResponse ", e);
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
		}

		return fields;
	}

	public Map<String, String> genrateVirtualAccountResponse(String response, Fields fields) {
		Map<String, String> saveResponse = new HashMap<String, String>();
		try {

			if (response == null) {
				fields.clear();
				saveResponse.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
				saveResponse.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.VALIDATION_FAILED.getResponseMessage());
				saveResponse.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
				return saveResponse;
			}
			// JSONObject responseJSON = new JSONObject(response);
			JSONObject respObj = new JSONObject(response);
			if (respObj.getString("status").equalsIgnoreCase("SUCCESS")
					&& respObj.getString("subCode").equalsIgnoreCase("200")) {
				if (respObj.has("data")) {

					JSONObject respOBJ = respObj.getJSONObject("data");
					fields.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(), (String) respOBJ.getString("accountNumber"));
					fields.put(FieldType.IFSC_CODE.getName(), (String) respOBJ.getString("ifsc"));
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
					fields.put(FieldType.PG_RESP_CODE.getName(), respObj.getString("subCode"));
					fields.put(FieldType.PG_RESPONSE_STATUS.getName(), (String) respObj.getString("status"));
					fields.put(FieldType.PG_RESPONSE_MSG.getName(), (String) respObj.getString("message"));

					saveResponse.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(),
							(String) respOBJ.getString("accountNumber"));
					saveResponse.put(FieldType.IFSC_CODE.getName(), (String) respOBJ.getString("ifsc"));
					saveResponse.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
					saveResponse.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
					saveResponse.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
				}
			} else {
				fields.clear();
				saveResponse.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
				saveResponse.put(FieldType.PG_RESPONSE_STATUS.getName(), (String) respObj.getString("status"));
				saveResponse.put(FieldType.PG_RESPONSE_MSG.getName(), (String) respObj.getString("message"));
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

	public Map<String, String> genrateVpaResponse(String response, Fields fields) {
		Map<String, String> saveResponse = new HashMap<String, String>();
		try {
			if (response == null) {
				fields.clear();
				saveResponse.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
				saveResponse.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.VALIDATION_FAILED.getResponseMessage());
				saveResponse.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
				return saveResponse;
			}
			JSONObject respObj = new JSONObject(response);
			if (respObj.getString("status").equalsIgnoreCase("SUCCESS")
					&& respObj.getString("subCode").equalsIgnoreCase("200")) {
				if (respObj.has("data")) {
					JSONObject respOBJ = respObj.getJSONObject("data");
					fields.put(FieldType.VPA.getName(), (String) respOBJ.getString("vpa"));
					fields.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
					fields.put(FieldType.PG_RESP_CODE.getName(), respObj.getString("subCode"));
					fields.put(FieldType.PG_RESPONSE_STATUS.getName(), (String) respObj.getString("status"));
					fields.put(FieldType.PG_RESPONSE_MSG.getName(), (String) respObj.getString("message"));
					saveResponse.put(FieldType.VPA.getName(), (String) respOBJ.getString("vpa"));
					saveResponse.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
					saveResponse.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
					saveResponse.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());

				}
			} else {
				fields.clear();
				saveResponse.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
				saveResponse.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
				saveResponse.put(FieldType.PG_RESPONSE_MSG.getName(), (String) respObj.getString("message"));
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

	public Map<String, String> qrCodeForExistingResponse(String response, Fields fields) {

		Map<String, String> saveResponse = new HashMap<String, String>();
		try {
			if (response == null) {
				fields.clear();
				saveResponse.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
				saveResponse.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.VALIDATION_FAILED.getResponseMessage());
				saveResponse.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
				return saveResponse;
			}
			JSONObject respObj = new JSONObject(response);
			if (respObj.getString("status").equalsIgnoreCase("SUCCESS")
					&& respObj.getString("subCode").equalsIgnoreCase("200")) {
				logger.info(
						"UPI QR generated successfully !! for cust ID >> " + fields.get(FieldType.CUST_ID.getName()));
				String pngImage = (String) respObj.get("qrCode").toString();
				pngImage = pngImage.replace("data:image/png;base64,", "");

				fields.put(FieldType.UPI_QR_CODE.getName(), (String) pngImage);
				fields.put(FieldType.PG_RESP_CODE.getName(), respObj.getString("subCode"));
				fields.put(FieldType.PG_RESPONSE_STATUS.getName(), (String) respObj.getString("status"));
				fields.put(FieldType.PG_RESPONSE_MSG.getName(), (String) respObj.getString("message"));
				saveResponse.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(),
						fields.get(FieldType.CUSTOMER_ACCOUNT_NO.getName()));
				saveResponse.put(FieldType.CUST_ID.getName(), fields.get(FieldType.CUST_ID.getName()));
				saveResponse.put(FieldType.VPA.getName(), fields.get(FieldType.VPA.getName()));
				saveResponse.put(FieldType.UPI_QR_CODE.getName(), pngImage);
				saveResponse.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
				saveResponse.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
				saveResponse.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
				saveResponse.put(FieldType.COMPANY_NAME.getName(), fields.get(FieldType.COMPANY_NAME.getName()));

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
