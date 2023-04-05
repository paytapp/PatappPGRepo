package com.paymentgateway.pgui.dao;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentgateway.commons.api.BindbControllerServiceProvider;
import com.paymentgateway.commons.api.SmsSender;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.NBToken;
import com.paymentgateway.commons.user.Token;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.user.VpaToken;
import com.paymentgateway.commons.user.WLToken;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CountryCodes;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CustTransactionAuthentication;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MerchantTypeService;
import com.paymentgateway.commons.util.ModeType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PaymentTypeProvider;
import com.paymentgateway.commons.util.PaymentTypeTransactionProvider;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StaticDataProvider;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.SystemConstants;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.UserStatusType;
import com.paymentgateway.oneclick.TokenManager;
import com.paymentgateway.pg.core.pageintegrator.GeneralValidator;
import com.paymentgateway.pg.core.util.CalculateSurchargeAmount;
import com.paymentgateway.pg.core.util.RequestCreator;
import com.paymentgateway.pg.core.util.ResponseCreator;
import com.paymentgateway.pg.core.util.TransactionResponser;
import com.paymentgateway.pg.core.util.UpdateProcessor;
import com.paymentgateway.pgui.action.IndustryId;
import com.paymentgateway.pgui.action.beans.SessionCleaner;
import com.paymentgateway.pgui.action.service.ActionService;
import com.paymentgateway.pgui.action.service.PgActionServiceFactory;
import com.paymentgateway.pgui.action.service.PrepareRequestParemeterService;

@Service
public class RequestAction {

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private ResponseCreator responseCreator;

	@Autowired
	private UpdateProcessor updateProcessor;

	@Autowired
	private UserDao userDao;

	@Autowired
	private TransactionResponser transactionResponser;

	@Autowired
	private RequestCreator requestCreator;

	@Autowired
	private TokenManager tokenManager;

	@Autowired
	private GeneralValidator generalValidator;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private CalculateSurchargeAmount calculateSurchargeAmount;

	@Autowired
	@Qualifier("paymentTypeProvider")
	private PaymentTypeProvider paymentTypeProvider;

	@Autowired
	private PrepareRequestParemeterService prepareRequestParemeterService;

	@Autowired
	private SmsSender smsSender;

	@Autowired
	private MerchantTypeService merchantTypeService;

	@Autowired
	private Fields field;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private BindbControllerServiceProvider bindbControllerServiceProvider;

	@Autowired
	private UserSettingDao userSettingDao;

	@Autowired
	private StaticDataProvider staticDataProvider;
	private Logger logger = LoggerFactory.getLogger(RequestAction.class.getName());
	private static final Random RANDOM = new SecureRandom();
	private static final String pendingTxnStatus = "Sent to Bank-Enrolled";

	private String responseCode;
	private String acquirerFlag;
	private String resellerId;
	private String token;
	private String paymentFlow;
	private Map<String, Object> supportedPaymentTypeMap = new HashMap<String, Object>();
	private Map<String, Object> cardPaymentTypeMap = new HashMap<String, Object>();
	private Map<String, Token> tokenMap = new HashMap<String, Token>();
	private Map<String, VpaToken> vpaTokenMap = new HashMap<String, VpaToken>();
	private Map<String, NBToken> nbTokenMap = new HashMap<String, NBToken>();
	private Map<String, WLToken> wlTokenMap = new HashMap<String, WLToken>();

	@SuppressWarnings({ "static-access" })
	public Map<String, String> paymentPageRequest(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		Map<String, String> responseMap = new HashMap<String, String>();

		try {
			// clean session for new request
			request.getSession().invalidate();

			// create fields for transaction
			ActionService service = PgActionServiceFactory.getActionService();
			Fields fields = service.prepareFields(request.getParameterMap());

			fields.logAllFields("Payment Request received ");

			String referer = request.getHeader("Referer");
			logger.info("Payment request received from URL " + referer);

			if (StringUtils.isNotBlank(referer) && referer.contains("paymentGateway.com")) {
				fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
				fields.put(FieldType.HASH.getName(), "12345678123456781234567812345678");
			}

			if (StringUtils.isBlank(fields.get(FieldType.HASH.getName()))) {
				throw new SystemException(ErrorType.VALIDATION_FAILED, "Invalid " + FieldType.HASH.getName());
			}

			generalValidator.validateHash(fields);
			generalValidator.validateReturnUrl(fields);

			boolean exist = fieldsDao.checkDuplicateOrderId(fields);

			if (exist) {
				User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
				fields.put(FieldType.TXNTYPE.getName(),
						ModeType.getDefaultPurchaseTransaction(user.getModeType()).getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DUPLICATE_ORDER_ID.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE_ORDER_ID.getCode());
				fields.put(FieldType.STATUS.getName(), StatusType.DUPLICATE.getName());
				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
				responseCreator.ResponsePost(fields, response);
				return responseMap;
			}

			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DUPLICATE.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE.getCode());
			fields.put(FieldType.STATUS.getName(), StatusType.DUPLICATE.getName());

			// check parent merchant payid
			User checkParent = null;
			checkParent = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
			if (checkParent != null) {
				if (checkParent.getUserType() == UserType.PARENTMERCHANT) {
					if (checkParent.getUserStatus() != UserStatusType.ACTIVE) {
						fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
						fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.USER_INACTIVE.getResponseMessage());
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.USER_INACTIVE.getCode());
						fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
						responseCreator.ResponsePost(fields, response);
					}
					String childPayId = null;

					// configure router logic for it's child merchant

					childPayId = merchantTypeService.getChildMerchantPayId(fields.get(FieldType.PAY_ID.getName()),
							fields.get(FieldType.CUSTOMER_CATEGORY.getName()));

					if (StringUtils.isNotBlank(childPayId)) {
						fields.put(FieldType.PARENT_PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
						fields.put(FieldType.PAY_ID.getName(), childPayId);
					} else {
						fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
						fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REJECTED_BY_PG.getResponseMessage());
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED_BY_PG.getCode());
						fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
						responseCreator.ResponsePost(fields, response);
					}
				}
			}
			fields.removeExtraFields();
			String subMerchantId = null;
			String superMerchantId = null;
			boolean isSubMerchant = false;

			// Check For Sub Merchant Transactions via invoice:
			if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
				subMerchantId = fields.get(FieldType.SUB_MERCHANT_ID.getName());

				User subMerchant = null;

				if (propertiesManager.propertiesMap.get("useStaticData").equalsIgnoreCase("Y")) {
					subMerchant = staticDataProvider.getUserData(subMerchantId);
				} else {
					subMerchant = userDao.findPayId(subMerchantId);
				}

				if (subMerchant.getUserStatus() != UserStatusType.ACTIVE) {
					fields.put(FieldType.TXNTYPE.getName(),
							ModeType.getDefaultPurchaseTransaction(subMerchant.getModeType()).getName());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.USER_INACTIVE.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.USER_INACTIVE.getCode());
					fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
					fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
					responseCreator.ResponsePost(fields, response);
					return responseMap;
				}
				// Remove for hash Validation
				fields.remove(FieldType.SUB_MERCHANT_ID.getName());
			} else {
				// Check For Sub Merchant Transactions via website:
				User subMerchant = null;

				if (propertiesManager.propertiesMap.get("useStaticData").equalsIgnoreCase("Y")) {
					subMerchant = staticDataProvider.getUserData(fields.get(FieldType.PAY_ID.getName()));
				} else {
					subMerchant = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
				}

				if (null == subMerchant) {

					fields.put(FieldType.TXNTYPE.getName(), "null");
					fields.put(FieldType.RESPONSE_MESSAGE.getName(),
							ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getCode());
					fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
					fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
					responseCreator.InvalidUserResponsePost(fields, response);
					return responseMap;
				}

				if (subMerchant.isSuperMerchant() && StringUtils.isNotBlank(subMerchant.getSuperMerchantId())) {

					fields.put(FieldType.TXNTYPE.getName(),
							ModeType.getDefaultPurchaseTransaction(subMerchant.getModeType()).getName());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REJECTED_BY_PG.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED_BY_PG.getCode());
					fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
					fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
					responseCreator.ResponsePost(fields, response);
					return responseMap;
				}
				User supermerchant = null;
				if (!subMerchant.isSuperMerchant() && StringUtils.isNotBlank(subMerchant.getSuperMerchantId())) {
					superMerchantId = subMerchant.getSuperMerchantId();
					subMerchantId = subMerchant.getPayId();
					isSubMerchant = true;
					supermerchant = userDao.findPayId(superMerchantId);
				}

				if (supermerchant != null && supermerchant.getUserStatus() != UserStatusType.ACTIVE) {
					fields.put(FieldType.TXNTYPE.getName(),
							ModeType.getDefaultPurchaseTransaction(supermerchant.getModeType()).getName());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.USER_INACTIVE.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.USER_INACTIVE.getCode());
					fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
					fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
					responseCreator.ResponsePost(fields, response);
					return responseMap;
				}

				if (subMerchant.getUserStatus() != UserStatusType.ACTIVE) {
					fields.put(FieldType.TXNTYPE.getName(),
							ModeType.getDefaultPurchaseTransaction(subMerchant.getModeType()).getName());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REJECTED_BY_PG.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED_BY_PG.getCode());
					fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
					fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
					responseCreator.ResponsePost(fields, response);
					return responseMap;
				}
			}

			String fieldsAsString = fields.getFieldsAsString();
			request.getSession().setAttribute(Constants.FIELDS.getValue(), fields);

			fields.put(FieldType.INTERNAL_REQUEST_FIELDS.getName(), fieldsAsString);
			logger.info("Raw Request Fields:  " + fields.getFieldsAsString());

			if (StringUtils.isBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
				fields.remove(FieldType.SUB_MERCHANT_ID.getName());
			}

			// Add Sub Merchant Id Again for keeping transaction records for sub
			// merchant
			if (StringUtils.isNotBlank(subMerchantId)) {
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
			}

			// Update PayId to super merchant id and add sub merchant payid as
			// SUB_MERCHANT_ID for record
			if (isSubMerchant) {
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
				fields.put(FieldType.PAY_ID.getName(), superMerchantId);
				request.getSession().setAttribute(FieldType.SUPER_MERCHANT_ID.getName(), superMerchantId);
				responseMap.put("SUPER_MERCHANT_ID",
						(String) request.getSession().getAttribute(FieldType.SUPER_MERCHANT_ID.getName()));
			}

			User user = null;
			if (propertiesManager.propertiesMap.get("useStaticData").equalsIgnoreCase("Y")) {
				user = staticDataProvider.getUserData(fields.get(FieldType.PAY_ID.getName()));
			} else {
				user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
			}

			// Added validation when User does not exist

			if (null == user) {

				fields.put(FieldType.TXNTYPE.getName(), "null");
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getCode());
				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
				responseCreator.InvalidUserResponsePost(fields, response);
