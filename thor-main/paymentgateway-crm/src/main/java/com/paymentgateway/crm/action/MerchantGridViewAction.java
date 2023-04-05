package com.paymentgateway.crm.action;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.MerchantGridViewService;
import com.paymentgateway.commons.user.MerchantDetails;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DataEncoder;

public class MerchantGridViewAction extends AbstractSecureAction {
	
	@Autowired
	private MerchantGridViewService merchantGridViewService;
	
	@Autowired
	private DataEncoder encoder;
	@Autowired
	private CrmValidator validator;

	private static Logger logger = LoggerFactory.getLogger(MerchantGridViewAction.class.getName());
	private static final long serialVersionUID = 3293888841176590776L;
	private List<MerchantDetails> aaData;
	private User sessionUser = new User();
	private String businessType;
	private String merchantStatus;
	private String byWhom;
	
	@Override
	public String execute() {
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		logger.info("Inside MerchantGridViewAction()");
		try {	
			if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)){
					
				aaData = encoder.encodeMerchantDetailsObj(merchantGridViewService.getAllMerchantsFromMap(getBusinessType(),getMerchantStatus(),getByWhom()));
			
			}
//			else if(sessionUser.getUserType().equals(UserType.SUBADMIN)) {
//					aaData = encoder.encodeMerchantDetailsObj(merchantGridViewService.getAllMerchantsByType(getBusinessType(),getMerchantStatus(),getByWhom()));
//			}
			
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}
	public void validate() {
	if ((validator.validateBlankField(getBusinessType()))) {
	
	} else if (!(validator.validateField(CrmFieldType.BUSINESS_TYPE,
			getBusinessType()))) {
		addFieldError(CrmFieldType.BUSINESS_TYPE.getName(), validator
				.getResonseObject().getResponseMessage());
	}
	}
//Show all resellerList in Dashbord
	public String listOfReseller(){
		try {			
			aaData = encoder.encodeMerchantDetailsObj(merchantGridViewService.getAllReseller());
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}
	
	 public String resellerList(){
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		 try {			
				aaData = encoder.encodeMerchantDetailsObj(merchantGridViewService.getAllReselerMerchants(sessionUser.getResellerId()));
				return SUCCESS;
			} catch (Exception exception) {
				logger.error("Exception", exception);
				return ERROR;
			}
	 }
	 	
	
	public String getMerchantStatus() {
		return merchantStatus;
	}
	public void setMerchantStatus(String merchantStatus) {
		this.merchantStatus = merchantStatus;
	}
	public String getByWhom() {
		return byWhom;
	}
	public void setByWhom(String byWhom) {
		this.byWhom = byWhom;
	}
	public List<MerchantDetails> getaaData() {
		return aaData;
	}

	public void setaaData(List<MerchantDetails> setaaData) {
		this.aaData = setaaData;
	}
	public String getBusinessType() {
		return businessType;
	}
	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}
	
	
}