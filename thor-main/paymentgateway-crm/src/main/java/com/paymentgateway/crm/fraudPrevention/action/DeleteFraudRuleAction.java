package com.paymentgateway.crm.fraudPrevention.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.FraudPrevention;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.crm.action.AbstractSecureAction;
import com.paymentgateway.pg.core.fraudPrevention.model.FraudPreventionDao;

/**
 * @author Harpreet,Rahul
 *
 */
public class DeleteFraudRuleAction extends AbstractSecureAction{
	
	@Autowired
	private FraudPreventionDao fraudPreventionDao;

	private static final long serialVersionUID = -2472986893677385590L;

	private static Logger logger = LoggerFactory.getLogger(DeleteFraudRuleAction.class.getName());
	private List<Merchants> merchantList = new ArrayList<Merchants>();	
	//request param for deleteFraudPreventionRules 
	private String payId;
	private Long ruleId;
	private String response;
	
	// to delete fraud rule from database
	public String execute(){
		try{
			if(!StringUtils.isBlank(ruleId.toString())){
				User sessionUser = (User)sessionMap.get(Constants.USER.getValue());
				FraudPrevention	rule = fraudPreventionDao.getFraudRuleListbyRuleId(ruleId);
				Date currentDate = new Date();
				
				rule.setStatus(TDRStatus.INACTIVE);
				rule.setProcessedBy(sessionUser.getEmailId());
				rule.setUpdateDate(currentDate);
				fraudPreventionDao.update(rule);
				setResponse("Fraud rule deleted successfully.");
				return SUCCESS;
			}else{
				setResponse("Try again, Something went wrong!");
				return ERROR;
			}
		}catch(Exception exception){
			logger.error("Fraud Prevention System - Exception : " , exception);
			setResponse("Try again, Something went wrong!");
			return ERROR;
		}
	}

	public void validate(){
		CrmValidator validator = new CrmValidator();
		if(!validator.validateBlankField(ruleId)){
			setResponse("Try again, Something went wrong!");			
		}
		if(!StringUtils.isBlank(payId)){
			if(!validator.validateBlankField(payId)){
				setResponse("Try again, Something went wrong!");			
			}
		}
	}
	
	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public Long getRuleId() {
		return ruleId;
	}

	public void setRuleId(Long ruleId) {
		this.ruleId = ruleId;
	}
}