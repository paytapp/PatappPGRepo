package com.paymentgateway.notification.sms.controller;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.commons.api.SmsSender;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.notification.sms.sendSms.SmsSenderApi;
import com.paymentgateway.notification.sms.smsCreater.SendPerformanceSmsServiceSMS;
import com.paymentgateway.notification.sms.smsCreater.SmsRequestObject;
import com.paymentgateway.notification.sms.smsCreater.UpdateMerchantCharges;
import com.paymentgateway.notification.sms.smsCreater.UpdateMerchantVPA;

@RestController
public class SmsSenderController {

	@Autowired
	private SmsSenderApi smsSender;

	@Autowired
	private SendPerformanceSmsServiceSMS sendPerformanceSmsService;
	
	@Autowired
	private UpdateMerchantCharges updateMerchantCharges;
	
	@Autowired
	private UpdateMerchantVPA updateMerchantVPA;
	
	@Autowired
	private SmsSender smsSend;

	private static Logger logger = LoggerFactory.getLogger(SmsSenderController.class.getName());

	@RequestMapping(method = RequestMethod.POST, value = "/sendSMS")
	public String sendSms(@RequestBody ResponseObject responseObject) throws Exception {
		String response = smsSender.sendSMS(responseObject.getReceiverNumber(), responseObject.getResponseMessage());
		logger.info(response);
		return "success";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/sendDailySMS")
	public String sendDailySms(@RequestBody ResponseObject responseObject) throws Exception {
		sendPerformanceSmsService.sendSms(responseObject.getPayId(), responseObject.getResponseMessage());
		logger.info("SMS sent");
		return "success";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/checkTxnSms")
	public String checkTransactionSMS(@RequestBody SmsRequestObject responseObject) throws Exception {
		sendPerformanceSmsService.checkLastTxn(responseObject.getPayId(), responseObject.getDuration());
		return "success";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/updateMerchantCharges")
	public String updateMerchantCharges(@RequestBody SmsRequestObject responseObject) throws Exception {
		updateMerchantCharges.updateCharges(responseObject.getDateFrom(), responseObject.getDateTo(),
			 responseObject.getPayId());
		return "success";
	}
	

	@RequestMapping(method = RequestMethod.POST, value = "/updateMerchantVpa")
	public String updateMerchantVpa(@RequestBody SmsRequestObject responseObject) throws Exception {
		String response = updateMerchantVPA.updatePreviousVpa();
		return response;
	}
	
	@RequestMapping(method = RequestMethod.POST,value = "/sendMandateSignSMS", consumes = MediaType.APPLICATION_JSON_VALUE)
	public String eNachRegistrationLink(@RequestBody Map<String, String> requestField) throws Exception {
	
		String response = null;
		for (Map.Entry<String, String> pair : requestField.entrySet()) {
			String smsInnuvisolutions = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
			if(StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {
				logger.info("Sms Send by smsInnuvisolutions");
				response = smsSend.sendSMSByInnvisSolution(pair.getKey(), pair.getValue());
			}else {
				logger.info("Normal Sms send");
				response = smsSend.sendSMS(pair.getKey(), pair.getValue());
			}
		}
		
		logger.info(response);
		//return "success";
		 return response;
	}
}
