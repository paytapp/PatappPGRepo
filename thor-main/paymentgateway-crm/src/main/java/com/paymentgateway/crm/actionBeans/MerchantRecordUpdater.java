package com.paymentgateway.crm.actionBeans;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.MPADao;
import com.paymentgateway.commons.kms.EncryptDecryptService;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.NotificationEmailer;
import com.paymentgateway.commons.user.PendingUserApproval;
import com.paymentgateway.commons.user.PendingUserApprovalDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.PendingRequestEmailProcessor;
import com.paymentgateway.commons.util.TDRStatus;

/**
 * @author Rahul
 *
 */
@Service
public class MerchantRecordUpdater {

	@Autowired
	PendingUserApprovalDao pendingUserApprovalDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private MPADao mpaDao;

	@Autowired
	MerchantLogUpdater merchantLogUpdater;

	@Autowired
	EncryptDecryptService encryptDecryptService;

	@Autowired
	private PendingRequestEmailProcessor pendingRequestEmailProcessor;

	private NotificationEmailer notificationEmailerDB = null;
	private Set<Account> accountSetDB = null;
	private String actionMessage;
	User dbuser = null;
	private User userFromDB = null;
	private List<User> userList;

	public Map<String, User> updateUserPendingDetails(User userFE, User sessionUser, List<Account> newAccounts,
			List<AccountCurrency> accountCurrencyList, StringBuilder permissions) {
		User user = updateUserDetails(userFE, sessionUser, newAccounts, accountCurrencyList, permissions);
		Map<String, User> returnMap = new HashMap<String, User>();
		returnMap.put(actionMessage, user);
		return returnMap;
	}

