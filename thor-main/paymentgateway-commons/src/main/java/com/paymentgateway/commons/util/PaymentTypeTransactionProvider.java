package com.paymentgateway.commons.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.paymentgateway.commons.user.ChargingDetails;

public class PaymentTypeTransactionProvider {
	
	Map<String,Object> supportedCardTypeMap= new TreeMap<String,Object>();
    Map<String,Object> supportedPaymentTypeMap= new TreeMap<String,Object>();
	List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
	
	
	public Map<String, Object> getSupportedCardTypeMap() {
		return supportedCardTypeMap;
	}
	public void setSupportedCardTypeMap(Map<String, Object> supportedCardTypeMap) {
		this.supportedCardTypeMap = supportedCardTypeMap;
	}
	public Map<String, Object> getSupportedPaymentTypeMap() {
		return supportedPaymentTypeMap;
	}
	public void setSupportedPaymentTypeMap(Map<String, Object> supportedPaymentTypeMap) {
		this.supportedPaymentTypeMap = supportedPaymentTypeMap;
	}
	public List<ChargingDetails> getChargingDetailsList() {
		return chargingDetailsList;
	}
	public void setChargingDetailsList(List<ChargingDetails> chargingDetailsList) {
		this.chargingDetailsList = chargingDetailsList;
	}	

	
	
}
