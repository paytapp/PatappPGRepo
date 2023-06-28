package com.paymentgateway.pg.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.SmsSender;
import com.paymentgateway.commons.dao.ENachDao;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.dao.UpiAutoPayDao;
import com.paymentgateway.commons.email.EmailServiceProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.BitlyUrlShortener;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.SystemProperties;
import com.paymentgateway.pg.core.pageintegrator.GeneralValidator;

@Service
public class ENachRegistrationService {

	private Logger logger = LoggerFactory.getLogger(ENachRegistrationService.class.getName());

	@Autowired
	private EmailServiceProvider emailServiceProvider;

	@Autowired
	private GeneralValidator generalValidator;

	@Autowired
	private BitlyUrlShortener bitlyUrlShortener;

	@Autowired
	private SmsSender smsSender;

	@Autowired
	private Fields field;

	@Autowired
	FieldsDao fieldsDao;

	@Autowired
	ENachDao eNachDao;

	public Map<String, String> eMandateSignLink(Fields fields) {

		String responseBody = null;
		HashMap<String, String> response = new HashMap<String, String>();
		Fields responseFields = new Fields(fields);

		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDate = df.format(date);

		Fields newFields = new Fields();
		if (fields.contains(FieldType.HASH.getName()) && StringUtils.isNotBlank(fields.get(FieldType.HASH.getName()))) {

			String merchantHash = fields.get(FieldType.HASH.getName());
			fields.remove(FieldType.HASH.getName());
			String calculatedHash;
			if (!fields.contains(FieldType.PAY_ID.getName())) {
				logger.error("PAY_ID not available ");
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PAY_ID.getInternalMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PAY_ID.getCode());
				return response;
			}
			try {
				calculatedHash = Hasher.getHash(fields);
			} catch (SystemException e1) {
				logger.error("Exception while generating Hash; " + e1);
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PAY_ID.getInternalMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PAY_ID.getCode());
				return response;
			}
			logger.info("Merchant Hash: " + merchantHash + " | Calculated Hash: " + calculatedHash);
			if (merchantHash.equals(calculatedHash)) {
				Map<String, String> validationResult = ValidateFieldsForEMandateSignRequest(fields);
				if (validationResult.get(FieldType.RESPONSE_CODE.getName())
						.equalsIgnoreCase(ErrorType.SUCCESS.getResponseCode())) {

					try {
						fields.remove(FieldType.HASH.getName());

						fields.put(FieldType.AMOUNT.getName(), "1");
						fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

						Map<String, String> responseMap = new HashMap<String, String>();
						responseMap = emailServiceProvider.eMandateSignForAPI(fields);

						// Sending sms
						try {
							StringBuilder smsBody = new StringBuilder();
							smsBody.append("Dear Customer" + "\n\n"
									+ "Please click on the link below to register for eNach mandate. INR 1 will be deducted from your account to verify your bank account details. ");

							smsBody.append(bitlyUrlShortener
									.createShortUrlUsingBitly(responseMap.get(FieldType.EMANDATE_URL.getName())));

							smsBody.append("\n\n--\nTeam Payment GateWay");

							String smsInnuvisolutions = PropertiesManager.propertiesMap
									.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());

							String smsResponseBody;
							if (StringUtils.isNotBlank(smsInnuvisolutions)
									&& smsInnuvisolutions.equalsIgnoreCase("Y")) {
								smsResponseBody = smsSender.sendSMSByInnvisSolution(fields.get("CUST_MOBILE"),
										smsBody.toString());
							} else {
								smsResponseBody = smsSender.sendSMS(fields.get("CUST_MOBILE"), smsBody.toString());
							}
							if (smsResponseBody != null) {
								responseMap.put("SEND_SMS", ErrorType.SUCCESS.getInternalMessage());
							} else {
								responseMap.put("SEND_SMS", ErrorType.SMS_ERROR.getInternalMessage());
							}
						} catch (Exception exception) {
							logger.error("exception is ", exception);
							responseMap.put("SEND_SMS", ErrorType.SMS_ERROR.getInternalMessage());
							throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to SMS ");
						}

						responseFields.putAll(responseMap);
						responseFields.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
						responseFields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getInternalMessage());
						responseFields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
						responseFields.put(FieldType.EMANDATE_URL.getName(), bitlyUrlShortener
								.createShortUrlUsingBitly(responseMap.get(FieldType.EMANDATE_URL.getName())));

					} catch (Exception exception) {
						logger.error("Error while sending mail and sms, ", exception);
					}

