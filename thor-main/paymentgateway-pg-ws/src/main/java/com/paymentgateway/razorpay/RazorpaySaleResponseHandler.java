package com.paymentgateway.razorpay;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.Validator;
import com.paymentgateway.pg.core.util.ProcessManager;
import com.paymentgateway.pg.core.util.Processor;
import com.paymentgateway.pg.core.util.RazorpayUtils;

@Service
public final class RazorpaySaleResponseHandler {
	private static Logger logger = LoggerFactory.getLogger(RazorpaySaleResponseHandler.class.getName());

	@Autowired
	private Validator generalValidator;

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	private RazorpayTransformer razorpayTransformer;

	@Autowired
	private RazorpayUtils razorpayUtils;

	@Autowired
	private FieldsDao fieldsDao;

	public Map<String, String> process(Fields fields) throws SystemException {

		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		generalValidator.validate(fields);
		String response = fields.get(FieldType.RAZORPAY_RESPONSE_FIELD.getName());

		Transaction transactionResponse = toTransaction(response, fields);

		// Validate Response Signature

		boolean isSignatureMatch = false;
		boolean isDuplicateResponse = false;
		
		if (StringUtils.isNotBlank(transactionResponse.getRazorpay_payment_id())) {

			JSONObject attributes = new JSONObject();
			attributes.put("razorpay_signature", transactionResponse.getRazorpay_signature());
			attributes.put("razorpay_order_id", transactionResponse.getRazorpay_order_id());
			attributes.put("razorpay_payment_id", transactionResponse.getRazorpay_payment_id());

			isSignatureMatch = razorpayUtils.verifyPaymentSignature(attributes,
					fields.get(FieldType.TXN_KEY.getName()));
			
			// Verify Duplicate Response with Razorpay Payment Id and Order Id in ACQ_ID and RRN respectively
			
			isDuplicateResponse = fieldsDao.checkRazorpayDuplicateCapture(transactionResponse.getRazorpay_payment_id(),
					transactionResponse.getRazorpay_order_id());
		}


		if (!isSignatureMatch) {

			logger.warn("Razorpay Response signature not matching with calculated signature");
			fields.put(FieldType.STATUS.getName(), StatusType.DENIED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SIGNATURE_MISMATCH.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SIGNATURE_MISMATCH.getResponseMessage());
		}

		else if (isDuplicateResponse) {

			logger.warn("Razorpay Response is duplicate, response pg ref is already present in database");
			fields.put(FieldType.STATUS.getName(), StatusType.DENIED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DUPLICATE.getResponseMessage());

		} else {
			razorpayTransformer.updateResponse(fields, transactionResponse);
		}

		fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
		fields.remove(FieldType.RAZORPAY_RESPONSE_FIELD.getName());
		ProcessManager.flow(updateProcessor, fields, true);
		return fields.getFields();

	}

	public Transaction toTransaction(String response, Fields fields) {

		Transaction transaction = new Transaction();

		if (StringUtils.isBlank(response)) {
			logger.warn("Empty response received from RazorPay");
			return transaction;
		}

		String respArray[] = response.split("&&");

		// Success
		if (response.contains("razorpay_payment_id")) {
			transaction.setRazorpay_payment_id(respArray[0].split("=")[1]);
			transaction.setRazorpay_order_id(respArray[1].split("=")[1]);
			transaction.setRazorpay_signature(respArray[2].split("=")[1]);
		}

		// Error
		if (response.contains("error_code")) {
			transaction.setError_code(respArray[0].split("=")[1]);
			transaction.setError_description(respArray[1].split("=")[1]);
			transaction.setError_source(respArray[2].split("=")[1]);
			transaction.setError_step(respArray[3].split("=")[1]);
			transaction.setError_reason(respArray[4].split("=")[1]);

		}

		return transaction;
	}

}
