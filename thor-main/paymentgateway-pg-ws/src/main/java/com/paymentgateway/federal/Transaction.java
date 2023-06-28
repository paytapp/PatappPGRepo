package com.paymentgateway.federal;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;

@Service("federalTransaction")
public class Transaction {
	
	private String pan;
	private String expiry;
	private String merchant_id;
	private String currency;
	private String currency_exponent;
	private String display_amount;
	private String purchase_amount;
	private String order_desc;
	private String md;
	private String xid;
	private String recur_frequency;
	private String recur_expiry;
	private String installments;
	private String device_category;
	private String message_hash;
	private String ID;
	private String eci;
	private String cavv;
	private String status;
	private String mpiErrorCode;
	private String mpiErrorDescription;
	
	private String pg_instance_id;
	private String perform;
	private String exp_date_yyyy;
	private String exp_date_mm;
	private String cvv2;
	private String name_on_card;
	private String email;
	private String currency_code;
	private String amount;
	private String merchant_reference_no;
	private String customer_device_id;
	
	private String mpiId;
	private String transaction_id;
	private String pg_error_code;
	private String pg_error_detail;
	private String approval_code;
	private String PAReq;
	private String PARes;
	private String expmonth;
	private String expyear;
	private String card;
	private String amt;
	private String totalAmt;
	private String currencycode;
	private String id;
	private String password;
	
	private String responseCode;
	private String response;
	private String approvalTime;
	private String payerAddress;
	private String payerApprovalNum;
	private String payeeAddress;
	private String payeeApprovalNum;

	public void setEnrollment(Fields fields) {
		
		setCardDetails(fields);
		setMerchantInformation(fields);
		setOrderInformation(fields);
	}

	private void setMerchantInformation(Fields fields) {
		setId(fields.get(FieldType.MERCHANT_ID.getName()));
	}	

	private void setOrderInformation(Fields fields) {
		/*String addSurchargeFlag = PropertiesManager.propertiesMap.get("FEDERAL_ADD_SURCHARGE");
		String amount = null;
		if (addSurchargeFlag.equals("Y")) {
			amount = fields.get(FieldType.TOTAL_AMOUNT.getName());
		} else {
			amount = fields.get(FieldType.AMOUNT.getName());
		}*/

		setAmt(fields.get(FieldType.AMOUNT.getName()));
		setTotalAmt(fields.get(FieldType.TOTAL_AMOUNT.getName()));
		setCurrencycode(fields.get(FieldType.CURRENCY_CODE.getName()));
		setOrder_desc(fields.get(FieldType.PRODUCT_DESC.getName()));	
		setMd(fields.get(FieldType.TXN_ID.getName()));
	}

	private void setCardDetails(Fields fields) {
		setCard(fields.get(FieldType.CARD_NUMBER.getName()));
		setCvv2(fields.get(FieldType.CVV.getName()));
		String expDate = fields.get(FieldType.CARD_EXP_DT.getName());
		setExpyear(expDate.substring(4, 6));
		setExpmonth(expDate.substring(0, 2));
	}
	
	public void setAuthorization(Fields fields){
		setCard(fields.get(FieldType.CARD_NUMBER.getName()));
		setCvv2(fields.get(FieldType.CVV.getName()));
		String expDate = fields.get(FieldType.CARD_EXP_DT.getName());
		setExpyear(expDate.substring(4, 6));
		setExpmonth(expDate.substring(0, 2));
		
		setAmt(fields.get(FieldType.AMOUNT.getName()));
		setTotalAmt(fields.get(FieldType.TOTAL_AMOUNT.getName()));
		
	}

	public String getPan() {
		return pan;
	}

	public void setPan(String pan) {
		this.pan = pan;
	}

