/*
 * package com.paymentgateway.crm.action;
 * 
 * import java.math.BigDecimal; import java.util.Date;
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
		 * public class ServiceTaxEditAction extends AbstractSecureAction {
		 * 
		 * @Autowired private ServiceTaxDao serviceTaxDao;
		 * 
		 * @Autowired private PendingRequestEmailProcessor pendingRequestEmailProcessor;
		 * 
		 * @Autowired private CrmValidator validator;
		 * 
		 * private static Logger logger =
		 * LoggerFactory.getLogger(ServiceTaxEditAction.class.getName()); private static
		 * final long serialVersionUID = -6517340843571949786L;
		 * 
		 * private Long id; private String businessType; private String status; private
		 * BigDecimal serviceTax; private String response; private String userType;
		 * private String loginEmailId; private Date currentDate = new Date();
		 * 
		 * public String execute() { Session session = null;
		 * 
		 * StringBuilder permissions = new StringBuilder();
		 * permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		 * ServiceTax findPendingRequest = new ServiceTax();
		 * 
		 * if (userType.equals(UserType.ADMIN.toString()) ||
		 * permissions.toString().contains("Create Service Tax")) {
		 * 
		 * // Cancel Pending Request findPendingRequest =
		 * serviceTaxDao.findPendingRequest(businessType); if (findPendingRequest !=
		 * null) { try {
		 * 
		 * session = HibernateSessionProvider.getSession(); Transaction tx =
		 * session.beginTransaction(); id = findPendingRequest.getId();
		 * session.load(findPendingRequest, findPendingRequest.getId()); ServiceTax
		 * serviceTaxDetails = (ServiceTax) session.get(ServiceTax.class, id);
		 * 
		 * serviceTaxDetails.setStatus(TDRStatus.CANCELLED);
		 * serviceTaxDetails.setUpdatedDate(currentDate);
		 * serviceTaxDetails.setProcessedBy(loginEmailId);
		 * session.update(serviceTaxDetails); tx.commit(); session.close(); } catch
		 * (HibernateException e) {
		 * 
		 * logger.error("Service Tax edit failed"); return ERROR; }
		 * 
		 * }
		 * 
		 * ServiceTax activeServiceTax = new ServiceTax(); activeServiceTax =
		 * serviceTaxDao.findServiceTax(businessType);
		 * 
		 * // Deactivate active service tax if (activeServiceTax != null) { try {
		 * 
		 * session = HibernateSessionProvider.getSession(); Transaction txn =
		 * session.beginTransaction(); id = activeServiceTax.getId();
		 * session.load(activeServiceTax, activeServiceTax.getId()); ServiceTax
		 * serviceTaxDetail = (ServiceTax) session.get(ServiceTax.class, id); if
		 * (userType.equals(UserType.ADMIN.toString()) ||
		 * permissions.toString().contains("Create Service Tax")) {
		 * serviceTaxDetail.setStatus(TDRStatus.INACTIVE);
		 * serviceTaxDetail.setUpdatedDate(currentDate);
		 * serviceTaxDetail.setProcessedBy(loginEmailId); }
		 * session.update(serviceTaxDetail); txn.commit(); session.close(); } catch
		 * (HibernateException e) { logger.error("Service Tax Edit Failed " + e); return
		 * ERROR; } }
		 * 
		 * try {
		 * 
		 * createNewServiceTax(businessType, serviceTax, userType); } catch
		 * (HibernateException e) { logger.error("Service Tax Edit Failed " + e); }
		 * 
		 * setResponse(ErrorType.SERVICE_TAX_DETAILS_SAVED.getResponseMessage()); return
		 * SUCCESS; }
		 * 
		 * else { if (findPendingRequest.getId() != null) {
		 * 
		 * setResponse(ErrorType.SERVICE_TAX_REQUEST_ALREADY_PENDING.getResponseMessage(
		 * )); return SUCCESS; }
		 * 
		 * else { createNewServiceTax(businessType, serviceTax, userType);
		 * setResponse(ErrorType.SERVICE_TAX_REQUEST_SAVED_FOR_APPROVAL.
		 * getResponseMessage()); return SUCCESS; } }
		 * 
		 * }
		 * 
		 * public void validate() { if (validator.validateBlankField(getBusinessType()))
		 * { // addFieldError(CrmFieldType.BUSINESS_TYPE.getName(),
		 * validator.getResonseObject().getResponseMessage()); } else if
		 * (!(validator.validateField(CrmFieldType.BUSINESS_NAME, getBusinessType()))) {
		 * addFieldError(CrmFieldType.BUSINESS_NAME.getName(),
		 * validator.getResonseObject().getResponseMessage()); } if
		 * (validator.validateBlankField(getStatus())) { //
		 * addFieldError(CrmFieldType.STATUS.getName(),
		 * validator.getResonseObject().getResponseMessage()); } else if
		 * (!(validator.validateField(CrmFieldType.STATUS, getStatus()))) {
		 * addFieldError(CrmFieldType.STATUS.getName(),
		 * validator.getResonseObject().getResponseMessage()); } if
		 * (validator.validateBlankField(getResponse())) { //
		 * addFieldError(CrmFieldType.RESPONSE.getName(),
		 * validator.getResonseObject().getResponseMessage()); } else if
		 * (!(validator.validateField(CrmFieldType.RESPONSE, getResponse()))) {
		 * addFieldError(CrmFieldType.RESPONSE.getName(),
		 * validator.getResonseObject().getResponseMessage()); } if
		 * (validator.validateBlankField(getUserType())) { //
		 * addFieldError(CrmFieldType.USER_TYPE.getName(),
		 * validator.getResonseObject().getResponseMessage()); } else if
		 * (!(validator.validateField(CrmFieldType.USER_TYPE, getUserType()))) {
		 * addFieldError(CrmFieldType.USER_TYPE.getName(),
		 * validator.getResonseObject().getResponseMessage()); } if
		 * (validator.validateBlankField(getLoginEmailId())) { //
		 * addFieldError(CrmFieldType.EMAILID.getName(),
		 * validator.getResonseObject().getResponseMessage()); } else if
		 * (!(validator.validateField(CrmFieldType.EMAILID, getLoginEmailId()))) {
		 * addFieldError(CrmFieldType.EMAILID.getName(),
		 * validator.getResonseObject().getResponseMessage()); } }
		 * 
		 * private void createNewServiceTax(String businessType, BigDecimal serviceTax,
		 * String userType) {
		 * 
		 * try {
		 * 
		 * StringBuilder permissions = new StringBuilder();
		 * permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		 * ServiceTax newServiceTax = new ServiceTax();
		 * newServiceTax.setCreatedDate(currentDate);
		 * newServiceTax.setBusinessType(businessType); if
		 * (userType.equals(UserType.ADMIN.toString()) ||
		 * permissions.toString().contains("Create Service Tax")) {
		 * newServiceTax.setStatus(TDRStatus.ACTIVE);
		 * newServiceTax.setProcessedBy(loginEmailId);
		 * newServiceTax.setRequestedBy(loginEmailId); } else {
		 * newServiceTax.setStatus(TDRStatus.PENDING);
		 * newServiceTax.setRequestedBy(loginEmailId);
		 * 
		 * } newServiceTax.setUpdatedDate(currentDate);
		 * newServiceTax.setServiceTax(serviceTax);
		 * logger.info("Sending service tax email");
		 * serviceTaxDao.create(newServiceTax); if
		 * (userType.equals(UserType.ADMIN.toString()) ||
		 * permissions.toString().contains("Create Service Tax")) {
		 * logger.info("Sending service tax email from admin");
		 * //pendingRequestEmailProcessor.processServiceTaxEmail("Active", loginEmailId,
		 * userType, businessType);
		 * 
		 * } else { logger.info("Sending service tax email from sub-admin"); //
		 * pendingRequestEmailProcessor.processServiceTaxEmail("Pending", loginEmailId,
		 * userType, businessType); }
		 * 
		 * }
		 * 
		 * catch (Exception e) { logger.error("Service tax edit failed , error = " + e);
		 * }
		 * 
		 * }
		 * 
		 * public BigDecimal getServiceTax() { return serviceTax; }
		 * 
		 * public void setServiceTax(BigDecimal serviceTax) { this.serviceTax =
		 * serviceTax; }
		 * 
		 * public String getStatus() { return status; }
		 * 
		 * public void setStatus(String status) { this.status = status; }
		 * 
		 * public String getBusinessType() { return businessType; }
		 * 
		 * public void setBusinessType(String businessType) { this.businessType =
		 * businessType; }
		 * 
		 * public String getUserType() { return userType; }
		 * 
		 * public void setUserType(String userType) { this.userType = userType; }
		 * 
		 * public String getLoginEmailId() { return loginEmailId; }
		 * 
		 * public void setLoginEmailId(String loginEmailId) { this.loginEmailId =
		 * loginEmailId; }
		 * 
		 * public String getResponse() { return response; }
		 * 
		 * public void setResponse(String response) { this.response = response; } }
		 */