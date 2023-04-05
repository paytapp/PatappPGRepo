package com.paymentgateway.crm.action;
/*package com.paymentgateway.crm.action;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;

*//**
 * Neeraj
 *//*
public class UpdateServiceTaxAction extends AbstractSecureAction {
	
	@Autowired
	private CrmValidator validator;

	private static final long serialVersionUID = -228182371499688572L;
	private static Logger logger = LoggerFactory.getLogger(UpdateServiceTaxAction.class.getName());
	private ChargingDetails chargingDetails = new ChargingDetails();
	private String merchantServiceTax;
	private String response;

	public String execute() {
		try {

		} catch (Exception exception) {
			setResponse(ErrorType.CHARGINGDETAIL_NOT_SAVED.getResponseMessage());
			logger.error("Exception", exception);
		}

		return SUCCESS;

	}

	public void validate() {

		if (validator.validateBlankField(getMerchantServiceTax())) {
		} else if (!validator.validateField(CrmFieldType.MERCHANT_SERVICE_TAX, getMerchantServiceTax())) {
			addFieldError(CrmFieldType.MERCHANT_SERVICE_TAX.getName(),
					ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
		if (validator.validateBlankField(getResponse())) {
		} else if (!validator.validateField(CrmFieldType.RESPONSE, getResponse())) {
			addFieldError(CrmFieldType.RESPONSE.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
	}

	public ChargingDetails getChargingDetails() {
		return chargingDetails;
	}

	public void setChargingDetails(ChargingDetails chargingDetails) {
		this.chargingDetails = chargingDetails;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getMerchantServiceTax() {
		return merchantServiceTax;
	}

	public void setMerchantServiceTax(String merchantServiceTax) {
		this.merchantServiceTax = merchantServiceTax;
	}

}*/