package com.paymentgateway.pgui.action;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.pgui.action.service.FetchEventPagesDetailService;

/**
 * @author Rahul
 *
 */
public class EventPagesDetailAction extends AbstractSecureAction{

	private static final long serialVersionUID = 8573079391051192021L;
	
	@Autowired
	private FetchEventPagesDetailService fetchEventPagesDetailService;
	
	private String payId;
	private String uniqueNo;
	private String userInfoString;
	
	public String execute() {
		JSONObject userInfo = fetchEventPagesDetailService.prepareEventDetails(uniqueNo, payId);
		setUserInfoString(userInfo.toString());
		return SUCCESS;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getUniqueNo() {
		return uniqueNo;
	}

	public void setUniqueNo(String uniqueNo) {
		this.uniqueNo = uniqueNo;
	}

	public String getUserInfoString() {
		return userInfoString;
	}

	public void setUserInfoString(String userInfoString) {
		this.userInfoString = userInfoString;
	}
	
}
