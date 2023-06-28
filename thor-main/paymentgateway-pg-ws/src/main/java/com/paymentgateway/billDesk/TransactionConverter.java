package com.paymentgateway.billDesk;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;
import com.paymentgateway.pg.core.util.BillDeskUtil;

/**
 * @author Rahul
 *
 */
@Service("billDeskTransactionConverter")
public class TransactionConverter {

	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;
	
	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private BillDeskUtil billDeskUtil;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private UserSettingDao userSettingDao;

	private static final Logger logger = LoggerFactory.getLogger(TransactionConverter.class.getName());

	@SuppressWarnings("incomplete-switch")
	public String perpareRequest(Fields fields, Transaction transaction) throws SystemException {

		String request = null;

		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case AUTHORISE:
		case ENROLL:
			request = enrollmentRequest(fields, transaction);
			fieldsDao.saveSessionParam(fields);
			break;
		case REFUND:
			request = refundRequest(fields, transaction);
			StringBuilder req = new StringBuilder();
			req.append(Constants.REQUEST_MSG);
			req.append(request);
			request = req.toString();
			break;
		case SALE:
			if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.NET_BANKING.getCode())) {
				request = saleRequest(fields, transaction);
			} else {
				request = authorizationRequest(fields, transaction);
			}
			break;
		case CAPTURE:
			break;
		case STATUS:
			request = statusEnquiryRequest(fields, transaction);
			break;
		}
		return request.toString();

	}

	public String enrollmentRequest(Fields fields, Transaction transaction) throws SystemException {

		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		String returnUrl = null;
		
		UserSettingData userSetting = userSettingDao.fetchDataUsingPayId(fields.get(FieldType.PAY_ID.getName()));
		if (userSetting.isAllowCustomHostedUrl()) {
			returnUrl = (userSetting.getCustomHostedUrl() + "/pgui/jsp/billdeskCardResponse?pgRefNo=");
		} else {
			returnUrl = PropertiesManager.propertiesMap.get(Constants.CARD_SALE_RETURN_URL);
		}
		returnUrl = returnUrl + fields.get(FieldType.PG_REF_NUM.getName());
		String currency = Currency.getAlphabaticCode(fields.get(FieldType.CURRENCY_CODE.getName()));
		JSONObject mainRequest = new JSONObject();

		StringBuilder request = new StringBuilder();
		request.append(fields.get(FieldType.MERCHANT_ID.getName()));
		request.append(Constants.PIPE);
		request.append(fields.get(FieldType.PG_REF_NUM.getName()));
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(amount);
		request.append(Constants.PIPE);
		request.append("CARD");
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(currency);
		request.append(Constants.PIPE);
		request.append(Constants.ITEM_CODE);
		request.append(Constants.PIPE);
		request.append(Constants.TYPE_FIELD_1);
		request.append(Constants.PIPE);
		request.append(fields.get(FieldType.PASSWORD.getName()));
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(Constants.TYPE_FIELD_2);
		request.append(Constants.PIPE);
		request.append(fields.get(FieldType.PG_REF_NUM.getName()));
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(returnUrl);

		String checkSum = billDeskUtil.generateChecksum(request.toString(), fields.get(FieldType.TXN_KEY.getName()));

		request.append(Constants.PIPE);
		request.append(checkSum);

		StringBuilder payRequest = new StringBuilder();
		payRequest.append(fields.get(FieldType.CARD_NUMBER.getName()));
		payRequest.append(Constants.PIPE);
		payRequest.append(transaction.getExpMonth());
		payRequest.append(Constants.PIPE);
		payRequest.append(transaction.getExpYear());
		payRequest.append(Constants.PIPE);
		payRequest.append(transaction.getCvv());
		payRequest.append(Constants.PIPE);
		payRequest.append(Constants.NA);
		payRequest.append(Constants.PIPE);
		payRequest.append(transaction.getMember());

		mainRequest.put("msg", request);
		mainRequest.put("paydata", "");
		mainRequest.put("ipaddress", fields.get(FieldType.INTERNAL_CUST_IP.getName()));
		mainRequest.put("useragent", fields.get(FieldType.INTERNAL_HEADER_USER_AGENT.getName()));
		logger.info("Enrollment Request message to BillDesk: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " "
				+ "Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + mainRequest);
		mainRequest.put("paydata", payRequest);
		return mainRequest.toString();

	}

	public String authorizationRequest(Fields fields, Transaction transaction) throws SystemException {

		JSONObject mainRequest = new JSONObject();
			
		StringBuilder payRequest = new StringBuilder();
		payRequest.append(fields.get(FieldType.CARD_NUMBER.getName()));
		payRequest.append(Constants.PIPE);
		payRequest.append(transaction.getExpMonth());
		payRequest.append(Constants.PIPE);
		payRequest.append(transaction.getExpYear());
		payRequest.append(Constants.PIPE);
		payRequest.append(transaction.getCvv());
		payRequest.append(Constants.PIPE);
		payRequest.append(Constants.NA);
		payRequest.append(Constants.PIPE);
		payRequest.append(transaction.getMember());

		mainRequest.put("paydata", payRequest);
		return mainRequest.toString();

	}

	public String saleRequest(Fields fields, Transaction transaction) throws SystemException {

		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		String returnUrl = null;
		
		UserSettingData userSetting = userSettingDao.fetchDataUsingPayId(fields.get(FieldType.PAY_ID.getName()));
		if (userSetting.isAllowCustomHostedUrl()) {
			returnUrl = (userSetting.getCustomHostedUrl() + "/pgui/jsp/billdeskResponse?pgRefNo=");
		} else {
			returnUrl = PropertiesManager.propertiesMap.get(Constants.SALE_RETURN_URL);
		}
		
		returnUrl = returnUrl + fields.get(FieldType.PG_REF_NUM.getName());
		String currency = Currency.getAlphabaticCode(fields.get(FieldType.CURRENCY_CODE.getName()));
		String bankCode = BillDeskBankCode.getBankCode(fields.get(FieldType.MOP_TYPE.getName()));

		StringBuilder request = new StringBuilder();
		request.append(fields.get(FieldType.MERCHANT_ID.getName()));
		request.append(Constants.PIPE);
		request.append(fields.get(FieldType.PG_REF_NUM.getName()));
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(amount);
		request.append(Constants.PIPE);
		request.append(bankCode);
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(currency);
		request.append(Constants.PIPE);
		request.append(Constants.ITEM_CODE);
		request.append(Constants.PIPE);
		request.append(Constants.TYPE_FIELD_1);
		request.append(Constants.PIPE);
		request.append(fields.get(FieldType.PASSWORD.getName()));
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(Constants.TYPE_FIELD_2);
		request.append(Constants.PIPE);
		request.append(fields.get(FieldType.PG_REF_NUM.getName()));
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(returnUrl);

		String checkSum = billDeskUtil.generateChecksum(request.toString(), fields.get(FieldType.TXN_KEY.getName()));

		request.append(Constants.PIPE);
		request.append(checkSum);

		logger.info("Request message to BillDesk: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " "
				+ "Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + request);
		return request.toString();

	}

	public String refundRequest(Fields fields, Transaction transaction) throws SystemException {
		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		DateFormat currentDate = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar calobj = Calendar.getInstance();
		String timeStamp = currentDate.format(calobj.getTime());

		String saleTxnDate = fields.get(FieldType.PG_DATE_TIME.getName());
		DateFormat oldFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		Date oldDate = null;
		try {
			oldDate = (Date) oldFormatter.parse(saleTxnDate);
		} catch (ParseException e) {
			logger.error("exception while parsing date " , e);
		}
		String txnDate = formatter.format(oldDate);

		StringBuilder request = new StringBuilder();
		request.append(Constants.REFUND_REQUEST_TYPE);
		request.append(Constants.PIPE);
		request.append(fields.get(FieldType.MERCHANT_ID.getName()));
		request.append(Constants.PIPE);
		request.append(fields.get(FieldType.ACQ_ID.getName()));
		request.append(Constants.PIPE);
		request.append(txnDate);
		request.append(Constants.PIPE);
		request.append(fields.get(FieldType.ORIG_TXN_ID.getName()));
		request.append(Constants.PIPE);
		request.append(Amount.toDecimal(fields.get(FieldType.SALE_TOTAL_AMOUNT.getName()),
				fields.get(FieldType.CURRENCY_CODE.getName())));
		request.append(Constants.PIPE);
		request.append(amount);
		request.append(Constants.PIPE);
		request.append(timeStamp);
		request.append(Constants.PIPE);
		request.append(fields.get(FieldType.PG_REF_NUM.getName()));
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(Constants.NA);
		request.append(Constants.PIPE);
		request.append(Constants.NA);

		String checkSum = billDeskUtil.generateChecksum(request.toString(), fields.get(FieldType.TXN_KEY.getName()));
		request.append(Constants.PIPE);
		request.append(checkSum);

		logger.info("Refund Request message to BillDesk: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " "
				+ "Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + request);
		return request.toString();

	}

	public String statusEnquiryRequest(Fields fields, Transaction transaction) {

		return null;

	}

	public TransactionConverter() {
	}

	public void logRequest(String requestMessage, Fields fields) {
		log("Request message to BillDesk: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id = "
				+ fields.get(FieldType.TXN_ID.getName()) + " " + "Url= " + requestMessage, fields);
	}

	private void log(String message, Fields fields) {
		message = Pattern.compile("(<card>)([\\s\\S]*?)(</card>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<pan>)([\\s\\S]*?)(</pan>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<expmonth>)([\\s\\S]*?)(</expmonth>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<expyear>)([\\s\\S]*?)(</expyear>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<cvv2>)([\\s\\S]*?)(</cvv2>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<password>)([\\s\\S]*?)(</password>)").matcher(message).replaceAll("$1$3");
		MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
		logger.info(message);
	}

	public Transaction toTransaction(String response, Fields fields) {
		Transaction transaction = new Transaction();
		String[] resArray = response.split("\\|");
		String receivedChecksum = resArray[13];

		String refinedMsg = response.replace("|" + receivedChecksum, "");
		String checkSumKey = fields.get(FieldType.TXN_KEY.getName());
		String calculatedChecksum = billDeskUtil.generateChecksum(refinedMsg, checkSumKey);

		if (!calculatedChecksum.equals(receivedChecksum)) {
			StringBuilder hashMessage = new StringBuilder("Merchant hash =");
			hashMessage.append(receivedChecksum);
			hashMessage.append(", Calculated Hash=");
			hashMessage.append(calculatedChecksum);
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), "HASHFAILED");
			fields.put(FieldType.PG_RESP_CODE.getName(), "LP999");
			logger.error("Hash Mismatch " + hashMessage.toString());

		} else {

			fields.put(FieldType.ACQ_ID.getName(), resArray[9]);
			fields.put(FieldType.PG_DATE_TIME.getName(), resArray[7]);
			fields.put(FieldType.PG_RESP_CODE.getName(), resArray[8]);
			fields.put(FieldType.PG_TXN_STATUS.getName(), resArray[12]);
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), resArray[10]);
		}
		return transaction;
	}
	
	public Transaction toTransactionAuthorization(String response, Fields fields) {
		Transaction transaction = new Transaction();
		JSONObject billdeskResponse = new JSONObject(response);
		String res = billdeskResponse.getString("msg");
		String[] resArray = res.split("\\|");
		String receivedChecksum = resArray[25];

		String refinedMsg = res.replace("|" + receivedChecksum, "");
		String calculatedChecksum = billDeskUtil.generateChecksum(refinedMsg, fields.get(FieldType.TXN_KEY.getName()));

		if (!calculatedChecksum.equals(receivedChecksum)) {
			StringBuilder hashMessage = new StringBuilder("Merchant hash =");
			hashMessage.append(receivedChecksum);
			hashMessage.append(", Calculated Hash=");
			hashMessage.append(calculatedChecksum);
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), "HASHFAILED");
			fields.put(FieldType.PG_RESP_CODE.getName(), "LP999");
			transaction.setResponseCode("LP999");
			logger.error("Hash Mismatch " + hashMessage.toString());

		} else {
			transaction.setResponseCode(resArray[14]);
			fields.put(FieldType.ACQ_ID.getName(), resArray[2]);
			fields.put(FieldType.RRN.getName(), resArray[3]);
			fields.put(FieldType.PG_DATE_TIME.getName(), resArray[13]);
			fields.put(FieldType.PG_RESP_CODE.getName(), resArray[14]);
			fields.put(FieldType.PG_TXN_STATUS.getName(), resArray[23]);
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), resArray[24]);
		}
		return transaction;
	}

	public Transaction statusToTransaction(String response, Fields fields) {
		Transaction transaction = new Transaction();
		String[] resArray = response.split("\\|");
		String receivedChecksum = resArray[32];

		String refinedMsg = response.replace("|" + receivedChecksum, "");
		String checkSumKey = fields.get(FieldType.TXN_KEY.getName());
		String calculatedChecksum = billDeskUtil.generateChecksum(refinedMsg, checkSumKey);

		if (!calculatedChecksum.equals(receivedChecksum)) {
			StringBuilder hashMessage = new StringBuilder("Merchant hash =");
			hashMessage.append(receivedChecksum);
			hashMessage.append(", Calculated Hash=");
			hashMessage.append(calculatedChecksum);
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), "HASHFAILED");
			fields.put(FieldType.PG_RESP_CODE.getName(), "LP999");
			logger.error("Hash Mismatch " + hashMessage.toString());

		} else {

			fields.put(FieldType.ACQ_ID.getName(), resArray[3]);
			fields.put(FieldType.RRN.getName(), resArray[4]);
			fields.put(FieldType.PG_DATE_TIME.getName(), resArray[14]);
			fields.put(FieldType.PG_RESP_CODE.getName(), resArray[15]);
			fields.put(FieldType.PG_TXN_STATUS.getName(), resArray[24]);
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), resArray[25]);
		}
		return transaction;
	}

	public Transaction toTransactionCard(String response) {
		Transaction transaction = new Transaction();
		if (response.contains("rdata")) {
			JSONObject billdeskResponse = new JSONObject(response);

			Map<String, String> rDataMap = new HashMap<String, String>();
			JSONObject rData = new JSONObject();
			rData = billdeskResponse.getJSONObject("rdata");

			JSONObject parameters = new JSONObject();
			parameters = rData.getJSONObject("parameters");

			for (Object key : rData.keySet()) {

				String key1 = key.toString();
				String value = rData.get(key.toString()).toString();
				rDataMap.put(key1, value);
			}

			transaction.setAcsUrl(rDataMap.get("url"));
			transaction.setPostParameters(parameters.toString());
		}
		return transaction;

	}

}
