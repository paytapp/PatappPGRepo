package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Proxy;

import com.paymentgateway.commons.util.UserStatusType;

@Entity
@Proxy(lazy = false)
public class UserAudit implements Serializable {

	private static final long serialVersionUID = -2963690929037643384L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String payId;

	@Column(columnDefinition = "TEXT", length = 1000)
	private String industryCategory;
	private String companyName;
	private String typeOfEntity;
	private String cin;
	private String registrationNumber;
	private String dateOfIncorporation;
	private String businessPan;
	private String companyRegisteredAddress;
	private String tradingAddress1;
	private String tradingCountry;
	private String tradingState;
	private String tradingPin;
	private String gstin;
	private String companyPhone;
	private String companyWebsite;
	private String companyEmailId;
	private String businessEmailForCommunication;
	private String contactName;
	private String contactMobile;
	private String contactEmail;
	private String contactLandline;
	private String director1FullName;
	private String director1Pan;
	private String director1Email;
	private String director1Mobile;
	private String director1Landline;
	private String director1Address;
	private String director2FullName;
	private String director2Pan;
	private String director2Email;
	private String director2Mobile;
	private String director2Landline;
	private String director2Address;

	private String mobile;
	private UserStatusType userStatus;

	private String mpaDataUpdatedBy;
	private String mpaDataUpdatedByEmail;
	private String mpaDataUpdatedByUserType;
	private String mpaDataUpdateDate;

	@Column(columnDefinition = "LONGTEXT", length = 100)
	private String merchantSupportEmailId;
	@Column(columnDefinition = "LONGTEXT", length = 100)
	private String merchantSupportMobileNumber;
	@Column(columnDefinition = "LONGTEXT", length = 100)
	private String merchantSupportLandLine;

	private String accountNumber;
	private String accountIfsc;
	private String accountHolderName;
	private String accountMobileNumber;

	private String annualTurnover;
	private String annualTurnoverOnline;
	private String percentageCC;
	private String percentageDC;
	private String percentageDomestic;
	private String percentageInternational;
	private String percentageCD;
	private String percentageNeftOrImpsOrRtgs;
	private String percentageNB;
	private String percentageUP;
	private String percentageWL;
	private String percentageEM;

	private Boolean expressPay;
	private String expressPayParameter;
	private String allowDuplicateSaleOrderId;
	private String allowDuplicateRefundOrderId;
	private String allowDuplicateSaleOrderIdInRefund;
	private String allowDuplicateRefundOrderIdSale;

	@Column(columnDefinition = "TEXT", length = 32)
	private String emailId;
	@Column(columnDefinition = "boolean default false")
	private boolean transactionSmsFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean transactionAuthenticationEmailFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean transactionCustomerEmailFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean refundTransactionCustomerEmailFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean transactionEmailerFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean refundTransactionMerchantEmailFlag = false;
	private String transactionEmailId;
	private String transactionSms;
	@Column(columnDefinition = "boolean default false")
	private boolean eposMerchant = false;

	private String resellerId;
	private String superMerchantId;
	private String terminalId;
	private Date registrationDate;
	private Date activationDate;

	@Column(columnDefinition = "boolean default false")
	private boolean merchantHostedFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean iframePaymentFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean checkOutJsFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean surchargeFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean retryTransactionCustomeFlag = false;
	private String attemptTrasacation;
	@Column(columnDefinition = "boolean default false")
	private boolean expressPayFlag = false;
	private String cardSaveParam;
	@Column(columnDefinition = "boolean default false")
	private boolean saveVPAFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean bookingRecord = false;
	@Column(columnDefinition = "boolean default false")
	private boolean eNachReportFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean upiAutoPayReportFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean acceptPostSettledInEnquiry = false;

	private String vpaSaveParam;
	@Column(columnDefinition = "boolean default false")
	private boolean discountingFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean logoFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean allowLogoInPgPage = false;
	private String logoName;
	private String codName;
	private String paymentMessageSlab;
	@Column(columnDefinition = "boolean default false")
	private boolean allowPartSettle = false;
	@Column(columnDefinition = "boolean default false")
	private boolean allowSubtractValue = false;
	private String deviation;
	@Column(columnDefinition = "boolean default false")
	private boolean customTransactionStatus = false;
	@Column(columnDefinition = "boolean default false")
	private boolean skipOrderIdForRefund = false;
	@Column(columnDefinition = "boolean default false")
	private boolean allowRefundDuplicate = false;
	@Column(columnDefinition = "boolean default false")
	private boolean allowSaleDuplicate = false;
	@Column(columnDefinition = "boolean default false")
	private boolean allowSaleInRefund = false;
	@Column(columnDefinition = "boolean default false")
	private boolean allowRefundInSale = false;
	@Column(columnDefinition = "boolean default false")
	private boolean allowDuplicateNotSaleOrderId = false;
	private float extraRefundLimit;
	private float oneTimeRefundLimit;
	private float RefundLimitRemains;
	@Column(columnDefinition = "boolean default false")
	private boolean LimitChangedFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean sameLimitFlag = false;
	private String mCC;
	@Column(columnDefinition = "boolean default false")
	private boolean retailMerchantFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean capturedMerchantFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean vendorPayOutFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean accountVerificationFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean vpaVerificationFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean loadWalletFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean nodalReportFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean paymentAdviceFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean merchantInitiatedDirectFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean impsFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean virtualAccountFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean customerQrFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean saveNBFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean saveWLFlag = false;
	private String nbSaveParam;
	private String wlSaveParam;
	@Column(columnDefinition = "boolean default false")
	private boolean transactionCustomerSMSFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean transactionMerchantSMSFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean extraRefundAmount = false;
	@Column(columnDefinition = "boolean default false")
	private boolean oneTimeRefundAmount = false;
	@Column(columnDefinition = "TEXT", length = 24)
	private String virtualAccountNo;
	@Column(columnDefinition = "TEXT", length = 24)
	private String virtualIfscCode;
	@Column(columnDefinition = "TEXT", length = 24)
	private String virtualBeneficiaryName;
	@Column(columnDefinition = "TEXT", length = 32)
	private String merchantVPA;

