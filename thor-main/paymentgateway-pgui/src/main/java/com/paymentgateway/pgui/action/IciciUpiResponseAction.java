package com.paymentgateway.pgui.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.AcquirerType;
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
import com.paymentgateway.pg.core.util.IciciUtil;
import com.paymentgateway.pg.core.util.UpiHistorian;

/**
 * @author Amitosh Aanand, Rahul , Shaiwal
 *
 */
@Service
public class IciciUpiResponseAction {

	String status = "";
	String pgTxnMsg = "";
	ErrorType errorType = null;

	public static Map<String, User> userMap = new HashMap<String, User>();

	@Autowired
	private IciciUtil iciciUtil;

	@Autowired
	private UpiHistorian upiHistorian;

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private UserDao userDao;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private UserSettingDao userSettingDao;


	private static Logger logger = LoggerFactory.getLogger(IciciUpiResponseAction.class.getName());

	public void iciciUpiResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {
		Fields responseField = null;

		try {
			BufferedReader inputBuffered = httpRequest.getReader();
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = inputBuffered.readLine()) != null) {
				response.append(inputLine);
			}
			inputBuffered.close();

			logger.info("ICICI Callback Response >>> " + response.toString());

			String decryptedResponse = null;

			if (response.toString().contains("TxnStatus")) {
				logger.info("Response is already decrypted");
				decryptedResponse = response.toString();
			} else {
				decryptedResponse = iciciUtil.decrypt(response.toString());
			}

			logger.info("ICICI Decrypted Callback Response  " + decryptedResponse);

			JSONObject jsonResponse = new JSONObject(decryptedResponse);
			Fields fields = new Fields();
			User responseUser = null;

			String responseMessage = jsonResponse.getString("TxnStatus");
			String payerAmount = jsonResponse.getString("PayerAmount");
			String payerName = jsonResponse.getString("PayerName");
			String payerMobile = jsonResponse.getString("PayerMobile");
			String payerVpa = jsonResponse.getString("PayerVA");
			String pgRefNum = jsonResponse.getString("merchantTranId");
			String bankRRN = jsonResponse.getString("BankRRN");
			String merchantTranId = jsonResponse.getString("merchantTranId");
			String txnCompletionDate = jsonResponse.getString("TxnCompletionDate");
			String txnInitDate = jsonResponse.getString("TxnInitDate");

			updateStatusResponse(responseMessage);
			fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.ICICIUPI.getCode());
			fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());

			logger.info("Checking if response is for Static QR Transaction");

			if (merchantTranId.length() == 8) {

				Date dNow = new Date();
				String dateNow = DateCreater.formatDateForDb(dNow);

				MongoDatabase dbIns = null;
				dbIns = mongoInstance.getDB();
				MongoCollection<Document> collection = dbIns.getCollection(
						propertiesManager.propertiesMap.get("MONGO_DB_" + Constants.CUST_QR_COLLECTION.getValue()));

				List<BasicDBObject> vpaQuery = new ArrayList<BasicDBObject>();

				vpaQuery.add(new BasicDBObject("CUSTOMER_ID", merchantTranId));

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
				fields.put(FieldType.ACQ_ID.getName(), bankRRN);
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

				fields.put(FieldType.ORDER_ID.getName(), orderId);
				fields.put(FieldType.CURRENCY_CODE.getName(), "356");
				fields.put(FieldType.STATUS.getName(), status);
				fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
				fields.put(FieldType.PAYMENT_TYPE.getName(), PaymentType.UPI.getCode());
				fields.put(FieldType.PG_DATE_TIME.getName(), dateNow);
				fields.put(FieldType.PG_RESP_CODE.getName(), errorType.getResponseCode());
				fields.put(FieldType.PG_TXN_STATUS.getName(), responseMessage);
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), errorType.getResponseMessage());
				fields.put(FieldType.UDF3.getName(), payerVpa);
				fields.put(FieldType.RRN.getName(), bankRRN);
				fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
				fields.put(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName(), "India");

				UserSettingData merchntSettings=userSettingDao.fetchDataUsingPayId(responseUser.getPayId());
				
				if (merchntSettings.isSurchargeFlag()) {
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

			}

			else {
				logger.info("Response is not for Static QR Transaction");

				fields.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
				upiHistorian.findPrevious(fields);
				logger.info("After historian ICICI upi" + fields.getFieldsAsString());

				if (StringUtils.isBlank(fields.get(FieldType.MOP_TYPE.getName()))) {
					fields.put(FieldType.MOP_TYPE.getName(), MopType.UPI_QR.getCode());
					fields.put(FieldType.ORIG_TXN_ID.getName(), fields.get(FieldType.PG_REF_NUM.getName()));
					fields.put(FieldType.PAYMENT_TYPE.getName(), PaymentType.UPI.getCode());
				}

			}

			fields.put(FieldType.STATUS.getName(), status);
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
			fields.put(FieldType.PG_RESP_CODE.getName(), errorType.getResponseCode());
			fields.put(FieldType.PG_TXN_STATUS.getName(), responseMessage);
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), errorType.getResponseMessage());
			fields.put(FieldType.UDF3.getName(), payerVpa);
			fields.put(FieldType.PAYER_ADDRESS.getName(), payerVpa);
			fields.put(FieldType.RRN.getName(), bankRRN);
			fields.put(FieldType.PG_DATE_TIME.getName(), txnCompletionDate);
			fields.put(FieldType.STATUS.getName(), status.toString());
			fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
			logger.info("fields send to transact " + fields.getFieldsAsString());

			Map<String, String> resp = transactionControllerServiceProvider.transact(fields,
					Constants.TXN_WS_UPI_PROCESSOR.getValue());
			responseField = new Fields(resp);
			logger.info("Response received from WS for ICICI upi " + responseField.getFieldsAsString());
		} catch (Exception e) {
			logger.error("Error in ICICI bank UPI callback = ", e);
			String path = httpRequest.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host") + "/pgui/jsp/error";
				httpResponse.sendRedirect(resultPath);
			}
			httpResponse.sendRedirect("error");
		}
	}

	public void updateStatusResponse(String receivedResponse) throws SystemException {
		try {
			logger.info("Inside ICICI UPI Response action in updateStatusResponse method response message received is "
					+ receivedResponse);
			if (receivedResponse.equals(Constants.ICICI_UPI_SUCCESS_RESPONSE_MSG.getValue())) {
				status = StatusType.CAPTURED.getName();
				errorType = ErrorType.SUCCESS;
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.DECLINED;
				pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
			}
		} catch (Exception e) {
			logger.error("Unknown Exception :", e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"Unknown exception in  ICICIUpiResponseAction");
		}
	}

}
