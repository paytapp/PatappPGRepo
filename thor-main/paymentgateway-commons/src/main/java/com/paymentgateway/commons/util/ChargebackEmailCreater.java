package com.paymentgateway.commons.util;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.email.PepipostEmailSender;
import com.paymentgateway.commons.user.Chargeback;
import com.paymentgateway.commons.user.ChargebackComment;
import com.paymentgateway.commons.user.User;

@Service
public class ChargebackEmailCreater {
	
	private static final Logger logger= LoggerFactory.getLogger(ChargebackEmailCreater.class);
	
	@Autowired
	private PepipostEmailSender pepipostEmailSender;
	
	
	public void sendChargebackRaisedEmail(Chargeback chargeback, User txnUser, Map<String, File> attachedFiles) {
		try{
				String subject = "Chargeback Raised | "+chargeback.getCaseId()+" | "+DateCreater.defaultCurrentDateTime();
				String emailCC = PropertiesManager.propertiesMap.get("CHARGEBACK_CC_EMAILS");
				String emailBody = emailBodyForOpeningChargeback(chargeback,txnUser);
				
				pepipostEmailSender.sendChargebackEmail(subject, emailBody, txnUser.getEmailId(), emailCC, attachedFiles);

		}catch (Exception e) {
			logger.info("exception in sendChargebackRaisedEmail()",e);
		}
		
	}
	
	public void sendChargebackClosedEmail(Chargeback chargeback, User txnUser){
		
		try {
			String subject = "Chargeback Closed | "+chargeback.getCaseId()+" | "+DateCreater.defaultCurrentDateTime();
			String emailCC = PropertiesManager.propertiesMap.get("CHARGEBACK_CC_EMAILS");
			String emailBody = emailBodyForClosingChargeback(chargeback,txnUser);
			
			pepipostEmailSender.sendChargebackEmail(subject, emailBody, txnUser.getEmailId(), emailCC, null);
		} catch (Exception e) {
			logger.info("exception in sendChargebackClosedEmail()",e);
		}
		
	}
	
	public void sendChargebackAddCommentEmail(Chargeback chargeback, ChargebackComment chargebackComment,
			User sessionUser, Map<String, File> attachedFiles) {
		try{
			
			String subject = "Chargeback Comment Added | "+chargeback.getCaseId()+" | "+DateCreater.defaultCurrentDateTime();
			String emailCC = PropertiesManager.propertiesMap.get("CHARGEBACK_CC_EMAILS");
			String emailBody = emailBodyForAddCommentChargeback(chargeback,chargebackComment,sessionUser);
			
			pepipostEmailSender.sendChargebackEmail(subject, emailBody, "chargeback@paymentGateWay.com", emailCC, attachedFiles);
			
		}catch (Exception e) {
			logger.info("exception in sendChargebackAddCommentEmail() ",e);
		}
		
	}
	