	@Column(columnDefinition = "boolean default false")
	private boolean transactionFailedMerchantSMSFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean transactionFailedCustomerSMSFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean transactionRefundMerchantSMSFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean transactionRefundCustomerSMSFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean transactionFailedMerchantEmailFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean transactionFailedCustomerEmailFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean allowQRScanFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean allowUpiQRFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean netSettledFlag = false;
	@Column(columnDefinition = "TEXT", length = 3)
	private String paymentCycle;
	@Column(columnDefinition = "boolean default false")
	private boolean smtMerchant = false;
	private String customHostedUrl;
	@Column(columnDefinition = "boolean default false")
	private boolean allowCustomHostedUrl = false;
	@Column(columnDefinition = "boolean default false")
	private boolean lyraPay = false;
	@Column(columnDefinition = "boolean default false")
	private boolean resellerMerchantSignupFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean resellerUserStatusFlag = false;
	private String businessName;
	@Column(columnDefinition = "boolean default false")
	private boolean non3dsTxn = false;
	@Column(columnDefinition = "boolean default false")
	private boolean autoRefund = false;
	@Column(columnDefinition = "boolean default false")
	private boolean callBackFlag = false;
	private String callBackUrl;
	@Column(columnDefinition = "boolean default false")
	private boolean topupFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean statementFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean allCallBackFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean allowNodalPayoutFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean allowPayoutUpdateStatus = false;
	@Column(columnDefinition = "boolean default false")
	private boolean allowECollectionFee = false;
	
	@Column(columnDefinition = "TEXT", length = 1000)
	private String whiteListReturnUrl;
	
	@Column(columnDefinition = "boolean default false")
	private boolean whiteListReturnUrlFlag = false;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getIndustryCategory() {
		return industryCategory;
	}

	public void setIndustryCategory(String industryCategory) {
		this.industryCategory = industryCategory;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getTypeOfEntity() {
		return typeOfEntity;
	}

	public void setTypeOfEntity(String typeOfEntity) {
		this.typeOfEntity = typeOfEntity;
	}

	public String getCin() {
		return cin;
	}

	public void setCin(String cin) {
		this.cin = cin;
	}

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}

	public String getDateOfIncorporation() {
		return dateOfIncorporation;
	}

	public void setDateOfIncorporation(String dateOfIncorporation) {
		this.dateOfIncorporation = dateOfIncorporation;
	}

	public String getBusinessPan() {
		return businessPan;
	}

	public void setBusinessPan(String businessPan) {
		this.businessPan = businessPan;
	}

	public String getCompanyRegisteredAddress() {
		return companyRegisteredAddress;
	}

	public void setCompanyRegisteredAddress(String companyRegisteredAddress) {
		this.companyRegisteredAddress = companyRegisteredAddress;
	}

	public String getTradingAddress1() {
		return tradingAddress1;
	}

	public void setTradingAddress1(String tradingAddress1) {
		this.tradingAddress1 = tradingAddress1;
	}

	public String getTradingCountry() {
		return tradingCountry;
	}

	public void setTradingCountry(String tradingCountry) {
		this.tradingCountry = tradingCountry;
	}

	public String getTradingState() {
		return tradingState;
	}

	public void setTradingState(String tradingState) {
		this.tradingState = tradingState;
	}

	public String getTradingPin() {
		return tradingPin;
	}

	public void setTradingPin(String tradingPin) {
		this.tradingPin = tradingPin;
	}

	public String getGstin() {
		return gstin;
	}

	public void setGstin(String gstin) {
		this.gstin = gstin;
	}

	public String getCompanyPhone() {
		return companyPhone;
	}

	public void setCompanyPhone(String companyPhone) {
		this.companyPhone = companyPhone;
	}

	public String getCompanyWebsite() {
		return companyWebsite;
	}

	public void setCompanyWebsite(String companyWebsite) {
		this.companyWebsite = companyWebsite;
	}

	public String getCompanyEmailId() {
		return companyEmailId;
	}

	public void setCompanyEmailId(String companyEmailId) {
		this.companyEmailId = companyEmailId;
	}

