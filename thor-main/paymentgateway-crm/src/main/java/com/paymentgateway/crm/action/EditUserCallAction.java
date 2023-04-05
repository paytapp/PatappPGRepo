package com.paymentgateway.crm.action;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.Permissions;
import com.paymentgateway.commons.user.Roles;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;

public class EditUserCallAction extends AbstractSecureAction{
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private CrmValidator validator;
	
	@Autowired
	private UserSettingDao userSettingDao;


	private static final long serialVersionUID = 6921991472327981800L;

	private static Logger logger = LoggerFactory.getLogger(EditUserCallAction.class.getName());
	
	private List<PermissionType> listPermissionType;
	
	private String firstName;
	private String lastName;
	private String mobile;
	private String emailId;
	private Boolean isActive;
	private String permissionString;
	private String subUserType;
	private String businessName;
	
	private boolean merchantInitiatedDirectFlag;
	private boolean accountVerificationFlag;
	private boolean eNachReportFlag;
	private boolean virtualAccountFlag;
	private boolean vpaVerificationFlag;
	private boolean bookingReportFlag;
	private boolean customerQrFlag;
	private boolean capturedMerchantFlag;
	private boolean upiAutoPayReportFlag;
	
	public String execute() {
		try {
			User sessionUser = null;			
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if(sessionUser.getUserType().equals(UserType.MERCHANT)) {
				User merchantSubUser = userDao.findByEmailId(emailId);
				
				UserSettingData userSettings = userSettingDao.fetchDataUsingPayId(merchantSubUser.getParentPayId());
				
				if(sessionUser.isSuperMerchant()) {
					setListPermissionType(PermissionType.getSubUserPermissionTypeForSuperMerchant());
				} else {
					setListPermissionType(PermissionType.getSubUserPermissionType());
				}
				setMerchantInitiatedDirectFlag(userSettings.isMerchantInitiatedDirectFlag());
				
//				setCapturedMerchantFlag(merchant1.isCapturedMerchantFlag());
//				setBookingReportFlag(merchant1.isBookingRecord());
//				setCustomerQrFlag(merchant1.isCustomerQrFlag());
//				setMerchantInitiatedDirectFlag(merchant1.isMerchantInitiatedDirectFlag());
//				seteNachReportFlag(merchant1.iseNachReportFlag());
//				setVirtualAccountFlag(merchant1.isVirtualAccountFlag());
//				setAccountVerificationFlag(merchant1.isAccountVerificationFlag());
//				setVpaVerificationFlag(merchant1.isVpaVerificationFlag());
//				setUpiAutoPayReportFlag(merchant1.isUpiAutoPayReportFlag());
				//setListPermissionType(PermissionType.getSubUserPermissionType());
				
//				if(!merchantUser.isAccountVerificationFlag()) {
//					listPermissionType.remove(PermissionType.getInstanceFromName("Account Verification"));
//				}
//				if(!merchantUser.isVpaVerificationFlag()){
//					listPermissionType.remove(PermissionType.getInstanceFromName("VPA Verification"));
//				}
				/*if(sessionUser.iseNachReportFlag()) {
					seteNachReport(true);
				}*/
				getPermissions();
				setSubUserType(getSubUserTypeValue());
			}else if(sessionUser.getUserType().equals(UserType.ACQUIRER)){
				setListPermissionType(PermissionType.getSubAcquirerPermissionType());
				getPermissions();
			}else if(sessionUser.getUserType().equals(UserType.ADMIN) ||  sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				getPermissions();
				setListPermissionType(PermissionType.getSubUserPermissionType());
				setSubUserType(getSubUserTypeValue());
				User merchantSubUser = userDao.findByEmailId(emailId);
				UserSettingData userSettings = userSettingDao.fetchDataUsingPayId(merchantSubUser.getParentPayId());
				setMerchantInitiatedDirectFlag(userSettings.isMerchantInitiatedDirectFlag());
				
//				seteNachReportFlag(merchant1.iseNachReportFlag());
//				setVirtualAccountFlag(merchant1.isVirtualAccountFlag());
//				setAccountVerificationFlag(merchant1.isAccountVerificationFlag());
//				setVpaVerificationFlag(merchant1.isVpaVerificationFlag());
//				setUpiAutoPayReportFlag(merchant1.isUpiAutoPayReportFlag());
			}
			

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return INPUT;
	}
	
	private void getPermissions() {
		Session session = null;
		try {
			//no need to use SystemSetting here Sub User details fetching
			User subUser = userDao.find(getEmailId());
			session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			session.load(subUser, subUser.getEmailId());
			
			Set<Roles> roles = subUser.getRoles();
			Set<Permissions> permissions = roles.iterator().next().getPermissions();
			if (!permissions.isEmpty()) {
				StringBuilder perms = new StringBuilder();
//				if(subUser.isEposMerchant()) {
//					perms.append("allowEpos");
//					perms.append("-");
//				}
				Iterator<Permissions> itr = permissions.iterator();
				while (itr.hasNext()) {
					PermissionType permissionType = itr.next().getPermissionType();
					
					if(!subUser.isEposMerchant() && permissionType.getPermission().equalsIgnoreCase("Allow ePOS")){
						continue;
					}
					
					if(!subUser.isVpaVerificationFlag() && permissionType.getPermission().equalsIgnoreCase("VPA Verification")){
						continue;
					}
					if(!subUser.isAccountVerificationFlag() && permissionType.getPermission().equalsIgnoreCase("Account Verification")){
						continue;
					}
					perms.append(permissionType.getPermission());
					perms.append("-");
				}
				if(StringUtils.isNotEmpty(perms.toString()))
					perms.deleteCharAt(perms.length() - 1);
				if(subUser.isRetailMerchantFlag() && (!subUser.getSubUserType().equalsIgnoreCase("vendorType"))) {
					perms.append("-Vendor Report");
				}
				setPermissionString(perms.toString());
			}
			tx.commit();
		} finally {
			HibernateSessionProvider.closeSession(session);
		}
	}
	
	public String getSubUserTypeValue() {
		User subUser = userDao.findByEmailId(emailId);
		String suUserType="";
		
		if(!StringUtils.isEmpty(subUser.getSubUserType())){
			suUserType = subUser.getSubUserType();
		}
		setBusinessName(subUser.getBusinessName());
		return suUserType;
	}
	public void validate() {
		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
			if (sessionUser.isSuperMerchant()) {
				setListPermissionType(PermissionType.getSubUserPermissionTypeForSuperMerchant());
			} else {
				setListPermissionType(PermissionType.getSubUserPermissionType());
			}
		}

		if ((validator.validateBlankField(getFirstName()))) {
			addFieldError(CrmFieldType.FIRSTNAME.getName(), validator
					.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.FIRSTNAME,
				getFirstName()))) {
			addFieldError(CrmFieldType.FIRSTNAME.getName(), validator
					.getResonseObject().getResponseMessage());
		}

