package com.paymentgateway.cashfree;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.CashfreeChecksumUtil;

/**
 * @author Sandeep
 *
 */
@Service
public class CashfreeUpiQrResponseHandler {

	public static Map<String, User> userMap = new HashMap<String, User>();

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private UserDao userDao;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private CashfreeChecksumUtil cashfreeChecksumUtil;

	@Autowired
	private FieldsDao fieldsDao;

	private static Logger logger = LoggerFactory.getLogger(CashfreeUpiQrResponseHandler.class.getName());

	@SuppressWarnings("static-access")
	public void cashfreeUpiQrResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {
		Fields responseField = new Fields();

		try {
			Map<String, String[]> fieldMapObj = httpRequest.getParameterMap();
			Map<String, String> response = new HashMap<String, String>();
		
			for (Entry<String, String[]> entry : fieldMapObj.entrySet()) {
				try {
					response.put(entry.getKey(), entry.getValue()[0]);
				} catch (ClassCastException classCastException) {
					logger.error("Exception", classCastException);
				}
			}

			logger.info("Cashfree Callback Response >>> " + response.toString());

			if (!response.isEmpty() && response.containsKey("event")
					&& response.get("event").equalsIgnoreCase("AMOUNT_COLLECTED")) {

				Fields fields = new Fields();
				User responseUser = null;
				String vAccountNumber = "";
				String payerVpa = "";
				String remitterIfsc = "";
				String remitterAccount = "";

				String payerAmount = response.get("amount");
				String payerName = response.get("remitterName");
				String payerEmail = response.get("email");
				String payerMobile = response.get("phone");
				if (StringUtils.isNotBlank(response.get("remitterVpa"))) {
					payerVpa = response.get("remitterVpa");
				}
				if (StringUtils.isNotBlank(response.get("vAccountNumber"))) {
					vAccountNumber = response.get("vAccountNumber");
				}
				if (StringUtils.isNotBlank(response.get("remitterIfsc"))) {
					remitterIfsc = response.get("remitterIfsc");
				}
				if (StringUtils.isNotBlank(response.get("remitterAccount"))) {
					remitterAccount = response.get("remitterAccount");
				}
				String acqId = response.get("referenceId");
				String bankRRN = response.get("utr");
				String merchantTranId = response.get("vAccountId");
				String paymentTime = response.get("paymentTime");

				if (StringUtils.isBlank(vAccountNumber)) {

					fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.CASHFREE.getCode());
					fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());

					logger.info("Checking if response is for Static QR Transaction");

					Date dNow = new Date();
					String dateNow = DateCreater.formatDateForDb(dNow);

					MongoDatabase dbIns = null;
					dbIns = mongoInstance.getDB();
					MongoCollection<Document> collection = dbIns.getCollection(
							propertiesManager.propertiesMap.get("MONGO_DB_" + Constants.CASHFREE_QRCODE.getValue()));
					List<BasicDBObject> vpaQuery = new ArrayList<BasicDBObject>();

					vpaQuery.add(new BasicDBObject(FieldType.CUST_ID.getName(), merchantTranId));

					BasicDBObject finalQuery = new BasicDBObject("$and", vpaQuery);
					Document doc = new Document();
					long count = collection.countDocuments(finalQuery);

					if (count == 1) {
						logger.info(
								"This response is for Static QR Transaction , VIRTUAL_VPA_NUM == " + merchantTranId);

						MongoCursor<Document> cursor = collection.find(finalQuery).iterator();
						while (cursor.hasNext()) {
							doc = cursor.next();
							String payId = doc.get(FieldType.PAY_ID.getName()).toString();
							responseUser = userDao.findPayId(payId);
							break;
						}
					} else {

						logger.info("Customer Id count for VIRTUAL_VPA_NUM == " + merchantTranId + " is , count == "
								+ count + " unable to send callback due to invalid number of customer account");
						responseUser = null;

					}

					if (responseUser != null) {

						// Generate a new PG Ref Num of 16 digits for creating an entry in DB in case of
						// UPI STATIC QR Transaction
						String newPgRef = TransactionManager.getNewTransactionId();

						// This PG REF will also be the new _id, TXN_ID, OID and ORDER_ID for this
						// transaction, except for Satin
						// A custom Order Id will be generated for Satin using account number stored in
						// customerQR collection.

						String orderId = newPgRef;

						

						fields.put(FieldType.PG_REF_NUM.getName(), newPgRef);
						fields.put(FieldType.MOP_TYPE.getName(), MopType.STATIC_UPI_QR.getCode());
						fields.put(FieldType.CUST_ID.getName(), doc.get(FieldType.CUST_ID.getName()).toString());

						if (responseUser != null && StringUtils.isNotBlank(responseUser.getSuperMerchantId())
								&& !responseUser.isSuperMerchant()) {
							fields.put(FieldType.PAY_ID.getName(), responseUser.getSuperMerchantId());
							fields.put(FieldType.SUB_MERCHANT_ID.getName(), responseUser.getPayId());
						} else {
							fields.put(FieldType.PAY_ID.getName(), responseUser.getPayId());
						}
						if (StringUtils.isNotBlank(responseUser.getResellerId())) {
							fields.put(FieldType.RESELLER_ID.getName(), responseUser.getResellerId());
						}

						fields.put("_id", newPgRef);

						if (payerAmount.contains(".")) {
							fields.put(FieldType.AMOUNT.getName(), payerAmount.replace(".", ""));
							fields.put(FieldType.TOTAL_AMOUNT.getName(), payerAmount.replace(".", ""));
						} else {
							fields.put(FieldType.AMOUNT.getName(), payerAmount + "00");
							fields.put(FieldType.TOTAL_AMOUNT.getName(), payerAmount + "00");
						}

						fields.put(FieldType.ORIG_TXN_ID.getName(), newPgRef);
						fields.put(FieldType.ORIG_TXNTYPE.getName(), TransactionType.SALE.getName());
						fields.put(FieldType.ACQ_ID.getName(), acqId);
						fields.put(FieldType.OID.getName(), newPgRef);
						fields.put(FieldType.PAYMENTS_REGION.getName(), AccountCurrencyRegion.DOMESTIC.toString());
						fields.put(FieldType.CARD_HOLDER_TYPE.getName(), CardHolderType.CONSUMER.toString());
						fields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");
						fields.put(FieldType.PAYER_ADDRESS.getName(), payerVpa);
						fields.put(FieldType.TRANSACTION_MODE.getName(), "Direct");
						fields.put(FieldType.TXN_CAPTURE_FLAG.getName(), "Real-Time");
						fields.put(FieldType.TXN_DATE.getName(), dateNow.substring(0, 10).replace("-", ""));
						fields.put(FieldType.CARD_MASK.getName(), payerVpa);
						fields.put(FieldType.TXN_ID.getName(), newPgRef);

						fields.put(FieldType.CUST_NAME.getName(), payerName);
						fields.put(FieldType.CUST_PHONE.getName(), payerMobile);
						fields.put(FieldType.CUST_EMAIL.getName(), payerEmail);
						fields.put(FieldType.ORDER_ID.getName(), orderId);
						fields.put(FieldType.CURRENCY_CODE.getName(), "356");
						fields.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
						fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
						fields.put(FieldType.PAYMENT_TYPE.getName(), PaymentType.UPI.getCode());
						fields.put(FieldType.PG_DATE_TIME.getName(), dateNow);
						fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
						fields.put(FieldType.PG_TXN_STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
						fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
						fields.put(FieldType.UDF3.getName(), payerVpa);
						fields.put(FieldType.RRN.getName(), bankRRN);
						fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
						fields.put(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName(), "India");

						if (responseUser != null && responseUser.isSurchargeFlag()) {
							fields.put(FieldType.SURCHARGE_FLAG.getName(), "Y");
						} else {
							fields.put(FieldType.SURCHARGE_FLAG.getName(), "N");
						}

						// Checking signature getting from cashfree upi response
						JSONObject jsonRequest = new JSONObject();
						String cashfreeResponseSignature = response.get("signature");
						response.remove("signature");
						for (String keyFields : response.keySet()) {
							jsonRequest.put(keyFields, response.get(keyFields));
						}
						String calculateSignature = cashfreeChecksumUtil.checkUpiQrResponseHash(jsonRequest,
								getTxnKey(responseUser.getPayId().toString()));
						if (!calculateSignature.equals(cashfreeResponseSignature)) {

							StringBuilder Message = new StringBuilder("Response Signature =");
							Message.append(cashfreeResponseSignature);
							Message.append(", Calculated Signature =");
							Message.append(calculateSignature);
							logger.error(Message.toString());

							fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
							fields.put(FieldType.RESPONSE_MESSAGE.getName(),
									ErrorType.DENIED_BY_FRAUD.getResponseMessage());
							fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DENIED_BY_FRAUD.getCode());
							fields.put(FieldType.PG_TXN_MESSAGE.getName(),
									ErrorType.FRAUD_RESPONSE.getResponseMessage());
							fields.put(FieldType.PG_TXN_STATUS.getName(), StatusType.INVALID.getName());
							fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.DENIED_BY_FRAUD.getCode());

						}

						fields.put(FieldType.CREATE_DATE.getName(), dateNow);
						fields.put(FieldType.UPDATE_DATE.getName(), dateNow);
						fields.put(FieldType.INSERTION_DATE.getName(), paymentTime);
						fields.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));

