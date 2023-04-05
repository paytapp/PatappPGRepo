package com.paymentgateway.pgui.action;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.EPOSTransactionDao;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.kms.EncryptDecryptService;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.threadpool.ThreadPoolProvider;
import com.paymentgateway.pg.core.acquirerDoubleVerification.CheckBankResponseUsingStatusEnquiry;
import com.paymentgateway.pg.core.acquirerDoubleVerification.CheckDBEntryForPgref;
import com.paymentgateway.pg.core.util.PayuUtil;
import com.paymentgateway.pg.core.util.ResponseCreator;
import com.paymentgateway.pg.core.util.TransactionResponser;
import com.paymentgateway.pgui.action.service.RetryTransactionProcessor;

/**
 * @author Rahul
 *
 */
@Service
public class PayuResponseAction {

	private static Logger logger = LoggerFactory.getLogger(PayuResponseAction.class.getName());

	@Autowired
	TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private ResponseCreator responseCreator;

	@Autowired
	private UserDao userDao;

	@Autowired
	private RetryTransactionProcessor retryTransactionProcessor;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	EPOSTransactionDao eposDao;

	@Autowired
	private TransactionResponser transactionResponser;

	@Autowired
	private EncryptDecryptService encryptDecryptService;

	@Autowired
	private CheckBankResponseUsingStatusEnquiry checkBankResponseUsingStatusEnquiry;

	@Autowired
	private CheckDBEntryForPgref checkDBEntryForPgref;

	private Fields responseMap = null;

