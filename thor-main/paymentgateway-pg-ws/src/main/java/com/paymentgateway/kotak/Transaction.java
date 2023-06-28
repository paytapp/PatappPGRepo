package com.paymentgateway.kotak;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;

@Service("kotakTransaction")
public class Transaction {
	
	private String cardNumber;
	private String expiryDate;
	private String cvv;
	private String txnType;
	private String responseCode;
	private String acqId;
	private String authCode;
	private String hash;
	private String terminalId;
	private String amount;
	private String maskedCardNo;
	private String txnRefNo;
	private String message;
	private String merchantId;
	private String orderInfo;
	private String cardType;
	private String bacthNo;
	private String status;
	
	
	
	public void setEnrollment(Fields fields) {
		setCardDetails(fields);
		setTxnType(fields);
	}
	
	public void setRefund(Fields fields) {
		setTxnType(fields);
	}
	
	private void setCardDetails(Fields fields) {
		setCardNumber(fields.get(FieldType.CARD_NUMBER.getName()));
		setCvv(fields.get(FieldType.CVV.getName()));
		String expDate = fields.get(FieldType.CARD_EXP_DT.getName());
		String expYear = (expDate.substring(4, 6));
		String expMonth = (expDate.substring(0, 2));
		setExpiryDate(expMonth.concat(expYear));
	}
	
	private void setTxnType(Fields fields) {
		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if(txnType.equals(TransactionType.SALE.toString())){
			setTxnType("01");
		}else if(txnType.equals(TransactionType.REFUND.toString())){
			setTxnType("04");
		}else if(txnType.equals(TransactionType.ENQUIRY.toString())){
			setTxnType("05");
		}else{
			
		}
	}
	
	public String getCardNumber() {
		return cardNumber;
	}
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}
	public String getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}
	public String getCvv() {
		return cvv;
	}
	public void setCvv(String cvv) {
		this.cvv = cvv;
	}
	public String getTxnType() {
		return txnType;
	}
	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	public String getAcqId() {
		return acqId;
	}

	public void setAcqId(String acqId) {
		this.acqId = acqId;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getTerminalId() {
		return terminalId;
	}

	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}

	public String getMaskedCardNo() {
		return maskedCardNo;
	}

	public void setMaskedCardNo(String maskedCardNo) {
		this.maskedCardNo = maskedCardNo;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTxnRefNo() {
		return txnRefNo;
	}

	public void setTxnRefNo(String txnRefNo) {
		this.txnRefNo = txnRefNo;
	}

	public String getBacthNo() {
		return bacthNo;
	}

	public void setBacthNo(String bacthNo) {
		this.bacthNo = bacthNo;
	}

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public String getOrderInfo() {
		return orderInfo;
	}

	public void setOrderInfo(String orderInfo) {
		this.orderInfo = orderInfo;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
