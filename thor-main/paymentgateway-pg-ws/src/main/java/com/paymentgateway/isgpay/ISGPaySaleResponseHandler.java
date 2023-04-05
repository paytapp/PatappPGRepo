package com.paymentgateway.isgpay;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.Validator;
import com.paymentgateway.pg.core.util.ProcessManager;
import com.paymentgateway.pg.core.util.Processor;
import com.paymentgateway.pg.history.Historian;

/**
 * @author Rahul
 *
 */

@Service
public class ISGPaySaleResponseHandler {

	private static Logger logger = LoggerFactory.getLogger(ISGPaySaleResponseHandler.class.getName());

	@Autowired
	private Validator generalValidator;

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	private ISGPayTransformer iSGPayTransformer;
	
	@Autowired
	private Historian historian;

	public Map<String, String> process(Fields fields) throws SystemException {

		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		generalValidator.validate(fields);
		Transaction transactionResponse = new Transaction();
		String isgPayResponse = fields.get(FieldType.ISGPAY_RESPONSE_FIELD.getName());
		transactionResponse = toTransaction(isgPayResponse);

		iSGPayTransformer = new ISGPayTransformer(transactionResponse);
		iSGPayTransformer.updateResponse(fields);

		historian.addPreviousSaleFields(fields);
		fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
		fields.remove(FieldType.ISGPAY_RESPONSE_FIELD.getName());
		ProcessManager.flow(updateProcessor, fields, true);
		return fields.getFields();

	}

	public Transaction toTransaction(String isgPayResponse) {

		Transaction transaction = new Transaction();

		String responseparamsArray[] = isgPayResponse.split(Pattern.quote("||"));

		Map<String, String> responseMap = new HashMap<String, String>();

		for (String item : responseparamsArray) {

			String itemsArray[] = item.split("=");
			String key = itemsArray[0];
			String value = itemsArray[1];
			responseMap.put(key, value);

		}
		
		logger.info("Final Bank Response for ISGPAY : " + isgPayResponse );

		if (StringUtils.isNotBlank(responseMap.get("TerminalId"))) {
			transaction.setTerminalId(responseMap.get("TerminalId"));
		}

		if (StringUtils.isNotBlank(responseMap.get("MerchantId"))) {
			transaction.setMerchantId(responseMap.get("MerchantId"));
		}

		if (StringUtils.isNotBlank(responseMap.get("BankId"))) {
			transaction.setBankId(responseMap.get("BankId"));
		}

		if (StringUtils.isNotBlank(responseMap.get("ResponseCode"))) {
			transaction.setResponseCode(responseMap.get("ResponseCode"));
		}

		if (StringUtils.isNotBlank(responseMap.get("Message"))) {
			transaction.setMessage(responseMap.get("Message"));
		}

		if (StringUtils.isNotBlank(responseMap.get("MCC"))) {
			transaction.setMcc(responseMap.get("MCC"));
		}

		if (StringUtils.isNotBlank(responseMap.get("pgTxnId"))) {
			transaction.setPgTxnId(responseMap.get("pgTxnId"));
		}

		if (StringUtils.isNotBlank(responseMap.get("TxnRefNo"))) {
			transaction.setTxnRefNo(responseMap.get("TxnRefNo"));
		}

		if (StringUtils.isNotBlank(responseMap.get("hashValidated"))) {
			transaction.setHashValidated(responseMap.get("hashValidated"));
		}

		if (StringUtils.isNotBlank(responseMap.get("TransactionType"))) {
			transaction.setIsgTransactionType(responseMap.get("TransactionType"));
		}

		if (StringUtils.isNotBlank(responseMap.get("ENROLLED"))) {
			transaction.setENROLLED(responseMap.get("ENROLLED"));
		}

		if (StringUtils.isNotBlank(responseMap.get("Stan"))) {
			transaction.setStan(responseMap.get("Stan"));
		}

		if (StringUtils.isNotBlank(responseMap.get("NewTransactionId"))) {
			transaction.setNewTransactionId(responseMap.get("NewTransactionId"));
		}

		if (StringUtils.isNotBlank(responseMap.get("TxnId"))) {
			transaction.setTxnId(responseMap.get("TxnId"));
		}

		if (StringUtils.isNotBlank(responseMap.get("UCAP"))) {
			transaction.setUCAP(responseMap.get("UCAP"));
		}

		if (StringUtils.isNotBlank(responseMap.get("MessageType"))) {
			transaction.setMessageType(responseMap.get("MessageType"));
		}

		if (StringUtils.isNotBlank(responseMap.get("TransactionDate"))) {
			transaction.setTransactionDate(responseMap.get("TransactionDate"));
		}

		if (StringUtils.isNotBlank(responseMap.get("AuthCode"))) {
			transaction.setAuthCode(responseMap.get("AuthCode"));
		}

		if (StringUtils.isNotBlank(responseMap.get("PosEntryMode"))) {
			transaction.setPosEntryMode(responseMap.get("PosEntryMode"));
		}

		if (StringUtils.isNotBlank(responseMap.get("RetRefNo"))) {
			transaction.setRetRefNo(responseMap.get("RetRefNo"));
		}

		if (StringUtils.isNotBlank(responseMap.get("TransactionTime"))) {
			transaction.setTransactionTime(responseMap.get("TransactionTime"));
		}

		if (StringUtils.isNotBlank(responseMap.get("AuthStatus"))) {
			transaction.setAuthStatus(responseMap.get("AuthStatus"));
		}

		if (StringUtils.isNotBlank(responseMap.get("ccAuthReply_processorResponse"))) {
			transaction.setCcAuthReply_processorResponse(responseMap.get("ccAuthReply_processorResponse"));
		}
		
		
		return transaction;
	}

}
