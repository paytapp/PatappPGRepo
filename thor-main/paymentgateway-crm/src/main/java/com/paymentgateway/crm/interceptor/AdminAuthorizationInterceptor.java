package com.paymentgateway.crm.interceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.Permissions;
import com.paymentgateway.commons.user.Roles;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Puneet
 * 
 */
public class AdminAuthorizationInterceptor extends AbstractInterceptor {
	private static Logger logger = LoggerFactory.getLogger(AdminAuthorizationInterceptor.class.getName());
	private static final long serialVersionUID = 852707981164914179L;
	private static final String SubAdminDefaultPermissions = "loginHistoryAction,adminProfile,setDefaultCurrency,uploadFile,getSubMerchantListByPayId";
	private static final String SubUserDefaultPermissions = "loginHistoryAction,adminProfile,merchantProfile,passwordChange,viewChargebackDetailsAction,agentSearch,agentSearchAction,downloadPaymentsReport,downloadPaymentsReportAction,subUserProfile,getParentDetail,getPieChartSaleData,getPieChartRefundData,downloadTransaction,DownloadTransationByStatus,complaintRaiseAction,viewComplaintAction,complaintRaise";
	private static final String subuserPrefix = "SUBUSER_";
	private static final String subAdminPrefix = "SUBADMIN_";
	private PropertiesManager propertiesManager = new PropertiesManager();

