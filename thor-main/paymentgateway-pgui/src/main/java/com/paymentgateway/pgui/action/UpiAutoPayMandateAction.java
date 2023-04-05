package com.paymentgateway.pgui.action;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.UpiAutoPayDao;
import com.paymentgateway.commons.util.AutoPayFrequency;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.pg.core.util.IciciUpiAutoPayUtil;
import com.paymentgateway.pg.core.util.ResponseCreator;

/**
 * @author Rajit
 **/
@Service
public class UpiAutoPayMandateAction {

	@Autowired
	private UpiAutoPayDao upiAutoPayDao;

	@Autowired
	private IciciUpiAutoPayUtil iciciUpiAutoPayUtil;

//	@Autowired
//	private ResponseCreator responseCreator;

	private static Logger logger = LoggerFactory.getLogger(UpiAutoPayMandateAction.class.getName());

	private String frequency;

	private Map<String, String> aaData = new HashMap<String, String>();

	public Map<String, String> upiAutoPayMandateHandler(Map<String, String> reqmap, HttpServletResponse res) {

		logger.info("inside UpiAutoPayMandateAction for mandate registration ");
		String decryptedResponse = null;
		HttpURLConnection con = null;
		try {

//			PrintWriter out = res.getWriter();
			LinkedHashMap<String, String> fieldsMap = new LinkedHashMap<String, String>();

			Date date = new Date();
			SimpleDateFormat yyyymmdd = new SimpleDateFormat(CrmFieldConstants.OUTPUT_DATE_FORMAT.getValue());
			SimpleDateFormat ddmmyyyy = new SimpleDateFormat(CrmFieldConstants.INPUT_DATE_FOR_MIS_REPORT.getValue());
			frequency = AutoPayFrequency.getAutoPayFrequencyCode(reqmap.get("frequency"));

			fieldsMap.put("merchantId", PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MID.getValue()));
			fieldsMap.put("subMerchantId", PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MID.getValue()));
			fieldsMap.put("terminalId",
					PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_TERMINAL_ID.getValue()));
			fieldsMap.put("merchantName",
					PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MERCHANT_NAME.getValue()));
			fieldsMap.put("subMerchantName",
					PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_SUB_MERCHANT_NAME.getValue()));
			fieldsMap.put("payerVa", reqmap.get("payerVPA"));
			fieldsMap.put("amount", reqmap.get("amount"));
			fieldsMap.put("note", reqmap.get("note"));
			fieldsMap.put("collectByDate", DateCreater.requestDateForAutoPay(new Date(date.getTime() + 1000 * 60 * 5)));
			fieldsMap.put("merchantTranId", TransactionManager.getNewTransactionId());
			fieldsMap.put("billNumber", reqmap.get("tenure"));
			fieldsMap.put("validityStartDate", ddmmyyyy.format(new Date()));

			fieldsMap.put("validityEndDate", ddmmyyyy.format(yyyymmdd.parse(reqmap.get("endDate"))));
			fieldsMap.put("amountLimit",
					PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_AMOUNT_LIMIT.getValue()));
			fieldsMap.put("remark", "Mandate Request");
			fieldsMap.put("requestType", "C");
			fieldsMap.put("frequency", frequency);
			fieldsMap.put("autoExecute", "N");

			if (frequency.equalsIgnoreCase("WK")) {
				fieldsMap.put("debitDay", "7");
			} else if (frequency.equalsIgnoreCase("BM")) {
				fieldsMap.put("debitDay", "15");
			} else if (frequency.equals("MT") || frequency.equals("QT") || frequency.equals("HY")
					|| frequency.equals("YR")) {
				fieldsMap.put("debitDay", "31");
			} else {
				fieldsMap.put("debitDay", CrmFieldConstants.NA.getValue());
			}

			if (frequency.equals("OT") || frequency.equals("DL") || frequency.equals("AS")) {
				fieldsMap.put("debitRule", CrmFieldConstants.NA.getValue());
			} else {
				fieldsMap.put("debitRule", "BF");
			}

			fieldsMap.put("revokable", Constants.Y_FLAG.getValue());
			fieldsMap.put("blockfund", Constants.N_FLAG.getValue());
			fieldsMap.put("purpose", reqmap.get("purpose"));

			String requestParam = fieldsMap.toString().replace("{", "{\"").replace("=", "\":").replace(", ", ", \"")
					.replace("\":", "\":\"").replace("}", "\"}").replace(", \"", "\", \"");

			logger.info("Upi AutoPay Mandate Request " + requestParam);
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MANDATE_URL.getValue());

			URL requestUrl = new URL(serviceUrl);
			con = (HttpURLConnection) requestUrl.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("apikey",
					PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_API_KEY.getValue()));

			// Encrypt The Request
			String encryptedRequest = iciciUpiAutoPayUtil.upiAutoPayEncryption(requestParam);
			logger.info("Encrypted Request is " + encryptedRequest);
			con.setDoOutput(true);
			con.setUseCaches(false);
			OutputStream os = con.getOutputStream();
			os.write(encryptedRequest.getBytes());
			os.flush();
			os.close();

			int responseCode = con.getResponseCode();
			logger.info("Upi AutoPay Mandate Response Code " + responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				logger.info("Encrypted Response recived is " + response.toString());
				decryptedResponse = iciciUpiAutoPayUtil.upiAutoPayDecryption(response.toString());
				logger.info("Decrypted Response of ICICI Upi AutoPay is " + decryptedResponse);
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(decryptedResponse);

				fieldsMap.put(FieldType.ORDER_ID.getName(), reqmap.get("orderId"));
				fieldsMap.put(FieldType.DATE_FROM.getName(), reqmap.get("startDate"));
				fieldsMap.put(FieldType.DATE_TO.getName(), reqmap.get("endDate"));
				fieldsMap.put(FieldType.TENURE.getName(), reqmap.get("tenure"));
				fieldsMap.put(FieldType.CUST_EMAIL.getName(), reqmap.get("custEmail"));
				fieldsMap.put(FieldType.CUST_PHONE.getName(), reqmap.get("custMobile"));
				fieldsMap.put(FieldType.PAY_ID.getName(), reqmap.get("payId"));
				fieldsMap.put(FieldType.RETURN_URL.getName(), reqmap.get("returnUrl"));
				fieldsMap.put(FieldType.CREATE_DATE.getName(), DateCreater.formatDateForDb(date));
				fieldsMap.put(FieldType.TOTAL_AMOUNT.getName(), reqmap.get("totalAmount"));
				fieldsMap.put(FieldType.MONTHLY_AMOUNT.getName(), reqmap.get("monthlyAmount"));
				aaData.put("MERCHANT_LOGO", reqmap.get("merchantLogo"));
				upiAutoPayDao.insert(fieldsMap, json);

				aaData.put(FieldType.ORDER_ID.getName(), reqmap.get("orderId"));
				aaData.put(FieldType.DATE_FROM.getName(), reqmap.get("startDate"));
				aaData.put(FieldType.DATE_TO.getName(), reqmap.get("endDate"));
				aaData.put(FieldType.TENURE.getName(), reqmap.get("tenure"));
				aaData.put(FieldType.CUST_EMAIL.getName(), reqmap.get("custEmail"));
				aaData.put(FieldType.CUST_PHONE.getName(), reqmap.get("custMobile"));
				aaData.put(FieldType.PAY_ID.getName(), reqmap.get("payId"));
				aaData.put(FieldType.RETURN_URL.getName(), reqmap.get("returnUrl"));
				aaData.put(FieldType.CREATE_DATE.getName(), DateCreater.formatDateForDb(date));
				aaData.put(FieldType.AMOUNT.getName(), reqmap.get("amount"));
				aaData.put(FieldType.TOTAL_AMOUNT.getName(), reqmap.get("totalAmount"));
				aaData.put(FieldType.MONTHLY_AMOUNT.getName(), reqmap.get("monthlyAmount"));
				aaData.put(FieldType.PAYER_ADDRESS.getName(), reqmap.get("payerVPA"));
				aaData.put(FieldType.PAYMENT_TYPE.getName(), PaymentType.UPI.getName());
				if (StringUtils.isNotBlank(reqmap.get("returnUrl"))) {
					aaData.put(FieldType.RETURN_URL.getName(), reqmap.get("returnUrl"));
				} else {
					aaData.put(FieldType.RETURN_URL.getName(), "");
				}

				if (json.get("message").toString().equalsIgnoreCase("Transaction Initiated")) {
					aaData.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
				} else {
					aaData.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				}
//				if (StringUtils.isNotBlank(reqmap.get("returnUrl"))) {
//					String finalResponse = responseCreator.createPgResponse(new Fields(aaData));
//					logger.info("final response sent " + finalResponse);
//					out.write(finalResponse);
//					out.flush();
//					out.close();
//				}

				return aaData;
			} else {
				logger.info("Error while connecting Response Code is ", con.getResponseCode());
				return null;
			}
		} catch (Exception ex) {
			logger.error("Exception in mandate upi autoPay connection ", ex);
			return null;
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
	}
}
