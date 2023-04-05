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

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.user.Enach;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.crm.mongoReports.TxnReports;

/**
 * @author Rajit
 */

public class EnachTransactionReportAction extends AbstractSecureAction {
	
	@Autowired
	private TxnReports txnReport;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;
	
	private static final long serialVersionUID = -2720630973159739910L;
	private static Logger logger = LoggerFactory.getLogger(EnachTransactionReportAction.class.getName());
	
	private String orderId;
	private String merchantPayId;
	private String subMerchantPayId;
	private String toDate;
	private String fromDate;
	private String pgRefNumber;
	private String umrnNumber;
	private String status;
	private InputStream fileInputStream;
	public String filename;
	private String response;
	private String responseMsg;
	private String txnType;
	private User sessionUser = new User();
	
	List<Enach> aaData = new ArrayList<Enach>();

	public String execute() {

		logger.info("Inside EnachTransactionReportAction execute function ");
		try {
			String resellerId = "";
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				// for Sub Merchant logIn
				if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					merchantPayId = sessionUser.getSuperMerchantId();
					subMerchantPayId = sessionUser.getPayId();

				} else if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isBlank(sessionUser.getSuperMerchantId())) {
					// For Merchant logIn
					merchantPayId = sessionUser.getPayId();
				} else if (sessionUser.isSuperMerchant() == true
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					// For Super Merchant Login
					merchantPayId = sessionUser.getPayId();
					if (!subMerchantPayId.equalsIgnoreCase("ALL")) {
						subMerchantPayId = userDao.getPayIdByEmailId(subMerchantPayId);
					}
				}

			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				resellerId = sessionUser.getPayId();

			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				User parentUser = userDao.findPayId(sessionUser.getParentPayId());
				if (parentUser.isSuperMerchant() == true && StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					// for Super Merchant
					merchantPayId = parentUser.getPayId();

				} else if (parentUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					// for Sub merchant
					merchantPayId = parentUser.getSuperMerchantId();
					subMerchantPayId = parentUser.getPayId();
				} else {
					// for merchant
					merchantPayId = parentUser.getPayId();
				}

			} else {
				/*if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
					subMerchantPayId = userDao.getPayIdByEmailId(subMerchantPayId);
				}*/
			}
			setFromDate(DateCreater.toDateTimeformatCreater(fromDate));
			setToDate(DateCreater.formDateTimeformatCreater(toDate));
			String txnType = "Sale";
			aaData = txnReport.getEnachMandateDetailsForReport(orderId, umrnNumber, merchantPayId, subMerchantPayId,
					resellerId, status, txnType, fromDate, toDate);

		} catch (Exception ex) {
			logger.error("caught exception while view eNach Transaction Report :" , ex);
		}
		return SUCCESS;
	}
	
	public String statusEnquiryForDebitTransaction() {

		Fields fields = new Fields();
		try {
			fields.put(FieldType.ORDER_ID.getName(), orderId);
			fields.put(FieldType.PAY_ID.getName(), merchantPayId);
			fields.put(FieldType.PG_REF_NUM.getName(), pgRefNumber);
			Map<String, String> res = transactionControllerServiceProvider.transact(fields,
					Constants.ICICI_ENACH_TRANSACTION_STATUS_ENQUIRY.getValue());
			response = res.get("responseCode");
			responseMsg = res.get("response");
		} catch (Exception ex) {
			logger.info("exception while status enquiry for schedule transaction " + ex);
			return ERROR;
		}

		return SUCCESS;
	}
	
	public String downloadEnachTransactionDetail() {

		logger.info("inside EnachTransactionReportAction download debit transaction ");
		try {
			String resellerId = "";
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				// for Sub Merchant logIn
				if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					merchantPayId = sessionUser.getSuperMerchantId();
					subMerchantPayId = sessionUser.getPayId();

				} else if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isBlank(sessionUser.getSuperMerchantId())) {
					// For Merchant logIn
					merchantPayId = sessionUser.getPayId();
				} else if (sessionUser.isSuperMerchant() == true
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					// For Super Merchant Login
					merchantPayId = sessionUser.getPayId();
					if (!subMerchantPayId.equalsIgnoreCase("ALL")) {
						//subMerchantPayId = userDao.getPayIdByEmailId(subMerchantPayId);
					}
				}

			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				resellerId = sessionUser.getPayId();

			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				User parentUser = userDao.findPayId(sessionUser.getParentPayId());
				if (parentUser.isSuperMerchant() == true && StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					// for Super Merchant
					merchantPayId = parentUser.getPayId();

				} else if (parentUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					// for Sub merchant
					merchantPayId = parentUser.getSuperMerchantId();
					subMerchantPayId = parentUser.getPayId();
				} else {
					// for merchant
					merchantPayId = parentUser.getPayId();
				}
			}
			aaData = txnReport.getEnachTransactionReport(orderId, "");

			logger.info("List generated successfully for Download Transaction Report");
			SXSSFWorkbook wb = new SXSSFWorkbook(100);
			Row row;
			int rownum = 1;
			Sheet sheet = wb.createSheet("Debit Detail Report");
			row = sheet.createRow(0);

			if (StringUtils.isNotBlank(subMerchantPayId)) {
				row.createCell(0).setCellValue("Sr No");
				row.createCell(1).setCellValue("Merchant Name");
				row.createCell(2).setCellValue("Pay Id");
				row.createCell(3).setCellValue("Sub Merchant Name");
				row.createCell(4).setCellValue("Sub Merchant Pay Id");
				row.createCell(5).setCellValue("Order Id");
				row.createCell(6).setCellValue("Pg Ref Number(Transaction)");
				row.createCell(7).setCellValue("Payment Type");
				row.createCell(8).setCellValue("Registration Date");
				row.createCell(9).setCellValue("Create Date");
				row.createCell(10).setCellValue("Debit Date");
				row.createCell(11).setCellValue("Due Date");
				/*row.createCell(11).setCellValue("Settlement Date");*/
				row.createCell(12).setCellValue("Cust Name");
				row.createCell(13).setCellValue("Cust Email");
				row.createCell(14).setCellValue("Cust Mobile");
				row.createCell(15).setCellValue("Amount");
				row.createCell(16).setCellValue("Txn Amount");
				row.createCell(17).setCellValue("Total Amount");
				row.createCell(18).setCellValue("Status");

				for (Enach eNach : aaData) {

					row = sheet.createRow(rownum++);
					eNach.setSrNo(String.valueOf(rownum - 1));
					Object[] objArr = eNach.methodDownloadEnachTransactionReportForSuperMerchant();

					int cellnum = 0;
					for (Object obj : objArr) {
						// this line creates a cell in the next column of that
						// row
						Cell cell = row.createCell(cellnum++);
						if (obj instanceof String)
							cell.setCellValue((String) obj);
						else if (obj instanceof Integer)
							cell.setCellValue((Integer) obj);

					}
				}
			} else {

				row.createCell(0).setCellValue("Sr No");
				row.createCell(1).setCellValue("Merchant Name");
				row.createCell(2).setCellValue("Pay Id");
				row.createCell(3).setCellValue("Order Id");
				row.createCell(4).setCellValue("Pg Ref Number(Transaction)");
				row.createCell(5).setCellValue("Payment Type");
				row.createCell(6).setCellValue("Registration Date");
				row.createCell(7).setCellValue("Create Date");
				row.createCell(8).setCellValue("Debit Date");
				row.createCell(9).setCellValue("Due Date");
				row.createCell(10).setCellValue("Cust Name");
				row.createCell(11).setCellValue("Cust Email");
				row.createCell(12).setCellValue("Cust Mobile");
				row.createCell(13).setCellValue("Amount");
				row.createCell(14).setCellValue("Txn Amount");
				row.createCell(15).setCellValue("Total Amount");
				row.createCell(16).setCellValue("Status");

				for (Enach eNach : aaData) {

					row = sheet.createRow(rownum++);
					eNach.setSrNo(String.valueOf(rownum - 1));
					Object[] objArr = eNach.methodDownloadEnachTransactionReport();

					int cellnum = 0;
					for (Object obj : objArr) {
						// this line creates a cell in the next column of that
						// row
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
				filename = "Debit_Detail_Report_" + df.format(new Date()) + FILE_EXTENSION;
				File file = new File(filename);

				// this Writes the workbook
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				fileInputStream = new FileInputStream(file);
				addActionMessage(filename + " written successfully on disk.");

			} catch (Exception ex) {
				logger.info("Exception caught in download transaction report" + ex);
				return ERROR;
			}
		} catch (Exception ex) {
			logger.info("exception caught in debitTransaction function " + ex);
		}
		return SUCCESS;
	}
	
	public String downloadEnachTransactionReport() {
		
		logger.info("inside downloadEnachTransactionReport download transaction ");
		try {
			String resellerId = "";
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			
			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				// for Sub Merchant logIn
				if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					merchantPayId = sessionUser.getSuperMerchantId();
					subMerchantPayId = sessionUser.getPayId();

				} else if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isBlank(sessionUser.getSuperMerchantId())) {
					// For Merchant logIn
					merchantPayId = sessionUser.getPayId();
				} else if(sessionUser.isSuperMerchant() == true
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					//For Super Merchant Login
					merchantPayId = sessionUser.getPayId();
					/*if(!subMerchantPayId.equalsIgnoreCase("ALL")) {
						subMerchantPayId = userDao.getPayIdByEmailId(subMerchantPayId);
					}*/
				}

			}  else if(sessionUser.getUserType().equals(UserType.RESELLER)) {
				resellerId = sessionUser.getPayId();
				
			} else if(sessionUser.getUserType().equals(UserType.SUBUSER)) {
				User parentUser = userDao.findPayId(sessionUser.getParentPayId());
				if(parentUser.isSuperMerchant() == true && StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					//for Super Merchant
					merchantPayId = parentUser.getPayId();
					
				} else if(parentUser.isSuperMerchant() == false && StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					//for Sub merchant
					merchantPayId = parentUser.getSuperMerchantId();
					subMerchantPayId = parentUser.getPayId();
				} else {
					//for merchant
					merchantPayId = parentUser.getPayId();
				}
				
			} else {
				/*if(StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
					subMerchantPayId = userDao.getPayIdByEmailId(subMerchantPayId);
				}*/
			}
			setToDate(DateCreater.formDateTimeformatCreater(toDate));
			setFromDate(DateCreater.toDateTimeformatCreater(fromDate));
			
			txnType = "Sale";
			aaData = txnReport.getDownloadEnachTransactionReport(orderId, umrnNumber, merchantPayId, subMerchantPayId, resellerId, status, txnType, fromDate, toDate);
			
			logger.info("List generated successfully for Download Transaction Report");
			SXSSFWorkbook wb = new SXSSFWorkbook(100);
			Row row;
			int rownum = 1;
			Sheet sheet = wb.createSheet("Transaction Report");
			row = sheet.createRow(0);
			
			if(StringUtils.isNotBlank(subMerchantPayId)) {
			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Merchant Name");
			row.createCell(2).setCellValue("Pay Id");
			row.createCell(3).setCellValue("Sub Merchant Name");
			row.createCell(4).setCellValue("Sub Merchant Pay Id");
			row.createCell(5).setCellValue("Order Id");
			row.createCell(6).setCellValue("Pg Ref Number");
			row.createCell(7).setCellValue("Payment Type");
			row.createCell(8).setCellValue("Registration Date");
			row.createCell(9).setCellValue("Create Date");
			row.createCell(10).setCellValue("Debit Date");
			row.createCell(11).setCellValue("Due Date");
			//row.createCell(12).setCellValue("Settled Date");
			row.createCell(12).setCellValue("Cust Name");
			row.createCell(13).setCellValue("Cust Email");
			row.createCell(14).setCellValue("Cust Mobile");
			row.createCell(15).setCellValue("Amount");
			row.createCell(16).setCellValue("Debit Amount");
			row.createCell(17).setCellValue("Total Amount");
			row.createCell(18).setCellValue("Status");
		
			for (Enach eNach : aaData) {

				row = sheet.createRow(rownum++);
				eNach.setSrNo(String.valueOf(rownum - 1));
				Object[] objArr = eNach.methodDownloadEnachTransactionReportForSuperMerchant();

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
			
			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Merchant Name");
			row.createCell(2).setCellValue("Pay Id");
			row.createCell(3).setCellValue("Order Id");
			row.createCell(4).setCellValue("Pg Ref Number");
			row.createCell(5).setCellValue("Payment Type");
			row.createCell(6).setCellValue("Registration Date");
			row.createCell(7).setCellValue("Create Date");
			row.createCell(8).setCellValue("Debit Date");
			row.createCell(9).setCellValue("Due Date");
			//row.createCell(10).setCellValue("Settled Date");
			row.createCell(10).setCellValue("Cust Name");
			row.createCell(11).setCellValue("Cust Email");
			row.createCell(12).setCellValue("Cust Mobile");
			row.createCell(13).setCellValue("Amount");
			row.createCell(14).setCellValue("Debit Amount");
			row.createCell(15).setCellValue("Total Amount");
			row.createCell(16).setCellValue("Status");
		
			for (Enach eNach : aaData) {

				row = sheet.createRow(rownum++);
				eNach.setSrNo(String.valueOf(rownum - 1));
				Object[] objArr = eNach.methodDownloadEnachTransactionReport();

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
				filename = "Transaction_Report_" + df.format(new Date()) + FILE_EXTENSION;
				File file = new File(filename);

				// this Writes the workbook
				FileOutputStream out = new FileOutputStream(file);
				wb.write(out);
				out.flush();
				out.close();
				wb.dispose();
				fileInputStream = new FileInputStream(file);
				addActionMessage(filename + " written successfully on disk.");
			
			
			} catch(Exception ex) {
				logger.info("Exception caught in download transaction report" +ex);
				return ERROR;
			}
			
			
		} catch(Exception ex) {
			logger.info("exception caught in debitTransaction function "+ex);
		}

		
		return SUCCESS;
	}
	
	public List<Enach> getAaData() {
		return aaData;
	}

	public void setAaData(List<Enach> aaData) {
		this.aaData = aaData;
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

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}
	public String getPgRefNumber() {
		return pgRefNumber;
	}

	public void setPgRefNumber(String pgRefNumber) {
		this.pgRefNumber = pgRefNumber;
	}

	public String getUmrnNumber() {
		return umrnNumber;
	}

	public void setUmrnNumber(String umrnNumber) {
		this.umrnNumber = umrnNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
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
	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}
}
