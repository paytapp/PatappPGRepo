package com.paymentgateway.notification.sms.smsCreater;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.SystemConstants;
import com.paymentgateway.notification.sms.constants.Constants;

@Component
public class UrlCreater {
	@Autowired
	private SmsMessageCreater smsMessageCreater;

	public static final String USER_NAME = "username=";
	public static final String SMS_PASSWORD = "&password=";
	public static final String SMS_TO = "&to=";
	public static final String SMS_FROM = "&from=";
	public static final String SMS_TEXT = "&udh=" + "&text=";
	public static final String SMS_DLRMASK = "&dlr-mask=19";
	public static final String SMS_DLRURL = "&dlr-url";
	private static PropertiesManager propertiesManager = new PropertiesManager();
	private static final String username = propertiesManager.getEmailProperty(Constants.USER_NAME);
	private static final String from = propertiesManager.getEmailProperty(Constants.SMS_SENDER);
	private static final String password = propertiesManager.getEmailProperty(Constants.SMS_PASSWORD);
	private static final String urlString = propertiesManager.getEmailProperty(Constants.SMS_API_URL);
	private static final String promousername = propertiesManager.getEmailProperty(Constants.PROMO_USER_NAME);
	private static final String promofrom = propertiesManager.getEmailProperty(Constants.PROMO_SMS_SENDER);
	private static final String promopassword = propertiesManager.getEmailProperty(Constants.PROMO_SMS_PASSWORD);

	public String createURL(Map<String, String> fields, User user) throws UnsupportedEncodingException {
		String message = smsMessageCreater.createSmsText(fields, user);
		String to = propertiesManager.getEmailProperty(Constants.PROMO_SMS_COUNTRY_CODE) + user.getMobile();

		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(urlString);
		requestUrl.append(USER_NAME);
		requestUrl.append(urlEncoder(username));
		requestUrl.append(SMS_PASSWORD);
		requestUrl.append(urlEncoder(password));
		requestUrl.append(SMS_TO);
		requestUrl.append(urlEncoder(to));
		requestUrl.append(SMS_FROM);
		requestUrl.append(urlEncoder(from));
		requestUrl.append(SMS_TEXT);
		requestUrl.append(urlEncoder(message));
		requestUrl.append(SMS_DLRMASK);
		requestUrl.append(SMS_DLRURL);

		return requestUrl.toString();
	}

	public String createPromoURL(Invoice invoice, String url) throws UnsupportedEncodingException {
		String message = smsMessageCreater.createPromoSms(url, invoice);
		String to = invoice.getRecipientMobile();

		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(urlString);
		requestUrl.append(USER_NAME);
		requestUrl.append(urlEncoder(promousername));
		requestUrl.append(SMS_PASSWORD);
		requestUrl.append(urlEncoder(promopassword));
		requestUrl.append(SMS_TO);
		requestUrl.append(urlEncoder(to));
		requestUrl.append(SMS_FROM);
		requestUrl.append(urlEncoder(promofrom));
		requestUrl.append(SMS_TEXT);
		requestUrl.append(urlEncoder(message));
		requestUrl.append(SMS_DLRMASK);
		requestUrl.append(SMS_DLRURL);

		return requestUrl.toString();
	}

	public static String urlEncoder(String value) throws UnsupportedEncodingException {
		return (URLEncoder.encode(value, SystemConstants.DEFAULT_ENCODING_UTF_8));
	}
}
