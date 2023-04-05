package com.paymentgateway.crm.action;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.email.EmailServiceProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.Permissions;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.Roles;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.actionBeans.CreateSubUser;

/**
 * @author Rahul
 *
 */

public class AddSubUserAction extends AbstractSecureAction {

	@Autowired
	private EmailServiceProvider emailServiceProvider;

	@Autowired
	private UserDao userDao;

	@Autowired
	private CrmValidator validator;

	@Autowired
	private CreateSubUser createSubUser;

	private static final long serialVersionUID = 5867519202316872074L;

	private static Logger logger = LoggerFactory.getLogger(AddSubUserAction.class.getName());

	private String firstName;
	private String lastName;
	private String businessName;
	private String mobile;
	private String emailId;
	private String allowEpos;
	private List<String> lstPermissionType;
	private List<PermissionType> listPermissionType;
	private User user = new User();
	private String permissionString = "";
	private boolean disableButtonFlag;
	private ResponseObject responseObject;
	private String subUserType;

	public String execute() {
		try {
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				if (sessionUser.isSuperMerchant()) {
					setListPermissionType(PermissionType.getSubUserPermissionTypeForSuperMerchant());
				} else {
					setListPermissionType(PermissionType.getSubUserPermissionType());
				}
			} else if (sessionUser.getUserType().equals(UserType.ACQUIRER)) {
				setListPermissionType(PermissionType.getSubAcquirerPermissionType());
			} 
			Set<Permissions> permissions = new HashSet<Permissions>();

			if (lstPermissionType == null) {

			} else {
				for (String permissionType : lstPermissionType) {
					Permissions permission = new Permissions();
					permission.setPermissionType(PermissionType.getInstanceFromName(permissionType));
					permissions.add(permission);
				}
			}
			if(lstPermissionType != null && lstPermissionType.contains("Allow ePOS")) {
				setAllowEpos("true");
			}else {
				setAllowEpos("false");
			}
			if (sessionUser.getUserType().equals(UserType.ACQUIRER)) {
				responseObject = createSubUser.createUser(getUserInstance(), UserType.SUBACQUIRER,
						sessionUser.getPayId(), permissions, null);
			} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				responseObject = createSubUser.createUser(getUserInstance(), UserType.SUBUSER, sessionUser.getPayId(),
						permissions, subUserType);
			} 
			if (!ErrorType.SUCCESS.getResponseCode().equals(responseObject.getResponseCode())) {
				addActionMessage(responseObject.getResponseMessage());
				return INPUT;
			}
			// Sending Email for Email Validation
			String fullName = firstName + " " + lastName;
			
				emailServiceProvider.addUser(responseObject, getFirstName(), fullName);
				addActionMessage(CrmFieldConstants.DETAILS_SUBUSER_SUCCESSFULLY.getValue());
			
			
			if(lstPermissionType.contains("eNACH Report")) {
				listPermissionType.add(PermissionType.getInstanceFromName("eNACH Report"));
			}
			
			getPermissions();
			disableButtonFlag = true;
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	private User getUserInstance() {
		user.setFirstName(getFirstName());
		user.setLastName(getLastName());
		user.setMobile(getMobile());
		user.setEmailId(getEmailId());
		user.setBusinessName(getBusinessName());
		
		return user;
	}

	

	private void getPermissions() {
		Session session = null;
		try {

			User subUser = userDao.find(getEmailId());
			session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			session.load(subUser, subUser.getEmailId());

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
			tx.commit();
		} finally {
			HibernateSessionProvider.closeSession(session);
		}
	}

	public void validate() {

		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
			if (sessionUser.isSuperMerchant()) {
				setListPermissionType(PermissionType.getSubUserPermissionTypeForSuperMerchant());
			} else {
				setListPermissionType(PermissionType.getSubUserPermissionType());
			}
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

	public List<String> getLstPermissionType() {
		return lstPermissionType;
	}

	public void setLstPermissionType(List<String> lstPermissionType) {
		this.lstPermissionType = lstPermissionType;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getPermissionString() {
		return permissionString;
	}

	public void setPermissionString(String permissionString) {
		this.permissionString = permissionString;
	}

	public List<PermissionType> getListPermissionType() {
		return listPermissionType;
	}

	public void setListPermissionType(List<PermissionType> listPermissionType) {
		this.listPermissionType = listPermissionType;
	}

	public boolean isDisableButtonFlag() {
		return disableButtonFlag;
	}

	public void setDisableButtonFlag(boolean disableButtonFlag) {
		this.disableButtonFlag = disableButtonFlag;
	}

	public ResponseObject getResponseObject() {
		return responseObject;
	}

	public void setResponseObject(ResponseObject responseObject) {
		this.responseObject = responseObject;
	}

	public String getAllowEpos() {
		return allowEpos;
	}

	public void setAllowEpos(String allowEpos) {
		this.allowEpos = allowEpos;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getSubUserType() {
		return subUserType;
	}

	public void setSubUserType(String subUserType) {
		this.subUserType = subUserType;
	}

}
