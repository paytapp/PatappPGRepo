package com.paymentgateway.idbi;

import java.io.Serializable;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;

@Service("idbiTransaction")
public class Transaction implements Serializable {
	
private static final long serialVersionUID = 1L;
	
	private String mid;
	private String orderId;
	private String trnAmt;
	private String currency;
	private String trnRemarks;
	private String meTransReqType;
	private String cardNo;
	private String resUrl;
	private String reqMsg;
	private String statusDesc;
	private String enckey;
	private String txnType;
	private String responseCode;
	private String acqId;
	private String message;
	private String authCode;
	
	private String pgMeTrnRefNo;
	private String authNStatus;
	private String authZStatus;
	private String captureStatus;
	private String rrn; 
	private String trnReqDate;
	private String statusCode;
	private String addField1;
	private String addField2;
	private String addField3;
	private String addField4;
	private String addField5;
	private String addField6;
	private String addField7;
	private String addField8;
	private String addField9;
	private String addField10;
	
	
	//Additional for ME Capture
	 private String  payTypeCode;
	 private String  cardNumber;
	 private String expiryDate;	
	 private String  nameOnCard; 
	 private String  cvv;
	 
	//Additional for SKU
		private String shippingCharges;
		private String taxAmount;
		private String grossTrnAmt;

		
	//Additional to Get Transaction Status by Merchant
		 private String cancelRefundFlag;
		 private String refundAmt;
	     private String refundStatusDesc;
		 private String netBankCode;
	 
	 
	 public void setEnrollment(Fields fields) {
			setCardDetails(fields);
			setCurrency(fields);
			setPaymentType(fields);
		}
	 
	 public void setSaleDetails(Fields fields) {
			setCurrency(fields);
			setPaymentType(fields);
		}
	 
	 private void setCardDetails(Fields fields) {
		    setNameOnCard(fields.get(FieldType.CUST_NAME.getName()));
			setCardNumber(fields.get(FieldType.CARD_NUMBER.getName()));
			setCvv(fields.get(FieldType.CVV.getName()));
			String expDate = fields.get(FieldType.CARD_EXP_DT.getName());
			String expYear = (expDate.substring(4, 6));
			String expMonth = (expDate.substring(0, 2));
			setExpiryDate(expMonth.concat(expYear));
		}
		
		
		public void setCurrency(Fields fields) {
			String currencyCode =fields.get(FieldType.CURRENCY_CODE.getName());
			String currency = Currency.getAlphabaticCode(currencyCode);
			setCurrency(currency);	
		}
	 
	 
		public void setPaymentType(Fields fields) {
			String paymentType = fields.get(FieldType.PAYMENT_TYPE.getName());
			if(paymentType.equals(PaymentType.CREDIT_CARD.getCode())){
				setPayTypeCode("CC");
			}else if(paymentType.equals(PaymentType.DEBIT_CARD.getCode())){
				setPayTypeCode("DC");
			}else if(paymentType.equals(PaymentType.NET_BANKING.getCode())){
				setPayTypeCode("NB");
			}else{
			}
			
		}
		
	 
	public String getRefundStatusDesc() {
			return refundStatusDesc;
		}

		public void setRefundStatusDesc(String refundStatusDesc) {
			this.refundStatusDesc = refundStatusDesc;
		}

	public String getAuthNStatus() {
			return authNStatus;
		}

		public void setAuthNStatus(String authNStatus) {
			this.authNStatus = authNStatus;
		}

		public String getAuthZStatus() {
			return authZStatus;
		}

		public void setAuthZStatus(String authZStatus) {
			this.authZStatus = authZStatus;
		}

		public String getCaptureStatus() {
			return captureStatus;
		}

		public void setCaptureStatus(String captureStatus) {
			this.captureStatus = captureStatus;
		}

		public String getRrn() {
			return rrn;
		}

		public void setRrn(String rrn) {
			this.rrn = rrn;
		}

		public String getTrnReqDate() {
			return trnReqDate;
		}

		public void setTrnReqDate(String trnReqDate) {
			this.trnReqDate = trnReqDate;
		}

		public String getStatusCode() {
			return statusCode;
		}

		public void setStatusCode(String statusCode) {
			this.statusCode = statusCode;
		}

		public String getAddField1() {
			return addField1;
		}

		public void setAddField1(String addField1) {
			this.addField1 = addField1;
		}

