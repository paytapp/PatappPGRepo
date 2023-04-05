package com.paymentgateway.pgui.action;

import java.io.IOException;
import java.util.HashMap;
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
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.threadpool.ThreadPoolProvider;
import com.paymentgateway.pg.core.util.ResponseCreator;

/**
 * @author Sunil, Neeraj
 *
 */
@Service
public class MigsResponseAction {

	private Logger logger = LoggerFactory.getLogger(MigsResponseAction.class.getName());

	private Fields responseMap = null;

	@Autowired
	TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private UserDao userDao;

	@Autowired
	private ResponseCreator responseCreator;

	@Autowired
	EPOSTransactionDao eposDao;

	public void migsResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {
		try {
			Map<String, String[]> fieldMapObj = httpRequest.getParameterMap();
			Map<String, String> migsResponseMap = new HashMap<String, String>();

			for (Entry<String, String[]> entry : fieldMapObj.entrySet()) {
				try {
					migsResponseMap.put(entry.getKey().trim(), ((String[]) entry.getValue())[0].trim());

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
			String oId = migsResponseMap.get("vpc_OrderInfo");
			String currencyCode = fields.get("CURRENCY_CODE");
			String vpc_locale = migsResponseMap.get("vpc_Locale");
			fields.putAll(migsResponseMap);
			fields.logAllFields("Migs 3DS map: ");
			logger.info("MIGS ResponseMap Received ->3DS map " + migsResponseMap);
			try {

				fields.put((FieldType.INTERNAL_ORIG_TXN_TYPE.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
				fields.put(FieldType.CARD_NUMBER.getName(),
						(String) httpRequest.getSession().getAttribute(FieldType.CARD_NUMBER.getName()));
				fields.put(FieldType.CARD_EXP_DT.getName(),
						(String) httpRequest.getSession().getAttribute(FieldType.CARD_EXP_DT.getName()));
				fields.put(FieldType.CVV.getName(),
						(String) httpRequest.getSession().getAttribute(FieldType.CVV.getName()));
				fields.put((FieldType.INTERNAL_CARD_ISSUER_BANK.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));
				fields.put((FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()), (String) httpRequest.getSession()
						.getAttribute(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()));
				fields.put(FieldType.OID.getName(), oId);
				fields.put(FieldType.CURRENCY_CODE.getName(), currencyCode);
				fields.put(FieldType.VPC_LOCALE.getName(), vpc_locale);
				fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.AXISMIGS.getCode());

				Map<String, String> response = transactionControllerServiceProvider.migsTransact(fields,
						Constants.TXN_MIGS_PROCESSOR.getValue());
				responseMap = new Fields(response);

				String pgFlag = (String) httpRequest.getSession()
						.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
				if (StringUtils.isNotBlank(pgFlag)) {
					responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
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

				// TODO... run time transaction failed retry to payment page
				/*
				 * fields.put(FieldType.ACQUIRER_TYPE.getName(), (String)
				 * httpRequest.getSession().getAttribute(FieldType.ACQUIRER_TYPE .getName()));
				 */

				// Fetch user for retryTransaction ,SendEmailer and SmsSenser

				User user = userDao.getUserClass(responseMap.get(FieldType.PAY_ID.getName()));

				// TODO.. Retry Transaction Temporary block when retry transaction will be
				// checked then will do work on this.
				// Retry Transaction Block Start
				/*
				 * if (!responseMap.get(FieldType.RESPONSE_CODE.getName()).equals(
				 * ErrorType.SUCCESS.getCode())) {
				 * 
				 * if (retryTransactionProcessor.retryTransaction(fields, sessionMap, user)) {
				 * addActionMessage(CrmFieldConstants.RETRY_TRANSACTION .getValue()); return
				 * "paymentPage"; } }
				 */
				// Retry Transaction Block End

				// Sending Email for Transaction Status to merchant
				// TODO countryCode put in Fiels object.........................................
				String countryCode = (String) httpRequest.getSession()
						.getAttribute(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName());

				// sendTransactionEmail.sendEmail(responseMap);

				// emailBuilder.postMan(fields, countryCode, user);

				responseMap.put(FieldType.RETURN_URL.getName(),
						(String) httpRequest.getSession().getAttribute(FieldType.RETURN_URL.getName()));
			} catch (SystemException systemException) {
				logger.error("Exception", systemException);
				responseMap.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.INTERNAL_SYSTEM_ERROR.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INTERNAL_SYSTEM_ERROR.getResponseCode());
				String pgFlag = (String) httpRequest.getSession()
						.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
				if (StringUtils.isNotBlank(pgFlag)) {
					responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
				}
				responseCreator.create(responseMap);
				responseCreator.ResponsePost(responseMap, httpResponse);
//				return NONE;
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
}
