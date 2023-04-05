package com.paymentgateway.pgui.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.CRC32;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.pg.core.acquirerDoubleVerification.CheckDBEntryForPgref;
import com.paymentgateway.pg.core.util.ResponseCreator;
import com.paymentgateway.pg.core.util.TransactionResponser;

/**
 * @author Sandeep
 *
 */
@Service
public class AirPayUpiResponseAction {

	private static Logger logger = LoggerFactory.getLogger(AirPayUpiResponseAction.class.getName());

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private UserDao userDao;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private CheckDBEntryForPgref checkDBEntryForPgref;

	@Autowired
	private TransactionResponser transactionResponser;

	@Autowired
	private ResponseCreator responseCreator;

	public Map<String, String> airPayResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {
		Fields responseMap = null;
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
			JsonObject resJson = new JsonObject();
			for (Entry<String, String> entry : requestMap.entrySet()) {
				resJson.addProperty(entry.getKey(), entry.getValue());
			}

			logger.info("Response received from Airpayfree: " + resJson.toString());
			// Log all entries from requestMap

			if (!requestMap.isEmpty()) {

				String transid = requestMap.get("TRANSACTIONID");
				String securehash = requestMap.get("ap_SecureHash");

				Fields fields = new Fields();

				fields = fieldsDao.getPreviousForOrderId(transid);
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
				logger.info("After getPreviousForOrderId for Airpay upi" + fields.getFieldsAsString());
				// matching secure hash

				String calculateSecurehash = secureHash(requestMap, fields);
				logger.info("calculated hash >>>> " + calculateSecurehash);
				logger.info("secure hash >>>> " + securehash);
				if (!calculateSecurehash.equals(securehash)) {
					StringBuilder Message = new StringBuilder("Response secure hash =");
					Message.append(securehash);
					Message.append(", Calculated secure hash =");
					Message.append(calculateSecurehash);
					logger.error(Message.toString());

					// return response
					Map<String, String> pgRefResponse = new HashMap<String, String>();
					pgRefResponse = checkDBEntryForPgref.searchPaymentStatus(transid);

					internalRequestFields = pgRefResponse.get(FieldType.INTERNAL_REQUEST_FIELDS.getName());
					paramaters = internalRequestFields.split("~");
					paramMap = new HashMap<String, String>();
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
					responseMap.put(FieldType.PG_TXN_MESSAGE.getName(),
							ErrorType.SIGNATURE_MISMATCH.getResponseMessage());
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

				// pg ref from db
				Map<String, String> pgRefResponse = new HashMap<String, String>();
				String sessionPgRef = (String) httpRequest.getSession().getAttribute(FieldType.PG_REF_NUM.getName());
				Boolean pgRefMatch = false;
				if (StringUtils.isNotBlank(sessionPgRef) && sessionPgRef.equalsIgnoreCase(transid)) {
					pgRefMatch = true;
				}
				// pgref match with db
				if (!pgRefMatch) {
					pgRefResponse = checkDBEntryForPgref.searchPaymentStatus(transid);
					if (StringUtils.isNotBlank(pgRefResponse.get(FieldType.STATUS.getName()))
							&& !pgRefResponse.get(FieldType.STATUS.getName())
									.equalsIgnoreCase(StatusType.SENT_TO_BANK.getName())
							&& !pgRefResponse.get(FieldType.STATUS.getName())
									.equalsIgnoreCase(StatusType.ENROLLED.getName())
							&& !pgRefResponse.get(FieldType.STATUS.getName())
									.equalsIgnoreCase(StatusType.PROCESSING.getName())
							&& !pgRefResponse.get(FieldType.STATUS.getName())
									.equalsIgnoreCase(StatusType.PENDING.getName())
							&& !pgRefResponse.get(FieldType.STATUS.getName())
									.equalsIgnoreCase(StatusType.FAILED.getName())
							&& !pgRefResponse.get(FieldType.STATUS.getName())
									.equalsIgnoreCase(StatusType.REJECTED.getName())) {
						logger.info(
								"Already db entry found other then pending, Enrolled, sent to bank, processing, failed, rejected");
						internalRequestFields = pgRefResponse.get(FieldType.INTERNAL_REQUEST_FIELDS.getName());
						paramaters = internalRequestFields.split("~");
						paramMap = new HashMap<String, String>();
						for (String param : paramaters) {
							String[] parameterPair = param.split("=");
							if (parameterPair.length > 1) {
								paramMap.put(parameterPair[0].trim(), parameterPair[1].trim());
							}
						}
						responseMap = new Fields(pgRefResponse);
						responseMap.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
						responseMap.put(FieldType.AMOUNT.getName(),
								Amount.removeDecimalAmount(paramMap.get(FieldType.AMOUNT.getName()),
										paramMap.get(FieldType.CURRENCY_CODE.getName())));
						responseMap.put(FieldType.TOTAL_AMOUNT.getName(),
								Amount.formatAmount(pgRefResponse.get(FieldType.TOTAL_AMOUNT.getName()),
										pgRefResponse.get(FieldType.CURRENCY_CODE.getName())));
						responseMap.put(FieldType.RESPONSE_MESSAGE.getName(),
								ErrorType.DENIED_BY_FRAUD.getResponseMessage());
						responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DENIED_BY_FRAUD.getCode());
						responseMap.put(FieldType.PG_TXN_MESSAGE.getName(),
								ErrorType.FRAUD_RESPONSE.getResponseMessage());
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

				fields.put(FieldType.AIRPAY_RESPONSE_FIELD.getName(), resJson.toString());

				fields.logAllFields(
						"Updated airpay callback Recieved Map TxnType = " + fields.get(FieldType.TXNTYPE.getName())
								+ " " + "Txn id = " + fields.get(FieldType.TXN_ID.getName()));
				fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.AIRPAY.getCode());

				if (fields.get(FieldType.PAYMENT_TYPE.getName()) != null
						&& (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.UPI.getCode())
								|| fields.get(FieldType.PAYMENT_TYPE.getName())
										.equalsIgnoreCase(PaymentType.WALLET.getCode()))
						|| fields.get(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.NET_BANKING.getCode())) {
					fields.put(FieldType.PAYMENTS_REGION.getName(), AccountCurrencyRegion.DOMESTIC.toString());
					fields.put(FieldType.CARD_HOLDER_TYPE.getName(), CardHolderType.CONSUMER.toString());
				}

				Map<String, String> response = transactionControllerServiceProvider.transact(fields,
						Constants.TXN_WS_AIRPAY_PROCESSOR.getValue());
				responseMap = new Fields(response);

				String isMerchantHosted = (String) httpRequest.getSession()
						.getAttribute(FieldType.IS_MERCHANT_HOSTED.getName());
				if (StringUtils.isNotBlank(isMerchantHosted)) {
					responseMap.put(FieldType.IS_MERCHANT_HOSTED.getName(), isMerchantHosted);
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
				if (httpRequest.getSession() != null) {
					httpRequest.getSession().setAttribute(Constants.TRANSACTION_COMPLETE_FLAG.getValue(),
							Constants.Y_FLAG.getValue());
					httpRequest.getSession().invalidate();
				}
				responseMap.remove(FieldType.HASH.getName());
				responseMap.remove(FieldType.TXN_KEY.getName());
				responseMap.remove(FieldType.ACQUIRER_TYPE.getName());
				responseMap.remove(FieldType.PASSWORD.getName());
				responseMap.remove(FieldType.IS_INTERNAL_REQUEST.getName());
				responseCreator.create(responseMap);
				responseCreator.ResponsePost(responseMap, httpResponse);
				return responseMap.getFields();
			} else {
				logger.info("Airpay Response is null ... ");
			}

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
		return responseMap.getFields();
	}

	public Map<String, String> getTxnKey(Fields fields) throws SystemException {

		User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		Account account = null;
		Set<Account> accounts = user.getAccounts();

		if (accounts == null || accounts.size() == 0) {
			logger.info("No account found for Pay ID = " + fields.get(FieldType.PAY_ID.getName()) + " and ORDER ID = "
					+ fields.get(FieldType.ORDER_ID.getName()));
		} else {
			for (Account accountThis : accounts) {
				if (accountThis.getAcquirerName()
						.equalsIgnoreCase(AcquirerType.getInstancefromCode(AcquirerType.AIRPAY.getCode()).getName())) {
					account = accountThis;
					break;
				}
			}
		}

		AccountCurrency accountCurrency = account.getAccountCurrency(fields.get(FieldType.CURRENCY_CODE.getName()));
		Map<String, String> acqParamMap = new HashMap<String, String>();

		acqParamMap.put(FieldType.MERCHANT_ID.getName(), accountCurrency.getMerchantId());
		acqParamMap.put(FieldType.ADF1.getName(), accountCurrency.getAdf1());

		return acqParamMap;

	}

	private String secureHash(Map<String, String> requestMap, Fields fields) {
		String sCRC = "";
		try {
			Map<String, String> acqParamMap = new HashMap<String, String>();
			acqParamMap = getTxnKey(fields);

			String mID = acqParamMap.get(FieldType.MERCHANT_ID.getName());
			String userName = acqParamMap.get(FieldType.ADF1.getName());

			StringBuilder sParam = new StringBuilder();
			sParam.append(requestMap.get("TRANSACTIONID") + ":" + requestMap.get("APTRANSACTIONID") + ":"
					+ requestMap.get("AMOUNT") + ":" + requestMap.get("TRANSACTIONSTATUS") + ":"
					+ requestMap.get("MESSAGE") + ":" + mID + ":" + userName);
			if (requestMap.containsKey("CUSTOMERVPA")) {
				sParam.append(":" + requestMap.get("CUSTOMERVPA"));
			}

			CRC32 crc = new CRC32();
			crc.update(sParam.toString().getBytes());
			sCRC = "" + crc.getValue();
		} catch (Exception e) {
			logger.error("Exception", e);
		}
		return sCRC;
	}

}
