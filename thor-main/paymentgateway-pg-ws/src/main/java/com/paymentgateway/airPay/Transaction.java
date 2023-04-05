package com.paymentgateway.airPay;

import org.springframework.stereotype.Service;

/**
 * @author Sandeep
 *
 */

@Service("airPayTransaction")
public class Transaction {

	private String buyerEmail;
	private String buyerPhone;
	private String buyerFirstName;
	private String buyerLastName;
	private String buyerAddress;
	private String amount;
	private String buyerCity;
	private String buyerState;
	private String buyerPinCode;
	private String buyerCountry;
	private String customvar;
	private String txnsubtype;
	private String orderid;
	private String UserName;
	private String Password;
	private String Secret;
	private String MercId;
	private String TRANSACTIONID;
	private String APTRANSACTIONID;
	private String AMOUNT;
	private String TRANSACTIONSTATUS;
	private String MESSAGE;
	private String ap_SecureHash;
	private String CUSTOMVAR;
	private String mode;
	private String success;
	private String message;
	private String airpayid;
	private String refundairpayid;

	private String TRANSACTIONTIME;
	private String TRANSACTIONPAYMENTSTATUS;
	private String CHMOD;
	private String BANKRESPONSEMSG;
	private String CURRENCYCODE;
	private String TRANSACTIONTYPE;
	private String TXN_RETRY;
	private String CUSTOMEREMAIL;
	private String CUSTOMERPHONE;
	private String BILLEDAMOUNT;
	private String SURCHARGE;
	private String CUSTOMER;
	private String CUSTOMERVPA;

	public String getBuyerEmail() {
		return buyerEmail;
	}

	public void setBuyerEmail(String buyerEmail) {
		this.buyerEmail = buyerEmail;
	}

	public String getBuyerPhone() {
		return buyerPhone;
	}

	public void setBuyerPhone(String buyerPhone) {
		this.buyerPhone = buyerPhone;
	}

	public String getBuyerFirstName() {
		return buyerFirstName;
	}

	public void setBuyerFirstName(String buyerFirstName) {
		this.buyerFirstName = buyerFirstName;
	}

	public String getBuyerLastName() {
		return buyerLastName;
	}

	public void setBuyerLastName(String buyerLastName) {
		this.buyerLastName = buyerLastName;
	}

	public String getBuyerAddress() {
		return buyerAddress;
	}

	public void setBuyerAddress(String buyerAddress) {
		this.buyerAddress = buyerAddress;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getBuyerCity() {
		return buyerCity;
	}

	public void setBuyerCity(String buyerCity) {
		this.buyerCity = buyerCity;
	}

	public String getBuyerState() {
		return buyerState;
	}

	public void setBuyerState(String buyerState) {
		this.buyerState = buyerState;
	}

	public String getBuyerPinCode() {
		return buyerPinCode;
	}

	public void setBuyerPinCode(String buyerPinCode) {
		this.buyerPinCode = buyerPinCode;
	}

	public String getBuyerCountry() {
		return buyerCountry;
	}

	public void setBuyerCountry(String buyerCountry) {
		this.buyerCountry = buyerCountry;
	}

	public String getCustomvar() {
		return customvar;
	}

	public void setCustomvar(String customvar) {
		this.customvar = customvar;
	}

	public String getTxnsubtype() {
		return txnsubtype;
	}

	public void setTxnsubtype(String txnsubtype) {
		this.txnsubtype = txnsubtype;
	}

	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}

	public String getUserName() {
		return UserName;
	}

	public void setUserName(String userName) {
		UserName = userName;
	}

	public String getPassword() {
		return Password;
	}

	public void setPassword(String password) {
		Password = password;
	}

	public String getSecret() {
		return Secret;
	}

	public void setSecret(String secret) {
		Secret = secret;
	}

	public String getMercId() {
		return MercId;
	}

	public void setMercId(String mercId) {
		MercId = mercId;
	}

	public String getTRANSACTIONID() {
		return TRANSACTIONID;
	}

	public void setTRANSACTIONID(String tRANSACTIONID) {
		TRANSACTIONID = tRANSACTIONID;
	}

	public String getAPTRANSACTIONID() {
		return APTRANSACTIONID;
	}

	public void setAPTRANSACTIONID(String aPTRANSACTIONID) {
		APTRANSACTIONID = aPTRANSACTIONID;
	}

	public String getAMOUNT() {
		return AMOUNT;
	}

	public void setAMOUNT(String aMOUNT) {
		AMOUNT = aMOUNT;
	}