	public void sendChargebackAccptedRejectEmail(Chargeback chargeback, User sessionUser) {
		
	try{
		
			String subject = "Chargeback "+chargeback.getStatus()+" | "+chargeback.getCaseId()+" | "+DateCreater.defaultCurrentDateTime();
			String emailCC = PropertiesManager.propertiesMap.get("CHARGEBACK_CC_EMAILS");
			String emailBody = emailBodyForAcceptRejectChargeback(chargeback,sessionUser);
			
			pepipostEmailSender.sendChargebackEmail(subject, emailBody, "chargeback@paymentGateWay.com", emailCC,null);
			
		}catch (Exception e) {
			logger.info("exception in sendChargebackAddCommentEmail() ",e);
		}
		
	}
	
	
	private String emailBodyForAcceptRejectChargeback(Chargeback chargeback, User sessionUser) {
		try{
			String logoUrl = PropertiesManager.propertiesMap.get("logoForEmail");
			chargeback.getChargebackAmount();
			
			StringBuilder body = new StringBuilder();
			
			body.append("<!DOCTYPE html>");
			body.append("<html lang='en'> <head> <meta charset='UTF-8'> <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
			body.append("<title>Chargeback Email</title>");
			body.append("<style> body{ font-family: 'Times New Roman', Times, serif} table table td, table table th {border: 1px solid;} table table {width: 100%;border-collapse: collapse;} </style>");
			body.append("</head>");
			body.append("<body bgcolor='#ccc'>");
			body.append("<table width='700' border='0' border-collapse='collapse' cellpadding='0' cellspacing='0' bgcolor='#fff' align='center'>");
			body.append("<tbody> <tr>");
			body.append("<td width='70%' bgcolor='#002664' style='padding: 10px;color: #fff;font-size: 20px;'>E-mail notification</td>");
			body.append("<td width='30%' style='padding: 10px;' align='right'> <img src="+logoUrl+" alt='' width='150'> </td>");
			body.append("</tr>");
			body.append("<tr> <td colspan='3' style='padding: 10px;'>");
			body.append("<h2 style='margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;margin-top: 10px;'>");
			
			body.append("Dear Team, <br><br>");
			
			body.append("Chargeback ID : "+chargeback.getCaseId()+" Has been <b>"+chargeback.getStatus()+"</b> <br>");
			body.append("<tr> <td colspan='3'> <h2 style='margin: 0;padding-left: 10px;'>Chargeback Details</h2> </td> </tr>");
			body.append("<tr> <td colspan='3' style='padding: 10px'>");
			body.append("<table width='100%' cellpadding='10' bgColor='#f5f5f5'> <tr>");
			body.append("<td>Case ID</td> <td>"+chargeback.getCaseId()+"</td> </tr><tr>");
			body.append("<td>Order ID</td> <td>"+chargeback.getOrderId()+"</td> </tr><tr>");
			body.append("<td>Updated By </td> <td>"+sessionUser.getBusinessName()+"</td> </tr><tr>");
			body.append("<td>Status</td> <td>"+chargeback.getStatus()+"</td></tr> </table> </td> </tr>");
			
			body.append("<tr><td colspan='3' style='padding: 10px;font-size: 14px;color: #a0a0a0'></td></tr>");
			body.append("<tr><td colspan='3' style='padding-left: 10px'>Regards</td></tr><br>");
			body.append("<tr><td colspan='3' style='padding-left: 10px;padding-bottom: 10px;'>Payment GateWay Team.</td> </tr>");
			body.append("</tbody> </table> </body> </html>");
			
			return body.toString();
			
		}catch (Exception e) {
			logger.info("Exception in emailBodyForOpeningChargeback()",e);
		}
		return null;
	}

	private String emailBodyForAddCommentChargeback(Chargeback chargeback, ChargebackComment chargebackComment,
			User sessionUser) {
			
		try{
			String logoUrl = PropertiesManager.propertiesMap.get("logoForEmail");
			chargeback.getChargebackAmount();
			
			StringBuilder body = new StringBuilder();
			
			body.append("<!DOCTYPE html>");
			body.append("<html lang='en'> <head> <meta charset='UTF-8'> <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
			body.append("<title>Chargeback Email</title>");
			body.append("<style> body{ font-family: 'Times New Roman', Times, serif} table table td, table table th {border: 1px solid;} table table {width: 100%;border-collapse: collapse;} </style>");
			body.append("</head>");
			body.append("<body bgcolor='#ccc'>");
			body.append("<table width='700' border='0' border-collapse='collapse' cellpadding='0' cellspacing='0' bgcolor='#fff' align='center'>");
			body.append("<tbody> <tr>");
			body.append("<td width='70%' bgcolor='#002664' style='padding: 10px;color: #fff;font-size: 20px;'>E-mail notification</td>");
			body.append("<td width='30%' style='padding: 10px;' align='right'> <img src="+logoUrl+" alt='' width='150'> </td>");
			body.append("</tr>");
			body.append("<tr> <td colspan='3' style='padding: 10px;'>");
			body.append("<h2 style='margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;margin-top: 10px;'>");
			
			body.append("Dear Team, <br><br>");
			
			body.append("New Comment is Added on chargeback ID : "+chargeback.getCaseId()+" <br>");
			body.append("<tr> <td colspan='3'> <h2 style='margin: 0;padding-left: 10px;'>Chargeback Details</h2> </td> </tr>");
			body.append("<tr> <td colspan='3' style='padding: 10px'>");
			body.append("<table width='100%' cellpadding='10' bgColor='#f5f5f5'> <tr>");
			body.append("<td>Case ID</td> <td>"+chargeback.getCaseId()+"</td> </tr><tr>");
			body.append("<td>Order ID</td> <td>"+chargeback.getOrderId()+"</td> </tr><tr>");
			body.append("<td>Comment </td> <td>"+chargebackComment.getCommentBody()+"</td> </tr><tr>");
			body.append("<td>Comment By </td> <td>"+sessionUser.getBusinessName()+"</td> </tr><tr>");
			body.append("<td>Comment Date Time</td> <td>"+DateCreater.defaultCurrentDateTime()+"</td></tr> </table> </td> </tr>");
			
			body.append("<tr><td colspan='3' style='padding: 10px;font-size: 14px;color: #a0a0a0'></td></tr>");
			body.append("<tr><td colspan='3' style='padding-left: 10px'>Regards</td></tr><br>");
			body.append("<tr><td colspan='3' style='padding-left: 10px;padding-bottom: 10px;'>Payment GateWay Team.</td> </tr>");
			body.append("</tbody> </table> </body> </html>");
			
			return body.toString();
			
		}catch (Exception e) {
			logger.info("Exception in emailBodyForOpeningChargeback()",e);
		}
		return null;
	}

	private String emailBodyForOpeningChargeback(Chargeback chargeback, User user) {
		try{
			
			String logoUrl = PropertiesManager.propertiesMap.get("logoForEmail");
			chargeback.getChargebackAmount();
			
			StringBuilder body = new StringBuilder();
			
			body.append("<!DOCTYPE html>");
			body.append("<html lang='en'> <head> <meta charset='UTF-8'> <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
			body.append("<title>Chargeback Email</title>");
			body.append("<style> body{ font-family: 'Times New Roman', Times, serif} table table td, table table th {border: 1px solid;} table table {width: 100%;border-collapse: collapse;} </style>");
			body.append("</head>");
			body.append("<body bgcolor='#ccc'>");
			body.append("<table width='700' border='0' border-collapse='collapse' cellpadding='0' cellspacing='0' bgcolor='#fff' align='center'>");
			body.append("<tbody> <tr>");
			body.append("<td width='70%' bgcolor='#002664' style='padding: 10px;color: #fff;font-size: 20px;'>E-mail notification</td>");
			body.append("<td width='30%' style='padding: 10px;' align='right'> <img src="+logoUrl+" alt='' width='150'> </td>");
			body.append("</tr>");
			body.append("<tr> <td colspan='3' style='padding: 10px;'>");
			body.append("<h2 style='margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;margin-top: 10px;'>");
			
			body.append("Dear Merchant, <br><br>");
			
			body.append("Chargeback has been raised against below Transaction<br>");
			body.append("<tr> <td colspan='3'> <h3 style='margin: 0;padding-left: 10px;'>Chargeback Details</h3> </td> </tr>");
			body.append("<tr> <td colspan='3' style='padding: 10px'>");
			body.append("<table width='100%' cellpadding='10' bgColor='#f5f5f5'> <tr>");
			body.append("<td>Case ID</td> <td>"+chargeback.getCaseId()+"</td> </tr><tr>");
			body.append("<td>Order ID</td> <td>"+chargeback.getOrderId()+"</td> </tr><tr>");
			body.append("<td>Merchant</td> <td>"+user.getBusinessName()+"</td> </tr><tr>");
			body.append("<td>Amount</td> <td>"+chargeback.getChargebackAmount()+"</td> </tr><tr>");
			body.append("<td>Status</td> <td>"+chargeback.getStatus()+"</td></tr> </table> </td> </tr>");
			
			body.append("<tr><td td colspan='3' style='padding: 10px;'>Request you to please provide us with the below mentioned documents.</td></tr><br><br>");
			body.append("<tr><td td colspan='3' style='padding: 10px;'>You may please submit the documents only in PDF format as a revert to this email (without changing the subject line).</td></tr>");
			
			body.append("<tr> <td colspan='3' style='padding: 10px'>");
			body.append("<table width='50%' cellpadding='10' bgColor='#f5f5f5'>");
			body.append("<tr><td>No</td><td>Document</td><td>Type</td><td></td></tr>");
			body.append("<tr><td>1</td><td>Invoice</td><td>Mandatory</td> <td>A copy of the bills / invoices for the mentioned transactions drawn in the name of the customer / cardholders.</td> </tr>");
			body.append("<tr><td>2</td><td>PoD</td><td>Mandatory</td> <td>A proof of delivery of goods / services (i.e. dispatch challan, courier receipt acknowledgement, shipment number of registered courier services )</td> </tr>");
			body.append("<tr><td>3</td><td>ID proof</td><td>Non-mandatory</td> <td>ID proof of the cardholder, only if collected at the time of delivery of goods / services</td> </tr>");
			body.append("<tr><td>4</td><td>Others</td><td>Non-mandatory</td> <td>Any other relevant document sufficient to establish the authenticity of the transaction as well as the acceptance of the delivery by the purchaser / cardholder</td> </tr>");

			body.append("</table> </td> </tr>");
			
			body.append("<tr><td colspan='3' style='padding: 10px;font-size: 14px;color: #a0a0a0'></td></tr>");
			body.append("<tr><td colspan='3' style='padding-left: 10px'>Regards</td></tr><br>");
			body.append("<tr><td colspan='3' style='padding-left: 10px;padding-bottom: 10px;'>Payment GateWay Team.</td> </tr>");
			body.append("</tbody> </table> </body> </html>");
			
			return body.toString();
			
		}catch (Exception e) {
			logger.info("Exception in emailBodyForOpeningChargeback()",e);
		}
		return null;
	}
	
	private String emailBodyForClosingChargeback(Chargeback chargeback, User user) {
		try{
			
			String logoUrl = PropertiesManager.propertiesMap.get("logoForEmail");
			chargeback.getChargebackAmount();
			
			StringBuilder body = new StringBuilder();
			
			body.append("<!DOCTYPE html>");
			body.append("<html lang='en'> <head> <meta charset='UTF-8'> <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
			body.append("<title>Chargeback Email</title>");
			body.append("<style> body{ font-family: 'Times New Roman', Times, serif} table table td, table table th {border: 1px solid;} table table {width: 100%;border-collapse: collapse;} </style>");
			body.append("</head>");
			body.append("<body bgcolor='#ccc'>");
			body.append("<table width='700' border='0' border-collapse='collapse' cellpadding='0' cellspacing='0' bgcolor='#fff' align='center'>");
			body.append("<tbody> <tr>");
			body.append("<td width='70%' bgcolor='#002664' style='padding: 10px;color: #fff;font-size: 20px;'>E-mail notification</td>");
			body.append("<td width='30%' style='padding: 10px;' align='right'> <img src="+logoUrl+" alt='' width='150'> </td>");
			body.append("</tr>");
			body.append("<tr> <td colspan='3' style='padding: 10px;'>");
			body.append("<h2 style='margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;margin-top: 10px;'>");
			
			body.append("Dear Merchant, <br><br>");
			
			body.append("The below dispute is Closed.<br>");
			body.append("<tr> <td colspan='3'> <h2 style='margin: 0;padding-left: 10px;'>Chargeback Details</h2> </td> </tr>");
			body.append("<tr> <td colspan='3' style='padding: 10px'>");
			body.append("<table width='100%' cellpadding='10' bgColor='#f5f5f5'> <tr>");
			body.append("<td>Case ID</td> <td>"+chargeback.getCaseId()+"</td> </tr><tr>");
			body.append("<td>Order ID</td> <td>"+chargeback.getOrderId()+"</td> </tr><tr>");
			body.append("<td>Merchant</td> <td>"+user.getBusinessName()+"</td> </tr><tr>");
			body.append("<td>Amount</td> <td>"+chargeback.getChargebackAmount()+"</td> </tr><tr>");
			body.append("<td>Status</td> <td>"+chargeback.getStatus()+"</td></tr> </table> </td> </tr>");
			
			body.append("<tr><td colspan='3' style='padding: 10px;font-size: 14px;color: #a0a0a0'></td></tr>");
			body.append("<tr><td colspan='3' style='padding-left: 10px'>Regards</td></tr><br>");
			body.append("<tr><td colspan='3' style='padding-left: 10px;padding-bottom: 10px;'>Payment GateWay Team.</td> </tr>");
			body.append("</tbody> </table> </body> </html>");
			
			return body.toString();
			
		}catch (Exception e) {
			logger.info("Exception in emailBodyForOpeningChargeback()",e);
		}
		return null;
	}

}
