package com.paymentgateway.payout.toshaniDigital;

import javax.transaction.SystemException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionManager;

@Service
public class ToshaniDigitalRequestCreator {

	private static Logger logger = LoggerFactory.getLogger(ToshaniDigitalRequestCreator.class.getName());


	public String createTransactionRequest(Fields fields, JSONObject adfFields) throws SystemException {

		try {

			if (StringUtils.isBlank(fields.get(FieldType.TXN_ID.getName()))) {
				fields.put(FieldType.TXN_ID.getName(), TransactionManager.getNewTransactionId());
			}

			JSONObject builder = new JSONObject();

			builder.put("access_token",fields.get(FieldType.MERCHANT_ID.getName()));
			builder.put("txnAmount",fields.get(FieldType.AMOUNT.getName()));
			builder.put("beneIfscCode",fields.get(FieldType.IFSC_CODE.getName()));
			builder.put("beneAccNum",fields.get(FieldType.BENE_ACCOUNT_NO.getName()));
			builder.put("beneName",fields.get(FieldType.BENE_NAME.getName()));
			builder.put("orderid",fields.get(FieldType.TXN_ID.getName()));
			builder.put("txnPaymode",fields.get(FieldType.TXNTYPE.getName()));
			
			logger.info("Toshani Payout Request = {} ",builder.toString());
			return builder.toString();
		} catch (Exception e) {
			logger.info("Exception in createTransactionRequest() ", e);
		}

		return null;
	}

	public String createStatusEnqRequest(Fields fields, JSONObject adfFields) {
		try {
			
			JSONObject builder = new JSONObject();

			builder.put("access_token",fields.get(FieldType.MERCHANT_ID.getName()));
			builder.put("orderid",fields.get(FieldType.TXN_ID.getName()));

			logger.info("Toshani Digital Payout Status Request Payload = {} and Order Id = {} " , builder.toString() ,fields.get(FieldType.ORDER_ID.getName()));
			
			return builder.toString();
		} catch (Exception e) {
			logger.info("Exception in createStatusEnqRequest() ", e);
		}
		return null;
	}

}
