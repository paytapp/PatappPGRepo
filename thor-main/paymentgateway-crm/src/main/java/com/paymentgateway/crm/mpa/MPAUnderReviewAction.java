package com.paymentgateway.crm.mpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.crm.action.AbstractSecureAction;

/**
 * @author Amitosh Aanand
 *
 */
public class MPAUnderReviewAction extends AbstractSecureAction {

	@Autowired
	MPAServicesFactory serviceFactory;

	private Object mpaData;
	private String payId;
	private User user = new User();
	private static final long serialVersionUID = -5436866617469352168L;
	private static Logger logger = LoggerFactory.getLogger(MPAUnderReviewAction.class.getName());

	public String execute() {
		try {
			setMpaData(serviceFactory.fetchAllPendingMPA());
			return SUCCESS;
		} catch (Exception e) {
			logger.error("Exception caught while fetching MPAs which are under review, " , e);
			return ERROR;
		}
	}

	public String apporovedInReview() {
		try {
			user = (User) sessionMap.get(Constants.USER);
			setMpaData(serviceFactory.approveMPAByPayId(payId, user));
			return SUCCESS;
		} catch (Exception e) {
			logger.error("Exception caught while fetching MPAs which are under review, " , e);
			return ERROR;
		}
	}

	public String rejectedInReview() {
		try {
			user = (User) sessionMap.get(Constants.USER);
			setMpaData(serviceFactory.rejectMPAbyPayId(payId, user));
			return SUCCESS;
		} catch (Exception e) {
			logger.error("Exception caught while fetching MPAs which are under review, " , e);
			return ERROR;
		}
	}

	public Object getMpaData() {
		return mpaData;
	}

	public void setMpaData(Object mpaData) {
		this.mpaData = mpaData;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}
}
