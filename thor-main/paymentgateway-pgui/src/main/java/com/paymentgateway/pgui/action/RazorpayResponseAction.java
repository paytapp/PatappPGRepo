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
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.threadpool.ThreadPoolProvider;
import com.paymentgateway.pg.core.util.ResponseCreator;
import com.paymentgateway.pgui.action.service.RetryTransactionProcessor;

/**
 * @author Shaiwal
 *
 */
@Service
public class RazorpayResponseAction {

	private static Logger logger = LoggerFactory.getLogger(RazorpayResponseAction.class.getName());

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private ResponseCreator responseCreator;

	@Autowired
	private UserDao userDao;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private RetryTransactionProcessor retryTransactionProcessor;

	@Autowired
	private EPOSTransactionDao eposDao;

	private Fields responseMap = null;

	public void razorpayResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {
		try {
			Map<String, String[]> fieldMapObj = httpRequest.getParameterMap();
			Map<String, String> requestMap = new HashMap<String, String>();

			for (Entry<String, String[]> entry : fieldMapObj.entrySet()) {
				try {
					requestMap.put(entry.getKey(), ((String[]) entry.getValue())[0]);

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

			StringBuilder razorpayResponse = new StringBuilder();
			
			// Log all entries from requestMap
			if (!requestMap.isEmpty()) {

				Iterator itr = requestMap.entrySet().iterator();
				while (itr.hasNext()) {
					Map.Entry obj = (Entry) itr.next();
					razorpayResponse.append(obj.getKey() + " = " + obj.getValue() + " ~");
				}
			} else {
				logger.info("Request Map is empty ");
			}
			
			logger.info("Response received from Razorpay: " + razorpayResponse);
			String pgRefNo = httpRequest.getParameter("pgRefNo");
			logger.info("Razorpay Pg Ref Num from URL: " + pgRefNo);
			
			// Log all entries from sessionMap
			Map<String, String> sessionMap = new HashMap<String, String>();
			Enumeration<String> keys = httpRequest.getSession().getAttributeNames();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();

				sessionMap.put(key, (String) httpRequest.getSession().getAttribute(key).toString());
			}
			if (!sessionMap.isEmpty()) {

				StringBuilder sb = new StringBuilder();
				Iterator itr = sessionMap.entrySet().iterator();
				while (itr.hasNext()) {

					Map.Entry obj = (Entry) itr.next();

					if (obj.getKey().toString().equalsIgnoreCase(FieldType.TXN_KEY.getName())) {
						logger.info("Txn Key Present in FIELDS map");
						continue;
					}
					sb.append(obj.getKey() + " = " + obj.getValue() + " ~");
				}
			} else {
				logger.info("Session Map is empty");
			}

			Fields fields = new Fields();

			if (sessionMap.isEmpty()) {
				// Check if fields is empty
				if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
					logger.info("FIELDS is blank in session Map, getting data from DB");

					fields = fieldsDao.getPreviousForPgRefNum(pgRefNo);
					String internalRequestFields = fields.get(FieldType.INTERNAL_REQUEST_FIELDS.getName());
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
		
			String txnKey = (String) httpRequest.getSession().getAttribute(FieldType.TXN_KEY.getName());
			if (StringUtils.isNotBlank(txnKey)) {
				logger.info("Key found in session for Razorpay : ");

			} else {
				logger.info("Key not found in session for Razorpay : ");

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
					txnKey = getTxnKey(fields);
				} else {
					txnKey = getTxnKey(fields);
				}

			}

			StringBuilder razorpayResponseUpdated = new StringBuilder();
			
			// In case of Success
			if (requestMap.get("razorpay_payment_id") != null && requestMap.get("razorpay_order_id") != null && requestMap.get("razorpay_signature") != null) {
				
				razorpayResponseUpdated.append("razorpay_payment_id=");
				razorpayResponseUpdated.append(requestMap.get("razorpay_payment_id"));
				razorpayResponseUpdated.append("&&");
				razorpayResponseUpdated.append("razorpay_order_id=");
				razorpayResponseUpdated.append(requestMap.get("razorpay_order_id"));
				razorpayResponseUpdated.append("&&");
				razorpayResponseUpdated.append("razorpay_signature=");
				razorpayResponseUpdated.append(requestMap.get("razorpay_signature"));
			}
			
			else if (requestMap.get("error[code]") != null) {
				
				razorpayResponseUpdated.append("error_code=");
				razorpayResponseUpdated.append(requestMap.get("error[code]"));
				razorpayResponseUpdated.append("&&");
				razorpayResponseUpdated.append("error_description=");
				razorpayResponseUpdated.append(requestMap.get("error[description]"));
				razorpayResponseUpdated.append("&&");
				razorpayResponseUpdated.append("error_source=");
				razorpayResponseUpdated.append(requestMap.get("error[source]"));
				razorpayResponseUpdated.append("&&");
				razorpayResponseUpdated.append("error_step=");
				razorpayResponseUpdated.append(requestMap.get("error[step]"));
				razorpayResponseUpdated.append("&&");
				razorpayResponseUpdated.append("error_reason=");
				razorpayResponseUpdated.append(requestMap.get("error[reason]"));
				razorpayResponseUpdated.append("&&");
				razorpayResponseUpdated.append("error_metadata=");
				razorpayResponseUpdated.append(requestMap.get("error[metadata]"));
			}

			logger.info("Updated Response received from Razorpay: " + razorpayResponseUpdated.toString());
			
			fields.put(FieldType.RAZORPAY_RESPONSE_FIELD.getName(), razorpayResponseUpdated.toString());
			fields.logAllFields("Updated 3DS Recieved Map TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " "
					+ "Txn id = " + fields.get(FieldType.TXN_ID.getName()));
			fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.RAZORPAY.getCode());

			if (StringUtils.isNotBlank(
					(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()))) {
				fields.put(FieldType.TXNTYPE.getName(),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
			}
			if (StringUtils
					.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_CUST_IP.getName()))) {
				fields.put((FieldType.INTERNAL_CUST_IP.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_CUST_IP.getName()));
			}
			if (StringUtils.isNotBlank(
					(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()))) {
				fields.put((FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()));
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

			if (StringUtils
					.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.ACQUIRER_MODE.getName()))) {
				fields.put((FieldType.ACQUIRER_MODE.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.ACQUIRER_MODE.getName()));
			}

