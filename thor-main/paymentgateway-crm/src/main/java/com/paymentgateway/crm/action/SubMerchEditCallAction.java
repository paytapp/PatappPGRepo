package com.paymentgateway.crm.action;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.UserStatusType;

public class SubMerchEditCallAction extends AbstractSecureAction {

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private CrmValidator validator;
	
	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private UserSettingDao userSettingDao;


	private static final long serialVersionUID = -6368002305712366054L;
	private static Logger logger = LoggerFactory.getLogger(SubMerchEditCallAction.class.getName());



	private String firstName;
	private String lastName;
	private String businessName;
	private String mobile;
	private String emailId;
	private UserStatusType status;
	private String currentStatus;
	private String subMerchantPayId;
	private String superMerchantId;
	private String superMerchantName;
	private String subMerchantSalt;
	private String paymentUrl;
	private boolean bookingRecord;
	private boolean logoFlag;
	private String accountHolderName;
	private String accountIfsc;
	private String accountMobileNumber;
	private String accountNumber;
	private boolean capturedMerchantFlag;
	private boolean retailMerchantFlag;
	private boolean accountVerificationFlag;
	private boolean loadWalletFlag;
	private boolean eposMerchant;
	private boolean checkOutJsFlag;
	private boolean nodalReportFlag;
	
	public String execute() {

		try {
			setPaymentUrl(propertiesManager.propertiesMap.get("RequestURL"));
			User sessionUser = null;
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) ||
					(sessionUser.getUserType().equals(UserType.MERCHANT) && sessionUser.isSuperMerchant() )) { 
				
				User subMerchUser = userDao.findPayIdByEmail(getEmailId());
				UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(subMerchUser.getPayId());

				
				if (subMerchUser != null) {
					
					String salt = propertiesManager.getSalt(subMerchUser.getPayId());		
					setSubMerchantSalt(salt);
					if (StringUtils.isNotBlank(subMerchUser.getFirstName())) {
						setFirstName(subMerchUser.getFirstName());
					}
					else {
						setFirstName("");
					}
					
					if (StringUtils.isNotBlank(subMerchUser.getLastName())) {
						setLastName(subMerchUser.getLastName());
					}
					else {
						setLastName("");
					}
					
					if (StringUtils.isNotBlank(subMerchUser.getAccHolderName())) {
						setAccountHolderName(subMerchUser.getAccHolderName());
					}
					else {
						setAccountHolderName("");
					}
					
					if (StringUtils.isNotBlank(subMerchUser.getIfscCode())) {
						setAccountIfsc(subMerchUser.getIfscCode());
					}
					else {
						setAccountIfsc("");
					}
					
					if (StringUtils.isNotBlank(subMerchUser.getAccountNo())) {
						setAccountNumber(subMerchUser.getAccountNo());
					}
					else {
						setAccountNumber("");
					}
					
					if (StringUtils.isNotBlank(subMerchUser.getTelephoneNo())) {
						setAccountMobileNumber(subMerchUser.getTelephoneNo());
					}
					else {
						setAccountMobileNumber("");
					}
					
					setSuperMerchantName(userDao.getBusinessNameByPayId(subMerchUser.getSuperMerchantId()));
//					setEposMerchant(subMerchUser.isEposMerchant());
					setBusinessName(subMerchUser.getBusinessName());
					setMobile(subMerchUser.getMobile());
					setEmailId(subMerchUser.getEmailId());
					setCurrentStatus(subMerchUser.getUserStatus().toString());
					setSubMerchantPayId(subMerchUser.getPayId());
					setSuperMerchantId(subMerchUser.getSuperMerchantId());
					setBookingRecord(merchantSettings.isBookingRecord());
					setCapturedMerchantFlag(merchantSettings.isCapturedMerchantFlag());
					setRetailMerchantFlag(merchantSettings.isRetailMerchantFlag());
					setLogoFlag(merchantSettings.isLogoFlag());
					setAccountVerificationFlag(merchantSettings.isAccountVerificationFlag());
					setLoadWalletFlag(merchantSettings.isLoadWalletFlag());
					setCheckOutJsFlag(subMerchUser.isCheckOutJsFlag());
					setNodalReportFlag(merchantSettings.isNodalReportFlag());
					setStatus(status);
				}
			}

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return INPUT;
	}

	public boolean isLogoFlag() {
		return logoFlag;
	}

	public void setLogoFlag(boolean logoFlag) {
		this.logoFlag = logoFlag;
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

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

	public String getSubMerchantSalt() {
		return subMerchantSalt;
	}

	public void setSubMerchantSalt(String subMerchantSalt) {
		this.subMerchantSalt = subMerchantSalt;
	}

	public String getPaymentUrl() {
		return paymentUrl;
	}

	public void setPaymentUrl(String paymentUrl) {
		this.paymentUrl = paymentUrl;
	}

	public String getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(String currentStatus) {
		this.currentStatus = currentStatus;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getSuperMerchantId() {
		return superMerchantId;
	}

	public void setSuperMerchantId(String superMerchantId) {
		this.superMerchantId = superMerchantId;
	}

	public UserStatusType getStatus() {
		return status;
	}

	public void setStatus(UserStatusType status) {
		this.status = status;
	}

	public boolean isBookingRecord() {
		return bookingRecord;
	}

	public void setBookingRecord(boolean bookingRecord) {
		this.bookingRecord = bookingRecord;
	}

	public String getAccountHolderName() {
		return accountHolderName;
	}

	public void setAccountHolderName(String accountHolderName) {
		this.accountHolderName = accountHolderName;
	}

	public String getAccountIfsc() {
		return accountIfsc;
	}

	public void setAccountIfsc(String accountIfsc) {
		this.accountIfsc = accountIfsc;
	}

	public String getAccountMobileNumber() {
		return accountMobileNumber;
	}

	public void setAccountMobileNumber(String accountMobileNumber) {
		this.accountMobileNumber = accountMobileNumber;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getSuperMerchantName() {
		return superMerchantName;
	}

	public void setSuperMerchantName(String superMerchantName) {
		this.superMerchantName = superMerchantName;
	}
	public boolean isCapturedMerchantFlag() {
		return capturedMerchantFlag;
	}

	public void setCapturedMerchantFlag(boolean capturedMerchantFlag) {
		this.capturedMerchantFlag = capturedMerchantFlag;
	}
	public boolean isRetailMerchantFlag() {
		return retailMerchantFlag;
	}

	public void setRetailMerchantFlag(boolean retailMerchantFlag) {
		this.retailMerchantFlag = retailMerchantFlag;
	}

	public boolean isEposMerchant() {
		return eposMerchant;
	}

	public void setEposMerchant(boolean eposMerchant) {
		this.eposMerchant = eposMerchant;
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
}
