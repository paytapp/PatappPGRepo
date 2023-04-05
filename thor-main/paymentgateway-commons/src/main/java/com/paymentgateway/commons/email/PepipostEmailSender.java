package com.paymentgateway.commons.email;

import java.io.File;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.user.EmailData;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmEmailer;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class PepipostEmailSender {

	private final Logger logger = LoggerFactory.getLogger(PepipostEmailSender.class.getName());
	
	@Autowired
	private CrmEmailer crmEmailer; 

	public String sendEmail(String body, String subject, String mailTo, String mailCc) {

		EmailData email = new EmailData();

		String status = null;

		try {

			email.setBody(body);
			email.setSubject(subject);
			email.setEmailTo(mailTo);
			email.setEmailCc(mailCc);

			status = sendingEmailProcess(email);

		} catch (Exception e) {
			logger.info("exception in email sending using pepipost sevice", e);
		}

		return status;
	}

	public String sendEmailWithAttachment(String body, String subject, String mailTo, String mailCc, File file) {

		EmailData email = new EmailData();
		String status = null;

		try {

			email.setBody(body);
			email.setSubject(subject);
			email.setEmailTo(mailTo);
			email.setEmailCc(mailCc);

			if (file != null) {
				String fileName = file.getName();
				byte[] fileContent = FileUtils.readFileToByteArray(file);
				String encodedString = Base64.getEncoder().encodeToString(fileContent);
				email.setFileContentbase64(encodedString);
				email.setFileName(fileName);
			}

			status = sendingEmailProcess(email);

		} catch (Exception e) {
			logger.info("exception in email sending using pepipost sevice", e);
		}

		return status;
	}

	public boolean invoiceEmail(String body, String subject, String mailTo, String mailBcc,
			boolean emailExceptionHandlerFlag) throws Exception {

		if (StringUtils.isBlank(mailTo) && StringUtils.isBlank(mailBcc)) {
			return false;
		}

		EmailData email = new EmailData();

		String status = null;
		
		try {
			email.setBody(body);
			email.setSubject(subject);
			email.setEmailTo(mailTo);
			email.setEmailbcc(mailBcc);

			status = sendingEmailProcess(email);
			
			if(StringUtils.isNotBlank(status))
				return true;
			else
				return false;
			

		} catch (Exception e) {
			logger.info("exception in email sending using pepipost sevice invoiceEmail()", e);
			return false;
		}

	}

	private String sendingEmailProcess(EmailData email) {
		logger.info("sending email with pepipost service");
		String status;
		try {
			JSONObject mainObj = new JSONObject();

			String url = PropertiesManager.propertiesMap.get("ApiUrl");
			String apiKey = PropertiesManager.propertiesMap.get("ApiKey");
			String emailFrom = PropertiesManager.propertiesMap.get("MailSender");
			String emailSenderName = PropertiesManager.propertiesMap.get("MailSenderName");

			if (email == null) {
				logger.info("Email class is empty");
				return null;
			}

			if (StringUtils.isBlank(email.getEmailTo())) {
				logger.info("EmailTo is empty");
				return null;
			}

			// From block
			JSONObject fromObj = new JSONObject();
			if(StringUtils.isNotBlank(email.getEmailFrom()))
				fromObj.put("email", emailFrom);
			else
				fromObj.put("email", emailFrom);
			
			fromObj.put("name", emailSenderName);
			mainObj.put("from", fromObj);

			// Content
			JSONArray contentObjArray = new JSONArray();
			JSONObject contentObj = new JSONObject();
			contentObj.put("type", "html");
			contentObj.put("value", email.getBody());
			mainObj.put("content", contentObjArray.put(contentObj));

			// Persnalization
			JSONArray personalizeObjArray = new JSONArray();
			JSONObject personalizeObj = new JSONObject();

			// Persnalization-> To
			JSONArray toObjArray = new JSONArray();
			JSONObject toObj = new JSONObject();
			toObj.put("email", email.getEmailTo());

			personalizeObj.put("to", toObjArray.put(toObj));
			
			if(StringUtils.isNotBlank(email.getEmailCc())){
				// Persnalization-> CC
				JSONArray ccObjArray = new JSONArray();
				
				for(String ccEmailId : email.getEmailCc().split(",")) {
					JSONObject ccObj = new JSONObject();
					ccObj.put("email", ccEmailId);
					ccObjArray.put(ccObj);
				}
				personalizeObj.put("cc", ccObjArray);
				
			}
			
			mainObj.put("personalizations", personalizeObjArray.put(personalizeObj));
			

			mainObj.put("subject", email.getSubject());

			JSONObject settingObj = new JSONObject();
			settingObj.put("open_track", true);
			settingObj.put("click_track", true);
			settingObj.put("unsubscribe_track", false);

			mainObj.put("settings", settingObj);

			// attachment if single
			if (StringUtils.isNotBlank(email.getFileContentbase64()) && StringUtils.isNotBlank(email.getFileName())) {

				JSONArray attachmentObjArray = new JSONArray();
				JSONObject attachmentObj = new JSONObject();
				attachmentObj.put("content", email.getFileContentbase64());
				attachmentObj.put("name", email.getFileName());

				mainObj.put("attachments", attachmentObjArray.put(attachmentObj));
			}

			// if multiple attachments
			if (email.getMultipleAttachments()!=null && !email.getMultipleAttachments().isEmpty()) {

				JSONArray attachmentObjArray = new JSONArray();

				for (Map.Entry<String, String> entry : email.getMultipleAttachments().entrySet()) {
					JSONObject attachmentObj = new JSONObject();
					attachmentObj.put("content", entry.getValue());
					attachmentObj.put("name", entry.getKey());
					attachmentObjArray.put(attachmentObj);
				}

				mainObj.put("attachments", attachmentObjArray);
			}

			// Add BCC if any
			if (StringUtils.isNotBlank(email.getEmailbcc())) {

				JSONArray emailBccObjArray = new JSONArray();
				JSONObject emailBccObj = new JSONObject();
				emailBccObj.put("bcc", email.getEmailbcc());

				mainObj.put("bcc", emailBccObjArray.put(emailBccObj));
			}

			logger.info("Final request data for email with pepipost service is " + mainObj.toString());

			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(url);
			request.toString();

			StringEntity params = new StringEntity(mainObj.toString());
			request.addHeader(Constants.CONTENT_TYPE.getValue(), Constants.APPLICATION_JSON.getValue());
			request.addHeader("api_key", apiKey);
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			StatusLine statusLine = resp.getStatusLine();
			int statusCode = resp.getStatusLine().getStatusCode();
			status = resp.getStatusLine().toString();
			logger.info("email Status code " + statusCode + " status " + status);
		} catch (Exception exception) {
			logger.error("error : ", exception);
			return null;
		}
		return status;
	}

	public String invoiceEmailWithAttachment(String body, String subject, String mailTo, String mailcc, File file,
			boolean emailExceptionHandlerFlag) throws Exception {

		if (StringUtils.isBlank(mailTo) && StringUtils.isBlank(mailcc)) {
            return "failed";
		}

		EmailData email = new EmailData();
		String status = null;

		try {

			email.setBody(body);
			email.setSubject(subject);
			email.setEmailTo(mailTo);
			email.setEmailCc(mailcc);

			if (file != null) {
				String fileName = file.getName();
				byte[] fileContent = FileUtils.readFileToByteArray(file);
				String encodedString = Base64.getEncoder().encodeToString(fileContent);
				email.setFileContentbase64(encodedString);
				email.setFileName(fileName);
			}

			status = sendingEmailProcess(email);
			if(status.contains("Accepted")) {
                return "success";   
            }else {
                return "failed";
            }
		} catch (Exception e) {
			logger.info("exception in email sending using pepipost sevice", e);
			return "failed";
		}

	}

	

    public String eposEmailWithAttachmentToCustomer(String body, String subject, String mailTo, File file) throws Exception {

        if (StringUtils.isBlank(mailTo)) {
            return "failed";
        }

        EmailData email = new EmailData();
        String status = null;

        try {

            email.setBody(body);
            email.setSubject(subject);
            email.setEmailTo(mailTo);
            

            if (file != null) {
                String fileName = file.getName();
                byte[] fileContent = FileUtils.readFileToByteArray(file);
                String encodedString = Base64.getEncoder().encodeToString(fileContent);
                email.setFileContentbase64(encodedString);
                email.setFileName(fileName);
            }

            status = sendingEmailProcess(email);
            if(status.contains("Accepted")) {
                return "success";   
            }else {
                return "failed";
            }
            

        } catch (Exception e) {
            logger.info("exception in email sending using pepipost sevice", e);
            return "failed";
        }

    }
	
	public void sendEmailWithAttachment(String subject, String body, String attachmentFileName, byte[] bytes,
			String mailTo, String mailFrom, String filenameexcel, byte[] bytesexcel, String emailCc) {

		EmailData email = new EmailData();
		String status = null;

		try {
			Map<String, String> multiAttachDoc = new HashMap<String, String>();
			String excelbase64 = Base64.getEncoder().encodeToString(bytesexcel);
			String pdfbase64 = Base64.getEncoder().encodeToString(bytes);

			email.setBody(body);
			email.setSubject(subject);
			email.setEmailTo(mailTo);
			if(StringUtils.isNotBlank(emailCc))
				email.setEmailCc(emailCc);

			multiAttachDoc.put(filenameexcel, excelbase64);
			multiAttachDoc.put(attachmentFileName, pdfbase64);

			email.setMultipleAttachments(multiAttachDoc);

			status = sendingEmailProcess(email);
			
			if(StringUtils.isNotBlank(status))
				logger.info("email sent successfully "+status);

		} catch (Exception e) {
			logger.info("exception in email sending using pepipost sevice  sendEmailWithAttachment()", e);

		}
	}
	
	
	public void sendTransactionEmailToMerchant(Fields fields, User user) throws Exception {

		if (StringUtils.isBlank(user.getEmailId()) && StringUtils.isBlank(user.getTransactionEmailId())) {
			logger.info("Merchant Email Not Found");
			return;
		}
		
		EmailData email = new EmailData();
		String status = null;

		try {

			email.setBody(crmEmailer.merchantTransactionEmailBody(fields, user));
			email.setSubject("Transaction Successful | " + fields.get(FieldType.ORDER_ID.getName()).toString() + " | Payment GateWay");
			email.setEmailTo(user.getEmailId());
			
			status = sendingEmailProcess(email);

		} catch (Exception e) {
			logger.info("exception in email sending using pepipost sevice sendTransactionEmailToMerchant() ", e);
		}

	}
	
	public void sendTransactionEmailToCustomer(Fields fields, User user) throws Exception {

			if (StringUtils.isBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
				logger.info("Customer Email Not Found");
				return;

			}
			
			EmailData email = new EmailData();

			try {

				email.setBody(crmEmailer.customerTransactionEmailBody(fields, user));
				email.setSubject("Your Transaction is Successful | " + user.getBusinessName() + " | "
						+ fields.get(FieldType.ORDER_ID.getName()) + "");
				email.setEmailTo(fields.get(FieldType.CUST_EMAIL.getName()));
				
				sendingEmailProcess(email);

			} catch (Exception e) {
				logger.info("exception in email sending using pepipost sevice sendTransactionEmailToCustomer() ", e);
			}
	}

	public void sendChargebackEmail(String subject, String body, String mailTo, String mailCc ,
			Map<String, File> attachedFiles) {
		
		EmailData email = new EmailData();

		try {

			email.setBody(body);
			email.setSubject(subject);
			email.setEmailTo(mailTo);
			email.setEmailCc(mailCc);
			email.setEmailFrom("chargeback@PaymentGateWay.com");
			
			if(attachedFiles!=null){

				Map<String,String> multiAttachmentData =new HashMap<String, String>();
				for (Map.Entry<String, File> entry : attachedFiles.entrySet()) {
					String fileName = entry.getKey();
					byte[] fileContent = FileUtils.readFileToByteArray(entry.getValue());
					String encodedString = Base64.getEncoder().encodeToString(fileContent);
					multiAttachmentData.put(fileName, encodedString);
				}
				email.setMultipleAttachments(multiAttachmentData);
			}
			sendingEmailProcess(email);
		
		} catch (Exception e) {
			logger.info("exception in email sending using pepipost sevice", e);
		}
		
	}
	
	public boolean sendEmailThroughApi(String body, String subject, String toEmail, String emailToBcc) {
		EmailData email = new EmailData();

		String status = null;

		try {

			email.setBody(body);
			email.setSubject(subject);
			email.setEmailTo(toEmail);
			email.setEmailCc(emailToBcc);

			status = sendingEmailProcess(email);
			
			if(StringUtils.isNotBlank(status)){
				return true;
			}else{
				return false;
			}

		} catch (Exception e) {
			logger.info("exception in email sending using pepipost sevice", e);
		}

		return false;
	}
	

	
}
