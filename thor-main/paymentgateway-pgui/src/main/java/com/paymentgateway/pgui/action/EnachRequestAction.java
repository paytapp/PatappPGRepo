package com.paymentgateway.pgui.action;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.dao.UpiAutoPayDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.NBToken;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Base64EncodeDecode;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.EnachDCIssuerType;
import com.paymentgateway.commons.util.EnachNBIssuerType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.Frequency;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pgui.action.service.ActionService;
import com.paymentgateway.pgui.action.service.PgActionServiceFactory;

/**
 * @author Rajit, Sandeep(Struts to Spring MVC changes)
 */
@Service
public class EnachRequestAction {

	private static Logger logger = LoggerFactory.getLogger(EnachRequestAction.class.getName());

	@Autowired
	TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private Base64EncodeDecode base64EncodeDecode;

	@Autowired
	private UserDao userDao;

	@Autowired
	private UpiAutoPayDao upiAutoPayDao;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private PropertiesManager propertiesManager;

	@SuppressWarnings("static-access")
	public Map<String, String> enachRequestHandler(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		Map<String, String> aaData = new HashMap<String, String>();
		logger.info("inside execute EnachRequestAction");
		try {
			ActionService service = PgActionServiceFactory.getActionService();
			Fields fields = service.prepareFields(request.getParameterMap());

			logger.info("request fields " + fields.getFieldsAsString());
			Map<String, String> validateFields = ValidateRequestFields(fields);
			if (validateFields.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase("true")) {

				String merchantHash = fields.get(FieldType.HASH.getName());
				fields.remove(FieldType.HASH.getName());
				String calculatedHash = Hasher.getHash(fields);

				if (merchantHash.equals(calculatedHash)) {

					logger.info("merchant hash matched");
					User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));

					// for reseller sub merchant
					if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())
							&& StringUtils.isNotBlank(user.getResellerId())) {
						aaData.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
						aaData.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
						aaData.put(FieldType.RESELLER_ID.getName(), user.getResellerId());

					} else if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
						// for Sub Merchant