	public String getTRANSACTIONSTATUS() {
		return TRANSACTIONSTATUS;
	}

	public void setTRANSACTIONSTATUS(String tRANSACTIONSTATUS) {
		TRANSACTIONSTATUS = tRANSACTIONSTATUS;
	}

	public String getMESSAGE() {
		return MESSAGE;
	}

	public void setMESSAGE(String mESSAGE) {
		MESSAGE = mESSAGE;
	}

	public String getAp_SecureHash() {
		return ap_SecureHash;
	}

	public void setAp_SecureHash(String ap_SecureHash) {
		this.ap_SecureHash = ap_SecureHash;
	}

	public String getCUSTOMVAR() {
		return CUSTOMVAR;
	}

	public void setCUSTOMVAR(String cUSTOMVAR) {
		CUSTOMVAR = cUSTOMVAR;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * @return the success
	 */
	public String getSuccess() {
		return success;
	}

	/**
	 * @param success the success to set
	 */
	public void setSuccess(String success) {
		this.success = success;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the airpayid
	 */
	public String getAirpayid() {
		return airpayid;
	}

	/**
	 * @param airpayid the airpayid to set
	 */
	public void setAirpayid(String airpayid) {
		this.airpayid = airpayid;
	}

	/**
	 * @return the refundairpayid
	 */
	public String getRefundairpayid() {
		return refundairpayid;
	}

	/**
	 * @param refundairpayid the refundairpayid to set
	 */
	public void setRefundairpayid(String refundairpayid) {
		this.refundairpayid = refundairpayid;
	}

	public String getTRANSACTIONTIME() {
		return TRANSACTIONTIME;
	}

	public void setTRANSACTIONTIME(String tRANSACTIONTIME) {
		TRANSACTIONTIME = tRANSACTIONTIME;
	}

	public String getTRANSACTIONPAYMENTSTATUS() {
		return TRANSACTIONPAYMENTSTATUS;
	}

	public void setTRANSACTIONPAYMENTSTATUS(String tRANSACTIONPAYMENTSTATUS) {
		TRANSACTIONPAYMENTSTATUS = tRANSACTIONPAYMENTSTATUS;
	}

	public String getCHMOD() {
		return CHMOD;
	}

	public void setCHMOD(String cHMOD) {
		CHMOD = cHMOD;
	}

	public String getBANKRESPONSEMSG() {
		return BANKRESPONSEMSG;
	}

	public void setBANKRESPONSEMSG(String bANKRESPONSEMSG) {
		BANKRESPONSEMSG = bANKRESPONSEMSG;
	}

	public String getCURRENCYCODE() {
		return CURRENCYCODE;
	}

	public void setCURRENCYCODE(String cURRENCYCODE) {
		CURRENCYCODE = cURRENCYCODE;
	}

	public String getTRANSACTIONTYPE() {
		return TRANSACTIONTYPE;
	}

	public void setTRANSACTIONTYPE(String tRANSACTIONTYPE) {
		TRANSACTIONTYPE = tRANSACTIONTYPE;
	}

	public String getTXN_RETRY() {
		return TXN_RETRY;
	}

	public void setTXN_RETRY(String tXN_RETRY) {
		TXN_RETRY = tXN_RETRY;
	}

	public String getCUSTOMEREMAIL() {
		return CUSTOMEREMAIL;
	}

	public void setCUSTOMEREMAIL(String cUSTOMEREMAIL) {
		CUSTOMEREMAIL = cUSTOMEREMAIL;
	}

	public String getCUSTOMERPHONE() {
		return CUSTOMERPHONE;
	}

	public void setCUSTOMERPHONE(String cUSTOMERPHONE) {
		CUSTOMERPHONE = cUSTOMERPHONE;
	}

	public String getBILLEDAMOUNT() {
		return BILLEDAMOUNT;
	}

	public void setBILLEDAMOUNT(String bILLEDAMOUNT) {
		BILLEDAMOUNT = bILLEDAMOUNT;
	}

	public String getSURCHARGE() {
		return SURCHARGE;
	}

	public void setSURCHARGE(String sURCHARGE) {
		SURCHARGE = sURCHARGE;
	}

	public String getCUSTOMER() {
		return CUSTOMER;
	}

	public void setCUSTOMER(String cUSTOMER) {
		CUSTOMER = cUSTOMER;
	}

	public String getCUSTOMERVPA() {
		return CUSTOMERVPA;
	}

	public void setCUSTOMERVPA(String cUSTOMERVPA) {
		CUSTOMERVPA = cUSTOMERVPA;
	}

}
