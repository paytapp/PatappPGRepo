package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.BusinessType;
import com.paymentgateway.commons.util.Constants;

/**
 * @author Puneet
 *
 */
public class DisplaySignupPage extends AbstractSecureAction {

	
	@Autowired
	private UserDao userDao;
	
	private static final long serialVersionUID = 3472447203584744945L;
	private Map<String, String> industryCategoryList = new TreeMap<String, String>();
	private List<Merchants> superMerchantList = new ArrayList<Merchants>();
	
	public String execute() {
		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		Map<String, String> industryCategoryLinkedMap = BusinessType.getIndustryCategoryList();
		industryCategoryList.putAll(industryCategoryLinkedMap);
		
		if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			setSuperMerchantList(userDao.getSuperMerchantList());
			return INPUT;
		}
		
		else if (sessionUser.getUserType().equals(UserType.RESELLER) ) {
			setSuperMerchantList(userDao.getResellerSuperMerchantList(sessionUser.getResellerId()));
			return "reseller";
		}
		else if (sessionUser.getUserType().equals(UserType.MERCHANT) ) {
			
			if (sessionUser.isSuperMerchant()) {
				Merchants merchant = new Merchants();
				merchant.setEmailId(sessionUser.getEmailId());
				merchant.setBusinessName(sessionUser.getBusinessName());
				merchant.setPayId(sessionUser.getPayId());
				merchant.setSuperMerchantId(sessionUser.getSuperMerchantId());
				superMerchantList.add(merchant);
				return "superMerchant";
			}
			else {
				return INPUT;
			}
			
		}
		else {
			setSuperMerchantList(userDao.getSuperMerchantList());
			return INPUT;
		}
		
	}

	public Map<String, String> getIndustryCategoryList() {
		return industryCategoryList;
	}

	public void setIndustryCategoryList(Map<String, String> industryCategoryList) {
		this.industryCategoryList = industryCategoryList;
	}

	public List<Merchants> getSuperMerchantList() {
		return superMerchantList;
	}

	public void setSuperMerchantList(List<Merchants> superMerchantList) {
		this.superMerchantList = superMerchantList;
	}

}
