package com.paymentgateway.crm.mpa;

import java.util.Date;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.paymentgateway.commons.util.ModeType;
import com.paymentgateway.commons.util.UserStatusType;

public class MPAMerchantDetailsPojo {
	
	//user

	@Enumerated(EnumType.STRING)
	private ModeType modeType;
	@Enumerated(EnumType.STRING)
	private UserStatusType userStatus;
	private String comments;
	
	private String payId;
	private String terminalId;
	private String resellerId;
	private String firstName;
	private String companyName;
	private Date registrationDate;
	private Date activationDate;
	private String businessName;
	private String lastName;
	private String industryCategory;
	private String industrySubCategory;
	private String emailId;
	
	private String mobile;
	private String telephoneNo;
	private String city;
	private String country;
	private String address;
	private String state;
	private String postalCode;
	
	private boolean transactionSmsFlag;
	private boolean transactionAuthenticationEmailFlag;
	private boolean transactionCustomerEmailFlag;
	private boolean refundTransactionCustomerEmailFlag;
	private boolean refundTransactionMerchantEmailFlag;
	private boolean transactionEmailerFlag;
	private String transactionEmailId;
	private String transactionSms;
	
	private String defaultCurrency;
	private String paymentMessageSlab;
	private boolean merchantHostedFlag;
	private boolean iframePaymentFlag;
	private boolean surchargeFlag;
	private boolean retryTransactionCustomeFlag;
	private boolean expressPayFlag;
	private String cardSaveParam;
	private boolean saveVPAFlag;
	private String vpaSaveParam;
	private boolean skipOrderIdForRefund;
	private boolean allowSaleDuplicate;
	private boolean allowRefundDuplicate;
	private boolean allowSaleInRefund;
	private boolean allowRefundInSale;
	private float extraRefundLimit;
	private String amexSellerId;
	private String mCC;
	
	private String settlementNamingConvention;
	private String refundValidationNamingConvention;
	
	private String bankName;
	private String ifscCode;
	private String accHolderName;
	private String currency;
	private String branchName;
	private String panCard;
	private String accountNo;
	
	private String organisationType;
	private String multiCurrency;
	private String businessModel;
	private String operationAddress;
	private String operationCity;
	private String operationPostalCode;
	private String dateOfEstablishment;
	private String cin;
	private String pan;
	private String panName;
	private String noOfTransactions;
	private String amountOfTransactions;
	private String merchantGstNo;
	
	//mpa

	private String accountNumber;
	private String accountIfsc;
	private String accountHolderName;
	private String businessPan;
	private String typeOfEntity;
	private String tradingAddress1;
	
	
	
	
	private String contactMobile;
	private String contactLandline;
	private String tradingCountry;
	private String tradingState;
	private String tradingPin;
	private String gstin;
	private String companyPhone;
	private String companyWebsite;
	private String companyEmailId;
}
