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
import com.paymentgateway.crm.actionBeans.CreateSubAdmin;

/**
 * @author Amitosh Aanand
 *
 */
public class AddSubAdminAction extends AbstractSecureAction {

	@Autowired
	private EmailServiceProvider emailServiceProvider;

	@Autowired
	private CrmValidator validator;

	@Autowired
	private UserDao userDao;

	@Autowired
	private CreateSubAdmin createSubAdmin;

	@Autowired
	private AddSubAdminPermissionAction addSubAdminPermissionAction;

	private static final long serialVersionUID = -3762555013302088094L;

	private static Logger logger = LoggerFactory.getLogger(AddSubAdminAction.class.getName());

	private String firstName;
	private String lastName;
	private String mobile;
	private String emailId;
	private List<String> lstPermissionType;
	private List<PermissionType> listPermissionType;
	private User user = new User();
	private String permissionString = "";
	private boolean disableButtonFlag;
	private ResponseObject responseObject;
	
	private List<PermissionType> analyticsPermission;
	private List<PermissionType> merchantSetupPermission;
	private List<PermissionType> merchantConfiguratiomsPermission;
	private List<PermissionType> resellerPermission;
	private List<PermissionType> viewConfigurationPermission;
	private List<PermissionType> quickSearchPermission;
	private List<PermissionType> reportingPermission;
	private List<PermissionType> quickPayPermission;
	private List<PermissionType> batchOperationsPermission;
	private List<PermissionType> disbursementsPermission;
	private List<PermissionType> fraudPreventionPermission;
	private List<PermissionType> manageUsersPermission;
	private List<PermissionType> manageAcquirersPermission;
	private List<PermissionType> manageIssuersPermission;
	private List<PermissionType> agentAccessPermission;
	private List<PermissionType> chargebackPermission;
	private List<PermissionType> msedclPermission;
	private List<PermissionType> subscriptionPermission;
	private List<PermissionType> accountVerificationPermission;

	public String execute() {

		try {
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.ADMIN)) {
				setSubAdminPermissions();
			} else if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				setListPermissionType(addSubAdminPermissionAction.getSubAdminPermissionType(sessionMap));
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

