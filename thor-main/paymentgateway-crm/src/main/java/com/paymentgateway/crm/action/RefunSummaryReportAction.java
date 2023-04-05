package com.paymentgateway.crm.action;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.action.AbstractSecureAction;
import com.paymentgateway.commons.user.RefundSummary;
import com.paymentgateway.commons.user.RefundValidationDetails;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.crm.mongoReports.RefundSummaryReportService;

/**
 * @author Vijaya
 *
 */

public class RefunSummaryReportAction extends AbstractSecureAction {

	private static final long serialVersionUID = -4269892349842953338L;
	
	private static Logger logger = LoggerFactory.getLogger(RefunSummaryReportAction.class.getName());
	
	private String refundRequestDate;
	private String merchant;
	private String subMerchantEmailId;
	private String mode;
	private String acquirer;
	private String paymentType;
	private String currency;
	private String transactionFlag;
	private List<RefundSummary> aaData;
	
	@Autowired
	RefundSummaryReportService refundSummaryReportService;
	
	@Autowired
	UserDao userDao;
	
	public String execute() {
		logger.info("Inside RefunSummaryReportAction Class, In execute method !!");
		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		String merchantPayId = "";
		String subMerchantPayId = "";
		if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
		
			merchantPayId = sessionUser.getSuperMerchantId();
			subMerchantPayId = sessionUser.getPayId();
		
		} else if(!merchant.equalsIgnoreCase("All")) {
			
			User merchantt = userDao.findPayIdByEmail(merchant);
			merchantPayId = merchantt.getPayId();

			if (StringUtils.isNotBlank(subMerchantEmailId) && !subMerchantEmailId.equalsIgnoreCase("All")) {
				subMerchantPayId = userDao.getPayIdByEmailId(subMerchantEmailId);
			} else {
				subMerchantPayId = subMerchantEmailId;
			}
		} else {
			merchantPayId = merchant;
		}
		setRefundRequestDate(DateCreater.toDateTimeformatCreater(refundRequestDate));
		setAaData(refundSummaryReportService.getData(merchantPayId,subMerchantPayId, refundRequestDate, acquirer, paymentType, mode, currency));
		return SUCCESS;
	}

	public String getRefundRequestDate() {
		return refundRequestDate;
	}

	public void setRefundRequestDate(String refundRequestDate) {
		this.refundRequestDate = refundRequestDate;
	}

	public String getMerchant() {
		return merchant;
	}

	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public List<RefundSummary> getAaData() {
		return aaData;
	}

	public void setAaData(List<RefundSummary> aaData) {
		this.aaData = aaData;
	}
	public String getSubMerchantEmailId() {
		return subMerchantEmailId;
	}

	public void setSubMerchantEmailId(String subMerchantEmailId) {
		this.subMerchantEmailId = subMerchantEmailId;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getTransactionFlag() {
		return transactionFlag;
	}

	public void setTransactionFlag(String transactionFlag) {
		this.transactionFlag = transactionFlag;
	}

}
