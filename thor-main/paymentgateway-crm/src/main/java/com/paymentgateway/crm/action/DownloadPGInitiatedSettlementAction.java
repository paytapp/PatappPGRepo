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

import com.paymentgateway.commons.dao.PGPayoutDao;
import com.paymentgateway.commons.user.ImpsDownloadObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;

public class DownloadPGInitiatedSettlementAction extends AbstractSecureAction{
	
	@Autowired
	private PGPayoutDao vendorPayOutDao;
	
	@Autowired
	private UserDao userDao;

	private static final long serialVersionUID = 2871252777725723745L;

	private static Logger logger = LoggerFactory.getLogger(DownloadPGInitiatedSettlementAction.class.getName());
	
	private String reportPGMerchant;
	private String reportPGSubMerchant;
	private String reportPGStatus;
	private String reportPGDateFrom;
	private String reportPGDateTo;
	private String reportPGOrderId;
	
	private InputStream fileInputStream;
	private String filename;
	private User sessionUser = new User();
	private User user = new User();
	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
	@SuppressWarnings("static-access")
	public String execute() {
	
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<ImpsDownloadObject> pgReportList = new ArrayList<ImpsDownloadObject>();
		setReportPGDateFrom(DateCreater.toDateTimeformatCreater(reportPGDateFrom));
		setReportPGDateTo(DateCreater.formDateTimeformatCreater(reportPGDateTo));
		String merchantPayId = "";
		String subMerchantPayIdd = "";
		if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
			merchantPayId = sessionUser.getSuperMerchantId();
			subMerchantPayIdd = sessionUser.getPayId();
		} else {
			merchantPayId =  reportPGMerchant;
			
			if (StringUtils.isNotBlank(reportPGSubMerchant) && !reportPGSubMerchant.equalsIgnoreCase("All")) {
				subMerchantPayIdd = reportPGSubMerchant;
			}else {
				subMerchantPayIdd = reportPGSubMerchant;
			}
			
		}
		pgReportList = vendorPayOutDao.fetchVendorPayOutReportData(merchantPayId, subMerchantPayIdd, getReportPGOrderId(), getReportPGStatus(), getReportPGDateFrom(), getReportPGDateTo(),sessionUser);
		logger.info("List generated successfully for DownloadPGInitiatedSettlementAction");
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		// Create a blank sheet
		Sheet sheet =  wb.createSheet("PG Initiated Settlement Report");
		row = sheet.createRow(0);
		
		if(StringUtils.isNotBlank(merchantPayId)) {
		  user = userDao.findPayId(merchantPayId);
		}
		
