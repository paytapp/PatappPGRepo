package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.ExceptionReport;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncoder;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.actionBeans.DownloadReportGenerateService;
import com.paymentgateway.crm.mongoReports.ExceptionReportData;

public class ExceptionReportController extends AbstractSecureAction {

	private static final long serialVersionUID = 8626282226799143239L;
	private static Logger logger = LoggerFactory.getLogger(ExceptionReportController.class.getName());

	private String status;
	private String dateFrom;
	private String dateTo;
	private String merchant;
	private String acquirer;
	private String settledFlag;
	private String fileName;
	private InputStream fileInputStream;

	private BigInteger recordsTotal;
	private BigInteger recordsFiltered;
	private int length;
	private int start;
	private boolean generateReport;
	private String reportType;

	private List<ExceptionReport> aaData;

	@Autowired
	private DataEncoder encoder;

	@Autowired
	private ExceptionReportData exceptionReportData;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private DownloadReportGenerateService reportGenerateService;

	public String execute() {

		int totalCount;
		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		try {
			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				totalCount = exceptionReportData.getDataCount(getMerchant(), getAcquirer(), getStatus(), getDateFrom(),
						getDateTo(), ErrorType.MERCHANT_EXCEPTION.getResponseMessage(), getSettledFlag());
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}
				aaData = encoder.encodeExceptionReportObj(exceptionReportData.getData(getMerchant(), getAcquirer(),
						getStatus(), getDateFrom(), getDateTo(), getStart(), getLength(),
						ErrorType.MERCHANT_EXCEPTION.getResponseMessage(), getSettledFlag()));
				recordsFiltered = recordsTotal;

				return SUCCESS;
			} else {
				totalCount = exceptionReportData.getDataCount(getMerchant(), getAcquirer(), getStatus(), getDateFrom(),
						getDateTo(), ErrorType.MERCHANT_EXCEPTION.getResponseMessage(), getSettledFlag());
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}
				aaData = encoder.encodeExceptionReportObj(exceptionReportData.getData(getMerchant(), getAcquirer(),
						getStatus(), getDateFrom(), getDateTo(), getStart(), getLength(),
						ErrorType.MERCHANT_EXCEPTION.getResponseMessage(), getSettledFlag()));
				recordsFiltered = recordsTotal;
				return SUCCESS;
			}

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}

	}

	public String bankException() {

		int totalCount;
		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		try {
			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				totalCount = exceptionReportData.getDataCount(getMerchant(), getAcquirer(), getStatus(), getDateFrom(),
						getDateTo(), ErrorType.BANK_EXCEPTION.getResponseMessage(), getSettledFlag());
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}
				aaData = encoder.encodeExceptionReportObj(exceptionReportData.getData(getMerchant(), getAcquirer(),
						getStatus(), getDateFrom(), getDateTo(), getStart(), getLength(),
						ErrorType.BANK_EXCEPTION.getResponseMessage(), getSettledFlag()));
				recordsFiltered = recordsTotal;

				return SUCCESS;
			} else {
				totalCount = exceptionReportData.getDataCount(getMerchant(), getAcquirer(), getStatus(), getDateFrom(),
						getDateTo(), ErrorType.BANK_EXCEPTION.getResponseMessage(), getSettledFlag());
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}
				aaData = encoder.encodeExceptionReportObj(exceptionReportData.getData(getMerchant(), getAcquirer(),
						getStatus(), getDateFrom(), getDateTo(), getStart(), getLength(),
						ErrorType.BANK_EXCEPTION.getResponseMessage(), getSettledFlag()));
				recordsFiltered = recordsTotal;
				return SUCCESS;
			}

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}

	}

	public String bankExceptionDownload() {
		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		List<ExceptionReport> bankExceptionList = new ArrayList<ExceptionReport>();

		bankExceptionList = exceptionReportData.getDataForDownload(getMerchant(), getAcquirer(), getStatus(),
				getDateFrom(), getDateTo(), ErrorType.BANK_EXCEPTION.getResponseMessage(), getSettledFlag());

		logger.info("List generated successfully for Download BankException Report");
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		// Create a blank sheet
		Sheet sheet = wb.createSheet("BankException Report");
		row = sheet.createRow(0);

		row.createCell(0).setCellValue("Sr No");
		row.createCell(1).setCellValue("Txn Id");
		row.createCell(2).setCellValue("Pg Ref Num");
		row.createCell(3).setCellValue("Merchant");
		row.createCell(4).setCellValue("Acquirer");
		row.createCell(5).setCellValue("Date");
		row.createCell(6).setCellValue("Order Id");
		row.createCell(7).setCellValue("Settled Flag");
		row.createCell(8).setCellValue("Status");
		row.createCell(9).setCellValue("Pg Settled Amount");
		row.createCell(10).setCellValue("Acquirer Settled Amount");
		row.createCell(11).setCellValue("Difference Amount");
		row.createCell(12).setCellValue("Exception");

		for (ExceptionReport bankException : bankExceptionList) {
			row = sheet.createRow(rownum++);
			bankException.setSrNo(String.valueOf(rownum - 1));
			Object[] objArr = bankException.downloadBankExceptionReport();

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
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String FILE_EXTENSION = ".xlsx";
			String dateFolder = dateFormat.format(new Date()).split(" ")[0];
			
			if (isGenerateReport()) {
				String fileLocation = PropertiesManager.propertiesMap
						.get(Constants.REPORTS_FILE_LOCATION_URL.getValue());
				try {
					Files.createDirectories(Paths.get(fileLocation));
				} catch (IOException e1) {
					logger.error("Error in creating Directorie ", e1);
				}

				if (sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
					
					if (sessionUser.getUserType().equals(UserType.ADMIN)) {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "AdminCreated/" + dateFolder + "/"
								+ sessionUser.getPayId() + "/";
					} else if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
						fileLocation = PropertiesManager.propertiesMap
								.get(Constants.REPORTS_FILE_LOCATION_URL.getValue()) + "/SubAdminCreated/" + dateFolder + "/"
								+ sessionUser.getPayId() + "/";
					}
					
					if (StringUtils.isNotBlank(getMerchant()) && !getMerchant().equalsIgnoreCase("ALL")) {

						fileName = getReportType() + "_Report_" + getMerchant() + "_"
								+ DateCreater.changeDateString(dateFrom) + "_" + DateCreater.changeDateString(dateTo)
								+ FILE_EXTENSION;
					} else {
						fileName = getReportType() + "_Report_" + DateCreater.changeDateString(dateFrom) + "_"
								+ DateCreater.changeDateString(dateTo) + FILE_EXTENSION;
					}
				} 
				File file = new File(fileLocation, fileName);
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				logger.info(fileName + " File generated successfully");
				reportGenerateService.insertFileStatusInDB(getReportType(), fileName, fileLocation, getMerchant(),
						"", "", dateFrom, dateTo,sessionUser.getPayId());
			} else {
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				fileName = "Exception_Report" + df.format(new Date()) + FILE_EXTENSION;
				File file = new File(fileName);

				// this Writes the workbook
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				fileInputStream = new FileInputStream(file);
				addActionMessage(fileName + " written successfully on disk.");
				logger.info("File generated successfully for ExceptionReportController");
			}
		} catch (Exception exception) {
			setGenerateReport(false);
			logger.error("Exception", exception);
		}
		return SUCCESS;
	}

	public String generateReportFile() {

		setGenerateReport(true);
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					setReportType("BankException");
					bankExceptionDownload();
				} catch (Exception e) {
					setGenerateReport(false);
					logger.error("Exception while generating BankException Report ", e);
				}
			}
		};

		propertiesManager.executorImpl(runnable);

		return SUCCESS;

	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getMerchant() {
		return merchant;
	}

	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public BigInteger getRecordsTotal() {
		return recordsTotal;
	}

	public void setRecordsTotal(BigInteger recordsTotal) {
		this.recordsTotal = recordsTotal;
	}

	public BigInteger getRecordsFiltered() {
		return recordsFiltered;
	}

	public void setRecordsFiltered(BigInteger recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public List<ExceptionReport> getAaData() {
		return aaData;
	}

	public void setAaData(List<ExceptionReport> aaData) {
		this.aaData = aaData;
	}

	public void validate() {
		logger.info("Inside validate");
	}

	public String getSettledFlag() {
		return settledFlag;
	}

	public void setSettledFlag(String settledFlag) {
		this.settledFlag = settledFlag;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
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

}
