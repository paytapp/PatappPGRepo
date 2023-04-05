package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.Permissions;
import com.paymentgateway.commons.user.Roles;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.SaltFactory;
import com.paymentgateway.commons.util.UserStatusType;
import com.paymentgateway.crm.actionBeans.CurrencyMapProvider;
import com.paymentgateway.crm.actionBeans.MerchantRecordUpdater;

public class ResellerAccountUpdateAction extends AbstractSecureAction
		implements ServletRequestAware, ModelDriven<User> {

	@Autowired
	private UserDao userDao;

	@Autowired
	private CrmValidator crmValidator;

	@Autowired
	private CurrencyMapProvider currencyMapProvider;

	@Autowired
	private MerchantRecordUpdater merchantRecordUpdater;
	
	@Autowired
	private UserSettingDao userSettingDao;

	private static Logger logger = LoggerFactory.getLogger(ResellerAccountUpdateAction.class.getName());
	private static final long serialVersionUID = -7165881905141203999L;
	private User user = new User();
	private File logoImage;
	private String merchantLogo;
	private String salt;
	private HttpServletRequest request;
	private String defaultCurrency;
	private Map<String, String> currencyMap = new LinkedHashMap<String, String>();

	private List<String> lstPermissionType;
	private List<PermissionType> listPermissionType;
	private String permissionString = "";

	public String saveResellerAction() {

		Date date = new Date();
		try {
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			setSalt(SaltFactory.getSaltProperty(user));
			User dbUser = userDao.findPayId(user.getPayId());
			user.setBusinessName(dbUser.getBusinessName());
			user.setUserType(UserType.RESELLER);
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {

				if (user.getUserStatus().toString().equals(UserStatusType.ACTIVE.getStatus().toString())) {
					user.setActivationDate(date);
				} else if (user.getUserStatus().toString().equals(UserStatusType.SUSPENDED.getStatus().toString())
						|| user.getUserStatus().toString()
								.equals(UserStatusType.TRANSACTION_BLOCKED.getStatus().toString())) {
					user.setActivationDate(null);
					logger.info("Merchant EmailId" + ":" + user.getEmailId() + "," + "Merchant Status" + ":"
							+ user.getUserStatus().getStatus() + "," + "Ip Address" + ":" + request.getRemoteAddr());
				}
				editPermission(user);
				setUser(merchantRecordUpdater.updateResellerDetails(user));
				saveMerchantLogo(logoImage, user.getResellerId());
				addActionMessage(CrmFieldConstants.USER_DETAILS_UPDATED.getValue());
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

	private void editPermission(User user) {
		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {

			Set<Roles> roles = user.getRoles();
			Iterator<Roles> itr = roles.iterator();
			Roles role = new Roles();
			if (sessionUser.getUserType().equals(UserType.SUBADMIN)
					|| sessionUser.getUserType().equals(UserType.ADMIN)) {
				if (!roles.isEmpty()) {
					role = itr.next();
					Iterator<Permissions> permissionIterator = role.getPermissions().iterator();
					while (permissionIterator.hasNext()) {
						// not used but compulsory for iterator working
						@SuppressWarnings("unused")
						Permissions permission = permissionIterator.next();
						permissionIterator.remove();
					}
				}
				setListPermissionType(PermissionType.getResellerPermissionTypeByCategory(7));
				listPermissionType.add(PermissionType.getInstanceFromName("Quick Search"));
				if (lstPermissionType == null) {

				} else {
					for (String permissionType : lstPermissionType) {
						Permissions permission = new Permissions();
						permission.setPermissionType(PermissionType.getInstanceFromName(permissionType));
						role.addPermission(permission);
					}
				}
				user.addRole(role);
				getPermissions(user);
			}
			
		} catch(Exception ex){
			logger.error("Exception in editPermission : ", ex);
		}
	}

private void getPermissions(User agent) {
		
		try{
			Set<Roles> roles = agent.getRoles();
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
		
		catch(Exception e){
			logger.error("Can not find permissions ", e);
		}
		
	}

	@SuppressWarnings("unchecked")
	public void validate() {
		User userDB = new User();
		userDB = getUser();
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
		/*
		 * if ((crmValidator.validateBlankField(user.getBusinessType()))) { } else if
		 * (!(crmValidator.validateField(CrmFieldType.BUSINESS_TYPE,
		 * user.getBusinessType()))) {
		 * addFieldError(CrmFieldType.BUSINESS_TYPE.getName(), crmValidator
		 * .getResonseObject().getResponseMessage()); }
		 */
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
			addFieldError(CrmFieldType.IFSC_CODE.getName(), crmValidator.getResonseObject().getResponseMessage());
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
		} else if (!(crmValidator.isValidEmailId(user.getTransactionEmailId()))) {
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
		if (!getFieldErrors().isEmpty()) {
			user.setUserType(UserType.MERCHANT);
			currencyMap = currencyMapProvider.currencyMap(user);

		}
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
		UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());
		if (merchantSettings.isLogoFlag()) {
			file = new File(PropertiesManager.propertiesMap.get(Constants.LOGO_FILE_UPLOAD_LOCATION.getValue()) + "//"
					+ user.getPayId(), user.getPayId() + ".png");
		} else {
				return "";
		}
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


	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getModel() {
		return user;
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

	public Map<String, String> getCurrencyMap() {
		return currencyMap;
	}

	public void setCurrencyMap(Map<String, String> currencyMap) {
		this.currencyMap = currencyMap;
	}

	public String getDefaultCurrency() {
		return defaultCurrency;
	}

	public void setDefaultCurrency(String defaultCurrency) {
		this.defaultCurrency = defaultCurrency;
	}

	public List<String> getLstPermissionType() {
		return lstPermissionType;
	}

	public void setLstPermissionType(List<String> lstPermissionType) {
		this.lstPermissionType = lstPermissionType;
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

	public String getMerchantLogo() {
		return merchantLogo;
	}

	public void setMerchantLogo(String merchantLogo) {
		this.merchantLogo = merchantLogo;
	}

	public File getLogoImage() {
		return logoImage;
	}

	public void setLogoImage(File logoImage) {
		this.logoImage = logoImage;
	}

}