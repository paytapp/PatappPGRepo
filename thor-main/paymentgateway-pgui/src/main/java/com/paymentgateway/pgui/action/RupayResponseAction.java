package com.paymentgateway.pgui.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pg.core.util.ResponseCreator;
import com.paymentgateway.pgui.action.service.RetryTransactionProcessor;

/**
 * @author Rahul
 *
 */

@Service
public class RupayResponseAction {

	@Autowired
	TransactionControllerServiceProvider transactionControllerServiceProvider;

	private static Logger logger = LoggerFactory.getLogger(RupayResponseAction.class.getName());

	private Fields responseMap = null;

	@Autowired
	private UserDao userDao;

	@Autowired
	private RetryTransactionProcessor retryTransactionProcessor;

	@Autowired
	private ResponseCreator responseCreator;

	public void rupayResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
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

			String auth = requestMap.get("auth");
			String paymentId = requestMap.get("paymentid");
			String acqId = requestMap.get("tranid");
			String pgDateTime = requestMap.get("postdate");
			String avr = requestMap.get("avr");
			String rrn = requestMap.get("ref");
			String pgStatus = requestMap.get("result");
			String pgResponseCode = requestMap.get("authRespCode");

			Fields fields = new Fields();

			Object fieldsObj = null;
			if (StringUtils
					.isNotBlank(httpRequest.getSession().getAttribute(FieldType.CHECKOUT_JS_FLAG.getName()).toString())
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

			fields.put(FieldType.AUTH_CODE.getName(), auth);
			fields.put(FieldType.PAYMENT_ID.getName(), paymentId);
			fields.put(FieldType.ACQ_ID.getName(), acqId);
			fields.put(FieldType.PG_DATE_TIME.getName(), pgDateTime);
			fields.put(FieldType.AVR.getName(), avr);
			fields.put(FieldType.RRN.getName(), rrn);
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgStatus);
			fields.put(FieldType.PG_RESP_CODE.getName(), pgResponseCode);

			fields.logAllFields("Updated 3DS Recieved Map :");
			fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.FSS.getCode());
			fields.put(FieldType.TXNTYPE.getName(),
					(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
			fields.put((FieldType.PAYMENTS_REGION.getName()),
					(String) httpRequest.getSession().getAttribute(FieldType.PAYMENTS_REGION.getName()));
			fields.put((FieldType.CARD_HOLDER_TYPE.getName()),
					(String) httpRequest.getSession().getAttribute(FieldType.CARD_HOLDER_TYPE.getName()));
			fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");

			fields.remove(FieldType.TXN_ID.getName());
			fields.put((FieldType.OID.getName()),
					(String) httpRequest.getSession().getAttribute(FieldType.OID.getName()));
			Map<String, String> response = transactionControllerServiceProvider.transact(fields,
					Constants.TXN_WS_RUPAY_PROCESSOR.getValue());
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

			fields.put(FieldType.RETURN_URL.getName(),
					(String) httpRequest.getSession().getAttribute(FieldType.RETURN_URL.getName()));
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
