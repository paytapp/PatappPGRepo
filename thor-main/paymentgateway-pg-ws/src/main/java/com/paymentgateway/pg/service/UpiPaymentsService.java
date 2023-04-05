package com.paymentgateway.pg.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.dao.RouterConfigurationDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.RouterConfiguration;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.onUsOffUs;
import com.paymentgateway.idfcUpi.IdfcUpiIntegrator;
import com.paymentgateway.idfcUpi.IdfcUpiTransformer;
import com.paymentgateway.idfcUpi.Transaction;
import com.paymentgateway.idfcUpi.TransactionCommunicator;
import com.paymentgateway.idfcUpi.TransactionConverter;
import com.paymentgateway.pg.core.pageintegrator.GeneralValidator;
import com.paymentgateway.pg.core.util.ProcessManager;
import com.paymentgateway.pg.core.util.Processor;
import com.paymentgateway.pg.core.util.TransactionResponser;
import com.paymentgateway.pg.security.SecurityProcessor;

@Service
public class UpiPaymentsService {
	Logger logger = LoggerFactory.getLogger(UpiPaymentsService.class.getName());

	private static BigDecimal minAmountSlab2 = new BigDecimal(
			PropertiesManager.propertiesMap.get("LimitSlab2MinAmount"));
	private static BigDecimal minAmountSlab3 = new BigDecimal(
			PropertiesManager.propertiesMap.get("LimitSlab3MinAmount"));

	@Autowired
	private TransactionResponser transactionResponser;

	@Autowired
	private GeneralValidator generalValidator;

	@Autowired
	private VpaValidationIDFC vpaValidationIDFC;

	@Autowired
	private VpaValidationCASHFREE vpaValidationCASHFREE;

	@Autowired
	private SecurityProcessor securityProcessor;

	@Autowired
	private UserDao userDao;

	@Autowired
	private Fields field;

	@Autowired
	private RouterConfigurationDao routerConfigurationDao;

	@Autowired
	@Qualifier("idfcUpiTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("idfcUpiTransactionCommunicator")
	private TransactionCommunicator communicator;

	@Autowired
	@Qualifier("idfcUpiIntegrator")
	private IdfcUpiIntegrator idfcUpiIntegrator;

	@Autowired
	@Qualifier("idfcUpiTransformer")
	private IdfcUpiTransformer upiTransformer;

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	@Qualifier("cashfreeProcessor")
	private Processor cashfreeProcessor;

	
	public Map<String, String> validateVpa(Fields fields) throws SystemException {
		Map<String, String> response = new HashMap<String, String>();
//		validate hash
		boolean hashResult = validateHash(fields);
		if (!hashResult) {
			fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
			transactionResponser.addHash(fields);
			return fields.getFields();
		}

//		validate fields
		validateVpaFields(fields);
		if (!fields.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase(ErrorType.SUCCESS.getResponseCode())
				&& !fields.get(FieldType.RESPONSE_MESSAGE.getName())
						.equalsIgnoreCase(ErrorType.SUCCESS.getResponseMessage())) {
			transactionResponser.addHash(fields);
			return fields.getFields();
		}

//		validate VPA using switch for multiple acquirer
		JSONObject jsonResponse = new JSONObject();
		Fields newFields = null;

		// added amount and currency manually to find the acquirer for slab 1
		fields.put(FieldType.AMOUNT.getName(), "10000");
		fields.put(FieldType.CURRENCY_CODE.getName(), "356");
		String acquirerName = getAcquirerVpaValidation(fields);
		String acqList = "IDFCUPI Bank, LYRA, PAYTM, CASHFREE,PAYU";
		if (StringUtils.isBlank(acquirerName) || !acqList.contains(acquirerName)) {
			fields.remove(FieldType.ACQUIRER_MODE.getName());
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_NOT_FOUND.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_NOT_FOUND.getResponseCode());
			transactionResponser.addHash(fields);
			return fields.getFields();
		}
		/*
		 * User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName())); if
		 * (user.isSuperMerchant()) { fields.remove(FieldType.ACQUIRER_MODE.getName());
		 * fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
		 * fields.put(FieldType.RESPONSE_MESSAGE.getName(),
		 * ErrorType.USER_NOT_ALLOWED.getResponseMessage());
		 * fields.put(FieldType.RESPONSE_CODE.getName(),
		 * ErrorType.USER_NOT_ALLOWED.getResponseCode());
		 * transactionResponser.addHash(fields); return fields.getFields(); }
		 */
		AcquirerType acquirer = AcquirerType.getInstancefromName(acquirerName);
		fields.put(FieldType.ACQUIRER_TYPE.getName(), acquirer.toString());
		switch (acquirer) {

		case IDFCUPI:

//			go for idfcUPI vpa validation
			jsonResponse = vpaValidationIDFC.validationResponse(fields);
			if (StringUtils.isNotBlank(jsonResponse.get("ResCode").toString())
					&& jsonResponse.get("ResCode").toString().equalsIgnoreCase("000")) {
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
				response.put(FieldType.STATUS.getName(),
						WordUtils.capitalizeFully(ErrorType.SUCCESS.getResponseMessage()));
				response.put(FieldType.PAYER_ADDRESS.getName(), fields.get(FieldType.PAYER_ADDRESS.getName()));
				response.put(FieldType.PAYER_NAME.getName(), jsonResponse.get("VerifiedName").toString());
				response.put(FieldType.PG_TXN_MESSAGE.getName(), "Virtual Address exist for Transaction");
				response.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				newFields = new Fields(response);
				transactionResponser.addHash(newFields);

			} else {
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getResponseCode());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), StatusType.FAILED.getName());
				response.put(FieldType.STATUS.getName(), WordUtils.capitalizeFully(StatusType.FAILED.getName()));
				response.put(FieldType.PAYER_ADDRESS.getName(), fields.get(FieldType.PAYER_ADDRESS.getName()));
				response.put(FieldType.PG_TXN_MESSAGE.getName(), "Virtual Address not exist for Transaction");
				response.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				newFields = new Fields(response);
				transactionResponser.addHash(newFields);
			}

			break;

		case CASHFREE:
