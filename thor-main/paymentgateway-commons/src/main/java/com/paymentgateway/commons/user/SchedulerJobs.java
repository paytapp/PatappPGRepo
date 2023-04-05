/**
 * POJO for all scheduler based jobs in DB 
 */
package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Proxy;

import com.paymentgateway.commons.util.JobType;

/**
 * @author Amitosh Aanand
 *
 */
@Entity
@Proxy(lazy = false)
public class SchedulerJobs implements Serializable {

	private static final long serialVersionUID = -6269395713253643552L;

	public SchedulerJobs() {

	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long jobId;
	
	private String jobType;
	private String jobTime;
	private String jobFrequency;
	private String jobDetails;
	
	@Column(columnDefinition = "TEXT", length = 65535)
	private String jobParams;
	
	private boolean jobStatus;
	private Date createdDate;
	private Date updatedDate;
	private String createdBy;
	private String updatedBy;

	public Long getJobId() {
		return jobId;
	}
	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}
	public String getJobType() {
		return jobType;
	}
	public void setJobType(String jobType) {
		this.jobType = jobType;
	}
	public String getJobTime() {
		return jobTime;
	}
	public void setJobTime(String jobTime) {
		this.jobTime = jobTime;
	}
	public String getJobFrequency() {
		return jobFrequency;
	}
	public void setJobFrequency(String jobFrequency) {
		this.jobFrequency = jobFrequency;
	}
	public String getJobDetails() {
		return jobDetails;
	}
	public void setJobDetails(String jobDetails) {
		this.jobDetails = jobDetails;
	}
	public String getJobParams() {
		return jobParams;
	}
	public void setJobParams(String jobParams) {
		this.jobParams = jobParams;
	}
	public boolean isJobStatus() {
		return jobStatus;
	}
	public void setJobStatus(boolean jobStatus) {
		this.jobStatus = jobStatus;
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
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
}