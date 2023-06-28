package com.paymentgateway.bob;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.TransactionType;

@Service("bobTransaction")
public class Transaction {

	private String action;
	private String amount;
	private String expMonth;
	private String expYear;
	private String id;
	private String card;
	private String cvv;
	private String type;
	private String member;
	private String password;
	private String txnKey;
	private String result;
	private String auth;
	private String ref;
	private String avr;
	private String error_text;
	private String error_code_tag;
	private String paymentid;
	private String trackId;
	private String error_service_tag;
	private String tranId;
	private String payId;
	private String paymentType;
	private String cardIssuerCountry;
	private String cardType;
	private String transactionType;

	public void setEnrollment(Fields fields) {
		setCardDetails(fields);
		setMerchantInformation(fields);
		setAction(fields);
		setUdf(fields);
	}

	public void setSale(Fields fields) {
		setCard(fields.get(FieldType.PAYER_ADDRESS.getName()));
		setMerchantInformation(fields);
		setUdf(fields);
		setAction(fields);
		setMember(fields.get(FieldType.PAYER_NAME.getName()));
		fields.put(FieldType.CUST_NAME.getName(), fields.get(FieldType.PAYER_NAME.getName()));
	}

	public void setUdf(Fields fields) {
		String paymentType = fields.get(FieldType.PAYMENT_TYPE.getName());
		if (paymentType.equals(PaymentType.CREDIT_CARD.getCode()) || paymentType.equals(PaymentType.EMI_CC.getCode())) {
			setPaymentType("Credit Card");
		} else if (paymentType.equals(PaymentType.DEBIT_CARD.getCode())
				|| paymentType.equals(PaymentType.EMI_DC.getCode())) {
			setPaymentType("Debit Card");
		} else {
			setPaymentType("UPI_VPA");
		}
		setCardIssuerCountry("Domestic");
		setCardType("Consumer");
		setTransactionType("Onus");
	}

	private void setCardDetails(Fields fields) {
		setCard(fields.get(FieldType.CARD_NUMBER.getName()));
		setCvv(fields.get(FieldType.CVV.getName()));
		String expDate = fields.get(FieldType.CARD_EXP_DT.getName());
		setExpYear(expDate.substring(2, 6));
		setExpMonth(expDate.substring(0, 2));
		setMember(fields.get(FieldType.CUST_NAME.getName()));
	}

	private void setMerchantInformation(Fields fields) {
		setId(fields.get(FieldType.MERCHANT_ID.getName()));
		setPassword(fields.get(FieldType.PASSWORD.getName()));
		setTxnKey(fields.get(FieldType.TXN_KEY.getName()));

		String paymentType = fields.get(FieldType.PAYMENT_TYPE.getName());
		if (paymentType.equals(PaymentType.CREDIT_CARD.getCode()) || paymentType.equals(PaymentType.EMI_CC.getCode())) {
			setType("C");
		} else if (paymentType.equals(PaymentType.DEBIT_CARD.getCode())
				|| paymentType.equals(PaymentType.EMI_DC.getCode())) {
			setType("D");
		} else {
			setType("UPI_VPA");
		}
	}

	@SuppressWarnings("incomplete-switch")
	private void setAction(Fields fields) {
		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (txnType.equals(TransactionType.ENROLL.getName())) {
			txnType = fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName());
		}

		String action = null;
		switch (TransactionType.getInstance(txnType)) {
		case AUTHORISE:
			action = "4";
			break;
		case REFUND:
			action = "2";
			break;
		case SALE:
			action = "1";
			break;
		case CAPTURE:
			break;
		case STATUS:
			action = "8";
			break;
		}

		setAction(action);
	}

	public void setRefund(Fields fields) {
		setAction(fields);
	}

	public void setStatusEnquiry(Fields fields) {
		setAction(fields);
		setMerchantInformation(fields);
		String paymentType = fields.get(FieldType.PAYMENT_TYPE.getName());
		if (paymentType.equals(PaymentType.CREDIT_CARD.getCode())) {
			setType("C");
		} else if (paymentType.equals(PaymentType.DEBIT_CARD.getCode())) {
			setType("D");
		} else {
			setType("UPI_VPA");
		}
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getExpMonth() {
		return expMonth;
	}

	public void setExpMonth(String expMonth) {
		this.expMonth = expMonth;
	}

	public String getExpYear() {
		return expYear;
	}

	public void setExpYear(String expYear) {
		this.expYear = expYear;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCard() {
		return card;
	}

	public void setCard(String card) {
		this.card = card;
	}

	public String getCvv() {
		return cvv;
	}

	public void setCvv(String cvv) {
		this.cvv = cvv;
	}

	public String getMember() {
		return member;
	}

	public void setMember(String member) {
		this.member = member;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTxnKey() {
		return txnKey;
	}

	public void setTxnKey(String txnKey) {
		this.txnKey = txnKey;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getError_text() {
		return error_text;
	}

	public void setError_text(String error_text) {
		this.error_text = error_text;
	}

	public String getAvr() {
		return avr;
	}

	public void setAvr(String avr) {
		this.avr = avr;
	}

	public String getError_code_tag() {
		return error_code_tag;
	}

	public void setError_code_tag(String error_code_tag) {
		this.error_code_tag = error_code_tag;
	}

	public String getPaymentid() {
		return paymentid;
	}

	public void setPaymentid(String paymentid) {
		this.paymentid = paymentid;
	}

	public String getTrackId() {
		return trackId;
	}

	public void setTrackId(String trackId) {
		this.trackId = trackId;
	}

	public String getError_service_tag() {
		return error_service_tag;
	}

	public void setError_service_tag(String error_service_tag) {
		this.error_service_tag = error_service_tag;
	}

	public String getTranId() {
		return tranId;
	}

	public void setTranId(String tranId) {
		this.tranId = tranId;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getCardIssuerCountry() {
		return cardIssuerCountry;
	}

	public void setCardIssuerCountry(String cardIssuerCountry) {
		this.cardIssuerCountry = cardIssuerCountry;
	}

}
