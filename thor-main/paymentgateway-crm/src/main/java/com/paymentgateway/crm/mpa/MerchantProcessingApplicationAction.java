package com.paymentgateway.crm.mpa;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.ElectricityBoardType;
import com.paymentgateway.crm.action.AbstractSecureAction;

/**
 * @author Amitosh Aanand
 *
 */

public class MerchantProcessingApplicationAction extends AbstractSecureAction {

	@Autowired
	private MPAServicesFactory mpaServicesFactory;

	@Autowired
	private MPAResponseCreatorUI responseCreator;

	@Autowired
	private CrmValidator validator;

	private String payId;

	private String industryCategory;
	private String typeOfEntity;
	private String companyName;
	private String cin;
	private String dateOfIncorporation;
	private String businessPan;
	private String companyRegisteredAddress;
	private String tradingAddress1;
	private String tradingAddress2;
	private String tradingCountry;
	private String tradingState;
	private String tradingPin;
	private String gstin;
	private String companyPhone;
	private String companyWebsite;
	private String companyEmailId;
	private String businessEmailForCommunication;
	private String registrationNumber;

	private String directorName;
	private String directorEmail;

	private String directorNumber;
	private String consumerNumber;
	private String electricityProvider;

	private String filePath;

	private String panNumber;
	private String firstName;
	private String middleName;
	private String lastName;

	private String gstinUsername;
	private String gstinOtp;
	private String appKey;

	private String uid;
	private String esignName;
	private Object urlMap;

	private Object mpaData;
	private User user = new User();

	private static JSONObject authenticationResponse;
	private static final long serialVersionUID = 6995594440344649854L;
	private static Logger logger = LoggerFactory.getLogger(MerchantProcessingApplicationAction.class.getName());

	public String execute() {
		logger.info("Inside execute(), MerchantProcessingApplicationAction");
		try {
			authenticationResponse = mpaServicesFactory.loggingIn();
		} catch (Exception exception) {
			logger.error("Exception caught in execute(), MerchantProcessingApplicationAction, " , exception);
			return INPUT;
		}
		return SUCCESS;
	}

	public String cinByCompanyName() {
		logger.info("Inside rocCompanyNameSearch(), MerchantProcessingApplicationAction");
		try {
			user = (User) sessionMap.get(Constants.USER);
			setMpaData(mpaServicesFactory.cinByCompanyName(companyName, user, authenticationResponse));
		} catch (Exception exception) {
			logger.error("Exception caught in cinByCompanyName(), MerchantProcessingApplicationAction, " + exception);
			if (mpaData == null) {
				setMpaData(responseCreator.errorResponseCreator(Constants.ERROR_TYPE_CIN_BY_COMPANY_NAME));
			}
		}
		return SUCCESS;
	}