						fields.put(FieldType.ACQUIRER_TDR_SC.getName(), "0.00");
						fields.put(FieldType.ACQUIRER_GST.getName(), "0.00");
						fields.put(FieldType.PG_GST.getName(), "0.00");
						fields.put(FieldType.PG_TDR_SC.getName(), "0.00");
						fields.put(FieldType.MERCHANT_TDR_SC.getName(), "0.00");
						fields.put(FieldType.MERCHANT_GST.getName(), "0.00");
						fields.put(FieldType.RESELLER_CHARGES.getName(), "0.00");
						fields.put(FieldType.RESELLER_GST.getName(), "0.00");

						logger.info("fields send to transact " + fields.getFieldsAsString());

						Map<String, String> resp = transactionControllerServiceProvider.transact(fields,
								Constants.TXN_WS_UPI_PROCESSOR.getValue());
						responseField = new Fields(resp);
						logger.info("Response received from WS for Cashfree upi " + responseField.getFieldsAsString());

						// sending callback to merchant in case of invalid txn

						if (StringUtils.isNotBlank(fields.get(FieldType.STATUS.getName()))
								&& StringUtils.isNotBlank(fields.get(FieldType.PAY_ID.getName()))
								&& fields.get(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.INVALID.getName())) {

							fieldsDao.sendCallback(responseField);
						}
					}
				} else {
					MongoDatabase dbIns = null;
					dbIns = mongoInstance.getDB();
					MongoCollection<Document> collection = dbIns.getCollection(propertiesManager.propertiesMap
							.get("MONGO_DB_" + Constants.COINSWITCH_ACCOUNTS.getValue()));
					List<BasicDBObject> vpaQuery = new ArrayList<BasicDBObject>();

					vpaQuery.add(new BasicDBObject(FieldType.CUST_ID.getName(), merchantTranId));

					BasicDBObject finalQuery = new BasicDBObject("$and", vpaQuery);
					Document doc = new Document();
					long count = collection.countDocuments(finalQuery);

					if (count == 1) {
						logger.info(
								"This response is for Static QR Transaction , VIRTUAL_VPA_NUM == " + merchantTranId);

						fields.put("_id", TransactionManager.getNewTransactionId());

						fields.put(FieldType.CUST_ID.getName(), merchantTranId);
						fields.put(FieldType.PHONE_NO.getName(), payerMobile);
						fields.put(FieldType.CUST_NAME.getName(), payerName);
						fields.put(FieldType.VIRTUAL_ACC_NUM.getName(), vAccountNumber);
						fields.put(FieldType.TOTAL_AMOUNT.getName(), Amount.formatAmount(payerAmount, "356"));
						fields.put(FieldType.TXNTYPE.getName(), "CREDIT");
						fields.put(FieldType.PURPOSE.getName(), "NA");
						fields.put(FieldType.RRN.getName(), (String) bankRRN);

						fields.put(FieldType.ACQ_ID.getName(), (String) acqId);

						String txnId = TransactionManager.getNewTransactionId();
						fields.put(FieldType.TXN_ID.getName(), txnId);
						fields.put(FieldType.PG_REF_NUM.getName(), txnId);
						fields.put(FieldType.PAYMENT_TYPE.getName(), PaymentType.UPI.getCode());
						fields.put(FieldType.MOP_TYPE.getName(), MopType.STATIC_UPI_QR.getCode());
						fields.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
						fields.put(FieldType.RESPONSE_CODE.getName(), "000");
						fields.put(FieldType.RESPONSE_MESSAGE.getName(), "SUCCESS");

						fieldsDao.insertCoinSwitchTxnResponse(fields);

					} else {

						logger.info("Customer Id count for VIRTUAL_VPA_NUM == " + merchantTranId + " is , count == "
								+ count + " unable to send callback due to invalid number of customer account");
						responseUser = null;

					}
				}
			}
		} catch (Exception e) {
			logger.error("Error in Cashfree UPI callback = ", e);
			String path = httpRequest.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host") + "/pgui/jsp/error";
				httpResponse.sendRedirect(resultPath);
			}
			httpResponse.sendRedirect("error");
		}
	}

	public String getTxnKey(String payId) throws SystemException {
		logger.info("getTxnKey for Pay Id for " + payId);
		User user = userDao.findPayId(payId);
		Account account = null;
		Set<Account> accounts = user.getAccounts();

		if (accounts == null || accounts.size() == 0) {
			logger.info("No account found for Pay ID = " + payId);
		} else {
			for (Account accountThis : accounts) {
				if (accountThis.getAcquirerName().equalsIgnoreCase(
						AcquirerType.getInstancefromCode(AcquirerType.CASHFREE.getCode()).getName())) {
					account = accountThis;
					break;
				}
			}
		}

		AccountCurrency accountCurrency = account.getAccountCurrency("356");
//		String mId = accountCurrency.getAdf1();
		String txnKey = accountCurrency.getAdf2();
		return txnKey;
	}

	public boolean validateHash(Fields fields) {

		try {
			String fieldHash = fields.get(FieldType.HASH.getName());
			fields.remove(FieldType.HASH.getName());

			logger.info("Hash from Merchant  == " + fieldHash);
			String hash = Hasher.getHash(fields);
			logger.info("Calculated Hash == " + hash);

			if (!hash.equals(fieldHash)) {
				logger.info("Hash Mismatch , VA : " + fields.get(FieldType.VIRTUAL_ACC_NUM.getName())
						+ " Calculated hash ==  " + hash + " Merchant hash == " + fieldHash);
				return false;
			}

		} catch (Exception e) {
			logger.info("exception in Validate Hash ", e);
		}

		return true;
	}

}
