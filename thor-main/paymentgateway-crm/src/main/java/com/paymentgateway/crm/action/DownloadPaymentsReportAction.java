package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.TransactionSearchDownloadObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.actionBeans.DownloadReportGenerateService;
import com.paymentgateway.crm.actionBeans.SessionUserIdentifier;
import com.paymentgateway.crm.mongoReports.TxnReports;

public class DownloadPaymentsReportAction extends AbstractSecureAction {

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private DownloadReportGenerateService reportGenerateService;
	
	@Autowired
	private PropertiesManager propertiesManager;
	
	private static final long serialVersionUID = 2871252777725723745L;

	private static Logger logger = LoggerFactory.getLogger(DownloadPaymentsReportAction.class.getName());

	private List<Merchants> merchantList = new LinkedList<Merchants>();
	private Map<String, String> currencyMap = new HashMap<String, String>();
	private String currency;
	private String dateFrom;
	private String dateTo;
	private String merchantPayId;
	private String subMerchantPayId;

	private String paymentType;
	private String transactionType;
	private String status;
	private String acquirer;
	private InputStream fileInputStream;
	private String filename;
	private User sessionUser = new User();
	private String paymentsRegion;
	private String cardHolderType;
	private Boolean searchFlag;
	private Set<String> orderIdSet = null;
	private boolean generateReport;
	private String reportType;
	public String subUserPayId;
	private User subuser = new User();
	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

	@Autowired
	private TxnReports txnReports;

	@Autowired
	private SessionUserIdentifier userIdentifier;

	@Autowired
	private CrmValidator validator;
	
