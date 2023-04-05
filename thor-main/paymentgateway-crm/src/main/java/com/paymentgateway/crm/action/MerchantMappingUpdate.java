package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.dao.MerchantAcquirerPropertiesDao;
import com.paymentgateway.commons.dao.PendingMappingRequestDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.MerchantAcquirerProperties;
import com.paymentgateway.commons.user.PendingMappingRequest;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.Surcharge;
import com.paymentgateway.commons.user.SurchargeDao;
import com.paymentgateway.commons.user.SurchargeDetails;
import com.paymentgateway.commons.user.SurchargeDetailsDao;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PendingRequestEmailProcessor;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.crm.actionBeans.UserMappingEditor;

/**
 * @author Puneet
 *
 */
public class MerchantMappingUpdate extends AbstractSecureAction {

	@Autowired
	private PendingMappingRequestDao pendingMappingRequestDao;

	@Autowired
	private SurchargeDao surchargeDao;

	@Autowired
	private SurchargeDetailsDao surchargeDetailsDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private CrmValidator validator;

	@Autowired
	private UserMappingEditor userMappingEditor;

	@Autowired
	private PendingRequestEmailProcessor pendingRequestEmailProcessor;

	@Autowired
	private MerchantAcquirerPropertiesDao merchantAcquirerPropertiesDao;

	@Autowired
	private UserSettingDao userSettingDao;

	private static Logger logger = LoggerFactory.getLogger(MerchantMappingUpdate.class.getName());
	private static final long serialVersionUID = -9103516274778187455L;

	private String merchantEmailId;
	private String mapString;
	private String acquirer;
	private String response;
	private String accountCurrencySet;
	private String userType;
	private String emailId;

	private boolean international;
	private boolean domestic;
	private boolean commercial;
	private boolean customer;
	private boolean onUs;
	private boolean offUs;

	public String execute() {

		try {

			StringBuilder permissions = new StringBuilder();
			permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));

