package com.paymentgateway.commons.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.User;
@Embeddable
public class Currency {
	private String code;
	private int places;
	private static final String alphabaticFileName= "alphabatic-currencycode.properties";
	private static Map<String, Integer> currencies = loadCurrencies();
	private static final String currencyPrefix= "CURRENCY_";
	private static Logger logger = LoggerFactory.getLogger(Currency.class.getName());

	public Currency() {

	}

	public Currency(String code, int places) {
		this.code = code;
		this.places = places;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getPlaces() {
		return places;
	}

	public void setPlaces(int places) {
		this.places = places;
	}

	public static void validateCurrency(String currencyCode) throws SystemException {

		if (null == currencyCode) {
			return;
		}

		if (!currencies.containsKey(currencyCode)) {
			throw new SystemException(ErrorType.VALIDATION_FAILED, "Invalid "
					+ FieldType.CURRENCY_CODE.getName());
		}
	}

	public static Map<String, Integer> loadCurrencies() {
		Map<String, String> allCurrencies = (new PropertiesManager())
				.getAllProperties(PropertiesManager.getCurrencyfile());
		Map<String, Integer> currenciesMap = new HashMap<String, Integer>();

		for (String currencyCode : allCurrencies.keySet()) {
			int numberOfPlaces = Integer.parseInt(allCurrencies
					.get(currencyCode));
			currenciesMap.put(currencyCode, numberOfPlaces);
		}

		return currenciesMap;
	}// loadCurrencies()

	public static int getNumberOfPlaces(String currencyCode) {
		if (StringUtils.isNotBlank(currencyCode) && currencyCode.equalsIgnoreCase("356")) {
			return 2;
		}
		else {
			return 2;
		}
	}

	public static Currency getDefaultCurrency() {
		String currCode = ConfigurationConstants.DEFAULT_CURRENCY.getValue();
		return new Currency(currCode, getNumberOfPlaces(currCode));
	}
	
	public static String getAlphabaticCode(String numericCurrencyCode) {
		return PropertiesManager.propertiesMap.get(currencyPrefix+numericCurrencyCode);
	}

	public static String getNumericCode(String alphabaticCurrencyCode) {
		return PropertiesManager.propertiesMap.get(alphabaticCurrencyCode);
	}
	 
	public static Map<String,String> getSupportedCurreny(User user){
		Map<String,String> currencyMap = new HashMap<String,String>();
		
		for(Account account: user.getAccounts()){
			Set<AccountCurrency> AccountCurrencySet = account.getAccountCurrencySet();
			for(AccountCurrency accountCurrency:AccountCurrencySet){
				String currencyCode = accountCurrency.getCurrencyCode();
				currencyMap.put(currencyCode, Currency.getAlphabaticCode(currencyCode));
			}
		}
		return currencyMap;
	}
	
	public static Map<String,String> getAllCurrency(){
		PropertiesManager propertiesManager= new PropertiesManager();
		Map<String,String> allCurrencyMap;
		allCurrencyMap = propertiesManager.getAllProperties(alphabaticFileName);
		return allCurrencyMap;
	}
}
