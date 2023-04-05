/*
 * package com.paymentgateway.crm.action;
 * 
 * import java.util.Date;
 * 
 * import org.slf4j.Logger; import org.slf4j.LoggerFactory; import
 * org.hibernate.HibernateException; import org.hibernate.Session; import
 * org.hibernate.Transaction; import
 * org.springframework.beans.factory.annotation.Autowired;
 * 
 * import com.paymentgateway.commons.dao.HibernateSessionProvider; import
 * com.paymentgateway.commons.dao.ServiceTaxDao; import
 * com.paymentgateway.commons.exception.ErrorType; import
 * com.paymentgateway.commons.user.ServiceTax; import
 * com.paymentgateway.commons.user.User; import
 * com.paymentgateway.commons.user.UserType; import
 * com.paymentgateway.commons.util.Constants; import
 * com.paymentgateway.commons.util.CrmFieldType; import
 * com.paymentgateway.commons.util.CrmValidator; import
 * com.paymentgateway.commons.util.PendingRequestEmailProcessor; import
 * com.paymentgateway.commons.util.TDRStatus;
 * 
 *//**
	 * @author Shaiwal
	 *
	 *//*
		 * public class ServiceTaxApproveRejectAction extends AbstractSecureAction {
		 * 
		 * @Autowired private ServiceTaxDao serviceTaxDao;
		 * 
		 * @Autowired private PendingRequestEmailProcessor pendingRequestEmailProcessor;
		 * 
		 * @Autowired private CrmValidator validator;
		 * 
		 * private static Logger logger =
		 * LoggerFactory.getLogger(ServiceTaxApproveRejectAction.class.getName());
		 * private static final long serialVersionUID = -6517340843571949786L;
		 * 
		 * private String businessType; private String emailId; private String userType;
		 * private String operation; private Date currentDate = new Date();
		 * 
		 * public String execute() {
		 * 
		 * StringBuilder permissions = new StringBuilder();
		 * permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		 * 
		 * try { if (permissions.toString().contains("Create Service Tax") ||
		 * userType.equals(UserType.ADMIN.toString())) {
		 * 
		 * ServiceTax serviceTaxToUpdate = new ServiceTax(); serviceTaxToUpdate =
		 * serviceTaxDao.findPendingRequest(businessType); User user = (User)
		 * sessionMap.get(Constants.USER.getValue());
		 * 
		 * if (serviceTaxToUpdate != null) {
		 * 
		 * if (operation.equals("accept")) {
		 * 
		 * logger.info("Service tax accepted"); ServiceTax activeServiceTax = new
		 * ServiceTax(); activeServiceTax = serviceTaxDao.findServiceTax(businessType);
		 * 
		 * if (activeServiceTax != null) {
		 * 
		 * updateServiceTax(activeServiceTax, TDRStatus.INACTIVE); }
		 * updateServiceTax(serviceTaxToUpdate, TDRStatus.ACTIVE);
		 * logger.info("Service tax accept updated , business Type = " + businessType);
		 * 
		 * //pendingRequestEmailProcessor.processServiceTaxApproveRejectEmail(
		 * "Approved", emailId, userType, // businessType,
		 * serviceTaxToUpdate.getRequestedBy());
		 * 
		 * } else { updateServiceTax(serviceTaxToUpdate, TDRStatus.REJECTED); //
		 * pendingRequestEmailProcessor.processServiceTaxApproveRejectEmail("Rejected",
		 * emailId, userType, // businessType, serviceTaxToUpdate.getRequestedBy()); }
		 * 
		 * }
		 * 
		 * }
		 * 
		 * else { addActionMessage("Denied operation , contact Admin"); } } catch
		 * (Exception e) { logger.error("Service tax operaion failed"); return ERROR; }
		 * 
		 * return SUCCESS; }
		 * 
		 * public void validate() { if
		 * ((validator.validateBlankField(getBusinessType()))) { //
		 * addFieldError(CrmFieldType.BUSINESS_TYPE.getName(),
		 * validator.getResonseObject().getResponseMessage()); } else if
		 * (!(validator.validateField(CrmFieldType.BUSINESS_NAME, getBusinessType()))) {
		 * addFieldError(CrmFieldType.BUSINESS_NAME.getName(),
		 * validator.getResonseObject().getResponseMessage()); } if
		 * (validator.validateBlankField(getEmailId())) { //
		 * addFieldError(CrmFieldType.EMAILID.getName(),
		 * validator.getResonseObject().getResponseMessage()); } else if
		 * (!(validator.isValidEmailId(getEmailId()))) {
		 * addFieldError(CrmFieldType.EMAILID.getName(),
		 * ErrorType.INVALID_FIELD_VALUE.getResponseMessage()); } if
		 * ((validator.validateBlankField(getUserType()))) { //
		 * addFieldError(CrmFieldType.USER_TYPE.getName(),
		 * validator.getResonseObject().getResponseMessage()); } else if
		 * (!(validator.validateField(CrmFieldType.USER_TYPE, getUserType()))) {
		 * addFieldError(CrmFieldType.USER_TYPE.getName(),
		 * validator.getResonseObject().getResponseMessage()); } if
		 * ((validator.validateBlankField(getOperation()))) { //
		 * addFieldError(CrmFieldType.OPERATION.getName(),
		 * validator.getResonseObject().getResponseMessage()); } else if
		 * (!(validator.validateField(CrmFieldType.OPERATION, getOperation()))) {
		 * addFieldError(CrmFieldType.OPERATION.getName(),
		 * validator.getResonseObject().getResponseMessage()); } }
		 * 
		 * public void updateServiceTax(ServiceTax serviceTax, TDRStatus status) {
		 * 
		 * try {
		 * 
		 * Session session = null; session = HibernateSessionProvider.getSession();
		 * Transaction tx = session.beginTransaction(); Long id = serviceTax.getId();
		 * session.load(serviceTax, serviceTax.getId()); ServiceTax sTax = (ServiceTax)
		 * session.get(ServiceTax.class, id); sTax.setStatus(status);
		 * sTax.setUpdatedDate(currentDate); sTax.setProcessedBy(emailId);
		 * session.update(sTax); tx.commit(); session.close();
		 * 
		 * } catch (HibernateException e) { e.printStackTrace(); } finally {
		 * 
		 * }
		 * 
		 * }
		 * 
		 * public String getUserType() { return userType; }
		 * 
		 * public void setUserType(String userType) { this.userType = userType; }
		 * 
		 * public String getEmailId() { return emailId; }
		 * 
		 * public void setEmailId(String emailId) { this.emailId = emailId; }
		 * 
		 * public String getOperation() { return operation; }
		 * 
		 * public void setOperation(String operation) { this.operation = operation; }
		 * 
		 * public String getBusinessType() { return businessType; }
		 * 
		 * public void setBusinessType(String businessType) { this.businessType =
		 * businessType; }
		 * 
		 * }
		 */