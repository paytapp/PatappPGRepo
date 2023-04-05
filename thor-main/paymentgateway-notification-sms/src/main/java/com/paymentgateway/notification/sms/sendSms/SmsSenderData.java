package com.paymentgateway.notification.sms.sendSms;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.notification.sms.smsCreater.UrlCreater;

/**
 * @author Neeraj Description - SMS API
 */
@Component
public class SmsSenderData {

	private  Logger logger = LoggerFactory.getLogger(SmsSenderData.class.getName());

	@Autowired
	@Qualifier("userDao")
	private UserDao userDao;

	@Autowired
	private UrlCreater urlCreater; 
	
	// Dynamic Message Format
	public void sendSMS(Map<String, String> fields) {
		try {
			User user = userDao.getUserClass(fields.get(FieldType.PAY_ID.getName()));
			if (user.isTransactionSmsFlag() == true) {
				// Bind on the basis of Merchant Contact Number
				String requestUrl = urlCreater.createURL(fields, user);

				URL url = new URL(requestUrl);
				HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();

				if (urlconnection.getResponseMessage().equals(Constants.CONTANT_OK.getValue())) {
					MDC.put(Constants.CRM_LOG_USER_PREFIX.getValue(), user.getEmailId());
					logger.info("Transaction sms sent for TXN_ID:" + fields.get(FieldType.TXN_ID.getName()));
				} else {
					MDC.put(Constants.CRM_LOG_USER_PREFIX.getValue(), user.getEmailId());
					logger.info("Error sending SMS for TXN_ID:" + fields.get(FieldType.TXN_ID.getName()));
				}
				urlconnection.disconnect();
			}

		} catch (Exception exception) {
			logger.error("Error sending SMS for TXN_ID:" + fields.get(FieldType.TXN_ID.getName()), exception);
		}
	}

	public void sendPromoSMS(Invoice invoice, String url) {
		try {
			// Bind on the basis of Merchant Contact Number
			String requestUrl = urlCreater.createPromoURL(invoice, url);
			URL msgUrl = new URL(requestUrl);
			HttpURLConnection urlconnection = (HttpURLConnection) msgUrl.openConnection();
			if (urlconnection.getResponseMessage().equals(Constants.CONTANT_OK.getValue())) {
				logger.info("Invoice sms sent:" + invoice.getInvoiceId());
			} else {

				logger.info("Error sending SMS for Invoice:" + invoice.getInvoiceId());
			}
			urlconnection.disconnect();
		} catch (Exception exception) {
			logger.error("Error sending SMS for Invoice:" + invoice.getInvoiceId());
		}
	}
}
