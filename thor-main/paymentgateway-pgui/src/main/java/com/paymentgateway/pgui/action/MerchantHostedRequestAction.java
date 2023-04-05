package com.paymentgateway.pgui.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.BindbControllerServiceProvider;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.dao.PaymentOptionsDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.PaymentOptions;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CountryCodes;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MerchantPaymentType;
import com.paymentgateway.commons.util.MerchantTypeService;
import com.paymentgateway.commons.util.ModeType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StaticDataProvider;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.UserStatusType;
import com.paymentgateway.oneclick.TokenManager;
import com.paymentgateway.pg.core.pageintegrator.GeneralValidator;
import com.paymentgateway.pg.core.util.CalculateSurchargeAmount;
import com.paymentgateway.pg.core.util.RequestCreator;
import com.paymentgateway.pg.core.util.ResponseCreator;
import com.paymentgateway.pg.core.util.TransactionResponser;
import com.paymentgateway.pg.core.util.UpdateProcessor;
import com.paymentgateway.pgui.action.service.ActionService;

/**
 * @author Rahul
 *
 */
@Service
public class MerchantHostedRequestAction {

	private static Logger logger = LoggerFactory.getLogger(MerchantHostedRequestAction.class.getName());

	private static BigDecimal minAmountSlab2 = new BigDecimal(
			PropertiesManager.propertiesMap.get("LimitSlab2MinAmount"));
	private static BigDecimal minAmountSlab3 = new BigDecimal(
			PropertiesManager.propertiesMap.get("LimitSlab3MinAmount"));
	private static final Random RANDOM = new SecureRandom();

	@Autowired
	private BindbControllerServiceProvider binService;

	@Autowired
	private ResponseCreator responseCreator;
	@Autowired
	TransactionResponser transactionResponser;
	@Autowired
	private RequestCreator requestCreator;
	@Autowired
	private GeneralValidator generalValidator;
	@Autowired
	private UserDao userDao;
	@Autowired
	private FieldsDao fieldsDao;
	@Autowired
	private PropertiesManager propertiesManager;
	@Autowired
	private UpdateProcessor updateProcessor;
	@Autowired
	private CalculateSurchargeAmount calculateSurchargeAmount;
	@Autowired
	private TokenManager tokenManager;
	@Autowired
	private StaticDataProvider staticDataProvider;
	@Autowired
	private ActionService actionService;
	@Autowired
	private PaymentOptionsDao paymentOptionsDao;
	@Autowired
	private UserSettingDao userSettingDao;
	@Autowired
	private MerchantTypeService merchantTypeService;

	private String acquirerFlag;
	private String resellerId;
	private static final String pendingTxnStatus = "Sent to Bank-Enrolled";

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	public Map<String, String> merchantHostedPaymentPageRequest(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Map<String, String> merchantHostedResponse = new HashMap<String, String>();
		try {
			// clean session
			request.getSession().invalidate();

			// create new fields for direct transaction; decrypt fields
			Fields fields = actionService.prepareFieldsMerchantHosted(request.getParameterMap());
			fields.logAllFields("Payment Request received ");
			if (StringUtils.isBlank(fields.get(FieldType.HASH.getName()))) {
				throw new SystemException(ErrorType.VALIDATION_FAILED, "Invalid " + FieldType.HASH.getName());
			}

			// Add IP Address to Fields
			if (StringUtils.isNotBlank(request.getHeader("X-Forwarded-For"))) {
				fields.put((FieldType.INTERNAL_CUST_IP.getName()), request.getHeader("X-Forwarded-For").split(",")[0]);
				request.getSession().setAttribute((FieldType.INTERNAL_CUST_IP.getName()),
						request.getHeader("X-Forwarded-For").split(",")[0]);
				
				logger.info("Payment Request received from IP " + request.getHeader("X-Forwarded-For") );
			}
			
			String fieldsAsString = fields.getFieldsAsBlobString();
			request.getSession().setAttribute(Constants.FIELDS.getValue(), fields);
			generalValidator.validateHash(fields);
			fields.put(FieldType.INTERNAL_REQUEST_FIELDS.getName(), fieldsAsString);
			generalValidator.validateReturnUrl(fields);
			generalValidator.validateMandatoryFields(fields, getMandatoryStatusRequestFields());
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
			}

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
					fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.USER_INACTIVE.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.USER_INACTIVE.getCode());
					fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
					fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
					responseCreator.ResponsePost(fields, response);
//					return Action.NONE;
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
					fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(),
							ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getCode());
					fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
					fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
					responseCreator.InvalidUserResponsePost(fields, response);
//					return Action.NONE;
				}

				if (subMerchant.isSuperMerchant() && StringUtils.isNotBlank(subMerchant.getSuperMerchantId())) {

					fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REJECTED_BY_PG.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED_BY_PG.getCode());
					fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
					fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
					responseCreator.ResponsePost(fields, response);
//					return Action.NONE;
				}

				User supermerchant = null;
				if (!subMerchant.isSuperMerchant() && StringUtils.isNotBlank(subMerchant.getSuperMerchantId())) {
					superMerchantId = subMerchant.getSuperMerchantId();
					subMerchantId = subMerchant.getPayId();
					isSubMerchant = true;
					supermerchant = userDao.findPayId(superMerchantId);
				}

				if (supermerchant != null && supermerchant.getUserStatus() != UserStatusType.ACTIVE) {
					fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.USER_INACTIVE.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.USER_INACTIVE.getCode());
					fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
					fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
					responseCreator.ResponsePost(fields, response);
