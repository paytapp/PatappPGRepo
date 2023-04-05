package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.Permissions;
import com.paymentgateway.commons.user.Roles;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.BusinessType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Puneet
 *
 */
public class MerchantListRedirectAction extends AbstractSecureAction {
	private static Logger logger = LoggerFactory.getLogger(MerchantListRedirectAction.class.getName());
	private static final long serialVersionUID = -2541609533197706532L;
	private static final String subAdminPrefix = "SUBADMIN_";
	private static final String SubAdminDefaultPermissions = "loginHistoryAction,adminProfile,setDefaultCurrency,uploadFile,getSubMerchantListByPayId";
	
	@Autowired
	private UserDao userDao;
	
	private List<Merchants> merchantList = new ArrayList<Merchants>();
	private User sessionUser = new User();
	private boolean editPermission;
	private boolean viewPermission;
	private Map<String, String> industryTypes = new TreeMap<String, String>();

	public String execute() {
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			if(sessionUser.getUserType().equals(UserType.ADMIN)){
				setEditPermission(true);
				setViewPermission(true);
				setMerchantList(userDao.getSuperMerchantList());
			}else if(sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				
				setViewPermission(true);
				setEditPermission(true);
				setMerchantList(userDao.getSuperMerchantList());
				//getPermission(sessionUser);
			}else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {

				User parentUser = userDao.findPayId(sessionUser.getParentPayId());
				if (parentUser != null && parentUser.getUserType().equals(UserType.MERCHANT)
						&& StringUtils.isNotBlank(parentUser.getSuperMerchantId()) && parentUser.isSuperMerchant()) {

					Merchants merchant = new Merchants();
					merchant.setEmailId(parentUser.getEmailId());
					merchant.setBusinessName(parentUser.getBusinessName());
					merchant.setPayId(parentUser.getPayId());
					merchant.setSuperMerchantId(parentUser.getSuperMerchantId());
					merchantList.add(merchant);

				}
			}else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				String resellerId=sessionUser.getResellerId();
				setMerchantList(userDao.getResellerSuperMerchantList(resellerId));	
			}
			Map<String,String>	industryCategoryLinkedMap = BusinessType.getIndustryCategoryList();
			industryTypes.putAll(industryCategoryLinkedMap);
			return INPUT;
		} catch (Exception exception) {
			logger.error("Exception ", exception);
			return ERROR;
		}
	}

	public void getPermission(User user) {
		
		PropertiesManager propertiesManager = new PropertiesManager();
		Map<String, String> permissionsMap = new HashMap<>();
		permissionsMap = propertiesManager.getAllProperties("subAdminPermission.properties");

		Set<Roles> roles = user.getRoles();
		Set<Permissions> permissions = roles.iterator().next().getPermissions();

		List<String> permissionsList = new ArrayList<>();

		if (!permissions.isEmpty()) {
			StringBuilder perms = new StringBuilder();
			Iterator<Permissions> itr = permissions.iterator();
			while (itr.hasNext()) {
				PermissionType permissionType = itr.next().getPermissionType();
				permissionsList.add(subAdminPrefix + permissionType.toString());
			}
		}

		StringBuilder permissionsStrings = new StringBuilder();

		for (String val : permissionsList) {
			if (null != permissionsMap.get(val)) {
				permissionsStrings.append(permissionsMap.get(val));
				permissionsStrings.append(",");
			}
		}
		permissionsStrings.append(SubAdminDefaultPermissions);

		String[] permissionStringArray = permissionsStrings.toString().split(",");

		for (String val : permissionStringArray) {
			if(val.equalsIgnoreCase("View Merchant Details")) {
				setViewPermission(true);
			}
			if(val.equalsIgnoreCase("Edit Merchant Details")) {
				setEditPermission(true);
			}
			
		}
		
		/*
		 * Set<Roles> roles = user.getRoles(); Set<Permissions> permissions =
		 * roles.iterator().next().getPermissions(); if (!permissions.isEmpty()) {
		 * StringBuilder perms = new StringBuilder(); Iterator<Permissions> itr =
		 * permissions.iterator(); while (itr.hasNext()) { PermissionType permissionType
		 * = itr.next().getPermissionType(); String permission =
		 * permissionType.getPermission();
		 * if(permission.equalsIgnoreCase("View Merchant Details")) {
		 * setViewPermission(true); }
		 * 
		 * if(permission.equalsIgnoreCase("Edit Merchant Details")) {
		 * setEditPermission(true); } } }
		 */
	}
	
	public boolean isEditPermission() {
		return editPermission;
	}

	public void setEditPermission(boolean editPermission) {
		this.editPermission = editPermission;
	}

	public boolean isViewPermission() {
		return viewPermission;
	}

	public void setViewPermission(boolean viewPermission) {
		this.viewPermission = viewPermission;
	}

	public Map<String, String> getIndustryTypes() {
		return industryTypes;
	}

	public void setIndustryTypes(Map<String, String> industryTypes) {
		this.industryTypes = industryTypes;
	}

	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}
	

}
