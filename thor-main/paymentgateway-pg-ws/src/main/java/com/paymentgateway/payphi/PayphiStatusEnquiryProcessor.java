package com.paymentgateway.payphi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.EPOSTransactionDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TxnType;
import com.paymentgateway.commons.util.threadpool.ThreadPoolProvider;
import com.paymentgateway.pg.core.util.PayphiUtil;

/**
 * @author Shaiwal
 *
 */
@Service
public class PayphiStatusEnquiryProcessor {

	@Autowired
	@Qualifier("payphiTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	private PayphiUtil payphiUtil;

	@Autowired
	private PropertiesManager propertiesManager;

	private static Logger logger = LoggerFactory.getLogger(PayphiStatusEnquiryProcessor.class.getName());

	public void enquiryProcessor(Fields fields) {
		Map<String, String> request = statusEnquiryRequest(fields);
		String response = "";
		try {
			response = getResponse(request);
		} catch (SystemException exception) {
			logger.error("Exception", exception);
		}

		updateFields(fields, response);

	}

	public Map<String, String> statusEnquiryRequest(Fields fields) {

		Map<String, String> hmReqFields = new TreeMap<String, String>();

		try {

			hmReqFields.put(Constants.MERCHANT_ID, fields.get(FieldType.MERCHANT_ID.getName()));
			hmReqFields.put(Constants.AGG_ID, fields.get(FieldType.PASSWORD.getName()));
			hmReqFields.put(Constants.MERCH_TXN_NO, fields.get(FieldType.PG_REF_NUM.getName()));
			hmReqFields.put(Constants.ORIG_TXN_NO, fields.get(FieldType.PG_REF_NUM.getName()));
			hmReqFields.put(Constants.TXN_TYPE, "STATUS");

			StringBuilder sb = new StringBuilder();

			for (Entry<String, String> entry : hmReqFields.entrySet()) {
				sb.append(entry.getValue());
			}

			String hashValue = payphiUtil.generateHash(sb.toString(), fields.get(FieldType.TXN_KEY.getName()));
			hmReqFields.put(Constants.SECURE_HASH, hashValue);

			logger.info("Payphi Status Enquiry resuest " + sb.toString());
			return hmReqFields;

		}

		catch (Exception e) {
			logger.error("Exception in preparing Payphi Status Enquiry request " , e);
		}

		return hmReqFields;

	}

	public String getResponse(Map<String, String> reqMap) throws SystemException {

		String hostUrl = propertiesManager.propertiesMap.get(Constants.PAYPHI_STATUS_ENQ_URL);

		try {

			URL obj = new URL(hostUrl);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			StringBuilder sb = new StringBuilder();

			for (Entry<String, String> entry : reqMap.entrySet()) {
				sb.append(entry.getKey() + "=" + entry.getValue() + "&");
			}

			String urlParams = sb.toString();
			urlParams = urlParams.substring(0, urlParams.length() - 1);

			con.setDoOutput(true);
			con.setDoInput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParams);
			wr.flush();
			wr.close();
			logger.info("Sending POST request to URL : " + hostUrl);
			int responseCode = con.getResponseCode();
			logger.info("Post parameters : " + urlParams);
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// print result
			logger.info(response.toString());
			return response.toString();

		} catch (Exception e) {
			logger.error("Exception in Payphi status enquiry", e);
		}
		return null;
	}

	public void updateFields(Fields fields, String response) {

		Transaction transactionResponse = new Transaction();
		transactionResponse = toStatusTransaction(response);
		updateResponse(fields, transactionResponse);

	}// toTransaction()


