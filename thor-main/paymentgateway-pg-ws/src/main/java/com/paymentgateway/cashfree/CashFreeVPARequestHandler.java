package com.paymentgateway.cashfree;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
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
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.SystemException;
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
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;

/*
*@auther Vishal Yadav
*/

@Service
public class CashFreeVPARequestHandler {

	private static Logger logger = LoggerFactory.getLogger(CashFreeVPARequestHandler.class.getName());
	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private CashFreeVPAResponseHandler cashFreeResponseHandler;

	@Autowired
	private CashFreeVPADBEntry cashFreeDBEntry;

	@Autowired
	private UserDao userDao;

	public static String TO_GENTERATE_NEW_TOKEN = PropertiesManager.propertiesMap
			.get("CASHFREE_TO_GENTERATE_NEW_TOKEN");
	public static String TO_GENTERATE_VIRTUAL_ACCOUNT = PropertiesManager.propertiesMap
			.get("CASHFREE_TO_GENTERATE_VIRTUAL_ACCOUNT");
	public static String TO_GENTERATE_VPA = PropertiesManager.propertiesMap.get("CASHFREE_TO_GENTERATE_VPA");
	public static String TO_GET_QR_CODE_FOR_EXISTING = PropertiesManager.propertiesMap
			.get("CASHFREE_TO_GET_QR_CODE_FOR_EXISTING");
	public static String CASHFREE_CLIENT_ID = PropertiesManager.propertiesMap.get("CASHFREE_CLIENT_ID");
	public static String CASHFREE_CLIENT_SECRET = PropertiesManager.propertiesMap.get("CASHFREE_CLIENT_SECRET");
	public static String TO_UPDATE_STATUS = PropertiesManager.propertiesMap.get("CASHFREE_TO_UPDATE_STATUS");

