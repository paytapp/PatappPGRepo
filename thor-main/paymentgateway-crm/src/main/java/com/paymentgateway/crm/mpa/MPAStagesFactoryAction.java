package com.paymentgateway.crm.mpa;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.BusinessType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.crm.action.AbstractSecureAction;

/**
 * @author Amitosh Aanand
 *
 */
public class MPAStagesFactoryAction extends AbstractSecureAction {

	private static final long serialVersionUID = -2696968744625593204L;

	@Autowired
	private MPADataFactory factory;

	@Autowired
	private MPAServicesFactory servicesFactory;

	@Autowired
	private MPAResponseCreatorUI responseCreator;

	@Autowired
	private CrmValidator validator;
	
	@Autowired
	private UserDao userDao;

	private String industryCategory;
	private static Logger logger = LoggerFactory.getLogger(MPAStagesFactoryAction.class.getName());
	
	private Map<String, String> industryCategoryList = new TreeMap<String, String>();
	private String payId;

	private String typeOfEntity;
	private String companyName;
	private String cin;
	private String registrationNumber;
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

	private String contactName;
	private String contactMobile;
	private String contactEmail;
	private String contactLandline;
	private String director1FullName;
	private String director1Pan;
	private String director1Email;
	private String director1Mobile;
	private String director1Landline;
	private String director1Address;
	private String director1DOB;
	private String director2FullName;
	private String director2Pan;
	private String director2Email;
	private String director2Mobile;
	private String director2Landline;
	private String director2Address;
	private String director2DOB;
	
	private String merchantSupportEmailId;
	private String merchantSupportMobileNumber;
	private String merchantSupportLandLine;
	
	private String accountNumber;
	private String accountIfsc;
	private String accountHolderName;
	private String accountMobileNumber;

	private String annualTurnover;
	private String annualTurnoverOnline;
	private String percentageCC;
	private String percentageDC;
	private String percentageDomestic;
	private String percentageInternational;
	private String percentageCD;
	private String percentageNeftOrImpsOrRtgs;
	private String percentageNB;
	private String percentageUP;
	private String percentageWL;
	private String percentageEM;

	private String thirdPartyForCardData;
	private String refundsAllowed;

	private String technicalContactName;
	private String technicalContactMobile;
	private String technicalContactEmail;
	private String technicalContactLandline;
	private String serverDetails;
	private String serverCompanyName;
	private String serverCompanyLandline;
	private String serverCompanyAddress;
	private String serverCompanyMobile;
	private String operatingSystem;
	private String backendTechnology;
	private String applicationServerTechnology;
	private String productionServerIp;

	private String merchantType;
	private String surcharge;
	private String integrationType;
	private String customizedInvoiceDesign;
	private String internationalCards;
	private String expressPay;
	private String expressPayParameter;
	private String allowDuplicateSaleOrderId;
	private String allowDuplicateRefundOrderId;
	private String allowDuplicateSaleOrderIdInRefund;
	private String allowDuplicateRefundOrderIdSale;

	private String stage;

	private User user = new User();
	private Object mpaData;

