package com.paymentgateway.crm.action;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.Surcharge;
import com.paymentgateway.commons.user.SurchargeDao;
import com.paymentgateway.commons.user.SurchargeDetails;
import com.paymentgateway.commons.user.SurchargeDetailsDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PendingRequestEmailProcessor;
import com.paymentgateway.commons.util.TDRStatus;

public class SurchargeDetailEditAction extends AbstractSecureAction {
	
	@Autowired
	private SurchargeDao surchargeDao;
	
	@Autowired
	private SurchargeDetailsDao surchargeDetailsDao;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private PendingRequestEmailProcessor pendingRequestEmailProcessor;
	
	@Autowired
	private CrmValidator validator;
	
	private static Logger logger = LoggerFactory.getLogger(SurchargeDetailEditAction.class.getName());
	private static final long serialVersionUID = -6517340843571949786L;

	private SurchargeDetails surchargeDetails = new SurchargeDetails();
	private String emailId;
	private String paymentType;
	private String response;

	private BigDecimal surchargeAmount;
	private BigDecimal surchargePercentage;
	private BigDecimal minTransactionAmount;
	private String paymentTypeName;
	private String paymentsRegion;
	private String loginEmailId;
	private String userType;
	private Date currentDate = new Date();

	public String execute() {

		User user = userDao.findPayIdByEmail(emailId);
		paymentTypeName = PaymentType.getpaymentName(paymentType);
		String payId = user.getPayId();
		String merchantName = user.getBusinessName();

		try {

			SurchargeDetails surchargeDetailsFromDb = surchargeDetailsDao.findDetailsByRegion(payId, paymentTypeName,paymentsRegion);

			StringBuilder permissions = new StringBuilder();
			permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));

