package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.ChargingDetailsFactory;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;

public class ChargingPlatformAction extends AbstractSecureAction {

	@Autowired
	private ChargingDetailsFactory chargingDetailProvider;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private CrmValidator validator;
	
	private static Logger logger = LoggerFactory
			.getLogger(ChargingPlatformAction.class.getName());
	private static final long serialVersionUID = -6879974923614009981L;

	public List<Merchants> listMerchant = new ArrayList<Merchants>();
	private Map<String, Object> aaData = new HashMap<String, Object>();
	private String emailId;
	private String acquirer;
	private String paymentRegion;
	private String acquiringMode;
	private String cardHolderType;
	private String paymentType;
	private String response;

	@SuppressWarnings("unchecked")
	public String execute() {
		setListMerchant(userDao.getMerchantList());
		try {
			if (emailId != null && acquirer != null) {
				setAaData(chargingDetailProvider.getChargingDetailsMap(emailId,
						acquirer,paymentRegion, acquiringMode, cardHolderType,paymentType));
				if(aaData.size()==0){
					addActionMessage(ErrorType.CHARGINGDETAIL_NOT_FETCHED.getResponseMessage());
				}
				if(aaData.size()==1 && aaData.containsKey("regionType")) {
					setResponse("Merchant Mapping not saved");
				}
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
			addActionMessage(ErrorType.GST_DEATILS_NOT_AVAILABLE.getResponseMessage());
		}
		return INPUT;
	}

	// To display page without using token
	@SuppressWarnings("unchecked")
	public String displayList() {
		setListMerchant(userDao.getMerchantList());
		return INPUT;
	}

	
	
	public void validate() {
		if ((validator.validateBlankField(getAcquirer()))) {
		} else if (!validator.validateField(CrmFieldType.ACQUIRER,
				getAcquirer())) {
			addFieldError(CrmFieldType.ACQUIRER.getName(),
					ErrorType.INVALID_FIELD.getResponseMessage());
		}
		if (validator.validateBlankField(getEmailId())) {
		} else if (!validator.validateField(CrmFieldType.EMAILID, getEmailId())) {
			addFieldError("emailId",
					ErrorType.INVALID_FIELD.getResponseMessage());
		}
	}

	
	
	public Map<String, Object> getAaData() {
		return aaData;
	}

	public void setAaData(Map<String, Object> aaData) {
		this.aaData = aaData;
	}

	public String getPaymentRegion() {
		return paymentRegion;
	}

	public void setPaymentRegion(String paymentRegion) {
		this.paymentRegion = paymentRegion;
	}

	public String getAcquiringMode() {
		return acquiringMode;
	}

	public void setAcquiringMode(String acquiringMode) {
		this.acquiringMode = acquiringMode;
	}

	public String getCardHolderType() {
		return cardHolderType;
	}

	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}

	public List<Merchants> getListMerchant() {
		return listMerchant;
	}

	public void setListMerchant(List<Merchants> listMerchant) {
		this.listMerchant = listMerchant;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
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
	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}
}
