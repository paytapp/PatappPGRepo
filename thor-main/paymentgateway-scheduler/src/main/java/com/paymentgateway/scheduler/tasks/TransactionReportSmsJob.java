package com.paymentgateway.scheduler.tasks;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.user.SchedulerJobs;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.scheduler.commons.MaintainSchedulerLogs;
import com.paymentgateway.scheduler.core.ServiceControllerProvider;
import com.paymentgateway.scheduler.core.TaskManager;

@Service
public class TransactionReportSmsJob implements TaskManager {

	private static final Logger logger = LoggerFactory.getLogger(TransactionReportSmsJob.class);

	@Autowired
	private ServiceControllerProvider serviceControllerProvider;

	@Autowired
	private MaintainSchedulerLogs maintainSchedulerLogs;

	public void invokeSMSService(String messageType, JSONObject jobParam, String url, SchedulerJobs job) {
		try {
			logger.info("Started sending daily " + messageType + " SMS.");
			if (jobParam.has(Constants.PAYID.getValue())) {
				JSONArray payId = jobParam.getJSONArray(Constants.PAYID.getValue());
				for (int i = 0; i < payId.length(); i++) {
					MDC.put("mdcData", payId.getString(i));
					logger.info("Sending sms for merchant " + payId.getString(i));
					JSONObject requestObject = createJsonRequest(messageType, payId.getString(i));
					try {
						serviceControllerProvider.sendSms(requestObject, url);
						taskLogger(job, requestObject.toString(), null, true);
					} catch (Exception e) {
						logger.error("Error processing SMS ", e);
						taskLogger(job, requestObject.toString(), null, false);
					}
				}
			} else {
				logger.info("No data in job param to initiate sending " + messageType + " SMS");
			}
		} catch (Exception e) {
			logger.error("Error processing SMS", e);
			taskLogger(job, null, null, false);
		}
	}

	private JSONObject createJsonRequest(String messageType, String payId) {
		JSONObject value = new JSONObject();
		value.put(Constants.PAYID.getValue(), payId);
		value.put(Constants.RESPONSE_MESSAGE_TEXT.getValue(), messageType);
		return value;
	}

	@Override
	public void taskLogger(SchedulerJobs job, String request, String response, Boolean jobStatus) {
		maintainSchedulerLogs.taskLogger(job, request, response, jobStatus);
	}
}
