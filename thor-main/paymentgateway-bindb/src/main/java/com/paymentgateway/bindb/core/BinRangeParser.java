package com.paymentgateway.bindb.core;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.BinRange;
import com.paymentgateway.commons.util.FieldType;

@Service
@Scope(value="session", proxyMode=ScopedProxyMode.TARGET_CLASS)
public class BinRangeParser {

	public JSONObject getBinParser(StringBuilder response) throws ParseException {
		Object obj;
		JSONParser parser = new JSONParser();
		obj = parser.parse(response.toString());
		JSONObject jObject = (JSONObject) obj;
		return jObject;
	}

	public Map<String, String> parseToMap(BinRange binRange) {
		Map<String, String> binMap = new HashMap<String, String>();

		binMap.put(FieldType.MOP_TYPE.getName(), binRange.getMopType().getCode());
		binMap.put(FieldType.PAYMENT_TYPE.getName(), binRange.getCardType().getCode());
		binMap.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(), binRange.getIssuerBankName());
		binMap.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(), binRange.getIssuerCountry());
		binMap.put(FieldType.PAYMENTS_REGION.getName(), binRange.getRfu1());
		binMap.put(FieldType.CARD_HOLDER_TYPE.getName(), binRange.getRfu2());
		
		return binMap;
	}
}
