package com.paymentgateway.pgui.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.pg.core.util.ResponseCreator;
import com.paymentgateway.pgui.action.beans.SearchTransactionActionBean;
import com.paymentgateway.pgui.action.service.RetryTransactionProcessor;

@Service
public class SearchTransactionAction {

	private static Logger logger = LoggerFactory.getLogger(SearchTransactionAction.class.getName());

	@Autowired
	private UserDao userDao;
	@Autowired
	private ResponseCreator responseCreator;
	
	@Autowired
	private SearchTransactionActionBean searchTransactionActionBean;

	@Autowired
	private RetryTransactionProcessor retryTransactionProcessor;
	
	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	public Map<String, String> verifyUpiRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			Map<String, String> reqMap) throws IOException {
		Map<String, String> responseFields = new HashMap<String, String>();
		if (StringUtils.isNotBlank(reqMap.get("pgRefNum"))) {
			logger.info("Looking for transctions with the PG_REF_NUM " + reqMap.get("pgRefNum"));
		} else {
			logger.info("Looking for transctions with the OID " + reqMap.get("oid"));
		}

		Map<String, String> responseMap = new HashMap<String, String>();

		responseMap = searchTransactionActionBean.searchPayment(reqMap.get("pgRefNum"), reqMap.get("oid"));

		if (responseMap == null) {
			responseFields.put("transactionStatus", StatusType.SENT_TO_BANK.getName().toString());
			return responseFields;
		}

		else {
			try {
				Fields fields = null;
				if (reqMap.containsKey(FieldType.CHECKOUT_JS_FLAG.getName()) && reqMap.get(FieldType.CHECKOUT_JS_FLAG.getName()).equalsIgnoreCase("true")) {
					httpRequest.getSession().invalidate();
				} else {
					fields = (Fields) httpRequest.getSession().getAttribute(Constants.FIELDS.getValue());
				}
				if (null != fields) {
				} else {
					if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("ADMIN_PAYID"))
							&& StringUtils.isNotBlank(reqMap.get("encSessionData"))) {
						Map<String, String> fieldsMap = new HashMap<String, String>();
						Map<String, String> responseDecryptMap = transactionControllerServiceProvider.hostedDecrypt(
								PropertiesManager.propertiesMap.get("ADMIN_PAYID"), reqMap.get("encSessionData"));
						if (!responseDecryptMap.isEmpty()) {
							String decryptedString = responseDecryptMap.get(FieldType.ENCDATA.getName());
							String[] fieldArray = decryptedString.split("~");

							for (String key : fieldArray) {
								String[] namValuePair = key.split("=", 2);
								httpRequest.getSession().setAttribute(namValuePair[0], namValuePair[1]);
							}
						}
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
						logger.info(fieldsMap.toString());

					} else {
						logger.info("session fields lost");
						String path = httpRequest.getContextPath();
						logger.info(path);
						if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
							String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host") + "/pgui/jsp/error";
							httpResponse.sendRedirect(resultPath);
						}
						httpResponse.sendRedirect("error");
					}
				}

				fields.putAll(responseMap);

				// Fetch user for retryTransaction ,SendEmailer and SmsSenser

				User user = userDao.getUserClass(responseMap.get(FieldType.PAY_ID.getName()));

				// Retry Transaction Block Start
				if (!(responseMap.get(FieldType.STATUS.getName())
						.equalsIgnoreCase(StatusType.CAPTURED.getName().toString())
						|| responseMap.get(FieldType.STATUS.getName())
								.equalsIgnoreCase(StatusType.SENT_TO_BANK.getName().toString()))) {

					if (retryTransactionProcessor.retryTransaction(new Fields(responseMap), httpRequest, user)) {
//						addActionMessage(CrmFieldConstants.RETRY_TRANSACTION.getValue());
						httpRequest.getSession().removeAttribute(FieldType.ACQUIRER_TYPE.getName());
						httpRequest.getSession().setAttribute(FieldType.RETRY_FLAG.getName(), "Y");
						fields.put(FieldType.RETRY_URL.getName(),
								PropertiesManager.propertiesMap.get(FieldType.RETRY_URL.getName()));
						responseCreator.ResponsePost(new Fields(responseMap), httpResponse);
//						return Action.NONE;
					}

				}

				String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());

				if (fields.get(FieldType.AMOUNT.getName()) != null) {
					String amount = fields.get(FieldType.AMOUNT.getName());
					fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(amount, currencyCode));
				}

				if (fields.get(FieldType.TOTAL_AMOUNT.getName()) != null) {
					String upTotalAmount = fields.get(FieldType.TOTAL_AMOUNT.getName());
					fields.put(FieldType.TOTAL_AMOUNT.getName(), Amount.formatAmount(upTotalAmount, currencyCode));
				}
				String pgFlag = (String) httpRequest.getSession()
						.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());

				fields.put(FieldType.RETURN_URL.getName(),
						httpRequest.getSession().getAttribute(FieldType.RETURN_URL.getName()).toString());
				fields.remove(FieldType.HASH.getName());
				fields.remove(FieldType.ORIG_TXN_ID.getName());
				responseCreator.create(fields);
				// transactionResponser.addHash(field);
				logger.info("Fields after response received after STB ...   " + fields);
				responseFields.put("transactionStatus", fields.get(FieldType.STATUS.getName()));
				Fields uiFields = new Fields();
				if (StringUtils.isNotBlank(reqMap.get("oid"))) {
					uiFields.put(FieldType.OID.getName(), reqMap.get("oid"));
				}
				uiFields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName()));
				uiFields.put(FieldType.RETURN_URL.getName(), fields.get(FieldType.RETURN_URL.getName()));
				for (String key : uiFields.keySet()) {
					responseFields.put(key, uiFields.get(key));
				}

				return responseFields;
			} catch (Exception e) {
				logger.error("Exception in SearchTransactionAction ", e);
				String path = httpRequest.getContextPath();
				logger.info(path);
				if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
					String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host") + "/pgui/jsp/error";
					httpResponse.sendRedirect(resultPath);
				}
				httpResponse.sendRedirect("error");
			}

		}
		return responseFields;
	}
}