//				List<String> fieldTypeList = new ArrayList<String>(fields.getFields().keySet());
//				for (String fieldType : fieldTypeList) {
//					responseMap.put(fieldType, fields.get(fieldType));
//				}
//
//				return responseMap;
			}

			UserSettingData userSettings = userSettingDao.fetchDataUsingPayId(user.getPayId());

			// Return URL whitelisting
			boolean returnUrlWhitelistingFlag = userSettings.isWhiteListReturnUrlFlag();
			if (returnUrlWhitelistingFlag) {
				String whitelistedURL = userSettings.getWhiteListReturnUrl();
				if (!whitelistedURL.equals(fields.get(FieldType.RETURN_URL.getName()))) {
					fields.put(FieldType.TXNTYPE.getName(),
							ModeType.getDefaultPurchaseTransaction(user.getModeType()).getName());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REJECTED_BY_PG.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED_BY_PG.getCode());
					fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
					fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
					responseCreator.ResponsePost(fields, response);
//					return Action.NONE;
				}
			}

			if (StringUtils.isNotBlank(user.getResellerId())) {
				resellerId = user.getResellerId();
			}

			if (fields.contains(FieldType.EPOS_PAYMENT_OPTION.getName())
					&& StringUtils.isNotBlank(fields.get(FieldType.EPOS_PAYMENT_OPTION.getName()))) {
				fields.put(FieldType.EPOS_MERCHANT.getName(), String.valueOf(true));
			}
			String Industry = user.getIndustryCategory();
			List<String> idName = new LinkedList<String>();

			if (StringUtils.isNotBlank(Industry)) {
				idName.addAll(IndustryId.getIndustryId(Industry));
				for (String id : idName) {
					request.getSession().setAttribute(FieldType.INDUSTRY_ID.getName(), id);

				}
			} else {
				request.getSession().setAttribute(FieldType.INDUSTRY_ID.getName(), "NA");
			}

			fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(),
					ModeType.getDefaultPurchaseTransaction(user.getModeType()).getName());

			request.getSession().setAttribute((FieldType.INTERNAL_ORIG_TXN_TYPE.getName()),
					ModeType.getDefaultPurchaseTransaction(user.getModeType()).getName());
			request.getSession().setAttribute((FieldType.INTERNAL_SHOPIFY_YN.getName()),
					fields.get(FieldType.INTERNAL_SHOPIFY_YN.getName()));
			request.getSession().setAttribute((FieldType.CANCEL_URL.getName()),
					fields.get(FieldType.CANCEL_URL.getName()));
			request.getSession().setAttribute(Constants.CUSTOM_TOKEN.getValue(), generateGUID());
			request.getSession().setAttribute((FieldType.RETURN_URL.getName()),
					fields.get(FieldType.RETURN_URL.getName()));
			request.getSession().setAttribute((FieldType.CURRENCY_CODE.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName()));
			request.getSession().setAttribute((FieldType.SURCHARGE_FLAG.getName()),
					((userSettings.isSurchargeFlag()) ? "Y" : "N"));
			request.getSession().setAttribute(FieldType.MERCHANT_PAYMENT_TYPE.getName(),
					fields.get(FieldType.MERCHANT_PAYMENT_TYPE.getName()));

			if (StringUtils.isNotBlank(request.getHeader("X-Forwarded-For"))) {
				fields.put((FieldType.INTERNAL_CUST_IP.getName()), request.getHeader("X-Forwarded-For").split(",")[0]);
				request.getSession().setAttribute((FieldType.INTERNAL_CUST_IP.getName()),
						request.getHeader("X-Forwarded-For").split(",")[0]);
			}

			request.getSession().setAttribute((FieldType.INTERNAL_HEADER_ACEEPT.getName()),
					request.getHeader("Accept"));
			request.getSession().setAttribute((FieldType.INTERNAL_HEADER_USER_AGENT.getName()),
					request.getHeader("User-Agent"));
			fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));

			String countryCode = "NotAvailable";
			if (request.getHeader("CloudFront-Viewer-Country") != null) {
				countryCode = request.getHeader("CloudFront-Viewer-Country");
			}
			fields.put((FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()), CountryCodes.getCountryName(countryCode));
			request.getSession().setAttribute(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName(),
					CountryCodes.getCountryName(countryCode));

			if (countryCode.equals(CrmFieldConstants.INDIA_REGION_CODE.getValue())) {
				fields.put((FieldType.INTERNAL_TXN_AUTHENTICATION.getName()),
						CustTransactionAuthentication.SUCCESS.getAuthenticationName());
			} else {
				fields.put((FieldType.INTERNAL_TXN_AUTHENTICATION.getName()),
						CustTransactionAuthentication.PENDING.getAuthenticationName());
			}

			fields = requestCreator(fields, request);
			responseCode = fields.get(FieldType.RESPONSE_CODE.getName());

			if (StringUtils.isEmpty(responseCode)) {
				// return Action.ERROR;
				logger.info("response code is missing redirect to error page");
				String path = request.getContextPath();
				logger.info(path);
				if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
					String resultPath = request.getScheme() + "://" + request.getHeader("Host") + "/pgui/jsp/error";
					response.sendRedirect(resultPath);
				}
				response.sendRedirect("error");
			} else if (responseCode.equals(ErrorType.SUCCESS.getCode())) {

				// These fields are being used at PaymentPage
				request.getSession().setAttribute((FieldType.PAY_ID.getName()), fields.get(FieldType.PAY_ID.getName()));
				request.getSession().setAttribute(FieldType.ORDER_ID.getName(),
						fields.get(FieldType.ORDER_ID.getName()));
				request.getSession().setAttribute(FieldType.CUST_NAME.getName(),
						fields.get(FieldType.CUST_NAME.getName()));
				request.getSession().setAttribute(FieldType.CURRENCY_CODE.getName(),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				request.getSession().setAttribute(FieldType.AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
				request.getSession().setAttribute(FieldType.CUST_PHONE.getName(),
						fields.get(FieldType.CUST_PHONE.getName()));

				// get String of supported payment types
				PaymentTypeTransactionProvider paymentTypeTransactionProvider = paymentTypeProvider
						.setSupportedPaymentOptions((user.getPayId()));
				setSupportedPaymentTypeMap(paymentTypeTransactionProvider.getSupportedPaymentTypeMap());
				setCardPaymentTypeMap(paymentTypeTransactionProvider.getSupportedCardTypeMap());

				List<ChargingDetails> chargesList = paymentTypeTransactionProvider.getChargingDetailsList();
				for (ChargingDetails chargingDetail : chargesList) {
					if (chargingDetail.getPaymentsRegion().equals(AccountCurrencyRegion.INTERNATIONAL)) {
						request.getSession().setAttribute(Constants.REGION_TYPE.getValue(),
								AccountCurrencyRegion.INTERNATIONAL);
						break;
					}
				}
				request.getSession().setAttribute(Constants.PAYMENT_TYPE_MOP.getValue(),
						paymentTypeTransactionProvider.getSupportedPaymentTypeMap());
				request.getSession().setAttribute(Constants.CARD_PAYMENT_TYPE_MOP.getValue(), getCardPaymentTypeMap());
				if (StringUtils.isNotBlank(fields.get(userSettings.getCardSaveParam()))) {
					request.getSession().setAttribute(Constants.EXPRESS_PAY_FLAG.getValue(),
							userSettings.isExpressPayFlag());
				} else {
					request.getSession().setAttribute(Constants.EXPRESS_PAY_FLAG.getValue(), false);
				}

				if (StringUtils.isNotBlank(fields.get(userSettings.getVpaSaveParam()))) {
					request.getSession().setAttribute(Constants.SAVE_VPA_FLAG.getValue(), userSettings.isSaveVPAFlag());
				} else {
					request.getSession().setAttribute(Constants.SAVE_VPA_FLAG.getValue(), false);
				}

				if (StringUtils.isNotBlank(fields.get(userSettings.getNbSaveParam()))) {
					request.getSession().setAttribute(Constants.SAVE_NB_FLAG.getValue(), userSettings.isSaveNBFlag());
				} else {
					request.getSession().setAttribute(Constants.SAVE_NB_FLAG.getValue(), false);
				}

				if (StringUtils.isNotBlank(fields.get(userSettings.getWlSaveParam()))) {
					request.getSession().setAttribute(Constants.SAVE_WL_FLAG.getValue(), userSettings.isSaveWLFlag());
				} else {
					request.getSession().setAttribute(Constants.SAVE_WL_FLAG.getValue(), false);
				}

				request.getSession().setAttribute(Constants.IFRAME_PAYMENT_PAGE.getValue(),
						userSettings.isIframePaymentFlag());
				request.getSession().setAttribute(FieldType.INTERNAL_ORIG_TXN_ID.getName(),
						fields.get(FieldType.TXN_ID.getName()));
				request.getSession().setAttribute(FieldType.OID.getName(), fields.get(FieldType.TXN_ID.getName()));
				if (StringUtils.isBlank(userSettings.getCardSaveParam())) {
					request.getSession().setAttribute(Constants.TOKEN.getValue(), "NA");
				} else if (StringUtils.isBlank(fields.get(userSettings.getCardSaveParam()))) {
					request.getSession().setAttribute(Constants.TOKEN.getValue(), "NA");
				} else {
					if (!userSettings.isExpressPayFlag()) {
						request.getSession().setAttribute(Constants.TOKEN.getValue(), "NA");
					} else {
						tokenMap = tokenManager.getAll(fields, user);
						if (tokenMap.isEmpty()) {
							request.getSession().setAttribute(Constants.TOKEN.getValue(), "NA");
						} else {
							request.getSession().setAttribute(Constants.TOKEN.getValue(), tokenMap);
						}
					}
				}
				if (StringUtils.isBlank(userSettings.getVpaSaveParam())) {
					request.getSession().setAttribute(Constants.VPA_TOKEN.getValue(), "NA");
				} else if (StringUtils.isBlank(fields.get(userSettings.getVpaSaveParam()))) {
					request.getSession().setAttribute(Constants.VPA_TOKEN.getValue(), "NA");
				} else {
					if (!userSettings.isSaveVPAFlag()) {
						request.getSession().setAttribute(Constants.VPA_TOKEN.getValue(), "NA");
					} else {
						vpaTokenMap = tokenManager.getAllVpa(fields, user);
						if (vpaTokenMap.isEmpty()) {
							request.getSession().setAttribute(Constants.VPA_TOKEN.getValue(), "NA");
						} else {
							request.getSession().setAttribute(Constants.VPA_TOKEN.getValue(), vpaTokenMap);
						}
					}
				}
				if (StringUtils.isBlank(userSettings.getNbSaveParam())) {
					request.getSession().setAttribute(Constants.NB_TOKEN.getValue(), "NA");
				} else if (StringUtils.isBlank(fields.get(userSettings.getNbSaveParam()))) {
					request.getSession().setAttribute(Constants.NB_TOKEN.getValue(), "NA");
				} else {
					if (!userSettings.isSaveNBFlag()) {
						request.getSession().setAttribute(Constants.NB_TOKEN.getValue(), "NA");
					} else {
						nbTokenMap = tokenManager.getAllBank(fields, user);
						if (nbTokenMap.isEmpty()) {
							request.getSession().setAttribute(Constants.NB_TOKEN.getValue(), "NA");
						} else {
							request.getSession().setAttribute(Constants.NB_TOKEN.getValue(), nbTokenMap);
						}
					}
				}
				if (StringUtils.isBlank(userSettings.getWlSaveParam())) {
					request.getSession().setAttribute(Constants.WL_TOKEN.getValue(), "NA");
				} else if (StringUtils.isBlank(fields.get(userSettings.getWlSaveParam()))) {
					request.getSession().setAttribute(Constants.WL_TOKEN.getValue(), "NA");
				} else {
					if (!userSettings.isSaveWLFlag()) {
						request.getSession().setAttribute(Constants.WL_TOKEN.getValue(), "NA");
					} else {
						wlTokenMap = tokenManager.getAllWallet(fields, user);
						if (wlTokenMap.isEmpty()) {
							request.getSession().setAttribute(Constants.WL_TOKEN.getValue(), "NA");
						} else {
							request.getSession().setAttribute(Constants.WL_TOKEN.getValue(), wlTokenMap);
						}
					}
				}
				if (userSettings.isCheckOutJsFlag() == true) {
					request.getSession().setAttribute(FieldType.CHECKOUT_JS_FLAG.getName(), "Y");
				} else {
					request.getSession().setAttribute(FieldType.CHECKOUT_JS_FLAG.getName(), "N");
				}
				String surchargeFlag = request.getSession().getAttribute(FieldType.SURCHARGE_FLAG.getName()).toString();
				BigDecimal adTransSurcharge = BigDecimal.ZERO;
				BigDecimal surchargeADAmount = BigDecimal.ZERO;
				BigDecimal ccConsumerTransSurcharge = BigDecimal.ZERO;
				BigDecimal surchargeCCConsumerAmount = BigDecimal.ZERO;
				BigDecimal ccCommercialTransSurcharge = BigDecimal.ZERO;
				BigDecimal surchargeCCCommercialAmount = BigDecimal.ZERO;
				BigDecimal ccPremiumTransSurcharge = BigDecimal.ZERO;
				BigDecimal surchargeCCPremiumAmount = BigDecimal.ZERO;
				BigDecimal ccAmexTransSurcharge = BigDecimal.ZERO;
				BigDecimal surchargeCCAmexAmount = BigDecimal.ZERO;

				BigDecimal dcVisaTransSurcharge = BigDecimal.ZERO;
				BigDecimal surchargeDCVisaAmount = BigDecimal.ZERO;
				BigDecimal dcMasterTransSurcharge = BigDecimal.ZERO;
				BigDecimal surchargeDCMasterAmount = BigDecimal.ZERO;
				BigDecimal dcRupayTransSurcharge = BigDecimal.ZERO;
				BigDecimal surchargeDCRupayAmount = BigDecimal.ZERO;

				// BigDecimal dcTransSurcharge = BigDecimal.ZERO;
				// BigDecimal surchargeDCAmount = BigDecimal.ZERO;
				BigDecimal wlTransSurcharge = BigDecimal.ZERO;
				BigDecimal surchargeWLAmount = BigDecimal.ZERO;
				BigDecimal upTransSurcharge = BigDecimal.ZERO;
				BigDecimal surchargeUPAmount = BigDecimal.ZERO;
				BigDecimal nbTransSurcharge = BigDecimal.ZERO;
				BigDecimal surchargeNBAmount = BigDecimal.ZERO;
				BigDecimal pcTransSurcharge = BigDecimal.ZERO;
				BigDecimal surchargePCAmount = BigDecimal.ZERO;
				BigDecimal cdTransSurcharge = BigDecimal.ZERO;
				BigDecimal surchargeCDAmount = BigDecimal.ZERO;
				BigDecimal apTransSurcharge = BigDecimal.ZERO;
				BigDecimal surchargeAPAmount = BigDecimal.ZERO;
				BigDecimal crTransSurcharge = BigDecimal.ZERO;
				BigDecimal surchargeCRAmount = BigDecimal.ZERO;
				BigDecimal inTransSurcharge = BigDecimal.ZERO;
				BigDecimal surchargeINAmount = BigDecimal.ZERO;
				BigDecimal emCCTransSurcharge = BigDecimal.ZERO;
				BigDecimal surchargeEMCCAmount = BigDecimal.ZERO;
				BigDecimal mqrTransSurcharge = BigDecimal.ZERO;
				BigDecimal emDCTransSurcharge = BigDecimal.ZERO;
				BigDecimal surchargeEMDCAmount = BigDecimal.ZERO;
				BigDecimal surchargeMQRAmount = BigDecimal.ZERO;

				if (surchargeFlag.equals(Constants.Y_FLAG.getValue())) {
					fields.put(FieldType.SURCHARGE_FLAG.getName(),
							request.getSession().getAttribute(FieldType.SURCHARGE_FLAG.getName()).toString());
					String payId = request.getSession().getAttribute(FieldType.PAY_ID.getName()).toString();

					String convertedamount = request.getSession().getAttribute(FieldType.AMOUNT.getName()).toString();
					String currency = request.getSession().getAttribute(FieldType.CURRENCY_CODE.getName()).toString();
					String amount = Amount.toDecimal(convertedamount, currency);

					BigDecimal[] surCCConsumerAmount = null;
					BigDecimal[] surCCCommercialAmount = null;
					BigDecimal[] surCCPremiumAmount = null;
					BigDecimal[] surCCAmexAmount = null;
					BigDecimal[] surDCVisaAmount = null;
					BigDecimal[] surDCMasterAmount = null;
					BigDecimal[] surDCRupayAmount = null;
					BigDecimal[] surWLAmount = null;
					BigDecimal[] surUPAmount = null;
					BigDecimal[] surNBAmount = null;
					BigDecimal[] surPCAmount = null;
					BigDecimal[] surCDAmount = null;
					BigDecimal[] surAPAmount = null;
					BigDecimal[] surCRAmount = null;
					BigDecimal[] surINAmount = null;
					BigDecimal[] surEMCCAmount = null;
					BigDecimal[] surEMDCAmount = null;
					BigDecimal[] surMQRAmount = null;

					String slabId = "";
					BigDecimal txnAmount = new BigDecimal(Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
							fields.get(FieldType.CURRENCY_CODE.getName())));
					BigDecimal minAmountSlab1 = new BigDecimal(
							PropertiesManager.propertiesMap.get("LimitSlab1MinAmount"));
					BigDecimal maxAmountSlab1 = new BigDecimal(
							PropertiesManager.propertiesMap.get("LimitSlab1MaxAmount"));
					BigDecimal minAmountSlab2 = new BigDecimal(
							PropertiesManager.propertiesMap.get("LimitSlab2MinAmount"));
					BigDecimal maxAmountSlab2 = new BigDecimal(
							PropertiesManager.propertiesMap.get("LimitSlab2MaxAmount"));
					int minSlab1CompareResult = txnAmount.compareTo(minAmountSlab1);
					int maxSlab1CompareResult = txnAmount.compareTo(maxAmountSlab1);
					int minSlab2CompareResult = txnAmount.compareTo(minAmountSlab2);
					int maxSlab2CompareResult = txnAmount.compareTo(maxAmountSlab2);

					if (((minSlab1CompareResult > 0) || (minSlab1CompareResult == 0))
							&& ((maxSlab1CompareResult < 0) || (maxSlab1CompareResult == 0))) {
						slabId = "01";
					} else if (((minSlab2CompareResult > 0) || (minSlab2CompareResult == 0))
							&& ((maxSlab2CompareResult < 0) || (maxSlab2CompareResult == 0))) {
						slabId = "02";
					} else {
						slabId = "03";
					}

					String paymentTypeMops = request.getSession().getAttribute(Constants.PAYMENT_TYPE_MOP.getValue())
							.toString();
					Object regionType = request.getSession().getAttribute(Constants.REGION_TYPE.getValue());
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_CC.getValue())) {
						surCCConsumerAmount = calculateSurchargeAmount.fetchCCConsumerSurchargeDetails(amount, payId,
								AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
						ccConsumerTransSurcharge = surCCConsumerAmount[0];
						surchargeCCConsumerAmount = surCCConsumerAmount[1];

						surCCCommercialAmount = calculateSurchargeAmount.fetchCCCommercialSurchargeDetails(amount,
								payId, AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
						ccCommercialTransSurcharge = surCCCommercialAmount[0];
						surchargeCCCommercialAmount = surCCCommercialAmount[1];

						surCCPremiumAmount = calculateSurchargeAmount.fetchCCPremiumSurchargeDetails(amount, payId,
								AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
						ccPremiumTransSurcharge = surCCPremiumAmount[0];
						surchargeCCPremiumAmount = surCCPremiumAmount[1];
						surCCAmexAmount = calculateSurchargeAmount.fetchCCAmexSurchargeDetails(amount, payId,
								AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
						ccAmexTransSurcharge = surCCAmexAmount[0];
						surchargeCCAmexAmount = surCCAmexAmount[1];
					}
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_DC.getValue())) {
						surDCVisaAmount = calculateSurchargeAmount.fetchDCVisaSurchargeDetails(amount, payId,
								AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
						dcVisaTransSurcharge = surDCVisaAmount[0];
						surchargeDCVisaAmount = surDCVisaAmount[1];

						surDCMasterAmount = calculateSurchargeAmount.fetchDCMasterSurchargeDetails(amount, payId,
								AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
						dcMasterTransSurcharge = surDCMasterAmount[0];
						surchargeDCMasterAmount = surDCMasterAmount[1];

						surDCRupayAmount = calculateSurchargeAmount.fetchDCRupaySurchargeDetails(amount, payId,
								AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
						dcRupayTransSurcharge = surDCRupayAmount[0];
						surchargeDCRupayAmount = surDCRupayAmount[1];
					}
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_WL.getValue())) {
						surWLAmount = calculateSurchargeAmount.fetchWLSurchargeDetails(amount, payId,
								AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
						wlTransSurcharge = surWLAmount[0];
						surchargeWLAmount = surWLAmount[1];
					}
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_UP.getValue())) {
						surUPAmount = calculateSurchargeAmount.fetchUPSurchargeDetails(amount, payId,
								AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
						upTransSurcharge = surUPAmount[0];
						surchargeUPAmount = surUPAmount[1];
					}
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_MQR.getValue())) {
						surMQRAmount = calculateSurchargeAmount.fetchSurchargeDetails(amount, payId,
								AccountCurrencyRegion.DOMESTIC, slabId, resellerId, PaymentType.MQR,
								CardHolderType.CONSUMER, null);
						mqrTransSurcharge = surUPAmount[0];
						surchargeMQRAmount = surUPAmount[1];
					}
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_NB.getValue())) {
						surNBAmount = calculateSurchargeAmount.fetchNBSurchargeDetails(amount, payId,
								AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
						nbTransSurcharge = surNBAmount[0];
						surchargeNBAmount = surNBAmount[1];
					}
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_PC.getValue())) {
						surPCAmount = calculateSurchargeAmount.fetchPCSurchargeDetails(amount, payId,
								AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
						pcTransSurcharge = surPCAmount[0];
						surchargePCAmount = surPCAmount[1];
					}
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_CD.getValue())) {
						surCDAmount = calculateSurchargeAmount.fetchCDSurchargeDetails(amount, payId,
								AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
						cdTransSurcharge = surCDAmount[0];
						surchargeCDAmount = surCDAmount[1];
					}
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_AP.getValue())) {
						surAPAmount = calculateSurchargeAmount.fetchAPSurchargeDetails(amount, payId,
								AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
						apTransSurcharge = surAPAmount[0];
						surchargeAPAmount = surAPAmount[1];
					}
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_CRYPTO.getValue())) {
						surCRAmount = calculateSurchargeAmount.fetchCRSurchargeDetails(amount, payId,
								AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
						crTransSurcharge = surCRAmount[0];
						surchargeCRAmount = surCRAmount[1];
					}
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_EM_CC.getValue())) {
						surEMCCAmount = calculateSurchargeAmount.fetchEMCCSurchargeDetails(amount, payId,
								AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
						emCCTransSurcharge = surEMCCAmount[0];
						surchargeEMCCAmount = surEMCCAmount[1];
					}
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_EM_DC.getValue())) {
						surEMDCAmount = calculateSurchargeAmount.fetchEMDCSurchargeDetails(amount, payId,
								AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
						emDCTransSurcharge = surEMDCAmount[0];
						surchargeEMDCAmount = surEMDCAmount[1];
					}
					if (null != regionType) {
						if (regionType.toString().contains(Constants.PAYMENT_TYPE_IN.getValue())) {
							surINAmount = calculateSurchargeAmount.fetchCCCommercialSurchargeDetails(amount, payId,
									AccountCurrencyRegion.INTERNATIONAL, slabId, resellerId);
							inTransSurcharge = surINAmount[0];
							surchargeINAmount = surINAmount[1];
						}
					}

					request.getSession().setAttribute(FieldType.CC_CONSUMER_TOTAL_AMOUNT.getName(),
							surchargeCCConsumerAmount);
					request.getSession().setAttribute(FieldType.CC_COMMERCIAL_TOTAL_AMOUNT.getName(),
							surchargeCCCommercialAmount);
					request.getSession().setAttribute(FieldType.CC_PREMIUM_TOTAL_AMOUNT.getName(),
							surchargeCCPremiumAmount);
					request.getSession().setAttribute(FieldType.CC_AMEX_TOTAL_AMOUNT.getName(), surchargeCCAmexAmount);
					request.getSession().setAttribute(FieldType.DC_VISA_TOTAL_AMOUNT.getName(), surchargeDCVisaAmount);
					request.getSession().setAttribute(FieldType.DC_MASTERCARD_TOTAL_AMOUNT.getName(),
							surchargeDCMasterAmount);
					request.getSession().setAttribute(FieldType.DC_RUPAY_TOTAL_AMOUNT.getName(),
							surchargeDCRupayAmount);
					request.getSession().setAttribute(FieldType.NB_TOTAL_AMOUNT.getName(), surchargeNBAmount);
					request.getSession().setAttribute(FieldType.UP_TOTAL_AMOUNT.getName(), surchargeUPAmount);
					request.getSession().setAttribute(FieldType.MQR_TOTAL_AMOUNT.getName(), surchargeMQRAmount);
					request.getSession().setAttribute(FieldType.AD_TOTAL_AMOUNT.getName(), surchargeADAmount);
					request.getSession().setAttribute(FieldType.WL_TOTAL_AMOUNT.getName(), surchargeWLAmount);
					request.getSession().setAttribute(FieldType.PC_TOTAL_AMOUNT.getName(), surchargePCAmount);
					request.getSession().setAttribute(FieldType.CD_TOTAL_AMOUNT.getName(), surchargeCDAmount);
					request.getSession().setAttribute(FieldType.AP_TOTAL_AMOUNT.getName(), surchargeAPAmount);
					request.getSession().setAttribute(FieldType.CR_TOTAL_AMOUNT.getName(), surchargeCRAmount);
					request.getSession().setAttribute(FieldType.IN_TOTAL_AMOUNT.getName(), surchargeINAmount);
					request.getSession().setAttribute(FieldType.EMI_CC_TOTAL_AMOUNT.getName(), surchargeEMCCAmount);
					request.getSession().setAttribute(FieldType.EMI_DC_TOTAL_AMOUNT.getName(), surchargeEMDCAmount);

					request.getSession().setAttribute(FieldType.CC_CONSUMER_SURCHARGE.getName(),
							ccConsumerTransSurcharge);
					request.getSession().setAttribute(FieldType.CC_COMMERCIAL_SURCHARGE.getName(),
							ccCommercialTransSurcharge);
					request.getSession().setAttribute(FieldType.CC_PREMIUM_SURCHARGE.getName(),
							ccPremiumTransSurcharge);
					request.getSession().setAttribute(FieldType.CC_AMEX_SURCHARGE.getName(), ccAmexTransSurcharge);
					request.getSession().setAttribute(FieldType.DC_VISA_SURCHARGE.getName(), dcVisaTransSurcharge);
					request.getSession().setAttribute(FieldType.DC_MASTERCARD_SURCHARGE.getName(),
							dcMasterTransSurcharge);
					request.getSession().setAttribute(FieldType.DC_RUPAY_SURCHARGE.getName(), dcRupayTransSurcharge);
					request.getSession().setAttribute(FieldType.NB_SURCHARGE.getName(), nbTransSurcharge);
					request.getSession().setAttribute(FieldType.UP_SURCHARGE.getName(), upTransSurcharge);
					request.getSession().setAttribute(FieldType.AD_SURCHARGE.getName(), adTransSurcharge);
					request.getSession().setAttribute(FieldType.WL_SURCHARGE.getName(), wlTransSurcharge);
					request.getSession().setAttribute(FieldType.PC_SURCHARGE.getName(), pcTransSurcharge);
					request.getSession().setAttribute(FieldType.CD_SURCHARGE.getName(), cdTransSurcharge);
					request.getSession().setAttribute(FieldType.AP_SURCHARGE.getName(), apTransSurcharge);
					request.getSession().setAttribute(FieldType.CR_SURCHARGE.getName(), crTransSurcharge);
					request.getSession().setAttribute(FieldType.IN_SURCHARGE.getName(), inTransSurcharge);
					request.getSession().setAttribute(FieldType.EMI_CC_SURCHARGE.getName(), emCCTransSurcharge);
					request.getSession().setAttribute(FieldType.EMI_DC_SURCHARGE.getName(), emDCTransSurcharge);
					request.getSession().setAttribute(FieldType.MQR_SURCHARGE.getName(), mqrTransSurcharge);

					JSONObject requestParameterJson = prepareRequestParemeterService.prepareRequestParameter(fields,
							user, request);
					if (requestParameterJson.length() > 0) {
						request.getSession().setAttribute(FieldType.SUPPORTED_PAYMENT_TYPE.getName(),
								requestParameterJson.toString());
						responseMap.put("suportedPaymentTypeMap", requestParameterJson.toString());

						if (request.getHeader("User-Agent").contains("Mobile")
								|| request.getHeader("User-Agent").contains("iphone")
								|| (request.getHeader("CloudFront-Is-Mobile-Viewer") != null
										&& request.getHeader("CloudFront-Is-Mobile-Viewer").equalsIgnoreCase("true"))
								|| (request.getHeader("CloudFront-Is-Tablet-Viewer") != null
										&& request.getHeader("CloudFront-Is-Tablet-Viewer").equalsIgnoreCase("true"))) {
							logger.info("Opening Mobile Payment page");
						} else {
							logger.info("Opening web Payment page");
						}
					} else {
						fields.put(FieldType.AMOUNT.getName(),
								(String) request.getSession().getAttribute(FieldType.AMOUNT.getName()));
						fields.put(FieldType.TXNTYPE.getName(),
								ModeType.getDefaultPurchaseTransaction(user.getModeType()).getName());
						fields.put(FieldType.RESPONSE_MESSAGE.getName(),
								ErrorType.PAYMENT_OPTIONS_NOT_CONFIGURED.getResponseMessage());
						fields.put(FieldType.RESPONSE_CODE.getName(),
								ErrorType.PAYMENT_OPTIONS_NOT_CONFIGURED.getCode());
						fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
						fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
						responseCreator.create(fields);
						responseCreator.ResponsePost(fields, response);
					}

				} else {

					String convertedamount = request.getSession().getAttribute(FieldType.AMOUNT.getName()).toString();
					String currency = request.getSession().getAttribute(FieldType.CURRENCY_CODE.getName()).toString();
					String amount = Amount.toDecimal(convertedamount, currency);
					BigDecimal bigDecimalAmt = new BigDecimal(amount);
					BigDecimal[] surCCConsumerAmount = null;
					BigDecimal[] surCCCommercialAmount = null;
					BigDecimal[] surCCPremiumAmount = null;

					String paymentTypeMops = request.getSession().getAttribute(Constants.PAYMENT_TYPE_MOP.getValue())
							.toString();
					Object regionType = request.getSession().getAttribute(Constants.REGION_TYPE.getValue());
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_AD.getValue())) {
						adTransSurcharge = BigDecimal.ZERO;
						surchargeADAmount = bigDecimalAmt;
					}

					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_DC.getValue())) {
						dcVisaTransSurcharge = BigDecimal.ZERO;
						surchargeDCVisaAmount = bigDecimalAmt;
					}
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_WL.getValue())) {
						wlTransSurcharge = BigDecimal.ZERO;
						surchargeWLAmount = bigDecimalAmt;
					}
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_UP.getValue())) {
						upTransSurcharge = BigDecimal.ZERO;
						surchargeUPAmount = bigDecimalAmt;
					}
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_MQR.getValue())) {
						mqrTransSurcharge = BigDecimal.ZERO;
						surchargeMQRAmount = bigDecimalAmt;
					}
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_NB.getValue())) {
						nbTransSurcharge = BigDecimal.ZERO;
						surchargeNBAmount = bigDecimalAmt;
					}
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_PC.getValue())) {
						pcTransSurcharge = BigDecimal.ZERO;
						surchargePCAmount = bigDecimalAmt;
					}
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_CD.getValue())) {
						cdTransSurcharge = BigDecimal.ZERO;
						surchargeCDAmount = bigDecimalAmt;
					}
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_AP.getValue())) {
						apTransSurcharge = BigDecimal.ZERO;
						surchargeAPAmount = bigDecimalAmt;
					}
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_CRYPTO.getValue())) {
						crTransSurcharge = BigDecimal.ZERO;
						surchargeCRAmount = bigDecimalAmt;
					}
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_EM_CC.getValue())) {
						emCCTransSurcharge = BigDecimal.ZERO;
						surchargeEMCCAmount = bigDecimalAmt;
					}
					if (paymentTypeMops.contains(Constants.PAYMENT_TYPE_EM_DC.getValue())) {
						emDCTransSurcharge = BigDecimal.ZERO;
						surchargeEMDCAmount = bigDecimalAmt;
					}

					if (null != regionType) {
						if (regionType.toString().contains(Constants.PAYMENT_TYPE_IN.getValue())) {
							inTransSurcharge = BigDecimal.ZERO;
							surchargeINAmount = bigDecimalAmt;
						}
					}
					request.getSession().setAttribute(FieldType.CC_CONSUMER_TOTAL_AMOUNT.getName(),
							surchargeCCConsumerAmount);
					request.getSession().setAttribute(FieldType.CC_COMMERCIAL_TOTAL_AMOUNT.getName(),
							surchargeCCCommercialAmount);
					request.getSession().setAttribute(FieldType.CC_PREMIUM_TOTAL_AMOUNT.getName(),
							surchargeCCPremiumAmount);
					request.getSession().setAttribute(FieldType.CC_AMEX_TOTAL_AMOUNT.getName(), surchargeCCAmexAmount);
					request.getSession().setAttribute(FieldType.DC_VISA_TOTAL_AMOUNT.getName(), surchargeDCVisaAmount);
					request.getSession().setAttribute(FieldType.NB_TOTAL_AMOUNT.getName(), surchargeNBAmount);
					request.getSession().setAttribute(FieldType.UP_TOTAL_AMOUNT.getName(), surchargeUPAmount);
					request.getSession().setAttribute(FieldType.AD_TOTAL_AMOUNT.getName(), surchargeADAmount);
					request.getSession().setAttribute(FieldType.WL_TOTAL_AMOUNT.getName(), surchargeWLAmount);
					request.getSession().setAttribute(FieldType.PC_TOTAL_AMOUNT.getName(), surchargePCAmount);
					request.getSession().setAttribute(FieldType.CD_TOTAL_AMOUNT.getName(), surchargeCDAmount);
					request.getSession().setAttribute(FieldType.AP_TOTAL_AMOUNT.getName(), surchargeAPAmount);
					request.getSession().setAttribute(FieldType.CR_TOTAL_AMOUNT.getName(), surchargeCRAmount);
					request.getSession().setAttribute(FieldType.IN_TOTAL_AMOUNT.getName(), surchargeINAmount);
					request.getSession().setAttribute(FieldType.EMI_CC_TOTAL_AMOUNT.getName(), surchargeEMCCAmount);
					request.getSession().setAttribute(FieldType.EMI_DC_TOTAL_AMOUNT.getName(), surchargeEMDCAmount);
					request.getSession().setAttribute(FieldType.MQR_TOTAL_AMOUNT.getName(), surchargeMQRAmount);

					request.getSession().setAttribute(FieldType.CC_CONSUMER_SURCHARGE.getName(),
							ccConsumerTransSurcharge);
					request.getSession().setAttribute(FieldType.CC_COMMERCIAL_SURCHARGE.getName(),
							ccCommercialTransSurcharge);
					request.getSession().setAttribute(FieldType.CC_PREMIUM_SURCHARGE.getName(),
							ccPremiumTransSurcharge);
					request.getSession().setAttribute(FieldType.CC_AMEX_SURCHARGE.getName(), ccAmexTransSurcharge);
					request.getSession().setAttribute(FieldType.DC_VISA_SURCHARGE.getName(), dcVisaTransSurcharge);
					request.getSession().setAttribute(FieldType.NB_SURCHARGE.getName(), nbTransSurcharge);
					request.getSession().setAttribute(FieldType.UP_SURCHARGE.getName(), upTransSurcharge);
					request.getSession().setAttribute(FieldType.MQR_SURCHARGE.getName(), mqrTransSurcharge);
					request.getSession().setAttribute(FieldType.AP_SURCHARGE.getName(), apTransSurcharge);
					request.getSession().setAttribute(FieldType.AD_SURCHARGE.getName(), adTransSurcharge);
					request.getSession().setAttribute(FieldType.WL_SURCHARGE.getName(), wlTransSurcharge);
					request.getSession().setAttribute(FieldType.PC_SURCHARGE.getName(), pcTransSurcharge);
					request.getSession().setAttribute(FieldType.CD_SURCHARGE.getName(), cdTransSurcharge);
					request.getSession().setAttribute(FieldType.AP_SURCHARGE.getName(), apTransSurcharge);
					request.getSession().setAttribute(FieldType.CR_SURCHARGE.getName(), crTransSurcharge);
					request.getSession().setAttribute(FieldType.IN_SURCHARGE.getName(), inTransSurcharge);
					request.getSession().setAttribute(FieldType.EMI_CC_SURCHARGE.getName(), emCCTransSurcharge);
					request.getSession().setAttribute(FieldType.EMI_DC_SURCHARGE.getName(), emDCTransSurcharge);

					JSONObject requestParameterJson = prepareRequestParemeterService.prepareRequestParameter(fields,
							user, request);
					if (requestParameterJson.length() > 0) {

						request.getSession().setAttribute(FieldType.SUPPORTED_PAYMENT_TYPE.getName(),
								requestParameterJson.toString());
						responseMap.put("suportedPaymentTypeMap", requestParameterJson.toString());

						if (request.getHeader("User-Agent").contains("Mobile")
								|| request.getHeader("User-Agent").contains("iphone")
								|| (request.getHeader("CloudFront-Is-Mobile-Viewer") != null
										&& request.getHeader("CloudFront-Is-Mobile-Viewer").equalsIgnoreCase("true"))
								|| (request.getHeader("CloudFront-Is-Tablet-Viewer") != null
										&& request.getHeader("CloudFront-Is-Tablet-Viewer").equalsIgnoreCase("true"))) {
							logger.info("Opening Mobile Payment page");

						} else {
							logger.info("Opening web Payment page");
						}

					} else {
						fields.put(FieldType.AMOUNT.getName(),
								(String) request.getSession().getAttribute(FieldType.AMOUNT.getName()));
						fields.put(FieldType.TXNTYPE.getName(),
								ModeType.getDefaultPurchaseTransaction(user.getModeType()).getName());
						fields.put(FieldType.RESPONSE_MESSAGE.getName(),
								ErrorType.PAYMENT_OPTIONS_NOT_CONFIGURED.getResponseMessage());
						fields.put(FieldType.RESPONSE_CODE.getName(),
								ErrorType.PAYMENT_OPTIONS_NOT_CONFIGURED.getCode());
						fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
						fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
						responseCreator.create(fields);
						responseCreator.ResponsePost(fields, response);
//						List<String> fieldTypeList = new ArrayList<String>(fields.getFields().keySet());
//						for (String fieldType : fieldTypeList) {
//							responseMap.put(fieldType, fields.get(fieldType));
//						}
//
//						return responseMap;
					}
				}
			} else {
				fields.put(FieldType.TXNTYPE.getName(),
						ModeType.getDefaultPurchaseTransaction(user.getModeType()).getName());
				String crisFlag = (String) request.getSession()
						.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
				if (StringUtils.isNotBlank(crisFlag)) {
					fields.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), crisFlag);
				}
				if (fields.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase("999")) {
					fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				}
				fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
				responseCreator.create(fields);
				responseCreator.ResponsePost(fields, response);