		public String getAddField2() {
			return addField2;
		}

		public void setAddField2(String addField2) {
			this.addField2 = addField2;
		}

		public String getAddField3() {
			return addField3;
		}

		public void setAddField3(String addField3) {
			this.addField3 = addField3;
		}

		public String getAddField4() {
			return addField4;
		}

		public void setAddField4(String addField4) {
			this.addField4 = addField4;
		}

		public String getAddField5() {
			return addField5;
		}

		public void setAddField5(String addField5) {
			this.addField5 = addField5;
		}

		public String getAddField6() {
			return addField6;
		}

		public void setAddField6(String addField6) {
			this.addField6 = addField6;
		}

		public String getAddField7() {
			return addField7;
		}

		public void setAddField7(String addField7) {
			this.addField7 = addField7;
		}

		public String getAddField8() {
			return addField8;
		}

		public void setAddField8(String addField8) {
			this.addField8 = addField8;
		}

		public String getAddField9() {
			return addField9;
		}

		public void setAddField9(String addField9) {
			this.addField9 = addField9;
		}

		public String getAddField10() {
			return addField10;
		}

		public void setAddField10(String addField10) {
			this.addField10 = addField10;
		}

	public String getAuthCode() {
			return authCode;
		}

		public void setAuthCode(String authCode) {
			this.authCode = authCode;
		}

	public String getShippingCharges() {
			return shippingCharges;
		}

		public void setShippingCharges(String shippingCharges) {
			this.shippingCharges = shippingCharges;
		}

		public String getTaxAmount() {
			return taxAmount;
		}

		public void setTaxAmount(String taxAmount) {
			this.taxAmount = taxAmount;
		}

		public String getGrossTrnAmt() {
			return grossTrnAmt;
		}

		public void setGrossTrnAmt(String grossTrnAmt) {
			this.grossTrnAmt = grossTrnAmt;
		}

		public String getPgMeTrnRefNo() {
			return pgMeTrnRefNo;
		}

		public void setPgMeTrnRefNo(String pgMeTrnRefNo) {
			this.pgMeTrnRefNo = pgMeTrnRefNo;
		}

		public String getCancelRefundFlag() {
			return cancelRefundFlag;
		}

		public void setCancelRefundFlag(String cancelRefundFlag) {
			this.cancelRefundFlag = cancelRefundFlag;
		}

		public String getRefundAmt() {
			return refundAmt;
		}

		public void setRefundAmt(String refundAmt) {
			this.refundAmt = refundAmt;
		}

		public String getNetBankCode() {
			return netBankCode;
		}

		public void setNetBankCode(String netBankCode) {
			this.netBankCode = netBankCode;
		}

	public String getResponseCode() {
			return responseCode;
		}

		public void setResponseCode(String responseCode) {
			this.responseCode = responseCode;
		}

		public String getAcqId() {
			return acqId;
		}

		public void setAcqId(String acqId) {
			this.acqId = acqId;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

	public String getTxnType() {
			return txnType;
		}

		public void setTxnType(String txnType) {
			this.txnType = txnType;
		}

	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getTrnAmt() {
		return trnAmt;
	}
	public void setTrnAmt(String trnAmt) {
		this.trnAmt = trnAmt;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getTrnRemarks() {
		return trnRemarks;
	}
	public void setTrnRemarks(String trnRemarks) {
		this.trnRemarks = trnRemarks;
	}
	public String getMeTransReqType() {
		return meTransReqType;
	}
	public void setMeTransReqType(String meTransReqType) {
		this.meTransReqType = meTransReqType;
	}
	public String getCardNo() {
		return cardNo;
	}
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	public String getResUrl() {
		return resUrl;
	}
	public void setResUrl(String resUrl) {
		this.resUrl = resUrl;
	}
	public String getReqMsg() {
		return reqMsg;
	}
	public void setReqMsg(String reqMsg) {
		this.reqMsg = reqMsg;
	}
	public String getStatusDesc() {
		return statusDesc;
	}
	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}
	public String getEnckey() {
		return enckey;
	}
	public void setEnckey(String enckey) {
		this.enckey = enckey;
	}
	public String getPayTypeCode() {
		return payTypeCode;
	}
	public void setPayTypeCode(String payTypeCode) {
		this.payTypeCode = payTypeCode;
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
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	 
	
}
