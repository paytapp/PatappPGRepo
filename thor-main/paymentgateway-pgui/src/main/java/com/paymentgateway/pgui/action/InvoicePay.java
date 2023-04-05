package com.paymentgateway.pgui.action;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opensymphony.xwork2.ModelDriven;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.user.InvoiceTransactionDao;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.PromotionalPaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pgui.action.beans.InvoiceHasher;

@Service
public class InvoicePay {

	@Autowired
	private InvoiceTransactionDao InvoiceTransactionDao;

	@Autowired
	private InvoiceHasher invoiceHasher;

	private Invoice invoice = new Invoice();
	private static Logger logger = LoggerFactory.getLogger(InvoicePay.class.getName());
	private String svalue;
	private String invoiceUrl;
	private String hash;
	private String totalamount;
	private String enablePay;
	private String currencyName;

	public Map<String, String> invoicePayHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {
		Map<String, String> invoiceMap = new HashMap<String, String>();
		try {

			invoice = InvoiceTransactionDao.findByInvoiceId(httpRequest.getParameter("svalue"));
			if (null == invoice) {
				String path = httpRequest.getContextPath();
				logger.info(path);
				if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
					String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host")
							+ "/pgui/jsp/error";
					httpResponse.sendRedirect(resultPath);
				}
				httpResponse.sendRedirect("error");
			}
			if (invoice.getInvoiceType().equals(PromotionalPaymentType.INVOICE_PAYMENT.getName())) {
				invoiceUrl = PropertiesManager.propertiesMap.get(CrmFieldConstants.INVOICE_URL.getValue())
						+ invoice.getInvoiceId();
			} else if (invoice.getInvoiceType().equals(PromotionalPaymentType.PROMOTIONAL_PAYMENT.getName())) {
				invoiceUrl = PropertiesManager.propertiesMap.get(CrmFieldConstants.INVOICE_PROMOTIONAL_URL.getValue())
						+ invoice.getInvoiceId();
			}

			hash = invoiceHasher.createInvoiceHash(invoice);
			totalamount = Amount.formatAmount(invoice.getTotalAmount(), invoice.getCurrencyCode());
			currencyName = Currency.getAlphabaticCode(invoice.getCurrencyCode());

			invoiceMap.put("logo", invoice.getLogo());
			invoiceMap.put("invoiceId", invoice.getInvoiceId());
			invoiceMap.put("name", invoice.getName());
			invoiceMap.put("city", invoice.getCity());
			invoiceMap.put("country", invoice.getCountry());
			invoiceMap.put("state", invoice.getState());
			invoiceMap.put("zip", invoice.getZip());
			invoiceMap.put("phone", invoice.getPhone());
			invoiceMap.put("email", invoice.getEmail());
			invoiceMap.put("address", invoice.getAddress());
			invoiceMap.put("productName", invoice.getProductName());
			invoiceMap.put("productDesc", invoice.getProductDesc());
			invoiceMap.put("quantity", invoice.getQuantity());
			invoiceMap.put("currencyName", currencyName);
			invoiceMap.put("expiresDay", invoice.getExpiresDay());
			invoiceMap.put("durationFrom", invoice.getDurationFrom());
			invoiceMap.put("durationTo", invoice.getDurationTo());
			invoiceMap.put("amount", invoice.getAmount());
			invoiceMap.put("serviceCharge", invoice.getServiceCharge());
			invoiceMap.put("totalAmount", totalamount);
			invoiceMap.put("UDF11", invoice.getUDF11());
			invoiceMap.put("UDF12", invoice.getUDF12());
			invoiceMap.put("UDF13", invoice.getUDF13());
			invoiceMap.put("UDF14", invoice.getUDF14());
			invoiceMap.put("UDF15", invoice.getUDF15());
			invoiceMap.put("UDF16", invoice.getUDF16());
			invoiceMap.put("UDF17", invoice.getUDF17());
			invoiceMap.put("UDF18", invoice.getUDF18());
			invoiceMap.put("payId", invoice.getPayId());
			invoiceMap.put("currencyCode", invoice.getCurrencyCode());
			invoiceMap.put("returnUrl", invoice.getReturnUrl());
			invoiceMap.put("subMerchantId", invoice.getSubMerchantId());
			invoiceMap.put("hash", hash);
			invoiceMap.put("mop", invoice.getMop());
			invoiceMap.put("invoiceUrl", invoiceUrl);
			invoiceMap.put("invoicePayUrl", PropertiesManager.propertiesMap.get("invoicePaymentLink"));
			invoiceMap.put("enablePay", getEnablePayNow());
		} catch (Exception exception) {
			MDC.put(Constants.CRM_LOG_USER_PREFIX.getValue(), invoice.getPayId() + svalue);
			logger.error("Exception", exception);
			String path = httpRequest.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host") + "/pgui/jsp/error";
				httpResponse.sendRedirect(resultPath);
			}
			httpResponse.sendRedirect("error");
		}
		return invoiceMap;
	}

//	public void validate() {
//
//		if (validator.validateBlankField(getSvalue())) {
//			addFieldError("svalue", ErrorType.INVALID_FIELD.getResponseMessage());
//		} else if (!validator.validateField(CrmFieldType.INVOICE_ID, getSvalue())) {
//			addFieldError("svalue", ErrorType.INVALID_FIELD.getResponseMessage());
//		}
//	}

	private String getEnablePayNow() throws ParseException {
		try {
			DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			String expiryDateString = invoice.getExpiresDay();

			String parseDate = LocalDateTime.parse(expiryDateString, formatter1).format(formatter);
			LocalDateTime dateTime = LocalDateTime.parse(parseDate, formatter);

			LocalDateTime todayDate = LocalDateTime.now();
			String dateToday = formatter.format(todayDate);
			LocalDateTime todayTime = LocalDateTime.parse(dateToday, formatter);

			if (dateTime.isBefore(todayTime)) {
				enablePay = "FALSE";
			} else {
				enablePay = "TRUE";
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return enablePay;
	}

}
