package com.paymentgateway.bindb.core;

public class BinLookupConverter {
	
	public static final String MERCHANT_REFERENCE_OPEN_TAG = "<c:merchantReferenceCode>";
	public static final String MERCHANT_REFERENCE_CLOSE_TAG = "</c:merchantReferenceCode>";
	public static final String REQUEST_ID_OPEN_TAG = "<c:requestID>";
	public static final String REQUEST_ID_CLOSE_TAG = "</c:requestID>";
	public static final String DECISION_OPEN_TAG = "<c:decision>";
	public static final String DECISION_CLOSE_TAG = "</c:decision>";
	public static final String REASON_OPEN_TAG = "<c:reasonCode>";
	public static final String REASON_CLOSE_TAG = "</c:reasonCode>";
	public static final String REQUEST_TOKEN_OPEN_TAG = "<c:requestToken>";
	public static final String REQUEST_TOKEN_CLOSE_TAG = "</c:requestToken>";
	
	
	public static final String REQUEST_DATE_TIME_OPEN_TAG = "<c:requestDateTime>";
	public static final String REQUEST_DATE_TIME_CLOSE_TAG = "</c:requestDateTime>";
	
	
	public static final String REQUEST_CARD_TYPE_OPEN_TAG = "<c:cardType>";
	public static final String REQUEST_CARD_TYPE_CLOSE_TAG = "</c:cardType>";
	
	
	public static final String REQUEST_CARD_TYPE_NAME_OPEN_TAG = "<c:cardTypeName>";
	public static final String REQUEST_CARD_TYPE_NAME_CLOSE_TAG = "</c:cardTypeName>";

	public static final String REQUEST_CARD_SUB_TYPE_OPEN_TAG = "<c:cardSubType>";
	public static final String REQUEST_CARD_SUB_TYPE_CLOSE_TAG = "</c:cardSubType>";
	

	public static final String REQUEST_PRODUCTION_CATEGORY_OPEN_TAG = "<c:productCategory>";
	public static final String REQUEST_PRODUCTION_CATEGORY_CLOSE_TAG = "</c:productCategory>";
	
	public static final String REQUEST_NAME_OPEN_TAG = "<c:name>";
	public static final String REQUEST_NAME_CLOSE_TAG = "</c:name>";
	
	public static final String REQUEST_COUNTRY_OPEN_TAG = "<c:country>";
	public static final String REQUEST_COUNTRY_CLOSE_TAG = "</c:country>";
	
	public static final String REQUEST_COUNTRY_NUMERIC_CODE_OPEN_TAG = "<c:countryNumericCode>";
	public static final String REQUEST_COUNTRY_NUMERIC_CODE_CLOSE_TAG = "</c:countryNumericCode>";
	
	
	public BinLookupModel toTransaction(String xml) {

		BinLookupModel lookup = new BinLookupModel();        
		lookup.setMerchantReferenceCode(getTextBetweenTags(xml, MERCHANT_REFERENCE_OPEN_TAG, MERCHANT_REFERENCE_CLOSE_TAG));
		lookup.setRequestID(getTextBetweenTags(xml,REQUEST_ID_OPEN_TAG,REQUEST_ID_CLOSE_TAG));
		lookup.setDecision(getTextBetweenTags(xml, DECISION_OPEN_TAG, DECISION_CLOSE_TAG));
		lookup.setReasonCode(getTextBetweenTags(xml,REASON_OPEN_TAG,REASON_CLOSE_TAG));
		lookup.setRequestToken(getTextBetweenTags(xml,REQUEST_TOKEN_OPEN_TAG,REQUEST_TOKEN_CLOSE_TAG));
		lookup.setRequestDateTime(getTextBetweenTags(xml,REQUEST_DATE_TIME_OPEN_TAG,REQUEST_DATE_TIME_CLOSE_TAG));
		lookup.setCardType(getTextBetweenTags(xml,REQUEST_CARD_TYPE_OPEN_TAG,REQUEST_CARD_TYPE_CLOSE_TAG));
		lookup.setCardTypeName(getTextBetweenTags(xml,REQUEST_CARD_TYPE_NAME_OPEN_TAG,REQUEST_CARD_TYPE_NAME_CLOSE_TAG));
		lookup.setCardSubType(getTextBetweenTags(xml,REQUEST_CARD_SUB_TYPE_OPEN_TAG,REQUEST_CARD_SUB_TYPE_CLOSE_TAG));
		lookup.setProductCategory(getTextBetweenTags(xml,REQUEST_PRODUCTION_CATEGORY_OPEN_TAG,REQUEST_PRODUCTION_CATEGORY_CLOSE_TAG));
		lookup.setIssuerName(getTextBetweenTags(xml,REQUEST_NAME_OPEN_TAG,REQUEST_NAME_CLOSE_TAG));
		lookup.setCountry(getTextBetweenTags(xml,REQUEST_COUNTRY_OPEN_TAG,REQUEST_COUNTRY_CLOSE_TAG));
		lookup.setCountryNumericCode(getTextBetweenTags(xml,REQUEST_COUNTRY_NUMERIC_CODE_OPEN_TAG,REQUEST_COUNTRY_NUMERIC_CODE_CLOSE_TAG));
		

		return lookup;
	}   
	   // toTransaction()
 
	public BinLookupConverter() {
	}

	public String getTextBetweenTags(String text, String tag1, String tag2) {

		int leftIndex = text.indexOf(tag1);
		if (leftIndex == -1) {
			return null;
		}

		int rightIndex = text.indexOf(tag2);
		if (rightIndex != -1) {
			leftIndex = leftIndex + tag1.length();
			return text.substring(leftIndex, rightIndex);
		}

		return null;
	}


}
