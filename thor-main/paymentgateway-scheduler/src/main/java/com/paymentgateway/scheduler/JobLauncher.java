package com.paymentgateway.scheduler;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.scheduler.commons.ConfigurationProvider;
import com.paymentgateway.scheduler.jobs.AutoDeleteHistoricDataFoldersJob;
import com.paymentgateway.scheduler.jobs.AutoRefundJob;
import com.paymentgateway.scheduler.jobs.BulkImpsUpiJob;
import com.paymentgateway.scheduler.jobs.ClosingAmountUpdateJob;
import com.paymentgateway.scheduler.jobs.CryptoStatusEnquiry;
import com.paymentgateway.scheduler.jobs.DebitTransactionNotificationScheduleJob;
import com.paymentgateway.scheduler.jobs.DebitTransactionScheduleJob;
import com.paymentgateway.scheduler.jobs.DebitTransactionStatusEnquiryJob;
import com.paymentgateway.scheduler.jobs.HealthCheckUpPgUiAndPgWs;
import com.paymentgateway.scheduler.jobs.MasterJob;
import com.paymentgateway.scheduler.jobs.OrderConfirmationJob;
import com.paymentgateway.scheduler.jobs.PayoutStatusEnquiryJob;
import com.paymentgateway.scheduler.jobs.RetryCallbackJob;
import com.paymentgateway.scheduler.jobs.TransactionStatusEnquiryForCapturedJob;
import com.paymentgateway.scheduler.jobs.TransactionStatusEnquiryJob;
import com.paymentgateway.scheduler.jobs.UpiAutoPayDebitTransactionScheduleJob;
import com.paymentgateway.scheduler.jobs.UpiAutoPayDebitTransactionStatusEnquiryCriteriaScheduleJob;

@Service
public class JobLauncher {

	@Autowired
	private ConfigurationProvider configurationProvider;

	@Bean
	public JobDetail masterJob() {
		if (configurationProvider.getMasterJobStatus().equalsIgnoreCase("1")) {
			return JobBuilder.newJob(MasterJob.class).withIdentity("masterJob")
					.usingJobData(Constants.RESPONSE_MESSAGE_TEXT.getValue(), "masterJob").storeDurably().build();
		} else {
			return null;
		}
	}

	@Bean
	public Trigger masterJobTrigger() {
		if (configurationProvider.getMasterJobStatus().equalsIgnoreCase("1")) {
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
					.cronSchedule(configurationProvider.getMasterJobCron());
			return TriggerBuilder.newTrigger().withSchedule(scheduleBuilder).forJob(masterJob())
					.withIdentity("masterJob").build();
		} else {
			return null;
		}
	}

	// Pending Transaction Scheduler

	/*
	 * @Bean public JobDetail pendingTxnJob() { return
	 * JobBuilder.newJob(PendingBookingUpdateJob.class).withIdentity(
	 * "pendingUpdaterJob")
	 * .usingJobData(Constants.RESPONSE_MESSAGE_TEXT.getValue(),
	 * "pendingUpdaterJob").storeDurably().build(); }
	 * 
	 * @Bean public Trigger pendingTxnJobTrigger() { CronScheduleBuilder
	 * scheduleBuilder = CronScheduleBuilder
	 * .cronSchedule(configurationProvider.getPendingBookingJobCron()); return
	 * TriggerBuilder.newTrigger().withSchedule(scheduleBuilder).forJob(
	 * pendingTxnJob()) .withIdentity("pendingUpdaterJob").build(); }
	 */

	// transactionStatusEnquiryJobDetails Used for getting all pending
	// transaction
	// from ipaytransaction collection
	// for status enquiry
	@Bean
	public JobDetail transactionStatusEnquiryJobDetails() {
		if (configurationProvider.getTransactionStatusEnquiry().equalsIgnoreCase("1")) {
			return JobBuilder.newJob(TransactionStatusEnquiryJob.class).withIdentity("statusEnquiry")
					.usingJobData(Constants.RESPONSE_MESSAGE_TEXT.getValue(), "transactionStatusEnquiry").storeDurably()
					.build();
		} else {
			return null;
		}

	}

