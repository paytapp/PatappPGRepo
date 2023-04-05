package com.paymentgateway.notification.sms.smsCreater;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.SmsSender;
import com.paymentgateway.commons.user.AnalyticsData;
import com.paymentgateway.commons.user.MerchantDailySMSObject;
import com.paymentgateway.commons.user.TransactionCountSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class SendPerformanceSmsServiceSMS {

	private static Logger logger = LoggerFactory.getLogger(SendPerformanceSmsServiceSMS.class.getName());

	@Autowired
	private AnalyticsDataServiceSMS analyticsDataService;

	@Autowired
	private TransactionSummaryCountServiceSMS transactionSummaryCountService;

	@Autowired
	private UserDao userDao;

	@Autowired
	private SmsSender smsSender;
	
	@Autowired
	private Fields field;

	public String sendSms(String merchantPayId, String smsParam) {
		StringBuilder finalMessage = new StringBuilder();
		Date dateToday = new Date();
		Date yesterdayDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
		String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(dateToday);
		String modifiedDateYesterday = new SimpleDateFormat("yyyy-MM-dd").format(yesterdayDate);

		String dateTodayFrom = modifiedDate + " 00:00:00";
		String dateTodayTo = modifiedDate + " 23:59:59";

		String dateYesterDayFrom = modifiedDateYesterday + " 00:00:00";
		String dateYesterDayTo = modifiedDateYesterday + " 23:59:59";

		String dateInitial = "2018-10-16 00:00:00";
		String paymentMethods = "ALL";
		String acquirer = "ALL";

		logger.info("Inside SendPerformanceSmsServiceSMS sendSms");
		try {
			if (StringUtils.isBlank(acquirer)) {
				acquirer = "ALL";
			}

			if (StringUtils.isBlank(paymentMethods)) {
				paymentMethods = "ALL";
			}

			StringBuilder smsSuccesslist = new StringBuilder();
			StringBuilder smsFailedlist = new StringBuilder();

			User merchant = new User();
			if (!merchantPayId.equalsIgnoreCase("All")) {
				merchant = userDao.findPayId(merchantPayId);
			}

			if (StringUtils.isBlank(merchant.getTransactionSms())) {
				String message = "No mobile number configured for this merchant to send SMS !";
				logger.info(message);
				return message;
			}

			if (StringUtils.isNotBlank(smsParam) && smsParam.equalsIgnoreCase("capturedData")) {

				logger.info("Preparing capturedData SMS ");
				AnalyticsData analyticsData = analyticsDataService.getTransactionCount(dateYesterDayFrom,
						dateYesterDayTo, merchantPayId, paymentMethods, acquirer, null, smsParam);

				// Capture Data variables
				String date = dateYesterDayFrom;
				String merchantName = merchant.getBusinessName();
				String totTransCount = analyticsData.getSuccessTxnCount();
				String totTransAmount = analyticsData.getTotalCapturedTxnAmount();
				String ccPercentShare = analyticsData.getCCTxnPercent();
				String dcPercentShare = analyticsData.getDCTxnPercent();
				String upiPercentShare = analyticsData.getUPTxnPercent();
				String nbPercentShare = analyticsData.getNBTxnPercent();
				String wlPercentShare = analyticsData.getWLTxnPercent();
				String emPercentShare = analyticsData.getEMTxnPercent();
				String cdPercentShare = analyticsData.getCDTxnPercent();
				String paymentGatewayAvgTransAmt = analyticsData.getAvgTkt();
				String looktoBook = analyticsData.getSuccessTxnPercent();
				String rejectionRate = analyticsData.getTotalRejectedTxnPercent();

				logger.info("SMS Data Prepared ");
				// Create SMS body

				StringBuilder capturedSMSBody = new StringBuilder();

				SimpleDateFormat outFormat = new SimpleDateFormat("dd-MMM-yyyy");

				Date dateCapFrom = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH).parse(dateYesterDayFrom);
				Date dateCapTo = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH).parse(dateYesterDayTo);

				String dateCapFromString = outFormat.format(dateCapFrom);
				String dateCapToString = outFormat.format(dateCapTo);

				capturedSMSBody.append("Day End Captured Data for merchant " + merchantName + "\n");
				capturedSMSBody.append("\n");
				if (dateCapFromString.equalsIgnoreCase(dateCapToString)) {
					capturedSMSBody.append("Capture Date: " + dateCapFromString + " \n");
				} else {
					capturedSMSBody.append("Capture Date: From " + dateCapFromString + " to " + dateCapToString + "\n");
				}

				capturedSMSBody.append("Tot. Trans Count: " + totTransCount + "\n");
				capturedSMSBody.append("Tot. Trans Amt: "
						+ (format(String.format("%.0f", Double.valueOf(totTransAmount)))) + ".00" + "\n");
				capturedSMSBody.append("CC Share: " + ccPercentShare + "% \n");
				capturedSMSBody.append("DC Share: " + dcPercentShare + "% \n");
				capturedSMSBody.append("UP Share: " + upiPercentShare + "% \n");
				capturedSMSBody.append("NB Share: " + nbPercentShare + "% \n");
				capturedSMSBody.append("WL Share: " + wlPercentShare + "% \n");
				capturedSMSBody.append("EM Share: " + emPercentShare + "% \n");
				capturedSMSBody.append("CD Share: " + cdPercentShare + "% \n");
				capturedSMSBody.append("Payment GateWay Avg. Trans Amt: "
						+ ((format(String.format("%.0f", Double.valueOf(paymentGatewayAvgTransAmt)))) + ".00"));
				// capturedSMSBody.append("Look to Book: " + looktoBook + " \n");
				// capturedSMSBody.append("Rejection Rate: " + rejectionRate + " %");

				logger.info("SMS Data Sending =  " + capturedSMSBody.toString());
				String smsSendingList = merchant.getTransactionSms();
				String[] smsSendingListArray = smsSendingList.split(",");

				if (smsSendingList.length() == 0) {
					finalMessage.append("No mobile number configured");
				} else {
					for (String mobileNo : smsSendingListArray) {
						//logger.info("SMS sent to mobile  " + mobileNo);
						
						String smsInnuvisolutions = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
						String responseMsg;
						if(StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {
							logger.info("Sms Send by smsInnuvisolutions");
							 responseMsg = smsSender.sendSMSByInnvisSolution(mobileNo, capturedSMSBody.toString());
						}else {
							logger.info("Normal sms Send");
						     responseMsg = smsSender.sendSMS(mobileNo, capturedSMSBody.toString());
						}
						
						//String response = smsSender.sendSMS(mobileNo, capturedSMSBody.toString());
						if (responseMsg.contains("success") || responseMsg.contains("000")) {
							smsSuccesslist.append(mobileNo);
							smsSuccesslist.append(" , ");
						} else {
							smsFailedlist.append(mobileNo);
							smsFailedlist.append(" , ");
						}
					}
					if (smsSuccesslist.length() > 1) {
						smsSuccesslist.deleteCharAt(smsSuccesslist.length() - 2);
					}
					if (smsFailedlist.length() > 1) {
						smsFailedlist.deleteCharAt(smsFailedlist.length() - 2);
					}

					if (StringUtils.isNotBlank(smsSuccesslist.toString())) {
						finalMessage.append("SMS sent successfully to " + smsSuccesslist.toString() + "\n");
						logger.info(finalMessage.toString());
					}

					if (StringUtils.isNotBlank(smsFailedlist.toString())) {
						finalMessage.append("Failed to sent sms on  " + smsFailedlist.toString() + "\n");
						logger.info(finalMessage.toString());
					}
				}
			}

			else if (StringUtils.isNotBlank(smsParam) && smsParam.equalsIgnoreCase("settledData")) {

				logger.info("Preparing settledData SMS ");

				TransactionCountSearch transactionCountSearch = transactionSummaryCountService.getTransactionCount(
						dateTodayFrom, dateTodayTo, merchantPayId, paymentMethods, acquirer, null, 1, 1, "ALL", "ALL",
						"ALL", TransactionType.SALE.getName());

				TransactionCountSearch transactionCountSearchCumulative = transactionSummaryCountService
						.getTransactionCount(dateInitial, dateTodayTo, merchantPayId, paymentMethods, acquirer, null, 1,
								1, "ALL", "ALL", "ALL", TransactionType.SALE.getName());

				String dateSettled = dateTodayFrom;
				String merchantName = merchant.getBusinessName();
				String cumBookAmt = transactionCountSearchCumulative.getActualSettlementAmount();
				String cumProfit = transactionCountSearchCumulative.getTotalProfit();

				double totalSettledCumultive = Double.valueOf(transactionCountSearchCumulative.getSaleSettledCount());
				double postSettledTxnCountCumulative = Double
						.valueOf(transactionCountSearchCumulative.getPostSettledTransactionCount());
				double totalBookedCumulative = totalSettledCumultive - postSettledTxnCountCumulative;

				double postSettledTxnCount = Double.valueOf(transactionCountSearch.getPostSettledTransactionCount());

				double totalSettled = Double.valueOf(transactionCountSearch.getSaleSettledCount());
				double totalBooked = totalSettled - postSettledTxnCount;

				double successPer = 0.00;
				if (totalSettled > 0) {
					successPer = (totalBooked / totalSettled) * 100;
				}

				String totTransSettledCount = (String.format("%.0f", totalSettled));
				String transAmtSettled = transactionCountSearch.getSaleSettledAmount();
				String tktBookCount = (String.format("%.0f", totalBooked));
				String bookAmt = transactionCountSearch.getActualSettlementAmount();
				String paymentGateWayProfit = transactionCountSearch.getTotalProfit();

				String ccPercentShareSettled = transactionCountSearch.getCcSettledPercentage();
				String dcPercentShareSettled = transactionCountSearch.getDcSettledPercentage();
				String upPercentShareSettled = transactionCountSearch.getUpSettledPercentage();
				String successPercent = (String.format("%.2f", successPer));
				String avgBookingAmtSettled = transactionCountSearch.getAvgSettlementAmount();

				logger.info("SMS data prepared");
				// Create SMS body

				StringBuilder settledSMSBody = new StringBuilder();

				settledSMSBody.append("Day End Settled Data \n");
				settledSMSBody.append("Merchant: " + merchantName + " \n");
				settledSMSBody.append("Date: " + dateSettled + " \n");
				settledSMSBody.append("Cum. Book: " + totalBookedCumulative + " \n");
				settledSMSBody.append("Cum. Book Amt: " + cumBookAmt + "\n");
				settledSMSBody.append("Cum. Profit: " + cumProfit + " \n");
				settledSMSBody.append("Tot. Trans: " + totTransSettledCount + " \n");
				settledSMSBody.append("Trans Amt: " + transAmtSettled + " \n");
				settledSMSBody.append("Tkt Book: " + tktBookCount + " \n");
				settledSMSBody.append("Book Amt: " + bookAmt + " \n");
				settledSMSBody.append("Payment GateWay Profit: " + paymentGateWayProfit + " \n");
				settledSMSBody.append("CC % Share: " + ccPercentShareSettled + " \n");
				settledSMSBody.append("DC % Share: " + dcPercentShareSettled + " \n");
				settledSMSBody.append("UPI % Share: " + upPercentShareSettled + " \n");
				settledSMSBody.append("Success %: " + successPercent + " \n");
				settledSMSBody.append("Avg. Booking Amt: " + avgBookingAmtSettled + " \n");

				logger.info("SMS Prepared  " + settledSMSBody.toString());

				String smsSendingList = merchant.getTransactionSms();

				String[] smsSendingListArray = smsSendingList.split(",");

				for (String mobileNo : smsSendingListArray) {
					String smsInnuvisolutions = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
					if(StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {
						logger.info("Sms Send by smsInnuvisolutions");
						 smsSender.sendSMSByInnvisSolution(mobileNo, settledSMSBody.toString());
					}else {
						logger.info("Normal sms send");
					     smsSender.sendSMS(mobileNo, settledSMSBody.toString());
					}
					//smsSender.sendSMS(mobileNo, settledSMSBody.toString());
					logger.info("SMS sent to mobile  " + field.fieldMask(mobileNo));

				}

				if (StringUtils.isNotBlank(smsSuccesslist.toString())) {
					finalMessage.append("SMS sent successfully to " + smsSuccesslist.toString() + "\n");
				}

				if (StringUtils.isNotBlank(smsFailedlist.toString())) {
					finalMessage.append("  Failed to sent sms on  " + smsFailedlist.toString() + "\n");
				}

			} else if (StringUtils.isNotBlank(smsParam) && smsParam.equalsIgnoreCase("merchantData")) {
				logger.info("Preparing Data for Merchant SMS New");

				MerchantDailySMSObject merchantSMSObject = transactionSummaryCountService.getMerchantSMSDailyData(
						dateYesterDayFrom, dateYesterDayTo, merchantPayId, paymentMethods, acquirer, null, 1, 1, "ALL",
						"ALL", "ALL", TransactionType.SALE.getName());

				String merchantName = merchant.getBusinessName();
				logger.info("Mechant SMS data prepared");
				// Create SMS body

				StringBuilder capturedSMSBody = new StringBuilder();

				capturedSMSBody.append("Day End Captured Data for merchant: " + merchantName + " \n");
				capturedSMSBody.append(" \n");
				capturedSMSBody.append("Captured Date: " + dateYesterDayFrom + "\n");
				capturedSMSBody.append("Total Booking: " + merchantSMSObject.getTotalBooking() + "\n");
				capturedSMSBody.append("Total Amount: " + merchantSMSObject.getTotalAmount() + "\n");
				capturedSMSBody.append("Total Comm. (inc.GST): " + merchantSMSObject.getTotalCommWithGST() + "\n");
				capturedSMSBody.append(" \n");
				capturedSMSBody.append("Bank Comm. (inc.GST): " + merchantSMSObject.getBankCommWithGST() + "\n");
				capturedSMSBody.append(
						merchantName + " Comm. (inc.GST): " + merchantSMSObject.getMerchantCommWithGST() + " \n");
				capturedSMSBody.append(" \n");

				capturedSMSBody
						.append("CC % Share in No. of Transaction: " + merchantSMSObject.getCcPercentTicket() + " %\n");
				capturedSMSBody.append("CC " + merchantName + " Comm. (inc.GST):"
						+ merchantSMSObject.getCcMerchantCommWithGST() + "\n");
				capturedSMSBody.append("CC Bank Comm. (inc.GST):" + merchantSMSObject.getCcBankCommWithGST() + "\n");

				capturedSMSBody
						.append("DC % Share in No. of Transaction: " + merchantSMSObject.getDcPercentTicket() + " %\n");
				capturedSMSBody.append("DC " + merchantName + " Comm. (inc.GST):"
						+ merchantSMSObject.getDcMerchantCommWithGST() + "\n");
				capturedSMSBody.append("DC Bank Comm. (inc.GST):" + merchantSMSObject.getDcBankCommWithGST() + "\n");

				capturedSMSBody.append(
						"UPI % Share in No. of Transaction: " + merchantSMSObject.getUpPercentTicket() + " %\n");
				capturedSMSBody.append("UPI " + merchantName + " Comm. (inc.GST):"
						+ merchantSMSObject.getUpMerchantCommWithGST() + "\n");
				capturedSMSBody.append("UPI Bank Comm. (inc.GST):" + merchantSMSObject.getUpBankCommWithGST() + "\n");

				capturedSMSBody.append(
						"WALLET % Share in No. of Transaction: " + merchantSMSObject.getWlPercentTicket() + " %\n");
				capturedSMSBody.append("WALLET " + merchantName + " Comm. (inc.GST):"
						+ merchantSMSObject.getWlMerchantCommWithGST() + "\n");
				capturedSMSBody
						.append("WALLET Bank Comm. (inc.GST):" + merchantSMSObject.getWlBankCommWithGST() + "\n");

				capturedSMSBody.append(
						"NETBANKING % Share in No. of Transaction: " + merchantSMSObject.getWlPercentTicket() + " %\n");
				capturedSMSBody.append("NETBANKING " + merchantName + " Comm. (inc.GST):"
						+ merchantSMSObject.getWlMerchantCommWithGST() + "\n");
				capturedSMSBody
						.append("NETBANKING Bank Comm. (inc.GST):" + merchantSMSObject.getWlBankCommWithGST() + "\n");

				capturedSMSBody.append(
						"COD % Share in No. of Transaction: " + merchantSMSObject.getWlPercentTicket() + " %\n");
				capturedSMSBody.append("COD " + merchantName + " Comm. (inc.GST):"
						+ merchantSMSObject.getWlMerchantCommWithGST() + "\n");
				capturedSMSBody.append("COD Bank Comm. (inc.GST):" + merchantSMSObject.getWlBankCommWithGST() + "\n");

				capturedSMSBody.append(
						"EMI % Share in No. of Transaction: " + merchantSMSObject.getWlPercentTicket() + " %\n");
				capturedSMSBody.append("EMI " + merchantName + " Comm. (inc.GST):"
						+ merchantSMSObject.getWlMerchantCommWithGST() + "\n");
				capturedSMSBody.append("EMI Bank Comm. (inc.GST):" + merchantSMSObject.getWlBankCommWithGST() + "\n");

				logger.info("Mechant SMS Prepared  " + capturedSMSBody.toString());
				String smsSendingList = merchant.getTransactionSms();

				String[] smsSendingListArray = smsSendingList.split(",");

				for (String mobileNo : smsSendingListArray) {

					try {
						logger.info("Start 2 sec delay in before sending next SMS");
						Thread.sleep(2000);
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}

					logger.info("End 2 sec delay in before sending next SMS");

					String smsInnuvisolutions = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
					
					if(StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {
						logger.info("Sms Send by smsInnuvisolutions");
						 smsSender.sendSMSByInnvisSolution(mobileNo, capturedSMSBody.toString());
					}else {
						logger.info("Normal sms send");
					     smsSender.sendSMS(mobileNo, capturedSMSBody.toString());
					}
					//smsSender.sendSMS(mobileNo, capturedSMSBody.toString());
					logger.info("SMS sent to mobile  " + field.fieldMask(mobileNo));
				}

			}

		} catch (Exception e) {
			logger.error("Exception in getting transaction summary count data for SMS " , e);
		}

		return finalMessage.toString();
	}

	public void checkLastTxn(String payId, String duration) {

		try {

			Date dateBefore = new Date(System.currentTimeMillis() - (Integer.valueOf(duration) * 60) * 1000);
			Date dateNow = new Date();

			String formattedDateBefore = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(dateBefore);
			String formattedDateNow = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(dateNow);

			boolean isTxnDone = analyticsDataService.isTxnCapturedForMerchant(payId, formattedDateBefore,
					formattedDateNow);
			if (!isTxnDone) {

				SimpleDateFormat outFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss");

				Date dateCapFrom = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH)
						.parse(formattedDateBefore);
				Date dateCapTo = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH).parse(formattedDateNow);

				String dateCapFromString = outFormat.format(dateCapFrom);
				String dateCapToString = outFormat.format(dateCapTo);

				User user = userDao.findPayId(payId);
				StringBuilder smsBody = new StringBuilder();
				smsBody.append("Alert ! No Successful Transaction Done in last " + duration + " minutes for Merchant "
						+ user.getBusinessName() + "\n");
				smsBody.append("From : " + dateCapFromString + "\n");
				smsBody.append("To : " + dateCapToString + "\n");

				logger.info("SMS Data Sending =  " + smsBody.toString());
				String smsSendingList = user.getTransactionSms();
				String[] smsSendingListArray = smsSendingList.split(",");

				for (String mobileNo : smsSendingListArray) {
					
					String smsInnuvisolutions = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
					if(StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {
						logger.info("Sms Send by smsInnuvisolutions");
						 smsSender.sendSMSByInnvisSolution(mobileNo, smsBody.toString());
					}else {
						logger.info("Normal Sms send");
					     smsSender.sendSMS(mobileNo, smsBody.toString());
					}
					//smsSender.sendSMS(mobileNo, smsBody.toString());
					logger.info("SMS sent to mobile  " + field.fieldMask(mobileNo));

				}
			}

		} catch (Exception e) {
			logger.error("Exception occured in sending hourly SMS   " , e);
		}
	}

	public String format(String amount) {
		StringBuilder stringBuilder = new StringBuilder();
		char amountArray[] = amount.toCharArray();
		int a = 0, b = 0;
		for (int i = amountArray.length - 1; i >= 0; i--) {
			if (a < 3) {
				stringBuilder.append(amountArray[i]);
				a++;
			} else if (b < 2) {
				if (b == 0) {
					stringBuilder.append(",");
					stringBuilder.append(amountArray[i]);
					b++;
				} else {
					stringBuilder.append(amountArray[i]);
					b = 0;
				}
			}
		}
		return stringBuilder.reverse().toString();
	}
}
