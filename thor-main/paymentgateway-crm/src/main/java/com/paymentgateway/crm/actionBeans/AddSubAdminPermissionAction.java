package com.paymentgateway.crm.actionBeans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.struts2.dispatcher.SessionMap;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.util.Constants;

/**
 * @author Rahul
 *
 */
@Service
public class AddSubAdminPermissionAction {

	public List<PermissionType> getSubAdminPermissionType(SessionMap<String, Object> sessionMap) {
			StringBuilder permissions = new StringBuilder();
			permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));

			String permision = permissions.toString();

			List<String> list = new ArrayList<String>(Arrays.asList(permision.split("-")));
			List<PermissionType> permissionTypeList = new ArrayList<PermissionType>();
			for (String permissionName : list) {
				PermissionType pType = PermissionType.getInstanceFromName(permissionName);
				permissionTypeList.add(pType);
			}

			return permissionTypeList;
	}
}
