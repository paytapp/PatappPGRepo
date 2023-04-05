package com.paymentgateway.crm.action;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
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
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.UserStatusType;

/**
 * @ Neeraj
 */
public class SubUserEditAction extends AbstractSecureAction {

	@Autowired
	private UserDao userDao;

	@Autowired
	private CrmValidator validator;
	
	@Autowired
	private UserSettingDao userSettingDao;

	private static final long serialVersionUID = -2429379754814283308L;
	private static Logger logger = LoggerFactory.getLogger(SubUserEditAction.class.getName());
	private String firstName;
	private String lastName;
	private String mobile;
	private String emailId;
	private Boolean isActive;
	private String businessName;
	private String allowEpos;
	private String subUserType;
	private boolean accountVerificationFlag;
	private boolean vpaVerificationFlag;
	private List<String> lstPermissionType;
	private List<PermissionType> listPermissionType;
	private String permissionString;
	private boolean merchantInitiatedDirectFlag;
	private boolean eNachReportFlag;
	private boolean virtualAccountFlag;
	private boolean bookingReportFlag;
	private boolean customerQrFlag;
	private boolean capturedMerchantFlag;
	private boolean upiAutoPayReportFlag;

	public String editSubUser() {
		try {
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			User user = new User();
			user = userDao.find(getEmailId());
			User merchant1 = userDao.findPayId(user.getParentPayId());
			
			UserSettingData userSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());
			UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchant1.getPayId());
			
			editPermission(user);
			user.setFirstName(getFirstName());
			user.setLastName(getLastName());
			user.setMobile(getMobile());
			user.setBusinessName(getBusinessName());

				if (lstPermissionType.contains("Allow ePOS")) {
					merchantSettings.setEposMerchant(true);
				} else {
					merchantSettings.setEposMerchant(false);
				}

				if (lstPermissionType.contains("Account Verification")) {
					merchantSettings.setAccountVerificationFlag(true);
				} else {
					merchantSettings.setAccountVerificationFlag(false);
				}

				if (lstPermissionType.contains("VPA Verification")) {
					merchantSettings.setVpaVerificationFlag(true);
				} else {
					merchantSettings.setVpaVerificationFlag(false);
				}
				
