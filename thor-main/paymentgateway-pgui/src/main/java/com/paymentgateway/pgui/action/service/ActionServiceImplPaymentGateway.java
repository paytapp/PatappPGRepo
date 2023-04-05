package com.paymentgateway.pgui.action.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pg.core.util.ConstantsPaymentGateway;
import com.paymentgateway.pg.core.util.MerchantPaymentGatewayUtil;

@Service
public class ActionServiceImplPaymentGateway implements ActionServicePaymentGateway {
	private static Logger logger = LoggerFactory.getLogger(ActionServiceImpl.class.getName());

	@Override
	public Fields prepareFieldspg(Map<String, String[]> map) throws SystemException {
		String decryptRequest = null;

		Fields fields = new Fields();
		
		for(Entry<String,String[]> entry:map.entrySet()) {
			logger.info("Parameter received from Payment Gateway key:" + entry.getKey() + " and value: " + Arrays.toString(entry.getValue()));
		}
		
		// get request string
		String request = map.get("encdata")[0];
		
		// String decryptRequest;
		try {
				decryptRequest = MerchantPaymentGatewayUtil.decryptPaymentGateway(request,
						PropertiesManager.propertiesMap.get(ConstantsPaymentGateway.PAYMENT_GATEWAY_KEY),
						PropertiesManager.propertiesMap.get(ConstantsPaymentGateway.PAYMENT_GATEWAY_IV));
				isCheckSumMatching(decryptRequest);
				mapFields(decryptRequest, fields);
			} catch (Exception exception) {
				
				logger.error("Exception", exception);
			}

		fields.removeInternalFields();
		fields.remove(FieldType.HASH.getName());
		fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

		return fields;
	}

	private boolean isCheckSumMatching(String decryptRequest) throws SystemException {
		String receivedChecksum = decryptRequest.substring(decryptRequest.lastIndexOf(Constants.EQUATOR) + 1);
		String checksumString = decryptRequest.substring(0, decryptRequest.lastIndexOf("|"));
		String checksum = Hasher.getHash(checksumString);
		return receivedChecksum.equalsIgnoreCase(checksum);
	}

	private void mapFields(String decryptRequest, Fields fields) {
		String[] values = decryptRequest.split("\\|");
		if (values.length == 10) {
			Map<String, String> receivedValues = new HashMap<>();
			for (String string : values) {
				String[] splitter = string.split(Constants.EQUATOR);
				String value = string.substring(string.indexOf(Constants.EQUATOR)+1,string.length());
				receivedValues.put(splitter[0], value);
			}
			String currenyCode = Currency.getNumericCode(receivedValues.get(Constants.CURRENCY_TYPE));

			fields.put(FieldType.PAY_ID.getName(), receivedValues.get(Constants.MERCHANT_CODE));
			fields.put(FieldType.AMOUNT.getName(),
					Amount.formatAmount(receivedValues.get(Constants.TXN_AMOUNT), currenyCode));
			fields.put(FieldType.ORDER_ID.getName(), receivedValues.get(Constants.RESERVATION_ID));
			fields.put(FieldType.CURRENCY_CODE.getName(), currenyCode);
			fields.put(FieldType.RETURN_URL.getName(), receivedValues.get(Constants.RESPONSE_URL));
		} else {
			logger.error("Response message has more/less parameters than expected: " + decryptRequest);
		}
	}
}