//				List<String> fieldTypeList = new ArrayList<String>(fields.getFields().keySet());
//				for (String fieldType : fieldTypeList) {
//					responseMap.put(fieldType, fields.get(fieldType));
//				}
//
//				return responseMap;
			}

		} catch (SystemException systemException) {
			logger.error("Exception", systemException);
			Fields fields = (Fields) request.getSession().getAttribute(Constants.FIELDS.getValue());

			if (null == fields) {
				Fields invalidfields = new Fields();
//				return "invalidRequest";
				invalidfields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				invalidfields.put(FieldType.RESPONSE_CODE.getName(), systemException.getErrorType().getCode());
				invalidfields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getMessage());
				invalidfields.put(FieldType.RETURN_URL.getName(),
						propertiesManager.propertiesMap.get("staticPgQrReturnUrl"));
//				return merchantHostedResponse;
//				responseCreator.create(invalidfields);
				PrintWriter out = response.getWriter();
				String finalResponse = responseCreator.createPgResponse(invalidfields);
				logger.info("final response sent " + finalResponse);
				out.write(finalResponse);
				out.flush();
				out.close();
				return invalidfields.getFields();
			}

			String salt = propertiesManager.getSalt(fields.get(FieldType.PAY_ID.getName()));

			if (null == salt) {
				Fields invalidfields = new Fields();
//				return "invalidRequest";
				invalidfields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				invalidfields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
				invalidfields.put(FieldType.RESPONSE_CODE.getName(), systemException.getErrorType().getCode());
				invalidfields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getMessage());
				PrintWriter out = response.getWriter();
				String finalResponse = responseCreator.createPgResponse(invalidfields);
				logger.info("final response sent " + finalResponse);
				out.write(finalResponse);
				out.flush();
				out.close();
				return invalidfields.getFields();

			}

			if (StringUtils.isBlank(fields.get(FieldType.TXNTYPE.getName()))) {

				User user = null;

				if (propertiesManager.propertiesMap.get("useStaticData").equalsIgnoreCase("Y")) {
					user = staticDataProvider.getUserData(fields.get(FieldType.PAY_ID.getName()));
				} else {
					user = userDao.getUserClass(fields.get(FieldType.PAY_ID.getName()));
				}

				fields.put(FieldType.TXNTYPE.getName(),
						ModeType.getDefaultPurchaseTransaction(user.getModeType()).getName());
			}