	public String fetchStage() {
		try {
			logger.info("Inside checkStage(), MerchantProcessingApplicationAction");
			Map<String, String> industryCategoryLinkedMap = BusinessType.getIndustryCategoryList();
			industryCategoryList.putAll(industryCategoryLinkedMap);
			user = (User) sessionMap.get(Constants.USER.getValue());
			if (user.getUserType().equals(UserType.MERCHANT)) {
				setMpaData(factory.fetchSavedStageData(user, user.getPayId(), stage));
			} else if (user.getUserType().equals(UserType.SUBUSER)) {
				setMpaData(factory.fetchSavedStageData(user, user.getParentPayId(), stage));
			} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
					|| user.getUserType().equals(UserType.SUPERADMIN)) {
				setMpaData(factory.fetchSavedStageData(user, payId, stage));
			}
			return SUCCESS;
		} catch (Exception e) {
			logger.error("Error caught while fetching MPA saved stages, " , e);
			responseCreator.errorResponseCreator("ERROR_TYPE_UNKNOWN");
			return SUCCESS;
		}
	}

	public String saveStage() {
		logger.info("Inside saveStage00(), MerchantProcessingApplicationAction");
		boolean isOnlineMpaFlag = true;
		user = (User) sessionMap.get(Constants.USER.getValue());
		
		try {
			
			if(StringUtils.isNotBlank(stage) && (stage.equalsIgnoreCase("04") || stage.equalsIgnoreCase("05"))) {
				if (user.getUserType().equals(UserType.MERCHANT)) {
					isOnlineMpaFlag = user.isMpaOnlineFlag();
				}else if (user.getUserType().equals(UserType.SUBUSER)) {
					isOnlineMpaFlag = user.isMpaOnlineFlag();
				}else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
						|| user.getUserType().equals(UserType.SUPERADMIN)) {
					User usermpa = userDao.findPayId(getPayId());
					isOnlineMpaFlag = usermpa.isMpaOnlineFlag();
				}
			}
			
			
			if (StringUtils.isNotBlank(stage) && stage.equalsIgnoreCase("00")) {
				if (user.getUserType().equals(UserType.MERCHANT)) {
					setMpaData(factory.saveStage00Data(user, user.getPayId(), companyName, typeOfEntity, cin,
							registrationNumber, dateOfIncorporation, businessPan, companyRegisteredAddress,
							tradingAddress1, tradingAddress2, tradingCountry, tradingState, tradingPin, gstin,
							companyPhone, companyWebsite, companyEmailId, businessEmailForCommunication, industryCategory, stage));
				} else if (user.getUserType().equals(UserType.SUBUSER)) {
					setMpaData(factory.saveStage00Data(user, user.getParentPayId(), companyName, typeOfEntity, cin,
							registrationNumber, dateOfIncorporation, businessPan, companyRegisteredAddress,
							tradingAddress1, tradingAddress2, tradingCountry, tradingState, tradingPin, gstin,
							companyPhone, companyWebsite, companyEmailId, businessEmailForCommunication, industryCategory, stage));
				} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
						|| user.getUserType().equals(UserType.SUPERADMIN)) {
					setMpaData(factory.saveStage00Data(user, payId, companyName, typeOfEntity, cin, registrationNumber,
							dateOfIncorporation, businessPan, companyRegisteredAddress, tradingAddress1,
							tradingAddress2, tradingCountry, tradingState, tradingPin, gstin, companyPhone,
							companyWebsite, companyEmailId, businessEmailForCommunication, industryCategory, stage));
				}
			} else if (StringUtils.isNotBlank(stage) && stage.equalsIgnoreCase("01")) {
				if (user.getUserType().equals(UserType.MERCHANT)) {
					setMpaData(factory.saveStage01Data(user, user.getPayId(), contactName, contactMobile, contactEmail,
							contactLandline, director1FullName, director1Pan, director1Email, director1Mobile,
							director1Landline, director1Address, director1DOB, director2FullName, director2Pan,
							director2Email, director2Mobile, director2Landline, director2Address, director2DOB, stage, 
							merchantSupportEmailId,merchantSupportMobileNumber,merchantSupportLandLine));
				} else if (user.getUserType().equals(UserType.SUBUSER)) {
					setMpaData(factory.saveStage01Data(user, user.getParentPayId(), contactName, contactMobile,
							contactEmail, contactLandline, director1FullName, director1Pan, director1Email,
							director1Mobile, director1Landline, director1Address, director1DOB, director2FullName,
							director2Pan, director2Email, director2Mobile, director2Landline, director2Address,
							director2DOB, stage, merchantSupportEmailId,merchantSupportMobileNumber, merchantSupportLandLine));
				} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
						|| user.getUserType().equals(UserType.SUPERADMIN)) {
					setMpaData(factory.saveStage01Data(user, payId, contactName, contactMobile, contactEmail,
							contactLandline, director1FullName, director1Pan, director1Email, director1Mobile,
							director1Landline, director1Address, director1DOB, director2FullName, director2Pan,
							director2Email, director2Mobile, director2Landline, director2Address, director1DOB, stage, 
							merchantSupportEmailId,merchantSupportMobileNumber, merchantSupportLandLine));
				}
			} else if (StringUtils.isNotBlank(stage) && stage.equalsIgnoreCase("02")) {
				if (user.getUserType().equals(UserType.MERCHANT)) {
					setMpaData(factory.saveStage02Data(user, user.getPayId(), accountNumber, accountIfsc,
							accountHolderName, accountMobileNumber, stage));
				} else if (user.getUserType().equals(UserType.SUBUSER)) {
					setMpaData(factory.saveStage02Data(user, user.getParentPayId(), accountNumber, accountIfsc,
							accountHolderName, accountMobileNumber, stage));
				} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
						|| user.getUserType().equals(UserType.SUPERADMIN)) {
					setMpaData(factory.saveStage02Data(user, payId, accountNumber, accountIfsc, accountHolderName,
							accountMobileNumber, stage));
				}
			} else if (StringUtils.isNotBlank(stage) && stage.equalsIgnoreCase("03")) {
				if (user.getUserType().equals(UserType.MERCHANT)) {
					setMpaData(factory.saveStage03Data(user, user.getPayId(), annualTurnover, annualTurnoverOnline,
							percentageCC, percentageDC, percentageDomestic, percentageInternational, percentageCD,
							percentageNeftOrImpsOrRtgs, percentageNB, percentageUP, percentageWL, percentageEM, stage));
				} else if (user.getUserType().equals(UserType.SUBUSER)) {
					setMpaData(factory.saveStage03Data(user, user.getParentPayId(), annualTurnover,
							annualTurnoverOnline, percentageCC, percentageDC, percentageDomestic,
							percentageInternational, percentageCD, percentageNeftOrImpsOrRtgs, percentageNB,
							percentageUP, percentageWL, percentageEM, stage));
				} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
						|| user.getUserType().equals(UserType.SUPERADMIN)) {
					setMpaData(factory.saveStage03Data(user, payId, annualTurnover, annualTurnoverOnline, percentageCC,
							percentageDC, percentageDomestic, percentageInternational, percentageCD,
							percentageNeftOrImpsOrRtgs, percentageNB, percentageUP, percentageWL, percentageEM, stage));
				}
			} 
			else if (StringUtils.isNotBlank(stage) && stage.equalsIgnoreCase("04") && isOnlineMpaFlag == true) {
				if (user.getUserType().equals(UserType.MERCHANT)) {
					setMpaData(factory.saveStage04Data(user, user.getPayId(), merchantType, surcharge, integrationType,
							customizedInvoiceDesign, internationalCards, expressPay, expressPayParameter,
							allowDuplicateSaleOrderId, allowDuplicateRefundOrderId, allowDuplicateSaleOrderIdInRefund,
							allowDuplicateRefundOrderIdSale, stage));
				} else if (user.getUserType().equals(UserType.SUBUSER)) {
					setMpaData(factory.saveStage04Data(user, user.getParentPayId(), merchantType, surcharge,
							integrationType, customizedInvoiceDesign, internationalCards, expressPay,
							expressPayParameter, allowDuplicateSaleOrderId, allowDuplicateRefundOrderId,
							allowDuplicateSaleOrderIdInRefund, allowDuplicateRefundOrderIdSale, stage));
				} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
						|| user.getUserType().equals(UserType.SUPERADMIN)) {
					setMpaData(factory.saveStage04Data(user, payId, merchantType, surcharge, integrationType,
							customizedInvoiceDesign, internationalCards, expressPay, expressPayParameter,
							allowDuplicateSaleOrderId, allowDuplicateRefundOrderId, allowDuplicateSaleOrderIdInRefund,
							allowDuplicateRefundOrderIdSale, stage));
				}
			} 
