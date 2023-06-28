/**
 * 
 */
package com.paymentgateway.iciciUpi;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class IciciUpiStatusEnquiryProcessor {

	@Autowired
	@Qualifier("iciciUpiTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("iciciUpiTransactionCommunicator")
	private TransactionCommunicator communicator;

	@Autowired
	private IciciUpiTransformer iciciUpiTransformer;

	private static Logger logger = LoggerFactory.getLogger(IciciUpiStatusEnquiryProcessor.class.getName());

	public void enquiryProcessor(Fields fields) {
		logger.info("Inside IciciUpiStatusEnquiryProcessor");
		String request = statusEnquiryRequest(fields);
		String response = "";
		try {
			response = communicator.getResponse(request, fields);
		} catch (SystemException exception) {
			logger.error("Exception", exception);
		}
		updateFields(fields, response);
	}

	private String statusEnquiryRequest(Fields fields) {
		try {
			JSONObject request = new JSONObject();
			request.put(Constants.MERCHANT_ID, fields.get(FieldType.MERCHANT_ID.getName()));
			if (StringUtils.isNotBlank(fields.get(FieldType.ADF1.getName()))) {
				request.put(Constants.SUB_MERCHANT_ID, fields.get(FieldType.ADF1.getName()));
			} else {
				request.put(Constants.SUB_MERCHANT_ID, fields.get(FieldType.MERCHANT_ID.getName()));
			}
			request.put(Constants.TERMINAL_ID, fields.get(FieldType.TXN_KEY.getName()));
			request.put(Constants.MERCHANT_TRANSACTION_ID, fields.get(FieldType.PG_REF_NUM.getName()));

			String statusEnquiryRequest = request.toString();
			logger.info("Status Enquiry Request to ICIC UPI, " + statusEnquiryRequest);

			String encodedAndEncyptedstatusEnquiryRequest = converter.encryptAndEncodeRequest(request.toString());
			logger.info("Encrypted Status Enquiry Request to ICIC UPI, " + encodedAndEncyptedstatusEnquiryRequest);
			return encodedAndEncyptedstatusEnquiryRequest;
		} catch (Exception e) {
			logger.error("Exception caugth, " , e);
		}
		return null;
	}
	
	private void updateFields(Fields fields, String response) {
		iciciUpiTransformer.updateEnquiryResponse(fields, new JSONObject(response));		
	}
}