			if (surchargeDetailsFromDb != null) {

				// If userType is admin , change surcharge status to inactive
				// and create new surcharge
				if (userType.equals(UserType.ADMIN.toString()) || permissions.toString().contains("Create Surcharge")) {

					disablePreviousCharge(surchargeDetailsFromDb);
					cancelPendingCharge(payId, paymentTypeName);
					createNewSurcharge(payId, TDRStatus.ACTIVE);
					//pendingRequestEmailProcessor.processMerchantSurchargeRequestEmail(TDRStatus.ACTIVE.getName(), loginEmailId, userType, merchantName, payId);
					

				} else {
					
					if (checkExistingSurchargeRequest(payId, paymentTypeName,paymentsRegion)){
						
						setResponse(ErrorType.MERCHANT_SURCHARGE_REQUEST_ALREADY_PENDING.getResponseMessage());
						return SUCCESS;
					}
					createNewSurcharge(payId, TDRStatus.PENDING);
					//pendingRequestEmailProcessor.processMerchantSurchargeRequestEmail(TDRStatus.PENDING.getName(), loginEmailId, userType, merchantName, payId);
					setResponse(ErrorType.MERCHANT_SURCHARGE_REQUEST_SENT_FOR_APPROVAL.getResponseMessage());
					return SUCCESS;
				}

			} else {

				
				if (userType.equals(UserType.ADMIN.toString()) || permissions.toString().contains("Create Surcharge")) {
					cancelPendingCharge(payId, paymentTypeName);
					createNewSurcharge(payId, TDRStatus.ACTIVE);
				} else {
					
					if (checkExistingSurchargeRequest(payId, paymentTypeName,paymentsRegion)){
						
						setResponse(ErrorType.MERCHANT_SURCHARGE_REQUEST_ALREADY_PENDING.getResponseMessage());
						return SUCCESS;
					}
					createNewSurcharge(payId, TDRStatus.PENDING);
					setResponse(ErrorType.MERCHANT_SURCHARGE_REQUEST_SENT_FOR_APPROVAL.getResponseMessage());
					return SUCCESS;
				}
			}

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		setResponse(ErrorType.MERCHANT_SURCHARGE_UPDATED.getResponseMessage());
		return SUCCESS;
	}
	public void validate() {
	if ((validator.validateBlankField(getEmailId()))) {
		addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.EMAILID,getEmailId()))) {
		addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getPaymentType()))) {
		addFieldError(CrmFieldType.PAYMENT_TYPE.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.PAYMENT_TYPE,getPaymentType()))) {
		addFieldError(CrmFieldType.PAYMENT_TYPE.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getResponse()))) {
		//addFieldError(CrmFieldType.RESPONSE.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.RESPONSE,getResponse()))) {
		addFieldError(CrmFieldType.RESPONSE.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getLoginEmailId()))) {
		addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.EMAILID,getLoginEmailId()))) {
		addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getUserType()))) {
		addFieldError(CrmFieldType.USER_TYPE.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.USER_TYPE,getUserType()))) {
		addFieldError(CrmFieldType.USER_TYPE.getName(), validator.getResonseObject().getResponseMessage());
	}
	
	}
	

	public void disablePreviousCharge(SurchargeDetails surchargeDetailsFromDb) {

		Session session = null;
		Long id = surchargeDetailsFromDb.getId();
		session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		session.load(surchargeDetailsFromDb, surchargeDetailsFromDb.getId());
		try {
			SurchargeDetails surchargeDetails = (SurchargeDetails) session.get(SurchargeDetails.class, id);
			surchargeDetails.setStatus(TDRStatus.INACTIVE);
			surchargeDetails.setUpdatedDate(currentDate);
			surchargeDetails.setProcessedBy(loginEmailId);
			session.update(surchargeDetails);
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			logger.error("Exception " , e);
		} finally {
			session.close();
		}

	}

	public boolean checkExistingSurchargeRequest(String payId , String paymentTypeName , String paymentsRegion){
		
		SurchargeDetails surchargeDetails = new SurchargeDetails();
		surchargeDetails = surchargeDetailsDao.findPendingDetailsByRegion(payId, paymentTypeName,paymentsRegion);
		if (surchargeDetails != null){
			return true;
		}
		return false;
	}
	public void cancelPendingCharge(String payId, String paymentType) {

		Session session = null;

		SurchargeDetails surchargeDetailsFromDb = surchargeDetailsDao.findPendingDetailsByRegion(payId, paymentType,paymentsRegion);
		if (surchargeDetailsFromDb != null) {
			Long id = surchargeDetailsFromDb.getId();
			session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			session.load(surchargeDetailsFromDb, surchargeDetailsFromDb.getId());
			try {
				SurchargeDetails surchargeDetails = (SurchargeDetails) session.get(SurchargeDetails.class, id);
				surchargeDetails.setStatus(TDRStatus.CANCELLED);
				surchargeDetails.setUpdatedDate(currentDate);
				surchargeDetails.setProcessedBy(loginEmailId);
				session.update(surchargeDetails);
				tx.commit();
			} catch (HibernateException e) {
				if (tx != null)
					tx.rollback();
				e.printStackTrace();
			} finally {
				session.close();
			}
		}
	}

	public void createNewSurcharge(String payId, TDRStatus status) {

		SurchargeDetails newSurchargeDetails = new SurchargeDetails();
		newSurchargeDetails.setPayId(payId);
		newSurchargeDetails.setPaymentType(paymentTypeName);
		if (String.valueOf(surchargeAmount).equals("")) {
			newSurchargeDetails.setSurchargeAmount(BigDecimal.ZERO);
		}
		else {
			newSurchargeDetails.setSurchargeAmount(surchargeAmount);
		}
		
		if (String.valueOf(surchargePercentage).equals("")) {
			newSurchargeDetails.setSurchargePercentage(BigDecimal.ZERO);
		}
		else {
			newSurchargeDetails.setSurchargePercentage(surchargePercentage);
		}
		
		if (String.valueOf(surchargePercentage).equals("")) {
			newSurchargeDetails.setMinTransactionAmount(BigDecimal.ZERO);
		}
		else {
			newSurchargeDetails.setMinTransactionAmount(minTransactionAmount);
		}
		
		
		newSurchargeDetails.setStatus(status);
		newSurchargeDetails.setCreatedDate(currentDate);
		newSurchargeDetails.setUpdatedDate(currentDate);
		
		if (paymentsRegion.equals(AccountCurrencyRegion.DOMESTIC.toString())) {
			
			newSurchargeDetails.setPaymentsRegion(AccountCurrencyRegion.DOMESTIC);
		}
		else {
			newSurchargeDetails.setPaymentsRegion(AccountCurrencyRegion.INTERNATIONAL
					);
		}
		
		newSurchargeDetails.setId(null);
		newSurchargeDetails.setRequestedBy(loginEmailId);
		if (status.equals(TDRStatus.ACTIVE)){
			newSurchargeDetails.setProcessedBy(loginEmailId);
		}
		surchargeDetailsDao.create(newSurchargeDetails);
		
	}

	public BigDecimal getSurchargeAmount() {
		return surchargeAmount;
	}

	public void setSurchargeAmount(BigDecimal surchargeAmount) {
		this.surchargeAmount = surchargeAmount;
	}

	public BigDecimal getSurchargePercentage() {
		return surchargePercentage;
	}

	public void setSurchargePercentage(BigDecimal surchargePercentage) {
		this.surchargePercentage = surchargePercentage;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public SurchargeDetails getSurchargeDetails() {
		return surchargeDetails;
	}

	public void setSurchargeDetails(SurchargeDetails surchargeDetails) {
		this.surchargeDetails = surchargeDetails;
	}
	
	public String getLoginEmailId() {
		return loginEmailId;
	}

	public void setLoginEmailId(String loginEmailId) {
		this.loginEmailId = loginEmailId;
	}
	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}
	public BigDecimal getMinTransactionAmount() {
		return minTransactionAmount;
	}
	public void setMinTransactionAmount(BigDecimal minTransactionAmount) {
		this.minTransactionAmount = minTransactionAmount;
	}
	public String getPaymentsRegion() {
		return paymentsRegion;
	}
	public void setPaymentsRegion(String paymentsRegion) {
		this.paymentsRegion = paymentsRegion;
	}
}