	public User updateUserDetails(User userFE, User sessionUser, List<Account> newAccounts,
			List<AccountCurrency> accountCurrencyList, StringBuilder permissions) {
		Date date = new Date();
		userFromDB = userDao.findPayId(userFE.getPayId());
		User userDB = (User) SerializationUtils.clone(userFromDB);
		// Set details of user for edit
		userFromDB.setModeType(userFE.getModeType());
		userFromDB.setComments(userFE.getComments());
		userFromDB.setWhiteListIpAddress(userFE.getWhiteListIpAddress());
		userFromDB.setUserStatus(userFE.getUserStatus());

		userFromDB.setBusinessName(userFE.getBusinessName());
		userFromDB.setFirstName(userFE.getFirstName());
		userFromDB.setLastName(userFE.getLastName());
		userFromDB.setCompanyName(userFE.getCompanyName());
		userFromDB.setWebsite(userFE.getWebsite());
		userFromDB.setContactPerson(userFE.getContactPerson());
		userFromDB.setEmailId(userFE.getEmailId());
		userFromDB.setRegistrationDate(userFE.getRegistrationDate());
		Date alreadyActiveUser = userFromDB.getActivationDate();
		if (alreadyActiveUser != null) {
			userFromDB.setActivationDate(userFromDB.getActivationDate());
		} else {
			userFromDB.setActivationDate(userFE.getActivationDate());
		}
		userFromDB.setMerchantType(userFE.getMerchantType());
		userFromDB.setNoOfTransactions(userFE.getNoOfTransactions());
		userFromDB.setAmountOfTransactions(userFE.getAmountOfTransactions());
		
		// Getting 	Select Reseller from front End -- Quick Fix by Shaiwal
		if (StringUtils.isNotBlank(userFE.getResellerId()) && !userFE.getResellerId().equalsIgnoreCase("Select Reseller")) {
			userFromDB.setResellerId(userFE.getResellerId());
		}
		else {
			userFromDB.setResellerId("");
		}
	
		userFromDB.setProductDetail(userFE.getProductDetail());
		
		if (StringUtils.isNotBlank(userFE.getSuperMerchantId())) {
			userFromDB.setSuperMerchantId(userFE.getSuperMerchantId());
		}
		else {
			userFromDB.setSuperMerchantId(null);
		}
		
		userFromDB.setMobile(userFE.getMobile());
		userFromDB.setTransactionSmsFlag(userFE.isTransactionSmsFlag());
		userFromDB.setTelephoneNo(userFE.getTelephoneNo());
		userFromDB.setFax(userFE.getFax());
		userFromDB.setAddress(userFE.getAddress());
		userFromDB.setCity(userFE.getCity());
		userFromDB.setState(userFE.getState());
		userFromDB.setCountry(userFE.getCountry());
		userFromDB.setPostalCode(userFE.getPostalCode());

		userFromDB.setBankName(userFE.getBankName());
		userFromDB.setIfscCode(userFE.getIfscCode());
		userFromDB.setAccHolderName(userFE.getAccHolderName());
		userFromDB.setCurrency(userFE.getCurrency());
		userFromDB.setBranchName(userFE.getBranchName());
		userFromDB.setPanCard(userFE.getPanCard());
		userFromDB.setAccountNo(userFE.getAccountNo());

		userFromDB.setOrganisationType(userFE.getOrganisationType());
		userFromDB.setWebsite(userFE.getWebsite());
		userFromDB.setMultiCurrency(userFE.getMultiCurrency());
		userFromDB.setBusinessModel(userFE.getBusinessModel());
		userFromDB.setOperationAddress(userFE.getOperationAddress());
		userFromDB.setOperationState(userFE.getOperationState());
		userFromDB.setOperationCity(userFE.getOperationCity());
		userFromDB.setOperationPostalCode(userFE.getOperationPostalCode());
		userFromDB.setDateOfEstablishment(userFE.getDateOfEstablishment());

		userFromDB.setCin(userFE.getCin());
		userFromDB.setPan(userFE.getPan());
		userFromDB.setPanName(userFE.getPanName());
		userFromDB.setNoOfTransactions(userFE.getNoOfTransactions());
		userFromDB.setAmountOfTransactions(userFE.getAmountOfTransactions());
		userFromDB.setTransactionEmailerFlag(userFE.isTransactionEmailerFlag());
		userFromDB.setTransactionEmailId(userFE.getTransactionEmailId());
		userFromDB.setExpressPayFlag(userFE.isExpressPayFlag());
		userFromDB.setMerchantHostedFlag(userFE.isMerchantHostedFlag());
		userFromDB.setIframePaymentFlag(userFE.isIframePaymentFlag());
		userFromDB.setCheckOutJsFlag(userFE.isCheckOutJsFlag());
		userFromDB.setSurchargeFlag(userFE.isSurchargeFlag());
		userFromDB.setTransactionAuthenticationEmailFlag(userFE.isTransactionAuthenticationEmailFlag());
		userFromDB.setTransactionCustomerEmailFlag(userFE.isTransactionCustomerEmailFlag());
		userFromDB.setRefundTransactionCustomerEmailFlag(userFE.isRefundTransactionCustomerEmailFlag());
		userFromDB.setRefundTransactionMerchantEmailFlag(userFE.isRefundTransactionMerchantEmailFlag());
		userFromDB.setRetryTransactionCustomeFlag(userFE.isRetryTransactionCustomeFlag());
		userFromDB.setAttemptTrasacation(userFE.getAttemptTrasacation());
		userFromDB.setExtraRefundLimit(userFE.getExtraRefundLimit());
		userFromDB.setOneTimeRefundLimit(userFE.getOneTimeRefundLimit());
		userFromDB.setRefundLimitRemains(userFE.getRefundLimitRemains());
		userFromDB.setUpdateDate(date);
		userFromDB.setDefaultCurrency(userFE.getDefaultCurrency());
		userFromDB.setMCC(userFE.getMCC());
		userFromDB.setAmexSellerId(userFE.getAmexSellerId());
		userFromDB.setDefaultLanguage(userFE.getDefaultLanguage());
		userFromDB.setIndustryCategory(userFE.getIndustryCategory());
		userFromDB.setIndustrySubCategory(userFE.getIndustrySubCategory());
		userFromDB.setUpdatedBy(sessionUser.getEmailId());
		userFromDB.setTransactionSms(userFE.getTransactionSms());
		userFromDB.setSkipOrderIdForRefund(userFE.isSkipOrderIdForRefund());
		userFromDB.setAllowDuplicateOrderId(userFE.getAllowDuplicateOrderId());
		userFromDB.setPaymentMessageSlab(userFE.getPaymentMessageSlab());
		userFromDB.setCardSaveParam(userFE.getCardSaveParam());
		userFromDB.setNbSaveParam(userFE.getNbSaveParam());
		userFromDB.setWlSaveParam(userFE.getWlSaveParam());
		userFromDB.setSaveNBFlag(userFE.isSaveNBFlag());
		userFromDB.setSaveWLFlag(userFE.isSaveWLFlag());
		userFromDB.setAllowSaleDuplicate(userFE.isAllowSaleDuplicate());
		userFromDB.setAllowRefundDuplicate(userFE.isAllowRefundDuplicate());
		userFromDB.setAllowSaleInRefund(userFE.isAllowSaleInRefund());
		userFromDB.setAllowRefundInSale(userFE.isAllowRefundInSale());
		userFromDB.setAllowDuplicateNot(userFE.isAllowDuplicateNot());
		userFromDB.setTerminalId(userFE.getTerminalId());
		userFromDB.setVpaSaveParam(userFE.getVpaSaveParam());
		userFromDB.setSaveVPAFlag(userFE.isSaveVPAFlag());
		userFromDB.setAllowLogoInPgPage(userFE.isAllowLogoInPgPage());
		userFromDB.setLogoFlag(userFE.isLogoFlag());
		userFromDB.setRetailMerchantFlag(userFE.isRetailMerchantFlag());
		userFromDB.setCapturedMerchantFlag(userFE.isCapturedMerchantFlag());
		userFromDB.setLogoName(userFE.getLogoName());
		userFromDB.setCodName(userFE.getCodName());
		userFromDB.setBookingRecord(userFE.isBookingRecord());
		userFromDB.seteNachReportFlag(userFE.iseNachReportFlag());
		userFromDB.setUpiAutoPayReportFlag(userFE.isUpiAutoPayReportFlag());
		userFromDB.setCustomTransactionStatus(userFE.isCustomTransactionStatus());
		userFromDB.setAllowPartSettle(userFE.isAllowPartSettle());
		userFromDB.setDeviation(userFE.getDeviation());
		userFromDB.setAllowSubtractValue(userFE.isAllowSubtractValue());
		userFromDB.setDiscountingFlag(userFE.isDiscountingFlag());
		userFromDB.setEposMerchant(userFE.isEposMerchant());
		userFromDB.setSaveNBFlag(userFE.isSaveNBFlag());
		userFromDB.setSaveWLFlag(userFE.isSaveWLFlag());
		userFromDB.setNbSaveParam(userFE.getNbSaveParam());
		userFromDB.setWlSaveParam(userFE.getWlSaveParam());
		userFromDB.setTransactionSmsFlag(userFE.isTransactionSmsFlag());		
		userFromDB.setTransactionCustomerSMSFlag(userFE.isTransactionCustomerSMSFlag());
		userFromDB.setTransactionMerchantSMSFlag(userFE.isTransactionMerchantSMSFlag());
		userFromDB.setTransactionFailedMerchantSMSFlag(userFE.isTransactionFailedMerchantSMSFlag());
        userFromDB.setTransactionFailedCustomerSMSFlag(userFE.isTransactionFailedCustomerSMSFlag());
        userFromDB.setTransactionRefundMerchantSMSFlag(userFE.isTransactionRefundMerchantSMSFlag());
        userFromDB.setTransactionRefundCustomerSMSFlag(userFE.isTransactionRefundCustomerSMSFlag());
        userFromDB.setTransactionFailedMerchantEmailFlag(userFE.isTransactionFailedMerchantEmailFlag());
        userFromDB.setTransactionFailedCustomerEmailFlag(userFE.isTransactionFailedCustomerEmailFlag());
        userFromDB.setVendorPayOutFlag(userFE.isVendorPayOutFlag());
        userFromDB.setAccountVerificationFlag(userFE.isAccountVerificationFlag());
        userFromDB.setVpaVerificationFlag(userFE.isVpaVerificationFlag());
        userFromDB.setLoadWalletFlag(userFE.isLoadWalletFlag());
        userFromDB.setSmtMerchant(userFE.isSmtMerchant());
        userFromDB.setNodalReportFlag(userFE.isNodalReportFlag());
        userFromDB.setPaymentAdviceFlag(userFE.isPaymentAdviceFlag());
        userFromDB.setMerchantInitiatedDirectFlag(userFE.isMerchantInitiatedDirectFlag());
        userFromDB.setImpsFlag(userFE.isImpsFlag());
        userFromDB.setAllowQRScanFlag(userFE.isAllowQRScanFlag());
        userFromDB.setVirtualAccountFlag(userFE.isVirtualAccountFlag());
        userFromDB.setAllowUpiQRFlag(userFE.isAllowUpiQRFlag());
        userFromDB.setNetSettledFlag(userFE.isNetSettledFlag());
        userFromDB.setCustomHostedUrl(userFE.getCustomHostedUrl());
        userFromDB.setAllowCustomHostedUrl(userFE.isAllowCustomHostedUrl());
        userFromDB.setLyraPay(userFE.isLyraPay());
        userFromDB.setCustomerQrFlag(userFE.isCustomerQrFlag());
        userFromDB.setResellerMerchantSignupFlag(userFE.isResellerMerchantSignupFlag());
        userFromDB.setResellerUserStatusFlag(userFE.isResellerUserStatusFlag());    
        userFromDB.setNon3dsTxn(userFE.isNon3dsTxn());
        userFromDB.setBusinessName(userFE.getBusinessName());
        userFromDB.setAutoRefund(userFE.isAutoRefund());
        userFromDB.setCallBackFlag(userFE.isCallBackFlag());
        userFromDB.setCallBackUrl(userFE.getCallBackUrl());
        userFromDB.setStatementFlag(userFE.isStatementFlag());
        userFromDB.setTopupFlag(userFE.isTopupFlag());
        userFromDB.setAllCallBackFlag(userFE.isAllCallBackFlag());
        
        userFromDB.setAcceptPostSettledInEnquiry(userFE.isAcceptPostSettledInEnquiry());
        userFromDB.setAllowNodalPayoutFlag(userFE.isAllowNodalPayoutFlag());
        userFromDB.setAllowPayoutUpdateStatus(userFE.isAllowPayoutUpdateStatus());
        userFromDB.setAllowECollectionFee(userFE.isAllowECollectionFee());
        userFromDB.setWhiteListReturnUrl(userFE.getWhiteListReturnUrl());
        userFromDB.setWhiteListReturnUrlFlag(userFE.isWhiteListReturnUrlFlag());
		// Check Settlement Naming Convention & Refund Naming Convention values
//		List<User> userList = userDao.findAll();
//		if (userList != null) {
//			for (User user : userList) {
//
//				if (!user.getPayId().equals(userFE.getPayId())) {
//
//					if (StringUtils.isBlank(userFE.getSettlementNamingConvention())
//							|| StringUtils.isBlank(userFE.getRefundValidationNamingConvention())) {
//
//						if (StringUtils.isBlank(userFE.getSettlementNamingConvention().trim())
//								|| StringUtils.isBlank(userFE.getRefundValidationNamingConvention().trim())) {
//							setActionMessage(
//									CrmFieldConstants.SETTLEMENT_AND_REFUND_VALIDATION_NAMING_MESSAGE.getValue());
//							return userFromDB;
//						}
//					} else {
//
//						if (userFE.getSettlementNamingConvention().replaceAll(",", "").trim()
//								.equals(user.getSettlementNamingConvention())) {
//							setActionMessage(CrmFieldConstants.SETTLEMENT_NAMING_MESSAGE.getValue());
//							return userFromDB;
//						}
//						// if (!StringUtils.isEmpty(userFE.getRefundValidationNamingConvention())) {
//						// if
//						// (!StringUtils.isEmpty(userFE.getRefundValidationNamingConvention().trim())) {
//						if (userFE.getRefundValidationNamingConvention().replaceAll(",", "").trim()
//								.equals(user.getRefundValidationNamingConvention())) {
//							setActionMessage(CrmFieldConstants.REFUND_VALIDATION_NAMING_MESSAGE.getValue());
//							return userFromDB;
//						}
//					}
//				}
//			}
//		}
//
//		userFromDB.setSettlementNamingConvention(userFE.getSettlementNamingConvention().replaceAll(",", "").trim());
//		userFromDB.setRefundValidationNamingConvention(
//				userFE.getRefundValidationNamingConvention().replaceAll(",", "").trim());
		// Update account details
		// updateAccount(newAccounts, accountCurrencyList);

		if (sessionUser.getUserType().equals(UserType.ADMIN)
				|| permissions.toString().contains("Edit Merchant Details")) {
			userDao.update(userFromDB);
			merchantLogUpdater.updateValue(userFE, sessionUser, newAccounts, accountCurrencyList, userDB);

			if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				pendingRequestEmailProcessor.processEditMerchantDetailsEmail("Active", sessionUser.getEmailId(),
						sessionUser.getUserType().toString(), userFromDB.getEmailId());
			}

			setActionMessage(CrmFieldConstants.USER_DETAILS_UPDATED.getValue());
		} else if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			PendingUserApproval request = pendingUserApprovalDao.find(userFE.getPayId());
			if (request != null) {
				setActionMessage(CrmFieldConstants.PENDING_REQUEST_EXIST.getValue());
				return userFromDB;
			}

