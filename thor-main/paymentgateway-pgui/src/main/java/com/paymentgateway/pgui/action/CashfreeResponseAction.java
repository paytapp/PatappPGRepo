package com.paymentgateway.pgui.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.pg.core.acquirerDoubleVerification.CheckDBEntryForPgref;
import com.paymentgateway.pg.core.util.CashfreeChecksumUtil;
import com.paymentgateway.pg.core.util.ResponseCreator;
import com.paymentgateway.pg.core.util.TransactionResponser;

@Service
public class CashfreeResponseAction {

	private static Logger logger = LoggerFactory.getLogger(CashfreeResponseAction.class.getName());

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private ResponseCreator responseCreator;

	@Autowired
	private UserDao userDao;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private TransactionResponser transactionResponser;

	@Autowired
	private CheckDBEntryForPgref checkDBEntryForPgref;

	@Autowired
	private CashfreeChecksumUtil cashfreeChecksumUtil;

	public CashfreeResponseAction() {
	}

	public Map<String, String> cashfreeResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {

		Fields responseMap = null;

		try {
//			httpRequest.getSession().invalidate();
			Map<String, String[]> fieldMapObj = httpRequest.getParameterMap();
			Map<String, String> requestMap = new HashMap<String, String>();

			for (Entry<String, String[]> entry : fieldMapObj.entrySet()) {
				try {
					requestMap.put(entry.getKey(), entry.getValue()[0]);

				} catch (ClassCastException classCastException) {
					logger.error("Exception", classCastException);
				}
			}

			JsonObject resJson = new JsonObject();
			String pgRef = null;

			for (Entry<String, String> entry : requestMap.entrySet()) {

				resJson.addProperty(entry.getKey(), entry.getValue());
				if (entry.getKey().equalsIgnoreCase("orderId")) {
					pgRef = entry.getValue();
				}
			}

			logger.info("Response received from Cashfree: " + resJson.toString());
			Fields fields = new Fields();

			String txnKey = (String) httpRequest.getSession().getAttribute(FieldType.TXN_KEY.getName());
			String merchantId = (String) httpRequest.getSession().getAttribute(FieldType.MERCHANT_ID.getName());

			Map<String, String> sessionMap = new HashMap<String, String>();

			// Get Fields if session map is blank
			if (sessionMap.isEmpty()) {
				if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
					logger.info("FIELDS is blank in session Map, getting data from DB");
					fields = fieldsDao.getPreviousForPgRefNum(pgRef);

					String internalRequestFields = fields.get(FieldType.INTERNAL_REQUEST_FIELDS.getName());
					String[] paramaters = internalRequestFields.split("~");
					Map<String, String> paramMap = new HashMap<String, String>();
					paramaters = internalRequestFields.split("~");
					paramMap = new HashMap<String, String>();
					for (String param : paramaters) {
						String[] parameterPair = param.split("=");
						if (parameterPair.length > 1) {
							paramMap.put(parameterPair[0].trim(), parameterPair[1].trim());
						}
					}
					String checkout_Flag = "";
					boolean checkout_flag = userDao.getCheckoutFlag(fields.get(FieldType.PAY_ID.getName()));
					if (checkout_flag == false) {
						checkout_Flag = "N";
					} else {
						checkout_Flag = "Y";
					}
					fields.put(FieldType.CHECKOUT_JS_FLAG.getName(), checkout_Flag);
					httpRequest.getSession().setAttribute(FieldType.CHECKOUT_JS_FLAG.getName(), checkout_Flag);
					fields.put(FieldType.RETURN_URL.getName(), paramMap.get(FieldType.RETURN_URL.getName()));
					httpRequest.getSession().setAttribute(FieldType.RETURN_URL.getName(),
							paramMap.get(FieldType.RETURN_URL.getName()));
					if (StringUtils.isNotBlank(paramMap.get(FieldType.INTERNAL_CUST_IP.getName()))) {
						fields.put((FieldType.INTERNAL_CUST_IP.getName()),
								paramMap.get(FieldType.INTERNAL_CUST_IP.getName()));
					}
					if (StringUtils.isNotBlank(paramMap.get(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()))) {
						fields.put((FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()),
								paramMap.get(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()));
					}

				}
			}

			Enumeration<String> keys = httpRequest.getSession().getAttributeNames();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				sessionMap.put(key, (String) httpRequest.getSession().getAttribute(key).toString());
			}

			// sessionMap.clear();

