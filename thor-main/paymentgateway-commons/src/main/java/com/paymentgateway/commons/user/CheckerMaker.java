package com.paymentgateway.commons.user;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Proxy;

@Entity
@Proxy(lazy= false)
public class CheckerMaker implements Serializable{

	private static final long serialVersionUID = 7987196259699866265L;
	
	@Id
	@Column(nullable=false,unique=true)
	private String industryCategory;
	private String checkerPayId;
	private String makerPayId; 
	private String checkerName;
	private String makerName;
	
	
	public CheckerMaker() {
		
	}
	
	public String getIndustryCategory() {
		return industryCategory;
	}
	public void setIndustryCategory(String industryCategory) {
		this.industryCategory = industryCategory;
	}
	public String getCheckerPayId() {
		return checkerPayId;
	}
	public void setCheckerPayId(String checkerPayId) {
		this.checkerPayId = checkerPayId;
	}
	public String getMakerPayId() {
		return makerPayId;
	}
	public void setMakerPayId(String makerPayId) {
		this.makerPayId = makerPayId;
	}
	public String getCheckerName() {
		return checkerName;
	}
	public void setCheckerName(String checkerName) {
		this.checkerName = checkerName;
	}
	public String getMakerName() {
		return makerName;
	}
	public void setMakerName(String makerName) {
		this.makerName = makerName;
	}
	
	
		
}