			PendingUserApproval newPendingUserApproval = createPendingApprovalFields(userFE, sessionUser);
			pendingUserApprovalDao.create(newPendingUserApproval);
			setActionMessage(CrmFieldConstants.DETAILS_UPDATE_REQUEST.getValue());
			return userFromDB;
		
		}
		else if ((sessionUser.getUserType().equals(UserType.MERCHANT))||(sessionUser.isSuperMerchant())) {
			userDao.update(userFromDB);
			merchantLogUpdater.updateValue(userFE, sessionUser, newAccounts, accountCurrencyList, userDB);

		if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			pendingRequestEmailProcessor.processEditMerchantDetailsEmail("Active", sessionUser.getEmailId(),
					sessionUser.getUserType().toString(), userFromDB.getEmailId());
		}

		setActionMessage(CrmFieldConstants.USER_DETAILS_UPDATED.getValue());
	}
		return userFromDB;
	}

	public MerchantProcessingApplication updateMPADetails(MerchantProcessingApplication MPAData) {
		MerchantProcessingApplication mpa = mpaDao.fetchMPADataByPayId(MPAData.getPayId());
		if (mpa != null) {
			mpa.setIndustryCategory(MPAData.getIndustryCategory());
			mpa.setTypeOfEntity(MPAData.getTypeOfEntity());
			mpa.setCompanyName(MPAData.getCompanyName());
			mpa.setRegistrationNumber(MPAData.getRegistrationNumber());
			mpa.setDateOfIncorporation(MPAData.getDateOfIncorporation());
			mpa.setCompanyEmailId(MPAData.getCompanyEmailId());
			mpa.setCompanyRegisteredAddress(MPAData.getCompanyRegisteredAddress());
			mpa.setTradingAddress1(MPAData.getTradingAddress1());
			mpa.setTradingCountry(MPAData.getTradingCountry());
			mpa.setTradingState(MPAData.getTradingState());
			mpa.setTradingPin(MPAData.getTradingPin());
			mpa.setBusinessPan(MPAData.getBusinessPan());
			mpa.setGstin(MPAData.getGstin());
			mpa.setCompanyPhone(MPAData.getCompanyPhone());
			mpa.setCompanyWebsite(MPAData.getCompanyWebsite());
			mpa.setBusinessEmailForCommunication(MPAData.getBusinessEmailForCommunication());
			mpa.setMpaFiles(MPAData.getMpaFiles());
			// --------
			mpa.setContactName(MPAData.getContactName());
			mpa.setContactMobile(MPAData.getContactMobile());
			mpa.setContactEmail(MPAData.getContactEmail());
			mpa.setContactLandline(MPAData.getContactLandline());

			mpa.setDirector1FullName(MPAData.getDirector1FullName());
			mpa.setDirector1Pan(MPAData.getDirector1Pan());
			mpa.setDirector1Email(MPAData.getDirector1Email());
			mpa.setDirector1Mobile(MPAData.getDirector1Mobile());
			mpa.setDirector1Landline(MPAData.getDirector1Landline());
			mpa.setDirector1Address(MPAData.getDirector1Address());

			mpa.setDirector2FullName(MPAData.getDirector2FullName());
			mpa.setDirector2Pan(MPAData.getDirector2Pan());
			mpa.setDirector2Mobile(MPAData.getDirector2Mobile());
			mpa.setDirector2Email(MPAData.getDirector2Email());
			mpa.setDirector2Landline(MPAData.getDirector2Landline());
			mpa.setDirector2Address(MPAData.getDirector2Address());
			// --------
			mpa.setAccountNumber(MPAData.getAccountNumber());
			mpa.setAccountHolderName(MPAData.getAccountHolderName());
			mpa.setAccountIfsc(MPAData.getAccountIfsc());
			mpa.setAccountMobileNumber(MPAData.getAccountMobileNumber());
			// --------
			mpa.setAnnualTurnover(MPAData.getAnnualTurnover());
			mpa.setAnnualTurnoverOnline(MPAData.getAnnualTurnoverOnline());
			mpa.setPercentageCC(MPAData.getPercentageCC());
			mpa.setPercentageDC(MPAData.getPercentageDC());
			mpa.setPercentageNB(MPAData.getPercentageNB());
			mpa.setPercentageUP(MPAData.getPercentageUP());
			mpa.setPercentageWL(MPAData.getPercentageWL());
			mpa.setPercentageEM(MPAData.getPercentageEM());
			mpa.setPercentageCD(MPAData.getPercentageCD());
			mpa.setPercentageNeftOrImpsOrRtgs(MPAData.getPercentageNeftOrImpsOrRtgs());
			mpa.setPercentageDomestic(MPAData.getPercentageDomestic());
			mpa.setPercentageInternational(MPAData.getPercentageInternational());
			// --------
			mpa.setMerchantType(MPAData.getMerchantType());
			mpa.setSurcharge(MPAData.getSurcharge());
			mpa.setIntegrationType(MPAData.getIntegrationType());
			mpa.setCustomizedInvoiceDesign(MPAData.getCustomizedInvoiceDesign());
			mpa.setInternationalCards(MPAData.getInternationalCards());
			mpa.setExpressPay(MPAData.getExpressPay());
			mpa.setExpressPayParameter(MPAData.getExpressPayParameter());
			mpa.setAllowDuplicateSaleOrderId(MPAData.getAllowDuplicateSaleOrderId());
			mpa.setAllowDuplicateRefundOrderId(MPAData.getAllowDuplicateRefundOrderId());
			mpa.setAllowDuplicateSaleOrderIdInRefund(MPAData.getAllowDuplicateSaleOrderIdInRefund());
			mpa.setAllowDuplicateRefundOrderIdSale(MPAData.getAllowDuplicateRefundOrderIdSale());
			mpa.setAllowDuplicateNotSaleOrderId(MPAData.isAllowDuplicateNotSaleOrderId());
			// ---------
			mpa.setTechnicalContactName(MPAData.getTechnicalContactName());
			mpa.setTechnicalContactEmail(MPAData.getTechnicalContactEmail());
			mpa.setTechnicalContactLandline(MPAData.getTechnicalContactLandline());
			mpa.setTechnicalContactMobile(MPAData.getTechnicalContactMobile());

			mpa.setServerDetails(MPAData.getServerDetails());
			mpa.setServerCompanyName(MPAData.getServerCompanyName());
			mpa.setServerCompanyAddress(MPAData.getServerCompanyAddress());
			mpa.setServerCompanyMobile(MPAData.getServerCompanyMobile());
			mpa.setServerCompanyLandline(MPAData.getServerCompanyLandline());

			mpa.setOperatingSystem(MPAData.getOperatingSystem());
			mpa.setBackendTechnology(MPAData.getBackendTechnology());
			mpa.setApplicationServerTechnology(MPAData.getApplicationServerTechnology());
			mpa.setProductionServerIp(MPAData.getProductionServerIp());
			// ---------
			mpa.setThirdPartyForCardData(MPAData.getThirdPartyForCardData());
			mpa.setRefundsAllowed(MPAData.getRefundsAllowed());

			mpa.setEsignAadhaarType(MPAData.getEsignAadhaarType());
			mpa.setEsignCountry(MPAData.getEsignCountry());
			mpa.setEsignGender(MPAData.getEsignGender());
			mpa.setEsignName(MPAData.getEsignName());
			mpa.setEsignPincode(MPAData.getEsignPincode());
			mpa.setEsignState(MPAData.getEsignState());
			mpa.setEsignUidLastFourDigits(MPAData.getEsignUidLastFourDigits());
			mpa.setEsignYOB(MPAData.getEsignYOB());
			mpa.setTransactionCustomerEmailFlag(MPAData.isTransactionCustomerEmailFlag());
			mpa.setTransactionSmsFlag(MPAData.isTransactionSmsFlag());
			mpa.setTransactionCustomerSMSFlag(MPAData.isTransactionCustomerSMSFlag());
			mpa.setTransactionMerchantSMSFlag(MPAData.isTransactionMerchantSMSFlag());
			mpa.setMerchantSupportEmailId(MPAData.getMerchantSupportEmailId());
			mpa.setMerchantSupportMobileNumber(MPAData.getMerchantSupportMobileNumber());
			mpa.setMerchantSupportLandLine(MPAData.getMerchantSupportLandLine());
			mpa.setExtraRefundLimit(MPAData.getExtraRefundLimit());
			mpa.setOneTimeRefundLimit(MPAData.getOneTimeRefundLimit());
			mpa.setRefundLimitRemains(MPAData.getRefundLimitRemains());
			mpa.setRegistrationDate(MPAData.getRegistrationDate());
			mpa.setCin(MPAData.getCin());
			mpa.setSkipOrderIdForRefund(MPAData.isSkipOrderIdForRefund());
			mpa.setUserStatus(MPAData.getUserStatus());
			mpa.setModeType(MPAData.getModeType());
			mpa.setComments(MPAData.getComments());
			mpa.setTerminalId(MPAData.getTerminalId());
			mpa.setSuperMerchantId(MPAData.getSuperMerchantId());
			mpa.setRefundTransactionCustomerEmailFlag(MPAData.isRefundTransactionCustomerEmailFlag());
			mpa.setTransactionEmailerFlag(MPAData.isTransactionEmailerFlag());
			mpa.setRefundTransactionMerchantEmailFlag(MPAData.isRefundTransactionMerchantEmailFlag());
			mpa.setTransactionEmailId(MPAData.getTransactionEmailId());
			mpa.setTransactionSms(MPAData.getTransactionSms());
			mpa.setRetryTransactionCustomeFlag(MPAData.isRetryTransactionCustomeFlag());
			mpa.setSaveVPAFlag(MPAData.isSaveVPAFlag());
			mpa.setVpaSaveParam(MPAData.getVpaSaveParam());
			mpa.setSaveNBFlag(MPAData.isSaveNBFlag());
			mpa.setNbSaveParam(MPAData.getNbSaveParam());
			mpa.setSaveWLFlag(MPAData.isSaveWLFlag());
			mpa.setWlSaveParam(MPAData.getWlSaveParam());
			mpa.setBookingRecord(MPAData.isBookingRecord());
			mpa.seteNachReportFlag(MPAData.iseNachReportFlag());
			if(!MPAData.iseNachReportFlag()) {
				List<Merchants> subMerchantList = userDao.getSubMerchantListBySuperPayId(MPAData.getSuperMerchantId());
				for(Merchants subMerchant : subMerchantList) {
					userDao.updateSubMerchanteNachFlag(subMerchant.getPayId(), MPAData.iseNachReportFlag());
				}
			}
			
			mpa.setUpiAutoPayReportFlag(MPAData.isUpiAutoPayReportFlag());
			if(!MPAData.isUpiAutoPayReportFlag()) {
				List<Merchants> subMerchantList = userDao.getSubMerchantListBySuperPayId(MPAData.getSuperMerchantId());
				for(Merchants subMerchant : subMerchantList) {
					userDao.updateSubMerchantUpiAutoPayFlag(subMerchant.getPayId(), MPAData.isUpiAutoPayReportFlag());
				}
			}
			
			mpa.setAcceptPostSettledInEnquiry(MPAData.isAcceptPostSettledInEnquiry());
			if(!MPAData.isAcceptPostSettledInEnquiry()) {
				List<Merchants> subMerchantList = userDao.getSubMerchantListBySuperPayId(MPAData.getSuperMerchantId());
				for(Merchants subMerchant : subMerchantList) {
					userDao.updateSubMerchantAcceptPostSettledInEnquiryFlag(subMerchant.getPayId(), MPAData.isAcceptPostSettledInEnquiry());
				}
			}
			
			mpa.setCustomTransactionStatus(MPAData.isCustomTransactionStatus());
			mpa.setLogoFlag(MPAData.isLogoFlag());
			mpa.setLogoName(MPAData.getLogoName());
			mpa.setCodName(MPAData.getCodName());
			mpa.setAllowLogoInPgPage(MPAData.isAllowLogoInPgPage());
			mpa.setAllowPartSettle(MPAData.isAllowPartSettle());
			mpa.setAllowSubtractValue(MPAData.isAllowSubtractValue());
			mpa.setExtraRefundAmount(MPAData.isExtraRefundAmount());
			mpa.setOneTimeRefundAmount(MPAData.isOneTimeRefundAmount());
			mpa.setAmexSellerId(MPAData.getAmexSellerId());
			mpa.setmCC(MPAData.getmCC());
			mpa.setVirtualAccountNo(MPAData.getVirtualAccountNo());
			mpa.setPaymentCycle(MPAData.getPaymentCycle());
			mpa.setTransactionFailedMerchantSMSFlag(MPAData.isTransactionFailedMerchantSMSFlag());
            mpa.setTransactionFailedCustomerSMSFlag(MPAData.isTransactionFailedCustomerSMSFlag());
            mpa.setTransactionRefundMerchantSMSFlag(MPAData.isTransactionRefundMerchantSMSFlag());
            mpa.setTransactionRefundCustomerSMSFlag(MPAData.isTransactionRefundCustomerSMSFlag());
            mpa.setTransactionFailedMerchantEmailFlag(MPAData.isTransactionFailedMerchantEmailFlag());
            mpa.setTransactionFailedCustomerEmailFlag(MPAData.isTransactionFailedCustomerEmailFlag());
            mpa.setAccountVerificationFlag(MPAData.isAccountVerificationFlag());
            mpa.setNodalReportFlag(MPAData.isNodalReportFlag());
            mpa.setSmtMerchant(MPAData.isSmtMerchant());
            mpa.setPaymentAdviceFlag(MPAData.isPaymentAdviceFlag());
            mpa.setEposMerchant(MPAData.isEposMerchant());
            mpa.setRetailMerchantFlag(MPAData.isRetailMerchantFlag());
            mpa.setCapturedMerchantFlag(MPAData.isCapturedMerchantFlag());          
            mpa.setMerchantInitiatedDirectFlag(MPAData.isMerchantInitiatedDirectFlag());
            mpa.setImpsFlag(MPAData.isImpsFlag());
            mpa.setVendorPayOutFlag(MPAData.isVendorPayOutFlag());            
            mpa.setLoadWalletFlag(MPAData.isLoadWalletFlag());
            mpa.setCheckOutJsFlag(MPAData.isCheckOutJsFlag());
            mpa.setVirtualAccountFlag(MPAData.isVirtualAccountFlag());
            mpa.setVpaVerificationFlag(MPAData.isVpaVerificationFlag());
            mpa.setCustomerQrFlag(MPAData.isCustomerQrFlag());
            mpa.setResellerMerchantSignupFlag(MPAData.isResellerMerchantSignupFlag());
            mpa.setResellerUserStatusFlag(MPAData.isResellerUserStatusFlag());
            mpa.setNon3dsTxn(MPAData.isNon3dsTxn());
            mpa.setBusinessName(MPAData.getBusinessName());
            mpa.setAutoRefund(MPAData.isAutoRefund());
            mpa.setCallBackFlag(MPAData.isCallBackFlag());
            mpa.setCallBackUrl(MPAData.getCallBackUrl());
            mpa.setStatementFlag(MPAData.isStatementFlag());
            mpa.setTopupFlag(MPAData.isTopupFlag());
            mpa.setAllowNodalPayoutFlag(MPAData.isAllowNodalPayoutFlag());
            mpa.setAllowPayoutUpdateStatus(MPAData.isAllowPayoutUpdateStatus());
            mpa.setAllowECollectionFee(MPAData.isAllowECollectionFee());
            mpaDao.update(mpa);
		} else {
			mpa = new MerchantProcessingApplication();

			mpa.setPayId(MPAData.getPayId());
			mpa.setIndustryCategory(MPAData.getIndustryCategory());
			mpa.setTypeOfEntity(MPAData.getTypeOfEntity());
			mpa.setCompanyName(MPAData.getCompanyName());
			mpa.setRegistrationNumber(MPAData.getRegistrationNumber());
			mpa.setDateOfIncorporation(MPAData.getDateOfIncorporation());
			mpa.setCompanyEmailId(MPAData.getCompanyEmailId());
			mpa.setCompanyRegisteredAddress(MPAData.getCompanyRegisteredAddress());
			mpa.setTradingAddress1(MPAData.getTradingAddress1());
			mpa.setTradingCountry(MPAData.getTradingCountry());
			mpa.setTradingState(MPAData.getTradingState());
			mpa.setTradingPin(MPAData.getTradingPin());
			mpa.setBusinessPan(MPAData.getBusinessPan());
			mpa.setGstin(MPAData.getGstin());
			mpa.setCompanyPhone(MPAData.getCompanyPhone());
			mpa.setCompanyWebsite(MPAData.getCompanyWebsite());
			mpa.setBusinessEmailForCommunication(MPAData.getBusinessEmailForCommunication());
			mpa.setMpaFiles(MPAData.getMpaFiles());
			// --------
			mpa.setContactName(MPAData.getContactName());
			mpa.setContactMobile(MPAData.getContactMobile());
			mpa.setContactEmail(MPAData.getContactEmail());
			mpa.setContactLandline(MPAData.getContactLandline());

			mpa.setDirector1FullName(MPAData.getDirector1FullName());
			mpa.setDirector1Pan(MPAData.getDirector1Pan());
			mpa.setDirector1Email(MPAData.getDirector1Email());
			mpa.setDirector1Mobile(MPAData.getDirector1Mobile());
			mpa.setDirector1Landline(MPAData.getDirector1Landline());
			mpa.setDirector1Address(MPAData.getDirector1Address());

			mpa.setDirector2FullName(MPAData.getDirector2FullName());
			mpa.setDirector2Pan(MPAData.getDirector2Pan());
			mpa.setDirector2Mobile(MPAData.getDirector2Mobile());
			mpa.setDirector2Email(MPAData.getDirector2Email());
			mpa.setDirector2Landline(MPAData.getDirector2Landline());
			mpa.setDirector2Address(MPAData.getDirector2Address());
			// --------
			mpa.setAccountNumber(MPAData.getAccountNumber());
			mpa.setAccountHolderName(MPAData.getAccountHolderName());
			mpa.setAccountIfsc(MPAData.getAccountIfsc());
			mpa.setAccountMobileNumber(MPAData.getAccountMobileNumber());
			// --------
			mpa.setAnnualTurnover(MPAData.getAnnualTurnover());
			mpa.setAnnualTurnoverOnline(MPAData.getAnnualTurnoverOnline());
			mpa.setPercentageCC(MPAData.getPercentageCC());
			mpa.setPercentageDC(MPAData.getPercentageDC());
			mpa.setPercentageNB(MPAData.getPercentageNB());
			mpa.setPercentageUP(MPAData.getPercentageUP());
			mpa.setPercentageWL(MPAData.getPercentageWL());
			mpa.setPercentageEM(MPAData.getPercentageEM());
			mpa.setPercentageCD(MPAData.getPercentageCD());
			mpa.setPercentageNeftOrImpsOrRtgs(MPAData.getPercentageNeftOrImpsOrRtgs());
			mpa.setPercentageDomestic(MPAData.getPercentageDomestic());
			mpa.setPercentageInternational(MPAData.getPercentageInternational());
			// --------
			mpa.setMerchantType(MPAData.getMerchantType());
			mpa.setSurcharge(MPAData.getSurcharge());
			mpa.setIntegrationType(MPAData.getIntegrationType());
			mpa.setCustomizedInvoiceDesign(MPAData.getCustomizedInvoiceDesign());
			mpa.setInternationalCards(MPAData.getInternationalCards());
			mpa.setExpressPay(MPAData.getExpressPay());
			mpa.setExpressPayParameter(MPAData.getExpressPayParameter());
			mpa.setAllowDuplicateSaleOrderId(MPAData.getAllowDuplicateSaleOrderId());
			mpa.setAllowDuplicateRefundOrderId(MPAData.getAllowDuplicateRefundOrderId());
			mpa.setAllowDuplicateSaleOrderIdInRefund(MPAData.getAllowDuplicateSaleOrderIdInRefund());
			mpa.setAllowDuplicateRefundOrderIdSale(MPAData.getAllowDuplicateRefundOrderIdSale());
			mpa.setAllowDuplicateNotSaleOrderId(MPAData.isAllowDuplicateNotSaleOrderId());
			// ---------
			mpa.setTechnicalContactName(MPAData.getTechnicalContactName());
			mpa.setTechnicalContactEmail(MPAData.getTechnicalContactEmail());
			mpa.setTechnicalContactLandline(MPAData.getTechnicalContactLandline());
			mpa.setTechnicalContactMobile(MPAData.getTechnicalContactMobile());

			mpa.setServerDetails(MPAData.getServerDetails());
			mpa.setServerCompanyName(MPAData.getServerCompanyName());
			mpa.setServerCompanyAddress(MPAData.getServerCompanyAddress());
			mpa.setServerCompanyMobile(MPAData.getServerCompanyMobile());
			mpa.setServerCompanyLandline(MPAData.getServerCompanyLandline());

			mpa.setOperatingSystem(MPAData.getOperatingSystem());
			mpa.setBackendTechnology(MPAData.getBackendTechnology());
			mpa.setApplicationServerTechnology(MPAData.getApplicationServerTechnology());
			mpa.setProductionServerIp(MPAData.getProductionServerIp());
			// ---------
			mpa.setThirdPartyForCardData(MPAData.getThirdPartyForCardData());
			mpa.setRefundsAllowed(MPAData.getRefundsAllowed());

			mpa.setEsignAadhaarType(MPAData.getEsignAadhaarType());
			mpa.setEsignCountry(MPAData.getEsignCountry());
			mpa.setEsignGender(MPAData.getEsignGender());
			mpa.setEsignName(MPAData.getEsignName());
			mpa.setEsignPincode(MPAData.getEsignPincode());
			mpa.setEsignState(MPAData.getEsignState());
			mpa.setEsignUidLastFourDigits(MPAData.getEsignUidLastFourDigits());
			mpa.setEsignYOB(MPAData.getEsignYOB());
			mpa.setTransactionCustomerEmailFlag(MPAData.isTransactionCustomerEmailFlag());
			mpa.setTransactionSmsFlag(MPAData.isTransactionSmsFlag());
			mpa.setTransactionCustomerSMSFlag(MPAData.isTransactionCustomerSMSFlag());
			mpa.setTransactionMerchantSMSFlag(MPAData.isTransactionMerchantSMSFlag());
			mpa.setMerchantSupportEmailId(MPAData.getMerchantSupportEmailId());
			mpa.setMerchantSupportMobileNumber(MPAData.getMerchantSupportMobileNumber());
			mpa.setMerchantSupportLandLine(MPAData.getMerchantSupportLandLine());
			mpa.setExtraRefundLimit(MPAData.getExtraRefundLimit());
			mpa.setOneTimeRefundLimit(MPAData.getOneTimeRefundLimit());
			mpa.setRefundLimitRemains(MPAData.getRefundLimitRemains());
			mpa.setRegistrationDate(MPAData.getRegistrationDate());
			mpa.setSurchargeFlag(MPAData.isSurchargeFlag());
			mpa.setCin(MPAData.getCin());
			mpa.setContactName(MPAData.getContactName());
			mpa.setContactMobile(MPAData.getContactMobile());
			mpa.setVirtualAccountNo(MPAData.getVirtualAccountNo());
			mpa.setSkipOrderIdForRefund(MPAData.isSkipOrderIdForRefund());
			mpa.setUserStatus(MPAData.getUserStatus());
			mpa.setModeType(MPAData.getModeType());
			mpa.setComments(MPAData.getComments());
			mpa.setTerminalId(MPAData.getTerminalId());
			mpa.setSuperMerchantId(MPAData.getSuperMerchantId());
			mpa.setRefundTransactionCustomerEmailFlag(MPAData.isRefundTransactionCustomerEmailFlag());
			mpa.setTransactionEmailerFlag(MPAData.isTransactionEmailerFlag());
			mpa.setRefundTransactionMerchantEmailFlag(MPAData.isRefundTransactionMerchantEmailFlag());
			mpa.setTransactionEmailId(MPAData.getTransactionEmailId());
			mpa.setTransactionSms(MPAData.getTransactionSms());
			mpa.setRetryTransactionCustomeFlag(MPAData.isRetryTransactionCustomeFlag());
			mpa.setSaveVPAFlag(MPAData.isSaveVPAFlag());
			mpa.setVpaSaveParam(MPAData.getVpaSaveParam());
			mpa.setSaveNBFlag(MPAData.isSaveNBFlag());
			mpa.setNbSaveParam(MPAData.getNbSaveParam());
			mpa.setSaveWLFlag(MPAData.isSaveWLFlag());
			mpa.setWlSaveParam(MPAData.getWlSaveParam());
			mpa.setBookingRecord(MPAData.isBookingRecord());
			mpa.seteNachReportFlag(MPAData.iseNachReportFlag());
			mpa.setCustomTransactionStatus(MPAData.isCustomTransactionStatus());
			mpa.setLogoFlag(MPAData.isLogoFlag());
			mpa.setLogoName(MPAData.getLogoName());
			mpa.setCodName(MPAData.getCodName());
			mpa.setAllowLogoInPgPage(MPAData.isAllowLogoInPgPage());
			mpa.setAllowPartSettle(MPAData.isAllowPartSettle());
			mpa.setAllowSubtractValue(MPAData.isAllowSubtractValue());
			mpa.setExtraRefundAmount(MPAData.isExtraRefundAmount());
			mpa.setOneTimeRefundAmount(MPAData.isOneTimeRefundAmount());
			mpa.setAmexSellerId(MPAData.getAmexSellerId());
			mpa.setmCC(MPAData.getmCC());
			mpa.setVirtualAccountNo(MPAData.getVirtualAccountNo());
			mpa.setPaymentCycle(MPAData.getPaymentCycle());
			mpa.setTransactionFailedMerchantSMSFlag(MPAData.isTransactionFailedMerchantSMSFlag());
	        mpa.setTransactionFailedCustomerSMSFlag(MPAData.isTransactionFailedCustomerSMSFlag());
	        mpa.setTransactionRefundMerchantSMSFlag(MPAData.isTransactionRefundMerchantSMSFlag());
	        mpa.setTransactionRefundCustomerSMSFlag(MPAData.isTransactionRefundCustomerSMSFlag());
	        mpa.setTransactionFailedMerchantEmailFlag(MPAData.isTransactionFailedMerchantEmailFlag());
	        mpa.setTransactionFailedCustomerEmailFlag(MPAData.isTransactionFailedCustomerEmailFlag());
	        mpa.setSmtMerchant(MPAData.isSmtMerchant());
            mpa.setEposMerchant(MPAData.isEposMerchant());
            mpa.setRetailMerchantFlag(MPAData.isRetailMerchantFlag());
            mpa.setCapturedMerchantFlag(MPAData.isCapturedMerchantFlag());
            mpa.setLoadWalletFlag(MPAData.isLoadWalletFlag());
            mpa.setCheckOutJsFlag(MPAData.isCheckOutJsFlag());
            mpa.setMerchantInitiatedDirectFlag(MPAData.isMerchantInitiatedDirectFlag());
            mpa.setImpsFlag(MPAData.isImpsFlag());
            mpa.setVendorPayOutFlag(MPAData.isVendorPayOutFlag());
            mpa.setVirtualAccountFlag(MPAData.isVirtualAccountFlag());
			mpaDao.create(mpa);

		}
		return mpa;
	}

	public PendingUserApproval createPendingApprovalFields(User userFE, User sessionUser) {
		Date date = new Date();
		User dbuser = userDao.findPayId(userFE.getPayId());

		// Set details of user for edit
		PendingUserApproval pua = new PendingUserApproval();
		pua.setModeType(userFE.getModeType());
		pua.setComments(userFE.getComments());
		pua.setWhiteListIpAddress(userFE.getWhiteListIpAddress());
		pua.setUserStatus(userFE.getUserStatus());

		pua.setBusinessName(userFE.getBusinessName());
		pua.setFirstName(userFE.getFirstName());
		pua.setLastName(userFE.getLastName());
		pua.setCompanyName(userFE.getCompanyName());
		pua.setWebsite(userFE.getWebsite());
		pua.setContactPerson(userFE.getContactPerson());
		pua.setEmailId(userFE.getEmailId());
		pua.setRegistrationDate(userFE.getRegistrationDate());

		pua.setMerchantType(userFE.getMerchantType());
		pua.setNoOfTransactions(userFE.getNoOfTransactions());
		
		// Getting 	Select Reseller from front End -- Quick Fix by Shaiwal
		if (StringUtils.isNotBlank(userFE.getResellerId()) && !userFE.getResellerId().equalsIgnoreCase("Select Reseller")) {
			pua.setResellerId(userFE.getResellerId());
		}
		else {
			pua.setResellerId("");
		}
		
		pua.setProductDetail(userFE.getProductDetail());

		pua.setMobile(userFE.getMobile());
		pua.setTransactionSmsFlag(userFE.isTransactionSmsFlag());
		pua.setTelephoneNo(userFE.getTelephoneNo());
		pua.setFax(userFE.getFax());
		pua.setAddress(userFE.getAddress());
		pua.setCity(userFE.getCity());
		pua.setState(userFE.getState());
		pua.setCountry(userFE.getCountry());
		pua.setPostalCode(userFE.getPostalCode());

		pua.setBankName(userFE.getBankName());
		pua.setIfscCode(userFE.getIfscCode());
		pua.setAccHolderName(userFE.getAccHolderName());
		pua.setCurrency(userFE.getCurrency());
		pua.setBranchName(userFE.getBranchName());
		pua.setPanCard(userFE.getPanCard());
		pua.setAccountNo(userFE.getAccountNo());

		pua.setOrganisationType(userFE.getOrganisationType());
		pua.setWebsite(userFE.getWebsite());
		pua.setMultiCurrency(userFE.getMultiCurrency());
		pua.setBusinessModel(userFE.getBusinessModel());
		pua.setOperationAddress(userFE.getOperationAddress());
		pua.setOperationState(userFE.getOperationState());
		pua.setOperationCity(userFE.getOperationCity());
		pua.setOperationPostalCode(userFE.getOperationPostalCode());
		pua.setDateOfEstablishment(userFE.getDateOfEstablishment());

		pua.setCin(userFE.getCin());
		pua.setPan(userFE.getPan());
		pua.setPanName(userFE.getPanName());
		pua.setNoOfTransactions(userFE.getNoOfTransactions());
		pua.setAmountOfTransactions(userFE.getAmountOfTransactions());
		pua.setTransactionEmailerFlag(userFE.isTransactionEmailerFlag());
		pua.setTransactionEmailId(userFE.getTransactionEmailId());
		pua.setExpressPayFlag(userFE.isExpressPayFlag());
		pua.setMerchantHostedFlag(userFE.isMerchantHostedFlag());
		pua.setIframePaymentFlag(userFE.isIframePaymentFlag());
		pua.setSurchargeFlag(userFE.isSurchargeFlag());
		pua.setTransactionAuthenticationEmailFlag(userFE.isTransactionAuthenticationEmailFlag());
		pua.setTransactionCustomerEmailFlag(userFE.isTransactionCustomerEmailFlag());
		pua.setRefundTransactionCustomerEmailFlag(userFE.isRefundTransactionCustomerEmailFlag());
		pua.setRefundTransactionMerchantEmailFlag(userFE.isRefundTransactionMerchantEmailFlag());
		pua.setRetryTransactionCustomeFlag(userFE.isRetryTransactionCustomeFlag());
		pua.setRefundValidationNamingConvention(userFE.getRefundValidationNamingConvention());
		pua.setAttemptTrasacation(userFE.getAttemptTrasacation());
		pua.setExtraRefundLimit(userFE.getExtraRefundLimit());
		pua.setOneTimeRefundLimit(userFE.getOneTimeRefundLimit());
		pua.setRefundLimitRemains(userFE.getRefundLimitRemains());
		pua.setUpdateDate(date);
		pua.setDefaultCurrency(userFE.getDefaultCurrency());
		pua.setMCC(userFE.getMCC());
		pua.setAmexSellerId(userFE.getAmexSellerId());
		pua.setDefaultLanguage(userFE.getDefaultLanguage());
		pua.setIndustryCategory(userFE.getIndustryCategory());
		pua.setIndustrySubCategory(userFE.getIndustrySubCategory());
		pua.setRequestedBy(sessionUser.getEmailId());
		pua.setPayId(userFE.getPayId());
		pua.setRequestStatus(TDRStatus.PENDING.toString());
		pua.setTransactionSms(userFE.getTransactionSms());
		pua.setAllowDuplicateOrderId(userFE.getAllowDuplicateOrderId());
		pua.setTransactionSms(userFE.getTransactionSms());
		pua.setCardSaveParam(userFE.getCardSaveParam());
		pua.setAllowSaleDuplicate(userFE.isAllowSaleDuplicate());
		pua.setAllowRefundDuplicate(userFE.isAllowRefundDuplicate());
		pua.setAllowSaleInRefund(userFE.isAllowSaleInRefund());
		pua.setAllowRefundInSale(userFE.isAllowRefundInSale());
		pua.setTerminalId(userFE.getTerminalId());
		pua.setVpaSaveParam(userFE.getVpaSaveParam());
		pua.setSaveVPAFlag(userFE.isSaveVPAFlag());
		pua.setNbSaveParam(userFE.getNbSaveParam());
		pua.setWlSaveParam(userFE.getWlSaveParam());
		pua.setSaveNBFlag(userFE.isSaveNBFlag());
		pua.setSaveWLFlag(userFE.isSaveWLFlag());
		pua.setLogoFlag(userFE.isLogoFlag());
		pua.setLogoName(userFE.getLogoName());
		pua.setCodName(userFE.getCodName());
		pua.setBookingRecord(userFE.isBookingRecord());
		pua.setCustomTransactionStatus(userFE.isCustomTransactionStatus());
		pua.setSettlementNamingConvention(userFE.getSettlementNamingConvention());
		pua.setDiscountingFlag(userFE.isDiscountingFlag());
		pua.setTransactionCustomerSMSFlag(userFE.isTransactionCustomerSMSFlag());
		pua.setTransactionSmsFlag(userFE.isTransactionSmsFlag());
		return pua;

	}

	// merchant end code
	// update profile from user end when merchant is nor activated
	public User updateUserProfile(User userFE) {

		userFromDB = userDao.findPayId(userFE.getPayId());

		// Set details of user for edit
		userFromDB.setBusinessName(userFE.getBusinessName());
		userFromDB.setFirstName(userFE.getFirstName());
		userFromDB.setLastName(userFE.getLastName());
		userFromDB.setEmailId(userFE.getEmailId());
		userFromDB.setCompanyName(userFE.getCompanyName());
		userFromDB.setTelephoneNo(userFE.getTelephoneNo());
		userFromDB.setAddress(userFE.getAddress());
		userFromDB.setCity(userFE.getCity());
		userFromDB.setState(userFE.getState());
		userFromDB.setCountry(userFE.getCountry());
		userFromDB.setPostalCode(userFE.getPostalCode());

		userFromDB.setOrganisationType(userFE.getOrganisationType());
		userFromDB.setWebsite(userFE.getWebsite());
		userFromDB.setMultiCurrency(userFE.getMultiCurrency());
		userFromDB.setBusinessModel(userFE.getBusinessModel());

		userFromDB.setAddress(userFE.getAddress());
		userFromDB.setState(userFE.getState());
		userFromDB.setCity(userFE.getCity());
		userFromDB.setPostalCode(userFE.getPostalCode());
		userFromDB.setOperationAddress(userFE.getOperationAddress());
		userFromDB.setOperationState(userFE.getOperationState());
		userFromDB.setOperationCity(userFE.getOperationCity());
		userFromDB.setOperationPostalCode(userFE.getOperationPostalCode());

		userFromDB.setBankName(userFE.getBankName());
		userFromDB.setIfscCode(userFE.getIfscCode());
		userFromDB.setAccHolderName(userFE.getAccHolderName());
		userFromDB.setCurrency(userFE.getCurrency());
		userFromDB.setBranchName(userFE.getBranchName());
		userFromDB.setPanCard(userFE.getPanCard());
		userFromDB.setAccountNo(userFE.getAccountNo());

		userFromDB.setDateOfEstablishment(userFE.getDateOfEstablishment());
		userFromDB.setCin(userFE.getCin());
		userFromDB.setPan(userFE.getPan());
		userFromDB.setPanName(userFE.getPanName());
		userFromDB.setNoOfTransactions(userFE.getNoOfTransactions());
		userFromDB.setAmountOfTransactions(userFE.getAmountOfTransactions());
		userFromDB.setTransactionEmailerFlag(userFE.isTransactionEmailerFlag());
		userFromDB.setTransactionEmailId(userFE.getTransactionEmailId());
		userFromDB.setDefaultCurrency(userFE.getDefaultCurrency());
		userFromDB.setTransactionSms(userFE.getTransactionSms());
		userFromDB.setAllowDuplicateOrderId(userFE.getAllowDuplicateOrderId());
		userFromDB.setPaymentMessageSlab(userFE.getPaymentMessageSlab());
		userFromDB.setAllowSaleDuplicate(userFE.isAllowSaleDuplicate());
		userFromDB.setAllowRefundDuplicate(userFE.isAllowRefundDuplicate());
		userFromDB.setAllowSaleInRefund(userFE.isAllowSaleInRefund());
		userFromDB.setAllowRefundInSale(userFE.isAllowRefundInSale());
		userFromDB.setTerminalId(userFE.getTerminalId());
		userFromDB.setTransactionCustomerSMSFlag(userFE.isTransactionCustomerSMSFlag());
		userFromDB.setTransactionSmsFlag(userFE.isTransactionSmsFlag());
		userFromDB.setResellerMerchantSignupFlag(userFE.isResellerMerchantSignupFlag());
        userFromDB.setResellerUserStatusFlag(userFE.isResellerUserStatusFlag());
		userFromDB.setRoles(userFE.getRoles());
		userDao.update(userFromDB);

		return userFromDB;
	}

	public void updateAccount(List<Account> newAccounts, List<AccountCurrency> accountCurrencyList) {
		accountSetDB = userFromDB.getAccounts();

		for (Account accountFE : newAccounts) {
			for (Account accountDB : accountSetDB) {
				if (accountFE.getAcquirerPayId().equals(accountDB.getAcquirerPayId())) {
					accountDB.setPrimaryStatus(accountFE.isPrimaryStatus());

					accountDB.setPrimaryNetbankingStatus(accountFE.isPrimaryNetbankingStatus());
					Set<AccountCurrency> accountCurrencySetDB = accountDB.getAccountCurrencySet();

					// accountDB.setNetbankingPrimaryStatus(accountFE.isNetbankingPrimaryStatus());
					for (AccountCurrency accountCurrencyDB : accountCurrencySetDB) {
						for (AccountCurrency accountCurrencyFE : accountCurrencyList) {
							if (accountCurrencyFE.getCurrencyCode().equals(accountCurrencyDB.getCurrencyCode())
									&& accountCurrencyFE.getAcqPayId().equals(accountCurrencyDB.getAcqPayId())) {
								accountCurrencyDB.setMerchantId(accountCurrencyFE.getMerchantId());
								accountCurrencyDB.setTxnKey(accountCurrencyFE.getTxnKey());
								accountCurrencyDB.setPassword(accountCurrencyFE.getPassword());
								accountCurrencyDB.setDirectTxn(accountCurrencyFE.isDirectTxn());

								accountCurrencyDB.setAdf1(accountCurrencyFE.getAdf1());
								accountCurrencyDB.setAdf2(accountCurrencyFE.getAdf2());
								accountCurrencyDB.setAdf3(accountCurrencyFE.getAdf3());
								accountCurrencyDB.setAdf4(accountCurrencyFE.getAdf4());
								accountCurrencyDB.setAdf5(accountCurrencyFE.getAdf5());
								accountCurrencyDB.setAdf8(accountCurrencyFE.getAdf8());
								accountCurrencyDB.setAdf9(accountCurrencyFE.getAdf9());
								accountCurrencyDB.setAdf10(accountCurrencyFE.getAdf10());
								accountCurrencyDB.setAdf11(accountCurrencyFE.getAdf11());

								if (!StringUtils.isAnyEmpty(accountCurrencyFE.getPassword())) {
									if (!StringUtils.isAnyEmpty(accountCurrencyFE.getPassword().trim())) {
										accountCurrencyDB.setPassword(encryptDecryptService
												.encrypt(userFromDB.getPayId(), accountCurrencyFE.getPassword()));
									}

								}

								/*
								 * if (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf1())) { if
								 * (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf1().trim())) {
								 * accountCurrencyDB.setAdf1(
								 * encryptDecryptService.encrypt(accountCurrencyFE.getAdf1())); }
								 * 
								 * }
								 * 
								 * if (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf2())) { if
								 * (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf2().trim())) {
								 * accountCurrencyDB.setAdf2(
								 * encryptDecryptService.encrypt(accountCurrencyFE.getAdf2())); }
								 * 
								 * }
								 * 
								 * if (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf3())) { if
								 * (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf3().trim())) {
								 * accountCurrencyDB.setAdf3(
								 * encryptDecryptService.encrypt(accountCurrencyFE.getAdf3())); }
								 * 
								 * }
								 * 
								 * if (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf4())) { if
								 * (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf4().trim())) {
								 * accountCurrencyDB.setAdf4(
								 * encryptDecryptService.encrypt(accountCurrencyFE.getAdf4())); }
								 * 
								 * }
								 * 
								 * if (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf5())) { if
								 * (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf5().trim())) {
								 * accountCurrencyDB.setAdf5(
								 * encryptDecryptService.encrypt(accountCurrencyFE.getAdf5())); }
								 * 
								 * }
								 */

								if (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf6())) {
									if (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf6().trim())) {
										accountCurrencyDB.setAdf6(encryptDecryptService.encrypt(userFromDB.getPayId(),
												accountCurrencyFE.getAdf6()));
									}

								}

								if (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf7())) {
									if (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf7().trim())) {
										accountCurrencyDB.setAdf7(encryptDecryptService.encrypt(userFromDB.getPayId(),
												accountCurrencyFE.getAdf7()));
									}

								}

								/*
								 * if (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf8())) { if
								 * (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf8().trim())) {
								 * accountCurrencyDB.setAdf8(
								 * encryptDecryptService.encrypt(accountCurrencyFE.getAdf8())); }
								 * 
								 * }
								 * 
								 * 
								 * if (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf9())) { if
								 * (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf9().trim())) {
								 * accountCurrencyDB.setAdf9(
								 * encryptDecryptService.encrypt(accountCurrencyFE.getAdf9())); }
								 * 
								 * }
								 * 
								 * 
								 * if (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf10())) { if
								 * (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf10().trim())) {
								 * accountCurrencyDB.setAdf10(
								 * encryptDecryptService.encrypt(accountCurrencyFE.getAdf10())); }
								 * 
								 * }
								 * 
								 * 
								 * if (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf11())) { if
								 * (!StringUtils.isAnyEmpty(accountCurrencyFE.getAdf11().trim())) {
								 * accountCurrencyDB.setAdf11(
								 * encryptDecryptService.encrypt(accountCurrencyFE.getAdf11())); }
								 * 
								 * }
								 */

							}
						}
					}
				}
			}
		}
	}

	public User updateResellerDetails(User user) {
		Date date = new Date();
		userFromDB = userDao.findPayId(user.getPayId());
		userFromDB.setModeType(user.getModeType());
		userFromDB.setComments(user.getComments());
		userFromDB.setWhiteListIpAddress(user.getWhiteListIpAddress());
		userFromDB.setUserStatus(user.getUserStatus());

		userFromDB.setBusinessName(user.getBusinessName());
		userFromDB.setFirstName(user.getFirstName());
		userFromDB.setLastName(user.getLastName());
		userFromDB.setCompanyName(user.getCompanyName());
		userFromDB.setWebsite(user.getWebsite());
		userFromDB.setContactPerson(user.getContactPerson());
		userFromDB.setEmailId(user.getEmailId());
		userFromDB.setRegistrationDate(user.getRegistrationDate());
		userFromDB.setActivationDate(user.getActivationDate());

		userFromDB.setMerchantType(user.getMerchantType());
		userFromDB.setNoOfTransactions(user.getNoOfTransactions());
		userFromDB.setAmountOfTransactions(user.getAmountOfTransactions());
		
		// Getting 	Select Reseller from front End -- Quick Fix by Shaiwal
		if (StringUtils.isNotBlank(user.getResellerId()) && user.getResellerId() !="Select Reseller") {
			userFromDB.setResellerId(user.getResellerId());
		}
		else {
			userFromDB.setResellerId("");
		}
		
		userFromDB.setProductDetail(user.getProductDetail());

		userFromDB.setMobile(user.getMobile());
		userFromDB.setTransactionSmsFlag(user.isTransactionSmsFlag());
		userFromDB.setTelephoneNo(user.getTelephoneNo());
		userFromDB.setFax(user.getFax());
		userFromDB.setAddress(user.getAddress());
		userFromDB.setCity(user.getCity());
		userFromDB.setState(user.getState());
		userFromDB.setCountry(user.getCountry());
		userFromDB.setPostalCode(user.getPostalCode());

		userFromDB.setBankName(user.getBankName());
		userFromDB.setIfscCode(user.getIfscCode());
		userFromDB.setAccHolderName(user.getAccHolderName());
		userFromDB.setCurrency(user.getCurrency());
		userFromDB.setBranchName(user.getBranchName());
		userFromDB.setPanCard(user.getPanCard());
		userFromDB.setAccountNo(user.getAccountNo());

		userFromDB.setOrganisationType(user.getOrganisationType());
		userFromDB.setWebsite(user.getWebsite());
		userFromDB.setMultiCurrency(user.getMultiCurrency());
		userFromDB.setBusinessModel(user.getBusinessModel());
		userFromDB.setOperationAddress(user.getOperationAddress());
		userFromDB.setOperationState(user.getOperationState());
		userFromDB.setOperationCity(user.getOperationCity());
		userFromDB.setOperationPostalCode(user.getOperationPostalCode());
		userFromDB.setDateOfEstablishment(user.getDateOfEstablishment());

		userFromDB.setCin(user.getCin());
		userFromDB.setPan(user.getPan());
		userFromDB.setPanName(user.getPanName());
		userFromDB.setNoOfTransactions(user.getNoOfTransactions());
		userFromDB.setAmountOfTransactions(user.getAmountOfTransactions());
		userFromDB.setTransactionEmailerFlag(user.isTransactionEmailerFlag());
		userFromDB.setTransactionEmailId(user.getTransactionEmailId());
		userFromDB.setExpressPayFlag(user.isExpressPayFlag());
		userFromDB.setMerchantHostedFlag(user.isMerchantHostedFlag());
		userFromDB.setIframePaymentFlag(user.isIframePaymentFlag());
		userFromDB.setCheckOutJsFlag(user.isCheckOutJsFlag());
		userFromDB.setTransactionAuthenticationEmailFlag(user.isTransactionAuthenticationEmailFlag());
		userFromDB.setTransactionCustomerEmailFlag(user.isTransactionCustomerEmailFlag());
		userFromDB.setRetryTransactionCustomeFlag(user.isRetryTransactionCustomeFlag());
		userFromDB.setAttemptTrasacation(user.getAttemptTrasacation());
		userFromDB.setUpdateDate(date);
		userFromDB.setDefaultCurrency(user.getDefaultCurrency());
		userFromDB.setTransactionSms(user.getTransactionSms());
		userFromDB.setAllowDuplicateOrderId(user.getAllowDuplicateOrderId());
		userFromDB.setSkipOrderIdForRefund(user.isSkipOrderIdForRefund());
		userFromDB.setPaymentMessageSlab(user.getPaymentMessageSlab());
		userFromDB.setCardSaveParam(user.getCardSaveParam());
		userFromDB.setAllowSaleDuplicate(user.isAllowSaleDuplicate());
		userFromDB.setAllowRefundDuplicate(user.isAllowRefundDuplicate());
		userFromDB.setAllowSaleInRefund(user.isAllowSaleInRefund());
		userFromDB.setAllowRefundInSale(user.isAllowRefundInSale());
		userFromDB.setTerminalId(user.getTerminalId());
		userFromDB.setVpaSaveParam(user.getVpaSaveParam());
		userFromDB.setSaveVPAFlag(user.isSaveVPAFlag());
		userFromDB.setEposMerchant(user.isEposMerchant());
		userFromDB.setNbSaveParam(user.getNbSaveParam());
		userFromDB.setWlSaveParam(user.getWlSaveParam());
		userFromDB.setSaveNBFlag(user.isSaveNBFlag());
		userFromDB.setSaveWLFlag(user.isSaveWLFlag());
		userFromDB.setDiscountingFlag(user.isDiscountingFlag());
		userFromDB.setSmtMerchant(user.isSmtMerchant());
		userFromDB.seteNachReportFlag(user.iseNachReportFlag());
		userFromDB.setAccountVerificationFlag(user.isAccountVerificationFlag());
		userFromDB.setVpaVerificationFlag(user.isVpaVerificationFlag());
		userFromDB.setMerchantInitiatedDirectFlag(user.isMerchantInitiatedDirectFlag());
		userFromDB.setCustomerQrFlag(user.isCustomerQrFlag());
		userFromDB.setResellerMerchantSignupFlag(user.isResellerMerchantSignupFlag());
        userFromDB.setResellerUserStatusFlag(user.isResellerUserStatusFlag());
        userFromDB.setNon3dsTxn(user.isNon3dsTxn());
        userFromDB.setRoles(user.getRoles());
        userFromDB.setLogoFlag(user.isLogoFlag());
		userDao.update(userFromDB);

		return userFromDB;
	}

	public String getActionMessage() {
		return actionMessage;
	}

	public void setActionMessage(String actionMessage) {
		this.actionMessage = actionMessage;
	}

	public NotificationEmailer updateNotificationEmail(NotificationEmailer userFE, String payId) {
		notificationEmailerDB = userDao.findByEmailerByPayId(payId);
		if (notificationEmailerDB == null) {
			userFE.setPayId(payId);
			userDao.createEmailerFalg(userFE);
		} else {
			notificationEmailerDB.setTransactionEmailerFlag(userFE.isTransactionEmailerFlag());
			notificationEmailerDB.setRefundTransactionCustomerEmailFlag(userFE.isRefundTransactionCustomerEmailFlag());
			notificationEmailerDB.setTransactionCustomerEmailFlag(userFE.isTransactionCustomerEmailFlag());
			notificationEmailerDB.setRefundTransactionMerchantEmailFlag(userFE.isRefundTransactionMerchantEmailFlag());
			notificationEmailerDB.setSendMultipleEmailer(userFE.getSendMultipleEmailer());
			notificationEmailerDB.setTransactionAuthenticationEmailFlag(userFE.isTransactionAuthenticationEmailFlag());
			notificationEmailerDB.setTransactionCustomerEmailFlag(userFE.isTransactionCustomerEmailFlag());
			notificationEmailerDB.setTransactionSmsFlag(userFE.isTransactionSmsFlag());
			notificationEmailerDB.setSurchargeFlag(userFE.isSurchargeFlag());
			notificationEmailerDB.setExpressPayFlag(userFE.isExpressPayFlag());
			notificationEmailerDB.setIframePaymentFlag(userFE.isIframePaymentFlag());
			notificationEmailerDB.setMerchantHostedFlag(userFE.isMerchantHostedFlag());
			userDao.updateNotificationEamiler(notificationEmailerDB);

		}
		return notificationEmailerDB;
	}

	/*
	 * public NotificationEmailer updateNotificationEmail(NotificationEmailer[]
	 * userFE, String payId) { notificationEmailerDB =
	 * userDao.findByEmailerByPayId(payId); if(notificationEmailerDB ==null){ for
	 * (NotificationEmailer notificationEmailer : userFE) {
	 * notificationEmailer.setPayId(payId); userDao.createEmailerFalg(userFE); }
	 * }else{ for (NotificationEmailer notificationEmailer : userFE) {
	 * notificationEmailerDB.setTransactionEmailerFlag(notificationEmailer.
	 * isTransactionEmailerFlag());
	 * notificationEmailerDB.setRefundTransactionCustomerEmailFlag(
	 * notificationEmailer.isRefundTransactionCustomerEmailFlag());
	 * notificationEmailerDB.setTransactionCustomerEmailFlag(notificationEmailer.
	 * isTransactionCustomerEmailFlag());
	 * notificationEmailerDB.setSurchargeFlag(notificationEmailer.isSurchargeFlag())
	 * ; userDao.updateNotificationEamiler(notificationEmailerDB); } } return
	 * notificationEmailerDB; }
	 */

}