	public String getBusinessEmailForCommunication() {
		return businessEmailForCommunication;
	}

	public void setBusinessEmailForCommunication(String businessEmailForCommunication) {
		this.businessEmailForCommunication = businessEmailForCommunication;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactMobile() {
		return contactMobile;
	}

	public void setContactMobile(String contactMobile) {
		this.contactMobile = contactMobile;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getContactLandline() {
		return contactLandline;
	}

	public void setContactLandline(String contactLandline) {
		this.contactLandline = contactLandline;
	}

	public String getDirector1FullName() {
		return director1FullName;
	}

	public void setDirector1FullName(String director1FullName) {
		this.director1FullName = director1FullName;
	}

	public String getDirector1Pan() {
		return director1Pan;
	}

	public void setDirector1Pan(String director1Pan) {
		this.director1Pan = director1Pan;
	}

	public String getDirector1Email() {
		return director1Email;
	}

	public void setDirector1Email(String director1Email) {
		this.director1Email = director1Email;
	}

	public String getDirector1Mobile() {
		return director1Mobile;
	}

	public void setDirector1Mobile(String director1Mobile) {
		this.director1Mobile = director1Mobile;
	}

	public String getDirector1Landline() {
		return director1Landline;
	}

	public void setDirector1Landline(String director1Landline) {
		this.director1Landline = director1Landline;
	}

	public String getDirector1Address() {
		return director1Address;
	}

	public void setDirector1Address(String director1Address) {
		this.director1Address = director1Address;
	}

	public String getDirector2FullName() {
		return director2FullName;
	}

	public void setDirector2FullName(String director2FullName) {
		this.director2FullName = director2FullName;
	}

	public String getDirector2Pan() {
		return director2Pan;
	}

	public void setDirector2Pan(String director2Pan) {
		this.director2Pan = director2Pan;
	}

	public String getDirector2Email() {
		return director2Email;
	}

	public void setDirector2Email(String director2Email) {
		this.director2Email = director2Email;
	}

	public String getDirector2Mobile() {
		return director2Mobile;
	}

	public void setDirector2Mobile(String director2Mobile) {
		this.director2Mobile = director2Mobile;
	}

	public String getDirector2Landline() {
		return director2Landline;
	}

	public void setDirector2Landline(String director2Landline) {
		this.director2Landline = director2Landline;
	}

	public String getDirector2Address() {
		return director2Address;
	}

	public void setDirector2Address(String director2Address) {
		this.director2Address = director2Address;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getMpaDataUpdatedBy() {
		return mpaDataUpdatedBy;
	}

	public void setMpaDataUpdatedBy(String mpaDataUpdatedBy) {
		this.mpaDataUpdatedBy = mpaDataUpdatedBy;
	}

	public String getMpaDataUpdatedByEmail() {
		return mpaDataUpdatedByEmail;
	}

	public void setMpaDataUpdatedByEmail(String mpaDataUpdatedByEmail) {
		this.mpaDataUpdatedByEmail = mpaDataUpdatedByEmail;
	}

	public String getMpaDataUpdatedByUserType() {
		return mpaDataUpdatedByUserType;
	}

	public void setMpaDataUpdatedByUserType(String mpaDataUpdatedByUserType) {
		this.mpaDataUpdatedByUserType = mpaDataUpdatedByUserType;
	}

	public String getMpaDataUpdateDate() {
		return mpaDataUpdateDate;
	}

	public void setMpaDataUpdateDate(String mpaDataUpdateDate) {
		this.mpaDataUpdateDate = mpaDataUpdateDate;
	}

	public String getMerchantSupportEmailId() {
		return merchantSupportEmailId;
	}

	public void setMerchantSupportEmailId(String merchantSupportEmailId) {
		this.merchantSupportEmailId = merchantSupportEmailId;
	}

	public String getMerchantSupportMobileNumber() {
		return merchantSupportMobileNumber;
	}

	public void setMerchantSupportMobileNumber(String merchantSupportMobileNumber) {
		this.merchantSupportMobileNumber = merchantSupportMobileNumber;
	}

	public String getMerchantSupportLandLine() {
		return merchantSupportLandLine;
	}

	public void setMerchantSupportLandLine(String merchantSupportLandLine) {
		this.merchantSupportLandLine = merchantSupportLandLine;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getAccountIfsc() {
		return accountIfsc;
	}

	public void setAccountIfsc(String accountIfsc) {
		this.accountIfsc = accountIfsc;
	}

	public String getAccountHolderName() {
		return accountHolderName;
	}

	public void setAccountHolderName(String accountHolderName) {
		this.accountHolderName = accountHolderName;
	}

	public String getAccountMobileNumber() {
		return accountMobileNumber;
	}

	public void setAccountMobileNumber(String accountMobileNumber) {
		this.accountMobileNumber = accountMobileNumber;
	}

	public String getAnnualTurnover() {
		return annualTurnover;
	}

	public void setAnnualTurnover(String annualTurnover) {
		this.annualTurnover = annualTurnover;
	}

	public String getAnnualTurnoverOnline() {
		return annualTurnoverOnline;
	}

	public void setAnnualTurnoverOnline(String annualTurnoverOnline) {
		this.annualTurnoverOnline = annualTurnoverOnline;
	}

	public String getPercentageCC() {
		return percentageCC;
	}

	public void setPercentageCC(String percentageCC) {
		this.percentageCC = percentageCC;
	}

	public String getPercentageDC() {
		return percentageDC;
	}

	public void setPercentageDC(String percentageDC) {
		this.percentageDC = percentageDC;
	}

	public String getPercentageDomestic() {
		return percentageDomestic;
	}

	public void setPercentageDomestic(String percentageDomestic) {
		this.percentageDomestic = percentageDomestic;
	}

	public String getPercentageInternational() {
		return percentageInternational;
	}

	public void setPercentageInternational(String percentageInternational) {
		this.percentageInternational = percentageInternational;
	}

	public String getPercentageCD() {
		return percentageCD;
	}

	public void setPercentageCD(String percentageCD) {
		this.percentageCD = percentageCD;
	}

	public String getPercentageNeftOrImpsOrRtgs() {
		return percentageNeftOrImpsOrRtgs;
	}

	public void setPercentageNeftOrImpsOrRtgs(String percentageNeftOrImpsOrRtgs) {
		this.percentageNeftOrImpsOrRtgs = percentageNeftOrImpsOrRtgs;
	}

	public String getPercentageNB() {
		return percentageNB;
	}

	public void setPercentageNB(String percentageNB) {
		this.percentageNB = percentageNB;
	}

	public String getPercentageUP() {
		return percentageUP;
	}

	public void setPercentageUP(String percentageUP) {
		this.percentageUP = percentageUP;
	}

	public String getPercentageWL() {
		return percentageWL;
	}

	public void setPercentageWL(String percentageWL) {
		this.percentageWL = percentageWL;
	}

	public String getPercentageEM() {
		return percentageEM;
	}

	public void setPercentageEM(String percentageEM) {
		this.percentageEM = percentageEM;
	}

	public Boolean getExpressPay() {
		return expressPay;
	}

	public void setExpressPay(Boolean expressPay) {
		this.expressPay = expressPay;
	}

	public String getExpressPayParameter() {
		return expressPayParameter;
	}

	public void setExpressPayParameter(String expressPayParameter) {
		this.expressPayParameter = expressPayParameter;
	}

	public String getAllowDuplicateSaleOrderId() {
		return allowDuplicateSaleOrderId;
	}

	public void setAllowDuplicateSaleOrderId(String allowDuplicateSaleOrderId) {
		this.allowDuplicateSaleOrderId = allowDuplicateSaleOrderId;
	}

	public String getAllowDuplicateRefundOrderId() {
		return allowDuplicateRefundOrderId;
	}

	public void setAllowDuplicateRefundOrderId(String allowDuplicateRefundOrderId) {
		this.allowDuplicateRefundOrderId = allowDuplicateRefundOrderId;
	}

	public String getAllowDuplicateSaleOrderIdInRefund() {
		return allowDuplicateSaleOrderIdInRefund;
	}

	public void setAllowDuplicateSaleOrderIdInRefund(String allowDuplicateSaleOrderIdInRefund) {
		this.allowDuplicateSaleOrderIdInRefund = allowDuplicateSaleOrderIdInRefund;
	}

	public String getAllowDuplicateRefundOrderIdSale() {
		return allowDuplicateRefundOrderIdSale;
	}

	public void setAllowDuplicateRefundOrderIdSale(String allowDuplicateRefundOrderIdSale) {
		this.allowDuplicateRefundOrderIdSale = allowDuplicateRefundOrderIdSale;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public boolean isTransactionSmsFlag() {
		return transactionSmsFlag;
	}

	public void setTransactionSmsFlag(boolean transactionSmsFlag) {
		this.transactionSmsFlag = transactionSmsFlag;
	}

	public boolean isTransactionAuthenticationEmailFlag() {
		return transactionAuthenticationEmailFlag;
	}

	public void setTransactionAuthenticationEmailFlag(boolean transactionAuthenticationEmailFlag) {
		this.transactionAuthenticationEmailFlag = transactionAuthenticationEmailFlag;
	}

	public boolean isTransactionCustomerEmailFlag() {
		return transactionCustomerEmailFlag;
	}

	public void setTransactionCustomerEmailFlag(boolean transactionCustomerEmailFlag) {
		this.transactionCustomerEmailFlag = transactionCustomerEmailFlag;
	}

	public boolean isRefundTransactionCustomerEmailFlag() {
		return refundTransactionCustomerEmailFlag;
	}

	public void setRefundTransactionCustomerEmailFlag(boolean refundTransactionCustomerEmailFlag) {
		this.refundTransactionCustomerEmailFlag = refundTransactionCustomerEmailFlag;
	}

	public boolean isTransactionEmailerFlag() {
		return transactionEmailerFlag;
	}

	public void setTransactionEmailerFlag(boolean transactionEmailerFlag) {
		this.transactionEmailerFlag = transactionEmailerFlag;
	}

	public boolean isRefundTransactionMerchantEmailFlag() {
		return refundTransactionMerchantEmailFlag;
	}

	public void setRefundTransactionMerchantEmailFlag(boolean refundTransactionMerchantEmailFlag) {
		this.refundTransactionMerchantEmailFlag = refundTransactionMerchantEmailFlag;
	}

	public String getTransactionEmailId() {
		return transactionEmailId;
	}

	public void setTransactionEmailId(String transactionEmailId) {
		this.transactionEmailId = transactionEmailId;
	}

	public String getTransactionSms() {
		return transactionSms;
	}

	public void setTransactionSms(String transactionSms) {
		this.transactionSms = transactionSms;
	}

	public boolean isEposMerchant() {
		return eposMerchant;
	}

	public void setEposMerchant(boolean eposMerchant) {
		this.eposMerchant = eposMerchant;
	}

	public String getResellerId() {
		return resellerId;
	}

	public void setResellerId(String resellerId) {
		this.resellerId = resellerId;
	}

	public String getSuperMerchantId() {
		return superMerchantId;
	}

	public void setSuperMerchantId(String superMerchantId) {
		this.superMerchantId = superMerchantId;
	}

	public String getTerminalId() {
		return terminalId;
	}

	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public Date getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(Date activationDate) {
		this.activationDate = activationDate;
	}

	public boolean isMerchantHostedFlag() {
		return merchantHostedFlag;
	}

	public void setMerchantHostedFlag(boolean merchantHostedFlag) {
		this.merchantHostedFlag = merchantHostedFlag;
	}

	public boolean isIframePaymentFlag() {
		return iframePaymentFlag;
	}

	public void setIframePaymentFlag(boolean iframePaymentFlag) {
		this.iframePaymentFlag = iframePaymentFlag;
	}

	public boolean isCheckOutJsFlag() {
		return checkOutJsFlag;
	}

	public void setCheckOutJsFlag(boolean checkOutJsFlag) {
		this.checkOutJsFlag = checkOutJsFlag;
	}

	public boolean isSurchargeFlag() {
		return surchargeFlag;
	}

	public void setSurchargeFlag(boolean surchargeFlag) {
		this.surchargeFlag = surchargeFlag;
	}

	public boolean isRetryTransactionCustomeFlag() {
		return retryTransactionCustomeFlag;
	}

	public void setRetryTransactionCustomeFlag(boolean retryTransactionCustomeFlag) {
		this.retryTransactionCustomeFlag = retryTransactionCustomeFlag;
	}

	public String getAttemptTrasacation() {
		return attemptTrasacation;
	}

	public void setAttemptTrasacation(String attemptTrasacation) {
		this.attemptTrasacation = attemptTrasacation;
	}

	public boolean isExpressPayFlag() {
		return expressPayFlag;
	}

	public void setExpressPayFlag(boolean expressPayFlag) {
		this.expressPayFlag = expressPayFlag;
	}

	public String getCardSaveParam() {
		return cardSaveParam;
	}

	public void setCardSaveParam(String cardSaveParam) {
		this.cardSaveParam = cardSaveParam;
	}

	public boolean isSaveVPAFlag() {
		return saveVPAFlag;
	}

	public void setSaveVPAFlag(boolean saveVPAFlag) {
		this.saveVPAFlag = saveVPAFlag;
	}

	public boolean isBookingRecord() {
		return bookingRecord;
	}

	public void setBookingRecord(boolean bookingRecord) {
		this.bookingRecord = bookingRecord;
	}

	public boolean iseNachReportFlag() {
		return eNachReportFlag;
	}

	public void seteNachReportFlag(boolean eNachReportFlag) {
		this.eNachReportFlag = eNachReportFlag;
	}

	public boolean isUpiAutoPayReportFlag() {
		return upiAutoPayReportFlag;
	}

	public void setUpiAutoPayReportFlag(boolean upiAutoPayReportFlag) {
		this.upiAutoPayReportFlag = upiAutoPayReportFlag;
	}

	public boolean isAcceptPostSettledInEnquiry() {
		return acceptPostSettledInEnquiry;
	}

	public void setAcceptPostSettledInEnquiry(boolean acceptPostSettledInEnquiry) {
		this.acceptPostSettledInEnquiry = acceptPostSettledInEnquiry;
	}

	public String getVpaSaveParam() {
		return vpaSaveParam;
	}

	public void setVpaSaveParam(String vpaSaveParam) {
		this.vpaSaveParam = vpaSaveParam;
	}

	public boolean isDiscountingFlag() {
		return discountingFlag;
	}

	public void setDiscountingFlag(boolean discountingFlag) {
		this.discountingFlag = discountingFlag;
	}

	public boolean isLogoFlag() {
		return logoFlag;
	}

	public void setLogoFlag(boolean logoFlag) {
		this.logoFlag = logoFlag;
	}

	public boolean isAllowLogoInPgPage() {
		return allowLogoInPgPage;
	}

	public void setAllowLogoInPgPage(boolean allowLogoInPgPage) {
		this.allowLogoInPgPage = allowLogoInPgPage;
	}

	public String getLogoName() {
		return logoName;
	}

	public void setLogoName(String logoName) {
		this.logoName = logoName;
	}

	public String getCodName() {
		return codName;
	}

	public void setCodName(String codName) {
		this.codName = codName;
	}

	public String getPaymentMessageSlab() {
		return paymentMessageSlab;
	}

	public void setPaymentMessageSlab(String paymentMessageSlab) {
		this.paymentMessageSlab = paymentMessageSlab;
	}

	public boolean isAllowPartSettle() {
		return allowPartSettle;
	}

	public void setAllowPartSettle(boolean allowPartSettle) {
		this.allowPartSettle = allowPartSettle;
	}

	public boolean isAllowSubtractValue() {
		return allowSubtractValue;
	}

	public void setAllowSubtractValue(boolean allowSubtractValue) {
		this.allowSubtractValue = allowSubtractValue;
	}

	public String getDeviation() {
		return deviation;
	}

	public void setDeviation(String deviation) {
		this.deviation = deviation;
	}

	public boolean isCustomTransactionStatus() {
		return customTransactionStatus;
	}

	public void setCustomTransactionStatus(boolean customTransactionStatus) {
		this.customTransactionStatus = customTransactionStatus;
	}

	public boolean isSkipOrderIdForRefund() {
		return skipOrderIdForRefund;
	}

	public void setSkipOrderIdForRefund(boolean skipOrderIdForRefund) {
		this.skipOrderIdForRefund = skipOrderIdForRefund;
	}

	public boolean isAllowRefundDuplicate() {
		return allowRefundDuplicate;
	}

	public void setAllowRefundDuplicate(boolean allowRefundDuplicate) {
		this.allowRefundDuplicate = allowRefundDuplicate;
	}

	public boolean isAllowSaleDuplicate() {
		return allowSaleDuplicate;
	}

	public void setAllowSaleDuplicate(boolean allowSaleDuplicate) {
		this.allowSaleDuplicate = allowSaleDuplicate;
	}

	public boolean isAllowSaleInRefund() {
		return allowSaleInRefund;
	}

	public void setAllowSaleInRefund(boolean allowSaleInRefund) {
		this.allowSaleInRefund = allowSaleInRefund;
	}

	public boolean isAllowRefundInSale() {
		return allowRefundInSale;
	}

	public void setAllowRefundInSale(boolean allowRefundInSale) {
		this.allowRefundInSale = allowRefundInSale;
	}

	public boolean isAllowDuplicateNotSaleOrderId() {
		return allowDuplicateNotSaleOrderId;
	}

	public void setAllowDuplicateNotSaleOrderId(boolean allowDuplicateNotSaleOrderId) {
		this.allowDuplicateNotSaleOrderId = allowDuplicateNotSaleOrderId;
	}

	public float getExtraRefundLimit() {
		return extraRefundLimit;
	}

	public void setExtraRefundLimit(float extraRefundLimit) {
		this.extraRefundLimit = extraRefundLimit;
	}

	public float getOneTimeRefundLimit() {
		return oneTimeRefundLimit;
	}

	public void setOneTimeRefundLimit(float oneTimeRefundLimit) {
		this.oneTimeRefundLimit = oneTimeRefundLimit;
	}

	public float getRefundLimitRemains() {
		return RefundLimitRemains;
	}

	public void setRefundLimitRemains(float refundLimitRemains) {
		RefundLimitRemains = refundLimitRemains;
	}

	public boolean isLimitChangedFlag() {
		return LimitChangedFlag;
	}

	public void setLimitChangedFlag(boolean limitChangedFlag) {
		LimitChangedFlag = limitChangedFlag;
	}

	public boolean isSameLimitFlag() {
		return sameLimitFlag;
	}

	public void setSameLimitFlag(boolean sameLimitFlag) {
		this.sameLimitFlag = sameLimitFlag;
	}

	public String getmCC() {
		return mCC;
	}

	public void setmCC(String mCC) {
		this.mCC = mCC;
	}

	public boolean isRetailMerchantFlag() {
		return retailMerchantFlag;
	}

	public void setRetailMerchantFlag(boolean retailMerchantFlag) {
		this.retailMerchantFlag = retailMerchantFlag;
	}

	public boolean isCapturedMerchantFlag() {
		return capturedMerchantFlag;
	}

	public void setCapturedMerchantFlag(boolean capturedMerchantFlag) {
		this.capturedMerchantFlag = capturedMerchantFlag;
	}

	public boolean isVendorPayOutFlag() {
		return vendorPayOutFlag;
	}

	public void setVendorPayOutFlag(boolean vendorPayOutFlag) {
		this.vendorPayOutFlag = vendorPayOutFlag;
	}

	public boolean isAccountVerificationFlag() {
		return accountVerificationFlag;
	}

	public void setAccountVerificationFlag(boolean accountVerificationFlag) {
		this.accountVerificationFlag = accountVerificationFlag;
	}

	public boolean isVpaVerificationFlag() {
		return vpaVerificationFlag;
	}

	public void setVpaVerificationFlag(boolean vpaVerificationFlag) {
		this.vpaVerificationFlag = vpaVerificationFlag;
	}

	public boolean isLoadWalletFlag() {
		return loadWalletFlag;
	}

	public void setLoadWalletFlag(boolean loadWalletFlag) {
		this.loadWalletFlag = loadWalletFlag;
	}

	public boolean isNodalReportFlag() {
		return nodalReportFlag;
	}

	public void setNodalReportFlag(boolean nodalReportFlag) {
		this.nodalReportFlag = nodalReportFlag;
	}

	public boolean isPaymentAdviceFlag() {
		return paymentAdviceFlag;
	}

	public void setPaymentAdviceFlag(boolean paymentAdviceFlag) {
		this.paymentAdviceFlag = paymentAdviceFlag;
	}

	public boolean isMerchantInitiatedDirectFlag() {
		return merchantInitiatedDirectFlag;
	}

	public void setMerchantInitiatedDirectFlag(boolean merchantInitiatedDirectFlag) {
		this.merchantInitiatedDirectFlag = merchantInitiatedDirectFlag;
	}

	public boolean isImpsFlag() {
		return impsFlag;
	}

	public void setImpsFlag(boolean impsFlag) {
		this.impsFlag = impsFlag;
	}

	public boolean isVirtualAccountFlag() {
		return virtualAccountFlag;
	}

	public void setVirtualAccountFlag(boolean virtualAccountFlag) {
		this.virtualAccountFlag = virtualAccountFlag;
	}

	public boolean isCustomerQrFlag() {
		return customerQrFlag;
	}

	public void setCustomerQrFlag(boolean customerQrFlag) {
		this.customerQrFlag = customerQrFlag;
	}

	public boolean isSaveNBFlag() {
		return saveNBFlag;
	}

	public void setSaveNBFlag(boolean saveNBFlag) {
		this.saveNBFlag = saveNBFlag;
	}

	public boolean isSaveWLFlag() {
		return saveWLFlag;
	}

	public void setSaveWLFlag(boolean saveWLFlag) {
		this.saveWLFlag = saveWLFlag;
	}

	public String getNbSaveParam() {
		return nbSaveParam;
	}

	public void setNbSaveParam(String nbSaveParam) {
		this.nbSaveParam = nbSaveParam;
	}

	public String getWlSaveParam() {
		return wlSaveParam;
	}

	public void setWlSaveParam(String wlSaveParam) {
		this.wlSaveParam = wlSaveParam;
	}

	public boolean isTransactionCustomerSMSFlag() {
		return transactionCustomerSMSFlag;
	}

	public void setTransactionCustomerSMSFlag(boolean transactionCustomerSMSFlag) {
		this.transactionCustomerSMSFlag = transactionCustomerSMSFlag;
	}

	public boolean isTransactionMerchantSMSFlag() {
		return transactionMerchantSMSFlag;
	}

	public void setTransactionMerchantSMSFlag(boolean transactionMerchantSMSFlag) {
		this.transactionMerchantSMSFlag = transactionMerchantSMSFlag;
	}

	public boolean isExtraRefundAmount() {
		return extraRefundAmount;
	}

	public void setExtraRefundAmount(boolean extraRefundAmount) {
		this.extraRefundAmount = extraRefundAmount;
	}

	public boolean isOneTimeRefundAmount() {
		return oneTimeRefundAmount;
	}

	public void setOneTimeRefundAmount(boolean oneTimeRefundAmount) {
		this.oneTimeRefundAmount = oneTimeRefundAmount;
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

	public String getMerchantVPA() {
		return merchantVPA;
	}

	public void setMerchantVPA(String merchantVPA) {
		this.merchantVPA = merchantVPA;
	}

	public boolean isTransactionFailedMerchantSMSFlag() {
		return transactionFailedMerchantSMSFlag;
	}

	public void setTransactionFailedMerchantSMSFlag(boolean transactionFailedMerchantSMSFlag) {
		this.transactionFailedMerchantSMSFlag = transactionFailedMerchantSMSFlag;
	}

	public boolean isTransactionFailedCustomerSMSFlag() {
		return transactionFailedCustomerSMSFlag;
	}

	public void setTransactionFailedCustomerSMSFlag(boolean transactionFailedCustomerSMSFlag) {
		this.transactionFailedCustomerSMSFlag = transactionFailedCustomerSMSFlag;
	}

	public boolean isTransactionRefundMerchantSMSFlag() {
		return transactionRefundMerchantSMSFlag;
	}

	public void setTransactionRefundMerchantSMSFlag(boolean transactionRefundMerchantSMSFlag) {
		this.transactionRefundMerchantSMSFlag = transactionRefundMerchantSMSFlag;
	}

	public boolean isTransactionRefundCustomerSMSFlag() {
		return transactionRefundCustomerSMSFlag;
	}

	public void setTransactionRefundCustomerSMSFlag(boolean transactionRefundCustomerSMSFlag) {
		this.transactionRefundCustomerSMSFlag = transactionRefundCustomerSMSFlag;
	}

	public boolean isTransactionFailedMerchantEmailFlag() {
		return transactionFailedMerchantEmailFlag;
	}

	public void setTransactionFailedMerchantEmailFlag(boolean transactionFailedMerchantEmailFlag) {
		this.transactionFailedMerchantEmailFlag = transactionFailedMerchantEmailFlag;
	}

	public boolean isTransactionFailedCustomerEmailFlag() {
		return transactionFailedCustomerEmailFlag;
	}

	public void setTransactionFailedCustomerEmailFlag(boolean transactionFailedCustomerEmailFlag) {
		this.transactionFailedCustomerEmailFlag = transactionFailedCustomerEmailFlag;
	}

	public boolean isAllowQRScanFlag() {
		return allowQRScanFlag;
	}

	public void setAllowQRScanFlag(boolean allowQRScanFlag) {
		this.allowQRScanFlag = allowQRScanFlag;
	}

	public boolean isAllowUpiQRFlag() {
		return allowUpiQRFlag;
	}

	public void setAllowUpiQRFlag(boolean allowUpiQRFlag) {
		this.allowUpiQRFlag = allowUpiQRFlag;
	}

	public boolean isNetSettledFlag() {
		return netSettledFlag;
	}

	public void setNetSettledFlag(boolean netSettledFlag) {
		this.netSettledFlag = netSettledFlag;
	}

	public String getPaymentCycle() {
		return paymentCycle;
	}

	public void setPaymentCycle(String paymentCycle) {
		this.paymentCycle = paymentCycle;
	}

	public boolean isSmtMerchant() {
		return smtMerchant;
	}

	public void setSmtMerchant(boolean smtMerchant) {
		this.smtMerchant = smtMerchant;
	}

	public String getCustomHostedUrl() {
		return customHostedUrl;
	}

	public void setCustomHostedUrl(String customHostedUrl) {
		this.customHostedUrl = customHostedUrl;
	}

	public boolean isAllowCustomHostedUrl() {
		return allowCustomHostedUrl;
	}

	public void setAllowCustomHostedUrl(boolean allowCustomHostedUrl) {
		this.allowCustomHostedUrl = allowCustomHostedUrl;
	}

	public boolean isLyraPay() {
		return lyraPay;
	}

	public void setLyraPay(boolean lyraPay) {
		this.lyraPay = lyraPay;
	}

	public boolean isResellerMerchantSignupFlag() {
		return resellerMerchantSignupFlag;
	}

	public void setResellerMerchantSignupFlag(boolean resellerMerchantSignupFlag) {
		this.resellerMerchantSignupFlag = resellerMerchantSignupFlag;
	}

	public boolean isResellerUserStatusFlag() {
		return resellerUserStatusFlag;
	}

	public void setResellerUserStatusFlag(boolean resellerUserStatusFlag) {
		this.resellerUserStatusFlag = resellerUserStatusFlag;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public boolean isNon3dsTxn() {
		return non3dsTxn;
	}

	public void setNon3dsTxn(boolean non3dsTxn) {
		this.non3dsTxn = non3dsTxn;
	}

	public boolean isAutoRefund() {
		return autoRefund;
	}

	public void setAutoRefund(boolean autoRefund) {
		this.autoRefund = autoRefund;
	}

	public boolean isCallBackFlag() {
		return callBackFlag;
	}

	public void setCallBackFlag(boolean callBackFlag) {
		this.callBackFlag = callBackFlag;
	}

	public String getCallBackUrl() {
		return callBackUrl;
	}

	public void setCallBackUrl(String callBackUrl) {
		this.callBackUrl = callBackUrl;
	}

	public boolean isTopupFlag() {
		return topupFlag;
	}

	public void setTopupFlag(boolean topupFlag) {
		this.topupFlag = topupFlag;
	}

	public boolean isStatementFlag() {
		return statementFlag;
	}

	public void setStatementFlag(boolean statementFlag) {
		this.statementFlag = statementFlag;
	}

	public boolean isAllCallBackFlag() {
		return allCallBackFlag;
	}

	public void setAllCallBackFlag(boolean allCallBackFlag) {
		this.allCallBackFlag = allCallBackFlag;
	}

	public UserStatusType getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(UserStatusType userStatus) {
		this.userStatus = userStatus;
	}

	public boolean isAllowNodalPayoutFlag() {
		return allowNodalPayoutFlag;
	}

	public void setAllowNodalPayoutFlag(boolean allowNodalPayoutFlag) {
		this.allowNodalPayoutFlag = allowNodalPayoutFlag;
	}

	public boolean isAllowPayoutUpdateStatus() {
		return allowPayoutUpdateStatus;
	}

	public void setAllowPayoutUpdateStatus(boolean allowPayoutUpdateStatus) {
		this.allowPayoutUpdateStatus = allowPayoutUpdateStatus;
	}

	public boolean isAllowECollectionFee() {
		return allowECollectionFee;
	}

	public void setAllowECollectionFee(boolean allowECollectionFee) {
		this.allowECollectionFee = allowECollectionFee;
	}

	public String getWhiteListReturnUrl() {
		return whiteListReturnUrl;
	}

	public void setWhiteListReturnUrl(String whiteListReturnUrl) {
		this.whiteListReturnUrl = whiteListReturnUrl;
	}

	public boolean isWhiteListReturnUrlFlag() {
		return whiteListReturnUrlFlag;
	}

	public void setWhiteListReturnUrlFlag(boolean whiteListReturnUrlFlag) {
		this.whiteListReturnUrlFlag = whiteListReturnUrlFlag;
	}

}
