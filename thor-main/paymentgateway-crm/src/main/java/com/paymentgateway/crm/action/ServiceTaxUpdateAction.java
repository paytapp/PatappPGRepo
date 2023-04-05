package com.paymentgateway.crm.action;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.ChargingDetailsDao;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.TDRStatus;

/**
 * @author Puneet
 *
 */
public class ServiceTaxUpdateAction extends AbstractSecureAction {

	@Autowired
	private ChargingDetailsDao chargingDetailsDao;

	@Autowired
	private CrmValidator validator;

	private static final long serialVersionUID = -1465492690619542607L;

	private String response;
	private double newServiceTax;
	private static Logger logger = LoggerFactory.getLogger(ServiceTaxUpdateAction.class.getName());

	public String execute() {
		try {

			Date currentDate = new Date();
			// get all active TDRs
			List<ChargingDetails> chargingDetailList = chargingDetailsDao.getAllActiveChargingDetails();
			// change one by one
			// create new
			for (ChargingDetails oldChargingDetail : chargingDetailList) {
				ChargingDetails newChargingDetail = SerializationUtils.clone(oldChargingDetail);
				newChargingDetail.setId(null);
				// create new TDR
				newChargingDetail.setPgServiceTax(newServiceTax);
				newChargingDetail.setBankServiceTax(newServiceTax);
				newChargingDetail.setMerchantServiceTax(newServiceTax);
				newChargingDetail.setStatus(TDRStatus.ACTIVE);
				newChargingDetail.setCreatedDate(currentDate);
				newChargingDetail.setId(null);
				// deactivate old TDR
				oldChargingDetail.setStatus(TDRStatus.INACTIVE);
				oldChargingDetail.setUpdatedDate(currentDate);

			}
			setResponse("Service tax updated successfully for all active TDRs");
		} catch (Exception exception) {
			logger.error("Error updating servcice tax " , exception);
			setResponse(ErrorType.INTERNAL_SYSTEM_ERROR.getResponseMessage());
		}
		// return response
		// send mail on completion/error
		return SUCCESS;
	}

	public void validate() {
		if (validator.validateBlankField(getResponse())) {
			//addFieldError(CrmFieldType.RESPONSE.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.RESPONSE, getResponse()))) {
			addFieldError(CrmFieldType.RESPONSE.getName(), validator.getResonseObject().getResponseMessage());
		}
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public double getNewServiceTax() {
		return newServiceTax;
	}

	public void setNewServiceTax(double newServiceTax) {
		this.newServiceTax = newServiceTax;
	}
}
