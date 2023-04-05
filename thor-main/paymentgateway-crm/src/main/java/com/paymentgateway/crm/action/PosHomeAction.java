package com.paymentgateway.crm.action;

import org.apache.struts2.dispatcher.SessionMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ActionContext;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Constants;

/**
 * @author Shaiwal
 *
 */

public class PosHomeAction extends AbstractSecureAction {

	protected SessionMap<String, Object> sessionMap;

	private static Logger logger = LoggerFactory.getLogger(PosHomeAction.class.getName());
	private static final long serialVersionUID = -452094231699163577L;
	private User sessionUser = new User();
	
	public String execute() {
		
		sessionMap = (SessionMap<String, Object>) ActionContext.getContext().getSession();
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		
		sessionMap.put(Constants.TERMINAL_ID.getValue(), sessionUser.getTerminalId());
		sessionMap.put(Constants.PAY_ID.getValue(), sessionUser.getPayId());
		
		return SUCCESS;
	}

}
