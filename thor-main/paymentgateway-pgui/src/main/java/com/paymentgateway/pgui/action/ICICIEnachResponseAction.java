package com.paymentgateway.pgui.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AWSSESEmailService;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmEmailer;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.EnachDCIssuerType;
import com.paymentgateway.commons.util.EnachNBIssuerType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.Frequency;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.pg.core.util.ResponseCreator;

/**
 * @author Rajit
 */
@Service
public class ICICIEnachResponseAction {

	private static Logger logger = LoggerFactory.getLogger(ICICIEnachResponseAction.class);

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private ResponseCreator responseCreator;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private UserDao userDao;

	@Autowired
	private Fields fields;

	@Autowired
	private AWSSESEmailService awsSESEmailService;

	private String message;
	private String merchantReturnUrl;

	// for user cancel button

	private String paymentMode;
	private String accountNumber;
	private String accountType;
	private String ifscCode;
	private String accountHolderName;
	private String frequency;
	private String payId;
	private String amount;
	private String maxAmount;
	private String totalAmount;
	private String debitStartDate;
	private String debitEndDate;
	private String cardNumber;
	private String bankCode;
	private String tenure;
	private String consumerMobileNo;
	private String consumerEmailId;
	private String merchantLogo;
	private String consumerId;

	Map<String, String> aaData = new HashMap<String, String>();

	@SuppressWarnings("static-access")
	public Map<String, String> iciciEnachResponseHandler(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		logger.info("Inside ICICIEnachResponseAction, execute() funtion ");
		try {
//			PrintWriter out = response.getWriter();
			String[] status = { "responseCode", "response", "responseMessage", "TXN_ID" };
			Map<String, String[]> fieldMapObj = request.getParameterMap();
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(fieldMapObj);
			logger.info("Response received from icici ENach on callback : " + json);

			if (StringUtils.isNotBlank(message) && message.equalsIgnoreCase("cancel")) {
				String comAmt = "0";
				aaData.put(FieldType.STATUS.getName(), StatusType.CANCELLED.getName());
				aaData.put(FieldType.RESPONSE_MESSAGE.getName(), "Cancelled By User");

				String txnId = TransactionManager.getNewTransactionId();
				User user = userDao.findPayId(payId);

				// for reseller sub merchant
				if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())
						&& StringUtils.isNotBlank(user.getResellerId())) {
					aaData.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
					aaData.put(FieldType.SUB_MERCHANT_ID.getName(), payId);
					aaData.put(FieldType.RESELLER_ID.getName(), user.getResellerId());

				} else if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
					// for Sub Merchant

					// super merchantId
					aaData.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
					// sub MerchantId
					aaData.put(FieldType.SUB_MERCHANT_ID.getName(), payId);

				} else if (StringUtils.isNotBlank(user.getResellerId())) {
					// Reseller Merchant
					aaData.put(FieldType.RESELLER_ID.getName(), user.getResellerId());
					aaData.put(FieldType.PAY_ID.getName(), payId);
					aaData.put(FieldType.SUB_MERCHANT_ID.getName(), "");
				} else {
					// super merchantId
					aaData.put(FieldType.PAY_ID.getName(), payId);
					// sub MerchantId
					aaData.put(FieldType.SUB_MERCHANT_ID.getName(), "");
				}
				List<String> debitDateList = fieldsDao.getDueDateList(debitStartDate, "", tenure,
						aaData.get(FieldType.PAY_ID.getName()), aaData.get(FieldType.SUB_MERCHANT_ID.getName()),
						"eNach");
				StringBuilder debitDateBuilder = new StringBuilder();

				for (String date : debitDateList) {
					debitDateBuilder.append(date).append(",");
				}
				debitDateBuilder.deleteCharAt(debitDateBuilder.length() - 1);
				aaData.put("DEBIT_START_DATE", debitStartDate);
				aaData.put("END_DATE", debitEndDate);
				aaData.put("DEBIT_DATE_LIST", debitDateBuilder.toString());
				aaData.put(CrmFieldType.CURRENCY.getName(), "INR");

