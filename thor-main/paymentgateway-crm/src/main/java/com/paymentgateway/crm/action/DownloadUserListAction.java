package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.user.UserTypeListObject;
import com.paymentgateway.commons.util.Constants;

public class DownloadUserListAction extends AbstractSecureAction{
	
	/**
	 * @Alam
	 */
	private static final long serialVersionUID = -5747450949561514584L;

	@Autowired
	private UserDao userDao;

	private static Logger logger = LoggerFactory.getLogger(DownloadUserListAction.class.getName());

	private String userType;
	private InputStream fileInputStream;
	private String fileName;
	public List<UserTypeListObject> userTypeList = new ArrayList<UserTypeListObject>();
	User sessionUser = new User();

	public String execute() {
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		
		logger.info("inside DownloadUserListAction execute()");
		userTypeList = userDao.getUserListbyUserTypeForDownload(userType, sessionUser);
		
		logger.info("List create successfully for Student Fee Download");

		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		Sheet sheet = wb.createSheet(userType + " List");
		
		if (userType.equalsIgnoreCase(UserType.RESELLER.name())) {

			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Pay Id");
			row.createCell(2).setCellValue("Business Name");
			row.createCell(3).setCellValue("Reseller Type");
			row.createCell(4).setCellValue("Status");
			row.createCell(5).setCellValue("Mobile");
			row.createCell(6).setCellValue("Reg. Date");
			row.createCell(7).setCellValue("Updated Date");
			row.createCell(8).setCellValue("Email ID");
			row.createCell(9).setCellValue("User Type");

			for (UserTypeListObject userObj : userTypeList) {

				row = sheet.createRow(rownum++);
				userObj.setSrNo(String.valueOf(rownum - 1));
				Object[] objArr = userObj.downloadResellerListReport();

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
			
		}else if(userType.equalsIgnoreCase(UserType.MERCHANT.name())) {

			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Pay Id");
			row.createCell(2).setCellValue("Business Name");
			row.createCell(3).setCellValue("Status");
			row.createCell(4).setCellValue("Mobile");
			row.createCell(5).setCellValue("Reg. Date");
			row.createCell(6).setCellValue("Last Updated");
			row.createCell(7).setCellValue("Email ID");
			row.createCell(8).setCellValue("User Type");
			row.createCell(9).setCellValue("Maker Name");
			row.createCell(10).setCellValue("Maker Status");
			row.createCell(11).setCellValue("Maker Status Update");
			row.createCell(12).setCellValue("Checker Name");
			row.createCell(13).setCellValue("Checker Status");
			row.createCell(14).setCellValue("Checker Status Update");

			for (UserTypeListObject userObj : userTypeList) {

				row = sheet.createRow(rownum++);
				userObj.setSrNo(String.valueOf(rownum - 1));
				Object[] objArr = userObj.downloadMerchantOrSuperMerchantListReport();

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
			
		}else if(userType.equalsIgnoreCase(UserType.SUBUSER.name())) {

			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Pay Id");
			row.createCell(2).setCellValue("Business Name");
			row.createCell(3).setCellValue("Parent Name");
			row.createCell(4).setCellValue("Status");
			row.createCell(5).setCellValue("Mobile");
			row.createCell(6).setCellValue("Reg. Date");
			row.createCell(7).setCellValue("Last Updated");
			row.createCell(8).setCellValue("Email ID");
			row.createCell(9).setCellValue("User Type");
			row.createCell(10).setCellValue("Sub-User Type");

			for (UserTypeListObject userObj : userTypeList) {

				row = sheet.createRow(rownum++);
				userObj.setSrNo(String.valueOf(rownum - 1));
				Object[] objArr = userObj.downloadSubUserListReport();

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
			
		}else if(userType.equalsIgnoreCase("SUPER_MERCHANT")) {

			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Pay Id");
			row.createCell(2).setCellValue("Business Name");
			row.createCell(3).setCellValue("Status");
			row.createCell(4).setCellValue("Mobile");
			row.createCell(5).setCellValue("Reg. Date");
			row.createCell(6).setCellValue("Last Updated");
			row.createCell(7).setCellValue("Email ID");
			row.createCell(8).setCellValue("User Type");
			row.createCell(9).setCellValue("Maker Name");
			row.createCell(10).setCellValue("Maker Status");
			row.createCell(11).setCellValue("Maker Status Update");
			row.createCell(12).setCellValue("Checker Name");
			row.createCell(13).setCellValue("Checker Status");
			row.createCell(14).setCellValue("Checker Status Update");

			for (UserTypeListObject userObj : userTypeList) {

				row = sheet.createRow(rownum++);
				userObj.setSrNo(String.valueOf(rownum - 1));
				Object[] objArr = userObj.downloadMerchantOrSuperMerchantListReport();

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
			
		}else if(userType.equalsIgnoreCase("SUB_MERCHANT")) {

			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Pay Id");
			row.createCell(2).setCellValue("Business Name");
			row.createCell(3).setCellValue("Super-Merchant name");
			row.createCell(4).setCellValue("Status");
			row.createCell(5).setCellValue("Mobile");
			row.createCell(6).setCellValue("Reg. Date");
			row.createCell(7).setCellValue("Last Updated");
			row.createCell(8).setCellValue("Email ID");
			row.createCell(9).setCellValue("User Type");

			for (UserTypeListObject userObj : userTypeList) {

				row = sheet.createRow(rownum++);
				userObj.setSrNo(String.valueOf(rownum - 1));
				Object[] objArr = userObj.downloadSubMerchantListReport();

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
			fileName = userType + "_LIST_" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(fileName);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(fileName + " written successfully on disk.");
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		
		return SUCCESS;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	
}
