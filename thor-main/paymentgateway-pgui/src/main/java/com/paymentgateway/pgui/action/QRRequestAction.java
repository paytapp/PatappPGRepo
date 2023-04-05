package com.paymentgateway.pgui.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.kms.EncryptDecryptService;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.ResponseCreator;
import com.paymentgateway.pg.core.util.TransactionResponser;

/**
 * @author Sandeep Sharma
 *
 */
@Service
public class QRRequestAction {

	@Autowired
	private ResponseCreator responseCreator;

	@Autowired
	private UserDao userDao;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private EncryptDecryptService encryptDecryptService;

	@Autowired
	private TransactionResponser transactionResponser;

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private UserSettingDao userSettingDao;

	private static Logger logger = LoggerFactory.getLogger(QRRequestAction.class.getName());

	public Map<String, String> qrRequestHandler(HttpServletRequest request, HttpServletResponse res,
			Map<String, String> reqmap) throws IOException {
		Map<String, String> responseFields = new HashMap<String, String>();
		logger.info("Inside execute() of QRRequestAction");
		try {
			String mqrData = (String) request.getSession().getAttribute(FieldType.MQR_QR_CODE.getName());
			if (StringUtils.isNotBlank(mqrData)) {
				String responseMsg = (String) request.getSession().getAttribute(FieldType.RESPONSE_MESSAGE.getName());
				String pgRefNum = (String) request.getSession().getAttribute(FieldType.PG_REF_NUM.getName());
				String responseCode = (String) request.getSession().getAttribute(FieldType.RESPONSE_CODE.getName());
				String status = (String) request.getSession().getAttribute(FieldType.STATUS.getName());
				String paymentType = (String) request.getSession().getAttribute(FieldType.PAYMENT_TYPE.getName());
				String pgTxnMsg = (String) request.getSession().getAttribute(FieldType.PG_TXN_MESSAGE.getName());
				String vpa = (String) request.getSession().getAttribute(FieldType.VPA.getName());
				String totalAmount = (String) request.getSession().getAttribute(FieldType.TOTAL_AMOUNT.getName());

				responseFields.put("mqrQrCode", mqrData);
				if (pgTxnMsg != null) {
					responseFields.put("responseMessage", pgTxnMsg);
				} else {
					responseFields.put("responseMessage", responseMsg);
				}
				responseFields.put("oid", pgRefNum);
				responseFields.put("responseCode", responseCode);
				responseFields.put("transactionStatus", status);
				responseFields.put("txnType", paymentType);
				responseFields.put("mqrVpa", vpa);
				responseFields.put("totalAmount", totalAmount);

			} else {
				Fields fields = null;
				if (reqmap.containsKey(FieldType.CHECKOUT_JS_FLAG.getName())
						&& reqmap.get(FieldType.CHECKOUT_JS_FLAG.getName()).equalsIgnoreCase("true")) {
					request.getSession().invalidate();
				} else {
					fields = (Fields) request.getSession().getAttribute(Constants.FIELDS.getValue());
				}
				if (null != fields) {
				} else {
					if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("ADMIN_PAYID"))
							&& StringUtils.isNotBlank(reqmap.get("encSessionData"))) {
						Map<String, String> fieldsMap = new HashMap<String, String>();
						Map<String, String> responseMap = transactionControllerServiceProvider.hostedDecrypt(
								PropertiesManager.propertiesMap.get("ADMIN_PAYID"), reqmap.get("encSessionData"));
						if (!responseMap.isEmpty()) {
							String decryptedString = responseMap.get(FieldType.ENCDATA.getName());
							String[] fieldArray = decryptedString.split("~");

							for (String key : fieldArray) {
								String[] namValuePair = key.split("=", 2);
								request.getSession().setAttribute(namValuePair[0], namValuePair[1]);
							}
						}
						String sessionFields = (String) request.getSession().getAttribute(Constants.FIELDS.getValue());
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
						String path = request.getContextPath();
						logger.info(path);
						if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
							String resultPath = request.getScheme() + "://" + request.getHeader("Host")
									+ "/pgui/jsp/error";
							res.sendRedirect(resultPath);
						}
						res.sendRedirect("error");
					}
				}

