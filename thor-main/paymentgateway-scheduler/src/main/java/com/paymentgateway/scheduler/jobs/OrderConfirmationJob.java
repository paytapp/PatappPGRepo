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
public class OrderConfirmationJob extends QuartzJobBean {

	@Autowired
	private ConfigurationProvider configurationProvider;

	@Autowired
	private TransactionDataProvider transactionDataProvider;

	@Autowired
	private ServiceControllerProvider serviceControllerProvider;

	private List<Document> transactionEnquirySet = new ArrayList<Document>();

	private static final Logger logger = LoggerFactory.getLogger(OrderConfirmationJob.class);

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		fetchAcquirerData();
	}

	private void fetchAcquirerData() {

		try {

			logger.info("Started fetching captured txn for order confirmation");
			Set<String> orderIdSet = transactionDataProvider.fetchTransactionCapturedData();
			String orderConfirmUrl = configurationProvider.getOrderConfirmApiUrl();

			for (String orderId : orderIdSet) {

				logger.info("Sending order confirm request , URL = " + orderConfirmUrl + " 	orderId == " + orderId);

				JSONObject data = new JSONObject();
				data.put(FieldType.ORDER_ID.getName(), orderId);
				serviceControllerProvider.bankStatusEnquiry(data, orderConfirmUrl);
			}

		}

		catch (Exception e) {
			logger.error("Exception in order confirm api from scheduler", e);
		}

	}
}