	public String getExpiry() {
		return expiry;
	}

	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}

	public String getMerchant_id() {
		return merchant_id;
	}

	public void setMerchant_id(String merchant_id) {
		this.merchant_id = merchant_id;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getCurrency_exponent() {
		return currency_exponent;
	}

	public void setCurrency_exponent(String currency_exponent) {
		this.currency_exponent = currency_exponent;
	}

	public String getDisplay_amount() {
		return display_amount;
	}

	public void setDisplay_amount(String display_amount) {
		this.display_amount = display_amount;
	}

	public String getPurchase_amount() {
		return purchase_amount;
	}

	public void setPurchase_amount(String purchase_amount) {
		this.purchase_amount = purchase_amount;
	}

	public String getOrder_desc() {
		return order_desc;
	}

	public void setOrder_desc(String order_desc) {
		this.order_desc = order_desc;
	}

	public String getMd() {
		return md;
	}

	public void setMd(String md) {
		this.md = md;
	}

	public String getXid() {
		return xid;
	}

	public void setXid(String xid) {
		this.xid = xid;
	}

	public String getRecur_frequency() {
		return recur_frequency;
	}

	public void setRecur_frequency(String recur_frequency) {
		this.recur_frequency = recur_frequency;
	}

	public String getRecur_expiry() {
		return recur_expiry;
	}

	public void setRecur_expiry(String recur_expiry) {
		this.recur_expiry = recur_expiry;
	}

	public String getInstallments() {
		return installments;
	}

	public void setInstallments(String installments) {
		this.installments = installments;
	}

	public String getDevice_category() {
		return device_category;
	}

	public void setDevice_category(String device_category) {
		this.device_category = device_category;
	}

	public String getMessage_hash() {
		return message_hash;
	}

	public void setMessage_hash(String message_hash) {
		this.message_hash = message_hash;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public String getEci() {
		return eci;
	}

	public void setEci(String eci) {
		this.eci = eci;
	}

	public String getCavv() {
		return cavv;
	}

	public void setCavv(String cavv) {
		this.cavv = cavv;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMpiErrorCode() {
		return mpiErrorCode;
	}

	public void setMpiErrorCode(String mpiErrorCode) {
		this.mpiErrorCode = mpiErrorCode;
	}

	public String getMpiErrorDescription() {
		return mpiErrorDescription;
	}

	public void setMpiErrorDescription(String mpiErrorDescription) {
		this.mpiErrorDescription = mpiErrorDescription;
	}

	public String getPg_instance_id() {
		return pg_instance_id;
	}

	public void setPg_instance_id(String pg_instance_id) {
		this.pg_instance_id = pg_instance_id;
	}

	public String getPerform() {
		return perform;
	}

	public void setPerform(String perform) {
		this.perform = perform;
	}

	public String getExp_date_yyyy() {
		return exp_date_yyyy;
	}

	public void setExp_date_yyyy(String exp_date_yyyy) {
		this.exp_date_yyyy = exp_date_yyyy;
	}

	public String getExp_date_mm() {
		return exp_date_mm;
	}

	public void setExp_date_mm(String exp_date_mm) {
		this.exp_date_mm = exp_date_mm;
	}

	public String getCvv2() {
		return cvv2;
	}

	public void setCvv2(String cvv2) {
		this.cvv2 = cvv2;
	}

	public String getName_on_card() {
		return name_on_card;
	}

	public void setName_on_card(String name_on_card) {
		this.name_on_card = name_on_card;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCurrency_code() {
		return currency_code;
	}

	public void setCurrency_code(String currency_code) {
		this.currency_code = currency_code;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getMerchant_reference_no() {
		return merchant_reference_no;
	}

	public void setMerchant_reference_no(String merchant_reference_no) {
		this.merchant_reference_no = merchant_reference_no;
	}

	public String getCustomer_device_id() {
		return customer_device_id;
	}

	public void setCustomer_device_id(String customer_device_id) {
		this.customer_device_id = customer_device_id;
	}

	public String getMpiId() {
		return mpiId;
	}

	public void setMpiId(String mpiId) {
		this.mpiId = mpiId;
	}

	public String getTransaction_id() {
		return transaction_id;
	}

	public void setTransaction_id(String transaction_id) {
		this.transaction_id = transaction_id;
	}

	public String getPg_error_code() {
		return pg_error_code;
	}

	public void setPg_error_code(String pg_error_code) {
		this.pg_error_code = pg_error_code;
	}

	public String getPg_error_detail() {
		return pg_error_detail;
	}

	public void setPg_error_detail(String pg_error_detail) {
		this.pg_error_detail = pg_error_detail;
	}

	public String getApproval_code() {
		return approval_code;
	}

	public void setApproval_code(String approval_code) {
		this.approval_code = approval_code;
	}

	public String getPAReq() {
		return PAReq;
	}

	public void setPAReq(String pAReq) {
		PAReq = pAReq;
	}

	public String getPARes() {
		return PARes;
	}

	public void setPARes(String pARes) {
		PARes = pARes;
	}

	public String getExpmonth() {
		return expmonth;
	}

	public void setExpmonth(String expmonth) {
		this.expmonth = expmonth;
	}

	public String getExpyear() {
		return expyear;
	}

	public void setExpyear(String expyear) {
		this.expyear = expyear;
	}

	public String getCard() {
		return card;
	}

	public void setCard(String card) {
		this.card = card;
	}

	public String getCurrencycode() {
		return currencycode;
	}

	public void setCurrencycode(String currencycode) {
		this.currencycode = currencycode;
	}

	public String getAmt() {
		return amt;
	}

	public void setAmt(String amt) {
		this.amt = amt;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getApprovalTime() {
		return approvalTime;
	}

	public void setApprovalTime(String approvalTime) {
		this.approvalTime = approvalTime;
	}

	public String getPayerAddress() {
		return payerAddress;
	}

	public void setPayerAddress(String payerAddress) {
		this.payerAddress = payerAddress;
	}

	public String getPayerApprovalNum() {
		return payerApprovalNum;
	}

	public void setPayerApprovalNum(String payerApprovalNum) {
		this.payerApprovalNum = payerApprovalNum;
	}

	public String getPayeeAddress() {
		return payeeAddress;
	}

	public void setPayeeAddress(String payeeAddress) {
		this.payeeAddress = payeeAddress;
	}

	public String getPayeeApprovalNum() {
		return payeeApprovalNum;
	}

	public void setPayeeApprovalNum(String payeeApprovalNum) {
		this.payeeApprovalNum = payeeApprovalNum;
	}

	public String getTotalAmt() {
		return totalAmt;
	}

	public void setTotalAmt(String totalAmt) {
		this.totalAmt = totalAmt;
	}


}
