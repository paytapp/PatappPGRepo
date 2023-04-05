package com.paymentgateway.pgui.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.EPOSTransactionDao;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.kms.EncryptDecryptService;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.YesbankCbUpiResultType;
import com.paymentgateway.commons.util.threadpool.ThreadPoolProvider;
import com.paymentgateway.pg.core.util.UpiHistorian;
import com.paymentgateway.pg.core.util.YesBankUpiUtil;

@Service
public class YesBankUpiResponseAction {

	private static Logger logger = LoggerFactory.getLogger(YesBankUpiResponseAction.class.getName());

	private String payId;

	String status = "";
	ErrorType errorType = null;
	String pgTxnMsg = "";

	@Autowired
	TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private YesBankUpiUtil yesBankUpiUtil;

	@Autowired
	private UpiHistorian upiHistorian;

	@Autowired
	EncryptDecryptService encryptDecryptService;

	@Autowired
	EPOSTransactionDao eposDao;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	private PropertiesManager propertiesManager;

	public void yesBankUpiResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {
		payId = httpRequest.getParameter("payId");
		
		if (StringUtils.isBlank(payId)) {
			logger.info("YESBANKCB APPROVED CALLBACK RESPONSE NO PAYID ");
		} else {
			logger.info("YESBANKCB APPROVED CALLBACK RESPONSE WITH PAYID " + payId);
		}

		Fields responseField = null;
		try {

			BufferedReader rd = httpRequest.getReader();
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			logger.info("YESBANKCB APPROVED CALLBACK RESPONSE ENCRYPTED STRING 1  " + response);
			String resStr = response.toString();
			logger.info("YESBANKCB APPROVED CALLBACK RESPONSE ENCRYPTED STRING 2  " + resStr);
			String encrypted = resStr.substring(6, resStr.length() - 1);
			logger.info("YESBANKCB APPROVED CALLBACK RESPONSE encrypted  " + encrypted);
			Fields fields = new Fields();

			PropertiesManager propertiesManager = new PropertiesManager();
			String key = "";

			if (StringUtils.isBlank(payId)) {

				key = propertiesManager.propertiesMap.get(Constants.YESBANK_UPI_MERCHANT_KEY.getValue());
			}
			// To extract key from payId present in URL.
			else {
				User user = new UserDao().findPayId(payId);
				String acquirerCode = AcquirerType.YESBANKCB.getCode();
				Account account = user.getAccountUsingAcquirerCode(acquirerCode);

				try {

					logger.info("YESBANKCB APPROVED CALLBACK RESPONSE DEFAULT CURRENCY  "
							+ Constants.DEFAULT_CURRENCY_CODE.getValue());

					AccountCurrency accountCurrency = account
							.getAccountCurrency(Constants.DEFAULT_CURRENCY_CODE.getValue());

					// Decrypt values

					if (!StringUtils.isEmpty(accountCurrency.getAdf7())) {
						key = encryptDecryptService.decrypt(payId, accountCurrency.getAdf7());
						logger.info("key value if payID is present  " + key);
					}

				} catch (Exception e) {

					logger.error(
							"Error in YEs bank UPI callback for Gpay while getting the key from accountCurrency = ", e);
					String path = httpRequest.getContextPath();
					logger.info(path);
					if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
						String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host")
								+ "/pgui/jsp/error";
						httpResponse.sendRedirect(resultPath);
					}
					httpResponse.sendRedirect("error");
				}

			}

			String decryptedString = "";
			try {
				decryptedString = yesBankUpiUtil.decrypt(encrypted, key);

			} catch (Exception e) {

				logger.error("Error in YES bank UPI callback data decryption = ", e);
				String path = httpRequest.getContextPath();
				logger.info(path);
				if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
					String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host")
							+ "/pgui/jsp/error";
					httpResponse.sendRedirect(resultPath);
				}
				httpResponse.sendRedirect("error");
			}

			String[] value_split = decryptedString.split("\\|");

