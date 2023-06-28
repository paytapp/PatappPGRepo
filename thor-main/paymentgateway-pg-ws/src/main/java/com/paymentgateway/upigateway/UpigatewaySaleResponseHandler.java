package com.paymentgateway.upigateway;

import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.ProcessManager;
import com.paymentgateway.pg.core.util.Processor;

@Service
public class UpigatewaySaleResponseHandler {

	private static Logger logger = LoggerFactory.getLogger(UpigatewaySaleResponseHandler.class.getName());

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	@Qualifier("upigatewayTransactionConverter")
	private TransactionConverter transactionConverter;

	@Autowired
	private UpigatewayTransformer upigatewayTransformer;

	@Autowired
	private FieldsDao fieldsDao;
	
	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	@Qualifier("upigatewayTransactionCommunicator")
	private TransactionCommunicator transactionCommunicator;

	public Map<String, String> process(Fields fields) throws SystemException {

		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getCode());
		Transaction transactionResponse = new Transaction();
		String response = fields.get(FieldType.UPIGATEWAY_RESPONSE_FIELD.getName());
		logger.info("UPIGateway Response Fields >>> " + response);
		transactionResponse = toTransaction(response, fields);
		
		AccountCurrency ac = getAccountCurrency(fields);
		transactionResponse.setKey(ac.getMerchantId());
		
		boolean isDuplicateResponse = false;
		boolean isAmountMatchResponse = false;

		isDuplicateResponse = fieldsDao.checkUpigatewayDuplicateCapture(transactionResponse.getClient_txn_id(),
				fields.get(FieldType.PAY_ID.getName()));
		transactionResponse = verifyTransaction(fields, transactionResponse);
		isAmountMatchResponse = verifyAmount(fields, transactionResponse);
		
		if (!isAmountMatchResponse) {

			logger.warn("UPIGateway amount in response did not match with amount in database");
			fields.put(FieldType.STATUS.getName(), StatusType.DENIED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SIGNATURE_MISMATCH.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SIGNATURE_MISMATCH.getResponseMessage());
		}

		else if (isDuplicateResponse) {

			logger.warn("UPIGateway Response is duplicate, response pg ref is already present in database");
			fields.put(FieldType.STATUS.getName(), StatusType.DENIED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DUPLICATE.getResponseMessage());

		} else {
			upigatewayTransformer = new UpigatewayTransformer(transactionResponse);
			upigatewayTransformer.updateResponse(fields);
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

		
		try {
			
			String statusUrl = propertiesManager.propertiesMap.get(Constants.UPIGATEWAY_STATUS_ENQUIRY_URL);
			
			JSONObject reqJson = new JSONObject();
			SimpleDateFormat format2 = new SimpleDateFormat("dd-MM-yyyy");

			String date2 = format2.format(new Date());
			
			
			reqJson.put(Constants.client_txn_id, transactionResponse.getClient_txn_id());
			reqJson.put(Constants.txn_date, date2);
			reqJson.put(Constants.key, transactionResponse.getKey());
			
			
			String statusResponse = transactionCommunicator.statusEnqPostRequest(statusUrl,reqJson.toString(),transactionResponse);
			Transaction transactionStatus = transactionConverter.toStatusTransaction(statusResponse);
			return transactionStatus;
			
		} catch (Exception e) {
			logger.info("Exception in verifyTransaction for UPIGateway",e);
			return null;
		}

	}

	private boolean verifyAmount(Fields fields, Transaction transactionResponse) throws SystemException {

		String amountFromFields = fields.get(FieldType.TOTAL_AMOUNT.getName());
		
		amountFromFields = amountFromFields.substring(0, amountFromFields.length() - 2);
		String amountInStatus =  transactionResponse.getAmount();
		
		if (amountFromFields.equalsIgnoreCase(amountInStatus)) {

			logger.info(
					"UPIGateway Amount in real time transaction matches with Amount in verification transaction for Amount real time = "
							+ amountFromFields
							+ " Amount in Status = " + amountInStatus);
			return true;
		} else {
			logger.info(
					"UPIGateway Amount in real time transaction does not match with Amount in verification transaction for Amount real time = "
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
						.equalsIgnoreCase(AcquirerType.getInstancefromCode(AcquirerType.UPIGATEWAY.getCode()).getName())) {
					account = accountThis;
					break;
				}
			}
		}

		accountCurrency = account.getAccountCurrency(fields.get(FieldType.CURRENCY_CODE.getName()));
		return accountCurrency;

	}
}
