package com.paymentgateway.payout.cashfreePayout;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.dao.PayoutAcquirerMappingDao;
import com.paymentgateway.commons.dao.SUFDetailDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.PayoutAcquireMapping;
import com.paymentgateway.commons.user.PayoutAcquirer;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.payout.merchantApi.VendorPayoutDao;

@Service
public class CashfreePayoutService {

	@Autowired
	private CashfreeCommunicator communicator;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private VendorPayoutDao vendorPayoutDao;

	@Autowired
	private PayoutAcquirerMappingDao payoutAcquireMappingDao;

	@Autowired
	private Fields field;

	@Autowired
	private SUFDetailDao sufDetailDao;

	@Autowired
	private UserSettingDao userSettingDao;

	private static final Logger logger = LoggerFactory.getLogger(CashfreePayoutService.class);
	public static String authTokenNow = null;

	public String getCashfreeAuthToken(Fields fields, JSONObject adfFields) {
		logger.info("Inside getCashfreeAuthToken()");
		try {
			String url = adfFields.getString(Constants.ADF_5);

			String response = communicator.communicateForAuthToken(fields, url);

			String authToken = null;
			JSONObject resObj = new JSONObject(response);

			if (resObj != null && resObj.get("status") != null
					&& resObj.get("status").toString().equalsIgnoreCase("SUCCESS")) {

				JSONObject dataObj = resObj.getJSONObject("data");
				authToken = dataObj.get("token").toString();
				authTokenNow = authToken;
				logger.info("Auth Token = " + authToken);

			} else {
				logger.info("Auth Token Not Received");
			}
			return authToken;

		} catch (Exception e) {
			logger.info("exception in getCashfreeAuthToken() ", e);
		}

		return null;
	}

	public String validateVPACashfree(Fields fields, JSONObject adfFields, String authToken) {
		logger.info("inside validateVPACashfree()");

		try {

			String url = adfFields.getString(Constants.ADF_10);

			String response = communicator.vpaValidationCashfree(fields, authToken, url);

			if (StringUtils.isNotBlank(response)) {

				JSONObject resJson = new JSONObject(response);
				if (resJson != null) {

					if (resJson.get("status") != null) {
						if (resJson.get("status").toString().equalsIgnoreCase("SUCCESS")) {

							JSONObject dataJson = resJson.getJSONObject("data");
							if (dataJson.get("accountExists") != null
									&& dataJson.get("accountExists").toString().equalsIgnoreCase("YES")) {
								return "YES";
							} else {
								return "NO";
							}
						} else {
							if (resJson.has("message")) {
								fields.put(FieldType.RESPONSE_MESSAGE.getName(), (String) resJson.get("message"));
								return "Error";
							}

						}
					}
				}

			}

			return null;

		} catch (Exception e) {
			logger.error("Exception in comunication with Cashfree VPA Validation for txn Id"
					+ fields.get(FieldType.TXN_ID.getName()), e);
			return null;
		}
	}

	public String validateAuthToken(String authToken, JSONObject adfFields) {
		String response = communicator.verifyAuthToken(authToken, adfFields);
		String status = null;
		JSONObject resObj = new JSONObject(response);

		if (resObj != null && resObj.get("status") != null && resObj.get("subCode") != null
				&& resObj.get("status").toString().equalsIgnoreCase("SUCCESS")
				&& resObj.get("subCode").toString().equalsIgnoreCase("200")) {

			status = resObj.get("status").toString();

			logger.info("Auth Verify Status " + status);

		} else {
			logger.info("Auth Token Not Received");
		}
		return response;
	}

	public String fetchBene(Fields fields, JSONObject adfFields, String authToken) {
		String response = communicator.fetchBene(fields, authToken, adfFields);

		if (StringUtils.isBlank(response)) {
			fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Beneficiary");
			return response;
		}
		logger.info("Bene Id is = " + response);
		return response;
	}