		if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			if( ((StringUtils.isBlank(merchantPayId)) && (StringUtils.isBlank(subMerchantPayIdd)))
					|| ( (StringUtils.isNotBlank(merchantPayId)) && (subMerchantPayIdd.equalsIgnoreCase("All")) )
					|| ( (StringUtils.isNotBlank(merchantPayId)) &&  (StringUtils.isNotBlank(subMerchantPayIdd)) )
					) {
				row.createCell(0).setCellValue("Merchant Name");
			    row.createCell(1).setCellValue("Sub Merchant Name");
				row.createCell(2).setCellValue("Transaction ID");
				row.createCell(3).setCellValue("Order ID");
				row.createCell(4).setCellValue("Date");
				row.createCell(5).setCellValue("Mobile No");
				row.createCell(6).setCellValue("Beneficiary Name");
				row.createCell(7).setCellValue("Bank Account Number");
				row.createCell(8).setCellValue("Bank IFSC");
				row.createCell(9).setCellValue("Amount");
				row.createCell(10).setCellValue("Response Message");
				row.createCell(11).setCellValue("Status");
				
				for (ImpsDownloadObject transactionSearch : pgReportList) {
					row = sheet.createRow(rownum++);
					//transactionSearch.setSrNo(String.valueOf(rownum-1));
					Object[] objArr = transactionSearch.myCsvMethodDownloadPGInitiatedForSubMerchant();

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
				row.createCell(1).setCellValue("Transaction ID");
				row.createCell(2).setCellValue("Order ID");
				row.createCell(3).setCellValue("Date");
				row.createCell(4).setCellValue("Mobile No");
				row.createCell(5).setCellValue("Beneficiary Name");
				row.createCell(6).setCellValue("Bank Account Number");
				row.createCell(7).setCellValue("Bank IFSC");
				row.createCell(8).setCellValue("Amount");
				row.createCell(9).setCellValue("Response Message");
				row.createCell(10).setCellValue("Status");
				for (ImpsDownloadObject transactionSearch : pgReportList) {
					row = sheet.createRow(rownum++);
					//transactionSearch.setSrNo(String.valueOf(rownum-1));
					Object[] objArr = transactionSearch.myCsvMethodDownloadPGInitiatedForMerchant();

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
		}else {
			if((sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) || (sessionUser.isSuperMerchant() == true) ) {
				row.createCell(0).setCellValue("Merchant Name");
			    row.createCell(1).setCellValue("Sub Merchant Name");
				row.createCell(2).setCellValue("Transaction ID");
				row.createCell(3).setCellValue("Order ID");
				row.createCell(4).setCellValue("Date");
				row.createCell(5).setCellValue("Mobile No");
				row.createCell(6).setCellValue("Beneficiary Name");
				row.createCell(7).setCellValue("Bank Account Number");
				row.createCell(8).setCellValue("Bank IFSC");
				row.createCell(9).setCellValue("Amount");
				row.createCell(10).setCellValue("Response Message");
				row.createCell(11).setCellValue("Status");
				
				for (ImpsDownloadObject transactionSearch : pgReportList) {
					row = sheet.createRow(rownum++);
					//transactionSearch.setSrNo(String.valueOf(rownum-1));
					Object[] objArr = transactionSearch.myCsvMethodDownloadPGInitiatedForSubMerchant();

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
				row.createCell(1).setCellValue("Transaction ID");
				row.createCell(2).setCellValue("Order ID");
				row.createCell(3).setCellValue("Date");
				row.createCell(4).setCellValue("Mobile No");
				row.createCell(5).setCellValue("Beneficiary Name");
				row.createCell(6).setCellValue("Bank Account Number");
				row.createCell(7).setCellValue("Bank IFSC");
				row.createCell(8).setCellValue("Amount");
				row.createCell(9).setCellValue("Response Message");
				row.createCell(10).setCellValue("Status");
				for (ImpsDownloadObject transactionSearch : pgReportList) {
					row = sheet.createRow(rownum++);
					//transactionSearch.setSrNo(String.valueOf(rownum-1));
					Object[] objArr = transactionSearch.myCsvMethodDownloadPGInitiatedForMerchant();

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
			filename = "PG_Initiated_Settlement_Report" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(filename);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(filename + " written successfully on disk.");
			logger.info("File generated successfully for DownloadPGInitiatedSettlementAction");
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}

			
			return SUCCESS;
		
	}
	
	public String getReportPGMerchant() {
		return reportPGMerchant;
	}
	public void setReportPGMerchant(String reportPGMerchant) {
		this.reportPGMerchant = reportPGMerchant;
	}
	public String getReportPGSubMerchant() {
		return reportPGSubMerchant;
	}
	public void setReportPGSubMerchant(String reportPGSubMerchant) {
		this.reportPGSubMerchant = reportPGSubMerchant;
	}
	public String getReportPGStatus() {
		return reportPGStatus;
	}
	public void setReportPGStatus(String reportPGStatus) {
		this.reportPGStatus = reportPGStatus;
	}
	public String getReportPGDateFrom() {
		return reportPGDateFrom;
	}
	public void setReportPGDateFrom(String reportPGDateFrom) {
		this.reportPGDateFrom = reportPGDateFrom;
	}
	public String getReportPGDateTo() {
		return reportPGDateTo;
	}
	public void setReportPGDateTo(String reportPGDateTo) {
		this.reportPGDateTo = reportPGDateTo;
	}
	public String getReportPGOrderId() {
		return reportPGOrderId;
	}
	public void setReportPGOrderId(String reportPGOrderId) {
		this.reportPGOrderId = reportPGOrderId;
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
}
