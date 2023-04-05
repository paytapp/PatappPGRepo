package com.paymentgateway.icici.composite.api;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.dao.PayoutAcquirerMappingDao;
import com.paymentgateway.commons.dao.SUFDetailDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.CoinSwitchCustomer;
import com.paymentgateway.commons.user.PayoutAcquireMapping;
import com.paymentgateway.commons.user.PayoutAcquirer;
import com.paymentgateway.commons.user.SUFDetail;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.payout.VendorPayoutDao;
import com.paymentgateway.pg.core.util.IciciUtil;

@Service
public class IciciResponseHandler {

	private static Logger logger = LoggerFactory.getLogger(IciciResponseHandler.class.getName());

	@Autowired
	private Fields field;

	@Autowired
	private IciciUtil iciciUtils;

	@Autowired
	private IciciTransactionConverter iciciTransactionConverter;

	@Autowired
	private IciciApiTransformer iciciApiTransformer;

	@Autowired
	private IciciCompositeDao IciciCompositeDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private VendorPayoutDao vendorPayoutDao;

	@Autowired
	private UserSettingDao userSettingDao;
	
	@Autowired
	private PayoutAcquirerMappingDao payoutAcquirerMappingDao;

	public String eCollectionResponseHandler(JSONObject jsonRequest) {
		// logger.info("Inside eCollectionResponseHandler(), JSON Request for
		// decrytion is : " + jsonRequest);
		Fields fields = new Fields();
		String encryptedResponse = "";
		try {
			BigDecimal amt = new BigDecimal((String) jsonRequest.get("AMT")).setScale(2);

			fields.put("_id", TransactionManager.getNewTransactionId());

			fields.put(FieldType.CUSTOMER_CODE.getName(), (String) jsonRequest.get("CustomerCode"));
			fields.put(FieldType.VIRTUAL_AC_CODE.getName(), (String) jsonRequest.get("VirtualACCode"));
			fields.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(), (String) jsonRequest.get("CustomerAccountNo"));

			fields.put(FieldType.TXNTYPE.getName(), "COLLECTION");
			fields.put(FieldType.ORIG_TXNTYPE.getName(), "COLLECTION");
			fields.put(FieldType.PAYMENT_TYPE.getName(), (String) jsonRequest.get("MODE"));
			fields.put(FieldType.MOP_TYPE.getName(), (String) jsonRequest.get("MODE"));

			fields.put(FieldType.RRN.getName(), (String) jsonRequest.get("UTR"));
			fields.put(FieldType.SENDER_REMARK.getName(), (String) jsonRequest.get("SENDER REMARK"));

			fields.put(FieldType.AMOUNT.getName(), String.valueOf(amt));
			fields.put(FieldType.TOTAL_AMOUNT.getName(), String.valueOf(amt));
			fields.put(FieldType.PAYEE_NAME.getName(), (String) jsonRequest.get("PayeeName"));
			fields.put(FieldType.PAYEE_ACCOUNT_NUMBER.getName(), (String) jsonRequest.get("PayeeAccountNumber"));
			fields.put(FieldType.PAYEE_BANK_IFSC.getName(), (String) jsonRequest.get("PayeeBankIFSC"));
			fields.put(FieldType.PG_DATE_TIME.getName(), (String) jsonRequest.get("PayeePaymentDate"));
			fields.put(FieldType.ACQ_ID.getName(), (String) jsonRequest.get("BankInternalTransactionNumber"));

			String txnId = TransactionManager.getNewTransactionId();
			fields.put(FieldType.TXN_ID.getName(), txnId);
			fields.put(FieldType.PG_REF_NUM.getName(), txnId);
			fields.put(FieldType.OID.getName(), txnId);
			fields.put(FieldType.ORIG_TXN_ID.getName(), txnId);
			fields.put(FieldType.ORDER_ID.getName(), "LP" + txnId);
			fields.put(FieldType.CURRENCY_CODE.getName(), "356");
			fields.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), "000");
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), "SUCCESS");
			fields.put(FieldType.ACQUIRER_NAME.getName(), PayoutAcquirer.ICICI.name());

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			fields.put(FieldType.CREATE_DATE.getName(), dateNow);
			fields.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));

			if (StringUtils.isNotBlank((String) jsonRequest.get("VirtualACCode"))) {
				User user = userDao.findByVirtualAcc((String) jsonRequest.get("VirtualACCode"));

				if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
					fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
					fields.put(FieldType.SUB_MERCHANT_ID.getName(), user.getPayId());
					User user1 = userDao.findPayId(user.getSuperMerchantId());
					if (StringUtils.isNotBlank(user1.getResellerId())) {
						fields.put(FieldType.RESELLER_ID.getName(), user1.getResellerId());
					}
				} else {
					fields.put(FieldType.PAY_ID.getName(), user.getPayId());
					if (StringUtils.isNotBlank(user.getResellerId())) {
						fields.put(FieldType.RESELLER_ID.getName(), user.getResellerId());
					}
				}
			}

			// checking duplicate entry in db
			String isDuplicate = field.checkECollectionDuplicateResponse(fields);

			if (!StringUtils.isEmpty(isDuplicate) && isDuplicate.equals("N")) {

				vendorPayoutDao.updateSufCharges(fields);

				field.insertECollectionResponse(fields);
				encryptedResponse = createEncryptedResponse(isDuplicate);

			} else if (!StringUtils.isEmpty(isDuplicate) && isDuplicate.equals("Y")) {

				encryptedResponse = createEncryptedResponse(isDuplicate);
			}

		} catch (Exception exception) {
			logger.error("Exception in eCollectionResponseHandler", exception);
			return null;
		}
		return encryptedResponse;
	}

	public String eCollectionResponseHandlerCustom(JSONObject jsonRequest) {

		Fields fields = new Fields();
		String encryptedResponse = "";
		try {
			BigDecimal amt = new BigDecimal((String) jsonRequest.get("Amount")).setScale(2); // Done

			fields.put("_id", TransactionManager.getNewTransactionId());

			fields.put(FieldType.CUSTOMER_CODE.getName(), (String) jsonRequest.get("ClientCode")); // DONE
			fields.put(FieldType.VIRTUAL_AC_CODE.getName(), (String) jsonRequest.get("VirtualAccountNumber")); // DONE
			fields.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(), (String) jsonRequest.get("ClientAccountNo"));

			fields.put(FieldType.TXNTYPE.getName(), "COLLECTION");
			fields.put(FieldType.ORIG_TXNTYPE.getName(), "COLLECTION");
			fields.put(FieldType.PAYMENT_TYPE.getName(), (String) jsonRequest.get("Mode")); // DONE
			fields.put(FieldType.MOP_TYPE.getName(), (String) jsonRequest.get("Mode")); // DONE

			fields.put(FieldType.RRN.getName(), (String) jsonRequest.get("UTR")); // DONE
			fields.put(FieldType.SENDER_REMARK.getName(), (String) jsonRequest.get("SenderRemark"));

			fields.put(FieldType.AMOUNT.getName(), String.valueOf(amt));
			fields.put(FieldType.TOTAL_AMOUNT.getName(), String.valueOf(amt));
			fields.put(FieldType.PAYEE_NAME.getName(), (String) jsonRequest.get("PayerName"));
			fields.put(FieldType.PAYEE_ACCOUNT_NUMBER.getName(), (String) jsonRequest.get("PayerAccNumber"));
			fields.put(FieldType.PAYEE_BANK_IFSC.getName(), (String) jsonRequest.get("PayerBankIFSC"));
			fields.put(FieldType.PG_DATE_TIME.getName(), (String) jsonRequest.get("PayerPaymentDate"));
			fields.put(FieldType.ACQ_ID.getName(), (String) jsonRequest.get("BankInternalTransactionNumber"));

			String txnId = TransactionManager.getNewTransactionId();
			fields.put(FieldType.TXN_ID.getName(), txnId);
			fields.put(FieldType.PG_REF_NUM.getName(), txnId);
			fields.put(FieldType.OID.getName(), txnId);
			fields.put(FieldType.ORIG_TXN_ID.getName(), txnId);
			fields.put(FieldType.ORDER_ID.getName(), "LP" + txnId);
			fields.put(FieldType.CURRENCY_CODE.getName(), "356");
			fields.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), "000");
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), "SUCCESS");
			fields.put(FieldType.ACQUIRER_NAME.getName(), PayoutAcquirer.ICICI.name());

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			fields.put(FieldType.CREATE_DATE.getName(), dateNow);
			fields.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));

			if (StringUtils.isNotBlank((String) jsonRequest.get("VirtualAccountNumber"))) {
				User user = userDao.findByVirtualAcc((String) jsonRequest.get("VirtualAccountNumber"));

				if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
					fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
					fields.put(FieldType.SUB_MERCHANT_ID.getName(), user.getPayId());
					User user1 = userDao.findPayId(user.getSuperMerchantId());
					if (StringUtils.isNotBlank(user1.getResellerId())) {
						fields.put(FieldType.RESELLER_ID.getName(), user1.getResellerId());
					}
				} else {
					fields.put(FieldType.PAY_ID.getName(), user.getPayId());
					if (StringUtils.isNotBlank(user.getResellerId())) {
						fields.put(FieldType.RESELLER_ID.getName(), user.getResellerId());
					}
				}
			}

			// checking duplicate entry in db
			String isDuplicate = field.checkECollectionDuplicateResponse(fields);

			if (!StringUtils.isEmpty(isDuplicate) && isDuplicate.equals("N")) {
				vendorPayoutDao.updateSufCharges(fields);
				field.insertECollectionResponse(fields);
				encryptedResponse = createEncryptedResponseCustom(isDuplicate);

			} else if (!StringUtils.isEmpty(isDuplicate) && isDuplicate.equals("Y")) {

				encryptedResponse = createEncryptedResponseCustom(isDuplicate);
			}

		} catch (Exception exception) {
			logger.error("Exception in eCollectionResponseHandler", exception);
			return null;
		}
		return encryptedResponse;
	}

	public String eCollectionResponseHandlerComposite(JSONObject jsonRequest) {

		Fields fields = new Fields();
		String encryptedResponse = "";
		try {
			BigDecimal amt = new BigDecimal((String) jsonRequest.get("Amount")).setScale(2); // Done

			fields.put("_id", TransactionManager.getNewTransactionId());

			fields.put(FieldType.CUSTOMER_CODE.getName(), (String) jsonRequest.get("ClientCode")); // DONE
			fields.put(FieldType.VIRTUAL_AC_CODE.getName(), (String) jsonRequest.get("VirtualAccountNumber")); // DONE
			fields.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(), (String) jsonRequest.get("ClientAccountNo"));

			fields.put(FieldType.TXNTYPE.getName(), "COLLECTION");
			fields.put(FieldType.ORIG_TXNTYPE.getName(), "COLLECTION");
			fields.put(FieldType.PAYMENT_TYPE.getName(), (String) jsonRequest.get("Mode")); // DONE
			fields.put(FieldType.MOP_TYPE.getName(), (String) jsonRequest.get("Mode")); // DONE

			fields.put(FieldType.RRN.getName(), (String) jsonRequest.get("UTR")); // DONE
			fields.put(FieldType.SENDER_REMARK.getName(), (String) jsonRequest.get("SenderRemark"));

			fields.put(FieldType.AMOUNT.getName(), String.valueOf(amt));
			fields.put(FieldType.TOTAL_AMOUNT.getName(), String.valueOf(amt));
			fields.put(FieldType.PAYEE_NAME.getName(), (String) jsonRequest.get("PayerName"));
			fields.put(FieldType.PAYEE_ACCOUNT_NUMBER.getName(), (String) jsonRequest.get("PayerAccNumber"));
			fields.put(FieldType.PAYEE_BANK_IFSC.getName(), (String) jsonRequest.get("PayerBankIFSC"));
			fields.put(FieldType.PG_DATE_TIME.getName(), (String) jsonRequest.get("PayerPaymentDate"));
			fields.put(FieldType.ACQ_ID.getName(), (String) jsonRequest.get("BankInternalTransactionNumber"));

			String txnId = TransactionManager.getNewTransactionId();
			fields.put(FieldType.TXN_ID.getName(), txnId);
			fields.put(FieldType.PG_REF_NUM.getName(), txnId);
			fields.put(FieldType.OID.getName(), txnId);
			fields.put(FieldType.ORIG_TXN_ID.getName(), txnId);
			fields.put(FieldType.ORDER_ID.getName(), "LP" + txnId);
			fields.put(FieldType.CURRENCY_CODE.getName(), "356");
			fields.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), "000");
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), "SUCCESS");
			fields.put(FieldType.ACQUIRER_NAME.getName(), PayoutAcquirer.ICICI.name());
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			fields.put(FieldType.CREATE_DATE.getName(), dateNow);
			fields.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));

			if (StringUtils.isNotBlank((String) jsonRequest.get("VirtualAccountNumber"))) {
				PayoutAcquireMapping mapping = payoutAcquirerMappingDao.findMappingwithVirtualAcNo((String) jsonRequest.get("VirtualAccountNumber"));
				
				User user=userDao.findPayId(mapping.getPayId());

				if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
					fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
					fields.put(FieldType.SUB_MERCHANT_ID.getName(), user.getPayId());
					User user1 = userDao.findPayId(user.getSuperMerchantId());
					if (StringUtils.isNotBlank(user1.getResellerId())) {
						fields.put(FieldType.RESELLER_ID.getName(), user1.getResellerId());
					}
				} else {
					fields.put(FieldType.PAY_ID.getName(), user.getPayId());
					if (StringUtils.isNotBlank(user.getResellerId())) {
						fields.put(FieldType.RESELLER_ID.getName(), user.getResellerId());
					}
				}
			}

			// checking duplicate entry in db
			String isDuplicate = field.checkECollectionDuplicateResponse(fields);

			if (!StringUtils.isEmpty(isDuplicate) && isDuplicate.equals("N")) {

				vendorPayoutDao.updateSufCharges(fields);
				field.insertECollectionResponse(fields);
				encryptedResponse = createEncryptedResponseComposite(isDuplicate);

			} else if (!StringUtils.isEmpty(isDuplicate) && isDuplicate.equals("Y")) {

				encryptedResponse = createEncryptedResponseComposite(isDuplicate);
			}

		} catch (Exception exception) {
			logger.error("Exception in eCollectionResponseHandlerComposite", exception);
			return null;
		}
		return encryptedResponse;
	}

	public String createEncryptedResponse(String duplicacyResponse) throws IOException {
		logger.info("inside createECollectionResponse() If E-Collection Response is Duplicate in db : "
				+ duplicacyResponse);
		String response = null;
		try {

			JSONObject responseJson = new JSONObject();
			if (duplicacyResponse == "N") {
				responseJson.put("SuccessANDRejected", "Successful Transaction");
				responseJson.put("CODE", "11");
			} else {
				responseJson.put("SuccessANDRejected", "Duplicate UTR");
				responseJson.put("CODE", "06");
			}

			response = iciciUtils.eCollectionEncrypt(responseJson.toString());

			logger.info("ICICI E-Collection Encrypted response : " + response);

		} catch (Exception exception) {
			logger.error("Exception  ", exception);
			return null;
		}
		return response;
	}

	public String createEncryptedResponseCustom(String duplicacyResponse) throws IOException {
		logger.info("inside createECollectionResponse() If E-Collection Response is Duplicate in db : "
				+ duplicacyResponse);
		String response = null;
		try {

			JSONObject responseJson = new JSONObject();
			if (duplicacyResponse == "N") {
				responseJson.put("Response", "Successful Transaction");
				responseJson.put("Code", "11");
			} else {
				responseJson.put("Response", "Duplicate UTR");
				responseJson.put("Code", "06");
			}

			response = iciciUtils.ecollectionApiEncryptionPayble(responseJson.toString(), true);

			logger.info("ICICI E-Collection Encrypted response : " + response);

		} catch (Exception exception) {
			logger.error("Exception  ", exception);
			return null;
		}
		return response;
	}

	public String createEncryptedResponseComposite(String duplicacyResponse) throws IOException {
		logger.info("inside createEncryptedResponseComposite() If E-Collection Response is Duplicate in db : "
				+ duplicacyResponse);
		String response = null;
		try {

			JSONObject responseJson = new JSONObject();
			if (duplicacyResponse == "N") {
				responseJson.put("Response", "Successful Transaction");
				responseJson.put("Code", "11");
			} else {
				responseJson.put("Response", "Duplicate UTR");
				responseJson.put("Code", "06");
			}

			response = iciciUtils.ecollectionApiEncryptionComposite(responseJson.toString());

			logger.info("ICICI E-Collection Encrypted response : " + response);

		} catch (Exception exception) {
			logger.error("Exception  ", exception);
			return null;
		}
		return response;
	}

	public Map<String, String> impsProcess(Fields fields, String response) throws SystemException {
		Transaction transactionResponse = iciciTransactionConverter.toTransaction(response);
		iciciApiTransformer = new IciciApiTransformer(transactionResponse);
		iciciApiTransformer.updateResponse(fields);
		fields.put(FieldType.TXNTYPE.getName(), "IMPS");
		String payId = fields.get(FieldType.PAY_ID.getName());

		User user = userDao.findPayId(payId);

		if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
			fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
		}

		fields.put(FieldType.VIRTUAL_AC_CODE.getName(), user.getVirtualAccountNo());
		field.insertIciciIMPSTransaction(fields);
		try {
			if (fields.contains(FieldType.STATUS.getName())) {
				if (!fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {
					vendorPayoutDao.UpdateForClosing(fields);
				}
			}
		} catch (Exception e) {
			logger.error("Exception  ", e);
			return null;
		}
		// removed from here.
		// if
		// (StringUtils.isNotBlank(fields.get(FieldType.CAPTURED_DATE_FROM.getName()))
		// &&
		// StringUtils.isNotBlank(fields.get(FieldType.CAPTURED_DATE_TO.getName())))
		// {
		// field.updateUTRAndPayoutDate(fields, null);
		// }
		return fields.getFields();

	}

	public Map<String, String> impsStatusEnqProcess(Fields fields, String response) throws SystemException {
		Transaction transactionResponse = iciciTransactionConverter.toTransaction(response);
		iciciApiTransformer = new IciciApiTransformer(transactionResponse);
		iciciApiTransformer.updateResponse(fields);
		fields.put(FieldType.TXNTYPE.getName(), "IMPS");
		field.updateIciciIMPSTransaction(fields);

		return fields.getFields();

	}

	public Map<String, String> cibBeneResponseHandler(Fields fields, String response) throws SystemException {
		logger.info("inside cibBeneResponseHandler()");
		iciciApiTransformer.updateBeneResponse(fields, response);

		if (Boolean.valueOf(fields.get(FieldType.BENE_DEFAULT.getName()))) {
			field.updateIciciCibDefaultBeneFields(fields);
		}

		field.insertIciciCibBeneficiaryFields(fields);

		return fields.getFields();
	}

	public Map<String, String> cibRegResponseHandler(Fields fields, String response) throws SystemException {
		// logger.info("inside cibRegResponseHandler()");
		iciciApiTransformer.updateRegResponse(fields, response);

		return fields.getFields();
	}

	public Map<String, String> cibRegStatusResponseHandler(Fields fields, String response) throws SystemException {
		// logger.info("inside cibRegStatusResponseHandler()");
		iciciApiTransformer.updateRegStatusResponse(fields, response);

		return fields.getFields();
	}

	public Map<String, String> cibBeneStatusResponseHandler(Fields fields, String response) throws SystemException {
		// logger.info("inside cibBeneStatusResponseHandler()");
		iciciApiTransformer.updateBeneStatusResponse(fields, response);
		field.updateIciciCibBeneficiaryFields(fields);

		return fields.getFields();
	}

	public Map<String, String> cibTransactionResponseHandler(Fields fields, String response, User user)
			throws SystemException {
		// logger.info("inside cibTransactionResponseHandler()");
		iciciApiTransformer.updateTransactionResponse(fields, response);
		logger.info("updated fields are " + fields.getFields());
		field.insertIciciCibFields(fields);

		IciciCompositeDao.insertPayoutNodalTopupTransaction(fields, user);
		// removed from here.
		// field.updateUTRAndPayoutDate(fields, response);
		return fields.getFields();
	}

	public Map<String, String> cibTransactionResponseHandler(Fields fields, String response) throws SystemException {
		// logger.info("inside cibTransactionResponseHandler()");
		// iciciApiTransformer.updateTransactionResponse(fields, response);
		logger.info("updated fields are " + fields.getFields());

		String txnType = fields.get(FieldType.TXNTYPE.getName());

		if (txnType.equals(com.paymentgateway.icici.composite.api.Constants.IMPS)) {

			Transaction transactionResponse = iciciTransactionConverter.toCompositeImpsTransaction(response);
			iciciApiTransformer = new IciciApiTransformer(transactionResponse);
			iciciApiTransformer.updateIMPSCompositeTransactionResponse(fields);

		} else {
			Transaction transactionResponse = iciciTransactionConverter.toCompositeRtgsNeftTransaction(response);
			iciciApiTransformer = new IciciApiTransformer(transactionResponse);
			iciciApiTransformer.updateNEFTRTGSCompositeTransactionResponse(fields);
		}

		fields.put(FieldType.TRANSACTION_OF.getName(), "CIB_TRANSACTION");

		field.insertIciciCibFields(fields);
		// removed from here.
		// field.updateUTRAndPayoutDate(fields, response);
		return fields.getFields();
	}

	public Map<String, String> cibTransactionStatusResponseHandler(Fields fields, String response)
			throws SystemException {
		// logger.info("inside cibTransactionResponseHandler()");

//		fields.put(field.fetchPreviousCibFields(fields.get(FieldType.TXN_ID.getName())));
		String txnType = fields.get(FieldType.TXNTYPE.getName());

		if (txnType.equals(com.paymentgateway.icici.composite.api.Constants.IMPS)) {

			Transaction transactionResponse = iciciTransactionConverter.toCompositeImpsTransaction(response);
			iciciApiTransformer = new IciciApiTransformer(transactionResponse);
			iciciApiTransformer.updateIMPSCompositeTransactionResponse(fields);

		} else {
			Transaction transactionResponse = iciciTransactionConverter.toCompositeRtgsNeftTransaction(response);
			iciciApiTransformer = new IciciApiTransformer(transactionResponse);
			iciciApiTransformer.updateNEFTRTGSCompositeTransactionResponse(fields);
		}

		field.updateCibTransactionFields(fields);

		return fields.getFields();
	}

	public Map<String, String> cibTransactionStatusResponseHandler(Fields fields, String response, User user)
			throws SystemException {
		// logger.info("inside cibTransactionResponseHandler()");
		fields.put(field.fetchPreviousCibFields(fields.get(FieldType.TXN_ID.getName())));
		iciciApiTransformer.updateTransactionStatusResponse(fields, response);
		field.insertIciciCibFields(fields);

		IciciCompositeDao.checkStatusPayoutNodalTopupTransaction(fields, user);

		return fields.getFields();
	}

	public Map<String, String> cibBeneBalanceInqHandler(Fields fields, String response) throws SystemException {
		// logger.info("inside cibBeneBalanceInqHandler()");
		iciciApiTransformer.updateBalanceInqResponse(fields, response);

		return fields.getFields();
	}

	public Map<String, String> cibAccountStatementResponseHandler(Fields fields, String response)
			throws SystemException {
		iciciApiTransformer.updateAccountStatementResponse(fields, response);
		logger.info(fields.getFields() + " response " + response);
		return fields.getFields();
	}

	public void compositeIMPSTransactionResponseHandler(Fields fields, String response) throws SystemException {
		Transaction transactionResponse = iciciTransactionConverter.toCompositeImpsTransaction(response);
		iciciApiTransformer = new IciciApiTransformer(transactionResponse);
		iciciApiTransformer.updateIMPSCompositeTransactionResponse(fields);

		String payId = fields.get(FieldType.PAY_ID.getName());

		User user = userDao.findPayId(payId);

		fields.put(FieldType.VIRTUAL_AC_CODE.getName(), user.getVirtualAccountNo());
		if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
			fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
		}

		// For bene Account verification only
		if (StringUtils.isNotBlank(fields.get(FieldType.BENE_NAME_REQUEST.getName()))) {
			if (fields.get(FieldType.BENE_NAME_REQUEST.getName())
					.equalsIgnoreCase(fields.get(FieldType.BENE_NAME.getName()))) {
				fields.remove(FieldType.BENE_NAME_REQUEST.getName());
			}
		}

		// fields.put(FieldType.STATUS.getName(), "Timeout");
		// logger.info("fields for insert " + fields.getFields() + " txn id " +
		// fields.get(FieldType.TXN_ID.getName()));
		if (fields.contains("flagBulk")) {
			fields.remove("flagBulk");
			field.updateIciciIMPSBulkTransaction(fields);
		} else {
			field.insertIciciCompositeFields(fields);
		}

		// update Bene name in the Beneficiary data
		if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName()))
			IciciCompositeDao.updateBeneName(fields);

		try {
			if (fields.contains(FieldType.USER_TYPE.getName())) {
				if (StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName())) && (fields
						.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct")
						|| fields.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Indirect"))) {
					if (fields.contains(FieldType.STATUS.getName())) {
						if (!((fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName()))
								|| (fields.get(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.TIMEOUT.getName())))) {
							// vendorPayoutDao.UpdateForClosing(fields);
						}
					}
				}
			}
		} catch (Exception exception) {
			logger.error("Exception in updating closing collection", exception);
		}
		if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName())))
			removeSubMerchantId(fields);

		fields.remove(FieldType.VIRTUAL_AC_CODE.getName());

	}

	public Map<String, String> compositeIMPSStatusEnqResponseHandler(Fields fields, String response)
			throws SystemException {
		// logger.info("inside compositeIMPSStatusEnqResponseHandler()");

		Transaction transactionResponse = iciciTransactionConverter.toCompositeImpsTransaction(response);
		iciciApiTransformer = new IciciApiTransformer(transactionResponse);
		iciciApiTransformer.updateIMPSCompositeTransactionResponse(fields);

		field.updateIciciIMPSTransaction(fields);

		String payId = fields.get(FieldType.PAY_ID.getName());
		User user = userDao.findPayId(payId);

		if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
			fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
		}

		try {
			if (fields.contains(FieldType.USER_TYPE.getName())) {
				if (StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName())) && (fields
						.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct")
						|| fields.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Indirect"))) {
					if (fields.contains(FieldType.STATUS.getName())) {
						if (!((fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName()))
								|| (fields.get(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.TIMEOUT.getName())))) {
							vendorPayoutDao.UpdateForClosing(fields);
						}
					}
				}
			}
		} catch (Exception exception) {
			logger.error("Exception in updating closing collection", exception);
		}
		// field.updateUTRAndPayoutDate(fields, response);

		return fields.getFields();
	}

	public Map<String, String> compositeUPIStatusEnqResponseHandler(Fields fields, String response)
			throws SystemException {
		// logger.info("inside compositeUPIStatusEnqResponseHandler()");

		Transaction transactionResponse = iciciTransactionConverter.toCompositeUpiTransaction(response);
		iciciApiTransformer = new IciciApiTransformer(transactionResponse);

		iciciApiTransformer.updateUPICompositeTransactionStatusEnqResponse(fields);

		updateFinalStatus(fields);

		field.updateIciciIMPSTransaction(fields);

		String payId = fields.get(FieldType.PAY_ID.getName());
		User user = userDao.findPayId(payId);

		if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
			fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
		}
		try {
			if (fields.contains(FieldType.USER_TYPE.getName())) {
				if (StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName())) && (fields
						.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct")
						|| fields.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Indirect"))) {
					if (fields.contains(FieldType.STATUS.getName())) {
						if (!((fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName()))
								|| (fields.get(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.TIMEOUT.getName())))) {
							vendorPayoutDao.UpdateForClosing(fields);
						}
					}
				}
			}
		} catch (Exception exception) {
			logger.error("Exception in updating closing collection", exception);
		}

		return fields.getFields();
	}

	public Map<String, String> compositeUPITransactionResponseHandler(Fields fields, String response)
			throws SystemException {
		logger.info("inside compositeUPITransactionResponseHandler()");
		Transaction transactionResponse = iciciTransactionConverter.toCompositeUpiTransaction(response);
		iciciApiTransformer = new IciciApiTransformer(transactionResponse);
		iciciApiTransformer.updateUPICompositeTransactionResponse(fields);

		String payId = fields.get(FieldType.PAY_ID.getName());

		User user = userDao.findPayId(payId);

		fields.put(FieldType.VIRTUAL_AC_CODE.getName(), user.getVirtualAccountNo());

		if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
			fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
		}

		// logger.info("fields for insert " + fields.getFields() + " txn id " +
		// fields.get(FieldType.TXN_ID.getName()));

		// fields.put(FieldType.STATUS.getName(), "Timeout");
		if (fields.contains("flagBulk")) {
			fields.remove("flagBulk");
			field.updateIciciIMPSBulkTransaction(fields);
		} else {
			field.insertIciciCompositeFields(fields);
		}

		try {
			if (fields.contains(FieldType.USER_TYPE.getName())) {
				if (StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName())) && (fields
						.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct")
						|| fields.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Indirect"))) {
					if (fields.contains(FieldType.STATUS.getName())) {
						if (!((fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName()))
								|| (fields.get(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.TIMEOUT.getName())))) {
							// vendorPayoutDao.UpdateForClosing(fields);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception ", e);
		}

		if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName())))
			removeSubMerchantId(fields);

		fields.remove(FieldType.VIRTUAL_AC_CODE.getName());

		return fields.getFields();
	}

	public Map<String, String> compositeBeneAdditionResponseHandler(Fields fields, String response)
			throws SystemException {
		// logger.info("inside compositeBeneAdditionResponseHandler()");

		Transaction transactionResponse = iciciTransactionConverter.toCompositeBeneAddition(response);
		iciciApiTransformer = new IciciApiTransformer(transactionResponse);
		Map<String, String> beneFields = iciciApiTransformer.updateBeneAdditionTransactionResponse(fields);

		String payId = fields.get(FieldType.PAY_ID.getName());

		User user = userDao.findPayId(payId);

		if (StringUtils.isNotBlank(user.getSuperMerchantId()) && !user.isSuperMerchant()) {
			logger.info("SuperMerchant Found for PayId " + payId);
			beneFields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			beneFields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
		} else {
			beneFields.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
		}

		if (isPaybleMerchant(fields)) {
			beneFields.put(FieldType.IS_PAYBLE_MERCHANT.getName(), Constants.Y_FLAG.getValue());
		} else {
			beneFields.put(FieldType.IS_PAYBLE_MERCHANT.getName(), Constants.N_FLAG.getValue());
		}
		field.insertIciciCompositeBeneFields(beneFields);
		// logger.info("Bene Inserted");

		beneFields.remove(FieldType.IS_PAYBLE_MERCHANT.getName());

		return beneFields;
	}

	public void compositeRTGSNEFTTransactionResponseHandler(Fields fields, String response) throws SystemException {
		logger.info("inside compositeRTGSNEFTTransactionResponseHandler()");
		Transaction transactionResponse = iciciTransactionConverter.toCompositeRtgsNeftTransaction(response);
		iciciApiTransformer = new IciciApiTransformer(transactionResponse);
		iciciApiTransformer.updateNEFTRTGSCompositeTransactionResponse(fields);

		String payId = fields.get(FieldType.PAY_ID.getName());

		User user = userDao.findPayId(payId);

		fields.put(FieldType.VIRTUAL_AC_CODE.getName(), user.getVirtualAccountNo());
		if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
			fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
		}

		logger.info("fields for inserting " + fields.getFields() + " txn id " + fields.get(FieldType.TXN_ID.getName()));
		field.insertIciciCompositeFields(fields);

		if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName())))
			removeSubMerchantId(fields);

		if (StringUtils.isNotBlank(fields.get(FieldType.RESELLER_ID.getName())))
			fields.remove(FieldType.RESELLER_ID.getName());
		fields.remove(FieldType.VIRTUAL_AC_CODE.getName());

	}

	public void compositeAccountStatementResponseHandler(Fields fields, String response) throws SystemException {
		logger.info(" inside compositeAccountStatementResponseHandler ");
		iciciApiTransformer.updateCompositeAccountStatementResponse(fields, response);
		// logger.info(fields.getFields() + " response " + response);
	}

	public boolean isBeneSuccess(Map<String, String> beneResponse) {

		if (beneResponse.get(FieldType.STATUS.getName()).equalsIgnoreCase("success")) {
			return true;
		} else {
			if (StringUtils.isNotBlank(beneResponse.get(FieldType.PG_RESP_CODE.getName()))
					&& (beneResponse.get(FieldType.PG_RESP_CODE.getName()).equalsIgnoreCase("100340")
							|| beneResponse.get(FieldType.PG_RESP_CODE.getName()).equalsIgnoreCase("999590")
							|| beneResponse.get(FieldType.PG_RESP_CODE.getName()).equalsIgnoreCase("100340")
							|| beneResponse.get(FieldType.PG_RESP_CODE.getName()).equalsIgnoreCase("100260"))) {
				return true;
			}

		}

		return false;

	}

	public boolean isBeneAlreadyRegistered(Fields fields) {
		// logger.info("Checking Beneficiary in DB");
		return IciciCompositeDao.isDuplicateBene(fields);

	}

	private void removeSubMerchantId(Fields fields) {
		if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
			logger.info("Found Sub_Merchant ID " + fields.get(FieldType.SUB_MERCHANT_ID.getName()));

			fields.put(FieldType.PAY_ID.getName(), fields.get(FieldType.SUB_MERCHANT_ID.getName()));
			fields.remove(FieldType.SUB_MERCHANT_ID.getName());

		}

		// logger.info("Final Response for removing Sub Merchant = " +
		// fields.getFields());

	}

	public boolean isDuplicateAccountNumberNodalBene(Fields fields) {
		// logger.info("Checking Nodal Beneficiary in DB");
		return IciciCompositeDao.checkNodalBeneDuplicateAccountNo(fields);

	}

	public boolean isPaybleMerchant(Fields fields) {
		logger.info("Checking isPaybleMerchant");

		String payblePayId = PropertiesManager.propertiesMap.get("PAYBLE_PAY_ID");

		String reqPayId = fields.get(FieldType.PAY_ID.getName());

		if (StringUtils.isNotBlank(payblePayId) && StringUtils.isNotBlank(reqPayId) && payblePayId.contains(reqPayId)) {
			logger.info("This is Payble Merchant " + reqPayId);
			return true;
		}

		return false;

	}

	public void updateFinalStatus(Fields fields) {
		// if UPI status enquiry get same response code after 15 min it will
		// mark failed
		// as mentioned in bank response file
		String[] bankResponseCodeList = { "5", "91", "9999", "72", "73", "74", "0U27", "00RR", "18", "6", "U48", "U88",
				"27", "101", "94", "92", "91" };
		String bankResponseCode = fields.get(FieldType.PG_RESP_CODE.getName());

		for (String bankCode : bankResponseCodeList) {
			if (bankCode.equals(bankResponseCode)) {
				field.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
			}
		}
	}

	/*
	public String coinSwitchCustomersResponseHandler(JSONObject jsonRequest) {
		// logger.info("Inside coinSwitchCustomersResponseHandler(), JSON
		// Request for
		Fields fields = new Fields();
		String encryptedResponse = "";
		try {
			CoinSwitchCustomer customerDetail = field
					.findCustomerByVirtualAccNo((String) jsonRequest.get("VirtualACCode"));

			BigDecimal amount = new BigDecimal((String) jsonRequest.get("AMT")).setScale(2);

			fields.put("_id", TransactionManager.getNewTransactionId());

			fields.put(FieldType.CUST_ID.getName(), (String) customerDetail.getCustId());
			fields.put(FieldType.PHONE_NO.getName(), (String) customerDetail.getPhoneNo());
			fields.put(FieldType.CUST_NAME.getName(), (String) customerDetail.getCustName());
			fields.put(FieldType.VIRTUAL_ACC_NUM.getName(), (String) jsonRequest.get("VirtualACCode"));
			fields.put(FieldType.TOTAL_AMOUNT.getName(), String.valueOf(amount));
			fields.put(FieldType.TXNTYPE.getName(), "CREDIT");
			fields.put(FieldType.PURPOSE.getName(), "Coin Purchase");
			fields.put(FieldType.RRN.getName(), (String) jsonRequest.get(FieldType.UTR.getName()));

			fields.put(FieldType.ACQ_ID.getName(), (String) jsonRequest.get("BankInternalTransactionNumber"));

			String txnId = TransactionManager.getNewTransactionId();
			fields.put(FieldType.TXN_ID.getName(), txnId);
			fields.put(FieldType.PG_REF_NUM.getName(), txnId);
			fields.put(FieldType.PAYMENT_TYPE.getName(), (String) jsonRequest.get("MODE"));
			fields.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), "000");
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), "SUCCESS");

			// checking duplicate entry in db
			String isDuplicate = field.checkCoinSwitchDuplicateTxnResponse(fields);

			if (!StringUtils.isEmpty(isDuplicate) && isDuplicate.equals("N")) {

				field.insertCoinSwitchTxnResponse(fields);
				// callback to coinswith after credit entry
				try {
					Runnable runnable = new Runnable() {

						@Override
						public void run() {
							logger.info("Callback to Coin Switch with RRN = " + fields.get(FieldType.UTR.getName())
									+ " And pgRef = " + fields.get(FieldType.PG_REF_NUM.getName()));
							//fieldsDao.sendCallbackToCoinSwitch(fields);

						}
					};
					propertiesManager.executorImpl(runnable);
				} catch (Exception e) {
					logger.error("Callback to Coin Switch with RRN = " + fields.get(FieldType.UTR.getName()));
					logger.error("Exception in Callback ", e);

				}
				if (!customerDetail.getAccountNo().equalsIgnoreCase(jsonRequest.get("PayerAccNumber").toString())
						|| !customerDetail.getBankIfsc()
								.equalsIgnoreCase(jsonRequest.get("PayerBankIFSC").toString())) {

					// reversal code because account no and ifsc code not
					// matched with customer bank
					// account No and bank ifsc
					fields.put(FieldType.PAYER_ACCOUNT_NO.getName(), jsonRequest.get("PayerAccNumber").toString());
					fields.put(FieldType.IFSC_CODE.getName(), jsonRequest.get("PayerBankIFSC").toString());
					fields.put(FieldType.PAYER_NAME.getName(), jsonRequest.get("PayerName").toString());
					fields.put(FieldType.PAY_ID.getName(),
							propertiesManager.propertiesMap.get("CoinSwitch_Merchant_PayId"));
					Map<String, String> payoutResponse = new HashMap<String, String>();

					payoutResponse = iciciUtils.coinSwitchPayout(fields);
					logger.info("Payout response from imps >>>>>>>>>>> " + payoutResponse);

					Fields payoutFields = new Fields();

					payoutFields.put("_id", TransactionManager.getNewTransactionId());

					payoutFields.put(FieldType.CUST_ID.getName(), (String) fields.get(FieldType.CUST_ID.getName()));
					payoutFields.put(FieldType.PHONE_NO.getName(), (String) fields.get(FieldType.PHONE_NO.getName()));
					payoutFields.put(FieldType.CUST_NAME.getName(), (String) fields.get(FieldType.CUST_NAME.getName()));
					payoutFields.put(FieldType.VIRTUAL_ACC_NUM.getName(),
							fields.get(FieldType.VIRTUAL_ACC_NUM.getName()));
					payoutFields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.TOTAL_AMOUNT.getName()));
					payoutFields.put(FieldType.TXNTYPE.getName(), "DEBIT");
					payoutFields.put(FieldType.PURPOSE.getName(), "Payout");
					payoutFields.put(FieldType.RRN.getName(), payoutResponse.get(FieldType.RRN.getName()));

					payoutFields.put(FieldType.TXN_ID.getName(), payoutResponse.get(FieldType.TXN_ID.getName()));
					payoutFields.put(FieldType.PG_REF_NUM.getName(),
							payoutResponse.get(FieldType.PG_REF_NUM.getName()));
					payoutFields.put(FieldType.PAYMENT_TYPE.getName(), payoutResponse.get("TXNTYPE"));
					payoutFields.put(FieldType.STATUS.getName(), payoutResponse.get(FieldType.STATUS.getName()));
					payoutFields.put(FieldType.RESPONSE_CODE.getName(),
							payoutResponse.get(FieldType.RESPONSE_CODE.getName()));
					payoutFields.put(FieldType.RESPONSE_MESSAGE.getName(),
							payoutResponse.get(FieldType.RESPONSE_MESSAGE.getName()));
					payoutFields.put(FieldType.PG_TXN_MESSAGE.getName(),
							payoutResponse.get(FieldType.PG_TXN_MESSAGE.getName()));

					field.insertCoinSwitchTxnResponse(payoutFields);

					// callback after reversal
					try {
						Runnable runnable = new Runnable() {

							@Override
							public void run() {
								logger.info("Callback to Coin Switch with RRN = "
										+ payoutFields.get(FieldType.RRN.getName()) + " And pgRef = "
										+ payoutFields.get(FieldType.PG_REF_NUM.getName()));
								fieldsDao.sendCallbackToCoinSwitch(payoutFields);

							}
						};
						propertiesManager.executorImpl(runnable);
					} catch (Exception e) {
						logger.error("Callback to Coin Switch with RRN = " + fields.get(FieldType.UTR.getName()));
						logger.error("Exception in Callback ", e);

					}
				}

				encryptedResponse = createEncryptedResponse(isDuplicate);

			} else if (!StringUtils.isEmpty(isDuplicate) && isDuplicate.equals("Y")) {

				encryptedResponse = createEncryptedResponse(isDuplicate);
			}

		} catch (Exception exception) {
			logger.error("Exception in eCollectionResponseHandler", exception);
			return null;
		}
		return encryptedResponse;
	}*/

	
	/*public String coinSwitchCustomersResponseHandlerHybrid(JSONObject jsonRequest) {
		// logger.info("Inside coinSwitchCustomersResponseHandler(), JSON
		// Request for
		Fields fields = new Fields();
		String encryptedResponse = "";
		try {
			CoinSwitchCustomer customerDetail = field
					.findCustomerByVirtualAccNo((String) jsonRequest.get("VirtualAccountNumber"));

			BigDecimal amount = new BigDecimal((String) jsonRequest.get("Amount")).setScale(2);

			fields.put("_id", TransactionManager.getNewTransactionId());

			fields.put(FieldType.CUST_ID.getName(), (String) customerDetail.getCustId());
			fields.put(FieldType.PHONE_NO.getName(), (String) customerDetail.getPhoneNo());
			fields.put(FieldType.CUST_NAME.getName(), (String) customerDetail.getCustName());
			fields.put(FieldType.VIRTUAL_ACC_NUM.getName(), (String) jsonRequest.get("VirtualAccountNumber"));
			fields.put(FieldType.TOTAL_AMOUNT.getName(), String.valueOf(amount));
			fields.put(FieldType.TXNTYPE.getName(), "CREDIT");
			fields.put(FieldType.PURPOSE.getName(), "Coin Purchase");
			fields.put(FieldType.RRN.getName(), (String) jsonRequest.get(FieldType.UTR.getName()));

			fields.put(FieldType.ACQ_ID.getName(), (String) jsonRequest.get("BankInternalTransactionNumber"));

			String txnId = TransactionManager.getNewTransactionId();
			fields.put(FieldType.TXN_ID.getName(), txnId);
			fields.put(FieldType.PG_REF_NUM.getName(), txnId);
			fields.put(FieldType.PAYMENT_TYPE.getName(), (String) jsonRequest.get("Mode"));
			fields.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), "000");
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), "SUCCESS");

			// checking duplicate entry in db
			String isDuplicate = field.checkCoinSwitchDuplicateTxnResponse(fields);

			if (!StringUtils.isEmpty(isDuplicate) && isDuplicate.equals("N")) {
				field.insertCoinSwitchTxnResponse(fields);
				// callback to coinswith after credit entry

				if (!customerDetail.getAccountNo().equalsIgnoreCase(jsonRequest.get("PayerAccNumber").toString())
						|| !customerDetail.getBankIfsc()
								.equalsIgnoreCase(jsonRequest.get("PayerBankIFSC").toString())) {

					// reversal code because account no and ifsc code not
					// matched with customer bank
					// account No and bank ifsc
					fields.put(FieldType.PAYER_ACCOUNT_NO.getName(), jsonRequest.get("PayerAccNumber").toString());
					fields.put(FieldType.IFSC_CODE.getName(), jsonRequest.get("PayerBankIFSC").toString());
					fields.put(FieldType.PAYER_NAME.getName(), jsonRequest.get("PayerName").toString());
					fields.put(FieldType.PAY_ID.getName(),
							propertiesManager.propertiesMap.get("CoinSwitch_Merchant_PayId"));
					Map<String, String> payoutResponse = new HashMap<String, String>();

					payoutResponse = iciciUtils.coinSwitchPayout(fields);
					logger.info("Payout response from imps >>>>>>>>>>> " + payoutResponse);

					Fields payoutFields = new Fields();

					payoutFields.put("_id", TransactionManager.getNewTransactionId());

					payoutFields.put(FieldType.CUST_ID.getName(), (String) fields.get(FieldType.CUST_ID.getName()));
					payoutFields.put(FieldType.PHONE_NO.getName(), (String) fields.get(FieldType.PHONE_NO.getName()));
					payoutFields.put(FieldType.CUST_NAME.getName(), (String) fields.get(FieldType.CUST_NAME.getName()));
					payoutFields.put(FieldType.VIRTUAL_ACC_NUM.getName(),
							fields.get(FieldType.VIRTUAL_ACC_NUM.getName()));
					payoutFields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.TOTAL_AMOUNT.getName()));
					payoutFields.put(FieldType.TXNTYPE.getName(), "DEBIT");
					payoutFields.put(FieldType.PURPOSE.getName(), "Payout");
					payoutFields.put(FieldType.RRN.getName(), payoutResponse.get(FieldType.RRN.getName()));

					payoutFields.put(FieldType.TXN_ID.getName(), payoutResponse.get(FieldType.TXN_ID.getName()));
					payoutFields.put(FieldType.PG_REF_NUM.getName(),
							payoutResponse.get(FieldType.PG_REF_NUM.getName()));
					payoutFields.put(FieldType.PAYMENT_TYPE.getName(), payoutResponse.get("TXNTYPE"));
					payoutFields.put(FieldType.STATUS.getName(), payoutResponse.get(FieldType.STATUS.getName()));
					payoutFields.put(FieldType.RESPONSE_CODE.getName(),
							payoutResponse.get(FieldType.RESPONSE_CODE.getName()));
					payoutFields.put(FieldType.RESPONSE_MESSAGE.getName(),
							payoutResponse.get(FieldType.RESPONSE_MESSAGE.getName()));
					payoutFields.put(FieldType.PG_TXN_MESSAGE.getName(),
							payoutResponse.get(FieldType.PG_TXN_MESSAGE.getName()));

					field.insertCoinSwitchTxnResponse(payoutFields);

					// callback after reversal
					// try {
					// Runnable runnable = new Runnable() {
					//
					// @Override
					// public void run() {
					// logger.info("Callback to Coin Switch with RRN = " +
					// payoutFields.get(FieldType.RRN.getName())
					// + " And pgRef = " +
					// payoutFields.get(FieldType.PG_REF_NUM.getName()));
					// fieldsDao.sendCallbackToCoinSwitch(payoutFields);
					//
					// }
					// };
					// propertiesManager.executorImpl(runnable);
					// } catch (Exception e) {
					// logger.error("Callback to Coin Switch with RRN = " +
					// fields.get(FieldType.UTR.getName()));
					// logger.error("Exception in Callback ", e);
					//
					// }
				} else {
					try {
						Runnable runnable = new Runnable() {

							@Override
							public void run() {
								logger.info("Callback to Coin Switch with RRN = " + fields.get(FieldType.RRN.getName())
										+ " And pgRef = " + fields.get(FieldType.PG_REF_NUM.getName()));
								fieldsDao.sendCallbackToCoinSwitch(fields);

							}
						};
						propertiesManager.executorImpl(runnable);
					} catch (Exception e) {
						logger.error("Callback to Coin Switch with RRN = " + fields.get(FieldType.RRN.getName()));
						logger.error("Exception in Callback ", e);

					}
				}

				encryptedResponse = createEncryptedResponse(isDuplicate);

			} else if (!StringUtils.isEmpty(isDuplicate) && isDuplicate.equals("Y")) {

				encryptedResponse = createEncryptedResponse(isDuplicate);
			}

		} catch (Exception exception) {
			logger.error("Exception in coinSwitchCustomersResponseHandler", exception);
			return null;
		}
		return encryptedResponse;
	}*/

	public Map<String, String> compositeNeftRtgsStatusEnqResponseHandler(Fields fields, String response)
			throws SystemException {

		Transaction transactionResponse = iciciTransactionConverter.toCompositeRtgsNeftTransaction(response);
		iciciApiTransformer = new IciciApiTransformer(transactionResponse);
		iciciApiTransformer.updateNEFTRTGSCompositeTransactionResponse(fields);

		field.updateIciciIMPSTransaction(fields);

		String payId = fields.get(FieldType.PAY_ID.getName());
		User user = userDao.findPayId(payId);

		if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
			fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
		}

		try {
			if (fields.contains(FieldType.USER_TYPE.getName())) {
				if (StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName())) && (fields
						.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct")
						|| fields.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Indirect"))) {
					if (fields.contains(FieldType.STATUS.getName())) {
						if (!((fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName()))
								|| (fields.get(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.TIMEOUT.getName())))) {
							vendorPayoutDao.UpdateForClosing(fields);
						}
					}
				}
			}
		} catch (Exception exception) {
			logger.error("Exception in updating closing collection", exception);
		}
		// field.updateUTRAndPayoutDate(fields, response);

		return fields.getFields();
	}

	public void findPrevFields(Fields fields) throws SystemException {
		fields.put(field.fetchPreviousCibFields(fields.get(FieldType.TXN_ID.getName())));
	}

}