	public void updateResponse(Fields fields, Transaction transaction) {

		
		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;

		if ((StringUtils.isNotBlank(transaction.getTxnStatus()))
				&& (StringUtils.isNotBlank(transaction.getTxnResponseCode()))
				&& (StringUtils.isNotBlank(transaction.getResponseCode()))
				&& ((transaction.getTxnStatus()).equalsIgnoreCase("SUC"))
				&& ((transaction.getTxnResponseCode()).equalsIgnoreCase("0000"))
				&& ((transaction.getResponseCode()).equalsIgnoreCase("000"))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;

			if (StringUtils.isNotBlank(transaction.getRespDescription())) {
				pgTxnMsg = transaction.getRespDescription();
			} else {
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
			}

		} else {
			if (StringUtils.isNotBlank(transaction.getResponseCode())
					&& StringUtils.isNotBlank(transaction.getTxnResponseCode())
					&& !((transaction.getResponseCode()).equalsIgnoreCase("000"))
					&& !((transaction.getTxnResponseCode()).equalsIgnoreCase("0000"))) {
				PayphiResultType resultInstance = PayphiResultType.getInstanceFromName(transaction.getResponseCode());

				if (resultInstance != null) {
					status = resultInstance.getStatusCode();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getiPayCode());
					if (StringUtils.isNotBlank(transaction.getRespDescription())) {
						pgTxnMsg = transaction.getRespDescription();
					} else {
						pgTxnMsg = resultInstance.getMessage();
					}

				} else {
					status = StatusType.DECLINED.getName();
					errorType = ErrorType.getInstanceFromCode("004");
					if (StringUtils.isNotBlank(transaction.getRespDescription())) {
						pgTxnMsg = transaction.getRespDescription();
					} else {
						pgTxnMsg = "Transaction Declined by acquirer";
					}
				}

			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;
				
				if (StringUtils.isNotBlank(transaction.getTxnRespDescription())) {
					pgTxnMsg = transaction.getTxnRespDescription();
				}
				else if (StringUtils.isNotBlank(transaction.getRespDescription())) {
					pgTxnMsg = transaction.getRespDescription();
				} else {
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
				}

			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		fields.put(FieldType.AUTH_CODE.getName(), transaction.getTxnAuthID());
		fields.put(FieldType.RRN.getName(), transaction.getTxnAuthID());
		fields.put(FieldType.ACQ_ID.getName(), transaction.getTxnID());
		
		if (StringUtils.isNotBlank(transaction.getTxnResponseCode())) {
			fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getTxnResponseCode());
		}
		else {
			fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getResponseCode());
		}
		
		if (StringUtils.isNotBlank(transaction.getTxnStatus())) {
			fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getTxnStatus());
		}
		else {
			fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getRespDescription());
		}
		
		if (StringUtils.isNotBlank(transaction.getTxnRespDescription())) {
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getTxnRespDescription());
		}
		else {
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getRespDescription());
		}
		
		if (!StringUtils.isEmpty(fields.get(FieldType.EPOS_MERCHANT.getName()))) {
			if ((Boolean.valueOf(fields.get(FieldType.EPOS_MERCHANT.getName())))
					&& (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TxnType.REFUND.getName()))) {
				ExecutorService es = ThreadPoolProvider.getExecutorService();
				es.execute(new Runnable() {
					@Override
					public void run() {
						new EPOSTransactionDao().updateEposRefundTransaction(fields);
					}
				});
				es.shutdown();
			}
		}
		 
		 

	}

	public Transaction toStatusTransaction(String payphiResponse) {

		Transaction transaction = new Transaction();

		JSONObject resJson = new JSONObject(payphiResponse);

		try {

			if (payphiResponse.contains("responseCode")) {
				transaction.setResponseCode(resJson.get("responseCode").toString());
			}

			if (payphiResponse.contains("respDescription")) {
				transaction.setRespDescription(resJson.get("respDescription").toString());
			}

			if (payphiResponse.contains("merchantTxnNo")) {
				transaction.setMerchantTxnNo(resJson.get("merchantTxnNo").toString());
			}

			if (payphiResponse.contains("txnStatus")) {
				transaction.setTxnStatus(resJson.get("txnStatus").toString());
			}

			if (payphiResponse.contains("txnResponseCode")) {
				transaction.setTxnResponseCode(resJson.get("txnResponseCode").toString());
			}

			if (payphiResponse.contains("txnRespDescription")) {
				transaction.setTxnRespDescription(resJson.get("txnRespDescription").toString());
			}

			if (payphiResponse.contains("txnID")) {
				transaction.setTxnID(resJson.get("txnID").toString());
			}

			if (payphiResponse.contains("txnAuthID")) {
				transaction.setTxnAuthID(resJson.get("txnAuthID").toString());
			}

		}

		catch (Exception e) {
			logger.error("Exception in payphi status enquiry response ", e);
		}

		return transaction;
	}

}
