package com.paymentgateway.floxypay;

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
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.ProcessManager;
import com.paymentgateway.pg.core.util.Processor;

@Service
public class FloxypaySaleResponseHandler {

	private static Logger logger = LoggerFactory.getLogger(FloxypaySaleResponseHandler.class.getName());

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	@Qualifier("floxypayTransactionConverter")
	private TransactionConverter transactionConverter;

	@Autowired
	private FloxypayTransformer floxypayTransformer;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	@Qualifier("floxypayTransactionCommunicator")
	private TransactionCommunicator transactionCommunicator;

	public Map<String, String> process(Fields fields) throws SystemException {

		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getCode());
		Transaction transactionResponse = new Transaction();
		String response = fields.get(FieldType.FLOXYPAY_RESPONSE_FIELD.getName());
		logger.info("Floxypay Response Fields >>> " + response);
		transactionResponse = toTransaction(response);

		boolean isDuplicateResponse = false;
		boolean isVerificationMatch = false;

		isDuplicateResponse = fieldsDao.checkFloxypayDuplicateCapture(transactionResponse.getSystemid(),
				fields.get(FieldType.PAY_ID.getName()));
		isVerificationMatch = verifyTransaction(fields, transactionResponse);
		if (!isVerificationMatch) {

			logger.warn("Floxypay double verification response did not match");
			fields.put(FieldType.STATUS.getName(), StatusType.DENIED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SIGNATURE_MISMATCH.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SIGNATURE_MISMATCH.getResponseMessage());
		}

		else if (isDuplicateResponse) {

			logger.warn("Floxypay Response is duplicate, response pg ref is already present in database");
			fields.put(FieldType.STATUS.getName(), StatusType.DENIED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DUPLICATE.getResponseMessage());

		} else {
			floxypayTransformer = new FloxypayTransformer(transactionResponse);
			floxypayTransformer.updateResponse(fields);
		}

		fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
		ProcessManager.flow(updateProcessor, fields, true);
		return fields.getFields();

	}

	public Transaction toTransaction(String response) {
		Transaction transaction = new Transaction();
		transaction = transactionConverter.toTransaction(response);
		return transaction;
	}

	private boolean verifyTransaction(Fields fields, Transaction transactionResponse) throws SystemException {

		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put("order_id", fields.get(FieldType.PG_REF_NUM.getName()));

		Transaction transactionStatus = new Transaction();
		transactionStatus.setXkey(fields.get(FieldType.MERCHANT_ID.getName()));
		transactionStatus.setXsecret(fields.get(FieldType.TXN_KEY.getName()));

		String statusResponse = transactionCommunicator.statusEnqPostRequest(jsonRequest.toString(), transactionStatus);
		transactionStatus = transactionConverter.toStatusTransaction(statusResponse);

		if (transactionResponse.getStatus().equalsIgnoreCase(transactionStatus.getStatus())) {
			logger.info(
					"Status in real time transaction matches with status in verification transaction for Pg Ref Num = "
							+ fields.get(FieldType.PG_REF_NUM.getName()) + " Status = "
							+ transactionStatus.getStatus());

			if (transactionResponse.getAmount().equalsIgnoreCase(Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()), fields.get(FieldType.CURRENCY_CODE.getName())))) {

				logger.info(
						"Amount in real time transaction matches with Amount in verification transaction for Amount real time = "
								+ Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()), fields.get(FieldType.CURRENCY_CODE.getName())) + " Amount in Status = "
								+ transactionStatus.getAmount());
				return true;
			} else {
				logger.info(
						"Amount in real time transaction does not match with Amount in verification transaction for Amount real time = "
								+ fields.get(FieldType.TOTAL_AMOUNT.getName()) + " Amount in Status = "
								+ transactionStatus.getAmount());
				return false;
			}

		} else {
			logger.info(
					"Status in real time transaction does not match with status in verification transaction for Pg Ref Num = "
							+ fields.get(FieldType.PG_REF_NUM.getName()) + " Status in realtime = "
							+ transactionResponse.getStatus() + " Status in enquiry = "
							+ transactionStatus.getStatus());
			return false;
		}
	}
}
