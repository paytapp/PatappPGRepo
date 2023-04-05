package com.paymentgateway.crm.action;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.SmsSender;
import com.paymentgateway.commons.dao.ENachDao;
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
public class EnachThroughLinkAction extends AbstractSecureAction {

	@Autowired
	private EmailServiceProvider emailServiceProvider;

	@Autowired
	private SmsSender smsSender;

	@Autowired
	private Fields field;

	@Autowired
	private ENachDao enachDao;
	
	@Autowired
	private BitlyUrlShortener bitlyUrlShortener;

	@Autowired
	@Qualifier("propertiesManager")
	private PropertiesManager propertiesManager;

	private static final long serialVersionUID = 5270945060983924608L;
	private static Logger logger = LoggerFactory.getLogger(EnachThroughLinkAction.class.getName());

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

			requestFields.put(FieldType.RETURN_URL.getName(), "");
			requestFields.put(FieldType.AMOUNT.getName(), "1.00");
			requestFields.put(FieldType.MONTHLY_AMOUNT.getName(), 
					String.valueOf(new BigDecimal(monthlyAmount).setScale(2,
					BigDecimal.ROUND_HALF_UP)));
			requestFields.put(FieldType.FREQUENCY.getName(), frequency);
			requestFields.put(FieldType.TENURE.getName(), tenure);

			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				requestFields.put(FieldType.PAY_ID.getName(), sessionUser.getPayId());
			} else {

				if (StringUtils.isNotBlank(subMerchantEmailId)) {
					requestFields.put(FieldType.PAY_ID.getName(),
							/* userDao.getPayIdByEmailId( */subMerchantEmailId/* ) */);
				} else {
					requestFields.put(FieldType.PAY_ID.getName(), merchantPayId);
				}
			}

			requestFields.put(FieldType.CUST_EMAIL.getName(), custEmailId);
			requestFields.put("CUST_MOBILE", custMobile);
			requestFields.put("ORDER_ID", orderId);
			requestFields.put(FieldType.HASH.getName(), Hasher.getHash(new Fields(requestFields)));

			String responseBody;
			String serviceUrl = "";
			if (linkThrough.equalsIgnoreCase("email")) {
				serviceUrl = PropertiesManager.propertiesMap.get(Constants.ICICI_ENACH_THROUGH_LINK_EMAIL.getValue());
				try {

					responseMap = emailServiceProvider.eMandateSignMail(new Fields(requestFields));
					responseBody = responseMap.get("responseBody");

					if(responseBody != null) {
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
				serviceUrl = PropertiesManager.propertiesMap.get(Constants.ICICI_ENACH_THROUGH_LINK_SMS.getValue());
				try {
					StringBuilder smsBody = new StringBuilder();
					smsBody.append("Dear Customer" + "\n\n"
							+ "Please click on the link below to register for eNach mandate. INR 1 will be deducted from your account to verify your bank account details. ");

					String url = propertiesManager.propertiesMap.get("DEMO_ENACH_MANDATE_SIGN");

					String longUrl = url + "?ORDER_ID=:" + requestFields.get("ORDER_ID") + "," + "?AMOUNT=:"
							+ requestFields.get(FieldType.AMOUNT.getName()) + "," + "?MONTHLY_AMOUNT=:"
							+ requestFields.get(FieldType.MONTHLY_AMOUNT.getName()) + "," + "?FREQUENCY=:"
							+ requestFields.get(FieldType.FREQUENCY.getName()) + "," + "?TENURE=:"
							+ requestFields.get(FieldType.TENURE.getName()) + "," + "?PAY_ID=:"
							+ requestFields.get(FieldType.PAY_ID.getName()) + "," + "?CUST_MOBILE=:"
							+ requestFields.get("CUST_MOBILE") + "," + "?CUST_EMAIL=:"
							+ requestFields.get(FieldType.CUST_EMAIL.getName()) + "," + "?HASH=:"
							+ requestFields.get(FieldType.HASH.getName()) + "," + "?RETURN_URL=:"
							+ requestFields.get(FieldType.RETURN_URL.getName()) + "+";
					
					smsBody.append(bitlyUrlShortener.createShortUrlUsingBitly(longUrl));

					smsBody.append("\n\n--\nTeam Payment Gateway");

					String smsInnuvisolutions = PropertiesManager.propertiesMap
							.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());

					responseMap.put(FieldType.EMANDATE_URL.getName(), bitlyUrlShortener.createShortUrlUsingBitly(longUrl));
					
					if (StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {
						responseBody = smsSender.sendSMSByInnvisSolution(custMobile, smsBody.toString());
					} else {
						responseBody = smsSender.sendSMS(custMobile, smsBody.toString());
					}
					if (responseBody == null) {
						responseMessage = "SMS Not Send";
						response = "FAIL";
					} else {
						responseMessage = ("SMS has been sent to " + field.fieldMask(custMobile));
						response = SUCCESS;
					}
				} catch (Exception exception) {
					logger.error("exception is ", exception);
					responseMessage = "SMS Not Send";
					response = "FAIL";
					throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to SMS ");
				}
				
			} else {
				StringBuilder responseMessageSB = new StringBuilder();
				//Sending Email
				try {
					responseMap = emailServiceProvider.eMandateSignMail(new Fields(requestFields));
					responseBody = responseMap.get("responseBody");

					if(responseBody != null) {
						responseMessageSB.append(responseBody);
					} else {
						responseMessageSB.append("Mail Not Send");
					}
					//responseBody = emailServiceProvider.eMandateSign(new Fields(requestFields));
					//responseMessage = responseBody;
					//responseMessageSB.append(responseBody);
					response = SUCCESS;
				} catch (Exception exception) {
					logger.error("exception is ", exception);
					//responseMessage = "Mail Not Send";
					responseMessageSB.append("Mail Not Send");
					response = "FAIL";
					throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to emailer");
				}
				
				responseMessageSB.append(" & ");
				//Sending sms
				try {
					StringBuilder smsBody = new StringBuilder();
					smsBody.append("Dear Customer" + "\n\n"
							+ "Please click on the link below to register for eNach mandate. INR 1 will be deducted from your account to verify your bank account details. ");

					String url = propertiesManager.propertiesMap.get("DEMO_ENACH_MANDATE_SIGN");

					String longUrl = url + "?ORDER_ID=:" + requestFields.get("ORDER_ID") + "," + "?AMOUNT=:"
							+ requestFields.get(FieldType.AMOUNT.getName()) + "," + "?MONTHLY_AMOUNT=:"
							+ requestFields.get(FieldType.MONTHLY_AMOUNT.getName()) + "," + "?FREQUENCY=:"
							+ requestFields.get(FieldType.FREQUENCY.getName()) + "," + "?TENURE=:"
							+ requestFields.get(FieldType.TENURE.getName()) + "," + "?PAY_ID=:"
							+ requestFields.get(FieldType.PAY_ID.getName()) + "," + "?CUST_MOBILE=:"
							+ requestFields.get("CUST_MOBILE") + "," + "?CUST_EMAIL=:"
							+ requestFields.get(FieldType.CUST_EMAIL.getName()) + "," + "?HASH=:"
							+ requestFields.get(FieldType.HASH.getName()) + "," + "?RETURN_URL=:"
							+ requestFields.get(FieldType.RETURN_URL.getName()) + "+";
					smsBody.append(bitlyUrlShortener.createShortUrlUsingBitly(longUrl));

					smsBody.append("\n\n--\nTeam Payment Gateway");

					String smsInnuvisolutions = PropertiesManager.propertiesMap
							.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());

					responseMap.put(FieldType.EMANDATE_URL.getName(), bitlyUrlShortener.createShortUrlUsingBitly(longUrl));
					
					if (StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {
						responseBody = smsSender.sendSMSByInnvisSolution(custMobile, smsBody.toString());
					} else {
						responseBody = smsSender.sendSMS(custMobile, smsBody.toString());
					}
					if (responseBody == null) {
						//responseMessage = "SMS Not Send";
						responseMessageSB.append("SMS Not Send");
						response = "FAIL";
					} else {
						//responseMessage = ("SMS has been sent to " + custMobile);
						responseMessageSB.append("SMS sent to " + field.fieldMask(custMobile));
						response = SUCCESS;
					}
				} catch (Exception exception) {
					logger.error("exception is ", exception);
					//responseMessage = "SMS Not Send";
					responseMessageSB.append("SMS Not Send");
					response = "FAIL";
					throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to SMS ");
				}
				
				responseMessage = responseMessageSB.toString();
				
/*				serviceUrl = PropertiesManager.propertiesMap.get(Constants.ICICI_ENACH_THROUGH_LINK_SMS.getValue());
				try {

					StringBuilder smsBody = new StringBuilder();
					smsBody.append("Dear Customer your E_Nach Mandate Sign " + "\n");

					String url = propertiesManager.propertiesMap.get("DEMO_ENACH_MANDATE_SIGN");
					// String returnUrl = requestField.get("RETURN_URL");
					smsBody.append("<a href='" + url + "?ORDER_ID=:" + requestFields.get("ORDER_Id") + "," + "?AMOUNT=:"
							+ requestFields.get(FieldType.AMOUNT.getName()) + "," + "?MONTHLY_AMOUNT=:"
							+ requestFields.get(FieldType.MONTHLY_AMOUNT.getName()) + "," + "?FREQUENCY=:"
							+ requestFields.get(FieldType.FREQUENCY.getName()) + "," + "?TENURE=:"
							+ requestFields.get(FieldType.TENURE.getName()) + "," + "?PAY_ID=:"
							+ requestFields.get(FieldType.ACCOUNT_HOLDER_NAME.getName()) + "," + "?CUST_MOBILE=:"
							+ requestFields.get("CUST_MOBILE") + "," + "?CUST_EMAIL=:"
							+ requestFields.get(FieldType.ACCOUNT_HOLDER_NAME.getName()) + "," + "?HASH=:"
							+ requestFields.get(FieldType.ACCOUNT_HOLDER_NAME.getName()) + "," + "?RETURN_URL=:"
							+ requestFields.get("RETURN_URL") + "+ \">");

					JSONObject json = new JSONObject();
					json.put(custMobile, smsBody);

					CloseableHttpClient httpClient = HttpClientBuilder.create().build();
					HttpPost request = new HttpPost(serviceUrl);
					StringEntity params = new StringEntity(json.toString());
					request.addHeader("content-type", "application/json");
					request.setEntity(params);
					HttpResponse resp = httpClient.execute(request);
					responseBody = EntityUtils.toString(resp.getEntity());
					responseMessage = responseBody;
					response = SUCCESS;
				} catch (Exception exception) {
					logger.error("exception is ", exception);
					responseMessage = "SMS Not Send";
					response = "FAIL";
					throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to SMS ");
				}
				*/
			}			
			requestFields.put(FieldType.EMANDATE_URL.getName(), responseMap.get(FieldType.EMANDATE_URL.getName()));
			Fields reqFields= new Fields(requestFields);
			enachDao.insertEnachRegistrationLinkDetail(reqFields);			
			
		} catch (Exception ex) {
			logger.info("Exception caught while sending eNach mandate through link " + ex);
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
