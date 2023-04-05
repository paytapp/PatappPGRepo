package com.paymentgateway.hdfc.upi;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.pg.core.util.HdfcUpiUtil;

@Service
public class HdfcUpiStatusEnquiryProcessor {

	private static Logger logger = LoggerFactory.getLogger(HdfcUpiStatusEnquiryProcessor.class.getName());

	@Autowired
	@Qualifier("hdfcUpiUtil")
	private HdfcUpiUtil hdfcUpiUtil;

	@Autowired
	@Qualifier("hdfcUpiTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("hdfcUpiTransactionCommunicator")
	private TransactionCommunicator communicator;

	@Autowired
	@Qualifier("hdfcUpiTransformer")
	private HdfcUpiTransformer hdfcUpiTransformer;

	public void enquiryProcessor(Fields fields) {
		JSONObject request = statusEnquiryRequest(fields);
		String response = null;
		try {
			response = communicator.getResponse(request, fields);
		} catch (SystemException exception) {
			logger.error("Exception", exception);
		}

		updateFields(fields, response);

	}

	public JSONObject statusEnquiryRequest(Fields fields) {
		String merchantId = fields.get(FieldType.ADF5.getName());
		String pgRefNum = fields.get(FieldType.PG_REF_NUM.getName());
		StringBuilder request = new StringBuilder();
		request.append(merchantId);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(pgRefNum);
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.LAST_VALUE);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(Constants.LAST_VALUE);

		String key = fields.get(FieldType.ADF7.getName());
		String encryptedString = null;
		try {
			encryptedString = hdfcUpiUtil.encrypt(request.toString(), key);
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}

		JSONObject json = new JSONObject();
		json.put(Constants.REQUEST_MESSAGE, encryptedString);
		json.put(Constants.PG_MERCHANT_ID, merchantId);
		JSONObject requestjson = json;
		return requestjson;

	}

	public void updateFields(Fields fields, String response) {

		Transaction transactionResponse = new Transaction();
		transactionResponse = converter.toTransactionStatusEnquiry(response, fields);
		hdfcUpiTransformer = new HdfcUpiTransformer(transactionResponse);
		hdfcUpiTransformer.updateResponse(fields, transactionResponse);
	}

}
