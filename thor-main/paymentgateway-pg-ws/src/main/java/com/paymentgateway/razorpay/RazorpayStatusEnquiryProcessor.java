package com.paymentgateway.razorpay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
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
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

@Service
public final class RazorpayStatusEnquiryProcessor {

	@Autowired
	@Qualifier("razorpayTransactionConverter")
	private TransactionConverter converter;

	private static Logger logger = LoggerFactory.getLogger(RazorpayStatusEnquiryProcessor.class.getName());

	public void enquiryProcessor(Fields fields) {

		try {

			String rzrPayOrderId = fields.get(FieldType.AUTH_CODE.getName());
			String response = "";
			response = getResponse(rzrPayOrderId, fields);
			updateFields(fields, response);

		} catch (SystemException exception) {
			logger.error("Exception", exception);
		} catch (Exception e) {
			logger.error("Exception in processing status enquiry response for Razorpay ", e);
		}

	}

	public String getResponse(String request, Fields fields) throws SystemException {

		String hostUrl = "";

		try {

			hostUrl = PropertiesManager.propertiesMap.get(Constants.RAZORPAY_STATUS_ENQ_URL);
			StringBuilder sb = new StringBuilder();
			sb.append(hostUrl);
			sb.append(request);
			sb.append("/payments");

			String userpass = fields.get(FieldType.MERCHANT_ID.getName()) + ":"
					+ fields.get(FieldType.TXN_KEY.getName());
			String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));

			HttpURLConnection httpClient = (HttpURLConnection) new URL(sb.toString()).openConnection();
			httpClient.setRequestMethod("GET");
			httpClient.setRequestProperty("Authorization", basicAuth);
			logger.info("Sending request for status enq to URL " + sb.toString());

			try (BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()))) {

				StringBuilder response = new StringBuilder();
				String line;

				while ((line = in.readLine()) != null) {
					response.append(line);
				}

				if (StringUtils.isBlank(response)) {
					throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR,
							"Network Exception with Razorpay for status request ");
				} else {
					logger.info("Response received for Razorpay status enq " + response.toString());
					return response.toString();
				}

			}

		} catch (Exception e) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"Network Exception with Razorpay for status request ");

		}
	}

	public void updateFields(Fields fields, String response) throws SystemException {

		Transaction transaction = toTransaction(response);

		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;

		if (fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equalsIgnoreCase(TransactionType.SALE.getCode())) {

			if ((StringUtils.isNotBlank(transaction.getCaptured()))
					&& ((transaction.getCaptured()).equalsIgnoreCase(Constants.STATUS_ENQ_SUCCESS_CODE)))

			{
				status = StatusType.CAPTURED.getName();
				errorType = ErrorType.SUCCESS;
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

			}

			else {
				if ((StringUtils.isNotBlank(transaction.getError_code()))) {

					String respCode = null;
					if (StringUtils.isNotBlank(transaction.getError_code())) {
						respCode = transaction.getError_code();
					}

					RazorpayResultType resultInstance = RazorpayResultType.getInstanceFromCode(respCode.toUpperCase());

					if (resultInstance != null) {
						status = resultInstance.getStatusCode();
						errorType = ErrorType.getInstanceFromCode(resultInstance.getiPayCode());

						if (StringUtils.isNotBlank(transaction.getError_description())) {
							pgTxnMsg = transaction.getError_description();
						}

						else {
							pgTxnMsg = resultInstance.getMessage();
						}

					} else {
						status = StatusType.REJECTED.getName();
						errorType = ErrorType.REJECTED;

						if (StringUtils.isNotBlank(transaction.getError_description())) {
							pgTxnMsg = transaction.getError_description();
						}

						else {
							pgTxnMsg = ErrorType.FAILED.toString();
						}

					}

				} else {
					status = StatusType.FAILED_AT_ACQUIRER.getName();
					errorType = ErrorType.FAILED;
					if (StringUtils.isNotBlank(transaction.getError_description())) {
						pgTxnMsg = transaction.getError_description();
					} else {
						pgTxnMsg = ErrorType.FAILED.toString();
					}

				}
			}

			fields.put(FieldType.STATUS.getName(), status);
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

			if (StringUtils.isNotBlank(transaction.getPayment_id())) {
				fields.put(FieldType.RRN.getName(), transaction.getOrder_id());
			}

			if (StringUtils.isNotBlank(transaction.getOrder_id())) {
				fields.put(FieldType.ACQ_ID.getName(), transaction.getPayment_id());
			}

			if (StringUtils.isNotBlank(transaction.getError_code())) {
				fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getError_code());
			} else {
				fields.put(FieldType.PG_RESP_CODE.getName(), errorType.getResponseCode());
			}

			if (StringUtils.isNotBlank(transaction.getStatus())) {
				fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getStatus());
			} else {
				fields.put(FieldType.PG_TXN_STATUS.getName(), errorType.getResponseCode());
			}

			fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);

		}

	}

	public Transaction toTransaction(String jsonResponse) throws SystemException {

		Transaction transaction = new Transaction();

		try {

			if (StringUtils.isBlank(jsonResponse)) {
				logger.warn("Empty response received for Razorpay status enquiry request");
				return transaction;
			}

			JSONObject obj = new JSONObject(jsonResponse);

			if (obj.get("items") != null) {

				JSONArray itemsArr = obj.getJSONArray("items");
				JSONObject itemObj = itemsArr.getJSONObject(0);

				if (itemObj.get("id") != null && StringUtils.isNotBlank(itemObj.get("id").toString())
						&& !itemObj.get("id").toString().equalsIgnoreCase("null")) {
					transaction.setPayment_id(itemObj.get("id").toString());
				}

				if (itemObj.get("status") != null && StringUtils.isNotBlank(itemObj.get("status").toString())
						&& !itemObj.get("status").toString().equalsIgnoreCase("null")) {
					transaction.setStatus(itemObj.get("status").toString());
				}

				if (itemObj.get("order_id") != null && StringUtils.isNotBlank(itemObj.get("order_id").toString())
						&& !itemObj.get("order_id").toString().equalsIgnoreCase("null")) {
					transaction.setOrder_id(itemObj.get("order_id").toString());
				}

				if (itemObj.get("captured") != null && StringUtils.isNotBlank(itemObj.get("captured").toString())
						&& !itemObj.get("captured").toString().equalsIgnoreCase("null")) {
					transaction.setCaptured(itemObj.get("captured").toString());
				}

				if (itemObj.get("error_code") != null && StringUtils.isNotBlank(itemObj.get("error_code").toString())
						&& !itemObj.get("error_code").toString().equalsIgnoreCase("null")) {
					transaction.setError_code(itemObj.get("error_code").toString());
				}

				if (itemObj.get("error_description") != null
						&& StringUtils.isNotBlank(itemObj.get("error_description").toString())
						&& !itemObj.get("error_description").toString().equalsIgnoreCase("null")) {
					transaction.setError_description(itemObj.get("error_description").toString());
				}

				if (itemObj.get("error_reason") != null
						&& StringUtils.isNotBlank(itemObj.get("error_reason").toString())
						&& !itemObj.get("error_reason").toString().equalsIgnoreCase("null")) {
					transaction.setError_reason(itemObj.get("error_reason").toString());
				}

			}

		} catch (Exception e) {
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					ErrorType.INTERNAL_SYSTEM_ERROR.getInternalMessage());
		}

		return transaction;

	}

}
