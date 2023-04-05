/**
 * It will act as a service factory for scheduler jobs
 */
package com.paymentgateway.crm.actionBeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.SchedulerJobsDao;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.SchedulerJobs;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.JobType;

/**
 * @author Amitosh Aanand
 *
 */
@Service
@SuppressWarnings("unchecked")
public class SchedulerJobsFactory {

	@Autowired
	private SchedulerJobsDao schedulerJobsDao;

	@Autowired
	private UserDao userDao;

	private static Logger logger = LoggerFactory.getLogger(SchedulerJobsFactory.class.getName());

	public HashMap<String, Object> fetchActiveJobsAndJobParamsByJobType(String jobType) {
		StringBuilder query = new StringBuilder("select jobId, jobType, jobTime, jobFrequency, jobDetails "
				+ "from SchedulerJobs SJ where SJ.jobStatus=true");
		HashMap<String, Object> jobMap = new HashMap<String, Object>();
		if (StringUtils.isBlank(jobType) || jobType.equalsIgnoreCase("ALL")) {
			logger.info("Fetching all active jobs");
			jobMap.put(Constants.ACTIVE_JOBS.getValue(), schedulerJobsDao.fetchActiveJobs(query.toString()));
			jobMap.put(Constants.JOB_PARAMS.getValue(), new ArrayList());
		} else {
			logger.info("Fetching all active jobs with " + jobType + " as job type");
			query.append(" and jobType ='" + jobType + "'");
			jobMap.put(Constants.ACTIVE_JOBS.getValue(), schedulerJobsDao.fetchActiveJobs(query.toString()));
			jobMap.put(Constants.JOB_PARAMS.getValue(), jobParams(jobType));
		}
		return jobMap;
	}

	public HashMap<String, Object> fetchActiveJobsAndJobParamsByJobId(String jobId) {
		HashMap<String, Object> jobMap = new HashMap<String, Object>();
		SchedulerJobs job = schedulerJobsDao.fetchActiveJobsByJobId(jobId);
		logger.info("Fetching all active jobs");
		jobMap.put(Constants.ACTIVE_JOBS.getValue(), job);
		jobMap.put(Constants.JOB_PARAMS.getValue(), jobParams(job.getJobType().toString()));
		return jobMap;
	}

	public String createJob(String jobType, String jobTime, String jobFrequency, String jobDetails, String jobParams,
			User sessionUser) {
		try {
			if (schedulerJobsDao.checkExistingJob(jobType, jobTime, jobFrequency, jobParams)) {
				logger.info("Job of job type: " + jobType + " with job time: " + jobTime + " and frequency: "
						+ jobFrequency + " and job params: " + jobParams + " already exists.");
				return "Job already exists";
			} else {
				logger.info("New job being created by " + sessionUser.getUserType() + " with email id: "
						+ sessionUser.getEmailId());
				schedulerJobsDao.createJob(jobType, jobTime, jobFrequency, jobDetails, jobParams,
						sessionUser.getEmailId());
				logger.info("Job created successfully");
				return "Job created successfully";
			}
		} catch (Exception e) {
			logger.error("Exception caught while creating new job, " , e);
			return "Unable to create new job !";
		}
	}

	public String updateJob(String jobId, String jobTime, String jobFrequency, String jobDetails, String jobParams,
			User sessionUser) {
		try {
			logger.info("Updating job with job id :" + jobId + " by " + sessionUser.getUserType() + " with email id: "
					+ sessionUser.getEmailId());
			schedulerJobsDao.upadteJob(jobId, jobTime, jobFrequency, jobDetails, jobParams, sessionUser.getEmailId());
			logger.info("Job updated successfully");
			return "Job updated successfully";
		} catch (Exception e) {
			logger.error("Exception caught while updating job, " , e);
			return "Unable to update selected job";
		}
	}

	public String deleteJob(String jobId, User sessionUser) {
		try {
			logger.info("Deleting job with job id :" + jobId + " by " + sessionUser.getUserType() + " with email id: "
					+ sessionUser.getEmailId());
			schedulerJobsDao.deleteJob(jobId, sessionUser.getEmailId());
			logger.info("Job deleted successfully");
			return "Job deleted successfully";
		} catch (Exception e) {
			logger.error("Exception caught while deleting job, " , e);
			return "Unable to delete selected job";
		}
	}

