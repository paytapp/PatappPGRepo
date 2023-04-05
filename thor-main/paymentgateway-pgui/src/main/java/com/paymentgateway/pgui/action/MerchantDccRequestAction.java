package com.paymentgateway.pgui.action;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.pg.core.pageintegrator.GeneralValidator;
import com.paymentgateway.pg.core.util.RequestCreator;
import com.paymentgateway.pgui.action.service.ActionService;

public class MerchantDccRequestAction extends AbstractSecureAction implements ServletRequestAware {

	private static final long serialVersionUID = 4588662731360119045L;
	private static final Logger logger = LoggerFactory.getLogger(MerchantDccRequestAction.class.getName());
	private HttpServletRequest request;

	@Autowired
	private ActionService actionService;

	@Autowired
	private RequestCreator requestCreator;

	@Autowired
	private GeneralValidator generalValidator;

	@Autowired
	private UserDao userDao;

	public String execute() {
		try {
			// clean session
			sessionMap.invalidate();
			
			/*
			 * Fields fields = actionService.prepareFields(request.getParameterMap());
			 * String fieldsAsString = fields.getFieldsAsBlobString();
			 * sessionMap.put(Constants.FIELDS.getValue(), fields);
			 * fields.put(FieldType.INTERNAL_REQUEST_FIELDS.getName(), fieldsAsString);
			 */
			
			Map<String, String> params = new HashMap<>();
			Map<String, String> requestcreater = new HashMap<String, String>();

			Map<String, String[]> parameterMap = request.getParameterMap();
			parameterMap.forEach((key, value) -> {
				params.put(key, value[0]);
			});

			SimpleDateFormat datetime = new SimpleDateFormat("YYYY:MM:dd-HH:mm:ss");
			Date currentDateTime = new Date();
			String txndatetime = datetime.format(currentDateTime);
			String storename = "3344000689";
			String sharedsecret = "hgH!k23#Lt";

			String amount = params.get("AMOUNT");
			String txnType = params.get("TXNTYPE");
			String orderId = params.get("ORDER_ID");
			String currency = params.get("CURRENCY_CODE");
			String return_url = params.get("RETURN_URL");

			String calculatedHash = createHash(storename, txndatetime, amount, currency, sharedsecret);
			// System.out.println(calculatedHash);
			requestcreater.put("Transaction Type", txnType);
			requestcreater.put("Currency", currency);
			requestcreater.put("Order Id", orderId);
			requestcreater.put("Chargetotal", amount);
			requestcreater.put("Date Time", txndatetime);
			requestcreater.put("Response URL", return_url);
			requestcreater.put("Calculated Hash", calculatedHash);

//			requestCreator.createDccRequestPage(requestcreater);

		} catch (Exception exception) {
			sessionMap.invalidate();
			logger.error("Unknown error in merchant DCC payment request", exception);
			return ERROR;
		}
		return Action.NONE;
	}

	public static String createHash(String storeId, String txndatetime, String chargetotal, String currency,
			String sharedsecret) {
		String stringToHash = storeId + txndatetime + chargetotal + currency + sharedsecret;
		System.out.println("stringToHash :" + stringToHash);
		return calculateHashFromHex(new StringBuffer(stringToHash));
	}

	private static String calculateHashFromHex(StringBuffer buffer) {
		String algorithm = "SHA-256";

		MessageDigest messageDigest = null;

		try {
			messageDigest = MessageDigest.getInstance(algorithm);
		} catch (Exception e) {
			throw new IllegalArgumentException("Algorithm '" + algorithm + "' not supported");
		}

		StringBuffer result = new StringBuffer();
		StringBuffer sb = new StringBuffer();
		byte[] bytes = buffer.toString().getBytes();

		int byteLen = bytes.length;

		for (int i = 0; i < byteLen; i++) {

			byte b = bytes[i];
			sb.append(Character.forDigit((b & 240) >> 4, 16));
			sb.append(Character.forDigit((b & 15), 16));

		}

		buffer = new StringBuffer(sb.toString());
		messageDigest.update(buffer.toString().getBytes());
		byte[] message = messageDigest.digest();
		int messageLen = message.length;

		for (int j = 0; j < messageLen; j++) {

			byte b = message[j];
			String apps = Integer.toHexString(b & 0xff);
			if (apps.length() == 1) {
				apps = "0" + apps;
			}
			result.append(apps);
		}
		System.out.println("HASH :" + result.toString());
		return result.toString();

	}

	@Override
	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}

}
