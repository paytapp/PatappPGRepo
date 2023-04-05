package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.crm.actionBeans.CurrencyMapProvider;

/**
 * Shiva
 */

public class UserSettingAction extends AbstractSecureAction implements ModelDriven<UserSettingData> {

	private static Logger logger = LoggerFactory.getLogger(UserSettingAction.class.getName());
	private static final long serialVersionUID = -2208500327407452217L;

	@Autowired
	UserSettingDao userSettingDao;

	@Autowired
	CurrencyMapProvider currencyMapProvider;

	@Autowired
	UserDao userDao;

	private UserSettingData userSetting = new UserSettingData();
	private Map<String, String> currencyMap = new LinkedHashMap<String, String>();
	private List<Merchants> merchantList = new ArrayList<>(); 
	
	private boolean parentMerchantFlag = false;

	@Override
	public String execute() {

		try {
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			if (sessionUser.getUserType().equals(UserType.ADMIN)) {
				
				User user = userDao.findPayId(userSetting.getPayId());
				
				if(StringUtils.isNotBlank(userSetting.getSuperMerchantName()) && StringUtils.isNotBlank(userSetting.getPayId())){
					userSetting.setSuperMerchantId(userDao.findPayId(userSetting.getPayId()).getSuperMerchantId());
				}else{
					currencyMap = currencyMapProvider.currencyMap(userDao.findPayId(userSetting.getPayId()));
				}
				
				if(user.getUserType().equals(UserType.PARENTMERCHANT)){
					setParentMerchantFlag(true);
				}
				
				userSettingDao.saveOrUpdate(userSetting);
				
			}
		} catch (Exception e) {
			logger.info("exception found in saving user setting flags ", e);
		}

		return SUCCESS;
	}

	public String fetchUserSettingData() {
		logger.info("inside the fetchUserSettingData() for payId " + userSetting.getPayId());

		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		if (StringUtils.isNotBlank(userSetting.getPayId())) {
			setUserSetting(userSettingDao.fetchData(userSetting));
			
			User user = userDao.findPayId(userSetting.getPayId());
			
			if(user.getUserType().equals(UserType.PARENTMERCHANT)){
				setParentMerchantFlag(true);
				setMerchantList(userDao.getActiveMerchant());
			}
			
			if(StringUtils.isBlank(userSetting.getSuperMerchantName())){
				currencyMap = currencyMapProvider.currencyMap(userDao.findPayId(userSetting.getPayId()));
			}
			
		}
		return INPUT;
	}
	
	public String fetchSubMerchantSettingsData() {
		logger.info("inside the fetchSubMerchantSettingsData() for payId " + userSetting.getPayId());

		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		if (StringUtils.isNotBlank(userSetting.getPayId())) {
			setUserSetting(userSettingDao.fetchData(userSetting));
			
			if(StringUtils.isBlank(userSetting.getSuperMerchantName())){
				currencyMap = currencyMapProvider.currencyMap(userDao.findPayId(userSetting.getPayId()));
			}
		}
		return INPUT;
	}

	@Override
	public UserSettingData getModel() {
		return userSetting;
	}

	public Map<String, String> getCurrencyMap() {
		return currencyMap;
	}

	public void setCurrencyMap(Map<String, String> currencyMap) {
		this.currencyMap = currencyMap;
	}

	public UserSettingData getUserSetting() {
		return userSetting;
	}

	public void setUserSetting(UserSettingData userSetting) {
		this.userSetting = userSetting;
	}

	public boolean isParentMerchantFlag() {
		return parentMerchantFlag;
	}

	public void setParentMerchantFlag(boolean parentMerchantFlag) {
		this.parentMerchantFlag = parentMerchantFlag;
	}

	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}
	

}
