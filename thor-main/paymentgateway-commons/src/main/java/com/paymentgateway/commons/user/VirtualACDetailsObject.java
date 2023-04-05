package com.paymentgateway.commons.user;

public class VirtualACDetailsObject {

	private String virtualAccountNo;
	private String virtualIfscCode;
	private String virtualBeneficiaryName;
	private String merchantName;
	private String subMerchantName;
	private String createDate;
	private String srNo;
	
	
	
	
	
	
	
	public Object[] csvFileDownloadMethodForMerchant() {
		  Object[] objectArray = new Object[6];
		  
		 
		  objectArray[0] = srNo;
		  objectArray[1] = merchantName;
		  objectArray[2] = virtualAccountNo;
		  objectArray[3] = virtualIfscCode;
		  objectArray[4] = virtualBeneficiaryName;
		  objectArray[5] = createDate;
		  
		  return objectArray;
		}
	
	
	public Object[] csvFileDownloadMethodForMerchantAndSubMerchant() {
		  Object[] objectArray = new Object[7];
		  
		 
		  objectArray[0] = srNo;
		  objectArray[1] = merchantName;
		  objectArray[2] = subMerchantName;
		  objectArray[3] = virtualAccountNo;
		  objectArray[4] = virtualIfscCode;
		  objectArray[5] = virtualBeneficiaryName;
		  objectArray[6] = createDate;
		  
		  return objectArray;
		}
	
	
	
	
	public String getVirtualAccountNo() {
		return virtualAccountNo;
	}
	public void setVirtualAccountNo(String virtualAccountNo) {
		this.virtualAccountNo = virtualAccountNo;
	}
	public String getVirtualIfscCode() {
		return virtualIfscCode;
	}
	public void setVirtualIfscCode(String virtualIfscCode) {
		this.virtualIfscCode = virtualIfscCode;
	}
	public String getVirtualBeneficiaryName() {
		return virtualBeneficiaryName;
	}
	public void setVirtualBeneficiaryName(String virtualBeneficiaryName) {
		this.virtualBeneficiaryName = virtualBeneficiaryName;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public String getSubMerchantName() {
		return subMerchantName;
	}

	public void setSubMerchantName(String subMerchantName) {
		this.subMerchantName = subMerchantName;
	}

	public String getSrNo() {
		return srNo;
	}

	public void setSrNo(String srNo) {
		this.srNo = srNo;
	}

}
