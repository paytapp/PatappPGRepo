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
import com.paymentgateway.pg.core.util.ResponseCreator;
import com.paymentgateway.pgui.action.service.RetryTransactionProcessor;

@Service
public class IPintResponseAction {

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private ResponseCreator responseCreator;

	@Autowired
	private UserDao userDao;

	@Autowired
	private RetryTransactionProcessor retryTransactionProcessor;

	@Autowired
	private FieldsDao fieldsDao;

	private Fields responseMap = null;
	private static Logger logger = LoggerFactory.getLogger(IPintResponseAction.class.getName());

	public void ipintResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {

		try {

			String txnId = null;
			Map<String, String[]> fieldMapObj = httpRequest.getParameterMap();
			logger.info("ipnint response recived " + fieldMapObj);
			Map<String, String> requestMap = new HashMap<String, String>();
			StringBuilder responseString = new StringBuilder();

			for (Entry<String, String[]> entry : fieldMapObj.entrySet()) {
				try {
					requestMap.put(entry.getKey(), entry.getValue()[0]);
					responseString.append(entry.getKey());
					responseString.append("=");
					responseString.append(entry.getValue()[0]);
					responseString.append(";");

					if (entry.getKey().equalsIgnoreCase("pgrefNum")) {
						String[] arrPGRF = (entry.getValue()[0]).split("\\?");
						txnId = arrPGRF[0];
					}

				} catch (ClassCastException classCastException) {
					logger.error("Exception", classCastException);
				}
			}

			logger.info("ipnint response recieved " + responseString);
			// Log all entries from requestMap

			// TODO tarun match

			if (!requestMap.isEmpty()) {

				StringBuilder sb = new StringBuilder();
				Iterator itr = requestMap.entrySet().iterator();
				while (itr.hasNext()) {
					Map.Entry obj = (Entry) itr.next();
					sb.append(obj.getKey() + " = " + obj.getValue() + " ~");
				}
				logger.info("Ipint Request Map received >>> " + sb.toString());
			} else {
				logger.info("Ipint Request Map is empty ");
			}
			Map<String, String> sessionMap = new HashMap<String, String>();
			Enumeration<String> keys = httpRequest.getSession().getAttributeNames();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				sessionMap.put(key, (String) httpRequest.getSession().getAttribute(key).toString());
			}
			// Log all entries from sessionMap
			if (!sessionMap.isEmpty()) {

				StringBuilder sb = new StringBuilder();
				Iterator itr = sessionMap.entrySet().iterator();
				while (itr.hasNext()) {
					Map.Entry obj = (Entry) itr.next();
					sb.append(obj.getKey() + " = " + obj.getValue() + " ~");
				}
				logger.info("Ipint sessionMap values >>> " + sb.toString());
			} else {
				logger.info("Ipint Session Map is empty");
			}

			Fields fields = new Fields();

			if (sessionMap.isEmpty()) {
				if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
					logger.info("FIELDS is blank in session Map, getting data from DB");

					fields = fieldsDao.getPreviousForPgRefNum(txnId);
					String checkout_Flag = "";
					boolean checkout_flag = userDao.getCheckoutFlag(fields.get(FieldType.PAY_ID.getName()));
					if (checkout_flag == false) {
						checkout_Flag = "N";
					} else {
						checkout_Flag = "Y";
					}
					fields.put(FieldType.CHECKOUT_JS_FLAG.getName(), checkout_Flag);
					httpRequest.getSession().setAttribute(FieldType.CHECKOUT_JS_FLAG.getName(), checkout_Flag);
					fields.put(FieldType.RETURN_URL.getName(), fields.get(FieldType.RETURN_URL.getName()));
					httpRequest.getSession().setAttribute(FieldType.RETURN_URL.getName(),
							fields.get(FieldType.RETURN_URL.getName()));
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
			// Check if fields is empty

			fields.put(FieldType.IPINT_RESPONSE_FIELD.getName(), responseString.toString());
			fields.logAllFields("Ipint Response Recieved :");

			fields.logAllFields("Updated 3DS Recieved Map TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " "
					+ "Txn id = " + fields.get(FieldType.TXN_ID.getName()));

			fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.IPINT.getCode());

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

			fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");

			if (StringUtils.isNotBlank(fields.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()))) {
				sessionMap.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(),
						fields.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()))) {
				sessionMap.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(),
						fields.get(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()));
			}

			Map<String, String> response = transactionControllerServiceProvider.transact(fields,
					Constants.TXN_WS_IPINT_PROCESSOR.getValue());
			responseMap = new Fields(response);

			String crisFlag = (String) httpRequest.getSession()
					.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
			if (StringUtils.isNotBlank(crisFlag)) {
				responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), crisFlag);
			}
			String isMerchantHosted = (String) httpRequest.getSession()
					.getAttribute(FieldType.IS_MERCHANT_HOSTED.getName());
			if (StringUtils.isNotBlank(isMerchantHosted)) {
				responseMap.put(FieldType.IS_MERCHANT_HOSTED.getName(), isMerchantHosted);
			}
			// Fetch user for retryTransaction ,SendEmailer and SmsSenser
			User user = userDao.getUserClass(responseMap.get(FieldType.PAY_ID.getName()));

			// Retry Transaction Block Start
			if (!responseMap.get(FieldType.RESPONSE_CODE.getName()).equals(ErrorType.SUCCESS.getCode())) {

				if (retryTransactionProcessor.retryTransaction(responseMap, httpRequest, user)) {
//					addActionMessage(CrmFieldConstants.RETRY_TRANSACTION.getValue());
					httpRequest.getSession().removeAttribute(FieldType.IPINT_FINAL_REQUEST.getName());
					httpRequest.getSession().removeAttribute(FieldType.IPINT_RESPONSE_FIELD.getName());
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
			responseMap.remove(FieldType.PASSWORD.getName());
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
//			return ERROR;
		}
	}

	public Map<String, String> getTxnKey(Fields fields) throws SystemException {

		Map<String, String> keyMap = new HashMap<String, String>();

		User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		Account account = null;
		Set<Account> accounts = user.getAccounts();

		if (accounts == null || accounts.size() == 0) {
			logger.info("No account found for Pay ID = " + fields.get(FieldType.PAY_ID.getName()) + " and ORDER ID = "
					+ fields.get(FieldType.ORDER_ID.getName()));
		} else {
			for (Account accountThis : accounts) {
				if (accountThis.getAcquirerName()
						.equalsIgnoreCase(AcquirerType.getInstancefromCode(AcquirerType.IPINT.getCode()).getName())) {
					account = accountThis;
					break;
				}
			}
		}

		AccountCurrency accountCurrency = account.getAccountCurrency(fields.get(FieldType.CURRENCY_CODE.getName()));

		keyMap.put("merchantId", accountCurrency.getMerchantId());
//		keyMap.put("password", aWSEncryptDecryptService.decrypt(accountCurrency.getPassword()));

		return keyMap;

	}

}
