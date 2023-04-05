package com.paymentgateway.crm.action;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.paymentgateway.commons.dao.PayoutAcquirerMappingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.PayoutAcquireMapping;
import com.paymentgateway.commons.user.UserDao;

/**
 * Shiva
 */
public class PayoutAcquireMappingAction extends AbstractSecureAction implements ModelDriven<PayoutAcquireMapping> {

	private static final long serialVersionUID = 8153187660271574455L;

	private static Logger logger = LoggerFactory.getLogger(PayoutAcquireMappingAction.class.getName());

	@Autowired
	private PayoutAcquirerMappingDao payoutAcquirerMappingDao;
	
	@Autowired
	private UserDao userDao;

	private PayoutAcquireMapping acqMapping = new PayoutAcquireMapping();
	
	private static String Success = "success";
	private static String Failed = "failed";

	public String execute() {
		logger.info("inside Saving Payout Mapping");
		try {
			if (acqMapping != null) {

				payoutAcquirerMappingDao.saveMapping(acqMapping);
				logger.info("Payout Mapping Saved Successfully");
				
				acqMapping.setResponse(Success);
				acqMapping.setResponseMsg("Data Saved Successfully");
			}			
		} catch (Exception e) {
			logger.info("Exception while saving the payout mapping",e);
			acqMapping.setResponse(Failed);
			acqMapping.setResponseMsg("System Exception");
			
		}

		return SUCCESS;
	}
	
	public String fetchRecord() {

		try {
			if (acqMapping != null) {

				payoutAcquirerMappingDao.fetchSavedMapping(acqMapping);
				
				acqMapping.setResponse(Success);
				acqMapping.setResponseMsg("Data Fetched");
			}
		} catch (Exception e) {
			logger.info("Exception while Fetching the payout mapping",e);
			acqMapping.setResponse(Failed);
			acqMapping.setResponseMsg("System Exception");
		}

		return SUCCESS;
	}
	
	public String saveMerchantMapping() {

		try {
			if (acqMapping != null) {
				
				if(StringUtils.isNotBlank(acqMapping.getSubMerchantPayId())){
					acqMapping.setSubMerchantName(userDao.getBusinessNameByPayId(acqMapping.getSubMerchantPayId()));
					acqMapping.setMerchantName(userDao.getBusinessNameByPayId(acqMapping.getPayId()));
				}else{
					acqMapping.setMerchantName(userDao.getBusinessNameByPayId(acqMapping.getPayId()));
				}
				
				payoutAcquirerMappingDao.saveMerchantMapping(acqMapping);
				
				if(acqMapping.getStatus().equals(ErrorType.SUCCESS.getResponseMessage())){
					logger.info("Payout Merchant Mapping Saved Successfully");
					
					acqMapping.setResponse(Success);
					acqMapping.setResponseMsg("Mapping Saved Successfully");
				}else if(acqMapping.getStatus().equals(ErrorType.DUPLICATE.getResponseMessage())){
					logger.info("Found Duplicate Payout Merchant Mapping");
					
					acqMapping.setResponse(Failed);
					acqMapping.setResponseMsg("Duplicate Mapping Found");
				}else{
					logger.info("Failed Payout Merchant Mapping");
					
					acqMapping.setResponse(Failed);
					acqMapping.setResponseMsg("Mapping not saved Failed status");
				}
			}
		} catch (Exception e) {
			logger.info("Exception while saving the payout mapping",e);
			acqMapping.setResponse(Failed);
			acqMapping.setResponseMsg("Mapping not saved");
		}

		return SUCCESS;
	}
	
	public String fetchMerchantRecord() {

		try {
			if (acqMapping != null) {

				payoutAcquirerMappingDao.fetchSavedMerchantMapping(acqMapping);
				
				acqMapping.setResponse(Success);
				acqMapping.setResponseMsg("Data Fetched");
			}
		} catch (Exception e) {
			logger.info("Exception while Fetching the payout mapping",e);
			acqMapping.setResponse(Failed);
			acqMapping.setResponseMsg("System Exception");
		}

		return SUCCESS;
	}
	
	public String fetchAllMerchantRecords() {

		try {
			if (acqMapping != null) {

				payoutAcquirerMappingDao.fetchAllSavedMerchantMapping(acqMapping);
				
				acqMapping.setResponse(Success);
				acqMapping.setResponseMsg("Data Fetched");
			}
		} catch (Exception e) {
			logger.info("Exception while Fetching the payout mapping",e);
			acqMapping.setResponse(Failed);
			acqMapping.setResponseMsg("System Exception");
		}

		return SUCCESS;
	}
	
	public String deleteMerchantRecord() {

		try {
			if (acqMapping != null) {

				payoutAcquirerMappingDao.deleteMerchantMapping(acqMapping);
				
				acqMapping.setResponse(Success);
				acqMapping.setResponseMsg("Mapping deleted");
			}
		} catch (Exception e) {
			logger.info("Exception while deleting the payout mapping",e);
			acqMapping.setResponse(Failed);
			acqMapping.setResponseMsg("System Exception");
		}

		return SUCCESS;
	}
	
	@Override
	public PayoutAcquireMapping getModel() {
		return acqMapping;
	}

	public PayoutAcquireMapping getAcqMapping() {
		return acqMapping;
	}

	public void setAcqMapping(PayoutAcquireMapping acqMapping) {
		this.acqMapping = acqMapping;
	}
	

}	
