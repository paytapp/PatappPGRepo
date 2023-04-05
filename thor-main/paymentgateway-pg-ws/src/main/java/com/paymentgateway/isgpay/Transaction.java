package com.paymentgateway.isgpay;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Rahul
 *
 */

@Service("iSGPayTransaction")
public class Transaction {

	// Merchant Details

	private String merchantId;
	private String terminalId;
	private String encryptionKey;
	private String salt;
	private String bankId;
	private String mcc;
	private String accessCode;
	private String version;

	// Transaction Details Data
	private String encData;
	private String amount;
	private String refundAmount;
	private String returnUrl;
	private String currency;
	private String txnType;
	private String payOpt;
	private String txnRefNum;

	// Payment Details Data
	private String cardNumber;
	private String expDate;
	private String cvv;
	private String cardHolderName;
	private String cardType;
	private String customerMobileNumber;
	private String vpa;

	// Response parameters

	private String responseCode;
	private String message;
	private String pgTxnId;
	private String TxnRefNo;
	private String hashValidated;
	private String IsgTransactionType;
	private String ENROLLED;
	private String Stan;
	private String NewTransactionId;
	private String TxnId;
	private String UCAP;
	private String MessageType;
	private String TransactionDate;
	private String AuthCode;
	private String PosEntryMode;
	private String RetRefNo;
	private String TransactionTime;
	private String AuthStatus;
	private String Status;

	private String ccAuthReply_processorResponse;

	public void setEnrollment(Fields fields, String rupayFlag) {
		setMerchantInformation(fields, rupayFlag);
		setTxnDataDetails(fields);
	}

	private void setMerchantInformation(Fields fields, String rupayFlag) {

		if (fields.get(FieldType.MOP_TYPE.getName()).equalsIgnoreCase(MopType.RUPAY.getCode())
				&& StringUtils.isNotBlank(rupayFlag) && rupayFlag.equalsIgnoreCase("Y")) {
			setMerchantId(fields.get(FieldType.ADF6.getName()));
			setSalt(fields.get(FieldType.ADF7.getName()));
			setEncryptionKey(fields.get(FieldType.ADF8.getName()));
			setBankId(fields.get(FieldType.ADF1.getName()));
			setTerminalId(fields.get(FieldType.ADF9.getName()));
			setMcc(fields.get(FieldType.ADF10.getName()));
			setAccessCode(fields.get(FieldType.ADF11.getName()));
			setVersion("1");
		} else {
			setMerchantId(fields.get(FieldType.MERCHANT_ID.getName()));
			setSalt(fields.get(FieldType.PASSWORD.getName()));
			setEncryptionKey(fields.get(FieldType.TXN_KEY.getName()));
			setBankId(fields.get(FieldType.ADF1.getName()));
			setTerminalId(fields.get(FieldType.ADF2.getName()));
			setMcc(fields.get(FieldType.ADF3.getName()));
			setAccessCode(fields.get(FieldType.ADF4.getName()));
			setVersion("1");
		}

	}

