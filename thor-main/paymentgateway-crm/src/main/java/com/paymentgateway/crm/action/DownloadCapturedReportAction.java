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
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.actionBeans.DownloadReportGenerateService;
import com.paymentgateway.crm.mongoReports.TxnReports;

public class DownloadCapturedReportAction extends AbstractSecureAction {
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private TxnReports txnReports;
	
	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private DownloadReportGenerateService reportGenerateService;
	
	private static final long serialVersionUID = 2871252777725723745L;

	private static Logger logger = LoggerFactory.getLogger(DownloadCapturedReportAction.class.getName());
	
	private String reportMerchant;
	private String subMerchant;
	private String reportPaymentMethod;
	private String reportPgRefNum;
	private String reportOrderId;
	private String reportCurrency;
	private String reportPostSettleFlag;
	private String reportDateFrom;
	private String reportDateTo;
	private InputStream fileInputStream;
	private String filename;
	private boolean generateReport;
	private String reportType;
	public String subUserPayId;
	private User subuser = new User();
	private User sessionUser = new User();
	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
	@SuppressWarnings("static-access")
	public String execute() {
	
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<TransactionSearch> capturedDataList = new ArrayList<TransactionSearch>();
		setReportDateFrom(DateCreater.toDateTimeformatCreater(reportDateFrom));
		setReportDateTo(DateCreater.formDateTimeformatCreater(reportDateTo));
		
		String merchantPayId = "";
		String subMerchantPayId = "";
		if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
			merchantPayId = sessionUser.getSuperMerchantId();
			subMerchantPayId = sessionUser.getPayId();
		} else {
			merchantPayId =  reportMerchant;
			
			if (StringUtils.isNotBlank(subMerchant) && !subMerchant.equalsIgnoreCase("All")) {
				subMerchantPayId = userDao.getPayIdByEmailId(subMerchant);
			}
			if(StringUtils.isNotBlank(subMerchant) && subMerchant.equalsIgnoreCase("All")) {
				subMerchantPayId  = subMerchant;
			}
		}
		capturedDataList = txnReports.capturedDataForDownload( merchantPayId, subMerchantPayId, reportPaymentMethod, reportPgRefNum, reportOrderId, reportCurrency, reportPostSettleFlag, reportDateFrom, reportDateTo ,sessionUser);
		BigDecimal st = null;

		logger.info("List generated successfully for DownloadPaymentsReportAction");
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		// Create a blank sheet
		Sheet sheet =  wb.createSheet("Captured Report");
		row = sheet.createRow(0);
        
