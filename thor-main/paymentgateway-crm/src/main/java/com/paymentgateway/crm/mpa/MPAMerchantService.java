package com.paymentgateway.crm.mpa;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.MPAMerchantDao;
import com.paymentgateway.commons.email.EmailServiceProvider;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.MPAMerchant;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.MPAStatusEmail;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class MPAMerchantService {
	
	@Autowired
	private UserDao userDao;	

	@Autowired
	private EmailServiceProvider emailServiceProvider;
	
	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private MPAMerchantDao mpaMerchantDao;
	
	private static Logger logger = LoggerFactory.getLogger(MPAServiceController.class.getName());


	public void senEmailToCheckermaker(String payId, String merchantStatus, String updatedBy, String formStatus) {
		
		logger.info("Inside senEmailToCheckermaker(), MPAMerchantService");
		
	try {
		
		MPAMerchant mpaMerchant = mpaMerchantDao.findByPayId(payId);
		
		String subject = null;
		String email = null;
		String url = null;
		String MerchantName = mpaMerchant.getBusinessName();
		String checkerEmailId = userDao.getEmailIdByPayId(mpaMerchant.getCheckerPayId());
		String makerEmailId = userDao.getEmailIdByPayId(mpaMerchant.getMakerPayId());
		String adminEmailId = userDao.getAdminEmail();
		
		if(!StringUtils.isEmpty(formStatus)) {
			subject = "MPA Submitted | Request for Review | " + MerchantName;
			email = adminEmailId + "," + makerEmailId;
			url = propertiesManager.getEmailProperty(Constants.REVIEW_MPAMERCHANT_URL.getValue());
		}else if(updatedBy != null && updatedBy.equalsIgnoreCase("Maker")){
			subject = "MPA | " + merchantStatus +" by Maker | Request for Review | " + MerchantName;
			email = adminEmailId + "," + makerEmailId + "," + checkerEmailId;
			url = propertiesManager.getEmailProperty(Constants.REVIEW_MPAMERCHANT_URL.getValue());
		}else if(updatedBy !=null && updatedBy.equalsIgnoreCase("Checker")){
			subject = "MPA | " + MerchantName + " | " + merchantStatus;
			email = adminEmailId + "," + makerEmailId + "," + checkerEmailId;
		}else {
			subject = "MPA | " + MerchantName + " | " + merchantStatus;
			email = adminEmailId + "," + makerEmailId + "," + checkerEmailId;
		}
		String messageBody = getMessageBody(url,mpaMerchant);
		
		MPAStatusEmail mpaStatusEmail = new MPAStatusEmail();
		mpaStatusEmail.setToEmail(email);;
		mpaStatusEmail.setSubject(subject);
		mpaStatusEmail.setMessageBody(messageBody);
		
		emailServiceProvider.mpaStatusEmailSender(mpaStatusEmail);
		
		}catch(Exception ex) {
			logger.error("Exception caught while saving MPAMerchant, " , ex);
		}
		
	}
	
	public String SaveFile(String uploadedBy, String filename, File file, String payId)
			throws SystemException {
		String destPath = PropertiesManager.propertiesMap.get("CheckerMakerStatusUpdateFilePath") + payId + "/" + uploadedBy;
		File destFile = new File(destPath, filename);

		try {
			FileUtils.copyFile(file, destFile);

		} catch (Exception exception) {
			logger.error("Exception Occured in Uploading file", exception);
		}

		return "SUCCESS";

	}
	
	public String getMessageBody(String url, MPAMerchant mpaMerchant) {
		String body = null;
		StringBuilder content = new StringBuilder();
	
		
		try {
			content.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">"
					+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
					+ "<title>Invoice</title><style>body{font-family: Arial, Helvetica, sans-serif;}</style></head>");
			content.append("<body>");
			content.append("<table width=\"450\"  height=\"100%\" bgcolor=\"#f5f5f5\" align=\"center\" style=\"padding: 20px;padding-top: 5px\">");
			content.append("<tbody><tr>");
			content.append("<td align=\"center\"><img src=\""+propertiesManager.getSystemProperty("emailerLogoURL")+"\" alt=\"\" ></td></tr>");
			content.append("<tr><td><h2 style=\"margin: 0;font-size: 15px;font-weight: 400;color: #8a8a8a;margin-bottom: 10px;\">Dear ");
			if(StringUtils.isEmpty(mpaMerchant.getMakerStatus()))
				content.append(mpaMerchant.getMakerName());
			if(!StringUtils.isEmpty(mpaMerchant.getMakerStatus()) && StringUtils.isEmpty(mpaMerchant.getCheckerStatus()))
				content.append(mpaMerchant.getCheckerName());
			if(!StringUtils.isEmpty(mpaMerchant.getMakerStatus()) && !StringUtils.isEmpty(mpaMerchant.getCheckerStatus()))
				content.append(mpaMerchant.getMakerName());
			content.append("/Admin,</td></tr>");
			content.append("<tr><td><table width=\"400\" align=\"center\" bgcolor=\"#fff\" cellpadding=\"15\" style=\"border-collapse:collapse;background-color: #fff\">");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Merchant Name:</td>");
			content.append("<td>"+mpaMerchant.getBusinessName()+"</td></tr>");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Maker:</td>");
			content.append("<td>"+mpaMerchant.getMakerName()+"</td></tr>");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Checker:</td>");
			content.append("<td>"+mpaMerchant.getCheckerName()+"</td></tr>");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Date of Submission:</td>");
			content.append("<td>"+mpaMerchant.getFormSubmissionDate()+"</td></tr>");
			
			if(!StringUtils.isEmpty(mpaMerchant.getMakerStatus())){
				content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
				content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Maker Status:</td>");
				content.append("<td>"+mpaMerchant.getMakerStatus()+"</td></tr>");
				content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
				content.append("<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Date of Review (Maker):</td>");
				content.append("<td>"+ mpaMerchant.getMakerStatusUpDate() +"</td></tr>");
			}
			if(!StringUtils.isEmpty(mpaMerchant.getCheckerStatus())){
				content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
				content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Checker Status:</td>");
				content.append("<td>"+mpaMerchant.getCheckerStatus()+"</td></tr>");
				content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
				content.append("<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Date of Review (Checker):</td>");
				content.append("<td>"+ mpaMerchant.getCheckerStatusUpDate() +"</td></tr>");
			
			}
			content.append("<tr align=\"center\"><td colspan=\"2\" style=\"padding: 20px 0\">");
			
			if(StringUtils.isEmpty(mpaMerchant.getCheckerStatus())) {
				content.append("<a href=\""+url+"\" style=\"padding: 8px 27px;height: 30px;display:inline-block;font-size: 12px;color: #fff;font-weight: 600;border-radius: 5px;text-decoration: none;\">");
				content.append("<img src=\""+propertiesManager.getSystemProperty("emailerReviewButton")+"\"></a>");
			}
			content.append("</td></tr></table></td></tr>");
			content.append("<tr><td><table width=\"400\" align=\"center\" style=\"border-collapse:collapse; height:auto !important; color:#000;\"><tr>");
			content.append("<td style=\"padding-top: 10px;padding-bottom: 10px;line-height: 20px;font-size: 14px;\">Thanks<br>");
			content.append("<span style=\"display: block;\">Team Paymnet Gateway</span></td></tr>");
			content.append("<tr><td align=\"right\" style=\"font-size: 12px;padding-top: 15px;\">&copy; 2020 www.paymnetgateway.com All rights reserved.</td></tr>");
			content.append("</table></td></tr></tbody></table></body></html>");
			body = content.toString();
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}
}