	public String simpleSearchByCin() {
		logger.info("Inside simpleSearchByCin(), MerchantProcessingApplicationAction");
		try {
			user = (User) sessionMap.get(Constants.USER);
			if (user.getUserType().equals(UserType.MERCHANT)) {
				setMpaData(mpaServicesFactory.simpleSearchByCin(user, user.getPayId(), cin, typeOfEntity,
						industryCategory));
			} else if (user.getUserType().equals(UserType.SUBUSER)) {
				setMpaData(mpaServicesFactory.simpleSearchByCin(user, user.getParentPayId(), cin, typeOfEntity,
						industryCategory));
			} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
					|| user.getUserType().equals(UserType.SUPERADMIN)) {
				setMpaData(mpaServicesFactory.simpleSearchByCin(user, payId, cin, typeOfEntity, industryCategory));
			}
		} catch (Exception exception) {
			logger.error("Exception caught in simpleSearchByCin(), MerchantProcessingApplicationAction, " , exception);
			if (mpaData == null) {
				setMpaData(responseCreator.errorResponseCreator(Constants.ERROR_TYPE_CIN_ATTEMPT_WARN));
			}
		}
		return SUCCESS;
	}

	public String snecs() {
		logger.info("Inside snecs(), MerchantProcessingApplicationAction");
		try {
			user = (User) sessionMap.get(Constants.USER);
			if (user.getUserType().equals(UserType.MERCHANT)) {
				setMpaData(mpaServicesFactory.snecs(user, user.getPayId(), registrationNumber, tradingState));
			} else if (user.getUserType().equals(UserType.SUBUSER)) {
				setMpaData(mpaServicesFactory.snecs(user, user.getParentPayId(), registrationNumber, tradingState));
			} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
					|| user.getUserType().equals(UserType.SUPERADMIN)) {
				setMpaData(mpaServicesFactory.snecs(user, payId, registrationNumber, tradingState));
			}
		} catch (Exception exception) {
			logger.error("Exception caught in snecs(), MerchantProcessingApplicationAction, " , exception);
			if (mpaData == null) {
				setMpaData(responseCreator.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN));
			}
		}
		return SUCCESS;
	}

	public String panToGst() {
		logger.info("Inside panToGst(), MerchantProcessingApplicationAction");
		try {
			user = (User) sessionMap.get(Constants.USER);
			/*
			 * Boolean response = mpaServicesFactory.panVerification1(businessPan,
			 * companyName, companyEmailId, "businessPan"); if (response) {
			 */
			if (user.getUserType().equals(UserType.MERCHANT)) {
				setMpaData(mpaServicesFactory.fetchGstByPan(user, user.getPayId(), businessPan, companyEmailId,
						tradingState));
			} else if (user.getUserType().equals(UserType.SUBUSER)) {
				setMpaData(mpaServicesFactory.fetchGstByPan(user, user.getParentPayId(), businessPan, companyEmailId,
						tradingState));
			} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
					|| user.getUserType().equals(UserType.SUPERADMIN)) {
				setMpaData(mpaServicesFactory.fetchGstByPan(user, payId, businessPan, companyEmailId, tradingState));
			}
			/*
			 * } else { setMpaData(mpaServicesFactory.createBusinessPanErrorResponse()); }
			 */
		} catch (Exception exception) {
			logger.error("Exception caught in panToGst(), MerchantProcessingApplicationAction, " , exception);
			if (mpaData == null) {
				setMpaData(responseCreator.errorResponseCreator(Constants.ERROR_TYPE_PAN_TO_GSTIN));
			}
		}
		return SUCCESS;
	}

	public String processDrivingLicense() {
		logger.info("Inside processDrivingLicense(), MerchantProcessingApplicationAction");
		try {
			user = (User) sessionMap.get(Constants.USER);
			if (user.getUserType().equals(UserType.MERCHANT)) {
				setMpaData(mpaServicesFactory.processDrivingLicenseImage(user, user.getPayId(), directorNumber));
			} else if (user.getUserType().equals(UserType.SUBUSER)) {
				setMpaData(mpaServicesFactory.processDrivingLicenseImage(user, user.getParentPayId(), directorNumber));
			} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
					|| user.getUserType().equals(UserType.SUPERADMIN)) {
				setMpaData(mpaServicesFactory.processDrivingLicenseImage(user, payId, directorNumber));
			}
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
			if (mpaData == null) {
				setMpaData(responseCreator.errorResponseCreator(Constants.ERROR_TYPE_DRIVING_LICENSE_EXTRACTION));
			}
		}
		return SUCCESS;
	}

	public String detailsByEBill() {
		logger.info("Inside detailsByEBill(), MerchantProcessingApplicationAction");
		try {
			user = (User) sessionMap.get(Constants.USER);
			if (user.getUserType().equals(UserType.MERCHANT)) {
				setMpaData(mpaServicesFactory.fetchDetailsByEBill(user, user.getPayId(), directorNumber, consumerNumber,
						ElectricityBoardType.getElectricityBoardCode(electricityProvider)));
			} else if (user.getUserType().equals(UserType.SUBUSER)) {
				setMpaData(mpaServicesFactory.fetchDetailsByEBill(user, user.getParentPayId(), directorNumber,
						consumerNumber, ElectricityBoardType.getElectricityBoardCode(electricityProvider)));
			} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
					|| user.getUserType().equals(UserType.SUPERADMIN)) {
				setMpaData(mpaServicesFactory.fetchDetailsByEBill(user, payId, directorNumber, consumerNumber,
						ElectricityBoardType.getElectricityBoardCode(electricityProvider)));
			}
		} catch (Exception exception) {
			logger.error("Exception caught in panToGst(), MerchantProcessingApplicationAction, " , exception);
			if (mpaData == null) {
				setMpaData(responseCreator.errorResponseCreator(Constants.ERROR_TYPE_ELECTRICTY));
			}
		}
		return SUCCESS;
	}

	public String invokeGstrOtp() {
		logger.info("Inside invokeGstrOtp(), MerchantProcessingApplicationAction");
		try {
			user = (User) sessionMap.get(Constants.USER);
			if (user.getUserType().equals(UserType.MERCHANT)) {
				setMpaData(mpaServicesFactory.getGSTROtp(user, user.getPayId(), gstinUsername));
			} else if (user.getUserType().equals(UserType.SUBUSER)) {
				setMpaData(mpaServicesFactory.getGSTROtp(user, user.getParentPayId(), gstinUsername));
			} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
					|| user.getUserType().equals(UserType.SUPERADMIN)) {
				setMpaData(mpaServicesFactory.getGSTROtp(user, payId, gstinUsername));
			}
		} catch (Exception exception) {
			logger.error("Exception caught in panToGst(), MerchantProcessingApplicationAction, " , exception);
			if (mpaData == null) {
				setMpaData(responseCreator.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN));
			}
		}
		return SUCCESS;
	}

	public String invokeGstr3b() {
		logger.info("Inside invokeGstr3b(), MerchantProcessingApplicationAction");
		try {
			user = (User) sessionMap.get(Constants.USER);
			if (user.getUserType().equals(UserType.MERCHANT)) {
				setMpaData(mpaServicesFactory.invokeGstr3b(user, user.getPayId(), gstinUsername, gstinOtp, appKey));
			} else if (user.getUserType().equals(UserType.SUBUSER)) {
				setMpaData(
						mpaServicesFactory.invokeGstr3b(user, user.getParentPayId(), gstinUsername, gstinOtp, appKey));
			} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
					|| user.getUserType().equals(UserType.SUPERADMIN)) {
				setMpaData(mpaServicesFactory.invokeGstr3b(user, payId, gstinUsername, gstinOtp, appKey));
			}
		} catch (Exception exception) {
			logger.error("Exception caught in panToGst(), MerchantProcessingApplicationAction, " , exception);
			if (mpaData == null) {
				setMpaData(responseCreator.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN));
			}
		}
		return SUCCESS;
	}

	public String processCheque() {
		logger.info("Inside processCheque(), MerchantProcessingApplicationAction");
		try {
			user = (User) sessionMap.get(Constants.USER);
			if (user.getUserType().equals(UserType.MERCHANT)) {
				setMpaData(mpaServicesFactory.processChequeImage(user, user.getPayId()));
			} else if (user.getUserType().equals(UserType.SUBUSER)) {
				setMpaData(mpaServicesFactory.processChequeImage(user, user.getParentPayId()));
			} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
					|| user.getUserType().equals(UserType.SUPERADMIN)) {
				setMpaData(mpaServicesFactory.processChequeImage(user, payId));
			}
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
			if (mpaData == null) {
				setMpaData(
						responseCreator.errorResponseCreator(Constants.ERROR_TYPE_CHEQUE_EXTRACTION_BANK_VERIFICATION));
			}
		}
		return SUCCESS;
	}

	public String panVerification() {
		logger.info("Inside panVerification(), MerchantProcessingApplicationAction");
		try {
			setMpaData(mpaServicesFactory.individualPanVerification(panNumber, directorName, directorEmail));
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
			if (mpaData == null) {
				setMpaData(responseCreator.errorResponseCreator(Constants.ERROR_TYPE_INDIVIDUAL_PAN));
			}
		}
		return SUCCESS;
	}

	public String businessPanVerification() {
		logger.info("Inside businessPanVerification(), MerchantProcessingApplicationAction");
		try {
			setMpaData(mpaServicesFactory.businessPanVerification(panNumber, companyName, companyEmailId));
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
			if (mpaData == null) {
				setMpaData(responseCreator.errorResponseCreator(Constants.ERROR_TYPE_BUSINESS_PAN));
			}
		}
		return SUCCESS;
	}

	public String processESign() {
		logger.info("Inside processESign(), MerchantProcessingApplicationAction");
		try {
			user = (User) sessionMap.get(Constants.USER);
			if (user.getUserType().equals(UserType.MERCHANT)) {
				mpaServicesFactory.loadAgreementFile(user, user.getPayId());
				setUrlMap(mpaServicesFactory.processESign(user, user.getPayId(), esignName));
			} else if (user.getUserType().equals(UserType.SUBUSER)) {
				mpaServicesFactory.loadAgreementFile(user, user.getParentPayId());
				setUrlMap(mpaServicesFactory.processESign(user, user.getParentPayId(), esignName));
			} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
					|| user.getUserType().equals(UserType.SUPERADMIN)) {
				mpaServicesFactory.loadAgreementFile(user, getPayId());
				setUrlMap(mpaServicesFactory.processESign(user, getPayId(), esignName));
			}
			logger.info("Inside processESign(), MerchantProcessingApplicationAction mpaData : " + getUrlMap());
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
			if (mpaData == null) {
				setMpaData(responseCreator.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN));
			}
		}
		return SUCCESS;
	}

	public String fetchEsignResponseData() {
		logger.info("Inside getEsignResponseData(), MerchantProcessingApplicationAction");
		try {
			user = (User) sessionMap.get(Constants.USER);
			Map<String, String> responseMap = new HashMap<String, String>();

			if (user.getUserType().equals(UserType.MERCHANT)) {

				responseMap = mpaServicesFactory.getEsignResponse(user.getPayId());
				if (responseMap != null) {
					if (!StringUtils.isEmpty(responseMap.get("aadhaarType"))) {
						setMpaData(responseMap);
					} else {
						setUrlMap(responseMap);
					}
				} else {
					setMpaData(responseCreator.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN));
				}
			} else if (user.getUserType().equals(UserType.SUBUSER)) {
				responseMap = mpaServicesFactory.getEsignResponse(user.getParentPayId());
				if (responseMap != null) {
					if (!StringUtils.isEmpty(responseMap.get("aadhaarType"))) {
						setMpaData(responseMap);
					} else {
						setUrlMap(responseMap);
					}
				} else {
					setMpaData(responseCreator.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN));
				}
			} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
					|| user.getUserType().equals(UserType.SUPERADMIN)) {
				responseMap = mpaServicesFactory.getEsignResponse(getPayId());
				if (responseMap != null) {
					if (!StringUtils.isEmpty(responseMap.get("aadhaarType"))) {
						setMpaData(responseMap);
					} else {
						setUrlMap(responseMap);
					}
				} else {
					setMpaData(responseCreator.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN));
				}
			}
			logger.info("Inside processESign(), MerchantProcessingApplicationAction mpaData : " + getMpaData());
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
			if (mpaData == null) {
				setMpaData(responseCreator.errorResponseCreator(Constants.ERROR_TYPE_BUSINESS_PAN));
			}
		}

