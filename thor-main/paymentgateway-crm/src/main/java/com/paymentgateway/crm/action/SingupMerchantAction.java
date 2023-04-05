package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


import com.paymentgateway.commons.dao.CheckerMakerDao;
import com.paymentgateway.commons.email.EmailServiceProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.CheckerMaker;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.crm.actionBeans.CreateNewUser;

public class SingupMerchantAction extends AbstractSecureAction {

	@Autowired
	private EmailServiceProvider emailServiceProvider;

	@Autowired
	private CrmValidator validator;

	@Autowired
	private CreateNewUser createUser;

	@Autowired
	private CheckerMakerDao checkerMakerDao;
	
	@Autowired
	private UserDao userDao;

	private static Logger logger = LoggerFactory.getLogger(SingupMerchantAction.class.getName());
	private static final long serialVersionUID = 5995449017764989418L;

	private String emailId;
	private String businessName;
	private String mobile;
	private String confirmPin;
	private String userRoleType;
	private String pin;
	private String industryCategory;
	private String industrySubCategory;
	private String mpaOnlineOffLineFlag;
	private ResponseObject responseObject = new ResponseObject();
	private Map<String, String> industryCategoryList = new TreeMap<String, String>();
	private Map<String, String> industrySubCategoryList = new TreeMap<String, String>();
	private String superMerchant;
	private String isPartner;
	private List<Merchants> superMerchantList = new ArrayList<Merchants>();
	
	public String execute() {
		List<CheckerMaker> checkerMakerList = new ArrayList<CheckerMaker>();
		setSuperMerchantList(userDao.getSuperMerchantList());
		CheckerMaker checkerMaker = new CheckerMaker();
		User user = null;
		try {
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			checkerMakerList = checkerMakerDao.findAllChekerMaker();
			for (CheckerMaker ckMk : checkerMakerList) {
				if (ckMk.getIndustryCategory().equalsIgnoreCase(getIndustryCategory())) {
					checkerMaker = ckMk;
					break;
				}
			}
			user = getUserInstance(checkerMaker);
			if (userRoleType.equals(CrmFieldConstants.USER_RESELLER_TYPE.getValue())) {
				responseObject = createUser.createUser(user, UserType.RESELLER, "",
						sessionUser, getIsPartner());
			} 
			else if (userRoleType.equals(CrmFieldConstants.SUPER_MERCHANT_TYPE.getValue())) {
				responseObject = createUser.createUser(user, UserType.SUPERMERCHANT, "",
						sessionUser,"");
			}
			else if (userRoleType.equals(CrmFieldConstants.SUB_MERCHANT_TYPE.getValue())) {
				responseObject = createUser.createSubMerchant(user, UserType.SUBMERCHANT, "",
						sessionUser,superMerchant);
			}
			else {
				responseObject = createUser.createUser(user, UserType.MERCHANT, "",
						sessionUser,"");
			}

			if (!ErrorType.SUCCESS.getResponseCode().equals(responseObject.getResponseCode())) {
				addActionMessage(responseObject.getResponseMessage());

			}
			if (ErrorType.SUCCESS.getResponseCode().equals(responseObject.getResponseCode())) {
				responseObject.setResponseMessage(user.getBusinessName() + " Registered Successfully");
				addActionMessage(responseObject.getResponseMessage());
			}
			// Sending Email for Email Validation
			emailServiceProvider.emailValidator(responseObject);
			
			if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				setSuperMerchantList(userDao.getSuperMerchantList());
				return SUCCESS;
			}
			
			else if (sessionUser.getUserType().equals(UserType.RESELLER) ) {
				setSuperMerchantList(userDao.getSuperMerchantList());
				return "reseller";
			}
			else if (sessionUser.getUserType().equals(UserType.MERCHANT) ) {
				
				if (sessionUser.isSuperMerchant()) {
					Merchants merchant = new Merchants();
					merchant.setEmailId(sessionUser.getEmailId());
					merchant.setBusinessName(sessionUser.getBusinessName());
					merchant.setPayId(sessionUser.getPayId());
					merchant.setSuperMerchantId(sessionUser.getSuperMerchantId());
					superMerchantList.add(merchant);
					return "superMerchant";
				}
				else {
					return SUCCESS;
				}
				
			}
			else {
				return SUCCESS;
			}
			

		
		//	return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	private User getUserInstance(CheckerMaker checkerMaker) {
		User user = new User();
		user.setEmailId(getEmailId());

		if (StringUtils.isNotBlank(getPin())) {
			user.setPin(getPin());
		}
		user.setMobile(getMobile());
		user.setBusinessName(getBusinessName());
		user.setBusinessName(getBusinessName());
	
		user.setTransactionMerchantSMSFlag(false);
		user.setTransactionCustomerSMSFlag(false);
		if(!StringUtils.isEmpty(mpaOnlineOffLineFlag) && mpaOnlineOffLineFlag.equalsIgnoreCase("online"))
			user.setMpaOnlineFlag(true);
		else
			user.setMpaOnlineFlag(false);

		if (userRoleType.equals(CrmFieldConstants.USER_RESELLER_TYPE.getValue())) {
		} else {
			user.setIndustryCategory(industryCategory);
			user.setIndustrySubCategory(industrySubCategory);
			if (checkerMaker != null) {
				user.setCheckerPayId(checkerMaker.getCheckerPayId());
				user.setMakerPayId(checkerMaker.getMakerPayId());
				user.setCheckerName(checkerMaker.getCheckerName());
				user.setMakerName(checkerMaker.getMakerName());
			}
		}
		return user;
	}

