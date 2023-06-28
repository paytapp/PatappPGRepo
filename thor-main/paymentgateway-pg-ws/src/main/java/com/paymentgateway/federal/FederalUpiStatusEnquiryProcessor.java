package com.paymentgateway.federal;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;

@Service
public class FederalUpiStatusEnquiryProcessor {
	
	@Autowired
	@Qualifier("federalUPITransactionConverter")
	private UpiTranssactionConverter converter;
	
	@Autowired
	@Qualifier("federalUpiTransactionCommunicator")
	private UpiTransactionCommunicator communicator;

	private FederalUpiTransformer federalUpiTransformer = null;
	
	private static Logger logger = LoggerFactory.getLogger(FederalUpiStatusEnquiryProcessor.class.getName());
	
	public void enquiryProcessor(Fields fields) {
		JSONObject request = statusEnquiryRequest(fields);
		JSONObject response = new JSONObject();
		try {
			response = communicator.getResponse(request, fields);
		} catch (SystemException exception) {
			logger.error("Exception", exception);
		}
		
		updateFields(fields, response);
				
	}
	
	public JSONObject statusEnquiryRequest(Fields fields) {

		String senderUserId = fields.get(FieldType.ADF5.getName());
		String senderPassword = fields.get(FieldType.ADF6.getName());
		String senderCode = fields.get(FieldType.ADF7.getName());

		JSONObject tranEnqBody = new JSONObject();
		tranEnqBody.put(Constants.TRANSACTION_ID, fields.get(FieldType.PG_REF_NUM.getName()));

		JSONObject tranEnqHeader = new JSONObject();

		tranEnqHeader.put(Constants.SENDER_USER_ID, senderUserId);
		tranEnqHeader.put(Constants.SENDER_PASSWORD, senderPassword);
		tranEnqHeader.put(Constants.SENDER_CODE, senderCode);

		JSONObject txnEnqReq = new JSONObject();

		txnEnqReq.put(Constants.TRAN_ENQ_HEADER, tranEnqHeader);
		txnEnqReq.put(Constants.TRAN_ENQ_BODY, tranEnqBody);

		JSONObject mainObj = new JSONObject();

		mainObj.put(Constants.TRANSACTION_ENQ_REQ, txnEnqReq);
		
		return mainObj;

	}
	
	public void updateFields(Fields fields, JSONObject response) {
		Transaction transactionResponse = new Transaction();
		transactionResponse = converter.toTransaction(response);
		federalUpiTransformer = new FederalUpiTransformer(transactionResponse);
		federalUpiTransformer.updateResponse(fields);
		
	}

}
