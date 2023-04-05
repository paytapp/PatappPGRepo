package com.paymentgateway.crm.action;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.SmsControllerServiceProvider;
import com.paymentgateway.commons.dao.UpiAutoPayDao;
import com.paymentgateway.commons.email.EmailServiceProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.BitlyUrlShortener;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionManager;

/**
 * @author Rajit
 */
public class UpiAutoPayThroughLinkAction extends AbstractSecureAction {

	@Autowired
	private EmailServiceProvider emailServiceProvider;

	@Autowired
	private BitlyUrlShortener bitlyUrlShortener;

	@Autowired
	private UpiAutoPayDao upiAutoPayDao;

	@Autowired
	private SmsControllerServiceProvider smsControllerServiceProvider;

	@Autowired
	private Fields fields;

	@Autowired
	@Qualifier("propertiesManager")
	private PropertiesManager propertiesManager;
	private static final long serialVersionUID = 6908917229260503829L;
	private static Logger logger = LoggerFactory.getLogger(UpiAutoPayThroughLinkAction.class.getName());

	private String merchantPayId;
	private String subMerchantEmailId;
	private String returnURL;
	private String custMobile;
	private String custEmailId;
	private String monthlyAmount;
	private String frequency;
	private String tenure;
	private String linkThrough; // sms, email, both
	private String response;
	private String responseMessage;
	private User sessionUser = new User();

	@SuppressWarnings("static-access")
	public String execute() {

		String orderId = "CI" + TransactionManager.getNewTransactionId();
		logger.info("inside mandate registration through link");
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			Map<String, String> requestFields = new HashMap<String, String>();
			Map<String, String> responseMap = new HashMap<String, String>();

			requestFields.put(FieldType.RETURN_URL.getName(),
					propertiesManager.propertiesMap.get("UPI_AUTOPAY_REPONSE"));
			requestFields.put(FieldType.AMOUNT.getName(), "1.00");
			requestFields.put(FieldType.MONTHLY_AMOUNT.getName(),
					String.valueOf(new BigDecimal(monthlyAmount).setScale(2, BigDecimal.ROUND_HALF_UP)));
			requestFields.put(FieldType.FREQUENCY.getName(), frequency);
			requestFields.put(FieldType.TENURE.getName(), tenure);
			requestFields.put(FieldType.PURPOSE.getName(), "RECURRING");

			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				requestFields.put(FieldType.PAY_ID.getName(), sessionUser.getPayId());
			} else {

				if (StringUtils.isNotBlank(subMerchantEmailId)) {
					requestFields.put(FieldType.PAY_ID.getName(), subMerchantEmailId);
				} else {
					requestFields.put(FieldType.PAY_ID.getName(), merchantPayId);
				}
			}

			requestFields.put(FieldType.CUST_EMAIL.getName(), custEmailId);
			requestFields.put("CUST_MOBILE", custMobile);
			requestFields.put(FieldType.ORDER_ID.getName(), orderId);
			requestFields.put(FieldType.HASH.getName(), Hasher.getHash(new Fields(requestFields)));

