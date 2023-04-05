package com.paymentgateway.crm.action;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
public class UpiAutoPayMandateAction extends AbstractSecureAction {

	@Autowired
	private UpiAutoPayDao upiAutoPayDao;

	@Autowired
	private IciciUpiAutoPayUtil iciciUpiAutoPayUtil;

	@Autowired
	private ResponseCreator responseCreator;

	private static final long serialVersionUID = -153775510726930173L;
	private static Logger logger = LoggerFactory.getLogger(UpiAutoPayMandateAction.class.getName());

	private String amount;
	private String totalAmount;

	private String startDate;
	private String endDate;
	private String tenure;
	private String custEmail;
	private String custMobile;
	private String payId;
	private String returnUrl;
	private String orderId;
	private String payerVPA;
	private String note;
	private String frequency;
	private String purpose;
	private String monthlyAmount;
	private String merchantLogo;

	private Map<String, String> aaData = new HashMap<String, String>();

	public String execute() {

		logger.info("inside UpiAutoPayMandateAction for mandate registration ");
		String decryptedResponse = null;
		HttpURLConnection con = null;
		try {

			PrintWriter out = ServletActionContext.getResponse().getWriter();
			LinkedHashMap<String, String> fieldsMap = new LinkedHashMap<String, String>();

			Date date = new Date();
			SimpleDateFormat yyyymmdd = new SimpleDateFormat(CrmFieldConstants.OUTPUT_DATE_FORMAT.getValue());
			SimpleDateFormat ddmmyyyy = new SimpleDateFormat(CrmFieldConstants.INPUT_DATE_FOR_MIS_REPORT.getValue());
			frequency = AutoPayFrequency.getAutoPayFrequencyCode(frequency);

			fieldsMap.put("merchantId", PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MID.getValue()));
			fieldsMap.put("subMerchantId", PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MID.getValue()));
			fieldsMap.put("terminalId",
					PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_TERMINAL_ID.getValue()));
			fieldsMap.put("merchantName",
					PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MERCHANT_NAME.getValue()));
			fieldsMap.put("subMerchantName",
					PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_SUB_MERCHANT_NAME.getValue()));
			fieldsMap.put("payerVa", payerVPA);
			fieldsMap.put("amount", amount);
			fieldsMap.put("note", note);
			fieldsMap.put("collectByDate", DateCreater.requestDateForAutoPay(new Date(date.getTime() + 1000 * 60 * 5)));
			fieldsMap.put("merchantTranId", TransactionManager.getNewTransactionId());
			fieldsMap.put("billNumber", tenure);
			fieldsMap.put("validityStartDate", ddmmyyyy.format(new Date()));

//			fieldsMap.put("validityEndDate", ddmmyyyy.format(yyyymmdd.parse(endDate)));
			fieldsMap.put("validityEndDate", DateCreater.replaceHyphenToSlashInDate(endDate));
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
			fieldsMap.put("purpose", purpose);

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

				fieldsMap.put(FieldType.ORDER_ID.getName(), orderId);
				fieldsMap.put(FieldType.DATE_FROM.getName(), startDate);
				fieldsMap.put(FieldType.DATE_TO.getName(), endDate);
				fieldsMap.put(FieldType.TENURE.getName(), tenure);
				fieldsMap.put(FieldType.CUST_EMAIL.getName(), custEmail);
				fieldsMap.put(FieldType.CUST_PHONE.getName(), custMobile);
				fieldsMap.put(FieldType.PAY_ID.getName(), payId);
				fieldsMap.put(FieldType.RETURN_URL.getName(), returnUrl);
				fieldsMap.put(FieldType.CREATE_DATE.getName(), DateCreater.formatDateForDb(date));
				fieldsMap.put(FieldType.TOTAL_AMOUNT.getName(), totalAmount);
				fieldsMap.put(FieldType.MONTHLY_AMOUNT.getName(), monthlyAmount);
				aaData.put("MERCHANT_LOGO", merchantLogo);
				upiAutoPayDao.insert(fieldsMap, json);

				aaData.put(FieldType.ORDER_ID.getName(), orderId);
				aaData.put(FieldType.DATE_FROM.getName(), startDate);
				aaData.put(FieldType.DATE_TO.getName(), endDate);
				aaData.put(FieldType.TENURE.getName(), tenure);
				aaData.put(FieldType.CUST_EMAIL.getName(), custEmail);
				aaData.put(FieldType.CUST_PHONE.getName(), custMobile);
				aaData.put(FieldType.PAY_ID.getName(), payId);
				aaData.put(FieldType.RETURN_URL.getName(), returnUrl);
				aaData.put(FieldType.CREATE_DATE.getName(), DateCreater.formatDateForDb(date));
				aaData.put(FieldType.AMOUNT.getName(), amount);
				aaData.put(FieldType.TOTAL_AMOUNT.getName(), totalAmount);
				aaData.put(FieldType.MONTHLY_AMOUNT.getName(), monthlyAmount);
				aaData.put(FieldType.PAYER_ADDRESS.getName(), payerVPA);
				aaData.put(FieldType.PAYMENT_TYPE.getName(), PaymentType.UPI.getName());
				if (StringUtils.isNotBlank(returnUrl)) {
					aaData.put(FieldType.RETURN_URL.getName(), returnUrl);
				} else {
					aaData.put(FieldType.RETURN_URL.getName(), "");
				}

				if (json.get("message").toString().equalsIgnoreCase("Transaction Initiated")) {
					aaData.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
				} else {
					aaData.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				}
				if (StringUtils.isNotBlank(returnUrl)) {
					String finalResponse = responseCreator.createPgResponse(new Fields(aaData));
					logger.info("final response sent " + finalResponse);
					out.write(finalResponse);
					out.flush();
					out.close();
				}

				return SUCCESS;
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

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getTenure() {
		return tenure;
	}

	public void setTenure(String tenure) {
		this.tenure = tenure;
	}

	public String getCustEmail() {
		return custEmail;
	}

	public void setCustEmail(String custEmail) {
		this.custEmail = custEmail;
	}

	public String getCustMobile() {
		return custMobile;
	}

	public void setCustMobile(String custMobile) {
		this.custMobile = custMobile;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getPayerVPA() {
		return payerVPA;
	}

	public void setPayerVPA(String payerVPA) {
		this.payerVPA = payerVPA;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}

	public Map<String, String> getAaData() {
		return aaData;
	}

	public void setAaData(Map<String, String> aaData) {
		this.aaData = aaData;
	}

	public String getMonthlyAmount() {
		return monthlyAmount;
	}

	public void setMonthlyAmount(String monthlyAmount) {
		this.monthlyAmount = monthlyAmount;
	}

	public String getMerchantLogo() {
		return merchantLogo;
	}

	public void setMerchantLogo(String merchantLogo) {
		this.merchantLogo = merchantLogo;
	}
}
