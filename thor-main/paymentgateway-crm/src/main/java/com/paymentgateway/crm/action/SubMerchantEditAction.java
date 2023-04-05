package com.paymentgateway.crm.action;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.UserStatusType;

/**
 * @author Shaiwal
 *
 */
public class SubMerchantEditAction extends AbstractSecureAction {

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private CrmValidator validator;

	private static final long serialVersionUID = 1183250308057362565L;
	private static Logger logger = LoggerFactory.getLogger(SubMerchantEditAction.class.getName());
	private String firstName;
	private String lastName;
	private String businessName;
	private String mobile;
	private String emailId;
	private String status;
	private String currentStatus;
	private String subMerchantPayId;
	private String superMerchantId;
	private String subMerchantSalt;
	private String paymentUrl;
	private boolean bookingRecord;
	private boolean logoFlag;
	private File logoImageFile;
	private String accountHolderName;
	private String accountIfsc;
	private String accountMobileNumber;
	private String accountNumber;
	private String response;
	private boolean capturedMerchantFlag;
	private boolean retailMerchantFlag;
	private boolean eposMerchant;
	private boolean accountVerificationFlag;
	private boolean loadWalletFlag;
	private boolean checkOutJsFlag;
	private boolean nodalReportFlag;
	
	public String execute() {
		try {

			UserStatusType userStatus = null;

			if (StringUtils.isBlank(status) || status.equalsIgnoreCase("All")) {
				userStatus = null;
			} else if (status.equalsIgnoreCase("ACTIVE")) {
				userStatus = UserStatusType.ACTIVE;
			} else if (status.equalsIgnoreCase("PENDING")) {
				userStatus = UserStatusType.PENDING;
			} else if (status.equalsIgnoreCase("TRANSACTION_BLOCKED")) {
				userStatus = UserStatusType.TRANSACTION_BLOCKED;
			} else if (status.equalsIgnoreCase("SUSPENDED")) {
				userStatus = UserStatusType.SUSPENDED;
			} else if (status.equalsIgnoreCase("TERMINATED")) {
				userStatus = UserStatusType.TERMINATED;
			}

			setPaymentUrl(propertiesManager.propertiesMap.get("RequestURL"));
			User sessionUser = null;
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)
					|| (sessionUser.getUserType().equals(UserType.MERCHANT) && sessionUser.isSuperMerchant())) {

				String salt = propertiesManager.getSalt(subMerchantPayId);
				setSubMerchantSalt(salt);

				User subMerchUser = userDao.findPayId(subMerchantPayId);

				if (subMerchUser != null) {

					if (StringUtils.isNotBlank(getFirstName())) {
						subMerchUser.setFirstName(getFirstName());
					}

					if (StringUtils.isNotBlank(getLastName())) {
						subMerchUser.setLastName(getLastName());
					}

					if (userStatus != null) {
						subMerchUser.setUserStatus(userStatus);
					}
					
					if (StringUtils.isNotBlank(getAccountHolderName())) {
						subMerchUser.setAccHolderName(getAccountHolderName());
					} else {
						subMerchUser.setAccHolderName("");
					}
					
					if (StringUtils.isNotBlank(getAccountIfsc())) {
						subMerchUser.setIfscCode(getAccountIfsc());
					} else {
						subMerchUser.setIfscCode("");
					}
					
					if (StringUtils.isNotBlank(getAccountNumber())) {
						subMerchUser.setAccountNo(getAccountNumber());
					} else {
						subMerchUser.setAccountNo("");
					}
					
					if (StringUtils.isNotBlank(getAccountMobileNumber())) {
						subMerchUser.setTelephoneNo(getAccountMobileNumber());
					} else {
						subMerchUser.setTelephoneNo("");
					}
					
					
					subMerchUser.setUpdateDate(new Date());
					subMerchUser.setUpdatedBy(sessionUser.getEmailId());
//					subMerchUser.setBookingRecord(bookingRecord);
//					subMerchUser.setEposMerchant(eposMerchant);
//					subMerchUser.setCapturedMerchantFlag(capturedMerchantFlag);
//					subMerchUser.setRetailMerchantFlag(retailMerchantFlag);
//					subMerchUser.setLogoFlag(logoFlag);
//					subMerchUser.setAccountVerificationFlag(accountVerificationFlag);
//					subMerchUser.setLoadWalletFlag(loadWalletFlag);
//					subMerchUser.setCheckOutJsFlag(checkOutJsFlag);
//					subMerchUser.setNodalReportFlag(nodalReportFlag);
					
				}
				if (subMerchUser != null) {
					if (StringUtils.isNotBlank(subMerchUser.getFirstName())) {
						setFirstName(subMerchUser.getFirstName());
					} else {
						setFirstName("");
					}

					if (StringUtils.isNotBlank(subMerchUser.getLastName())) {
						setLastName(subMerchUser.getLastName());
					} else {
						setLastName("");
					}
					
					if (StringUtils.isNotBlank(subMerchUser.getAccHolderName())) {
						setAccountHolderName(subMerchUser.getAccHolderName());
					} else {
						setAccountHolderName("");
					}
					
					if (StringUtils.isNotBlank(subMerchUser.getIfscCode())) {
						setAccountIfsc(subMerchUser.getIfscCode());
					} else {
						setAccountIfsc("");
					}
					
					if (StringUtils.isNotBlank(subMerchUser.getAccountNo())) {
						setAccountNumber(subMerchUser.getAccountNo());
					} else {
						setAccountNumber("");
					}
					
					if (StringUtils.isNotBlank(subMerchUser.getTelephoneNo())) {
						setAccountMobileNumber(subMerchUser.getTelephoneNo());
					} else {
						setAccountMobileNumber("");
					}

					setBusinessName(subMerchUser.getBusinessName());
					setMobile(subMerchUser.getMobile());
					setEmailId(subMerchUser.getEmailId());
					setCurrentStatus(subMerchUser.getUserStatus().toString());
					setSubMerchantPayId(subMerchUser.getPayId());
					setSuperMerchantId(subMerchUser.getSuperMerchantId());
//					setBookingRecord(subMerchUser.isBookingRecord());
//					setEposMerchant(subMerchUser.isEposMerchant());
//					setCapturedMerchantFlag(subMerchUser.isCapturedMerchantFlag());
//					setRetailMerchantFlag(subMerchUser.isRetailMerchantFlag());
//					setLogoFlag(subMerchUser.isLogoFlag());
//					setAccountVerificationFlag(subMerchUser.isAccountVerificationFlag());
//					setLoadWalletFlag(subMerchUser.isLoadWalletFlag());
//					setNodalReportFlag(subMerchUser.isNodalReportFlag());
					saveLogoFile();
					userDao.update(subMerchUser);
					setResponse("Saved Successfully");
				}

			}

		} catch (Exception exception) {
			logger.error("Exception", exception);
			setResponse("Not Saved Successfully");
			return ERROR;
		}
		return INPUT;
	}

	public void validate() {

		if (StringUtils.isNotBlank(getFirstName())) {

			if (!(validator.validateField(CrmFieldType.FIRSTNAME, getFirstName()))) {
				addFieldError(CrmFieldType.FIRSTNAME.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (StringUtils.isNotBlank(getLastName())) {

			if (!(validator.validateField(CrmFieldType.LASTNAME, getLastName()))) {
				addFieldError(CrmFieldType.LASTNAME.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (validator.validateBlankField(getSubMerchantPayId())) {
			addFieldError(CrmFieldType.PAY_ID.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.PAY_ID, getSubMerchantPayId()))) {
			addFieldError(CrmFieldType.PAY_ID.getName(), validator.getResonseObject().getResponseMessage());
		}
	}
	public String saveLogoFile() {
		
		String srcfileName = subMerchantPayId + ".png";
		
		try {
			if(logoImageFile != null && subMerchantPayId !=null) {
				File destFile = new File(PropertiesManager.propertiesMap.get(Constants.LOGO_FILE_UPLOAD_LOCATION.getValue()) + "//" + subMerchantPayId, srcfileName);
				FileUtils.copyFile(logoImageFile, destFile);
			}
			
		} catch (IOException e) {
			logger.error("Exception cought Wile saving logoImage File : " , e);
		}
		return SUCCESS;
	}
	
	public boolean isLogoFlag() {
		return logoFlag;
	}

	public void setLogoFlag(boolean logoFlag) {
		this.logoFlag = logoFlag;
	}

	public File getLogoImageFile() {
		return logoImageFile;
	}

	public void setLogoImageFile(File logoImageFile) {
		this.logoImageFile = logoImageFile;
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

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(String currentStatus) {
		this.currentStatus = currentStatus;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

	public String getSuperMerchantId() {
		return superMerchantId;
	}

	public void setSuperMerchantId(String superMerchantId) {
		this.superMerchantId = superMerchantId;
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

	public boolean isBookingRecord() {
		return bookingRecord;
	}

	public void setBookingRecord(boolean bookingRecord) {
		this.bookingRecord = bookingRecord;
	}
	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
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