	@Bean
	public Trigger transactionStatusEnquiryJobTrigger() {

		if (configurationProvider.getTransactionStatusEnquiry().equalsIgnoreCase("1")) {

			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
					.cronSchedule(configurationProvider.getTransactionBankStatusEnquiryCron());

			return TriggerBuilder.newTrigger().withSchedule(scheduleBuilder)
					.forJob(transactionStatusEnquiryJobDetails()).withIdentity("statusEnquiryJobTrigger").build();
		} else {
			return null;
		}

	}

	@Bean
	public JobDetail transactionStatusEnquiryForCapturedJobDetails() {
		if (configurationProvider.getTransactionStatusEnquiry().equalsIgnoreCase("1")) {
			return JobBuilder.newJob(TransactionStatusEnquiryForCapturedJob.class)
					.withIdentity("statusEnquiryCapturedTxn")
					.usingJobData(Constants.RESPONSE_MESSAGE_TEXT.getValue(), "capturedTransactionStatusEnquiry")
					.storeDurably().build();
		} else {
			return null;
		}

	}

	@Bean
	public Trigger transactionStatusEnquiryForCapturedJobTrigger() {

		if (configurationProvider.getTransactionStatusEnquiry().equalsIgnoreCase("1")) {

			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
					.cronSchedule(configurationProvider.getTransactionBankStatusEnquiryCron());

			return TriggerBuilder.newTrigger().withSchedule(scheduleBuilder)
					.forJob(transactionStatusEnquiryForCapturedJobDetails()).withIdentity("statusEnquiryCapturedTxn")
					.build();
		} else {
			return null;
		}

	}
	
	@Bean
	public JobDetail pendingTransactionsStatusUpdateJob() {
		if (configurationProvider.getPendingTransactionsStatusUpdate().equalsIgnoreCase("1")) {
			return JobBuilder.newJob(PendingTransactionStatusUpdateJob.class)
					.withIdentity("pendingTransactionsStatusUpdateJob")
					.usingJobData(Constants.RESPONSE_MESSAGE_TEXT.getValue(), "pendingTransactionsStatusUpdateJob")
					.storeDurably().build();
		} else {
			return null;
		}

	}

	@Bean
	public Trigger pendingtransactionsStatusUpdateJobTrigger() {
		if (configurationProvider.getPendingTransactionsStatusUpdate().equalsIgnoreCase("1")) {
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
					.cronSchedule(configurationProvider.getPendingTransactionsStatusUpdateCron());
			return TriggerBuilder.newTrigger().withSchedule(scheduleBuilder)
					.forJob(pendingTransactionsStatusUpdateJob())
					.withIdentity("pendingTransactionsStatusUpdateJob").build();
		} else {
			return null;
		}
	}

	// orderConfirmation JOb Details Used for getting order confirmation
	@Bean
	public JobDetail orderConfirmationJobDetails() {
		if (configurationProvider.getOrderConfirmation().equalsIgnoreCase("1")) {
			return JobBuilder.newJob(OrderConfirmationJob.class).withIdentity("orderConfirmation")
					.usingJobData(Constants.RESPONSE_MESSAGE_TEXT.getValue(), "orderConfirmationEnquiry").storeDurably()
					.build();
		} else {
			return null;
		}

	}

	@Bean
	public Trigger orderConfirmationJobTrigger() {

		if (configurationProvider.getOrderConfirmation().equalsIgnoreCase("1")) {

			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
					.cronSchedule(configurationProvider.getOrderConfirmationCron());

			return TriggerBuilder.newTrigger().withSchedule(scheduleBuilder).forJob(orderConfirmationJobDetails())
					.withIdentity("orderConfirmationJobTrigger").build();
		} else {
			return null;
		}

	}