//			else if (StringUtils.isNotBlank(stage) && stage.equalsIgnoreCase("05")) {
//				if (user.getUserType().equals(UserType.MERCHANT)) {
//					setMpaData(
//							factory.saveStage05Data(user, user.getPayId(), technicalContactName, technicalContactMobile,
//									technicalContactEmail, technicalContactLandline, serverDetails, serverCompanyName,
//									serverCompanyLandline, serverCompanyAddress, serverCompanyMobile, operatingSystem,
//									backendTechnology, applicationServerTechnology, productionServerIp, stage));
//				} else if (user.getUserType().equals(UserType.SUBUSER)) {
//					setMpaData(factory.saveStage05Data(user, user.getParentPayId(), technicalContactName,
//							technicalContactMobile, technicalContactEmail, technicalContactLandline, serverDetails,
//							serverCompanyName, serverCompanyLandline, serverCompanyAddress, serverCompanyMobile,
//							operatingSystem, backendTechnology, applicationServerTechnology, productionServerIp,
//							stage));
//				} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
//						|| user.getUserType().equals(UserType.SUPERADMIN)) {
//					setMpaData(factory.saveStage05Data(user, payId, technicalContactName, technicalContactMobile,
//							technicalContactEmail, technicalContactLandline, serverDetails, serverCompanyName,
//							serverCompanyLandline, serverCompanyAddress, serverCompanyMobile, operatingSystem,
//							backendTechnology, applicationServerTechnology, productionServerIp, stage));
//				}
//			} 
//			else if (StringUtils.isNotBlank(stage) && stage.equalsIgnoreCase("04")) {
//				if (user.getUserType().equals(UserType.MERCHANT)) {
//					setMpaData(factory.saveStage06Data(user, user.getPayId(), thirdPartyForCardData, refundsAllowed,
//							stage));
//				} else if (user.getUserType().equals(UserType.SUBUSER)) {
//					setMpaData(factory.saveStage06Data(user, user.getParentPayId(), thirdPartyForCardData,
//							refundsAllowed, stage));
//				} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
//						|| user.getUserType().equals(UserType.SUPERADMIN)) {
//					setMpaData(factory.saveStage06Data(user, payId, thirdPartyForCardData, refundsAllowed, stage));
//				}
//			} 
			else if (StringUtils.isNotBlank(stage) && stage.equalsIgnoreCase("04")) {
				if (user.getUserType().equals(UserType.MERCHANT)) {
					setMpaData(factory.saveStage07Data(user, user.getPayId(), stage, isOnlineMpaFlag));
				} else if (user.getUserType().equals(UserType.SUBUSER)) {
					setMpaData(factory.saveStage07Data(user, user.getParentPayId(), stage, isOnlineMpaFlag));
				} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
						|| user.getUserType().equals(UserType.SUPERADMIN)) {
					setMpaData(factory.saveStage07Data(user, payId, stage, isOnlineMpaFlag));
				}
			} else if (StringUtils.isNotBlank(stage) && stage.equalsIgnoreCase("05") && isOnlineMpaFlag == true) {
				if (user.getUserType().equals(UserType.MERCHANT)) {
					setMpaData(factory.saveStage08Data(user, user.getPayId(), stage));
				} else if (user.getUserType().equals(UserType.SUBUSER)) {
					setMpaData(factory.saveStage08Data(user, user.getParentPayId(), stage));
				} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
				|| user.getUserType().equals(UserType.SUPERADMIN)) {
					setMpaData(factory.saveStage08Data(user, payId, stage));				}
			}else if (StringUtils.isNotBlank(stage) && stage.equalsIgnoreCase("05") && isOnlineMpaFlag == false) {
				if (user.getUserType().equals(UserType.MERCHANT)) {
					servicesFactory.performFinalStage(user, user.getPayId());
				} else if (user.getUserType().equals(UserType.SUBUSER)) {
					servicesFactory.performFinalStage(user, user.getParentPayId());
				} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
						|| user.getUserType().equals(UserType.SUPERADMIN)) {
					servicesFactory.performFinalStage(user, payId);
				}
			} else if (StringUtils.isNotBlank(stage) && stage.equalsIgnoreCase("06")) {
				if (user.getUserType().equals(UserType.MERCHANT)) {
					servicesFactory.performFinalStage(user, user.getPayId());
				} else if (user.getUserType().equals(UserType.SUBUSER)) {
					servicesFactory.performFinalStage(user, user.getParentPayId());
				} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
						|| user.getUserType().equals(UserType.SUPERADMIN)) {
					servicesFactory.performFinalStage(user, payId);
				}
			}
		} catch (Exception exception) {
			logger.error("Exception caught in saveStage(), MPAStagesFactoryAction, " , exception);
			responseCreator.errorResponseCreator("ERROR_TYPE_UNKNOWN");
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

		/*
		 * if (!(validator.validateBlankField(getDateOfIncorporation()))) { if
		 * (!(validator.validateField(CrmFieldType.DATE_OF_INCORPORATION,
		 * getDateOfIncorporation()))) {
		 * addFieldError(CrmFieldType.DATE_OF_INCORPORATION.getName(),
		 * validator.getResonseObject().getResponseMessage()); } }
		 */

		if (!(validator.validateBlankField(getRegistrationNumber()))) {
			if (!(validator.validateField(CrmFieldType.REGISTRATION_NUMBER, getRegistrationNumber()))) {
				addFieldError(CrmFieldType.REGISTRATION_NUMBER.getName(),
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
				addFieldError(CrmFieldType.ADDRESS.getName(),
						validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getTradingAddress1()))) {
			if (!(validator.validateField(CrmFieldType.ADDRESS, getTradingAddress1()))) {
				addFieldError(CrmFieldType.ADDRESS.getName(),
						validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getTradingAddress2()))) {
			if (!(validator.validateField(CrmFieldType.ADDRESS, getTradingAddress2()))) {
				addFieldError(CrmFieldType.ADDRESS.getName(),
						validator.getResonseObject().getResponseMessage());
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

		if (!(validator.validateBlankField(getCompanyWebsite()))) {
			if (!(validator.validateField(CrmFieldType.WEBSITE, getCompanyWebsite()))) {
				addFieldError(CrmFieldType.WEBSITE.getName(), validator.getResonseObject().getResponseMessage());
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

		if (!(validator.validateBlankField(getContactName()))) {
			if (!(validator.validateField(CrmFieldType.CONTACT_PERSON, getContactName()))) {
				addFieldError(CrmFieldType.CONTACT_PERSON.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getContactMobile()))) {
			if (!(validator.validateField(CrmFieldType.MOBILE, getContactMobile()))) {
				addFieldError(CrmFieldType.MOBILE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getContactEmail()))) {
			if (!(validator.validateField(CrmFieldType.EMAILID, getContactEmail()))) {
				addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getContactLandline()))) {
			if (!(validator.validateField(CrmFieldType.TELEPHONE_NO, getContactLandline()))) {
				addFieldError(CrmFieldType.TELEPHONE_NO.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getDirector1FullName()))) {
			if (!(validator.validateField(CrmFieldType.FULL_NAME, getDirector1FullName()))) {
				addFieldError(CrmFieldType.FULL_NAME.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getDirector1Pan()))) {
			if (!(validator.validateField(CrmFieldType.PAN_NUMBER, getDirector1Pan()))) {
				addFieldError(CrmFieldType.PAN_NUMBER.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getDirector1Email()))) {
			if (!(validator.validateField(CrmFieldType.EMAILID, getDirector1Email()))) {
				addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getDirector1Mobile()))) {
			if (!(validator.validateField(CrmFieldType.MOBILE, getDirector1Mobile()))) {
				addFieldError(CrmFieldType.MOBILE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getDirector1Landline()))) {
			if (!(validator.validateField(CrmFieldType.TELEPHONE_NO, getDirector1Landline()))) {
				addFieldError(CrmFieldType.TELEPHONE_NO.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getDirector1Address()))) {
			if (!(validator.validateField(CrmFieldType.ADDRESS, getDirector1Address()))) {
				addFieldError(CrmFieldType.ADDRESS.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getDirector1DOB()))) {
			if (!(validator.validateField(CrmFieldType.DOB, getDirector1DOB()))) {
				addFieldError(CrmFieldType.DOB.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		if (!(validator.validateBlankField(getDirector2FullName()))) {
			if (!(validator.validateField(CrmFieldType.FULL_NAME, getDirector2FullName()))) {
				addFieldError(CrmFieldType.FULL_NAME.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getDirector2Pan()))) {
			if (!(validator.validateField(CrmFieldType.PAN_NUMBER, getDirector2Pan()))) {
				addFieldError(CrmFieldType.PAN_NUMBER.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getDirector2Email()))) {
			if (!(validator.validateField(CrmFieldType.EMAILID, getDirector2Email()))) {
				addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getDirector2Mobile()))) {
			if (!(validator.validateField(CrmFieldType.MOBILE, getDirector2Mobile()))) {
				addFieldError(CrmFieldType.MOBILE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getDirector2Landline()))) {
			if (!(validator.validateField(CrmFieldType.TELEPHONE_NO, getDirector2Landline()))) {
				addFieldError(CrmFieldType.TELEPHONE_NO.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getDirector2Address()))) {
			if (!(validator.validateField(CrmFieldType.ADDRESS, getDirector2Address()))) {
				addFieldError(CrmFieldType.ADDRESS.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getDirector2DOB()))) {
			if (!(validator.validateField(CrmFieldType.DOB, getDirector2DOB()))) {
				addFieldError(CrmFieldType.DOB.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getAccountNumber()))) {
			if (!(validator.validateField(CrmFieldType.ACCOUNT_NO, getAccountNumber()))) {
				addFieldError(CrmFieldType.ACCOUNT_NO.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getAccountIfsc()))) {
			if (!(validator.validateField(CrmFieldType.IFSC_CODE, getAccountIfsc()))) {
				addFieldError(CrmFieldType.IFSC_CODE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getAccountHolderName()))) {
			if (!(validator.validateField(CrmFieldType.FULL_NAME, getAccountHolderName()))) {
				addFieldError(CrmFieldType.FULL_NAME.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getAccountMobileNumber()))) {
			if (!(validator.validateField(CrmFieldType.MOBILE, getAccountMobileNumber()))) {
				addFieldError(CrmFieldType.MOBILE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getAnnualTurnover()))) {
			if (!(validator.validateField(CrmFieldType.ANNUAL_TURNOVER, getAnnualTurnover()))) {
				addFieldError(CrmFieldType.ANNUAL_TURNOVER.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getAnnualTurnoverOnline()))) {
			if (!(validator.validateField(CrmFieldType.ANNUAL_TURNOVER_ONLINE, getAnnualTurnoverOnline()))) {
				addFieldError(CrmFieldType.ANNUAL_TURNOVER_ONLINE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		/*if (!(validator.validateBlankField(getPercentageCC()))) {
			if (!(validator.validateField(CrmFieldType.PERCENTAGE, getPercentageCC()))) {
				addFieldError(CrmFieldType.PERCENTAGE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getPercentageDC()))) {
			if (!(validator.validateField(CrmFieldType.PERCENTAGE, getPercentageDC()))) {
				addFieldError(CrmFieldType.PERCENTAGE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getPercentageDomestic()))) {
			if (!(validator.validateField(CrmFieldType.PERCENTAGE, getPercentageDomestic()))) {
				addFieldError(CrmFieldType.PERCENTAGE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		if (!(validator.validateBlankField(getPercentageInternational()))) {
			if (!(validator.validateField(CrmFieldType.PERCENTAGE, getPercentageInternational()))) {
				addFieldError(CrmFieldType.PERCENTAGE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		if (!(validator.validateBlankField(getPercentageCD()))) {
			if (!(validator.validateField(CrmFieldType.PERCENTAGE, getPercentageCD()))) {
				addFieldError(CrmFieldType.PERCENTAGE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		if (!(validator.validateBlankField(getPercentageNeftOrImpsOrRtgs()))) {
			if (!(validator.validateField(CrmFieldType.PERCENTAGE, getPercentageNeftOrImpsOrRtgs()))) {
				addFieldError(CrmFieldType.PERCENTAGE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		if (!(validator.validateBlankField(getPercentageNB()))) {
			if (!(validator.validateField(CrmFieldType.PERCENTAGE, getPercentageNB()))) {
				addFieldError(CrmFieldType.PERCENTAGE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		if (!(validator.validateBlankField(getPercentageUP()))) {
			if (!(validator.validateField(CrmFieldType.PERCENTAGE, getPercentageUP()))) {
				addFieldError(CrmFieldType.PERCENTAGE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}	
		if (!(validator.validateBlankField(getPercentageWL()))) {
			if (!(validator.validateField(CrmFieldType.PERCENTAGE, getPercentageWL()))) {
				addFieldError(CrmFieldType.PERCENTAGE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		if (!(validator.validateBlankField(getPercentageEM()))) {
			if (!(validator.validateField(CrmFieldType.PERCENTAGE, getPercentageEM()))) {
				addFieldError(CrmFieldType.PERCENTAGE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}*/

		if (!(validator.validateBlankField(getThirdPartyForCardData()))) {
			if (!(validator.validateField(CrmFieldType.THIRD_PARTY_NAME, getThirdPartyForCardData()))) {
				addFieldError(CrmFieldType.THIRD_PARTY_NAME.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		/*
		 * if (!(validator.validateBlankField(getRefundsAllowed()))) { if
		 * (!(validator.validateField(CrmFieldType.REFUNDS_ALLOWED,
		 * getRefundsAllowed()))) {
		 * addFieldError(CrmFieldType.REFUNDS_ALLOWED.getName(),
		 * validator.getResonseObject().getResponseMessage()); } }
		 */
		
		if (!(validator.validateBlankField(getTechnicalContactName()))) {
			if (!(validator.validateField(CrmFieldType.FULL_NAME, getTechnicalContactName()))) {
				addFieldError(CrmFieldType.FULL_NAME.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getTechnicalContactMobile()))) {
			if (!(validator.validateField(CrmFieldType.MOBILE, getTechnicalContactMobile()))) {
				addFieldError(CrmFieldType.MOBILE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getTechnicalContactEmail()))) {
			if (!(validator.validateField(CrmFieldType.EMAILID, getTechnicalContactEmail()))) {
				addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getTechnicalContactLandline()))) {
			if (!(validator.validateField(CrmFieldType.TELEPHONE_NO, getTechnicalContactLandline()))) {
				addFieldError(CrmFieldType.TELEPHONE_NO.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		/*
		 * if (!(validator.validateBlankField(getServerDetails()))) { if
		 * (!(validator.validateField(CrmFieldType.SERVER_DETAILS, getServerDetails())))
		 * { addFieldError(CrmFieldType.SERVER_DETAILS.getName(),
		 * validator.getResonseObject().getResponseMessage()); } }
		 */

		if (!(validator.validateBlankField(getServerCompanyName()))) {
			if (!(validator.validateField(CrmFieldType.COMPANY_NAME, getServerCompanyName()))) {
				addFieldError(CrmFieldType.COMPANY_NAME.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getServerCompanyLandline()))) {
			if (!(validator.validateField(CrmFieldType.TELEPHONE_NO, getServerCompanyLandline()))) {
				addFieldError(CrmFieldType.TELEPHONE_NO.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getServerCompanyAddress()))) {
			if (!(validator.validateField(CrmFieldType.ADDRESS, getServerCompanyAddress()))) {
				addFieldError(CrmFieldType.ADDRESS.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getServerCompanyMobile()))) {
			if (!(validator.validateField(CrmFieldType.MOBILE, getServerCompanyMobile()))) {
				addFieldError(CrmFieldType.MOBILE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getOperatingSystem()))) {
			if (!(validator.validateField(CrmFieldType.OS_ARCHITECTURE, getOperatingSystem()))) {
				addFieldError(CrmFieldType.OS_ARCHITECTURE.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		/*
		 * if (!(validator.validateBlankField(getBackendTechnology()))) { if
		 * (!(validator.validateField(CrmFieldType.BACKEND_TECHNOLOGY,
		 * getBackendTechnology()))) {
		 * addFieldError(CrmFieldType.BACKEND_TECHNOLOGY.getName(),
		 * validator.getResonseObject().getResponseMessage()); } }
		 */
		
		/*
		 * if (!(validator.validateBlankField(getApplicationServerTechnology()))) { if
		 * (!(validator.validateField(CrmFieldType.APPLICATION_SERVER_TECHNOLOGY,
		 * getApplicationServerTechnology()))) {
		 * addFieldError(CrmFieldType.APPLICATION_SERVER_TECHNOLOGY.getName(),
		 * validator.getResonseObject().getResponseMessage()); } }
		 */
		
		if (!(validator.validateBlankField(getProductionServerIp()))) {
			if (!(validator.validateField(CrmFieldType.IP, getProductionServerIp()))) {
				addFieldError(CrmFieldType.IP.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		/*
		 * if (!(validator.validateBlankField(getMerchantType()))) { if
		 * (!(validator.validateField(CrmFieldType.MERCHANT_TYPE, getMerchantType()))) {
		 * addFieldError(CrmFieldType.MERCHANT_TYPE.getName(),
		 * validator.getResonseObject().getResponseMessage()); } }
		 */

		if (!(validator.validateBlankField(getSurcharge()))) {
			if (!(validator.validateField(CrmFieldType.TRUE_FALSE_STRING, getSurcharge()))) {
				addFieldError(CrmFieldType.TRUE_FALSE_STRING.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		/*
		 * if (!(validator.validateBlankField(getIntegrationType()))) { if
		 * (!(validator.validateField(CrmFieldType.INTEGRATION_TYPE,
		 * getIntegrationType()))) {
		 * addFieldError(CrmFieldType.INTEGRATION_TYPE.getName(),
		 * validator.getResonseObject().getResponseMessage()); } }
		 */
		
		if (!(validator.validateBlankField(getCustomizedInvoiceDesign()))) {
			if (!(validator.validateField(CrmFieldType.TRUE_FALSE_STRING, getCustomizedInvoiceDesign()))) {
				addFieldError(CrmFieldType.TRUE_FALSE_STRING.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		if (!(validator.validateBlankField(getInternationalCards()))) {
			if (!(validator.validateField(CrmFieldType.TRUE_FALSE_STRING, getInternationalCards()))) {
				addFieldError(CrmFieldType.TRUE_FALSE_STRING.getName(), validator.getResonseObject().getResponseMessage());
			}
		}

		if (!(validator.validateBlankField(getExpressPay()))) {
			if (!(validator.validateField(CrmFieldType.TRUE_FALSE_STRING, getInternationalCards()))) {
				addFieldError(CrmFieldType.TRUE_FALSE_STRING.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		/*
		 * if (!(validator.validateBlankField(getExpressPayParameter()))) { if
		 * (!(validator.validateField(CrmFieldType.EXPRESS_PAY_PARAMETER,
		 * getExpressPayParameter()))) {
		 * addFieldError(CrmFieldType.EXPRESS_PAY_PARAMETER.getName(),
		 * validator.getResonseObject().getResponseMessage()); } }
		 */
		
		/*
		 * if (!(validator.validateBlankField(getAllowDuplicateSaleOrderId()))) { if
		 * (!(validator.validateField(CrmFieldType.YES_NO_NOTSURE,
		 * getAllowDuplicateSaleOrderId()))) {
		 * addFieldError(CrmFieldType.YES_NO_NOTSURE.getName(),
		 * validator.getResonseObject().getResponseMessage()); } }
		 * 
		 * if (!(validator.validateBlankField(getAllowDuplicateRefundOrderId()))) { if
		 * (!(validator.validateField(CrmFieldType.YES_NO_NOTSURE,
		 * getAllowDuplicateRefundOrderId()))) {
		 * addFieldError(CrmFieldType.YES_NO_NOTSURE.getName(),
		 * validator.getResonseObject().getResponseMessage()); } }
		 * 
		 * if (!(validator.validateBlankField(getAllowDuplicateSaleOrderIdInRefund())))
		 * { if (!(validator.validateField(CrmFieldType.YES_NO_NOTSURE,
		 * getAllowDuplicateSaleOrderIdInRefund()))) {
		 * addFieldError(CrmFieldType.YES_NO_NOTSURE.getName(),
		 * validator.getResonseObject().getResponseMessage()); } }
		 * 
		 * if (!(validator.validateBlankField(getAllowDuplicateRefundOrderIdSale()))) {
		 * if (!(validator.validateField(CrmFieldType.YES_NO_NOTSURE,
		 * getAllowDuplicateRefundOrderIdSale()))) {
		 * addFieldError(CrmFieldType.YES_NO_NOTSURE.getName(),
		 * validator.getResonseObject().getResponseMessage()); } }
		 */

		/*
		 * if (!(validator.validateBlankField(getStage()))) { if
		 * (!(validator.validateField(CrmFieldType.MPA_STAGE, getStage()))) {
		 * addFieldError(CrmFieldType.MPA_STAGE.getName(),
		 * validator.getResonseObject().getResponseMessage()); } }
		 */

	}
	
	public String getMerchantSupportLandLine() {
		return merchantSupportLandLine;
	}

	public void setMerchantSupportLandLine(String merchantSupportLandLine) {
		this.merchantSupportLandLine = merchantSupportLandLine;
	}

	public String getMerchantSupportEmailId() {
		return merchantSupportEmailId;
	}

	public void setMerchantSupportEmailId(String merchantSupportEmailId) {
		this.merchantSupportEmailId = merchantSupportEmailId;
	}

	public String getMerchantSupportMobileNumber() {
		return merchantSupportMobileNumber;
	}

	public void setMerchantSupportMobileNumber(String merchantSupportMobileNumber) {
		this.merchantSupportMobileNumber = merchantSupportMobileNumber;
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

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactMobile() {
		return contactMobile;
	}

	public void setContactMobile(String contactMobile) {
		this.contactMobile = contactMobile;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getContactLandline() {
		return contactLandline;
	}

	public void setContactLandline(String contactLandline) {
		this.contactLandline = contactLandline;
	}

	public String getDirector1FullName() {
		return director1FullName;
	}

	public void setDirector1FullName(String director1FullName) {
		this.director1FullName = director1FullName;
	}

	public String getDirector1Pan() {
		return director1Pan;
	}

	public void setDirector1Pan(String director1Pan) {
		this.director1Pan = director1Pan;
	}

	public String getDirector1Email() {
		return director1Email;
	}

	public void setDirector1Email(String director1Email) {
		this.director1Email = director1Email;
	}

	public String getDirector1Mobile() {
		return director1Mobile;
	}

	public void setDirector1Mobile(String director1Mobile) {
		this.director1Mobile = director1Mobile;
	}

	public String getDirector1Landline() {
		return director1Landline;
	}

	public void setDirector1Landline(String director1Landline) {
		this.director1Landline = director1Landline;
	}

	public String getDirector1Address() {
		return director1Address;
	}

	public void setDirector1Address(String director1Address) {
		this.director1Address = director1Address;
	}

	public String getDirector1DOB() {
		return director1DOB;
	}

	public void setDirector1DOB(String director1dob) {
		director1DOB = director1dob;
	}

	public String getDirector2FullName() {
		return director2FullName;
	}

	public void setDirector2FullName(String director2FullName) {
		this.director2FullName = director2FullName;
	}

	public String getDirector2Pan() {
		return director2Pan;
	}

	public void setDirector2Pan(String director2Pan) {
		this.director2Pan = director2Pan;
	}

	public String getDirector2Email() {
		return director2Email;
	}

	public void setDirector2Email(String director2Email) {
		this.director2Email = director2Email;
	}

	public String getDirector2Mobile() {
		return director2Mobile;
	}

	public void setDirector2Mobile(String director2Mobile) {
		this.director2Mobile = director2Mobile;
	}

	public String getDirector2Landline() {
		return director2Landline;
	}

	public void setDirector2Landline(String director2Landline) {
		this.director2Landline = director2Landline;
	}

	public String getDirector2Address() {
		return director2Address;
	}

	public void setDirector2Address(String director2Address) {
		this.director2Address = director2Address;
	}

	public String getDirector2DOB() {
		return director2DOB;
	}

	public void setDirector2DOB(String director2dob) {
		director2DOB = director2dob;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getAccountIfsc() {
		return accountIfsc;
	}

	public void setAccountIfsc(String accountIfsc) {
		this.accountIfsc = accountIfsc;
	}

	public String getAccountHolderName() {
		return accountHolderName;
	}

	public void setAccountHolderName(String accountHolderName) {
		this.accountHolderName = accountHolderName;
	}

	public String getAccountMobileNumber() {
		return accountMobileNumber;
	}

	public void setAccountMobileNumber(String accountMobileNumber) {
		this.accountMobileNumber = accountMobileNumber;
	}

	public String getAnnualTurnover() {
		return annualTurnover;
	}

	public void setAnnualTurnover(String annualTurnover) {
		this.annualTurnover = annualTurnover;
	}

	public String getAnnualTurnoverOnline() {
		return annualTurnoverOnline;
	}

	public void setAnnualTurnoverOnline(String annualTurnoverOnline) {
		this.annualTurnoverOnline = annualTurnoverOnline;
	}

	public String getPercentageCC() {
		return percentageCC;
	}

	public void setPercentageCC(String percentageCC) {
		this.percentageCC = percentageCC;
	}

	public String getPercentageDC() {
		return percentageDC;
	}

	public void setPercentageDC(String percentageDC) {
		this.percentageDC = percentageDC;
	}

	public String getPercentageDomestic() {
		return percentageDomestic;
	}

	public void setPercentageDomestic(String percentageDomestic) {
		this.percentageDomestic = percentageDomestic;
	}

	public String getPercentageInternational() {
		return percentageInternational;
	}

	public void setPercentageInternational(String percentageInternational) {
		this.percentageInternational = percentageInternational;
	}

	public String getPercentageCD() {
		return percentageCD;
	}

	public void setPercentageCD(String percentageCD) {
		this.percentageCD = percentageCD;
	}

	public String getPercentageNeftOrImpsOrRtgs() {
		return percentageNeftOrImpsOrRtgs;
	}

	public void setPercentageNeftOrImpsOrRtgs(String percentageNeftOrImpsOrRtgs) {
		this.percentageNeftOrImpsOrRtgs = percentageNeftOrImpsOrRtgs;
	}

	public String getPercentageNB() {
		return percentageNB;
	}

	public void setPercentageNB(String percentageNB) {
		this.percentageNB = percentageNB;
	}

	public String getPercentageUP() {
		return percentageUP;
	}

	public void setPercentageUP(String percentageUP) {
		this.percentageUP = percentageUP;
	}

	public String getPercentageWL() {
		return percentageWL;
	}

	public void setPercentageWL(String percentageWL) {
		this.percentageWL = percentageWL;
	}

	public String getPercentageEM() {
		return percentageEM;
	}

	public void setPercentageEM(String percentageEM) {
		this.percentageEM = percentageEM;
	}

	public String getThirdPartyForCardData() {
		return thirdPartyForCardData;
	}

	public void setThirdPartyForCardData(String thirdPartyForCardData) {
		this.thirdPartyForCardData = thirdPartyForCardData;
	}

	/*
	 * public String getRefundPolicyFilePath() { return refundPolicyFilePath; }
	 * 
	 * public void setRefundPolicyFilePath(String refundPolicyFilePath) {
	 * this.refundPolicyFilePath = refundPolicyFilePath; }
	 */

	public String getRefundsAllowed() {
		return refundsAllowed;
	}

	public void setRefundsAllowed(String refundsAllowed) {
		this.refundsAllowed = refundsAllowed;
	}

	public String getTechnicalContactName() {
		return technicalContactName;
	}

	public void setTechnicalContactName(String technicalContactName) {
		this.technicalContactName = technicalContactName;
	}

	public String getTechnicalContactMobile() {
		return technicalContactMobile;
	}

	public void setTechnicalContactMobile(String technicalContactMobile) {
		this.technicalContactMobile = technicalContactMobile;
	}

	public String getTechnicalContactEmail() {
		return technicalContactEmail;
	}

	public void setTechnicalContactEmail(String technicalContactEmail) {
		this.technicalContactEmail = technicalContactEmail;
	}

	public String getTechnicalContactLandline() {
		return technicalContactLandline;
	}

	public void setTechnicalContactLandline(String technicalContactLandline) {
		this.technicalContactLandline = technicalContactLandline;
	}

	public String getServerDetails() {
		return serverDetails;
	}

	public void setServerDetails(String serverDetails) {
		this.serverDetails = serverDetails;
	}

	public String getServerCompanyName() {
		return serverCompanyName;
	}

	public void setServerCompanyName(String serverCompanyName) {
		this.serverCompanyName = serverCompanyName;
	}

	public String getServerCompanyLandline() {
		return serverCompanyLandline;
	}

	public void setServerCompanyLandline(String serverCompanyLandline) {
		this.serverCompanyLandline = serverCompanyLandline;
	}

	public String getServerCompanyAddress() {
		return serverCompanyAddress;
	}

	public void setServerCompanyAddress(String serverCompanyAddress) {
		this.serverCompanyAddress = serverCompanyAddress;
	}

	public String getServerCompanyMobile() {
		return serverCompanyMobile;
	}

	public void setServerCompanyMobile(String serverCompanyMobile) {
		this.serverCompanyMobile = serverCompanyMobile;
	}

	public String getOperatingSystem() {
		return operatingSystem;
	}

	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}

	public String getBackendTechnology() {
		return backendTechnology;
	}

	public void setBackendTechnology(String backendTechnology) {
		this.backendTechnology = backendTechnology;
	}

	public String getApplicationServerTechnology() {
		return applicationServerTechnology;
	}

	public void setApplicationServerTechnology(String applicationServerTechnology) {
		this.applicationServerTechnology = applicationServerTechnology;
	}

	public String getProductionServerIp() {
		return productionServerIp;
	}

	public void setProductionServerIp(String productionServerIp) {
		this.productionServerIp = productionServerIp;
	}

	public String getSurcharge() {
		return surcharge;
	}

	public void setSurcharge(String surcharge) {
		this.surcharge = surcharge;
	}

	public String getIntegrationType() {
		return integrationType;
	}

	public void setIntegrationType(String integrationType) {
		this.integrationType = integrationType;
	}

	public String getCustomizedInvoiceDesign() {
		return customizedInvoiceDesign;
	}

	public void setCustomizedInvoiceDesign(String customizedInvoiceDesign) {
		this.customizedInvoiceDesign = customizedInvoiceDesign;
	}

	public String getInternationalCards() {
		return internationalCards;
	}

	public void setInternationalCards(String internationalCards) {
		this.internationalCards = internationalCards;
	}

	public String getExpressPay() {
		return expressPay;
	}

	public void setExpressPay(String expressPay) {
		this.expressPay = expressPay;
	}

	public String getExpressPayParameter() {
		return expressPayParameter;
	}

	public void setExpressPayParameter(String expressPayParameter) {
		this.expressPayParameter = expressPayParameter;
	}

	public String getAllowDuplicateSaleOrderId() {
		return allowDuplicateSaleOrderId;
	}

	public void setAllowDuplicateSaleOrderId(String allowDuplicateSaleOrderId) {
		this.allowDuplicateSaleOrderId = allowDuplicateSaleOrderId;
	}

	public String getAllowDuplicateRefundOrderId() {
		return allowDuplicateRefundOrderId;
	}

	public void setAllowDuplicateRefundOrderId(String allowDuplicateRefundOrderId) {
		this.allowDuplicateRefundOrderId = allowDuplicateRefundOrderId;
	}

	public String getAllowDuplicateSaleOrderIdInRefund() {
		return allowDuplicateSaleOrderIdInRefund;
	}

	public void setAllowDuplicateSaleOrderIdInRefund(String allowDuplicateSaleOrderIdInRefund) {
		this.allowDuplicateSaleOrderIdInRefund = allowDuplicateSaleOrderIdInRefund;
	}

	public String getAllowDuplicateRefundOrderIdSale() {
		return allowDuplicateRefundOrderIdSale;
	}

	public void setAllowDuplicateRefundOrderIdSale(String allowDuplicateRefundOrderIdSale) {
		this.allowDuplicateRefundOrderIdSale = allowDuplicateRefundOrderIdSale;
	}

	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public Object getMpaData() {
		return mpaData;
	}

	public void setMpaData(Object mpaData) {
		this.mpaData = mpaData;
	}

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}

	public String getMerchantType() {
		return merchantType;
	}

	public void setMerchantType(String merchantType) {
		this.merchantType = merchantType;
	}
	
	public String getIndustryCategory() {
		return industryCategory;
	}

	public void setIndustryCategory(String industryCategory) {
		this.industryCategory = industryCategory;
	}

	public Map<String, String> getIndustryCategoryList() {
		return industryCategoryList;
	}

	public void setIndustryCategoryList(Map<String, String> industryCategoryList) {
		this.industryCategoryList = industryCategoryList;
	}
}
