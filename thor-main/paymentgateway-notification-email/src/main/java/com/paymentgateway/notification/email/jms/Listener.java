/*
 * package com.paymentgateway.notification.email.jms;
 * 
 * import java.util.HashMap; import java.util.Map;
 * 
 * import javax.jms.Message; import javax.jms.TextMessage; import
 * org.json.simple.JSONObject; import org.json.simple.parser.JSONParser; import
 * org.slf4j.Logger; import org.slf4j.LoggerFactory; import org.slf4j.Logger;
 * import org.slf4j.LoggerFactory; import
 * org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.beans.factory.annotation.Qualifier; import
 * org.springframework.jms.JmsException; import
 * org.springframework.jms.annotation.JmsListener; import
 * org.springframework.stereotype.Component; import
 * com.fasterxml.jackson.databind.ObjectMapper; import
 * com.paymentgateway.notification.email.sendEmailer.EmailSender;
 * 
 * @Component public class Listener {
 * 
 * private Logger logger = LoggerFactory.getLogger(Listener.class.getName());
 * 
 * @Autowired
 * 
 * @Qualifier("emailSender") private EmailSender emailSender;
 * 
 * private static void log(String message) { System.out.println(message); }
 * 
 * @SuppressWarnings("unchecked")
 * 
 * @JmsListener(destination = "trnsactionemail.topic") public void
 * receiveMessage(final Message jsonMessage) throws JmsException, Exception {
 * log("::Inside trnsactionemail::"); String messageData = null; Map<String,
 * String> responseMap = new HashMap<String, String>(); JSONParser parser = new
 * JSONParser(); ObjectMapper mapper = new ObjectMapper(); try { if (jsonMessage
 * instanceof TextMessage) { TextMessage textMessage = (TextMessage)
 * jsonMessage; messageData = textMessage.getText(); JSONObject json =
 * (JSONObject) parser.parse(messageData); Object fieldsObject =
 * json.get("fields"); responseMap = mapper.convertValue(fieldsObject,
 * Map.class); emailSender.sendTransactionEmail(responseMap);
 * log("-----------------------\n Email Sent \n---------------------------"); }
 * } catch (Exception exception) { logger.debug("jms error" + exception); } } }
 */