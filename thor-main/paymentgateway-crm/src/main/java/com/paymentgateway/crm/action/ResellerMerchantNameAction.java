package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.StatusType;

/**
 * @author Rajit
 */
public class ResellerMerchantNameAction extends AbstractSecureAction {

	@Autowired
	private UserDao userDao;
	
	private static Logger logger = LoggerFactory.getLogger(ResellerMerchantNameAction.class.getName());
	private static final long serialVersionUID = 4756240870798626882L;
	
	private List<Merchants> merchantList = new ArrayList<Merchants>();
	private User sessionUser = null;
	private List<StatusType> lst;

	public String execute() {
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			//setMerchantList(userDao.getMerchantListByResellerId(sessionUser.getResellerId()));
			
			setLst(StatusType.getStatusType());
		}  catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return INPUT;
	}

	public List<StatusType> getLst() {
		return lst;
	}

	public void setLst(List<StatusType> lst) {
		this.lst = lst;
	}
	
	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}
}