			if (getFirstName() == null && getLastName() == null && getMobile() == null && getEmailId() == null
					&& lstPermissionType == null) {

			} else {

				if (sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
					logger.info("Create Subadmin");
					responseObject = createSubAdmin.createNewSubAdmin(getUserInstance(), UserType.SUBADMIN,
							sessionUser.getPayId(), permissions, getEmailId());

				}
				if (!ErrorType.SUCCESS.getResponseCode().equals(responseObject.getResponseCode())) {
					addActionMessage(responseObject.getResponseMessage());
					return INPUT;
				}
				// Sending Email for Email Validation
				logger.info("Create Subadmin email sending");
				String fullName = firstName + " " + lastName;
				emailServiceProvider.addUser(responseObject, getFirstName(), fullName);

				addActionMessage(CrmFieldConstants.DETAILS_SUBADMAIN_SUCCESSFULLY.getValue());
				getPermissions();
				disableButtonFlag = true;
			}
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	private void setSubAdminPermissions() {
		setAnalyticsPermission(PermissionType.getSubAdminPermissionTypeByCategory(Integer.valueOf(Constants.SUB_ADMIN_ANALYTICS_PERMISSION.getValue())));
		setMerchantSetupPermission(PermissionType.getSubAdminPermissionTypeByCategory(Integer.valueOf(Constants.SUB_ADMIN_MERCHANT_SETUP_PERMISSION.getValue())));
		setMerchantConfiguratiomsPermission(PermissionType.getSubAdminPermissionTypeByCategory(Integer.valueOf(Constants.SUB_ADMIN_MERCHANT_CONFIGURATIOMS_PERMISSION.getValue())));
		setResellerPermission(PermissionType.getSubAdminPermissionTypeByCategory(Integer.valueOf(Constants.SUB_ADMIN_RESELLER_PERMISSION.getValue())));
		setViewConfigurationPermission(PermissionType.getSubAdminPermissionTypeByCategory(Integer.valueOf(Constants.SUB_ADMIN_VIEW_CONFIGURATION_PERMISSION.getValue())));
		setQuickSearchPermission(PermissionType.getSubAdminPermissionTypeByCategory(Integer.valueOf(Constants.SUB_ADMIN_QUICK_SEARCH_PERMISSION.getValue())));
		setReportingPermission(PermissionType.getSubAdminPermissionTypeByCategory(Integer.valueOf(Constants.SUB_ADMIN_REPORTING_PERMISSION.getValue())));
		setQuickPayPermission(PermissionType.getSubAdminPermissionTypeByCategory(Integer.valueOf(Constants.SUB_ADMIN_QUICK_PAY_PERMISSION.getValue())));
		setBatchOperationsPermission(PermissionType.getSubAdminPermissionTypeByCategory(Integer.valueOf(Constants.SUB_ADMIN_BATCH_OPERATIONS_PERMISSION.getValue())));
		setDisbursementsPermission(PermissionType.getSubAdminPermissionTypeByCategory(Integer.valueOf(Constants.SUB_ADMIN_DISBURSEMENTS_PERMISSION.getValue())));
		setFraudPreventionPermission(PermissionType.getSubAdminPermissionTypeByCategory(Integer.valueOf(Constants.SUB_ADMIN_FRAUD_PREVENTION_PERMISSION.getValue())));
		setManageUsersPermission(PermissionType.getSubAdminPermissionTypeByCategory(Integer.valueOf(Constants.SUB_ADMIN_MANAGE_USERS_PERMISSION.getValue())));
		setManageAcquirersPermission(PermissionType.getSubAdminPermissionTypeByCategory(Integer.valueOf(Constants.SUB_ADMIN_MANAGE_ACQUIRERS_PERMISSION.getValue())));
		setManageIssuersPermission(PermissionType.getSubAdminPermissionTypeByCategory(Integer.valueOf(Constants.SUB_ADMIN_MANAGE_ISSUERS_PERMISSION.getValue())));
		setAgentAccessPermission(PermissionType.getSubAdminPermissionTypeByCategory(Integer.valueOf(Constants.SUB_ADMIN_AGENT_ACCESS_PERMISSION.getValue())));
		setChargebackPermission(PermissionType.getSubAdminPermissionTypeByCategory(Integer.valueOf(Constants.SUB_ADMIN_CHARGEBACK_PERMISSION.getValue())));
		setMsedclPermission(PermissionType.getSubAdminPermissionTypeByCategory(Integer.valueOf(Constants.SUB_ADMIN_MSEDCL_PERMISSION.getValue())));
		setSubscriptionPermission(PermissionType.getSubAdminPermissionTypeByCategory(Integer.valueOf(Constants.SUB_ADMIN_SUBSCRIPTION_PERMISSION.getValue())));
		setAccountVerificationPermission(PermissionType.getSubAdminPermissionTypeByCategory(Integer.valueOf(Constants.SUB_ADMIN_ACCOUNT_VERIFICATION_PERMISSION.getValue())));
	}

	private User getUserInstance() {
		user.setFirstName(getFirstName());
		user.setLastName(getLastName());
		user.setMobile(getMobile());
		user.setEmailId(getEmailId());

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
		if (sessionUser.getUserType().equals(UserType.ADMIN)) {
			setListPermissionType(PermissionType.getPermissionType());
		} else if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			setListPermissionType(addSubAdminPermissionAction.getSubAdminPermissionType(sessionMap));
		}

		if (getFirstName() == null && getLastName() == null && getMobile() == null && getEmailId() == null
				&& lstPermissionType == null) {

		}

