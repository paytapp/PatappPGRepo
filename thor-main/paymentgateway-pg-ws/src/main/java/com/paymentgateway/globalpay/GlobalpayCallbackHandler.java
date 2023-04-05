package com.paymentgateway.globalpay;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.ProcessManager;
import com.paymentgateway.pg.core.util.Processor;
import com.paymentgateway.pg.service.IPManagerUtil;

@Service
public class GlobalpayCallbackHandler {

	private static Logger logger = LoggerFactory.getLogger(GlobalpayCallbackHandler.class.getName());

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	@Qualifier("globalpayTransactionConverter")
	private TransactionConverter transactionConverter;

	@Autowired
	private GlobalpayTransformer globalpayTransformer;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private IPManagerUtil iPManagerUtil;
	
	@Autowired
	@Qualifier("globalpayTransactionCommunicator")
	private TransactionCommunicator transactionCommunicator;

	public void process(String callback ,HttpServletRequest httpRequest ) throws SystemException {

		// Check Source IP Address
		String responseIp = iPManagerUtil.getIp(httpRequest);
		logger.info("Globalpay Callback Received from IP {} for Fields {}",responseIp,callback);
		
		String whitelistIp =  propertiesManager.propertiesMap.get("GLOBALPAYIPADD");
		
		if (!whitelistIp.contains(responseIp)) {
			logger.info("Globalpay Callback Received from non-whitelisted IP {} for Fields {}",responseIp,callback);
			return;
		}
		
		// Validate JSON
		JSONObject callbackJson = new JSONObject(callback);
		
		String event_type = callbackJson.get("event_type").toString();
		logger.info("event_type is {}" , event_type);
		
		if (event_type.equalsIgnoreCase("TRANSACTION")) {
			JSONObject dataJson = callbackJson.getJSONObject("data");
			
			String transaction_id = dataJson.get("transaction_id").toString();
			String merchant_order_id = dataJson.get("merchant_order_id").toString();
			String payment_amount = dataJson.get("payment_amount").toString();
			
			logger.info("transaction_id = {} , merchant_order_id = {} , payment_amount = {} ",transaction_id,merchant_order_id,payment_amount);
		}
		
		
		// Check status in DB for Transaction , if status is sent to bank / captured then ignore this callback
		
		
		// Else update the DB
		
		/*
		 * Fields fields = new Fields();
		 * 
		 * String newTxnId = TransactionManager.getNewTransactionId();
		 * fields.put(FieldType.TXN_ID.getName(), newTxnId);
		 * fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getCode());
		 * Transaction transactionResponse = new Transaction(); String response =
		 * fields.get(FieldType.GLOBALPAY_RESPONSE_FIELD.getName());
		 * logger.info("Globalpay Response Fields >>> " + response); transactionResponse
		 * = toTransaction(response, fields); AccountCurrency ac =
		 * getAccountCurrency(fields);
		 * transactionResponse.setMerchant_id(ac.getMerchantId());
		 * transactionResponse.setMerchant_key(ac.getTxnKey());
		 * 
		 * boolean isDuplicateResponse = false; boolean isAmountMatchResponse = false;
		 * 
		 * isDuplicateResponse =
		 * fieldsDao.checkGlobalpayDuplicateCapture(transactionResponse.
		 * getMerchant_order_id(), fields.get(FieldType.PAY_ID.getName()));
		 * transactionResponse = verifyTransaction(fields, transactionResponse);
		 * isAmountMatchResponse = verifyAmount(fields, transactionResponse);
		 * 
		 * if (!isAmountMatchResponse) {
		 * 
		 * logger.
		 * warn("Globalpay amount in response did not match with amount in database");
		 * fields.put(FieldType.STATUS.getName(), StatusType.DENIED.getName());
		 * fields.put(FieldType.RESPONSE_CODE.getName(),
		 * ErrorType.SIGNATURE_MISMATCH.getCode());
		 * fields.put(FieldType.RESPONSE_MESSAGE.getName(),
		 * ErrorType.SIGNATURE_MISMATCH.getResponseMessage()); }
		 * 
		 * else if (isDuplicateResponse) {
		 * 
		 * logger.
		 * warn("Globalpay Response is duplicate, response pg ref is already present in database"
		 * ); fields.put(FieldType.STATUS.getName(), StatusType.DENIED.getName());
		 * fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE.getCode());
		 * fields.put(FieldType.RESPONSE_MESSAGE.getName(),
		 * ErrorType.DUPLICATE.getResponseMessage());
		 * 
		 * } else { globalpayTransformer = new
		 * GlobalpayTransformer(transactionResponse);
		 * globalpayTransformer.updateResponse(fields); }
		 * 
		 * fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(),
		 * fields.get(FieldType.TXNTYPE.getName()));
		 * ProcessManager.flow(updateProcessor, fields, true);
		 */

	}

	public Transaction toTransaction(String response, Fields fields) {
		Transaction transaction = new Transaction();
		transaction = transactionConverter.toTransaction(response, fields);
		return transaction;
	}

	private Transaction verifyTransaction(Fields fields, Transaction transactionResponse) throws SystemException {

		String statusUrl = PropertiesManager.propertiesMap.get(Constants.GLOBALPAY_STATUS_ENQUIRY_URL);
		JSONObject reqJson = new JSONObject();

		reqJson.put(Constants.merchant_order_id, fields.get(FieldType.PG_REF_NUM.getName()));

		String statusResponse = transactionCommunicator.statusEnqPostRequest(reqJson.toString(), statusUrl,
				transactionResponse);
		Transaction transactionStatus = transactionConverter.toStatusTransaction(statusResponse);
		return transactionStatus;

	}

	private boolean verifyAmount(Fields fields, Transaction transactionResponse) throws SystemException {

		String amountFromFields = Amount
				.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()), fields.get(FieldType.CURRENCY_CODE.getName()))
				.split("\\.")[0];
		String amountInStatus = transactionResponse.getPayment_amount().split("\\.")[0];

		if (amountFromFields.equalsIgnoreCase(amountInStatus)) {

			logger.info(
					"Globalpay Amount in real time transaction matches with Amount in verification transaction for Amount real time = "
							+ amountFromFields + " Amount in Status = " + amountInStatus);
			return true;
		} else {
			logger.info(
					"Globalpay Amount in real time transaction does not match with Amount in verification transaction for Amount real time = "
							+ amountFromFields + " Amount in Status = " + amountInStatus);
			return false;
		}

	}
	
	public AccountCurrency getAccountCurrency(Fields fields) throws SystemException {

		AccountCurrency accountCurrency = null;

		User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		Account account = null;
		Set<Account> accounts = user.getAccounts();

		if (accounts == null || accounts.size() == 0) {
			logger.info("No account found for Pay ID = " + fields.get(FieldType.PAY_ID.getName()) + " and ORDER ID = "
					+ fields.get(FieldType.ORDER_ID.getName()));
		} else {
			for (Account accountThis : accounts) {
				if (accountThis.getAcquirerName().equalsIgnoreCase(
						AcquirerType.getInstancefromCode(AcquirerType.GLOBALPAY.getCode()).getName())) {
					account = accountThis;
					break;
				}
			}
		}

		accountCurrency = account.getAccountCurrency(fields.get(FieldType.CURRENCY_CODE.getName()));
		return accountCurrency;

	}
}
