package com.paymentgateway.crm.actionBeans;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.apache.struts2.dispatcher.SessionMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.commons.api.SmsSender;
import com.paymentgateway.commons.user.AnalyticsData;
import com.paymentgateway.commons.user.TransactionCountSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;

@RestController
public class SendPerformanceSmsService {
	
	private static Logger logger = LoggerFactory.getLogger(SendPerformanceSmsService.class.getName());
	
	@Autowired
	private AnalyticsDataService analyticsDataService;
	
	@Autowired
	private TransactionSummaryCountService transactionSummaryCountService;
	
	@Autowired
	private UserDao userDao;

	@Autowired
	private SmsSender smsSender;
	
	private SessionMap<String, Object> sessionMap;
	
	@RequestMapping(method = RequestMethod.POST,value = "/sendPerformanceSms", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void sendPerformanceSms(@RequestParam String dateFrom, @RequestParam String dateTo, @RequestParam String merchantEmailId,
			@RequestParam String paymentMethods, @RequestParam String acquirer,@RequestParam String smsParam, @RequestParam String txnType){
		 //emailBuilder.remittanceProcessEmail(utr,payId,merchant,datefrom,netAmount,remittedDate,remittedAmount,status);
		String dateInitial;
		
		logger.info("Inside SendPerformanceSmsAction sendPerformanceSms Method");
		try {

			if (StringUtils.isBlank(acquirer)) {
				acquirer = "ALL";
			}

			if (StringUtils.isBlank(paymentMethods)) {
				paymentMethods = "ALL";
			}

			dateFrom = DateCreater.toDateTimeformatCreater(dateFrom);
			dateTo = DateCreater.formDateTimeformatCreater(dateTo);
			dateInitial = "2018-10-16 00:00:00";
			StringBuilder smsSuccesslist = new StringBuilder();
			StringBuilder smsFailedlist = new StringBuilder();

			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.SUPERADMIN)
					|| sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {

				String merchantPayId = null;
				User merchant = new User();
				if (!merchantEmailId.equalsIgnoreCase("All")) {
					merchant = userDao.findPayIdByEmail(merchantEmailId);
					merchantPayId = merchant.getPayId();
				} else {
					merchantPayId = merchantEmailId;
				}

				if (StringUtils.isBlank(merchant.getTransactionSms())) {
					//setResponse("No mobile number configured for this merchant to send SMS !");
					logger.info("No mobile number configured for this merchant to send SMS !");
					return;
				}

				if (StringUtils.isNotBlank(smsParam) && smsParam.equalsIgnoreCase("capturedData")) {

					logger.info("Preparing capturedData SMS ");
					AnalyticsData analyticsData = analyticsDataService.getTransactionCount(dateFrom, dateTo,
							merchantPayId, paymentMethods, acquirer, sessionUser, smsParam, txnType,"","");

					/*
					 * if (Integer.valueOf(analyticsData.getSuccessTxnCount()) < 1) {
					 * setResponse("Captured transaction count is Zero , SMS not sent"); return
					 * SUCCESS; }
					 */

					// Capture Data variables
					String date = dateFrom;
					String merchantName = merchant.getBusinessName();
					String totTransCount = analyticsData.getSuccessTxnCount();
					String totTransAmount = analyticsData.getTotalCapturedTxnAmount();
					String ccPercentShare = analyticsData.getCCTxnPercent();
					String dcPercentShare = analyticsData.getDCTxnPercent();
					String upiPercentShare = analyticsData.getUPTxnPercent();
					String paymentGatewayPayAvgTransAmt = analyticsData.getAvgTkt();
					String looktoBook = analyticsData.getSuccessTxnPercent();
					String rejectionRate = analyticsData.getTotalRejectedTxnPercent();

					logger.info("SMS Data Prepared ");
					// Create SMS body

					StringBuilder capturedSMSBody = new StringBuilder();

					capturedSMSBody.append("Day End Captured Data \n");
					capturedSMSBody.append("Date: " + date + " \n");
					capturedSMSBody.append("Merchant: " + merchantName + " \n");
					capturedSMSBody.append("Tot. Trans Count: " + totTransCount + "\n");
					capturedSMSBody.append("Tot. Trans Amt: " + totTransAmount + " \n");
					capturedSMSBody.append("CC % Share: " + ccPercentShare + " \n");
					capturedSMSBody.append("DC % Share: " + dcPercentShare + " \n");
					capturedSMSBody.append("UP % Share: " + upiPercentShare + " \n");
					capturedSMSBody.append("Gateway Avg. Trans Amt: " + paymentGatewayPayAvgTransAmt + " \n");
					capturedSMSBody.append("Look to Book: " + looktoBook + " \n");
					capturedSMSBody.append("Rejection Rate: " + rejectionRate + " \n");

					logger.info("SMS Data Sending =  " + capturedSMSBody.toString());
					String smsSendingList = merchant.getTransactionSms();
					String[] smsSendingListArray = smsSendingList.split(",");

					for (String mobileNo : smsSendingListArray) {

						//logger.info("SMS sent to mobile  " + mobileNo);
						
						String smsInnuvisolutions = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
						String response;
						if(StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {
							response = smsSender.sendSMSByInnvisSolution(mobileNo, capturedSMSBody.toString());
						}else {
							response = smsSender.sendSMS(mobileNo, capturedSMSBody.toString());
						}
						//String response = smsSender.sendSMS(mobileNo, capturedSMSBody.toString());

						if (response.contains("success") || response.contains("000")) {
							smsSuccesslist.append(mobileNo);
							smsSuccesslist.append(" , ");
						} else {
							smsFailedlist.append(mobileNo);
							smsFailedlist.append(" , ");
						}
					}

					StringBuilder finalMessage = new StringBuilder();

					if (StringUtils.isNotBlank(smsSuccesslist.toString())) {
						finalMessage.append("SMS sent successfully to " + smsSuccesslist.toString() + "\n");
					}

					if (StringUtils.isNotBlank(smsFailedlist.toString())) {
						finalMessage.append("  Failed to sent sms on  " + smsFailedlist.toString() + "\n");
					}

					logger.info(finalMessage.toString());
				}

				else if (StringUtils.isNotBlank(smsParam) && smsParam.equalsIgnoreCase("settledData")) {

					logger.info("Preparing settledData SMS ");

					TransactionCountSearch transactionCountSearch = transactionSummaryCountService.getTransactionCount(
							dateFrom, dateTo, merchantPayId, null, paymentMethods, acquirer, sessionUser, 1, 1, "ALL",
							"ALL", "ALL", TransactionType.SALE.getName(),"Settled","ALL","");

/*					if (Integer.valueOf(transactionCountSearch.getSaleSettledCount()) < 1) {
						setResponse("Settled transaction count is Zero , SMS not sent");
						return SUCCESS;
					}*/

					TransactionCountSearch transactionCountSearchCumulative = transactionSummaryCountService
							.getTransactionCount(dateInitial, dateTo, merchantPayId, null, paymentMethods, acquirer,
									sessionUser, 1, 1, "ALL", "ALL", "ALL", TransactionType.SALE.getName(),"Settled","ALL", "");
					// Settled Data variables

					String dateSettled = dateFrom;
					String merchantName = merchant.getBusinessName();
					String cumBookAmt = transactionCountSearchCumulative.getActualSettlementAmount();
					String cumProfit = transactionCountSearchCumulative.getTotalProfit();

					double totalSettledCumultive = Double.valueOf(transactionCountSearchCumulative.getSaleSettledCount());
					double postSettledTxnCountCumulative = Double
							.valueOf(transactionCountSearchCumulative.getPostSettledTransactionCount());
					double totalBookedCumulative = totalSettledCumultive - postSettledTxnCountCumulative;
					
					double postSettledTxnCount = Double
							.valueOf(transactionCountSearch.getPostSettledTransactionCount());
					
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
					String paymentGatewayProfit = transactionCountSearch.getTotalProfit();

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
					settledSMSBody.append("Gateway Profit: " + paymentGatewayProfit + " \n");
					settledSMSBody.append("CC % Share: " + ccPercentShareSettled + " \n");
					settledSMSBody.append("DC % Share: " + dcPercentShareSettled + " \n");
					settledSMSBody.append("UPI % Share: " + upPercentShareSettled + " \n");
					settledSMSBody.append("Success %: " + successPercent + " \n");
					settledSMSBody.append("Avg. Booking Amt: " + avgBookingAmtSettled + " \n");

					logger.info("SMS Prepared  " + settledSMSBody.toString());

					String smsSendingList = merchant.getTransactionSms();

					String[] smsSendingListArray = smsSendingList.split(",");

					for (String mobileNo : smsSendingListArray) {
						//logger.info("SMS sent to mobile  " + mobileNo);
						
						String smsInnuvisolutions = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
						String response;
						if(StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {
							response = smsSender.sendSMSByInnvisSolution(mobileNo, settledSMSBody.toString());
						}else {
							response = smsSender.sendSMS(mobileNo, settledSMSBody.toString());
						}
						
						//String response = smsSender.sendSMS(mobileNo, settledSMSBody.toString());

						if (response.contains("success") || response.contains("000")) {
							smsSuccesslist.append(mobileNo);
							smsSuccesslist.append(" , ");
						} else {
							smsFailedlist.append(mobileNo);
							smsFailedlist.append(" , ");
						}
					}

					StringBuilder finalMessage = new StringBuilder();

					if (StringUtils.isNotBlank(smsSuccesslist.toString())) {
						finalMessage.append("SMS sent successfully to " + smsSuccesslist.toString() + "\n");
					}

					if (StringUtils.isNotBlank(smsFailedlist.toString())) {
						finalMessage.append("  Failed to sent sms on  " + smsFailedlist.toString() + "\n");
					}

					logger.info(finalMessage.toString());
				}
			}

		} catch (Exception e) {
			logger.error("Exception in getting transaction summary count data for SMS " , e);
		}
	}
}
