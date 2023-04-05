package com.paymentgateway.payout.globalPay;

import javax.transaction.SystemException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionManager;

@Service
public class GlobalPayRequestCreator {

	private static Logger logger = LoggerFactory.getLogger(GlobalPayRequestCreator.class.getName());

	public String createTransactionRequest(Fields fields, JSONObject adfFields) throws SystemException {

		try {

			if (StringUtils.isBlank(fields.get(FieldType.TXN_ID.getName()))) {
				fields.put(FieldType.TXN_ID.getName(), TransactionManager.getNewTransactionId());
			}

			JSONObject builder = new JSONObject();

			builder.put("ref_id", fields.get(FieldType.TXN_ID.getName()));
			builder.put("account_holder", fields.get(FieldType.BENE_NAME.getName()));
			builder.put("account_number", fields.get(FieldType.BENE_ACCOUNT_NO.getName()));
			builder.put("ifsc_code", fields.get(FieldType.IFSC_CODE.getName()));
			builder.put("payout_amount", fields.get(FieldType.AMOUNT.getName()));
			builder.put("customer_id", fields.get(FieldType.TXN_ID.getName()));

			int random = (int) Math.floor(Math.random() * (999999999 - 500000000 + 1) + 500000000);
//			String phoneNumber = "9" + ThreadLocalRandom.current().nextInt(500000000, 999999999);
			String phoneNumber = "9" + String.valueOf(random);
			if (StringUtils.isNotBlank(fields.get(FieldType.CUST_NAME.getName()))) {
				builder.put("customer_name", fields.get(FieldType.CUST_NAME.getName()));
			} else {
				builder.put("customer_name", PropertiesManager.propertiesMap.get("GLOBALPAYName"));
			}
			if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
				builder.put("customer_email", fields.get(FieldType.CUST_EMAIL.getName()));
			} else {

				builder.put("customer_email", phoneNumber + "@gmail.com");
			}
			if (StringUtils.isNotBlank(fields.get(FieldType.PHONE_NO.getName()))) {
				builder.put("customer_mobile", fields.get(FieldType.PHONE_NO.getName()));
			} else {

				builder.put("customer_mobile", phoneNumber);
			}

			builder.put("payout_count", "1");
			builder.put("total_payout_amount", "1");

			logger.info("GlobalPay Payout Request = {} ", builder.toString());
			return builder.toString();
		} catch (Exception e) {
			logger.info("Exception in createTransactionRequest() ", e);
		}

		return null;
	}

	public String createStatusEnqRequest(Fields fields, JSONObject adfFields) {
		try {

			JSONObject builder = new JSONObject();

			builder.put("ref_id", fields.get(FieldType.TXN_ID.getName()));

			logger.info("GlobalPay Payout Status Request Payload = {} and Order Id = {} ", builder.toString(),
					fields.get(FieldType.ORDER_ID.getName()));

			return builder.toString();
		} catch (Exception e) {
			logger.info("Exception in createStatusEnqRequest() ", e);
		}
		return null;
	}

}
