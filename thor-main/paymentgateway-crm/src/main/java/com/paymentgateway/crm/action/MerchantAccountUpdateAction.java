package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.paymentgateway.commons.dao.MPADao;
import com.paymentgateway.commons.dao.PendingMappingRequestDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.ChargingDetailsDao;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.user.Payment;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.SurchargeDetailsDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.BinCountryMapperType;
import com.paymentgateway.commons.util.BusinessType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.ModeType;
import com.paymentgateway.commons.util.SaltFactory;
import com.paymentgateway.commons.util.UserStatusType;
import com.paymentgateway.crm.actionBeans.CurrencyMapProvider;
import com.paymentgateway.crm.actionBeans.IndustryTypeCategoryProvider;
import com.paymentgateway.crm.actionBeans.MerchantRecordUpdater;
import com.paymentgateway.pg.core.fraudPrevention.util.AccountPasswordScrambler;

/**
 * @author Chandan, Puneet, Neeraj, Rahul
 *
 */
public class MerchantAccountUpdateAction extends AbstractSecureAction
		implements ServletRequestAware, ModelDriven<User> {

	@Autowired
	private ChargingDetailsDao chargingDetailsDao;
	@Autowired
	private SurchargeDetailsDao surchargeDetailsDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private CurrencyMapProvider currencyMapProvider;

	@Autowired
	private MerchantRecordUpdater merchantRecordUpdater;

	@Autowired
	private AccountPasswordScrambler accPwdScrambler;

	@Autowired
	private CrmValidator crmValidator;
	
	@Autowired
	IndustryTypeCategoryProvider industryTypeCategoryProvider;
	
	@Autowired
	private PendingMappingRequestDao pendingMappingRequestDao;
	
	@Autowired
	private UserSettingDao userSettingDao;


	@Autowired
	private MPADao mpaDao;

	private static Logger logger = LoggerFactory.getLogger(MerchantAccountUpdateAction.class.getName());
	private static final long serialVersionUID = -7290087594947995464L;
	private User user = new User();
	private MerchantProcessingApplication MPAData = new MerchantProcessingApplication();
	private List<Account> accountList = new ArrayList<Account>();
	private Map<String, List<AccountCurrency>> accountCurrencyMap = new HashMap<String, List<AccountCurrency>>();
	private List<AccountCurrency> accountCurrencyList = new ArrayList<AccountCurrency>();
	private String salt;
	private HttpServletRequest request;
	private String defaultCurrency;
	private String annualTurnover;
	private Map<String, String> currencyMap = new LinkedHashMap<String, String>();
	private Map<String, String> industryTypesList = new TreeMap<String, String>();
	private ResponseObject responseObject = new ResponseObject();

	public String saveAction() {
		Date date = new Date();

		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));

		try {
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			setSalt(SaltFactory.getSaltProperty(user));
			Map<String, String> industryCategoryLinkedMap = BusinessType.getIndustryCategoryList();
			industryTypesList.putAll(industryCategoryLinkedMap);
			user.setUserType(sessionUser.getUserType());
			user.setParentPayId(sessionUser.getParentPayId());
			user.setDefaultCurrency(sessionUser.getDefaultCurrency());
			currencyMap = currencyMapProvider.currencyMap(user);
			addPaymentMapped(user, currencyMap);
			
			UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());
				if((MPAData=mpaDao.fetchMPADataByPayId(user.getPayId()))!= null){
					setMPAData(MPAData);
				}else{
					if(merchantSettings.isAllowPartSettle()){
						MerchantProcessingApplication MPAData1=new MerchantProcessingApplication();
						MPAData1.setValidCin(false);
						MPAData1.setValidCompanyName(false);
						MPAData1.setValidDirector1Pan(false);
						MPAData1.setValidDirector2Pan(false);
						MPAData1.setValidPan(false);
						MPAData1.setCinAttempts(0);
						MPAData1.setPayId(user.getPayId());
						MPAData1.setAnnualTurnover(annualTurnover);
						mpaDao.create(MPAData1);
						setMPAData(MPAData1);
					}else{
						setMPAData(MPAData);
					}
				}
			
			
			setIndustryTypesList(BusinessType.getIndustryCategoryList());
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				if (user.getUserStatus().toString().equals(UserStatusType.ACTIVE.getStatus().toString())) {
					user.setActivationDate(date);
					if (!(chargingDetailsDao.isChargingDetailsSet(user.getPayId())
							|| surchargeDetailsDao.isSurchargeDetailsSet(user.getPayId()))) {
						responseObject.setResponseCode("1111");
						responseObject.setResponseMessage(CrmFieldConstants.USER_CHARGINGDETAILS_NOT_SET_MSG.getValue());
						addActionMessage(responseObject.getResponseMessage());
						return CrmFieldConstants.ADMIN.getValue();
					}
				} else if (user.getUserStatus().toString().equals(UserStatusType.SUSPENDED.getStatus().toString())
						|| user.getUserStatus().toString()
								.equals(UserStatusType.TRANSACTION_BLOCKED.getStatus().toString())) {
					user.setActivationDate(null);
				}
				
				if(StringUtils.isBlank(user.getSuperMerchantId())){
					if (!pendingMappingRequestDao.findActiveMappingByEmailIdForActiveStatus(user.getEmailId())) {
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
		}

		catch (Exception e) {
			logger.error("Exception " , e);
		}
		return user;

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
		boolean hdfcAcquirerFlag = false;
		boolean yesBankAcquirerFlag = false;
		boolean direcpayAcquirerFlag = false;
		boolean modeTypeFlag = false;
		boolean yesBankNetbnkingPrimaryFlag = false;
		boolean direcpayNetbankingPrimaryFlag = false;
		boolean direcpayExistFlag = false;
		boolean yesBankExistFlag = false;
		boolean hdfcBankExistFlag = false;
		boolean americanExpressAcquirerFlag = false;
		boolean americanExpressExistFlag = false;

		if ((crmValidator.validateBlankField(user.getFirstName()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.FIRSTNAME, user.getFirstName()))) {
			addFieldError(CrmFieldType.FIRSTNAME.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getLastName()))) {

		} else if (!(crmValidator.validateField(CrmFieldType.LASTNAME, user.getLastName()))) {
			addFieldError(CrmFieldType.LASTNAME.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getCompanyName()))) {

		} else if (!(crmValidator.validateField(CrmFieldType.COMPANY_NAME, user.getCompanyName()))) {
			addFieldError(CrmFieldType.COMPANY_NAME.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if (crmValidator.validateBlankField(user.getIndustryCategory())) {
		} else if (!(crmValidator.validateField(CrmFieldType.INDUSTRY_CATEGORY, user.getIndustryCategory()))) {
			addFieldError(CrmFieldType.INDUSTRY_CATEGORY.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (crmValidator.validateBlankField(user.getIndustrySubCategory())) {
		} else if (!(crmValidator.validateField(CrmFieldType.INDUSTRY_SUB_CATEGORY, user.getIndustrySubCategory()))) {
			addFieldError(CrmFieldType.INDUSTRY_SUB_CATEGORY.getName(),
					ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getTelephoneNo()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.TELEPHONE_NO, user.getTelephoneNo()))) {
			addFieldError(CrmFieldType.TELEPHONE_NO.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getAddress()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.ADDRESS, user.getAddress()))) {
			addFieldError(CrmFieldType.ADDRESS.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getCity()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.CITY, user.getCity()))) {
			addFieldError(CrmFieldType.CITY.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getState()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.STATE, user.getState()))) {
			addFieldError(CrmFieldType.STATE.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getCountry()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.COUNTRY, user.getCountry()))) {
			addFieldError(CrmFieldType.COUNTRY.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getPostalCode()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.POSTALCODE, user.getPostalCode()))) {
			addFieldError(CrmFieldType.POSTALCODE.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getBankName()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.BANK_NAME, user.getBankName()))) {
			addFieldError(CrmFieldType.BANK_NAME.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getIfscCode()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.IFSC_CODE, user.getIfscCode()))) {
			addFieldError(CrmFieldType.IFSC_CODE.getName(), ErrorType.IFSC_CODE.getInternalMessage());
		}
		if ((crmValidator.validateBlankField(user.getAccHolderName()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.ACC_HOLDER_NAME, user.getAccHolderName()))) {
			addFieldError(CrmFieldType.ACC_HOLDER_NAME.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getCurrency()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.CURRENCY, user.getCurrency()))) {
			addFieldError(CrmFieldType.CURRENCY.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getBranchName()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.BRANCH_NAME, user.getBranchName()))) {
			addFieldError(CrmFieldType.BRANCH_NAME.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getBusinessName()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.BUSINESS_NAME, user.getBusinessName()))) {
			addFieldError(CrmFieldType.BUSINESS_NAME.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getComments()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.COMMENTS, user.getComments()))) {
			addFieldError(CrmFieldType.COMMENTS.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getPanCard()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.PANCARD, user.getPanCard()))) {
			addFieldError(CrmFieldType.PANCARD.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getAccountNo()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.ACCOUNT_NO, user.getAccountNo()))) {
			addFieldError(CrmFieldType.ACCOUNT_NO.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getWebsite()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.WEBSITE, user.getWebsite()))) {
			addFieldError(CrmFieldType.WEBSITE.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getOrganisationType()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.ORGANIZATIONTYPE, user.getOrganisationType()))) {
			addFieldError(CrmFieldType.ORGANIZATIONTYPE.getName(),
					crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getMultiCurrency()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.MULTICURRENCY, user.getMultiCurrency()))) {
			addFieldError(CrmFieldType.MULTICURRENCY.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getBusinessModel()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.BUSINESSMODEL, user.getBusinessModel()))) {
			addFieldError(CrmFieldType.BUSINESSMODEL.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getOperationAddress()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.OPERATIONADDRESS, user.getOperationAddress()))) {
			addFieldError(CrmFieldType.OPERATIONADDRESS.getName(),
					crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getOperationCity()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.CITY, user.getOperationCity()))) {
			addFieldError(CrmFieldType.CITY.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getOperationState()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.STATE, user.getOperationState()))) {
			addFieldError(CrmFieldType.STATE.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getOperationPostalCode()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.OPERATION_POSTAL_CODE, user.getOperationPostalCode()))) {
			addFieldError(CrmFieldType.OPERATION_POSTAL_CODE.getName(),
					crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getCin()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.CIN, user.getCin()))) {
			addFieldError(CrmFieldType.CIN.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getPan()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.PAN, user.getPan()))) {
			addFieldError(CrmFieldType.PAN.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getPanName()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.PANNAME, user.getPanName()))) {
			addFieldError(CrmFieldType.PANNAME.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getNoOfTransactions()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.NO_OF_TRANSACTIONS, user.getNoOfTransactions()))) {
			addFieldError(CrmFieldType.NO_OF_TRANSACTIONS.getName(),
					crmValidator.getResonseObject().getResponseMessage());
		}

		if ((crmValidator.validateBlankField(user.getAmountOfTransactions()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.AMOUNT_OF_TRANSACTIONS, user.getAmountOfTransactions()))) {
			addFieldError(CrmFieldType.AMOUNT_OF_TRANSACTIONS.getName(),
					crmValidator.getResonseObject().getResponseMessage());
		}

		if ((crmValidator.validateBlankField(user.getDateOfEstablishment()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.DATE_OF_ESTABLISHMENT, user.getDateOfEstablishment()))) {
			addFieldError(CrmFieldType.DATE_OF_ESTABLISHMENT.getName(),
					crmValidator.getResonseObject().getResponseMessage());
		}

		if ((crmValidator.validateBlankField(user.getAccountValidationKey()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.ACCOUNT_VALIDATION_KEY, user.getAccountValidationKey()))) {
			addFieldError(CrmFieldType.ACCOUNT_VALIDATION_KEY.getName(),
					crmValidator.getResonseObject().getResponseMessage());
		}

		if ((crmValidator.validateBlankField(user.getUploadePhoto()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.UPLOADE_PHOTO, user.getUploadePhoto()))) {
			addFieldError(CrmFieldType.UPLOADE_PHOTO.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getUploadedPanCard()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.UPLOADE_PAN_CARD, user.getUploadedPanCard()))) {
			addFieldError(CrmFieldType.UPLOADE_PAN_CARD.getName(),
					crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getUploadedPhotoIdProof()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.UPLOADE_PHOTOID_PROOF, user.getUploadedPhotoIdProof()))) {
			addFieldError(CrmFieldType.UPLOADE_PHOTOID_PROOF.getName(),
					crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getUploadedContractDocument()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.UPLOADE_CONTRACT_DOCUMENT,
				user.getUploadedContractDocument()))) {
			addFieldError(CrmFieldType.UPLOADE_CONTRACT_DOCUMENT.getName(),
					crmValidator.getResonseObject().getResponseMessage());
		}
		if (crmValidator.validateBlankField(user.getTransactionEmailId())) {
		} else if (!(crmValidator.isValidBatchEmailId(user.getTransactionEmailId())
				|| (crmValidator.isValidEmailId(user.getTransactionEmailId())))) {
			addFieldError(CrmFieldType.TRANSACTION_EMAIL_ID.getName(),
					crmValidator.getResonseObject().getResponseMessage());
		}

		if (crmValidator.validateBlankField(user.getEmailId())) {
			addFieldError(CrmFieldType.EMAILID.getName(), crmValidator.getResonseObject().getResponseMessage());
		} else if (!(crmValidator.isValidEmailId(user.getEmailId()))) {
			addFieldError(CrmFieldType.EMAILID.getName(), crmValidator.getResonseObject().getResponseMessage());
		}

		if ((crmValidator.validateBlankField(user.getContactPerson()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.CONTACT_PERSON, user.getContactPerson()))) {
			addFieldError(CrmFieldType.CONTACT_PERSON.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getPayId()))) {
			addFieldError(CrmFieldType.PAY_ID.getName(), crmValidator.getResonseObject().getResponseMessage());
		} else if (!(crmValidator.validateField(CrmFieldType.PAY_ID, user.getPayId()))) {
			addFieldError(CrmFieldType.PAY_ID.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getPassword()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.PASSWORD, user.getPassword()))) {
			addFieldError(CrmFieldType.PASSWORD.getName(), crmValidator.getResonseObject().getResponseMessage());
		}

		if ((crmValidator.validateBlankField(user.getParentPayId()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.PARENT_PAY_ID, user.getParentPayId()))) {
			addFieldError(CrmFieldType.PARENT_PAY_ID.getName(), crmValidator.getResonseObject().getResponseMessage());
		}

		if ((crmValidator.validateBlankField(user.getWhiteListIpAddress()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.WHITE_LIST_IPADDRES, user.getWhiteListIpAddress()))) {
			addFieldError(CrmFieldType.WHITE_LIST_IPADDRES.getName(),
					crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getFax()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.FAX, user.getFax()))) {
			addFieldError(CrmFieldType.FAX.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getMobile()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.MOBILE, user.getMobile()))) {
			addFieldError(CrmFieldType.MOBILE.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getProductDetail()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.PRODUCT_DETAIL, user.getProductDetail()))) {
			addFieldError(CrmFieldType.PRODUCT_DETAIL.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getResellerId()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.RESELLER_ID, user.getResellerId()))) {
			addFieldError(CrmFieldType.RESELLER_ID.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getMerchantType()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.MERCHANT_TYPE, user.getMerchantType()))) {
			addFieldError(CrmFieldType.MERCHANT_TYPE.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getBranchName()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.BRANCH_NAME, user.getBranchName()))) {
			addFieldError(CrmFieldType.BRANCH_NAME.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		if ((crmValidator.validateBlankField(user.getDefaultCurrency()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.DEFAULT_CURRENCY, user.getDefaultCurrency()))) {
			addFieldError(CrmFieldType.DEFAULT_CURRENCY.getName(),
					crmValidator.getResonseObject().getResponseMessage());
		}
//		if ((crmValidator.validateBlankField(user.getMCC()))) {
//		} else if (!(crmValidator.validateField(CrmFieldType.MCC, user.getMCC()))) {
//			addFieldError(CrmFieldType.MCC.getName(), crmValidator.getResonseObject().getResponseMessage());
//		}
		if ((crmValidator.validateBlankField(user.getAmexSellerId()))) {
		} else if (!(crmValidator.validateField(CrmFieldType.AMEX_SELLER_ID, user.getAmexSellerId()))) {
			addFieldError(CrmFieldType.AMEX_SELLER_ID.getName(), crmValidator.getResonseObject().getResponseMessage());
		}
		/*
		 * for (Account accountFE : accountList) { if
		 * ((crmValidator.validateBlankField(accountFE.getMerchantId()))) { } else if
		 * (!(crmValidator.validateField(CrmFieldType.MERCHANTID,
		 * accountFE.getMerchantId()))) {
		 * addFieldError(CrmFieldType.MERCHANTID.getName(),
		 * crmValidator.getResonseObject().getResponseMessage()); }
		 * 
		 * if ((crmValidator.validateBlankField(accountFE.getAcquirerPayId()))) { } else
		 * if (!(crmValidator.validateField(CrmFieldType.ACQUIRER_PAYID,
		 * accountFE.getAcquirerPayId()))) {
		 * addFieldError(CrmFieldType.ACQUIRER_PAYID.getName(),
		 * crmValidator.getResonseObject().getResponseMessage()); } if
		 * ((crmValidator.validateBlankField(accountFE.getAcquirerName()))) { } else if
		 * (!(crmValidator.validateField(CrmFieldType.AQCQUIRER_NAME,
		 * accountFE.getAcquirerName()))) {
		 * addFieldError(CrmFieldType.AQCQUIRER_NAME.getName(),
		 * crmValidator.getResonseObject().getResponseMessage()); } if
		 * ((crmValidator.validateBlankField(accountFE.getPassword()))) { } else if
		 * (!(crmValidator.validateField(CrmFieldType.ACCOUNT_PASSWORD,
		 * accountFE.getPassword()))) {
		 * addFieldError(CrmFieldType.ACCOUNT_PASSWORD.getName(),
		 * crmValidator.getResonseObject().getResponseMessage()); } if
		 * ((crmValidator.validateBlankField(accountFE.getTxnKey()))) { } else if
		 * (!(crmValidator.validateField(CrmFieldType.TXN_KEY, accountFE.getTxnKey())))
		 * { addFieldError(CrmFieldType.TXN_KEY.getName(),
		 * crmValidator.getResonseObject().getResponseMessage()); } }
		 */
		/*
		 * for (AccountCurrency accountCurrency : accountCurrencyList) { if
		 * ((crmValidator.validateBlankField(accountCurrency.getAcqPayId()))) { } else
		 * if (!(crmValidator.validateField(CrmFieldType.ACQ_PAYID,
		 * accountCurrency.getAcqPayId()))) {
		 * addFieldError(CrmFieldType.ACQ_PAYID.getName(),
		 * crmValidator.getResonseObject().getResponseMessage()); } if
		 * ((crmValidator.validateBlankField(accountCurrency.getCurrencyCode()))) { }
		 * else if (!(crmValidator.validateField(CrmFieldType.CURRENCY,
		 * accountCurrency.getCurrencyCode()))) {
		 * addFieldError(CrmFieldType.CURRENCY.getName(),
		 * crmValidator.getResonseObject().getResponseMessage()); } if
		 * ((crmValidator.validateBlankField(accountCurrency.getMerchantId()))) { } else
		 * if (!(crmValidator.validateField(CrmFieldType.MERCHANTID,
		 * accountCurrency.getMerchantId()))) {
		 * addFieldError(CrmFieldType.MERCHANTID.getName(),
		 * crmValidator.getResonseObject().getResponseMessage()); } if
		 * ((crmValidator.validateBlankField(accountCurrency.getPassword()))) { } else
		 * if (!(crmValidator.validateField(CrmFieldType.ACCOUNT_PASSWORD,
		 * accountCurrency.getPassword()))) {
		 * addFieldError(CrmFieldType.ACCOUNT_PASSWORD.getName(),
		 * crmValidator.getResonseObject().getResponseMessage()); } if
		 * ((crmValidator.validateBlankField(accountCurrency.getTxnKey()))) { } else if
		 * (!(crmValidator.validateField(CrmFieldType.TXN_KEY,
		 * accountCurrency.getTxnKey()))) {
		 * addFieldError(CrmFieldType.TXN_KEY.getName(),
		 * crmValidator.getResonseObject().getResponseMessage()); } Map<String,String>
		 * industryCategoryLinkedMap = BusinessType.getIndustryCategoryList();
		 * industryTypesList.putAll(industryCategoryLinkedMap); }
		 */

		// Validation between check boxes
		/*
		 * for (Account accountFE : accountList) { if
		 * (accountFE.getAcquirerName().equalsIgnoreCase(AcquirerType.CITRUS_PAY.getName
		 * ())) { yesBankExistFlag = true; if (accountFE.isPrimaryStatus() == true) {
		 * yesBankAcquirerFlag = true; } if (accountFE.isPrimaryNetbankingStatus() ==
		 * true) { yesBankNetbnkingPrimaryFlag = true; }
		 * 
		 * if (yesBankAcquirerFlag &&
		 * user.getModeType().getName().equalsIgnoreCase(ModeType.AUTH_CAPTURE.getName()
		 * )) { modeTypeFlag = true; } } if
		 * (accountFE.getAcquirerName().equalsIgnoreCase(AcquirerType.FSS.getName())) {
		 * hdfcBankExistFlag = true; if (accountFE.isPrimaryStatus() == true) {
		 * hdfcAcquirerFlag = true; } } if
		 * (accountFE.getAcquirerName().equalsIgnoreCase(AcquirerType.AMEX.getName())) {
		 * americanExpressExistFlag = true; if (accountFE.isPrimaryStatus() == true) {
		 * americanExpressAcquirerFlag = true; } } if
		 * (accountFE.getAcquirerName().equalsIgnoreCase(AcquirerType.DIREC_PAY.getName(
		 * ))) { direcpayExistFlag = true; if (accountFE.isPrimaryStatus() == true) {
		 * direcpayAcquirerFlag = true; } if (accountFE.isPrimaryNetbankingStatus() ==
		 * true) { direcpayNetbankingPrimaryFlag = true; } } }
		 */
		/*
		 * if ((yesBankExistFlag == true || hdfcBankExistFlag == true ||
		 * americanExpressExistFlag == true) && (!yesBankAcquirerFlag &&
		 * !hdfcAcquirerFlag && !americanExpressAcquirerFlag)) {
		 * addFieldError(CrmFieldType.PAY_ID.getName(),
		 * CrmFieldConstants.SELECT_PRIMARY_CARD.getValue());
		 * addActionMessage(CrmFieldConstants.SELECT_PRIMARY_CARD.getValue()); }
		 */
		/*
		 * if ((direcpayExistFlag == true || yesBankExistFlag == true) &&
		 * (!direcpayNetbankingPrimaryFlag && !yesBankNetbnkingPrimaryFlag)) {
		 * addFieldError(CrmFieldType.PAY_ID.getName(),
		 * CrmFieldConstants.SELECT_ONE_NETBANKING.getValue());
		 * addActionMessage(CrmFieldConstants.SELECT_ONE_NETBANKING.getValue()); }
		 */
		if (modeTypeFlag) {
			addFieldError(CrmFieldType.PAY_ID.getName(), CrmFieldConstants.SELECT_SALE.getValue());
			addActionMessage(CrmFieldConstants.SELECT_SALE.getValue());
		}
		if (yesBankNetbnkingPrimaryFlag && direcpayNetbankingPrimaryFlag
				|| direcpayAcquirerFlag && yesBankAcquirerFlag) {
			addFieldError(CrmFieldType.PAY_ID.getName(), CrmFieldConstants.SELECT_DIRECPAY_YES.getValue());
			addActionMessage(CrmFieldConstants.SELECT_DIRECPAY_YES.getValue());
		}
		/*
		 * if (hdfcAcquirerFlag && yesBankAcquirerFlag) {
		 * addFieldError(CrmFieldType.PAY_ID.getName(),
		 * CrmFieldConstants.SELECT_ONLY_ONE.getValue());
		 * addActionMessage(CrmFieldConstants.SELECT_ONLY_ONE.getValue()); }
		 */

		// TODO validation field password encry
		if (!getFieldErrors().isEmpty()) {
			/*
			 * accountList = updateAccount(accountList, accountCurrencyList); Set<Account>
			 * set = new HashSet<Account>(accountList); user.setAccounts(set);
			 */
			User userFromDb = userDao.getUserClass(user.getPayId());
			float extraRefundLimitDB = userFromDb.getExtraRefundLimit();
			user.setExtraRefundLimit(extraRefundLimitDB);
		}
	}

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

	public User getModel() {
		return user;
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
	

}