	private Long getRandomNumber() {
		
		boolean isDuplicateVirtualAccountNo=false;
		
		Long randomNum;
		
		do{
			logger.info("generating random number");
			randomNum=(long) Math.round(Math.round(Math.random()*(99999999 - 10000000)+ 10000000));
			isDuplicateVirtualAccountNo=userDao.checkDuplicateVirtualAccountNo("LETZ"+randomNum);
			logger.info("virtual Account is "+"LETZ"+randomNum+" found duplicate? "+isDuplicateVirtualAccountNo);
		}while(isDuplicateVirtualAccountNo);
		
		
		return randomNum;
	}

	public void validate() {

		// Validate blank and invalid fields
		if (validator.validateBlankField(getPin())) {
			addFieldError(CrmFieldType.PIN.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.isValidPin(getPin()))) {
			addFieldError(CrmFieldType.PIN.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}

		if ((validator.validateBlankField(getBusinessName()))) {
			addFieldError(CrmFieldType.BUSINESS_NAME.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.BUSINESS_NAME, getBusinessName()))) {
			addFieldError(CrmFieldType.BUSINESS_NAME.getName(), validator.getResonseObject().getResponseMessage());
		}

		if (validator.validateBlankField(getMobile())) {
			addFieldError(CrmFieldType.MOBILE.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.MOBILE, getMobile()))) {
			addFieldError(CrmFieldType.MOBILE.getName(), validator.getResonseObject().getResponseMessage());
		}

		if (validator.validateBlankField(getEmailId())) {
			addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.isValidEmailId(getEmailId()))) {
			addFieldError(CrmFieldType.EMAILID.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
	}
	
	
	public String getMpaOnlineOffLineFlag() {
		return mpaOnlineOffLineFlag;
	}

	public void setMpaOnlineOffLineFlag(String mpaOnlineOffLineFlag) {
		this.mpaOnlineOffLineFlag = mpaOnlineOffLineFlag;
	}

	public Map<String, String> getIndustrySubCategoryList() {
		return industrySubCategoryList;
	}

	public void setIndustrySubCategoryList(Map<String, String> industrySubCategoryList) {
		this.industrySubCategoryList = industrySubCategoryList;
	}

	public Map<String, String> getIndustryCategoryList() {
		return industryCategoryList;
	}

	public void setIndustryCategoryList(Map<String, String> industryCategoryList) {
		this.industryCategoryList = industryCategoryList;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getIndustryCategory() {
		return industryCategory;
	}

	public void setIndustryCategory(String industryCategory) {
		this.industryCategory = industryCategory;
	}

	public String getIndustrySubCategory() {
		return industrySubCategory;
	}

	public void setIndustrySubCategory(String industrySubCategory) {
		this.industrySubCategory = industrySubCategory;
	}

	public String getUserRoleType() {
		return userRoleType;
	}

	public void setUserRoleType(String userRoleType) {
		this.userRoleType = userRoleType;
	}

	public ResponseObject getResponseObject() {
		return responseObject;
	}

	public void setResponseObject(ResponseObject responseObject) {
		this.responseObject = responseObject;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public List<Merchants> getSuperMerchantList() {
		return superMerchantList;
	}

	public void setSuperMerchantList(List<Merchants> superMerchantList) {
		this.superMerchantList = superMerchantList;
	}

	public String getSuperMerchant() {
		return superMerchant;
	}

	public void setSuperMerchant(String superMerchant) {
		this.superMerchant = superMerchant;
	}
	
	public String getIsPartner() {
		return isPartner;
	}

	public void setIsPartner(String isPartner) {
		this.isPartner = isPartner;
	}
}
