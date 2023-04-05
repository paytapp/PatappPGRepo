package com.paymentgateway.icici.composite.api;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.FieldNamingStrategy;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.kms.EncryptDecryptService;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.pg.core.util.IciciUtil;

@Service("iciciTransactionConverter")
public class IciciTransactionConverter {

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private Fields fields;

	@Autowired
	private CrmValidator crmValidator;

	@Autowired
	private EncryptDecryptService encryptDecryptService;

	@Autowired
	private IciciResponseHandler iciciResponseHandler;

	private static Logger logger = LoggerFactory.getLogger(IciciTransactionConverter.class.getName());

	public static final String ACT_CODE_OPEN = "<ActCode>";
	public static final String ACT_CODE_CLOSE = "</ActCode>";
	public static final String RESPONSE_OPEN = "<Response>";
	public static final String RESPONSE_CLOSE = "</Response>";
	public static final String BANK_RRN_OPEN = "<BankRRN>";
	public static final String BANK_RRN_CLOSE = "</BankRRN>";
	public static final String TRAN_REF_OPEN = "<TranRefNo>";
	public static final String TRAN_REF_CLOSE = "</TranRefNo>";
	public static final String BENE_NAME_OPEN = "<BeneName>";
	public static final String BENE_NAME_CLOSE = "</BeneName>";

	public String createRequestForAddBene(Fields fields) {
		logger.info("Creating Add Beneficiry Request");

		try {

			String aggrId = propertiesManager.propertiesMap.get("AGGR_ID");
			String userId = propertiesManager.propertiesMap.get("USER_ID");
			String corpId = fields.get(FieldType.CORP_ID.getName());
			String URN = fields.get(FieldType.URN.getName());

			JSONObject json = new JSONObject();

			json.put(Constants.BENE_AGGR_ID, aggrId);
			json.put(Constants.BENE_CORP_ID, corpId);
			json.put(Constants.BENE_CORP_USER, userId);
			json.put(Constants.URN, URN);
			json.put(Constants.BENE_ACC_NO, fields.get(FieldType.BENE_ACCOUNT_NO.getName()));
			json.put(Constants.BENE_NAME, fields.get(FieldType.BENE_NAME.getName()));
			json.put(Constants.BENE_NICKNAME, fields.get(FieldType.BENE_ALIAS.getName()));

			if (fields.get(FieldType.BENE_PAYEE_TYPE.getName()).equalsIgnoreCase("offus")) {
				json.put(Constants.BENE_PAYEE_TYPE, "O");
			} else {
				json.put(Constants.BENE_PAYEE_TYPE, "W");
			}

			json.put(Constants.BENE_IFSC, fields.get(FieldType.IFSC.getName()));

			logger.info("Created Add Beneficiry Request is " + json.toString() + " " + fields.getFields());
			return json.toString();
		} catch (Exception e) {
			logger.error("Exception in creating response ", e);
			return null;
		} finally {
			removeAccountDetails(fields);
		}
	}

	public String createRequestForValidateBene(Fields fields) {
		logger.info("Creating Validate Beneficiry Request");

		try {

			String userId = propertiesManager.propertiesMap.get("USER_ID");
			String corpId = fields.get(FieldType.CORP_ID.getName());
			String aliasId = fields.get(FieldType.ALIAS_ID.getName());

			JSONObject json = new JSONObject();

			json.put(Constants.BENE_CORP_ID, corpId);

			if (StringUtils.isNotBlank(aliasId)) {
				json.put(Constants.BENE_CORP_USER, aliasId);
			} else {
				json.put(Constants.BENE_CORP_USER, userId);
			}

			json.put(Constants.BENE_ACC_NO, fields.get(FieldType.BENE_ACCOUNT_NO.getName()));

			logger.info("Created Validate Beneficiry Request is " + json.toString());
			return json.toString();
		} catch (Exception e) {
			logger.error("Exception in creating response ", e);
			return null;
		} finally {
			removeAccountDetails(fields);
		}
	}

	public String createRequestForTransaction(Fields fields) {
		logger.info("Creating Transaction Request");

		try {

			String aggrName = propertiesManager.propertiesMap.get("AGGR_NAME");
			String aggrId = propertiesManager.propertiesMap.get("AGGR_ID");
			String userId = propertiesManager.propertiesMap.get("USER_ID");
			
			String defaultIfsc = propertiesManager.propertiesMap.get("ICICI_COMPOSITE_DEFAULT_IFSC");
			
			String corpId = fields.get(FieldType.CORP_ID.getName());
			String URN = fields.get(FieldType.URN.getName());
			String accountNo = fields.get(FieldType.ACCOUNT_NO.getName());
			String amount =  String.valueOf(new BigDecimal(fields.get(FieldType.AMOUNT.getName())).setScale(2));
			
			fields.put(FieldType.AMOUNT.getName(),amount);

			//String currency = Currency.getAlphabaticCode(fields.get(FieldType.CURRENCY_CODE.getName()));


			//String TxnType = getTxnTypeForCIB(fields.get(FieldType.TXNTYPE.getName()));
			String txnId = TransactionManager.getNewTransactionId();

			fields.put(FieldType.TXN_ID.getName(), txnId);
			
			String beneName = fields.get(FieldType.PAYEE_NAME.getName());
			String beneIfsc = fields.get(FieldType.IFSC.getName());
			String beneAcNo = fields.get(FieldType.BENE_ACCOUNT_NO.getName());
			

			JSONObject json = new JSONObject();
					
			
			switch (fields.get(FieldType.TXNTYPE.getName())) {
			case Constants.IMPS:
				
				String remitterCode = propertiesManager.propertiesMap.get("NODAL_IMPS_RETAILER_CODE");
				String remitterMobile = propertiesManager.propertiesMap.get("NODAL_REMITTER_MOBILE");
				String passcode = propertiesManager.propertiesMap.get("NODAL_IMPS_PASSCODE");
				String bcId = propertiesManager.propertiesMap.get("NODAL_IMPS_BCID");
				
				String txnDate = getDateForImpsTransaction();
				
				json.put(Constants.amount, amount);
				json.put(Constants.tranRefNo, txnId);
				json.put(Constants.localTxnDtTime, txnDate);
				json.put(Constants.aggrId, aggrId);
				json.put(Constants.mobile, remitterMobile);
				 json.put(Constants.passCode, passcode);
				 json.put(Constants.paymentRef, "FTTransferP2A");
				 json.put(Constants.senderName, beneName);
				 json.put(Constants.beneIFSC, beneIfsc);
				 json.put(Constants.beneAccNo, beneAcNo);
				 json.put(Constants.bcID, bcId);
				 json.put(Constants.retailerCode, remitterCode);
				 json.put(Constants.crpId, corpId);
				 json.put(Constants.crpUsr, userId);
				break;
			case Constants.NEFT:
				
				json.put(Constants.tranRefNo, txnId);
				json.put(Constants.senderAcctNo, accountNo);
				json.put(Constants.beneAccNo, beneAcNo);
				
				json.put(Constants.beneName, beneName);
				json.put(Constants.amount, amount);
				json.put(Constants.narration1, "Payment GateWay Settlement");

				if (beneIfsc.contains("ICIC")){
					json.put(Constants.txnType, "TPA");
					json.put(Constants.beneIFSC, defaultIfsc);
					fields.put(FieldType.TXNTYPE.getName(),"TPA");
				}else{
					json.put(Constants.txnType, "RGS");
					json.put(Constants.beneIFSC, beneIfsc);
				}

				json.put(Constants.NEFT_AGGR_ID, aggrId);
				json.put(Constants.NEFT_AGGR_NAME, aggrName);
				json.put(Constants.NEFT_CORP_ID, corpId);
				json.put(Constants.NEFT_CORP_USER, userId);
				json.put(Constants.NEFT_URN, URN);
				
				break;
			case Constants.RTGS:
				
				json.put("AMOUNT", amount);
				json.put("UNIQUEID", txnId);
				
				if(StringUtils.isNotBlank(fields.get(FieldType.REMARKS.getName())))
					json.put("REMARKS", fields.get(FieldType.REMARKS.getName()));
				else
					json.put("REMARKS", "Payment GateWay Settlement");

				json.put("CURRENCY", "INR");
				json.put("PAYEENAME", beneName);
				json.put("IFSC", beneIfsc);
				json.put("CREDITACC", beneAcNo);

				json.put("CORPID", corpId);
				json.put("AGGRID", aggrId);
				json.put("USERID", userId);
				json.put("URN", URN);
				json.put("AGGRNAME", aggrName);
				json.put("DEBITACC", accountNo);
				
				if (beneIfsc.contains("ICIC")){
					json.put(Constants.TXNTYPE, "TPA");
					json.put("IFSC", defaultIfsc);
					fields.put(FieldType.TXNTYPE.getName(),"TPA");
				}else{
					json.put(Constants.TXNTYPE, "RTG");
					json.put("IFSC", beneIfsc);
				}
				break;
			
			default:
				logger.info("invalid txn type in createRequestForTransaction() "+fields.get(FieldType.TXNTYPE.getName()));
				break;
			}

			logger.info("Created Transaction Request is " + json.toString() + " txn Id " + txnId + " "
					+ fields.getFields());
			return json.toString();
		} catch (Exception e) {
			logger.error("Exception in creating response " , e);
			return null;
		} finally {
			removeAccountDetails(fields);
		}
	}

