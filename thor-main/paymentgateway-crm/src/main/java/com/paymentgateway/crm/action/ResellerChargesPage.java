/**
 * 
 */
package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.UserDao;

/**
 * @author Amitosh Aanand
 *
 */
public class ResellerChargesPage extends AbstractSecureAction {

	private static final long serialVersionUID = 5495474881748260781L;

	public List<Merchants> listMerchant = new ArrayList<Merchants>();
	public List<Merchants> listReseller = new ArrayList<Merchants>();

	private boolean showMerchant;
	private boolean showSaveButton;

	@Autowired
	private UserDao userDao;

	@SuppressWarnings("unchecked")
	public String execute() {
		setListMerchant(userDao.getMerchantList());
		setListReseller(userDao.getResellerList());
		setShowMerchant(false);
		setShowSaveButton(false);
		return SUCCESS;
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

	public List<Merchants> getListReseller() {
		return listReseller;
	}

	public void setListReseller(List<Merchants> listReseller) {
		this.listReseller = listReseller;
	}
}