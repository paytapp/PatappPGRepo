/*package com.paymentgateway.crm.session;

import java.util.Timer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;

import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.PropertiesManager;

*//**
 * @author Neeraj
 *//*

public class TaskSchedulerListener implements ServletContextListener {	
	private static Logger logger = LoggerFactory.getLogger(TaskSchedulerListener.class.getName());
	public static boolean postBackFlag;
	@Autowired
	private PropertiesManager propertiesManager;
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		
		String flag = ConfigurationConstants.SEND_POSTBACK_FLAG.getValue();
		if(flag.equals(Constants.TRUE.getValue())){
			postBackFlag = true;
		}
		if (propertiesManager.getSystemProperty(CrmFieldConstants.RUN_SCHEDULAR_FALG.getValue()).equals(Constants.TRUE.getValue())) {
			logger.info("Schedular started");
			TaskSchedular task = new TaskSchedular(); TODO
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(task,Long.valueOf(ConfigurationConstants.TASK_SCHEDULAR_RUNNING_DELAY.getValue()),
					Long.valueOf(ConfigurationConstants.TASK_SCHEDULAR_RUNNING_DELAY.getValue()));

			//quartz job for recurring payments
			try{  TODO
				JobDetail recurringPaymentJob = JobBuilder.newJob(RecurringPaymentsJob.class).withIdentity(Constants.RECURRING_PAYMENT_JOB.getValue(), Constants.CITRUSPAY.getValue()).build();
				Trigger recurringPaymentJobTrigger = TriggerBuilder.newTrigger().withIdentity(Constants.RECURRING_PAYMENT_JOB_TRIGGER.getValue(),Constants.CITRUSPAY_TRIGGER.getValue()).
																withSchedule(CronScheduleBuilder.cronSchedule(propertiesManager.getSystemProperty(Constants.DAILY_CRON_STRING.getValue()))).build();
				Scheduler scheduler = new StdSchedulerFactory().getScheduler();
				scheduler.start();
				scheduler.scheduleJob(recurringPaymentJob, recurringPaymentJobTrigger);
			}catch(Exception exception){
				logger.error("Error running schedular for recurring payments " + exception);
			}
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("Schedular end");
	}
}*/