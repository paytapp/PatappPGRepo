package com.paymentgateway.digitalsolution;

import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.ProcessManager;
import com.paymentgateway.pg.core.util.Processor;
import com.paymentgateway.pg.core.util.QaicashUtil;

@Service
public class DigitalsolutionSaleResponseHandler {

	private static Logger logger = LoggerFactory.getLogger(DigitalsolutionSaleResponseHandler.class.getName());

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	@Qualifier("digitalsolutionTransactionConverter")
	private TransactionConverter transactionConverter;

	@Autowired
	private DigitalsolutionTransformer digitalsolutionTransformer;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	@Qualifier("digitalsolutionTransactionCommunicator")
	private TransactionCommunicator transactionCommunicator;

	public Map<String, String> process(Fields fields) throws SystemException {

		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getCode());
		Transaction transactionResponse = new Transaction();
		String response = fields.get(FieldType.DIGITALSOLUTION_RESPONSE_FIELD.getName());
		logger.info("Digitalsolution Response Fields >>> " + response);
		transactionResponse = toTransaction(response, fields);

		boolean isDuplicateResponse = false;
		boolean isAmountMatchResponse = false;

		isDuplicateResponse = fieldsDao.checkDigSolDupCapture(transactionResponse.getClint_ref_id(),
				fields.get(FieldType.PAY_ID.getName()));
		transactionResponse = verifyTransaction(fields, transactionResponse);
		isAmountMatchResponse = verifyAmount(fields, transactionResponse);
		
		if (!isAmountMatchResponse) {

			logger.warn("Digitalsolution amount in response did not match with amount in database");
			fields.put(FieldType.STATUS.getName(), StatusType.DENIED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SIGNATURE_MISMATCH.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SIGNATURE_MISMATCH.getResponseMessage());
		}

		else if (isDuplicateResponse) {

			logger.warn("Digitalsolution Response is duplicate, response pg ref is already present in database");
			fields.put(FieldType.STATUS.getName(), StatusType.DENIED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DUPLICATE.getResponseMessage());

		} else {
			digitalsolutionTransformer = new DigitalsolutionTransformer(transactionResponse);
			digitalsolutionTransformer.updateResponse(fields);
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

		JSONObject jsonReq = new JSONObject();

		jsonReq.put(Constants.token, fields.get(FieldType.MERCHANT_ID.getName()));
		jsonReq.put(Constants.clint_ref_id, fields.get(FieldType.PG_REF_NUM.getName()));

		String statusUrl = PropertiesManager.propertiesMap.get(Constants.DIGITALSOLUTION_STATUS_ENQUIRY_URL);

		String statusResponse = transactionCommunicator.statusEnqPostRequest(statusUrl.toString(),jsonReq.toString());
		Transaction transactionStatus = transactionConverter.toStatusTransaction(statusResponse);
		return transactionStatus;

	}

	private boolean verifyAmount(Fields fields, Transaction transactionResponse) throws SystemException {

		String amountFromFields = Amount.toDecimal(
				fields.get(FieldType.TOTAL_AMOUNT.getName()), fields.get(FieldType.CURRENCY_CODE.getName())).split("\\.")[0];
		String amountInStatus =  transactionResponse.getAmount().split("\\.")[0];
		
		if (amountFromFields.equalsIgnoreCase(amountInStatus)) {

			logger.info(
					"Qaicash Amount in real time transaction matches with Amount in verification transaction for Amount real time = "
							+ amountFromFields
							+ " Amount in Status = " + amountInStatus);
			return true;
		} else {
			logger.info(
					"Qaicash Amount in real time transaction does not match with Amount in verification transaction for Amount real time = "
							+ amountFromFields + " Amount in Status = "
							+ amountInStatus);
			return false;
		}

	}
}
