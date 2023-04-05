package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TxnType;
import com.paymentgateway.crm.actionBeans.CurrencyMapProvider;
import com.paymentgateway.crm.mongoReports.TxnReports;

/**
 * @author Chandan
 *
 */
public class MerchantNameAction extends AbstractSecureAction {

	@Autowired
	private UserDao userDao;

	@Autowired
	private CurrencyMapProvider currencyMapProvider;

	@Autowired
	private TxnReports txnReports;
	
	@Autowired
	private UserSettingDao userSettingDao;

	@Autowired
	private PropertiesManager propertiesManager;

	private static Logger logger = LoggerFactory.getLogger(MerchantNameAction.class.getName());
	private static final long serialVersionUID = -5990800125330748024L;

	private String payId;
	private String response;

	private List<Merchants> merchantList = new ArrayList<Merchants>();
	private Map<String, String> currencyMap = new HashMap<String, String>();
	private User sessionUser = null;
	private List<StatusType> lst;
	private List<TxnType> txnTypelist;
	private List<Merchants> merchantAndSuperMechantList = new ArrayList<Merchants>();
	private List<Merchants> subMerchantList = new ArrayList<Merchants>();
	private boolean glocalFlag = false;
	private boolean isSuperMerchant = false;
	public boolean retailMerchantFlag = false;
	private List<Merchants> subUserList = new ArrayList<Merchants>();
	private String merchPayIdforVendor;
	public boolean vendorReportFlag = false;
	private boolean eNachReport;
	private boolean virtualAccountFlag;
	private boolean accountVerification;
	private boolean merchantInitiatedDirectFlag;
	private boolean bookingReportFlag;
	private boolean customerQrFlag;
	private boolean capturedMerchantFlag;
	private boolean upiAutoPayReport;
	
	@SuppressWarnings("unchecked")
	public String execute() {
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			
			if(sessionUser.getUserType().equals(UserType.RECONUSER)) {
				Merchants merchant = new Merchants();
				 merchant.setEmailId(sessionUser.getEmailId());
                 merchant.setBusinessName(sessionUser.getBusinessName());
                 
                 merchant.setPayId(sessionUser.getPayId());
                 merchant.setIsSuperMerchant(false);
                 merchantList.add(merchant);
			}
			
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				setMerchantList(userDao.getAllStatusMerchantList());
				currencyMap = Currency.getAllCurrency();
				setMerchantAndSuperMechantList(userDao.getMerchantAndSuperMerchantList());
			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				setMerchantList(userDao.getActiveResellerMerchants(sessionUser.getResellerId()));
				currencyMap = Currency.getAllCurrency();
			} else if (sessionUser.getUserType().equals(UserType.MERCHANT)
					|| sessionUser.getUserType().equals(UserType.SUBUSER)
					|| sessionUser.getUserType().equals(UserType.SUBACQUIRER)) {
				Merchants merchant = new Merchants();
				if (sessionUser.isSuperMerchant()) {
					merchant.setEmailId(sessionUser.getEmailId());
					merchant.setBusinessName(sessionUser.getBusinessName());
					merchant.setPayId(sessionUser.getPayId());
					merchant.setIsSuperMerchant(sessionUser.isSuperMerchant());
					merchantList.add(merchant);
					setSubMerchantList(userDao.getSubMerchantListBySuperPayId(sessionUser.getPayId()));
				} else if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					merchantList.addAll(userDao.getSuperOrSubMerchantByPayId(sessionUser.getSuperMerchantId()));

					merchant.setEmailId(sessionUser.getEmailId());
					merchant.setBusinessName(sessionUser.getBusinessName());
					merchant.setPayId(sessionUser.getPayId());
					merchant.setIsSuperMerchant(sessionUser.isSuperMerchant());
					subMerchantList.add(merchant);
				} else {
					merchant.setEmailId(sessionUser.getEmailId());
					merchant.setBusinessName(sessionUser.getBusinessName());
					merchant.setPayId(sessionUser.getPayId());
					merchant.setIsSuperMerchant(sessionUser.isSuperMerchant());
					merchantList.add(merchant);
				}
				if (sessionUser.getUserType().equals(UserType.MERCHANT) && sessionUser.isSuperMerchant() == true) {
					setSubMerchantList(userDao.getSubMerchantListBySuperPayId(merchant.getPayId()));
				}
				String identifierKey = propertiesManager.propertiesMap.get(Constants.MERCHANT_PAYID.getValue());
				if (StringUtils.isNotBlank(identifierKey) && identifierKey.contains(sessionUser.getPayId())) {
					setGlocalFlag(true);
				}

