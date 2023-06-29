package com.paymentgateway.pgui.action.beans;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.SmsSender;
import com.paymentgateway.commons.dao.EPOSTransactionDao;
import com.paymentgateway.commons.user.EPOSTransaction;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AWSSESEmailService;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.BASE64Encoder;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PDFCreator;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class HandleEPOSSaleResponse {

	@Autowired
	EPOSTransactionDao eposTransactionDao;

	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	private SmsSender smsSender;

	@Autowired
	private UserDao userDao;

	@Autowired
	private AWSSESEmailService awsSESEmailService;

	@Autowired
	private PDFCreator pdfCreator;

	private String fileName;

	private static Logger logger = LoggerFactory.getLogger(HandleEPOSSaleResponse.class.getName());

	public void handleSuccessResponse(Map<String, String> requestMap, HttpServletRequest httpRequest) {
		try {
			logger.info("Handling EPOS Sale Captured response");
			EPOSTransaction epos = eposTransactionDao.findByInvoiceId(requestMap.get("ORDER_ID"));
			assistPdfCreator(requestMap, httpRequest, epos);
			Runnable runnable = new Runnable() {
				public void run() {
					try {
						handleEposSuccessSmsToMerchant(epos);
						handleEposSuccessSms(epos);
					} catch (Exception e) {
						logger.error("Exception in sending SMS ", e);
					}
				}
			};
			
			propertiesManager.executorImpl(runnable);

			fileName = "Receipt_" + requestMap.get("ORDER_ID") + ".pdf";
			File file = new File(fileName);

			pdfCreator.createEposPdf(epos, file);
			if (StringUtils.isNotBlank(epos.getCUST_EMAIL())) {
				String subject = "Payment Success -- Invoice ID: " + requestMap.get("ORDER_ID");
				awsSESEmailService.eposEmailWithAttachmentToCustomer(epos, subject, epos.getCUST_EMAIL(), true, file);
			}
			User merchant = userDao.findPayId(epos.getPAY_ID());
			if (StringUtils.isNotBlank(merchant.getEmailId())) {
				String subject = "Payment Received -- Invoice ID: " + requestMap.get("ORDER_ID");
				awsSESEmailService.eposEmailWithAttachmentToMerchant(epos, subject, merchant.getEmailId(), file);
			}
		} catch (Exception e) {
			logger.error("Exception caught, " + e);
		}
	}

	private void handleEposSuccessSmsToMerchant(EPOSTransaction epos) {
		String businessName = "";
		StringBuilder message = new StringBuilder();
		if (StringUtils.isNotBlank(epos.getBUSINESS_NAME())) {
			businessName = epos.getBUSINESS_NAME();
		} else {
			businessName = "Merchant";
		}
		message.append("Dear " + businessName + ",\n");
		message.append("Payment of " + Currency.getAlphabaticCode(epos.getCURRENCY_CODE()) + " " + epos.getAMOUNT());
		message.append(" has been made by Customer with ");
		if (StringUtils.isNotBlank(epos.getCUST_MOBILE()) && StringUtils.isNotBlank(epos.getCUST_EMAIL())) {
			message.append("Mobile: " + epos.getCUST_MOBILE() + " and Email ID: " + epos.getCUST_EMAIL());
		} else if (StringUtils.isNotBlank(epos.getCUST_MOBILE())) {
			message.append("Mobile: " + epos.getCUST_MOBILE());
		} else if (StringUtils.isNotBlank(epos.getCUST_EMAIL())) {
			message.append("Email ID: " + epos.getCUST_EMAIL());
		}
		message.append("\nThanks,\n");
		message.append("Team Payment Gateway");
		try {
			logger.info("Sending success SMS to Merchant, SMS: " + message.toString());
			
			String smsInnuvisolutions = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
			
			if(StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {
				 smsSender.sendSMSByInnvisSolution(userDao.findByEmailId(epos.getCREATED_BY()).getMobile(), message.toString());
			}else {
			     smsSender.sendSMS(userDao.findByEmailId(epos.getCREATED_BY()).getMobile(), message.toString());
			}
			//smsSender.sendSMS(userDao.findByEmailId(epos.getCREATED_BY()).getMobile(), message.toString());
		} catch (Exception e) {
			logger.error("Exception caught while sending EPOS sale success SMS to the merchant " + e);
		}
	}

	public void handleFailureResponse(Map<String, String> requestMap, HttpServletRequest httpRequest) {
		logger.info("Handling EPOS sale failure response");
		try {
			EPOSTransaction epos = eposTransactionDao.findByInvoiceId(requestMap.get("ORDER_ID"));
			Runnable runnable =  new Runnable() {
				public void run() {
					try {
						handleEposSaleFailureSms(epos);
					} catch (Exception e) {
						logger.error("Exception in sending SMS ", e);
					}
				}
			};
			propertiesManager.executorImpl(runnable);

			if (StringUtils.isNotBlank(epos.getCUST_EMAIL())) {
				String subject = "Payment Success -- Invoice ID: " + requestMap.get("ORDER_ID");
				awsSESEmailService.eposEmailWithAttachmentToCustomer(epos, subject, epos.getCUST_EMAIL(), false, null);
			}
		} catch (Exception e) {
			logger.error("Exception caught, " + e);
		}
	}

	private void handleEposSaleFailureSms(EPOSTransaction epos) {
		if (StringUtils.isNotBlank(epos.getCUST_MOBILE())) {
			StringBuilder message = new StringBuilder();
			message.append("Dear Customer, \n");
			message.append(
					"Your payment of " + Currency.getAlphabaticCode(epos.getCURRENCY_CODE()) + " " + epos.getAMOUNT());
			message.append(" with Invoice ID " + epos.getINVOICE_ID());
			if (StringUtils.isNotBlank(epos.getBUSINESS_NAME())) {
				message.append(" for " + epos.getBUSINESS_NAME());
			}
			message.append(" has failed.\nWe apologize for the inconvenience caused.\n");
			message.append("Thanks, \n");
			message.append("Team Payment Gateway");
			try {
				String smsInnuvisolutions = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
				String responseMsg;
				if(StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {
					 smsSender.sendSMSByInnvisSolution(epos.getCUST_MOBILE(), message.toString());
				}else {
				     smsSender.sendSMS(epos.getCUST_MOBILE(), message.toString());
				}
				//smsSender.sendSMS(epos.getCUST_MOBILE(), message.toString());
			} catch (Exception e) {
				logger.error("Exception caught while sending EPOS sale failure SMS to the customer with Mobile number "
						+ epos.getCUST_MOBILE() + ", " + e);
			}
		}
	}

	private void handleEposSuccessSms(EPOSTransaction epos) {
		if (StringUtils.isNotBlank(epos.getCUST_MOBILE())) {
			StringBuilder message = new StringBuilder();
			message.append("Dear Customer, \n");
			message.append(
					"Your payment of " + Currency.getAlphabaticCode(epos.getCURRENCY_CODE()) + " " + epos.getAMOUNT());
			message.append(" with Invoice ID " + epos.getINVOICE_ID());
			if (StringUtils.isNotBlank(epos.getBUSINESS_NAME())) {
				message.append(" for " + epos.getBUSINESS_NAME());
			}
			message.append(" is successful.");
			message.append("\nThanks, \n");
			message.append("Team Payment Gateway");
			try {
				
				String smsInnuvisolutions = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
				
				if(StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {
					 smsSender.sendSMSByInnvisSolution(epos.getCUST_MOBILE(), message.toString());
				}else {
				     smsSender.sendSMS(epos.getCUST_MOBILE(), message.toString());
				}
				//smsSender.sendSMS(epos.getCUST_MOBILE(), message.toString());
			} catch (Exception e) {
				logger.error("Exception caught while sending EPOS sale success SMS to the customer with Mobile number "
						+ epos.getCUST_MOBILE() + ", " + e);
			}
		}
	}

	private void assistPdfCreator(Map<String, String> requestMap, HttpServletRequest httpRequest,
			EPOSTransaction epos) {
		logger.info("Updating EPOS values into session map for PDF creation");
		String payId = null;
		String imageBase64 = null;
		if (StringUtils.isNotBlank(requestMap.get("PAY_ID"))) {
			payId = String.valueOf(requestMap.get("PAY_ID"));
		}

		if (StringUtils.isNotBlank(payId)) {
			try {
				String imageAddr = System.getenv("PG_PROPS") + "invoiceImage/" + payId + ".png";
				if (new File(imageAddr).exists()) {
					BufferedImage img = ImageIO.read(new File(imageAddr));
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ImageIO.write(img, "png", bos);
					byte[] imageBytes = bos.toByteArray();
					BASE64Encoder encoder = new BASE64Encoder();
					imageBase64 = encoder.encode(imageBytes);
					bos.close();
				}
			} catch (IOException e) {
				logger.error("Exception in generating base 64 for merchant logo");
			}
		}

		String createDate = requestMap.get(FieldType.RESPONSE_DATE_TIME.getName());

		String date = createDate.substring(0, 10);
		String time = createDate.substring(11, createDate.length());

//		sessionMap = (SessionMap<String, Object>) ActionContext.getContext().getSession();

		if (StringUtils.isNotBlank(payId)) {
			httpRequest.setAttribute("PAY_ID", payId);
		} else {
			httpRequest.setAttribute("PAY_ID", "");
		}

		if (StringUtils.isNotBlank(imageBase64)) {
			httpRequest.setAttribute("IMAGE_MERCHANT", imageBase64);
		} else {
			httpRequest.setAttribute("IMAGE_MERCHANT", "");
		}

		if (StringUtils.isNotBlank(requestMap.get("ORDER_ID"))) {
			httpRequest.setAttribute(Constants.ORDER_ID.getValue(), requestMap.get("ORDER_ID"));
		} else {
			httpRequest.setAttribute(Constants.ORDER_ID.getValue(), "");
		}

		if (StringUtils.isNotBlank(requestMap.get("TOTAL_AMOUNT"))) {
			httpRequest.setAttribute("TOTAL_AMOUNT", Amount.toDecimal(requestMap.get("TOTAL_AMOUNT"), "356"));
		} else if (StringUtils.isNotBlank(requestMap.get("AMOUNT"))) {
			httpRequest.setAttribute("TOTAL_AMOUNT", Amount.toDecimal(requestMap.get("AMOUNT"), "356"));
		} else {
			httpRequest.setAttribute("TOTAL_AMOUNT", "");
		}

		if (StringUtils.isNotBlank(requestMap.get("STATUS"))) {
			httpRequest.setAttribute("STATUS", requestMap.get("STATUS"));
		} else {
			httpRequest.setAttribute("STATUS", "");
		}

		if (StringUtils.isNotBlank(requestMap.get("PG_REF_NUM"))) {
			httpRequest.setAttribute(Constants.PG_REF_NUM.getValue(), requestMap.get("PG_REF_NUM"));
		} else {
			httpRequest.setAttribute(Constants.PG_REF_NUM.getValue(), "");
		}

		if (StringUtils.isNotBlank(epos.getBUSINESS_NAME())) {
			httpRequest.setAttribute("MERCHANT_NAME", epos.getBUSINESS_NAME());
		} else {
			httpRequest.setAttribute("MERCHANT_NAME", "");
		}

		if (StringUtils.isNotBlank(epos.getINVOICE_ID())) {
			httpRequest.setAttribute("INVOICE_ID", epos.getINVOICE_ID());
		} else {
			httpRequest.setAttribute("INVOICE_ID", "");
		}

		if (StringUtils.isNotBlank(epos.getCUST_EMAIL())) {
			httpRequest.setAttribute("EMAIL", epos.getCUST_EMAIL());
		} else {
			httpRequest.setAttribute("EMAIL", "");
		}

		if (StringUtils.isNotBlank(epos.getCUST_MOBILE())) {
			httpRequest.setAttribute("MOBILE", epos.getCUST_MOBILE());
		} else {
			httpRequest.setAttribute("MOBILE", "");
		}

		if (StringUtils.isNotBlank(date)) {
			httpRequest.setAttribute("TXN_DATE", date);
		} else {
			httpRequest.setAttribute("TXN_DATE", "");
		}

		if (StringUtils.isNotBlank(time)) {
			httpRequest.setAttribute("TXN_TIME", time);
		} else {
			httpRequest.setAttribute("TXN_TIME", "");
		}

		if (StringUtils.isNotBlank(epos.getAMOUNT())) {
			httpRequest.setAttribute("AMOUNT", epos.getAMOUNT());
		} else {
			httpRequest.setAttribute("AMOUNT", "");
		}
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