	public String createRequestForTransactionInquiry(Fields fields) {
		logger.info("Creating Transaction Inquiry Request");

		try {

			String aggrId = propertiesManager.propertiesMap.get("AGGR_ID");
			String userId = propertiesManager.propertiesMap.get("USER_ID");
			String corpId = fields.get(FieldType.CORP_ID.getName());
			String URN = fields.get(FieldType.URN.getName());

			JSONObject json = new JSONObject();
			
			String txnType=fields.get(FieldType.TXNTYPE.getName());

			if(txnType.equalsIgnoreCase("IMPS")){
				String passcode = propertiesManager.propertiesMap.get("NODAL_IMPS_PASSCODE");
				String bcId = propertiesManager.propertiesMap.get("NODAL_IMPS_BCID");
				
				
				json.put(Constants.transRefNo, fields.get(FieldType.TXN_ID.getName()));
				json.put(Constants.passCode, passcode);
				json.put(Constants.bcID, bcId);
			}else{
				json.put(Constants.UNIQUEID, fields.get(FieldType.TXN_ID.getName()));
				json.put(Constants.AGGRID, aggrId);
				json.put(Constants.CORPID, corpId);
				json.put(Constants.USERID, userId);
				json.put(Constants.URN, URN);
			}
			

			logger.info("Created Transaction Inquiry Request is " + json.toString());
			return json.toString();
		} catch (Exception e) {
			logger.error("Exception in creating response ", e);
			return null;
		} finally {
			removeAccountDetails(fields);
		}
	}

	public String createRequestForAccountStatement(Fields fields) {
		logger.info("Creating Account Statement Request");

		try {

			String aggrId = propertiesManager.propertiesMap.get("AGGR_ID");
			String userId = propertiesManager.propertiesMap.get("USER_ID");
			String corpId = fields.get(FieldType.CORP_ID.getName());
			String URN = fields.get(FieldType.URN.getName());
			String accountNo = fields.get(FieldType.ACCOUNT_NO.getName());

			JSONObject json = new JSONObject();

			json.put(Constants.ACCOUNTNO, accountNo);
			json.put(Constants.FROMDATE, fields.get(FieldType.DATEFROM.getName()));
			json.put(Constants.TODATE, fields.get(FieldType.DATETO.getName()));
			json.put(Constants.AGGRID, aggrId);
			json.put(Constants.CORPID, corpId);
			json.put(Constants.USERID, userId);
			json.put(Constants.URN, URN);

			logger.info("Created Account Statement Request is " + json.toString());
			return json.toString();
		} catch (Exception e) {
			logger.error("Exception in creating request for acc statement ", e);
			return null;
		} finally {
			removeAccountDetails(fields);
		}
	}

	public String createRequestForBalanceInquiry(Fields fields) {
		logger.info("Creating Balance Inquiry Request");

		try {

			String aggrName = propertiesManager.propertiesMap.get("AGGR_NAME");
			String aggrId = propertiesManager.propertiesMap.get("AGGR_ID");
			String userId = propertiesManager.propertiesMap.get("USER_ID");
			String corpId = fields.get(FieldType.CORP_ID.getName());
			String URN = fields.get(FieldType.URN.getName());
			String accountNo = fields.get(FieldType.ACCOUNT_NO.getName());

			JSONObject json = new JSONObject();

			json.put(Constants.AGGRID, aggrId);
			json.put(Constants.CORPID, corpId);
			json.put(Constants.USERID, userId);
			json.put(Constants.URN, URN);
			json.put(Constants.ACCOUNTNO, accountNo);

			logger.info("Created Balance Request is " + json.toString());
			return json.toString();
		} catch (Exception e) {
			logger.error("Exception in creating response ", e);
			return null;
		} finally {
			removeAccountDetails(fields);
		}
	}

	public String createRequestForRegistration(Fields fields) {
		// logger.info("Creating Registration Request");

		try {

			JSONObject json = new JSONObject();

			json.put(Constants.AGGRID, fields.get(FieldType.AGGR_ID.getName()));
			json.put(Constants.AGGRNAME, fields.get(FieldType.AGGR_NAME.getName()));
			json.put(Constants.CORPID, fields.get(FieldType.CORP_ID.getName()));
			json.put(Constants.USERID, fields.get(FieldType.USER_ID.getName()));
			json.put(Constants.URN, fields.get(FieldType.URN.getName()));
			json.put(Constants.ALIASID, fields.get(FieldType.ALIAS_ID.getName()));

			logger.info("Created Registration Request is " + json.toString());
			return json.toString();
		} catch (Exception e) {
			logger.error("Exception in creating response ", e);
			return null;
		}
	}

	public String createRequestForRegistrationStatus(Fields fields) {
		// logger.info("Creating Registration Status Request");

		try {

			JSONObject json = new JSONObject();

			json.put(Constants.AGGRID, fields.get(FieldType.AGGR_ID.getName()));
			json.put(Constants.AGGRNAME, fields.get(FieldType.AGGR_NAME.getName()));
			json.put(Constants.CORPID, fields.get(FieldType.CORP_ID.getName()));
			json.put(Constants.USERID, fields.get(FieldType.USER_ID.getName()));
			json.put(Constants.URN, fields.get(FieldType.URN.getName()));

			logger.info("Created Registration Status Request is " + json.toString());
			return json.toString();
		} catch (Exception e) {
			logger.error("Exception in creating response ", e);
			return null;
		}
	}

