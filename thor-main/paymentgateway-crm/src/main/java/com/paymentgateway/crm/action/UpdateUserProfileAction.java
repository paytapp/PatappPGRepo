package com.paymentgateway.crm.action;

import java.util.Date;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.PendingUserApproval;
import com.paymentgateway.commons.user.PendingUserApprovalDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.ModeType;
import com.paymentgateway.commons.util.OrderIdType;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.commons.util.UserStatusType;

/**
 * @author Rahul
 *
 */
public class UpdateUserProfileAction extends AbstractSecureAction {

	@Autowired
	PendingUserApprovalDao pendingUserApprovalDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private CrmValidator validator;

	private static final long serialVersionUID = 3663172661005555870L;
	private static Logger logger = LoggerFactory.getLogger(UpdateUserProfileAction.class.getName());

	private String emailId;
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
	private String requestedBy;
	private String mobile;
	private boolean transactionSmsFlag;
	private String telephoneNo;
	private String fax;
	private String address;
	private String city;
	private String state;
	private String country;
	private String postalCode;
	private ModeType modeType;
	private String comments;
	private String whiteListIpAddress;
	private String bankName;
	private String ifscCode;
	private String accHolderName;
	private String currency;
	private String branchName;
	private String panCard;
	private String accountNo;
	private boolean emailValidationFlag;
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
	private String industryCategory;
	private String industrySubCategory;
	private String transactionEmailId;
	private String transactionMobileNo;
	private boolean transactionEmailerFlag;
	private boolean expressPayFlag;
	private boolean merchantHostedFlag;
	private boolean iframePaymentFlag;
	private boolean transactionAuthenticationEmailFlag;
	private boolean transactionCustomerEmailFlag;
	private boolean refundTransactionCustomerEmailFlag;
	private boolean refundTransactionMerchantEmailFlag;
	private boolean retryTransactionCustomeFlag;
	private boolean surchargeFlag;
	private float extraRefundLimit;
	private String defaultCurrency;
	private String amexSellerId;
	private String mCC;
	private String defaultLanguage;
	private UserStatusType userStatus;
	private String loginEmailId;
	private UserType loginUserType;
	private String operation;
	private String transactionSms;
	private String  settlementNamingConvention;
	private String refundValidationNamingConvention;
	
