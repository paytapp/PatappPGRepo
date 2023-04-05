package com.paymentgateway.payout.apexPay;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

@Service
public class ApexPayUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(ApexPayUtils.class);

	public String getTransactionHash(Fields fields){
		try{
			StringBuilder hashString = new StringBuilder();
			
			hashString.append(fields.get(FieldType.PHONE_NO.getName()))
			.append(fields.get(FieldType.IFSC_CODE.getName()))
			.append(fields.get(FieldType.AMOUNT.getName()))
			.append(fields.get(FieldType.BENE_ACCOUNT_NO.getName()))
			.append(fields.get(FieldType.BENE_NAME.getName()))
			.append(fields.get(FieldType.TXN_ID.getName()))
			.append(fields.get(FieldType.TXNTYPE.getName()))
			.append(fields.get(FieldType.ADF1.getName()));
			
			return Hasher.getHash(hashString.toString()).toLowerCase();
		}catch (Exception e) {
			// TODO: handle exception
		}
		return "";
	}

	public void removeFields(Fields fields) {
		fields.remove(FieldType.ADF1.getName());
		fields.remove(FieldType.ADF2.getName());
		fields.remove(FieldType.ADF4.getName());
	}

	public void updateStatusForPayoutTransaction(Fields fields) {

		logger.info("Orignal response data for payout before updating status >> "+fields.getFields());
		
		if(!fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())){
			fields.put(FieldType.STATUS.getName(),StatusType.PROCESSING.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(),ErrorType.PROCESSING.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(),ErrorType.PROCESSING.getResponseMessage());
		}
		
	}

	public void removeSubMerchantId(Fields fields) {
		if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
			logger.info("Found Sub_Merchant ID " + fields.get(FieldType.SUB_MERCHANT_ID.getName()));

			fields.put(FieldType.PAY_ID.getName(), fields.get(FieldType.SUB_MERCHANT_ID.getName()));
			fields.remove(FieldType.SUB_MERCHANT_ID.getName());

		}
		
	}

	
	
}
