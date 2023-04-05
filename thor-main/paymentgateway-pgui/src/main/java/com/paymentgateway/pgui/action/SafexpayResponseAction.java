package com.paymentgateway.pgui.action;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pg.core.util.PayGateCryptoUtils;
import com.paymentgateway.pg.core.util.ResponseCreator;
import com.paymentgateway.pgui.action.service.RetryTransactionProcessor;

@Service
public class SafexpayResponseAction {

	private static Logger logger = LoggerFactory.getLogger(SafexpayResponseAction.class.getName());

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private PayGateCryptoUtils payGateCryptoUtils;

	@Autowired
	private ResponseCreator responseCreator;

	@Autowired
	private UserDao userDao;

	@Autowired
	private RetryTransactionProcessor retryTransactionProcessor;

	@Autowired
	private FieldsDao fieldsDao;

	private Fields responseMap = null;

	public void safexpayResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {
		try {
			Map<String, String[]> fieldMapObj = httpRequest.getParameterMap();
			Map<String, String> requestMap = new HashMap<String, String>();

			for (Entry<String, String[]> entry : fieldMapObj.entrySet()) {
				try {
					requestMap.put(entry.getKey(), entry.getValue()[0]);

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

			String pgRefNo = httpRequest.getParameter("pgRefNo");

			if (!requestMap.isEmpty()) {

				StringBuilder sb = new StringBuilder();
				Iterator itr = requestMap.entrySet().iterator();
				while (itr.hasNext()) {
					Map.Entry obj = (Entry) itr.next();
					sb.append(obj.getKey() + " = " + obj.getValue() + " ~");
				}
				logger.info("Response Map received from acquirer >>> " + sb.toString());
			} else {
				logger.info("Response Map received from acquirer is empty ");
			}

			Fields fields = new Fields();

			Map<String, String> sessionMap = new HashMap<String, String>();
			Enumeration<String> keys = httpRequest.getSession().getAttributeNames();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				sessionMap.put(key, (String) httpRequest.getSession().getAttribute(key).toString());
			}
			if (sessionMap.isEmpty()) {
				// Check if fields is empty by fetching PAY_ID
				if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
					logger.info("Fields is blank in session Map, getting payment data from DB for Safexpay");

					fields = fieldsDao.getPreviousForPgRefNum(pgRefNo);
					String checkout_Flag = "";
					boolean checkout_flag = userDao.getCheckoutFlag(fields.get(FieldType.PAY_ID.getName()));
					if (checkout_flag == false) {
						checkout_Flag = "N";
					} else {
						checkout_Flag = "Y";
					}
					fields.put(FieldType.CHECKOUT_JS_FLAG.getName(), checkout_Flag);
					httpRequest.getSession().setAttribute(FieldType.CHECKOUT_JS_FLAG.getName(), checkout_Flag);
					httpRequest.getSession().setAttribute(FieldType.RETURN_URL.getName(),
							fields.get(FieldType.RETURN_URL.getName()));
					/*
					 * String internalRequestFields = null; internalRequestFields =
					 * fieldsDao.getPreviousForOID(fields.get(FieldType.OID.getName()));
					 * 
					 * if (StringUtils.isBlank(internalRequestFields)) { logger.
					 * info("New Order entry not found for this OID , getting data from SENT TO BANK "
					 * + fields.get(FieldType.OID.getName())); internalRequestFields =
					 * fieldsDao.getPreviousForOIDSTB(fields.get(FieldType.OID.getName())); } else {
					 * logger.info("New Order entry found for this OID in New Order - Pending txn" +
					 * fields.get(FieldType.OID.getName())); }
					 * 
					 * String[] paramaters = internalRequestFields.split("~"); Map<String, String>
					 * paramMap = new HashMap<String, String>(); for (String param : paramaters) {
					 * String[] parameterPair = param.split("="); if (parameterPair.length > 1) {
					 * paramMap.put(parameterPair[0].trim(), parameterPair[1].trim()); } }
					 */

					/*
					 * if (StringUtils.isNotBlank(paramMap.get(FieldType.RETURN_URL.getName()))) {
					 * logger.info("Return URL found for ORDER ID " +
					 * paramMap.get(FieldType.ORDER_ID.getName()) + "Return URL >> " +
					 * paramMap.get(FieldType.RETURN_URL.getName()));
					 * fields.put(FieldType.RETURN_URL.getName(),
					 * paramMap.get(FieldType.RETURN_URL.getName()));
					 * sessionMap.put(FieldType.RETURN_URL.getName(),
					 * paramMap.get(FieldType.RETURN_URL.getName())); } else {
					 * logger.info("Return URL not found for ORDER ID " +
					 * paramMap.get(FieldType.ORDER_ID.getName())); }
					 */

					/*
					 * if
					 * (StringUtils.isNotBlank(paramMap.get(FieldType.IS_MERCHANT_HOSTED.getName()))
					 * ) { logger.info( "IS_MERCHANT_HOSTED flag found for ORDER ID " +
					 * paramMap.get(FieldType.ORDER_ID.getName()));
					 * fields.put(FieldType.IS_MERCHANT_HOSTED.getName(),
					 * paramMap.get(FieldType.IS_MERCHANT_HOSTED.getName()));
					 * sessionMap.put(FieldType.IS_MERCHANT_HOSTED.getName(),
					 * paramMap.get(FieldType.IS_MERCHANT_HOSTED.getName())); } else { logger.info(
					 * "IS_MERCHANT_HOSTED not found for ORDER ID " +
					 * paramMap.get(FieldType.ORDER_ID.getName())); }
					 */

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
			String txnKey = (String) httpRequest.getSession().getAttribute(FieldType.TXN_KEY.getName());

			if (StringUtils.isNotBlank(txnKey)) {
				logger.info("Key found in session for Safexpay decryption");

			} else {
				logger.info("Key not found in session for Safexpay decryption as sessionMap is null");

				if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
					fields = fieldsDao.getPreviousForPgRefNum(pgRefNo);
					String internalRequestFields = fieldsDao.getPreviousForOID(fields.get(FieldType.OID.getName()));
					String[] paramaters = internalRequestFields.split("~");
					Map<String, String> paramMap = new HashMap<String, String>();
					for (String param : paramaters) {
						String[] parameterPair = param.split("=");
						if (parameterPair.length > 1) {
							paramMap.put(parameterPair[0].trim(), parameterPair[1].trim());
						}
					}
					fields.put(FieldType.RETURN_URL.getName(), paramMap.get(FieldType.RETURN_URL.getName()));
					httpRequest.getSession().setAttribute(FieldType.RETURN_URL.getName(),
							paramMap.get(FieldType.RETURN_URL.getName()));
					txnKey = getTxnKey(fields);
				} else {
					txnKey = getTxnKey(fields);
				}

			}

			String other_details = requestMap.get("other_details");
			String pg_details = requestMap.get("pg_details");
			String txn_response = requestMap.get("txn_response");
			String fraud_details = requestMap.get("fraud_details");

			String other_details_dec = "";
			String pg_details_dec = "";
			String txn_response_dec = "";
			String fraud_details_dec = "";

			if (StringUtils.isNotBlank(other_details)) {
				other_details_dec = payGateCryptoUtils.decrypt(other_details, txnKey);
			}

			if (StringUtils.isNotBlank(pg_details)) {
				pg_details_dec = payGateCryptoUtils.decrypt(pg_details, txnKey);
			}

			if (StringUtils.isNotBlank(txn_response)) {
				txn_response_dec = payGateCryptoUtils.decrypt(txn_response, txnKey);
			}

			if (StringUtils.isNotBlank(fraud_details)) {
				fraud_details_dec = payGateCryptoUtils.decrypt(fraud_details, txnKey);
			}

			StringBuilder safexpayResponse = new StringBuilder();
			safexpayResponse.append(other_details_dec);
			safexpayResponse.append("&&");
			safexpayResponse.append(pg_details_dec);
			safexpayResponse.append("&&");
			safexpayResponse.append(txn_response_dec);
			safexpayResponse.append("&&");
			safexpayResponse.append(fraud_details_dec);

			logger.info("Decrypted response received from Safexpay: " + " other_details_dec  >> " + other_details_dec
					+ " pg_details_dec  >> " + pg_details_dec + " txn_response_dec  >> " + txn_response_dec
					+ " fraud_details_dec  >> " + fraud_details_dec);

			fields.put(FieldType.SAFEXPAY_RESPONSE_FIELD.getName(), safexpayResponse.toString());

			fields.logAllFields("Updated 3DS Recieved Map TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " "
					+ "Txn id = " + fields.get(FieldType.TXN_ID.getName()));
			fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.SAFEXPAY.getCode());

			if (StringUtils.isNotBlank(
					(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()))) {
				fields.put(FieldType.TXNTYPE.getName(),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
			}

			if (StringUtils
					.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.PAYMENTS_REGION.getName()))) {
				fields.put((FieldType.PAYMENTS_REGION.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.PAYMENTS_REGION.getName()));
			}

			if (StringUtils
					.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.CARD_HOLDER_TYPE.getName()))) {
				fields.put((FieldType.CARD_HOLDER_TYPE.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.CARD_HOLDER_TYPE.getName()));
			}

			if (StringUtils.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.OID.getName()))) {
				fields.put((FieldType.OID.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.OID.getName()));

			}

			if (StringUtils.isNotBlank(fields.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()))) {
				httpRequest.getSession().setAttribute(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(),
						fields.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()))) {
				httpRequest.getSession().setAttribute(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(),
						fields.get(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()));
			}

			fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
			Map<String, String> response = transactionControllerServiceProvider.transact(fields,
					Constants.TXN_WS_SAFEXPAY_PROCESSOR.getValue());
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
					httpRequest.getSession().removeAttribute(FieldType.BOB_FINAL_REQUEST.getName());
					httpRequest.getSession().removeAttribute(FieldType.BOB_RESPONSE_FIELD.getName());
					httpRequest.getSession().removeAttribute(FieldType.ACQUIRER_TYPE.getName());
					httpRequest.getSession().setAttribute(FieldType.RETRY_FLAG.getName(), "Y");
					responseMap.put(FieldType.RETRY_URL.getName(),
							PropertiesManager.propertiesMap.get(FieldType.RETRY_URL.getName()));
					responseCreator.ResponsePost(responseMap, httpResponse);
//					return Action.NONE;
				}

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
			responseMap.remove(FieldType.IS_INTERNAL_REQUEST.getName());
			responseCreator.create(responseMap);
			responseCreator.ResponsePost(responseMap, httpResponse);

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
	}

	public String getTxnKey(Fields fields) throws SystemException {

		User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		Account account = null;
		Set<Account> accounts = user.getAccounts();

		if (accounts == null || accounts.size() == 0) {
			logger.info("No account found for Pay ID = " + fields.get(FieldType.PAY_ID.getName()) + " and ORDER ID = "
					+ fields.get(FieldType.ORDER_ID.getName()));
		} else {
			for (Account accountThis : accounts) {
				if (accountThis.getAcquirerName().equalsIgnoreCase(
						AcquirerType.getInstancefromCode(AcquirerType.SAFEXPAY.getCode()).getName())) {
					account = accountThis;
					break;
				}
			}
		}

		AccountCurrency accountCurrency = account.getAccountCurrency(fields.get(FieldType.CURRENCY_CODE.getName()));
		String txnKey = accountCurrency.getTxnKey();
		return txnKey;

	}
}
