package com.paymentgateway.kotak.upi;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;


@Service
public class KotakUpiIntegrator {
	private static Logger logger = LoggerFactory.getLogger(KotakUpiIntegrator.class.getName());
	@Autowired
	@Qualifier("kotakUpiTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("kotakUpiFactory")
	private TransactionFactory transactionFactory;

	@Autowired
	@Qualifier("kotakUpiTransactionCommunicator")
	private TransactionCommunicator communicator;

	public void process(Fields fields) throws SystemException {
		Transaction transactionRequest = transactionFactory.getInstance(fields);
		String accessTokenResponse = authorization(fields);
		String vpaResponse = vpaAddressValidator(fields, accessTokenResponse);
		// compare the request
		if (vpaResponse.equals(Constants.SUCCESS_RESPONSE)) {
			 accessTokenResponse = authorization(fields);
			send(fields ,accessTokenResponse);
		} else {
			communicator.updateInvalidVpaResponse(fields, vpaResponse);
		} 

	}

	public String authorization(Fields fields) throws SystemException {

		String request = converter.authoriseRequest(fields);
		logger.info("OAuth request" + request);
		JSONObject response = communicator.getAuthoriseResponse(request, fields);
		String accessToken = response.getString(Constants.ACCESS_TOKEN);
		logger.info("OAuth response" + accessToken);

		return accessToken;

	}

	public String vpaAddressValidator(Fields fields, String accessTokenResponse) throws SystemException {

		JSONObject request = converter.vpaValidatorRequest(fields);
		logger.info("VPA Validation request FOR KOTAK UPI" + request);
		JSONObject response = communicator.getVPAResponse(request, fields,accessTokenResponse);
		logger.info("VPA Validation  response kotakUpi" + response);
		String vpaStatus = response.getString(Constants.CODE);
		logger.info("VPA Validation responseCode kotakUpi" + vpaStatus);
		return vpaStatus;

	}

	public void send(Fields fields,String accessTokenResponse) throws SystemException {
		
		JSONObject request = new JSONObject();
		String collectStatus = "";
		String xHeader = "";
		String transactionType = fields.get(FieldType.TXNTYPE.getName());
		try {
			request = converter.perpareRequest(fields);
			if (transactionType.equals(TransactionType.SALE.getName())) {
				xHeader = converter.perpareHeader(fields, request);
			} else if (transactionType.equals(TransactionType.REFUND.getName())) {
				xHeader = converter.perpareRefundHeader(fields, request);
			}
		} catch (Exception e) {
			logger.error("Exception kotak upi : " , e);
		}
		 logger.info("Collect API request for KotakUpi" + request);
		JSONObject response = communicator.getResponse(request, fields,accessTokenResponse,xHeader);
		 logger.info("Collect API response for kotakUpi" + response);
		 if(!response.toString().isEmpty()) {
		 collectStatus = response.getString(Constants.CODE); 
		 }
		 communicator.updateSaleResponse(fields, collectStatus);
}

}