	/*
	 * // ECollection Data
	 * 
	 * @Bean public JobDetail eCollectionJobDetails() { if
	 * (configurationProvider.geteCollectionData().equalsIgnoreCase("1")) { return
	 * JobBuilder.newJob(ECollectionDataJob.class).withIdentity( "eCollectionData")
	 * .usingJobData(Constants.RESPONSE_MESSAGE_TEXT.getValue(),
	 * "eCollectionDataEnquiry").storeDurably() .build(); } else { return null; }
	 * 
	 * }
	 * 
	 * 
	 * 
	 * 
	 * @Bean public Trigger eCollectionJobTrigger() {
	 * 
	 * if (configurationProvider.geteCollectionData().equalsIgnoreCase("1")) {
	 * 
	 * CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
	 * .cronSchedule(configurationProvider.geteColletionCron());
	 * 
	 * return TriggerBuilder.newTrigger().withSchedule(scheduleBuilder)
	 * .forJob(eCollectionJobDetails()).withIdentity("eCollectionDataJobTrigger" ).
	 * build(); } else { return null; }
	 * 
	 * }
	 */

	// Bulk for IMPS and UPI
	@Bean
	public JobDetail bulkImpsUpiJobDetails() {
		if (configurationProvider.getBulkImpsUpiData().equalsIgnoreCase("1")) {
			return JobBuilder.newJob(BulkImpsUpiJob.class).withIdentity("BulkImpsUpiJob")
					.usingJobData(Constants.RESPONSE_MESSAGE_TEXT.getValue(), "BulkImpsUpiJobEnquiry").storeDurably()
					.build();
		} else {
			return null;
		}

	}

	@Bean
	public Trigger bulkImpsUpiJobTrigger() {

		if (configurationProvider.getBulkImpsUpiData().equalsIgnoreCase("1")) {

			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
					.cronSchedule(configurationProvider.getImpsUpiCron());

			return TriggerBuilder.newTrigger().withSchedule(scheduleBuilder).forJob(bulkImpsUpiJobDetails())
					.withIdentity("bulkImpsUpiJobTrigger").build();
		} else {
			return null;
		}

	}

	// closingAmountUpdateJobDetails Used for updating closing amount for all
	// merchants
	@Bean
	public JobDetail closingAmountUpdateJobDetails() {
		if (configurationProvider.getClosingAmount().equalsIgnoreCase("1")) {
			return JobBuilder.newJob(ClosingAmountUpdateJob.class).withIdentity("closingAmount")
					.usingJobData(Constants.RESPONSE_MESSAGE_TEXT.getValue(), "ClosingAmountUpdate").storeDurably()
					.build();
		} else {
			return null;
		}

	}

	@Bean
	public Trigger closingAmountUpdateJobTrigger() {

		if (configurationProvider.getClosingAmount().equalsIgnoreCase("1")) {

			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
					.cronSchedule(configurationProvider.getClosingAmountCron());

			return TriggerBuilder.newTrigger().withSchedule(scheduleBuilder).forJob(closingAmountUpdateJobDetails())
					.withIdentity("closingAmountJobTrigger").build();
		} else {
			return null;
		}

	}

	// Status enquiry for Payout
	@Bean
	public JobDetail payoutStatusEnquiryJobDetails() {
		if (configurationProvider.getPayoutStatusEnquiry().equalsIgnoreCase("1")) {
			return JobBuilder.newJob(PayoutStatusEnquiryJob.class).withIdentity("payoutStatusEnquiry")
					.usingJobData(Constants.RESPONSE_MESSAGE_TEXT.getValue(), "PayoutStatusEnquiry").storeDurably()
					.build();
		} else {
			return null;
		}

	}

	@Bean
	public Trigger payoutStatusEnquiryJobTrigger() {

		if (configurationProvider.getPayoutStatusEnquiry().equalsIgnoreCase("1")) {

			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
					.cronSchedule(configurationProvider.getPayoutStatusEnquiryCron());

			return TriggerBuilder.newTrigger().withSchedule(scheduleBuilder).forJob(payoutStatusEnquiryJobDetails())
					.withIdentity("payoutStatusEnquiryJobTrigger").build();
		} else {
			return null;
		}

	}

