package com.paymentgateway.crm.action;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.Surcharge;
import com.paymentgateway.commons.user.SurchargeDao;
import com.paymentgateway.commons.user.SurchargeDetails;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PendingRequestEmailProcessor;
import com.paymentgateway.commons.util.TDRStatus;

/**
 * @author Shaiwal
 *
 */
public class SurchargeMappingDetailsEditAction extends AbstractSecureAction {

	@Autowired
	private SurchargeDao surchargeDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private PendingRequestEmailProcessor pendingRequestEmailProcessor;

	@Autowired
	private CrmValidator validator;

	private static Logger logger = LoggerFactory.getLogger(SurchargeMappingDetailsEditAction.class.getName());
	private static final long serialVersionUID = -6517340843571949786L;

	private SurchargeDetails surchargeDetails = new SurchargeDetails();
	private String emailId;
	private String paymentType;

	private Long id;
	private String payId;
	private String acquirer;
	private String merchantIndustryType;
	private String mopType;
	private BigDecimal bankSurchargePercentageOnCommercial;
	private BigDecimal bankSurchargePercentageOnCustomer;
	private BigDecimal bankSurchargePercentageOffCommercial;
	private BigDecimal bankSurchargePercentageOffCustomer;
	private BigDecimal bankSurchargeAmountOnCommercial;
	private BigDecimal bankSurchargeAmountOnCustomer;
	private BigDecimal bankSurchargeAmountOffCommercial;
	private BigDecimal bankSurchargeAmountOffCustomer;
	private boolean allowOnOff;
	private String status;
	private String response;
	private String userType;
	private String loginUserEmailId;
	private String paymentsRegion;
	private Date currentDate = new Date();

