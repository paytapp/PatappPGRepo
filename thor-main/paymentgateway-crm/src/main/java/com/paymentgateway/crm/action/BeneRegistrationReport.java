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

import com.paymentgateway.commons.dao.MerchantInitiatedDirectDao;
import com.paymentgateway.commons.user.ImpsDownloadObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;

public class BeneRegistrationReport extends AbstractSecureAction {

	private static final long serialVersionUID = 2811346574562571717L;

	private static Logger logger = LoggerFactory.getLogger(BeneRegistrationReport.class.getName());
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private MerchantInitiatedDirectDao merchantInitiatedDirectDao;
	
	private String payId;
	private String subMerchantPayId;
	private String channel;
	private String status;
	private String dateTo;
	private String dateFrom;
	private InputStream fileInputStream;
	private String filename;
	
	//hide
	private String bankIFSC;
	private String bankAccountNumber;
	private String response;
	private String responseMsg;
	
	private List<ImpsDownloadObject> aaData;
	private User sessionUser = new User();
	public String execute() {
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		logger.info("Inside merchantInitiatedDirectReport()");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
		String payIdReport="";
		String subMerchantPayIdReport="";
		
		User userByPayId = null;
		
		if(sessionUser.getUserType().equals(UserType.SUBUSER)) {
			User user =null;
			if(StringUtils.isNotBlank(sessionUser.getParentPayId())) {
			user = userDao.findPayId(sessionUser.getParentPayId());
			if(user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
				payIdReport = user.getSuperMerchantId();
				subMerchantPayIdReport = user.getPayId();
			}else {
				if(StringUtils.isNotBlank(subMerchantPayId)) {
					payIdReport = payId;
					subMerchantPayIdReport = subMerchantPayId;
				}else {
				payIdReport = payId;
				}
			}
			}
			}else
		if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
			payIdReport = sessionUser.getSuperMerchantId();
			subMerchantPayIdReport = sessionUser.getPayId();
		}else
		if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
			userByPayId = userDao.findPayId(subMerchantPayId);
			payIdReport = userByPayId.getSuperMerchantId();
			subMerchantPayIdReport = subMerchantPayId;
			
		} else {
			payIdReport = payId;
			subMerchantPayIdReport = subMerchantPayId;
		}
		
		setAaData(merchantInitiatedDirectDao.BeneRegistrationReportData(dateFrom, dateTo, payIdReport, subMerchantPayIdReport , status, channel, sessionUser));
		
		
		return SUCCESS;
		
		}catch(Exception e) {
			logger.error("exception " , e);
		}
		return SUCCESS;
	}

	public String BeneRegistrationReportDownload() {
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		logger.info("Inside BeneRegistrationReportDownload()");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<ImpsDownloadObject> merchantInitiatedDirectList = new ArrayList<ImpsDownloadObject>();
		String payIdReport="";
		String subMerchantPayIdReport="";
		
		User userByPayId = null;
		if(sessionUser.getUserType().equals(UserType.SUBUSER)) {
			User user =null;
			if(StringUtils.isNotBlank(sessionUser.getParentPayId())) {
			user = userDao.findPayId(sessionUser.getParentPayId());
			if(user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
				payIdReport = user.getSuperMerchantId();
				subMerchantPayIdReport = user.getPayId();
			}else {
				
				if(user.isSuperMerchant() == true && StringUtils.isNotBlank(user.getSuperMerchantId())) {
					if(StringUtils.isNotBlank(subMerchantPayId)) {
						payIdReport = payId;
						subMerchantPayIdReport = subMerchantPayId;
					}else {
						payIdReport = payId;
						subMerchantPayIdReport = "ALL";
					}
				}
				else {
				payIdReport = payId;
				}
			}
			}
			}else
		if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
			payIdReport = sessionUser.getSuperMerchantId();
			subMerchantPayIdReport = sessionUser.getPayId();
		}else
		if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
			userByPayId = userDao.findPayId(subMerchantPayId);
			payIdReport = userByPayId.getSuperMerchantId();
			subMerchantPayIdReport = subMerchantPayId;
			
		} else {
			payIdReport = payId;
			subMerchantPayIdReport = subMerchantPayId;
		}
		
		merchantInitiatedDirectList = merchantInitiatedDirectDao.BeneRegistrationReportData(dateFrom, dateTo, payIdReport, subMerchantPayIdReport , status, channel, sessionUser);
		BigDecimal st = null;

		logger.info("List generated successfully for beneRegistrationReportDownload");
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		// Create a blank sheet
		Sheet sheet =  wb.createSheet("Bene Registration Report");
		row = sheet.createRow(0);
		
		if(StringUtils.isNotBlank(subMerchantPayIdReport) ||((payIdReport.equalsIgnoreCase("ALL")))) {
			row.createCell(0).setCellValue("Merchant Name");
			row.createCell(1).setCellValue("Sub Merchant Name");
			row.createCell(2).setCellValue("Order Id");
			row.createCell(3).setCellValue("Channel");
			row.createCell(4).setCellValue("Phone Number");
			row.createCell(5).setCellValue("Bene Name");
			row.createCell(6).setCellValue("Bank Account Number/VPA");
			row.createCell(7).setCellValue("IFSC Code");
			row.createCell(8).setCellValue("Status");
					
			for (ImpsDownloadObject transactionSearch : merchantInitiatedDirectList) {
				row = sheet.createRow(rownum++);
				//transactionSearch.setSrNo(String.valueOf(rownum-1));
				Object[] objArr = transactionSearch.myXlsxMethodDownloadBeneRegistrationReportSub();

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
			row.createCell(1).setCellValue("Order Id");
			row.createCell(2).setCellValue("Channel");
			row.createCell(3).setCellValue("Phone Number");
			row.createCell(4).setCellValue("Bene Name");
			row.createCell(5).setCellValue("Bank Account Number/VPA");
			row.createCell(6).setCellValue("IFSC Code");
			row.createCell(7).setCellValue("Status");
			
			
			for (ImpsDownloadObject transactionSearch : merchantInitiatedDirectList) {
				row = sheet.createRow(rownum++);
				//transactionSearch.setSrNo(String.valueOf(rownum-1));
				Object[] objArr = transactionSearch.myXlsxMethodDownloadBeneRegistrationReportMer();

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
			String FILE_EXTENSION = ".xlsx";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			filename = "Bene_Registration_Report" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(filename);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(filename + " written successfully on disk.");
			logger.info("File generated successfully for BeneRegistrationReportDownload");
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}

		return SUCCESS;
	}
	
	public String deleteBeneficiary() {
		logger.info("Inside deleteBeneficiary()");
		try {
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		String payIdReport="";
		String subMerchantPayIdReport="";
		
		User userByPayId = null;
		if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
			payIdReport = sessionUser.getSuperMerchantId();
			subMerchantPayIdReport = sessionUser.getPayId();
		}else{
			payIdReport = payId;
			subMerchantPayIdReport = subMerchantPayId;
		}
		
		if(merchantInitiatedDirectDao.deleteBeneficiaryData(payIdReport,subMerchantPayIdReport,bankAccountNumber,bankIFSC,sessionUser)) {
			setResponse("success");
			setResponseMsg("Successfully Deleted");
		}else {
			setResponse("failed");
			setResponseMsg("UnSuccessful");
		}
		}catch (Exception e) {
			logger.error("exception " , e);
			setResponse("failed");
			setResponseMsg("Failed Due To System Error");
		}
		return SUCCESS;
		
	}
	
	public String getDateTo() {
		return dateTo;
	}
	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}
	public String getDateFrom() {
		return dateFrom;
	}
	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}
	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public List<ImpsDownloadObject> getAaData() {
		return aaData;
	}

	public void setAaData(List<ImpsDownloadObject> aaData) {
		this.aaData = aaData;
	}


	public InputStream getFileInputStream() {
		return fileInputStream;
	}


	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}


	public String getFilename() {
		return filename;
	}


	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getBankIFSC() {
		return bankIFSC;
	}

	public void setBankIFSC(String bankIFSC) {
		this.bankIFSC = bankIFSC;
	}

	public String getBankAccountNumber() {
		return bankAccountNumber;
	}

	public void setBankAccountNumber(String bankAccountNumber) {
		this.bankAccountNumber = bankAccountNumber;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getResponseMsg() {
		return responseMsg;
	}

	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}
	
	
}
