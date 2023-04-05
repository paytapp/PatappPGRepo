package com.paymentgateway.pgui.action;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
 * @author Sunil, Neeraj,Rahul
 *
 */
public class ResponseHandler {

	private static Logger logger = LoggerFactory.getLogger(ResponseHandler.class.getName());

	private Fields responseMap = null;
	private String redirectUrl;
	private Integer count;

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private UserDao userDao;

	@Autowired
	private RetryTransactionProcessor retryTransactionProcessor;

	@Autowired
	private ResponseCreator responseCreator;

	public void responseHandling(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		try {
			Map<String, String[]> fieldMapObj = httpRequest.getParameterMap();
			Map<String, String> requestMap = new HashMap<String, String>();

			for (Entry<String, String[]> entry : fieldMapObj.entrySet()) {
				try {
					requestMap.put(entry.getKey(), ((String[]) entry.getValue())[0]);

				} catch (ClassCastException classCastException) {
					logger.error("Exception", classCastException);
				}
			}

			String paRes = requestMap.get("PaRes");
			String md = requestMap.get("MD");

			Fields fields = new Fields();
			fields.put(FieldType.MD.getName(), md);
			fields.put(FieldType.PARES.getName(), paRes);

			fields.logAllFields("3DS Recieved Map :");

			Object fieldsObj = (Fields) httpRequest.getSession().getAttribute("FIELDS");

			if (null != fieldsObj) {
				fields.put((Fields) fieldsObj);
			}

			fields.logAllFields("Updated 3DS Recieved Map TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " "
					+ "Txn id = " + fields.get(FieldType.TXN_ID.getName()));
			fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.FSS.getCode());
			fields.put(FieldType.TXNTYPE.getName(),
					(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
			fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
			fields.put((FieldType.INTERNAL_CARD_ISSUER_BANK.getName()),
					(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));
			fields.put((FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()),
					(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()));
			fields.put((FieldType.PAYMENTS_REGION.getName()),
					(String) httpRequest.getSession().getAttribute(FieldType.PAYMENTS_REGION.getName()));
			fields.put((FieldType.CARD_HOLDER_TYPE.getName()),
					(String) httpRequest.getSession().getAttribute(FieldType.CARD_HOLDER_TYPE.getName()));
			fields.remove(FieldType.TXN_ID.getName());
			fields.put((FieldType.OID.getName()),
					(String) httpRequest.getSession().getAttribute(FieldType.OID.getName()));
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
					// addActionMessage(CrmFieldConstants.RETRY_TRANSACTION.getValue());
					httpRequest.getSession().setAttribute(FieldType.RETRY_FLAG.getName(), "Y");
					httpRequest.getSession().removeAttribute(FieldType.ACQUIRER_TYPE.getName());
					responseMap.put(FieldType.RETRY_URL.getName(),
							PropertiesManager.propertiesMap.get(FieldType.RETRY_URL.getName()));
					responseCreator.ResponsePost(responseMap, httpResponse);
//					return Action.NONE;
				}

			}

			/*
			 * Object previousFields = sessionMap.get(Constants.FIELDS.getValue()); Fields
			 * sessionFields = null; if (null != previousFields) { sessionFields = (Fields)
			 * previousFields; } else { // TODO: Handle } sessionFields.put(responseMap);
			 */
			// Retry Transaction Block End
			// Sending Email for Transaction Status to merchant TODO...
			/*
			 * String countryCode = (String)
			 * sessionMap.get(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName());
			 * emailBuilder.postMan(responseMap, countryCode, user);
			 */

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
		}
//		return Action.NONE;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

}
