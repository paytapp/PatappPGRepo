package com.paymentgateway.ipint;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;

@Service("ipintTransactionConverter")
public class TransactionConverter {

	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;

	@SuppressWarnings("incomplete-switch")
	public String perpareRequest(Fields fields, Transaction transaction) throws SystemException {

		String request = null;

		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case AUTHORISE:
		case ENROLL:
			break;
		case SALE:
			request = saleRequest(fields);
			break;
		case CAPTURE:
			break;
		case STATUS:
			break;
		}

		return request;

	}

	public String saleRequest(Fields fields) throws SystemException {

		String responseURL = PropertiesManager.propertiesMap.get(Constants.RESPONSE_URL);
		responseURL = responseURL + "?pgrefNum=" + fields.get(FieldType.PG_REF_NUM.getName());
		// String callbackURL =
		// PropertiesManager.propertiesMap.get(Constants.CALLBACK_URL);
		String amount = acquirerTxnAmountProvider.amountProvider(fields);

		JSONObject json = new JSONObject();
		json.put(Constants.CLIENT_EMAIL_ID, fields.get(FieldType.CUST_EMAIL.getName()));
		json.put(Constants.CLIENT_CURRENCY, Currency.getAlphabaticCode(fields.get(FieldType.CURRENCY_CODE.getName())));
		json.put(Constants.AMOUNT, amount);
		json.put(Constants.MERCHANT_ID, "");
		json.put(Constants.MERCHANT_WEBSITE, responseURL);
		// json.put(Constants.MERCHANT_WEBSITE, callbackURL);
		return json.toString();

	}

	public Transaction toTransaction(String response, Fields fields) {

		Transaction transaction = new Transaction();
		if (StringUtils.isNotBlank(response)) {
			JSONObject jsonResponse = new JSONObject(response);

			if (jsonResponse.has("session_id") && !jsonResponse.isNull("session_id")) {
				transaction.setSessionId(jsonResponse.get("session_id").toString());
			}
		}

		return transaction;

	}// toTransaction()


	public Transaction saleResponseErrorCheck(String jsonResponse) {
		Transaction transaction = new Transaction();
		if (StringUtils.isBlank(jsonResponse)) {
			return transaction;
		}
		JSONObject respObj = new JSONObject(jsonResponse);
		if (respObj.has("error")) {
				transaction.setResultMsg(respObj.getString("message"));
		}
		
		return transaction;

	}
}