			if (StringUtils.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.TENURE.getName()))) {
				fields.put((FieldType.TENURE.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.TENURE.getName()));
			}

			if (StringUtils.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.OID.getName()))) {
				fields.put((FieldType.OID.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.OID.getName()));

			}

			if (StringUtils
					.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.RATE_OF_INTEREST.getName()))) {
				fields.put((FieldType.RATE_OF_INTEREST.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.RATE_OF_INTEREST.getName()));

			}

			if (StringUtils
					.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.EMI_PER_MONTH.getName()))) {
				fields.put((FieldType.EMI_PER_MONTH.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.EMI_PER_MONTH.getName()));

			}

			if (StringUtils
					.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.EMI_TOTAL_AMOUNT.getName()))) {
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
					Constants.TXN_WS_RAZORPAY_PROCESSOR.getValue());
			responseMap = new Fields(response);

			String pgFlag = (String) httpRequest.getSession()
					.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
			if (StringUtils.isNotBlank(pgFlag)) {
				responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
			}


			User user = userDao.getUserClass(responseMap.get(FieldType.PAY_ID.getName()));

			// Retry Transaction Block Start
			if (!responseMap.get(FieldType.RESPONSE_CODE.getName()).equals(ErrorType.SUCCESS.getCode())) {

				if (retryTransactionProcessor.retryTransaction(responseMap, httpRequest, user)) {
					sessionMap.remove(FieldType.RAZORPAY_FINAL_REQUEST.getName());
					sessionMap.remove(FieldType.RAZORPAY_RESPONSE_FIELD.getName());
					sessionMap.remove(FieldType.ACQUIRER_TYPE.getName());
					httpRequest.getSession().setAttribute(FieldType.RETRY_FLAG.getName(), "Y");
					responseMap.put(FieldType.RETRY_URL.getName(),
							PropertiesManager.propertiesMap.get(FieldType.RETRY_URL.getName()));
					responseCreator.ResponsePost(responseMap, httpResponse);
				}

			}


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
				logger.info("In validating session map for Razorpay Response Action");
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
				if (accountThis.getAcquirerName()
						.equalsIgnoreCase(AcquirerType.getInstancefromCode(AcquirerType.RAZORPAY.getCode()).getName())) {
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
