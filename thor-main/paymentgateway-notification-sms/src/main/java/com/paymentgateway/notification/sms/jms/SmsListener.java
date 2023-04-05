package com.paymentgateway.notification.sms.jms;
/*package com.paymentgateway.notification.sms.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Message;
import javax.jms.TextMessage;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentgateway.notification.sms.sendSms.SmsSenderData;

@Component
public class SmsListener {
	private final Logger logger =LoggerFactory.getLogger(SmsListener.class.getName());
	@Autowired
	SmsSenderData smsSender ;
	
	@SuppressWarnings("unchecked")
	//@JmsListener(destination = "trnsactionSms.topic")
	public void smsReciever(final Message jsonMessage ){
		String messageData = null;
		Map<String, String> responseMap = new HashMap<String, String>();
		JSONParser parser = new JSONParser();
		ObjectMapper mapper = new ObjectMapper();
		try {
			if (jsonMessage instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) jsonMessage;
				messageData = textMessage.getText();
				JSONObject json = (JSONObject) parser.parse(messageData);
				Object fieldsObject = json.get("fields");
				responseMap = mapper.convertValue(fieldsObject, Map.class);
				smsSender.sendSMS(responseMap);
			}
			}catch (Exception exception) {
				logger.error("jms error" +exception);
			}
		}
	
	
	
	//@JmsListener(destination = "invoiceSms.topic")
	public void invoiceReciever(final Message jsonMessage){
		String messageData = null;
		Map<String, String> responseMap = new HashMap<String, String>();
		JSONParser parser = new JSONParser();
		ObjectMapper mapper = new ObjectMapper();
		try {
			if (jsonMessage instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) jsonMessage;
				messageData = textMessage.getText();
				JSONObject json = (JSONObject) parser.parse(messageData);
				Object fieldsObject = json.get("fields");
				responseMap = mapper.convertValue(fieldsObject, Map.class);
				//smsSender.sendPromoSMS(responseMap, url);
			}
			}catch (Exception exception) {
				logger.error("jms error" +exception);
			}
	}
}



*/