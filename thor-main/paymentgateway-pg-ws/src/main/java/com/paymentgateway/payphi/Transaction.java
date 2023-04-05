package com.paymentgateway.payphi;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.TransactionType;


/**
 * @author Shaiwal
 *77
 */

@Service("payphiTransaction")
public class Transaction {


	private String merchantID;
	private String aggregatorID;
	private String merchantTxnNo;
	private String amount;
	private String currencyCode;
	private String secureHash;
	private String payType;
	private String customerEmailID;

	private String transactionType;
	private String paymentMode;
	private String txnDate;
	private String returnURL;
	private String customerMobileNo;
	private String cardNo;
	private String cardExp;
	private String nameOnCard;
	private String cvv;
	private String txnKey;
	private String callType;
	private String responseCode;
	private String respDescription;
	private String txnID;
	private String txnAuthID;
	private String paymentID;
	private String txnStatus;
	private String txnResponseCode;
	private String txnRespDescription;
	private String paymentOptionCodes;
	private String customerUPIAlias;
	private String originalTxnNo;
	
	public void setEnrollment(Fields fields) {
		setMerchantInformation(fields);
		setTxnDataDetails(fields);
	}

	private void setMerchantInformation(Fields fields) {
		
		setMerchantID(fields.get(FieldType.MERCHANT_ID.getName()));
		setAggregatorID(fields.get(FieldType.PASSWORD.getName()));
		setTxnKey(fields.get(FieldType.TXN_KEY.getName()));
	}

