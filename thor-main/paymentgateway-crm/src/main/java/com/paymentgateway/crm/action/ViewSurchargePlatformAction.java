package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.Surcharge;
import com.paymentgateway.commons.user.SurchargeDetails;
import com.paymentgateway.commons.user.SurchargeMappingPopulator;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.ViewSurchargePopulator;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.AcquirerTypeUI;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.crm.actionBeans.SurchargeAcquirerDetailsFactory;
import com.paymentgateway.crm.actionBeans.SurchargeDetailsFactory;
import com.paymentgateway.crm.actionBeans.SurchargeMappingDetailsFactory;
import com.paymentgateway.crm.actionBeans.ViewSurchargeMappingDetailsFactory;

public class ViewSurchargePlatformAction extends AbstractSecureAction {
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private CrmValidator validator;
	
	@Autowired
	private SurchargeAcquirerDetailsFactory surchargeAcquirerDetailProvider;
	
	@Autowired
	private SurchargeDetailsFactory surchargeDetailProvider;
	
	@Autowired
	ViewSurchargeMappingDetailsFactory surchargeMappingDetailsFactory;
	
	private static Logger logger = LoggerFactory.getLogger(ViewSurchargePlatformAction.class.getName());
	private static final long serialVersionUID = -6879974923614009981L;

	public List<Merchants> listMerchant = new ArrayList<Merchants>();
	private Map<String, List<SurchargeDetails>> aaData = new HashMap<String, List<SurchargeDetails>>();
	private Map<String, List<Surcharge>> acquirerData = new HashMap<String, List<Surcharge>>();
	private Map<String, List<ViewSurchargePopulator>> surchargeMapData = new HashMap<String, List<ViewSurchargePopulator>>();
//	private List<ViewSurchargePopulator> surchargeMapData = new ArrayList<ViewSurchargePopulator>();
	private String emailId;
	private String acquirerType;

	@SuppressWarnings("unchecked")
	public String execute() {
		setListMerchant(userDao.getMerchantList());
	

		try {
			if (emailId != null && acquirerType != null) {

				User user = userDao.findPayIdByEmail(emailId);
				String payId = user.getPayId();

				setSurchargeMapData(surchargeMappingDetailsFactory.getSurchargeAcquirerDetails(payId, acquirerType));

				/*if (surchargeMapData.size() == 0) {

					addActionMessage(ErrorType.SURCHARGEDETAIL_NOT_FETCHED.getResponseMessage());
				}*/

			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
			addActionMessage(ErrorType.UNKNOWN.getResponseMessage());
		}

		return INPUT;
	}

	
	// To display page without using token
	@SuppressWarnings("unchecked")
	public String displayList() {
		setListMerchant(userDao.getMerchantList());
		return INPUT;
	}

//	public void validate() {
//		if ((validator.validateBlankField(getAcquirerType()))) {
//		} else if (!validator.validateField(CrmFieldType.ACQUIRER, getAcquirerType())) {
//			addFieldError(CrmFieldType.ACQUIRER.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
//		}
////		if (validator.validateBlankField(getEmailId())) {
////		} else if (!validator.validateField(CrmFieldType.EMAILID, getEmailId())) {
////			addFieldError("emailId", ErrorType.INVALID_FIELD.getResponseMessage());
////		}
//	}

	
	
	
	public Map<String, List<ViewSurchargePopulator>> getSurchargeMapData() {
		return surchargeMapData;
	}


	public void setSurchargeMapData(Map<String, List<ViewSurchargePopulator>> surchargeMapData) {
		this.surchargeMapData = surchargeMapData;
	}

	
	
	

	public List<Merchants> getListMerchant() {
		return listMerchant;
	}

//	public List<ViewSurchargePopulator> getSurchargeMapData() {
//		return surchargeMapData;
//	}
//
//
//	public void setSurchargeMapData(List<ViewSurchargePopulator> surchargeMapData) {
//		this.surchargeMapData = surchargeMapData;
//	}


	public void setListMerchant(List<Merchants> listMerchant) {
		this.listMerchant = listMerchant;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}



	public Map<String, List<SurchargeDetails>> getAaData() {
		return aaData;
	}

	public void setAaData(Map<String, List<SurchargeDetails>> aaData) {
		this.aaData = aaData;
	}



	public Map<String, List<Surcharge>> getAcquirerData() {
		return acquirerData;
	}

	public void setAcquirerData(Map<String, List<Surcharge>> acquirerData) {
		this.acquirerData = acquirerData;
	}


	public String getAcquirerType() {
		return acquirerType;
	}


	public void setAcquirerType(String acquirerType) {
		this.acquirerType = acquirerType;
	}


	
 

}
