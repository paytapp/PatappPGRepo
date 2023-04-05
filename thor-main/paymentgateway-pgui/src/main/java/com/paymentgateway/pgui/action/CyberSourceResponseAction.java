package com.paymentgateway.pgui.action;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
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
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.threadpool.ThreadPoolProvider;
import com.paymentgateway.pg.core.util.ResponseCreator;
import com.paymentgateway.pgui.action.beans.SessionCleaner;
import com.paymentgateway.pgui.action.service.RetryTransactionProcessor;

@Service
public class CyberSourceResponseAction {

	private static Logger logger = LoggerFactory.getLogger(CyberSourceResponseAction.class.getName());

	private Fields responseMap = null;

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private EPOSTransactionDao eposDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private RetryTransactionProcessor retryTransactionProcessor;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private ResponseCreator responseCreator;

	public void cyberSourceResponsehandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {
		try {
			String pgRefNo = httpRequest.getParameter("pgRefNo");
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

			// Log all entries from requestMap
			if (!requestMap.isEmpty()) {

				StringBuilder sb = new StringBuilder();
				Iterator itr = requestMap.entrySet().iterator();
				while (itr.hasNext()) {
					Map.Entry obj = (Entry) itr.next();
					sb.append(obj.getKey() + " = " + obj.getValue() + " ~");
				}
				logger.info("Request Map received >>> " + sb.toString());
			} else {
				logger.info("Request Map is empty ");
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

					if (obj.getKey().toString().equalsIgnoreCase(FieldType.TXN_KEY.getName())) {
						logger.info("Txn Key Present in FIELDS map");
						continue;
					}
					sb.append(obj.getKey() + " = " + obj.getValue() + " ~");
				}
			} else {
				logger.info("Session Map is empty");
			}

			String paRes = requestMap.get("PaRes");
			String md = requestMap.get("MD");

			Fields fields = new Fields();
			fields.put(FieldType.MD.getName(), md);
			fields.put(FieldType.PARES.getName(), paRes);

			fields.logAllFields("3DS Recieved Map :");

			if (sessionMap.isEmpty()) {
				// Check if fields is empty
				if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
					logger.info("FIELDS is blank in session Map, getting data from DB");

					fields = fieldsDao.getPreviousForPgRefNum(pgRefNo);
					String internalRequestFields = fields.get(FieldType.INTERNAL_REQUEST_FIELDS.getName());
					// String internalRequestFields =
					// fieldsDao.getPreviousForOID(fields.get(FieldType.OID.getName()));
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

					fieldsDao.getSessionParam(fields);
					httpRequest.getSession().setAttribute(FieldType.CARD_NUMBER.getName(), fields.get("RFU1"));
					httpRequest.getSession().setAttribute(FieldType.CARD_EXP_DT.getName(), fields.get("RFU2"));
					httpRequest.getSession().setAttribute(FieldType.CVV.getName(), fields.get("RFU3"));
					httpRequest.getSession().setAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(),
							TransactionType.SALE.getName());
					fields.remove("RFU1");
					fields.remove("RFU2");
					fields.remove("RFU3");
					fields.put(FieldType.MD.getName(), md);
					fields.put(FieldType.PARES.getName(), paRes);
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
			fields.logAllFields("Updated 3DS Recieved Map TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " "
					+ "Txn id = " + fields.get(FieldType.TXN_ID.getName()));
			fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.AXISBANKCB.getCode());
			if (StringUtils.isNotBlank(
					(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()))) {
				fields.put(FieldType.TXNTYPE.getName(),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
			}
			fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
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
					.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_CUST_IP.getName()))) {
				fields.put((FieldType.INTERNAL_CUST_IP.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_CUST_IP.getName()));
			}
			if (StringUtils.isNotBlank(
					(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()))) {
				fields.put((FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()));
			}
			/*
			 * fields.put((FieldType.INTERNAL_CARD_ISSUER_BANK.getName()), (String)
			 * sessionMap.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));
			 * fields.put((FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()), (String)
			 * sessionMap.get(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()));
			 */
			/*
			 * fields.put((FieldType.INTERNAL_ORIG_TXN_ID.getName()), (String)
			 * sessionMap.get(FieldType.INTERNAL_ORIG_TXN_ID.getName()));
			 */
			if (StringUtils.isNotBlank(
					(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_ID.getName()))) {
				fields.put((FieldType.INTERNAL_ORIG_TXN_ID.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_ID.getName()));
			}
			// fields.put((FieldType.CVV.getName()), (String)
			// sessionMap.get(FieldType.CVV.getName()));
			if (StringUtils.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.CVV.getName()))) {
				fields.put((FieldType.CVV.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.CVV.getName()));
			}
			// fields.put((FieldType.CARD_NUMBER.getName()), (String)
			// sessionMap.get(FieldType.CARD_NUMBER.getName()));
			if (StringUtils
					.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.CARD_NUMBER.getName()))) {
				fields.put((FieldType.CARD_NUMBER.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.CARD_NUMBER.getName()));
			}
			// fields.put((FieldType.CARD_EXP_DT.getName()), (String)
			// sessionMap.get(FieldType.CARD_EXP_DT.getName()));
			if (StringUtils
					.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.CARD_EXP_DT.getName()))) {
				fields.put((FieldType.CARD_EXP_DT.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.CARD_EXP_DT.getName()));
			}
			// fields.put((FieldType.ACQUIRER_MODE.getName()), (String)
			// sessionMap.get(FieldType.ACQUIRER_MODE.getName()));
			if (StringUtils
					.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.ACQUIRER_MODE.getName()))) {
				fields.put((FieldType.ACQUIRER_MODE.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.ACQUIRER_MODE.getName()));
			}
			// fields.put((FieldType.TENURE.getName()), (String)
			// sessionMap.get(FieldType.TENURE.getName()));
			if (StringUtils.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.TENURE.getName()))) {
				fields.put((FieldType.TENURE.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.TENURE.getName()));
			}
			/*
			 * fields.put((FieldType.RATE_OF_INTEREST.getName()), (String)
			 * sessionMap.get(FieldType.RATE_OF_INTEREST.getName()));
			 */
			if (StringUtils
					.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.RATE_OF_INTEREST.getName()))) {
				fields.put((FieldType.RATE_OF_INTEREST.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.RATE_OF_INTEREST.getName()));
			}
			// fields.put((FieldType.EMI_PER_MONTH.getName()), (String)
			// sessionMap.get(FieldType.EMI_PER_MONTH.getName()));
			if (StringUtils
					.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.EMI_PER_MONTH.getName()))) {
				fields.put((FieldType.EMI_PER_MONTH.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.EMI_PER_MONTH.getName()));
			}
			/*
			 * fields.put((FieldType.EMI_TOTAL_AMOUNT.getName()), (String)
			 * sessionMap.get(FieldType.EMI_TOTAL_AMOUNT.getName()));
			 */
			if (StringUtils
					.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.EMI_TOTAL_AMOUNT.getName()))) {
				fields.put((FieldType.EMI_TOTAL_AMOUNT.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.EMI_TOTAL_AMOUNT.getName()));
			}
			// fields.put((FieldType.ISSUER_BANK.getName()), (String)
			// sessionMap.get(FieldType.ISSUER_BANK.getName()));
			if (StringUtils
					.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.ISSUER_BANK.getName()))) {
				fields.put((FieldType.ISSUER_BANK.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.ISSUER_BANK.getName()));
			}
			// fields.put((FieldType.EMI_INTEREST.getName()), (String)
			// sessionMap.get(FieldType.EMI_INTEREST.getName()));
			if (StringUtils
					.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.EMI_INTEREST.getName()))) {
				fields.put((FieldType.EMI_INTEREST.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.EMI_INTEREST.getName()));
			}
			fields.remove(FieldType.TXN_ID.getName());
			// fields.put((FieldType.OID.getName()), (String)
			// sessionMap.get(FieldType.OID.getName()));
			if (StringUtils.isNotBlank((String) httpRequest.getSession().getAttribute(FieldType.OID.getName()))) {
				fields.put((FieldType.OID.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.OID.getName()));
			}
			Map<String, String> response = transactionControllerServiceProvider.transact(fields,
					Constants.TXN_WS_INTERNAL.getValue());
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
					httpRequest.getSession().setAttribute(FieldType.RETRY_FLAG.getName(), "Y");
					httpRequest.getSession().removeAttribute(FieldType.ACQUIRER_TYPE.getName());
					responseMap.put(FieldType.RETRY_URL.getName(),
							PropertiesManager.propertiesMap.get(FieldType.RETRY_URL.getName()));
					responseCreator.ResponsePost(responseMap, httpResponse);
