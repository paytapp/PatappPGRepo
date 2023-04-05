package com.paymentgateway.commons.email;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.EmailData;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.BitlyUrlShortener;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.EmailerConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MPAStatusEmail;
import com.paymentgateway.commons.util.MailersObject;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class EmailServiceProvider {
	private Logger logger;
	private String body;
	private String subject;
	private String toEmail;
	private String emailToBcc;
	private boolean emailExceptionHandlerFlag;
	private StringBuilder responseMessage;

	@Autowired
	@Qualifier("propertiesManager")
	private PropertiesManager propertiesManager;

	@Autowired
	@Qualifier("userDao")
	private UserDao userDao;

	@Autowired
	private MerchantPaymentAdviseReportpdfAutoSend merchantPaymentAdviseReportpdfAutoSend;


	@Autowired
	private BitlyUrlShortener bitlyUrlShortener;
	
	@Autowired
	private PepipostEmailSender pepipostEmailSender;

	public EmailServiceProvider() {
		logger = LoggerFactory.getLogger(EmailServiceProvider.class.getName());
		responseMessage = new StringBuilder();
	}

	public void emailValidator(final ResponseObject responseObject) {
		try {
			
				body = accountValidation(responseObject.getAccountValidationID(),
						propertiesManager.getEmailProperty(Constants.EMAIL_VALIDATORURL.getValue()), "");
				subject = "Verify Your Account | Payment Gateway Solution Private Limited";
			
			toEmail = responseObject.getEmail();
			setEmailExceptionHandlerFlag(true);
			pepipostEmailSender.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
		} catch (Exception exception) {
			logger.error("Exception", (Throwable) exception);
		}
	}

	@SuppressWarnings("static-access")
	public String accountValidation(String accountValidationID, String url, String batuaPage) {

		String content = null;
		StringBuilder body = new StringBuilder();
		try {
			String logoUrl = "";
			
				logoUrl = propertiesManager.propertiesMap.get("logoForEmail");
					
			body.append(
					"<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
			body.append("<title>Your Transaction is Successful</title>");
			body.append("<style>body{font-family: Arial, Helvetica, sans-serif;}</style></head>");
			body.append(
					"<body><table width=\"450\"  height=\"100%\" bgcolor=\"#f5f5f5\" align=\"center\" style=\"padding: 20px;padding-top: 5px;padding-left: 5px\">");
			body.append("<tbody><tr>");
			body.append("<td align=\"center\"><img style=\"padding-top: 5px; width: 150px; height: 109px\" src="
					+ logoUrl + " alt=\"\" ></td></tr><br>");
			body.append("<tr align=\"left\"><td>");
			body.append("Dear Partner,<br/><br/>");
			body.append("Congratulations!</td>");
			body.append(
					"</tr><tr><td style=\"padding-bottom:15px; padding-top:15px\">You have successfully registered with us. Please verify and activate your account.<br /></td></tr>");
			body.append("<tr style=\"text-align:center;\"><td><a href=\"" + url + "?id=" + accountValidationID
					+ "\"><button type=\"button\" style=\"border: none; color: white; background-color:blue; padding: 8px 16px; text-align: center "
					+ "text-decoration: none; display: inline-block; font-size:14px; margin: 4px 2px; cursor: pointer;\">Click to Verify</button></a></td></tr>");
				body.append(
						"<tr><td style=\"padding-top:15px\">Please connect with us at <a href = \"mailto:support@PaymentGateway.com\">support@PaymentGateway.com</a> for any further information.</td></tr>");
			body.append(
					"<tr><td><table width=\"400\" align=\"left\" style=\"border-collapse:collapse; height:auto !important; color:#000;\">");
			body.append(
					"<tr><td style=\"padding-top: 10px;padding-bottom: 10px;line-height: 20px;font-size: 14px;\">--<br>Regards<br>");
				body.append(
						"<span style=\"display: block;\">Team Payment Gateway</span></td></tr></table></td></tr></table></body></html>");
			content = body.toString();

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return content;
	}

	

	

	public void addUser(final ResponseObject responseObject, final String firstName, final String fullName) {
		try {
			body = addUserr(firstName, responseObject,
					propertiesManager.getResetPasswordProperty(Constants.RESET_PASSWORD_URL.getValue()));
			if (!StringUtils.isEmpty(responseObject.getUserType())
					&& responseObject.getUserType().equalsIgnoreCase("SUBADMIN")) {
				subject = "Sub-Admin Creation | Account Validation | " + responseObject.getName();
			} else if (!StringUtils.isEmpty(responseObject.getUserType())
					&& responseObject.getUserType().equalsIgnoreCase("AGENT")) {
				subject = "Agent Creation | Account Validation | " + responseObject.getName();
			} else if (!StringUtils.isEmpty(responseObject.getUserType())
					&& responseObject.getUserType().equalsIgnoreCase("SUBUSER")) {
				subject = "Sub-User Creation | Account Validation | " + responseObject.getName();
			}
			toEmail = responseObject.getEmail();
			setEmailExceptionHandlerFlag(false);
			pepipostEmailSender.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
		} catch (Exception exception) {
			logger.error("Exception", (Throwable) exception);
		}
	}

	public String addUserr(String firstName, ResponseObject responseObject, String url) {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {
			content.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">"
					+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
					+ "<title>Invoice</title><style>body{font-family: Arial, Helvetica, sans-serif;}</style></head>");
			content.append("<body>");
			content.append(
					"<table width=\"450\"  height=\"100%\" bgcolor=\"#f5f5f5\" align=\"center\" style=\"padding: 20px;padding-top: 5px\">");
			content.append("<tbody><tr>");
			content.append("<td align=\"center\"><img src=\"" + propertiesManager.getSystemProperty("emailerLogoURL")
					+ "\" alt=\"\" ></td></tr>");
			content.append(
					"<tr><td><h2 style=\"margin: 0;font-size: 15px;font-weight: 400;color: #8a8a8a;margin-bottom: 10px;\">Dear "
							+ firstName + "</td></tr>");
			if (!StringUtils.isEmpty(responseObject.getUserType())
					&& responseObject.getUserType().equalsIgnoreCase("SUBADMIN"))
				content.append(
						"<tr><td><h2 style=\"margin: 0;font-size: 15px;font-weight: 400;color: #8a8a8a;margin-bottom: 10px;\">Your Sub-Admin account has been created </td></tr>");
			if (!StringUtils.isEmpty(responseObject.getUserType())
					&& responseObject.getUserType().equalsIgnoreCase("SUBUSER"))
				content.append(
						"<tr><td><h2 style=\"margin: 0;font-size: 15px;font-weight: 400;color: #8a8a8a;margin-bottom: 10px;\">Your Sub-User account has been created </td></tr>");
			if (!StringUtils.isEmpty(responseObject.getUserType())
					&& responseObject.getUserType().equalsIgnoreCase("AGENT"))
				content.append(
						"<tr><td><h2 style=\"margin: 0;font-size: 15px;font-weight: 400;color: #8a8a8a;margin-bottom: 10px;\">Your Agent account has been created </td></tr>");
			content.append(
					"<tr><td><table width=\"400\" align=\"center\" bgcolor=\"#fff\" cellpadding=\"15\" style=\"border-collapse:collapse;background-color: #fff\">");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Creation Date:</td>");
			String date = responseObject.getCreationDate();
			if (StringUtils.isBlank(date)) {
				date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
			}
			content.append("<td>" + date + "</td></tr>");
			String setPinImage = "data:image/png;base64,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/GMzLdsAADoTaVRYdFhNTDpjb20uYWRvYmUueG1wAAAAAAA8P3hwYWNrZXQgYmVnaW49Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/Pgo8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4OnhtcHRrPSJBZG9iZSBYTVAgQ29yZSA1LjUtYzAxNCA3OS4xNTE0ODEsIDIwMTMvMDMvMTMtMTI6MDk6MTUgICAgICAgICI+CiAgIDxyZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyI+CiAgICAgIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICAgICAgICAgIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyIKICAgICAgICAgICAgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iCiAgICAgICAgICAgIHhtbG5zOnN0RXZ0PSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VFdmVudCMiCiAgICAgICAgICAgIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyIKICAgICAgICAgICAgeG1sbnM6cGhvdG9zaG9wPSJodHRwOi8vbnMuYWRvYmUuY29tL3Bob3Rvc2hvcC8xLjAvIgogICAgICAgICAgICB4bWxuczp0aWZmPSJodHRwOi8vbnMuYWRvYmUuY29tL3RpZmYvMS4wLyIKICAgICAgICAgICAgeG1sbnM6ZXhpZj0iaHR0cDovL25zLmFkb2JlLmNvbS9leGlmLzEuMC8iPgogICAgICAgICA8eG1wOkNyZWF0b3JUb29sPkFkb2JlIFBob3Rvc2hvcCBDQyAoV2luZG93cyk8L3htcDpDcmVhdG9yVG9vbD4KICAgICAgICAgPHhtcDpDcmVhdGVEYXRlPjIwMjAtMDctMjRUMTE6MzU6MzkrMDU6MzA8L3htcDpDcmVhdGVEYXRlPgogICAgICAgICA8eG1wOk1ldGFkYXRhRGF0ZT4yMDIwLTA3LTI0VDExOjM1OjM5KzA1OjMwPC94bXA6TWV0YWRhdGFEYXRlPgogICAgICAgICA8eG1wOk1vZGlmeURhdGU+MjAyMC0wNy0yNFQxMTozNTozOSswNTozMDwveG1wOk1vZGlmeURhdGU+CiAgICAgICAgIDx4bXBNTTpJbnN0YW5jZUlEPnhtcC5paWQ6ODA3NGJhYmUtZDAxNC1iYTQ4LWFjZTMtNzEyYTZlZDA0YTRiPC94bXBNTTpJbnN0YW5jZUlEPgogICAgICAgICA8eG1wTU06RG9jdW1lbnRJRD54bXAuZGlkOmNlYjM5NGYyLWNiNjctZDE0MS05ZTI3LWIzOGRiYjc0MGYwYzwveG1wTU06RG9jdW1lbnRJRD4KICAgICAgICAgPHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD54bXAuZGlkOmNlYjM5NGYyLWNiNjctZDE0MS05ZTI3LWIzOGRiYjc0MGYwYzwveG1wTU06T3JpZ2luYWxEb2N1bWVudElEPgogICAgICAgICA8eG1wTU06SGlzdG9yeT4KICAgICAgICAgICAgPHJkZjpTZXE+CiAgICAgICAgICAgICAgIDxyZGY6bGkgcmRmOnBhcnNlVHlwZT0iUmVzb3VyY2UiPgogICAgICAgICAgICAgICAgICA8c3RFdnQ6YWN0aW9uPmNyZWF0ZWQ8L3N0RXZ0OmFjdGlvbj4KICAgICAgICAgICAgICAgICAgPHN0RXZ0Omluc3RhbmNlSUQ+eG1wLmlpZDpjZWIzOTRmMi1jYjY3LWQxNDEtOWUyNy1iMzhkYmI3NDBmMGM8L3N0RXZ0Omluc3RhbmNlSUQ+CiAgICAgICAgICAgICAgICAgIDxzdEV2dDp3aGVuPjIwMjAtMDctMjRUMTE6MzU6MzkrMDU6MzA8L3N0RXZ0OndoZW4+CiAgICAgICAgICAgICAgICAgIDxzdEV2dDpzb2Z0d2FyZUFnZW50PkFkb2JlIFBob3Rvc2hvcCBDQyAoV2luZG93cyk8L3N0RXZ0OnNvZnR3YXJlQWdlbnQ+CiAgICAgICAgICAgICAgIDwvcmRmOmxpPgogICAgICAgICAgICAgICA8cmRmOmxpIHJkZjpwYXJzZVR5cGU9IlJlc291cmNlIj4KICAgICAgICAgICAgICAgICAgPHN0RXZ0OmFjdGlvbj5zYXZlZDwvc3RFdnQ6YWN0aW9uPgogICAgICAgICAgICAgICAgICA8c3RFdnQ6aW5zdGFuY2VJRD54bXAuaWlkOjgwNzRiYWJlLWQwMTQtYmE0OC1hY2UzLTcxMmE2ZWQwNGE0Yjwvc3RFdnQ6aW5zdGFuY2VJRD4KICAgICAgICAgICAgICAgICAgPHN0RXZ0OndoZW4+MjAyMC0wNy0yNFQxMTozNTozOSswNTozMDwvc3RFdnQ6d2hlbj4KICAgICAgICAgICAgICAgICAgPHN0RXZ0OnNvZnR3YXJlQWdlbnQ+QWRvYmUgUGhvdG9zaG9wIENDIChXaW5kb3dzKTwvc3RFdnQ6c29mdHdhcmVBZ2VudD4KICAgICAgICAgICAgICAgICAgPHN0RXZ0OmNoYW5nZWQ+Lzwvc3RFdnQ6Y2hhbmdlZD4KICAgICAgICAgICAgICAgPC9yZGY6bGk+CiAgICAgICAgICAgIDwvcmRmOlNlcT4KICAgICAgICAgPC94bXBNTTpIaXN0b3J5PgogICAgICAgICA8ZGM6Zm9ybWF0PmltYWdlL3BuZzwvZGM6Zm9ybWF0PgogICAgICAgICA8cGhvdG9zaG9wOkNvbG9yTW9kZT4zPC9waG90b3Nob3A6Q29sb3JNb2RlPgogICAgICAgICA8cGhvdG9zaG9wOklDQ1Byb2ZpbGU+c1JHQiBJRUM2MTk2Ni0yLjE8L3Bob3Rvc2hvcDpJQ0NQcm9maWxlPgogICAgICAgICA8dGlmZjpPcmllbnRhdGlvbj4xPC90aWZmOk9yaWVudGF0aW9uPgogICAgICAgICA8dGlmZjpYUmVzb2x1dGlvbj43MjAwMDAvMTAwMDA8L3RpZmY6WFJlc29sdXRpb24+CiAgICAgICAgIDx0aWZmOllSZXNvbHV0aW9uPjcyMDAwMC8xMDAwMDwvdGlmZjpZUmVzb2x1dGlvbj4KICAgICAgICAgPHRpZmY6UmVzb2x1dGlvblVuaXQ+MjwvdGlmZjpSZXNvbHV0aW9uVW5pdD4KICAgICAgICAgPGV4aWY6Q29sb3JTcGFjZT4xPC9leGlmOkNvbG9yU3BhY2U+CiAgICAgICAgIDxleGlmOlBpeGVsWERpbWVuc2lvbj45NTwvZXhpZjpQaXhlbFhEaW1lbnNpb24+CiAgICAgICAgIDxleGlmOlBpeGVsWURpbWVuc2lvbj40MTwvZXhpZjpQaXhlbFlEaW1lbnNpb24+CiAgICAgIDwvcmRmOkRlc2NyaXB0aW9uPgogICA8L3JkZjpSREY+CjwveDp4bXBtZXRhPgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgIAo8P3hwYWNrZXQgZW5kPSJ3Ij8+zwa+QQAAACBjSFJNAAB6JQAAgIMAAPn/AACA6QAAdTAAAOpgAAA6mAAAF2+SX8VGAAAGvUlEQVR42uybfWxVRRrGf+/cG2JRQdtKrNBdbNEENVjBCBqjde/GFRFp/Ip8VN2I0N2shEBMtRJEo+wS425dLbBFV9CSKipBQTEGpcSPRY1rMdRFtrJ0EZvG3uIXiHh7xj96zj1n5p5zUVO62eacfybPzNyZ93nnmafTufeI1hqAlS9sG9f80rbp7R37xvdmnKQWlIhyNG4pKEE5AIgCAOWWIoCAJExstyuvn10qH4tfLyKOFqVMLEpE3LgSJibhmHF5ZcIdXqEBkQQ6Anuf0wiiXD4qGL8K8nNAFCIOKIWSMAyiSCbVkTEjh304fUJJ820VRTsBRGvN3GWrZ2x4peU2RCqDyRLlBSd9wdhlaDKViUUiMIGkKxMrMZPnJSHYT9yk4SZRQLBwzrhh8/iLL8rjp7L8NMqvt5MvyuKTCMH4ouybf0vVpFGPL7/iF08nTj3vsnErmzbcjVApiJt7j1wAu6W4wYuRjCjlW4sTqfx8i6WyZMRty4nLXSzBi0vcIcRYLBv7vLxxA9gVmbhxSFi8yhJdXpzlU7brs2+GDTupoDXx1dDSBekDX870k9bXz5vcr+4jpK3SG9TGLlszmdjKDwZHRKldtQXqETN5HsbbqWR3qDmPFZ+FxeAdstPt+Akq347TxTki1SCqvPNg5gv1Sce+igAfg4z/+IEKZhno4U+WrRQLRwIfixGFr9osFnNYMWMN7ZNdHLEWSyy+YkWlAjkhIicEFiLYKVww3kJ80vn1OJXJ9A6BvgXRbjcdBEajdqv90nwcE2qrXUcCH2uCUYB2LKzNYbUZq7a4BOO3+RlsDH4+Hx0RbW6lE4G1mUOXT6ZXH6fCFjJU+eRXPqHK52co38bqKKomV/k5o4fvZIOFhMUUNV5YpTrKTsjh46gccUcpn/zK16HK52co39KadsxNYSvfGiobX8TONXeCNnZLmL61taMIS1io0l0cxsc8qMeeP9Ce/9OUH3t+v3r+T1N+7Pmx58eeH3t+7Pmx58eeH3v+/6PnJ21xaFfXur89/+zLWfWHSaQKh0Av4Byi9a3XmbPqn3Tn8XwtY7mzbiz/+eOLrAO0eBd7/hy1989j/ogMhzxeqpf0f/dyz6oWNqeBmdfReWYXp937NhqoXXwL1QfepbLhY7oDfKf9/lrmptdz5bp+9Hwsz/dz5CSNhZSg8oODe1i7t3t+aUbgmAugtZvM0SypuYTSHc9wQWMb3SIwsoL5Fw51E0922f0h++ZAn0D56UUB7JZi/l1pfWM5k5u8+/xTuGHBNB6tmcjEB97JzhEUatHZFSyt/JQ5LQd9vjmP03efDzlMgyH7Sk+EYG0l2PGu3gfK80sYeXI3LY07/WTv30H989tdUEZN3TzamurY03QXbfXXU1UowK9Z/8RlTD3+FOY8vpA9j1UzP8zzc6ZMs+4v7ewuOZUpEf0+bktzUVWK2vJIux8snt9Je1cx1fdNITVmeI4OU/Ov4faCncyctZSyWQ3M3VnIA3dcwVls4ZrfbmXjN5/TeOtDlM1+ivowzyfE8wuTFOQ5nnzb/SqLdhcwZ/okzo08zgyKc/5eli1t5sH0aP60eCF71tSyue5qJo8CmMCN5xzh2fqttALIV7z52GY2FZRxyxn+dzB5z/m2GIvPoGb2aIr3drDGaDP7vrD8DRopo3F2eYS0j+05f4A8H0i3sbr+I1aLwKjzuHlGioZ7b2bBrT2UDi9k6sp7qDGIH2ZjKfDv4Lk/2vMrUvPoTPVVHTr8Hbv/9RGzGlqPcvjaz7Ln9lBZM57GS7p4eYA9/8efdvrznL+/lTUPHuSsFTcyJdXGvi972F77CEt6wr7DNT+ec9pxn9bX/srkpoT1na0i3yMIetd2qjecQMtVF3GgYzB6ftFvWPvQdKrKT8o2FY8/l0lDe3jvg/dp+ABuqpvGxUWukkddyJLZ51MMoDOQKeC0C0AzjOKTf6TnH/1f0iyL7m3vsKhjODeNHTIIz/np7ax4bxqLFi3kz8d5rtLDs2ufpLEHaHiau26/jr89XEdBBnC+ZtP650gDyA4a3j2f9b9byFVzM/xj46PM2mie83PEKBKqVM8obSwcYkPDm1Qs/hUT+9vz85zzZcSl1VsRVWkQiX+3cyx/t+Px2RLf7cR3O/F9fnyfH9/nx/f5sefHnh97fuz5sefHnh97fuz5g8Tzk8nEkdjzB97zkwk5rMp/Wdoae/7Ae355yYkfqhlXp9YCLbHnD6jnvzpjwojmxKbmJ7rau774flf73iJERgejid/JOibvZG2pmjjy74svLnld4vdw/3fv4f4wABmXS4ZgLrfWAAAAAElFTkSuQmCC";
			content.append("<tr align=\"center\"><td colspan=\"2\">click on the below link to set the pin</td></tr>");
			content.append("<tr align=\"center\"><td colspan=\"2\" style=\"padding: 20px 0\">");

			content.append("<a href='" + url + "?id=" + responseObject.getAccountValidationID()
					+ "' style=\"padding: 8px 27px;height: 30px;display:inline-block;font-size: 12px;color: #fff;font-weight: 600;border-radius: 5px;text-decoration: none;\">");
			
			content.append("<img src=\"" + setPinImage + "\" \"alt=\"Set PIN\"></a>");
			content.append("</td></tr></table></td></tr>");
			content.append(
					"<tr><td><table width=\"400\" align=\"center\" style=\"border-collapse:collapse; height:auto !important; color:#000;\"><tr>");
			content.append(
					"<td style=\"padding-top: 10px;padding-bottom: 10px;line-height: 20px;font-size: 14px;\">Thanks<br>");
			content.append("<span style=\"display: block;\">Team Payment Gateway</span></td></tr>");
			content.append(
					"<tr><td align=\"right\" style=\"font-size: 12px;padding-top: 15px;\">&copy; 2020 www.PaymentGateway.com All rights reserved.</td></tr>");
			content.append("</table></td></tr></tbody></table></body></html>");
			body = content.toString();

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}

	

	public void passwordChange(final ResponseObject responseObject, final String emailId) {
		try {
			body = passwordUpdate();
			subject = EmailerConstants.COMPANY.getValue() + Constants.CRM_PASSWORD_CHANGE_ACKNOWLEDGEMENT.getValue();
			toEmail = emailId;
			setEmailExceptionHandlerFlag(false);
			pepipostEmailSender.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
		} catch (Exception exception) {
			logger.error("Exception", (Throwable) exception);
		}
	}

	public String passwordUpdate() {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {
			makeHeader(content);
			content.append("<tr>");
			content.append("<td align='left' style='font:normal 14px Arial;'><p><em>Dear User,<br /><br />");
			content.append("Your CRM password has been updated successfully.<br />");
			content.append("</p>");
			makeFooter(content);
			body = content.toString();
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}

	private StringBuilder makeHeader(StringBuilder content) {
		// StringBuilder content = new StringBuilder();
		content.append("<html xmlns='http://www.w3.org/1999/xhtml'>");
		content.append("<head>");
		content.append("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>");
		content.append("<title>Payment Acknowledgement</title>");
		content.append("<style>");
		content.append("table.product-spec{border:1px solid #eaeaea; border-bottom:1px solid");
		content.append(
				"#dedede;font-family:Arial,Helvetica,sans-serif;background:#eeeeee}table.product-spec th{font-size:16px;");
		content.append(
				"font-weight:bold;padding:5px;border-right:1px solid #dedede; border-bottom:1px solid #dedede; background:#0271bb;");
		content.append(
				"color:#ffffff;}table.product-spec td{font-size:12px;padding:6px; border-right:1px solid #dedede; border-bottom:1px solid");
		content.append("#dedede;background-color:#ffffff;}");
		content.append(
				"table.product-spec td p { font:normal 12px Arial; color:#6f6f6f; padding:0px; margin:0px; line-height:12px; }");
		content.append("</style>");
		content.append("</head>");
		content.append("<body>");
		content.append("<br /><br />");
		content.append(
				"<table width='700' border='0' align='center' cellpadding='7' cellspacing='0' style='border:1px solid #d4d4d4;");
		content.append("background-color:#ffffff; border-radius:10px;'>");
		content.append("<tr>");
		content.append(
				"<td height='60' align='center' valign='middle' bgcolor='#fbfbfb' style='border-bottom:1px solid #d4d4d4;");
		content.append("border-top-left-radius:10px; border-top-right-radius:10px;'><img src='"
				+ propertiesManager.getSystemProperty("logoForEmail") + "'/></td>");
		content.append("</tr>");
		return content;
	}

	private StringBuilder makeFooter(StringBuilder content) {
		// StringBuilder content = new StringBuilder();
		content.append("<p><em>Assuring you of our best services at all times.</em></p>");
		content.append(
				"<p><em>If you have any questions about your transaction or any other matter, please feel free to contact us at ");
		content.append(EmailerConstants.CONTACT_US_EMAIL.getValue());
		content.append(" or by phone at ");
		content.append(EmailerConstants.PHONE_NO.getValue());
		content.append(".</em></p>");
		content.append("<p></p></td>");
		content.append("</tr>");
		content.append("<tr>");
		content.append(
				"<td align='left' valign='middle' bgcolor='#fbfbfb' style='border-top:1px solid #d4d4d4; border-bottom-left-radius:10px;");
		content.append(
				"border-bottom-right-radius:10px;'><table width='100%' border='0' cellspacing='0' cellpadding='0.'>");
		content.append("<tr>");
		content.append("<td align='left' valign='middle' style='font-family:Arial;'><strong>We Care!<br />");
		content.append(EmailerConstants.GATEWAY.getValue());
		content.append(" Team</strong></td>");
		content.append("<td align='right' valign='bottom' style='font:normal 12px Arial;'>&copy; 2020 ");
		content.append(EmailerConstants.WEBSITE.getValue());
		content.append(" All rights reserved.</td>");
		content.append("</tr>");
		content.append("</table></td>");
		content.append("</tr>");
		content.append("</table>");
		content.append("</body>");
		content.append("</html>");
		return content;
	}

	public void pinChange(final ResponseObject responseObject, final String emailId) {
		try {
			body = passwordUpdate();
			subject = EmailerConstants.COMPANY.getValue() + Constants.CRM_PASSWORD_CHANGE_ACKNOWLEDGEMENT.getValue();
			toEmail = emailId;
			setEmailExceptionHandlerFlag(false);
			pepipostEmailSender.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
		} catch (Exception exception) {
			logger.error("Exception", (Throwable) exception);
		}
	}

	public void pinReset(final String accountValidationKey, final String email) {
		try {
			body = passwordReset(accountValidationKey,
					propertiesManager.getResetPasswordProperty(Constants.RESET_PASSWORD_URL.getValue()));
			subject = Constants.RESET_PASSWORD_EMAIL.getValue() + EmailerConstants.COMPANY.getValue();
			toEmail = email;
			setEmailExceptionHandlerFlag(true);
			pepipostEmailSender.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
		} catch (Exception exception) {
			logger.error("Exception", (Throwable) exception);
		}
	}

	public String passwordReset(String accountValidationID, String url) {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {
			makeHeader(content);
			content.append("<tr>");
			content.append("<td align='left' style='font:normal 14px Arial;'><em><p>Dear User,<br /><br />");
			content.append("There was recently a request to change the password for your account.<br /><br />");
			content.append(
					"If you requested this password change, please click on below button to reset your account password.<br />");
			content.append("</p></em>");
			content.append(
					"<table width='20%' border='0' align='left' cellpadding='0.' cellspacing='0' class='product-spec1'>");
			content.append("<tr>");
			content.append("<td height='30' align='center' valign='middle' bgcolor='#2b6dd1' color='#fff'><a href='"
					+ url + "?id=" + accountValidationID + "'>Reset Password</a></td>");
			content.append("</tr>");
			content.append("</table>");
			content.append("<br />");
			content.append("<br />");
			makeFooter(content);
			body = content.toString();
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}

	public void remittanceProcess(final String utr, final String payId, final String merchant, final String datefrom,
			final String netAmount, final String remittedDate, final String remittedAmount, final String status) {
		try {
			final User user = userDao.getUserClass(payId);
			body = remittanceEmailBody(utr, payId, merchant, datefrom, netAmount, remittedDate, remittedAmount, status);
			subject = EmailerConstants.COMPANY.getValue() + Constants.REMITTANCE_PROCESSED.getValue() + payId;
			toEmail = user.getEmailId();
			setEmailExceptionHandlerFlag(false);
			pepipostEmailSender.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
		} catch (Exception exception) {
			logger.error("Exception", (Throwable) exception);
		}
	}

	public String remittanceEmailBody(String utr, String payId, String merchant, String datefrom, String netAmount,
			String remittedDate, String remittedAmount, String status) {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {
			makeHeader(content);
			content.append("<tr>");
			content.append("<td align='left' style='font:normal 14px Arial;'>");
			content.append("<p><em>Dear Customer,</em></p>");
			content.append(
					"<p><table width='96%' border='0' align='center' cellpadding='0.' cellspacing='0' class='product-spec'>");
			content.append("<tr>");
			content.append("<th colspan='2' align='left' valign='middle'>Remittance Processed Successful. </th>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td width='27%'>Merchant Name :- </td>");
			content.append("<td width='73%'>" + merchant + " </td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>Transaction Date :-  </td>");
			content.append("<td>" + datefrom + "</td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>Remitted Amount :- </td>");
			content.append("<td>" + remittedAmount + "</td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>Remitted Date :- </td>");
			content.append("<td>" + remittedDate + "</td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>UTR :- </td>");
			content.append("<td>" + utr + "</td>");
			content.append("</tr>");
			content.append("</table></p>");
			makeFooter(content);
			body = content.toString();
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}

	public void sendBulkEmailServiceTax(final String emailID, final String subject, String messageBody) {
		try {
			messageBody = serviceTaxUpdate(messageBody);
			pepipostEmailSender.sendEmail(messageBody, subject, Constants.PG_DEMO_EMAIL.getValue(), emailID);
		} catch (Exception exception) {
			logger.error("Exception", (Throwable) exception);
		}
	}

	public String serviceTaxUpdate(String emailContent) {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {
			emailContent = "Hi Merchant";
			content.append("<tr>");
			content.append("<td align='left' style='font:normal 14px Arial;'><p><em>Dear Merchant,<br /><br />");
			content.append(
					"<table width='20%' border='0' align='left' cellpadding='0.' cellspacing='0' class='product-spec1'>");
			content.append("<tr>");
			content.append("<br /><br />" + emailContent + "</em></p>");
			content.append("</tr>");
			makeHeader(content);
			content.append("</table>");
			content.append("</td>");
			content.append("</tr>");
			content.append("<br /><br />");
			makeFooter(content);
			body = content.toString();

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}

	public Map<String, String> sendPaymentAdviseEmail(Object subMerchantPayId, Object merchantPayId,
			Object sessionUserPayId, Object payoutDate, Object currency) {
		String msg = "success";
		Map<String, String> responseMap = new HashMap<String, String>();
		try {
			Runnable runnable = new Runnable() {

				@Override
				public void run() {
					try {
						merchantPaymentAdviseReportpdfAutoSend.getpdfFileForEmail(String.valueOf(subMerchantPayId),
								String.valueOf(merchantPayId), String.valueOf(sessionUserPayId),
								String.valueOf(payoutDate), String.valueOf(currency));
					} catch (Exception e) {
						logger.error(
								"Exception in runnable class inside sendPaymentAdvieReport method and EmailBuilder class ",
								e);
					}
				}
			};
			propertiesManager.executorImpl(runnable);
			responseMap.put("response", msg);
		} catch (Exception exception) {
			responseMap.put("response", "Exception");
			logger.error("Exception", exception);
		}
		return responseMap;
	}

	public void emailPendingRequest(final String emailID, final String subject, String messageBody,
			final String requestStatus, final String loginEmailId, final String userType, final String merchantName,
			final String makerName, final String checkerName, final String date) {
		try {
			messageBody = emailPendingRequest(merchantName, makerName, checkerName, date, requestStatus, userType,
					messageBody);
			pepipostEmailSender.sendEmail(messageBody, subject, emailID, Constants.PG_DEMO_EMAIL.getValue());
		} catch (Exception exception) {
			logger.error("Exception", (Throwable) exception);
		}
	}

	public String emailPendingRequest(String merchantName, String makerName, String checkerName, String requestDate,
			String status, String userType, String description) {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {

			content.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">"
					+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
					+ "<title>Invoice</title><style>body{font-family: Arial, Helvetica, sans-serif;}</style></head>");
			content.append("<body>");
			content.append(
					"<table width=\"300\"  height=\"100%\" bgcolor=\"#f5f5f5\" align=\"center\" style=\"padding: 20px;padding-top: 5px\">");
			content.append("<tbody><tr>");
			content.append("<td align=\"center\"><img src=\"" + propertiesManager.getSystemProperty("emailerLogoURL")
					+ "\" alt=\"\" ></td></tr>");

			content.append(
					"<tr><td><h2 style=\"margin: 0;font-size: 15px;font-weight: 400;color: #8a8a8a;margin-bottom: 10px;\">Dear ");
			content.append(checkerName);
			content.append(",</td></tr>");
			content.append(
					"<tr><td><table width=\"300\" align=\"center\" bgcolor=\"#fff\" cellpadding=\"15\" style=\"border-collapse:collapse;background-color: #fff\">");

			if (StringUtils.isNotBlank(merchantName)) {
				content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
				content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Merchant Name:</td>");
				content.append("<td>" + merchantName + "</td></tr>");
			}

			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Maker:</td>");
			content.append("<td>" + makerName + "</td></tr>");

			if (StringUtils.isNotBlank(merchantName)) {
				content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
				content.append(
						"<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Checker:</td>");
				content.append("<td>" + checkerName + "</td></tr>");
			}

			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Status:</td>");
			content.append("<td>" + status + "</td></tr>");

			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append(
					"<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Create Date :</td>");
			content.append("<td>" + requestDate + "</td></tr>");

			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Description:</td>");
			content.append("<td>" + description + "</td></tr></table></td></tr>");

			content.append(
					"<tr><td><table width=\"400\" align=\"center\" style=\"border-collapse:collapse; height:auto !important; color:#000;\"><tr>");
			content.append(
					"<td style=\"padding-top: 10px;padding-bottom: 10px;line-height: 20px;font-size: 14px;\">Thanks,<br>");
			content.append("<span style=\"display: block;\">Team Payment Gateway</span></td></tr>");

			content.append(
					"<tr><td align=\"right\" style=\"font-size: 12px;padding-top: 15px;\">&copy; 2020 www.PaymentGateway.com All rights reserved.</td></tr>");
			content.append("</table></td></tr></tbody></table></body></html>");

			body = content.toString();
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}

	public void mpaStatusEmailSender(final MPAStatusEmail mpaStatusEmail) {
		String[] emailIdArray = mpaStatusEmail.getToEmail().split(",");

		try {
			for (int i = 0; i < emailIdArray.length; i++) {

				/*
				 * emailer.sendEmail(mpaStatusEmail.getMessageBody(),
				 * mpaStatusEmail.getSubject(), emailIdArray[i], "",
				 * isEmailExceptionHandlerFlag()); // Sending
				 */
				pepipostEmailSender.sendEmail(mpaStatusEmail.getMessageBody(), mpaStatusEmail.getSubject(),
						emailIdArray[i], "");
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void multipleUsersEmailSender(final MailersObject mailersObject) throws Exception {
		List<String> emailIdList = mailersObject.getToEmailList();

		try {
			for (String emailId : emailIdList) {

				/*
				 * emailer.sendEmail(mailersObject.getMessageBody(), mailersObject.getSubject(),
				 * emailId, "", isEmailExceptionHandlerFlag()); // Sending
				 */
				pepipostEmailSender.sendEmail(mailersObject.getMessageBody(), mailersObject.getSubject(), emailId, "");
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	// Preparing Email for Demo EMandate Sign
	public Map<String, String> eMandateSignForAPI(Fields requestField) throws Exception {
		String response = null;
		Map<String, String> responseMap = new HashMap<String, String>();

		Map<String, String> emailBody = new HashMap<String, String>();
		try {
			emailBody = eMandateSignBody(requestField,
					userDao.getBusinessNameByPayId(requestField.get(FieldType.PAY_ID.getName())));
			body = emailBody.get("body");
			subject = "E-Mandate Sign";
			toEmail = requestField.get("CUST_EMAIL");
			setEmailExceptionHandlerFlag(false);
			/*
			 * response = emailer.sendEmail(getBody(), getSubject(), getToEmail(),
			 * getEmailToBcc(), isEmailExceptionHandlerFlag());
			 */
			response = pepipostEmailSender.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
			if (response != null) {
				responseMap.put("SEND_EMAIL", ErrorType.SUCCESS.getInternalMessage());
			} else {
				responseMap.put("SEND_EMAIL", ErrorType.EMAIL_ERROR.getInternalMessage());
			}

		} catch (Exception exception) {
			logger.error("Exception", exception);
			responseMap.put("SEND_EMAIL", ErrorType.EMAIL_ERROR.getInternalMessage());
		}
		responseMap.put(FieldType.EMANDATE_URL.getName(), emailBody.get(FieldType.EMANDATE_URL.getName()));
		return responseMap;
	}

	// Email for crm EMandate Sign
	public Map<String, String> eMandateSignMail(Fields requestField) throws Exception {
		String response = null;
		Map<String, String> responseMap = new HashMap<String, String>();

		Map<String, String> emailBody = new HashMap<String, String>();
		try {
			emailBody = eMandateSignBody(requestField,
					userDao.getBusinessNameByPayId(requestField.get(FieldType.PAY_ID.getName())));
			body = emailBody.get("body");
			subject = "E-Mandate Sign";
			toEmail = requestField.get("CUST_EMAIL");
			setEmailExceptionHandlerFlag(false);
			/*
			 * response = emailer.sendEmail(getBody(), getSubject(), getToEmail(),
			 * getEmailToBcc(), isEmailExceptionHandlerFlag());
			 */
			response = pepipostEmailSender.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
			
		} catch (Exception exception) {
			logger.error("Exception", exception);			
		}
		responseMap.put(FieldType.EMANDATE_URL.getName(), bitlyUrlShortener.createShortUrlUsingBitly(emailBody.get(FieldType.EMANDATE_URL.getName())));
		responseMap.put("responseBody", response);
		return responseMap;
	}

	public String eMandateSign(Fields requestField) throws Exception {
		String response = null;
		Map<String, String> emailBody = new HashMap<String, String>();
		try {
			emailBody = eMandateSignBody(requestField,
					userDao.getBusinessNameByPayId(requestField.get(FieldType.PAY_ID.getName())));
			body = emailBody.get("body");
			subject = "E-Mandate Sign";
			toEmail = requestField.get("CUST_EMAIL");
			setEmailExceptionHandlerFlag(false);
			/*
			 * response = emailer.sendEmail(getBody(), getSubject(), getToEmail(),
			 * getEmailToBcc(), isEmailExceptionHandlerFlag());
			 */
			response = pepipostEmailSender.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return response;
	}

	public String reSendEMandate(Map<String, String> requestMap) throws Exception {
		String response = null;
		Map<String, String> emailBody = new HashMap<String, String>();
		try {
			emailBody = eMandateSignBody(new Fields(requestMap),"");
			body = emailBody.get("body");
			//body = eMandateSignBodyWithUrl(requestMap.get(FieldType.EMANDATE_URL.getName()));
			subject = "E-Mandate Sign";
			toEmail = requestMap.get(FieldType.CUST_EMAIL.getName());
			setEmailExceptionHandlerFlag(false);
			
			response = pepipostEmailSender.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return response;
	}

	@SuppressWarnings("static-access")
	public Map<String, String> eMandateSignBody(Fields requestField, String merchantName) {
		String body = null;
		String eMandateURL = null;
		Map<String, String> response = new HashMap<String, String>();
		StringBuilder content = new StringBuilder();
		try {
			content.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">"
					+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
					+ "<title>E-Nach Mandate</title><style>body{font-family: Arial, Helvetica, sans-serif;}</style></head>");
			content.append("<body>");
			content.append(
					"<table width=\"450\"  height=\"100%\" bgcolor=\"#f5f5f5\" align=\"center\" style=\"padding: 0px;padding-top: 0px\">");
			content.append("<tbody><tr>");

			content.append(
					"<body bgcolor=\"#ccc\"><table width=\"700\" border=\"0\" border-collapse=\"collapse\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"#f5f5f5\" align=\"center\">"
							+ " <tbody> <tr> <td width=\"70%\" bgcolor=\"#002664\" style=\"padding: 10px;color: #fff;font-size: 24px;\">E-mail notification</td>"
							+ "<td width=\"30%\" bgcolor=\"#fff\" style=\"padding: 10px;\" align=\"center\">");
			content.append("<img src=\"" + propertiesManager.getSystemProperty("emailerLogoURL")
					+ "\" alt=\"/\" width=\"150\"> </td></tr><tr> <td colspan=\"2\" style=\"padding: 10px;\">"
					+ " <h2 style=\"margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;margin-top: 10px;\">Dear Customer,"
					+ "<br>This is eNACH Registration Mail </h2> </td></tr>");

			content.append(
					"<tr><td colspan=\"2\" align=\"center\"><table width=\"400\" align=\"center\" bgcolor=\"#fff\" cellpadding=\"15\" style=\"border-collapse:collapse;background-color: #fff\">");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600;color: #888888;\">Creation Date:</td>");
			content.append("<td align=\"right\">" + DateCreater.defaultFromDate() + "</td></tr>");
			content.append(
					"<tr align=\"center\"><td colspan=\"2\">Click on the button for eNACH Registration.</td></tr>");
			content.append("<tr align=\"center\"><td colspan=\"2\" style=\"padding: 20px 0\">");

			String url = propertiesManager.propertiesMap.get("DEMO_ENACH_MANDATE_SIGN");
			String returnUrl = requestField.get("RETURN_URL");
			eMandateURL = "" + url + "?ORDER_ID=:" + requestField.get("ORDER_ID") + "," + "?AMOUNT=:"
					+ requestField.get(FieldType.AMOUNT.getName()) + "," + "?MONTHLY_AMOUNT=:"
					+ requestField.get(FieldType.MONTHLY_AMOUNT.getName()) + "," + "?FREQUENCY=:"
					+ requestField.get(FieldType.FREQUENCY.getName()) + "," + "?TENURE=:"
					+ requestField.get(FieldType.TENURE.getName()) + "," + "?PAY_ID=:"
					+ requestField.get(FieldType.PAY_ID.getName()) + "," + "?CUST_MOBILE=:"
					+ requestField.get("CUST_MOBILE") + "," + "?CUST_EMAIL=:"
					+ requestField.get(FieldType.CUST_EMAIL.getName()) + "," + "?HASH=:"
					+ requestField.get(FieldType.HASH.getName()) + "," + "?RETURN_URL=:" + returnUrl + "+";

			content.append("<a href='" + eMandateURL
					+ "' style=\"padding: 8px 27px;height: 30px;display:inline-block;font-size: 12px;color: #fff;font-weight: 600;border-radius: 5px;text-decoration: none;\">");
			content.append(
					"<span style=\"background-color: #002a96; color: #fff; display: inline-block; padding: 3px 10px;width: 120px;text-align: center;height: 35px;line-height: 35px; font-weight: bold; border-radius: 5px;\">Registration</span></a>");
			content.append("</td></tr></table></td></tr>");
			content.append(
					"<tr><td><table width=\"400\" align=\"center\" style=\"border-collapse:collapse; height:auto !important; color:#000;\"><tr>");
			content.append(
					"<tr> <td colspan=\"3\" style=\"padding: 10px;\"> <h2 style=\"margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;"
							+ "margin-top: 10px;\">Thanks<br>Team Payment Gateway</h2> </td></tr><tr> <td colspan=\"3\" style=\"padding: 10px;font-size: 14px;color: #a0a0a0\">"
							+ " &copy; 2021 <a href=\"http://www.PaymentGateway.com\">www.PaymentGateway.com</a> All rights reserved. </td></tr>");
			content.append(
					"<tr> <td colspan=\"3\" style=\"padding: 10px;font-size: 14px;color: #a0a0a0\"> Please reach us at <a href=\"mailto:support@PaymentGateway.com\">"
							+ "support@PaymentGateway.com</a> in case of queries. </td></tr></tbody></table></body></html>");
			body = content.toString();
			response.put("body", body);
			response.put(FieldType.EMANDATE_URL.getName(), eMandateURL);

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return response;
	}
	
	public String eMandateSignBodyWithUrl(String eMandateUrl) {
		String responseBody = null;
		StringBuilder content = new StringBuilder();
		try {
			content.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">"
					+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
					+ "<title>E-Nach Mandate</title><style>body{font-family: Arial, Helvetica, sans-serif;}</style></head>");
			content.append("<body>");
			content.append(
					"<table width=\"450\"  height=\"100%\" bgcolor=\"#f5f5f5\" align=\"center\" style=\"padding: 0px;padding-top: 0px\">");
			content.append("<tbody><tr>");

			content.append(
					"<body bgcolor=\"#ccc\"><table width=\"700\" border=\"0\" border-collapse=\"collapse\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"#f5f5f5\" align=\"center\">"
							+ " <tbody> <tr> <td width=\"70%\" bgcolor=\"#002664\" style=\"padding: 10px;color: #fff;font-size: 24px;\">E-mail notification</td>"
							+ "<td width=\"30%\" bgcolor=\"#fff\" style=\"padding: 10px;\" align=\"center\">");
			content.append("<img src=\"" + propertiesManager.getSystemProperty("emailerLogoURL")
					+ "\" alt=\"/\" width=\"150\"> </td></tr><tr> <td colspan=\"2\" style=\"padding: 10px;\">"
					+ " <h2 style=\"margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;margin-top: 10px;\">Dear Customer,"
					+ "<br>This is eNACH Registration Mail </h2> </td></tr>");

			content.append(
					"<tr><td colspan=\"2\" align=\"center\"><table width=\"400\" align=\"center\" bgcolor=\"#fff\" cellpadding=\"15\" style=\"border-collapse:collapse;background-color: #fff\">");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600;color: #888888;\">Creation Date:</td>");
			content.append("<td align=\"right\">" + DateCreater.defaultFromDate() + "</td></tr>");
			content.append(
					"<tr align=\"center\"><td colspan=\"2\">Click on the button for eNACH Registration.</td></tr>");
			content.append("<tr align=\"center\"><td colspan=\"2\" style=\"padding: 20px 0\">");

			content.append("<a href='" + eMandateUrl
					+ "' style=\"padding: 8px 27px;height: 30px;display:inline-block;font-size: 12px;color: #fff;font-weight: 600;border-radius: 5px;text-decoration: none;\">");
			content.append(
					"<span style=\"background-color: #002a96; color: #fff; display: inline-block; padding: 3px 10px;width: 120px;text-align: center;height: 35px;line-height: 35px; font-weight: bold; border-radius: 5px;\">Registration</span></a>");
			content.append("</td></tr></table></td></tr>");
			content.append(
					"<tr><td><table width=\"400\" align=\"center\" style=\"border-collapse:collapse; height:auto !important; color:#000;\"><tr>");
			content.append(
					"<tr> <td colspan=\"3\" style=\"padding: 10px;\"> <h2 style=\"margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;"
							+ "margin-top: 10px;\">Thanks<br>Team Payment Gateway</h2> </td></tr><tr> <td colspan=\"3\" style=\"padding: 10px;font-size: 14px;color: #a0a0a0\">"
							+ " &copy; 2021 <a href=\"http://www.PaymentGateway.com\">www.PaymentGateway.com</a> All rights reserved. </td></tr>");
			content.append(
					"<tr> <td colspan=\"3\" style=\"padding: 10px;font-size: 14px;color: #a0a0a0\"> Please reach us at <a href=\"mailto:support@PaymentGateway.com\">"
							+ "support@PaymentGateway.com</a> in case of queries. </td></tr></tbody></table></body></html>");
			responseBody = content.toString();
			
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return responseBody;
	}

	// Preparing Email for Demo Upi AutoPay Mandate Sign
	public String upiAutoPayMandateSign(Fields requestField) throws Exception {
		String response = null;
		try {
			body = upiAutoPayMandateSignBody(requestField,
					userDao.getBusinessNameByPayId(requestField.get(FieldType.PAY_ID.getName())));
			subject = "UPI AutoPay Mandate Sign";
			toEmail = requestField.get("CUST_EMAIL");
			setEmailExceptionHandlerFlag(false);
			/*
			 * response = emailer.sendEmail(getBody(), getSubject(), getToEmail(),
			 * getEmailToBcc(), isEmailExceptionHandlerFlag());
			 */
			response = pepipostEmailSender.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return response;
	}
	
	// Preparing Email for Demo Upi AutoPay Mandate Sign
	public Map<String, String> upiAutoPayMandateSignMail(Fields requestField) throws Exception {
		String response = null;
		Map<String, String> responseMap = new HashMap<String, String>();

		Map<String, String> emailBody = new HashMap<String, String>();
		try {
			emailBody = upiAutoPayMandateSignBodyMap(requestField,
					userDao.getBusinessNameByPayId(requestField.get(FieldType.PAY_ID.getName())));
			body = emailBody.get("body");
			subject = "UPI AutoPay Mandate Sign";
			toEmail = requestField.get("CUST_EMAIL");
			setEmailExceptionHandlerFlag(false);
			/*
			 * response = emailer.sendEmail(getBody(), getSubject(), getToEmail(),
			 * getEmailToBcc(), isEmailExceptionHandlerFlag());
			 */
			response = pepipostEmailSender.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
			
		} catch (Exception exception) {
			logger.error("Exception", exception);			
		}
		responseMap.put(FieldType.EMANDATE_URL.getName(), emailBody.get(FieldType.EMANDATE_URL.getName()));
		responseMap.put("responseBody", response);
		return responseMap;
	}

	public boolean upiAutoPayMandateSignThroughApi(Fields requestField) throws Exception {
		boolean response;
		try {
			body = upiAutoPayMandateSignBody(requestField,
					userDao.getBusinessNameByPayId(requestField.get(FieldType.PAY_ID.getName())));
			subject = "UPI AutoPay Mandate Sign";
			toEmail = requestField.get("CUST_EMAIL");
			setEmailExceptionHandlerFlag(false);
			/*
			 * response = emailer.sendEmail(getBody(), getSubject(), getToEmail(),
			 * getEmailToBcc(), isEmailExceptionHandlerFlag());
			 */
			response = pepipostEmailSender.sendEmailThroughApi(getBody(), getSubject(), getToEmail(), getEmailToBcc());
			
			return response;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return false;
		}
	}

	@SuppressWarnings("static-access")
	public String upiAutoPayMandateSignBody(Fields requestField, String merchantName) {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {
			content.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">"
					+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
					+ "<title>UPI AutoPay Mandate</title><style>body{font-family: Arial, Helvetica, sans-serif;}</style></head>");
			content.append("<body bgcolor=\"#ccc\">");

			content.append(
					"<table height=\"100%\" bgcolor=\"#f5f5f5\" align=\"center\" style=\"padding: 0px;padding-top: 0px\">");
			content.append("<tbody><tr>");

			content.append(
					"<table width=\"700\" border=\"0\" border-collapse=\"collapse\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"#f5f5f5\" align=\"center\"><tbody>");

			content.append(
					"<tr><td width=\"70%\" bgcolor=\"#002664\" style=\"padding: 10px;color: #fff;font-size: 24px;\">E-mail notification</td><td width=\"30%\" bgcolor=\"#fff\" style=\"padding: 10px;\" align=\"center\"><img src=\""
							+ propertiesManager.getSystemProperty("emailerLogoURL")
							+ "\" alt=\"/\" width=\"150\"> </td></tr>");

			content.append(
					"<tr><td colspan=\"2\" style=\"padding: 10px;\"><h2 style=\"margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;margin-top: 10px;\">Dear Customer,<br>This is UPI AutoPay Registration Mail </h2></td></tr>");

			content.append(
					"<tr><td colspan=\"2\"><table width=\"400\" align=\"center\" bgcolor=\"#fff\" cellpadding=\"15\" style=\"border-collapse:collapse;background-color: #fff\">");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Creation Date:</td>");
			content.append("<td>" + DateCreater.defaultFromDate() + "</td></tr>");
			content.append(
					"<tr align=\"center\"><td colspan=\"2\">Click on the below button to Upi AutoPay Registration</td></tr>");
			content.append("<tr align=\"center\"><td colspan=\"2\" style=\"padding: 20px 0\">");

			String url = propertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MANDATE_SIGN.getValue());
			String mandate_url = url + "?ORDER_ID=:" + requestField.get(FieldType.ORDER_ID.getName()) + ","
					+ "?AMOUNT=:" + requestField.get(FieldType.AMOUNT.getName()) + "," + "?MONTHLY_AMOUNT=:"
					+ requestField.get(FieldType.MONTHLY_AMOUNT.getName()) + "," + "?FREQUENCY=:"
					+ requestField.get(FieldType.FREQUENCY.getName()) + "," + "?TENURE=:"
					+ requestField.get(FieldType.TENURE.getName()) + "," + "?PAY_ID=:"
					+ requestField.get(FieldType.PAY_ID.getName()) + "," + "?CUST_MOBILE=:"
					+ requestField.get("CUST_MOBILE") + "," + "?CUST_EMAIL=:"
					+ requestField.get(FieldType.CUST_EMAIL.getName()) + "," + "?PURPOSE=:"
					+ requestField.get(FieldType.PURPOSE.getName()) + "," + "?HASH=:"
					+ requestField.get(FieldType.HASH.getName()) + "," + "?RETURN_URL=:"
					+ requestField.get("RETURN_URL");
			String bitly_url = bitlyUrlShortener.createShortUrlUsingBitly(mandate_url);
			content.append("<a href='" + bitly_url
					+ "' style=\"padding: 8px 27px;height: 30px;display:inline-block;font-size: 12px;color: #fff;font-weight: 600;border-radius: 5px;text-decoration: none !important;\">");
			content.append("<img src=\"" + propertiesManager.propertiesMap.get("registration_image")
					+ "\" alt=\"Registration\" title=\"Registration\" style=\"display:block\" width=\"160\" height=\"50\" /></a>");
			content.append("</td></tr></table></td></tr>");
			content.append(
					"<tr><td><table width=\"400\" align=\"center\" style=\"border-collapse:collapse; height:auto !important; color:#000;\"><tr>");
			content.append(
					"<tr> <td colspan=\"3\" style=\"padding: 10px;\"> <h2 style=\"margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;"
							+ "margin-top: 10px;\">Thanks<br>Team Payment Gateway</h2> </td></tr><tr> <td colspan=\"3\" style=\"padding: 10px;font-size: 14px;color: #a0a0a0\">"
							+ " &copy; 2021 <a href=\"http://www.PaymentGateway.com\">www.PaymentGateway.com</a> All rights reserved. </td></tr>");
			content.append(
					"<tr> <td colspan=\"3\" style=\"padding: 10px;font-size: 14px;color: #a0a0a0\"> Please reach us at <a href=\"mailto:support@PaymentGateway.com\">"
							+ "support@PaymentGateway.com</a> in case of queries. </td></tr></tbody></table></body></html>");
			body = content.toString();

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}
	
	@SuppressWarnings("static-access")
	public Map<String, String> upiAutoPayMandateSignBodyMap(Fields requestField, String merchantName) {
		
		String body = null;
		Map<String, String> response = new HashMap<String, String>();
		StringBuilder content = new StringBuilder();
		try {
			content.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">"
					+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
					+ "<title>UPI AutoPay Mandate</title><style>body{font-family: Arial, Helvetica, sans-serif;}</style></head>");
			content.append("<body bgcolor=\"#ccc\">");

			content.append(
					"<table height=\"100%\" bgcolor=\"#f5f5f5\" align=\"center\" style=\"padding: 0px;padding-top: 0px\">");
			content.append("<tbody><tr>");

			content.append(
					"<table width=\"700\" border=\"0\" border-collapse=\"collapse\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"#f5f5f5\" align=\"center\"><tbody>");

			content.append(
					"<tr><td width=\"70%\" bgcolor=\"#002664\" style=\"padding: 10px;color: #fff;font-size: 24px;\">E-mail notification</td><td width=\"30%\" bgcolor=\"#fff\" style=\"padding: 10px;\" align=\"center\"><img src=\""
							+ propertiesManager.getSystemProperty("emailerLogoURL")
							+ "\" alt=\"/\" width=\"150\"> </td></tr>");

			content.append(
					"<tr><td colspan=\"2\" style=\"padding: 10px;\"><h2 style=\"margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;margin-top: 10px;\">Dear Customer,<br>This is UPI AutoPay Registration Mail </h2></td></tr>");

			content.append(
					"<tr><td colspan=\"2\"><table width=\"400\" align=\"center\" bgcolor=\"#fff\" cellpadding=\"15\" style=\"border-collapse:collapse;background-color: #fff\">");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Creation Date:</td>");
			content.append("<td>" + DateCreater.defaultFromDate() + "</td></tr>");
			content.append(
					"<tr align=\"center\"><td colspan=\"2\">Click on the below button to Upi AutoPay Registration</td></tr>");
			content.append("<tr align=\"center\"><td colspan=\"2\" style=\"padding: 20px 0\">");

			String url = propertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MANDATE_SIGN.getValue());
			String mandate_url = url + "?ORDER_ID=:" + requestField.get(FieldType.ORDER_ID.getName()) + ","
					+ "?AMOUNT=:" + requestField.get(FieldType.AMOUNT.getName()) + "," + "?MONTHLY_AMOUNT=:"
					+ requestField.get(FieldType.MONTHLY_AMOUNT.getName()) + "," + "?FREQUENCY=:"
					+ requestField.get(FieldType.FREQUENCY.getName()) + "," + "?TENURE=:"
					+ requestField.get(FieldType.TENURE.getName()) + "," + "?PAY_ID=:"
					+ requestField.get(FieldType.PAY_ID.getName()) + "," + "?CUST_MOBILE=:"
					+ requestField.get("CUST_MOBILE") + "," + "?CUST_EMAIL=:"
					+ requestField.get(FieldType.CUST_EMAIL.getName()) + "," + "?PURPOSE=:"
					+ requestField.get(FieldType.PURPOSE.getName()) + "," + "?HASH=:"
					+ requestField.get(FieldType.HASH.getName()) + "," + "?RETURN_URL=:"
					+ requestField.get("RETURN_URL");
			String bitly_url = bitlyUrlShortener.createShortUrlUsingBitly(mandate_url);
			content.append("<a href='" + bitly_url
					+ "' style=\"padding: 8px 27px;height: 30px;display:inline-block;font-size: 12px;color: #fff;font-weight: 600;border-radius: 5px;text-decoration: none !important;\">");
			content.append("<img src=\"" + propertiesManager.propertiesMap.get("registration_image")
					+ "\" alt=\"Registration\" title=\"Registration\" style=\"display:block\" width=\"160\" height=\"50\" /></a>");
			content.append("</td></tr></table></td></tr>");
			content.append(
					"<tr><td><table width=\"400\" align=\"center\" style=\"border-collapse:collapse; height:auto !important; color:#000;\"><tr>");
			content.append(
					"<tr> <td colspan=\"3\" style=\"padding: 10px;\"> <h2 style=\"margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;"
							+ "margin-top: 10px;\">Thanks<br>Team Payment Gateway</h2> </td></tr><tr> <td colspan=\"3\" style=\"padding: 10px;font-size: 14px;color: #a0a0a0\">"
							+ " &copy; 2021 <a href=\"http://www.PaymentGateway.com\">www.PaymentGateway.com</a> All rights reserved. </td></tr>");
			content.append(
					"<tr> <td colspan=\"3\" style=\"padding: 10px;font-size: 14px;color: #a0a0a0\"> Please reach us at <a href=\"mailto:support@PaymentGateway.com\">"
							+ "support@PaymentGateway.com</a> in case of queries. </td></tr></tbody></table></body></html>");
			body = content.toString();

			response.put("body", body);
			response.put(FieldType.EMANDATE_URL.getName(), bitly_url);
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return response;
	}

	public void sendComplaintMail(String[] mailArray, String complaintId, String complaintType,
			BasicDBObject complaintObj) {
		String bodyHeader = complaintRaiseHeader();
		String bodyMid = null;
		String bodyfooter = complaintRaiseFooter();
		String subject = null;
		try {
			String status = (String) complaintObj.get(FieldType.STATUS.getName());
			subject = (String) complaintObj.get(FieldType.STATUS.getName()) + " | "
					+ complaintObj.get(FieldType.MERCHANT_NAME.getName()) + " | "
					+ complaintObj.get(FieldType.PAY_ID.getName()) + " | " + "Complaint ID : "
					+ complaintObj.get(FieldType.COMPLAINT_ID.getName());
			if (status.equalsIgnoreCase("Open")) {
				bodyMid = complaintRaiseMid("#f99898", "#ad0e0e");
				MessageFormat mf = new MessageFormat(bodyMid);
				Object[] args2 = { complaintObj.get(FieldType.COMPLAINT_ID.getName()),
						complaintObj.get(FieldType.MERCHANT_NAME.getName()),
						complaintObj.get(FieldType.PAY_ID.getName()), complaintObj.get(FieldType.CREATED_BY.getName()),
						complaintObj.get(FieldType.PHONE_NO.getName()),
						complaintObj.get(FieldType.COMPLAINT_TYPE.getName()),
						complaintObj.get(FieldType.COMMENTS.getName()), complaintObj.get(FieldType.STATUS.getName()),
						complaintObj.get("DATE") };
				bodyMid = mf.format(args2);
				body = bodyHeader + bodyMid + bodyfooter;
			} else if (status.equalsIgnoreCase("In-Process")) {
				bodyMid = complaintRaiseMid("#a2f9c3", "#0fa135");
				MessageFormat mf = new MessageFormat(bodyMid);
				Object[] args2 = { complaintObj.get(FieldType.COMPLAINT_ID.getName()),
						complaintObj.get(FieldType.MERCHANT_NAME.getName()),
						complaintObj.get(FieldType.PAY_ID.getName()), complaintObj.get(FieldType.CREATED_BY.getName()),
						complaintObj.get(FieldType.PHONE_NO.getName()),
						complaintObj.get(FieldType.COMPLAINT_TYPE.getName()),
						complaintObj.get(FieldType.COMMENTS.getName()), complaintObj.get(FieldType.STATUS.getName()),
						complaintObj.get("DATE") };
				bodyMid = mf.format(args2);
				body = bodyHeader + bodyMid + bodyfooter;
			} else if (status.equalsIgnoreCase("Resolved")) {
				// c#8d5c0c bg
				bodyMid = complaintRaiseMid("#f7d295", "#8d5c0c");
				MessageFormat mf = new MessageFormat(bodyMid);
				Object[] args2 = { complaintObj.get(FieldType.COMPLAINT_ID.getName()),
						complaintObj.get(FieldType.MERCHANT_NAME.getName()),
						complaintObj.get(FieldType.PAY_ID.getName()), complaintObj.get(FieldType.CREATED_BY.getName()),
						complaintObj.get(FieldType.PHONE_NO.getName()),
						complaintObj.get(FieldType.COMPLAINT_TYPE.getName()),
						complaintObj.get(FieldType.COMMENTS.getName()), complaintObj.get(FieldType.STATUS.getName()),
						complaintObj.get("DATE") };
				bodyMid = mf.format(args2);
				body = bodyHeader + bodyMid + bodyfooter;
			}

			for (String emailId : mailArray) {

				pepipostEmailSender.sendEmail(body, subject, emailId, "");
			}
		} catch (Exception exception) {
			logger.error("Exception in sendComplaintMail : ", exception);
		}

	}

	private String complaintRaiseHeader() {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {
			content.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">"
					+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
					+ "<title>Complaint Email</title><style>body{font-family: Arial, Helvetica, sans-serif;}</style></head>");
			content.append("<body bgcolor=\"#ccc\">");
			content.append(
					"<table width=\"700\" border=\"0\" border-collapse=\"collapse\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"#fff\" align=\"center\">");
			content.append("<tbody><tr>");
			body = content.toString();

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}

	private String complaintRaiseMid(String bgColor, String color) {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {
			content.append(
					"<td width=\"70%\" bgcolor=\"#002664\" style=\"padding: 10px;color: #fff;font-size: 24px;\">Complaint ID : {0}</td>"
							+ "<td width=\"30%\" style=\"padding: 10px;\" align=\"right\">"
							+ "<img src=\"https://uat.Payment Gateway.com/crm/images/logo.png\" alt=\"\" width=\"150\"> </td> </tr>");

			content.append(
					"<tr><td colspan=\"3\" style=\"padding: 10px;\"><h2 style=\"margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;margin-top: 10px;\">Dear Partner, <br><br> Your complaint has been successfully registered at Payment Gateway. You will receive the response shortly. </h2></td> </tr>\r\n");
			content.append(
					"<tr><td colspan=\"3\"><h2 style=\"margin: 0;padding-left: 10px;\">Complaint  Details</h2></td></tr>");
			content.append(
					"<tr><td colspan=\"3\" style=\"padding: 10px\"><table width=\"100%\" cellpadding=\"10\" border=\"0\" bgColor=\"#f5f5f5\">\r\n"
							+ "                    <tr><td>Merchant Name</td><td>{1}</td></tr>\r\n"
							+ "                 " + "		    <tr><td>Reported on:</td><td>{8}</td></tr>\r\n"
							+ "                    <tr><td>Pay ID</td><td>{2}</td></tr>\r\n"
							+ "                    <tr><td>Email:</td><td>{3}</td></tr>\r\n"
							+ "                    <tr><td>Contact Number:</td><td>{4}</td></tr>\r\n"
							+ "                    <tr><td>Issue/Suggestion:</td><td>{5}</td></tr>\r\n"
							+ "                    <tr><td>Brief:</td><td>{6}</td></tr>\r\n"
							+ "                    <tr><td>Status:</td><td><span style='background-color:" + bgColor
							+ ";color:" + color
							+ ";padding: 5px 20px;display: inline-block;border-radius: 5px;font-size: 12px;font-weight: 600'>{7}</td></tr></table></td></tr>\r\n");

			body = content.toString();

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}

	private String complaintRaiseFooter() {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {
			content.append(
					"<tr><td colspan=\"3\" style=\"padding: 10px;font-size: 14px;color: #a0a0a0\">This is an automated mail. Please do not reply to this mail.</td></tr><tr><td colspan=\"3\" style=\"padding: 10px;font-size: 14px;color: #a0a0a0\">Team Payment Gateway</td></tr>\r\n"
							+ "     </tbody></table></body></html>");

			body = content.toString();

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}

	private String createComplaintMailBody() {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {
			content.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">"
					+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
					+ "<title>E-Nach Mandate</title><style>body{font-family: Arial, Helvetica, sans-serif;}</style></head>");
			content.append("<body>");
			content.append(
					"<table width=\"450\"  height=\"100%\" bgcolor=\"#f5f5f5\" align=\"center\" style=\"padding: 0px;padding-top: 0px\">");
			content.append("<tbody><tr>");

			content.append(
					"<body bgcolor=\"#ccc\"><table width=\"700\" border=\"0\" border-collapse=\"collapse\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"#f5f5f5\" align=\"center\">"
							+ " <tbody> <tr> <td width=\"70%\" bgcolor=\"#002664\" style=\"padding: 10px;color: #fff;font-size: 24px;\">E-mail notification</td>"
							+ "<td width=\"30%\" bgcolor=\"#fff\" style=\"padding: 10px;\" align=\"center\">");
			content.append("<img src=\"" + propertiesManager.getSystemProperty("emailerLogoURL")
					+ "\" alt=\"/\" width=\"150\"> </td></tr><tr> <td colspan=\"3\" style=\"padding: 10px;\">"
					+ " <h2 style=\"margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;margin-top: 10px;\">Dear Customer,"
					+ "<br>This is eNACH Registration Mail </h2> </td></tr>");

			content.append(
					"<tr><td><table width=\"400\" align=\"center\" bgcolor=\"#fff\" cellpadding=\"15\" style=\"border-collapse:collapse;background-color: #fff\">");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Creation Date:</td>");
			content.append("<td>" + DateCreater.defaultFromDate() + "</td></tr>");
			String setReviewImage = "data:image/png;base64,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/GMzLdsAADoUaVRYdFhNTDpjb20uYWRvYmUueG1wAAAAAAA8P3hwYWNrZXQgYmVnaW49Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/Pgo8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4OnhtcHRrPSJBZG9iZSBYTVAgQ29yZSA1LjUtYzAxNCA3OS4xNTE0ODEsIDIwMTMvMDMvMTMtMTI6MDk6MTUgICAgICAgICI+CiAgIDxyZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyI+CiAgICAgIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICAgICAgICAgIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyIKICAgICAgICAgICAgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iCiAgICAgICAgICAgIHhtbG5zOnN0RXZ0PSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VFdmVudCMiCiAgICAgICAgICAgIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyIKICAgICAgICAgICAgeG1sbnM6cGhvdG9zaG9wPSJodHRwOi8vbnMuYWRvYmUuY29tL3Bob3Rvc2hvcC8xLjAvIgogICAgICAgICAgICB4bWxuczp0aWZmPSJodHRwOi8vbnMuYWRvYmUuY29tL3RpZmYvMS4wLyIKICAgICAgICAgICAgeG1sbnM6ZXhpZj0iaHR0cDovL25zLmFkb2JlLmNvbS9leGlmLzEuMC8iPgogICAgICAgICA8eG1wOkNyZWF0b3JUb29sPkFkb2JlIFBob3Rvc2hvcCBDQyAoV2luZG93cyk8L3htcDpDcmVhdG9yVG9vbD4KICAgICAgICAgPHhtcDpDcmVhdGVEYXRlPjIwMjEtMDYtMjJUMTA6NDg6MDYrMDU6MzA8L3htcDpDcmVhdGVEYXRlPgogICAgICAgICA8eG1wOk1ldGFkYXRhRGF0ZT4yMDIxLTA2LTIyVDEwOjQ4OjA2KzA1OjMwPC94bXA6TWV0YWRhdGFEYXRlPgogICAgICAgICA8eG1wOk1vZGlmeURhdGU+MjAyMS0wNi0yMlQxMDo0ODowNiswNTozMDwveG1wOk1vZGlmeURhdGU+CiAgICAgICAgIDx4bXBNTTpJbnN0YW5jZUlEPnhtcC5paWQ6OTQ5YTUyYjYtZTZkYi1kNDQ2LWI2OWYtMDA2ZWM2N2JkNTc5PC94bXBNTTpJbnN0YW5jZUlEPgogICAgICAgICA8eG1wTU06RG9jdW1lbnRJRD54bXAuZGlkOmJhN2VmNThhLTdlZTItNWY0MS04NGQ5LWZjZjU3MzllMGNjYzwveG1wTU06RG9jdW1lbnRJRD4KICAgICAgICAgPHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD54bXAuZGlkOmJhN2VmNThhLTdlZTItNWY0MS04NGQ5LWZjZjU3MzllMGNjYzwveG1wTU06T3JpZ2luYWxEb2N1bWVudElEPgogICAgICAgICA8eG1wTU06SGlzdG9yeT4KICAgICAgICAgICAgPHJkZjpTZXE+CiAgICAgICAgICAgICAgIDxyZGY6bGkgcmRmOnBhcnNlVHlwZT0iUmVzb3VyY2UiPgogICAgICAgICAgICAgICAgICA8c3RFdnQ6YWN0aW9uPmNyZWF0ZWQ8L3N0RXZ0OmFjdGlvbj4KICAgICAgICAgICAgICAgICAgPHN0RXZ0Omluc3RhbmNlSUQ+eG1wLmlpZDpiYTdlZjU4YS03ZWUyLTVmNDEtODRkOS1mY2Y1NzM5ZTBjY2M8L3N0RXZ0Omluc3RhbmNlSUQ+CiAgICAgICAgICAgICAgICAgIDxzdEV2dDp3aGVuPjIwMjEtMDYtMjJUMTA6NDg6MDYrMDU6MzA8L3N0RXZ0OndoZW4+CiAgICAgICAgICAgICAgICAgIDxzdEV2dDpzb2Z0d2FyZUFnZW50PkFkb2JlIFBob3Rvc2hvcCBDQyAoV2luZG93cyk8L3N0RXZ0OnNvZnR3YXJlQWdlbnQ+CiAgICAgICAgICAgICAgIDwvcmRmOmxpPgogICAgICAgICAgICAgICA8cmRmOmxpIHJkZjpwYXJzZVR5cGU9IlJlc291cmNlIj4KICAgICAgICAgICAgICAgICAgPHN0RXZ0OmFjdGlvbj5zYXZlZDwvc3RFdnQ6YWN0aW9uPgogICAgICAgICAgICAgICAgICA8c3RFdnQ6aW5zdGFuY2VJRD54bXAuaWlkOjk0OWE1MmI2LWU2ZGItZDQ0Ni1iNjlmLTAwNmVjNjdiZDU3OTwvc3RFdnQ6aW5zdGFuY2VJRD4KICAgICAgICAgICAgICAgICAgPHN0RXZ0OndoZW4+MjAyMS0wNi0yMlQxMDo0ODowNiswNTozMDwvc3RFdnQ6d2hlbj4KICAgICAgICAgICAgICAgICAgPHN0RXZ0OnNvZnR3YXJlQWdlbnQ+QWRvYmUgUGhvdG9zaG9wIENDIChXaW5kb3dzKTwvc3RFdnQ6c29mdHdhcmVBZ2VudD4KICAgICAgICAgICAgICAgICAgPHN0RXZ0OmNoYW5nZWQ+Lzwvc3RFdnQ6Y2hhbmdlZD4KICAgICAgICAgICAgICAgPC9yZGY6bGk+CiAgICAgICAgICAgIDwvcmRmOlNlcT4KICAgICAgICAgPC94bXBNTTpIaXN0b3J5PgogICAgICAgICA8ZGM6Zm9ybWF0PmltYWdlL3BuZzwvZGM6Zm9ybWF0PgogICAgICAgICA8cGhvdG9zaG9wOkNvbG9yTW9kZT4zPC9waG90b3Nob3A6Q29sb3JNb2RlPgogICAgICAgICA8cGhvdG9zaG9wOklDQ1Byb2ZpbGU+c1JHQiBJRUM2MTk2Ni0yLjE8L3Bob3Rvc2hvcDpJQ0NQcm9maWxlPgogICAgICAgICA8dGlmZjpPcmllbnRhdGlvbj4xPC90aWZmOk9yaWVudGF0aW9uPgogICAgICAgICA8dGlmZjpYUmVzb2x1dGlvbj43MjAwMDAvMTAwMDA8L3RpZmY6WFJlc29sdXRpb24+CiAgICAgICAgIDx0aWZmOllSZXNvbHV0aW9uPjcyMDAwMC8xMDAwMDwvdGlmZjpZUmVzb2x1dGlvbj4KICAgICAgICAgPHRpZmY6UmVzb2x1dGlvblVuaXQ+MjwvdGlmZjpSZXNvbHV0aW9uVW5pdD4KICAgICAgICAgPGV4aWY6Q29sb3JTcGFjZT4xPC9leGlmOkNvbG9yU3BhY2U+CiAgICAgICAgIDxleGlmOlBpeGVsWERpbWVuc2lvbj4xMDc8L2V4aWY6UGl4ZWxYRGltZW5zaW9uPgogICAgICAgICA8ZXhpZjpQaXhlbFlEaW1lbnNpb24+MzY8L2V4aWY6UGl4ZWxZRGltZW5zaW9uPgogICAgICA8L3JkZjpEZXNjcmlwdGlvbj4KICAgPC9yZGY6UkRGPgo8L3g6eG1wbWV0YT4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAKPD94cGFja2V0IGVuZD0idyI/PuwYnC8AAAAgY0hSTQAAeiUAAICDAAD5/wAAgOkAAHUwAADqYAAAOpgAABdvkl/FRgAACFpJREFUeNrsWmtQVdcV/tYBubYqVWqqdGrKtUMrmY5BJz4jU1CwIDGxUosPpJ0rghAfI4YaUdMEhGQUW98IGJxKG1IkPqfRRCJktBEUA1EjTq3BGq1ggxIeDRewqz/2eexzHyiGppPp5cedNZez1lnrW9/+9trnXGJmAG+WHCzYW1RV/WFnZxcYIIUBImIGkcJgIsXJJgZACli9EkQAQfUiBuQriRRmJsfIuq8CQPcFAEdft3GEb+9y0Gox8tdqgXolgUm35Wx9+nlPDA5MnBs6f+ZEAMTMv05/ecuOPIAAiNsLg0Fk2GoI9TKRqMk2+TrH0X1NccxXAiQu0SMziAwbBIXVO7ryNXJwXYvrHB5UixkHI84qW2TOi7Feo54YvXpdBphVByd2mG319jDbmq8CZrj1NbpNRBDfiKQlDkoscMdBw9cxjmAumFxxhyUvdps/HhoHqqy9FhgwzOteq/3GjZtyJ9XQms0AqdhDMIJd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";
			content.append(
					"<tr align=\"center\"><td colspan=\"2\">click on the below button to eNACH Registration</td></tr>");
			content.append("<tr align=\"center\"><td colspan=\"2\" style=\"padding: 20px 0\">");

			content.append("<img src=\"" + setReviewImage + "\" \"alt=\"Registration\"></a>");
			content.append("</td></tr></table></td></tr>");
			content.append(
					"<tr><td><table width=\"400\" align=\"center\" style=\"border-collapse:collapse; height:auto !important; color:#000;\"><tr>");
			content.append(
					"<tr> <td colspan=\"3\" style=\"padding: 10px;\"> <h2 style=\"margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;"
							+ "margin-top: 10px;\">Thanks<br>Team Payment Gateway</h2> </td></tr><tr> <td colspan=\"3\" style=\"padding: 10px;font-size: 14px;color: #a0a0a0\">"
							+ " &copy; 2021 <a href=\"http://www.PaymentGateway.com\">www.PaymentGateway.com</a> All rights reserved. </td></tr>");
			content.append(
					"<tr> <td colspan=\"3\" style=\"padding: 10px;font-size: 14px;color: #a0a0a0\"> Please reach us at <a href=\"mailto:support@PaymentGateway.com\">"
							+ "support@PaymentGateway.com</a> in case of queries. </td></tr></tbody></table></body></html>");
			body = content.toString();

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}

	public void sendEmailApi(final EmailData email) {
	}

	public boolean isEmailExceptionHandlerFlag() {
		return emailExceptionHandlerFlag;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getToEmail() {
		return toEmail;
	}

	public void setToEmail(String toEmail) {
		this.toEmail = toEmail;
	}

	public void setEmailExceptionHandlerFlag(boolean emailExceptionHandlerFlag) {
		this.emailExceptionHandlerFlag = emailExceptionHandlerFlag;
	}

	public String getEmailToBcc() {
		return emailToBcc;
	}

	public void setEmailToBcc(String emailToBcc) {
		this.emailToBcc = emailToBcc;
	}

	public StringBuilder getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(StringBuilder responseMessage) {
		this.responseMessage = responseMessage;
	}

}