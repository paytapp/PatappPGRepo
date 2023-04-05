package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;

import com.paymentgateway.commons.util.ModeType;
import com.paymentgateway.commons.util.UserStatusType;

/**
 * @author Amitosh Aanand
 *
 */
@Entity
@Proxy(lazy = false)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class MerchantProcessingApplication implements Serializable {

	private static final long serialVersionUID = 3633116339101868742L;

	public MerchantProcessingApplication() {

	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	// PG generated
	private String payId;
	private String merchantName;

	// CIN
	// Company details
	// Stage 00
	@Column(columnDefinition = "TEXT", length = 65535)
	private String cinResponse;

	@Column(columnDefinition = "TEXT", length = 65535)
	private String snecsResponse;

	@Column(columnDefinition = "TEXT", length = 65535)
	private String panToGstResponse;

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

	// Stage 01
	@Column(columnDefinition = "LONGTEXT", length = 6553500)
	private String director1Image;

	@Column(columnDefinition = "LONGTEXT", length = 6553500)
	private String director2Image;

	private String contactName;
	private String contactMobile;
	private String contactEmail;
	private String contactLandline;
	private String director1FullName;
	private String director1Pan;
	private Boolean director1PanVerified;
	private String director1Email;
	private String director1Mobile;
	private String director1Landline;
	private String director1Address;
	private String director1DOB;
	private String director2FullName;
	private String director2Pan;
	private Boolean director2PanVerified;
	private String director2Email;
	private String director2Mobile;
	private String director2Landline;
	private String director2Address;
	private String director2DOB;

	// Stage 1A
	@Column(columnDefinition = "TEXT", length = 65535)
	private String director1ElectrictyResponse;

	@Column(columnDefinition = "TEXT", length = 65535)
	private String director1DrivingLicenseResponse;

	@Column(columnDefinition = "LONGTEXT", length = 6553500)
	private String director1DrivingLicenseBase64;

	@Column(columnDefinition = "TEXT", length = 65535)
	private String director2ElectrictyResponse;

	@Column(columnDefinition = "TEXT", length = 65535)
	private String director2DrivingLicenseResponse;

	@Column(columnDefinition = "LONGTEXT", length = 6553500)
	private String director2DrivingLicenseBase64;

	private String director1ConsumerNumber;
	private String director1ElectricityProvider;
	private String director1LicenseNumber;
	private Date director1DOI;

	private String director2ConsumerNumber;
	private String director2ElectricityProvider;
	private String director2LicenseNumber;
	private Date director2DOI;
	
	@Column(columnDefinition = "TEXT", length = 20)
	private String tan;

	// Stage 02
	@Column(columnDefinition = "TEXT", length = 65535)
	private String chequeExtractionResponse;

	@Column(columnDefinition = "TEXT", length = 65535)
	private String bankAccountVerificationResponse;

	@Column(columnDefinition = "LONGTEXT", length = 6553500)
	private String chequeBase64;

	@Column(columnDefinition = "LONGTEXT", length = 6553500)
	private String eSignBase64;
	
	@Column(columnDefinition = "LONGTEXT", length = 6553500)
	private String eSignResponseData;
	
	@Column(columnDefinition = "LONGTEXT", length = 1000)
	private String eSignDataId;
	
	@Column(columnDefinition = "LONGTEXT", length = 1000)
	private String esignUrlResponse;
	
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

	// Stage03
	@Column(columnDefinition = "TEXT", length = 65535)
	private String GSTR3bResponse;
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

	// Stage 04
	@Column(columnDefinition = "LONGTEXT", length = 6553500)
	private String refundPolicyBase64;
	private String thirdPartyForCardData;
	private String refundsAllowed;

	// Stage 05
	private String technicalContactName;
	private String technicalContactMobile;
	private String technicalContactEmail;
	private String technicalContactLandline;
	private String serverDetails;
	private String serverCompanyName;
	private String serverCompanyLandline;
	private String serverCompanyAddress;
	private String serverCompanyMobile;
	private String operatingSystem;
	private String backendTechnology;
	private String applicationServerTechnology;
	private String productionServerIp;

	// Stage 06
	@Column(columnDefinition = "LONGTEXT", length = 6553500)
	private String customizedInvoiceBase64;
	private String merchantType;
	private Boolean surcharge;
	private String integrationType;
	private Boolean customizedInvoiceDesign;
	private Boolean internationalCards;
	private Boolean expressPay;
	private String expressPayParameter;
	private String allowDuplicateSaleOrderId;
	private String allowDuplicateRefundOrderId;
	private String allowDuplicateSaleOrderIdInRefund;
	private String allowDuplicateRefundOrderIdSale;	

	//esign
    @Column(columnDefinition = "TEXT", length = 100)
    private String esignCountry;
    @Column(columnDefinition = "TEXT", length = 100)
    private String esignPincode;
    @Column(columnDefinition = "TEXT", length = 100)
    private String esignGender;
    @Column(columnDefinition = "TEXT", length = 100)
    private String esignName;
    @Column(columnDefinition = "TEXT", length = 100)
    private String esignAadhaarType;
    @Column(columnDefinition = "TEXT", length = 100)
    private String esignState;
    @Column(columnDefinition = "TEXT", length = 100)
    private String esignYOB;
    @Column(columnDefinition = "TEXT", length = 100)
    private String esignUidLastFourDigits;
    
	// Final Stage
	// Properties for analysis
	@Column(columnDefinition = "TEXT", length = 65535)
	private String entityNegativeListResponse;
	private Boolean entityNegativeListFound;	
	
	@Column(columnDefinition = "TEXT", length = 65535)
	private String directorNegativeListResponse;

	private int cinAttempts;
	private boolean validCompanyName;
	private boolean validPan;
	private boolean validCin;
	private boolean validDirector1Pan;
	private boolean validDirector2Pan;

	// Stage management
	private String mpaSavedStage;

	private Boolean accountVerification;
	
	private Boolean gstVerification;
	// PG defaults
	private Date createdDate;
	private Date updatedDate;
	private String requestedBy;
	private String updatedBy;
	@Column(columnDefinition = "TEXT", length = 1000)
	private String reviewedBy;
	@Column(columnDefinition = "TEXT", length = 1000)
	private String approvedBy;
	private String status;
	@Column(columnDefinition = "TEXT", length = 1000)
	private String mpaFiles;

	@Transient
	private UserStatusType userStatus;
	@Transient
	private String partAnnualTurnover;
	@Transient
	private ModeType modeType;
	@Transient
	private String comments;
	@Transient
	private String emailId;
	@Transient
	private boolean transactionSmsFlag;
	@Transient
	private boolean transactionAuthenticationEmailFlag;
	@Transient
	private boolean transactionCustomerEmailFlag;
	@Transient
	private boolean refundTransactionCustomerEmailFlag;
	@Transient
	private boolean transactionEmailerFlag;
	@Transient
	private boolean refundTransactionMerchantEmailFlag;
	@Transient
	private String transactionEmailId;
	@Transient
	private String transactionSms;
	@Transient
	private boolean eposMerchant;
	
//	Onboarding Merchant
	
	@Transient
	private String resellerId;
	@Transient
	private String superMerchantId;
	@Transient
	private String terminalId;
	@Transient
	private Date registrationDate;
	@Transient
	private Date activationDate;

// Transactional setting	
	@Transient
	private boolean merchantHostedFlag;
	@Transient
	private boolean iframePaymentFlag;
	@Transient
	private boolean checkOutJsFlag;
	@Transient
	private boolean surchargeFlag;
	@Transient
	private boolean retryTransactionCustomeFlag;
	@Transient
	private String attemptTrasacation;
	@Transient
	private boolean expressPayFlag;
	@Transient
	private String cardSaveParam;
	@Transient
	private boolean saveVPAFlag;
	@Transient
	private boolean bookingRecord;
	@Transient
	private boolean eNachReportFlag;
	@Transient
	private boolean upiAutoPayReportFlag;
	@Transient
	private boolean acceptPostSettledInEnquiry;
	
	@Transient
	private String vpaSaveParam;
	@Transient
	private boolean discountingFlag;
	
	@Transient
	private boolean logoFlag;
	@Transient
	private boolean allowLogoInPgPage;
	@Transient
	private String logoName;
	@Transient
	private String codName;
	@Transient
	private String paymentMessageSlab;
	
	@Transient
	private boolean allowPartSettle;
	@Transient
	private boolean allowSubtractValue;
	@Transient
    private String deviation;
    
	@Transient
	private String makerStatus;
	@Transient
	private String checkerStatus;
	@Transient
	private String checkerComments;
	@Transient
	private String makerComments;
	@Transient
	private String makerFileName;
	@Transient
	private String checkerFileName;
	@Transient
	private boolean customTransactionStatus;

	//-	Order ID Settings
	@Transient
	private boolean skipOrderIdForRefund;
	@Transient
	private boolean allowRefundDuplicate;
	@Transient
	private boolean allowSaleDuplicate;
	@Transient
	private boolean allowSaleInRefund;
	@Transient
	private boolean allowRefundInSale;
	@Transient
	private boolean allowDuplicateNotSaleOrderId;

	@Transient
	private float extraRefundLimit;
	@Transient
	private float oneTimeRefundLimit;
	@Transient
	private float RefundLimitRemains;
	@Transient
	private boolean LimitChangedFlag;
	@Transient
	private boolean sameLimitFlag;
	
	@Transient
	private boolean isMpaOnlineFlag;
	
//-	AMEX Settings
	@Transient
	private String amexSellerId;
	@Transient
	private String mCC;

	// 	Report's fields hiding flag
	@Transient
	private boolean retailMerchantFlag;
	
	@Transient
	private boolean capturedMerchantFlag;
	
	@Transient
	private boolean vendorPayOutFlag;
	
	@Transient
	private boolean accountVerificationFlag;
	@Transient
	private boolean vpaVerificationFlag;
	@Transient
	private boolean loadWalletFlag;
	
	@Transient
	private boolean nodalReportFlag;
	
	@Transient
	private boolean paymentAdviceFlag;
		
	@Transient
	private boolean merchantInitiatedDirectFlag;
	
	@Transient
	private boolean impsFlag;
	
	@Transient
	@Column(columnDefinition = "boolean default false")
	private boolean virtualAccountFlag;
	
	@Transient
	@Column(columnDefinition = "boolean default false")
	private boolean customerQrFlag;
	
	@Transient
	private boolean saveNBFlag;;
	@Transient
	private boolean saveWLFlag;
	@Transient
	private String nbSaveParam;
	@Transient
	private String wlSaveParam;
	@Transient
    private boolean transactionCustomerSMSFlag;
	@Transient
	private boolean transactionMerchantSMSFlag;
	
	@Transient
	private boolean extraRefundAmount;
	@Transient
	private boolean oneTimeRefundAmount;
    
	@Column(columnDefinition = "TEXT", length = 24)
	private String virtualAccountNo;
	@Column(columnDefinition = "TEXT", length = 24)
	private String virtualIfscCode;
	public String getMerchantVPA() {
		return merchantVPA;
	}
	public void setMerchantVPA(String merchantVPA) {
		this.merchantVPA = merchantVPA;
	}

	@Column(columnDefinition = "TEXT", length = 24)
	private String virtualBeneficiaryName;
	@Column(columnDefinition = "TEXT", length = 32)
	private String merchantVPA;
	
    @Transient
    private boolean transactionFailedMerchantSMSFlag;
    @Transient
    private boolean transactionFailedCustomerSMSFlag;
    @Transient
    private boolean transactionRefundMerchantSMSFlag;
    @Transient
    private boolean transactionRefundCustomerSMSFlag;
    @Transient
    private boolean transactionFailedMerchantEmailFlag;
    @Transient
    private boolean transactionFailedCustomerEmailFlag;
    @Transient
    private boolean allowQRScanFlag;
    @Transient
    private boolean allowUpiQRFlag;
    @Transient
    private boolean netSettledFlag;
	@Column(columnDefinition = "TEXT", length = 3)
	private String paymentCycle;
	
	@Column(columnDefinition = "boolean default false")
	private boolean smtMerchant= false;
	@Transient
    private String customHostedUrl;
	@Transient
    private boolean allowCustomHostedUrl;
	@Transient
	private boolean lyraPay;
	
	@Transient
	private String whiteListReturnUrl;
	@Transient
	private boolean whiteListReturnUrlFlag = false;
	
	@Transient
	private boolean resellerMerchantSignupFlag;
	@Transient
	private boolean resellerUserStatusFlag;
	@Transient
	private String businessName;
	@Transient
	private boolean non3dsTxn= false;
	@Transient
	private boolean autoRefund= false;
	@Transient
	private boolean callBackFlag= false;
	@Transient
	private String callBackUrl;
	@Column(columnDefinition = "TEXT", length = 100)
    private String amlStatus;

	@Transient
	private boolean topupFlag = false;
	@Transient
	private boolean statementFlag = false;
	@Transient
	private boolean allCallBackFlag= false;
	@Transient
	private boolean allowNodalPayoutFlag = false;
	@Transient
	private boolean allowPayoutUpdateStatus = false;
	@Transient
	private boolean allowECollectionFee = false;
	

	public String getAmlStatus() {
		return amlStatus;
	}
	public void setAmlStatus(String amlStatus) {
		this.amlStatus = amlStatus;
	}
	public boolean isLyraPay() {
		return lyraPay;
	}
	public void setLyraPay(boolean lyraPay) {
		this.lyraPay = lyraPay;
	}
	public boolean isAllowUpiQRFlag() {
		return allowUpiQRFlag;
	}
	public void setAllowUpiQRFlag(boolean allowUpiQRFlag) {
		this.allowUpiQRFlag = allowUpiQRFlag;
	}
	public boolean isAllowQRScanFlag() {
		return allowQRScanFlag;
	}
	public void setAllowQRScanFlag(boolean allowQRScanFlag) {
		this.allowQRScanFlag = allowQRScanFlag;
	}
	public boolean isTransactionCustomerSMSFlag() {
		return transactionCustomerSMSFlag;
	}
	public void setTransactionCustomerSMSFlag(boolean transactionCustomerSMSFlag) {
		this.transactionCustomerSMSFlag = transactionCustomerSMSFlag;
	}
	public boolean isAllowLogoInPgPage() {
		return allowLogoInPgPage;
	}
	public void setAllowLogoInPgPage(boolean allowLogoInPgPage) {
		this.allowLogoInPgPage = allowLogoInPgPage;
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
	public String getMerchantSupportLandLine() {
		return merchantSupportLandLine;
	}
	public void setMerchantSupportLandLine(String merchantSupportLandLine) {
		this.merchantSupportLandLine = merchantSupportLandLine;
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
	public String getSuperMerchantId() {
		return superMerchantId;
	}
	public void setSuperMerchantId(String superMerchantId) {
		this.superMerchantId = superMerchantId;
	}
	public boolean isMpaOnlineFlag() {
		return isMpaOnlineFlag;
	}
	public void setMpaOnlineFlag(boolean isMpaOnlineFlag) {
		this.isMpaOnlineFlag = isMpaOnlineFlag;
	}
	public Boolean getAccountVerification() {
		return accountVerification;
	}
	public void setAccountVerification(Boolean accountVerification) {
		this.accountVerification = accountVerification;
	}
	public Boolean getGstVerification() {
		return gstVerification;
	}
	public void setGstVerification(Boolean gstVerification) {
		this.gstVerification = gstVerification;
	}
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
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public String getCinResponse() {
		return cinResponse;
	}
	public void setCinResponse(String cinResponse) {
		this.cinResponse = cinResponse;
	}
	public String getSnecsResponse() {
		return snecsResponse;
	}
	public void setSnecsResponse(String snecsResponse) {
		this.snecsResponse = snecsResponse;
	}
	public String getPanToGstResponse() {
		return panToGstResponse;
	}
	public void setPanToGstResponse(String panToGstResponse) {
		this.panToGstResponse = panToGstResponse;
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
	public String getDirector1Image() {
		return director1Image;
	}
	public void setDirector1Image(String director1Image) {
		this.director1Image = director1Image;
	}
	public String getDirector2Image() {
		return director2Image;
	}
	public void setDirector2Image(String director2Image) {
		this.director2Image = director2Image;
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
	public Boolean getDirector1PanVerified() {
		return director1PanVerified;
	}
	public void setDirector1PanVerified(Boolean director1PanVerified) {
		this.director1PanVerified = director1PanVerified;
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
	public String getDirector1DOB() {
		return director1DOB;
	}
	public void setDirector1DOB(String director1dob) {
		director1DOB = director1dob;
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
	public Boolean getDirector2PanVerified() {
		return director2PanVerified;
	}
	public void setDirector2PanVerified(Boolean director2PanVerified) {
		this.director2PanVerified = director2PanVerified;
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
	public String getDirector2DOB() {
		return director2DOB;
	}
	public void setDirector2DOB(String director2dob) {
		director2DOB = director2dob;
	}
	public String getDirector1ElectrictyResponse() {
		return director1ElectrictyResponse;
	}
	public void setDirector1ElectrictyResponse(String director1ElectrictyResponse) {
		this.director1ElectrictyResponse = director1ElectrictyResponse;
	}
	public String getDirector1DrivingLicenseResponse() {
		return director1DrivingLicenseResponse;
	}
	public void setDirector1DrivingLicenseResponse(String director1DrivingLicenseResponse) {
		this.director1DrivingLicenseResponse = director1DrivingLicenseResponse;
	}
	public String getDirector1DrivingLicenseBase64() {
		return director1DrivingLicenseBase64;
	}
	public void setDirector1DrivingLicenseBase64(String director1DrivingLicenseBase64) {
		this.director1DrivingLicenseBase64 = director1DrivingLicenseBase64;
	}
	public String getDirector2ElectrictyResponse() {
		return director2ElectrictyResponse;
	}
	public void setDirector2ElectrictyResponse(String director2ElectrictyResponse) {
		this.director2ElectrictyResponse = director2ElectrictyResponse;
	}
	public String getDirector2DrivingLicenseResponse() {
		return director2DrivingLicenseResponse;
	}
	public void setDirector2DrivingLicenseResponse(String director2DrivingLicenseResponse) {
		this.director2DrivingLicenseResponse = director2DrivingLicenseResponse;
	}
	public String getDirector2DrivingLicenseBase64() {
		return director2DrivingLicenseBase64;
	}
	public void setDirector2DrivingLicenseBase64(String director2DrivingLicenseBase64) {
		this.director2DrivingLicenseBase64 = director2DrivingLicenseBase64;
	}
	public String getDirector1ConsumerNumber() {
		return director1ConsumerNumber;
	}
	public void setDirector1ConsumerNumber(String director1ConsumerNumber) {
		this.director1ConsumerNumber = director1ConsumerNumber;
	}
	public String getDirector1ElectricityProvider() {
		return director1ElectricityProvider;
	}
	public void setDirector1ElectricityProvider(String director1ElectricityProvider) {
		this.director1ElectricityProvider = director1ElectricityProvider;
	}
	public String getDirector1LicenseNumber() {
		return director1LicenseNumber;
	}
	public void setDirector1LicenseNumber(String director1LicenseNumber) {
		this.director1LicenseNumber = director1LicenseNumber;
	}
	public Date getDirector1DOI() {
		return director1DOI;
	}
	public void setDirector1DOI(Date director1doi) {
		director1DOI = director1doi;
	}
	public String getDirector2ConsumerNumber() {
		return director2ConsumerNumber;
	}
	public void setDirector2ConsumerNumber(String director2ConsumerNumber) {
		this.director2ConsumerNumber = director2ConsumerNumber;
	}
	public String getDirector2ElectricityProvider() {
		return director2ElectricityProvider;
	}
	public void setDirector2ElectricityProvider(String director2ElectricityProvider) {
		this.director2ElectricityProvider = director2ElectricityProvider;
	}
	public String getDirector2LicenseNumber() {
		return director2LicenseNumber;
	}
	public void setDirector2LicenseNumber(String director2LicenseNumber) {
		this.director2LicenseNumber = director2LicenseNumber;
	}
	public Date getDirector2DOI() {
		return director2DOI;
	}
	public void setDirector2DOI(Date director2doi) {
		director2DOI = director2doi;
	}
	public String getChequeExtractionResponse() {
		return chequeExtractionResponse;
	}
	public void setChequeExtractionResponse(String chequeExtractionResponse) {
		this.chequeExtractionResponse = chequeExtractionResponse;
	}
	public String getBankAccountVerificationResponse() {
		return bankAccountVerificationResponse;
	}
	public void setBankAccountVerificationResponse(String bankAccountVerificationResponse) {
		this.bankAccountVerificationResponse = bankAccountVerificationResponse;
	}
	public String getChequeBase64() {
		return chequeBase64;
	}
	public void setChequeBase64(String chequeBase64) {
		this.chequeBase64 = chequeBase64;
	}
	public String geteSignBase64() {
		return eSignBase64;
	}
	public void seteSignBase64(String eSignBase64) {
		this.eSignBase64 = eSignBase64;
	}
	public String geteSignResponseData() {
		return eSignResponseData;
	}
	public void seteSignResponseData(String eSignResponseData) {
		this.eSignResponseData = eSignResponseData;
	}
	public String geteSignDataId() {
		return eSignDataId;
	}
	public void seteSignDataId(String eSignDataId) {
		this.eSignDataId = eSignDataId;
	}
	public String getEsignUrlResponse() {
		return esignUrlResponse;
	}
	public void setEsignUrlResponse(String esignUrlResponse) {
		this.esignUrlResponse = esignUrlResponse;
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
	public String getGSTR3bResponse() {
		return GSTR3bResponse;
	}
	public void setGSTR3bResponse(String gSTR3bResponse) {
		GSTR3bResponse = gSTR3bResponse;
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
	public String getRefundPolicyBase64() {
		return refundPolicyBase64;
	}
	public void setRefundPolicyBase64(String refundPolicyBase64) {
		this.refundPolicyBase64 = refundPolicyBase64;
	}
	public String getThirdPartyForCardData() {
		return thirdPartyForCardData;
	}
	public void setThirdPartyForCardData(String thirdPartyForCardData) {
		this.thirdPartyForCardData = thirdPartyForCardData;
	}
	public String getRefundsAllowed() {
		return refundsAllowed;
	}
	public void setRefundsAllowed(String refundsAllowed) {
		this.refundsAllowed = refundsAllowed;
	}
	public String getTechnicalContactName() {
		return technicalContactName;
	}
	public void setTechnicalContactName(String technicalContactName) {
		this.technicalContactName = technicalContactName;
	}
	public String getTechnicalContactMobile() {
		return technicalContactMobile;
	}
	public void setTechnicalContactMobile(String technicalContactMobile) {
		this.technicalContactMobile = technicalContactMobile;
	}
	public String getTechnicalContactEmail() {
		return technicalContactEmail;
	}
	public void setTechnicalContactEmail(String technicalContactEmail) {
		this.technicalContactEmail = technicalContactEmail;
	}
	public String getTechnicalContactLandline() {
		return technicalContactLandline;
	}
	public void setTechnicalContactLandline(String technicalContactLandline) {
		this.technicalContactLandline = technicalContactLandline;
	}
	public String getServerDetails() {
		return serverDetails;
	}
	public void setServerDetails(String serverDetails) {
		this.serverDetails = serverDetails;
	}
	public String getServerCompanyName() {
		return serverCompanyName;
	}
	public void setServerCompanyName(String serverCompanyName) {
		this.serverCompanyName = serverCompanyName;
	}
	public String getServerCompanyLandline() {
		return serverCompanyLandline;
	}
	public void setServerCompanyLandline(String serverCompanyLandline) {
		this.serverCompanyLandline = serverCompanyLandline;
	}
	public String getServerCompanyAddress() {
		return serverCompanyAddress;
	}
	public void setServerCompanyAddress(String serverCompanyAddress) {
		this.serverCompanyAddress = serverCompanyAddress;
	}
	public String getServerCompanyMobile() {
		return serverCompanyMobile;
	}
	public void setServerCompanyMobile(String serverCompanyMobile) {
		this.serverCompanyMobile = serverCompanyMobile;
	}
	public String getOperatingSystem() {
		return operatingSystem;
	}
	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}
	public String getBackendTechnology() {
		return backendTechnology;
	}
	public void setBackendTechnology(String backendTechnology) {
		this.backendTechnology = backendTechnology;
	}
	public String getApplicationServerTechnology() {
		return applicationServerTechnology;
	}
	public void setApplicationServerTechnology(String applicationServerTechnology) {
		this.applicationServerTechnology = applicationServerTechnology;
	}
	public String getProductionServerIp() {
		return productionServerIp;
	}
	public void setProductionServerIp(String productionServerIp) {
		this.productionServerIp = productionServerIp;
	}
	public String getCustomizedInvoiceBase64() {
		return customizedInvoiceBase64;
	}
	public void setCustomizedInvoiceBase64(String customizedInvoiceBase64) {
		this.customizedInvoiceBase64 = customizedInvoiceBase64;
	}
	public String getMerchantType() {
		return merchantType;
	}
	public void setMerchantType(String merchantType) {
		this.merchantType = merchantType;
	}
	public Boolean getSurcharge() {
		return surcharge;
	}
	public void setSurcharge(Boolean surcharge) {
		this.surcharge = surcharge;
	}
	public String getIntegrationType() {
		return integrationType;
	}
	public void setIntegrationType(String integrationType) {
		this.integrationType = integrationType;
	}
	public Boolean getCustomizedInvoiceDesign() {
		return customizedInvoiceDesign;
	}
	public void setCustomizedInvoiceDesign(Boolean customizedInvoiceDesign) {
		this.customizedInvoiceDesign = customizedInvoiceDesign;
	}
	public Boolean getInternationalCards() {
		return internationalCards;
	}
	public void setInternationalCards(Boolean internationalCards) {
		this.internationalCards = internationalCards;
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
	public String getEsignCountry() {
		return esignCountry;
	}
	public void setEsignCountry(String esignCountry) {
		this.esignCountry = esignCountry;
	}
	public String getEsignPincode() {
		return esignPincode;
	}
	public void setEsignPincode(String esignPincode) {
		this.esignPincode = esignPincode;
	}
	public String getEsignGender() {
		return esignGender;
	}
	public void setEsignGender(String esignGender) {
		this.esignGender = esignGender;
	}
	public String getEsignName() {
		return esignName;
	}
	public void setEsignName(String esignName) {
		this.esignName = esignName;
	}
	public String getEsignAadhaarType() {
		return esignAadhaarType;
	}
	public void setEsignAadhaarType(String esignAadhaarType) {
		this.esignAadhaarType = esignAadhaarType;
	}
	public String getEsignState() {
		return esignState;
	}
	public void setEsignState(String esignState) {
		this.esignState = esignState;
	}
	public String getEsignYOB() {
		return esignYOB;
	}
	public void setEsignYOB(String esignYOB) {
		this.esignYOB = esignYOB;
	}
	public String getEsignUidLastFourDigits() {
		return esignUidLastFourDigits;
	}
	public void setEsignUidLastFourDigits(String esignUidLastFourDigits) {
		this.esignUidLastFourDigits = esignUidLastFourDigits;
	}
	public String getEntityNegativeListResponse() {
		return entityNegativeListResponse;
	}
	public void setEntityNegativeListResponse(String entityNegativeListResponse) {
		this.entityNegativeListResponse = entityNegativeListResponse;
	}
	public Boolean getEntityNegativeListFound() {
		return entityNegativeListFound;
	}
	public void setEntityNegativeListFound(Boolean entityNegativeListFound) {
		this.entityNegativeListFound = entityNegativeListFound;
	}
	public String getDirectorNegativeListResponse() {
		return directorNegativeListResponse;
	}
	public void setDirectorNegativeListResponse(String directorNegativeListResponse) {
		this.directorNegativeListResponse = directorNegativeListResponse;
	}
	public int getCinAttempts() {
		return cinAttempts;
	}
	public void setCinAttempts(int cinAttempts) {
		this.cinAttempts = cinAttempts;
	}
	public boolean isValidCompanyName() {
		return validCompanyName;
	}
	public void setValidCompanyName(boolean validCompanyName) {
		this.validCompanyName = validCompanyName;
	}
	public boolean isValidPan() {
		return validPan;
	}
	public void setValidPan(boolean validPan) {
		this.validPan = validPan;
	}
	public boolean isValidCin() {
		return validCin;
	}
	public void setValidCin(boolean validCin) {
		this.validCin = validCin;
	}
	public boolean isValidDirector1Pan() {
		return validDirector1Pan;
	}
	public void setValidDirector1Pan(boolean validDirector1Pan) {
		this.validDirector1Pan = validDirector1Pan;
	}
	public boolean isValidDirector2Pan() {
		return validDirector2Pan;
	}
	public void setValidDirector2Pan(boolean validDirector2Pan) {
		this.validDirector2Pan = validDirector2Pan;
	}
	public String getMpaSavedStage() {
		return mpaSavedStage;
	}
	public void setMpaSavedStage(String mpaSavedStage) {
		this.mpaSavedStage = mpaSavedStage;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Date getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}
	public String getRequestedBy() {
		return requestedBy;
	}
	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}
	public String getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	public String getReviewedBy() {
		return reviewedBy;
	}
	public void setReviewedBy(String reviewedBy) {
		this.reviewedBy = reviewedBy;
	}
	public String getApprovedBy() {
		return approvedBy;
	}
	public void setApprovedBy(String approvedBy) {
		this.approvedBy = approvedBy;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMpaFiles() {
		return mpaFiles;
	}
	public void setMpaFiles(String mpaFiles) {
		this.mpaFiles = mpaFiles;
	}
	public UserStatusType getUserStatus() {
		return userStatus;
	}
	public void setUserStatus(UserStatusType userStatus) {
		this.userStatus = userStatus;
	}
	public ModeType getModeType() {
		return modeType;
	}
	public void setModeType(ModeType modeType) {
		this.modeType = modeType;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
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
	public String getResellerId() {
		return resellerId;
	}
	public void setResellerId(String resellerId) {
		this.resellerId = resellerId;
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
	public String getVpaSaveParam() {
		return vpaSaveParam;
	}
	public void setVpaSaveParam(String vpaSaveParam) {
		this.vpaSaveParam = vpaSaveParam;
	}
	public boolean isLogoFlag() {
		return logoFlag;
	}
	public void setLogoFlag(boolean logoFlag) {
		this.logoFlag = logoFlag;
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
	public String getMakerStatus() {
		return makerStatus;
	}
	public void setMakerStatus(String makerStatus) {
		this.makerStatus = makerStatus;
	}
	public String getCheckerStatus() {
		return checkerStatus;
	}
	public void setCheckerStatus(String checkerStatus) {
		this.checkerStatus = checkerStatus;
	}
	public String getCheckerComments() {
		return checkerComments;
	}
	public void setCheckerComments(String checkerComments) {
		this.checkerComments = checkerComments;
	}
	public String getMakerComments() {
		return makerComments;
	}
	public void setMakerComments(String makerComments) {
		this.makerComments = makerComments;
	}
	public String getMakerFileName() {
		return makerFileName;
	}
	public void setMakerFileName(String makerFileName) {
		this.makerFileName = makerFileName;
	}
	public String getCheckerFileName() {
		return checkerFileName;
	}
	public void setCheckerFileName(String checkerFileName) {
		this.checkerFileName = checkerFileName;
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
	public float getExtraRefundLimit() {
		return extraRefundLimit;
	}
	public void setExtraRefundLimit(float extraRefundLimit) {
		this.extraRefundLimit = extraRefundLimit;
	}
	public String getAmexSellerId() {
		return amexSellerId;
	}
	public void setAmexSellerId(String amexSellerId) {
		this.amexSellerId = amexSellerId;
	}
	public String getmCC() {
		return mCC;
	}
	public void setmCC(String mCC) {
		this.mCC = mCC;
	}
	public boolean isTransactionMerchantSMSFlag() {
		return transactionMerchantSMSFlag;
	}
	public void setTransactionMerchantSMSFlag(boolean transactionMerchantSMSFlag) {
		this.transactionMerchantSMSFlag = transactionMerchantSMSFlag;
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
	public boolean isExtraRefundAmount() {
		return extraRefundAmount;
	}
	public void setExtraRefundAmount(boolean extraRefundAmount) {
		this.extraRefundAmount = extraRefundAmount;
	}
	public String getTan() {
		return tan;
	}
	public void setTan(String tan) {
		this.tan = tan;
	}
	public String getPaymentCycle() {
		return paymentCycle;
	}
	public void setPaymentCycle(String paymentCycle) {
		this.paymentCycle = paymentCycle;
	}
	public boolean isRetailMerchantFlag() {
		return retailMerchantFlag;
	}
	public void setRetailMerchantFlag(boolean retailMerchantFlag) {
		this.retailMerchantFlag = retailMerchantFlag;
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

	public boolean isCapturedMerchantFlag() {
		return capturedMerchantFlag;
	}
	public void setCapturedMerchantFlag(boolean capturedMerchantFlag) {
		this.capturedMerchantFlag = capturedMerchantFlag;
	}

	public boolean isCustomTransactionStatus() {
		return customTransactionStatus;
	}
	public void setCustomTransactionStatus(boolean customTransactionStatus) {
		this.customTransactionStatus = customTransactionStatus;
	}
	public boolean isEposMerchant() {
		return eposMerchant;
	}
	public void setEposMerchant(boolean eposMerchant) {
		this.eposMerchant = eposMerchant;
	}
	
	public boolean isVendorPayOutFlag() {
		return vendorPayOutFlag;
	}
	public void setVendorPayOutFlag(boolean vendorPayOutFlag) {
		this.vendorPayOutFlag = vendorPayOutFlag;
	}
	public float getOneTimeRefundLimit() {
		return oneTimeRefundLimit;
	}
	public void setOneTimeRefundLimit(float oneTimeRefundLimit) {
		this.oneTimeRefundLimit = oneTimeRefundLimit;
	}
	public boolean isOneTimeRefundAmount() {
		return oneTimeRefundAmount;
	}
	public void setOneTimeRefundAmount(boolean oneTimeRefundAmount) {
		this.oneTimeRefundAmount = oneTimeRefundAmount;
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
	public boolean isAccountVerificationFlag() {
		return accountVerificationFlag;
	}
	public void setAccountVerificationFlag(boolean accountVerificationFlag) {
		this.accountVerificationFlag = accountVerificationFlag;
	}
	public boolean isLoadWalletFlag() {
		return loadWalletFlag;
	}
	public void setLoadWalletFlag(boolean loadWalletFlag) {
		this.loadWalletFlag = loadWalletFlag;
	}
	public boolean isSmtMerchant() {
		return smtMerchant;
	}
	public void setSmtMerchant(boolean smtMerchant) {
		this.smtMerchant = smtMerchant;
	}
	public boolean isCheckOutJsFlag() {
		return checkOutJsFlag;
	}
	public void setCheckOutJsFlag(boolean checkOutJsFlag) {
		this.checkOutJsFlag = checkOutJsFlag;
	}
	public boolean isNodalReportFlag() {
		return nodalReportFlag;
	}
	public void setNodalReportFlag(boolean nodalReportFlag) {
		this.nodalReportFlag = nodalReportFlag;
	}
	public boolean isDiscountingFlag() {
		return discountingFlag;
	}
	public void setDiscountingFlag(boolean discountingFlag) {
		this.discountingFlag = discountingFlag;
	}
	
	public boolean isPaymentAdviceFlag() {
		return paymentAdviceFlag;
	}
	public void setPaymentAdviceFlag(boolean paymentAdviceFlag) {
		this.paymentAdviceFlag = paymentAdviceFlag;
	}
	public String getPaymentMessageSlab() {
		return paymentMessageSlab;
	}
	public void setPaymentMessageSlab(String paymentMessageSlab) {
		this.paymentMessageSlab = paymentMessageSlab;
	}
	public String getPartAnnualTurnover() {
		return partAnnualTurnover;
	}
	public void setPartAnnualTurnover(String partAnnualTurnover) {
		this.partAnnualTurnover = partAnnualTurnover;
	}
	public boolean iseNachReportFlag() {
		return eNachReportFlag;
	}
	public void seteNachReportFlag(boolean eNachReportFlag) {
		this.eNachReportFlag = eNachReportFlag;
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
	public boolean isVpaVerificationFlag() {
		return vpaVerificationFlag;
	}
	public void setVpaVerificationFlag(boolean vpaVerificationFlag) {
		this.vpaVerificationFlag = vpaVerificationFlag;
	}
	public boolean isCustomerQrFlag() {
		return customerQrFlag;
	}
	public void setCustomerQrFlag(boolean customerQrFlag) {
		this.customerQrFlag = customerQrFlag;
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
	public boolean isNon3dsTxn() {
		return non3dsTxn;
	}
	public void setNon3dsTxn(boolean non3dsTxn) {
		this.non3dsTxn = non3dsTxn;
	}

	public boolean isUpiAutoPayReportFlag() {
		return upiAutoPayReportFlag;
	}
	public void setUpiAutoPayReportFlag(boolean upiAutoPayReportFlag) {
		this.upiAutoPayReportFlag = upiAutoPayReportFlag;
	}
	public boolean isNetSettledFlag() {
		return netSettledFlag;
	}
	public void setNetSettledFlag(boolean netSettledFlag) {
		this.netSettledFlag = netSettledFlag;
	}
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}
	public boolean isAutoRefund() {
		return autoRefund;
	}
	public void setAutoRefund(boolean autoRefund) {
		this.autoRefund = autoRefund;
	}
	
	public String getCallBackUrl() {
		return callBackUrl;
	}

	public void setCallBackUrl(String callBackUrl) {
		this.callBackUrl = callBackUrl;
	}
	public boolean isCallBackFlag() {
		return callBackFlag;
	}
	public void setCallBackFlag(boolean callBackFlag) {
		this.callBackFlag = callBackFlag;
	}
	public boolean isAcceptPostSettledInEnquiry() {
		return acceptPostSettledInEnquiry;
	}
	public void setAcceptPostSettledInEnquiry(boolean acceptPostSettledInEnquiry) {
		this.acceptPostSettledInEnquiry = acceptPostSettledInEnquiry;
	}
	
	public boolean isAllowDuplicateNotSaleOrderId() {
		return allowDuplicateNotSaleOrderId;
	}
	public void setAllowDuplicateNotSaleOrderId(boolean allowDuplicateNotSaleOrderId) {
		this.allowDuplicateNotSaleOrderId = allowDuplicateNotSaleOrderId;
	}
	public boolean isTopupFlag() {
		return topupFlag;
	}
	public boolean isStatementFlag() {
		return statementFlag;
	}
	public void setTopupFlag(boolean topupFlag) {
		this.topupFlag = topupFlag;
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