	/*
	 * // Status enquiry for Payout Capture Txn
	 * 
	 * @Bean public JobDetail payoutStatusEnquiryForCaptureJobDetails() { if
	 * (configurationProvider.getPayoutStatusEnquiry().equalsIgnoreCase("1")) {
	 * return JobBuilder.newJob(PayoutCapturedTxnStatusEnq.class).withIdentity(
	 * "payoutCapturedStatusEnquiry")
	 * .usingJobData(Constants.RESPONSE_MESSAGE_TEXT.getValue(),
	 * "payoutCapturedStatusEnquiry") .storeDurably().build(); } else { return null;
	 * }
	 * 
	 * }
	 * 
	 * @Bean public Trigger payoutStatusEnquiryForCaptureJobTrigger() {
	 * 
	 * if (configurationProvider.getPayoutStatusEnquiry().equalsIgnoreCase("1")) {
	 * 
	 * CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
	 * .cronSchedule(configurationProvider.getPayoutStatusEnquiryCron());
	 * 
	 * return TriggerBuilder.newTrigger().withSchedule(scheduleBuilder)
	 * .forJob(payoutStatusEnquiryForCaptureJobDetails())
	 * .withIdentity("payoutCapturedTxnStatusEnquiryJobTrigger").build(); } else {
	 * return null; }
	 * 
	 * }
	 */

	// Debit Transaction Schedule JOb Details Used for Initiate debit from
	// account
	@Bean
	public JobDetail scheduleTransactionJobDetails() {

		if (configurationProvider.getTransactionSchedule().equalsIgnoreCase("1")) {
			return JobBuilder.newJob(DebitTransactionScheduleJob.class).withIdentity("transactionSchedule")
					.usingJobData(Constants.RESPONSE_MESSAGE_TEXT.getValue(), "scheduleTransactionJobDetails")
					.storeDurably().build();
		} else {
			return null;
		}
	}

	@Bean
	public Trigger scheduleTransactionJobTrigger() {

		if (configurationProvider.getTransactionSchedule().equalsIgnoreCase("1")) {
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
					.cronSchedule(configurationProvider.getTransactionScheduleCron());

			return TriggerBuilder.newTrigger().withSchedule(scheduleBuilder).forJob(scheduleTransactionJobDetails())
					.withIdentity("scheduleTransactionJobTrigger").build();
		} else {
			return null;
		}
	}

	// Status Enquiry For Debit Transaction
	@Bean
	public JobDetail statusEnquiryScheduleTransactionJobDetails() {

		if (configurationProvider.getStatusEnquiryTransactionSchedule().equalsIgnoreCase("1")) {
			return JobBuilder.newJob(DebitTransactionStatusEnquiryJob.class)
					.withIdentity("statusEnquiryScheduleTransaction")
					.usingJobData(Constants.RESPONSE_MESSAGE_TEXT.getValue(),
							"statusEnquiryScheduleTransactionJobDetails")
					.storeDurably().build();
		} else {
			return null;
		}
	}

	@Bean
	public Trigger statusEnquiryScheduleTransactionJobTrigger() {

		if (configurationProvider.getStatusEnquiryTransactionSchedule().equalsIgnoreCase("1")) {
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
					.cronSchedule(configurationProvider.getStatusEnquiryTransactionScheduleCron());

			return TriggerBuilder.newTrigger().withSchedule(scheduleBuilder)
					.forJob(statusEnquiryScheduleTransactionJobDetails())
					.withIdentity("statusEnquiryScheduleTransactionJobTrigger").build();
		} else {
			return null;
		}
	}

	// Upi AutoPay Debit Transaction Notification Job Details
	@Bean
	public JobDetail transactionNotificationJobDetails() {

		if (configurationProvider.getTransactionNotificationSchedule().equalsIgnoreCase("1")) {
			return JobBuilder.newJob(DebitTransactionNotificationScheduleJob.class)
					.withIdentity("transactionNotificationSchedule")
					.usingJobData(Constants.RESPONSE_MESSAGE_TEXT.getValue(), "transactionNotificationJobDetails")
					.storeDurably().build();
		} else {
			return null;
		}
	}

