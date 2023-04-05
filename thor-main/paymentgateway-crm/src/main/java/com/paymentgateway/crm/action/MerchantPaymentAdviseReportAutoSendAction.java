package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


import com.paymentgateway.commons.dao.SUFDetailDao;
import com.paymentgateway.commons.email.EmailServiceProvider;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.crm.mongoReports.TxnReports;

/**
 * @author Rajit
 */
public class MerchantPaymentAdviseReportAutoSendAction extends AbstractSecureAction {

	@Autowired
	private TxnReports txnReports;

	@Autowired
	private SUFDetailDao sufDetailDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private EmailServiceProvider emailServiceProvider;

	private static Logger logger = LoggerFactory.getLogger(MerchantPaymentAdviseReportAutoSendAction.class.getName());
	private static final long serialVersionUID = 7658901196135972529L;

	private String payoutDate;
	private String merchantPayId;
	private String subMerchantPayId;
	private String filename;
	private String currency;
	private User sessionUser = new User();
	private Set<String> orderIdSet = null;
	private String responseMsg;

	public String execute() {
		try {
			Map<String, String> responseMap = new HashMap<String, String>();

			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			responseMap = emailServiceProvider.sendPaymentAdviseEmail(subMerchantPayId, merchantPayId,
					sessionUser.getPayId(), payoutDate, currency);

			String respValue = responseMap.get("response");
			logger.info("response from email = " + respValue);
			if (StringUtils.isNoneBlank(respValue)) {
				if (respValue.equalsIgnoreCase("success")) {
					setResponseMsg("success");
					logger.info("responseMsg in if condition = " + responseMsg);
				} else {
					setResponseMsg("failed");
					logger.info("responseMsg in else condition = " + responseMsg);
					return ERROR;
				}
			} else {
				setResponseMsg("failed");
				logger.info("responseMsg in outer else = " + responseMsg);
				return ERROR;
			}
		} catch (Exception e) {
			logger.error("Exception ", e);
			return ERROR;
		}
		logger.info("responseMsg = " + responseMsg);
		return SUCCESS;

	}
	
	public Merchants getMerchant(User user) {

		Merchants merchant = new Merchants();

		merchant.setEmailId(user.getEmailId());
		merchant.setPayId(user.getPayId());
		merchant.setBusinessName(user.getBusinessName());
		merchant.setSuperMerchantId(user.getSuperMerchantId());
		merchant.setIsSuperMerchant(user.isSuperMerchant());

		return merchant;
	}

	public TxnReports getTxnReports() {
		return txnReports;
	}

	public void setTxnReports(TxnReports txnReports) {
		this.txnReports = txnReports;
	}

	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public User getSessionUser() {
		return sessionUser;
	}

	public void setSessionUser(User sessionUser) {
		this.sessionUser = sessionUser;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

	public Set<String> getOrderIdSet() {
		return orderIdSet;
	}

	public void setOrderIdSet(Set<String> orderIdSet) {
		this.orderIdSet = orderIdSet;
	}

	public String getResponseMsg() {
		return responseMsg;
	}

	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	public String getPayoutDate() {
		return payoutDate;
	}

	public void setPayoutDate(String payoutDate) {
		this.payoutDate = payoutDate;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

}