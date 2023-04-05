package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TxnType;
import com.paymentgateway.crm.actionBeans.CurrencyMapProvider;

/**
 * @author Chandan
 *
 */
public class RefundMerchantNameAction extends AbstractSecureAction {
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private CurrencyMapProvider currencyMapProvider;
	

	private static Logger logger = LoggerFactory.getLogger(RefundMerchantNameAction.class.getName());
	private static final long serialVersionUID = -5990800125330748024L;

	private List<Merchants> merchantList = new ArrayList<Merchants>();
	private Map<String, String> currencyMap = new HashMap<String, String>();
	private User sessionUser = null;
	private List<StatusType> lst;
	private List<TxnType> txnTypelist;
	private List<Merchants> subMerchantList = new ArrayList<Merchants>();
	private boolean retailMerchantFlag = false;
	private List<Merchants> subUserList = new ArrayList<Merchants>();
	private boolean superMerchant = false;
	
	@SuppressWarnings("unchecked")
	public String execute() {
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			UserSettingData userSettings=(UserSettingData) sessionMap.get(Constants.USER_SETTINGS);

			if(sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				setMerchantList(userDao.getAllStatusMerchantList());
				currencyMap = Currency.getAllCurrency();
			}else if(sessionUser.getUserType().equals(UserType.RESELLER)) {
				setMerchantList(userDao.getActiveResellerMerchants(sessionUser.getResellerId()));
				currencyMap = Currency.getAllCurrency();
			}else if(sessionUser.getUserType().equals(UserType.MERCHANT) || sessionUser.getUserType().equals(UserType.SUBUSER)  || sessionUser.getUserType().equals(UserType.SUBACQUIRER)) {
				Merchants merchant = new Merchants();
				merchant.setEmailId(sessionUser.getEmailId());
				merchant.setBusinessName(sessionUser.getBusinessName());
				merchant.setPayId(sessionUser.getPayId());
				merchantList.add(merchant);
				if(sessionUser.getUserType().equals(UserType.MERCHANT) && sessionUser.isSuperMerchant() == true) {
					setSubMerchantList(userDao.getSubMerchantListBySuperPayId(merchant.getPayId()));
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
				if(sessionUser.getUserType().equals(UserType.SUBUSER) || sessionUser.getUserType().equals(UserType.SUBACQUIRER)){
					String parentMerchantPayId = sessionUser.getParentPayId();
					User parentMerchant = userDao
							.findPayId(parentMerchantPayId);
					merchant.setMerchant(parentMerchant);
					merchantList.add(merchant);
					
					if (parentMerchant.getUserType().equals(UserType.MERCHANT) && parentMerchant.isSuperMerchant()) {
						if(sessionUser.getSubUserType().equals("normalType")){
							setSubMerchantList(userDao.getSubMerchantListBySuperPayId(parentMerchant.getPayId()));
							setSuperMerchant(true);
						}else{
							setSuperMerchant(false);
						}
					}
					
					Object[] obj = merchantList.toArray();
					for(Object sortList : obj){
						if(merchantList.indexOf(sortList) != merchantList.lastIndexOf(sortList)){
							merchantList.remove(merchantList.lastIndexOf(sortList));
						}
					}
				}
				
				// Changed by Shaiwal for handling Sub User login when user setting is not present for SubUser
				if(sessionUser.getUserType().equals(UserType.SUBUSER)){
					
					String parentMerchantPayId = sessionUser.getParentPayId();
					User parentMerchant = userDao
							.findPayId(parentMerchantPayId);
					setRetailMerchantFlag(parentMerchant.isRetailMerchantFlag());
				}
				else {
					setRetailMerchantFlag(userSettings.isRetailMerchantFlag());
				}
				
				//setRetailMerchantFlag(userSettings.isRetailMerchantFlag());
 				currencyMap = currencyMapProvider.currencyMap(sessionUser);
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
				setMerchantList(userDao.getSubUserList(sessionUser
						.getPayId()));
				currencyMap = Currency.getSupportedCurreny(sessionUser);
			} else if (sessionUser.getUserType().equals(UserType.SUBUSER) || sessionUser.getUserType().equals(UserType.SUBACQUIRER)) {
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
	public List<Merchants> getSubMerchantList() {
		return subMerchantList;
	}

	public void setSubMerchantList(List<Merchants> subMerchantList) {
		this.subMerchantList = subMerchantList;
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

	public boolean isSuperMerchant() {
		return superMerchant;
	}

	public void setSuperMerchant(boolean superMerchant) {
		this.superMerchant = superMerchant;
	}
	

}
