package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.user.Merchants;
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
import com.paymentgateway.commons.util.UserStatusType;
import com.paymentgateway.crm.actionBeans.CurrencyMapProvider;

@Service
public class IndexAction extends ForwardAction {

	@Autowired
	private CurrencyMapProvider currencyMapProvider;

	@Autowired
	private UserDao userDao;

	@Autowired
	private CrmValidator validator;

	@Autowired
	private UserSettingDao userSettingDao;

	private static final long serialVersionUID = -4616437586910475430L;

	private static Logger logger = LoggerFactory.getLogger(IndexAction.class.getName());
	private List<Merchants> merchantList = new ArrayList<Merchants>();
	private Map<String, String> currencyMap = new LinkedHashMap<String, String>();
	private List<Merchants> subMerchantList = new ArrayList<Merchants>();
	private User user = new User();
	private String permissionString = "";
	private String reselleId;
	private boolean superMerchant = false;
	private boolean reportPermission = false;
	private boolean qrReportFlag = false;
	private boolean onlineMpaFlag = false;

	@SuppressWarnings("unchecked")
	public String authoriseUser() {
		try {

			user = (User) sessionMap.get(Constants.USER.getValue());
			if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
					|| user.getUserType().equals(UserType.SUPERADMIN) || user.getUserType().equals(UserType.RESELLER)) {

				if (user.getUserType().equals(UserType.RESELLER))
					merchantList = new UserDao().getActiveResellerMerchants(user.getResellerId());
				else
					merchantList = userDao.getMerchantActiveList();

				currencyMap = currencyMapProvider.currencyMap(user);

			}

			else if (user.getUserType().equals(UserType.MERCHANT)) {
				Merchants merchant = new Merchants();
				merchant.setMerchant(user);
				merchantList.add(merchant);

				// List for Super Merchant
				if (user.isSuperMerchant() && user.getUserStatus().equals(UserStatusType.ACTIVE)) {
					merchantList = userDao.getActiveSubMerchants(user);
					for (Merchants merch : merchantList) {
						UserSettingData merchantSettings = userSettingDao.fetchDataUsingPayId(merch.getPayId());
						if (merchantSettings.isCustomerQrFlag()) {
							user.setCustomerQrFlag(true);
							break;
						}
					}
				}
				currencyMap = currencyMapProvider.currencyMap(user);

				// Currency Map For Sub Merchant
				if (!user.isSuperMerchant() && StringUtils.isNotBlank(user.getSuperMerchantId())) {
					User superMerch = userDao.findPayId(user.getSuperMerchantId());
					if (superMerch != null) {
						currencyMap = currencyMapProvider.currencyMap(superMerch);
					}

				}

			} else if (user.getUserType().equals(UserType.PARENTMERCHANT)) {
				Merchants merchant = new Merchants();
				merchant.setMerchant(user);
				merchantList.add(merchant);

				// List for Super Merchant
				if (user.isSuperMerchant() && user.getUserStatus().equals(UserStatusType.ACTIVE)) {
					merchantList = userDao.getActiveSubMerchants(user);
					for (Merchants merch : merchantList) {
						UserSettingData merchantSettings = userSettingDao.fetchDataUsingPayId(merch.getPayId());
						if (merchantSettings.isCustomerQrFlag()) {
							user.setCustomerQrFlag(true);
							break;
						}
					}
				}
				currencyMap = currencyMapProvider.currencyMap(user);
			}

			else if (user.getUserType().equals(UserType.RECONUSER)) {
				Merchants merchant = new Merchants();
				merchant.setMerchant(user);
				merchantList.add(merchant);

				currencyMap.put(CrmFieldConstants.INR.getValue(), "356");

			} else if (user.getUserType().equals(UserType.SUBUSER)) {
				User parentUser = userDao.findPayId(user.getParentPayId());

				if (parentUser.getUserType().equals(UserType.MERCHANT) && parentUser.isSuperMerchant()) {
					if (user.getSubUserType().equals("normalType")) {

						setSubMerchantList(userDao.getSubMerchantListBySuperPayId(parentUser.getPayId()));
						setSuperMerchant(true);
					} else {
						setSuperMerchant(false);
					}
				}

				Merchants merchant = new Merchants();
				merchant.setMerchant(parentUser);
				merchantList.add(merchant);
			}

			if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.RESELLER)) {
				currencyMap = currencyMapProvider.currencyMap(user);
				return CrmFieldConstants.ADMIN.getValue();
			} else if (user.getUserType().equals(UserType.SUPERADMIN)) {
				currencyMap = currencyMapProvider.currencyMap(user);
				return CrmFieldConstants.SUPERADMIN.getValue();
			} else if (user.getUserType().equals(UserType.MERCHANT)
					|| user.getUserType().equals(UserType.POSMERCHANT)) {
				if (user.getUserStatus().equals(UserStatusType.SUSPENDED)) {
					if (user.isMpaOnlineFlag() == false) {
						return CrmFieldConstants.OFFLINE_MPA.getValue();
					} else {
						return CrmFieldConstants.NEW_USER.getValue();
						// for online MPA
						// setOnlineMpaFlag(true);
						// return CrmFieldConstants.OFFLINE_MPA.getValue();
					}
				} else {
					return CrmFieldConstants.MERCHANT.getValue();
				}
			} else if (user.getUserType().equals(UserType.PARENTMERCHANT)) {
				
				return CrmFieldConstants.PARENTMERCHANT.getValue();
				
			}else if (user.getUserType().equals(UserType.SUBUSER)) {
				currencyMap = currencyMapProvider.currencyMap(user);

				if (getReportPermission(user)) {
					setReportPermission(true);
				}
				// for skiping dashboard for vendortype SubUser
				if (user.getSubUserType().equalsIgnoreCase("vendorType")) {
					return "vendorTypeSubUser";
				}
				// if(user.getSubUserType() != null &&
				// user.getSubUserType().equalsIgnoreCase("vendorType")) {
				// return "vendorType";
				// }else if(user.getSubUserType() != null &&
				// user.getSubUserType().equalsIgnoreCase("ePosType")) {
				// return "ePosType";
				// }else {
				return CrmFieldConstants.SUBUSER.getValue();
				// }
			} else if (user.getUserType().equals(UserType.ACQUIRER)) {
				currencyMap = currencyMapProvider.currencyMap(user);
				return CrmFieldConstants.ACQUIRER.getValue();
			} else if (user.getUserType().equals(UserType.SUBACQUIRER)) {
				currencyMap = currencyMapProvider.currencyMap(user);
				return CrmFieldConstants.ACQUIRER_SUBUSER.getValue();
			} else if (user.getUserType().equals(UserType.SUBADMIN)) {
				currencyMap = currencyMapProvider.currencyMap(user);
				return CrmFieldConstants.SUBADMIN.getValue();
			} else if (user.getUserType().equals(UserType.AGENT)) {
				currencyMap = currencyMapProvider.currencyMap(user);
				return CrmFieldConstants.AGENT.getValue();
			} else if (user.getUserType().equals(UserType.RECONUSER)) {
				currencyMap.put(CrmFieldConstants.INR.getValue(), "356");
				return CrmFieldConstants.RECONUSER.getValue();
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return LOGIN; // unmapped user
	}

	public boolean getReportPermission(User subUser) {
		boolean permissonAllowed = false;
		Set<Roles> roles = subUser.getRoles();
		Iterator<Roles> itr = roles.iterator();
		Roles role = new Roles();
		if (!roles.isEmpty()) {
			role = itr.next();
			Iterator<Permissions> permissionIterator = role.getPermissions().iterator();
			while (permissionIterator.hasNext()) {
				PermissionType permissionType = permissionIterator.next().getPermissionType();
				if (permissionType.getPermission().equalsIgnoreCase("View Transaction Reports")) {
					permissonAllowed = true;
				}
			}
		}

		return permissonAllowed;
	}

	public void validate() {

		if ((validator.validateBlankField(getReselleId()))) {
			/*
			 * addFieldError(CrmFieldType.RESELLER_ID.getName(), validator
			 * .getResonseObject().getResponseMessage());
			 */
		} else if (!(validator.validateField(CrmFieldType.RESELLER_ID, getReselleId()))) {
			addFieldError(CrmFieldType.RESELLER_ID.getName(), validator.getResonseObject().getResponseMessage());
		}
	}

	public Map<String, String> getCurrencyMap() {
		return currencyMap;
	}

	public void setCurrencyMap(Map<String, String> currencyMap) {
		this.currencyMap = currencyMap;
	}

	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getPermissionString() {
		return permissionString;
	}

	public void setPermissionString(String permissionString) {
		this.permissionString = permissionString;
	}

	public String getReselleId() {
		return reselleId;
	}

	public void setReselleId(String reselleId) {
		this.reselleId = reselleId;
	}

	public List<Merchants> getSubMerchantList() {
		return subMerchantList;
	}

	public boolean isSuperMerchant() {
		return superMerchant;
	}

	public void setSubMerchantList(List<Merchants> subMerchantList) {
		this.subMerchantList = subMerchantList;
	}

	public void setSuperMerchant(boolean superMerchant) {
		this.superMerchant = superMerchant;
	}

	public boolean isReportPermission() {
		return reportPermission;
	}

	public void setReportPermission(boolean reportPermission) {
		this.reportPermission = reportPermission;
	}

	public boolean isQrReportFlag() {
		return qrReportFlag;
	}

	public void setQrReportFlag(boolean qrReportFlag) {
		this.qrReportFlag = qrReportFlag;
	}

	public boolean isOnlineMpaFlag() {
		return onlineMpaFlag;
	}

	public void setOnlineMpaFlag(boolean onlineMpaFlag) {
		this.onlineMpaFlag = onlineMpaFlag;
	}

}
