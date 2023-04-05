package com.paymentgateway.commons.user;

/**
 * @author Amitosh Aanand
 *
 */
public class EPOSTransaction {

	private String _id;
	private String INVOICE_ID;
	private String REFUND_ORDER_ID;
	private String CUST_NAME;
	private String CUST_MOBILE;
	private String CUST_EMAIL;
	private String AMOUNT;
	private String TOTAL_AMOUNT;
	private String CURRENCY_CODE;
	private String EPOS_PAYMENT_OPTION;
	private String PAYMENT_TYPE;
	private String MOP_TYPE;
	private String ORIG_TXNTYPE;
	private String SALT_KEY; // Remove from EPOS
	private String STATUS; // Remove from EPOS
	private String PAY_ID;
	private String PAYMENT_URL;
	private String RETURN_URL;
	private String SHORT_URL;
	private String BUSINESS_NAME;
	private String CREATED_BY;
	private String CREATE_DATE;
	private String EXPIRY_DATE;
	private String REMARKS;

	// udf fields

	private String UDF11;
	private String UDF12;
	private String UDF13;
	private String UDF14;
	private String UDF15;
	private String UDF16;
	private String UDF17;
	private String UDF18;

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getINVOICE_ID() {
		return INVOICE_ID;
	}

	public void setINVOICE_ID(String iNVOICE_ID) {
		INVOICE_ID = iNVOICE_ID;
	}

	public String getREFUND_ORDER_ID() {
		return REFUND_ORDER_ID;
	}

	public void setREFUND_ORDER_ID(String rEFUND_ORDER_ID) {
		REFUND_ORDER_ID = rEFUND_ORDER_ID;
	}

	public String getCUST_NAME() {
		return CUST_NAME;
	}

	public void setCUST_NAME(String cUST_NAME) {
		CUST_NAME = cUST_NAME;
	}

	public String getCUST_MOBILE() {
		return CUST_MOBILE;
	}

	public void setCUST_MOBILE(String cUST_MOBILE) {
		CUST_MOBILE = cUST_MOBILE;
	}

	public String getCUST_EMAIL() {
		return CUST_EMAIL;
	}

	public void setCUST_EMAIL(String cUST_EMAIL) {
		CUST_EMAIL = cUST_EMAIL;
	}

	public String getAMOUNT() {
		return AMOUNT;
	}

	public void setAMOUNT(String aMOUNT) {
		AMOUNT = aMOUNT;
	}

	public String getTOTAL_AMOUNT() {
		return TOTAL_AMOUNT;
	}

	public void setTOTAL_AMOUNT(String tOTAL_AMOUNT) {
		TOTAL_AMOUNT = tOTAL_AMOUNT;
	}

	public String getCURRENCY_CODE() {
		return CURRENCY_CODE;
	}

	public void setCURRENCY_CODE(String cURRENCY_CODE) {
		CURRENCY_CODE = cURRENCY_CODE;
	}

	public String getEPOS_PAYMENT_OPTION() {
		return EPOS_PAYMENT_OPTION;
	}

	public void setEPOS_PAYMENT_OPTION(String ePOS_PAYMENT_OPTION) {
		EPOS_PAYMENT_OPTION = ePOS_PAYMENT_OPTION;
	}

	public String getPAYMENT_TYPE() {
		return PAYMENT_TYPE;
	}

	public void setPAYMENT_TYPE(String pAYMENT_TYPE) {
		PAYMENT_TYPE = pAYMENT_TYPE;
	}

	public String getMOP_TYPE() {
		return MOP_TYPE;
	}

	public void setMOP_TYPE(String mOP_TYPE) {
		MOP_TYPE = mOP_TYPE;
	}

	public String getORIG_TXNTYPE() {
		return ORIG_TXNTYPE;
	}

	public void setORIG_TXNTYPE(String oRIG_TXNTYPE) {
		ORIG_TXNTYPE = oRIG_TXNTYPE;
	}

	public String getSALT_KEY() {
		return SALT_KEY;
	}

	public void setSALT_KEY(String sALT_KEY) {
		SALT_KEY = sALT_KEY;
	}

	public String getSTATUS() {
		return STATUS;
	}

	public void setSTATUS(String sTATUS) {
		STATUS = sTATUS;
	}

	public String getPAY_ID() {
		return PAY_ID;
	}

	public void setPAY_ID(String pAY_ID) {
		PAY_ID = pAY_ID;
	}

	public String getPAYMENT_URL() {
		return PAYMENT_URL;
	}

	public void setPAYMENT_URL(String pAYMENT_URL) {
		PAYMENT_URL = pAYMENT_URL;
	}

	public String getRETURN_URL() {
		return RETURN_URL;
	}

	public void setRETURN_URL(String rETURN_URL) {
		RETURN_URL = rETURN_URL;
	}

	public String getSHORT_URL() {
		return SHORT_URL;
	}

	public void setSHORT_URL(String sHORT_URL) {
		SHORT_URL = sHORT_URL;
	}

	public String getBUSINESS_NAME() {
		return BUSINESS_NAME;
	}

	public void setBUSINESS_NAME(String bUSINESS_NAME) {
		BUSINESS_NAME = bUSINESS_NAME;
	}

	public String getCREATED_BY() {
		return CREATED_BY;
	}

	public void setCREATED_BY(String cREATED_BY) {
		CREATED_BY = cREATED_BY;
	}

	public String getCREATE_DATE() {
		return CREATE_DATE;
	}

	public void setCREATE_DATE(String cREATE_DATE) {
		CREATE_DATE = cREATE_DATE;
	}

	public String getEXPIRY_DATE() {
		return EXPIRY_DATE;
	}

	public void setEXPIRY_DATE(String eXPIRY_DATE) {
		EXPIRY_DATE = eXPIRY_DATE;
	}

	public String getREMARKS() {
		return REMARKS;
	}

	public void setREMARKS(String rEMARKS) {
		REMARKS = rEMARKS;
	}

	public String getUDF11() {
		return UDF11;
	}

	public void setUDF11(String uDF11) {
		UDF11 = uDF11;
	}

	public String getUDF12() {
		return UDF12;
	}

	public void setUDF12(String uDF12) {
		UDF12 = uDF12;
	}

	public String getUDF13() {
		return UDF13;
	}

	public void setUDF13(String uDF13) {
		UDF13 = uDF13;
	}

	public String getUDF14() {
		return UDF14;
	}

	public void setUDF14(String uDF14) {
		UDF14 = uDF14;
	}

	public String getUDF15() {
		return UDF15;
	}

	public void setUDF15(String uDF15) {
		UDF15 = uDF15;
	}

	public String getUDF16() {
		return UDF16;
	}

	public void setUDF16(String uDF16) {
		UDF16 = uDF16;
	}

	public String getUDF17() {
		return UDF17;
	}

	public void setUDF17(String uDF17) {
		UDF17 = uDF17;
	}

	public String getUDF18() {
		return UDF18;
	}

	public void setUDF18(String uDF18) {
		UDF18 = uDF18;
	}
}