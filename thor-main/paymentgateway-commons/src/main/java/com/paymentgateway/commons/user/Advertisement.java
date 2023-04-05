package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Proxy;

import com.paymentgateway.commons.util.TDRStatus;

@Entity
@Proxy(lazy= false)
@Table
public class Advertisement implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3678505640681661141L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	private boolean showAds;
	private Date createdDate;
	private Date updatedDate;
	private String processedBy;
	private String bannerURL;
	private String linkURL;
	
	private String status;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Date getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}
	public String getProcessedBy() {
		return processedBy;
	}
	public void setProcessedBy(String processedBy) {
		this.processedBy = processedBy;
	}
	public boolean isShowAds() {
		return showAds;
	}
	public void setShowAds(boolean showAds) {
		this.showAds = showAds;
	}
	public String getBannerURL() {
		return bannerURL;
	}
	public void setBannerURL(String bannerURL) {
		this.bannerURL = bannerURL;
	}
	public String getLinkURL() {
		return linkURL;
	}
	public void setLinkURL(String linkURL) {
		this.linkURL = linkURL;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
}
