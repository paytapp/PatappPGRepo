package com.paymentgateway.payout.qaicash;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.payout.apexPay.Constants;
import com.paymentgateway.pg.core.util.QaicashUtil;

@Service
public class QaicashRequestCreator {

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private QaicashUtil qaicashUtil;

	private static Logger logger = LoggerFactory.getLogger(QaicashRequestCreator.class);

	public String createTransactionRequest(Fields fields, JSONObject adfFields) {
		logger.info("Inside Qaicash createTransactionRequest() ");

		try {

			Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
			String strDate = formatter.format(date);

			String url = propertiesManager.propertiesMap.get("QaicashPayoutCallbackUrl");
			String ifscField = fields.get(FieldType.IFSC_CODE.getName()).substring(0, 4);
			String bank = PayoutBankCode.getBankFullIFSCCodeByIFSC4C(ifscField);
			if (StringUtils.isBlank(bank)) {
				bank = "SBININ";
			}
			String txnType = fields.get(FieldType.TXNTYPE.getName());
			String txnId = fields.get(FieldType.TXN_ID.getName());

			if (StringUtils.isBlank(txnId)) {
				txnId = TransactionManager.getNewTransactionId();
				fields.put(FieldType.TXN_ID.getName(), txnId);
			}

			JSONObject requestJson = new JSONObject();
			requestJson.put("orderId", fields.get(FieldType.TXN_ID.getName()));
			requestJson.put("amount", fields.get(FieldType.AMOUNT.getName()));
			requestJson.put("currency", "INR");
			requestJson.put("dateTime", strDate);
			requestJson.put("language", "en-Us");
			requestJson.put("userId", fields.get(FieldType.TXN_ID.getName()));
			requestJson.put("payoutMethod", "LOCAL_BANK_TRANSFER");
			requestJson.put("redirectUrl", url);
			requestJson.put("callbackUrl", url);
			requestJson.put("bank", bank);
			requestJson.put("beneficiaryName", fields.get(FieldType.BENE_NAME.getName()));
			requestJson.put("accountNumber", fields.get(FieldType.BENE_ACCOUNT_NO.getName()));
			requestJson.put("bankIFSC", fields.get(FieldType.IFSC_CODE.getName()));

			StringBuilder hmacString = new StringBuilder();
			hmacString.append(fields.get(FieldType.ADF1.getName()));
			hmacString.append("|");
			hmacString.append(fields.get(FieldType.TXN_ID.getName()));
			hmacString.append("|");
			hmacString.append(fields.get(FieldType.AMOUNT.getName()));
			hmacString.append("|");
			hmacString.append("INR");
			hmacString.append("|");
			hmacString.append(strDate);
			hmacString.append("|");
			hmacString.append(fields.get(FieldType.TXN_ID.getName()));
			hmacString.append("|");
			hmacString.append(fields.get(FieldType.BENE_NAME.getName()));
			hmacString.append("|");
			hmacString.append(bank);
			hmacString.append("|");
			hmacString.append(fields.get(FieldType.BENE_ACCOUNT_NO.getName()));

			String hash = qaicashUtil.HMAC_SHA256(fields.get(FieldType.ADF2.getName()), hmacString.toString());
			requestJson.put("messageAuthenticationCode", hash);

			logger.info("Qaicash Transaction Request :: {} ", requestJson);

			return requestJson.toString();

		} catch (Exception e) {
			logger.info("Exceptionn in createTransactionRequest() ", e);
		}
		return null;
	}

	public String createStatusEnqRequest(Fields fields, JSONObject adfFields) {
		try {

			StringBuilder hmacString = new StringBuilder();
			hmacString.append(fields.get(FieldType.ADF1.getName()));
			hmacString.append("|");
			hmacString.append(fields.get(FieldType.TXN_ID.getName()));
			String hash = qaicashUtil.HMAC_SHA256(fields.get(FieldType.ADF2.getName()), hmacString.toString());

			StringBuilder request = new StringBuilder();
			request.append(adfFields.getString(Constants.ADF_2));
			request.append("/");
			request.append(fields.get(FieldType.ADF1.getName()));
			request.append("/payout/");
			request.append(fields.get(FieldType.TXN_ID.getName()));
			request.append("/mac/");
			request.append(hash);
			
			logger.info("Status Check Request URL {} for Txn Id {} ",request.toString(),fields.get(FieldType.TXN_ID.getName()));
			
			return request.toString();
		} catch (Exception e) {
			logger.info("Exception in createStatusEnqRequest() ", e);
		}
		return null;
	}

}
