package com.paymentgateway.crm.action;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.crm.actionBeans.NodalPayoutUpdateService;

public class NodalPayoutsUpdateAction extends AbstractSecureAction {

	private static final long serialVersionUID = -6998673672249328456L;
	private String merchant;
	private String acquirer;
	private String settlementDate;
	private String nodalSettlementDate;
	private String response;
	private String fromDate;
	private String toDate;
	private String paymentMethod;
	private String nodalType;
	private List<Merchants> merchantList = new ArrayList<Merchants>();
	private User sessionUser = new User();

	private static Logger logger = LoggerFactory.getLogger(NodalPayoutsUpdateAction.class.getName());
	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

	@Autowired
	private NodalPayoutUpdateService nodalPayoutUpdateService;
	
	@Autowired
	private UserDao userDao;

	public String execute() {
		logger.info("Inside NodalPayoutsUpdateAction , execute");
		
		String payId = userDao.getPayIdByEmailId(merchant);
		fromDate = settlementDate + " 00:00:00";
		toDate = settlementDate + " 23:59:59";
		setMerchantList(userDao.getMerchantActiveList());
		fromDate = DateCreater.toDateTimeformatCreater(settlementDate);
		toDate =   DateCreater.formDateTimeformatCreater(settlementDate);
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		/*setResponse(nodalPayoutUpdateService.updateNodalTransactions(payId, acquirer, 
				nodalSettlementDate, nodalType,paymentMethod, fromDate, toDate, sessionUser));*/
          addActionMessage(nodalPayoutUpdateService.updateNodalTransactions(payId, acquirer, 
  				nodalSettlementDate, nodalType,paymentMethod, fromDate, toDate, sessionUser));
		return SUCCESS;
	}

	public String getMerchant() {
		return merchant;
	}

	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public String getSettlementDate() {
		return settlementDate;
	}

	public void setSettlementDate(String settlementDate) {
		this.settlementDate = settlementDate;
	}

	public String getNodalSettlementDate() {
		return nodalSettlementDate;
	}

	public void setNodalSettlementDate(String nodalSettlementDate) {
		this.nodalSettlementDate = nodalSettlementDate;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getNodalType() {
		return nodalType;
	}

	public void setNodalType(String nodalType) {
		this.nodalType = nodalType;
	}

	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}


}
