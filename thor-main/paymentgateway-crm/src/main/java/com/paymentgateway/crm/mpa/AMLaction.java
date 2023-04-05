package com.paymentgateway.crm.mpa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.MPADao;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.crm.action.AbstractSecureAction;

/**
 * @author Pooja Pancholi
 */

public class AMLaction extends AbstractSecureAction {

	@Autowired
	private AMLService amlService;
	
	@Autowired
	private MPADao mpaDao;

	// Mandatory fields
	private String payId;
	private String firstName;
	private String customerCategory;

	// Optional fields
	private String middleName;
	private String lastName;
	private String cin;
	private String din;
	private String pan;
	private String mobile;
	private String result;

	private static final long serialVersionUID = 6892432803032157615L;
	private static Logger logger = LoggerFactory.getLogger(AMLaction.class.getName());

	private User user = new User();
	public String execute() {

		logger.info("Inside execute(), AMLaction");
		
		String amlPayId = null;
		user = (User) sessionMap.get(Constants.USER.getValue());
		MerchantProcessingApplication mpaData = null;
		if (user.getUserType().equals(UserType.MERCHANT)) {
			amlPayId = user.getPayId();
			mpaData = mpaDao.fetchMPADataByPayId(amlPayId);
		} else if (user.getUserType().equals(UserType.SUBUSER)) {
			amlPayId = user.getPayId();
			mpaData = mpaDao.fetchMPADataByPayId(amlPayId);
		} else if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
				|| user.getUserType().equals(UserType.SUPERADMIN)) {
			amlPayId = payId;
			mpaData = mpaDao.fetchMPADataByPayId(amlPayId);
		}

		Map<String, String> requestMap = new HashMap<>();
		Map<String, String> respMap;
		try {

			if (StringUtils.isNotBlank(mpaData.getDirector1FullName())) {
				requestMap.put("FirstName", mpaData.getDirector1FullName());
			}

			if (StringUtils.isNotBlank(middleName)) {
				requestMap.put("MiddleName", middleName);
			}

			if (StringUtils.isNotBlank(lastName)) {
				requestMap.put("LastName", lastName);
			}

				requestMap.put("CustomerCategory", "IND");

			if (StringUtils.isNotBlank(mpaData.getDirector1Pan())) {
				requestMap.put("Pan", mpaData.getDirector1Pan());
			}
			
			if (StringUtils.isNotBlank(mpaData.getDirector1Mobile())) {
				requestMap.put("PersonalMobileNumber", mpaData.getDirector1Mobile());
			}
			
			if (StringUtils.isNotBlank(mpaData.getDirector1Email())) {
				requestMap.put("PersonalEmail", mpaData.getDirector1Email());
			}
			
			String response = amlService.getAMLResponse(requestMap, amlPayId);

			if (StringUtils.isNoneBlank(response)) {
				setResult("Success");
			} else {
				setResult("Failed");
			}
			return SUCCESS;

		} catch (Exception e) {
			return ERROR;
		}
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getCustomerCategory() {
		return customerCategory;
	}

	public void setCustomerCategory(String customerCategory) {
		this.customerCategory = customerCategory;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getCin() {
		return cin;
	}

	public void setCin(String cin) {
		this.cin = cin;
	}

	public String getDin() {
		return din;
	}

	public void setDin(String din) {
		this.din = din;
	}

	public String getPan() {
		return pan;
	}

	public void setPan(String pan) {
		this.pan = pan;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

}