				request.getSession().setAttribute((FieldType.INTERNAL_ORIG_TXN_TYPE.getName()),
						TransactionType.SALE.getName());

				fields.put(FieldType.PAYMENT_TYPE.getName(), PaymentType.MQR.getCode());
				fields.put(FieldType.MOP_TYPE.getName(), MopType.QR.getCode());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
				fields.put(FieldType.INTERNAL_ORIG_TXN_ID.getName(),
						(String) request.getSession().getAttribute(FieldType.OID.getName()));
				fields.put(FieldType.CARD_HOLDER_TYPE.getName(), CardHolderType.CONSUMER.toString());
				fields.put(FieldType.PAYMENTS_REGION.getName(), AccountCurrencyRegion.DOMESTIC.toString());

				String surchargeFlag = (String) request.getSession().getAttribute("SURCHARGE_FLAG").toString();
				if (surchargeFlag.equals("Y")) {
					fields.put((FieldType.SURCHARGE_FLAG.getName()), surchargeFlag);
				}

				String amount = fields.get(FieldType.AMOUNT.getName());
				if (StringUtils.isBlank(amount)) {
					throw new SystemException(ErrorType.BANK_SURCHARGE_REJECTED, "Bank Surcharge request rejected");
				}
				UserSettingData userSettingData = userSettingDao
						.fetchDataUsingPayId(fields.get(FieldType.PAY_ID.getName()));
				if (userSettingData.isRandomAmount()) {
					Random rand = new Random();
					int random = rand.nextInt(90) + 10;
					amount = String.valueOf(Integer.parseInt(amount) + random);
					fields.put(FieldType.TOTAL_AMOUNT.getName(), amount);
				} else {
					request.getSession().setAttribute(FieldType.TOTAL_AMOUNT.getName(), amount);
				}

				logger.info("MQR Request action , order ID = " + fields.get(FieldType.ORDER_ID.getName())
						+ "MQR Request action , pay ID = " + fields.get(FieldType.PAY_ID.getName()));
				Map<String, String> response = transactionControllerServiceProvider.transact(fields,
						Constants.TXN_WS_INTERNAL.getValue());

				String pgFlag = (String) request.getSession()
						.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
				if (response.get(FieldType.RESPONSE_CODE.getName())
						.equalsIgnoreCase(ErrorType.ACQUIRER_ERROR.getResponseCode())) {
					response.remove(FieldType.ACQUIRER_TYPE.getName());
				}
				Fields responseMap = new Fields(response);

				logger.info("Response received from Transact in UPI Request Action "
						+ responseMap.get(FieldType.RESPONSE_CODE.getName()));
				if (responseMap.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase(ErrorType.UNKNOWN.getCode())) {

					logger.info("Response received === " + responseMap.get(FieldType.RESPONSE_CODE.getName())
							+ " Redirecting to merchant ");
					responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), (String) request.getSession()
							.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName()));
					responseCreator.ResponsePost(responseMap, res);
