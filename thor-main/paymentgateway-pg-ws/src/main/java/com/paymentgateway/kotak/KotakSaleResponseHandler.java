package com.paymentgateway.kotak;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
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
public class KotakSaleResponseHandler {

	private static Logger logger = LoggerFactory.getLogger(KotakSaleResponseHandler.class.getName());

	@Autowired
	private Validator generalValidator;
	
	@Autowired
	private Historian historian;

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	private KotakTransformer kotakTransformer;

	public Map<String, String> process(Fields fields) throws SystemException {
		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		Transaction transactionResponse = new Transaction();
		String response = fields.get(FieldType.KOTAK_RESPONSE_FIELD.getName());
		boolean res = isHashMatching(response, fields);
		fields.remove(FieldType.KOTAK_RESPONSE_FIELD.getName());
		generalValidator.validate(fields);
		try {
			response = URLDecoder.decode(response, "UTF-8" );
		} catch (UnsupportedEncodingException exception) {
			logger.error("Exception " , exception);
		}
		if (res == true) {
			transactionResponse = toTransaction(response, transactionResponse);
			kotakTransformer = new KotakTransformer(transactionResponse);
			kotakTransformer.updateResponse(fields);
		} else {
			fields.put(FieldType.STATUS.getName(), StatusType.DENIED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SIGNATURE_MISMATCH.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SIGNATURE_MISMATCH.getResponseMessage());
		}
		historian.addPreviousSaleFields(fields);
		fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
		ProcessManager.flow(updateProcessor, fields, true);
		return fields.getFields();

	}

	public Transaction toTransaction(String response, Transaction transactionResponse) {
		Transaction transaction = new Transaction();
		String[] values = response.split("::");
		Map<String, String> receivedValues = new HashMap<>();
		for (String string : values) {
			String[] splitter = string.split(Constants.EQUATOR);
			receivedValues.put(splitter[0], splitter[1]);

		}
		String responseCode = receivedValues.get(Constants.RESPONSE_CODE);
		transaction.setResponseCode(responseCode);
		transaction.setAcqId(receivedValues.get(Constants.RET_REF_NO));
		if (responseCode.equals(Constants.RESPONSE_CODE_VALUE)) {
			transaction.setAuthCode(receivedValues.get(Constants.AUTH_CODE));
		}
		transaction.setHash(receivedValues.get(Constants.HASH));
		transaction.setMessage(receivedValues.get(Constants.MESSAGE));
		return transaction;
	}

	private boolean isHashMatching(String transactionResponse, Fields fields) throws SystemException {
		String[] ary = transactionResponse.split(Constants.SEPARATOR);
		Arrays.sort(ary);
		StringBuilder hashString = new StringBuilder();
		Map<String, String> myMap = new TreeMap<String, String>();

		for (int i = 0; i < ary.length; i++) {

			String key = ary[i].split(Constants.EQUATOR)[0];
			String value = ary[i].split(Constants.EQUATOR)[1];
			
			if(key.equalsIgnoreCase(Constants.MESSAGE)) {
				value = value.replace("+", " ");
			}
			try {
				value = URLDecoder.decode(value, "UTF-8" );
			} catch (UnsupportedEncodingException exception) {
				logger.error("Exception " , exception);
			}
			myMap.put(key, value);
		}
		hashString.append(fields.get(FieldType.PASSWORD.getName()));
		for (Map.Entry<String, String> param : myMap.entrySet()) {

			if (param.getKey().equalsIgnoreCase(Constants.HASH)) {
				continue;
			} else {

				hashString.append(param.getValue());
			}
		}

		String generateHash = null;
		String receivedHash = myMap.get(Constants.HASH);
		try {
			generateHash = Hasher.getHash(hashString.toString());
		} catch (SystemException exception) {
			logger.error("Exception", exception);
		}

		return receivedHash.equalsIgnoreCase(generateHash);
	}

	
}
