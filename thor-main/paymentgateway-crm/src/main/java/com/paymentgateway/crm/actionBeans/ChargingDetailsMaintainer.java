package com.paymentgateway.crm.actionBeans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.SerializationUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.ChargingDetailsFactory;
import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.dao.RouterConfigurationDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.ChargingDetailsDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PendingRequestEmailProcessor;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.onUsOffUs;

@Service
public class ChargingDetailsMaintainer {

	@Autowired
	private UserDao userDao;

	@Autowired
	private ChargingDetailsDao chargingDetailsDao;

	@Autowired
	private ChargingDetailsFactory chargingDetailProvider;

	@Autowired
	private PendingRequestEmailProcessor pendingRequestEmailProcessor;
	
	@Autowired
	private RouterConfigurationDao routerConfigDao;
	

	private static Logger logger = LoggerFactory.getLogger(ChargingDetailsMaintainer.class.getName());

	public ChargingDetailsMaintainer() {

	}

	public void editChargingDetail(String emailId, String acquirer, ChargingDetails chargingDetail,
			boolean isPermissionGranted, String userType, String loginUserEmailId) throws SystemException {
		Session session = null;
		try {
			User user = userDao.find(emailId);
			Account account = user.getAccountUsingAcquirerCode(acquirer);
			if (null == account) {
				throw new SystemException(ErrorType.ACQUIRER_NOT_FOUND,
						ErrorType.ACQUIRER_NOT_FOUND.getResponseMessage());
			}

			session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			session.load(account, account.getId());

			ChargingDetails chargingDetailFromDb = chargingDetailProvider.getSingleChargingDetail(account,
					chargingDetail.getId());

			if (null == chargingDetailFromDb) {
				throw new SystemException(ErrorType.CHARGINGDETAIL_NOT_FETCHED,
						ErrorType.CHARGINGDETAIL_NOT_FETCHED.getResponseMessage());
			}

			if ((chargingDetailFromDb.getStatus().equals(TDRStatus.ACTIVE))) { // deactivate current and add new charging details
				editExistingChargingDetails(account, chargingDetailFromDb, chargingDetail, isPermissionGranted,
						userType, loginUserEmailId);
				session.saveOrUpdate(account);
				tx.commit();
				
				// Here Charging Details are getting updated from their default values, so create config for both ONUS and OFFUS
				routerConfigDao.updateRouterByCD(chargingDetail,"updated",true);
				
			} else { // Edit blank charging detail
				chargingDetail = editBlankChargingDetails(account, chargingDetailFromDb, chargingDetail,
						isPermissionGranted, userType, loginUserEmailId);
				chargingDetailsDao.update(chargingDetail);
				routerConfigDao.updateRouterByCD(chargingDetail,"updated",true);
			//	if (userType.equalsIgnoreCase("SUBADMIN")) {

					// User loggedInUser = userDao.findPayIdByEmail(loginUserEmailId);
					/*
					 * String merchantBusinessName =
					 * userDao.getBusinessNameByPayId(chargingDetail.getPayId());
					 * logger.info("pendingRequestEmailProcessor.processTDRRequestEmail  " +
					 * chargingDetail.getStatus().getName() + "  loginUserEmailId " +
					 * loginUserEmailId + "  userType  " + userType + "   merchantBusinessName  " +
					 * merchantBusinessName + "  chargingDetail.getPayId()  " +
					 * chargingDetail.getPayId());
					 */
					// pendingRequestEmailProcessor.processTDRRequestEmail(chargingDetail.getStatus().getName(),
					// loginUserEmailId, userType, merchantBusinessName, chargingDetail.getPayId());

				//}
			}
		} finally {
			HibernateSessionProvider.closeSession(session);
		}
	}