						// super merchantId
						aaData.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
						// sub MerchantId
						aaData.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));

					} else if (StringUtils.isNotBlank(user.getResellerId())) {
						// Reseller Merchant
						aaData.put(FieldType.RESELLER_ID.getName(), user.getResellerId());
						aaData.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
						aaData.put(FieldType.SUB_MERCHANT_ID.getName(), "");

					} else {
						// super merchantId
						aaData.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
						// sub MerchantId
						aaData.put(FieldType.SUB_MERCHANT_ID.getName(), "");
					}

					StringBuilder base64EncodeImage = new StringBuilder();
					String finalLogoLocation = null;
					File imageLocation = new File(
							propertiesManager.propertiesMap.get(Constants.LOGO_FILE_UPLOAD_LOCATION.getValue()) + "/"
									+ aaData.get(FieldType.PAY_ID.getName()));
					if (!imageLocation.exists()) {
						logger.info("no such a directory for merchant logo ");
						base64EncodeImage.append(
								propertiesManager.propertiesMap.get(Constants.ICICI_ENACH_PAYMENT_GATEWAY_LOGO.getValue()));
					} else {

						String contents[] = imageLocation.list();
						finalLogoLocation = imageLocation.toString() + "/" + contents[0];
						if (contents[0].contains(".png")) {
							base64EncodeImage.append("data:image/png;base64,");
							base64EncodeImage.append(base64EncodeDecode.base64Encoder(new File(finalLogoLocation)));
						} else {
							base64EncodeImage.append("data:image/jpg;base64,");
							base64EncodeImage.append(base64EncodeDecode.base64Encoder(new File(finalLogoLocation)));
						}
					}

					boolean duplicateFlag = upiAutoPayDao.checkDuplicateOrderId(
							fields.get(FieldType.ORDER_ID.getName()), aaData.get(FieldType.PAY_ID.getName()),
							aaData.get(FieldType.SUB_MERCHANT_ID.getName()), Constants.ENACH_COLLECTION.getValue());

					if (!duplicateFlag) {

						String currentDate = DateCreater.defaultFromDate();
						fields.put(FieldType.DATE_FROM.getName(), currentDate);
						fields.put(FieldType.DATE_TO.getName(), currentDate);

						String totalAmount = String
								.valueOf(new BigDecimal(fields.get(FieldType.MONTHLY_AMOUNT.getName()))
										.multiply(new BigDecimal(fields.get(FieldType.TENURE.getName())))
										.setScale(2, RoundingMode.HALF_DOWN));

						SimpleDateFormat sdf = new SimpleDateFormat(CrmFieldConstants.INPUT_DATE_FORMAT.getValue());
						Date date = sdf.parse(fields.get(FieldType.DATE_FROM.getName()));
						sdf = new SimpleDateFormat(CrmFieldConstants.OUTPUT_DATE_FORMAT.getValue());

						List<String> startEndDebitDate = null;

						if (fields.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("MNTH")
								|| fields.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("ADHO")) {

							startEndDebitDate = fieldsDao.getDueDateList(sdf.format(date),
									fields.get(FieldType.DATE_TO.getName()), fields.get(FieldType.TENURE.getName()),
									aaData.get(FieldType.PAY_ID.getName()),
									aaData.get(FieldType.SUB_MERCHANT_ID.getName()), "eNach");

						} else if (fields.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("QURT")) {

							startEndDebitDate = upiAutoPayDao.getDueDateListForMonth(sdf.format(date),
									fields.get(FieldType.DATE_TO.getName()), fields.get(FieldType.TENURE.getName()), 3,
									aaData.get(FieldType.PAY_ID.getName()),
									aaData.get(FieldType.SUB_MERCHANT_ID.getName()), "eNach");

						} else if (fields.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("MIAN")) {

							startEndDebitDate = upiAutoPayDao.getDueDateListForMonth(sdf.format(date),
									fields.get(FieldType.DATE_TO.getName()), fields.get(FieldType.TENURE.getName()), 6,
									aaData.get(FieldType.PAY_ID.getName()),
									aaData.get(FieldType.SUB_MERCHANT_ID.getName()), "eNach");

						} else if (fields.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("YEAR")) {

							startEndDebitDate = upiAutoPayDao.getDueDateListForMonth(sdf.format(date),
									fields.get(FieldType.DATE_TO.getName()), fields.get(FieldType.TENURE.getName()), 12,
									aaData.get(FieldType.PAY_ID.getName()),
									aaData.get(FieldType.SUB_MERCHANT_ID.getName()), "eNach");
						} else if (fields.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("BIMN")) {

							startEndDebitDate = upiAutoPayDao.getDueDateListForDays(sdf.format(date),
									fields.get(FieldType.DATE_TO.getName()), fields.get(FieldType.TENURE.getName()), 15,
									aaData.get(FieldType.PAY_ID.getName()),
									aaData.get(FieldType.SUB_MERCHANT_ID.getName()), "eNach");
						} else if (fields.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("WEEK")) {

							startEndDebitDate = upiAutoPayDao.getDueDateListForDays(sdf.format(date),
									fields.get(FieldType.DATE_TO.getName()), fields.get(FieldType.TENURE.getName()), 7,
									aaData.get(FieldType.PAY_ID.getName()),
									aaData.get(FieldType.SUB_MERCHANT_ID.getName()), "eNach");
						} else if (fields.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("DAIL")) {

							startEndDebitDate = upiAutoPayDao.getDueDateListForDays(sdf.format(date),
									fields.get(FieldType.DATE_TO.getName()), fields.get(FieldType.TENURE.getName()), 1,
									aaData.get(FieldType.PAY_ID.getName()),
									aaData.get(FieldType.SUB_MERCHANT_ID.getName()), "eNach");

						} else {
							startEndDebitDate = upiAutoPayDao.getDueDateListForOneTime(sdf.format(date),
									fields.get(FieldType.DATE_TO.getName()), aaData.get(FieldType.PAY_ID.getName()),
									aaData.get(FieldType.SUB_MERCHANT_ID.getName()), "eNach");
						}

						aaData.put("DEBIT_END_DATE", startEndDebitDate.get(startEndDebitDate.size() - 1));
						aaData.put("DEBIT_START_DATE", startEndDebitDate.get(0));
						aaData.put(FieldType.REGISTRATION_DATE.getName(), sdf.format(date));

						aaData.put("CONSUMER_ID", fields.get(FieldType.ORDER_ID.getName()));
						aaData.put("MERCHANT_NAME",
								userDao.getBusinessNameByPayId(fields.get(FieldType.PAY_ID.getName())));
						aaData.put(FieldType.TENURE.getName(), fields.get(FieldType.TENURE.getName()));
						aaData.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
						aaData.put(FieldType.FREQUENCY.getName(),
								Frequency.getFrequencyName(fields.get(FieldType.FREQUENCY.getName())));

						aaData.put(FieldType.AMOUNT.getName(), "1.00");
						aaData.put(FieldType.TOTAL_AMOUNT.getName(),
								String.valueOf(new BigDecimal(totalAmount).setScale(2, BigDecimal.ROUND_HALF_UP)));
						aaData.put(FieldType.MONTHLY_AMOUNT.getName(),
								String.valueOf(new BigDecimal(fields.get(FieldType.MONTHLY_AMOUNT.getName()))
										.setScale(2, BigDecimal.ROUND_HALF_UP)));

						aaData.put("CUSTOMER_MOBILE", fields.get("CUST_MOBILE"));
						aaData.put("CUSTOMER_EMAIL", fields.get(FieldType.CUST_EMAIL.getName()));
						aaData.put(FieldType.RETURN_URL.getName(),
								propertiesManager.propertiesMap.get(Constants.ICICI_ENACH_FORM_REPONSE.getValue()));
						aaData.put("MERCHANT_RETURN_URL", fields.get(FieldType.RETURN_URL.getName()));

						aaData.put("LOGO", base64EncodeImage.toString());
						aaData.put(FieldType.RESPONSE.getName(), "success");
						JSONObject enachDCIssuerTypeJson;
						JSONArray enachDCIssuerTypeArray = new JSONArray();
						EnachDCIssuerType eNachDCIssuerType[] = EnachDCIssuerType.values();
						for (EnachDCIssuerType key : eNachDCIssuerType) {
							enachDCIssuerTypeJson = new JSONObject();
							enachDCIssuerTypeJson.put("labelKey", key.getCode());
							enachDCIssuerTypeJson.put("value",
									WordUtils.capitalizeFully(EnachDCIssuerType.getIssuerName(key.getCode())));
							enachDCIssuerTypeArray.put(enachDCIssuerTypeJson);
						}
						aaData.put("dcBankList", enachDCIssuerTypeArray.toString());
						JSONObject enachNBIssuerTypeJson;
						JSONArray enachNBIssuerTypeArray = new JSONArray();
						EnachNBIssuerType eNachNBIssuerType[] = EnachNBIssuerType.values();
						for (EnachNBIssuerType key : eNachNBIssuerType) {
							enachNBIssuerTypeJson = new JSONObject();
							enachNBIssuerTypeJson.put("labelKey", key.getCode());
							enachNBIssuerTypeJson.put("value",
									WordUtils.capitalizeFully(EnachNBIssuerType.getIssuerName(key.getCode())));
							enachNBIssuerTypeArray.put(enachNBIssuerTypeJson);
						}
						aaData.put("nbList", enachNBIssuerTypeArray.toString());

					} else {
						aaData.put(FieldType.RESPONSE.getName(), ErrorType.DUPLICATE_ORDER_ID.getResponseMessage());
						aaData.put(FieldType.RETURN_URL.getName(),
								propertiesManager.propertiesMap.get(Constants.ICICI_ENACH_FORM_REPONSE.getValue()));
						aaData.put("MERCHANT_RETURN_URL", fields.get(FieldType.RETURN_URL.getName()));
						logger.info("merchant send a duplicate order id");
					}

				} else {
					logger.info("merchant hash not match");
					StringBuilder hashMessage = new StringBuilder("Merchant hash = ");
					hashMessage.append(merchantHash);
					hashMessage.append(", Calculated Hash = ");
					hashMessage.append(calculatedHash);
					MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
					logger.error(hashMessage.toString());

					aaData.put(FieldType.RESPONSE.getName(), Constants.TRANSACTIONSTATE_N.getValue());
					aaData.put(FieldType.RETURN_URL.getName(),
							propertiesManager.propertiesMap.get(Constants.ICICI_ENACH_FORM_REPONSE.getValue()));
					aaData.put("MERCHANT_RETURN_URL", fields.get(FieldType.RETURN_URL.getName()));
				}

			} else {

				switch (validateFields.get(FieldType.RESPONSE_MESSAGE.getName())) {

				case "Invalid Request ID":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Request ID");
					break;
				case "Invalid End Date":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid End Date");
					break;
				case "Invalid Start Date":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Start Date");
					break;
				case "Invalid Amount":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Amount");
					break;
				case "Invalid Monthly Amount":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Monthly Amount");
					break;
				case "Invalid Frequency":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Frequency");
					break;
				case "Invalid Tenure":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Tenure");
					break;
				case "Invalid Merchant ID":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Merchant ID");
					break;
				case "Invalid Customer Mobile":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Customer Mobile");
					break;
				case "Invalid Customer Email":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Customer Email");
					break;
				default:
					break;
				}
				aaData.put(FieldType.RETURN_URL.getName(),
						propertiesManager.propertiesMap.get(Constants.ICICI_ENACH_REPONSE.getValue()));
				aaData.put("MERCHANT_RETURN_URL", fields.get(FieldType.RETURN_URL.getName()));
			}

		} catch (Exception ex) {
			logger.info("exception caught while mandte registration ", ex);
//			return ERROR;
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

	private Map<String, String> ValidateRequestFields(Fields fields) {

		Map<String, String> validationMap = new HashMap<String, String>();

		if (fields.contains(FieldType.ORDER_ID.getName())
				&& StringUtils.isNotBlank(fields.get(FieldType.ORDER_ID.getName()))) {

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

						if (fields.contains(FieldType.PAY_ID.getName())
								&& StringUtils.isNotBlank(fields.get(FieldType.PAY_ID.getName()))
								&& StringUtils.isNumeric(fields.get(FieldType.PAY_ID.getName()))) {

							if (fields.contains("CUST_MOBILE") && StringUtils.isNotBlank(fields.get("CUST_MOBILE"))
									&& StringUtils.isNumeric(fields.get("CUST_MOBILE"))) {

								if (fields.contains(FieldType.CUST_EMAIL.getName())
										&& StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {

									logger.info("all request fields are valid");
									validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), "success");
									validationMap.put(FieldType.RESPONSE_CODE.getName(), "true");
									return validationMap;

								}
								logger.info("Invalid CUST_EMAIL");
								validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Customer Email");
								validationMap.put(FieldType.RESPONSE_CODE.getName(), "false");
								return validationMap;
							}
							logger.info("Invalid CUST_MOBILE");
							validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Customer Mobile");
							validationMap.put(FieldType.RESPONSE_CODE.getName(), "false");
							return validationMap;
						}
						logger.info("Invalid PAY_ID");
						validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Merchant ID");
						validationMap.put(FieldType.RESPONSE_CODE.getName(), "false");
						return validationMap;
					}
					logger.info("Invalid TENURE");
					validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Tenure");
					validationMap.put(FieldType.RESPONSE_CODE.getName(), "false");
					return validationMap;
				}
				logger.info("Invalid FREQUENCY");
				validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Frequency");
				validationMap.put(FieldType.RESPONSE_CODE.getName(), "false");
				return validationMap;
			}
			logger.info("Invalid MONTHLY_AMOUNT");
			validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Monthly Amount");
			validationMap.put(FieldType.RESPONSE_CODE.getName(), "false");
			return validationMap;
		}
		logger.info("Invalid ORDER_ID");
		validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Request ID");
		validationMap.put(FieldType.RESPONSE_CODE.getName(), "false");
		return validationMap;
	}
}