	public Map<String, String> payuResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {
		try {

			String txnId = null;
			Map<String, String[]> fieldMapObj = httpRequest.getParameterMap();
			Map<String, String> requestMap = new HashMap<String, String>();
			StringBuilder responseString = new StringBuilder();

			for (Entry<String, String[]> entry : fieldMapObj.entrySet()) {
				try {
					requestMap.put(entry.getKey(), entry.getValue()[0]);
					responseString.append(entry.getKey());
					responseString.append("=");
					responseString.append(entry.getValue()[0]);
					responseString.append(";");

					if (entry.getKey().equalsIgnoreCase("txnId")) {
						txnId = entry.getValue()[0];
					}

				} catch (ClassCastException classCastException) {
					logger.error("Exception", classCastException);
					String path = httpRequest.getContextPath();
					logger.info(path);
					if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
						String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host")
								+ "/pgui/jsp/error";
						httpResponse.sendRedirect(resultPath);
					}
					httpResponse.sendRedirect("error");
				}
			}

			Fields fields = new Fields();
			// Log all entries from requestMap

			if (!requestMap.isEmpty()) {

				StringBuilder sb = new StringBuilder();
				Iterator itr = requestMap.entrySet().iterator();
				while (itr.hasNext()) {
					Map.Entry obj = (Entry) itr.next();
					sb.append(obj.getKey() + " = " + obj.getValue() + " ~");
				}
				logger.info("PAYU Request Map received >>> " + sb.toString());
			} else {
				logger.info("PAYU Request Map is empty ");
			}
			Map<String, String> sessionMap = new HashMap<String, String>();
			Enumeration<String> keys = httpRequest.getSession().getAttributeNames();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();

				sessionMap.put(key, (String) httpRequest.getSession().getAttribute(key).toString());
			}
			// Log all entries from sessionMap
			sessionMap.clear();
			if (!sessionMap.isEmpty()) {

				StringBuilder sb = new StringBuilder();
				Iterator itr = sessionMap.entrySet().iterator();
				while (itr.hasNext()) {
					Map.Entry obj = (Entry) itr.next();
					sb.append(obj.getKey() + " = " + obj.getValue() + " ~");
				}
				// logger.info("PAYU sessionMap values >>> " + sb.toString());
			} else {
				logger.info("PAYU Session Map is empty");

				if (sessionMap.isEmpty()) {
					if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
						logger.info("FIELDS is blank in session Map, getting data from DB");
						fields = fieldsDao.getPreviousForPgRefNum(txnId);

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

			}

			String sessionPgRef = (String) httpRequest.getSession().getAttribute(FieldType.PG_REF_NUM.getName());
			Boolean pgRefMatch = false;
			Map<String, String> pgRefResponse = new HashMap<String, String>();

			// checking session pgRef and response pgRef
			if (StringUtils.isNotBlank(sessionPgRef) && sessionPgRef.equalsIgnoreCase(txnId)) {
				pgRefMatch = true;
			}
			// if session data is null then check txn status from DB using response pgRef
			if (!pgRefMatch) {
				pgRefResponse = checkDBEntryForPgref.searchPaymentStatus(txnId);
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

			/* uncomment this code after pay hash method fix */
//				String internalRequestFields = pgRefResponse.get(FieldType.INTERNAL_REQUEST_FIELDS.getName());
//				String[] paramaters = internalRequestFields.split("~");
//				Map<String, String> paramMap = new HashMap<String, String>();
//				for (String param : paramaters) {
//					String[] parameterPair = param.split("=");
//					if (parameterPair.length > 1) {
//						paramMap.put(parameterPair[0].trim(), parameterPair[1].trim());
//					}
//				}
//				responseMap = new Fields(pgRefResponse);
//				responseMap.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
//				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DENIED_BY_FRAUD.getResponseMessage());
//				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DENIED_BY_FRAUD.getCode());
//				responseMap.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.SIGNATURE_MISMATCH.getResponseMessage());
//				responseMap.put(FieldType.RETURN_URL.getName(), paramMap.get(FieldType.RETURN_URL.getName()));
//				transactionResponser.removeInvalidResponseFields(responseMap);
//				transactionResponser.addResponseDateTime(responseMap);
//				String pgFlag = (String) httpRequest.getSession()
//						.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
//				if (StringUtils.isNotBlank(pgFlag)) {
//					responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
//				}
//				responseMap.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
//				responseCreator.create(responseMap);
//				responseCreator.ResponsePost(responseMap, httpResponse);
//				return responseMap.getFields();
//			}

			// status enquiry
			Map<String, String> statusEnquiryResponseMap = new HashMap<String, String>();
			Map<String, String> statusEnquiryFields = new HashMap<String, String>();
			if (!sessionMap.isEmpty()) {
				statusEnquiryFields = getTxnKey(sessionMap, fields.get(FieldType.PAY_ID.getName()));
			} else {
				statusEnquiryFields = getTxnKey(pgRefResponse, fields.get(FieldType.PAY_ID.getName()));
			}
			statusEnquiryFields.put(FieldType.PG_REF_NUM.getName(), txnId);
			String statusEnquiryResponse = checkBankResponseUsingStatusEnquiry
					.payuBankStatusEnquiry(statusEnquiryFields);

			logger.info("status from status enquiry in response action >>> " + statusEnquiryResponse.toString());
			statusEnquiryResponse = statusEnquiryResponse.substring(1, statusEnquiryResponse.length() - 1);
			statusEnquiryResponse = statusEnquiryResponse.replaceAll("\"", "");
			String[] paramaters = statusEnquiryResponse.split(",");
			Map<String, String> paramMap = new HashMap<String, String>();
			for (String param : paramaters) {
				String[] parameterPair = param.split(":");
				if (parameterPair.length > 1) {
					statusEnquiryResponseMap.put(parameterPair[0].trim(), parameterPair[1].trim());
				}
			}
			if (!requestMap.get("status").equalsIgnoreCase(statusEnquiryResponseMap.get("status"))) {
				if (pgRefResponse.isEmpty()
						|| !pgRefResponse.containsKey(FieldType.INTERNAL_REQUEST_FIELDS.getName())) {
					pgRefResponse = checkDBEntryForPgref.searchPaymentStatus(txnId);
				}
				String internalRequestFields = pgRefResponse.get(FieldType.INTERNAL_REQUEST_FIELDS.getName());
				String[] paramater = internalRequestFields.split("~");
				paramMap = new HashMap<String, String>();
				for (String param : paramater) {
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
				responseMap.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.DENIED_BY_FRAUD.getResponseMessage());
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

				// Check if fields is empty
				if (sessionMap.isEmpty()) {
					if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
						logger.info("FIELDS is blank in session Map, getting data from DB");

						fields = fieldsDao.getPreviousForPgRefNum(txnId);
						String internalRequestFields = fields.get(FieldType.INTERNAL_REQUEST_FIELDS.getName());
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
							String[] fieldArray = sessionFieldsObj.split(",");
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
				fields.put(FieldType.PAYU_RESPONSE_FIELD.getName(), responseString.toString());
				fields.logAllFields("Payu Response Recieved :");

				fields.logAllFields("Updated 3DS Recieved Map TxnType = " + fields.get(FieldType.TXNTYPE.getName())
						+ " " + "Txn id = " + fields.get(FieldType.TXN_ID.getName()));
				fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.PAYU.getCode());

				if (StringUtils.isNotBlank(
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()))) {
					fields.put(FieldType.TXNTYPE.getName(),
							(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
				}
				if (StringUtils.isNotBlank(
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_CUST_IP.getName()))) {
					fields.put((FieldType.INTERNAL_CUST_IP.getName()),
							(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_CUST_IP.getName()));
				}
				if (StringUtils.isNotBlank((String) httpRequest.getSession()
						.getAttribute(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()))) {
					fields.put((FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()), (String) httpRequest.getSession()
							.getAttribute(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()));
				}

				if (StringUtils.isNotBlank(
						(String) httpRequest.getSession().getAttribute(FieldType.PAYMENTS_REGION.getName()))) {
					fields.put((FieldType.PAYMENTS_REGION.getName()),
							(String) httpRequest.getSession().getAttribute(FieldType.PAYMENTS_REGION.getName()));
				}

				if (StringUtils.isNotBlank(
						(String) httpRequest.getSession().getAttribute(FieldType.CARD_HOLDER_TYPE.getName()))) {
					fields.put((FieldType.CARD_HOLDER_TYPE.getName()),
							(String) httpRequest.getSession().getAttribute(FieldType.CARD_HOLDER_TYPE.getName()));
				}

				if (StringUtils.isNotBlank(
						(String) httpRequest.getSession().getAttribute(FieldType.ACQUIRER_MODE.getName()))) {
					fields.put((FieldType.ACQUIRER_MODE.getName()),
							(String) httpRequest.getSession().getAttribute(FieldType.ACQUIRER_MODE.getName()));
				}

				if (StringUtils
						.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.TENURE.getName()))) {
					fields.put((FieldType.TENURE.getName()),
							(String) httpRequest.getSession().getAttribute(FieldType.TENURE.getName()));
				}

				if (StringUtils.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.OID.getName()))) {
					fields.put((FieldType.OID.getName()),
							(String) httpRequest.getSession().getAttribute(FieldType.OID.getName()));

				}

				if (StringUtils.isNotBlank(
						(String) httpRequest.getSession().getAttribute(FieldType.RATE_OF_INTEREST.getName()))) {
					fields.put((FieldType.RATE_OF_INTEREST.getName()),
							(String) httpRequest.getSession().getAttribute(FieldType.RATE_OF_INTEREST.getName()));

				}

				if (StringUtils.isNotBlank(
						(String) httpRequest.getSession().getAttribute(FieldType.EMI_PER_MONTH.getName()))) {
					fields.put((FieldType.EMI_PER_MONTH.getName()),
							(String) httpRequest.getSession().getAttribute(FieldType.EMI_PER_MONTH.getName()));

				}

				if (StringUtils.isNotBlank(
						(String) httpRequest.getSession().getAttribute(FieldType.EMI_TOTAL_AMOUNT.getName()))) {
					fields.put((FieldType.EMI_TOTAL_AMOUNT.getName()),
							(String) httpRequest.getSession().getAttribute(FieldType.EMI_TOTAL_AMOUNT.getName()));

				}

				if (StringUtils
						.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.EMI_INTEREST.getName()))) {
					fields.put((FieldType.EMI_INTEREST.getName()),
							(String) httpRequest.getSession().getAttribute(FieldType.EMI_INTEREST.getName()));

				}

				if (StringUtils
						.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.ISSUER_BANK.getName()))) {
					fields.put((FieldType.ISSUER_BANK.getName()),
							(String) httpRequest.getSession().getAttribute(FieldType.ISSUER_BANK.getName()));

				}

				fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");

				Map<String, String> response = transactionControllerServiceProvider.transact(fields,
						Constants.TXN_WS_PAYU_PROCESSOR.getValue());
				responseMap = new Fields(response);

				String pgFlag = (String) httpRequest.getSession()
						.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
				if (StringUtils.isNotBlank(pgFlag)) {
					responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
				}

				// Fetch user for retryTransaction ,SendEmailer and SmsSenser

				User user = userDao.getUserClass(responseMap.get(FieldType.PAY_ID.getName()));

				// Retry Transaction Block Start
				if (!responseMap.get(FieldType.RESPONSE_CODE.getName()).equals(ErrorType.SUCCESS.getCode())) {

					if (retryTransactionProcessor.retryTransaction(responseMap, httpRequest, user)) {
//					addActionMessage(CrmFieldConstants.RETRY_TRANSACTION.getValue());
						httpRequest.getSession().removeAttribute(FieldType.PAYU_FINAL_REQUEST.getName());
						httpRequest.getSession().removeAttribute(FieldType.PAYU_RESPONSE_FIELD.getName());
						httpRequest.getSession().removeAttribute(FieldType.ACQUIRER_TYPE.getName());
						httpRequest.getSession().setAttribute(FieldType.RETRY_FLAG.getName(), "Y");
						responseMap.put(FieldType.RETRY_URL.getName(),
								PropertiesManager.propertiesMap.get(FieldType.RETRY_URL.getName()));
						responseCreator.ResponsePost(responseMap, httpResponse);
//					return Action.NONE;
					}

				}

				/*
				 * Object previousFields =
				 * httpRequest.getSession().getAttribute(Constants.FIELDS.getValue()); Fields
				 * sessionFields = null; if (null != previousFields) { sessionFields = (Fields)
				 * previousFields; } else { // TODO: Handle } sessionFields.put(responseMap);
				 */
				// Retry Transaction Block End
				// Sending Email for Transaction Status to merchant TODO...
				/*
				 * String countryCode = (String)
				 * httpRequest.getSession().getAttribute(FieldType.INTERNAL_CUST_COUNTRY_NAME.
				 * getName()); emailBuilder.postMan(responseMap, countryCode, user);
				 */

				Fields Fields = new Fields();
				Fields.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
				Fields.put(FieldType.STATUS.getName(), fields.get(FieldType.STATUS.getName()));
				Fields.put(FieldType.MOP_TYPE.getName(), fields.get(FieldType.MOP_TYPE.getName()));
				if (Boolean.valueOf(fields.get(FieldType.EPOS_MERCHANT.getName()))) {
					ExecutorService es = ThreadPoolProvider.getExecutorService();
					es.execute(new Runnable() {
						@Override
						public void run() {
							eposDao.updateEposCharges(Fields);
							Fields.removeInternalFields();
							Fields.removeSecureFields();
							Fields.remove(FieldType.ORIG_TXN_ID.getName());
							Fields.remove(FieldType.HASH.getName());
						}
					});
					es.shutdown();
				}

				if (StringUtils
						.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.RETURN_URL.getName()))) {
					fields.put((FieldType.RETURN_URL.getName()),
							(String) httpRequest.getSession().getAttribute(FieldType.RETURN_URL.getName()));

				}

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
				responseMap.put(FieldType.IS_MERCHANT_HOSTED.getName(),
						(String) httpRequest.getSession().getAttribute(FieldType.IS_MERCHANT_HOSTED.getName()));
				if (httpRequest.getSession() != null) {
					logger.info("In validating session map for PAYU Response Action");
					httpRequest.getSession().setAttribute(Constants.TRANSACTION_COMPLETE_FLAG.getValue(),
							Constants.Y_FLAG.getValue());
					httpRequest.getSession().invalidate();
				}
				responseMap.remove(FieldType.HASH.getName());
				responseMap.remove(FieldType.TXN_KEY.getName());
				responseMap.remove(FieldType.ACQUIRER_TYPE.getName());
				responseMap.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
				responseCreator.create(responseMap);
				responseCreator.ResponsePost(responseMap, httpResponse);
				return responseMap.getFields();
			}
		} catch (

		Exception exception) {
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
							AcquirerType.getInstancefromCode(AcquirerType.PAYU.getCode()).getName())) {
						account = accountThis;
						break;
					}
				}
			}

			AccountCurrency accountCurrency = account.getAccountCurrency("356");
			String mId = accountCurrency.getMerchantId();
			String password = encryptDecryptService.decrypt(payId, accountCurrency.getPassword());
			Map<String, String> merchantdetailsMap = new HashMap<String, String>();
			merchantdetailsMap.put(FieldType.MERCHANT_ID.getName(), mId);
			merchantdetailsMap.put(FieldType.PASSWORD.getName(), password);
			return merchantdetailsMap;
		} else {
			User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
			Account account = null;
			Set<Account> accounts = user.getAccounts();

			if (accounts == null || accounts.size() == 0) {
				logger.info("No account found for Pay ID = " + fields.get(FieldType.PAY_ID.getName())
						+ " and ORDER ID = " + fields.get(FieldType.ORDER_ID.getName()));
			} else {
				for (Account accountThis : accounts) {
					if (accountThis.getAcquirerName().equalsIgnoreCase(
							AcquirerType.getInstancefromCode(AcquirerType.PAYU.getCode()).getName())) {
						account = accountThis;
						break;
					}
				}
			}

			AccountCurrency accountCurrency = account.getAccountCurrency(fields.get(FieldType.CURRENCY_CODE.getName()));
			String mId = accountCurrency.getMerchantId();
			String password = encryptDecryptService.decrypt(fields.get(FieldType.PAY_ID.getName()),
					accountCurrency.getPassword());
			Map<String, String> merchantdetailsMap = new HashMap<String, String>();
			merchantdetailsMap.put(FieldType.MERCHANT_ID.getName(), mId);
			merchantdetailsMap.put(FieldType.PASSWORD.getName(), password);
			return merchantdetailsMap;

		}

	}

}
