package com.paymentgateway.crm.action;

import java.util.Date;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.ChargingDetailsDao;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PendingRequestEmailProcessor;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.onUsOffUs;
import com.paymentgateway.crm.actionBeans.ChargingDetailsMaintainer;

/**
 * @author Puneet, Amitosh
 *
 */
public class ChargingDetailEditAction extends AbstractSecureAction implements ModelDriven<ChargingDetails> {

	@Autowired
	private CrmValidator validator;
	
	@Autowired
	private PendingRequestEmailProcessor pendingRequestEmailProcessor;

	@Autowired
	private ChargingDetailsMaintainer editChargingDetails;

	@Autowired
	private ChargingDetailsDao chargingDetailsDao;

	@Autowired
	private UserDao userDao;

	private static Logger logger = LoggerFactory.getLogger(ChargingDetailEditAction.class.getName());
	private static final long serialVersionUID = -6517340843571949786L;

	private ChargingDetails chargingDetails = new ChargingDetails();
	private String emailId;
	private String acquirer;
	private String response;
	private String userType;
	private String loginUserEmailId;
	private String currency;
	private String mopType;
	private String transactionType;
	private Object tdrFcDetail;
	private String acquiringMode;
	private String merchantGST;
	private String allowFC;
	private String maxChargeMerchant;
	private String maxChargeAcquirer;
	private String cardHolderType;
	private String paymentRegion;
	private User sessionUser = new User();
	private String paymentType;
	private String slab1;
	private String slab2;
	private String slab3;
	private boolean isPermissionGranted = false;
	private boolean newEntry = false;
	private String chargesFlag;
	public String execute() {
		try {
			
			User merchantPayId = userDao.findPayIdByEmail(emailId);
			long serviceTax=Long.parseLong(PropertiesManager.propertiesMap.get("SERVICE_TAX"));
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			loginUserEmailId = sessionUser.getEmailId();
			userType = sessionUser.getUserType().name().toString();

			String[] slabArray1 = slab1.split(",");
			String[] slabArray2 = slab2.split(",");
			String[] slabArray3 = slab3.split(",");

			StringBuilder permissions = new StringBuilder();
			permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));

			if (permissions.toString().contains("Create TDR")) {
				isPermissionGranted = true;
			}