				if (sessionUser.getUserType().equals(UserType.MERCHANT) && sessionUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {

					if (StringUtils.isNotBlank(identifierKey)
							&& identifierKey.contains(sessionUser.getSuperMerchantId())) {
						setGlocalFlag(true);
					}
				}

				if (sessionUser.getUserType().equals(UserType.MERCHANT)){
					List<Merchants> dbSubUserList = userDao.getSubUserList(sessionUser.getPayId());
					for(Merchants subuser : dbSubUserList) {
						
						if(StringUtils.isNotEmpty(subuser.getSubUserType()) && subuser.getSubUserType().equalsIgnoreCase("eposType")) {
							subUserList.add(subuser);
						}
					}
					
					if(!subUserList.isEmpty()) {
						sessionMap.put("SUBUSERFLAG", true);
					}else {
						sessionMap.put("SUBUSERFLAG", false);
					}
				}
				
				if (sessionUser.getUserType().equals(UserType.SUBUSER)
						|| sessionUser.getUserType().equals(UserType.SUBACQUIRER)) {
					String parentMerchantPayId = sessionUser.getParentPayId();
					User parentMerchant = userDao.findPayId(parentMerchantPayId);
					merchant.setMerchant(parentMerchant);
					merchantList.add(merchant);

					if (parentMerchant.getUserType().equals(UserType.MERCHANT)
							&& parentMerchant.isSuperMerchant() == false
							&& StringUtils.isNotBlank(parentMerchant.getSuperMerchantId())) {
						User superMerchant = userDao.findPayId(parentMerchant.getSuperMerchantId());
						if (StringUtils.isNotBlank(identifierKey) && identifierKey.contains(superMerchant.getPayId())) {
							setGlocalFlag(true);
						}
					}else if (parentMerchant.getUserType().equals(UserType.MERCHANT) && parentMerchant.isSuperMerchant()) {
						if(sessionUser.getSubUserType().equals("normalType")){
							setSubMerchantList(userDao.getSubMerchantListBySuperPayId(parentMerchant.getPayId()));
							setSuperMerchant(true);
						}else{
							setSuperMerchant(false);
						}
					}

					if (StringUtils.isNotBlank(identifierKey) && identifierKey.contains(parentMerchantPayId)) {
						setGlocalFlag(true);
					}
					Object[] obj = merchantList.toArray();
					for (Object sortList : obj) {
						if (merchantList.indexOf(sortList) != merchantList.lastIndexOf(sortList)) {
							merchantList.remove(merchantList.lastIndexOf(sortList));
						}
					}
				}
				if(!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					currencyMap = currencyMapProvider.currencyMap(userDao.findPayId(sessionUser.getSuperMerchantId()));
				}else {
					currencyMap = currencyMapProvider.currencyMap(sessionUser);
				}
					
			}
			else if(sessionUser.getUserType().equals(UserType.RECONUSER)) {
				setMerchantList(userDao.getAllStatusMerchantList());
				currencyMap.put(CrmFieldConstants.INR.getValue(), "356");
			}
			setTxnTypelist(TxnType.gettxnType());
			setLst(StatusType.getStatusType());
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}

		return INPUT;
	}

	public String subUserList() {
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				setMerchantList(userDao.getSubUserList(sessionUser.getPayId()));
				currencyMap = Currency.getSupportedCurreny(sessionUser);
			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)
					|| sessionUser.getUserType().equals(UserType.SUBACQUIRER)) {
				Merchants merchant = new Merchants();
				User user = new User();
				user = userDao.findPayId(sessionUser.getParentPayId());
				merchant.setEmailId(user.getEmailId());
				merchant.setBusinessName(user.getBusinessName());
				merchant.setPayId(user.getPayId());
				merchantList.add(merchant);
				currencyMap = Currency.getSupportedCurreny(sessionUser);
			}

			setLst(StatusType.getStatusType());
			setTxnTypelist(TxnType.gettxnType());
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}

		return INPUT;
	}

	public String subMerchantListByEmailId() {

		try {
			User merchant = new User();
			if(StringUtils.isNotEmpty(payId) && payId.contains("@")) {
				merchant = userDao.findByEmailId(payId);
			}else {
				merchant = userDao.findPayId(payId);
			}
			
			UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());
			setRetailMerchantFlag(merchantSettings.isRetailMerchantFlag());
			setSubMerchantList(userDao.getSubMerchantListBySuperPayId(merchant.getPayId()));
			String identifierKey = propertiesManager.propertiesMap.get(Constants.MERCHANT_PAYID.getValue());
			if (StringUtils.isNotBlank(identifierKey) && identifierKey.contains(merchant.getPayId())) {
				setGlocalFlag(true);
			}
			
			if(merchantSettings.iseNachReportFlag()) {
				seteNachReport(true);
			}
			if(merchantSettings.isUpiAutoPayReportFlag()) {
				setUpiAutoPayReport(true);
			}
			if(merchantSettings.isVirtualAccountFlag()) {
				setVirtualAccountFlag(true);
			}
			if (!getSubMerchantList().isEmpty()) {
				setSuperMerchant(true);
			}
			if(merchantSettings.isAccountVerificationFlag()){
				setAccountVerification(true);
			}
			if(merchantSettings.isMerchantInitiatedDirectFlag()){
				setMerchantInitiatedDirectFlag(true);
			}
			if(merchantSettings.isBookingRecord()){
				setBookingReportFlag(true);
			}
			if(merchantSettings.isCustomerQrFlag()){
				setCustomerQrFlag(true);
			}
			if(merchantSettings.isCapturedMerchantFlag()){
				setCapturedMerchantFlag(true);
			}
			List<Merchants> dbSubUserList = userDao.getSubUserList(merchant.getPayId());
			for(Merchants subuser : dbSubUserList) {
				
				if(StringUtils.isNotEmpty(subuser.getSubUserType()) && vendorReportFlag && subuser.getSubUserType().equalsIgnoreCase("vendorType")) {
					subUserList.add(subuser);
				}else if(StringUtils.isNotEmpty(subuser.getSubUserType()) && !vendorReportFlag && subuser.getSubUserType().equalsIgnoreCase("eposType")){
					subUserList.add(subuser);
				}
			}
			
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return SUCCESS;
	}
	
	public String getVendorTypeSubUserList() {
		try {
			
			User merchant = new User();
			if(StringUtils.isNotEmpty(payId) && payId.contains("@")) {
				merchant = userDao.findByEmailId(payId);
			}else {
				merchant = userDao.findPayId(payId);
			}
			
			List<Merchants> SubUserListOfSuperMerchant = null;
			
			List<Merchants> SubUserListOfMerchant = userDao.getSubUserList(merchant.getPayId());
			for(Merchants subuser : SubUserListOfMerchant) {
				
				if(StringUtils.isNotEmpty(subuser.getSubUserType()) && vendorReportFlag && subuser.getSubUserType().equalsIgnoreCase("vendorType")) {
					subUserList.add(subuser);
				}else if(StringUtils.isNotEmpty(subuser.getSubUserType()) && !vendorReportFlag && subuser.getSubUserType().equalsIgnoreCase("eposType")){
					subUserList.add(subuser);
				}
			}
			
			if(merchant.isSuperMerchant() == false && StringUtils.isNotEmpty(merchant.getSuperMerchantId())) {
				
				SubUserListOfSuperMerchant = userDao.getSubUserList(merchant.getSuperMerchantId());
				if(SubUserListOfSuperMerchant != null) {
					for(Merchants subuser : SubUserListOfSuperMerchant) {
						
						if(StringUtils.isNotEmpty(subuser.getSubUserType()) && vendorReportFlag && subuser.getSubUserType().equalsIgnoreCase("vendorType")) {
							subUserList.add(subuser);
						}else if(StringUtils.isNotEmpty(subuser.getSubUserType()) && !vendorReportFlag && subuser.getSubUserType().equalsIgnoreCase("eposType")){
							subUserList.add(subuser);
						}
					}
				}
			}
			
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return SUCCESS;
	}
	
	public String subMerchantListByPayId() {

		try {
			setSubMerchantList(userDao.getSubMerchantListBySuperPayId(payId));
			String identifierKey = propertiesManager.propertiesMap.get(Constants.MERCHANT_PAYID.getValue());
			if (StringUtils.isNotBlank(identifierKey) && identifierKey.contains(payId)) {
				setGlocalFlag(true);
			}
			if (!getSubMerchantList().isEmpty()) {
				setSuperMerchant(true);
			}

			/*
			 * if(getSubMerchantList().isEmpty()) { setResponse(ERROR); } else {
			 * setResponse("SUCCESS"); }
			 */
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return SUCCESS;
	}

	

	public String superMerchantList() {
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				setMerchantList(userDao.getSuperMerchantList());
				currencyMap = Currency.getAllCurrency();
				

			} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {

				if (sessionUser.getUserType().equals(UserType.MERCHANT) && sessionUser.isSuperMerchant() == true
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					// setMerchantList(userDao.getSuperOrSubMerchantByPayId(sessionUser.getPayId()));
					setSubMerchantList(userDao.getSubMerchantListBySuperPayId(sessionUser.getPayId()));
					currencyMap = currencyMapProvider.currencyMap(sessionUser);
				} else if (sessionUser.getUserType().equals(UserType.MERCHANT) && sessionUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {

					setSubMerchantList(userDao.getSuperOrSubMerchantByPayId(sessionUser.getPayId()));
					User parentMerchant = userDao.findBySuperMerchantId(sessionUser.getSuperMerchantId());
					currencyMap = currencyMapProvider.currencyMap(parentMerchant);
				}

			}
		} catch (Exception ex) {

		}
		return INPUT;
	}

	

	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}

	public List<StatusType> getLst() {
		return lst;
	}

	public void setLst(List<StatusType> lst) {
		this.lst = lst;
	}

	public Map<String, String> getCurrencyMap() {
		return currencyMap;
	}

	public void setCurrencyMap(Map<String, String> currencyMap) {
		this.currencyMap = currencyMap;
	}

	public List<TxnType> getTxnTypelist() {
		return txnTypelist;
	}

	public void setTxnTypelist(List<TxnType> txnTypelist) {
		this.txnTypelist = txnTypelist;
	}

	public List<Merchants> getMerchantAndSuperMechantList() {
		return merchantAndSuperMechantList;
	}

	public void setMerchantAndSuperMechantList(List<Merchants> merchantAndSuperMechantList) {
		this.merchantAndSuperMechantList = merchantAndSuperMechantList;
	}

	public List<Merchants> getSubMerchantList() {
		return subMerchantList;
	}

	public void setSubMerchantList(List<Merchants> subMerchantList) {
		this.subMerchantList = subMerchantList;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public Boolean getGlocalFlag() {
		return glocalFlag;
	}

	public void setGlocalFlag(Boolean glocalFlag) {
		this.glocalFlag = glocalFlag;
	}

	public boolean isSuperMerchant() {
		return isSuperMerchant;
	}

	public void setSuperMerchant(boolean isSuperMerchant) {
		this.isSuperMerchant = isSuperMerchant;
	}

	public boolean isRetailMerchantFlag() {
		return retailMerchantFlag;
	}

	public void setRetailMerchantFlag(boolean retailMerchantFlag) {
		this.retailMerchantFlag = retailMerchantFlag;
	}

	public List<Merchants> getSubUserList() {
		return subUserList;
	}

	public void setSubUserList(List<Merchants> subUserList) {
		this.subUserList = subUserList;
	}

	public String getMerchPayIdforVendor() {
		return merchPayIdforVendor;
	}

	public void setMerchPayIdforVendor(String merchPayIdforVendor) {
		this.merchPayIdforVendor = merchPayIdforVendor;
	}

	public boolean isVendorReportFlag() {
		return vendorReportFlag;
	}

	public void setVendorReportFlag(boolean vendorReportFlag) {
		this.vendorReportFlag = vendorReportFlag;
	}
	public boolean iseNachReport() {
		return eNachReport;
	}

	public void seteNachReport(boolean eNachReport) {
		this.eNachReport = eNachReport;
	}

	public boolean isAccountVerification() {
		return accountVerification;
	}

	public void setAccountVerification(boolean accountVerification) {
		this.accountVerification = accountVerification;
	}

	public boolean isMerchantInitiatedDirectFlag() {
		return merchantInitiatedDirectFlag;
	}

	public void setMerchantInitiatedDirectFlag(boolean merchantInitiatedDirectFlag) {
		this.merchantInitiatedDirectFlag = merchantInitiatedDirectFlag;
	}

	public boolean isVirtualAccountFlag() {
		return virtualAccountFlag;
	}

	public void setVirtualAccountFlag(boolean virtualAccountFlag) {
		this.virtualAccountFlag = virtualAccountFlag;
	}

	public boolean isBookingReportFlag() {
		return bookingReportFlag;
	}

	public void setBookingReportFlag(boolean bookingReportFlag) {
		this.bookingReportFlag = bookingReportFlag;
	}

	public boolean isCustomerQrFlag() {
		return customerQrFlag;
	}

	public void setCustomerQrFlag(boolean customerQrFlag) {
		this.customerQrFlag = customerQrFlag;
	}

	public boolean isCapturedMerchantFlag() {
		return capturedMerchantFlag;
	}

	public void setCapturedMerchantFlag(boolean capturedMerchantFlag) {
		this.capturedMerchantFlag = capturedMerchantFlag;
	}
	public boolean isUpiAutoPayReport() {
		return upiAutoPayReport;
	}

	public void setUpiAutoPayReport(boolean upiAutoPayReport) {
		this.upiAutoPayReport = upiAutoPayReport;
	}
}