	private void setTxnDataDetails(Fields fields) {

		setTxnRefNum(fields.get(FieldType.PG_REF_NUM.getName()));
		setAmount(fields.get(FieldType.TOTAL_AMOUNT.getName()));
		/*
		 * String returnUrl =
		 * PropertiesManager.propertiesMap.get(Constants.ISGPAY_RETURN_URL); returnUrl =
		 * returnUrl + fields.get(FieldType.PG_REF_NUM.getName());
		 * setReturnUrl(returnUrl);
		 */
		setCurrency(fields.get(FieldType.CURRENCY_CODE.getName()));
		if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())) {
			setPayOpt(Constants.PAY_OPT_CC_VAL);
		} else if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())) {
			setPayOpt(Constants.PAY_OPT_DC_VAL);
		} else if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.NET_BANKING.getCode())) {
			setPayOpt(Constants.PAY_OPT_NB_VAL);
			// setPayOpt(ISGPayBankCode.getBankCode(fields.get(FieldType.MOP_TYPE.getName())));
		} else if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.UPI.getCode())) {
			setPayOpt(Constants.PAY_OPT_UPI_VAL);
		}

		if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.SALE.getCode())) {
			setTxnType(Constants.ISGPAY_TXN_TYPE_PAY_VALUE);
		}

		setCardNumber(fields.get(FieldType.CARD_NUMBER.getName()));
		setCvv(fields.get(FieldType.CVV.getName()));
		setExpDate(fields.get(FieldType.CARD_EXP_DT.getName()));
	}

	public void setStatusEnquiry(Fields fields, String rupayFlag) {

		setMerchantId(fields.get(FieldType.ADF6.getName()));
		setSalt(fields.get(FieldType.ADF7.getName()));
		setBankId(fields.get(FieldType.ADF1.getName()));
		setTerminalId(fields.get(FieldType.ADF9.getName()));
		setTxnRefNum(fields.get(FieldType.PG_REF_NUM.getName()));
		setAccessCode(fields.get(FieldType.ADF11.getName()));
		setTxnType("Status");

	}

	public void setRefund(Fields fields, String rupayFlag) {

		if (fields.get(FieldType.MOP_TYPE.getName()).equalsIgnoreCase(MopType.RUPAY.getCode())
				&& StringUtils.isNotBlank(rupayFlag) && rupayFlag.equalsIgnoreCase("Y")) {

			setMerchantId(fields.get(FieldType.ADF6.getName()));
			setSalt(fields.get(FieldType.ADF7.getName()));
			setBankId(fields.get(FieldType.ADF1.getName()));
			setTerminalId(fields.get(FieldType.ADF9.getName()));
			setRefundAmount(fields.get(FieldType.TOTAL_AMOUNT.getName()));
			setTxnRefNum(fields.get(FieldType.PG_REF_NUM.getName()));
			setRetRefNo(fields.get(FieldType.RRN.getName()));
			setAuthCode(fields.get(FieldType.AUTH_CODE.getName()));
			setAccessCode(fields.get(FieldType.ADF11.getName()));
			setTxnType("Refund");
		} else {
			setMerchantId(fields.get(FieldType.MERCHANT_ID.getName()));
			setSalt(fields.get(FieldType.PASSWORD.getName()));
			setBankId(fields.get(FieldType.ADF1.getName()));
			setTerminalId(fields.get(FieldType.ADF2.getName()));
			setRefundAmount(fields.get(FieldType.TOTAL_AMOUNT.getName()));
			setTxnRefNum(fields.get(FieldType.PG_REF_NUM.getName()));
			if (fields.get(FieldType.MOP_TYPE.getName()).equalsIgnoreCase(MopType.VISA.getCode())) {
				setRetRefNo(fields.get(FieldType.ACQ_ID.getName()));
			} else {
				setRetRefNo(fields.get(FieldType.RRN.getName()));
			}

			setAuthCode(fields.get(FieldType.AUTH_CODE.getName()));
			setAccessCode(fields.get(FieldType.ADF4.getName()));
			setTxnType("Refund");
		}

	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getCvv() {
		return cvv;
	}

	public void setCvv(String cvv) {
		this.cvv = cvv;
	}

	public String getCardHolderName() {
		return cardHolderName;
	}

	public void setCardHolderName(String cardHolderName) {
		this.cardHolderName = cardHolderName;
	}

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public String getCustomerMobileNumber() {
		return customerMobileNumber;
	}

	public void setCustomerMobileNumber(String customerMobileNumber) {
		this.customerMobileNumber = customerMobileNumber;
	}

	public String getVpa() {
		return vpa;
	}

	public void setVpa(String vpa) {
		this.vpa = vpa;
	}

	public String getEncryptionKey() {
		return encryptionKey;
	}

	public void setEncryptionKey(String encryptionKey) {
		this.encryptionKey = encryptionKey;
	}

	public String getTerminalId() {
		return terminalId;
	}

	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public String getBankId() {
		return bankId;
	}

	public void setBankId(String bankId) {
		this.bankId = bankId;
	}

	public String getMcc() {
		return mcc;
	}

	public void setMcc(String mcc) {
		this.mcc = mcc;
	}

	public String getAccessCode() {
		return accessCode;
	}

	public void setAccessCode(String accessCode) {
		this.accessCode = accessCode;
	}

	public String getEncData() {
		return encData;
	}

	public void setEncData(String encData) {
		this.encData = encData;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getPayOpt() {
		return payOpt;
	}

	public void setPayOpt(String payOpt) {
		this.payOpt = payOpt;
	}

	public String getTxnRefNum() {
		return txnRefNum;
	}

	public void setTxnRefNum(String txnRefNum) {
		this.txnRefNum = txnRefNum;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getExpDate() {
		return expDate;
	}

	public void setExpDate(String expDate) {
		this.expDate = expDate;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPgTxnId() {
		return pgTxnId;
	}

	public void setPgTxnId(String pgTxnId) {
		this.pgTxnId = pgTxnId;
	}

	public String getTxnRefNo() {
		return TxnRefNo;
	}

	public void setTxnRefNo(String txnRefNo) {
		TxnRefNo = txnRefNo;
	}

	public String getHashValidated() {
		return hashValidated;
	}

	public void setHashValidated(String hashValidated) {
		this.hashValidated = hashValidated;
	}

	public String getIsgTransactionType() {
		return IsgTransactionType;
	}

	public void setIsgTransactionType(String isgTransactionType) {
		IsgTransactionType = isgTransactionType;
	}

	public String getENROLLED() {
		return ENROLLED;
	}

	public void setENROLLED(String eNROLLED) {
		ENROLLED = eNROLLED;
	}

	public String getStan() {
		return Stan;
	}

	public void setStan(String stan) {
		Stan = stan;
	}

	public String getNewTransactionId() {
		return NewTransactionId;
	}

	public void setNewTransactionId(String newTransactionId) {
		NewTransactionId = newTransactionId;
	}

	public String getTxnId() {
		return TxnId;
	}

	public void setTxnId(String txnId) {
		TxnId = txnId;
	}

	public String getUCAP() {
		return UCAP;
	}

	public void setUCAP(String uCAP) {
		UCAP = uCAP;
	}

	public String getMessageType() {
		return MessageType;
	}

	public void setMessageType(String messageType) {
		MessageType = messageType;
	}

	public String getTransactionDate() {
		return TransactionDate;
	}

	public void setTransactionDate(String transactionDate) {
		TransactionDate = transactionDate;
	}

	public String getAuthCode() {
		return AuthCode;
	}

	public void setAuthCode(String authCode) {
		AuthCode = authCode;
	}

	public String getPosEntryMode() {
		return PosEntryMode;
	}

	public void setPosEntryMode(String posEntryMode) {
		PosEntryMode = posEntryMode;
	}

	public String getRetRefNo() {
		return RetRefNo;
	}

	public void setRetRefNo(String retRefNo) {
		RetRefNo = retRefNo;
	}

	public String getTransactionTime() {
		return TransactionTime;
	}

	public void setTransactionTime(String transactionTime) {
		TransactionTime = transactionTime;
	}

	public String getAuthStatus() {
		return AuthStatus;
	}

	public void setAuthStatus(String authStatus) {
		AuthStatus = authStatus;
	}

	public String getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(String refundAmount) {
		this.refundAmount = refundAmount;
	}

	public String getStatus() {
		return Status;
	}

	public void setStatus(String status) {
		Status = status;
	}

	public String getCcAuthReply_processorResponse() {
		return ccAuthReply_processorResponse;
	}

	public void setCcAuthReply_processorResponse(String ccAuthReply_processorResponse) {
		this.ccAuthReply_processorResponse = ccAuthReply_processorResponse;
	}

}
