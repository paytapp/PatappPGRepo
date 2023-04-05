package com.paymentgateway.crm.action;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.CustomPageDao;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;
/**
 * @author Rajit
 */
public class DownloadCustomPageAction extends AbstractSecureAction {
	
	private static final long serialVersionUID = -6751283169211954757L;
	private static Logger logger = LoggerFactory.getLogger(DownloadCustomPageAction.class.getName());
	
	@Autowired
	private UserDao userDao;
	
	private String payId;
	
	public String execute() {
		
		String bussinessName = userDao.getBusinessNameByPayId(payId);
		File file = new File(PropertiesManager.propertiesMap.get(Constants.CUSTOM_PAGE_LOGO_LOCATION.getValue()) + "//" + bussinessName);
		String zip = PropertiesManager.propertiesMap.get(Constants.CUSTOM_PAGE_LOGO_LOCATION.getValue()) + "//" + bussinessName+".zip";
		
		return SUCCESS;
	}
	
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
}