//			saveInvalidTransaction(fields, request);
			// If an invalid request of valid merchant save it
			if (fields.get(FieldType.RESPONSE_CODE.getName()).equals(ErrorType.INVALID_RETURN_URL.getCode())) {
				// return "invalidRequest";
				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
				fields.put(FieldType.RESPONSE_CODE.getName(), systemException.getErrorType().getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getMessage());
				fields.put(FieldType.RETURN_URL.getName(), propertiesManager.propertiesMap.get("staticPgQrReturnUrl"));
				responseCreator.create(fields);
				fields.removeInternalFields();
				fields.put(FieldType.IS_MERCHANT_HOSTED.getName(), "Y");
				responseCreator.ResponsePost(fields, response);
				request.getSession().invalidate();
				List<String> fieldTypeList = new ArrayList<String>(fields.getFields().keySet());
				for (String fieldType : fieldTypeList) {
					responseMap.put(fieldType, fields.get(fieldType));
				}
				return responseMap;

			}
			// fields.put(FieldType.TXNTYPE.getName(),
			// ModeType.getDefaultPurchaseTransaction(user.getModeType()).getName());
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), systemException.getErrorType().getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getMessage());
			fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
			responseCreator.create(fields);
			fields.removeInternalFields();
			String crisFlag = (String) request.getSession()
					.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
			if (StringUtils.isNotBlank(crisFlag)) {
				fields.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), crisFlag);
			}
			responseCreator.ResponsePost(fields, response);
			request.getSession().invalidate();
			List<String> fieldTypeList = new ArrayList<String>(fields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				responseMap.put(fieldType, fields.get(fieldType));
			}
