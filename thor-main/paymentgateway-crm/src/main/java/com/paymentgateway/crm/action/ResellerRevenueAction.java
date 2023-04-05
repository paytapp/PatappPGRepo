package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.action.AbstractSecureAction;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;

/**
 * @author Amitosh Aanand
 *
 */
public class ResellerRevenueAction extends AbstractSecureAction {

	@Autowired
	private UserDao userDao;

	public String resellerId;
	public List<Merchants> listMerchant = new ArrayList<Merchants>();
	public List<Merchants> listReseller = new ArrayList<Merchants>();
	private static final long serialVersionUID = 6136070413613356019L;

	@SuppressWarnings("unchecked")
	public String execute() {
		try {
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.SUPERADMIN)
					|| sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			setListMerchant(userDao.getMerchantList());
			setListReseller(userDao.getResellerList());
			} else if(sessionUser.getUserType().equals(UserType.RESELLER)) {
				setListMerchant(userDao.getActiveResellerMerchants(sessionUser.getResellerId()));
			}
		} catch (Exception e) {
			return ERROR;
		}
		return INPUT;
	}
	
	public String getMerchantByResellerPayId() {
		try {
			setListMerchant(userDao.getActiveResellerMerchants(resellerId));
		} catch (Exception ex) {
			return ERROR;
		}
		return SUCCESS;
	}
	
	public List<Merchants> getListMerchant() {
		return listMerchant;
	}
	public void setListMerchant(List<Merchants> listMerchant) {
		this.listMerchant = listMerchant;
	}
	public List<Merchants> getListReseller() {
		return listReseller;
	}
	public void setListReseller(List<Merchants> listReseller) {
		this.listReseller = listReseller;
	}
	public String getResellerId() {
		return resellerId;
	}
	public void setResellerId(String resellerId) {
		this.resellerId = resellerId;
	}
}