	@SuppressWarnings("resource")
	public String execute() {

		if (StringUtils.isBlank(acquirer)) {
			acquirer = "ALL";

		}

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<TransactionSearchDownloadObject> transactionList = new ArrayList<TransactionSearchDownloadObject>();
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));

		String merchPayId = "";
		String subMerchPayId = "";
		if (sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
			merchPayId = sessionUser.getSuperMerchantId();
			subMerchPayId = sessionUser.getPayId();
		} else {
			merchPayId = merchantPayId;

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
				subMerchPayId = userDao.getPayIdByEmailId(subMerchantPayId);
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && subMerchantPayId.equalsIgnoreCase("All")) {
				subMerchPayId = subMerchantPayId;
			}
		}

		if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
			
			String subUserId = "";
			if (!userDao.isSubUserPrevilageTypeAll(sessionUser)) {
				subUserId = sessionUser.getPayId();
			}
			
			User user = userDao.findPayId(sessionUser.getParentPayId());
			
			if(user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
				merchPayId = user.getSuperMerchantId();
				subMerchPayId = user.getPayId();
			}else {
				merchPayId = user.getPayId();
			}

			if (!StringUtils.isEmpty(sessionUser.getSubUserType())
					&& sessionUser.getSubUserType().equalsIgnoreCase("ePosType")) {
				
				orderIdSet = txnReports.findBySubuserId(subUserId, sessionUser.getParentPayId());
				sessionUser = user; 
				
			} else if (!StringUtils.isEmpty(sessionUser.getSubUserType())
					&& sessionUser.getSubUserType().equalsIgnoreCase("normalType")) {

				sessionUser = user;
			}
		}

		transactionList = txnReports.searchTransactionForDownload(merchPayId, subMerchPayId, paymentType, status,
				currency, transactionType, dateFrom, dateTo, sessionUser, paymentsRegion, acquirer, getSearchFlag(),
				orderIdSet);
		BigDecimal st = null;

		logger.info("List generated successfully for DownloadPaymentsReportAction");
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		// Create a blank sheet
		Sheet sheet = wb.createSheet("Payments Report");
		row = sheet.createRow(0);
		
		if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			int n =2;
			if (StringUtils.isBlank(subMerchPayId)) {

				row.createCell(0).setCellValue("Sr No");
				row.createCell(1).setCellValue("Txn Id");
				row.createCell(2).setCellValue("Pg Ref Num");
				row.createCell(3).setCellValue("Merchant");
				row.createCell(4).setCellValue("PayId");
				row.createCell(5).setCellValue("Acquirer Type");
				row.createCell(6).setCellValue("Date");
				row.createCell(7).setCellValue("Order Id");
				row.createCell(8).setCellValue("Payment Method");
				row.createCell(9).setCellValue("Mop Type");
				row.createCell(10).setCellValue("CardHolder Type");
				row.createCell(11).setCellValue("Mask");
				row.createCell(12).setCellValue("Txn Type");
				row.createCell(13).setCellValue("Transaction Mode");
				row.createCell(14).setCellValue("Status");
				row.createCell(15).setCellValue("Transaction Region");
				row.createCell(16).setCellValue("Base Amount");
				row.createCell(17).setCellValue("TDR / Surcharge (Merchant)");
				row.createCell(18).setCellValue("GST (Merchant)");
				// add by vishal 
				row.createCell(19).setCellValue("SUF CHARGES");
				row.createCell(20).setCellValue("SUF GST");
				row.createCell(19+n).setCellValue("TDR / Surcharge (Acquirer)");
				row.createCell(20+n).setCellValue("GST (Acquirer)");
				row.createCell(21+n).setCellValue("TDR / Surcharge (PG)");
				row.createCell(22+n).setCellValue("GST (PG)");
				row.createCell(23+n).setCellValue("TDR / Surcharge (Reseller)");
				row.createCell(24+n).setCellValue("GST (Reseller)");
				row.createCell(25+n).setCellValue("Total Amount");
	//			row.createCell(21).setCellValue("Delta Refund Flag");
				row.createCell(26+n).setCellValue("ACQ ID");
				row.createCell(27+n).setCellValue("RRN");
				row.createCell(28+n).setCellValue("Transaction Flag");
				row.createCell(29+n).setCellValue("Refund Order ID");
				row.createCell(30+n).setCellValue("PG Response Message");
				row.createCell(31+n).setCellValue("Acquirer Response Message");
				row.createCell(32+n).setCellValue("UDF11");
				row.createCell(33+n).setCellValue("UDF12");
				row.createCell(34+n).setCellValue("UDF13");
				row.createCell(35+n).setCellValue("UDF14");
				row.createCell(36+n).setCellValue("UDF15");
				row.createCell(37+n).setCellValue("UDF16");
				row.createCell(38+n).setCellValue("UDF17");
				row.createCell(39+n).setCellValue("UDF18");

				for (TransactionSearchDownloadObject transactionSearch : transactionList) {
					row = sheet.createRow(rownum++);
					transactionSearch.setSrNo(String.valueOf(rownum - 1));
					
					Object[] objArr = transactionSearch.myCsvMethodDownloadPaymentsReportForAdmin();

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
				row.createCell(3).setCellValue("Merchant");
				row.createCell(4).setCellValue("Sub Merchant");
				row.createCell(5).setCellValue("PayId");
				row.createCell(6).setCellValue("Acquirer Type");
				row.createCell(7).setCellValue("Date");
				row.createCell(8).setCellValue("Order Id");
				row.createCell(9).setCellValue("Payment Method");
				row.createCell(10).setCellValue("Mop Type");
				row.createCell(11).setCellValue("CardHolder Type");
				row.createCell(12).setCellValue("Mask");
				row.createCell(13).setCellValue("Txn Type");
				row.createCell(14).setCellValue("Transaction Mode");
				row.createCell(15).setCellValue("Status");
				row.createCell(16).setCellValue("Transaction Region");
				row.createCell(17).setCellValue("Base Amount");
				row.createCell(18).setCellValue("TDR / Surcharge (Merchant)");
				row.createCell(19).setCellValue("GST (Merchant)");
				// add by vishal 
				row.createCell(20).setCellValue("SUF CHARGES");
				row.createCell(21).setCellValue("SUF GST");
				
				row.createCell(20+n).setCellValue("TDR / Surcharge (Acquirer)");
				row.createCell(21+n).setCellValue("GST (Acquirer)");
				row.createCell(22+n).setCellValue("TDR / Surcharge (PG)");
				row.createCell(23+n).setCellValue("GST (PG)");
				row.createCell(24+n).setCellValue("TDR / Surcharge (Reseller)");
				row.createCell(25+n).setCellValue("GST (Reseller)");
				row.createCell(26+n).setCellValue("Total Amount");
				row.createCell(27+n).setCellValue("ACQ ID");
				row.createCell(28+n).setCellValue("RRN");
				row.createCell(29+n).setCellValue("Transaction Flag");
				row.createCell(30+n).setCellValue("Refund Order ID");
				row.createCell(31+n).setCellValue("PG Response Message");
				row.createCell(32+n).setCellValue("Acquirer Response Message");
				row.createCell(33+n).setCellValue("UDF11");
				row.createCell(34+n).setCellValue("UDF12");
				row.createCell(35+n).setCellValue("UDF13");
				row.createCell(36+n).setCellValue("UDF14");
				row.createCell(37+n).setCellValue("UDF15");
				row.createCell(38+n).setCellValue("UDF16");
				row.createCell(39+n).setCellValue("UDF17");
				row.createCell(40+n).setCellValue("UDF18");

				for (TransactionSearchDownloadObject transactionSearch : transactionList) {
					row = sheet.createRow(rownum++);
					transactionSearch.setSrNo(String.valueOf(rownum - 1));
					
					Object[] objArr = transactionSearch.myCsvMethodDownloadPaymentsReportForAdminAndSubMerchant();

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
		} else {

			if (StringUtils.isBlank(subMerchPayId)) {
				int n =2;
				row.createCell(0).setCellValue("Sr No");
				row.createCell(1).setCellValue("Txn Id");
				row.createCell(2).setCellValue("Pg Ref Num");
				row.createCell(3).setCellValue("Merchant");
				row.createCell(4).setCellValue("Date");
				row.createCell(5).setCellValue("Order Id");
				row.createCell(6).setCellValue("Payment Method");
				row.createCell(7).setCellValue("Mop Type");
				row.createCell(8).setCellValue("CardHolder Type");
				row.createCell(9).setCellValue("Mask");
				row.createCell(10).setCellValue("Txn Type");
				row.createCell(11).setCellValue("Transaction Mode");
				row.createCell(12).setCellValue("Status");
				row.createCell(13).setCellValue("Transaction Region");
				row.createCell(14).setCellValue("Base Amount");
				row.createCell(15).setCellValue("TDR / Surcharge");
				row.createCell(16).setCellValue("GST");
				// add by vishal 
				row.createCell(17).setCellValue("SUF CHARGES");
				row.createCell(18).setCellValue("SUF GST");
				
				
				row.createCell(17+n).setCellValue("Total Amount");
//				row.createCell(18).setCellValue("Delta Refund Flag");
				row.createCell(18+n).setCellValue("ACQ ID");
				row.createCell(19+n).setCellValue("RRN");
				row.createCell(20+n).setCellValue("Transaction Flag");
				row.createCell(21+n).setCellValue("Refund Order ID");
				row.createCell(22+n).setCellValue("PG Response Message");
				row.createCell(23+n).setCellValue("Acquirer Response Message");
				row.createCell(24+n).setCellValue("UDF11");
				row.createCell(25+n).setCellValue("UDF12");
				row.createCell(26+n).setCellValue("UDF13");
				row.createCell(27+n).setCellValue("UDF14");
				row.createCell(28+n).setCellValue("UDF15");
				row.createCell(29+n).setCellValue("UDF16");
				row.createCell(30+n).setCellValue("UDF17");
				row.createCell(31+n).setCellValue("UDF18");

				for (TransactionSearchDownloadObject transactionSearch : transactionList) {
					row = sheet.createRow(rownum++);
					transactionSearch.setSrNo(String.valueOf(rownum - 1));
					
					Object[] objArr = transactionSearch.myCsvMethodDownloadPaymentsReport(sessionUser);

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
				int n=2;
				row.createCell(0).setCellValue("Sr No");
				row.createCell(1).setCellValue("Txn Id");
				row.createCell(2).setCellValue("Pg Ref Num");
				row.createCell(3).setCellValue("Merchant");
				row.createCell(4).setCellValue("Sub Merchant");
				row.createCell(5).setCellValue("Date");
				row.createCell(6).setCellValue("Order Id");
				row.createCell(7).setCellValue("Payment Method");
				row.createCell(8).setCellValue("Mop Type");
				row.createCell(9).setCellValue("CardHolder Type");
				row.createCell(10).setCellValue("Mask");
				row.createCell(11).setCellValue("Txn Type");
				row.createCell(12).setCellValue("Transaction Mode");
				row.createCell(13).setCellValue("Status");
				row.createCell(14).setCellValue("Transaction Region");
				row.createCell(15).setCellValue("Base Amount");
				row.createCell(16).setCellValue("TDR / Surcharge");
				row.createCell(17).setCellValue("GST");
				
				//add by vishal
				row.createCell(18).setCellValue("SUF CHARGES");
				row.createCell(19).setCellValue("SUF GST");
				
				row.createCell(18+n).setCellValue("Total Amount");
//				row.createCell(18).setCellValue("Delta Refund Flag");
				row.createCell(19+n).setCellValue("ACQ ID");
				row.createCell(20+n).setCellValue("RRN");
				row.createCell(21+n).setCellValue("Transaction Flag");
				row.createCell(22+n).setCellValue("Refund Order ID");
				row.createCell(23+n).setCellValue("PG Response Message");
				row.createCell(24+n).setCellValue("Acquirer Response Message");
				row.createCell(25+n).setCellValue("UDF11");
				row.createCell(26+n).setCellValue("UDF12");
				row.createCell(27+n).setCellValue("UDF13");
				row.createCell(28+n).setCellValue("UDF14");
				row.createCell(29+n).setCellValue("UDF15");
				row.createCell(30+n).setCellValue("UDF16");
				row.createCell(31+n).setCellValue("UDF17");
				row.createCell(32+n).setCellValue("UDF18");

				for (TransactionSearchDownloadObject transactionSearch : transactionList) {
					row = sheet.createRow(rownum++);
					transactionSearch.setSrNo(String.valueOf(rownum - 1));
					
					Object[] objArr = transactionSearch.myCsvMethodDownloadPaymentsReportForSubMerchant(sessionUser);

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
		}
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String FILE_EXTENSION = ".xlsx";
			String dateFolder = dateFormat.format(new Date()).split(" ")[0];
			
			if(isGenerateReport()) {
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
					if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
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
				
				File file = new File(fileLocation,filename);
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				logger.info(filename + " File generated successfully");
				reportGenerateService.insertFileStatusInDB(getReportType(), filename, fileLocation, merchPayId, subMerchPayId, "",dateFrom, dateTo, sessionUser.getPayId());
			} else {
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				filename = "Payments_Report" + df.format(new Date()) + FILE_EXTENSION;
				File file = new File(filename);

				// this Writes the workbook
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				fileInputStream = new FileInputStream(file);
				addActionMessage(filename + " written successfully on disk.");
				logger.info("File generated successfully for DownloadPaymentsReportAction");
			}
		} catch (Exception exception) {
			setGenerateReport(false);
			logger.error("Exception", exception);
		}

		return SUCCESS;

	}
	
	public String generateReportFile() {

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		setGenerateReport(true);
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					setReportType("SearchTransaction");
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
	
	
	public void validate() {

		
		if (!(validator.validateBlankField(getCurrency()))) {
			if (!(validator.validateField(CrmFieldType.CURRENCY, getCurrency()))) {
				addFieldError(CrmFieldType.CURRENCY.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		if (!(validator.validateBlankField(getDateFrom()))) {
			if (!(validator.validateField(CrmFieldType.DATE_FROM, getDateFrom()))) {
				addFieldError(CrmFieldType.DATE_FROM.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		if (!validator.validateBlankField(getDateTo())) {
			if (DateCreater.formatStringToDate(DateCreater.formatFromDate(getDateFrom()))
					.compareTo(DateCreater.formatStringToDate(DateCreater.formatFromDate(getDateTo()))) > 0) {
				addFieldError(CrmFieldType.DATE_FROM.getName(), CrmFieldConstants.FROMTO_DATE_VALIDATION.getValue());
			} else if (DateCreater.diffDate(getDateFrom(), getDateTo()) > 31) {
				addFieldError(CrmFieldType.DATE_FROM.getName(), CrmFieldConstants.DATE_RANGE.getValue());
			}
		}

		
		
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

	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

	public Boolean getSearchFlag() {
		return searchFlag;
	}

	public void setSearchFlag(Boolean searchFlag) {
		this.searchFlag = searchFlag;
	}

	public Set<String> getOrderIdSet() {
		return orderIdSet;
	}

	public void setOrderIdSet(Set<String> orderIdSet) {
		this.orderIdSet = orderIdSet;
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

}
