/*
 * package com.paymentgateway.crm.jmstemplate;
 * 
 * import org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.jms.core.JmsTemplate; import
 * org.springframework.stereotype.Component;
 * 
 * import com.google.gson.Gson; import com.paymentgateway.commons.util.Fields;
 * 
 * @Component public class SendTransactionEmail {
 * 
 * @Autowired private JmsTemplate jmsTemplate;
 * 
 * private Gson gson = new Gson(); // Send email producer public void
 * sendEmail(final Fields fields) { String fieldsResponse = gson.toJson(fields);
 * jmsTemplate.convertAndSend("trnsactionemail.topic", fieldsResponse); }
 * 
 * // Send sms producer public void sendSms(final Fields fields) { String
 * fieldSmsResponse = gson.toJson(fields);
 * jmsTemplate.convertAndSend("trnsactionSms.topic", fieldSmsResponse);
 * 
 * }
 * 
 * }
 */