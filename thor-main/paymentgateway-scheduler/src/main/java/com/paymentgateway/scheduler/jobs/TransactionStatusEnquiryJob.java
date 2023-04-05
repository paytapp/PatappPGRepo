package com.paymentgateway.scheduler.jobs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.json.JSONObject;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.scheduler.commons.ConfigurationProvider;
import com.paymentgateway.scheduler.commons.PendingStatusEnquiryTransactions;
import com.paymentgateway.scheduler.commons.TransactionDataProvider;
import com.paymentgateway.scheduler.commons.TransactionStatusEnquiry;
import com.paymentgateway.scheduler.core.ServiceControllerProvider;

/**
 * @author Shaiwal
 *
 */

@SuppressWarnings("unused")
public class TransactionStatusEnquiryJob extends QuartzJobBean {

	@Autowired
	private ConfigurationProvider configurationProvider;

	@Autowired
	private TransactionDataProvider transactionDataProvider;

	@Autowired
	private ServiceControllerProvider serviceControllerProvider;

	private List<Document> transactionEnquirySet = new ArrayList<Document>();

	private static final Logger logger = LoggerFactory.getLogger(TransactionStatusEnquiryJob.class);

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		fetchAcquirerData();
	}

	private void fetchAcquirerData() {

		try {

			logger.info("Started fetching transaction data");
			Set<String> pgRefSet = transactionDataProvider.fetchTransactionData();
			String bankStatusEnquiryUrl = configurationProvider.getEnquiryApiUrl();
			String key = configurationProvider.getEnquiryApiKey();
			
			for (String pgRef : pgRefSet) {

				logger.info("Sending status enquiry request , URL = " + bankStatusEnquiryUrl + " 	pgRef == " + pgRef);

				JSONObject data = new JSONObject();
				data.put(FieldType.PG_REF_NUM.getName(), pgRef);
				data.put(FieldType.KEY_ID.getName(), key);
				
				serviceControllerProvider.bankStatusEnquiry(data, bankStatusEnquiryUrl);
			}

		}

		catch (Exception e) {
			logger.error("Exception in bank status enquiry from scheduler", e);
		}

	}
}