	private boolean skipOrderIdForRefund;
	private OrderIdType allowDuplicateOrderId;
	private String paymentMessageSlab;
	private String response;
	
	
	public String execute() {
		Date date = new Date();
		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));

		try {

			if (permissions.toString().contains("Edit Merchant Details") || loginUserType.equals(UserType.ADMIN)) {
				if (operation.equals("reject")) {
					PendingUserApproval pendingUser = pendingUserApprovalDao.find(payId);
					pendingUser.setRequestStatus(TDRStatus.REJECTED.toString());
					pendingUser.setUpdateDate(date);
					pendingUser.setProcessedBy(loginEmailId);
					pendingUserApprovalDao.update(pendingUser);
					setResponse(ErrorType.MERCHANT_ACCOUNT_EDIT_REQUEST_REJECT.getResponseMessage());
					return SUCCESS;
				} else if (operation.equals("accept")) {
					User dbUser = userDao.findPayId(payId);
					dbUser.setAccHolderName(accHolderName);
					dbUser.setAccountNo(accountNo);
					dbUser.setAddress(address);
					dbUser.setAmexSellerId(amexSellerId);
					dbUser.setAmountOfTransactions(amountOfTransactions);
					dbUser.setAttemptTrasacation(attemptTrasacation);
					dbUser.setBankName(bankName);
					dbUser.setBranchName(branchName);
					dbUser.setBusinessModel(businessModel);
					dbUser.setBusinessName(businessName);
					dbUser.setCin(cin);
					dbUser.setCity(city);
					dbUser.setCompanyName(companyName);
					dbUser.setComments(comments);
					dbUser.setContactPerson(contactPerson);
					dbUser.setCountry(country);
					dbUser.setCurrency(currency);
					dbUser.setDateOfEstablishment(dateOfEstablishment);
					dbUser.setDefaultCurrency(defaultCurrency);
					dbUser.setDefaultLanguage(defaultLanguage);
					dbUser.setEmailValidationFlag(emailValidationFlag);
					dbUser.setExpressPayFlag(expressPayFlag);
					dbUser.setExtraRefundLimit(extraRefundLimit);
					dbUser.setFax(fax);
					dbUser.setFirstName(firstName);
					dbUser.setIframePaymentFlag(iframePaymentFlag);
					dbUser.setIfscCode(ifscCode);
					dbUser.setIndustryCategory(industryCategory);
					dbUser.setIndustrySubCategory(industrySubCategory);
					dbUser.setLastName(lastName);
					dbUser.setMCC(mCC);
					dbUser.setMerchantHostedFlag(merchantHostedFlag);
					dbUser.setMerchantType(merchantType);
					dbUser.setMobile(mobile);
					dbUser.setModeType(modeType);
					dbUser.setMultiCurrency(multiCurrency);
					dbUser.setNoOfTransactions(noOfTransactions);
					dbUser.setOperationAddress(operationAddress);
					dbUser.setOperationCity(operationCity);
					dbUser.setOperationPostalCode(operationPostalCode);
					dbUser.setOperationState(operationState);
					dbUser.setOrganisationType(organisationType);
					dbUser.setPan(pan);
					dbUser.setPanCard(panCard);
					dbUser.setPanName(panName);
					dbUser.setPayId(payId);
					dbUser.setPostalCode(postalCode);
					dbUser.setProductDetail(productDetail);
					dbUser.setRefundTransactionCustomerEmailFlag(refundTransactionCustomerEmailFlag);
					dbUser.setRefundTransactionMerchantEmailFlag(refundTransactionMerchantEmailFlag);
					dbUser.setRegistrationDate(registrationDate);
					dbUser.setResellerId(resellerId);
					dbUser.setRetryTransactionCustomeFlag(retryTransactionCustomeFlag);
					dbUser.setState(state);
					dbUser.setSurchargeFlag(surchargeFlag);
					dbUser.setTelephoneNo(telephoneNo);
					dbUser.setTransactionAuthenticationEmailFlag(transactionAuthenticationEmailFlag);
					dbUser.setTransactionCustomerEmailFlag(refundTransactionCustomerEmailFlag);
					dbUser.setTransactionEmailId(transactionEmailId);
					dbUser.setTransactionEmailerFlag(transactionEmailerFlag);
					dbUser.setTransactionSmsFlag(transactionSmsFlag);
					dbUser.setUserStatus(userStatus);
					dbUser.setWebsite(website);
					dbUser.setWhiteListIpAddress(whiteListIpAddress);
					dbUser.setUpdateDate(date);
					dbUser.setUpdatedBy(loginEmailId);
					dbUser.setTransactionSms(transactionSms);
					dbUser.setSkipOrderIdForRefund(skipOrderIdForRefund);
					dbUser.setAllowDuplicateOrderId(allowDuplicateOrderId);
					dbUser.setPaymentMessageSlab(paymentMessageSlab);
					dbUser.setSettlementNamingConvention(settlementNamingConvention);
					dbUser.setRefundValidationNamingConvention(refundValidationNamingConvention);
					
					userDao.update(dbUser);

					PendingUserApproval pendingUser = pendingUserApprovalDao.find(payId);
					pendingUser.setProcessedBy(loginEmailId);
					pendingUser.setRequestStatus(TDRStatus.ACCEPTED.toString());
					pendingUser.setUpdateDate(date);
					pendingUserApprovalDao.update(pendingUser);
				}
			}
			setResponse(ErrorType.MERCHANT_ACCOUNT_EDIT_REQUEST_ACCEPT.getResponseMessage());
			return SUCCESS;
		} catch (Exception e) {
			logger.error("Exception " , e);
			return ERROR;
		}
	}
	public void validate() {

		if (validator.validateBlankField(getEmailId())) {
		} else if (!validator.validateField(CrmFieldType.EMAILID, getEmailId())) {
			addFieldError(CrmFieldType.EMAILID.getName(), ErrorType.INVALID_EMAIL_ID.getResponseMessage());
		}

		if (validator.validateBlankField(getPayId())) {
		} else if (!validator.validateField(CrmFieldType.PAY_ID, getPayId())) {
			addFieldError(CrmFieldType.PAY_ID.getName(), ErrorType.PAY_ID.getResponseMessage());
		}
		if (validator.validateBlankField(getBusinessName())) {
		} else if (!validator.validateField(CrmFieldType.BUSINESS_NAME, getBusinessName())) {
			addFieldError(CrmFieldType.BUSINESS_NAME.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getFirstName())) {
		} else if (!validator.validateField(CrmFieldType.FIRSTNAME, getFirstName())) {
			addFieldError(CrmFieldType.FIRSTNAME.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getLastName())) {
		} else if (!validator.validateField(CrmFieldType.LASTNAME, getLastName())) {
			addFieldError(CrmFieldType.LASTNAME.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getCompanyName())) {
		} else if (!validator.validateField(CrmFieldType.COMPANY_NAME, getCompanyName())) {
			addFieldError(CrmFieldType.COMPANY_NAME.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getContactPerson())) {
		} else if (!validator.validateField(CrmFieldType.CONTACT_PERSON, getContactPerson())) {
			addFieldError(CrmFieldType.CONTACT_PERSON.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getMerchantType())) {
		} else if (!validator.validateField(CrmFieldType.MERCHANT_TYPE, getMerchantType())) {
			addFieldError(CrmFieldType.MERCHANT_TYPE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getResellerId())) {
		} else if (!validator.validateField(CrmFieldType.RESELLER_ID, getResellerId())) {
			addFieldError(CrmFieldType.RESELLER_ID.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getRequestedBy())) {
		} else if (!validator.validateField(CrmFieldType.PRODUCT_DETAIL, getProductDetail())) {
			addFieldError(CrmFieldType.PRODUCT_DETAIL.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getRequestedBy())) {
		} else if (!validator.validateField(CrmFieldType.EMAILID, getRequestedBy())) {
			addFieldError(CrmFieldType.EMAILID.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getMobile())) {
		} else if (!validator.validateField(CrmFieldType.MOBILE, getMobile())) {
			addFieldError(CrmFieldType.MOBILE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getTelephoneNo())) {
		} else if (!validator.validateField(CrmFieldType.TELEPHONE_NO, getTelephoneNo())) {
			addFieldError(CrmFieldType.TELEPHONE_NO.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getFax())) {
		} else if (!validator.validateField(CrmFieldType.FAX, getFax())) {
			addFieldError(CrmFieldType.FAX.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getAddress())) {
		} else if (!validator.validateField(CrmFieldType.ADDRESS, getAddress())) {
			addFieldError(CrmFieldType.ADDRESS.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getCity())) {
		} else if (!validator.validateField(CrmFieldType.CITY, getCity())) {
			addFieldError(CrmFieldType.CITY.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getState())) {
		} else if (!validator.validateField(CrmFieldType.STATE, getState())) {
			addFieldError(CrmFieldType.STATE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getCountry())) {
		} else if (!validator.validateField(CrmFieldType.COUNTRY, getCountry())) {
			addFieldError(CrmFieldType.COUNTRY.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getPostalCode())) {
		} else if (!validator.validateField(CrmFieldType.POSTALCODE, getPostalCode())) {
			addFieldError(CrmFieldType.POSTALCODE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getComments())) {
		} else if (!validator.validateField(CrmFieldType.COMMENTS, getComments())) {
			addFieldError(CrmFieldType.COMMENTS.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getWhiteListIpAddress())) {
		} else if (!validator.validateField(CrmFieldType.WHITE_LIST_IPADDRES, getWhiteListIpAddress())) {
			addFieldError(CrmFieldType.WHITE_LIST_IPADDRES.getName(),
					ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getBankName())) {
		} else if (!validator.validateField(CrmFieldType.BANK_NAME, getBankName())) {
			addFieldError(CrmFieldType.BANK_NAME.getName(), ErrorType.BANK_NAME.getResponseMessage());
		}
		if (validator.validateBlankField(getIfscCode())) {
		} else if (!validator.validateField(CrmFieldType.IFSC_CODE, getIfscCode())) {
			addFieldError(CrmFieldType.IFSC_CODE.getName(), ErrorType.IFSC_CODE.getResponseMessage());
		}
		if (validator.validateBlankField(getAccHolderName())) {
		} else if (!validator.validateField(CrmFieldType.ACC_HOLDER_NAME, getAccHolderName())) {
			addFieldError(CrmFieldType.ACC_HOLDER_NAME.getName(), ErrorType.ACC_HOLDER_NAME.getResponseMessage());
		}
		if (validator.validateBlankField(getCurrency())) {
		} else if (!validator.validateField(CrmFieldType.CURRENCY, getCurrency())) {
			addFieldError(CrmFieldType.CURRENCY.getName(), ErrorType.CURRENCY.getResponseMessage());
		}
		if (validator.validateBlankField(getBranchName())) {
		} else if (!validator.validateField(CrmFieldType.BRANCH_NAME, getBranchName())) {
			addFieldError(CrmFieldType.BRANCH_NAME.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getPanCard())) {
		} else if (!validator.validateField(CrmFieldType.PANCARD, getPanCard())) {
			addFieldError(CrmFieldType.PANCARD.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getAccountNo())) {
		} else if (!validator.validateField(CrmFieldType.ACCOUNT_NO, getAccountNo())) {
			addFieldError(CrmFieldType.ACCOUNT_NO.getName(), ErrorType.ACCOUNT_NO.getResponseMessage());
		}
		if (validator.validateBlankField(getOrganisationType())) {
		} else if (!validator.validateField(CrmFieldType.ORGANIZATIONTYPE, getOrganisationType())) {
			addFieldError(CrmFieldType.ORGANIZATIONTYPE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getWebsite())) {
		} else if (!validator.validateField(CrmFieldType.WEBSITE, getWebsite())) {
			addFieldError(CrmFieldType.WEBSITE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getMultiCurrency())) {
		} else if (!validator.validateField(CrmFieldType.MULTICURRENCY, getMultiCurrency())) {
			addFieldError(CrmFieldType.MULTICURRENCY.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getBusinessModel())) {
		} else if (!validator.validateField(CrmFieldType.BUSINESSMODEL, getBusinessModel())) {
			addFieldError(CrmFieldType.BUSINESSMODEL.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getOperationAddress())) {
		} else if (!validator.validateField(CrmFieldType.OPERATIONADDRESS, getOperationAddress())) {
			addFieldError(CrmFieldType.OPERATIONADDRESS.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getOperationState())) {
		} else if (!validator.validateField(CrmFieldType.PPERATION_STATE, getFirstName())) {
			addFieldError(CrmFieldType.PPERATION_STATE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getOperationCity())) {
		} else if (!validator.validateField(CrmFieldType.OPERATION_CITY, getOperationCity())) {
			addFieldError(CrmFieldType.OPERATION_CITY.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getOperationPostalCode())) {
		} else if (!validator.validateField(CrmFieldType.OPERATION_POSTAL_CODE, getOperationPostalCode())) {
			addFieldError(CrmFieldType.OPERATION_POSTAL_CODE.getName(),
					ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getDateOfEstablishment())) {
		} else if (!validator.validateField(CrmFieldType.DATE_OF_ESTABLISHMENT, getDateOfEstablishment())) {
			addFieldError(CrmFieldType.DATE_OF_ESTABLISHMENT.getName(),
					ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getCin())) {
		} else if (!validator.validateField(CrmFieldType.CIN, getCin())) {
			addFieldError(CrmFieldType.CIN.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getPan())) {
		} else if (!validator.validateField(CrmFieldType.PAN, getPan())) {
			addFieldError(CrmFieldType.PAN.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getPanName())) {
		} else if (!validator.validateField(CrmFieldType.PANNAME, getPanName())) {
			addFieldError(CrmFieldType.PANNAME.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getNoOfTransactions())) {
		} else if (!validator.validateField(CrmFieldType.NO_OF_TRANSACTIONS, getNoOfTransactions())) {
			addFieldError(CrmFieldType.NO_OF_TRANSACTIONS.getName(),
					ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getAmountOfTransactions())) {
		} else if (!validator.validateField(CrmFieldType.AMOUNT_OF_TRANSACTIONS, getAmountOfTransactions())) {
			addFieldError(CrmFieldType.AMOUNT_OF_TRANSACTIONS.getName(),
					ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getAttemptTrasacation())) {
		} else if (!validator.validateField(CrmFieldType.ATTEMPT_TRASACATION, getAttemptTrasacation())) {
			addFieldError(CrmFieldType.ATTEMPT_TRASACATION.getName(),
					ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getIndustryCategory())) {
		} else if (!validator.validateField(CrmFieldType.INDUSTRY_CATEGORY, getIndustryCategory())) {
			addFieldError(CrmFieldType.INDUSTRY_CATEGORY.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getIndustrySubCategory())) {
		} else if (!validator.validateField(CrmFieldType.INDUSTRY_SUB_CATEGORY, getIndustrySubCategory())) {
			addFieldError(CrmFieldType.INDUSTRY_SUB_CATEGORY.getName(),
					ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		
		if (validator.validateBlankField(getTransactionEmailId())) {
		} else if (!validator.validateField(CrmFieldType.MOBILE,getTransactionMobileNo())) {
			addFieldError(CrmFieldType.MOBILE.getName(),
					ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		
		
		if (validator.validateBlankField(getTransactionEmailId())) {
		} else if (!validator.validateField(CrmFieldType.TRANSACTION_EMAIL_ID, getTransactionEmailId())) {
			addFieldError(CrmFieldType.TRANSACTION_EMAIL_ID.getName(),
					ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		
		if (validator.validateBlankField(getDefaultCurrency())) {
		} else if (!validator.validateField(CrmFieldType.DEFAULT_CURRENCY, getDefaultCurrency())) {
			addFieldError(CrmFieldType.DEFAULT_CURRENCY.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getAmexSellerId())) {
		} else if (!validator.validateField(CrmFieldType.AMEX_SELLER_ID, getAmexSellerId())) {
			addFieldError(CrmFieldType.AMEX_SELLER_ID.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getmCC())) {
		} else if (!validator.validateField(CrmFieldType.MCC, getmCC())) {
			addFieldError(CrmFieldType.MCC.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getDefaultLanguage())) {
		} else if (!validator.validateField(CrmFieldType.DEFAULT_LANGUAGE, getDefaultLanguage())) {
			addFieldError(CrmFieldType.DEFAULT_LANGUAGE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getLoginEmailId())) {
		} else if (!validator.validateField(CrmFieldType.EMAILID, getLoginEmailId())) {
			addFieldError(CrmFieldType.EMAILID.getName(), ErrorType.INVALID_EMAIL_ID.getResponseMessage());
		}
		if (validator.validateBlankField(getOperation())) {
		} else if (!validator.validateField(CrmFieldType.OPERATION, getOperation())) {
			addFieldError(CrmFieldType.OPERATION.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getSettlementNamingConvention())) {
		} else if (!validator.validateField(CrmFieldType.SETTLEMENT, getOperation())) {
			addFieldError(CrmFieldType.SETTLEMENT.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getRefundValidationNamingConvention())) {
		} else if (!validator.validateField(CrmFieldType.REFUND_VALIDATION, getOperation())) {
			addFieldError(CrmFieldType.SETTLEMENT.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}

	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
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

	public String getContactPerson() {
		return contactPerson;
	}

	public void setContactPerson(String contactPerson) {
		this.contactPerson = contactPerson;
	}

	public String getMerchantType() {
		return merchantType;
	}

	public void setMerchantType(String merchantType) {
		this.merchantType = merchantType;
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

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public String getRequestedBy() {
		return requestedBy;
	}

	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
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

	public boolean isEmailValidationFlag() {
		return emailValidationFlag;
	}

	public void setEmailValidationFlag(boolean emailValidationFlag) {
		this.emailValidationFlag = emailValidationFlag;
	}

	public String getOrganisationType() {
		return organisationType;
	}

	public void setOrganisationType(String organisationType) {
		this.organisationType = organisationType;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
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

	public String getAttemptTrasacation() {
		return attemptTrasacation;
	}

	public void setAttemptTrasacation(String attemptTrasacation) {
		this.attemptTrasacation = attemptTrasacation;
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

	public boolean isExpressPayFlag() {
		return expressPayFlag;
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

	public boolean isRetryTransactionCustomeFlag() {
		return retryTransactionCustomeFlag;
	}

	public void setRetryTransactionCustomeFlag(boolean retryTransactionCustomeFlag) {
		this.retryTransactionCustomeFlag = retryTransactionCustomeFlag;
	}

	public boolean isSurchargeFlag() {
		return surchargeFlag;
	}

	public void setSurchargeFlag(boolean surchargeFlag) {
		this.surchargeFlag = surchargeFlag;
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

	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	public UserStatusType getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(UserStatusType userStatus) {
		this.userStatus = userStatus;
	}

	public String getLoginEmailId() {
		return loginEmailId;
	}

	public void setLoginEmailId(String loginEmailId) {
		this.loginEmailId = loginEmailId;
	}

	public UserType getLoginUserType() {
		return loginUserType;
	}

	public void setLoginUserType(UserType loginUserType) {
		this.loginUserType = loginUserType;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getTransactionMobileNo() {
		return transactionMobileNo;
	}

	public void setTransactionMobileNo(String transactionMobileNo) {
		this.transactionMobileNo = transactionMobileNo;
	}

	public String getTransactionSms() {
		return transactionSms;
	}

	public void setTransactionSms(String transactionSms) {
		this.transactionSms = transactionSms;
	}

	public boolean isSkipOrderIdForRefund() {
		return skipOrderIdForRefund;
	}

	public void setSkipOrderIdForRefund(boolean skipOrderIdForRefund) {
		this.skipOrderIdForRefund = skipOrderIdForRefund;
	}

	public OrderIdType getAllowDuplicateOrderId() {
		return allowDuplicateOrderId;
	}

	public void setAllowDuplicateOrderId(OrderIdType allowDuplicateOrderId) {
		this.allowDuplicateOrderId = allowDuplicateOrderId;
	}

	public String getPaymentMessageSlab() {
		return paymentMessageSlab;
	}

	public void setPaymentMessageSlab(String paymentMessageSlab) {
		this.paymentMessageSlab = paymentMessageSlab;
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
	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}


}