//					return Action.NONE;
				}

			}
			// Retry Transaction Block End

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
			/*
			 * Object previousFields = sessionMap.get(Constants.FIELDS.getValue()); Fields
			 * sessionFields = null; if (null != previousFields) { sessionFields = (Fields)
			 * previousFields; } else { // TODO: Handle }
			 */

			// Sending Email for Transaction Status to merchant

			// String countryCode = (String)
			// sessionMap.get(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName());

			// TODO emailBuilder.postMan(responseMap, countryCode, user);

			// sessionFields.put(responseMap);
			fields.put(FieldType.RETURN_URL.getName(),
					(String) httpRequest.getSession().getAttribute(FieldType.RETURN_URL.getName()));

			if (StringUtils.isNotBlank(
					(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()))) {
				responseMap.put(FieldType.CARD_ISSUER_BANK.getName(),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));

			}
			if (StringUtils.isNotBlank(
					(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()))) {
				responseMap.put(FieldType.CARD_ISSUER_COUNTRY.getName(), (String) httpRequest.getSession()
						.getAttribute(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()));

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
			fieldsDao.deleteSessionParam(fields);
			responseMap.remove(FieldType.ACQUIRER_TYPE.getName());
			responseMap.remove(FieldType.ORIG_TXN_ID.getName());
			responseMap.remove(FieldType.MD.getName());
			responseMap.remove(FieldType.MERCHANT_ID.getName());
			responseMap.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
			responseCreator.create(responseMap);
			responseCreator.ResponsePost(responseMap, httpResponse);

		} catch (Exception exception) {
			logger.error("Exception", exception);
//			return ERROR;
			String path = httpRequest.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host") + "/pgui/jsp/error";
				httpResponse.sendRedirect(resultPath);
			}
			httpResponse.sendRedirect("error");
		}
	}
}
