
package com.paymentgateway.idfcUpi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;

/**
 * @author VJ
 *
 */

@Service("idfcUpiTransactionConverter")
public class TransactionConverter {
	private static Logger logger = LoggerFactory.getLogger(TransactionConverter.class.getName());

	@Autowired
	@Qualifier("idfcUpiHmacAlgo")
	private IdfcUpiHmacAlgo idfcUpiHmacAlgo;

	@Autowired
	@Qualifier("idfcUpiUtilAlgo")
	private IdfcUpiAlgoUtil idfcUpiUtilAlgo;

	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;
	private static Map<String, String> generatedDEK = new HashMap<String, String>();

	@SuppressWarnings("incomplete-switch")
	public JSONObject perpareRequest(Fields fields, String encryptedDEK) throws SystemException {

		JSONObject request = null;

		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case REFUND:
			request = payRequest(fields, encryptedDEK);
			break;
		case SALE:
			request = collectRequest(fields, encryptedDEK);
			break;
		case ENQUIRY:
			// request = statusEnquiryRequest(fields);
			break;
		}
		return request;

	}

	public String generateDek(Fields fields) throws SystemException {
		String encryptedDEK = "";
		String payIdIdfcUpi = Constants.IDFC_PREFIX + fields.get(FieldType.PAY_ID.getName());
		logger.info("IDFC bank UPI TransactionConverter in generateDek method  for payIdIdfcUpi = " + payIdIdfcUpi);
		String genDEK = "";
		try {
			if (generatedDEK.isEmpty()) {
				encryptedDEK = idfcUpiUtilAlgo.generateDEK(fields);
				logger.info("IDFC bank UPI TransactionConverter in generateDek method  for encryptedDEK = "
						+ fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id"
						+ fields.get(FieldType.TXN_ID.getName()) + " " + encryptedDEK);
				generatedDEK.put(payIdIdfcUpi, encryptedDEK);
			}
			genDEK = generatedDEK.get(payIdIdfcUpi);

		} catch (Exception e) {
			logger.error("Exception : " , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in encrypt method for idfc upi in TransactionConverter");

		}
		logger.info("IDFC bank UPI TransactionConverter in generateDek method  for encryptedDEK = "
				+ fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id" + fields.get(FieldType.TXN_ID.getName())
				+ " " + genDEK);
		return genDEK;

	}

	public JSONObject vpaValidatorRequest(Fields fields, String encryptedDEK) throws SystemException {

		String virAddress = fields.get(FieldType.PAYER_ADDRESS.getName());
		String MobileNo = fields.get(FieldType.ADF9.getName());
		String DeviceID = PropertiesManager.propertiesMap.get(Constants.DEVICEID);
		String Channel = PropertiesManager.propertiesMap.get(Constants.CHAN);
		String MerchantID = fields.get(FieldType.ADF5.getName());
		String SubMerchantID = "";
		String TerminalID = fields.get(FieldType.ADF10.getName());
		String merchantCredential = "";
		String HMAC = "";
		String appVersion = PropertiesManager.propertiesMap.get(Constants.APPVERSION);
		String hmacKey = fields.get(FieldType.ADF7.getName());
		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		String trxnID = Constants.PRE_FIX + fields.get(FieldType.TXN_ID.getName()) + Constants.POST_FIX;

		try {
			merchantCredential = idfcUpiUtilAlgo.generateMerchantCredential(
					trxnID + Constants.SPECIAL_CHA + Constants.TRANS_PASSWORD, fields, encryptedDEK);

		} catch (Exception e) {
			logger.error("Exception : " , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in encrypt method for idfc upi in TransactionConverter");

		}

		DateFormat currentDate = new SimpleDateFormat(Constants.DATEFORMAT);
		Calendar calobj = Calendar.getInstance();
		String timeStamp = currentDate.format(calobj.getTime());

		JSONObject json = new JSONObject();
		json.put(Constants.OPERATION_NAME, Constants.OPERATION_NAME_VPA);
		json.put(Constants.TXN_ID, trxnID);
		json.put(Constants.MOBILE_NO, MobileNo);
		json.put(Constants.VIR_ADD, virAddress);
		json.put(Constants.DEVICE_ID, DeviceID);
		json.put(Constants.CHANNEL, Channel);
		json.put(Constants.TIME_STAMP, timeStamp);
		json.put(Constants.MERCHANT_ID, MerchantID);
		json.put(Constants.SUBMERCHANT_ID, SubMerchantID);
		json.put(Constants.TERMINAL_ID, TerminalID);
		json.put(Constants.MERCHANT_CREDENTIAL, merchantCredential);
		json.put(Constants.APP_VERSION, appVersion);
		json.put(Constants.HMAC, HMAC);

		String strHmac = json.toString();
		logger.info("IDFC bank UPI VPA VALIDATION request  = " + fields.get(FieldType.TXNTYPE.getName()) + " "
				+ "Txn id " + fields.get(FieldType.TXN_ID.getName()) + " " + strHmac);

		String HMACFinal = "";
		try {
			HMACFinal = idfcUpiHmacAlgo.verifyHMACvalue(strHmac.trim(), hmacKey);
			logger.info(
					"IDFC bank UPI VPA VALIDATION request for HMACFinal = " + fields.get(FieldType.TXNTYPE.getName())
							+ " " + "Txn id " + fields.get(FieldType.TXN_ID.getName()) + " " + HMACFinal);

		} catch (Exception e) {
			logger.error("Exception : " , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in encrypt method for idfc upi in TransactionConverter");

		}

		json.put(Constants.HMAC, HMACFinal);

		fields.put(FieldType.CUST_NAME.getName(), fields.get(FieldType.PAYER_NAME.getName()));
		/*
		 * fields.put(FieldType.UDF3.getName(),
		 * fields.get(FieldType.PAYER_ADDRESS.getName())); String payerName =
		 * fields.get(FieldType.PAYER_NAME.getName());
		 * fields.put(FieldType.UDF4.getName(), payerName);
		 */

		return json;

	}

	public JSONObject collectRequest(Fields fields, String encryptedDEK) throws SystemException {

		String payerAddress = fields.get(FieldType.PAYER_ADDRESS.getName());
		String amount = acquirerTxnAmountProvider.amountProvider(fields);

		// to add 10 min in expiry date
		DateFormat currDate = new SimpleDateFormat(Constants.EXP_DATEFORMAT);
		Calendar cal = Calendar.getInstance();
		String currentdate = currDate.format(cal.getTime());
		cal.add(Calendar.MINUTE, 7);

		String Expdate = currentdate + Constants.DATE_SEPERATOR + currDate.format(cal.getTime());

		String MobileNo = fields.get(FieldType.ADF9.getName());
		String PayeeVirAdd = fields.get(FieldType.ADF8.getName());

		String Remarks = Constants.COLLECT;
		String DevIp = PropertiesManager.propertiesMap.get(Constants.DEVIP);
		String DevType = PropertiesManager.propertiesMap.get(Constants.DEVTYPE);
		String DevOs = PropertiesManager.propertiesMap.get(Constants.DEVOS);
		String DevApp = PropertiesManager.propertiesMap.get(Constants.DEVAPP);
		String DevCapability = PropertiesManager.propertiesMap.get(Constants.DEVCAPABILITY);
		String DevId = PropertiesManager.propertiesMap.get(Constants.DEVID);
		String GeoCode = PropertiesManager.propertiesMap.get(Constants.GEOCODE);
		String DeviceID = PropertiesManager.propertiesMap.get(Constants.DEVICEID);
		String Channel = PropertiesManager.propertiesMap.get(Constants.CHAN);
		String appVersion = PropertiesManager.propertiesMap.get(Constants.APPVERSION);
		String payeeCode = PropertiesManager.propertiesMap.get(Constants.PAYEE_CODE);
		String MerchantID = fields.get(FieldType.ADF5.getName());
		String SubMerchantID = "";
		String TerminalID = fields.get(FieldType.ADF10.getName());
		String MerchantCredential = "";
		String RemitterDetail = "";
		String RefURL = PropertiesManager.propertiesMap.get(Constants.IDFC_UPI_REF_URL);
		String TxnRefId = "123212786481";
		String HMAC = "";
		String hmacKey = fields.get(FieldType.ADF7.getName());
		String trxnID = Constants.PRE_FIX + fields.get(FieldType.PG_REF_NUM.getName()) + Constants.POST_FIX;
		try {
			MerchantCredential = idfcUpiUtilAlgo.generateMerchantCredential(trxnID + "#" + Constants.TRANS_PASSWORD,
					fields, encryptedDEK);
			logger.info("IDFC bank Collect request for MerchantCredential = " + fields.get(FieldType.TXNTYPE.getName())
					+ " " + "Txn id" + fields.get(FieldType.TXN_ID.getName()) + " " + MerchantCredential);
		} catch (Exception e) {
			logger.error("Exception : " , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in encrypt method for idfc upi in TransactionConverter");

		}
		DateFormat currentDate = new SimpleDateFormat(Constants.DATEFORMAT);
		Calendar calobj = Calendar.getInstance();
		String timeStamp = currentDate.format(calobj.getTime());

		JSONObject json = new JSONObject();
		json.put(Constants.OPERATION_NAME, Constants.OPERATION_NAME_COLLECT);
		json.put(Constants.TXN_ID, trxnID);
		json.put(Constants.MOBILE_NO, MobileNo);
		json.put(Constants.AMOUNT, amount);
		json.put(Constants.PAYER_VIR_ADD, payerAddress);
		json.put(Constants.PAYEE_VIR_ADD, PayeeVirAdd);
		json.put(Constants.EXPDATE, Expdate);
		json.put(Constants.REMITTER_DETAIL, Constants.BLANK);
		json.put(Constants.REMARKS, Remarks);
		json.put(Constants.TXN_REF_ID, Constants.BLANK);
		json.put(Constants.REF_URL, Constants.BLANK);
		// json.put(Constants.EXP_DEV_LOC, Constants.COUNTRY);
		json.put(Constants.DEV_IP, DevIp);
		json.put(Constants.DEV_TYPE, DevType);
		json.put(Constants.DEV_OS, DevOs);
		json.put(Constants.APP_VERSION, appVersion);
		json.put(Constants.DEV_APP, DevApp);
		json.put(Constants.DEV_CAPABILITY, DevCapability);
		json.put(Constants.REMITTER_DETAIL, RemitterDetail);
		json.put(Constants.REF_URL, RefURL);
		json.put(Constants.TXN_REF_ID, TxnRefId);
		// json.put(Constants.DEV_ID, DevId);
		json.put(Constants.DEV_LOCATION, Constants.COUNTRY);
		json.put(Constants.GEO_CODE, GeoCode);
		json.put(Constants.PAYEE_CODE, payeeCode);
		json.put(Constants.DEVICE_ID, DeviceID);
		json.put(Constants.CHANNEL, Channel);
		json.put(Constants.TIME_STAMP, timeStamp);
		json.put(Constants.MERCHANT_ID, MerchantID);
		json.put(Constants.SUBMERCHANT_ID, SubMerchantID);
		json.put(Constants.TERMINAL_ID, TerminalID);
		json.put(Constants.MERCHANT_CREDENTIAL, MerchantCredential);
		json.put(Constants.HMAC, HMAC);

		String strHmac = json.toString();
		logger.info("IDFC bank Collect request  = " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id"
				+ fields.get(FieldType.TXN_ID.getName()) + " " + strHmac);

		String HMACFinal = "";
		try {
			HMACFinal = idfcUpiHmacAlgo.verifyHMACvalue(strHmac.trim(), hmacKey);
			logger.info("IDFC bank Collect request for HMACFinal = " + fields.get(FieldType.TXNTYPE.getName()) + " "
					+ "Txn id" + fields.get(FieldType.TXN_ID.getName()) + " " + HMACFinal);

		} catch (Exception e) {
			logger.error("Exception : " , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in encrypt method for idfc upi in TransactionConverter");

		}

		json.put(Constants.HMAC, HMACFinal);

		fields.put(FieldType.CUST_NAME.getName(), fields.get(FieldType.PAYER_NAME.getName()));
		// String payerName = fields.get(FieldType.PAYER_NAME.getName());
		// fields.put(FieldType.UDF4.getName(), payerName);

		return json;
	}

	public JSONObject payRequest(Fields fields, String encryptedDEK) throws SystemException {
		String PayeeVirAdd = fields.get(FieldType.PAYER_ADDRESS.getName());
		if (StringUtils.isBlank(PayeeVirAdd)) {
			PayeeVirAdd = fields.get(FieldType.UDF3.getName());
		}
		String amount = acquirerTxnAmountProvider.amountProvider(fields);

		String MobileNo = fields.get(FieldType.ADF9.getName());
		String PayerVirAdd = fields.get(FieldType.ADF8.getName());
		String Remarks = Constants.REFUND;
		String DeviceID = PropertiesManager.propertiesMap.get(Constants.DEVICEID);
		String Channel = PropertiesManager.propertiesMap.get(Constants.CHAN);
		String MerchantID = fields.get(FieldType.ADF5.getName());
		String SubMerchantID = "";
		String TerminalID = fields.get(FieldType.ADF10.getName());
		String MerchantCredential = "";
		String HMAC = "";
		String AppVersion = PropertiesManager.propertiesMap.get(Constants.APPVERSION);
		String origTxnId = fields.get(FieldType.ACQ_ID.getName());
		// String origTxnId = "IDFIR105619030616194511111111111111";
		String hmacKey = fields.get(FieldType.ADF7.getName());
		String trxnID = Constants.PRE_FIX + fields.get(FieldType.PG_REF_NUM.getName()) + Constants.POST_FIX;
		try {
			MerchantCredential = idfcUpiUtilAlgo.generateMerchantCredential(trxnID + "#" + Constants.TRANS_PASSWORD,
					fields, encryptedDEK);
			logger.info("IDFC bank Collect request for MerchantCredential = " + fields.get(FieldType.TXNTYPE.getName())
					+ " " + "Txn id" + fields.get(FieldType.TXN_ID.getName()) + " " + MerchantCredential);
		} catch (Exception e) {
			logger.error("Exception : " , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in encrypt method for idfc upi in TransactionConverter");

		}
		DateFormat currentDate = new SimpleDateFormat(Constants.DATEFORMAT);
		Calendar calobj = Calendar.getInstance();
		String timeStamp = currentDate.format(calobj.getTime());

		JSONObject json = new JSONObject();
		json.put(Constants.OPERATION_NAME, Constants.OPERATION_NAME_REFUND);
		json.put(Constants.TXN_ID, trxnID);
		json.put(Constants.MOBILE_NO, MobileNo);
		json.put(Constants.ORIG_TXN_ID_REFUND, origTxnId);
		// json.put(Constants.ORIG_CUSTREF_ID, origTxnId);
		json.put(Constants.AMOUNT, amount);
		json.put(Constants.PAYER_VIR_ADD, PayerVirAdd);
		json.put(Constants.PAYEE_VIR_ADD, PayeeVirAdd);
		json.put(Constants.REMARKS, Remarks);
		json.put(Constants.DEVICE_ID, DeviceID);
		json.put(Constants.CHANNEL, Channel);
		json.put(Constants.APP_VERSION, AppVersion);
		json.put(Constants.TIME_STAMP, timeStamp);
		json.put(Constants.MERCHANT_ID, MerchantID);
		json.put(Constants.SUBMERCHANT_ID, SubMerchantID);
		json.put(Constants.TERMINAL_ID, TerminalID);
		json.put(Constants.MERCHANT_CREDENTIAL, MerchantCredential);
		json.put(Constants.HMAC, HMAC);

		String strHmac = json.toString();
		logger.info("IDFC bank Collect request  = " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id"
				+ fields.get(FieldType.TXN_ID.getName()) + " " + strHmac);

		String HMACFinal = "";
		try {
			HMACFinal = idfcUpiHmacAlgo.verifyHMACvalue(strHmac.trim(), hmacKey);
			logger.info("IDFC bank Collect request for HMACFinal = " + fields.get(FieldType.TXNTYPE.getName()) + " "
					+ "Txn id" + fields.get(FieldType.TXN_ID.getName()) + " " + HMACFinal);

		} catch (Exception e) {
			logger.error("Exception : " , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in encrypt method for idfc upi in TransactionConverter");

		}

		json.put(Constants.HMAC, HMACFinal);

		return json;
	}

	public Transaction toTransaction(JSONObject jsonResponse, Fields fields) throws SystemException {
		Transaction transaction = new Transaction();
		try {

			String collectStatus = jsonResponse.getString(Constants.RES_CODE);
			String collectMsg = jsonResponse.getString(Constants.RES_MSG);

			transaction.setStatus(collectStatus);
			transaction.setResponseMessage(collectMsg);

		} catch (Exception e) {
			logger.error("Exception in to transact methord in converter class for idfc bank upi : " , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in toTransaction method for idfc upi in TransactionConverter");
		}
		logger.info("Collect API sale response  = " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id"
				+ fields.get(FieldType.TXN_ID.getName()) + " " + transaction.getStatus());
		return transaction;

	}

	public Transaction toVpaTransaction(JSONObject jsonResponse, Fields fields) throws SystemException {

		Transaction transaction = new Transaction();
		try {
			String collectStatus = jsonResponse.getString(Constants.RES_CODE);
			String collectMsg = jsonResponse.getString(Constants.RES_MSG);

			transaction.setStatus(collectStatus);
			transaction.setResponseMessage(collectMsg);
		} catch (Exception e) {
			logger.error("Exception in to transact methord in converter class for idfc bank upi : " , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in toVpaTransaction method for idfc upi in TransactionConverter");
		}
		logger.info("Collect API sale response  = " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id"
				+ fields.get(FieldType.TXN_ID.getName()) + " " + transaction.getStatus());
		return transaction;

	}

	public Transaction toTransactionFailureRes(JSONObject jsonResponse, Fields fields) throws SystemException {
		Transaction transaction = new Transaction();
		try {

			String collectStatus = jsonResponse.getString(Constants.RES_CODE);
			String collectMsg = jsonResponse.getString(Constants.RES_MSG);
			String dateTime = jsonResponse.getString(Constants.TIME_STAMP);
			transaction.setResponse(collectMsg);
			transaction.setStatus(collectStatus);
			transaction.setDateTime(dateTime);
		} catch (Exception e) {
			logger.error("Exception in to transact methord in converter class for yes bank upi : " , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in toTransaction method for yes upi in TransactionConverter");
		}
		logger.info("Collect API sale response after decryption = " + fields.get(FieldType.TXNTYPE.getName()) + " "
				+ "Txn id" + fields.get(FieldType.TXN_ID.getName()) + " " + transaction.getStatus());
		return transaction;

	}

	public Transaction toRefundTransaction(JSONObject jsonResponse, Fields fields) throws SystemException {

		Transaction transaction = new Transaction();
		try {
			String collectStatus = jsonResponse.getString(Constants.RES_CODE);
			String collectMsg = jsonResponse.getString(Constants.RES_MSG).replaceAll("'", " -apostrophe_symbol- ");
			;
			String refundAcq = jsonResponse.getString(Constants.TXN_REF_ID);
			String refundRRN = jsonResponse.getString(Constants.CUST_REF_ID);
			String dateTime = jsonResponse.getString(Constants.TIME_STAMP);

			transaction.setStatus(collectStatus);
			transaction.setResponseMessage(collectMsg);
			transaction.setAcq_id(refundAcq);
			transaction.setRrn(refundRRN);
			transaction.setDateTime(dateTime);

			if (!fields.contains(FieldType.PG_REF_NUM.getName())) {
				String transactionid = jsonResponse.getString("TxnId");
				String pgRefNum = transactionid.substring(5, 21);
				fields.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
			}
		} catch (Exception e) {
			logger.error("Exception in to transact methord in converter class for idfc bank upi : " , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in toVpaTransaction method for idfc upi in TransactionConverter");
		}
		logger.info("Collect API sale response  = " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id"
				+ fields.get(FieldType.TXN_ID.getName()) + " " + transaction.getStatus());
		return transaction;

	}

	public Transaction toTransactionStatusEnquiry(JSONObject jsonResponse, Fields fields) throws SystemException {
		Transaction transaction = new Transaction();
		try {

			String resCode = jsonResponse.getString(Constants.RES_CODE);
			String orgResCode = jsonResponse.getString(Constants.ORG_RES_CODE);
			String orgTxnStatus = jsonResponse.getString(Constants.ORG_TXN_STATUS);
			String orgResDesc = jsonResponse.getString(Constants.ORG_RES_DESC);

			if (jsonResponse.has("OrgTxnPayerAddr")) {
				String payeeVpa = jsonResponse.getString("OrgTxnPayerAddr");
				transaction.setCustomerVPA(payeeVpa);
			}
			if (jsonResponse.has("OrgCustRefId")) {
				String customerReference = jsonResponse.getString("OrgCustRefId");
				transaction.setOrgCustRefId(customerReference);
			}
			if (jsonResponse.has("PayerVirAddr")) {
				String payerVpa = jsonResponse.getString("PayerVirAddr");
			}
			if (jsonResponse.has("ResDesc")) {
				String resDesc = jsonResponse.getString("ResDesc").replaceAll("'", " -apostrophe_symbol- ");
			}
			if (jsonResponse.has("OrgTxnRefId")) {
				String acqId = jsonResponse.getString("OrgTxnRefId");
				transaction.setAcq_id(acqId);
			}
			if (jsonResponse.has("OrgTxnPayeeAddr")) {
				String orgTxnPayeeAddr = jsonResponse.getString("OrgTxnPayeeAddr");
			}
			if (jsonResponse.has("OrgTxnStatus")) {
				String OrgTxnStatus = jsonResponse.get("OrgTxnStatus").toString();
			}

			transaction.setOrgTxnStatus(orgTxnStatus);
			transaction.setResponse(resCode);
			transaction.setOrgResponse(orgResCode);
			transaction.setStatus(orgTxnStatus);
			transaction.setResponseMessage(orgResDesc);

		} catch (Exception e) {
			logger.error("Exception in to transact methord in converter class for idfc bank upi : " , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in toTransactionStatusEnquiry method for idfc upi in TransactionConverter");
		}
		logger.info("Collect API sale response  = " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id= "
				+ fields.get(FieldType.TXN_ID.getName()) + " " + transaction.getStatus());
		return transaction;
	}

}
