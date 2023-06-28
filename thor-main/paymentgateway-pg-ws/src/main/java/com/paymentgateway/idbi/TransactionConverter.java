package com.paymentgateway.idbi;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;
import com.paymentgateway.pg.core.util.IdbiUtil;

@Service("idbiTransactionConverter")
public class TransactionConverter {
	private static Logger logger = LoggerFactory.getLogger(TransactionConverter.class.getName());

	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;

	@Autowired
	private  IdbiUtil idbiutil;
	
	@SuppressWarnings("incomplete-switch")
	public String perpareRequest(Fields fields, Transaction transaction) throws SystemException {

		String request = null;
        String txnType=fields.get(FieldType.ORIG_TXNTYPE.getName());
		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case AUTHORISE:
		case ENROLL:
			break;
		case REFUND:			
		    request = refundRequest(fields, transaction);
			break;
		case SALE:
			if ((fields.get(FieldType.PAYMENT_TYPE.getName()).equals(PaymentType.NET_BANKING.getCode()))) {
				request = saleRequestNb(fields, transaction);
			}else {
			request = saleRequest(fields, transaction);
			}
			break;
		case CAPTURE:
			break;
		case STATUS:
			if(txnType.equals("SALE"))
			{
			request = saleStatusEnquiryRequest(fields, transaction);
			}
			else
			{
			request = refundStatusEnquiryRequest(fields, transaction);
			}
			break;
		}
		return request;

	}

	public String saleRequest(Fields fields, Transaction transaction) throws SystemException {

		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		String returnUrl = PropertiesManager.propertiesMap.get(Constant.SALE_RETURN_URL);
		
		StringBuilder request = new StringBuilder();
		request.append(fields.get(FieldType.MERCHANT_ID.getName()));
		request.append(Constant.PIPE);
		request.append(fields.get(FieldType.PG_REF_NUM.getName()));
		request.append(Constant.PIPE);
		request.append(amount);
		request.append(Constant.PIPE);
		request.append(transaction.getCurrency());
		request.append(Constant.PIPE);
		request.append(Constant.TRANSACTION_REMARKS);
		request.append(Constant.PIPE);
		request.append(Constant.TRANSACTION_REQUEST_TYPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(returnUrl);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(transaction.getPayTypeCode());
		request.append(Constant.PIPE);
		request.append(transaction.getCardNumber());
		request.append(Constant.PIPE);
		request.append(transaction.getExpiryDate());
		request.append(Constant.PIPE);
		request.append(transaction.getCvv());
		request.append(Constant.PIPE);
		request.append(transaction.getNameOnCard());
		request.append(Constant.PIPE);
		request.append(Constant.ADDFIELD10);
		request.append(Constant.PIPE);
		String encryptionKey = fields.get(FieldType.TXN_KEY.getName());
		String encryptedString = null;
		try {
			idbiutil.initEncrypt(encryptionKey);
            encryptedString=idbiutil.encryptMEMessage(request.toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		logRequest(request.toString(), fields);
		return encryptedString;

	}
	
	
	
	public String saleRequestNb(Fields fields, Transaction transaction) throws SystemException {

		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		String returnUrl = PropertiesManager.propertiesMap.get(Constant.SALE_RETURN_URL);
		String bankCode = IdbiBankCode.getBankCode(fields.get(FieldType.MOP_TYPE.getName()));
		
		StringBuilder request = new StringBuilder();
		request.append(fields.get(FieldType.MERCHANT_ID.getName()));
		request.append(Constant.PIPE);
		request.append(fields.get(FieldType.PG_REF_NUM.getName()));
		request.append(Constant.PIPE);
		request.append(amount);
		request.append(Constant.PIPE);
		request.append(transaction.getCurrency());
		request.append(Constant.PIPE);
		request.append(Constant.TRANSACTION_REMARKS);
		request.append(Constant.PIPE);
		request.append(Constant.TRANSACTION_REQUEST_TYPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(returnUrl);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.ADDFIELD9);
		request.append(Constant.PIPE);
		request.append(Constant.ADDFIELD9);
		request.append(Constant.PIPE);
		request.append(transaction.getPayTypeCode());
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(bankCode);
		String encryptionKey = fields.get(FieldType.TXN_KEY.getName());
		String encryptedString = null;
		try {
			idbiutil.initEncrypt(encryptionKey);
            encryptedString=idbiutil.encryptMEMessage(request.toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		logRequest(request.toString(), fields);
		return encryptedString;

	}


	
	
	public String saleStatusEnquiryRequest(Fields fields, Transaction transaction) throws SystemException {
		StringBuilder request = new StringBuilder();
		request.append(fields.get(FieldType.MERCHANT_ID.getName()));
		request.append(Constant.PIPE);
		request.append(fields.get(FieldType.ORIG_TXN_ID.getName()));
		request.append(Constant.PIPE);
		request.append(fields.get(FieldType.ACQ_ID.getName()));
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		
		logger.info("Sale Status Request for IDBI: Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + request);
		String encryptionKey = fields.get(FieldType.TXN_KEY.getName());
		String encryptedString = null;
		try {
			idbiutil.initEncrypt(encryptionKey);		
			encryptedString="merchantReqStrT=" +idbiutil.encryptMEMessage(request.toString())+ "&mid=" + fields.get(FieldType.MERCHANT_ID.getName());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return encryptedString;

	}
	
	
	
	public String refundStatusEnquiryRequest(Fields fields, Transaction transaction) throws SystemException {
		String message;
		JSONObject json = new JSONObject();
		json.put(Constant.MERCHANT_ID, fields.get(FieldType.MERCHANT_ID.getName()));
		json.put(Constant.TXN_REF_NO, fields.get(FieldType.ACQ_ID.getName()));
		json.put(Constant.REQUEST_REF_ORDER_ID,fields.get(FieldType.ORIG_TXN_ID.getName()));
		message = json.toString();

		logger.info("Refund Status Request for IDBI: Txn id = " + fields.get(FieldType.TXN_ID.getName()) + message);
		String encryptionKey = fields.get(FieldType.TXN_KEY.getName());
		String encryptedString = null;
		try {
			idbiutil.initEncrypt(encryptionKey);		
			encryptedString="merchantReqStrT=" +idbiutil.encryptMEMessage(message)+ "&mid=" + fields.get(FieldType.MERCHANT_ID.getName());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return encryptedString;

	}
	
	
	
	public String refundRequest(Fields fields, Transaction transaction) throws SystemException {

		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		StringBuilder request = new StringBuilder();
		request.append(fields.get(FieldType.MERCHANT_ID.getName()));
		request.append(Constant.PIPE);
		request.append(fields.get(FieldType.ORIG_TXN_ID.getName()));
		request.append(Constant.PIPE);
		request.append(fields.get(FieldType.ACQ_ID.getName()));
		request.append(Constant.PIPE);
		request.append(amount);
		request.append(Constant.PIPE);
		request.append(fields.get(FieldType.PG_REF_NUM.getName()));
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(fields.get(FieldType.REFUND_FLAG.getName()));
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.PIPE);
		request.append(Constant.ADDFIELD9);
		request.append(Constant.PIPE);
		request.append(Constant.ADDFIELD10);
	
		logger.info("Refund Request for IDBI: Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + request);
		String encryptionKey = fields.get(FieldType.TXN_KEY.getName());
		String encryptedString = null;
		try {
			idbiutil.initEncrypt(encryptionKey);		
			encryptedString="merchantReqStrT=" +idbiutil.encryptMEMessage(request.toString())+ "&MID=" + fields.get(FieldType.MERCHANT_ID.getName());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return encryptedString;

	}
	
	
	
	
	public Transaction toTransaction(Fields fields,String refundResponse1)
	{
		logger.info("Refund Response Received from IDBI toTransaction method(): TxnType =" + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id =" + fields.get(FieldType.TXN_ID.getName()) + " " + refundResponse1);

		Transaction refundTransaction=new Transaction();
		try
		{
		idbiutil.initDecrypt(fields.get(FieldType.TXN_KEY.getName()));
		String response=idbiutil.decryptMEssage(refundResponse1);	
		logger.info("Decrypt refund Response Received from IDBI toTransaction method(): TxnType =" + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id =" + fields.get(FieldType.TXN_ID.getName()) + " " + response);
		
		if(StringUtils.isNotBlank(response))
		{
		String[] resIdbiRefund = response.split("\\|");
		String refundStatusCode=resIdbiRefund[4];
	    refundTransaction.setAcqId(resIdbiRefund[0]);
	    refundTransaction.setPgMeTrnRefNo(resIdbiRefund[1]);
	    refundTransaction.setResponseCode(refundStatusCode);
	    refundTransaction.setRefundAmt(resIdbiRefund[3]);   
	    refundTransaction.setStatusCode(refundStatusCode);
	    refundTransaction.setMessage(resIdbiRefund[5]);
		}
		}
		catch(Exception ex)
		{
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			logger.error("Network Exception with IDBI", ex);
		}
		
		return refundTransaction;
	}
	
	
	
	public Transaction toStatusTransaction(Fields fields,String statusResponse)
	{
		logger.info("Status Response for IDBI: TxnType =" + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id =" + fields.get(FieldType.TXN_ID.getName()) + " " + statusResponse);
		Transaction statusTransaction=new Transaction();
		try
		{
		idbiutil.initDecrypt(fields.get(FieldType.TXN_KEY.getName()));
		String response=idbiutil.decryptMEssage(statusResponse);	
		String[] resIdbiRefund = response.split("\\|");
		statusTransaction.setAcqId(resIdbiRefund[0]);
		statusTransaction.setRrn(resIdbiRefund[6]);
		statusTransaction.setAuthCode(resIdbiRefund[7]);
		statusTransaction.setResponseCode(resIdbiRefund[8]);
		statusTransaction.setStatusCode(resIdbiRefund[10]);
		statusTransaction.setMessage(resIdbiRefund[11]);
		}
		catch(Exception ex)
		{
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			logger.error("Network Exception with IDBI", ex);
		}
    	  return statusTransaction;
	}
	
	
	
	
	
	public void logRequest(String message, Fields fields){
		
		String request[]=message.split("\\|");
		String cardType=request[20];
		String cardNumber=request[21];
		String expYear=request[22];
		String cvv=request[23];
		message=Pattern.compile(cardType).matcher(message).replaceAll("");
		message=Pattern.compile(cardNumber).matcher(message).replaceAll("");
		message=Pattern.compile(expYear).matcher(message).replaceAll("");
		message=Pattern.compile(cvv).matcher(message).replaceAll("");
		logger.info("Sale Request for IDBI Bank : TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " " +  "Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + message);
	}


	}