				// Total Transaction Amount that are debited from customer account
				aaData.put(Constants.AMOUNT.getValue(), amount);
				// max amount field that are debited from customer account
				aaData.put("TRANSACTION_AMOUNT", maxAmount);
				aaData.put(FieldType.MAX_AMOUNT.getName(), totalAmount);
				aaData.put(FieldType.TOTAL_AMOUNT.getName(), totalAmount);

				if (StringUtils.isNotBlank(paymentMode)) {
					aaData.put("PAYMENT_MODE", paymentMode);
				} else {
					aaData.put("PAYMENT_MODE", Constants.NA.getValue());
				}

				aaData.put("ACCOUNT_NUMBER", accountNumber);
				aaData.put(CrmFieldType.IFSC_CODE.getName(), ifscCode);
				aaData.put(FieldType.ACCOUNT_TYPE.getName(), accountType);
				aaData.put("COM_AMT", comAmt);
				aaData.put(FieldType.ACCOUNT_HOLDER_NAME.getName(), accountHolderName);
				aaData.put("CONSUMER_MOBILE_NO", consumerMobileNo);
				aaData.put("CONSUMER_EMAIL_ID", consumerEmailId);
				aaData.put(FieldType.FREQUENCY.getName(), frequency);
				aaData.put("CONSUMER_ID", consumerId);
				aaData.put("CARD_NUMBER", cardNumber);
				aaData.put("BANK_CODE", bankCode);
				aaData.put(FieldType.TENURE.getName(), tenure);

				if (StringUtils.isNotBlank(paymentMode)) {
					if (paymentMode.equalsIgnoreCase("netBanking")) {
						aaData.put(FieldType.BANK_NAME.getName(), EnachNBIssuerType.getIssuerName(bankCode));
					} else {
						aaData.put(FieldType.BANK_NAME.getName(), EnachDCIssuerType.getIssuerName(bankCode));
					}
				} else {
					aaData.put(FieldType.BANK_NAME.getName(), Constants.NA.getValue());
				}

				aaData.put("MERCHANT_LOGO", merchantLogo);
				aaData.put("PAYMENT_GATEWAY_LOGO",
						propertiesManager.propertiesMap.get(Constants.ICICI_ENACH_PAYMENT_GATEWAY_LOGO.getValue()));
				aaData.put(FieldType.RETURN_URL.getName(), merchantReturnUrl);
				aaData.put("MERCHANT_RETURN_URL", merchantReturnUrl);
				aaData.put(FieldType.TXNTYPE.getName(), "Registration");
				aaData.put(FieldType.TXN_ID.getName(), txnId);
				aaData.put(FieldType.PG_REF_NUM.getName(), txnId);
				aaData.put(FieldType.ORIG_TXN_ID.getName(), txnId);
				Fields fields = new Fields(aaData);
				fieldsDao.insertEnachRegistrationDetail(fields);