	private void setTxnDataDetails(Fields fields) {

		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddhhmmss");  
	    String strDate= formatter.format(date);  
	    
		setMerchantTxnNo(fields.get(FieldType.PG_REF_NUM.getName()));
		setAmount(fields.get(FieldType.TOTAL_AMOUNT.getName()));
		setCurrencyCode(fields.get(FieldType.CURRENCY_CODE.getName()));
		setPayType("1"); // Direct 
		
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
		setCustomerEmailID(fields.get(FieldType.CUST_EMAIL.getName()));
		}
		else {
			setCustomerEmailID("guest@phicommerce.com");
		}
		setTransactionType("SALE");
		if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())
				 || fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())) {
			setPaymentMode("CARD");
		}
		else if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.UPI.getCode())) {
			setCustomerUPIAlias(fields.get(FieldType.PAYER_ADDRESS.getName()));
			setPaymentMode("UPI");
		}
		else if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.NET_BANKING.getCode())) {
			setPaymentOptionCodes(PayphiNBMopType.getBankCode(fields.get(FieldType.MOP_TYPE.getName())));
			setPaymentMode("NB");
		}
		
		setTxnDate(strDate); 
		setCallType("s2s");
		
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_PHONE.getName()))) {
			setCustomerMobileNo(fields.get(FieldType.CUST_PHONE.getName()));
			}
			else {
				setCustomerMobileNo("8888888888");
			}
		
		if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())) {
			
			String expDate = fields.get(FieldType.CARD_EXP_DT.getName());
			String expDateCustom = expDate.substring(2, 6)+expDate.substring(0, 2);
			setCardNo(fields.get(FieldType.CARD_NUMBER.getName()));
			setCardExp(expDateCustom);
			setCvv(fields.get(FieldType.CVV.getName()));
			setNameOnCard(fields.get(FieldType.CUST_NAME.getName()));
			setPaymentOptionCodes("CC");
		} else if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())){
			String expDate = fields.get(FieldType.CARD_EXP_DT.getName());
			String expDateCustom = expDate.substring(2, 6)+expDate.substring(0, 2);
			setCardNo(fields.get(FieldType.CARD_NUMBER.getName()));
			setCardExp(expDateCustom);
			setCvv(fields.get(FieldType.CVV.getName()));
			setNameOnCard(fields.get(FieldType.CUST_NAME.getName()));
			setPaymentOptionCodes("DC");
		}

	}

	public void setStatusEnquiry(Fields fields) {
	
		setMerchantInformation(fields);
		
		setMerchantTxnNo(fields.get(FieldType.PG_REF_NUM.getName()));
		setTransactionType("STATUS");
		setOriginalTxnNo(fields.get(FieldType.PG_REF_NUM.getName()));
		
	}
	

	public void setRefund(Fields fields) {
		
		setMerchantInformation(fields);
		
		setMerchantTxnNo(fields.get(FieldType.TXN_ID.getName()));
		setAmount(Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()), fields.get(FieldType.CURRENCY_CODE.getName())));
		setTransactionType("REFUND");
		setOriginalTxnNo(fields.get(FieldType.PG_REF_NUM.getName()));
		
	}

	public String getMerchantID() {
		return merchantID;
	}

	public void setMerchantID(String merchantID) {
		this.merchantID = merchantID;
	}

	public String getAggregatorID() {
		return aggregatorID;
	}

	public void setAggregatorID(String aggregatorID) {
		this.aggregatorID = aggregatorID;
	}

	public String getMerchantTxnNo() {
		return merchantTxnNo;
	}

	public void setMerchantTxnNo(String merchantTxnNo) {
		this.merchantTxnNo = merchantTxnNo;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getSecureHash() {
		return secureHash;
	}

	public void setSecureHash(String secureHash) {
		this.secureHash = secureHash;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public String getCustomerEmailID() {
		return customerEmailID;
	}

	public void setCustomerEmailID(String customerEmailID) {
		this.customerEmailID = customerEmailID;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}

	public String getTxnDate() {
		return txnDate;
	}

	public void setTxnDate(String txnDate) {
		this.txnDate = txnDate;
	}

	public String getReturnURL() {
		return returnURL;
	}

	public void setReturnURL(String returnURL) {
		this.returnURL = returnURL;
	}

	public String getCustomerMobileNo() {
		return customerMobileNo;
	}

	public void setCustomerMobileNo(String customerMobileNo) {
		this.customerMobileNo = customerMobileNo;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getCardExp() {
		return cardExp;
	}

	public void setCardExp(String cardExp) {
		this.cardExp = cardExp;
	}

	public String getNameOnCard() {
		return nameOnCard;
	}

	public void setNameOnCard(String nameOnCard) {
		this.nameOnCard = nameOnCard;
	}

	public String getCvv() {
		return cvv;
	}

	public void setCvv(String cvv) {
		this.cvv = cvv;
	}

	public String getTxnKey() {
		return txnKey;
	}

	public void setTxnKey(String txnKey) {
		this.txnKey = txnKey;
	}

	public String getCallType() {
		return callType;
	}

	public void setCallType(String callType) {
		this.callType = callType;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getRespDescription() {
		return respDescription;
	}

	public void setRespDescription(String respDescription) {
		this.respDescription = respDescription;
	}

	public String getTxnAuthID() {
		return txnAuthID;
	}

	public void setTxnAuthID(String txnAuthID) {
		this.txnAuthID = txnAuthID;
	}

	public String getTxnID() {
		return txnID;
	}

	public void setTxnID(String txnID) {
		this.txnID = txnID;
	}

	public String getPaymentID() {
		return paymentID;
	}

	public void setPaymentID(String paymentID) {
		this.paymentID = paymentID;
	}

	public String getTxnStatus() {
		return txnStatus;
	}

	public void setTxnStatus(String txnStatus) {
		this.txnStatus = txnStatus;
	}

	public String getTxnResponseCode() {
		return txnResponseCode;
	}

	public void setTxnResponseCode(String txnResponseCode) {
		this.txnResponseCode = txnResponseCode;
	}

	public String getTxnRespDescription() {
		return txnRespDescription;
	}

	public void setTxnRespDescription(String txnRespDescription) {
		this.txnRespDescription = txnRespDescription;
	}

	public String getPaymentOptionCodes() {
		return paymentOptionCodes;
	}

	public void setPaymentOptionCodes(String paymentOptionCodes) {
		this.paymentOptionCodes = paymentOptionCodes;
	}

	public String getCustomerUPIAlias() {
		return customerUPIAlias;
	}

	public void setCustomerUPIAlias(String customerUPIAlias) {
		this.customerUPIAlias = customerUPIAlias;
	}

	public String getOriginalTxnNo() {
		return originalTxnNo;
	}

	public void setOriginalTxnNo(String originalTxnNo) {
		this.originalTxnNo = originalTxnNo;
	}

	
}