		if ((validator.validateBlankField(getLastName()))) {
			addFieldError(CrmFieldType.LASTNAME.getName(), validator
					.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.LASTNAME,
				getLastName()))) {
			addFieldError(CrmFieldType.LASTNAME.getName(), validator
					.getResonseObject().getResponseMessage());
		}

		if (validator.validateBlankField(getMobile())) {
			addFieldError(CrmFieldType.MOBILE.getName(), validator
					.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.MOBILE, getMobile()))) {
			addFieldError(CrmFieldType.MOBILE.getName(), validator
					.getResonseObject().getResponseMessage());
		}

		if (validator.validateBlankField(getEmailId())) {
			addFieldError(CrmFieldType.EMAILID.getName(), validator
					.getResonseObject().getResponseMessage());
		} else if (!(validator.isValidEmailId(getEmailId()))) {
			addFieldError(CrmFieldType.EMAILID.getName(),
					ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
	}
	
	public List<PermissionType> getListPermissionType() {
		return listPermissionType;
	}
	public void setListPermissionType(List<PermissionType> listPermissionType) {
		this.listPermissionType = listPermissionType;
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

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public String getPermissionString() {
		return permissionString;
	}

	public void setPermissionString(String permissionString) {
		this.permissionString = permissionString;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getSubUserType() {
		return subUserType;
	}

	public void setSubUserType(String subUserType) {
		this.subUserType = subUserType;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public boolean isMerchantInitiatedDirectFlag() {
		return merchantInitiatedDirectFlag;
	}

	public void setMerchantInitiatedDirectFlag(boolean merchantInitiatedDirectFlag) {
		this.merchantInitiatedDirectFlag = merchantInitiatedDirectFlag;
	}

	public boolean isAccountVerificationFlag() {
		return accountVerificationFlag;
	}

	public boolean isVpaVerificationFlag() {
		return vpaVerificationFlag;
	}

	public void setAccountVerificationFlag(boolean accountVerificationFlag) {
		this.accountVerificationFlag = accountVerificationFlag;
	}

	public void setVpaVerificationFlag(boolean vpaVerificationFlag) {
		this.vpaVerificationFlag = vpaVerificationFlag;
	}

	public boolean iseNachReportFlag() {
		return eNachReportFlag;
	}

	public void seteNachReportFlag(boolean eNachReportFlag) {
		this.eNachReportFlag = eNachReportFlag;
	}

	public boolean isVirtualAccountFlag() {
		return virtualAccountFlag;
	}

	public void setVirtualAccountFlag(boolean virtualAccountFlag) {
		this.virtualAccountFlag = virtualAccountFlag;
	}

	public boolean isBookingReportFlag() {
		return bookingReportFlag;
	}

	public void setBookingReportFlag(boolean bookingReportFlag) {
		this.bookingReportFlag = bookingReportFlag;
	}

	public boolean isCustomerQrFlag() {
		return customerQrFlag;
	}

	public void setCustomerQrFlag(boolean customerQrFlag) {
		this.customerQrFlag = customerQrFlag;
	}

	public boolean isCapturedMerchantFlag() {
		return capturedMerchantFlag;
	}

	public void setCapturedMerchantFlag(boolean capturedMerchantFlag) {
		this.capturedMerchantFlag = capturedMerchantFlag;
	}
	public boolean isUpiAutoPayReportFlag() {
		return upiAutoPayReportFlag;
	}

	public void setUpiAutoPayReportFlag(boolean upiAutoPayReportFlag) {
		this.upiAutoPayReportFlag = upiAutoPayReportFlag;
	}
	
}