            if(StringUtils.isBlank(subMerchantPayId)) {
			
            	row.createCell(0).setCellValue("Sr No");
    			row.createCell(1).setCellValue("Txn Id");
    			row.createCell(2).setCellValue("Pg Ref Num");
    			row.createCell(3).setCellValue("Merchant");
    			row.createCell(4).setCellValue("Captured Date");
    			row.createCell(5).setCellValue("Settled Date");
    			row.createCell(6).setCellValue("Order Id");
    			row.createCell(7).setCellValue("RRN");
    			row.createCell(8).setCellValue("Payment Method");
    			row.createCell(9).setCellValue("MopType");
    			row.createCell(10).setCellValue("Card Mask");
    			row.createCell(11).setCellValue("Cust Name");
    			row.createCell(12).setCellValue("Cardholder Type");
    			row.createCell(13).setCellValue("Txn Type");
    			row.createCell(14).setCellValue("Status");
    			row.createCell(15).setCellValue("Transaction Region");
    			row.createCell(16).setCellValue("Base Amount");
    			row.createCell(17).setCellValue("Total Amount");
    			row.createCell(18).setCellValue("TDR / Surcharge");
    			row.createCell(19).setCellValue("GST");
    			row.createCell(20).setCellValue("Merchant Amount");
    			row.createCell(21).setCellValue("Post Settled Flag");
    			row.createCell(22).setCellValue("Part Settled Flag");
            	
			
			for (TransactionSearch transactionList: capturedDataList) {
				row = sheet.createRow(rownum++);
				transactionList.setSrNo(rownum - 1);
				Object[] objArr = transactionList.myCsvMethodForCapturedMerchant();

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
			row.createCell(5).setCellValue("Captured Date");
			row.createCell(6).setCellValue("Settled Date");
			row.createCell(7).setCellValue("Order Id");
			row.createCell(8).setCellValue("RRN");
			row.createCell(9).setCellValue("Payment Method");
			row.createCell(10).setCellValue("MopType");
			row.createCell(11).setCellValue("Card Mask");
			row.createCell(12).setCellValue("Cust Name");
			row.createCell(13).setCellValue("Cardholder Type");
			row.createCell(14).setCellValue("Txn Type");
			row.createCell(15).setCellValue("Status");
			row.createCell(16).setCellValue("Transaction Region");
			row.createCell(17).setCellValue("Base Amount");
			row.createCell(18).setCellValue("Total Amount");
			row.createCell(19).setCellValue("TDR / Surcharge");
			row.createCell(20).setCellValue("GST");
			row.createCell(21).setCellValue("Merchant Amount");
			row.createCell(22).setCellValue("Post Settled Flag");
			row.createCell(23).setCellValue("Part Settled Flag");
        	
		
		for (TransactionSearch transactionList: capturedDataList) {
			row = sheet.createRow(rownum++);
			transactionList.setSrNo(rownum - 1);
			Object[] objArr = transactionList.myCsvMethodForCapturedSubMerchant();

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
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "AdminCreated/" + dateFolder
								+ "/" + sessionUser.getPayId() + "/";
					} else if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/SubAdminCreated/" + dateFolder
								+ "/" + sessionUser.getPayId() + "/";
					} else {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/ResellerCreated/" + dateFolder
								+ "/" + sessionUser.getPayId() + "/";
					}
					try {
						Files.createDirectories(Paths.get(fileLocation));
					} catch (IOException e1) {
						logger.error("Error in creating Directory ", e1);
					}
					if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
						if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
							if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
								filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchantPayId + "-"
										+ subMerchantPayId + "_" + DateCreater.changeDateString(reportDateFrom) + "_"
										+ DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;

							} else {
								filename = getReportType() + "_Report_" + merchantPayId + "-" + subMerchantPayId + "_"
										+ DateCreater.changeDateString(reportDateFrom) + "_"
										+ DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
							}
						} else {
							if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {

								filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchantPayId + "-"
										+ DateCreater.changeDateString(reportDateFrom) + "_"
										+ DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
							} else {
								filename = getReportType() + "_Report_" + merchantPayId + "-"
										+ DateCreater.changeDateString(reportDateFrom) + "_"
										+ DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
							}
						}
					} else {
						filename = getReportType() + "_Report_" + DateCreater.changeDateString(reportDateFrom) + "_"
								+ DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
					}
				} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
					if (sessionUser.isSuperMerchant()) {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/SuperMerchantCreated/"
								+ dateFolder + "/" + sessionUser.getPayId() + "/";
						try {
							Files.createDirectories(Paths.get(fileLocation));
						} catch (IOException e1) {
							logger.error("Error in creating Directory ", e1);
						}
						if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
							if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
								filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + merchantPayId + "-"
										+ subMerchantPayId + "_" + DateCreater.changeDateString(reportDateFrom) + "_"
										+ DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
							} else {
								filename = getReportType() + "_Report_" + merchantPayId + "-" + subMerchantPayId + "_"
										+ DateCreater.changeDateString(reportDateFrom) + "_"
										+ DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
							}

						} else {
							if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
								filename = getReportType() + "_Report_" + subuser.getPayId() + "_" + merchantPayId + "-"
										+ DateCreater.changeDateString(reportDateFrom) + "_"
										+ DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
							} else {
								filename = getReportType() + "_Report_" + merchantPayId + "-"
										+ DateCreater.changeDateString(reportDateFrom) + "_"
										+ DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
							}
						}
					} else if (!sessionUser.isSuperMerchant()
							&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/SubMerchantCreated/"
								+ dateFolder + "/" + sessionUser.getPayId() + "/";
						try {
							Files.createDirectories(Paths.get(fileLocation));
						} catch (IOException e1) {
							logger.error("Error in creating Directory ", e1);
						}
						if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
							filename = getReportType() + "_Report_" + subuser.getPayId() + "-" + subMerchantPayId + "_"
									+ DateCreater.changeDateString(reportDateFrom) + "_"
									+ DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
						} else {
							filename = getReportType() + "_Report_" + subMerchantPayId + "_"
									+ DateCreater.changeDateString(reportDateFrom) + "_"
									+ DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
						}
					} else {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/MerchantCreated/" + dateFolder
								+ "/" + sessionUser.getPayId() + "/";
						try {
							Files.createDirectories(Paths.get(fileLocation));
						} catch (IOException e1) {
							logger.error("Error in creating Directory ", e1);
						}
						if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
							filename = getReportType() + "_Report_" + subuser.getPayId() + "_" + merchantPayId + "-"
									+ DateCreater.changeDateString(reportDateFrom) + "_"
									+ DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
						} else {
							filename = getReportType() + "_Report_" + merchantPayId + "-"
									+ DateCreater.changeDateString(reportDateFrom) + "_"
									+ DateCreater.changeDateString(reportDateTo) + FILE_EXTENSION;
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
							+ DateCreater.changeDateString(reportDateFrom) + "_" + DateCreater.changeDateString(reportDateTo)
							+ FILE_EXTENSION;
				}

				File file = new File(fileLocation, filename);
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				logger.info(filename + " File generated successfully");
				reportGenerateService.insertFileStatusInDB(getReportType(), filename, fileLocation, merchantPayId,
						subMerchantPayId, "", reportDateFrom, reportDateTo, sessionUser.getPayId());
			} else {
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				filename = "Custom_Capture_Report" + df.format(new Date()) + FILE_EXTENSION;
				File file = new File(filename);

				// this Writes the workbook
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				fileInputStream = new FileInputStream(file);
				addActionMessage(filename + " written successfully on disk.");
				logger.info("File generated successfully for DownloadCustomCaptureReportAction");
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
					setReportType("CustomCapture");
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

	public String getReportMerchant() {
		return reportMerchant;
	}
	public void setReportMerchant(String reportMerchant) {
		this.reportMerchant = reportMerchant;
	}
	public String getSubMerchant() {
		return subMerchant;
	}
	public void setSubMerchant(String subMerchant) {
		this.subMerchant = subMerchant;
	}
	public String getReportPaymentMethod() {
		return reportPaymentMethod;
	}
	public void setReportPaymentMethod(String reportPaymentMethod) {
		this.reportPaymentMethod = reportPaymentMethod;
	}
	public String getReportPgRefNum() {
		return reportPgRefNum;
	}
	public void setReportPgRefNum(String reportPgRefNum) {
		this.reportPgRefNum = reportPgRefNum;
	}
    public String getReportOrderId() {
		return reportOrderId;
	}
	public void setReportOrderId(String reportOrderId) {
		this.reportOrderId = reportOrderId;
	}
	public String getReportCurrency() {
		return reportCurrency;
	}
	public void setReportCurrency(String reportCurrency) {
		this.reportCurrency = reportCurrency;
	}
	public String getReportPostSettleFlag() {
		return reportPostSettleFlag;
	}
	public void setReportPostSettleFlag(String reportPostSettleFlag) {
		this.reportPostSettleFlag = reportPostSettleFlag;
	}
	public String getReportDateFrom() {
		return reportDateFrom;
	}
	public void setReportDateFrom(String reportDateFrom) {
		this.reportDateFrom = reportDateFrom;
	}
	public String getReportDateTo() {
		return reportDateTo;
	}
	public void setReportDateTo(String reportDateTo) {
		this.reportDateTo = reportDateTo;
	}
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
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
