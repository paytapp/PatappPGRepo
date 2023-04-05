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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.ImpsDownloadObject;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.TransactionSearchDownloadObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.mongoReports.TxnReports;

public class DownloadImpsReportAction extends AbstractSecureAction {
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private TxnReports txnReports;
	
	@Autowired
	private PropertiesManager propertiesManager;
	
	private static final long serialVersionUID = 2871252777725723745L;

	private static Logger logger = LoggerFactory.getLogger(DownloadImpsReportAction.class.getName());
	
	private List<Merchants> merchantList = new LinkedList<Merchants>();
	
	private String reportMerchant;
	private String reportSubMerchant;
	private String reportStatus;
	private String reportDateFrom;
	private String reportDateTo;
	private String reportChannel;
		
	private InputStream fileInputStream;
	private String filename;
	private User sessionUser = new User();
	
	
	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
	
	@SuppressWarnings("static-access")
	public String execute() {
		
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<ImpsDownloadObject> impsList = new ArrayList<ImpsDownloadObject>();
		setReportDateFrom(DateCreater.toDateTimeformatCreater(reportDateFrom));
		setReportDateTo(DateCreater.formDateTimeformatCreater(reportDateTo));
		String merchantPayId = "";
		String subMerchantPayId = "";
		if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
			merchantPayId = sessionUser.getSuperMerchantId();
			subMerchantPayId = sessionUser.getPayId();
		} else {
			merchantPayId =  reportMerchant;
			
			if (StringUtils.isNotBlank(reportSubMerchant) && !reportSubMerchant.equalsIgnoreCase("All")) {
				subMerchantPayId = reportSubMerchant;
			}
			if(StringUtils.isNotBlank(reportSubMerchant) && reportSubMerchant.equalsIgnoreCase("All")) {
				subMerchantPayId  = reportSubMerchant;
			}
		}
		
		impsList = txnReports.searchImpsReportForDownload( merchantPayId,subMerchantPayId , reportStatus,reportDateFrom,reportDateTo,sessionUser, reportChannel);
		BigDecimal st = null;

