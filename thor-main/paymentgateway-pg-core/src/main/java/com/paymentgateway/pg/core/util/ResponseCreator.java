package com.paymentgateway.pg.core.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.SmsControllerServiceProvider;
import com.paymentgateway.commons.api.SmsSender;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.EPOSTransactionDao;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.AWSSESEmailService;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PDFCreator;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StaticDataProvider;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TxnType;
import com.paymentgateway.commons.util.threadpool.ThreadPoolProvider;
import com.paymentgateway.pg.core.security.TransactionConverterPaymentGateway;

/**
 * @author Rahul
 */

@Service
public class ResponseCreator extends Forwarder {

	private static Logger logger = LoggerFactory.getLogger(ResponseCreator.class.getName());
	private static final long serialVersionUID = 6021494405007391983L;
	private static final String prefix = "PAYMENT_GATEWAY_ACQUIRER_";
	@Autowired
	@Qualifier("responseProcessor")
	private Processor responseProcessor;

	@Autowired
	@Qualifier("propertiesManager")
	private PropertiesManager propertiesManager;

	@Autowired
	@Qualifier("paymentGatewayTransactionConverter")
	private TransactionConverterPaymentGateway converter;

	@Autowired
	private TransactionResponser transactionResponser;

	@Autowired
	private MerchantHostedUtils merchantHostedUtils;

	@Autowired
	private AWSSESEmailService awsSESEmailService;

	@Autowired
	private PDFCreator pdfCreator;

	@Autowired
	private SmsSender smsSender;

	@Autowired
	private EPOSTransactionDao eposDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private StaticDataProvider staticDataProvider;

	@Autowired
	private SmsControllerServiceProvider smsControllerServiceProvider;

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private UserSettingDao userSettingDao;

	public void create(Fields fields) {
		try {
			responseProcessor.preProcess(fields);
			responseProcessor.process(fields);
			responseProcessor.postProcess(fields);

		} catch (SystemException systemException) {
			logger.error("Exception", systemException);
			fields.clear();
		} catch (Exception exception) {
			logger.error("Exception", exception);
			fields.clear();
		}
	}

