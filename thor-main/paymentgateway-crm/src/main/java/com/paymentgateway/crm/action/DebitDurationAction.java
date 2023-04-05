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
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.FieldType;

/**
 * @author Rajit
 */
public class DebitDurationAction extends AbstractAction {

	@Autowired
	private UserDao userDao;
	
	private static final long serialVersionUID = -7935362786026225687L;
	private static Logger logger = LoggerFactory.getLogger(DebitDurationAction.class.getName());

	private String merchantPayId;
	private String subMerchantPayId;
	private String debitDuration;
	
	private String response;
	private String responseCode;
	
	private InputStream fileInputStream;
	private String filename;
	List<Map<String, String>> aaData = new ArrayList<Map<String, String>>();
	
	public String execute() {
		
		logger.info("inside DebitDurationAction to view debit duration details");
		try {
			setAaData(userDao.fetchDebitDurationDetails(merchantPayId, subMerchantPayId, debitDuration));
		} catch(Exception ex) {
			logger.info("exception caught while get debit duration details" +ex);			
		}
		return SUCCESS;
	}
	
	public String save() {
		
		logger.info("Inside save debit duration ");

		try {
			/*if(StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				subMerchantPayId = userDao.getPayIdByEmailId(subMerchantPayId);
			}*/
			List<Merchants> merchantList = new ArrayList<Merchants>();
			if (merchantPayId.equalsIgnoreCase("ALL")) {

				merchantList = userDao.getMerchantAndSubMerchantList();
				for (Merchants merchant : merchantList) {
					userDao.updateDebitDuration(debitDuration, merchant.getPayId());
				}

			} else if (!merchantPayId.equalsIgnoreCase("ALL") && StringUtils.isNotBlank(subMerchantPayId)
					&& subMerchantPayId.equalsIgnoreCase("ALL")) {

				merchantList = userDao.getSubMerchantList();
				for (Merchants merchant : merchantList) {
					userDao.updateDebitDuration(debitDuration, merchant.getPayId());
				}

			} else if (!merchantPayId.equalsIgnoreCase("ALL") && StringUtils.isNotBlank(subMerchantPayId)
					&& !subMerchantPayId.equalsIgnoreCase("ALL")) {

				userDao.updateDebitDuration(debitDuration, subMerchantPayId);

			} else if (!merchantPayId.equalsIgnoreCase("ALL") && StringUtils.isBlank(subMerchantPayId)) {

				userDao.updateDebitDuration(debitDuration, merchantPayId);
			}

			setResponse("Debit Duration has been saved successfully");
			setResponseCode("success");
		} catch (Exception ex) {
			logger.info("exception caught while save debit duration " + ex);
			setResponse("Debit Duration could'nt saved successfully");
			setResponseCode("fail");
		}
		return SUCCESS;
	}
	
	public String edit() {

		logger.info("inside edit debit duration");
		try {			
				userDao.updateDebitDuration(debitDuration, merchantPayId);

			setResponse("Debit Duration edit successfully");
			setResponseCode("success");
		} catch (Exception ex) {
			logger.info("exception caught while edit debit duration " + ex);
			setResponse("Debit Duration could'nt edit successfully");
			setResponseCode("fail");
		}
		return SUCCESS;
	}
	
	public String downloadDebitDetails() {
		logger.info("inside downloadDebitDetails for download");
		try {
			setAaData(userDao.fetchDebitDurationDetails(merchantPayId, subMerchantPayId, debitDuration));
			
			logger.info("List generated successfully for Download Debit Duration Report");
			SXSSFWorkbook wb = new SXSSFWorkbook(100);
			Row row;
			int rownum = 1;
			Sheet sheet = wb.createSheet("Debit Duration Report");
			row = sheet.createRow(0);

			if(merchantPayId.equalsIgnoreCase("ALL") ||StringUtils.isNotBlank(subMerchantPayId)) {
				
				row.createCell(0).setCellValue("Sr No");
				row.createCell(1).setCellValue("Pay Id");
				row.createCell(2).setCellValue("Merchant Name");
				row.createCell(3).setCellValue("Sub Merchant Pay Id");
				row.createCell(4).setCellValue("Sub Merchant Name");
				row.createCell(5).setCellValue("Debit Duration");
			} else {
				
				row.createCell(0).setCellValue("Sr No");
				row.createCell(1).setCellValue("Pay Id");
				row.createCell(2).setCellValue("Merchant Name");
				row.createCell(3).setCellValue("Debit Duration");
			}
			for (Map<String, String> debitDuration : aaData) {

				row = sheet.createRow(rownum++);
				Object[] objArr = new Object[6];

				objArr[0] = String.valueOf(rownum - 1);
				
				if(merchantPayId.equalsIgnoreCase("ALL") ||StringUtils.isNotBlank(subMerchantPayId)) {
					
					objArr[1] = debitDuration.get(FieldType.PAY_ID.getName());
					objArr[2] = debitDuration.get(FieldType.MERCHANT_NAME.getName());
					objArr[3] = debitDuration.get(FieldType.SUB_MERCHANT_ID.getName());
					objArr[4] = debitDuration.get("SUB_MERCHANT_NAME");
					objArr[5] = debitDuration.get(FieldType.DEBIT_DAY.getName());
				} else {
					
					objArr[1] = debitDuration.get(FieldType.PAY_ID.getName());
					objArr[2] = debitDuration.get(FieldType.MERCHANT_NAME.getName());
					objArr[3] = debitDuration.get(FieldType.DEBIT_DAY.getName());
				}
				
				int cellnum = 0;
				for (Object obj : objArr) {
					
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Integer)
						cell.setCellValue((Integer) obj);
				}
			}
			try {
				String FILE_EXTENSION = ".xlsx";
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				filename = "Debit_Duration_Report_" + df.format(new Date()) + FILE_EXTENSION;
				File file = new File(filename);

				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				fileInputStream = new FileInputStream(file);
				addActionMessage(filename + " written successfully on disk.");

			} catch (Exception ex) {
				logger.info("Exception caught while generate download debit duration report" + ex);
				return ERROR;
			}
			
		} catch(Exception ex) {
			logger.info("exception in download debit duration excel file "+ex);
			
		}
		return SUCCESS;
	}
	
	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

	public String getDebitDuration() {
		return debitDuration;
	}

	public void setDebitDuration(String debitDuration) {
		this.debitDuration = debitDuration;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
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
	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	public List<Map<String, String>> getAaData() {
		return aaData;
	}

	public void setAaData(List<Map<String, String>> aaData) {
		this.aaData = aaData;
	}
}