	public String sendPayout(Fields fields, String beneId, JSONObject adfFields, String authToken) {

		String response = communicator.sendPayout(fields, authToken, beneId, adfFields);
		updateCashfreePayoutResponse(fields, response);

		String status = fields.get(FieldType.STATUS.getName());
		String pgCode = fields.get(FieldType.PG_RESP_CODE.getName());
		String pgMsg = fields.get(FieldType.PG_TXN_MESSAGE.getName());

		if (StringUtils.isNotBlank(status) && StringUtils.isNotBlank(pgCode) && StringUtils.isNotBlank(pgMsg)) {

			if (status.equals(StatusType.DECLINED.getName())
					&& pgMsg.equalsIgnoreCase("Not enough available balance in the account")
					&& pgCode.equalsIgnoreCase("400")) {

				Date dNow = new Date();
				String dateNow = DateCreater.formatDateForDb(dNow);
				fields.put(FieldType.CREATE_DATE.getName(), dateNow);

				fields.put(FieldType.IS_STATUS_FINAL.getName(), "Y");
				return response;
			}

		}
		updateStatusForPayoutTransaction(fields);

		return response;
	}

	private void updateStatusForPayoutTransaction(Fields fields) {

		logger.info("Orignal response data for payout before updating status >> " + fields.getFields());

		if (!fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {
			fields.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PROCESSING.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PROCESSING.getResponseMessage());
		}
	}

