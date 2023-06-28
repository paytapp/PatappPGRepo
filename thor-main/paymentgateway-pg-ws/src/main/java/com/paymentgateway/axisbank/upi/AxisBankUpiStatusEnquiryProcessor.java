package com.paymentgateway.axisbank.upi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

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

@Service
public class AxisBankUpiStatusEnquiryProcessor {

	@Autowired
	@Qualifier("axisBankUpiTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("axisBankUpiFactory")
	private TransactionFactory transactionFactory;

	private static Logger logger = LoggerFactory.getLogger(AxisBankUpiStatusEnquiryProcessor.class.getName());

	public void enquiryProcessor(Fields fields) {
		String request = statusEnquiryRequest(fields);
		String response = "";
		try {
			response = getResponse(request);
		} catch (SystemException exception) {
			logger.error("Exception in Axis Bank Status Enq", exception);
		}

		updateFields(fields, response);

	}

	public String statusEnquiryRequest(Fields fields) {

		JSONObject request = null;
		try {
			transactionFactory.getInstance(fields);
			request = converter.perpareRequest(fields);
		}

		catch (Exception e) {
			logger.error("Exception in Axis UPI Status Enquiry", e);
		}

		return request.toString();
	}

	public static String getResponse(String request) throws SystemException {

		String hostUrl = "";
		hostUrl = PropertiesManager.propertiesMap.get(Constants.AXISBANK_UPI_STATUS_ENQ_URL);

		StringBuilder serverResponse = new StringBuilder();
		HttpsURLConnection connection = null;

		try {

			URL url = new URL(hostUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Content-Length", request.toString());

			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);

			// Send request
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			int code = ((HttpURLConnection) connection).getResponseCode();
			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);

			}
			rd.close();
			logger.info(" Response received For Axis UPI Status Enq >> " + serverResponse.toString());
		} catch (Exception e) {
			logger.error("Exception in Axis UPI Status Request ", e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return serverResponse.toString();
	}


	public void updateFields(Fields fields, String response) {

		try {
			
			JSONObject jsonDataObj = new JSONObject(response);
			JSONArray ja_data = jsonDataObj.getJSONArray("data");
			JSONObject jsonObj = ja_data.getJSONObject(0);
			
			Transaction transaction = new Transaction();

			if (jsonObj.get(Constants.CODE) != null) {
				transaction.setCode(jsonObj.get(Constants.CODE).toString());
			}

			if (jsonObj.get(Constants.RESULT) != null) {
				transaction.setResult(jsonObj.get(Constants.RESULT).toString());
			}

			if (jsonObj.get(Constants.TRAN_ID) != null) {
				transaction.setTranid(jsonObj.get(Constants.TRAN_ID).toString());
			}
			
			if (jsonObj.get(Constants.REF_ID) != null) {
				transaction.setRefid(jsonObj.get(Constants.REF_ID).toString());
			}
			
			if (jsonObj.get(Constants.STATUS) != null) {
				transaction.setStatus(jsonObj.get(Constants.STATUS).toString());
			}
			
			if (jsonObj.get(Constants.REMARKS) != null) {
				transaction.setRemarks(jsonObj.get(Constants.REMARKS).toString());
			}
			
			if (jsonObj.get(Constants.TXNID) != null) {
				transaction.setTxnid(jsonObj.get(Constants.TXNID).toString());
			}
			
			String status = null;
			ErrorType errorType = null;
			String pgTxnMsg = null;
			
			if ((StringUtils.isNotBlank(transaction.getCode()))	&& ((transaction.getCode()).equalsIgnoreCase("00"))
					&& (StringUtils.isNotBlank(transaction.getResult()))&& ((transaction.getResult()).equalsIgnoreCase("S")))

			{
				
				status = StatusType.CAPTURED.getName();
				errorType = ErrorType.SUCCESS;
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

			}

			else {
				if ((StringUtils.isNotBlank(transaction.getCode()))) {
					
					AxisBankUpiStatusEnqResultType resultInstance = AxisBankUpiStatusEnqResultType.getInstanceFromName(transaction.getCode());

					if (resultInstance != null) {
						status = resultInstance.getStatusCode();
						errorType = ErrorType.getInstanceFromCode(resultInstance.getiPayCode());
						pgTxnMsg = resultInstance.getMessage();
					} else {
						status = StatusType.FAILED_AT_ACQUIRER.getName();
						errorType = ErrorType.getInstanceFromCode("022");
						pgTxnMsg = "Transaction failed at acquirer";
					}

				} else {
					status = StatusType.REJECTED.getName();
					errorType = ErrorType.REJECTED;
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();

				}
			}
			 
			  fields.put(FieldType.STATUS.getName(), status);
			  fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
			  fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
			  
			  if (StringUtils.isNotBlank(transaction.getRefid())){
				  
				  fields.put(FieldType.RRN.getName(), transaction.getwCollectTxnId());
			  }
			  
			  
			  if (StringUtils.isNotBlank(transaction.getTxnid())){
				  fields.put(FieldType.ACQ_ID.getName(), transaction.getwCollectTxnId());
			  }
			 
			  if (StringUtils.isNotBlank(transaction.getTxnid())){
				  fields.put(FieldType.ACQ_ID.getName(), transaction.getwCollectTxnId());
			  }
			  
			  fields.put(FieldType.PG_RESP_CODE.getName(),transaction.getCode());
			  fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getResult());
			  fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
			  
			  if (StringUtils.isNotBlank(transaction.getResult())){
				  fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getResult());
			  }
			  
			  if (StringUtils.isNotBlank(transaction.getStatus())){
				  fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getStatus());
			  }
			  
			
		}
		catch (Exception e) {
			logger.error("Exception in Axis UPI Status Enquiry Response ",e);
		}
		
		
	}
}
