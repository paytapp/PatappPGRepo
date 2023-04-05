package com.paymentgateway.commons.util;

import java.math.BigDecimal;

import com.paymentgateway.commons.util.Constants;


public class Amount {
	public static final char DECIMAL = '.';
	
	public static String toDecimal(String amount, String currencyCode){				
		StringBuilder decimalAmount = new StringBuilder();
		
		int places = Currency.getNumberOfPlaces(currencyCode);
		decimalAmount.append(amount.substring(0, amount.length() - places));
		decimalAmount.append(DECIMAL);
		decimalAmount.append(amount.substring(amount.length() - places));
		
		return decimalAmount.toString();
	}
	
	public static String formatAmount(String amount, String currencyCode){
	
		final int places = Currency.getNumberOfPlaces(currencyCode);		
		BigDecimal amountValue = new BigDecimal(amount).setScale(places, BigDecimal.ROUND_HALF_UP);
		amount = amountValue.toString();
		amount = amount.replaceAll("\\.", Constants.BLANK_REPLACEMENT_STRING.getValue());
		return amount;	
	}
	
	public static String removeDecimalAmount(String amount, String currencyCode){
		
		final int places = Currency.getNumberOfPlaces(currencyCode);		
		BigDecimal amountValue = new BigDecimal(amount).setScale(places, BigDecimal.ROUND_HALF_UP);
		amount = amountValue.toString();
		amount = amount.replaceAll("\\.", Constants.BLANK_REPLACEMENT_STRING.getValue());
		amount = amount.substring(0, amount.length() - places);
		return amount;	
	}
	
}
