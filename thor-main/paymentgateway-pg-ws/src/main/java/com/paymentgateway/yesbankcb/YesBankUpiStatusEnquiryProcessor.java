package com.paymentgateway.yesbankcb;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pg.core.util.YesBankUpiUtil;

@Service
public class YesBankUpiStatusEnquiryProcessor {

	@Autowired
	private TransactionConverter converter;

	@Autowired
	private TransactionCommunicator communicator;

	@Autowired
	private YesBankUpiUtil yesBankUpiUtil;

	@Autowired
	private YesBankCbTransformer yesBankCbTransformer;

	private static Logger logger = LoggerFactory.getLogger(YesBankUpiStatusEnquiryProcessor.class.getName());

	public void enquiryProcessor(Fields fields) {
		JSONObject request = statusEnquiryRequest(fields);
		Transaction statusEnquiryResponse = new Transaction();
		String response = null;
		try {
			response = communicator.getResponse(request, fields);
			statusEnquiryResponse = converter.toTransactionStatusEnquiry(response, fields);
			updateFields(fields, statusEnquiryResponse);
		} catch (SystemException exception) {
			logger.error("Exception", exception);
		}

	}

	public JSONObject statusEnquiryRequest(Fields fields) {

		String merchantId = fields.get(FieldType.ADF5.getName());
		String lastValue = "NA";
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
		request.append("");
		request.append(Constants.PIPE_SEPARATOR);
		request.append(lastValue);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(lastValue);

		logger.info("Yes Bank UPI Status Enquiry Request = " + request  + " for Order Id "
				+ fields.get(FieldType.ORDER_ID.getName()));
		String key = fields.get(FieldType.ADF7.getName());

		if (StringUtils.isBlank(key)) {
			key = PropertiesManager.propertiesMap.get(Constants.YES_BANKCB_UPI_KEY);
		}

		String encryptedString = null;
		try {
			encryptedString = yesBankUpiUtil.encrypt(request.toString(), key);
			logger.info("Yes Bank UPI Status Enquiry Encrypted Request = " + encryptedString  + " for Order Id "
					+ fields.get(FieldType.ORDER_ID.getName()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		JSONObject json = new JSONObject();
		json.put(Constants.REQUEST_MESSAGE, encryptedString);
		json.put(Constants.PG_MERCHANT_ID, merchantId);

		JSONObject requestjson = json;

		return requestjson;

	}

	public void updateFields(Fields fields, Transaction transactionResponse) throws SystemException {
		yesBankCbTransformer.updateResponse(fields, transactionResponse);

	}

}