//			setMpaData(mpaServicesFactory.getEsignResponse(user.getPayId()));
//			MerchantProcessingApplication mpaData = mpaDao.fetchMPADataByPayId(user.getPayId());
//			
//			if(mpaData.geteSignResponseData() != null) {
//				JSONObject eSignResopnseData = new JSONObject(mpaData.geteSignResponseData());
//				setMpaData(responseCreatorUI.createESignDataResponse(eSignResopnseData));
//			
//			}else if(mpaData.getEsignUrlResponse() != null) {
//					String eSignUrlResponse = mpaData.getEsignUrlResponse();
//					Map<String, String> url = new HashMap<String, String>();
//					url.put("url", eSignUrlResponse);
//					setUrlMap(url);
//			}

		return SUCCESS;
	}

	public String logOut() {
		try {
			logger.info("Inside logOut(), MerchantProcessingApplicationAction");
			mpaServicesFactory.logOut();
			authenticationResponse = null;
		} catch (Exception exception) {
			logger.error("Exception caught in logOut(), MerchantProcessingApplicationAction, " , exception);
		}
		return SUCCESS;
	}

	public void validate() {

		if (!(validator.validateBlankField(getTypeOfEntity()))) {
			if (!(validator.validateField(CrmFieldType.TYPE_OF_ENTITY, getTypeOfEntity()))) {
				addFieldError(CrmFieldType.TYPE_OF_ENTITY.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getCompanyName()))) {
			if (!(validator.validateField(CrmFieldType.COMPANY_NAME, getCompanyName()))) {
				addFieldError(CrmFieldType.COMPANY_NAME.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getCin()))) {
			if (!(validator.validateField(CrmFieldType.CIN, getCin()))) {
				addFieldError(CrmFieldType.CIN.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getDateOfIncorporation()))) {
			if (!(validator.validateField(CrmFieldType.DATE_OF_INCORPORATION, getDateOfIncorporation()))) {
				addFieldError(CrmFieldType.DATE_OF_INCORPORATION.getName(),
						validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getBusinessPan()))) {
			if (!(validator.validateField(CrmFieldType.PAN_NUMBER, getBusinessPan()))) {
				addFieldError(CrmFieldType.PAN_NUMBER.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getCompanyRegisteredAddress()))) {
			if (!(validator.validateField(CrmFieldType.ADDRESS, getCompanyRegisteredAddress()))) {
				addFieldError(CrmFieldType.ADDRESS.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getTradingAddress1()))) {
			if (!(validator.validateField(CrmFieldType.ADDRESS, getTradingAddress1()))) {
				addFieldError(CrmFieldType.ADDRESS.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getTradingAddress2()))) {
			if (!(validator.validateField(CrmFieldType.ADDRESS, getTradingAddress2()))) {
				addFieldError(CrmFieldType.ADDRESS.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getTradingCountry()))) {
			if (!(validator.validateField(CrmFieldType.COUNTRY, getTradingCountry()))) {
				addFieldError(CrmFieldType.COUNTRY.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getTradingState()))) {
			if (!(validator.validateField(CrmFieldType.STATE, getTradingState()))) {
				addFieldError(CrmFieldType.STATE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getTradingPin()))) {
			if (!(validator.validateField(CrmFieldType.POSTALCODE, getTradingPin()))) {
				addFieldError(CrmFieldType.POSTALCODE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getGstin()))) {
			if (!(validator.validateField(CrmFieldType.MERCHANT_GST_NUMBER, getGstin()))) {
				addFieldError(CrmFieldType.MERCHANT_GST_NUMBER.getName(),
						validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getCompanyPhone()))) {
			if (!(validator.validateField(CrmFieldType.TELEPHONE_NO, getCompanyPhone()))) {
				addFieldError(CrmFieldType.TELEPHONE_NO.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getCompanyEmailId()))) {
			if (!(validator.validateField(CrmFieldType.MERCHANT_EMAIL_ID, getCompanyEmailId()))) {
				addFieldError(CrmFieldType.MERCHANT_EMAIL_ID.getName(),
						validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getBusinessEmailForCommunication()))) {
			if (!(validator.validateField(CrmFieldType.MERCHANT_EMAIL_ID, getBusinessEmailForCommunication()))) {
				addFieldError(CrmFieldType.MERCHANT_EMAIL_ID.getName(),
						validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getRegistrationNumber()))) {
			if (!(validator.validateField(CrmFieldType.REGISTRATION_NUMBER, getRegistrationNumber()))) {
				addFieldError(CrmFieldType.REGISTRATION_NUMBER.getName(),
						validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getDirectorName()))) {
			if (!(validator.validateField(CrmFieldType.DIRECTOR_NAME, getDirectorName()))) {
				addFieldError(CrmFieldType.DIRECTOR_NAME.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getDirectorEmail()))) {
			if (!(validator.validateField(CrmFieldType.EMAILID, getDirectorEmail()))) {
				addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getDirectorNumber()))) {
			if (!(validator.validateField(CrmFieldType.DIRECTOR_NUMBER, getDirectorNumber()))) {
				addFieldError(CrmFieldType.DIRECTOR_NUMBER.getName(),
						validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getConsumerNumber()))) {
			if (!(validator.validateField(CrmFieldType.CONSUMER_NUMBER, getConsumerNumber()))) {
				addFieldError(CrmFieldType.CONSUMER_NUMBER.getName(),
						validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getElectricityProvider()))) {
			if (!(validator.validateField(CrmFieldType.ELECTRICITY_PROVIDER, getElectricityProvider()))) {
				addFieldError(CrmFieldType.ELECTRICITY_PROVIDER.getName(),
						validator.getResonseObject().getResponseMessage());
			}
		}

		/*
		 * if (!(validator.validateBlankField(getFilePath()))) { if
		 * (!(validator.validateField(CrmFieldType.FILE_PATH, getFilePath()))) {
		 * addFieldError(CrmFieldType.FILE_PATH.getName(),
		 * validator.getResonseObject().getResponseMessage()); } }
		 */

		if (!(validator.validateBlankField(getPanNumber()))) {
			if (!(validator.validateField(CrmFieldType.PAN_NUMBER, getPanNumber()))) {
				addFieldError(CrmFieldType.PAN_NUMBER.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getFirstName()))) {
			if (!(validator.validateField(CrmFieldType.FIRSTNAME, getFirstName()))) {
				addFieldError(CrmFieldType.FIRSTNAME.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getMiddleName()))) {
			if (!(validator.validateField(CrmFieldType.MIDDLENAME, getMiddleName()))) {
				addFieldError(CrmFieldType.MIDDLENAME.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getLastName()))) {
			if (!(validator.validateField(CrmFieldType.LASTNAME, getLastName()))) {
				addFieldError(CrmFieldType.LASTNAME.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		/*
		 * if (!(validator.validateBlankField(getGstinUsername()))) { if
		 * (!(validator.validateField(CrmFieldType.GSTIN_USERNAME, getGstinUsername())))
		 * { addFieldError(CrmFieldType.GSTIN_USERNAME.getName(),
		 * validator.getResonseObject().getResponseMessage()); } }
		 */

		if (!(validator.validateBlankField(getGstinOtp()))) {
			if (!(validator.validateField(CrmFieldType.GST_OTP, getGstinOtp()))) {
				addFieldError(CrmFieldType.GST_OTP.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		/*
		 * if (!(validator.validateBlankField(getAppKey()))) { if
		 * (!(validator.validateField(CrmFieldType.APP_KEY, getAppKey()))) {
		 * addFieldError(CrmFieldType.APP_KEY.getName(),
		 * validator.getResonseObject().getResponseMessage()); } }
		 */
	}

	public Object getUrlMap() {
		return urlMap;
	}

	public void setUrlMap(Object urlMap) {
		this.urlMap = urlMap;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getEsignName() {
		return esignName;
	}

	public void setEsignName(String esignName) {
		this.esignName = esignName;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getTypeOfEntity() {
		return typeOfEntity;
	}

	public void setTypeOfEntity(String typeOfEntity) {
		this.typeOfEntity = typeOfEntity;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getCin() {
		return cin;
	}

	public void setCin(String cin) {
		this.cin = cin;
	}

	public String getDateOfIncorporation() {
		return dateOfIncorporation;
	}

	public void setDateOfIncorporation(String dateOfIncorporation) {
		this.dateOfIncorporation = dateOfIncorporation;
	}

	public String getBusinessPan() {
		return businessPan;
	}

	public void setBusinessPan(String businessPan) {
		this.businessPan = businessPan;
	}

	public String getCompanyRegisteredAddress() {
		return companyRegisteredAddress;
	}

	public void setCompanyRegisteredAddress(String companyRegisteredAddress) {
		this.companyRegisteredAddress = companyRegisteredAddress;
	}

	public String getTradingAddress1() {
		return tradingAddress1;
	}

	public void setTradingAddress1(String tradingAddress1) {
		this.tradingAddress1 = tradingAddress1;
	}

	public String getTradingAddress2() {
		return tradingAddress2;
	}

	public void setTradingAddress2(String tradingAddress2) {
		this.tradingAddress2 = tradingAddress2;
	}

	public String getTradingCountry() {
		return tradingCountry;
	}

	public void setTradingCountry(String tradingCountry) {
		this.tradingCountry = tradingCountry;
	}

	public String getTradingState() {
		return tradingState;
	}

	public void setTradingState(String tradingState) {
		this.tradingState = tradingState;
	}

	public String getTradingPin() {
		return tradingPin;
	}

	public void setTradingPin(String tradingPin) {
		this.tradingPin = tradingPin;
	}

	public String getGstin() {
		return gstin;
	}

	public void setGstin(String gstin) {
		this.gstin = gstin;
	}

	public String getCompanyPhone() {
		return companyPhone;
	}

	public void setCompanyPhone(String companyPhone) {
		this.companyPhone = companyPhone;
	}

	public String getCompanyWebsite() {
		return companyWebsite;
	}

	public void setCompanyWebsite(String companyWebsite) {
		this.companyWebsite = companyWebsite;
	}

	public String getCompanyEmailId() {
		return companyEmailId;
	}

	public void setCompanyEmailId(String companyEmailId) {
		this.companyEmailId = companyEmailId;
	}

	public String getBusinessEmailForCommunication() {
		return businessEmailForCommunication;
	}

	public void setBusinessEmailForCommunication(String businessEmailForCommunication) {
		this.businessEmailForCommunication = businessEmailForCommunication;
	}

	public String getPanNumber() {
		return panNumber;
	}

	public void setPanNumber(String panNumber) {
		this.panNumber = panNumber;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Object getMpaData() {
		return mpaData;
	}

	public void setMpaData(Object mpaData) {
		this.mpaData = mpaData;
	}

	public String getConsumerNumber() {
		return consumerNumber;
	}

	public void setConsumerNumber(String consumerNumber) {
		this.consumerNumber = consumerNumber;
	}

	public String getElectricityProvider() {
		return electricityProvider;
	}

	public void setElectricityProvider(String electricityProvider) {
		this.electricityProvider = electricityProvider;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getDirectorNumber() {
		return directorNumber;
	}

	public void setDirectorNumber(String directorNumber) {
		this.directorNumber = directorNumber;
	}

	public String getDirectorName() {
		return directorName;
	}

	public void setDirectorName(String directorName) {
		this.directorName = directorName;
	}

	public String getDirectorEmail() {
		return directorEmail;
	}

	public void setDirectorEmail(String directorEmail) {
		this.directorEmail = directorEmail;
	}

	public String getGstinUsername() {
		return gstinUsername;
	}

	public void setGstinUsername(String gstinUsername) {
		this.gstinUsername = gstinUsername;
	}

	public String getGstinOtp() {
		return gstinOtp;
	}

	public void setGstinOtp(String gstinOtp) {
		this.gstinOtp = gstinOtp;
	}

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getIndustryCategory() {
		return industryCategory;
	}

	public void setIndustryCategory(String industryCategory) {
		this.industryCategory = industryCategory;
	}
}
