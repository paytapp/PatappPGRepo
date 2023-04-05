package com.paymentgateway.crm.mpa;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.MerchantDetails;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncoder;
import com.paymentgateway.crm.action.AbstractSecureAction;

public class MerchanCreatedBYSubAdminAction extends AbstractSecureAction{

	private static final long serialVersionUID = -6294026979332599580L;
	private static Logger logger = LoggerFactory.getLogger(MerchanCreatedBYSubAdminAction.class.getName());
	
	@Autowired
	private DataEncoder encoder;
	
	@Autowired
	private UserDao userDao;
	
	private List<MerchantDetails> aaData;
	private User sessionUser = new User();
	private String payId;
	private String stage;

	public String execute() {
		logger.info("Inside execute(), MerchanCreatedBYSubAdminAction");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		
		if(sessionUser.getUserType().equals(UserType.ADMIN)) {
			setAaData(encoder.encodeMPAMerchantDetails(userDao.fetchMerchantsCreatedBySubAdmin("")));
		}else if(sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			setAaData(encoder.encodeMPAMerchantDetails(userDao.fetchMerchantsCreatedBySubAdmin(sessionUser.getPayId())));
		}
		return SUCCESS;
	}
	
	public String redirectToMPAForm() {
		setPayId(payId);
		return SUCCESS;
	}

	
	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public List<MerchantDetails> getAaData() {
		return aaData;
	}

	public void setAaData(List<MerchantDetails> aaData) {
		this.aaData = aaData;
	}
	
}