	@Bean
	public Trigger transactionNotificationJobTrigger() {

		if (configurationProvider.getTransactionNotificationSchedule().equalsIgnoreCase("1")) {
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
					.cronSchedule(configurationProvider.getTransactionNotificationScheduleCron());

			return TriggerBuilder.newTrigger().withSchedule(scheduleBuilder).forJob(transactionNotificationJobDetails())
					.withIdentity("transactionNotificationJobTrigger").build();
		} else {
			return null;
		}
	}

	// Upi AutoPay Debit Transaction Job Details
	@Bean
	public JobDetail upiAutoPayDebitTransactionJobDetails() {

		if (configurationProvider.getUpiAutoPayDebitTransactionSchedule().equalsIgnoreCase("1")) {
			return JobBuilder.newJob(UpiAutoPayDebitTransactionScheduleJob.class)
					.withIdentity("upiAutoPayDebitTransactionJobDetails")
					.usingJobData(Constants.RESPONSE_MESSAGE_TEXT.getValue(), "upiAutoPayDebitTransactionJobDetails")
					.storeDurably().build();
		} else {
			return null;
		}
	}

	@Bean
	public Trigger upiAutoPayDebitTransactionJobTrigger() {

		if (configurationProvider.getUpiAutoPayDebitTransactionSchedule().equalsIgnoreCase("1")) {
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
					.cronSchedule(configurationProvider.getUpiAutoPayDebitTransactionScheduleCron());

			return TriggerBuilder.newTrigger().withSchedule(scheduleBuilder)
					.forJob(upiAutoPayDebitTransactionJobDetails()).withIdentity("upiAutoPayDebitTransactionJobTrigger")
					.build();
		} else {
			return null;
		}
	}

	// Upi AutoPay Debit Transaction Status Enquiry Criteria Job Details
	@Bean
	public JobDetail upiAutoPayDebitTransactionStatusEnquiryCriteriaJobDetails() {

		if (configurationProvider.getUpiAutoPayStatusEnquiryCriteriaSchedule().equalsIgnoreCase("1")) {
			return JobBuilder.newJob(UpiAutoPayDebitTransactionStatusEnquiryCriteriaScheduleJob.class)
					.withIdentity("upiAutoPayDebitTransactionStatusEnquiryCriteriaJobDetails")
					.usingJobData(Constants.RESPONSE_MESSAGE_TEXT.getValue(),
							"upiAutoPayDebitTransactionStatusEnquiryCriteriaJobDetails")
					.storeDurably().build();
		} else {
			return null;
		}
	}

	@Bean
	public Trigger upiAutoPayDebitTransactionStatusEnquiryCriteriaJobTrigger() {

		if (configurationProvider.getUpiAutoPayStatusEnquiryCriteriaSchedule().equalsIgnoreCase("1")) {
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
					.cronSchedule(configurationProvider.getUpiAutoPayStatusEnquiryCriteriaScheduleCron());

			return TriggerBuilder.newTrigger().withSchedule(scheduleBuilder)
					.forJob(upiAutoPayDebitTransactionStatusEnquiryCriteriaJobDetails())
					.withIdentity("upiAutoPayDebitTransactionStatusEnquiryCriteriaJobTrigger").build();
		} else {
			return null;
		}
	}

	// retry callback job
	@Bean
	public JobDetail retryCallbackJobDetails() {
		if (configurationProvider.getRetryCallback().equalsIgnoreCase("1")) {
			return JobBuilder.newJob(RetryCallbackJob.class).withIdentity("retryCallback")
					.usingJobData(Constants.RESPONSE_MESSAGE_TEXT.getValue(), "retryCallback").storeDurably().build();
		} else {
			return null;
		}

	}

	@Bean
	public Trigger retryCallbackJobTrigger() {

		if (configurationProvider.getRetryCallback().equalsIgnoreCase("1")) {

			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
					.cronSchedule(configurationProvider.getRetryCallbackCron());

			return TriggerBuilder.newTrigger().withSchedule(scheduleBuilder).forJob(retryCallbackJobDetails())
					.withIdentity("retryCallbackJobTrigger").build();
		} else {
			return null;
		}

	}