	public static String requestCreater(String request, String vpa, Fields fields) throws SystemException {
		String hostUrl = "";
		String responseData = "";
		try {
			String requestTYpe = "POST";
			String requestMethod = fields.get(FieldType.REQUEST_TYPE.getName());

			switch (requestMethod) {
			case "TO_GENTERATE_NEW_TOKEN":
				hostUrl = TO_GENTERATE_NEW_TOKEN;
				break;
			case "TO_GENTERATE_VIRTUAL_ACCOUNT":
				hostUrl = TO_GENTERATE_VIRTUAL_ACCOUNT;
				break;
			case "TO_GENTERATE_VPA":
				hostUrl = TO_GENTERATE_VPA;
				break;
			case "TO_GET_QR_CODE_FOR_EXISTING":
				hostUrl = TO_GET_QR_CODE_FOR_EXISTING + vpa;
				break;
			case "TO_UPDATE_STATUS":
				hostUrl = TO_UPDATE_STATUS;
				break;
			}
			logger.info("------url-----" + hostUrl);
			URL url = new URL(hostUrl);

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(requestTYpe);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", "Bearer " + fields.get(FieldType.TOKEN.getName()));
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			// connection.setConnectTimeout(60000);
			// connection.setReadTimeout(60000);

			DataOutputStream requestWriter = new DataOutputStream(connection.getOutputStream());
			requestWriter.writeBytes(request);
			requestWriter.close();

			InputStream is = connection.getInputStream();
			BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(is));
			String decodedString;
			while ((decodedString = bufferedreader.readLine()) != null) {
				responseData = responseData + decodedString;
			}
			bufferedreader.close();

		} catch (Exception e) {
			logger.error("Exception in CashFree", e);
		}
		return responseData;

	}

	public String genrateQRCode(Fields fields, String vpa) {
		try {
			HttpsURLConnection connection = null;
			StringBuilder serverResponse = new StringBuilder();
			String hostUrl = TO_GET_QR_CODE_FOR_EXISTING + vpa;
			URL url = new URL(hostUrl);

			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", "Bearer " + fields.get(FieldType.TOKEN.getName()));
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Language", "en-US");
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
				serverResponse.append('\r');
			}
			rd.close();
			String str = serverResponse.toString();
			return str;

		} catch (IOException e) {
			logger.error("Error communicating with CashFree  QR Code : ", e);
		}
		return null;

	}

	public String genrateDynamicQRCode(Fields fields, String vpa) {
		try {

			HttpsURLConnection connection = null;
			StringBuilder serverResponse = new StringBuilder();
			String hostUrl = "https://cac-api.cashfree.com/cac/v1/createDynamicQRCode?virtualVpaId=" + vpa + "&amount="
					+ fields.get(FieldType.AMOUNT.getName());
			URL url = new URL(hostUrl);

			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", "Bearer " + fields.get(FieldType.TOKEN.getName()));
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Language", "en-US");
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
				serverResponse.append('\r');
			}
			rd.close();
			String str = serverResponse.toString();
			return str;

		} catch (IOException e) {
			logger.error("Error communicating with CashFree  QR Code : ", e);
		}
		return null;

	}

	public static String requestCreaterToken(String request, String vpa, Fields fields, String clientId,
			String clientSecret) throws SystemException {
		String hostUrl = "";
		String responseData = "";
		try {
			String requestMethod = fields.get(FieldType.REQUEST_TYPE.getName());
			switch (requestMethod) {
			case "TO_GENTERATE_NEW_TOKEN":
				hostUrl = TO_GENTERATE_NEW_TOKEN;
				break;
			case "TO_GENTERATE_VIRTUAL_ACCOUNT":
				hostUrl = TO_GENTERATE_VIRTUAL_ACCOUNT;
				break;
			case "TO_GENTERATE_VPA":
				hostUrl = TO_GENTERATE_VPA;
				break;
			case "TO_GET_QR_CODE_FOR_EXISTING":
				hostUrl = TO_GET_QR_CODE_FOR_EXISTING + vpa;
				break;
			case "TO_UPDATE_STATUS":
				hostUrl = TO_UPDATE_STATUS;
				break;
			}
			logger.info("------url-----" + hostUrl);
			URL url = new URL(hostUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("X-Client-Id", clientId);
			connection.setRequestProperty("X-Client-Secret", clientSecret);
			// connection.setRequestProperty("Authorization", "Bearer
			// "+fields.get(FieldType.TOKEN.getName()));
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			// connection.setConnectTimeout(60000);
			// connection.setReadTimeout(60000);

			DataOutputStream requestWriter = new DataOutputStream(connection.getOutputStream());
			requestWriter.writeBytes(request);
			requestWriter.close();
			InputStream is = connection.getInputStream();
			BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(is));
			String decodedString;
			while ((decodedString = bufferedreader.readLine()) != null) {
				responseData = responseData + decodedString;
			}
			bufferedreader.close();

		} catch (Exception e) {
			logger.error("Exception in CashFree ", e);
		}
		return responseData;

	}

	public Fields genrateToken(Fields fields) {
		JSONObject respObj = null;
		try {
			if (StringUtils.isNotBlank(fields.get(FieldType.PAY_ID.getName()))) {
				Map<String, String> txnData = new HashMap<String, String>();
				txnData = getTxnKey(fields.get(FieldType.PAY_ID.getName()));
				String clientid = txnData.get(FieldType.MERCHANT_ID.getName());
				String clientSecret = txnData.get(FieldType.TXN_KEY.getName());
				respObj = new JSONObject();
				respObj.put("clientId", clientid);
				respObj.put("clientSecret", clientSecret);
				fields.put(FieldType.REQUEST_TYPE.getName(), "TO_GENTERATE_NEW_TOKEN");
				String response = requestCreaterToken(respObj.toString(), null, fields, clientid, clientSecret);
				fields = cashFreeResponseHandler.genrateNewTokenResponse(response, fields);
				fields.logAllFields("Fields in generate token response for custID >> " + fields.get(FieldType.CUST_ID.getName()));
			}
		} catch (Exception e) {
			logger.error("Exception in CashFree OR gentateToken >>> ", e);
		}
		return fields;
	}

	public Map<String, String> genrateVirtualAccountRequest(Fields fields) {
		Map<String, String> saveResponse = new HashMap<String, String>();
		JSONObject respObj = null;
		try {
			respObj = new JSONObject();
			respObj.put("vAccountId", fields.get(FieldType.CUST_ID.getName()));
			respObj.put("name", fields.get(FieldType.CUST_NAME.getName()));
			respObj.put("phone", fields.get(FieldType.PHONE_NO.getName()));
			respObj.put("email", fields.get(FieldType.CUST_EMAIL.getName()));
			respObj.put("notifGroup", "DEFAULT");
			fields.put(FieldType.REQUEST_TYPE.getName(), "TO_GENTERATE_VIRTUAL_ACCOUNT");
			String response = requestCreater(respObj.toString(), null, fields);
			saveResponse = cashFreeResponseHandler.genrateVirtualAccountResponse(response, fields);

		} catch (Exception e) {
			logger.error("Exception in CashFree OR virtual account request >>> ", e);
		}
		return saveResponse;
	}
	
	public Map<String,String> vaUpdateStatusRequest(Fields fields) {
		
		JSONObject respObj = null;
		try {
				respObj = new JSONObject();
				respObj.put("vAccountId", fields.get(FieldType.CUST_ID.getName()));
				respObj.put("status", fields.get(FieldType.UPDATE_STATUS.getName()));
				
				fields.put(FieldType.REQUEST_TYPE.getName(),"TO_UPDATE_STATUS");
				String response =requestCreater(respObj.toString(), null, fields);
				
				cashFreeResponseHandler.updateVAStatusResponseHandler(response, fields);
				
		}catch (Exception e) {
			logger.info("Exception in upiQrUpdateStatus ",e);
		}
		return fields.getFields();
	}

	public Map<String, String> genrateVPARequst(Fields fields) {
		Map<String, String> saveResponse = new HashMap<String, String>();
		JSONObject respObj = null;
		try {
			respObj = new JSONObject();
			respObj.put("virtualVpaId", fields.get(FieldType.CUST_ID.getName()));
			respObj.put("name", fields.get(FieldType.CUST_NAME.getName()));
			respObj.put("phone", fields.get(FieldType.CUST_PHONE.getName()));
			respObj.put("email", fields.get(FieldType.CUST_EMAIL.getName()));
			respObj.put("notifGroup", "DEFAULT");
			fields.put(FieldType.REQUEST_TYPE.getName(), "TO_GENTERATE_VPA");
			fields.logAllFields(
					"requst fields for genrating vparequest for custID >> " + fields.get(FieldType.CUST_ID.getName()));
			String response = requestCreater(respObj.toString(), null, fields);
			saveResponse = cashFreeResponseHandler.genrateVpaResponse(response, fields);

		} catch (Exception e) {
			logger.error("Exception in CashFree OR gentate vpa >>> ", e);
		}
		return saveResponse;
	}

	public Map<String, String> genrateCreateQRCodeRequst(Fields fields) {
		Map<String, String> saveResponse = new HashMap<String, String>();
		try {
			fields.put(FieldType.REQUEST_TYPE.getName(), "TO_GET_QR_CODE_FOR_EXISTING");
			fields.logAllFields(
					"fields for genrateCreateQRCodeRequst for custID >> " + fields.get(FieldType.CUST_ID.getName()));
			String response = genrateQRCode(fields, fields.get(FieldType.VPA.getName()));
			saveResponse = cashFreeResponseHandler.qrCodeForExistingResponse(response, fields);
		} catch (Exception e) {
			logger.error("Exception in CashFree OR gentate QR code >>> ", e);
		}
		return saveResponse;
	}

	public Map<String, String> genrateCreateDynamicQRCodeRequest(Fields fields) {
		Map<String, String> saveResponse = new HashMap<String, String>();
		try {
			fields.put(FieldType.REQUEST_TYPE.getName(), "TO_GET_QR_CODE_FOR_EXISTING");
			fields.logAllFields("fields for genrateCreateDynamicQRCodeRequst");
			String response = genrateDynamicQRCode(fields, fields.get(FieldType.CUST_ID.getName()));
			saveResponse = cashFreeResponseHandler.qrCodeForExistingResponse(response, fields);

		} catch (Exception e) {
			logger.error("Exception in CashFree OR gentate QR code >>> ", e);
		}
		return saveResponse;
	}

	public Map<String, String> getDataByAccountNo(Fields fields) {
		Map<String, String> saveResponse = new HashMap<String, String>();
		try {
			saveResponse = cashFreeDBEntry.getDataByAccountNo(fields);

		} catch (Exception e) {
			logger.error("Exception in CashFree OR DB entries >>> ", e);
		}
		return saveResponse;
	}

	public Map<String, String> genrateCashFreeQRCode(Fields fields) {
		Map<String, String> saveResponse = new HashMap<String, String>();
		try {

			int reqid = Integer.parseInt(fields.get(FieldType.SLAB_ID.getName()));

			switch (reqid) {
			// for generating VPA and QR
			case 1:
				saveResponse = genrateVPARequst(fields);
				if (fields.get(FieldType.RESPONSE_CODE.getName()) != null
						&& fields.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase("000")) {
					cashFreeDBEntry.addNewEntryData(fields, "virtualVPA");
					saveResponse.clear();
					saveResponse = genrateCreateQRCodeRequst(fields);
					if (fields.get(FieldType.RESPONSE_CODE.getName()) != null
							&& fields.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase("000")) {
						cashFreeDBEntry.updateDataEntry(fields, "virtualQR", fields.get(FieldType.CUST_ID.getName()));
					}
				}
				break;
			// VPA is already regerated case for QR generating
			case 2:
				saveResponse = genrateCreateQRCodeRequst(fields);
				if (fields.get(FieldType.RESPONSE_CODE.getName()) != null
						&& fields.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase("000")) {
					cashFreeDBEntry.updateDataEntry(fields, "virtualQR", fields.get(FieldType.CUST_ID.getName()));

				}
				break;
			// Both VPA and QR already generated Send response from DB to Merchant
			case 3:
				logger.info("QR already in DB featching details from DB");
				saveResponse = getDataByAccountNo(fields);
				break;
			}

		} catch (Exception e) {
			logger.error("Exception in CashFree OR gentate vpa >>> ", e);
		}
		return saveResponse;
	}

	public Map<String, String> genrateCashFreeDynamicQRCode(Fields fields) {
		Map<String, String> saveResponse = new HashMap<String, String>();
		try {

			int reqid = Integer.parseInt(fields.get(FieldType.SLAB_ID.getName()));

			switch (reqid) {
			case 1:
				saveResponse = genrateVPARequst(fields);
				if (fields.get(FieldType.RESPONSE_CODE.getName()) != null
						&& fields.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase("000")) {
					cashFreeDBEntry.addNewEntryData(fields, "virtualVPA");
					saveResponse.clear();
					saveResponse = genrateCreateDynamicQRCodeRequest(fields);
					if (fields.get(FieldType.RESPONSE_CODE.getName()) != null
							&& fields.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase("000")) {
						cashFreeDBEntry.updateDataEntry(fields, "virtualQR", fields.get(FieldType.CUST_ID.getName()));
					}
				}
				break;
			case 2:
				saveResponse = genrateCreateDynamicQRCodeRequest(fields);
				if (fields.get(FieldType.RESPONSE_CODE.getName()) != null
						&& fields.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase("000")) {
					cashFreeDBEntry.updateDataEntry(fields, "virtualQR", fields.get(FieldType.CUST_ID.getName()));

				}
				break;
			case 3:
				saveResponse = getDataByAccountNo(fields);
				break;
			}

		} catch (Exception e) {
			logger.error("Exception in CashFree OR gentate vpa >>> ", e);
		}
		return saveResponse;
	}

	public void federalResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {
		try {
			Map<String, String[]> fieldMapObj = httpRequest.getParameterMap();
			Map<String, String> requestMap = new HashMap<String, String>();
			for (Entry<String, String[]> entry : fieldMapObj.entrySet()) {
				try {
					requestMap.put(entry.getKey(), entry.getValue()[0]);
					// key = key.replaceFirst("^\\s*", "");
				} catch (ClassCastException classCastException) {
					logger.error("Exception", classCastException);
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

			// Map.Entry<String, String> entry=null;
			for (Map.Entry<String, String> entry : requestMap.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
			}
			logger.info("response cashfree  = " + requestMap);
			// Fields fields = new Fields();
			BasicDBObject fields = new BasicDBObject();
			String newTxnId = TransactionManager.getNewTransactionId();
			String Id = TransactionManager.getNewTransactionId();

			String currentDate = DateCreater.formatDateForDb(new Date());
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date currDateFormat = format.parse(currentDate);
			LocalDate localDate = currDateFormat.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String DatesIndex = localDate.toString().replaceAll("-", "");

			logger.info(requestMap.get("amount"));
			fields.put("_id", Id);
			fields.put(FieldType.AMOUNT.getName(), requestMap.get("amount"));
			fields.put(FieldType.ORIG_TXN_ID.getName(), newTxnId);
			fields.put(FieldType.PG_REF_NUM.getName(), newTxnId);
			fields.put(FieldType.OID.getName(), newTxnId);
			fields.put(FieldType.TXN_ID.getName(), newTxnId);
			fields.put(FieldType.ORDER_ID.getName(), "LTZCF" + newTxnId);
			fields.put(FieldType.CUST_NAME.getName(), requestMap.get("remitterName"));
			fields.put(FieldType.ACQ_ID.getName(), requestMap.get("referenceId"));
			fields.put(FieldType.RRN.getName(), requestMap.get("utr"));
			fields.put(FieldType.CUST_EMAIL.getName(), requestMap.get("email"));
			fields.put(FieldType.PAYER_ADDRESS.getName(), requestMap.get("virtualVpaId"));
			fields.put(FieldType.PAYER_NAME.getName(), requestMap.get("remitterVpa"));
			fields.put(FieldType.PG_DATE_TIME.getName(), requestMap.get("paymentTime"));
			fields.put(FieldType.TXN_DATE.getName(), requestMap.get("paymentTime"));
			BasicDBObject finalquery = new BasicDBObject(FieldType.VPA.getName(), requestMap.get("virtualVpaId"));
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.CASHFREE_QRCODE.getValue()));
			Document document = (Document) coll.find(finalquery).first();
			if (document != null) {
				fields.put(FieldType.PAY_ID.getName(), document.getString(FieldType.PAY_ID.getName()));
			} else {
				fields.put(FieldType.PAY_ID.getName(), null);
			}

			fields.put(FieldType.DATE_INDEX.getName(), DatesIndex);
			fields.put(FieldType.CREATE_DATE.getName(), currentDate);
			fields.put(FieldType.UPDATE_DATE.getName(), currentDate);

			fields.put(FieldType.INTERNAL_REQUEST_FIELDS.getName(), requestMap.toString());
			fields.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			fields.put(FieldType.ACCT_ID.getName(), "0");
			fields.put(FieldType.PAYMENTS_REGION.getName(), "DOMESTIC");
			fields.put(FieldType.CARD_HOLDER_TYPE.getName(), "CONSUMER");
			fields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");
			fields.put(FieldType.CARD_MASK.getName(), null);
			fields.put(FieldType.TXNTYPE.getName(), "SALE");
			fields.put(FieldType.MOP_TYPE.getName(), "UP");
			fields.put(FieldType.CURRENCY_CODE.getName(), "356");
			fields.put(FieldType.PAYMENT_TYPE.getName(), "UP");
			fields.put(FieldType.ACQUIRER_TYPE.getName(), "CASHFREE");
			fields.put(FieldType.PRODUCT_DESC.getName(), null);
			fields.put(FieldType.AUTH_CODE.getName(), null);
			fields.put(FieldType.PG_RESP_CODE.getName(), null);
			fields.put(FieldType.INTERNAL_CUST_IP.getName(), null);
			fields.put(FieldType.INTERNAL_TXN_AUTHENTICATION.getName(), null);
			fields.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(), null);
			fields.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(), null);
			fields.put(FieldType.INTERNAL_USER_EMAIL.getName(), null);
			fields.put(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName(), null);
			fields.put(FieldType.IS_RECURRING.getName(), null);
			fields.put(FieldType.RECURRING_TRANSACTION_COUNT.getName(), null);
			fields.put(FieldType.RECURRING_TRANSACTION_INTERVAL.getName(), null);
			fields.put(FieldType.SURCHARGE_FLAG.getName(), null);
			fields.put(FieldType.REFUND_FLAG.getName(), null);
			fields.put(FieldType.REFUND_ORDER_ID.getName(), null);
			fields.put(FieldType.REQUEST_DATE.getName(), null);
			fields.put(FieldType.SRC_ACCOUNT_NO.getName(), null);
			fields.put(FieldType.BENE_ACCOUNT_NO.getName(), null);
			fields.put(FieldType.BENE_NAME.getName(), null);
			fields.put(FieldType.BENEFICIARY_CD.getName(), null);
			fields.put(FieldType.ACQUIRER_TDR_SC.getName(), "0");
			fields.put(FieldType.ACQUIRER_GST.getName(), "0");
			fields.put(FieldType.PG_GST.getName(), "0");
			fields.put(FieldType.PG_TDR_SC.getName(), "0");

			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection1 = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			Document cashfreeDoc = new Document(fields);
			collection1.insertOne(cashfreeDoc);

		} catch (Exception exception) {
			logger.error("Exception", exception);
			String path = httpRequest.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host") + "/pgui/jsp/error";
				httpResponse.sendRedirect(resultPath);
			}
			httpResponse.sendRedirect("error");
		}
	}

	public Map<String, String> getTxnKey(String payId) throws SystemException {

		logger.info("Pay Id for ");
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
		String mId = accountCurrency.getAdf1();
		String txnKey = accountCurrency.getAdf2();
		Map<String, String> merchantdetailsMap = new HashMap<String, String>();
		merchantdetailsMap.put(FieldType.MERCHANT_ID.getName(), mId);
		merchantdetailsMap.put(FieldType.TXN_KEY.getName(), txnKey);
		return merchantdetailsMap;
	}
}
