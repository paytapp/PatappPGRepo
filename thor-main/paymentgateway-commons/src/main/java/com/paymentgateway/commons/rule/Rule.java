package com.paymentgateway.commons.rule;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import org.hibernate.annotations.Proxy;

/**
 * @author Surender
 *
 */

@Entity
@Proxy(lazy= false)
public class Rule {

	@Id
	@Column(nullable=false,unique=true)
	private String id;
	
	private String name;	
	private String field1;
	private String field2;
	
	@Enumerated(EnumType.STRING)
	private OperandType operandType;
	
	@Enumerated(EnumType.STRING)
	private OperatorType operatorType;
	
	private Rule rule;

	public Rule() {
	}

	public Rule(String field1, String field2, OperandType operandType,
			OperatorType operatorType) {
		this.field1 = field1;
		this.field2 = field2;
		this.operandType = operandType;
		this.operatorType = operatorType;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getField1() {
		return field1;
	}

	public void setField1(String field1) {
		this.field1 = field1;
	}

	public String getField2() {
		return field2;
	}

	public void setField2(String field2) {
		this.field2 = field2;
	}

	public OperandType getOperandType() {
		return operandType;
	}

	public void setOperandType(OperandType operandType) {
		this.operandType = operandType;
	}

	public OperatorType getOperatorType() {
		return operatorType;
	}

	public void setOperatorType(OperatorType operatorType) {
		this.operatorType = operatorType;
	}

	public Rule getRule() {
		return rule;
	}

	public void setRule(Rule rule) {
		this.rule = rule;
	}
}
