package com.paymentgateway.crm.action;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.SmsSender;
import com.paymentgateway.commons.user.AnalyticsData;
import com.paymentgateway.commons.user.MerchantSMSObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.crm.actionBeans.AnalyticsDataService;
import com.paymentgateway.crm.actionBeans.TransactionSummaryCountService;

public class SendPerformanceSmsAction extends AbstractSecureAction {

	private static final long serialVersionUID = -6323553936458486881L;

	private static Logger logger = LoggerFactory.getLogger(SendPerformanceSmsAction.class.getName());

	private String txnType;
	private String dateFrom;
	private String dateInitial;
	private String dateTo;
	public String paymentMethods;
	public String acquirer;
	private String merchantEmailId;
	private String smsParam;
	private String response;
	private String mopType;
	private String currency;

	@Autowired
	private AnalyticsDataService analyticsDataService;

	@Autowired
	private TransactionSummaryCountService transactionSummaryCountService;

	@Autowired
	private UserDao userDao;

	@Autowired
	private SmsSender smsSender;

	PropertiesManager propertiesManager = new PropertiesManager();

	public String execute() {
		logger.info("Inside SendPerformanceSmsAction execute");
		try {
			if (StringUtils.isBlank(acquirer)) {
				acquirer = "ALL";
			}
			if (StringUtils.isBlank(paymentMethods)) {
				paymentMethods = "ALL";
			}

			dateInitial = "2018-10-16 00:00:00";
			// dateInitial = "2019-01-16 00:00:00";
			dateFrom = DateCreater.toDateTimeformatCreater(dateFrom);
			dateTo = DateCreater.formDateTimeformatCreater(dateTo);
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


				if (StringUtils.isNotBlank(smsParam) && smsParam.equalsIgnoreCase("capturedData")) {

					logger.info("Preparing capturedData SMS ");
					AnalyticsData analyticsData = analyticsDataService.getTransactionCount(dateFrom, dateTo,
							merchantPayId, paymentMethods, acquirer, sessionUser, smsParam, txnType, mopType, currency);

					// Capture Data variables
					String date = dateFrom;
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
					
					logger.info("SMS Data Prepared ");
					// Create SMS body

					StringBuilder capturedSMSBody = new StringBuilder();

					SimpleDateFormat outFormat = new SimpleDateFormat("dd-MMM-yyyy");

					Date dateCapFrom = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH).parse(dateFrom);
					Date dateCapTo = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH).parse(dateTo);

					String dateCapFromString = outFormat.format(dateCapFrom);
					String dateCapToString = outFormat.format(dateCapTo);

					capturedSMSBody.append("Day End Captured Data for merchant " + merchantName + "\n");
					capturedSMSBody.append("\n");
					if (dateCapFromString.equalsIgnoreCase(dateCapToString)) {
						capturedSMSBody.append("Capture Date: " + dateCapFromString + " \n");
					} else {
						capturedSMSBody
								.append("Capture Date: From " + dateCapFromString + " to " + dateCapToString + "\n");
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
					capturedSMSBody.append("Payment Gateway Avg. Trans Amt: "
							+ ((format(String.format("%.0f", Double.valueOf(paymentGatewayAvgTransAmt)))) + ".00"));
				
					logger.info("SMS Data Sending =  " + capturedSMSBody.toString());
					String smsSendingList = merchant.getTransactionSms();
					String[] smsSendingListArray = smsSendingList.split(",");
					StringBuilder finalMessage = new StringBuilder();
					if (smsSendingList.length() == 0) {
						finalMessage.append("No mobile number configured");
					} else {
						for (String mobileNo : smsSendingListArray) {
							//logger.info("SMS sent to mobile  " + mobileNo);
							
							String smsInnuvisolutions = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
							String responseMsg;
							if(StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {
								responseMsg = smsSender.sendSMSByInnvisSolution(mobileNo, capturedSMSBody.toString());
							}else {
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
						}

						if (StringUtils.isNotBlank(smsFailedlist.toString())) {
							finalMessage.append("  Failed to sent sms on  " + smsFailedlist.toString() + "\n");
						}
					}
					setResponse(finalMessage.toString());
				} else if (StringUtils.isNotBlank(smsParam)
						&& smsParam.equalsIgnoreCase("paymentGatewayCapturedData")) {

					logger.info("Preparing PaymentGatewaycapturedData SMS ");

					AnalyticsData analyticsData = analyticsDataService.getTransactionCount(dateFrom, dateTo,
							merchantPayId, paymentMethods, acquirer, sessionUser, smsParam, txnType, mopType, currency);

					AnalyticsData analyticsDataCumm = analyticsDataService.getTransactionCount(dateInitial, dateTo,
							merchantPayId, paymentMethods, acquirer, sessionUser, smsParam, txnType, mopType, currency);

					AnalyticsData analyticsDataPaymentGatewayProfit = analyticsDataService.getMerchantSMSData(dateFrom, dateTo,
							merchantPayId, paymentMethods, acquirer, sessionUser, smsParam);

					AnalyticsData analyticsDataCummPaymentGatewayProfit = analyticsDataService.getMerchantSMSData(dateInitial,
							dateTo, merchantPayId, paymentMethods, acquirer, sessionUser, smsParam);

					AnalyticsData analyticsDataPaymentGatewayProfitIncGst = analyticsDataService
							.getTransactionTotalProfitCount(dateFrom, dateTo, merchantPayId, paymentMethods, acquirer,
									sessionUser, smsParam);

					AnalyticsData analyticsDataCummPaymentGatewayProfitIncGst = analyticsDataService
							.getTransactionTotalProfitCount(dateInitial, dateTo, merchantPayId, paymentMethods,
									acquirer, sessionUser, smsParam);

					// Capture Cumm Data variables
					String totTransCountCumm = analyticsDataCumm.getSuccessTxnCount();
					String totTransAmountCumm = analyticsDataCumm.getTotalCapturedTxnAmount();
					String paymentGatewayProfitCumm = analyticsDataCummPaymentGatewayProfitIncGst.getpaymentGatewayProfitInclGstCumm(); 
					String paymentGatewayProfitInclGstCumm = analyticsDataCummPaymentGatewayProfitIncGst.getpaymentGatewayProfitCumm();
						
					logger.info("Preparing paymentGatewaycapturedData SMS Capture Cumm Data variables" + totTransCountCumm);
					BigDecimal totalTransCountCumm = new BigDecimal(totTransCountCumm.replace(",", ""));

					// calculation for AvgPaymentGatewayProfit IncGstCumm
					BigDecimal profitIncGstCumm = new BigDecimal(paymentGatewayProfitInclGstCumm.replace(",", ""));
					BigDecimal calValueGstCumm = profitIncGstCumm.divide(totalTransCountCumm, 2,
							RoundingMode.HALF_DOWN);
					String avgPaymentGatewayProfitInclGstCumm = calValueGstCumm.toString();
					logger.info("Preparing PaymentGatewaycapturedData SMS C/calculation for AvgpaymentGatewayProfit IncGstCumms"
							+ avgPaymentGatewayProfitInclGstCumm);
					// calculation for Avg PaymentGateway Profit ExcGstCumm
					String stCumm = analyticsDataCummPaymentGatewayProfitIncGst.getGst();
					String st = analyticsDataPaymentGatewayProfitIncGst.getGst();

					logger.info("Preparing PaymentGatewaycapturedData SMS calculation for AvgPaymentGatewayProfit ExcGstCumm" + st);
					BigDecimal perc = new BigDecimal(stCumm);
					BigDecimal amt = new BigDecimal(paymentGatewayProfitInclGstCumm.replace(",", ""));
					BigDecimal ONE_HUNDRED = new BigDecimal(100);
					BigDecimal calValue = amt.multiply(perc).divide(ONE_HUNDRED);
					BigDecimal finalAmt = amt.subtract(calValue);
					BigDecimal paymentGatewayProfitExcGstCumm = finalAmt.divide(totalTransCountCumm, 2,
							RoundingMode.HALF_DOWN);
					String avgPaymentGatewayProfitExcGstCumm = paymentGatewayProfitExcGstCumm.toString();

					// Capture Data variables
					String date = dateFrom;
					String merchantName = merchant.getBusinessName();
					String totTransCount = analyticsData.getSuccessTxnCount();
					String totTransAmount = analyticsData.getTotalCapturedTxnAmount();
					String ccPercentShare = analyticsData.getCCTxnPercent();
					String dcPercentShare = analyticsData.getDCTxnPercent();
					String upiPercentShare = analyticsData.getUPTxnPercent();
					String PaymentGatewayAvgTransAmt = analyticsData.getAvgTkt();
					String looktoBook = analyticsData.getSuccessTxnPercent();
					String rejectionRate = analyticsData.getTotalRejectedTxnPercent();
					String paymentGatewayProfit = analyticsDataPaymentGatewayProfitIncGst.getpaymentGatewayProfitExcGstCumm();   
					String paymentGateayProfitInclGst = analyticsDataPaymentGatewayProfitIncGst.getpaymentGatewayProfitInclGstCumm();

					BigDecimal totalTransCount = new BigDecimal(totTransCount.replace(",", ""));

					// calculation for Avg Payment Gateway Profit IncGst
					BigDecimal profitIncGst = new BigDecimal(paymentGateayProfitInclGst.replace(",", ""));
					BigDecimal calValueGst = profitIncGst.divide(totalTransCount, 2, RoundingMode.HALF_DOWN);
					String avgPaymentGatewayProfitInclGst = calValueGst.toString();

					// calculation for Payment Gateway Profit ExcGst
					BigDecimal perc2 = new BigDecimal(st);
					BigDecimal amt2 = new BigDecimal(paymentGateayProfitInclGst.replace(",", ""));
					BigDecimal ONE_HUNDRED2 = new BigDecimal(100);
					BigDecimal calValue2 = amt2.multiply(perc2).divide(ONE_HUNDRED2);
					BigDecimal finalAmt2 = amt2.subtract(calValue2);
					BigDecimal paymentGatewayProfitExcGst = finalAmt2.divide(totalTransCount, 2, RoundingMode.HALF_DOWN);
					String avgPaymentGatewayProfitExcGst = paymentGatewayProfitExcGst.toString();

					logger.info("SMS Data Prepared ");
					// Create SMS body

					StringBuilder capturedSMSBody = new StringBuilder();

					SimpleDateFormat outFormat = new SimpleDateFormat("dd-MMM-yyyy");

					Date dateCapFrom = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH).parse(dateFrom);
					Date dateCapTo = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH).parse(dateTo);

					String dateCapFromString = outFormat.format(dateCapFrom);
					String dateCapToString = outFormat.format(dateCapTo);

					capturedSMSBody.append("Day End Captured Data for merchant " + merchantName + "  \n");
					capturedSMSBody.append(" \n");
					capturedSMSBody.append("Cumm Txn Count: " + totTransCountCumm + "\n");
					capturedSMSBody.append("Cumm Txn Amt: INR  "
							+ (format(String.format("%.0f", Double.valueOf(totTransAmountCumm.replace(",", "")))))
							+ ".00" + "\n");
					capturedSMSBody.append("Cumm PaymentGateway Profit: "
							+ (format(String.format("%.0f", Double.valueOf(paymentGatewayProfitCumm.replace(",", "")))))
							+ ".00" + "\n");
					capturedSMSBody
							.append("Cumm PaymentGateway Profit/Txn(Inc.GST): "
									+ (format(String.format("%.0f",
											Double.valueOf(avgPaymentGatewayProfitInclGstCumm.replace(",", "")))))
									+ ".00" + " \n"); // to chng
					capturedSMSBody.append("Cumm PaymentGateway Profit/Txn(Exc.GST): "
							+ (format(
									String.format("%.0f", Double.valueOf(avgPaymentGatewayProfitExcGstCumm.replace(",", "")))))
							+ ".00" + " \n"); // to chng

					capturedSMSBody.append(" \n");
					capturedSMSBody.append(" \n");

					if (dateCapFromString.equalsIgnoreCase(dateCapToString)) {
						capturedSMSBody.append("Capture Date: " + dateCapFromString + " \n");
					} else {
						capturedSMSBody
								.append("Capture Date: From " + dateCapFromString + " to " + dateCapToString + "\n");
					}
					capturedSMSBody.append(" \n");
					capturedSMSBody.append("Tot. Txn Count: " + totTransCount + "\n");
					capturedSMSBody.append("Tot. Txn Amt: INR  "
							+ (format(String.format("%.0f", Double.valueOf(totTransAmount.replace(",", ""))))) + ".00"
							+ "\n");
					capturedSMSBody.append("CC Share: " + ccPercentShare + "% \n");
					capturedSMSBody.append("DC Share: " + dcPercentShare + "% \n");
					capturedSMSBody.append("UP Share: " + upiPercentShare + "% \n");
					
					capturedSMSBody.append("Payment Gateway Profit: "
							+ (format(String.format("%.0f", Double.valueOf(paymentGatewayProfit.replace(",", ""))))) + ".00"
							+ "\n");
					capturedSMSBody.append("Payment Gateway Profit/Txn(Inc.GST): "
							+ (format(String.format("%.0f", Double.valueOf(avgPaymentGatewayProfitInclGst.replace(",", "")))))
							+ ".00" + " \n");
					capturedSMSBody.append("Payment Gateway Profit/Txn(Exc.GST): "
							+ (format(String.format("%.0f", Double.valueOf(avgPaymentGatewayProfitExcGst.replace(",", "")))))
							+ ".00" + " \n");
					
					logger.info("SMS Data Sending =  " + capturedSMSBody.toString());
					StringBuilder finalMessage = new StringBuilder();
					setResponse(finalMessage.toString());
				}

				else if (StringUtils.isNotBlank(smsParam) && smsParam.equalsIgnoreCase("settledData")) {

					logger.info("Preparing Merchant SMS ");

					MerchantSMSObject merchantSMSObject = transactionSummaryCountService.getMerchantSMSData(dateFrom,
							dateTo, merchantPayId, paymentMethods, acquirer, sessionUser, 1, 1, "ALL", "ALL", "ALL",
							TransactionType.SALE.getName());

					// Settled Data variables

					String merchantName = merchant.getBusinessName();
					logger.info("Mechant SMS data prepared");
					// Create SMS body

					StringBuilder settledSMSBody = new StringBuilder();

					settledSMSBody.append("Day End Settlement Data for merchant " + merchantName);
					// settledSMSBody.append(" \n");
					settledSMSBody.append("\n\nCapture Date: " + merchantSMSObject.getDateCaptured());
					settledSMSBody.append("\nSettlement Date: " + merchantSMSObject.getDateSettled());
					// settledSMSBody.append(" \n");
					settledSMSBody.append("\n\nTotal Sale Amount: " + merchantSMSObject.getTotalSettledAmount());
					settledSMSBody.append("\n\nCC  Sale Amount: " + merchantSMSObject.getCcSettledAmt());
					settledSMSBody.append("\nDC  Sale Amount: " + merchantSMSObject.getDcSettledAmt());
					settledSMSBody.append("\nUPI Sale Amount: " + merchantSMSObject.getUpSettledAmt());
					settledSMSBody.append("\nNB Sale Amount: " + merchantSMSObject.getNbSettledAmt());
					settledSMSBody.append("\nWL Sale Amount: " + merchantSMSObject.getWlSettledAmt());
					settledSMSBody.append("\nEM Sale Amount: " + merchantSMSObject.getEmSettledAmt());
					settledSMSBody.append("\nCD Sale Amount: " + merchantSMSObject.getCdSettledAmt());
					// settledSMSBody.append(" \n");
					if(StringUtils.isNotBlank(merchantSMSObject.getTotalTxnCount())) {
						settledSMSBody.append("\n\nTotal Settled Txn count: " + merchantSMSObject.getTotalTxnCount());
					}else {
						settledSMSBody.append("\n\nTotal Settled Txn count: 0");
					}
					
					settledSMSBody.append("\n\nCC  TXN: " + merchantSMSObject.getCcTxnPer());
					settledSMSBody.append("\nDC  TXN: " + merchantSMSObject.getDcTxnPer());
					settledSMSBody.append("\nUPI TXN: " + merchantSMSObject.getUpTxnPer());
					settledSMSBody.append("\nNB TXN: " + merchantSMSObject.getNbTxnPer());
					settledSMSBody.append("\nWL TXN: " + merchantSMSObject.getWlTxnPer());
					settledSMSBody.append("\nEM TXN: " + merchantSMSObject.getEmTxnPer());
					settledSMSBody.append("\nCD TXN: " + merchantSMSObject.getCdTxnPer());
					settledSMSBody.append(" \n");
					settledSMSBody.append("Total Settled Amount (Sale settled - Refund settled): "
							+ merchantSMSObject.getSettledAmount() + "\n");
					logger.info("Mechant SMS Prepared  " + settledSMSBody.toString());
					String smsSendingList = merchant.getTransactionSms();
					String[] smsSendingListArray = smsSendingList.split(",");
					StringBuilder finalMessage = new StringBuilder();
					if (smsSendingList.length() == 0) {
						finalMessage.append("No mobile number configured");
					} else {
						for (String mobileNo : smsSendingListArray) {
							logger.info("SMS sent to mobile  " + mobileNo);
							
							String smsInnuvisolutions = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
							String responseMsg;
							if(StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {
								responseMsg = smsSender.sendSMSByInnvisSolution(mobileNo, settledSMSBody.toString());
							}else {
								responseMsg = smsSender.sendSMS(mobileNo, settledSMSBody.toString());
							}
							
							//String response = smsSender.sendSMS(mobileNo, settledSMSBody.toString());
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
						}

						if (StringUtils.isNotBlank(smsFailedlist.toString())) {
							finalMessage.append("  Failed to sent sms on  " + smsFailedlist.toString() + "\n");
						}
					}
					setResponse(finalMessage.toString());
				}

			}

		} catch (Exception e) {
			logger.error("Exception in getting transaction summary count data for SMS " , e);
		}

		return SUCCESS;
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

	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public String getMerchantEmailId() {
		return merchantEmailId;
	}

	public void setMerchantEmailId(String merchantEmailId) {
		this.merchantEmailId = merchantEmailId;
	}

	public String getPaymentMethods() {
		return paymentMethods;
	}

	public void setPaymentMethods(String paymentMethods) {
		this.paymentMethods = paymentMethods;
	}

	public String getDateInitial() {
		return dateInitial;
	}

	public void setDateInitial(String dateInitial) {
		this.dateInitial = dateInitial;
	}

	public String getSmsParam() {
		return smsParam;
	}

	public void setSmsParam(String smsParam) {
		this.smsParam = smsParam;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}
	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getMopType() {
		return mopType;
	}

	public void setMopType(String mopType) {
		this.mopType = mopType;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
}
