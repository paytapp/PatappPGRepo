package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
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
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.mongoReports.TxnReports;

public class DownloadFraudAnalyticsReportData extends AbstractSecureAction {
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private TxnReports txnReports;
	
	@Autowired
	private PropertiesManager propertiesManager;
	
	private static final long serialVersionUID = 2871252777725723745L;

	private static Logger logger = LoggerFactory.getLogger(DownloadFraudAnalyticsReportData.class.getName());
	
	private String reportMerchant;
	private String subMerchant;
	private String reportPaymentRegion;
	private String reportCountryCodes;
	private String reportDateFrom;
	private String reportDateTo;
	private String reportStatus;
	private InputStream fileInputStream;
	private String filename;
	private User sessionUser = new User();
	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
	@SuppressWarnings("static-access")
	public String execute() {
	
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<TransactionSearch> fraudAnalyticsDataList = new ArrayList<TransactionSearch>();
		setReportDateFrom(DateCreater.toDateTimeformatCreater(reportDateFrom));
		setReportDateTo(DateCreater.formDateTimeformatCreater(reportDateTo));
		
		String merchantPayId = "";
		String subMerchantPayId = "";
		if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
			merchantPayId = sessionUser.getSuperMerchantId();
			subMerchantPayId = sessionUser.getPayId();
		} else {
			
			if (StringUtils.isNotBlank(subMerchant) && !subMerchant.equalsIgnoreCase("All")) {
				subMerchantPayId = userDao.getPayIdByEmailId(subMerchant);
			}
			 if(StringUtils.isNotBlank(reportMerchant) && !reportMerchant.equalsIgnoreCase("All")) {
             	merchantPayId = userDao.getPayIdByEmailId(reportMerchant);
				}
		}
		
		fraudAnalyticsDataList = txnReports.fraudAnalyticsReportData( merchantPayId, subMerchantPayId, reportPaymentRegion, reportCountryCodes, reportStatus, reportDateFrom, reportDateTo ,sessionUser);
		BigDecimal st = null;

		logger.info("List generated successfully for DownloadPaymentsReportAction");
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		// Create a blank sheet
		Sheet sheet =  wb.createSheet("Captured Report");
		row = sheet.createRow(0);
		
        if(StringUtils.isBlank(subMerchant)) {
			
			row.createCell(0).setCellValue("Merchant Name");
			row.createCell(1).setCellValue("Order ID");
			row.createCell(2).setCellValue("Pg Ref Num");
			row.createCell(3).setCellValue("Date of Transaction");
			row.createCell(4).setCellValue("Country of Origin");
			row.createCell(5).setCellValue("Payment Region");
			row.createCell(6).setCellValue("Payment Type");
			row.createCell(7).setCellValue("Amount");
			row.createCell(8).setCellValue("Total Amount");
			row.createCell(9).setCellValue("Status");
			row.createCell(10).setCellValue("Payment Gateway Response Msg");
			
			for (TransactionSearch transactionList: fraudAnalyticsDataList) {
				row = sheet.createRow(rownum++);
				Object[] objArr = transactionList.myCsvMethodForFraudMerchant();

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

			row.createCell(0).setCellValue("Merchant Name");
			row.createCell(1).setCellValue("Sub-Merchant Name");
			row.createCell(2).setCellValue("Order ID");
			row.createCell(3).setCellValue("Pg Ref Num");
			row.createCell(4).setCellValue("Date of Transaction");
			row.createCell(5).setCellValue("Country of Origin");
			row.createCell(6).setCellValue("Payment Region");
			row.createCell(7).setCellValue("Payment Type");
			row.createCell(8).setCellValue("Amount");
			row.createCell(9).setCellValue("Total Amount");
			row.createCell(10).setCellValue("Status");
			row.createCell(11).setCellValue("Payment Gateway Response Msg");
			
		
		for (TransactionSearch transactionList: fraudAnalyticsDataList) {
			row = sheet.createRow(rownum++);
			Object[] objArr = transactionList.myCsvMethodForFraudSubMerchant();

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
		String FILE_EXTENSION = ".csv";
		DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
		filename = "Fraud_AnalyticsReport" + df.format(new Date()) + FILE_EXTENSION;
		File file = new File(filename);

		// this Writes the workbook
		FileOutputStream out = new FileOutputStream(file);
		wb.write(out);
		out.flush();
		out.close();
		wb.dispose();
		fileInputStream = new FileInputStream(file);
		addActionMessage(filename + " written successfully on disk.");
		logger.info("File generated successfully for DownloadFraudAnalyticsReportAction");
	} catch (Exception exception) {
		logger.error("Exception", exception);
	}
	return SUCCESS;
	}
	
	public String getReportPaymentRegion() {
		return reportPaymentRegion;
	}

	public void setReportPaymentRegion(String reportPaymentRegion) {
		this.reportPaymentRegion = reportPaymentRegion;
	}

	public String getReportCountryCodes() {
		return reportCountryCodes;
	}

	public void setReportCountryCodes(String reportCountryCodes) {
		this.reportCountryCodes = reportCountryCodes;
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
	
	public String getReportStatus() {
		return reportStatus;
	}

	public void setReportStatus(String reportStatus) {
		this.reportStatus = reportStatus;
	}

}
