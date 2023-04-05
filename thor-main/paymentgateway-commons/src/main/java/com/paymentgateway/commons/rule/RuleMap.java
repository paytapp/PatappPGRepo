package com.paymentgateway.commons.rule;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import org.hibernate.annotations.Proxy;

import com.paymentgateway.commons.util.AcquirerType;

/**
 * @author Surender
 *
 */

@Entity
@Proxy(lazy= false)
public class RuleMap {
	
	@Id
	@Column(nullable=false,unique=true)
	private String id;

	private String ruleId;
	
	@Enumerated(EnumType.STRING)
	private AcquirerType acquirerType;
	
	public RuleMap() {
	}

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public AcquirerType getAcquirerType() {
		return acquirerType;
	}

	public void setAcquirerType(AcquirerType acquirerType) {
		this.acquirerType = acquirerType;
	}	
}
