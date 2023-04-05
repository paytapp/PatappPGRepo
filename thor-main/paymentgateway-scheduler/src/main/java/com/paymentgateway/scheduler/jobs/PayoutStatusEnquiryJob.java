package com.paymentgateway.scheduler.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.paymentgateway.scheduler.commons.ImpsUpiDataProvider;

@SuppressWarnings("unused")
public class PayoutStatusEnquiryJob extends QuartzJobBean{

	@Autowired
	private ImpsUpiDataProvider impsUpiDataProvider;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		// TODO Auto-generated method stub

		payoutStatusEnquiry();
	}
	
	private void payoutStatusEnquiry() {
		impsUpiDataProvider.fetchPayoutData();
		
		}
}