	public String createIMPSRequest(Fields fields) {

		StringBuilder request = new StringBuilder();
		String payId = propertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");
		String encPassCode = propertiesManager.propertiesMap.get("ICICI_IMPS_PASSCODE");
		String decrptedPassCode = encryptDecryptService.decrypt(payId, encPassCode);
		String retailerCode = propertiesManager.propertiesMap.get("ICICI_IMPS_RETAILER_CODE");
		String remitterMobileNo = propertiesManager.propertiesMap.get("REMITTER_MOBILE");
		String remitterName = propertiesManager.propertiesMap.get("REMITTER_NAME");

		String txnId = fields.get(FieldType.TXN_ID.getName());

		if (StringUtils.isBlank(txnId)) {
			txnId = TransactionManager.getNewTransactionId();
			fields.put(FieldType.TXN_ID.getName(), txnId);
		}

		request.append(Constants.BENEFICIARY_ACC_NO);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.BENE_ACCOUNT_NO.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.BENEFICIARY_IFSC);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.IFSC_CODE.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.AMOUNT);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.AMOUNT.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.TRAN_REF_NO);
		request.append(Constants.EQUATOR);
		request.append(txnId);
		request.append(Constants.SEPARATOR);
		request.append(Constants.PAYMENT_REF);
		request.append(Constants.EQUATOR);
		request.append(Constants.PAYMENT_TYPE);
		request.append(Constants.SEPARATOR);
		request.append(Constants.REMITTER_NAME);
		request.append(Constants.EQUATOR);
		request.append(remitterName);
		request.append(Constants.SEPARATOR);
		request.append(Constants.REMITTER_MOBILE);
		request.append(Constants.EQUATOR);
		request.append(remitterMobileNo);
		request.append(Constants.SEPARATOR);
		request.append(Constants.RETAILER_CODE);
		request.append(Constants.EQUATOR);
		request.append(retailerCode);
		request.append(Constants.SEPARATOR);
		request.append(Constants.PASSCODE);
		request.append(Constants.EQUATOR);
		request.append(decrptedPassCode);
		logger.info("ICICI IMPS request : " + request.toString());
		return request.toString();
	}

	public boolean validateHash(Fields fields) throws SystemException {
		String bankAccountNumber = fields.get(FieldType.BENE_ACCOUNT_NO.getName());
		String bankAccountName = fields.get(FieldType.BENE_NAME.getName());
		String bankIfsc = fields.get(FieldType.IFSC_CODE.getName());
		String amount = fields.get(FieldType.AMOUNT.getName());
		String payId = fields.get(FieldType.PAY_ID.getName());
		String capturedDateFrom = fields.get(FieldType.CAPTURED_DATE_FROM.getName());
		String capturedDateTo = fields.get(FieldType.CAPTURED_DATE_TO.getName());
		String settledDate = fields.get(FieldType.SETTLED_DATE.getName());
		String mobileNo = fields.get(FieldType.PHONE_NO.getName());
		String userType = fields.get(FieldType.USER_TYPE.getName());
		String orderId = fields.get(FieldType.ORDER_ID.getName());

		String adminPayId = propertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");
		String salt = PropertiesManager.saltStore.get(adminPayId);
		if (StringUtils.isBlank(salt)) {
			salt = (new PropertiesManager()).getSalt(adminPayId);
			if (salt != null) {
				// logger.info("Salt found from propertiesManager for payId ");
			}

		} else {
			// Shaiwal - No need to log everytime
			// logger.info("Salt found from static map in propertiesManager");
		}

		String hashString;
		if (userType.equalsIgnoreCase("Merchant Initiated Direct")) {
			hashString = fields.get(FieldType.HASH.getName());
			if (!hashString.equals(fields.get(FieldType.HASH.getName()))) {
				return false;
			}
			return true;
		} else {
			hashString = bankAccountNumber + bankAccountName + bankIfsc + amount + payId + capturedDateFrom
					+ capturedDateTo + settledDate + mobileNo + userType + orderId + salt;
		}
		String hash = Hasher.getHash(hashString);
		if (!hash.equals(fields.get(FieldType.HASH.getName()))) {
			return false;
		}
		return true;

	}

	public boolean cibRegistrationValidateHash(Fields fields) throws SystemException {

		String aggrName = fields.get(FieldType.AGGR_NAME.getName());
		String aggrId = fields.get(FieldType.AGGR_ID.getName());
		String corpId = fields.get(FieldType.CORP_ID.getName());
		String userId = fields.get(FieldType.USER_ID.getName());
		String urn = fields.get(FieldType.URN.getName());

		String adminPayId = propertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");
		String salt = PropertiesManager.saltStore.get(adminPayId);

		if (StringUtils.isBlank(salt)) {
			salt = (new PropertiesManager()).getSalt(adminPayId);
			if (salt != null) {
				// logger.info("Salt found from propertiesManager for payId ");
			}

		} else {
			// logger.info("Salt found from static map in propertiesManager");
		}

		String hashString = FieldType.REQ_REGISTRATION.getName() + aggrName + aggrId + corpId + userId + urn + salt;
		String hash = Hasher.getHash(hashString);

		String fieldHash = fields.get(FieldType.HASH.getName());
		fields.remove(FieldType.HASH.getName());

		if (!hash.equalsIgnoreCase(fieldHash)) {
			logger.info("Hash mismatch , Calculated Hash == " + hash + " Merchant Hash == " + fieldHash);
			return false;
		}
		return true;

	}

	public boolean cibRegistrationStatusValidateHash(Fields fields) throws SystemException {
		String aggrName = fields.get(FieldType.AGGR_NAME.getName());
		String aggrId = fields.get(FieldType.AGGR_ID.getName());
		String corpId = fields.get(FieldType.CORP_ID.getName());
		String userId = fields.get(FieldType.USER_ID.getName());
		String urn = fields.get(FieldType.URN.getName());

		String adminPayId = propertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");
		String salt = PropertiesManager.saltStore.get(adminPayId);

		if (StringUtils.isBlank(salt)) {
			salt = (new PropertiesManager()).getSalt(adminPayId);
			if (salt != null) {
				// logger.info("Salt found from propertiesManager for payId ");
			}

		} else {
			// logger.info("Salt found from static map in propertiesManager");
		}

		String hashString = FieldType.REQ_REGISTRATION_STATUS.getName() + aggrName + aggrId + corpId + userId + urn
				+ salt;
		String hash = Hasher.getHash(hashString);

		String fieldHash = fields.get(FieldType.HASH.getName());
		fields.remove(FieldType.HASH.getName());

		if (!hash.equalsIgnoreCase(fieldHash)) {
			logger.info("Hash mismatch , Calculated Hash " + hash + " Merchant Hash " + fieldHash);
			return false;
		}
		return true;

	}

	public boolean cibAddBenValidateHash(Fields fields) throws SystemException {
		String bankAccountNumber = fields.get(FieldType.BENE_ACCOUNT_NO.getName());
		String bankAccountName = fields.get(FieldType.BENE_NAME.getName());
		String bankIfsc = fields.get(FieldType.IFSC.getName());
		String bankAccountNickName = fields.get(FieldType.BENE_ALIAS.getName());
		String payeeType = fields.get(FieldType.BENE_PAYEE_TYPE.getName());
		String payId = fields.get(FieldType.PAY_ID.getName());
		String subMerchantId = fields.get(FieldType.SUB_MERCHANT_ID.getName());
		boolean defaultBen = Boolean.valueOf(fields.get(FieldType.BENE_DEFAULT.getName()));

		if (StringUtils.isBlank(subMerchantId)) {
			subMerchantId = "";
		}

		String fieldHash = fields.get(FieldType.HASH.getName());
		fields.remove(FieldType.HASH.getName());

		String adminPayId = propertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");
		String salt = PropertiesManager.saltStore.get(adminPayId);

		if (StringUtils.isBlank(salt)) {
			salt = (new PropertiesManager()).getSalt(adminPayId);
			if (salt != null) {
				// logger.info("Salt found from propertiesManager for payId ");
			}

		} else {
			// logger.info("Salt found from static map in propertiesManager");
		}

		String hashString = FieldType.REQ_ADDBENE.getName() + payId + subMerchantId + bankAccountNumber
				+ bankAccountName + bankIfsc + bankAccountNickName + payeeType + defaultBen + salt;
		String hash = Hasher.getHash(hashString);
		if (!hash.equalsIgnoreCase(fieldHash)) {
			logger.info("Hash mismatch , Calculated Hash " + hash + " Merchant Hash " + fieldHash);
			return false;
		}
		return true;

	}

	public boolean cibTransactionValidateHash(Fields fields) throws SystemException {
		String bankAccountNumber = fields.get(FieldType.BENE_ACCOUNT_NO.getName());
		String bankAccountName = fields.get(FieldType.PAYEE_NAME.getName());
		String bankIfsc = fields.get(FieldType.IFSC.getName());
		String payId = fields.get(FieldType.PAY_ID.getName());
		String amount = fields.get(FieldType.AMOUNT.getName());
		String currency = fields.get(FieldType.CURRENCY_CODE.getName());
		String txnType = fields.get(FieldType.TXNTYPE.getName());
		String remarks = fields.get(FieldType.REMARKS.getName());
		String subMerchantId = fields.get(FieldType.SUB_MERCHANT_ID.getName());

		if (StringUtils.isBlank(remarks)) {
			remarks = "";
		}
		if (StringUtils.isBlank(subMerchantId)) {
			subMerchantId = "";
		}

		String adminPayId = propertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");
		String salt = PropertiesManager.saltStore.get(adminPayId);

		if (StringUtils.isBlank(salt)) {
			salt = (new PropertiesManager()).getSalt(adminPayId);
			if (salt != null) {
				logger.info("Salt found from propertiesManager for payId ");
			}

		} else {
			logger.info("Salt found from static map in propertiesManager");
		}

		String hashString = FieldType.REQ_TRANSACTION.getName() + payId + subMerchantId + bankAccountNumber
				+ bankAccountName + bankIfsc + amount + currency + txnType + remarks + salt;
		String hash = Hasher.getHash(hashString);

		String fieldHash = fields.get(FieldType.HASH.getName());
		fields.remove(FieldType.HASH.getName());

		if (!hash.equalsIgnoreCase(fieldHash)) {
			logger.info("Hash mismatch , Calculated Hash " + hash + " Merchant Hash " + fieldHash);
			return false;
		}
		return true;

	}

	public boolean cibBalInqValidateHash(Fields fields) throws SystemException {

		String adminPayId = propertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");
		String salt = PropertiesManager.saltStore.get(adminPayId);

		if (StringUtils.isBlank(salt)) {
			salt = (new PropertiesManager()).getSalt(adminPayId);
			if (salt != null) {
				// logger.info("Salt found from propertiesManager for payId ");
			}

		} else {
			// logger.info("Salt found from static map in propertiesManager");
		}

		String hashString = FieldType.REQ_BALANCE_INQUIRY.getName() + salt;
		String hash = Hasher.getHash(hashString);

		String fieldHash = fields.get(FieldType.HASH.getName());
		fields.remove(FieldType.HASH.getName());

		if (!hash.equalsIgnoreCase(fieldHash)) {
			logger.info("Hash mismatch , Calculated Hash == " + hash + " Merchant Hash == " + fieldHash);
			return false;
		}
		return true;

	}

	public boolean cibBeneStatusValidateHash(Fields fields) throws SystemException {
		String bankAccountNumber = fields.get(FieldType.BENE_ACCOUNT_NO.getName());

		String adminPayId = propertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");
		String salt = PropertiesManager.saltStore.get(adminPayId);

		if (StringUtils.isBlank(salt)) {
			salt = (new PropertiesManager()).getSalt(adminPayId);
			if (salt != null) {
				// logger.info("Salt found from propertiesManager for payId ");
			}

		} else {
			// logger.info("Salt found from static map in propertiesManager");
		}

		String hashString = FieldType.REQ_VALIDBENE.getName() + bankAccountNumber + salt;
		String hash = Hasher.getHash(hashString);

		String fieldHash = fields.get(FieldType.HASH.getName());
		fields.remove(FieldType.HASH.getName());

		if (!hash.equalsIgnoreCase(fieldHash)) {
			logger.info("Hash mismatch , Calculated Hash == " + hash + " Merchant Hash == " + fieldHash);
			return false;
		}
		return true;
	}

	public boolean cibTransactionInqValidateHash(Fields fields) throws SystemException {
		String txnId = fields.get(FieldType.TXN_ID.getName());

		String adminPayId = propertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");
		String salt = PropertiesManager.saltStore.get(adminPayId);

		if (StringUtils.isBlank(salt)) {
			salt = (new PropertiesManager()).getSalt(adminPayId);
			if (salt != null) {
				// logger.info("Salt found from propertiesManager for payId ");
			}

		} else {
			// logger.info("Salt found from static map in propertiesManager");
		}

		String hashString = FieldType.REQ_TRANSACTION_INQUIRY.getName() + txnId + salt;
		String hash = Hasher.getHash(hashString);

		String fieldHash = fields.get(FieldType.HASH.getName());
		fields.remove(FieldType.HASH.getName());

		if (!hash.equalsIgnoreCase(fieldHash)) {
			logger.info("Hash mismatch , Calculated Hash == " + hash + " Merchant Hash == " + fieldHash);
			return false;
		}
		return true;
	}

	public boolean cibAccountStatementValidateHash(Fields fields) throws SystemException {
		String fieldHash = fields.get(FieldType.HASH.getName());
		fields.remove(FieldType.HASH.getName());

		String dateFrom = fields.get(FieldType.DATEFROM.getName());
		String dateTo = fields.get(FieldType.DATETO.getName());

		String adminPayId = propertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");
		String salt = PropertiesManager.saltStore.get(adminPayId);

		if (StringUtils.isBlank(salt)) {
			salt = (new PropertiesManager()).getSalt(adminPayId);
			if (salt != null) {
				// logger.info("Salt found from propertiesManager for payId ");
			}

		} else {
			// logger.info("Salt found from static map in propertiesManager");
		}

		String hashString = FieldType.REQ_ACCOUNT_STATEMENT.getName() + dateFrom + dateTo + salt;
		String hash = Hasher.getHash(hashString);

		if (!hash.equalsIgnoreCase(fieldHash)) {
			logger.info("Hash mismatch , Calculated Hash == " + hash + " Merchant Hash == " + fieldHash);
			return false;
		}
		return true;

	}

	public String createIMPSStatusEnqRequest(Fields fields) {
		StringBuilder request = new StringBuilder();
		String payId = propertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");
		String encPassCode = propertiesManager.propertiesMap.get("ICICI_IMPS_PASSCODE");
		String decrptedPassCode = encryptDecryptService.decrypt(payId, encPassCode);

		request.append(Constants.TRAN_REF_NO);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.TXN_ID.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.PASSCODE);
		request.append(Constants.EQUATOR);
		request.append(decrptedPassCode);
		logger.info("ICICI IMPS status enq request : " + request.toString());
		return request.toString();
	}

	public Transaction toTransaction(String xml) {
		Transaction transaction = new Transaction();
		transaction.setActCode(getTextBetweenTags(xml, ACT_CODE_OPEN, ACT_CODE_CLOSE));
		transaction.setBeneName(getTextBetweenTags(xml, BENE_NAME_OPEN, BENE_NAME_CLOSE));
		transaction.setResponse(getTextBetweenTags(xml, RESPONSE_OPEN, RESPONSE_CLOSE));
		transaction.setRrn(getTextBetweenTags(xml, BANK_RRN_OPEN, BANK_RRN_CLOSE));
		transaction.setTranRefNo(getTextBetweenTags(xml, TRAN_REF_OPEN, TRAN_REF_CLOSE));
		return transaction;

	}

	public Transaction toCompositeImpsTransaction(String json) {
		Transaction transaction = new Transaction();
		logger.info("toCompositeImpsTransaction for IMPS composite " + json);
		try {
			if (StringUtils.isNotBlank(json)) {
				JSONObject data = new JSONObject(json);
				if (data.has("ImpsResponse")) {

					JSONObject subJson = (JSONObject) data.get("ImpsResponse");

					if (subJson.has("ActCode")) {
						transaction.setActCode(subJson.getString("ActCode"));
						if (subJson.has("success")) {
							transaction.setSuccess(subJson.getBoolean("success"));
						}
						if (subJson.has("Response")) {
							transaction.setResponse(subJson.getString("Response"));
						}
						if (subJson.has("TransRefNo")) {
							transaction.setTranRefNo(subJson.getString("TransRefNo"));
						}
						if (subJson.has("BankRRN")) {
							transaction.setRrn(subJson.getString("BankRRN"));
						}
						if (subJson.has("BeneName")) {
							transaction.setBeneName(subJson.getString("BeneName"));
						}
						if (subJson.has("TranRefNo")) {
							transaction.setTranRefNo(subJson.getString("TranRefNo"));
						}
					} else if (subJson.has("errorCode")) {
						transaction.setActCode(subJson.getString("errorCode"));
						transaction.setResponse(subJson.getString("description"));
					}
				} else {
					if (data.has("ActCode")) {
						transaction.setActCode(data.getString("ActCode"));
						if (data.has("success")) {
							transaction.setSuccess(data.getBoolean("success"));
						}
						if (data.has("Response")) {
							transaction.setResponse(data.getString("Response"));
						}
						if (data.has("TransRefNo")) {
							transaction.setTranRefNo(data.getString("TransRefNo"));
						}
						if (data.has("BankRRN")) {
							transaction.setRrn(data.getString("BankRRN"));
						}
						if (data.has("BeneName")) {
							transaction.setBeneName(data.getString("BeneName"));
						}
						if (data.has("TranRefNo")) {
							transaction.setTranRefNo(data.getString("TranRefNo"));
						}
					} else if (data.has("errorCode")) {
						transaction.setActCode(data.getString("errorCode"));
						transaction.setResponse(data.getString("description"));
					} else if (data.has("ErrorCode")) {
						transaction.setActCode(data.getString("ErrorCode"));
						transaction.setResponse(data.getString("Message"));
					} else if (data.has("success")) {
						if (data.has("success")) {
							transaction.setSuccess(data.getBoolean("success"));
						}
						transaction.setActCode(String.valueOf(data.getInt("response")));
						transaction.setResponse(data.getString("message"));
					}
				}

			}

		} catch (Exception e) {
			logger.error("exception in toCompositeImpsTransaction()", e);
		}
		return transaction;
	}

	public Transaction toCompositeUpiTransaction(String json) {
		Transaction transaction = new Transaction();
		try {
			if (StringUtils.isNotBlank(json)) {
				JSONObject data = new JSONObject(json);

				if (data.has("response")) {
					transaction.setSuccess(data.getBoolean("success"));
					if (data.has("response") && data.get("response") instanceof String)
						transaction.setResponse(data.getString("response"));
					else if (data.has("response") && data.get("response") instanceof Integer)
						transaction.setResponse(String.valueOf(data.getInt("response")));
					if (data.has("message"))
						transaction.setMessage(data.getString("message"));
					if (data.has("BankRRN"))
						transaction.setRrn(data.getString("BankRRN"));
					if (data.has("UpiTranlogId"))
						transaction.setTranLogId(data.getString("UpiTranlogId"));
					if (data.has("SeqNo"))
						transaction.setSeqNo(data.getString("SeqNo"));
					if (data.has("MobileAppData") && StringUtils.isNotBlank("MobileAppData")) {
						transaction.setMobileAppData(data.getString("MobileAppData"));
					}
				} else if (data.has("errorCode")) {
					transaction.setResponse(data.getString("errorCode"));
					transaction.setMessage(data.getString("description"));
				}
			}
		} catch (Exception e) {
			logger.error("exception toCompositeUpiTransaction() ", e);
		}
		return transaction;

	}

	public Transaction toCompositeBeneAddition(String json) {

		Transaction transaction = new Transaction();
		logger.info("Response is " + json);
		try {
			if (StringUtils.isNotBlank(json)) {
				JSONObject data = new JSONObject(json);

				if (StringUtils.isNotBlank(data.getString("Response"))) {
					if (data.getString("Response").equalsIgnoreCase("SUCCESS")) {
						transaction.setResponse(data.getString("Response"));
						transaction.setMessage(data.getString("Message"));
						if (data.has("BNF_ID"))
							transaction.setBnfId(data.getString("BNF_ID"));

					} else {
						transaction.setResponse(data.getString("Response"));
						transaction.setMessage(data.getString("Message"));
						transaction.setErrorCode(data.getString("ErrorCode"));
					}

				} else {
					logger.info("response field not found " + json.toString());
				}
			}
		} catch (Exception e) {
			logger.error("exception in toCompositeBeneAddition() ", e);
		}

		return transaction;

	}

	public String getTextBetweenTags(String text, String tag1, String tag2) {

		int leftIndex = text.indexOf(tag1);
		if (leftIndex == -1) {
			return null;
		}

		int rightIndex = text.indexOf(tag2);
		if (rightIndex != -1) {
			leftIndex = leftIndex + tag1.length();
			return text.substring(leftIndex, rightIndex);
		}

		return null;
	}// getTextBetweenTags()

	public String getTxnTypeForCIB(String TxnType) {
		String txnType = null;

		if (TxnType.equalsIgnoreCase("RTGS")) {
			txnType = "RTG";
		} else if (TxnType.equalsIgnoreCase("NEFT")) {
			txnType = "RGS";
		} else if (TxnType.equalsIgnoreCase("IMPS")) {
			txnType = "IFS";
		} else if (TxnType.equalsIgnoreCase("OWN")) {
			txnType = "OWN";
		} else if (TxnType.equalsIgnoreCase("TPA")) {
			txnType = "TPA";
		} else if (TxnType.equalsIgnoreCase("VAP")) {
			txnType = "VAP";
		}
		return txnType;
	}

	public void getNodalDetails(Fields fields) {

		String corpId = propertiesManager.propertiesMap.get("NODAL_CORP_ID");
		String URN = propertiesManager.propertiesMap.get("NODAL_URN");
		String nodalAccNo = propertiesManager.propertiesMap.get("NODAL_ACC_NO");
		String nodalAliasId = propertiesManager.propertiesMap.get("NODAL_ALIAS_ID");

		fields.put(FieldType.CORP_ID.getName(), corpId);
		fields.put(FieldType.URN.getName(), URN);
		fields.put(FieldType.ACCOUNT_NO.getName(), nodalAccNo);
		fields.put(FieldType.ALIAS_ID.getName(), nodalAliasId);

	}

	public void getCurrentDetails(Fields fields) {

		String corpId = propertiesManager.propertiesMap.get("CURRENT_CORP_ID");
		String URN = propertiesManager.propertiesMap.get("CURRENT_URN");
		String currentAccNo = propertiesManager.propertiesMap.get("CURRENT_ACC_NO");
		String currentAliasId = propertiesManager.propertiesMap.get("NODAL_ALIAS_ID");

		fields.put(FieldType.CORP_ID.getName(), corpId);
		fields.put(FieldType.URN.getName(), URN);
		fields.put(FieldType.ACCOUNT_NO.getName(), currentAccNo);
		fields.put(FieldType.ALIAS_ID.getName(), currentAliasId);

	}

	public void removeAccountDetails(Fields fields) {

		fields.remove(FieldType.CORP_ID.getName());
		fields.remove(FieldType.URN.getName());
		fields.remove(FieldType.ACCOUNT_NO.getName());
		fields.remove(FieldType.ALIAS_ID.getName());

	}

	public boolean validateHashForApi(Fields fields) throws SystemException {

		// For P2M Payouts, amount is in decimal , first convert to paise
		String st = PropertiesManager.propertiesMap.get("P2M_MERCHANT_PAYID");

		if (StringUtils.isNotBlank(st) && st.contains(FieldType.PAY_ID.getName())) {
			if (fields.get(FieldType.AMOUNT.getName()).contains(".")) {
				fields.put(FieldType.AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()).replace(".", ""));
			}
		}

		String fieldHash = fields.get(FieldType.HASH.getName());
		fields.remove(FieldType.HASH.getName());

		logger.info("Hash from Merchant  == " + fieldHash);
		String hash = Hasher.getHash(fields);
		logger.info("Calculated Hash == " + hash);
		if (!hash.equals(fieldHash)) {
			logger.info("Hash Mismatch , OrderId: " + fields.get(FieldType.ORDER_ID.getName()) + " Calculated hash ==  "
					+ hash + " Merchant hash == " + fieldHash + " Txn Id " + fields.get(FieldType.TXN_ID.getName()));
			return false;
		}
		fields.put(FieldType.HASH.getName(), fieldHash);
		return true;
	}

	public boolean validateFields(Fields fields) throws SystemException {
		String orderId = fields.get(FieldType.ORDER_ID.getName());
		String payId = fields.get(FieldType.PAY_ID.getName());
		String beneName = fields.get(FieldType.BENE_NAME.getName());
		String ifscCode = fields.get(FieldType.IFSC_CODE.getName());
		String beneAccountNo = fields.get(FieldType.BENE_ACCOUNT_NO.getName());
		String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());
		String phoneNo = fields.get(FieldType.PHONE_NO.getName());
		String amount = fields.get(FieldType.AMOUNT.getName());

		// logger.info("Checking Empty and Valid Fields for orderID :" +
		// orderId);

		if (StringUtils.isBlank(orderId) || !(crmValidator.validateField(CrmFieldType.ORDER_ID, orderId))) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_ORDER_ID.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_ORDER_ID.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid OrderId for Order Id " + fields.get(FieldType.ORDER_ID.getName()));
			return false;
		}

		if (StringUtils.isBlank(payId) || !(crmValidator.validateField(CrmFieldType.PAY_ID, payId))) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid PayId for Order Id " + fields.get(FieldType.ORDER_ID.getName()));

			return false;
		}

		if (StringUtils.isBlank(beneName) || !(crmValidator.validateField(CrmFieldType.ACC_HOLDER_NAME, beneName))) {

			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACC_HOLDER_NAME.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACC_HOLDER_NAME.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid Bene Name for Order Id " + fields.get(FieldType.ORDER_ID.getName()));

			return false;
		}

		if (StringUtils.isBlank(ifscCode) || !(crmValidator.validateField(CrmFieldType.IFSC_CODE, ifscCode))) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.IFSC_CODE.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.IFSC_CODE.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid IFSC for Order Id " + fields.get(FieldType.ORDER_ID.getName()));

			return false;
		}

		if (StringUtils.isBlank(beneAccountNo)
				|| !(crmValidator.validateField(CrmFieldType.ACCOUNT_NO, beneAccountNo))) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACCOUNT_NO.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACCOUNT_NO.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid Account No. for Order Id " + fields.get(FieldType.ORDER_ID.getName()));

			return false;
		}

		if (StringUtils.isBlank(currencyCode) || !(crmValidator.validateField(CrmFieldType.CURRENCY, currencyCode))) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.CURRENCY.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.CURRENCY.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid Currency Code for Order Id " + fields.get(FieldType.ORDER_ID.getName()));
			return false;
		}

		if (StringUtils.isNotBlank(phoneNo)) {
			if (!(crmValidator.validateField(CrmFieldType.MOBILE, phoneNo))) {

				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PHONE_NUMBER.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PHONE_NUMBER.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Invalid Mobile No for Order Id " + fields.get(FieldType.ORDER_ID.getName()));
				return false;
			}
		}
		if (StringUtils.isBlank(amount) || !(crmValidator.validateField(CrmFieldType.AMOUNT_OF_TRANSACTIONS, amount))) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_AMOUNT.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_AMOUNT.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid Amount for Order Id " + fields.get(FieldType.ORDER_ID.getName()));

			return false;
		}

		return true;
	}

	public boolean validateFieldsUPI(Fields fields) throws SystemException {
		String orderId = fields.get(FieldType.ORDER_ID.getName());
		String payId = fields.get(FieldType.PAY_ID.getName());
		String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());
		String phoneNo = fields.get(FieldType.PHONE_NO.getName());
		String amount = fields.get(FieldType.AMOUNT.getName());
		String vpa = fields.get(FieldType.PAYER_ADDRESS.getName());

		// logger.info("Checking Empty and Valid Fields for orderID :" +
		// orderId);

		if (StringUtils.isBlank(orderId) || !(crmValidator.validateField(CrmFieldType.ORDER_ID, orderId))) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_ORDER_ID.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_ORDER_ID.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid Order Id for Order Id " + fields.get(FieldType.ORDER_ID.getName()));

			return false;
		}

		if (StringUtils.isBlank(payId) || !(crmValidator.validateField(CrmFieldType.PAY_ID, payId))) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid PAY ID for Order Id " + fields.get(FieldType.ORDER_ID.getName()));
			return false;
		}

		if (StringUtils.isBlank(vpa) || !(crmValidator.validateField(CrmFieldType.VPA, vpa))) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_VPA.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_VPA.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid VPA for Order Id " + fields.get(FieldType.ORDER_ID.getName()));
			return false;
		}

		if (StringUtils.isBlank(currencyCode) || !(crmValidator.validateField(CrmFieldType.CURRENCY, currencyCode))) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.CURRENCY.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.CURRENCY.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid Currency Code for Order Id " + fields.get(FieldType.ORDER_ID.getName()));
			return false;
		}

		if (StringUtils.isNotBlank(phoneNo)) {
			if (!(crmValidator.validateField(CrmFieldType.MOBILE, phoneNo))) {

				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PHONE_NUMBER.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PHONE_NUMBER.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Invalid Mobile for Order Id " + fields.get(FieldType.ORDER_ID.getName()));
				return false;
			}
		}
		if (StringUtils.isBlank(amount) || !(crmValidator.validateField(CrmFieldType.AMOUNT_OF_TRANSACTIONS, amount))) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_AMOUNT.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_AMOUNT.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid Amount for Order Id " + fields.get(FieldType.ORDER_ID.getName()));
			return false;
		}

		return true;
	}

	public boolean validateFieldsForAddBene(Fields fields) throws SystemException {
		String orderId = fields.get(FieldType.ORDER_ID.getName());
		String payId = fields.get(FieldType.PAY_ID.getName());
		String beneName = fields.get(FieldType.BENE_NAME.getName());
		String ifscCode = fields.get(FieldType.IFSC_CODE.getName());
		String beneAccountNo = fields.get(FieldType.BENE_ACCOUNT_NO.getName());
		String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());
		String phoneNo = fields.get(FieldType.PHONE_NO.getName());

		// logger.info("Checking Empty and Valid Fields for orderID :" +
		// orderId);

		if (StringUtils.isBlank(orderId) || !(crmValidator.validateField(CrmFieldType.ORDER_ID, orderId))) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_ORDER_ID.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_ORDER_ID.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid OrderId for Order Id " + fields.get(FieldType.ORDER_ID.getName()));
			return false;
		}

		if (StringUtils.isBlank(payId) || !(crmValidator.validateField(CrmFieldType.PAY_ID, payId))) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid PayId for Order Id " + fields.get(FieldType.ORDER_ID.getName()));

			return false;
		}

		if (StringUtils.isBlank(beneName) || !(crmValidator.validateField(CrmFieldType.ACC_HOLDER_NAME, beneName))) {

			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACC_HOLDER_NAME.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACC_HOLDER_NAME.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid Bene Name for Order Id " + fields.get(FieldType.ORDER_ID.getName()));

			return false;
		}

		if (StringUtils.isBlank(ifscCode) || !(crmValidator.validateField(CrmFieldType.IFSC_CODE, ifscCode))) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.IFSC_CODE.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.IFSC_CODE.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid IFSC for Order Id " + fields.get(FieldType.ORDER_ID.getName()));

			return false;
		}

		if (StringUtils.isBlank(beneAccountNo)
				|| !(crmValidator.validateField(CrmFieldType.ACCOUNT_NO, beneAccountNo))) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACCOUNT_NO.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACCOUNT_NO.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid Account No. for Order Id " + fields.get(FieldType.ORDER_ID.getName()));

			return false;
		}

		if (StringUtils.isBlank(currencyCode) || !(crmValidator.validateField(CrmFieldType.CURRENCY, currencyCode))) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.CURRENCY.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.CURRENCY.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid Currency Code for Order Id " + fields.get(FieldType.ORDER_ID.getName()));
			return false;
		}

		if (StringUtils.isNotBlank(phoneNo)) {
			if (!(crmValidator.validateField(CrmFieldType.MOBILE, phoneNo))) {

				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PHONE_NUMBER.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PHONE_NUMBER.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Invalid Mobile No for Order Id " + fields.get(FieldType.ORDER_ID.getName()));
				return false;
			}
		}

		return true;
	}

	public boolean validateFieldsAddBeneUPI(Fields fields) throws SystemException {
		String orderId = fields.get(FieldType.ORDER_ID.getName());
		String payId = fields.get(FieldType.PAY_ID.getName());
		String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());
		String phoneNo = fields.get(FieldType.PHONE_NO.getName());
		String vpa = fields.get(FieldType.PAYER_ADDRESS.getName());

		// logger.info("Checking Empty and Valid Fields for orderID :" +
		// orderId);

		if (StringUtils.isBlank(orderId) || !(crmValidator.validateField(CrmFieldType.ORDER_ID, orderId))) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_ORDER_ID.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_ORDER_ID.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid Order Id for Order Id " + fields.get(FieldType.ORDER_ID.getName()));

			return false;
		}

		if (StringUtils.isBlank(payId) || !(crmValidator.validateField(CrmFieldType.PAY_ID, payId))) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid PAY ID for Order Id " + fields.get(FieldType.ORDER_ID.getName()));
			return false;
		}

		if (StringUtils.isBlank(vpa) || !(crmValidator.validateField(CrmFieldType.VPA, vpa))) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_VPA.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_VPA.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid VPA for Order Id " + fields.get(FieldType.ORDER_ID.getName()));
			return false;
		}

		if (StringUtils.isBlank(currencyCode) || !(crmValidator.validateField(CrmFieldType.CURRENCY, currencyCode))) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.CURRENCY.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.CURRENCY.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			logger.info("Invalid Currency Code for Order Id " + fields.get(FieldType.ORDER_ID.getName()));
			return false;
		}

		if (StringUtils.isNotBlank(phoneNo)) {
			if (!(crmValidator.validateField(CrmFieldType.MOBILE, phoneNo))) {

				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PHONE_NUMBER.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PHONE_NUMBER.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Invalid Mobile for Order Id " + fields.get(FieldType.ORDER_ID.getName()));
				return false;
			}
		}

		return true;
	}

	public String createCompositeIMPSRequest(Fields fields) {
		JSONObject requestJson = new JSONObject();

		String aggrId = null;
		String userId = null;
		String corpId = null;
		String bcId;
		String passCode;
		String remitterMobileNo;

		if (iciciResponseHandler.isPaybleMerchant(fields)) {

			// No need of aggrId , userId and corpId for PAYBLE_MERCHANT

			
			// userId =
			// PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_USER_ID");
			// corpId =
			// PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_CORP_ID");
			//aggrId = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_AGGR_ID");
			bcId = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_IMPS_BCID");
			passCode = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_IMPS_PASSCODE");
			remitterMobileNo = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_REMITTER_MOBILE");
		} else {
			
			// userId =
			// PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_USER_ID");
			// corpId =
			// PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_CORP_ID");
			aggrId = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_AGGR_ID");
			bcId = PropertiesManager.propertiesMap.get("ICICI_IMPS_BCID");
			passCode = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_IMPS_PASSCODE");
			remitterMobileNo = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_REMITTER_MOBILE");
		}

		String retailerCode = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_IMPS_RETAILER_CODE");

		String txnId = fields.get(FieldType.TXN_ID.getName());

		if (StringUtils.isBlank(txnId)) {
			txnId = TransactionManager.getNewTransactionId();
			fields.put(FieldType.TXN_ID.getName(), txnId);
		}

		String txnDate = getDateForImpsTransaction();


		requestJson.put(Constants.PayMethod, "IMPS");
		requestJson.put("amount", fields.get(FieldType.AMOUNT.getName()));
		requestJson.put("localTxnDtTime", txnDate);
		requestJson.put("beneAccNo", fields.get(FieldType.BENE_ACCOUNT_NO.getName()));
		requestJson.put("beneIFSC", fields.get(FieldType.IFSC_CODE.getName()));
		requestJson.put("tranRefNo", txnId);
		requestJson.put("paymentRef", Constants.PAYMENT_TYPE);
		requestJson.put("senderName", fields.get(FieldType.BENE_NAME.getName()));
		requestJson.put("mobile", remitterMobileNo);
		requestJson.put("retailerCode", retailerCode);
		requestJson.put("passCode", passCode);
		requestJson.put("bcID", bcId);
		
		if(StringUtils.isNotBlank(aggrId)){
			requestJson.put("aggrId", aggrId);
		}

		logger.info("PAYBLE/Payment GateWay ICICI COMPOSITE IMPS request : " + requestJson.toString() + " txn id " + txnId);

		return requestJson.toString();
	}

	public String createCompositeIMPSStatusEnqRequest(Fields fields) {
		JSONObject requestJson = new JSONObject();

		String passCode;
		String bcId;

		if (iciciResponseHandler.isPaybleMerchant(fields)) {
			passCode = propertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_IMPS_PASSCODE");
			bcId = propertiesManager.propertiesMap.get("PAYBLE_ICICI_IMPS_BCID");
		} else {
			passCode = propertiesManager.propertiesMap.get("ICICI_COMPOSITE_IMPS_PASSCODE");
			bcId = propertiesManager.propertiesMap.get("ICICI_IMPS_BCID");
		}

		requestJson.put(Constants.transRefNo, fields.get(FieldType.TXN_ID.getName()));
		requestJson.put(Constants.passCode, passCode);
		requestJson.put(Constants.bcID, bcId);

		logger.info("ICICI COMPOSITE IMPS Status Enq request : " + requestJson.toString());
		return requestJson.toString();
	}

	public String createCompositeUPIRequest(Fields fields) {

		String aggrId = null;
		String userId = null;
		String corpId = null;
		String URN;
		String deviceId;
		String mobileNo;
		String payerVa;
		String profileId;
		String channelCode;
		String mcc;

		if (iciciResponseHandler.isPaybleMerchant(fields)) {
			// aggrId =
			// PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_AGGR_ID");
			// userId =
			// PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_USER_ID");
			// corpId =
			// PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_CORP_ID");
			// URN =
			// PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_URN");
			deviceId = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_UPI_DEVICE_ID");
			payerVa = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_UPI_PAYER_VA");
			mobileNo = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_UPI_MOBILE");
			profileId = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_UPI_PROFILE_ID");
			channelCode = propertiesManager.propertiesMap.get("PAYBLE_ICICI_UPI_CHANNEL_CODE");
			mcc = propertiesManager.propertiesMap.get("PAYBLE_ICICI_UPI_MCC");
		} else {

			// userId =
			// PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_USER_ID");
			// corpId =
			// PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_CORP_ID");
			// URN = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_URN");
			aggrId = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_AGGR_ID");
			deviceId = PropertiesManager.propertiesMap.get("ICICI_UPI_DEVICE_ID");
			payerVa = PropertiesManager.propertiesMap.get("ICICI_UPI_PAYER_VA");
			mobileNo = PropertiesManager.propertiesMap.get("ICICI_UPI_MOBILE");
			profileId = PropertiesManager.propertiesMap.get("ICICI_UPI_PROFILE_ID");
			channelCode = propertiesManager.propertiesMap.get("ICICI_UPI_CHANNEL_CODE");
			mcc = propertiesManager.propertiesMap.get("ICICI_UPI_MCC");
		}

		String accountProvider = propertiesManager.propertiesMap.get("ICICI_UPI_ACCOUNT_PROVIDER");
		String defaultAcc = propertiesManager.propertiesMap.get("ICICI_UPI_DEFAULT_ACC");
		String preApproved = propertiesManager.propertiesMap.get("ICICI_UPI_PRE_APPROVED");
		String defaultDebit = propertiesManager.propertiesMap.get("ICICI_UPI_DEFAULT_DEBIT");
		String defaultCredit = propertiesManager.propertiesMap.get("ICICI_UPI_DEFAULT_CREDIT");
		String upiTxnType = propertiesManager.propertiesMap.get("ICICI_UPI_TXN_TYPE");
		String merchantType = propertiesManager.propertiesMap.get("ICICI_UPI_MERCHANT_TYPE");

		JSONObject json = new JSONObject();

		String txnId = fields.get(FieldType.TXN_ID.getName());

		if (StringUtils.isBlank(txnId)) {
			txnId = TransactionManager.getNewTransactionId();
			fields.put(FieldType.TXN_ID.getName(), txnId);
		}

		// CURRENTY Using current account
		// if (!iciciResponseHandler.isPaybleMerchant(fields)) {
		// json.put(Constants.DEVICE_ID, deviceId);
		// json.put(Constants.mobile, mobileNo);
		// json.put(Constants.CHANNEL_CODE, channelCode);
		// json.put(Constants.PROFILE_ID, profileId);
		// json.put(Constants.SEQ_NO, "ICI" + txnId);
		// json.put(Constants.ACCOUNT_PROVIDER, accountProvider);
		// json.put(Constants.USE_DEFAULT_ACC, defaultAcc);
		// json.put(Constants.PAYER_VA, payerVa);
		// json.put(Constants.PAYEE_VA,
		// fields.get(FieldType.PAYER_ADDRESS.getName()));
		// json.put(Constants.PAYEE_NAME,
		// fields.get(FieldType.PAYER_NAME.getName()));
		// json.put(Constants.UPI_AMOUNT,
		// fields.get(FieldType.AMOUNT.getName()));
		// json.put(Constants.PRE_APPROVED, preApproved);
		// json.put(Constants.DEFAULT_DEBIT, defaultDebit);
		// json.put(Constants.DEFAULT_CREDIT, defaultCredit);
		// json.put(Constants.TXN_TYPE, upiTxnType);
		// json.put(Constants.UPI_REMARKS, "NONE");
		// json.put(Constants.MCC, mcc);
		// json.put(Constants.MERCHANT_TYPE, merchantType);
		// json.put(Constants.VPA,
		// fields.get(FieldType.PAYER_ADDRESS.getName()));
		// json.put(Constants.UPI_AGGR_ID, aggrId);
		// json.put(Constants.UPI_CORP_ID, corpId);
		// json.put(Constants.UPI_USER_ID, userId);
		// } else {

		json.put(Constants.DEVICE_ID, deviceId);
		json.put(Constants.mobile, mobileNo);
		json.put(Constants.CHANNEL_CODE, channelCode);
		json.put(Constants.PROFILE_ID, profileId);
		json.put(Constants.SEQ_NO, "ICI" + txnId);
		json.put(Constants.ACCOUNT_PROVIDER, accountProvider);
		json.put(Constants.USE_DEFAULT_ACC, defaultAcc);
		json.put(Constants.PAYER_VA, payerVa);
		json.put(Constants.PAYEE_VA, fields.get(FieldType.PAYER_ADDRESS.getName()));
		json.put(Constants.UPI_AMOUNT, fields.get(FieldType.AMOUNT.getName()));
		json.put(Constants.PRE_APPROVED, preApproved);
		json.put(Constants.DEFAULT_DEBIT, defaultDebit);
		json.put(Constants.DEFAULT_CREDIT, defaultCredit);
		json.put(Constants.TXN_TYPE, upiTxnType);
		json.put(Constants.UPI_REMARKS, "none");
		json.put(Constants.MCC, mcc);
		json.put(Constants.MERCHANT_TYPE, merchantType);

		if (StringUtils.isNotBlank(aggrId))
			json.put(Constants.UPI_AGGR_ID, aggrId);

		logger.info("Created UPI Transaction Request is " + json.toString());
		return json.toString();
	}

	public String createCompositeUPIStatusEnqRequest(Fields fields) {

		String deviceId;
		String mobileNo;
		String profileId;

		if (iciciResponseHandler.isPaybleMerchant(fields)) {
			deviceId = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_UPI_DEVICE_ID");
			mobileNo = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_UPI_MOBILE");
			profileId = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_UPI_PROFILE_ID");
		} else {
			deviceId = PropertiesManager.propertiesMap.get("ICICI_UPI_DEVICE_ID");
			mobileNo = PropertiesManager.propertiesMap.get("ICICI_UPI_MOBILE");
			profileId = PropertiesManager.propertiesMap.get("ICICI_UPI_PROFILE_ID");
		}

		String channelCode = propertiesManager.propertiesMap.get("ICICI_UPI_CHANNEL_CODE");

		String txnId = "ICI" + TransactionManager.getNewTransactionId();

		JSONObject json = new JSONObject();

		json.put(Constants.DEVICE_ID, deviceId);
		json.put(Constants.mobile, mobileNo);
		json.put(Constants.CHANNEL_CODE, channelCode);
		json.put(Constants.PROFILE_ID, profileId);
		json.put(Constants.SEQ_NO, txnId);
		json.put(Constants.ORI_SEQ_NO, "ICI" + fields.get(FieldType.TXN_ID.getName()));

		logger.info("ICICI COMPOSITE UPI Status Enq request : " + json.toString() + "Txn Id "
				+ fields.get(FieldType.TXN_ID.getName()));
		return json.toString();
	}

	public String createCompositeNEFTRequest(Fields fields) {

		String aggrName = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_AGGR_NAME");
		String aggrId = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_AGGR_ID");
		String userId = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_USER_ID");
		String corpId = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_CORP_ID");
		String URN = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_URN");

		String accountNo = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_DEBIT_ACCOUNT_NO");

		String defaultIfsc = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_DEFAULT_IFSC");
		String currency = Currency.getAlphabaticCode(fields.get(FieldType.CURRENCY_CODE.getName()));
		String ifsc = fields.get(FieldType.IFSC_CODE.getName());

		JSONObject json = new JSONObject();

		String txnId = TransactionManager.getNewTransactionId();

		fields.put(FieldType.TXN_ID.getName(), txnId);

		json.put(Constants.tranRefNo, txnId);
		json.put(Constants.senderAcctNo, accountNo);
		json.put(Constants.beneAccNo, fields.get(FieldType.BENE_ACCOUNT_NO.getName()));

		json.put(Constants.beneName, fields.get(FieldType.BENE_NAME.getName()));
		json.put(Constants.amount, fields.get(FieldType.AMOUNT.getName()));
		json.put(Constants.narration1, fields.get(FieldType.BENE_NAME.getName()));

		if (ifsc.contains("ICIC")) {
			json.put(Constants.txnType, "TPA");
			json.put(Constants.beneIFSC, defaultIfsc);
		} else {
			json.put(Constants.txnType, "RGS");
			json.put(Constants.beneIFSC, ifsc);
		}

		// only for nodal account
		// json.put(Constants.WORKFLOW_REQD, "N");

		json.put(Constants.NEFT_AGGR_ID, aggrId);
		json.put(Constants.NEFT_AGGR_NAME, aggrName);
		json.put(Constants.NEFT_CORP_ID, corpId);
		json.put(Constants.NEFT_CORP_USER, userId);
		json.put(Constants.NEFT_URN, URN);

		logger.info("Created Transaction Request is " + json.toString());
		return json.toString();
	}

	public String createCompositeRTGSRequest(Fields fields) {

		String aggrName = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_AGGR_NAME");
		String aggrId = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_AGGR_ID");
		String userId = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_USER_ID");
		String corpId = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_CORP_ID");
		String URN = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_URN");

		String accountNo = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_DEBIT_ACCOUNT_NO");

		String defaultIfsc = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_DEFAULT_IFSC");

		String currency = Currency.getAlphabaticCode(fields.get(FieldType.CURRENCY_CODE.getName()));

		String ifsc = fields.get(FieldType.IFSC_CODE.getName());

		JSONObject json = new JSONObject();

		String txnId = TransactionManager.getNewTransactionId();

		fields.put(FieldType.TXN_ID.getName(), txnId);

		json.put(Constants.UNIQUEID, txnId);
		json.put(Constants.DEBITACC, accountNo);
		json.put(Constants.CREDITACC, fields.get(FieldType.BENE_ACCOUNT_NO.getName()));

		json.put(Constants.TRANSCTION_AMOUNT, fields.get(FieldType.AMOUNT.getName()));
		json.put(Constants.CURRENCY, currency);

		if (ifsc.contains("ICIC")) {
			json.put(Constants.TXNTYPE, "TPA");
			json.put(Constants.IFSC, defaultIfsc);
		} else {
			json.put(Constants.TXNTYPE, "RTG");
			json.put(Constants.IFSC, ifsc);
		}

		json.put(Constants.PAYEENAME, fields.get(FieldType.BENE_NAME.getName()));
		if (StringUtils.isBlank(fields.get(FieldType.REMARKS.getName())))
			json.put(Constants.REMARKS, "Payment GateWay Transaction");
		else
			json.put(Constants.REMARKS, fields.get(FieldType.REMARKS.getName()));

		json.put(Constants.WORKFLOW_REQD, "N");

		json.put(Constants.AGGRID, aggrId);
		json.put(Constants.AGGRNAME, aggrName);
		json.put(Constants.CORPID, corpId);
		json.put(Constants.USERID, userId);
		json.put(Constants.URN, URN);

		logger.info("Created RTGS Transaction Request is " + json.toString());
		return json.toString();
	}

	private String getDateForImpsTransaction() {
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddHHmmss");

		return sdf.format(new Date());

	}

	public String createCompositeIMPSRequestForAddBene(Fields fields) {
		// logger.info("Creating Composite Add Beneficiry Request for IMPS");

		try {

			String aggrId;
			String userId;
			String corpId;
			String URN;

			if (iciciResponseHandler.isPaybleMerchant(fields)) {
				aggrId = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_AGGR_ID");
				userId = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_USER_ID");
				corpId = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_CORP_ID");
				URN = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_URN");
			} else {
				aggrId = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_AGGR_ID");
				userId = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_USER_ID");
				corpId = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_CORP_ID");
				URN = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_URN");
			}

			JSONObject json = new JSONObject();

			json.put(Constants.BENE_AGGR_ID, aggrId);
			json.put(Constants.BENE_CORP_ID, corpId);
			json.put(Constants.BENE_CORP_USER, userId);
			json.put(Constants.URN, URN);
			json.put(Constants.BENE_ACC_NO, fields.get(FieldType.BENE_ACCOUNT_NO.getName()));
			json.put(Constants.BENE_NAME, fields.get(FieldType.BENE_NAME.getName()));
			json.put(Constants.BENE_NICKNAME, fields.get(FieldType.BENE_ACCOUNT_NO.getName()));

			if (fields.get(FieldType.IFSC_CODE.getName()).contains("ICIC")) {
				json.put(Constants.BENE_PAYEE_TYPE, "W");
			} else {
				json.put(Constants.BENE_PAYEE_TYPE, "O");
			}

			// json.put("NetworkId","WIB");

			json.put(Constants.BENE_IFSC, fields.get(FieldType.IFSC_CODE.getName()));

			logger.info("Created Add Beneficiry Request is " + fields.maskFields(json.toString()) + " Txn ID "
					+ fields.get(FieldType.TXN_ID.getName()));
			return json.toString();
		} catch (Exception e) {
			logger.error("Exception in creating Composite Beneficiary Addition Request " + " Txn ID "
					+ fields.get(FieldType.TXN_ID.getName()) + " ", e);
			return null;
		}
	}

	public String createCompositeUPIRequestForAddBene(Fields fields) {
		// logger.info("Creating Composite Add Beneficiry Request for UPI");

		try {

			String aggrId;
			String userId;
			String corpId;
			String URN;

			if (iciciResponseHandler.isPaybleMerchant(fields)) {
				aggrId = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_AGGR_ID");
				userId = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_USER_ID");
				corpId = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_CORP_ID");
				URN = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_URN");
			} else {
				aggrId = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_AGGR_ID");
				userId = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_USER_ID");
				corpId = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_CORP_ID");
				URN = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_URN");
			}

			JSONObject json = new JSONObject();

			json.put(Constants.UPI_AGGR_ID, aggrId);
			json.put(Constants.UPI_BENE_CORPID, corpId);
			json.put(Constants.UPI_USER_ID, userId);
			json.put(Constants.UPI_URN, URN);
			json.put(Constants.ADD_BENE_VPA, fields.get(FieldType.PAYER_ADDRESS.getName()));

			logger.info("Created Add Beneficiry Request is " + json.toString());
			return json.toString();
		} catch (Exception e) {
			logger.error("Exception in creating response ", e);
			return null;
		}
	}

	public String createCompositeAccountStatementRequest(Fields fields) {
		logger.info("Creating Composite Account Statement Request");

		try {

			String userType = fields.get(FieldType.USER_TYPE.getName());
			String fileType = fields.get(FieldType.FILE_TYPE.getName());

			String aggrId;
			String userId;
			String corpId;
			String URN;
			String accountNo;

			if (StringUtils.isNotBlank(fileType) && fileType.equalsIgnoreCase("Current")) {
				if (StringUtils.isNotBlank(userType) && userType.equalsIgnoreCase("Payble")) {
					aggrId = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_AGGR_ID");
					userId = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_USER_ID");
					corpId = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_CORP_ID");
					URN = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_URN");
					accountNo = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_DEBIT_ACCOUNT_NO");
				} else {
					aggrId = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_AGGR_ID");
					userId = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_USER_ID");
					corpId = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_CORP_ID");
					URN = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_URN");
					accountNo = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_DEBIT_ACCOUNT_NO");
				}
			} else {
				aggrId = PropertiesManager.propertiesMap.get("ICICI_NODAL_COMPOSITE_AGGR_ID");
				userId = PropertiesManager.propertiesMap.get("ICICI_NODAL_COMPOSITE_USER_ID");
				corpId = PropertiesManager.propertiesMap.get("ICICI_NODAL_COMPOSITE_CORP_ID");
				URN = PropertiesManager.propertiesMap.get("ICICI_NODAL_COMPOSITE_URN");
				accountNo = PropertiesManager.propertiesMap.get("ICICI_NODAL_COMPOSITE_DEBIT_ACCOUNT_NO");
			}

			JSONObject requestJson = new JSONObject();

			requestJson.put(Constants.ACCOUNTNO, accountNo);
			requestJson.put(Constants.FROMDATE, fields.get(FieldType.DATEFROM.getName()));
			requestJson.put(Constants.TODATE, fields.get(FieldType.DATETO.getName()));
			requestJson.put(Constants.AGGRID, aggrId);
			requestJson.put(Constants.CORPID, corpId);
			requestJson.put(Constants.USERID, userId);
			requestJson.put(Constants.URN, URN);

			// CONFLG will be N for 1st hit then response will have LASTTRID
			// which will use in next request with CONFLG = Y
			if (StringUtils.isNotBlank(fields.get(Constants.LASTTRID))) {
				requestJson.put(Constants.LASTTRID, fields.get(Constants.LASTTRID));
				requestJson.put(Constants.CONFLG, "Y");
			} else {
				requestJson.put(Constants.CONFLG, "N");
			}

			logger.info("Created Composite Account Statement Request is " + requestJson.toString());
			return requestJson.toString();
		} catch (Exception e) {
			logger.error("Exception in creating Composite Account Statement request ", e);
			return null;
		}
	}

	public void maskFields(String request) {
		JSONObject jsonReq = new JSONObject(request);
		for (String key : jsonReq.keySet()) {
			if (key.equalsIgnoreCase("beneAccNo")) {
				String reqValue = fields.fieldMask(jsonReq.getString(key));
				jsonReq.put("beneAccNo", reqValue);
			} else if (key.equalsIgnoreCase("beneIFSC")) {
				String reqValue = fields.fieldMask(jsonReq.getString(key));
				jsonReq.put("beneIFSC", reqValue);
			} else if (key.equalsIgnoreCase("mobile")) {
				String reqValue = fields.fieldMask(jsonReq.getString(key));
				jsonReq.put("mobile", reqValue);
			}
		}
		logger.info("ICICI COMPOSITE IMPS request : " + jsonReq.toString());
	}

	public Transaction toCompositeRtgsNeftTransaction(String json) {
		Transaction transaction = new Transaction();
		if (StringUtils.isNotBlank(json)) {
			JSONObject data = new JSONObject(json);

			if (data.has("STATUS") && data.getString("STATUS").equalsIgnoreCase("SUCCESS")) {
				transaction.setStatus(data.getString("STATUS"));
				if (data.has("RESPONSE"))
					transaction.setResponse(data.getString("RESPONSE"));
				if (data.has("UTRNUMBER"))
					transaction.setUtr(data.getString("UTRNUMBER"));
				if (data.has("UTR"))
					transaction.setUtr(data.getString("UTR"));
				if (data.has("REQID"))
					transaction.setReqId(data.getString("REQID"));
				if (data.has("UNIQUEID"))
					transaction.setUniqueId(data.getString("UNIQUEID"));
			} else if (data.has("ERRORCODE")) {
				transaction.setErrorCode(data.getString("ERRORCODE"));
				if (data.has("STATUS"))
					transaction.setStatus(data.getString("STATUS"));
				if (data.has("MESSAGE"))
					transaction.setMessage(data.getString("MESSAGE"));
				if (data.has("RESPONSECODE"))
					transaction.setResponseCode(data.getString("RESPONSECODE"));
				if (data.has("RESPONSE"))
					transaction.setResponse(data.getString("RESPONSE"));
			} else {
				if (data.has("STATUS"))
					transaction.setStatus(data.getString("STATUS"));
				if (data.has("MESSAGE"))
					transaction.setMessage(data.getString("MESSAGE"));
				if (data.has("RESPONSE"))
					transaction.setResponse(data.getString("RESPONSE"));
			}
		}
		return transaction;

	}

	public String createStatusCheckNeftRtgs(Fields fields) {

		String aggrId = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_AGGR_ID");
		String userId = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_USER_ID");
		String corpId = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_CORP_ID");
		String URN = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_URN");

		JSONObject json = new JSONObject();

		json.put(Constants.UNIQUEID, fields.get(FieldType.TXN_ID.getName()));

		json.put(Constants.AGGRID, aggrId);
		json.put(Constants.CORPID, corpId);
		json.put(Constants.USERID, userId);
		json.put(Constants.URN, URN);

		logger.info("Created RTGS Transaction Request is " + json.toString());
		return json.toString();
	}

	public String getPriorityforCIBComposite(String txnType) {
		if(txnType.equalsIgnoreCase("IMPS")){
			return "0100";
		}else if(txnType.equalsIgnoreCase("NEFT")){
			return "0010";
		}else if(txnType.equalsIgnoreCase("RTGS")){
			return "0001";
		}
		return null;
	}
}
