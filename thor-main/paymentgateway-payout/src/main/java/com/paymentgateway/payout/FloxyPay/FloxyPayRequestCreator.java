package com.paymentgateway.payout.FloxyPay;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionManager;

@Service
public class FloxyPayRequestCreator {

	private static Logger logger = LoggerFactory.getLogger(FloxyPayRequestCreator.class);

	public String createTransactionRequest(Fields fields, JSONObject adfFields) {
		logger.info("inside createTransactionRequest() ");

		try {

			String txnType = fields.get(FieldType.TXNTYPE.getName());
			String txnId = fields.get(FieldType.TXN_ID.getName());

			if (StringUtils.isBlank(txnId)) {
				txnId = TransactionManager.getNewTransactionId();
				fields.put(FieldType.TXN_ID.getName(), txnId);
			}

			JSONObject requestJson = new JSONObject();
			requestJson.put("orderid", fields.get(FieldType.TXN_ID.getName()));
			requestJson.put("amount", fields.get(FieldType.AMOUNT.getName()));
			requestJson.put("mobile", fields.get(FieldType.PHONE_NO.getName()));
			requestJson.put("note", "Payout");

			if (txnType.equalsIgnoreCase("UPI")) {
				requestJson.put("upi", fields.get(FieldType.PAYER_ADDRESS.getName()));
				requestJson.put("name", fields.get(FieldType.PAYER_NAME.getName()));
			} else {
				requestJson.put("name", fields.get(FieldType.BENE_NAME.getName()));
				requestJson.put("account", fields.get(FieldType.BENE_ACCOUNT_NO.getName()));
				requestJson.put("ifsc", fields.get(FieldType.IFSC_CODE.getName()));
			}

			logger.info("FloxyPay Transaction Request :: {} ",requestJson);

			return requestJson.toString();

		} catch (Exception e) {
			logger.info("Exceptionn in createTransactionRequest() ", e);
		}
		return null;
	}

	public String createStatusEnqRequest(Fields fields, JSONObject adfFields) {
		try{
			JSONObject requestJson = new JSONObject();
			requestJson.put("order_id", fields.get(FieldType.TXN_ID.getName()));
			
			logger.info("FloxyPay Status Enquiry Request Payload >> {} ",requestJson.toString());
			return requestJson.toString();
		}catch (Exception e) {
			logger.info("Exception in createStatusEnqRequest() ",e);
		}
		return null;
	}

}
