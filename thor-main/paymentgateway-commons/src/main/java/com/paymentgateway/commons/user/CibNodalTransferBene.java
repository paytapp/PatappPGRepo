package com.paymentgateway.commons.user;

public class CibNodalTransferBene {
	
	private String merchantName;
	private String subMerchantName;
	private String alias;
	private String PayId;
	private String createDate;
	private String bankAccountName;
	private String bankAccountNumber;
	private String bankIfsc;
	private String payeeType;
	private String status;
	private String responseMsg;
	private boolean defaultBene;
	
	
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getPayId() {
		return PayId;
	}
	public void setPayId(String payId) {
		PayId = payId;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public String getBankAccountName() {
		return bankAccountName;
	}
	public void setBankAccountName(String bankAccountName) {
		this.bankAccountName = bankAccountName;
	}
	public String getBankAccountNumber() {
		return bankAccountNumber;
	}
	public void setBankAccountNumber(String bankAccountNumber) {
		this.bankAccountNumber = bankAccountNumber;
	}

	public String getPayeeType() {
		return payeeType;
	}
	public void setPayeeType(String payeeType) {
		this.payeeType = payeeType;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getResponseMsg() {
		return responseMsg;
	}
	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}
	public String getBankIfsc() {
		return bankIfsc;
	}
	public void setBankIfsc(String bankIfsc) {
		this.bankIfsc = bankIfsc;
	}
	public boolean isDefaultBene() {
		return defaultBene;
	}
	public void setDefaultBene(boolean defaultBene) {
		this.defaultBene = defaultBene;
	}
	
	
	public String getSubMerchantName() {
		return subMerchantName;
	}
	public void setSubMerchantName(String subMerchantName) {
		this.subMerchantName = subMerchantName;
	}
	public Object[] csvMethodForDownloadFile() {
		  Object[] objectArray = new Object[11];
		  
		  
		  objectArray[0] = merchantName;
		  objectArray[1] = alias;
		  objectArray[2] = PayId;
		  objectArray[3] = createDate;
		  objectArray[4] = bankAccountName;
		  objectArray[5] = bankAccountNumber;
		  objectArray[6] = bankIfsc;
		  objectArray[7] = payeeType;
		  objectArray[8] = status;
		  objectArray[9] = responseMsg;
		  
		  return objectArray;
		}
	
	public Object[] csvMethodForSubMerchatDownloadFile() {
		  Object[] objectArray = new Object[11];
		  
		  
		  objectArray[0] = merchantName;
		  objectArray[1] = subMerchantName;
		  objectArray[2] = alias;
		  objectArray[3] = PayId;
		  objectArray[4] = createDate;
		  objectArray[5] = bankAccountName;
		  objectArray[6] = bankAccountNumber;
		  objectArray[7] = bankIfsc;
		  objectArray[8] = payeeType;
		  objectArray[9] = status;
		  objectArray[10] = responseMsg;
		  
		  return objectArray;
		}
	
	
	

}
