package com.paymentgateway.cashfree;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;
import com.paymentgateway.pg.core.util.CashfreeChecksumUtil;

@Service("cashfreeTransactionConverter")
public class TransactionConverter {

	private static Logger logger = LoggerFactory.getLogger(TransactionConverter.class.getName());

	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;

	@Autowired
	private CashfreeChecksumUtil cashfreeChecksumUtil;
	
	@Autowired
	private UserSettingDao userSettingDao;

	@SuppressWarnings("incomplete-switch")
	public String perpareRequest(Fields fields, Transaction transaction) throws SystemException {

		String request = null;

		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case AUTHORISE:
		case ENROLL:
			break;
		case REFUND:
			request = refundRequest(fields, transaction);
			break;
		case SALE:
			request = saleRequest(fields, transaction);
			break;
		case CAPTURE:
			break;
		case STATUS:
			request = statusEnquiryRequest(fields, transaction);
			break;
		}
		return request;

	}

	public String saleRequest(Fields fields, Transaction transaction) throws SystemException {

		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		String returnUrl = null;
		UserSettingData userSetting = userSettingDao.fetchDataUsingPayId((fields.get(FieldType.PAY_ID.getName())));
		
		if (userSetting.isAllowCustomHostedUrl()) {
			returnUrl = (userSetting.getCustomHostedUrl() + "/pgui/jsp/cashfreeResponse");

		} else {
			returnUrl = PropertiesManager.propertiesMap.get(Constants.SALE_RETURN_URL);
		}

		JsonObject jsonRequest = new JsonObject();
		prepareSaleRequest(jsonRequest, fields, returnUrl, amount);

		String signature = cashfreeChecksumUtil.getHash(jsonRequest, fields.get(FieldType.TXN_KEY.getName()));
		jsonRequest.addProperty(Constants.signature, signature);

		return jsonRequest.toString();

	}

	public String refundRequest(Fields fields, Transaction transaction) throws SystemException {

		try {

			String amount = acquirerTxnAmountProvider.amountProvider(fields);
			StringBuilder sb = new StringBuilder();

			sb.append("appId=" + fields.get(FieldType.MERCHANT_ID.getName()));
			sb.append("&");
			sb.append("secretKey=" + fields.get(FieldType.TXN_KEY.getName()));
			sb.append("&");
			sb.append("referenceId=" + fields.get(FieldType.ACQ_ID.getName()));
			sb.append("&");
			sb.append("refundAmount=" + amount);
			sb.append("&");
			sb.append("refundNote=" + "Refund");

			return sb.toString();

		}

		catch (Exception e) {
			logger.error("Exception in generating cashfree refund request", e);
			return null;
		}
	}

	public String statusEnquiryRequest(Fields fields, Transaction transaction) throws SystemException {

		StringBuilder sb = new StringBuilder();

		sb.append("appId=" + fields.get(FieldType.MERCHANT_ID.getName()));
		sb.append("&");
		sb.append("secretKey=" + fields.get(FieldType.TXN_KEY.getName()));
		sb.append("&");
		sb.append("orderId=" + fields.get(FieldType.PG_REF_NUM.getName()));

		return sb.toString();

	}

	public Transaction toTransaction(String response) {

		Transaction transaction = new Transaction();
		try {

			JSONObject resJson = new JSONObject(response);

			if (response.contains(Constants.orderId)) {
				transaction.setOrderId(resJson.get(Constants.orderId).toString());
			}

			if (response.contains(Constants.orderAmount)) {
				transaction.setOrderAmount(resJson.get(Constants.orderAmount).toString());
			}

			if (response.contains(Constants.referenceId)) {
				transaction.setReferenceId(resJson.get(Constants.referenceId).toString());
			}

			if (response.contains(Constants.txStatus)) {
				transaction.setTxStatus(resJson.get(Constants.txStatus).toString());
			}

			if (response.contains(Constants.paymentMode)) {
				transaction.setPaymentMode(resJson.get(Constants.paymentMode).toString());
			}

			if (response.contains(Constants.txMsg)) {
				transaction.setTxMsg(resJson.get(Constants.txMsg).toString());
			}
		}

		catch (Exception e) {
			logger.error("Exception", e);
		}

		return transaction;
	}

	public void logRequest(String requestMessage, Fields fields) {
		log("Request message to Cashfree  : Url= " + requestMessage, fields);
	}

	private void log(String message, Fields fields) {
		message = Pattern.compile("(<CardNumber>)([\\s\\S]*?)(</card>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<ExpiryDate>)([\\s\\S]*?)(</pan>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<CardSecurityCode>)([\\s\\S]*?)(</expmonth>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<TerminalId>)([\\s\\S]*?)(</expyear>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<PassCode>)([\\s\\S]*?)(</cvv2>)").matcher(message).replaceAll("$1$3");
		// message =
		// Pattern.compile("(<password>)([\\s\\S]*?)(</password>)").matcher(message).replaceAll("$1$3");
		MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
		logger.info(message);
	}

	private void prepareSaleRequest(JsonObject jsonRequest, Fields fields, String returnUrl, String amount) {

		jsonRequest.addProperty(Constants.appId, fields.get(FieldType.MERCHANT_ID.getName()));
		jsonRequest.addProperty(Constants.orderId, fields.get(FieldType.PG_REF_NUM.getName()));
		jsonRequest.addProperty(Constants.orderAmount, amount);
		jsonRequest.addProperty(Constants.orderCurrency,
				Currency.getAlphabaticCode(fields.get(FieldType.CURRENCY_CODE.getName())));

		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_NAME.getName()))) {
			jsonRequest.addProperty(Constants.customerName, fields.get(FieldType.CUST_NAME.getName()));
		} else {
			jsonRequest.addProperty(Constants.customerName, "NA");
		}

		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
			jsonRequest.addProperty(Constants.customerEmail, fields.get(FieldType.CUST_EMAIL.getName()));
		} else {
			jsonRequest.addProperty(Constants.customerEmail, "support.txn@PaymentGateway.com");
		}

		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_PHONE.getName()))) {
			jsonRequest.addProperty(Constants.customerPhone, fields.get(FieldType.CUST_PHONE.getName()));
		} else {
			jsonRequest.addProperty(Constants.customerPhone, PropertiesManager.propertiesMap.get("CashFreeCustMobile"));
		}

		jsonRequest.addProperty(Constants.returnUrl, returnUrl);

		if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())
				|| fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())) {

			logger.info("Cashfree Request before adding payment parameters  incase of cards " + jsonRequest.toString());

			String expDate = fields.get(FieldType.CARD_EXP_DT.getName());

			jsonRequest.addProperty(Constants.paymentOption, Constants.paymentOptionCard);
			jsonRequest.addProperty(Constants.card_number, fields.get(FieldType.CARD_NUMBER.getName()));
			jsonRequest.addProperty(Constants.card_holder, fields.get(FieldType.CARD_HOLDER_NAME.getName()));
			jsonRequest.addProperty(Constants.card_expiryMonth, expDate.substring(0, 2));
			jsonRequest.addProperty(Constants.card_expiryYear, expDate.substring(2, 6));
			jsonRequest.addProperty(Constants.card_cvv, fields.get(FieldType.CVV.getName()));

		} else if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.UPI.getCode())) {
			jsonRequest.addProperty(Constants.paymentOption, Constants.paymentOptionUPI);
			jsonRequest.addProperty(Constants.upi_vpa, fields.get(FieldType.PAYER_ADDRESS.getName()));

			logger.info("Cashfree Request for UPI " + jsonRequest.toString());

			fields.put(FieldType.CARD_MASK.getName(), fields.get(FieldType.PAYER_ADDRESS.getName()));

		} else if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.NET_BANKING.getCode())) {

			jsonRequest.addProperty(Constants.paymentOption, Constants.paymentOptionNB);
			jsonRequest.addProperty(Constants.paymentCode,
					CashfreeMopType.getBankCode(fields.get(FieldType.MOP_TYPE.getName())));

			logger.info("Cashfree Request for NB " + jsonRequest.toString());

		} else if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.WALLET.getCode())) {

			jsonRequest.addProperty(Constants.paymentOption, Constants.paymentOptionWallet);
			jsonRequest.addProperty(Constants.paymentCode,
					CashfreeMopType.getBankCode(fields.get(FieldType.MOP_TYPE.getName())));

			logger.info("Cashfree Request for Wallet " + jsonRequest.toString());

		}

	}

	public Transaction toTransactionStatus(String response) {

		Transaction transaction = new Transaction();
		try {

			JSONObject resJson = new JSONObject(response);

			if (response.contains(Constants.orderStatus)) {
				transaction.setOrderStatus(resJson.get(Constants.orderStatus).toString());
			}

			if (response.contains(Constants.orderAmount)) {
				transaction.setOrderAmount(resJson.get(Constants.orderAmount).toString());
			}

			if (response.contains(Constants.status)) {
				transaction.setStatus(resJson.get(Constants.status).toString());
			}

			if (response.contains(Constants.txStatus)) {
				transaction.setTxStatus(resJson.get(Constants.txStatus).toString());
			}

			if (response.contains(Constants.txMsg)) {
				transaction.setTxMsg(resJson.get(Constants.txMsg).toString());
			}

			if (response.contains(Constants.referenceId)) {
				transaction.setReferenceId(resJson.get(Constants.referenceId).toString());
			}
		}

		catch (Exception e) {
			logger.error("Exception", e);
		}

		return transaction;
	}

	public Transaction toTransactionRefund(String response) {

		Transaction transaction = new Transaction();
		try {

			JSONObject resJson = new JSONObject(response);

			if (response.contains(Constants.message)) {
				transaction.setMessage(resJson.get(Constants.message).toString());
			}

			if (response.contains(Constants.refundId)) {
				transaction.setRefundId(resJson.get(Constants.refundId).toString());
			}

			if (response.contains(Constants.status)) {
				transaction.setStatus(resJson.get(Constants.status).toString());
			}
		}

		catch (Exception e) {
			logger.error("Exception", e);
		}

		return transaction;
	}

	public String createOrderIdRequest(Fields fields) {

		
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

		JSONObject request = new JSONObject();

		request.put("order_id", fields.get(FieldType.PG_REF_NUM.getName()));
		request.put("order_amount", Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
				fields.get(FieldType.CURRENCY_CODE.getName())));
		request.put("order_currency", "INR");

		// customer Details
		JSONObject custDetails = new JSONObject();
		custDetails.put("customer_id", fields.get(FieldType.PG_REF_NUM.getName()));
		
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
			custDetails.put("customer_email", fields.get(FieldType.CUST_EMAIL.getName()));
		}
		else {
			custDetails.put("customer_email", PropertiesManager.propertiesMap.get("CASHFREE_UPI_EMAIL_ID"));
		}
		
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_PHONE.getName()))) {
			custDetails.put("customer_phone", fields.get(FieldType.CUST_PHONE.getName()));
		}
		else {
			custDetails.put("customer_phone", PropertiesManager.propertiesMap.get("CASHFREE_UPI_MOBILE"));
		}

		request.put("customer_details", custDetails);

		JSONObject orderMeta = new JSONObject();

		orderMeta.put("notify_url", PropertiesManager.propertiesMap.get("CASHFREEUpiReturnUrl"));
		orderMeta.put("payment_methods", "upi");

		request.put("order_meta", orderMeta);

		request.put("order_expiry_time", inputFormat.format(new Date()));
		request.put("order_note", "Payment Request");

		logger.info("createOrderIdRequest >> " + request.toString());

		return request.toString();

	}

	public String payOrderRequest(String orderToken, String upiId) {

		// HEADER -->> "x-api-version","2021-05-21", "Content-Type", "application/json"
		// REQUEST -->>
		// {"order_token":"iAuJefl0B8xJ3szj7tLl","payment_method":{"upi":{"channel":"collect","upi_id":"9716010015@ybl"}}}
		// RESPONSE -->>
		// {"payment_method":"upi","channel":"collect","action":"custom","data":{"url":null,"payload":null,"content_type":null,"method":null},"cf_payment_id":732184779}

		// for qrCode change channel = qrCode

		// URL -->> https://api.cashfree.com/pg/orders/pay

		JSONObject request = new JSONObject();

		request.put("order_token", orderToken);

		// payment Details
		JSONObject paymentMethod = new JSONObject();

		JSONObject upiOrder = new JSONObject();
		upiOrder.put("channel", "collect");
		upiOrder.put("upi_id", upiId);
		paymentMethod.put("upi", upiOrder);

		request.put("payment_method", paymentMethod);
		return request.toString();
	}

	public String orderDetails(Fields fields) {

		// HEADER -->> "Content-Type", "application/x-www-form-urlencoded"
		// REQUEST -->>
		// appId=1553470609250e46988da9e459743551&secretKey=8c7f5ca2680cc60c8e2d6d45549d251baf6b769d&orderId=1007720128155614
		// URL https://api.cashfree.com/api/v1/order/info/status

		// RESPONSE -->>
		// {"orderStatus":"PAID","orderAmount":"1.00","status":"OK","txStatus":"SUCCESS","txTime":"2022-01-28
		// 15:56:16","txMsg":"00::Transaction is
		// Successful","referenceId":"732224891","paymentMode":"UPI","orderCurrency":"INR","paymentDetails":{"payersVPA":"9716010015@ybl","utr":"202826793810"}}

		StringBuilder data = new StringBuilder();

		data.append("appId=");
		data.append(fields.get(FieldType.MERCHANT_ID.getName()));
		data.append("&secretKey=");
		data.append(fields.get(FieldType.TXN_KEY.getName()));
		data.append("&orderId=");
		data.append(fields.get(FieldType.PG_REF_NUM.getName()));

		logger.info("Status Enquiry for Order id " + data.toString());

		return data.toString();
	}
}
