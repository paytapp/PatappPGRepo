package com.paymentgateway.payout.apexPay;

import javax.transaction.SystemException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionManager;

@Service
public class ApexPayRequestCreator {
	
	private static final Logger logger = LoggerFactory.getLogger(ApexPayRequestCreator.class);
	
	@Autowired
	private ApexPayUtils apexPayUtils;

	public String createTransactionRequest(Fields fields, JSONObject adfFields) throws SystemException{
		
		try{
			
			if(StringUtils.isBlank(fields.get(FieldType.TXN_ID.getName()))){
				fields.put(FieldType.TXN_ID.getName(),TransactionManager.getNewTransactionId());
			}
			
			JSONObject JsonRequest = new JSONObject();
			
			JsonRequest.put("api_token", fields.get(FieldType.ADF2.getName()));
			JsonRequest.put("RemittanceAmount", fields.get(FieldType.AMOUNT.getName()));
			JsonRequest.put("MobileNumber", fields.get(FieldType.PHONE_NO.getName()));
			JsonRequest.put("IFSCCode", fields.get(FieldType.IFSC_CODE.getName()));
			JsonRequest.put("AccountNumber", fields.get(FieldType.BENE_ACCOUNT_NO.getName()));
			JsonRequest.put("BeneficiaryName", fields.get(FieldType.BENE_NAME.getName()));
			JsonRequest.put("client_id", fields.get(FieldType.TXN_ID.getName()));
			JsonRequest.put("type", fields.get(FieldType.TXNTYPE.getName()));
			JsonRequest.put("hash", apexPayUtils.getTransactionHash(fields));
			
			return JsonRequest.toString();
		}catch (Exception e) {
			logger.info("Exception in createTransactionRequest() ",e);
		}
		
		return null;
	}

	public String createStatusEnqRequest(Fields fields, JSONObject adfFields) {
		try{
			StringBuilder query = new StringBuilder();
			
			query.append("?")
			.append("api_token")
			.append("=")
			.append(fields.get(FieldType.ADF2.getName()))
			.append("&")
			.append("client_id")
			.append("=")
			.append(fields.get(FieldType.TXN_ID.getName()));
			
			return query.toString();
		}catch (Exception e) {
			logger.info("Exception in createTransactionRequest() ",e);
		}
		return null;
	}
	
	public String createbalanceRequest(Fields fields, JSONObject adfFields) {
		try{
			StringBuilder query = new StringBuilder();
			
			query.append("?")
			.append("api_token")
			.append("=")
			.append(fields.get(FieldType.ADF2.getName()));
			
			return query.toString();
		}catch (Exception e) {
			logger.info("Exception in createbalanceRequest() ",e);
		}
		return null;
	}

}