	private void updateCashfreePayoutResponse(Fields fields, String response) {

		try {

			Transaction transaction = new Transaction();
			transaction = toTransactionCashfree(response);

			fields.remove(FieldType.HASH.getName());

			String status = null;
			ErrorType errorType = null;
			String pgTxnMsg = null;

			// status = SUCCESS , subCode = 200 , acknowledged = 1
			if ((StringUtils.isNotBlank(transaction.getStatus()))
					&& ((transaction.getStatus()).equalsIgnoreCase("SUCCESS"))
					&& (StringUtils.isNotBlank(transaction.getSubCode()))
					&& ((transaction.getSubCode()).equalsIgnoreCase("200"))
					&& (StringUtils.isNotBlank(transaction.getAcknowledged()))
					&& ((transaction.getAcknowledged()).equalsIgnoreCase("1"))) {
				status = StatusType.CAPTURED.getName();
				errorType = ErrorType.SUCCESS;

				if (StringUtils.isNotBlank(transaction.getMessage())) {
					pgTxnMsg = transaction.getMessage();
				} else {
					pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
				}

			}
			// status = SUCCESS , subCode = 200
			else if ((StringUtils.isNotBlank(transaction.getStatus()))
					&& ((transaction.getStatus()).equalsIgnoreCase("SUCCESS"))
					&& (StringUtils.isNotBlank(transaction.getSubCode()))
					&& ((transaction.getSubCode()).equalsIgnoreCase("200"))) {

				status = StatusType.PROCESSING.getName();
				errorType = ErrorType.PROCESSING;

				if (StringUtils.isNotBlank(transaction.getMessage())) {
					pgTxnMsg = transaction.getMessage();
				} else {
					pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
				}

			}

			else if ((StringUtils.isNotBlank(transaction.getStatus()))
					&& ((transaction.getStatus()).equalsIgnoreCase("SUCCESS"))
					&& (StringUtils.isNotBlank(transaction.getSubCode()))
					&& ((transaction.getSubCode()).equalsIgnoreCase("201"))) {
				status = StatusType.CAPTURED.getName();
				errorType = ErrorType.SUCCESS;

				if (StringUtils.isNotBlank(transaction.getMessage())) {
					pgTxnMsg = transaction.getMessage();
				} else {
					pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
				}

			}

			else if ((StringUtils.isNotBlank(transaction.getStatus()))
					&& ((transaction.getStatus()).equalsIgnoreCase("PENDING"))
					&& (StringUtils.isNotBlank(transaction.getSubCode()))
					&& ((transaction.getSubCode()).equalsIgnoreCase("201"))) {
				status = StatusType.PROCESSING.getName();
				errorType = ErrorType.PROCESSING;

				if (StringUtils.isNotBlank(transaction.getMessage())) {
					pgTxnMsg = transaction.getMessage();
				} else {
					pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
				}

			}

			else {
				if (StringUtils.isNotBlank(transaction.getSubCode())) {
					CashfreePayoutResultType resultInstance = CashfreePayoutResultType
							.getInstanceFromName(transaction.getSubCode());

					if (resultInstance != null) {
						status = resultInstance.getStatusCode();
						errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGateway());
						pgTxnMsg = resultInstance.getMessage();
					} else {
						status = StatusType.DECLINED.getName();
						errorType = ErrorType.getInstanceFromCode("004");
						pgTxnMsg = "Transaction Declined by bank";
					}

				} else {
					status = StatusType.REJECTED.getName();
					errorType = ErrorType.REJECTED;
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
				}
			}

			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
			fields.put(FieldType.TXNTYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
			fields.put(FieldType.STATUS.getName(), status);
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

			if (StringUtils.isNotBlank(transaction.getReferenceId())) {
				fields.put(FieldType.RRN.getName(), transaction.getReferenceId());
			} else {
				fields.put(FieldType.RRN.getName(), "NA");
			}

			if (StringUtils.isNotBlank(transaction.getUtr())) {
				fields.put(FieldType.UTR_NO.getName(), transaction.getUtr());
			} else {
				fields.put(FieldType.UTR_NO.getName(), "NA");
			}

			if (StringUtils.isNotBlank(transaction.getSubCode())) {
				fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getSubCode());
			} else {
				fields.put(FieldType.PG_RESP_CODE.getName(), errorType.getResponseCode());
			}

			if (StringUtils.isNotBlank(transaction.getMessage())) {
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getMessage());
			}

		} catch (Exception e) {
			logger.error("Exception ", e);
		}

	}

	private Transaction toTransactionCashfree(String json) {
		Transaction transaction = new Transaction();
		logger.info("Response for Cashfree >>  " + json);

		try {
			if (StringUtils.isNotBlank(json)) {

				JSONObject data = new JSONObject(json);

				if (data.has("status")) {
					transaction.setStatus(data.get("status").toString());
				}

				if (data.has("subCode")) {
					transaction.setSubCode(data.get("subCode").toString());
				}

				if (data.has("message")) {
					transaction.setMessage(data.get("message").toString());
				}

				if (data.has("data")) {

					JSONObject dataInner = data.getJSONObject("data");

					if (dataInner.has("referenceId")) {
						transaction.setReferenceId(dataInner.get("referenceId").toString());
					}

					if (dataInner.has("utr")) {
						transaction.setUtr(dataInner.get("utr").toString());
					}

					if (dataInner.has("acknowledged")) {
						transaction.setAcknowledged(dataInner.get("acknowledged").toString());
					}
				}

			}

		} catch (Exception e) {
			logger.error("Exception in converting Cashfree payout Transaction", e);
		}
		return transaction;
	}

	public void insertPayoutTxn(Fields fields) {

		try {

			String payId = fields.get(FieldType.PAY_ID.getName());
			User user = userDao.findPayId(payId);

			fields.put(FieldType.VIRTUAL_AC_CODE.getName(), user.getVirtualAccountNo());

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
			}

			if (fields.contains("flagBulk")) {
				fields.remove("flagBulk");
				fieldsDao.updateIMPSBulkTransactionStatus(fields);
			} else {
				fieldsDao.insertPayoutTransactionFields(fields);
			}

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				fields.put(FieldType.PAY_ID.getName(), user.getPayId());
				fields.remove(FieldType.SUB_MERCHANT_ID.getName());
			}
		} catch (Exception e) {
			logger.info("exception in insert payout txn ", e);
		}

	}

	public void payoutStatus(Fields fields, JSONObject adfFields, String authToken) {

		String response = communicator.getStatus(fields, authToken, adfFields);

		if (StringUtils.isNotBlank(response)) {
			JSONObject responseJson = new JSONObject(response);

		} else {

		}

	}

	public void handlePayoutCallback(Fields fields) throws SystemException {

		String event = fields.get("event");

		if (event.equalsIgnoreCase("TRANSFER_SUCCESS") || event.equalsIgnoreCase("TRANSFER_FAILED")
				|| event.equalsIgnoreCase("TRANSFER_REVERSED")) {

			fieldsDao.updateCashfreePayoutCallbackTxn(fields);
			logger.info("transaction fields are >> " + fields.getFields());
		}

	}

	public String checkTxnStatus(Fields fields) throws SystemException {

		String event = fields.get("event");
		String status = null;
		if (event.equalsIgnoreCase("TRANSFER_SUCCESS") || event.equalsIgnoreCase("TRANSFER_FAILED")
				|| event.equalsIgnoreCase("TRANSFER_REVERSED")) {
			String txnId = fields.get("transferId");

			status = fieldsDao.getPayoutTxnStatus(txnId);
		}
		return status;

	}

	public String handleAddBalaceRequest(Fields fields) {

		try {
			String amount = fields.get("amount");
			String utr = fields.get("utr");

			fields.put(FieldType.AMOUNT.getName(), amount);
			fields.put(FieldType.UTR.getName(), utr);
			fields.put(FieldType.ACQUIRER_NAME.getName(), PayoutAcquirer.CASHFREE.name());

			removeBankFields(fields);

			String response = insertPayoutAddBalanceInECollection(fields);

			return response;
		} catch (Exception e) {
			logger.info("exception in handleAddBalaceRequest() ", e);
		}
		
		return null;

	}

	private void removeBankFields(Fields fields) {
		fields.remove("event");
		fields.remove("ledgerBalance");
		fields.remove("amount");
		fields.remove("utr");
		fields.remove("signature");
	}

	private String insertPayoutAddBalanceInECollection(Fields fields) {

		String encryptedResponse = null;
		try {

			PayoutAcquireMapping payoutMapping = payoutAcquireMappingDao.findMappingwithAcquirerNameAndPayId(
					fields.get(FieldType.PAY_ID.getName()), PayoutAcquirer.CASHFREE.name());
			BigDecimal amt = new BigDecimal((String) fields.get(FieldType.AMOUNT.getName())).setScale(2); // Done

			fields.put("_id", TransactionManager.getNewTransactionId());

			fields.put(FieldType.CUSTOMER_CODE.getName(), "NA"); // DONE
			fields.put(FieldType.VIRTUAL_AC_CODE.getName(), (String) payoutMapping.getVan()); // DONE
			fields.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(), "NA");

			fields.put(FieldType.TXNTYPE.getName(), "COLLECTION");
			fields.put(FieldType.ORIG_TXNTYPE.getName(), "COLLECTION");
			fields.put(FieldType.PAYMENT_TYPE.getName(), "IMPS"); // DONE
			fields.put(FieldType.MOP_TYPE.getName(), "IMPS"); // DONE

			fields.put(FieldType.RRN.getName(), (String) fields.get(FieldType.UTR.getName())); // DONE
			fields.put(FieldType.SENDER_REMARK.getName(), "NA");

			fields.put(FieldType.AMOUNT.getName(), String.valueOf(amt));
			fields.put(FieldType.TOTAL_AMOUNT.getName(), String.valueOf(amt));
			fields.put(FieldType.PAYEE_NAME.getName(), "NA");
			fields.put(FieldType.PAYEE_ACCOUNT_NUMBER.getName(), "NA");
			fields.put(FieldType.PAYEE_BANK_IFSC.getName(), "NA");
			fields.put(FieldType.PG_DATE_TIME.getName(), "NA");
			fields.put(FieldType.ACQ_ID.getName(), (String) fields.get(FieldType.UTR.getName()));

			String txnId = TransactionManager.getNewTransactionId();
			fields.put(FieldType.TXN_ID.getName(), txnId);
			fields.put(FieldType.PG_REF_NUM.getName(), txnId);
			fields.put(FieldType.OID.getName(), txnId);
			fields.put(FieldType.ORIG_TXN_ID.getName(), txnId);
			fields.put(FieldType.ORDER_ID.getName(), "LP" + txnId);
			fields.put(FieldType.CURRENCY_CODE.getName(), "356");
			fields.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), "000");
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.name());
			fields.put(FieldType.ACQUIRER_NAME.getName(), PayoutAcquirer.CASHFREE.name());
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			fields.put(FieldType.CREATE_DATE.getName(), dateNow);
			fields.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));

			if (StringUtils.isNotBlank((String) payoutMapping.getVan())) {

				User user = userDao.findPayId(payoutMapping.getPayId());

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
				encryptedResponse = "Success";

			} else if (!StringUtils.isEmpty(isDuplicate) && isDuplicate.equals("Y")) {

				encryptedResponse = "Duplicate";
			}

		} catch (Exception exception) {
			logger.error("Exception in eCollectionResponseHandlerComposite", exception);
			return null;
		}
		return encryptedResponse;

	}

}