			if (value_split[2].equalsIgnoreCase(Constants.COLLECT_AUTH.getValue())) {

				logger.info("YESBANKCB APPROVED CALLBACK RESPONSE DECRYPT VALUE " + decryptedString);

				String receivedResponseCode = value_split[7];
				String receivedResponse = value_split[5];

				updateStatusResponse(receivedResponseCode, receivedResponse);

				String pgTxn = pgTxnMsg;
				String responseCode = value_split[7];
				String pgDateTime = value_split[4];
				String responseMsg = status;
				String ReferenceId = value_split[18];
//				String amount = value_split[3];
				String pgRefNum = value_split[1];
				String rrn = value_split[16];
				String merchantVPA = value_split[20];
				String payerAddress = value_split[10];
				String arn = value_split[11];

				logger.info("Merchant VPA YES BANK UPI " + merchantVPA);

				fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.YESBANKCB.getCode());
				fields.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
				fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
				logger.info("fields before historian " + fields.getFieldsAsString());
				upiHistorian.findPrevious(fields);
				logger.info("After historian " + fields.getFieldsAsString());
				fields.put(FieldType.STATUS.getName(), status);
				fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.toString().replaceAll("_", ""));
				fields.put(FieldType.PG_RESP_CODE.getName(), responseCode);
				fields.put(FieldType.PG_TXN_STATUS.getName(), responseMsg);
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxn);
				fields.put(FieldType.UDF1.getName(), merchantVPA);
				fields.put(FieldType.CARD_MASK.getName(), payerAddress);
				fields.put(FieldType.ARN.getName(), arn);
				fields.put(FieldType.ACQ_ID.getName(), rrn);
				fields.put(FieldType.RRN.getName(), rrn);
				fields.put(FieldType.PG_DATE_TIME.getName(), pgDateTime);
				fields.put(FieldType.AUTH_CODE.getName(), ReferenceId);
				fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");

				logger.info("fields send to transact  FOR YES BANK UPI" + fields.get(FieldType.TXNTYPE.getName()) + " "
						+ "Txn id" + fields.get(FieldType.TXN_ID.getName()) + " " + fields.getFieldsAsString());

				Map<String, String> res = transactionControllerServiceProvider.transact(fields,
						Constants.TXN_WS_UPI_PROCESSOR.getValue());

				Fields Fields = new Fields();
				Fields.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
				Fields.put(FieldType.STATUS.getName(), fields.get(FieldType.STATUS.getName()));
				Fields.put(FieldType.MOP_TYPE.getName(), fields.get(FieldType.MOP_TYPE.getName()));
				if (Boolean.valueOf(fields.get(FieldType.EPOS_MERCHANT.getName()))) {
					ExecutorService es = ThreadPoolProvider.getExecutorService();
					es.execute(new Runnable() {
						@Override
						public void run() {
							eposDao.updateEposCharges(Fields);
							Fields.removeInternalFields();
							Fields.removeSecureFields();
							Fields.remove(FieldType.ORIG_TXN_ID.getName());
							Fields.remove(FieldType.HASH.getName());
						}
					});
					es.shutdown();
				}

				res.remove(FieldType.ORIG_TXN_ID.getName());

				responseField = new Fields(res);
				res.remove(FieldType.ORIG_TXN_ID.getName());
				logger.info("Response received from WS FOR YES BANK UPI" + responseField);

			} else if (value_split[2].equalsIgnoreCase(Constants.PAYMENT_RECV.getValue())) {
				logger.info("YESBANKCB APPROVED CALLBACK RESPONSE received as PAYMENT_RECV  "
						+ fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id"
						+ fields.get(FieldType.TXN_ID.getName()) + " " + decryptedString);

				Date dNow = new Date();
				String dateNow = DateCreater.formatDateForDb(dNow);

				String[] qr_value_split = decryptedString.split("\\|");
				String pgRefNum = qr_value_split[1];
				fields = fieldsDao.getPreviousForPgRefNum(pgRefNum);
				if (!fields.get(FieldType.RESPONSE_CODE.getName())
						.equalsIgnoreCase(ErrorType.INVALID_PAYID_ATTEMPT.getCode())) {
					User responseUser = null;
					String receivedResponseCode = value_split[7];
					String receivedResponse = value_split[5];

					updateStatusResponse(receivedResponseCode, receivedResponse);

					String pgTxn = pgTxnMsg;
					String responseCode = value_split[7];
					String pgDateTime = value_split[4];
					String responseMsg = status;
					String ReferenceId = value_split[18];
//				String amount = value_split[3];
					// String pgRefNum = value_split[1];
					String rrn = value_split[16];
					String merchantVPA = value_split[20];
					String payerAddress = value_split[10];
					String arn = value_split[11];

					logger.info("Merchant VPA YES BANK UPI " + merchantVPA);

					fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.YESBANKCB.getCode());
					fields.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
					fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
					logger.info("fields before historian " + fields.getFieldsAsString());
					upiHistorian.findPrevious(fields);
					logger.info("After historian " + fields.getFieldsAsString());
					fields.put(FieldType.STATUS.getName(), status);
					fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.toString().replaceAll("_", ""));
					fields.put(FieldType.PG_RESP_CODE.getName(), responseCode);
					fields.put(FieldType.PG_TXN_STATUS.getName(), responseMsg);
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxn);
					fields.put(FieldType.UDF1.getName(), merchantVPA);
					fields.put(FieldType.CARD_MASK.getName(), payerAddress);
					fields.put(FieldType.ARN.getName(), arn);
					fields.put(FieldType.ACQ_ID.getName(), rrn);
					fields.put(FieldType.RRN.getName(), rrn);
					fields.put(FieldType.PG_DATE_TIME.getName(), pgDateTime);
					fields.put(FieldType.AUTH_CODE.getName(), ReferenceId);
					fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");

					if (responseUser.isSurchargeFlag()) {
						fields.put(FieldType.SURCHARGE_FLAG.getName(), "Y");
					} else {
						fields.put(FieldType.SURCHARGE_FLAG.getName(), "N");
					}

					fields.put(FieldType.CREATE_DATE.getName(), dateNow);
					fields.put(FieldType.UPDATE_DATE.getName(), dateNow);
					fields.put(FieldType.INSERTION_DATE.getName(), dateNow);
					fields.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));

					fields.put(FieldType.ACQUIRER_TDR_SC.getName(), "0.00");
					fields.put(FieldType.ACQUIRER_GST.getName(), "0.00");
					fields.put(FieldType.PG_GST.getName(), "0.00");
					fields.put(FieldType.PG_TDR_SC.getName(), "0.00");
					fields.put(FieldType.MERCHANT_TDR_SC.getName(), "0.00");
					fields.put(FieldType.MERCHANT_GST.getName(), "0.00");
					fields.put(FieldType.RESELLER_CHARGES.getName(), "0.00");
					fields.put(FieldType.RESELLER_GST.getName(), "0.00");

				} else {
					logger.info("YESBANK UPi static QR response =  " + " " + "Txn id"
							+ fields.get(FieldType.TXN_ID.getName()) + " " + pgRefNum);

					User responseUser = null;
					MongoDatabase dbIns = null;
					dbIns = mongoInstance.getDB();
					MongoCollection<Document> collection = dbIns.getCollection(
							propertiesManager.propertiesMap.get("MONGO_DB_" + Constants.CUST_QR_COLLECTION.getValue()));

					List<BasicDBObject> vpaQuery = new ArrayList<BasicDBObject>();

					vpaQuery.add(new BasicDBObject("CUSTOMER_ID", "1232131232"));

					BasicDBObject finalQuery = new BasicDBObject("$and", vpaQuery);
					Document doc = new Document();
					long count = collection.countDocuments(finalQuery);

					if (count == 1) {
						logger.info("This response is for Static QR Transaction , merchantTranId == " + pgRefNum);

						MongoCursor<Document> cursor = collection.find(finalQuery).iterator();
						while (cursor.hasNext()) {
							doc = cursor.next();
							String payId = doc.get("PAY_ID").toString();
							responseUser = userDao.findPayId(payId);
							break;
						}
					} else {

						logger.info("Customer Id count for Pg Ref Num == " + pgRefNum + " is , count == " + count
								+ " unable to send callback due to invalid number of customer account");
						responseUser = null;

					}
					String txnId = TransactionManager.getNewTransactionId();
					String orderId = txnId;
					

					
					fields.put(FieldType.PG_REF_NUM.getName(), txnId);
					fields.put(FieldType.ORIG_TXN_ID.getName(), txnId);
					fields.put(FieldType.TXN_ID.getName(), txnId);
					fields.put(FieldType.OID.getName(), txnId);
					
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

					fields.put("_id", txnId);

					String receivedResponseCode = value_split[7];
					String receivedResponse = value_split[5];

					updateStatusResponse(receivedResponseCode, receivedResponse);

					String pgTxn = pgTxnMsg;
					String responseCode = value_split[7];
					String pgDateTime = value_split[4];
					String responseMsg = status;
					String ReferenceId = value_split[18];
//				String amount = value_split[3];
					// String pgRefNum = value_split[1];
					String rrn = value_split[16];
					String merchantVPA = value_split[20];
					String payerAddress = value_split[10];
					String arn = value_split[11];

					fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.YESBANKCB.getCode());
					fields.put(FieldType.MOP_TYPE.getName(), MopType.STATIC_UPI_QR.getCode());
					fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
					fields.put(FieldType.STATUS.getName(), status);
					fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.toString().replaceAll("_", ""));
					fields.put(FieldType.PG_RESP_CODE.getName(), responseCode);
					fields.put(FieldType.PG_TXN_STATUS.getName(), responseMsg);
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxn);
					fields.put(FieldType.UDF1.getName(), merchantVPA);
					fields.put(FieldType.CARD_MASK.getName(), payerAddress);
					fields.put(FieldType.ARN.getName(), arn);
					fields.put(FieldType.ACQ_ID.getName(), rrn);
					fields.put(FieldType.RRN.getName(), rrn);
					fields.put(FieldType.PG_DATE_TIME.getName(), pgDateTime);
					fields.put(FieldType.AUTH_CODE.getName(), ReferenceId);
					fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
					fields.put(FieldType.SURCHARGE_FLAG.getName(), "N");
					fields.put(FieldType.ORDER_ID.getName(), orderId);
					fields.put(FieldType.CREATE_DATE.getName(), dateNow);
					fields.put(FieldType.UPDATE_DATE.getName(), dateNow);
					fields.put(FieldType.INSERTION_DATE.getName(), dateNow);
					fields.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));

					fields.put(FieldType.ACQUIRER_TDR_SC.getName(), "0.00");
					fields.put(FieldType.ACQUIRER_GST.getName(), "0.00");
					fields.put(FieldType.PG_GST.getName(), "0.00");
					fields.put(FieldType.PG_TDR_SC.getName(), "0.00");
					fields.put(FieldType.MERCHANT_TDR_SC.getName(), "0.00");
					fields.put(FieldType.MERCHANT_GST.getName(), "0.00");
					fields.put(FieldType.RESELLER_CHARGES.getName(), "0.00");
					fields.put(FieldType.RESELLER_GST.getName(), "0.00");

				}
				Map<String, String> resp = transactionControllerServiceProvider.transact(fields,
						Constants.TXN_WS_UPI_PROCESSOR.getValue());
				responseField = new Fields(resp);
				logger.info("Response received from WS for Yes upi " + responseField.getFieldsAsString());
			}
		} catch (Exception e) {
			logger.error("Error in YEs bank UPI callback 2 = ", e);
			String path = httpRequest.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host") + "/pgui/jsp/error";
				httpResponse.sendRedirect(resultPath);
			}
			httpResponse.sendRedirect("error");
		}

	}

	public void updateStatusResponse(String receivedResponseCode, String receivedResponse) throws SystemException {
		try {
			logger.info(" inside YESBANKCB Response action in  updateStatusResponse method response code is ==  "
					+ receivedResponseCode);
			if (receivedResponseCode.equals(Constants.YES_UPI_SUCCESS_CODE.getValue())
					&& receivedResponse.equals(Constants.YES_UPI_RESPONSE.getValue())) {
				status = StatusType.CAPTURED.getName();
				errorType = ErrorType.SUCCESS;
			} else {
				if (StringUtils.isNotBlank(receivedResponseCode)) {
					YesbankCbUpiResultType resultInstance = YesbankCbUpiResultType
							.getInstanceFromName(receivedResponseCode);
					logger.info(
							" inside YESBANKCB Response action in  updateStatusResponse method resultInstance is : == "
									+ resultInstance);
					if (resultInstance != null) {
						if (resultInstance.getPaymentGatewayCode() != null) {
							logger.info(
									" inside YESBANKCB Response action in  updateStatusResponse method resultInstance is ==  "
											+ resultInstance.getStatusName() + (resultInstance.getPaymentGatewayCode()));
							status = resultInstance.getStatusName();
							errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
							pgTxnMsg = resultInstance.getMessage();
						} else {
							status = StatusType.REJECTED.getName();
							errorType = ErrorType.REJECTED;
							pgTxnMsg = ErrorType.REJECTED.getResponseMessage();

						}

					} else {
						status = StatusType.REJECTED.getName();
						errorType = ErrorType.REJECTED;
						pgTxnMsg = ErrorType.REJECTED.getResponseMessage();

					}

				} else {
					status = StatusType.REJECTED.getName();
					errorType = ErrorType.REJECTED;
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();

				}
			}
		} catch (Exception e) {
			logger.error("Unknown Exception :" + e.getMessage());
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e, "unknown exception in  yesUpiResponseAction");
		}
	}
}
