/**
 * Action handler class for Scheduler jobs
 */
package com.paymentgateway.crm.action;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.JobTimeFactory;
import com.paymentgateway.crm.actionBeans.SchedulerJobsFactory;
import com.paymentgateway.crm.mpa.Constants;

/**
 * @author Amitosh Aanand
 *
 */
public class SchedulerJobsAction extends AbstractSecureAction {

	@Autowired
	private SchedulerJobsFactory schedulerJobsFactory;
	
	@Autowired
	private JobTimeFactory time;

	private String jobId;

	private String jobType;
	private String jobTime;
	private String jobFrequency;
	private String jobDetails;

	private String precedingTime;
	private String timeIntervalSlot;
	private String acquirerType;
	private String txnType;
	private String paymentType;
	private String payId;
	private String status;
	private String autoRefund;

	private Object jobs;

	private User sessionUser = new User();

	private static final long serialVersionUID = -3441312741164627398L;
	private static Logger logger = LoggerFactory.getLogger(SchedulerJobsAction.class.getName());

	// It can fetch all active jobs along with job parameters for selected job type
	public String execute() {
		try {
			logger.info("Inside execute(), SchedulerJobAction");
			if (StringUtils.isBlank(jobId)) {
				setJobs(schedulerJobsFactory.fetchActiveJobsAndJobParamsByJobType(jobType));
			} else {
				setJobs(schedulerJobsFactory.fetchActiveJobsAndJobParamsByJobId(jobId));
			}
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	// It can create new job
	public String createSchedulerJob() {
		try {
			logger.info("Inside createSchedulerJob(), SchedulerJobAction");
			sessionUser = (User) sessionMap.get(Constants.USER);
			String jobParams = schedulerJobsFactory.createJobParams(precedingTime, timeIntervalSlot, acquirerType,
					txnType, paymentType, payId, status, autoRefund);
			addActionMessage(schedulerJobsFactory.createJob(jobType, time.manageJobTime(jobTime),
					jobFrequency, jobDetails, jobParams, sessionUser));
			setJobs(schedulerJobsFactory.fetchActiveJobsAndJobParamsByJobType(null));
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	// It can update an existing active job using jobId
	public String updateSchedulerJob() {
		try {
			logger.info("Inside updateSchedulerJob(), SchedulerJobAction");
			sessionUser = (User) sessionMap.get(Constants.USER);
			String jobParams = schedulerJobsFactory.createJobParams(precedingTime, timeIntervalSlot, acquirerType,
					txnType, paymentType, payId, status, autoRefund);
			addActionMessage(schedulerJobsFactory.updateJob(jobId, time.manageJobTime(jobTime),
					jobFrequency, jobDetails, jobParams, sessionUser));
			setJobs(schedulerJobsFactory.fetchActiveJobsAndJobParamsByJobType(null));
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	// It can delete an existing job using jobId
	public String deleteSchedulerJob() {
		try {
			logger.info("Inside deleteSchedulerJob(), SchedulerJobAction");
			sessionUser = (User) sessionMap.get(Constants.USER);
			addActionMessage(schedulerJobsFactory.deleteJob(jobId, sessionUser));
			setJobs(schedulerJobsFactory.fetchActiveJobsAndJobParamsByJobType(null));
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getPrecedingTime() {
		return precedingTime;
	}

	public void setPrecedingTime(String precedingTime) {
		this.precedingTime = precedingTime;
	}

	public String getTimeIntervalSlot() {
		return timeIntervalSlot;
	}

	public void setTimeIntervalSlot(String timeIntervalSlot) {
		this.timeIntervalSlot = timeIntervalSlot;
	}

	public String getAcquirerType() {
		return acquirerType;
	}

	public void setAcquirerType(String acquirerType) {
		this.acquirerType = acquirerType;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
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

	public Object getJobs() {
		return jobs;
	}

	public void setJobs(Object jobs) {
		this.jobs = jobs;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAutoRefund() {
		return autoRefund;
	}

	public void setAutoRefund(String autoRefund) {
		this.autoRefund = autoRefund;
	}
}