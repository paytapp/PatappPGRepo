package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.SummaryReportObject;
import com.paymentgateway.commons.user.Surcharge;
import com.paymentgateway.commons.user.SurchargeDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.crm.actionBeans.DownloadReportGenerateService;

public class DownloadSummaryReportAction extends AbstractSecureAction {

	private static final long serialVersionUID = -1862272004872627431L;

	private static Logger logger = LoggerFactory.getLogger(DownloadSummaryReportAction.class.getName());
	
	@Autowired
	private DownloadReportGenerateService reportGenerateService;
	
	@Autowired
	private PropertiesManager propertiesManager;
		
	private List<Surcharge> surchargeList = new ArrayList<Surcharge>();
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
	private String currency;
	private String dateFrom;
	private String dateTo;
//	private String merchantPayId;
//	private String subMerchantPayId;
	private String merchantEmailId;
	private String subMerchantEmailId;
	private String paymentType;
	private String mopType;
	private String transactionType;
	private String status;
	private String acquirer;
	private String paymentMethods;
	private String partSettleflag;
	private InputStream fileInputStream;
	private String filename;
	private User sessionUser = new User();
	private String paymentsRegion;
	private String cardHolderType;
	private boolean generateReport;
	private String reportType;
	public String subUserPayId;
	private String transactionFlag;
	private User subuser = new User();
	

	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

	@Autowired
	private SummaryReportQuery summaryReportQuery;

	@Autowired
	private SurchargeDao surchargeDao;

	@Autowired
	private UserDao userDao;

	@SuppressWarnings("resource")
	public String execute() {

		logger.info("Inside download summary report action");
		if (acquirer == null || acquirer.isEmpty()) {
			acquirer = "ALL";

		}

		if (paymentsRegion == null || paymentsRegion.isEmpty()) {
			paymentsRegion = "ALL";

		}

		if (mopType == null || mopType.isEmpty()) {
			mopType = "ALL";

		}

		if (transactionType == null || transactionType.isEmpty()) {
			transactionType = "ALL";
		}

		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<SummaryReportObject> transactionList = new ArrayList<SummaryReportObject>();
		String merchPayId = "";
		String subMerchPayId = "";
		
		try {
			
			if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
			
				merchPayId = sessionUser.getSuperMerchantId();
				subMerchPayId = sessionUser.getPayId();
			
			} else /*if(!merchantPayId.equalsIgnoreCase("All"))*/ {
				
				//User merchant = userDao.findPayIdByEmail(merchantPayId);
				if (StringUtils.isNotBlank(merchantEmailId) && !merchantEmailId.equalsIgnoreCase("All")) {
					merchPayId = userDao.getPayIdByEmailId(merchantEmailId);
				} else {
					merchPayId = merchantEmailId;
				}
				
				if (StringUtils.isNotBlank(subMerchantEmailId) && !subMerchantEmailId.equalsIgnoreCase("All")) {
					subMerchPayId = userDao.getPayIdByEmailId(subMerchantEmailId);
				} else {
					subMerchPayId = subMerchantEmailId;
				}
			} /*else {
				merchPayId = merchantPayId;
			}*/
			
			transactionList = summaryReportQuery.summaryReportDownload(dateFrom, dateTo, merchPayId, subMerchPayId, paymentMethods,
					acquirer, currency, sessionUser, getPaymentsRegion(), getCardHolderType(), "", mopType,
					transactionType, partSettleflag, transactionFlag);
		} catch (Exception e) {
			logger.error("Exception", e);
		}

		logger.info("List generated successfully for Download summary Report");
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		// Create a blank sheet
		Sheet sheet = wb.createSheet("Summary Report");

		row = sheet.createRow(0);

