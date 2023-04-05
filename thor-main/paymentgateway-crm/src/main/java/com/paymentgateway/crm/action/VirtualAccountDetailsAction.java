package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
import com.paymentgateway.commons.user.VirtualACDetailsObject;
import com.paymentgateway.commons.util.Constants;

public class VirtualAccountDetailsAction extends AbstractSecureAction {

	/**
	 * Alam
	 */
	private static final long serialVersionUID = 4598358279982577906L;

	private static Logger logger = LoggerFactory.getLogger(VirtualAccountDetailsAction.class.getName());

	@Autowired
	private UserDao userDao;

	@Autowired
	private VirtualAcDetailsService virtualAcDetailsService;

//	private String virtualAccountNo;
//	private String virtualIfscCode;
//	private String virtualBeneficiaryName;
//	private String createDate;
	private String payId;
	private String subMerchantId;
	private InputStream fileInputStream;
	private String filename;
	private User sessionUser = new User();
	List<VirtualACDetailsObject> aaData;

	public String execute() {

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {

			if (StringUtils.isNotBlank(payId) && !payId.equalsIgnoreCase("ALL")) {
				aaData = virtualAcDetailsService.getVirtualAccountDetails(payId, subMerchantId);
			} else {
				aaData = virtualAcDetailsService.getAllVirtualAccountDetails();
			}

		}

		return SUCCESS;
	}

	public String downloadDetails() {

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<VirtualACDetailsObject> virtualAcDetailsList = new ArrayList<VirtualACDetailsObject>();

		logger.info("List generated successfully for DownloadPaymentsReportAction");
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		// Create a blank sheet
		Sheet sheet = wb.createSheet("Virtual Account Details Report");
		row = sheet.createRow(0);

		if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {

			if (StringUtils.isNotBlank(payId) && !payId.equalsIgnoreCase("ALL")) {
				virtualAcDetailsList = virtualAcDetailsService.getVirtualAccountDetails(payId, subMerchantId);
			} else {
				virtualAcDetailsList = virtualAcDetailsService.getAllVirtualAccountDetails();
			}

		}

		if (StringUtils.isNotBlank(payId) && StringUtils.isBlank(subMerchantId)) {

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Merchant Name");
			row.createCell(2).setCellValue("Virtual Account NO");
			row.createCell(3).setCellValue("Virtual Ifsc Code");
			row.createCell(4).setCellValue("Virtual Beneficiary Name");
			row.createCell(5).setCellValue("Create Date");

			for (VirtualACDetailsObject virtualAcDetails : virtualAcDetailsList) {
				row = sheet.createRow(rownum++);
				virtualAcDetails.setSrNo(String.valueOf(rownum - 1));
				Object[] objArr = virtualAcDetails.csvFileDownloadMethodForMerchant();

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
		} else if (StringUtils.isNotBlank(subMerchantId)) {

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Merchant Name");
			row.createCell(2).setCellValue("Sub-Merchant Name");
			row.createCell(3).setCellValue("Virtual Account NO");
			row.createCell(4).setCellValue("Virtual Ifsc Code");
			row.createCell(5).setCellValue("Virtual Beneficiary Name");
			row.createCell(6).setCellValue("Create Date");

			for (VirtualACDetailsObject virtualAcDetails : virtualAcDetailsList) {
				row = sheet.createRow(rownum++);
				virtualAcDetails.setSrNo(String.valueOf(rownum - 1));
				Object[] objArr = virtualAcDetails.csvFileDownloadMethodForMerchantAndSubMerchant();

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
			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Merchant Name");
			row.createCell(2).setCellValue("Virtual Account NO");
			row.createCell(3).setCellValue("Virtual Ifsc Code");
			row.createCell(4).setCellValue("Virtual Beneficiary Name");
			row.createCell(5).setCellValue("Create Date");

			for (VirtualACDetailsObject virtualAcDetails : virtualAcDetailsList) {
				row = sheet.createRow(rownum++);
				virtualAcDetails.setSrNo(String.valueOf(rownum - 1));
				Object[] objArr = virtualAcDetails.csvFileDownloadMethodForMerchant();

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
			filename = "Virtual_Account_Details" + FILE_EXTENSION;
			File file = new File(filename);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(filename + " written successfully on disk.");
			logger.info("File generated successfully for VirtualAccountDetailsAction");
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}

		return SUCCESS;
	}


	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getSubMerchantId() {
		return subMerchantId;
	}

	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
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

	public List<VirtualACDetailsObject> getAaData() {
		return aaData;
	}

	public void setAaData(List<VirtualACDetailsObject> aaData) {
		this.aaData = aaData;
	}

}
