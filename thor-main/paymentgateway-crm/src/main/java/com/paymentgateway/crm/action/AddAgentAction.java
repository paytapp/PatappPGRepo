package com.paymentgateway.crm.action;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import com.paymentgateway.crm.actionBeans.AddSubAdminPermissionAction;
import com.paymentgateway.crm.actionBeans.CreateAgent;

public class AddAgentAction extends AbstractSecureAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6812539598761798561L;

	@Autowired
	private CrmValidator validator;
	
	@Autowired
	private CreateAgent createAgent;

	@Autowired
	private AddSubAdminPermissionAction addSubAdminPermissionAction;

	private static Logger logger = LoggerFactory.getLogger(AddAgentAction.class.getName());

	private String firstName;
	private String lastName;
	private String mobileNumber;
	private String emailId;
	private String businessName;
	private List<String> lstPermissionType;
	private List<PermissionType> listPermissionType;
	private User user = new User();
	private String permissionString = "";
	private boolean disableButtonFlag;
	private ResponseObject responseObject;

	public String execute() {
		try {
			logger.info("Inside execute(), Add agent ");
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (getFirstName() == null && getLastName() == null && getBusinessName() == null && getEmailId() == null) {
			} else {
				if (sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {

					Set<Permissions> permissions = new HashSet<Permissions>();

					if (lstPermissionType == null) {

					} else {
						for (String permissionType : lstPermissionType) {
							Permissions permission = new Permissions();
							permission.setPermissionType(PermissionType.getInstanceFromName(permissionType));
							permissions.add(permission);
						}
					}

					logger.info("Create Agent");
					responseObject = createAgent.createNewAgent(getUserInstance(), UserType.AGENT, permissions);
				}
				if (!ErrorType.SUCCESS.getResponseCode().equals(responseObject.getResponseCode())) {
					addActionMessage(responseObject.getResponseMessage());
					return INPUT;
				}
				addActionMessage(CrmFieldConstants.DETAILS_ACQUIRER_SUCCESSFULLY.getValue());
				disableButtonFlag = true;
			}
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	private User getUserInstance() {
		user.setFirstName(getFirstName());
		user.setLastName(getLastName());
		user.setMobile(getMobileNumber());
		user.setEmailId(getEmailId());
		user.setBusinessName(getBusinessName());
		return user;
	}

	public void validate() {

		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		if (sessionUser.getUserType().equals(UserType.ADMIN)) {
			setListPermissionType(PermissionType.getPermissionType());
		} else if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			setListPermissionType(addSubAdminPermissionAction.getSubAdminPermissionType(sessionMap));
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

		if (validator.validateBlankField(getEmailId())) {
			addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.isValidEmailId(getEmailId()))) {
			addFieldError(CrmFieldType.EMAILID.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}

		if (validator.validateBlankField(getMobileNumber())) {
			addFieldError(CrmFieldType.MOBILE.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.MOBILE,getMobileNumber()))) {
			addFieldError(CrmFieldType.MOBILE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
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

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

}
