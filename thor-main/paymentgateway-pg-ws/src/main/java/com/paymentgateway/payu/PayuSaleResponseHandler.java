package com.paymentgateway.payu;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.kms.EncryptDecryptService;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.Validator;
import com.paymentgateway.pg.core.util.PayuUtil;
import com.paymentgateway.pg.core.util.ProcessManager;
import com.paymentgateway.pg.core.util.Processor;

/**
 * @author Rahul
 *
 */
@Service
public class PayuSaleResponseHandler {
	private static Logger logger = LoggerFactory.getLogger(PayuSaleResponseHandler.class.getName());

	@Autowired
	private Validator generalValidator;

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	private UserDao userDao;

	@Autowired
	private PayuStatusEnquiryProcessor payuStatusEnquiryProcessor;

	@Autowired
	private EncryptDecryptService encryptDecryptService;

	public Map<String, String> process(Fields fields) throws SystemException {

		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		generalValidator.validate(fields);
		String response = fields.get(FieldType.PAYU_RESPONSE_FIELD.getName());

		Transaction transactionResponse = toTransaction(response, fields);
		/*
		 * boolean res = isHashMatching(transactionResponse, fields); boolean doubleVer
		 * = doubleVerification(transactionResponse, fields);
		 */
		
		PayuTransformer payuTransformer = new PayuTransformer(transactionResponse);
		payuTransformer.updateResponse(fields);

		/*
		 * if (res && doubleVer) { PayuTransformer payuTransformer = new
		 * PayuTransformer(transactionResponse); payuTransformer.updateResponse(fields);
		 * } else { fields.put(FieldType.STATUS.getName(), StatusType.DENIED.getName());
		 * fields.put(FieldType.RESPONSE_CODE.getName(),
		 * ErrorType.SIGNATURE_MISMATCH.getCode());
		 * fields.put(FieldType.RESPONSE_MESSAGE.getName(),
		 * ErrorType.SIGNATURE_MISMATCH.getResponseMessage()); }
		 */

		fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
		fields.remove(FieldType.PAYU_RESPONSE_FIELD.getName());
		ProcessManager.flow(updateProcessor, fields, true);
		return fields.getFields();

	}

