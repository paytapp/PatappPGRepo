package com.paymentgateway.grezpay;

import java.util.Map;
import java.util.Set;

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

@Service
public class GrezpaySaleResponseHandler {

	private static Logger logger = LoggerFactory.getLogger(GrezpaySaleResponseHandler.class.getName());

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	@Qualifier("grezpayTransactionConverter")
	private TransactionConverter transactionConverter;

	@Autowired
	private GrezpayTransformer grezpayTransformer;

	@Autowired
	private FieldsDao fieldsDao;
	
	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	@Qualifier("grezpayTransactionCommunicator")
	private TransactionCommunicator transactionCommunicator;

	public Map<String, String> process(Fields fields) throws SystemException {

		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getCode());
		Transaction transactionResponse = new Transaction();
		String response = fields.get(FieldType.GREZPAY_RESPONSE_FIELD.getName());
		logger.info("Grezpay Response Fields >>> " + response);
		transactionResponse = toTransaction(response, fields);
		transactionResponse.setStatusFields(fields);
		
		boolean isDuplicateResponse = false;
		boolean isAmountMatchResponse = false;

		isDuplicateResponse = fieldsDao.checkGrezpayDuplicateCapture(transactionResponse.getMerchant_order_token(),
				fields.get(FieldType.PAY_ID.getName()));
		transactionResponse = verifyTransaction(fields, transactionResponse);
		isAmountMatchResponse = verifyAmount(fields, transactionResponse);
		
		if (!isAmountMatchResponse) {

			logger.warn("Grezpay amount in response did not match with amount in database");
			fields.put(FieldType.STATUS.getName(), StatusType.DENIED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SIGNATURE_MISMATCH.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SIGNATURE_MISMATCH.getResponseMessage());
		}

		else if (isDuplicateResponse) {

			logger.warn("Grezpay Response is duplicate, response pg ref is already present in database");
			fields.put(FieldType.STATUS.getName(), StatusType.DENIED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DUPLICATE.getResponseMessage());

		} else {
			grezpayTransformer = new GrezpayTransformer(transactionResponse);
			grezpayTransformer.updateResponse(fields);
		}

		fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
		ProcessManager.flow(updateProcessor, fields, true);
		return fields.getFields();

	}

	public Transaction toTransaction(String response, Fields fields) {
		Transaction transaction = new Transaction();
		transaction = transactionConverter.toTransaction(response, fields);
		return transaction;
	}

	private Transaction verifyTransaction(Fields fields, Transaction transactionResponse) throws SystemException {

		String statusUrl = propertiesManager.propertiesMap.get(Constants.GREZPAY_STATUS_ENQUIRY_URL);
		
		JSONObject reqJson = new JSONObject();
		reqJson.put(Constants.identifier, transactionResponse.getIdentifier());
		
		String statusResponse = transactionCommunicator.statusEnqPostRequest(statusUrl,reqJson.toString(),transactionResponse);
		Transaction transactionStatus = transactionConverter.toStatusTransaction(statusResponse);
		return transactionStatus;

	}

	private boolean verifyAmount(Fields fields, Transaction transactionResponse) throws SystemException {

		String amountFromFields = Amount.toDecimal(
				fields.get(FieldType.TOTAL_AMOUNT.getName()), fields.get(FieldType.CURRENCY_CODE.getName())).split("\\.")[0];
		String amountInStatus =  transactionResponse.getAmount().split("\\.")[0];
		
		if (amountFromFields.equalsIgnoreCase(amountInStatus)) {

			logger.info(
					"Grezpay Amount in real time transaction matches with Amount in verification transaction for Amount real time = "
							+ amountFromFields
							+ " Amount in Status = " + amountInStatus);
			return true;
		} else {
			logger.info(
					"Grezpay Amount in real time transaction does not match with Amount in verification transaction for Amount real time = "
							+ amountFromFields + " Amount in Status = "
							+ amountInStatus);
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
				if (accountThis.getAcquirerName()
						.equalsIgnoreCase(AcquirerType.getInstancefromCode(AcquirerType.GREZPAY.getCode()).getName())) {
					account = accountThis;
					break;
				}
			}
		}

		accountCurrency = account.getAccountCurrency(fields.get(FieldType.CURRENCY_CODE.getName()));
		return accountCurrency;

	}
}
