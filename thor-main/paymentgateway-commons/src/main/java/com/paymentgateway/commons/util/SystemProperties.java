package com.paymentgateway.commons.util;

import java.util.Collection;

public class SystemProperties {

	private static final boolean debug = checkDebug();
	private static final Collection<String> responseFields = createResponseFields();
	private static final Collection<String> secureRequestFields = getSecureRequestFieldsFromConfigFile();
	private static final Collection<String> requestFields = getRequestFields();
	private static final Collection<String> fssEnrollMandatoryFields = getFssEnrollMandatoryFields();
	private static final Collection<String> firstDataEnrollMandatoryFields = getFirstDataEnrollMandatoryFields();
	private static final Collection<String> internalRequestFields = createInternalRequestFields();
	private static final Collection<String> internalResponseFields = getInternalResponseFields();
	private static final Collection<String> callbackFields = getCallbackFields();
	private static final Collection<String> eNachDebitTxnCallbackfields = getENachDebitTxnCallbackfields();
	private static final Collection<String> upiAutoPayTxnCallbackfields = getUpiAutoPayTxnCallbackfields();


	public SystemProperties() {
		
		PropertiesManager pn = new PropertiesManager();
	}

	private static Collection<String> getSecureRequestFieldsFromConfigFile() {
		return Helper.parseFields(ConfigurationConstants.SECURE_REQUEST_FIELDS.getValue());
	}
	private static boolean checkDebug(){
		if(ConfigurationConstants.IS_DEBUG.getValue().equals("1")){
			return true;
		} else {
			return false;
		}
	}

	public static Collection<String> getAllDBRequestFields() {
		return Helper.parseFields(ConfigurationConstants.DB_ALLREQUESTSTRINGFIELDS.getValue());
	}
	// created by shashi only for try
	 public static Collection<String> getDBFields() {
	  return Helper.parseFields(ConfigurationConstants.DB_FIELDS.getValue());
	 }
	
	 public static Collection<String> getTransactionStatusFields() {
		  return Helper.parseFields(ConfigurationConstants.TRANSACTION_STATUS_FIELDS.getValue());
		 }
	 
	 public static Collection<String> getECollectionDBFields() {
		  return Helper.parseFields(ConfigurationConstants.ECOLLECTION_RESPONSE_FIELDS.getValue());
		 }
	 
	 public static Collection<String> getCustInfoFields() {
		  return Helper.parseFields(ConfigurationConstants.CUSTOMER_INFO_FIELDS.getValue());
		 }
		
	private static Collection<String> getRequestFields() {
		return Helper.parseFields(ConfigurationConstants.REQUEST_FIELDS.getValue());
	}
	private static Collection<String> createInternalRequestFields() {
		return Helper.parseFields(ConfigurationConstants.INTERNAL_REQUEST_FIELDS.getValue());
	}
	private static Collection<String> getInternalResponseFields() {
		return Helper.parseFields(ConfigurationConstants.INTERNAL_RESPONSE_FIELDS.getValue());
	}
	private static Collection<String> getFssEnrollMandatoryFields(){
		return Helper.parseFields(ConfigurationConstants.FSS_MANDATORY_FIELDS_ENROLL.getValue());
	}
	
	private static Collection<String> getFirstDataEnrollMandatoryFields(){
		return Helper.parseFields(ConfigurationConstants.FIRSTDATA_MANDATORY_FIELDS_ENROLL.getValue());
	}
	private static Collection<String> getCallbackFields(){
		return Helper.parseFields(ConfigurationConstants.CALLBACK_FIELDS.getValue());
	}
	
	private static Collection<String> getENachDebitTxnCallbackfields(){
		return Helper.parseFields(ConfigurationConstants.ENACHDEBIT_CALLBACK_FIELDS.getValue());
	}
	private static Collection<String> getUpiAutoPayTxnCallbackfields(){
		return Helper.parseFields(ConfigurationConstants.UPIAUTOPAYTXN_CALLBACK_FIELDS.getValue());
	}
	public static Collection<String> getSecureRequestFields() {
		return secureRequestFields;
	}
	
	public static Collection<String> getSecurerequestfields() {
		return secureRequestFields;
	}

	public static boolean isDebug() {
		return debug;
	}
	
	private static Collection<String> createResponseFields() {
		return Helper.parseFields(ConfigurationConstants.RESPONSE_FIELDS.getValue());
	}

	public static Collection<String> getResponseFields() {
		return responseFields;
	}

	public static Collection<String> getRequestfields() {
		return requestFields;
	}

	public static Collection<String> getInternalRequestFields() {
		return internalRequestFields;
	}

	public static Collection<String> getInternalResponsefields() {
		return internalResponseFields;
	}

	public static Collection<String> getFssenrollmandatoryfields() {
		return fssEnrollMandatoryFields;
	}

	public static Collection<String> getFirstdataenrollmandatoryfields() {
		return firstDataEnrollMandatoryFields;
	}

	public static Collection<String> getCallbackfields() {
		return callbackFields;
	}

	
	public static Collection<String> geteNachDebitTxnCallbackfields() {
		return eNachDebitTxnCallbackfields;
	}
	
	public static Collection<String> getUpiAutoPayTxnCallbackFields() {
		return upiAutoPayTxnCallbackfields;
	}

}
