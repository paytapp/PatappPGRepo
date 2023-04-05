package com.paymentgateway.crm.action;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.Permissions;
import com.paymentgateway.commons.user.Roles;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.UserStatusType;

public class EditUserAction extends AbstractSecureAction {
	
	@Autowired
	private CrmValidator validator;
	
	@Autowired
	private UserDao userDao;

	private static Logger logger = LoggerFactory.getLogger(EditUserAction.class.getName());
	private static final long serialVersionUID = -3721233026057086346L;

	private String firstName;
	private String lastName;
	private String mobile;
	private String emailId;
	private Boolean isActive;

	private List<String> lstPermissionType;
	private List<PermissionType> listPermissionType;
	private String permissionString = "";

	public String editUser() {
		try {

			User user = new User();

			user = userDao.find(getEmailId());
			user.setFirstName(getFirstName());
			user.setLastName(getLastName());
			user.setMobile(getMobile());

			if (getIsActive()) {
				user.setUserStatus(UserStatusType.ACTIVE);
			} else {
				user.setUserStatus(UserStatusType.PENDING);
			}
			editPermission(user);
			userDao.update(user);
			addActionMessage(CrmFieldConstants.USER_DETAILS_UPDATED.getValue());
			return INPUT;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	private void editPermission(User user) {
		Session session = null;
		try {
			session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			session.load(user, user.getEmailId());

			Set<Roles> roles = user.getRoles();
			Iterator<Roles> itr = roles.iterator();
			Roles role = new Roles();
			if (!roles.isEmpty()) {
				role = itr.next();
				Iterator<Permissions> permissionIterator = role
						.getPermissions().iterator();
				while (permissionIterator.hasNext()) {
					// not used but compulsory for iterator working
					@SuppressWarnings("unused")
					Permissions permission = permissionIterator.next();
					permissionIterator.remove();
				}
			}
           if(user.getUserType().equals(UserType.SUBUSER)){
			setListPermissionType(PermissionType.getPermissionType());
			for (String permissionType : lstPermissionType) {
				Permissions permission = new Permissions();
				permission.setPermissionType(PermissionType.getInstanceFromName(permissionType));
				role.addPermission(permission);
			}
    }else if(user.getUserType().equals(UserType.SUBACQUIRER)){
	setListPermissionType(PermissionType.getSubAcquirerPermissionType());
	for (String permissionType : lstPermissionType) {
		Permissions permission = new Permissions();
		permission.setPermissionType(PermissionType.getInstanceFromName(permissionType));
		role.addPermission(permission);
	}
    }	//set permission string
			getPermissions(user);
			tx.commit();			
		} finally {
			HibernateSessionProvider.closeSession(session);
		}
	}

	private void getPermissions(User subUser) {
		Set<Roles> roles = subUser.getRoles();
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

	public void validate() {

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

}
