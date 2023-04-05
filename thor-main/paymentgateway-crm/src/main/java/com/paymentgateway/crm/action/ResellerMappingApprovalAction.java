package com.paymentgateway.crm.action;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.PendingResellerMappingApproval;
import com.paymentgateway.commons.user.PendingResellerMappingDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.TDRStatus;

/**
 * @author Rahul
 *
 */
public class ResellerMappingApprovalAction extends AbstractSecureAction {

	@Autowired
	private PendingResellerMappingDao pendingResellerMappingDao;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private CrmValidator validator;
	
	private static final long serialVersionUID = 6467520675757548735L;

	private String merchantEmailId;
	private String resellerId;
	private String loginemailId;
	private String operation;
	private UserType userType;
	private String requestedBy;

	public String execute() {

		Date date = new Date();
		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		
		try{
			
			if (permissions.toString().contains("Edit Merchant Details") || userType.equals(UserType.ADMIN)) {
				PendingResellerMappingApproval pendingMapping = pendingResellerMappingDao.find(merchantEmailId);
				if (operation.equals("accept")) {
					pendingMapping.setRequestStatus(TDRStatus.ACCEPTED.toString());
					pendingMapping.setUpdateDate(date);
					pendingMapping.setProcessedBy(loginemailId);
					pendingResellerMappingDao.update(pendingMapping);
					
					User dbUser = userDao.findPayIdByEmail(merchantEmailId);
					User userReseller = userDao.find(resellerId); 
					String resellerPayId = userReseller.getPayId(); 
					dbUser.setResellerId(resellerPayId);
					dbUser.setUpdateDate(date);
					dbUser.setUpdatedBy(loginemailId);
					userDao.update(dbUser);

				} else if (operation.equals("reject")) {
					pendingMapping.setRequestStatus(TDRStatus.REJECTED.toString());
					pendingMapping.setUpdateDate(date);
					pendingMapping.setProcessedBy(loginemailId);
					pendingResellerMappingDao.update(pendingMapping);

				}

			}
			return SUCCESS;
		}
		catch(Exception exception){
			return ERROR;
		}

		

	}
	public void validate() {
	if ((validator.validateBlankField(getMerchantEmailId()))) {
		/*addFieldError(CrmFieldType.MERCHANT_EMAIL_ID.getName(), validator
				.getResonseObject().getResponseMessage());*/
	} else if (!(validator.validateField(CrmFieldType.MERCHANT_EMAIL_ID,
			getMerchantEmailId()))) {
		addFieldError(CrmFieldType.MERCHANT_EMAIL_ID.getName(), validator
				.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getResellerId()))) {
	/*	addFieldError(CrmFieldType.RESELLER_ID.getName(), validator
				.getResonseObject().getResponseMessage());*/
	} else if (!(validator.validateField(CrmFieldType.RESELLER_ID,
			getResellerId()))) {
		addFieldError(CrmFieldType.RESELLER_ID.getName(), validator
				.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getLoginemailId()))) {
	/*	addFieldError(CrmFieldType.EMAILID.getName(), validator
				.getResonseObject().getResponseMessage());*/
	} else if (!(validator.validateField(CrmFieldType.EMAILID,
			getLoginemailId()))) {
		addFieldError(CrmFieldType.EMAILID.getName(), validator
				.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getOperation()))) {
	/*	addFieldError(CrmFieldType.OPERATION.getName(), validator
				.getResonseObject().getResponseMessage());*/
	} else if (!(validator.validateField(CrmFieldType.OPERATION,
			getOperation()))) {
		addFieldError(CrmFieldType.OPERATION.getName(), validator
				.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getRequestedBy()))) {
/*		addFieldError(CrmFieldType.EMAILID.getName(), validator
				.getResonseObject().getResponseMessage());*/
	} else if (!(validator.validateField(CrmFieldType.EMAILID,
			getRequestedBy()))) {
		addFieldError(CrmFieldType.EMAILID.getName(), validator
				.getResonseObject().getResponseMessage());
	}
	
	
	}

	public String getMerchantEmailId() {
		return merchantEmailId;
	}

	public void setMerchantEmailId(String merchantEmailId) {
		this.merchantEmailId = merchantEmailId;
	}

	public String getResellerId() {
		return resellerId;
	}

	public void setResellerId(String resellerId) {
		this.resellerId = resellerId;
	}

	public String getLoginemailId() {
		return loginemailId;
	}

	public void setLoginemailId(String loginemailId) {
		this.loginemailId = loginemailId;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	public String getRequestedBy() {
		return requestedBy;
	}

	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}

}