//
//			return responseMap;
		} catch (Exception exception) {
			SessionCleaner.cleanSession(request.getSession());
			logger.error("Exception", exception);
			String path = request.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = request.getScheme() + "://" + request.getHeader("Host") + "/pgui/jsp/error";
				response.sendRedirect(resultPath);
			}
			response.sendRedirect("error");
		}
		return responseMap;
	}

	private Fields requestCreator(Fields fields, HttpServletRequest request) throws Exception {

		fields.put(FieldType.TXNTYPE.getName(), TransactionType.NEWORDER.getName());
		fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
		fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
		fields.logAllFields("All request fields :");

		Map<String, String> response = transactionControllerServiceProvider.transact(fields,
				Constants.TXN_WS_INTERNAL.getValue());
		fields = new Fields(response);
		request.getSession().setAttribute(Constants.FIELDS.getValue(), fields);
		logger.info("Response fields from pgws:  " + fields.getFieldsAsString());
		return fields;
	}

	private void saveInvalidTransaction(Fields fields, HttpServletRequest request) {
		fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
		request.getSession().setAttribute((FieldType.INTERNAL_ORIG_TXN_TYPE.getName()),
				fields.get(FieldType.TXNTYPE.getName()));
		try {
			updateProcessor.preProcess(fields);
			updateProcessor.prepareInvalidTransactionForStorage(fields);
		} catch (SystemException systemException) {
			logger.error("Unable to save invalid transaction", systemException);
		} catch (Exception exception) {
			logger.error("Unhandaled error", exception);
		}
	}

	public Map<String, String> cancelByUser(HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> responseMap = new HashMap<String, String>();
		try {
			Fields fields = null;
			ActionService service = PgActionServiceFactory.getActionService();
			Fields newfields = service.prepareFields(request.getParameterMap());

			if (newfields.contains(FieldType.CHECKOUT_JS_FLAG.getName())
					&& newfields.get(FieldType.CHECKOUT_JS_FLAG.getName()).equalsIgnoreCase("true")) {
				request.getSession().invalidate();
			} else {
				fields = (Fields) request.getSession().getAttribute(Constants.FIELDS.getValue());
			}
			if (null != fields) {
			} else {
				if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("ADMIN_PAYID"))
						&& StringUtils.isNotBlank(newfields.get("encSessionData"))) {
					Map<String, String> fieldsMap = new HashMap<String, String>();
					Map<String, String> responseDecryptMap = transactionControllerServiceProvider.hostedDecrypt(
							PropertiesManager.propertiesMap.get("ADMIN_PAYID"), newfields.get("encSessionData"));
					if (!responseDecryptMap.isEmpty()) {
						String decryptedString = responseDecryptMap.get(FieldType.ENCDATA.getName());
						String[] fieldArray = decryptedString.split("~");

						for (String key : fieldArray) {
							String[] namValuePair = key.split("=", 2);
							request.getSession().setAttribute(namValuePair[0], namValuePair[1]);
						}
					}
					String sessionFields = (String) request.getSession().getAttribute(Constants.FIELDS.getValue());
					sessionFields = sessionFields.substring(1, sessionFields.length() - 1);
					String[] fieldArray = sessionFields.split(",");
					for (String key : fieldArray) {
						if (key.charAt(0) == ' ') {
							key = key.replaceFirst("^\\s*", "");
						}
						String[] namValuePair = key.split("=", 2);
						fieldsMap.put(namValuePair[0], namValuePair[1]);
					}
					fields = new Fields(fieldsMap);
					logger.info(fieldsMap.toString());

				} else {
					logger.info("session fields lost");
					String path = request.getContextPath();
					logger.info(path);
					if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
						String resultPath = request.getScheme() + "://" + request.getHeader("Host") + "/pgui/jsp/error";
						response.sendRedirect(resultPath);
					}
					response.sendRedirect("error");
				}
			}
			if (null == fields) {
				fields = new Fields();
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.TIMEOUT.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.TIMEOUT.getCode());
				fields.put(FieldType.STATUS.getName(), StatusType.TIMEOUT.getName());
				fields.put(FieldType.RETURN_URL.getName(), ConfigurationConstants.DEFAULT_RETURN_URL.getValue());

				String pgFlag = (String) request.getSession()
						.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
				if (StringUtils.isNotBlank(pgFlag)) {
					fields.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
				}
				request.getSession().invalidate();
				fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
				responseCreator.create(fields);
				responseCreator.ResponsePost(fields, response);
			} else {

				fieldsDao.getPreviousDataFromSentToBank(fields);
				// Remove hash generated earlier
				fields.remove(FieldType.HASH.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.CANCELLED.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.CANCELLED.getCode());
				fields.put(FieldType.STATUS.getName(), StatusType.CANCELLED.getName());
				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
				if (StringUtils.isNotBlank((String) request.getSession().getAttribute("UPI_TOTAL_AMOUNT"))) {
					fields.put(FieldType.TOTAL_AMOUNT.getName(),
							(String) request.getSession().getAttribute("UPI_TOTAL_AMOUNT"));
				}

				fields.put(FieldType.CHECKOUT_JS_FLAG.getName(),
						(String) request.getSession().getAttribute(FieldType.CHECKOUT_JS_FLAG.getName()));
				fields.put(FieldType.INTERNAL_SHOPIFY_YN.getName(),
						(String) request.getSession().getAttribute(FieldType.INTERNAL_SHOPIFY_YN.getName()));
				String shopifyFlag = (String) request.getSession()
						.getAttribute(FieldType.INTERNAL_SHOPIFY_YN.getName());

				fields.put((FieldType.CANCEL_URL.getName()),
						(String) request.getSession().getAttribute(FieldType.CANCEL_URL.getName()));
				Object txnTypeObject = (Object) request.getSession()
						.getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName());
				if (null != shopifyFlag && shopifyFlag.equals("Y")) {
					fields.put((FieldType.RETURN_URL.getName()),
							(String) request.getSession().getAttribute(FieldType.CANCEL_URL.getName()));
				}

				if (null != txnTypeObject) {
					String txnType = (String) txnTypeObject;
					fields.put(FieldType.TXNTYPE.getName(), txnType);
					fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), txnType);
				}
				fields.put(FieldType.INTERNAL_ORIG_TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));
				fields.put(FieldType.OID.getName(), fields.get(FieldType.TXN_ID.getName()));
				String newTxnId = TransactionManager.getNewTransactionId();
				fields.put(FieldType.TXN_ID.getName(), newTxnId);
				if (StringUtils.isBlank(fields.get(FieldType.PG_REF_NUM.getName()))) {
					fields.put(FieldType.PG_REF_NUM.getName(), newTxnId);
				}
				String surchargeFlag = (String) request.getSession().getAttribute(FieldType.SURCHARGE_FLAG.getName());
				fields.put(FieldType.SURCHARGE_FLAG.getName(), surchargeFlag);
				fields.put(FieldType.CARD_HOLDER_TYPE.getName(), CardHolderType.CONSUMER.toString());
				fields.put(FieldType.PAYMENTS_REGION.getName(), AccountCurrencyRegion.DOMESTIC.toString());
				try {
					field.insert(fields);
				} catch (SystemException systemException) {
					SessionCleaner.cleanSession(request.getSession());
					fields.put(FieldType.RESPONSE_CODE.getName(), systemException.getErrorType().getCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getMessage());
					logger.error("Unable to update cancelled transaction", systemException);
				}
				String pgFlag = (String) request.getSession()
						.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
				if (StringUtils.isNotBlank(pgFlag)) {
					fields.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
				}
				fields.remove(FieldType.PG_TXN_MESSAGE.getName());
				fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
				responseCreator.create(fields);
				responseCreator.ResponsePost(fields, response);
				request.getSession().invalidate();
				List<String> fieldTypeList = new ArrayList<String>(fields.getFields().keySet());
				for (String fieldType : fieldTypeList) {
					responseMap.put(fieldType, fields.get(fieldType));
				}

			}
			return responseMap;
		} catch (Exception exception) {
			Fields fields = new Fields();
			String pgFlag = (String) request.getSession().getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
			if (StringUtils.isNotBlank(pgFlag)) {
				fields.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
			}
			SessionCleaner.cleanSession(request.getSession());
			logger.error("Exception", exception);

			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.CANCELLED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.CANCELLED.getCode());
			fields.put(FieldType.STATUS.getName(), StatusType.CANCELLED.getName());
			fields.put(FieldType.RETURN_URL.getName(), ConfigurationConstants.DEFAULT_RETURN_URL.getValue());
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
			responseCreator.create(fields);
			responseCreator.ResponsePost(fields, response);
			List<String> fieldTypeList = new ArrayList<String>(fields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				responseMap.put(fieldType, fields.get(fieldType));
			}
			return responseMap;

		}
	}

	public void returnToMerchant(HttpServletRequest httpRequest, HttpServletResponse httpreResponse) {
		try {
			Fields fields = (Fields) httpRequest.getSession().getAttribute(Constants.FIELDS.getValue());

			if (null == fields) {
				fields = new Fields();
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.TIMEOUT.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.TIMEOUT.getCode());
				fields.put(FieldType.STATUS.getName(), StatusType.TIMEOUT.getName());
				fields.put(FieldType.RETURN_URL.getName(), ConfigurationConstants.DEFAULT_RETURN_URL.getValue());

				String pgFlag = (String) httpRequest.getSession()
						.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
				if (StringUtils.isNotBlank(pgFlag)) {
					fields.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
				}
				httpRequest.getSession().invalidate();
				fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
				responseCreator.create(fields);
				responseCreator.ResponsePost(fields, httpreResponse);
			} else {
				fields.remove(FieldType.MERCHANT_ID.getName());
				fields.remove(FieldType.ACQUIRER_TDR_SC.getName());
				fields.remove(FieldType.ACQUIRER_TYPE.getName());
				fields.remove(FieldType.ORIG_TXN_ID.getName());
				fields.remove(FieldType.PG_GST.getName());
				fields.remove(FieldType.CARD_EXP_DT.getName());
				fields.remove(FieldType.OID.getName());
				fields.remove(FieldType.ACQUIRER_MODE.getName());
				fields.remove(FieldType.PAYMENTS_REGION.getName());
				fields.remove(FieldType.SLAB_ID.getName());
				fields.remove(FieldType.AUTH_CODE.getName());
				fields.remove(FieldType.ACQUIRER_GST.getName());
				fields.remove(FieldType.PG_TDR_SC.getName());
				fields.remove(FieldType.AVR.getName());
				fields.remove(FieldType.MD.getName());
				fields.remove(FieldType.RESELLER_CHARGES.getName());
				fields.remove(FieldType.RESELLER_GST.getName());
				fields.remove(FieldType.PG_RESELLER_CHARGE.getName());
				fields.remove(FieldType.PG_RESELLER_GST.getName());

				String pgFlag = (String) httpRequest.getSession()
						.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
				if (StringUtils.isNotBlank(pgFlag)) {
					fields.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
				}
				// fields.remove(FieldType.PG_TXN_MESSAGE.getName());
				fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
				responseCreator.create(fields);
				responseCreator.ResponsePost(fields, httpreResponse);
				httpRequest.getSession().invalidate();
			}
			// return NONE;
		} catch (Exception exception) {
			Fields fields = new Fields();
			String pgFlag = (String) httpRequest.getSession()
					.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
			if (StringUtils.isNotBlank(pgFlag)) {
				fields.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
			}
			httpRequest.getSession().invalidate();
			logger.error("Exception", exception);

			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.CANCELLED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.CANCELLED.getCode());
			fields.put(FieldType.STATUS.getName(), StatusType.CANCELLED.getName());
			fields.put(FieldType.RETURN_URL.getName(), ConfigurationConstants.DEFAULT_RETURN_URL.getValue());
			responseCreator.ResponsePost(fields, httpreResponse);
			// return Action.ERROR;

		}
	}

	@SuppressWarnings("unchecked")
	public String acquirerHandler(HttpServletRequest request, HttpServletResponse res) throws IOException {
		try {
			// create fields for transaction
			Fields fields = null;
			ActionService service = PgActionServiceFactory.getActionService();
			Fields newFields = service.prepareFields(request.getParameterMap());
			if (newFields.contains(FieldType.CHECKOUT_JS_FLAG.getName())
					&& newFields.get(FieldType.CHECKOUT_JS_FLAG.getName()).equalsIgnoreCase("true")) {
				request.getSession().invalidate();
			} else {
				if (StringUtils.isNoneBlank(newFields.get("paymentFlow"))
						&& newFields.get("paymentFlow").equalsIgnoreCase("ADDANDPAY")) {
					Map<String, String> fieldsMap = new HashMap<String, String>();
					String sessionFields = (String) request.getSession().getAttribute(Constants.FIELDS.getValue());
					sessionFields = sessionFields.substring(1, sessionFields.length() - 1);
					String[] fieldArray = sessionFields.split(",");
					for (String key : fieldArray) {
						if (key.charAt(0) == ' ') {
							key = key.replaceFirst("^\\s*", "");
						}
						String[] namValuePair = key.split("=", 2);
						fieldsMap.put(namValuePair[0], namValuePair[1]);
					}
					fields = new Fields(fieldsMap);
				} else {
					fields = (Fields) request.getSession().getAttribute(Constants.FIELDS.getValue());
				}
			}
			if (null != fields) {
			} else {
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("ADMIN_PAYID"))
						&& StringUtils.isNotBlank(newFields.get("encSessionData"))) {
					Map<String, String> fieldsMap = new HashMap<String, String>();
					Map<String, String> responseMap = transactionControllerServiceProvider.hostedDecrypt(
							propertiesManager.propertiesMap.get("ADMIN_PAYID"), newFields.get("encSessionData"));
					if (!responseMap.isEmpty()) {
						String decryptedString = responseMap.get(FieldType.ENCDATA.getName());
						String[] fieldArray = decryptedString.split("~");

						for (String key : fieldArray) {
							String[] namValuePair = key.split("=", 2);
							request.getSession().setAttribute(namValuePair[0], namValuePair[1]);
						}
					}
					String sessionFields = (String) request.getSession().getAttribute(Constants.FIELDS.getValue());
					sessionFields = sessionFields.substring(1, sessionFields.length() - 1);
					String[] fieldArray = sessionFields.split(",");
					for (String key : fieldArray) {
						if (key.charAt(0) == ' ') {
							key = key.replaceFirst("^\\s*", "");
						}
						String[] namValuePair = key.split("=", 2);
						fieldsMap.put(namValuePair[0], namValuePair[1]);
					}
					fields = new Fields(fieldsMap);
					logger.info(fieldsMap.toString());

				} else {
					logger.info("session fields lost");
					String path = request.getContextPath();
					logger.info(path);
					if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
						String resultPath = request.getScheme() + "://" + request.getHeader("Host") + "/pgui/jsp/error";
						res.sendRedirect(resultPath);
					}
					res.sendRedirect("error");
				}
			}

			if (request.getSession().getAttribute(FieldType.IS_ENROLLED.getName()) != null
					&& request.getSession().getAttribute(FieldType.IS_ENROLLED.getName()).toString()
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {
				// return Action.NONE;
			} else {
				request.getSession().setAttribute(FieldType.IS_ENROLLED.getName(), Constants.Y_FLAG.getValue());
			}

			if (request.getSession().getAttribute(FieldType.RETRY_FLAG.getName()) != null
					&& request.getSession().getAttribute(FieldType.RETRY_FLAG.getName()).toString()
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {
				fields.remove(FieldType.PG_TXN_MESSAGE.getName());
				fields.remove(FieldType.AUTH_CODE.getName());
				fields.remove(FieldType.RESPONSE_CODE.getName());
				fields.remove(FieldType.MERCHANT_ID.getName());
				fields.remove(FieldType.PG_RESP_CODE.getName());
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.ACQUIRER_TYPE.getName()))) {
				String origTxnType = (String) request.getSession()
						.getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName());
				String oid = (String) request.getSession().getAttribute(FieldType.OID.getName());
				Fields dbfields = fieldsDao.getPreviousForPgRefNum(
						(String) request.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_ID.getName()));
				fields.put(FieldType.CARD_HOLDER_TYPE.getName(), dbfields.get(FieldType.CARD_HOLDER_TYPE.getName()));
				fields.put(FieldType.PAYMENTS_REGION.getName(), dbfields.get(FieldType.PAYMENTS_REGION.getName()));
				fields.put(FieldType.OID.getName(), oid);
				fields.put(FieldType.TXNTYPE.getName(), origTxnType);
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), origTxnType);
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DUPLICATE.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE.getCode());
				fields.put(FieldType.STATUS.getName(), StatusType.DUPLICATE.getName());
				fields.put(FieldType.TXNTYPE.getName(), fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));

				fields.put(FieldType.INTERNAL_ORIG_TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));
				String newTxnId = TransactionManager.getNewTransactionId();
				fields.put(FieldType.TXN_ID.getName(), newTxnId);
				String surchargeFlag = request.getSession().getAttribute(FieldType.SURCHARGE_FLAG.getName()).toString();
				fields.put(FieldType.SURCHARGE_FLAG.getName(), surchargeFlag);
				fields.remove(FieldType.PG_TXN_MESSAGE.getName());
				fields.remove(FieldType.ACQ_ID.getName());
				try {
					field.insert(fields);
				} catch (SystemException systemException) {
					SessionCleaner.cleanSession(request.getSession());
					fields.put(FieldType.RESPONSE_CODE.getName(), systemException.getErrorType().getCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(),
							systemException.getErrorType().getResponseMessage());
					logger.error("Unable to update cancelled transaction", systemException);
				}

				transactionResponser.removeInvalidResponseFields(fields);
				transactionResponser.addResponseDateTime(fields);
				String pgFlag = (String) request.getSession()
						.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
				if (StringUtils.isNotBlank(pgFlag)) {
					fields.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
				}
				request.getSession().invalidate();
				fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
				responseCreator.create(fields);
				responseCreator.ResponsePost(fields, res);
				// return Action.NONE;

			}

			if (StringUtils.isNotBlank(
					(String) request.getSession().getAttribute(FieldType.INTERNAL_HEADER_ACEEPT.getName()))) {
				fields.put((FieldType.INTERNAL_HEADER_ACEEPT.getName()),
						(String) request.getSession().getAttribute(FieldType.INTERNAL_HEADER_ACEEPT.getName()));
			}
			if (StringUtils.isNotBlank(
					(String) request.getSession().getAttribute(FieldType.INTERNAL_HEADER_USER_AGENT.getName()))) {
				fields.put((FieldType.INTERNAL_HEADER_USER_AGENT.getName()),
						(String) request.getSession().getAttribute(FieldType.INTERNAL_HEADER_USER_AGENT.getName()));
			}
			if (StringUtils
					.isNotBlank((String) request.getSession().getAttribute(FieldType.INTERNAL_CUST_IP.getName()))) {
				fields.put((FieldType.INTERNAL_CUST_IP.getName()),
						(String) request.getSession().getAttribute(FieldType.INTERNAL_CUST_IP.getName()));
			}
			if (StringUtils.isNotBlank(
					(String) request.getSession().getAttribute(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()))) {
				fields.put((FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()),
						(String) request.getSession().getAttribute(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()));
			}
			String cardPaymentType = newFields.get("paymentType");
			String cardMopType = newFields.get("mopType");

			if (cardPaymentType.equals(PaymentType.NET_BANKING.getCode())) {

				fields.put(FieldType.PAYMENT_TYPE.getName(), newFields.get("paymentType"));
				// fields.put(FieldType.MOP_TYPE.getName(), cardMopType);
				fields.put(FieldType.MOP_TYPE.getName(), MopType.getMopTypeName(newFields.get("mopType")));
				fields.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(), "IN");
				fields.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(), MopType.getmopName(newFields.get("mopType")));
				// setMopType(MopType.getmopName(newFields.get("mopType")));
			}

			else if (cardPaymentType.equals(PaymentType.WALLET.getCode())) {
				fields.put(FieldType.PAYMENT_TYPE.getName(), newFields.get("paymentType"));
				fields.put(FieldType.MOP_TYPE.getName(), MopType.getMopTypeName(newFields.get("mopType")));
				fields.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(), "IN");
				fields.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(), MopType.getmopName(newFields.get("mopType")));
			} else if (cardPaymentType.equals(PaymentType.COD.getCode())) {
				fields.put(FieldType.PAYMENT_TYPE.getName(), newFields.get("paymentType"));
				fields.put(FieldType.MOP_TYPE.getName(), MopType.getMopTypeName(newFields.get("mopType")));
				fields.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(), "IN");
				fields.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(), MopType.getmopName(newFields.get("mopType")));
			} else if (cardPaymentType.equals(PaymentType.AAMARPAY.getCode())) {
				fields.put(FieldType.PAYMENT_TYPE.getName(), newFields.get("paymentType"));
				fields.put(FieldType.MOP_TYPE.getName(), MopType.getMopTypeName("AAMARPAY"));
				fields.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(), "IN");
				fields.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(), MopType.getmopName(newFields.get("mopType")));
			} else if (cardPaymentType.equals(PaymentType.CRYPTO.getCode())) {
				fields.put(FieldType.PAYMENT_TYPE.getName(), newFields.get("paymentType"));
				fields.put(FieldType.MOP_TYPE.getName(), newFields.get("mopType"));
				fields.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(), "IN");
				fields.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(),
						MopType.getMopTypeName(newFields.get("mopType")));// "Crypto");
			} else if (cardPaymentType.equals(PaymentType.EXPRESS_PAY.getCode())) {
				fields.put(FieldType.PAYMENT_TYPE.getName(), newFields.get("paymentType"));
				fields.put(FieldType.MOP_TYPE.getName(), newFields.get("mopType"));
				fields.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(), "IN");
				fields.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(), MopType.getmopName(newFields.get("mopType")));
			} else if (cardPaymentType.equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())
					|| cardPaymentType.equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())
					|| cardPaymentType.equalsIgnoreCase(PaymentType.EMI_CC.getCode())
					|| cardPaymentType.equalsIgnoreCase(PaymentType.EMI_DC.getCode())) {
				Map<String, String> binRangeResponseMap = new HashMap<String, String>();
				if (newFields.contains(FieldType.CHECKOUT_JS_FLAG.getName())
						&& newFields.get(FieldType.CHECKOUT_JS_FLAG.getName()).equalsIgnoreCase("true")) {
					binRangeResponseMap = bindbControllerServiceProvider.binfind(newFields.get("bin"),
							newFields.get("payId"));
				} else {
					binRangeResponseMap = (Map<String, String>) request.getSession()
							.getAttribute(Constants.BIN.getValue());
				}
				String binMopType = binRangeResponseMap.get(FieldType.MOP_TYPE.getName());
				String binPaymentType = binRangeResponseMap.get(FieldType.PAYMENT_TYPE.getName());
				String binIssuerBankName = binRangeResponseMap.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName());
				String binIssuerCountry = binRangeResponseMap.get(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName());
				String paymentsRegion = binRangeResponseMap.get(FieldType.PAYMENTS_REGION.getName());
				String cardHolderType = binRangeResponseMap.get(FieldType.CARD_HOLDER_TYPE.getName());

				logger.info("BinRangeResponseMap values for orderId = " + fields.get(FieldType.ORDER_ID.getName())
						+ " binMopType = " + binMopType + " binPaymentType = " + binPaymentType
						+ " binpPaymentsRegion = " + paymentsRegion + " binCardHolderType = " + cardHolderType);

				if (!(binMopType.equals(cardMopType)) && (binPaymentType.equals(cardPaymentType))) {
					String origTxnType = (String) request.getSession()
							.getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName());
					fields.put(FieldType.TXNTYPE.getName(), origTxnType);
					fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), origTxnType);

					Map<String, String> responseFields = transactionControllerServiceProvider.transact(fields,
							Constants.TXN_WS_INTERNAL.getValue());
					Fields field = new Fields(responseFields);

					field.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DENIED_BY_FRAUD.getResponseMessage());
					field.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DENIED_BY_FRAUD.getCode());
					field.put(FieldType.TXNTYPE.getName(), fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));

					transactionResponser.removeInvalidResponseFields(field);
					transactionResponser.addResponseDateTime(field);
					String pgFlag = (String) request.getSession()
							.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
					if (StringUtils.isNotBlank(pgFlag)) {
						fields.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
					}
					field.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
					responseCreator.create(field);
					responseCreator.ResponsePost(field, res);
					// return Action.NONE;
				}

				if (cardPaymentType.isEmpty() || cardMopType.isEmpty()) {
					String origTxnType = (String) request.getSession()
							.getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName());
					fields.put(FieldType.TXNTYPE.getName(), origTxnType);
					fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), origTxnType);

					Map<String, String> responseFields = transactionControllerServiceProvider.transact(fields,
							Constants.TXN_WS_INTERNAL.getValue());
					Fields field = new Fields(responseFields);

					field.put(FieldType.RESPONSE_MESSAGE.getName(),
							ErrorType.CARD_NUMBER_NOT_SUPPORTED.getResponseMessage());
					field.put(FieldType.RESPONSE_CODE.getName(), ErrorType.CARD_NUMBER_NOT_SUPPORTED.getCode());
					field.put(FieldType.TXNTYPE.getName(), fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));

					transactionResponser.removeInvalidResponseFields(field);
					transactionResponser.addResponseDateTime(field);
					String pgFlag = (String) request.getSession()
							.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
					if (StringUtils.isNotBlank(pgFlag)) {
						fields.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
					}
					field.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
					responseCreator.create(field);
					responseCreator.ResponsePost(field, res);
					// return Action.NONE;

				}

				fields.put(FieldType.PAYMENT_TYPE.getName(), newFields.get("paymentType"));
				fields.put(FieldType.MOP_TYPE.getName(), newFields.get("mopType"));
				fields.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(), binIssuerCountry);
				fields.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(), binIssuerBankName);

				if (!StringUtils.isBlank(cardHolderType)) {
					fields.put(FieldType.CARD_HOLDER_TYPE.getName(), cardHolderType);
				} else {
					fields.put(FieldType.CARD_HOLDER_TYPE.getName(), CardHolderType.CONSUMER.toString());
				}

				// If PAYMENTS_REGION not found from bin , set defaults to
				// DOMESTIC
				if (!StringUtils.isBlank(paymentsRegion)) {
					fields.put(FieldType.PAYMENTS_REGION.getName(), paymentsRegion);
				} else {
					fields.put(FieldType.PAYMENTS_REGION.getName(), AccountCurrencyRegion.DOMESTIC.toString());
				}

				request.getSession().setAttribute((FieldType.CARD_HOLDER_TYPE.getName()),
						fields.get(FieldType.CARD_HOLDER_TYPE.getName()));
				request.getSession().setAttribute((FieldType.PAYMENTS_REGION.getName()),
						fields.get(FieldType.PAYMENTS_REGION.getName()));

				if (cardPaymentType.equals(PaymentType.EMI_CC.getCode())
						|| cardPaymentType.equals(PaymentType.EMI_DC.getCode())) {

					fields.put(FieldType.TENURE.getName(), newFields.get("tenure"));
					fields.put(FieldType.RATE_OF_INTEREST.getName(), newFields.get("rateOfInterest"));
					fields.put(FieldType.EMI_PER_MONTH.getName(), newFields.get("perMonthEmiAmount"));
					fields.put(FieldType.EMI_TOTAL_AMOUNT.getName(), newFields.get("totalEmiAmount"));
					fields.put(FieldType.ISSUER_BANK.getName(), newFields.get("issuerName"));
					fields.put(FieldType.EMI_INTEREST.getName(), newFields.get("emiInterest"));
					request.getSession().setAttribute(FieldType.TENURE.getName(), newFields.get("tenure"));
					request.getSession().setAttribute(FieldType.RATE_OF_INTEREST.getName(),
							newFields.get("rateOfInterest"));
					request.getSession().setAttribute(FieldType.EMI_PER_MONTH.getName(),
							newFields.get("perMonthEmiAmount"));
					request.getSession().setAttribute(FieldType.EMI_TOTAL_AMOUNT.getName(),
							newFields.get("totalEmiAmount"));
					request.getSession().setAttribute(FieldType.ISSUER_BANK.getName(), newFields.get("issuerName"));
					request.getSession().setAttribute(FieldType.EMI_INTEREST.getName(), newFields.get("emiInterest"));
				}
			} else {

			}

			String origTxnType = (String) request.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName());
			fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), origTxnType);

			// Put transaction id as original txnId for subsequent transactions
			String sessionOId = (String) request.getSession().getAttribute(FieldType.OID.getName());
			fields.put(FieldType.INTERNAL_ORIG_TXN_ID.getName(), sessionOId);
			switch (PaymentType.getInstanceUsingCode(fields.get(FieldType.PAYMENT_TYPE.getName()))) {
			case CREDIT_CARD:
			case DEBIT_CARD:
			case PREPAID_CARD:
			case EMI_CC:
			case EMI_DC:
				fields.put(FieldType.TXNTYPE.getName(), TransactionType.ENROLL.getName());
				if (!newFields.get("mopType").endsWith(MopType.EZEECLICK.getCode())) {

					// {emiInterest=, issuerBankName=, amount=,
					// cardsaveflag1=on, totalEmiAmount=,
					// cardName=sandeep, issuerName=, expiryMonth=12,
					// paymentsRegion=DOMESTIC,
					// mopType=VI, rateOfInterest=, expiryYear=2023,
					// paymentType=CC, issuerCountry=,
					// cardsaveflag=true, perMonthEmiAmount=, cvvNumber=123,
					// cardHolderType=CONSUMER, tenure=, cardNumber=4000 0000
					// 0000 0002}

					fields.put(FieldType.CARD_NUMBER.getName(),
							newFields.get("cardNumber").replace(" ", "").replace(",", ""));
					fields.put(FieldType.CARD_EXP_DT.getName(),
							newFields.get("expiryMonth") + newFields.get("expiryYear"));
					fields.put(FieldType.CVV.getName(), newFields.get("cvvNumber").replace(" ", "").replace(",", ""));
					fields.put(FieldType.CUST_NAME.getName(), newFields.get("cardName"));
				}
				// Save Card detail for Express Payment

				if (StringUtils.isNotBlank(fields.get(FieldType.RESPONSE_CODE.getName()))
						&& fields.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase(ErrorType.SUCCESS.getCode())
						&& !fields.get(FieldType.TXNTYPE.getName())
								.equalsIgnoreCase(TransactionType.NEWORDER.getName())) {
					if (newFields.get("cardsaveflag").equalsIgnoreCase("true")) {

						User user = null;

						if (propertiesManager.propertiesMap.get("useStaticData").equalsIgnoreCase("Y")) {
							user = staticDataProvider.getUserData(fields.get(FieldType.PAY_ID.getName()));
						} else {
							user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
						}

						tokenManager.addToken(fields, user);
					}
				}

				request.getSession().setAttribute(Constants.CUSTOM_TOKEN.getValue(), generateGUID());
				request.getSession().setAttribute((FieldType.CARD_NUMBER.getName()),
						fields.get(FieldType.CARD_NUMBER.getName()));
				request.getSession().setAttribute((FieldType.CARD_EXP_DT.getName()),
						fields.get(FieldType.CARD_EXP_DT.getName()));
				request.getSession().setAttribute((FieldType.CVV.getName()), fields.get(FieldType.CVV.getName()));
				request.getSession().setAttribute((FieldType.INTERNAL_CARD_ISSUER_BANK.getName()),
						fields.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));
				request.getSession().setAttribute((FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()),
						fields.get(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()));
				fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
				break;

			case EXPRESS_PAY:
				if (null != newFields.get("tokenId")) {
					fields.put(FieldType.TOKEN_ID.getName(), newFields.get("tokenId"));
					fields.put(FieldType.KEY_ID.getName(), SystemConstants.DEFAULT_KEY_ID);
					Map<String, Token> tokenMap = (Map<String, Token>) request.getSession()
							.getAttribute(Constants.TOKEN.getValue());
					Token token = tokenMap.get(newFields.get("tokenId"));
					// decryoted token
					Map<String, String> cardObj = tokenManager.decryptToken(token);
					fields.put(FieldType.TXNTYPE.getName(), TransactionType.ENROLL.getName());
					fields.put(FieldType.CARD_NUMBER.getName(),
							cardObj.get(FieldType.CARD_NUMBER.getName()).replace(" ", "").replace(",", ""));
					fields.put(FieldType.CARD_EXP_DT.getName(), token.getExpiryDate());
					fields.put(FieldType.MOP_TYPE.getName(), token.getMopType());
					fields.put(FieldType.PAYMENT_TYPE.getName(), token.getPaymentType());
					fields.put(FieldType.CUST_NAME.getName(), token.getCustomerName());
					fields.put(FieldType.CVV.getName(), newFields.get("cvvNumber").replace(" ", "").replace(",", ""));
					fields.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(), token.getCardIssuerBank());
					fields.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(), token.getCardIssuerCountry());
					fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
					fields.put(FieldType.CARD_HOLDER_TYPE.getName(), token.getCardHolderType());
					fields.put(FieldType.PAYMENTS_REGION.getName(), token.getPaymentsRegion());

					request.getSession().setAttribute(Constants.CUSTOM_TOKEN.getValue(), generateGUID());
					request.getSession().setAttribute((FieldType.CARD_NUMBER.getName()),
							fields.get(FieldType.CARD_NUMBER.getName()));
					request.getSession().setAttribute((FieldType.CARD_EXP_DT.getName()),
							fields.get(FieldType.CARD_EXP_DT.getName()));
					request.getSession().setAttribute((FieldType.CVV.getName()), fields.get(FieldType.CVV.getName()));
					request.getSession().setAttribute((FieldType.INTERNAL_CARD_ISSUER_BANK.getName()),
							fields.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));
					request.getSession().setAttribute((FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()),
							fields.get(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()));
					request.getSession().setAttribute((FieldType.CARD_HOLDER_TYPE.getName()),
							fields.get(FieldType.CARD_HOLDER_TYPE.getName()));
					request.getSession().setAttribute((FieldType.PAYMENTS_REGION.getName()),
							fields.get(FieldType.PAYMENTS_REGION.getName()));
				}
				break;
			case NET_BANKING:
				fields.put(FieldType.APP_CODE.getName(), newFields.get("appMode"));
				fields.put(FieldType.TXN_SOURCE.getName(), newFields.get("txnSource"));
				request.getSession().setAttribute((FieldType.INTERNAL_ORIG_TXN_TYPE.getName()),
						TransactionType.SALE.getName());
				fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
				fields.put(FieldType.CARD_HOLDER_TYPE.getName(), CardHolderType.CONSUMER.toString());
				fields.put(FieldType.PAYMENTS_REGION.getName(), AccountCurrencyRegion.DOMESTIC.toString());
				request.getSession().setAttribute((FieldType.CARD_HOLDER_TYPE.getName()),
						CardHolderType.CONSUMER.toString());
				request.getSession().setAttribute((FieldType.PAYMENTS_REGION.getName()),
						AccountCurrencyRegion.DOMESTIC.toString());
				if (StringUtils.isNotBlank(fields.get(FieldType.RESPONSE_CODE.getName()))
						&& fields.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase(ErrorType.SUCCESS.getCode())
						&& !fields.get(FieldType.TXNTYPE.getName())
								.equalsIgnoreCase(TransactionType.NEWORDER.getName())) {
					tokenManager.addNBToken(fields, userDao.findPayId(fields.get(FieldType.PAY_ID.getName())));
				}
				break;
			case WALLET:
				request.getSession().setAttribute((FieldType.INTERNAL_ORIG_TXN_TYPE.getName()),
						TransactionType.SALE.getName());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
				fields.put(FieldType.CARD_HOLDER_TYPE.getName(), CardHolderType.CONSUMER.toString());
				fields.put(FieldType.PAYMENTS_REGION.getName(), AccountCurrencyRegion.DOMESTIC.toString());
				request.getSession().setAttribute((FieldType.CARD_HOLDER_TYPE.getName()),
						CardHolderType.CONSUMER.toString());

				// for Mobikwik Wallet
				if (StringUtils.isNotBlank(newFields.get("otp")))
					fields.put(FieldType.OTP.getName(), newFields.get("otp"));
				// for Mobikwik Wallet
				if (StringUtils.isNotBlank(newFields.get("phoneNo")))
					fields.put(FieldType.PHONE_NO.getName(), newFields.get("phoneNo"));
				// for Paytm Wallet token
				if (StringUtils.isNotBlank(newFields.get("token"))) {
					fields.put(FieldType.TOKEN.getName(), newFields.get("token"));
				}
				// for Paytm Wallet paymentFlow
				if (StringUtils.isNotBlank(newFields.get("paymentFlow"))) {
					fields.put(FieldType.PAYMENT_FlOW.getName(), newFields.get("paymentFlow"));
				}
				// for Paytm Wallet paymentFlow
				if (StringUtils.isNotBlank(newFields.get("addMoneyPaymentType"))
						&& (newFields.get("addMoneyPaymentType").equalsIgnoreCase("CC")
								|| newFields.get("addMoneyPaymentType").equalsIgnoreCase("DC")
								|| newFields.get("addMoneyPaymentType").equalsIgnoreCase("NB"))) {
					if (StringUtils.isNotBlank(newFields.get("expiryMonth"))
							&& StringUtils.isNotBlank(newFields.get("expiryYear"))
							&& StringUtils.isNotBlank(newFields.get("cardNumber"))
							&& StringUtils.isNotBlank(newFields.get("cvvNumber"))) {
					}
					if (newFields.get("addMoneyPaymentType").equalsIgnoreCase("CC")) {
						fields.put(FieldType.PAYTMENT_MODE.getName(), "CREDIT_CARD");
						fields.put(FieldType.CARD_INFO.getName(),
								"|" + newFields.get("cardNumber").replace(" ", "").trim() + "|"
										+ newFields.get("cvvNumber").trim() + "|" + newFields.get("expiryMonth")
										+ newFields.get("expiryYear"));
					} else if (newFields.get("addMoneyPaymentType").equalsIgnoreCase("DC")) {
						fields.put(FieldType.PAYTMENT_MODE.getName(), "DEBIT_CARD");
						fields.put(FieldType.CARD_INFO.getName(),
								"|" + newFields.get("cardNumber").replace(" ", "").trim() + "|"
										+ newFields.get("cvvNumber").trim() + "|" + newFields.get("expiryMonth")
										+ newFields.get("expiryYear"));
					} else if (newFields.get("addMoneyPaymentType").equalsIgnoreCase("NB")) {
						fields.put(FieldType.PAYTMENT_MODE.getName(), "NET_BANKING");
						fields.put(FieldType.CARD_INFO.getName(), newFields.get("mopType"));

					}
					fields.put(FieldType.MOP_TYPE.getName(), "PPL");
				}

				request.getSession().setAttribute((FieldType.PAYMENTS_REGION.getName()),
						AccountCurrencyRegion.DOMESTIC.toString());
				if (StringUtils.isNotBlank(fields.get(FieldType.RESPONSE_CODE.getName()))
						&& fields.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase(ErrorType.SUCCESS.getCode())
						&& !fields.get(FieldType.TXNTYPE.getName())
								.equalsIgnoreCase(TransactionType.NEWORDER.getName())) {
					tokenManager.addWalletToken(fields, userDao.findPayId(fields.get(FieldType.PAY_ID.getName())));
				}
				break;
			case EMI:
				break;
			case RECURRING_PAYMENT:
				break;
			case UPI:
				break;
			case COD:
			case AAMARPAY:
				request.getSession().setAttribute((FieldType.INTERNAL_ORIG_TXN_TYPE.getName()),
						TransactionType.SALE.getName());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
				fields.put(FieldType.CARD_HOLDER_TYPE.getName(), CardHolderType.CONSUMER.toString());
				fields.put(FieldType.PAYMENTS_REGION.getName(), AccountCurrencyRegion.DOMESTIC.toString());
				request.getSession().setAttribute((FieldType.CARD_HOLDER_TYPE.getName()),
						CardHolderType.CONSUMER.toString());
				request.getSession().setAttribute((FieldType.PAYMENTS_REGION.getName()),
						AccountCurrencyRegion.DOMESTIC.toString());
				break;
			case CRYPTO:
				request.getSession().setAttribute((FieldType.INTERNAL_ORIG_TXN_TYPE.getName()),
						TransactionType.SALE.getName());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
				fields.put(FieldType.CARD_HOLDER_TYPE.getName(), CardHolderType.CONSUMER.toString());
				fields.put(FieldType.PAYMENTS_REGION.getName(), AccountCurrencyRegion.DOMESTIC.toString());
				request.getSession().setAttribute((FieldType.CARD_HOLDER_TYPE.getName()),
						CardHolderType.CONSUMER.toString());
				request.getSession().setAttribute((FieldType.PAYMENTS_REGION.getName()),
						AccountCurrencyRegion.DOMESTIC.toString());
				break;
			default:
				break;

			}

			String surchargeFlag = request.getSession().getAttribute(FieldType.SURCHARGE_FLAG.getName()).toString();
			if (surchargeFlag.equals("Y")) {

				fields.put((FieldType.SURCHARGE_FLAG.getName()), surchargeFlag);
				String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());
				// Add surcharge amount and total amount on the basis of payment
				// type
				String paymentType = fields.get(FieldType.PAYMENT_TYPE.getName());
				if (paymentType.equals(PaymentType.CREDIT_CARD.getCode())) {

					String ccTotalAmount = "";

					if (StringUtils.isNotBlank(newFields.get("paymentsRegion")) && newFields.get("paymentsRegion")
							.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.toString())) {
						if (StringUtils.isNotBlank(newFields.get("cardHolderType")) && newFields.get("cardHolderType")
								.equalsIgnoreCase(CardHolderType.CONSUMER.toString())) {
							ccTotalAmount = request.getSession()
									.getAttribute(FieldType.CC_CONSUMER_TOTAL_AMOUNT.getName()).toString();
							ccTotalAmount = Amount.formatAmount(ccTotalAmount, currencyCode);
						}
						if (StringUtils.isNotBlank(newFields.get("cardHolderType")) && newFields.get("cardHolderType")
								.equalsIgnoreCase(CardHolderType.COMMERCIAL.toString())) {
							ccTotalAmount = request.getSession()
									.getAttribute(FieldType.CC_COMMERCIAL_TOTAL_AMOUNT.getName()).toString();
							ccTotalAmount = Amount.formatAmount(ccTotalAmount, currencyCode);
						}
						if (StringUtils.isNotBlank(newFields.get("cardHolderType")) && newFields.get("cardHolderType")
								.equalsIgnoreCase(CardHolderType.PREMIUM.toString())) {
							ccTotalAmount = request.getSession()
									.getAttribute(FieldType.CC_PREMIUM_TOTAL_AMOUNT.getName()).toString();
							ccTotalAmount = Amount.formatAmount(ccTotalAmount, currencyCode);
						}

					} else if (StringUtils.isNotBlank(newFields.get("paymentsRegion")) && newFields
							.get("paymentsRegion").equalsIgnoreCase(AccountCurrencyRegion.INTERNATIONAL.toString())) {
						ccTotalAmount = request.getSession().getAttribute(FieldType.IN_TOTAL_AMOUNT.getName())
								.toString();
						ccTotalAmount = Amount.formatAmount(ccTotalAmount, currencyCode);
					} else {
						ccTotalAmount = request.getSession().getAttribute(FieldType.CC_CONSUMER_TOTAL_AMOUNT.getName())
								.toString();
						ccTotalAmount = Amount.formatAmount(ccTotalAmount, currencyCode);
					}

					fields.put(FieldType.TOTAL_AMOUNT.getName(), ccTotalAmount);
				} else if (paymentType.equals(PaymentType.DEBIT_CARD.getCode())) {

					String dcTotalAmount = "";

					if (StringUtils.isNotBlank(newFields.get("mopType"))
							&& newFields.get("mopType").equalsIgnoreCase(MopType.VISA.getCode())) {
						dcTotalAmount = request.getSession().getAttribute(FieldType.DC_VISA_TOTAL_AMOUNT.getName())
								.toString();
						dcTotalAmount = Amount.formatAmount(dcTotalAmount, currencyCode);
					}
					if (StringUtils.isNotBlank(newFields.get("mopType"))
							&& newFields.get("mopType").equalsIgnoreCase(MopType.MASTERCARD.getCode())) {
						dcTotalAmount = request.getSession()
								.getAttribute(FieldType.DC_MASTERCARD_TOTAL_AMOUNT.getName()).toString();
						dcTotalAmount = Amount.formatAmount(dcTotalAmount, currencyCode);
					}
					if (StringUtils.isNotBlank(newFields.get("mopType"))
							&& newFields.get("mopType").equalsIgnoreCase(MopType.RUPAY.getCode())) {
						dcTotalAmount = request.getSession().getAttribute(FieldType.DC_RUPAY_TOTAL_AMOUNT.getName())
								.toString();
						dcTotalAmount = Amount.formatAmount(dcTotalAmount, currencyCode);
					}

					else if (StringUtils.isNotBlank(newFields.get("paymentsRegion")) && newFields.get("paymentsRegion")
							.equalsIgnoreCase(AccountCurrencyRegion.INTERNATIONAL.toString())) {
						dcTotalAmount = request.getSession().getAttribute(FieldType.IN_TOTAL_AMOUNT.getName())
								.toString();
						dcTotalAmount = Amount.formatAmount(dcTotalAmount, currencyCode);
					} else {
						dcTotalAmount = request.getSession().getAttribute(FieldType.DC_VISA_TOTAL_AMOUNT.getName())
								.toString();
						dcTotalAmount = Amount.formatAmount(dcTotalAmount, currencyCode);
					}
					fields.put(FieldType.TOTAL_AMOUNT.getName(), dcTotalAmount);

				} else if (paymentType.equals(PaymentType.PREPAID_CARD.getCode())) {

					String pcTotalAmount = "";

					if (StringUtils.isNotBlank(newFields.get("paymentsRegion")) && newFields.get("paymentsRegion")
							.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.toString())) {
						pcTotalAmount = request.getSession().getAttribute(FieldType.PC_TOTAL_AMOUNT.getName())
								.toString();
						pcTotalAmount = Amount.formatAmount(pcTotalAmount, currencyCode);
					}

					else if (StringUtils.isNotBlank(newFields.get("paymentsRegion")) && newFields.get("paymentsRegion")
							.equalsIgnoreCase(AccountCurrencyRegion.INTERNATIONAL.toString())) {
						pcTotalAmount = request.getSession().getAttribute(FieldType.IN_TOTAL_AMOUNT.getName())
								.toString();
						pcTotalAmount = Amount.formatAmount(pcTotalAmount, currencyCode);
					} else {
						pcTotalAmount = request.getSession().getAttribute(FieldType.PC_TOTAL_AMOUNT.getName())
								.toString();
						pcTotalAmount = Amount.formatAmount(pcTotalAmount, currencyCode);
					}
					fields.put(FieldType.TOTAL_AMOUNT.getName(), pcTotalAmount);

				} else if (paymentType.equals(PaymentType.NET_BANKING.getCode())) {
					String nbTotalAmount = request.getSession().getAttribute(FieldType.NB_TOTAL_AMOUNT.getName())
							.toString();
					nbTotalAmount = Amount.formatAmount(nbTotalAmount, currencyCode);
					fields.put(FieldType.TOTAL_AMOUNT.getName(), nbTotalAmount);
				} else if (paymentType.equals(PaymentType.UPI.getCode())) {
					String upiTotalAmount = request.getSession().getAttribute(FieldType.UP_TOTAL_AMOUNT.getName())
							.toString();
					upiTotalAmount = Amount.formatAmount(upiTotalAmount, currencyCode);
					fields.put(FieldType.TOTAL_AMOUNT.getName(), upiTotalAmount);
				}

				else if (paymentType.equals(PaymentType.WALLET.getCode())) {
					String wlTotalAmount = request.getSession().getAttribute(FieldType.WL_TOTAL_AMOUNT.getName())
							.toString();
					wlTotalAmount = Amount.formatAmount(wlTotalAmount, currencyCode);
					fields.put(FieldType.TOTAL_AMOUNT.getName(), wlTotalAmount);
				} else if (paymentType.equals(PaymentType.COD.getCode())) {
					String cdTotalAmount = request.getSession().getAttribute(FieldType.CD_TOTAL_AMOUNT.getName())
							.toString();
					cdTotalAmount = Amount.formatAmount(cdTotalAmount, currencyCode);
					fields.put(FieldType.TOTAL_AMOUNT.getName(), cdTotalAmount);
				} else if (paymentType.equals(PaymentType.AAMARPAY.getCode())) {
					String apTotalAmount = request.getSession().getAttribute(FieldType.AP_TOTAL_AMOUNT.getName())
							.toString();
					apTotalAmount = Amount.formatAmount(apTotalAmount, currencyCode);
					fields.put(FieldType.TOTAL_AMOUNT.getName(), apTotalAmount);
				} else if (paymentType.equals(PaymentType.EMI_CC.getCode())) {

					String emiCCTotalAmount = "";

					if (StringUtils.isNotBlank(newFields.get("paymentsRegion")) && newFields.get("paymentsRegion")
							.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.toString())) {
						emiCCTotalAmount = request.getSession().getAttribute(FieldType.EMI_CC_TOTAL_AMOUNT.getName())
								.toString();
						emiCCTotalAmount = Amount.formatAmount(emiCCTotalAmount, currencyCode);
					}

					else if (StringUtils.isNotBlank(newFields.get("paymentsRegion")) && newFields.get("paymentsRegion")
							.equalsIgnoreCase(AccountCurrencyRegion.INTERNATIONAL.toString())) {
						emiCCTotalAmount = request.getSession().getAttribute(FieldType.IN_TOTAL_AMOUNT.getName())
								.toString();
						emiCCTotalAmount = Amount.formatAmount(emiCCTotalAmount, currencyCode);
					} else {
						emiCCTotalAmount = request.getSession().getAttribute(FieldType.EMI_CC_TOTAL_AMOUNT.getName())
								.toString();
						emiCCTotalAmount = Amount.formatAmount(emiCCTotalAmount, currencyCode);
					}
					fields.put(FieldType.TOTAL_AMOUNT.getName(), emiCCTotalAmount);

				} else if (paymentType.equals(PaymentType.EMI_DC.getCode())) {

					String emiDCTotalAmount = "";

					if (StringUtils.isNotBlank(newFields.get("paymentsRegion")) && newFields.get("paymentsRegion")
							.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.toString())) {
						emiDCTotalAmount = request.getSession().getAttribute(FieldType.EMI_DC_TOTAL_AMOUNT.getName())
								.toString();
						emiDCTotalAmount = Amount.formatAmount(emiDCTotalAmount, currencyCode);
					}

					else if (StringUtils.isNotBlank(newFields.get("paymentsRegion")) && newFields.get("paymentsRegion")
							.equalsIgnoreCase(AccountCurrencyRegion.INTERNATIONAL.toString())) {
						emiDCTotalAmount = request.getSession().getAttribute(FieldType.IN_TOTAL_AMOUNT.getName())
								.toString();
						emiDCTotalAmount = Amount.formatAmount(emiDCTotalAmount, currencyCode);
					} else {
						emiDCTotalAmount = request.getSession().getAttribute(FieldType.EMI_DC_TOTAL_AMOUNT.getName())
								.toString();
						emiDCTotalAmount = Amount.formatAmount(emiDCTotalAmount, currencyCode);
					}
					fields.put(FieldType.TOTAL_AMOUNT.getName(), emiDCTotalAmount);

				} else {
					throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
				}
			} else {

				// TDR MODE
				fields.put((FieldType.SURCHARGE_FLAG.getName()), "");
				String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());
				String paymentType = fields.get(FieldType.PAYMENT_TYPE.getName());
				if (paymentType.equals(PaymentType.CREDIT_CARD.getCode())) {

					String ccTotalAmount = Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()), currencyCode);
					fields.put(FieldType.TOTAL_AMOUNT.getName(), ccTotalAmount);

				} else if (paymentType.equals(PaymentType.DEBIT_CARD.getCode())) {

					String dcTotalAmount = Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()), currencyCode);
					fields.put(FieldType.TOTAL_AMOUNT.getName(), dcTotalAmount);

				} else if (paymentType.equals(PaymentType.PREPAID_CARD.getCode())) {

					String pcTotalAmount = Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()), currencyCode);
					fields.put(FieldType.TOTAL_AMOUNT.getName(), pcTotalAmount);

				} else if (paymentType.equals(PaymentType.EMI_CC.getCode())) {

					String emiCCTotalAmount = Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()), currencyCode);
					fields.put(FieldType.TOTAL_AMOUNT.getName(), emiCCTotalAmount);

				} else if (paymentType.equals(PaymentType.EMI_DC.getCode())) {

					String emiDCTotalAmount = Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()), currencyCode);
					fields.put(FieldType.TOTAL_AMOUNT.getName(), emiDCTotalAmount);

				} else if (paymentType.equals(PaymentType.NET_BANKING.getCode())) {
					String nbTotalAmount = request.getSession().getAttribute(FieldType.NB_TOTAL_AMOUNT.getName())
							.toString();
					nbTotalAmount = Amount.formatAmount(nbTotalAmount, currencyCode);
					fields.put(FieldType.TOTAL_AMOUNT.getName(), nbTotalAmount);
				} else if (paymentType.equals(PaymentType.UPI.getCode())) {
					String upiTotalAmount = request.getSession().getAttribute(FieldType.UP_TOTAL_AMOUNT.getName())
							.toString();
					upiTotalAmount = Amount.formatAmount(upiTotalAmount, currencyCode);
					fields.put(FieldType.TOTAL_AMOUNT.getName(), upiTotalAmount);
				}

				// changes by shivanand
				else if (paymentType.equals(PaymentType.WALLET.getCode())) {
					String wlTotalAmount = request.getSession().getAttribute(FieldType.WL_TOTAL_AMOUNT.getName())
							.toString();
					wlTotalAmount = Amount.formatAmount(wlTotalAmount, currencyCode);
					fields.put(FieldType.TOTAL_AMOUNT.getName(), wlTotalAmount);
				} else if (paymentType.equals(PaymentType.COD.getCode())) {
					String cdTotalAmount = request.getSession().getAttribute(FieldType.CD_TOTAL_AMOUNT.getName())
							.toString();
					cdTotalAmount = Amount.formatAmount(cdTotalAmount, currencyCode);
					fields.put(FieldType.TOTAL_AMOUNT.getName(), cdTotalAmount);
				} else if (paymentType.equals(PaymentType.AAMARPAY.getCode())) {
					String apTotalAmount = request.getSession().getAttribute(FieldType.AP_TOTAL_AMOUNT.getName())
							.toString();
					apTotalAmount = Amount.formatAmount(apTotalAmount, currencyCode);
					fields.put(FieldType.TOTAL_AMOUNT.getName(), apTotalAmount);
				} else if (paymentType.equals(PaymentType.CRYPTO.getCode())) {
					String crTotalAmount = request.getSession().getAttribute(FieldType.CR_TOTAL_AMOUNT.getName())
							.toString();
					crTotalAmount = Amount.formatAmount(crTotalAmount, currencyCode);
					fields.put(FieldType.TOTAL_AMOUNT.getName(), crTotalAmount);
				} else {
					throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
				}
				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
			}

			if (Double.valueOf(fields.get(FieldType.TOTAL_AMOUNT.getName()))
					.compareTo(Double.valueOf(fields.get(FieldType.AMOUNT.getName()))) < 0) {
				fields.put(FieldType.TXNTYPE.getName(),
						(String) request.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(),
						(String) request.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));

				Map<String, String> responseFields = transactionControllerServiceProvider.transact(fields,
						Constants.TXN_WS_INTERNAL.getValue());
				Fields field = new Fields(responseFields);

				field.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.BANK_SURCHARGE_REJECTED.getResponseMessage());
				field.put(FieldType.RESPONSE_CODE.getName(), ErrorType.BANK_SURCHARGE_REJECTED.getCode());
				field.put(FieldType.TXNTYPE.getName(), fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));

				transactionResponser.removeInvalidResponseFields(field);
				transactionResponser.addResponseDateTime(field);
				String pgFlag = (String) request.getSession()
						.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
				if (StringUtils.isNotBlank(pgFlag)) {
					fields.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
				}
				field.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
				responseCreator.create(field);
				responseCreator.ResponsePost(field, res);
				// return Action.NONE;

			}

			Map<String, String> response = transactionControllerServiceProvider.transact(fields,
					Constants.TXN_WS_INTERNAL.getValue());
			String pgFlag = (String) request.getSession().getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
			Fields responseMap = new Fields(response);
			// sessionMap.put(Constants.FIELDS.getValue(), responseMap);
			responseMap.logAllFields("Response received from pgws :");
			request.getSession().setAttribute(Constants.FIELDS.getValue(), responseMap);
			request.getSession().setAttribute(FieldType.INTERNAL_ORIG_TXN_ID.getName(),
					responseMap.get(FieldType.TXN_ID.getName()));
			String status = responseMap.get(FieldType.STATUS.getName());
			String txnType = responseMap.get(FieldType.PAYMENT_TYPE.getName());
			String acquirer = responseMap.get(FieldType.ACQUIRER_TYPE.getName());
			request.getSession().setAttribute(FieldType.ACQUIRER_TYPE.getName(), acquirer);
			request.getSession().setAttribute(FieldType.ACQUIRER_TDR_SC.getName(),
					responseMap.get(FieldType.ACQUIRER_TDR_SC.getName()));
			request.getSession().setAttribute(FieldType.ACQUIRER_GST.getName(),
					responseMap.get(FieldType.ACQUIRER_GST.getName()));
			request.getSession().setAttribute(FieldType.PG_GST.getName(), responseMap.get(FieldType.PG_GST.getName()));
			request.getSession().setAttribute(FieldType.PG_TDR_SC.getName(),
					responseMap.get(FieldType.PG_TDR_SC.getName()));

			request.getSession().setAttribute(FieldType.RESELLER_CHARGES.getName(),
					responseMap.get(FieldType.RESELLER_CHARGES.getName()));
			request.getSession().setAttribute(FieldType.RESELLER_GST.getName(),
					responseMap.get(FieldType.RESELLER_GST.getName()));
			request.getSession().setAttribute(FieldType.PG_RESELLER_CHARGE.getName(),
					responseMap.get(FieldType.PG_RESELLER_CHARGE.getName()));
			request.getSession().setAttribute(FieldType.PG_RESELLER_GST.getName(),
					responseMap.get(FieldType.PG_RESELLER_GST.getName()));

			request.getSession().setAttribute(FieldType.ACQUIRER_MODE.getName(),
					responseMap.get(FieldType.ACQUIRER_MODE.getName()));
			request.getSession().setAttribute(FieldType.SLAB_ID.getName(),
					responseMap.get(FieldType.SLAB_ID.getName()));
			if ((status.equals(StatusType.ENROLLED.getName())) || (status.equals(StatusType.CAPTURED.getName()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& txnType.equals(PaymentType.NET_BANKING.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName()) && acquirer.equals(AcquirerType.BOB.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.HDFC.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.KOTAK.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.IDBIBANK.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.FSSPAY.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.BILLDESK.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.PAYU.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.ISGPAY.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.PAYPHI.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.SAFEXPAY.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.APEXPAY.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.VEPAY.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.CASHFREE.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.AIRPAY.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.QAICASH.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.GREZPAY.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.FLOXYPAY.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.DIGITALSOLUTIONS.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.IPINT.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.RAZORPAY.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.UPIGATEWAY.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.GLOBALPAY.getCode()))
					|| (status.equals(StatusType.PENDING.getName()) && txnType.equals(PaymentType.UPI.getName()))) {

				// To check if acquirer is Citrus if acquirer null return
				// nothing and check capture status
				// String acquirer =
				// responseMap.get(FieldType.ACQUIRER_TYPE.getName());
				if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.ICICI_FIRSTDATA.getCode())) {
					request.getSession().setAttribute(FieldType.CVV.getName(), fields.get(FieldType.CVV.getName()));
					requestCreator.FirstDataEnrollRequest(responseMap, res);
				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.IDFC_FIRSTDATA.getCode())) {
					request.getSession().setAttribute(FieldType.CVV.getName(), fields.get(FieldType.CVV.getName()));
					requestCreator.FirstDataEnrollRequest(responseMap, res);
				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.YESBANKCB.getCode())) {
					request.getSession().setAttribute(FieldType.CVV.getName(), fields.get(FieldType.CVV.getName()));
					request.getSession().setAttribute(FieldType.CARD_NUMBER.getName(),
							fields.get(FieldType.CARD_NUMBER.getName()));
					request.getSession().setAttribute(FieldType.CARD_EXP_DT.getName(),
							fields.get(FieldType.CARD_EXP_DT.getName()));
					requestCreator.cyberSourceEnrollRequest(responseMap, res);
				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.AXISBANKCB.getCode())) {
					request.getSession().setAttribute(FieldType.CVV.getName(), fields.get(FieldType.CVV.getName()));
					request.getSession().setAttribute(FieldType.CARD_NUMBER.getName(),
							fields.get(FieldType.CARD_NUMBER.getName()));
					request.getSession().setAttribute(FieldType.CARD_EXP_DT.getName(),
							fields.get(FieldType.CARD_EXP_DT.getName()));
					requestCreator.cyberSourceEnrollRequest(responseMap, res);
				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.CASHFREE.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					requestCreator.generateCashfreeRequest(responseMap, res);
				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.AIRPAY.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					request.getSession().setAttribute(FieldType.ADF1.getName(),
							responseMap.get(FieldType.ADF1.getName()));
					request.getSession().setAttribute(FieldType.ADF2.getName(),
							responseMap.get(FieldType.ADF2.getName()));
					requestCreator.generateAirPayPeRequest(responseMap, res);
				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.QAICASH.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					requestCreator.generateQaicashRequest(responseMap, res);
				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.GLOBALPAY.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					requestCreator.generateGlobalpayRequest(responseMap, res);
				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.GREZPAY.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					request.getSession().setAttribute(FieldType.PASSWORD.getName(),
							responseMap.get(FieldType.PASSWORD.getName()));
					requestCreator.generateGrezpayRequest(responseMap, res);
				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.FLOXYPAY.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					request.getSession().setAttribute(FieldType.PG_REF_NUM.getName(),
							responseMap.get(FieldType.PG_REF_NUM.getName()));
					requestCreator.generateFloxypayRequest(responseMap, res);
				} else if ((!StringUtils.isEmpty(acquirer))
						&& acquirer.equals(AcquirerType.DIGITALSOLUTIONS.getCode())) {
					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					request.getSession().setAttribute(FieldType.PG_REF_NUM.getName(),
							responseMap.get(FieldType.PG_REF_NUM.getName()));
					requestCreator.generateDigitalSolutionRequest(responseMap, res);
				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.RAZORPAY.getCode())) {

					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					requestCreator.generateRazorpayRequest(responseMap, res);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.FSS.getCode())) {
					if (txnType.equals(PaymentType.UPI.getName())) {

					} else {
						requestCreator.EnrollRequest(responseMap, res);
					}

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.FEDERAL.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					requestCreator.generateFederalRequest(responseMap, res);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.SAFEXPAY.getCode())) {

					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.ADF1.getName(),
							responseMap.get(FieldType.ADF1.getName()));
					requestCreator.generateSafexpayRequest(responseMap, res);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.PAYU.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					requestCreator.generatePayuRequest(responseMap, res);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.BOB.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					requestCreator.generateBobRequest(responseMap, res);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.PAYPHI.getCode())) {

					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.PASSWORD.getName(),
							responseMap.get(FieldType.PASSWORD.getName()));
					requestCreator.generatePayphiRequest(responseMap, res);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.APEXPAY.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.PG_REF_NUM.getName(),
							responseMap.get(FieldType.PG_REF_NUM.getName()));
					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					request.getSession().setAttribute(FieldType.PASSWORD.getName(),
							responseMap.get(FieldType.PASSWORD.getName()));
					requestCreator.generateApexPayRequest(responseMap, res);
				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.IPINT.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					requestCreator.generateIPintRequest(responseMap, res);
				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.VEPAY.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.PG_REF_NUM.getName(),
							responseMap.get(FieldType.PG_REF_NUM.getName()));
					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					request.getSession().setAttribute(FieldType.PASSWORD.getName(),
							responseMap.get(FieldType.PASSWORD.getName()));
					requestCreator.generateApexPayRequest(responseMap, res);
				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.HDFC.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					requestCreator.generateHDfcRequest(responseMap, res);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.FSSPAY.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					requestCreator.generateFssPayRequest(responseMap, res);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.UPIGATEWAY.getCode())) {
					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					requestCreator.generateUpigatewayRequest(responseMap, res);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.FSS.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					requestCreator.EnrollRequest(responseMap, res);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.KOTAK.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.PASSWORD.getName(),
							responseMap.get(FieldType.PASSWORD.getName()));
					requestCreator.generateKotakRequest(responseMap, res);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.BILLDESK.getCode())) {
					if (fields.get(FieldType.PAYMENT_TYPE.getName())
							.equalsIgnoreCase(PaymentType.NET_BANKING.getCode())) {
						request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
								responseMap.get(FieldType.TXN_KEY.getName()));
						requestCreator.generateBillDeskRequest(responseMap, res);
					} else {
						requestCreator.billDeskEnrollRequest(responseMap, res);
					}

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.ISGPAY.getCode())) {
					if (fields.get(FieldType.MOP_TYPE.getName()).equalsIgnoreCase(MopType.RUPAY.getCode())
							&& PropertiesManager.propertiesMap.get("selectMidForRupay") != null
							&& PropertiesManager.propertiesMap.get("selectMidForRupay").equalsIgnoreCase("Y")) {
						logger.info("ISGPAY >> Rupay MID Used");
						request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
								responseMap.get(FieldType.ADF8.getName()));
						request.getSession().setAttribute(FieldType.PASSWORD.getName(),
								responseMap.get(FieldType.ADF7.getName()));
					} else {
						logger.info("ISGPAY >> VI MC MID Used");
						request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
								responseMap.get(FieldType.TXN_KEY.getName()));
						request.getSession().setAttribute(FieldType.PASSWORD.getName(),
								responseMap.get(FieldType.PASSWORD.getName()));
					}

					requestCreator.generateIsgpayRequest(responseMap, res);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.IDBIBANK.getCode())) {

					PaymentType paymentType = PaymentType
							.getInstanceUsingCode(fields.get(FieldType.PAYMENT_TYPE.getName()));

					switch (paymentType) {
					case NET_BANKING:
						request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
								responseMap.get(FieldType.TXN_KEY.getName()));
						request.getSession().setAttribute(FieldType.PG_REF_NUM.getName(),
								responseMap.get(FieldType.PG_REF_NUM.getName()));
						requestCreator.generateIdbiRequest(responseMap, res);
						break;

					case CREDIT_CARD:
					case DEBIT_CARD:
						String integrationMode = PropertiesManager.propertiesMap.get("IDBIINTEGRATIONMODE");
						if (StringUtils.isNotBlank(integrationMode) && integrationMode.equals("REDIRECTION")) {
							request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
									responseMap.get(FieldType.TXN_KEY.getName()));
							request.getSession().setAttribute(FieldType.PG_REF_NUM.getName(),
									responseMap.get(FieldType.PG_REF_NUM.getName()));
							requestCreator.generateIdbiRequest(responseMap, res);
						} else {
							request.getSession().setAttribute(FieldType.CVV.getName(),
									fields.get(FieldType.CVV.getName()));
							request.getSession().setAttribute(FieldType.TOKEN_ID.getName(),
									responseMap.get(FieldType.TOKEN_ID.getName()));
							request.getSession().setAttribute(FieldType.CUST_NAME.getName(),
									fields.get(FieldType.CUST_NAME.getName()));
							request.getSession().setAttribute(FieldType.CARD_NUMBER.getName(),
									fields.get(FieldType.CARD_NUMBER.getName()));
							request.getSession().setAttribute(FieldType.CARD_EXP_DT.getName(),
									fields.get(FieldType.CARD_EXP_DT.getName()));
							request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
									responseMap.get(FieldType.TXN_KEY.getName()));
							requestCreator.generateEnrollIdbiRequest(responseMap, res);
						}
						break;
					default:
						break;
					}

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.AXISMIGS.getCode())) {

					requestCreator.sendMigsEnrollTransaction(responseMap, res);

				} else if (!(pendingTxnStatus.contains(status))) {
					request.getSession().invalidate();
					acquirerFlag = "none";
				} else {
					acquirerFlag = "none";
				}
			} else if (status.equals(StatusType.REJECTED.getName()) || status.equals(StatusType.FAILED.getName())) {
				if (request.getSession().getAttribute(FieldType.CHECKOUT_JS_FLAG.getName()).toString()
						.equalsIgnoreCase("Y")) {
					responseMap.put(FieldType.CHECKOUT_JS_FLAG.getName(),
							(String) request.getSession().getAttribute(FieldType.CHECKOUT_JS_FLAG.getName()));
				}
				responseMap.put(FieldType.TXNTYPE.getName(),
						(String) request.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());
				transactionResponser.removeInvalidResponseFields(responseMap);
				transactionResponser.addResponseDateTime(responseMap);

				if (StringUtils.isNotBlank(pgFlag)) {
					responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
				}
				responseMap.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
				responseCreator.create(responseMap);
				responseCreator.ResponsePost(responseMap, res);
				// return Action.NONE;
			} else if (status.equals(StatusType.DENIED_BY_FRAUD.getName())) {
				if (request.getSession().getAttribute(FieldType.CHECKOUT_JS_FLAG.getName()).toString()
						.equalsIgnoreCase("Y")) {
					responseMap.put(FieldType.CHECKOUT_JS_FLAG.getName(),
							(String) request.getSession().getAttribute(FieldType.CHECKOUT_JS_FLAG.getName()));
				}
				responseMap.put(FieldType.TXNTYPE.getName(),
						(String) request.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DENIED_BY_FRAUD.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DENIED_BY_FRAUD.getCode());
				transactionResponser.removeInvalidResponseFields(responseMap);
				transactionResponser.addResponseDateTime(responseMap);

				if (StringUtils.isNotBlank(pgFlag)) {
					responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
				}
				responseMap.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
				responseCreator.create(responseMap);
				responseCreator.ResponsePost(responseMap, res);
				// return Action.NONE;
			} else if (responseMap.get(FieldType.RESPONSE_CODE.getName())
					.equals(ErrorType.DUPLICATE_ORDER_ID.getResponseCode())) {
				if (request.getSession().getAttribute(FieldType.CHECKOUT_JS_FLAG.getName()).toString()
						.equalsIgnoreCase("Y")) {
					responseMap.put(FieldType.CHECKOUT_JS_FLAG.getName(),
							(String) request.getSession().getAttribute(FieldType.CHECKOUT_JS_FLAG.getName()));
				}
				transactionResponser.removeInvalidResponseFields(responseMap);
				transactionResponser.addResponseDateTime(responseMap);

				if (StringUtils.isNotBlank(pgFlag)) {
					responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
				}
				responseMap.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
				responseCreator.create(responseMap);
				responseCreator.ResponsePost(responseMap, res);
				// return Action.NONE;
			} else {
				if (request.getSession().getAttribute(FieldType.CHECKOUT_JS_FLAG.getName()).toString()
						.equalsIgnoreCase("Y")) {
					responseMap.put(FieldType.CHECKOUT_JS_FLAG.getName(),
							(String) request.getSession().getAttribute(FieldType.CHECKOUT_JS_FLAG.getName()));
				}
				responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(),
						(String) request.getSession().getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName()));
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.INTERNAL_SYSTEM_ERROR.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INTERNAL_SYSTEM_ERROR.getCode());
				responseMap.put(FieldType.TXNTYPE.getName(),
						(String) request.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
				transactionResponser.removeInvalidResponseFields(responseMap);
				transactionResponser.addResponseDateTime(responseMap);
				if (StringUtils.isNotBlank(pgFlag)) {
					responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
				}
				responseMap.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
				responseCreator.create(responseMap);
				responseCreator.ResponsePost(responseMap, res);
				// return Action.NONE;
			}

			responseMap.remove(FieldType.PAREQ.getName());
			responseMap.removeSecureFields();
		} catch (

		SystemException systemException) {
			logger.error("systemException", systemException);

		} catch (Exception exception) {
			logger.error("Error handling of transaction", exception);
			String path = request.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = request.getScheme() + "://" + request.getHeader("Host") + "/pgui/jsp/error";
				res.sendRedirect(resultPath);
			}
			res.sendRedirect("error");

		}
		return acquirerFlag;

	}

	public Map<String, String> redirectToPaymentPage(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {
		Map<String, String> redirectResponse = new HashMap<String, String>();
		try {

			if (httpRequest.getParameterMap().size() == 0) {
				redirectResponse.put("timeout", "timeOut");
				return redirectResponse;
			}

			Fields fields = (Fields) httpRequest.getSession().getAttribute(Constants.FIELDS.getValue());

			httpRequest.getSession().removeAttribute(FieldType.PG_TXN_MESSAGE.getName());
			httpRequest.getSession().removeAttribute(FieldType.AUTH_CODE.getName());
			httpRequest.getSession().removeAttribute(FieldType.RESPONSE_CODE.getName());
			httpRequest.getSession().removeAttribute(FieldType.MERCHANT_ID.getName());
			httpRequest.getSession().removeAttribute(FieldType.PG_RESP_CODE.getName());
			httpRequest.getSession().removeAttribute(Constants.FIELDS.getValue());

			fields.remove("PASSWORD");
			fields.remove("RESPONSE_CODE");
			fields.remove("RESPONSE_MESSAGE");
			fields.remove("PG_TXN_MESSAGE");
			fields.remove("STATUS");

			httpRequest.getSession().setAttribute(Constants.FIELDS.getValue(), fields);

		} catch (Exception exception) {
			logger.error("Error handling of transaction", exception);
			// return ERROR;
			String path = httpRequest.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host") + "/pgui/jsp/error";
				httpResponse.sendRedirect(resultPath);
			}
			httpResponse.sendRedirect("error");
		}
		return redirectResponse;
	}

	// Remove Saved Card
	public void deleteSavedCard(HttpServletRequest httpRequest, Map<String, String> reqMap) {
		Fields fields = new Fields();
		fields = retriveSessionFields(httpRequest, reqMap);
		if (null != reqMap.get("tokenId")) {
			fields.put(FieldType.TOKEN_ID.getName(), reqMap.get("tokenId"));

			tokenManager.removeSavedCard(fields, userDao.findPayId(fields.get(FieldType.PAY_ID.getName())));
			tokenMap = tokenManager.getAll(fields, userDao.findPayId(fields.get(FieldType.PAY_ID.getName())));
			if (tokenMap.isEmpty()) {
				httpRequest.getSession().setAttribute(Constants.TOKEN.getValue(), "NA");
			} else {
				httpRequest.getSession().setAttribute(Constants.TOKEN.getValue(), tokenMap);
			}
		}
	}

	// Remove VPA
	public void deleteVpa(HttpServletRequest httpRequest, Map<String, String> reqMap) {
		Fields fields = new Fields();
		fields = retriveSessionFields(httpRequest, reqMap);
		if (null != reqMap.get("tokenId")) {
			fields.put(FieldType.TOKEN_ID.getName(), reqMap.get("tokenId"));
			tokenManager.removeSavedVPA(fields, userDao.findPayId(fields.get(FieldType.PAY_ID.getName())));
			vpaTokenMap = tokenManager.getAllVpa(fields, userDao.findPayId(fields.get(FieldType.PAY_ID.getName())));
			if (vpaTokenMap.isEmpty()) {
				httpRequest.getSession().setAttribute(Constants.VPA_TOKEN.getValue(), "NA");
			} else {
				httpRequest.getSession().setAttribute(Constants.VPA_TOKEN.getValue(), vpaTokenMap);
			}
		}
	}

	// Remove NB token
	public void deleteNbToken(HttpServletRequest httpRequest, Map<String, String> reqMap) {
		Fields fields = new Fields();
		fields = retriveSessionFields(httpRequest, reqMap);
		if (null != reqMap.get("tokenId")) {
			fields.put(FieldType.TOKEN_ID.getName(), reqMap.get("tokenId"));
			tokenManager.removeSavedNbBank(fields, userDao.findPayId(fields.get(FieldType.PAY_ID.getName())));
			nbTokenMap = tokenManager.getAllBank(fields, userDao.findPayId(fields.get(FieldType.PAY_ID.getName())));
			if (nbTokenMap.isEmpty()) {
				httpRequest.getSession().setAttribute(Constants.NB_TOKEN.getValue(), "NA");
			} else {
				httpRequest.getSession().setAttribute(Constants.NB_TOKEN.getValue(), nbTokenMap);
			}
		}
	}

	// Remove Saved wallet token
	public void deleteWlToken(HttpServletRequest httpRequest, Map<String, String> reqMap) {
		Fields fields = new Fields();
		fields = retriveSessionFields(httpRequest, reqMap);
		if (null != reqMap.get("tokenId")) {
			fields.put(FieldType.TOKEN_ID.getName(), reqMap.get("tokenId"));
			tokenManager.removeSavedWallet(fields, userDao.findPayId(fields.get(FieldType.PAY_ID.getName())));
			wlTokenMap = tokenManager.getAllWallet(fields, userDao.findPayId(fields.get(FieldType.PAY_ID.getName())));
			if (wlTokenMap.isEmpty()) {
				httpRequest.getSession().setAttribute(Constants.WL_TOKEN.getValue(), "NA");
			} else {
				httpRequest.getSession().setAttribute(Constants.WL_TOKEN.getValue(), wlTokenMap);
			}
		}
	}

	//
	// for payment modes that are not supported
	public void prepareUnsupportedPaymnetResponse(Fields fields, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws SystemException {
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PAYMENT_OPTION_NOT_SUPPORTED.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PAYMENT_OPTION_NOT_SUPPORTED.getCode());

		transactionResponser.removeInvalidResponseFields(fields);
		transactionResponser.addResponseDateTime(fields);
		fields.removeSecureFields();
		transactionResponser.addHash(fields);
		String pgFlag = (String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
		if (StringUtils.isNotBlank(pgFlag)) {
			fields.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
		}
		fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
		responseCreator.create(fields);
		responseCreator.ResponsePost(fields, httpResponse);
	}

	public boolean isJSONValid(String jsonInString) {
		try {

			if (!jsonInString.contains("{")) {
				return false;
			}
			final ObjectMapper mapper = new ObjectMapper();
			mapper.readTree(jsonInString);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private Fields retriveSessionFields(HttpServletRequest httpRequest, Map<String, String> reqMap) {
		Fields fields = new Fields();
		if (httpRequest.getSession().getAttribute("FIELDS").getClass().getSimpleName().toString()
				.equalsIgnoreCase("Fields")) {
			fields = (Fields) httpRequest.getSession().getAttribute("FIELDS");
		} else {
			Map<String, String> fieldsMap = new HashMap<String, String>();
			String sessionFields = (String) httpRequest.getSession().getAttribute(Constants.FIELDS.getValue());
			sessionFields = sessionFields.substring(1, sessionFields.length() - 1);
			String[] fieldArray = sessionFields.split(",");
			for (String key : fieldArray) {
				if (key.charAt(0) == ' ') {
					key = key.replaceFirst("^\\s*", "");
				}
				String[] namValuePair = key.split("=", 2);
				fieldsMap.put(namValuePair[0], namValuePair[1]);
			}
			fields = new Fields(fieldsMap);
			logger.info(fields.toString());
		}
		return fields;
	}

	@SuppressWarnings("unused")
	private static String encode(String url) {
		try {
			String encodeURL = URLEncoder.encode(url, "UTF-8");
			return encodeURL;
		} catch (UnsupportedEncodingException e) {
			return "Issue while encoding" + e.getMessage();
		}
	}

	private static String decode(String url) {
		try {
			String prevURL = "";
			String decodeURL = url;
			while (!prevURL.equals(decodeURL)) {
				prevURL = decodeURL;
				decodeURL = URLDecoder.decode(decodeURL, "UTF-8");
			}
			return decodeURL;
		} catch (UnsupportedEncodingException e) {
			return "Issue while decoding" + e.getMessage();
		}
	}

	public static String generateGUID() {
		return new BigInteger(165, RANDOM).toString(36).toUpperCase();
	}

	public void log(String message) {
		message = Pattern.compile("(encSessionData\":\")([\\s\\S]*?)(\",\")").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(suportedPaymentTypeMap\":\")([\\s\\S]*?)(\",\")").matcher(message)
				.replaceAll("$1$3");
		logger.info(message);
	}

	public Map<String, Object> getSupportedPaymentTypeMap() {
		return supportedPaymentTypeMap;
	}

	public void setSupportedPaymentTypeMap(Map<String, Object> supportedPaymentTypeMap) {
		this.supportedPaymentTypeMap = supportedPaymentTypeMap;
	}

	public Map<String, Object> getCardPaymentTypeMap() {
		return cardPaymentTypeMap;
	}

	public void setCardPaymentTypeMap(Map<String, Object> cardPaymentTypeMap) {
		this.cardPaymentTypeMap = cardPaymentTypeMap;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getPaymentFlow() {
		return paymentFlow;
	}

	public void setPaymentFlow(String paymentFlow) {
		this.paymentFlow = paymentFlow;
	}

}
