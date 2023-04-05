package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.util.Date;

public class MerchantObject implements Serializable{
	
	private String resellerName;
	private String payId;
	private String businessName;
	private String subMerchant;
	private String status;
	private String registrationDate;
	
	public String getResellerName() {
		return resellerName;
	}
	public void setResellerName(String resellerName) {
		this.resellerName = resellerName;
	}

	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}
	public String getSubMerchant() {
		return subMerchant;
	}
	public void setSubMerchant(String subMerchant) {
		this.subMerchant = subMerchant;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getRegistrationDate() {
		return registrationDate;
	}
	public void setRegistrationDate(String registrationDate) {
		this.registrationDate = registrationDate;
	}
	
	public Object[] myCsvMethodDownloadSubMarchantListView() {
		  Object[] objectArray = new Object[5];
		  
		 
		  
		  objectArray[0] = registrationDate;
		  objectArray[1] = businessName;
		  objectArray[2] = subMerchant;
		  objectArray[3] = payId;
		  objectArray[4] = status;
		  return objectArray;
		}
	
	public Object[] myCsvMethodDownloadMarchantListView() {
		  Object[] objectArray = new Object[4];
		
		  objectArray[0] = registrationDate;
		  objectArray[1] = businessName;
		  objectArray[2] = payId;
		  objectArray[3] = status;
		  return objectArray;
		}
	
	public Object[] myCsvMethodDownloadResellerSubMarchantListView() {
		  Object[] objectArray = new Object[6];
		  
		 
		  
		  objectArray[0] = registrationDate;
		  objectArray[1] = resellerName;
		  objectArray[2] = businessName;
		  objectArray[3] = subMerchant;
		  objectArray[4] = payId;
		  objectArray[5] = status;
		  return objectArray;
		}
	
	public Object[] myCsvMethodDownloadResellerMarchantListView() {
		  Object[] objectArray = new Object[5];
		
		  objectArray[0] = registrationDate;
		  objectArray[1] = resellerName;
		  objectArray[2] = businessName;
		  objectArray[3] = payId;
		  objectArray[4] = status;
		  return objectArray;
		}
	
}
