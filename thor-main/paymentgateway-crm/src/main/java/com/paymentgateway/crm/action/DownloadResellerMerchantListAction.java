package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
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

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.MerchantObject;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;

public class DownloadResellerMerchantListAction extends AbstractSecureAction{
	@Autowired
	private CrmValidator validator;
	
	@Autowired
	private UserDao userDao;
	
	private static final long serialVersionUID = 2871252777725723745L;

	private static Logger logger = LoggerFactory.getLogger(DownloadMerchantAction.class.getName());
	
	private String resellerList;
	private String reportMerchant;
	private String subMerchant;
	private InputStream fileInputStream;
	private String filename;
	private User sessionUser = new User();
	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
	
	@SuppressWarnings("static-access")
	public String execute() {
		String resellerId = "";
		
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<MerchantObject> merchantListUpdated = new ArrayList<MerchantObject>();
		try {
			User user = new User();
			String merchantPayId = "";
			String subMerchantPayIdd = "";
			if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
				merchantPayId = sessionUser.getSuperMerchantId();
				subMerchantPayIdd = sessionUser.getPayId();
			} else {
				merchantPayId = reportMerchant;

				if (StringUtils.isNotBlank(subMerchant) && !subMerchant.equalsIgnoreCase("All")) {
					//subMerchantPayIdd = subMerchantPayId;
					subMerchantPayIdd = userDao.getPayIdByEmailId(subMerchant);
				}else {
					subMerchantPayIdd=subMerchant;
				}
			}
			if((resellerList.equalsIgnoreCase("ALL")) && (merchantPayId.equalsIgnoreCase("ALL"))) {
				List<Merchants> merchantList = new UserDao().getAllActiveReseller();
				for(Merchants merchant : merchantList) {
					List<Merchants>	merchantReseller = new UserDao().getActiveResellerMerchants(merchant.getResellerId());
					List<Merchants>	merchantReseller1 = new UserDao().getResellerByResellerId(merchant.getResellerId());
					for(Merchants merchantUpdate : merchantReseller) {
						String emailId = userDao.getEmailIdByPayId(merchantUpdate.getPayId());
						user = new UserDao().findByEmailId(emailId);
						MerchantObject user1 = new MerchantObject();
						user1.setRegistrationDate(user.getRegistrationDate().toString());
						for(Merchants merchantUpdate1 : merchantReseller1) {
						user1.setResellerName(merchantUpdate1.getBusinessName());
						}
						user1.setBusinessName(merchantUpdate.getBusinessName());
						user1.setPayId(merchantUpdate.getPayId());
						user1.setStatus(user.getUserStatus().toString());
						merchantListUpdated.add(user1);
					}
				}
			}else if(!(resellerList.equalsIgnoreCase("ALL")) && (merchantPayId.equalsIgnoreCase("ALL"))) {
				List<Merchants>	merchantReseller = new UserDao().getActiveResellerMerchants(resellerList);
				List<Merchants>	merchantReseller1 = new UserDao().getResellerByResellerId(resellerList);
				for(Merchants merchantUpdate : merchantReseller) {
					String emailId = userDao.getEmailIdByPayId(merchantUpdate.getPayId());
					user = new UserDao().findByEmailId(emailId);
					MerchantObject user1 = new MerchantObject();
					user1.setRegistrationDate(user.getRegistrationDate().toString());
					for(Merchants merchantUpdate1 : merchantReseller1) {
					user1.setResellerName(merchantUpdate1.getBusinessName());
					}
					user1.setBusinessName(merchantUpdate.getBusinessName());
					user1.setPayId(merchantUpdate.getPayId());
					user1.setStatus(user.getUserStatus().toString());
					merchantListUpdated.add(user1);
				}
			}else if(!(resellerList.equalsIgnoreCase("ALL")) && !(merchantPayId.equalsIgnoreCase("ALL")) && (subMerchantPayIdd.equalsIgnoreCase(""))) {
				List<Merchants>	merchantReseller1 = new UserDao().getResellerByResellerId(resellerList);
				String emailId = userDao.getEmailIdByPayId(merchantPayId);
				user = new UserDao().findByEmailId(emailId);
				MerchantObject user1 = new MerchantObject();
				user1.setRegistrationDate(user.getRegistrationDate().toString());
				for(Merchants merchantUpdate1 : merchantReseller1) {
					user1.setResellerName(merchantUpdate1.getBusinessName());
					}
				user1.setBusinessName(user.getBusinessName());
				user1.setPayId(user.getPayId());
				user1.setStatus(user.getUserStatus().toString());
				merchantListUpdated.add(user1);
				
			}else if(!(resellerList.equalsIgnoreCase("ALL")) && !(merchantPayId.equalsIgnoreCase("ALL")) && (subMerchantPayIdd.equalsIgnoreCase("ALL"))) {
				List<Merchants>	merchantReseller1 = new UserDao().getResellerByResellerId(resellerList);
				List<Merchants> merchantList = userDao.getSubMerchantListBySuperPayId(merchantPayId);
				for(Merchants merchants : merchantList) {
					MerchantObject user1 = new MerchantObject();
					String emailId = userDao.getEmailIdByPayId(merchants.getPayId());
					user = new UserDao().findByEmailId(emailId);
					user1.setRegistrationDate(user.getRegistrationDate().toString());
					for(Merchants merchantUpdate1 : merchantReseller1) {
						user1.setResellerName(merchantUpdate1.getBusinessName());
						}
					user1.setBusinessName(userDao.getBusinessNameByPayId(merchantPayId));
					user1.setSubMerchant(userDao.getBusinessNameByPayId(merchants.getPayId()));
					user1.setPayId(user.getPayId());
					user1.setStatus(user.getUserStatus().toString());
					merchantListUpdated.add(user1);
					
				}
				
			}else {
				List<Merchants>	merchantReseller1 = new UserDao().getResellerByResellerId(resellerList);
				MerchantObject user1 = new MerchantObject();
				String emailId = userDao.getEmailIdByPayId(merchantPayId);
				user = new UserDao().findByEmailId(emailId);
				user1.setRegistrationDate(user.getRegistrationDate().toString());
				for(Merchants merchantUpdate1 : merchantReseller1) {
					user1.setResellerName(merchantUpdate1.getBusinessName());
				}
				user1.setBusinessName(user.getBusinessName());
				user1.setSubMerchant(userDao.getBusinessNameByPayId(subMerchantPayIdd));
				user1.setPayId(user.getPayId());
				user1.setStatus(user.getUserStatus().toString());
				merchantListUpdated.add(user1);
				
			}
			BigDecimal st = null;

			logger.info("List generated successfully for DownloadPaymentsReportAction");
			SXSSFWorkbook wb = new SXSSFWorkbook(100);
			Row row;
			int rownum = 1;
			// Create a blank sheet
			Sheet sheet =  wb.createSheet("Merchant Details Report");
			row = sheet.createRow(0);
			if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				if(StringUtils.isNotBlank(subMerchantPayIdd)) {
				row.createCell(0).setCellValue("Registration Date");
				row.createCell(1).setCellValue("Reseller Name");
				row.createCell(2).setCellValue("Merchant Name");
				row.createCell(3).setCellValue("Sub Merchant");
				row.createCell(4).setCellValue("Pay ID");
				row.createCell(5).setCellValue("Status");
				
				for (MerchantObject marchantReportList : merchantListUpdated) { 
					row = sheet.createRow(rownum++); 
					Object[] objArr = marchantReportList.myCsvMethodDownloadResellerSubMarchantListView();
						  
						  int cellnum = 0; 
						  for (Object obj : objArr) { // this line creates a cell inthe next column of that row 
						  Cell cell = row.createCell(cellnum++);
						  if (obj instanceof String)
							  cell.setCellValue((String) obj); 
						  else if (obj instanceof Integer) 
							  cell.setCellValue((Integer) obj);
						  
						  }
						  }
				}else {
					row.createCell(0).setCellValue("Registration Date");
					row.createCell(1).setCellValue("Reseller Name");
					row.createCell(2).setCellValue("Merchant Name");
					row.createCell(3).setCellValue("Pay ID");
					row.createCell(4).setCellValue("Status");
					for (MerchantObject marchantReportList : merchantListUpdated) { 
						row = sheet.createRow(rownum++); 
						Object[] objArr = marchantReportList.myCsvMethodDownloadResellerMarchantListView();
							  
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
			}
			String FILE_EXTENSION = ".csv";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			filename = "ResellerMerchantList_Report" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(filename);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(filename + " written successfully on disk.");
			logger.info("File generated successfully for DownloadResellerMerchantListReportAction");
		}catch(Exception e) {
			logger.error("Exception", e);
		}
		return SUCCESS;
	}


	public String getResellerList() {
		return resellerList;
	}

	public void setResellerList(String resellerList) {
		this.resellerList = resellerList;
	}

	public String getReportMerchant() {
		return reportMerchant;
	}

	public void setReportMerchant(String reportMerchant) {
		this.reportMerchant = reportMerchant;
	}
	
	public String getsubMerchant() {
		return subMerchant;
	}

	public void setSubMerchant(String subMerchant) {
		this.subMerchant = subMerchant;
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
