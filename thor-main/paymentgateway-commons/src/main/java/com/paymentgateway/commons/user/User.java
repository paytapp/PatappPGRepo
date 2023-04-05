package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.springframework.util.StringUtils;

import com.paymentgateway.commons.util.ModeType;
import com.paymentgateway.commons.util.OrderIdType;
import com.paymentgateway.commons.util.UserStatusType;

@Entity
@Proxy(lazy = false)
@Table(indexes = { @Index(name = "IDX_MYIDX1", columnList = "emailId,payId") })
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class User implements Serializable {
	private static final long serialVersionUID = 8476685067435231830L;

	// Personal details
	@Id
	@Column(nullable = false, unique = true)
	private String emailId;
	private String password;
	private String pin;
	private String payId;
	private String businessName;
	private String firstName;
	private String lastName;
	private String companyName;
	private String contactPerson;

	private String merchantType;
	private String resellerId;
	private String productDetail;
	private Date registrationDate;
	private Date updateDate;
	private Date activationDate;
	private String updatedBy;
	@Transient
	private String requestedBy;

	// Contact Details
	private String mobile;
	@Column(columnDefinition = "boolean default false")
	private boolean transactionSmsFlag;
	private String telephoneNo;
	private String fax;
	private String address;
	private String city;
	private String state;
	private String country;
	private String postalCode;

	// Action
	@Enumerated(EnumType.STRING)
	private ModeType modeType;

	@Enumerated(EnumType.STRING)
	private OrderIdType allowDuplicateOrderId;

	private String comments;
	private String whiteListIpAddress;

	// Bank Details
	private String bankName;
	private String ifscCode;
	private String accHolderName;
	private String currency;
	private String branchName;
	private String panCard;
	private String accountNo;

	@Column(columnDefinition = "TEXT", length = 24)
	private String virtualAccountNo;
	@Column(columnDefinition = "TEXT", length = 24)
	private String virtualIfscCode;
	@Column(columnDefinition = "TEXT", length = 24)
	private String virtualBeneficiaryName;
	@Column(columnDefinition = "TEXT", length = 32)
	private String merchantVPA;
	// Documents
	private String uploadePhoto;
	private String uploadedPanCard;
	private String uploadedPhotoIdProof;
	private String uploadedContractDocument;

	// Account Activation
	private String accountValidationKey;
	@Column(columnDefinition = "boolean default false")
	private boolean emailValidationFlag;
	@Column(columnDefinition = "boolean default false")
	private boolean phoneValidationFlag;

	// business details
	private String organisationType;
	private String website;
	private String multiCurrency;
	private String businessModel;
	private String operationAddress;
	private String operationState;
	private String operationCity;
	private String operationPostalCode;
	private String dateOfEstablishment;
	private String cin;
	private String pan;
	private String panName;
	private String noOfTransactions;
	private String amountOfTransactions;
	private String attemptTrasacation;
	private String merchantGstNo;
	// industry classification
	private String industryCategory;
	private String industrySubCategory;

	// Checker Maker fields
	private String makerName;
	private String checkerName;
	private String makerPayId;
	private String checkerPayId;
	private String makerStatus;
	private String checkerStatus;
	private String makerStatusUpDate;
	private String checkerStatusUpDate;
	@Column(columnDefinition = "boolean default false")
	private boolean editPermission;
	private String adminStatus;
	private String adminStatusUpDate;
	private String checkerComments;
	private String makerComments;

	// Transaction Emailer
	private String transactionEmailId;
	private String transactionMobileNo;
	@Column(columnDefinition = "boolean default false")
	private boolean transactionEmailerFlag;

	// Payment Flag
	@Column(columnDefinition = "boolean default false")
	private boolean expressPayFlag;
	@Column(columnDefinition = "boolean default false")
	private boolean merchantHostedFlag;
	@Column(columnDefinition = "boolean default false")
	private boolean iframePaymentFlag;

	@Column(columnDefinition = "boolean default false")
	private boolean checkOutJsFlag;
	
	@Column(columnDefinition = "boolean default false")
	private boolean transactionAuthenticationEmailFlag;
	@Column(columnDefinition = "boolean default false")
	private boolean transactionCustomerEmailFlag;
	@Column(columnDefinition = "boolean default false")
	private boolean refundTransactionCustomerEmailFlag;
	@Column(columnDefinition = "boolean default false")
	private boolean refundTransactionMerchantEmailFlag;
	@Column(columnDefinition = "boolean default false")
	private boolean retryTransactionCustomeFlag;
	@Column(columnDefinition = "boolean default false")
	private boolean surchargeFlag;
	@Column(columnDefinition = "boolean default false")
	private boolean discountingFlag;
	@Column(columnDefinition = "boolean default false")
	private boolean skipOrderIdForRefund;
	
	private String parentPayId;
	private String transactionSms;
	private String paymentMessageSlab;
	@Column(columnDefinition = "boolean default false")
	private boolean allowSaleDuplicate;
	@Column(columnDefinition = "boolean default false")
	private boolean allowRefundDuplicate;
	@Column(columnDefinition = "boolean default false")
	private boolean allowSaleInRefund;
	@Column(columnDefinition = "boolean default false")
	private boolean allowRefundInSale;
	@Column(columnDefinition = "boolean default false")
	private boolean allowDuplicateNot;

	@Column(columnDefinition = "boolean default false")
	private boolean saveVPAFlag;

	@Column(columnDefinition = "boolean default false")
	private boolean logoFlag;
	@Column(columnDefinition = "boolean default false")
	private boolean allowLogoInPgPage;
	@Column(columnDefinition = "TEXT", length = 24)
	private String logoName;
	@Column(columnDefinition = "TEXT", length = 24)
	private String codName;

	@Column(columnDefinition = "TEXT", length = 24)
	private String subUserType;
	
	@Column(columnDefinition = "boolean default false")
	private boolean bookingRecord;
	@Column(columnDefinition = "boolean default false")
	private boolean customTransactionStatus;
	@Column(columnDefinition = "boolean default false")
	private boolean eNachReportFlag;
	@Column(columnDefinition = "boolean default false")
	private boolean upiAutoPayReportFlag;
	@Column(columnDefinition = "boolean default false")
	private boolean acceptPostSettledInEnquiry;

	// saving last activity
	private String lastActionName;

	// online offline mpa flag
	@Column(columnDefinition = "boolean default false")
	private boolean isMpaOnlineFlag;

	// Report's fields hiding flag
	@Column(columnDefinition = "boolean default false")
	private boolean retailMerchantFlag;
	@Column(columnDefinition = "boolean default false")
	private boolean capturedMerchantFlag;
	@Column(columnDefinition = "boolean default false")
	private boolean partnerFlag;

	@Column(columnDefinition = "boolean default false")
	private boolean vendorPayOutFlag;

	private boolean nodalReportFlag;
	private boolean paymentAdviceFlag;

	@Column(columnDefinition = "boolean default false")
	private boolean merchantInitiatedDirectFlag;

	@Column(columnDefinition = "boolean default false")
	private boolean impsFlag;

	@Column(columnDefinition = "boolean default false")
	private boolean virtualAccountFlag;

	@Column(columnDefinition = "boolean default false")
	private boolean customerQrFlag;

	@Column(columnDefinition = "boolean default false")
	private boolean accountVerificationFlag;
	@Column(columnDefinition = "boolean default false")
	private boolean vpaVerificationFlag;
	@Column(columnDefinition = "boolean default false")
	private boolean loadWalletFlag;

	// Part Settlement
	@Column(columnDefinition = "boolean default false")
	private boolean allowPartSettle;
	@Column(columnDefinition = "boolean default false")
	private boolean allowSubtractValue;
	@Column(columnDefinition = "TEXT", length = 24)
	private String deviation;

	private String permissionType;

	@Column(columnDefinition = "TEXT", length = 24)
	private String merchantCreatedBy;

	@Column(columnDefinition = "TEXT", length = 24)
	private String merchantCreatorName;

	@Column(columnDefinition = "TEXT", length = 24)
	private String mpaStage;

	@Column(columnDefinition = "TEXT", length = 65535)
	private String customHtmlString;

	@Column(columnDefinition = "TEXT", length = 500)
	private String customHostedUrl;
	
	@Column(columnDefinition = "TEXT", length = 1000)
	private String whiteListReturnUrl;
	
	@Column(columnDefinition = "boolean default false")
	private boolean whiteListReturnUrlFlag = false;

	@Column(columnDefinition = "boolean default false")
	private boolean allowCustomHostedUrl = false;

	// Naming Convention
	private String settlementNamingConvention;
	private String refundValidationNamingConvention;

	@Column(columnDefinition = "boolean default false")
	private boolean khadiMerchant;

	public OrderIdType getAllowDuplicateOrderId() {
		return allowDuplicateOrderId;
	}

	public void setAllowDuplicateOrderId(OrderIdType allowDuplicateOrderId) {
		this.allowDuplicateOrderId = allowDuplicateOrderId;
	}

	// configurable From User Amount
	private float extraRefundLimit;
	private float oneTimeRefundLimit;
	private float RefundLimitRemains;

	// default currency
	private String defaultCurrency;

	// Amex field43
	private String amexSellerId;
	private String mCC;

	// payment page default language
	private String defaultLanguage;

	@Enumerated(EnumType.STRING)
	private UserStatusType userStatus;

	@Enumerated(EnumType.STRING)
	private UserType userType;

	// Email Expiry Time
	private Date emailExpiryTime;

	// card save param
	private String cardSaveParam;
	private String vpaSaveParam;
	private String terminalId;
	@Column(columnDefinition = "boolean default false")
	private boolean saveNBFlag;
	@Column(columnDefinition = "boolean default false")
	private boolean saveWLFlag;
	@Column(columnDefinition = "TEXT", length = 24)
	private String nbSaveParam;
	@Column(columnDefinition = "TEXT", length = 24)
	private String wlSaveParam;

	@Column(columnDefinition = "TEXT", length = 24, nullable = true)
	private String superMerchantId;

	@Column(columnDefinition = "boolean default false")
	private boolean isSuperMerchant;

	@Column(columnDefinition = "boolean default false")
	private boolean schoolManagement;

	@Column(columnDefinition = "TEXT", length = 24)
	private String failLoginCount;

	/*
	 * @Enumerated(EnumType.STRING) private BusinessType businessType;
	 */

	@OneToMany(targetEntity = Roles.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private Set<Roles> roles = new HashSet<Roles>();

	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@OneToMany(targetEntity = Account.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "")
	private Set<Account> accounts = new HashSet<Account>();

	@OneToMany(targetEntity = NotificationEmailer.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private Set<NotificationEmailer> notificationEmailers = new HashSet<NotificationEmailer>();

//	@OneToMany(targetEntity=RouterRule.class,fetch = FetchType.EAGER,cascade = CascadeType.ALL)
//	private Set<RouterRule> routerRule = new HashSet<RouterRule>();

//	@OneToMany(targetEntity=RuleMap.class,fetch = FetchType.EAGER,cascade = CascadeType.ALL)
//	private Set<RuleMap> rules = new HashSet<RuleMap>();

	@Column(columnDefinition = "boolean default false")
	private boolean eposMerchant;

	@Column(columnDefinition = "boolean default false")
	private boolean transactionCustomerSMSFlag = false;

	@Column(columnDefinition = "boolean default false")
	private boolean transactionMerchantSMSFlag = false;

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
	private boolean smtMerchant = false;

	@Column(columnDefinition = "boolean default false")
	private boolean allowQRScanFlag = false;
	@Column(columnDefinition = "boolean default false")
	private boolean allowUpiQRFlag = false;

	@Column(columnDefinition = "boolean default false")
	private boolean netSettledFlag = false;

	@Column(columnDefinition = "boolean default false")
	private boolean lyraPay = false;

	@Column(columnDefinition = "TEXT", length = 2)
	private String debitDuration;

	@Column(columnDefinition = "TEXT", length = 2)
	private String upiAutoPayDebitDuration;

	@Column(columnDefinition = "boolean default false")
	private boolean resellerMerchantSignupFlag = false;

	@Column(columnDefinition = "boolean default false")
	private boolean resellerUserStatusFlag = false;

	@Column(columnDefinition = "boolean default false")
	private boolean non3dsTxn = false;

	@Column(columnDefinition = "boolean default false")
	private boolean autoRefund = false;
	
	@Column(columnDefinition = "boolean default false")
	private boolean callBackFlag = false;

	@Column(columnDefinition = "TEXT", length = 500)
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

	public User() {

	}

	public Account getAccountUsingAcquirerCode(String acquirer) {
		Set<Account> accounts = getAccounts();
		User acquirerFromDb = new UserDao().findAcquirerByCode(acquirer);

		for (Account account : accounts) {
			if (StringUtils.isEmpty(account.getAcquirerPayId())) {
				continue;
			}
			if (acquirerFromDb == null) {
				continue;
			}
			if (account.getAcquirerPayId().equals(acquirerFromDb.getPayId())) {
				return account;
			}
		}
		return null;
	}

	public Account getAccountUsingAcquirerCode1(String acquirer) {
		Set<Account> accounts = getAccounts();
		User acquirerFromDb = new UserDao().findAcquirerByCode(acquirer);
		if (!(acquirerFromDb == null)) {
			for (Account account : accounts) {
				if (StringUtils.isEmpty(account.getAcquirerPayId())) {
					continue;
				}
				if (account.getAcquirerPayId().equals(acquirerFromDb.getPayId())) {
					return account;
				}

			}
		}
		return null;
	}

	public String getPrimaryAccount() {
		String acquirerName = "";
		for (Account account : accounts) {
			if (true == account.isPrimaryStatus()) {
				acquirerName = account.getAcquirerName();
				break;
			}
		}
		return acquirerName;
	}

	public String getNetbankingPrimaryAccount() {
		String acquirerName = "";
		for (Account account : accounts) {
			if (true == account.isPrimaryNetbankingStatus()) {
				acquirerName = account.getAcquirerName();
				break;
			}
		}
		return acquirerName;
	}

	public String getMpaStage() {
		return mpaStage;
	}

	public void setMpaStage(String mpaStage) {
		this.mpaStage = mpaStage;
	}

	/*
	 * public String getMerchantCreatedBy() { return merchantCreatedBy; }
	 * 
	 * public void setMerchantCreatedBy(String merchantCreatedBy) {
	 * this.merchantCreatedBy = merchantCreatedBy; }
	 */

	public String getMerchantCreatorName() {
		return merchantCreatorName;
	}

	public boolean isAllowLogoInPgPage() {
		return allowLogoInPgPage;
	}

	public void setAllowLogoInPgPage(boolean allowLogoInPgPage) {
		this.allowLogoInPgPage = allowLogoInPgPage;
	}

	public boolean isMpaOnlineFlag() {
		return isMpaOnlineFlag;
	}

	public void setMpaOnlineFlag(boolean isMpaOnlineFlag) {
		this.isMpaOnlineFlag = isMpaOnlineFlag;
	}

	public void setMerchantCreatorName(String merchantCreatorName) {
		this.merchantCreatorName = merchantCreatorName;
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

	public String getAdminStatus() {
		return adminStatus;
	}

	public void setAdminStatus(String adminStatus) {
		this.adminStatus = adminStatus;
	}

	public String getAdminStatusUpDate() {
		return adminStatusUpDate;
	}

	public void setAdminStatusUpDate(String adminStatusUpDate) {
		this.adminStatusUpDate = adminStatusUpDate;
	}

	public boolean isEditPermission() {
		return editPermission;
	}

	public void setEditPermission(boolean editPermission) {
		this.editPermission = editPermission;
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

	public String getMakerStatusUpDate() {
		return makerStatusUpDate;
	}

	public void setMakerStatusUpDate(String makerStatusUpDate) {
		this.makerStatusUpDate = makerStatusUpDate;
	}

	public String getCheckerStatusUpDate() {
		return checkerStatusUpDate;
	}

	public void setCheckerStatusUpDate(String checkerStatusUpDate) {
		this.checkerStatusUpDate = checkerStatusUpDate;
	}

	public String getMakerName() {
		return makerName;
	}

	public void setMakerName(String makerName) {
		this.makerName = makerName;
	}

	public String getCheckerName() {
		return checkerName;
	}

	public void setCheckerName(String checkerName) {
		this.checkerName = checkerName;
	}

	public String getMakerPayId() {
		return makerPayId;
	}

	public void setMakerPayId(String makerPayId) {
		this.makerPayId = makerPayId;
	}

	public String getCheckerPayId() {
		return checkerPayId;
	}

	public void setCheckerPayId(String checkerPayId) {
		this.checkerPayId = checkerPayId;
	}

	public String getPermissionType() {
		return permissionType;
	}

	public void setPermissionType(String permissionType) {
		this.permissionType = permissionType;
	}

	public void addRole(Roles role) {
		this.roles.add(role);
	}

	public void removeRole(Roles role) {
		this.roles.remove(role);
	}

	public Set<Roles> getRoles() {
		return roles;
	}

	public void setRoles(Set<Roles> roles) {
		this.roles = roles;
	}

	public void addAccount(Account account) {
		this.accounts.add(account);
	}

	public void removeAccount(Account account) {
		this.accounts.remove(account);
	}

	public Set<Account> getAccounts() {
		return accounts;
	}

	public void setAccounts(Set<Account> accounts) {
		this.accounts = accounts;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getMerchantType() {
		return merchantType;
	}

	public void setMerchantType(String merchantType) {
		this.merchantType = merchantType;
	}

	public String getNoOfTransactions() {
		return noOfTransactions;
	}

	public void setNoOfTransactions(String noOfTransactions) {
		this.noOfTransactions = noOfTransactions;
	}

	public String getAmountOfTransactions() {
		return amountOfTransactions;
	}

	public void setAmountOfTransactions(String amountOfTransactions) {
		this.amountOfTransactions = amountOfTransactions;
	}

	public String getResellerId() {
		return resellerId;
	}

	public void setResellerId(String resellerId) {
		this.resellerId = resellerId;
	}

	public String getProductDetail() {
		return productDetail;
	}

	public void setProductDetail(String productDetail) {
		this.productDetail = productDetail;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public String getContactPerson() {
		return contactPerson;
	}

	public void setContactPerson(String contactPerson) {
		this.contactPerson = contactPerson;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public boolean isTransactionSmsFlag() {
		return transactionSmsFlag;
	}

	public void setTransactionSmsFlag(boolean transactionSmsFlag) {
		this.transactionSmsFlag = transactionSmsFlag;
	}

	public String getTelephoneNo() {
		return telephoneNo;
	}

	public void setTelephoneNo(String telephoneNo) {
		this.telephoneNo = telephoneNo;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
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

	public String getWhiteListIpAddress() {
		return whiteListIpAddress;
	}

	public void setWhiteListIpAddress(String whiteListIpAddress) {
		this.whiteListIpAddress = whiteListIpAddress;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getIfscCode() {
		return ifscCode;
	}

	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}

	public String getAccHolderName() {
		return accHolderName;
	}

	public void setAccHolderName(String accHolderName) {
		this.accHolderName = accHolderName;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getPanCard() {
		return panCard;
	}

	public void setPanCard(String panCard) {
		this.panCard = panCard;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public String getUploadePhoto() {
		return uploadePhoto;
	}

	public void setUploadePhoto(String uploadePhoto) {
		this.uploadePhoto = uploadePhoto;
	}

	public String getUploadedPanCard() {
		return uploadedPanCard;
	}

	public void setUploadedPanCard(String uploadedPanCard) {
		this.uploadedPanCard = uploadedPanCard;
	}

	public String getUploadedPhotoIdProof() {
		return uploadedPhotoIdProof;
	}

	public void setUploadedPhotoIdProof(String uploadedPhotoIdProof) {
		this.uploadedPhotoIdProof = uploadedPhotoIdProof;
	}

	public String getUploadedContractDocument() {
		return uploadedContractDocument;
	}

	public void setUploadedContractDocument(String uploadedContractDocument) {
		this.uploadedContractDocument = uploadedContractDocument;
	}

	public String getAccountValidationKey() {
		return accountValidationKey;
	}

	public void setAccountValidationKey(String accountValidationKey) {
		this.accountValidationKey = accountValidationKey;
	}

	public boolean isEmailValidationFlag() {
		return emailValidationFlag;
	}

	public void setEmailValidationFlag(boolean emailValidationFlag) {
		this.emailValidationFlag = emailValidationFlag;
	}

	public boolean isPhoneValidationFlag() {
		return phoneValidationFlag;
	}

	public void setPhoneValidationFlag(boolean phoneValidationFlag) {
		this.phoneValidationFlag = phoneValidationFlag;
	}

	public UserStatusType getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(UserStatusType userStatus) {
		this.userStatus = userStatus;
	}

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public Date getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(Date activationDate) {
		this.activationDate = activationDate;
	}

	public String getMultiCurrency() {
		return multiCurrency;
	}

	public void setMultiCurrency(String multiCurrency) {
		this.multiCurrency = multiCurrency;
	}

	public String getBusinessModel() {
		return businessModel;
	}

	public void setBusinessModel(String businessModel) {
		this.businessModel = businessModel;
	}

	public String getOperationAddress() {
		return operationAddress;
	}

	public void setOperationAddress(String operationAddress) {
		this.operationAddress = operationAddress;
	}

	public String getOperationState() {
		return operationState;
	}

	public void setOperationState(String operationState) {
		this.operationState = operationState;
	}

	public String getOperationCity() {
		return operationCity;
	}

	public void setOperationCity(String operationCity) {
		this.operationCity = operationCity;
	}

	public String getOperationPostalCode() {
		return operationPostalCode;
	}

	public void setOperationPostalCode(String operationPostalCode) {
		this.operationPostalCode = operationPostalCode;
	}

	public String getDateOfEstablishment() {
		return dateOfEstablishment;
	}

	public void setDateOfEstablishment(String dateOfEstablishment) {
		this.dateOfEstablishment = dateOfEstablishment;
	}

	public String getCin() {
		return cin;
	}

	public void setCin(String cin) {
		this.cin = cin;
	}

	public String getPan() {
		return pan;
	}

	public void setPan(String pan) {
		this.pan = pan;
	}

	public String getPanName() {
		return panName;
	}

	public void setPanName(String panName) {
		this.panName = panName;
	}

	public String getOrganisationType() {
		return organisationType;
	}

	public void setOrganisationType(String organisationType) {
		this.organisationType = organisationType;
	}

	public String getTransactionEmailId() {
		return transactionEmailId;
	}

	public void setTransactionEmailId(String transactionEmailId) {
		this.transactionEmailId = transactionEmailId;
	}

	public boolean isTransactionEmailerFlag() {
		return transactionEmailerFlag;
	}

	public void setTransactionEmailerFlag(boolean transactionEmailerFlag) {
		this.transactionEmailerFlag = transactionEmailerFlag;
	}

	public String getParentPayId() {
		return parentPayId;
	}

	public void setParentPayId(String parentPayId) {
		this.parentPayId = parentPayId;
	}

	public boolean isExpressPayFlag() {
		return expressPayFlag;
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

	public boolean isRefundTransactionMerchantEmailFlag() {
		return refundTransactionMerchantEmailFlag;
	}

	public void setRefundTransactionMerchantEmailFlag(boolean refundTransactionMerchantEmailFlag) {
		this.refundTransactionMerchantEmailFlag = refundTransactionMerchantEmailFlag;
	}

	public void setExpressPayFlag(boolean expressPayFlag) {
		this.expressPayFlag = expressPayFlag;
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

	public boolean isTransactionAuthenticationEmailFlag() {
		return transactionAuthenticationEmailFlag;
	}

	public void setTransactionAuthenticationEmailFlag(boolean transactionAuthenticationEmailFlag) {
		this.transactionAuthenticationEmailFlag = transactionAuthenticationEmailFlag;
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

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public String getLastActionName() {
		return lastActionName;
	}

	public void setLastActionName(String lastActionName) {
		this.lastActionName = lastActionName;
	}

	public String getSettlementNamingConvention() {
		return settlementNamingConvention;
	}

	public void setSettlementNamingConvention(String settlementNamingConvention) {
		this.settlementNamingConvention = settlementNamingConvention;
	}

	public String getRefundValidationNamingConvention() {
		return refundValidationNamingConvention;
	}

	public void setRefundValidationNamingConvention(String refundValidationNamingConvention) {
		this.refundValidationNamingConvention = refundValidationNamingConvention;
	}

	public float getExtraRefundLimit() {
		return extraRefundLimit;
	}

	public void setExtraRefundLimit(float extraRefundLimit) {
		this.extraRefundLimit = extraRefundLimit;
	}

	public String getDefaultCurrency() {
		return defaultCurrency;
	}

	public void setDefaultCurrency(String defaultCurrency) {
		this.defaultCurrency = defaultCurrency;
	}

	public String getMCC() {
		return mCC;
	}

	public void setMCC(String mCC) {
		this.mCC = mCC;
	}

	public String getAmexSellerId() {
		return amexSellerId;
	}

	public void setAmexSellerId(String amexSellerId) {
		this.amexSellerId = amexSellerId;
	}

	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	/*
	 * public BusinessType getBusinessType() { return businessType; }
	 * 
	 * public void setBusinessType(BusinessType businessType) { this.businessType =
	 * businessType; }
	 */
	public boolean isSurchargeFlag() {
		return surchargeFlag;
	}

	public void setSurchargeFlag(boolean surchargeFlag) {
		this.surchargeFlag = surchargeFlag;
	}

	public String getIndustryCategory() {
		return industryCategory;
	}

	public void setIndustryCategory(String industryCategory) {
		this.industryCategory = industryCategory;
	}

	public String getIndustrySubCategory() {
		return industrySubCategory;
	}

	public void setIndustrySubCategory(String industrySubCategory) {
		this.industrySubCategory = industrySubCategory;
	}

	public String getRequestedBy() {
		return requestedBy;
	}

	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}

	public Set<NotificationEmailer> getNotificationEmailers() {
		return notificationEmailers;
	}

	public void setNotificationEmailers(Set<NotificationEmailer> notificationEmailers) {
		this.notificationEmailers = notificationEmailers;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Date getEmailExpiryTime() {
		return emailExpiryTime;
	}

	public void setEmailExpiryTime(Date emailExpiryTime) {
		this.emailExpiryTime = emailExpiryTime;
	}

	public String getMerchantGstNo() {
		return merchantGstNo;
	}

	public void setMerchantGstNo(String merchantGstNo) {
		this.merchantGstNo = merchantGstNo;
	}

	public String getTransactionSms() {
		return transactionSms;
	}

	public void setTransactionSms(String transactionSms) {
		this.transactionSms = transactionSms;
	}

	public String getTransactionMobileNo() {
		return transactionMobileNo;
	}

	public boolean isSkipOrderIdForRefund() {
		return skipOrderIdForRefund;
	}

	public void setSkipOrderIdForRefund(boolean skipOrderIdForRefund) {
		this.skipOrderIdForRefund = skipOrderIdForRefund;
	}

	public String getPaymentMessageSlab() {
		return paymentMessageSlab;
	}

	public void setPaymentMessageSlab(String paymentMessageSlab) {
		this.paymentMessageSlab = paymentMessageSlab;
	}

	public String getCardSaveParam() {
		return cardSaveParam;
	}

	public void setCardSaveParam(String cardSaveParam) {
		this.cardSaveParam = cardSaveParam;
	}

	public boolean isAllowSaleDuplicate() {
		return allowSaleDuplicate;
	}

	public void setAllowSaleDuplicate(boolean allowSaleDuplicate) {
		this.allowSaleDuplicate = allowSaleDuplicate;
	}

	public boolean isAllowRefundDuplicate() {
		return allowRefundDuplicate;
	}

	public void setAllowRefundDuplicate(boolean allowRefundDuplicate) {
		this.allowRefundDuplicate = allowRefundDuplicate;
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

	public String getTerminalId() {
		return terminalId;
	}

	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}

	public boolean isSaveVPAFlag() {
		return saveVPAFlag;
	}

	public void setSaveVPAFlag(boolean saveVPAFlag) {
		this.saveVPAFlag = saveVPAFlag;
	}

	public String getVpaSaveParam() {
		return vpaSaveParam;
	}

	public void setVpaSaveParam(String vpaSaveParam) {
		this.vpaSaveParam = vpaSaveParam;
	}

	public String getMerchantCreatedBy() {
		return merchantCreatedBy;
	}

	public void setMerchantCreatedBy(String merchantCreatedBy) {
		this.merchantCreatedBy = merchantCreatedBy;
	}

	public String getmCC() {
		return mCC;
	}

	public void setmCC(String mCC) {
		this.mCC = mCC;
	}

	public void setTransactionMobileNo(String transactionMobileNo) {
		this.transactionMobileNo = transactionMobileNo;
	}

	public boolean isAllowPartSettle() {
		return allowPartSettle;
	}

	public void setAllowPartSettle(boolean allowPartSettle) {
		this.allowPartSettle = allowPartSettle;
	}

	public String getDeviation() {
		return deviation;
	}

	public void setDeviation(String deviation) {
		this.deviation = deviation;
	}

	public boolean isAllowSubtractValue() {
		return allowSubtractValue;
	}

	public void setAllowSubtractValue(boolean allowSubtractValue) {
		this.allowSubtractValue = allowSubtractValue;
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

	public boolean isBookingRecord() {
		return bookingRecord;
	}

	public void setBookingRecord(boolean bookingRecord) {
		this.bookingRecord = bookingRecord;
	}

	public String getSuperMerchantId() {
		return superMerchantId;
	}

	public void setSuperMerchantId(String superMerchantId) {
		this.superMerchantId = superMerchantId;
	}

	public boolean isSuperMerchant() {
		return isSuperMerchant;
	}

	public void setSuperMerchant(boolean isSuperMerchant) {
		this.isSuperMerchant = isSuperMerchant;
	}

	public boolean isDiscountingFlag() {
		return discountingFlag;
	}

	public void setDiscountingFlag(boolean discountingFlag) {
		this.discountingFlag = discountingFlag;
	}

	public boolean isSchoolManagement() {
		return schoolManagement;
	}

	public void setSchoolManagement(boolean schoolManagement) {
		this.schoolManagement = schoolManagement;
	}

	public boolean isEposMerchant() {
		return eposMerchant;
	}

	public void setEposMerchant(boolean eposMerchant) {
		this.eposMerchant = eposMerchant;
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

	public String getVirtualAccountNo() {
		return virtualAccountNo;
	}

	public void setVirtualAccountNo(String virtualAccountNo) {
		this.virtualAccountNo = virtualAccountNo;
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

	public String getFailLoginCount() {
		return failLoginCount;
	}

	public void setFailLoginCount(String failLoginCount) {
		this.failLoginCount = failLoginCount;
	}

	public boolean isKhadiMerchant() {
		return khadiMerchant;
	}

	public void setKhadiMerchant(boolean khadiMerchant) {
		this.khadiMerchant = khadiMerchant;
	}

	public String getCustomHtmlString() {
		return customHtmlString;
	}

	public void setCustomHtmlString(String customHtmlString) {
		this.customHtmlString = customHtmlString;
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

	public boolean isPartnerFlag() {
		return partnerFlag;
	}

	public void setPartnerFlag(boolean partnerFlag) {
		this.partnerFlag = partnerFlag;
	}

	public String getSubUserType() {
		return subUserType;
	}

	public void setSubUserType(String subUserType) {
		this.subUserType = subUserType;
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

	public float getRefundLimitRemains() {
		return RefundLimitRemains;
	}

	public void setRefundLimitRemains(float refundLimitRemains) {
		RefundLimitRemains = refundLimitRemains;
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

	public boolean isAllowQRScanFlag() {
		return allowQRScanFlag;
	}

	public void setAllowQRScanFlag(boolean allowQRScanFlag) {
		this.allowQRScanFlag = allowQRScanFlag;
	}

	public boolean iseNachReportFlag() {
		return eNachReportFlag;
	}

	public void seteNachReportFlag(boolean eNachReportFlag) {
		this.eNachReportFlag = eNachReportFlag;
	}

	public String getMerchantVPA() {
		return merchantVPA;
	}

	public void setMerchantVPA(String merchantVPA) {
		this.merchantVPA = merchantVPA;
	}

	public boolean isVirtualAccountFlag() {
		return virtualAccountFlag;
	}

	public void setVirtualAccountFlag(boolean virtualAccountFlag) {
		this.virtualAccountFlag = virtualAccountFlag;
	}

	public boolean isAllowUpiQRFlag() {
		return allowUpiQRFlag;
	}

	public void setAllowUpiQRFlag(boolean allowUpiQRFlag) {
		this.allowUpiQRFlag = allowUpiQRFlag;
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

	public String getDebitDuration() {
		return debitDuration;
	}

	public void setDebitDuration(String debitDuration) {
		this.debitDuration = debitDuration;
	}

	public boolean isResellerMerchantSignupFlag() {
		return resellerMerchantSignupFlag;
		
	}

	public boolean isResellerUserStatusFlag() {
		return resellerUserStatusFlag;
	}

	public void setResellerMerchantSignupFlag(boolean resellerMerchantSignupFlag) {
		this.resellerMerchantSignupFlag = resellerMerchantSignupFlag;
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

	public boolean isAutoRefund() {
		return autoRefund;
	}

	public void setAutoRefund(boolean autoRefund) {
		this.autoRefund = autoRefund;
	}

	public String getUpiAutoPayDebitDuration() {
		return upiAutoPayDebitDuration;
	}

	public void setUpiAutoPayDebitDuration(String upiAutoPayDebitDuration) {
		this.upiAutoPayDebitDuration = upiAutoPayDebitDuration;
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
	public boolean isAllowDuplicateNot() {
		return allowDuplicateNot;
	}
	
	public void setAllowDuplicateNot(boolean allowDuplicateNot) {
		this.allowDuplicateNot = allowDuplicateNot;
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