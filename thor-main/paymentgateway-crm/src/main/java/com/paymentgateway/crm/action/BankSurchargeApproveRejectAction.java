package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.Surcharge;
import com.paymentgateway.commons.user.SurchargeDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.TDRStatus;

/**
 * @author Shaiwal
 *
 */
public class BankSurchargeApproveRejectAction extends AbstractSecureAction {
	
	@Autowired
	private SurchargeDao surchargeDao;
	@Autowired
	private CrmValidator validator;

	private static Logger logger = LoggerFactory.getLogger(BankSurchargeApproveRejectAction.class.getName());
	private static final long serialVersionUID = -6517340843571949786L;

	private String paymentType;
	private String payId;
	private String mopType;
	private String acquirer;
	private String emailId;
	private String userType;
	private String operation;
	private String response;
	public Date currentDate = new Date();

	public String execute() {

		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));

		if (permissions.toString().contains(CrmFieldConstants.CREATE_SURCHARGE.getValue()) || userType.equals(UserType.ADMIN.toString())) {

			List<Surcharge> surchargeToUpdate = new ArrayList <Surcharge>();
			
			surchargeToUpdate = surchargeDao.findPendingSurchargeListByPayIdAcquirerName(payId,paymentType,acquirer,mopType);

			if (surchargeToUpdate != null) {

				if (operation.equals("accept")) {
					List<Surcharge> activeSurchargeList = new ArrayList <Surcharge>();
					activeSurchargeList = surchargeDao.findSurchargeListByPayIdAcquirerName(payId,paymentType,acquirer,mopType);

					if (activeSurchargeList != null) {

						for (Surcharge surcharge : activeSurchargeList){
							updateSurcharge(surcharge, TDRStatus.INACTIVE);
						}
					}
					
					for (Surcharge surcharge:surchargeToUpdate){
						updateSurcharge(surcharge, TDRStatus.ACTIVE);
					}
					setResponse(ErrorType.BANK_SURCHARGE_UPDATED.getResponseMessage());
					return SUCCESS;
					
				} else {
					for (Surcharge surcharge:surchargeToUpdate){
						updateSurcharge(surcharge, TDRStatus.REJECTED);
					}
					setResponse(ErrorType.BANK_SURCHARGE_REJECTED.getResponseMessage());
					return SUCCESS;
				}

			}

		}

		return SUCCESS;
	}

	public void updateSurcharge(Surcharge surcharge, TDRStatus status) {

		try {

			Session session = null;
			session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			Long id = surcharge.getId();
			session.load(surcharge, surcharge.getId());
			Surcharge sch = (Surcharge) session.get(Surcharge.class, id);
			sch.setStatus(status);
			sch.setUpdatedDate(currentDate);
			sch.setProcessedBy(emailId);
			session.update(sch);
			tx.commit();
			session.close();

		} catch (HibernateException e) {
			logger.error("Exception: " , e);
		} finally {

		}

	}
	public void validate(){
		
		if ((validator.validateBlankField(getPaymentType()))) {
			addFieldError(CrmFieldType.PAYMENT_TYPE.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.PAYMENT_TYPE, getPaymentType()))) {
			addFieldError(CrmFieldType.PAYMENT_TYPE.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(getPayId()))) {
		//	addFieldError(CrmFieldType.PAY_ID.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.PAY_ID, getPayId()))) {
			addFieldError(CrmFieldType.PAY_ID.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(getMopType()))) {
		//	addFieldError(CrmFieldType.MOP_TYPE.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.MOP_TYPE, getMopType()))) {
			addFieldError(CrmFieldType.MOP_TYPE.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(getAcquirer()))) {
		//	addFieldError(CrmFieldType.ACQUIRER.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.ACQUIRER, getAcquirer()))) {
			addFieldError(CrmFieldType.ACQUIRER.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(getEmailId()))) {
		//	addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.EMAILID, getEmailId()))) {
			addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(getUserType()))) {
		//	addFieldError(CrmFieldType.USER_TYPE.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.USER_TYPE, getUserType()))) {
			addFieldError(CrmFieldType.USER_TYPE.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(getOperation()))) {
		//	addFieldError(CrmFieldType.OPERATION.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.OPERATION, getOperation()))) {
			addFieldError(CrmFieldType.OPERATION.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(getResponse()))) {
		//	addFieldError(CrmFieldType.RESPONSE.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.RESPONSE, getResponse()))) {
			addFieldError(CrmFieldType.RESPONSE.getName(), validator.getResonseObject().getResponseMessage());
		}
		
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getMopType() {
		return mopType;
	}

	public void setMopType(String mopType) {
		this.mopType = mopType;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

}
