package com.paymentgateway.payout.icici.composite;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.payout.merchantApi.VendorPayoutDao;
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
	private VendorPayoutDao vendorPayoutDao;

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
			BigDecimal amt = new BigDecimal((String) jsonRequest.get("Amount")).setScale(2); //Done

			fields.put("_id", TransactionManager.getNewTransactionId());

			fields.put(FieldType.CUSTOMER_CODE.getName(), (String) jsonRequest.get("ClientCode")); //DONE
			fields.put(FieldType.VIRTUAL_AC_CODE.getName(), (String) jsonRequest.get("VirtualAccountNumber")); //DONE
			fields.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(), (String) jsonRequest.get("ClientAccountNo"));

			fields.put(FieldType.TXNTYPE.getName(), "COLLECTION");
			fields.put(FieldType.ORIG_TXNTYPE.getName(), "COLLECTION");
			fields.put(FieldType.PAYMENT_TYPE.getName(), (String) jsonRequest.get("Mode")); //DONE
			fields.put(FieldType.MOP_TYPE.getName(), (String) jsonRequest.get("Mode")); //DONE

			fields.put(FieldType.RRN.getName(), (String) jsonRequest.get("UTR")); //DONE
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
			BigDecimal amt = new BigDecimal((String) jsonRequest.get("Amount")).setScale(2); //Done

			fields.put("_id", TransactionManager.getNewTransactionId());

			fields.put(FieldType.CUSTOMER_CODE.getName(), (String) jsonRequest.get("ClientCode")); //DONE
			fields.put(FieldType.VIRTUAL_AC_CODE.getName(), (String) jsonRequest.get("VirtualAccountNumber")); //DONE
			fields.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(), (String) jsonRequest.get("ClientAccountNo"));

			fields.put(FieldType.TXNTYPE.getName(), "COLLECTION");
			fields.put(FieldType.ORIG_TXNTYPE.getName(), "COLLECTION");
			fields.put(FieldType.PAYMENT_TYPE.getName(), (String) jsonRequest.get("Mode")); //DONE
			fields.put(FieldType.MOP_TYPE.getName(), (String) jsonRequest.get("Mode")); //DONE

			fields.put(FieldType.RRN.getName(), (String) jsonRequest.get("UTR")); //DONE
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

			response = iciciUtils.ecollectionApiEncryptionPayble(responseJson.toString(),true);

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

	public Map<String, String> cibTransactionResponseHandler(Fields fields, String response) throws SystemException {
		// logger.info("inside cibTransactionResponseHandler()");
		iciciApiTransformer.updateTransactionResponse(fields, response);
		logger.info("updated fields are " + fields.getFields());
		field.insertIciciCibFields(fields);
		// removed from here.
		// field.updateUTRAndPayoutDate(fields, response);
		return fields.getFields();
	}

	public Map<String, String> cibTransactionStatusResponseHandler(Fields fields, String response)
			throws SystemException {
		// logger.info("inside cibTransactionResponseHandler()");
		fields.put(field.fetchPreviousCibFields(fields.get(FieldType.TXN_ID.getName())));
		iciciApiTransformer.updateTransactionStatusResponse(fields, response);
		field.insertIciciCibFields(fields);

		return fields.getFields();
	}

	public Map<String, String> cibBeneBalanceInqHandler(Fields fields, String response) throws SystemException {
		// logger.info("inside cibBeneBalanceInqHandler()");
		iciciApiTransformer.updateBalanceInqResponse(fields, response);

		return fields.getFields();
	}

	public Map<String, String> cibAccountStatementResponseHandler(Fields fields, String response)
			throws SystemException {
		//temp code ends
		iciciApiTransformer.updateAccountStatementResponse(fields, response);
		logger.info(fields.getFields() + " response " + response);
		return fields.getFields();
	}

	public void compositeIMPSTransactionResponseHandler(Fields fields, String response) throws SystemException {
		// logger.info("inside compositeIMPSTransactionResponseHandler()");
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
		
		//updating rather than capture status to Processing
		updateStatusForPayoutTransaction(fields);

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

	/*	try {
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
		}*/
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


	/*	try {
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
		}*/
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
		
		//updating rather than capture status to Processing
		updateStatusForPayoutTransaction(fields);
		

		if (fields.contains("flagBulk")) {
			fields.remove("flagBulk");
			field.updateIciciIMPSBulkTransaction(fields);
		} else {
			field.insertIciciCompositeFields(fields);
		}

		if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName())))
			removeSubMerchantId(fields);

		fields.remove(FieldType.VIRTUAL_AC_CODE.getName());

		return fields.getFields();
	}

	private void updateStatusForPayoutTransaction(Fields fields) {
		
		logger.info("Orignal response data for payout before updating status >> "+fields.getFields());
		
		if(!fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())){
			fields.put(FieldType.STATUS.getName(),StatusType.PROCESSING.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(),ErrorType.PROCESSING.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(),ErrorType.PROCESSING.getResponseMessage());
		}
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
		
		//updating rather than capture status to Processing
		updateStatusForPayoutTransaction(fields);
		
		logger.info("fields for inserting " + fields.getFields() + " txn id " + fields.get(FieldType.TXN_ID.getName()));
		if (fields.contains("flagBulk")) {
			fields.remove("flagBulk");
			field.updateIciciIMPSBulkTransaction(fields);
		} else {
			field.insertIciciCompositeFields(fields);
		}



		if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName())))
			removeSubMerchantId(fields);

		if (StringUtils.isNotBlank(fields.get(FieldType.RESELLER_ID.getName())))
			fields.remove(FieldType.RESELLER_ID.getName());
		fields.remove(FieldType.VIRTUAL_AC_CODE.getName());

	}
	
	public Map<String, String> compositeNeftRtgsStatusEnqResponseHandler(Fields fields, String response)
			throws SystemException {
		

		Transaction transactionResponse = iciciTransactionConverter.toCompositeRtgsNeftTransaction(response);
		iciciApiTransformer = new IciciApiTransformer(transactionResponse);
		iciciApiTransformer.updateNEFTRTGSCompositeTransactionResponse(fields);

		field.updateIciciIMPSTransaction(fields);
		
		// field.updateUTRAndPayoutDate(fields, response);

		return fields.getFields();
	}
	
	public void compositeAccountStatementResponseHandler(Fields fields, String response)
			throws SystemException {
		logger.info(" inside compositeAccountStatementResponseHandler ");
		iciciApiTransformer.updateCompositeAccountStatementResponse(fields,response);
//		logger.info(fields.getFields() + " response " + response);
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
		//if UPI status enquiry get same response code after 15 min it will mark failed as mentioned in bank response file
		String[] bankResponseCodeList = {"5","91","9999","72","73","74","0U27","00RR","18","6","U48","U88","27","101","94","92","91"};
		String bankResponseCode = fields.get(FieldType.PG_RESP_CODE.getName());
		
		for (String bankCode : bankResponseCodeList) {
			if(bankCode.equals(bankResponseCode)){
				field.put(FieldType.STATUS.getName(),StatusType.FAILED.getName());
			}
		}
	}
	
	

}