//				return Action.NONE;
				}

				if (responseMap.get(FieldType.RESPONSE_CODE.getName())
						.equalsIgnoreCase(ErrorType.ACQUIRER_NOT_FOUND.getCode())) {

					logger.info("Response Code === " + responseMap.get(FieldType.RESPONSE_CODE.getName())
							+ " Redirecting to merchant ");
					if (StringUtils.isNotBlank(pgFlag)) {
						responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
					}
					responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), (String) request.getSession()
							.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName()));
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(),
							ErrorType.INTERNAL_SYSTEM_ERROR.getResponseMessage());
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INTERNAL_SYSTEM_ERROR.getCode());
					responseMap.put(FieldType.TXNTYPE.getName(),
							(String) request.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
					transactionResponser.removeInvalidResponseFields(responseMap);
					transactionResponser.addResponseDateTime(responseMap);
					responseCreator.ResponsePost(responseMap, res);
				}

				logger.info("Response fields from pgws:  " + responseMap.getFieldsAsString());
				responseMap.put(FieldType.RETURN_URL.getName(),
						(String) request.getSession().getAttribute(FieldType.RETURN_URL.getName()));
				responseMap.removeInternalFields();
				responseMap.removeSecureFields();
				responseMap.remove(FieldType.HASH.getName());
				responseMap.put(FieldType.HASH.getName(), Hasher.getHash(responseMap));
				responseFields.put("mqrQrCode", responseMap.get(FieldType.MQR_QR_CODE.getName()));
				if (responseMap.get(FieldType.PG_TXN_MESSAGE.getName()) != null) {
					responseFields.put("responseMessage", responseMap.get(FieldType.PG_TXN_MESSAGE.getName()));
				} else {
					responseFields.put("responseMessage", responseMap.get(FieldType.RESPONSE_MESSAGE.getName()));
				}
				responseFields.put("oid", responseMap.get(FieldType.PG_REF_NUM.getName()));
				responseFields.put("responseCode", responseMap.get(FieldType.RESPONSE_CODE.getName()));
				responseFields.put("transactionStatus", responseMap.get(FieldType.STATUS.getName()));
				responseFields.put("txnType", responseMap.get(FieldType.PAYMENT_TYPE.getName()));
				responseFields.put("mqrVpa", responseMap.get(FieldType.VPA.getName()));
				responseFields.put("totalAmount", responseMap.get(FieldType.TOTAL_AMOUNT.getName()));

				request.getSession().setAttribute((FieldType.MQR_QR_CODE.getName()),
						responseMap.get(FieldType.MQR_QR_CODE.getName()));
				request.getSession().setAttribute((FieldType.VPA.getName()), responseMap.get(FieldType.VPA.getName()));
				request.getSession().setAttribute((FieldType.PG_TXN_MESSAGE.getName()),
						responseMap.get(FieldType.PG_TXN_MESSAGE.getName()));
				request.getSession().setAttribute((FieldType.RESPONSE_MESSAGE.getName()),
						responseMap.get(FieldType.RESPONSE_MESSAGE.getName()));
				request.getSession().setAttribute((FieldType.PG_REF_NUM.getName()),
						responseMap.get(FieldType.PG_REF_NUM.getName()));
				request.getSession().setAttribute((FieldType.RESPONSE_CODE.getName()),
						responseMap.get(FieldType.RESPONSE_CODE.getName()));
				request.getSession().setAttribute((FieldType.STATUS.getName()),
						responseMap.get(FieldType.STATUS.getName()));
				request.getSession().setAttribute((FieldType.PAYMENT_TYPE.getName()),
						responseMap.get(FieldType.PAYMENT_TYPE.getName()));
				request.getSession().setAttribute((FieldType.TOTAL_AMOUNT.getName()),
						responseMap.get(FieldType.TOTAL_AMOUNT.getName()));

				fields = null;
			}
		} catch (SystemException systemException) {
			logger.error("systemException", systemException);
		} catch (Exception exception) {
			logger.error("Error handling of transaction", exception);
			String path = request.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = request.getScheme() + "://" + request.getHeader("Host") + "/pgui/jsp/error";
				res.sendRedirect(resultPath);
			}
			res.sendRedirect("error");
		}
		return responseFields;
	}

	public Map<String, String> submitUtrRequestHandler(HttpServletRequest request, HttpServletResponse res,
			Map<String, String> reqmap) throws IOException, SystemException {
		Map<String, String> responseFields = new HashMap<String, String>();
		Fields fields = null;
		if (reqmap.containsKey(FieldType.CHECKOUT_JS_FLAG.getName())
				&& reqmap.get(FieldType.CHECKOUT_JS_FLAG.getName()).equalsIgnoreCase("true")) {
			request.getSession().invalidate();
		} else {
			fields = (Fields) request.getSession().getAttribute(Constants.FIELDS.getValue());
		}
		if (null != fields) {
		} else {
			if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("ADMIN_PAYID"))
					&& StringUtils.isNotBlank(reqmap.get("encSessionData"))) {
				Map<String, String> fieldsMap = new HashMap<String, String>();
				Map<String, String> responseMap = transactionControllerServiceProvider.hostedDecrypt(
						PropertiesManager.propertiesMap.get("ADMIN_PAYID"), reqmap.get("encSessionData"));
				if (!responseMap.isEmpty()) {
					String decryptedString = responseMap.get(FieldType.ENCDATA.getName());
					String[] fieldArray = decryptedString.split("~");

					for (String key : fieldArray) {
						String[] namValuePair = key.split("=", 2);
						request.getSession().setAttribute(namValuePair[0], namValuePair[1]);
					}
				}
				String sessionFields = (String) request.getSession().getAttribute(Constants.FIELDS.getValue());
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
				String path = request.getContextPath();
				logger.info(path);
				if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
					String resultPath = request.getScheme() + "://" + request.getHeader("Host") + "/pgui/jsp/error";
					res.sendRedirect(resultPath);
				}
				res.sendRedirect("error");
			}
		}
		String pgRefNum = (String) request.getSession().getAttribute(FieldType.PG_REF_NUM.getName());
		String vpa = (String) request.getSession().getAttribute(FieldType.VPA.getName());

		fields.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
		fields.put(FieldType.VPA.getName(), vpa);
		fields.put(FieldType.UTR.getName(), reqmap.get("utrNumber"));

		fieldsDao.updateUTRInSentToBankTransaction(fields);
		// getting acquirermapping
		getAcqData(fields.get(FieldType.PAY_ID.getName()), fields);
		// call submit api and update acqid in sent to bank entry
		String requestData = createSubmitUtrRequest(fields);
		responseFields = transactionControllerServiceProvider.submitP2PTSPUTR(requestData, fields);
		logger.info("P2PTSP Submit Utr Response :- " + responseFields.toString());

		if (!responseFields.isEmpty()) {
			if (StringUtils.isNotBlank(responseFields.get("response_code").toString())
					&& responseFields.get("response_code").toString().equalsIgnoreCase("000")
					&& StringUtils.isNotBlank(responseFields.get("txn_id").toString())) {
//update txn_id as acqid in sent to bank
				fields.put(FieldType.ACQ_ID.getName(), responseFields.get("txn_id").toString());
				fieldsDao.updateSentToBankTransaction(fields);

			}
		}
		responseFields.clear();
		responseFields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName()));
		responseFields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());

		return responseFields;

	}

	private String createSubmitUtrRequest(Fields fields) throws IOException {
		JSONObject requestJson = new JSONObject();

		requestJson.put("SecurityID", fields.get(FieldType.PASSWORD.getName()));
		requestJson.put("trackId", fields.get(FieldType.PG_REF_NUM.getName()));
		requestJson.put("currency", "INR");
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_NAME.getName()))) {
			requestJson.put("name", fields.get(FieldType.CUST_NAME.getName()));
		}
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_PHONE.getName()))) {
			requestJson.put("mobile", fields.get(FieldType.CUST_PHONE.getName()));
		}
		requestJson.put("upi_vpa", fields.get(FieldType.VPA.getName()));
		requestJson.put("utr", fields.get(FieldType.UTR.getName()));
		requestJson.put("amount", fields.get(FieldType.TOTAL_AMOUNT.getName()));

		logger.info("P2PTSP P2PTSP Submit Utr Request :- " + requestJson.toString());
		return requestJson.toString();
	}

	private void getAcqData(String payId, Fields fields) throws SystemException {

		logger.info("Pay Id for ");
		User user = userDao.findPayId(payId);
		Account account = null;
		Set<Account> accounts = user.getAccounts();

		if (accounts == null || accounts.size() == 0) {
			logger.info("No account found for Pay ID = " + payId);
		} else {
			for (Account accountThis : accounts) {
				if (accountThis.getAcquirerName()
						.equalsIgnoreCase(AcquirerType.getInstancefromCode(AcquirerType.P2PTSP.getCode()).getName())) {
					account = accountThis;
					break;
				}
			}
		}

		AccountCurrency accountCurrency = account.getAccountCurrency("356");
		String mId = accountCurrency.getMerchantId();
		String txnKey = accountCurrency.getTxnKey();
		String password = encryptDecryptService.decrypt(payId, accountCurrency.getPassword());
		fields.put(FieldType.MERCHANT_ID.getName(), mId);
		fields.put(FieldType.TXN_KEY.getName(), txnKey);
		fields.put(FieldType.PASSWORD.getName(), password);
	}

}
