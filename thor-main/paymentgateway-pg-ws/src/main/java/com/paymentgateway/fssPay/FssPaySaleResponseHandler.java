package com.paymentgateway.fssPay;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
import com.paymentgateway.pg.history.Historian;

@Service
public class FssPaySaleResponseHandler {

	private static Logger logger = LoggerFactory.getLogger(FssPaySaleResponseHandler.class.getName());

	@Autowired
	private Validator generalValidator;

	@Autowired
	private Historian historian;

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	private FssPayTransformer fssPayTransformer;

	@Autowired
	private FieldsDao fieldsDao;

	public static final String RESULT_OPEN_TAG = "<result>";
	public static final String RESULT_CLOSE_TAG = "</result>";
	public static final String ERROR_TEXT_OPEN_TAG = "<error_text>";
	public static final String ERROR_TEXT_CLOSE_TAG = "</error_text>";
	public static final String PAYMENT_ID_OPEN_TAG = "<paymentid>";
	public static final String PAYMENT_ID_CLOSE_TAG = "</paymentid>";
	public static final String AUTH_OPEN_TAG = "<auth>";
	public static final String AUTH_CLOSE_TAG = "</auth>";
	public static final String REF_OPEN_TAG = "<ref>";
	public static final String REF_CLOSE_TAG = "</ref>";
	public static final String AVR_OPEN_TAG = "<avr>";
	public static final String AVR_CLOSE_TAG = "</avr>";
	public static final String TRANID_OPEN_TAG = "<tranid>";
	public static final String TRANID_CLOSE_TAG = "</tranid>";
	public static final String ERROR_CODE_OPEN_TAG = "<error_code_tag>";
	public static final String ERROR_CODE_CLOSE_TAG = "</error_code_tag>";
	public static final String ERROR_SERVICE_OPEN_TAG = "<error_service_tag>";
	public static final String ERROR_SERVICE_CLOSE_TAG = "</error_service_tag>";
	public static final String AMOUNT_OPEN_TAG = "<amt>";
	public static final String AMOUNT_CLOSE_TAG = "</amt>";
	public static final String TRACKID_OPEN_TAG = "<trackid>";
	public static final String TRACKID_CLOSE_TAG = "</trackid>";
	public static final String PAY_ID_OPEN_TAG = "<payid>";
	public static final String PAY_ID_CLOSE_TAG = "</payid>";

	public Map<String, String> process(Fields fields) throws SystemException {
		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		generalValidator.validate(fields);
		Transaction transactionResponse = new Transaction();
		String xmlResponse = fields.get(FieldType.FSS_PAY_RESPONSE_FIELD.getName());
		transactionResponse = toTransaction(xmlResponse);
		if ((StringUtils.isNotBlank(transactionResponse.getResult()))
				&& ((transactionResponse.getResult()).equalsIgnoreCase("SUCCESS"))) {
			boolean response = fieldsDao.validatePgRefNum(transactionResponse.getTrackId(),
					transactionResponse.getTranId());
			if (response) {
				fields.put(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DENIED_BY_FRAUD.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DENIED_BY_FRAUD.getInternalMessage());
			} else {

				fssPayTransformer = new FssPayTransformer(transactionResponse);
				fssPayTransformer.updateResponse(fields);
			}
		}
		
		else{
			
			fssPayTransformer = new FssPayTransformer(transactionResponse);
			fssPayTransformer.updateResponse(fields);
		}
		
		
		historian.addPreviousSaleFields(fields);
		fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
		fields.remove(FieldType.FSS_PAY_RESPONSE_FIELD.getName());
		ProcessManager.flow(updateProcessor, fields, true);
		return fields.getFields();

	}

	public Transaction toTransaction(String xml) {

		Transaction transaction = new Transaction();

		String result = getTextBetweenTags(xml, RESULT_OPEN_TAG, RESULT_CLOSE_TAG);
		if (StringUtils.isNotBlank(result)) {
			transaction.setResult(result);
		} else {
			String errorCode = getTextBetweenTags(xml, ERROR_CODE_OPEN_TAG, ERROR_CODE_CLOSE_TAG);
			if (errorCode.contains("IPAY")) {
				String bankCode = errorCode.substring(0, 11);
				transaction.setResult(bankCode);
			} else {
				transaction.setResult("FSS0100001");
			}

		}

		transaction.setError_code_tag(getTextBetweenTags(xml, ERROR_CODE_OPEN_TAG, ERROR_CODE_CLOSE_TAG));
		transaction.setError_service_tag(getTextBetweenTags(xml, ERROR_SERVICE_OPEN_TAG, ERROR_SERVICE_CLOSE_TAG));
		transaction.setError_text(getTextBetweenTags(xml, ERROR_TEXT_OPEN_TAG, ERROR_TEXT_CLOSE_TAG));
		transaction.setPaymentid(getTextBetweenTags(xml, PAYMENT_ID_OPEN_TAG, PAYMENT_ID_CLOSE_TAG));
		transaction.setAuth(getTextBetweenTags(xml, AUTH_OPEN_TAG, AUTH_CLOSE_TAG));
		transaction.setRef(getTextBetweenTags(xml, REF_OPEN_TAG, REF_CLOSE_TAG));
		transaction.setAvr(getTextBetweenTags(xml, AVR_OPEN_TAG, AVR_CLOSE_TAG));
		transaction.setTranId(getTextBetweenTags(xml, TRANID_OPEN_TAG, TRANID_CLOSE_TAG));
		transaction.setAmount(getTextBetweenTags(xml, AMOUNT_OPEN_TAG, AMOUNT_CLOSE_TAG));
		transaction.setPayId(getTextBetweenTags(xml, PAY_ID_OPEN_TAG, PAY_ID_CLOSE_TAG));
		transaction.setTrackId(getTextBetweenTags(xml, TRACKID_OPEN_TAG, TRACKID_CLOSE_TAG));
		return transaction;
	}// toTransaction()

	public String getTextBetweenTags(String text, String tag1, String tag2) {

		int leftIndex = text.indexOf(tag1);
		if (leftIndex == -1) {
			return null;
		}

		int rightIndex = text.indexOf(tag2);
		if (rightIndex != -1) {
			leftIndex = leftIndex + tag1.length();
			return text.substring(leftIndex, rightIndex);
		}

		return null;
	}// getTextBetweenTags()

}
