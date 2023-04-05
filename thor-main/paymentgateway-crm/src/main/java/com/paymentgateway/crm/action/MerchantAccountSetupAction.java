package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.paymentgateway.commons.dao.MPADao;
import com.paymentgateway.commons.dao.MPAMerchantDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.MPAMerchant;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.Payment;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.Permissions;
import com.paymentgateway.commons.user.Roles;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.BinCountryMapperType;
import com.paymentgateway.commons.util.BusinessType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.SaltFactory;
import com.paymentgateway.crm.actionBeans.CurrencyMapProvider;
import com.paymentgateway.crm.actionBeans.IndustryTypeCategoryProvider;
import com.paymentgateway.pg.core.fraudPrevention.util.AccountPasswordScrambler;

/**
 * @author Neeraj, Puneet
 *
 */
public class MerchantAccountSetupAction extends AbstractSecureAction implements ModelDriven<User> {

	@Autowired
	IndustryTypeCategoryProvider industryTypeCategoryProvider;

	@Autowired
	private UserDao userDao;

	@Autowired
	private CrmValidator validator;

	@Autowired
	private MPAMerchantDao mpaMerchantDao;

	@Autowired
	private CurrencyMapProvider currencyMapProvider;

	@SuppressWarnings("unused")
	@Autowired
	private AccountPasswordScrambler accPwdScrambler;

	@Autowired
	private MPADao mpaDao;

	@Autowired
	private UserSettingDao userSettingDao;

	private static Logger logger = LoggerFactory.getLogger(MerchantAccountSetupAction.class.getName());
	private static final long serialVersionUID = -642857372066409390L;

	private File logoImage;
	private User user = new User();
	private MerchantProcessingApplication MPAData = new MerchantProcessingApplication();
	private String salt;
	private String encKey;
	private String requestUrl;
	private String merchantLogo;
	private Boolean isFlag;
	private Boolean showDownload;
	private Boolean showCheckerFileDownload;
	private Map<String, String> currencyMap = new LinkedHashMap<String, String>();
	private Map<String, String> industryTypesList = new TreeMap<String, String>();
	private List<Merchants> resellerList = new ArrayList<Merchants>();
	private List<Merchants> superMerchantList = new ArrayList<Merchants>();
	private boolean eNachFlag;
	private boolean upiAutoPayFlag;
	private List<PermissionType> listPermissionType = new ArrayList<PermissionType>();
	private String permissionString = "";
	private List<String> lstPermissionType;
	private boolean customStatusEnquiryFlag;
	private boolean eCollectionFeeFlag;

