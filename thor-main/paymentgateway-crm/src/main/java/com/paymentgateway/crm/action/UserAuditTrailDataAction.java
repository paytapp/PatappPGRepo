package com.paymentgateway.crm.action;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.UserAuditDataService;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserAudit;
import com.paymentgateway.commons.user.UserAuditDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncoder;

public class UserAuditTrailDataAction extends AbstractSecureAction {

	@Autowired
	private UserAuditDataService userAuditDataService;

	@Autowired
	private DataEncoder encoder;

	@Autowired
	private UserAuditDao userAuditDao;

	private static Logger logger = LoggerFactory.getLogger(UserAuditTrailDataAction.class.getName());
	private static final long serialVersionUID = -4589708040501647522L;
	private List<UserAudit> aaData;
	private User sessionUser = new User();
	private String businessType;
	private String merchantStatus;
	private String payId;
	private String id;
	private List<UserAudit> userDataByPayId;

	@Override
	public String execute() {
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		logger.info("Inside UserAuditTrailDataAction >>> ");
		try {
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				aaData = encoder.encodeUserAuditDetailsObj(
						userAuditDataService.getAllUserAuditList(getBusinessType(), getMerchantStatus()));
			}
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception in UserAuditTrailDataAction >>> ", exception);
			return ERROR;
		}
	}

	public String fetchUserAuditDataById() {
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		logger.info("Inside UserAuditTrailDataAction, fetchUserAuditDataById()");
		try {
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				setUserDataByPayId(userAuditDao.getUserAuditDataById(getId()));
			}
		} catch (Exception e) {
			logger.error("Exception caught >>> ", e);
			return ERROR;
		}
		return SUCCESS;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getMerchantStatus() {
		return merchantStatus;
	}

	public void setMerchantStatus(String merchantStatus) {
		this.merchantStatus = merchantStatus;
	}

	public List<UserAudit> getaaData() {
		return aaData;
	}

	public void setaaData(List<UserAudit> setaaData) {
		this.aaData = setaaData;
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public List<UserAudit> getUserDataByPayId() {
		return userDataByPayId;
	}

	public void setUserDataByPayId(List<UserAudit> userDataByPayId) {
		this.userDataByPayId = userDataByPayId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}