		if(StringUtils.isNotBlank(subMerchPayId)) {
			
			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Txn Id");
			row.createCell(2).setCellValue("Pg Ref Num");
			row.createCell(3).setCellValue("Payment Method");
			row.createCell(4).setCellValue("Mop Type");
			row.createCell(5).setCellValue("Order Id");
			row.createCell(6).setCellValue("Merchant");
			row.createCell(7).setCellValue("Sub Merchant");
			row.createCell(8).setCellValue("Currency");
			row.createCell(9).setCellValue("Transaction Type");
			row.createCell(10).setCellValue("Capture Date");
			row.createCell(11).setCellValue("Settlement Date");
			row.createCell(12).setCellValue("Transaction Region");
			row.createCell(13).setCellValue("Card Holder Type");
			row.createCell(14).setCellValue("Acquirer Mode");
			row.createCell(15).setCellValue("Acquirer");
			row.createCell(16).setCellValue("Total Amount");
			row.createCell(17).setCellValue("TDR/SC (Merchant)");
			row.createCell(18).setCellValue("TDR/SC (Acquirer)");
			row.createCell(19).setCellValue("TDR/SC (Payment Gateway)");
			row.createCell(20).setCellValue("TDR/SC (Reseller)");
			row.createCell(21).setCellValue("GST (Merchant)");
			row.createCell(22).setCellValue("GST(Acquirer)");
			row.createCell(23).setCellValue("GST(Payment Gateway)");
			row.createCell(24).setCellValue("GST(Reseller)");
			row.createCell(25).setCellValue("Merchant Amount");
			row.createCell(26).setCellValue("SUF Charge");
			row.createCell(27).setCellValue("ACQ ID");
			row.createCell(28).setCellValue("RRN");
			row.createCell(29).setCellValue("Transaction Flag");
//			row.createCell(30).setCellValue("Delta Refund flag");
			row.createCell(30).setCellValue("Part Settled Flag");
			row.createCell(31).setCellValue("Refund Order ID");
			row.createCell(32).setCellValue("Transaction Flag");
			
			for (SummaryReportObject transactionSearch : transactionList) {
				row = sheet.createRow(rownum++);
				transactionSearch.setSrNo(String.valueOf(rownum - 1));
				Object[] objArr = transactionSearch.myCsvMethodDownloadSummaryReportForSuperMerchant();

				int cellnum = 0;
				for (Object obj : objArr) {
					// this line creates a cell in the next column of that row
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Integer)
						cell.setCellValue((Integer) obj);

				}
			}
		} else {
		
		row.createCell(0).setCellValue("Sr No");
		row.createCell(1).setCellValue("Txn Id");
		row.createCell(2).setCellValue("Pg Ref Num");
		row.createCell(3).setCellValue("Payment Method");
		row.createCell(4).setCellValue("Mop Type");
		row.createCell(5).setCellValue("Order Id");
		row.createCell(6).setCellValue("Merchant");
		row.createCell(7).setCellValue("Currency");
		row.createCell(8).setCellValue("Transaction Type");
		row.createCell(9).setCellValue("Capture Date");
		row.createCell(10).setCellValue("Settlement Date");
		row.createCell(11).setCellValue("Transaction Region");
		row.createCell(12).setCellValue("Card Holder Type");
		row.createCell(13).setCellValue("Acquirer Mode");
		row.createCell(14).setCellValue("Acquirer");
		row.createCell(15).setCellValue("Total Amount");
		row.createCell(16).setCellValue("TDR/SC (Merchant)");
		row.createCell(17).setCellValue("TDR/SC (Acquirer)");
		row.createCell(18).setCellValue("TDR/SC (Payment Gateway)");
		row.createCell(19).setCellValue("TDR/SC (Reseller)");
		row.createCell(20).setCellValue("GST (Merchant)");
		row.createCell(21).setCellValue("GST(Acquirer)");
		row.createCell(22).setCellValue("GST(Payment Gateway)");
		row.createCell(23).setCellValue("GST(Reseller)");
		row.createCell(24).setCellValue("Merchant Amount");
		row.createCell(25).setCellValue("SUF Charge");
		row.createCell(26).setCellValue("ACQ ID");
		row.createCell(27).setCellValue("RRN");
		row.createCell(28).setCellValue("Transaction Flag");
//		row.createCell(29).setCellValue("Delta Refund flag");
		row.createCell(29).setCellValue("Part Settled Flag");
		row.createCell(30).setCellValue("Refund Order ID");
		row.createCell(31).setCellValue("Transaction Flag");
		for (SummaryReportObject transactionSearch : transactionList) {
			row = sheet.createRow(rownum++);
			transactionSearch.setSrNo(String.valueOf(rownum - 1));
			Object[] objArr = transactionSearch.myCsvMethodDownloadSummaryReport();

			int cellnum = 0;
			for (Object obj : objArr) {
				// this line creates a cell in the next column of that row
				Cell cell = row.createCell(cellnum++);
				if (obj instanceof String)
					cell.setCellValue((String) obj);
				else if (obj instanceof Integer)
					cell.setCellValue((Integer) obj);

			}
		}
		
		}
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String FILE_EXTENSION = ".xlsx";
			String dateFolder = dateFormat.format(new Date()).split(" ")[0];
			
			if (isGenerateReport()) {
				
				
				
				String fileLocation = "";
				if (sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)
						|| sessionUser.getUserType().equals(UserType.RESELLER)) {
					if (sessionUser.getUserType().equals(UserType.ADMIN)) {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "AdminCreated/" + dateFolder + "/"
								+ sessionUser.getPayId() + "/";
					} else if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/SubAdminCreated/" + dateFolder + "/"
								+ sessionUser.getPayId() + "/";
					} else {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/ResellerCreated/" + dateFolder + "/"
								+ sessionUser.getPayId() + "/";
					}
					try {
						Files.createDirectories(Paths.get(fileLocation));
					} catch (IOException e1) {
						logger.error("Error in creating Directory ", e1);
					}
					if (StringUtils.isNotBlank(merchPayId) && !merchPayId.equalsIgnoreCase("ALL")) {
						if (StringUtils.isNotBlank(subMerchPayId) && !subMerchPayId.equalsIgnoreCase("ALL")) {
							if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
								
								filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchPayId + "-" + subMerchPayId + "_" + DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
								
							} else {
								filename = getReportType() + "_Report_" + merchPayId + "-" + subMerchPayId + "_"
										+ DateCreater.changeDateString(dateFrom) + "_"
										+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							}
						} else {
							if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
								filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchPayId + "-"
										+ DateCreater.changeDateString(dateFrom) + "_"
										+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							} else {
								filename = getReportType() + "_Report_" + merchPayId + "_"
										+ DateCreater.changeDateString(dateFrom) + "_"
										+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							}
						}
					} else {
						filename = getReportType() + "_Report_" + DateCreater.changeDateString(dateFrom) + "_"
								+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
					}
				} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
					if (sessionUser.isSuperMerchant()) {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/SuperMerchantCreated/" + dateFolder + "/"
								+ sessionUser.getPayId() + "/";
						try {
							Files.createDirectories(Paths.get(fileLocation));
						} catch (IOException e1) {
							logger.error("Error in creating Directory ", e1);
						}
						if (StringUtils.isNotBlank(subMerchPayId) && !subMerchPayId.equalsIgnoreCase("ALL")) {
							if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
								filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchPayId + "-"
										+ subMerchPayId + "_" + DateCreater.changeDateString(dateFrom) + "_"
										+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							} else {
								filename = getReportType() + "_Report_" + merchPayId + "-" + subMerchPayId + "_"
										+ DateCreater.changeDateString(dateFrom) + "_"
										+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							}

						} else {
							if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
								filename = getReportType() + "_Report_" + subuser.getPayId() + "_" + merchPayId + "_"
										+ DateCreater.changeDateString(dateFrom) + "_"
										+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							} else {
								filename = getReportType() + "_Report_" + merchPayId + "_"
										+ DateCreater.changeDateString(dateFrom) + "_"
										+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
							}
						}
					} else if (!sessionUser.isSuperMerchant()
							&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/SubMerchantCreated/" + dateFolder + "/"
								+ sessionUser.getPayId() + "/";
						try {
							Files.createDirectories(Paths.get(fileLocation));
						} catch (IOException e1) {
							logger.error("Error in creating Directory ", e1);
						}
						if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
							filename = getReportType() + "_Report_" + subuser.getPayId() + "_" + subMerchPayId + "_"
									+ DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo)
									+ FILE_EXTENSION;
						} else {
							filename = getReportType() + "_Report_" + subMerchPayId + "_"
									+ DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo)
									+ FILE_EXTENSION;
						}
					} else {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/MerchantCreated/" + dateFolder + "/"
								+ sessionUser.getPayId() + "/";
						try {
							Files.createDirectories(Paths.get(fileLocation));
						} catch (IOException e1) {
							logger.error("Error in creating Directory ", e1);
						}
						if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
							filename = getReportType() + "_Report_" + subuser.getPayId() + "_" + merchPayId + "_"
									+ DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo)
									+ FILE_EXTENSION;
						} else {
							filename = getReportType() + "_Report_" + merchPayId + "_"
									+ DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo)
									+ FILE_EXTENSION;
						}

					}
				} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
					fileLocation = PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
							+ "/SubUserCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/";
					try {
						Files.createDirectories(Paths.get(fileLocation));
					} catch (IOException e1) {
						logger.error("Error in creating Directory ", e1);
					}
					filename = getReportType() + "_Report_" + sessionUser.getPayId() + "_"
							+ DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo)
							+ FILE_EXTENSION;
				}
				
				File file = new File(fileLocation, filename);
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				logger.info(filename + " File generated successfully");
				reportGenerateService.insertFileStatusInDB(getReportType(), filename, fileLocation, merchPayId, subMerchPayId, "",
						dateFrom, dateTo, sessionUser.getPayId());
			} else {

				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				filename = "Summary_Report" + df.format(new Date()) + FILE_EXTENSION;
				File file = new File(filename);

				// this Writes the workbook
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				fileInputStream = new FileInputStream(file);
				addActionMessage(filename + " written successfully on disk.");
			}
		} catch (Exception exception) {
			setGenerateReport(false);
			logger.error("Exception", exception);
		}

		logger.info("File generated successfully for Download summary Report");
		return SUCCESS;

	}

	public String generateReportFile() {

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		setGenerateReport(true);
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					setReportType("SummaryReport");
					execute();
				} catch (Exception e) {
					setGenerateReport(false);
					logger.error("Exception while generating SearchPayment Report ", e);
				}
			}
		};

		propertiesManager.executorImpl(runnable);

		return SUCCESS;
	
	}
	
	public List<SummaryReportObject> findDetails(List<SummaryReportObject> transactionList) {

		logger.info("Inside search summary report Action , findDetails , ");
		List<SummaryReportObject> transactionList1 = new ArrayList<SummaryReportObject>();
		Map<String, List<Surcharge>> surchargeMap = new HashMap<String, List<Surcharge>>();

		BigDecimal merchantGstAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
		BigDecimal pgGstAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
		BigDecimal paymentGatewayGstAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
		BigDecimal acquirerGstAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
		Map<String, User> userMap = new HashMap<String, User>();
		TdrPojo tdrPojo = new TdrPojo();
		BigDecimal st = null;
		String bussinessType = null;
		String bussinessName = "";
		for (SummaryReportObject transactionSearch : transactionList) {
			String payId = transactionSearch.getPayId();
			if (!StringUtils.isBlank(payId)) {
				User user = new User();

				if (userMap.get(payId) != null) {
					user = userMap.get(payId);
				} else {
					user = userDao.findPayId(payId);
					userMap.put(payId, user);
				}
				String amount = transactionSearch.getAmount();
				bussinessType = user.getIndustryCategory();
				bussinessName = user.getBusinessName();
				st = new BigDecimal(PropertiesManager.propertiesMap.get("SERVICE_TAX"));
				st = st.setScale(2, RoundingMode.HALF_DOWN);
				if (!StringUtils.isBlank(transactionSearch.getSurchargeFlag())) {
					if (transactionSearch.getSurchargeFlag().equals("Y")) {
						String txnAmount = transactionSearch.getAmount();
						String surchargeAmount = transactionSearch.getTotalAmount();
						BigDecimal nettxnAmount = new BigDecimal(txnAmount);
						transactionSearch.setTotalAmount(surchargeAmount);

						PaymentType paymentType = PaymentType
								.getInstanceUsingCode(transactionSearch.getPaymentMethods());

						if (paymentType == null) {
							logger.info("Payment Type is null for Pg Ref Num " + transactionSearch.getPgRefNum());
							continue;
						}

						AcquirerType acquirerType = AcquirerType
								.getInstancefromCode(transactionSearch.getAcquirerType());

						if (acquirerType == null) {
							logger.info("acquirerType is null for Pg Ref Num " + transactionSearch.getPgRefNum());
							continue;
						}

						MopType mopType = MopType.getInstanceIgnoreCase(transactionSearch.getMopType());

						if (mopType == null) {
							logger.info("mopType is null for Pg Ref Num " + transactionSearch.getPgRefNum());
							continue;
						}

						if (paymentsRegion == null) {
							paymentsRegion = AccountCurrencyRegion.DOMESTIC.toString();
						}

						String paymentsRegion = transactionSearch.getTransactionRegion();

						Date surchargeStartDate = null;
						Date surchargeEndDate = null;
						Date settlementDate = null;
						Surcharge surcharge = new Surcharge();

						try {
							for (Surcharge surchargeData : surchargeList) {

								if (AcquirerType.getInstancefromName(surchargeData.getAcquirerName()).toString()
										.equalsIgnoreCase(transactionSearch.getAcquirerType())
										&& surchargeData.getPaymentType().getCode()
												.equalsIgnoreCase(transactionSearch.getPaymentMethods())
										&& surchargeData.getMopType().getName()
												.equalsIgnoreCase(transactionSearch.getMopType())
										&& surchargeData.getPaymentsRegion().name()
												.equalsIgnoreCase(transactionSearch.getTransactionRegion())
										&& surchargeData.getPayId().equalsIgnoreCase(transactionSearch.getPayId())) {

									surchargeStartDate = format.parse(surchargeData.getCreatedDate().toString());
									surchargeEndDate = format.parse(surchargeData.getUpdatedDate().toString());
									if (surchargeStartDate.compareTo(surchargeEndDate) == 0) {
										surchargeEndDate = new Date();
									}

									settlementDate = format.parse(transactionSearch.getDateFrom());

									if (settlementDate.compareTo(surchargeStartDate) >= 0
											&& settlementDate.compareTo(surchargeEndDate) <= 0) {
										surcharge = surchargeData;
										break;
									} else {
										continue;
									}
								}
							}
						} catch (Exception e) {
							logger.error("Exception " + e);
						}

						if (surcharge.getBankSurchargeAmountCustomer() == null
								|| surcharge.getBankSurchargePercentageCustomer() == null
								|| surcharge.getBankSurchargeAmountCommercial() == null
								|| surcharge.getBankSurchargePercentageCommercial() == null) {
							logger.info("Surcharge is null for payId = " + transactionSearch.getPayId() + " acquirer = "
									+ transactionSearch.getAcquirerType() + " mop = " + transactionSearch.getMopType()
									+ "  paymentType = " + transactionSearch.getPaymentMethods() + "  paymentRegion = "
									+ transactionSearch.getTransactionRegion());
							continue;
						}

						BigDecimal bankSurchargeFC;
						BigDecimal bankSurchargePercent;

						if (transactionSearch.getCardHolderType() == null
								|| transactionSearch.getCardHolderType().isEmpty()) {

							bankSurchargeFC = surcharge.getBankSurchargeAmountCustomer();
							bankSurchargePercent = surcharge.getBankSurchargePercentageCustomer();
						}

						else if (transactionSearch.getCardHolderType()
								.equalsIgnoreCase(CardHolderType.CONSUMER.toString())) {

							bankSurchargeFC = surcharge.getBankSurchargeAmountCustomer();
							bankSurchargePercent = surcharge.getBankSurchargePercentageCustomer();
						} else {

							bankSurchargeFC = surcharge.getBankSurchargeAmountCommercial();
							bankSurchargePercent = surcharge.getBankSurchargePercentageCommercial();
						}

						BigDecimal netsurchargeAmount = new BigDecimal(surchargeAmount);
						BigDecimal netcalculatedSurcharge = netsurchargeAmount.subtract(nettxnAmount);
						netcalculatedSurcharge = netcalculatedSurcharge.setScale(2, RoundingMode.HALF_DOWN);

						BigDecimal gstCalculate = netcalculatedSurcharge.multiply(st).divide(((ONE_HUNDRED).add(st)), 2,
								RoundingMode.HALF_DOWN);

						BigDecimal bankSurchargeAmount;
						BigDecimal pgSurchargeAmount;
						BigDecimal paymentGatewaySurchargeAmount;
						BigDecimal acquirerSurchargeAmount;

						if (netcalculatedSurcharge.equals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN))) {
							bankSurchargeAmount = BigDecimal.ZERO;
							pgSurchargeAmount = BigDecimal.ZERO;
							paymentGatewaySurchargeAmount = BigDecimal.ZERO;
							acquirerSurchargeAmount = BigDecimal.ZERO;
						}

						else {

							acquirerSurchargeAmount = nettxnAmount.multiply(bankSurchargePercent)
									.divide(((ONE_HUNDRED)), 2, RoundingMode.HALF_DOWN);
							acquirerSurchargeAmount = acquirerSurchargeAmount.add(bankSurchargeFC).setScale(2,
									RoundingMode.HALF_DOWN);
							;

							bankSurchargeAmount = netcalculatedSurcharge.subtract(acquirerSurchargeAmount);
							bankSurchargeAmount = bankSurchargeAmount.subtract(gstCalculate);
							bankSurchargeAmount = bankSurchargeAmount.setScale(2, RoundingMode.HALF_DOWN);
							pgSurchargeAmount = bankSurchargeAmount.divide(new BigDecimal(2),
									RoundingMode.HALF_DOWN);
							paymentGatewaySurchargeAmount = bankSurchargeAmount.divide(new BigDecimal(2),
									RoundingMode.HALF_DOWN);

						}

						acquirerGstAmount = acquirerSurchargeAmount.multiply(st).divide(((ONE_HUNDRED)), 2,
								RoundingMode.HALF_DOWN);

						merchantGstAmount = bankSurchargeAmount.multiply(st).divide(((ONE_HUNDRED)), 2,
								RoundingMode.HALF_DOWN);
						pgGstAmount = merchantGstAmount.divide(new BigDecimal(2), RoundingMode.HALF_DOWN);
						paymentGatewayGstAmount = merchantGstAmount.divide(new BigDecimal(2), RoundingMode.HALF_DOWN);

						BigDecimal totalSurcharge = netcalculatedSurcharge.subtract(gstCalculate);
						BigDecimal totalAmtPaytoMerchant = netsurchargeAmount
								.subtract(gstCalculate.add(totalSurcharge));

						if (transactionSearch.getTxnType().equalsIgnoreCase(TransactionType.REFUNDRECO.getName())) {
							BigDecimal minusValue = BigDecimal.valueOf(-1);

							netsurchargeAmount = netsurchargeAmount.multiply(minusValue).setScale(2,
									RoundingMode.HALF_DOWN);

							surchargeAmount = String.valueOf(netsurchargeAmount);

							acquirerSurchargeAmount = acquirerSurchargeAmount.multiply(minusValue);

							pgSurchargeAmount = pgSurchargeAmount.multiply(minusValue);
							paymentGatewaySurchargeAmount = paymentGatewaySurchargeAmount.multiply(minusValue);

							pgGstAmount = pgGstAmount.multiply(minusValue);
							paymentGatewayGstAmount = paymentGatewayGstAmount.multiply(minusValue);

							acquirerGstAmount = acquirerGstAmount.multiply(minusValue);
							totalAmtPaytoMerchant = totalAmtPaytoMerchant.multiply(minusValue);
						}

						String gstCalculateString = String.valueOf(gstCalculate);
						String totalSurchargeString = String.valueOf(totalSurcharge);
						String totalAmtPaytoMerchantString = String.valueOf(totalAmtPaytoMerchant);
						tdrPojo.setTotalAmtPaytoMerchant(totalAmtPaytoMerchantString);
						tdrPojo.setTotalGstOnMerchant(gstCalculateString);
						tdrPojo.setNetMerchantPayableAmount(totalAmtPaytoMerchantString);
						tdrPojo.setMerchantTdrCalculate(totalSurchargeString);
						tdrPojo.setTotalAmount(surchargeAmount);
						tdrPojo.setAcquirerSurchargeAmount(String.valueOf(acquirerSurchargeAmount));
						tdrPojo.setBankSurchargeAmount(String.valueOf(bankSurchargeAmount));
						tdrPojo.setPgSurchargeAmount(String.valueOf(pgSurchargeAmount));
						tdrPojo.setPaymentGatewaySurchargeAmount(String.valueOf(paymentGatewaySurchargeAmount));
					}
				} else {

					/*
					 * ChargingDetails chargingDetails =
					 * cdf.getChargingDetailForReport(transactionSearch.getDateFrom(), payId,
					 * transactionSearch.getAcquirerType(), transactionSearch.getPaymentMethods(),
					 * transactionSearch.getMopType(), transactionSearch.getTxnType(),
					 * transactionSearch.getCurrency()); tdrPojo =
					 * chargiesCalculation(chargingDetails.getBankTDR(),
					 * chargingDetails.getPgFixCharge(), chargingDetails.getPgTDR(),
					 * chargingDetails.getBankFixCharge(), chargingDetails.getMerchantFixCharge(),
					 * chargingDetails.getMerchantTDR(), st, amount);
					 */
				}

				if (!tdrPojo.equals(null)) {
					transactionSearch.setAmount(tdrPojo.getTotalAmount());
					transactionSearch.setMerchants(bussinessName);
					transactionSearch.setTdrScAcquirer(tdrPojo.getAcquirerSurchargeAmount());
					transactionSearch.setTdrScPg(tdrPojo.getPgSurchargeAmount());
					transactionSearch.setTdrScPaymentGateway(tdrPojo.getPaymentGatewaySurchargeAmount());
					transactionSearch.setGstScAcquirer(String.valueOf(pgGstAmount));
					transactionSearch.setGstScPg(String.valueOf(paymentGatewayGstAmount));
					transactionSearch.setGstScPaymentGateway(String.valueOf(acquirerGstAmount));
					transactionSearch.setTotalAmount(tdrPojo.getTotalAmount());
					transactionSearch
							.setNetMerchantPayableAmount(String.valueOf(tdrPojo.getNetMerchantPayableAmount()));
				}
				transactionList1.add(transactionSearch);
			}
		}
		return transactionList1;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
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

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getPaymentsRegion() {
		return paymentsRegion;
	}

	public void setPaymentsRegion(String paymentsRegion) {
		this.paymentsRegion = paymentsRegion;
	}

	public String getCardHolderType() {
		return cardHolderType;
	}

	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public String getPaymentMethods() {
		return paymentMethods;
	}

	public void setPaymentMethods(String paymentMethods) {
		this.paymentMethods = paymentMethods;
	}

	public String getMopType() {
		return mopType;
	}

	public void setMopType(String mopType) {
		this.mopType = mopType;
	}

	public String getPartSettleflag() {
		return partSettleflag;
	}

	public void setPartSettleflag(String partSettleflag) {
		this.partSettleflag = partSettleflag;
	}

	public String getMerchantEmailId() {
		return merchantEmailId;
	}

	public void setMerchantEmailId(String merchantEmailId) {
		this.merchantEmailId = merchantEmailId;
	}

	public String getSubMerchantEmailId() {
		return subMerchantEmailId;
	}

	public void setSubMerchantEmailId(String subMerchantEmailId) {
		this.subMerchantEmailId = subMerchantEmailId;
	}

	public boolean isGenerateReport() {
		return generateReport;
	}

	public void setGenerateReport(boolean generateReport) {
		this.generateReport = generateReport;
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public String getSubUserPayId() {
		return subUserPayId;
	}

	public void setSubUserPayId(String subUserPayId) {
		this.subUserPayId = subUserPayId;
	}

	public User getSubuser() {
		return subuser;
	}

	public void setSubuser(User subuser) {
		this.subuser = subuser;
	}

	public String getTransactionFlag() {
		return transactionFlag;
	}

	public void setTransactionFlag(String transactionFlag) {
		this.transactionFlag = transactionFlag;
	}
	
}