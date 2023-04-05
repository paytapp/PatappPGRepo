package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.BusinessType;

/**
 * @author Shaiwal
 *
 */
public class DisplayMerchantDefaultRate extends AbstractSecureAction{

	private static final long serialVersionUID = -3054395368649186158L;
	private Map<String, String> industryCategoryList = new TreeMap<String, String>();
	public List<Merchants> listMerchant = new ArrayList<Merchants>();
	
	private boolean showMerchant;
	private boolean showSaveButton;
	
	@Autowired
	private UserDao userDao;
	
	public String execute(){
		Map<String,String>	industryCategoryLinkedMap = BusinessType.getIndustryCategoryList();
		 industryCategoryList.putAll(industryCategoryLinkedMap);
		 setListMerchant(userDao.getMerchantList());
			setShowMerchant(false);
			setShowSaveButton(false);
		return SUCCESS;
		
	}
	

	public Map<String, String> getIndustryCategoryList() {
		return industryCategoryList;
	}

	public void setIndustryCategoryList(Map<String, String> industryCategoryList) {
		this.industryCategoryList = industryCategoryList;
	}


	public List<Merchants> getListMerchant() {
		return listMerchant;
	}


	public void setListMerchant(List<Merchants> listMerchant) {
		this.listMerchant = listMerchant;
	}


	public boolean isShowMerchant() {
		return showMerchant;
	}


	public void setShowMerchant(boolean showMerchant) {
		this.showMerchant = showMerchant;
	}


	public boolean isShowSaveButton() {
		return showSaveButton;
	}


	public void setShowSaveButton(boolean showSaveButton) {
		this.showSaveButton = showSaveButton;
	}


	
	
}