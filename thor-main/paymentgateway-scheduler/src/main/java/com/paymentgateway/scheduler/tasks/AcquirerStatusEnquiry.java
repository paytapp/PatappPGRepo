package com.paymentgateway.scheduler.tasks;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.user.SchedulerJobs;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.scheduler.commons.MaintainSchedulerLogs;
import com.paymentgateway.scheduler.commons.StatusEnquiryDataProvider;
import com.paymentgateway.scheduler.commons.UpdateRefundTransactions;
import com.paymentgateway.scheduler.core.ServiceControllerProvider;
import com.paymentgateway.scheduler.core.TaskManager;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class AcquirerStatusEnquiry implements TaskManager {

	@Autowired
	private StatusEnquiryDataProvider statusEnquiryDataProvider;

	@Autowired
	private ServiceControllerProvider serviceControllerProvider;

	@Autowired
	private UpdateRefundTransactions updateRefundTransactions;

	@Autowired
	private MaintainSchedulerLogs maintainSchedulerLogs;

	private static final Logger logger = LoggerFactory.getLogger(AcquirerStatusEnquiry.class);

	@SuppressWarnings("deprecation")
	public void invokeStatusEnquiry(JSONObject jobParam, String url, SchedulerJobs job) {
		try {
			logger.info("Started fetching transaction data for status enquiry");
			List<Document> statusEnquirydata = new ArrayList<Document>();
			LocalDateTime endTime1 = LocalDateTime.now()
					.minusMinutes(Long.parseLong(jobParam.getString(Constants.PRECEDING_TIME.getValue())));
			LocalDateTime startTime1 = endTime1
					.minusMinutes(Long.parseLong(jobParam.getString(Constants.TIME_INTERVAL_SLOT.getValue())));

			String startTime = startTime1.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			String endTime = endTime1.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			logger.info("Time interval calculated, Start time " + startTime + " and End time " + endTime);

			JSONArray merchantPayIdArray = jobParam.getJSONArray(Constants.PAYID.getValue().toString());
			JSONArray acquirerTypeArray = jobParam.getJSONArray(Constants.ACQUIRER_TYPE.getValue());
			JSONArray transactionTypeArray = jobParam.getJSONArray(Constants.TXNTYPE.getValue());
			JSONArray paymentTypeArray = jobParam.getJSONArray(Constants.PAYMENT_TYPE1.getValue());
			JSONArray statusTypeArray = jobParam.getJSONArray(Constants.STATUS.getValue());

			for (int a = 0; a < merchantPayIdArray.length(); a++) {
				for (int b = 0; b < acquirerTypeArray.length(); b++) {
					for (int c = 0; c < transactionTypeArray.length(); c++) {
						for (int d = 0; d < paymentTypeArray.length(); d++) {
							statusEnquirydata.addAll(statusEnquiryDataProvider.fetchData(startTime, endTime,
									merchantPayIdArray.getString(a), acquirerTypeArray.getString(b),
									transactionTypeArray.getString(c), paymentTypeArray.getString(d), statusTypeArray));
						}
					}
				}
			}

			if (!statusEnquirydata.isEmpty()) {
				List<Document> finalStatusList = new ArrayList<Document>();
				List<Document> autoRefundableList = new ArrayList<Document>();
				for (Document statusEnquiryDataRaw : statusEnquirydata) {
					statusEnquiryDataRaw.remove("INSERTION_DATE");
					JsonWriterSettings writerSettings = new JsonWriterSettings(JsonMode.STRICT, true);
					JSONObject enquiryTransaction = new JSONObject(statusEnquiryDataRaw.toJson(writerSettings));
					try {
						String response = serviceControllerProvider.makeStatusEnquiry(enquiryTransaction, url);
						if (StringUtils.isNotBlank(response)) {
							JSONObject jsonResponse = new JSONObject(response);
							if (jsonResponse.has("STATUS") && (jsonResponse.getString("STATUS").equalsIgnoreCase(
									"Declined") || jsonResponse.getString("STATUS").equalsIgnoreCase("Rejected")
									|| jsonResponse.getString("STATUS").equalsIgnoreCase("Error")
									|| jsonResponse.getString("STATUS").equalsIgnoreCase("Denied")
									|| jsonResponse.getString("STATUS").equalsIgnoreCase("Cancelled")
									|| jsonResponse.getString("STATUS").equalsIgnoreCase("Denied by risk")
									|| jsonResponse.getString("STATUS").equalsIgnoreCase("Duplicate")
									|| jsonResponse.getString("STATUS").equalsIgnoreCase("Failed")
									|| jsonResponse.getString("STATUS").equalsIgnoreCase("Invalid")
									|| jsonResponse.getString("STATUS").equalsIgnoreCase("Authentication Failed")
									|| jsonResponse.getString("STATUS").equalsIgnoreCase("Denied due to fraud")
									|| jsonResponse.getString("STATUS").equalsIgnoreCase("Failed at Acquirer")
									|| jsonResponse.getString("STATUS").equalsIgnoreCase("Timed out at Acquirer"))) {
								jsonResponse.put("_id", jsonResponse.get(FieldType.TXN_ID.getName()));
								finalStatusList.add(Document.parse(jsonResponse.toString()));
							}
							if (jsonResponse.has("STATUS") 
									&& (jsonResponse.getString("STATUS").equalsIgnoreCase("Captured")
									&& (!enquiryTransaction.getString("STATUS").equalsIgnoreCase("Captured")))) {
								jsonResponse.put("_id", jsonResponse.get(FieldType.TXN_ID.getName()));
								finalStatusList.add(Document.parse(jsonResponse.toString()));
								if (jobParam.has(Constants.AUTO_REFUND.getValue())) {
									if (jobParam.getBoolean(Constants.AUTO_REFUND.getValue())) {
										autoRefundableList.add(Document.parse(jsonResponse.toString()));
									}
								}
							}
							logger.info("Response: " + response);
							taskLogger(job, enquiryTransaction.toString(), response, true);
						} else {
							taskLogger(job, enquiryTransaction.toString(), response, false);
							logger.info("Null response received from PG-WS");
						}
					} catch (Exception e) {
						logger.error("Error while performing status enquiry " , e);
						taskLogger(job, enquiryTransaction.toString(), null, false);
					}
				}
				updateRefundTransactions.updateAndRefund(finalStatusList, autoRefundableList);
				finalStatusList.clear();
				autoRefundableList.clear();
			} else {
				logger.info("No data found for status enquiry");
				taskLogger(job, null, null, false);
			}
		} catch (Exception e) {
			logger.error("Error while performing status enquiry " , e);
			taskLogger(job, null, null, false);
		}
	}

	@Override
	public void taskLogger(SchedulerJobs job, String request, String response, Boolean jobStatus) {
		maintainSchedulerLogs.taskLogger(job, request, response, jobStatus);
	}
}