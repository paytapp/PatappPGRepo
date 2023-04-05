package com.paymentgateway.crm.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.PendingBulkUserDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.PendingBulkUserRequest;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.PendingRequestEmailProcessor;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.crm.actionBeans.CreateNewUser;

public class BulkUserAcceptRejectAction extends AbstractSecureAction {

	private static final long serialVersionUID = 6608355391025512472L;


	@Autowired
	private CrmValidator validator;
	
	@Autowired
	private CreateNewUser createUser;
	
	@Autowired
	private PendingBulkUserDao pendingBulkUserDao;
	
	@Autowired
	private PendingRequestEmailProcessor pendingRequestEmailProcessor;
		
	
	private static Logger logger = LoggerFactory.getLogger(BulkUserAcceptRejectAction.class.getName());
	
	private String requestedBy;
	private String operation;
	private String emailId;
	private String businessName;
	private String industryCategory;
	private String industrySubCategory;
	private String mobileNumber;
	private String password;
	private String pin;
	private String response;
	
	public String execute() {
		
		try {
			StringBuilder permissions = new StringBuilder();
			permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
			User user = (User) sessionMap.get(Constants.USER.getValue());

			if (permissions.toString().contains(PermissionType.CREATE_BULK_USER.getPermission()) || user.getUserType().equals(UserType.ADMIN)) {

				PendingBulkUserRequest pendingBulkUser = pendingBulkUserDao.find(mobileNumber);

				if (operation.equalsIgnoreCase("Reject")) {

					pendingBulkUserDao.updatePendingBulkUser(pendingBulkUser, TDRStatus.REJECTED, pendingBulkUser.getRequestedBy(),
							user.getEmailId());
					
					pendingRequestEmailProcessor.processBulkUserApproveRejectEmail(TDRStatus.REJECTED.getName(), user.getEmailId(), user.getUserType().toString(),
							PermissionType.CREATE_BULK_USER.getPermission());
				
					setResponse(ErrorType.MERCHANT_REQUEST_REJECT.getResponseMessage());
					return SUCCESS;

				} else if (operation.equalsIgnoreCase("Accept")) {

					setPassword("PaymentGateway@".concat(getMobileNumber()));
					setPin(Constants.DEFAULT_PIN.getValue());
					createUser.createUser(getUser(), UserType.MERCHANT, "", user,"");
					pendingBulkUserDao.updatePendingBulkUser(pendingBulkUser, TDRStatus.ACCEPTED, pendingBulkUser.getRequestedBy(),
							user.getEmailId());
					
					pendingRequestEmailProcessor.processBulkUserApproveRejectEmail(TDRStatus.ACTIVE.getName(), user.getEmailId(), user.getUserType().toString(),
							PermissionType.CREATE_BULK_USER.getPermission());
					
					setResponse(ErrorType.MERCHANT_REQUEST_ACCEPT.getResponseMessage());
				}

			}
		} catch (Exception ex) {
			logger.info("caught exception in BulkUserAcceptRejectAction while accept or reject the pending request");
		}
	return SUCCESS;
	}
	
	
	private User getUser() {
		
		User user = new User();
		user.setEmailId(getEmailId());
		user.setMobile(getMobileNumber());
		user.setBusinessName(getBusinessName());
		user.setPassword(getPassword());
		user.setIndustryCategory(industryCategory);
		user.setIndustrySubCategory(industrySubCategory);
		user.setPin(getPin());
		
		return user;
	}
	
	public void validate() {
		
	}
	
	public String getOperation() {
		return operation;
	}


	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getIndustryCategory() {
		return industryCategory;
	}

	public void setIndustryCategory(String industryCategory) {
		this.industryCategory = industryCategory;
	}

	public String getIndustrySubCategory() {
		return industrySubCategory;
	}

	public void setIndustrySubCategory(String industrySubCategory) {
		this.industrySubCategory = industrySubCategory;
	}
	
	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}
	
	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}
	
	public String getRequestedBy() {
		return requestedBy;
	}

	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}

}
