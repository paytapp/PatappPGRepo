package com.paymentgateway.payphi;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.Validator;
import com.paymentgateway.pg.core.util.PayphiUtil;
import com.paymentgateway.pg.core.util.ProcessManager;
import com.paymentgateway.pg.core.util.Processor;
import com.paymentgateway.pg.history.Historian;

/**
 * @author Shaiwal
 *
 */

@Service
public class PayphiSaleResponseHandler {

	private static Logger logger = LoggerFactory.getLogger(PayphiSaleResponseHandler.class.getName());

	@Autowired
	private Validator generalValidator;

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	private PayphiTransformer payphiTransformer;

	@Autowired
	private PayphiUtil payphiUtil;

	@Autowired
	private Historian historian;

	@Autowired
	private PayphiStatusEnquiryProcessor payphiStatusEnquiryProcessor;

	public Map<String, String> process(Fields fields) throws SystemException {

		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		generalValidator.validate(fields);
		Transaction transactionResponse = new Transaction();
		String payphiResponse = fields.get(FieldType.PAYPHI_RESPONSE_FIELD.getName());
		transactionResponse = toTransaction(payphiResponse);

		payphiTransformer = new PayphiTransformer(transactionResponse);
		payphiTransformer.updateResponse(fields);

		// Double verification for captured transaction
		boolean isHashMatch = doubleVerification(fields, transactionResponse);
		
		// Re-verify Status for Failed Transactions 
		if (!fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {
			statusEnquiry(fields);
		}
		
		
		if (!isHashMatch) {

			fields.put(FieldType.STATUS.getName(), StatusType.DECLINED.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Bank Hash does not match calculated hash");
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DECLINED.getCode());
		}
		historian.addPreviousSaleFields(fields);
		fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
		fields.remove(FieldType.PAYPHI_RESPONSE_FIELD.getName());
		ProcessManager.flow(updateProcessor, fields, true);
		return fields.getFields();

	}

	public Transaction toTransaction(String payphiResponse) {

		Transaction transaction = new Transaction();

		String responseparamsArray[] = payphiResponse.split("&&");

		Map<String, String> responseMap = new HashMap<String, String>();

		for (String item : responseparamsArray) {

			String itemsArray[] = item.split("=");
			String key = itemsArray[0];
			String value = itemsArray[1];
			responseMap.put(key, value);

		}

		logger.info("Final Bank Response for PayPhi : " + payphiResponse);

		if (StringUtils.isNotBlank(responseMap.get("responseCode"))) {
			transaction.setResponseCode(responseMap.get("responseCode"));
		}

		if (StringUtils.isNotBlank(responseMap.get("respDescription"))) {
			transaction.setRespDescription(responseMap.get("respDescription"));
		}

		if (StringUtils.isNotBlank(responseMap.get("merchantTxnNo"))) {
			transaction.setMerchantTxnNo(responseMap.get("merchantTxnNo"));
		}

		if (StringUtils.isNotBlank(responseMap.get("txnID"))) {
			transaction.setTxnID(responseMap.get("txnID"));
		}

		if (StringUtils.isNotBlank(responseMap.get("txnAuthID"))) {
			transaction.setTxnAuthID(responseMap.get("txnAuthID"));
		}

		if (StringUtils.isNotBlank(responseMap.get("paymentID"))) {
			transaction.setPaymentID(responseMap.get("paymentID"));
		}

		return transaction;
	}

	public boolean doubleVerification(Fields fields, Transaction transaction) {

		try {
			
			if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {
				return true;
			}

			String payphiResponseSplit[] = fields.get(FieldType.PAYPHI_RESPONSE_FIELD.getName()).split("&&");
			Map<String, String> hmReqFields = new TreeMap<String, String>();
			String bankHash = null;

			for (String param : payphiResponseSplit) {
				String paramSplit[] = param.split("=");

				if (paramSplit[0].equalsIgnoreCase("secureHash")) {
					bankHash = paramSplit[1];
				} else {
					hmReqFields.put(paramSplit[0], paramSplit[1]);
				}
			}

			StringBuilder sb = new StringBuilder();

			for (Entry<String, String> entry : hmReqFields.entrySet()) {
				sb.append(entry.getValue());
			}

			String calculatedHash = payphiUtil.generateHash(sb.toString(), fields.get(FieldType.TXN_KEY.getName()));

			logger.info("Bank hash for Order Id = " + fields.get(FieldType.ORDER_ID.getName()) + "  >>>  " + bankHash);
			logger.info("Calculated hash for Order Id = " + fields.get(FieldType.ORDER_ID.getName()) + "  >>>  "
					+ calculatedHash);

			if (bankHash.equalsIgnoreCase(calculatedHash)) {

				logger.info("Bank Hash Matches with Calculated hash");
				return true;
			} else {

				logger.info("Bank Hash does not match with Calculated hash");
				return false;
			}
		} catch (Exception e) {

			logger.error("Exception in double verification for PG REF NUM == " +fields.get(FieldType.PG_REF_NUM.getName()) ,e );
		}

		return false;

	}

	public boolean statusEnquiry(Fields fields) {

		try {
			logger.info("Before Double enquiry , Status for PG REF NUM = " + fields.get(FieldType.PG_REF_NUM.getName()) + " == "
					+ fields.get(FieldType.STATUS.getName()));
			
			payphiStatusEnquiryProcessor.enquiryProcessor(fields);
			
			logger.info("After Double enquiry , Status for PG REF NUM = " + fields.get(FieldType.PG_REF_NUM.getName()) + " == "
					+ fields.get(FieldType.STATUS.getName()));

		} catch (Exception e) {
			logger.error(
					"Exception in payphi Double enquiry for PG REF NUM == " + fields.get(FieldType.PG_REF_NUM.getName()),e);
		}

		return false;

	}

}
