package com.paymentgateway.vepay;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.PayGateCryptoUtils;

@Service
public class VepayStatusEnquiryProcessor {

	@Autowired
	@Qualifier("vepayTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	private PayGateCryptoUtils payGateCryptoUtils;

	private static Logger logger = LoggerFactory.getLogger(VepayStatusEnquiryProcessor.class.getName());

	public void enquiryProcessor(Fields fields) {
		String request = statusEnquiryRequest(fields);
		String response = "";
		try {

			response = getResponse(request);
			updateFields(fields, response);

		} catch (SystemException exception) {
			logger.error("Exception", exception);
		} catch (Exception e) {
			logger.error("Exception in decrypting status enquiry response for vepay ", e);
		}

	}

	public String statusEnquiryRequest(Fields fields) {

		try {

			JsonObject refRequest = new JsonObject();

			refRequest.addProperty("ag_id", fields.get(FieldType.ADF1.getName()).toString());
			refRequest.addProperty("me_id", fields.get(FieldType.MERCHANT_ID.getName()).toString());
			refRequest.addProperty("order_no",
					payGateCryptoUtils.encrypt(fields.get(FieldType.PG_REF_NUM.getName()).toString(),
							fields.get(FieldType.TXN_KEY.getName()).toString()));

			refRequest.addProperty("ag_ref", "");

			return refRequest.toString();

		}

		catch (Exception e) {
			logger.error("Exception in preparing Vepay Status Enquiry Request", e);
			return null;
		}

	}

	public static String getResponse(String request) throws SystemException {

		String hostUrl = "";

		Fields fields = new Fields();
		fields.put(FieldType.TXNTYPE.getName(), "STATUS");
		try {

			hostUrl = PropertiesManager.propertiesMap.get(Constants.VEPAY_STATUS_ENQ_URL);

			URL url = new URL(hostUrl);
			URLConnection connection = null;
			connection = url.openConnection();
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			DataOutputStream dataoutputstream = new DataOutputStream(connection.getOutputStream());
			dataoutputstream.writeBytes(request);
			dataoutputstream.flush();
			dataoutputstream.close();
			BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String decodedString;
			String response = "";
			while ((decodedString = bufferedreader.readLine()) != null) {
				response = response + decodedString;
			}

			return response;
		} catch (IOException ioException) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, ioException,
					"Network Exception with Vepay for status enquiry request " + hostUrl.toString());
		}

	}

	public void updateFields(Fields fields, String response) {

		Transaction transaction = new Transaction();

		transaction = toTransaction(payGateCryptoUtils.decrypt(response, fields.get(FieldType.TXN_KEY.getName())),
				fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()), fields.get(FieldType.PG_REF_NUM.getName()));

		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;

		/*
		 * if (fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equalsIgnoreCase(
		 * TransactionType.SALE.getCode())) {
		 * 
		 * if ((StringUtils.isNotBlank(transaction.getRes_code())) &&
		 * (StringUtils.isNotBlank(transaction.getStatus())) &&
		 * ((transaction.getRes_code()).equalsIgnoreCase(Constants.
		 * STATUS_ENQ_SUCCESS_CODE)) &&
		 * ((transaction.getStatus()).equalsIgnoreCase(Constants.SUCCESS_STATUS)))
		 * 
		 * { status = StatusType.CAPTURED.getName(); errorType = ErrorType.SUCCESS;
		 */

				/*
				 * if (StringUtils.isNotBlank(transaction.getRes_message())) { pgTxnMsg =
				 * transaction.getRes_message(); }
				 * 
				 * else { pgTxnMsg = ErrorType.SUCCESS.getResponseMessage(); }
				 */

			//}

			/*
			 * else { if ((StringUtils.isNotBlank(transaction.getRes_code()))) {
			 * 
			 * String respCode = null; if
			 * (StringUtils.isNotBlank(transaction.getRes_code())) { respCode =
			 * transaction.getRes_code(); }
			 * 
			 * VepayResultType resultInstance =
			 * VepayResultType.getInstanceFromName(respCode);
			 * 
			 * if (resultInstance != null) { status = resultInstance.getStatusCode();
			 * errorType = ErrorType.getInstanceFromCode(resultInstance.getiPayCode());
			 * 
			 * if (StringUtils.isNotBlank(transaction.getRes_message())) { pgTxnMsg =
			 * transaction.getRes_message(); }
			 * 
			 * else { pgTxnMsg = resultInstance.getMessage(); }
			 * 
			 * } else { status = StatusType.FAILED_AT_ACQUIRER.getName(); errorType =
			 * ErrorType.FAILED;
			 * 
			 * if (StringUtils.isNotBlank(transaction.getRes_message())) { pgTxnMsg =
			 * transaction.getRes_message(); }
			 * 
			 * else { pgTxnMsg = ErrorType.FAILED.toString(); }
			 * 
			 * }
			 * 
			 * } else { status = StatusType.FAILED_AT_ACQUIRER.getName(); errorType =
			 * ErrorType.FAILED; if (StringUtils.isNotBlank(transaction.getRes_message())) {
			 * pgTxnMsg = transaction.getRes_message(); } else { pgTxnMsg =
			 * ErrorType.FAILED.toString(); }
			 * 
			 * } }
			 */

			fields.put(FieldType.STATUS.getName(), status);
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

			/*
			 * fields.put(FieldType.ACQ_ID.getName(), transaction.getPg_ref());
			 * fields.put(FieldType.RRN.getName(), transaction.getAg_ref());
			 * fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getRes_code());
			 */

			if (StringUtils.isNotBlank(transaction.getStatus())) {
				fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getStatus());
			} else {
				fields.put(FieldType.PG_TXN_STATUS.getName(), errorType.getResponseCode());
			}

			fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);

		}

	//}

	public Transaction toTransaction(String jsonResponse, String txnType, String order) {

		Transaction transaction = new Transaction();

		try {

			if (StringUtils.isBlank(jsonResponse)) {

				logger.info("Empty response received for vepay status enquiry request");
				return transaction;
			}

			JSONObject respObj = new JSONObject(jsonResponse);
			JSONObject txn_response = respObj.getJSONObject("txn_response");
			
			/*
			 * transaction.setAg_ref(txn_response.get("ag_ref").toString());
			 * transaction.setPg_ref(txn_response.get("pg_ref").toString());
			 * transaction.setRes_code(txn_response.get("res_code").toString());
			 * transaction.setRes_message(txn_response.get("res_message").toString());
			 */
			transaction.setStatus(txn_response.get("status").toString());

		} catch (Exception e) {
			logger.error("Exception in parsing status enquiry response for vepay" , e);
		}

		return transaction;

	}

}
