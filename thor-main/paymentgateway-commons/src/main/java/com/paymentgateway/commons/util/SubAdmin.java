package com.paymentgateway.commons.util;

import java.io.Serializable;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.user.User;

/**
 * @author shashi
 *
 */

@Service
public class SubAdmin implements Serializable {

	private static final long serialVersionUID = -5371568875441496496L;

	public SubAdmin() {

	}

	private String agentEmailId;
	private String agentFirstName;
	private String agentLastName;
	private String payId;
	private String agentExperience;
	private String agentMobile;
	private Boolean agentIsActive;
	private String permissionType;

	public void setSubAdmin(User user) {
		setAgentEmailId(user.getEmailId());
		setAgentFirstName(user.getFirstName());
		setAgentLastName(user.getLastName());
		setPayId(user.getPayId());

	}
	

	public String getPermissionType() {
		return permissionType;
	}


	public void setPermissionType(String permissionType) {
		this.permissionType = permissionType;
	}


	public String getAgentEmailId() {
		return agentEmailId;
	}

	public void setAgentEmailId(String agentEmailId) {
		this.agentEmailId = agentEmailId;
	}

	public String getAgentFirstName() {
		return agentFirstName;
	}

	public void setAgentFirstName(String agentFirstName) {
		this.agentFirstName = agentFirstName;
	}

	public String getAgentLastName() {
		return agentLastName;
	}

	public void setAgentLastName(String agentLastName) {
		this.agentLastName = agentLastName;
	}

	public String getAgentExperience() {
		return agentExperience;
	}

	public void setAgentExperience(String agentExperience) {
		this.agentExperience = agentExperience;
	}

	public String getAgentMobile() {
		return agentMobile;
	}

	public void setAgentMobile(String agentMobile) {
		this.agentMobile = agentMobile;
	}

	public Boolean getAgentIsActive() {
		return agentIsActive;
	}

	public void setAgentIsActive(Boolean agentIsActive) {
		this.agentIsActive = agentIsActive;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

}
