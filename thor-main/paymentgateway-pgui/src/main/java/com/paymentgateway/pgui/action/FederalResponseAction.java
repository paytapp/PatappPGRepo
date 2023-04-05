package com.paymentgateway.pgui.action;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.EPOSTransactionDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.threadpool.ThreadPoolProvider;
import com.paymentgateway.pg.core.util.ResponseCreator;
import com.paymentgateway.pg.core.util.TransactionResponser;

/**
 * @author Rahul
 *
 */

@Service
public class FederalResponseAction {

	@Autowired
	TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	EPOSTransactionDao eposDao;

	private static Logger logger = LoggerFactory.getLogger(FederalResponseAction.class.getName());

	private Fields responseMap = null;

	@Autowired
	private TransactionResponser transactionResponser;

	@Autowired
	private ResponseCreator responseCreator;

	public void federalResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
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

			for (Map.Entry<String, String> entry : requestMap.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				logger.info("response map after trim :  key = " + key + "      value = " + value);
			}

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

			String eci = requestMap.get(Constants.FEDERAL_ECI.getValue());
			String cavv = requestMap.get(Constants.FEDERAL_CAVV.getValue());
			String mpiErrorCode = requestMap.get(Constants.FEDERAL_MPI_ERROR_CODE.getValue());
			String xid = requestMap.get(Constants.FEDERAL_XID.getValue());
			String amount = requestMap.get(Constants.FEDERAL_AMOUNT.getValue());
			String receivedHash = requestMap.get(Constants.FEDERAL_MERCHANT_HASH.getValue());
			String md = requestMap.get(Constants.FEDERAL_MD.getValue());
			String currency = requestMap.get(Constants.FEDERAL_CURRENCY.getValue());
			String ID = requestMap.get(Constants.FEDERAL_ID.getValue());
			String status = requestMap.get(Constants.FEDERAL_STATUS.getValue());

			fields.put(FieldType.KEY_ID.getName(), ID);
			// fields.put(FieldType.AMOUNT.getName(), amount);
			fields.put(FieldType.CURRENCY_CODE.getName(), currency);
			fields.put(FieldType.RESPONSE_CODE.getName(), mpiErrorCode);

			fields.put(FieldType.FEDERAL_MPI_ID.getName(), ID);
			fields.put(FieldType.FEDERAL_MD.getName(), md);
			fields.put(FieldType.FEDERAL_ECI.getName(), eci);
			fields.put(FieldType.FEDERAL_XID.getName(), xid);
			fields.put(FieldType.FEDERAL_CAVV.getName(), cavv);
			fields.put(FieldType.FEDERAL_STATUS.getName(), status);

			String calculatedHash = getHash(fields, httpRequest);
			if (!calculatedHash.equals(receivedHash)) {
				StringBuilder hashMessage = new StringBuilder("Merchant hash =");
				hashMessage.append(receivedHash);
				hashMessage.append(", Calculated Hash=");
				hashMessage.append(calculatedHash);

				logger.error(hashMessage.toString());
				if (!PropertiesManager.propertiesMap.get("alloFailedResponseHash").equals("Y")) {
					handleInvalidHash(httpRequest, httpResponse);
				}
			}

