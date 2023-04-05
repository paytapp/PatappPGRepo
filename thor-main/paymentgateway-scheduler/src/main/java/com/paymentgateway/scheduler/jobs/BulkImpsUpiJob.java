package com.paymentgateway.scheduler.jobs;

import java.util.List;

import org.bson.Document;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.paymentgateway.scheduler.commons.ImpsUpiDataProvider;

/**
 * @author Pooja Pancholi
 *
 */

@SuppressWarnings("unused")
public class BulkImpsUpiJob extends QuartzJobBean{
	
	@Autowired
	private ImpsUpiDataProvider impsUpiDataProvider;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		// TODO Auto-generated method stub

		fetchImpsUpiDataForTransaction();
	}
	
	private void fetchImpsUpiDataForTransaction() {
		impsUpiDataProvider.fetchImpsUpiData();
		
		}
}
