/**
 * Job to control all jobs
 */
package com.paymentgateway.scheduler.jobs;

import java.util.List;

import org.json.JSONObject;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.paymentgateway.commons.dao.SchedulerJobsDao;
import com.paymentgateway.commons.user.SchedulerJobs;
import com.paymentgateway.commons.util.JobType;
import com.paymentgateway.scheduler.commons.ConfigurationProvider;
import com.paymentgateway.scheduler.tasks.AcquirerStatusEnquiry;
import com.paymentgateway.scheduler.tasks.ChargebackFinalStatus;
import com.paymentgateway.scheduler.tasks.TransactionReportSmsJob;

/**
 * @author Amitosh Aanand
 *
 */
public class MasterJob extends QuartzJobBean {

	@Autowired
	private SchedulerJobsDao schedulerJobsDao;

	@Autowired
	private TransactionReportSmsJob transactionReportSmsJob;

	@Autowired
	private ChargebackFinalStatus chargebackFinalStatus;

	@Autowired
	private AcquirerStatusEnquiry acquirerStatusEnquiry;

	@Autowired
	private ConfigurationProvider configurationProvider;

	private static final Logger logger = LoggerFactory.getLogger(MasterJob.class);

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		runMaster();
	}

	private void runMaster() {
		List<SchedulerJobs> schedulerJobsList = schedulerJobsDao.fetchActiveJobsAtCurrentMoment();
		if (!schedulerJobsList.isEmpty()) {
			logger.info("Total number of jobs found : " + schedulerJobsList.size() + " at time the moment");
			rescheduleJobs(schedulerJobsList);
			runJobs(schedulerJobsList);
		}
	}

	private void rescheduleJobs(List<SchedulerJobs> schedulerJobsList) {
		for (SchedulerJobs job : schedulerJobsList) {
			schedulerJobsDao.rescheduleJob(job.getJobId());
		}
	}

	private void runJobs(List<SchedulerJobs> schedulerJobsList) {
		for (SchedulerJobs job : schedulerJobsList) {
			if (job.getJobType().equalsIgnoreCase(JobType.DAILY_CAPTURED_SMS.getCode())) {
				transactionReportSmsJob.invokeSMSService(JobType.DAILY_CAPTURED_SMS.getIdentity(),
						new JSONObject(job.getJobParams()), configurationProvider.getSmsApiUrl(), job);
			}
			if (job.getJobType().equalsIgnoreCase(JobType.DAILY_MERCHANT_DATA_SMS.getCode())) {
				transactionReportSmsJob.invokeSMSService(JobType.DAILY_MERCHANT_DATA_SMS.getIdentity(),
						new JSONObject(job.getJobParams()), configurationProvider.getSmsApiUrl(), job);
			}
			if (job.getJobType().equalsIgnoreCase(JobType.DAILY_SETTLED_DATA_STATUS.getCode())) {
				transactionReportSmsJob.invokeSMSService(JobType.DAILY_SETTLED_DATA_STATUS.getIdentity(),
						new JSONObject(job.getJobParams()), configurationProvider.getSmsApiUrl(), job);
			}
			if (job.getJobType().equalsIgnoreCase(JobType.DAILY_PG_CAPTURED_DATA_SMS.getCode())) {
				transactionReportSmsJob.invokeSMSService(JobType.DAILY_PG_CAPTURED_DATA_SMS.getIdentity(),
						new JSONObject(job.getJobParams()), configurationProvider.getSmsApiUrl(), job);
			}
			if (job.getJobType().equalsIgnoreCase(JobType.CHARGEBACK_FINAL_STATUS.getCode())) {
				chargebackFinalStatus.startChargebackStatusUpdater(job);
			}
			if (job.getJobType().equalsIgnoreCase(JobType.TRANSACTION_STATUS_ENQUIRY.getCode())) {
				acquirerStatusEnquiry.invokeStatusEnquiry(new JSONObject(job.getJobParams()),
						configurationProvider.getStatusEnquiryApiUrl(), job);
			}
		}
	}
}