			if (!mpiErrorCode.equals(Constants.SUCCESS_CODE.getValue())
					|| !status.equals(Constants.Y_FLAG.getValue())) {
				logger.info("Failure received from Fedral 3DS system");
				Fields field = (Fields) httpRequest.getSession().getAttribute(Constants.FIELDS.getValue());
				field.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
				field.put(FieldType.FEDERAL_MPIERROR_CODE.getName(), mpiErrorCode);
				field.put(FieldType.FEDERAL_STATUS.getName(), status);
				field.put((FieldType.OID.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.OID.getName()));
				field.put(FieldType.ORIG_TXN_ID.getName(),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_ID.getName()));
				fields.put((FieldType.PAYMENTS_REGION.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.PAYMENTS_REGION.getName()));
				fields.put((FieldType.CARD_HOLDER_TYPE.getName()),
						(String) httpRequest.getSession().getAttribute(FieldType.CARD_HOLDER_TYPE.getName()));
				field.put(FieldType.TXNTYPE.getName(),
						(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
				// field.put(FieldType.FEDERAL_RESPONSE_MESSAGE.getName(),
				// ErrorType.INVALID_FIELD.getResponseMessage());
				Map<String, String> response = transactionControllerServiceProvider.transact(field,
						Constants.TXN_WS_FEDERAL_RETURN_URL.getValue());
				Fields processedResponse = new Fields(response);
				String pgFlag = (String) httpRequest.getSession()
						.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
				if (StringUtils.isNotBlank(pgFlag)) {
					processedResponse.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
				}
				responseCreator.create(processedResponse);
				transactionResponser.removeInvalidResponseFields(processedResponse);
				responseCreator.ResponsePost(processedResponse, httpResponse);
//				return Action.NONE;
			}

			fields.logAllFields("Updated 3DS Recieved Map :");
			fields.put(FieldType.TXNTYPE.getName(),
					(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
			fields.put(FieldType.CARD_NUMBER.getName(),
					(String) httpRequest.getSession().getAttribute(FieldType.CARD_NUMBER.getName()));
			fields.put(FieldType.CARD_EXP_DT.getName(),
					(String) httpRequest.getSession().getAttribute(FieldType.CARD_EXP_DT.getName()));
			fields.put(FieldType.CVV.getName(),
					(String) httpRequest.getSession().getAttribute(FieldType.CVV.getName()));
			fields.put((FieldType.OID.getName()),
					(String) httpRequest.getSession().getAttribute(FieldType.OID.getName()));
			fields.put(FieldType.ORIG_TXN_ID.getName(),
					(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_ID.getName()));
			fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
			fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.FEDERAL.getCode());

			Map<String, String> response = transactionControllerServiceProvider.transact(fields,
					Constants.TXN_WS_INTERNAL.getValue());
			responseMap = new Fields(response);
			String pgFlag = (String) httpRequest.getSession()
					.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
			if (StringUtils.isNotBlank(pgFlag)) {
				responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
			}

			Object previousFields = httpRequest.getSession().getAttribute(Constants.FIELDS.getValue());
			Fields sessionFields = null;
			if (null != previousFields) {
				sessionFields = (Fields) previousFields;
			} else {
				// TODO: Handle
			}

			// Sending Email for Transaction Status to merchant

			String countryCode = (String) httpRequest.getSession()
					.getAttribute(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName());

			// TODO emailBuilder.postMan(responseMap, countryCode, user);

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

			sessionFields.put(responseMap);
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
			responseMap.remove(FieldType.TXN_KEY.getName());
			responseMap.remove(FieldType.ACQUIRER_TYPE.getName());
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

	public void handleInvalidHash(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws SystemException {
		Fields field = (Fields) httpRequest.getSession().getAttribute(Constants.FIELDS.getValue());
		field.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
		field.put(FieldType.FEDERAL_RESPONSE_MESSAGE.getName(), ErrorType.INVALID_HASH.getResponseMessage());
		field.put(FieldType.FEDERAL_MPIERROR_CODE.getName(), ErrorType.INVALID_HASH.getResponseCode());
		field.put(FieldType.TXNTYPE.getName(),
				(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
		field.put(FieldType.OID.getName(), (String) httpRequest.getSession().getAttribute(FieldType.OID.getName()));
		Map<String, String> response = transactionControllerServiceProvider.transact(field,
				Constants.TXN_WS_FEDERAL_RETURN_URL.getValue());
		Fields processedResponse = new Fields(response);
		String pgFlag = (String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
		if (StringUtils.isNotBlank(pgFlag)) {
			processedResponse.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
		}
		responseCreator.create(processedResponse);
		transactionResponser.removeInvalidResponseFields(processedResponse);
		responseCreator.ResponsePost(processedResponse, httpResponse);
	}

	public String getHash(Fields fields, HttpServletRequest httpRequest) throws SystemException {
		String response = null;
		String cavv = fields.get(FieldType.FEDERAL_CAVV.getName());

		String hashKey = (String) httpRequest.getSession().getAttribute(FieldType.TXN_KEY.getName());
		StringBuilder request = new StringBuilder();
		request.append(fields.get(FieldType.FEDERAL_MPI_ID.getName()));
		request.append(Constants.DIRECPAY_SEPARATOR.getValue());
		request.append(fields.get(FieldType.FEDERAL_ECI.getName()));
		request.append(Constants.DIRECPAY_SEPARATOR.getValue());
		request.append(fields.get(FieldType.FEDERAL_STATUS.getName()));
		request.append(Constants.DIRECPAY_SEPARATOR.getValue());

		if (cavv != null && StringUtils.isNotBlank(cavv)) {
			request.append(fields.get(FieldType.FEDERAL_CAVV.getName()));
			request.append(Constants.DIRECPAY_SEPARATOR.getValue());
		}
		request.append(fields.get(FieldType.AMOUNT.getName()));
		request.append(Constants.DIRECPAY_SEPARATOR.getValue());
		request.append(fields.get(FieldType.CURRENCY_CODE.getName()));
		request.append(Constants.DIRECPAY_SEPARATOR.getValue());
		request.append(fields.get(FieldType.RESPONSE_CODE.getName()));
		request.append(Constants.DIRECPAY_SEPARATOR.getValue());
		request.append(fields.get(FieldType.FEDERAL_XID.getName()));
		request.append(Constants.DIRECPAY_SEPARATOR.getValue());
		request.append(fields.get(FieldType.FEDERAL_MD.getName()));
		request.append(Constants.DIRECPAY_SEPARATOR.getValue());
		request.append(hashKey);

		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance(PropertiesManager.propertiesMap.get("FederalHash_algorithm"));
		} catch (NoSuchAlgorithmException exception) {
			logger.error("Exception in FederalResponseAction " + exception.getMessage());
		}
		try {
			response = new String(encodeMessageHash(messageDigest.digest(request.toString().getBytes("UTF-8"))));
		} catch (UnsupportedEncodingException exception) {
			logger.error("Exception in FederalResponseAction " + exception.getMessage());
		}

		return response;
	}

	public static String encodeMessageHash(byte[] data) {
		String res = DatatypeConverter.printBase64Binary(data);

		return res;
	}

}
