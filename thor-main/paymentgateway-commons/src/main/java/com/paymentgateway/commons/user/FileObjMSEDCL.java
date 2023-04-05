package com.paymentgateway.commons.user;

public class FileObjMSEDCL {
	
	private String bu;
	private String zone;
	private String circle;
	private String div;
	private String subDiv;
	private String zName;
	private String cName;
	private String dName;
	private String sName;
	private String ccCode;
	private String urFlag;
	
	public String getBu() {
		return bu;
	}
	public void setBu(String bu) {
		this.bu = bu;
	}
	public String getZone() {
		return zone;
	}
	public void setZone(String zone) {
		this.zone = zone;
	}
	public String getCircle() {
		return circle;
	}
	public void setCircle(String circle) {
		this.circle = circle;
	}
	public String getDiv() {
		return div;
	}
	public void setDiv(String div) {
		this.div = div;
	}
	public String getSubDiv() {
		return subDiv;
	}
	public void setSubDiv(String subDiv) {
		this.subDiv = subDiv;
	}
	public String getzName() {
		return zName;
	}
	public void setzName(String zName) {
		this.zName = zName;
	}
	public String getcName() {
		return cName;
	}
	public void setcName(String cName) {
		this.cName = cName;
	}
	public String getdName() {
		return dName;
	}
	public void setdName(String dName) {
		this.dName = dName;
	}
	public String getsName() {
		return sName;
	}
	public void setsName(String sName) {
		this.sName = sName;
	}
	public String getCcCode() {
		return ccCode;
	}
	public void setCcCode(String ccCode) {
		this.ccCode = ccCode;
	}
	public String getUrFlag() {
		return urFlag;
	}
	public void setUrFlag(String urFlag) {
		this.urFlag = urFlag;
	}
	
	public Object[] xlsxMethodForDownloadFile() {
		  Object[] objectArray = new Object[17];
		  
		  objectArray[0] = bu;
		  objectArray[1] = zone;
		  objectArray[2] = circle;
		  objectArray[3] = div;
		  objectArray[4] = subDiv;
		  objectArray[5] = zName;
		  objectArray[6] = cName;
		  objectArray[7] = dName;
		  objectArray[8] = sName;
		  objectArray[9] = ccCode;
		  objectArray[10] = urFlag;
		  objectArray[11] = bu;
		  
		  return objectArray;
		}
	

	
}
