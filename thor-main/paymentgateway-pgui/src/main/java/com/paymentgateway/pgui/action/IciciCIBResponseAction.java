package com.paymentgateway.pgui.action;

import java.io.BufferedReader;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.pg.core.util.IciciUtil;

public class IciciCIBResponseAction extends AbstractSecureAction implements ServletRequestAware{
	
	/**
	 * 
	 */
	
	@Autowired
	IciciUtil iciciUtil;

	
	private static final long serialVersionUID = 2653647622127575615L;
	private static Logger logger = LoggerFactory.getLogger(IciciCIBResponseAction.class.getName());
	
	private Fields responseMap = null;
	private HttpServletRequest httpRequest;

	@Override
	public void setServletRequest(HttpServletRequest request) {
		this.httpRequest = request;
	}
	
	public String execute() {
		try {
			BufferedReader inputBuffered = httpRequest.getReader();
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = inputBuffered.readLine()) != null) {
				response.append(inputLine);
			}
			
			inputBuffered.close();
			logger.info("ICICI Callback Response "+response.toString());
			//Decrypt The Response
			String decryptedResponse=iciciUtil.decrypt(response.toString());
			logger.info("ICICI Callback Response dycrypted "+decryptedResponse);
			JSONObject json=new JSONObject(decryptedResponse);
			String txnId=(String) json.get("UNIQUEID");
			
			iciciUtil.IciciCibResponseHandler(json,txnId);
			logger.info("CIB Callback Updated for Txn Id "+txnId);
			return decryptedResponse;
			
		}catch (Exception e) {
			logger.info("exception "+e);
		}
		return SUCCESS;
	}
	
	
}