	private boolean doubleVerification(Transaction transactionResponse, Fields fields) throws SystemException {
		try {
			if (StringUtils.isBlank(transactionResponse.getResponseCode())) {
				return false;
			}
			// Don't check for transactions other than successful transactions.
			if (!(transactionResponse.getResponseCode()).equalsIgnoreCase(Constants.E000)) {
				return true;
			}

			fields.logAllFields("Payu Double verification fields : ");
			String request = payuStatusEnquiryProcessor.statusEnquiryRequest(fields);
			logger.info("PayU double verification reequest : order id : " + fields.get(FieldType.ORDER_ID.getName())
					+ " : " );log(request, fields);
			String response = PayuStatusEnquiryProcessor.getResponse(request);
			logger.info("PayU double verification response : order id : " + fields.get(FieldType.ORDER_ID.getName())
					+ " : " );log(request, fields);

			Transaction doubleVerTxnResponse = payuStatusEnquiryProcessor.toTransaction(response,
					fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()), fields.get(FieldType.PG_REF_NUM.getName()));

			if (doubleVerTxnResponse == null) {
				logger.info("Double verification transaction response is null.");
				return false;
			}

			if (StringUtils.isBlank(doubleVerTxnResponse.getAmount())
					|| StringUtils.isBlank(transactionResponse.getAmount())) {
				logger.info("Amount is empty in txn response : " + transactionResponse.getAmount()
						+ ", double verification : " + doubleVerTxnResponse.getAmount());
				return false;
			}

			String fieldsAmount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName()));

			if ((doubleVerTxnResponse.getResponseCode().equals(transactionResponse.getResponseCode()))
					&& doubleVerTxnResponse.getAmount().equals(fieldsAmount)) {
				return true;
			} else {
				logger.info(
						"Response mismatch in double verification of payu transaction : double verification response : "
								+ doubleVerTxnResponse.getResponseCode() + " , transcation response : "
								+ transactionResponse.getResponseCode());
				logger.info("Amount mismatch in double verification of payu transaction : double verification amount : "
						+ doubleVerTxnResponse.getAmount() + " , fields amount : " + fieldsAmount);
				return false;
			}

		} catch (Exception e) {
			logger.info("Exception while payu double verification : " + e);
			logger.info(e.getMessage());
			return false;
		}

	}

	private boolean isHashMatching(Transaction transactionResponse, Fields fields) throws SystemException {

		if (StringUtils.isBlank(transactionResponse.getResponseCode())) {
			return false;
		}

		// Don't check for transactions other than successful transactions.
		if (!(transactionResponse.getResponseCode()).equalsIgnoreCase(Constants.E000)) {
			return true;
		}

		ErrorType errorType = null;
		getTxnKey(fields);
		String salt = fields.get(FieldType.PASSWORD.getName());
		String merchantkey = fields.get(FieldType.MERCHANT_ID.getName());

		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put(Constants.SALT, salt);
		reqMap.put(Constants.STATUS, transactionResponse.getStatus());
		reqMap.put(Constants.EMAIL, transactionResponse.getEmail());
		String firstName = fields.get(FieldType.CUST_NAME.getName());
		if (StringUtils.isBlank(firstName)) {
			firstName = Constants.PAYMENT_GATEWAY;
		}

		reqMap.put(Constants.FIRSTNAME, firstName);
		reqMap.put(Constants.AMOUNT, Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
				fields.get(FieldType.CURRENCY_CODE.getName())));
		reqMap.put(Constants.TXNID, transactionResponse.getTxnId());
		reqMap.put(Constants.KEY, merchantkey);

		String resphash = transactionResponse.getHash();
		String calRespHash = PayuUtil.payuResponseHash(reqMap);

		if (StringUtils.isBlank(resphash) || StringUtils.isBlank(calRespHash)) {
			logger.info("Response hash or calculated hash is empty");
			errorType = ErrorType.SIGNATURE_MISMATCH;
			fields.put(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
			return false;
		}

		if (resphash.equals(calRespHash)) {
			return true;
		} else {
			logger.info("Response hash, calculated hash mismatch");
			logger.info("Response hash   : " + resphash);
			logger.info("Calculated hash : " + calRespHash);
			logger.info(reqMap.toString());
			errorType = ErrorType.SIGNATURE_MISMATCH;
			fields.put(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
			return false;
		}
	}

	public void getTxnKey(Fields fields) throws SystemException {
		User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		Account account = null;
		Set<Account> accounts = user.getAccounts();
		Map<String, String> userDetails = new HashMap<String, String>();
		if (accounts == null || accounts.size() == 0) {
			logger.info("No account found for Pay ID = " + fields.get(FieldType.PAY_ID.getName()) + " and ORDER ID = "
					+ fields.get(FieldType.ORDER_ID.getName()));
		} else {
			for (Account accountThis : accounts) {
				if (accountThis.getAcquirerName()
						.equalsIgnoreCase(AcquirerType.getInstancefromCode(AcquirerType.PAYU.getCode()).getName())) {
					account = accountThis;
					break;
				}
			}
		}

		AccountCurrency accountCurrency = account.getAccountCurrency(fields.get(FieldType.CURRENCY_CODE.getName()));
		String mId = accountCurrency.getMerchantId();
		String password = encryptDecryptService.decrypt(fields.get(FieldType.PAY_ID.getName()),
				accountCurrency.getPassword());

		fields.put(FieldType.MERCHANT_ID.getName(), mId);
		fields.put(FieldType.PASSWORD.getName(), password);

	}

	public Transaction toTransaction(String response, Fields fields) {

		Transaction transaction = new Transaction();

		if (StringUtils.isBlank(response)) {

			logger.info("Empty response received ");
			return transaction;
		}

		String respArray[] = response.replace(" ", "").split(";");

		for (String data : respArray) {

			if (data.contains(Constants.ERROR_MESSAGE)) {

				String dataArray[] = data.split("=");
				transaction.setResponseMsg(dataArray[1]);
			}

			if (data.contains(Constants.ERROR)) {

				String dataArray[] = data.split("=");
				if (dataArray[0].equalsIgnoreCase(Constants.ERROR)) {

					if (StringUtils.isNotBlank(dataArray[1])) {
						transaction.setResponseCode(dataArray[1]);
					}
				}
			}

			if (data.contains(Constants.MIHPAYID)) {

				String dataArray[] = data.split("=");
				if (dataArray[0].equalsIgnoreCase(Constants.MIHPAYID)) {

					if (StringUtils.isNotBlank(dataArray[1])) {
						transaction.setMihPayuId(dataArray[1]);
					}

				}

			}

			if (data.contains(Constants.TXNID)) {

				String dataArray[] = data.split("=");

				if (dataArray[0].equalsIgnoreCase(Constants.TXNID)) {

					if (StringUtils.isNotBlank(dataArray[1])) {
						transaction.setTxnId(dataArray[1]);
					}

				}

			}
			if (data.contains(Constants.STATUS)) {

				String dataArray[] = data.split("=");

				if (dataArray[0].equalsIgnoreCase(Constants.STATUS)) {

					if (StringUtils.isNotBlank(dataArray[1])) {
						transaction.setStatus(dataArray[1]);
					}

				}

			}

			if (data.contains(Constants.BANK_REF_NUMB)) {

				String dataArray[] = data.split("=");
				if (dataArray[0].equalsIgnoreCase(Constants.BANK_REF_NUMB)) {

					if (dataArray.length > 1 && StringUtils.isNotBlank(dataArray[1])) {
						transaction.setBankRefNum(dataArray[1]);
					}
				}

			}

			if (data.contains(Constants.HASH)) {

				String dataArray[] = data.split("=");
				if (dataArray[0].equalsIgnoreCase(Constants.HASH)) {

					if (dataArray.length > 1 && StringUtils.isNotBlank(dataArray[1])) {
						transaction.setHash(dataArray[1]);
					}
				}

			}

			if (data.contains(Constants.EMAIL)) {

				String dataArray[] = data.split("=");
				if (dataArray[0].equalsIgnoreCase(Constants.EMAIL)) {

					if (dataArray.length > 1 && StringUtils.isNotBlank(dataArray[1])) {
						transaction.setEmail(dataArray[1]);
					}
				}

			}
			if (data.contains(Constants.FIRSTNAME)) {

				String dataArray[] = data.split("=");
				if (dataArray[0].equalsIgnoreCase(Constants.FIRSTNAME)) {

					if (dataArray.length > 1 && StringUtils.isNotBlank(dataArray[1])) {
						transaction.setFirstName(dataArray[1]);
					}
				}

			}

			if (data.contains(Constants.PRODUCT_INFO)) {
				String dataArray[] = data.split("=");
				if (dataArray[0].equalsIgnoreCase(Constants.PRODUCT_INFO)) {

					if (dataArray.length > 1 && StringUtils.isNotBlank(dataArray[1])) {
						transaction.setProductInfo(dataArray[1]);
					}
				}

			}
			if (data.contains(Constants.AMOUNT)) {
				String dataArray[] = data.split("=");
				if (dataArray[0].equalsIgnoreCase(Constants.AMOUNT)) {

					if (dataArray.length > 1 && StringUtils.isNotBlank(dataArray[1])) {
						transaction.setAmount(dataArray[1]);
					}
				}

			}
		}

		return transaction;
	}
	
	private static void log(String message, Fields fields){
		message = Pattern.compile("(cardInfo\":\")([\\s\\S]*?)(\",\")").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(CARD_EXP_DT\":\")([\\s\\S]*?)(\",\")").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(cvv\":\")([\\s\\S]*?)(\",\")").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<pan>)([\\s\\S]*?)(</pan>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<card>)([\\s\\S]*?)(</card>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<pan>)([\\s\\S]*?)(</pan>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<expmonth>)([\\s\\S]*?)(</expmonth>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<expyear>)([\\s\\S]*?)(</expyear>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<cvv2>)([\\s\\S]*?)(</cvv2>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<password>)([\\s\\S]*?)(</password>)").matcher(message).replaceAll("$1$3");
	//	MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
		logger.info(message);
	}

}