	// TODO.. refactor
	public void editAllChargingDetails(String userEmail, String acquirer, ChargingDetails newChargingDetails,
			boolean isPermissionGranted, String userType) throws SystemException {
		Session session = null;
		Date currentDate = new Date();
		try {
			User user = userDao.find(userEmail);
			Account account = user.getAccountUsingAcquirerCode(acquirer);
			if (null == account) {
				throw new SystemException(ErrorType.ACQUIRER_NOT_FOUND,
						ErrorType.ACQUIRER_NOT_FOUND.getResponseMessage());
			}
			session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			session.load(account, account.getId());
			Set<ChargingDetails> chargingDetailList = account.getChargingDetails();
			List<ChargingDetails> newChargingDetailsList = new ArrayList<ChargingDetails>();
			for (ChargingDetails chargingDetailFromDb : chargingDetailList) {
				if (chargingDetailFromDb.getStatus().getName().equals(TDRStatus.ACTIVE.getName())
						&& chargingDetailFromDb.getPaymentType().getCode().equals(PaymentType.NET_BANKING.getCode())) {
					// edit only net-banking charging details
					if (!(chargingDetailFromDb.getMerchantTDR() == 0.0)) { // deactivate current and add new charging
																			// details
						// editExistingChargingDetails(account, chargingDetailFromDb,
						// newChargingDetails);

						if (userType.equals(UserType.ADMIN.toString()) || isPermissionGranted) {
							chargingDetailFromDb.setStatus(TDRStatus.INACTIVE);
							chargingDetailFromDb.setUpdatedDate(currentDate);
						}

						ChargingDetails newChargingDetail = SerializationUtils.clone(newChargingDetails);
						newChargingDetail.setAcquirerName(chargingDetailFromDb.getAcquirerName());
						newChargingDetail.setPayId(chargingDetailFromDb.getPayId());
						newChargingDetail.setPgServiceTax(newChargingDetails.getMerchantServiceTax());
						newChargingDetail.setBankServiceTax(newChargingDetails.getMerchantServiceTax());

						if (userType.equals(UserType.ADMIN.toString()) || isPermissionGranted) {
							newChargingDetail.setStatus(TDRStatus.ACTIVE);
						} else {
							newChargingDetail.setStatus(TDRStatus.PENDING);
						}
						newChargingDetail.setCreatedDate(currentDate);
						newChargingDetail.setMopType(chargingDetailFromDb.getMopType());
						newChargingDetail.setId(null);
						newChargingDetailsList.add(newChargingDetail);
					} else {// Edit blank charging detail
						chargingDetailFromDb.setAllowFixCharge(newChargingDetails.isAllowFixCharge());
						chargingDetailFromDb.setFixChargeLimit(newChargingDetails.getFixChargeLimit());
						chargingDetailFromDb.setBankFixCharge(newChargingDetails.getBankFixCharge());
						chargingDetailFromDb.setBankFixChargeAFC(newChargingDetails.getBankFixChargeAFC());
						chargingDetailFromDb.setBankServiceTax(newChargingDetails.getBankServiceTax());
						chargingDetailFromDb.setBankTDR(newChargingDetails.getBankTDR());
						chargingDetailFromDb.setBankTDRAFC(newChargingDetails.getBankTDRAFC());
						chargingDetailFromDb.setMerchantFixCharge(newChargingDetails.getMerchantFixCharge());
						chargingDetailFromDb.setMerchantFixChargeAFC(newChargingDetails.getMerchantFixChargeAFC());
						chargingDetailFromDb.setMerchantServiceTax(newChargingDetails.getMerchantServiceTax());
						chargingDetailFromDb.setMerchantTDR(newChargingDetails.getMerchantTDR());
						chargingDetailFromDb.setMerchantTDRAFC(newChargingDetails.getMerchantTDRAFC());
						//chargingDetailFromDb.setPgFixCharge(newChargingDetails.getPgFixCharge());
						//chargingDetailFromDb.setPgFixChargeAFC(newChargingDetails.getPgChargeAFC());
						//chargingDetailFromDb.setPgServiceTax(newChargingDetails.getPgServiceTax());
						//chargingDetailFromDb.setPgTDR(newChargingDetails.getPgTDR());
						//chargingDetailFromDb.setPgTDRAFC(newChargingDetails.getPgTDRAFC());
						chargingDetailFromDb.setCreatedDate(new Date());
					}
				}
			}
			for (ChargingDetails newChargingDetailFromList : newChargingDetailsList) {
				account.addChargingDetail(newChargingDetailFromList);
			}
			session.saveOrUpdate(account);
			tx.commit();
		} finally {
			HibernateSessionProvider.closeSession(session);
		}
	}

