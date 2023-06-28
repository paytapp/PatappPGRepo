package com.paymentgateway.P2PTSP;

import java.io.IOException;
import java.net.URL;

import org.apache.http.protocol.RequestDate;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class P2PTSPService {

	private static Logger logger = LoggerFactory.getLogger(P2PTSPService.class.getName());

	public Fields getQrCode(Fields fields) throws IOException, SystemException {
		String request = createQrCodeRequest(fields);
		String response = requestForQR(request, fields);
		updateQRResponse(response, fields);
		clearFields(fields);
		return fields;
	}

	public Fields clearFields(Fields fields) {
		fields.remove(FieldType.PASSWORD.getName());
		fields.remove(FieldType.TXN_KEY.getName());
		fields.remove(FieldType.MERCHANT_ID.getName());
		return fields;
	}

	public String createQrCodeRequest(Fields fields) throws IOException {
		JSONObject requestJson = new JSONObject();

		requestJson.put("SecurityID", fields.get(FieldType.PASSWORD.getName()));
		requestJson.put("trackId", fields.get(FieldType.PG_REF_NUM.getName()));
		requestJson.put("currency", "INR");
		requestJson.put("amount", fields.get(FieldType.TOTAL_AMOUNT.getName()));

		logger.info("P2PTSP MQR Initiated Request :- " + requestJson.toString());
		return requestJson.toString();
	}

	public String requestForQR(String data, Fields fields) throws IOException, SystemException {
		URL url = new URL(PropertiesManager.propertiesMap.get("P2PTSPINITITATEURL"));
		try {
			OkHttpClient client = new OkHttpClient().newBuilder().build();
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType, data);
			Request request = new Request.Builder().url(url).method("POST", body)
					.addHeader("merchantId", fields.get(FieldType.MERCHANT_ID.getName()))
					.addHeader("verificationToken", fields.get(FieldType.TXN_KEY.getName()))
					.addHeader("Content-Type", "application/json").build();
			Response response = client.newCall(request).execute();
			String reposneData = response.body().string();
			return reposneData;
		} catch (Exception e) {
			logger.error("Network Exception with P2PTSP >>> ", e);
			MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"Network Exception with P2PTSP " + url.toString());
		}
	}

	public Fields updateQRResponse(String response, Fields fields) {
		logger.info("P2PTSP MQR Initiated Response :- " + response);

		try {
			JSONObject responsData = new JSONObject(response);
			if (responsData.has("response_code") && responsData.getString("response_code").equalsIgnoreCase("000")) {
				if (responsData.has("upi_qr")) {
					fields.put(FieldType.MQR_QR_CODE.getName(), responsData.getString("upi_qr"));
					fields.put(FieldType.VPA.getName(), responsData.getString("upi_vpa"));
					fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
					fields.put(FieldType.PG_TXN_STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
					fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());

				}
			} else {
				if (responsData.has("response_code")
						&& responsData.getString("response_code").equalsIgnoreCase("001")) {
					fields.put(FieldType.STATUS.getName(), StatusType.DECLINED.getName());
					fields.put(FieldType.PG_RESP_CODE.getName(), responsData.getString("RESPONSE_CODE"));
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), responsData.getString("RESPONSE_MESSAGE"));
					fields.put(FieldType.PG_TXN_STATUS.getName(), ErrorType.DECLINED.getResponseCode());
				} else {
					fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
					fields.put(FieldType.PG_TXN_STATUS.getName(), StatusType.FAILED.getName());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());
					fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.FAILED.getResponseCode());
				}
			}
		} catch (Exception e) {
			logger.error("Expention in setu response token", e);
		}
		return fields;
	}

	public void enquiryProcessor(Fields fields) throws IOException {
		String request = statusEnquiryRequest(fields);
		String response = "";
		try {
			response = getResponse(request, fields);
			updateFields(fields, response);
		} catch (SystemException exception) {
			logger.error("Exception", exception);
		}
	}

	public String statusEnquiryRequest(Fields fields) throws IOException {
		JSONObject requestJson = new JSONObject();

		requestJson.put("SecurityID", fields.get(FieldType.PASSWORD.getName()));
		requestJson.put("trackId", fields.get(FieldType.PG_REF_NUM.getName()));
		requestJson.put("amount", Amount.formatAmount(fields.get(FieldType.TOTAL_AMOUNT.getName()),
				fields.get(FieldType.CURRENCY_CODE.getName())));

		logger.info("P2PTSP Status Enquiry Request :- " + requestJson.toString());
		return requestJson.toString();
	}

	public String getResponse(String data, Fields fields) throws IOException, SystemException {
		URL url = new URL(PropertiesManager.propertiesMap.get("P2PTSPSTATUSENQUIRYURL"));
		try {
			OkHttpClient client = new OkHttpClient().newBuilder().build();
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType, data);
			Request request = new Request.Builder().url(url).method("POST", body)
					.addHeader("merchantId", fields.get(FieldType.MERCHANT_ID.getName()))
					.addHeader("verificationToken", fields.get(FieldType.TXN_KEY.getName()))
					.addHeader("Content-Type", "application/json").build();
			Response response = client.newCall(request).execute();
			String reposneData = response.body().string();

			logger.info("P2PTSP Status Enquiry response :- " + reposneData);
			return reposneData;
		} catch (Exception e) {
			logger.error("Network Exception with P2PTSP >>> ", e);
			MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"Network Exception with P2PTSP " + url.toString());
		}
	}

	public void updateFields(Fields fields, String response) {

		JSONObject responseData = new JSONObject(response);
		if (responseData.has("trackId") && responseData.has("status")) {
			if (responseData.get("status").toString().equalsIgnoreCase("approved")) {
				fields.put(FieldType.RRN.getName(), responseData.get("utr").toString());
				fields.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
			} else if (responseData.get("status").toString().equalsIgnoreCase("initiated")
					|| responseData.get("status").toString().equalsIgnoreCase("processing")) {

			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());
			}
		}
	}
}