	public String createJobParams(String precedingTime, String timeIntervalSlot, String acquirerType, String txnType,
			String paymentType, String payId, String status, String autoRefund) {
		logger.info("Creating job params");
		JSONObject jobParams = new JSONObject();
		if (StringUtils.isNotBlank(precedingTime)) {
			jobParams.put(Constants.PRECEDING_TIME.getValue(), precedingTime);
		}
		if (StringUtils.isNotBlank(timeIntervalSlot)) {
			jobParams.put(Constants.TIME_INTERVAL_SLOT.getValue(), timeIntervalSlot);
		}
		if (StringUtils.isNotBlank(acquirerType)) {
			jobParams.put(Constants.ACQUIRER_TYPE.getValue(), getJSONArray(acquirerType));
		}
		if (StringUtils.isNotBlank(txnType)) {
			jobParams.put(Constants.TXNTYPE.getValue(), getJSONArray(txnType));
		}
		if (StringUtils.isNotBlank(paymentType)) {
			jobParams.put(Constants.PAYMENT_TYPE1.getValue(), getJSONArray(paymentType));
		}
		if (StringUtils.isNotBlank(status)) {
			jobParams.put(Constants.STATUS.getValue(), getJSONArray(status));
		}
		if (StringUtils.isNotBlank(autoRefund)) {
			jobParams.put(Constants.AUTO_REFUND.getValue(), Boolean.valueOf(autoRefund));
		}
		if (StringUtils.isNotBlank(payId)) {
			if (payId.equalsIgnoreCase("ALL")) {
				List<Merchants> allActiveMerchants = userDao.getMerchantActiveList();
				StringBuilder allPayId = new StringBuilder();
				allPayId.append(",");
				for (Merchants merchant : allActiveMerchants) {
					allPayId.append(merchant.getPayId());
				}
				jobParams.put(Constants.PAYID.getValue(), getJSONArray(allPayId.toString().substring(0)));
			} else {
				jobParams.put(Constants.PAYID.getValue(), getJSONArray(payId));
			}
		}
		return jobParams.toString();
	}

	private List<String> jobParams(String jobType) {
		List<String> jobParam = new ArrayList<String>();
		if (jobType.equalsIgnoreCase(JobType.DAILY_CAPTURED_SMS.toString())
				|| jobType.equalsIgnoreCase(JobType.DAILY_MERCHANT_DATA_SMS.toString())
				|| jobType.equalsIgnoreCase(JobType.DAILY_PG_CAPTURED_DATA_SMS.toString())
				|| jobType.equalsIgnoreCase(JobType.DAILY_SETTLED_DATA_STATUS.toString())
				|| jobType.equalsIgnoreCase(JobType.DAILY_PG_CAPTURED_DATA_SMS.toString())) {
			jobParam.add(Constants.PAYID.getValue());
		} else if (jobType.equalsIgnoreCase(JobType.TRANSACTION_STATUS_ENQUIRY.toString())) {
			jobParam.add(Constants.PRECEDING_TIME.getValue());
			jobParam.add(Constants.TIME_INTERVAL_SLOT.getValue());
			jobParam.add(Constants.PAYID.getValue());
			jobParam.add(Constants.ACQUIRER_TYPE.getValue());
			jobParam.add(Constants.TXNTYPE.getValue());
			jobParam.add(Constants.PAYMENT_TYPE1.getValue());
			jobParam.add(Constants.STATUS.getValue());
			jobParam.add(Constants.AUTO_REFUND.getValue());
		} else {
			jobParam.addAll(new ArrayList());
		}
		return jobParam;
	}

	private JSONArray getJSONArray(String value) {
		String values[] = value.split(",");
		JSONArray valuesArray = new JSONArray();
		for (int i = 0; i < values.length; i++) {
			valuesArray.put(values[i]);
		}
		return valuesArray;
	}
}