			if (getIsActive()) {
				user.setUserStatus(UserStatusType.ACTIVE);
			} else {
				user.setUserStatus(UserStatusType.PENDING);
			}
			setCapturedMerchantFlag(merchantSettings.isCapturedMerchantFlag());
			setBookingReportFlag(merchantSettings.isBookingRecord());
			setCustomerQrFlag(merchantSettings.isCustomerQrFlag());
			setMerchantInitiatedDirectFlag(merchantSettings.isMerchantInitiatedDirectFlag());
			seteNachReportFlag(merchantSettings.iseNachReportFlag());
			setVirtualAccountFlag(merchantSettings.isVirtualAccountFlag());
			setAccountVerificationFlag(merchantSettings.isAccountVerificationFlag());
			setVpaVerificationFlag(merchantSettings.isVpaVerificationFlag());
			setUpiAutoPayReportFlag(merchantSettings.isUpiAutoPayReportFlag());
			setSubUserType(getSubUserTypeValue());
			userDao.update(user);
			addActionMessage(CrmFieldConstants.USER_DETAILS_UPDATED.getValue());
			return INPUT;
		} catch (

		Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	private void editPermission(User user) {
		Session session = null;
		User sessionUser = null;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			session.load(user, user.getEmailId());

			Set<Roles> roles = user.getRoles();
			Iterator<Roles> itr = roles.iterator();
			Roles role = new Roles();
			if (!roles.isEmpty()) {
				role = itr.next();
				Iterator<Permissions> permissionIterator = role.getPermissions().iterator();
				while (permissionIterator.hasNext()) {
					// not used but compulsory for iterator working
					@SuppressWarnings("unused")
					Permissions permission = permissionIterator.next();
					permissionIterator.remove();
				}
			}
			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				if (sessionUser.isSuperMerchant()) {
					setListPermissionType(PermissionType.getSubUserPermissionTypeForSuperMerchant());
				} else {
					setListPermissionType(PermissionType.getSubUserPermissionType());
				}
				if (lstPermissionType == null) {

				} else {
					for (String permissionType : lstPermissionType) {
						Permissions permission = new Permissions();
						permission.setPermissionType(PermissionType.getInstanceFromName(permissionType));
						role.addPermission(permission);
					}
				}
			} else if (sessionUser.getUserType().equals(UserType.ACQUIRER)) {
				setListPermissionType(PermissionType.getSubAcquirerPermissionType());
				if (lstPermissionType == null) {

				} else {
					for (String permissionType : lstPermissionType) {
						Permissions permission = new Permissions();
						permission.setPermissionType(PermissionType.getInstanceFromName(permissionType));
						role.addPermission(permission);
					}
				}
			} else if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				setListPermissionType(PermissionType.getSubUserPermissionType());
				if (lstPermissionType == null) {

				} else {
					for (String permissionType : lstPermissionType) {
						Permissions permission = new Permissions();
						permission.setPermissionType(PermissionType.getInstanceFromName(permissionType));
						role.addPermission(permission);
					}
				}
			}

			// set permission string
			getPermissions(user);
			tx.commit();
		} finally {
			HibernateSessionProvider.closeSession(session);
		}
	}

	private void getPermissions(User agent) {
		Set<Roles> roles = agent.getRoles();
		Set<Permissions> permissions = roles.iterator().next().getPermissions();
		if (!permissions.isEmpty()) {
			StringBuilder perms = new StringBuilder();
			Iterator<Permissions> itr = permissions.iterator();
			while (itr.hasNext()) {
				PermissionType permissionType = itr.next().getPermissionType();
				perms.append(permissionType.getPermission());
				perms.append("-");
			}
			perms.deleteCharAt(perms.length() - 1);
			setPermissionString(perms.toString());
		}
	}

	public String getSubUserTypeValue() {
		User subUser = userDao.findByEmailId(emailId);
		String suUserType = "";

		if (!StringUtils.isEmpty(subUser.getSubUserType())) {
			suUserType = subUser.getSubUserType();
		}
		setBusinessName(subUser.getBusinessName());
		return suUserType;
	}

	public void validate() {

		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
			setListPermissionType(PermissionType.getSubUserPermissionType());
		} else if (sessionUser.getUserType().equals(UserType.ACQUIRER)) {
			setListPermissionType(PermissionType.getSubAcquirerPermissionType());
		}

		if ((validator.validateBlankField(getFirstName()))) {
			addFieldError(CrmFieldType.FIRSTNAME.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.FIRSTNAME, getFirstName()))) {
			addFieldError(CrmFieldType.FIRSTNAME.getName(), validator.getResonseObject().getResponseMessage());
		}

		if ((validator.validateBlankField(getLastName()))) {
			addFieldError(CrmFieldType.LASTNAME.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.LASTNAME, getLastName()))) {
			addFieldError(CrmFieldType.LASTNAME.getName(), validator.getResonseObject().getResponseMessage());
		}

		if (validator.validateBlankField(getMobile())) {
			addFieldError(CrmFieldType.MOBILE.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.MOBILE, getMobile()))) {
			addFieldError(CrmFieldType.MOBILE.getName(), validator.getResonseObject().getResponseMessage());
		}

		if (validator.validateBlankField(getEmailId())) {
			addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.isValidEmailId(getEmailId()))) {
			addFieldError(CrmFieldType.EMAILID.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
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

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public List<String> getLstPermissionType() {
		return lstPermissionType;
	}

	public void setLstPermissionType(List<String> lstPermissionType) {
		this.lstPermissionType = lstPermissionType;
	}

	public List<PermissionType> getListPermissionType() {
		return listPermissionType;
	}

	public void setListPermissionType(List<PermissionType> listPermissionType) {
		this.listPermissionType = listPermissionType;
	}

	public String getPermissionString() {
		return permissionString;
	}

	public void setPermissionString(String permissionString) {
		this.permissionString = permissionString;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getAllowEpos() {
		return allowEpos;
	}

	public void setAllowEpos(String allowEpos) {
		this.allowEpos = allowEpos;
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

	public String getSubUserType() {
		return subUserType;
	}

	public void setSubUserType(String subUserType) {
		this.subUserType = subUserType;
	}

	public boolean isMerchantInitiatedDirectFlag() {
		return merchantInitiatedDirectFlag;
	}

	public void setMerchantInitiatedDirectFlag(boolean merchantInitiatedDirectFlag) {
		this.merchantInitiatedDirectFlag = merchantInitiatedDirectFlag;
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