			String responseBody = "";
			if (linkThrough.equalsIgnoreCase("email")) {
				try {
					responseMap = emailServiceProvider.upiAutoPayMandateSignMail(new Fields(requestFields));
					responseBody = responseMap.get("responseBody");

					if (responseBody != null) {
						responseMessage = responseBody;
					} else {
						responseMessage = "Mail Not Send";
					}

					response = SUCCESS;
				} catch (Exception exception) {
					logger.error("exception is ", exception);
					responseMessage = "Mail Not Send";
					response = "FAIL";
					throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to emailer");
				}
			} else if (linkThrough.equalsIgnoreCase("sms")) {
				try {
					String url = propertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MANDATE_SIGN.getValue());
					String mandate_url = url + "?ORDER_ID=:" + requestFields.get(FieldType.ORDER_ID.getName()) + ","
							+ "?AMOUNT=:" + requestFields.get(FieldType.AMOUNT.getName()) + "," + "?MONTHLY_AMOUNT=:"
							+ requestFields.get(FieldType.MONTHLY_AMOUNT.getName()) + "," + "?FREQUENCY=:"
							+ requestFields.get(FieldType.FREQUENCY.getName()) + "," + "?TENURE=:"
							+ requestFields.get(FieldType.TENURE.getName()) + "," + "?PAY_ID=:"
							+ requestFields.get(FieldType.PAY_ID.getName()) + "," + "?CUST_MOBILE=:"
							+ requestFields.get("CUST_MOBILE") + "," + "?CUST_EMAIL=:"
							+ requestFields.get(FieldType.CUST_EMAIL.getName()) + "," + "?PURPOSE=:"
							+ requestFields.get(FieldType.PURPOSE.getName()) + "," + "?HASH=:"
							+ requestFields.get(FieldType.HASH.getName()) + "," + "?RETURN_URL=:"
							+ requestFields.get("RETURN_URL");
					String bitly_url = bitlyUrlShortener.createShortUrlUsingBitly(mandate_url);

					boolean isSmsSend = smsControllerServiceProvider
							.sendUpiAutoPayLinkViaSMS(requestFields.get("CUST_MOBILE"), bitly_url);
					if (isSmsSend == true) {
						responseMessage = "SMS Send to " + fields.fieldMask(requestFields.get("CUST_MOBILE"));
						response = SUCCESS;
					} else {
						responseMessage = "SMS Not Send";
						response = "FAIL";
					}
				} catch (Exception exception) {
					logger.error("exception is ", exception);
					responseMessage = "SMS Not Send";
					response = "FAIL";
					throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to SMS ");
				}
			} else {
				try {
					responseMap = emailServiceProvider.upiAutoPayMandateSignMail(new Fields(requestFields));
					responseBody = responseMap.get("responseBody");

					if (responseBody != null) {
						responseMessage = responseBody;
					} else {
						responseMessage = "Mail Not Send";
					}

					response = SUCCESS;
				} catch (Exception exception) {
					logger.error("exception is ", exception);
					responseMessage = "Mail Not Send";
					response = "FAIL";
					throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to emailer");
				}

				try {
					String url = propertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MANDATE_SIGN.getValue());
					String mandate_url = url + "?ORDER_ID=:" + requestFields.get(FieldType.ORDER_ID.getName()) + ","
							+ "?AMOUNT=:" + requestFields.get(FieldType.AMOUNT.getName()) + "," + "?MONTHLY_AMOUNT=:"
							+ requestFields.get(FieldType.MONTHLY_AMOUNT.getName()) + "," + "?FREQUENCY=:"
							+ requestFields.get(FieldType.FREQUENCY.getName()) + "," + "?TENURE=:"
							+ requestFields.get(FieldType.TENURE.getName()) + "," + "?PAY_ID=:"
							+ requestFields.get(FieldType.PAY_ID.getName()) + "," + "?CUST_MOBILE=:"
							+ requestFields.get("CUST_MOBILE") + "," + "?CUST_EMAIL=:"
							+ requestFields.get(FieldType.CUST_EMAIL.getName()) + "," + "?PURPOSE=:"
							+ requestFields.get(FieldType.PURPOSE.getName()) + "," + "?HASH=:"
							+ requestFields.get(FieldType.HASH.getName()) + "," + "?RETURN_URL=:"
							+ requestFields.get("RETURN_URL");
					String bitly_url = bitlyUrlShortener.createShortUrlUsingBitly(mandate_url);

					boolean isSmsSend = smsControllerServiceProvider
							.sendUpiAutoPayLinkViaSMS(requestFields.get("CUST_MOBILE"), bitly_url);
					if (isSmsSend == true) {
						if (StringUtils.isNoneBlank(requestFields.get(FieldType.CUST_EMAIL.getName()))
								&& StringUtils.isNotBlank(requestFields.get("CUST_MOBILE"))) {
							responseMessage = "Link send on Email "
									+ fields.maskEmail(requestFields.get(FieldType.CUST_EMAIL.getName()))
									+ " and SMS on " + fields.fieldMask(requestFields.get("CUST_MOBILE"));
						} else {
							responseMessage = "Link send";
						}
						response = SUCCESS;
					} else {
						responseMessage = "SMS Not Send";
						response = "FAIL";
					}
				} catch (Exception exception) {
					logger.error("exception is ", exception);
					responseMessage = "SMS Not Send";
					response = "FAIL";
					throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to SMS ");
				}
			}

			requestFields.put(FieldType.EMANDATE_URL.getName(), responseMap.get(FieldType.EMANDATE_URL.getName()));
			Fields reqFields = new Fields(requestFields);
			upiAutoPayDao.insertUpiAutopayRegistrationLinkDetail(reqFields);

		} catch (Exception ex) {
			logger.info("Exception caught while sending upi autopay mandate through link ", ex);
		}
		return SUCCESS;
	}

	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public String getSubMerchantEmailId() {
		return subMerchantEmailId;
	}

	public void setSubMerchantEmailId(String subMerchantEmailId) {
		this.subMerchantEmailId = subMerchantEmailId;
	}

	public String getCustMobile() {
		return custMobile;
	}

	public void setCustMobile(String custMobile) {
		this.custMobile = custMobile;
	}

	public String getCustEmailId() {
		return custEmailId;
	}

	public void setCustEmailId(String custEmailId) {
		this.custEmailId = custEmailId;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public String getReturnURL() {
		return returnURL;
	}

	public void setReturnURL(String returnURL) {
		this.returnURL = returnURL;
	}

	public String getMonthlyAmount() {
		return monthlyAmount;
	}

	public void setMonthlyAmount(String monthlyAmount) {
		this.monthlyAmount = monthlyAmount;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public String getTenure() {
		return tenure;
	}

	public void setTenure(String tenure) {
		this.tenure = tenure;
	}

	public String getLinkThrough() {
		return linkThrough;
	}

	public void setLinkThrough(String linkThrough) {
		this.linkThrough = linkThrough;
	}

}