	@Override
	public String intercept(ActionInvocation actionInvocation) {
		try {
			Map<String, Object> sessionMap = actionInvocation.getInvocationContext().getSession();
			Object userObject = sessionMap.get(Constants.USER.getValue());

			if (null == userObject) {
				return Action.LOGIN;
			}

			User user = (User) userObject;
			if (user.getUserType().equals(UserType.ADMIN)) {
				return actionInvocation.invoke();
			} else if (user.getUserType().equals(UserType.SUBADMIN)) {
				return actionInvocation.invoke();
				
				//Commented temporarily
				/*
				 * String actionName = actionInvocation.getProxy().getActionName();
				 * 
				 * PropertiesManager propertiesManager = new PropertiesManager(); Map<String,
				 * String> permissionsMap = new HashMap<>(); permissionsMap =
				 * propertiesManager.getAllProperties("subAdminPermission.properties");
				 * 
				 * Set<Roles> roles = user.getRoles(); Set<Permissions> permissions =
				 * roles.iterator().next().getPermissions();
				 * 
				 * List<String> permissionsList = new ArrayList<>();
				 * 
				 * if (!permissions.isEmpty()) { StringBuilder perms = new StringBuilder();
				 * Iterator<Permissions> itr = permissions.iterator(); while (itr.hasNext()) {
				 * PermissionType permissionType = itr.next().getPermissionType();
				 * permissionsList.add(subAdminPrefix + permissionType.toString()); } }
				 * 
				 * StringBuilder permissionsStrings = new StringBuilder();
				 * 
				 * for (String val : permissionsList) { if (null != permissionsMap.get(val)) {
				 * permissionsStrings.append(permissionsMap.get(val));
				 * permissionsStrings.append(","); } }
				 * permissionsStrings.append(SubAdminDefaultPermissions);
				 * 
				 * String[] permissionStringArray=permissionsStrings.toString().split(",");
				 * 
				 * for (String val : permissionStringArray) { if (val.contentEquals(actionName))
				 * { return actionInvocation.invoke(); } }
				 * 
				 * 
				 * 
				 * if (permissionsStrings.toString().contains(actionName)) { return
				 * actionInvocation.invoke(); }
				 * 
				 * return Action.LOGIN;
				 */

			}

			else if (user.getUserType().equals(UserType.SUBUSER)) {

				String actionName = actionInvocation.getProxy().getActionName();

				PropertiesManager propertiesManager = new PropertiesManager();
				Map<String, String> permissionsMap = new HashMap<>();
				permissionsMap = propertiesManager.getAllProperties("subUserPermission.properties");

				Set<Roles> roles = user.getRoles();
				Set<Permissions> permissions = roles.iterator().next().getPermissions();

				List<String> permissionsList = new ArrayList<>();

				if (!permissions.isEmpty()) {
					StringBuilder perms = new StringBuilder();
					Iterator<Permissions> itr = permissions.iterator();
					while (itr.hasNext()) {
						PermissionType permissionType = itr.next().getPermissionType();
						permissionsList.add(subuserPrefix + permissionType.toString());
					}
				}

				StringBuilder permissionsStrings = new StringBuilder();

				for (String val : permissionsList) {
					if (null != permissionsMap.get(val)) {
						permissionsStrings.append(permissionsMap.get(val));
						permissionsStrings.append(",");
					}
				}
				permissionsStrings.append(SubUserDefaultPermissions);

				String[] permissionStringArray = permissionsStrings.toString().split(",");

				for (String val : permissionStringArray) {
					if (val.contentEquals(actionName)) {
						return actionInvocation.invoke();
					}
				}

				/*
				 * if (permissionsStrings.toString().contains(actionName)) { return
				 * actionInvocation.invoke(); }
				 */

			}

			else if (user.getUserType().equals(UserType.MERCHANT)) {

				String actionName = actionInvocation.getProxy().getActionName();

				PropertiesManager propertiesManager = new PropertiesManager();
				Map<String, String> permissionsMap = new HashMap<>();
				permissionsMap = propertiesManager.getAllProperties("merchantPermission.properties");

				StringBuilder permissionsStrings = new StringBuilder();

				for (String value : permissionsMap.values()) {
					permissionsStrings.append(value);
					permissionsStrings.append(",");
				}

				String[] permissionStringArray = permissionsStrings.toString().split(",");

				for (String val : permissionStringArray) {
					if (val.contentEquals(actionName)) {
						return actionInvocation.invoke();
					}
				}
				/*
				 * if (permissionsStrings.toString().contains(actionName)) { return
				 * actionInvocation.invoke(); }
				 */
			}else if (user.getUserType().equals(UserType.PARENTMERCHANT)) {

				String actionName = actionInvocation.getProxy().getActionName();

				PropertiesManager propertiesManager = new PropertiesManager();
				Map<String, String> permissionsMap = new HashMap<>();
				permissionsMap = propertiesManager.getAllProperties("parentMerchantPermission.properties");

				StringBuilder permissionsStrings = new StringBuilder();

				for (String value : permissionsMap.values()) {
					permissionsStrings.append(value);
					permissionsStrings.append(",");
				}

				String[] permissionStringArray = permissionsStrings.toString().split(",");

				for (String val : permissionStringArray) {
					if (val.contentEquals(actionName)) {
						return actionInvocation.invoke();
					}
				}
			}

			else if (user.getUserType().equals(UserType.RESELLER)) {
				return actionInvocation.invoke();
			} else if (user.getUserType().equals(UserType.RECONUSER)) {
				return actionInvocation.invoke();
				/*
				 * String actionName = actionInvocation.getProxy().getActionName();
				 * 
				 * PropertiesManager propertiesManager = new PropertiesManager(); Map<String,
				 * String> permissionsMap = new HashMap<>(); permissionsMap =
				 * propertiesManager.getAllProperties("reconUserPermission.properties");
				 * 
				 * StringBuilder permissionsStrings = new StringBuilder();
				 * 
				 * for (String value : permissionsMap.values()) {
				 * permissionsStrings.append(value); permissionsStrings.append(","); }
				 * 
				 * String[] permissionStringArray=permissionsStrings.toString().split(",");
				 * 
				 * for (String val : permissionStringArray) { if (val.contentEquals(actionName))
				 * { return actionInvocation.invoke(); } }
				 */
			} else if (user.getUserType().equals(UserType.AGENT)) {
//				String actionName = actionInvocation.getProxy().getActionName();

//				PropertiesManager propertiesManager = new PropertiesManager();
//				Map<String, String> permissionsMap = new HashMap<>();
//				permissionsMap = propertiesManager.getAllProperties("agentPermission.properties");
//
//				StringBuilder permissionsStrings = new StringBuilder();
//
//				for (String value : permissionsMap.values()) {
//					permissionsStrings.append(value);
//				}
//
//				if (permissionsStrings.toString().contains(actionName)) {
//					return actionInvocation.invoke();
//				}
				return actionInvocation.invoke();
			}
			return Action.LOGIN;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return Action.ERROR;
		}
	}
}