				aaData.put(FieldType.RETURN_URL.getName(), merchantReturnUrl);
//				if (StringUtils.isNotBlank(merchantReturnUrl)) {
//					String finalResponse = responseCreator.createPgResponse(fields);
//					out.write(finalResponse);
//					out.flush();
//					out.close();
//				}
				/* logger.info("final response sent " + finalResponse); */

			} else if (StringUtils.isNotBlank(message) && message.equalsIgnoreCase("hashFail")) {

				aaData.put(FieldType.STATUS.getName(), StatusType.CANCELLED.getName());
				aaData.put(FieldType.RESPONSE_MESSAGE.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				aaData.put(FieldType.RETURN_URL.getName(), merchantReturnUrl);
				Fields fields = new Fields(aaData);

//				if (StringUtils.isNotBlank(merchantReturnUrl)) {
//					String finalResponse = responseCreator.createPgResponse(fields);
//					logger.info("final response sent " + finalResponse);
//					out.write(finalResponse);
//					out.flush();
//					out.close();
//				}

			} else if (StringUtils.isNotBlank(message) && message.equalsIgnoreCase("duplicateOrderId")) {

				aaData.put(FieldType.STATUS.getName(), StatusType.CANCELLED.getName());
				aaData.put(FieldType.RESPONSE_MESSAGE.getName(), "Duplicate Request");
				aaData.put(FieldType.RETURN_URL.getName(), merchantReturnUrl);
				Fields fields = new Fields(aaData);

//				if (StringUtils.isNotBlank(merchantReturnUrl)) {
//					String finalResponse = responseCreator.createPgResponse(fields);
//					logger.info("final response sent " + finalResponse);
//					out.write(finalResponse);
//					out.flush();
//					out.close();
//				}

			} else if (StringUtils.isNotBlank(message) && message.equalsIgnoreCase("invalidRequestID")) {
				aaData.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Request ID");
				aaData.put(FieldType.RETURN_URL.getName(), merchantReturnUrl);
				Fields fields = new Fields(aaData);

//				if (StringUtils.isNotBlank(merchantReturnUrl)) {
//					String finalResponse = responseCreator.createPgResponse(fields);
//					logger.info("final response sent " + finalResponse);
//					out.write(finalResponse);
//					out.flush();
//					out.close();
//				}

			} else if (StringUtils.isNotBlank(message) && message.equalsIgnoreCase("invalidEndDate")) {

				aaData.put(FieldType.STATUS.getName(), StatusType.CANCELLED.getName());
				aaData.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid End Date");
				aaData.put(FieldType.RETURN_URL.getName(), merchantReturnUrl);
				Fields fields = new Fields(aaData);

//				if (StringUtils.isNotBlank(merchantReturnUrl)) {
//					String finalResponse = responseCreator.createPgResponse(fields);
//					logger.info("final response sent " + finalResponse);
//					out.write(finalResponse);
//					out.flush();
//					out.close();
//				}

			} else if (StringUtils.isNotBlank(message) && message.equalsIgnoreCase("invalidStartDate")) {
				aaData.put(FieldType.STATUS.getName(), StatusType.CANCELLED.getName());
				aaData.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Start Date");
				aaData.put(FieldType.RETURN_URL.getName(), merchantReturnUrl);
				Fields fields = new Fields(aaData);

//				if (StringUtils.isNotBlank(merchantReturnUrl)) {
//					String finalResponse = responseCreator.createPgResponse(fields);
//					logger.info("final response sent " + finalResponse);
//					out.write(finalResponse);
//					out.flush();
//					out.close();
//				}

			} else if (StringUtils.isNotBlank(message) && message.equalsIgnoreCase("invalidAmount")) {
				aaData.put(FieldType.STATUS.getName(), StatusType.CANCELLED.getName());
				aaData.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Amount");
				aaData.put(FieldType.RETURN_URL.getName(), merchantReturnUrl);
				Fields fields = new Fields(aaData);

//				if (StringUtils.isNotBlank(merchantReturnUrl)) {
//					String finalResponse = responseCreator.createPgResponse(fields);
//					logger.info("final response sent " + finalResponse);
//					out.write(finalResponse);
//					out.flush();
//					out.close();
//				}

			} else if (StringUtils.isNotBlank(message) && message.equalsIgnoreCase("invalidMonthlyAmount")) {
				aaData.put(FieldType.STATUS.getName(), StatusType.CANCELLED.getName());
				aaData.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Monthly Amount");
				aaData.put(FieldType.RETURN_URL.getName(), merchantReturnUrl);
				Fields fields = new Fields(aaData);

//				if (StringUtils.isNotBlank(merchantReturnUrl)) {
//					String finalResponse = responseCreator.createPgResponse(fields);
//					logger.info("final response sent " + finalResponse);
//					out.write(finalResponse);
//					out.flush();
//					out.close();
//				}

			} else if (StringUtils.isNotBlank(message) && message.equalsIgnoreCase("invalidFrequency")) {
				aaData.put(FieldType.STATUS.getName(), StatusType.CANCELLED.getName());
				aaData.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Frequency");
				aaData.put(FieldType.RETURN_URL.getName(), merchantReturnUrl);
				Fields fields = new Fields(aaData);

//				if (StringUtils.isNotBlank(merchantReturnUrl)) {
//					String finalResponse = responseCreator.createPgResponse(fields);
//					logger.info("final response sent " + finalResponse);
//					out.write(finalResponse);
//					out.flush();
//					out.close();
//				}

			} else if (StringUtils.isNotBlank(message) && message.equalsIgnoreCase("invalidTenure")) {
				aaData.put(FieldType.STATUS.getName(), StatusType.CANCELLED.getName());
				aaData.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Tenure");
				aaData.put(FieldType.RETURN_URL.getName(), merchantReturnUrl);
				Fields fields = new Fields(aaData);

//				if (StringUtils.isNotBlank(merchantReturnUrl)) {
//					String finalResponse = responseCreator.createPgResponse(fields);
//					logger.info("final response sent " + finalResponse);
//					out.write(finalResponse);
//					out.flush();
//					out.close();
//				}

			} else if (StringUtils.isNotBlank(message) && message.equalsIgnoreCase("invalidMerchantID")) {
				aaData.put(FieldType.STATUS.getName(), StatusType.CANCELLED.getName());
				aaData.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Merchant ID");
				aaData.put(FieldType.RETURN_URL.getName(), merchantReturnUrl);
				Fields fields = new Fields(aaData);

//				if (StringUtils.isNotBlank(merchantReturnUrl)) {
//					String finalResponse = responseCreator.createPgResponse(fields);
//					logger.info("final response sent " + finalResponse);
//					out.write(finalResponse);
//					out.flush();
//					out.close();
//				}

			} else if (StringUtils.isNotBlank(message) && message.equalsIgnoreCase("invalidCustomerMobile")) {
				aaData.put(FieldType.STATUS.getName(), StatusType.CANCELLED.getName());
				aaData.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Customer Mobile No");
				aaData.put(FieldType.RETURN_URL.getName(), merchantReturnUrl);
				Fields fields = new Fields(aaData);

//				if (StringUtils.isNotBlank(merchantReturnUrl)) {
//					String finalResponse = responseCreator.createPgResponse(fields);
//					logger.info("final response sent " + finalResponse);
//					out.write(finalResponse);
//					out.flush();
//					out.close();
//				}

			} else if (StringUtils.isNotBlank(message) && message.equalsIgnoreCase("invalidCustomerEmail")) {
				aaData.put(FieldType.STATUS.getName(), StatusType.CANCELLED.getName());
				aaData.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Customer Email Id");
				aaData.put(FieldType.RETURN_URL.getName(), merchantReturnUrl);
				Fields fields = new Fields(aaData);

//				if (StringUtils.isNotBlank(merchantReturnUrl)) {
//					String finalResponse = responseCreator.createPgResponse(fields);
//					logger.info("final response sent " + finalResponse);
//					out.write(finalResponse);
//					out.flush();
//					out.close();
//				}

			} else {

				String[] stringArr = json.split("\\[");
				String[] quotesSeprateString = stringArr[1].toString().split("\"");
				String[] curlySeparatedString = quotesSeprateString[1].split("\\{");
				String umrnString = curlySeparatedString[2];
				String[] fieldSeparator = umrnString.split("~");
				String[] URMNumberArr = fieldSeparator[0].split(":");
				String[] finalString = quotesSeprateString[1].split("\\|");
				String mandateRegistrationId = finalString[finalString.length - 3];

				for (int i = 0; i < status.length; i++) {
					aaData.put(status[i], finalString[i]);
				}
				aaData.put("MANDATE_REGISTRATION_ID", mandateRegistrationId);
				aaData.put(URMNumberArr[0], URMNumberArr[1]);
				logger.info(aaData.toString());
				fieldsDao.updateENachRegistrationDetailByResponse(aaData, aaData.get(FieldType.TXN_ID.getName()));

				// insert document according to tenure
				if (aaData.get("response").equalsIgnoreCase("success")) {
					fieldsDao.insertTransactionPendingDocEnach(aaData.get(FieldType.TXN_ID.getName()));
				}

				HashMap<String, String> registrationDetail = fieldsDao
						.getEnachMandateDetailsByTxnId(aaData.get(FieldType.TXN_ID.getName()));

				aaData.put(FieldType.PAYMENT_TYPE.getName(), registrationDetail.get("paymentMode"));
				aaData.put(FieldType.ACCOUNT_TYPE.getName(), registrationDetail.get("accountType"));
				aaData.put(FieldType.ACCOUNT_NO.toString(), fields.fieldMask(registrationDetail.get("accountNumber")));
				aaData.put(FieldType.ACCOUNT_HOLDER_NAME.getName(), registrationDetail.get("accountHolderName"));
				aaData.put(FieldType.IFSC_CODE.getName(), fields.fieldMask(registrationDetail.get("ifscCode")));

				if (registrationDetail.containsKey("LOGO")) {
					aaData.put("LOGO", registrationDetail.get("LOGO"));
				}

				aaData.put(FieldType.ORDER_ID.getName(), registrationDetail.get(FieldType.ORDER_ID.getName()));
				aaData.put(FieldType.PAY_ID.getName(), registrationDetail.get("payId"));
				aaData.put(FieldType.UMRN_NUMBER.getName(), registrationDetail.get("umrnNumber"));
				aaData.put(FieldType.BANK_NAME.getName(), registrationDetail.get("bankName"));
				aaData.put(FieldType.CUST_PHONE.getName(), fields.fieldMask(registrationDetail.get("mobileNumber")));
				aaData.put(FieldType.CUST_EMAIL.getName(), fields.maskEmail(registrationDetail.get("emailId")));
				aaData.put(FieldType.AMOUNT.getName(), registrationDetail.get("amount"));
				aaData.put(FieldType.MAX_AMOUNT.getName(), registrationDetail.get("maxAmount"));
				aaData.put(FieldType.TOTAL_AMOUNT.getName(), registrationDetail.get("totalAmount"));
				aaData.put(FieldType.FREQUENCY.getName(),
						Frequency.getFrequencyName(registrationDetail.get("frequency")));
				aaData.put(FieldType.TENURE.getName(), registrationDetail.get("tenure"));

				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				Date date = sdf.parse(registrationDetail.get("startDate"));
				sdf = new SimpleDateFormat("yyyy-MM-dd");

				List<String> debitDateList = fieldsDao.getDueDateList(sdf.format(date), "",
						registrationDetail.get("tenure"), aaData.get(FieldType.PAY_ID.getName()), "", "eNach");
				StringBuilder debitDateBuilder = new StringBuilder();
				for (String debitDate : debitDateList) {
					debitDateBuilder.append(debitDate).append(",");
				}
				debitDateBuilder.deleteCharAt(debitDateBuilder.length() - 1);
				aaData.put("DEBIT_DATE_LIST", debitDateBuilder.toString());
				aaData.put(FieldType.DATEFROM.getName(), registrationDetail.get("startDate"));
				aaData.put(FieldType.DATETO.getName(), registrationDetail.get("endDate"));
				aaData.put(FieldType.RETURN_URL.getName(), registrationDetail.get("merchantReturnUrl"));
				aaData.put(FieldType.MERCHANT_NAME.getName(), registrationDetail.get("merchantName"));
				aaData.put(FieldType.MERCHANT_EMAIL.getName(), registrationDetail.get("merchantEmail"));
				aaData.put(FieldType.RESPONSE_CODE.getName(), registrationDetail.get("responseCode"));
				aaData.put(FieldType.RESPONSE_MESSAGE.getName(), registrationDetail.get("responseMessage"));
				aaData.put(FieldType.TXN_DATE.getName(), registrationDetail.get("updatedDate"));
				aaData.put(FieldType.STATUS.getName(), registrationDetail.get("status"));

				String merchantResponseHash = Hasher.getHash(new Fields(aaData));
				aaData.put(FieldType.HASH.getName(), merchantResponseHash);

				awsSESEmailService.sendEMandateEmailToUser(aaData);

//				if (StringUtils.isNotBlank(registrationDetail.get("merchantReturnUrl"))) {
//					String finalResponse = responseCreator.createPgResponse(new Fields(aaData));
//					logger.info("final response sent " + finalResponse);
//					out.write(finalResponse);
//					out.flush();
//					out.close();
//				}
			}
		} catch (Exception e) {
			logger.error("Exception in icici bank eNach callback response : ", e);
			String path = request.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = request.getScheme() + "://" + request.getHeader("Host") + "/pgui/jsp/error";
				response.sendRedirect(resultPath);
			}
			response.sendRedirect("error");
		}
		return aaData;
	}

}