	public void ResponsePost(Fields fields, HttpServletResponse response) {
		String shopifyFlag = fields.get(FieldType.INTERNAL_SHOPIFY_YN.getName());
		String internalPaymentGatewayYN = fields.get(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());

		User user = null;
		if (StringUtils.isNotBlank(fields.get(FieldType.PARENT_PAY_ID.getName()))) {
			fields.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PARENT_PAY_ID.getName()));
			fields.remove(FieldType.PARENT_PAY_ID.getName());
		}
		if (propertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
				&& propertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
						.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {
			user = staticDataProvider.getUserData(fields.get(FieldType.PAY_ID.getName()));

		} else {
			user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		}

		UserSettingData userSettings = userSettingDao.fetchDataUsingPayId(user.getPayId());

		boolean merchantHostedFlag = userSettings.isMerchantHostedFlag();

		fields.put(FieldType.MERCHANT_NAME.getName(), user.getBusinessName());

		if (fields.get(FieldType.PAY_ID.getName())
				.equalsIgnoreCase(PropertiesManager.propertiesMap.get("MSEDCL_PAY_ID"))
				&& (!fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.INVALID.getName()))) {
			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));
			fields.put(FieldType.TOTAL_AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));
		}
		if (userSettings.isCustomTransactionStatus()) {
			if (!(fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())
					&& fields.get(FieldType.RESPONSE_CODE.getName()).equals(ErrorType.SUCCESS.getCode()))) {
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.TRANSACTION_FAILED.getResponseMessage());
			}

		}
		try {

			logger.info("checking for merchnat hosted flag = ", fields.get(FieldType.IS_MERCHANT_HOSTED.getName()));
			PrintWriter out = response.getWriter();

			if (null != shopifyFlag && shopifyFlag.equals("Y")) {
				// Map<String,String> responseMap = new
				// TransactionConverter().prepareResponse(fields); TODO
				// new ShopifyResponseCreater().ResponsePost(fields, responseMap);
				// TODO
			} else if (null != internalPaymentGatewayYN && internalPaymentGatewayYN.equals("Y")) {
				String key = propertiesManager.getSystemProperty(ConstantsPaymentGateway.PAYMENT_GATEWAY_KEY);
				String iv = propertiesManager.getSystemProperty(ConstantsPaymentGateway.PAYMENT_GATEWAY_IV);

				try {
					StringBuilder requestfields = converter.mapSaleFields(fields);
					requestfields = converter.mapChecksum(requestfields);
					logger.info("Plain text response to  Payment Gateway" + requestfields);
					String encryptedString = MerchantPaymentGatewayUtil.encryptPaymentGateway(requestfields.toString(),
							key, iv);
					String finalResponse = createCrisResponse(fields, encryptedString);
					logger.info("encrypted response to Payment Gateway " + finalResponse);
					out.write(finalResponse);
					out.flush();
					out.close();
				} catch (Exception e) {
					logger.error("Exception : ", e);
				}
			} else if (merchantHostedFlag && StringUtils.isNotBlank(fields.get(FieldType.STATUS.getName()))
					&& !fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.INVALID.getName())) {
				try {
					String pgrfNO = fields.get(FieldType.PG_REF_NUM.getName());
					fields.removeInternalFields();
					fields.removeSecureFields();
					fields.remove(FieldType.ORIG_TXN_ID.getName());
					fields.remove(FieldType.HASH.getName());
					fields.remove(FieldType.ORIG_TXN_ID.getName());
					transactionResponser.addHash(fields);
					fields.logAllFields("Plain Text response for merchant");
					String finalResponse = createMerchantHostedResponse(fields);
					logger.info(
							"encrypted response for merchant and  PR_RF_NUM :- " + pgrfNO + " :--:" + finalResponse);
					out.write(finalResponse);
					out.flush();
					out.close();

				} catch (Exception e) {
					logger.error("Error sending response to merchant : ", e);
				}
			} else {

				if (Boolean.valueOf(fields.get(FieldType.EPOS_MERCHANT.getName()))) {
					ExecutorService es = ThreadPoolProvider.getExecutorService();
					es.execute(new Runnable() {
						@Override
						public void run() {
							eposDao.updateEposCharges(fields);
							fields.removeInternalFields();
							fields.removeSecureFields();
							fields.remove(FieldType.ORIG_TXN_ID.getName());
							fields.remove(FieldType.HASH.getName());
						}
					});
					es.shutdown();
				}

				fields.removeInternalFields();
				fields.removeSecureFields();
				fields.remove(FieldType.ORIG_TXN_ID.getName());
				fields.remove(FieldType.HASH.getName());
				fields.put(FieldType.BOOKING_MERCHANT_FLAG.getName(), "N");
				String bookingPayId = PropertiesManager.propertiesMap.get("BOOKING_MERCHANT_PAYID");

				logger.info("Sending Transaction email and SMS to Customer & Merchant for Order Id= "
						+ fields.get(FieldType.ORDER_ID.getName()));

				transactionEmailerSms(fields);

				// If request is from a Sub-Merchant , change Pay Id in response to Sub Merchant
				// Pay Id
				if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
					fields.put(FieldType.PAY_ID.getName(), fields.get(FieldType.SUB_MERCHANT_ID.getName()));
				}
				if (StringUtils.isNotEmpty(fields.get(FieldType.CUST_NAME.getName()))) {
					fields.put(FieldType.CUST_NAME.getName(), fields.get(FieldType.CUST_NAME.getName()).trim());
				}
				transactionResponser.addHash(fields);
				fields.remove(FieldType.ORIG_TXN_ID.getName());
				String finalResponse = null;
				if (fields.contains("RETRY_URL")) {
					finalResponse = retryResponseCreator(fields);
				} else {
					finalResponse = createPgResponse(fields);
				}
				logger.info("final response sent " + finalResponse);
				out.write(finalResponse);
				out.flush();
				out.close();
			}
		} catch (Exception e) {
			logger.error("Error sending response to merchant : ", e);
		}
	}

	private void transactionEmailerSms(Fields fields) {
		logger.info("Inside transactionEmailerSms()");

		Runnable runnable = new Runnable() {
			public void run() {
				try {
					User user = null;

					if (propertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
							&& propertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
									.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {
						user = staticDataProvider.getUserData(fields.get(FieldType.PAY_ID.getName()));

					} else {
						user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
					}

					String status = fields.get(FieldType.STATUS.getName());
					String txnType = fields.get(FieldType.TXNTYPE.getName());

					if (status.equalsIgnoreCase(StatusType.CAPTURED.toString())
							&& txnType.equalsIgnoreCase(TxnType.SALE.getName())) {

						if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
							User superMerchant = null;

							if (propertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
									&& propertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
											.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {
								superMerchant = staticDataProvider.getUserData(user.getSuperMerchantId());

							} else {
								superMerchant = userDao.findPayId(user.getSuperMerchantId());

							}

							if (superMerchant.isTransactionEmailerFlag()) {
								logger.info("sending Transaction email to Merchant");
								awsSESEmailService.sendTransactionEmailToMerchant(fields, user);
							}
							if (superMerchant.isTransactionCustomerEmailFlag()) {
								logger.info("sending Transaction email to Customer");
								awsSESEmailService.sendTransactionEmailToCustomer(fields, user);
							}

							String custPhone = fields.get(FieldType.CUST_PHONE.getName());
							String merchPhone = user.getMobile();
							String totalAmount = Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),
									fields.get(FieldType.CURRENCY_CODE.getName()));
							String orderId = fields.get(FieldType.ORDER_ID.getName());

							if (superMerchant.isTransactionCustomerSMSFlag()) {
								logger.info("sending Transaction SMS to Customer");
								smsControllerServiceProvider.transactionSmsForCustomer(custPhone, totalAmount, orderId,
										user.getBusinessName());
							}
							if (superMerchant.isTransactionMerchantSMSFlag()) {
								logger.info("sending Transaction SMS to Merchant");
								smsControllerServiceProvider.transactionSmsForMerchant(merchPhone, totalAmount,
										orderId);
							}

						} else {

							if (user.isTransactionEmailerFlag()) {
								logger.info("sending Transaction email to Merchant");
								awsSESEmailService.sendTransactionEmailToMerchant(fields, user);
							}
							if (user.isTransactionCustomerEmailFlag()) {
								logger.info("sending Transaction email to Customer");
								awsSESEmailService.sendTransactionEmailToCustomer(fields, user);
							}

							String custPhone = fields.get(FieldType.CUST_PHONE.getName());
							String merchPhone = user.getMobile();
							String totalAmount = Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),
									fields.get(FieldType.CURRENCY_CODE.getName()));
							String orderId = fields.get(FieldType.ORDER_ID.getName());

							if (user.isTransactionCustomerSMSFlag()) {
								logger.info("sending Transaction SMS to Customer");
								smsControllerServiceProvider.transactionSmsForCustomer(custPhone, totalAmount, orderId,
										user.getBusinessName());
							}
							if (user.isTransactionMerchantSMSFlag()) {
								logger.info("sending Transaction SMS to Merchant");
								smsControllerServiceProvider.transactionSmsForMerchant(merchPhone, totalAmount,
										orderId);
							}

						}

					} else {

						if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
							User superMerchant = null;

							if (propertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
									&& propertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
											.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {
								superMerchant = staticDataProvider.getUserData(user.getSuperMerchantId());

							} else {
								superMerchant = userDao.findPayId(user.getSuperMerchantId());

							}

							if (superMerchant.isTransactionFailedMerchantEmailFlag()) {
								logger.info("sending Transaction Failed Email to Merchant");
								awsSESEmailService.sendTransactionFailedEmailToMerchant(fields, user);
							}
							if (superMerchant.isTransactionFailedCustomerEmailFlag()) {
								logger.info("sending Transaction Failed Email to Customer");
								awsSESEmailService.sendTransactionFailedEmailToCustomer(fields, user);
							}

							String custPhone = fields.get(FieldType.CUST_PHONE.getName());
							String merchPhone = user.getMobile();
							String totalAmount = Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),
									fields.get(FieldType.CURRENCY_CODE.getName()));
							String orderId = fields.get(FieldType.ORDER_ID.getName());

							if (superMerchant.isTransactionFailedCustomerSMSFlag()) {
								logger.info("sending Transaction Failed SMS to Customer");
								smsControllerServiceProvider.transactionFailedSmsForCustomer(custPhone, totalAmount,
										orderId, superMerchant.getBusinessName());
							}
							if (superMerchant.isTransactionFailedMerchantSMSFlag()) {
								logger.info("sending Transaction Failed SMS to Merchant");
								smsControllerServiceProvider.transactionFailedSmsForMerchant(merchPhone, totalAmount,
										orderId);
							}

						} else {

							if (user.isTransactionFailedMerchantEmailFlag()) {
								logger.info("sending Transaction Failed email to Merchant");
								awsSESEmailService.sendTransactionFailedEmailToMerchant(fields, user);
							}
							if (user.isTransactionFailedCustomerEmailFlag()) {
								logger.info("sending Transaction Failed email to Customer");
								awsSESEmailService.sendTransactionFailedEmailToCustomer(fields, user);
							}

							String custPhone = fields.get(FieldType.CUST_PHONE.getName());
							String merchPhone = user.getMobile();
							String totalAmount = Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),
									fields.get(FieldType.CURRENCY_CODE.getName()));
							String orderId = fields.get(FieldType.ORDER_ID.getName());

							if (user.isTransactionFailedCustomerSMSFlag()) {
								logger.info("sending Transaction Failed SMS to Customer");
								smsControllerServiceProvider.transactionFailedSmsForCustomer(custPhone, totalAmount,
										orderId, user.getBusinessName());
							}
							if (user.isTransactionFailedMerchantSMSFlag()) {
								logger.info("sending Transaction Failed SMS to Merchant");
								smsControllerServiceProvider.transactionFailedSmsForMerchant(merchPhone, totalAmount,
										orderId);
							}

						}

					}
				} catch (Exception e) {
					logger.error("exception in sending email to customer or merchant ", e);
				}
			}

		};

		propertiesManager.executorImpl(runnable);

	}

	// old method if revert required TODO...delete after testing
	public void ResponsePostOld(Fields fields, HttpServletResponse response) {
		// send sms
		// SmsSender.sendSMS(fields); // TODO
		String hexa = null;

		String shopifyFlag = fields.get(FieldType.INTERNAL_SHOPIFY_YN.getName());
		String internalPaymentGatewayYN = fields.get(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());

		if (null != shopifyFlag && shopifyFlag.equals("Y")) {
			// Map<String,String> responseMap = new
			// TransactionConverter().prepareResponse(fields); TODO
			// new ShopifyResponseCreater().ResponsePost(fields, responseMap);
			// TODO
		} else if (null != internalPaymentGatewayYN && internalPaymentGatewayYN.equals("Y")) {

			String key = propertiesManager.getSystemProperty(ConstantsPaymentGateway.PAYMENT_GATEWAY_KEY);
			String iv = propertiesManager.getSystemProperty(ConstantsPaymentGateway.PAYMENT_GATEWAY_IV);
			StringBuilder requestfields = converter.mapSaleFields(fields);
			try {
				requestfields = converter.mapChecksum(requestfields);

				String encrypt = MerchantPaymentGatewayUtil.encryptPaymentGateway(requestfields.toString(), key, iv);
				System.out.println("Encrypted String= " + encrypt);
				try {
					fields.removeInternalFields();
					fields.removeSecureFields();
					fields.remove(FieldType.HASH.getName());
					PrintWriter out = response.getWriter();
					StringBuilder httpRequest = new StringBuilder();
					httpRequest.append("<HTML>");
					httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
					httpRequest.append("<form name=\"form1\" action=\"");
					httpRequest.append(fields.get(FieldType.RETURN_URL.getName()));
					httpRequest.append("\" method=\"post\">");
					httpRequest.append("<input type=\"hidden\" name=\"");
					httpRequest.append("encdata");
					// httpRequest.append(FieldType.ENCDATA.getName());
					httpRequest.append("\" value=\"");
					httpRequest.append(encrypt);
					httpRequest.append("\">");
					httpRequest.append("<input type=\"hidden\" name=\"");
					httpRequest.append(FieldType.PAY_ID.getName());
					httpRequest.append("\" value=\"");
					httpRequest.append(fields.get(FieldType.PAY_ID.getName()));
					httpRequest.append("\">");
					httpRequest.append("<input type=\"hidden\" name=\"");
					httpRequest.append(FieldType.ORDER_ID.getName());
					httpRequest.append("\" value=\"");
					httpRequest.append(fields.get(FieldType.ORDER_ID.getName()));
					httpRequest.append("\">");
					httpRequest.append("</form>");
					httpRequest.append("<script language=\"JavaScript\">");
					httpRequest.append("function OnLoadEvent()");
					httpRequest.append("{document.form1.submit();}");
					httpRequest.append("</script>");
					httpRequest.append("</BODY>");
					httpRequest.append("</HTML>");
					logger.info("final request sent " + httpRequest);
					out.write(httpRequest.toString());
					out.flush();
					out.close();

				} catch (UnsupportedEncodingException e) {
					logger.error("Exception", e);
				} catch (IOException e) {
					logger.error("Exception", e);
				}
			} catch (SystemException e) {
				logger.error("Exception", e);
			}

		} else {
			try {
				fields.removeInternalFields();
				fields.removeSecureFields();
				fields.remove(FieldType.HASH.getName());
				transactionResponser.addHash(fields);
				PrintWriter out = response.getWriter();
				StringBuilder httpRequest = new StringBuilder();
				httpRequest.append("<HTML>");
				httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
				httpRequest.append("<form name=\"form1\" action=\"");
				httpRequest.append(fields.get(FieldType.RETURN_URL.getName()));
				httpRequest.append("\" method=\"post\">");
				for (String key : fields.keySet()) {
					httpRequest.append("<input type=\"hidden\" name=\"");
					httpRequest.append(key);
					httpRequest.append("\" value=\"");
					httpRequest.append(fields.get(key));
					httpRequest.append("\">");
				}
				httpRequest.append("</form>");
				httpRequest.append("<script language=\"JavaScript\">");
				httpRequest.append("function OnLoadEvent()");
				httpRequest.append("{document.form1.submit();}");
				httpRequest.append("</script>");
				httpRequest.append("</BODY>");
				httpRequest.append("</HTML>");
				logger.info("final request sent " + httpRequest);
				out.write(httpRequest.toString());
				out.flush();
				out.close();

			} catch (Exception exception) {
				logger.error("Exception", exception);
			}
		}
	}

	public String createPgResponse(Fields fields) {

		StringBuilder returnUrl = new StringBuilder();
		returnUrl.append(fields.get(FieldType.RETURN_URL.getName()));

		if (fields.get(FieldType.RETURN_URL.getName()).contains("sdkResponse")) {

			returnUrl.append("?");

			for (String key : fields.keySet()) {
				returnUrl.append(key);
				returnUrl.append("=");
				returnUrl.append(fields.get(key));
				returnUrl.append("&");
			}

			returnUrl.deleteCharAt(returnUrl.length() - 1);

		}
		StringBuilder httpRequest = new StringBuilder();
		httpRequest.append("<HTML>");
		httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
		httpRequest.append("<form name=\"form1\" action=\"");
		httpRequest.append(returnUrl.toString());
		httpRequest.append("\" method=\"post\">");
		for (String key : fields.keySet()) {
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append(key);
			httpRequest.append("\" value=\"");
			httpRequest.append(fields.get(key));
			httpRequest.append("\">");
		}
		httpRequest.append("</form>");
		httpRequest.append("<script language=\"JavaScript\">");
		httpRequest.append("function OnLoadEvent()");
		httpRequest.append("{document.form1.submit();}");
		httpRequest.append("</script>");
		httpRequest.append("</BODY>");
		httpRequest.append("</HTML>");
		return httpRequest.toString();
	}

	public String retryResponseCreator(Fields fields) {

		StringBuilder returnUrl = new StringBuilder();
		returnUrl.append(fields.get(FieldType.RETRY_URL.getName()));

		StringBuilder httpRequest = new StringBuilder();
		httpRequest.append("<HTML>");
		httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
		httpRequest.append("<form name=\"form1\" action=\"");
		httpRequest.append(returnUrl.toString());
		httpRequest.append("\" method=\"post\">");
		for (String key : fields.keySet()) {
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append(key);
			httpRequest.append("\" value=\"");
			httpRequest.append(fields.get(key));
			httpRequest.append("\">");
		}
		httpRequest.append("</form>");
		httpRequest.append("<script language=\"JavaScript\">");
		httpRequest.append("function OnLoadEvent()");
		httpRequest.append("{document.form1.submit();}");
		httpRequest.append("</script>");
		httpRequest.append("</BODY>");
		httpRequest.append("</HTML>");
		return httpRequest.toString();
	}

	public String createCrisResponse(Fields fields, String encryptedString) {
		StringBuilder httpRequest = new StringBuilder();
		httpRequest.append("<HTML>");
		httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
		httpRequest.append("<form name=\"form1\" action=\"");
		httpRequest.append(fields.get(FieldType.RETURN_URL.getName()));
		httpRequest.append("\" method=\"post\">");
		httpRequest.append("<input type=\"hidden\" name=\"");
		httpRequest.append("encdata");
		httpRequest.append("\" value=\"");
		httpRequest.append(encryptedString.toUpperCase());
		httpRequest.append("\">");
		httpRequest.append("<input type=\"hidden\" name=\"");
		httpRequest.append(FieldType.PAY_ID.getName());
		httpRequest.append("\" value=\"");
		httpRequest.append(fields.get(FieldType.PAY_ID.getName()));
		httpRequest.append("\">");
		httpRequest.append("<input type=\"hidden\" name=\"");
		httpRequest.append(FieldType.ORDER_ID.getName());
		httpRequest.append("\" value=\"");
		httpRequest.append(fields.get(FieldType.ORDER_ID.getName()));
		httpRequest.append("\">");
		httpRequest.append("</form>");
		httpRequest.append("<script language=\"JavaScript\">");
		httpRequest.append("function OnLoadEvent()");
		httpRequest.append("{document.form1.submit();}");
		httpRequest.append("</script>");
		httpRequest.append("</BODY>");
		httpRequest.append("</HTML>");

		return httpRequest.toString();
	}

	public String createCrisUpiResponse(Fields fields) {
		String encryptedString = "";
		String key = propertiesManager.getSystemProperty(ConstantsPaymentGateway.PAYMENT_GATEWAY_KEY);
		String iv = propertiesManager.getSystemProperty(ConstantsPaymentGateway.PAYMENT_GATEWAY_IV);

		try {
			StringBuilder requestfields = converter.mapSaleFields(fields);
			requestfields = converter.mapChecksum(requestfields);
			logger.info("Plain text response to  Payment Gateway for UPI" + requestfields.toString());
			encryptedString = MerchantPaymentGatewayUtil.encryptPaymentGateway(requestfields.toString(), key, iv);
			logger.info("encrypted response to Payment Gateway for UPI" + encryptedString.toUpperCase());
		} catch (Exception e) {
			logger.error("Exception", e);
		}

		return encryptedString.toUpperCase();
	}

	public String createMerchantHostedResponse(Fields fields) {

		String encData = null;
		try {
			encData = merchantHostedUtils.hostedEncryptMerchantResponse(fields);
		} catch (SystemException e) {
			logger.error("Exception", e);
		}
		StringBuilder httpRequest = new StringBuilder();
		httpRequest.append("<HTML>");
		httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
		httpRequest.append("<form name=\"form1\" action=\"");
		httpRequest.append(fields.get(FieldType.RETURN_URL.getName()));
		httpRequest.append("\" method=\"post\">");
		httpRequest.append("<input type=\"hidden\" name=\"");
		httpRequest.append(FieldType.ENCDATA.getName());
		httpRequest.append("\" value=\"");
		httpRequest.append(encData);
		httpRequest.append("\">");
		httpRequest.append("<input type=\"hidden\" name=\"");
		httpRequest.append(FieldType.PAY_ID.getName());
		httpRequest.append("\" value=\"");
		httpRequest.append(fields.get(FieldType.PAY_ID.getName()));
		httpRequest.append("\">");
		httpRequest.append("<input type=\"hidden\" name=\"");
		httpRequest.append(FieldType.ORDER_ID.getName());
		httpRequest.append("\" value=\"");
		httpRequest.append(fields.get(FieldType.ORDER_ID.getName()));
		httpRequest.append("\">");
		httpRequest.append("</form>");
		httpRequest.append("<script language=\"JavaScript\">");
		httpRequest.append("function OnLoadEvent()");
		httpRequest.append("{document.form1.submit();}");
		httpRequest.append("</script>");
		httpRequest.append("</BODY>");
		httpRequest.append("</HTML>");

		return httpRequest.toString();

	}

	public void InvalidUserResponsePost(Fields fields, HttpServletResponse response) {

		try {
			PrintWriter out = response.getWriter();

			fields.removeInternalFields();
			fields.removeSecureFields();
			fields.remove(FieldType.HASH.getName());
			fields.remove(FieldType.ORIG_TXN_ID.getName());
			String finalResponse = createPgResponse(fields);
			logger.info("final response sent " + finalResponse);
			out.write(finalResponse);
			out.flush();
			out.close();

		} catch (Exception e) {
			logger.error("Error sending response to merchant : ", e);
		}
	}
}
