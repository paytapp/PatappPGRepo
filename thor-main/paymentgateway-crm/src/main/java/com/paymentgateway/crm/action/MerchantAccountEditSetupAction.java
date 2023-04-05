package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.opensymphony.xwork2.ModelDriven;
import com.paymentgateway.commons.dao.MPADao;
import com.paymentgateway.commons.dao.MerchantGridViewService;
import com.paymentgateway.commons.dao.PendingMappingRequestDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.ChargingDetailsDao;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.Payment;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.SurchargeDetailsDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserAudit;
import com.paymentgateway.commons.user.UserAuditDao;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.BinCountryMapperType;
import com.paymentgateway.commons.util.BusinessType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.SaltFactory;
import com.paymentgateway.commons.util.UserStatusType;
import com.paymentgateway.crm.actionBeans.CurrencyMapProvider;
import com.paymentgateway.crm.actionBeans.IndustryTypeCategoryProvider;
import com.paymentgateway.crm.actionBeans.MerchantRecordUpdater;
import com.paymentgateway.crm.mpa.MPAServicesFactory;
import com.paymentgateway.pg.core.fraudPrevention.util.AccountPasswordScrambler;

public class MerchantAccountEditSetupAction extends AbstractSecureAction
		implements ServletRequestAware, ModelDriven<MerchantProcessingApplication> {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private ChargingDetailsDao chargingDetailsDao;

	@Autowired
	private SurchargeDetailsDao surchargeDetailsDao;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private UserSettingDao userSettingDao;

	@Autowired
	private UserAuditDao userAuditDao;

	@Autowired
	private MPAServicesFactory mpaServicesFactory;

	@Autowired
	private CurrencyMapProvider currencyMapProvider;

	@Autowired
	private MerchantRecordUpdater merchantRecordUpdater;

	@Autowired
	private AccountPasswordScrambler accPwdScrambler;

	@Autowired
	private CrmValidator crmValidator;

	@Autowired
	private MerchantGridViewService merchantGridViewService;

	@Autowired
	IndustryTypeCategoryProvider industryTypeCategoryProvider;

	@Autowired
	private PendingMappingRequestDao pendingMappingRequestDao;

	@Autowired
	private MPADao mpaDao;

	private static Logger logger = LoggerFactory.getLogger(MerchantAccountEditSetupAction.class.getName());
	private static final long serialVersionUID = -7290087594947995464L;

	private File logoImage;
	private User user = new User();
//	private UserAudit userAudit = new UserAudit();
	private MerchantProcessingApplication MPAData = new MerchantProcessingApplication();
	private List<Account> accountList = new ArrayList<Account>();
	private Map<String, List<AccountCurrency>> accountCurrencyMap = new HashMap<String, List<AccountCurrency>>();
	private List<AccountCurrency> accountCurrencyList = new ArrayList<AccountCurrency>();
	private String salt;
	private String encKey;
	private String requestUrl;
	private HttpServletRequest request;
	private String defaultCurrency;
	private String annualTurnover;
	private String merchantLogo;
	private Boolean showDownload;
	private Map<String, String> currencyMap = new LinkedHashMap<String, String>();
	private Map<String, String> industryTypesList = new TreeMap<String, String>();
	private ResponseObject responseObject = new ResponseObject();
	private List<Merchants> resellerList = new ArrayList<Merchants>();

	private static final String prefix = "MONGO_DB_";

	public String saveAction() {
		Date date = new Date();
		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));

		try {
			User merchant = userDao.findPayId(MPAData.getPayId());

			if (merchant != null) {
				/*
				 * merchant.setRegistrationDate(MPAData.getRegistrationDate());
				 * merchant.setModeType(MPAData.getModeType());
				 * merchant.setUserStatus(MPAData.getUserStatus());
				 * merchant.setSurchargeFlag(MPAData.getSurcharge());
				 * merchant.setActivationDate(MPAData.getActivationDate());
				 * merchant.setSaveNBFlag(MPAData.isSaveNBFlag());
				 * merchant.setNbSaveParam(MPAData.getNbSaveParam());
				 */
				//setUserAuditDetails(merchant);

				setUser(getUserDetails(merchant));
			}
			if (StringUtils.isNotBlank(MPAData.getMpaFiles())) {
				setShowDownload(true);
			} else {
				setShowDownload(false);
			}
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			setSalt(SaltFactory.getSaltProperty(user));
			setEncKey(new PropertiesManager().getEncSalt(user.getPayId()));
			Map<String, String> industryCategoryLinkedMap = BusinessType.getIndustryCategoryList();
			industryTypesList.putAll(industryCategoryLinkedMap);
			user.setUserType(sessionUser.getUserType());
			user.setParentPayId(sessionUser.getParentPayId());
			user.setDefaultCurrency(sessionUser.getDefaultCurrency());
			if (MPAData.isCustomTransactionStatus()) {
				user.setCustomTransactionStatus(MPAData.isCustomTransactionStatus());
				if (StringUtils.isNotBlank(merchant.getSuperMerchantId())) {
					List<Merchants> subMerchantList = userDao.getActiveSubMerchants(merchant);
					for (Merchants merchantList : subMerchantList) {
						User merchant1 = userDao.findPayId(merchantList.getPayId());
						merchant1.setCustomTransactionStatus(true);
						userDao.update(merchant1);
					}

				} else {
					List<Merchants> subUserList = userDao.getSubUserList(MPAData.getPayId());
					for (Merchants merchantList : subUserList) {
						User merchant1 = userDao.findPayId(merchantList.getPayId());
						merchant1.setCustomTransactionStatus(true);
						userDao.update(merchant1);
					}
				}

			} else {
				user.setCustomTransactionStatus(MPAData.isCustomTransactionStatus());
				if (StringUtils.isNotBlank(merchant.getSuperMerchantId())) {
					List<Merchants> subMerchantList = userDao.getActiveSubMerchants(merchant);
					for (Merchants merchantList : subMerchantList) {
						User merchant1 = userDao.findPayId(merchantList.getPayId());
						merchant1.setCustomTransactionStatus(false);
						userDao.update(merchant1);
					}

				} else {
					List<Merchants> subUserList = userDao.getSubUserList(MPAData.getPayId());
					for (Merchants merchantList : subUserList) {
						User merchant1 = userDao.findPayId(merchantList.getPayId());
						merchant1.setCustomTransactionStatus(false);
						userDao.update(merchant1);
					}
				}
			}

			if (MPAData.isRetailMerchantFlag()) {
				user.setRetailMerchantFlag(MPAData.isRetailMerchantFlag());
				if (StringUtils.isNotBlank(merchant.getSuperMerchantId())) {
					List<Merchants> subMerchantList = userDao.getActiveSubMerchants(merchant);
					for (Merchants merchantList : subMerchantList) {
						User merchant1 = userDao.findPayId(merchantList.getPayId());
						merchant1.setRetailMerchantFlag(true);
						userDao.update(merchant1);
					}
				} else {
					List<Merchants> subUserList = userDao.getSubUserList(MPAData.getPayId());
					for (Merchants merchantList : subUserList) {
						User merchant1 = userDao.findPayId(merchantList.getPayId());
						merchant1.setRetailMerchantFlag(true);
						userDao.update(merchant1);
					}
				}
			} else {
				user.setRetailMerchantFlag(MPAData.isRetailMerchantFlag());
				if (StringUtils.isNotBlank(merchant.getSuperMerchantId())) {
					List<Merchants> subMerchantList = userDao.getActiveSubMerchants(merchant);
					for (Merchants merchantList : subMerchantList) {
						User merchant1 = userDao.findPayId(merchantList.getPayId());
						merchant1.setRetailMerchantFlag(false);
						userDao.update(merchant1);
					}

				} else {
					List<Merchants> subUserList = userDao.getSubUserList(MPAData.getPayId());
					for (Merchants merchantList : subUserList) {
						User merchant1 = userDao.findPayId(merchantList.getPayId());
						merchant1.setRetailMerchantFlag(false);
						userDao.update(merchant1);
					}
				}
			}

			if (!MPAData.isAccountVerificationFlag()) {
				List<Merchants> subUserList = userDao.getSubUserList(MPAData.getPayId());
				for (Merchants merchantList : subUserList) {
					User merchant1 = userDao.findPayId(merchantList.getPayId());
					merchant1.setAccountVerificationFlag(false);
					userDao.update(merchant1);
				}
			}
			if (!MPAData.isVpaVerificationFlag()) {
				List<Merchants> subUserList = userDao.getSubUserList(MPAData.getPayId());
				for (Merchants merchantList : subUserList) {
					User merchant1 = userDao.findPayId(merchantList.getPayId());
					merchant1.setVpaVerificationFlag(false);
					userDao.update(merchant1);
				}
			}
			if (!MPAData.isAllowECollectionFee() && merchant.isSuperMerchant()) {
				List<Merchants> subMerchantList = userDao.getSubMerchantListBySuperPayId(merchant.getPayId());
				for (Merchants merchantList : subMerchantList) {
					User merchant1 = userDao.findPayId(merchantList.getPayId());
					merchant1.setAllowECollectionFee(false);
					userDao.update(merchant1);
				}
			}

			if (MPAData.isPaymentAdviceFlag()) {
				user.setPaymentAdviceFlag(MPAData.isPaymentAdviceFlag());
				if (StringUtils.isNotBlank(merchant.getSuperMerchantId())) {
					List<Merchants> subMerchantList = userDao.getActiveSubMerchants(merchant);
					for (Merchants merchantList : subMerchantList) {
						User merchant1 = userDao.findPayId(merchantList.getPayId());
						merchant1.setPaymentAdviceFlag(true);
						userDao.update(merchant1);
					}
				} else {
					List<Merchants> subUserList = userDao.getSubUserList(MPAData.getPayId());
					for (Merchants merchantList : subUserList) {
						User merchant1 = userDao.findPayId(merchantList.getPayId());
						merchant1.setPaymentAdviceFlag(true);
						userDao.update(merchant1);
					}
				}
			} else {
				user.setPaymentAdviceFlag(MPAData.isPaymentAdviceFlag());
				if (StringUtils.isNotBlank(merchant.getSuperMerchantId())) {
					List<Merchants> subMerchantList = userDao.getActiveSubMerchants(merchant);
					for (Merchants merchantList : subMerchantList) {
						User merchant1 = userDao.findPayId(merchantList.getPayId());
						merchant1.setPaymentAdviceFlag(false);
						userDao.update(merchant1);
					}

				} else {
					List<Merchants> subUserList = userDao.getSubUserList(MPAData.getPayId());
					for (Merchants merchantList : subUserList) {
						User merchant1 = userDao.findPayId(merchantList.getPayId());
						merchant1.setPaymentAdviceFlag(false);
						userDao.update(merchant1);
					}
				}
			}

			if (MPAData.isMerchantInitiatedDirectFlag()) {
				user.setPaymentAdviceFlag(MPAData.isMerchantInitiatedDirectFlag());
				List<Merchants> subUserList = userDao.getSubUserList(MPAData.getPayId());
				for (Merchants merchantList : subUserList) {
					User merchant1 = userDao.findPayId(merchantList.getPayId());
					merchant1.setMerchantInitiatedDirectFlag(true);
					userDao.update(merchant1);
				}
			} else {
				user.setPaymentAdviceFlag(MPAData.isMerchantInitiatedDirectFlag());
				List<Merchants> subUserList = userDao.getSubUserList(MPAData.getPayId());
				for (Merchants merchantList : subUserList) {
					User merchant1 = userDao.findPayId(merchantList.getPayId());
					merchant1.setMerchantInitiatedDirectFlag(false);
					userDao.update(merchant1);
				}
			}

			user.setEposMerchant(Boolean.valueOf(MPAData.isEposMerchant()));
			currencyMap = currencyMapProvider.currencyMap(user);
			addPaymentMapped(user, currencyMap);
			setResellerList(userDao.getAllActiveReseller());
			setRequestUrl(new PropertiesManager().getSystemProperty("RequestURL"));

			if (MPAData == null) {
				if (user.isAllowPartSettle()) {
					MerchantProcessingApplication MPAData1 = new MerchantProcessingApplication();
					MPAData1.setValidCin(false);
					MPAData1.setValidCompanyName(false);
					MPAData1.setValidDirector1Pan(false);
					MPAData1.setValidDirector2Pan(false);
					MPAData1.setValidPan(false);
					MPAData1.setCinAttempts(0);
					MPAData1.setAnnualTurnover(annualTurnover);
					mpaDao.create(MPAData1);
					setMPAData(MPAData1);
				} else {
					setMPAData(MPAData);
				}
			}

			
			addResellerIdInTransaction(merchant);
			if (StringUtils.isNotBlank(merchant.getSuperMerchantId()) && !merchant.isSuperMerchant()) {
				addSuperMerchantId(merchant);
			}
			setIndustryTypesList(BusinessType.getIndustryCategoryList());
			if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)
					|| sessionUser.getUserType().equals(UserType.SUPERADMIN)
					|| sessionUser.getUserType().equals(UserType.MERCHANT)) {
				if (user.getUserStatus().toString().equals(UserStatusType.ACTIVE.getStatus().toString())) {
					user.setActivationDate(date);
					if (!(chargingDetailsDao.isChargingDetailsSet(user.getPayId())
							|| surchargeDetailsDao.isSurchargeDetailsSet(user.getPayId()))) {
						responseObject.setResponseCode("1111");
						responseObject
								.setResponseMessage(CrmFieldConstants.USER_CHARGINGDETAILS_NOT_SET_MSG.getValue());
						addActionMessage(responseObject.getResponseMessage());
						return CrmFieldConstants.ADMIN.getValue();
					}
				} else if (user.getUserStatus().toString().equals(UserStatusType.SUSPENDED.getStatus().toString())
						|| user.getUserStatus().toString()
								.equals(UserStatusType.TRANSACTION_BLOCKED.getStatus().toString())) {
					user.setActivationDate(null);
				}

				String emailId = userDao.getEmailIdByPayId(MPAData.getPayId());

				// Set MPA data in DB, even if no mapping is present
				saveMerchantLogo(logoImage, MPAData.getPayId());
				MerchantProcessingApplication tempMpa = merchantRecordUpdater.updateMPADetails(MPAData);

				// for the sub merchant status of Approve or Rejected
				MPAData.setStatus(tempMpa.getStatus());

				if (StringUtils.isBlank(user.getSuperMerchantId()) || user.isSuperMerchant()) {
					if (!pendingMappingRequestDao.findActiveMappingByEmailIdForActiveStatus(emailId)) {
						responseObject.setResponseCode("102");
						responseObject.setResponseMessage(CrmFieldConstants.USER_MERCHANT_MAPPING_NOT_SET.getValue());
						addActionMessage(responseObject.getResponseMessage());
						return CrmFieldConstants.ADMIN.getValue();
					}
				}
				Map<String, User> tempMap = new HashMap<String, User>();

				tempMap = merchantRecordUpdater.updateUserPendingDetails(user, sessionUser, accountList,
						accountCurrencyList, permissions);

				for (Map.Entry<String, User> entry : tempMap.entrySet()) {
					setUser(entry.getValue());
					responseObject.setResponseCode("101");
					responseObject.setResponseMessage(entry.getKey());
					addActionMessage(responseObject.getResponseMessage());
				}
				setUser(accPwdScrambler.retrieveAndDecryptPass(user));
				currencyMap = currencyMapProvider.currencyMap(user);
				merchantGridViewService.addUserInMap(user);
				return CrmFieldConstants.ADMIN.getValue();
			} else {
				setUser(merchantRecordUpdater.updateUserProfile(user));
				sessionMap.put(Constants.USER.getValue(), user);
				return CrmFieldConstants.SIGNUP_PROFILE.getValue();
			}

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
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
		} catch (Exception e) {
			logger.error("Exception " , e);
		}
		return user;
	}

	// to provide default country
	public String getDefaultCountry(User user) {
		if (StringUtils.isBlank(user.getCountry())) {
			return BinCountryMapperType.INDIA.getName();
		} else {
			return user.getCountry();
		}
	}

	public User getUserDetails(User user) {

		user.setIndustryCategory(MPAData.getIndustryCategory());
		user.setModeType(MPAData.getModeType());
		user.setComments(MPAData.getComments());
		user.setUserStatus(MPAData.getUserStatus());
		// user.setEmailId(MPAData.getEmailId());
		user.setTransactionAuthenticationEmailFlag(MPAData.isTransactionAuthenticationEmailFlag());
		user.setTransactionCustomerEmailFlag(MPAData.isTransactionCustomerEmailFlag());
		user.setTransactionCustomerSMSFlag(MPAData.isTransactionCustomerSMSFlag());
		user.setRefundTransactionCustomerEmailFlag(MPAData.isRefundTransactionCustomerEmailFlag());
		user.setTransactionEmailerFlag(MPAData.isTransactionEmailerFlag());
		user.setRefundTransactionMerchantEmailFlag(MPAData.isRefundTransactionMerchantEmailFlag());
		user.setTransactionEmailId(MPAData.getTransactionEmailId());
		user.setTransactionSms(MPAData.getTransactionSms());
		if (StringUtils.isNotBlank(MPAData.getTradingCountry())) {
			user.setCountry(MPAData.getTradingCountry());
		} else {
			user.setCountry(getDefaultCountry(user));
			MPAData.setTradingCountry(user.getCountry());
		}

		if (MPAData.isMerchantHostedFlag()) {
			user.setMerchantHostedFlag(true);
			MPAData.setMerchantHostedFlag(true);
		} else {
			user.setMerchantHostedFlag(false);
			MPAData.setMerchantHostedFlag(false);
		}
		user.setCheckOutJsFlag(MPAData.isCheckOutJsFlag());

		if (MPAData.isIframePaymentFlag()) {
			user.setIframePaymentFlag(true);
			MPAData.setIframePaymentFlag(true);
		} else {
			user.setIframePaymentFlag(false);
			MPAData.setIframePaymentFlag(false);
		}

		if (MPAData.isSurchargeFlag()) {
			user.setSurchargeFlag(true);
			MPAData.setSurchargeFlag(true);
		} else {
			user.setSurchargeFlag(false);
			MPAData.setSurchargeFlag(false);
		}
		user.setRetryTransactionCustomeFlag(MPAData.isRetryTransactionCustomeFlag());
		user.setAttemptTrasacation(MPAData.getAttemptTrasacation());

		if (MPAData.getExpressPay() != null && MPAData.getExpressPay()) {
			user.setExpressPayFlag(true);
			MPAData.setExpressPay(true);
		} else {
			user.setExpressPayFlag(false);
			MPAData.setExpressPay(false);
		}

		if (MPAData.isAllowPartSettle()) {
			MPAData.setAnnualTurnover(MPAData.getPartAnnualTurnover());
		}
		user.setDiscountingFlag(MPAData.isDiscountingFlag());
		user.setCardSaveParam(MPAData.getCardSaveParam());
		user.setSaveVPAFlag(MPAData.isSaveVPAFlag());
		user.setBookingRecord(MPAData.isBookingRecord());
		user.setVpaSaveParam(MPAData.getVpaSaveParam());
		user.seteNachReportFlag(MPAData.iseNachReportFlag());
		user.setUpiAutoPayReportFlag(MPAData.isUpiAutoPayReportFlag());
		user.setTerminalId(MPAData.getTerminalId());
		user.setRegistrationDate(MPAData.getRegistrationDate());
		user.setActivationDate(MPAData.getActivationDate());

		user.setAllowPartSettle(MPAData.isAllowPartSettle());
		user.setAllowSubtractValue(MPAData.isAllowSubtractValue());

		user.setSkipOrderIdForRefund(MPAData.isSkipOrderIdForRefund());
		user.setDeviation(MPAData.getDeviation());

		if (MPAData.isAllowSaleDuplicate() == true) {
			user.setAllowSaleDuplicate(true);
			MPAData.setAllowSaleDuplicate(true);
		} else {
			user.setAllowSaleDuplicate(false);
			MPAData.setAllowSaleDuplicate(false);
		}

		if (MPAData.isAllowRefundDuplicate() == true) {
			user.setAllowRefundDuplicate(true);
			MPAData.setAllowRefundDuplicate(true);
		} else {
			user.setAllowRefundDuplicate(false);
			MPAData.setAllowRefundDuplicate(false);
		}
		if (MPAData.isAllowSaleInRefund() == true) {
			user.setAllowSaleInRefund(true);
			MPAData.setAllowSaleInRefund(true);
		} else {
			user.setAllowSaleInRefund(false);
			MPAData.setAllowSaleInRefund(false);
		}
		if (MPAData.isAllowRefundInSale() == true) {
			user.setAllowRefundInSale(true);
			MPAData.setAllowRefundInSale(true);
		} else {
			user.setAllowRefundInSale(false);
			MPAData.setAllowRefundInSale(false);
		}
		if (MPAData.isAllowDuplicateNotSaleOrderId()) {
			user.setAllowDuplicateNot(true);
			MPAData.setAllowDuplicateNotSaleOrderId(true);
		} else {
			user.setAllowDuplicateNot(false);
			MPAData.setAllowDuplicateNotSaleOrderId(false);
		}

		if (!StringUtils.isEmpty(MPAData.getResellerId()) && !MPAData.getResellerId().equalsIgnoreCase("ALL")
				&& !MPAData.getResellerId().equalsIgnoreCase("Select Reseller")) {
			user.setResellerId(MPAData.getResellerId());
		} else {
			// user.setResellerId("");
			user.setResellerId("");
			MPAData.setResellerId("");
		}
		user.setAmexSellerId(MPAData.getAmexSellerId());
		user.setmCC(MPAData.getmCC());
		if (StringUtils.isNotBlank(MPAData.getSuperMerchantId())) {
			user.setSuperMerchantId(MPAData.getSuperMerchantId());
		} else {
			user.setSuperMerchantId(null);
		}

		user.setCheckerStatus(MPAData.getCheckerStatus());
		user.setMakerStatus(MPAData.getMakerStatus());
		user.setCheckerComments(MPAData.getCheckerComments());
		user.setMakerComments(MPAData.getMakerComments());

		user.setAllowLogoInPgPage(MPAData.isAllowLogoInPgPage());
		user.setLogoFlag(MPAData.isLogoFlag());
		user.setCodName(MPAData.getCodName());
		user.setLogoName(MPAData.getLogoName());

		user.setRetailMerchantFlag(MPAData.isRetailMerchantFlag());
		user.setCapturedMerchantFlag(MPAData.isCapturedMerchantFlag());

		user.setSaveNBFlag(MPAData.isSaveNBFlag());
		user.setSaveWLFlag(MPAData.isSaveWLFlag());
		user.setNbSaveParam(MPAData.getNbSaveParam());
		user.setWlSaveParam(MPAData.getWlSaveParam());
		user.setTransactionSmsFlag(MPAData.isTransactionSmsFlag());
		user.setTransactionCustomerEmailFlag(MPAData.isTransactionCustomerEmailFlag());
		user.setTransactionMerchantSMSFlag(MPAData.isTransactionMerchantSMSFlag());
		user.setAllowQRScanFlag(MPAData.isAllowQRScanFlag());
		user.setAllowUpiQRFlag(MPAData.isAllowUpiQRFlag());
		user.setNetSettledFlag(MPAData.isNetSettledFlag());
		user.setCustomHostedUrl(MPAData.getCustomHostedUrl());
		user.setAllowCustomHostedUrl(MPAData.isAllowCustomHostedUrl());
		user.setWhiteListReturnUrl(MPAData.getWhiteListReturnUrl());
		user.setWhiteListReturnUrlFlag(MPAData.isWhiteListReturnUrlFlag());

		if (MPAData.getOneTimeRefundLimit() > 0) {
			user.setExtraRefundLimit(0);
		} else {
			user.setExtraRefundLimit(MPAData.getExtraRefundLimit());
		}

		user.setOneTimeRefundLimit(MPAData.getOneTimeRefundLimit());

		if (MPAData.isLimitChangedFlag()) {
			user.setRefundLimitRemains(MPAData.getOneTimeRefundLimit());
			MPAData.setRefundLimitRemains(MPAData.getOneTimeRefundLimit());
		} else {
			user.setRefundLimitRemains(MPAData.getRefundLimitRemains());
		}
		if (MPAData.isSameLimitFlag()) {
			user.setRefundLimitRemains(MPAData.getOneTimeRefundLimit());
		}
		user.setCompanyName(MPAData.getCompanyName());
		user.setWebsite(MPAData.getCompanyWebsite());
		user.setTransactionFailedMerchantSMSFlag(MPAData.isTransactionFailedMerchantSMSFlag());
		user.setTransactionFailedCustomerSMSFlag(MPAData.isTransactionFailedCustomerSMSFlag());
		user.setTransactionRefundMerchantSMSFlag(MPAData.isTransactionRefundMerchantSMSFlag());
		user.setTransactionRefundCustomerSMSFlag(MPAData.isTransactionRefundCustomerSMSFlag());
		user.setTransactionFailedMerchantEmailFlag(MPAData.isTransactionFailedMerchantEmailFlag());
		user.setTransactionFailedCustomerEmailFlag(MPAData.isTransactionFailedCustomerEmailFlag());
		user.setAccountVerificationFlag(MPAData.isAccountVerificationFlag());
		user.setLoadWalletFlag(MPAData.isLoadWalletFlag());
		user.setSmtMerchant(MPAData.isSmtMerchant());
		user.setNodalReportFlag(MPAData.isNodalReportFlag());
		user.setPaymentMessageSlab(MPAData.getPaymentMessageSlab());
		user.setState(MPAData.getTradingState());
		user.setAddress(MPAData.getTradingAddress1());
		user.setPostalCode(MPAData.getTradingPin());
		user.setTelephoneNo(MPAData.getContactLandline());
		user.setContactPerson(MPAData.getContactName());
		user.setVendorPayOutFlag(MPAData.isVendorPayOutFlag());
		user.setMerchantInitiatedDirectFlag(MPAData.isMerchantInitiatedDirectFlag());
		user.setImpsFlag(MPAData.isImpsFlag());
		user.setVirtualAccountFlag(MPAData.isVirtualAccountFlag());
		user.setLyraPay(MPAData.isLyraPay());
		user.setVpaVerificationFlag(MPAData.isVpaVerificationFlag());
		user.setCustomerQrFlag(MPAData.isCustomerQrFlag());
		user.setResellerMerchantSignupFlag(MPAData.isResellerMerchantSignupFlag());
		user.setResellerUserStatusFlag(MPAData.isResellerUserStatusFlag());
		user.setNon3dsTxn(MPAData.isNon3dsTxn());
		user.setBusinessName(MPAData.getBusinessName());
		user.setAutoRefund(MPAData.isAutoRefund());
		user.setCallBackFlag(MPAData.isCallBackFlag());
		user.setCallBackUrl(MPAData.getCallBackUrl());
		user.setAcceptPostSettledInEnquiry(MPAData.isAcceptPostSettledInEnquiry());
		user.setStatementFlag(MPAData.isStatementFlag());
		user.setTopupFlag(MPAData.isTopupFlag());
		user.setAllCallBackFlag(MPAData.isAllCallBackFlag());
		user.setAllowNodalPayoutFlag(MPAData.isAllowNodalPayoutFlag());
		user.setAllowPayoutUpdateStatus(MPAData.isAllowPayoutUpdateStatus());
		user.setAllowECollectionFee(MPAData.isAllowECollectionFee());
		return user;
	}

	public void setUserAuditDetails(User user) {

		UserAudit userAudit = new UserAudit();

		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		MerchantProcessingApplication previousMPAData = mpaDao.getMpaDataperPayId(user.getPayId());

		Date dNow = new Date();
		String dateNow = DateCreater.formatDateForDb(dNow);

		if (null != previousMPAData) {
			userAudit.setIndustryCategory(previousMPAData.getIndustryCategory());
			userAudit.setTypeOfEntity(previousMPAData.getTypeOfEntity());
			userAudit.setCompanyName(previousMPAData.getCompanyName());
			userAudit.setCin(previousMPAData.getCin());
			userAudit.setDateOfIncorporation(previousMPAData.getDateOfIncorporation());
			userAudit.setCompanyEmailId(previousMPAData.getCompanyEmailId());
			userAudit.setCompanyRegisteredAddress(previousMPAData.getCompanyRegisteredAddress());
			userAudit.setTradingAddress1(previousMPAData.getTradingAddress1());
			userAudit.setTradingCountry(previousMPAData.getTradingCountry());
			userAudit.setTradingState(previousMPAData.getTradingState());
			userAudit.setTradingPin(previousMPAData.getTradingPin());
			userAudit.setBusinessPan(previousMPAData.getBusinessPan());
			userAudit.setGstin(previousMPAData.getGstin());
			userAudit.setCompanyPhone(previousMPAData.getCompanyPhone());
			userAudit.setCompanyWebsite(previousMPAData.getCompanyWebsite());
			userAudit.setBusinessEmailForCommunication(previousMPAData.getBusinessEmailForCommunication());
			userAudit.setContactName(previousMPAData.getContactName());
			userAudit.setContactMobile(previousMPAData.getContactMobile());
			userAudit.setContactLandline(previousMPAData.getContactLandline());
			userAudit.setDirector1FullName(previousMPAData.getDirector1FullName());
			userAudit.setDirector1Pan(previousMPAData.getDirector1Pan());
			userAudit.setDirector1Email(previousMPAData.getDirector1Email());
			userAudit.setDirector1Mobile(previousMPAData.getDirector1Mobile());
			userAudit.setDirector1Landline(previousMPAData.getDirector1Landline());
			userAudit.setDirector1Address(previousMPAData.getDirector1Address());
			userAudit.setDirector2FullName(previousMPAData.getDirector2FullName());
			userAudit.setDirector2Pan(previousMPAData.getDirector2Pan());
			userAudit.setDirector2Email(previousMPAData.getDirector2Email());
			userAudit.setDirector2Mobile(previousMPAData.getDirector2Mobile());
			userAudit.setDirector2Landline(previousMPAData.getDirector2Landline());
			userAudit.setDirector2Address(previousMPAData.getDirector2Address());
			userAudit.setMerchantSupportEmailId(previousMPAData.getMerchantSupportEmailId());
			userAudit.setMerchantSupportMobileNumber(previousMPAData.getMerchantSupportMobileNumber());
			userAudit.setMerchantSupportLandLine(previousMPAData.getMerchantSupportLandLine());
			userAudit.setAccountNumber(previousMPAData.getAccountNumber());
			userAudit.setVirtualAccountNo(previousMPAData.getVirtualAccountNo());
			userAudit.setAccountHolderName(previousMPAData.getAccountHolderName());
			userAudit.setAccountIfsc(previousMPAData.getAccountIfsc());
			userAudit.setAccountMobileNumber(previousMPAData.getAccountMobileNumber());
			userAudit.setAnnualTurnover(previousMPAData.getAnnualTurnover());
			userAudit.setAnnualTurnoverOnline(previousMPAData.getAnnualTurnoverOnline());
			userAudit.setPercentageCC(previousMPAData.getPercentageCC());
			userAudit.setPercentageDC(previousMPAData.getPercentageDC());
			userAudit.setPercentageNB(previousMPAData.getPercentageNB());
			userAudit.setPercentageUP(previousMPAData.getPercentageUP());
			userAudit.setPercentageWL(previousMPAData.getPercentageWL());
			userAudit.setPercentageEM(previousMPAData.getPercentageEM());
			userAudit.setPercentageCD(previousMPAData.getPercentageCD());
			userAudit.setPercentageNeftOrImpsOrRtgs(previousMPAData.getPercentageNeftOrImpsOrRtgs());
			userAudit.setPercentageDomestic(previousMPAData.getPercentageDomestic());
			userAudit.setPercentageInternational(previousMPAData.getPercentageInternational());
			userAudit.setResellerId(previousMPAData.getResellerId());
			userAudit.setPaymentCycle(previousMPAData.getPaymentCycle());
			userAudit.setExpressPayParameter(previousMPAData.getExpressPayParameter());
			userAudit.setAllowDuplicateSaleOrderId(previousMPAData.getAllowDuplicateSaleOrderId());
			userAudit.setAllowDuplicateRefundOrderId(previousMPAData.getAllowDuplicateRefundOrderId());
			userAudit.setAllowDuplicateSaleOrderIdInRefund(previousMPAData.getAllowDuplicateSaleOrderIdInRefund());
			userAudit.setAllowDuplicateRefundOrderIdSale(previousMPAData.getAllowDuplicateRefundOrderIdSale());
		}
		userAudit.setMerchantVPA(user.getMerchantVPA());
		userAudit.setMobile(user.getMobile());
		userAudit.setEmailId(user.getEmailId());
		userAudit.setBusinessName(user.getBusinessName());
		userAudit.setPayId(user.getPayId());
		userAudit.setTerminalId(user.getTerminalId());
		userAudit.setUserStatus(user.getUserStatus());

		userAudit.setSuperMerchantId(user.getSuperMerchantId());
		userAudit.setRegistrationDate(user.getRegistrationDate());
		userAudit.setActivationDate(user.getActivationDate());
		userAudit.setTransactionSmsFlag(user.isTransactionSmsFlag());
		userAudit.setTransactionAuthenticationEmailFlag(user.isTransactionAuthenticationEmailFlag());
		userAudit.setTransactionCustomerEmailFlag(user.isTransactionCustomerEmailFlag());
		userAudit.setTransactionFailedCustomerEmailFlag(user.isTransactionFailedCustomerEmailFlag());
		userAudit.setTransactionCustomerSMSFlag(user.isTransactionCustomerSMSFlag());
		userAudit.setTransactionFailedCustomerSMSFlag(user.isTransactionFailedCustomerSMSFlag());
		userAudit.setTransactionRefundCustomerSMSFlag(user.isTransactionRefundCustomerSMSFlag());
		userAudit.setRefundTransactionCustomerEmailFlag(user.isRefundTransactionCustomerEmailFlag());
		userAudit.setTransactionEmailerFlag(user.isTransactionEmailerFlag());
		userAudit.setTransactionMerchantSMSFlag(user.isTransactionMerchantSMSFlag());
		userAudit.setTransactionFailedMerchantSMSFlag(user.isTransactionFailedMerchantSMSFlag());
		userAudit.setTransactionFailedMerchantEmailFlag(user.isTransactionFailedMerchantEmailFlag());
		userAudit.setTransactionRefundMerchantSMSFlag(user.isTransactionRefundMerchantSMSFlag());
		userAudit.setRefundTransactionMerchantEmailFlag(user.isRefundTransactionMerchantEmailFlag());
		userAudit.setTransactionEmailId(user.getTransactionEmailId());
		userAudit.setTransactionSms(user.getTransactionSms());

		userAudit.setPaymentMessageSlab(user.getPaymentMessageSlab());
		//userAudit.setMerchantHostedFlag(user.isMerchantHostedFlag());
		//userAudit.setIframePaymentFlag(user.isIframePaymentFlag());
		userAudit.setCheckOutJsFlag(user.isCheckOutJsFlag());
		userAudit.setSurchargeFlag(user.isSurchargeFlag());
		userAudit.setDiscountingFlag(user.isDiscountingFlag());
		userAudit.setLoadWalletFlag(user.isLoadWalletFlag());
		userAudit.setEposMerchant(user.isEposMerchant());
		userAudit.setBookingRecord(user.isBookingRecord());
		userAudit.seteNachReportFlag(user.iseNachReportFlag());
		userAudit.setUpiAutoPayReportFlag(user.isUpiAutoPayReportFlag());
		userAudit.setAcceptPostSettledInEnquiry(user.isAcceptPostSettledInEnquiry());
		userAudit.setCustomTransactionStatus(user.isCustomTransactionStatus());
		userAudit.setCapturedMerchantFlag(user.isCapturedMerchantFlag());
		userAudit.setPaymentAdviceFlag(user.isPaymentAdviceFlag());
		userAudit.setRetailMerchantFlag(user.isRetailMerchantFlag());
		userAudit.setLyraPay(user.isLyraPay());
		userAudit.setNon3dsTxn(user.isNon3dsTxn());
		userAudit.setRetryTransactionCustomeFlag(user.isRetryTransactionCustomeFlag());
		userAudit.setAttemptTrasacation(user.getAttemptTrasacation());
		userAudit.setAutoRefund(user.isAutoRefund());
		userAudit.setSaveVPAFlag(user.isSaveVPAFlag());
		userAudit.setVpaSaveParam(user.getVpaSaveParam());
		userAudit.setSaveNBFlag(user.isSaveNBFlag());
		userAudit.setNbSaveParam(user.getNbSaveParam());
		userAudit.setSaveWLFlag(user.isSaveWLFlag());
		userAudit.setWlSaveParam(user.getWlSaveParam());
		userAudit.setExpressPayFlag(user.isExpressPayFlag());
		userAudit.setMerchantInitiatedDirectFlag(user.isMerchantInitiatedDirectFlag());
		userAudit.setNodalReportFlag(user.isNodalReportFlag());
		userAudit.setVirtualAccountFlag(user.isVirtualAccountFlag());
		userAudit.setTopupFlag(user.isTopupFlag());
		userAudit.setStatementFlag(user.isStatementFlag());
		userAudit.setAccountVerificationFlag(user.isAccountVerificationFlag());
		userAudit.setVpaVerificationFlag(user.isVpaVerificationFlag());
		userAudit.setAllowCustomHostedUrl(user.isAllowCustomHostedUrl());
		userAudit.setCustomHostedUrl(user.getCustomHostedUrl());
		userAudit.setSkipOrderIdForRefund(user.isSkipOrderIdForRefund());
		userAudit.setSmtMerchant(user.isSmtMerchant());
		userAudit.setLogoFlag(user.isLogoFlag());
		userAudit.setLogoName(user.getLogoName());
		userAudit.setAllowLogoInPgPage(user.isAllowLogoInPgPage());
		userAudit.setCodName(user.getCodName());
		userAudit.setAllowPartSettle(user.isAllowPartSettle());

		userAudit.setAllowSubtractValue(user.isAllowSubtractValue());
		userAudit.setDeviation(user.getDeviation());

		userAudit.setExtraRefundLimit(user.getExtraRefundLimit());

		userAudit.setOneTimeRefundLimit(user.getOneTimeRefundLimit());
		userAudit.setRefundLimitRemains(user.getRefundLimitRemains());

		userAudit.setmCC(user.getmCC());
		userAudit.setAllowQRScanFlag(user.isAllowQRScanFlag());
		userAudit.setAllowUpiQRFlag(user.isAllowUpiQRFlag());
		userAudit.setCustomerQrFlag(user.isCustomerQrFlag());
		userAudit.setCallBackUrl(user.getCallBackUrl());
		userAudit.setCallBackFlag(user.isCallBackFlag());
		userAudit.setAllCallBackFlag(user.isAllCallBackFlag());
		userAudit.setMpaDataUpdatedBy(sessionUser.getBusinessName());
		userAudit.setMpaDataUpdatedByEmail(sessionUser.getEmailId());
		userAudit.setMpaDataUpdatedByUserType(sessionUser.getUserType().toString());
		userAudit.setMpaDataUpdateDate(dateNow);
		userAudit.setAllowNodalPayoutFlag(user.isAllowNodalPayoutFlag());
		userAudit.setAllowPayoutUpdateStatus(user.isAllowPayoutUpdateStatus());
		userAudit.setAllowECollectionFee(user.isAllowECollectionFee());
		userAudit.setWhiteListReturnUrl(user.getWhiteListReturnUrl());
		userAudit.setWhiteListReturnUrlFlag(user.isWhiteListReturnUrlFlag());
		userAuditDao.updateUserAudit(userAudit);
	}

	public void saveMerchantLogo(File logoImageFile, String payId) {
		String srcfileName = payId + ".png";
		try {
			if (logoImageFile != null && payId != null) {
				File destFile = new File(
						PropertiesManager.propertiesMap.get(Constants.LOGO_FILE_UPLOAD_LOCATION.getValue()) + "//"
								+ payId,
						srcfileName);
				FileUtils.copyFile(logoImageFile, destFile);
				setMerchantLogo(getBase64LogoPerMerchant(user));
			}
		} catch (IOException e) {
			logger.error("Exception cought Wile saving logoImage File : " , e);
		}
	}

	public String getBase64LogoPerMerchant(User user) {
		String base64File = "";
		File file = null;
//		if (user.isLogoFlag()) {
//			file = new File(PropertiesManager.propertiesMap.get(Constants.LOGO_FILE_UPLOAD_LOCATION.getValue()) + "//"
//					+ user.getPayId(), user.getPayId() + ".png");
//		} else {
//			if (StringUtils.isNotEmpty(user.getSuperMerchantId())) {
//				User superMerchant = userDao.findPayId(user.getSuperMerchantId());
//				if (superMerchant != null && superMerchant.isLogoFlag()) {
//					file = new File(PropertiesManager.propertiesMap.get(Constants.LOGO_FILE_UPLOAD_LOCATION.getValue())
//							+ "//" + superMerchant.getPayId(), superMerchant.getPayId() + ".png");
//				} else {
//					return "";
//				}
//			} else {
//				return "";
//			}
//		}
		try (FileInputStream imageInFile = new FileInputStream(file)) {
			byte fileData[] = new byte[(int) file.length()];
			imageInFile.read(fileData);
			base64File = Base64.getEncoder().encodeToString(fileData);
		} catch (FileNotFoundException e) {
			logger.error("Exception caught while encoding into Base64, " , e);
			return "";
		} catch (IOException e) {
			logger.error("Exception caught while encoding into Base64, " , e);
			return "";
		} catch (Exception e) {
			logger.error("Exception caught while encoding into Base64, " , e);
			return "";
		}
		return base64File;
	}

	@SuppressWarnings("unused")
	private boolean isMPAFileUploaded(User u) {
		File f = new File(PropertiesManager.propertiesMap.get("MPA_FILE_UPLOAD_LOCATION") + "/" + u.getPayId() + "/"
				+ "mpaFiles");
		if (f.exists() && f.isDirectory()) {
			return true;
		}
		return false;
	}

	public void validate() {

//		boolean hdfcAcquirerFlag = false;
//		boolean yesBankAcquirerFlag = false;
//		boolean direcpayAcquirerFlag = false;
//		boolean modeTypeFlag = false;
//		boolean yesBankNetbnkingPrimaryFlag = false;
//		boolean direcpayNetbankingPrimaryFlag = false;
//		boolean direcpayExistFlag = false;
//		boolean yesBankExistFlag = false;
//		boolean hdfcBankExistFlag = false;
//		boolean americanExpressAcquirerFlag = false;
//		boolean americanExpressExistFlag = false;
		try {
			if (!(crmValidator.validateBlankField(MPAData.getTypeOfEntity()))) {
				if (!(crmValidator.validateField(CrmFieldType.TYPE_OF_ENTITY, MPAData.getTypeOfEntity()))) {
					addFieldError(CrmFieldType.TYPE_OF_ENTITY.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getCompanyName()))) {
				if (!(crmValidator.validateField(CrmFieldType.COMPANY_NAME, MPAData.getCompanyName()))) {
					addFieldError(CrmFieldType.COMPANY_NAME.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getCin()))) {
				if (!(crmValidator.validateField(CrmFieldType.CIN, MPAData.getCin()))) {
					addFieldError(CrmFieldType.CIN.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

			/*
			 * if (!(crmValidator.validateBlankField(getDateOfIncorporation()))) { if
			 * (!(crmValidator.validateField(CrmFieldType.DATE_OF_INCORPORATION,
			 * getDateOfIncorporation()))) {
			 * addFieldError(CrmFieldType.DATE_OF_INCORPORATION.getName(),
			 * crmValidator.getResonseObject().getResponseMessage()); } }
			 */

			if (!(crmValidator.validateBlankField(MPAData.getRegistrationNumber()))) {
				if (!(crmValidator.validateField(CrmFieldType.REGISTRATION_NUMBER, MPAData.getRegistrationNumber()))) {
					addFieldError(CrmFieldType.REGISTRATION_NUMBER.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getBusinessPan()))) {
				if (!(crmValidator.validateField(CrmFieldType.PAN_NUMBER, MPAData.getBusinessPan()))) {
					addFieldError(CrmFieldType.PAN_NUMBER.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getCompanyRegisteredAddress()))) {
				if (!(crmValidator.validateField(CrmFieldType.ADDRESS, MPAData.getCompanyRegisteredAddress()))) {
					addFieldError(CrmFieldType.ADDRESS.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getTradingAddress1()))) {
				if (!(crmValidator.validateField(CrmFieldType.ADDRESS, MPAData.getTradingAddress1()))) {
					addFieldError(CrmFieldType.ADDRESS.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

//		if (!(crmValidator.validateBlankField(MPAData.getTradingAddress2()))) {
//			if (!(crmValidator.validateField(CrmFieldType.ADDRESS, MPAData.getTradingAddress2()))) {
//				addFieldError(CrmFieldType.ADDRESS.getName(),
//						crmValidator.getResonseObject().getResponseMessage());
//			}
//		}

			if (!(crmValidator.validateBlankField(MPAData.getTradingCountry()))) {
				if (!(crmValidator.validateField(CrmFieldType.COUNTRY, MPAData.getTradingCountry()))) {
					addFieldError(CrmFieldType.COUNTRY.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getTradingState()))) {
				if (!(crmValidator.validateField(CrmFieldType.STATE, MPAData.getTradingState()))) {
					addFieldError(CrmFieldType.STATE.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getTradingPin()))) {
				if (!(crmValidator.validateField(CrmFieldType.POSTALCODE, MPAData.getTradingPin()))) {
					addFieldError(CrmFieldType.POSTALCODE.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getGstin()))) {
				if (!(crmValidator.validateField(CrmFieldType.MERCHANT_GST_NUMBER, MPAData.getGstin()))) {
					addFieldError(CrmFieldType.MERCHANT_GST_NUMBER.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getCompanyPhone()))) {
				if (!(crmValidator.validateField(CrmFieldType.TELEPHONE_NO, MPAData.getCompanyPhone()))) {
					addFieldError(CrmFieldType.TELEPHONE_NO.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getCompanyWebsite()))) {
				if (!(crmValidator.validateField(CrmFieldType.WEBSITE, MPAData.getCompanyWebsite()))) {
					addFieldError(CrmFieldType.WEBSITE.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getCompanyEmailId()))) {
				if (!(crmValidator.validateField(CrmFieldType.MERCHANT_EMAIL_ID, MPAData.getCompanyEmailId()))) {
					addFieldError(CrmFieldType.MERCHANT_EMAIL_ID.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getBusinessEmailForCommunication()))) {
				if (!(crmValidator.validateField(CrmFieldType.MERCHANT_EMAIL_ID,
						MPAData.getBusinessEmailForCommunication()))) {
					addFieldError(CrmFieldType.MERCHANT_EMAIL_ID.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getContactName()))) {
				if (!(crmValidator.validateField(CrmFieldType.CONTACT_PERSON, MPAData.getContactName()))) {
					addFieldError(CrmFieldType.CONTACT_PERSON.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getContactMobile()))) {
				if (!(crmValidator.validateField(CrmFieldType.MOBILE, MPAData.getContactMobile()))) {
					addFieldError(CrmFieldType.MOBILE.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getContactEmail()))) {
				if (!(crmValidator.validateField(CrmFieldType.EMAILID, MPAData.getContactEmail()))) {
					addFieldError(CrmFieldType.EMAILID.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getContactLandline()))) {
				if (!(crmValidator.validateField(CrmFieldType.TELEPHONE_NO, MPAData.getContactLandline()))) {
					addFieldError(CrmFieldType.TELEPHONE_NO.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getDirector1FullName()))) {
				if (!(crmValidator.validateField(CrmFieldType.FULL_NAME, MPAData.getDirector1FullName()))) {
					addFieldError(CrmFieldType.FULL_NAME.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getDirector1Pan()))) {
				if (!(crmValidator.validateField(CrmFieldType.PAN_NUMBER, MPAData.getDirector1Pan()))) {
					addFieldError(CrmFieldType.PAN_NUMBER.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getDirector1Email()))) {
				if (!(crmValidator.validateField(CrmFieldType.EMAILID, MPAData.getDirector1Email()))) {
					addFieldError(CrmFieldType.EMAILID.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getDirector1Mobile()))) {
				if (!(crmValidator.validateField(CrmFieldType.MOBILE, MPAData.getDirector1Mobile()))) {
					addFieldError(CrmFieldType.MOBILE.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getDirector1Landline()))) {
				if (!(crmValidator.validateField(CrmFieldType.TELEPHONE_NO, MPAData.getDirector1Landline()))) {
					addFieldError(CrmFieldType.TELEPHONE_NO.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getDirector1Address()))) {
				if (!(crmValidator.validateField(CrmFieldType.ADDRESS, MPAData.getDirector1Address()))) {
					addFieldError(CrmFieldType.ADDRESS.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getDirector1DOB()))) {
				if (!(crmValidator.validateField(CrmFieldType.DOB, MPAData.getDirector1DOB()))) {
					addFieldError(CrmFieldType.DOB.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getDirector2FullName()))) {
				if (!(crmValidator.validateField(CrmFieldType.FULL_NAME, MPAData.getDirector2FullName()))) {
					addFieldError(CrmFieldType.FULL_NAME.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getDirector2Pan()))) {
				if (!(crmValidator.validateField(CrmFieldType.PAN_NUMBER, MPAData.getDirector2Pan()))) {
					addFieldError(CrmFieldType.PAN_NUMBER.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getDirector2Email()))) {
				if (!(crmValidator.validateField(CrmFieldType.EMAILID, MPAData.getDirector2Email()))) {
					addFieldError(CrmFieldType.EMAILID.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getDirector2Mobile()))) {
				if (!(crmValidator.validateField(CrmFieldType.MOBILE, MPAData.getDirector2Mobile()))) {
					addFieldError(CrmFieldType.MOBILE.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getDirector2Landline()))) {
				if (!(crmValidator.validateField(CrmFieldType.TELEPHONE_NO, MPAData.getDirector2Landline()))) {
					addFieldError(CrmFieldType.TELEPHONE_NO.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getDirector2Address()))) {
				if (!(crmValidator.validateField(CrmFieldType.ADDRESS, MPAData.getDirector2Address()))) {
					addFieldError(CrmFieldType.ADDRESS.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getDirector2DOB()))) {
				if (!(crmValidator.validateField(CrmFieldType.DOB, MPAData.getDirector2DOB()))) {
					addFieldError(CrmFieldType.DOB.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getAccountNumber()))) {
				if (!(crmValidator.validateField(CrmFieldType.ACCOUNT_NO, MPAData.getAccountNumber()))) {
					addFieldError(CrmFieldType.ACCOUNT_NO.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getAccountIfsc()))) {
				if (!(crmValidator.validateField(CrmFieldType.IFSC_CODE, MPAData.getAccountIfsc()))) {
					addFieldError(CrmFieldType.IFSC_CODE.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getAccountHolderName()))) {
				if (!(crmValidator.validateField(CrmFieldType.FULL_NAME, MPAData.getAccountHolderName()))) {
					addFieldError(CrmFieldType.FULL_NAME.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getAccountMobileNumber()))) {
				if (!(crmValidator.validateField(CrmFieldType.MOBILE, MPAData.getAccountMobileNumber()))) {
					addFieldError(CrmFieldType.MOBILE.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getAnnualTurnover()))) {
				if (!(crmValidator.validateField(CrmFieldType.ANNUAL_TURNOVER, MPAData.getAnnualTurnover()))) {
					addFieldError(CrmFieldType.ANNUAL_TURNOVER.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getAnnualTurnoverOnline()))) {
				if (!(crmValidator.validateField(CrmFieldType.ANNUAL_TURNOVER_ONLINE,
						MPAData.getAnnualTurnoverOnline()))) {
					addFieldError(CrmFieldType.ANNUAL_TURNOVER_ONLINE.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

//		if (!(crmValidator.validateBlankField(MPAData.getPercentageCC()))) {
//			if (!(crmValidator.validateField(CrmFieldType.PERCENTAGE, MPAData.getPercentageCC()))) {
//				addFieldError(CrmFieldType.PERCENTAGE.getName(), crmValidator.getResonseObject().getResponseMessage());
//			}
//		}
//
//		if (!(crmValidator.validateBlankField(MPAData.getPercentageDC()))) {
//			if (!(crmValidator.validateField(CrmFieldType.PERCENTAGE, MPAData.getPercentageDC()))) {
//				addFieldError(CrmFieldType.PERCENTAGE.getName(), crmValidator.getResonseObject().getResponseMessage());
//			}
//		}
//
//		if (!(crmValidator.validateBlankField(MPAData.getPercentageDomestic()))) {
//			if (!(crmValidator.validateField(CrmFieldType.PERCENTAGE, MPAData.getPercentageDomestic()))) {
//				addFieldError(CrmFieldType.PERCENTAGE.getName(), crmValidator.getResonseObject().getResponseMessage());
//			}
//		}
//		
//		if (!(crmValidator.validateBlankField(MPAData.getPercentageInternational()))) {
//			if (!(crmValidator.validateField(CrmFieldType.PERCENTAGE, MPAData.getPercentageInternational()))) {
//				addFieldError(CrmFieldType.PERCENTAGE.getName(), crmValidator.getResonseObject().getResponseMessage());
//			}
//		}
//		
//		if (!(crmValidator.validateBlankField(MPAData.getPercentageCD()))) {
//			if (!(crmValidator.validateField(CrmFieldType.PERCENTAGE, MPAData.getPercentageCD()))) {
//				addFieldError(CrmFieldType.PERCENTAGE.getName(), crmValidator.getResonseObject().getResponseMessage());
//			}
//		}
//		
//		if (!(crmValidator.validateBlankField(MPAData.getPercentageNeftOrImpsOrRtgs()))) {
//			if (!(crmValidator.validateField(CrmFieldType.PERCENTAGE, MPAData.getPercentageNeftOrImpsOrRtgs()))) {
//				addFieldError(CrmFieldType.PERCENTAGE.getName(), crmValidator.getResonseObject().getResponseMessage());
//			}
//		}
//		
//		if (!(crmValidator.validateBlankField(MPAData.getPercentageNB()))) {
//			if (!(crmValidator.validateField(CrmFieldType.PERCENTAGE, MPAData.getPercentageNB()))) {
//				addFieldError(CrmFieldType.PERCENTAGE.getName(), crmValidator.getResonseObject().getResponseMessage());
//			}
//		}
//		
//		if (!(crmValidator.validateBlankField(MPAData.getPercentageUP()))) {
//			if (!(crmValidator.validateField(CrmFieldType.PERCENTAGE, MPAData.getPercentageUP()))) {
//				addFieldError(CrmFieldType.PERCENTAGE.getName(), crmValidator.getResonseObject().getResponseMessage());
//			}
//		}
//		
//		if (!(crmValidator.validateBlankField(MPAData.getPercentageWL()))) {
//			if (!(crmValidator.validateField(CrmFieldType.PERCENTAGE, MPAData.getPercentageWL()))) {
//				addFieldError(CrmFieldType.PERCENTAGE.getName(), crmValidator.getResonseObject().getResponseMessage());
//			}
//		}
//		
//		if (!(crmValidator.validateBlankField(MPAData.getPercentageEM()))) {
//			if (!(crmValidator.validateField(CrmFieldType.PERCENTAGE, MPAData.getPercentageEM()))) {
//				addFieldError(CrmFieldType.PERCENTAGE.getName(), crmValidator.getResonseObject().getResponseMessage());
//			}
//		}

			if (!(crmValidator.validateBlankField(MPAData.getThirdPartyForCardData()))) {
				if (!(crmValidator.validateField(CrmFieldType.THIRD_PARTY_NAME, MPAData.getThirdPartyForCardData()))) {
					addFieldError(CrmFieldType.THIRD_PARTY_NAME.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			/*
			 * if (!(crmValidator.validateBlankField(getRefundsAllowed()))) { if
			 * (!(crmValidator.validateField(CrmFieldType.REFUNDS_ALLOWED,
			 * getRefundsAllowed()))) {
			 * addFieldError(CrmFieldType.REFUNDS_ALLOWED.getName(),
			 * crmValidator.getResonseObject().getResponseMessage()); } }
			 */

			if (!(crmValidator.validateBlankField(MPAData.getTechnicalContactName()))) {
				if (!(crmValidator.validateField(CrmFieldType.FULL_NAME, MPAData.getTechnicalContactName()))) {
					addFieldError(CrmFieldType.FULL_NAME.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getTechnicalContactMobile()))) {
				if (!(crmValidator.validateField(CrmFieldType.MOBILE, MPAData.getTechnicalContactMobile()))) {
					addFieldError(CrmFieldType.MOBILE.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getTechnicalContactEmail()))) {
				if (!(crmValidator.validateField(CrmFieldType.EMAILID, MPAData.getTechnicalContactEmail()))) {
					addFieldError(CrmFieldType.EMAILID.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getTechnicalContactLandline()))) {
				if (!(crmValidator.validateField(CrmFieldType.TELEPHONE_NO, MPAData.getTechnicalContactLandline()))) {
					addFieldError(CrmFieldType.TELEPHONE_NO.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			/*
			 * if (!(crmValidator.validateBlankField(getServerDetails()))) { if
			 * (!(crmValidator.validateField(CrmFieldType.SERVER_DETAILS,
			 * getServerDetails()))) { addFieldError(CrmFieldType.SERVER_DETAILS.getName(),
			 * crmValidator.getResonseObject().getResponseMessage()); } }
			 */

			if (!(crmValidator.validateBlankField(MPAData.getServerCompanyName()))) {
				if (!(crmValidator.validateField(CrmFieldType.COMPANY_NAME, MPAData.getServerCompanyName()))) {
					addFieldError(CrmFieldType.COMPANY_NAME.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getServerCompanyLandline()))) {
				if (!(crmValidator.validateField(CrmFieldType.TELEPHONE_NO, MPAData.getServerCompanyLandline()))) {
					addFieldError(CrmFieldType.TELEPHONE_NO.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getServerCompanyAddress()))) {
				if (!(crmValidator.validateField(CrmFieldType.ADDRESS, MPAData.getServerCompanyAddress()))) {
					addFieldError(CrmFieldType.ADDRESS.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getServerCompanyMobile()))) {
				if (!(crmValidator.validateField(CrmFieldType.MOBILE, MPAData.getServerCompanyMobile()))) {
					addFieldError(CrmFieldType.MOBILE.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

			if (!(crmValidator.validateBlankField(MPAData.getOperatingSystem()))) {
				if (!(crmValidator.validateField(CrmFieldType.OS_ARCHITECTURE, MPAData.getOperatingSystem()))) {
					addFieldError(CrmFieldType.OS_ARCHITECTURE.getName(),
							crmValidator.getResonseObject().getResponseMessage());
				}
			}

			/*
			 * if (!(crmValidator.validateBlankField(getBackendTechnology()))) { if
			 * (!(crmValidator.validateField(CrmFieldType.BACKEND_TECHNOLOGY,
			 * getBackendTechnology()))) {
			 * addFieldError(CrmFieldType.BACKEND_TECHNOLOGY.getName(),
			 * crmValidator.getResonseObject().getResponseMessage()); } }
			 */

			/*
			 * if (!(crmValidator.validateBlankField(getApplicationServerTechnology()))) {
			 * if (!(crmValidator.validateField(CrmFieldType.APPLICATION_SERVER_TECHNOLOGY,
			 * getApplicationServerTechnology()))) {
			 * addFieldError(CrmFieldType.APPLICATION_SERVER_TECHNOLOGY.getName(),
			 * crmValidator.getResonseObject().getResponseMessage()); } }
			 */

			if (!(crmValidator.validateBlankField(MPAData.getProductionServerIp()))) {
				if (!(crmValidator.validateField(CrmFieldType.IP, MPAData.getProductionServerIp()))) {
					addFieldError(CrmFieldType.IP.getName(), crmValidator.getResonseObject().getResponseMessage());
				}
			}

			/*
			 * if (!(crmValidator.validateBlankField(getMerchantType()))) { if
			 * (!(crmValidator.validateField(CrmFieldType.MERCHANT_TYPE,
			 * getMerchantType()))) { addFieldError(CrmFieldType.MERCHANT_TYPE.getName(),
			 * crmValidator.getResonseObject().getResponseMessage()); } }
			 */

//		if (!(crmValidator.validateBlankField(MPAData.getSurcharge().toString()))) {
//			if (!(crmValidator.validateField(CrmFieldType.TRUE_FALSE_STRING, MPAData.getSurcharge().toString()))) {
//				addFieldError(CrmFieldType.TRUE_FALSE_STRING.getName(), crmValidator.getResonseObject().getResponseMessage());
//			}
//		}

			/*
			 * if (!(crmValidator.validateBlankField(getIntegrationType()))) { if
			 * (!(crmValidator.validateField(CrmFieldType.INTEGRATION_TYPE,
			 * getIntegrationType()))) {
			 * addFieldError(CrmFieldType.INTEGRATION_TYPE.getName(),
			 * crmValidator.getResonseObject().getResponseMessage()); } }
			 */

//			if (!(crmValidator.validateBlankField(MPAData.getCustomizedInvoiceDesign().toString()))) {
//				if (!(crmValidator.validateField(CrmFieldType.TRUE_FALSE_STRING,
//						MPAData.getCustomizedInvoiceDesign().toString()))) {
//					addFieldError(CrmFieldType.TRUE_FALSE_STRING.getName(),
//							crmValidator.getResonseObject().getResponseMessage());
//				}
//			}

//			if (!(crmValidator.validateBlankField(MPAData.getInternationalCards().toString()))) {
//				if (!(crmValidator.validateField(CrmFieldType.TRUE_FALSE_STRING,
//						MPAData.getInternationalCards().toString()))) {
//					addFieldError(CrmFieldType.TRUE_FALSE_STRING.getName(),
//							crmValidator.getResonseObject().getResponseMessage());
//				}
//			}

			// Temp
			/*
			 * if (!(crmValidator.validateBlankField(MPAData.getExpressPayParameter()))) {
			 * if(!(crmValidator.validateField(CrmFieldType.EXPRESS_PAY_PARAMETER,MPAData.
			 * getExpressPayParameter()))) {
			 * addFieldError(CrmFieldType.EXPRESS_PAY_PARAMETER.getName(),
			 * crmValidator.getResonseObject().getResponseMessage()); } }
			 */

//			if (!(crmValidator.validateBlankField(MPAData.getAllowDuplicateSaleOrderId()))) {
//				if (!(crmValidator.validateField(CrmFieldType.YES_NO_NOTSURE,
//						MPAData.getAllowDuplicateSaleOrderId()))) {
//					addFieldError(CrmFieldType.YES_NO_NOTSURE.getName(),
//							crmValidator.getResonseObject().getResponseMessage());
//				}
//			}

//			if (!(crmValidator.validateBlankField(MPAData.getAllowDuplicateRefundOrderId()))) {
//				if (!(crmValidator.validateField(CrmFieldType.YES_NO_NOTSURE,
//						MPAData.getAllowDuplicateRefundOrderId()))) {
//					addFieldError(CrmFieldType.YES_NO_NOTSURE.getName(),
//							crmValidator.getResonseObject().getResponseMessage());
//				}
//			}

//			if (!(crmValidator.validateBlankField(MPAData.getAllowDuplicateSaleOrderIdInRefund()))) {
//				if (!(crmValidator.validateField(CrmFieldType.YES_NO_NOTSURE,
//						MPAData.getAllowDuplicateSaleOrderIdInRefund()))) {
//					addFieldError(CrmFieldType.YES_NO_NOTSURE.getName(),
//							crmValidator.getResonseObject().getResponseMessage());
//				}
//			}

//			if (!(crmValidator.validateBlankField(MPAData.getAllowDuplicateRefundOrderIdSale()))) {
//				if (!(crmValidator.validateField(CrmFieldType.YES_NO_NOTSURE,
//						MPAData.getAllowDuplicateRefundOrderIdSale()))) {
//					addFieldError(CrmFieldType.YES_NO_NOTSURE.getName(),
//							crmValidator.getResonseObject().getResponseMessage());
//				}
//			}

			/*
			 * if (!(crmValidator.validateBlankField(getStage()))) { if
			 * (!(crmValidator.validateField(CrmFieldType.MPA_STAGE, getStage()))) {
			 * addFieldError(CrmFieldType.MPA_STAGE.getName(),
			 * crmValidator.getResonseObject().getResponseMessage()); } }
			 */

			if ((crmValidator.validateBlankField(MPAData.getComments()))) {
			} else if (!(crmValidator.validateField(CrmFieldType.COMMENTS, MPAData.getComments()))) {
				addFieldError(CrmFieldType.COMMENTS.getName(), crmValidator.getResonseObject().getResponseMessage());
			}
			if (crmValidator.validateBlankField(MPAData.getTransactionEmailId())) {
			} else if (!(crmValidator.isValidBatchEmailId(MPAData.getTransactionEmailId())
					|| (crmValidator.isValidEmailId(MPAData.getTransactionEmailId())))) {
				addFieldError(CrmFieldType.TRANSACTION_EMAIL_ID.getName(),
						crmValidator.getResonseObject().getResponseMessage());
			}

			if (crmValidator.validateBlankField(MPAData.getEmailId())) {
				// addFieldError(CrmFieldType.EMAILID.getName(),
				// crmValidator.getResonseObject().getResponseMessage());
			} else if (!(crmValidator.isValidEmailId(MPAData.getEmailId()))) {
				addFieldError(CrmFieldType.EMAILID.getName(), crmValidator.getResonseObject().getResponseMessage());
			}
			if ((crmValidator.validateBlankField(MPAData.getPayId()))) {
				addFieldError(CrmFieldType.PAY_ID.getName(), crmValidator.getResonseObject().getResponseMessage());
			} else if (!(crmValidator.validateField(CrmFieldType.PAY_ID, MPAData.getPayId()))) {
				addFieldError(CrmFieldType.PAY_ID.getName(), crmValidator.getResonseObject().getResponseMessage());
			}
			if ((crmValidator.validateBlankField(MPAData.getmCC()))) {
			} else if (!(crmValidator.validateField(CrmFieldType.MCC, MPAData.getmCC()))) {
				addFieldError(CrmFieldType.MCC.getName(), crmValidator.getResonseObject().getResponseMessage());
			}

			if (!getFieldErrors().isEmpty()) {
				User userFromDb = userDao.getUserClass(MPAData.getPayId());
				float extraRefundLimitDB = userFromDb.getExtraRefundLimit();
				user.setExtraRefundLimit(extraRefundLimitDB);
			}
			if (!getFieldErrors().isEmpty()) {
				User userFromDb = userDao.getUserClass(MPAData.getPayId());
				float oneTimeRefundLimitDB = userFromDb.getOneTimeRefundLimit();
				user.setOneTimeRefundLimit(oneTimeRefundLimitDB);
			}
		} catch (Exception ex) {
			logger.error("Exception cought in validation " , ex);
		}
	}

	@SuppressWarnings("unused")
	private List<Account> updateAccount(List<Account> newAccounts, List<AccountCurrency> accountCurrencyList) {
		List<Account> accountList = new ArrayList<Account>();
		for (Account accountFE : newAccounts) {
			for (AccountCurrency accountCurrencyFE : accountCurrencyList) {
				if (accountCurrencyFE.getAcqPayId().equals(accountFE.getAcquirerPayId())) {
					Set<AccountCurrency> accountCurrencySet = new HashSet<AccountCurrency>();
					accountCurrencySet.add(accountCurrencyFE);
					accountFE.addAccountCurrency(accountCurrencyFE);
				}
			}
			accountList.add(accountFE);
		}
		return accountList;
	}

	/*
	 * public void addResellerIdInTransaction(User merchant) { logger.
	 * info("Inside addResellerIdInTransaction(), MerchantAccountEditSetupAction");
	 * try { int total = 0; int total1 = 0; int total2 = 0; int total3 = 0; int
	 * total4 = 0; int total5 = 0; BasicDBObject payIdQuery = new BasicDBObject();
	 * List<BasicDBObject> subMerchantConditionList = new
	 * ArrayList<BasicDBObject>(); BasicDBObject subMerchantQuery = new
	 * BasicDBObject(); BasicDBObject resellerQuery = new BasicDBObject();
	 * BasicDBObject flagQuery = new BasicDBObject(); String payId =
	 * merchant.getPayId(); String resellerId = merchant.getResellerId();
	 * MongoDatabase dbIns = mongoInstance.getDB(); MongoCollection<Document> coll =
	 * dbIns .getCollection(PropertiesManager.propertiesMap.get(prefix +
	 * Constants.COLLECTION_NAME.getValue())); MongoCollection<Document> coll1 =
	 * dbIns.getCollection( PropertiesManager.propertiesMap.get(prefix +
	 * Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
	 * MongoCollection<Document> coll2 = dbIns
	 * .getCollection(PropertiesManager.propertiesMap.get(prefix +
	 * Constants.E_COLLECTION.getValue())); MongoCollection<Document> coll3 =
	 * dbIns.getCollection( PropertiesManager.propertiesMap.get(prefix +
	 * Constants.IMPS_SETTlEMENT_COLLECTION.getValue())); MongoCollection<Document>
	 * coll4 = dbIns.getCollection( PropertiesManager.propertiesMap.get(prefix +
	 * Constants.CLOSING_AMOUNT_COLLECTION.getValue())); MongoCollection<Document>
	 * coll5 = dbIns.getCollection( PropertiesManager.propertiesMap.get(prefix +
	 * Constants.COMPOSITE_BENE_COLLECTION.getValue())); List<Merchants>
	 * subMerchantList = userDao.getSubMerchantListBySuperPayId(payId); if
	 * (StringUtils.isNotBlank(payId)) { if (merchant.isSuperMerchant() == true) {
	 * for (Merchants subMerchant : subMerchantList) {
	 * 
	 * subMerchantConditionList .add((new
	 * BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchant.getPayId())));
	 * }
	 * 
	 * subMerchantQuery.append("$or", subMerchantConditionList); } else {
	 * payIdQuery.append(FieldType.PAY_ID.getName(), payId); } } if
	 * (StringUtils.isNotBlank(resellerId)) { flagQuery.append("$ne", resellerId);
	 * resellerQuery.append(FieldType.RESELLER_ID.getName(), flagQuery); } else {
	 * flagQuery.append("$ne", "null");
	 * resellerQuery.append(FieldType.RESELLER_ID.getName(), flagQuery); }
	 * List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();
	 * 
	 * if (!subMerchantQuery.isEmpty()) { finalList.add(subMerchantQuery); }
	 * 
	 * if (!payIdQuery.isEmpty()) { finalList.add(payIdQuery); }
	 * 
	 * if (!resellerQuery.isEmpty()) { finalList.add(resellerQuery); } BasicDBObject
	 * finalquery = new BasicDBObject("$and", finalList); total = (int)
	 * coll.count(finalquery); total1 = (int) coll1.count(finalquery); total2 =
	 * (int) coll2.count(finalquery); total3 = (int) coll3.count(finalquery); total4
	 * = (int) coll4.count(finalquery); total5 = (int) coll5.count(finalquery); if
	 * (total != 0 || total1 != 0 || total2 != 0 || total3 != 0 || total4 != 0 ||
	 * total5 != 0) { BasicDBObject match = new BasicDBObject("$match", finalquery);
	 * BasicDBObject sort = new BasicDBObject("$sort", new
	 * BasicDBObject("CREATE_DATE", -1)); List<BasicDBObject> pipeline =
	 * Arrays.asList(match, sort); AggregateIterable<Document> output =
	 * coll.aggregate(pipeline); output.allowDiskUse(true); MongoCursor<Document>
	 * cursor = output.iterator(); AggregateIterable<Document> output2 =
	 * coll2.aggregate(pipeline); output2.allowDiskUse(true); MongoCursor<Document>
	 * cursor2 = output2.iterator(); AggregateIterable<Document> output3 =
	 * coll3.aggregate(pipeline); output3.allowDiskUse(true); MongoCursor<Document>
	 * cursor3 = output3.iterator(); AggregateIterable<Document> output4 =
	 * coll4.aggregate(pipeline); output4.allowDiskUse(true); MongoCursor<Document>
	 * cursor4 = output4.iterator(); AggregateIterable<Document> output5 =
	 * coll5.aggregate(pipeline); output5.allowDiskUse(true); MongoCursor<Document>
	 * cursor5 = output5.iterator(); while (cursor.hasNext()) { Document dbobj =
	 * cursor.next(); Bson filter; if
	 * (StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))
	 * ) { filter = new Document(FieldType.SUB_MERCHANT_ID.getName(),
	 * dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))
	 * .append(FieldType.PAY_ID.getName(),
	 * dbobj.getString(FieldType.PAY_ID.getName()))
	 * .append(FieldType.TXN_ID.getName(),
	 * dbobj.getString(FieldType.TXN_ID.getName())); } else { filter = new
	 * Document(FieldType.PAY_ID.getName(),
	 * dbobj.getString(FieldType.PAY_ID.getName()))
	 * .append(FieldType.TXN_ID.getName(),
	 * dbobj.getString(FieldType.TXN_ID.getName())); } Bson newValue; if
	 * (StringUtils.isNotBlank(merchant.getResellerId())) { newValue = new
	 * Document(FieldType.RESELLER_ID.getName(), merchant.getResellerId()); } else {
	 * newValue = new Document(FieldType.RESELLER_ID.getName(), "null"); }
	 * 
	 * Bson updateOperationDocument = new Document("$set", newValue); if (total !=
	 * 0) { coll.updateOne(filter, updateOperationDocument); } if (total1 != 0) {
	 * coll1.updateOne(filter, updateOperationDocument); } }
	 * 
	 * while (cursor2.hasNext()) { Document dbobj = cursor2.next(); Bson filter; if
	 * (StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))
	 * ) { filter = new Document(FieldType.SUB_MERCHANT_ID.getName(),
	 * dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))
	 * .append(FieldType.PAY_ID.getName(),
	 * dbobj.getString(FieldType.PAY_ID.getName()))
	 * .append(FieldType.TXN_ID.getName(),
	 * dbobj.getString(FieldType.TXN_ID.getName())); } else { filter = new
	 * Document(FieldType.PAY_ID.getName(),
	 * dbobj.getString(FieldType.PAY_ID.getName()))
	 * .append(FieldType.TXN_ID.getName(),
	 * dbobj.getString(FieldType.TXN_ID.getName())); } Bson newValue; if
	 * (StringUtils.isNotBlank(merchant.getResellerId())) { newValue = new
	 * Document(FieldType.RESELLER_ID.getName(), merchant.getResellerId()); } else {
	 * newValue = new Document(FieldType.RESELLER_ID.getName(), "null"); }
	 * 
	 * Bson updateOperationDocument = new Document("$set", newValue); if (total2 !=
	 * 0) { coll2.updateOne(filter, updateOperationDocument); } }
	 * 
	 * while (cursor3.hasNext()) { Document dbobj = cursor3.next(); Bson filter; if
	 * (StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))
	 * ) { filter = new Document(FieldType.SUB_MERCHANT_ID.getName(),
	 * dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))
	 * .append(FieldType.PAY_ID.getName(),
	 * dbobj.getString(FieldType.PAY_ID.getName()))
	 * .append(FieldType.TXN_ID.getName(),
	 * dbobj.getString(FieldType.TXN_ID.getName())); } else { filter = new
	 * Document(FieldType.PAY_ID.getName(),
	 * dbobj.getString(FieldType.PAY_ID.getName()))
	 * .append(FieldType.TXN_ID.getName(),
	 * dbobj.getString(FieldType.TXN_ID.getName())); } Bson newValue; if
	 * (StringUtils.isNotBlank(merchant.getResellerId())) { newValue = new
	 * Document(FieldType.RESELLER_ID.getName(), merchant.getResellerId()); } else {
	 * newValue = new Document(FieldType.RESELLER_ID.getName(), "null"); }
	 * 
	 * Bson updateOperationDocument = new Document("$set", newValue); if (total3 !=
	 * 0) { coll3.updateOne(filter, updateOperationDocument); } }
	 * 
	 * while (cursor4.hasNext()) { Document dbobj = cursor4.next(); Bson filter; if
	 * (StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))
	 * ) { filter = new Document(FieldType.SUB_MERCHANT_ID.getName(),
	 * dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))
	 * .append(FieldType.PAY_ID.getName(),
	 * dbobj.getString(FieldType.PAY_ID.getName()))
	 * .append(FieldType.DATE_INDEX.getName(),
	 * dbobj.getString(FieldType.DATE_INDEX.getName())); } else { filter = new
	 * Document(FieldType.PAY_ID.getName(),
	 * dbobj.getString(FieldType.PAY_ID.getName()))
	 * .append(FieldType.DATE_INDEX.getName(),
	 * dbobj.getString(FieldType.DATE_INDEX.getName())); } Bson newValue; if
	 * (StringUtils.isNotBlank(merchant.getResellerId())) { newValue = new
	 * Document(FieldType.RESELLER_ID.getName(), merchant.getResellerId()); } else {
	 * newValue = new Document(FieldType.RESELLER_ID.getName(), "null"); }
	 * 
	 * Bson updateOperationDocument = new Document("$set", newValue); if (total4 !=
	 * 0) { coll4.updateOne(filter, updateOperationDocument); } } while
	 * (cursor5.hasNext()) { Document dbobj = cursor5.next(); Bson filter; if
	 * (StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))
	 * ) { filter = new Document(FieldType.SUB_MERCHANT_ID.getName(),
	 * dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))
	 * .append(FieldType.PAY_ID.getName(),
	 * dbobj.getString(FieldType.PAY_ID.getName()))
	 * .append(FieldType.TXN_ID.getName(),
	 * dbobj.getString(FieldType.TXN_ID.getName())); } else { filter = new
	 * Document(FieldType.PAY_ID.getName(),
	 * dbobj.getString(FieldType.PAY_ID.getName()))
	 * .append(FieldType.TXN_ID.getName(),
	 * dbobj.getString(FieldType.TXN_ID.getName())); } Bson newValue; if
	 * (StringUtils.isNotBlank(merchant.getResellerId())) { newValue = new
	 * Document(FieldType.RESELLER_ID.getName(), merchant.getResellerId()); } else {
	 * newValue = new Document(FieldType.RESELLER_ID.getName(), "null"); }
	 * 
	 * Bson updateOperationDocument = new Document("$set", newValue); if (total5 !=
	 * 0) { coll5.updateOne(filter, updateOperationDocument); } } } } catch
	 * (Exception e) {
	 * logger.error("Exception cought in  addResellerIdInTransaction ", e); } }
	 */

	public void addResellerIdInTransaction(User merchant) {
		logger.info("Inside addResellerIdInTransaction(), MerchantAccountEditSetupAction");
		try {
			int total = 0;
			int total1 = 0;
			int total2 = 0;
			int total3 = 0;
			int total4 = 0;
			int total5 = 0;
			BasicDBObject payIdQuery = new BasicDBObject();
			List<BasicDBObject> subMerchantConditionList = new ArrayList<BasicDBObject>();
			BasicDBObject subMerchantQuery = new BasicDBObject();
			BasicDBObject resellerQuery = new BasicDBObject();
			BasicDBObject flagQuery = new BasicDBObject();
			String payId = merchant.getPayId();
			logger.info("Pay Id = " + payId);
			String resellerId = merchant.getResellerId();
			logger.info("Reseller Id = " + resellerId);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCollection<Document> coll1 = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
			MongoCollection<Document> coll2 = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.E_COLLECTION.getValue()));
			MongoCollection<Document> coll3 = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			MongoCollection<Document> coll4 = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CLOSING_AMOUNT_COLLECTION.getValue()));
			MongoCollection<Document> coll5 = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.COMPOSITE_BENE_COLLECTION.getValue()));
			List<Merchants> subMerchantList = userDao.getSubMerchantListBySuperPayId(payId);
			if (StringUtils.isNotBlank(payId)) {
				if (merchant.isSuperMerchant() == true) {
					for (Merchants subMerchant : subMerchantList) {

						subMerchantConditionList
								.add((new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchant.getPayId())));
					}

					subMerchantQuery.append("$or", subMerchantConditionList);
				} else {
					payIdQuery.append(FieldType.PAY_ID.getName(), payId);
				}
			}
			if (StringUtils.isNotBlank(resellerId)) {
				flagQuery.append("$ne", resellerId);
				resellerQuery.append(FieldType.RESELLER_ID.getName(), flagQuery);
			} else {
				flagQuery.append("$ne", null);
				resellerQuery.append(FieldType.RESELLER_ID.getName(), flagQuery);
			}
			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

			if (!subMerchantQuery.isEmpty()) {
				finalList.add(subMerchantQuery);
			}

			if (!payIdQuery.isEmpty()) {
				finalList.add(payIdQuery);
			}

			if (!resellerQuery.isEmpty()) {
				finalList.add(resellerQuery);
			}
			BasicDBObject finalquery = new BasicDBObject("$and", finalList);
			total = (int) coll.count(finalquery);
			total1 = (int) coll1.count(finalquery);
			total2 = (int) coll2.count(finalquery);
			total3 = (int) coll3.count(finalquery);
			total4 = (int) coll4.count(finalquery);
			total5 = (int) coll5.count(finalquery);
			if (total != 0 || total1 != 0 || total2 != 0 || total3 != 0 || total4 != 0 || total5 != 0) {

				Bson filter = null;
				if (StringUtils.isNotBlank(payId)) {
					filter = new Document(FieldType.PAY_ID.getName(), payId);
				}
				Bson newValue = null;
				if (StringUtils.isNotBlank(merchant.getResellerId())) {
					newValue = new Document(FieldType.RESELLER_ID.getName(), merchant.getResellerId());
				} else {
					newValue = new Document(FieldType.RESELLER_ID.getName(), null);
				}

				Bson updateOperationDocument = new Document("$set", newValue);

				if (total != 0) {
					coll.updateMany(filter, updateOperationDocument);
				}
				if (total1 != 0) {
					coll1.updateMany(filter, updateOperationDocument);
				}
				if (total2 != 0) {
					coll2.updateMany(filter, updateOperationDocument);
				}
				if (total3 != 0) {
					coll3.updateMany(filter, updateOperationDocument);
				}
				if (total4 != 0) {
					coll4.updateMany(filter, updateOperationDocument);
				}
				if (total5 != 0) {
					coll5.updateMany(filter, updateOperationDocument);
				}
			}
		} catch (Exception e) {
			logger.error("Exception cought in  addResellerIdInTransaction ", e);
		}
	}

	public void addSuperMerchantId(User merchant) {
		logger.info("Inside addSuperMerchantId(), MerchantAccountEditSetupAction");
		try {
			String payId = merchant.getPayId();
			String superMerchantId = merchant.getSuperMerchantId();
			logger.info("Pay Id = " + payId);
			logger.info("Sub Merchant Id = " + superMerchantId);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCollection<Document> coll1 = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
			MongoCollection<Document> coll2 = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.E_COLLECTION.getValue()));
			MongoCollection<Document> coll3 = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			MongoCollection<Document> coll4 = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CLOSING_AMOUNT_COLLECTION.getValue()));
			MongoCollection<Document> coll5 = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.COMPOSITE_BENE_COLLECTION.getValue()));
			Bson filter = null;
			if (StringUtils.isNotBlank(payId)) {
				filter = new Document(FieldType.PAY_ID.getName(), payId);
			}
			Bson newValue = null;
			if (StringUtils.isNotBlank(payId) && StringUtils.isNotBlank(superMerchantId)) {
				newValue = new Document(FieldType.PAY_ID.getName(), superMerchantId)
						.append(FieldType.SUB_MERCHANT_ID.getName(), payId);
			}

			Bson updateOperationDocument = new Document("$set", newValue);
			coll.updateMany(filter, updateOperationDocument);
			coll1.updateMany(filter, updateOperationDocument);
			coll2.updateMany(filter, updateOperationDocument);
			coll3.updateMany(filter, updateOperationDocument);
			coll4.updateMany(filter, updateOperationDocument);
			coll5.updateMany(filter, updateOperationDocument);
		} catch (Exception e) {
			logger.error("Exception cought in  addSuperMerchantId ", e);
		}
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

	public List<Account> getAccountList() {
		return accountList;
	}

	public void setAccountList(List<Account> accountList) {
		this.accountList = accountList;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public MerchantProcessingApplication getModel() {
		return MPAData;
	}

	public Map<String, String> getCurrencyMap() {
		return currencyMap;
	}

	public void setCurrencyMap(Map<String, String> currencyMap) {
		this.currencyMap = currencyMap;
	}

	public Map<String, List<AccountCurrency>> getAccountCurrencyMap() {
		return accountCurrencyMap;
	}

	public void setAccountCurrencyMap(Map<String, List<AccountCurrency>> accountCurrencyMap) {
		this.accountCurrencyMap = accountCurrencyMap;
	}

	public List<AccountCurrency> getAccountCurrencyList() {
		return accountCurrencyList;
	}

	public void setAccountCurrencyList(List<AccountCurrency> accountCurrencyList) {
		this.accountCurrencyList = accountCurrencyList;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	@Override
	public void setServletRequest(HttpServletRequest request) {
		this.request = request;

	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public String getDefaultCurrency() {
		return defaultCurrency;
	}

	public void setDefaultCurrency(String defaultCurrency) {
		this.defaultCurrency = defaultCurrency;
	}

	public Map<String, String> getIndustryTypesList() {
		return industryTypesList;
	}

	public void setIndustryTypesList(Map<String, String> industryTypesList) {
		this.industryTypesList = industryTypesList;
	}

	public ResponseObject getResponseObject() {
		return responseObject;
	}

	public void setResponseObject(ResponseObject responseObject) {
		this.responseObject = responseObject;
	}

	public MerchantProcessingApplication getMPAData() {
		return MPAData;
	}

	public void setMPAData(MerchantProcessingApplication mPAData) {
		MPAData = mPAData;
	}

	public String getAnnualTurnover() {
		return annualTurnover;
	}

	public void setAnnualTurnover(String annualTurnover) {
		this.annualTurnover = annualTurnover;
	}

	public String getMerchantLogo() {
		return merchantLogo;
	}

	public void setMerchantLogo(String merchantLogo) {
		this.merchantLogo = merchantLogo;
	}

	public Boolean getShowDownload() {
		return showDownload;
	}

	public void setShowDownload(Boolean showDownload) {
		this.showDownload = showDownload;
	}

	public String getEncKey() {
		return encKey;
	}

	public void setEncKey(String encKey) {
		this.encKey = encKey;
	}

//	public UserAudit getUserAudit() {
//		return userAudit;
//	}
//
//	public void setUserAudit(UserAudit userAudit) {
//		this.userAudit = userAudit;
//	}

}