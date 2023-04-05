package com.paymentgateway.bindb.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.BinMapperType;
import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Shashi
 *
 */
@Service
@Scope(value="session", proxyMode=ScopedProxyMode.TARGET_CLASS)
public class ApiBinRangeProvider {

	@Autowired
	private BinRangeCommunicator binRangeCommunicator;

	@Autowired
	private BinRangeParser binRangeParser;

	@Autowired
	private BinRangeUpdater binRangeUpdater;
	
	public Map<String, String> getdefualtBinrange(String cardBin) throws IOException, ParseException {
		
		String requestUrl;
		Map<String, String> binMap = new HashMap<String, String>();
		requestUrl = (PropertiesManager.propertiesMap.get("CitrusBinRangeFinder"));
		StringBuilder response = binRangeCommunicator.getCommunicator(requestUrl, cardBin);
		JSONObject jObject = binRangeParser.getBinParser(response);
		binMap = parseYesBankResponse(binMap, jObject);
		//add bin to DB
		binRangeUpdater.addBinToDb(binMap, cardBin);
		return binMap;
	}

	// Using YesBank/Citrus BinRange API
	private Map<String, String> parseYesBankResponse(Map<String, String> binMap, JSONObject jObject) {
		Object cardScheme = jObject.get("cardscheme");
		cardScheme = BinMapperType.getCodeUsingName(cardScheme.toString().toUpperCase());
		if (null != cardScheme) {
			binMap.put(FieldType.MOP_TYPE.getName(), (String) cardScheme);
		}

		Object issuingBank = jObject.get("issuingbank");
		if (null != issuingBank) {
			binMap.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(), (String) issuingBank);
		}

		Object cardType = jObject.get("cardtype");
		cardType = BinMapperType.getCodeUsingName(cardType.toString());
		if (null != cardType) {
			binMap.put(FieldType.PAYMENT_TYPE.getName(), (String) cardType);
		}

		Object cardCountry = jObject.get("country");
		if (null != cardCountry) {
			binMap.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(), (String) cardCountry);
		}
		return binMap;
	}
}
