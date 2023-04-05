package com.paymentgateway.crm.mpa;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.json.JSONUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.opensymphony.xwork2.Action;
import com.paymentgateway.crm.action.AbstractSecureAction;

/**
 * @author Amitosh Aanand
 *
 */
public class MPACallbackResponseAction extends AbstractSecureAction implements ServletRequestAware {

	private static final long serialVersionUID = -1007034629222977683L;
	private HttpServletRequest httpRequest;
	private static Logger logger = LoggerFactory.getLogger(MPACallbackResponseAction.class);

	@Autowired
	private MPAServicesFactory mpaServicesFactory;
	
	
	
	@Override
	public void setServletRequest(HttpServletRequest request) {
		this.httpRequest = request;
	}
	public MPACallbackResponseAction() {
	}
	
	public String execute() {

		logger.info("Inside execute(), MPACallbackResponseAction");

		try {
			Object obj = JSONUtil.deserialize(httpRequest.getReader());
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(obj);
			logger.info("Response received from Signzy on call back response url: " + json);
			JSONObject res = new JSONObject(json);

			// Add method to save callback response
			return Action.NONE;
		} catch (Exception e) {

			logger.error("Error in idfc bank UPI callback = " , e);
		}

		return Action.NONE;
	}

	public String esignCallbackResponse() {
		
		logger.info("Inside esignCallbackResponse(), MPACallbackResponseAction");
		try {
			Object obj = JSONUtil.deserialize(httpRequest.getReader());
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(obj);
			//logger.info("Response received from Signzy on ESIGN call back response url: " + json);
			JSONObject res = new JSONObject(json);
			logger.info("Response received from Signzy on ESIGN call back response url: " + res);
			
			mpaServicesFactory.saveEsignResponseData(res);
			
			logger.info("Response of ESIGN call back url is saved: " + res);
			return Action.NONE;
		} catch (Exception e) {

			logger.error("Error in ESIGN callback URL = " , e);
		}
		
	return Action.NONE;	
	}
	
//	public String esignRedirectResponse() {
//		
//		logger.info("Inside esignRedirectResponse(), MPACallbackResponseAction");
//		try {
//			
//			setMpaData(factory.fetchSavedStageData(null, getPayId(), null));
//			
//		} catch (Exception e) {
//
//			logger.error("Error in ESIGN Redirect URL = " + e);
//		}
//	return SUCCESS;
//	}


	
	
}