	public String execute() {

		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));

		User user = userDao.findPayIdByEmail(emailId);
		payId = user.getPayId();
		String merchantName = user.getBusinessName();
		Session session = null;

		//String paymentTypeName = PaymentType.getInstanceUsingCode(paymentType).getName();
		//SurchargeDetails surchargeDetail = surchargeDetailsDao.findDetails(payId, paymentTypeName);

		/*if (surchargeDetail.getSurchargePercentage().compareTo(bankSurchargePercentageOn) < 0
				|| surchargeDetail.getSurchargeAmount().compareTo(bankSurchargeAmountOn) < 0) {
			return ERROR;
		}
		if (allowOnOff) {
			if (surchargeDetail.getSurchargePercentage().compareTo(bankSurchargePercentageOff) < 0
					|| surchargeDetail.getSurchargeAmount().compareTo(bankSurchargeAmountOff) < 0) {
				return ERROR;
			}
		}*/

		if (status.equals(TDRStatus.ACTIVE.getName())) {

			if (userType.equals(UserType.ADMIN.toString()) || permissions.toString().contains("Create Surcharge")) {
				try {

					// Cancel any pending requests for surcharge update
					cancelPendingCharge(payId, paymentType, acquirer, mopType);

					List<Surcharge> existingSurchargeList = new ArrayList<Surcharge>();
					existingSurchargeList = surchargeDao.findSurchargeListByPayIdAcquirerNameRegion(payId, paymentType,
							acquirer, mopType,paymentsRegion);

					for (Surcharge surcharge : existingSurchargeList) {

						try {

							session = HibernateSessionProvider.getSession();
							Transaction tx = session.beginTransaction();
							id = surcharge.getId();
							session.load(surcharge, surcharge.getId());
							Surcharge surchargeDetails = (Surcharge) session.get(Surcharge.class, id);
							surchargeDetails.setStatus(TDRStatus.INACTIVE);
							surchargeDetails.setUpdatedDate(currentDate);
							surchargeDetails.setProcessedBy(loginUserEmailId);
							session.update(surchargeDetails);
							tx.commit();
							session.close();

						} catch (HibernateException e) {

							logger.error("Exception " , e);
						} finally {

							session.close();
						}
					}
				}

				catch (Exception e) {
					logger.error("Exception " , e);
				}
			}

		}

		if (allowOnOff) {

			if (userType.equals(UserType.ADMIN.toString()) || permissions.toString().contains("Create Surcharge")) {
				createNewSurcharge("1", bankSurchargeAmountOnCommercial,bankSurchargeAmountOnCustomer, bankSurchargePercentageOnCommercial, 
						bankSurchargePercentageOnCustomer,TDRStatus.ACTIVE);
				createNewSurcharge("2", bankSurchargeAmountOffCommercial,bankSurchargeAmountOffCustomer, bankSurchargePercentageOffCommercial, 
						bankSurchargePercentageOffCustomer,TDRStatus.ACTIVE);				
			//	pendingRequestEmailProcessor.processBankSurchargeRequestEmail(TDRStatus.ACTIVE.getName(),
			//			loginUserEmailId, userType, merchantName, payId);
				setResponse(ErrorType.BANK_SURCHARGE_UPDATED.getResponseMessage());
				return SUCCESS;
			} else {

				List<Surcharge> pendingSurchargeList = new ArrayList<Surcharge>();
				pendingSurchargeList = surchargeDao.findPendingSurchargeListByPayIdAcquirerName(payId, paymentType,
						acquirer, mopType);
				if (pendingSurchargeList.size() > 0) {

					setResponse(ErrorType.BANK_SURCHARGE_REQUEST_ALREADY_PENDING.getResponseMessage());
					return SUCCESS;
				} else {
					createNewSurcharge("1", bankSurchargeAmountOnCommercial, bankSurchargeAmountOnCustomer,bankSurchargePercentageOnCommercial,
							bankSurchargePercentageOnCustomer,TDRStatus.PENDING);
					createNewSurcharge("2", bankSurchargeAmountOffCommercial, bankSurchargeAmountOffCustomer,bankSurchargePercentageOffCommercial,
							bankSurchargePercentageOffCustomer,TDRStatus.PENDING);
			//		pendingRequestEmailProcessor.processBankSurchargeRequestEmail(TDRStatus.PENDING.getName(),
			//				loginUserEmailId, userType, merchantName, payId);
					setResponse(ErrorType.BANK_SURCHARGE_REQUEST_SENT_FOR_APPROVAL.getResponseMessage());
					return SUCCESS;
				}

			}
		} else {
			if (userType.equals(UserType.ADMIN.toString()) || permissions.toString().contains("Create Surcharge")) {
				createNewSurcharge("0", bankSurchargeAmountOnCommercial, bankSurchargeAmountOnCustomer ,bankSurchargePercentageOnCommercial,bankSurchargePercentageOnCustomer, TDRStatus.ACTIVE);
				//createNewSurcharge("0", bankSurchargeAmountOnCustomer, bankSurchargePercentageOnCustomer, TDRStatus.ACTIVE, AccountCurrencyType.CONSUMER);
			//	pendingRequestEmailProcessor.processBankSurchargeRequestEmail(TDRStatus.ACTIVE.getName(),
			//			loginUserEmailId, userType, merchantName, payId);
				setResponse(ErrorType.BANK_SURCHARGE_UPDATED.getResponseMessage());
				return SUCCESS;
			} else {

				List<Surcharge> pendingSurchargeList = new ArrayList<Surcharge>();
				pendingSurchargeList = surchargeDao.findPendingSurchargeListByPayIdAcquirerName(payId, paymentType,
						acquirer, mopType);
				if (pendingSurchargeList.size() > 0) {

					setResponse(ErrorType.BANK_SURCHARGE_REQUEST_ALREADY_PENDING.getResponseMessage());
					return SUCCESS;
				} else {
					createNewSurcharge("0", bankSurchargeAmountOnCommercial, bankSurchargeAmountOnCustomer,bankSurchargePercentageOnCommercial, 
							bankSurchargePercentageOnCustomer,TDRStatus.PENDING);
					
					/*pendingRequestEmailProcessor.processBankSurchargeRequestEmail(pendingSurchargeList.TDRStatus.PENDING.getName(),
							loginUserEmailId, userType, merchantName, payId);*/
					setResponse(ErrorType.BANK_SURCHARGE_REQUEST_SENT_FOR_APPROVAL.getResponseMessage());
					return SUCCESS;
				}

			}

		}

	}

	public void validate() {
		if ((validator.validateBlankField(getEmailId()))) {
			addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.EMAILID, getEmailId()))) {
			addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(getPaymentType()))) {
			addFieldError(CrmFieldType.PAYMENT_TYPE.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.PAYMENT_TYPE, getPaymentType()))) {
			addFieldError(CrmFieldType.PAYMENT_TYPE.getName(), validator.getResonseObject().getResponseMessage());
		}

		if ((validator.validateBlankField(getAcquirer()))) {
			addFieldError(CrmFieldType.ACQUIRER.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.ACQUIRER, getAcquirer()))) {
			addFieldError(CrmFieldType.ACQUIRER.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(getMerchantIndustryType()))) {
			addFieldError(CrmFieldType.BUSINESS_NAME.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.BUSINESS_NAME, getMerchantIndustryType()))) {
			addFieldError(CrmFieldType.BUSINESS_NAME.getName(), validator.getResonseObject().getResponseMessage());
		}
		/*
		 * if ((validator.validateBlankField(getMopType()))) {
		 * addFieldError(CrmFieldType.MOP_TYPE.getName(),
		 * validator.getResonseObject().getResponseMessage()); } else if
		 * (!(validator.validateField(CrmFieldType.MOP_TYPE, getMopType()))) {
		 * addFieldError(CrmFieldType.MOP_TYPE.getName(),
		 * validator.getResonseObject().getResponseMessage()); }
		 */
		if ((validator.validateBlankField(getStatus()))) {
			addFieldError(CrmFieldType.STATUS.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.STATUS, getStatus()))) {
			addFieldError(CrmFieldType.STATUS.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(getResponse()))) {
			//addFieldError(CrmFieldType.RESPONSE.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.RESPONSE, getResponse()))) {
			addFieldError(CrmFieldType.RESPONSE.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(getUserType()))) {
			addFieldError(CrmFieldType.USER_TYPE.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.USER_TYPE, getUserType()))) {
			addFieldError(CrmFieldType.USER_TYPE.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(getLoginUserEmailId()))) {
			addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.EMAILID, getLoginUserEmailId()))) {
			addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
		}
	}

	private void createNewSurcharge(String onOff, BigDecimal bankSurchargeAmountCommercial, BigDecimal bankSurchargeAmountCustomer,
			BigDecimal bankSurchargePercentageCommercial, BigDecimal bankSurchargePercentageCustomer,
			TDRStatus status) {

		PaymentType payType = PaymentType.getInstanceUsingCode(paymentType);
		MopType mpType = MopType.getInstance(mopType);
		AccountCurrencyRegion acr ;
		
		if (getPaymentsRegion().equals(AccountCurrencyRegion.DOMESTIC.toString())) {
			acr = AccountCurrencyRegion.DOMESTIC;
		}
		else {
			acr  = AccountCurrencyRegion.INTERNATIONAL;
		}
		
		Surcharge newSurcharge = new Surcharge();
		newSurcharge.setAcquirerName(acquirer);
		newSurcharge.setAllowOnOff(allowOnOff);
		
			newSurcharge.setBankSurchargeAmountCommercial(bankSurchargeAmountCommercial);
			newSurcharge.setBankSurchargePercentageCommercial(bankSurchargePercentageCommercial);
			newSurcharge.setBankSurchargeAmountCustomer(bankSurchargeAmountCustomer);
			newSurcharge.setBankSurchargePercentageCustomer(bankSurchargePercentageCustomer);

		newSurcharge.setCreatedDate(currentDate);
		newSurcharge.setMerchantIndustryType(merchantIndustryType);
		newSurcharge.setMopType(mpType);
		newSurcharge.setOnOff(onOff);
		newSurcharge.setPayId(payId);
		newSurcharge.setPaymentType(payType);
		newSurcharge.setStatus(status);
		newSurcharge.setPaymentsRegion(acr);
		newSurcharge.setUpdatedDate(currentDate);
		if (status.equals(TDRStatus.ACTIVE)) {
			newSurcharge.setProcessedBy(loginUserEmailId);
		}
		newSurcharge.setRequestedBy(loginUserEmailId);

		surchargeDao.create(newSurcharge);

	}

	public void cancelPendingCharge(String payId, String paymentTypeName, String acquirerName, String mopTypeName) {

		Session session = null;
		List<Surcharge> pendingSurchargeList = new ArrayList<Surcharge>();
		pendingSurchargeList = surchargeDao.findPendingSurchargeListByPayIdAcquirerName(payId, paymentType, acquirer,
				mopType);

		if (pendingSurchargeList.size() > 0) {
			for (Surcharge surcharge : pendingSurchargeList) {

				try {

					session = HibernateSessionProvider.getSession();
					Transaction tx = session.beginTransaction();
					id = surcharge.getId();
					session.load(surcharge, surcharge.getId());
					Surcharge surchargeDetails = (Surcharge) session.get(Surcharge.class, id);
					surchargeDetails.setStatus(TDRStatus.CANCELLED);
					surchargeDetails.setUpdatedDate(currentDate);
					surchargeDetails.setProcessedBy(loginUserEmailId);
					session.update(surchargeDetails);
					tx.commit();
					session.close();

				} catch (HibernateException e) {
					logger.error("Exception in surcharge details edit , Exception " , e);
					
				} finally {

				}
			}
		}
	}

	public String getMerchantIndustryType() {
		return merchantIndustryType;
	}

	public void setMerchantIndustryType(String merchantIndustryType) {
		this.merchantIndustryType = merchantIndustryType;
	}

	public String getMopType() {
		return mopType;
	}

	public void setMopType(String mopType) {
		this.mopType = mopType;
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


	public boolean isAllowOnOff() {
		return allowOnOff;
	}

	public void setAllowOnOff(boolean allowOnOff) {
		this.allowOnOff = allowOnOff;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getLoginUserEmailId() {
		return loginUserEmailId;
	}

	public void setLoginUserEmailId(String loginUserEmailId) {
		this.loginUserEmailId = loginUserEmailId;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public BigDecimal getBankSurchargePercentageOnCommercial() {
		return bankSurchargePercentageOnCommercial;
	}

	public void setBankSurchargePercentageOnCommercial(BigDecimal bankSurchargePercentageOnCommercial) {
		this.bankSurchargePercentageOnCommercial = bankSurchargePercentageOnCommercial;
	}

	public BigDecimal getBankSurchargePercentageOnCustomer() {
		return bankSurchargePercentageOnCustomer;
	}

	public void setBankSurchargePercentageOnCustomer(BigDecimal bankSurchargePercentageOnCustomer) {
		this.bankSurchargePercentageOnCustomer = bankSurchargePercentageOnCustomer;
	}

	public BigDecimal getBankSurchargePercentageOffCommercial() {
		return bankSurchargePercentageOffCommercial;
	}

	public void setBankSurchargePercentageOffCommercial(BigDecimal bankSurchargePercentageOffCommercial) {
		this.bankSurchargePercentageOffCommercial = bankSurchargePercentageOffCommercial;
	}

	public BigDecimal getBankSurchargePercentageOffCustomer() {
		return bankSurchargePercentageOffCustomer;
	}

	public void setBankSurchargePercentageOffCustomer(BigDecimal bankSurchargePercentageOffCustomer) {
		this.bankSurchargePercentageOffCustomer = bankSurchargePercentageOffCustomer;
	}

	public BigDecimal getBankSurchargeAmountOnCommercial() {
		return bankSurchargeAmountOnCommercial;
	}

	public void setBankSurchargeAmountOnCommercial(BigDecimal bankSurchargeAmountOnCommercial) {
		this.bankSurchargeAmountOnCommercial = bankSurchargeAmountOnCommercial;
	}

	public BigDecimal getBankSurchargeAmountOnCustomer() {
		return bankSurchargeAmountOnCustomer;
	}

	public void setBankSurchargeAmountOnCustomer(BigDecimal bankSurchargeAmountOnCustomer) {
		this.bankSurchargeAmountOnCustomer = bankSurchargeAmountOnCustomer;
	}

	public BigDecimal getBankSurchargeAmountOffCommercial() {
		return bankSurchargeAmountOffCommercial;
	}

	public void setBankSurchargeAmountOffCommercial(BigDecimal bankSurchargeAmountOffCommercial) {
		this.bankSurchargeAmountOffCommercial = bankSurchargeAmountOffCommercial;
	}

	public BigDecimal getBankSurchargeAmountOffCustomer() {
		return bankSurchargeAmountOffCustomer;
	}

	public void setBankSurchargeAmountOffCustomer(BigDecimal bankSurchargeAmountOffCustomer) {
		this.bankSurchargeAmountOffCustomer = bankSurchargeAmountOffCustomer;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getPaymentsRegion() {
		return paymentsRegion;
	}

	public void setPaymentsRegion(String paymentsRegion) {
		this.paymentsRegion = paymentsRegion;
	}

}
