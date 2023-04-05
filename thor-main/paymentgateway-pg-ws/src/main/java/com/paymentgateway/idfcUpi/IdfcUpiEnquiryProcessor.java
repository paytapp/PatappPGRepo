package com.paymentgateway.idfcUpi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.IdfcUpiAlgoUtil;
import com.paymentgateway.commons.util.IdfcUpiHmacAlgo;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TxnType;

/**
 * @author Amitosh
 *
 */
@Service
public class IdfcUpiEnquiryProcessor {
	
	@Autowired
	@Qualifier("idfcUpiUtilAlgo")
	private IdfcUpiAlgoUtil idfcUpiUtilAlgo;
	
	@Autowired
	@Qualifier("idfcUpiTransformer")
	private IdfcUpiTransformer idfcUpiTransformer;
	
	@Autowired
	@Qualifier("idfcUpiTransactionConverter")
	private TransactionConverter converter;
	
	@Autowired
	@Qualifier("idfcUpiTransactionCommunicator")
	private TransactionCommunicator communicator;
	
	private static Logger logger = LoggerFactory.getLogger(IdfcUpiEnquiryProcessor.class.getName());

	public void enquiryProcessor(Fields fields) throws SystemException {
		
		String MobileNo = fields.get(FieldType.ADF9.getName());
		String OrgTxnId = Constants.PRE_FIX + fields.get(FieldType.PG_REF_NUM.getName()) + Constants.POST_FIX;
		String Channel = PropertiesManager.propertiesMap.get(Constants.CHAN);
		String MerchantID = fields.get(FieldType.ADF5.getName());
		String SubMerchantID = "";
		String DevId = PropertiesManager.propertiesMap.get(Constants.DEVID);
		String AppVersion = PropertiesManager.propertiesMap.get(Constants.APPVERSION);
		String TerminalID = fields.get(FieldType.ADF10.getName());
		String MerchantCredential = "";
		String HMAC = "";
		String hmacKey = fields.get(FieldType.ADF7.getName());
		String trxnID = Constants.PRE_FIX + TransactionManager.getNewTransactionId() + Constants.POST_FIX;
		String encryptedDEK = fields.get(FieldType.ADF6.getName());
		try {
			MerchantCredential = idfcUpiUtilAlgo.generateMerchantCredential(trxnID + Constants.SPECIAL_CHA + Constants.TRANS_PASSWORD,
					fields, encryptedDEK);
		} catch (Exception e) {
			logger.error("Exception : " , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in encrypt method for idfc upi in IdfcUpiEnquiryProcessor");
		}

		DateFormat currentDate = new SimpleDateFormat(Constants.DATEFORMAT);
		Calendar calobj = Calendar.getInstance();
		String timeStamp = currentDate.format(calobj.getTime());

		JSONObject json = new JSONObject();
		json.put(Constants.OPERATION_NAME, Constants.OPERATION_NAME_STATUS);
		json.put(Constants.TXN_ID, trxnID);
		json.put(Constants.MOBILE_NO, MobileNo);
		json.put(Constants.ORIGTRANS_ID, OrgTxnId);
		json.put(Constants.DEVICE_ID, DevId);
		json.put(Constants.APP_VERSION, AppVersion);
		json.put(Constants.CHANNEL, Channel);
		json.put(Constants.TIME_STAMP, timeStamp);
		json.put(Constants.MERCHANT_ID, MerchantID);
		json.put(Constants.SUBMERCHANT_ID, SubMerchantID);
		json.put(Constants.TERMINAL_ID, TerminalID);
		json.put(Constants.MERCHANT_CREDENTIAL, MerchantCredential);
		json.put(Constants.HMAC, HMAC);

		String strHmac = json.toString();

		String HMACFinal = "";
		try {
			IdfcUpiHmacAlgo hmacAlgo = new IdfcUpiHmacAlgo();
			HMACFinal = hmacAlgo.verifyHMACvalue(strHmac.trim(), hmacKey);
		} catch (Exception e) {
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in encrypt method for idfc upi in TransactionConverter");
		}
		json.put(Constants.HMAC, HMACFinal);
		JSONObject serverResp = communicator.getStatusResponse(json,fields);
		updateFields(fields, serverResp);
		
	}

	public void updateFields(Fields fields, JSONObject response) throws SystemException {
		Transaction transactionResponse = new Transaction();
		fields.put(FieldType.TXNTYPE.getName(), TxnType.SALE.getName());
		transactionResponse = converter.toTransactionStatusEnquiry(response, fields);
		idfcUpiTransformer = new IdfcUpiTransformer(transactionResponse);
		idfcUpiTransformer.updateEnquiryResponse(fields, transactionResponse);
	}
}