		else {
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

	public EmailServiceProvider getEmailServiceProvider() {
		return emailServiceProvider;
	}

	public void setEmailControllerServiceProvider(EmailServiceProvider emailServiceProvider) {
		this.emailServiceProvider = emailServiceProvider;
	}

	public CreateSubAdmin getCreateSubAdmin() {
		return createSubAdmin;
	}

	public void setCreateSubAdmin(CreateSubAdmin createSubAdmin) {
		this.createSubAdmin = createSubAdmin;
	}

	public AddSubAdminPermissionAction getAddSubAdminPermissionAction() {
		return addSubAdminPermissionAction;
	}

	public void setAddSubAdminPermissionAction(AddSubAdminPermissionAction addSubAdminPermissionAction) {
		this.addSubAdminPermissionAction = addSubAdminPermissionAction;
	}

	public List<PermissionType> getAnalyticsPermission() {
		return analyticsPermission;
	}

	public void setAnalyticsPermission(List<PermissionType> analyticsPermission) {
		this.analyticsPermission = analyticsPermission;
	}

	public List<PermissionType> getMerchantSetupPermission() {
		return merchantSetupPermission;
	}

	public void setMerchantSetupPermission(List<PermissionType> merchantSetupPermission) {
		this.merchantSetupPermission = merchantSetupPermission;
	}

	public List<PermissionType> getMerchantConfiguratiomsPermission() {
		return merchantConfiguratiomsPermission;
	}

	public void setMerchantConfiguratiomsPermission(List<PermissionType> merchantConfiguratiomsPermission) {
		this.merchantConfiguratiomsPermission = merchantConfiguratiomsPermission;
	}

	public List<PermissionType> getResellerPermission() {
		return resellerPermission;
	}

	public void setResellerPermission(List<PermissionType> resellerPermission) {
		this.resellerPermission = resellerPermission;
	}

	public List<PermissionType> getViewConfigurationPermission() {
		return viewConfigurationPermission;
	}

	public void setViewConfigurationPermission(List<PermissionType> viewConfigurationPermission) {
		this.viewConfigurationPermission = viewConfigurationPermission;
	}

	public List<PermissionType> getQuickSearchPermission() {
		return quickSearchPermission;
	}

	public void setQuickSearchPermission(List<PermissionType> quickSearchPermission) {
		this.quickSearchPermission = quickSearchPermission;
	}

	public List<PermissionType> getReportingPermission() {
		return reportingPermission;
	}

	public void setReportingPermission(List<PermissionType> reportingPermission) {
		this.reportingPermission = reportingPermission;
	}

	
	public List<PermissionType> getQuickPayPermission() {
		return quickPayPermission;
	}

	public void setQuickPayPermission(List<PermissionType> quickPayPermission) {
		this.quickPayPermission = quickPayPermission;
	}



	public List<PermissionType> getBatchOperationsPermission() {
		return batchOperationsPermission;
	}

	public void setBatchOperationsPermission(List<PermissionType> batchOperationsPermission) {
		this.batchOperationsPermission = batchOperationsPermission;
	}

	public List<PermissionType> getDisbursementsPermission() {
		return disbursementsPermission;
	}

	public void setDisbursementsPermission(List<PermissionType> disbursementsPermission) {
		this.disbursementsPermission = disbursementsPermission;
	}

	public List<PermissionType> getFraudPreventionPermission() {
		return fraudPreventionPermission;
	}

	public void setFraudPreventionPermission(List<PermissionType> fraudPreventionPermission) {
		this.fraudPreventionPermission = fraudPreventionPermission;
	}

	public List<PermissionType> getManageUsersPermission() {
		return manageUsersPermission;
	}

	public void setManageUsersPermission(List<PermissionType> manageUsersPermission) {
		this.manageUsersPermission = manageUsersPermission;
	}

	public List<PermissionType> getManageAcquirersPermission() {
		return manageAcquirersPermission;
	}

	public void setManageAcquirersPermission(List<PermissionType> manageAcquirersPermission) {
		this.manageAcquirersPermission = manageAcquirersPermission;
	}

	public List<PermissionType> getManageIssuersPermission() {
		return manageIssuersPermission;
	}

	public void setManageIssuersPermission(List<PermissionType> manageIssuersPermission) {
		this.manageIssuersPermission = manageIssuersPermission;
	}

	public List<PermissionType> getAgentAccessPermission() {
		return agentAccessPermission;
	}

	public void setAgentAccessPermission(List<PermissionType> agentAccessPermission) {
		this.agentAccessPermission = agentAccessPermission;
	}

	public List<PermissionType> getChargebackPermission() {
		return chargebackPermission;
	}

	public void setChargebackPermission(List<PermissionType> chargebackPermission) {
		this.chargebackPermission = chargebackPermission;
	}

	public List<PermissionType> getMsedclPermission() {
		return msedclPermission;
	}

	public void setMsedclPermission(List<PermissionType> msedclPermission) {
		this.msedclPermission = msedclPermission;
	}
	public List<PermissionType> getSubscriptionPermission() {
		return subscriptionPermission;
	}
	
	public void setSubscriptionPermission(List<PermissionType> subscriptionPermission) {
		this.subscriptionPermission = subscriptionPermission;
	}

	public List<PermissionType> getAccountVerificationPermission() {
		return accountVerificationPermission;
	}

	public void setAccountVerificationPermission(List<PermissionType> accountVerificationPermission) {
		this.accountVerificationPermission = accountVerificationPermission;
	}
	
}