			String sessionPgRef = (String) httpRequest.getSession().getAttribute(FieldType.PG_REF_NUM.getName());
			Boolean pgRefMatch = false;
			Map<String, String> pgRefResponse = new HashMap<String, String>();

			// checking session pgRef and response pgRef
			if (StringUtils.isNotBlank(sessionPgRef) && sessionPgRef.equalsIgnoreCase(pgRef)) {
				pgRefMatch = true;
			}
			// if session data is null then check txn status from DB using response pgRef
			if (!pgRefMatch) {
				pgRefResponse = checkDBEntryForPgref.searchPaymentStatus(pgRef);
				if (StringUtils.isNotBlank(pgRefResponse.get(FieldType.STATUS.getName()))
						&& !pgRefResponse.get(FieldType.STATUS.getName())
								.equalsIgnoreCase(StatusType.SENT_TO_BANK.getName())
						&& !pgRefResponse.get(FieldType.STATUS.getName())
								.equalsIgnoreCase(StatusType.ENROLLED.getName())
						&& !pgRefResponse.get(FieldType.STATUS.getName())
								.equalsIgnoreCase(StatusType.PENDING.getName())) {
					String internalRequestFields = pgRefResponse.get(FieldType.INTERNAL_REQUEST_FIELDS.getName());
					String[] paramaters = internalRequestFields.split("~");
					Map<String, String> paramMap = new HashMap<String, String>();
					for (String param : paramaters) {
						String[] parameterPair = param.split("=");
						if (parameterPair.length > 1) {
							paramMap.put(parameterPair[0].trim(), parameterPair[1].trim());
						}
					}
					responseMap = new Fields(pgRefResponse);
					responseMap.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
					responseMap.put(FieldType.AMOUNT.getName(), Amount.removeDecimalAmount(
							paramMap.get(FieldType.AMOUNT.getName()), paramMap.get(FieldType.CURRENCY_CODE.getName())));
					responseMap.put(FieldType.TOTAL_AMOUNT.getName(),
							Amount.formatAmount(pgRefResponse.get(FieldType.TOTAL_AMOUNT.getName()),
									pgRefResponse.get(FieldType.CURRENCY_CODE.getName())));
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(),
							ErrorType.DENIED_BY_FRAUD.getResponseMessage());
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DENIED_BY_FRAUD.getCode());
					responseMap.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.FRAUD_RESPONSE.getResponseMessage());
					responseMap.put(FieldType.RETURN_URL.getName(), paramMap.get(FieldType.RETURN_URL.getName()));
					transactionResponser.removeInvalidResponseFields(responseMap);
					transactionResponser.addResponseDateTime(responseMap);
					String pgFlag = (String) httpRequest.getSession()
							.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
					if (StringUtils.isNotBlank(pgFlag)) {
						responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
					}
					responseMap.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
					responseCreator.create(responseMap);
					responseCreator.ResponsePost(responseMap, httpResponse);
					return responseMap.getFields();
				}
			}

			// checking signature received from response data
			JSONObject jsonRequest = new JSONObject();
			String cashfreeResponseSignature = requestMap.get("signature");
			Map<String, String> responseMerchantData = new HashMap<String, String>();
			if (!sessionMap.isEmpty()) {
				responseMerchantData = getTxnKey(sessionMap, fields.get(FieldType.PAY_ID.getName()));
			} else {
				responseMerchantData = getTxnKey(pgRefResponse, fields.get(FieldType.PAY_ID.getName()));
			}
			requestMap.remove("signature");
			for (String keyFields : requestMap.keySet()) {
				jsonRequest.put(keyFields, requestMap.get(keyFields));
			}
			String calculateSignature = cashfreeChecksumUtil.checkSaleResponseHash(jsonRequest,
					responseMerchantData.get(FieldType.TXN_KEY.getName()));
			if (!calculateSignature.equals(cashfreeResponseSignature)) {
				StringBuilder Message = new StringBuilder("Response Signature =");
				Message.append(cashfreeResponseSignature);
				Message.append(", Calculated Signature =");
				Message.append(calculateSignature);
				logger.error(Message.toString());

				// return response
				if (pgRefResponse.isEmpty()
						|| !pgRefResponse.containsKey(FieldType.INTERNAL_REQUEST_FIELDS.getName())) {
					pgRefResponse = checkDBEntryForPgref.searchPaymentStatus(pgRef);
				}
				String internalRequestFields = pgRefResponse.get(FieldType.INTERNAL_REQUEST_FIELDS.getName());
				String[] paramaters = internalRequestFields.split("~");
				Map<String, String> paramMap = new HashMap<String, String>();
				for (String param : paramaters) {
					String[] parameterPair = param.split("=");
					if (parameterPair.length > 1) {
						paramMap.put(parameterPair[0].trim(), parameterPair[1].trim());
					}
				}
				responseMap = new Fields(pgRefResponse);
				responseMap.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				responseMap.put(FieldType.AMOUNT.getName(), Amount.removeDecimalAmount(
						paramMap.get(FieldType.AMOUNT.getName()), paramMap.get(FieldType.CURRENCY_CODE.getName())));
				responseMap.put(FieldType.TOTAL_AMOUNT.getName(),
						Amount.formatAmount(pgRefResponse.get(FieldType.TOTAL_AMOUNT.getName()),
								pgRefResponse.get(FieldType.CURRENCY_CODE.getName())));
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DENIED_BY_FRAUD.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DENIED_BY_FRAUD.getCode());
				responseMap.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.SIGNATURE_MISMATCH.getResponseMessage());
				responseMap.put(FieldType.RETURN_URL.getName(), paramMap.get(FieldType.RETURN_URL.getName()));
				transactionResponser.removeInvalidResponseFields(responseMap);
				transactionResponser.addResponseDateTime(responseMap);
				String pgFlag = (String) httpRequest.getSession()
						.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
				if (StringUtils.isNotBlank(pgFlag)) {
					responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
				}
				responseMap.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
				responseCreator.create(responseMap);
				responseCreator.ResponsePost(responseMap, httpResponse);
				return responseMap.getFields();
			}

			else {

				if (sessionMap.isEmpty()) {
					if (StringUtils.isBlank(txnKey)) {
						if (StringUtils.isNotBlank(pgRef)) {
							String pgRefNum = pgRef;
							fields = fieldsDao.getPreviousForPgRefNum(pgRefNum);
							String internalRequestFields = fieldsDao
									.getPreviousForOID(fields.get(FieldType.OID.getName()));

							if (StringUtils.isBlank(internalRequestFields)) {
								internalRequestFields = fieldsDao
										.getPreviousByOIDForSentToBank(fields.get(FieldType.OID.getName()));
							}
							String[] paramaters = internalRequestFields.split("~");
							Map<String, String> paramMap = new HashMap<String, String>();
							for (String param : paramaters) {
								String[] parameterPair = param.split("=");
								if (parameterPair.length > 1) {
									paramMap.put(parameterPair[0].trim(), parameterPair[1].trim());
								}
							}
							String checkout_Flag = "";
							boolean checkout_flag = userDao.getCheckoutFlag(fields.get(FieldType.PAY_ID.getName()));
							if (checkout_flag == false) {
								checkout_Flag = "N";
							} else {
								checkout_Flag = "Y";
							}
							fields.put(FieldType.CHECKOUT_JS_FLAG.getName(), checkout_Flag);
							httpRequest.getSession().setAttribute(FieldType.CHECKOUT_JS_FLAG.getName(), checkout_Flag);
							fields.put(FieldType.RETURN_URL.getName(), paramMap.get(FieldType.RETURN_URL.getName()));
							httpRequest.getSession().setAttribute(FieldType.RETURN_URL.getName(),
									paramMap.get(FieldType.RETURN_URL.getName()));

							if (StringUtils.isNotBlank(paramMap.get(FieldType.IS_MERCHANT_HOSTED.getName()))) {
								logger.info("IS_MERCHANT_HOSTED flag found for ORDER ID "
										+ paramMap.get(FieldType.ORDER_ID.getName()) + " in CASHFREE");
								fields.put(FieldType.IS_MERCHANT_HOSTED.getName(),
										paramMap.get(FieldType.IS_MERCHANT_HOSTED.getName()));
								httpRequest.getSession().setAttribute(FieldType.IS_MERCHANT_HOSTED.getName(),
										paramMap.get(FieldType.IS_MERCHANT_HOSTED.getName()));
							} else {
								logger.info("IS_MERCHANT_HOSTED not found for ORDER ID "
										+ paramMap.get(FieldType.ORDER_ID.getName()) + " in CASHFREE");
							}

							Map<String, String> paymentMap = new HashMap<String, String>();
							paymentMap = getTxnKey(pgRefResponse, fields.get(FieldType.PAY_ID.getName()));

							txnKey = paymentMap.get(FieldType.TXN_KEY.getName());
							merchantId = paymentMap.get(FieldType.MERCHANT_ID.getName());
							fields.put(FieldType.MERCHANT_ID.getName(),
									paymentMap.get(FieldType.MERCHANT_ID.getName()));
							fields.put(FieldType.TXN_KEY.getName(), paymentMap.get(FieldType.TXN_KEY.getName()));
						}

					}
				} else {
					Object fieldsObj = null;
					if (StringUtils.isNotBlank(
							httpRequest.getSession().getAttribute(FieldType.CHECKOUT_JS_FLAG.getName()).toString())
							&& httpRequest.getSession().getAttribute(FieldType.CHECKOUT_JS_FLAG.getName()).toString()
									.equalsIgnoreCase("Y")) {
						if (httpRequest.getSession().getAttribute("FIELDS").getClass().getSimpleName().toString()
								.equalsIgnoreCase("Fields")) {
							fieldsObj = (Fields) httpRequest.getSession().getAttribute("FIELDS");
						} else {
							String sessionFieldsObj = (String) httpRequest.getSession().getAttribute("FIELDS");
							Map<String, String> fieldsMap = new HashMap<String, String>();
							sessionFieldsObj = sessionFieldsObj.substring(1, sessionFieldsObj.length() - 1);
							List<String> fieldArray = split(sessionFieldsObj);

							for (String key : fieldArray) {
								if (key.charAt(0) == ' ') {
									key = key.replaceFirst("^\\s*", "");
								}
								String[] namValuePair = key.split("=", 2);
								fieldsMap.put(namValuePair[0], namValuePair[1]);
							}
							fieldsObj = new Fields(fieldsMap);
							logger.info(fieldsMap.toString());
						}
					} else {
						fieldsObj = (Fields) httpRequest.getSession().getAttribute("FIELDS");
					}
					if (null != fieldsObj) {
						fields.put((Fields) fieldsObj);
					}
				}

				fields.put(FieldType.CASHFREE_RESPONSE_FIELD.getName(), resJson.toString());
				fields.put(FieldType.TXN_KEY.getName(), txnKey);
				fields.put(FieldType.MERCHANT_ID.getName(), merchantId);
				fields.logAllFields("Cashfree Response Recieved :");

				fields.logAllFields("Updated 3DS Recieved Map TxnType = " + fields.get(FieldType.TXNTYPE.getName())
						+ " " + "Txn id = " + fields.get(FieldType.TXN_ID.getName()));
				fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.CASHFREE.getCode());
				fields.put(FieldType.TXNTYPE.getName(),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
				fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
				fields.put((FieldType.PAYMENTS_REGION.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.PAYMENTS_REGION.getName()));
				fields.put((FieldType.CARD_HOLDER_TYPE.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.CARD_HOLDER_TYPE.getName()));
				fields.put((FieldType.OID.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.OID.getName()));

				if (fields.get(FieldType.PAYMENT_TYPE.getName()) != null
						&& (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.UPI.getCode())
								|| fields.get(FieldType.PAYMENT_TYPE.getName())
										.equalsIgnoreCase(PaymentType.WALLET.getCode()))
						|| fields.get(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.NET_BANKING.getCode())) {
					fields.put(FieldType.PAYMENTS_REGION.getName(), AccountCurrencyRegion.DOMESTIC.toString());
					fields.put(FieldType.CARD_HOLDER_TYPE.getName(), CardHolderType.CONSUMER.toString());
				}

				Map<String, String> response = transactionControllerServiceProvider.transact(fields,
						Constants.TXN_WS_CASHFREE_PROCESSOR.getValue());
				responseMap = new Fields(response);

				String isMerchantHosted = (String) httpRequest.getSession()
						.getAttribute(FieldType.IS_MERCHANT_HOSTED.getName());
				if (StringUtils.isNotBlank(isMerchantHosted)) {
					responseMap.put(FieldType.IS_MERCHANT_HOSTED.getName(), isMerchantHosted);
				}
				fields.put(FieldType.RETURN_URL.getName(),
						(String) httpRequest.getSession().getAttribute(FieldType.RETURN_URL.getName()));

				String cardIssuerBank = (String) httpRequest.getSession()
						.getAttribute(FieldType.INTERNAL_CARD_ISSUER_BANK.getName());
				String cardIssuerCountry = (String) httpRequest.getSession()
						.getAttribute(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName());
				if (StringUtils.isNotBlank(cardIssuerBank)) {
					responseMap.put(FieldType.CARD_ISSUER_BANK.getName(), cardIssuerBank);
				}
				if (StringUtils.isNotBlank(cardIssuerCountry)) {
					responseMap.put(FieldType.CARD_ISSUER_COUNTRY.getName(), cardIssuerCountry);
				}
				responseMap.put(FieldType.CHECKOUT_JS_FLAG.getName(),
						(String) httpRequest.getSession().getAttribute(FieldType.CHECKOUT_JS_FLAG.getName()));
				responseMap.put(FieldType.INTERNAL_SHOPIFY_YN.getName(),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_SHOPIFY_YN.getName()));
				if (httpRequest.getSession() != null) {
					httpRequest.getSession().setAttribute(Constants.TRANSACTION_COMPLETE_FLAG.getValue(),
							Constants.Y_FLAG.getValue());
					httpRequest.getSession().invalidate();
				}
				responseMap.remove(FieldType.HASH.getName());
				responseMap.remove(FieldType.TXN_KEY.getName());
				responseMap.remove(FieldType.ACQUIRER_TYPE.getName());
				responseMap.remove(FieldType.PASSWORD.getName());
				responseMap.remove(FieldType.IS_INTERNAL_REQUEST.getName());
				responseCreator.create(responseMap);
				responseCreator.ResponsePost(responseMap, httpResponse);
				return responseMap.getFields();
			}

		} catch (Exception exception) {
			logger.error("Exception", exception);
			String path = httpRequest.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host") + "/pgui/jsp/error";
				httpResponse.sendRedirect(resultPath);
			}
			httpResponse.sendRedirect("error");
		}
		return responseMap.getFields();
	}

	public Map<String, String> getTxnKey(Map<String, String> fields, String payId) throws SystemException {

		if (StringUtils.isNotBlank(payId)) {
			logger.info("Pay Id for ");
			User user = userDao.findPayId(payId);
			Account account = null;
			Set<Account> accounts = user.getAccounts();

			if (accounts == null || accounts.size() == 0) {
				logger.info("No account found for Pay ID = " + payId);
			} else {
				for (Account accountThis : accounts) {
					if (accountThis.getAcquirerName().equalsIgnoreCase(
							AcquirerType.getInstancefromCode(AcquirerType.CASHFREE.getCode()).getName())) {
						account = accountThis;
						break;
					}
				}
			}

			AccountCurrency accountCurrency = account.getAccountCurrency("356");
			String mId = accountCurrency.getMerchantId();
			String txnKey = accountCurrency.getTxnKey();
			Map<String, String> merchantdetailsMap = new HashMap<String, String>();
			merchantdetailsMap.put(FieldType.MERCHANT_ID.getName(), mId);
			merchantdetailsMap.put(FieldType.TXN_KEY.getName(), txnKey);
			return merchantdetailsMap;
		} else {

			logger.info("Pay Id from fields = " + fields.get(FieldType.PAY_ID.getName()));
			User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
			Account account = null;
			Set<Account> accounts = user.getAccounts();

			if (accounts == null || accounts.size() == 0) {
				logger.info("No account found for Pay ID = " + fields.get(FieldType.PAY_ID.getName())
						+ " and ORDER ID = " + fields.get(FieldType.ORDER_ID.getName()));
			} else {
				for (Account accountThis : accounts) {
					if (accountThis.getAcquirerName().equalsIgnoreCase(
							AcquirerType.getInstancefromCode(AcquirerType.CASHFREE.getCode()).getName())) {
						account = accountThis;
						break;
					}
				}
			}

			AccountCurrency accountCurrency = account.getAccountCurrency(fields.get(FieldType.CURRENCY_CODE.getName()));
			String mId = accountCurrency.getMerchantId();
			String txnKey = accountCurrency.getTxnKey();
			Map<String, String> merchantdetailsMap = new HashMap<String, String>();
			merchantdetailsMap.put(FieldType.MERCHANT_ID.getName(), mId);
			merchantdetailsMap.put(FieldType.TXN_KEY.getName(), txnKey);
			return merchantdetailsMap;

		}

	}

	public static List<String> split(String input) {
		int nParens = 0;
		int start = 0;
		List<String> result = new ArrayList<>();
		for (int i = 0; i < input.length(); i++) {
			switch (input.charAt(i)) {
			case ',':
				if (nParens == 0) {
					result.add(input.substring(start, i));
					start = i + 1;
				}
				break;
			case '{':
				nParens++;
				break;
			case '}':
				nParens--;
				if (nParens < 0)
					throw new IllegalArgumentException("Unbalanced parenthesis at offset #" + i);
				break;
			}
		}
		if (nParens > 0)
			throw new IllegalArgumentException("Missing closing parenthesis");
		result.add(input.substring(start));
		return result;
	}
}
