package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.actionBeans.AddSubAdminPermissionAction;

/**
 * @author Rahul
 *
 */
public class AddUserCallAction extends AbstractSecureAction {

	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private UserSettingDao userSettingDao;



	private static Logger logger = LoggerFactory.getLogger(AddUserCallAction.class.getName());
	private static final long serialVersionUID = 9033493264155370845L;
	private List<PermissionType> listPermissionType;
	private List<PermissionType> subUserPermissionType;
	private List<Merchants> merchantList = new ArrayList<Merchants>();
	private boolean showEpos;
	private boolean eNachReport;

	public String execute() {
		try {
			User sessionUser = null;
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			UserSettingData userSettings=(UserSettingData) sessionMap.get(Constants.USER_SETTINGS.getValue());
			
			setShowEpos(false);
			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				if (sessionUser.isSuperMerchant()) {
					if (userSettings.isEposMerchant()) {
						setShowEpos(true);
					}
					if(userSettings.iseNachReportFlag()) {
						seteNachReport(true);
					}
					setSubUserPermissionType(PermissionType.getSubUserAccessPrevilageType());
					setListPermissionType(PermissionType.getSubUserPermissionTypeForSuperMerchant());
				} else {
					/*
					 * if (StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) { if
					 * (userDao.findPayId(sessionUser.getSuperMerchantId()).isEposMerchant()) {
					 * setShowEpos(true); } } else { if
					 * (userDao.findPayId(sessionUser.getPayId()).isEposMerchant()) {
					 * setShowEpos(true); } }
					 */
					if(userSettings.iseNachReportFlag()) {
						seteNachReport(true);
					}
					setShowEpos(true);
					setSubUserPermissionType(PermissionType.getSubUserAccessPrevilageType());
					setListPermissionType(PermissionType.getSubUserPermissionType());
				}
				return "merchant";
			} else if (sessionUser.getUserType().equals(UserType.ACQUIRER)) {
				setListPermissionType(PermissionType.getSubAcquirerPermissionType());
				return "reseller";
			} else if (sessionUser.getUserType().equals(UserType.ADMIN)) {
				setListPermissionType(PermissionType.getPermissionType());
				return "subAdmin";
			} else if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				setListPermissionType(new AddSubAdminPermissionAction().getSubAdminPermissionType(sessionMap));
				return "subAdmin";
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		;
		return null;
	}

	
	public String addUserByAdmin() {
		try {
		User sessionUser = null;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		setShowEpos(false);
		if (sessionUser.getUserType().equals(UserType.ADMIN)) {
			setShowEpos(true);
			setSubUserPermissionType(PermissionType.getSubUserAccessPrevilageType());
			setListPermissionType(PermissionType.getSubUserPermissionType());
			setMerchantList(userDao.getAllStatusMerchantList());
			return "merchant";
		}
	  }catch(Exception e) {
		  logger.error("Exception", e);
			return ERROR; 
	  }
		return null;
		
	}

	public List<PermissionType> getListPermissionType() {
		return listPermissionType;
	}

	public void setListPermissionType(List<PermissionType> listPermissionType) {
		this.listPermissionType = listPermissionType;
	}

	public boolean isShowEpos() {
		return showEpos;
	}

	public void setShowEpos(boolean showEpos) {
		this.showEpos = showEpos;
	}

	public List<PermissionType> getSubUserPermissionType() {
		return subUserPermissionType;
	}

	public void setSubUserPermissionType(List<PermissionType> subUserPermissionType) {
		this.subUserPermissionType = subUserPermissionType;
	}
	
	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}
	public boolean iseNachReport() {
		return eNachReport;
	}

	public void seteNachReport(boolean eNachReport) {
		this.eNachReport = eNachReport;
	}
}
