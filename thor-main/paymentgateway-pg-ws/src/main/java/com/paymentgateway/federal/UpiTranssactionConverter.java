package com.paymentgateway.federal;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;

/**
 * @author Rahul
 *
 */
@Service("federalUPITransactionConverter")
public class UpiTranssactionConverter {
	
	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;

	private static Logger logger = LoggerFactory.getLogger(UpiTranssactionConverter.class.getName());

	@SuppressWarnings("incomplete-switch")
	public JSONObject perpareRequest(Fields fields) throws SystemException {

		JSONObject request = null;
		
		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case REFUND:
			 request = payRequest(fields);
			break;
		case SALE:
			request = collectRequest(fields);
			break;
		case STATUS:
			request = statusEnquiryRequest(fields);
			break;
		}
		return request;

	}

	public JSONObject vpaValidatorRequest(Fields fields) {

		String senderUserId = fields.get(FieldType.ADF5.getName());
		String senderPassword = fields.get(FieldType.ADF6.getName());
		String senderCode = fields.get(FieldType.ADF7.getName());
		String txnType = PropertiesManager.propertiesMap.get("FederalUPIValAddTxnType");
		String payeeAddress = fields.get(FieldType.ADF10.getName());
		String payeeName = fields.get(FieldType.ADF11.getName());
		String envFlag = PropertiesManager.propertiesMap.get(Constants.UPI_ENV_FLAG);	
		String preFix = "";
		if(envFlag.equalsIgnoreCase(Constants.UAT_ENV)){
			preFix = PropertiesManager.propertiesMap.get(Constants.UPI_UAT_PREFIX_FLAG);
		} else if(envFlag.equalsIgnoreCase(Constants.PROD_ENV)){
			preFix = PropertiesManager.propertiesMap.get(Constants.UPI_PROD_PREFIX_FLAG);
		}
		
		JSONObject merchantBody = new JSONObject();
		merchantBody.put(Constants.TRANSACTION_ID, preFix+fields.get(FieldType.TXN_ID.getName()));
		merchantBody.put(Constants.TXN_TYPE, txnType);
		merchantBody.put(Constants.PAYER_ADDRESS, payeeAddress);
		merchantBody.put(Constants.PAYER_NAME, payeeName);
		merchantBody.put(Constants.PAYEE_ADDRESS, fields.get(FieldType.PAYER_ADDRESS.getName()));
		JSONObject merchantHeader = new JSONObject();

		merchantHeader.put(Constants.SENDER_USER_ID, senderUserId);
		merchantHeader.put(Constants.SENDER_PASSWORD, senderPassword);
		merchantHeader.put(Constants.SENDER_CODE, senderCode);

		JSONObject reqValAdd = new JSONObject();

		reqValAdd.put(Constants.MERCHANT_HEADER, merchantHeader);
		reqValAdd.put(Constants.MERCHANT_BODY, merchantBody);

		JSONObject mainObj = new JSONObject();

		mainObj.put(Constants.REQ_VAL_ADD, reqValAdd);
		logger.info("VPA validation Request to federal bank " +  mainObj.toString());
		return mainObj;

	}

	public JSONObject collectRequest(Fields fields) throws SystemException {

		
		String expiryTime = PropertiesManager.propertiesMap.get("FederalUPIExpiryTime");
		String txnType = PropertiesManager.propertiesMap.get("FederalUPISaleTxnType");
		
		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		String senderUserId = fields.get(FieldType.ADF5.getName());
		String senderPassword = fields.get(FieldType.ADF6.getName());
		String senderCode = fields.get(FieldType.ADF7.getName());
		String payeeType = fields.get(FieldType.ADF8.getName());
		String merchantCategoryCode = fields.get(FieldType.ADF9.getName());
		String payeeAddress = fields.get(FieldType.ADF10.getName());
		String payeeName = fields.get(FieldType.ADF11.getName());
		String envFlag = PropertiesManager.propertiesMap.get(Constants.UPI_ENV_FLAG);	
		String preFix = "";
		if(envFlag.equalsIgnoreCase(Constants.UAT_ENV)){
			preFix = PropertiesManager.propertiesMap.get(Constants.UPI_UAT_PREFIX_FLAG);
		} else if(envFlag.equalsIgnoreCase(Constants.PROD_ENV)){
			preFix = PropertiesManager.propertiesMap.get(Constants.UPI_PROD_PREFIX_FLAG);
		}
		
		String customerReferenceId = TransactionManager.getId();
		String collectTxnId = TransactionManager.getNewTransactionId();
		JSONObject ReqPay = new JSONObject();
		ReqPay.put(Constants.TRANSACTION_ID, preFix+collectTxnId);
		ReqPay.put(Constants.REMARKS, fields.get(FieldType.PRODUCT_DESC.getName()));
		ReqPay.put(Constants.TRANSACTION_REFERENCE_ID, preFix+collectTxnId);
		ReqPay.put(Constants.CUSTOMER_REFERENCE, customerReferenceId);
		ReqPay.put(Constants.TXN_TYPE, txnType);
		ReqPay.put(Constants.EXPIRY_TIME, expiryTime);
		ReqPay.put(Constants.MERCHANT_CATEGORY_CODE, merchantCategoryCode);
		ReqPay.put(Constants.PAYER_ADDRESS, fields.get(FieldType.PAYER_ADDRESS.getName()));
		ReqPay.put(Constants.PAYER_NAME, fields.get(FieldType.PAYER_NAME.getName()));
		ReqPay.put(Constants.AMT, amount);
		ReqPay.put(Constants.PAYEE_ADDRESS, payeeAddress);
		ReqPay.put(Constants.PAYEE_TYPE, payeeType);

		JSONObject MerchantBody = new JSONObject();
		MerchantBody.put(Constants.REQ_PAY, ReqPay);

		JSONObject MerchantHeader = new JSONObject();

		MerchantHeader.put(Constants.SENDER_USER_ID, senderUserId);
		MerchantHeader.put(Constants.SENDER_PASSWORD, senderPassword);
		MerchantHeader.put(Constants.SENDER_CODE, senderCode);

		JSONObject uPPaymentReq = new JSONObject();

		uPPaymentReq.put(Constants.MERCHANT_HEADER, MerchantHeader);
		uPPaymentReq.put(Constants.MERCHANT_BODY, MerchantBody);

		JSONObject mainObj = new JSONObject();

		mainObj.put(Constants.UPI_PAYMENT_REQ, uPPaymentReq);
		logger.info("Collect request to Federal bank " +  mainObj.toString());
		fields.put(FieldType.UDF1.getName(), payeeAddress);
		fields.put(FieldType.UDF2.getName(), payeeName);
		fields.put(FieldType.UDF3.getName(), fields.get(FieldType.PAYER_ADDRESS.getName()));
		fields.put(FieldType.UDF4.getName(), fields.get(FieldType.PAYER_NAME.getName()));
		fields.put(FieldType.UDF5.getName(), customerReferenceId);
		return mainObj;

	}
	
	public JSONObject payRequest(Fields fields) {

		String refundSenderUserId = fields.get(FieldType.ADF5.getName());;
		String refundSenderPassword = fields.get(FieldType.ADF6.getName());;
		String refundSenderCode = fields.get(FieldType.ADF7.getName());;
		String txnType = PropertiesManager.propertiesMap.get("FederalUPIRefundTxnType");
		String customerReferenceId = TransactionManager.getId();
		String envFlag = PropertiesManager.propertiesMap.get(Constants.UPI_ENV_FLAG);	
		String preFix = "";
		if(envFlag.equalsIgnoreCase(Constants.UAT_ENV)){
			preFix = PropertiesManager.propertiesMap.get(Constants.UPI_UAT_PREFIX_FLAG);
		} else if(envFlag.equalsIgnoreCase(Constants.PROD_ENV)){
			preFix = PropertiesManager.propertiesMap.get(Constants.UPI_PROD_PREFIX_FLAG);
		}
		
		String senderUserId = refundSenderUserId;
		String senderPassword = refundSenderPassword;
		String senderCode = refundSenderCode;
		String payeeType = fields.get(FieldType.ADF8.getName());
		String merchantCategoryCode = fields.get(FieldType.ADF9.getName());
		String payeeAddress = fields.get(FieldType.ADF10.getName());
		String payeeName = fields.get(FieldType.ADF11.getName());
		String amount =	fields.get(FieldType.AMOUNT.getName());
		String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());
		String convertedAmount = Amount.toDecimal(amount, currencyCode);
		
		JSONObject ReqPay = new JSONObject();
		ReqPay.put(Constants.TRANSACTION_ID, preFix+fields.get(FieldType.TXN_ID.getName()));
		ReqPay.put(Constants.REMARKS, Constants.REFUND_DESCRIPTION_MESSAGE);
		ReqPay.put(Constants.TRANSACTION_REFERENCE_ID, preFix+fields.get(FieldType.TXN_ID.getName()));
		ReqPay.put(Constants.CUSTOMER_REFERENCE, customerReferenceId);
		ReqPay.put(Constants.TXN_TYPE, txnType);
		ReqPay.put(Constants.MERCHANT_CATEGORY_CODE, merchantCategoryCode);
		ReqPay.put(Constants.PAYER_ADDRESS, fields.get(FieldType.UDF1.getName()));
		ReqPay.put(Constants.PAYER_TYPE, payeeType);
		ReqPay.put(Constants.AMT, convertedAmount);
		ReqPay.put(Constants.PAYEE_ADDRESS, fields.get(FieldType.UDF3.getName()));
		ReqPay.put(Constants.PAYEE_NAME, fields.get(FieldType.UDF4.getName()));

		JSONObject MerchantBody = new JSONObject();
		MerchantBody.put(Constants.REQ_PAY, ReqPay);

		JSONObject MerchantHeader = new JSONObject();

		MerchantHeader.put(Constants.SENDER_USER_ID, senderUserId);
		MerchantHeader.put(Constants.SENDER_PASSWORD, senderPassword);
		MerchantHeader.put(Constants.SENDER_CODE, senderCode);

		JSONObject uPPaymentReq = new JSONObject();

		uPPaymentReq.put(Constants.MERCHANT_HEADER, MerchantHeader);
		uPPaymentReq.put(Constants.MERCHANT_BODY, MerchantBody);

		JSONObject mainObj = new JSONObject();

		mainObj.put(Constants.UPI_PAYMENT_REQ, uPPaymentReq);
		
		logger.info("Pay request to Federal bank " +  mainObj.toString());
		fields.put(FieldType.UDF1.getName(), payeeAddress);
		fields.put(FieldType.UDF2.getName(), payeeName);
		fields.put(FieldType.UDF3.getName(), fields.get(FieldType.PAYER_ADDRESS.getName()));
		fields.put(FieldType.UDF4.getName(), fields.get(FieldType.PAYER_NAME.getName()));
		fields.put(FieldType.UDF5.getName(), customerReferenceId);
		return mainObj;

	}

	public JSONObject statusEnquiryRequest(Fields fields) {

		String senderUserId = fields.get(FieldType.ADF5.getName());
		String senderPassword = fields.get(FieldType.ADF6.getName());
		String senderCode = fields.get(FieldType.ADF7.getName());
		String envFlag = PropertiesManager.propertiesMap.get(Constants.UPI_ENV_FLAG);	
		String preFix = "";
		if(envFlag.equalsIgnoreCase(Constants.UAT_ENV)){
			preFix = PropertiesManager.propertiesMap.get(Constants.UPI_UAT_PREFIX_FLAG);
		} else if(envFlag.equalsIgnoreCase(Constants.PROD_ENV)){
			preFix = PropertiesManager.propertiesMap.get(Constants.UPI_PROD_PREFIX_FLAG);
		}
		

		JSONObject tranEnqBody = new JSONObject();
		tranEnqBody.put(Constants.TRANSACTION_ID, preFix+fields.get(FieldType.ORIG_TXN_ID.getName()));

		JSONObject tranEnqHeader = new JSONObject();

		tranEnqHeader.put(Constants.SENDER_USER_ID, senderUserId);
		tranEnqHeader.put(Constants.SENDER_PASSWORD, senderPassword);
		tranEnqHeader.put(Constants.SENDER_CODE, senderCode);

		JSONObject txnEnqReq = new JSONObject();

		txnEnqReq.put(Constants.TRAN_ENQ_HEADER, tranEnqHeader);
		txnEnqReq.put(Constants.TRAN_ENQ_BODY, tranEnqBody);

		JSONObject mainObj = new JSONObject();

		mainObj.put(Constants.TRANSACTION_ENQ_REQ, txnEnqReq);
		
		logger.info("Status Enq request to Federal bank" +  mainObj.toString());
		return mainObj;

	}

	public Transaction toTransactionForVpaValidator(JSONObject response) {

		Transaction transaction = new Transaction();
		Map<String, String> requestMap = new HashMap<String, String>();

		JSONObject RespValAddObject = new JSONObject();
		RespValAddObject = response.getJSONObject(Constants.ACK);
		if(RespValAddObject == null){
		RespValAddObject = response.getJSONObject(Constants.RESP_VAL_ADD);
		}
		for (Object key : RespValAddObject.keySet()) {

			String key1 = key.toString();
			String value = RespValAddObject.get(key.toString()).toString();
			requestMap.put(key1, value);

		}

		transaction.setResponse(requestMap.get(Constants.RESPONSE));
		transaction.setResponseCode(requestMap.get(Constants.RESPONSE_CODE));

		return transaction;

	}

	public Transaction toTransactionForCollect(JSONObject response) {

		Transaction transaction = new Transaction();
		Map<String, String> responseMap = new HashMap<String, String>();

		JSONObject RespValAddObject = new JSONObject();
		RespValAddObject = response.getJSONObject(Constants.ACK);

		for (Object key : RespValAddObject.keySet()) {

			String key1 = key.toString();
			String value = RespValAddObject.get(key.toString()).toString();
			responseMap.put(key1, value);

		}
		transaction.setResponse(responseMap.get(Constants.RESPONSE));
		transaction.setResponseCode(responseMap.get(Constants.RESPONSE_CODE));
		
		return transaction;

	}
	
	public Transaction toTransaction(JSONObject response) {

		Transaction transaction = new Transaction();
		Map<String, String> responseMap = new HashMap<String, String>();

		JSONObject RespValAddObject = new JSONObject();
		RespValAddObject = response.getJSONObject(Constants.RESP_VAL_ADD);

		for (Object key : RespValAddObject.keySet()) {

			String key1 = key.toString();
			String value = RespValAddObject.get(key.toString()).toString();
			responseMap.put(key1, value);

		}
		transaction.setTransaction_id(responseMap.get(Constants.TRANSACTION_ID));
		transaction.setResponse(responseMap.get(Constants.RESPONSE));
		transaction.setResponseCode(responseMap.get(Constants.RESPONSE_CODE));
		transaction.setPayerApprovalNum(responseMap.get(Constants.PAYER_APPROVAL_NUM));
		transaction.setPayeeApprovalNum(responseMap.get(Constants.PAYEE_APPROVAL_NUM));

		return transaction;

	}

}