	public void editExistingChargingDetails(Account account, ChargingDetails chargingDetailFromDb,
			ChargingDetails newChargingDetails, boolean isPermissionGranted, String userType, String loginUserEmailId) {
		Date currentDate = new Date();

		if (chargingDetailFromDb.getStatus().equals(TDRStatus.ACTIVE)) {

			if (userType.equals(UserType.ADMIN.toString()) || isPermissionGranted) {

				chargingDetailFromDb.setStatus(TDRStatus.INACTIVE);
				chargingDetailFromDb.setUpdatedDate(currentDate);
				chargingDetailFromDb.setUpdateBy(loginUserEmailId);
			}

			ChargingDetails pendingChargingDetail = new ChargingDetails();

			pendingChargingDetail = chargingDetailsDao.findPendingChargingDetail(chargingDetailFromDb.getMopType(),
					chargingDetailFromDb.getPaymentType(), chargingDetailFromDb.getTransactionType(),
					chargingDetailFromDb.getAcquirerName(), chargingDetailFromDb.getCurrency(),
					chargingDetailFromDb.getPayId(), chargingDetailFromDb.getSlabId(), chargingDetailFromDb.getCardHolderType());

			Session session = null; //new changes
			if (null != pendingChargingDetail) {
				//session is here
				if (pendingChargingDetail != null) {
					Long id = pendingChargingDetail.getId();
					session = HibernateSessionProvider.getSession();
					Transaction tx = session.beginTransaction();
					session.load(pendingChargingDetail, pendingChargingDetail.getId());
					try {
						ChargingDetails pendingChargingDetails = (ChargingDetails) session.get(ChargingDetails.class,
								id);
						pendingChargingDetails.setStatus(TDRStatus.CANCELLED);
						pendingChargingDetails.setUpdatedDate(currentDate);
						pendingChargingDetail.setUpdateBy(loginUserEmailId);
						session.update(pendingChargingDetails);
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

			ChargingDetails newChargingDetail = SerializationUtils.clone(newChargingDetails);
			newChargingDetail.setAcquirerName(chargingDetailFromDb.getAcquirerName());
			newChargingDetail.setPayId(chargingDetailFromDb.getPayId());
			newChargingDetail.setPgServiceTax(newChargingDetails.getMerchantServiceTax());
			newChargingDetail.setBankServiceTax(newChargingDetails.getMerchantServiceTax());
			newChargingDetail.setResellerServiceTax(newChargingDetails.getResellerServiceTax());
			
			if (userType.equals(UserType.ADMIN.toString()) || isPermissionGranted) {
				newChargingDetail.setStatus(TDRStatus.ACTIVE);
				newChargingDetail.setUpdateBy(loginUserEmailId);
				newChargingDetail.setRequestedBy(loginUserEmailId);
			} else {
				newChargingDetail.setStatus(TDRStatus.PENDING);
				newChargingDetail.setUpdateBy("");
				newChargingDetail.setRequestedBy(loginUserEmailId);
			}
			newChargingDetail.setCreatedDate(currentDate);
			newChargingDetail.setId(null);

			account.addChargingDetail(newChargingDetail);

		} else { // Edit current detail
			newChargingDetails.setAcquirerName(chargingDetailFromDb.getAcquirerName());
			newChargingDetails.setPayId(chargingDetailFromDb.getPayId());
			newChargingDetails.setPgServiceTax(newChargingDetails.getMerchantServiceTax());
			newChargingDetails.setBankServiceTax(newChargingDetails.getMerchantServiceTax());
			newChargingDetails.setResellerServiceTax(newChargingDetails.getResellerServiceTax());
			
			if (userType.equals(UserType.ADMIN.toString()) || isPermissionGranted) {
				newChargingDetails.setStatus(TDRStatus.ACTIVE);
			} else {
				newChargingDetails.setStatus(TDRStatus.PENDING);
			}
			newChargingDetails.setCreatedDate(currentDate);
			account.addChargingDetail(newChargingDetails);
		}
	}

	public ChargingDetails editBlankChargingDetails(Account account, ChargingDetails chargingDetailFromDb,
			ChargingDetails newChargingDetails, boolean isPermissionGranted, String userType, String loginUserEmailId) {
		Date currentDate = new Date();
		newChargingDetails.setAcquirerName(chargingDetailFromDb.getAcquirerName());
		newChargingDetails.setPayId(chargingDetailFromDb.getPayId());
		newChargingDetails.setPgServiceTax(newChargingDetails.getMerchantServiceTax());
		newChargingDetails.setBankServiceTax(newChargingDetails.getMerchantServiceTax());
		if (userType.equals(UserType.ADMIN.toString()) || isPermissionGranted) {
			newChargingDetails.setStatus(TDRStatus.ACTIVE);
		} else {
			newChargingDetails.setStatus(TDRStatus.PENDING);
		}
		newChargingDetails.setCreatedDate(currentDate);
		newChargingDetails.setUpdateBy(loginUserEmailId);
		return newChargingDetails;
	}

	public List<ChargingDetails> createChargingDetail(String acquirerName, String payId, String token, String currencyCode) {

		List<ChargingDetails> chargingDetailList = new ArrayList<ChargingDetails>();
		
		String[] splittedToken = token.split("-");

		MopType mopType = MopType.getmop(splittedToken[1]);
		PaymentType paymentType = PaymentType.getInstance(splittedToken[0]);
		
		chargingDetailList = createChargingDetail(paymentType,mopType,acquirerName,payId,currencyCode);
		
		/*
		 * List<ChargingDetails> cdList = createChargingDetail
		 * (paymentType,mopType,acquirerName,payId,currencyCode);
		 * 
		 * chargingDetail.setAcquirerName(acquirerName); chargingDetail.setPayId(payId);
		 * chargingDetail.setMopType(MopType.getmop(splittedToken[1]));
		 * chargingDetail.setPaymentType(PaymentType.getInstance(splittedToken[0]));
		 * String paymentType = chargingDetail.getPaymentType().getCode().toString(); if
		 * (StringUtils.isNotBlank(paymentType) &&
		 * paymentType.equalsIgnoreCase(PaymentType.NET_BANKING.getCode())) {
		 * chargingDetail.setTransactionType(TransactionType.SALE); } // end
		 * chargingDetail.setStatus(TDRStatus.ACTIVE);
		 * chargingDetail.setCurrency(currencyCode); if (splittedToken.length == 3) {
		 * chargingDetail.setTransactionType(TransactionType.SALE); }
		 */
		return chargingDetailList;
	}
	

	public List<ChargingDetails> createChargingDetail(PaymentType paymentType, MopType mopType, String acquirerName,
			String payId, String accountCurrencyCode) {
		
		
		List<ChargingDetails> cdList = new ArrayList<ChargingDetails>();
		
		List<String> slabIdList = new ArrayList<String>();
		slabIdList.add("01,0.01,1000");
		slabIdList.add("02,1000.01,2000");
		slabIdList.add("03,2000.01,1000000");
		
		if(paymentType.equals(PaymentType.CREDIT_CARD) || paymentType.equals(PaymentType.DEBIT_CARD )|| paymentType.equals(PaymentType.EMI_CC ) || paymentType.equals(PaymentType.EMI_DC )) {

			List<onUsOffUs> acquiringModeListDom = new ArrayList<onUsOffUs>();
			acquiringModeListDom.add(onUsOffUs.OFF_US);
			acquiringModeListDom.add(onUsOffUs.ON_US);
			
			
			List<CardHolderType> cardHolderTypeListDom = new ArrayList<CardHolderType>();
			cardHolderTypeListDom.add(CardHolderType.COMMERCIAL);
			cardHolderTypeListDom.add(CardHolderType.CONSUMER);
			cardHolderTypeListDom.add(CardHolderType.PREMIUM);
			
			
			// Create Charging Details List for Domestic Cards
			for (onUsOffUs onOff : acquiringModeListDom) {
				for (CardHolderType cardType : cardHolderTypeListDom ) {
					for (String slabid : slabIdList) {
						
						ChargingDetails newChargingDetails = new ChargingDetails();
						
						newChargingDetails.setPaymentsRegion(AccountCurrencyRegion.DOMESTIC);
						newChargingDetails.setPaymentType(paymentType);
						newChargingDetails.setMopType(mopType);
						newChargingDetails.setAcquirerName(acquirerName);
						newChargingDetails.setPayId(payId);
						newChargingDetails.setStatus(TDRStatus.ACTIVE);
						newChargingDetails.setCurrency(accountCurrencyCode);
						newChargingDetails.setTransactionType(TransactionType.SALE);
						newChargingDetails.setCardHolderType(cardType);
						newChargingDetails.setAcquiringMode(onOff);
						
						
						String slabArray [] = slabid.split(",");
						newChargingDetails.setSlabId(slabArray[0]);
						newChargingDetails.setMinTxnAmount(Double.valueOf(slabArray[1]));
						newChargingDetails.setMaxTxnAmount(Double.valueOf(slabArray[2]));
						
						newChargingDetails.setAllowFixCharge(false);
						newChargingDetails.setFixChargeLimit(Double.valueOf(0.00));
						newChargingDetails.setBankFixCharge(Double.valueOf(0.00));
						newChargingDetails.setBankFixChargeAFC(Double.valueOf(0.00));
						newChargingDetails.setBankServiceTax(Double.valueOf(0.00));
						newChargingDetails.setBankTDR(Double.valueOf(0.00));
						newChargingDetails.setBankTDRAFC(Double.valueOf(0.00));
						newChargingDetails.setMerchantFixCharge(Double.valueOf(0.00));
						newChargingDetails.setMerchantFixChargeAFC(Double.valueOf(0.00));
						newChargingDetails.setMerchantServiceTax(Double.valueOf(0.00));
						newChargingDetails.setMerchantTDR(Double.valueOf(0.00));
						newChargingDetails.setMerchantTDRAFC(Double.valueOf(0.00));
						//newChargingDetails.setPgFixCharge(Double.valueOf(0.00));
						//newChargingDetails.setPgFixChargeAFC(Double.valueOf(0.00));
						//newChargingDetails.setPgServiceTax(Double.valueOf(0.00));
						//newChargingDetails.setPgTDR(Double.valueOf(0.00));
						//newChargingDetails.setPgTDRAFC(Double.valueOf(0.00));
						newChargingDetails.setCreatedDate(new Date());
					
						cdList.add(newChargingDetails);
					}
				}
			}
			
			
			List<onUsOffUs> acquiringModeListInt = new ArrayList<onUsOffUs>();
			acquiringModeListInt.add(onUsOffUs.OFF_US);
			
			List<CardHolderType> cardHolderTypeListInt = new ArrayList<CardHolderType>();
			cardHolderTypeListInt.add(CardHolderType.COMMERCIAL);
			
			
			// Create Charging Details List for International Cards
			
			for (onUsOffUs onOff : acquiringModeListInt) {
				for (CardHolderType cardType : cardHolderTypeListInt) {
					for (String slabId : slabIdList) {
					
						ChargingDetails newChargingDetails = new ChargingDetails();
						
						newChargingDetails.setPaymentsRegion(AccountCurrencyRegion.INTERNATIONAL);
						newChargingDetails.setPaymentType(paymentType);
						newChargingDetails.setMopType(mopType);
						newChargingDetails.setAcquirerName(acquirerName);
						newChargingDetails.setPayId(payId);
						newChargingDetails.setStatus(TDRStatus.ACTIVE);
						newChargingDetails.setCurrency(accountCurrencyCode);
						newChargingDetails.setTransactionType(TransactionType.SALE);
						newChargingDetails.setCardHolderType(cardType);
						newChargingDetails.setAcquiringMode(onOff);
						
						String slabArray [] = slabId.split(",");
						newChargingDetails.setSlabId(slabArray[0]);
						newChargingDetails.setMinTxnAmount(Double.valueOf(slabArray[1]));
						newChargingDetails.setMaxTxnAmount(Double.valueOf(slabArray[2]));
						
						newChargingDetails.setAllowFixCharge(false);
						newChargingDetails.setFixChargeLimit(Double.valueOf(0.00));
						newChargingDetails.setBankFixCharge(Double.valueOf(0.00));
						newChargingDetails.setBankFixChargeAFC(Double.valueOf(0.00));
						newChargingDetails.setBankServiceTax(Double.valueOf(0.00));
						newChargingDetails.setBankTDR(Double.valueOf(0.00));
						newChargingDetails.setBankTDRAFC(Double.valueOf(0.00));
						newChargingDetails.setMerchantFixCharge(Double.valueOf(0.00));
						newChargingDetails.setMerchantFixChargeAFC(Double.valueOf(0.00));
						newChargingDetails.setMerchantServiceTax(Double.valueOf(0.00));
						newChargingDetails.setMerchantTDR(Double.valueOf(0.00));
						newChargingDetails.setMerchantTDRAFC(Double.valueOf(0.00));
						//newChargingDetails.setPgFixCharge(Double.valueOf(0.00));
						//newChargingDetails.setPgFixChargeAFC(Double.valueOf(0.00));
						//newChargingDetails.setPgServiceTax(Double.valueOf(0.00));
						//newChargingDetails.setPgTDR(Double.valueOf(0.00));
						//newChargingDetails.setPgTDRAFC(Double.valueOf(0.00));
						newChargingDetails.setCreatedDate(new Date());
					
						cdList.add(newChargingDetails);
						
					}
					
				}
			}
			
			
		}
		
		else {
			
				for (String slabid : slabIdList) {
					
					ChargingDetails newChargingDetails = new ChargingDetails();
					
					newChargingDetails.setPaymentsRegion(AccountCurrencyRegion.DOMESTIC);
					newChargingDetails.setPaymentType(paymentType);
					newChargingDetails.setMopType(mopType);
					newChargingDetails.setAcquirerName(acquirerName);
					newChargingDetails.setPayId(payId);
					newChargingDetails.setStatus(TDRStatus.ACTIVE);
					newChargingDetails.setCurrency(accountCurrencyCode);
					newChargingDetails.setTransactionType(TransactionType.SALE);
					newChargingDetails.setCardHolderType(CardHolderType.CONSUMER);
					newChargingDetails.setAcquiringMode(onUsOffUs.OFF_US);
					
					
					String slabArray [] = slabid.split(",");
					newChargingDetails.setSlabId(slabArray[0]);
					newChargingDetails.setMinTxnAmount(Double.valueOf(slabArray[1]));
					newChargingDetails.setMaxTxnAmount(Double.valueOf(slabArray[2]));
					
					newChargingDetails.setAllowFixCharge(false);
					newChargingDetails.setFixChargeLimit(Double.valueOf(0.00));
					newChargingDetails.setBankFixCharge(Double.valueOf(0.00));
					newChargingDetails.setBankFixChargeAFC(Double.valueOf(0.00));
					newChargingDetails.setBankServiceTax(Double.valueOf(0.00));
					newChargingDetails.setBankTDR(Double.valueOf(0.00));
					newChargingDetails.setBankTDRAFC(Double.valueOf(0.00));
					newChargingDetails.setMerchantFixCharge(Double.valueOf(0.00));
					newChargingDetails.setMerchantFixChargeAFC(Double.valueOf(0.00));
					newChargingDetails.setMerchantServiceTax(Double.valueOf(0.00));
					newChargingDetails.setMerchantTDR(Double.valueOf(0.00));
					newChargingDetails.setMerchantTDRAFC(Double.valueOf(0.00));
					//newChargingDetails.setPgFixCharge(Double.valueOf(0.00));
					//newChargingDetails.setPgFixChargeAFC(Double.valueOf(0.00));
					//newChargingDetails.setPgServiceTax(Double.valueOf(0.00));
					//newChargingDetails.setPgTDR(Double.valueOf(0.00));
					//newChargingDetails.setPgTDRAFC(Double.valueOf(0.00));
					newChargingDetails.setCreatedDate(new Date());
				
					cdList.add(newChargingDetails);
				}
			
		}
		return cdList;
	}

	public void updateServiceTax(ChargingDetails oldChargingDetail, double newServiceTax) {
		Date currentDate = new Date();

		ChargingDetails newChargingDetail = SerializationUtils.clone(oldChargingDetail);
		// create new TDR
		//newChargingDetail.setPgServiceTax(newServiceTax);
		newChargingDetail.setBankServiceTax(newServiceTax);
		newChargingDetail.setMerchantServiceTax(newServiceTax);
		newChargingDetail.setStatus(TDRStatus.ACTIVE);
		newChargingDetail.setCreatedDate(currentDate);
		newChargingDetail.setId(null);
		// deactivate old TDR
		oldChargingDetail.setStatus(TDRStatus.INACTIVE);
		oldChargingDetail.setUpdatedDate(currentDate);
	}

	public ChargingDetails createChargingDetail(PaymentType paymentType, MopType mopType, String acquirerName,
			String payId, String accountCurrencyCode, onUsOffUs acquiringMode, CardHolderType cardHolderType,
			AccountCurrencyRegion paymentsRegion, String slabId) {
		ChargingDetails newChargingDetails = new ChargingDetails();

		newChargingDetails.setPaymentType(paymentType);
		newChargingDetails.setMopType(mopType);
		newChargingDetails.setAcquirerName(acquirerName);
		newChargingDetails.setPayId(payId);
		newChargingDetails.setStatus(TDRStatus.ACTIVE);
		newChargingDetails.setCurrency(accountCurrencyCode);
		newChargingDetails.setPaymentsRegion(paymentsRegion);
		newChargingDetails.setCardHolderType(cardHolderType);
		newChargingDetails.setAcquiringMode(acquiringMode);
		newChargingDetails.setSlabId(slabId);

		return newChargingDetails;
	}
}
