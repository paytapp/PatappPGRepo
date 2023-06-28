package com.paymentgateway.kotak;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;

@Service("kotakTransactionCommunicator")
public class TransactionCommunicator {

	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());
	
	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;

	public void updateSaleResponse(Fields fields, String request) {
		logger.info("Encrypted Request to Kotak bank: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + request);
		fields.put(FieldType.KOTAK_FINAL_REQUEST.getName(), request);
		fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
	}

	@SuppressWarnings("incomplete-switch")
	public String getResponse(String request, Fields fields) throws SystemException {

		String hostUrl = "";
		String response = "";

		TransactionType transactionType = TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()));
		switch (transactionType) {
		case SALE:
		case AUTHORISE:
			break;
		case ENROLL:
			break;
		case CAPTURE:
			break;
		case REFUND:
			hostUrl = PropertiesManager.propertiesMap.get(Constants.REFUND_REQUEST_URL);
			response = refundPostRequest(fields, hostUrl);
			break;
		case STATUS:
			hostUrl = PropertiesManager.propertiesMap.get(Constants.STATUS_ENQ_REQUEST_URL);
			response = statusEnqPostRequest(fields, hostUrl);
			break;
		}
		return response;

	}
	
	public String refundPostRequest(Fields fields, String hostUrl) throws SystemException {
		String response = "";
		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		PostMethod postMethod = new PostMethod(hostUrl);
		postMethod.addParameter(Constants.TXN_REF_NO,
				fields.get(FieldType.ORIG_TXN_ID.getName()));
		postMethod.addParameter(Constants.TXN_TYPE,
				"04");
		postMethod.addParameter(Constants.MERCHANT_ID,
				fields.get(FieldType.MERCHANT_ID.getName()));
		postMethod.addParameter(Constants.PASS_CODE,
				fields.get(FieldType.ADF3.getName()));
		postMethod.addParameter(Constants.AMOUNT,
				fields.get(FieldType.SALE_TOTAL_AMOUNT.getName()));
		postMethod.addParameter(Constants.TERMINAL_ID,
				fields.get(FieldType.ADF1.getName()));
		postMethod.addParameter(Constants.RET_REF_NO,
				fields.get(FieldType.ACQ_ID.getName()));
		postMethod.addParameter(Constants.REFUND_AMOUNT,
				amount);
		postMethod.addParameter(Constants.REF_CANCEL_ID,
				fields.get(FieldType.PG_REF_NUM.getName()));
		postMethod.addParameter(Constants.HASH,
				fields.get(FieldType.HASH.getName()));
		
		StringBuilder requestParams = new StringBuilder();
		NameValuePair[] requestFields = postMethod.getParameters();
		for (NameValuePair nameValuePair : requestFields) {
			requestParams.append(nameValuePair.toString());
			requestParams.append("&");
		}

		logger.info("Request parameters to kotak: TxnType = " + fields.get(FieldType.TXNTYPE.getName() + " " + "Txn id = " + fields.get(FieldType.TXN_ID.getName())) + " "
				+ requestParams.toString());
		
		response = transact(postMethod, hostUrl, fields);
		return response;
	}
	
	public String statusEnqPostRequest(Fields fields, String hostUrl) throws SystemException {
		String response = "";
		PostMethod postMethod = new PostMethod(hostUrl);
		postMethod.addParameter(Constants.TXN_REF_NO,
				fields.get(FieldType.ORDER_ID.getName()));
		postMethod.addParameter(Constants.TXN_TYPE,
				"");
		postMethod.addParameter(Constants.MERCHANT_ID,
				fields.get(FieldType.MERCHANT_ID.getName()));
		postMethod.addParameter(Constants.PASS_CODE,
				fields.get(FieldType.ADF3.getName()));
		postMethod.addParameter(Constants.TERMINAL_ID,
				fields.get(FieldType.ADF1.getName()));
		postMethod.addParameter(Constants.HASH,
				fields.get(FieldType.HASH.getName()));
		
		StringBuilder requestParams = new StringBuilder();
		NameValuePair[] requestFields = postMethod.getParameters();
		for (NameValuePair nameValuePair : requestFields) {
			requestParams.append(nameValuePair.toString());
			requestParams.append("&");
		}

		logger.info("Request parameters to kotak: TxnType = " + fields.get(FieldType.TXNTYPE.getName() + " " + "Txn id = " + fields.get(FieldType.TXN_ID.getName())) + " "
				+ requestParams.toString());
		response = transact(postMethod, hostUrl, fields);
		return response;
	}

	public String transact(HttpMethod httpMethod, String hostUrl, Fields fields) throws SystemException {
		String response = "";

		try {
			HttpClient httpClient = new HttpClient();
			httpClient.executeMethod(httpMethod);

			if (httpMethod.getStatusCode() == HttpStatus.SC_OK) {
				response = httpMethod.getResponseBodyAsString();
				//logger.info("Response from kotak: " + response);
			} else {
				throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Network Exception with kotak "
						+ hostUrl.toString() + "recieved response code" + httpMethod.getStatusCode());
			}
		} catch (IOException ioException) {
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, ioException,
					"Network Exception with kotak " + hostUrl.toString());
		}
		logger.info("Response message from kotak: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + response);
		return response;
	}
}