	@Bean
	public JobDetail autoRefundJobDetails() {
		if (configurationProvider.getAutoRefund().equalsIgnoreCase("1")) {
			return JobBuilder.newJob(AutoRefundJob.class).withIdentity("autoRefund")
					.usingJobData(Constants.RESPONSE_MESSAGE_TEXT.getValue(), "autoRefund").storeDurably().build();
		} else {
			return null;
		}

	}

	@Bean
	public Trigger autoRefundJobTrigger() {

		if (configurationProvider.getAutoRefund().equalsIgnoreCase("1")) {

			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
					.cronSchedule(configurationProvider.getAutoRefundCron());

			return TriggerBuilder.newTrigger().withSchedule(scheduleBuilder).forJob(autoRefundJobDetails())
					.withIdentity("autoRefundJobTrigger").build();
		} else {
			return null;
		}

	}

	@Bean
	public JobDetail autoDeleteHistoricDataFoldersJobDetails() {
		if (configurationProvider.getAutoDeleteHistoricDataFolders().equalsIgnoreCase("1")) {
			return JobBuilder.newJob(AutoDeleteHistoricDataFoldersJob.class).withIdentity("deleteHistoricDataFolder")
					.usingJobData(Constants.RESPONSE_MESSAGE_TEXT.getValue(), "deleteHistoricDataFolder").storeDurably()
					.build();
		} else {
			return null;
		}

	}

	@Bean
	public Trigger autoDeleteHistoricDataFoldersJobTrigger() {
		if (configurationProvider.getAutoDeleteHistoricDataFolders().equalsIgnoreCase("1")) {
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
					.cronSchedule(configurationProvider.getAutoDeleteHistoricDataFoldersCron());
			return TriggerBuilder.newTrigger().withSchedule(scheduleBuilder)
					.forJob(autoDeleteHistoricDataFoldersJobDetails()).withIdentity("autoDeleteHistoricDataFolder")
					.build();
		} else {
			return null;
		}

	}

	@Bean
	public JobDetail healthCheckupPgUiAndPgWs() {
		if (configurationProvider.getHealthCheckUp().equalsIgnoreCase("1")) {
			return JobBuilder.newJob(HealthCheckUpPgUiAndPgWs.class).withIdentity("HealthCheckUpPgUiAndPgWs")
					.usingJobData(Constants.RESPONSE_MESSAGE_TEXT.getValue(), "HealthCheckUpPgUiAndPgWs").storeDurably()
					.build();
		} else {
			return null;
		}

	}

	@Bean
	public Trigger healthCheckupJobTrigger() {
		if (configurationProvider.getHealthCheckUp().equalsIgnoreCase("1")) {
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
					.cronSchedule(configurationProvider.getHealthCheckUpCron());
			return TriggerBuilder.newTrigger().withSchedule(scheduleBuilder).forJob(healthCheckupPgUiAndPgWs())
					.withIdentity("healthCheckupPgUiAndPgWs").build();
		} else {
			return null;
		}

	}

	@Bean
	public JobDetail cryptoStatusEnquiryCheck() {
		if (configurationProvider.getCryptoStatusCheck().equalsIgnoreCase("1")) {
			return JobBuilder.newJob(CryptoStatusEnquiry.class).withIdentity("CryptoStatusEnquiryCheck")
					.usingJobData(Constants.RESPONSE_MESSAGE_TEXT.getValue(), "CryptoStatusEnquiryCheck").storeDurably()
					.build();
		} else {
			return null;
		}

	}

	@Bean
	public Trigger cryptoStatusCheck() {
		if (configurationProvider.getCryptoStatusCheck().equalsIgnoreCase("1")) {
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
					.cronSchedule(configurationProvider.getCryptoStatusCheckCron());
			return TriggerBuilder.newTrigger().withSchedule(scheduleBuilder).forJob(cryptoStatusEnquiryCheck())
					.withIdentity("cryptoStatusEnquiryCheck").build();
		} else {
			return null;
		}

	}

}