//					return Action.NONE;
				}

				if (subMerchant.getUserStatus() != UserStatusType.ACTIVE) {
					fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REJECTED_BY_PG.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED_BY_PG.getCode());
					fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
					fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
					responseCreator.ResponsePost(fields, response);
//					return Action.NONE;
				}
			}

			if (StringUtils.isBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
				fields.remove(FieldType.SUB_MERCHANT_ID.getName());
			}

			// Add Sub Merchant Id Again for keeping transaction records for sub merchant
			if (StringUtils.isNotBlank(subMerchantId)) {
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
			}

			// Update PayId to super merchant id and add sub merchant payid as
			// SUB_MERCHANT_ID for record
			if (isSubMerchant) {
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
				fields.put(FieldType.PAY_ID.getName(), superMerchantId);
				request.getSession().setAttribute(FieldType.SUPER_MERCHANT_ID.getName(), superMerchantId);
			}

			User user = userDao.getUserClass(fields.get(FieldType.PAY_ID.getName()));

			UserSettingData merchntSettings = userSettingDao
					.fetchDataUsingPayId(fields.get(FieldType.PAY_ID.getName()));

			if (merchntSettings.isCheckOutJsFlag() == true) {
				request.getSession().setAttribute(FieldType.CHECKOUT_JS_FLAG.getName(), "Y");
			} else {
				request.getSession().setAttribute(FieldType.CHECKOUT_JS_FLAG.getName(), "N");
			}

			// Return URL whitelisting
			boolean returnUrlWhitelistingFlag = merchntSettings.isWhiteListReturnUrlFlag();
			if (returnUrlWhitelistingFlag) {
				String whitelistedURL = merchntSettings.getWhiteListReturnUrl();
				if (!whitelistedURL.equals(fields.get(FieldType.RETURN_URL.getName()))) {
					fields.put(FieldType.TXNTYPE.getName(),
							ModeType.getDefaultPurchaseTransaction(user.getModeType()).getName());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REJECTED_BY_PG.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED_BY_PG.getCode());
					fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
					fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
					responseCreator.ResponsePost(fields, response);
//								return Action.NONE;
				}
			}
			MerchantPaymentType merchantPaymentType = MerchantPaymentType
					.getInstanceFromCode(fields.get(FieldType.PAYMENT_TYPE.getName()));
			PaymentOptions paymentOption = new PaymentOptions();
			if (fields.contains(FieldType.SUB_MERCHANT_ID.getName())
					&& StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
				paymentOption = paymentOptionsDao.getPaymentOption(fields.get(FieldType.SUB_MERCHANT_ID.getName()));
			} else {
				paymentOption = paymentOptionsDao.getPaymentOption(fields.get(FieldType.PAY_ID.getName()));
			}
			// unsupported/invalid payment type received from merchant
			if (null == merchantPaymentType || null == paymentOption) {
				throw new SystemException(ErrorType.VALIDATION_FAILED, "Invalid payment type");
			}
			if (fields.get(FieldType.PAY_ID.getName())
					.equalsIgnoreCase(PropertiesManager.propertiesMap.get("MSEDCL_PAY_ID"))) {
				fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName())));
				request.getSession().setAttribute(FieldType.AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
				Date dNow = new Date();
				String dateNow = DateCreater.formatDateForDb(dNow);
				String[] prodDesc = fields.get(FieldType.PRODUCT_DESC.getName()).split("_");
				fields.put(FieldType.CUST_ID.getName(), prodDesc[0]);
				fields.put(FieldType.UDF12.getName(), prodDesc[1]);
				fields.put(FieldType.UDF13.getName(), prodDesc[2]);
				fields.put(FieldType.UDF17.getName(), prodDesc[3]);
				fields.put(FieldType.UDF11.getName(), prodDesc[4]);
				fields.put(FieldType.UDF14.getName(), prodDesc[5]);
				fields.put(FieldType.UDF15.getName(), prodDesc[6]);
				fields.put(FieldType.UDF16.getName(), dateNow.substring(0, 10).replace("-", ""));
				request.getSession().setAttribute(FieldType.CUST_ID.getName(), fields.get(FieldType.CUST_ID.getName()));
				request.getSession().setAttribute(FieldType.UDF12.getName(), fields.get(FieldType.UDF12.getName()));
				request.getSession().setAttribute(FieldType.UDF13.getName(), fields.get(FieldType.UDF13.getName()));
				request.getSession().setAttribute(FieldType.UDF17.getName(), fields.get(FieldType.UDF17.getName()));
				request.getSession().setAttribute(FieldType.UDF11.getName(), fields.get(FieldType.UDF11.getName()));
				request.getSession().setAttribute(FieldType.UDF14.getName(), fields.get(FieldType.UDF14.getName()));
				request.getSession().setAttribute(FieldType.UDF15.getName(), fields.get(FieldType.UDF15.getName()));
				request.getSession().setAttribute(FieldType.UDF16.getName(), fields.get(FieldType.UDF16.getName()));
			}
			request.getSession().setAttribute((FieldType.SURCHARGE_FLAG.getName()),
					((merchntSettings.isSurchargeFlag()) ? "Y" : "N"));
			request.getSession().setAttribute(Constants.CUSTOM_TOKEN.getValue(), generateGUID());
			merchantHostedResponse.put(Constants.CUSTOM_TOKEN.getValue(),
					(String) request.getSession().getAttribute(Constants.CUSTOM_TOKEN.getValue()));
			fields.put((FieldType.SURCHARGE_FLAG.getName()), ((merchntSettings.isSurchargeFlag()) ? "Y" : "N"));
			request.getSession().setAttribute((FieldType.TOTAL_AMOUNT.getName()),
					fields.get(FieldType.AMOUNT.getName()));
			fields.put((FieldType.TOTAL_AMOUNT.getName()), fields.get(FieldType.AMOUNT.getName()));
			// Map payment type from merchant to PG payment type
			switch (merchantPaymentType) {
			case CARD:
				// validating payment option for card is enable for merchant or not
				if (!paymentOption.isCreditCard() || !paymentOption.isDebitCard()) {
					throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
				}
				// do bin check, card validation and assign mop
				generalValidator.validateCardNumber(fields);
				Map<String, String> binMap = binService.binfind(
						fields.get(FieldType.CARD_NUMBER.getName()).substring(0, 9),
						fields.get(FieldType.PAY_ID.getName()));
				if (null == binMap || binMap.isEmpty()) {
					throw new SystemException(ErrorType.CARD_NUMBER_NOT_SUPPORTED,
							"Bin not present for card, OrderId: " + fields.get(FieldType.ORDER_ID.getName()));
				}
				fields.put(FieldType.PAYMENT_TYPE.getName(), binMap.get(FieldType.PAYMENT_TYPE.getName()));
				fields.put(FieldType.MOP_TYPE.getName(), binMap.get(FieldType.MOP_TYPE.getName()));
				fields.put(FieldType.PAYMENTS_REGION.getName(), binMap.get(FieldType.PAYMENTS_REGION.getName()));
				fields.put(FieldType.CARD_HOLDER_TYPE.getName(), binMap.get(FieldType.CARD_HOLDER_TYPE.getName()));
				fields.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(),
						binMap.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));
				fields.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(),
						binMap.get(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()));
				fields.put(FieldType.TXNTYPE.getName(), TransactionType.ENROLL.getName());
				fields.put(FieldType.CUST_NAME.getName(), fields.get(FieldType.CARD_HOLDER_NAME.getName()));
				request.getSession().setAttribute((FieldType.CARD_NUMBER.getName()),
						fields.get(FieldType.CARD_NUMBER.getName()));
				request.getSession().setAttribute((FieldType.CARD_EXP_DT.getName()),
						fields.get(FieldType.CARD_EXP_DT.getName()));

				request.getSession().setAttribute((FieldType.CVV.getName()), fields.get(FieldType.CVV.getName()));
				request.getSession().setAttribute((FieldType.INTERNAL_CARD_ISSUER_BANK.getName()),
						fields.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));
				request.getSession().setAttribute((FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()),
						fields.get(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()));
				request.getSession().setAttribute((FieldType.PAYMENTS_REGION.getName()),
						binMap.get(FieldType.PAYMENTS_REGION.getName()));
				request.getSession().setAttribute((FieldType.CARD_HOLDER_TYPE.getName()),
						fields.get(FieldType.CARD_HOLDER_TYPE.getName()));
				request.getSession().setAttribute(FieldType.CUST_NAME.getName(),
						fields.get(FieldType.CARD_HOLDER_NAME.getName()));
				// Save Card detail for Express Payment
				if ((!StringUtils.isEmpty(fields.get(FieldType.SAVED_CARD_FLAG.getName())))
						&& fields.get(FieldType.SAVED_CARD_FLAG.getName()).equalsIgnoreCase("true")) {
					if (merchntSettings.isExpressPayFlag()) {
						if (StringUtils.isBlank(merchntSettings.getCardSaveParam())
								&& StringUtils.isBlank(fields.get(merchntSettings.getCardSaveParam()))) {
							tokenManager.addToken(fields, user);
						}
					}

				}

				break;
			case TOKEN:
				break;
			case NET_BANKING:
				// validating payment option for NetBanking is enable for merchant or not
				if (!paymentOption.isNetBanking()) {
					throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
				}
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.PAYMENT_TYPE.getName(), PaymentType.NET_BANKING.getCode());
				fields.put(FieldType.CARD_HOLDER_TYPE.getName(), CardHolderType.CONSUMER.toString());
				fields.put(FieldType.PAYMENTS_REGION.getName(), AccountCurrencyRegion.DOMESTIC.toString());
				// fields.put(FieldType.MOP_TYPE.getName(),
				// MopType.getmopName(fields.get(FieldType.MOP_TYPE.getName())));
				request.getSession().setAttribute((FieldType.CARD_HOLDER_TYPE.getName()),
						CardHolderType.CONSUMER.toString());
				request.getSession().setAttribute((FieldType.PAYMENTS_REGION.getName()),
						AccountCurrencyRegion.DOMESTIC.toString());
				break;
			case UPI:
				// validating payment option for UPI is enable for merchant or not
				if (!paymentOption.isUpi()) {
					throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
				}
				fields.put(FieldType.PAYMENT_TYPE.getName(), PaymentType.UPI.getCode());
				fields.put(FieldType.MOP_TYPE.getName(), MopType.UPI.getCode());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.CARD_HOLDER_TYPE.getName(), CardHolderType.CONSUMER.toString());
				fields.put(FieldType.PAYMENTS_REGION.getName(), AccountCurrencyRegion.DOMESTIC.toString());
				request.getSession().setAttribute((FieldType.CARD_HOLDER_TYPE.getName()),
						CardHolderType.CONSUMER.toString());
				request.getSession().setAttribute((FieldType.PAYMENTS_REGION.getName()),
						AccountCurrencyRegion.DOMESTIC.toString());
				request.getSession().setAttribute((FieldType.PAYER_ADDRESS.getName()),
						fields.get(FieldType.PAYER_ADDRESS.getName()));
				break;
			case WALLET:
				// validating payment option for Wallet is enable for merchant or not
				if (!paymentOption.isWallet()) {
					throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
				}
				fields.put(FieldType.PAYMENT_TYPE.getName(), PaymentType.WALLET.getCode());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.CARD_HOLDER_TYPE.getName(), CardHolderType.CONSUMER.toString());
				fields.put(FieldType.PAYMENTS_REGION.getName(), AccountCurrencyRegion.DOMESTIC.toString());
				request.getSession().setAttribute((FieldType.CARD_HOLDER_TYPE.getName()),
						CardHolderType.CONSUMER.toString());
				request.getSession().setAttribute((FieldType.PAYMENTS_REGION.getName()),
						AccountCurrencyRegion.DOMESTIC.toString());
				break;
			case COD:
				// validating payment option for cod is enable for merchant or not
				if (!paymentOption.isCashOnDelivery()) {
					throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
				}
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
			}

			String origTxnType = ModeType.getDefaultPurchaseTransaction(user.getModeType()).getName();
			request.getSession().setAttribute((FieldType.INTERNAL_ORIG_TXN_TYPE.getName()),
					ModeType.getDefaultPurchaseTransaction(user.getModeType()).getName());
			fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), origTxnType);

			// put original transaction type
			request.getSession().setAttribute((FieldType.RETURN_URL.getName()),
					fields.get(FieldType.RETURN_URL.getName()));

			if (StringUtils.isNotBlank(request.getHeader("X-Forwarded-For"))) {
				fields.put((FieldType.INTERNAL_CUST_IP.getName()), request.getHeader("X-Forwarded-For").split(",")[0]);
				request.getSession().setAttribute((FieldType.INTERNAL_CUST_IP.getName()),
						request.getHeader("X-Forwarded-For").split(",")[0]);
			}
			request.getSession().setAttribute((FieldType.INTERNAL_HEADER_ACEEPT.getName()),
					request.getHeader("Accept"));
			request.getSession().setAttribute((FieldType.INTERNAL_HEADER_USER_AGENT.getName()),
					request.getHeader("User-Agent"));
			fields.put((FieldType.INTERNAL_HEADER_ACEEPT.getName()), request.getHeader("Accept"));
			fields.put((FieldType.INTERNAL_HEADER_USER_AGENT.getName()), request.getHeader("User-Agent"));
			String countryCode = request.getHeader("CloudFront-Viewer-Country");
			logger.info("Header value " + request.getHeader("User-Agent"));
			logger.info("CloudFront-Is-Mobile-Viewer    " + request.getHeader("CloudFront-Is-Mobile-Viewer"));
			logger.info("CloudFront-Is-Tablet-Viewer    " + request.getHeader("CloudFront-Is-Tablet-Viewer"));
			logger.info("CloudFront-Is-SmartTV-Viewer    " + request.getHeader("CloudFront-Is-SmartTV-Viewer"));
			logger.info("CloudFront-Is-Desktop-Viewer    " + request.getHeader("CloudFront-Is-Desktop-Viewer"));
			if (StringUtils.isBlank(countryCode)) {
				countryCode = "NA";
			}

			fields.put((FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()), CountryCodes.getCountryName(countryCode));
			request.getSession().setAttribute(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName(),
					CountryCodes.getCountryName(countryCode));

			fields.put(FieldType.INTERNAL_REQUEST_FIELDS.getName(), fieldsAsString);
			fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), origTxnType);
			fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
			// Put merchant hosted page flag
			fields.put(FieldType.IS_MERCHANT_HOSTED.getName(), "Y");
			request.getSession().setAttribute(FieldType.IS_MERCHANT_HOSTED.getName(), "Y");
			// Put transaction id as original txnId for subsequent transactions
			fields.put(FieldType.INTERNAL_ORIG_TXN_ID.getName(), fields.get(FieldType.ORIG_TXN_ID.getName()));
			fields.remove(FieldType.ORIG_TXN_ID.getName());

			// Put ORIG_TXN_ID in session for updating transaction status in case of timeout
			request.getSession().setAttribute(FieldType.INTERNAL_ORIG_TXN_ID.getName(),
					fields.get(FieldType.INTERNAL_ORIG_TXN_ID.getName()));
			if (StringUtils.isNotBlank(user.getResellerId())) {
				resellerId = user.getResellerId();
			}
			// handle surcharge flg and amount

			handleTransactionCharges(fields, request);

			Map<String, String> responseFields = transactionControllerServiceProvider.transact(fields,
					Constants.TXN_WS_INTERNAL.getValue());
			Fields responseMap = new Fields(responseFields);
			// sessionMap.put(Constants.FIELDS.getValue(), responseMap);
			responseMap.logAllFields("Response received from pgws :");
			request.getSession().setAttribute(Constants.FIELDS.getValue(), responseMap);
			request.getSession().setAttribute(FieldType.INTERNAL_ORIG_TXN_ID.getName(),
					responseMap.get(FieldType.TXN_ID.getName()));
			request.getSession().setAttribute(FieldType.PG_REF_NUM.getName(),
					responseMap.get(FieldType.PG_REF_NUM.getName()));
			request.getSession().setAttribute(FieldType.OID.getName(), responseMap.get(FieldType.PG_REF_NUM.getName()));
			request.getSession().setAttribute(FieldType.ORDER_ID.getName(),
					responseMap.get(FieldType.ORDER_ID.getName()));
			responseMap.put(FieldType.IS_MERCHANT_HOSTED.getName(), "Y");
			String status = responseMap.get(FieldType.STATUS.getName());
			String txnType = responseMap.get(FieldType.PAYMENT_TYPE.getName());
			String acquirer = responseMap.get(FieldType.ACQUIRER_TYPE.getName());
			if (StringUtils.isNotBlank(status) && StringUtils.isNotBlank(acquirer)
					&& txnType.equalsIgnoreCase(PaymentType.UPI.getCode())
					&& status.equals(StatusType.SENT_TO_BANK.getName())
					&& !acquirer.equalsIgnoreCase(AcquirerType.PAYPHI.getCode())
					&& !acquirer.equalsIgnoreCase(AcquirerType.BOB.getCode())
					&& !acquirer.equalsIgnoreCase(AcquirerType.PAYU.getCode())
					&& !acquirer.equalsIgnoreCase(AcquirerType.FSSPAY.getCode())
					&& !acquirer.equalsIgnoreCase(AcquirerType.SAFEXPAY.getCode())
					&& !acquirer.equalsIgnoreCase(AcquirerType.APEXPAY.getCode())
					&& !acquirer.equalsIgnoreCase(AcquirerType.AIRPAY.getCode())
					&& !acquirer.equalsIgnoreCase(AcquirerType.QAICASH.getCode())
					&& !acquirer.equalsIgnoreCase(AcquirerType.GLOBALPAY.getCode())
					&& !acquirer.equalsIgnoreCase(AcquirerType.DIGITALSOLUTIONS.getCode())
					&& !acquirer.equalsIgnoreCase(AcquirerType.FLOXYPAY.getCode())
					&& !acquirer.equalsIgnoreCase(AcquirerType.RAZORPAY.getCode())
					&& !acquirer.equalsIgnoreCase(AcquirerType.VEPAY.getCode())
					&& !acquirer.equalsIgnoreCase(AcquirerType.GREZPAY.getCode())
					&& !acquirer.equalsIgnoreCase(AcquirerType.UPIGATEWAY.getCode())) {
				merchantHostedResponse.put(FieldType.PG_REF_NUM.getName(),
						responseMap.get(FieldType.PG_REF_NUM.getName()));
				merchantHostedResponse.put(FieldType.RESPONSE_CODE.getName(),
						responseMap.get(FieldType.RESPONSE_CODE.getName()));
				merchantHostedResponse.put(FieldType.PAY_ID.getName(), responseMap.get(FieldType.PAY_ID.getName()));
				merchantHostedResponse.put("upiLoader", "Y");
				return merchantHostedResponse;

//				return "upiLoader";
			}
			request.getSession().setAttribute(FieldType.ACQUIRER_TYPE.getName(), acquirer);
			if (StringUtils.isNotBlank(status) && StringUtils.isNotBlank(acquirer) && ((status
					.equals(StatusType.ENROLLED.getName()))
					|| (status.equals(StatusType.CAPTURED.getName()))
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
							&& acquirer.equals(AcquirerType.ISGPAY.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.PAYPHI.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.PAYU.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.CASHFREE.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.SAFEXPAY.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.APEXPAY.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.VEPAY.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.AIRPAY.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.QAICASH.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.GLOBALPAY.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.GREZPAY.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.DIGITALSOLUTIONS.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.FLOXYPAY.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.IPINT.getCode()))
					|| (status.equals(StatusType.SENT_TO_BANK.getName())
							&& acquirer.equals(AcquirerType.UPIGATEWAY.getCode()))
					|| (status.equals(StatusType.PENDING.getName()) && txnType.equals(PaymentType.UPI.getName())))) {

				// To check if acquirer is Citrus if acquirer null return
				// nothing and check capture status
				// String acquirer =
				// responseMap.get(FieldType.ACQUIRER_TYPE.getName());
				if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.ICICI_FIRSTDATA.getCode())) {
					request.getSession().setAttribute(FieldType.CVV.getName(), fields.get(FieldType.CVV.getName()));
					requestCreator.FirstDataEnrollRequest(responseMap, response);
				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.IDFC_FIRSTDATA.getCode())) {
					request.getSession().setAttribute(FieldType.CVV.getName(), fields.get(FieldType.CVV.getName()));
					requestCreator.FirstDataEnrollRequest(responseMap, response);
				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.YESBANKCB.getCode())) {
					request.getSession().setAttribute(FieldType.CVV.getName(), fields.get(FieldType.CVV.getName()));
					request.getSession().setAttribute(FieldType.CARD_NUMBER.getName(),
							fields.get(FieldType.CARD_NUMBER.getName()));
					request.getSession().setAttribute(FieldType.CARD_EXP_DT.getName(),
							fields.get(FieldType.CARD_EXP_DT.getName()));
					requestCreator.cyberSourceEnrollRequest(responseMap, response);
				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.PAYU.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					requestCreator.generatePayuRequest(responseMap, response);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.CASHFREE.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					requestCreator.generateCashfreeRequest(responseMap, response);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.SAFEXPAY.getCode())) {

					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.ADF1.getName(),
							responseMap.get(FieldType.ADF1.getName()));
					requestCreator.generateSafexpayRequest(responseMap, response);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.FSS.getCode())) {
					if (txnType.equals(PaymentType.UPI.getName())) {

					} else {
						requestCreator.EnrollRequest(responseMap, response);
					}

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.FEDERAL.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					requestCreator.generateFederalRequest(responseMap, response);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.BOB.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					requestCreator.generateBobRequest(responseMap, response);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.HDFC.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					requestCreator.generateHDfcRequest(responseMap, response);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.APEXPAY.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					request.getSession().setAttribute(FieldType.PASSWORD.getName(),
							responseMap.get(FieldType.PASSWORD.getName()));
					requestCreator.generateApexPayRequest(responseMap, response);
				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.VEPAY.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					request.getSession().setAttribute(FieldType.PASSWORD.getName(),
							responseMap.get(FieldType.PASSWORD.getName()));
					requestCreator.generateApexPayRequest(responseMap, response);
				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.AIRPAY.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					request.getSession().setAttribute(FieldType.ADF1.getName(),
							responseMap.get(FieldType.ADF1.getName()));
					request.getSession().setAttribute(FieldType.ADF2.getName(),
							responseMap.get(FieldType.ADF2.getName()));
					requestCreator.generateAirPayPeRequest(responseMap, response);
				}
				else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.QAICASH.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					requestCreator.generateQaicashRequest(responseMap, response);
				}
				else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.GLOBALPAY.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					requestCreator.generateGlobalpayRequest(responseMap, response);
				}
				else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.GREZPAY.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					request.getSession().setAttribute(FieldType.PASSWORD.getName(),
							responseMap.get(FieldType.PASSWORD.getName()));
					requestCreator.generateGrezpayRequest(responseMap, response);
				}
				else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.UPIGATEWAY.getCode())) {
					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					requestCreator.generateUpigatewayRequest(responseMap, response);
				}
				else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.DIGITALSOLUTIONS.getCode())) {
					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					requestCreator.generateDigitalSolutionRequest(responseMap, response);
				}
				else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.FLOXYPAY.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					request.getSession().setAttribute(FieldType.PG_REF_NUM.getName(),
							responseMap.get(FieldType.PG_REF_NUM.getName()));
					requestCreator.generateFloxypayRequest(responseMap, response);
				}
				else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.RAZORPAY.getCode())) {

					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					requestCreator.generateRazorpayRequest(responseMap, response);
				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.FSSPAY.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					requestCreator.generateFssPayRequest(responseMap, response);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.AXISBANKCB.getCode())) {
					request.getSession().setAttribute(FieldType.CVV.getName(), fields.get(FieldType.CVV.getName()));
					request.getSession().setAttribute(FieldType.CARD_NUMBER.getName(),
							fields.get(FieldType.CARD_NUMBER.getName()));
					request.getSession().setAttribute(FieldType.CARD_EXP_DT.getName(),
							fields.get(FieldType.CARD_EXP_DT.getName()));
					requestCreator.cyberSourceEnrollRequest(responseMap, response);
				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.FSS.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					requestCreator.EnrollRequest(responseMap, response);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.IPINT.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					 requestCreator.generateIPintRequest(responseMap,response);

				}else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.KOTAK.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.PASSWORD.getName(),
							responseMap.get(FieldType.PASSWORD.getName()));
					requestCreator.generateKotakRequest(responseMap, response);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.BILLDESK.getCode())) {
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					requestCreator.generateBillDeskRequest(responseMap, response);

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

					requestCreator.generateIsgpayRequest(responseMap, response);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.PAYPHI.getCode())) {

					request.getSession().setAttribute(FieldType.MERCHANT_ID.getName(),
							responseMap.get(FieldType.MERCHANT_ID.getName()));
					request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
							responseMap.get(FieldType.TXN_KEY.getName()));
					request.getSession().setAttribute(FieldType.PASSWORD.getName(),
							responseMap.get(FieldType.PASSWORD.getName()));
					requestCreator.generatePayphiRequest(responseMap, response);

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.IDBIBANK.getCode())) {

					PaymentType paymentType = PaymentType
							.getInstanceUsingCode(fields.get(FieldType.PAYMENT_TYPE.getName()));

					switch (paymentType) {
					case NET_BANKING:
						request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
								responseMap.get(FieldType.TXN_KEY.getName()));
						request.getSession().setAttribute(FieldType.PG_REF_NUM.getName(),
								responseMap.get(FieldType.PG_REF_NUM.getName()));
						requestCreator.generateIdbiRequest(responseMap, response);
						break;

					case CREDIT_CARD:
					case DEBIT_CARD:
						String integrationMode = PropertiesManager.propertiesMap.get("IDBIINTEGRATIONMODE");
						if (StringUtils.isNotBlank(integrationMode) && integrationMode.equals("REDIRECTION")) {
							request.getSession().setAttribute(FieldType.TXN_KEY.getName(),
									responseMap.get(FieldType.TXN_KEY.getName()));
							request.getSession().setAttribute(FieldType.PG_REF_NUM.getName(),
									responseMap.get(FieldType.PG_REF_NUM.getName()));
							requestCreator.generateIdbiRequest(responseMap, response);
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
							requestCreator.generateEnrollIdbiRequest(responseMap, response);
						}
						break;
					default:
						break;
					}

				} else if ((!StringUtils.isEmpty(acquirer)) && acquirer.equals(AcquirerType.AXISMIGS.getCode())) {

					requestCreator.sendMigsEnrollTransaction(responseMap, response);

				} else if (!(pendingTxnStatus.contains(status))) {
					request.getSession().invalidate();
					acquirerFlag = "NONE";
				} else {
					acquirerFlag = "NONE";
				}
			} else if (StringUtils.isNotBlank(status) && status.equals(StatusType.REJECTED.getName())
					|| StringUtils.isNotBlank(status) && status.equals(StatusType.FAILED.getName())) {

				responseMap.put(FieldType.TXNTYPE.getName(),
						(String) request.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
				transactionResponser.removeInvalidResponseFields(responseMap);
				transactionResponser.addResponseDateTime(responseMap);
				responseMap.put(FieldType.IS_MERCHANT_HOSTED.getName(), "Y");
				fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
				responseCreator.create(fields);
				responseCreator.ResponsePost(responseMap, response);
//				return Action.NONE;
			} else if (StringUtils.isNotBlank(status) && status.equals(StatusType.DENIED_BY_FRAUD.getName())) {

				responseMap.put(FieldType.TXNTYPE.getName(),
						(String) request.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DENIED_BY_FRAUD.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DENIED_BY_FRAUD.getCode());
				transactionResponser.removeInvalidResponseFields(responseMap);
				transactionResponser.addResponseDateTime(responseMap);
				fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
				responseCreator.create(fields);
				responseCreator.ResponsePost(responseMap, response);
//				return Action.NONE;
			} else {
				responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(),
						(String) request.getSession().getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName()));
				if (StringUtils.isBlank(fields.get(FieldType.RESPONSE_CODE.getName()))) {
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(),
							ErrorType.INTERNAL_SYSTEM_ERROR.getResponseMessage());
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INTERNAL_SYSTEM_ERROR.getCode());
				}
				responseMap.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				responseMap.put(FieldType.TXNTYPE.getName(),
						(String) request.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
				transactionResponser.removeInvalidResponseFields(responseMap);
				transactionResponser.addResponseDateTime(responseMap);
				responseMap.put(FieldType.IS_MERCHANT_HOSTED.getName(), "Y");
				fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
				responseCreator.create(fields);
				responseCreator.ResponsePost(responseMap, response);
//				return Action.NONE;
			}

			responseMap.remove(FieldType.PAREQ.getName());
			responseMap.removeSecureFields();
		} catch (SystemException systemException) {
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

			User user = userDao.getUserClass(fields.get(FieldType.PAY_ID.getName()));
			String origTxnType = ModeType.getDefaultPurchaseTransaction(user.getModeType()).getName();

//			saveInvalidTransaction(fields, origTxnType, request);
			// If an invalid request of valid merchant save it
			if (StringUtils.isNotBlank(fields.get(FieldType.RESPONSE_CODE.getName()))
					&& fields.get(FieldType.RESPONSE_CODE.getName()).equals(ErrorType.INVALID_RETURN_URL.getCode())) {
//				return "invalidRequest";
				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				fields.put(FieldType.TXNTYPE.getName(), origTxnType);
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
					merchantHostedResponse.put(fieldType, fields.get(fieldType));
				}
				return merchantHostedResponse;
			}
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), systemException.getErrorType().getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getMessage());
			fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
			fields.put(FieldType.TXNTYPE.getName(), origTxnType);
			fields.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
			responseCreator.create(fields);
			fields.removeInternalFields();
			fields.put(FieldType.IS_MERCHANT_HOSTED.getName(), "Y");
			responseCreator.ResponsePost(fields, response);
			request.getSession().invalidate();
			List<String> fieldTypeList = new ArrayList<String>(fields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				merchantHostedResponse.put(fieldType, fields.get(fieldType));
			}
