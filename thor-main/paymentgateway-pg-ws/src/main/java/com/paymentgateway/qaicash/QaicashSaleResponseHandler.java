package com.paymentgateway.qaicash;

import java.util.Map;

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
public class QaicashSaleResponseHandler {

	private static Logger logger = LoggerFactory.getLogger(QaicashSaleResponseHandler.class.getName());

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	@Qualifier("qaicashTransactionConverter")
	private TransactionConverter transactionConverter;

	@Autowired
	private QaicashTransformer qaicashTransformer;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private QaicashUtil qaicashUtil;

	@Autowired
	@Qualifier("qaicashTransactionCommunicator")
	private TransactionCommunicator transactionCommunicator;

	public Map<String, String> process(Fields fields) throws SystemException {

		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getCode());
		Transaction transactionResponse = new Transaction();
		String response = fields.get(FieldType.QAICASH_RESPONSE_FIELD.getName());
		logger.info("QAICASH Response Fields >>> " + response);
		transactionResponse = toTransaction(response, fields);

		boolean isDuplicateResponse = false;
		boolean isAmountMatchResponse = false;

		isDuplicateResponse = fieldsDao.checkQaicashDuplicateCapture(transactionResponse.getOrderId(),
				fields.get(FieldType.PAY_ID.getName()));
		transactionResponse = verifyTransaction(fields, transactionResponse);
		isAmountMatchResponse = verifyAmount(fields, transactionResponse);
		
		if (!isAmountMatchResponse) {

			logger.warn("Qaicash amount in response did not match with amount in database");
			fields.put(FieldType.STATUS.getName(), StatusType.DENIED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SIGNATURE_MISMATCH.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SIGNATURE_MISMATCH.getResponseMessage());
		}

		else if (isDuplicateResponse) {

			logger.warn("Qaicash Response is duplicate, response pg ref is already present in database");
			fields.put(FieldType.STATUS.getName(), StatusType.DENIED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DUPLICATE.getResponseMessage());

		} else {
			qaicashTransformer = new QaicashTransformer(transactionResponse);
			qaicashTransformer.updateResponse(fields);
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

		StringBuilder hmacString = new StringBuilder();
		hmacString.append(transactionResponse.getMerchantId());
		hmacString.append("|");
		hmacString.append(transactionResponse.getOrderId());

		String hmac = qaicashUtil.HMAC_SHA256(transactionResponse.getMerchantApiKey(), hmacString.toString());

		StringBuilder statusUrl = new StringBuilder();
		statusUrl.append(PropertiesManager.propertiesMap.get(Constants.QAICASH_STATUS_ENQUIRY_URL));
		statusUrl.append(transactionResponse.getMerchantId());
		statusUrl.append("/deposit/");
		statusUrl.append(transactionResponse.getOrderId());
		statusUrl.append("/mac/");
		statusUrl.append(hmac);

		String statusResponse = transactionCommunicator.statusEnqPostRequest(statusUrl.toString());
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