					Collection<String> callbackFields = SystemProperties.getResponseFields();
					for (String key : callbackFields) {
						if (StringUtils.isNotBlank(responseFields.get(key))) {
							newFields.put(key, responseFields.get(key));
						}
					}
				} else {					
					response.putAll(validationResult);
					response.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				}
			} else {
				responseFields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				responseFields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_HASH.getInternalMessage());
				responseFields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
			}

			newFields.put(responseFields);
			newFields.put(FieldType.RESPONSE_DATE_TIME.getName(), strDate);

			try {

				newFields.put(FieldType.HASH.getName(), Hasher.getHash(newFields));

				List<String> fieldTypeList = new ArrayList<String>(newFields.getFields().keySet());
				for (String fieldType : fieldTypeList) {
					response.put(fieldType, newFields.get(fieldType));
				}

			} catch (SystemException e1) {
				List<String> fieldTypeList = new ArrayList<String>(responseFields.getFields().keySet());
				for (String fieldType : fieldTypeList) {
					response.put(fieldType, responseFields.get(fieldType));
				}
				response.remove(FieldType.HASH.getName());
				logger.error("Exception while generating Hash; " + e1);
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PAY_ID.getInternalMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PAY_ID.getCode());
			}
			return response;

		} else {
			List<String> fieldTypeList = new ArrayList<String>(responseFields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				response.put(fieldType, responseFields.get(fieldType));
			}
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.NO_HASH.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.NO_HASH.getCode());
			return response;
		}
	}

	private Map<String, String> ValidateFieldsForEMandateSignRequest(Fields fields) {

		Map<String, String> validationMap = new HashMap<String, String>();

		if (fields.contains(FieldType.ORDER_ID.getName())) {

			if (fields.contains(FieldType.MONTHLY_AMOUNT.getName())
					&& StringUtils.isNotBlank(fields.get(FieldType.MONTHLY_AMOUNT.getName()))
					&& !fields.get(FieldType.MONTHLY_AMOUNT.getName()).contains("-")
					&& NumberUtils.isNumber(fields.get(FieldType.MONTHLY_AMOUNT.getName()))) {

				if (fields.contains(FieldType.FREQUENCY.getName())
						&& StringUtils.isNotBlank(fields.get(FieldType.FREQUENCY.getName()))
						&& StringUtils.isAlpha(fields.get(FieldType.FREQUENCY.getName()))) {

					if (fields.contains(FieldType.TENURE.getName())
							&& StringUtils.isNotBlank(fields.get(FieldType.TENURE.getName()))
							&& StringUtils.isNumeric(fields.get(FieldType.TENURE.getName()))) {

						if (fields.contains("CUST_MOBILE") && StringUtils.isNotBlank(fields.get("CUST_MOBILE"))
								&& StringUtils.isNumeric(fields.get("CUST_MOBILE"))) {

							if (fields.contains(FieldType.CUST_EMAIL.getName())
									&& StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))
									&& generalValidator.isValidEmailId(fields.get(FieldType.CUST_EMAIL.getName()))) {

								if (StringUtils.isNotBlank(fields.get(FieldType.ORDER_ID.getName()))
										&& StringUtils.isAlphanumeric(fields.get(FieldType.ORDER_ID.getName()))) {

									boolean duplicateFlag = false;
//									if (fields.contains(FieldType.SUB_MERCHANT_ID.getName()) && StringUtils
//											.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
//										duplicateFlag = upiAutoPayDao.checkDuplicateOrderId(fields.get("ORDER_ID"),
//												fields.get(FieldType.PAY_ID.getName()),
//												fields.get(FieldType.SUB_MERCHANT_ID.getName()),
//												Constants.ENACH_COLLECTION.getValue());
//									} else {
									duplicateFlag = eNachDao.duplicateOrderIdForMandateLink(
											fields.get(FieldType.ORDER_ID.getName()),
											fields.get(FieldType.PAY_ID.getName()),
											fields.get(FieldType.SUB_MERCHANT_ID.getName()));
//									}
									if (!duplicateFlag) {
										logger.info("all request fields are valid");
										validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
												ErrorType.SUCCESS.getResponseMessage());
										validationMap.put(FieldType.RESPONSE_CODE.getName(),
												ErrorType.SUCCESS.getResponseCode());
										return validationMap;
									} else {
										logger.info("Duplicate Order ID");
										validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
												ErrorType.DUPLICATE_ORDER_ID.getResponseMessage());
										validationMap.put(FieldType.RESPONSE_CODE.getName(),
												ErrorType.DUPLICATE_ORDER_ID.getResponseCode());
										return validationMap;
									}
								}
								logger.info("Valid ORDER_ID Required");
								validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
										ErrorType.INVALID_ORDER_ID.getResponseMessage());
								validationMap.put(FieldType.RESPONSE_CODE.getName(),
										ErrorType.INVALID_ORDER_ID.getResponseCode());
								return validationMap;
							}

							logger.info("Invalid CUST_EMAIL");
							validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
									ErrorType.INVALID_CUST_EMAIL.getResponseMessage());
							validationMap.put(FieldType.RESPONSE_CODE.getName(),
									ErrorType.INVALID_CUST_EMAIL.getResponseCode());
							return validationMap;
						}
						logger.info("Invalid CUST_MOBILE");
						validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
								ErrorType.INVALID_CUST_MOBILE.getResponseMessage());
						validationMap.put(FieldType.RESPONSE_CODE.getName(),
								ErrorType.INVALID_CUST_MOBILE.getResponseCode());
						return validationMap;
					}

					logger.info("Invalid TENURE");
					validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
							ErrorType.INVALID_TENURE.getResponseMessage());
					validationMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_TENURE.getResponseCode());
					return validationMap;
				}
				logger.info("Invalid FREQUENCY");
				validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.INVALID_FREQUENCY.getResponseMessage());
				validationMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_FREQUENCY.getResponseCode());
				return validationMap;
			}
			logger.info("Invalid MONTHLY_AMOUNT");
			validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
					ErrorType.INVALID_MONTHLY_AMOUNT.getResponseMessage());
			validationMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_MONTHLY_AMOUNT.getResponseCode());
			return validationMap;
		} else {
			logger.info("ORDER_ID Required");
			validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.NO_ORDER_ID.getResponseMessage());
			validationMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.NO_ORDER_ID.getResponseCode());
			return validationMap;
		}
	}

	public Map<String, String> eNachRegistrationStatusEnquiry(Fields fields) {

		HashMap<String, String> response = new HashMap<String, String>();
		// Fields responseFields = new Fields(fields);
		// Fields responseFields = new Fields(fields);
		Fields newFields = new Fields(fields);

		HashMap<String, String> responseMap = new HashMap<String, String>();

		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDate = df.format(date);

//		boolean flag = validHash(fields);
		if (fields.contains(FieldType.HASH.getName()) && StringUtils.isNotBlank(fields.get(FieldType.HASH.getName()))) {
			List<String> fieldTypeList = new ArrayList<String>(newFields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				responseMap.put(fieldType, newFields.get(fieldType));
			}

			String merchantHash = fields.get(FieldType.HASH.getName());
			fields.remove(FieldType.HASH.getName());
			String calculatedHash;
			if (!fields.contains(FieldType.PAY_ID.getName())) {
				logger.error("PAY_ID not available ");
				response.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PAY_ID.getInternalMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PAY_ID.getCode());
				return response;
			}
			try {
				calculatedHash = Hasher.getHash(fields);
			} catch (SystemException e1) {
				logger.error("Exception while generating Hash; " + e1);
				response.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PAY_ID.getInternalMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PAY_ID.getCode());
				return response;
			}
			logger.info("Merchant Hash: " + merchantHash + " | Calculated Hash: " + calculatedHash);
			if (merchantHash.equals(calculatedHash)) {
				if (fields.contains(FieldType.ORDER_ID.getName())) {
					if (StringUtils.isNotBlank(fields.get(FieldType.ORDER_ID.getName()))
							&& StringUtils.isAlphanumeric(fields.get(FieldType.ORDER_ID.getName()))) {

						responseMap.putAll(fieldsDao.getENachRegistrationDetailsByOrderId(fields));

						Collection<String> callbackFields = SystemProperties.getResponseFields();
						for (String key : callbackFields) {
							if (StringUtils.isNotBlank(responseMap.get(key))) {
								responseMap.put(key, responseMap.get(key));
							}
						}
						responseMap.put(FieldType.DEBIT_START_DATE.getName(),
								responseMap.get(FieldType.DATEFROM.getName()));
						responseMap.put(FieldType.DEBIT_END_DATE.getName(),
								responseMap.get(FieldType.DATETO.getName()));
						responseMap.remove(FieldType.DATEFROM.getName());
						responseMap.remove(FieldType.DATETO.getName());
						responseMap.remove("MERCHANT_LOGO");
					} else {
						logger.info("Valid ORDER_ID Required");
						responseMap.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
						responseMap.put(FieldType.RESPONSE_MESSAGE.getName(),
								ErrorType.INVALID_ORDER_ID.getResponseMessage());
						responseMap.put(FieldType.RESPONSE_CODE.getName(),
								ErrorType.INVALID_ORDER_ID.getResponseCode());
						return responseMap;
					}
				} else {
					logger.info("ORDER_ID Required");
					responseMap.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.NO_ORDER_ID.getResponseMessage());
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.NO_ORDER_ID.getResponseCode());
					return responseMap;
				}
			} else {
				responseMap.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_HASH.getInternalMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());

			}

			responseMap.put(FieldType.RESPONSE_DATE_TIME.getName(), strDate);

			newFields.putAll(responseMap);
			try {

				response.put(FieldType.HASH.getName(), Hasher.getHash(newFields));
				response.putAll(responseMap);

			} catch (SystemException e1) {
				logger.error("Exception while generating Hash; " + e1);
				response.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PAY_ID.getInternalMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PAY_ID.getCode());
			}

			return response;

		} else {
			List<String> fieldTypeList = new ArrayList<String>(newFields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				response.put(fieldType, newFields.get(fieldType));
			}
			responseMap.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.NO_HASH.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.NO_HASH.getCode());
			return response;
		}
	}

}
