package com.paymentgateway.googlePay;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;
/**
 * @author vj
 *
 */

@Service("googlePayTransactionConverter")
public class TransactionConverter {
	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;

	public JSONObject authoriseRequest(Fields fields, String accessTokenResponse) throws SystemException {

		String mobileNo = fields.get(FieldType.PAYER_PHONE.getName());
		String googleMerchantId = fields.get(FieldType.ADF8.getName());
		String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());
		String currencyName = Currency.getAlphabaticCode(currencyCode);
		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		String strArray[] = amount.split(Constants.SPLIT);
		String units = strArray[0];
		String decValue = strArray[1];
		String nanos = getNano(decValue);

		JSONObject merchantInfo = new JSONObject();
		merchantInfo.put(Constants.GOOGLEMERCHANTID, googleMerchantId);

		JSONObject userInfo = new JSONObject();
		userInfo.put(Constants.MOBILENO, mobileNo);

		JSONObject merchantTransactionDetails = new JSONObject();
		merchantTransactionDetails.put(Constants.TXNID, fields.get(FieldType.PG_REF_NUM.getName()));
		merchantTransactionDetails.put(Constants.DESCRIPTION, Constants.SAMPLE_DESCRIPTION);

		JSONObject amountPayable = new JSONObject();
		amountPayable.put(Constants.CURRENCYCODE, currencyName);
		amountPayable.put(Constants.UNITS, units);
		amountPayable.put(Constants.NANOS, nanos);

		merchantTransactionDetails.put(Constants.AMOUNTPAYABLE, amountPayable);

		JSONObject json = new JSONObject();
		json.put(Constants.ORIGINATINGPLATFORM, Constants.DESKTOP);
		json.put(Constants.MERCHANTTXNDETAIL, merchantTransactionDetails);
		json.put(Constants.USERINFO, userInfo);
		json.put(Constants.MERCHANTINFO, merchantInfo);

		return json;

	}

	// converting  value after decimal to nanos
	private String getNano(String str) {
		int len = str.length();
		for (int i = 0; i < 9 - len; i++) {
			str += Constants.ZERO;
		}

		return str;
	}

}