//			go for CASHFREE vpa validation
			jsonResponse = vpaValidationCASHFREE.validationResponse(fields);
			if (StringUtils.isNotBlank(jsonResponse.get("valid").toString())
					&& jsonResponse.get("valid").toString().equalsIgnoreCase("true")) {
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
				response.put(FieldType.STATUS.getName(),
						WordUtils.capitalizeFully(ErrorType.SUCCESS.getResponseMessage()));
				response.put(FieldType.PAYER_ADDRESS.getName(), fields.get(FieldType.PAYER_ADDRESS.getName()));
				response.put(FieldType.PAYER_NAME.getName(), jsonResponse.get("name").toString());
				response.put(FieldType.PG_TXN_MESSAGE.getName(), "Virtual Address exist for Transaction");
				response.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				newFields = new Fields(response);
				transactionResponser.addHash(newFields);

			} else {
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getResponseCode());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), StatusType.FAILED.getName());
				response.put(FieldType.STATUS.getName(), WordUtils.capitalizeFully(StatusType.FAILED.getName()));
				response.put(FieldType.PAYER_ADDRESS.getName(), fields.get(FieldType.PAYER_ADDRESS.getName()));
				response.put(FieldType.PG_TXN_MESSAGE.getName(), "Virtual Address not exist for Transaction");
				response.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				newFields = new Fields(response);
				transactionResponser.addHash(newFields);
			}

			break;

		case PAYU:

			jsonResponse = validateVpaPayu(fields);
			if (StringUtils.isNotBlank(jsonResponse.get("status").toString())
					&& jsonResponse.get("status").toString().equalsIgnoreCase("SUCCESS")
					&& StringUtils.isNotBlank(jsonResponse.get("isVPAValid").toString())
					&& jsonResponse.get("isVPAValid").toString().equalsIgnoreCase("1")) {
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
				response.put(FieldType.STATUS.getName(),
						WordUtils.capitalizeFully(ErrorType.SUCCESS.getResponseMessage()));
				response.put(FieldType.PAYER_ADDRESS.getName(), fields.get(FieldType.PAYER_ADDRESS.getName()));
				response.put(FieldType.PAYER_NAME.getName(), "NA");
				response.put(FieldType.PG_TXN_MESSAGE.getName(), "Virtual Address exist for Transaction");
				response.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				newFields = new Fields(response);
				transactionResponser.addHash(newFields);

			} else {
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getResponseCode());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), StatusType.FAILED.getName());
				response.put(FieldType.STATUS.getName(), WordUtils.capitalizeFully(StatusType.FAILED.getName()));
				response.put(FieldType.PAYER_ADDRESS.getName(), fields.get(FieldType.PAYER_ADDRESS.getName()));
				response.put(FieldType.PG_TXN_MESSAGE.getName(), "Virtual Address not exist for Transaction");
				response.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				newFields = new Fields(response);
				transactionResponser.addHash(newFields);
			}

			break;
		default:
			break;

		}
		logger.info("Final response for For validateVpa() " + fields.getFields());
		return newFields.getFields();
	}

	public String payuUpiHash(Fields fields) {
		String hash = "";
		String key = fields.get(FieldType.ADF1.getName());
		String command = fields.get(FieldType.ADF2.getName());
		String var1 = fields.get(FieldType.PAYER_ADDRESS.getName());
		String salt = fields.get(FieldType.ADF3.getName());
		String str = key + "|" + command + "|" + var1 + "|" + salt;
		hash = digestHash(str);
		return hash;
	}

	private String digestHash(String str) {
		StringBuilder hash = new StringBuilder();
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
			messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
			byte[] mdbytes = messageDigest.digest();
			for (byte hashByte : mdbytes) {
				hash.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
			}
			return hash.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public JSONObject validateVpaPayu(Fields fields) {
		String vpa = fields.get(FieldType.PAYER_ADDRESS.getName());

		try {

			Map<String, String> requestMap = new HashMap<String, String>();
			requestMap.put("var1", vpa);
			// requestMap.put("key", "ZyeUwJ");
			requestMap.put("key", "H16ki6");
			requestMap.put("command", "validateVPA");

			// fields.put(FieldType.ADF1.getName(),"ZyeUwJ");
			fields.put(FieldType.ADF1.getName(), "H16ki6");
			fields.put(FieldType.ADF2.getName(), "validateVPA");
			// fields.put(FieldType.ADF3.getName(),"vtJQ0IFS");
			fields.put(FieldType.ADF3.getName(), "R38seLkr");

			String hash = payuUpiHash(fields);
			requestMap.put("hash", hash);

			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String, String> param : requestMap.entrySet()) {
				if (postData.length() != 0)
					postData.append('&');
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}

			String response = getVpaResp(postData.toString());

			JSONObject resJson = new JSONObject(response);
			return resJson;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String getVpaResp(String request) {

		String url = "https://info.payu.in/merchant/postservice.php?form=2";
		HttpURLConnection con = null;

		System.out.println("Request = " + request);
		try {
			URL requestUrl = new URL(url);
			con = (HttpURLConnection) requestUrl.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			con.setDoOutput(true);
			con.setUseCaches(false);
			OutputStream os = con.getOutputStream();
			os.write(request.getBytes());
			os.flush();
			os.close();

			int responseCode = con.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				return response.toString();

			} else {
				return String.valueOf(con.getResponseCode());

			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			con.disconnect();
		}

	}

	public Map<String, String> collectAmount(Fields fields) throws SystemException {
		Map<String, String> response = new HashMap<String, String>();
		boolean hashResult;
		try {
			hashResult = validateHash(fields);
		} catch (SystemException systemException) {
			logger.info("Invalid PAY_ID");
			response.put(FieldType.RESPONSE_CODE.getName(), systemException.getErrorType().getCode());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getMessage());
			return response;
		}
//		validate hash
		if (!hashResult) {
			fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
			transactionResponser.addHash(fields);
			return fields.getFields();
		}
//		validate fields
		validateCollectAmountFields(fields);
		if (!fields.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase(ErrorType.SUCCESS.getResponseCode())
				&& !fields.get(FieldType.RESPONSE_MESSAGE.getName())
						.equalsIgnoreCase(ErrorType.SUCCESS.getResponseMessage())) {
			transactionResponser.addHash(fields);
			return fields.getFields();
		}

		try {
			field.validateSaleDupicateOrderId(fields);
		} catch (SystemException systemException) {
			logger.info("Duplicate OrderId");
			response.put(FieldType.RESPONSE_CODE.getName(), systemException.getErrorType().getCode());
			response.put(FieldType.RESPONSE_MESSAGE.getName(),
					ErrorType.getInstanceFromCode(systemException.getErrorType().getCode()).getResponseMessage());
			return response;
		}

//		sale request after validating vpa using switch for multiple acquirer
		String acquirerName = getAcquirer(fields);
		String acqList = "IDFCUPI Bank, LYRA, CASHFREE";
		if (StringUtils.isBlank(acquirerName) || !acqList.contains(acquirerName)) {
			fields.remove(FieldType.ACQUIRER_MODE.getName());
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_NOT_FOUND.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_NOT_FOUND.getResponseCode());
			transactionResponser.addHash(fields);
			return fields.getFields();
		}
		User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		if (user.isSuperMerchant()) {
			fields.remove(FieldType.ACQUIRER_MODE.getName());
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.USER_NOT_ALLOWED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.USER_NOT_ALLOWED.getResponseCode());
			transactionResponser.addHash(fields);
			return fields.getFields();
		}
		AcquirerType acquirer = AcquirerType.getInstancefromName(acquirerName);
		fields.put(FieldType.ACQUIRER_TYPE.getName(), acquirer.toString());
		switch (acquirer) {

		case IDFCUPI:
			// go for idbi collect
			fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
			fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.IDFCUPI.getCode());
			fields.put(FieldType.CURRENCY_CODE.getName(), "356");
			fields.put(FieldType.PG_REF_NUM.getName(), TransactionManager.getNewTransactionId());
			fields.put(FieldType.TXN_ID.getName(), fields.get(FieldType.PG_REF_NUM.getName()));

			fields.put(FieldType.PAYER_NAME.getName(), fields.get(FieldType.CUST_NAME.getName()));
			if (StringUtils.isNotBlank(user.getSuperMerchantId()) && !user.isSuperMerchant()) {
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
			}

			securityProcessor.addAcquirerFields(fields);
			Transaction transactionResponse = new Transaction();
			JSONObject request = converter.perpareRequest(fields, fields.get(FieldType.ADF6.getName()));
			JSONObject jsonResponse = communicator.getResponse(request, fields);
			if (StringUtils.isNotBlank(jsonResponse.toString())) {
				transactionResponse = converter.toTransaction(jsonResponse, fields);
				upiTransformer.updateResponse(fields, transactionResponse);

			} else {
				logger.info(
						"Collect API  Collect Response, if response is blank " + fields.get(FieldType.TXNTYPE.getName())
								+ " " + "Txn id" + fields.get(FieldType.TXN_ID.getName()) + " " + jsonResponse);
				upiTransformer.updateResponse(fields, transactionResponse);
			}

			break;

		case CASHFREE:
			// CASHFREE Collect Request
			fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
			fields.put(FieldType.PAYMENT_TYPE.getName(), MopType.UPI.getCode());
			fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.CASHFREE.getCode());
			fields.put(FieldType.CURRENCY_CODE.getName(), "356");
			fields.put(FieldType.PG_REF_NUM.getName(), TransactionManager.getNewTransactionId());
			fields.put(FieldType.TXN_ID.getName(), fields.get(FieldType.PG_REF_NUM.getName()));

			fields.put(FieldType.PAYER_NAME.getName(), fields.get(FieldType.CUST_NAME.getName()));

			if (StringUtils.isNotBlank(user.getSuperMerchantId()) && !user.isSuperMerchant()) {
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
			}

			securityProcessor.addAcquirerFields(fields);
			ProcessManager.flow(cashfreeProcessor, fields, false);

			break;

		default:
			break;

		}
		fields.put(FieldType.ORIG_TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));
		fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
		fields.put(FieldType.MOP_TYPE.getName(), MopType.UPI.getCode());
		fields.put(FieldType.PAYMENT_TYPE.getName(), MopType.UPI.getCode());
		fields.put(FieldType.PAYMENTS_REGION.getName(), AccountCurrencyRegion.DOMESTIC.name());
		fields.put(FieldType.CARD_HOLDER_TYPE.getName(), CardHolderType.CONSUMER.name());
		fields.put(FieldType.ACQUIRER_MODE.getName(), onUsOffUs.OFF_US.name());

		securityProcessor.addTransactionDataFields(fields);

		ProcessManager.flow(updateProcessor, fields, true);

		if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.SENT_TO_BANK.getName())
				&& fields.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase(ErrorType.SUCCESS.getCode())) {
			fields.put(FieldType.STATUS.getName(), StatusType.PENDING.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PENDING.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PENDING.getResponseMessage());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), "Collect Initiated Successfully");
		} else {
			fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), "Collect Initiated Failed");
		}

		fields.removeInternalFields();
		fields.removeSecureFields();
		fields.remove(FieldType.ORIG_TXN_ID.getName());
		fields.remove(FieldType.HASH.getName());
		fields.remove(FieldType.ACQUIRER_TYPE.getName());
		fields.remove(FieldType.UDF1.getName());
		fields.remove(FieldType.MERCHANT_GST.getName());
		fields.remove(FieldType.RESELLER_CHARGES.getName());
		fields.remove(FieldType.MOP_TYPE.getName());
		fields.remove(FieldType.SLAB_ID.getName());
		fields.remove(FieldType.ACQUIRER_GST.getName());
		fields.remove(FieldType.PG_TDR_SC.getName());
		fields.remove(FieldType.CARD_HOLDER_TYPE.getName());
		fields.remove(FieldType.PAYMENTS_REGION.getName());
		fields.remove(FieldType.ACQUIRER_MODE.getName());
		fields.remove(FieldType.PART_SETTLE.getName());
		fields.remove(FieldType.OID.getName());
		fields.remove(FieldType.RESELLER_GST.getName());
		fields.remove(FieldType.PG_GST.getName());
		fields.remove(FieldType.MERCHANT_TDR_SC.getName());
		fields.remove(FieldType.SUF_GST.getName());
		fields.remove(FieldType.ACQUIRER_TDR_SC.getName());
		fields.remove(FieldType.SUF_TDR.getName());
		fields.remove(FieldType.PG_DATE_TIME.getName());
		fields.remove(FieldType.MERCHANT_ID.getName());
		fields.remove(FieldType.CASHFREE_FINAL_REQUEST.getName());
		fields.remove(FieldType.TXN_KEY.getName());
		if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
			fields.put(FieldType.PAY_ID.getName(), fields.get(FieldType.SUB_MERCHANT_ID.getName()));
			fields.remove(FieldType.SUB_MERCHANT_ID.getName());
		}

		transactionResponser.addResponseDateTime(fields);
		transactionResponser.addHash(fields);

		return fields.getFields();

	}

	public Fields validateVpaFields(Fields fields) {
		if (fields.contains(FieldType.PAY_ID.getName())
				&& StringUtils.isNotBlank(fields.get(FieldType.PAY_ID.getName()))
				&& StringUtils.isNumeric(fields.get(FieldType.PAY_ID.getName()))) {

			if (fields.contains(FieldType.PAYER_ADDRESS.getName())
					&& StringUtils.isNotBlank(fields.get(FieldType.PAYER_ADDRESS.getName()))
					&& generalValidator.isValidUpiAddress(fields.get(FieldType.PAYER_ADDRESS.getName()))) {

				logger.info("All request fields are valid");
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
				return fields;

			}
			logger.info("Invalid PAYER_ADDRESS");
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYER_ADDRESS.getResponseCode());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYER_ADDRESS.getResponseMessage());
			return fields;
		}
		logger.info("Invalid PAY_ID");
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAY_ID.getResponseCode());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAY_ID.getResponseMessage());
		return fields;
	}

	public Fields validateCollectAmountFields(Fields fields) {
		if (fields.contains(FieldType.PAY_ID.getName())
				&& StringUtils.isNotBlank(fields.get(FieldType.PAY_ID.getName()))
				&& StringUtils.isNumeric(fields.get(FieldType.PAY_ID.getName()))) {

			if (fields.contains(FieldType.AMOUNT.getName())
					&& StringUtils.isNotBlank(fields.get(FieldType.AMOUNT.getName()))
					&& StringUtils.isNumeric(fields.get(FieldType.AMOUNT.getName()))) {

				if (fields.contains(FieldType.ORDER_ID.getName())
						&& StringUtils.isNotBlank(fields.get(FieldType.ORDER_ID.getName()))) {

					if (fields.contains(FieldType.CURRENCY_CODE.getName())
							&& StringUtils.isNotBlank(fields.get(FieldType.CURRENCY_CODE.getName()))
							&& StringUtils.isNumeric(fields.get(FieldType.CURRENCY_CODE.getName()))) {

						if (fields.contains(FieldType.PAYER_ADDRESS.getName())
								&& StringUtils.isNotBlank(fields.get(FieldType.PAYER_ADDRESS.getName()))
								&& generalValidator.isValidUpiAddress(fields.get(FieldType.PAYER_ADDRESS.getName()))) {

							logger.info("All request fields are valid");
							fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
							fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
							return fields;

						}
						logger.info("Invalid PAYER_ADDRESS");
						fields.put(FieldType.RESPONSE_MESSAGE.getName(),
								ErrorType.INVALID_PAYER_ADDRESS.getResponseMessage());
						fields.put(FieldType.RESPONSE_CODE.getName(),
								ErrorType.INVALID_PAYER_ADDRESS.getResponseCode());
						return fields;
					}
					logger.info("Invalid CURRENCY CODE");
					fields.put(FieldType.RESPONSE_MESSAGE.getName(),
							ErrorType.INVALID_CURRENCY_CODE.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_CURRENCY_CODE.getResponseCode());
					return fields;
				}
				logger.info("Invalid ORDER_ID");
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_ORDER_ID.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_ORDER_ID.getResponseCode());
				return fields;
			}
			logger.info("Invalid AMOUNT");
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_AMOUNT.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_AMOUNT.getResponseCode());
			return fields;
		}
		logger.info("Invalid PAY_ID");
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseCode());
		return fields;
	}

	public boolean validateHash(Fields fields) throws SystemException {
		String fieldsHash = fields.remove(FieldType.HASH.getName());
		if (StringUtils.isEmpty(fieldsHash)) {
			return false;
		}

		String calculateHash = Hasher.getHash(fields);
		if (!calculateHash.equalsIgnoreCase(fieldsHash)) {
			StringBuilder hashMessage = new StringBuilder("Merchant hash =");
			hashMessage.append(fieldsHash);
			hashMessage.append(", Calculated Hash=");
			hashMessage.append(calculateHash);
			logger.error(hashMessage.toString());
			return false;
		}
		return true;
	}

	public String getAcquirer(Fields fields) {

		String acquirerName = "";
		User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));

		if (StringUtils.isNotBlank(user.getSuperMerchantId()) && !user.isSuperMerchant()) {
			fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
		}
		BigDecimal txnAmount = new BigDecimal(Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
				fields.get(FieldType.CURRENCY_CODE.getName())));
		String slabId = "";
		if (txnAmount.compareTo(minAmountSlab3) >= 0) {
			slabId = "03";
		} else if (txnAmount.compareTo(minAmountSlab2) >= 0) {
			slabId = "02";
		} else {
			slabId = "01";
		}
		String identifier = fields.get(FieldType.PAY_ID.getName()) + fields.get(FieldType.CURRENCY_CODE.getName())
				+ PaymentType.UPI.getCode() + MopType.UPI.getCode() + TransactionType.SALE.getName()
				+ AccountCurrencyRegion.DOMESTIC.name() + CardHolderType.CONSUMER.name() + slabId;
		List<RouterConfiguration> rulesListUpi = new ArrayList<RouterConfiguration>();
		rulesListUpi = routerConfigurationDao.findActiveAcquirersByIdentifier(identifier);

		if (rulesListUpi.size() == 0) {
			logger.info("No acquirer found for identifier = " + identifier + " Order id "
					+ fields.get(FieldType.ORDER_ID.getName()));
		} else if (rulesListUpi.size() > 1) {
			int randomNumberUpi = getRandomNumber();
			int min = 1;
			int max = 0;
			for (RouterConfiguration routerConfiguration : rulesListUpi) {
				int loadPercentage = routerConfiguration.getLoadPercentage();
				min = 1 + max;
				max = max + loadPercentage;
				if (randomNumberUpi >= min && randomNumberUpi < max) {
					acquirerName = AcquirerType.getAcquirerName(routerConfiguration.getAcquirer());
					fields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");
					break;
				}
			}
		} else {

			for (RouterConfiguration routerConfiguration : rulesListUpi) {
				acquirerName = AcquirerType.getAcquirerName(routerConfiguration.getAcquirer());
				fields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");
			}
		}
		if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
			fields.put(FieldType.PAY_ID.getName(), fields.get(FieldType.SUB_MERCHANT_ID.getName()));
			fields.remove(FieldType.SUB_MERCHANT_ID.getName());
		}
		if (StringUtils.isEmpty(acquirerName)) {
			return acquirerName;
		} //
		fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.getInstancefromName(acquirerName).getCode());
		logger.info("Acquirer for identifier = " + identifier + " Order id " + fields.get(FieldType.ORDER_ID.getName())
				+ " Pay id " + fields.get(FieldType.PAY_ID.getName()) + " Acquirer = " + acquirerName);
		return acquirerName;

	}

	public String getAcquirerVpaValidation(Fields fields) {

		String acquirerName = PropertiesManager.propertiesMap.get("VPA_VALIDATION_ACQUIRER");

		return acquirerName;
		// Commented by shaiwal for load testing
		/*
		 * String acquirerName = ""; User user =
		 * userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		 * 
		 * if (StringUtils.isNotBlank(user.getSuperMerchantId()) &&
		 * !user.isSuperMerchant()) { fields.put(FieldType.SUB_MERCHANT_ID.getName(),
		 * fields.get(FieldType.PAY_ID.getName()));
		 * fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId()); }
		 * BigDecimal txnAmount = new
		 * BigDecimal(Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
		 * fields.get(FieldType.CURRENCY_CODE.getName()))); String slabId = ""; if
		 * (txnAmount.compareTo(minAmountSlab3) >= 0) { slabId = "03"; } else if
		 * (txnAmount.compareTo(minAmountSlab2) >= 0) { slabId = "02"; } else { slabId =
		 * "01"; } String identifier = fields.get(FieldType.PAY_ID.getName()) +
		 * fields.get(FieldType.CURRENCY_CODE.getName()) + PaymentType.UPI.getCode() +
		 * MopType.UPI.getCode() + TransactionType.SALE.getName() +
		 * AccountCurrencyRegion.DOMESTIC.name() + CardHolderType.CONSUMER.name() +
		 * slabId; List<RouterConfiguration> rulesListUpi = new
		 * ArrayList<RouterConfiguration>(); rulesListUpi =
		 * routerConfigurationDao.findActiveAcquirersByIdentifier(identifier);
		 * 
		 * if (rulesListUpi.size() == 0) {
		 * logger.info("No acquirer found for identifier = " + identifier + " Order id "
		 * + fields.get(FieldType.ORDER_ID.getName())); } else if (rulesListUpi.size() >
		 * 1) { int randomNumberUpi = getRandomNumber(); int min = 1; int max = 0; for
		 * (RouterConfiguration routerConfiguration : rulesListUpi) { int loadPercentage
		 * = routerConfiguration.getLoadPercentage(); min = 1 + max; max = max +
		 * loadPercentage; if (randomNumberUpi >= min && randomNumberUpi < max) {
		 * acquirerName =
		 * AcquirerType.getAcquirerName(routerConfiguration.getAcquirer());
		 * fields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US"); break; } } } else {
		 * 
		 * for (RouterConfiguration routerConfiguration : rulesListUpi) { acquirerName =
		 * AcquirerType.getAcquirerName(routerConfiguration.getAcquirer());
		 * fields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US"); } } if
		 * (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
		 * fields.put(FieldType.PAY_ID.getName(),
		 * fields.get(FieldType.SUB_MERCHANT_ID.getName()));
		 * fields.remove(FieldType.SUB_MERCHANT_ID.getName()); } if
		 * (StringUtils.isEmpty(acquirerName)) { return acquirerName; } //
		 * fields.put(FieldType.ACQUIRER_TYPE.getName(),
		 * AcquirerType.getInstancefromName(acquirerName).getCode());
		 * logger.info("Acquirer for identifier = " + identifier + " Order id " +
		 * fields.get(FieldType.ORDER_ID.getName()) + " Pay id " +
		 * fields.get(FieldType.PAY_ID.getName()) + " Acquirer = " + acquirerName);
		 * return acquirerName;
		 */
	}

	private static int getRandomNumber() {
		Random rnd = new Random();
		int randomNumber = (int) (rnd.nextInt(100)) + 1;
		return randomNumber;
	}
}
