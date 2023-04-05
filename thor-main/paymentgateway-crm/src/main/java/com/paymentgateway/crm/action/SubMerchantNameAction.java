package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

/**
 * @author Chandan
 *
 */
public class SubMerchantNameAction extends AbstractSecureAction {

	@Autowired
	private UserDao userDao;

	@Autowired
	private PropertiesManager propertiesManager;

	private static Logger logger = LoggerFactory.getLogger(SubMerchantNameAction.class.getName());
	private static final long serialVersionUID = -5990800125330748024L;

	private List<Merchants> merchantList = new ArrayList<Merchants>();
	private User sessionUser = null;
	private List<StatusType> lst;

	@SuppressWarnings("unchecked")
	public String execute() {
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				setMerchantList(userDao.getSuperMerchantList());
			}
			/*
			 * else if(sessionUser.getUserType().equals(UserType.RESELLER)) {
			 * setMerchantList(userDao.getResellerMerchantList(sessionUser.getResellerId()))
			 * ; }
			 */

			// For super merchant
			else if (sessionUser.getUserType().equals(UserType.MERCHANT)
					&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId()) && sessionUser.isSuperMerchant()) {

				Merchants merchant = new Merchants();
				merchant.setEmailId(sessionUser.getEmailId());
				merchant.setBusinessName(sessionUser.getBusinessName());
				merchant.setPayId(sessionUser.getPayId());
				merchant.setSuperMerchantId(sessionUser.getSuperMerchantId());
				merchantList.add(merchant);

			}

			else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {

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
			
			
			
			setLst(StatusType.getStatusType());
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}

		return INPUT;
	}

	

	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}

	public List<StatusType> getLst() {
		return lst;
	}

	public void setLst(List<StatusType> lst) {
		this.lst = lst;
	}

}
