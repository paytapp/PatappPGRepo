/**
 * Job to control all jobs
 */
package com.paymentgateway.scheduler.jobs;

import java.text.MessageFormat;

import org.json.JSONObject;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.paymentgateway.commons.api.SmsSender;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.scheduler.core.ServiceControllerProvider;

/**
 * @author Vishal
 *
 */
public class HealthCheckUpPgUiAndPgWs extends QuartzJobBean {


	@Autowired
	private  MongoInstance mongoInstance;
	
	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private ServiceControllerProvider serviceControllerProvider;
	
	@Autowired
	private SmsSender smsSender;
	
	private static final String success ="200";
	private static final String fail ="400";
	private static final Logger logger = LoggerFactory.getLogger(HealthCheckUpPgUiAndPgWs.class);
	private static final String prefix = "MONGO_DB_";
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		healthCheckupPGUIAndPGWS();
	}

	private void healthCheckupPGUIAndPGWS() {
		try {
			String payId = propertiesManager.propertiesMap.get("HC_PAY_ID");
			MessageFormat mf = new MessageFormat(propertiesManager.propertiesMap.get("HC_PWS_PUI_MOB_MSG"));
		    String[] mobilesNumbers =propertiesManager.propertiesMap.get("HC_PWS_PUI_MOB_NO").split(",");  
			int mobileNumCount = mobilesNumbers.length;
			
			
			if(healthCheckupPGUI(payId)!=success) {
				String sendMsg = mf.format(new Object[] {"PGUI"}); 
			    for(int i=0; i<mobileNumCount; i++) {
			    	smsSender.healthCheckUpSMS(mobilesNumbers[i], sendMsg);
				}
			}
			if (healthCheckupPGWS(payId)!=success) {
				String sendMsg = mf.format(new Object[] {"PGWS"});
				for(int i=0; i<mobileNumCount; i++) {
					smsSender.healthCheckUpSMS(mobilesNumbers[i], sendMsg);
				}
			}
			
		}
		
		catch(Exception e) {
			logger.error("Exception in JOB for running HealthCheckupPGUIAndPGWS >>>> ",e);
		}
	}
	
	private String  healthCheckupPGUI(String payId) {
		String resp = fail;
		try {
			
				String url = PropertiesManager.propertiesMap.get("HC_PGUI_URL");
				JSONObject data=new JSONObject().put(FieldType.PAY_ID.getName(),payId);
				String response = serviceControllerProvider.checkHealthProvider(data, url);
				 if (response!=null &&  response.equalsIgnoreCase("200")) {
					 resp = success; 
				 }
						
			}catch(Exception e) {
			logger.error("Exception in JOB for running HealthCheckupPGUI  >>>> ",e);
			resp = fail;
		}
		return  resp;
	}
	
	private String  healthCheckupPGWS(String payId) {
		String resp = fail;
		try {
			String url = PropertiesManager.propertiesMap.get("HC_PGWS_URL");
			
			JSONObject data=new JSONObject().put(FieldType.PAY_ID.getName(),payId);
			//serviceControllerProvider.checkHealthProvider(data, url);
			String response = serviceControllerProvider.checkHealthProvider(data, url);
			if (response!=null &&  response.equalsIgnoreCase("200")) {
				 resp = success; 
			 }
		}
		catch(Exception e) {
			logger.error("Exception in JOB for running HealthCheckupPGWS  >>>> ",e);
			resp= fail;
		}
		return resp;
	}

	
	
	
}
