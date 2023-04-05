package com.paymentgateway.scheduler.jobs;

import java.util.List;

import org.bson.Document;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.paymentgateway.scheduler.commons.ConfigurationProvider;
import com.paymentgateway.scheduler.commons.ECollectionDataProvider;

/**
 * @author Pooja Pancholi
 *
 */

@SuppressWarnings("unused")
public class ECollectionDataJob extends QuartzJobBean{
	
	@Autowired
	private ECollectionDataProvider eCollectionDataProvider;
	
	@Autowired
	private ConfigurationProvider configurationProvider;
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		// TODO Auto-generated method stub

		fetchECollectionData();
	}
	
	private void fetchECollectionData() {
	List<Document> closingdata =  eCollectionDataProvider.fetchECollectionData();
	
	}

			
}