			if (newEntry == false) {
				for (int i = 1; i <= 3; i++) {
					chargingDetails = chargingDetailsDao.getChargingDetailsPerSlabs(merchantPayId.getPayId(),
							PaymentType.getInstance(paymentType).toString(), paymentRegion,
							Currency.getNumericCode(currency), MopType.getInstanceIgnoreCase(mopType).toString(),
							transactionType.toUpperCase(), acquiringMode,
							AcquirerType.getAcquirerName(acquirer), "0" + i, cardHolderType);
					if (chargingDetails == null) {
						newEntry = true;
						break;
					}

					chargingDetails.setMaxChargeAcquirer(Double.valueOf(maxChargeAcquirer));
					chargingDetails.setMaxChargeMerchant(Double.valueOf(maxChargeMerchant));
					chargingDetails.setAcquiringMode(onUsOffUs.valueOf(acquiringMode));
					chargingDetails.setAllowFixCharge(Boolean.valueOf(allowFC));
					chargingDetails.setChargesFlag(Boolean.valueOf(chargesFlag));
					if (chargingDetails.getSlabId().equalsIgnoreCase("01")) {
						chargingDetails.setMinTxnAmount(
								Double.valueOf(PropertiesManager.propertiesMap.get("LimitSlab1MinAmount")));
						chargingDetails.setMaxTxnAmount(
								Double.valueOf(PropertiesManager.propertiesMap.get("LimitSlab1MaxAmount")));
						chargingDetails.setMerchantTDR(Double.valueOf(slabArray1[0]));
						chargingDetails.setMerchantFixCharge(Double.valueOf(slabArray1[1]));
						chargingDetails.setBankTDR(Double.valueOf(slabArray1[2]));
						chargingDetails.setBankFixCharge(Double.valueOf(slabArray1[3]));
						chargingDetails.setResellerTDR(Double.valueOf(slabArray1[4]));
						chargingDetails.setResellerFixCharge(Double.valueOf(slabArray1[5]));
						chargingDetails.setMerchantServiceTax(serviceTax);
						chargingDetails.setBankServiceTax(serviceTax);
						chargingDetails.setResellerServiceTax(serviceTax);
					} else if (chargingDetails.getSlabId().equalsIgnoreCase("02")) {
						chargingDetails.setMinTxnAmount(
								Double.valueOf(PropertiesManager.propertiesMap.get("LimitSlab2MinAmount")));
						chargingDetails.setMaxTxnAmount(
								Double.valueOf(PropertiesManager.propertiesMap.get("LimitSlab2MaxAmount")));
						chargingDetails.setMerchantTDR(Double.valueOf(slabArray2[0]));
						chargingDetails.setMerchantFixCharge(Double.valueOf(slabArray2[1]));
						chargingDetails.setBankTDR(Double.valueOf(slabArray2[2]));
						chargingDetails.setBankFixCharge(Double.valueOf(slabArray2[3]));
						chargingDetails.setResellerTDR(Double.valueOf(slabArray2[4]));
						chargingDetails.setResellerFixCharge(Double.valueOf(slabArray2[5]));
						chargingDetails.setMerchantServiceTax(serviceTax);
						chargingDetails.setBankServiceTax(serviceTax);
						chargingDetails.setResellerServiceTax(serviceTax);
					} else if (chargingDetails.getSlabId().equalsIgnoreCase("03")) {
						chargingDetails.setMinTxnAmount(
								Double.valueOf(PropertiesManager.propertiesMap.get("LimitSlab3MinAmount")));
						chargingDetails.setMaxTxnAmount(
								Double.valueOf(PropertiesManager.propertiesMap.get("LimitSlab3MaxAmount")));
						chargingDetails.setMerchantTDR(Double.valueOf(slabArray3[0]));
						chargingDetails.setMerchantFixCharge(Double.valueOf(slabArray3[1]));
						chargingDetails.setBankTDR(Double.valueOf(slabArray3[2]));
						chargingDetails.setBankFixCharge(Double.valueOf(slabArray3[3]));
						chargingDetails.setResellerTDR(Double.valueOf(slabArray3[4]));
						chargingDetails.setResellerFixCharge(Double.valueOf(slabArray3[5]));
						chargingDetails.setMerchantServiceTax(serviceTax);
						chargingDetails.setBankServiceTax(serviceTax);
						chargingDetails.setResellerServiceTax(serviceTax);
					}
					if(sessionUser.getUserType().equals(UserType.SUBADMIN)){
						chargingDetails.setRequestBySubAdmin(true);
					}
					editChargingDetails.editChargingDetail(emailId, acquirer, chargingDetails, isPermissionGranted,
							userType, loginUserEmailId);
				}
			}
			if (newEntry == true) {
				Date currentDate = new Date();
				User user = userDao.find(emailId);


				ChargingDetails chargingDetailsWithoutSlab = chargingDetailsDao.getChargingDetailsWithoutSlabs(
						merchantPayId.getPayId(), PaymentType.getInstance(paymentType).toString(),
						Currency.getNumericCode(currency), MopType.getInstanceIgnoreCase(mopType).toString(),
						transactionType.toUpperCase(), AcquirerType.getAcquirerName(acquirer), cardHolderType);
				if (chargingDetailsWithoutSlab == null) {
					chargingDetailsWithoutSlab = chargingDetailsDao.getChargingDetailsWithoutSlabs(merchantPayId.getPayId(),
							PaymentType.getInstance(paymentType).toString(), Currency.getNumericCode(currency),
							MopType.getInstanceIgnoreCase(mopType).toString(), transactionType.toUpperCase(),
							AcquirerType.getAcquirerName(acquirer), cardHolderType);
				}
				for (int i = 1; i <= 3; i++) {

					Account account = user.getAccountUsingAcquirerCode(acquirer);
					if (null == account) {
						throw new SystemException(ErrorType.ACQUIRER_NOT_FOUND,
								ErrorType.ACQUIRER_NOT_FOUND.getResponseMessage());
					}
					Session session = null;
					session = HibernateSessionProvider.getSession();
					Transaction tx = session.beginTransaction();
					session.load(account, account.getId());
					ChargingDetails chargingDetailsFromDb = null;
					for (ChargingDetails cDetail : account.getChargingDetails()) {
						if (cDetail.getId().equals(chargingDetailsWithoutSlab.getId())) {
							chargingDetailsFromDb = cDetail;
						}
					}
					if (userType.equals(UserType.ADMIN.toString()) || isPermissionGranted) {
						chargingDetailsFromDb.setStatus(TDRStatus.INACTIVE);
						chargingDetailsFromDb.setUpdatedDate(currentDate);
						chargingDetailsFromDb.setUpdateBy(loginUserEmailId);
					}

					ChargingDetails chargingDetails = new ChargingDetails();
					if (i == 1) {
						chargingDetails.setMinTxnAmount(
								Double.valueOf(PropertiesManager.propertiesMap.get("LimitSlab1MinAmount")));
						chargingDetails.setMaxTxnAmount(
								Double.valueOf(PropertiesManager.propertiesMap.get("LimitSlab1MaxAmount")));
						chargingDetails.setMerchantTDR(Double.valueOf(slabArray1[0]));
						chargingDetails.setMerchantFixCharge(Double.valueOf(slabArray1[1]));
						chargingDetails.setBankTDR(Double.valueOf(slabArray1[2]));
						chargingDetails.setBankFixCharge(Double.valueOf(slabArray1[3]));
						chargingDetails.setResellerTDR(Double.valueOf(slabArray1[4]));
						chargingDetails.setResellerFixCharge(Double.valueOf(slabArray1[5]));
						chargingDetails.setSlabId("01");
						chargingDetails.setMerchantServiceTax(serviceTax);
						chargingDetails.setBankServiceTax(serviceTax);
						chargingDetails.setResellerServiceTax(serviceTax);
					} else if (i == 2) {
						chargingDetails.setMinTxnAmount(
								Double.valueOf(PropertiesManager.propertiesMap.get("LimitSlab2MinAmount")));
						chargingDetails.setMaxTxnAmount(
								Double.valueOf(PropertiesManager.propertiesMap.get("LimitSlab2MaxAmount")));
						chargingDetails.setMerchantTDR(Double.valueOf(slabArray2[0]));
						chargingDetails.setMerchantFixCharge(Double.valueOf(slabArray2[1]));
						chargingDetails.setBankTDR(Double.valueOf(slabArray2[2]));
						chargingDetails.setBankFixCharge(Double.valueOf(slabArray2[3]));
						chargingDetails.setResellerTDR(Double.valueOf(slabArray2[4]));
						chargingDetails.setResellerFixCharge(Double.valueOf(slabArray2[5]));
						chargingDetails.setSlabId("02");
						chargingDetails.setMerchantServiceTax(serviceTax);
						chargingDetails.setBankServiceTax(serviceTax);
						chargingDetails.setResellerServiceTax(serviceTax);
					} else if (i == 3) {
						chargingDetails.setMinTxnAmount(
								Double.valueOf(PropertiesManager.propertiesMap.get("LimitSlab3MinAmount")));
						chargingDetails.setMaxTxnAmount(
								Double.valueOf(PropertiesManager.propertiesMap.get("LimitSlab3MaxAmount")));
						chargingDetails.setMerchantTDR(Double.valueOf(slabArray3[0]));
						chargingDetails.setMerchantFixCharge(Double.valueOf(slabArray3[1]));
						chargingDetails.setBankTDR(Double.valueOf(slabArray3[2]));
						chargingDetails.setBankFixCharge(Double.valueOf(slabArray3[3]));
						chargingDetails.setResellerTDR(Double.valueOf(slabArray3[4]));
						chargingDetails.setResellerFixCharge(Double.valueOf(slabArray3[5]));
						chargingDetails.setSlabId("03");
						chargingDetails.setMerchantServiceTax(serviceTax);
						chargingDetails.setBankServiceTax(serviceTax);
						chargingDetails.setResellerServiceTax(serviceTax);
					}

					chargingDetails.setCurrency(Currency.getNumericCode(currency));
					chargingDetails.setPaymentType(PaymentType.getInstanceIgnoreCase(paymentType));
					chargingDetails.setTransactionType(TransactionType.getInstance(transactionType));
					chargingDetails.setMopType(MopType.getInstanceIgnoreCase(mopType));
					chargingDetails.setMaxChargeAcquirer(Double.valueOf(maxChargeAcquirer));
					if (userType.equals(UserType.ADMIN.toString())) {
						chargingDetails.setStatus(TDRStatus.ACTIVE);
						chargingDetails.setUpdateBy(loginUserEmailId);
						chargingDetails.setRequestedBy(loginUserEmailId);
					} else {
						chargingDetails.setStatus(TDRStatus.PENDING);
						chargingDetails.setRequestedBy(loginUserEmailId);
						chargingDetails.setRequestBySubAdmin(true);
						setResponse(ErrorType.CHARGING_DETAILS_REQUEST_APPROVAL.getResponseMessage());
					}
					chargingDetails.setCreatedDate(currentDate);
					chargingDetails.setMaxChargeMerchant(Double.valueOf(maxChargeMerchant));
					chargingDetails.setAcquiringMode(onUsOffUs.valueOf(acquiringMode));
					chargingDetails.setAllowFixCharge(Boolean.valueOf(allowFC));
					chargingDetails.setChargesFlag(Boolean.valueOf(chargesFlag));
					chargingDetails.setAcquirerName(chargingDetailsFromDb.getAcquirerName());
					chargingDetails.setPayId(chargingDetailsFromDb.getPayId());
					chargingDetails.setPaymentsRegion(AccountCurrencyRegion.valueOf(paymentRegion));
					chargingDetails.setCardHolderType(CardHolderType.valueOf(cardHolderType));
					
					if(sessionUser.getUserType().equals(UserType.SUBADMIN)){
						chargingDetails.setRequestBySubAdmin(true);
					}
					account.addChargingDetail(chargingDetails);
					session.saveOrUpdate(account);
					tx.commit();
				}
			}
			if (userType.equals(UserType.ADMIN.toString())) {
				
				//pendingRequestEmailProcessor.processTDRRequestEmail(requestStatus, loginUserEmailId, userType, merchantName, merchantPayId);
				setResponse(ErrorType.SUCCESSFULLY_SAVED.getResponseMessage());
			} else {
				pendingRequestEmailProcessor.processTDRRequestEmail("Pending", loginUserEmailId, userType, merchantPayId.getEmailId(), PermissionType.CREATE_TDR.getPermission());
				setResponse(ErrorType.CHARGING_DETAILS_REQUEST_APPROVAL.getResponseMessage());
			}

		} catch (Exception exception) {
			setResponse(ErrorType.CHARGINGDETAIL_NOT_SAVED.getResponseMessage());
			logger.error("Exception", exception);
		}
		return SUCCESS;
	}



	public void validate() {
	}

	public ChargingDetailsMaintainer getEditChargingDetails() {
		return editChargingDetails;
	}

	public void setEditChargingDetails(ChargingDetailsMaintainer editChargingDetails) {
		this.editChargingDetails = editChargingDetails;
	}

	public ChargingDetails getChargingDetails() {
		return chargingDetails;
	}

	public void setChargingDetails(ChargingDetails chargingDetails) {
		this.chargingDetails = chargingDetails;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
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

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getMopType() {
		return mopType;
	}

	public void setMopType(String mopType) {
		this.mopType = mopType;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public Object getTdrFcDetail() {
		return tdrFcDetail;
	}

	public void setTdrFcDetail(Object tdrFcDetail) {
		this.tdrFcDetail = tdrFcDetail;
	}

	public String getAcquiringMode() {
		return acquiringMode;
	}

	public void setAcquiringMode(String acquiringMode) {
		this.acquiringMode = acquiringMode;
	}

	public String getMerchantGST() {
		return merchantGST;
	}

	public void setMerchantGST(String merchantGST) {
		this.merchantGST = merchantGST;
	}

	public String getAllowFC() {
		return allowFC;
	}

	public void setAllowFC(String allowFC) {
		this.allowFC = allowFC;
	}

	public String getMaxChargeMerchant() {
		return maxChargeMerchant;
	}

	public void setMaxChargeMerchant(String maxChargeMerchant) {
		this.maxChargeMerchant = maxChargeMerchant;
	}

	public String getMaxChargeAcquirer() {
		return maxChargeAcquirer;
	}

	public void setMaxChargeAcquirer(String maxChargeAcquirer) {
		this.maxChargeAcquirer = maxChargeAcquirer;
	}

	public String getCardHolderType() {
		return cardHolderType;
	}

	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}

	public String getPaymentRegion() {
		return paymentRegion;
	}

	public void setPaymentRegion(String paymentRegion) {
		this.paymentRegion = paymentRegion;
	}

	public boolean isPermissionGranted() {
		return isPermissionGranted;
	}

	public void setPermissionGranted(boolean isPermissionGranted) {
		this.isPermissionGranted = isPermissionGranted;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getSlab1() {
		return slab1;
	}

	public void setSlab1(String slab1) {
		this.slab1 = slab1;
	}

	public String getSlab2() {
		return slab2;
	}

	public void setSlab2(String slab2) {
		this.slab2 = slab2;
	}

	public String getSlab3() {
		return slab3;
	}

	public void setSlab3(String slab3) {
		this.slab3 = slab3;
	}

	@Override
	public ChargingDetails getModel() {
		// TODO Auto-generated method stub
		return null;
	}



	public String getChargesFlag() {
		return chargesFlag;
	}



	public void setChargesFlag(String chargesFlag) {
		this.chargesFlag = chargesFlag;
	}
	
}
