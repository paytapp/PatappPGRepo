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

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.ResponseCreator;
import com.paymentgateway.pg.core.util.TransactionResponser;
import com.paymentgateway.pgui.action.service.ActionService;
import com.paymentgateway.pgui.action.service.PgActionServiceFactory;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class UPIQRRequestAction {

	@Autowired
	private ResponseCreator responseCreator;

	@Autowired
	private TransactionResponser transactionResponser;

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	private static Logger logger = LoggerFactory.getLogger(UPIQRRequestAction.class.getName());

	public Map<String, String> upiqrRequestHandler(HttpServletRequest request, HttpServletResponse res,
			Map<String, String> reqmap) throws IOException {
		Map<String, String> responseFields = new HashMap<String, String>();
		logger.info("Inside execute() of UPIQRRequestAction");
		try {
			Fields fields = null;
			if (reqmap.containsKey(FieldType.CHECKOUT_JS_FLAG.getName()) && reqmap.get(FieldType.CHECKOUT_JS_FLAG.getName()).equalsIgnoreCase("true")) {
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

			request.getSession().setAttribute((FieldType.INTERNAL_ORIG_TXN_TYPE.getName()),
					TransactionType.SALE.getName());

			fields.put(FieldType.PAYMENT_TYPE.getName(), PaymentType.UPI.getCode());
			fields.put(FieldType.MOP_TYPE.getName(), MopType.UPI_QR.getCode());
			fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.SALE.getName());
			fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
			fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
			fields.put(FieldType.INTERNAL_ORIG_TXN_ID.getName(),
					(String) request.getSession().getAttribute(FieldType.OID.getName()));
			fields.put(FieldType.CARD_HOLDER_TYPE.getName(), CardHolderType.CONSUMER.toString());
			fields.put(FieldType.PAYMENTS_REGION.getName(), AccountCurrencyRegion.DOMESTIC.toString());

			if (fields.get(FieldType.PG_TXN_MESSAGE.getName()) != null) {
				String PG_TXN_MESSAGE = fields.get(FieldType.PG_TXN_MESSAGE.getName());
				if (PG_TXN_MESSAGE.contains(":")) {
					PG_TXN_MESSAGE.replace(":", " ");
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), PG_TXN_MESSAGE);
				}
			}

			String surchargeFlag = (String) request.getSession().getAttribute("SURCHARGE_FLAG").toString();
			if (surchargeFlag.equals("Y")) {
				fields.put((FieldType.SURCHARGE_FLAG.getName()), surchargeFlag);
			}

			String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());
			String paymentType = fields.get(FieldType.PAYMENT_TYPE.getName());
			if (paymentType.equals(PaymentType.UPI.getCode())) {
				String upTotalAmount = request.getSession().getAttribute(FieldType.UP_TOTAL_AMOUNT.getName())
						.toString();
				upTotalAmount = Amount.formatAmount(upTotalAmount, currencyCode);
				fields.put(FieldType.TOTAL_AMOUNT.getName(), upTotalAmount);
				request.getSession().setAttribute(FieldType.TOTAL_AMOUNT.getName(), upTotalAmount);
			} else {
				throw new SystemException(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED, "Unsupported payment type");
			}

			if (StringUtils.isBlank(fields.get(FieldType.AMOUNT.getName()))) {
				throw new SystemException(ErrorType.BANK_SURCHARGE_REJECTED, "Bank Surcharge request rejected");
			}

			if (surchargeFlag.equals("Y")) {
				logger.info("IN UPI REq action orderId = " + fields.get(FieldType.ORDER_ID.getName())
						+ " total amount = " + fields.get(FieldType.TOTAL_AMOUNT.getName()) + "  amount  = "
						+ fields.get(FieldType.AMOUNT.getName()));
				if (Double.valueOf(fields.get(FieldType.TOTAL_AMOUNT.getName()))
						.compareTo(Double.valueOf(fields.get(FieldType.AMOUNT.getName()))) < 0) {
					throw new SystemException(ErrorType.BANK_SURCHARGE_REJECTED, "Bank Surcharge request rejected");
				}
			}

			logger.info("UPI Request action , order ID = " + fields.get(FieldType.ORDER_ID.getName())
					+ "UPI Request action , pay ID = " + fields.get(FieldType.PAY_ID.getName()));
			Map<String, String> response = transactionControllerServiceProvider.transact(fields,
					Constants.TXN_WS_INTERNAL.getValue());

			String pgFlag = (String) request.getSession().getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
			if (response.get(FieldType.RESPONSE_CODE.getName())
					.equalsIgnoreCase(ErrorType.INVALID_VPA.getResponseCode())) {
				response.remove(FieldType.ACQUIRER_TYPE.getName());
			}
			Fields responseMap = new Fields(response);

			logger.info("Response received from Transact in UPI Request Action "
					+ responseMap.get(FieldType.RESPONSE_CODE.getName()));
			if (responseMap.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase(ErrorType.UNKNOWN.getCode())) {

				logger.info("Response received === " + responseMap.get(FieldType.RESPONSE_CODE.getName())
						+ " Redirecting to merchant ");
				responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(),
						(String) request.getSession().getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName()));
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
				responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(),
						(String) request.getSession().getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName()));
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.INTERNAL_SYSTEM_ERROR.getResponseMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INTERNAL_SYSTEM_ERROR.getCode());
				responseMap.put(FieldType.TXNTYPE.getName(),
						(String) request.getSession().getAttribute(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
				transactionResponser.removeInvalidResponseFields(responseMap);
				transactionResponser.addResponseDateTime(responseMap);
				responseCreator.ResponsePost(responseMap, res);
//				return Action.NONE;
			}

			logger.info("Response fields from pgws:  " + responseMap.getFieldsAsString());
//			setUpiQrCode(responseMap.get(FieldType.UPI_QR_CODE.getName()));
			// sessionMap.put(Constants.FIELDS.getValue(), responseMap);
			// sessionMap.put(FieldType.INTERNAL_ORIG_TXN_ID.getName(),
			// responseMap.get(FieldType.TXN_ID.getName()));
			// sessionMap.put(FieldType.ACQUIRER_TYPE.getName(),
			// responseMap.get(FieldType.ACQUIRER_TYPE.getName()));
			responseMap.put(FieldType.RETURN_URL.getName(),
					(String) request.getSession().getAttribute(FieldType.RETURN_URL.getName()));
//			if (responseMap.get(FieldType.PG_TXN_MESSAGE.getName()) != null) {
//				setResponseMessage(responseMap.get(FieldType.PG_TXN_MESSAGE.getName()));
//			} else {
//				setResponseMessage(responseMap.get(FieldType.RESPONSE_MESSAGE.getName()));
//			}
//			setOid(responseMap.get(FieldType.TXN_ID.getName()));
//			setResponseCode(responseMap.get(FieldType.RESPONSE_CODE.getName()));
//			setTransactionStatus(responseMap.get(FieldType.STATUS.getName()));
//			setTxnType(responseMap.get(FieldType.PAYMENT_TYPE.getName()));
			responseMap.removeInternalFields();
			responseMap.removeSecureFields();
			responseMap.remove(FieldType.HASH.getName());
			responseMap.put(FieldType.HASH.getName(), Hasher.getHash(responseMap));
			responseFields.put("upiQrCode", responseMap.get(FieldType.UPI_QR_CODE.getName()));
			if (responseMap.get(FieldType.PG_TXN_MESSAGE.getName()) != null) {
				responseFields.put("responseMessage", responseMap.get(FieldType.PG_TXN_MESSAGE.getName()));
			} else {
				responseFields.put("responseMessage", responseMap.get(FieldType.RESPONSE_MESSAGE.getName()));
			}
			responseFields.put("oid", responseMap.get(FieldType.TXN_ID.getName()));
			responseFields.put("responseCode", responseMap.get(FieldType.RESPONSE_CODE.getName()));
			responseFields.put("transactionStatus", responseMap.get(FieldType.STATUS.getName()));
			responseFields.put("txnType", responseMap.get(FieldType.PAYMENT_TYPE.getName()));

			fields = null;
//			return SUCCESS;
		} catch (SystemException systemException) {
			logger.error("systemException", systemException);
		} catch (Exception exception) {
			logger.error("Error handling of transaction", exception);
//			return ERROR;
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

}
