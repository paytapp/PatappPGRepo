package com.paymentgateway.notification.email.emailBodyCreater;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.itextpdf.text.BadElementException;
import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.EmailerConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;

/*
 @author Sunil 
 */
@Component
public class EmailBodyCreator {
	private Logger logger = LoggerFactory.getLogger(EmailBodyCreator.class);

	@Autowired
	@Qualifier("propertiesManager")
	private PropertiesManager propertiesManager;
	// private StringBuilder content = new StringBuilder();

	public String bodyTransactionEmail(Map<String, String> responseMap, String heading, String message) {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {
			makeHeader(content);
			content.append("<tr>");
			content.append("<td align='left' style='font:normal 14px Arial;'><p><em>Dear  " + heading + ",</td>");
			content.append("<br /><br />" + message + "</em></p>");
			content.append(
					"<p><table width='96%' border='0' align='center' cellpadding='0' cellspacing='0' class='product-spec'>");
			content.append("<tr>");
			content.append("<th colspan='2' align='left' valign='middle'>Payment Summary</th>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td width='27%'>Payment Amount :</td>");
			content.append("<td width='73%'>" + Amount.toDecimal(responseMap.get(CrmFieldConstants.AMOUNT.getValue()),
					responseMap.get(CrmFieldConstants.CURRENCYCODE.getValue())) + "</td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>Order ID:</td>");
			content.append("<td>" + responseMap.get(CrmFieldConstants.ORDER_ID.getValue()) + "</td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>Transaction ID: 	</td>");
			content.append("<td>" + responseMap.get(CrmFieldConstants.TXN_ID.getValue()) + "</td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>Transaction Status:</td>");
			content.append("<td>" + responseMap.get(CrmFieldConstants.TXN_STATUS) + "</td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>Transaction Response:</td>");
			content.append("<td>" + responseMap.get(CrmFieldConstants.RESPONSE_MESSAGE.getValue()) + "</td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>Transaction Date &amp; Time:</td>");
			content.append("<td>" + responseMap.get(CrmFieldConstants.RESPONSE_DATE_TIME.getValue()) + "</td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>Customer Name:</td>");
			content.append("<td>" + responseMap.get(CrmFieldConstants.CUST_NAME.getValue()) + "</td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>Customer Email:</td>");
			content.append("<td>" + responseMap.get(CrmFieldConstants.CUST_EMAIL.getValue()) + "</td>");
			content.append("</tr>");
			content.append("</table>");
			content.append("</p>");
			makeFooter(content);

			body = content.toString();

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}

	public String transactionFailled(Map<String, String> responseMap) {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {
			makeHeader(content);
			content.append("<tr>");
			content.append("<td align='left' style='font:normal 14px Arial;'><p><em>Dear User,<br />");
			content.append("<br />");
			content.append("Thank you for paying with ");
			content.append(EmailerConstants.GATEWAY.getValue());
			content.append(
					". Your Payment has been Failled. If you wish to pay again for the services please click on the below given button, which will redirect you to our payment page.</em></p>");
			content.append(
					"<p><table width='20%' border='0' align='center' cellpadding='0.' cellspacing='0' class='product-spec1'>");
			content.append("<tr>");
			content.append(
					"<td height='30' align='center' valign='middle' bgcolor='#2b6dd1'><a href='#'>Reprocess Pay</a></td>");
			content.append("</tr>");
			content.append("</table>");
			content.append(
					"<p><table width='96%' border='0' align='center' cellpadding='0.' cellspacing='0' class='product-spec'>");
			content.append("<tr>");
			content.append("<th colspan='2' align='left' valign='middle'>Payment Summary</th>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td width='27%'>Payment Amount (Rs):</td>");
			content.append("<td width='73%'>" + Amount.toDecimal(responseMap.get(CrmFieldConstants.AMOUNT.getValue()),
					responseMap.get(CrmFieldConstants.CURRENCYCODE.getValue())) + "</td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>Order ID:</td>");
			content.append("<td>" + responseMap.get(CrmFieldConstants.ORDER_ID.getValue()) + "</td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>Transaction ID: 	</td>");
			content.append("<td>" + responseMap.get(CrmFieldConstants.TXN_ID.getValue()) + "</td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>Transaction Status:</td>");
			content.append("<td>" + responseMap.get(CrmFieldConstants.TXN_STATUS.getValue()) + "</td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>Transaction Response:</td>");
			content.append("<td>" + responseMap.get(CrmFieldConstants.RESPONSE_MESSAGE.getValue()) + "</td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>Transaction Date &amp; Time:</td>");
			content.append("<td>" + responseMap.get(CrmFieldConstants.RESPONSE_DATE_TIME.getValue()) + "</td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>Customer Name:</td>");
			content.append("<td>" + responseMap.get(CrmFieldConstants.CUST_NAME.getValue()) + "</td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>Customer Email:</td>");
			content.append("<td>" + responseMap.get(CrmFieldConstants.CUST_EMAIL.getValue()) + "</td>");
			content.append("</tr>");
			content.append("</table>");
			content.append("</p>");
			makeFooter(content);
			body = content.toString();

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}

	public String bodyPaytm(Fields responseMap) {
		String body = null;
		try {
			StringBuilder contant = new StringBuilder();
			contant.append("Dear Merchant" + "," + "\n\n");
			contant.append("Thank you for transaction with ");
			contant.append(EmailerConstants.COMPANY.getValue());
			contant.append(".\n\nPayment Amount (Rs)		: " + responseMap.get("TXNAMOUNT") + "\n");
			contant.append(
					"Transaction Auth Code	: " + responseMap.get(CrmFieldConstants.AUTH_CODE.getValue()) + "\n");
			contant.append("Transaction ID			: " + responseMap.get(CrmFieldConstants.TXN_ID.getValue()) + "\n");
			contant.append("Transaction Status		: " + responseMap.get("RESPONSE_MESSAGE") + "\n");
			contant.append("Transaction Response	: " + responseMap.get("RESPMSG") + "\n");
			contant.append("Transaction Date & Time	: " + responseMap.get("TXNDATE") + "\n");
			contant.append("Assuring you of our best service at all times.\n");
			contant.append("In case any Further query, please do tell us at ");
			contant.append(EmailerConstants.CONTACT_US_EMAIL.getValue());
			contant.append(" or by phone at ");
			contant.append(EmailerConstants.PHONE_NO.getValue());
			contant.append(" \n\n We Care!\n");
			contant.append(EmailerConstants.GATEWAY.getValue());
			contant.append("Team");
			body = contant.toString();

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}

	public String bodyDirecPay(Fields responseMap) {
		String body = null;
		try {
			StringBuilder contant = new StringBuilder();
			contant.append("Dear Merchant" + "," + "\n\n");
			contant.append("Thank you for transaction with ");
			contant.append(EmailerConstants.COMPANY.getValue());
			contant.append(".\n\n");
			contant.append("Payment Amount (Rs)		: " + responseMap.get("TXNAMOUNT") + "\n");
			contant.append(
					"Transaction Auth Code	: " + responseMap.get(CrmFieldConstants.AUTH_CODE.getValue()) + "\n");
			contant.append("Transaction ID			: " + responseMap.get(CrmFieldConstants.TXN_ID.getValue()) + "\n");
			contant.append("Transaction Status		: " + responseMap.get(CrmFieldConstants.RESPONSE_MESSAGE.getValue())
					+ "\n");
			contant.append("Transaction Response	: " + responseMap.get("RESPMSG") + "\n");
			contant.append("Transaction Date & Time	: " + responseMap.get("TXNDATE") + "\n");
			contant.append("Assuring you of our best service at all times." + "\n");
			contant.append("In case any Further query, please do tell us at");
			contant.append(EmailerConstants.CONTACT_US_EMAIL.getValue());
			contant.append(" or by phone at");
			contant.append(EmailerConstants.PHONE_NO.getValue());
			contant.append(" \n\n We Care!" + "\n");
			contant.append(EmailerConstants.GATEWAY.getValue());
			contant.append("Team");
			body = contant.toString();

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}

	public String refundEmailBody(Map<String, String> fields, String senderType, String businessName) {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {
			makeHeader(content);
			content.append("<tr>");
			content.append("<td align='left' style='font:normal 14px Arial;'>");
			content.append("<p><em>Dear " + senderType + ",</em></p>");
			content.append(
					"<p><table width='96%' border='0' align='center' cellpadding='0.' cellspacing='0' class='product-spec'>");
			content.append("<tr>");
			content.append("<th colspan='2' align='left' valign='middle'>Refund Processed Successful. </th>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td width='27%'>Merchant Name :- </td>");
			content.append("<td width='73%'>" + businessName + "</td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>Order Id :-  </td>");
			content.append("<td>" + fields.get(CrmFieldType.ORDER_ID.toString()) + "</td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>Amount :- </td>");
			content.append("<td>" + Amount.toDecimal(fields.get(CrmFieldConstants.AMOUNT.getValue()),
					fields.get(CrmFieldConstants.CURRENCYCODE.getValue())) + "</td>");
			content.append("</tr>");
			content.append("</table></p>");
			makeFooter(content);
			body = content.toString();
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
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
                        "<tr><td style=\"padding-top:15px\">Please connect with us at <a href = \"mailto:support@paymnetgateway.com\">support@paymnetgateway.com</a> for any further information.</td></tr>");
          body.append(
					"<tr><td><table width=\"400\" align=\"left\" style=\"border-collapse:collapse; height:auto !important; color:#000;\">");
			body.append(
					"<tr><td style=\"padding-top: 10px;padding-bottom: 10px;line-height: 20px;font-size: 14px;\">--<br>Regards<br>");
                body.append(
                        "<span style=\"display: block;\">Team Paymnet Gateway</span></td></tr></table></td></tr></table></body></html>");
            content = body.toString();

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return content;
	}

	public String signupConfirmation(String businessName, String emailId, String mobile) {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {
			makeHeader(content);
			content.append("<tr>");
			content.append("<td align='left' style='font:normal 14px Arial;'><p><em>Dear Merchant,<br /><br />");
			content.append("Congratulations for Signing-up with ");
			content.append(EmailerConstants.GATEWAY.getValue());
			content.append(
					".<br /><table width='96%' border='0' align='center' cellpadding='0.' cellspacing='0' class='product-spec'>");
			content.append("<tr>");
			content.append("<th colspan='2' align='left' valign='middle'>Merchant Signup Details</th>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td width='27%'>Business  Name :- ");
			content.append("<br /></td>");
			content.append("<td width='73%'>" + businessName + "</td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>Email  Id :-</td>");
			content.append("<td> " + emailId + " </td>");
			content.append("</tr>");
			content.append("<tr>");
			content.append("<td>Phone  No :- </td>");
			content.append("<td>" + mobile + "</td>");
			content.append("</tr>");
			content.append("</table>");
			content.append("</p>");
			makeFooter(content);
			body = content.toString();
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}

	public String addUser(String firstName, ResponseObject responseObject, String url) {
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
			content.append("<span style=\"display: block;\">Team Paymnet Gateway</span></td></tr>");
			content.append(
					"<tr><td align=\"right\" style=\"font-size: 12px;padding-top: 15px;\">&copy; 2020 www.paymentgateway.com All rights reserved.</td></tr>");
			content.append("</table></td></tr></tbody></table></body></html>");
			body = content.toString();

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}

	@SuppressWarnings("static-access")
	public String eMandateSignBody(Fields requestField, String merchantName) {
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

			String url = propertiesManager.propertiesMap.get("DEMO_ENACH_MANDATE_SIGN");
			String returnUrl = requestField.get("RETURN_URL");
			content.append("<a href='" + url + "?ORDER_ID=:" + requestField.get("ORDER_Id") + "," + "?AMOUNT=:"
					+ requestField.get(FieldType.AMOUNT.getName()) + "," + "?MONTHLY_AMOUNT=:"
					+ requestField.get(FieldType.MONTHLY_AMOUNT.getName()) + "," + "?FREQUENCY=:"
					+ requestField.get(FieldType.FREQUENCY.getName()) + "," + "?TENURE=:"
					+ requestField.get(FieldType.TENURE.getName()) + "," + "?PAY_ID=:"
					+ requestField.get(FieldType.PAY_ID.getName()) + "," + "?CUST_MOBILE=:"
					+ requestField.get("CUST_MOBILE") + "," + "?CUST_EMAIL=:"
					+ requestField.get(FieldType.CUST_EMAIL.getName()) + "," + "?HASH=:"
					+ requestField.get(FieldType.HASH.getName()) + "," + "?RETURN_URL=:" + returnUrl
					+ "+' style=\"padding: 8px 27px;height: 30px;display:inline-block;font-size: 12px;color: #fff;font-weight: 600;border-radius: 5px;text-decoration: none;\">");

			content.append("<img src=\"" + setReviewImage + "\" \"alt=\"Registration\"></a>");
			content.append("</td></tr></table></td></tr>");
			content.append(
					"<tr><td><table width=\"400\" align=\"center\" style=\"border-collapse:collapse; height:auto !important; color:#000;\"><tr>");
			content.append(
					"<tr> <td colspan=\"3\" style=\"padding: 10px;\"> <h2 style=\"margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;"
							+ "margin-top: 10px;\">Thanks<br>Team Paymnet Gateway</h2> </td></tr><tr> <td colspan=\"3\" style=\"padding: 10px;font-size: 14px;color: #a0a0a0\">"
							+ " &copy; 2021 <a href=\"http://www.paymentgateway.com\">www.paymentgateway.com</a> All rights reserved. </td></tr>");
			content.append(
					"<tr> <td colspan=\"3\" style=\"padding: 10px;font-size: 14px;color: #a0a0a0\"> Please reach us at <a href=\"mailto:support@paymnetgateway.com\">"
							+ "support@paymnetgateway.com</a> in case of queries. </td></tr></tbody></table></body></html>");
			body = content.toString();

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}

	@SuppressWarnings("static-access")
	public String upiAutoPayMandateSignBody(Fields requestField, String merchantName) {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {
			content.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">"
					+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
					+ "<title>UPI AutoPay Mandate</title><style>body{font-family: Arial, Helvetica, sans-serif;}</style></head>");
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
					+ "<br>This is UPI AutoPay Registration Mail </h2> </td></tr>");

			content.append(
					"<tr><td><table width=\"400\" align=\"center\" bgcolor=\"#fff\" cellpadding=\"15\" style=\"border-collapse:collapse;background-color: #fff\">");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Creation Date:</td>");
			content.append("<td>" + DateCreater.defaultFromDate() + "</td></tr>");
			String setRegistrationImage = "data:image/png;base64,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/GMzLdsAADoUaVRYdFhNTDpjb20uYWRvYmUueG1wAAAAAAA8P3hwYWNrZXQgYmVnaW49Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/Pgo8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4OnhtcHRrPSJBZG9iZSBYTVAgQ29yZSA1LjUtYzAxNCA3OS4xNTE0ODEsIDIwMTMvMDMvMTMtMTI6MDk6MTUgICAgICAgICI+CiAgIDxyZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyI+CiAgICAgIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICAgICAgICAgIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyIKICAgICAgICAgICAgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iCiAgICAgICAgICAgIHhtbG5zOnN0RXZ0PSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VFdmVudCMiCiAgICAgICAgICAgIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyIKICAgICAgICAgICAgeG1sbnM6cGhvdG9zaG9wPSJodHRwOi8vbnMuYWRvYmUuY29tL3Bob3Rvc2hvcC8xLjAvIgogICAgICAgICAgICB4bWxuczp0aWZmPSJodHRwOi8vbnMuYWRvYmUuY29tL3RpZmYvMS4wLyIKICAgICAgICAgICAgeG1sbnM6ZXhpZj0iaHR0cDovL25zLmFkb2JlLmNvbS9leGlmLzEuMC8iPgogICAgICAgICA8eG1wOkNyZWF0b3JUb29sPkFkb2JlIFBob3Rvc2hvcCBDQyAoV2luZG93cyk8L3htcDpDcmVhdG9yVG9vbD4KICAgICAgICAgPHhtcDpDcmVhdGVEYXRlPjIwMjEtMDYtMjJUMTA6NDg6MDYrMDU6MzA8L3htcDpDcmVhdGVEYXRlPgogICAgICAgICA8eG1wOk1ldGFkYXRhRGF0ZT4yMDIxLTA2LTIyVDEwOjQ4OjA2KzA1OjMwPC94bXA6TWV0YWRhdGFEYXRlPgogICAgICAgICA8eG1wOk1vZGlmeURhdGU+MjAyMS0wNi0yMlQxMDo0ODowNiswNTozMDwveG1wOk1vZGlmeURhdGU+CiAgICAgICAgIDx4bXBNTTpJbnN0YW5jZUlEPnhtcC5paWQ6OTQ5YTUyYjYtZTZkYi1kNDQ2LWI2OWYtMDA2ZWM2N2JkNTc5PC94bXBNTTpJbnN0YW5jZUlEPgogICAgICAgICA8eG1wTU06RG9jdW1lbnRJRD54bXAuZGlkOmJhN2VmNThhLTdlZTItNWY0MS04NGQ5LWZjZjU3MzllMGNjYzwveG1wTU06RG9jdW1lbnRJRD4KICAgICAgICAgPHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD54bXAuZGlkOmJhN2VmNThhLTdlZTItNWY0MS04NGQ5LWZjZjU3MzllMGNjYzwveG1wTU06T3JpZ2luYWxEb2N1bWVudElEPgogICAgICAgICA8eG1wTU06SGlzdG9yeT4KICAgICAgICAgICAgPHJkZjpTZXE+CiAgICAgICAgICAgICAgIDxyZGY6bGkgcmRmOnBhcnNlVHlwZT0iUmVzb3VyY2UiPgogICAgICAgICAgICAgICAgICA8c3RFdnQ6YWN0aW9uPmNyZWF0ZWQ8L3N0RXZ0OmFjdGlvbj4KICAgICAgICAgICAgICAgICAgPHN0RXZ0Omluc3RhbmNlSUQ+eG1wLmlpZDpiYTdlZjU4YS03ZWUyLTVmNDEtODRkOS1mY2Y1NzM5ZTBjY2M8L3N0RXZ0Omluc3RhbmNlSUQ+CiAgICAgICAgICAgICAgICAgIDxzdEV2dDp3aGVuPjIwMjEtMDYtMjJUMTA6NDg6MDYrMDU6MzA8L3N0RXZ0OndoZW4+CiAgICAgICAgICAgICAgICAgIDxzdEV2dDpzb2Z0d2FyZUFnZW50PkFkb2JlIFBob3Rvc2hvcCBDQyAoV2luZG93cyk8L3N0RXZ0OnNvZnR3YXJlQWdlbnQ+CiAgICAgICAgICAgICAgIDwvcmRmOmxpPgogICAgICAgICAgICAgICA8cmRmOmxpIHJkZjpwYXJzZVR5cGU9IlJlc291cmNlIj4KICAgICAgICAgICAgICAgICAgPHN0RXZ0OmFjdGlvbj5zYXZlZDwvc3RFdnQ6YWN0aW9uPgogICAgICAgICAgICAgICAgICA8c3RFdnQ6aW5zdGFuY2VJRD54bXAuaWlkOjk0OWE1MmI2LWU2ZGItZDQ0Ni1iNjlmLTAwNmVjNjdiZDU3OTwvc3RFdnQ6aW5zdGFuY2VJRD4KICAgICAgICAgICAgICAgICAgPHN0RXZ0OndoZW4+MjAyMS0wNi0yMlQxMDo0ODowNiswNTozMDwvc3RFdnQ6d2hlbj4KICAgICAgICAgICAgICAgICAgPHN0RXZ0OnNvZnR3YXJlQWdlbnQ+QWRvYmUgUGhvdG9zaG9wIENDIChXaW5kb3dzKTwvc3RFdnQ6c29mdHdhcmVBZ2VudD4KICAgICAgICAgICAgICAgICAgPHN0RXZ0OmNoYW5nZWQ+Lzwvc3RFdnQ6Y2hhbmdlZD4KICAgICAgICAgICAgICAgPC9yZGY6bGk+CiAgICAgICAgICAgIDwvcmRmOlNlcT4KICAgICAgICAgPC94bXBNTTpIaXN0b3J5PgogICAgICAgICA8ZGM6Zm9ybWF0PmltYWdlL3BuZzwvZGM6Zm9ybWF0PgogICAgICAgICA8cGhvdG9zaG9wOkNvbG9yTW9kZT4zPC9waG90b3Nob3A6Q29sb3JNb2RlPgogICAgICAgICA8cGhvdG9zaG9wOklDQ1Byb2ZpbGU+c1JHQiBJRUM2MTk2Ni0yLjE8L3Bob3Rvc2hvcDpJQ0NQcm9maWxlPgogICAgICAgICA8dGlmZjpPcmllbnRhdGlvbj4xPC90aWZmOk9yaWVudGF0aW9uPgogICAgICAgICA8dGlmZjpYUmVzb2x1dGlvbj43MjAwMDAvMTAwMDA8L3RpZmY6WFJlc29sdXRpb24+CiAgICAgICAgIDx0aWZmOllSZXNvbHV0aW9uPjcyMDAwMC8xMDAwMDwvdGlmZjpZUmVzb2x1dGlvbj4KICAgICAgICAgPHRpZmY6UmVzb2x1dGlvblVuaXQ+MjwvdGlmZjpSZXNvbHV0aW9uVW5pdD4KICAgICAgICAgPGV4aWY6Q29sb3JTcGFjZT4xPC9leGlmOkNvbG9yU3BhY2U+CiAgICAgICAgIDxleGlmOlBpeGVsWERpbWVuc2lvbj4xMDc8L2V4aWY6UGl4ZWxYRGltZW5zaW9uPgogICAgICAgICA8ZXhpZjpQaXhlbFlEaW1lbnNpb24+MzY8L2V4aWY6UGl4ZWxZRGltZW5zaW9uPgogICAgICA8L3JkZjpEZXNjcmlwdGlvbj4KICAgPC9yZGY6UkRGPgo8L3g6eG1wbWV0YT4KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAKPD94cGFja2V0IGVuZD0idyI/PuwYnC8AAAAgY0hSTQAAeiUAAICDAAD5/wAAgOkAAHUwAADqYAAAOpgAABdvkl/FRgAACFpJREFUeNrsWmtQVdcV/tYBubYqVWqqdGrKtUMrmY5BJz4jU1CwIDGxUosPpJ0rghAfI4YaUdMEhGQUW98IGJxKG1IkPqfRRCJktBEUA1EjTq3BGq1ggxIeDRewqz/2eexzHyiGppPp5cedNZez1lnrW9/+9trnXGJmAG+WHCzYW1RV/WFnZxcYIIUBImIGkcJgIsXJJgZACli9EkQAQfUiBuQriRRmJsfIuq8CQPcFAEdft3GEb+9y0Gox8tdqgXolgUm35Wx9+nlPDA5MnBs6f+ZEAMTMv05/ecuOPIAAiNsLg0Fk2GoI9TKRqMk2+TrH0X1NccxXAiQu0SMziAwbBIXVO7ryNXJwXYvrHB5UixkHI84qW2TOi7Feo54YvXpdBphVByd2mG319jDbmq8CZrj1NbpNRBDfiKQlDkoscMdBw9cxjmAumFxxhyUvdps/HhoHqqy9FhgwzOteq/3GjZtyJ9XQms0AqdhDMIJd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";
			content.append(
					"<tr align=\"center\"><td colspan=\"2\">click on the below button to Upi AutoPay Registration</td></tr>");
			content.append("<tr align=\"center\"><td colspan=\"2\" style=\"padding: 20px 0\">");

			String url = propertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MANDATE_SIGN.getValue());
			// String returnUrl = requestField.get("RETURN_URL");

			content.append("<a href='" + url + "?ORDER_ID=:" + requestField.get(FieldType.ORDER_ID.getName()) + ","
					+ "?AMOUNT=:" + requestField.get(FieldType.AMOUNT.getName()) + "," + "?CUST_MOBILE=:"
					+ requestField.get("CUST_MOBILE") + "," + "?MONTHLY_AMOUNT=:"
					+ requestField.get(FieldType.MONTHLY_AMOUNT.getName()) + "," + "?FREQUENCY=:"
					+ requestField.get(FieldType.FREQUENCY.getName()) + "," + "?PURPOSE=:"
					+ requestField.get(FieldType.PURPOSE.getName()) + "," + "?TENURE=:"
					+ requestField.get(FieldType.TENURE.getName()) + "," + "?PAY_ID=:"
					+ requestField.get(FieldType.PAY_ID.getName()) + "," + "?CUST_EMAIL=:"
					+ requestField.get("CUST_EMAIL") + "," + "?HASH=:" + requestField.get(FieldType.HASH.getName())
					/* + "," + "?RETURN_URL=:" + returnUrl */
					+ "+' style=\"padding: 8px 27px;height: 30px;display:inline-block;font-size: 12px;color: #fff;font-weight: 600;border-radius: 5px;text-decoration: none;\">");

			content.append("<img src=\"" + setRegistrationImage + "\" \"alt=\"Registration\"></a>");
			content.append("</td></tr></table></td></tr>");
			content.append(
					"<tr><td><table width=\"400\" align=\"center\" style=\"border-collapse:collapse; height:auto !important; color:#000;\"><tr>");
			content.append(
					"<tr> <td colspan=\"3\" style=\"padding: 10px;\"> <h2 style=\"margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;"
							+ "margin-top: 10px;\">Thanks<br>Team Paymnet Gateway</h2> </td></tr><tr> <td colspan=\"3\" style=\"padding: 10px;font-size: 14px;color: #a0a0a0\">"
							+ " &copy; 2021 <a href=\"http://www.paymentgateway.com\">www.paymentgateway.com</a> All rights reserved. </td></tr>");
			content.append(
					"<tr> <td colspan=\"3\" style=\"padding: 10px;font-size: 14px;color: #a0a0a0\"> Please reach us at <a href=\"mailto:support@paymnetgateway.com\">"
							+ "support@paymnetgateway.com</a> in case of queries. </td></tr></tbody></table></body></html>");
			body = content.toString();

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
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
			content.append("<span style=\"display: block;\">Team Paymnet Gateway</span></td></tr>");

			content.append(
					"<tr><td align=\"right\" style=\"font-size: 12px;padding-top: 15px;\">&copy; 2020 www.paymentgateway.com All rights reserved.</td></tr>");
			content.append("</table></td></tr></tbody></table></body></html>");

			body = content.toString();
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
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

	public String invoiceLink(Invoice invoice) {
		String body = null;
		StringBuilder content = new StringBuilder();

		try {
			content.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">"
					+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
					+ "<title>Invoice</title><style>body{font-family: Arial, Helvetica, sans-serif;}</style></head>");
			content.append("<body>");
			content.append(
					"<table width=\"350\"  height=\"100%\" bgcolor=\"#f5f5f5\" align=\"center\" style=\"padding: 20px;padding-top: 5px\">");
			content.append("<tbody><tr>");
			content.append("<td align=\"center\"><img src=\"" + propertiesManager.getSystemProperty("logoForEmail")
					+ "\" alt=\"\" style=\"padding-top: 5px\" ></td></tr>");
			content.append(
					"<tr><td><h2 style=\"margin: 0;font-size: 15px;font-weight: 400;color: #8a8a8a;margin-bottom: 10px;\">Your Invoice</td></tr>");
			content.append(
					"<tr><td><table width=\"400\" align=\"center\" bgcolor=\"#fff\" cellpadding=\"15\" style=\"border-collapse:collapse;background-color: #fff\">");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Name</td>");
			content.append("<td>" + invoice.getName() + "</td></tr>");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append(
					"<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Product Name</td>");
			content.append("<td>" + invoice.getProductName() + "</td></tr>");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Mobile</td>");
			if (invoice.getPhone() != null) {
				content.append("<td>" + invoice.getPhone() + "</td></tr>");
			} else {
				content.append("<td>" + "" + "</td></tr>");
			}
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Email</td>");
			content.append("<td>" + invoice.getEmail() + "</td></tr>");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append(
					"<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Amount INR</td>");
			content.append("<td>" + invoice.getTotalAmount() + "</td></tr>");
			/*
			 * content.
			 * append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			 * content.
			 * append("<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Payment Link</td>"
			 * ); content.append("<td>"+url+"</td></tr>");
			 */
			content.append("<tr align=\"center\">");
			// content.append("<td style=\"font-weight: 600; width:200px;color:
			// #888888;\"></td>");
			content.append(
					"<td colspan=\"2\" style=\"border-bottom: 1px solid #ddd;\"><img src=\"data:image/jpg;base64,"
							+ invoice.getQr() + "\" width=\"150px\" height=\"150px\" alt=\"/\"></td></tr>");
			content.append("<tr align=\"center\"><td colspan=\"2\" style=\"padding: 20px 0\">");
			content.append("<a href=\"" + invoice.getLongUrl()
					+ "\" style=\"padding: 8px 27px;height: 30px;display:inline-block;font-size: 12px;color: #fff;font-weight: 600;border-radius: 5px;text-decoration: none;\">");
			content.append("<img src=\"" + propertiesManager.getSystemProperty("emailerPayButton") + "\"/></a>");
			content.append("</td></tr></table></td></tr>");
			content.append(
					"<tr><td><table width=\"400\" align=\"center\" style=\"border-collapse:collapse; height:auto !important; color:#000;\"><tr>");
			content.append(
					"<td style=\"padding-top: 10px;padding-bottom: 10px;line-height: 20px;font-size: 14px;\">Thanks<br>");
			content.append("<span style=\"display: block;\">Team Paymnet Gateway</span></td></tr>");
			content.append("<tr><td style=\"font-size: 12px;\">");
			content.append(
					"For any queries feel free to connect with us at +91 120 433 4884. You may also drop your query to us at "
							+ "<a href=\"mailto:support@paymnetgateway.com\">support@paymnetgateway.com</a></td></tr>");
			content.append(
					"<tr><td align=\"right\" style=\"font-size: 12px;padding-top: 15px;\">&copy; 2020 www.paymentgateway.com All rights reserved.</td></tr>");
			content.append("</table></td></tr></tbody></table></body></html>");
			body = content.toString();
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}

	public String documentUpload() {
		String body = null;
		try {
			StringBuilder contant = new StringBuilder();
			contant.append("Dear Merchant," + "\n\n");
			contant.append("Your Document uploaded successfully" + "\n\n");
			contant.append("Assuring you of our best service at all times.\n");
			contant.append("In case any Further query, please do tell us at ");
			contant.append(EmailerConstants.CONTACT_US_EMAIL.getValue());
			contant.append(" or ");
			contant.append(EmailerConstants.PHONE_NO.getValue());
			contant.append(" \n\n We Care!" + "\n");
			contant.append(EmailerConstants.GATEWAY.getValue());
			contant.append(" Team");
			body = contant.toString();
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}

	public String transactionAuthenticationLink(Map<String, String> fields, String url, String txnId) {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {
			makeHeader(content);
			content.append("<tr>");
			content.append("<td align='left' style='font:normal 14px Arial;'><p><em>Dear Customer,<br />");
			content.append("<br />");
			content.append("Thank you for paying with ");
			content.append(EmailerConstants.GATEWAY.getValue());
			content.append(".<br /><br />");
			content.append("Please click on the below button to authenticate your recent transaction at ");
			content.append(EmailerConstants.GATEWAY.getValue());
			content.append(".</em><br />");
			content.append(
					"<table width='20%' border='0' align='left' cellpadding='0.' cellspacing='0' class='product-spec1'>");
			content.append("<tr>");
			content.append("<td height='30' align='center' valign='middle' bgcolor='#2b6dd1' color='#fff'><a href='"
					+ url + "?id=" + txnId + "'>Authentication</a></td>");
			content.append("</tr>");
			content.append("</table></p>");
			content.append("<br /><br />");
			makeFooter(content);
			body = content.toString();
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
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

	public String getEmailBodyWithAttachement(String payoutDate, Merchants merchant, String image, int saletxn,
			int refundtxn, BigDecimal saleAmount, BigDecimal refundAmount, BigDecimal settlementAmount, String utr,
			String account, int chargeback, BigDecimal chargebackamount, String currency)
			throws BadElementException, MalformedURLException, IOException {

		StringBuilder body = new StringBuilder();
		String currencyType = "INR ";
		if(StringUtils.isNotBlank(currency)) {
			currencyType = currency + " ";
		}
		
		if (image.contains("null.png")) {
			image = PropertiesManager.propertiesMap.get("noLogoMerchant");
		}
		String footerlogo = PropertiesManager.propertiesMap.get("emailerLogoURL");
		String img = PropertiesManager.propertiesMap.get("dashboardEmailLogo");
		String imgdocs = PropertiesManager.propertiesMap.get("paymentAdviceReportDocs");

		body.append("<!DOCTYPE html>");
		body.append(
				"<html lang='en'> <head> <meta charset='UTF-8'> <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
		body.append(
				"<link href=\"https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700;900&display=swap\" rel=\"stylesheet\"><title>Payment Advise Report</title>");
		body.append("<style> body{font-family: 'Roboto', sans-serif;} </style>");
		body.append("</head>");
		body.append("<body bgcolor='#fff'>");
		body.append("<table width=\"800\" align=\"center\" cellspacing=\"10px\">");
		body.append("<tr>");
		body.append("<td><img src=\"" + image + "\" width=\"100\" alt=\"/\"></td>");
		body.append("<td align=\"right\"><h2>Daily Settelment Report - " + payoutDate + "</h2></td>");
		body.append("</tr>");
		body.append("<tr><td colspan=\"2\"> <table bgcolor=\"#002664\" cellpadding=\"10px\" width=\"100%\">");
		body.append("<tr><td colspan=\"4\" style=\"color: #fff\" align=\"center\"><img width=\"100\" src=\"" + imgdocs
				+ "\" alt=\"\"><h2>" + merchant.getBusinessName() + "</h2></td></tr>");
		body.append("<tr align='center' style='color: #fff'><td width='33%'><span> "
				+ currencyType + saleAmount.setScale(2, BigDecimal.ROUND_HALF_EVEN)
				+ "</span><br><span style='display: block'>Total Sale</span></td>");
		body.append(" <td width='33%'><span> " + currencyType + refundAmount.setScale(2, BigDecimal.ROUND_HALF_EVEN)
				+ "</span><br><span style='display: block'>Total Refund</span></td>");
		body.append("<td width='33%'><span> " + currencyType + settlementAmount.setScale(2, BigDecimal.ROUND_HALF_EVEN)
				+ "</span><br><span style='display: block'>Total Settled</span></td></tr>");
		body.append(
				"<tr align='center'><td colspan='4'><a href='https://www.paymentgateway.com/crm/jsp/login' style='border-radius:25px;text-decoration: none;display:inline-block;padding: 0px 20px;color: #fff;background-color: #3477e0'><img src=\""
						+ img + "\" alt=\"/\"></a></td></tr></table></td></tr>");

		body.append("<tr ><td><table cellpadding=\"3px\"><tr><td>Captured Transactions:</td><td><b>" + saletxn
				+ "</b></td></tr><tr><td>Refund Transactions</td><td><b>" + refundtxn
				+ "</b></td></tr><tr><td>UTR:</td><td><b>" + utr + "</b></td></tr></table></td>");
		body.append("<td><table cellpadding='3px' align='right'><tr><td>No. of Chargeback:</td><td><b>" + chargeback
				+ "</b></td></tr><tr><td>Chargeback Amount:</td><td><b>" + currencyType + chargebackamount
				+ "</b></td></tr><tr><td>Account No</td><td><b>" + account + "</b></td></tr></table></td></tr>");
		body.append(
				"<tr><td><a href=\"mailto:support@paymnetgateway.com\">support@paymnetgateway.com</a></td><td align=\"right\"><img src=\""
						+ footerlogo + "\" width=\"150\" alt=\"/\"></td></tr>");
		body.append("</table> </body> </html>");

		return body.toString();

	}

	public String AttachmentFileEmail(String fileName) {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {
			content.append("<tr>");
			content.append("<td align='left' style='font:normal 14px Arial;'><p><em>Dear Bank,<br /><br />");
			content.append(
					"<table width='20%' border='0' align='left' cellpadding='0.' cellspacing='0' class='product-spec1'>");
			content.append("<tr>");
			content.append("<br /><br />" + fileName + "</em></p>");
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

	public String mpaStatusUpdate(String emailContent) {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {
			content.append("<tr>");
			content.append("<td align='left' style='font:normal 14px Arial;'><p><em>Dear Admin/SubAdmin,<br /><br />");
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

}