	public String execute() {
		try {
			setUser(userDao.findPayId(user.getPayId()));
			// Setting annual Turnover
			MerchantProcessingApplication mpa = mpaDao.fetchMPADataByPayId(user.getPayId());

			setMPAData(getMerchantSettingDetails(user, mpa));

			// setMerchantLogo(getBase64LogoPerMerchant(user));

			if (StringUtils.isNotBlank(MPAData.getSuperMerchantId())) {
				User superMerchantUser = userDao.findPayId(MPAData.getSuperMerchantId());

				seteNachFlag(superMerchantUser.iseNachReportFlag());
				seteCollectionFeeFlag(superMerchantUser.isAllowECollectionFee());
				setUpiAutoPayFlag(superMerchantUser.isUpiAutoPayReportFlag());
				setCustomStatusEnquiryFlag(superMerchantUser.isAcceptPostSettledInEnquiry());
			}

			if (StringUtils.isNotBlank(MPAData.getSuperMerchantId())) {
				setUpiAutoPayFlag(upiAutoPayReportFlag(MPAData.getSuperMerchantId()));
			}

			if (StringUtils.isNotBlank(MPAData.getSuperMerchantId())) {
				setCustomStatusEnquiryFlag(acceptPostSettledInEnquiry(MPAData.getSuperMerchantId()));
			}

			if (StringUtils.isNotBlank(MPAData.getMpaFiles())) {
				setShowDownload(true);
			} else {
				setShowDownload(false);
			}

			setShowCheckerFileDownload(isCheckerFileUploaded(MPAData.getEmailId()));
			if (StringUtils.isNotBlank(user.getResellerId())) {
				setResellerList(userDao.getAllActiveReseller());
			} else {
				setResellerList(userDao.getAllActiveReseller());
			}

			if (user.getUserType().equals(UserType.RESELLER)) {
				getPermissions(user);
			}
			setResellerList(userDao.getAllActiveReseller());
			setSuperMerchantList(userDao.getSuperMerchantList());

			// setUser(accPwdScrambler.retrieveAndDecryptPass(user)); //Decrypt
			// password to
			// display at front end
			setSalt(SaltFactory.getSaltProperty(user));
			setEncKey(new PropertiesManager().getEncSalt(user.getPayId()));
			// set currencies
			currencyMap = currencyMapProvider.currencyMap(user);
			addPaymentMapped(user, currencyMap);
			// set IndustryTypes
			Map<String, String> industryCategoryLinkedMap = industryTypeCategoryProvider.industryTypes(user);
			industryTypesList.putAll(industryCategoryLinkedMap);
			setRequestUrl(new PropertiesManager().getSystemProperty("RequestURL"));

			if (StringUtils.isBlank(user.getSuperMerchantId())) {
				if (currencyMap.isEmpty()) {
					addFieldError(CrmFieldType.DEFAULT_CURRENCY.getName(),
							ErrorType.UNMAPPED_CURRENCY_ERROR.getResponseMessage());
					addActionMessage("No currency mapped!!");
					return SUCCESS;
				}
			}

			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	// public String getBase64LogoPerMerchant(User user) {
	// String base64File = "";
	// File file = null;
	// if (user.isLogoFlag()) {
	// if (StringUtils.isNotBlank(user.getResellerId())) {
	// file = new
	// File(PropertiesManager.propertiesMap.get(Constants.LOGO_FILE_UPLOAD_LOCATION.getValue())
	// + "//" + user.getResellerId(), user.getResellerId() + ".png");
	// } else {
	// file = new
	// File(PropertiesManager.propertiesMap.get(Constants.LOGO_FILE_UPLOAD_LOCATION.getValue())
	// + "//" + user.getPayId(), user.getPayId() + ".png");
	// }
	// } else {
	// if (StringUtils.isNotEmpty(user.getSuperMerchantId())) {
	// User superMerchant = userDao.findPayId(user.getSuperMerchantId());
	// if (superMerchant != null && superMerchant.isLogoFlag()) {
	// file = new
	// File(PropertiesManager.propertiesMap.get(Constants.LOGO_FILE_UPLOAD_LOCATION.getValue())
	// + "//" + superMerchant.getPayId(), superMerchant.getPayId() + ".png");
	// } else {
	// return "";
	// }
	// } else {
	// return "";
	// }
	// }
	// try (FileInputStream imageInFile = new FileInputStream(file)) {
	// byte fileData[] = new byte[(int) file.length()];
	// imageInFile.read(fileData);
	// base64File = Base64.getEncoder().encodeToString(fileData);
	// } catch (FileNotFoundException e) {
	// logger.error("Exception caught while encoding into Base64, " , e);
	// return "";
	// } catch (IOException e) {
	// logger.error("Exception caught while encoding into Base64, " , e);
	// return "";
	// } catch (Exception e) {
	// logger.error("Exception caught while encoding into Base64, " , e);
	// return "";
	// }
	// return base64File;
	// }

	@SuppressWarnings("unused")
	private boolean isMPAFileUploaded(User u) {
		File f = new File(PropertiesManager.propertiesMap.get("MPA_FILE_UPLOAD_LOCATION") + "/" + u.getPayId() + "/"
				+ "mpaFiles");
		if (f.exists() && f.isDirectory()) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("unused")
	private String fetchMerchantLogo(String payId) {
		String path = PropertiesManager.propertiesMap.get(Constants.LOGO_FILE_UPLOAD_LOCATION.getValue()) + payId;
		if (new File(path).exists()) {
			File directoryPath = new File(path);
			File filesList[] = directoryPath.listFiles();
			if (filesList.length > 0) {
				path = path + "/" + filesList[0].getName();
			}
			String destDirectory = "\\crm\\image\\userlogo\\";
			try {
				FileUtils.copyDirectory(new File(path), new File(destDirectory));
			} catch (IOException e) {
				logger.error("Exception caught, ", e);
			}
			return destDirectory + payId + ".png";
		}
		return "";
	}

	public User addPaymentMapped(User user, Map<String, String> currencyMap) {

		try {

			Set<Account> accountSet = new HashSet<Account>();
			Set<Account> accountSetUpdated = new HashSet<Account>();
			accountSet = user.getAccounts();

			for (Account account : accountSet) {

				Set<Payment> paymentSet = account.getPayments();
				AccountCurrency accountCurrency = account.getAccountCurrency(CrmFieldConstants.INR.getValue());
				Set<AccountCurrency> accountCurrencySetUpdated = new HashSet<AccountCurrency>();

				StringBuilder paymentTypeString = new StringBuilder();

				for (Payment payment : paymentSet) {
					paymentTypeString.append(payment.getPaymentType().getName());
					paymentTypeString.append(" | ");
				}
				accountCurrency.setMappedPaymentTypes(paymentTypeString.toString());
				accountCurrency.setCurrencyName(CrmFieldConstants.INR.toString());
				accountCurrencySetUpdated.add(accountCurrency);
				account.setAccountCurrencySet(accountCurrencySetUpdated);
				accountSetUpdated.add(account);

			}

			user.setAccounts(accountSetUpdated);
			return user;
		}

		catch (Exception e) {
			logger.error("Exception ", e);
		}
		return user;

	}

	public MerchantProcessingApplication getMerchantSettingDetails(User user, MerchantProcessingApplication mpaData) {

		if (mpaData == null) {
			mpaData = new MerchantProcessingApplication();
			mpaData.setPayId(user.getPayId());
		}
		MPAMerchant mpaMerchant = mpaMerchantDao.findByPayId(user.getPayId());
		mpaData.setEposMerchant(user.isEposMerchant());
		mpaData.setUserStatus(user.getUserStatus());
		mpaData.setModeType(user.getModeType());
		mpaData.setComments(user.getComments());
		mpaData.setEmailId(user.getEmailId());

		mpaData.setCardSaveParam(user.getCardSaveParam());
		// Notification Settings
		mpaData.setTransactionSmsFlag(user.isTransactionSmsFlag());
		mpaData.setTransactionAuthenticationEmailFlag(user.isTransactionAuthenticationEmailFlag());
		mpaData.setTransactionCustomerEmailFlag(user.isTransactionCustomerEmailFlag());
		mpaData.setRefundTransactionCustomerEmailFlag(user.isRefundTransactionCustomerEmailFlag());
		mpaData.setTransactionEmailerFlag(user.isTransactionEmailerFlag());
		mpaData.setRefundTransactionMerchantEmailFlag(user.isRefundTransactionMerchantEmailFlag());
		mpaData.setTransactionEmailId(user.getTransactionEmailId());
		mpaData.setTransactionSms(user.getTransactionSms());
		mpaData.setTransactionSmsFlag(user.isTransactionSmsFlag());
		mpaData.setTransactionCustomerSMSFlag(user.isTransactionCustomerSMSFlag());
		mpaData.setTransactionMerchantSMSFlag(user.isTransactionMerchantSMSFlag());
		mpaData.setTransactionFailedMerchantSMSFlag(user.isTransactionFailedMerchantSMSFlag());
		mpaData.setTransactionFailedCustomerSMSFlag(user.isTransactionFailedCustomerSMSFlag());
		mpaData.setTransactionRefundMerchantSMSFlag(user.isTransactionRefundMerchantSMSFlag());
		mpaData.setTransactionRefundCustomerSMSFlag(user.isTransactionRefundCustomerSMSFlag());
		mpaData.setTransactionFailedMerchantEmailFlag(user.isTransactionFailedMerchantEmailFlag());
		mpaData.setTransactionFailedCustomerEmailFlag(user.isTransactionFailedCustomerEmailFlag());
		// - Transactional Settings

		mpaData.setMerchantHostedFlag(user.isMerchantHostedFlag());

		mpaData.setIframePaymentFlag(user.isIframePaymentFlag());
		mpaData.setCheckOutJsFlag(user.isCheckOutJsFlag());

		mpaData.setSurchargeFlag(user.isSurchargeFlag());
		mpaData.setRetryTransactionCustomeFlag(user.isRetryTransactionCustomeFlag());
		mpaData.setAttemptTrasacation(user.getAttemptTrasacation());
		mpaData.setExpressPayFlag(user.isExpressPayFlag());
		mpaData.setExpressPay(user.isExpressPayFlag());
		mpaData.setSaveVPAFlag(user.isSaveVPAFlag());
		mpaData.setBookingRecord(user.isBookingRecord());
		mpaData.seteNachReportFlag(user.iseNachReportFlag());
		mpaData.setUpiAutoPayReportFlag(user.isUpiAutoPayReportFlag());
		mpaData.setCustomTransactionStatus(user.isCustomTransactionStatus());
		mpaData.setVpaSaveParam(user.getVpaSaveParam());
		mpaData.setExtraRefundLimit(user.getExtraRefundLimit());
		if (user.getExtraRefundLimit() > 0) {
			mpaData.setExtraRefundAmount(true);
		} else {
			mpaData.setExtraRefundAmount(false);
		}

		mpaData.setOneTimeRefundLimit(user.getOneTimeRefundLimit());
		if (user.getOneTimeRefundLimit() > 0) {
			mpaData.setOneTimeRefundAmount(true);
		} else {
			mpaData.setOneTimeRefundAmount(false);
		}

		mpaData.setRefundLimitRemains(user.getRefundLimitRemains());

		mpaData.setTerminalId(user.getTerminalId());
		mpaData.setRegistrationDate(user.getRegistrationDate());
		mpaData.setActivationDate(user.getActivationDate());

		mpaData.setAllowPartSettle(user.isAllowPartSettle());
		mpaData.setAllowSubtractValue(user.isAllowSubtractValue());
		mpaData.setSkipOrderIdForRefund(user.isSkipOrderIdForRefund());
		mpaData.setAllowRefundDuplicate(user.isAllowRefundDuplicate());
		mpaData.setAllowSaleDuplicate(user.isAllowSaleDuplicate());
		mpaData.setAllowSaleInRefund(user.isAllowSaleInRefund());
		mpaData.setAllowRefundInSale(user.isAllowRefundInSale());
		mpaData.setAllowDuplicateNotSaleOrderId(user.isAllowDuplicateNot());

		mpaData.setAmexSellerId(user.getAmexSellerId());
		mpaData.setmCC(user.getMCC());

		mpaData.setCheckerStatus(user.getCheckerStatus());
		mpaData.setMakerStatus(user.getMakerStatus());
		mpaData.setCheckerComments(user.getCheckerComments());
		mpaData.setMakerComments(user.getMakerComments());

		mpaData.setSaveNBFlag(user.isSaveNBFlag());
		mpaData.setSaveWLFlag(user.isSaveWLFlag());
		mpaData.setNbSaveParam(user.getNbSaveParam());
		mpaData.setWlSaveParam(user.getWlSaveParam());
		mpaData.setDiscountingFlag(user.isDiscountingFlag());

		mpaData.setResellerId(user.getResellerId());
		if (StringUtils.isNotBlank(mpaData.getTradingCountry())) {
			mpaData.setTradingCountry(user.getCountry());
		}

		if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
			mpaData.setSuperMerchantId(user.getSuperMerchantId());
		}

		if (mpaData.isAllowPartSettle()) {
			mpaData.setPartAnnualTurnover(mpaData.getAnnualTurnover());
		}
		mpaData.setDeviation(user.getDeviation());
		mpaData.setMpaOnlineFlag(user.isMpaOnlineFlag());
		mpaData.setVirtualAccountNo(user.getVirtualAccountNo());
		mpaData.setVirtualIfscCode(user.getVirtualIfscCode());
		mpaData.setVirtualBeneficiaryName(user.getVirtualBeneficiaryName());
		mpaData.setMerchantVPA(user.getMerchantVPA());
		if (user.isAllowRefundDuplicate() == true || (mpaData.getAllowDuplicateRefundOrderId() != null
				&& mpaData.getAllowDuplicateRefundOrderId().equalsIgnoreCase("YES"))) { // check
			mpaData.setAllowRefundDuplicate(true);
			mpaData.setAllowDuplicateRefundOrderId("true");
		} else {
			mpaData.setAllowRefundDuplicate(user.isAllowRefundDuplicate());
		}
		if (user.isAllowSaleDuplicate() == true || (mpaData.getAllowDuplicateSaleOrderId() != null
				&& mpaData.getAllowDuplicateSaleOrderId().equalsIgnoreCase("YES"))) { // check
			mpaData.setAllowSaleDuplicate(true);
			mpaData.setAllowDuplicateSaleOrderId("true");
		} else {
			mpaData.setAllowSaleDuplicate(user.isAllowSaleDuplicate());
		}
		if (user.isAllowSaleInRefund() == true || (mpaData.getAllowDuplicateSaleOrderIdInRefund() != null
				&& mpaData.getAllowDuplicateSaleOrderIdInRefund().equalsIgnoreCase("YES"))) { // check
			mpaData.setAllowSaleInRefund(true);
			mpaData.setAllowDuplicateSaleOrderIdInRefund("true");
		} else {
			mpaData.setAllowSaleInRefund(user.isAllowSaleInRefund());
		}
		if (user.isAllowRefundInSale() == true || (mpaData.getAllowDuplicateRefundOrderIdSale() != null
				&& mpaData.getAllowDuplicateRefundOrderIdSale().equalsIgnoreCase("YES"))) { // check
			mpaData.setAllowRefundInSale(true);
			mpaData.setAllowDuplicateRefundOrderIdSale("true");
		} else {
			mpaData.setAllowRefundInSale(user.isAllowRefundInSale());
			mpaData.setIframePaymentFlag(user.isIframePaymentFlag());
			user.setRetryTransactionCustomeFlag(MPAData.isRetryTransactionCustomeFlag());
			user.setAttemptTrasacation(MPAData.getAttemptTrasacation());
		}
		if (user.isExpressPayFlag() || (MPAData.getExpressPay() != null && MPAData.getExpressPay())) {
			user.setExpressPayFlag(true);
			MPAData.setExpressPay(true);
			MPAData.setExpressPayFlag(true);
		} else {
			user.setExpressPayFlag(false);
			MPAData.setExpressPay(false);
			MPAData.setExpressPayFlag(false);
		}

		if (mpaMerchant != null) {
			mpaData.setCheckerFileName(mpaMerchant.getCheckerFileName());
			mpaData.setMakerFileName(mpaMerchant.getMakerFileName());
		}

		if (mpaData.getGstVerification() == null)
			mpaData.setGstVerification(false);
		if (mpaData.getDirector1PanVerified() == null)
			mpaData.setDirector1PanVerified(false);
		if (mpaData.getDirector2PanVerified() == null)
			mpaData.setDirector2PanVerified(false);
		if (mpaData.getAccountVerification() == null)
			mpaData.setAccountVerification(false);

		mpaData.setAllowLogoInPgPage(user.isAllowLogoInPgPage());
		mpaData.setLogoFlag(user.isLogoFlag());
		mpaData.setCodName(user.getCodName());
		mpaData.setLogoName(user.getLogoName());

		mpaData.setRetailMerchantFlag(user.isRetailMerchantFlag());
		mpaData.setCapturedMerchantFlag(user.isCapturedMerchantFlag());
		mpaData.setVendorPayOutFlag(user.isVendorPayOutFlag());
		mpaData.setAccountVerificationFlag(user.isAccountVerificationFlag());
		mpaData.setLoadWalletFlag(user.isLoadWalletFlag());
		mpaData.setNodalReportFlag(user.isNodalReportFlag());
		mpaData.setPaymentAdviceFlag(user.isPaymentAdviceFlag());
		mpaData.setSmtMerchant(user.isSmtMerchant());
		mpaData.setPaymentMessageSlab(user.getPaymentMessageSlab());
		mpaData.setMerchantInitiatedDirectFlag(user.isMerchantInitiatedDirectFlag());
		mpaData.setImpsFlag(user.isImpsFlag());
		mpaData.setAllowQRScanFlag(user.isAllowQRScanFlag());
		mpaData.setVirtualAccountFlag(user.isVirtualAccountFlag());
		mpaData.setAllowUpiQRFlag(user.isAllowUpiQRFlag());
		mpaData.setCustomHostedUrl(user.getCustomHostedUrl());
		mpaData.setAllowCustomHostedUrl(user.isAllowCustomHostedUrl());
		mpaData.setLyraPay(user.isLyraPay());
		mpaData.setVpaVerificationFlag(user.isVpaVerificationFlag());
		mpaData.setCustomerQrFlag(user.isCustomerQrFlag());
		mpaData.setNetSettledFlag(user.isNetSettledFlag());
		mpaData.setWhiteListReturnUrl(user.getWhiteListReturnUrl());
		mpaData.setWhiteListReturnUrlFlag(user.isWhiteListReturnUrlFlag());

		if (StringUtils.isNotBlank(user.getState())) {
			mpaData.setTradingState(user.getState());
		}
		if (StringUtils.isNotBlank(user.getAddress())) {
			mpaData.setTradingAddress1(user.getAddress());
		}
		if (StringUtils.isNotBlank(user.getPostalCode())) {
			mpaData.setTradingPin(user.getPostalCode());
		}
		if (StringUtils.isNotBlank(user.getTelephoneNo())) {
			mpaData.setContactLandline(user.getTelephoneNo());
		}

		if (StringUtils.isNotBlank(user.getIndustryCategory())) {
			mpaData.setIndustryCategory(user.getIndustryCategory());
		}

		mpaData.setResellerMerchantSignupFlag(user.isResellerMerchantSignupFlag());
		mpaData.setResellerUserStatusFlag(user.isResellerUserStatusFlag());
		mpaData.setNon3dsTxn(user.isNon3dsTxn());
		mpaData.setBusinessName(user.getBusinessName());
		mpaData.setAutoRefund(user.isAutoRefund());
		mpaData.setCallBackFlag(user.isCallBackFlag());
		mpaData.setCallBackUrl(user.getCallBackUrl());
		mpaData.setAcceptPostSettledInEnquiry(user.isAcceptPostSettledInEnquiry());
		mpaData.setStatementFlag(user.isStatementFlag());
		mpaData.setTopupFlag(user.isTopupFlag());
		mpaData.setAllCallBackFlag(user.isAllCallBackFlag());
		mpaData.setAllowNodalPayoutFlag(user.isAllowNodalPayoutFlag());
		mpaData.setAllowPayoutUpdateStatus(user.isAllowPayoutUpdateStatus());
		mpaData.setAllowECollectionFee(user.isAllowECollectionFee());
		return mpaData;
	}

	public void getLogoImageFile(String payId) {

		try {
			String fileLocation = PropertiesManager.propertiesMap.get("MPA_FILE_UPLOAD_LOCATION") + "/" + payId;
			if (!StringUtils.isEmpty(MPAData.getLogoName())) {
				String fileName = MPAData.getLogoName() + "_" + payId + ".png";
				File destFile = new File(fileLocation, fileName);
				setLogoImage(destFile);
			}

		} catch (Exception e) {
			logger.error("Exception Cought while getting File : ", e);
		}
	}

	private void getPermissions(User user) {
		try {
			setListPermissionType(PermissionType.getResellerPermissionTypeByCategory(7));
			listPermissionType.add(PermissionType.getInstanceFromName("Quick Search"));
			Set<Roles> roles = user.getRoles();
			if (roles != null && !roles.isEmpty()) {
				Set<Permissions> permissions = roles.iterator().next().getPermissions();
				if (!permissions.isEmpty()) {
					StringBuilder perms = new StringBuilder();
					Iterator<Permissions> itr = permissions.iterator();
					while (itr.hasNext()) {
						PermissionType permissionType = itr.next().getPermissionType();
						perms.append(permissionType.getPermission());
						perms.append("-");
					}
					perms.deleteCharAt(perms.length() - 1);
					setPermissionString(perms.toString());
				}
			}
		} catch (Exception ex) {
			logger.error("Exception in getPermissions : ", ex);
		}
	}

	public Map<String, String> getIndustryTypesList() {
		return industryTypesList;
	}

	public void setIndustryTypesList(Map<String, String> industryTypesList) {
		this.industryTypesList = industryTypesList;
	}

	public void validator() {
		if ((validator.validateBlankField(getSalt()))) {
			/*
			 * addFieldError(CrmFieldType.SALT_KEY.getName(), validator
			 * .getResonseObject().getResponseMessage());
			 */
		} else if (!(validator.validateField(CrmFieldType.SALT_KEY, getSalt()))) {
			addFieldError(CrmFieldType.SALT_KEY.getName(), validator.getResonseObject().getResponseMessage());
		}
		Map<String, String> industryCategoryLinkedMap = BusinessType.getIndustryCategoryList();
		industryTypesList.putAll(industryCategoryLinkedMap);
	}

	// to provide default country
	public String getDefaultCountry() {
		if (StringUtils.isBlank(user.getCountry())) {
			return BinCountryMapperType.INDIA.getName();
		} else {
			return user.getCountry();
		}
	}

	public void validate() {
		if (!validator.validateField(CrmFieldType.PAY_ID, user.getPayId())) {
			addFieldError(CrmFieldType.PAY_ID.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
	}

	private Boolean isCheckerFileUploaded(String emailId) {
		User user = userDao.findByEmailId(emailId);
		File f = new File(PropertiesManager.propertiesMap.get("MPA_FILE_UPLOAD_LOCATION") + "/" + user.getPayId() + "/"
				+ "checkerFiles");
		if (f.exists() && f.isDirectory()) {
			return true;
		}
		return false;
	}

	public String validatePrimary() {
		Set<Account> accounts = null;
		accounts = user.getAccounts();
		for (Account account : accounts) {
			if (account.isPrimaryStatus()) {
				setIsFlag(true);
				return SUCCESS;
			}
		}
		setIsFlag(false);
		return SUCCESS;
	}

	public boolean eNachReportFlag(String superMerchantPayId) {
		return userSettingDao.fetchDataUsingPayId(superMerchantPayId).iseNachReportFlag();
	}

	public boolean upiAutoPayReportFlag(String superMerchantPayId) {
		return userSettingDao.fetchDataUsingPayId(superMerchantPayId).isUpiAutoPayReportFlag();
	}

	public boolean acceptPostSettledInEnquiry(String superMerchantPayId) {
		return userSettingDao.fetchDataUsingPayId(superMerchantPayId).isAcceptPostSettledInEnquiry();
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public List<Merchants> getResellerList() {
		return resellerList;
	}

	public void setResellerList(List<Merchants> resellerList) {
		this.resellerList = resellerList;
	}

	public File getLogoImage() {
		return logoImage;
	}

	public void setLogoImage(File logoImage) {
		this.logoImage = logoImage;
	}

	public User getModel() {
		return user;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Boolean getIsFlag() {
		return isFlag;
	}

	public void setIsFlag(Boolean isFlag) {
		this.isFlag = isFlag;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public Map<String, String> getCurrencyMap() {
		return currencyMap;
	}

	public void setCurrencyMap(Map<String, String> currencyMap) {
		this.currencyMap = currencyMap;
	}

	public MerchantProcessingApplication getMPAData() {
		return MPAData;
	}

	public void setMPAData(MerchantProcessingApplication mPAData) {
		MPAData = mPAData;
	}

	public Boolean getShowDownload() {
		return showDownload;
	}

	public void setShowDownload(Boolean showDownload) {
		this.showDownload = showDownload;
	}

	public String getMerchantLogo() {
		return merchantLogo;
	}

	public void setMerchantLogo(String merchantLogo) {
		this.merchantLogo = merchantLogo;
	}

	public Boolean getShowCheckerFileDownload() {
		return showCheckerFileDownload;
	}

	public void setShowCheckerFileDownload(Boolean showCheckerFileDownload) {
		this.showCheckerFileDownload = showCheckerFileDownload;
	}

	public boolean iseNachFlag() {
		return eNachFlag;
	}

	public void seteNachFlag(boolean eNachFlag) {
		this.eNachFlag = eNachFlag;
	}

	public List<PermissionType> getListPermissionType() {
		return listPermissionType;
	}

	public void setListPermissionType(List<PermissionType> listPermissionType) {
		this.listPermissionType = listPermissionType;
	}

	public String getPermissionString() {
		return permissionString;
	}

	public void setPermissionString(String permissionString) {
		this.permissionString = permissionString;
	}

	public List<String> getLstPermissionType() {
		return lstPermissionType;
	}

	public void setLstPermissionType(List<String> lstPermissionType) {
		this.lstPermissionType = lstPermissionType;
	}

	public boolean isCustomStatusEnquiryFlag() {
		return customStatusEnquiryFlag;
	}

	public void setCustomStatusEnquiryFlag(boolean customStatusEnquiryFlag) {
		this.customStatusEnquiryFlag = customStatusEnquiryFlag;
	}

	public String getEncKey() {
		return encKey;
	}

	public void setEncKey(String encKey) {
		this.encKey = encKey;
	}

	public boolean isUpiAutoPayFlag() {
		return upiAutoPayFlag;
	}

	public void setUpiAutoPayFlag(boolean upiAutoPayFlag) {
		this.upiAutoPayFlag = upiAutoPayFlag;
	}

	public List<Merchants> getSuperMerchantList() {
		return superMerchantList;
	}

	public void setSuperMerchantList(List<Merchants> superMerchantList) {
		this.superMerchantList = superMerchantList;
	}

	public boolean iseCollectionFeeFlag() {
		return eCollectionFeeFlag;
	}

	public void seteCollectionFeeFlag(boolean eCollectionFeeFlag) {
		this.eCollectionFeeFlag = eCollectionFeeFlag;
	}

}
