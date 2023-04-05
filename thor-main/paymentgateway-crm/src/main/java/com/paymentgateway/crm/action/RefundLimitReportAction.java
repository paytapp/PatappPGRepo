package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.RefundLimitService;
import com.paymentgateway.commons.user.RefundLimitObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;

/**
 * @Mahboob Alam
 *
 */

public class RefundLimitReportAction extends AbstractSecureAction{
	
	@Autowired
	private RefundLimitService refundLimitService;
	
	private String payId;
	private String subMerchantId;
	private float extraRefundLimit;
	private float oneTimeRefundLimit;
	private float refundLimitRemains;
	private boolean limitChangedFlag;
	private boolean extraRefundAmount;
	private boolean oneTimeRefundAmount;
	private String dateFrom;
	private String dateTo;
	private String remarks;
	private List<RefundLimitObject> aaData = new ArrayList<RefundLimitObject>();
	private RefundLimitObject refundLimit;
	
	private static Logger logger = LoggerFactory.getLogger(RefundLimitReportAction.class.getName());
	private static final long serialVersionUID = -8230514513827344844L;
	
	private User sessionUser = new User();

	/**
	 * @author Mehboob Alam
	 *
	 */
	
	public String execute() {
		logger.info("Inside the execute : ");
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
			if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				setAaData(refundLimitService.fetchRefundLimitReport(payId, subMerchantId, dateTo, dateFrom, sessionUser));
			} else if(sessionUser.getUserType().equals(UserType.RESELLER)) {
				setAaData(refundLimitService.fetchRefundLimitReport(payId, subMerchantId, dateTo, dateFrom, sessionUser));
			}else if(sessionUser.getUserType().equals(UserType.MERCHANT)) {
				setAaData(refundLimitService.fetchRefundLimitReport(payId, subMerchantId, dateTo, dateFrom, sessionUser));
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}

		return SUCCESS;
	}
	
	public String assignRefundLimit() {
		logger.info("Inside the AssignRefundLimit : ");
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			
			if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				refundLimitService.createOrUpdateRefundLimit(payId, subMerchantId, oneTimeRefundLimit, refundLimitRemains, extraRefundLimit, sessionUser, oneTimeRefundAmount, extraRefundAmount, remarks);
			}
		}catch(Exception ex) {
			logger.error("Caught exception in AssignRefundLimit Refund Limit : ", ex);
			return ERROR;
		}
		
		return SUCCESS;
	}
	public String fetchRefundLimitBypayId() {
		logger.info("Inside the AssignRefundLimit : ");
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			
			if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				setRefundLimit(refundLimitService.getLimitByPayId(payId, subMerchantId));
			}
		}catch(Exception ex) {
			logger.error("Caught exception in AssignRefundLimit Refund Limit : ", ex);
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

	public String getSubMerchantId() {
		return subMerchantId;
	}

	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}

	public float getExtraRefundLimit() {
		return extraRefundLimit;
	}

	public void setExtraRefundLimit(float extraRefundLimit) {
		this.extraRefundLimit = extraRefundLimit;
	}

	public float getOneTimeRefundLimit() {
		return oneTimeRefundLimit;
	}

	public void setOneTimeRefundLimit(float oneTimeRefundLimit) {
		this.oneTimeRefundLimit = oneTimeRefundLimit;
	}

	public float getRefundLimitRemains() {
		return refundLimitRemains;
	}

	public void setRefundLimitRemains(float refundLimitRemains) {
		this.refundLimitRemains = refundLimitRemains;
	}

	public boolean isLimitChangedFlag() {
		return limitChangedFlag;
	}

	public void setLimitChangedFlag(boolean limitChangedFlag) {
		this.limitChangedFlag = limitChangedFlag;
	}

	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}

	public List<RefundLimitObject> getAaData() {
		return aaData;
	}

	public void setAaData(List<RefundLimitObject> aaData) {
		this.aaData = aaData;
	}

	public User getSessionUser() {
		return sessionUser;
	}

	public void setSessionUser(User sessionUser) {
		this.sessionUser = sessionUser;
	}

	public boolean isExtraRefundAmount() {
		return extraRefundAmount;
	}

	public void setExtraRefundAmount(boolean extraRefundAmount) {
		this.extraRefundAmount = extraRefundAmount;
	}

	public boolean isOneTimeRefundAmount() {
		return oneTimeRefundAmount;
	}

	public void setOneTimeRefundAmount(boolean oneTimeRefundAmount) {
		this.oneTimeRefundAmount = oneTimeRefundAmount;
	}

	public RefundLimitObject getRefundLimit() {
		return refundLimit;
	}

	public void setRefundLimit(RefundLimitObject refundLimit) {
		this.refundLimit = refundLimit;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
}
