package com.paymentgateway.scheduler.core;

import com.paymentgateway.commons.user.SchedulerJobs;

/**
 * @author Amitosh Aanand
 *
 */
public interface TaskManager {

	public void taskLogger(SchedulerJobs job, String request, String response, Boolean jobStatus);
}
