
package com.paymentgateway.toshanidigital;

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
public class ToshanidigitalSaleResponseHandler {

	private static Logger logger = LoggerFactory.getLogger(ToshanidigitalSaleResponseHandler.class.getName());

	@Autowired

	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired

	@Qualifier("toshanidigitalTransactionConverter")
	private TransactionConverter transactionConverter;

	@Autowired
	private ToshanidigitalTransformer toshanidigitalTransformer;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private UserDao userDao;

	@Autowired

	@Qualifier("toshanidigitalTransactionCommunicator")
	private TransactionCommunicator transactionCommunicator;

	public Map<String, String> process(Fields fields) throws SystemException {

		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getCode());
		Transaction transactionResponse = new Transaction();
		String response = fields.get(FieldType.TOSHANIDIGITAL_RESPONSE_FIELD.getName());
		logger.info("Toshani Digital Response Fields >>> " + response);
		transactionResponse = toTransaction(response, fields);

		AccountCurrency ac = getAccountCurrency(fields);
		transactionResponse.setAccess_token(ac.getMerchantId());

		boolean isDuplicateResponse = false;
		boolean isAmountMatchResponse = false;
		boolean isStatusMatchResponse = false;

		isDuplicateResponse = fieldsDao.checkToshaniDigitalDuplicateCapture(transactionResponse.getOrder_id(),
				fields.get(FieldType.PAY_ID.getName()));
		
		Transaction transactionVerify = verifyTransaction(fields, transactionResponse);
		
		isAmountMatchResponse = verifyAmount(fields, transactionResponse);
		isStatusMatchResponse = verifyStatusCheck(transactionResponse, transactionVerify);
		
		if (!isStatusMatchResponse) {

			logger.warn("Toshani Status in response did not match with Status in callback response");
			fields.put(FieldType.STATUS.getName(), StatusType.DENIED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SIGNATURE_MISMATCH.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SIGNATURE_MISMATCH.getResponseMessage());
		}
		
		if (!isAmountMatchResponse) {

			logger.warn("Toshani amount in response did not match with amount in database");
			fields.put(FieldType.STATUS.getName(), StatusType.DENIED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SIGNATURE_MISMATCH.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SIGNATURE_MISMATCH.getResponseMessage());
		}

		else if (isDuplicateResponse) {

			logger.warn("Toshani Response is duplicate, response Acq Id is already present in database");
			fields.put(FieldType.STATUS.getName(), StatusType.DENIED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DUPLICATE.getResponseMessage());

		} else {
			toshanidigitalTransformer = new ToshanidigitalTransformer(transactionResponse);
			toshanidigitalTransformer.updateResponse(fields,transactionResponse);
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

			String statusUrl = propertiesManager.propertiesMap.get(Constants.TOSHANIDIGITAL_STATUS_ENQUIRY_URL);

			JSONObject reqJson = new JSONObject();

			reqJson.put(Constants.orderid, transactionResponse.getOrder_id());
			reqJson.put(Constants.access_token, transactionResponse.getAccess_token());

			String statusResponse = transactionCommunicator.statusEnqPostRequest(statusUrl, reqJson.toString(),
					transactionResponse);
			Transaction transactionStatus = transactionConverter.toStatusTransaction(statusResponse);
			return transactionStatus;

		} catch (Exception e) {
			logger.info("Exception in verifyTransaction for Toshani", e);
			return null;
		}

	}

	private boolean verifyStatusCheck(Transaction transactionResponse, Transaction transactionVerify) throws SystemException {

		if (!transactionResponse.getOrder_id().toString().equalsIgnoreCase(transactionVerify.getOrder_id().toString())) {

			logger.info(
					"Toshani Order Id in real time transaction does not match with Order Id in verification transaction for Order Id real time = "
							+ transactionResponse.getOrder_id().toString() + " Order Id in Status = " + transactionVerify.getOrder_id().toString());
			return false;
		}
		
		else if (!transactionResponse.getPayments_status().toString().equalsIgnoreCase(transactionVerify.getPayments_status().toString())) {

			logger.info(
					"Toshani Status in real time transaction does not match with Status in verification transaction for Status real time = "
							+ transactionResponse.getPayments_status().toString() + " Status in Status = " + transactionVerify.getPayments_status().toString());
			return false;
		}
		
		else if (!transactionResponse.getPayment_amount().toString().equalsIgnoreCase(transactionVerify.getPayment_amount().toString())) {

			logger.info(
					"Toshani Amount in real time transaction does not match with Amount in verification transaction for Amount real time = "
							+ transactionResponse.getPayment_amount().toString() + " Amount in Status = " + transactionVerify.getPayment_amount().toString());
			return false;
		}
		
		else if (!transactionResponse.getRrn().toString().equalsIgnoreCase(transactionVerify.getRrn().toString())) {

			logger.info(
					"Toshani RRN in real time transaction does not match with RRN in verification transaction for RRN real time = "
							+ transactionResponse.getRrn().toString() + " RRN in Status = " + transactionVerify.getRrn().toString());
			return false;
		}
		else {
			return true;
		}

	}
	
	private boolean verifyAmount(Fields fields, Transaction transactionResponse) throws SystemException {

		String amountFromFields = fields.get(FieldType.TOTAL_AMOUNT.getName());

		String amountInStatus = transactionResponse.getPayment_amount();
		amountInStatus = amountInStatus.replace(".", "");
		if (amountFromFields.equalsIgnoreCase(amountInStatus)) {

			logger.info(
					"Toshani Amount in real time transaction matches with Amount in verification transaction for Amount real time = "
							+ amountFromFields + " Amount in Status = " + amountInStatus);
			return true;
		} else {
			logger.info(
					"Toshani Amount in real time transaction does not match with Amount in verification transaction for Amount real time = "
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
						AcquirerType.getInstancefromCode(AcquirerType.TOSHANIDIGITAL.getCode()).getName())) {
					account = accountThis;
					break;
				}
			}
		}

		accountCurrency = account.getAccountCurrency(fields.get(FieldType.CURRENCY_CODE.getName()));
		return accountCurrency;

	}
}
