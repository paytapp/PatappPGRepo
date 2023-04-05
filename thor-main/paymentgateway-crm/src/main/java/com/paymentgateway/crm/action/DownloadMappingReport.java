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

import com.paymentgateway.commons.dao.DownloadMappingDetailsFactory;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.ECollectionObject;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.ProductionReportObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.crm.mongoReports.TxnReports;

public class DownloadMappingReport extends AbstractSecureAction{
	
	@Autowired
	private CrmValidator validator;
	
	@Autowired
	private DownloadMappingDetailsFactory mappingDetails;
	
	private static final long serialVersionUID = 2871252777725723745L;

	private static Logger logger = LoggerFactory.getLogger(DownloadMappingReport.class.getName());
	
	private String merchant;
	private String acquirer;
	private String currency;
	private InputStream fileInputStream;
	private String filename;
	private User sessionUser = new User();
	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
	
	@SuppressWarnings("static-access")
	public String execute() {
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<ProductionReportObject> mappingList = new ArrayList<ProductionReportObject>();
		if(merchant.equalsIgnoreCase("ALL") && acquirer.equalsIgnoreCase("ALL") && currency.equalsIgnoreCase("ALL")) {
		mappingList = mappingDetails.getActiveMappingList( merchant, acquirer, currency, sessionUser);
		}
		else if(!(merchant.equalsIgnoreCase("ALL")) && acquirer.equalsIgnoreCase("ALL") && currency.equalsIgnoreCase("ALL")) {
			mappingList = mappingDetails.getActiveMappingListByMerchant( merchant, acquirer, currency, sessionUser);	
		}
		else if(!(merchant.equalsIgnoreCase("ALL")) && !(acquirer.equalsIgnoreCase("ALL")) && currency.equalsIgnoreCase("ALL")) {
			mappingList = mappingDetails.getMappingListByMerchantAcquirer( merchant, acquirer, currency, sessionUser);
		}
		else if((merchant.equalsIgnoreCase("ALL")) && !(acquirer.equalsIgnoreCase("ALL")) && currency.equalsIgnoreCase("ALL")) {
			mappingList = mappingDetails.getMappingListByAcquirer( merchant, acquirer, currency, sessionUser);
		}
		else if((merchant.equalsIgnoreCase("ALL")) && (acquirer.equalsIgnoreCase("ALL")) && !(currency.equalsIgnoreCase("ALL"))) {
			mappingList = mappingDetails.getMappingListByCurrency( merchant, acquirer, currency, sessionUser);
		}
		else if((merchant.equalsIgnoreCase("ALL")) && !(acquirer.equalsIgnoreCase("ALL")) && !(currency.equalsIgnoreCase("ALL"))) {
			mappingList = mappingDetails.getMappingListByAcquirerCurrency( merchant, acquirer, currency, sessionUser);
		}
		else if(!(merchant.equalsIgnoreCase("ALL")) && (acquirer.equalsIgnoreCase("ALL")) && !(currency.equalsIgnoreCase("ALL"))) {
			mappingList = mappingDetails.getMappingListByMerchantCurrency( merchant, acquirer, currency, sessionUser);
		}else {
			mappingList = mappingDetails.getMappingListByMerchantAcquirerCurrency( merchant, acquirer, currency, sessionUser);
		}
		BigDecimal st = null;

		logger.info("List generated successfully for DownloadPaymentsReportAction");
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		// Create a blank sheet
		Sheet sheet =  wb.createSheet("Mapping Report");
		row = sheet.createRow(0);
		
		if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			
			row.createCell(0).setCellValue("Merchant Name");
			row.createCell(1).setCellValue("Pay ID");
			row.createCell(2).setCellValue("MOP");
			row.createCell(3).setCellValue("Payment Type");
			row.createCell(4).setCellValue("Code");
			
			
			  for (ProductionReportObject mapping : mappingList) { row =
			  sheet.createRow(rownum++); Object[] objArr =
					  mapping.myCsvMethodDownloadMappingByAdminView();
			  
			  int cellnum = 0; 
			  for (Object obj : objArr) { // this line creates a cell inthe next column of that row 
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
			filename = "Mapping_Report" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(filename);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(filename + " written successfully on disk.");
			logger.info("File generated successfully for DownloadMappingReportAction");
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}

		return SUCCESS;
		
	}
	
	public void validate() {		
		if ((validator.validateBlankField(getAcquirer()) || (getAcquirer().equals(CrmFieldConstants.ALL.getValue())))) {
		} else if (!(validator.validateField(CrmFieldType.ACQUIRER, getAcquirer()))) {
		addFieldError(CrmFieldType.ACQUIRER.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
	}
	if ((validator.validateBlankField(getMerchant())) || (getMerchant().equals(CrmFieldConstants.ALL.getValue()))) {
		} else if (!(validator.validateField(CrmFieldType.PAY_ID, getMerchant()))) {
		addFieldError(CrmFieldType.PAY_ID.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
	}
	if ((validator.validateBlankField(getCurrency()) || (getCurrency().equals(CrmFieldConstants.ALL.getValue())))) {
		} else if (!(validator.validateField(CrmFieldType.CURRENCY, getCurrency()))) {
		addFieldError(CrmFieldType.CURRENCY.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
	}
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

    public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
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
	