		logger.info("List generated successfully for DownloadPaymentsReportAction");
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		// Create a blank sheet
		Sheet sheet =  wb.createSheet("IMPS Transfer Report");
		row = sheet.createRow(0);
		if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			if(StringUtils.isBlank(subMerchantPayId) && (!(merchantPayId.equalsIgnoreCase("ALL")))) {
			row.createCell(0).setCellValue("Merchant Name");
			row.createCell(1).setCellValue("Transaction ID");
			row.createCell(2).setCellValue("Pay ID");
			row.createCell(3).setCellValue("Order ID");
			row.createCell(4).setCellValue("Mobile No");
			row.createCell(5).setCellValue("Channel");
			row.createCell(6).setCellValue("IMPS Ref Number");
			row.createCell(7).setCellValue("Date of Transfer");
			row.createCell(8).setCellValue("TXNs Captured From");
			row.createCell(9).setCellValue("TXNs Captured To");
			row.createCell(10).setCellValue("System Settlement Date");
			row.createCell(11).setCellValue("Bank Account Name");
			row.createCell(12).setCellValue("Bank Account Number");
			row.createCell(13).setCellValue("Bank IFSC");
			row.createCell(14).setCellValue("Amount");
			row.createCell(15).setCellValue("Response Message");
			row.createCell(16).setCellValue("Status");
			
			
			for (ImpsDownloadObject transactionSearch : impsList) {
				row = sheet.createRow(rownum++);
				//transactionSearch.setSrNo(String.valueOf(rownum-1));
				Object[] objArr = transactionSearch.myCsvMethodDownloadImpsReportByView();

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
		  }else {
			    row.createCell(0).setCellValue("Merchant Name");
			    row.createCell(1).setCellValue("Sub Merchant Name");
				row.createCell(2).setCellValue("Transaction Id");
				row.createCell(3).setCellValue("Pay ID");
				row.createCell(4).setCellValue("Order Id");
				row.createCell(5).setCellValue("Mobile No");
				row.createCell(6).setCellValue("Channel");
				row.createCell(7).setCellValue("IMPS Ref Number");
				row.createCell(8).setCellValue("Date of Transfer");
				row.createCell(9).setCellValue("TXNs Captured From");
				row.createCell(10).setCellValue("TXNs Captured To");
				row.createCell(11).setCellValue("System Settlement Date");
				row.createCell(12).setCellValue("Bank Account Name");
				row.createCell(13).setCellValue("Bank Account Number");
				row.createCell(14).setCellValue("Bank IFSC");
				row.createCell(15).setCellValue("Amount");
				row.createCell(16).setCellValue("Response Message");
				row.createCell(17).setCellValue("Status");
				
				
				for (ImpsDownloadObject transactionSearch : impsList) {
					row = sheet.createRow(rownum++);
					//transactionSearch.setSrNo(String.valueOf(rownum-1));
					Object[] objArr = transactionSearch.myCsvMethodDownloadImpsReportBySubMerchantView();

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

			
		}else{
			if(StringUtils.isBlank(subMerchantPayId) && (!(merchantPayId.equalsIgnoreCase("ALL")))) {
			row.createCell(0).setCellValue("Merchant Name");
			row.createCell(1).setCellValue("Transaction Id");
			row.createCell(2).setCellValue("Pay ID");
			row.createCell(3).setCellValue("Order Id");
			row.createCell(4).setCellValue("Mobile No");
			row.createCell(5).setCellValue("Channel");
			row.createCell(6).setCellValue("IMPS Ref Number");
			row.createCell(7).setCellValue("Date of Transfer");
			row.createCell(8).setCellValue("System Settlement Date");
			row.createCell(9).setCellValue("Bank Account Name");
			row.createCell(10).setCellValue("Bank Account Number");
			row.createCell(11).setCellValue("Bank IFSC");
			row.createCell(12).setCellValue("Amount");
			row.createCell(13).setCellValue("Response Message");
			row.createCell(14).setCellValue("Status");
			
			
			for (ImpsDownloadObject transactionSearch : impsList) {
				row = sheet.createRow(rownum++);
				//transactionSearch.setSrNo(String.valueOf(rownum-1));
				Object[] objArr = transactionSearch.csvMethodDownloadForMerchantImpsReport();

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
			}else {
				row.createCell(0).setCellValue("Merchant Name");
				 row.createCell(1).setCellValue("Sub Merchant Name");
				row.createCell(2).setCellValue("Transaction Id");
				row.createCell(3).setCellValue("Pay ID");
				row.createCell(4).setCellValue("Order Id");
				row.createCell(5).setCellValue("Mobile No");
				row.createCell(6).setCellValue("Channel");
				row.createCell(7).setCellValue("IMPS Ref Number");
				row.createCell(8).setCellValue("Date of Transfer");
				row.createCell(9).setCellValue("System Settlement Date");
				row.createCell(10).setCellValue("Bank Account Name");
				row.createCell(11).setCellValue("Bank Account Number");
				row.createCell(12).setCellValue("Bank IFSC");
				row.createCell(13).setCellValue("Amount");
				row.createCell(14).setCellValue("Response Message");
				row.createCell(15).setCellValue("Status");
				
				
				for (ImpsDownloadObject transactionSearch : impsList) {
					row = sheet.createRow(rownum++);
					//transactionSearch.setSrNo(String.valueOf(rownum-1));
					Object[] objArr = transactionSearch.csvMethodDownloadForSubMerchantImpsReport();

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
    			String FILE_EXTENSION = ".csv";
    			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
    			filename = "IMPS_Transfer_Report" + df.format(new Date()) + FILE_EXTENSION;
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
    		} catch (Exception exception) {
    			logger.error("Exception", exception);
    		}

    		return SUCCESS;

	}
	
	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}

	public String getReportMerchant() {
		return reportMerchant;
	}

	public void setReportMerchant(String reportMerchant) {
		this.reportMerchant = reportMerchant;
	}
	
	public String getReportSubMerchant() {
		return reportSubMerchant;
	}

	public void setReportSubMerchant(String reportSubMerchant) {
		this.reportSubMerchant = reportSubMerchant;
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

	public String getReportStatus() {
		return reportStatus;
	}

	public void setReportStatus(String reportStatus) {
		this.reportStatus = reportStatus;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}
	
	public String getReportChannel() {
		return reportChannel;
	}

	public void setReportChannel(String reportChannel) {
		this.reportChannel = reportChannel;
	}

}