//			return Action.NONE;
		} catch (Exception exception) {
			request.getSession().invalidate();
			logger.error("Unknown error in merchant hosted payment", exception);
			String path = request.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = request.getScheme() + "://" + request.getHeader("Host") + "/pgui/jsp/error";
				response.sendRedirect(resultPath);
			}
			response.sendRedirect("error");
//			return ERROR;
		}
		return merchantHostedResponse;
	}

	private void handleTransactionCharges(Fields fields, HttpServletRequest request) throws SystemException {
		String paymentsRegion = fields.get(FieldType.PAYMENTS_REGION.getName());
		if (StringUtils.isBlank(paymentsRegion)) {
			paymentsRegion = AccountCurrencyRegion.DOMESTIC.toString();
		}
		String slabId = "";
		BigDecimal txnAmount = new BigDecimal(Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
				fields.get(FieldType.CURRENCY_CODE.getName())));

		if (txnAmount.compareTo(minAmountSlab3) >= 0) {
			slabId = "03";
		} else if (txnAmount.compareTo(minAmountSlab2) >= 0) {
			slabId = "02";
		} else {
			slabId = "01";
		}
		String amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
				fields.get(FieldType.CURRENCY_CODE.getName()));
		String payId = fields.get(FieldType.PAY_ID.getName());
		String surchargeFlag = (String) request.getSession().getAttribute(FieldType.SURCHARGE_FLAG.getName());
		String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());

		if (surchargeFlag.equals("Y")) {
			fields.put((FieldType.SURCHARGE_FLAG.getName()), surchargeFlag);
			// Add surcharge amount and total amount on the basis of payment type
			String paymentType = fields.get(FieldType.PAYMENT_TYPE.getName());
			PaymentType paymentTypeS = PaymentType.getInstanceUsingCode(paymentType);
			switch (paymentTypeS) {
			case CREDIT_CARD:
				/*
				 * BigDecimal[] surCCAmount =
				 * calculateSurchargeAmount.fetchCCSurchargeDetails(amount, payId,
				 * AccountCurrencyRegion.DOMESTIC, slabId); // BigDecimal adTransSurcharge =
				 * surCCAmount[0]; BigDecimal surchargeCCAmount = surCCAmount[1]; String
				 * ccTotalAmount = surchargeCCAmount.toString(); ccTotalAmount =
				 * Amount.formatAmount(ccTotalAmount, currencyCode);
				 * fields.put(FieldType.TOTAL_AMOUNT.getName(), ccTotalAmount);
				 */
				break;
			case DEBIT_CARD:
				BigDecimal[] surDCAmount = calculateSurchargeAmount.fetchDCVisaSurchargeDetails(amount, payId,
						AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
				// BigDecimal dcTransSurcharge = surDCAmount[0];
				BigDecimal surchargeDCAmount = surDCAmount[1];
				String dcTotalAmount = surchargeDCAmount.toString();
				dcTotalAmount = Amount.formatAmount(dcTotalAmount, currencyCode);
				fields.put(FieldType.TOTAL_AMOUNT.getName(), dcTotalAmount);
				break;
			case NET_BANKING:
				BigDecimal[] surNBAmount = calculateSurchargeAmount.fetchNBSurchargeDetails(amount, payId,
						AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
				// BigDecimal nbTransSurcharge = surNBAmount[0];
				BigDecimal surchargeNBAmount = surNBAmount[1];
				String nbTotalAmount = surchargeNBAmount.toString();
				nbTotalAmount = Amount.formatAmount(nbTotalAmount, currencyCode);
				fields.put(FieldType.TOTAL_AMOUNT.getName(), nbTotalAmount);
				break;
			case UPI:
				BigDecimal[] surUPAmount = calculateSurchargeAmount.fetchUPSurchargeDetails(amount, payId,
						AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
				// BigDecimal upTransSurcharge = surUPAmount[0];
				BigDecimal surchargeUPAmount = surUPAmount[1];
				String upiTotalAmount = surchargeUPAmount.toString();
				upiTotalAmount = Amount.formatAmount(upiTotalAmount, currencyCode);
				fields.put(FieldType.TOTAL_AMOUNT.getName(), upiTotalAmount);
				break;
			case WALLET:
				BigDecimal[] surWLAmount = calculateSurchargeAmount.fetchWLSurchargeDetails(amount, payId,
						AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
				// BigDecimal wlTransSurcharge = surWLAmount[0];
				BigDecimal surchargeWLAmount = surWLAmount[1];
				String wlTotalAmount = surchargeWLAmount.toString();
				wlTotalAmount = Amount.formatAmount(wlTotalAmount, currencyCode);
				fields.put(FieldType.TOTAL_AMOUNT.getName(), wlTotalAmount);
				break;
			case COD:
				BigDecimal[] surCDAmount = calculateSurchargeAmount.fetchCDSurchargeDetails(amount, payId,
						AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
				// BigDecimal cdTransSurcharge = surCDAmount[0];
				BigDecimal surchargeCDAmount = surCDAmount[1];
				String cdTotalAmount = surchargeCDAmount.toString();
				cdTotalAmount = Amount.formatAmount(cdTotalAmount, currencyCode);
				fields.put(FieldType.TOTAL_AMOUNT.getName(), cdTotalAmount);
				break;
			case EMI_CC:
				BigDecimal[] surEMCCAmount = calculateSurchargeAmount.fetchEMCCSurchargeDetails(amount, payId,
						AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
				// BigDecimal emCCTransSurcharge = surEMCCAmount[0];
				BigDecimal surchargeEMCCAmount = surEMCCAmount[1];
				String emCCTotalAmount = surchargeEMCCAmount.toString();
				emCCTotalAmount = Amount.formatAmount(emCCTotalAmount, currencyCode);
				fields.put(FieldType.TOTAL_AMOUNT.getName(), emCCTotalAmount);
				break;
			case EMI_DC:
				BigDecimal[] surEMDCAmount = calculateSurchargeAmount.fetchEMDCSurchargeDetails(amount, payId,
						AccountCurrencyRegion.DOMESTIC, slabId, resellerId);
				// BigDecimal emDCTransSurcharge = surEMDCAmount[0];
				BigDecimal surchargeEMDCAmount = surEMDCAmount[1];
				String emDCTotalAmount = surchargeEMDCAmount.toString();
				emDCTotalAmount = Amount.formatAmount(emDCTotalAmount, currencyCode);
				fields.put(FieldType.TOTAL_AMOUNT.getName(), emDCTotalAmount);
				break;
			case INTERNATIONAL:
				/*
				 * BigDecimal[] surINAmount =
				 * calculateSurchargeAmount.fetchCCSurchargeDetails(amount, payId,
				 * AccountCurrencyRegion.INTERNATIONAL, slabId); // BigDecimal inTransSurcharge
				 * = surINAmount[0]; BigDecimal surchargeINAmount = surINAmount[1]; String
				 * inTotalAmount = surchargeINAmount.toString(); inTotalAmount =
				 * Amount.formatAmount(inTotalAmount, currencyCode);
				 * fields.put(FieldType.TOTAL_AMOUNT.getName(), inTotalAmount);
				 */
				break;
			default:
				// unsupported payment type
				throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
			}
		} else {
			// TDR MODE
			fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
		}
	}

	private void saveInvalidTransaction(Fields fields, String origTxnType, HttpServletRequest request) {
		// TODO... validate and sanitize fields
		fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), origTxnType);

		request.getSession().setAttribute((FieldType.INTERNAL_ORIG_TXN_TYPE.getName()), origTxnType);

		try {
			updateProcessor.preProcess(fields);
			updateProcessor.prepareInvalidTransactionForStorage(fields);
		} catch (SystemException systemException) {
			logger.error("Unable to save invalid transaction", systemException);
		} catch (Exception exception) {
			logger.error("Unhandaled error", exception);
			// Non reachable code for safety
		}
	}

	public static String generateGUID() {
		return new BigInteger(165, RANDOM).toString(36).toUpperCase();
	}

	public String getAcquirerFlag() {
		return acquirerFlag;
	}

	public void setAcquirerFlag(String acquirerFlag) {
		this.acquirerFlag = acquirerFlag;
	}

	public static Map<String, FieldType> getMandatoryStatusRequestFields() {
		Map<String, FieldType> fields = new HashMap<String, FieldType>();

		fields.put(FieldType.ORDER_ID.getName(), FieldType.ORDER_ID);
		fields.put(FieldType.AMOUNT.getName(), FieldType.AMOUNT);
		fields.put(FieldType.CURRENCY_CODE.getName(), FieldType.CURRENCY_CODE);

		return fields;
	}

}
