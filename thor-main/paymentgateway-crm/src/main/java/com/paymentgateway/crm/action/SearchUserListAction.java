package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.UserStatusType;

public class SearchUserListAction extends AbstractSecureAction {

	@Autowired
	private UserDao userDao;

	private static final long serialVersionUID = -3220279696930666685L;
	private static Logger logger = LoggerFactory.getLogger(SearchUserListAction.class.getName());
	private List<Merchants> aaData = new ArrayList<Merchants>();
	private User sessionUser = new User();
	private String status = "";
	private String merchantEmailId;
	private String subMerchantEmailId;

	@Override
	public String execute() {
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<Merchants> subUserData = new ArrayList<Merchants>();
		UserStatusType userStatus = null;

		if (status.equalsIgnoreCase("All")) {
			userStatus = null;
		} else if (status.equalsIgnoreCase("ACTIVE")) {
			userStatus = UserStatusType.ACTIVE;
		} else if (status.equalsIgnoreCase("PENDING")) {
			userStatus = UserStatusType.PENDING;
		} else if (status.equalsIgnoreCase("TRANSACTION_BLOCKED")) {
			userStatus = UserStatusType.TRANSACTION_BLOCKED;
		} else if (status.equalsIgnoreCase("SUSPENDED")) {
			userStatus = UserStatusType.SUSPENDED;
		} else if (status.equalsIgnoreCase("TERMINATED")) {
			userStatus = UserStatusType.TERMINATED;
		}
		try {
			String merchantPayId = "";
			String subMerchantPayId = "";

			if (StringUtils.isBlank(subMerchantEmailId) && StringUtils.isBlank(merchantEmailId)) {
                merchantEmailId = sessionUser.getEmailId();

                if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
                    subMerchantEmailId = userDao.getEmailIdByPayId(sessionUser.getPayId());
                }
            }
            if (sessionUser.getUserType().equals(UserType.MERCHANT) && sessionUser.isSuperMerchant()) {
                merchantEmailId = sessionUser.getEmailId();
            }

			if (StringUtils.isNotBlank(subMerchantEmailId)) {
				if (subMerchantEmailId.equalsIgnoreCase("ALL")) {
					merchantPayId = userDao.getPayIdByEmailId(merchantEmailId);
				} else {
					subMerchantPayId = userDao.getPayIdByEmailId(subMerchantEmailId);
					merchantPayId = subMerchantPayId;
				}
			} else {
				if (merchantEmailId.equalsIgnoreCase("ALL")) {
					merchantPayId = merchantEmailId;
				} else {
					merchantPayId = userDao.getPayIdByEmailId(merchantEmailId);
				}
			}

			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				if (merchantPayId.equalsIgnoreCase("ALL")) {
					List<Merchants> data = userDao.getAllSubUserListByAdminUsers(userStatus);

					for (Merchants subUserList : data) {
						Merchants merchant = new Merchants();
						merchant.setPayId(subUserList.getPayId());
						merchant.setEmailId(subUserList.getEmailId());
						merchant.setFirstName(subUserList.getFirstName());
						merchant.setLastName(subUserList.getLastName());
						if (StringUtils.isNotBlank(subUserList.getParentPayId())) {
							merchant.setBusinessName(userDao.getBusinessNameByPayId(subUserList.getParentPayId()));
						} else {
							merchant.setBusinessName("NA");
						}
						merchant.setMobile(subUserList.getMobile());
						if (subUserList.getRegistrationDate() == null) {
							merchant.setRegistrationDate("NA");
						} else {
							merchant.setRegistrationDate(String.valueOf(subUserList.getRegistrationDate()));
						}
						merchant.setIsActive(subUserList.getIsActive());
						merchant.setSubUserType(subUserList.getSubUserType());
						subUserData.add(merchant);
					}

					setAaData(subUserData);
				} else {
					List<Merchants> data = userDao.getSubUsers(merchantPayId, userStatus);
					for (Merchants subUserList : data) {
						Merchants merchant = new Merchants();
						merchant.setPayId(subUserList.getPayId());
						merchant.setEmailId(subUserList.getEmailId());
						merchant.setFirstName(subUserList.getFirstName());
						merchant.setLastName(subUserList.getLastName());
						merchant.setBusinessName(userDao.getBusinessNameByPayId(merchantPayId));
						merchant.setMobile(subUserList.getMobile());
						if (subUserList.getRegistrationDate() == null) {
							merchant.setRegistrationDate("NA");
						} else {
							merchant.setRegistrationDate(String.valueOf(subUserList.getRegistrationDate()));
						}
						merchant.setIsActive(subUserList.getIsActive());
						merchant.setSubUserType(subUserList.getSubUserType());
						subUserData.add(merchant);
					}
					setAaData(subUserData);
				}
				return SUCCESS;
			}
			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				
				List<Merchants> data = userDao.getSubUsers(sessionUser.getPayId(), userStatus);
				for (Merchants subUserList : data) {
					Merchants merchant = new Merchants();
					merchant.setPayId(subUserList.getPayId());
					merchant.setEmailId(subUserList.getEmailId());
					merchant.setFirstName(subUserList.getFirstName());
					merchant.setLastName(subUserList.getLastName());
					merchant.setBusinessName(userDao.getBusinessNameByPayId(merchantPayId));
					merchant.setMobile(subUserList.getMobile());
					if (subUserList.getRegistrationDate() == null) {
						merchant.setRegistrationDate("NA");
					} else {
						merchant.setRegistrationDate(String.valueOf(subUserList.getRegistrationDate()));
					}
					merchant.setIsActive(subUserList.getIsActive());
					merchant.setSubUserType(subUserList.getSubUserType());
					subUserData.add(merchant);
				}
				setAaData(subUserData);
			}
		} catch (Exception e) {
			logger.error("Exception", e);
			return ERROR;
		}
		return SUCCESS;
	}

	public List<Merchants> getAaData() {
		return aaData;
	}

	public void setAaData(List<Merchants> aaData) {
		this.aaData = aaData;
	}

	public String getMerchantEmailId() {
		return merchantEmailId;
	}

	public void setMerchantEmailId(String merchantEmailId) {
		this.merchantEmailId = merchantEmailId;
	}

	public String getSubMerchantEmailId() {
		return subMerchantEmailId;
	}

	public void setSubMerchantEmailId(String subMerchantEmailId) {
		this.subMerchantEmailId = subMerchantEmailId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