			if (userType.equals(UserType.ADMIN.toString())) {

				PendingMappingRequest existingPMR = pendingMappingRequestDao
						.duplicateFindPendingMappingRequest(merchantEmailId, acquirer, mapString);
				PendingMappingRequest existingActivePMR = pendingMappingRequestDao
						.findActiveMappingRequest(merchantEmailId, acquirer);

				if (existingPMR != null) {
					updateMapping(existingPMR, TDRStatus.CANCELLED, "", emailId);
				}
				if (existingActivePMR != null) {
					updateMapping(existingActivePMR, TDRStatus.INACTIVE, "", emailId);
				}

				createNewMappingEntry(TDRStatus.ACTIVE, emailId, emailId);
				if (mapString != null && merchantEmailId != null && acquirer != null) {
					processMapString();
					updateSurchargeMapping();
					updateMerchantAcquirerProperties(merchantEmailId);

					if (userType.equalsIgnoreCase(UserType.SUBADMIN.toString())) {

						pendingRequestEmailProcessor.processMappingEmail("Active", emailId, userType, merchantEmailId,
								"");
					}
					setResponse(ErrorType.MAPPING_SAVED.getResponseMessage());
					if (acquirer.equalsIgnoreCase(AcquirerType.ISGPAY.getCode())) {

						UserSettingData userSettings = userSettingDao
								.fetchDataUsingPayId(userDao.getPayIdByEmailId(merchantEmailId));

						if (userSettings != null) {
							JSONObject jsonObj = (JSONObject) new JSONArray(accountCurrencySet).get(0);
							String merchantId = jsonObj.getString("merchantId");

							userSettings.setIsgPayMerchantID(merchantId);
							userSettingDao.saveOrUpdate(userSettings);
						}
					}

					return SUCCESS;
				}
			} else if (permissions.toString().contains(PermissionType.MERCHANT_MAPPING.getPermission())) {

				PendingMappingRequest existingPMR = pendingMappingRequestDao
						.duplicateFindPendingMappingRequest(merchantEmailId, acquirer, mapString);
				if (existingPMR != null) {

					setResponse(ErrorType.MAPPING_REQUEST_ALREADY_PENDING.getResponseMessage());
					return SUCCESS;
				}

				createNewMappingEntry(TDRStatus.PENDING, emailId, "");

				pendingRequestEmailProcessor.processMappingEmail("Pending", emailId, userType, merchantEmailId,
						PermissionType.CREATE_MERCHANT_MAPPING.getPermission());

				setResponse(ErrorType.MAPPING_SAVED_FOR_APPROVAL.getResponseMessage());

				if (acquirer.equalsIgnoreCase(AcquirerType.ISGPAY.getCode())) {
					UserSettingData userSettings = userSettingDao
							.fetchDataUsingPayId(userDao.getPayIdByEmailId(merchantEmailId));

					if (userSettings != null) {
						JSONObject jsonObj = (JSONObject) new JSONArray(accountCurrencySet).get(0);
						String merchantId = jsonObj.getString("merchantId");
						userSettings.setIsgPayMerchantID(merchantId);
						userSettingDao.saveOrUpdate(userSettings);
					}
				}
				return SUCCESS;

			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
			setResponse(ErrorType.MAPPING_NOT_SAVED.getResponseMessage());
			return SUCCESS;
		}
		setResponse(ErrorType.MAPPING_NOT_SAVED.getResponseMessage());
		return SUCCESS;
	}

	public void updateMerchantAcquirerProperties(String merchantEmailId) {

		Date date = new Date();
		String merchantPayId = userDao.getPayIdByEmailId(merchantEmailId);
		MerchantAcquirerProperties merchantAcquirerProperties = new MerchantAcquirerProperties();

		merchantAcquirerProperties.setMerchantPayId(merchantPayId);
		merchantAcquirerProperties.setAcquirerCode(acquirer);
		merchantAcquirerProperties.setStatus(TDRStatus.ACTIVE);
		merchantAcquirerProperties.setCreateDate(date);
		merchantAcquirerProperties.setUpdateDate(date);

		try {

			/*
			 * if (commercial && customer) {
			 * merchantAcquirerProperties.setCardHolderType(CardHolderType.ALL); } else if
			 * (commercial && !customer) {
			 * merchantAcquirerProperties.setCardHolderType(CardHolderType.COMMERCIAL); }
			 * else if (!commercial && customer) {
			 * merchantAcquirerProperties.setCardHolderType(CardHolderType.CONSUMER); }
			 */

			if (international && domestic) {
				merchantAcquirerProperties.setPaymentsRegion(AccountCurrencyRegion.ALL);
			} else if (international && !domestic) {
				merchantAcquirerProperties.setPaymentsRegion(AccountCurrencyRegion.INTERNATIONAL);
			} else if (!international && domestic) {
				merchantAcquirerProperties.setPaymentsRegion(AccountCurrencyRegion.DOMESTIC);
			} else {
				merchantAcquirerProperties.setStatus(TDRStatus.INACTIVE);
			}

			merchantAcquirerPropertiesDao.addOrUpdateMerchantAcquirerProperties(merchantAcquirerProperties);
		}

		catch (Exception e) {
			logger.error("Exception in updateMerchantAcquirerProperties ", e);
		}

	}

	public void updateMapping(PendingMappingRequest pmr, TDRStatus status, String requestedBy, String processedBy) {

		Date currentDate = new Date();
		try {

			Session session = null;
			session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			Long id = pmr.getId();
			session.load(pmr, pmr.getId());
			PendingMappingRequest pendingRequest = (PendingMappingRequest) session.get(PendingMappingRequest.class, id);
			pendingRequest.setStatus(status);
			pendingRequest.setUpdatedDate(currentDate);
			if (!requestedBy.equalsIgnoreCase("")) {
				pendingRequest.setRequestedBy(requestedBy);
			}
			if (!processedBy.equalsIgnoreCase("")) {
				pendingRequest.setProcessedBy(processedBy);
			}

			session.update(pendingRequest);
			tx.commit();
			session.close();

		} catch (HibernateException e) {
			e.printStackTrace();
		} finally {

		}

	}

	public void updateSurchargeMapping() {

		try {

			String payId = userDao.getPayIdByEmailId(merchantEmailId);
			String acquirerName = AcquirerType.getInstancefromCode(acquirer).getName();

			List<Surcharge> activeSurchargeList = new ArrayList<Surcharge>();
			List<SurchargeDetails> activeSurchargeDetailsList = new ArrayList<SurchargeDetails>();
			Set<String> paymentTypeSet = new HashSet<String>();
			Map<PaymentType, MopType> paymentTypeMopTypeMap = new HashMap<PaymentType, MopType>();

			activeSurchargeList = surchargeDao.findActiveSurchargeListByPayIdAcquirer(payId, acquirerName);
			activeSurchargeDetailsList = surchargeDetailsDao.getActiveSurchargeDetailsByPayId(payId);

			List<String> mapStringlist = new ArrayList<String>(Arrays.asList(mapString.split(",")));
			for (String mapStrings : mapStringlist) {
				String[] tokens = mapStrings.split("-");
				if (tokens[0].equalsIgnoreCase("Credit Card") || tokens[0].equalsIgnoreCase("Debit Card")) {
					PaymentType key = PaymentType.getInstanceIgnoreCase(tokens[0]);
					MopType value = MopType.getmop(tokens[1]);
					paymentTypeMopTypeMap.put(key, value);
				} else if (tokens[0].equalsIgnoreCase("Net Banking")) {
					PaymentType key = PaymentType.getInstanceIgnoreCase(tokens[0]);
					MopType value = MopType.getmop(tokens[1]);
					paymentTypeMopTypeMap.put(key, value);
				}

			}

			for (Surcharge surcharge : activeSurchargeList) {

				boolean isMatch = false;

				for (Map.Entry<PaymentType, MopType> entry : paymentTypeMopTypeMap.entrySet()) {
					if (surcharge.getPaymentType().equals(entry.getKey())
							&& surcharge.getMopType().equals(entry.getValue())) {
						isMatch = true;
					}
				}

				if (isMatch) {
					continue;
				} else {
					deactivateSurcharge(surcharge);
				}

			}

			for (String mapStrings : mapStringlist) {
				String[] tokens = mapStrings.split("-");
				paymentTypeSet.add(tokens[0]);
			}

			for (SurchargeDetails sd : activeSurchargeDetailsList) {

				boolean isMatch = false;

				for (String paymentType : paymentTypeSet) {
					if (sd.getPaymentType().equalsIgnoreCase(paymentType)) {
						isMatch = true;
					}
				}

				if (isMatch) {
					continue;
				} else {
					deactivateSurchargeDetails(sd);
				}
			}

			System.out.println(paymentTypeSet);

		} catch (Exception e) {
			logger.error("Exception occured in MerchantMappingUpdate , updateSurchargeMapping , exception = ", e);
		}
	}

	public void deactivateSurchargeDetails(SurchargeDetails sd) {

		/*
		 * Date currentDate = new Date(); try {
		 * 
		 * Session session = null; session = HibernateSessionProvider.getSession();
		 * Transaction tx = session.beginTransaction(); Long id = sd.getId();
		 * session.load(sd, sd.getId()); SurchargeDetails surchargeDetails =
		 * (SurchargeDetails) session.get(SurchargeDetails.class, id);
		 * surchargeDetails.setStatus(TDRStatus.INACTIVE);
		 * surchargeDetails.setUpdatedDate(currentDate);
		 * surchargeDetails.setProcessedBy(emailId);
		 * 
		 * session.update(surchargeDetails); tx.commit(); session.close();
		 * 
		 * } catch (HibernateException e) { e.printStackTrace(); } finally {
		 * 
		 * }
		 */

	}

	public void deactivateSurcharge(Surcharge sch) {

		/*
		 * Date currentDate = new Date(); try {
		 * 
		 * Session session = null; session = HibernateSessionProvider.getSession();
		 * Transaction tx = session.beginTransaction(); Long id = sch.getId();
		 * session.load(sch, sch.getId()); Surcharge surcharge= (Surcharge)
		 * session.get(Surcharge.class, id); surcharge.setStatus(TDRStatus.INACTIVE);
		 * surcharge.setUpdatedDate(currentDate); surcharge.setProcessedBy(emailId);
		 * 
		 * session.update(surcharge); tx.commit(); session.close();
		 * 
		 * } catch (HibernateException e) { e.printStackTrace(); } finally {
		 * 
		 * }
		 */

	}

	public void createNewMappingEntry(TDRStatus status, String requestedBy, String processedBy) {

		try {

			PendingMappingRequest pmr = new PendingMappingRequest();

			Date date = new Date();
			pmr.setMerchantEmailId(merchantEmailId);
			pmr.setMapString(mapString);
			pmr.setAcquirer(acquirer);
			pmr.setAccountCurrencySet(accountCurrencySet);
			pmr.setCreatedDate(date);
			pmr.setUpdatedDate(date);
			pmr.setStatus(status);
			if (status.equals(TDRStatus.ACTIVE))
				pmr.setRequestBySubAdmin(false);
			else
				pmr.setRequestBySubAdmin(true);

			if (!requestedBy.equalsIgnoreCase("")) {
				pmr.setRequestedBy(requestedBy);
			}

			if (!processedBy.equalsIgnoreCase("")) {
				pmr.setProcessedBy(processedBy);
			}

			pendingMappingRequestDao.create(pmr);

		} catch (Exception e) {
			logger.error("Exception occured in MerchantMappingUpdate , createNewMappingEntry , exception =  ", e);
		}

	}

	private void processMapString() {
		Gson gson = new Gson();
		AccountCurrency[] accountCurrencies = gson.fromJson(accountCurrencySet, AccountCurrency[].class);
		userMappingEditor.decideAccountChange(getMerchantEmailId(), getMapString(), getAcquirer(), accountCurrencies);
	}

	public void validate() {
		Gson gson = new Gson();
		AccountCurrency[] accountCurrencies = gson.fromJson(accountCurrencySet, AccountCurrency[].class);
		if ((validator.validateBlankField(getAcquirer()))) {
		} else if (!validator.validateField(CrmFieldType.ACQUIRER, getAcquirer())) {
			addFieldError(CrmFieldType.ACQUIRER.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
		if ((validator.validateBlankField(getMapString()))) {
		} else if (!validator.validateField(CrmFieldType.MAP_STRING, getMapString())) {
			addFieldError(CrmFieldType.MAP_STRING.getName(), ErrorType.INVALID_FIELD.getResponseMessage());

		}
		if ((validator.validateBlankField(getMerchantEmailId()))) {
		} else if (!validator.validateField(CrmFieldType.MERCHANT_EMAILID, getMerchantEmailId())) {
			addFieldError(CrmFieldType.MERCHANT_EMAILID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());

		}
		if ((validator.validateBlankField(getResponse()))) {
		} else if (!validator.validateField(CrmFieldType.RESPONSE, getResponse())) {
			addFieldError(CrmFieldType.RESPONSE.getName(), ErrorType.INVALID_FIELD.getResponseMessage());

		}
		// AccountCurrency Class validation
		for (AccountCurrency accountCurrencyFE : accountCurrencies) {
			if ((validator.validateBlankField(accountCurrencyFE.getAcqPayId()))) {
			} else if (!(validator.validateField(CrmFieldType.ACQ_PAYID, accountCurrencyFE.getAcqPayId()))) {
				addFieldError(CrmFieldType.ACQ_PAYID.getName(), validator.getResonseObject().getResponseMessage());
			}
			if ((validator.validateBlankField(accountCurrencyFE.getMerchantId()))) {
			} else if (!(validator.validateField(CrmFieldType.MERCHANTID, accountCurrencyFE.getMerchantId()))) {
				addFieldError(CrmFieldType.MERCHANTID.getName(), validator.getResonseObject().getResponseMessage());
			}
			if ((validator.validateBlankField(accountCurrencyFE.getCurrencyCode()))) {
			} else if (!(validator.validateField(CrmFieldType.CURRENCY, accountCurrencyFE.getCurrencyCode()))) {
				addFieldError(CrmFieldType.CURRENCY.getName(), validator.getResonseObject().getResponseMessage());
			}
			if ((validator.validateBlankField(accountCurrencyFE.getPassword()))) {

			} else if (!(validator.validateField(CrmFieldType.ACCOUNT_PASSWORD, accountCurrencyFE.getPassword()))) {
				addFieldError(CrmFieldType.ACCOUNT_PASSWORD.getName(),
						validator.getResonseObject().getResponseMessage());
			}
			if ((validator.validateBlankField(accountCurrencyFE.getTxnKey()))) {
			} else if (!(validator.validateField(CrmFieldType.TXN_KEY, accountCurrencyFE.getTxnKey()))) {
				addFieldError(CrmFieldType.TXN_KEY.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
	}

	public String getMapString() {
		return mapString;
	}

	public void setMapString(String mapString) {
		this.mapString = mapString;
	}

	public String display() {
		return NONE;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public String getMerchantEmailId() {
		return merchantEmailId;
	}

	public void setMerchantEmailId(String merchantEmailId) {
		this.merchantEmailId = merchantEmailId;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getAccountCurrencySet() {
		return accountCurrencySet;
	}

	public void setAccountCurrencySet(String accountCurrencySet) {
		this.accountCurrencySet = accountCurrencySet;
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

	public boolean isInternational() {
		return international;
	}

	public void setInternational(boolean international) {
		this.international = international;
	}

	public boolean isDomestic() {
		return domestic;
	}

	public void setDomestic(boolean domestic) {
		this.domestic = domestic;
	}

	public boolean isCommercial() {
		return commercial;
	}

	public void setCommercial(boolean commercial) {
		this.commercial = commercial;
	}

	public boolean isCustomer() {
		return customer;
	}

	public void setCustomer(boolean customer) {
		this.customer = customer;
	}

	public boolean isOnUs() {
		return onUs;
	}

	public void setOnUs(boolean onUs) {
		this.onUs = onUs;
	}

	public boolean isOffUs() {
		return offUs;
	}

	public void setOffUs(boolean offUs) {
		this.offUs = offUs;
	}

}
