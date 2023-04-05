package com.paymentgateway.crm.mailers;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.paymentgateway.commons.email.EmailServiceProvider;
import com.paymentgateway.commons.util.MailersObject;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class MailerService {

	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private EmailServiceProvider emailServiceProvider;
	
	
	
	private static Logger logger = LoggerFactory.getLogger(MailerService.class.getName());
	
	public void emailGenerator(MailersObject mailersObject, String mailType, String dateFrom, String dateTo, String timeFrom, String timeTo, String settlementDate, String settlementTime, String paymentType, String isDateRequiredFlag) throws Exception{
		
		String mailContent = "";
		if(!StringUtils.isEmpty(mailType) && mailType.equalsIgnoreCase("maintenanceActivity")) {
			mailContent = "This is to inform you that, there will be a planned downtime of Paymnet Gateway IPG services, starting from  " + timeFrom + " " + dateFrom + " to " + timeTo + " " + dateTo;
			mailersObject.setSubject("Paymnet Gateway - IPG Scheduled Downtime");
			mailersObject.setMessageBody(createEmailBody(mailContent));
		}
		if(!StringUtils.isEmpty(mailType) && mailType.equalsIgnoreCase("settlementDelay")) {
			
			if(!StringUtils.isEmpty(settlementDate)) {
				mailContent = "This is to inform you that, the transactions captured from " + timeFrom + " " + dateFrom + " to " + timeTo + " " + dateTo + " will be settled on " + settlementTime + " " + settlementDate;
			}else {
				mailContent = "This is to inform you that, the settlement of the transactions captured from  " + timeFrom + " " + dateFrom + " to " + timeTo + " " + dateTo + " has been delayed today.";
			}
			mailersObject.setSubject("Paymnet Gateway IPG | Delay in Settlement");
			mailersObject.setMessageBody(createEmailBody(mailContent));
		}
		if(!StringUtils.isEmpty(mailType) && mailType.equalsIgnoreCase("paymentTypeDown")) {
			
			if(StringUtils.isNotEmpty(isDateRequiredFlag) && Boolean.valueOf(isDateRequiredFlag) == false) {
				mailContent = "This is to inform you that, '"+ paymentType +"' services are down on Paymnet Gateway IPG, We will update you once services are online";
			}else {
				mailContent = "This is to inform you that, '"+ paymentType +"' services are down on Paymnet Gateway IPG, starting from " + timeFrom + " " + dateFrom + " to " + timeTo + " " + dateTo;
			}
				String[] paymentTypeArray = paymentType.split(",");
			StringBuilder builder = new StringBuilder();
			int count = 0;
			for(String pmntTyp : paymentTypeArray) {
				
				if(count > 0 ) {
					builder.append(" | ");
				}
				builder.append(pmntTyp);
				count++;
			}
			paymentType = builder.toString();
			mailersObject.setSubject("Paymnet Gateway IPG | " + paymentType + " Down");
			mailersObject.setMessageBody(createEmailBody(mailContent));
		}
	
		emailServiceProvider.multipleUsersEmailSender(mailersObject);
			
	}
	
	
	public String createEmailBody(String mailContent) {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {
			content.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>Settlement Delay</title><style>body {font-family: Arial, Helvetica, sans-serif;}</style></head><body>");
            content.append("<table width=\"450\" height=\"100%\" bgcolor=\"#f5f5f5\" align=\"center\" style=\"padding: 20px; padding-top: 5px; border: 1px solid #ddd;\">");
            content.append("<tbody><tr><td style=\"padding: 20px;\"><table><tbody>");
            content.append("<tr><td align=\"center\"><img height=\"50\" src=\""+propertiesManager.getSystemProperty("emailerLogoURL")+"\" alt=\"\" ></td></tr>");
            content.append("<tr><td style=\"padding: 10px; background: #fff; border: 1px solid #eee;\">");
            content.append("<table width=\"100%\" style=\"border-collapse:collapse; height:auto !important; color:#000;\"><tbody>");
            content.append("<tr><td><h2 style=\"margin: 0;font-size: 15px;font-weight: 400;color: #8a8a8a;margin-bottom: 10px;\">Dear Sir/Ma'am</h2></td></tr>");
            content.append("<tr><td><h2 style=\"margin: 0;font-size: 15px;font-weight: 400;color: #8a8a8a;margin-bottom: 10px;\">Greetings from Paymnet Gateway!</h2></td></tr>");
            content.append("<tr><td><h2 style=\"margin: 0;font-size: 15px; line-height: 22px; font-weight: 400;color: #8a8a8a;margin-bottom: 10px;\">");
            content.append(mailContent);
            content.append("</h2></td></tr>");
            content.append("<tr><td style=\"padding-top: 10px;padding-bottom: 10px;line-height: 20px;font-size: 14px;color: #8a8a8a;\">Regards,<br><span style=\"display: block;\">Team Paymnet Gateway</span></td></tr>");
            content.append("</tbody></table></td></tr></tbody></table></td></tr></tbody></table></body></html>");
			
            body = content.toString();
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}
}
